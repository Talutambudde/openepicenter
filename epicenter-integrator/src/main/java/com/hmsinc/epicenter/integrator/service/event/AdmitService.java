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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import ca.uhn.hl7v2.model.v25.message.ADT_A01;

import com.hmsinc.epicenter.integrator.DuplicateDataException;
import com.hmsinc.epicenter.integrator.IncompleteDataException;
import com.hmsinc.epicenter.integrator.IncompleteDataException.IncompleteDataType;
import com.hmsinc.epicenter.model.health.Admit;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.mergence.model.HL7Message;

/**
 * Handles admit (ADT_A01) messages.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @org.apache.xbean.XBean element="admitService" description="EpiCenter Admit Service"
 */
public class AdmitService extends AbstractHL7EventHandler<Admit> {

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.integrator.service.event.HL7EventService#handleEvent(com.hmsinc.mergence.model.HL7Message, com.hmsinc.epicenter.model.health.PatientDetail)
	 */
	public Admit handleEvent(HL7Message message, PatientDetail details) throws Exception {
		
		Validate.notNull(message);
		Validate.notNull(details);

		// Make sure we have a patientId
		if (details.getPatient().getPatientId() == null) {
			throw new IncompleteDataException("No patient ID in message.", IncompleteDataType.PATIENT_ID);
		}

		final Admit admit = new Admit();
		configureInteraction(message, admit, details);
		
		Validate.isTrue(message.getMessage() instanceof ADT_A01);
		final ADT_A01 adt = (ADT_A01) message.getMessage();

		// Visit number (PV1-19) - REQUIRED FOR ADMIT
		admit.setVisitNumber(StringUtils.trimToNull(adt.getPV1().getVisitNumber().getIDNumber().getValue()));
		if (admit.getVisitNumber() == null) {
			throw new IncompleteDataException("No visit number in message.", IncompleteDataType.VISIT_NUMBER);
		}

		// Check for an existing record
		final Long existingId = healthRepository.findExistingAdmit(admit);
		if (existingId != null) {
			throw new DuplicateDataException("Admit already exists.", existingId);
		}
		
		// Admit date
		admit.setInteractionDate(extractDate(adt.getPV1().getAdmitDateTime().getTime(), message));
		
		// ICD9 Codes
		admit.setIcd9(extractICD9Codes(adt));
				
		// Diagnosis - use DG1 first, and PV2-3 if blank.
		final String diagnosis = extractDiagnosis(adt);
		if (diagnosis != null && isValidComplaint(diagnosis)) {
			admit.setReason(diagnosis);
		} else {
			admit.setReason(extractReason(adt));
		}

		if (admit.getReason() == null && admit.getIcd9() == null) {
			throw new IncompleteDataException("No admit reason or ICD9 code provided.", IncompleteDataType.REASON);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug(admit.toString());
		}
		
		return admit;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.integrator.service.event.HL7EventHandler#checkForExistingEvent(com.hmsinc.epicenter.model.health.Interaction)
	 */
	public void checkForExistingInteraction(Interaction interaction) throws DuplicateDataException {
		Validate.isTrue(interaction instanceof Admit);
		final Admit admit = (Admit)interaction;
		
		final Long existingId = healthRepository.findExistingAdmit(admit);
		if (existingId != null) {
			throw new DuplicateDataException("Admit already exists.", existingId);
		}	
	}

	
}
