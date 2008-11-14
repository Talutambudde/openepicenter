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
package com.hmsinc.epicenter.model.analysis;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Attribute;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.ts4j.TimeSeriesPeriod;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Describes parameters to use for analysis.
 * 
 * FIXME: Make this immutable.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AnalysisParameters.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class AnalysisParameters implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1530854406063287852L;

	private DateTime startDate;

	private DateTime endDate;

	private DataType dataType;
	
	private Set<Classification> classifications = new HashSet<Classification>();

	private Set<Attribute> attributes = new HashSet<Attribute>();
	
	private AnalysisLocation location = AnalysisLocation.HOME;

	private Set<Geography> containers = new HashSet<Geography>();

	private Envelope filter;

	private Geometry secondaryFilter;

	private TimeSeriesPeriod period = TimeSeriesPeriod.DAY;

	private DataConditioning dataConditioning = DataConditioning.NONE;
	
	private DataRepresentation dataRepresentation = DataRepresentation.ACTUAL;
	
	/**
	 * @param startDate
	 * @param endDate
	 */
	public <T extends Geography> AnalysisParameters(DateTime startDate, DateTime endDate) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
	}

	/**
	 * @param startDate
	 * @param endDate
	 * @param classifications
	 * @param location
	 */
	public AnalysisParameters(DateTime startDate, DateTime endDate, Collection<Classification> classificationList,
			AnalysisLocation location, Geography container) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.classifications.addAll(classificationList);
		this.location = location;
		this.containers.add(container);
	}

	/**
	 * @param startDate
	 * @param endDate
	 * @param classifications
	 * @param location
	 */
	public AnalysisParameters(DateTime startDate, DateTime endDate, Geography container, AnalysisLocation location,
			DataType dataType, Collection<Classification> classificationList, Collection<Attribute> attributeList) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.classifications.addAll(classificationList);
		this.location = location;
		this.containers.add(container);
		this.dataType = dataType;
		this.attributes.addAll(attributeList);
	}
	
	/**
	 * @return the startDate
	 */
	public DateTime getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(DateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public DateTime getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(DateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the classifications
	 */
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
	 * @return the ageGroups
	 */
	public Set<AgeGroup> getAgeGroups() {
		final Set<AgeGroup> allAgeGroups = new HashSet<AgeGroup>();
		if (attributes != null) {
			for (Attribute a : attributes) {
				if (a instanceof AgeGroup) {
					allAgeGroups.add((AgeGroup)a);
				}
			}
		}
		return Collections.unmodifiableSet(allAgeGroups);
	}

	/**
	 * @return the genders
	 */
	public Set<Gender> getGenders() {
		final Set<Gender> allGenders = new HashSet<Gender>();
		if (attributes != null) {
			for (Attribute a : attributes) {
				if (a instanceof Gender) {
					allGenders.add((Gender)a);
				}
			}
		}
		return Collections.unmodifiableSet(allGenders);
	}


	/**
	 * @return the location
	 */
	public AnalysisLocation getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(AnalysisLocation location) {
		this.location = location;
	}

	/**
	 * @return the filter
	 */
	public Envelope getFilter() {
		return filter;
	}

	/**
	 * @param filter
	 *            the filter to set
	 */
	public void setFilter(Envelope filter) {
		this.filter = filter;
	}

	/**
	 * @return the secondaryFilter
	 */
	public Geometry getSecondaryFilter() {
		return secondaryFilter;
	}

	/**
	 * @param secondaryFilter
	 *            the secondaryFilter to set
	 */
	public void setSecondaryFilter(Geometry secondaryFilter) {
		this.secondaryFilter = secondaryFilter;
	}

	/**
	 * @return the period
	 */
	public TimeSeriesPeriod getPeriod() {
		return period;
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(TimeSeriesPeriod period) {
		this.period = period;
	}

	/**
	 * @return the dataType
	 */
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the containers
	 */
	public Set<Geography> getContainers() {
		if (this.containers == null) {
			this.containers = new HashSet<Geography>();
		}
		return this.containers;
	}

	/**
	 * @param containers
	 *            the containers to set
	 */
	public void setContainers(Set<Geography> containers) {
		if (containers == null) {
			this.containers = new HashSet<Geography>();
		} else {
			this.containers = containers;
		}
	}

	/**
	 * @param container
	 */
	public void setContainer(Geography container) {
		this.containers = new HashSet<Geography>();
		if (container != null) {
			this.containers.add(container);
		}
	}

	/**
	 * @return
	 */
	public Geography getContainer() {
		Validate.isTrue(getContainers().size() < 2, "More than one item in the parameters!");
		return getContainers().size() == 1 ? getContainers().iterator().next() : null;
	}

	/**
	 * @return the attributes
	 */
	public Set<Attribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(Set<Attribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the dataConditioning
	 */
	public DataConditioning getDataConditioning() {
		return dataConditioning;
	}

	/**
	 * @param dataConditioning the dataConditioning to set
	 */
	public void setDataConditioning(DataConditioning dataConditioning) {
		this.dataConditioning = dataConditioning;
	}

	/**
	 * @return the dataRepresentation
	 */
	public DataRepresentation getDataRepresentation() {
		return dataRepresentation;
	}

	/**
	 * @param dataRepresentation the dataRepresentation to set
	 */
	public void setDataRepresentation(DataRepresentation dataRepresentation) {
		this.dataRepresentation = dataRepresentation;
	}

	/**
	 * Validates that all the required parameters are set.
	 */
	public void validate() {
		
		Validate.notNull(startDate, "Start date must be set.");
		Validate.notNull(endDate, "End date must be set.");
		
		Validate.isTrue(startDate.isBefore(endDate) || startDate.equals(endDate), "Start date must be before or equal to end date.");
				
		Validate.isTrue((containers != null && containers.size() > 0) || filter != null, "Container or spatial filter must be set.");
		
		Validate.notNull(period, "Period must be set.");
		
		Validate.notNull(location, "Patient location must be set.");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toStringExclude(this, "container");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {

		final AnalysisParameters clone;

		try {
			clone = (AnalysisParameters) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}

		clone.getAttributes().addAll(attributes);
		clone.getClassifications().addAll(classifications);
		clone.getContainers().addAll(containers);

		return clone;
	}

}
