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
package com.hmsinc.epicenter.integrator.service.event;

import com.hmsinc.epicenter.integrator.DuplicateDataException;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.PatientDetail;
import com.hmsinc.mergence.model.HL7Message;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * 
 */
public interface HL7EventHandler<E extends Interaction> {

	public E handleEvent(final HL7Message message, final PatientDetail details) throws Exception;

	public void checkForExistingInteraction(Interaction interaction) throws DuplicateDataException;
	
}
