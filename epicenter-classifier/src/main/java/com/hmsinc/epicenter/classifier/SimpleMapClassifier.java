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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.hmsinc.epicenter.classifier.config.Category;
import com.hmsinc.epicenter.classifier.config.ClassifierConfig;
import com.hmsinc.epicenter.classifier.config.Entry;
import com.hmsinc.epicenter.classifier.config.EntryType;

/**
 * A MapClassifier is a simple lookup map with no fancy parsing.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: SimpleMapClassifier.java 1758 2008-06-17 18:10:12Z steve.kondik $
 */
public class SimpleMapClassifier extends AbstractClassificationEngine {

	private final Map<String, String> classifier = new HashMap<String, String>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.AbstractClassificationEngine#doInit(com.hmsinc.epicenter.classifier.config.ClassifierConfig)
	 */
	@Override
	protected synchronized void doInit(ClassifierConfig config) throws Exception {

		// Convert the training set to a Map
		for (Category category : config.getTrainingSet().getCategories()) {
			for (Entry entry : category.getEntries()) {
				if (entry.getType() == null || entry.getType().equals(EntryType.MAP)) {
					if (entry.getValue() == null) {
						logger.warn("Null entry in classifier!");
					} else {
						classifier.put(entry.getValue(), category.getName());
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#classify(java.lang.String)
	 */
	public List<String> classify(String text) {

		final List<String> result = new ArrayList<String>();
		if (text != null) {
			final String filtered = StringUtils.trimToNull(text);
			if (filtered != null && classifier.containsKey(filtered)) {
				result.add(classifier.get(filtered));
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#getCategories()
	 */
	public Collection<String> getCategories() {
		final Set<String> categories = new HashSet<String>();
		categories.addAll(classifier.values());
		return categories;
	}

}
