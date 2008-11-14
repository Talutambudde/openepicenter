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
package com.hmsinc.epicenter.model.workflow.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.criteriaQuery;
import static com.hmsinc.epicenter.model.util.ModelUtils.namedQuery;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.model.workflow.Subscription;
import com.hmsinc.epicenter.model.workflow.Workflow;
import com.hmsinc.epicenter.model.workflow.WorkflowObject;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;
import com.hmsinc.epicenter.model.workflow.WorkflowState;
import com.hmsinc.epicenter.model.workflow.WorkflowStateType;
import com.hmsinc.hibernate.criterion.SpatialRestrictions;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WorkflowRepositoryImpl.java 205 2007-09-26 16:52:01Z
 *          steve.kondik $
 */
public class WorkflowRepositoryImpl extends AbstractJPARepository<WorkflowObject, Long> implements WorkflowRepository {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getDefaultWorkflow()
	 */
	public Workflow getDefaultWorkflow() {
		return (Workflow) namedQuery(entityManager, "getWorkflow").setParameter("name",
				WorkflowRepository.DEFAULT_WORKFLOW).getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getInitalState(com.hmsinc.epicenter.model.workflow.Workflow)
	 */
	public WorkflowState getInitalState(Workflow workflow) {
		return (WorkflowState) namedQuery(entityManager, "getInitialState").setParameter("workflow", workflow)
				.getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getInitialDisposition()
	 */
	public EventDisposition getInitialDisposition() {
		return (EventDisposition) namedQuery(entityManager, "getInitialDisposition").getSingleResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getSubscriptions(com.hmsinc.epicenter.model.analysis.classify.Classification,
	 *      com.hmsinc.epicenter.model.geography.Geography)
	 */
	@SuppressWarnings("unchecked")
	public List<Subscription> getSubscriptions(Classification classification, Geography geography) {

		Validate.notNull(classification, "Classification must be specified.");
		Validate.notNull(geography, "Geography must be specified.");

		final Criteria c = criteriaQuery(entityManager, Subscription.class);
		c.createCriteria("classifications").add(Restrictions.idEq(classification.getId()));
		c.createCriteria("geographies").add(SpatialRestrictions.contains("geometry", geography.getGeometry()));
		c.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		return c.list();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getInvestigations(org.joda.time.DateTime,
	 *      org.joda.time.DateTime, com.vividsolutions.jts.geom.Geometry,
	 *      boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Investigation> getInvestigations(DateTime startDate, DateTime endDate, Geometry geometry,
			Collection<Organization> organizations, boolean showAll, Integer offset, Integer numRows) {

		final Criteria c = criteriaQuery(entityManager, Investigation.class, "investigation");
		applyInvestigationCriteria(c, startDate, endDate, geometry, organizations, showAll);

		if (offset != null) {
			c.setFirstResult(offset);
		}

		if (numRows != null) {
			c.setMaxResults(numRows);
		}

		c.addOrder(Order.desc("timestamp"));

		return c.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getInvestigationCount(org.joda.time.DateTime,
	 *      org.joda.time.DateTime, com.vividsolutions.jts.geom.Geometry,
	 *      boolean)
	 */
	public Integer getInvestigationCount(DateTime startDate, DateTime endDate, Geometry geometry,
			Collection<Organization> organizations, boolean showAll) {

		final Criteria c = criteriaQuery(entityManager, Investigation.class, "investigation");
		applyInvestigationCriteria(c, startDate, endDate, geometry, organizations, showAll);

		c.setProjection(Projections.rowCount());

		return (Integer) c.uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.workflow.WorkflowRepository#getDateOfOldestInvestigation(com.vividsolutions.jts.geom.Geometry)
	 */
	public DateTime getDateOfOldestInvestigation(Geometry geometry, Collection<Organization> organizations) {

		final Criteria c = criteriaQuery(entityManager, Investigation.class, "investigation");
		applyInvestigationCriteria(c, null, null, geometry, organizations, false);

		c.setProjection(Projections.min("timestamp"));
		
		final Object min = c.uniqueResult();
		return (min == null ? null : (DateTime)min);
	}

	/**
	 * Basic filter for investigations.
	 * 
	 * @param c
	 * @param startDate
	 * @param endDate
	 * @param geometry
	 * @param organizations
	 * @param showAll
	 * @return
	 */
	private Criteria applyInvestigationCriteria(final Criteria c, DateTime startDate, DateTime endDate,
			Geometry geometry, Collection<Organization> organizations, boolean showAll) {

		if (startDate != null) {
			if (endDate == null) {
				c.add(Restrictions.gt("timestamp", startDate));
			} else {
				c.add(Restrictions.between("timestamp", startDate, endDate));
			}
		}
		
		if (organizations != null && organizations.size() > 0) {
			c.add(Restrictions.in("organization", organizations));
		}

		if (geometry != null) {
			c.add(Subqueries.exists(getInvestigationGeometryFilter(geometry)));
		}

		if (showAll == false) {
			c.createCriteria("state").add(Restrictions.ne("stateType", WorkflowStateType.TERMINAL));
		}

		return c;
	}

	/**
	 * Get the subquery for filtering based on events.
	 * 
	 * @param geometry
	 * @return
	 */
	private DetachedCriteria getInvestigationGeometryFilter(final Geometry geometry) {
	
		final DetachedCriteria dc = DetachedCriteria.forClass(Event.class, "event");
		dc.createCriteria("event.investigations").add(Restrictions.eqProperty("id", "investigation.id"));
		dc.createCriteria("event.geography").add(SpatialRestrictions.withinOrFilter("geometry", geometry, 1000, true));
		dc.setProjection(Projections.property("id"));
		
 	//	dc.setComment(" */ /*+ ORDERED ");
		return dc;
	}

}
