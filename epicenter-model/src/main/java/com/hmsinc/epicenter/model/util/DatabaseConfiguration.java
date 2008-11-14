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
package com.hmsinc.epicenter.model.util;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Required;

/**
 * Helper class for determining database-specific values.
 * 
 * @author shade
 * @version $Id$
 */
public class DatabaseConfiguration {

	@Resource
	private Map<String, String> databaseConstantsMap;

	private String databaseType;

	@PostConstruct
	public void validate() {
		Validate.notNull(databaseType, "Database name must be specified!");
		Validate.isTrue(databaseConstantsMap.containsKey("hibernate-" + databaseType), "Unsupported database: " + databaseType);
	}

	public String getQuartzDelegate() {
		return databaseConstantsMap.get("quartz-" + databaseType);
	}

	public String getHibernateDialect() {
		return databaseConstantsMap.get("hibernate-" + databaseType);
	}

	/**
	 * @return the databaseType
	 */
	public String getDatabaseType() {
		return databaseType;
	}

	/**
	 * @param databaseType the databaseType to set
	 */
	@Required
	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

}
