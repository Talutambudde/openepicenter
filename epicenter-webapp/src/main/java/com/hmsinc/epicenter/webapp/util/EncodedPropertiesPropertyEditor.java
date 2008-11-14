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

import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shade
 * @version $Id: EncodedPropertiesPropertyEditor.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public class EncodedPropertiesPropertyEditor extends PropertyEditorSupport {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyEditorSupport#getAsText()
	 */
	@Override
	public String getAsText() {
		
		String ret = null;
		if (getValue() instanceof Properties) {
		
			final Properties props = (Properties)getValue();
			final Map<String, String> map = new HashMap<String, String>();
			
			for (String key : props.stringPropertyNames()) {
				map.put(key, props.getProperty(key));
			}
			
			ret = URLUtils.mapToQueryString(map);
		}
		return ret;
		
		
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyEditorSupport#setAsText(java.lang.String)
	 */
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		
		logger.debug("Decode: {}", text);
		
		final Properties props = new Properties();
		final Map<String, String> map = URLUtils.queryStringToMap(text);
		props.putAll(map);
		this.setValue(props);
	}

	
}
