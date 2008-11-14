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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * A WorkflowTransition describes a transition between WorkflowStates.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WorkflowTransition.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "WORKFLOW_TRANSITION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@XmlRootElement(name = "transition", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "WorkflowTransition", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowTransition implements WorkflowObject, Comparable<WorkflowTransition> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8169033110096566385L;

	@XmlAttribute
	private Long id;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String action;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String description;

	private WorkflowState fromState;

	@XmlIDREF
	@XmlAttribute(name = "to")
	private WorkflowState toState;

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_WORKFLOW_TRANSITION") })
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
	 * @return the action
	 */
	@Column(name = "ACTION", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
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
	 * @return the fromState
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_STATE_FROM", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_WORKFLOW_TRANSITION_1")
	public WorkflowState getFromState() {
		return fromState;
	}

	/**
	 * @param fromState
	 *            the fromState to set
	 */
	public void setFromState(WorkflowState fromState) {
		this.fromState = fromState;
	}

	/**
	 * @return the toState
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_STATE_TO", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_WORKFLOW_TRANSITION_2")
	public WorkflowState getToState() {
		return toState;
	}

	/**
	 * @param toState
	 *            the toState to set
	 */
	public void setToState(WorkflowState toState) {
		this.toState = toState;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(WorkflowTransition o) {
		return new CompareToBuilder().append(action, o.getAction()).toComparison();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("action", action).append("description", description).append("from", fromState).append("to", toState).toString();
	}
	
	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (parent != null && parent instanceof WorkflowState) {
			this.fromState = (WorkflowState)parent;
		}
	}
}
