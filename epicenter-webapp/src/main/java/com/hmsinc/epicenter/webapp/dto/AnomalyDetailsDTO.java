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

import java.util.Set;
import java.util.TreeSet;

import org.directwebremoting.annotations.DataTransferObject;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.epicenter.model.attribute.Attribute;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.surveillance.notification.EventNotifierUtils;
import com.hmsinc.epicenter.util.FormatUtils;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.vividsolutions.jts.geom.Envelope;

/**
 * @author shade
 * @version $Id: AnomalyDetailsDTO.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@DataTransferObject
public class AnomalyDetailsDTO extends AnomalyDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6975742032397300075L;

	private final Set<InvestigationDTO> investigations = new TreeSet<InvestigationDTO>();
	
	private final Envelope envelope;

	private final double predictedValue;
	
	private double currentValue;

	private final String associatedAlgorithmName;
	
	private final String attributes;
	
	private final String attributeInfo;
	
	public AnomalyDetailsDTO(final Anomaly anomaly, final EpiCenterUser user, final String associatedAlgorithmName) {

		super(anomaly, user);
		
		// Bounding box for the map.
		envelope = new Envelope(anomaly.getGeography().getGeometry().getEnvelopeInternal());
		envelope.expandBy(envelope.getWidth() * .75, envelope.getHeight() * .75);
				
		// Active investigations.
		for (Investigation investigation : anomaly.getInvestigations()) {
			if (SpatialSecurity.isAccessible(user, investigation.getOrganization())) {
				investigations.add(new InvestigationDTO(investigation));
			}
		}
		
		this.predictedValue = FormatUtils.round(anomaly.getResult().getResults().get(SurveillanceResultType.ACTUAL).last().getDoubleProperty(ResultType.PREDICTED), 2);
				
		this.associatedAlgorithmName = associatedAlgorithmName;
		
		final StringBuilder ab = new StringBuilder();
		for (Attribute a : anomaly.getSet().getAttributes()) {
			if (ab.length()> 0) {
				ab.append(",");
			}
			ab.append(a.getId());
		}
		
		this.attributes = ab.toString();
		
		this.attributeInfo = EventNotifierUtils.makeAgeGroupAndGenderString(anomaly);
		
	}

	/**
	 * @return the envelope
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

	/**
	 * @return the investigations
	 */
	public Set<InvestigationDTO> getInvestigations() {
		return investigations;
	}

	/**
	 * @return the predictedValue
	 */
	public double getPredictedValue() {
		return predictedValue;
	}

	/**
	 * @return the currentValue
	 */
	public double getCurrentValue() {
		return currentValue;
	}

	/**
	 * @param currentValue the currentValue to set
	 */
	public void setCurrentValue(double currentValue) {
		this.currentValue = currentValue;
	}

	/**
	 * @return the associatedAlgorithmName
	 */
	public String getAssociatedAlgorithmName() {
		return associatedAlgorithmName;
	}

	/**
	 * @return the attributes
	 */
	public String getAttributes() {
		return attributes;
	}

	/**
	 * @return the attributeInfo
	 */
	public String getAttributeInfo() {
		return attributeInfo;
	}
			
}
