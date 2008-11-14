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
package com.hmsinc.epicenter.classifier.test;

import junit.framework.TestCase;

import com.hmsinc.epicenter.classifier.util.ClassifierUtils;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ClassifierUtilsTest.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
public class ClassifierUtilsTest extends TestCase {

	public void testFiltering() throws Exception {
		assertEquals("anxiety", ClassifierUtils.filter("0^anxiety"));
		assertEquals("rash", ClassifierUtils.filter("RASH  FOR 3 MONTHS"));
		assertEquals("testing", ClassifierUtils.filter("t   tEsTiNg 3211432&&#@4^@ "));
		assertEquals("headache", ClassifierUtils.filter("H/A"));
		assertEquals("", ClassifierUtils.filter("N..V"));
	}
}
