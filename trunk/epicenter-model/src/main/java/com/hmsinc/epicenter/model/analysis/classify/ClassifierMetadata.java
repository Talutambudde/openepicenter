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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.hmsinc.epicenter.model.analysis.DataType;

/**
 * Holds classifier metadata - used for default configuration.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ClassifierMetadata.java 913 2008-02-08 21:08:02Z steve.kondik $
 */
@XmlType(name = "ClassifierMetadata", namespace = "http://epicenter.hmsinc.com/model")
@XmlRootElement(name = "classifier-metadata", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
public class ClassifierMetadata implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2580854133045491888L;

	@XmlElementWrapper(name = "classifiers", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "classifier", namespace = "http://epicenter.hmsinc.com/model")
	private List<Classifier> classifiers = new ArrayList<Classifier>();

	@XmlElementWrapper(name = "data-types", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "data-type", namespace = "http://epicenter.hmsinc.com/model")
	private List<DataType> dataTypes = new ArrayList<DataType>();
	
	/**
	 * @return the classifiers
	 */
	public List<Classifier> getClassifiers() {
		return classifiers;
	}

	/**
	 * @param classifiers
	 *            the classifiers to set
	 */
	public void setClassifiers(List<Classifier> classifiers) {
		this.classifiers = classifiers;
	}

	/**
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * @param dataTypes the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

}
