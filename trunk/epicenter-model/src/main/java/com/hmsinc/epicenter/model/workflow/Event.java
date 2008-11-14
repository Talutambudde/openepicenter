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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.geography.GeographicalEntity;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.Organization;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
@Entity
@Table(name = "EVENT")
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.Table(appliesTo = "EVENT", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_EVENT_1", columnNames = "ID_EVENT_DISPOSITION"),
		@org.hibernate.annotations.Index(name = "IDX_EVENT_2", columnNames = "ID_GEOGRAPHY") })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { @NamedQuery(name = "getVisibleEvents", query = "from Event e where e.geography.class = :geoclass and filter(e.geography.geometry, :geometry) = 'TRUE'") } )
public class Event implements WorkflowObject, GeographicalEntity, Comparable<Event> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -696136799726238587L;

	private Long id;

	private DateTime timestamp = new DateTime();

	private String description;

	private Geography geography;
	
	private Organization organization;
	
	private EventDisposition disposition;
	
	private Set<Investigation> investigations = new HashSet<Investigation>();

	/**
	 * 
	 */
	public Event() {
		super();
	}

	/**
	 * @param description
	 * @param geography
	 * @param organization
	 */
	public Event(String description, Geography geography, EventDisposition disposition, Organization organization) {
		super();
		this.description = description;
		this.geography = geography;
		this.disposition = disposition;
		this.organization = organization;
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_EVENT") })
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
	 * @return the organization
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGANIZATION", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_EVENT_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}

	/**
	 * @return the geography
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GEOGRAPHY", unique = false, nullable = false, insertable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ForeignKey(name = "FK_EVENT_2")
	public Geography getGeography() {
		return geography;
	}

	/**
	 * @param geography
	 *            the geography to set
	 */
	public void setGeography(Geography geography) {
		this.geography = geography;
	}
	
	/**
	 * @return the disposition
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_EVENT_DISPOSITION", unique = false, nullable = false, insertable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ForeignKey(name = "FK_EVENT_3")
	public EventDisposition getDisposition() {
		return disposition;
	}

	/**
	 * @param disposition the disposition to set
	 */
	public void setDisposition(EventDisposition disposition) {
		this.disposition = disposition;
	}

	/**
	 * @return the investigations
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "events", targetEntity = Investigation.class)
	@ForeignKey(name = "FK_INVESTIGATION_EVENT_1", inverseName = "FK_INVESTIGATION_EVENT_2")
	// for some strange reason caching here is causing bug TRX-609
	//@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Investigation> getInvestigations() {
		return investigations;
	}

	/**
	 * @param investigations
	 *            the investigations to set
	 */
	public void setInvestigations(Set<Investigation> investigations) {
		this.investigations = investigations;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Event rhs) {
		return new CompareToBuilder().append(timestamp, rhs.getTimestamp()).append(id, rhs.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("description", description).append("geography", geography).append("disposition", disposition).append("timestamp", timestamp).toString();
	}
}
