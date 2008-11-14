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

import static com.hmsinc.epicenter.util.DateTimeUtils.formatDurationDays;
import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.checkPermission;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.directwebremoting.ScriptSession;
import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.proxy.dwr.Util;
import org.joda.time.DateTime;
import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.surveillance.SurveillanceRepository;
import com.hmsinc.epicenter.model.workflow.Activity;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.model.workflow.Workflow;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;
import com.hmsinc.epicenter.model.workflow.WorkflowState;
import com.hmsinc.epicenter.model.workflow.WorkflowStateType;
import com.hmsinc.epicenter.model.workflow.WorkflowTransition;
import com.hmsinc.epicenter.util.DateTimeUtils;
import com.hmsinc.epicenter.webapp.data.QueryService;
import com.hmsinc.epicenter.webapp.dto.ActivityDTO;
import com.hmsinc.epicenter.webapp.dto.InvestigationDTO;
import com.hmsinc.epicenter.webapp.dto.InvestigationDetailsDTO;
import com.hmsinc.epicenter.webapp.dto.KeyValueDTO;
import com.hmsinc.epicenter.webapp.dto.ListView;
import com.hmsinc.epicenter.webapp.util.GeometryUtils;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * TODO: Redo access controls with Acegi aspects.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:WorkflowService.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@RemoteProxy(name = "WorkflowService")
public class WorkflowService extends AbstractRemoteService {

	@Resource
	private QueryService queryService;
	
	@Resource
	private SurveillanceRepository surveillanceRepository;
	
	@Resource
	private WorkflowRepository workflowRepository;
	
	@Resource(name = "velocityEngine")
	private VelocityEngine velocityEngine;

	@Resource
	private Properties activityLogTemplates;

	@Resource
	private AntiSamy antiSamy;
	
	@Resource
	private Policy antiSamyPolicy;
	
