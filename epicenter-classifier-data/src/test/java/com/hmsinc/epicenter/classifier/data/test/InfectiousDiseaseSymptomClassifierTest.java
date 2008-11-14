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
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 *
 */
public class InfectiousDiseaseSymptomClassifierTest extends TestCase {

	private static final String XML_CONFIG = "classpath:InfectiousDiseaseSymptomClassifier.xml";
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void testSymptomClassifier() throws Throwable {
		
		final ClassificationEngine c = ClassifierFactory.createClassifier(XML_CONFIG);
		assertNotNull(c);
		
		List<String> cls = c.classify("BROKEN NECK VAGINAL BLEED ABDOMINAL GUTS HEAD EXPLODED LIGHTBULB");
		logger.info(cls.toString());
		assertTrue(cls.size() > 0);		

		cls = c.classify("DIARRHEA FEVER SEIZURES");
		logger.info("Classified: DIARRHEA FEVER SEIZURES as " + cls.toString());
		assertTrue(cls.contains("Diarrhea") && cls.contains("Fever") && cls.contains("Neuro") && cls.size() == 3);		
		
		cls = c.classify("RECHECK SWOLLEN AREA");
		logger.info("Classified: RECHECK SWOLLEN AREA as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("VOMITING, DIFF BREATHING");
		logger.info("Classified: VOMITING, DIFF BREATHING as " + cls.toString());
		assertTrue(cls.contains("Vomiting") && cls.contains("Respiratory") && cls.size() == 2);
		
		cls = c.classify("FEVER, FEBRILE SEIZURE");
		logger.info("Classified: FEVER, FEBRILE SEIZURE as " + cls.toString());
		assertTrue(cls.contains("Fever") && cls.size() == 1);
		
		cls = c.classify("SOB; COUGH; BODY ACHES");
		logger.info("Classified: SOB; COUGH; BODY ACHES as " + cls.toString());
		assertTrue(cls.contains("Respiratory") && cls.contains("Cough") && cls.contains("Myalgia") &&  cls.size() == 3);
		
		cls = c.classify("HEADACHE, SORE THROAT, WEAK, SINUS PROBLEMS");
		logger.info("Classified: HEADACHE, SORE THROAT, WEAK, SINUS PROBLEMS as " + cls.toString());
		assertTrue(cls.contains("Headache") && cls.contains("ENT") && cls.contains("Fatigue") && cls.size() == 3);
		
		cls = c.classify("CRYING COUGH");
		logger.info("Classified: CRYING COUGH as " + cls.toString());
		assertTrue(cls.contains("Malaise") && cls.contains("Cough") && cls.size() == 2);
		
		
		// Tests added 4/30 in Epicenter 2.1.1 update
		cls = c.classify("ABSCESS DENTAL");
		logger.info("Classified: ABSCESS DENTAL as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("RIGHT HAND PAIN X6 HRS 2ND TO BEING HIT WITH SOCCOR BALL");
		logger.info("Classified: RIGHT HAND PAIN X6 HRS 2ND TO BEING HIT WITH SOCCOR BALL as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("LACERATION TO HEAD 2ND TO BEING HIT BY A ROCK");
		logger.info("Classified: LACERATION TO HEAD 2ND TO BEING HIT BY A ROCK as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("BACK PAIN RIGHT MIDDLE LOWER BACK NECK AND HA 2ND TO MVC");
		logger.info("Classified: BACK PAIN RIGHT MIDDLE LOWER BACK NECK AND HA 2ND TO MVC as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("BACK PAIN 2ND TO MVC");
		logger.info("Classified: BACK PAIN 2ND TO MVC as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("INFECTION RIGHT HAND 2ND FINGER SPREAD TO HIS EYES");
		logger.info("Classified: INFECTION RIGHT HAND 2ND FINGER SPREAD TO HIS EYES as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("RIGHT HAND LACERATION 2ND TO SKATEBOARDING 4/25/08 NOT GETT");
		logger.info("Classified: RIGHT HAND LACERATION 2ND TO SKATEBOARDING 4/25/08 NOT GETT as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("LOWER BACK PAIN 2ND TO WRESTLING WITH SON 4/26/08 BACK SU");
		logger.info("Classified: LOWER BACK PAIN 2ND TO WRESTLING WITH SON 4/26/08 BACK SU as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("FB LEFT EYE");
		logger.info("Classified: FB LEFT EYE as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("FB LEFT EYE AT WORK");
		logger.info("Classified: FB LEFT EYE AT WORK as " + cls.toString());
		assertTrue(cls.isEmpty());
		
		cls = c.classify("RIGHT WRIST PAIN 2ND TO LIFTING ROCK AND FEELING STABBING P");
		logger.info("Classified: RIGHT WRIST PAIN 2ND TO LIFTING ROCK AND FEELING STABBING P as " + cls.toString());
		assertTrue(cls.contains("Arthralgia") && cls.size() == 1);
	}
}
