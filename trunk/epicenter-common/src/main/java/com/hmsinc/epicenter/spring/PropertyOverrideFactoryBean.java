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
package com.hmsinc.epicenter.spring;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Simple factory bean that takes a Properties object and merges another
 * Properties object into it.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: PropertyOverrideFactoryBean.java 557 2007-12-13 16:03:30Z steve.kondik $
 * 
 */
public class PropertyOverrideFactoryBean implements FactoryBean {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private Resource overrideLocation;

	private Properties properties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		try {
			final Properties overrides = PropertiesLoaderUtils.loadProperties(overrideLocation);
			logger.info("Loaded properties override file: " + overrideLocation.getFilename());
			for (Map.Entry<Object, Object> entry : overrides.entrySet()) {
				properties.put(entry.getKey(), entry.getValue());
			}
		} catch (IOException e) {
			logger.debug("No properties override file found.");
		}
		return properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<?> getObjectType() {
		return Properties.class;
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
	 * @return the overrideLocation
	 */
	public Resource getOverrideLocation() {
		return overrideLocation;
	}

	/**
	 * @param overrideLocation
	 *            the overrideLocation to set
	 */
	@Required
	public void setOverrideLocation(Resource overrideLocation) {
		this.overrideLocation = overrideLocation;
	}

	/**
	 * @return the properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties
	 *            the properties to set
	 */
	@Required
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
