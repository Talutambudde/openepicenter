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

import com.hmsinc.epicenter.integrator.stats.StatisticsService.StatsType;
import com.hmsinc.mergence.model.DataSource;

/**
 * Holder for a statistics entry.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:StatisticsEntry.java 136 2007-05-17 17:13:24Z steve.kondik $
 */
public class StatisticsEntry {

	private final DataSource dataSource;

	private final StatsType statsType;

	private final Long messageId;

	private final String additionalInfo;

	public StatisticsEntry(DataSource dataSource, StatsType statsType, Long messageId, String additionalInfo) {
		this.dataSource = dataSource;
		this.statsType = statsType;
		this.messageId = messageId;
		this.additionalInfo = additionalInfo;
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @return the statsType
	 */
	public StatsType getStatsType() {
		return statsType;
	}

	/**
	 * @return the messageId
	 */
	public Long getMessageId() {
		return messageId;
	}

	/**
	 * @return the additionalInfo
	 */
	public String getAdditionalInfo() {
		return additionalInfo;
	}

}
