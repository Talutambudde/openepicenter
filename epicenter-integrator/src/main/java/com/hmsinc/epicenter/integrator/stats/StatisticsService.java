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
package com.hmsinc.epicenter.integrator.stats;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.mergence.model.DataSource;
import com.hmsinc.mergence.model.HL7Message;
import com.hmsinc.mergence.monitoring.AlertService;
import com.hmsinc.mergence.monitoring.AlertService.Severity;

/**
 * Keeps track of provider statistics. Simple in-memory implementation for now.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:StatisticsService.java 136 2007-05-17 17:13:24Z steve.kondik $
 * @org.apache.xbean.XBean element="statisticsService" description="Statistics tracking service"
 * 
 */
public class StatisticsService {

	public enum StatsType {
		DUPLICATE, INCOMPLETE;
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private int alertThreshold = 10;

	protected final BlockingQueue<StatisticsEntry> queue = new LinkedBlockingQueue<StatisticsEntry>();

	protected Thread statsServiceThread;

	@Resource
	protected AlertService alertService;

	/**
	 * Called when a duplicate message is received for updating statistics for a
	 * specific provider.
	 * 
	 * @param message
	 * @param type
	 * @param info
	 */
	public void updateProviderStats(HL7Message message, StatsType type, String info) {
		if (message != null && type != null) {
			try {
				queue.put(new StatisticsEntry(message.getDataSource(), type, message.getId(), info));
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@PostConstruct
	public void init() throws Exception {

		statsServiceThread = new StatisticsServiceThread();
		statsServiceThread.setName("EpiCenter StatisticsService");
		statsServiceThread.setDaemon(true);

		statsServiceThread.start();

		logger.debug("Statistics service started.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@PreDestroy
	public void destroy() throws Exception {

		if (statsServiceThread != null && statsServiceThread.isAlive()) {
			statsServiceThread.interrupt();
			statsServiceThread.join();
		}

		logger.debug("Statistics service shutdown.");
	}

	private class StatisticsServiceThread extends Thread {

		private final Map<Long, ProviderStatistics> providerStats = new HashMap<Long, ProviderStatistics>();

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			while (true) {
				try {
					doUpdateProviderStats(queue.take());

				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private void doUpdateProviderStats(final StatisticsEntry entry) {

			final DataSource p = entry.getDataSource();
			if (p != null) {
				if (providerStats.containsKey(p.getId())) {

					final ProviderStatistics s = providerStats.get(p.getId());
					final StatisticsCounter sc = s.getStats(entry.getStatsType());

					long tdiff = (System.currentTimeMillis() - (sc.getTimestamp()));
					if (tdiff <= 3600000) {

						sc.getEntries().add(entry);
						
						logger.info("{} stats for {}: {} in {} seconds.", new Object[] { entry.getStatsType(), p.getSendingFacility(), sc.getEntries().size(), (tdiff / 1000) } );

						if (sc.getEntries().size() > getAlertThreshold()
								&& (System.currentTimeMillis() - sc.getLastAlerted()) > 600000) {

							doAlert(sc, entry);
						}

					} else {
						providerStats.put(p.getId(), new ProviderStatistics());
					}
				} else {
					providerStats.put(p.getId(), new ProviderStatistics());
				}
			}
		}

		private void doAlert(final StatisticsCounter sc, final StatisticsEntry entry) {

			final StringBuilder alertMessage = new StringBuilder();
			final String type = StringUtils
					.capitalize(entry.getStatsType().toString().toLowerCase(Locale.getDefault()));

			alertMessage.append(type).append(" message threshold exceeded for facility ")
				.append(entry.getDataSource().getSendingFacility())
				.append("\n\nThe following messages triggered this alert: \n");

			for (final StatisticsEntry pEntry : sc.getEntries()) {
				alertMessage.append("  ").append(pEntry.getMessageId()).append(" (").append(pEntry.getAdditionalInfo())
						.append(")\n");
			}

			alertService.sendAlert(Severity.WARNING, alertMessage.toString());

			sc.setLastAlerted(System.currentTimeMillis());

		}
	}

	/**
	 * @return the alertThreshold
	 */
	public int getAlertThreshold() {
		return alertThreshold;
	}

	/**
	 * @param alertThreshold
	 *            the alertThreshold to set
	 */
	public void setAlertThreshold(int alertThreshold) {
		this.alertThreshold = alertThreshold;
	}

}
