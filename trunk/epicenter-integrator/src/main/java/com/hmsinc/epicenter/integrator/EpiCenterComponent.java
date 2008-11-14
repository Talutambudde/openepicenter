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

import java.util.List;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.Endpoint;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EpiCenterComponent.java 205 2007-09-26 16:52:01Z steve.kondik $
 * @org.apache.xbean.XBean element="component" description="EpiCenter Integrator Component"
 */
public class EpiCenterComponent extends DefaultComponent {

	private Endpoint[] endpoints;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.DefaultComponent#getConfiguredEndpoints()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected List<Endpoint> getConfiguredEndpoints() {
		return asList(getEndpoints());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.DefaultComponent#getEndpointClasses()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Class[] getEndpointClasses() {
		return new Class[] { EpiCenterEndpoint.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.servicemix.common.AsyncBaseLifeCycle#exceptionShouldRollbackTx(java.lang.Exception)
	 */
	@Override
	protected boolean exceptionShouldRollbackTx(Exception e) {
		return true;
	}

	/**
	 * @return the endpoints
	 */
	public Endpoint[] getEndpoints() {
		return endpoints;
	}

	/**
	 * @param endpoints
	 *            the endpoints to set
	 */
	public void setEndpoints(Endpoint[] endpoints) {
		this.endpoints = endpoints;
	}

}
