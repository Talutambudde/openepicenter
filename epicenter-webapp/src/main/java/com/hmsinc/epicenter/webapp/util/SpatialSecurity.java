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
package com.hmsinc.epicenter.webapp.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.GrantedAuthority;

import com.hmsinc.epicenter.model.analysis.AnalysisLocation;
import com.hmsinc.epicenter.model.geography.GeographicalEntity;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.permission.AuthorizedRegion;
import com.hmsinc.epicenter.model.permission.AuthorizedRegionType;
import com.hmsinc.epicenter.model.permission.EpiCenterUser;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.permission.PermissionException;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.surveillance.Anomaly;
import com.hmsinc.epicenter.model.util.ModelUtils;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.Investigation;
import com.hmsinc.epicenter.webapp.dto.GeographyDTO;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;

/**
 * Security related stuff.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: SpatialSecurity.java 1821 2008-07-11 16:01:12Z steve.kondik $
 */
public final class SpatialSecurity {

	private static final Logger logger = LoggerFactory.getLogger(SpatialSecurity.class);

	/**
	 * Determines the visibility of one geometry compared to another.
	 * 
	 * Equals, contains, or covers is FULL visibility.
	 * 
	 * Within/coveredby is LIMITED visibility.
	 * 
	 * @param container
	 * @param geo
	 * @return
	 */
	private static Visibility getVisibility(final Geometry container, final Geometry geo) {
		Visibility v = Visibility.NONE;
		if (container.equals(geo) || container.contains(geo) || container.covers(geo)) {
			v = Visibility.FULL;
		} else if (container.within(geo) || container.coveredBy(geo) || container.overlaps(geo)) {
			v = Visibility.LIMITED;
		}
		return v;
	}

	/**
	 * Determines the visibility of one geometry compared to another.
	 * 
	 * Equals, contains, or covers is FULL visibility.
	 * 
	 * Within/coveredby is LIMITED visibility.
	 * 
	 * Uses PreparedGeometry.
	 * 
	 * @param container
	 * @param geo
	 * @return
	 */
	private static Visibility getVisibility(final PreparedGeometry container, final Geometry geo) {
		Visibility v = Visibility.NONE;
		if (container.equals(geo) || container.contains(geo) || container.covers(geo)) {
			v = Visibility.FULL;
		} else if (container.within(geo) || container.coveredBy(geo) || container.overlaps(geo)) {
			v = Visibility.LIMITED;
		}
		return v;
	}
	
	/**
	 * @param container
	 * @param geography
	 * @return
	 */
	public static Visibility getVisibility(final Geography container, final Geography geography) {
		final Geometry geometry;
		if (Zipcode.class.isAssignableFrom(ModelUtils.getRealClass(geography))) {
			geometry = geography.getCentroid();
		} else {
			geometry = geography.getGeometry();
		}
		return getVisibility(container.getGeometry(), geometry);
	}
	
	/**
	 * @param user
	 * @param geography
	 * @return
	 */
	public static Visibility getVisibility(final EpiCenterUser user, final Geography geography) {

		Visibility v = Visibility.NONE;
		if (isGlobalAdministrator(user)) {
			v = Visibility.FULL;
		} else {
			final Geometry g;
			if (geography instanceof Zipcode) {
				g = geography.getGeometry().getCentroid();
			} else {
				g = geography.getGeometry();
			}
			v = getVisibility(user.getVisibleRegion(), g);
			if (Visibility.FULL.equals(v)) {
				if (isAggregateOnlyAccess(user, geography)) {
					v = Visibility.AGGREGATE_ONLY;
				}
			}
		}
		return v;
	}

