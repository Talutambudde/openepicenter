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
package com.hmsinc.epicenter.spatial.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.spatial.analysis.SpatialScanGrid;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * @author shade
 * @version $Id: SpatialGridUtils.java 1671 2008-05-22 18:52:35Z steve.kondik $
 */
public class SpatialGridUtils {

	private static final Logger logger = LoggerFactory.getLogger(SpatialGridUtils.class);
	
	private static final GeometryFactory geometryFactory = new GeometryFactory();
	
	/**
	 * Populates a SpatialScanGrid from the result of a spatial query.
	 * 
	 * @param data
	 * @param extent
	 */
	@SuppressWarnings("unchecked")
	public static SpatialScanGrid gridify(final Map<? extends Geography, SpatialGridItem> data, final Envelope extent) {
		
		final SpatialScanGrid grid = createGrid(data, extent);

		// Put the features into an index so we're not here all day..
		final SpatialIndex index = new STRtree();
		for (Geography geography : data.keySet()) {
			index.insert(geography.getGeometry().getEnvelopeInternal(), geography);
		}
		
		int fid = 0;
		
		for (int row = 0; row < grid.getRows(); row++) {
			for (int column = 0; column < grid.getColumns(); column++) {
		
				double cellCount = 0.0;
				double cellBaseline = 0.0;
				
				final Envelope cellExtent = getCellExtent(extent, grid, row, column);
				
				logger.trace("Processing cell: ({}, {})  Extent: {}", new Object[] { row, column, cellExtent });
				
				final List<? extends Geography> features = index.query(cellExtent);
				final Set<String> labels = new HashSet<String>();
				
				if (features != null && features.size() > 0) {
															
					for (Geography feature : features) {
						
						/*
						 * Pretend that we have an even population distribution, and
						 * scale the values based on the area of the geometry that is
						 * within this cell.
						 */
						final Geometry intersection = feature.getGeometry().intersection(geometryFactory.toGeometry(cellExtent));
						final double coverage = intersection.getArea() / feature.getGeometry().getArea();
						
						if (coverage > 0.0) {
							labels.add(feature.getName());
						}
						
						final double rawCount = data.get(feature).getValue();
						final double count = rawCount * coverage;
						
						final double rawBaseline = data.get(feature).getBaseline();
						final double baseline = rawBaseline * coverage;
						
						logger.trace("Coverage of cell for feature {} is {}%  [count: {}  adjusted count: {}  baseline: {}  adjusted baseline: {}]", new Object[] {
								feature.getDisplayName(), coverage * 100, rawCount, count, rawBaseline, baseline });
						
						cellCount += count;
						cellBaseline += baseline;
						
					}
				}
				
				logger.trace("Result for cell ({}, {}): [count={} baseline={}]", new Object[] {
						row, column, cellCount, cellBaseline });
				
				
				final CellLabel labelPoint = new CellLabel(fid, "(" + row + ", " + column +")", geometryFactory.toGeometry(cellExtent));
				grid.setCell(row, column, cellCount, cellBaseline, labelPoint);
				fid++;
			}
		}
		
		if (logger.isTraceEnabled()) {
			for (int x = 0; x < grid.getColumns(); x++) {
				final StringBuffer row = new StringBuffer();
				for (int y = 0; y < grid.getRows(); y++) {
					if (row.length() > 0) {
						row.append(", ");
					}
					row.append("([");
					row.append(x).append(", ").append(y).append("]: ");
					row.append(grid.getBaseline(y, x)).append(", ").append(grid.getCount(y, x)).append(")");
				}
				logger.trace(row.toString());
			}
		}
		
		return grid;
	}
	
