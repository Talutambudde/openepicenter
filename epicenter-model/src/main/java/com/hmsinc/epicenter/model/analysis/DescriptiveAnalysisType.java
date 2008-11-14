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

import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.provider.Facility;

/**
 * @author shade
 * @version $Id: DescriptiveAnalysisType.java 1402 2008-03-28 19:56:39Z steve.kondik $
 */
public enum DescriptiveAnalysisType {

	BY_FACILITY(Facility.class, "Facility Name"),
	BY_AGE_GROUP(AgeGroup.class, "Age Group"), 
	BY_GENDER(Gender.class, "Gender"), 
	BY_ZIPCODE(Zipcode.class, "Zipcode");

	private final Class<? extends QueryableAttribute> aggregateAttribute;

	private final String description;

	DescriptiveAnalysisType(final Class<? extends QueryableAttribute> aggregateAttribute, final String description) {
		this.aggregateAttribute = aggregateAttribute;
		this.description = description;
	}

	/**
	 * @return the aggregateAttribute
	 */
	public Class<? extends QueryableAttribute> getAggregateAttribute() {
		return aggregateAttribute;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
