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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.Subscription;

/**
 * Records notifications to the log file, sends nothing to the user.
 * This notifier is intended to be used for internal testing only.
 * 
 * @author Olek Poplavsky
 * @version $Id: LoggingEventNotifier.java 858 2008-02-04 19:22:24Z olek.poplavsky $
 */
public class LoggingEventNotifier implements EventNotifier {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void notify(final Event event, final EpiCenterUser user) {
		logger.info("Logging event notification for " + EventNotifierUtils.format(event)
            + ", normally would be sent to " + EventNotifierUtils.format(user));
	}
	
	public void notify(final Event event, final Subscription subscription) {
		logger.info("Logging event notification for " + EventNotifierUtils.format(event)
            + ", normally would be sent to " + EventNotifierUtils.format(subscription));
	}
}
