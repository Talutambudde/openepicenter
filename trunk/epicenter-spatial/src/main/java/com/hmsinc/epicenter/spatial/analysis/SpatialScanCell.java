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
package com.hmsinc.epicenter.spatial.analysis;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * SpatialScanCell.java
 *   
 * A cell objects composing a SpatialScanGrid. Includes a baseline, count, and
 * identifier value for the cell.
 * 
 * @author C. A. Cois
 * 
 * */

public class SpatialScanCell {
	
	private double count;
	private Object identifier;
	private double baseline;
	
	public SpatialScanCell(double cnt, double base, Object ident) {
		count = cnt;
		baseline = base;
		identifier = ident;
	}
	
	public double getCount() {
		return count;
	}
	
	public double getBaseline() {
		return baseline;
	}
	
	public Object getIdentifier() {
		return identifier;
	}
	
	public void setCount(double c) {
		count = c;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}