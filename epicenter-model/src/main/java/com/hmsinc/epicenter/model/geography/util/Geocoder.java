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
package com.hmsinc.epicenter.model.geography.util;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A Geocoder takes an address and returns a POINT Geometry.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Geocoder.java 94 2007-08-20 18:39:14Z steve.kondik $
 */
public interface Geocoder {

	public Geometry geocode(final String address, final String city, final String state, final String zipcode);

}
