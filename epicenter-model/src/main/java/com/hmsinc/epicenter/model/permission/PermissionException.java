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
 * @version $Id:PermissionException.java 220 2007-07-17 14:59:08Z steve.kondik $
 * 
 */
public class PermissionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7919990345851071975L;

	private PermissionExceptionType type;

	/**
	 * @param message
	 */
	public PermissionException(String message) {
		super(message);
	}

	/**
	 * @param type
	 * @param message
	 */
	public PermissionException(PermissionExceptionType type, String message) {
		super(type.toString() + ": " + message);
		this.type = type;
	}

	/**
	 * @param t
	 */
	public PermissionException(Throwable t) {
		super(t);
	}

	/**
	 * @param message
	 * @param t
	 */
	public PermissionException(String message, Throwable t) {
		super(message, t);
	}

	/**
	 * @param type
	 * @param message
	 * @param t
	 */
	public PermissionException(PermissionExceptionType type, String message, Throwable t) {
		super(type.toString() + ": " + message, t);
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public PermissionExceptionType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(PermissionExceptionType type) {
		this.type = type;
	}

}
