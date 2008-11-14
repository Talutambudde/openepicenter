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
package com.hmsinc.epicenter.model.surveillance.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.criteriaQuery;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceMethod;
import com.hmsinc.epicenter.model.surveillance.SurveillanceObject;
import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.surveillance.SurveillanceSet;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;
import com.hmsinc.epicenter.model.workflow.WorkflowStateType;
import com.hmsinc.hibernate.criterion.SpatialRestrictions;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:SurveillanceRepositoryImpl.java 205 2007-09-26 16:52:01Z
 *          steve.kondik $
 */
public class SurveillanceRepositoryImpl extends AbstractJPARepository<SurveillanceObject, Long> implements
		SurveillanceRepository {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.surveillance.SurveillanceRepository#getAnomalies(org.joda.time.DateTime,
	 *      org.joda.time.DateTime, boolean, boolean,
	 *      com.vividsolutions.jts.geom.Geometry, java.lang.Integer,
	 *      java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public List<Anomaly> getAnomalies(final DateTime startDate, final DateTime endDate, final boolean includeAll,
			final Geometry filter, final Geometry excludeFacilityEventsFilter, Integer offset, Integer numRows) {

		final Criteria c = criteriaQuery(entityManager, Anomaly.class);
		c.setCacheable(true);

		applyAnomalyCriteria(c, startDate, endDate, includeAll, filter, excludeFacilityEventsFilter);

		if (offset != null) {
			c.setFirstResult(offset);
		}
		if (numRows != null) {
			c.setMaxResults(numRows);
		}

		return c.addOrder(Order.desc("analysisTimestamp")).list();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.surveillance.SurveillanceRepository#getAnomalyCount(org.joda.time.DateTime,
	 *      org.joda.time.DateTime, boolean, boolean,
	 *      com.vividsolutions.jts.geom.Geometry)
	 */
	public Integer getAnomalyCount(final DateTime startDate, final DateTime endDate, final boolean includeAll,
			final Geometry filter, final Geometry excludeFacilityEventsFilter) {

		final Criteria c = criteriaQuery(entityManager, Anomaly.class);
		c.setCacheable(true);

		applyAnomalyCriteria(c, startDate, endDate, includeAll, filter, excludeFacilityEventsFilter);

		return (Integer) c.setProjection(Projections.rowCount()).uniqueResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.surveillance.SurveillanceRepository#getDateOfOldestAnomaly(com.vividsolutions.jts.geom.Geometry)
	 */
	public DateTime getDateOfOldestAnomaly(final Geometry filter, final Geometry excludeFacilityEventsFilter) {

		final Criteria c = criteriaQuery(entityManager, Anomaly.class);

		applyAnomalyCriteria(c, null, null, false, filter, excludeFacilityEventsFilter);
		
		c.setProjection(Projections.min("analysisTimestamp"));
		
		final Object min = c.uniqueResult();
		return (min == null ? null : (DateTime)min);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.surveillance.SurveillanceRepository#getLatestAnomaly(com.hmsinc.epicenter.model.geography.Geography,
	 *      com.hmsinc.epicenter.model.analysis.classify.Classification,
	 *      com.hmsinc.epicenter.model.surveillance.SurveillanceTask,
	 *      com.hmsinc.epicenter.model.surveillance.SurveillanceMethod,
	 *      com.hmsinc.epicenter.model.surveillance.SurveillanceSet,
	 *      org.joda.time.DateTime)
	 */
	public Anomaly getLatestAnomaly(final Geography geography, final Classification classification,
			final SurveillanceTask task, final SurveillanceMethod method, final SurveillanceSet set,
			final DateTime maxTime) {

		final Criteria c = criteriaQuery(entityManager, Anomaly.class);
		c.add(Restrictions.eq("geography", geography));
		if (classification != null) {
			c.add(Restrictions.eq("classification", classification));
		}

		if (task != null) {
			c.add(Restrictions.eq("task", task));
		}

		if (method != null) {
			c.add(Restrictions.eq("method", method));
		}

		if (set != null) {
			c.add(Restrictions.eq("set", set));
		}

		if (maxTime != null) {
			c.add(Restrictions.le("analysisTimestamp", maxTime));
		}

		c.setCacheable(false);
		c.addOrder(Order.desc("analysisTimestamp"));
		c.setMaxResults(1);

		final Anomaly anomaly = (Anomaly) c.uniqueResult();
		return anomaly;
	}

	/**
	 * @param c
	 * @param startDate
	 * @param endDate
	 * @param includeAll
	 * @param includeFacilityLocation
	 * @param filter
	 * @return
	 */
	private Criteria applyAnomalyCriteria(final Criteria c, final DateTime startDate, final DateTime endDate,
			final boolean includeAll, final Geometry filter, final Geometry excludeFacilityEventsFilter) {

		if (startDate == null && endDate != null) {
			c.add(Restrictions.lt("analysisTimestamp", endDate));
		} else if (startDate != null && endDate == null) {
			c.add(Restrictions.gt("analysisTimestamp", startDate));
		} else if (startDate != null && endDate != null) {
			c.add(Restrictions.between("analysisTimestamp", startDate, endDate));
		}

		if (!includeAll) {
			c.add(Restrictions.isEmpty("investigations")).createCriteria("disposition").add(
					Restrictions.eq("type", WorkflowStateType.INITIAL));
		}

		if (filter != null && excludeFacilityEventsFilter == null) {

			c.createCriteria("geography").add(SpatialRestrictions.withinOrFilter("geometry", filter, 1000, true));

		} else if (filter == null && excludeFacilityEventsFilter != null) {

			final Conjunction conjunction = Restrictions.conjunction();
			c.createAlias("geography", "eventGeography");
			c.createAlias("task", "eventTask");

			conjunction.add(Restrictions.eq("eventTask.location", AnalysisLocation.FACILITY));
			conjunction.add(SpatialRestrictions.withinOrFilter("eventGeography.geometry", excludeFacilityEventsFilter,
					1000, true));

			c.add(Restrictions.not(conjunction));

		} else if (filter != null && excludeFacilityEventsFilter != null) {

			final Conjunction conjunction = Restrictions.conjunction();
			c.createAlias("geography", "eventGeography");
			c.createAlias("task", "eventTask");

			// Find events where we have unlimited access
			conjunction.add(SpatialRestrictions.withinOrFilter("eventGeography.geometry", filter, 1000, true));

			// Filter events in the limited region
			final Conjunction facilityEventsConjunction = Restrictions.conjunction();
			facilityEventsConjunction.add(Restrictions.eq("eventTask.location", AnalysisLocation.FACILITY));
			facilityEventsConjunction.add(SpatialRestrictions.withinOrFilter("eventGeography.geometry", excludeFacilityEventsFilter, 1000, true));

			conjunction.add(Restrictions.not(facilityEventsConjunction));

			c.add(conjunction);
		}

		return c;
	}
}
