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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.AuditEvent;
import com.hmsinc.epicenter.model.permission.AuditEventType;
import com.hmsinc.epicenter.model.permission.AuthorizedRegion;
import com.hmsinc.epicenter.model.permission.AuthorizedRegionType;
import com.hmsinc.epicenter.model.permission.EpiCenterRole;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.permission.PermissionObject;
import com.hmsinc.epicenter.model.workflow.Workflow;
import com.hmsinc.epicenter.model.workflow.WorkflowRepository;
import com.hmsinc.epicenter.util.ReflectionUtils;
import com.hmsinc.epicenter.webapp.dto.AuthorizedRegionDTO;
import com.hmsinc.epicenter.webapp.dto.KeyValueDTO;
import com.hmsinc.epicenter.webapp.dto.TreeNode;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.hmsinc.epicenter.webapp.util.Visibility;

/**
 * Administrative services.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: AdminService.java 1740 2008-06-16 14:52:54Z steve.kondik $
 */
@RemoteProxy(name = "AdminService")
public class AdminService extends AbstractRemoteService {

	@Resource
	private WorkflowRepository workflowRepository;

	/**
	 * @param nodeId
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public List<TreeNode> getOrganizationTree(String nodeId) {

		final List<TreeNode> nodes = new ArrayList<TreeNode>();

		// root of the tree
		if (nodeId.equals("root")) {
			final List<Organization> organizations = permissionRepository.getOrganizations(true);
			for (Organization org : organizations) {
				nodes.add(new TreeNode("org-" + org.getId().toString(), org.getName(), false));
			}
			return nodes;

		} else if (nodeId.startsWith("org-")) {

			final Long id = Long.valueOf((nodeId.split("-"))[1]);
			// Users in the requested organization
			final Set<EpiCenterUser> users = permissionRepository.load(id, Organization.class).getUsers();
			for (EpiCenterUser user : users) {
				if (user.isEnabled()) {
					nodes.add(new TreeNode("user-" + user.getId().toString(), user.getUsername(), true));
				}
			}
		}

		return nodes;
	}

	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public List<KeyValueDTO> getAuthorizedRegionTypes() {
		final List<KeyValueDTO> dto = new ArrayList<KeyValueDTO>();
		for (AuthorizedRegionType type : AuthorizedRegionType.values()) {
			dto.add(new KeyValueDTO(type.name(), type.name()));
		}
		return dto;
	}

	/**
	 * @param id
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Organization getOrganization(Long id) {

		Validate.notNull(id);
		final Organization org = permissionRepository.getOrganization(id);
		Validate.notNull(org, "Invalid organization: " + id);

		return org;
	}

	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Set<KeyValueDTO> getSponsors(Long id) {
		
		Validate.notNull(id);
		final Organization org = permissionRepository.getOrganization(id);
		Validate.notNull(org, "Invalid organization: " + id);
		
		final Set<KeyValueDTO> sponsors = new HashSet<KeyValueDTO>();
		for (Organization sponsor : org.getSponsors()) {
			sponsors.add(new KeyValueDTO(sponsor.getId().toString(), sponsor.getName()));
		}
		
		return sponsors;
	}
	
	/**
	 * @param organizationName
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public boolean isOrganizationAvailable(final String organizationName) {
		return !permissionRepository.checkForExistingOrganization(organizationName);
	}

	/**
	 * @param obj
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional
	@RemoteMethod
	public Organization saveOrganization(Organization obj, Long workflowId, Long authoritativeRegionId, String sponsorIds,
			Collection<AuthorizedRegionDTO> authorizedRegions) {

		Validate.notNull(obj);
		Validate.notNull(workflowId);
		
		Validate.isTrue(!obj.getName().equals("Global Administrators"), "The administrator group is not editable.");
		
		final Workflow w = workflowRepository.load(workflowId, Workflow.class);
		Validate.notNull(w, "Invalid workflow: " + workflowId);

		final Organization merged = merge(obj, Organization.class);
		merged.setWorkflow(w);

		if (authoritativeRegionId != null) {
			final Geography g = geographyRepository.load(authoritativeRegionId, Geography.class);
			Validate.notNull(g, "Invalid geography: " + authoritativeRegionId);
			merged.setAuthoritativeRegion(g);
		}

		if (authorizedRegions != null) {
			updateAuthorizedRegions(merged, authorizedRegions);
		}
		
		if (sponsorIds != null) {
			
			final Set<Organization> sponsors = new HashSet<Organization>();
			final Collection<Long> ids = splitStringToIdList(sponsorIds);
			
			if (ids != null && ids.size() > 0) {
				for (Long sponsorId : ids) {
					final Organization sponsor = permissionRepository.load(sponsorId, Organization.class);
					Validate.notNull(sponsor, "Invalid sponsor id: " + sponsorId);
				
					logger.debug("Sponsor: {}", sponsor.getName());
				
					sponsors.add(sponsor);
				}
			}
			
			merged.getSponsors().clear();
			merged.getSponsors().addAll(sponsors);
		}
		
		permissionRepository.save(merged);

		audit("Updated organization: " + merged.toString());

		return merged;
	}

	/**
	 * @param organizationId
	 * @param deleteUsers
	 */
	@Secured("ROLE_ADMIN")
	@Transactional
	@RemoteMethod
	public void deleteOrganization(Long organizationId, boolean deleteUsers) {

		Validate.notNull(organizationId);
		final Organization o = permissionRepository.load(organizationId, Organization.class);
		
		Validate.isTrue(!o.getName().equals("Global Administrators"), "The administrator group is not removable.");
		
		for (EpiCenterUser user : o.getUsers()) {
			o.getUsers().remove(user);
			user.getOrganizations().remove(o);
			if (deleteUsers && user.getOrganizations().size() == 0) {
				deleteUser(user);
			} else {
				permissionRepository.save(user);
			}
		}

		// Save it before we do anything else to update user states
		permissionRepository.save(o);

		// TODO: Deal with SurveillanceTasks correctly
		if (o.getInvestigations().size() > 0 || o.getSurveillanceTasks().size() > 0 || o.getUsers().size() > 0) {
			o.setEnabled(false);
			permissionRepository.save(o);
		} else {
			permissionRepository.delete(o);
		}
		
		audit("Deleted organization: " + organizationId);
	}

