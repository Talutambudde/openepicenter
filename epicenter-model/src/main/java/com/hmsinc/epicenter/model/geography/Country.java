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
package com.hmsinc.epicenter.model.geography;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * TODO: Not in the geodatabase yet.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Country.java 144 2007-05-19 07:57:56Z steve.kondik $
 */
@Entity
@DiscriminatorValue("O")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all", region = "com.hmsinc.epicenter.model.GeographyCache")
@XmlRootElement(name = "country", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Country", namespace = "http://epicenter.hmsinc.com/model")
public class Country extends Geography {

	/**
	 * 
	 */
	private static final long serialVersionUID = -386426515645010716L;

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.geography.Geography#getDisplayName()
	 */
	@Override
	@Transient
	public String getDisplayName() {
		return getName(); 
	}
}
