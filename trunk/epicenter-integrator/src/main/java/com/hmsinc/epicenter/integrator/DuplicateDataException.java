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
 * @version $Id: DuplicateDataException.java 96 2007-04-12 20:08:42Z
 *          steve.kondik $
 */
public class DuplicateDataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5316451112855316540L;

	private Long originalId;

	/**
	 * 
	 */
	public DuplicateDataException() {
	}

	/**
	 * @param message
	 */
	public DuplicateDataException(String message) {
		super(message);
	}

	/**
	 * @param message
	 */
	public DuplicateDataException(String message, Long originalId) {
		super(message);
		this.originalId = originalId;
	}

	/**
	 * @param cause
	 */
	public DuplicateDataException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateDataException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param originalId
	 */
	public DuplicateDataException(String message, Throwable cause, Long originalId) {
		super(message, cause);
		this.originalId = originalId;
	}

	/**
	 * @return the originalId
	 */
	public Long getOriginalId() {
		return originalId;
	}

	/**
	 * @param originalId
	 *            the originalId to set
	 */
	public void setOriginalId(Long originalId) {
		this.originalId = originalId;
	}

}
