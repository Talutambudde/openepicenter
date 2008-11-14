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
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.attribute.Attribute;
import com.hmsinc.epicenter.model.permission.Organization;

/**
 * @author shade
 * @version $Id: SurveillanceSet.java 1471 2008-04-07 17:14:11Z steve.kondik $
 */
@Entity
@Table(name = "SURVEILLANCE_SET")
public class SurveillanceSet implements SurveillanceObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3602030583944451948L;

	private Long id;

	private String description;

	private Organization organization;

	private DataType datatype;

	private Set<Classification> classifications = new HashSet<Classification>(0);

	private Set<Attribute> attributes = new HashSet<Attribute>(0);

	private Set<SurveillanceTask> tasks = new HashSet<SurveillanceTask>(0);

	public SurveillanceSet() {
		super();
	}
	
	public SurveillanceSet(String description, DataType datatype) {
		super();
		this.description = description;
		this.datatype = datatype;
	}


	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_SURVEILLANCE_SET") })
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
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_ORGANIZATION", unique = false, nullable = true, insertable = true, updatable = true)
	@ForeignKey(name = "FK_SURVEILLANCE_SET_1")
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
	 * @return the datatype
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_DATA_TYPE", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_SURVEILLANCE_SET_2")
	public DataType getDatatype() {
		return datatype;
	}

	/**
	 * @param datatype
	 *            the datatype to set
	 */
	public void setDatatype(DataType datatype) {
		this.datatype = datatype;
	}

	/**
	 * @return the classifications
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Classification.class)
	@JoinTable(name = "SURVEILLANCE_SET_CLASSI", joinColumns = { @JoinColumn(name = "ID_SURVEILLANCE_SET") }, inverseJoinColumns = { @JoinColumn(name = "ID_CLASSIFICATION") })
	@ForeignKey(name = "FK_SURVEILLANCE_SET_CLASSI_1", inverseName = "FK_SURVEILLANCE_SET_CLASSI_2")
	public Set<Classification> getClassifications() {
		return classifications;
	}

	/**
	 * @param classifications
	 *            the classifications to set
	 */
	public void setClassifications(Set<Classification> classifications) {
		this.classifications = classifications;
	}

	/**
	 * @return the attributes
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Attribute.class)
	@JoinTable(name = "SURVEILLANCE_SET_ATTRIB", joinColumns = { @JoinColumn(name = "ID_SURVEILLANCE_SET") }, inverseJoinColumns = { @JoinColumn(name = "ID_ATTRIBUTE") })
	@ForeignKey(name = "FK_SURVEILLANCE_SET_ATTRIB_1", inverseName = "FK_SURVEILLANCE_SET_ATTRIB_2")
	public Set<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @param <T>
	 * @param attributeType
	 * @return
	 */
	@Transient
	public <T extends Attribute> Set<T> getAttribute(final Class<T> attributeType) {
		final Set<T> attrs = new HashSet<T>();
		for (Attribute attribute : getAttributes()) {
			if (attributeType.isInstance(attribute)) {
				attrs.add(attributeType.cast(attribute));
			}
		}
		return attrs;
	}
	
	/**
	 * @return the tasks
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, mappedBy = "sets", targetEntity = SurveillanceTask.class)
	public Set<SurveillanceTask> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void setTasks(Set<SurveillanceTask> tasks) {
		this.tasks = tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("description", description).append("datatype",
				datatype).append("classifications", classifications).append("attributes", attributes).toString();
	}

}
