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
package com.hmsinc.epicenter.model.provider;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author shade
 * 
 */
@MappedSuperclass
@XmlType(name="PhysicalEntity", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class PhysicalEntity implements ProviderObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1126656219220319547L;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String address1;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String address2;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String city;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String state;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String zipcode;

	/**
	 * @return the address1
	 */
	@Column(name = "ADDRESS1", unique = false, nullable = true, insertable = true, updatable = true, length = 40)
	public String getAddress1() {
		return address1;
	}

	/**
	 * @param address1
	 *            the address1 to set
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	/**
	 * @return the address2
	 */
	@Column(name = "ADDRESS2", unique = false, nullable = true, insertable = true, updatable = true, length = 40)
	public String getAddress2() {
		return address2;
	}

	/**
	 * @param address2
	 *            the address2 to set
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * @return the city
	 */
	@Column(name = "CITY", unique = false, nullable = true, insertable = true, updatable = true, length = 25)
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	@Column(name = "STATE", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the zipcode
	 */
	@Column(name = "ZIPCODE", unique = false, nullable = true, insertable = true, updatable = true, length = 5)
	public String getZipcode() {
		return zipcode;
	}

	/**
	 * @param zipcode
	 *            the zipcode to set
	 */
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

}
