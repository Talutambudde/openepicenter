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
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:County.java 144 2007-05-19 07:57:56Z steve.kondik $
 */
@Entity
@DiscriminatorValue("C")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all", region = "com.hmsinc.epicenter.model.GeographyCache")
@NamedQueries( { @NamedQuery(name = "getCountiesInState", query = "from County c where state = :state order by c.name"),
	@NamedQuery(name = "getCountiesByNameInState", query = "from County c where c.name in (:names) and c.state = :state order by c.name") } )
@XmlRootElement(name = "county", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "County", namespace = "http://epicenter.hmsinc.com/model")
public class County extends StateFeature {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2201958870398295263L;

	private Set<Zipcode> zipcodes = new TreeSet<Zipcode>();


	/**
	 * @return the zipcodes
	 */
	@XmlElementWrapper(name = "zipcodes", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "zipcode", namespace = "http://epicenter.hmsinc.com/model")
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Zipcode.class)
	@JoinTable(name = "GEO_COUNTY_ZIPCODE", joinColumns = { @JoinColumn(name = "ID_GEO_COUNTY") }, inverseJoinColumns = { @JoinColumn(name = "ID_GEO_ZIPCODE") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
	@ForeignKey(name = "FK_GEO_COUNTY_ZIPCODE_1")
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
		final StringBuilder sb = new StringBuilder(getName()).append(" County");
		if (getState() != null) {
			sb.append(", ").append(getState().getAbbreviation());
		}
		return sb.toString(); 
	}
	
}
