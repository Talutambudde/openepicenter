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

import static com.hmsinc.epicenter.util.DateTimeUtils.deltaDays;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.univariate.DescriptiveUnivariateAnalyzer;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.DataConditioning;
import com.hmsinc.epicenter.model.analysis.DataRepresentation;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyType;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.epicenter.service.data.DataQueryService;
import com.hmsinc.epicenter.service.discovery.AnalyzerDiscoveryService;
import com.hmsinc.epicenter.spatial.render.SpatialScanResult;
import com.hmsinc.epicenter.spatial.service.SpatialScanService;
import com.hmsinc.epicenter.spatial.util.GoogleProjection;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesCollection;
import com.hmsinc.ts4j.TimeSeriesPeriod;
import com.hmsinc.epicenter.webapp.chart.ChartColor;
import com.hmsinc.epicenter.webapp.chart.ChartService;
import com.hmsinc.epicenter.webapp.chart.ChartType;
import com.hmsinc.epicenter.webapp.chart.LineStyle;
import com.hmsinc.epicenter.webapp.chart.TimeSeriesChart;
import com.hmsinc.epicenter.webapp.data.QueryService;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;
import com.hmsinc.epicenter.webapp.dto.KeyValueDTO;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Handles data analysis in the UI.
 * 
 * TODO: Move most of this to a DWR converter instead of using DTOs.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AnalysisService.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@RemoteProxy(name = "AnalysisService")
public class AnalysisService extends AbstractRemoteService {

	@Resource
	private QueryService queryService;

	@Resource
	private DataQueryService dataQueryService;
	
	@Resource
	private ChartService chartService;

	@Resource
	private AnalyzerDiscoveryService analyzerDiscoveryService;

	@Resource
	private SpatialScanService spatialScanService;
	
