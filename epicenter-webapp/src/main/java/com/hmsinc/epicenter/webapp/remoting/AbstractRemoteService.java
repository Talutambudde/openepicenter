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

import static com.hmsinc.epicenter.util.DateTimeUtils.isToday;
import static com.hmsinc.epicenter.util.DateTimeUtils.toEndOfDay;
import static com.hmsinc.epicenter.util.DateTimeUtils.toStartOfDay;
import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.checkAggregateOnlyAccess;
import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.checkPermission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.attribute.AgeGroup;
import com.hmsinc.epicenter.model.attribute.Attribute;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.attribute.Gender;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.PermissionRepository;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;
import com.hmsinc.epicenter.webapp.dto.KeyValueDTO;

/**
 * Provides base methods for remote services.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AbstractRemoteService.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class AbstractRemoteService {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected static final URLCodec codec = new URLCodec();
	
	@Resource
	protected AnalysisRepository analysisRepository;
	
	@Resource
	protected AttributeRepository attributeRepository;

	@Resource
	protected GeographyRepository geographyRepository;

	@Resource
	protected PermissionRepository permissionRepository;


	/**
	 * Gets the current user's principal.
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	protected EpiCenterUser getPrincipal() {

		EpiCenterUser userPrincipal = null;
		final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new AccessDeniedException("No credentials found!");
		}

		Object principal = auth.getPrincipal();
		if (principal != null && principal instanceof EpiCenterUser) {

			// Reload it, since we're probably in a different thread.
			userPrincipal = permissionRepository.load(((EpiCenterUser) principal).getId(), EpiCenterUser.class);
	
		}

		return userPrincipal;
	}

	/**
	 * Converts an AnalysisParametersDTO into AnalysisParameters.
	 * 
	 * @param <T>
	 * @param paramsDTO
	 * @return
	 */
	@Transactional(readOnly = true)
	protected <T extends Geography> AnalysisParameters convertParameters(final AnalysisParametersDTO paramsDTO) {

		Validate.notNull(paramsDTO, "No parameters were given!");

		logger.debug(paramsDTO.toString());

		// Make sure we have valid, required parameters:
		Validate.notNull(paramsDTO.getStart(), "Start date must be specified");
		Validate.notNull(paramsDTO.getEnd(), "End date must be specified");
		Validate.notNull(paramsDTO.getLocation(), "Patient location must be specified");
		
		//Validate.isTrue(paramsDTO.getCategory() != null || paramsDTO.getCategory() != null, "Classifier or category must be specified.");
		
		Geography geography = null;
		if (paramsDTO.getGeography() != null) {
			geography = geographyRepository.load(paramsDTO.getGeography(), Geography.class);
			Validate.notNull(geography, "Invalid geography.");

			// Verify access
			checkPermission(getPrincipal(), geography);
			
			if (AnalysisLocation.FACILITY.equals(paramsDTO.getLocation())) {
				checkAggregateOnlyAccess(getPrincipal(), geography);
			}
			
			// Might need this at some point if regions get HUGE.
			//params.setSecondaryFilter(getPrincipal().getVisibleRegion());
		}
		
			
		DateTime start = paramsDTO.getStart();
		DateTime end = paramsDTO.getEnd();
		
		if (paramsDTO.isFixDates()) {

			// Set the startDate to 00:00:00
			start = toStartOfDay(paramsDTO.getStart());

			// Set the endDate to 23:59:59
			end = toEndOfDay(paramsDTO.getEnd());
			
		} 

		// Validate the dates
		Validate.isTrue(start.isBefore(end) || start.equals(end), "Start date must be before end date");
		final AnalysisParameters params = new AnalysisParameters(start, end);
		params.setContainer(geography);

		// Look up the classifiers, default to all categories
		if (paramsDTO.getClassifier() != null && !"TOTAL".equals(paramsDTO.getClassifier())) {
			final Classifier c = analysisRepository.load(Long.valueOf(paramsDTO.getClassifier()), Classifier.class);
			Validate.notNull(c, "Invalid classifier " + paramsDTO.getClassifier());
			if (paramsDTO.getCategory() == null || paramsDTO.getCategory().equals("ALL")) {
				params.setClassifications(c.getClassifications());
			} else {
				final Set<Classification> classifications = new HashSet<Classification>();

				for (Long id : splitStringToIdList(paramsDTO.getCategory())) {
					classifications.add(analysisRepository.load(id, Classification.class));
				}

				params.setClassifications(classifications);
			}
			Validate.isTrue(params.getClassifications() != null && params.getClassifications().size() > 0, "No classifications were found!");
		}

		// Attributes
		if (paramsDTO.getGender() != null && !paramsDTO.getGender().equalsIgnoreCase("all")) {
			final Collection<Long> idList = splitStringToIdList(paramsDTO.getGender());
			if (idList != null) {
				for (Long id : idList) {
					params.getAttributes().add(attributeRepository.load(id, Gender.class));
				}
			}
		}

		if (paramsDTO.getAgeGroup() != null && !paramsDTO.getAgeGroup().equalsIgnoreCase("all")) {
			final Collection<Long> idList = splitStringToIdList(paramsDTO.getAgeGroup());
			if (idList != null) {
				for (Long id : idList) {
					params.getAttributes().add(attributeRepository.load(id, AgeGroup.class));
				}
			}
		}

		if (paramsDTO.getAttributes() != null) {
			final Collection<Long> idList = splitStringToIdList(paramsDTO.getAttributes());
			if (idList != null) {
				for (Long id : idList) {
					params.getAttributes().add(attributeRepository.load(id, Attribute.class));
				}
			}
		}
		
		params.setLocation(paramsDTO.getLocation());

		if (paramsDTO.getDatatype() != null) {
			final DataType dataType = analysisRepository.load(paramsDTO.getDatatype(), DataType.class);
			Validate.notNull(dataType, "Invalid datatype: " + paramsDTO.getDatatype());
			params.setDataType(dataType);
		}
		
		if (paramsDTO.getRepresentation() != null) {
			params.setDataRepresentation(paramsDTO.getRepresentation());
		}
		
		if (paramsDTO.getConditioning() != null) {
			params.setDataConditioning(paramsDTO.getConditioning());
		}
		
		return params;
	}

	/**
	 * @param endDate
	 * @return
	 */
	protected static DateTime fixEndDate(final DateTime endDate) {
		return isToday(endDate) ? new DateTime() : endDate;
	}

	/**
	 * Prepends an "ALL" category to a list.
	 * 
	 * @param values
	 * @return
	 */
	protected static Collection<KeyValueDTO> prependAllCategory(final Collection<KeyValueDTO> values, final String text) {
		final List<KeyValueDTO> dto = new ArrayList<KeyValueDTO>();
		dto.add(new KeyValueDTO("ALL", text));
		dto.addAll(values);
		return dto;
	}

	/**
	 * Prepends a "NONE" category to a list.
	 * 
	 * @param values
	 * @return
	 */
	protected static Collection<KeyValueDTO> prependNoneCategory(final Collection<KeyValueDTO> values, final String text) {
		final List<KeyValueDTO> dto = new ArrayList<KeyValueDTO>();
		dto.add(new KeyValueDTO(null, text));
		dto.addAll(values);
		return dto;
	}

	/**
	 * @param value
	 * @return
	 */
	protected static Collection<Long> splitStringToIdList(final String idList) {
		Set<Long> ret = null;
		try {
			
			final String value = StringUtils.trimToNull(codec.decode(idList));
			if (value != null) {
				ret = new HashSet<Long>();
				for (String v : value.split(",")) {
					if (StringUtils.isNumeric(v)) {
						ret.add(Long.valueOf(v));
					}
				}
			}
		} catch (DecoderException e) {
			throw new IllegalArgumentException(e);
		}
		return ret;
	}
}
