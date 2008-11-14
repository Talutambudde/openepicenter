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
package com.hmsinc.epicenter.model.surveillance;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.hmsinc.ts4j.TimeSeries;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: SurveillanceResult.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@XmlRootElement(name = "surveillance-result", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "SurveillanceResult", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.FIELD)
public class SurveillanceResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3997556749607253775L;

	private Map<SurveillanceResultType, TimeSeries> results = new HashMap<SurveillanceResultType, TimeSeries>();

	/**
	 * @return the result
	 */
	public Map<SurveillanceResultType, TimeSeries> getResults() {
		return results;
	}

}
