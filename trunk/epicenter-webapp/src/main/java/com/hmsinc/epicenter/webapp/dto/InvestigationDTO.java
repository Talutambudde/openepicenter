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
package com.hmsinc.epicenter.webapp.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.webapp.util.GeometryUtils;
import com.vividsolutions.jts.geom.Point;

/**
 * Conveys specific parts of an Investigation.
 * 
 * @author shade
 * @version $Id: InvestigationDTO.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
@DataTransferObject
public class InvestigationDTO implements Serializable, Comparable<InvestigationDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5761327276543560723L;

	private final Long id;

	final String description;

	final String organizationName;

	final Point organizationPoint;
	
	final Point localityCentroid;
	
	final DateTime timestamp;

	private final KeyValueDTO createdBy;
	
	private final KeyValueDTO assignedTo;

	public InvestigationDTO(Investigation investigation) {
		
		this.id = investigation.getId();
		this.description = investigation.getDescription();
		this.timestamp = investigation.getTimestamp();
		this.organizationName = investigation.getOrganization().getName();
		this.organizationPoint = investigation.getOrganization().getVisibleRegion().getGeometry().getCentroid();
		this.assignedTo = investigation.getAssignedTo() == null ? null : new KeyValueDTO(investigation.getAssignedTo().getId().toString(), investigation.getAssignedTo().getFirstName() + " " + investigation.getAssignedTo().getLastName());
		this.createdBy = investigation.getCreatedBy() == null ? null : new KeyValueDTO(investigation.getCreatedBy().getId().toString(), investigation.getCreatedBy().getFirstName() + " " + investigation.getCreatedBy().getLastName());
		
		this.localityCentroid = GeometryUtils.getCentroidOfCollection(investigation.getLocalities());
		
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the timestamp
	 */
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the organizationName
	 */
	public String getOrganizationName() {
		return organizationName;
	}

	/**
	 * @return the organizationPoint
	 */
	public Point getOrganizationPoint() {
		return organizationPoint;
	}

	/**
	 * @return the assignedTo
	 */
	public KeyValueDTO getAssignedTo() {
		return assignedTo;
	}

	/**
	 * @return the createdBy
	 */
	public KeyValueDTO getCreatedBy() {
		return createdBy;
	}

	/**
	 * @return the localityCentroid
	 */
	public Point getLocalityCentroid() {
		return localityCentroid;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(InvestigationDTO other) {
		return new CompareToBuilder().append(getId(), other.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(1398655825, 397834477).appendSuper(super.hashCode()).append(this.timestamp).append(
				this.id).append(this.description).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		boolean ret = false;
		if (obj instanceof InvestigationDTO) {
			InvestigationDTO rhs = (InvestigationDTO) obj;
			ret = new EqualsBuilder().appendSuper(super.equals(obj)).append(this.timestamp, rhs.timestamp).append(
					this.id, rhs.id).append(this.description, rhs.description).isEquals();
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
		return new ToStringBuilder(this).append("id", id).append("description", description).append("timestamp",
				timestamp).toString();
	}

}
