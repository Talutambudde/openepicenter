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
package com.hmsinc.epicenter.webapp.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 *
 */
public class RemotingTest extends AbstractJpaTests {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	static {
		try {
			if (SimpleNamingContextBuilder.getCurrentContextBuilder() == null) {
				final SimpleNamingContextBuilder b = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
				b.bind("java:comp/env/surveillanceEnabled", false);
			}
		} catch (Exception e) {
			// Ignore
		}
	}
	
	public RemotingTest() throws Exception {

		setAutowireMode(AbstractDependencyInjectionSpringContextTests.AUTOWIRE_BY_NAME);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.test.AbstractSingleSpringContextTests#getConfigLocations()
	 */
	@Override
	protected String[] getConfigLocations() {
		return new String[] { "classpath:test-webapp-beans.xml" };
	}

	
	public void testStuff() throws Throwable {
		
	}
}
