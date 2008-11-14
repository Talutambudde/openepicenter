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
package com.hmsinc.epicenter.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.hmsinc.epicenter.classifier.ClassificationEngine;
import com.hmsinc.epicenter.classifier.ClassifierFactory;
import com.hmsinc.epicenter.classifier.NamedItemClassificationEngine;
import com.hmsinc.epicenter.model.analysis.AnalysisRepository;
import com.hmsinc.epicenter.model.analysis.DataType;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.analysis.classify.ClassificationTarget;
import com.hmsinc.epicenter.model.analysis.classify.Classifier;
import com.hmsinc.epicenter.model.analysis.classify.ClassifierMetadata;
import com.hmsinc.epicenter.model.attribute.AttributeRepository;
import com.hmsinc.epicenter.model.attribute.PatientClass;
import com.hmsinc.epicenter.model.health.Interaction;
import com.hmsinc.epicenter.model.initialization.UpgradeTasks;
import com.hmsinc.epicenter.util.ReflectionUtils;

/**
 * Handles classification of Interactions and management of Classifier
 * objects.
 * 
 * Classifiers are defined in XML, but also require a representation in the
 * database. Additionally, we need to be able to handle classifiers that are not
 * part of EpiCenter.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id: ClassificationService.java 1795 2008-06-27 18:47:54Z steve.kondik $
 */
