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
package com.hmsinc.epicenter.model.attribute;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.hmsinc.epicenter.model.health.Interaction;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
@Entity
@Table(name = "PATIENT_CLASS")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( {
		@NamedQuery(name = "getPatientClassByAbbreviation", query = "from PatientClass pc where pc.abbreviation = :abbreviation"),
		@NamedQuery(name = "getPatientClassByName", query = "from PatientClass pc where pc.name = :name"),
		@NamedQuery(name = "getUnknownPatientClass", query = "from PatientClass pc where pc.abbreviation is null") })
@XmlType(name = "PatientClass", namespace = "http://epicenter.hmsinc.com/model")
@XmlRootElement(name = "patient-class", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class PatientClass extends Attribute implements AttributeObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7943490266708275045L;

	@XmlAttribute
	private String abbreviation;

	@XmlAttribute
	private String description;

	private Set<Interaction> interactions = new HashSet<Interaction>();

	/**
	 * @return the abbreviation
	 */
	@Column(name = "ABBREVIATION", unique = true, nullable = false, insertable = true, updatable = true, length = 3)
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
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the interactions
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "patientClass")
	public Set<Interaction> getInteractions() {
		return interactions;
	}

	/**
	 * @param interactions
	 *            the interactions to set
	 */
	public void setInteractions(Set<Interaction> interactions) {
		this.interactions = interactions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(147, 79).append(abbreviation).append(this.getName()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof PatientClass == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final PatientClass ag = (PatientClass) o;
			ret = new EqualsBuilder().append(getAbbreviation(), ag.getAbbreviation()).append(getName(), ag.getName())
					.isEquals();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("abbreviation", abbreviation).toString();
	}
}
