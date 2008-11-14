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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.classifier.ClassificationEngine;
import com.hmsinc.epicenter.classifier.ClassifierFactory;

import junit.framework.TestCase;

/**
 * @author shade
 * @version $Id: InfluenzaRelatedIllnessClassifierTest.java 1803 2008-07-02 19:12:42Z steve.kondik $
 */
public class InfluenzaRelatedIllnessClassifierTest extends TestCase {

	private static final String XML_CONFIG = "classpath:InfluenzaRelatedIllnessClassifier.xml";
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void testILIClassifier() throws Throwable {
		
		final ClassificationEngine c = ClassifierFactory.createClassifier(XML_CONFIG);
		assertNotNull(c);
	}
}
