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
package com.hmsinc.epicenter.webapp.util;

import javax.annotation.Resource;

import org.jasypt.util.password.PasswordEncryptor;
import org.springframework.security.providers.encoding.PasswordEncoder;

/**
 * @author shade
 * @version $Id: SpringSecurityPasswordEncoder.java 1654 2008-05-13 14:26:16Z steve.kondik $
 */
public class SpringSecurityPasswordEncoder implements PasswordEncoder {

	@Resource
	private PasswordEncryptor passwordEncryptor = null;

	/**
	 * Encodes a password. This implementation completely ignores salt, as
	 * jasypt's <tt>PasswordEncryptor</tt> and <tt>StringDigester</tt>
	 * normally use a random one. Thus, it can be safely passed as <tt>null</tt>.
	 * 
	 * @param rawPass
	 *            The password to be encoded.
	 * @param salt
	 *            The salt, which will be ignored. It can be null.
	 */
	public String encodePassword(String rawPass, Object salt) {

		return passwordEncryptor.encryptPassword(rawPass);

	}

	/**
	 * Checks a password's validity. This implementation completely ignores
	 * salt, as jasypt's <tt>PasswordEncryptor</tt> and
	 * <tt>StringDigester</tt> normally use a random one. Thus, it can be
	 * safely passed as <tt>null</tt>.
	 * 
	 * @param encPass
	 *            The encrypted password (digest) against which to check.
	 * @param rawPass
	 *            The password to be checked.
	 * @param salt
	 *            The salt, which will be ignored. It can be null.
	 */
	public boolean isPasswordValid(String encPass, String rawPass, Object salt) {

		return passwordEncryptor.checkPassword(rawPass, encPass);

	}

}
