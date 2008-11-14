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
package com.hmsinc.epicenter.velocity;

import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.ReadableInstant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * Workalike of the Velocity-Tools DateTool format functions.
 * 
 * @author shade
 * @version $Id: DateTimeFormatTool.java 856 2008-02-04 18:08:51Z steve.kondik $
 */
public class DateTimeFormatTool {

	/**
	 * Converts the specified object to a date and returns a formatted string
	 * representing that date in the locale returned by {@link #getLocale()}.
	 * 
	 * @param format
	 *            the formatting instructions
	 * @param obj
	 *            the date object to be formatted
	 * @return a formatted string for this locale representing the specified
	 *         date or <code>null</code> if the parameters are invalid
	 */
	public String format(String format, ReadableInstant date) {
		final String ret;
		if (date == null) {
			ret = null;
		} else {
			final DateTimeFormatter formatter = getDateTimeStyle(format, format, Locale.getDefault(), DateTimeZone.getDefault());
			ret = formatter == null ? null : formatter.print(date);
		}
		return ret;
	}

	/**
	 * Returns the specified date as a string formatted according to the
	 * specified date and/or time styles.
	 * 
	 * @param dateStyle
	 *            the style pattern for the date
	 * @param timeStyle
	 *            the style pattern for the time
	 * @param obj
	 *            the date to be formatted
	 * @return a formatted representation of the given date
	 */
	public String format(String dateStyle, String timeStyle, ReadableInstant date) {
		final String ret;
		if (date == null) {
			ret = null;
		} else {
			final DateTimeFormatter formatter = getDateTimeStyle(dateStyle, timeStyle, Locale.getDefault(),
					DateTimeZone.getDefault());
			ret = formatter == null ? null : formatter.print(date);
		}
		return ret;
	}

	/**
	 * Returns the specified date as a string formatted according to the
	 * specified {@link Locale} and date and/or time styles.
	 * 
	 * @param dateStyle
	 *            the style pattern for the date
	 * @param timeStyle
	 *            the style pattern for the time
	 * @param obj
	 *            the date to be formatted
	 * @param locale
	 *            the {@link Locale} to be used for formatting the date
	 * @return a formatted representation of the given date
	 */
	public String format(String dateStyle, String timeStyle, ReadableInstant date, Locale locale) {
		final String ret;
		if (date == null) {
			ret = null;
		} else {
			final DateTimeFormatter formatter = getDateTimeStyle(dateStyle, timeStyle, locale, DateTimeZone
					.getDefault());
			ret = formatter == null ? null : formatter.print(date);
		}
		return ret;
	}

	/**
	 * Returns the specified date as a string formatted according to the
	 * specified {@link Locale} and date and/or time styles.
	 * 
	 * @param dateStyle
	 *            the style pattern for the date
	 * @param timeStyle
	 *            the style pattern for the time
	 * @param obj
	 *            the date to be formatted
	 * @param locale
	 *            the {@link Locale} to be used for formatting the date
	 * @param timezone
	 *            the {@link TimeZone} the date should be formatted for
	 * @return a formatted representation of the given date
	 */
	public String format(String dateStyle, String timeStyle, ReadableInstant date, Locale locale, DateTimeZone timezone) {
		final String ret;
		if (date == null) {
			ret = null;
		} else {
			final DateTimeFormatter formatter = getDateTimeStyle(dateStyle, timeStyle, locale, timezone);
			ret = formatter == null ? null : formatter.print(date);
		}
		return ret;
	}

	/**
	 * Checks a string to see if it matches one of the standard DateTimeFormat
	 * style patterns: full, long, medium, short, or default.
	 */
	private static DateTimeFormatter getDateStyle(String style, Locale locale, DateTimeZone zone) {
		final DateTimeFormatter ret;

		if (style.equalsIgnoreCase("full")) {
			ret = DateTimeFormat.fullDate();
		} else if (style.equalsIgnoreCase("long")) {
			ret = DateTimeFormat.longDate();
		} else if (style.equalsIgnoreCase("medium")) {
			ret = DateTimeFormat.mediumDate();
		} else if (style.equalsIgnoreCase("short")) {
			ret = DateTimeFormat.shortDate();
		} else if (style.equalsIgnoreCase("none")) {
			ret = null;
		} else {
			ret = DateTimeFormat.forPattern(style);
		}

		return ret == null ? null : ret.withLocale(locale).withZone(zone);
	}

	/**
	 * Checks a string to see if it matches one of the standard DateTimeFormat
	 * style patterns: full, long, medium, short, or default.
	 */
	private static DateTimeFormatter getTimeStyle(String style, Locale locale, DateTimeZone zone) {
		final DateTimeFormatter ret;
		if (style.equalsIgnoreCase("full")) {
			ret = DateTimeFormat.fullTime();
		} else if (style.equalsIgnoreCase("long")) {
			ret = DateTimeFormat.longTime();
		} else if (style.equalsIgnoreCase("medium")) {
			ret = DateTimeFormat.mediumTime();
		} else if (style.equalsIgnoreCase("short")) {
			ret = DateTimeFormat.shortTime();
		} else if (style.equalsIgnoreCase("none")) {
			ret = null;
		} else {
			ret = DateTimeFormat.forPattern(style);
		}
		return ret == null ? null : ret;
	}

	/**
	 * Checks a string to see if it matches one of the standard DateTimeFormat
	 * style patterns: full, long, medium, short, or default.
	 */
	private static DateTimeFormatter getDateTimeStyle(String dateStyle, String timeStyle, Locale locale,
			DateTimeZone zone) {
		final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
		final DateTimeFormatter date = getDateStyle(dateStyle, locale, zone);
		if (date != null) {
			builder.append(date);
		}
		final DateTimeFormatter time = getTimeStyle(timeStyle, locale, zone);
		if (date != null && time != null) {
			builder.append(DateTimeFormat.forPattern(" "));
		}
		if (time != null) {
			builder.append(time);
		}
		return builder.toFormatter();
	}

}
