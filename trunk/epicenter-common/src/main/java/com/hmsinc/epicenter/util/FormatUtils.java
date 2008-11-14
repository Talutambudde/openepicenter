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
package com.hmsinc.epicenter.util;

import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 *
 */
public class FormatUtils {

	private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.mediumDateTime();
	
	/**
	 * Round a double to a specific number of places.
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places) {
		return Math.round(value * Math.pow(10, places)) / Math.pow(10, places);
	}
	
	/**
	 * @param dateTime
	 * @return
	 */
	public static String formatDateTime(final ReadableInstant dateTime) {
		return DATETIME_FORMAT.print(dateTime);
	}

    /**
     * @param strings
     * @return
     */
    public static String join(String delimiter, Object... strings) {
    	final StringBuilder builder = new StringBuilder();
    	for (Object str : strings) {
    		if (builder.length() > 0) {
    			builder.append(delimiter);
    		}
    		if (str != null) {
    			builder.append(str.toString());
    		}
    	}
    	return builder.toString();
    }
    
    /**
     * @param s
     * @return
     */
    public static String camelize(String s) {
    	return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
