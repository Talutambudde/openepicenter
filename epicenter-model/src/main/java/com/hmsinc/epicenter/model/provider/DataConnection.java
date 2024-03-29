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

// Generated Jan 15, 2007 10:17:21 AM by Hibernate Tools 3.2.0.beta8

import static javax.persistence.GenerationType.AUTO;

import java.util.HashSet;
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
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * DataConnection generated by hbm2java
 */
@Entity
@Table(name = "DATA_CONNECTION")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { @NamedQuery(name = "getDataConnectionByName", query = "from DataConnection where name = :name"),
	@NamedQuery(name="exportDataConnection", query = "from DataConnection d left join fetch d.facilities f")})
@XmlType(name="DataConnection", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="connection", namespace = "http://epicenter.hmsinc.com/model")
public class DataConnection implements ProviderObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6664101010873423016L;

	// Fields

	@XmlAttribute(name="id")
	private Long id;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String name;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String productionIpAddress;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String testIpAddress;
	
	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private Short productionPort;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private Short testPort;

	@XmlElement(name="interface", namespace = "http://epicenter.hmsinc.com/model")
	private String interface_;

	@XmlElement(namespace = "http://epicenter.hmsinc.com/model")
	private String processMode;

	@XmlElementWrapper(name = "facilities", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "facility", namespace = "http://epicenter.hmsinc.com/model")
	private Set<Facility> facilities = new HashSet<Facility>(0);

	// Constructors

	/** default constructor */
	public DataConnection() {
	}

	/** minimal constructor */
	public DataConnection(String name) {
		this.name = name;
	}

	/** full constructor */
	public DataConnection(Long id, String name, String productionIpAddress, String testIpAddress, Short productionPort, Short testPort, String interface_,
			String processMode, Set<Facility> facilities) {
		this.id = id;
		this.name = name;
		this.productionIpAddress = productionIpAddress;
		this.testIpAddress = testIpAddress;
		this.productionPort = productionPort;
		this.testPort = testPort;
		this.interface_ = interface_;
		this.processMode = processMode;
		this.facilities = facilities;
	}

	// Property accessors
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_DATA_CONNECTION") })
	@GeneratedValue(strategy = AUTO, generator = "generator")
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 80)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "PROD_IP_ADDRESS", unique = false, nullable = true, insertable = true, updatable = true, length = 30)
	public String getProductionIpAddress() {
		return this.productionIpAddress;
	}

	public void setProductionIpAddress(String productionIpAddress) {
		this.productionIpAddress = productionIpAddress;
	}

	/**
	 * @return the testIpAddress
	 */
	@Column(name = "TEST_IP_ADDRESS", unique = false, nullable = true, insertable = true, updatable = true, length = 30)
	public String getTestIpAddress() {
		return testIpAddress;
	}

	/**
	 * @param testIpAddress the testIpAddress to set
	 */
	public void setTestIpAddress(String testIpAddress) {
		this.testIpAddress = testIpAddress;
	}

	@Column(name = "PRODUCTION_PORT", unique = false, nullable = true, insertable = true, updatable = true, precision = 5, scale = 0)
	public Short getProductionPort() {
		return this.productionPort;
	}

	public void setProductionPort(Short productionPort) {
		this.productionPort = productionPort;
	}

	@Column(name = "TEST_PORT", unique = false, nullable = true, insertable = true, updatable = true, precision = 5, scale = 0)
	public Short getTestPort() {
		return this.testPort;
	}

	public void setTestPort(Short testPort) {
		this.testPort = testPort;
	}

	@Column(name = "INTERFACE", unique = false, nullable = true, insertable = true, updatable = true, length = 40)
	public String getInterface_() {
		return this.interface_;
	}

	public void setInterface_(String interface_) {
		this.interface_ = interface_;
	}

	@Column(name = "PROCESS_MODE", unique = false, nullable = true, insertable = true, updatable = true, length = 10)
	public String getProcessMode() {
		return this.processMode;
	}

	public void setProcessMode(String processMode) {
		this.processMode = processMode;
	}

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY, targetEntity = Facility.class)
	@JoinTable(name = "DATA_CONN_FACILITY", joinColumns = { @JoinColumn(name = "ID_DATA_CONNECTION") }, inverseJoinColumns = { @JoinColumn(name = "ID_FACILITY") })
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public Set<Facility> getFacilities() {
		return this.facilities;
	}

	public void setFacilities(Set<Facility> facilities) {
		this.facilities = facilities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 43).append(getId()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		boolean ret = false;
		if (o instanceof DataConnection == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final DataConnection ag = (DataConnection) o;
			ret = new EqualsBuilder().append(getId(), ag.getId()).isEquals();
		}
		return ret;
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
