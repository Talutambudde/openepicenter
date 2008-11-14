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
package com.hmsinc.epicenter.surveillance.notification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.Subscription;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;

/**
 * Sends event notifications.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: NotificationService.java 1548 2008-04-11 16:33:58Z steve.kondik $
 */
public class NotificationService {
	
	@Resource
	private WorkflowRepository workflowRepository;

	private Map<String, EventNotifier> notifiers = new HashMap<String, EventNotifier>();

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * @param event
	 */
	@Transactional(readOnly = true)
	public void sendNotifications(final Event event) {
		
		if (!(event instanceof Anomaly)) {
			logger.error("Event '{}' is not an anomaly, we do not handle sending notifications to anything but "
					+ "anomalies for now", event.getDescription());
			return;
		}
		
		Anomaly anomaly = (Anomaly) event;
		List<Subscription> subscriptions = workflowRepository.getSubscriptions(anomaly.getClassification(), anomaly
				.getGeography());
		
		for (Subscription subscription : subscriptions) {
			for (EventNotifier notifier : notifiers.values()) {
				notifier.notify(event, subscription);
			}
		}
	}

	/**
	 * @param notifiers the notifiers to set
	 */
	@Required
	public void setNotifiers(Map<String, EventNotifier> notifiers) {
		this.notifiers = notifiers;
	}

}
