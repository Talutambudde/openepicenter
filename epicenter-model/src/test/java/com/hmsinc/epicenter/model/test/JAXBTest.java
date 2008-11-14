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

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.attribute.Attributes;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResult;
import com.hmsinc.epicenter.model.surveillance.SurveillanceResultType;
import com.hmsinc.ts4j.TimeSeries;
import com.hmsinc.ts4j.TimeSeriesPeriod;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:JAXBTest.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
public class JAXBTest extends TestCase {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private JAXBContext jc = JAXBContext.newInstance("com.hmsinc.epicenter.model");

	private static String CONFIG = "epicenter-attributes.xml";

	public JAXBTest() throws JAXBException {
		super();
	}

	public JAXBTest(String arg0) throws JAXBException {
		super(arg0);
	}

	public void testUnmarshal() throws Throwable {

		Unmarshaller u = jc.createUnmarshaller();
		Attributes wa = (Attributes) u.unmarshal(getClass().getResourceAsStream("/" + CONFIG));

		assertEquals(7, wa.getAgeGroups().size());

		assertEquals(3, wa.getGenders().size());

		assertEquals(3, wa.getPatientClasses().size());

	}
	
	public void testSurveillanceResult() throws Throwable {
		
		Unmarshaller u = jc.createUnmarshaller();
		Marshaller m = jc.createMarshaller();
		m.setProperty("jaxb.formatted.output", true);
		
		final DateTime end = new DateTime();
		final DateTime start = end.minusDays(30);
		
		final TimeSeries ts = new TimeSeries(TimeSeriesPeriod.DAY, start, end);
		//m.marshal(ts, System.out);
		
		final SurveillanceResult result = new SurveillanceResult();
		result.getResults().put(SurveillanceResultType.ACTUAL, ts);
		
		final StringWriter writer = new StringWriter();
		m.marshal(result, writer);
		
		final String xml = writer.toString();
		assertNotNull(xml);
		logger.debug(xml);
		
		final StringReader reader = new StringReader(xml);
		final Object obj = u.unmarshal(reader);
		 
		assertEquals(result.getResults().get(SurveillanceResultType.ACTUAL), ((SurveillanceResult)obj).getResults().get(SurveillanceResultType.ACTUAL));
		
	}
}
