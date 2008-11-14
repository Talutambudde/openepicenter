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

import static com.hmsinc.epicenter.webapp.util.GeometryUtils.getBoundingBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.directwebremoting.annotations.DataTransferObject;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.model.workflow.WorkflowState;
import com.hmsinc.epicenter.webapp.util.GeometryUtils;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.hmsinc.epicenter.webapp.util.Visibility;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

/**
 * @author shade
 * 
 */
@DataTransferObject
public class InvestigationDetailsDTO extends InvestigationDTO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1996142819099258764L;

	private final SortedSet<KeyValueDTO> users = new TreeSet<KeyValueDTO>();
	
	private final Organization organization;

	private final WorkflowState state;

	private final Envelope envelope;
	
	private final List<AnomalyDTO> anomalies = new ArrayList<AnomalyDTO>();
	
	private final Map<String, Double> currentValues = new HashMap<String, Double>();
		
	private final Set<String> localityLayers = new HashSet<String>();
	
	private final Set<GeographyDTO> localities = new HashSet<GeographyDTO>();
	
	public InvestigationDetailsDTO(Investigation investigation, EpiCenterUser user) {
		super(investigation);

		this.organization = investigation.getOrganization();
		this.state = investigation.getState();

		final Set<Point> locations = new HashSet<Point>();
		locations.add(organizationPoint);
		for (Event event : investigation.getEvents()) {
			
			if (event instanceof Anomaly) {
				final Anomaly a = (Anomaly)event;
				final Visibility v = SpatialSecurity.getVisibility(user, a.getGeography());
				if (Visibility.FULL.equals(v) 
					|| (Visibility.AGGREGATE_ONLY.equals(v) && !AnalysisLocation.FACILITY.equals(a.getTask().getLocation()))) {

					anomalies.add(new AnomalyDTO(a, user));
					locations.add(event.getGeography().getGeometry().getCentroid());
				}
			}
		}

		this.envelope = getBoundingBox(locations);
		
		for (EpiCenterUser u : investigation.getOrganization().getUsers()) {
			users.add(new KeyValueDTO(u.getId().toString(), u.getFirstName() + " " + u.getLastName()));
		}
		
		if (investigation.getLocalities() != null & investigation.getLocalities().size() > 0) {
			for (Geography locality : investigation.getLocalities()) {
				localities.add(new GeographyDTO(locality, Visibility.FULL));
				localityLayers.add(GeometryUtils.toLayerName(locality));
			}
		}
	}

	/**
	 * @return the organization
	 */
	public Organization getOrganization() {
		return organization;
	}

	/**
	 * @return the state
	 */
	public WorkflowState getState() {
		return state;
	}

	/**
	 * @return the envelope
	 */
	public Envelope getEnvelope() {
		return envelope;
	}

	/**
	 * @return the users
	 */
	public SortedSet<KeyValueDTO> getUsers() {
		return users;
	}

	/**
	 * @return the anomalies
	 */
	public List<AnomalyDTO> getAnomalies() {
		return anomalies;
	}

	/**
	 * @return the currentValues
	 */
	public Map<String, Double> getCurrentValues() {
		return currentValues;
	}

	/**
	 * @return the localityLayers
	 */
	public Set<String> getLocalityLayers() {
		return localityLayers;
	}

	/**
	 * @return the localities
	 */
	public Set<GeographyDTO> getLocalities() {
		return localities;
	}
		
}
