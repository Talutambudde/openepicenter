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
 * SpatialScanGrid.java
 *   
 * A grid for storing values on which to perform spatial scan analysis techniques. The grid
 * is composed of SpatialScanCell objects.
 * 
 * @author C. A. Cois
 * 
 * */

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.math.special.Gamma;

public class SpatialScanGrid {
	
	private final SpatialScanCell [][] grid;
	private final int rows;
	private final int columns;

	/** the sum of baseline values over the grid */
	private double B_all;
	/** the sum of count values over the grid */
	private double C_all;
	/** the total number of cells in the grid */
	private double numCells;
	
	/** q0 is the expected ratio of count to baseline under the null hypothesis */
	private double q0 = 1.0;// Note: 1.0 is the appropriate value when using expected value as 
	                        // the baseline.  When using population, q0 = sum_q/numCells, and is
							// calculated in the loops within analyzeGrid()
	
	/** P(D|H0), probability of the data given the null hypothesis */
	private double logP_D_H0;
	
	/** Initialize the internal array that will contain the grid data */
	public SpatialScanGrid(int rows, int columns) {
		this.grid = new SpatialScanCell[rows][columns];
		this.rows = rows;
		this.columns = columns;
	}
	
	/** Set the count and baseline values for a specified cell */
	public void setCell(int row, int column, double count, double baseline, Object ident) {
		grid[row][column] = new SpatialScanCell(count,baseline,ident);
	}
	
	/** Set the count value for a specified cell */
	public void setCellCount(int row, int column, double count) {
		grid[row][column].setCount(count);
	}
	
	/** Analyze the grid to calculate values for C_all and B_all */
	public void analyzeGrid() {
		B_all = 0.0; 
		C_all = 0.0;
		double sum_q = 0.0;
		
		// Iterate through the grid, summing all baseline and count values. We also 
		// calculate the sum of C/B ratios, for possible future use (these would be 
		// necessary to use population values as the baseline)
		for(int i = 0;i < rows;i++) {
			for(int j = 0;j < columns;j++) {
				
				B_all += grid[i][j].getBaseline();
				C_all += grid[i][j].getCount();
				
				// increment sum_q if baseline != 0 (to avoid NaN)
				if(grid[i][j].getBaseline() > 0.0) {
					sum_q += grid[i][j].getCount()/grid[i][j].getBaseline();
				}
				numCells++;
			}
		}
		// Disabled because we use expected value as the baseline...in this case, q0
		// is hard-coded to 1.0
		// q0 = sum_q/numCells;
		
		double alpha_all = q0*B_all;
		double beta_all = B_all;
		
		// calculate log likelihood of the null hypothesis
		
		/** Note: Individual terms in this equation are too large, and return
		 * infinite results. So, we reformulate the equation by taking its 
		 * natural log and finding its exponential at the end of calculation.
		 * 
		 *    Thus the original equation (Neill et al, page 3)
		 *    
		 *    P(D|H0) ~      (beta_all)^alpha_all * Gamma(alpha_all + C_all)
		 *              --------------------------------------------------------
		 *              (beta_all + B_all)^(alpha_all + C_all) * Gamma(alpha_all)
		 *              
		 *    becomes
		 *                  
		 * log(P(D|H0)) ~     [ ( alpha_all * log(beta_all) ) + logGamma(alpha_all + C_all) ] - 
		 *		  		  [ ( (alpha_all + C_all) * log(beta_all + B_all) ) + logGamma(alpha_all) ]
		 *              
		 */	
				
		logP_D_H0 = ( (alpha_all*Math.log(beta_all)) + Gamma.logGamma((alpha_all+C_all)) ) 
		   - ( ( (alpha_all + C_all) * (Math.log(beta_all + B_all))) + Gamma.logGamma(alpha_all) );
	}
	
	/** Return the likelihood of the null hypothesis, P(D|H0) */
	public double getNullHypothesisLogLikelihood() {
		return logP_D_H0;
	}
	
	/** Return the value of q0 */
	public double getQ0() {
		return q0;
	}
	
	/** Return the value of B_all */
	public double getB_all() {
		return B_all;
	}
	
	/** return the value of C_all */
	public double getC_all() {
		return C_all;
	}
	
	/** return the count value for the specified cell */
	public double getCount(int row, int column) {
		return grid[row][column].getCount();
	}
	
	/** return the baseline value for the specified cell */
	public double getBaseline(int row, int column) {
		return grid[row][column].getBaseline();
	}
	
	/** return the identifier of the specified cell */
	public Object getIdentifier(int row, int column) {
		return grid[row][column].getIdentifier();
	}
	
	/** return the number of rows in the grid */
	public int getRows() {
		return rows;
	}
	
	/** return the number of columns in the grid */
	public int getColumns() {
		return columns;
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