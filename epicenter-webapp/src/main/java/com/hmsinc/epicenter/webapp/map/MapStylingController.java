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
package com.hmsinc.epicenter.webapp.map;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Properties;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.spatial.service.SpatialScanService;
import com.hmsinc.epicenter.spatial.util.GoogleProjection;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;
import com.hmsinc.epicenter.webapp.remoting.AbstractRemoteService;
import com.hmsinc.epicenter.webapp.util.DateTimePropertyEditor;
import com.hmsinc.epicenter.webapp.util.EncodedPropertiesPropertyEditor;

/**
 * Creates dynamic map styles that are applied by Geoserver to map overlays.
 * Works as an interface between Geoserver and our algorithms.
 * 
 * @author Olek Poplavsky
 * @version $Id: MapStylingController.java 1801 2008-07-02 15:08:37Z steve.kondik $
 */
@Controller
public class MapStylingController extends AbstractRemoteService {
	
	private static final String SLD_CONTENT_TYPE = "text/xml";
	
	private static final String PNG_CONTENT_TYPE = "image/png";
	
	@Resource
	private StyleBuilder styleBuilder;

	@Resource
	private SpatialScanService spatialScanService;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.HttpRequestHandler#handleRequest(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Transactional(readOnly = true)
	@RequestMapping("/map-style")
	public void handleMapStyleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		final StyleParameters styleParameters = parse(request);
		
		response.setContentType(SLD_CONTENT_TYPE);
		response.addHeader("cache-control", "must-revalidate");

    	styleBuilder.build(response.getOutputStream(), styleParameters);

	}

	@Transactional(readOnly = true)
	@RequestMapping("/spatial-scan")
	public void handleSpatialScanRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		final StyleParameters styleParameters = parse(request);
		
		response.setContentType(PNG_CONTENT_TYPE);
		response.addHeader("cache-control", "must-revalidate");
		
		final BufferedImage image = spatialScanService.scan(styleParameters.getParameters(), styleParameters.getGeographyClass(), GoogleProjection.GOOGLE_MERCATOR, 800, 600, false, false).getImage();
		if (image == null) {
			logger.debug("No image to render: {}", styleParameters);
		} else {
			ImageIO.write(image, "png", response.getOutputStream());
		}
	}
	

	/**
	 * @param request
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	private StyleParameters parse(HttpServletRequest request) throws ServletException, IOException {
		
		final AnalysisParametersDTO queryParams = new AnalysisParametersDTO();
		
		final ServletRequestDataBinder binder = new ServletRequestDataBinder(queryParams);
		binder.registerCustomEditor(DateTime.class, new DateTimePropertyEditor());
		binder.registerCustomEditor(Properties.class, new EncodedPropertiesPropertyEditor());
		
		binder.bind(request);
		Validate.notNull(queryParams, "No parameters were specified");

		final AnalysisParameters params = convertParameters(queryParams);
		Validate.notNull(params);
				
		final StyleParameters styleParameters = new StyleParameters(request, params, queryParams, getPrincipal());
		logger.trace("Parameters: {}", styleParameters);
		
		return styleParameters;
	}
}
