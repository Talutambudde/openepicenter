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
package com.hmsinc.epicenter.model.test.integration;

import junit.framework.TestCase;

import com.hmsinc.epicenter.model.geography.util.Geocoder;
import com.hmsinc.epicenter.model.geography.util.GeocoderDotUSClient;
import com.vividsolutions.jts.geom.Point;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:GeocoderTest.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class GeocoderTest extends TestCase {

	public void testGeocoderDotUS() throws Throwable {

		final Geocoder g = new GeocoderDotUSClient();
		assertTrue(g.geocode("700 River Ave", "Pittsburgh", "PA", "15216") instanceof Point);

	}
}