	/**
	 * Gets all users in the current users Organizations.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Set<KeyValueDTO> getUsersInOrganization() {

		// Merge users from all assigned orgs
		final Set<EpiCenterUser> users = new HashSet<EpiCenterUser>();
		for (Organization org : getPrincipal().getOrganizations()) {
			users.addAll(org.getUsers());
		}

		// Copy into DTO
		final Set<KeyValueDTO> dto = new TreeSet<KeyValueDTO>();
		for (EpiCenterUser user : users) {
			dto.add(new KeyValueDTO(user.getId().toString(), user.getUsername()));
		}

		return dto;
	}

	/**
	 * Gets a list of available workflows.
	 * 
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getWorkflows() {
		final Set<KeyValueDTO> workflows = new TreeSet<KeyValueDTO>();
		for (Workflow workflow : workflowRepository.getList(Workflow.class)) {
			workflows.add(new KeyValueDTO(workflow.getId().toString(), workflow.getName()));
		}
		return workflows;
	}

	/**
	 * @param investigationId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public ListView<ActivityDTO> getInvestigationActivities(Long investigationId) {

		Validate.notNull(investigationId, "Investigation id must be specified.");

		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id " + investigationId);
		checkInvestigationAccess(investigation);

		final ListView<ActivityDTO> activitiesView = new ListView<ActivityDTO>(investigation.getActivities().size());
		for (Activity activity : investigation.getActivities()) {
			activitiesView.getItems().add(new ActivityDTO(activity));
		}

		Collections.sort(activitiesView.getItems());

		return activitiesView;
	}

	/**
	 * @param showAll
	 * @param startDate
	 * @param endDate
	 * @param geographyId
	 * @param offset
	 * @param numRows
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public ListView<InvestigationDTO> getInvestigations(boolean showAll, DateTime startDate, DateTime endDate, Long geographyId, Integer offset,
			Integer numRows) {

		Validate.notNull(startDate, "Start date must be specified.");
		Validate.notNull(endDate, "End date must be specified.");
		
		logger.debug("getting investigations  start: {}, end: {}", startDate, endDate);
		
		final Geometry geo;
		if (geographyId == null) {
			geo = null;
		} else {
			final Geography g = geographyRepository.load(geographyId, Geography.class);
			Validate.notNull(g, "Invalid geography: " + geographyId);
			checkPermission(getPrincipal(), g);
			geo = g.getGeometry();
		}
					
		// Get the investigations created by any organizations in the user's
		// visible region.
		final DateTime adjustedStart = DateTimeUtils.toStartOfDay(startDate);
		final DateTime adjustedEnd = DateTimeUtils.toEndOfDay(endDate);
		final List<Investigation> investigations = workflowRepository.getInvestigations(adjustedStart, adjustedEnd, geo, getVisibleOrganizations(getPrincipal()), showAll, offset, numRows);

		final Set<Point> locations = new HashSet<Point>();
		final ListView<InvestigationDTO> listview = new ListView<InvestigationDTO>(workflowRepository.getInvestigationCount(startDate, endDate, geo, getVisibleOrganizations(getPrincipal()), showAll));

		for (Investigation investigation : investigations) {
			
			final InvestigationDTO dto = new InvestigationDTO(investigation);
			listview.getItems().add(dto);
			
			// I don't like this:
			locations.add(dto.getOrganizationPoint());
			
			/*
			for (Event event : investigation.getEvents()) {
				locations.add(event.getGeography().getGeometry().getCentroid());
			}
			*/
		}

		listview.getAttributes().put("bbox", GeometryUtils.getBoundingBox(locations));

		return listview;
	}

	/**
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public ListView<InvestigationDTO> getActiveInvestigations(Integer offset, Integer numRows) {
	
		final List<Investigation> investigations = workflowRepository.getInvestigations(null, null,
				null, getVisibleOrganizations(getPrincipal()), false, offset, numRows);
		
		final ListView<InvestigationDTO> listview = new ListView<InvestigationDTO>(workflowRepository.getInvestigationCount(null, null, null, getPrincipal().getOrganizations(), false));
		for (Investigation investigation : investigations) {
			listview.getItems().add(new InvestigationDTO(investigation));
		}
		
		return listview;
	}
	
	/**
	 * @param description
	 * @param anomalies
	 * @return the id of the new Investigation
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public Long createInvestigation(final String description, final List<Long> anomalies, final List<Long> localities) {

		Validate.notNull(description, "Description must be provided.");
	//	Validate.notEmpty(localities, "A list of localities must be provided");
		
		final Organization primaryOrg = getPrincipal().getOrganizations().iterator().next();
		final WorkflowState initialState = workflowRepository.getInitalState(primaryOrg.getWorkflow());
		
		/*
		final List<Geography> geoLocalities = geographyRepository.getReferences(localities, Geography.class);
		for (Geography gl : geoLocalities) {
			checkPermission(getPrincipal(), gl);
		}
		*/
		
		final Investigation investigation = new Investigation(description, initialState, primaryOrg, getPrincipal());
	//	investigation.getLocalities().addAll(geoLocalities);
	//	checkPermission(getPrincipal(), investigation.getLocality());
		
		final Activity activity = logTemplate("create", investigation, null);
		investigation.getActivities().add(activity);
		
		if (anomalies != null) {
			for (Long anomalyId : anomalies) {

				Validate.notNull(anomalyId, "Event id must be specified.");
				final Anomaly anomaly = surveillanceRepository.load(anomalyId, Anomaly.class);
				Validate.notNull(anomaly, "Invalid anomaly id: " + anomalyId);
				checkPermission(getPrincipal(), anomaly);

				investigation.getEvents().add(anomaly);

				final Map<String, Object> items = new HashMap<String, Object>();
				items.put("event", anomaly);

				final Activity associate = logTemplate("associate-event", investigation, items);
				investigation.getActivities().add(associate);

			}
		}
		
		workflowRepository.save(investigation);

		return investigation.getId();
	}

	/**
	 * @param investigationId
	 * @return the InvestigationDetails data transfer object.
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public InvestigationDetailsDTO getInvestigationDetail(Long investigationId) {
		Validate.notNull(investigationId);

		logger.debug("Loading investigation id: " + investigationId);
		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id " + investigation);

		checkInvestigationAccess(investigation);

		final InvestigationDetailsDTO dto = new InvestigationDetailsDTO(investigation, getPrincipal());
		
		for (Event event : investigation.getEvents()) {
			if (event instanceof Anomaly) {
				final Anomaly anomaly = (Anomaly)event;
				final double current = queryService.getCurrentValueForAnomaly(anomaly);
				logger.info("Anomaly id: {}  Current value: {}", anomaly.getId(), current);
				dto.getCurrentValues().put(anomaly.getId().toString(), current);
			}
		}
		
		WebContextFactory.get().getScriptSession().setAttribute("investigation", investigation.getId());
		
		return dto;
	}

	@Secured("ROLE_USER")
	@Transactional
	//@RemoteMethod
	public void updateInvestigationLocality(Long investigationId, List<Long> localityIds) {
		
		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id " + investigation);

		checkInvestigationAccess(investigation);
		
		final Set<Geography> localities = new HashSet<Geography>();
		for (Long localityId : localityIds) {
			final Geography geography = geographyRepository.load(localityId, Geography.class);
			Validate.notNull(geography, "Invalid geography: " + localityId);
			checkPermission(getPrincipal(), geography);
			localities.add(geography);
		}
		
		investigation.setLocalities(localities);
		
		workflowRepository.save(investigation);
		
		// TODO: Log.
		
	}
	
	/**
	 * Adds an event to an investigation.
	 * 
	 * @param investigationId
	 * @param eventId
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public Collection<InvestigationDTO> addEventToInvestigation(Long investigationId, Long eventId) {

		Validate.notNull(investigationId, "Investigation id must be specified.");
		Validate.notNull(eventId, "Event id must be specified");

		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id: " + investigationId);
		checkInvestigationAccess(investigation);

		final Event event = workflowRepository.load(eventId, Event.class);
		Validate.notNull(event, "Invalid event id: " + eventId);
		checkPermission(getPrincipal(), event);

		investigation.getEvents().add(event);

		final Map<String, Object> items = new HashMap<String, Object>();
		items.put("event", event);

		final Activity activity = logTemplate("associate-event", investigation, items);
		investigation.getActivities().add(activity);

		workflowRepository.save(investigation);
		fireCometUpdate(investigation);
		
		final Set<InvestigationDTO> ins = new TreeSet<InvestigationDTO>();
		ins.add(new InvestigationDTO(investigation));
		
		for (Investigation inv : event.getInvestigations()) {
			ins.add(new InvestigationDTO(inv));
		}
		return ins;
	}
	
	/**
	 * @param investigationId
	 * @param eventId
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public void removeEventFromInvestigation(Long investigationId, Long eventId) {
		
		Validate.notNull(investigationId, "Investigation id must be specified.");
		Validate.notNull(eventId, "Event id must be specified");

		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id: " + investigationId);
		checkInvestigationAccess(investigation);

		final Event event = workflowRepository.load(eventId, Event.class);
		Validate.notNull(event, "Invalid event id: " + eventId);
		checkPermission(getPrincipal(), event);
		
		logger.debug("Number of events: {}", investigation.getEvents().size());
		
		investigation.getEvents().remove(event);

		logger.debug("Number of events: {}", investigation.getEvents().size());
		
		final Map<String, Object> items = new HashMap<String, Object>();
		items.put("event", event);

		final Activity activity = logTemplate("remove-event", investigation, items);
		investigation.getActivities().add(activity);

		workflowRepository.save(investigation);
		fireCometUpdate(investigation);
		
	}
	
	/**
	 * Change investigation assignment.
	 * 
	 * TODO: Notification via Email or XMPP.
	 * 
	 * @param eventId
	 * @param userId
	 * @return
	 * @throws PermissionException
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public KeyValueDTO updateInvestigationAssignment(Long investigationId, Long userId) {

		Validate.notNull(investigationId, "Investigation id must be specified.");
		Validate.notNull(userId, "User id must be specified.");

		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id: " + investigationId);
		checkInvestigationAccess(investigation);

		final EpiCenterUser user = permissionRepository.load(userId, EpiCenterUser.class);
		Validate.notNull(user, "Invalid user id: " + userId);
		if (!investigation.getOrganization().getUsers().contains(user)) {
			throw new AccessDeniedException("Cannot assign user to this event.");
		}

		investigation.setAssignedTo(user);

		// Add a log message
		final Activity activity = logTemplate("assign", investigation, null);
		investigation.getActivities().add(activity);

		workflowRepository.save(investigation);

		logger.info(activity.toString());
		fireCometUpdate(investigation);
		
		return new KeyValueDTO(user.getId().toString(), user.getFirstName() + " " + user.getLastName());
	}
	
	/**
	 * @param investigationId
	 * @param transitionId
	 * @param dispositions
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public WorkflowState takeActionOnInvestigationWithDispositions(Long investigationId, Long transitionId,
			Map<String, String> dispositions) {
		Validate.notNull(investigationId, "Investigation id must be specified.");
		Validate.notNull(transitionId, "Transition id must be specified.");
		Validate.notNull(dispositions, "Dispositions must be specified.");
		
		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id " + investigationId);
		checkInvestigationAccess(investigation);
		
		for (Event event : investigation.getEvents()) {
			String dispositionIdString = dispositions.get(event.getId().toString());
			if (dispositionIdString != null) {
				checkPermission(getPrincipal(), event);
				
				long dispositionId = Long.parseLong(dispositionIdString);
				EventDisposition disposition = workflowRepository.load(dispositionId, EventDisposition.class);
				event.setDisposition(disposition);
        		workflowRepository.save(event);
			}
		}
		return takeActionOnInvestigation(investigationId, transitionId);
	}

	/**
	 * Take an action on an investigation.
	 * 
	 * @param eventId
	 * @param transitionId
	 * @return
	 * @throws PermissionException
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public WorkflowState takeActionOnInvestigation(Long investigationId, Long transitionId) {

		Validate.notNull(investigationId, "Investigation id must be specified.");
		Validate.notNull(transitionId, "Transition id must be specified.");

		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid investigation id " + investigationId);
		checkInvestigationAccess(investigation);

		final WorkflowTransition transition = workflowRepository.load(transitionId, WorkflowTransition.class);
		Validate.notNull(transition, "Invalid transition id " + transitionId);

		Validate.isTrue(investigation.getState().getTransitions().contains(transition),
				"Transition is invalid for current investigation state.");

		investigation.setState(transition.getToState());

		// Add a log message
		final Map<String, Object> items = new HashMap<String, Object>();
		items.put("transition", transition);
		final Activity activity = logTemplate("take-action", investigation, items);
		investigation.getActivities().add(activity);

		workflowRepository.save(investigation);
		
		// Set any events to inactive if this investigation is terminated
		if (WorkflowStateType.TERMINAL.equals(investigation.getState().getStateType())) {
			for (Event event : investigation.getEvents()) {
				boolean shouldClose = true;
				for (Investigation i : event.getInvestigations()) {
					if (! WorkflowStateType.TERMINAL.equals(i.getState().getStateType())) {
						shouldClose = false;
						logger.debug("Not inactivating event as there are still pending investigations.");
						break;
					}
				}
				if (shouldClose) {
					logger.debug("Inactivating event id {}", event.getId());
					
					// FIXME: Need to set disposition
				//	event.setState(EventState.INACTIVE);
					workflowRepository.save(event);
				}
			}
		}
		
		
		workflowRepository.save(investigation);

		logger.info(activity.toString());
		fireCometUpdate(investigation);
		
		return investigation.getState();
	}

	/**
	 * Add a comment to an event.
	 * 
	 * @param eventId
	 * @param comment
	 * @return
	 * @throws PermissionException
	 */
	@Secured("ROLE_USER")
	@Transactional
	@RemoteMethod
	public boolean addCommentToInvestigation(Long investigationId, String comment) throws ScanException, PolicyException {

		Validate.notNull(investigationId, "Investigation id must be specified.");
		Validate.notNull(comment, "Comment must be specified.");

		final Investigation investigation = workflowRepository.load(investigationId, Investigation.class);
		Validate.notNull(investigation, "Invalid event id " + investigationId);
		checkInvestigationAccess(investigation);

		final Map<String, Object> items = new HashMap<String, Object>();
		final String cleanComment = antiSamy.scan(comment, antiSamyPolicy).getCleanHTML();
		logger.debug("Cleaned comment: {}", cleanComment);
		
		items.put("comment", cleanComment);

		final Activity activity = logTemplate("comment", investigation, items);
		investigation.getActivities().add(activity);

		workflowRepository.save(investigation);

		logger.info(getPrincipal().getUsername() + " added comment to event " + investigation.getId());
	
		fireCometUpdate(investigation);
		
		return true;

	}

	/**
	 * Renders a property value as a Velocity template to generate an Activity.
	 * 
	 * @param templateName
	 * @param investigation
	 * @param items
	 * @return the activity
	 */
	private Activity logTemplate(final String templateName, final Investigation investigation, final Map<String, Object> items) {

		Validate.isTrue(activityLogTemplates.containsKey(templateName), "No such template property: " + templateName);

		final VelocityContext context = new VelocityContext();

		if (items != null) {
			for (Map.Entry<String, Object> entry : items.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}
		}

		context.put("investigation", investigation);
		context.put("user", getPrincipal());

		final StringWriter writer = new StringWriter();
		try {
			velocityEngine.evaluate(context, writer, "activity-log", activityLogTemplates.getProperty(templateName));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return new Activity(investigation, getPrincipal(), writer.toString());
	}

	/**
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public String getDateOfOldestInvestigation() {
		String ret = "7 days";
	//	final Geometry v = isGlobalAdministrator(getPrincipal()) ? null : getPrincipal().getVisibleRegion();
		final DateTime c = workflowRepository.getDateOfOldestInvestigation(null, getVisibleOrganizations(getPrincipal()));
		if (c != null) {
			ret = formatDurationDays(c, new DateTime());
		}
		
		return ret;
	}
	
	/**
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<EventDisposition> getEventDispositions() {
		final List<EventDisposition> dispositions = workflowRepository.getList(EventDisposition.class);
		Collections.sort(dispositions);
		return dispositions;
	}
	
	/**
	 * @param investigation
	 */
	private void checkInvestigationAccess(final Investigation investigation) {
		
		if (!SpatialSecurity.isGlobalAdministrator(getPrincipal())) {
			final Set<Organization> orgs = permissionRepository.getVisibleOrganizations(getPrincipal());
			if (!orgs.contains(investigation.getOrganization())) {
				throw new AccessDeniedException("Organization " + investigation.getOrganization().getId() + " is not accessible.");
			}
		}
	}
	
	/**
	 * @param user
	 * @return
	 */
	private Set<Organization> getVisibleOrganizations(final EpiCenterUser user) {
		
		final Set<Organization> orgs;
		if (SpatialSecurity.isGlobalAdministrator(user)) {
			orgs = null;
		} else {
			orgs = permissionRepository.getVisibleOrganizations(user);
		}
		return orgs;
	}
	
	/**
	 * Refreshes the InvestigationDetails panel on any client that is viewing it.
	 * 
	 * We could get very crazy here.
	 * 
	 * @param investigation
	 */
	private void fireCometUpdate(final Investigation investigation) {
		
		WebContext wctx = WebContextFactory.get();
		Collection<ScriptSession> sessions = wctx.getAllScriptSessions();
		final Util dwrUtil = new Util();
		
		logger.trace("number of scriptsessions: " + sessions.size());
		
		final ScriptSession mySession = wctx.getScriptSession();
		
		for (ScriptSession session : sessions) {
			if (!session.equals(mySession)) {
				final Object i = session.getAttribute("investigation");
				if (i != null) {
					final Long investigationId = (Long)i;
					if (investigation.getId().equals(investigationId)) {
						dwrUtil.addScriptSession(session);
					}
				}
			}
		}
		
		dwrUtil.addFunctionCall("EpiCenter.core.Viewport.panels.investigationPanel.refreshDetails", 
				new InvestigationDetailsDTO(investigation, getPrincipal()));
		
	}
}
