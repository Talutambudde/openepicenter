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
package com.hmsinc.epicenter.webapp;

import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.PermissionRepository;

/**
 * Simple controller for the EpiCenter webapp.
 * 
 * @author shade
 * @version $Id: EpiCenterController.java 1568 2008-04-18 13:08:44Z steve.kondik $
 */
@Controller
public class EpiCenterController {

	@Resource
	private PermissionRepository permissionRepository;
	
	@Resource
	private String geoServerHost;
	
	@Resource
	private String googleAPIKey;

	@Resource
	private String geoServerSLDOverride;
	
	@Resource
	private Properties applicationProperties;
	
	static final String APP_VERSION;
	
	static {
		final Package p = Package.getPackage("com.hmsinc.epicenter.model");
		Validate.notNull(p);
		final String v = p.getImplementationVersion();
		APP_VERSION = v == null ? "" : "EpiCenter " + v;

	}
	
	@ModelAttribute("appVersion")
	public String getAppVersion() {
		return APP_VERSION;
	}

	@ModelAttribute("googleAPIKey")
	public String getGoogleAPIKey() {
		return googleAPIKey;
	}

	@ModelAttribute("geoServerHost")
	public String getGeoServerHost() {
		return geoServerHost;
	}
	
	/**
	 * Gets the current user's principal.
	 * 
	 * @return
	 */
	@ModelAttribute("principal")
	@Transactional(readOnly = true)
	public EpiCenterUser getPrincipal() {

		EpiCenterUser userPrincipal = null;
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new AccessDeniedException("No credentials found!");
		}

		Object principal = auth.getPrincipal();
		if (principal != null && principal instanceof EpiCenterUser) {

			// Reload it, since we're probably in a different thread.
			userPrincipal = permissionRepository.load(((EpiCenterUser) principal).getId(), EpiCenterUser.class);

		}

		return userPrincipal;
	}

	@ModelAttribute("sldURL")
	public String getSldURL() {
		
		// This can come from JNDI, a properties file, or we can just ignore it and let the JS decide.
		String ret = "";
		String override = StringUtils.trimToNull(geoServerSLDOverride);
		if (override.equals(".")) {
			if (applicationProperties.containsKey("geoserver.sld.override")) {
				override = applicationProperties.getProperty("geoserver.sld.override");
			} else {
				override = null;
			}
		}
		if (override != null) {
			ret = new StringBuilder("EpiCenter.GEOSERVER_SLD_OVERRIDE = \"").append(override).append("\";").toString();
		}
		return ret;
	}
	
	@RequestMapping("/login.html")
	public String loginHandler() {
		return "login";
	}

	@RequestMapping("/logout.html")
	public String logoutHandler() {
		return "logout";
	}

	@RequestMapping("/app.html")
	public String appHandler() {
		return "app";
	}

	@RequestMapping("/unsupported.html")
	public String unsupportedHandler() {
		return "unsupported";
	}

}
