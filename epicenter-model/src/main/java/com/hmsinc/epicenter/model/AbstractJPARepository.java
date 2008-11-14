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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.commons.lang.Validate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hmsinc.epicenter.model.util.ModelUtils;

/**
 * A base implementation of a domain object repository for Hibernate/JPA.
 * 
 * @author <a href="mailto:steve.kondik@hmsinc.com">Steve Kondik</a>
 * @version $Id:AbstractJPARepository.java 220 2007-07-17 14:59:08Z steve.kondik $
 */
@org.springframework.stereotype.Repository
public abstract class AbstractJPARepository<T extends Serializable, ID extends Serializable> implements Repository<T, ID>  {

	protected static final String CACHE_HINT = "org.hibernate.cacheable";
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = "epicenter-model")
	protected EntityManager entityManager;

	private static final boolean isOldHibernateVersion;
	
	// Try to detect Hibernate API breakage so we can run on Mergence 1.2
	static {
		
		boolean old = false;
		try {
			final Method m = AbstractEntityPersister.class.getMethod("getCacheAccessStrategy");
			if (m == null) {
				old = true;
			}
		} catch (NoSuchMethodException e) {
			old = true;
		}
		
		isOldHibernateVersion = old;
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#load(ID, java.lang.Class)
	 */
	public <L extends T> L load(final ID id, final Class<L> type) {
		Validate.notNull(type, "Type must be specified.");
		return entityManager.find(type, id);
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#getReference(ID, java.lang.Class)
	 */
	public <L extends T> L getReference(final ID id, final Class<L> type) {
		return entityManager.getReference(type, id);
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#getReferences(java.util.List, java.lang.Class)
	 */
	public <L extends T> List<L> getReferences(final Collection<ID> ids, final Class<L> type) {
		final List<L> references = new ArrayList<L>();
		for (ID id : ids) {
			references.add(entityManager.getReference(type, id));
		}
		return references;
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#update(T)
	 */
	public void update(final T t) {
		entityManager.merge(t);
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#update(java.util.List)
	 */
	public void update(final Collection<? extends T> t) {
		for (T obj : t) {
			entityManager.merge(obj);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#save(T)
	 */
	public void save(final T t) {
		entityManager.persist(t);
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#save(java.util.List)
	 */
	public void save(final Collection<? extends T> t) {
		for (T obj : t) {
			entityManager.persist(obj);
		}
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#delete(T)
	 */
	public void delete(final T t) {
		entityManager.remove(t);
	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#delete(java.util.Collection)
	 */
	public void delete(final Collection<? extends T> t) {
		for (T obj : t) {
			entityManager.remove(obj);
		}
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#refresh(java.io.Serializable)
	 */
	public void refresh(T t) {
		entityManager.refresh(t);
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#getList(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public <L extends T> List<L> getList(final Class<L> type) {
		
		Validate.notNull(type, "Type must be specified.");
		
		// Check to see if we should cache this entity first..
		boolean cache = true;
		
		if (!isOldHibernateVersion) {
			final ClassMetadata metadata = ((Session)entityManager.getDelegate()).getSessionFactory().getClassMetadata(type);
			if (metadata instanceof AbstractEntityPersister) {
				final AbstractEntityPersister p = (AbstractEntityPersister)metadata;
				if (p.getCacheAccessStrategy() == null) {
					cache = false;
				}
			}
		}
		return ModelUtils.criteriaQuery(entityManager, type).setCacheable(cache).list();

	}
	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#findBy(java.lang.Class, java.lang.String, java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public <L extends T> List<L> findBy(Class<L> type, String property, Object value) {
		
		Validate.notNull(type, "Type must be specified.");
		Validate.notNull(property, "Property must be specified.");
		
		final Criteria c = ModelUtils.criteriaQuery(entityManager, type);
		c.add(Restrictions.eq(property, value));
		return c.list();
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#count(java.lang.Class)
	 */
	public <L extends T> long count(final Class<L> type) {
		Validate.notNull(type, "Type must be specified.");
		final Criteria c = ModelUtils.criteriaQuery(entityManager, type);
		c.setProjection(Projections.rowCount());
		return ((Integer)c.uniqueResult()).longValue();
	}
	
	public void evict(Object t) {
		((Session)entityManager.getDelegate()).evict(t);
	}

	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#flush()
	 */
	public void flush() {
		entityManager.flush();
	}

	
	/* (non-Javadoc)
	 * @see com.hmsinc.epicenter.model.Repository#getTableForEntity(java.lang.Class)
	 */
	public <L extends T> String getTableForEntity(Class<L> type) {
		Validate.notNull(type, "Type must be specified.");
		String ret = null;
		final ClassMetadata metadata =  ((Session)entityManager.getDelegate()).getSessionFactory().getClassMetadata(type);
		if (metadata instanceof AbstractEntityPersister) {
			ret = ((AbstractEntityPersister)metadata).getTableName();
		}
		return ret;
	}
	
}
