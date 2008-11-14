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

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyType;
import com.hmsinc.epicenter.model.geography.Region;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.permission.AuthorizedRegion;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.hmsinc.epicenter.webapp.util.Visibility;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Returns styling parameters in a convenient and pluggable way.
 * 
 * TODO: Make better use of the databinder.
 * 
 * @author Olek Poplavsky
 * @version $Id: StyleParameters.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
class StyleParameters {

	private static final URLCodec codec = new URLCodec();
	
	private final Class<? extends Geography> geographyClass;

	private final AnalysisParameters parameters;
	
	private final String algorithmName;

	private final Properties algorithmProperties;

	private final boolean labelFeatures;
	
	/**
	 * @param request
	 * @param params
	 * @param dto
	 * @param user
	 */
	StyleParameters(HttpServletRequest request, AnalysisParameters params, final AnalysisParametersDTO queryParams, EpiCenterUser user)
			throws ServletRequestBindingException {

		this.parameters = params;
		
		// Workaround for http://jira.codehaus.org/browse/GEOS-1872
		try {
			this.algorithmName = codec.decode(queryParams.getAlgorithmName());
		} catch (DecoderException e) {
			throw new ServletRequestBindingException(e.getMessage());
		}
		
		this.algorithmProperties = queryParams.getAlgorithmProperties();

		this.parameters.setSecondaryFilter(SpatialSecurity.isGlobalAdministrator(user) ? null : user.getVisibleRegion().getGeometry());
		
		// If we're analyzing by facility, we need to strip out AGGREGATE_ONLY regions
		if (AnalysisLocation.FACILITY.equals(params.getLocation())) {
			for (Organization org : user.getOrganizations()) {
				for (AuthorizedRegion ar : org.getAuthorizedRegions()) {
					if (Visibility.AGGREGATE_ONLY.equals(SpatialSecurity.getVisibility(user, ar.getGeography()))) {
						this.parameters.setSecondaryFilter(this.parameters.getSecondaryFilter().difference(ar.getGeography().getGeometry()));
					}
				}
			}
		}
		double minLng = ServletRequestUtils.getRequiredDoubleParameter(request, "min_lng");
		double minLat = ServletRequestUtils.getRequiredDoubleParameter(request, "min_lat");
		double maxLng = ServletRequestUtils.getRequiredDoubleParameter(request, "max_lng");
		double maxLat = ServletRequestUtils.getRequiredDoubleParameter(request, "max_lat");
				
		this.geographyClass = GeographyType.valueOf(ServletRequestUtils.getRequiredStringParameter(request, "feature").toUpperCase()).getGeoClass();
		
		this.parameters.setFilter(new Envelope(minLng, maxLng, minLat, maxLat));
		
		this.labelFeatures = ServletRequestUtils.getBooleanParameter(request, "labelFeatures", false);
	}

	/**
	 * @return
	 */
	public String getLayerName() {
		String ret = null;
		if (Zipcode.class.equals(geographyClass)) {
			ret = "GEO_ZIPCODE";
		} else if (County.class.equals(geographyClass)) {
			ret = "GEO_COUNTY";
		} else if (State.class.equals(geographyClass)) {
			ret = "GEO_STATE";
		} else if (Region.class.equals(geographyClass)) {
			ret = "GEO_REGION";
		} else {
			throw new UnsupportedOperationException("Unknown geography type: " + geographyClass.getName());
		}
		return ret;
	}

	/**
	 * @return
	 */
	public boolean shouldRenderLabels() {
		return false;
	}

	/**
	 * @return the geographyClass
	 */
	public Class<? extends Geography> getGeographyClass() {
		return geographyClass;
	}

	/**
	 * @return the parameters
	 */
	public AnalysisParameters getParameters() {
		return parameters;
	}

	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}

	/**
	 * @return the algorithmProperties
	 */
	public Properties getAlgorithmProperties() {
		return algorithmProperties;
	}

	/**
	 * @return
	 */
	public boolean isLabelFeatures() {
		return labelFeatures;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		return EqualsBuilder.reflectionEquals(this, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
}