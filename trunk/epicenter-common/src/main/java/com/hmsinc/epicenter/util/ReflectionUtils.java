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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collection;

/**
 * Various reflection helpers.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ReflectionUtils.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class ReflectionUtils {

	/**
	 * Applies a property map to a bean.
	 * 
	 * @param object
	 * @param map
	 */
	public static void mapToBean(final Object object, final Map<String, String> map) {

		for (Map.Entry<String, String> entry : map.entrySet()) {
			setProperty(object, String.class, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * @param object
	 * @param property
	 * @return
	 */
	public static Method findSetterMethod(final Object object, final Class<?> type, final String property) {
		Method ret = null;
		final String prop = Character.toUpperCase(property.charAt(0)) + property.substring(1);
		try {
			ret = object.getClass().getMethod("set" + prop, type);
		} catch (NoSuchMethodException e) {
			// Skip it.
		}
		return ret;
	}

	/**
	 * @param object
	 * @param property
	 * @param value
	 */
	public static <T extends Object> void setProperty(final Object object, Class<T> type, final String property, final T value) {
		try {
			final Method m = findSetterMethod(object, type, property);
			if (m != null) {
				m.invoke(object, value);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param object
	 * @param type
	 * @param property
	 * @return
	 */
	public static Method findGetterMethod(final Object object, final String property) {
		Method ret = null;
		final String prop = Character.toUpperCase(property.charAt(0)) + property.substring(1);
		try {
			ret = object.getClass().getMethod("get" + prop);

		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	/**
	 * @param object
	 * @param property
	 * @param value
	 */
	public static <T extends Object> T getProperty(final Object object, final Class<T> type, String property) {
		T ret = null;
		try {
			ret = type.cast(findGetterMethod(object, property).invoke(object));

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	/**
	 * @param from
	 * @param to
	 * @param type
	 * @param property
	 */
	public static <T extends Object> void copyProperty(final Object from, final Object to, final Class<T> type, final String property) {
		setProperty(to, type, property, getProperty(from, type, property));
	}

	/**
	 * Copies properties from one object to another.
	 * 
	 * @param from
	 * @param to
	 * @param copyIfNull
	 */
	public static void copyProperties(final Object from, final Object to, final boolean copyIfNull, final boolean copyCollections) {

		for (Method m : from.getClass().getMethods()) {
			if (m.getName().startsWith("get")) {

				final String propertyName = m.getName().substring(3);

				try {
					Method target = to.getClass().getMethod("set" + propertyName, m.getReturnType());
					if (target != null && (!Collection.class.isAssignableFrom(target.getReturnType()) || copyCollections)) {
						Object value = m.invoke(from);
						if (!(value instanceof Collection) || copyCollections) {
							if (value != null || copyIfNull) {
								target.invoke(to, value);
							}

						}
					}

				} catch (NoSuchMethodException e) {
					// skip it
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
