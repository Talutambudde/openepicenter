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
import java.awt.Stroke;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.plot.Marker;
import org.jfree.data.general.Dataset;

public abstract class AbstractChart implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4632029882528510204L;

	protected String title;
	
	protected String xLabel;
	
	protected String yLabel;
	
	protected boolean alwaysScaleFromZero = true;
	
	protected Color defaultColor = new Color(0x224565);
	
	protected TickUnitSource rangeTickUnits = NumberAxis.createStandardTickUnits();
	
	protected final List<Color> colors = new ArrayList<Color>();

	protected final List<Stroke> strokes = new ArrayList<Stroke>();
	
	protected final List<Marker> markers = new ArrayList<Marker>();
	
	public AbstractChart() {
		super();
	}

	public abstract Dataset getItems();
	
	public abstract ChartType getType();
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the xLabel
	 */
	public String getXLabel() {
		return xLabel;
	}

	/**
	 * @param label
	 *            the xLabel to set
	 */
	public void setXLabel(String label) {
		xLabel = label;
	}

	/**
	 * @return the yLabel
	 */
	public String getYLabel() {
		return yLabel;
	}

	/**
	 * @param label
	 *            the yLabel to set
	 */
	public void setYLabel(String label) {
		yLabel = label;
	}

	/**
	 * @return the colors
	 */
	public List<Color> getColors() {
		return colors;
	}

	/**
	 * @return the defaultColor
	 */
	public Color getDefaultColor() {
		return defaultColor;
	}

	/**
	 * @param defaultColor the defaultColor to set
	 */
	public void setDefaultColor(Color defaultColor) {
		this.defaultColor = defaultColor;
	}

	/**
	 * @return the rangeTickUnits
	 */
	public TickUnitSource getRangeTickUnits() {
		return rangeTickUnits;
	}

	/**
	 * @param rangeTickUnits the rangeTickUnits to set
	 */
	public void setRangeTickUnits(TickUnitSource rangeTickUnits) {
		this.rangeTickUnits = rangeTickUnits;
	}

	/**
	 * @return the strokes
	 */
	public List<Stroke> getStrokes() {
		return strokes;
	}

	/**
	 * @return the markers
	 */
	public List<Marker> getMarkers() {
		return markers;
	}

	/**
	 * @return the alwaysScaleFromZero
	 */
	public boolean isAlwaysScaleFromZero() {
		return alwaysScaleFromZero;
	}

	/**
	 * @param alwaysScaleFromZero the alwaysScaleFromZero to set
	 */
	public void setAlwaysScaleFromZero(boolean alwaysScaleFromZero) {
		this.alwaysScaleFromZero = alwaysScaleFromZero;
	}
}