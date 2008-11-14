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

import com.aliasi.classify.Classification;
import com.aliasi.classify.JointClassification;
import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * A RankedClassifier returns a list of categories, ordered by best to worst.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: RankedClassifier.java 1090 2008-02-24 23:19:50Z steve.kondik $
 */
public class RankedClassifier extends AbstractLMClassifier {

	private static final int DEFAULT_NUM_RANKS = 3;

	private static final boolean DEFAULT_STOP_ON_OTHER = true;

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

				final Classification c = classifier.classify(filtered);
				if (c != null) {
					Validate.isTrue(JointClassification.class.isAssignableFrom(c.getClass()),
							"Underlying classifier does not support JointClassification");

					final JointClassification classification = (JointClassification) c;

					for (int i = 0; i < DEFAULT_NUM_RANKS; i++) {
						final String category = classification.category(i);
						logger.debug("Result for \"{}\": {}  (score={}, rank={})", new Object[] { text, category, classification.score(i), i });

						if (DEFAULT_STOP_ON_OTHER && category.equalsIgnoreCase("other")) {
							break;
						}
						if (!ignoredCategories.contains(category)) {
							result.add(category);
						}
					}
				}
			}
		}

		return result;
	}

}
