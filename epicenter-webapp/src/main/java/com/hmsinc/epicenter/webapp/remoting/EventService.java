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
package com.hmsinc.epicenter.webapp.remoting;

import static com.hmsinc.epicenter.util.DateTimeUtils.formatDurationDays;
import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.checkPermission;
import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.isGeographyAccessible;
import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.isGlobalAdministrator;
import static com.hmsinc.epicenter.webapp.util.GeometryUtils.toGeometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.jfree.chart.axis.NumberAxis;
import org.joda.time.DateTime;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.normalization.PopulationRateNormalizer;
import com.hmsinc.ts4j.analysis.util.AnalysisUtils;
import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.DescriptiveAnalysisType;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;
import com.hmsinc.epicenter.service.data.DataQueryService;
import com.hmsinc.epicenter.service.discovery.AnalyzerDiscoveryService;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;
import com.hmsinc.ts4j.TimeSeriesNode;
import com.hmsinc.epicenter.util.DateTimeUtils;
import com.hmsinc.epicenter.webapp.chart.BarChart;
import com.hmsinc.epicenter.webapp.chart.ChartColor;
import com.hmsinc.epicenter.webapp.chart.ChartService;
import com.hmsinc.epicenter.webapp.chart.LineStyle;
import com.hmsinc.epicenter.webapp.chart.TimeSeriesChart;
import com.hmsinc.epicenter.webapp.data.QueryService;
import com.hmsinc.epicenter.webapp.dto.AnomalyDTO;
import com.hmsinc.epicenter.webapp.dto.AnomalyDetailsDTO;
import com.hmsinc.epicenter.webapp.dto.ListView;
import com.hmsinc.epicenter.webapp.util.GeometryUtils;
import com.hmsinc.epicenter.webapp.util.SortableKeyValuePair;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * @author shade
 * @version $Id: EventService.java 1825 2008-07-14 13:33:35Z steve.kondik $
 */
@RemoteProxy(name="EventService")
public class EventService extends AbstractRemoteService {

	@Resource
	private SurveillanceRepository surveillanceRepository;
	
	@Resource
	private WorkflowRepository workflowRepository;
	
	@Resource
	private ChartService chartService;
	
	@Resource
	private AnalyzerDiscoveryService analyzerDiscoveryService;
	
	@Resource
	private PopulationRateNormalizer populationRateNormalizer;
	
	@Resource
	private DataQueryService dataQueryService;

	@Resource
	private QueryService queryService;
	
