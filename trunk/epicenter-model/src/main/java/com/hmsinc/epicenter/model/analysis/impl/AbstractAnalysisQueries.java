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
package com.hmsinc.epicenter.model.analysis.impl;

import java.util.Collection;

import org.apache.commons.lang.Validate;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.analysis.AnalysisObject;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Region;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.geography.util.EnvelopeUtils;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.ts4j.TimeSeriesPeriod;
import com.hmsinc.hibernate.QueryBuilder;

/**
 * Base class for generating the counts query.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AbstractCountsAnalysisQuery.java 85 2007-08-17 15:43:14Z
 *          steve.kondik $
 */
public abstract class AbstractAnalysisQueries extends AbstractJPARepository<AnalysisObject, Long> implements
		AnalysisQueries {

	/**
	 * Gets the HQL function to use for aggregating by the given period.
	 * 
	 * @param period
	 * @return
	 */
	protected static String getHqlForPeriod(final TimeSeriesPeriod period) {

		final String ret;
		switch (period) {
		case YEAR:
			ret = "date_trunc_year";
			break;
		case MONTH:
			ret = "date_trunc_month";
			break;
		case DAY:
			ret = "date";
			break;
		case HOUR:
			ret = "date_trunc_hour";
			break;
		case MINUTE:
			ret = "date_trunc_minute";
			break;
		default:
			throw new UnsupportedOperationException("Unsupported period: " + period);
		}

		return ret;

	}

	
	/**
	 * Must return the alias to the items Zipcode.
	 * 
	 * @param analysisParameters
	 * @return
	 */
	protected abstract String getZipcodeName(AnalysisParameters analysisParameters);
	
	
	/**
	 * Assembles the HQL for spatial aggregation, filtering, and security.
	 * 
	 * Abandon hope, all ye who enter.
	 * 
	 * @param <T>
	 * @param query
	 * @param aggregateGeographyType
	 * @param analysisParameters
	 * @return
	 */
	protected <T extends Geography> String applyGeography(QueryBuilder query,  
			Class<T> aggregateGeographyType, AnalysisParameters analysisParameters) {
		
		Validate.notNull(query, "A QueryBuilder must be specified.");
		Validate.notNull(analysisParameters, "Parameters must be specified.");
		Validate.notNull(aggregateGeographyType, "Aggregate type must be specified.");
		Validate.isTrue((analysisParameters.getContainers() != null && analysisParameters.getContainers().size() > 0) || analysisParameters.getFilter() != null, "A container or filter must be specified.");

		final String zipcodeName = getZipcodeName(analysisParameters);
		
		final String aggregateName;
		
		if (analysisParameters.getContainers().size() == 0) {
			
			if (Zipcode.class.equals(aggregateGeographyType)) {
				aggregateName = zipcodeName;
				
			} else if (County.class.equals(aggregateGeographyType)) {
				query.addJoin("join " + zipcodeName + ".counties as county");
				aggregateName = "county";
				
			} else if (State.class.equals(aggregateGeographyType)) {
				aggregateName = zipcodeName + ".state";
				
			} else if (Region.class.equals(aggregateGeographyType)) {
				query.addEntity(Region.class, "region").addWhere("within(" + zipcodeName + ".centroid, region.geometry) = 'TRUE'");
				aggregateName = "region";
				
			} else {
				throw new UnsupportedOperationException("Unsupported aggregate: " + aggregateGeographyType.getName());
			}
						
		} else {
			
			// Check if we have a HibernateProxy and unwind it (lazy properties)
			final Class<?> containerGeographyType = getContainerType(analysisParameters);
			final Collection<Geography> containers = analysisParameters.getContainers();
			Validate.notNull(containers, "No valid containers.");
			
			String spatialFilter = "within";
			if (containers.size() == 1) {
				final Geography g = containers.iterator().next();
				Validate.notNull(g.getGeometry(), "No valid geometry in object.");
				if (g.getGeometry().getArea() > 500) {
					spatialFilter = "filter";
				}
			}
			
			// Simple is just using the container as the aggregate
			final boolean isSimple = aggregateGeographyType.equals(containerGeographyType) ? true : false;
			
			if (Zipcode.class.equals(containerGeographyType)) {
				
				if (isSimple) {
					query.addWhere(zipcodeName + " in (:zipcode)", "zipcode", containers);
					aggregateName = zipcodeName;
				} else {
					throw new UnsupportedOperationException("Unsupported aggregate: " + aggregateGeographyType.getName());
				}
				
			} else if (County.class.equals(containerGeographyType)) {
						
				query.addJoin("join " + zipcodeName + ".counties as county").addWhere("county in (:county)", "county", containers);
				
				if (isSimple) {
					aggregateName = "county";
					
				} else if (Zipcode.class.equals(aggregateGeographyType)) {
					aggregateName = zipcodeName;
					
				} else if (Region.class.equals(aggregateGeographyType)) {
					query.addEntity(Region.class, "region").addWhere(spatialFilter + "(county.geometry, region.geometry) = 'TRUE'");
					aggregateName = "region";
					
				} else {
					throw new UnsupportedOperationException("Unsupported aggregate: " + aggregateGeographyType.getName());
				}
				
			} else if (State.class.equals(containerGeographyType)) {
				
				if (isSimple) {
					query.addWhere(zipcodeName + ".state in (:state)", "state", containers);
					aggregateName = zipcodeName + ".state";
					
				} else if (Zipcode.class.equals(aggregateGeographyType)) {
					query.addWhere(zipcodeName + ".state in (:state)", "state", containers);
					aggregateName = zipcodeName;
					
				} else if (County.class.equals(aggregateGeographyType)) {
					query.addJoin("join " + zipcodeName + ".counties as county").addWhere("county.state in (:state)", "state", containers);
					aggregateName = "county";
					
				} else if (Region.class.equals(aggregateGeographyType)) {
					query.addWhere(zipcodeName + ".state in (:state)", "state", containers);
					query.addEntity(Region.class, "region").addWhere(spatialFilter + "(region.geometry, " + zipcodeName + ".state.geometry) = 'TRUE'");
					aggregateName = "region";
					
				} else {
					throw new UnsupportedOperationException("Unsupported aggregate: " + aggregateGeographyType.getName());
				}
				
			} else if (Region.class.equals(containerGeographyType)) {
				
				query.addEntity(Region.class, "region").addWhere("region in (:region)", "region", containers);
				
 				if (isSimple) {
					query.addWhere(spatialFilter + "(" + zipcodeName + ".centroid, region.geometry) = 'TRUE'");
					aggregateName = "region";
					
				} else if (Zipcode.class.equals(aggregateGeographyType)) {
					query.addWhere(spatialFilter + "(" + zipcodeName + ".centroid, region.geometry) = 'TRUE'");
					aggregateName = zipcodeName;
					
				} else if (County.class.equals(aggregateGeographyType)) {
					query.addJoin("join " + zipcodeName + ".counties as county");
					query.addWhere(spatialFilter + "(county.geometry, region.geometry) = 'TRUE'");
					aggregateName = "county";
					
				} else if (State.class.equals(aggregateGeographyType)) {
					query.addWhere(spatialFilter + "(" + zipcodeName + ".state.geometry, region.geometry) = 'TRUE'");
					aggregateName = zipcodeName + ".state";
					
				} else {
					throw new UnsupportedOperationException("Unsupported aggregate: " + aggregateGeographyType.getName());
				}
				
			} else {
				throw new UnsupportedOperationException("Unsupported container: " + containerGeographyType.getName());
			}
		}
		
		// Apply filter to aggregateGeography
		if (analysisParameters.getFilter() != null) {
			query.addWhere("filter(" + aggregateName + (Zipcode.class.equals(aggregateGeographyType) ? ".centroid" : ".geometry") + ", :filter) = 'TRUE'", "filter", EnvelopeUtils.toGeometry(analysisParameters.getFilter(), 4326));
		}
		
		// Apply secondary filter to aggregateGeography
		if (analysisParameters.getSecondaryFilter() != null) {
			final String spatialFilter = analysisParameters.getSecondaryFilter().getArea() > 500 ? "filter" : "within";
			query.addWhere(spatialFilter + "(" + aggregateName + (Zipcode.class.equals(aggregateGeographyType) ? ".centroid" : ".geometry") + ", :secondaryFilter) = 'TRUE'", "secondaryFilter", analysisParameters.getSecondaryFilter());
		}
		
		return aggregateName;

	}
	
	/**
	 * Gets the grouping offset for date truncation.  This is just the end date as millis.
	 * 
	 * @param endDate
	 * @param period
	 * @return
	 */
	protected long getTimeOffset(final AnalysisParameters analysisParameters) {
		
		long ret = 0L;
		
		if (TimeSeriesPeriod.DAY.equals(analysisParameters.getPeriod())) {
			
			ret = analysisParameters.getEndDate().getMillis();
		}
			
		return ret;
	}
	
	/**
	 * Gets the class of geographies to use for this query.
	 * 
	 * @param analysisParameters
	 * @return
	 */
	protected Class<?> getContainerType(AnalysisParameters analysisParameters) {
		Class<?> last = null;
		if (analysisParameters.getContainers() != null && analysisParameters.getContainers().size() > 0) {
			for (Geography geography : analysisParameters.getContainers()) {
				final Class<?> t = ModelUtils.getRealClass(geography);
				Validate.isTrue(last == null || last.equals(t), "All containers must be of the same type.  Was: " + last + " Got: " + t);
				last = t;
			}
		}
		return last;
	}
}
