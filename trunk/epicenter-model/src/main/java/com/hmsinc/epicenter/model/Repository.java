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
package com.hmsinc.epicenter.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * A repository manages a group of related entities.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:Repository.java 220 2007-07-17 14:59:08Z steve.kondik $
 * 
 * @param <T>
 * @param <ID>
 */
public interface Repository<T extends Serializable, ID extends Serializable> {

	/**
	 * Loads the entity of the given type by id.
	 * 
	 * @param <L>
	 * @param id
	 * @param type
	 * @return
	 */
	public <L extends T> L load(final ID id, final Class<L> type);

	/**
	 * Gets a reference to a type of entity by id without actually loading it.
	 * 
	 * @param <L>
	 * @param id
	 * @param type
	 * @return
	 */
	public <L extends T> L getReference(final ID id, final Class<L> type);

	/**
	 * Gets references to a list of entities.
	 * 
	 * @param <L>
	 * @param ids
	 * @param type
	 * @return
	 */
	public <L extends T> List<L> getReferences(final Collection<ID> ids, final Class<L> type);
	
	/**
	 * Merges this entity.
	 * 
	 * @param t
	 */
	public void update(final T t);

	/**
	 * Merges this collection of entities.
	 * 
	 * @param t
	 */
	public void update(final Collection<? extends T> t);

	/**
	 * Persists this entity.
	 * 
	 * @param t
	 */
	public void save(final T t);

	/**
	 * Persists this collection of entities.
	 * 
	 * @param t
	 */
	public void save(final Collection<? extends T> t);

	/**
	 * Deletes this entity.
	 * 
	 * @param t
	 */
	public void delete(final T t);

	/**
	 * Deletes this collection of entities..
	 * 
	 * @param <L>
	 * @param type
	 * @return
	 */
	public void delete(final Collection<? extends T> t);
	
	/**
	 * Refreshes this entity.
	 * 
	 * @param t
	 */
	public void refresh(final T t);
	
	/**
	 * Gets all entities of the requested type.
	 * 
	 * @param <L>
	 * @param type
	 * @return
	 */
	public <L extends T> List<L> getList(final Class<L> type);

	/**
	 * Gets the count of entities of the requested type.
	 * 
	 * @param <L>
	 * @param type
	 * @return
	 */
	public <L extends T> long count(final Class<L> type);
	
	/**
	 * Evicts an object from the cache.
	 * 
	 * @param t
	 */
	public void evict(final Object t);
	
	/**
	 * Flush any pending transactions.
	 */
	public void flush();
	
	/**
	 * Get the name of the underlying database table for the given class.
	 * 
	 * @param <L>
	 * @param type
	 * @return
	 */
	public <L extends T> String getTableForEntity(final Class<L> type);
	
	/**
	 * Simple query by property name and type.
	 * 
	 * @param <L>
	 * @param type
	 * @param property
	 * @param value
	 * @return
	 */
	public <L extends T> List<L> findBy(final Class<L> type, final String property, final Object value);
	
}