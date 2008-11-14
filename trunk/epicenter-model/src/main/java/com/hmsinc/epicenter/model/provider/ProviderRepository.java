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
package com.hmsinc.epicenter.model.provider;

import java.util.List;

import com.hmsinc.epicenter.model.Repository;
import com.hmsinc.epicenter.model.geography.Geography;

/**
 * Manages the repository of ProviderObjects.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ProviderRepository.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
public interface ProviderRepository extends Repository<ProviderObject, Long> {

	/**
	 * Gets all DataConnections, eagerly fetching Facilities.
	 * 
	 * @return
	 */
	public abstract List<DataConnection> getAllDataConnections();

	/**
	 * Gets a DataConnection by name.
	 * 
	 * @param dataConnectionName
	 * @return
	 */
	public abstract DataConnection getDataConnectionByName(final String dataConnectionName);

	/**
	 * Gets a Facility by it's unique identifier..
	 * 
	 * @param identifier
	 * @return
	 */
	public abstract Facility getFacilityByIdentifier(final String identifier);

	/**
	 * Gets the list of facilities in a given Geography.
	 * 
	 * @param geography
	 * @return
	 */
	public abstract List<Facility> getFacilitiesInGeography(final Geography geography);



}