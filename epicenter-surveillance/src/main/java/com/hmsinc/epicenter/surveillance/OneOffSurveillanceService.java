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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;
import com.hmsinc.epicenter.surveillance.jobs.SurveillanceTaskRunner;

public class OneOffSurveillanceService extends AbstractSurveillanceService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private SurveillanceTaskRunner surveillanceTaskRunner;

	private Collection<SurveillanceTask> tasks = new ArrayList<SurveillanceTask>();
	
	/**
	 * Execute all surveillance tasks.
	 */
	protected void initializeSurveillanceTasks() {
		
		logger.info("Starting one-off surveillance..");
		
		final List<SurveillanceTask> tasks = surveillanceRepository.getList(SurveillanceTask.class);
		if (tasks != null) {
			for (SurveillanceTask task : tasks) {
				if (task.isEnabled() && task.getOrganization().isEnabled()) {
					handleTask(task);
				}
			}
		}
	}

	private void handleTask(SurveillanceTask task) {
		tasks.add(task);
		surveillanceTaskRunner.execute(task);
	}
}
