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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;

/**
 * Simple base launcher for the surveillance system. Just checks if we've
 * actually enabled surveillance, then invokes the initializeSurveillanceTasks()
 * method of whatever implementation is plugged in.
 * 
 * @version $Id: AbstractSurveillanceService.java 1812 2008-07-07 14:47:36Z steve.kondik $
 * @author shade
 */

public abstract class AbstractSurveillanceService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean surveillanceEnabled = false;

	@Resource
	private TransactionTemplate transactionTemplate;

	@Resource
	protected SurveillanceRepository surveillanceRepository;
	
	/**
	 * @throws Exception
	 */
	@PostConstruct
	public void init() throws Exception {

		if (isSurveillanceEnabled()) {

			logger.info("Initializing surveillance tasks..");

			// Initialize surveillanceTasks
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						initializeSurveillanceTasks();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
		} else {
			logger.info("Surveillance disabled.");
		}
	}

	/**
	 * @throws Exception
	 */
	protected abstract void initializeSurveillanceTasks() throws Exception;

	/**
	 * @return the surveillanceEnabled
	 */
	public boolean isSurveillanceEnabled() {
		return surveillanceEnabled;
	}

	/**
	 * @param surveillanceEnabled
	 *            the surveillanceEnabled to set
	 */
	public void setSurveillanceEnabled(boolean surveillanceEnabled) {
		this.surveillanceEnabled = surveillanceEnabled;
	}

}
