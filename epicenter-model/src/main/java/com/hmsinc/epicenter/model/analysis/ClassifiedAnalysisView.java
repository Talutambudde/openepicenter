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
package com.hmsinc.epicenter.model.analysis;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.attribute.PatientClass;
import com.hmsinc.epicenter.model.provider.Facility;

/**
 * The "analysis" table, used for counting classifications with aggregation.
 * This requires a materialized view or a trigger.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AnalysisView.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "MV_CLASSIFICATION")
public class ClassifiedAnalysisView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5939666246618138938L;
	
	private Long id;

	private DateTime interactionDate;

	private String patientZipcode;

	private Facility facility;

	private AgeGroup ageGroup;

	private Gender gender;

	private PatientClass patientClass;
	
	private Classification classification;

	private InteractionType interactionType;
	
	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID_INTERACTION", nullable = false, insertable = false, updatable = false)
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the interactionDate
	 */
	@Type(type = "joda")
	@Column(name = "INTERACTION_DATE", unique = false, nullable = false, insertable = false, updatable = false)
	public DateTime getInteractionDate() {
		return interactionDate;
	}

	/**
	 * @param interactionDate
	 *            the interactionDate to set
	 */
	public void setInteractionDate(DateTime interactionDate) {
		this.interactionDate = interactionDate;
	}

	/**
	 * @return the patientZipcode
	 */
	@Column(name = "PAT_ZIPCODE", nullable = true, unique = false,insertable = false, updatable = false, length = 5)
	public String getPatientZipcode() {
		return patientZipcode;
	}

	/**
	 * @param patientZipcode
	 *            the patientZipcode to set
	 */
	public void setPatientZipcode(String patientZipcode) {
		this.patientZipcode = patientZipcode;
	}

	/**
	 * @return the ageGroup
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_AGE_GROUP", unique = false, nullable = true, insertable = false, updatable = false)
	@ForeignKey(name = "none")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public AgeGroup getAgeGroup() {
		return ageGroup;
	}

	/**
	 * @param ageGroup
	 *            the ageGroup to set
	 */
	public void setAgeGroup(AgeGroup ageGroup) {
		this.ageGroup = ageGroup;
	}

	/**
	 * @return the gender
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GENDER", unique = false, nullable = true, insertable = false, updatable = false)
	@ForeignKey(name = "none")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Gender getGender() {
		return gender;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	/**
	 * @return the facility
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_FACILITY", unique = false, nullable = false, insertable = false, updatable = false)
	@ForeignKey(name = "none")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Facility getFacility() {
		return facility;
	}

	/**
	 * @param facility
	 *            the facility to set
	 */
	public void setFacility(Facility facility) {
		this.facility = facility;
	}

	/**
	 * @return the patientClass
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PATIENT_CLASS", unique = false, nullable = true, insertable = false, updatable = false)
	@ForeignKey(name = "none")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public PatientClass getPatientClass() {
		return patientClass;
	}

	/**
	 * @param patientClass the patientClass to set
	 */
	public void setPatientClass(PatientClass patientClass) {
		this.patientClass = patientClass;
	}
	
	/**
	 * @return the classification
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CLASSIFICATION", unique = false, nullable = true, insertable = false, updatable = false)
	@ForeignKey(name = "none")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Classification getClassification() {
		return classification;
	}

	/**
	 * @param classification the classification to set
	 */
	public void setClassification(Classification classification) {
		this.classification = classification;
	}

	/**
	 * @return the interactionType
	 */
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "INTERACTION_TYPE", unique = false, nullable = false, insertable = false, updatable = false)
	public InteractionType getInteractionType() {
		return interactionType;
	}

	/**
	 * @param interactionType the interactionType to set
	 */
	public void setInteractionType(InteractionType interactionType) {
		this.interactionType = interactionType;
	}
	
}
