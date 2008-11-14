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

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.workflow.Activity;

/**
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ActivityDTO.java 966 2008-02-15 15:44:11Z steve.kondik $
 */
@DataTransferObject
public class ActivityDTO implements Serializable, Comparable<ActivityDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2126422084150665943L;

	public final Long id;

	public final DateTime timestamp;

	public final String name;

	public final String username;

	public final String email;

	public final String log;

	/**
	 * @param id
	 * @param timestamp
	 * @param username
	 * @param email
	 * @param log
	 */
	public ActivityDTO(final Activity activity) {
		super();
		this.id = activity.getId();
		this.timestamp = activity.getTimestamp();
		this.name = activity.getUser().getFirstName() + " " + activity.getUser().getLastName();
		this.username = activity.getUser().getUsername();
		this.email = activity.getUser().getEmailAddress();
		this.log = activity.getLog();
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the timestamp
	 */
	public DateTime getTimestamp() {
		return timestamp;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @return the log
	 */
	public String getLog() {
		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ActivityDTO rhs) {
		return new CompareToBuilder().append(rhs.getTimestamp(), timestamp).append(rhs.getId(), id).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		return EqualsBuilder.reflectionEquals(this, o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
