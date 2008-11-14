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
 * Very dumb qualifier that does nothing but check if the actual value is above
 * the threshold.  Should only be used for testing.
 * 
 * @author shade
 * @version $Id: NullSurveillanceQualifier.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
public class NullSurveillanceQualifier implements SurveillanceQualifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.surveillance.qualify.SurveillanceQualifier#isQualifiedForAnalysis(com.hmsinc.ts4j.TimeSeries,
	 *      org.joda.time.DateTime)
	 */
	public boolean isQualifiedForAnalysis(TimeSeries rawData, DateTime analysisTime) {
		return true;
//		return rawData.getValue(analysisTime) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.surveillance.qualify.SurveillanceQualifier#isQualifiedForEvent(com.hmsinc.epicenter.model.surveillance.SurveillanceResult,
	 *      org.joda.time.DateTime)
	 */
	public boolean isQualifiedForEvent(SurveillanceResult result, DateTime analysisTime) {

		final TimeSeries actual = result.getResults().get(SurveillanceResultType.ACTUAL);
		Validate.notNull(actual, "No actual result found!");

		boolean ret = false;

		final TimeSeriesEntry entry = actual.get(analysisTime);
		if (entry.getValue() > entry.getDoubleProperty(ResultType.THRESHOLD)) {
			ret = true;
		}

		logger.trace("Entry: {}  Qualified: {}", entry, ret);
		
		return ret;
	}

}
