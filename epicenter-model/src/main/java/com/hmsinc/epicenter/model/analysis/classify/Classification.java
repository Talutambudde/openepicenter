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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.analysis.AnalysisObject;
import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.hmsinc.epicenter.model.health.Interaction;

/**
 * A category of a Classifier.
 * 
 * This class is designed to be used with both a database and an XML descriptor.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Classification.java 219 2007-07-17 14:37:39Z steve.kondik $
 */
@Entity
@Table(name = "CLASSIFICATION", uniqueConstraints = { @UniqueConstraint(columnNames={"ID_CLASSIFIER", "CATEGORY"})})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries( { @NamedQuery(name = "getClassifications", query = "from Classification c where c.classifier = :classifier and c.category in (:categories)") })
@XmlType(name = "Classification", namespace = "http://epicenter.hmsinc.com/model")
@XmlRootElement(name = "classification", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class Classification implements AnalysisObject, QueryableAttribute, Comparable<Classification> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2211596594413609529L;

	@XmlAttribute(required = true)
	private Long id;

	private Classifier classifier;

	@XmlAttribute(required = true)
	private String category;

	private String description;
	
	private Set<Interaction> interactions = new HashSet<Interaction>();

	/**
	 * 
	 */
	public Classification() {
		super();
	}

	/**
	 * @param classifier
	 * @param category
	 */
	public Classification(Classifier classifier, String category) {
		super();
		this.classifier = classifier;
		this.category = category;
	}

	/**
	 * @return the id
	 */
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_CLASSIFICATION") })
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
	 * @return the classifier
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CLASSIFIER", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_CLASSIFICATION_1")
	public Classifier getClassifier() {
		return classifier;
	}

	/**
	 * @param classifier
	 *            the classifier to set
	 */
	public void setClassifier(Classifier classifier) {
		this.classifier = classifier;
	}

	/**
	 * @return the category
	 */
	@Column(name = "CATEGORY", unique = false, nullable = false, insertable = true, updatable = true, length = 40)
	public String getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the description
	 */
	@Column(name = "DESCRIPTION", unique = false, nullable = true, insertable = true, updatable = true, length = 400)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the interactions
	 */
	@ManyToMany(mappedBy = "classifications", targetEntity = Interaction.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ForeignKey(name = "FK_HEALTH_ENCOUNT_CLASS_2")
	public Set<Interaction> getInteractions() {
		return interactions;
	}

	/**
	 * @param interactions
	 *            the interactions to set
	 */
	public void setInteractions(Set<Interaction> interactions) {
		this.interactions = interactions;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.attribute.Attribute#getName()
	 */
	@Transient
	public String getName() {
		return this.category;
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final HashCodeBuilder hb = new HashCodeBuilder(13, 5).append(getCategory());
		if (getClassifier() != null) {
			hb.append(getClassifier().getName()).append(getClassifier().getVersion());
		}
		return hb.toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		boolean ret = false;
		if (o instanceof String && getCategory().equals(o)) {
			ret = true;
		} else if (o instanceof Classification == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final Classification ag = (Classification) o;
			final EqualsBuilder eb = new EqualsBuilder().append(getCategory(), ag.getCategory());
			if (getClassifier() != null) {
				eb.append(getClassifier().getName(), ag.getClassifier().getName()).append(getClassifier().getVersion(), ag.getClassifier().getVersion());
			}
			ret = eb.isEquals();
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Classification c) {
		return new CompareToBuilder().append(getCategory(), c.getCategory()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("id", id).append("classifier", (classifier == null ? null : classifier.getName() + ":" + classifier.getVersion()))
				.append("category", category).toString();
	}
}
