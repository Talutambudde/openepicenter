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
package com.hmsinc.epicenter.model.geography.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.criteriaQuery;
import static com.hmsinc.epicenter.model.util.ModelUtils.namedQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.geography.util.EnvelopeUtils;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.hibernate.QueryBuilder;
import com.hmsinc.hibernate.criterion.SpatialRestrictions;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Manages the repository of Geography types.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:GeographyRepository.java 144 2007-05-19 07:57:56Z steve.kondik $
 */
public class GeographyRepositoryImpl extends AbstractJPARepository<Geography, Long> implements GeographyRepository {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getGeography(java.util.List,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getGeography(final List<String> names, final Class<T> geographyType) {
		Validate.notNull(names);
		return criteriaQuery(entityManager, geographyType).add(Restrictions.in("name", names)).list();

	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getGeography(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> T getGeography(String name, Class<T> geographyType) {
		Validate.notNull(name, "Name must be specified.");
		final List<T> result = criteriaQuery(entityManager, geographyType).add(Restrictions.naturalId().set("name", name)).list();
		return (result.size() > 0 ? result.get(0) : null);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getGeographiesInState(com.hmsinc.epicenter.model.geography.State,
	 *      java.lang.String, java.lang.Class, boolean)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getGeographiesInState(State state, String query, Class<T> geographyType, boolean includePartial) {
		
		final String name = (includePartial ? query + "%" : query);
		return criteriaQuery(entityManager, geographyType).add(Restrictions.eq("state", state)).add(Restrictions.ilike("name", name)).addOrder(Order.asc("name")).list();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getCountiesInState(com.hmsinc.epicenter.model.geography.State,
	 *      java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<County> getCountiesInState(final State state, final Collection<String> countyNames) {

		Validate.notNull(state);
		Validate.notNull(countyNames);
		return namedQuery(entityManager, "getCountiesByNameInState").setParameter("state", state).setParameter("names", countyNames).getResultList();

	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getZipcodesInState(com.hmsinc.epicenter.model.geography.State, java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public List<Zipcode> getZipcodesInState(State state, Collection<String> zipcodeNames) {
		
		Validate.notNull(state);
		Validate.notNull(zipcodeNames);
		
		return namedQuery(entityManager, "getZipcodesByNameInState").setParameter("state", state).setParameter("names", zipcodeNames).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getStateByAbbreviation(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public State getStateByAbbreviation(final String abbreviation) {

		Validate.notNull(abbreviation);
		State state = null;
		final List<State> states = namedQuery(entityManager, "getStateByAbbreviation").setParameter("abbreviation", abbreviation).getResultList();
		if (states.size() == 1) {
			state = states.get(0);
		}
		return state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getContained(com.hmsinc.epicenter.model.geography.Geography,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getContained(Geography geo, Class<T> containedType) {

		Validate.notNull(geo);
		Validate.notNull(containedType);
		
		// We force Oracle to join rather than bind here.
		final QueryBuilder q = new QueryBuilder();
		
		// Use filter if the area is too big.
		final String spatialOperator = geo.getGeometry().getArea() > 500 ? "filter" : "within";
		
		q.addEntity(geo.getClass(), "container").addEntity(containedType, "contained").addProperty("contained");
		q.addWhere("container = :container", "container", geo);
		q.addWhere(spatialOperator + "(contained." + (Zipcode.class.equals(containedType) ? "centroid" : "geometry") + ", container.geometry) = 'TRUE'");
		
		return q.toQuery((Session)entityManager.getDelegate(), true).list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getContained(com.hmsinc.epicenter.model.geography.Geography,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getContained(final Geometry geometry, final Class<T> containedType) {

		Validate.notNull(geometry);
		Validate.notNull(containedType);
		
		final Criteria c = criteriaQuery(entityManager, containedType)
			.add(SpatialRestrictions.within(getGeometryColumnName(containedType), geometry))
			.addOrder(Order.asc("name"));
		return c.list();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getContained(com.hmsinc.epicenter.model.geography.Envelope,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getContained(final Envelope envelope, final int srid, final Class<T> containedType) {

		Validate.notNull(envelope);
		Validate.notNull(containedType);
		Validate.notNull(srid);
				
		final Criteria c = criteriaQuery(entityManager, containedType).add(SpatialRestrictions.filter("geometry", EnvelopeUtils.toGeometry(envelope, srid)));
		return c.list();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getIntersecting(com.hmsinc.epicenter.model.geography.Geography,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getIntersecting(Geography geo, Class<T> intersectingType) {

		Validate.notNull(geo);
		Validate.notNull(intersectingType);
		
		// We force Oracle to join rather than bind here.
		final QueryBuilder q = new QueryBuilder();
		q.addEntity(ModelUtils.getRealClass(geo), "container").addEntity(intersectingType, "intersecting").addProperty("intersecting");
		q.addWhere("container = :container", "container", geo);
		q.addWhere("intersects(intersecting." + (Zipcode.class.equals(intersectingType) ? "centroid" : "geometry") + ", container.geometry) = 'TRUE'");
		
		return q.toQuery((Session)entityManager.getDelegate(), true).list();
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getIntersecting(com.vividsolutions.jts.geom.Geometry,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getIntersecting(Geometry geometry, Class<T> intersectingType) {

		Validate.notNull(geometry);
		Validate.notNull(intersectingType);

		return criteriaQuery(entityManager, intersectingType)
			.add(SpatialRestrictions.filter(getGeometryColumnName(intersectingType), geometry))
			.add(SpatialRestrictions.intersects(getGeometryColumnName(intersectingType), geometry))
			.addOrder(Order.asc("name")).list();

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#getInteracting(com.vividsolutions.jts.geom.Geometry,
	 *      java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getInteracting(Geometry geometry, Class<T> intersectingType) {

		Validate.notNull(geometry);
		Validate.notNull(intersectingType);

		return criteriaQuery(entityManager, intersectingType)
			.add(SpatialRestrictions.interacts("geometry", geometry))
			.addOrder(Order.asc("name")).list();

	}

	@SuppressWarnings("unchecked")
	private <T extends Geography> List<Long> getGeographyMatches(final String name, final Class<T> geographyType) {
		Validate.notNull(name);
		Validate.notNull(geographyType);
		
		return criteriaQuery(entityManager, geographyType).add(Restrictions.ilike("name", name)).setProjection(Projections.property("id")).list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#searchGeographies(java.lang.String)
	 */
	public <T extends Geography> List<T> searchGeographies(String query, final Class<T> geographyType) {

		Validate.notNull(query);
		Validate.notNull(geographyType);

		List<Long> obj = getGeographyMatches(query, geographyType);
		if (obj.size() == 0) {
			obj = getGeographyMatches(query + "%", geographyType);
		}
		final List<T> items = getReferences(obj, geographyType);
		Collections.sort(items);
		return items;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.GeographyRepository#inferPopulation(com.hmsinc.epicenter.model.geography.Geography)
	 */
	@SuppressWarnings("unchecked")
	public Long inferPopulation(Geography geography) {
		
		Validate.notNull(geography);
		
		final Long ret;
		if (geography.getPopulation() == null) {
			
			final Class<? extends Geography> geoClass = (Class<? extends Geography>)ModelUtils.getRealClass(geography);
			if (Zipcode.class.isAssignableFrom(geoClass)) {
				ret = 0L;
			} else {
				ret = (Long)criteriaQuery(entityManager, Zipcode.class)
				.add(SpatialRestrictions.withinOrFilter(getGeometryColumnName(geoClass), geography.getGeometry(), 500, false))
				.setProjection(Projections.sum("population")).uniqueResult();
			}
		} else {
			ret = geography.getPopulation();
		}
		
		return ret;
	}

	/**
	 * @param geo
	 * @return
	 */
	private static String getGeometryColumnName(final Class<? extends Geography> geo) {
		return (Zipcode.class.isAssignableFrom(geo) ? "centroid" : "geometry");
	}
	
	
}
