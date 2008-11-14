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
package com.hmsinc.epicenter.model.permission.impl;

import static com.hmsinc.epicenter.model.util.ModelUtils.criteriaQuery;
import static com.hmsinc.epicenter.model.util.ModelUtils.namedQuery;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.jasypt.util.password.PasswordEncryptor;
import org.joda.time.DateTime;

import com.hmsinc.epicenter.model.AbstractJPARepository;
import com.hmsinc.epicenter.model.permission.AuditEvent;
import com.hmsinc.epicenter.model.permission.AuditEventType;
import com.hmsinc.epicenter.model.permission.EpiCenterRole;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.permission.PasswordResetToken;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.permission.PermissionExceptionType;
import com.hmsinc.epicenter.model.permission.PermissionObject;
import com.hmsinc.epicenter.model.permission.PermissionRepository;
import com.hmsinc.hibernate.criterion.SpatialRestrictions;

/**
 * Manages the repository of PermissionObjects.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: PermissionRepositoryImpl.java 186 2007-06-07 14:49:05Z
 *          steve.kondik $
 */
public class PermissionRepositoryImpl extends AbstractJPARepository<PermissionObject, Long> implements PermissionRepository {

	@Resource
	private PasswordEncryptor passwordEncryptor;

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#authenticateUser(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public EpiCenterUser authenticateUser(final String username, final String password) throws PermissionException {

		Validate.notNull(username);
		Validate.notNull(password);

		EpiCenterUser user = null;
		final List<EpiCenterUser> l = namedQuery(entityManager, "getUser").setParameter("username", username).getResultList();

		if (l == null || l.size() != 1) {
			throw new PermissionException(PermissionExceptionType.AUTHENTICATION_FAILED, username);
		}

		user = l.get(0);

		// Check if the user is enabled..
		if (!user.isEnabled()) {
			throw new PermissionException(PermissionExceptionType.USER_IS_DISABLED, username);
		}
		
		// Check the password..
		if (!passwordEncryptor.checkPassword(password, user.getPassword())) {
			throw new PermissionException(PermissionExceptionType.AUTHENTICATION_FAILED,  username);
		}

