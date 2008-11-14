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
package com.hmsinc.epicenter.integrator;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:IncompleteDataException.java 137 2007-05-17 17:58:03Z steve.kondik $
 */
public class IncompleteDataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 518431922903559088L;

	public enum IncompleteDataType {
		EVENT_DATE,
		EVENT_TYPE,
		GENDER,
		PATIENT_ID,
		PATIENT_CLASS,
		REASON,
		VISIT_NUMBER,
		DISCHARGE_DISPOSITION,
		ZIPCODE;

	}

	private IncompleteDataType type;

	/**
	 * @param message
	 * @param t
	 * @param type
	 */
	public IncompleteDataException(String message, Throwable t, IncompleteDataType type) {
		super(message, t);
		this.type = type;
	}

	/**
	 * @param message
	 * @param type
	 */
	public IncompleteDataException(String message, IncompleteDataType type) {
		super(message);
		this.type = type;
	}
	
	/**
	 * @return the type
	 */
	public IncompleteDataType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(IncompleteDataType type) {
		this.type = type;
	}

}
