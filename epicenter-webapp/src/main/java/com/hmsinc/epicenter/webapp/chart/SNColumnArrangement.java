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
package com.hmsinc.epicenter.webapp.chart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;

import org.jfree.chart.block.Arrangement;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.data.Range;
import org.jfree.ui.Size2D;

/**
 * Arranges blocks in a column layout. This class is immutable.
 */
public class SNColumnArrangement implements Arrangement, Serializable {
	
	/** For serialization. */
	private static final long serialVersionUID = -3453453452898581555L;

	/** The horizontal gap between columns. */
	private double horizontalGap;

	/** The vertical gap between items in a column. */
	private double verticalGap;

	/**
	 * Creates a new instance.
	 */
	public SNColumnArrangement(double hGap, double vGap) {
		this.horizontalGap = hGap;
		this.verticalGap = vGap;
	}

	/**
	 * Adds a block to be managed by this instance. This method is usually
	 * called by the {@link BlockContainer}, you shouldn't need to call it
	 * directly.
	 * 
	 * @param block
	 *            the block.
	 * @param key
	 *            a key that controls the position of the block.
	 */
	public void add(Block block, Object key) {
		// nothing to do here
	}

	/**
	 * Calculates and sets the bounds of all the items in the specified
	 * container, subject to the given constraint. The <code>Graphics2D</code>
	 * can be used by some items (particularly items containing text) to
	 * calculate sizing parameters.
	 * 
	 * @param container
	 *            the container whose items are being arranged.
	 * @param g2
	 *            the graphics device.
	 * @param constraint
	 *            the size constraint.
	 * 
	 * @return The size of the container after arrangement of the contents.
	 */
	@SuppressWarnings("unchecked")
	public Size2D arrange(BlockContainer container, Graphics2D g2, RectangleConstraint constraint) {
		List<Block> blocks = container.getBlocks();

		// calculate max width of entries
		double maxWidth = 0.0;
		double maxHeight = 0.0;
		for (Block block : blocks) {
			Size2D size = block.arrange(g2, RectangleConstraint.NONE);
			maxWidth = Math.max(maxWidth, size.width + this.horizontalGap);
			maxHeight = Math.max(maxHeight, size.height + this.verticalGap);
		}

		// calculate number of columns
		double width = 0.0;
		if (constraint.getWidthConstraintType() == LengthConstraintType.FIXED) {
			width = constraint.getWidth();
		} else if (constraint.getWidthConstraintType() == LengthConstraintType.RANGE) {
			Range range = constraint.getWidthRange();
			width = range.getUpperBound();
		} else {
			throw new RuntimeException("Not implemented.");
		}

		int columns = (int) (Math.floor(width / maxWidth));
		if (columns > 0) {
			columns--;
		}

		// for all columns
		int colx = -1;
		int coly = 0;
		for (Block block : blocks) {
			Size2D size = block.arrange(g2, RectangleConstraint.NONE);
			if (colx < columns) {
				colx++;
			} else {
				colx = 0;
				coly++;
			}
			double x = colx * maxWidth;
			double y = coly * maxHeight;
			block.setBounds(new Rectangle2D.Double(x, y, size.width, size.height));
		}

		// calculate size of bounding
		double bWidth = (coly == 0) ? (colx + 1) * maxWidth : (columns + 1) * maxWidth;
		double bHeight = (coly + 1) * maxHeight;
		return new Size2D(bWidth, bHeight);
	}

	/**
	 * Clears any cached information.
	 */
	public void clear() {
		// no action required.
	}

	/**
	 * Tests this instance for equality with an arbitrary object.
	 * 
	 * @param obj
	 *            the object (<code>null</code> permitted).
	 * 
	 * @return A boolean.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof SNColumnArrangement)) {
			return false;
		}
		if (this.horizontalGap != ((SNColumnArrangement) obj).horizontalGap) {
			return false;
		}
		if (this.verticalGap != ((SNColumnArrangement) obj).verticalGap) {
			return false;
		}
		return true;
	}
}