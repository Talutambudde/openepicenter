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
package com.hmsinc.epicenter.webapp.remoting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.DataConditioning;
import com.hmsinc.epicenter.model.analysis.DataRepresentation;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.DescriptiveAnalysisType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.webapp.dto.DataTypeDTO;
import com.hmsinc.epicenter.webapp.dto.KeyValueDTO;

/**
 * Returns various lists of metadata.
 * 
 * @author shade
 * @version $Id: MetadataService.java 1578 2008-04-25 13:20:48Z steve.kondik $
 */
@RemoteProxy(name = "MetadataService")
public class MetadataService extends AbstractRemoteService {

	/**
	 * Gets all available classifiers.
	 * 
	 * Sorts with non-beta first.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<Classifier> getClassifiers() {
		final List<Classifier> ret = analysisRepository.getClassifiers(); 
		Collections.sort(ret, new Comparator<Classifier>() {
			public int compare(Classifier o1, Classifier o2) {
				return new CompareToBuilder().append(o1.isBeta(), o2.isBeta()).append(o1.getName(), o2.getName())
						.toComparison();
			}
		});
		return ret;
	}

	/**
	 * Gets categories for a classifier.
	 * 
	 * @param classifierId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getCategories(final Long classifierId) {

		Validate.notNull(classifierId, "Classifier id must be specified.");
		final Classifier classifier = analysisRepository.load(classifierId, Classifier.class);
		Validate.notNull(classifier, "Unknown classifier: " + classifierId);

		final Set<KeyValueDTO> ret = new TreeSet<KeyValueDTO>();

		for (Classification c : classifier.getClassifications()) {
			ret.add(new KeyValueDTO(c.getId().toString(), c.getCategory()));
		}

		return prependAllCategory(ret, "Top 8 Categories");
	}

	/**
	 * Gets all available aggregation locations
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getLocations() {
		final Set<KeyValueDTO> ret = new LinkedHashSet<KeyValueDTO>();
		for (AnalysisLocation a : AnalysisLocation.values()) {
			ret.add(new KeyValueDTO(a.name(), "Totaled by " + a.getDisplayName()));
		}
		return ret;
	}

	/**
	 * Gets available conditioning methods.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getDataConditioningMethods() {
		final Set<KeyValueDTO> ret = new LinkedHashSet<KeyValueDTO>();
		for (DataConditioning a : DataConditioning.values()) {
			ret.add(new KeyValueDTO(a.name(), a.getDisplayName()));
		}
		return ret;
	}

	/**
	 * Gets available representation methods.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getDataRepresentationMethods() {
		final Set<KeyValueDTO> ret = new LinkedHashSet<KeyValueDTO>();
		for (DataRepresentation a : DataRepresentation.values()) {
			ret.add(new KeyValueDTO(a.name(), a.getDisplayName()));
		}
		return ret;
	}
	
	/**
	 * Gets available types of descriptive analysis.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getDescriptiveAnalysisTypes() {
		final Set<KeyValueDTO> ret = new TreeSet<KeyValueDTO>();
		for (DescriptiveAnalysisType a : DescriptiveAnalysisType.values()) {
			ret.add(new KeyValueDTO(a.name(), a.getDescription()));
		}
		return ret;
	}
	
	/**
	 * Gets all available age groups.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getAgeGroups() {

		final List<KeyValueDTO> dtos = new ArrayList<KeyValueDTO>();

		final List<AgeGroup> ageGroups = attributeRepository.getList(AgeGroup.class);
		Collections.sort(ageGroups);

		for (AgeGroup a : ageGroups) {
			final String range = (a.getMinAge() == null && a.getMaxAge() == null ? "" : " (" + a.getMinAge() + "-"
					+ a.getMaxAge() + ")");
			dtos.add(new KeyValueDTO(a.getId().toString(), a.getName() + range));
		}

		return prependAllCategory(dtos, "All Age Groups");
	}

	/**
	 * Gets all available genders.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getGenders() {
		final Set<KeyValueDTO> genders = new TreeSet<KeyValueDTO>();
		for (Gender a : attributeRepository.getList(Gender.class)) {
			genders.add(new KeyValueDTO(a.getId().toString(), a.getName()));
		}

		return prependAllCategory(genders, "All Genders");
	}

	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<DataTypeDTO> getDataTypes() {

		final Set<DataTypeDTO> dataTypes = new TreeSet<DataTypeDTO>();
		for (DataType dataType : analysisRepository.getList(DataType.class)) {
			logger.debug("datatype: {}", dataType);
			dataTypes.add(new DataTypeDTO(dataType));
		}

		return dataTypes;
	}
	
	
	
}
