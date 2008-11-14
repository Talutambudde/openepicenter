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
package com.hmsinc.epicenter.model.analysis.classify;

import static javax.persistence.GenerationType.AUTO;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.hmsinc.epicenter.model.analysis.AnalysisObject;

/**
 * Classifier metadata, used by the ClassificationService.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Classifier.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
@XmlType(name="Classifier", namespace = "http://epicenter.hmsinc.com/model")
@XmlRootElement(name = "classifier", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@Entity
@Table(name = "CLASSIFIER", uniqueConstraints = { @UniqueConstraint(columnNames={"NAME", "VERSION"})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { @NamedQuery(name = "getClassifiers", query = "from Classifier c where c.enabled = true"),
	@NamedQuery(name = "getClassifierByName", query = "from Classifier c where c.name = :name order by c.version desc"),
	@NamedQuery(name = "getClassifierByNameAndVersion", query = "from Classifier where name = :name and version = :version") })
public class Classifier implements AnalysisObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389635224477843583L;

	private Long id;

	@XmlID
	@XmlAttribute
	private String name;

	private String version;

	private String description;

	private SortedSet<Classification> classifications = new TreeSet<Classification>();
	
	@XmlAttribute(required=true)
	private String resource;
	
	@XmlAttribute
	private boolean enabled = true;
	
	@XmlAttribute
	private boolean beta = false;
	
	@XmlElementWrapper(name = "targets", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "target", namespace = "http://epicenter.hmsinc.com/model")
	private List<ClassificationTarget> targets = new ArrayList<ClassificationTarget>();
	
	/**
	 * 
	 */
	public Classifier() {
		super();
	}

	/**
	 * @param name
	 * @param version
	 */
	public Classifier(String name, String version) {
		super();
		this.name = name;
		this.version = version;
	}

	/**
	 * @return the id
	 */
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_CLASSIFIER") })
	@Id
	@GeneratedValue(strategy = AUTO, generator = "generator")
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
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
	 * @return the name
	 */
	@Column(name = "NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 40)
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the version
	 */
	@Column(name = "VERSION", unique = false, nullable = false, insertable = true, updatable = true, length = 15)
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the enabled
	 */
	@Column(name = "ENABLED", unique = false, nullable = false, insertable = true, updatable = true)
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @return the beta
	 */
	@Column(name = "BETA", unique = false, nullable = false, insertable = true, updatable = true)
	public boolean isBeta() {
		return beta;
	}

	/**
	 * @param beta the beta to set
	 */
	public void setBeta(boolean beta) {
		this.beta = beta;
	}

	/**
	 * @return the targets
	 */
	@Fetch(FetchMode.JOIN)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "classifier")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public List<ClassificationTarget> getTargets() {
		return targets;
	}

	/**
	 * @param targets the targets to set
	 */
	public void setTargets(List<ClassificationTarget> targets) {
		this.targets = targets;
	}

	/**
	 * @return the classifications
	 */
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, mappedBy = "classifier")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@Fetch(FetchMode.JOIN)
	@Sort(type = SortType.NATURAL)
	public SortedSet<Classification> getClassifications() {
		return classifications;
	}

	/**
	 * @param classifications
	 *            the classifications to set
	 */
	public void setClassifications(SortedSet<Classification> classifications) {
		this.classifications = classifications;
	}

	/**
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the resource
	 */
	@Column(name = "RESOURCE_LOCATION", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
	public String getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(String resource) {
		this.resource = resource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(13, 7).append(getVersion()).append(getName()).append(getTargets()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		boolean ret = false;
		if (o instanceof Classifier == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {

			final Classifier ag = (Classifier) o;
			ret = new EqualsBuilder().append(getVersion(), ag.getVersion()).append(getName(), ag.getName()).append(getTargets(),
					ag.getTargets()).isEquals() && (ag.getClassifications().containsAll(getClassifications()));

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
		return new ToStringBuilder(this).append("id", id).append("version", version).append("name", name).append("targets", targets)
				.append("classifications", classifications).toString();
	}
}
