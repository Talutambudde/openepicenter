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
package com.hmsinc.epicenter.model.initialization;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hmsinc.epicenter.model.Repository;

/**
 * Tasks for upgrading between versions and initial configuration.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:UpgradeTasks.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
public class UpgradeTasks {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private PlatformTransactionManager transactionManager;

	private List<InitializationTask> tasks;

	private TransactionTemplate tt;

	/**
	 * Validates/creates static values from the epicenter-attributes
	 * configuration.
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@PostConstruct
	public void init() throws Exception {

		tt = new TransactionTemplate(transactionManager);

		tt.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {

				for (InitializationTask task : tasks) {
					logger.info("Executing initialization task: {}", task.getClass().getName());
					task.executeTask();
				}

			}
		});
	}

	/**
	 * Validates a list of known values against the values in a given
	 * repository.
	 * 
	 * TODO: Handle incremental updates..?
	 * 
	 * @param <T>
	 * @param attrs
	 * @param clazz
	 * @param repository
	 */
	public <T extends Serializable> void validateAttributes(final Collection<T> attrs, final Class<T> clazz,
			final Repository<? super T, ? extends Serializable> repository) {

		final List<T> dbValues = repository.getList(clazz);
		if (attrs.size() == dbValues.size()) {
			for (T value : attrs) {
				if (!dbValues.contains(value)) {
					logger.warn("Configured attributes do not match schema! Missing value: {}", value);
					// throw new RuntimeException("Configured attributes do not
					// match schema! Missing value: " + value.toString());
				}
			}

		} else if (dbValues.size() == 0 && attrs.size() > 0) {
			logger.info("Loading initial values for: {}", clazz.getName());
			repository.save(attrs);
		} else {
			logger.warn("Configured attributes do not match schema! [type: {}]", clazz.getName());
			// throw new RuntimeException("Configured attributes do not match
			// schema! [type: " + clazz.getName() + "]");
		}

	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	@Required
	public void setTasks(List<InitializationTask> tasks) {
		this.tasks = tasks;
	}

}
