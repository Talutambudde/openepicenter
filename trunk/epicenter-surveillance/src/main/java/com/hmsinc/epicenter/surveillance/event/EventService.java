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
package com.hmsinc.epicenter.surveillance.event;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceMethod;
import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResult;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.epicenter.model.surveillance.SurveillanceSet;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;
import com.hmsinc.epicenter.surveillance.notification.NotificationService;
import com.hmsinc.ts4j.TimeSeriesEntry;
import com.hmsinc.epicenter.util.FormatUtils;

/**
 * Generates events.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: EventService.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
public class EventService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private NotificationService notificationService;

	@Resource
	private SurveillanceRepository surveillanceRepository;
	
	@Resource
	private WorkflowRepository workflowRepository;
	
	/**
	 * @param eventDate
	 * @param task
	 * @param method
	 * @param set
	 * @param geography
	 * @param classification
	 * @param result
	 * @param analysisTime
	 */
	@Transactional
	public void handleEvent(final DateTime eventDate, final SurveillanceTask task, final SurveillanceMethod method,
			final SurveillanceSet set, final Geography geography, final Classification classification,
			final SurveillanceResult result, final DateTime analysisTime) {
		
		if (!checkForRecentAnomaly(task, method, set, geography, classification, analysisTime)) {
			createAnomaly(analysisTime, task, method, set, geography, classification, result, analysisTime);
		}
	}
	
	/**
	 * @param method
	 * @param geography
	 * @param classification
	 */
	@Transactional
	private void createAnomaly(final DateTime eventDate, final SurveillanceTask task, final SurveillanceMethod method,
		final SurveillanceSet set, final Geography geography, final Classification classification,
		final SurveillanceResult result, final DateTime analysisTime) {
		
		
		logger.info("Creating anomaly for task '{}', method '{}', set '{}', geography '{}', classification '{}', "
				+ "analysis time '{}'", new Object[] { task.getId(), method.getName(),
				set.getDescription(), geography.getDisplayName(), classification.getCategory(),
				analysisTime });
		
		final TimeSeriesEntry actualResult = result.getResults().get(SurveillanceResultType.ACTUAL).last();
    		
		final String description = createAnomalyDescription(method, geography, classification, actualResult);
			
		final EventDisposition initialDisposition = workflowRepository.getInitialDisposition();
		Validate.notNull(initialDisposition, "Could not find initial event disposition");
		
		final Anomaly anomaly = new Anomaly(description, geography, initialDisposition, task.getOrganization(),
				task, method, set, classification, result, analysisTime);
    
		surveillanceRepository.save(anomaly);
    
		notificationService.sendNotifications(anomaly);
		
	}

	/**
	 * @param task
	 * @param method
	 * @param geography
	 * @param classification
	 * @param analysisTimestamp
	 * @return
	 */
	@Transactional
	private boolean checkForRecentAnomaly(final SurveillanceTask task, final SurveillanceMethod method, final SurveillanceSet set, 
			final Geography geography, final Classification classification, final DateTime analysisTime) {
		
		boolean rtn = false;
		
		final Anomaly anomaly = surveillanceRepository.getLatestAnomaly(geography, classification, task, method, set, analysisTime);
		
		if (anomaly != null && anomaly.getAnalysisTimestamp().isAfter(analysisTime.minusDays(1))) {
			
			incrementLastSeenCounter(anomaly);
			logger.debug("Found recent existing anomaly '{}', not creating new one", anomaly);
			rtn = true;
		}
		
		return rtn;
	}
	
	/**
	 * @param anomaly
	 */
	@Transactional
	private int incrementLastSeenCounter(final Anomaly anomaly) {
		anomaly.setCount(anomaly.getCount() + 1);
		anomaly.setLastOccurrence(new DateTime());
		surveillanceRepository.save(anomaly);
		
		return anomaly.getCount();
	}
	
	/**
	 * @param method
	 * @param geography
	 * @param classification
	 * @param actualResult
	 * @return
	 */
	private static String createAnomalyDescription(final SurveillanceMethod method, final Geography geography,
			final Classification classification, final TimeSeriesEntry actualResult) {
		
		return new StringBuilder(classification.getCategory())
			.append(" by ").append(method.getName()).append(" in ").append(geography.getDisplayName())
			.append(" found ").append(actualResult.getValue()).append(" visits ")
			.append(" with a maximum of ").append(FormatUtils.round(actualResult.getDoubleProperty(ResultType.THRESHOLD), 2)).toString();
			
	}

}
