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
package com.hmsinc.epicenter.spatial.test.integration;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import org.apache.commons.csv.CSVParser;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;

import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.spatial.analysis.BayesianSpatialScanStatistic;
import com.hmsinc.epicenter.spatial.analysis.PosteriorGrid;
import com.hmsinc.epicenter.spatial.analysis.SpatialScanGrid;
import com.hmsinc.epicenter.spatial.render.SpatialScanRenderer;
import com.hmsinc.epicenter.spatial.service.SpatialScanService;
import com.hmsinc.epicenter.spatial.util.GoogleProjection;
import com.hmsinc.epicenter.spatial.util.CellLabel;
import com.hmsinc.epicenter.spatial.util.SpatialGridItem;
import com.hmsinc.epicenter.spatial.util.SpatialGridUtils;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * @author shade
 * @version $Id: SpatialScanIntegrationTest.java 1822 2008-07-11 16:09:51Z steve.kondik $
 */
public class SpatialScanIntegrationTest extends AbstractJpaTests {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private SpatialScanRenderer spatialScanRenderer;
	
	@Resource
	private GeographyRepository geographyRepository;

	@Resource
	private AnalysisRepository analysisRepository;
	
	@Resource
	private SpatialScanService spatialScanService;
	
	@Resource
	private BayesianSpatialScanStatistic bsss;
	
	public void testGridRenderer() throws Exception {
		
		final State ohio = geographyRepository.getStateByAbbreviation("OH");
		assertNotNull(ohio);
		
		final PosteriorGrid grid = new PosteriorGrid(20, 20);
		for (int x = 0; x < 20; x++) {
			for (int y = 0; y < 20; y++) {
				grid.setPosterior(x, y, Math.random());
			}
		}
		
		final BufferedImage image = spatialScanRenderer.renderGrid(grid, ohio.getGeometry().getEnvelopeInternal(), GoogleProjection.GOOGLE_MERCATOR, 800, 600, null, null);
		assertNotNull(image);
		
		ImageIO.write(image, "png", new File("target/random.png"));
		
	}
	
	public void testSyntheticOutbreak() throws Exception {
		
		final State ohio = geographyRepository.getStateByAbbreviation("OH");
		assertNotNull(ohio);
		
		final DateTime start = new DateTime();
		
		final SpatialScanGrid grid = createSpatialGrid(ohio);
		assertNotNull(grid);
		
		final PosteriorGrid result = bsss.runSpatialScan(grid, 5, 5);
	
		assertEquals(grid.getColumns(), result.getColumns());
		assertEquals(grid.getRows(), result.getRows());
		
		final long runningTime = new DateTime().getMillis() - start.getMillis();
		logger.info("Running time for BSSS was {}ms", runningTime);

		assertTrue(1 - result.getPosteriorProbNoOutbreak() > 0.9);
		
		
		final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
			
		final Set<Geometry> outbreakAreas = new HashSet<Geometry>();
		
		for (int row = 0; row < result.getRows(); row++) {
			for (int column = 0; column < result.getColumns(); column++) {
				
				final double posterior = result.getPosterior(row, column);
				if (posterior > 0.1) {
					
					final StringBuffer data = new StringBuffer();
					
					final CellLabel ident = (CellLabel)grid.getIdentifier(row, column);
					outbreakAreas.add(ident.getGeometry());
					final Envelope env = ident.getGeometry().getEnvelopeInternal();
					
					if (data.length() > 0) {
						data.append(", ");
					}
					data.append("([")
						.append(row).append(", ").append(column).append("]: ")
						.append("(baseline: ").append(grid.getBaseline(row, column))
						.append(", value: ").append(grid.getCount(row, column))
						.append(", posterior: ").append(posterior)
						.append(", extent: ").append(env.toString())
						.append(", label: ").append(ident.getLabel())
						.append(")");
					
					logger.info(data.toString());
				}
			}

		}
		
		assertTrue(outbreakAreas.size() > 0);
		
		// Assert that the detected outbreak area contains 43130 (the synthetic spike)
		final GeometryCollection geom = geometryFactory.createGeometryCollection(outbreakAreas.toArray(new Geometry[outbreakAreas.size()]));
		final Envelope outbreakEnv = geom.getEnvelopeInternal();
		assertNotNull(outbreakEnv);
		
		final Zipcode zip43130 = geographyRepository.getGeography("43130", Zipcode.class);
		assertNotNull(zip43130);
		
		assertEquals("43130", zip43130.getName());
		
		logger.info("Detected outbreak region is: {}", outbreakEnv);
		logger.info("Outbreak should be in: {}", zip43130.getGeometry().getEnvelopeInternal());
			
		
		assertTrue(outbreakEnv.intersects(zip43130.getGeometry().getEnvelopeInternal()));
		
		final BufferedImage image = spatialScanRenderer.renderGrid(result, ohio.getGeometry().getEnvelopeInternal(), null, 1600, 1200, SpatialGridUtils.createCellLabels(grid), ohio.getZipcodes());
		assertNotNull(image);
		
		ImageIO.write(image, "png", new File("target/synthetic.png"));
		
	}
	
	private SpatialScanGrid createSpatialGrid(final State geography) throws Exception {

		final Map<Zipcode, SpatialGridItem> items = new HashMap<Zipcode, SpatialGridItem>();
		
		final Set<Zipcode> zips = geography.getZipcodes();
		final Map<String, Zipcode> zipMap = new HashMap<String, Zipcode>();
		for (Zipcode z : zips) {
			zipMap.put(z.getName(), z);
		}
		
		final BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/ohio_test_data.csv"));
		final CSVParser parser = new CSVParser(reader);
		
		String[] line;
		while ((line = parser.getLine()) != null) {
			
			final Zipcode z = zipMap.get(line[0]);
			if (z == null) {
				logger.error("Unknown zipcode: {}", line[0]);
			} else {
				items.put(z, new SpatialGridItem(Double.valueOf(line[1]), Double.valueOf(line[2])));
			}
		}

		final SpatialScanGrid grid = SpatialGridUtils.gridify(items, geography.getGeometry().getEnvelopeInternal());
		assertNotNull(grid);
		
		return grid;
	}
	
	public void testSpatialScanService() throws Exception {
		
		final DateTime now = new DateTime();
		final AnalysisParameters p = new AnalysisParameters(now, now);
		
		final List<Classification> cls = analysisRepository.getClassifications(
				analysisRepository.getClassifierByName("Infectious Disease Symptoms"),
				Arrays.asList(new String[] { "Rash" }) );
		assertNotNull(cls);
		p.getClassifications().addAll(cls);
		
		final State ohio = geographyRepository.getStateByAbbreviation("OH");
		assertNotNull(ohio);
		p.setContainer(ohio);
		
		final BufferedImage image = spatialScanService.scan(p, County.class, null, 800, 600, true, true).getImage();
		assertNotNull(image);
		
		ImageIO.write(image, "png", new File("target/ohio.png"));
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:itest-spatial-beans.xml" };
	}
}
