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
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.epicenter.model.health.Registration;
import com.hmsinc.mergence.model.HL7Message;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AdmitService.java 219 2007-07-17 14:37:39Z steve.kondik $
 * @org.apache.xbean.XBean element="registrationService" description="EpiCenter
 *                         Registration Service"
 */
public class RegistrationService extends AbstractHL7EventHandler<Registration> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.integrator.service.event.HL7EventService#handleEvent(com.hmsinc.mergence.model.HL7Message,
	 *      com.hmsinc.epicenter.model.health.PatientDetail)
	 */
	public Registration handleEvent(HL7Message message, PatientDetail details) throws Exception {

		Validate.notNull(message);
		Validate.notNull(details);

		Validate.isTrue(message.getMessage() instanceof ADT_A01, "Message should be an ADT_A01 structure, but was: " + message.getMessage().getClass().getName());
		final ADT_A01 adt = (ADT_A01) message.getMessage();

		Registration registration = new Registration();
		configureInteraction(message, registration, details);

		// Visit number
		registration.setVisitNumber(StringUtils.trimToNull(adt.getPV1().getVisitNumber().getIDNumber().getValue()));

		// Admit date
		registration.setInteractionDate(extractDate(adt.getPV1().getAdmitDateTime().getTime(), message));

		// Admit reason (try both component 1 and 2)
		final String reason = extractReason(adt);
		
		if (reason != null && isValidComplaint(reason)) {
			registration.setReason(reason);
		}

		// ICD9 Codes
		registration.setIcd9(extractICD9Codes(adt));

		// Use ICD9 Description if no registrationReason
		if (registration.getReason() == null) {
			registration.setReason(extractDiagnosis(adt));
		}

		if (registration.getReason() == null && registration.getIcd9() == null) {
			throw new IncompleteDataException("No registration reason or ICD9 code provided.",
					IncompleteDataType.REASON);
		}

		if (logger.isDebugEnabled()) {
			logger.debug(registration.toString());
		}
		
		return registration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.integrator.service.event.HL7EventHandler#checkForExistingEvent(com.hmsinc.epicenter.model.health.Interaction)
	 */
	public void checkForExistingInteraction(Interaction interaction) throws DuplicateDataException {
		Validate.isTrue(interaction instanceof Registration);
		final Registration event = (Registration)interaction;
		
		final Long existingId = healthRepository.findExistingNaturalKey(event.getNaturalKey());
		if (existingId != null) {
			throw new DuplicateDataException("Registration already exists [naturalID: " + event.getNaturalKey() + "]", existingId);
		}
	}

}
