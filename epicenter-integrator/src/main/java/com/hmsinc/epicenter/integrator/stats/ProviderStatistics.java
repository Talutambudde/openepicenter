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
package com.hmsinc.epicenter.integrator.stats;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds statistics data.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ProviderStatistics.java 136 2007-05-17 17:13:24Z steve.kondik $
 */
public class ProviderStatistics {

	private final Map<StatisticsService.StatsType, StatisticsCounter> stats = new HashMap<StatisticsService.StatsType, StatisticsCounter>();

	/**
	 * @return the stats
	 */
	public StatisticsCounter getStats(StatisticsService.StatsType statsType) {
		if (!stats.containsKey(statsType)) {
			stats.put(statsType, new StatisticsCounter());
		}
		return stats.get(statsType);
	}
}
