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
package com.hmsinc.epicenter.model.health.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.criteriaQuery;
import static com.hmsinc.epicenter.model.util.ModelUtils.namedQuery;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.health.Admit;
import com.hmsinc.epicenter.model.health.Discharge;
import com.hmsinc.epicenter.model.health.HealthObject;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.Patient;
import com.hmsinc.epicenter.model.provider.Facility;

/**
 * Manages the repository of HealthObject types.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:HealthRepositoryImpl.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
public class HealthRepositoryImpl extends AbstractJPARepository<HealthObject, Long> implements HealthRepository {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#getPatient(java.lang.String,
	 *      com.hmsinc.epicenter.model.provider.Facility)
	 */
	@SuppressWarnings("unchecked")
	public Patient getPatient(final String patientId, final Facility facility) {
		Patient ret = null;
		if (facility == null && patientId == null) {
			throw new IllegalArgumentException("Patient ID and Facility must be provided");
		}
		final List<Patient> p = namedQuery(entityManager, "getPatient").setParameter("patientId", patientId).setParameter("facility", facility).getResultList();
		if (p.size() > 0) {
			ret = p.get(0);
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#getInteractions(java.lang.String, com.hmsinc.epicenter.model.provider.Facility, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<? extends Interaction> getInteractions(String patientId, Facility facility, String visitNumber) {
		
		Validate.notNull(patientId, "Patient id must be specified");
		Validate.notNull(facility, "Facility must be specified");
		Validate.notNull(visitNumber, "Visit number must be specified");
		
		final Criteria c = criteriaQuery(entityManager, Interaction.class);
		c.createCriteria("patient").add(Restrictions.eq("patientId", patientId)).add(Restrictions.eq("facility", facility));
		c.add(Restrictions.eq("visitNumber", visitNumber));
		
		return c.list();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#findExistingNaturalKey(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Long findExistingNaturalKey(String naturalKey) {
		Long ret = null;

		final List<Long> keys = namedQuery(entityManager, "findExistingNaturalKey").setParameter("naturalKey", naturalKey).getResultList();
		if (keys.size() > 0) {
			ret = keys.get(0);
		}
		return ret;
	}

	/* 
	 * FIXME: These should be combined when VisitNumber moves to Interaction.
	 * 
	 * (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#findExistingDischarge(com.hmsinc.epicenter.model.health.Discharge)
	 */
	@SuppressWarnings("unchecked")
	public Long findExistingDischarge(Discharge discharge) {
		Long ret = null;
		final List<Long> keys = namedQuery(entityManager, "findExistingDischarge").setParameter("visitNumber", discharge.getVisitNumber())
			.setParameter("patientId", discharge.getPatient().getPatientId()).setParameter("facility", discharge.getPatient().getFacility()).getResultList();
		if (keys.size() > 0) {
			ret = keys.get(0);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#findExistingAdmit(com.hmsinc.epicenter.model.health.Admit)
	 */
	@SuppressWarnings("unchecked")
	public Long findExistingAdmit(Admit admit) {
		Long ret = null;
		final List<Long> keys = namedQuery(entityManager, "findExistingAdmit").setParameter("visitNumber", admit.getVisitNumber())
			.setParameter("patientId", admit.getPatient().getPatientId()).setParameter("facility", admit.getPatient().getFacility()).getResultList();
		if (keys.size() > 0) {
			ret = keys.get(0);
		}
		return ret;
	}

}
