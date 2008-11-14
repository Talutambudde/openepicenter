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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyType;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.hmsinc.epicenter.webapp.util.Visibility;

/**
 * Simple DTO to convey Geography data and visibility.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: GeographyDTO.java 1412 2008-04-02 00:13:53Z steve.kondik $
 */
@DataTransferObject
public class GeographyDTO implements Serializable, Comparable<GeographyDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2790401440094134910L;

	private final Long id;

	private final String name;

	private final Visibility visibility;

	private final Long population;

	private final GeographyType type;
	
	/**
	 * @param geography
	 * @param visibility
	 */
	public GeographyDTO(Geography geography, Visibility visibility) {
		super();
		this.id = geography.getId();
		this.name = geography.getDisplayName();
		this.population = geography.getPopulation();
		this.visibility = visibility;
		this.type = GeographyType.valueOf(ModelUtils.getRealClass(geography).getSimpleName().toUpperCase());
	}

	/**
	 * @param geography
	 */
	public GeographyDTO(Geography geography, EpiCenterUser user) {
		super();
		this.id = geography.getId();
		this.name = geography.getDisplayName();
		this.population = geography.getPopulation();
		this.visibility = SpatialSecurity.getVisibility(user, geography);
		this.type = GeographyType.valueOf(ModelUtils.getRealClass(geography).getSimpleName().toUpperCase());
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the population
	 */
	public Long getPopulation() {
		return population;
	}

	/**
	 * @return the visibility
	 */
	public Visibility getVisibility() {
		return visibility;
	}

	/**
	 * @return the type
	 */
	public GeographyType getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(GeographyDTO g) {
		return new CompareToBuilder().append(name, g.getName()).append(id, g.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		return EqualsBuilder.reflectionEquals(this, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
