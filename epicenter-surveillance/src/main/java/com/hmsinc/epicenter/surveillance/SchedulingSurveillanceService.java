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
package com.hmsinc.epicenter.surveillance;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;
import com.hmsinc.epicenter.surveillance.jobs.SurveillanceTaskRunner;

/**
 * Manages scheduling of SurveillanceTasks via the Quartz Scheduler.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:SurveillanceService.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@ManagedResource(objectName = "com.hmsinc.epicenter:name=SurveillanceService")
public class SchedulingSurveillanceService extends AbstractSurveillanceService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String JOB_PREFIX = "surveillance-";

	@Resource
	private Scheduler scheduler;

	@Resource
	private JobDetail surveillanceSyncJobDetail;

	@Resource
	private CronTrigger surveillanceSyncJobTrigger;

	@Resource
	private SurveillanceTaskRunner surveillanceTaskRunner;

	@Override
	protected void initializeSurveillanceTasks() throws Exception {

		logger.info("Starting surveillance scheduler..");

		// Start the sync job if it's not already in the JobStore..
		if (isSurveillanceEnabled()) {
			if (scheduler.getJobDetail(surveillanceSyncJobDetail.getName(), Scheduler.DEFAULT_GROUP) == null) {
				scheduler.scheduleJob(surveillanceSyncJobDetail, surveillanceSyncJobTrigger);
			}
		}

	}

	@ManagedOperation(description = "Execute a Surveillance Task")
	@ManagedOperationParameters( { @ManagedOperationParameter(name = "id", description = "The id of the task to execute") })
	public void executeTask(final String id) throws Exception {
		Validate.notNull(id, "Surveillance task id must be specified.");

		// If the scheduler is enabled, we can use it to execute the task..
		if (isSurveillanceEnabled()) {
			scheduler.triggerJob(JOB_PREFIX + id.toString(), Scheduler.DEFAULT_GROUP);
		} else {
			final SurveillanceTask task = surveillanceRepository.load(Long.valueOf(id), SurveillanceTask.class);
			Validate.notNull(task, "Invalid task id: " + id);
			surveillanceTaskRunner.execute(task);
		}
	}

}