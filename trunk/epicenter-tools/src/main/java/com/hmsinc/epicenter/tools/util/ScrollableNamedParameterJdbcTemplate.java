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
package com.hmsinc.epicenter.tools.util;

import java.sql.ResultSet;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.jdbc.core.namedparam.ParsedSql;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * This subclass of NamedParameterJdbcTemplate makes creating of scrollable
 * ResultSets easy by exposing the
 * PreparedStatementCreatorFactory#setResultSetType(int) through a method on the
 * JdbcTemplate.
 * 
 * @author Richard L. Burton III, i71055
 * @since Spring 2.5
 */
public class ScrollableNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {

	/**
	 * The specific type of ResultSet to use. Defaults to
	 * ResultSet#TYPE_FORWARD_ONLY
	 */
	private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;

	/**
	 * Create a new NamedParameterJdbcTemplate for the given DataSource.
	 * <p>
	 * Creates a classic Spring JdbcTemplate and wraps it.
	 * 
	 * @param dataSource
	 *            the JDBC DataSource to access
	 */
	public ScrollableNamedParameterJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Create a new SimpleJdbcTemplate for the given classic Spring
	 * JdbcTemplate.
	 * 
	 * @param jdbcOperations
	 *            the classic Spring JdbcTemplate to wrap
	 */
	public ScrollableNamedParameterJdbcTemplate(JdbcOperations jdbcOperations) {
		super(jdbcOperations);
	}

	public int getResultSetType() {
		return resultSetType;
	}

	/**
	 * Set whether to use prepared statements that return a specific type of
	 * ResultSet.
	 * 
	 * @param resultSetType
	 *            the ResultSet type
	 * @see java.sql.ResultSet#TYPE_FORWARD_ONLY
	 * @see java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 * @see java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 */
	public void setResultSetType(int resultSetType) {
		this.resultSetType = resultSetType;
	}

	/**
	 * Build a PreparedStatementCreator based on the given SQL and named
	 * parameters.
	 * <p>
	 * Note: Not used for the <code>update</code> variant with generated key
	 * handling.
	 * 
	 * @param sql
	 *            SQL to execute
	 * @param paramSource
	 *            container of arguments to bind
	 * @return the corresponding PreparedStatementCreator
	 */
	@Override
	protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
		ParsedSql parsedSql = getParsedSql(sql);
		String sqlToUse = NamedParameterUtils.substituteNamedParameters(parsedSql, paramSource);
		Object[] params = NamedParameterUtils.buildValueArray(parsedSql, paramSource, null);
		int[] paramTypes = NamedParameterUtils.buildSqlTypeArray(parsedSql, paramSource);
		PreparedStatementCreatorFactory pscf = newPreparedStatementCreatorFactory(sqlToUse, paramTypes);
		return pscf.newPreparedStatementCreator(params);
	}

	/**
	 * Constructs the PreparedStatementCreatorFactory and allows subclasses to
	 * hook into the creation of the PreparedStatementCreatorFactory.
	 * 
	 * @param sqlToUse
	 *            SQL to execute
	 * @param paramTypes
	 *            The Jdbc parameter types.
	 * @return The newly created PreparedStatementCreatorFactory.
	 */
	protected PreparedStatementCreatorFactory newPreparedStatementCreatorFactory(String sqlToUse, int[] paramTypes) {
		PreparedStatementCreatorFactory pscf = new PreparedStatementCreatorFactory(sqlToUse, paramTypes);
		pscf.setResultSetType(resultSetType);
		return pscf;
	}

}