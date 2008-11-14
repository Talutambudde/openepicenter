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

import ca.uhn.hl7v2.model.v25.datatype.IS;
import ca.uhn.hl7v2.model.v25.message.ADT_A03;

import com.hmsinc.epicenter.integrator.DuplicateDataException;
import com.hmsinc.epicenter.integrator.IncompleteDataException;
import com.hmsinc.epicenter.integrator.IncompleteDataException.IncompleteDataType;
import com.hmsinc.epicenter.model.health.Discharge;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.mergence.model.HL7Message;

/**
 * Handles discharge (ADT_A03) messages.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @org.apache.xbean.XBean element="dischargeService" description="EpiCenter
 *                         Discharge Service"
 */
public class DischargeService extends AbstractHL7EventHandler<Discharge> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.integrator.service.event.HL7EventService#handleEvent(com.hmsinc.mergence.model.HL7Message,
	 *      com.hmsinc.epicenter.model.health.PatientDetail)
	 */
	public Discharge handleEvent(HL7Message message, PatientDetail details) throws Exception {

		Validate.notNull(message);
		Validate.notNull(details);

		// Make sure we have a patientId
		if (details.getPatient().getPatientId() == null) {
			throw new IncompleteDataException("No patient ID in message.", IncompleteDataType.PATIENT_ID);
		}

		final Discharge discharge = new Discharge();
		configureInteraction(message, discharge, details);

		Validate.isTrue(message.getMessage() instanceof ADT_A03);
		final ADT_A03 adt = (ADT_A03) message.getMessage();

		// Visit number (PV1-19) - REQUIRED FOR DISCHARGE
		discharge.setVisitNumber(StringUtils.trimToNull(adt.getPV1().getVisitNumber().getIDNumber().getValue()));
		if (discharge.getVisitNumber() == null) {
			throw new IncompleteDataException("No visit number in message.", IncompleteDataType.VISIT_NUMBER);
		}

		// Disposition (PV1-36)
		final IS disposition = adt.getPV1().getDischargeDisposition();
		if (disposition == null) {
			throw new IncompleteDataException("No discharge disposition in message.", IncompleteDataType.DISCHARGE_DISPOSITION);
		}
		
		discharge.setDisposition(disposition.getValue());

		
		// Parse integer since that's the main thing we'll use here.
		if (StringUtils.isNumeric(discharge.getDisposition())) {
			discharge.setDisposition(Integer.valueOf(discharge.getDisposition()).toString());
		}
		
		// Discharge date (PV1-44)
		discharge.setInteractionDate(extractDate(adt.getPV1().getDischargeDateTime(0).getTime(), message));

		if (logger.isDebugEnabled()) {
			logger.debug(discharge.toString());
		}
		
		return discharge;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.integrator.service.event.HL7EventHandler#checkForExistingEvent(com.hmsinc.epicenter.model.health.Interaction)
	 */
	public void checkForExistingInteraction(Interaction interaction) throws DuplicateDataException {
		Validate.isTrue(interaction instanceof Discharge);
		final Discharge discharge = (Discharge)interaction;
		
		final Long existingId = healthRepository.findExistingDischarge(discharge);
		if (existingId != null) {
			throw new DuplicateDataException("Discharge already exists.", existingId);
		}
	}

}
