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

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Geography which is contained within a state.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:StateFeature.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@MappedSuperclass
public abstract class StateFeature extends Geography {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9042526821639956189L;

	private State state;

	/**
	 * @return the state
	 */
	@XmlIDREF
	@XmlAttribute(required = true)
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_GEO_STATE", unique = false, nullable = true, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_GEOGRAPHY_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
	public State getState() {
		return state;
	}

	/**
	 * @param state
	 *            the statee to set
	 */
	public void setState(State state) {
		this.state = state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("state",
				(getState() == null ? null : getState().getName())).toString();
	}

}
