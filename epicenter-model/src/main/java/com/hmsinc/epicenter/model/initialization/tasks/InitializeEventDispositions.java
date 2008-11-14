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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.Validate;
import org.springframework.core.io.ResourceLoader;

import com.hmsinc.epicenter.model.initialization.InitializationTask;
import com.hmsinc.epicenter.model.initialization.UpgradeTasks;
import com.hmsinc.epicenter.model.util.ExportData;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;

/**
 * @author shade
 * 
 */
public class InitializeEventDispositions implements InitializationTask {

	private static final String EPICENTER_ATTRIBUTES = "classpath:epicenter-event-disposition.xml";

	@Resource
	private JAXBContext jaxbContext;

	@Resource
	private ResourceLoader resourceLoader;

	@Resource
	private WorkflowRepository workflowRepository;

	@Resource
	private UpgradeTasks upgradeTasks;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.initialization.InitializationTask#executeTask()
	 */
	public void executeTask() {

		try {

			final ExportData items = (ExportData) jaxbContext.createUnmarshaller().unmarshal(
					resourceLoader.getResource(EPICENTER_ATTRIBUTES).getInputStream());
			Validate.notNull(items, "Unable to get dispositions");
			
			final List<EventDisposition> dispositions = new ArrayList<EventDisposition>();
			for (Object item : items.getData()) {
				Validate.isTrue(item instanceof EventDisposition, "Item is not a disposition.");
				dispositions.add((EventDisposition) item);
			}
			Validate.isTrue(dispositions.size() > 0, "No valid dispositions were found.");
			
			upgradeTasks.validateAttributes(dispositions, EventDisposition.class, workflowRepository);

		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
