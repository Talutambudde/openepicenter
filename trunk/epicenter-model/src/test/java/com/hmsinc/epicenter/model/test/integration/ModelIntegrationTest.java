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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;

import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Region;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.test.AbstractModelTest;
import com.hmsinc.epicenter.model.workflow.Subscription;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;
import com.hmsinc.ts4j.TimeSeriesEntry;
import com.hmsinc.ts4j.TimeSeriesPeriod;

/**
 * TODO: Move spatial tests to Hibernate-Spatial project.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: WarehouseIntegrationTest.java 207 2007-04-24 19:33:16Z
 *          steve.kondik $
 */
public class ModelIntegrationTest extends AbstractModelTest {

	/*
	public void testRecentAnomalies() throws Throwable {
		
		final County dade = geographyRepository.load(9261L, County.class);
		assertNotNull(dade);
		assertEquals("Dade", dade.getName());
		
		final Classifier c = analysisRepository.getClassifierByName("Syndromes");
		assertNotNull(c);
		
		final List<Classification> cls = analysisRepository.getClassifications(c, Arrays.asList(new String[]{"Respiratory"}));
		assertEquals(1, cls.size());
		
		final SurveillanceTask task = surveillanceRepository.load(65L, SurveillanceTask.class);
		assertNotNull(task);
		
		final Anomaly a = surveillanceRepository.getLatestAnomaly(dade, cls.get(0), task, null, new DateTime());
		assertNotNull(a);
		System.out.println(a.toString());
	}
	*/
	
	public void testMultiClassDataTypeCounts() throws Throwable {
	
		DateTime c = new DateTime(2007, 2, 1, 0, 0, 0, 0);
		DateTime endDate = c.plusDays(6);

		final AnalysisParameters params = new AnalysisParameters(c, endDate);
		params.setLocation(AnalysisLocation.FACILITY);

		State ohio = geographyRepository.getStateByAbbreviation("OH");
		params.setContainer(ohio);

		final Classifier classifier = analysisRepository.getClassifierByName("Discharge Disposition");
		assertNotNull(classifier);
		
		params.setClassifications(classifier.getClassifications());

		final DataType dt = analysisRepository.getDataType("Discharge Disposition");
		assertNotNull(dt);
		params.setDataType(dt);
		
		params.setPeriod(TimeSeriesPeriod.DAY);
		TimeSeriesCollection<? extends Geography, Classification> counts = analysisRepository
				.getClassifiedCounts(params, State.class);

		assertNotNull(counts);
		assertTrue(counts.getMaps().size() > 0);
		
	}
	
	public void testSubscriptions() throws Throwable {

		final State oh = geographyRepository.getStateByAbbreviation("OH");
		assertNotNull(oh);
		
		final Classifier c = analysisRepository.getClassifierByName("Syndromes");
		assertNotNull(c);
		
		final List<Classification> cls = analysisRepository.getClassifications(c, Arrays.asList(new String[]{"Gastrointestinal"}));
		assertEquals(1, cls.size());
		
		final Classification g = cls.get(0);
		
		final List<Subscription> subs = workflowRepository.getSubscriptions(g, oh);
		for (Subscription s : subs) {
			System.out.println(s.getName());
		}
		
	}
	
	public void testGeographicalCountsUsingView() throws Throwable {

		// With view
		doGeographicalCounts();
	}

	/*
	
	public void testGetCases() throws Throwable {

		System.out.println("--------------------------------- (test getCases) ----");
		
		DateTime c = new DateTime(2007, 1, 6, 0, 0, 0, 0);
		DateTime endDate = c.plusDays(1);

		final AnalysisParameters params = new AnalysisParameters(c, endDate);
		params.setLocation(AnalysisLocation.HOME);

		State ohio = geographyRepository.getStateByAbbreviation("OH");

		params.setContainer(ohio);
		
		final Classifier classifier = analysisRepository.getClassifierByName("Syndromes");
		assertNotNull(classifier);
		
		params.setClassifications(classifier.getClassifications());

		assertNotNull(params.getClassifications());
		
		Long casesCount = analysisRepository.getCasesCount(params);
		List<? extends Registration> cases = analysisRepository.getCases(params, 1L, 100L);

		System.out.println("total: " + casesCount);

		for (Registration h : cases) {
			 System.out.println(h.toString());
		}
		assertEquals(100, cases.size());

		//cases = analysisRepository.getCases(params, null, null);
	//	assertEquals(casesCount.intValue(), cases.size());

		System.out.println("---------------------------------");
	}
	
	*/
	
