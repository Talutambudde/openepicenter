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

import static com.hmsinc.epicenter.webapp.util.SpatialSecurity.filterSpatialCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.springframework.security.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyType;
import com.hmsinc.epicenter.model.geography.Region;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.hmsinc.epicenter.model.permission.AuthorizedRegion;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.webapp.dto.GeographyDTO;
import com.hmsinc.epicenter.webapp.util.SpatialSecurity;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * Handles queries of geographic data.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:GeographyService.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@RemoteProxy(name = "GeographyService")
public class GeographyService extends AbstractRemoteService {
	
	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

	/**
	 * Gets all available States for the user.
	 * 
	 * Use StatesAndCounties to prime the cache.
	 * 
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<GeographyDTO> getStates() {
		final Set<GeographyDTO> items = new TreeSet<GeographyDTO>();
		for (State state : geographyRepository.getList(State.class)) {
			items.add(new GeographyDTO(state, SpatialSecurity.getVisibility(getPrincipal(), state)));
		}
		return items;
	}

	/**
	 * Returns a default Geography listing. Contains the users authorized
	 * regions and optionally contained regions.
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	@RemoteMethod
	private Collection<Geography> getVisibleRegions(final Organization entity) {

		final List<Geography> geolist = new ArrayList<Geography>();
		
		final List<Organization> orgs = new ArrayList<Organization>();
		if (entity == null) {
			orgs.addAll(getPrincipal().getOrganizations());
		} else {
			orgs.add(entity);
		}

		for (Organization org : orgs) {
			if (org.getAuthoritativeRegion() != null) {
				geolist.add(org.getAuthoritativeRegion());
			}
			for (AuthorizedRegion authorized : org.getAuthorizedRegions()) {
				if (authorized.getGeography() != null) {
					geolist.add(authorized.getGeography());
				}
			}
		}
		
		logger.debug("Visible: {}", geolist);
		
		// If we have a single match, find the objects contained inside it.
		if (geolist.size() == 1) {
			geolist.addAll(getContainedGeographies(geolist.get(0)));
		} else if (geolist.size() > 1) {
			Collections.sort(geolist);
		}

		return geolist;
	}

	/**
	 * Automagically completes a Geography name.
	 * 
	 * If entity is null, the current user's visible region is used as bounds.
	 * 
	 * @param query
	 * @return
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<GeographyDTO> autocompleteGeography(final Long orgId, String query) {

		logger.debug(query);
		
		Organization entity = null;
		if (orgId != null) {
			entity = permissionRepository.load(orgId, Organization.class);
			Validate.notNull(entity, "Invalid organization id: " + orgId);
			SpatialSecurity.checkPermission(getPrincipal(), entity);
		}
		
		final Set<Geography> geolist = new LinkedHashSet<Geography>();
		
		if (query.equals("ALL")) {
			
			geolist.addAll(getVisibleRegions(entity));
			
		} else {

			final String q = StringUtils.trimToNull(StringUtils.trimToEmpty(query).replaceAll(",+$", ""));

			if (q != null) {

				if (q.matches("^\\w\\w$")) {

					// match single state by abbreviatation
					final State state = geographyRepository.getStateByAbbreviation(q);
					if (state != null) {
						geolist.add(state);
					}
				} else if (q.matches("^\\w\\w,.*")) {

					// match something starting with state abbreviation
					final String[] fragments = q.split(",");
					final State state = geographyRepository.getStateByAbbreviation(fragments[0]);
					if (state != null) {
						if (fragments.length > 1 && StringUtils.trimToNull(fragments[1]) != null) {
							geolist.addAll(getGeographiesInState(state, fragments[1]));
						} else {
							geolist.add(state);
						}
					}
				} else if (q.matches("^\\w.*,\\s*\\w\\w$")) {

					// match something comma state abbreviation
					final String[] fragments = q.split(",");
					final String first = StringUtils.trimToNull(fragments[0]);
					if (first != null && fragments.length > 1) {
						final String second = StringUtils.trimToNull(fragments[1]);
						if (second != null) {
							final State state = geographyRepository.getStateByAbbreviation(second);
							if (state != null) {
								geolist.addAll(getGeographiesInState(state, first));
							}
						}
					}

				} else if (q.matches("^\\w\\w\\w.*,\\s*\\w\\w\\w.*")) {

					// match something comma something, each part being > 2
					// characters
					final String[] fragments = q.split(",");
					final String first = StringUtils.trimToNull(fragments[0]);
					if (first != null && fragments.length > 1) {
						final String second = StringUtils.trimToNull(fragments[1]);
						if (second != null) {

							String other = null;
							State state = null;

							// Find out which one is a state
							State s = geographyRepository.getGeography(first, State.class);
							if (s != null) {
								state = s;
								other = second;
							} else {
								s = geographyRepository.getGeography(second, State.class);
								if (s != null) {
									state = s;
									other = first;
								}
							}

							if (other != null && state != null) {
								geolist.addAll(getGeographiesInState(state, other));
							}
						}
					}
				} else {

					// Match anything
					final List<Geography> g = new ArrayList<Geography>();

					// Search region, state, county, zipcode
					if (StringUtils.isNumeric(q)) {
						g.addAll(geographyRepository.searchGeographies(q, Zipcode.class));
					} else {
						g.addAll(geographyRepository.searchGeographies(q, Region.class));
						if (g.size() != 1) {
							final List<State> states = geographyRepository.searchGeographies(q, State.class);
							if (states.size() == 1) {
								g.clear();
							}
							g.addAll(states);
						}
						if (g.size() != 1) {
							g.addAll(geographyRepository.searchGeographies(stripCountyFromQuery(q), County.class));
						}
					}

					geolist.addAll(g);
				}

				// If we have a single match, find the objects contained inside
				// it.
				if (geolist.size() == 1) {
					geolist.addAll(getContainedGeographies(geolist.iterator().next()));
				}
				
				if (entity == null) {
					for (Organization org : getPrincipal().getOrganizations()) {
						geolist.addAll(getVisibleRegions(org));
					}
				} else {
					geolist.addAll(getVisibleRegions(entity));
				}
			}
			
		}
		
		// Filter the list to the user's permissions
		// TODO: Unwind the collection and drill down if limited visibility.
		return entity == null ? filterSpatialCollection(getPrincipal(), geolist, true) : filterSpatialCollection(entity, geolist, true);
	}

	/**
	 * @return
	 *
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public Collection<GeographyDTO> autocompleteGeography() {
		return autocompleteGeography(null, "ALL");
	}
	*/
	
