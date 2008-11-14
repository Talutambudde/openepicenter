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
package com.hmsinc.epicenter.webapp.dto;

import java.io.Serializable;

/**
 * @author shade
 * @version $Id: AuthorizedRegionDTO.java 1654 2008-05-13 14:26:16Z steve.kondik $
 */
public class AuthorizedRegionDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7038900155862800563L;

	private Long grantedById;

	private String type;

	private Long geographyId;

	/**
	 * @return the grantedById
	 */
	public Long getGrantedById() {
		return grantedById;
	}

	/**
	 * @param grantedById
	 *            the grantedById to set
	 */
	public void setGrantedById(Long grantedById) {
		this.grantedById = grantedById;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the geographyId
	 */
	public Long getGeographyId() {
		return geographyId;
	}

	/**
	 * @param geographyId
	 *            the geographyId to set
	 */
	public void setGeographyId(Long geographyId) {
		this.geographyId = geographyId;
	}

}
