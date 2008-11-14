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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

/**
 * @author shade
 * @version $Id: URLUtils.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public final class URLUtils {

	private static final URLCodec codec = new URLCodec();

	/**
	 * @param queryString
	 * @return
	 */
	public static Map<String, String> queryStringToMap(final String queryString) {

		final Map<String, String> map = new HashMap<String, String>();
		try {

			if (queryString != null) {
				for (String pair : codec.decode(queryString).split("&")) {

					final String[] kv = pair.split("=");
					if (kv.length == 2) {
						map.put(codec.decode(kv[0]), codec.decode(kv[1]));

					}
				}
			}

		} catch (DecoderException e) {
			throw new IllegalArgumentException(e);
		}

		return map;
	}

	/**
	 * @param map
	 * @return
	 */
	public static String mapToQueryString(final Map<String, ? extends Object> map) {

		final StringBuilder sb = new StringBuilder();
		if (map != null) {
			for (Map.Entry<String, ? extends Object> entry : map.entrySet()) {

				if (sb.length() > 0) {
					sb.append("&");
				}
				try {
					sb.append(codec.encode(entry.getKey())).append("=").append(codec.encode(entry.getValue().toString()));
				} catch (EncoderException e) {
					throw new IllegalArgumentException(e);
				}
			}
		}

		return sb.toString();
	}
}
