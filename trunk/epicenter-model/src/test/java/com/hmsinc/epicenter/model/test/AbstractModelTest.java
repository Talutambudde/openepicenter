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

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.jpa.AbstractJpaTests;

import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.initialization.UpgradeTasks;
import com.hmsinc.epicenter.model.permission.PermissionRepository;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;

/**
 * Base class providing all Spring beans for the EpiCenter datamodel.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AbstractWarehouseTest.java 203 2007-04-23 02:19:47Z
 *          steve.kondik $
 */
public abstract class AbstractModelTest extends AbstractJpaTests {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	protected AnalysisRepository analysisRepository;

	@Resource
	protected AttributeRepository attributeRepository;

	@Resource
	protected GeographyRepository geographyRepository;

	@Resource
	protected HealthRepository healthRepository;

	@Resource
	protected PermissionRepository permissionRepository;

	@Resource
	protected ProviderRepository providerRepository;

	@Resource
	protected SurveillanceRepository surveillanceRepository;

	@Resource
	protected WorkflowRepository workflowRepository;
	
	@Resource
	protected UpgradeTasks upgradeTasks;

	@Resource
	protected JAXBContext jaxbContext;

}
