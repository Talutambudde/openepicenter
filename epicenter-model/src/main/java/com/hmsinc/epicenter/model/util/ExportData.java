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
package com.hmsinc.epicenter.model.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB holder class for the import/export utility.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ExportData.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@XmlType(name = "Export", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "export", namespace = "http://epicenter.hmsinc.com/model")
public class ExportData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4301939326715917604L;

	@XmlElementWrapper(name = "entries", namespace = "http://epicenter.hmsinc.com/model")
	@XmlElement(name = "entry", namespace = "http://epicenter.hmsinc.com/model")
	private List<Object> data = new ArrayList<Object>();

	public ExportData() {

	}

	public ExportData(List<Object> data) {
		this.data = data;
	}

	/**
	 * @return the data
	 */
	public List<Object> getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(List<Object> data) {
		this.data = data;
	}

}
