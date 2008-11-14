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
package com.hmsinc.epicenter.webapp.util;

import org.apache.commons.lang.Validate;
import org.owasp.validator.html.Policy;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

/**
 * FactoryBean for Antisamy policies.
 * 
 * @author shade
 * @version $Id: AntiSamyPolicyFactoryBean.java 1223 2008-03-05 20:12:59Z steve.kondik $
 */
public class AntiSamyPolicyFactoryBean implements FactoryBean {

	private Resource location;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {

		Validate.notNull(location, "Policy file location not specified.");
		Validate.isTrue(location.exists(), "Policy file " + location.toString() + " does not exist.");

		return Policy.getInstance(location.getFile());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<?> getObjectType() {
		return Policy.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @return the location
	 */
	public Resource getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	@Required
	public void setLocation(Resource location) {
		this.location = location;
	}

}