	private void doGeographicalCounts() throws Throwable {

		DateTime c = new DateTime(2007, 2, 1, 0, 0, 0, 0);
		DateTime endDate = c.plusDays(6);

		final AnalysisParameters params = new AnalysisParameters(c, endDate);
		params.setLocation(AnalysisLocation.FACILITY);

		State ohio = geographyRepository.getStateByAbbreviation("OH");
		params.setContainer(ohio);

		final Classifier classifier = analysisRepository.getClassifierByName("Syndromes");
		assertNotNull(classifier);
		
		params.setClassifications(classifier.getClassifications());

		final DataType dt = analysisRepository.getDataType("Emergency Registrations");
		assertNotNull(dt);
		params.setDataType(dt);
		
		// Test hourly county-level counts for a State
		params.setPeriod(TimeSeriesPeriod.HOUR);
		TimeSeriesCollection<? extends Geography, Classification> counts = analysisRepository
				.getClassifiedCounts(params, County.class);

		assertNotNull(counts);
		assertTrue(counts.getMaps().size() > 0);

		// Test zipcode on zipcode
		params.setPeriod(TimeSeriesPeriod.DAY);
		Zipcode zz = geographyRepository.getGeography(Arrays.asList("43952"), Zipcode.class).get(0);
		assertNotNull(zz);
		params.setContainer(zz);
		counts = analysisRepository.getClassifiedCounts(params, Zipcode.class);
		assertNotNull(counts);
		assertEquals(1, counts.getPrimaryIndexes().size());
		assertEquals(zz, counts.getPrimaryIndexes().iterator().next());
		
		// Test hourly single county count by zipcode
		County county = geographyRepository.getCountiesInState(ohio, Arrays.asList("Jefferson")).get(0);
		assertNotNull(county);
		Set<Zipcode> zipsInJefferson = county.getZipcodes();
		assertNotNull(zipsInJefferson);
		assertEquals(12, zipsInJefferson.size());
		
		params.setContainer(county);
		counts = analysisRepository.getClassifiedCounts(params, Zipcode.class);
		assertNotNull(counts);
		
		assertTrue(zipsInJefferson.containsAll(counts.getPrimaryIndexes()));


		// Test daily single county count
		counts = analysisRepository.getClassifiedCounts(params, County.class);
		assertNotNull(counts);
		assertEquals(1, counts.getMaps().size());
		assertEquals(county, counts.getPrimaryIndexes().iterator().next());
		

		// Test daily state count using Region (spatial query)
		List<Region> rl = geographyRepository.getGeography(Arrays.asList("United States"), Region.class);
		assertNotNull(rl);
		assertEquals(1, rl.size());
		Region r = rl.get(0);
		assertEquals("United States", r.getName());
		
		List<State> statesInUSA = geographyRepository.getContained(r, State.class);
		assertNotNull(statesInUSA);
		assertEquals(51, statesInUSA.size());
		
		params.setContainer(r);
		counts = analysisRepository.getClassifiedCounts(params, State.class);
		assertNotNull(counts);
		assertTrue(counts.getPrimaryIndexes().size() > 0);
		assertTrue(statesInUSA.containsAll(counts.getPrimaryIndexes()));

		// Test Region by Region
		counts = analysisRepository.getClassifiedCounts(params, Region.class);
		assertNotNull(counts);
		assertEquals(1, counts.getPrimaryIndexes().size());
		assertEquals("United States", counts.getPrimaryIndexes().iterator().next().getName());
		
		// Test zip counts for a State by facility
		params.setContainer(ohio);
		Set<Zipcode> zipsInOhio = ohio.getZipcodes();
		for (Zipcode zipzip : zipsInOhio) {
			assertEquals(ohio, zipzip.getState());
		}
		
		counts = analysisRepository.getClassifiedCounts(params, Zipcode.class);
		assertNotNull(counts);
		assertTrue(counts.getPrimaryIndexes().size() > 0);
		assertTrue(zipsInOhio.containsAll(counts.getPrimaryIndexes()));

		
		// Test county counts for a State by facility
		params.setContainer(ohio);
		Set<County> countiesInOhio = ohio.getCounties();
		counts = analysisRepository.getClassifiedCounts(params, County.class);
		assertNotNull(counts);
		assertTrue(counts.getPrimaryIndexes().size() > 0);
		assertTrue(countiesInOhio.containsAll(counts.getPrimaryIndexes()));
		

		// Test the whole state
		endDate = endDate.plusMonths(2);
		counts = analysisRepository.getClassifiedCounts(params, State.class);
		assertNotNull(counts);
		assertEquals(1, counts.getPrimaryIndexes().size());
		assertEquals(ohio, counts.getPrimaryIndexes().iterator().next());
		

		// Test normalized counts
		TimeSeriesCollection<County, Classification> norms = analysisRepository.getNormalizedCounts(params, County.class);
		assertNotNull(norms);
		assertTrue(norms.getPrimaryIndexes().size() > 0);
		assertTrue(countiesInOhio.containsAll(norms.getPrimaryIndexes()));

		
		// Test aggregated counts
		TimeSeriesCollection<County, Gender> ar = analysisRepository.getAggregatedCounts(params, County.class, Gender.class);
		assertNotNull(ar);
		assertTrue(ar.getPrimaryIndexes().size() > 0);
		
		TimeSeriesCollection<County, AgeGroup> ar2 = analysisRepository.getAggregatedCounts(params, County.class, AgeGroup.class);
		assertNotNull(ar2);
		assertTrue(ar2.getPrimaryIndexes().size() > 0);
		
		
		// Test combined counts
		Map<County, TimeSeries> combined = analysisRepository.getCombinedCounts(params, County.class);
		assertNotNull(combined);
		assertTrue(combined.size() > 0);
		
		
		// Test bounding box queries
		List<Zipcode> containedZips = geographyRepository.getContained(ohio.getGeometry().getEnvelopeInternal(), ohio.getGeometry().getSRID(), Zipcode.class);
		assertNotNull(containedZips);
		assertTrue(containedZips.size() > 0);
		
		params.setContainer(null);
		params.setFilter(ohio.getGeometry().getEnvelopeInternal());
		counts = analysisRepository.getClassifiedCounts(params, Zipcode.class);
		assertNotNull(counts);
		assertNotNull(counts.getPrimaryIndexes());
		assertTrue(counts.getPrimaryIndexes().size() > 0);
		assertTrue(containedZips.containsAll(counts.getPrimaryIndexes()));
		
		// test state bbox
		counts = analysisRepository.getClassifiedCounts(params, State.class);
		assertNotNull(counts);
		assertNotNull(counts.getPrimaryIndexes());
		assertTrue(counts.getPrimaryIndexes().size() > 0);
		
		// test county bbox
		counts = analysisRepository.getClassifiedCounts(params, County.class);
		assertNotNull(counts);
		assertNotNull(counts.getPrimaryIndexes());
		assertTrue(counts.getPrimaryIndexes().size() > 0);
		
		// test region bbox
		counts = analysisRepository.getClassifiedCounts(params, Region.class);
		assertNotNull(counts);
		assertNotNull(counts.getPrimaryIndexes());
		assertTrue(counts.getPrimaryIndexes().size() > 0);

		// test multi-state query
		final State utah = geographyRepository.getStateByAbbreviation("UT");
		assertNotNull(utah);

		params.getContainers().add(utah);
		params.getContainers().add(ohio);
		params.setFilter(null);
		counts = analysisRepository.getClassifiedCounts(params, State.class);
		assertNotNull(counts);
		assertNotNull(counts.getPrimaryIndexes());
		assertTrue(counts.getPrimaryIndexes().size() == 2);
	}

