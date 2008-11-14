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
package com.hmsinc.epicenter.services.test;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.test.jpa.AbstractJpaTests;

import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.health.Patient;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.epicenter.model.health.Registration;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.hmsinc.epicenter.service.ClassificationService;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
public class ServicesTest extends AbstractJpaTests {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	protected AnalysisRepository analysisRepository;

	@Resource
	protected AttributeRepository attributeRepository;

	@Resource
	protected HealthRepository healthRepository;

	@Resource
	protected ProviderRepository providerRepository;

	@Resource
	protected ClassificationService classificationService;

	public ServicesTest() {
		setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:test-service-beans.xml" };
	}

	public void test00ClassifierPopulation() throws Throwable {

		assertTrue(classificationService.getClassifiers().size() > 0);
		
		final List<Classifier> cls = analysisRepository.getList(Classifier.class);
		assertTrue(cls.size() > 0);

		assertTrue(cls.get(0).getClassifications().size() > 0);
		
		setComplete();
	}

	public void test01TestReinit() throws Throwable {
		
		classificationService.init();
		
		test00ClassifierPopulation();
		
		setComplete();
	}
	
	public void testClassificationService() throws Throwable {

		final Facility f = new Facility();
		f.setIdentifier("TEST");
		providerRepository.save(f);

		final Patient p = new Patient();
		p.setFacility(f);
		p.setPatientId("1234");

		final PatientDetail pd = new PatientDetail();
		pd.setZipcode("12345");
		pd.setGender(attributeRepository.getGenderByAbbreviation("M"));
		pd.setPatient(p);
		p.getPatientDetails().add(pd);

		final Registration r = new Registration();
		r.setAgeAtInteraction(23);
		r.setInteractionDate(new DateTime());
		r.setPatientClass(attributeRepository.getPatientClassByAbbreviation("E"));
		r.setMessageId(1234L);
		r.setVisitNumber("1234");
		r.setReason("OVERDOSE");
		r.setPatient(p);
		r.setPatientDetail(pd);
		r.setAgeGroup(attributeRepository.getAgeGroupForAge(23));
		
		p.getInteractions().add(r);

		final Set<Classification> cls = classificationService.classify(r);
		assertNotNull(cls);

		assertTrue(cls.size() > 0);

		r.getClassifications().addAll(cls);
		healthRepository.save(p);

		setComplete();

	}

	public void testClassificationServiceResults() throws Throwable {

		final List<Registration> rs = healthRepository.getList(Registration.class);
		logger.info(rs.toString());

		assertTrue(rs.size() > 0);

		logger.info(rs.get(0).getClassifications().toString());

		assertTrue(rs.get(0).getClassifications().size() > 0);

	}

}
