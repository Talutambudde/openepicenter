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
package com.hmsinc.epicenter.model.attribute;

import com.hmsinc.epicenter.model.Repository;

public interface AttributeRepository extends Repository<AttributeObject, Long> {

	/**
	 * Gets a Gender from the repository using it's abbreviation.
	 * 
	 * @param abbreviation
	 * @return
	 */
	public Gender getGenderByAbbreviation(final String abbreviation);

	/**
	 * Gets an AgeGroup from the repository that corresponds to a particular age.
	 * 
	 * @param age
	 * @return 
	 */
	public AgeGroup getAgeGroupForAge(final Integer age);

	/**
	 * Gets a PatientClass by abbreviation.
	 * 
	 * @param abbreviation
	 * @return
	 */
	public PatientClass getPatientClassByAbbreviation(final String abbreviation);

	/**
	 * Gets a PatientClass by name.
	 * 
	 * @param name
	 * @return
	 */
	public PatientClass getPatientClassByName(final String name);
	
}