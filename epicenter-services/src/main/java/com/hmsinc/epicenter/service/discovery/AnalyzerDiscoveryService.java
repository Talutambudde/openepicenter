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
package com.hmsinc.epicenter.service.discovery;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.univariate.DescriptiveUnivariateAnalyzer;

/**
 * @author shade
 * @version $Id: AnalyzerDiscoveryService.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@Service
public class AnalyzerDiscoveryService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, DescriptiveUnivariateAnalyzer> univariateAnalyzers = new TreeMap<String, DescriptiveUnivariateAnalyzer>();

	private final Map<String, DescriptiveUnivariateAnalyzer> univariateProbabilityAnalyzers = new TreeMap<String, DescriptiveUnivariateAnalyzer>();

	// Available algorithms. These should be configured in
	// epicenter-algorithms.xml
	@Autowired
	private DescriptiveUnivariateAnalyzer[] pipelines;

	@PostConstruct
	public void init() throws Exception {

		for (DescriptiveUnivariateAnalyzer algo : pipelines) {
			if (algo.getResultTypes().contains(ResultType.PROBABILITY)) {
				univariateProbabilityAnalyzers.put(algo.getName(), algo);
			} else if (algo.getResultTypes().contains(ResultType.THRESHOLD)) {
				univariateAnalyzers.put(algo.getName(), algo);
			}
		}
		logger.debug("Available analyzers: {}", univariateAnalyzers.keySet());

		logger.debug("Available probability analyzers: {}", univariateProbabilityAnalyzers.keySet());

	}

	/**
	 * @param analyzer
	 * @param date
	 * @return
	 */
	public DateTime getStartDateForAlgorithm(final String analyzer, final DateTime date) {

		Validate.notNull(date, "Date must be specified.");
		Validate.notNull(analyzer, "Algorithm name must be specified.");

		DescriptiveUnivariateAnalyzer a = null;
		if (univariateProbabilityAnalyzers.containsKey(analyzer)) {
			a = univariateProbabilityAnalyzers.get(analyzer);
		} else if (univariateAnalyzers.containsKey(analyzer)) {
			a = univariateAnalyzers.get(analyzer);
		} else {
			throw new IllegalArgumentException("Unknown algorithm: " + analyzer);
		}

		return a.getTrainingPeriod().add(1).subtractFrom(date);
	}

	/**
	 * @param analyzer
	 * @return
	 */
	public DescriptiveUnivariateAnalyzer getUnivariateAnalyzer(final String analyzer) {

		Validate.notNull(analyzer, "Analyzer name must be specified");
		Validate.isTrue(univariateAnalyzers.containsKey(analyzer), "Invalid analyzer: " + analyzer);
		return univariateAnalyzers.get(analyzer);
	}

	/**
	 * @param analyzer
	 * @return
	 */
	public DescriptiveUnivariateAnalyzer getUnivariateProbabilityAnalyzer(final String analyzer) {

		Validate.notNull(analyzer, "Analyzer name must be specified");
		Validate.isTrue(univariateProbabilityAnalyzers.containsKey(analyzer), "Invalid analyzer: " + analyzer);
		return univariateProbabilityAnalyzers.get(analyzer);
	}

	/**
	 * @return the univariateAnalyzers
	 */
	public Set<String> getUnivariateAnalyzers() {
		return univariateAnalyzers.keySet();
	}

	/**
	 * @return the univariateProbabilityAnalyzers
	 */
	public Set<String> getUnivariateProbabilityAnalyzers() {
		return univariateProbabilityAnalyzers.keySet();
	}

}
