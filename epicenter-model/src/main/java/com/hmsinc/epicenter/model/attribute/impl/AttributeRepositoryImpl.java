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
package com.hmsinc.epicenter.model.attribute.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.namedQuery;

import java.util.List;
import java.util.Locale;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.AttributeObject;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.attribute.PatientClass;

/**
 * Manages the repository of Attribute types.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AttributeRepositoryImpl.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
public class AttributeRepositoryImpl extends AbstractJPARepository<AttributeObject, Long> implements AttributeRepository {

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.attribute.AnalysisRepository#getGenderByAbbreviation(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public Gender getGenderByAbbreviation(final String abbreviation) {
		Gender g = null;
		if (abbreviation != null) {
			final List<Gender> gl = namedQuery(entityManager, "getGenderByAbbreviation").setParameter("abbreviation", abbreviation.toUpperCase(Locale.getDefault())).getResultList();
			if (gl.size() == 1) {
				g = gl.get(0);
			}
		}
		if (abbreviation == null || g == null) {
			g = (Gender)namedQuery(entityManager, "getUnknownGender").getSingleResult();
		}
		return g;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.attribute.AnalysisRepository#getAgeGroupForAge(java.lang.Integer)
	 */
	@SuppressWarnings("unchecked")
	public AgeGroup getAgeGroupForAge(final Integer age) {
		AgeGroup ret = null;
		if (age != null) {
			final List<AgeGroup> ags = namedQuery(entityManager, "getAgeGroupForAge").setParameter("age", age).getResultList();
			if (ags.size() > 1) {
				throw new RuntimeException("More than one AgeGroup found for " + age);
			}
			if (ags.size() == 1) {
				ret = ags.get(0);
			}
		}
		if (age == null || ret == null) {
			ret = (AgeGroup) namedQuery(entityManager, "getUnknownAgeGroup").getSingleResult();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#getPatientClassByAbbreviation(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public PatientClass getPatientClassByAbbreviation(String abbreviation) {
		PatientClass ret = null;
		if (abbreviation != null) {
			final List<PatientClass> pcs = namedQuery(entityManager, "getPatientClassByAbbreviation").setParameter("abbreviation", abbreviation).getResultList();
			if (pcs.size() == 1) {
				ret = pcs.get(0);
			}
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.health.HealthRepository#getPatientClassByName(java.lang.String)
	 */
	public PatientClass getPatientClassByName(String name) {
		return (PatientClass)namedQuery(entityManager, "getPatientClassByName").setParameter("name", name).getSingleResult();
	}
		
}
