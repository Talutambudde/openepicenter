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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author shade
 * @version $Id: EnvelopeUtils.java 1223 2008-03-05 20:12:59Z steve.kondik $
 */
public class EnvelopeUtils {

	private static final GeometryFactory geomFactory = new GeometryFactory();

	public static Geometry toGeometry(Envelope env, int SRID) {

		final Coordinate[] coords = new Coordinate[5];

		coords[0] = new Coordinate(env.getMinX(), env.getMinY());
		coords[1] = new Coordinate(env.getMinX(), env.getMaxY());
		coords[2] = new Coordinate(env.getMaxX(), env.getMaxY());
		coords[3] = new Coordinate(env.getMaxX(), env.getMinY());
		coords[4] = new Coordinate(env.getMinX(), env.getMinY());

		final Polygon polygon = geomFactory.createPolygon(geomFactory.createLinearRing(coords), null);
		polygon.setSRID(SRID);
		return polygon;

	}
}
