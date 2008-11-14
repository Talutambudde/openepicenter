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

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.apache.commons.lang.WordUtils;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.workflow.Event;

public class EmailMessageRenderer {
	
	@Resource(name = "velocityEngine")
	private VelocityEngine velocityEngine;

	private String url;
	
	public void prepare(final MimeMessageHelper message, String encoding, final Event event, final EpiCenterUser user)
			throws MessagingException, VelocityException {
					
		message.setSubject(EventNotifierUtils.getTemplate(event, user, "/templates/email-subject.vm", encoding, url, velocityEngine));
		
		String textContent = EventNotifierUtils.getTemplate(event, user, "/templates/email-text.vm", encoding, url, velocityEngine);
		String htmlContent = EventNotifierUtils.getTemplate(event, user, "/templates/email-html.vm", encoding, url, velocityEngine);

		message.setText(wrap(textContent), htmlContent);
	}

	private String wrap(String text) {
		StringBuffer rtn = new StringBuffer();

		for (String line : text.split("\n")) {
			rtn.append(WordUtils.wrap(line, 80)).append("\n");
		}
		return rtn.toString();
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	@Required
	public void setUrl(String url) {
		this.url = url;
	}
	
}
