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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.health.CodedVisit;
import com.hmsinc.epicenter.model.health.HealthRepository;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.health.Patient;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.hmsinc.epicenter.webapp.dto.AnalysisParametersDTO;
import com.hmsinc.epicenter.webapp.dto.CasesDTO;
import com.hmsinc.epicenter.webapp.dto.CasesDetailDTO;
import com.hmsinc.epicenter.webapp.dto.ListView;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.hmsinc.epicenter.webapp.util.Visibility;

/**
 * @author shade
 * @version $Id: PatientService.java 1822 2008-07-11 16:09:51Z steve.kondik $
 */
@RemoteProxy(name = "PatientService")
public class PatientService extends AbstractRemoteService {

	@Resource
	private ProviderRepository providerRepository;

	@Resource
	private HealthRepository healthRepository;

	/**
	 * Gets a line listing of visits using optional paging.
	 * 
	 * @param <T>
	 * @param classifierId
	 * @param classifierCategory
	 * @param start
	 * @param end
	 * @param geoType
	 * @param geoId
	 * @param patientLocation
	 * @param pageSize
	 * @param pageNum
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public <T extends Geography> ListView<CasesDTO> getCases(final AnalysisParametersDTO paramsDTO,
			final Integer offset, final Integer numRows) {

		final AnalysisParameters params = convertParameters(paramsDTO);

		SpatialSecurity.checkAggregateOnlyAccess(getPrincipal(), params.getContainer());

		logger.debug("Getting cases for: {}  [offset: {}, numRows: {}]", new Object[] { params, offset, numRows });

		// Hibernate should cache this
		final Long total = analysisRepository.getCasesCount(params);

		final Long offsetL = offset == null ? null : Long.valueOf(offset.longValue());
		final Long numRowsL = numRows == null ? null : Long.valueOf(numRows.longValue());

		final List<? extends Interaction> interactions = analysisRepository.getCases(params, offsetL, numRowsL);

		logger.debug("Got {} cases.", interactions.size());

		// Build the ListView
		final ListView<CasesDTO> cases = new ListView<CasesDTO>(total.intValue());
		for (final Interaction a : interactions) {

			// Only copy the requested classifications into the view
			final Set<String> clz = new HashSet<String>();

			for (Classification cc : a.getClassifications()) {
				if (params.getClassifications() == null || params.getClassifications().size() == 0
						|| params.getClassifications().contains(cc)) {
					clz.add(cc.getCategory());
				}
			}

			if (params.getClassifications() == null || params.getClassifications().size() == 0 || clz.size() > 0) {
				cases.getItems().add(filterFacility(new CasesDTO(a, clz), a));
			}
			// Evict from the cache since we're done.
			analysisRepository.evict(a);

		}
		return cases;
	}
	
	/**
	 * @param facilityId
	 * @param patientId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public List<CasesDetailDTO> getPatientHistory(final Long facilityId, final String patientId,
			final String visitNumber) {

		Validate.notNull(facilityId, "Facility id must be specified.");
		Validate.notNull(patientId, "Patient id must be specified.");

		final Facility facility = providerRepository.load(facilityId, Facility.class);
		Validate.notNull(facility, "Invalid facility: " + facilityId);

		final Zipcode facilityZip = geographyRepository.getGeography(facility.getZipcode(), Zipcode.class);

		final Patient patient = healthRepository.getPatient(patientId, facility);

		final List<CasesDetailDTO> history = new ArrayList<CasesDetailDTO>();

		if (facilityZip == null) {
			logger.error("Invalid facility zipcode: {}  [{}]", facility.getZipcode(), facility);
		} else if (patient == null) {
			logger.error("Invalid patient: {}", patientId);
		} else {

			if (Visibility.FULL.equals(SpatialSecurity.getVisibility(getPrincipal(), facilityZip))) {
				for (Interaction i : patient.getInteractions()) {

					final Zipcode iZip = geographyRepository.getGeography(i.getPatientDetail().getZipcode(), Zipcode.class);
					if (iZip != null 
							&& Visibility.FULL.equals(SpatialSecurity.getVisibility(getPrincipal(), iZip))) {

						if (visitNumber == null) {
							history.add((CasesDetailDTO)filterFacility(new CasesDetailDTO(i), i));
						} else {
							if (i instanceof CodedVisit) {
								final CodedVisit cv = (CodedVisit) i;
								if (visitNumber.equals(cv.getVisitNumber())) {
									history.add((CasesDetailDTO)filterFacility(new CasesDetailDTO(i), i));
								}
							}
						}
					}
				}
			}
		}
		return history;
	}

	/**
	 * Blanks the facility name for interactions outside the user's visible region.
	 * 
	 * @param dto
	 * @param i
	 * @return
	 */
	private CasesDTO filterFacility(final CasesDTO dto, final Interaction i) {
		
		final Facility f = i.getPatient().getFacility();
		if (f != null && f.getZipcode() != null) {
			final Zipcode facilityZip = geographyRepository.getGeography(i.getPatient().getFacility().getZipcode(), Zipcode.class);
		
			if (facilityZip != null) {
				if (!SpatialSecurity.isGeographyAccessible(getPrincipal(), facilityZip)) {
					dto.setFacilityId(null);
					dto.setFacilityName("(outside visible region)");
				}
			}
		}
		return dto;
		
	}
	
}
