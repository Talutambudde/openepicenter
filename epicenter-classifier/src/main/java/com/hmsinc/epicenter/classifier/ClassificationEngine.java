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

import java.util.Collection;
import java.util.List;

import com.hmsinc.epicenter.classifier.config.ClassifierConfig;

/**
 * A ClassificationEngine is the entry point for free-text classifiers.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ClassificationEngine.java 533 2007-12-10 03:53:20Z steve.kondik $
 */
public interface ClassificationEngine {

	/**
	 * Initialize this classifier.
	 * 
	 * @param config
	 * @throws Exception
	 */
	public void init(final ClassifierConfig config) throws Exception;

	/**
	 * Classify some text.
	 * 
	 * @param text
	 * @return the list of classifications
	 */
	public List<String> classify(final String text);

	/**
	 * Get the list of categories that this classifier has been trained on.
	 * 
	 * @return the categories
	 */
	public Collection<String> getCategories();

	/**
	 * Get the name of this classifier.
	 * 
	 * @return the name
	 */
	public String getName();

	/**
	 * Get this classifier's version.
	 * 
	 * @return the version
	 */
	public String getVersion();

	/**
	 * Gets a long description of this classifier.
	 * 
	 * @return the description
	 */
	public String getDescription();

}
