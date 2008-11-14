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
package com.hmsinc.epicenter.classifier.data.test;

import java.util.List;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.classifier.ClassificationEngine;
import com.hmsinc.epicenter.classifier.ClassifierFactory;

/**
 * @author C. A. Cois
 *
 */
public class AnimalRelatedInjurySymptomClassifierTest extends TestCase {

	private static final String XML_CONFIG = "classpath:AnimalRelatedInjuryClassifier.xml";
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void testSymptomClassifier() throws Throwable {
		
		final ClassificationEngine c = ClassifierFactory.createClassifier(XML_CONFIG);
		assertNotNull(c);
		
		List<String> cls;

		// Temporarily removed due to the need for an independent stopword set for regex classifiers.
		
		cls = c.classify("MAULED BY BEAR"); 
		assertTrue(cls.contains("Animal Attack") && cls.contains("Animal Scratch") && cls.size() == 2);
		logger.info("Classified: MAULED BY BEAR as " + cls.toString());
		cls = c.classify("KICKED BY HORSE");
		assertTrue(cls.contains("Misc. Animal Injury") && cls.size() == 1);
		logger.info("Classified: KICKED BY HORSE as " + cls.toString());
		cls = c.classify("MONKEY BITE");
		assertTrue(cls.contains("Animal Bite") && cls.size() == 1);
		logger.info("Classified: MONKEY BITE as " + cls.toString());
		cls = c.classify("DOG BITE");
		assertTrue(cls.contains("Animal Bite") && cls.size() == 1);
		logger.info("Classified: DOG BITE as " + cls.toString());
		cls = c.classify("CONTACT WITH RABID RACCOON");
		assertTrue(cls.contains("Rabies") && cls.size() == 1);
		logger.info("Classified: CONTACT WITH RABID RACCOON as " + cls.toString());
	}
}
