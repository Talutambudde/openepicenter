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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author shade
 * @version $Id: EventDisposition.java 1759 2008-06-17 18:12:00Z steve.kondik $
 */
@Entity
@Table(name = "EVENT_DISPOSITION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { 
	@NamedQuery(name = "getInitialDisposition", query = "from EventDisposition d where d.type=com.hmsinc.epicenter.model.workflow.WorkflowStateType.INITIAL") })
@XmlRootElement(name = "event-disposition", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "EventDisposition", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class EventDisposition implements WorkflowObject, Comparable<EventDisposition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5264318778261411214L;

	private Long id;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String name;

	@XmlAttribute(required = true, name = "state")
	private WorkflowStateType type;

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_EVENT_DISPOSITION") })
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
	 * @return the name
	 */
	@Column(name = "NAME", unique = true, nullable = false, insertable = true, updatable = true, length = 200)
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE", unique = false, nullable = false, insertable = true, updatable = true)
	public WorkflowStateType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(WorkflowStateType type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(EventDisposition o) {
		return new CompareToBuilder().append(getType(), o.getType()).append(getName(), o.getName()).append(getId(), o.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(139741, 991).append(getName()).append(getType()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		boolean ret = false;
		if (o instanceof EventDisposition == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final EventDisposition ag = (EventDisposition) o;
			ret = new EqualsBuilder().append(getName(), ag.getName()).append(getType(), ag.getType()).isEquals();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name).append("type", type).toString();
	}
}
