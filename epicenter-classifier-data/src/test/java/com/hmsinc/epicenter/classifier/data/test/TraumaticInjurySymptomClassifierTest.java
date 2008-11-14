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
public class TraumaticInjurySymptomClassifierTest extends TestCase {

	private static final String XML_CONFIG = "classpath:TraumaticInjuryClassifier.xml";
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void testSymptomClassifier() throws Throwable {
		
		final ClassificationEngine c = ClassifierFactory.createClassifier(XML_CONFIG);
		assertNotNull(c);
		
		List<String> cls;

		cls = c.classify("MVA-HEAD INJURY");
		assertTrue(cls.contains("MVA") && cls.contains("Injury") && cls.size() == 2);
		logger.info("Classified: MVA-HEAD INJURY as " + cls.toString());
		cls = c.classify("HEROIN WITHDRAWAL/LAST USE 4PM");
		assertTrue(cls.contains("Drugs") && cls.size() == 1);
		logger.info("Classified: HEROIN WITHDRAWAL/LAST USE 4PM as " + cls.toString());
		cls = c.classify("ARM LAC");
		assertTrue(cls.contains("Injury") && cls.size() == 1);
		logger.info("Classified: ARM LAC as " + cls.toString());
		cls = c.classify("^SHOT IN LEG WITH BB GUN");
		assertTrue(cls.contains("Gunshot") && cls.size() == 1);
		logger.info("Classified: ^SHOT IN LEG WITH BB GUN as " + cls.toString());
		cls = c.classify("HEAD INJ S/P HORSE KICK");
		assertTrue(cls.contains("Violence") && cls.size() == 1);
		logger.info("Classified: HEAD INJ S/P HORSE KICK as " + cls.toString());
		cls = c.classify("SUTURE COMING OPEN");
		assertTrue(cls.contains("Suture/Wound Check") && cls.size() == 1);
		logger.info("Classified: SUTURE COMING OPEN as " + cls.toString());
		
	}
}
