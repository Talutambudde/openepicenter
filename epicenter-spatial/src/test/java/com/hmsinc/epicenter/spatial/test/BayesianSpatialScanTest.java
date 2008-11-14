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
package com.hmsinc.epicenter.spatial.test;

import java.io.BufferedWriter;
import java.io.FileWriter;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.spatial.analysis.*;

// TODO: This test currently does not check the results against any gold standard, it just 
//       verifies error-free operation.

public class BayesianSpatialScanTest extends TestCase {
	
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public BayesianSpatialScanTest() {
		logger.debug("Initializing BayesianSpatialScanTest()...");	
	}
	
	public void testBayesianSpatialScan() throws Throwable {

		long start = System.currentTimeMillis();
		
		//initialize a grid
		int n = 22;
		int m = 20;
		BayesianSpatialScanStatistic BSSS = new BayesianSpatialScanStatistic();

		//int baseline = 100;//using a census population estimate baseline of 100, with counts generated for each cell between 0-10.
		int baseline = 5;//using expected count for each cell
		
		// create and initialize a random grid
		SpatialScanGrid grid = new SpatialScanGrid(n,m);
		grid = BSSS.populateRandomGrid(grid,1,10,baseline);
		
		// set some abnormally high counts
		grid.setCellCount(5, 5, 14);
		grid.setCellCount(5, 4, 14);
		grid.setCellCount(5, 6, 14);
		grid.setCellCount(4, 5, 14);
		grid.setCellCount(6, 5, 14);
		
		//TEST for NaN
		grid.setCell(6, 6, 0, 0, "NaNtest");
		grid.setCell(6, 7, 0, 0, "NaNtest2");
		grid.setCell(6, 7, 3, 0, "NaNtest2");
		grid.setCell(6, 7, 0, 3, "NaNtest2");
		
		// ** Run a scan generating all possible regions ** //
		PosteriorGrid pGrid = BSSS.runSpatialScan(grid, 5, 5);
		
		//print grid to file
		BufferedWriter out = new BufferedWriter(new FileWriter("target/SummedPosteriorResults.txt"));
		for(int i = 0;i < n;++i) {
			for(int j = 0;j < m;++j) {
				out.write(pGrid.getPosterior(i, j) + "|");
			}
			out.newLine();
		}
		out.close();
		
		long finish = System.currentTimeMillis();
		
		logger.debug("BSSS Algorithm took " + ((double)finish-(double)start)/1000.0 + " seconds to complete." );
	}
	
}