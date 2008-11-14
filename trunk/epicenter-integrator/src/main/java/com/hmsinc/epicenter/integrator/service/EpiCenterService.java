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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataIntegrityViolationException;

import com.hmsinc.epicenter.integrator.DuplicateDataException;
import com.hmsinc.epicenter.integrator.IncompleteDataException;
import com.hmsinc.epicenter.integrator.service.event.HL7EventHandler;
import com.hmsinc.epicenter.integrator.stats.StatisticsService;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.epicenter.service.ClassificationService;
import com.hmsinc.mergence.components.InvalidMessageException;
import com.hmsinc.mergence.model.HL7Message;
import com.hmsinc.mergence.monitoring.AlertService;
import com.hmsinc.mergence.monitoring.AlertService.Severity;
import com.hmsinc.mergence.util.HAPIUtils;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EpiCenterService.java 136 2007-05-17 17:13:24Z steve.kondik $
 * @org.apache.xbean.XBean
 */
public class EpiCenterService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private AlertService alertService;

	@Resource
	private ClassificationService classificationService;

	@Resource
	private PatientService patientService;

	@Resource
	private StatisticsService statisticsService;
	
	@Resource
	private Map<String, HL7EventHandler<? extends Interaction>> eventHandlers;
		
	private static final List<String> REQUIRED_FIELDS = Collections.unmodifiableList(Arrays.asList( "MSH", "PID", "PV1", "PV2", "DG1", "IN1" ));

	public void doProcess(final HL7Message hl7) throws Exception {

		if (hl7 == null) {
			throw new InvalidMessageException("HL7 message was null!");
		} else if (hl7.getDataSource() == null || hl7.getType() == null || hl7.getMessage() == null) {
			throw new InvalidMessageException("Missing data in HL7 message!");
		} else if (hl7.getId() == null) {
			throw new InvalidMessageException("No ID set on message.  Message must pass thru the messageStore first!");

		} else {
			
			hl7.setMessage(HAPIUtils.standardizeMessage(hl7.getMessage(), REQUIRED_FIELDS, "2.5"));
						
			PatientDetail details = null;

			try {
				// Create a patient record
				details = patientService.doPatient(hl7);

				if (details != null) {

					if (eventHandlers.containsKey(hl7.getType())) {
						
						final HL7EventHandler<? extends Interaction> handler = eventHandlers.get(hl7.getType());
						final Interaction interaction = handler.handleEvent(hl7, details);
						
						// Run the classifications
						if (interaction != null) {
							
							handler.checkForExistingInteraction(interaction);
							
							details.getPatient().getInteractions().add(interaction);
							
							final Set<Classification> classifications = classificationService.classify(interaction);
							if (classifications != null && classifications.size() > 0) {
								interaction.getClassifications().addAll(classifications);
							}
						}
						
						logger.debug("Patient details: {}", details);
						patientService.savePatient(details.getPatient());
						
					} else {
						logger.warn("No handler for message type: {}", hl7.getType());
					}
					
				} else {
					logger.warn("Unable to parse patient information from message: {}", hl7);
				}

			} catch (DataIntegrityViolationException e) {

				// Send an alert..
				logger.error(e.getMessage(), e);
				alertService.sendAlert(Severity.ERROR, hl7, e);

			} catch (IncompleteDataException e) {
				
				statisticsService.updateProviderStats(hl7, StatisticsService.StatsType.INCOMPLETE, "missing: " + e.getType().toString());
				logger.warn("{} [missing: {}  message: {}]", new Object[] { e.getMessage(), e.getType(), hl7 } );
				
			} catch (DuplicateDataException e) {

				statisticsService.updateProviderStats(hl7, StatisticsService.StatsType.DUPLICATE, "original: " + e.getOriginalId().toString());
				logger.warn("{} [original: {}]", e.getMessage(), e.getOriginalId());
				
			} finally {

				if (details != null && details.getPatient() != null && logger.isDebugEnabled()) {
					logger.info("Message processing complete. [{}]", details.getPatient());
				} else {
					logger.info("Message processing complete.");
				}
			}
		}

	}

	/**
	 * @return the eventHandlers
	 */
	public Map<String, HL7EventHandler<? extends Interaction>> getEventHandlers() {
		return eventHandlers;
	}

	/**
	 * @param eventHandlers the eventHandlers to set
	 */
	@Required
	public void setEventHandlers(Map<String, HL7EventHandler<? extends Interaction>> eventHandlers) {
		this.eventHandlers = eventHandlers;
	}
	
}
