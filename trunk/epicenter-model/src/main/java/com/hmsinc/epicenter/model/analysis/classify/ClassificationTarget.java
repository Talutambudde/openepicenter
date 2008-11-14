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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.analysis.AnalysisObject;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.attribute.PatientClass;
import com.hmsinc.epicenter.model.health.Interaction;

/**
 * Describes a classification target.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ClassificationTarget.java 1089 2008-02-23 16:59:07Z steve.kondik $
 */
@Entity
@Table(name = "CLASSIFICATION_TARGET")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@XmlType(name = "ClassificationTarget", namespace = "http://epicenter.hmsinc.com/model")
@XmlRootElement(name = "target", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@NamedQuery(name = "getClassifiersForInteraction", query = "from ClassificationTarget t where t.interactionClass = :interactionClass and t.patientClass = :patientClass")
public class ClassificationTarget implements AnalysisObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3586929290837069499L;

	private Long id;

	@XmlIDREF
	@XmlElement(name = "classifier", namespace = "http://epicenter.hmsinc.com/model")
	private Classifier classifier;

	private DataType dataType;

	@XmlElement(name = "interaction-class", namespace = "http://epicenter.hmsinc.com/model")
	private Class<? extends Interaction> interactionClass;

	@XmlElement(name = "patient-class", namespace = "http://epicenter.hmsinc.com/model")
	private PatientClass patientClass;

	@XmlElement(name = "property", namespace = "http://epicenter.hmsinc.com/model")
	private String propertyName;

	/**
	 * @return the id
	 */
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_CLASSIFICATION_TARGET") })
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
	@ForeignKey(name = "FK_CLASSIFICATION_TARGET_1")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
	 * @return the dataType
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_DATA_TYPE", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_CLASSIFICATION_TARGET_2")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public DataType getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the interactionClass
	 */
	@Column(name = "INTERACTION_CLASS", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
	public Class<? extends Interaction> getInteractionClass() {
		return interactionClass;
	}

	/**
	 * @param interactionClass
	 *            the interactionClass to set
	 */
	public void setInteractionClass(Class<? extends Interaction> interactionClass) {
		this.interactionClass = interactionClass;
	}

	/**
	 * @return the patientClass
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PATIENT_CLASS", unique = false, nullable = false, insertable = true, updatable = true)
	@ForeignKey(name = "FK_CLASSIFICATION_TARGET_3")
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	public PatientClass getPatientClass() {
		return patientClass;
	}

	/**
	 * @param patientClass
	 *            the patientClass to set
	 */
	public void setPatientClass(PatientClass patientClass) {
		this.patientClass = patientClass;
	}

	/**
	 * @return the propertyName
	 */
	@Column(name = "PROPERTY_NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 40)
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName
	 *            the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * "Magic" method for JAXB to map the cyclical reference to the enclosing
	 * element.
	 * 
	 * @param u
	 * @param parent
	 */
	public void afterUnmarshal(Unmarshaller u, Object parent) {
		if (parent instanceof Classifier) {
			this.classifier = (Classifier) parent;
		} else if (parent instanceof DataType) {
			this.dataType = (DataType) parent;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("classifier", classifier).append("dataType", dataType)
			.append("interactionClass", interactionClass).append("patientClass", patientClass.getName())
			.append("propertyName", propertyName).toString();
	}

}
