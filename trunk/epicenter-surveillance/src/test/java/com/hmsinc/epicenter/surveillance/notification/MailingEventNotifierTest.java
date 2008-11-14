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
package com.hmsinc.epicenter.surveillance.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceMethod;
import com.hmsinc.epicenter.model.surveillance.SurveillanceSet;
import com.hmsinc.epicenter.model.surveillance.SurveillanceTask;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-beans.xml" })
public class MailingEventNotifierTest {

	private static final String EVENT_DESCRIPTION = "event description";
	private static final String USER_EMAIL = "Fictitious User <john.doe@hmsinc.com>";
	private static final String ENCODING = "UTF-8";

	@Resource
	private MailingEventNotifier emailNotifier = null;

	@Resource
	private MockJavaMailSender mailSender;

	private Anomaly anomaly;
	private EpiCenterUser user;
	private List<Message> sentMessages;

	@Before
	@SuppressWarnings("serial")
	public void setUp() {
		assertNotNull(emailNotifier);
		
		Classification classification = new Classification(new Classifier("Infectious Disease Symptom", "1.0"), "Respiratory");
		classification.setCategory("Respiratory");
		anomaly = new Anomaly() {
    		
			@Override
        	public double getObservedValue() { return 16; }
			
			@Override
        	public double getObservedThreshold() { return 12.2229238347294; }
			
			@Override
        	public double getPredictedObservedValue() { return 9.991224987294; }
			
			@Override
        	public double getTotalValue() { return 161; }
			
			@Override
        	public double getNormalizedValue() { return 6.83092384729; }
		};
		
		SurveillanceTask task = new SurveillanceTask();
		task.setLocation(AnalysisLocation.HOME);
		
		SurveillanceMethod method = new SurveillanceMethod();
		method.setName("CuSum");
		
		State pa = new State() {
			@Override
        	public String getDisplayName() { return "Pennsylvania"; }
		};
		
		DataType ed = new DataType("Emergency Department Admissions");
		Gender testGender = new Gender("Male", "M");
		
		SurveillanceSet set = new SurveillanceSet("test", ed);
		set.getAttributes().add(testGender);
		
		anomaly.setId(123L);
		anomaly.setTask(task);
		anomaly.setMethod(method);
		anomaly.setDescription(EVENT_DESCRIPTION);
		anomaly.setClassification(classification);
		anomaly.setAnalysisTimestamp(new DateTime(2008, 1, 28, 14, 22, 0, 0));
		anomaly.setTimestamp(new DateTime(2008, 1, 28, 15, 30, 0, 0));
		anomaly.setGeography(pa);
		anomaly.setSet(set);
		
		user = new EpiCenterUser();
		user.setEnabled(true);
		user.setEmailAddress(USER_EMAIL);
		emailNotifier.notify(anomaly, user);
		sentMessages = mailSender.getMockTransport().getSentMessages();
	}
	
	@Test
	public void shouldReceiveOneMessage() {
		assertEquals(1, sentMessages.size());
	}

	@Test
	public void shouldBeSentToOneProperRecepient() throws MessagingException {
		MimeMessage message = (MimeMessage) sentMessages.get(0);
		Address[] recipients = message.getAllRecipients();
		assertEquals(1, recipients.length);
		assertEquals(USER_EMAIL, recipients[0].toString());
	}
	
	@Test
	public void shouldHaveCorrectSubject() throws MessagingException {
		MimeMessage message = (MimeMessage) sentMessages.get(0);
		assertEquals("Respiratory Anomaly in Pennsylvania", message.getSubject());
	}
	
	@Test
	public void shouldHaveCorrectStructure() throws MessagingException, IOException {
		MimeMessage message = (MimeMessage) sentMessages.get(0);
		
		MimeMultipart mm = (MimeMultipart) message.getContent();
		assertEquals(1, mm.getCount());
		
		MimeBodyPart body0 = (MimeBodyPart) mm.getBodyPart(0);
		assertNotNull(loadContent(body0.getInputStream()));
	}
	
	@Test
	public void shouldHaveCorrectEncoding() throws MessagingException, IOException {
		assertTrue(loadContent(getMessageAsStream()).contains("charset="+ENCODING));
	}
	