	/*
	public void testCountSerialize() throws Throwable {

		final AnalysisParameters params = new AnalysisParameters();

		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, 2007);
		c.set(Calendar.MONTH, Calendar.JULY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		params.setStartDate(c.getTime());

		Calendar endDate = (Calendar) c.clone();
		endDate.set(Calendar.MONTH, Calendar.AUGUST);
		params.setEndDate(endDate.getTime());

		params.setLocation(AnalysisLocation.HOME);

		State ohio = geographyRepository.getStateByAbbreviation("OH");
		params.setContainer(ohio);

		final Classifier classifier = analysisRepository.getClassifierByName("Infectious Disease Symptoms");
		assertNotNull(classifier);
		
		params.setClassifications(classifier.getClassifications());

		// Test hourly county-level counts for a State
		TimeSeriesCollection<? extends Geography, Classification, TimeSeriesEntry> counts = analysisRepository
				.getNormalizedCounts(params, TimeSeriesPeriod.DAY, County.class);
	
		final BufferedWriter output = new BufferedWriter(new FileWriter("/tmp/out.xml"));
		final Marshaller m = jaxbContext.createMarshaller();
		m.setProperty("jaxb.formatted.output", Boolean.TRUE);
		m.marshal(counts, output);
		
		output.close();
	}
	
	*/
	
	/*
	 * public void testSurveillanceXML() throws Throwable {
	 * 
	 * List<SurveillanceEvent> events =
	 * surveillanceRepository.getList(SurveillanceEvent.class);
	 * assertNotNull(events); assertTrue(events.size() > 0); SurveillanceEvent
	 * first = events.get(0); JAXB.marshal(first.getResult(), System.out);
	 * System.out.println(); }
	 */

	@SuppressWarnings("unused")
	private <T, C, E extends TimeSeriesEntry> void showCounts(TimeSeriesCollection<T, C> counts) throws Throwable {

		for (Map.Entry<T, Map<C, TimeSeries>> entry : counts.getMaps().entrySet()) {

			for (Map.Entry<C, TimeSeries> cmt : entry.getValue().entrySet()) {

				System.out.println("Aggregate: " + entry.getKey().toString() + " Classification: "
						+ cmt.getKey().toString());
				JAXB.marshal(cmt.getValue(), System.out);
				System.out.println();

			}
		}
	}

	@SuppressWarnings("unused")
	private <T, C, E extends TimeSeriesEntry> void showNorms(TimeSeriesCollection<T, C> counts) throws Throwable {

		for (Map.Entry<T, Map<C, TimeSeries>> entry : counts.getMaps().entrySet()) {

			for (Map.Entry<C, TimeSeries> cmt : entry.getValue().entrySet()) {

				System.out.println("Aggregate: " + entry.getKey().toString() + " Classification: "
						+ cmt.getKey().toString());
				jaxbContext.createMarshaller().marshal(cmt.getValue(), System.out);
				// JAXB.marshal(cmt.getValue(), System.out);
				System.out.println();

			}
		}
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
