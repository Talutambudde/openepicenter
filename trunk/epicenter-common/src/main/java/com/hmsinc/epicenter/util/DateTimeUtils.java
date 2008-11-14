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

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.ReadableInstant;

/**
 * Some helpers for working with Joda Time.
 * 
 * @author shade
 * @version $Id: DateTimeUtils.java 856 2008-02-04 18:08:51Z steve.kondik $
 */
public class DateTimeUtils {

	public static boolean isToday(final DateTime date) {
		return new LocalDate().equals(date.toLocalDate());
	}

	public static boolean isSameDay(final DateTime date, final DateTime otherDate) {
		return date.toLocalDate().equals(otherDate.toLocalDate());
	}

	public static DateTime toStartOfDay(final DateTime date) {
		return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0, 0, 0, date.getZone());
	}

	public static DateTime toEndOfDay(final DateTime date) {
		return new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 23, 59, 59, 999, date.getZone());
	}

	/**
     * Gets a difference in days, taking into account the timezone and DST.
     * 
     * @param start
     * @param end
     * @return
     */
    public static int deltaDays(final ReadableInstant start, final ReadableInstant end) {
    	return new Period(start, end, PeriodType.days()).getDays();
    }

    /**
     * @param start
     * @param end
     * @return
     */
    public static String formatDurationDays(final ReadableInstant start, final ReadableInstant end) {
    	final int duration = deltaDays(start, end) + 1;   	
    	return duration == 1 ? "1 day" : duration + " days";
    }
    
}
