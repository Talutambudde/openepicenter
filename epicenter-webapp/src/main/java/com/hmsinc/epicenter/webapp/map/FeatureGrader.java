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
package com.hmsinc.epicenter.webapp.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.hmsinc.epicenter.model.geography.Geography;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Assigns arbitrary grades to geography features based on the value generated
 * by previous algorithm. In other words, it groups features into buckets,
 * deciding what feature goes in what bucket.
 * 
 * @author Olek Poplavsky
 * @version $Id: FeatureGrader.java 1573 2008-04-21 14:15:01Z steve.kondik $
 */
@Service
class FeatureGrader {

	<G extends Geography> Map<String, List<Geography>> gradeFeatures(Map<G, Number> featureValueMap) {

		final Map<String, List<Geography>> gradedFeatures = new TreeMap<String, List<Geography>>();

		for (Map.Entry<G, Number> featureEntry : featureValueMap.entrySet()) {
			Geography geography = featureEntry.getKey();
			double value = featureEntry.getValue().doubleValue();
			gradeFeature(geography, value, gradedFeatures);
		}

		return gradedFeatures;
	}

	private void gradeFeature(Geography geography, double value, Map<String, List<Geography>> gradedFeatures) {

		final String prefix;
		if ((geography.getGeometry() instanceof Polygon) || (geography.getGeometry() instanceof MultiPolygon)) {
			prefix = "polygon";
		} else if ((geography.getGeometry() instanceof Point) || (geography.getGeometry() instanceof MultiPoint)) {
			prefix = "point";
		} else {
			throw new UnsupportedOperationException("Unsupported geometry type: " + geography.getGeometry().getClass().getName());
		}
		
		int gradeName = grade(value);
		List<Geography> group = gradedFeatures.get(prefix + gradeName);
		if (group == null) {
			group = new ArrayList<Geography>();
			gradedFeatures.put(prefix + gradeName, group);
		}
		gradedFeatures.get(prefix + gradeName).add(geography);

	}

	private static int grade(double value) {
		
		final int rtn;
		final double adjustedValue = value < 0 ? 0 : value;
		
		if (adjustedValue > 100) {
			rtn = 99;
		} else if (adjustedValue > 99) {
			rtn = 6;
		} else if (adjustedValue > 95) {
			rtn = 5;
		} else if (adjustedValue > 87) {
			rtn = 4;
		} else if (adjustedValue > 68) {
			rtn = 3;
		} else if (adjustedValue > 0) {
			rtn = 2;
		} else {
			rtn = 1;
		}

		return rtn;
	}
}