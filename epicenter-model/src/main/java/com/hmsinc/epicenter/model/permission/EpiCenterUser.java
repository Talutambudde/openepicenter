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

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Email;
import org.joda.time.DateTime;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.userdetails.UserDetails;

import com.hmsinc.epicenter.model.workflow.Activity;
import com.hmsinc.epicenter.model.workflow.Attachment;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.model.workflow.Subscription;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.util.GeometryCombiner;

/**
 * A user of the EpiCenter system.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:EpiCenterUser.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
@Entity
@Table(name = "APP_USER")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( {
		@NamedQuery(name = "getUser", query = "from EpiCenterUser u where u.username = :username"),
		@NamedQuery(name = "getUserByEmailAddress", query = "from EpiCenterUser u where lower(u.emailAddress) = lower(:emailAddress)"),
		@NamedQuery(name = "getUserCount", query = "select count(*) from EpiCenterUser where username = :username"),
		@NamedQuery(name = "getUserCountByEmailAddress", query = "select count(*) from EpiCenterUser where lower(emailAddress) = lower(:emailAddress)"),
		@NamedQuery(name = "checkForExistingUser", query = "select count(*) from EpiCenterUser where username = :username or lower(emailAddress) = lower(:emailAddress)") })
@XmlType(name = "EpiCenterUser", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "user", namespace = "http://epicenter.hmsinc.com/model")
public class EpiCenterUser implements PermissionObject, UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3756516481810727109L;
	
	@XmlAttribute
	private Long id;

	private DateTime createdTimestamp = new DateTime();

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String username;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String password;

	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	private String emailAddress;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String firstName;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String middleInitial;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String lastName;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String title;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String organization;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String address;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String city;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String state;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String zipcode;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String phoneNumber;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String phoneExtension;
	
	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String faxNumber;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private boolean enabled = true;

	@XmlElementWrapper(name = "roles", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "role", namespace = "http://epicenter.hmsinc.com/model")
	private Set<EpiCenterRole> roles = new HashSet<EpiCenterRole>();

	private Set<Organization> organizations = new HashSet<Organization>();

	private Set<Investigation> assignedEvents = new HashSet<Investigation>();

	private Set<Activity> activities = new HashSet<Activity>();

	private Set<Attachment> attachments = new HashSet<Attachment>();

	private Set<Subscription> subscriptions = new HashSet<Subscription>();

	private Set<AuditEvent> auditEvents = new HashSet<AuditEvent>();

	private Map<String, String> preferences = new HashMap<String, String>();
	
	private transient PreparedGeometry visibleRegion;

	private transient PreparedGeometry aggregateOnlyVisibleRegion;
	
	/**
	 * 
	 */
	public EpiCenterUser() {
		super();
	}

	/**
	 * @param id
	 */
	public EpiCenterUser(Long id) {
		super();
		this.id = id;
	}

	/**
	 * @param username
	 * @param password
	 * @param emailAddress
	 */
	public EpiCenterUser(String username, String password, String emailAddress) {
		super();
		this.username = username;
		this.password = password;
		this.emailAddress = emailAddress;
	}

	/**
	 * @param id
	 * @param username
	 * @param password
	 * @param emailAddress
	 * @param firstName
	 * @param middleInitial
	 * @param lastName
	 * @param title
	 * @param organization
	 * @param address
	 * @param city
	 * @param state
	 * @param zipcode
	 * @param phoneNumber
	 * @param faxNumber
	 * @param enabled
	 * @param roles
	 */
	public EpiCenterUser(Long id, String username, String password, String emailAddress, String firstName,
			String middleInitial, String lastName, String title, String organization, String address, String city,
			String state, String zipcode, String phoneNumber, String faxNumber, boolean enabled,
			Set<EpiCenterRole> roles) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.middleInitial = middleInitial;
		this.lastName = lastName;
		this.title = title;
		this.organization = organization;
		this.address = address;
		this.city = city;
		this.state = state;
		this.zipcode = zipcode;
		this.phoneNumber = phoneNumber;
		this.faxNumber = faxNumber;
		this.enabled = enabled;
		this.roles = roles;
	}

	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_APP_USER") })
	@GeneratedValue(strategy = SEQUENCE, generator = "generator")
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
	 * @return the createdTimestamp
	 */
	@Type(type = "joda")
	@Column(name = "CREATED_TIMESTAMP", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getCreatedTimestamp() {
		return createdTimestamp;
	}

	/**
	 * @param createdTimestamp
	 *            the createdTimestamp to set
	 */
	public void setCreatedTimestamp(DateTime createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	/**
	 * @return the username
	 */
	@Column(name = "USERNAME", unique = true, nullable = false, insertable = true, updatable = true, length = 80)
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	@Column(name = "PASSWORD", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the emailAddress
	 */
	@Email
	@Column(name = "EMAIL", unique = true, nullable = false, insertable = true, updatable = true, length = 80)
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
	 * @return the firstName
	 */
	@Column(name = "FIRST_NAME", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
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
	 * @return the middleInitial
	 */
	@Column(name = "MIDDLE_INITIAL", unique = false, nullable = true, insertable = true, updatable = true, length = 2)
	public String getMiddleInitial() {
		return middleInitial;
	}

	/**
	 * @param middleInitial
	 *            the middleInitial to set
	 */
	public void setMiddleInitial(String middleInitial) {
		this.middleInitial = middleInitial;
	}

	/**
	 * @return the lastName
	 */
	@Column(name = "LAST_NAME", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
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
	@Column(name = "TITLE", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
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
	 * @return the organization
	 */
	@Column(name = "ORGANIZATION", unique = false, nullable = true, insertable = true, updatable = true, length = 100)
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the address
	 */
	@Column(name = "ADDRESS", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getAddress() {
		return address;
	}

	/**
	 * @param address
	 *            the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the city
	 */
	@Column(name = "CITY", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
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
	@Column(name = "STATE", unique = false, nullable = true, insertable = true, updatable = true, length = 80)
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
	@Column(name = "ZIPCODE", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
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

	/**
	 * @return the phoneNumber
	 */
	@Column(name = "PHONE", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
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
	 * @return the phoneExtension
	 */
	@Column(name = "EXTENSION", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
	public String getPhoneExtension() {
		return phoneExtension;
	}

	/**
	 * @param phoneExtension the phoneExtension to set
	 */
	public void setPhoneExtension(String phoneExtension) {
		this.phoneExtension = phoneExtension;
	}

	/**
	 * @return the faxNumber
	 */
	@Column(name = "FAX", unique = false, nullable = true, insertable = true, updatable = true, length = 20)
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
	 * @return the enabled
	 */
	@Column(name = "ENABLED", unique = false, nullable = false, insertable = true, updatable = true, length = 1)
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled
	 *            the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the roles
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER, targetEntity = EpiCenterRole.class)
	@JoinTable(name = "APP_USER_ROLE", joinColumns = { @JoinColumn(name = "ID_APP_USER") }, inverseJoinColumns = { @JoinColumn(name = "ID_APP_ROLE") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<EpiCenterRole> getRoles() {
		return roles;
	}

	/**
	 * @param roles
	 *            the roles to set
	 */
	public void setRoles(Set<EpiCenterRole> roles) {
		this.roles = roles;
	}

	/**
	 * @return the organizations
	 */
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, mappedBy = "users", targetEntity = Organization.class)
	@ForeignKey(name = "FK_ORGANIZATION_USER_1", inverseName = "FK_ORGANIZATION_USER_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Organization> getOrganizations() {
		return organizations;
	}

	/**
	 * @param organizations
	 *            the organizations to set
	 */
	public void setOrganizations(Set<Organization> organizations) {
		this.organizations = organizations;
	}

	/**
	 * @return the assignedEvents
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, mappedBy = "assignedTo")
	public Set<Investigation> getAssignedEvents() {
		return assignedEvents;
	}

	/**
	 * @param assignedEvents
	 *            the assignedEvents to set
	 */
	public void setAssignedEvents(Set<Investigation> assignedEvents) {
		this.assignedEvents = assignedEvents;
	}

	/**
	 * @return the activities
	 */
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Activity> getActivities() {
		return activities;
	}

	/**
	 * @param activities
	 *            the activities to set
	 */
	public void setActivities(Set<Activity> activities) {
		this.activities = activities;
	}

	/**
	 * @return the attachments
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "owner")
	public Set<Attachment> getAttachments() {
		return attachments;
	}

	/**
	 * @param attachments
	 *            the attachments to set
	 */
	public void setAttachments(Set<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * @return the subscriptions
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "user")
	public Set<Subscription> getSubscriptions() {
		return subscriptions;
	}

	/**
	 * @param subscriptions
	 *            the subscriptions to set
	 */
	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	/**
	 * @return the auditEvents
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "user")
	public Set<AuditEvent> getAuditEvents() {
		return auditEvents;
	}

	/**
	 * @param auditEvents
	 *            the auditEvents to set
	 */
	public void setAuditEvents(Set<AuditEvent> auditEvents) {
		this.auditEvents = auditEvents;
	}

	/**
	 * @return the preferences
	 */
	@CollectionOfElements
	@JoinTable(name = "APP_USER_PREF", joinColumns = @JoinColumn(name = "ID_APP_USER"))
	@Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	@Column(name = "PREF_VALUE", nullable = false, length = 400)
	@ForeignKey(name = "FK_APP_USER_PREF_1")
	@org.hibernate.annotations.MapKey(columns = { @Column(name = "PREF_KEY") } )
	public Map<String, String> getPreferences() {
		return preferences;
	}

	/**
	 * @param preferences
	 *            the preferences to set
	 */
	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	/**
	 * Computes the user's visible region.
	 * 
	 * @return
	 */
	@Transient
	public PreparedGeometry getVisibleRegion() {

		if (this.visibleRegion == null) {

			synchronized (this) {
				final Set<Geometry> geometries = new HashSet<Geometry>();

				for (Organization org : organizations) {
					if (org.isEnabled()) {
						geometries.add(org.getVisibleRegion().getGeometry());
					}
				}

				if (geometries.size() == 1) {
					this.visibleRegion = PreparedGeometryFactory.prepare(geometries.iterator().next());
				} else if (geometries.size() > 1) {
					final Geometry combined = GeometryCombiner.combine(geometries);
					if (combined != null) {
						this.visibleRegion = PreparedGeometryFactory.prepare(combined.union());
					}
				}
			}
		}
		return this.visibleRegion;
	}

	/**
	 * Computes the filter for limited view.
	 * 
	 * @return the aggregateOnlyVisibleRegion
	 */
	@Transient
	public PreparedGeometry getAggregateOnlyVisibleRegion() {
		
		if (this.aggregateOnlyVisibleRegion == null) {
			
			synchronized (this) {
				final Set<Geometry> geometries = new HashSet<Geometry>();
				for (Organization org :organizations) {
					if (org.isEnabled()) {
						for (AuthorizedRegion ar : org.getAuthorizedRegions()) {
							if (AuthorizedRegionType.AGGREGATE_ONLY.equals(ar.getType())) {
								geometries.add(ar.getGeography().getGeometry());
							}
						}
					}
				}
				
				if (geometries.size() == 1) {
					this.aggregateOnlyVisibleRegion = PreparedGeometryFactory.prepare(geometries.iterator().next());
				} else if (geometries.size() > 1) {
					final Geometry combined = GeometryCombiner.combine(geometries);
					if (combined != null) {
						this.aggregateOnlyVisibleRegion = PreparedGeometryFactory.prepare(combined.union());
					}
				}
			}
		}
		
		return this.aggregateOnlyVisibleRegion;
	}

	/**
	 * @return
	 */
	@Transient
	public Envelope getVisibleRegionEnvelope() {
		return getVisibleRegion() == null ? null : getVisibleRegion().getGeometry().getEnvelopeInternal();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append(id).append(username).append(emailAddress).append(firstName).append(
				middleInitial).append(lastName).append(title).append(organization).append(address).append(city).append(
				state).append(zipcode).append(phoneNumber).append(faxNumber).append(enabled).append(roles).toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#getAuthorities()
	 */
	@Transient
	public GrantedAuthority[] getAuthorities() {
		return roles.toArray(new EpiCenterRole[roles.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
	 */
	@Transient
	public boolean isAccountNonExpired() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
	 */
	@Transient
	public boolean isAccountNonLocked() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	@Transient
	public boolean isCredentialsNonExpired() {
		return true;
	}

}
