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

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * The state of an event.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WorkflowState.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "WORKFLOW_STATE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@org.hibernate.annotations.Table(appliesTo = "WORKFLOW_STATE", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_WORKFLOW_STATE_1", columnNames = "TYPE") } )
@NamedQueries( { @NamedQuery(name = "getInitialState", query = "from WorkflowState w left join fetch w.transitionsTo tto left join fetch w.transitions tfrom where w.workflow=:workflow and w.stateType=com.hmsinc.epicenter.model.workflow.WorkflowStateType.INITIAL") })
@XmlRootElement(name = "workflow-state", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "WorkflowState", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class WorkflowState implements WorkflowObject, Comparable<WorkflowState> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1305393075916466313L;

	private Long id;

	@XmlID
	@XmlAttribute(required = true, name = "ref")
	protected String xmlID;

	@XmlAttribute(required = true, name = "state")
	private WorkflowStateType stateType = WorkflowStateType.TRANSITIONAL;

	private Workflow workflow;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String name;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String description;

	private SortedSet<WorkflowTransition> transitions = new TreeSet<WorkflowTransition>();

	@XmlElementWrapper(name = "transitions", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "transition", namespace = "http://epicenter.hmsinc.com/model")
	private SortedSet<WorkflowTransition> transitionsTo = new TreeSet<WorkflowTransition>();

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_WORKFLOW_STATE") })
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
		this.xmlID = id.toString();
	}

	/**
	 * @return the workflow
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_WORKFLOW", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_WORKFLOW_STATE_1")
	public Workflow getWorkflow() {
		return workflow;
	}

	/**
	 * @param workflow
	 *            the workflow to set
	 */
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}

	/**
	 * @return the type
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE", unique = false, nullable = false, insertable = true, updatable = true)
	public WorkflowStateType getStateType() {
		return stateType;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setStateType(WorkflowStateType stateType) {
		this.stateType = stateType;
	}

	/**
	 * @return the name
	 */
	@Column(name = "NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
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
	 * @return the transitions
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "fromState")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@Sort(type = SortType.NATURAL)
	public SortedSet<WorkflowTransition> getTransitions() {
		return transitions;
	}

	/**
	 * @param transitions
	 *            the transitions to set
	 */
	public void setTransitions(SortedSet<WorkflowTransition> transitions) {
		this.transitions = transitions;
	}

	/**
	 * @return the transitionsTo
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "toState")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@Sort(type = SortType.NATURAL)
	public SortedSet<WorkflowTransition> getTransitionsTo() {
		return transitionsTo;
	}

	/**
	 * @param transitionsTo
	 *            the transitionsTo to set
	 */
	public void setTransitionsTo(SortedSet<WorkflowTransition> transitionsTo) {
		this.transitionsTo = transitionsTo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(WorkflowState rhs) {
		return new CompareToBuilder().append(name, rhs.getName()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name).append("description", description).append("workflow",
				workflow).toString();
	}

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (parent != null && parent instanceof Workflow) {
			this.workflow = (Workflow) parent;
		}
	}
}
