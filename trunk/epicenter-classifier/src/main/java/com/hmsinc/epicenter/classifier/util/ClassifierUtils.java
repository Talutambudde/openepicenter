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
package com.hmsinc.epicenter.classifier.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ClassifierUtils.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
public final class ClassifierUtils {

	public static final Set<String> BASIC_STOPWORDS = makeStopSet("a", "about", "above", "ack", "across", "adj", "after", "afterwards",
			"again", "against", "ago", "albeit", "all", "almost", "alone", "along", "already", "also", "although", "always", "am",
			"ambulance", "among", "amongst", "an", "and", "another", "any", "anyhow", "anyone", "anything", "anywhere", "april", "are",
			"area", "around", "arr", "arrive", /*"as",*/ "at", "august", "bad", /*"be",*/ "became", "because", "become", "becomes", "becoming",
			"bed", "been", "before", "beforehand", "behind", "being", "below", "beside", "besides", "between", "beyond", "bilat",
			"bilateral", "both", /*"brother",*/ "but", "by", /*"ca",*/ "can", "cannot", "cc", "check", "child", /*"co",*/ "could", /*"cp",*/ "dad",
			"daughter", "day", "days", "december", /*"diff", "difficult", "difficulty",*/ "doctor", "down", "dr", "due", "during", "dx",
			"each", "earlier", "eg", "either", "else", "elsewhere", "em", "ems", "enough", "episode", "er", "etc", "even", "ever", "every",
			"everyone", "everything", "everywhere", "except", "father", "february", "female", "few", "first", "for", "fordays", "former",
			"formerly", "friday", "friend", "from", "further", "gave", "gx", "had", "has", "have", "having", "he", "hence", "her", "here",
			"hereafter", "hereby", "herein", "hereupon", "hers", "herself", "him", "himself", "his", "hours", "how", "however", "hrs",
			/*"hx",*/ "i", "ie", "if", /*"in",*/ "inc", "indeed", "into", "is", /*"it",*/ "its", "itself", "january", "july", "june", "just", "known",
			"last", "latter", "latterly", "least", "left", "less", "lft", "like", "ls", "lt", "ltd", "male", "many", "march", "may", "me",
			"medic", "meds", "meanwhile", "middle", "might", "mom", "monday", "month", "months", "more", "moreover", "mos", "most",
			"mostly", "mother", "mths", "much", "must", "my", "myself", "namely", "near", "neither", "never", "nevertheless", "next", "no",
			"nobody", "none", "noone", "nor", /*"not",*/ "nothing", "november", "now", "nowhere", "october", "of", /*"off",*/ "often", "on",
			"once", "one", "only", "onto", "op", "or", "other", "others", "otherwise", "our", "ours", "ourselves", /*"out",*/ "over", "own",
			"patient", "per", "perhaps", "pm", "pos", "poss", "possible", "post", "pn", "pre", "previous", "ps", /*"pt",*/ "pts", "px", /*"rad",*/
			"radiating", "rather", "re", /*"recheck",*/ "ref", "related", "right", "rm", "room", "rt", /*"rx",*/ "s", "same", "seem", "seemed",
			"seeming", "seems", "sent", "september", "several", "severe", "she", "should", "side", "sided", "since", /*"sister",*/ "so",
			"some", "somehow", "someone", "something", "sometime", "sometimes", "somewhere", "sp", "squad", "states", "status", "still",
			"st", "sts", "such", "sx", "sxs", "symptom", "symptoms", /*"t",*/ "than", "that", "the", "their", "them", "themselves", "then",
			"thence", "there", "thereafter", "thereby", "therefor", "therein", "thereupon", "these", "they", "think", "thinks", "this",
			"those", "though", "through", "throughout", "thru", "thursday", "thus", /*"to",*/ "today", "together", "too", "toward", "towards",
			"tuesday", "under", "unknown", "unspecified", "until", "up", "upon", /*"us",*/ "vb", "very", "via", "walkin", "was", "we",
			"wednesday", "week", "weeks", "well", "were", "what", "whatever", "whatsoever", "when", "whence", "whenever", "whensoever",
			"where", "whereafter", "whereas", "whereat", "whereby", "wherefrom", "wherein", "whereinto", "whereof", "whereon", "whereto",
			"whereunto", "whereupon", "wherever", "wherewith", "whether", "which", "whichever", "whichsoever", "while", "whilst",
			"whither", "who", "whoever", "whole", "whom", "whomever", "whomsoever", "whose", "whosoever", "why", "wife", /*"will",*/ "wi",
			"with", "within", "without", "wk", "wks", "would", "work", "xauthor", "xcal", "xdays", "xhours", "xhrs", "xnote", "xother",
			"xsubj", "xweek", "xweeks", "xwk", "xwks", "year", "years", "yes", "yesterday", "yet", "you", "your", "yours", "yourself",
			"yourselves", "yearly", "yrly", "yrs");
	
	public static CharSequence filter(final CharSequence complaint) {
		return filter(complaint, BASIC_STOPWORDS);
	}
	
	public static CharSequence filterAllowNumbers(final CharSequence complaint, final Set<String> stopwords) {
		String ret = "";
		if (complaint != null) {

			// Lowercase, alphabetic only, remove extra spaces..
			final String cleaned = StringUtils.trimToNull(complaint.toString().toLowerCase(Locale.getDefault()).replaceAll("h/a",
					"headache").replaceAll("n/v", "nausea vomiting").replaceAll("[/,]", " ").replaceAll("[^a-z\\s\\d]", " "));

			if (cleaned != null) {

				final StringBuilder buf = new StringBuilder();

				final String[] sp = cleaned.split("\\s");

				for (int i = 0; i < sp.length; i++) {
					if (sp[i] != null && sp[i].length() > 1 && !stopwords.contains(sp[i])) {
						if (buf.length() > 0) {
							buf.append(" ");
						}
						buf.append(sp[i]);
					}
				}
				if (buf.length() > 0) {
					ret = buf.toString();
				}
			}
		}
		return ret;
	}
	
	public static CharSequence filter(final CharSequence complaint, final Set<String> stopwords) {

		String ret = "";
		if (complaint != null) {

			// Lowercase, alphabetic only, remove extra spaces..
			final String cleaned = StringUtils.trimToNull(complaint.toString().toLowerCase(Locale.getDefault()).replaceAll("h/a",
					"headache").replaceAll("n/v", "nausea vomiting").replaceAll("[/,]", " ").replaceAll("[^a-z\\s]", " "));

			if (cleaned != null) {

				final StringBuilder buf = new StringBuilder();

				final String[] sp = cleaned.split("\\s");

				for (int i = 0; i < sp.length; i++) {
					if (sp[i] != null && sp[i].length() > 1 && !stopwords.contains(sp[i])) {
						if (buf.length() > 0) {
							buf.append(" ");
						}
						buf.append(sp[i]);
					}
				}
				if (buf.length() > 0) {
					ret = buf.toString();
				}
			}
		}
		return ret;
	}

	private static Set<String> makeStopSet(String... words) {
		final Set<String> stopSet = new HashSet<String>();
		stopSet.addAll(Arrays.asList(words));
		return Collections.unmodifiableSet(stopSet);
	}

}
