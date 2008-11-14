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
package com.hmsinc.epicenter.tools.importexport;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.hmsinc.epicenter.model.util.ExportData;
import com.hmsinc.epicenter.tools.RunnableTool;

/**
 * Handles import/export of XML data from a database.
 * 
 * Requires a named query on the entity to export such as
 * "exportDataConnection".
 * 
 * This needs more work to be useful for anything other than providers.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:ImportExportUtils.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
public class ImportExport implements RunnableTool {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private JAXBContext jaxbContext;

	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = "epicenter-model")
	private EntityManager entityManager;
	
	private String[] arguments;

	/**
	 * @return the arguments
	 */
	public String[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            the arguments to set
	 */
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public void run() {

		if (arguments.length != 4) {
			logger.error(getUsage());
		} else {

			if (arguments[1].equalsIgnoreCase("export")) {

				try {
					final Marshaller m = jaxbContext.createMarshaller();
					m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

					final OutputStream os = new FileOutputStream(arguments[3]);

					logger.info("Executing query..");

					m.marshal(new ExportData(entityManager.createNamedQuery("export" + arguments[2]).getResultList()),
							os);

					os.close();

					logger.info("Export complete.");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else if (arguments[1].equalsIgnoreCase("import")) {
				try {
					final InputStream is = new FileInputStream(arguments[3]);
					final Unmarshaller u = jaxbContext.createUnmarshaller();

					logger.info("Import starting..");
					final ExportData export = (ExportData) u.unmarshal(is);

					for (Object o : export.getData()) {
						logger.info("Saving: {}", o);
						entityManager.persist(o);
					}

					is.close();

					logger.info("Import complete.");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				logger.error(getUsage());
			}
		}
	}

	public String getUsage() {
		return "ImportExport [import/export] objectName filename";
	}
}
