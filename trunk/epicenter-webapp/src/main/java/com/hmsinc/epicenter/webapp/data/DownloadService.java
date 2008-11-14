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
package com.hmsinc.epicenter.webapp.data;

import static com.hmsinc.epicenter.util.DateTimeUtils.deltaDays;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Properties;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.health.CodedVisit;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.Registration;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;
import com.hmsinc.epicenter.webapp.remoting.AbstractRemoteService;
import com.hmsinc.epicenter.webapp.util.DateTimePropertyEditor;
import com.hmsinc.epicenter.webapp.util.EncodedPropertiesPropertyEditor;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesEntry;
import com.hmsinc.ts4j.TimeSeriesPeriod;
import com.hmsinc.ts4j.analysis.ResultType;

/**
 * Downloads cases and counts as a CSV.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: DownloadService.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
@Controller
public class DownloadService extends AbstractRemoteService {

	private static final String CONTENT_TYPE = "application/vns.ms-excel";

	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");

	private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT = DateTimeFormat.forPattern("MMddyyyy");

	private static final long PAGE_SIZE = 250L;

	@Resource
	private QueryService queryService;
	

	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	@RequestMapping(value = "/download/cases", method = RequestMethod.POST)
	public void downloadCases(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		final AnalysisParametersDTO queryParams = bind(request);
		final AnalysisParameters params = convertParameters(queryParams);
		logger.debug(params.toString());

		SpatialSecurity.checkAggregateOnlyAccess(getPrincipal(), params.getContainer());
		
		final String filename = new StringBuilder("cases-").append(params.getContainer().getName()).append("-").append(
				FILE_TIMESTAMP_FORMAT.print(params.getStartDate())).append("-").append(FILE_TIMESTAMP_FORMAT.print(params.getEndDate()))
				.append(".csv").toString();

		response.setContentType(CONTENT_TYPE);
		response.addHeader("cache-control", "must-revalidate");
		response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

		final OutputStream stream = response.getOutputStream();
		final CSVPrinter csv = new CSVPrinter(stream);

		final String[] header = new String[] { "Interaction Date", "Age", "Gender", "Zipcode", "Facility", "Visit Number", "Reason", "Classifications" };
		csv.println(header);
		
		// Stream the file to the client by paging thru the database
		long offset = 0;
		final Long size = analysisRepository.getCasesCount(params);

		while (offset <= size) {

			final Collection<? extends Interaction> admits = analysisRepository.getCases(params, offset, PAGE_SIZE);

			for (Interaction a : admits) {

				final StringBuilder clz = new StringBuilder();
				for (Classification cc : a.getClassifications()) {
					if (params.getClassifications() == null || params.getClassifications().size() == 0 || params.getClassifications().contains(cc)) {
						if (clz.length() > 0) {
							clz.append(", ");
						}
						clz.append(cc.getCategory());
					}
				}

				if (params.getClassifications() == null || params.getClassifications().size() == 0 || clz.length() > 0) {
					
					String reason = null;
					if (a instanceof Registration) {
						reason = ((Registration)a).getReason();
					}
					
					String visitNumber = null;
					if (a instanceof CodedVisit) {
						visitNumber = ((CodedVisit)a).getVisitNumber();
					}
					
					final String[] entry = new String[] {
							DATE_FORMAT.print(a.getInteractionDate()),
							ObjectUtils.toString(a.getAgeAtInteraction()),
							a.getPatientDetail().getGender() == null ? "" : a.getPatientDetail().getGender()
									.getAbbreviation(), ObjectUtils.toString(a.getPatientDetail().getZipcode()),
							filterFacility(a.getPatient().getFacility()),
							ObjectUtils.toString(visitNumber), ObjectUtils.toString(reason),
							ObjectUtils.toString(clz) };

					csv.println(entry);

					// Evict the object from the cache since we're done with it.
					analysisRepository.evict(a);

				}

			}

			stream.flush();

			offset = offset + PAGE_SIZE;
		}

		stream.flush();

	}

	private String filterFacility(final Facility f) {
		
		String ret = "(unknown)";
		
		if (f != null && f.getZipcode() != null) {
			final Zipcode facilityZip = geographyRepository.getGeography(f.getZipcode(), Zipcode.class);
		
			ret = f.getName();
			if (facilityZip != null) {
				if (!SpatialSecurity.isGeographyAccessible(getPrincipal(), facilityZip)) {
					ret = "(outside visible region)";
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	@Transactional(readOnly = true)
	@RequestMapping(value = "/download/counts", method = RequestMethod.POST)
	public void downloadCounts(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

		final AnalysisParametersDTO queryParams = bind(request);
		logger.debug("Query: {}", queryParams);
		
		final AnalysisParameters params = convertParameters(queryParams);
		logger.debug("Parameters: {}", params);

		params.setPeriod(deltaDays(params.getStartDate(), params.getEndDate()) > 0 ? TimeSeriesPeriod.DAY
				: TimeSeriesPeriod.HOUR);

		TimeSeries ts = queryService.queryForTimeSeries(params, queryParams.getAlgorithmName(), queryParams.getAlgorithmProperties());
		
		final String filename = new StringBuilder("counts-").append(params.getContainer().getName()).append("-").append(
				FILE_TIMESTAMP_FORMAT.print(params.getStartDate())).append("-").append(FILE_TIMESTAMP_FORMAT.print(params.getEndDate()))
				.append(".csv").toString();
		
		response.setContentType(CONTENT_TYPE);
		response.addHeader("cache-control", "must-revalidate");
		response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		
		final CSVPrinter csv = new CSVPrinter(response.getOutputStream());
		final String[] header = new String[] { "Date", "Classification", "Count", queryParams.getAlgorithmName() == null ? "" : (queryParams.getAlgorithmName() + " Threshold") };
		csv.println(header);
		
		final StringBuilder cs = new StringBuilder();
		for (Classification c : params.getClassifications()) {
			if (cs.length() > 0) {
				cs.append(", ");
			}
			cs.append(c.getCategory());
		}
		
		if (ts != null) {
			for (TimeSeriesEntry entry : ts) {
				final String[] line = new String[] { DATE_FORMAT.print(entry.getTime()), cs.toString(),
						String.valueOf(entry.getValue()),
						queryParams.getAlgorithmName() == null ? "" : entry.getProperty(ResultType.THRESHOLD) };
				csv.println(line);
			}
		}
	}
	
	/**
	 * @param request
	 * @return
	 */
	private AnalysisParametersDTO bind(final HttpServletRequest request) {
		
		final AnalysisParametersDTO queryParams = new AnalysisParametersDTO();
		
		final ServletRequestDataBinder binder = new ServletRequestDataBinder(queryParams);
		binder.registerCustomEditor(DateTime.class, new DateTimePropertyEditor());
		binder.registerCustomEditor(Properties.class, new EncodedPropertiesPropertyEditor());
		binder.bind(request);

		Validate.notNull(queryParams, "No parameters were specified");

		return queryParams;
	}
}
