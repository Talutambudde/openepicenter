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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.classifier.config.ClassifierConfig;
import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * A base class for classifiers.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AbstractClassificationEngine.java 1093 2008-02-25 01:43:33Z steve.kondik $
 */
public abstract class AbstractClassificationEngine implements ClassificationEngine {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String name;

	private String version;

	private String description;

	protected Set<String> stopwords;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#init(com.hmsinc.epicenter.classifier.config.ClassifierConfig)
	 */
	public synchronized final void init(final ClassifierConfig config) throws Exception {

		Validate.notNull(config, "Classifier config must be given!");
		logger.info("Creating classifier: \"{}\" [version: {}]", config.getName(), config.getVersion());

		doInit(config);

		this.name = config.getName();
		this.version = config.getVersion();
		this.description = config.getDescription();
		logger.info("Categories: {}", getCategories());
		
		if (config.getTrainingSet() != null && config.getTrainingSet().getStopwords() != null) {
			stopwords = new HashSet<String>();
			if (config.getTrainingSet().getStopwords().isOverride() == null || !config.getTrainingSet().getStopwords().isOverride()) {
				stopwords.addAll(ClassifierUtils.BASIC_STOPWORDS);
			}
			if (config.getTrainingSet().getStopwords().getStopwords() != null) {
				stopwords.addAll(config.getTrainingSet().getStopwords().getStopwords());
			}
		} else {
			stopwords = ClassifierUtils.BASIC_STOPWORDS;
		}
	}

	
	/**
	 * Initialize this classifier.
	 * 
	 * @param config
	 * @throws Exception
	 */
	protected abstract void doInit(final ClassifierConfig config) throws Exception;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

}
