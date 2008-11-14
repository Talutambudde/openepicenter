/**
 * Copyright (C) 2008 University of Pittsburgh
 * 
 * 
 * This file is part of Open EpiCenter
 * 
 *     Open EpiCenter is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     Open EpiCenter is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with Open EpiCenter.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 *   
 */
package com.hmsinc.epicenter.integrator.service.event;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.DataTypeException;
import ca.uhn.hl7v2.model.v25.datatype.CE;
import ca.uhn.hl7v2.model.v25.datatype.DTM;
import ca.uhn.hl7v2.model.v25.message.ADT_A01;
import ca.uhn.hl7v2.util.Terser;

import com.hmsinc.epicenter.integrator.IncompleteDataException;
import com.hmsinc.epicenter.integrator.IncompleteDataException.IncompleteDataType;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.attribute.PatientClass;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.mergence.model.HL7Message;
import com.hmsinc.mergence.monitoring.AlertService;
import com.hmsinc.mergence.util.HAPIUtils;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 *
 */
public abstract class AbstractHL7EventHandler<E extends Interaction> implements HL7EventHandler<E> {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected static final int MAX_ICD9_LENGTH = 80;

	protected static final int MAX_REASON_LENGTH = 400;
	
	@Resource
	protected AlertService alertService;

	@Resource
	protected AttributeRepository attributeRepository;
	
	@Resource
	protected HealthRepository healthRepository;
	
	protected E configureInteraction(HL7Message message, E interaction, PatientDetail detail) throws HL7Exception, IncompleteDataException {

		final Terser t = new Terser(message.getMessage());
		final String patientClass = t.get("/PV1-2");
	
		if (patientClass != null) {
			final PatientClass pc = attributeRepository.getPatientClassByAbbreviation(patientClass);
			if (pc == null) {
				throw new IncompleteDataException("Unsupported patient class in message: " + patientClass, IncompleteDataType.PATIENT_CLASS);
			}
			interaction.setPatientClass(pc);

		} else {
			throw new IncompleteDataException("No patient class in message", IncompleteDataType.PATIENT_CLASS);
		}
		
		interaction.setPatient(detail.getPatient());
		interaction.setPatientDetail(detail);
		interaction.setMessageId(message.getId());
		
		// Set the age
		interaction.setAgeAtInteraction(extractAge(message, detail));
		
		// Age group
		interaction.setAgeGroup(attributeRepository.getAgeGroupForAge(interaction.getAgeAtInteraction()));
		
		return interaction;
	}
	
	protected Integer extractAge(HL7Message message, PatientDetail detail) throws HL7Exception {
		
		Integer age = null;
		
		// Set the age using PID-7-2 if necessary
		if (detail.getDateOfBirth() == null) {

			// Check PID-7-2 for Age
			final String ageStr = StringUtils.trimToNull(message.getTerser().get("/PID-7-2"));

			if (ageStr == null) {
				logger.error("No date of birth or age set in message");
			} else if (StringUtils.isNumeric(ageStr)) {
				Integer ageAtInteraction = Integer.valueOf(ageStr);
				if (ageAtInteraction < 200) {
					age = ageAtInteraction;
				} else {
					logger.error("Age out of range: {}", ageAtInteraction);
				}
				logger.debug("Using age from PID-7-2: {}", ageAtInteraction);
			} else {
				logger.error("Non-numeric age specified in message");
			}
		}
		return age;
	}
	
