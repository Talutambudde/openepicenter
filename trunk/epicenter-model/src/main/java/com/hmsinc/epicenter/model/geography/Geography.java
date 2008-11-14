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
package com.hmsinc.epicenter.model.geography;

import static javax.persistence.GenerationType.AUTO;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Parameter;

import com.hmsinc.epicenter.model.analysis.QueryableAttribute;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Base class for Geography entities.
 * 
 * We use TABLE_PER_CLASS because GeoServer needs a separate table per featuretype.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Geography.java 144 2007-05-19 07:57:56Z steve.kondik $
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "GEOGRAPHY")
@org.hibernate.annotations.Table(appliesTo = "GEOGRAPHY", indexes = {
		@org.hibernate.annotations.Index(name = "IDX_GEOGRAPHY_1", columnNames = "TYPE"),
		@org.hibernate.annotations.Index(name = "IDX_GEOGRAPHY_2", columnNames = "NAME"),
		@org.hibernate.annotations.Index(name = "IDX_GEOGRAPHY_3", columnNames = "ABBREVIATION"),
		@org.hibernate.annotations.Index(name = "IDX_GEOGRAPHY_4", columnNames = "ID_GEO_STATE") } )
@DiscriminatorColumn(name = "TYPE", discriminatorType=DiscriminatorType.CHAR)
@DiscriminatorValue("X")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, include = "all", region = "com.hmsinc.epicenter.model.GeographyCache")
@NamedQueries( { 
	@NamedQuery(name = "getGeographyByName", query = "from Geography g where lower(name) like lower(:name)") } )
@XmlType(name = "Geography", namespace = "http://epicenter.hmsinc.com/model")
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "geography", namespace = "http://epicenter.hmsinc.com/model")
public abstract class Geography implements Serializable, Comparable<Geography>, QueryableAttribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3349175450536066328L;

	protected Long id;

	private String name;

	private Geometry geometry;
	
	private Geometry centroid;

	private Long population;
	
	/**
	 * @return the id
	 */
	@XmlAttribute(required = true)
	@Id
	@Column(name = "ID", nullable = false, insertable = true, updatable = true)
	@GenericGenerator(name = "generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "SEQ_GEOGRAPHY") })
	@GeneratedValue(strategy = AUTO, generator = "generator")
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.model.warehouse.geo.Geography#getName()
	 */
	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	@Column(name = "NAME", unique = false, nullable = false, insertable = true, updatable = true, length = 100)
	@NaturalId
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.model.warehouse.geo.Geography#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * The backing type of this column is determined by the Hibernate Spatial
	 * dialect.
	 * 
	 * In order for this property to be "lazy", compile-time bytecode
	 * instrumentation is required. The Maven build should do this
	 * automatically.
	 * 
	 */
	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	@Column(name = "GEOMETRY", unique = false, nullable = true, insertable = true, updatable = true)
	@org.hibernate.annotations.Type(type = "geometry")
	@XmlJavaTypeAdapter(com.hmsinc.hibernate.spatial.jaxb2.GeometryTypeAdapter.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
	public Geometry getGeometry() {
		return geometry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hmsinc.model.warehouse.geo.Geography#setGeometry(com.vividsolutions.jts.geom.Geometry)
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}
	
	/**
	 * @return the centroid
	 */
	@XmlElement(required = true, namespace = "http://epicenter.hmsinc.com/model")
	@Column(name = "CENTROID", unique = false, nullable = true, insertable = true, updatable = true)
	@Basic(fetch = FetchType.LAZY)
	@org.hibernate.annotations.Type(type = "geometry")
	@XmlJavaTypeAdapter(com.hmsinc.hibernate.spatial.jaxb2.GeometryTypeAdapter.class)
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region = "com.hmsinc.epicenter.model.GeographyCache")
	public Geometry getCentroid() {
		if (centroid == null && getGeometry() != null) {
			centroid = getGeometry().getCentroid();
			centroid.setSRID(getGeometry().getSRID());
		}
		return centroid;
	}

	/**
	 * @param centroid the centroid to set
	 */
	public void setCentroid(Geometry centroid) {
		this.centroid = centroid;
	}
	
	/**
	 * @return the population
	 */
	@XmlAttribute
	@Column(name = "POPULATION", nullable = true, insertable = true, updatable = true)
	public Long getPopulation() {
		return population;
	}

	/**
	 * @param population the population to set
	 */
	public void setPopulation(Long population) {
		this.population = population;
	}

	@Transient
	public abstract String getDisplayName();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(id).toHashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;
		if (!(obj instanceof Geography)) {
			ret = false;
		} else if (this == obj) {
			ret = true;
		} else {
			final Geography g = (Geography) obj;
			ret = id.equals(g.getId());
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
		return new ToStringBuilder(this).append("id", id).append("displayName", getDisplayName()).append("population", population).toString();
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Geography rh) {
		return new CompareToBuilder().append(getDisplayName(), rh.getDisplayName()).append(getId(), rh.getId()).toComparison();
	}
}