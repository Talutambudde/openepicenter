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
package com.hmsinc.epicenter.model.test.integration;

import java.util.*;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Region;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.test.AbstractModelTest;
import com.hmsinc.hibernate.spatial.GeometryType;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 *
 */
public class SpatialIntegrationTest extends AbstractModelTest {

	private Type geometryType = new CustomType(GeometryType.class, null);
	
	public void testGeography() throws Throwable {
			
		List<Zipcode> zips = geographyRepository.getGeography(Arrays.asList("15216", "15220"), Zipcode.class);
		assertEquals(2, zips.size());
		Zipcode z = zips.get(0);
		assertNotNull(z.getGeometry());
		assertEquals(4326, z.getGeometry().getSRID());

		System.out.println(z.getGeometry().toText());

	}

	public void testStates() throws Throwable {

		List<State> states = geographyRepository.getList(State.class);
		assertEquals(51, states.size());

	}

	public void testStateCollections() throws Throwable {
		
		State oh = geographyRepository.getStateByAbbreviation("OH");
		assertNotNull(oh);
		
		Set<County> counties = oh.getCounties();
		assertNotNull(counties);
		assertEquals(88, counties.size());
		
		for (County county : counties) {
			assertEquals(oh, county.getState());
		}
		
		Set<Zipcode> zips = oh.getZipcodes();
		assertNotNull(zips);
		assertEquals(1010, zips.size());
		
		for (Zipcode zip : zips) {
			assertEquals(oh, zip.getState());
		}
	}
	
	public void testZipcodeAndCountyRelationship() throws Throwable {
		
		State oh = geographyRepository.getStateByAbbreviation("OH");
		
		List<County> counties = geographyRepository.getCountiesInState(oh, Arrays.asList("Jefferson"));
		assertNotNull(counties);
		assertEquals(1, counties.size());
		
		Set<Zipcode> zipsInCounty = counties.get(0).getZipcodes();
		assertNotNull(zipsInCounty);
		assertEquals(12, zipsInCounty.size());
		
		Set<String> stringZips = new HashSet<String>();
		for (Zipcode z : zipsInCounty) {
			stringZips.add(z.getName());
		}
		
		List<Zipcode> zips = geographyRepository.getZipcodesInState(oh, stringZips);
		assertNotNull(zips);
		assertEquals(12, zips.size());
		assertTrue(zipsInCounty.containsAll(zips));
		
	}
	
	public void testRegions() throws Throwable {

		Region usa = null;

		List<Region> regions = geographyRepository.getList(Region.class);
		for (Region region : regions) {
			if (region.getName().equals("United States")) {
				usa = region;
				break;
			}
		}
		assertNotNull(usa);

	//	assertEquals(51, geographyRepository.getContained(usa, State.class).size());
	}

	public void testCountiesAndSpatialContainment() throws Throwable {

		// Let's find where I grew up :)
		State state = geographyRepository.getStateByAbbreviation("WV");
		assertEquals("West Virginia", state.getName());

		List<County> counties = geographyRepository.getCountiesInState(state, Arrays.asList("Hancock"));
		assertEquals(1, counties.size());
		assertEquals("Hancock", counties.get(0).getName());

		List<Zipcode> zipsInHancockCounty = geographyRepository.getContained(counties.get(0), Zipcode.class);
		assertEquals(4, zipsInHancockCounty.size());

		boolean found26062 = false;
		for (Zipcode z : zipsInHancockCounty) {

			System.out.println("Found zipcode: " + z.getName());

			if (z.getName().equals("26062")) {
				found26062 = true;
			}
		}

		if (!found26062) {
			fail("Zipcode 26062 was not found in Hancock county, WV.  The locals are coming with shotguns.");
		}
	}

	public void testLargeSpatialContainment() throws Throwable {
		
		List<Region> regions = geographyRepository.getGeography(Arrays.asList("United States"), Region.class);
		assertNotNull(regions);
		assertEquals(1, regions.size());
		
		Region usa = regions.get(0);
		assertEquals("United States", usa.getName());
		
		List<State> states = geographyRepository.getContained(usa, State.class);
		assertNotNull(regions);
		assertEquals(51, states.size());
	}
	
	@SuppressWarnings("unchecked")
	public void testGeographyUnion() throws Throwable {

		List<Geography> geos = sharedEntityManager.createQuery("from Geography g where g.id = :geo").setParameter("geo", 46794L)
				.getResultList();
		assertEquals(1, geos.size());
		assertEquals("26062", geos.get(0).getName());
	}

	public void testGeometryFunctions() throws Throwable {

		State state = geographyRepository.getStateByAbbreviation("WV");
		assertNotNull(state.getGeometry());

		// Test SRID
		Integer srid = (Integer) sharedEntityManager.createQuery(
				"select srid(s.geometry) from State s fetch all properties where s.abbreviation='WV'").getSingleResult();
		assertEquals(state.getGeometry().getSRID(), srid.intValue());

		// NOTE: Oracle will return it's coordinate list for WKT/WKB in
		// reverse!!

		// Test WKT
		String wkt = (String) sharedEntityManager.createQuery(
				"select astext(s.geometry) from State s fetch all properties where s.abbreviation='WV'").getSingleResult();
		assertNotNull(wkt);
		assertTrue(wkt.startsWith("MULTIPOLYGON"));

		WKTReader wktReader = new WKTReader();
		Geometry wktGeom = wktReader.read(wkt);
		wktGeom.setSRID(state.getGeometry().getSRID());
		// FIXME: SRID issue here i think
		// assertTrue(state.getGeometry().equals(wktGeom));

		// Test WKB
		byte[] wkb = (byte[]) sharedEntityManager.createQuery(
				"select asbinary(s.geometry) from State s fetch all properties where s.abbreviation='WV'").getSingleResult();

		WKBReader wkbReader = new WKBReader();
		Geometry wkbGeom = wkbReader.read(wkb);
		assertTrue(state.getGeometry().equals(wkbGeom));

		// Test dimension()
		Integer dimension = (Integer) sharedEntityManager.createQuery(
				"select dimension(s.geometry) from State s fetch all properties where s.abbreviation='WV'").getSingleResult();
		assertEquals(2, dimension.intValue());

		// Test geometrytype()
		String geotype = (String) sharedEntityManager.createQuery(
				"select geometrytype(s.geometry) from State s fetch all properties where s.abbreviation='WV'").getSingleResult();
		assertEquals("MULTIPOLYGON", geotype);
	}

	@SuppressWarnings("unchecked")
	public void testRelationalFunctions() throws Throwable {

		State penna = geographyRepository.getStateByAbbreviation("PA");
		List<State> touchesPA = ((Session) sharedEntityManager.getDelegate()).createQuery(
				"from State s fetch all properties where touches(s.geometry, :geo) = 'TRUE'").setParameter("geo", penna.getGeometry(),
				geometryType).list();

		assertTrue(touchesPA.contains(geographyRepository.getStateByAbbreviation("WV")));
		assertTrue(touchesPA.contains(geographyRepository.getStateByAbbreviation("OH")));
	}

	public void testAnalysisFunctions() throws Throwable {

		Double dbDistance = (Double) ((Session) sharedEntityManager.getDelegate())
				.createQuery(
						"select distance(centroid(s1.geometry), centroid(s2.geometry)) from State s1, State s2 where s1.abbreviation='CA' and s2.abbreviation='NY')")
				.list().iterator().next();

		assertTrue(dbDistance > 1);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:itest-model-beans.xml" };
	}
	

}