	/**
	 * Gets an Envelope that corresponds to the location of the cell on Earth.
	 * 
	 * @param extent
	 * @param grid
	 * @param row
	 * @param column
	 * @return
	 */
	private static Envelope getCellExtent(final Envelope extent, final SpatialScanGrid grid, int row, int column) {

		final double cellWidth = extent.getWidth() / grid.getColumns();
		final double cellHeight = extent.getHeight() / grid.getRows();

		final double minX = extent.getMinX() + (cellWidth * column);
		final double maxY = extent.getMaxY() - (cellHeight * row);
		
		return new Envelope(minX, minX + cellWidth, maxY - cellHeight, maxY);
		
	}
	
	/**
	 * Creates a SpatialScanGrid from a set of Geographies.  The cell size
	 * is determined by the average feature area vs. max extent.
	 * 
	 * @param data
	 * @param extent
	 * @return
	 */
	private static SpatialScanGrid createGrid(final Map<? extends Geography, SpatialGridItem> data, final Envelope extent) {
		
		double averageFeatureHeight = 0.0;
		double averageFeatureWidth = 0.0;
		
		for (Geography geography : data.keySet()) {
			final Envelope featureEnvelope = geography.getGeometry().getEnvelopeInternal();
			averageFeatureHeight += featureEnvelope.getHeight();
			averageFeatureWidth += featureEnvelope.getWidth();
		}
		
		averageFeatureHeight = averageFeatureHeight / data.keySet().size();
		averageFeatureWidth = averageFeatureWidth / data.keySet().size();
		
		logger.trace("Average feature height: {}  width: {}.", averageFeatureHeight, averageFeatureWidth);
		
		final int columns = (int)Math.ceil(extent.getWidth() / averageFeatureWidth) * 2;
		final int rows = (int)Math.ceil(extent.getHeight() / averageFeatureHeight) * 2;
		
		logger.debug("Creating grid with {} columns and {} rows.", columns, rows);
		
		return new SpatialScanGrid(rows, columns);

	}

	/**
	 * Creates a FeatureCollection of cell labels to identify the grid points.
	 * 
	 * @param grid
	 * @return
	 */
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> createCellLabels(final SpatialScanGrid grid) {
		
		final SimpleFeatureTypeBuilder sb = new SimpleFeatureTypeBuilder();
		sb.setName("cellLabel");
		sb.setNamespaceURI("http://epicenter.hmsinc.com");
		sb.setCRS(DefaultGeographicCRS.WGS84);
		sb.add("geometry", Polygon.class);
		sb.add("label", String.class);
		
		final SimpleFeatureType type = sb.buildFeatureType();

		final FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();
		
		for (int row = 0; row < grid.getRows(); row++) {
			for (int column = 0; column < grid.getColumns(); column++) {
				final Object ident = grid.getIdentifier(row, column);
				if (ident != null && ident instanceof CellLabel) {		

					final CellLabel labelPoint = (CellLabel)ident;
					
					final SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(type);
					sfb.set("geometry", labelPoint.getGeometry());
					sfb.set("label", labelPoint.getLabel());
					
					fc.add(sfb.buildFeature("cellLabel." + labelPoint.getFid()));
				}
			}
		}
		
		return fc;
	}
	
	/**
	 * Creates a FeatureCollection of all geographies.
	 * 
	 * @param <G>
	 * @param geographies
	 * @return
	 */
	public static <G extends Geography> FeatureCollection<SimpleFeatureType, SimpleFeature> createGeographyLayer(final Collection<G> geographies) {
		
		final SimpleFeatureTypeBuilder sb = new SimpleFeatureTypeBuilder();
		sb.setName("geography");
		sb.setNamespaceURI("http://epicenter.hmsinc.com");
		sb.setCRS(DefaultGeographicCRS.WGS84);
		sb.add("geometry", MultiPolygon.class);
		sb.add("label", String.class);
		
		final SimpleFeatureType type = sb.buildFeatureType();

		final FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();
		
		for (Geography g : geographies) {
			
			final SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(type);
			sfb.set("geometry", g.getGeometry());
			sfb.set("label", g.getDisplayName());
			fc.add(sfb.buildFeature("geography." + g.getId()));
		}

		return fc;
		
	}
}
