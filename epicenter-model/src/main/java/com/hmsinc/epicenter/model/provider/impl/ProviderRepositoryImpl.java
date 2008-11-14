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
package com.hmsinc.epicenter.model.provider.impl;

import static java.lang.Boolean.TRUE;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.provider.DataConnection;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderObject;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.hmsinc.hibernate.spatial.GeometryType;

/**
 * Manages the repository of ProviderObjects.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ProviderRepositoryImpl.java 150 2007-05-20 17:15:29Z
 *          steve.kondik $
 */
public class ProviderRepositoryImpl extends AbstractJPARepository<ProviderObject, Long> implements ProviderRepository {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.provider.ProviderRepository#getAllDataConnections()
	 */
	@SuppressWarnings("unchecked")
	public List<DataConnection> getAllDataConnections() {
		return entityManager.createNamedQuery("getAllDataConnections").setHint(CACHE_HINT, TRUE).getResultList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.provider.ProviderRepository#getDataConnectionByName(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public DataConnection getDataConnectionByName(final String dataConnectionName) {

		Validate.notNull(dataConnectionName);

		DataConnection dc = null;
		final List<DataConnection> l = entityManager.createNamedQuery("getDataConnectionByName").setParameter("name",
				dataConnectionName).setHint(CACHE_HINT, TRUE).getResultList();
		if (l.size() == 1) {
			dc = l.get(0);
		}

		return dc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.provider.ProviderRepository#getFacilityByIdentifier(java.lang.Stringg)
	 */
	@SuppressWarnings("unchecked")
	public Facility getFacilityByIdentifier(final String identifier) {

		Facility facility = null;
		if (identifier != null) {
			final List<Facility> l = entityManager.createNamedQuery("getFacilityByIdentifier").setParameter("identifier", identifier)
			.setHint(CACHE_HINT, TRUE).getResultList();

			if (l.size() == 1) {
				facility = l.get(0);
			}
		}

		return facility;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.provider.ProviderRepository#getFacilitiesInGeography(com.hmsinc.epicenter.model.geography.Geography)
	 */
	@SuppressWarnings("unchecked")
	public List<Facility> getFacilitiesInGeography(Geography geography) {

		Validate.notNull(geography);

		return ((Session) entityManager.getDelegate()).getNamedQuery("getFacilitiesInGeography").setParameter("geo",
				geography.getGeometry(), Hibernate.custom(GeometryType.class)).setCacheable(true).list();
	}

}
