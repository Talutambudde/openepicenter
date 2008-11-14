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
package com.hmsinc.epicenter.classifier.test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.hmsinc.epicenter.classifier.ClassificationEngine;
import com.hmsinc.epicenter.classifier.ClassifierFactory;
import com.hmsinc.epicenter.classifier.NamedItemClassificationEngine;
import com.hmsinc.epicenter.classifier.lm.BestCategoryClassifier;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
public class ClassifierTest extends TestCase {

	public void testSimpleClassifier() throws Throwable {

		final ClassificationEngine engine = ClassifierFactory.createClassifier("classpath:TestClassifier.xml");
		assertNotNull(engine);
		assertTrue(engine instanceof BestCategoryClassifier);
		assertEquals("TestClassifier", engine.getName());
		assertEquals(2, engine.getCategories().size());

		final List<String> categories = engine.classify("I MUST PASS");
		assertTrue(categories.contains("Pass"));

	}
	
	public void testRegexClassifier() throws Throwable {
		
		final ClassificationEngine engine = ClassifierFactory.createClassifier("classpath:TestRegexClassifier.xml");
		assertNotNull(engine);
		assertTrue(engine instanceof NamedItemClassificationEngine);
		final NamedItemClassificationEngine nc = (NamedItemClassificationEngine)engine;
		
		final String complaint = "I MUST PASS DUDE!!!!#$!@";
		
		
		List<String> result = nc.classify(complaint);
		assertTrue(result.contains("Pass"));
		
		final Map<String, Object> attrs = new LinkedHashMap<String, Object>();
		attrs.put("reason", complaint);
		
		attrs.put("age", 12);
		result = nc.classifyNamedItems(attrs);
		assertTrue(result.size() == 0);
		
		attrs.put("age", 15);
		result = nc.classifyNamedItems(attrs);
		assertTrue(result.contains("Pass"));
		
		attrs.put("reason", "OoPs " + complaint);
		result = nc.classifyNamedItems(attrs);
		assertTrue(result.size() == 0);
		
		attrs.put("name", "bob");
		result = nc.classifyNamedItems(attrs);
		assertTrue(result.size() == 0);
		
		attrs.put("name", "fred");
		result = nc.classifyNamedItems(attrs);
		assertTrue(result.contains("Pass"));
		
	}
}
