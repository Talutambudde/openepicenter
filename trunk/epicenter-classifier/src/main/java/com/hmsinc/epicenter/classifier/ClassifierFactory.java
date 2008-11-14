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
package com.hmsinc.epicenter.classifier;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.lang.Validate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.xml.sax.SAXException;

import com.hmsinc.epicenter.classifier.config.ClassifierConfig;

/**
 * Creates classifiers based on an XML configuration document.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ClassifierFactory.java 533 2007-12-10 03:53:20Z steve.kondik $
 */
public class ClassifierFactory {

	private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	private static final Schema schema;

	static {
		try {
			final InputStream is = resolver.getResource("classpath:classifier.xsd").getInputStream();
			schema = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI).newSchema(new StreamSource(is));
			is.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a classifier using the given resource location.
	 * 
	 * @param location
	 * @return classificationEngine
	 */
	public static ClassificationEngine createClassifier(final String location) throws IOException {

		final Resource resource = resolver.getResource(location);
		return createClassifier(resource);
	}

	/**
	 * Creates a classifier using the given resource.
	 * 
	 * @param resource
	 * @return classificationEngine
	 */
	public static ClassificationEngine createClassifier(final Resource resource) throws IOException {

		Validate.notNull(resource, "Resource was null!");
		if (!resource.exists()) {
			throw new IOException("Resource " + resource.toString() + " does not exist.");
		}
		
		ClassificationEngine engine = null;

		try {

			engine = instantiateClassifier(resource);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return engine;

	}

	/**
	 * Creates the actual classifier.
	 * 
	 * @param resource
	 * @return classificationEngine
	 * @throws Exception
	 */
	private static ClassificationEngine instantiateClassifier(final Resource resource) throws Exception {

		final Unmarshaller u = JAXBContext.newInstance("com.hmsinc.epicenter.classifier.config").createUnmarshaller();

		// Enable validation
		u.setSchema(schema);

		final InputStream is = resource.getInputStream();
		final ClassifierConfig config = (ClassifierConfig) u.unmarshal(is);
		is.close();

		Validate.notNull(config, "Configuration was null!");
		Validate.notNull(config.getImplementation(), "No implementation was specified.");

		final Class<?> implementation = Class.forName(config.getImplementation());
		Validate.isTrue(ClassificationEngine.class.isAssignableFrom(implementation),
				"Implementation must be an instance of ClassificationEngine (was: " + config.getImplementation() + ")");

		final ClassificationEngine engine = (ClassificationEngine) implementation.newInstance();
		engine.init(config);

		return engine;
	}
}
