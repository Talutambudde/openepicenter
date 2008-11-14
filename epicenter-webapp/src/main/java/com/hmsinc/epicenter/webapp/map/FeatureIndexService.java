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
package com.hmsinc.epicenter.webapp.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.hmsinc.epicenter.model.geography.County;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.geography.GeographyRepository;
import com.hmsinc.epicenter.model.geography.State;
import com.hmsinc.epicenter.model.geography.Zipcode;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * A thread-safe quadtree feature indexing service.
 *  
 * This is broken.  The quadtrees don't do referencing.
 * 
 * @author shade
 * @version $Id: FeatureIndexService.java 1157 2008-02-29 22:10:33Z steve.kondik $
 */
@Service
public class FeatureIndexService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private boolean featureIndexEnabled = false;

	@Resource
	private GeographyRepository geographyRepository;

	/*
	 * We'll use Quadtrees to keep an in-memory index of what features live
	 * where, so we don't have to demolish the database with bounding box
	 * queries.
	 */
	private final Map<Class<? extends Geography>, Quadtree> quadtrees = new HashMap<Class<? extends Geography>, Quadtree>();

	/*
	 * Our Quadtrees aren't really thread safe, but will be read-mostly so we
	 * can use locking.
	 */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock readLock = rwl.readLock();

	private final Lock writeLock = rwl.writeLock();

	public FeatureIndexService() {

		/*
		 * Initialize the Quadtrees for features we want to index..
		 */
		quadtrees.put(Zipcode.class, new Quadtree());
		quadtrees.put(County.class, new Quadtree());
		quadtrees.put(State.class, new Quadtree());
	}

	/**
	 * @param <T>
	 * @param searchEnv
	 * @param srid
	 * @param featureType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Geography> List<T> getFeatures(final Envelope searchEnv, final int srid,
			final Class<T> featureType) {

		Validate.notNull(searchEnv, "No search envelope specified.");
		Validate.notNull(srid, "No SRID specified.");
		Validate.notNull(featureType, "No feature type specified.");

		final List<T> containedFeatures;

		if (featureIndexEnabled) {
			if (quadtrees.containsKey(featureType)) {

				final List<Long> indexedFeatures;
				readLock.lock();

				try {
					indexedFeatures = quadtrees.get(featureType).query(searchEnv);
				} finally {
					readLock.unlock();
				}

				/*
				 * If nothing was found in the quadtree, hit the database and
				 * populate the index.
				 */
				if (indexedFeatures == null || indexedFeatures.size() == 0) {

					containedFeatures = queryFeatures(searchEnv, srid, featureType);
					if (containedFeatures != null && containedFeatures.size() > 0) {

						logger.trace("Adding features to index: {}", containedFeatures);

						writeLock.lock();
						try {
							for (T containedFeature : containedFeatures) {
								quadtrees.get(featureType).insert(containedFeature.getGeometry().getEnvelopeInternal(),
										containedFeature.getId());
							}
						} finally {
							writeLock.unlock();
						}
					}

				} else {

					containedFeatures = geographyRepository.getReferences(indexedFeatures, featureType);
					logger.trace("Using indexed features: {}", containedFeatures);
				}

			} else {

				// Just hit the database for non-static features.
				containedFeatures = queryFeatures(searchEnv, srid, featureType);
			}
		} else {
			containedFeatures = queryFeatures(searchEnv, srid, featureType);
		}
		return containedFeatures;
	}

	/**
	 * Gets features from the database.
	 * 
	 * @param <T>
	 * @param searchEnv
	 * @param srid
	 * @param featureType
	 * @return
	 */
	private <T extends Geography> List<T> queryFeatures(final Envelope searchEnv, final int srid,
			final Class<T> featureType) {
		return geographyRepository.getContained(searchEnv, srid, featureType);
	}

	/**
	 * @return the featureIndexEnabled
	 */
	public boolean isFeatureIndexEnabled() {
		return featureIndexEnabled;
	}

	/**
	 * @param featureIndexEnabled the featureIndexEnabled to set
	 */
	public void setFeatureIndexEnabled(boolean featureIndexEnabled) {
		this.featureIndexEnabled = featureIndexEnabled;
	}
	
}