	/**
	 * Generates a TimeSeries of total counts for the given geography.
	 * 
	 * @param <T>
	 * @param start
	 * @param end
	 * @param geoType
	 * @param geoId
	 * @param patientLocation
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	@SuppressWarnings("unchecked")
	public String getTotalChart(final AnalysisParametersDTO paramsDTO) {

		final AnalysisParameters params = convertParameters(paramsDTO);

		// Set the period based on the date range:
		params.setPeriod(deltaDays(params.getStartDate(), params.getEndDate()) > 0 ? TimeSeriesPeriod.DAY
				: TimeSeriesPeriod.HOUR);

		final Class<? extends Geography> aggregateType = (Class<? extends Geography>) ModelUtils.getRealClass(params.getContainer());
		final Map<? extends Geography, TimeSeries> ts = dataQueryService.queryTotals(params, aggregateType);
		Validate.notNull(ts, "Unable to get timeseries data!");

		final TimeSeries totals = ts.get(params.getContainer());

		final TimeSeriesChart chart = new TimeSeriesChart();
		chart.setType(ChartType.TIMESERIES_BAR);
		chart.setYLabel("Number of Visits");

		if (totals != null) {
			chart.add((params.getDataType() == null ? "Total Visits" : params.getDataType().getName()), totals);
		}

		return chartService.getChartURL(chart);
	}

	/**
	 * @param paramsDTO
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	@SuppressWarnings("unchecked")
	public String getSummaryChart(final AnalysisParametersDTO paramsDTO) {

		final AnalysisParameters analysisParameters = convertParameters(paramsDTO);
		final Class<? extends Geography> aggregateType = (Class<? extends Geography>) ModelUtils
				.getRealClass(analysisParameters.getContainer());

		final TimeSeriesCollection<? extends Geography, Classification> tsc = dataQueryService.query(analysisParameters, aggregateType);

		final TimeSeriesChart chart = new TimeSeriesChart();
		chart.setType(ChartType.MOUNTAIN);
		chart.setYLabel("Number of Visits");

		if (tsc.getPrimaryIndexes().contains(analysisParameters.getContainer())) {

			final Map<Classification, TimeSeries> tsm = tsc.getMaps().get(analysisParameters.getContainer());

			// Sort the map by the max value in the timeseries.
			final ArrayList<Classification> keys = new ArrayList<Classification>();
			keys.addAll(tsm.keySet());

			Collections.sort(keys, new Comparator<Classification>() {
				public int compare(Classification o1, Classification o2) {
					return new CompareToBuilder().append(tsm.get(o2).getMaxValue(), tsm.get(o1).getMaxValue())
							.toComparison();
				}
			});

			int i = 0;

			for (Classification c : keys) {
				if (!"Other".equals(c.getName())) {
					chart.add(c.getName(), tsm.get(c));
					i++;
					if (i > 6) {
						break;
					}
				}
			}
		}

		return chartService.getChartURL(chart);
	}

	/**
	 * Builds a timeseries for the given criteria.
	 * 
	 * @param classifierId
	 * @param classifierCategory
	 * @param start
	 * @param end
	 * @param geoType
	 * @param geoId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public String getTimeSeriesChart(final AnalysisParametersDTO paramsDTO) {

		final AnalysisParameters params = convertParameters(paramsDTO);
		final Properties analyzerProperties = paramsDTO.getAlgorithmProperties();

		// Set the period based on the date range:
		params.setPeriod(deltaDays(params.getStartDate(), params.getEndDate()) > 0 ? TimeSeriesPeriod.DAY
				: TimeSeriesPeriod.HOUR);

		TimeSeries ts = queryService.queryForTimeSeries(params, paramsDTO.getAlgorithmName(), analyzerProperties);
		TimeSeriesChart chart = new TimeSeriesChart();
		final List<XYAnnotation> events = new ArrayList<XYAnnotation>();

		if (ts != null) {

			chart.setYLabel(params.getDataRepresentation().getDisplayName());

			final String label = getAttributeLabel(params);
			if (!DataConditioning.NONE.equals(params.getDataConditioning())) {

				chart.add(label + " (Conditioned)", ts, ChartColor.VALUE.getColor(), LineStyle.SOLID);
				chart.add("Actual Value", ts, ResultType.RAW, ChartColor.VALUE.getColor(), LineStyle.DASHED);
				
			} else {
				
				chart.add(label, ts, ChartColor.VALUE.getColor(), LineStyle.SOLID);
				if (DataRepresentation.ACTUAL.equals(params.getDataRepresentation())) {
					chart.setRangeTickUnits(NumberAxis.createIntegerTickUnits());
				}
			}

			if (paramsDTO.getAlgorithmName() != null) {
				chart.add(paramsDTO.getAlgorithmName(), ts, ResultType.THRESHOLD, ChartColor.THRESHOLD.getColor(),
						LineStyle.SOLID);

				/*
				 * for (TimeSeriesEntry entry : ts) { if (entry.getValue() >=
				 * entry.getDoubleProperty(ResultType.THRESHOLD) &&
				 * entry.getValue() > 10) { final String value =
				 * String.valueOf(FormatUtils.round(entry.getValue(), 1)); final
				 * XYPointerAnnotation annotation = new
				 * XYPointerAnnotation(value, entry.getTime().getMillis(),
				 * entry.getValue(), 3.0 * Math.PI / 4.0 );
				 * annotation.setPaint(ChartColor.THRESHOLD.getColor());
				 * events.add(annotation); } }
				 */
			}
		}

		return chartService.getChartURL(chart, events);
	}

	/**
	 * @param paramsDTO
	 * @param feature
	 * @param width
	 * @param height
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public SpatialScanResult spatialScan(final AnalysisParametersDTO paramsDTO, 
			final double minX, final double minY, final double maxX, final double maxY, 
			final GeographyType feature, final int width, final int height) {

		Validate.notNull(feature, "Feature type is required.");
		
		final AnalysisParameters analysisParameters = convertParameters(paramsDTO);
		final Envelope bounds = new Envelope(minX, maxX, minY, maxY);
		Validate.notNull(bounds, "Invalid bbox.");
		
		analysisParameters.setFilter(bounds);
		
		return spatialScanService.scan(analysisParameters, feature.getGeoClass(), GoogleProjection.GOOGLE_MERCATOR, width, height, false, false);
		
	}
	
	/**
	 * @param analysisParameters
	 * @return
	 */
	public static String getAttributeLabel(final AnalysisParameters analysisParameters) {

		final String ageLabel;
		final String genderLabel;

		if (analysisParameters.getAgeGroups() == null || analysisParameters.getAgeGroups().size() == 0) {
			ageLabel = "All Ages";
		} else if (analysisParameters.getAgeGroups().size() == 1) {
			ageLabel = analysisParameters.getAgeGroups().iterator().next().getName();
		} else {
			ageLabel = "Custom Age";
		}

		if (analysisParameters.getGenders() == null || analysisParameters.getGenders().size() == 0) {
			genderLabel = "All Genders";
		} else if (analysisParameters.getGenders().size() == 1) {
			genderLabel = analysisParameters.getGenders().iterator().next().getName();
		} else {
			genderLabel = "Custom Gender";
		}

		return StringUtils.join(new String[] { ageLabel, genderLabel }, "+");
	}

	/**
	 * Gets all available algorithms.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getAlgorithms() {
		final Set<KeyValueDTO> ret = new TreeSet<KeyValueDTO>();
		for (String name : analyzerDiscoveryService.getUnivariateAnalyzers()) {
			ret.add(new KeyValueDTO(name, name));
		}
		return prependNoneCategory(ret, "No Analysis");
	}

	/**
	 * Gets all available algorithms.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getPosteriorAlgorithms() {
		final Set<KeyValueDTO> ret = new TreeSet<KeyValueDTO>();
		for (String name : analyzerDiscoveryService.getUnivariateProbabilityAnalyzers()) {
			ret.add(new KeyValueDTO(name, name));
		}
		return ret;
	}

	/**
	 * @param name
	 * @return
	 */
	@Secured("ROLE_USER")
	@RemoteMethod
	public DescriptiveUnivariateAnalyzer getAnalyzerDetails(final String name) {

		Validate.notNull(name, "Analyzer name is required.");

		final DescriptiveUnivariateAnalyzer analyzer;
		if (analyzerDiscoveryService.getUnivariateAnalyzers().contains(name)) {
			analyzer = analyzerDiscoveryService.getUnivariateAnalyzer(name);
		} else if (analyzerDiscoveryService.getUnivariateProbabilityAnalyzers().contains(name)) {
			analyzer = analyzerDiscoveryService.getUnivariateProbabilityAnalyzer(name);
		} else {
			throw new IllegalArgumentException("No such analyzer: " + name);
		}
		return analyzer;
	}

	/**
	 * @param name
	 * @param parameters
	 * @return
	 */
	@Secured("ROLE_USER")
	@RemoteMethod
	public List<Integer> getEffectiveTrainingPeriods(final String name, final Properties parameters) {
		
		Validate.notNull(name, "Analyzer name is required.");

		final DescriptiveUnivariateAnalyzer analyzer;
		if (analyzerDiscoveryService.getUnivariateAnalyzers().contains(name)) {
			analyzer = analyzerDiscoveryService.getUnivariateAnalyzer(name);
		} else if (analyzerDiscoveryService.getUnivariateProbabilityAnalyzers().contains(name)) {
			analyzer = analyzerDiscoveryService.getUnivariateProbabilityAnalyzer(name);
		} else {
			throw new IllegalArgumentException("No such analyzer: " + name);
		}
		
		return analyzer.getEffectiveTrainingPeriods(parameters);
	}
}
