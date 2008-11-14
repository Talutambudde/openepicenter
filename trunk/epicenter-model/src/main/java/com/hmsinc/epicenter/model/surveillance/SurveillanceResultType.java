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
package com.hmsinc.epicenter.model.surveillance;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

import com.hmsinc.epicenter.model.analysis.DataRepresentation;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: SurveillanceResultType.java 1557 2008-04-14 14:37:44Z steve.kondik $
 */
@XmlEnum(String.class)
@XmlType(namespace = "http://epicenter.hmsinc.com/model")
public enum SurveillanceResultType {

	ACTUAL(DataRepresentation.ACTUAL, "Number of Visits"), 
	TOTAL(DataRepresentation.ACTUAL, "Total Visits"),
	NORMALIZED(DataRepresentation.PERCENTAGE_OF_TOTAL, "Percentage of All Visits"),
	POPULATION(DataRepresentation.POPULATION_RATE, "Rate per 100,000 Population");
	
	private final DataRepresentation dataRepresentation;
	
	private final String description;
		
	SurveillanceResultType(final DataRepresentation dataRepresentation, final String description) {
		this.dataRepresentation = dataRepresentation;
		this.description = description;
	}

	/**
	 * @return
	 */
	public DataRepresentation getDataRepresentation() {
		return dataRepresentation;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	
}
