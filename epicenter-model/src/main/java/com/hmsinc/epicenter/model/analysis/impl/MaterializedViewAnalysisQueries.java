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
package com.hmsinc.epicenter.model.analysis.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.criteriaQuery;
import static com.hmsinc.epicenter.model.util.ModelUtils.*;
import static com.hmsinc.epicenter.util.FormatUtils.camelize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.AnalysisView;
import com.hmsinc.epicenter.model.analysis.ClassifiedAnalysisView;
import com.hmsinc.epicenter.model.analysis.InteractionType;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.ClassificationTarget;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.hibernate.QueryBuilder;

/**
 * Generates a counts query using a materialized view.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:MVCountsAnalysisQuery.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class MaterializedViewAnalysisQueries extends AbstractAnalysisQueries {

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.impl.AnalysisQueries#createAggregatedAnalysisQuery(com.hmsinc.epicenter.model.analysis.AnalysisParameters, java.lang.Class, java.lang.Class)
	 */
	public <T extends Geography, A extends QueryableAttribute> Query createAggregatedAnalysisQuery(
			AnalysisParameters analysisParameters, Class<T> aggregateGeographyType, Class<A> aggregateAttributeType) {
		
		final QueryBuilder query = new QueryBuilder().addEntity(ClassifiedAnalysisView.class, "analysis");
		applyAttributes(query, analysisParameters);
		applyClassifications(query, analysisParameters);
		
		final String aggregateName = applyGeography(query, aggregateGeographyType, analysisParameters);
		
		final String aggregatePropertyName = camelize(aggregateAttributeType.getSimpleName());
		
		final long offset = getTimeOffset(analysisParameters);
		
		query.addGroupProperty(aggregateName + ".id")
			.addGroupProperty("analysis." + aggregatePropertyName + ".id")
			.addGroupProperty(getHqlForPeriod(analysisParameters.getPeriod()) + "(analysis.interactionDate, " + offset + ")")
			.addProperty("count(*)")
			.addOrder(aggregateName + ".id")
			.addOrder("analysis." + aggregatePropertyName + ".id");
			
		return query.toQuery((Session)entityManager.getDelegate(), true);

	}


	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.impl.AnalysisQueries#createCombinedAnalysisQuery(com.hmsinc.epicenter.model.analysis.AnalysisParameters, java.lang.Class)
	 */
	public <T extends Geography> Query createCombinedAnalysisQuery(AnalysisParameters analysisParameters,
			Class<T> aggregateGeographyType) {
		
		final QueryBuilder query = new QueryBuilder().addEntity(ClassifiedAnalysisView.class, "analysis");
		applyAttributes(query, analysisParameters);
		applyClassifications(query, analysisParameters);
		
		final String aggregateName = applyGeography(query, aggregateGeographyType, analysisParameters);
			
		final long offset = getTimeOffset(analysisParameters);
		
		query.addGroupProperty(aggregateName + ".id")
			.addGroupProperty(getHqlForPeriod(analysisParameters.getPeriod()) + "(analysis.interactionDate, " + offset + ")")
			.addProperty("count(distinct analysis.id)")
			.addOrder(aggregateName + ".id");
			
		return query.toQuery((Session)entityManager.getDelegate(), true);

	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.CountsAnalysisQuery#createTotalAnalysisQuery(java.util.Map,
	 *      com.hmsinc.epicenter.model.analysis.AnalysisParameters, boolean,
	 *      java.lang.String, java.lang.Class,
	 *      com.hmsinc.epicenter.model.geography.Geography,
	 *      com.hmsinc.ts4j.TimeSeriesPeriod)
	 */
	public <T extends Geography> Query createTotalAnalysisQuery(AnalysisParameters analysisParameters,
			Class<T> aggregateGeographyType) {

		final QueryBuilder query = new QueryBuilder().addEntity(AnalysisView.class, "analysis");
		applyAttributes(query, analysisParameters);
		
		final String aggregateName = applyGeography(query, aggregateGeographyType, analysisParameters);
		
		final long offset = getTimeOffset(analysisParameters);

		query.addGroupProperty(aggregateName + ".id")
			.addGroupProperty(getHqlForPeriod(analysisParameters.getPeriod()) + "(analysis.interactionDate, " + offset + ")")
			.addProperty("count(*)")
			.addOrder(aggregateName + ".id");

		return query.toQuery((Session)entityManager.getDelegate(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.CountsAnalysisQuery#createGetCasesQuery(java.util.Map,
	 *      com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.epicenter.model.geography.Geography, java.lang.Long,
	 *      java.lang.Long)
	 */
	@SuppressWarnings("unchecked")
	public org.hibernate.Criteria createGetCasesQuery(AnalysisParameters analysisParameters, Long offset, Long numRows) {

		Validate.notNull(offset, "Offset must be set.");
		Validate.notNull(numRows, "Number of returned rows must be set.");
		Validate.notNull(analysisParameters.getDataType(), "Data type must be set.");
		
		Class<? extends Interaction> interactionClass = null;
		for (ClassificationTarget target : analysisParameters.getDataType().getTargets()) {
			Validate.notNull(target.getInteractionClass(), "Interaction class was null for " + target.toString());
			if (interactionClass == null) {
				interactionClass = target.getInteractionClass();
			} else {
				Validate.isTrue(interactionClass.equals(target.getInteractionClass()), "Inconsistent interaction classes");
			}
		}
		
		// Query is a subselect of Interaction based on materialized view.
		final Class<? extends Serializable> mv = (analysisParameters.getClassifications() == null || analysisParameters.getClassifications().size() == 0 ? AnalysisView.class : ClassifiedAnalysisView.class);
		
		final QueryBuilder query = new QueryBuilder().addEntity(mv, "analysis")
		  .addProperty("distinct(analysis.id)").addProperty("analysis.interactionDate")
		  .addOrder("analysis.interactionDate asc");
		
		applyAttributes(query, analysisParameters);
		applyClassifications(query, analysisParameters);
		
		// Geography
		applyGeography(query, analysisParameters.getContainer().getClass(), analysisParameters);
		
		disableNestedLoops(entityManager);
		
		final Query q = query.toQuery((Session)entityManager.getDelegate(), true);
		
		q.setFirstResult(offset.intValue());
		q.setMaxResults(numRows.intValue());
		final List<Object[]> items = q.list();
		final List<Long> interactionList = new ArrayList<Long>();
		for (Object[] item : items) {
			interactionList.add((Long)item[0]);
		}
		Criteria ret = null;
		
		enableNestedLoops(entityManager);
		
		if (interactionList.size() > 0) {
			
			// We need to use a ResultTransformer here to avoid duplicate rows.
			ret = criteriaQuery(entityManager, interactionClass)
				.setFetchMode("classifications", FetchMode.JOIN)
				.setFetchMode("patient", FetchMode.JOIN)
				.setFetchMode("patientDetail", FetchMode.JOIN)
				.add(Restrictions.in("id", interactionList))
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);

		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.CountsAnalysisQuery#createGetCasesCountQuery(java.util.Map,
	 *      com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.epicenter.model.geography.Geography)
	 */
	public Query createGetCasesCountQuery(AnalysisParameters analysisParameters) {

		final QueryBuilder query = new QueryBuilder();
		final Class<? extends Serializable> mv = (analysisParameters.getClassifications() == null || analysisParameters.getClassifications().size() == 0 ? AnalysisView.class : ClassifiedAnalysisView.class);

		query.addEntity(mv, "analysis").addProperty("count(distinct analysis.id)");

		// Geography
		applyGeography(query, analysisParameters.getContainer().getClass(), analysisParameters);

		// Attributes:
		applyAttributes(query, analysisParameters);
		applyClassifications(query, analysisParameters);
		
		return query.toQuery((Session)entityManager.getDelegate(), true);
	}

	/**
	 * @param query
	 * @param analysisParameters
	 * @return
	 */
	@Override
	protected <T extends Geography> String applyGeography(QueryBuilder query,  
			Class<T> aggregateGeographyType, AnalysisParameters analysisParameters) {
		
		// Join to GEOGRAPHY if needed
		query.addEntity(Zipcode.class, "geoZipcode");
		if (AnalysisLocation.HOME.equals(analysisParameters.getLocation())) {
			query.addWhere("analysis.patientZipcode = geoZipcode.name");
		} else {
			query.addWhere("analysis.facility.zipcode = geoZipcode.name");
		}
		
		return super.applyGeography(query, aggregateGeographyType, analysisParameters);
	}
	
	/**
	 * Appends common attributes to the query.
	 * 
	 * @param query
	 * @param analysisParameters
	 * @return
	 */
	private static QueryBuilder applyAttributes(final QueryBuilder query, final AnalysisParameters analysisParameters) {
		
		final String dateProp = "analysis.interactionDate ";
		if (analysisParameters.getEndDate() == null) {
			query.addWhere(dateProp + ">= :startDate", "startDate", analysisParameters.getStartDate());
		} else {
			query.addWhere(dateProp + "between :startDate and :endDate", Arrays.asList("startDate", "endDate"), Arrays.asList(analysisParameters.getStartDate(), analysisParameters.getEndDate()) );
		}

		/*
		 * Datatype.. Combination of PatientClass and InteractionType.
		 * Annoying code follows.. We need distinct combinations of these to form the query.
		 */
		if (analysisParameters.getDataType() != null) {
			
			final StringBuilder sb = new StringBuilder();
			final List<String> paramNames = new ArrayList<String>();
			final List<Object> params = new ArrayList<Object>();
			final Set<List<Object>> pairs = new HashSet<List<Object>>();
			
			for (ClassificationTarget target : analysisParameters.getDataType().getTargets()) {
				final List<Object> item = new ArrayList<Object>(2);
				final InteractionType type = InteractionType.valueOf(target.getInteractionClass().getSimpleName().toUpperCase());
				Validate.notNull(type, "Invalid interaction type!");
				item.add(target.getPatientClass());
				item.add(type);
				pairs.add(item);
			}
			
			int idx = 0;
			for (List<Object> pair : pairs) {
				if (sb.length() == 0) {
					sb.append("((");
				} else {
					sb.append(") or (");
				}
				
				sb.append("analysis.patientClass = :patientClass").append(idx)
				  .append(" and analysis.interactionType = :interactionType").append(idx);
				
				paramNames.add("patientClass" + idx);
				params.add(pair.get(0));
				
				paramNames.add("interactionType" + idx);
				params.add(pair.get(1));
				
				idx++;
			}
			
			if (sb.length() > 0) {
				sb.append("))");
				
				query.addWhere(sb.toString(), paramNames, params);
			}

		}
		
		query.addWhere("analysis.gender in (:genders)", "genders", analysisParameters.getGenders())
			 .addWhere("analysis.ageGroup in (:ageGroups)", "ageGroups", analysisParameters.getAgeGroups());
		
		return query;
	}
	
	/**
	 * Applies classifications to the query.
	 * 
	 * @param query
	 * @param analysisParameters
	 * @return
	 */
	private static QueryBuilder applyClassifications(final QueryBuilder query, final AnalysisParameters analysisParameters) {
		
		/*
		 * The -1 is a hack to work around a bug in Oracle 11g
		 */
		final Collection<Classification> classifications = analysisParameters.getClassifications();
		return query.addWhere("analysis.classification in (:classifications" + (classifications != null && classifications.size() == 1 ? ", -1)" : ")"), "classifications", classifications);
	}

	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.impl.AbstractAnalysisQueries#getZipcodeName(com.hmsinc.epicenter.model.analysis.AnalysisParameters)
	 */
	@Override
	protected String getZipcodeName(AnalysisParameters analysisParameters) {
		return "geoZipcode";
	}

}
