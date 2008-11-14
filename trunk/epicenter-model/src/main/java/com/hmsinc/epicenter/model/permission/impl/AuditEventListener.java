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
package com.hmsinc.epicenter.model.permission.impl;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.ui.rememberme.RememberMeProcessingFilter;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.permission.AuditEvent;
import com.hmsinc.epicenter.model.permission.AuditEventType;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.PermissionRepository;

/**
 * Simple event listener that generates AuditEvents for Acegi events.
 * 
 * @author shade
 * @version $Id: AuditEventListener.java 1568 2008-04-18 13:08:44Z steve.kondik $
 */
public class AuditEventListener implements ApplicationListener {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private PermissionRepository permissionRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Transactional
	public void onApplicationEvent(ApplicationEvent event) {

		if (event instanceof InteractiveAuthenticationSuccessEvent) {

			final InteractiveAuthenticationSuccessEvent authEvent = (InteractiveAuthenticationSuccessEvent) event;
			if (authEvent.getAuthentication() != null && authEvent.getAuthentication().getPrincipal() != null) {

				final EpiCenterUser user = permissionRepository.load(((EpiCenterUser) authEvent.getAuthentication()
						.getPrincipal()).getId(), EpiCenterUser.class);
				Validate.notNull(user, "Invalid user");

				if (RememberMeProcessingFilter.class.equals(authEvent.getGeneratedBy())) {
					logger.info("User \"{}\" logged in via RememberMeProcessingFilter", user.getUsername());
				} else {
					final AuditEvent auditEvent = new AuditEvent(user, AuditEventType.LOGIN);
					auditEvent.setDetails(authEvent.getAuthentication().getDetails().toString());

					permissionRepository.save(auditEvent);
				}

			}
		}
	}

}
