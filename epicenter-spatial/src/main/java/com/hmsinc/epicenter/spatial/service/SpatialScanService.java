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
package com.hmsinc.epicenter.spatial.service;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.univariate.DescriptiveUnivariateAnalyzer;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.epicenter.service.data.DataQueryService;
import com.hmsinc.epicenter.spatial.analysis.BayesianSpatialScanStatistic;
import com.hmsinc.epicenter.spatial.analysis.PosteriorGrid;
import com.hmsinc.epicenter.spatial.analysis.SpatialScanGrid;
import com.hmsinc.epicenter.spatial.render.SpatialScanRenderer;
import com.hmsinc.epicenter.spatial.render.SpatialScanResult;
import com.hmsinc.epicenter.spatial.util.SpatialGridItem;
import com.hmsinc.epicenter.spatial.util.SpatialGridUtils;
import com.hmsinc.ts4j.TimeSeries;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Executes a spatial scan and returns a visualization.
 * 
 * @author shade
 * @version $Id: SpatialScanService.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public class SpatialScanService {

	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	@Resource
	private DataQueryService dataQueryService;

	@Resource
	private BayesianSpatialScanStatistic bsss;

	@Resource
	private SpatialScanRenderer spatialScanRenderer;

	@Resource
	private DescriptiveUnivariateAnalyzer smaAnalyzer;

	@Resource
	private GeographyRepository geographyRepository;
	
	/**
	 * @param <G>
	 * @param analysisParameters
	 * @param resolution
	 * @param width
	 * @param height
	 * @return
	 */
	public <G extends Geography> SpatialScanResult scan(final AnalysisParameters analysisParameters,
			final Class<G> resolution, final CoordinateReferenceSystem targetCRS, int width, int height, boolean showGrid, boolean showFeatures) {
		
		final AnalysisParameters p = (AnalysisParameters) analysisParameters.clone();
		p.setStartDate(smaAnalyzer.getTrainingPeriod().subtractFrom(p.getEndDate()).minusDays(2));
		
		// We can't use a container directly, but we can convert it into a filter.
		if (p.getContainers() != null && p.getContainers().size() > 0) {
			final Geometry[] geometries = new Geometry[p.getContainers().size()];
			int i = 0;
			for (Geography container : p.getContainers()) {
				geometries[i] = container.getGeometry();
			}
			
			final GeometryCollection gc = geometryFactory.createGeometryCollection(geometries);
			p.setFilter(gc.getEnvelopeInternal());
			p.getContainers().clear();
		}
		
		/*
		if (p.getFilter() != null && targetCRS != null && !targetCRS.equals(DefaultGeographicCRS.WGS84)) {
			
			// Project the envelope to WGS84
			final ReferencedEnvelope refEnv = new ReferencedEnvelope(p.getFilter().getEnvelopeInternal(), targetCRS);
			try {
				p.setFilter(geometryFactory.toGeometry(refEnv.transform(DefaultGeographicCRS.WGS84, false)));
			} catch (FactoryException e) {
				throw new RuntimeException(e);
			} catch (TransformException e) {
				throw new RuntimeException(e);
			}
		}
		*/
		
		
		p.validate();

		// Call getContained since the query won't return geographies with a 0 count
		final List<G> allGeographies = geographyRepository.getContained(p.getFilter(), 4326, resolution);
				
		final BufferedImage resultImage;
		final double outbreakProbability;
		
		if (allGeographies.size() > 0) {
			
			final Map<G, TimeSeries> result = dataQueryService.queryCombined(p, resolution);

			final Map<G, SpatialGridItem> items = new HashMap<G, SpatialGridItem>();
			final Set<Geometry> geometries = new HashSet<Geometry>();

			for (G geography : allGeographies) {
				
				final SpatialGridItem item;
				if (result.containsKey(geography)) {
					final TimeSeries analyzed = smaAnalyzer.process(result.get(geography));
					item = new SpatialGridItem(analyzed.last().getValue(), analyzed.last().getDoubleProperty(ResultType.PREDICTED));
					
				} else {
					item = new SpatialGridItem(0, 0);
				}
				items.put(geography, item);
				geometries.add(geography.getGeometry());
			}
						
			final Envelope extent = p.getFilter();

			final SpatialScanGrid grid = SpatialGridUtils.gridify(items, extent);
			final PosteriorGrid bsssResult = bsss.runSpatialScan(grid, 5, 5);

			resultImage = spatialScanRenderer.renderGrid(bsssResult, extent, targetCRS, width, height, 
					(showGrid ? SpatialGridUtils.createCellLabels(grid) : null), 
					(showFeatures ? allGeographies : null));
			outbreakProbability = (1 - bsssResult.getPosteriorProbNoOutbreak());
			
		} else {
			resultImage = null;
			outbreakProbability = 0.0;
		}
		
		return new SpatialScanResult(outbreakProbability, resultImage);

	}
	
}
