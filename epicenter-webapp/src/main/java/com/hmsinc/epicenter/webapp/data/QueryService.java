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
package com.hmsinc.epicenter.webapp.data;

import static com.hmsinc.epicenter.util.FormatUtils.formatDateTime;

import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.Duration;
import com.hmsinc.ts4j.analysis.univariate.DescriptiveUnivariateAnalyzer;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.epicenter.service.data.DataQueryService;
import com.hmsinc.epicenter.service.discovery.AnalyzerDiscoveryService;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesEntry;
import com.hmsinc.epicenter.util.DateTimeUtils;

/**
 * @author shade
 * @version $Id: QueryService.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@Service
public class QueryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private DataQueryService dataQueryService;
	
	@Resource
	private AnalyzerDiscoveryService analyzerDiscoveryService;

	/**
	 * @param params
	 * @param algorithmName
	 * @param analyzerProperties
	 * @return
	 */
	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public TimeSeries queryForTimeSeries(final AnalysisParameters analysisParameters, String algorithmName, final Properties analyzerProperties) {
		
		DateTime start = analysisParameters.getStartDate();
		DescriptiveUnivariateAnalyzer algorithmPipeline = null;
		
		final String algorithm = StringUtils.trimToNull(algorithmName);
		
		if (algorithm != null) {

			Validate.isTrue(analyzerDiscoveryService.getUnivariateAnalyzers().contains(algorithm), "Invalid algorithm");
			algorithmPipeline = analyzerDiscoveryService.getUnivariateAnalyzer(algorithm);

			final Duration trainingPeriod;
			if (analyzerProperties == null) {
				trainingPeriod = algorithmPipeline.getTrainingPeriod();
			} else {
				trainingPeriod = algorithmPipeline.getTrainingPeriod(analyzerProperties);
			}

			Validate.isTrue(trainingPeriod.getLength() < 365, "Training window too large [was: "
					+ trainingPeriod.getLength() + "]");

			DateTime offsetStart = trainingPeriod.subtractFrom(start);
			Validate.isTrue(offsetStart.isBefore(analysisParameters.getEndDate()), "Start date must be before end date. [start: "
					+ formatDateTime(offsetStart) + " end: " + formatDateTime(analysisParameters.getEndDate()) + "]");
			analysisParameters.setStartDate(offsetStart);
		}

		logger.trace("Generating timeseries for: {}  ", analysisParameters);
		
		final Class<? extends Geography> aggregateGeographyType = (Class<? extends Geography>)ModelUtils.getRealClass(analysisParameters.getContainer());
		final Map<? extends Geography, TimeSeries> tsc = dataQueryService.queryCombined(analysisParameters, aggregateGeographyType);
		
		Validate.notNull(tsc, "Unable to get timeseries data!");
		
		TimeSeries ret = null;
		
		if (tsc.containsKey(analysisParameters.getContainer())) {

			ret = tsc.get(analysisParameters.getContainer());

			if (algorithmPipeline != null && algorithmPipeline.getSupportedPeriods().contains(ret.getPeriod())) {
				
				if (analyzerProperties == null) {
					logger.debug("Processing timeseries using: {}", algorithmPipeline);
					ret = algorithmPipeline.process(ret);
				} else {
					logger.debug("Processing timeseries using: {}  Parameters: {}", algorithmPipeline, analyzerProperties);
					ret = algorithmPipeline.process(ret, analyzerProperties);
				}
			}
		}
		
		analysisParameters.setStartDate(start);
		
		logger.trace("TimeSeries: {}", ret);
		
		return ret;
	}
	
	/**
	 * @param anomaly
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <G extends Geography> double getCurrentValueForAnomaly(final Anomaly anomaly) {
		
		final double currentValue;
		
		final AnalysisParameters p = anomaly.getAnalysisParameters();
		p.setEndDate(new DateTime());
		p.setStartDate(p.getEndDate().minusDays(3));
		
		final Class<G> geoClass = (Class<G>)ModelUtils.getRealClass(anomaly.getGeography());

		final Map<G, TimeSeries> ts = dataQueryService.queryCombined(p, geoClass);
				
		if (ts.containsKey(anomaly.getGeography())) {
			final TimeSeriesEntry current = ts.get(anomaly.getGeography()).get(p.getEndDate());
			currentValue = current.getValue();
		} else {
			currentValue = 0.0;
		}
		
		return currentValue;
	}
	
	/**
	 * @param <G>
	 * @param anomaly
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(readOnly=true)
	public <G extends Geography> TimeSeries getCurrentTimeSeriesForAnomaly(final Anomaly anomaly, final SurveillanceResultType type) {
		
		TimeSeries currentValue = null;
		
		final AnalysisParameters p = anomaly.getAnalysisParameters();
		p.setDataRepresentation(type.getDataRepresentation());
		
		// We'll plot 2 weeks before the anomaly
		p.setStartDate(anomaly.getAnalysisTimestamp().minusDays(14));
		
		// We want to show UP TO 7 extra days, if possible.
		final int extraDays = Math.min(7, DateTimeUtils.deltaDays(p.getEndDate(), new DateTime()));
		if (extraDays > 0) {
			p.setEndDate(p.getEndDate().plusDays(extraDays));
		}
		
		final Class<G> geoClass = (Class<G>)ModelUtils.getRealClass(anomaly.getGeography());
		final Map<G, TimeSeries> ts = dataQueryService.queryCombined(p, geoClass);
			
		if (ts.containsKey(anomaly.getGeography())) {
			
			final TimeSeries cur = ts.get(anomaly.getGeography());
			currentValue = cur.after(cur.getStart().plusDays(1));		
		}
				
		return currentValue;
	}
	
}
