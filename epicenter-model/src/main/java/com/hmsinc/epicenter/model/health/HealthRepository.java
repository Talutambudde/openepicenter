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
package com.hmsinc.epicenter.model.health;

import java.util.List;

import com.hmsinc.epicenter.model.Repository;
import com.hmsinc.epicenter.model.provider.Facility;

/**
 * Manages the repository of HealthObject types.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:HealthRepository.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
public interface HealthRepository extends Repository<HealthObject, Long> {

	/**
	 * Gets a patient by Patient ID and Facility.
	 * 
	 * @param patientId
	 * @param facility
	 * @return
	 */
	public Patient getPatient(final String patientId, final Facility facility);

	/**
	 * @param patientId
	 * @param facility
	 * @param visitNumber
	 * @return
	 */
	public List<? extends Interaction> getInteractions(final String patientId, final Facility facility, final String visitNumber);
	
	/**
	 * Looks up the "natural key" for a record. This value is unique per patient
	 * interaction and is used to prevent duplicates.
	 * 
	 * Only used for registration.
	 * 
	 * @param naturalKey
	 * @return
	 */
	public Long findExistingNaturalKey(String naturalKey);
	
	/**
	 * Finds any pre-existing discharge records.
	 * 
	 * @param discharge
	 * @return
	 */
	public Long findExistingDischarge(final Discharge discharge);

	/**
	 * Finds any pre-existing admit records.
	 * 
	 * @param admit
	 * @return
	 */
	public Long findExistingAdmit(final Admit admit);
		
}