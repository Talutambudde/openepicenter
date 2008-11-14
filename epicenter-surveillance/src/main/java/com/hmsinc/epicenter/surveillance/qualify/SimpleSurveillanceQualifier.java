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
package com.hmsinc.epicenter.surveillance.qualify;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResult;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesEntry;

/**
 * Basic qualifier which checks for the following conditions:
 * 
 * 1. Raw count is > 10 
 * 2. Actual threshold exceeded. 
 * 3. Normalized threshold exceeded (if available)
 * 
 * @author shade
 * @version $Id: SimpleSurveillanceQualifier.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
public class SimpleSurveillanceQualifier implements SurveillanceQualifier {

	private static final double MIN_RAW_COUNT = 10;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.surveillance.qualify.SurveillanceQualifier#isQualifiedForAnalysis(com.hmsinc.ts4j.TimeSeries,
	 *      org.joda.time.DateTime)
	 */
	public boolean isQualifiedForAnalysis(TimeSeries rawData, DateTime analysisTime) {

		final TimeSeriesEntry entry = rawData.get(analysisTime);

		final double value = entry.hasProperty(ResultType.RAW) ? entry.getDoubleProperty(ResultType.RAW) : entry.getValue();
		return value >= MIN_RAW_COUNT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.surveillance.qualify.SurveillanceQualifier#isQualifiedForEvent(com.hmsinc.epicenter.model.surveillance.SurveillanceResult,
	 *      org.joda.time.DateTime)
	 */
	public boolean isQualifiedForEvent(SurveillanceResult result, DateTime analysisTime) {

		final TimeSeries actual = result.getResults().get(SurveillanceResultType.ACTUAL);
		Validate.notNull(actual, "No actual value found in result!");

		boolean qualified = checkEntry(actual.get(analysisTime));

		// Also check the normalized value if we have it.
		if (qualified && result.getResults().containsKey(SurveillanceResultType.NORMALIZED)) {
			qualified = checkEntry(result.getResults().get(SurveillanceResultType.NORMALIZED).get(analysisTime));
		}

		logger.trace("Entry: {}  Qualified: {}", actual.get(analysisTime), qualified);
		
		return qualified;
	}

	/**
	 * Just verifies that we have exceeded the threshold.
	 * 
	 * @param entry
	 * @return
	 */
	private boolean checkEntry(final TimeSeriesEntry entry) {
		return entry.getValue() > entry.getDoubleProperty(ResultType.THRESHOLD);
	}
}
