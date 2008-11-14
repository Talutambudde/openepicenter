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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.apache.velocity.app.VelocityEngine;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import com.hmsinc.ts4j.analysis.Duration;
import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.univariate.DescriptiveUnivariateAnalyzer;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.service.discovery.AnalyzerDiscoveryService;
import com.hmsinc.ts4j.TimeSeries;

/**
 * Orchestrates building of dynamic map style.
 * 
 * @author Olek Poplavsky
 * @version $Id: StyleBuilder.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@Service
class StyleBuilder {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private FeatureGrader featureGrader;

	@Resource
	private MapCacheService mapCacheService;

	@Resource
	private AnalyzerDiscoveryService analyzerDiscoveryService;

	@Resource(name = "velocityEngine")
	private VelocityEngine velocityEngine;
	
	private static final String TEMPLATE_NAME = "/templates/map-style.vm";
	
	void build(OutputStream stream, StyleParameters styleParameters) throws IOException {
		
		final Map<String, Object> template = createTemplate(featureGrader.gradeFeatures(getProbabilities(styleParameters)));
		
		template.put("render_labels", styleParameters.isLabelFeatures());
		template.put("geography", styleParameters.getLayerName());
				
		final Writer writer = new BufferedWriter(new OutputStreamWriter(stream));	
		VelocityEngineUtils.mergeTemplate(velocityEngine, TEMPLATE_NAME, template, writer);
		writer.close();
	}

	private Map<Geography, Number> getProbabilities(StyleParameters styleParameters) {

		final Map<Geography, Number> featureToValueMap = new HashMap<Geography, Number>();
		
		final DescriptiveUnivariateAnalyzer analyzer = analyzerDiscoveryService.getUnivariateProbabilityAnalyzer(styleParameters.getAlgorithmName());

		Validate.notNull(analyzer, "Could not find analyzer " + styleParameters.getAlgorithmName());

		// We need to add 1 to the training period to get a single day of
		// results
		final Duration trainingPeriod = analyzer.getTrainingPeriod().add(1);
		final DateTime when = trainingPeriod.subtractFrom(styleParameters.getParameters().getEndDate());
		styleParameters.getParameters().setStartDate(when);

		final Map<? extends Geography, TimeSeries> counts = mapCacheService.getCachedCounts(styleParameters, styleParameters.getGeographyClass());

		for (Map.Entry<? extends Geography, TimeSeries> entry : counts.entrySet()) {

			final TimeSeries processed = analyzer.process(entry.getValue());
			Validate.notNull(processed, "Analysis result was null!");

			final double max = Math.max(0.0, processed.get(styleParameters.getParameters().getEndDate()).getDoubleProperty(ResultType.PROBABILITY));

			featureToValueMap.put(entry.getKey(), max * 100);
		}
		
		logger.trace("Probabilities: {}", featureToValueMap);
		
		return featureToValueMap;
	}

	private Map<String, Object> createTemplate(Map<String, List<Geography>> gradedFeatures) {

		final Map<String, Object> template = new HashMap<String, Object>();

		for (Map.Entry<String, List<Geography>> e : gradedFeatures.entrySet()) {
			String grade = e.getKey();
			List<Geography> features = e.getValue();
			template.put(grade, getGeographyIds(features));
			logGradedFeatures(grade, features);
		}

		return template;
	}
	
	private static List<String> getGeographyIds(List<Geography> geographies) {
		List<String> rtn = new ArrayList<String>();
		for (Geography geography : geographies) {
			rtn.add(geography.getId().toString());
		}
		return rtn;
	}
	
	private void logGradedFeatures(String grade, List<Geography> features) {

		if (logger.isTraceEnabled() && !features.isEmpty()) {
			StringBuilder featuresSB = new StringBuilder();
			String separator = "";
			for (Geography geography : features) {
				featuresSB.append(separator);
				featuresSB.append(geography.getName());
				featuresSB.append("(");
				featuresSB.append(geography.getId());
				featuresSB.append(")");
				separator = ", ";
			}
			logger.trace("Features group {}: {}", grade, featuresSB);
		}
	}
	
}