		return user;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#addUser(com.hmsinc.epicenter.model.permission.EpiCenterUser)
	 */
	public void addUser(final EpiCenterUser user) throws PermissionException {
		
		Validate.notNull(user);
		Validate.notNull(user.getUsername());
		Validate.notNull(user.getPassword());
		
		// First make sure the user doesn't already exist..
		long users = ((Long)namedQuery(entityManager, "getUserCount").setParameter("username", user.getUsername()).getSingleResult()).longValue();
		if (users > 0) {
			throw new PermissionException(PermissionExceptionType.USERNAME_ALREADY_EXISTS, user.getUsername());
		}
		
		long userEmails = ((Long)namedQuery(entityManager, "getUserCountByEmailAddress").setParameter("emailAddress", user.getEmailAddress()).getSingleResult()).longValue();
		if (userEmails > 0) {
			throw new PermissionException(PermissionExceptionType.EMAIL_ALREADY_EXISTS, user.getEmailAddress());
		}
		
		// Add ROLE_USER if it's not there.
		final EpiCenterRole userRole = getRole("ROLE_USER");
		if (!user.getRoles().contains(userRole)) {
			user.getRoles().add(userRole);
		}
		
		// Encrypt the password..
		user.setPassword(passwordEncryptor.encryptPassword(user.getPassword()));
		
		save(user);
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#checkForExistingOrganization(java.lang.String)
	 */
	public boolean checkForExistingOrganization(String organizationName) {
		Validate.notNull(organizationName, "Organization name is required.");
		final Long count = (Long)namedQuery(entityManager, "getOrganizationCount").setParameter("name", organizationName).getSingleResult();
		return count == 0 ? false : true;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#addOrganization(com.hmsinc.epicenter.model.permission.Organization)
	 */
	public void addOrganization(Organization organization) throws PermissionException {
		Validate.notNull(organization);
		Validate.notEmpty(organization.getName());
		
		long orgs = ((Long)namedQuery(entityManager, "getOrganizationCount").setParameter("name", organization.getName()).getSingleResult()).longValue();
		if (orgs > 0) {
			throw new PermissionException(PermissionExceptionType.ORGANIZATION_ALREADY_EXISTS, organization.getName());
		}
		
		save(organization);
		
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#changePassword(com.hmsinc.epicenter.model.permission.EpiCenterUser, java.lang.String)
	 */
	public void changePassword(final EpiCenterUser user, final String newPassword) {
		
		Validate.notNull(user);
		Validate.notNull(user.getUsername());
		Validate.notNull(newPassword);
		
		user.setPassword(passwordEncryptor.encryptPassword(newPassword));
		
		update(user);
		
		final AuditEvent event = new AuditEvent(user, AuditEventType.PASSWORD_CHANGED);
		save(event);
		
	}
	
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getUserByUsername(com.hmsinc.epicenter.model.permission.EpiCenterUser)
	 */
	@SuppressWarnings("unchecked")
	public EpiCenterUser getUserByUsername(final String username) throws PermissionException {
	
		Validate.notNull(username);

		final List<EpiCenterUser> l = namedQuery(entityManager, "getUser").setParameter("username", username).getResultList();
		if (l.size() == 0) {
			throw new PermissionException(PermissionExceptionType.UNKNOWN_USER, username);
		}
		return l.get(0);

	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getUserByEmailAddress(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public EpiCenterUser getUserByEmailAddress(String emailAddress) throws PermissionException {
		Validate.notNull(emailAddress);

		final List<EpiCenterUser> l = namedQuery(entityManager, "getUserByEmailAddress").setParameter("emailAddress", emailAddress).getResultList();
		if (l.size() == 0) {
			throw new PermissionException(PermissionExceptionType.UNKNOWN_USER, emailAddress);
		}
		return l.get(0);

	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#checkForExistingUser(java.lang.String, java.lang.String)
	 */
	public boolean checkForExistingUser(String username, String emailAddress) {
		Validate.notNull(username);
		Validate.notNull(emailAddress);
		
		boolean ret = false;
		final Long userCount = (Long) namedQuery(entityManager, "checkForExistingUser").setParameter("username", username)
			.setParameter("emailAddress", emailAddress).getSingleResult();
		
		if (userCount > 0) {
			ret = true;
		}
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getGlobalAdministrators()
	 */
	@SuppressWarnings("unchecked")
	public Organization getGlobalAdministrators() throws PermissionException {
		
		final List<Organization> l = namedQuery(entityManager, "getOrganizationByName").setParameter("name", PermissionRepository.GLOBAL_ADMIN_ORG).getResultList();
		if (l.size() == 0) {
			throw new PermissionException(PermissionExceptionType.UNKNOWN_ORGANIZATION, "Global Administrators");
		}
		return l.get(0);
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getOrganizations(boolean)
	 */
	@SuppressWarnings("unchecked")
	public List<Organization> getOrganizations(boolean enabled) {
		return namedQuery(entityManager, "getOrganizations").setParameter("enabled", enabled).getResultList();
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getRole(java.lang.String)
	 */
	public EpiCenterRole getRole(String roleName) {
		return (EpiCenterRole)namedQuery(entityManager, "getRole").setParameter("roleName", roleName).getSingleResult();
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getPasswordResetToken(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public PasswordResetToken getPasswordResetToken(String token) {
		Validate.notNull(token, "Token must be provided.");
		purgeExpiredTokens();
		PasswordResetToken ret = null;
		final List<PasswordResetToken> l = namedQuery(entityManager, "getPasswordResetToken").setParameter("token", token).getResultList();
		if (l != null && l.size() == 1) {
			ret = l.get(0);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#purgeExpiredTokens()
	 */
	public void purgeExpiredTokens() {
		namedQuery(entityManager, "purgeExpiredTokens").setParameter("time", new DateTime()).executeUpdate();		
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getOrganization(java.lang.Long)
	 */
	public Organization getOrganization(Long organizationId) {
		Validate.notNull(organizationId);
		return (Organization)namedQuery(entityManager, "getOrganization").setParameter("organizationId", organizationId).getSingleResult();
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getContainingOrganizations(com.hmsinc.epicenter.model.permission.Organization)
	 */
	@SuppressWarnings("unchecked")
	public List<Organization> getContainingOrganizations(Organization organization) {
		Validate.notNull(organization);
		return criteriaQuery(entityManager, Organization.class).createCriteria("authoritativeRegion")
			.add(SpatialRestrictions.contains("geometry", organization.getAuthoritativeRegion().getGeometry())).list();
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getAllSponsoredOrganizations(com.hmsinc.epicenter.model.permission.Organization)
	 */
	public List<Organization> getSponsorTree(Organization organization) {
		
		Validate.notNull(organization);
		final Set<Long> sponsoredIds = getSponsoredOrganizationsUsingRecursion(organization, new HashSet<Long>());
		return getReferences(sponsoredIds, Organization.class);
	}
	
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.permission.PermissionRepository#getVisibleOrganizations(com.hmsinc.epicenter.model.permission.EpiCenterUser)
	 */
	public Set<Organization> getVisibleOrganizations(EpiCenterUser user) {
		final Set<Organization> orgs = new LinkedHashSet<Organization>();
		for (Organization org : user.getOrganizations()) {
			orgs.addAll(getSponsorTree(org));
		}
		return orgs;
	}

	/**
	 * Recurses thru the tree of Organization sponsors.
	 * 
	 * @param organization
	 * @param sponsored
	 * @return
	 */
	private Set<Long> getSponsoredOrganizationsUsingRecursion(final Organization organization, final Set<Long> sponsored) {
		
		
		// We'll only work with IDs here so we avoid touching any lazy properties.
		Validate.notNull(sponsored);

		if (organization != null) {
			sponsored.add(organization.getId());
			final Set<Organization> so = organization.getSponsoredOrganizations();
			if (so != null && so.size() > 0) {
				for (Organization org : so) {
					getSponsoredOrganizationsUsingRecursion(org, sponsored);
				}
			}
		}	
		return sponsored;
	}
	
	
}
