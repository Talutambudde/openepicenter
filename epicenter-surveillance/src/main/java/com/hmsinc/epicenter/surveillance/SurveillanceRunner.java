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
package com.hmsinc.epicenter.surveillance;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author shade
 * @version $Id: SurveillanceRunner.java 1812 2008-07-07 14:47:36Z steve.kondik $
 */
public class SurveillanceRunner {

	private static final String CONTEXT_FILE_SA = "epicenter-surveillance-standalone.xml";

	private static final String CONTEXT_FILE_SCHED = "epicenter-surveillance-scheduled.xml";

	private static final String CONTEXT_FILE_ONEOFF = "epicenter-surveillance-oneoff.xml";

	private static AbstractApplicationContext ac;

	public static void main(String[] args) throws Exception {

		final boolean runOnce = Boolean.parseBoolean(System.getProperty("runOnce"));
		final String context = runOnce ? CONTEXT_FILE_ONEOFF : CONTEXT_FILE_SCHED;

		ac = new ClassPathXmlApplicationContext(new String[] { CONTEXT_FILE_SA, context });
		ac.registerShutdownHook();

		if (!runOnce) {
			// because Classworlds Launcher is not very polite and will kill the
			// app when main thread finishes, we have to make main thread hung
			// indefinitely... ugly but effective
			try {
				while (true) {
					Thread.sleep(10000);
				}
			} catch (InterruptedException e) {

				// we are done now, exiting...
			}
		}
	}

}
