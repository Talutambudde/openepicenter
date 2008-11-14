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
package com.hmsinc.epicenter.webapp.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.directwebremoting.annotations.DataTransferObject;

import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.ClassificationTarget;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;

/**
 * @author shade
 * @version $Id: DataTypeDTO.java 1306 2008-03-14 14:59:57Z steve.kondik $
 */
@DataTransferObject
public class DataTypeDTO implements Serializable, Comparable<DataTypeDTO> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4605989442911496881L;

	private final Long id;

	private final String name;

	private final Set<KeyValueDTO> classifiers = new LinkedHashSet<KeyValueDTO>();

	private final Map<Long, Set<KeyValueDTO>> categories = new HashMap<Long, Set<KeyValueDTO>>();

	private boolean allClassifiersBeta = true;
	
	public DataTypeDTO(final DataType dataType) {

		this.id = dataType.getId();
		this.name = dataType.getName();
		classifiers.add(new KeyValueDTO("TOTAL", "Total Counts"));

		final List<Classifier> ccs = new ArrayList<Classifier>();
		for (ClassificationTarget target : dataType.getTargets()) {
			ccs.add(target.getClassifier());
		}

		Collections.sort(ccs, new Comparator<Classifier>() {
			public int compare(Classifier o1, Classifier o2) {
				return new CompareToBuilder().append(o1.isBeta(), o2.isBeta()).append(o1.getName(), o2.getName())
						.toComparison();
			}
		});

		for (Classifier cc : ccs) {

			if (cc.isEnabled()) {

				classifiers.add(new KeyValueDTO(cc.getId().toString(), cc.getName(), cc.getName()
						+ (cc.isBeta() ? " (BETA)" : "")));

				final Set<Classification> cls = cc.getClassifications();
				categories.put(cc.getId(), new TreeSet<KeyValueDTO>());
				for (Classification c : cls) {
					categories.get(cc.getId()).add(new KeyValueDTO(c.getId().toString(), c.getCategory()));
				}
				
				if (!cc.isBeta()) {
					allClassifiersBeta = false;
				}
			}
		}
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the classifiers
	 */
	public Set<KeyValueDTO> getClassifiers() {
		return classifiers;
	}

	/**
	 * @return the categories
	 */
	public Map<Long, Set<KeyValueDTO>> getCategories() {
		return categories;
	}

	
	/**
	 * @return the allClassifiersBeta
	 */
	public boolean isAllClassifiersBeta() {
		return allClassifiersBeta;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(DataTypeDTO o) {
		return new CompareToBuilder().append(allClassifiersBeta, o.isAllClassifiersBeta()).append(name, o.getName()).append(id, o.getId()).toComparison();
	}

}
