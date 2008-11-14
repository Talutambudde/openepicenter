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

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.classify.Classification;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
@Entity
@Table(name = "ADMIT")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({@NamedQuery(name="findExistingAdmit", query="select messageId from Admit d where d.visitNumber = :visitNumber and d.patient.patientId = :patientId and d.patient.facility = :facility") } )
@org.hibernate.annotations.Table(appliesTo = "ADMIT", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_ADMIT_1", columnNames = "VISIT_NUM") })
public class Admit extends Interaction implements CodedVisit {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6888668456537643974L;

	private String visitNumber;

	private String reason;

	private String icd9;

	/**
	 * 
	 */
	public Admit() {
	}

	/**
	 * @param id
	 */
	public Admit(Long id) {
		super(id);
	}

	/**
	 * @param id
	 * @param patient
	 * @param patientDetail
	 * @param messageId
	 * @param interactionDate
	 * @param classifications
	 */
	public Admit(Long id, Patient patient, PatientDetail patientDetail, Long messageId, DateTime interactionDate,
			Set<Classification> classifications) {
		super(id, patient, patientDetail, messageId, interactionDate, classifications);
	}

	/**
	 * @return the visitNumber
	 */
	@Column(name = "VISIT_NUM", unique = false, nullable = false, insertable = true, updatable = true, length = 40)
	public String getVisitNumber() {
		return visitNumber;
	}

	/**
	 * @param visitNumber
	 *            the visitNumber to set
	 */
	public void setVisitNumber(String visitNumber) {
		this.visitNumber = visitNumber;
	}

	/**
	 * @return the reason
	 */
	@Column(name = "REASON", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason
	 *            the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the icd9
	 */
	@Column(name = "ICD9", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
	public String getIcd9() {
		return icd9;
	}

	/**
	 * @param icd9
	 *            the icd9 to set
	 */
	public void setIcd9(String icd9) {
		this.icd9 = icd9;
	}

}
