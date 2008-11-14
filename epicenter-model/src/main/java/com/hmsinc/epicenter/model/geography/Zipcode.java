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

import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Zipcode.java 144 2007-05-19 07:57:56Z steve.kondik $
 */
@Entity
@DiscriminatorValue("Z")
@NamedQueries( { 
	@NamedQuery(name = "getZipcodesByNameInState", query = "from Zipcode z where z.name in (:names) and z.state = :state order by z.name"),
	@NamedQuery(name = "getZipcodesForGeographyInState", query = "from Zipcode z where state = :state and filter(z.centroid, :geo) = 'TRUE' and within(z.centroid, :geo) = 'TRUE' order by z.name"),
	@NamedQuery(name = "getZipcodesForGeographyInCounty", query = "from Zipcode z where z.counties in (:county) and filter(z.centroid, :geo) = 'TRUE' and within(z.centroid, :geo) = 'TRUE' order by z.name")})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all", region = "com.hmsinc.epicenter.model.GeographyCache")
@XmlRootElement(name = "zipcode", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Zipcode", namespace = "http://epicenter.hmsinc.com/model")
public class Zipcode extends StateFeature {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8974481077858399930L;

	private String postOfficeName;

	private Set<County> counties = new TreeSet<County>();

	/**
	 * @return the postOfficeName
	 */
	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	@Column(name = "PO_NAME", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
	public String getPostOfficeName() {
		return postOfficeName;
	}

	/**
	 * @param postOfficeName
	 *            the postOfficeName to set
	 */
	public void setPostOfficeName(String postOfficeName) {
		this.postOfficeName = postOfficeName;
	}

	/**
	 * @return the counties
	 */
	@XmlTransient
	@ManyToMany(mappedBy = "zipcodes", targetEntity = County.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
	@ForeignKey(name = "FK_GEO_COUNTY_ZIPCODE_2")
	@Sort(type = SortType.NATURAL)
	public Set<County> getCounties() {
		return counties;
	}

	/**
	 * @param counties
	 *            the counties to set
	 */
	public void setCounties(Set<County> counties) {
		this.counties = counties;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.Geography#getDisplayName()
	 */
	@Override
	@Transient
	public String getDisplayName() {
		return getState() == null ? getName() : getName() + ", " + getState().getAbbreviation(); 
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("postOfficeName",	getPostOfficeName()).toString();
	}
}
