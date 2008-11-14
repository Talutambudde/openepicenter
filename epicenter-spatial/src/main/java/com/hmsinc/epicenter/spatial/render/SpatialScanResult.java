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
package com.hmsinc.epicenter.spatial.render;

import java.awt.image.BufferedImage;

/**
 * @author shade
 *
 */
public class SpatialScanResult {

	private final double probabilityOfOutBreak;
	
	private final BufferedImage image;

	/**
	 * @param probabilityOfOutBreak
	 * @param image
	 */
	public SpatialScanResult(double probabilityOfOutBreak, BufferedImage image) {
		super();
		this.probabilityOfOutBreak = probabilityOfOutBreak;
		this.image = image;
	}

	/**
	 * @return the probabilityOfOutBreak
	 */
	public double getProbabilityOfOutBreak() {
		return probabilityOfOutBreak;
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}
	
}