	/**
	 * @param user
	 * @return
	 */
	public static boolean isGlobalAdministrator(final EpiCenterUser user) {
		boolean ret = false;
		for (GrantedAuthority authority : user.getAuthorities()) {
			if (authority.getAuthority() != null && authority.getAuthority().equals("ROLE_ADMIN")) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * Validates that the user has access to the specified event.
	 * 
	 * @param event
	 * @throws PermissionException
	 */
	private static void checkEventPermission(final EpiCenterUser user, final Event event) {
		if (!user.getOrganizations().contains(event.getOrganization())) {
			final Visibility v = getVisibility(user, event.getGeography());
			if (event instanceof Anomaly && Visibility.AGGREGATE_ONLY.equals(v)
					&& AnalysisLocation.FACILITY.equals(((Anomaly) event).getTask().getLocation())) {
				throw new AccessDeniedException("Access to object id " + event.getId()
						+ " is denied (aggregate access only)");
			}

			if (Visibility.NONE.equals(v) || Visibility.LIMITED.equals(v)) {
				throw new AccessDeniedException("Access to object id " + event.getId() + " is denied.");
			}
		}
	}

	/**
	 * @param user
	 * @param geography
	 * @return
	 */
	public static void checkPermission(final EpiCenterUser user, final Geography geography) {

		final Visibility v = getVisibility(user, geography);
		if (Visibility.NONE.equals(v) || Visibility.LIMITED.equals(v)) {
			throw new AccessDeniedException("Selected geography is not accessible.");
		}
	}

	/**
	 * @param user
	 * @param geometry
	 */
	public static void checkPermission(final EpiCenterUser user, final Geometry geometry) {
		
		if (!isGlobalAdministrator(user)) {

			final Visibility v = getVisibility(user.getVisibleRegion(), geometry);
			if (!Visibility.FULL.equals(v)) {
				throw new AccessDeniedException("Selected geometry is not accessible.");
			}
		}
	}
	
	/**
	 * @param user
	 * @param item
	 */
	public static void checkPermission(final EpiCenterUser user, final GeographicalEntity item) {
		if (item instanceof Event) {
			checkEventPermission(user, (Event)item);
		} else if (!isAccessible(user, item)) {
			throw new AccessDeniedException("Selected entity is not accessible.");
		}
	}

	/**
	 * @param user
	 * @param investigation
	 */
	public static void checkPermission(final EpiCenterUser user, final Investigation investigation) {
		if (!isGlobalAdministrator(user) && !Visibility.FULL.equals(getVisibility(user.getVisibleRegion(), investigation.getLocality()))) {
			throw new AccessDeniedException("Selected investigation is not accessible.");
		}
	}
	
	/**
	 * @param user
	 * @param facility
	 * @return
	 */
	public static boolean isAccessible(final EpiCenterUser user, final Facility facility) {

		boolean ret = true;
		if (!isGlobalAdministrator(user)) {
			final Visibility v = getVisibility(user.getVisibleRegion(), facility.getGeometry());
			if (!Visibility.FULL.equals(v)) {
				ret = false;
			}
		}
		return ret;
	}
	
	/**
	 * @param user
	 * @param item
	 * @return
	 */
	public static boolean isAccessible(final EpiCenterUser user, final GeographicalEntity item) {
		final Visibility v;
		if (item instanceof Organization) {
			final Organization org = (Organization)item;
			if (org.getGeography() == null) {
				v = getVisibility(user.getVisibleRegion(), org.getVisibleRegion().getGeometry());
			} else {
				v = getVisibility(user, item.getGeography());
			}
		} else {
			v = getVisibility(user, item.getGeography());
		}
		
		return (!Visibility.NONE.equals(v) && !Visibility.LIMITED.equals(v));
	}

	/**
	 * @param user
	 * @param geography
	 * @return
	 */
	public static boolean isGeographyAccessible(final EpiCenterUser user, final Geography geography) {
		final Visibility v = getVisibility(user, geography);
		return (!Visibility.NONE.equals(v) && !Visibility.LIMITED.equals(v));
	}
	
	/**
	 * Removes geographies from a collection if they are not visible to the
	 * given user.
	 * 
	 * @param user
	 * @param geographies
	 * @param includeLimited
	 * @return
	 */
	public static Collection<GeographyDTO> filterSpatialCollection(final EpiCenterUser user,
			final Collection<? extends Geography> geographies, final boolean includeLimited) {

		final Set<GeographyDTO> visible = new LinkedHashSet<GeographyDTO>();

		for (Geography geography : geographies) {
			if (geography.getGeometry() != null) {
				if (isGlobalAdministrator(user)) {
					visible.add(new GeographyDTO(geography, Visibility.FULL));
				} else {
					final Visibility v = getVisibility(user, geography);
					logger.trace("geography: {}  visibility: {}", geography, v);
					if (!Visibility.NONE.equals(v) && (includeLimited || (!includeLimited && !Visibility.LIMITED.equals(v)))) {
						visible.add(new GeographyDTO(geography, v));
						logger.trace("including {}", geography);
					}
				}
			}
		}

		return visible;
	}

	/**
	 * Removes geographies from a collection if they are not contained within
	 * the given entity.
	 * 
	 * @param user
	 * @param geographies
	 * @param includeLimited
	 * @return
	 */
	public static Collection<GeographyDTO> filterSpatialCollection(final GeographicalEntity entity,
			final Collection<? extends Geography> geographies, final boolean includeLimited) {

		final Set<GeographyDTO> visible = new LinkedHashSet<GeographyDTO>();

		for (Geography geography : geographies) {
			if (geography.getGeometry() != null) {
				final Geometry g;
				if (geography instanceof Zipcode) {
					g = geography.getGeometry().getCentroid();
				} else {
					g = geography.getGeometry();
				}
				
				if (entity.getGeography() != null) {
					final Visibility v = getVisibility(entity.getGeography().getGeometry(), g);
					if (!Visibility.NONE.equals(v)) {
						if (includeLimited || (!includeLimited && !Visibility.LIMITED.equals(v))) {
							visible.add(new GeographyDTO(geography, v));
						}
					}
				}
			}
		}

		return visible;
	}

	/**
	 * @param user
	 * @param geography
	 * @return
	 */
	public static boolean isAggregateOnlyAccess(final EpiCenterUser user, final Geography geography) {
		boolean ret = false;
		if (!isGlobalAdministrator(user)) {
			for (Organization org : user.getOrganizations()) {
				if (org.getAuthorizedRegions() != null) {
					for (AuthorizedRegion ar : org.getAuthorizedRegions()) {
						final Geometry container = ar.getGeography().getGeometry();
						final Geometry geo = geography.getGeometry();
						if (AuthorizedRegionType.AGGREGATE_ONLY.equals(ar.getType())
								&& (container.equals(geo) || container.contains(geo) || container.covers(geo))) {
							ret = true;
						}
					}
				}
			}
		}
		return ret;

	}

	/**
	 * @param user
	 * @param geography
	 */
	public static void checkAggregateOnlyAccess(final EpiCenterUser user, final Geography geography) {
		if (isAggregateOnlyAccess(user, geography)) {
			throw new AccessDeniedException("Access to the selected geography is limited to aggregated data only.");
		}

	}

	/**
	 * Filters a collection of GeographicEntities.
	 * 
	 * @param <T>
	 * @param user
	 * @param items
	 * @return
	 */
	public static <T extends GeographicalEntity> Collection<T> filterGeographicalCollection(final EpiCenterUser user,
			final Collection<T> items, final Geometry container) {

		final Set<GeographicalEntity> badItems = new HashSet<GeographicalEntity>();
		if (!isGlobalAdministrator(user)) {
			for (T item : items) {
				logger.debug("checking item: {}", item);
				if (!getVisibility(user.getVisibleRegion(), item.getGeography().getGeometry()).equals(Visibility.FULL)) {
					logger.debug("Removing item: {}", item);
					badItems.add(item);
				}
			}
			items.removeAll(badItems);
		}

		if (container != null) {
			badItems.clear();
			for (T item : items) {
				if (!item.getGeography().getGeometry().within(container)
						&& !item.getGeography().getGeometry().coveredBy(container)) {
					logger.debug("Removing item: {}", item);
					badItems.add(item);
				}
			}
			items.removeAll(badItems);
		}

		return items;
	}
}
