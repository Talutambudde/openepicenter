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
/**
 * 
 */
package com.hmsinc.epicenter.model.initialization.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.initialization.InitializationTask;
import com.hmsinc.epicenter.model.permission.EpiCenterRole;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.permission.PermissionExceptionType;
import com.hmsinc.epicenter.model.permission.PermissionRepository;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;

/**
 * Initializes the default roles, administrative user, and administrative
 * organization.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: InitializeRolesAndUsers.java 1570 2008-04-18 13:43:25Z steve.kondik $
 */
public class InitializeRolesAndUsers implements InitializationTask {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private PermissionRepository permissionRepository;

	@Resource
	private WorkflowRepository workflowRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.initialization.InitializationTask#executeTask()
	 */
	public void executeTask() {

		final List<EpiCenterRole> allRoles = new ArrayList<EpiCenterRole>();
		
		final EpiCenterRole adminRole = new EpiCenterRole("ROLE_ADMIN", "Global Administrators");
		final EpiCenterRole userRole = new EpiCenterRole("ROLE_USER", "Standard User");
		
		allRoles.add(adminRole);
		allRoles.add(userRole);
		
		allRoles.add(new EpiCenterRole("ROLE_ORG_ADMIN", "Organization Administrators"));

		final List<EpiCenterRole> roles = permissionRepository.getList(EpiCenterRole.class);
		for (EpiCenterRole role : allRoles) {
			if (!roles.contains(role)) {
				logger.info("Creating role: " + role.toString());
				permissionRepository.save(role);
			}
		}

		try {
			permissionRepository.getGlobalAdministrators();
		} catch (PermissionException e) {

			if (e.getType().equals(PermissionExceptionType.UNKNOWN_ORGANIZATION)) {

				final Organization admins = new Organization(PermissionRepository.GLOBAL_ADMIN_ORG,
						"Users with full administrative rights to the EpiCenter system.", workflowRepository
								.getDefaultWorkflow());

				final EpiCenterUser user = new EpiCenterUser("admin", "password", "root@localhost.localdomain");
				user.setFirstName("System");
				user.setLastName("Administrator");

				user.getRoles().add(adminRole);
				user.getRoles().add(userRole);

				admins.getUsers().add(user);

				try {

					permissionRepository.addUser(user);
					permissionRepository.save(admins);
					logger.info("Created Global Administrator user and organization.");

				} catch (PermissionException ex) {
					throw new RuntimeException(ex);
				}
			} else {
				throw new RuntimeException(e);
			}
		}
	}

}
