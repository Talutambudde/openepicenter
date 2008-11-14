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
package com.hmsinc.epicenter.classifier;

import java.util.List;
import java.util.Map;

/**
 * @author shade
 * @version $Id: NamedItemClassificationEngine.java 1718 2008-06-10 15:39:55Z steve.kondik $
 */
public interface NamedItemClassificationEngine extends ClassificationEngine {

	/**
	 * Classify some text with correlation between multiple named items.
	 * 
	 * @param text
	 * @return the list of classifications
	 */
	public List<String> classifyNamedItems(final Map<String, Object> items);
	
}
