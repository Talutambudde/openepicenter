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
package com.hmsinc.epicenter.webapp.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

/**
 * @author shade
 * @version $Id: GeometryUtils.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
public class GeometryUtils {

	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	/**
	 * Gets a buffered bounding box for a list of points.
	 * 
	 * This can be used to center a map on a bunch of markers.
	 * 
	 * @param centroids
	 * @return the bounding box
	 */
	public static Envelope getBoundingBox(Collection<Point> centroids) {
		final Envelope env = new Envelope(geometryFactory.createMultiPoint(centroids.toArray(new Point[centroids.size()]))
				.getEnvelopeInternal());
		env.expandBy(env.getWidth() * .2, env.getHeight() * .2);
		return env;
	}
	
	/**
	 * Takes a set of Geography objects and returns the centroid.
	 * 
	 * @param geographies
	 * @return
	 */
	public static Point getCentroidOfCollection(Collection<? extends Geography> geographies) {
		
		final Point centroid;
		if (geographies != null && geographies.size() > 0) {
			final Set<Geometry> geometries = toGeometryList(geographies);
			centroid = GeometryCombiner.combine(geometries).getCentroid();
		} else {
			centroid = null;
		}
		return centroid;
	}
	
	/**
	 * Converts an Envelope to a Geometry in WGS84.
	 * 
	 * @param envelope
	 * @return
	 */
	public static Geometry toGeometry(final Envelope envelope) {
		return geometryFactory.toGeometry(envelope);
	}
	
	/**
	 * @param pg
	 * @return
	 */
	public static Geometry toGeometry(final PreparedGeometry pg) {
		return (pg == null ? null : pg.getGeometry());
	}
	
	/**
	 * Extracts geometries from geographies.
	 * 
	 * @param geographies
	 * @return
	 */
	public static Set<Geometry> toGeometryList(final Collection<? extends Geography> geographies) {
		
		final Set<Geometry> geometries = new HashSet<Geometry>();
		if (geographies != null) {
			for (Geography geography : geographies) {
				if (geography.getGeometry() != null) {
					geometries.add(geography.getGeometry());
				}
			}
		}
		return geometries;
	}
	
	/**
	 * Returns a WMS layer name for the specified geography.
	 * 
	 * @param geography
	 * @return
	 */
	public static String toLayerName(final Geography geography) {
		return "epicenter:GEO_" + ModelUtils.getRealClass(geography).getSimpleName().toUpperCase();
	}
	
	/**
	 * @param geographies
	 * @return
	 */
	public static Geometry union(final Collection<Geography> geographies) {
		
		final Set<Geometry> geometries = toGeometryList(geographies);
		return GeometryCombiner.combine(geometries).union();
	}
	
}
