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
import java.awt.BasicStroke;
import java.awt.Color;

import javax.annotation.Resource;

import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.data.time.Day;
import org.joda.time.DateTime;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.ts4j.analysis.forecasting.ForecastingAlgorithm;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.epicenter.webapp.chart.ChartColor;
import com.hmsinc.epicenter.webapp.chart.ChartService;
import com.hmsinc.epicenter.webapp.chart.LineStyle;
import com.hmsinc.epicenter.webapp.chart.TimeSeriesChart;
import com.hmsinc.epicenter.webapp.data.QueryService;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;

/**
 * @author shade
 * @version $Id: ForecastingService.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
@RemoteProxy(name = "ForecastingService")
public class ForecastingService extends AbstractRemoteService {

	@Resource
	private ForecastingAlgorithm waveletSeasonalTrendForecaster;
	
	@Resource
	private QueryService queryService;

	@Resource
	private ChartService chartService;
	
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public String getSeasonalTrendChart(final AnalysisParametersDTO paramsDTO) {
		
		final AnalysisParameters params = convertParameters(paramsDTO);
				
		// 60 day window
		final DateTime windowStart = params.getEndDate().minusDays(59);
		
		// Set the start date back 1 year + 60 days
		params.setStartDate(windowStart.minusYears(1).minusDays(59));
		
		final TimeSeriesChart chart = new TimeSeriesChart();
		
		final TimeSeries ts = queryService.queryForTimeSeries(params, paramsDTO.getAlgorithmName(), null);
		
		if (ts != null) {
			
			final TimeSeries forecast = waveletSeasonalTrendForecaster.process(ts.after(windowStart), ts.before(windowStart.minusDays(1)), null);
			final TimeSeries known = forecast.before(params.getEndDate());
			final TimeSeries predicted = forecast.after(params.getEndDate().plusDays(1));

			chart.addBand("70% Confidence Prediction", predicted, ResultType.LOWER_BOUND_70, ResultType.UPPER_BOUND_70, new Color(0x0072bf), new Color(0x0072bf));
			chart.addBand("80% Confidence Prediction", predicted, ResultType.LOWER_BOUND_80, ResultType.UPPER_BOUND_80, new Color(0x0099ff), new Color(0x0099ff));
			chart.addBand("90% Confidence Prediction", predicted, ResultType.LOWER_BOUND_90, ResultType.UPPER_BOUND_90, new Color(0x3fb2ff), new Color(0x3fb2ff));
			chart.addBand("95% Confidence Prediction", predicted, ResultType.LOWER_BOUND_95, ResultType.UPPER_BOUND_95, new Color(0xbfe5ff), new Color(0xbfe5ff));
		
			chart.add("Actual Value", known, ChartColor.VALUE.getColor(), LineStyle.DOTTED);
			chart.add("Actual Trend", known, ResultType.TREND, ChartColor.VALUE.getColor(), LineStyle.THICK);
					
			final DateTime knownFirst = known.first().getTime();
			final DateTime predictedFirst = predicted.first().getTime();

			final Marker marker = new IntervalMarker(
					new Day(knownFirst.getDayOfMonth(), knownFirst.getMonthOfYear(), knownFirst.getYear()).getFirstMillisecond(),
					new Day(predictedFirst.getDayOfMonth(), predictedFirst.getMonthOfYear(), predictedFirst.getYear()).getFirstMillisecond(),
					Color.LIGHT_GRAY, new BasicStroke(2.0f), null, null, 1.0f);
			marker.setAlpha(0.3f);
			chart.getMarkers().add(marker);
			chart.setYLabel(params.getDataRepresentation().getDisplayName());
			chart.setAlwaysScaleFromZero(false);
		}
		
		return chartService.getChartURL(chart);
		
	}
}
