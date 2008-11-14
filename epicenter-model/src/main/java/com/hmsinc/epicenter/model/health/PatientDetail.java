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
package com.hmsinc.epicenter.model.health;

import static com.hmsinc.epicenter.util.FormatUtils.formatDateTime;
import static javax.persistence.GenerationType.AUTO;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.util.InvalidZipcodeException;
import com.hmsinc.epicenter.model.util.ModelUtils;

/**
 * A patient detail record holds various personal information about a patient.
 * Since this data can possibly change, we timestamp it.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:PatientDetail.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
@Entity
@Table(name = "PATIENT_DETAIL")
@org.hibernate.annotations.Table(appliesTo = "PATIENT_DETAIL", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_PATIENT_DETAIL_1", columnNames = "ID_PATIENT"),
		@org.hibernate.annotations.Index(name = "IDX_PATIENT_DETAIL_2", columnNames = "ID_GENDER"),
		@org.hibernate.annotations.Index(name = "IDX_PATIENT_DETAIL_3", columnNames = "ZIPCODE") })
public class PatientDetail implements HealthObject, Comparable<PatientDetail> {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = 5657630126675218004L;

	private Long id;

	private DateTime timestamp = new DateTime();

	private Patient patient;

	private Gender gender;

	private DateTime dateOfBirth;

	private String zipcode;

	private String employerZipcode;
	
	private SortedSet<Interaction> interactions = new TreeSet<Interaction>();

	// Constructors

	/** default constructor */
	public PatientDetail() {
	}

	/**
	 * @param patient
	 */
	public PatientDetail(Patient patient) {
		super();
		this.patient = patient;
	}


	/** full constructor */
	public PatientDetail(Patient patient, Gender gender, String employerZipcode, DateTime dateOfBirth, String zipcode,
			SortedSet<Interaction> interactions) throws InvalidZipcodeException {
		this.patient = patient;
		this.gender = gender;
		this.employerZipcode = ModelUtils.validateZipcode(employerZipcode);
		this.dateOfBirth = dateOfBirth;
		this.zipcode = ModelUtils.validateZipcode(zipcode);
		this.interactions = interactions;
	}

	// Property accessors
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_PATIENT_DETAIL") })
	@Id
	@GeneratedValue(strategy = AUTO, generator = "generator")
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	@Type(type = "joda")
	@Column(name = "TIMESTAMP", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PATIENT", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_PATIENT_DETAIL_1")
	public Patient getPatient() {
		return this.patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GENDER", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_PATIENT_DETAIL_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Gender getGender() {
		return this.gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Type(type = "joda")
	@Column(name = "DOB", unique = false, nullable = true, insertable = true, updatable = true, length = 7)
	public DateTime getDateOfBirth() {
		return this.dateOfBirth;
	}

	public void setDateOfBirth(DateTime dob) {
		this.dateOfBirth = dob;
	}

	/**
	 * @return the employerZipcode
	 */
	@Column(name = "EMPLOYER_ZIPCODE", unique = false, nullable = true, insertable = true, updatable = true, length = 5)
	public String getEmployerZipcode() {
		return employerZipcode;
	}

	/**
	 * @param employerZipcode
	 *            the employerZipcode to set
	 */
	public void setEmployerZipcode(String employerZipcode) throws InvalidZipcodeException {
		this.employerZipcode = ModelUtils.validateZipcode(employerZipcode);
	}

	/**
	 * @return the zipcode
	 */
	@Column(name = "ZIPCODE", unique = false, nullable = true, insertable = true, updatable = true, length = 5)
	public String getZipcode() {
		return zipcode;
	}

	/**
	 * @param zipcode
	 *            the zipcode to set
	 */
	public void setZipcode(String zipcode) throws InvalidZipcodeException {
		this.zipcode = ModelUtils.validateZipcode(zipcode);
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "patientDetail")
	@Sort(type = SortType.NATURAL)
	public SortedSet<Interaction> getInteractions() {
		return this.interactions;
	}

	public void setInteractions(SortedSet<Interaction> interactions) {
		this.interactions = interactions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(PatientDetail p) {
		return new CompareToBuilder().append(getTimestamp(), p.getTimestamp()).append(getId(), p.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(23, 17).append(getPatient().getId()).append(getGender()).append(getZipcode()).append(
				getEmployerZipcode()).append(getDateOfBirth()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		boolean ret = false;
		if (o instanceof PatientDetail == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final PatientDetail ag = (PatientDetail) o;

			ret = new EqualsBuilder().append(getPatient().getId(), ag.getPatient().getId()).append(getGender(), ag.getGender()).append(
					getZipcode(), ag.getZipcode()).append(ag.getEmployerZipcode(), ag.getEmployerZipcode()).isEquals();

			// need to handle Date separate because of nulls and SQL dates
			if (ret == true) {
				if (getDateOfBirth() != null && ag.getDateOfBirth() != null && !getDateOfBirth().toLocalDate().equals(ag.getDateOfBirth().toLocalDate())) {
					ret = false;
				} else if (getDateOfBirth() == null && ag.getDateOfBirth() != null) {
					ret = false;
				} else if (getDateOfBirth() != null && ag.getDateOfBirth() == null) {
					ret = false;
				}

			}
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
		return new ToStringBuilder(this).append("id", id).append("timestamp", formatDateTime(timestamp)).append("patient",
				patient.getId()).append("gender", gender).append("zipcode", zipcode).append("employerZipcode", employerZipcode).append(
				"dateOfBirth", dateOfBirth).toString();
	}
}
