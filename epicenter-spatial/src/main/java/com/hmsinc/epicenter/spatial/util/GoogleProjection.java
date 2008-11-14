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
package com.hmsinc.epicenter.spatial.util;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author shade
 * @veraion $Id: GoogleProjection.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public class GoogleProjection {

    public static final String GOOGLE_MERCATOR_WKT =
        "PROJCS[\"Google Mercator\",\n" +
        "  GEOGCS[\"WGS 84\",\n" +
        "    DATUM[\"World Geodetic System 1984\",\n" +
        "      SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]],\n" +
        "      AUTHORITY[\"EPSG\",\"6326\"]],\n" +
        "    PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]],\n" +
        "    UNIT[\"degree\", 0.017453292519943295],\n" +
        "    AXIS[\"Geodetic latitude\", NORTH],\n" +
        "    AXIS[\"Geodetic longitude\", EAST],\n" +
        "    AUTHORITY[\"EPSG\",\"4326\"]],\n" +
        "  PROJECTION[\"Mercator_1SP\"],\n" +
        "  PARAMETER[\"semi_minor\", 6378137.0],\n" +
        "  PARAMETER[\"latitude_of_origin\", 0.0],\n" +
        "  PARAMETER[\"central_meridian\", 0.0],\n" +
        "  PARAMETER[\"scale_factor\", 1.0],\n" +
        "  PARAMETER[\"false_easting\", 0.0],\n" +
        "  PARAMETER[\"false_northing\", 0.0],\n" +
        "  UNIT[\"m\", 1.0],\n" +
        "  AXIS[\"Easting\", EAST],\n" +
        "  AXIS[\"Northing\", NORTH],\n" +
        "  AUTHORITY[\"EPSG\",\"900913\"]]";

	public static final CoordinateReferenceSystem GOOGLE_MERCATOR;

	static {

		try {
			GOOGLE_MERCATOR = CRS.parseWKT(GOOGLE_MERCATOR_WKT);
		} catch (FactoryException e) {
			throw new RuntimeException(e);
		}
	}

}
