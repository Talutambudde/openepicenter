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

import static com.hmsinc.epicenter.model.util.ModelUtils.disableNestedLoops;
import static com.hmsinc.epicenter.model.util.ModelUtils.enableNestedLoops;
import static com.hmsinc.epicenter.model.util.ModelUtils.namedQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Required;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.analysis.AnalysisObject;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.ClassificationTarget;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;
import com.hmsinc.ts4j.TimeSeriesEntry;
import com.hmsinc.ts4j.analysis.util.AnalysisUtils;

/**
 * Manages the analysis of EpiCenter entities.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AnalysisRepositoryImpl.java 51 2007-08-05 19:22:20Z
 *          steve.kondik $
 */
public class AnalysisRepositoryImpl extends AbstractJPARepository<AnalysisObject, Long> implements AnalysisRepository {

	private AnalysisQueries analysisQueries;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getClassifiersForInteraction(com.hmsinc.epicenter.model.health.Interaction)
	 */
	@SuppressWarnings("unchecked")
	public List<ClassificationTarget> getClassifiersForInteraction(Interaction interaction) {
		return namedQuery(entityManager, "getClassifiersForInteraction").setParameter("interactionClass",
				interaction.getClass()).setParameter("patientClass", interaction.getPatientClass()).getResultList();
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getClassifiers()
	 */
	@SuppressWarnings("unchecked")
	public List<Classifier> getClassifiers() {
		return namedQuery(entityManager, "getClassifiers").getResultList();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.AnalysisRepository#getClassifierByName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Classifier getClassifierByName(final String name) {

		Classifier ret = null;

		final List<Classifier> result = namedQuery(entityManager, "getClassifierByName").setParameter("name", name)
				.getResultList();

		if (result.size() > 0) {
			ret = result.get(0);
		}

		return ret;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.AnalysisRepository#getClassification(com.hmsinc.epicenter.model.analysis.classify.Classifier,
	 *      java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<Classification> getClassifications(final Classifier classifier, final List<String> categories) {
		return namedQuery(entityManager, "getClassifications").setParameter("classifier", classifier).setParameter(
				"categories", categories).getResultList();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getGeographicalCounts(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.ts4j.TimeSeriesPeriod,
	 *      com.hmsinc.epicenter.model.geography.Geography, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> TimeSeriesCollection<T, Classification> getClassifiedCounts(
			AnalysisParameters analysisParameters, Class<T> aggregateGeographyType) {

		// Make sure we have everything needed
		Validate.notNull(analysisParameters);
		Validate.notNull(aggregateGeographyType, "Aggregate type must be specified.");
		analysisParameters.validate();

		logger.trace("Parameters: {}", analysisParameters);

		disableNestedLoops(entityManager);
		
		final Query countsQuery = analysisQueries.createAggregatedAnalysisQuery(analysisParameters,
				aggregateGeographyType, Classification.class);

		return convertToTimeSeriesCollection(countsQuery.list(), aggregateGeographyType, analysisParameters,
				Classification.class);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getAggregatedCounts(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      java.lang.Class, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography, A extends QueryableAttribute> TimeSeriesCollection<T, A> getAggregatedCounts(
			AnalysisParameters analysisParameters, Class<T> aggregateGeographyType, Class<A> aggregateAttributeType) {

		// Make sure we have everything needed
		Validate.notNull(analysisParameters);
		Validate.notNull(aggregateGeographyType);
		Validate.notNull(aggregateAttributeType);
		analysisParameters.validate();

		logger.trace("Parameters: {}", analysisParameters);

		disableNestedLoops(entityManager);
		
		final Query countsQuery = analysisQueries.createAggregatedAnalysisQuery(analysisParameters,
				aggregateGeographyType, aggregateAttributeType);

		return convertToTimeSeriesCollection(countsQuery.list(), aggregateGeographyType, analysisParameters,
				aggregateAttributeType);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getCombinedCounts(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> Map<T, TimeSeries> getCombinedCounts(AnalysisParameters analysisParameters,
			Class<T> aggregateGeographyType) {

		// Make sure we have everything needed
		Validate.notNull(analysisParameters);
		Validate.notNull(aggregateGeographyType);
		analysisParameters.validate();

		logger.trace("Parameters: {}", analysisParameters);

		disableNestedLoops(entityManager);
		
		final Query countsQuery = analysisQueries.createCombinedAnalysisQuery(analysisParameters,
				aggregateGeographyType);

		return convertToTimeSeriesMap(countsQuery.list(), aggregateGeographyType, analysisParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getTotalCounts(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.ts4j.TimeSeriesPeriod,
	 *      com.hmsinc.epicenter.model.geography.Geography, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> Map<T, TimeSeries> getTotalCounts(AnalysisParameters analysisParameters,
			Class<T> aggregateGeographyType) {

		// Make sure we have everything needed
		Validate.notNull(analysisParameters);
		Validate.notNull(aggregateGeographyType);
		analysisParameters.validate();

		logger.trace("Parameters: {}", analysisParameters);

		disableNestedLoops(entityManager);
		
		final Query countsQuery = analysisQueries.createTotalAnalysisQuery(analysisParameters, aggregateGeographyType);

		return convertToTimeSeriesMap(countsQuery.list(), aggregateGeographyType, analysisParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getNormalizedCounts(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.ts4j.TimeSeriesPeriod,
	 *      com.hmsinc.epicenter.model.geography.Geography, java.lang.Class)
	 */
	public <T extends Geography> TimeSeriesCollection<T, Classification> getNormalizedCounts(
			AnalysisParameters analysisParameters, Class<T> aggregateGeographyType) {

		final Map<T, TimeSeries> totals = getTotalCounts(analysisParameters, aggregateGeographyType);

		final TimeSeriesCollection<T, Classification> counts = getClassifiedCounts(analysisParameters,
				aggregateGeographyType);

		return AnalysisUtils.normalize(counts, totals);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getCases(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.epicenter.model.geography.Geography, java.lang.Long,
	 *      java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public List<? extends Interaction> getCases(AnalysisParameters analysisParameters, Long offset, Long numRows) {

		List<? extends Interaction> ret = new ArrayList<Interaction>();

		// Make sure we have everything needed
		Validate.notNull(analysisParameters);
		analysisParameters.validate();

		logger.trace("Parameters: {}", analysisParameters);

		final org.hibernate.Criteria query = analysisQueries.createGetCasesQuery(analysisParameters, offset, numRows);

		if (query != null) {
			ret = query.list();
		}

		
		return ret;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.AnalysisRepository#getCasesCount(com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.epicenter.model.geography.Geography)
	 */
	public Long getCasesCount(AnalysisParameters analysisParameters) {

		// Make sure we have everything needed
		Validate.notNull(analysisParameters);
		analysisParameters.validate();

		logger.trace("Parameters: {}", analysisParameters);

		disableNestedLoops(entityManager);
		
		final Long count = (Long)analysisQueries.createGetCasesCountQuery(analysisParameters).uniqueResult();
		
		enableNestedLoops(entityManager);

		
		return count;
	}

	/**
	 * Builds a TimeSeriesCollection representing the timeseries from the list
	 * of objects returned from the query.
	 * 
	 * @param results
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private <T, A> TimeSeriesCollection<T, A> convertToTimeSeriesCollection(final List<Object[]> results,
			final Class<T> aggregateGeography, final AnalysisParameters analysisParameters,
			final Class<A> aggregateAttribute) {

		enableNestedLoops(entityManager);
		
		final TimeSeriesCollection<T, A> collection = new TimeSeriesCollection(analysisParameters.getPeriod(),
				analysisParameters.getStartDate(), analysisParameters.getEndDate());

		if (results != null) {

			for (Object[] obj : results) {

				if (!(obj.length == 4)) {
					throw new IllegalArgumentException("Array must contain four items (aggregate, map, date, count)");
				}

				// Check that all of our types are correct before we do anything
				// at all.
				Validate.notNull(obj[0]);
				Validate.isTrue(((obj[0] instanceof Long) || (obj[0].getClass().equals(aggregateGeography))));
				Validate.notNull(obj[1]);

				Validate.isTrue((obj[1] instanceof Long) || (aggregateAttribute.isAssignableFrom(obj[1].getClass())),
						"Object must be an instance of Long or Attribute!  Was: " + obj[1].getClass().getName());
				Validate.isTrue(obj[2] instanceof DateTime, "Expected DateTime, got: " + obj[2].getClass().getName());
				Validate.isTrue(obj[3] instanceof Long);

				// Aggregate first
				final T aggValue = (obj[0] instanceof Long) ? entityManager.getReference(aggregateGeography, obj[0])
						: aggregateGeography.cast(obj[0]);

				// Classification next.
				final A cm = (obj[1] instanceof Long) ? entityManager.getReference(aggregateAttribute, obj[1])
						: aggregateAttribute.cast(obj[1]);

				// Add to the collection
				collection.addTimeSeriesEntry(aggValue, cm, new TimeSeriesEntry((DateTime) obj[2], ((Long) obj[3])
						.doubleValue()));

				evict(obj);
			}
		}

		return collection;
	}

	/**
	 * Builds a Map of aggregated TimeSeries objects.
	 * 
	 * @param results
	 * @return
	 */
	private <T> Map<T, TimeSeries> convertToTimeSeriesMap(final List<Object[]> results, final Class<T> aggregate,
			final AnalysisParameters analysisParameters) {

		enableNestedLoops(entityManager);
		
		final Map<T, TimeSeries> collection = new TreeMap<T, TimeSeries>();

		if (results != null) {

			for (Object[] obj : results) {

				Validate.isTrue((obj.length == 3), "Array must contain three items (aggregate, date, count)");

				// Check that all of our types are correct before we do anything
				// at all.
				Validate.notNull(obj[0]);
				Validate.isTrue(((obj[0] instanceof Long) || (obj[0].getClass().equals(aggregate))));
				Validate.isTrue(obj[1] instanceof DateTime, "Expected DateTime, got: " + obj[2].getClass().getName());
				Validate.isTrue(obj[2] instanceof Long);

				// Aggregate first (we have to get an entity reference if we've
				// got a Long id).
				final T aggValue = (obj[0] instanceof Long) ? entityManager.getReference(aggregate, obj[0]) : aggregate.cast(obj[0]);

				if (!collection.containsKey(aggValue)) {
					collection.put(aggValue, new TimeSeries(analysisParameters.getPeriod(), analysisParameters
							.getStartDate(), analysisParameters.getEndDate()));
				}

				// Add to the collection
				collection.get(aggValue).add(new TimeSeriesEntry((DateTime) obj[1], ((Long) obj[2]).doubleValue()));
				evict(obj);
			}
		}

		return collection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.provider.ProviderRepository#getProviderType(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public DataType getDataType(final String name) {

		Validate.notNull(name);

		DataType dt = null;

		final List<DataType> dts = namedQuery(entityManager, "getDataType").setParameter("name", name).getResultList();
		if (dts.size() == 1) {
			dt = dts.get(0);
		}

		return dt;
	}
	

	/**
	 * @param analysisQueries
	 *            the analysisQueries to set
	 */
	@Required
	public void setAnalysisQueries(AnalysisQueries analysisQueries) {
		this.analysisQueries = analysisQueries;
	}

	
}
