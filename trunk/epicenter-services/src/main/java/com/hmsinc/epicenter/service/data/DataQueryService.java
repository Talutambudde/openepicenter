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
package com.hmsinc.epicenter.service.data;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.normalization.Normalizer;
import com.hmsinc.ts4j.analysis.normalization.PopulationRateNormalizer;
import com.hmsinc.ts4j.analysis.util.AnalysisUtils;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.DataConditioning;
import com.hmsinc.epicenter.model.analysis.DataRepresentation;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;
import com.hmsinc.ts4j.TimeSeriesNode;

/**
 * @author shade
 * @version $Id: DataQueryService.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@Service
public class DataQueryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private AnalysisRepository analysisRepository;

	@Resource
	private GeographyRepository geographyRepository;
	
	@Resource
	private Normalizer waveletDayOfWeekNormalizer;

	@Resource
	private PopulationRateNormalizer populationRateNormalizer;

	/**
	 * @param <G>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	public <G extends Geography> TimeSeriesCollection<G, Classification> query(
			final AnalysisParameters analysisParameters, final Class<G> aggregateGeographyType) {
		return query(analysisParameters, aggregateGeographyType, Classification.class);
	}

	/**
	 * @param <A>
	 * @param <G>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @param aggregateAttributeType
	 * @return
	 */
	@Transactional(readOnly = true)
	public <A extends QueryableAttribute, G extends Geography> TimeSeriesCollection<G, A> query(
			final AnalysisParameters analysisParameters, final Class<G> aggregateGeographyType,
			final Class<A> aggregateAttributeType) {

		Validate.notNull(analysisParameters, "Parameters must be specified.");
		Validate.notNull(aggregateGeographyType, "Geography type must be specified.");
		Validate.notNull(aggregateAttributeType, "Attribute type must be specified.");

		analysisParameters.validate();

		logger.trace("Parameters: {}  Geography type: {}  Attribute type: {}", new Object[] { analysisParameters,
				aggregateGeographyType, aggregateAttributeType });

		final TimeSeriesCollection<G, A> raw = analysisRepository.getAggregatedCounts(analysisParameters,
				aggregateGeographyType, aggregateAttributeType);

		// Handle data representation
		final TimeSeriesCollection<G, A> result = doDataRepresentation(analysisParameters, raw, aggregateGeographyType);

		// Handle data conditioning
		final TimeSeriesCollection<G, A> corrected = doDataConditioning(analysisParameters, result);

		return corrected;
	}

	/**
	 * @param <G>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	@Transactional(readOnly = true)
	public <G extends Geography> Map<G, TimeSeries> queryCombined(final AnalysisParameters analysisParameters,
			final Class<G> aggregateGeographyType) {

		Validate.notNull(analysisParameters, "Parameters must be specified.");
		Validate.notNull(aggregateGeographyType, "Geography type must be specified.");

		analysisParameters.validate();

		logger.debug("Parameters: {}  Geography type: {}", analysisParameters, aggregateGeographyType);

		final Map<G, TimeSeries> raw;
		if (analysisParameters.getClassifications() != null && analysisParameters.getClassifications().size() > 0) {
			raw = analysisRepository.getCombinedCounts(analysisParameters, aggregateGeographyType);
		} else {
			raw = analysisRepository.getTotalCounts(analysisParameters, aggregateGeographyType);
		}
		
		// Handle data representation
		final Map<G, TimeSeries> result = doDataRepresentation(analysisParameters, raw, aggregateGeographyType);

		// Handle data conditioning
		final Map<G, TimeSeries> corrected = doDataConditioning(analysisParameters, result);

		return corrected;
	}

	/**
	 * @param <G>
	 * @param analysisParameters
	 * @param aggregateGeographyType
	 * @return
	 */
	public <G extends Geography> Map<G, TimeSeries> queryTotals(final AnalysisParameters analysisParameters, final Class<G> aggregateGeographyType) {
		
		Validate.notNull(analysisParameters, "Parameters must be specified.");
		Validate.notNull(aggregateGeographyType, "Geography type must be specified.");

		analysisParameters.validate();

		logger.debug("Parameters: {}  Geography type: {}", analysisParameters, aggregateGeographyType);

		final Map<G, TimeSeries> raw = analysisRepository.getTotalCounts(analysisParameters, aggregateGeographyType);
		
		// Handle data representation
		final Map<G, TimeSeries> result = DataRepresentation.PERCENTAGE_OF_TOTAL.equals(analysisParameters.getDataRepresentation()) ? raw : doDataRepresentation(analysisParameters, raw, aggregateGeographyType);

		// Handle data conditioning
		final Map<G, TimeSeries> corrected = doDataConditioning(analysisParameters, result);

		return corrected;
	}
	
	/**
	 * @param <A>
	 * @param <G>
	 * @param analysisParameters
	 * @param raw
	 * @param aggregateGeographyType
	 * @return
	 */
	private <A extends QueryableAttribute, G extends Geography> TimeSeriesCollection<G, A> doDataRepresentation(
			final AnalysisParameters analysisParameters, final TimeSeriesCollection<G, A> raw,
			final Class<G> aggregateGeographyType) {

		final TimeSeriesCollection<G, A> result;

		// Handle data representation
		if (DataRepresentation.PERCENTAGE_OF_TOTAL.equals(analysisParameters.getDataRepresentation())) {

			final Map<G, TimeSeries> totals = analysisRepository.getTotalCounts(analysisParameters,
					aggregateGeographyType);
			result = AnalysisUtils.normalize(raw, totals);

		} else if (DataRepresentation.POPULATION_RATE.equals(analysisParameters.getDataRepresentation())) {

			result = new TimeSeriesCollection<G, A>(raw.getPeriod(), raw.getStart(), raw.getEnd());
			for (TimeSeriesNode<G, A> node : raw.getTimeSeriesNodes()) {
				final TimeSeries popn = populationRateNormalizer.normalize(node.getTimeSeries(), node.getPrimaryIndex()
						.getPopulation().intValue());
				result.addTimeSeries(node.getPrimaryIndex(), node.getSecondaryIndex(), popn);
			}

		} else {

			result = raw;
		}

		return result;
	}

	/**
	 * @param <G>
	 * @param analysisParameters
	 * @param raw
	 * @param aggregateGeographyType
	 * @return
	 */
	private <G extends Geography> Map<G, TimeSeries> doDataRepresentation(final AnalysisParameters analysisParameters,
			final Map<G, TimeSeries> raw, final Class<G> aggregateGeographyType) {

		final Map<G, TimeSeries> result;

		// Handle data representation
		if (DataRepresentation.PERCENTAGE_OF_TOTAL.equals(analysisParameters.getDataRepresentation())) {

			final Map<G, TimeSeries> totals = analysisRepository.getTotalCounts(analysisParameters,
					aggregateGeographyType);
			result = AnalysisUtils.normalize(raw, totals);

		} else if (DataRepresentation.POPULATION_RATE.equals(analysisParameters.getDataRepresentation())) {

			result = new HashMap<G, TimeSeries>();
			for (Map.Entry<G, TimeSeries> node : raw.entrySet()) {
				
				final Long population = getPopulationForGeography(node.getKey());
								
				final TimeSeries popn = populationRateNormalizer.normalize(node.getValue(), population.intValue());
				result.put(node.getKey(), popn);
			}

		} else {

			result = raw;
		}

		return result;
	}

	/**
	 * @param region
	 * @return
	 */
	private Long getPopulationForGeography(final Geography region) {
		
		Validate.notNull(region, "Region must be specified");
		
		final Long population;
		
		if (region.getPopulation() == null) {
			population = geographyRepository.inferPopulation(region);
		} else {
			population = region.getPopulation();
		}
		
		return population;
	}
	
	/**
	 * @param analysisParameters
	 * @param ts
	 * @return
	 */
	private TimeSeries doDataConditioning(final AnalysisParameters analysisParameters, final TimeSeries ts) {

		final TimeSeries conditioned;
		if (DataConditioning.DAY_OF_WEEK.equals(analysisParameters.getDataConditioning())) {
			conditioned = waveletDayOfWeekNormalizer.normalize(null, ts);
		} else {
			conditioned = ts;
		}
		return conditioned;
	}

	/**
	 * @param <G>
	 * @param analysisParameters
	 * @param result
	 * @return
	 */
	private <G extends Geography> Map<G, TimeSeries> doDataConditioning(final AnalysisParameters analysisParameters,
			final Map<G, TimeSeries> result) {

		final Map<G, TimeSeries> conditioned;
		if (DataConditioning.NONE.equals(analysisParameters.getDataConditioning())) {
			conditioned = result;
		} else {
			conditioned = new HashMap<G, TimeSeries>();
			for (Map.Entry<G, TimeSeries> entry : result.entrySet()) {
				conditioned.put(entry.getKey(), doDataConditioning(analysisParameters, entry.getValue()));
			}
		}
		return conditioned;
	}

	/**
	 * @param <A>
	 * @param <G>
	 * @param analysisParameters
	 * @param tsc
	 * @return
	 */
	private <A extends QueryableAttribute, G extends Geography> TimeSeriesCollection<G, A> doDataConditioning(
			final AnalysisParameters analysisParameters, final TimeSeriesCollection<G, A> tsc) {

		// Handle data conditioning
		final TimeSeriesCollection<G, A> conditioned;
		if (DataConditioning.NONE.equals(analysisParameters.getDataConditioning())) {
			conditioned = tsc;
		} else {
			conditioned = new TimeSeriesCollection<G, A>(tsc.getPeriod(), tsc.getStart(), tsc.getEnd());
			for (TimeSeriesNode<G, A> node : tsc.getTimeSeriesNodes()) {
				conditioned.addTimeSeries(node.getPrimaryIndex(), node.getSecondaryIndex(), doDataConditioning(
					analysisParameters, node.getTimeSeries()));
			}
		}

		return conditioned;
	}
}
