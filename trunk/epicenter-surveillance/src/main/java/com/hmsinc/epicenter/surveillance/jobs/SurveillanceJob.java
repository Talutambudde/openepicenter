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
package com.hmsinc.epicenter.surveillance.jobs;

import javax.annotation.Resource;
import static com.hmsinc.epicenter.surveillance.SchedulingSurveillanceService.JOB_PREFIX;
import org.apache.commons.lang.Validate;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;

/**
 * Executes a group of Algorithms for a region.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:SurveillanceJob.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class SurveillanceJob {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private SurveillanceRepository surveillanceRepository;
	
	@Resource
	private SurveillanceTaskRunner surveillanceTaskRunner;

	@Resource
	private Scheduler scheduler;
	
	/**
	 * @param taskId
	 */
	@Transactional
	public void run(final Long taskId) {

		Validate.notNull(taskId, "SurveillanceTask id is required!");

		final SurveillanceTask task = surveillanceRepository.load(taskId, SurveillanceTask.class);
		if (task == null) {
			logger.debug("Looks like surveillance task {} was deleted, unscheduling it", taskId);
			cancelJob(taskId);
		} else if (!task.isEnabled()) {
			logger.debug("Surveillance task {} was disabled, unscheduling it", taskId);
			cancelJob(taskId);
		} else if (!task.getOrganization().isEnabled()) {
			logger.debug("Surveillance task's {} organization {} was disabled, unscheduling it", taskId, task.getOrganization().getName());
			cancelJob(taskId);
		} else {
    		surveillanceTaskRunner.execute(task);
		}
	}



	/**
	 * @param taskId
	 */
	private void cancelJob(final Long taskId) {
		try {
			scheduler.deleteJob(JOB_PREFIX + taskId.toString(), Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			logger.error("Could not cancel surveillance job: " + JOB_PREFIX + taskId, e);
		}
	}
}