	@Transactional(readOnly = true)
	@RemoteMethod
	private List<? extends Geography> getGeographiesInState(final State state, final String text) {
		Validate.notNull(state);
		String query = StringUtils.trimToNull(text);
		final Class<? extends Geography> geographyType;
		if (StringUtils.isNumeric(query)) {
    		geographyType = Zipcode.class;
		} else {
    		geographyType = County.class;
    		query = stripCountyFromQuery(query);
		}
		List<? extends Geography> ret = geographyRepository.getGeographiesInState(state, query, geographyType, false);
		if (ret.size() == 0) {
			ret = geographyRepository.getGeographiesInState(state, query, geographyType, true);
		}
		return ret;
	}

	private String stripCountyFromQuery(String query) {
		String suffix = "county";
		String rtn = query.toLowerCase();
		while (suffix.length() > 0) {
			if (rtn.endsWith(suffix)) {
				rtn = StringUtils.trimToEmpty(query.substring(0, query.length() - suffix.length()));
				break;
			}
			suffix = suffix.substring(0, suffix.length() - 1);
		}
		return rtn;
	}

	/**
	 * @param g
	 * @return
	 */
	@Transactional(readOnly = true)
	private List<Geography> getContainedGeographies(final Geography g) {

		final List<Geography> containedList = new ArrayList<Geography>();

		if (g instanceof Region) {
			final Region r = (Region) g;
			if (r.getName().equals("United States")) {
				containedList.addAll(geographyRepository.getList(State.class));
			} else {
				containedList.addAll(geographyRepository.getIntersecting(r, State.class));
			}
		} else if (g instanceof State) {
			containedList.addAll(((State) g).getCounties());
		} else if (g instanceof County) {
			containedList.addAll(((County) g).getZipcodes());
		}
		Collections.sort(containedList);

		return containedList;
	}
	
	/**
	 * Gets the name of a geography that contains coordinate provided by a user.
	 * 
	 * @return
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
	 * @throws TransformException 
	 */
	@Secured("ROLE_USER")
	@Transactional(readOnly = true)
	@RemoteMethod
	public GeographyDTO getGeography(Double lat, Double lng, String feature) {
		Validate.notNull(lat, "Latitude must not be null!");
		Validate.notNull(lng, "Longitude must not be null!");
		
		Coordinate googleCoordinate = new Coordinate(lng, lat);
		
		Point queryPoint = geometryFactory.createPoint(googleCoordinate);
		
		Class<Geography> geographyClass = GeographyType.valueOf(feature.toUpperCase()).getGeoClass();
		
		final List<Geography> geographies = geographyRepository.getInteracting(queryPoint, geographyClass);
		Validate.notNull(geographies, "Null result for lat = " + lat + ", lng = " + lng);
		
		Geography geography = null;
		if (geographies.isEmpty()) {
			logger.debug("No {} found for lat {}, lng {}", new Object[] { feature, lat, lng });
		} else if (geographies.size() > 1) {
			logger.debug("More than one {} found for lat {}, lng {}", new Object[] { feature, lat, lng });
		} else {
    		geography = geographies.get(0);
		}
		
		GeographyDTO rtn = null;
		
		if (geography != null) {
    		rtn = new GeographyDTO(geography, getPrincipal());
		}
		
		return rtn;
	}
}