	/**
	 * @param id
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public EpiCenterUser getUser(Long id) {
		Validate.notNull(id);
		final EpiCenterUser user = permissionRepository.load(id, EpiCenterUser.class);
		Validate.notNull(user, "Invalid user: " + id);
		return user;
	}

	/**
	 * @param obj
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional
	@RemoteMethod
	public EpiCenterUser saveUser(EpiCenterUser obj, Long orgId, String password, boolean isOrgAdmin)
			throws PermissionException {

		Validate.notNull(obj);
		Validate.notNull(orgId);
		final Organization org = permissionRepository.load(orgId, Organization.class);
		Validate.notNull(org, "Invalid organization: " + orgId);
		Validate.isTrue(org.isEnabled(), "Organization is disabled.");

		EpiCenterUser user = null;

		final EpiCenterRole orgAdminRole = permissionRepository.getRole("ROLE_ORG_ADMIN");

		if (obj.getId() == null) {
			Validate.notEmpty(password, "No password given");
			user = obj;
			user.setPassword(password);
			permissionRepository.addUser(user);

			org.getUsers().add(user);

			if (isOrgAdmin) {
				user.getRoles().add(orgAdminRole);
			}

			permissionRepository.save(org);
			logger.info("Created user: {}", user);

		} else {
			
			obj.setPassword(null);
			user = merge(obj, EpiCenterUser.class);
			if (!user.getOrganizations().contains(org)) {
				user.getOrganizations().add(org);
				org.getUsers().add(user);
				permissionRepository.save(org);
			}

			if (isOrgAdmin) {
				if (!user.getRoles().contains(orgAdminRole)) {
					user.getRoles().add(orgAdminRole);
				}
			} else {
				if (user.getRoles().contains(orgAdminRole)) {
					user.getRoles().remove(orgAdminRole);
				}
			}
			permissionRepository.save(user);
			audit("Updated user: " + user.toString());
			
			final String pw = StringUtils.trimToNull(password);
			
			if (pw != null) {
				permissionRepository.changePassword(user, pw);
				logger.info("Changed user password: {}", user);
			}
		}

		return user;
	}

	/**
	 * @param id
	 */
	@Secured("ROLE_ADMIN")
	@Transactional
	@RemoteMethod
	public void deleteUser(Long id) {

		Validate.notNull(id);
		final EpiCenterUser user = permissionRepository.load(id, EpiCenterUser.class);
		Validate.notNull(user, "Invalid user id: " + id);

		for (Organization org : user.getOrganizations()) {
			org.getUsers().remove(user);
			user.getOrganizations().remove(org);
			permissionRepository.save(org);
		}

		deleteUser(user);
	}

