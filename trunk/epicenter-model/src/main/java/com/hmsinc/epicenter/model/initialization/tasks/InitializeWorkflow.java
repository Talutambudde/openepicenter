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
package com.hmsinc.epicenter.model.initialization.tasks;

import java.io.IOException;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;

import com.hmsinc.epicenter.model.initialization.InitializationTask;
import com.hmsinc.epicenter.model.workflow.Workflow;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;

/**
 * Creates the default workflow from XML configuration if it doesn't exist.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: InitializeWorkflow.java 533 2007-12-10 03:53:20Z steve.kondik $
 */
public class InitializeWorkflow implements InitializationTask {

	private static final String EPICENTER_WORKFLOW = "classpath:epicenter-workflow.xml";

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Resource
	private WorkflowRepository workflowRepository;

	@Resource
	private JAXBContext jaxbContext;

	@Resource
	private ResourceLoader resourceLoader;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.initialization.InitializationTask#executeTask()
	 */
	public void executeTask() {
		if (workflowRepository.getList(Workflow.class).size() == 0) {
			try {
				final Workflow workflow = (Workflow) jaxbContext.createUnmarshaller().unmarshal(
						resourceLoader.getResource(EPICENTER_WORKFLOW).getInputStream());
				Validate.isTrue(workflow.getStates().size() > 0, "No workflow states defined!");
				workflowRepository.save(workflow);
				logger.info("Created default workflow with {} states.", workflow.getStates().size());

			} catch (JAXBException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
