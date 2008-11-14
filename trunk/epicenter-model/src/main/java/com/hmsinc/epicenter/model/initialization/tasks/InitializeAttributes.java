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

import org.springframework.core.io.ResourceLoader;

import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.attribute.Attributes;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.attribute.PatientClass;
import com.hmsinc.epicenter.model.initialization.InitializationTask;
import com.hmsinc.epicenter.model.initialization.UpgradeTasks;

/**
 * Initialize the default set of attributes from XML.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: InitializeAttributes.java 533 2007-12-10 03:53:20Z steve.kondik $
 */
public class InitializeAttributes implements InitializationTask {

	private static final String EPICENTER_ATTRIBUTES = "classpath:epicenter-attributes.xml";

	@Resource
	private AttributeRepository attributeRepository;

	@Resource
	private JAXBContext jaxbContext;

	@Resource
	private ResourceLoader resourceLoader;

	@Resource
	private UpgradeTasks upgradeTasks;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.initialization.InitializationTask#executeTask()
	 */
	public void executeTask() {

		try {
			
			final Attributes wa = (Attributes) jaxbContext.createUnmarshaller().unmarshal(
					resourceLoader.getResource(EPICENTER_ATTRIBUTES).getInputStream());

			// Validate the age groups.
			upgradeTasks.validateAttributes(wa.getAgeGroups(), AgeGroup.class, attributeRepository);

			// Validate genders
			upgradeTasks.validateAttributes(wa.getGenders(), Gender.class, attributeRepository);

			// Validate patient classes
			upgradeTasks.validateAttributes(wa.getPatientClasses(), PatientClass.class, attributeRepository);
			
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
