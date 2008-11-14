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

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:PermissionExceptionType.java 220 2007-07-17 14:59:08Z
 *          steve.kondik $
 */
public enum PermissionExceptionType {

	AUTHENTICATION_FAILED, 
	USERNAME_ALREADY_EXISTS, 
	EMAIL_ALREADY_EXISTS, 
	USER_IS_DISABLED, 
	NO_VALID_ORGANIZATIONS,
	NO_VALID_REGIONS,
	ORGANIZATION_ALREADY_EXISTS, 
	UNKNOWN_USER, 
	UNKNOWN_ORGANIZATION, 
	ACCESS_DENIED;
}
