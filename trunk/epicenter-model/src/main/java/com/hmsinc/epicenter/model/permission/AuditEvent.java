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
package com.hmsinc.epicenter.model.permission;

import static javax.persistence.GenerationType.AUTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * Represents login, logout, and other auditing related events.
 * 
 * @author shade
 * @version $Id: AuditEvent.java 1583 2008-04-25 15:32:01Z steve.kondik $
 */
@Entity
@Table(name = "AUDIT_EVENT")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuditEvent implements PermissionObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private DateTime timestamp = new DateTime();

	private EpiCenterUser user;

	private AuditEventType type;

	private String details;

	AuditEvent() {
		
	}
	
	/**
	 * @param user
	 * @param type
	 */
	public AuditEvent(EpiCenterUser user, AuditEventType type) {
		super();
		this.user = user;
		this.type = type;
	}

	/**
	 * @param user
	 * @param type
	 */
	public AuditEvent(EpiCenterUser user, AuditEventType type, String details) {
		super();
		this.user = user;
		this.type = type;
		this.details = details;
	}
	
	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_AUDIT_EVENT") })
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
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APP_USER", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_AUDIT_EVENT_1")
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
	 * @return the type
	 */
	@Column(name = "TYPE", unique = false, nullable = false, insertable = true, updatable = true, length = 40)
	@Enumerated(EnumType.STRING)
	public AuditEventType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(AuditEventType type) {
		this.type = type;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("timestamp", timestamp).append("type", type).append(
				"user", user.getUsername()).append("details", details).toString();
	}

}
