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
package com.hmsinc.epicenter.webapp.chart;

import java.awt.Color;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.Dataset;

/**
 * @author shade
 *
 */
public class BarChart extends AbstractChart {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4426186927575175538L;
	
	private final DefaultCategoryDataset items = new DefaultCategoryDataset();

	/**
	 * @return the items
	 */
	@Override
	public Dataset getItems() {
		return items;
	}
	
	public void add(double value, String series, String category) {
		add(value, series, category, defaultColor);
	}

	public void add(double value, String series, String category, Color color) {
		items.addValue(value, series, category);
		colors.add(color);
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.webapp.chart.AbstractChart#getType()
	 */
	@Override
	public ChartType getType() {
		return ChartType.BAR;
	}

}