	@Test
	public void shouldHaveCorrectTextContent() throws MessagingException, IOException {
		String expected = "Respiratory Anomaly in Pennsylvania";
		
		expected += "Monitoring of emergency department admissions for residents of Pennsylvania";
		expected += "identified 16 interactions classified as respiratory (for patients grouped by";
		expected += "gender as male) by the Infectious Disease Symptom classifier. All interactions";
		expected += "occurred between January 27, 2008 2:22 PM and January 28, 2008 2:22 PM.";
		
		expected += "Using CuSum analysis, these 16 interactions exceed the predicted value of 9.99";
		expected += "and the maximum threshold of 12.22.";
		
		expected += "The time of the anomaly was 2:22 PM.";
		

		expected += "Summary";

		expected += "Time of Anomaly         January 28, 2008 2:22:00 PM EST";
		expected += "Time Detected           January 28, 2008 3:30:00 PM EST";
		expected += "Indication              Emergency Department Admissions";
		expected += "Classifier              Infectious Disease Symptom";
		expected += "Classification(s)       Respiratory";
		expected += "Location                Pennsylvania";
		expected += "Analysis Method         CuSum";
		expected += "Results of Analysis     6.83, (16/161)(normalized)";
		expected += "Records Totaled By      Home Location";

		expected += "Please use the following link to view this anomaly in";
		expected += "EpiCenter: http://dev3.hmsinc.com/epicenter?event=123";
		
		assertTextContentEquals(expected);
	}
	
	@Test
	public void shouldHaveCorrectHtmlContent() throws MessagingException, IOException {
		String[] expectedFragments = new String[] {
		
		"Monitoring of emergency department admissions for residents of Pennsylvania",
		"identified 16 interactions classified as respiratory (for patients grouped by gender as male) by the Infectious Disease Symptom classifier.",
		"All interactions occurred between January 27, 2008 2:22 PM and January 28, 2008 2:22 PM.",
		
		"Using CuSum analysis, these 16 interactions exceed the predicted value of 9.99 and the",
		"maximum threshold of 12.22.",
		
		"The time of the anomaly was 2:22 PM.",
		

		"Summary",

		"January 28, 2008 2:22:00 PM EST",
		"January 28, 2008 3:30:00 PM EST",
		"Emergency Department Admissions",
		"Respiratory",
		"Pennsylvania",
		"CuSum",
		"6.83, (16/161)(normalized)",
		"Home Location",

		"http://dev3.hmsinc.com/epicenter?event=123"
		};
		
		for (String fragment : expectedFragments) {
    		assertHtmlContentContains(fragment);
		}
		
	}
	
	private void assertTextContentEquals(String expected) throws IOException, MessagingException {
		String content = getTextMessageContent();
		
		System.out.println("Expected: " + expected);
		System.out.println("Got: " + content);
		assertEquals(expected, content);
	}
	
	private void assertHtmlContentContains(String expected) throws IOException, MessagingException {
		String content = getHtmlMessageContent();
		assertTrue("Message [" + content + "] should contain [" + expected + "]", content.contains(expected));
	}
	
	private String getTextMessageContent() throws IOException, MessagingException {
		String content = loadContent(getMessageAsStream());
		String prefix = "Content-Type: text/plain; charset=UTF-8Content-Transfer-Encoding: 7bit";
		String suffix = "------=_Part";
		String rtn = StringUtils.substringBetween(content, prefix, suffix);
		return rtn;
	}
	
	private String getHtmlMessageContent() throws IOException, MessagingException {
		String content = loadContent(getMessageAsStream());
		String prefix = "Content-Type: text/html;charset=UTF-8Content-Transfer-Encoding: 7bit";
		String suffix = "------=_Part";
		String rtn = StringUtils.substringBetween(content, prefix, suffix);
		return rtn;
	}
	
	private String loadContent(InputStream is) throws IOException {
		InputStreamReader reader = new InputStreamReader(is);
		StringBuffer rtn = new StringBuffer();
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line;
		while( (line = bufferedReader.readLine()) != null) {
			rtn.append(line);
		}
		return rtn.toString();
	}
	
	private InputStream getMessageAsStream() throws IOException, MessagingException {
		MimeMessage message = (MimeMessage) sentMessages.get(0);
		MimeMultipart mm = (MimeMultipart) message.getContent();
		MimeBodyPart body0 = (MimeBodyPart) mm.getBodyPart(0);
		return body0.getInputStream();
	}
	
	static {
		try {
			final SimpleNamingContextBuilder b = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
			b.bind("java:comp/env/surveillanceEnabled", false);
		} catch (Exception e) {
			throw new RuntimeException("Can not setup test case because of ", e);
		}
	}
}
