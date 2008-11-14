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
package com.hmsinc.epicenter.model.util;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $id$
 */
public class InvalidZipcodeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -237063648807795926L;

	/**
	 * 
	 */
	public InvalidZipcodeException() {
	}

	/**
	 * @param message
	 */
	public InvalidZipcodeException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidZipcodeException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidZipcodeException(String message, Throwable cause) {
		super(message, cause);
	}

}
