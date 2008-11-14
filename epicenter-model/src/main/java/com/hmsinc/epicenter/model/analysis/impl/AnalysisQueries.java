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

import org.hibernate.Criteria;
import org.hibernate.Query;

import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.geography.Geography;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AnalysisQueries.java 989 2008-02-19 14:58:46Z steve.kondik $
 */
public interface AnalysisQueries {

	public <T extends Geography, A extends QueryableAttribute> Query createAggregatedAnalysisQuery(
			final AnalysisParameters analysisParameters, final Class<T> aggregateGeographyType,
			final Class<A> aggregateAttributeType);

	public <T extends Geography> Query createCombinedAnalysisQuery(final AnalysisParameters analysisParameters,
			final Class<T> aggregateGeographyType);

	public <T extends Geography> Query createTotalAnalysisQuery(final AnalysisParameters analysisParameters,
			final Class<T> aggregateGeographyType);

	public Criteria createGetCasesQuery(final AnalysisParameters analysisParameters, Long offset, Long numRows);

	public Query createGetCasesCountQuery(final AnalysisParameters analysisParameters);

}