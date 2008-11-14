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
package com.hmsinc.epicenter.model.geography.util;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * A Geocoder implementation for the geocoder.us REST service.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:GeocoderDotUSClient.java 94 2007-08-20 18:39:14Z steve.kondik $
 */
public class GeocoderDotUSClient implements Geocoder {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final HttpClient httpClient = new HttpClient();

	private final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	private String geoCoderURL = "http://rpc.geocoder.us/service/csv";

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.geocoding.Geocoder#geocode(java.lang.String,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public Geometry geocode(String address, String city, String state, String zipcode) {

		Validate.notNull(address);
		Validate.notNull(city);
		Validate.notNull(state);
		Validate.notNull(zipcode);

		Geometry g = null;

		try {

			final GetMethod get = new GetMethod(geoCoderURL);

			final NameValuePair[] query = { new NameValuePair("address", address), new NameValuePair("city", city),
					new NameValuePair("state", state), new NameValuePair("zipcode", zipcode) };

			get.setQueryString(query);
			httpClient.executeMethod(get);

			final String response = get.getResponseBodyAsString();
			get.releaseConnection();

			if (response != null) {
				final StrTokenizer tokenizer = StrTokenizer.getCSVInstance(response);
				if (tokenizer.size() == 5) {

					final Double latitude = Double.valueOf(tokenizer.nextToken());
					final Double longitude = Double.valueOf(tokenizer.nextToken());

					g = factory.createPoint(new Coordinate(longitude, latitude));
					logger.debug("Geometry: " + g.toString());
				}
			}

		} catch (HttpException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return g;
	}

	/**
	 * @return the geoCoderURL
	 */
	public String getGeoCoderURL() {
		return geoCoderURL;
	}

	/**
	 * @param geoCoderURL
	 *            the geoCoderURL to set
	 */
	public void setGeoCoderURL(String geoCoderURL) {
		this.geoCoderURL = geoCoderURL;
	}

}
