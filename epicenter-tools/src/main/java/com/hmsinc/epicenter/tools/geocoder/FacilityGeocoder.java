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
package com.hmsinc.epicenter.tools.geocoder;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hmsinc.epicenter.model.geography.util.Geocoder;
import com.hmsinc.epicenter.model.provider.Facility;
import com.hmsinc.epicenter.model.provider.ProviderRepository;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Updates all Facility records with a gecoded geometry.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:FacilityGeocoder.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class FacilityGeocoder {

	private static final String[] CONTEXT_FILES = new String[] { "classpath:epicenter-utility-beans.xml" };

	private static ConfigurableApplicationContext appContext;

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		if (args.length == 5) {

			System.setProperty("db.driver", args[0]);
			System.setProperty("db.url", args[1]);
			System.setProperty("db.type", args[2]);
			System.setProperty("db.user", args[3]);
			System.setProperty("db.password", args[4]);

			appContext = new ClassPathXmlApplicationContext(CONTEXT_FILES);

			final ProviderRepository providerRepository = (ProviderRepository) appContext.getBean("providerRepository");
			Validate.notNull(providerRepository);

			final Geocoder geocoder = (Geocoder) appContext.getBean("geocoder");
			Validate.notNull(geocoder);

			final PlatformTransactionManager tm = (PlatformTransactionManager) appContext.getBean("transactionManager");
			Validate.notNull(tm);

			final TransactionTemplate template = new TransactionTemplate(tm);

			List<Facility> facilities = (List<Facility>) template.execute(new TransactionCallback() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.springframework.transaction.support.TransactionCallback#doInTransaction(org.springframework.transaction.TransactionStatus)
				 */
				public List<Facility> doInTransaction(TransactionStatus status) {
					return providerRepository.getList(Facility.class);
				}

			});

			for (final Facility facility : facilities) {
				if (facility.getGeometry() == null && facility.getAddress1() != null && facility.getCity() != null && facility.getState() != null && facility.getZipcode() != null) {
					template.execute(new TransactionCallbackWithoutResult() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see org.springframework.transaction.support.TransactionCallbackWithoutResult#doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus)
						 */
						@Override
						protected void doInTransactionWithoutResult(TransactionStatus status) {

							System.out.println(facility.toString());
							final Geometry geom = geocoder.geocode(facility.getAddress1(), facility.getCity(), facility
									.getState(), facility.getZipcode());
							if (geom != null) {
								facility.setGeometry(geom);
								providerRepository.update(facility);
							}
						}
					});

				}

			}

		} else {
			usage();
		}

	}

	public static void usage() {
		System.out.println("Usage: FacilityGeocoder driverClass databaseURL databaseType databaseUser databasePassword");
	}
}
