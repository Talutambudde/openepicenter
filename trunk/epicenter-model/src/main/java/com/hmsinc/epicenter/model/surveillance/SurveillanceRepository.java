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

import java.util.List;

import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.Repository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: SurveillanceRepository.java 162 2007-09-14 01:50:08Z
 *          steve.kondik $
 */
public interface SurveillanceRepository extends Repository<SurveillanceObject, Long> {

	public List<Anomaly> getAnomalies(final DateTime startDate, final DateTime endDate, final boolean includeAll,
			final Geometry filter, final Geometry excludeFacilityEventsFilter, Integer offset, Integer numRows);

	public Integer getAnomalyCount(final DateTime startDate, final DateTime endDate, final boolean includeAll,
			final Geometry filter, final Geometry excludeFacilityEventsFilter);

	public DateTime getDateOfOldestAnomaly(final Geometry filter, final Geometry excludeFacilityEventsFilter);

	public Anomaly getLatestAnomaly(final Geography geography, final Classification classification,
			final SurveillanceTask task, final SurveillanceMethod method, final SurveillanceSet set,
			final DateTime maxTime);

}
