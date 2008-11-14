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
package com.hmsinc.epicenter.model.surveillance;

import static com.hmsinc.epicenter.util.FormatUtils.formatDateTime;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.hmsinc.ts4j.analysis.ResultType;
import com.hmsinc.epicenter.model.analysis.AnalysisParameters;
import com.hmsinc.epicenter.model.analysis.classify.Classification;
import com.hmsinc.epicenter.model.geography.Geography;
import com.hmsinc.epicenter.model.permission.Organization;
import com.hmsinc.epicenter.model.workflow.Event;
import com.hmsinc.epicenter.model.workflow.EventDisposition;
import com.hmsinc.ts4j.TimeSeriesEntry;

/**
 * An Anomaly is generated when an Algorithm exceeds it's threshold.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:SurveillanceEvent.java 205 2007-09-26 16:52:01Z steve.kondik $
 */
@Entity
@Table(name = "ANOMALY")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@org.hibernate.annotations.Table(appliesTo = "ANOMALY", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_ANOMALY_1", columnNames = "ANALYSIS_TIMESTAMP") } )
@NamedQueries( { @NamedQuery(name = "getAnomalies", query = "from Anomaly where analysisTimestamp between :startDate and :endDate order by analysisTimestamp desc") })
public class Anomaly extends Event implements SurveillanceObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3721396635382232088L;

	private SurveillanceTask task;

	private SurveillanceMethod method;

	private SurveillanceSet set;
	
	private Classification classification;

	private SurveillanceResult result;

	private DateTime analysisTimestamp;
	
	private double observedValue = Double.NaN;
	
	private double observedThreshold = Double.NaN;
	
	private double totalValue = Double.NaN;
	
	private double totalThreshold = Double.NaN;
	
	private double normalizedValue = Double.NaN;
	
	private double normalizedThreshold = Double.NaN;
	
	private int count = 1;
	
	private DateTime lastOccurrence = new DateTime();
	
	/**
	 * 
	 */
	public Anomaly() {
		super();
	}

	/**
	 * @param description
	 * @param geography
	 * @param organization
	 */
	public Anomaly(String description, Geography geography, EventDisposition disposition, Organization organization) {
		super(description, geography, disposition, organization);
	}

	/**
	 * @param description
	 * @param geography
	 * @param organization
	 * @param task
	 * @param method
	 * @param classification
	 * @param result
	 */
	public Anomaly(String description, Geography geography, EventDisposition disposition, Organization organization, SurveillanceTask task,
			SurveillanceMethod method, SurveillanceSet set, Classification classification, SurveillanceResult result, DateTime analysisTimestamp) {
		super(description, geography, disposition, organization);
		this.task = task;
		this.method = method;
		this.set = set;
		this.classification = classification;
		this.result = result;
		this.analysisTimestamp = analysisTimestamp;
	}
	
	@Transient
	public double getPredictedObservedValue() {
		TimeSeriesEntry last = result.getResults().get(SurveillanceResultType.ACTUAL).last();
		return last.getDoubleProperty(ResultType.PREDICTED);
	}

	/**
	 * @return the task
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_SURVEILLANCE_TASK", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ANOMALY_1")
	public SurveillanceTask getTask() {
		return task;
	}

	/**
	 * @param task
	 *            the task to set
	 */
	public void setTask(SurveillanceTask task) {
		this.task = task;
	}

	/**
	 * @return the method
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_SURVEILLANCE_METHOD", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ANOMALY_2")
	public SurveillanceMethod getMethod() {
		return method;
	}

	/**
	 * @param method
	 *            the method to set
	 */
	public void setMethod(SurveillanceMethod method) {
		this.method = method;
	}

	/**
	 * @return the set
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_SURVEILLANCE_SET", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ANOMALY_4")
	public SurveillanceSet getSet() {
		return set;
	}

	/**
	 * @param set the set to set
	 */
	public void setSet(SurveillanceSet set) {
		this.set = set;
	}

	/**
	 * @return the classification
	 */
	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_CLASSIFICATION", unique = false, nullable = false, insertable = true, updatable = true)
	@org.hibernate.annotations.ForeignKey(name = "FK_ANOMALY_3")
	public Classification getClassification() {
		return classification;
	}

	/**
	 * @param classification
	 *            the classification to set
	 */
	public void setClassification(Classification classification) {
		this.classification = classification;
	}

	/**
	 * @return the result
	 */
	@Column(name = "RESULT", unique = false, nullable = true, insertable = true, updatable = true)
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "com.hmsinc.hibernate.xml.XMLType", parameters = { @Parameter(name = "contextPath", value = "com.hmsinc.epicenter.model:com.hmsinc.ts4j") })
	public SurveillanceResult getResult() {
		return result;
	}

	/**
	 * @param result
	 *            the result to set
	 */
	public void setResult(SurveillanceResult result) {
		this.result = result;
	}

	/**
	 * @return the analysisTimestamp
	 */
	@Type(type = "joda")
	@Column(name = "ANALYSIS_TIMESTAMP", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getAnalysisTimestamp() {
		return analysisTimestamp;
	}

	/**
	 * @param analysisTimestamp the analysisTimestamp to set
	 */
	public void setAnalysisTimestamp(DateTime analysisTimestamp) {
		this.analysisTimestamp = analysisTimestamp;
	}

	/*
	 * @return the count
	 */
	@Column(name = "COUNT", unique = false, nullable = false, insertable = true, updatable = true)
	public int getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(int count) {
		this.count = count;
	}

	/**
	 * @return the lastOccurrence
	 */
	@Type(type = "joda")
	@Column(name = "LAST_OCCURRENCE", unique = false, nullable = false, insertable = true, updatable = true)
	public DateTime getLastOccurrence() {
		return lastOccurrence;
	}

	/**
	 * @param lastOccurrence the lastOccurrence to set
	 */
	public void setLastOccurrence(DateTime lastOccurrence) {
		this.lastOccurrence = lastOccurrence;
	}

	/**
	 * @return the observedValue
	 */
	@Column(name = "OBSERVED_VALUE", unique = false, nullable = true, insertable = true, updatable = true)
	public double getObservedValue() {
		if (Double.isNaN(observedValue) && result != null && result.getResults().containsKey(SurveillanceResultType.ACTUAL)) {
			observedValue = result.getResults().get(SurveillanceResultType.ACTUAL).getValue(getAnalysisTimestamp());
		}
		return observedValue;
	}

	@SuppressWarnings("unused")
	private void setObservedValue(double observedValue) {
		this.observedValue = observedValue;
	}

	/**
	 * @return the thresholdValue
	 */
	@Column(name = "OBSERVED_THRESHOLD", unique = false, nullable = true, insertable = true, updatable = true)
	public double getObservedThreshold() {
		if (Double.isNaN(observedThreshold) && result != null && result.getResults().containsKey(SurveillanceResultType.ACTUAL)) {
			final TimeSeriesEntry entry = result.getResults().get(SurveillanceResultType.ACTUAL).get(getAnalysisTimestamp());
			Validate.notNull(entry, "Entry was null for " + formatDateTime(getAnalysisTimestamp()));
			if (entry.hasProperty(ResultType.THRESHOLD)) {
				observedThreshold = entry.getDoubleProperty(ResultType.THRESHOLD);
			}
		}
		return observedThreshold;
	}

	@SuppressWarnings("unused")
	private void setObservedThreshold(double observedThreshold) {
		this.observedThreshold = observedThreshold;
	}

	/**
	 * @return the totalValue
	 */
	@Column(name = "TOTAL_VALUE", unique = false, nullable = true, insertable = true, updatable = true)
	public double getTotalValue() {
		if (Double.isNaN(totalValue) && result != null && result.getResults().containsKey(SurveillanceResultType.TOTAL)) {
			totalValue = result.getResults().get(SurveillanceResultType.TOTAL).getValue(getAnalysisTimestamp());
		}
		return totalValue;
	}

	@SuppressWarnings("unused")
	private void setTotalValue(double totalValue) {
		this.totalValue = totalValue;
	}

	/**
	 * @return the totalThreshold
	 */
	@Column(name = "TOTAL_THRESHOLD", unique = false, nullable = true, insertable = true, updatable = true)
	public double getTotalThreshold() {
		if (Double.isNaN(totalThreshold) && result != null && result.getResults().containsKey(SurveillanceResultType.TOTAL)) {
			final TimeSeriesEntry entry = result.getResults().get(SurveillanceResultType.TOTAL).get(getAnalysisTimestamp());
			Validate.notNull(entry, "Entry was null for " + formatDateTime(getAnalysisTimestamp()));
			if (entry.hasProperty(ResultType.THRESHOLD)) {
				totalThreshold = entry.getDoubleProperty(ResultType.THRESHOLD);
			}
		}
		return totalThreshold;
	}

	@SuppressWarnings("unused")
	private void setTotalThreshold(double totalThreshold) {
		this.totalThreshold = totalThreshold;
	}

	/**
	 * @return the normalizedValue
	 */
	@Column(name = "NORMALIZED_VALUE", unique = false, nullable = true, insertable = true, updatable = true)
	public double getNormalizedValue() {
		if (Double.isNaN(normalizedValue) && result != null && result.getResults().containsKey(SurveillanceResultType.NORMALIZED)) {
			normalizedValue = result.getResults().get(SurveillanceResultType.NORMALIZED).getValue(getAnalysisTimestamp());
		}
		return normalizedValue;
	}

	@SuppressWarnings("unused")
	private void setNormalizedValue(double normalizedValue) {
		this.normalizedValue = normalizedValue;
	}

	/**
	 * @return the normalizedThreshold
	 */
	@Column(name = "NORMALIZED_THRESHOLD", unique = false, nullable = true, insertable = true, updatable = true)
	public double getNormalizedThreshold() {
		if (Double.isNaN(normalizedThreshold) && result != null && result.getResults().containsKey(SurveillanceResultType.NORMALIZED)) {
			final TimeSeriesEntry entry = result.getResults().get(SurveillanceResultType.NORMALIZED).get(getAnalysisTimestamp());
			Validate.notNull(entry, "Entry was null for " + formatDateTime(getAnalysisTimestamp()) + "  [" );
			if (entry.hasProperty(ResultType.THRESHOLD)) {
				normalizedThreshold = entry.getDoubleProperty(ResultType.THRESHOLD);
			}
		}
		return normalizedThreshold;
	}

	@SuppressWarnings("unused")
	private void setNormalizedThreshold(double normalizedThreshold) {
		this.normalizedThreshold = normalizedThreshold;
	}

	/**
	 * @return the parameters which can be used to recreate this anomaly
	 */
	@Transient
	public AnalysisParameters getAnalysisParameters() {
		final AnalysisParameters p = new AnalysisParameters(analysisTimestamp.minusDays(1), analysisTimestamp);
		p.getClassifications().add(classification);
		p.setLocation(getTask().getLocation());
		p.setContainer(getGeography());	
		p.setDataType(getSet().getDatatype());
		p.setAttributes(getSet().getAttributes());
		return p;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(final Anomaly g) {
		return new CompareToBuilder().append(getTimestamp(), g.getTimestamp()).append(getId(), g.getId()).toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(133, 41).append(getId()).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		boolean ret = false;
		if (o instanceof Anomaly == false) {
			ret = false;
		} else if (this == o) {
			ret = true;
		} else {
			final Anomaly ag = (Anomaly) o;
			ret = new EqualsBuilder().append(getId(), ag.getId()).isEquals();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("analysisTimestamp", analysisTimestamp).append("classification", classification).append("task", task).append("method", method).append("set", set).toString();
	}
}
