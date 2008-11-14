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
package com.hmsinc.epicenter.model.attribute;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * JAXB2 top level element for defining static data.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WarehouseAttributes.java 143 2007-05-19 07:45:47Z steve.kondik $
 */
@XmlRootElement(name = "attributes", namespace = "http://epicenter.hmsinc.com/model")
@XmlType(name = "Attributes", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class Attributes {

	@XmlElementWrapper(name = "agegroups", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "agegroup", namespace = "http://epicenter.hmsinc.com/model")
	private List<AgeGroup> ageGroups = new ArrayList<AgeGroup>();

	@XmlElementWrapper(name = "genders", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "gender", namespace = "http://epicenter.hmsinc.com/model")
	private List<Gender> genders = new ArrayList<Gender>();

	@XmlElementWrapper(name = "patient-classes", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "patient-class", namespace = "http://epicenter.hmsinc.com/model")
	private List<PatientClass> patientClasses = new ArrayList<PatientClass>();
	
	/**
	 * @return the ageGroups
	 */
	public List<AgeGroup> getAgeGroups() {
		return ageGroups;
	}

	/**
	 * @param ageGroups
	 *            the ageGroups to set
	 */
	public void setAgeGroups(List<AgeGroup> ageGroups) {
		this.ageGroups = ageGroups;
	}

	/**
	 * @return the genders
	 */
	public List<Gender> getGenders() {
		return genders;
	}

	/**
	 * @param genders
	 *            the genders to set
	 */
	public void setGenders(List<Gender> genders) {
		this.genders = genders;
	}

	/**
	 * @return the patientClasses
	 */
	public List<PatientClass> getPatientClasses() {
		return patientClasses;
	}

	/**
	 * @param patientClasses the patientClasses to set
	 */
	public void setPatientClasses(List<PatientClass> patientClasses) {
		this.patientClasses = patientClasses;
	}

	
}
