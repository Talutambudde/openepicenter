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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Parent;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Authorized regions allow one organization to delegate visibility to another.
 * 
 * @author shade
 * @version $Id: AuthorizedRegion.java 1483 2008-04-08 13:45:34Z steve.kondik $
 */
@Embeddable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthorizedRegion {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5269385407806953352L;

	private AuthorizedRegionType type = AuthorizedRegionType.FULL;

	private Organization grantedBy;

	private Organization grantedTo;

	private Geography geography;

	public AuthorizedRegion() {
		super();
	}

	public AuthorizedRegion(AuthorizedRegionType type, Organization grantedBy, Geography geography) {
		super();
		this.type = type;
		this.grantedBy = grantedBy;
		this.geography = geography;
		validate();
	}

	@PreUpdate
	@PrePersist
	void validate() {

		// Be sure that the granting organization is actually authoritative for
		// this region
		final Geometry geometry;
		if (Zipcode.class.isAssignableFrom(ModelUtils.getRealClass(geography))) {
			geometry = geography.getCentroid();
		} else {
			geometry = geography.getGeometry();
		}
		final Geometry container = grantedBy.getAuthoritativeRegion().getGeometry();
		if (!(container.equals(geometry) || container.contains(geometry) || container.covers(geometry))) {
			throw new IllegalStateException("Granting organization " + grantedBy.toString() + " is not authoritative for " + geography.toString());
		}
	}

	/**
	 * @return the type
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "TYPE", unique = false, nullable = false, insertable = true, updatable = true)
	public AuthorizedRegionType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(AuthorizedRegionType type) {
		this.type = type;
	}

	/**
	 * @return the grantedBy
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GRANTING_ORGANIZATION", unique = false, nullable = false, insertable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@org.hibernate.annotations.ForeignKey(name = "FK_AUTHORIZED_REGION_3")
	public Organization getGrantedBy() {
		return grantedBy;
	}

	/**
	 * @param grantedBy
	 *            the grantedBy to set
	 */
	public void setGrantedBy(Organization grantedBy) {
		this.grantedBy = grantedBy;
	}

	/**
	 * @return the grantedTo
	 */
	@Parent
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Organization getGrantedTo() {
		return grantedTo;
	}

	/**
	 * @param grantedTo
	 *            the grantedTo to set
	 */
	public void setGrantedTo(Organization grantedTo) {
		this.grantedTo = grantedTo;
	}

	/**
	 * @return the geography
	 */
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GEOGRAPHY", unique = false, nullable = false, insertable = true, updatable = true)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@org.hibernate.annotations.ForeignKey(name = "FK_AUTHORIZED_REGION_2")
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof AuthorizedRegion == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final AuthorizedRegion ag = (AuthorizedRegion) o;
			ret = new EqualsBuilder().append(grantedBy, ag.getGrantedBy()).append(grantedTo, ag.getGrantedTo()).append(
					type, ag.getType()).append(geography, ag.getGeography()).isEquals();
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
		return new HashCodeBuilder(651, 7773).append(grantedBy).append(grantedTo).append(type).append(geography)
				.toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("grantedBy", grantedBy).append("grantedTo", grantedTo).append("type",
				type).append("geography", geography).toString();
	}

}
