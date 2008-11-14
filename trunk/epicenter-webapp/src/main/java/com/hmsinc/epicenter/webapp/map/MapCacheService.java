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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.Validate;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.service.data.DataQueryService;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesPeriod;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A caching service to improve map performance.
 * 
 * @author shade
 * @version $Id: MapCacheService.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@Service
public class MapCacheService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	/*
	 * EHCache region for holding cached timeseries data.
	 */
	@Resource
	private Cache mapCache;

	@Resource
	private FeatureIndexService featureIndexService;

	@Resource
	private DataQueryService dataQueryService;

	/**
	 * Gets counts using the feature cache.
	 * 
	 * @param <T>
	 * @param analysisParameters
	 * @param period
	 * @param aggregateGeographyType
	 * @return
	 */
	public <T extends Geography> Map<T, TimeSeries> getCachedCounts(StyleParameters styleParameters,
			Class<T> aggregateGeographyType) {

		Validate.notNull(styleParameters, "Parameters must be specified.");
		
		final AnalysisParameters analysisParameters = styleParameters.getParameters();
		Validate.notNull(analysisParameters, "Parameters must be specified.");
		Validate.notNull(analysisParameters.getFilter(), "MBR filter must be specified.");

		/*
		 * Find the geographies in this MBR..
		 * 
		 * We'll use an in-memory quadtree to keep track of geographies to save
		 * on database queries..
		 */
		final List<T> containers = featureIndexService.getFeatures(
				analysisParameters.getFilter(), 4326,
				aggregateGeographyType);

		logger.trace("Features: {}", containers);

		final Map<T, String> uncached = new HashMap<T, String>();

		final Map<T, TimeSeries> tsc = new HashMap<T, TimeSeries>();

		/*
		 * Check for cached results first, and discard any negatively cached
		 * items for now.
		 */
		for (T container : containers) {

			/*
			 * We can skip over geographies that were specified in the secondary
			 * filter (security).
			 * 
			 */
			final Geometry filter = analysisParameters.getSecondaryFilter();
			if (filter == null
					
					|| (filter.getArea() > 500
							&& (filter.getEnvelopeInternal().contains(container.getGeometry().getEnvelopeInternal())
							|| filter.getEnvelopeInternal().intersects(container.getGeometry().getEnvelopeInternal())))
							
					|| filter.contains(container.getGeometry())
					|| filter.equals(container.getGeometry())
					|| filter.covers(container.getGeometry())) {
				
				final String key = getCacheKey(styleParameters, container);
				final Element obj = mapCache.get(key);

				if (obj == null) {

					uncached.put(container, key);

				} else {

					if ("NODATA".equals(obj.getValue())) {
						logger.trace("Using negatively cached feature: {}", container);

					} else {

						final TimeSeries cachedItem = (TimeSeries) obj.getValue();
						tsc.put(container, cachedItem);
						logger.trace("Using cached feature: {}", container);
					}
				}

			} else {
				logger.trace("Skipping filtered feature: {}", container);
			}
		}

		if (uncached.size() > 0) {

			logger.trace("Uncached features: {}", uncached);

			/*
			 * Clone the parameters, and swap the filter out for a geography
			 * list
			 */
			final AnalysisParameters clonedParameters = (AnalysisParameters) analysisParameters.clone();
			clonedParameters.setFilter(null);
			clonedParameters.setSecondaryFilter(null);
			
			clonedParameters.getContainers().addAll(uncached.keySet());
			
			/*
			 * Perform the query and cache the results.
			 */
			final Map<T, TimeSeries> query = dataQueryService.queryCombined(clonedParameters, aggregateGeographyType);

			for (Map.Entry<T, TimeSeries> entry : query.entrySet()) {
				mapCache.put(new Element(uncached.get(entry.getKey()), entry.getValue()));
				uncached.remove(entry.getKey());
			}

			/*
			 * Negatively cache anything that we didn't return from the query.
			 */
			for (Map.Entry<T, String> item : uncached.entrySet()) {
				mapCache.put(new Element(item.getValue(), "NODATA"));
				logger.trace("Negatively caching: {}", item.getKey());
			}

			tsc.putAll(query);
		}

		return tsc;
	}

	/**
	 * Builds the cache key that is valid for this request.
	 * 
	 * TODO: Refactor this so we don't need to mess around with it if we add new
	 * parameters.
	 * 
	 * @param parameters
	 * @param feature
	 * @return
	 */
	private static String getCacheKey(final StyleParameters styleParameters, final Geography feature) {

		final AnalysisParameters parameters = styleParameters.getParameters();
		
		final StringBuilder builder = new StringBuilder();
		builder.append(feature.getId()).append(":")
			   .append(makeKeyFromCollection(parameters.getAttributes())).append(":")
			   .append(makeKeyFromCollection(parameters.getClassifications())).append(":")
			   .append(parameters.getLocation().ordinal()).append(":")
			   .append(parameters.getDataRepresentation().ordinal()).append(":")
			   .append(parameters.getDataConditioning().ordinal()).append(":");
		
		if (new LocalDate().equals(parameters.getEndDate().toLocalDate())) {
			builder.append(TimeSeriesPeriod.HOUR.truncate(parameters.getStartDate()).getMillis()).append(":")
				   .append(TimeSeriesPeriod.HOUR.truncate(parameters.getEndDate()).getMillis());
		} else {
			builder.append(parameters.getStartDate().getMillis()).append(":")
				   .append(parameters.getEndDate().getMillis());
		}
		return builder.toString();
	}

	/**
	 * @param attributeList
	 * @return
	 */
	private static String makeKeyFromCollection(final Collection<? extends QueryableAttribute> attributeList) {
		final StringBuilder builder = new StringBuilder();
		for (QueryableAttribute attribute : attributeList) {
			if (builder.length() > 0) {
				builder.append(",");
			}
			builder.append(attribute.getId());
		}
		return builder.toString();
	}
}
