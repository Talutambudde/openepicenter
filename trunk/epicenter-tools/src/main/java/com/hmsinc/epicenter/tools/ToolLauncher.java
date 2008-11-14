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
package com.hmsinc.epicenter.tools;

import org.apache.commons.lang.Validate;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Simple launcher for EpiCenter CLI tools.
 * 
 * @author shade
 * @version $Id: ToolLauncher.java 1496 2008-04-08 18:39:10Z steve.kondik $
 */
public class ToolLauncher {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			final String app = args[0];
			final ConfigurableApplicationContext appContext = new ClassPathXmlApplicationContext("classpath:" + app + "-util.xml");
			final RunnableTool tool = (RunnableTool) appContext.getBean(app);
			Validate.notNull(tool, "Could not start tool: " + app);
			tool.setArguments(args);
			tool.run();
		}
	}

}
