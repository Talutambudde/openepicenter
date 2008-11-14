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
package com.hmsinc.epicenter.webapp.util;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.slf4j.*;

import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.AbstractProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter for Acegi to handle AJAX-style logins with a JSON response.
 * 
 * @author <a href="http://www.jroller.com/sjivan/entry/ajax_based_login_using_aceci">Sanjiv Jivan</a>
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: SpringSecurityAjaxFilter.java 1568 2008-04-18 13:08:44Z steve.kondik $
 */
public class SpringSecurityAjaxFilter extends OncePerRequestFilter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (isAjaxRequest(request)) {
					
			final RedirectResponseWrapper redirectResponseWrapper = new RedirectResponseWrapper(response);

			filterChain.doFilter(request, redirectResponseWrapper);
			
			if (redirectResponseWrapper.getRedirect() != null) {
				request.setCharacterEncoding("UTF-8");
				response.setContentType("text/plain;charset=utf-8");

				response.setHeader("Cache-Control", "no-cache");
				response.setDateHeader("Expires", 0);
				response.setHeader("Pragma", "no-cache");

				final String redirectURL = redirectResponseWrapper.getRedirect();
				logger.debug("Redirecting to: " + redirectURL);
				
				final String content;
				if (redirectURL.indexOf("?login_error=1") == -1) {
					content = "{'success': true, url: '" + redirectURL + "'}";

				} else {
					content = "{'success': false, errors: [{id: 'j_password', msg:'"
							+ ((AuthenticationException) request.getSession().getAttribute(
									AbstractProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY)).getMessage() + "'}]}";
				}
				response.getOutputStream().write(content.getBytes("UTF-8"));
			}

		} else {
			filterChain.doFilter(request, response);
		}
	}

	private boolean isAjaxRequest(HttpServletRequest request) {
		return request.getParameter("ajax") != null;
	}

	private static class RedirectResponseWrapper extends HttpServletResponseWrapper {
		
		private String redirect;

		public RedirectResponseWrapper(HttpServletResponse httpServletResponse) {
			super(httpServletResponse);
		}

		public String getRedirect() {
			return redirect;
		}

		@Override
		public void sendRedirect(String string) throws IOException {
			this.redirect = string;
		}
	}
}
