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
package com.hmsinc.epicenter.integrator;

import com.hmsinc.epicenter.integrator.service.EpiCenterService;
import com.hmsinc.mergence.components.AbstractHL7Endpoint;
import com.hmsinc.mergence.model.HL7Message;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: EpiCenterEndpoint.java 116 2007-01-05 15:05:36Z
 *          steve.kondik $
 * @org.apache.xbean.XBean element="endpoint" description="EpiCenter Integrator Endpoint"
 */
public class EpiCenterEndpoint extends AbstractHL7Endpoint {

	private EpiCenterService epiCenterService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.endpoints.ProviderEndpoint#start()
	 */
	@Override
	public synchronized void start() throws Exception {

		setEpiCenterService((EpiCenterService) getEndpointApplicationContext().getBean("epiCenterService"));
		super.start();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.mergence.components.AbstractHL7Endpoint#getEndpointApplicationContextNames()
	 */
	@Override
	protected String[] getEndpointApplicationContextNames() {
		return new String[] { "classpath:epicenter-integrator-beans.xml" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.messageplex.components.AbstractHL7Endpoint#processMessage(com.hmsinc.messageplex.model.HL7Message)
	 */
	@Override
	protected void processMessage(final HL7Message hl7) throws Exception {
		epiCenterService.doProcess(hl7);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.messageplex.components.AbstractHL7Endpoint#transformMessage(com.hmsinc.messageplex.model.HL7Message)
	 */
	@Override
	protected HL7Message transformMessage(final HL7Message message) throws Exception {
		throw new UnsupportedOperationException("Endpoint does not support transformation!");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.mergence.components.AbstractHL7Endpoint#canProcess(com.hmsinc.model.messaging.HL7Message)
	 */
	@Override
	protected boolean canProcess(HL7Message message) {
		boolean ret = false;
		if (epiCenterService.getEventHandlers().containsKey(message.getType())) {
			ret = true;
		}
		return ret;
	}

	/**
	 * @return the epiCenterService
	 */
	public EpiCenterService getEpiCenterService() {
		return epiCenterService;
	}

	/**
	 * @param epiCenterService the epiCenterService to set
	 */
	public void setEpiCenterService(EpiCenterService epiCenterService) {
		this.epiCenterService = epiCenterService;
	}

}
