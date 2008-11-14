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
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.Subscription;
/**
 * Sents event notifications via email.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: MailingEventNotifier.java 860 2008-02-04 21:07:25Z olek.poplavsky $
 */
public class MailingEventNotifier implements EventNotifier {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private JavaMailSender mailSender;
	
	@Resource
	private EmailMessageRenderer emailMessageRenderer;

	private String mailFrom;
	
	public void notify(final Event event, final EpiCenterUser user) {
		if (user.isEnabled()) {
			try {
				notifyInternal(event, user, null);
			} catch (MailException e) {
				logger.error("Can not send email notification", e);
			}
		}
	}
	
	public void notify(final Event event, final Subscription subscription) {
		if (subscription.getUser().isEnabled()) {
			try {
				notifyInternal(event, subscription.getUser(), subscription);
			} catch (MailException e) {
				logger.error("Can not send email notification", e);
			}
		}
	}

	/**
	 * @param mailFrom the mailFrom to set
	 */
	public void setMailFrom(String mailFrom) {
		this.mailFrom = mailFrom;
	}
	
	private void notifyInternal(final Event event, final EpiCenterUser user, final Subscription subscription) {
		if (shouldNotify(user, subscription)) {
    		logger.debug("Sending email event notification for {} to {}.", EventNotifierUtils.format(event),
					(subscription == null) ? EventNotifierUtils.format(user) : EventNotifierUtils.format(subscription));
    		mailSender.send(new MimeMessagePreparator() {
    			public void prepare(MimeMessage mimeMessage) throws Exception {
    				String encoding = "UTF-8";
    				MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
    				    MimeMessageHelper.MULTIPART_MODE_RELATED, encoding);
    				message.setFrom(mailFrom);
    				message.setTo(emailAddress(user, subscription));
    				emailMessageRenderer.prepare(message, encoding, event, user);
    			}
    		});
		}
	}
	
	private boolean shouldNotify(final EpiCenterUser user, final Subscription subscription) {
		boolean rtn = true;
		if (subscription != null) {
			rtn = subscription.getType().equals(Subscription.EMAIL);
    	}
		return rtn;
	}
	
	private String emailAddress(final EpiCenterUser user, final Subscription subscription) {
		String rtn = user.getEmailAddress();
		if (subscription != null) {
			if (subscription.getType().equals(Subscription.EMAIL)) {
				if (subscription.getDestination().length() > 0) {
					rtn = subscription.getDestination();
				}
			}
    	}
		return rtn;
	}
}
