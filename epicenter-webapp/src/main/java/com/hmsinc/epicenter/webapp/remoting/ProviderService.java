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
package com.hmsinc.epicenter.webapp.remoting;

import java.util.Collection;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderRepository;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ProviderService.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@RemoteProxy(name = "ProviderService")
public class ProviderService extends AbstractRemoteService {

	@Resource
	private ProviderRepository providerRepository;
	
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<Facility> getFacilitiesInGeography(final String geographyID) {

		Validate.notNull(geographyID, "Geography ID must not be null!");
		final Geography geography = geographyRepository.load(Long.valueOf(geographyID), Geography.class);

		Validate.notNull(geography, "No result for geography ID " + geographyID);

		return providerRepository.getFacilitiesInGeography(geography);

	}

}