@Service
public class ClassificationService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private AnalysisRepository analysisRepository;

	@Resource
	private AttributeRepository attributeRepository;

	@Resource
	private UpgradeTasks upgradeTasks;

	@Resource
	private PlatformTransactionManager transactionManager;

	@Resource
	private JAXBContext jaxbContext;

	@Resource
	private Cache classifierCache;
	
	private final Map<String, ClassificationEngine> classifiers = new HashMap<String, ClassificationEngine>();

	private org.springframework.core.io.Resource configuration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@PostConstruct
	public void init() throws Exception {

		logger.info("Initializing classifiers..");
		new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.springframework.transaction.support.TransactionCallbackWithoutResult#doInTransactionWithoutResult(org.springframework.transaction.TransactionStatus)
			 */
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {

				// Configure default classifiers
				final List<Classifier> allClassifiers = analysisRepository.getList(Classifier.class);

				try {

					final ClassifierMetadata cm = (ClassifierMetadata) jaxbContext.createUnmarshaller().unmarshal(
							configuration.getInputStream());

					for (Classifier classifier : cm.getClassifiers()) {

						try {
							final ClassificationEngine engine = ClassifierFactory.createClassifier(classifier.getResource());

							if (engine != null) {

								Validate.isTrue(engine.getName().equals(classifier.getName()), "Classifier names must match! (configured: "
										+ classifier.getName() + " actual: " + engine.getName());

								boolean isInstalled = false;
								for (Classifier installed : allClassifiers) {
									if (installed.getName().equals(engine.getName()) && installed.getVersion().equals(engine.getVersion())) {
										isInstalled = true;
										classifier = installed;
										break;
									}
								}
								
								classifiers.put(engine.getName(), engine);
								
								if (!isInstalled) {

									// Copy the properties and create
									// Classification objects
									classifier.setVersion(engine.getVersion());
									classifier.setDescription(engine.getDescription());

									logger.info("Installing classifier: {}", classifier.getName());
									
									final SortedSet<Classification> classifications = new TreeSet<Classification>();
									for (String category : engine.getCategories()) {
										if (!category.equalsIgnoreCase("Other")) {
											classifications.add(new Classification(classifier, category));
										}
									}
									classifier.setClassifications(classifications);

									analysisRepository.save(classifier);
									
								}
							}
						} catch (IOException e) {
							logger.warn(e.getMessage());
						}
					}

					
					// Load the and initialize any unconfigured classifiers..
					for (Classifier classifier : allClassifiers) {
						if (classifier.isEnabled() && !classifiers.containsKey(classifier.getName())) {

							try {
								final ClassificationEngine engine = ClassifierFactory.createClassifier(classifier.getResource());
								if (engine != null) {
									classifiers.put(engine.getName(), engine);
								}
							} catch (IOException e) {
								logger.warn(e.getMessage());
							}
						}
					}
					
					
					// Configure DataTypes
					final List<DataType> dataTypes = cm.getDataTypes();

					// Hydrate the PatientClasses because the XML descriptor
					// will only reference the name
					// Also remove any targets with unconfigured classifiers.
					for (DataType dataType : dataTypes) {
						logger.debug("Data type: " + dataType.toString());

						final List<ClassificationTarget> unconfigured = new ArrayList<ClassificationTarget>();

						for (ClassificationTarget ct : dataType.getTargets()) {

							if (ct.getClassifier() == null || ct.getClassifier().getId() == null && !classifiers.containsKey(ct.getClassifier().getName())) {
								logger.debug("Skipping unconfigured classification target");
								unconfigured.add(ct);
							} else {
								final PatientClass pc = attributeRepository.getPatientClassByName(ct.getPatientClass().getName());
								Validate.notNull(pc, "Unknown patient class: " + ct.getPatientClass().toString());
								ct.setPatientClass(pc);
								
								if (classifiers.containsKey(ct.getClassifier().getName())) {
									ct.setClassifier(analysisRepository.getClassifierByName(ct.getClassifier().getName()));
								}
							}
						}

						dataType.getTargets().removeAll(unconfigured);
					}

					upgradeTasks.validateAttributes(dataTypes, DataType.class, analysisRepository);

				} catch (JAXBException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				
			}
		});

	}

	/**
	 * Classify an Interaction using a specific ClassificationTarget
	 * 
	 * @param interaction
	 * @param target
	 * @return
	 */
	@Transactional(readOnly = true)
	public Set<Classification> classify(final Interaction interaction, final ClassificationTarget target) {
		
		final Set<Classification> classifications = new HashSet<Classification>();
		
		if (classifiers.containsKey(target.getClassifier().getName())) {

			final ClassificationEngine c = classifiers.get(target.getClassifier().getName());
			logger.trace("Classifier: {}", c.getName());
			
			if (c instanceof NamedItemClassificationEngine) {
				
				// Use a LinkedHashMap because we depend on order in the RegexClassifier.
				final Map<String, Object> properties = new LinkedHashMap<String, Object>();
				for (String propertyName : target.getPropertyName().split(",")) {

					properties.put(propertyName, ReflectionUtils.getProperty(interaction, Object.class, propertyName));
					final List<String> categories = ((NamedItemClassificationEngine)c).classifyNamedItems(properties);
					if (categories.size() > 0) {
						classifications.addAll(findClassifications(target.getClassifier(), categories));
					}
				}
			} else {
				
				final String property = ReflectionUtils.getProperty(interaction, String.class, target.getPropertyName());
				if (property != null) {
					final List<String> categories = c.classify(property);
					if (categories.size() > 0) {
						classifications.addAll(findClassifications(target.getClassifier(), categories));
					}
				}
			}
		}
		
		return classifications;
	}
	
	/**
	 * Classify a Interaction using metadata configuration.
	 * 
	 * @param interaction
	 * @return the classifications
	 */
	@Transactional(readOnly = true)
	public Set<Classification> classify(final Interaction interaction) {

		final Set<Classification> classifications = new HashSet<Classification>();
		if (interaction != null) {
			
			
			// Search for a matching Classifier..
			for (ClassificationTarget target : getClassifiersForInteraction(interaction)) {
				classifications.addAll(classify(interaction, target));
			}
		}
		
		logger.debug("classifications: {}", classifications);
		return classifications;
	}

	/**
	 * @param interaction
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<ClassificationTarget> getClassifiersForInteraction(final Interaction interaction) {
		
		final List<ClassificationTarget> cls;
		
		final Element e = classifierCache.get(interaction.getPatientClass().getAbbreviation() + "_" + interaction.getClass().getName());
		
		if (e == null) {
			cls = analysisRepository.getClassifiersForInteraction(interaction);
			classifierCache.put(new Element(interaction.getClass().getName(), cls));
		} else {
			cls = (List<ClassificationTarget>)e.getObjectValue();
		}
		
		return cls;
	}
	
	/**
	 * Get the Classification objects for the categories given.
	 * 
	 * @param classifier
	 * @param categories
	 * @return
	 */
	private Collection<Classification> findClassifications(Classifier classifier, List<String> categories) {
		final Set<Classification> ret = new HashSet<Classification>();
		for (Classification classification : classifier.getClassifications()) {
			if (categories.contains(classification.getCategory())) {
				ret.add(analysisRepository.load(classification.getId(), Classification.class));
			}
		}
		return ret;
	}

	/**
	 * @return the classifiers
	 */
	public Map<String, ClassificationEngine> getClassifiers() {
		return classifiers;
	}

	/**
	 * @param configuration
	 *            the configuration to set
	 */
	@Required
	public void setConfiguration(org.springframework.core.io.Resource configuration) {
		this.configuration = configuration;
	}

}
