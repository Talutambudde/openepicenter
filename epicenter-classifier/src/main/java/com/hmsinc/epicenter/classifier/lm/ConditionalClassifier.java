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
import com.aliasi.classify.ScoredClassification;
import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * A ConditionalClassifier returns the best category if a specified threshold
 * (score) is reached.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ConditionalClassifier.java 1090 2008-02-24 23:19:50Z steve.kondik $
 */
public class ConditionalClassifier extends AbstractLMClassifier {

	private static final double DEFAULT_THRESHOLD = Math.log(.065) / Math.log(2);

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

				final Classification jc = classifier.classify(filtered);
				if (jc != null) {

					Validate.isTrue(ScoredClassification.class.isAssignableFrom(jc.getClass()),
							"Underlying classifier does not support ScoredClassifications");
					final ScoredClassification jcs = (ScoredClassification) jc;

					// FIXME: This probably isn't right.
					if (jcs.size() > 0 && jcs.score(0) >= DEFAULT_THRESHOLD) {
						final String category = jcs.bestCategory();
						if (!ignoredCategories.contains(category)) {
							result.add(category);
						}
						logger.debug("Result for \"{}\": {}  (probability={})", new Object[] { text, category, jcs.score(0) });
					}
				}

			}
		}

		return result;

	}

}
