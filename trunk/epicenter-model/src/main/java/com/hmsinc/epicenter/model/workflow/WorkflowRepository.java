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
package com.hmsinc.epicenter.model.workflow;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.Repository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.Organization;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WorkflowRepository.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public interface WorkflowRepository extends Repository<WorkflowObject, Long> {

	public static final String DEFAULT_WORKFLOW = "Default Workflow";

	public Workflow getDefaultWorkflow();

	public WorkflowState getInitalState(Workflow workflow);

	public EventDisposition getInitialDisposition();
	
	public List<Investigation> getInvestigations(DateTime startDate, DateTime endDate, Geometry geometry,
			Collection<Organization> organizations,	boolean showAll, Integer offset, Integer numRows);

	public Integer getInvestigationCount(DateTime startDate, DateTime endDate, Geometry geometry, Collection<Organization> organizations, boolean showAll);
	
	public DateTime getDateOfOldestInvestigation(final Geometry visibleRegion, Collection<Organization> organizations);

	public List<Subscription> getSubscriptions(final Classification classification, final Geography geography);

}
