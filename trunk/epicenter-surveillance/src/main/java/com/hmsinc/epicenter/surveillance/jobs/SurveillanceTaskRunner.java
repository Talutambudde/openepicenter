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
package com.hmsinc.epicenter.surveillance.jobs;

import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.Duration;
import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.univariate.DescriptiveUnivariateAnalyzer;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.DataConditioning;
import com.hmsinc.epicenter.model.analysis.DataRepresentation;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.surveillance.SurveillanceMethod;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResult;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.epicenter.model.surveillance.SurveillanceSet;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;
import com.hmsinc.epicenter.service.data.DataQueryService;
import com.hmsinc.epicenter.service.discovery.AnalyzerDiscoveryService;
import com.hmsinc.epicenter.surveillance.event.EventService;
import com.hmsinc.epicenter.surveillance.qualify.SurveillanceQualifier;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;
import com.hmsinc.ts4j.TimeSeriesNode;

/**
 * Runs SurveillanceTasks.
 * 
 * @author shade
 * @author Olek Poplavsky
 * @version $Id: SurveillanceTaskRunner.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
public class SurveillanceTaskRunner {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private DataQueryService dataQueryService;

	@Resource
	private AnalyzerDiscoveryService analyzerDiscoveryService;

	@Resource
	private EventService eventService;

	private Map<String, SurveillanceQualifier> surveillanceQualifiers;
	
	private String defaultSurveillanceQualifier;
	
	/**
	 * @param <T>
	 * @param task
	 */
	@Transactional
	public void execute(final SurveillanceTask task) {

		final long startTime = new DateTime().getMillis();

		logger.info("Surveillance job {} is running... ({})", task.getId(), task.getDescription());

		logger.debug("SurveillanceTask: {}", task);

		Validate.notEmpty(task.getMethods(), "No surveillance methods defined for task: " + task.getId());
		Validate.notEmpty(task.getSets(), "No surveillance sets defined for task: " + task.getId());
		
		// How many days of data do we need?
		final Duration trainingPeriod = getTrainingPeriodForTask(task);
		logger.trace("Training period is: {}", trainingPeriod);

		// Subtract the delay from the endDate..
		DateTime endDate = new DateTime().withMillisOfSecond(0).minusSeconds(task.getDelay());

		// Set the start date to the end date minus the training
		// period and go back 1 month in order to save some historical
		// data..
		DateTime startDate = trainingPeriod.subtractFrom(endDate).minusMonths(1);
		
		
		// Loop thru the defined surveillance sets
		for (SurveillanceSet set : task.getSets()) {

			logger.trace("Using surveillance set: {}", set);

			final AnalysisParameters analysisParameters = getAnalysisParameters(startDate, endDate, task, set);

			final TimeSeriesCollection<Geography, Classification> data = dataQueryService.query(analysisParameters, task.getAggregateType().getGeoClass());
					
			analyze(data, endDate, task, set);

		}
		
		final long runtimeMillis = new DateTime().getMillis() - startTime;
		logger.debug("Surveillance task {} complete.  Running time was {}ms", task.getId(), runtimeMillis);

	}

	/**
	 * @param <T>
	 * @param tsGrid
	 * @param tsByGeography
	 * @param analysisTime
	 * @param task
	 */
	@Transactional
	private void analyze(TimeSeriesCollection<Geography, Classification> data,  final DateTime analysisTime,
			final SurveillanceTask task, final SurveillanceSet set) {

		final String qualifierName = task.getQualifier() == null ? defaultSurveillanceQualifier : task.getQualifier();
		Validate.notNull(qualifierName, "Qualifier was null and no default qualifier was set.");
		Validate.isTrue(surveillanceQualifiers.containsKey(qualifierName), "Unknown surveillance qualifier: " + qualifierName);
		
		final SurveillanceQualifier qualifier = surveillanceQualifiers.get(qualifierName);
		Validate.notNull(qualifier, "Invalid surveillance qualifier: " + qualifierName);
		
		logger.debug("Using surveillance qualifier: {}", qualifier.getClass().getName());
		
		for (TimeSeriesNode<Geography, Classification> node : data.getTimeSeriesNodes()) {
			
			final TimeSeries ts = node.getTimeSeries();
			
			logger.debug("Analyzing geography: {}, category: {}, task: {}, target time: {}", new Object[] {
				node.getPrimaryIndex().getDisplayName(), node.getSecondaryIndex().getCategory(),
				task.getId(), analysisTime });

			if (qualifier.isQualifiedForAnalysis(ts, analysisTime)) {
				
				for (final SurveillanceMethod method : task.getMethods()) {
					analyze(ts, analysisTime, task, set, method, qualifier, node.getPrimaryIndex(), node.getSecondaryIndex());
				}
				
			} else {
				logger.trace("Data not qualified for analysis");
			}
		}
	}

	/**
	 * @param <T>
	 * @param method
	 * @param task
	 * @param tsNode
	 * @param total
	 * @param analysisTime
	 */
	private void analyze(final TimeSeries data, final DateTime analysisTime, final SurveillanceTask task, final SurveillanceSet set,
			final SurveillanceMethod method, final SurveillanceQualifier qualifier, final Geography geography, final Classification classification) {

		logger.trace("Executing method: {}", method);
		
		final Set<String> properties = data.getAllProperties();
		final SurveillanceResult result = new SurveillanceResult();
		
		final DescriptiveUnivariateAnalyzer analyzer = analyzerDiscoveryService.getUnivariateAnalyzer(method.getName());
		
		// If we have a RAW result type, then some form of data conditioning has been applied.
		if (properties.contains(ResultType.RAW)) {
			
			result.getResults().put(SurveillanceResultType.NORMALIZED, analyzer.process(data, method.getParameters()));
			result.getResults().put(SurveillanceResultType.ACTUAL, analyzer.process(data.newTimeSeriesFromProperty(ResultType.RAW), method.getParameters()));
			properties.remove(ResultType.RAW);
			
		} else {
			
			result.getResults().put(SurveillanceResultType.ACTUAL, analyzer.process(data, method.getParameters()));
		}
		
		// Also analyze TOTAL if we have it.
		if (properties.contains(ResultType.TOTAL)) {
			result.getResults().put(SurveillanceResultType.TOTAL, analyzer.process(data.newTimeSeriesFromProperty(ResultType.TOTAL), method.getParameters()));
		}

		
		// Let the EventService take care of the rest.
		if (qualifier.isQualifiedForEvent(result, analysisTime)) {
			eventService.handleEvent(analysisTime, task, method, set, geography, classification, result, analysisTime);
		}
	}

	/**
	 * @param startDate
	 * @param endDate
	 * @param task
	 * @param set
	 * @return
	 */
	@Transactional(readOnly = true)
	private AnalysisParameters getAnalysisParameters(final DateTime startDate, final DateTime endDate, final SurveillanceTask task, final SurveillanceSet set) {
				
		final AnalysisParameters analysisParameters = new AnalysisParameters(startDate, endDate, task
				.getGeography(), task.getLocation(), set.getDatatype(), set.getClassifications(), set
				.getAttributes());
		
		if (task.getRepresentation() == null) {
			analysisParameters.setDataRepresentation(DataRepresentation.PERCENTAGE_OF_TOTAL);
		} else {
			analysisParameters.setDataRepresentation(task.getRepresentation());
		}
		
		if (task.getConditioning() == null) {
			analysisParameters.setDataConditioning(DataConditioning.NONE);
		} else {
			analysisParameters.setDataConditioning(task.getConditioning());
		}
		
		logger.trace("Parameters: {}", analysisParameters);
		
		return analysisParameters;
		
	}
	
	/**
	 * @param task
	 * @return
	 */
	@Transactional(readOnly = true)
	private Duration getTrainingPeriodForTask(final SurveillanceTask task) {

		// How many days of data do we need?
		Duration trainingPeriod = null;
		for (final SurveillanceMethod method : task.getMethods()) {

			Object[] params = { method.getDescription() };
			logger.trace("SurveillanceMethod: {}", params);

			// TODO: Only working for univariatePipeline right now.
			final DescriptiveUnivariateAnalyzer up = analyzerDiscoveryService.getUnivariateAnalyzer(method.getName());

			Validate.notNull(up, "Unknown surveillance method: " + method.getName());

			Duration currentDuration = up.getTrainingPeriod();
			Validate.notNull(up, "Training period should be defined for algorithm " + method.getName()
					+ " algorithm object " + up);
			if (trainingPeriod == null) {
				trainingPeriod = currentDuration;
			} else {
				Validate.isTrue(trainingPeriod.getPeriod().equals(currentDuration.getPeriod()),
						"Training periods better have same periods (units)");
				if (trainingPeriod.getLength() < currentDuration.getLength()) {
					trainingPeriod = currentDuration;
				}
			}
		}

		Validate.notNull(trainingPeriod, "Training period was null, check algorithm configuration.");
		Validate.isTrue(trainingPeriod.getLength() > 0, "Training period was 0, check configuration of task!");

		return trainingPeriod.add(1);
	}

	/**
	 * @param surveillanceQualifiers
	 */
	@Required
	public void setSurveillanceQualifiers(Map<String, SurveillanceQualifier> surveillanceQualifiers) {
		this.surveillanceQualifiers = surveillanceQualifiers;
	}

	/**
	 * @param defaultSurveillanceQualifier
	 */
	@Required
	public void setDefaultSurveillanceQualifier(String defaultSurveillanceQualifier) {
		this.defaultSurveillanceQualifier = defaultSurveillanceQualifier;
	}

	
}
