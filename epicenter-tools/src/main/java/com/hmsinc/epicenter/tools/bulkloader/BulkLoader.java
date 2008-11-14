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
package com.hmsinc.epicenter.tools.bulkloader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.GenericParser;
import ca.uhn.hl7v2.validation.impl.NoValidation;

import com.hmsinc.epicenter.integrator.service.EpiCenterService;
import com.hmsinc.epicenter.tools.RunnableTool;
import com.hmsinc.epicenter.tools.util.ScrollableNamedParameterJdbcTemplate;
import com.hmsinc.mergence.model.DataSource;
import com.hmsinc.mergence.model.HL7Message;
import com.hmsinc.mergence.scripting.MessageTransformer;
import com.hmsinc.mergence.scripting.PreProcessor;

/**
 * Reads messages from a Mergence MESSAGE table, applies a transformation and
 * sends to the EpiCenter Integrator.
 * 
 * @author shade
 * @version $Id: BulkLoader.java 1543 2008-04-10 22:30:59Z steve.kondik $
 */
public class BulkLoader implements RunnableTool {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final GenericParser parser = new GenericParser();

	private static final int WORKERS = 8;

	private boolean running = true;

	private static final String QUERY = "select m.id as message_id, m.message as message, d.id as data_source_id, d.identifier as identifier from message m inner join data_source d on m.id_data_source = d.id";

	@Resource
	private javax.sql.DataSource dataSource;

	@Resource
	private PreProcessor preprocessor;

	@Resource
	private MessageTransformer transformer;

	@Resource
	private EpiCenterService epiCenterService;

	@Resource
	private TransactionTemplate transactionTemplate;

	private BlockingQueue<HL7Message> queue = new LinkedBlockingQueue<HL7Message>(WORKERS);

	private ScrollableNamedParameterJdbcTemplate jdbcTemplate;

	private String[] arguments;

	/**
	 * @return the arguments
	 */
	public String[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Transactional
	public void run() {

		parser.setValidationContext(new NoValidation());
		jdbcTemplate = new ScrollableNamedParameterJdbcTemplate(dataSource);

		final ExecutorService executor = Executors.newFixedThreadPool(WORKERS);

		logger.info("Executing query (this will take some time)...");
		final Map<String, Object> parameters = new HashMap<String, Object>();
		final StringBuilder query = new StringBuilder(QUERY);
		if (arguments.length > 1) {
			query.append(" where m.id > :start");
			parameters.put("start", Long.valueOf(arguments[1]));
		}
		if (arguments.length > 2) {
			query.append(" and m.id < :end");
			parameters.put("end", Long.valueOf(arguments[2]));
		}
		query.append(" order by m.id asc");

		for (int i = 0; i < WORKERS; i++) {
			executor.execute(new BulkLoaderWorker());
		}

		jdbcTemplate.query(query.toString(), parameters, new BulkLoaderResultSetHandler());

		running = false;

	}

	private class BulkLoaderWorker implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {

			while (running) {

				try {

					final HL7Message message = queue.poll(5, TimeUnit.SECONDS);
					if (message != null) {
						transactionTemplate.execute(new TransactionCallbackWithoutResult() {

							@Override
							protected void doInTransactionWithoutResult(TransactionStatus status) {
								try {
									epiCenterService.doProcess(transformer.transform(message));
								} catch (Exception e) {
									status.setRollbackOnly();
									logger.error(e.getMessage(), e);
								}
							}
						});
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	private class BulkLoaderResultSetHandler implements RowCallbackHandler {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.springframework.jdbc.core.RowCallbackHandler#processRow(java.sql.ResultSet)
		 */
		public void processRow(ResultSet result) throws SQLException {
			try {
				final DataSource ds = new DataSource(result.getString("IDENTIFIER"));
				ds.setId(result.getLong("DATA_SOURCE_ID"));

				final String unparsedMessage = (result.getString("MESSAGE"));
				if (unparsedMessage != null) {
					final Message m = parser.parse(preprocessor.process(unparsedMessage));
					if (m != null) {
						final HL7Message hl7 = new HL7Message(ds, m);
						hl7.setId(result.getLong("MESSAGE_ID"));

						if (hl7.getId() % 1000 == 0) {
							logger.info("Message ID: {}", hl7.getId());
						}

						queue.put(hl7);
					}
				}
			} catch (Exception e) {
				logger.error("Row: {}  Error: {}", result, e);
			}
		}
	}

	public String getUsage() {
		return "bulkloader [starting id] [ending id]";
	}
	
}
