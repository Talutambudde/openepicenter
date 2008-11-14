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
package com.hmsinc.epicenter.webapp.dto;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.directwebremoting.annotations.DataTransferObject;

/**
 * A node of an EXTJS tree, serializable by DWR.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @author Carina@EXTJS.Forums
 * @version $Id: TreeNode.java 966 2008-02-15 15:44:11Z steve.kondik $
 */
@DataTransferObject
public class TreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2067392417212269794L;

	private final String id;

	private final String text;

	private final boolean leaf;

	private Serializable item;
	
	/**
	 * @param text
	 * @param id
	 */
	public TreeNode(String id, String text, boolean leaf) {
		super();
		this.text = text;
		this.id = id;
		this.leaf = leaf;
	}

	/**
	 * @param text
	 * @param id
	 */
	public TreeNode(String id, String text, boolean leaf, Serializable item) {
		super();
		this.text = text;
		this.id = id;
		this.leaf = leaf;
		this.item = item;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the leaf
	 */
	public boolean isLeaf() {
		return leaf;
	}

	/**
	 * @return the item
	 */
	public Serializable getItem() {
		return item;
	}

	/**
	 * @param item the item to set
	 */
	public void setItem(Serializable item) {
		this.item = item;
	}

	public String getItemClass() {
		String ret = null;
		if (item != null) {
			ret = item.getClass().getName();
		}
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		return EqualsBuilder.reflectionEquals(this, o);
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
