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

import static com.hmsinc.epicenter.util.FormatUtils.camelize;

import java.util.Arrays;

import org.hibernate.Query;
import org.hibernate.Session;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.hibernate.QueryBuilder;

/**
 * Generates a counts query using a standard tables. This is much slower than
 * using the materialized view, and not recommended for production use.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:StandardCountsAnalysisQuery.java 205 2007-09-26 16:52:01Z
 *          steve.kondik $
 * 
 */
public class StandardAnalysisQueries extends AbstractAnalysisQueries {

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.impl.AnalysisQueries#createAggregatedAnalysisQuery(com.hmsinc.epicenter.model.analysis.AnalysisParameters, java.lang.Class, java.lang.Class)
	 */
	public <T extends Geography, A extends QueryableAttribute> Query createAggregatedAnalysisQuery(
			AnalysisParameters analysisParameters, Class<T> aggregateGeographyType, Class<A> aggregateAttributeType) {
		
		final QueryBuilder query = new QueryBuilder().addEntity(Interaction.class, "interaction");
		query.addEntity(Zipcode.class, "geozip");
		query.addJoin("join interaction.patientDetail as pd").addJoin("join interaction.classifications as classification");
		
		applyAttributes(query, analysisParameters);
		applyClassifications(query, analysisParameters);
		
		if (AnalysisLocation.FACILITY.equals(analysisParameters.getLocation())) {
			query.addWhere("geozip.name = interaction.patient.facility.zipcode");
		} else {
			query.addWhere("geozip.name = pd.zipcode");
		}
		
		final String aggregateName = applyGeography(query, aggregateGeographyType, analysisParameters);
		final String aggregateAttributeName;
		if (Gender.class.equals(aggregateAttributeType)) {
			aggregateAttributeName = "pd.gender";
		} else if (Classification.class.equals(aggregateAttributeType)) {
			aggregateAttributeName = "classification";
		} else {
			aggregateAttributeName = "interaction" + camelize(aggregateAttributeType.getSimpleName());
		}

		final long offset = getTimeOffset(analysisParameters);
		
		query.addGroupProperty(aggregateName + ".id").addGroupProperty(aggregateAttributeName + ".id")
			.addGroupProperty(getHqlForPeriod(analysisParameters.getPeriod()) + "(interaction.interactionDate, " + offset + ")")
			.addProperty("count(*)")
			.addOrder(aggregateName + ".id")
			.addOrder(aggregateAttributeName + ".id");
		
		return query.toQuery((Session)entityManager.getDelegate(), true);

	}


	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.impl.AnalysisQueries#createCombinedAnalysisQuery(com.hmsinc.epicenter.model.analysis.AnalysisParameters, java.lang.Class)
	 */
	public <T extends Geography> Query createCombinedAnalysisQuery(AnalysisParameters analysisParameters,
			Class<T> aggregateGeographyType) {
		
		final QueryBuilder query = new QueryBuilder().addEntity(Interaction.class, "interaction");
		query.addEntity(Zipcode.class, "geozip");
		query.addJoin("join interaction.patientDetail as pd").addJoin(
				"join interaction.classifications as classification");
		
		applyAttributes(query, analysisParameters);
		applyClassifications(query, analysisParameters);
		
		if (AnalysisLocation.FACILITY.equals(analysisParameters.getLocation())) {
			query.addWhere("geozip.name = interaction.patient.facility.zipcode");
		} else {
			query.addWhere("geozip.name = pd.zipcode");
		}
		
		final String aggregateName = applyGeography(query, aggregateGeographyType, analysisParameters);
		final long offset = getTimeOffset(analysisParameters);
		
		query.addGroupProperty(aggregateName + ".id")
			.addGroupProperty(getHqlForPeriod(analysisParameters.getPeriod()) + "(interaction.interactionDate, " + offset + ")")
			.addProperty("count(*)")
			.addOrder(aggregateName + ".id");
		
		return query.toQuery((Session)entityManager.getDelegate(), true);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.CountsAnalysisQuery#createGetCasesCountQuery(java.util.Map,
	 *      com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.epicenter.model.geography.Geography)
	 */
	public Query createGetCasesCountQuery(AnalysisParameters analysisParameters) {
		throw new UnsupportedOperationException("Not implemented without view");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.analysis.impl.CountsAnalysisQuery#createGetCasesQuery(java.util.Map,
	 *      com.hmsinc.epicenter.model.analysis.AnalysisParameters,
	 *      com.hmsinc.epicenter.model.geography.Geography, java.lang.Long,
	 *      java.lang.Long)
	 */
	public org.hibernate.Criteria createGetCasesQuery(AnalysisParameters analysisParameters, Long offset, Long numRows) {
		throw new UnsupportedOperationException("Not implemented without view");
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
		
		final QueryBuilder query = new QueryBuilder().addEntity(Interaction.class, "interaction");
		query.addEntity(Zipcode.class, "geozip");
		query.addJoin("join interaction.patientDetail as pd");
		
		applyAttributes(query, analysisParameters);
		
		if (AnalysisLocation.FACILITY.equals(analysisParameters.getLocation())) {
			query.addWhere("geozip.name = interaction.patient.facility.zipcode");
		} else {
			query.addWhere("geozip.name = pd.zipcode");
		}
		
		final String aggregateName = applyGeography(query, aggregateGeographyType, analysisParameters);
		final long offset = getTimeOffset(analysisParameters);
		
		query.addGroupProperty(aggregateName + ".id")
			.addGroupProperty(getHqlForPeriod(analysisParameters.getPeriod()) + "(interaction.interactionDate, " + offset + ")")
			.addProperty("count(*)")
			.addOrder(aggregateName + ".id");
		
		return query.toQuery((Session)entityManager.getDelegate(), true);
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.analysis.impl.AbstractAnalysisQueries#getZipcodeName(com.hmsinc.epicenter.model.analysis.AnalysisParameters)
	 */
	@Override
	protected String getZipcodeName(AnalysisParameters analysisParameters) {
		return "geozip";
	}
	
	/**
	 * Appends common attributes to the query.
	 * 
	 * @param query
	 * @param analysisParameters
	 * @return
	 */
	private static QueryBuilder applyAttributes(final QueryBuilder query, final AnalysisParameters analysisParameters) {
		
		final String dateProp = "interaction.interactionDate ";

		if (analysisParameters.getEndDate() == null) {

			query.addWhere(dateProp + ">= :startDate", "startDate", analysisParameters.getStartDate());
		} else {

			query.addWhere(dateProp + "between :startDate and :endDate", Arrays.asList("startDate", "endDate"), Arrays.asList(analysisParameters.getStartDate(), analysisParameters.getEndDate()) );
		}

		query.addWhere("pd.gender in (:genders)", "genders", analysisParameters.getGenders())
			 .addWhere("pd.ageGroup in (:ageGroups)", "ageGroups", analysisParameters.getAgeGroups());
		
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
		
		return query.addWhere("classification in (:classifications)", "classifications", analysisParameters.getClassifications());
	}
}
