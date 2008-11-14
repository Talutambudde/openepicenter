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
package com.hmsinc.epicenter.model.permission;

import com.hmsinc.epicenter.model.Repository;
import java.util.List;
import java.util.Set;

/**
 * Manages the repository of PermissionObjects.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:PermissionRepository.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
public interface PermissionRepository extends Repository<PermissionObject, Long> {

	public static final String GLOBAL_ADMIN_ORG = "Global Administrators";
	
	public static final String GLOBAL_ADMIN_USER = "admin";
	
	public EpiCenterUser authenticateUser(final String username, final String password) throws PermissionException;
	
	public void addUser(final EpiCenterUser user) throws PermissionException;
	
	public void addOrganization(final Organization organization) throws PermissionException;
	
	public void changePassword(final EpiCenterUser user, final String newPassword);
	
	public EpiCenterUser getUserByUsername(final String username) throws PermissionException;
	
	public EpiCenterUser getUserByEmailAddress(final String emailAddress) throws PermissionException;
	
	public boolean checkForExistingUser(final String username, final String emailAddress);
	
	public boolean checkForExistingOrganization(final String organizationName);
	
	public Organization getGlobalAdministrators() throws PermissionException;
	
	public List<Organization> getOrganizations(final boolean enabled);
	
	public EpiCenterRole getRole(final String roleName);
	
	public void purgeExpiredTokens();
	
	public PasswordResetToken getPasswordResetToken(final String token);
	
	public Organization getOrganization(final Long organizationId);
	
	public List<Organization> getContainingOrganizations(final Organization organization);
	
	public List<Organization> getSponsorTree(final Organization organization);
	
	public Set<Organization> getVisibleOrganizations(final EpiCenterUser user);
	
}
