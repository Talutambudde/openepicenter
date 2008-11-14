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

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.generic.NumberTool;
import org.joda.time.DateTimeZone;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.hmsinc.epicenter.model.attribute.Attribute;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.Subscription;
import com.hmsinc.epicenter.velocity.DateTimeFormatTool;

// @review Use a StringBuilder for this.
public class EventNotifierUtils {

	static String format(final Event event) {
		String rtn = "[Event";
		rtn += " description = ";
		rtn += event.getDescription();
		rtn += "]";
		return rtn;
	}

	static String format(final Subscription subscription) {
		String rtn = "[Subscription";
		rtn += " type = ";
		rtn += subscription.getType();
		rtn += ", destination = ";
		rtn += subscription.getDestination();
		rtn += ", user = ";
		rtn += format(subscription.getUser());
		rtn += "]";
		return rtn;
	}

	static String format(final EpiCenterUser user) {
		String rtn = "[User";
		rtn += " username = ";
		rtn += user.getUsername();
		rtn += ", email = ";
		rtn += user.getEmailAddress();
		rtn += ", phone = ";
		rtn += user.getPhoneNumber();
		rtn += "]";
		return rtn;
	}

	private static String makeAttributeString(final Collection<? extends Attribute> attributes) {
		final StringBuilder sb = new StringBuilder();
		if (attributes.size() == 1) {
			sb.append(attributes.iterator().next().getName().toLowerCase());
		} else if (attributes.size() > 1) {
			int i = 0;
			for (Attribute a : attributes) {
				i++;
				if (sb.length() > 0) {
					if (i == attributes.size()) {
						sb.append(" or ");
					} else {
						sb.append(", ");
					}
				}
				sb.append(a.getName().toLowerCase());
			}
		}
		return sb.toString();
	}
	
	public static String makeAgeGroupAndGenderString(final Anomaly anomaly) {
		
		// Just do this in Java because it's too goofy in Velocity
		final String genders = StringUtils.trimToNull(makeAttributeString(anomaly.getSet().getAttribute(com.hmsinc.epicenter.model.attribute.Gender.class)));
		final String ageGroups = StringUtils.trimToNull(makeAttributeString(anomaly.getSet().getAttribute(com.hmsinc.epicenter.model.attribute.AgeGroup.class)));
		
		final StringBuilder sb = new StringBuilder();
		if (genders != null || ageGroups != null) {
			if (ageGroups != null) {
				sb.append("grouped by age as ").append(ageGroups);
			}
			if (genders != null) {
				if (sb.length() > 0) {
					sb.append("; and ");
				}
				sb.append("grouped by gender as ").append(genders);
			}
			sb.append(") ");
			sb.insert(0, "(for patients ");
		}
		
		return sb.toString();
	}
	
	
	public static String getTemplate(final Event event, final EpiCenterUser user, final String templateName,
			final String encoding, final String url, final VelocityEngine velocityEngine) {

		Map<String, Object> context = new HashMap<String, Object>();
		context.put("date", new DateTimeFormatTool());
		context.put("number", new NumberTool());
		context.put("event", event);
		context.put("user", user);

		context.put("locale", Locale.getDefault());
		context.put("timezone", DateTimeZone.getDefault());

		if (event instanceof Anomaly) {

			Anomaly anomaly = (Anomaly) event;

			context.put("analysisStartTime", anomaly.getAnalysisTimestamp().minusDays(1));
			context.put("predictedObservedValueDouble", anomaly.getPredictedObservedValue());
			context.put("epicenterUrl", url);

			
			context.put("attributes", makeAgeGroupAndGenderString(anomaly));
		}
		return VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, encoding, context);
	}
}
