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
package com.hmsinc.epicenter.model.provider;

import static javax.persistence.GenerationType.AUTO;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * @author shade
 * @version $Id: FacilityDUA.java 1238 2008-03-06 15:25:27Z steve.kondik $
 */
@Entity
@Table(name = "FACILITY_DUA")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FacilityDUA implements ProviderObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5511568288638170032L;

	private Long id;

	private String type;

	private DateTime signDate;

	private String signatory;

	private String position;

	private Facility facility;
	
	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_FACILITY_DUA") })
	@GeneratedValue(strategy = AUTO, generator = "generator")
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	@Column(name = "TYPE", nullable = false, insertable = true, updatable = true, length = 10)
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the signDate
	 */
	@Type(type = "joda")
	@Column(name = "SIGN_DATE", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getSignDate() {
		return signDate;
	}

	/**
	 * @param signDate
	 *            the signDate to set
	 */
	public void setSignDate(DateTime signDate) {
		this.signDate = signDate;
	}

	/**
	 * @return the signatory
	 */
	@Column(name = "SIGNATORY", nullable = false, insertable = true, updatable = true, length = 40)
	public String getSignatory() {
		return signatory;
	}

	/**
	 * @param signatory
	 *            the signatory to set
	 */
	public void setSignatory(String signatory) {
		this.signatory = signatory;
	}

	/**
	 * @return the position
	 */
	@Column(name = "POSITION", nullable = false, insertable = true, updatable = true, length = 10)
	public String getPosition() {
		return position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(String position) {
		this.position = position;
	}

	/**
	 * @return the facility
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_FACILITY", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_FACILITY_DUA_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Facility getFacility() {
		return facility;
	}

	/**
	 * @param facility the facility to set
	 */
	public void setFacility(Facility facility) {
		this.facility = facility;
	}

}
