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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.health.CodedVisit;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.Registration;

/**
 * Conveys a line-listing of cases.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:CasesDTO.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@DataTransferObject
public class CasesDTO implements Serializable, Comparable<CasesDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5867140824946979329L;

	private final DateTime interactionDate;

	private final Integer age;

	private final String gender;

	private final String zipcode;

	private Long facilityId;

	private final String patientId;

	private String facilityName;

	private final String visitNumber;

	private final String reason;

	private final String classification;
	
	/**
	 * @param interaction
	 * @param classifications
	 */
	public CasesDTO(final Interaction interaction) {

		super();
		this.interactionDate = interaction.getInteractionDate();
		this.age = interaction.getAgeAtInteraction();
		this.gender = interaction.getPatientDetail().getGender().getAbbreviation();
		this.zipcode = interaction.getPatientDetail().getZipcode();
		this.facilityName = interaction.getPatient().getFacility().getName();
		this.facilityId = interaction.getPatient().getFacility().getId();
		this.patientId = interaction.getPatient().getPatientId();
		this.visitNumber = (interaction instanceof CodedVisit ? ((CodedVisit) interaction).getVisitNumber() : null);
		this.reason = (interaction instanceof Registration ? ((Registration) interaction).getReason() : null);
		
		final List<String> classifications = new ArrayList<String>();
		for (Classification c : interaction.getClassifications()) {
			classifications.add(c.getCategory());
		}
		this.classification = StringUtils.join(classifications, ",");
	}
	
	/**
	 * @param interaction
	 * @param classifications
	 */
	public CasesDTO(final Interaction interaction, final Collection<String> classifications) {
		super();
		this.interactionDate = interaction.getInteractionDate();
		this.age = interaction.getAgeAtInteraction();
		this.gender = interaction.getPatientDetail().getGender().getAbbreviation();
		this.zipcode = interaction.getPatientDetail().getZipcode();
		this.facilityName = interaction.getPatient().getFacility().getName();
		this.facilityId = interaction.getPatient().getFacility().getId();
		this.patientId = interaction.getPatient().getPatientId();
		this.visitNumber = (interaction instanceof CodedVisit ? ((CodedVisit) interaction).getVisitNumber() : null);
		this.reason = (interaction instanceof Registration ? ((Registration) interaction).getReason() : null);
		this.classification = StringUtils.join(classifications, ",");
	}

	/**
	 * @return the interactionDate
	 */
	public DateTime getInteractionDate() {
		return interactionDate;
	}

	/**
	 * @return the age
	 */
	public Integer getAge() {
		return age;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @return the zipcode
	 */
	public String getZipcode() {
		return zipcode;
	}

	/**
	 * @return the facilityName
	 */
	public String getFacilityName() {
		return facilityName;
	}

	/**
	 * @return the visitNumber
	 */
	public String getVisitNumber() {
		return visitNumber;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @return the classification
	 */
	public String getClassification() {
		return classification;
	}

	/**
	 * @return the facilityId
	 */
	public Long getFacilityId() {
		return facilityId;
	}

	/**
	 * @return the patientId
	 */
	public String getPatientId() {
		return patientId;
	}

	/**
	 * @param facilityId the facilityId to set
	 */
	public void setFacilityId(Long facilityId) {
		this.facilityId = facilityId;
	}

	/**
	 * @param facilityName the facilityName to set
	 */
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final CasesDTO g) {
		return new CompareToBuilder().append(interactionDate, g.getInteractionDate()).append(facilityName,
				g.getFacilityName()).toComparison();
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
