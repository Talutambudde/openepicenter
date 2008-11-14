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
package com.hmsinc.epicenter.model.analysis;

import java.util.List;
import java.util.Map;

import com.hmsinc.epicenter.model.Repository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.ClassificationTarget;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;

public interface AnalysisRepository extends Repository<AnalysisObject, Long> {

	/**
	 * Get the ClassificationTargets that match the given Interaction.
	 * 
	 * @param clazz
	 * @param interactionType
	 * @return
	 */
	public List<ClassificationTarget> getClassifiersForInteraction(final Interaction interaction);

	/**
	 * Gets a Classifier using it's name.
	 * 
	 * @param name
	 * @return
	 */
	public Classifier getClassifierByName(final String name);

	/**
	 * Get a specific Classification (category).
	 * 
	 * @param classifier
	 * @param category
	 * @return
	 */
	public List<Classification> getClassifications(final Classifier classifier, final List<String> categories);

	/**
	 * Gets a list of all enabled classifiers.
	 * 
	 * @return
	 */
	public List<Classifier> getClassifiers();
	
	/**
	 * Returns aggregated counts/timeseries using the specified attributes and
	 * Geography.
	 * 
	 * @param <T>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	public <T extends Geography> TimeSeriesCollection<T, Classification> getClassifiedCounts(
			final AnalysisParameters analysisParameters, final Class<T> aggregateGeographyType);

	/**
	 * @param <T>
	 * @param <A>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @param aggregateAttributeType
	 * @return
	 */
	public <T extends Geography, A extends QueryableAttribute> TimeSeriesCollection<T, A> getAggregatedCounts(
			final AnalysisParameters analysisParameters, final Class<T> aggregateGeographyType,
			final Class<A> aggregateAttributeType);

	/**
	 * @param <T>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	public <T extends Geography> Map<T, TimeSeries> getCombinedCounts(final AnalysisParameters analysisParameters,
			final Class<T> aggregateGeographyType);

	/**
	 * Returns the total counts, grouped by Geography.
	 * 
	 * @param <T>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	public <T extends Geography> Map<T, TimeSeries> getTotalCounts(final AnalysisParameters analysisParameters,
			final Class<T> aggregateGeographyType);

	/**
	 * Convenience method to get normalized counts.
	 * 
	 * @param <T>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	public <T extends Geography> TimeSeriesCollection<T, Classification> getNormalizedCounts(
			final AnalysisParameters analysisParameters, final Class<T> aggregateGeographyType);

	/**
	 * Gets all Interactions for the specified parameters.
	 * 
	 * @param analysisParameters
	 * @return
	 */
	public List<? extends Interaction> getCases(final AnalysisParameters analysisParameters, Long offset, Long numRows);

	/**
	 * Gets the count of cases for the specified parameters.
	 * 
	 * @param analysisParameters
	 * @return
	 */
	public Long getCasesCount(final AnalysisParameters analysisParameters);
	
	/**
	 * Gets a DataType by name.
	 * 
	 * @param dataType
	 * @return
	 */
	public DataType getDataType(final String dataType);
	

}