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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Parent;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * Embedded mapping between Facility and Milestone with a timestamp and extra
 * details.
 * 
 * @author shade
 * @version $Id: FacilityMilestone.java 1024 2008-02-20 16:00:57Z steve.kondik $
 */
@Embeddable
public class FacilityMilestone implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8921632452803916170L;

	private Facility facility;

	private Milestone milestone;

	private DateTime timestamp = new DateTime();

	private String details;

	/**
	 * @return the facility
	 */
	@Parent
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
	 * @return the milestone
	 */
	@ManyToOne
	@JoinColumn(name = "ID_MILESTONE", nullable = false, updatable = false)
	@ForeignKey(name = "FK_FACILITY_MILESTONE_2")
	public Milestone getMilestone() {
		return milestone;
	}

	/**
	 * @param milestone
	 *            the milestone to set
	 */
	public void setMilestone(Milestone milestone) {
		this.milestone = milestone;
	}

	/**
	 * @return the timestamp
	 */
	@Type(type = "joda")
	@Column(name = "TIMESTAMP", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(DateTime timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the details
	 */
	@Column(name = "DETAIL", unique = false, nullable = true, insertable = true, updatable = true, length = 4000)
	public String getDetails() {
		return details;
	}

	/**
	 * @param details
	 *            the details to set
	 */
	public void setDetails(String details) {
		this.details = details;
	}

}
