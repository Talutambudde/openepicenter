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
package com.hmsinc.epicenter.integrator.test;

import java.util.List;
import java.util.SortedSet;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.client.ServiceMixClient;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.*;

import ca.uhn.hl7v2.model.v24.message.ADT_A01;
import ca.uhn.hl7v2.model.v24.message.ADT_A03;

import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.health.Admit;
import com.hmsinc.epicenter.model.health.Discharge;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.health.Patient;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.epicenter.model.health.Registration;
import com.hmsinc.epicenter.model.provider.DataConnection;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.hmsinc.mergence.model.DataSource;
import com.hmsinc.mergence.model.HL7Message;
import com.hmsinc.mergence.util.ER7Utils;
import com.hmsinc.mergence.util.HAPIUtils;
import com.hmsinc.mergence.util.HL7Marshaler;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
public class EpiCenterEndpointTest extends SpringTestSupport {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private TransactionTemplate tt;

	private HealthRepository healthRepository;

	private ProviderRepository providerRepository;
	
	private AnalysisRepository analysisRepository;
	
	private Long pid;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.tck.SpringTestSupport#setUp()
	 */
	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();

		tt = new TransactionTemplate((PlatformTransactionManager) getBean("transactionManager"));

		healthRepository = (HealthRepository) getBean("healthRepository");
		providerRepository = (ProviderRepository) getBean("providerRepository");
		analysisRepository = (AnalysisRepository) getBean("analysisRepository");
		
