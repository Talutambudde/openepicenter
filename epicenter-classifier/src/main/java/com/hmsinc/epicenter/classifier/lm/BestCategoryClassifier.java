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
package com.hmsinc.epicenter.classifier.lm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
public class BestCategoryClassifier extends AbstractLMClassifier {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#classify(java.lang.String)
	 */
	public List<String> classify(String text) {

		Validate.notNull(classifier, "Classifier has not been initialized!");
		
		final List<String> result = new ArrayList<String>();
		if (text != null) {

			final CharSequence filtered = ClassifierUtils.filter(text, stopwords);

			if (filtered != null) {
				final String category = classifier.classify(filtered).bestCategory();
				if (!ignoredCategories.contains(category)) {
					result.add(category);
				}
				logger.debug("Result for \"{}\": {}", text, category);
			}
		}

		return result;

	}

}
