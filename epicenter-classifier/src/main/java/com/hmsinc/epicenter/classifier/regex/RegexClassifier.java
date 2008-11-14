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
package com.hmsinc.epicenter.classifier.regex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.hmsinc.epicenter.classifier.AbstractClassificationEngine;
import com.hmsinc.epicenter.classifier.NamedItemClassificationEngine;
import com.hmsinc.epicenter.classifier.config.Category;
import com.hmsinc.epicenter.classifier.config.ClassifierConfig;
import com.hmsinc.epicenter.classifier.config.CoIndicators;
import com.hmsinc.epicenter.classifier.config.Entry;
import com.hmsinc.epicenter.classifier.config.EntryType;
import com.hmsinc.epicenter.classifier.config.Item;
import com.hmsinc.epicenter.classifier.config.TrainingSet;
import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * A classifier based on pattern matching.
 * 
 * @author aaron.cois
 * @author shade
 * @version $Id: RegexClassifier.java 1758 2008-06-17 18:10:12Z steve.kondik $
 */
public class RegexClassifier extends AbstractClassificationEngine implements NamedItemClassificationEngine {

	private TrainingSet ts = null;

	private final Map<Entry, Pattern> patternCache = new HashMap<Entry, Pattern>();

	private final Map<String, Expression> expressionCache = new HashMap<String, Expression>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.AbstractClassificationEngine#doInit(com.hmsinc.epicenter.classifier.config.ClassifierConfig)
	 */
	@Override
	protected synchronized void doInit(ClassifierConfig config) throws Exception {
		ts = config.getTrainingSet();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#getCategories()
	 */
	public Collection<String> getCategories() {
		final List<String> ret = new ArrayList<String>();
		for (Category category : ts.getCategories()) {
			ret.add(category.getName());
		}
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.NamedItemClassificationEngine#classifyNamedItems(java.util.Map)
	 */
	public List<String> classifyNamedItems(Map<String, Object> items) {

		final List<String> classes = new ArrayList<String>();
		
		// The first value is must always be our CC
		final Object first = items.entrySet().iterator().next().getValue();
		if (first != null) {
			Validate.isTrue(first instanceof String, "String complaint must be the first item in attribute list.  Was: " + first.getClass().getName());

			final String cc = filter(ts, (String) first);

			/*
			 * First check to see if we have any noneIndicators in this CC...if so,
			 * we classify as "None" and test nothing else.
			 */
			if (!isNoneIndicator(ts, cc, items)) {

				// for each classification
				for (Category category : ts.getCategories()) {

					// for each indicator within this classification
					for (Entry entry : category.getEntries()) {

						// We can also evaluate expressions here
						if (match(entry, cc, items) && !classes.contains(category.getName())
								&& (category.isIgnore() == null || !category.isIgnore())) {
							classes.add(category.getName());
						}
					}
				}
			}
		}
		return classes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.classifier.ClassificationEngine#classify(java.lang.String)
	 */
	public List<String> classify(String text) {

		final String cc = filter(ts, text);
		final List<String> classes = new ArrayList<String>();

		/*
		 * First check to see if we have any noneIndicators in this CC...if so,
		 * we classify as "None" and test nothing else.
		 */
		if (!isNoneIndicator(ts, cc, null)) {

			// for each classification
			for (Category category : ts.getCategories()) {

				// for each indicator within this classification
				for (Entry entry : category.getEntries()) {

					if (match(entry, cc) && !classes.contains(category.getName())
							&& (category.isIgnore() == null || !category.isIgnore())) {
						classes.add(category.getName());

					}
				}
			}
		}
		return classes;
	}

	/**
	 * @param ts
	 * @param text
	 * @return
	 */
	private String filter(final TrainingSet ts, final String text) {
		final String cc;
		if (ts.getStopwords() != null) {
			if (ts.getStopwords().isOverride()) {
				// make a dummy empty set to pass in, since stopwords are
				// overridden
				final Set<String> set = new HashSet<String>();
				cc = ClassifierUtils.filterAllowNumbers(text, set).toString();
			} else {
				cc = ClassifierUtils.filterAllowNumbers(text, stopwords).toString();
			}
		} else {
			cc = ClassifierUtils.filterAllowNumbers(text, stopwords).toString();
		}
		logger.trace("Filtered input \"{}\" to: {}", text, cc);
		return cc;
	}
	
	/**
	 * @param entry
	 * @param cc
	 * @return
	 */
	public boolean match(final Entry entry, final String cc) {
		return match(entry, cc, null);
	}

	/**
	 * @param entry
	 * @param cc
	 * @return
	 */
	public boolean match(final Entry entry, final String cc, final Map<String, Object> attrs) {

		return !isNegativeIndicator(entry, cc, attrs) 
			&& matchPattern(entry, cc) 
			&& matchCondition(entry, attrs) 
			&& matchCoIndicators(entry.getCoIndicators(), cc, attrs);

	}

	/**
	 * @param entry
	 * @param cc
	 * @param attrs
	 * @return
	 */
	private boolean matchCoIndicators(final CoIndicators coIndicators, final String cc, final Map<String, Object> attrs) {

		boolean matched = false;

		// Do we need a co-indicator as well?
		if (coIndicators == null) {
			matched = true;
		} else {

			// look for co-indicators
			for (Entry coEntry : coIndicators.getEntries()) {
				
				final boolean match = match(coEntry, cc, attrs);
				
				if (match) {
					matched = true;
					break;
				} 
			}

		}
		
		return matched;
	}

	/**
	 * @param entry
	 * @param cc
	 * @return
	 */
	private boolean matchPattern(final Entry entry, final String cc) {
		final Pattern p = getPatternForEntry(entry);
		final boolean ret = p.matcher(cc).matches();
		logger.trace("Match pattern {} for {}: {}", new Object[] { p.pattern(), cc, ret } );
		return ret;
	}

	/**
	 * Evaluates a condition using JEXL.
	 * 
	 * @param entry
	 * @param attributes
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private boolean matchCondition(final Entry entry, final Map<String, Object> attributes) {

		boolean conditionMatched = true;

		if (entry.getCondition() != null && attributes != null && attributes.size() > 0) {

			try {

				// JEXL doesn't cache internally, so do it ourselves
				final Expression e;
				if (expressionCache.containsKey(entry.getCondition())) {
					e = expressionCache.get(entry.getCondition());
				} else {
					e = ExpressionFactory.createExpression(entry.getCondition());
					expressionCache.put(entry.getCondition(), e);
				}

				final JexlContext jc = JexlHelper.createContext();
				jc.getVars().putAll(attributes);

				final Object result = e.evaluate(jc);
				Validate.isTrue(result instanceof Boolean, "Condition must return a boolean result.");
				conditionMatched = (Boolean) result;

			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			
			logger.trace("Match condition {} for {}: {}", new Object[] { entry.getCondition(), attributes, conditionMatched } );
		}
		
		return conditionMatched;
	}

	/**
	 * @param ts
	 * @param cc
	 * @return
	 */
	private boolean isNoneIndicator(final TrainingSet ts, final String cc, final Map<String, Object> attrs) {

		boolean hardNone = false;
		if (ts.getNoneIndicators() != null) {
			for (Entry entry : ts.getNoneIndicators().getEntries()) {

				// make regex, depending on entry type
				if (match(entry, cc, attrs)) {
					hardNone = true;
					break;
				}
			}
		}
		return hardNone;
	}

	/**
	 * @param entry
	 * @param cc
	 * @return
	 */
	private boolean isNegativeIndicator(final Entry entry, final String cc, final Map<String, Object> attrs) {

		boolean negativeIndicatorFound = false;

		// if we have negative indicators for this entry
		if (entry.getNegativeIndicators() != null) {

			for (Entry negEntry : entry.getNegativeIndicators().getEntries()) {

				if (match(negEntry, cc, attrs)) {
					negativeIndicatorFound = true;
					break;
				}
			}
		}

		return negativeIndicatorFound;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Pattern getPatternForEntry(final Entry entry) {

		final Pattern p;
		if (patternCache.containsKey(entry)) {

			p = patternCache.get(entry);

		} else {

			final String regex;
			if (entry.getType() != null && EntryType.SEGMENT.equals(entry.getType())) {
				regex = makeSegmentMatch(combineItems(entry));
			} else {
				regex = makeWordMatch(combineItems(entry));
			}
			p = Pattern.compile(regex);

			patternCache.put(entry, p);

		}

		return p;
	}

	/**
	 * @param entry
	 * @return
	 */
	private static String combineItems(final Entry entry) {

		final Set<String> itemList = new HashSet<String>();

		if (entry.getValue() != null) {
			itemList.add(entry.getValue());
		}

		if (entry.getItems() != null && entry.getItems().size() > 0) {
			for (Item item : entry.getItems()) {
				if (EntryType.SEGMENT.equals(item.getType())) {
					final StringBuilder sb = new StringBuilder();
					sb.append("((\\s+|^)").append(item.getValue()).append("(\\s+|$))");
					itemList.add(sb.toString());
				} else {
					itemList.add(item.getValue());
				}
			}
		}

		final String ret;
		if (itemList.size() == 1) {
			ret = itemList.iterator().next();
		} else if (itemList.size() > 1) {
			final StringBuilder sb = new StringBuilder();
			sb.append("(").append(StringUtils.join(itemList, "|")).append(")");
			ret = sb.toString();
		} else {
			throw new IllegalArgumentException("Invalid entry: " + entry.toString());
		}

		return ret;

	}

	/**
	 * @param fragment
	 * @return
	 */
	private static String makeSegmentMatch(final String fragment) {
		return new StringBuilder(".*(\\s+|^)").append(fragment).append("(\\s+|$).*").toString();
	}

	/**
	 * @param fragment
	 * @return
	 */
	private static String makeWordMatch(final String fragment) {
		return new StringBuilder(".*").append(fragment).append(".*").toString();
	}

}
