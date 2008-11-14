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

import static javax.persistence.GenerationType.AUTO;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author shade
 * @version $Id: Contact.java 1238 2008-03-06 15:25:27Z steve.kondik $
 */
@Entity
@Table(name = "CONTACT")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Contact extends PhysicalEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private String firstName;

	private String lastName;

	private String title;

	private String extension;

	private String emailAddress;

	private String phoneNumber;

	private String faxNumber;

	private String pagerNumber;

	private String cellPhoneNumber;

	private String note;
	
	private boolean massEmail = false;

	private Set<Facility> facilities = new HashSet<Facility>();

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_CONTACT") })
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
	 * @return the firstName
	 */
	@Column(name = "FIRST_NAME", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName
	 *            the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return the lastName
	 */
	@Column(name = "LAST_NAME", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
	public String getLastName() {
		return lastName;
	}

	/**
	 * @param lastName
	 *            the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * @return the title
	 */
	@Column(name = "TITLE", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the extension
	 */
	@Column(name = "EXTENSION", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
	public String getExtension() {
		return extension;
	}

	/**
	 * @param extension
	 *            the extension to set
	 */
	public void setExtension(String extension) {
		this.extension = extension;
	}

	/**
	 * @return the emailAddress
	 */
	@Column(name = "EMAIL", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param emailAddress
	 *            the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @return the pagerNumber
	 */
	@Column(name = "PAGER_NUMBER", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
	public String getPagerNumber() {
		return pagerNumber;
	}

	/**
	 * @param pagerNumber
	 *            the pagerNumber to set
	 */
	public void setPagerNumber(String pagerNumber) {
		this.pagerNumber = pagerNumber;
	}

	/**
	 * @return the cellPhoneNumber
	 */
	@Column(name = "CELL_NUMBER", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
	public String getCellPhoneNumber() {
		return cellPhoneNumber;
	}

	/**
	 * @param cellPhoneNumber
	 *            the cellPhoneNumber to set
	 */
	public void setCellPhoneNumber(String cellPhoneNumber) {
		this.cellPhoneNumber = cellPhoneNumber;
	}

	/**
	 * @return the massEmail
	 */
	@Column(name = "MASS_EMAIL", unique = false, nullable = true, insertable = true, updatable = true)
	public boolean isMassEmail() {
		return massEmail;
	}

	/**
	 * @param massEmail
	 *            the massEmail to set
	 */
	public void setMassEmail(boolean massEmail) {
		this.massEmail = massEmail;
	}

	/**
	 * @return the phoneNumber
	 */
	@Column(name = "PHONE_NUMBER", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
	public String getPhoneNumber() {
		return phoneNumber;
	}

	/**
	 * @param phoneNumber
	 *            the phoneNumber to set
	 */
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	/**
	 * @return the faxNumber
	 */
	@Column(name = "FAX_NUMBER", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
	public String getFaxNumber() {
		return faxNumber;
	}

	/**
	 * @param faxNumber
	 *            the faxNumber to set
	 */
	public void setFaxNumber(String faxNumber) {
		this.faxNumber = faxNumber;
	}

	/**
	 * @return the note
	 */
	@Column(name = "NOTE", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getNote() {
		return note;
	}

	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the facilities
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "contacts", targetEntity = Facility.class)
	@ForeignKey(name = "FK_FACILITY_CONTACT_1", inverseName = "FK_FACILITY_CONTACT_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Facility> getFacilities() {
		return facilities;
	}

	/**
	 * @param facilities
	 *            the facilities to set
	 */
	public void setFacilities(Set<Facility> facilities) {
		this.facilities = facilities;
	}

}
