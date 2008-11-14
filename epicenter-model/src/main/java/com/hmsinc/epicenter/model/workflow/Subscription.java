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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: Subscription.java 1089 2008-02-23 16:59:07Z steve.kondik $
 */
@Entity
@Table(name = "SUBSCRIPTION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Subscription implements WorkflowObject {
	public static final String EMAIL = "email";
	public static final String SMS = "sms";


	/**
	 * 
	 */
	private static final long serialVersionUID = 4640002259039766496L;
	
	private Long id;

	private String name;
	
	private EpiCenterUser user;

	private String type;

	private String destination;
	
	private Set<Geography> geographies = new HashSet<Geography>();

	private Set<Classification> classifications = new HashSet<Classification>();
	
	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_SUBSCRIPTION") })
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
	 * @return the user
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APP_USER", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_SUBSCRIPTION_1")
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
	@Column(name = "TYPE", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	@Column(name = "NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 400)
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the destination
	 */
	@Column(name = "DESTINATION", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = destination;
	}

	/**
	 * @return the geographies
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Geography.class)
	@JoinTable(name = "SUBSCRIPTION_GEOGRAPHY", joinColumns = { @JoinColumn(name = "ID_SUBSCRIPTION") }, inverseJoinColumns = { @JoinColumn(name = "ID_GEOGRAPHY") })
	@ForeignKey(name = "FK_SUBSCRIPTION_GEOGRAPHY_1", inverseName = "FK_SUBSCRIPTION_GEOGRAPHY_2")
	public Set<Geography> getGeographies() {
		return geographies;
	}

	/**
	 * @param geographies the geographies to set
	 */
	public void setGeographies(Set<Geography> geographies) {
		this.geographies = geographies;
	}

	/**
	 * @return the classifications
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Classification.class)
	@JoinTable(name = "SUBSCRIPTION_CLASSIFICATION", joinColumns = { @JoinColumn(name = "ID_SUBSCRIPTION") }, inverseJoinColumns = { @JoinColumn(name = "ID_CLASSIFICATION") })
	@ForeignKey(name = "FK_SUBSCRIPTION_CLASSI_1", inverseName = "FK_SUBSCRIPTION_CLASSI_2")
	public Set<Classification> getClassifications() {
		return classifications;
	}

	/**
	 * @param classifications the classifications to set
	 */
	public void setClassifications(Set<Classification> classifications) {
		this.classifications = classifications;
	}

}
