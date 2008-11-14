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
import java.util.Properties;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.DataConditioning;
import com.hmsinc.epicenter.model.analysis.DataRepresentation;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AnalysisParametersDTO.java 1702 2008-06-05 16:51:59Z steve.kondik $
 */
@DataTransferObject
public class AnalysisParametersDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8840381279253838569L;

	private String classifier;

	private String category;

	private DateTime start;

	private DateTime end;

	private Long geography;

	private AnalysisLocation location;

	private String ageGroup;

	private String gender;

	private String attributes;
	
	private Long datatype;
	
	private DataRepresentation representation = DataRepresentation.ACTUAL;
	
	private DataConditioning conditioning = DataConditioning.NONE;
	
	private String algorithmName;

	private boolean fixDates = true;

	private boolean alwaysSkipOther = true;
	
	private Properties algorithmProperties;
	
	/**
	 * @return the classifier
	 */
	public String getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier
	 *            the classifier to set
	 */
	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the geography
	 */
	public Long getGeography() {
		return geography;
	}

	/**
	 * @param geography
	 *            the geography to set
	 */
	public void setGeography(Long geography) {
		this.geography = geography;
	}

	/**
	 * @return the start
	 */
	public DateTime getStart() {
		return start;
	}

	/**
	 * @param start the start to set
	 */
	public void setStart(DateTime start) {
		this.start = start;
	}

	/**
	 * @return the end
	 */
	public DateTime getEnd() {
		return end;
	}

	/**
	 * @param end the end to set
	 */
	public void setEnd(DateTime end) {
		this.end = end;
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
	 * @return the ageGroup
	 */
	public String getAgeGroup() {
		return ageGroup;
	}

	/**
	 * @param ageGroup
	 *            the ageGroup to set
	 */
	public void setAgeGroup(String ageGroup) {
		this.ageGroup = ageGroup;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender
	 *            the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the representation
	 */
	public DataRepresentation getRepresentation() {
		return representation;
	}

	/**
	 * @param representation the representation to set
	 */
	public void setRepresentation(DataRepresentation representation) {
		this.representation = representation;
	}

	/**
	 * @return the conditioning
	 */
	public DataConditioning getConditioning() {
		return conditioning;
	}

	/**
	 * @param conditioning the conditioning to set
	 */
	public void setConditioning(DataConditioning conditioning) {
		this.conditioning = conditioning;
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @param algorithmName
	 *            the algorithmName to set
	 */
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	/**
	 * @return the fixDates
	 */
	public boolean isFixDates() {
		return fixDates;
	}

	/**
	 * @param fixDates
	 *            the fixDates to set
	 */
	public void setFixDates(boolean fixDates) {
		this.fixDates = fixDates;
	}

	/**
	 * @return the alwaysSkipOther
	 */
	public boolean isAlwaysSkipOther() {
		return alwaysSkipOther;
	}

	/**
	 * @param alwaysSkipOther the alwaysSkipOther to set
	 */
	public void setAlwaysSkipOther(boolean alwaysSkipOther) {
		this.alwaysSkipOther = alwaysSkipOther;
	}

	/**
	 * @return the algorithmProperties
	 */
	public Properties getAlgorithmProperties() {
		return algorithmProperties;
	}

	/**
	 * @param algorithmProperties the algorithmProperties to set
	 */
	public void setAlgorithmProperties(Properties algorithmProperties) {
		this.algorithmProperties = algorithmProperties;
	}
	
	/**
	 * @return the datatype
	 */
	public Long getDatatype() {
		return datatype;
	}

	/**
	 * @param datatype the datatype to set
	 */
	public void setDatatype(Long datatype) {
		this.datatype = datatype;
	}

	/**
	 * @return the attributes
	 */
	public String getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(String attributes) {
		this.attributes = attributes;
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
