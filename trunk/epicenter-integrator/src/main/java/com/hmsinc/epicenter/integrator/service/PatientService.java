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
package com.hmsinc.epicenter.integrator.service;

import java.util.SortedSet;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.util.Terser;

import com.hmsinc.epicenter.integrator.IncompleteDataException;
import com.hmsinc.epicenter.integrator.stats.StatisticsService;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.health.Patient;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.hmsinc.epicenter.model.util.InvalidZipcodeException;
import com.hmsinc.mergence.components.InvalidMessageException;
import com.hmsinc.mergence.model.HL7Message;
import com.hmsinc.mergence.util.ER7Utils;

/**
 * Manages Patients from HL7 messages.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:PatientParser.java 367 2006-10-12 19:49:54Z steve.kondik $
 * @org.apache.xbean.XBean element="patientService" description="EpiCenter
 *                         Integration Patient Service"
 */
public class PatientService {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private AttributeRepository attributeRepository;

	@Resource
	private HealthRepository healthRepository;

	@Resource
	private ProviderRepository providerRepository;

	@Resource
	private StatisticsService statisticsService;

	protected PatientDetail doPatient(final HL7Message message) throws HL7Exception, InvalidMessageException {

		PatientDetail details = null;

		if (message == null) {
			throw new InvalidMessageException("Message was null!");
		}

		final Patient patient = parsePatient(message);
		if (patient == null) {
			throw new InvalidMessageException("Patient parsing result was null!");
		}

		details = parseDetails(message, patient);

		return details;
	}

	private Patient parsePatient(final HL7Message message) throws InvalidMessageException, HL7Exception {

		final Facility facility = providerRepository.getFacilityByIdentifier(message.getDataSource().getSendingFacility());
		if (facility == null) {
			throw new InvalidMessageException("Facility not found: " + message.getDataSource().toString());
		}

		Patient patient = null;
		final Terser t = message.getTerser();

		// See if this Patient is already in the database..
		final String patientID = StringUtils.trimToNull(t.get("/PID-3"));

		if (patientID == null) {
			logger.warn("No patient ID set in message");
		} else {
			patient = healthRepository.getPatient(patientID, facility);
		}

		if (patient == null) {

			logger.debug("Creating new patient record");
			patient = new Patient(patientID, facility);

		} else {

			logger.debug("Found existing patient record: {}", patient.getId());
		}

		return patient;
	}

	private PatientDetail parseDetails(final HL7Message message, final Patient patient) throws HL7Exception {

		final Terser t = message.getTerser();

		// Create new set of Details
		PatientDetail patientDetail = new PatientDetail();
		patientDetail.setPatient(patient);

		// Zipcode
		String zipcode = t.get("/PID-11-5");

		try {
			if (zipcode == null) {
				throw new InvalidZipcodeException("No patient zipcode provided.");
			}
			patientDetail.setZipcode(zipcode);
		} catch (InvalidZipcodeException ize) {
			logger.error(ize.getMessage());
			statisticsService.updateProviderStats(message, StatisticsService.StatsType.INCOMPLETE, "missing: "
					+ IncompleteDataException.IncompleteDataType.ZIPCODE);
		}

		// Date of Birth
		final String dob = StringUtils.trimToNull(t.get("/PID-7"));
		if (dob == null) {
			logger.warn("No date of birth set in message");
		} else {
			patientDetail.setDateOfBirth(ER7Utils.fromER7Date(dob));
		}

		// Gender
		String genderAbbr = StringUtils.trimToNull(t.get("/PID-8"));
		if (genderAbbr == null) {
			genderAbbr = "U";
			logger.warn("No gender set in message");
			statisticsService.updateProviderStats(message, StatisticsService.StatsType.INCOMPLETE, "missing: "
					+ IncompleteDataException.IncompleteDataType.GENDER);
		}

		patientDetail.setGender(attributeRepository.getGenderByAbbreviation(genderAbbr));

		final SortedSet<PatientDetail> sortedDetails = patient.getPatientDetails();

		if (patient.getPatientId() == null) {

			// Just save it.
			logger.debug("Saving details without patientID");
			sortedDetails.add(patientDetail);

		} else if (sortedDetails.size() == 0) {

			// Just save it.
			logger.debug("Creating initial detail record");
			sortedDetails.add(patientDetail);

		} else {

			final PatientDetail latestDetail = sortedDetails.last();
			if (patientDetail.equals(latestDetail)) {

				patientDetail = latestDetail;
				logger.debug("Using existing detail record: {}", latestDetail.getId());

			} else {

				logger.debug("Creating updated detail record");
				logger.debug("Old: {}  New: {}", latestDetail, patientDetail);
				sortedDetails.add(patientDetail);

			}
		}

		return patientDetail;
	}

	protected void savePatient(final Patient patient) {
		healthRepository.save(patient);
	}
}
