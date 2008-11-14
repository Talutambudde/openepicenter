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
package com.hmsinc.epicenter.spatial.render;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.ColorMap;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.spatial.analysis.PosteriorGrid;
import com.hmsinc.epicenter.spatial.util.SpatialGridUtils;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Renders output of a spatial scan as a coverage.
 * 
 * @author shade
 * @version $Id: SpatialScanRenderer.java 1734 2008-06-13 13:09:52Z steve.kondik $
 */
public class SpatialScanRenderer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/*
	@Resource
	private ResourceLoader resourceLoader;
	
	private String styleLocation = "classpath:styles/spatial-scan-style.xml";
	*/
	
	private static final GridCoverageFactory gridCoverageFactory = new GridCoverageFactory();

	private String inputCRS = "EPSG:4326";
			
	/**
	 * @param grid
	 * @param extent
	 * @return
	 */
	public <G extends Geography> BufferedImage renderGrid(final PosteriorGrid grid, final Envelope extent, final CoordinateReferenceSystem targetCRS, int width, int height, 
			FeatureCollection<SimpleFeatureType, SimpleFeature> labelPoints, Collection<G> geographies) {

		Validate.notNull(grid, "Result grid is required.");
		Validate.notNull(extent, "Extent is required.");

		final CoordinateReferenceSystem sourceCRS;

		try {
			sourceCRS = CRS.decode(inputCRS);
		} catch (NoSuchAuthorityCodeException e) {
			throw new RuntimeException(e.getMessage(), e);
		} catch (FactoryException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
				
		final ReferencedEnvelope refEnv = new ReferencedEnvelope(extent, sourceCRS);
		
		logger.debug("Rendering image for extent: {}", refEnv);
		
		// Transform the posterior results into a coverage grid
		final float[][] matrix = new float[grid.getRows()][grid.getColumns()];
		for (int row = 0; row < grid.getRows(); row++) {
			for (int column = 0; column < grid.getColumns(); column++) {
				matrix[row][column] = (float)(grid.getPosterior(row, column) * 100);
			}
		}

		final GridCoverage2D coverage = gridCoverageFactory.create("SpatialScanOverlay", matrix, refEnv);
		
		final CoordinateReferenceSystem outputCRS = targetCRS == null ? sourceCRS : targetCRS;
		
		final MapContext context = new DefaultMapContext(outputCRS);
		
		if (geographies != null) {
			context.addLayer(SpatialGridUtils.createGeographyLayer(geographies), getGeographyStyle());
		}
		
		if (labelPoints != null) {
			context.addLayer(labelPoints, getLabelStyle());
		}
			
		context.addLayer(coverage, getCoverageStyle());
		
		return renderImage(context, width, height);

	}

	/**
	 * @param context
	 * @return
	 */
	private BufferedImage renderImage(final MapContext context, int width, int height) {
		
		logger.trace("Image width: {}  height: {}", width, height);
		
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		final Graphics2D graphics2D = (Graphics2D) image.getGraphics();

		final GTRenderer renderer = new StreamingRenderer();
		final RenderingHints h = new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		h.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		h.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		h.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		h.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		renderer.setJava2DHints(h);
		renderer.setContext(context);
		renderer.paint(graphics2D, new Rectangle(width, height), context.getAreaOfInterest());

		
		return image;
	}

	private Style getLabelStyle() {
		
		final StyleBuilder sb = new StyleBuilder();

		final TextSymbolizer ts = sb.createTextSymbolizer(sb.createFill(Color.RED), 
				new org.geotools.styling.Font[] { sb.createFont("Arial", 8) },
				sb.createHalo(), sb.attributeExpression("label"), null, null); 

        final PolygonSymbolizer pps = sb.createPolygonSymbolizer(Color.RED, 1);
        
        final Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("cellLabel", new Symbolizer[] { ts, pps }));
		
        return style;
	}
	
	private Style getGeographyStyle() {
		
		final StyleBuilder sb = new StyleBuilder();

		final TextSymbolizer ts = sb.createTextSymbolizer(sb.createFill(Color.BLACK), 
				new org.geotools.styling.Font[] { sb.createFont("Arial", 8) },
				sb.createHalo(), sb.attributeExpression("label"), null, null); 

        final PolygonSymbolizer pps = sb.createPolygonSymbolizer(Color.WHITE, Color.BLACK, 1);

        final Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("geography", new Symbolizer[] { ts, pps }));
		
        return style;
	}

	/**
	 * TODO: Move this into external XML.
	 * 
	 * @return
	 */
	private Style getCoverageStyle() {

		final StyleBuilder sb = new StyleBuilder();
		
		final ColorMap cm = sb.createColorMap(
				new String[] { "0", "10", "20", "40", "60", "80", "90", "95", "100" }, 
				new double[] { 0, 10, 20, 40, 60, 80, 90, 95,  100 }, 
				new Color[] { new Color(255, 255, 255, 0),
							  new Color(0xFFFFCC),
							  new Color(0xFFEDA0),
							  new Color(0xFED976),
							  new Color(0xFEB24C),
							  new Color(0xFD8D3C),
							  new Color(0xFC4E2A),
							  new Color(0xE31A1C),
							  new Color(0xB10026) },
			  ColorMap.TYPE_RAMP);
		
		final RasterSymbolizer rsDem = sb.createRasterSymbolizer(cm, 0.6);

		return sb.createStyle(rsDem);

	}

	/*
	 * TODO: We should be able to get our styles from an external SLD.
	 * 
	private Style getStyle() {
		
		final URL surl;
		final SLDParser stylereader;
		
		try {
			surl = resourceLoader.getResource(styleLocation).getURL();
			stylereader = new SLDParser(CommonFactoryFinder.getStyleFactory(null), surl);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		
		final Style style = stylereader.readXML()[0];
	//	final StyledLayerDescriptor sld = stylereader.parseSLD();
		Validate.notNull(style, "Unable to read SLD.");
		
		logger.info("Style: {}  ...{}", style, style.getName());
	//	return ((UserLayer)sld.getStyledLayers()[0]).getUserStyles()[0];
		return style;
	}
	*/
}
