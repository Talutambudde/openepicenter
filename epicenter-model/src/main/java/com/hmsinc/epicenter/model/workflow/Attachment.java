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

import java.sql.Blob;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
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
 * A file attached to an event.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EventAttachment.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "ATTACHMENT")
public class Attachment implements WorkflowObject, Comparable<Attachment> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7071724305279750610L;

	private Long id;

	private DateTime timestamp = new DateTime();

	private String filename;

	private String description;

	private Blob data;

	private EpiCenterUser owner;

	private Investigation investigation;

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_ATTACHMENT") })
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
	 * @return the filename
	 */
	@Column(name = "NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
	public String getFilename() {
		return filename;
	}

	/**
	 * @param filename
	 *            the filename to set
	 */
	public void setFilename(String filename) {
		this.filename = filename;
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
	 * @return the data
	 */
	@Lob
	@Column(name = "DATA", unique = false, nullable = false, insertable = true, updatable = true)
	public Blob getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(Blob data) {
		this.data = data;
	}

	/**
	 * @return the owner
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_USER", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ATTACHMENT_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public EpiCenterUser getOwner() {
		return owner;
	}

	/**
	 * @param owner
	 *            the owner to set
	 */
	public void setOwner(EpiCenterUser owner) {
		this.owner = owner;
	}

	/**
	 * @return the investigation
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_INVESTIGATION", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ATTACHMENT_2")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Attachment rhs) {
		return new CompareToBuilder().append(timestamp, rhs.getTimestamp()).append(id, rhs.getId()).toComparison();
	}
}
