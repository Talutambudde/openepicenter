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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.Year;
import org.joda.time.DateTime;

import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesEntry;
import com.hmsinc.ts4j.TimeSeriesPeriod;

/**
 * Adapter for generating charts from a TimeSeries object.
 * 
 * @author shade
 * @version $Id: TimeSeriesChart.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public class TimeSeriesChart extends AbstractChart implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2037044552720276695L;

	private final org.jfree.data.time.TimeSeriesCollection items = new org.jfree.data.time.TimeSeriesCollection();
	
	private final List<org.jfree.data.time.TimeSeriesCollection> bands = new ArrayList<org.jfree.data.time.TimeSeriesCollection>();
	
	private final List<Color> bandColors = new ArrayList<Color>();
	
	private final List<Color> bandFillColors = new ArrayList<Color>();
	
	private ChartType type = ChartType.LINE;	
	
	public void add(final String name, final TimeSeries ts) {
		
		final org.jfree.data.time.TimeSeries jts = new org.jfree.data.time.TimeSeries(name, getChartPeriod(ts.getPeriod()));
		for (TimeSeriesEntry entry : ts) {
			jts.add(getEntryPeriod(ts.getPeriod(), entry.getTime()), entry.getValue());
		}
		
		items.addSeries(jts);
	}
	
	/**
	 * @param name
	 * @param ts
	 */
	public void add(final String name, final TimeSeries ts, final Color color, final LineStyle lineStyle) {

		add(name, ts);
		
		if (color != null) {
			colors.add(color);
		}
		
		if (lineStyle != null) {
			strokes.add(lineStyle.getStroke());
		}
	}
	
	/**
	 * @param name
	 * @param ts
	 * @param property1
	 * @param property2
	 * @param color
	 */
	public void addBand(final String name, final TimeSeries ts, final String property1, final String property2, final Color lineColor, final Color bandColor) {
		
		final org.jfree.data.time.TimeSeriesCollection bandItems = new org.jfree.data.time.TimeSeriesCollection();
		
		final org.jfree.data.time.TimeSeries jts1 = new org.jfree.data.time.TimeSeries(name, getChartPeriod(ts.getPeriod()));
		final org.jfree.data.time.TimeSeries jts2 = new org.jfree.data.time.TimeSeries(name, getChartPeriod(ts.getPeriod()));
		
		for (TimeSeriesEntry entry : ts) {
			jts1.add(getEntryPeriod(ts.getPeriod(), entry.getTime()), entry.getDoubleProperty(property1));
			jts2.add(getEntryPeriod(ts.getPeriod(), entry.getTime()), entry.getDoubleProperty(property2));
		}
		
		bandItems.addSeries(jts1);
		bandItems.addSeries(jts2);
		
		bands.add(bandItems);
		
		bandColors.add(lineColor);
		bandFillColors.add(bandColor);
	}
	
	/**
	 * @param name
	 * @param ts
	 */
	public void add(final String name, final TimeSeries ts, final Color color) {

		add(name, ts);
		if (color != null) {
			colors.add(color);
		}
	}

	public void add(final String name, final TimeSeries ts, final String propertyName) {
		
		final org.jfree.data.time.TimeSeries jts = new org.jfree.data.time.TimeSeries(name, getChartPeriod(ts
				.getPeriod()));
		for (TimeSeriesEntry entry : ts) {
			jts.add(getEntryPeriod(ts.getPeriod(), entry.getTime()), entry.getDoubleProperty(propertyName));
		}
		items.addSeries(jts);
		
	}
	
	public void add(final String name, final TimeSeries ts, final String propertyName, final Color color, final LineStyle lineStyle) {
		
		add(name, ts, propertyName, color);
		if (lineStyle != null) {
			strokes.add(lineStyle.getStroke());
		}
	}
	
	/**
	 * @param name
	 * @param ts
	 * @param propertyName
	 */
	public void add(final String name, final TimeSeries ts, final String propertyName, final Color color) {

		add(name, ts, propertyName);
		if (color != null) {
			colors.add(color);
		}
	}

	
	/**
	 * @param tsp
	 * @return
	 */
	private static Class<? extends org.jfree.data.time.RegularTimePeriod> getChartPeriod(final TimeSeriesPeriod tsp) {

		final Class<? extends org.jfree.data.time.RegularTimePeriod> period;

		switch (tsp) {
		case DAY:
			period = Day.class;
			break;
		case HOUR:
			period = Hour.class;
			break;
		case MONTH:
			period = Month.class;
			break;
		case YEAR:
			period = Year.class;
			break;
		default:
			throw new UnsupportedOperationException("Unsupported period: " + tsp);
		}

		return period;
	}

	/**
	 * @param period
	 * @param date
	 * @return
	 */
	private static org.jfree.data.time.RegularTimePeriod getEntryPeriod(final TimeSeriesPeriod period,
			final DateTime date) {

		final org.jfree.data.time.RegularTimePeriod ret;

		switch (period) {
		case DAY:
			ret = new Day(date.getDayOfMonth(), date.getMonthOfYear(), date.getYear());
			break;
		case HOUR:
			ret = new Hour(date.getHourOfDay(), date.getDayOfYear(), date.getMonthOfYear(), date.getYear());
			break;
		case MONTH:
			ret = new Month(date.getMonthOfYear(), date.getYear());
			break;
		case YEAR:
			ret = new Year(date.getYear());
			break;
		default:
			throw new UnsupportedOperationException("Unsupported period: " + period);
		}

		return ret;

	}

	/**
	 * @return the items
	 */
	@Override
	public org.jfree.data.time.TimeSeriesCollection getItems() {
		return items;
	}

	/**
	 * @return the bands
	 */
	public List<org.jfree.data.time.TimeSeriesCollection> getBands() {
		return bands;
	}

	/**
	 * @return the bandColors
	 */
	public List<Color> getBandColors() {
		return bandColors;
	}

	/**
	 * @return the bandFillColors
	 */
	public List<Color> getBandFillColors() {
		return bandFillColors;
	}

	/**
	 * @return the colors
	 */
	@Override
	public List<Color> getColors() {
		final List<Color> ret;
		if (colors == null || colors.size() == 0) {
			ret = new ArrayList<Color>();
			Color color = defaultColor;
			for (int i = 0; i < items.getSeriesCount(); i++) {
				ret.add(color);
				color = new Color(Math.min(255, color.getRed() + 25), Math.min(255, color.getGreen() + 25), Math.min(255, color.getBlue() + 25));
			}
			
		} else {
			ret = colors;
		}
		return ret;
	}

	/**
	 * @return the type
	 */
	@Override
	public ChartType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(ChartType type) {
		this.type = type;
	}
	
}