	/**
	 * Extracts a Calendar object from a v2.5 DTM type.
	 * 
	 * @param dateTime
	 * @param message
	 * @return
	 * @throws IncompleteDataException
	 */
	protected DateTime extractDate(final DTM dateTime, final HL7Message message) throws IncompleteDataException {
		
		if (dateTime == null) {
			throw new IncompleteDataException("No date was set in message", IncompleteDataType.EVENT_DATE);
		}
		
		DateTime ret = null;
		
		if (dateTime.getValue() == null || dateTime.getValue().length() < 12) {

			// We want to also send an alert if the facility sends a
			// truncated date.
			final IncompleteDataException e = new IncompleteDataException("Incomplete date in message", IncompleteDataType.EVENT_DATE);
			alertService.sendAlert(AlertService.Severity.ERROR, message, e);
			throw e;
		}

		try {
			ret = HAPIUtils.createDateFromDTM(dateTime);
		} catch (DataTypeException e) {
			throw new IncompleteDataException(e.getMessage(), IncompleteDataType.EVENT_DATE);
		}

		if (ret == null) {
			throw new IncompleteDataException("No date was set in message", IncompleteDataType.EVENT_DATE);
		}
		
		return ret;
	}
	
	/**
	 * Extracts a diagnosis from an ADT_A01 structure.
	 * 
	 * @param adt
	 * @return
	 * @throws HL7Exception
	 */
	protected static String extractDiagnosis(final ADT_A01 adt) throws HL7Exception {
		
		String ret = null;

		// ICD9 Description
		if (adt.getDG1Reps() > 0) {

			final StringBuilder descs = new StringBuilder();

			for (int i = 0; i < adt.getDG1Reps(); i++) {

				if (adt.getDG1(i).getDiagnosisDescription() != null) {
					final String desc = StringUtils.trimToNull(adt.getDG1(i).getDiagnosisDescription().getValue());

					if (desc != null && isValidComplaint(desc) && (descs.length() + desc.length() + 1) < MAX_REASON_LENGTH) {
						if (descs.length() > 0) {
							descs.append("/");
						}
						descs.append(desc);
					}
				}
			}

			ret = StringUtils.trimToNull(descs.toString());
		}
		
		return ret;
	}
	
	/**
	 * Extracts ICD9 codes from an ADT_A01 v2.5 structure.
	 * 
	 * @param adt
	 * @return
	 * @throws HL7Exception
	 */
	protected static String extractICD9Codes(final ADT_A01 adt) throws HL7Exception {
		
		String ret = null;
		
		if (adt.getDG1Reps() > 0) {

			final StringBuilder codes = new StringBuilder();

			for (int i = 0; i < adt.getDG1Reps(); i++) {

				if (adt.getDG1(i).getDiagnosisCodeDG1().getIdentifier().getValue() != null) {
					final String code = StringUtils.trimToNull(adt.getDG1(i).getDiagnosisCodeDG1().getIdentifier().getValue());

					if (code != null && (codes.length() + code.length() + 1 < MAX_ICD9_LENGTH)) {
						if (codes.length() > 0) {
							codes.append("/");
						}
						codes.append(code);
					}
				}
			}
			
			ret = StringUtils.trimToNull(codes.toString());
		}
		return ret;
	}
	
	/**
	 * @param adt
	 * @return
	 * @throws HL7Exception
	 */
	protected static String extractReason(final ADT_A01 adt) {
	
		String ret = null;
		
		final CE complaint = adt.getPV2().getAdmitReason();
		final StrBuilder reasonBuf = new StrBuilder();

		final String complaintId = StringUtils.trimToNull(complaint.getIdentifier().getValue());
		final String complaintText = StringUtils.trimToNull(complaint.getText().getValue());
		if (complaintId != null && isValidComplaint(complaintId)) {
			reasonBuf.append(complaintId);
		}
		if (complaintText != null && isValidComplaint(complaintText)) {
			if (reasonBuf.length() > 0) {
				reasonBuf.append(" ");
			}
			reasonBuf.append(complaintText);
		}
		ret = StringUtils.trimToNull(reasonBuf.toString());

		return ret;
	}
	
	/**
	 * @param complaint
	 * @return
	 */
	protected static boolean isValidComplaint(final String complaint) {
		boolean ret = false;
		if (complaint != null && StringUtils.trimToNull(complaint.replaceAll("[^a-zA-Z\\s]", "")) != null) {
			ret = true;
		}
		return ret;
	}	
}