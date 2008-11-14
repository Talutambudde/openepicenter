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
package com.hmsinc.epicenter.model.geography;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Enumeration of Geography types and associated classes.  Used to map strings to types.
 */
@XmlEnum(String.class)
@XmlType(namespace = "http://epicenter.hmsinc.com/model")
public enum GeographyType {

	REGION(Region.class, "R"), STATE(State.class, "S"), COUNTY(County.class, "C"), ZIPCODE(Zipcode.class, "Z");

	private final Class<? extends Geography> geoClass;

	private final String discriminator;
	
	GeographyType(Class<? extends Geography> geoClass, String discriminator) {
		this.geoClass = geoClass;
		this.discriminator = discriminator;
	}

	/**
	 * @return the geoClass
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> Class<T> getGeoClass() {
    	// FIXME: This is a HACK.  How to properly parameterize this?!
		return (Class<T>) geoClass;
	}

	/**
	 * @return the discriminator
	 */
	public String getDiscriminator() {
		return discriminator;
	}
	
}