		tt.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {

				// This should auto-populate
				DataType dt = analysisRepository.getDataType("Emergency Department Registrations");
				assertNotNull(dt);

				DataConnection dc = new DataConnection();
				dc.setName("TEST");

				Facility f = new Facility("TEST");
			//	f.getDataTypes().add(dt);

				dc.getFacilities().add(f);

				providerRepository.save(dc);

				assertNotNull(f.getId());

				pid = f.getId();

			}
		});
	}

	@Test
	public void testSendingToStaticEndpoint() throws Exception {
		ServiceMixClient client = new DefaultServiceMixClient(jbi);

		HL7Message hl7 = makeA04Message();
		ADT_A01 m = (ADT_A01) hl7.getMessage();
		logger.debug("** {}", m);
		
		logger.debug("=== Test A04 ===================================");
		
		
		// Send three of the same, only one should actually be stored.
		logger.debug("Sending: {}", ER7Utils.toER7String(m));

		for (int i = 0; i < 3; i++) {

			sendMessage(client, hl7);

		}
		
		assertEquals(1L, getCount(Registration.class));
		
		logger.debug("=== Test A04 MVA ===================================");

		/*
		// Restart the component to cause re-validation of classifiers..
		jbi.getComponent("epicenter").stop();
		jbi.getComponent("epicenter").start();
		assertNotNull(jbi.getComponent("epicenter"));

		logger.debug("======================================");
		*/
		
		// Classify another Registration to make sure everything still works...
		// DON'T set the admitDateTime so we can test the comparator!
		m.getPV1().getVisitNumber().getID().setValue("457");
		m.getPV2().getAdmitReason().getText().setValue("MVA");
		hl7.setMessage(m);
		//m.getPV1().getAdmitDateTime().getTimeOfAnEvent().setValue("20060606122000");

		sendMessage(client, hl7);
		assertEquals(2L, getCount(Registration.class));
		
		logger.debug("=== Test A04 Merge ===================================");

		// Try a merge..
		m.getPID().getPatientAddress(0).getZipOrPostalCode().setValue("12346-0404");
		m.getPV1().getVisitNumber().getID().setValue("458");
		m.getPV1().getPatientClass().setValue("O");
		m.getPV1().getAdmitDateTime().getTimeOfAnEvent().setValue("20060623042000");
		m.getPV2().getAdmitReason().getText().setValue("SMOKING CRACK ALL DAY");
		hl7.setMessage(m);
		
		sendMessage(client, hl7);

		assertEquals(3L, getCount(Registration.class));
		
		logger.debug("=== Test A01 ===================================");

		// Admit
		sendMessage(client, makeA01Message());

		assertEquals(1L, getCount(Admit.class));
		
		logger.debug("=== Test A03 ===================================");
		
		// Discharge
		sendMessage(client, makeA03Message());

		assertEquals(1L, getCount(Discharge.class));
		
		logger.debug("======================================");
		
		assertCorrectResults();

	}

	private long getCount(final Class<? extends Interaction> type) {
		Object ret = tt.execute(new TransactionCallback() {
			
			public Object doInTransaction(TransactionStatus status) {
				return healthRepository.count(type);
			}
		});
		return ((Long)ret).longValue();
	}
	
	private void sendMessage(final ServiceMixClient client, final HL7Message hl7) throws Exception {

		logger.debug("Sending: {}", ER7Utils.toER7String(hl7.getMessage()));

		InOnly me = client.createInOnlyExchange();
		me.setService(new QName("urn:test", "epicenter"));
		NormalizedMessage message = me.getInMessage();

		HL7Marshaler.marshal(me, message, hl7);

		client.sendSync(me);
		assertExchangeWorked(me);
	}

	private HL7Message makeA04Message() throws Exception {

		HL7Message hl7 = HL7Marshaler.hapiToMessage(HAPIUtils.createMessage("ADT_A04", "2.4"));
		hl7.setId(1L);

		DataSource ds = new DataSource();
		ds.setId(pid);
		ds.setSendingFacility("TEST");
		hl7.setDataSource(ds);

		ADT_A01 m = (ADT_A01) hl7.getMessage();
		m.getMSH().getSendingFacility().getNamespaceID().setValue("TEST");
		m.getPID().getAdministrativeSex().setValue("F");
		m.getPID().getDateTimeOfBirth().getTimeOfAnEvent().setValue("19741117");
		m.getPID().getPatientAddress(0).getZipOrPostalCode().setValue("12345-0404");
		m.getPID().getPatientIdentifierList(0).getID().setValue("321");
		m.getPV1().getAdmitDateTime().getTimeOfAnEvent().setValue("20060606042000");
		m.getPV1().getPatientClass().setValue("E");
		m.getPV1().getVisitNumber().getID().setValue("456");
		m.getPV2().getAdmitReason().getText().setValue("CAR CRASH");
		m.getDG1().getDiagnosisCodeDG1().getIdentifier().setValue("123");
		m.getDG1().getDiagnosisDescription().setValue("CAR CRASH");

		return hl7;
	}

	private HL7Message makeA01Message() throws Exception {

		HL7Message hl7 = HL7Marshaler.hapiToMessage(HAPIUtils.createMessage("ADT_A01", "2.4"));
		hl7.setId(1L);

		DataSource ds = new DataSource();
		ds.setId(pid);
		ds.setSendingFacility("TEST");
		hl7.setDataSource(ds);

		ADT_A01 m = (ADT_A01) hl7.getMessage();
		m.getMSH().getSendingFacility().getNamespaceID().setValue("TEST");
		m.getPID().getAdministrativeSex().setValue("F");
		m.getPID().getDateTimeOfBirth().getTimeOfAnEvent().setValue("19741117");
		m.getPID().getPatientAddress(0).getZipOrPostalCode().setValue("12346-0404");
		m.getPID().getPatientIdentifierList(0).getID().setValue("321");
		m.getPV1().getAdmitDateTime().getTimeOfAnEvent().setValue("20060607042000");
		m.getPV1().getPatientClass().setValue("E");
		m.getPV1().getVisitNumber().getID().setValue("456");
		m.getPV2().getAdmitReason().getText().setValue("CAR CRASH");
		m.getDG1().getDiagnosisCodeDG1().getIdentifier().setValue("666");
		m.getDG1().getDiagnosisDescription().setValue("MVA");

		return hl7;
	}

	private HL7Message makeA03Message() throws Exception {

		HL7Message hl7 = HL7Marshaler.hapiToMessage(HAPIUtils.createMessage("ADT_A03", "2.4"));
		hl7.setId(1L);

		DataSource ds = new DataSource();
		ds.setId(pid);
		ds.setSendingFacility("TEST");
		hl7.setDataSource(ds);

		ADT_A03 m = (ADT_A03) hl7.getMessage();
		m.getMSH().getSendingFacility().getNamespaceID().setValue("TEST");
		m.getPID().getAdministrativeSex().setValue("F");
		m.getPID().getDateTimeOfBirth().getTimeOfAnEvent().setValue("19741117");
		m.getPID().getPatientAddress(0).getZipOrPostalCode().setValue("12346-0404");
		m.getPID().getPatientIdentifierList(0).getID().setValue("321");
		m.getPV1().getDischargeDateTime(0).getTimeOfAnEvent().setValue("20060608042000");
		m.getPV1().getPatientClass().setValue("E");
		m.getPV1().getVisitNumber().getID().setValue("456");
		m.getPV2().getAdmitReason().getText().setValue("CAR CRASH");
		m.getDG1().getDiagnosisCodeDG1().getIdentifier().setValue("123");
		m.getDG1().getDiagnosisDescription().setValue("CAR CRASH");

		m.getPV1().getDischargeDisposition().setValue("10");

		return hl7;

	}

	public void assertCorrectResults() {
		tt.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {

				// We should have 1 patient
				List<Patient> patients = healthRepository.getList(Patient.class);
				assertEquals(1, patients.size());

				Patient p = patients.get(0);
				assertEquals("321", p.getPatientId());

				// With 2 detail records
				SortedSet<PatientDetail> details = p.getPatientDetails();
				assertEquals(2, details.size());

				// And five health interactions
				SortedSet<Interaction> interactions = p.getInteractions();
				assertEquals(5, interactions.size());

				// First one
				PatientDetail d = details.first();
				assertEquals("F", d.getGender().getAbbreviation());
				assertEquals("12345", d.getZipcode());

				// 2 interactions with this PD
				assertEquals(2, d.getInteractions().size());

				// She moves to a new zipcode..
				PatientDetail d2 = details.last();
				assertEquals("12346", d2.getZipcode());

				// 3 interactions
				assertEquals(3, d2.getInteractions().size());

				// Third is outpatient
				int i = 0;
				for (final Interaction he : interactions) {

					assertNotNull(he);
					logger.info("[" + i + "]: " + he.toString());

					if (i == 0) {

						// First one is an ED registration
						assertEquals(he, d.getInteractions().first());

						assertTrue(he instanceof Registration);
						assertNotNull(he.getPatientClass());
						assertEquals("E", he.getPatientClass().getAbbreviation());
						
						Registration ed = (Registration) he;
						assertNotNull(ed.getAgeAtInteraction());
						/*
						assertEquals(Calendar.getInstance().get(Calendar.YEAR) - 1974, ed.getAgeAtInteraction()
								.intValue());
							*/
						assertNotNull(ed.getAgeGroup());
						assertEquals("Adult", ed.getAgeGroup().getName());
						assertEquals("123", ed.getIcd9());
						assertEquals("CAR CRASH", ed.getReason());
						assertEquals("456", ed.getVisitNumber());

						// Check the classification
						assertTrue(ed.getClassifications().size() > 0);
						// assertEquals("Pass",
						// ed.getClassifications().iterator().next().getCategory());

					} else if (i == 1) {

						assertTrue(he instanceof Registration);
						final Registration reg = (Registration) he;
						assertEquals("MVA", reg.getReason());

					} else if (i == 2) {

						// Third is an Admit
						assertTrue(he instanceof Admit);

						final Admit admit = (Admit) he;
						assertEquals("MVA", admit.getReason());
						assertEquals("666", admit.getIcd9());

						// No classifications (yet)
						assertEquals(0, he.getClassifications().size());

					} else if (i == 3) {

						// Fourth is the discharge
						assertTrue(he instanceof Discharge);

						// Should have 1 classification (disposition)
						assertEquals(1, he.getClassifications().size());

					} else if (i == 4) {

						// Fifth is an Outpatient
						// This one was sent third, but the date is later than
						// the rest.
						assertTrue(he instanceof Registration);
						assertNotNull(he.getPatientClass());
						assertEquals("O", he.getPatientClass().getAbbreviation());
						Registration o = (Registration) he;
						assertEquals("SMOKING CRACK ALL DAY", o.getReason());

						// Should have no classifications
						assertEquals(0, o.getClassifications().size());

					} else {
						fail();
					}

					i++;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.tck.SpringTestSupport#createBeanFactory()
	 */
	@Override
	protected AbstractXmlApplicationContext createBeanFactory() {
		return new ClassPathXmlApplicationContext("epicenter-integrator-test-context.xml");
	}

}