	/**
	 * Gets events visible to a user, with paging and date constraints.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public ListView<AnomalyDTO> getEvents(boolean includeAll, DateTime startDate, DateTime endDate, Long geographyId, Integer offset, Integer numRows) {

		Validate.notNull(startDate, "Start date must be specified.");
		Validate.notNull(endDate, "End date must be specified.");
		
		final Geometry geo;
		if (geographyId == null) {
			if (isGlobalAdministrator(getPrincipal())) {
				geo = null;
			} else {
				geo = getPrincipal().getVisibleRegion().getGeometry();
			}
		} else {
			final Geography geog = geographyRepository.load(geographyId, Geography.class);
			Validate.notNull(geog, "Invalid geography: " + geographyId);
			Validate.isTrue(isGeographyAccessible(getPrincipal(), geog), "Geography is not accessible");
			geo = geog.getGeometry();
		}
				
		final DateTime adjustedStart = DateTimeUtils.toStartOfDay(startDate);
		final DateTime adjustedEnd = DateTimeUtils.toEndOfDay(endDate);
		final Integer eventCount = surveillanceRepository.getAnomalyCount(adjustedStart, adjustedEnd, includeAll, geo, toGeometry(getPrincipal().getAggregateOnlyVisibleRegion()));
		final List<Anomaly> allEvents = surveillanceRepository.getAnomalies(adjustedStart, adjustedEnd, includeAll, geo, toGeometry(getPrincipal().getAggregateOnlyVisibleRegion()), offset, numRows);

		logger.debug("Found {} events for {}", allEvents.size(), getPrincipal().getUsername());

		final ListView<AnomalyDTO> eventsView = new ListView<AnomalyDTO>(eventCount);
		final Set<Point> centroids = new HashSet<Point>();
		
		for (Anomaly anomaly : sortEvents(allEvents)) {
			eventsView.getItems().add(new AnomalyDTO(anomaly, getPrincipal()));
			centroids.add(anomaly.getGeography().getGeometry().getCentroid());
		}

		eventsView.getAttributes().put("bbox", GeometryUtils.getBoundingBox(centroids));

		return eventsView;
	}

	/**
	 * Gets the 10 most recent visible anomalies.
	 * 
	 * @param startDate
	 * @param endDate
	 * @param geographyId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public ListView<AnomalyDTO> getRecentEvents(DateTime startDate, DateTime endDate, Long geographyId, Integer offset, Integer numRows) {
		
		final Geometry geo;
		if (geographyId == null) {
			if (isGlobalAdministrator(getPrincipal())) {
				geo = null;
			} else {
				geo = getPrincipal().getVisibleRegion().getGeometry();
			}
		} else {
			final Geography geog = geographyRepository.load(geographyId, Geography.class);
			Validate.notNull(geog, "Invalid geography: " + geographyId);
			Validate.isTrue(isGeographyAccessible(getPrincipal(), geog), "Geography is not accessible");
			geo = geog.getGeometry();
		}
		
		final DateTime adjustedStart = DateTimeUtils.toStartOfDay(startDate);
		final DateTime adjustedEnd = DateTimeUtils.toEndOfDay(endDate);
		
		final List<Anomaly> events = surveillanceRepository.getAnomalies(adjustedStart, adjustedEnd, false, geo, toGeometry(getPrincipal().getAggregateOnlyVisibleRegion()), offset, numRows);
		final ListView<AnomalyDTO> listview = new ListView<AnomalyDTO>(surveillanceRepository.getAnomalyCount(adjustedStart, adjustedEnd, false, geo, toGeometry(getPrincipal().getAggregateOnlyVisibleRegion())));
		
		for (Anomaly anomaly : events) {
			listview.getItems().add(new AnomalyDTO(anomaly, getPrincipal()));
		}
		return listview;
		
	}
	
	/**
	 * @param eventId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public AnomalyDTO getEvent(Long eventId) {
		
		Validate.notNull(eventId);

		logger.debug("Loading event id: " + eventId);
		final Anomaly anomaly = surveillanceRepository.load(eventId, Anomaly.class);
		Validate.notNull(anomaly, "Invalid event id " + eventId);

		checkPermission(getPrincipal(), anomaly);
		return new AnomalyDTO(anomaly, getPrincipal());
	}
	
	/**
	 * Gets an event by id. Will throw ACCESS_DENIED if the event is not visible
	 * for the user.
	 * 
	 * @param id
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public AnomalyDetailsDTO getEventDetail(Long eventId) {

		Validate.notNull(eventId);

		logger.debug("Loading event id: " + eventId);
		final Anomaly anomaly = surveillanceRepository.load(eventId, Anomaly.class);
		Validate.notNull(anomaly, "Invalid event id " + eventId);

		checkPermission(getPrincipal(), anomaly);
		
		final String associatedAlgorithmName = analyzerDiscoveryService.getUnivariateAnalyzer(anomaly.getMethod().getName()).getAssociatedAnalyzer().getName();
		final AnomalyDetailsDTO dto = new AnomalyDetailsDTO(anomaly, getPrincipal(), associatedAlgorithmName);
		dto.setCurrentValue(queryService.getCurrentValueForAnomaly(anomaly));
		return dto;
	}
	
	/**
	 * @param eventId
	 * @param type
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public String getEventChart(final Long eventId, final SurveillanceResultType type) {
		
		Validate.notNull(eventId);

		logger.debug("Loading event id: " + eventId);
		final Anomaly anomaly = surveillanceRepository.load(eventId, Anomaly.class);
		Validate.notNull(anomaly, "Invalid event id " + eventId);

		checkPermission(getPrincipal(), anomaly);
		
		final SurveillanceResultType actualType = SurveillanceResultType.POPULATION.equals(type) ? SurveillanceResultType.ACTUAL : type;
		
		Validate.isTrue(anomaly.getResult().getResults().containsKey(actualType), "SurveillanceResult " + actualType + " is not available.");
		
		TimeSeries ts = anomaly.getResult().getResults().get(actualType);
				
		if (SurveillanceResultType.POPULATION.equals(type)) {
			final Long population = anomaly.getGeography().getPopulation() == null ? geographyRepository.inferPopulation(anomaly.getGeography()) : anomaly.getGeography().getPopulation();
			Validate.notNull(population, "Could not determine population for: " + anomaly.getGeography().getDisplayName());
			ts = populationRateNormalizer.normalize(ts, population.intValue());
		}
		
		final TimeSeriesChart chart = new TimeSeriesChart();
		chart.setYLabel(type.getDescription());
		
		chart.add(AnalysisService.getAttributeLabel(anomaly.getAnalysisParameters()), ts, ChartColor.VALUE.getColor(), LineStyle.SOLID);
		
		if (! SurveillanceResultType.POPULATION.equals(type)) {
			chart.add(anomaly.getMethod().getName(), ts, ResultType.THRESHOLD, ChartColor.THRESHOLD.getColor(), LineStyle.SOLID);
		}
		
		final TimeSeries current = queryService.getCurrentTimeSeriesForAnomaly(anomaly, type);

		if (current != null) {
			chart.add("Current Value", current,	ChartColor.VALUE.getColor(), LineStyle.DASHED);
		}
		
		return chartService.getChartURL(chart);
		
	}
	

	/**
	 * @param eventId
	 * @param state
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public void updateEventState(Long eventId, Long dispositionId) {

		Validate.notNull(eventId, "Event id must be specified.");
		Validate.notNull(dispositionId, "Event disposition id must be specified.");

		final Event event = workflowRepository.load(eventId, Event.class);
		Validate.notNull(event, "Invalid event id: " + eventId);
		checkPermission(getPrincipal(), event);
		
		final EventDisposition disposition = workflowRepository.load(dispositionId, EventDisposition.class);
		Validate.notNull(disposition, "Invalid event disposition id: " + dispositionId);
		
		event.setDisposition(disposition);

		workflowRepository.save(event);
	}
	
	/**
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public String getDateOfOldestAnomaly() {
		String ret = "7 days";
		final Geometry v = isGlobalAdministrator(getPrincipal()) ? null : getPrincipal().getVisibleRegion().getGeometry();
		final DateTime c = surveillanceRepository.getDateOfOldestAnomaly(v, toGeometry(getPrincipal().getAggregateOnlyVisibleRegion()));
		if (c != null) {
			ret = formatDurationDays(c, new DateTime());
		}
		
		return ret;
	}
	
	/**
	 * @param eventId
	 * @param analysisType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RemoteMethod
	@Transactional(readOnly = true)
	public <G extends Geography, A extends QueryableAttribute> String getDescriptiveAnalysisChart(Long eventId, DescriptiveAnalysisType analysisType, SurveillanceResultType resultType) {
	
		Validate.notNull(analysisType, "Analysis type must be specified.");
		Validate.notNull(eventId, "Event id must be specified.");
		Validate.notNull(resultType, "Result type must be specified.");
		
		final Event event = workflowRepository.load(eventId, Event.class);
		Validate.notNull(event, "Invalid event id: " + eventId);
		checkPermission(getPrincipal(), event);
		Validate.isTrue(event instanceof Anomaly, "Event is not an anomaly.");
		
		final Anomaly anomaly = (Anomaly)event;
		final AnalysisParameters analysisParameters = anomaly.getAnalysisParameters();	
		analysisParameters.setStartDate(analysisParameters.getEndDate().minusDays(3));
		
		logger.debug("Parameters: {}", analysisParameters);
		
		// FIXME: This casting mess is very annoying.  We need to fix this. -sk
		final Class<G> gtype;
		final Class<A> atype;
		
		if (Geography.class.isAssignableFrom(analysisType.getAggregateAttribute())) {
			gtype = (Class<G>)analysisType.getAggregateAttribute();
			atype = (Class<A>)Classification.class;
		} else {
			gtype = (Class<G>)ModelUtils.getRealClass(anomaly.getGeography());
			atype = (Class<A>)analysisType.getAggregateAttribute();
		}
		
		if (DescriptiveAnalysisType.BY_ZIPCODE.equals(analysisType)) {
			analysisParameters.setLocation(AnalysisLocation.HOME);
		}
		
		final TimeSeriesCollection<G, A> ts = dataQueryService.query(analysisParameters, gtype, atype);
				
		Validate.notNull(ts, "Unable to get timeseries!");
			
		final BarChart chart = new BarChart();
		
		final Set<SortableKeyValuePair> pairs = new TreeSet<SortableKeyValuePair>();
		
		if (Geography.class.isAssignableFrom(analysisType.getAggregateAttribute())) {
			
			for (TimeSeriesNode node : ts.getTimeSeriesNodes()) {
				double value = node.getTimeSeries().getValue(anomaly.getAnalysisTimestamp());
				if (SurveillanceResultType.NORMALIZED.equals(resultType)) {
					value = (value / anomaly.getObservedValue()) * 100;
				} else if (SurveillanceResultType.POPULATION.equals(resultType)) {
					
					final Geography g = (Geography)node.getPrimaryIndex();
					final Long population = g.getPopulation() == null ? geographyRepository.inferPopulation(g) : g.getPopulation();
					value = AnalysisUtils.calculatePopulationRate(value, population.doubleValue());
				}
				pairs.add(new SortableKeyValuePair(((Geography)node.getPrimaryIndex()).getName(), value));
			}
			
		} else {
			
			if (ts.getPrimaryIndexes().contains(anomaly.getGeography())) {

				final Map<A, TimeSeries> map = ts.get((G) anomaly.getGeography());

				for (Map.Entry<A, TimeSeries> entry : map.entrySet()) {

					double value = entry.getValue().getValue(anomaly.getAnalysisTimestamp());

					if (SurveillanceResultType.NORMALIZED.equals(resultType)) {
						value = (value / anomaly.getObservedValue()) * 100;
					} else if (SurveillanceResultType.POPULATION.equals(resultType)) {

						final Long population = anomaly.getGeography().getPopulation() == null ? geographyRepository
								.inferPopulation(anomaly.getGeography()) : anomaly.getGeography().getPopulation();
								
						Validate.notNull(population, "Could not determine population for: " + anomaly.getGeography().getDisplayName());
						
						value = AnalysisUtils.calculatePopulationRate(value, population.doubleValue());
					}

					pairs.add(new SortableKeyValuePair(entry.getKey().getName(), value));

				}

			} else {
				logger.warn("Unable to generate descriptive analysis chart for: {}  [no data found]", anomaly.getGeography());
			}
		
		}
		
		int i = 0;
		for (SortableKeyValuePair pair : pairs) {
			if (pair.getValue() > 0) {
				chart.add(pair.getValue(), analysisType.getDescription() + (pairs.size() > 5 ? " - Top 5" : ""), pair.getKey());
				i++;
				if (i > 4) {
					break;
				}
			}
		}
		
		chart.setYLabel(resultType.getDescription());
		if (!SurveillanceResultType.NORMALIZED.equals(resultType)) {
			chart.setRangeTickUnits(NumberAxis.createIntegerTickUnits());
		}
		return chartService.getChartURL(chart);
		
		
	}
	
	/**
	 * Very annoying multi-level sort. 
	 * 
	 * @param allEvents
	 * @return
	 */
	private List<Anomaly> sortEvents(final List<Anomaly> allEvents) {

		// Group the events by geography (the query will return events ordered by timestamp descending)
		final Map<String, List<Anomaly>> sm = new TreeMap<String, List<Anomaly>>();
		for (Anomaly a : allEvents) {
			if (!sm.containsKey(a.getGeography().getDisplayName())) {
				sm.put(a.getGeography().getDisplayName(), new ArrayList<Anomaly>());
			}
			sm.get(a.getGeography().getDisplayName()).add(a);
		}

		// This is ugly, but gets us entries sorted by date
		final Comparator<Map.Entry<String, List<Anomaly>>> entryComparator = new Comparator<Map.Entry<String, List<Anomaly>>>() {
			
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			public int compare(Entry<String, List<Anomaly>> lhs, Entry<String, List<Anomaly>> rhs) {
				final DateTime leftDate = lhs.getValue().get(0).getAnalysisTimestamp();
				final DateTime rightDate = rhs.getValue().get(0).getAnalysisTimestamp();
				return rightDate.compareTo(leftDate);
			}

		};

		final List<Map.Entry<String, List<Anomaly>>> entries = new ArrayList<Map.Entry<String, List<Anomaly>>>(sm.entrySet());
				
		Collections.sort(entries, entryComparator);
	
		final List<Anomaly> sortedEvents = new ArrayList<Anomaly>();
		for (Map.Entry<String, List<Anomaly>> ssm : entries) {
			for (Anomaly a : ssm.getValue()) {
				sortedEvents.add(a);
			}
		}

		return sortedEvents;
	}
	
}
