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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.geography.GeographicalEntity;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.model.workflow.Workflow;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

/**
 * An Organization is a grouping of users and tasks, a defined workflow, and
 * authorities granted over a spatial region.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Organization.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "ORGANIZATION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { @NamedQuery(name = "getOrganizationByName", query = "from Organization o left join fetch o.users u where o.name=:name"),
	@NamedQuery(name = "getOrganizations", query = "from Organization o where o.enabled = :enabled order by name"),
	@NamedQuery(name = "getOrganizationCount", query = "select count(*) from Organization where name = :name"),
	@NamedQuery(name = "getOrganization", query = "from Organization o left join fetch o.workflow left join fetch o.authoritativeRegion left join o.authorizedRegions where o.id = :organizationId")})
@XmlRootElement(name = "organization", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "Organization", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class Organization implements PermissionObject, GeographicalEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1778880888814619107L;
	
	@XmlAttribute
	private Long id;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String name;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String description;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private Workflow workflow;

	@XmlElement(required = false, namespace = "http://epicenter.hmsinc.com/model")
	private Geography authoritativeRegion;

	private Set<Organization> sponsors = new HashSet<Organization>();
	
	private Set<Organization> sponsoredOrganizations = new HashSet<Organization>();
	
	private Set<AuthorizedRegion> authorizedRegions = new HashSet<AuthorizedRegion>();
	
	private Set<SurveillanceTask> surveillanceTasks = new HashSet<SurveillanceTask>();

	private Set<EpiCenterUser> users = new HashSet<EpiCenterUser>();

	private Set<Investigation> investigations = new HashSet<Investigation>();

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private boolean enabled = true;
	
	private PreparedGeometry visibleRegion;
	
	/**
	 * 
	 */
	public Organization() {
		super();
	}

	/**
	 * @param name
	 * @param description
	 * @param workflow
	 */
	public Organization(String name, String description, Workflow workflow) {
		super();
		this.name = name;
		this.description = description;
		this.workflow = workflow;
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_ORGANIZATION") })
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
	@Column(name = "NAME", unique = true, nullable = false, insertable = true, updatable = true, length = 80)
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
	 * @return the workflow
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_WORKFLOW", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_ORGANIZATION_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
	 * @return the authoritativeRegion
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GEOGRAPHY", unique = false, nullable = true, insertable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ForeignKey(name = "FK_ORGANIZATION_2")
	public Geography getAuthoritativeRegion() {
		return authoritativeRegion;
	}

	/**
	 * @param authoritativeRegion
	 *            the authoritativeRegion to set
	 */
	public void setAuthoritativeRegion(Geography authoritativeRegion) {
		this.authoritativeRegion = authoritativeRegion;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.GeographicalEntity#getGeography()
	 */
	@Transient
	public Geography getGeography() {
		return getAuthoritativeRegion();
	}

	/**
	 * @return the authorizedRegions
	 */
	@CollectionOfElements
	@JoinTable(name = "AUTHORIZED_REGION", joinColumns = { @JoinColumn(name = "ID_ORGANIZATION" )})
	@ForeignKey(name = "FK_AUTHORIZED_REGION_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<AuthorizedRegion> getAuthorizedRegions() {
		return authorizedRegions;
	}

	/**
	 * @param authorizedRegions the authorizedRegions to set
	 */
	public void setAuthorizedRegions(Set<AuthorizedRegion> authorizedRegions) {
		this.authorizedRegions = authorizedRegions;
	}

	/**
	 * @return the users
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = EpiCenterUser.class)
	@JoinTable(name = "ORGANIZATION_USER", joinColumns = { @JoinColumn(name = "ID_ORGANIZATION") }, inverseJoinColumns = { @JoinColumn(name = "ID_APP_USER") })
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

	/**
	 * @return the sponsoredOrganizations
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "sponsors", targetEntity = Organization.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Organization> getSponsoredOrganizations() {
		return sponsoredOrganizations;
	}

	/**
	 * @return the sponsors
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Organization.class)
	@JoinTable(name = "ORGANIZATION_SPONSOR", joinColumns = { @JoinColumn(name = "ID_ORGANIZATION") }, inverseJoinColumns = { @JoinColumn(name = "ID_SPONSOR_ORG") })
	@ForeignKey(name = "FK_ORGANIZATION_SPONSOR_1", inverseName = "FK_ORGANIZATION_SPONSOR_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Organization> getSponsors() {
		return sponsors;
	}

	/**
	 * @param sponsors the sponsors to set
	 */
	public void setSponsors(Set<Organization> sponsors) {
		this.sponsors = sponsors;
	}

	/**
	 * @param sponsoredOrganizations the sponsoredOrganizations to set
	 */
	public void setSponsoredOrganizations(Set<Organization> sponsoredOrganizations) {
		this.sponsoredOrganizations = sponsoredOrganizations;
	}

	/**
	 * @return the surveillanceTasks
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "organization")
	public Set<SurveillanceTask> getSurveillanceTasks() {
		return surveillanceTasks;
	}

	/**
	 * @param surveillanceTasks
	 *            the surveillanceTasks to set
	 */
	public void setSurveillanceTasks(Set<SurveillanceTask> surveillanceTasks) {
		this.surveillanceTasks = surveillanceTasks;
	}

	/**
	 * @return the investigations
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "organization")
	public Set<Investigation> getInvestigations() {
		return investigations;
	}

	/**
	 * @param investigations the investigations to set
	 */
	public void setInvestigations(Set<Investigation> investigations) {
		this.investigations = investigations;
	}

	/**
	 * @return the enabled
	 */
	@Column(name = "ENABLED", unique = false, nullable = false, insertable = true, updatable = true, length = 1)
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
	 * Computes the organization's visible region.
	 * 
	 * @return
	 */
	@Transient
	public PreparedGeometry getVisibleRegion() {

		if (this.visibleRegion == null) {

			synchronized (this) {
				final Set<Geometry> geometries = new HashSet<Geometry>();

				if (getAuthoritativeRegion() != null) {
							
					Validate.notNull(getAuthoritativeRegion().getGeometry(), "Authoritative region has a null geometry");
					geometries.add(getAuthoritativeRegion().getGeometry());
				}
				for (AuthorizedRegion authorized : getAuthorizedRegions()) {
						
					Validate.notNull(authorized.getGeography().getGeometry(), "Authorized region has a null geometry");
					geometries.add(authorized.getGeography().getGeometry());
				}
			

				if (geometries.size() == 1) {
					this.visibleRegion = PreparedGeometryFactory.prepare(geometries.iterator().next());
				} else if (geometries.size() > 1) {
					final Geometry combined = GeometryCombiner.combine(geometries);
					if (combined != null) {
						this.visibleRegion = PreparedGeometryFactory.prepare(combined.union());
					}
				}
			}
		}
		return this.visibleRegion;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Organization == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final Organization ag = (Organization) o;
			ret = new EqualsBuilder().append(getId(), ag.getId()).append(getName(), ag.getName()).isEquals();
		}
		return ret;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(27, 3).append(id).append(name).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("name", name).append("description", description)
			.append("enabled", enabled).toString();
	}

}
