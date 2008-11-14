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
 * BayesianSpatialScanStatistic.java
 *   
 * An implementation of a Bayesian Spatial Scan Statistic, based on the research published by 
 * Neill, Moore, and Cooper in "A Bayesian Spatial Scan Statistic", 2006. 
 * 
 * References to equations/information in the paper will be included in comments when appropriate. 
 * 
 * @author C. A. Cois
 * 
 * */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.special.Gamma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BayesianSpatialScanStatistic {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	/** This value represents the probability of an outbreak in any region S. We use a simple
	 * "uniform region prior", assuming that this outbreak probability is the same for all regions. 
	 * 
	 * TODO: This value should be made an algorithm parameter.  This will also allow this value to 
	 * be deduced from observed historical time series', rather than estimated.  Further development 
	 * would remove the assumption of uniformity for region priors, allowing each region to have an 
	 * independent prior based on historical data. */
	static final double P1 = 0.005;
	static final double logP1 = Math.log(P1);
	
	/** Calculate the prior probability of no outbreaks occurring, or the probability og the null
	 * hypothesis, P(H0). */
	static final double P_H0 = 1.0-P1;
	static final double logP_H0 = Math.log(P_H0);	

	
	public PosteriorGrid runSpatialScan(SpatialScanGrid grid, int maxRegionX, int maxRegionY) {

		//analyze the grid to calculate total count, total baseline, and P(D|H0) values
		grid.analyzeGrid();
		
		logger.trace("Grid: {}", grid);
		
		//generate list of all regions up to max size as defined by maxRegionX, maxRegionY
		Set<SpatialScanRegion> regions = this.generateRegions(grid, maxRegionX, maxRegionY);
		
		//calculate scores for each region
		List<SpatialScanRegion> sortedRegions = this.calculateRegionScores(grid, regions);
		
		//calculate the probability of the data P(D)
		double logP_D = this.calculatePD(grid, sortedRegions);
		
		//calculate posterior probabilities for all regions, then total posterior 
		//probability for each cell (sum of cell's posterior and the posteriors of 
		//all regions that contain it)
		PosteriorGrid pGrid = this.calculatePosteriorProbabilities(grid, regions, logP_D);
		
		//Return grid of posterior probabilities
		return pGrid;
	}
	
	/** Function that takes in a series of log likelihood values and adds them.
	 * e.g. from L1, L2, L2 produces P(L1 + L2) */
	private double addLogs(double... L) {
		double L1 = L[0];
		
		//calculate residual sum (sum of differences b/w L(2-n) and L1
		double residualSum = 0.0;
		for(int i = 0;i < L.length;i++) {
			if(!Double.isNaN(Math.exp(L[i]-L1))) {
				residualSum += Math.exp(L[i]-L1);
			}
		}
		
		// Calculate the log sum
		double logSum = L1 + Math.log(residualSum);
		logger.trace("logSum = {} + log(1 + {}) = {}", new Object[] { L1, residualSum, logSum });
		return logSum;
	}
	
	/** Calculates the likelihood P(D|H1(S)) of an outbreak in this region and 
	 * stores it in the region object */
	private void analyzeRegion(SpatialScanRegion region, SpatialScanGrid grid) {
		double C_in = region.getCount();
		double B_in = region.getBaseline();
		double C_out = grid.getC_all() - C_in;
		double B_out = (grid.getB_all() - B_in) + 1;
		
		// Variables to define the range of m to integrate over
		double mInitial = 1.0;
		double mFinal = 3.0;
		double mIncrement = 0.2;
		
		double ll = calculateLogLikelihood(grid,mInitial,mFinal,mIncrement,C_in,C_out,B_in,B_out);
		if (Double.isNaN(ll)) {
			throw new IllegalArgumentException("Log likelihood for region was NaN!  " + region.toString());
		}
		region.setLikelihood(ll);
	}
	
	/** Calculate the log Likelihood of an outbreak in a given region */
	private double calculateLogLikelihood(SpatialScanGrid grid, double mInitial, double mFinal, double mIncrement, double C_in, double C_out, double B_in, double B_out) {
		
		int mCounter = 0;
		double logLikelihoodSum = 0.0;
		
		logger.trace("C_in: {}, C_out: {}, B_in: {}, B_out: {}, Q0: {}",
				new Object[] { C_in, C_out, B_in, B_out, grid.getQ0() } );
		
		/** 
		 * This loop exists to account for the unknown value of parameter m. Final 
		 * scores are calculated by averaging over the distribution of m from 
		 * [mInitial:mFinal] at mIncrement intervals.
		 * */
		for(double m = mInitial;m <= mFinal;m += mIncrement) {
		
			double alpha_in = m*grid.getQ0()*B_in;
			double beta_in = B_in;
			double alpha_out = grid.getQ0()*B_out;
			double beta_out = B_out;
			
			
			/** We have all of the values we need, now to calculate probabilities */
			
			/** Note: Individual terms in this equation are too large, and return
			 * infinite results. So, we reformulate the equation by taking its 
			 * natural log. Eventually probability will be converted back to a 
			 * non-log form.
			 * 
			 *    E.g.   log(beta_in^alpha_in) = alpha_in * log(beta_in)
			 *    
			 *    Thus the original equation
			 *    
			 *                    (beta_in)^alpha_in * Gamma(alpha_in + C_in)               (beta_out)^alpha_out * Gamma(alpha_out + C_out)   
			 *  P(D|H1(S)) ~  ----------------------------------------------------  X  ---------------------------------------------------------
			 *                (beta_in + B_in)^(alpha_in + C_in) * Gamma(alpha_in)     (beta_out + B_out)^(alpha_out + C_out) * Gamma(alpha_out)
			 *              
			 *    becomes
			 *    
			 *  log(P(D|H1(S))) ~     [ ( alpha_in * log(beta_in) ) + logGamma(alpha_in + C_in) ] - 
			 *				     [ ( (alpha_in + C_in) * log(beta_in + B_in) ) + logGamma(alpha_inl) ] +
			 *				          [ ( alpha_out * log(beta_out) ) + logGamma(alpha_out + C_out) ] - 
			 *	     			 [ ( (alpha_out + C_out) * log(beta_out + B_out) ) + logGamma(alpha_out) ]
			 *              
			 */

			double logLikelihood = (( (alpha_in*Math.log(beta_in)) + Gamma.logGamma((alpha_in+(double)C_in)) ) 
			  - ( ((alpha_in + (double)C_in) * (Math.log(beta_in + (double)B_in))) + Gamma.logGamma(alpha_in) ))
			  + (( (alpha_out*Math.log(beta_out)) + Gamma.logGamma((alpha_out+(double)C_out)) ) 
			  - ( ((alpha_out + (double)C_out) * (Math.log(beta_out + (double)B_out))) + Gamma.logGamma(alpha_out) ));	
			
			logLikelihoodSum += logLikelihood;
			
			
			mCounter++;
		}//end for loop over m
		
		Validate.isTrue(mCounter > 0, "mCounter was 0!");
		
		double logLikelihood = (logLikelihoodSum / ((double)mCounter));				
		return logLikelihood;
	}
	
	/** Generates all possible regions, up to the given max n x m size, within the grid. 
	 * Regions are stored in the regions Set */
	private Set<SpatialScanRegion> generateRegions(SpatialScanGrid grid, int nMax, int mMax) {
		Validate.notNull(grid, "Error: Spatial grid has not been initialized!");
		
		Set<SpatialScanRegion> regions = new HashSet<SpatialScanRegion>();
		
		/** Calculate and store all regions within the grid up to size nMax x mMax */
		for(int n = 0;n < nMax;++n) {// for each x size
			for(int m = 0;m < mMax;++m) {// for each y size
				
				//for each region size
				int ctr = 0;
				for(int i = 0;i < grid.getRows()-n;++i) {// -n and -m to make sure we are still within the grid
					for(int j = 0;j < grid.getColumns()-m;++j) {
						// for each possible position of the region
						regions.add(new SpatialScanRegion(i, i+n, j, j+m));
						ctr++;					
					}
				}
			}
		}
		logger.debug("Generated {} total regions.", regions.size());
		return regions;
	}
			
	/** TEST FUNCTION ** Populate the grid using random numbers between low and high, with baseline specified.
	 * For now identifiers are assigned based on array index */
	public SpatialScanGrid populateRandomGrid(SpatialScanGrid grid, int low, int high, int baseline) {
		Random rand = new Random();
		
		int count;

		//populate the grid
		for(int i = 0;i < grid.getRows();++i) {
			for(int j = 0;j < grid.getColumns();++j) {
				String ident = "Cell" + i + j;
				count = rand.nextInt((high-low))+low;
				grid.setCell(i, j, count, baseline, ident);
			}
		}
		
		logger.debug("Uniform region prior (P1) = {}", P1);
		logger.debug("No Outbreak prior (P_H0) = {}", P_H0);
				
		return grid;
	}
	
	/** Calculate the prior probability of an outbreak in a given region, P(H_1(S)) */
	private List<SpatialScanRegion> calculateRegionScores(SpatialScanGrid grid, Set<SpatialScanRegion> regions) {		
		
		List<SpatialScanRegion> sortedRegions = new ArrayList<SpatialScanRegion>();
		
		double P_H1_S = P1/regions.size();//calculate the prior probability of a given region having an outbreak		
		double logP_H1_S = Math.log(P_H1_S);// Convert P(H_1(S)) to a log probability for computational purposes
		logger.debug("logP_H1_S: {}", logP_H1_S);
		
		int i = 0;
		/** Iterate through all regions, calculating region scores */
		for(SpatialScanRegion region : regions) {
			// set count and baseline values for the region
			scanRegion(region, grid);
			// analyze region to calculate the log likelihood logP(D|H1(S))
			analyzeRegion(region, grid);
			// get the log likelihood of the region, logP(D|H1(S))
			double logLikelihood = region.getLikelihood();
			// calculate the score of this region
			double score = logP_H1_S + logLikelihood;			
			// set the score of the region
			region.setScore(score);
			// add the region to the sorted set of regions
			sortedRegions.add(region);
			i++;
		}
		Collections.sort(sortedRegions);
		logger.debug("Calculated scores for {} regions", i);
		
		return sortedRegions;
	}
	
	/** Calculate the log probability of the data P(D) */
	private double calculatePD(SpatialScanGrid grid, List<SpatialScanRegion> sortedRegions) {
		
		// calculate the score for the null hypothesis
		double H0_score = grid.getNullHypothesisLogLikelihood() + logP_H0;
		
		logger.trace("grid.getNullHypothesisLogLikelihood()= {}     logP_H0 = {}", grid.getNullHypothesisLogLikelihood(), logP_H0);
		
		// create an ordered list of all scores, ready for summation
		double [] scores = new double[sortedRegions.size()+1];
		
		// if the null hypothesis has the highest score, put it in the front of the list
		if(H0_score > ((SpatialScanRegion)(sortedRegions.toArray()[0])).getScore()) {			
			scores[0] = H0_score;

			logger.trace("scores[0] = H0_score; H0_score = {}" + H0_score);
			
			int i = 1;
			for(SpatialScanRegion region : sortedRegions) {
				scores[i] = region.getScore();
				i++;
			}
		}
		else{// null hypothesis does not have the highest score
			int i = 0;
			for(SpatialScanRegion region : sortedRegions) {
				
				if(i==0)logger.trace("scores[0] = region[0].getScore(); region[0].getScore() = " + region.getScore());
				
				scores[i] = region.getScore();
				i++;
			}
			// H0 does not have the highest score, put it in the back of the list
			scores[i] = H0_score;
		}
		
		// use the ordered list to add the logs, producing log(P1+P2+...+PN)
		double scoresSum = addLogs(scores);
		double logP_D = scoresSum;
		logger.debug("logP_D = " + logP_D);
		
		return logP_D;
	}
		
	/** Calculate posterior probabilities for each cell in the grid.
	 *  
	 *  */
	private PosteriorGrid calculatePosteriorProbabilities(SpatialScanGrid grid, Set<SpatialScanRegion> regions, double logP_D) {
		int X = grid.getRows();
		int Y = grid.getColumns();
		
		// initialize posterior grid
		PosteriorGrid pGrid = new PosteriorGrid(X,Y);

		double P_H1_S = P1/regions.size();//calculate the prior probability of a given region having an outbreak		
		double logP_H1_S = Math.log(P_H1_S);// Convert P(H_1(S)) to a log probability for computational purposes
		
		// first, the posterior probability of the null hypothesis
		double logP_H0_D = grid.getNullHypothesisLogLikelihood() + logP_H0 - logP_D;
		pGrid.setPosteriorProbNoOutbreak(Math.exp(logP_H0_D));

		logger.debug("logP_H0_D = " + logP_H0_D);
		logger.debug("P_H0_D = " + Math.exp(logP_H0_D));
		logger.debug("Probability of outbreak: " + (1-Math.exp(logP_H0_D)));

		// now iterate through the region set to calculate the posterior probabilities for each region
		for(SpatialScanRegion region : regions) {
			double logP_H1_S_D = region.getLikelihood() + logP_H1_S - logP_D;
			region.setPosteriorProbability(Math.exp(logP_H1_S_D));
			
			// Now iterate through each cell in the region and add the value to each cell contained 
			// in that region
			Set<int[]> cells = region.getSetOfContainedCellCoordinates();
			// add the posterior of this region to each cell contained within
			for(int[] cell : cells) {
				double el = Math.exp(logP_H1_S_D);
				if(!Double.isNaN(el)) {
					pGrid.incrementPosterior(cell[1], cell[0], el);
				}
			}			
		}
		logger.debug("Finished calculating regional posterior probabilities");
		return pGrid;
	}
	
	private void scanRegion(SpatialScanRegion region, SpatialScanGrid grid) {
		//get coordinates of region within the grid
		int[] coords = region.getCoordinates();
		int sumCounts = 0;
		int sumBaselines = 0;
		//iterate through region, sum counts and baseline values
		for(int i = coords[0];i <= coords[1];++i) {
			for(int j = coords[2];j <= coords[3];++j) {
				sumCounts += grid.getCount(i, j);
				sumBaselines += grid.getBaseline(i, j);
			}
		}
		// store count and baseline values in the region
		region.setBaseline(sumBaselines);
		region.setCount(sumCounts);
	}

}