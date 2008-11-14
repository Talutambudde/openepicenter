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
package com.hmsinc.epicenter.model.permission;

import static javax.persistence.GenerationType.AUTO;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

/**
 * Generated when a user is sent a "forgot password" email. The token has a
 * default lifetime of 1 day.
 * 
 * @author shade
 * @version $Id: PasswordResetToken.java 1089 2008-02-23 16:59:07Z steve.kondik $
 */
@Entity
@Table(name = "APP_USER_TOKEN")
@NamedQueries( { @NamedQuery(name = "purgeExpiredTokens", query = "delete from PasswordResetToken where expiration < :time"),
	@NamedQuery(name = "getPasswordResetToken", query = "from PasswordResetToken where token = :token") } )
public class PasswordResetToken implements PermissionObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private EpiCenterUser user;

	private String token;

	private DateTime expiration = new DateTime().plusDays(1);

	PasswordResetToken() {
		// Default constructor for Hibernate
	}
	
	public PasswordResetToken(EpiCenterUser user) {
		this.user = user;
		this.token = StringUtils.remove(UUID.randomUUID().toString().toUpperCase(), "-");
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_APP_USER_TOKEN") })
	@GeneratedValue(strategy = AUTO, generator = "generator")
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the user
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_APP_USER", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_APP_USER_TOKEN_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public EpiCenterUser getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(EpiCenterUser user) {
		this.user = user;
	}

	/**
	 * @return the token
	 */
	@Column(name = "TOKEN", unique = true, nullable = false, insertable = true, updatable = true, length = 32)
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the expiration
	 */
	@Type(type = "joda")
	@Column(name = "EXPIRATION", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getExpiration() {
		return expiration;
	}

	/**
	 * @param expiration
	 *            the expiration to set
	 */
	public void setExpiration(DateTime expiration) {
		this.expiration = expiration;
	}

}
