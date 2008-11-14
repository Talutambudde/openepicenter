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
package com.hmsinc.epicenter.webapp.dto;

import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.health.CodedVisit;
import com.hmsinc.epicenter.model.health.Interaction;

/**
 * @author shade
 * @version $Id: CasesDetailDTO.java 1654 2008-05-13 14:26:16Z steve.kondik $
 */
public class CasesDetailDTO extends CasesDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5263429555482129024L;

	private final String id;
	
	private final String icd9Codes;

	private final String type;

	private final String patientClass;

	private final DateTime dob;

	private final String ageGroup;

	/**
	 * @param interaction
	 */
	public CasesDetailDTO(Interaction interaction) {
		super(interaction);
		this.id = interaction.getId().toString();
		this.icd9Codes = (interaction instanceof CodedVisit ? ((CodedVisit) interaction).getIcd9() : "None");
		this.type = interaction.getClass().getSimpleName();
		this.patientClass = interaction.getPatientClass().getName();
		this.dob = interaction.getPatientDetail().getDateOfBirth();
		this.ageGroup = interaction.getAgeGroup().getName();

	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @return the icd9Codes
	 */
	public String getIcd9Codes() {
		return icd9Codes;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the patientClass
	 */
	public String getPatientClass() {
		return patientClass;
	}

	/**
	 * @return the dob
	 */
	public DateTime getDob() {
		return dob;
	}

	/**
	 * @return the ageGroup
	 */
	public String getAgeGroup() {
		return ageGroup;
	}

}
