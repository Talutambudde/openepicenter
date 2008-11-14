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

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:StatisticsCounter.java 136 2007-05-17 17:13:24Z steve.kondik $
 */
public class StatisticsCounter {

	private long timestamp = System.currentTimeMillis();

	private long lastAlerted = 0;

	private final List<StatisticsEntry> entries = new ArrayList<StatisticsEntry>();


	/**
	 * @return the entries
	 */
	public List<StatisticsEntry> getEntries() {
		return entries;
	}

	/**
	 * @return the lastAlerted
	 */
	public long getLastAlerted() {
		return lastAlerted;
	}

	/**
	 * @param lastAlerted
	 *            the lastAlerted to set
	 */
	public void setLastAlerted(long lastAlerted) {
		this.lastAlerted = lastAlerted;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 *            the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
