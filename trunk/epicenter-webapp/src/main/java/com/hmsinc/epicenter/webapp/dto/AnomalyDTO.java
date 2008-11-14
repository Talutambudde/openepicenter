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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.epicenter.util.FormatUtils;
import com.vividsolutions.jts.geom.Point;

/**
 * Conveys basic Event information.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EventDTO.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@DataTransferObject
public class AnomalyDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1276353450538757062L;

	private final Long id;

	private final String description;

	private final GeographyDTO geography;

	private final Point locationPoint;

	private final Long classifierId;
	
	private final String classifierName;

	private final Long categoryId;

	private final Long dataTypeId;
	
	private final String category;

	private final String algorithmName;

	private final AnalysisLocation location;

	private final DateTime timestamp;

	private final DateTime detectionTimestamp;

	private final double actualValue;

	private final double actualThreshold;

	private final double normalizedValue;

	private final double normalizedThreshold;

	private final double totalValue;

	private final double totalThreshold;

	private final EventDisposition disposition;

	public AnomalyDTO(Anomaly event, EpiCenterUser user) {
		super();
		this.id = event.getId();
		this.timestamp = event.getAnalysisTimestamp();
		this.detectionTimestamp = event.getTimestamp();
		this.geography = new GeographyDTO(event.getGeography(), user);
		this.locationPoint = event.getGeography() == null ? null : event.getGeography().getGeometry().getCentroid();
		this.category = event.getClassification() == null ? null : event.getClassification().getCategory();
		this.categoryId = event.getClassification() == null ? null : event.getClassification().getId();
		this.algorithmName = event.getMethod().getName();
		this.location = event.getTask().getLocation();
		this.classifierId = event.getClassification().getClassifier().getId();
		this.classifierName = event.getClassification().getClassifier().getName();
		this.dataTypeId = event.getSet().getDatatype().getId();
		this.actualValue = FormatUtils.round(event.getObservedValue(), 2);
		this.actualThreshold = FormatUtils.round(event.getObservedThreshold(), 2);
		this.normalizedValue = FormatUtils.round(event.getNormalizedValue(), 2);
		this.normalizedThreshold = FormatUtils.round(event.getNormalizedThreshold(), 2);
		this.totalValue = FormatUtils.round(event.getTotalValue(), 2);
		this.totalThreshold = FormatUtils.round(event.getTotalThreshold(), 2);
		this.disposition = event.getDisposition();

		this.description = event.getDescription();
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
	 * @return the geography
	 */
	public GeographyDTO getGeography() {
		return geography;
	}

	/**
	 * @return the locationPoint
	 */
	public Point getLocationPoint() {
		return locationPoint;
	}

	/**
	 * @return the categoryId
	 */
	public Long getCategoryId() {
		return categoryId;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @return the location
	 */
	public AnalysisLocation getLocation() {
		return location;
	}

	/**
	 * @return the timestamp
	 */
	public DateTime getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return the classifierId
	 */
	public Long getClassifierId() {
		return classifierId;
	}
	
	/**
	 * @return the classifierName
	 */
	public String getClassifierName() {
		return classifierName;
	}

	/**
	 * @return the actualValue
	 */
	public double getActualValue() {
		return actualValue;
	}

	/**
	 * @return the actualThreshold
	 */
	public double getActualThreshold() {
		return actualThreshold;
	}

	/**
	 * @return the normalizedValue
	 */
	public double getNormalizedValue() {
		return normalizedValue;
	}

	/**
	 * @return the normalizedThreshold
	 */
	public double getNormalizedThreshold() {
		return normalizedThreshold;
	}

	/**
	 * @return the totalValue
	 */
	public double getTotalValue() {
		return totalValue;
	}

	/**
	 * @return the totalThreshold
	 */
	public double getTotalThreshold() {
		return totalThreshold;
	}

	/**
	 * @return the disposition
	 */
	public EventDisposition getDisposition() {
		return disposition;
	}

	/**
	 * @return the detectionTimestamp
	 */
	public DateTime getDetectionTimestamp() {
		return detectionTimestamp;
	}

	/**
	 * @return the dataTypeId
	 */
	public Long getDataTypeId() {
		return dataTypeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		return EqualsBuilder.reflectionEquals(this, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
