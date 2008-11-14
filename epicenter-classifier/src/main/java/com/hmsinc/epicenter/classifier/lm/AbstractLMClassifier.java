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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.aliasi.classify.ClassifierEvaluator;
import com.aliasi.classify.LMClassifier;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.hmsinc.epicenter.classifier.AbstractClassificationEngine;
import com.hmsinc.epicenter.classifier.config.Category;
import com.hmsinc.epicenter.classifier.config.ClassifierConfig;
import com.hmsinc.epicenter.classifier.config.Entry;
import com.hmsinc.epicenter.classifier.config.EntryType;
import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * A base class for LingPipe DynamicLMClassifiers.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AbstractLMClassifier.java 1496 2008-04-08 18:39:10Z steve.kondik $
 */
public abstract class AbstractLMClassifier extends AbstractClassificationEngine {

	private static final int DEFAULT_NGRAM_SIZE = 5;

	@SuppressWarnings("unchecked")
	protected LMClassifier classifier = null;

	protected Set<String> ignoredCategories = new HashSet<String>();
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.AbstractClassificationEngine#doInit(com.hmsinc.epicenter.classifier.config.ClassifierConfig)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected synchronized void doInit(ClassifierConfig config) throws Exception {

		Validate.notNull(config.getTrainingSet(), "Training set was null!");
		Validate.notNull(config.getTrainingSet().getCategories(), "No categories are defined.");
		
		// Extract categories
		final Set<String> categories = new HashSet<String>();
		
		for (Category category : config.getTrainingSet().getCategories()) {
			categories.add(category.getName());
			if (category.isIgnore() != null && category.isIgnore()) {
				ignoredCategories.add(category.getName());
			}
		}
		
		final NaiveBayesClassifier nbc = new NaiveBayesClassifier(categories.toArray(new String[categories.size()]), new IndoEuropeanTokenizerFactory(), 
				(config.getNgramSize() == null ? DEFAULT_NGRAM_SIZE : config.getNgramSize()));

		// Train the classifier
		for (Category category : config.getTrainingSet().getCategories()) {

			logger.debug("Training category: {}", category.getName());
			for (Entry entry : category.getEntries()) {
				if (entry.getValue() == null) {
					logger.warn("Null value in training set!");
				} else if (entry.getType() == null || entry.getType().equals(EntryType.WORD)) {
					nbc.train(category.getName(), entry.getValue());
				}
			}
		}

		logger.debug("Compiling the classifier..");
		classifier = (LMClassifier) AbstractExternalizable.compile(nbc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#getCategories()
	 */
	public Collection<String> getCategories() {
		
		Validate.notNull(classifier, "Classifier has not been initialized!");
		
		return Arrays.asList(classifier.categories());
	}

	/**
	 * Evaluates the classifier's performance against a CSV file.
	 * 
	 * The format of the test set should be "complaint,category", one per line.
	 * 
	 * @param testSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public ClassifierEvaluator<String, ? extends com.aliasi.classify.Classification> test(final String testSet) {

		Validate.notNull(classifier, "Classifier has not been initialized!");
		ClassifierEvaluator<String, ? extends com.aliasi.classify.Classification> evaluator = null;

		BufferedReader br = null;
		try {

			br = new BufferedReader(new FileReader(testSet), ",".charAt(0));
			final CSVParser csv = new CSVParser(br);

			// FIXME: Not sure how to make this typesafe
			evaluator = new ClassifierEvaluator(classifier, classifier.categories());
			logger.info("Testing classifier using testing source: {}", testSet);

			String[] fields;
			while ((fields = csv.getLine()) != null) {

				if (fields.length == 2) {
					final String category = StringUtils.trimToNull(fields[1]);
					final String complaint = ClassifierUtils.filter(fields[0], stopwords).toString();
					if (category != null && complaint != null) {
						evaluator.addCase(category, complaint);
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug(evaluator.confusionMatrix().toString());
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return evaluator;
	}
	
	/**
	 * @return the classifier
	 */
	@SuppressWarnings("unchecked")
	public LMClassifier getClassifier() {
		return classifier;
	}

}
