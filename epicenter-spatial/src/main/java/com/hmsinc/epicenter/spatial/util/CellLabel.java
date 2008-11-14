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
package com.hmsinc.epicenter.spatial.util;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author shade
 *
 */
public class CellLabel {

	private final Integer fid;
	
	private final String label;
	
	private final Geometry geometry;

	/**
	 * @param label
	 * @param point
	 */
	public CellLabel(Integer fid, String label, Geometry geometry) {
		super();
		this.fid = fid;
		this.label = label;
		this.geometry = geometry;
	}

	/**
	 * @return the fid
	 */
	public Integer getFid() {
		return fid;
	}


	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the geometry
	 */
	public Geometry getGeometry() {
		return geometry;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("label", label).append("geometry", geometry).toString();
	}
}
