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
package com.hmsinc.epicenter.tools.reclassifier;

import java.sql.Types;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.sql.DataSource;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.BatchSqlUpdate;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.ClassificationTarget;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.service.ClassificationService;
import com.hmsinc.epicenter.tools.RunnableTool;

/**
 * Creates classifications for existing Interactions.
 * 
 * TODO: Make this more configurable and smarter.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: Reclassifier.java 1603 2008-05-06 19:37:13Z steve.kondik $
 */
public class Reclassifier implements RunnableTool {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String INSERT_CLASSIFICATION = " (id_interaction, id_classification) values (?, ?)";

	private static final String DEFAULT_TABLE_NAME = "interaction_classification";
	
	private static final int BATCH_SIZE = 1000;

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = "epicenter-model")
	private EntityManager entityManager;

	@Resource
	private AnalysisRepository analysisRepository;

	@Resource
	private DataSource modelDataSource;

	@Resource
	private ClassificationService classificationService;

	private ClassificationTarget target;

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

	@Transactional
	public void run() {

		setup();

		final String destinationTable = (arguments.length > 4) ? arguments[4] : DEFAULT_TABLE_NAME;
		final String query = new StringBuilder("INSERT INTO ").append(destinationTable).append(INSERT_CLASSIFICATION).toString();
		
		final BatchSqlUpdate updater = new BatchSqlUpdate(modelDataSource, query);
		updater.declareParameter(new SqlParameter(Types.BIGINT));
		updater.declareParameter(new SqlParameter(Types.BIGINT));
		updater.setBatchSize(BATCH_SIZE);
		updater.compile();

		final StatelessSession ss = ((Session) entityManager.getDelegate())
				.getSessionFactory().openStatelessSession();

		final Criteria c = ss.createCriteria(target.getInteractionClass())
			.add(Restrictions.eq("patientClass", target.getPatientClass())).addOrder(Order.asc("id")).setCacheable(false);
		
		if (arguments.length > 2) {
			c.add(Restrictions.gt("id", Long.valueOf(arguments[2])));
		}

		if (arguments.length > 3) {
			c.add(Restrictions.lt("id", Long.valueOf(arguments[3])));
		}
		
		final ScrollableResults sr = c.scroll(ScrollMode.FORWARD_ONLY);
		int i = 0;
		while (sr.next()) {

			final Interaction interaction = (Interaction) sr.get(0);
			final Set<Classification> classifications = classificationService
					.classify(interaction, target);

			save(interaction, classifications, updater);

			i++;
			if (i % BATCH_SIZE == 0) {
				logger.info("Processed {} interactions (current id: {})", i,
						interaction.getId());
			}

			((Session) entityManager.getDelegate()).evict(interaction);
		}
		
		sr.close();
		
		updater.flush();
	}

	@Transactional(readOnly = true)
	public void setup() {

		Validate.isTrue(arguments.length > 1,
				"Classification target id is required.");

		target = analysisRepository.load(Long.valueOf(arguments[1]),
				ClassificationTarget.class);
		Validate.notNull(target, "Invalid classification target: "
				+ arguments[1]);
		logger.info("Using classification target: {}", target);

	}

	@Transactional
	public void save(Interaction interaction,
			Set<Classification> classifications, BatchSqlUpdate updater) {
		if (classifications != null) {
			for (Classification classification : classifications) {
				updater.update(new Object[] { interaction.getId(),
						classification.getId() });
			}
		}
	}

	public String getUsage() {
		return "reclassifier id_classification_target [starting id] [ending id]";
	}
	
}
