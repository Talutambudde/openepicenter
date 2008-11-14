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
package com.hmsinc.epicenter.model.workflow;

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

import org.apache.commons.lang.builder.CompareToBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;

/**
 * Activity on an event.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EventActivity.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "ACTIVITY")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Activity implements WorkflowObject, Comparable<Activity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3538609293777899715L;

	private Long id;

	private DateTime timestamp = new DateTime();

	private Investigation investigation;

	private EpiCenterUser user;

	private String log;

	/**
	 * 
	 */
	public Activity() {
		super();
	}

	/**
	 * @param event
	 * @param user
	 * @param log
	 */
	public Activity(Investigation investigation, EpiCenterUser user, String log) {
		super();
		this.investigation = investigation;
		this.user = user;
		this.log = log;
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_ACTIVITY") })
	@GeneratedValue(strategy = AUTO, generator = "generator")
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
	 * @return the user
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APP_USER", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ACTIVITY_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public EpiCenterUser getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(EpiCenterUser user) {
		this.user = user;
	}

	/**
	 * @return the investigation
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_INVESTIGATION", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ACTIVITY_2")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Investigation getInvestigation() {
		return investigation;
	}

	/**
	 * @param investigation
	 *            the investigation to set
	 */
	public void setInvestigation(Investigation investigation) {
		this.investigation = investigation;
	}

	/**
	 * @return the comment
	 */
	@Column(name = "LOG", unique = false, nullable = true, insertable = true, updatable = true, length = 4000)
	public String getLog() {
		return log;
	}

	/**
	 * @param log
	 *            the log to set
	 */
	public void setLog(String log) {
		this.log = log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Activity rhs) {
		return new CompareToBuilder().append(timestamp, rhs.getTimestamp()).append(id, rhs.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return log;
	}

}
