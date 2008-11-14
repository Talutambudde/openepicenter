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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

/**
 * An investigation is the process where anomalies in surveillance and other
 * events are examined.
 *  
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Event.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "INVESTIGATION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.hibernate.annotations.Table(appliesTo = "INVESTIGATION", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_INVESTIGATION_1", columnNames = "TIMESTAMP"),
		@org.hibernate.annotations.Index(name = "IDX_INVESTIGATION_2", columnNames = "ID_WORKFLOW_STATE" ),
		@org.hibernate.annotations.Index(name = "IDX_INVESTIGATION_3", columnNames = "ID_ORGANIZATION" ) } )
public class Investigation implements WorkflowObject, Comparable<Investigation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9214369090950002922L;

	private Long id;

	private DateTime timestamp = new DateTime();

	private DateTime lastUpdated = new DateTime();
	
	private String description;

	private WorkflowState state;

	private Organization organization;

	private EpiCenterUser createdBy;
	
	private EpiCenterUser assignedTo;

	private Set<Geography> localities = new HashSet<Geography>();
	
	private SortedSet<Activity> activities = new TreeSet<Activity>();

	private SortedSet<Attachment> attachments = new TreeSet<Attachment>();

	private SortedSet<Event> events = new TreeSet<Event>();

	private Geometry locality;
	
	/**
	 * 
	 */
	public Investigation() {
		super();
	}

	/**
	 * @param description
	 * @param state
	 * @param organization
	 */
	public Investigation(String description, WorkflowState state, Organization organization, EpiCenterUser createdBy) {
		super();
		this.description = description;
		this.state = state;
		this.organization = organization;
		this.createdBy = createdBy;
		this.assignedTo = createdBy;
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_INVESTIGATION") })
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
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", unique = false, nullable = false, insertable = true, updatable = true, length = 400)
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
	 * @return the state
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_WORKFLOW_STATE", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_INVESTIGATION_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public WorkflowState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(WorkflowState state) {
		this.state = state;
	}

	/**
	 * @return the assignedTo
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APP_USER_ASSIGNED_TO", unique = false, nullable = true, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_INVESTIGATION_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public EpiCenterUser getAssignedTo() {
		return assignedTo;
	}

	/**
	 * @param assignedTo
	 *            the assignedTo to set
	 */
	public void setAssignedTo(EpiCenterUser assignedTo) {
		this.assignedTo = assignedTo;
	}

	/**
	 * @return the lastUpdated
	 */
	@Type(type = "joda")
	@Column(name = "LAST_UPDATED", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(DateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @return the createdBy
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APP_USER_CREATED_BY", unique = false, nullable = true, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_INVESTIGATION_4")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public EpiCenterUser getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(EpiCenterUser createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the organization
	 */
	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGANIZATION", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_INVESTIGATION_3")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * @return the activities
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "investigation")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Activity> getActivities() {
		return activities;
	}

	/**
	 * @param activities
	 *            the activities to set
	 */
	public void setActivities(SortedSet<Activity> activities) {
		this.activities = activities;
	}

	/**
	 * @return the attachments
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "investigation")
	@Sort(type = SortType.NATURAL)
	public SortedSet<Attachment> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments
	 *            the attachments to set
	 */
	public void setAttachments(SortedSet<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * @return the events
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Event.class)
	@JoinTable(name = "INVESTIGATION_EVENT", joinColumns = { @JoinColumn(name = "ID_INVESTIGATION") }, inverseJoinColumns = { @JoinColumn(name = "ID_EVENT") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Event> getEvents() {
		return events;
	}

	/**
	 * @param events
	 *            the events to set
	 */
	public void setEvents(SortedSet<Event> events) {
		this.events = events;
	}

	/**
	 * @return the localities
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Geography.class)
	@JoinTable(name = "INVESTIGATION_GEOGRAPHY", joinColumns = { @JoinColumn(name = "ID_INVESTIGATION") }, inverseJoinColumns = { @JoinColumn(name = "ID_GEOGRAPHY") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@org.hibernate.annotations.ForeignKey(name = "FK_INVESTIGATION_GEOGRAPHY_1", inverseName = "FK_INVESTIGATION_GEOGRAPHY_2")
	public Set<Geography> getLocalities() {
		return localities;
	}

	/**
	 * @param localities the localities to set
	 */
	public void setLocalities(Set<Geography> localities) {
		this.localities = localities;
	}

	/**
	 * @return the locality as a geometry
	 */
	@Transient
	public Geometry getLocality() {
		
		if (locality == null) {
			synchronized(this) {
				final Set<Geometry> geometries = new HashSet<Geometry>();
				for (Geography locality : localities) {
					geometries.add(locality.getGeometry());
				}
				locality = GeometryCombiner.combine(geometries);
			}
		}
		
		return locality;
	}
	
	@PreUpdate
	public void updateTimestamp() {
		this.lastUpdated = new DateTime();
	}
	
	/*
	@PrePersist
	@PreUpdate
	public void validateEventLocality() {
		
		for (Event event : events) {
			if (event.getGeography() != null) {
				final Geometry g = event.getGeography().getGeometry();
				Validate.isTrue((getLocality().equals(g) || getLocality().contains(g)), "Event " + event.getId() + " must be contained within the defined locality.");
			}
		}
	}
	*/
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Investigation rhs) {
		return new CompareToBuilder().append(timestamp, rhs.getTimestamp()).append(id, rhs.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("description", description).append("organization", organization).append("timestamp", timestamp).append("lastUpdated", lastUpdated).toString();
	}
}
