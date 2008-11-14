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
package com.hmsinc.epicenter.spatial.analysis;

/**
 * SpatialScanRegion.java
 *   
 * A rectangular region within a SpatialScanGrid, encompassing m x n cells.  The 
 * region is defined by two points on the 2D grid, indicating the upper left and 
 * lower right corners of the region in grid-based coordinates. Regions include 
 * count and baseline values equal to the sum of the respective values of their 
 * contained cells. 
 * 
 * @author C. A. Cois
 * 
 * */

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

class SpatialScanRegion implements Comparable<SpatialScanRegion> {

	private int rowInitial;
	private int rowFinal;
	private int columnInitial;
	private int columnFinal;
	private int count;
	private int baseline;
	private double likelihood;
	private double posteriorProbability;
	private double includingRegionsPosteriorProbabilitySum;
	private double score;

	SpatialScanRegion(int rowInitial, int rowFinal, int columnInitial, int columnFinal) {
		this.rowInitial = rowInitial;
		this.rowFinal = rowFinal;
		this.columnInitial = columnInitial;
		this.columnFinal = columnFinal;
	}

	/*
	 * public int compareTo(Object o) { if(this.score <
	 * ((SpatialScanRegion)o).getScore()) return 1; else if(this.score ==
	 * ((SpatialScanRegion)o).getScore()) return 0; else return -1; }
	 */

	/** return the summed count within this region */
	public int getCount() {
		return count;
	}

	/** return the summed baseline within this region */
	public int getBaseline() {
		return baseline + 1;
	}

	/** return the coordinates of this region */
	public int[] getCoordinates() {
		int[] coords = new int[4];
		coords[0] = rowInitial;
		coords[1] = rowFinal;
		coords[2] = columnInitial;
		coords[3] = columnFinal;
		return coords;
	}

	/**
	 * Function that creates and returns an iteratable set of coordinates for
	 * all cells contained within this region
	 */
	public Set<int[]> getSetOfContainedCellCoordinates() {
		Set<int[]> coordSet = new HashSet<int[]>();

		for (int x = columnInitial; x <= columnFinal; x++) {
			for (int y = rowInitial; y <= rowFinal; y++) {
				int[] coords = new int[2];
				coords[0] = x;
				coords[1] = y;
				coordSet.add(coords);
			}
		}
		return coordSet;
	}

	// returns the geometric area of the region
	public int getArea() {
		return (rowFinal - rowInitial + 1) * (columnFinal - columnInitial + 1);
	}

	public double getLikelihood() {
		return likelihood;
	}

	public double getPosteriorProbability() {
		return posteriorProbability;
	}

	public double getIncludingRegionsPosteriorProbabilitySum() {
		return includingRegionsPosteriorProbabilitySum;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setBaseline(int baseline) {
		this.baseline = baseline;
	}

	public void setCoordinates(int rowInitial, int rowFinal, int columnInitial, int columnFinal) {
		this.rowInitial = rowInitial;
		this.rowFinal = rowFinal;
		this.columnInitial = columnInitial;
		this.columnFinal = columnFinal;
	}

	public void setLikelihood(double likelihood) {
		this.likelihood = likelihood;
	}

	public void setPosteriorProbability(double prob) {
		this.posteriorProbability = prob;
	}

	public void setIncludingRegionsPosteriorProbabilitySum(double prob) {
		this.includingRegionsPosteriorProbabilitySum = prob;
	}

	public void setScore(double sc) {
		this.score = sc;
	}

	public double getScore() {
		return this.score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(SpatialScanRegion rhs) {
		return new CompareToBuilder().append(score, rhs.getScore()).append(getCoordinates(), rhs.getCoordinates())
				.toComparison();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 333).append(rowInitial).append(rowFinal).append(columnInitial).append(columnFinal).toHashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}