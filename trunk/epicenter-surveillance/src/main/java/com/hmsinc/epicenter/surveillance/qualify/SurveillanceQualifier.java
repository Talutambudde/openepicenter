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

import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.surveillance.SurveillanceResult;
import com.hmsinc.ts4j.TimeSeries;

/**
 * @author shade
 * @version $Id: SurveillanceQualifier.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public interface SurveillanceQualifier {

	public boolean isQualifiedForAnalysis(final TimeSeries rawData, final DateTime analysisTime);
	
	public boolean isQualifiedForEvent(final SurveillanceResult result, final DateTime analysisTime);

}
