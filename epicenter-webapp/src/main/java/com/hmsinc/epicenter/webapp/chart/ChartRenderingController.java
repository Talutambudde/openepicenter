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
package com.hmsinc.epicenter.webapp.chart;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.Validate;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Renders cached charts as PNG images.
 * 
 * @author shade
 * @version $Id: ChartRenderingController.java 1059 2008-02-21 19:30:25Z steve.kondik $
 */
@Controller
public class ChartRenderingController {

	@Resource
	private Cache chartCache;

	/**
	 * @param id
	 * @param width
	 * @param height
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping("/chart")
	public void getChart(@RequestParam("id") final String id, @RequestParam("width") final int width, @RequestParam("height") final int height, final HttpServletResponse response) throws IOException {

		Validate.notNull(id, "No chart specified.");
		Validate.notNull(width, "No width specified.");
		Validate.notNull(height, "No height specified.");
		
		final Element e = chartCache.get(id);
		Validate.notNull(e, "Invalid chart id " + id);

		final JFreeChart chart = (JFreeChart) e.getObjectValue();
		Validate.notNull(chart, "Chart was null!");

		response.setContentType("image/png");
		response.addHeader("cache-control", "must-revalidate");

		ChartUtilities.writeChartAsPNG(response.getOutputStream(), chart, width, height);
				
	}
	
}
