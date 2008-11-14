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
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.permission.PermissionExceptionType;
import com.hmsinc.epicenter.model.permission.PermissionRepository;

/**
 * Spring Security UserDetailsService for EpiCenter.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EpiCenterUserDetailsService.java 205 2007-09-26 16:52:01Z
 *          steve.kondik $
 */
public class EpiCenterUserDetailsService implements UserDetailsService {

	@Resource
	private PermissionRepository permissionRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.security.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException, DataAccessException {

		Validate.notNull(username, "Username must be provided.");

		final EpiCenterUser user;
		try {
			user = permissionRepository.getUserByUsername(username);
			if (user.getOrganizations() == null || user.getOrganizations().size() == 0) {
				throw new UsernameNotFoundException(PermissionExceptionType.NO_VALID_ORGANIZATIONS + ": " + username);
			}
			if (user.getVisibleRegion() == null) {
				throw new UsernameNotFoundException(PermissionExceptionType.NO_VALID_REGIONS + ": " + username);
			}

		} catch (PermissionException e) {
			throw new UsernameNotFoundException(e.getType().toString() + ": " + username);
		}

		return user;
	}

}
