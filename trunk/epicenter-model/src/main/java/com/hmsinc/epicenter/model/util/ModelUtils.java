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
package com.hmsinc.epicenter.model.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.type.CustomType;
import org.hibernate.type.Type;

import com.hmsinc.hibernate.spatial.GeometryType;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Common utilities for the data model.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ModelUtils.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ModelUtils.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public final class ModelUtils {

	private static final Type geometryType = new CustomType(GeometryType.class, null);
	
	private ModelUtils() {
		// Don't need to instantiate this
	}

	/**
	 * Creates a cacheable native Hibernate query.
	 * 
	 * @param entityManager
	 * @param query
	 * @return
	 */
	public static org.hibernate.Query nativeQuery(final EntityManager entityManager, final String query) {
		return ((Session) entityManager.getDelegate()).createQuery(query).setCacheable(true);
	}

	/**
	 * Creates a cacheable native Hibernate named query.
	 * 
	 * @param entityManager
	 * @param namedQuery
	 * @return
	 */
	public static org.hibernate.Query nativeNamedQuery(final EntityManager entityManager, final String namedQuery) {
		return ((Session) entityManager.getDelegate()).getNamedQuery(namedQuery).setCacheable(true);
	}

	/**
	 * Creates a native hibernate Criteria query.
	 * 
	 * @param entityManager
	 * @param persistentClass
	 * @return
	 */
	public static Criteria criteriaQuery(final EntityManager entityManager, final Class<?> persistentClass) {
		return ((Session) entityManager.getDelegate()).createCriteria(persistentClass).setCacheable(true);
	}

	/**
	 * Creates a native hibernate Criteria query with an alias.
	 * 
	 * @param entityManager
	 * @param persistentClass
	 * @param alias
	 * @return
	 */
	public static Criteria criteriaQuery(final EntityManager entityManager, final Class<?> persistentClass,
			final String alias) {
		return ((Session) entityManager.getDelegate()).createCriteria(persistentClass, alias).setCacheable(true);
	}

	/**
	 * Creates a cacheable named query.
	 * 
	 * @param entityManager
	 * @param namedQuery
	 * @return
	 */
	public static Query namedQuery(final EntityManager entityManager, final String namedQuery) {
		return entityManager.createNamedQuery(namedQuery).setHint("org.hibernate.cacheable", Boolean.TRUE);
	}

	/**
	 * Creates a native SQL query.
	 * 
	 * @param entityManager
	 * @param query
	 * @return
	 */
	public static SQLQuery sqlQuery(final EntityManager entityManager, final String query) {
		return ((Session) entityManager.getDelegate()).createSQLQuery(query);
	}

	/**
	 * Appends values to a query.
	 * 
	 * @param query
	 * @param parameters
	 * @param fragment
	 * @param parameterName
	 * @param parameterValue
	 */
	@SuppressWarnings("unchecked")
	public static void appendToQuery(final StringBuilder query, final Map<String, Object> parameters,
			final String fragment, final String parameterName, final Object parameterValue) {
		if (parameterValue != null) {
			if (parameterValue instanceof Collection && ((Collection) parameterValue).size() == 0) {
				return;
			}

			query.append(fragment);
			query.append(" ");
			parameters.put(parameterName, parameterValue);
		}
	}

	/**
	 * Applies all parameters necessary to a query.
	 * 
	 * @param query
	 * @param parameters
	 * @return
	 */
	public static Query applyParameters(final Query query, final Map<String, Object> parameters) {
		for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
			query.setParameter(parameter.getKey(), parameter.getValue());
		}
		return query;
	}

	/**
	 * Applies all parameters necessary to a query (Hibernate native Query).
	 * 
	 * @param query
	 * @param parameters
	 * @return
	 */
	public static org.hibernate.Query applyParameters(final org.hibernate.Query query,
			final Map<String, Object> parameters) {
		for (Map.Entry<String, Object> parameter : parameters.entrySet()) {
			if (parameter.getValue() instanceof Collection) {
				query.setParameterList(parameter.getKey(), (Collection<?>) parameter.getValue());
			} else if (parameter.getValue() instanceof Geometry) {
				query.setParameter(parameter.getKey(), parameter.getValue(), geometryType);
			} else {
				query.setParameter(parameter.getKey(), parameter.getValue());
			}
		}
		return query;
	}

	/**
	 * Checks a zipcode for validity.
	 * 
	 * @param zipcode
	 * @return
	 * @throws InvalidZipcodeException
	 */
	public static String validateZipcode(String zipcode) throws InvalidZipcodeException {

		String newZip = null;

		if (zipcode != null) {

			final String cleanedZip = zipcode.length() >= 5 ? zipcode.substring(0, 5) : zipcode;

			if (StringUtils.isNumeric(cleanedZip) && cleanedZip.length() == 5) {
				newZip = cleanedZip;
			} else {
				throw new InvalidZipcodeException("Invalid zipcode in message (was: " + zipcode + ")");
			}
		}
		
		return newZip;
	}

	/**
	 * Gets the real class for an object, unwinding any HibernateProxies.
	 * 
	 * @param object
	 * @return
	 */
	public static Class<?> getRealClass(final Object object) {
		return Hibernate.getClass(object);
	}

	/**
	 * Disables "nested loops" optimization on PostgreSQL. This is needed with
	 * various spatial queries that confuse the optimizer.
	 * 
	 * @param entityManager
	 */
	public static void disableNestedLoops(final EntityManager entityManager) {

		final Session s = (Session) entityManager.getDelegate();
		final SessionFactoryImpl sf = (SessionFactoryImpl) s.getSessionFactory();
		if (sf.getDialect() instanceof PostgreSQLDialect) {
			s.doWork(new Work() {
				public void execute(Connection conn) throws SQLException {
					conn.createStatement().execute("set enable_nestloop=off");
				}

			});
		}
	}

	/**
	 * Enables "nested loops" optimization on PostgreSQL. This is needed with
	 * various spatial queries that confuse the optimizer.
	 * 
	 * @param entityManager
	 */
	public static void enableNestedLoops(final EntityManager entityManager) {

		final Session s = (Session) entityManager.getDelegate();
		final SessionFactoryImpl sf = (SessionFactoryImpl) s.getSessionFactory();
		if (sf.getDialect() instanceof PostgreSQLDialect) {
			s.doWork(new Work() {
				public void execute(Connection conn) throws SQLException {
					conn.createStatement().execute("set enable_nestloop=on");
				}

			});
		}
	}

	/**
	 * @param <T>
	 * @param entityManager
	 * @param c
	 * @param returnType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> listUsingCache(final EntityManager entityManager, final Criteria c, final Class<T> returnType) {
		
		final List<T> resultList = new ArrayList<T>();
		final List<Long> idList = c.setProjection(Projections.id()).list();
		for (Long id : idList) {
			resultList.add(entityManager.getReference(returnType, id));
		}
		return resultList;
	}
}
