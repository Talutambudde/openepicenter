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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
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
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.security.GrantedAuthority;

/**
 * A role represents a permission granted to a user, which can optionally be
 * associated with multiple spatial entities.
 *  
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EpiCenterRole.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
@Entity
@Table(name = "APP_ROLE")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { @NamedQuery(name = "getRole", query = "from EpiCenterRole r where r.permission = :roleName")})
@XmlRootElement(name = "role", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "EpiCenterRole", propOrder = { "id", "permission", "description" }, namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class EpiCenterRole implements PermissionObject, GrantedAuthority {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3980563802139364093L;

	@XmlAttribute
	private Long id;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String permission;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String description;

	private Set<EpiCenterUser> users = new HashSet<EpiCenterUser>();

	/**
	 * 
	 */
	public EpiCenterRole() {
		super();
	}

	/**
	 * @param permission
	 * @param description
	 */
	public EpiCenterRole(String permission, String description) {
		super();
		this.permission = permission;
		this.description = description;
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_APP_ROLE") })
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
	 * @return the permission
	 */
	@Column(name = "PERMISSION", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
	public String getPermission() {
		return permission;
	}

	/**
	 * @param permission
	 *            the permission to set
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
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
	 * @return the users
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, mappedBy = "roles", targetEntity = EpiCenterUser.class)
	@ForeignKey(name = "FK_APP_USER_ROLE_1", inverseName = "FK_APP_USER_ROLE_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<EpiCenterUser> getUsers() {
		return users;
	}

	/**
	 * @param users
	 *            the users to set
	 */
	public void setUsers(Set<EpiCenterUser> users) {
		this.users = users;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		final EpiCenterRole other = (EpiCenterRole)o;
		return new CompareToBuilder().append(permission, other.getPermission()).append(id, other.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 97).append(permission).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		boolean ret = false;
		if (o instanceof EpiCenterRole == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final EpiCenterRole ag = (EpiCenterRole) o;
			ret = new EqualsBuilder().append(permission, ag.getPermission()).isEquals();
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
		return new ToStringBuilder(this).append(id).append(permission).append(description).toString();
	}

	/* (non-Javadoc)
	 * @see org.acegisecurity.GrantedAuthority#getAuthority()
	 */
	@Transient
	public String getAuthority() {
		final StringBuilder authority = new StringBuilder();
		if (permission != null) {
			authority.append(permission.toUpperCase());
			
			// TODO: Append Geographies to the role.
		}
		return authority.toString();
	}
}
