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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.hibernate.annotations.Where;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:State.java 144 2007-05-19 07:57:56Z steve.kondik $
 */
@Entity
@DiscriminatorValue("S")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all", region = "com.hmsinc.epicenter.model.GeographyCache")
@NamedQueries( { @NamedQuery(name = "exportState", query = "from State s fetch all properties order by s.name"),
		@NamedQuery(name = "getStatesAndCounties", query = "from State s left join fetch s.counties order by s.name"),
		@NamedQuery(name = "getStateByAbbreviation", query = "from State s where upper(s.abbreviation)=upper(:abbreviation)")})
@XmlRootElement(name = "state", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "State", namespace = "http://epicenter.hmsinc.com/model")
public class State extends Geography implements ZipcodeContainer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6012290999157473311L;

	private String abbreviation;

	private Set<County> counties = new TreeSet<County>();

	private Set<Zipcode> zipcodes = new TreeSet<Zipcode>();

	/**
	 * @return the abbreviation
	 */
	@XmlID
	@XmlAttribute(required = true)
	@Column(name = "ABBREVIATION", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
	public String getAbbreviation() {
		return abbreviation;
	}

	/**
	 * @param abbreviation
	 *            the abbreviation to set
	 */
	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	/**
	 * @return the counties
	 */
	@XmlElementWrapper(name = "counties", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "county", namespace = "http://epicenter.hmsinc.com/model")
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "state")
	@Where(clause = "type = 'C'")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
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

	/**
	 * @return the zipcodes
	 */
	@XmlTransient
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "state")
	@Where(clause = "type = 'Z'")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
	@Sort(type = SortType.NATURAL)
	public Set<Zipcode> getZipcodes() {
		return zipcodes;
	}

	/**
	 * @param zipcodes
	 *            the zipcodes to set
	 */
	public void setZipcodes(Set<Zipcode> zipcodes) {
		this.zipcodes = zipcodes;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.Geography#getDisplayName()
	 */
	@Override
	@Transient
	public String getDisplayName() {
		return this.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("abbreviation", getAbbreviation()).toString();
	}
}
