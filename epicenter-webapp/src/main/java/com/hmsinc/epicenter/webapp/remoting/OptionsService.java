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
package com.hmsinc.epicenter.webapp.remoting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.util.ReflectionUtils;

/**
 * Service to handle user options.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: OptionsService.java 1568 2008-04-18 13:08:44Z steve.kondik $
 */
@RemoteProxy(name = "OptionsService")
public class OptionsService extends AbstractRemoteService {

	private static final List<String> USER_ATTRS = Arrays.asList("emailAddress", "firstName", "lastName", "middleInitial", "title",
			"organization", "address", "city", "state", "zipcode", "phoneNumber", "faxNumber");

	/**
	 * @param oldPassword
	 * @param newPassword
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public boolean changePassword(final String oldPassword, final String newPassword) {

		Validate.notNull(getPrincipal());
		Validate.notNull(oldPassword, "Old password was null!");
		Validate.notNull(newPassword, "New password was null!");

		boolean success = false;
		try {
			permissionRepository.authenticateUser(getPrincipal().getUsername(), oldPassword);
			permissionRepository.changePassword(getPrincipal(), newPassword);
			logger.info("Password changed for user " + getPrincipal().getUsername());
			success = true;
		} catch (PermissionException e) {
			logger.error("Authentication failed for password change!", e);
		}
		return success;
	}

	/**
	 * @return
	 */
	@Secured("ROLE_USER")
	@RemoteMethod
	public EpiCenterUser getUserInfo() {

		Validate.notNull(getPrincipal());
		return getPrincipal();
	}

	/**
	 * @param user
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public EpiCenterUser updateUserInfo(final EpiCenterUser user) {

		Validate.notNull(getPrincipal());
		Validate.notNull(user);
		
		// Apply the permitted fields to the object and save
		for (String attr : USER_ATTRS) {
			ReflectionUtils.copyProperty(user, getPrincipal(), String.class, attr);
			permissionRepository.update(getPrincipal());
		}
		return getPrincipal();
	}
	
	/**
	 * @param prefs
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public EpiCenterUser updatePreferences(final Map<String, String> prefs) {
		
		Validate.notNull(prefs);
		final EpiCenterUser user = getPrincipal();
		user.getPreferences().putAll(prefs);
			
		logger.debug("New prefs: {}", prefs);
		
		final Set<String> emptyKeys = new HashSet<String>();
		for (Map.Entry<String, String> entry : user.getPreferences().entrySet()) {
			final String value = StringUtils.trimToNull(entry.getValue());
			if (value == null) {
				emptyKeys.add(entry.getKey());
			} else {
				entry.setValue(value);
			}
		}
		
		for (String empty : emptyKeys) {
			user.getPreferences().remove(empty);
		}
		logger.debug("Cleaned prefs: {}", user.getPreferences());
		
		logger.trace("Prefs for user {}: {}", user.getUsername(), user.getPreferences());
		
		permissionRepository.save(user);
		
		return user;
		
	}
	
}
