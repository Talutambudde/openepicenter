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
 * PosteriorGrid.java
 * 
 * A grid for storing output posterior probability values from a spatial scan
 * analysis technique.
 * 
 * @author C. A. Cois
 * 
 */

public class PosteriorGrid {

	private double[][] grid;
	private int rows;
	private int columns;
	private double posteriorProbNoOutbreak;

	/** Constructor - creates the grid and initializes all values within to 0.0 */
	public PosteriorGrid(int rows, int columns) {
		grid = new double[rows][columns];
		// initialize all probabilities in grid to 0.0
		for (int i = 0; i < rows; ++i) {
			for (int j = 0; j < columns; ++j) {
				grid[i][j] = 0.0;
			}
		}
		this.rows = rows;
		this.columns = columns;
	}

	/** sets the posterior value of the specified cell */
	public void setPosterior(int n, int m, double posteriorProb) {
		grid[n][m] = posteriorProb;
	}

	/** adds to the posterior value of the specified cell */
	public void incrementPosterior(int n, int m, double posteriorProb) {
		grid[n][m] += posteriorProb;
	}

	/** return the posterior of the specified cell */
	public double getPosterior(int row, int column) {
		return grid[row][column];
	}

	/** return the number of rows in the grid */
	public int getRows() {
		return rows;
	}

	/** return the number of columns in the grid */
	public int getColumns() {
		return columns;
	}

	/**
	 * return the probability of the null hypothesis, i.e. no outbreak has
	 * occurred
	 */
	public double getPosteriorProbNoOutbreak() {
		return posteriorProbNoOutbreak;
	}

	/** set the probability of the null hypothesis, i.e. no outbreak has occurred */
	public void setPosteriorProbNoOutbreak(double prob) {
		posteriorProbNoOutbreak = prob;
	}
}