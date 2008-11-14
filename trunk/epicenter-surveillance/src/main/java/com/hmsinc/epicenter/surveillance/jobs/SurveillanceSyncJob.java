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

import java.text.ParseException;

import javax.annotation.Resource;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;

/**
 * Monitors surveillance tasks in the database, adds jobs for new tasks,
 * reschedules jobs that have trigger modified.
 * 
 * @author Olek Poplavsky
 * @version $Id: SurveillanceSyncJob.java 1810 2008-07-03 19:22:40Z steve.kondik $
 */
public class SurveillanceSyncJob {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String JOB_PREFIX = "surveillance-";
	
	@Resource
	private SurveillanceRepository surveillanceRepository;

	@Resource
	private Scheduler scheduler;

	@Transactional
	public void run() {
		logger.debug("Synchronizing surveillance tasks..");

		for (SurveillanceTask task : surveillanceRepository.getList(SurveillanceTask.class)) {
			if (task.isEnabled() && task.getOrganization().isEnabled()) {
				handleTask(task);
			}
		}

	}

	/**
	 * @param task
	 */
	private void handleTask(SurveillanceTask task) {
		try {
			if (isNewTask(task)) {
				logger.debug("Found new surveillance task {}.", task.getId());
				scheduleTask(task);
			} else if (isIntervalModified(task)) {
				logger.debug("Found surveillance task {} with modified interval.", task.getId());
				rescheduleTask(task);
			}
		} catch (Exception e) {
			logger.error("Can not handle task " + task.getId(), e);
		}
	}

	/**
	 * @param task
	 */
	private void scheduleTask(SurveillanceTask task) {

		try {
			if (scheduler.getJobDetail(JOB_PREFIX + task.getId().toString(), Scheduler.DEFAULT_GROUP) == null) {

				final PersistableMethodInvokingJobDetailFactoryBean job = new PersistableMethodInvokingJobDetailFactoryBean();
				job.setTargetBeanName("surveillanceJob");
				job.setTargetMethod("run");
				job.setConcurrent(false);
				job.setName(JOB_PREFIX + task.getId().toString());
				job.setArguments(new Object[] { task.getId() });

				job.afterPropertiesSet();

				scheduler.scheduleJob((JobDetail) job.getObject(), createTriggerForTask(task));

				logger.info("Scheduled surveillance task: {}", task.getId());

			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (SchedulerException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

	}
	
	/**
	 * @param task
	 * @return
	 * @throws ParseException
	 * @throws SchedulerException
	 */
	private CronTrigger createTriggerForTask(final SurveillanceTask task) throws ParseException, SchedulerException {
		return new CronTrigger(JOB_PREFIX + task.getId().toString(), Scheduler.DEFAULT_GROUP, JOB_PREFIX + task.getId().toString(),
				Scheduler.DEFAULT_GROUP, task.getTrigger());
	}

	/**
	 * @param task
	 * @return
	 * @throws SchedulerException
	 */
	private boolean isIntervalModified(SurveillanceTask task) throws SchedulerException {
		CronTrigger trigger = (CronTrigger) scheduler.getTrigger("surveillance-" + task.getId().toString(), Scheduler.DEFAULT_GROUP);
		return !trigger.getCronExpression().equals(task.getTrigger());
	}

	/**
	 * @param task
	 * @throws SchedulerException
	 * @throws ParseException
	 */
	private void rescheduleTask(SurveillanceTask task) throws SchedulerException, ParseException {
		scheduler.rescheduleJob("surveillance-" + task.getId().toString(), Scheduler.DEFAULT_GROUP, createTriggerForTask(task));
	}

	/**
	 * @param task
	 * @return
	 * @throws SchedulerException
	 */
	private boolean isNewTask(SurveillanceTask task) throws SchedulerException {
		JobDetail jobDetail = scheduler.getJobDetail("surveillance-" + task.getId().toString(), Scheduler.DEFAULT_GROUP);

		return jobDetail == null;
	}
}
