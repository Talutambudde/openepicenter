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
package com.hmsinc.epicenter.model.test;

import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.springframework.test.annotation.ExpectedException;
import org.springframework.test.annotation.NotTransactional;

import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Attributes;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.attribute.PatientClass;
import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.StateFeature;
import com.hmsinc.epicenter.model.geography.ZipcodeContainer;
import com.hmsinc.epicenter.model.health.Patient;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.epicenter.model.health.Registration;
import com.hmsinc.epicenter.model.permission.EpiCenterRole;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.permission.PermissionExceptionType;
import com.hmsinc.epicenter.model.provider.DataConnection;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.util.InvalidZipcodeException;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.epicenter.model.workflow.Workflow;
import com.hmsinc.epicenter.model.workflow.WorkflowState;
import com.hmsinc.epicenter.model.workflow.WorkflowTransition;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WarehouseModelTest.java 220 2007-07-17 14:59:08Z steve.kondik $
 * 
 */
public class ModelTest extends AbstractModelTest {

	private static String CONFIG = "/epicenter-attributes.xml";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:test-model-beans.xml" };
	}

	public void test00PopulateWithJAXB() throws Throwable {

		Attributes wa = (Attributes) jaxbContext.createUnmarshaller().unmarshal(getClass().getResourceAsStream(CONFIG));

		assertEquals(wa.getGenders().size(), attributeRepository.count(Gender.class));
		assertEquals(wa.getAgeGroups().size(), attributeRepository.count(AgeGroup.class));
		assertEquals(wa.getPatientClasses().size(), attributeRepository.count(PatientClass.class));

		// Re-run just to be sure equals/hashcode works..
		upgradeTasks.init();

		// Test the default workflow
		assertTrue(workflowRepository.count(Workflow.class) > 0);
		
		final Workflow workflow = workflowRepository.getList(Workflow.class).get(0);
		assertNotNull(workflow);
		
		logger.debug(workflow.toString());
		
		assertTrue(workflow.getStates().size() > 0);
		
		for (WorkflowState w : workflow.getStates()) {
			logger.debug(w.toString());
			for (WorkflowTransition t : w.getTransitionsTo()) {
				logger.debug(t.toString());
			}
		}
		
		final Workflow defaultWorkflow = workflowRepository.getDefaultWorkflow();
		assertNotNull(defaultWorkflow);
		final WorkflowState initialState = workflowRepository.getInitalState(defaultWorkflow);
		assertNotNull(initialState);
		logger.debug(initialState.toString());
		
		setComplete();

	}

	public void test01Providers() throws Throwable {

		DataConnection dc = new DataConnection("TEST");

		Facility facility = new Facility("TEST");
		dc.getFacilities().add(facility);

		providerRepository.save(dc);

		assertEquals(1, providerRepository.count(DataConnection.class));
		assertEquals(1, providerRepository.count(Facility.class));

		setComplete();
	}

	public void test02Users() throws Throwable {
		
		logger.debug("--------------------------------------");
		EpiCenterUser u = new EpiCenterUser("testuser", "testpassword", "test@test.com");
		EpiCenterRole r = new EpiCenterRole("testrole", "Test Role");
		u.getPreferences().put("TEST", "0");
		u.getRoles().add(r);

		permissionRepository.addUser(u);

		EpiCenterUser u1 = permissionRepository.authenticateUser("testuser", "testpassword");
		assertNotNull(u1);
		assertNotSame("testpassword", u1.getPassword());
		assertEquals(2, u.getRoles().size());
		assertEquals(1, u.getPreferences().size());
		
		logger.debug("Encrypted password: " + u1.getPassword());

		setComplete();
		logger.debug("--------------------------------------");

	}

	@ExpectedException(value = PermissionException.class)
	public void test021FailedAuthentication() throws Throwable {
		permissionRepository.authenticateUser("testuser", "badpassword");
	}

	public void test022ChangePassword() throws Throwable {

		EpiCenterUser u1 = permissionRepository.authenticateUser("testuser", "testpassword");
		assertNotNull(u1);
		permissionRepository.changePassword(u1, "blehbleh");

		try {
			permissionRepository.authenticateUser("testuser", "testpassword");
			fail("Password should have been changed!");
		} catch (PermissionException e) {
			assertEquals(PermissionExceptionType.AUTHENTICATION_FAILED, e.getType());
		}

		EpiCenterUser u2 = permissionRepository.authenticateUser("testuser", "blehbleh");
		assertNotNull(u2);
		assertEquals(u1.getId(), u2.getId());

		setComplete();
	}

	@NotTransactional
	@ExpectedException(value = InvalidZipcodeException.class)
	public void test04InvalidZipCode() throws Throwable {
		ModelUtils.validateZipcode("abcde");
	}

	public void test05Patients() throws Throwable {

		Facility f = providerRepository.getFacilityByIdentifier("TEST");
		assertNotNull(f);

		Patient p = new Patient("123", f);
		PatientDetail pd = new PatientDetail(p);

		pd.setDateOfBirth(new DateMidnight(1974, 11, 17).toDateTime());

		pd.setGender(attributeRepository.getGenderByAbbreviation("F"));
		assertNotNull(pd.getGender());
		assertEquals("F", pd.getGender().getAbbreviation());

		pd.setEmployerZipcode("12345");

		pd.setZipcode("54321");

		p.getPatientDetails().add(pd);

		healthRepository.save(p);

		assertEquals(1, healthRepository.count(Patient.class));
		assertEquals(1, healthRepository.count(PatientDetail.class));

		assertEquals(1, providerRepository.getFacilityByIdentifier("TEST").getPatients().size());

		assertEquals(1, p.getPatientDetails().size());

		setComplete();
	}

	public void test06Admits() throws Throwable {

		PatientClass ed = attributeRepository.getPatientClassByAbbreviation("E");
		
		Registration eda = new Registration(ed);

		Facility f = providerRepository.getFacilityByIdentifier("TEST");

		Patient p = healthRepository.getPatient("123", f);
		assertNotNull(p);
		assertNotNull(p.getPatientDetails().last());

		eda.setPatient(p);
		eda.setPatientDetail(p.getPatientDetails().last());

		final DateTime interactionDate = new DateMidnight(2006, 6, 6).toDateTime();
		eda.setInteractionDate(interactionDate);

		int age = eda.getAgeAtInteraction().intValue();
		assertEquals(31, age);

		eda.setReason("I WILL PASS");
		eda.setMessageId(1L);
		eda.setAgeGroup(attributeRepository.getAgeGroupForAge(age));

		assertTrue(eda.getAgeGroup().isAgeGroupForAge(age));
		assertEquals("Adult", eda.getAgeGroup().getName());

		eda.setVisitNumber("100");
		p.getInteractions().add(eda);

		healthRepository.save(p);

		assertEquals(1, healthRepository.count(Registration.class));

		setComplete();
	}

	public void test08SpatialTypes() throws Throwable {
		
		assertTrue(StateFeature.class.isAssignableFrom(County.class));
		assertTrue(ZipcodeContainer.class.isAssignableFrom(State.class));
	}
	
	public void test09Metadata() throws Throwable {
		assertEquals("PATIENT", healthRepository.getTableForEntity(Patient.class));
	}
	
	public void test10SetupOrganizationSponsors() throws Throwable {
		
		final Workflow w = workflowRepository.getDefaultWorkflow();
		assertNotNull(w);
		
		final Organization parent = new Organization("parent", "parent", w);
		final Organization org1 = new Organization("test1", "test1", w);
		final Organization org2 = new Organization("test2", "test2", w);
		
		parent.getSponsoredOrganizations().add(org1);
		parent.getSponsoredOrganizations().add(org2);
		
		org1.getSponsors().add(parent);
		org2.getSponsors().add(parent);
		
		permissionRepository.save(parent);

		setComplete();
	}
	
	public void test11OrganizationTree() throws Throwable {
		
		final List<Organization> pl = permissionRepository.findBy(Organization.class, "name", "parent");
		assertNotNull(pl);
		assertEquals(1, pl.size());
		
		final Organization parent = pl.get(0);
		assertEquals(2, parent.getSponsoredOrganizations().size());
		
		final List<Organization> tree = permissionRepository.getSponsorTree(parent);
		assertNotNull(tree);
		
		assertEquals(3, tree.size());
		
		assertTrue(tree.contains(parent));
		assertTrue(tree.containsAll(parent.getSponsoredOrganizations()));
		
	}
}
