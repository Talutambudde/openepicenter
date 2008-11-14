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
package com.hmsinc.epicenter.model.surveillance;

import static javax.persistence.GenerationType.AUTO;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.DataConditioning;
import com.hmsinc.epicenter.model.analysis.DataRepresentation;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyType;
import com.hmsinc.epicenter.model.permission.Organization;

/**
 * A SurveillanceTask defines a list of SurveillanceMethods to run for a
 * Geography.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:SurveillanceTask.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "SURVEILLANCE_TASK")
public class SurveillanceTask implements SurveillanceObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5508473744564418055L;

	private Long id;

	private String description;

	private String trigger;

	private Geography geography;

	private GeographyType aggregateType;

	private AnalysisLocation location;

	private Organization organization;

	private int delay = 3600;

	private boolean enabled = true;

	private DataRepresentation representation = DataRepresentation.PERCENTAGE_OF_TOTAL;
	
	private DataConditioning conditioning = DataConditioning.NONE;
	
	private String qualifier;
	
	private Set<SurveillanceSet> sets = new HashSet<SurveillanceSet>(0);
	
	private Set<SurveillanceMethod> methods = new HashSet<SurveillanceMethod>(0);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.surveillance.SurveillanceObject#getId()
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_SURVEILLANCE_TASK") })
	@GeneratedValue(strategy = AUTO, generator = "generator")
	public Long getId() {
		return this.id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.surveillance.SurveillanceObject#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @return the trigger
	 */
	@Column(name = "CRON_TRIGGER", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
	public String getTrigger() {
		return trigger;
	}

	/**
	 * @param trigger
	 *            the trigger to set
	 */
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}

	/**
	 * @return the geography
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GEOGRAPHY", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_SURVEILLANCE_TASK_3")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
	 * @return the aggregateType
	 */
	@Column(name = "AGGREGATE_TYPE", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
	@Enumerated(EnumType.STRING)
	public GeographyType getAggregateType() {
		return aggregateType;
	}

	/**
	 * @param aggregateType
	 *            the aggregateType to set
	 */
	public void setAggregateType(GeographyType aggregateType) {
		this.aggregateType = aggregateType;
	}

	/**
	 * @return the location
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "LOCATION", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
	public AnalysisLocation getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(AnalysisLocation location) {
		this.location = location;
	}

	/**
	 * @return the sets
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = SurveillanceSet.class)
	@JoinTable(name = "SURVEILLANCE_TASK_SET", joinColumns = { @JoinColumn(name = "ID_SURVEILLANCE_TASK") }, inverseJoinColumns = { @JoinColumn(name = "ID_SURVEILLANCE_SET") })
	@ForeignKey(name = "FK_SURVEILLANCE_TASK_SET_1", inverseName = "FK_SURVEILLANCE_TASK_SET_2")
	public Set<SurveillanceSet> getSets() {
		return sets;
	}

	/**
	 * @param sets the sets to set
	 */
	public void setSets(Set<SurveillanceSet> sets) {
		this.sets = sets;
	}

	/**
	 * @return the organization
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGANIZATION", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_SURVEILLANCE_TASK_2")
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
	 * @return the delay
	 */
	@Column(name = "DELAY", unique = false, nullable = false, insertable = true, updatable = true)
	public int getDelay() {
		return delay;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(int delay) {
		this.delay = delay;
	}

	/**
	 * @return the enabled
	 */
	@Column(name = "ENABLED", unique = false, nullable = false, insertable = true, updatable = true)
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the representation
	 */
	@Column(name = "REPRESENTATION", unique = false, nullable = true, insertable = true, updatable = true, length = 40)
	@Enumerated(EnumType.STRING)
	public DataRepresentation getRepresentation() {
		return representation;
	}

	/**
	 * @param representation the representation to set
	 */
	public void setRepresentation(DataRepresentation representation) {
		this.representation = representation;
	}

	/**
	 * @return the conditioning
	 */
	@Column(name = "CONDITIONING", unique = false, nullable = true, insertable = true, updatable = true, length = 40)
	@Enumerated(EnumType.STRING)
	public DataConditioning getConditioning() {
		return conditioning;
	}

	/**
	 * @param conditioning the conditioning to set
	 */
	public void setConditioning(DataConditioning conditioning) {
		this.conditioning = conditioning;
	}

	/**
	 * @return the qualifier
	 */
	@Column(name = "QUALIFIER", unique = false, nullable = true, insertable = true, updatable = true, length = 40)
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * @param qualifier the qualifier to set
	 */
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	
	/**
	 * @return the methods
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = SurveillanceMethod.class)
	@JoinTable(name = "SURVEILLANCE_TASK_METHOD", joinColumns = { @JoinColumn(name = "ID_SURVEILLANCE_TASK") }, inverseJoinColumns = { @JoinColumn(name = "ID_SURVEILLANCE_METHOD") })
	@ForeignKey(name = "FK_SURVEILLANCE_TASK_METHOD_1", inverseName = "FK_SURVEILLANCE_TASK_METHOD_2")
	public Set<SurveillanceMethod> getMethods() {
		return methods;
	}

	/**
	 * @param methods
	 *            the methods to set
	 */
	public void setMethods(Set<SurveillanceMethod> methods) {
		this.methods = methods;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("description", description).append("geography", geography).append("aggregate type",
				aggregateType).append("location", location).append("sets", sets).append("organization",	organization.getName()).toString();
	}

}