	@Secured("ROLE_ADMIN")
	@Transactional
	@RemoteMethod
	public void deleteUser(EpiCenterUser user) {

		// If the user has any associated objects, just disable it.
		if (user.getActivities().size() > 0 || user.getAssignedEvents().size() > 0 || user.getAttachments().size() > 0) {
			logger.info("Setting user {} to INACTIVE", user.getUsername());
			user.setEnabled(false);
			permissionRepository.save(user);
			audit("Deactivated user: " + user.getUsername());
		} else {
			permissionRepository.delete(user);
			audit("Deleted user: " + user.getUsername());
		}
	}

	/**
	 * @param organizationId
	 * @return
	 */
	@Secured("ROLE_ADMIN")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<KeyValueDTO> getOrganizationList() {

		final List<Organization> orgs = permissionRepository.getList(Organization.class);

		final Set<KeyValueDTO> dtos = new TreeSet<KeyValueDTO>();
		if (orgs != null) {
			for (Organization org : orgs) {
				if (org.isEnabled()) {
					dtos.add(new KeyValueDTO(org.getId().toString(), org.getName()));
				}
			}
		}
		return dtos;
	}

	/**
	 * @param authorizedRegions
	 */
	@Transactional
	private void updateAuthorizedRegions(final Organization org, Collection<AuthorizedRegionDTO> authorizedRegions) {

		Validate.notNull(org, "Organization must be specified.");
		Validate.notNull(authorizedRegions, "No authorized regions provided.");
		
		Validate.isTrue(!org.getName().equals("Global Administrators"), "The administrator group is not editable.");
		
		final Set<AuthorizedRegion> ars = new HashSet<AuthorizedRegion>();

		for (AuthorizedRegionDTO authorizedRegion : authorizedRegions) {

			Validate.notNull(authorizedRegion.getGrantedById(), "Granted by must be specified.");
			final Organization grantedBy = permissionRepository.load(authorizedRegion.getGrantedById(),
					Organization.class);
			Validate.notNull(grantedBy, "Invalid granted by id.");
			
			if (org.getId() != null) {
				Validate.isTrue(org.getId() != null && !org.getId().equals(grantedBy.getId()), "Can't assign regions to self.");
			}
			
			Validate.notNull(authorizedRegion.getGeographyId(), "Geography id must be specified");
			final Geography geography = geographyRepository.load(authorizedRegion.getGeographyId(), Geography.class);
			Validate.notNull(geography, "Invalid geography id: " + authorizedRegion.getGeographyId());

			Validate.isTrue(Visibility.FULL.equals(SpatialSecurity.getVisibility(grantedBy.getAuthoritativeRegion(), geography)), "Organization " + grantedBy.getName()
					+ " does not have authority for region " + geography.getDisplayName());

			final AuthorizedRegionType type = AuthorizedRegionType.valueOf(authorizedRegion.getType());
			Validate.notNull(type, "Invalid authorization type: " + authorizedRegion.getType());

			final AuthorizedRegion ar = new AuthorizedRegion(type, grantedBy, geography);
			ars.add(ar);

		}

		org.getAuthorizedRegions().clear();
		org.getAuthorizedRegions().addAll(ars);

	}

	/**
	 * Rehydrates the object and copies any changed properties into it.
	 * 
	 * @param <E>
	 * @param obj
	 * @param type
	 * @return
	 */
	private <E extends PermissionObject> E merge(E obj, final Class<E> type) {

		final E ret;
		if (obj.getId() != null) {
			ret = permissionRepository.load(obj.getId(), type);
			ReflectionUtils.copyProperties(obj, ret, false, false);
		} else {
			ret = obj;
		}
		return ret;
	}
	
	@Transactional
	private void audit(final String message) {
		
		final AuditEvent event = new AuditEvent(getPrincipal(), AuditEventType.ADMINISTRATIVE, message);
		permissionRepository.save(event);
		
		logger.info(event.toString());
	}
}
