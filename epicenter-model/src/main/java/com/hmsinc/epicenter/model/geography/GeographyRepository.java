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
package com.hmsinc.epicenter.model.geography;

import java.util.Collection;
import java.util.List;

import com.hmsinc.epicenter.model.Repository;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

public interface GeographyRepository extends Repository<Geography, Long> {

	/**
	 * Gets the named geographies of geographyType.
	 * 
	 * @param <T>
	 * @param names
	 * @param geographyType
	 * @return
	 */
	public <T extends Geography> List<T> getGeography(final List<String> names, final Class<T> geographyType);

	/**
	 * Gets the named geography of geographyType.
	 * 
	 * @param <T>
	 * @param names
	 * @param geographyType
	 * @return
	 */
	public <T extends Geography> T getGeography(final String name, final Class<T> geographyType);
	
	/**
	 * Get the requested Geography objects given a State.
	 * 
	 * @param state
	 * @param query
	 * @param geographyType
	 * @param includePartial
	 * @return
	 */
	public <T extends Geography> List<T> getGeographiesInState(final State state, final String query, final Class<T> geographyType, final boolean includePartial);

	/**
	 * Searches for a geography by name, returning partial matches if no exact match.
	 * 
	 * @param <T>
	 * @param query
	 * @param geographyType
	 * @return
	 */
	public <T extends Geography> List<T> searchGeographies(String query, final Class<T> geographyType);
	
	/**
	 * Select an exact list of counties from a state.
	 * 
	 * @param state
	 * @param countyNames
	 * @return
	 */
	public List<County> getCountiesInState(final State state, final Collection<String> countyNames);
	
	/**
	 * Select an exact list of zipcodes from a state.
	 * 
	 * @param state
	 * @param zipcodeNames
	 * @return
	 */
	public List<Zipcode> getZipcodesInState(final State state, final Collection<String> zipcodeNames);
	
	/**
	 * Gets a US State by it's abbreviation.
	 * 
	 * @param abbreviation
	 * @return
	 */
	public State getStateByAbbreviation(final String abbreviation);

	/**
	 * Given a Geography object, this method will use a spatial query to find all entities of
	 * the containedType that are inside/coveredby it's geometry.
	 * 
	 * @param <T>
	 * @param geo
	 * @param containedType
	 * @return
	 */
	public <T extends Geography> List<T> getContained(final Geography geo, final Class<T> containedType);
	
	public <T extends Geography> List<T> getContained(final Geometry geometry, final Class<T> containedType);
	
	public <T extends Geography> List<T> getContained(final Envelope geometry, final int srid, final Class<T> containedType);
	
	/**
	 * Given a Geography object, this method will use a spatial query to find all entities of
	 * the containedType that are inside/coveredby/overlapping/contained/covering it's geometry.
	 * 
	 * @param <T>
	 * @param geo
	 * @param intersectingType
	 * @return
	 */
	public <T extends Geography> List<T> getIntersecting(final Geography geo, final Class<T> intersectingType);
	
	public <T extends Geography> List<T> getIntersecting(final Geometry geometry, final Class<T> intersectingType);
	
	/**
	 * Given a Geometry object, this method will use a spatial query to find all entities of
	 * the containedType that are interacting in any way with it's geometry.
	 * 
	 * @param <T>
	 * @param geo
	 * @param intersectingType
	 * @return
	 */
	public <T extends Geography> List<T> getInteracting(Geometry geometry, Class<T> intersectingType);
		
	/**
	 * Gets population using contained zipcodes.
	 * 
	 * @param geography
	 * @return
	 */
	public Long inferPopulation(Geography geography);
	
}