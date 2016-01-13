/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.knowledge.database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Data;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.AbstractNetwork;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.Property;
import org.botlibre.thought.consciousness.Consciousness;
import org.eclipse.persistence.jpa.JpaEntityManager;
import org.eclipse.persistence.sessions.UnitOfWork;

/**
 * Network using JPA to access a Derby database.
 */

public class DatabaseNetwork extends AbstractNetwork {
	
	private Boolean trackAccessCount;

	private EntityManager entityManager;
	/** Cache the size query result. */
	private int size = -1;
	
	public DatabaseNetwork(EntityManager entityManager, boolean isShortTerm) {
		super(isShortTerm);
		this.entityManager = entityManager;
		entityManager.unwrap(UnitOfWork.class).setProperty("network", this);
	}
	
	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	protected void addRelationship(Relationship relationship) {
		this.entityManager.persist(relationship);
	}
	
	protected boolean trackAccessCount() {
		if (trackAccessCount == null) {
			trackAccessCount = this.bot.mind().getThought(Consciousness.class).isEnabled();
		}
		return trackAccessCount;
	}

	/**
	 * Resume the network after a save.
	 * Keep the MAX_SIZE number of most conscious vertices in memory.
	 */
	public void resume() {
		this.bot.log(this, "Resuming", Bot.FINE, this);
		if (!this.bot.mind().getThought(Consciousness.class).isEnabled()) {
			clear();
			return;
		}
		Set<Vertex> oldVerticies = new HashSet<Vertex>(allActive());
		// Shrink to fixed size.
		int level = 1;
		while ((oldVerticies.size() > MAX_SIZE) && (level < 256)) {
			Iterator<Vertex> iterator = oldVerticies.iterator();
			while ((oldVerticies.size() > MAX_SIZE) && iterator.hasNext()) {
				Vertex vertex = iterator.next();
				if ((!vertex.isPrimitive()) && vertex.getConsciousnessLevel() <= level) {
					iterator.remove();
				}
			}
			level = level * 2;
		}
		clear();
		for (Vertex oldVertex : oldVerticies) {
			Vertex newVertex = findById(oldVertex.getId());
			if (newVertex != null) { // Can be null if save failed.
				newVertex.setConsciousnessLevel(oldVertex.getConsciousnessLevel());
				if (newVertex.hasData()) {
					this.verticiesByData.put(newVertex.getData(), newVertex);
				}
			}
		}
	}

	/**
	 * Execute and commit the native query.
	 */
	public int executeNativeQuery(String sql) {
		this.bot.log(this, "SQL", Bot.FINE, sql);
		int rowCount = 0;
		synchronized (this.bot.memory()) {
			try {
				this.entityManager.getTransaction().begin();
				rowCount = this.entityManager.createNativeQuery(sql).executeUpdate();
				this.entityManager.getTransaction().commit();
				resetSize();
			} catch (RuntimeException failed) {
				this.bot.log(this, failed);
				if (this.entityManager.getTransaction().isActive()) {
					this.entityManager.getTransaction().rollback();
				}
				// If commit fails, clear short-term memory to avoid
				// repeated failures.
				clear();
				throw failed;
			}
		}
		((DatabaseNetwork)this.bot.memory().getLongTermMemory()).resetSize();
		return rowCount;
	}

	/**
	 * Execute and commit the update query.
	 */
	public int executeQuery(String jpql) {
		this.bot.log(this, "JPQL", Level.FINE, jpql);
		int rowCount = 0;
		synchronized (this.bot.memory()) {
			try {
				this.entityManager.getTransaction().begin();
				rowCount = this.entityManager.createQuery(jpql).executeUpdate();
				this.entityManager.getTransaction().commit();
				resetSize();
			} catch (RuntimeException failed) {
				this.bot.log(this, failed);
				if (this.entityManager.getTransaction().isActive()) {
					this.entityManager.getTransaction().rollback();
				}
				// If commit fails, clear short-term memory to avoid
				// repeated failures.
				clear();
				throw failed;
			}
		}
		((DatabaseNetwork)this.bot.memory().getLongTermMemory()).resetSize();
		return rowCount;
	}

	/**
	 * Commit memory to the database.
	 */
	public void save() {
		this.bot.log(this, "Saving", Level.FINE); //, newObjects);
		synchronized (this.bot.memory()) {
			try {
				this.entityManager.getTransaction().begin();
				this.entityManager.getTransaction().commit();
				resetSize();
			} catch (RuntimeException failed) {
				this.bot.log(this, failed);
				if (this.entityManager.getTransaction().isActive()) {
					this.entityManager.getTransaction().rollback();
				}
				// If commit fails, clear short-term memory to avoid
				// repeated failures.
				clear();
				throw failed;
			}
		}
		((DatabaseNetwork)this.bot.memory().getLongTermMemory()).resetSize();
		//newObjects = new HashSet<Vertex>();
	}
	
	public void resetSize() {
		this.size = -1;		
	}
	
	public synchronized void clear() {
		this.entityManager.clear();
		this.verticiesByData.clear();
		resetSize();
		this.entityManager.unwrap(UnitOfWork.class).setProperty("network", this);
	}

	/**
	 * Add the existing vertex to the network.
	 * Used to load an existing vertex, createVertex must be used to create a new one.
	 */
	public synchronized void addVertex(Vertex vertex) {
		vertex.setNetwork(this);
		if (vertex.hasData() && (vertex.getData() instanceof Data)) {
			this.entityManager.persist(vertex.getData());			
		}
		this.entityManager.persist(vertex);
		if (vertex.hasData()) {
			this.verticiesByData.put(vertex.getData(), vertex);
		}
		//newObjects.add(vertex);
	}

	/**
	 * Save the property setting to the current transaction.
	 */
	public void saveProperty(String propertyName, String value, boolean startup) {
		Property property = this.entityManager.find(Property.class, propertyName);
		if (property != null) {
			property.setValue(value);
			property.setStartup(startup);
		} else {
			property = new Property(propertyName, value, startup);
			this.entityManager.persist(property);
		}
		super.saveProperty(propertyName, value, startup);
	}

	/**
	 * Remove the property setting to the current transaction.
	 */
	public void removeProperty(String propertyName) {
		Property property = this.entityManager.find(Property.class, propertyName);
		if (property != null) {
			this.entityManager.remove(property);
		}
		super.removeProperty(propertyName);
	}
	
	public Network getParent() {
		return null;
	}
	
	public void setParent(Network parent) {

	}

	public int size() {
		if (!this.entityManager.isOpen()) {
			return 0;
		}
		if (isShortTerm()) {
			return allActive().size();
		}
		if (this.size == -1) {
			this.size = ((Number)this.entityManager.createQuery("Select count(v) from Vertex v").getSingleResult()).intValue();
		}
		return this.size;
	}
	
	/**
	 * Remove the vertex from the network.
	 * Note that the vertex must be no longer referenced by any other vertex in the network.
	 */
	public void removeVertex(Vertex vertex) {
		Vertex managed = findById(vertex.getId());
		if (managed == null) {
			return;
		}
		this.entityManager.remove(managed);
		if (vertex.hasData()) {
			this.verticiesByData.remove(vertex.getData());
			if (vertex.getData() instanceof Data) {
				this.entityManager.remove(findData((Data)vertex.getData()));				
			}
		}
	}
	
	/**
	 * Remove the vertex and all references to it from the network.
	 */
	public void removeVertexAndReferences(Vertex vertex) {
		Vertex managed = findById(vertex.getId());
		if (managed == null) {
			return;
		}
		Iterator<Relationship> iterator = findAllRelationshipsTo(vertex).iterator();
		// Remove all references.
		while (iterator.hasNext()) {
			Relationship relationship = iterator.next();
			//if (!((UnitOfWorkImpl)this.entityManager.unwrap(UnitOfWork.class)).isObjectDeleted(relationship.getSource())) {
				relationship.getSource().internalRemoveRelationship(relationship);
			//}
		}
		removeVertex(vertex);
	}
	
	/**
	 * Remove the relationship from the network.
	 * Note that the relationship must be no longer referenced by any other vertex in the network.
	 */
	public void removeRelationship(Relationship relationship) {
		if (relationship.getId() == null) {
			return;
		}
		Relationship managed = this.entityManager.find(relationship.getClass(), relationship.getId());
		if (managed != null) {
			this.entityManager.remove(managed);
		}
	}
	
	/**
	 * Return all active vertices.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Vertex> allActive() {
		UnitOfWork unitOfWork = this.entityManager.unwrap(JpaEntityManager.class).getUnitOfWork();
		try {
			return unitOfWork.getIdentityMapAccessor().getAllFromIdentityMap(null, BasicVertex.class, null, null);
		} catch (Exception exception) {
			return new ArrayList<Vertex>();
		}
	}
	
	public void setHints(Query query) {
		
	}
	
	/**
	 * Return all vertices.
	 */
	public synchronized List<Vertex> findAll() {
		return findAll(1000, 0);
	}
	
	/**
	 * Return all vertices.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Vertex> findAll(int pageSize, int page) {
		if (isShortTerm()) {
			return allActive();
		}
		Query query = this.entityManager.createQuery("Select v from Vertex v");
		setHints(query);
		query.setFirstResult(page * pageSize);
		query.setMaxResults(pageSize);
		return query.getResultList();
	}
	
	/**
	 * Return all vertices matching the filter.
	 */
	public synchronized List<Vertex> findAllLike(String filter) {
		return findAllLike(filter, 1000, 0);
	}
	
	/**
	 * Return all vertices matching the filter.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Vertex> findAllLike(String filter, int pageSize, int page) {
		Query query = null;
		if (filter.indexOf('*') == -1) {
			query = this.entityManager.createQuery("Select v from Vertex v where v.dataValue = :filter");
			query.setParameter("filter", filter);
		} else {
			query = this.entityManager.createQuery("Select v from Vertex v where v.dataValue like :filter");
			query.setParameter("filter", filter.replace('*', '%'));			
		}
		setHints(query);
		query.setFirstResult(page * pageSize);
		query.setMaxResults(pageSize);
		return query.getResultList();
	}
	
	/**
	 * Return all vertices matching the JPQL query.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized List<Vertex> findAllQuery(String jpql, Map parameters, int pageSize, int page) {
		try {
			Query query = this.entityManager.createQuery(jpql);
			setHints(query);
			query.setFirstResult(pageSize * page);
			query.setMaxResults(pageSize);
			for (Map.Entry parameter : (Set<Map.Entry>)parameters.entrySet()) {
				query.setParameter((String)parameter.getKey(), parameter.getValue());
			}
			return query.getResultList();
		} catch (Exception badQuery) {
			this.bot.log(this, badQuery);
			return new ArrayList<Vertex>();
		}
	}
	
	/**
	 * Return all vertices matching the JPQL query.
	 */
	public synchronized List<Vertex> findAllQuery(String jpql) {
		return findAllQuery(jpql, 1000);
	}
	
	/**
	 * Return all vertices.
	 */
	public synchronized int countAll() {
		Query query = this.entityManager.createQuery("Select count(v) from Vertex v");
		setHints(query);
		return ((Number)query.getSingleResult()).intValue();
	}
	
	/**
	 * Return all vertices matching the filter.
	 */
	public synchronized int countAllLike(String filter) {
		Query query = null;
		if (filter.indexOf('*') == -1) {
			query = this.entityManager.createQuery("Select count(v) from Vertex v where v.dataValue = :filter");
			query.setParameter("filter", filter);
		} else {
			query = this.entityManager.createQuery("Select count(v) from Vertex v where v.dataValue like :filter");
			query.setParameter("filter", filter.replace('*', '%'));			
		}
		setHints(query);
		return ((Number)query.getSingleResult()).intValue();
	}
	
	/**
	 * Return all vertices matching the JPQL query.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Vertex> findAllQuery(String jpql, int max) {
		try {
			Query query = this.entityManager.createQuery(jpql);
			setHints(query);
			query.setMaxResults(max);
			return query.getResultList();
		} catch (Exception badQuery) {
			this.bot.log(this, badQuery);
			return new ArrayList<Vertex>();
		}
	}
	
	/**
	 * Execute the native SQL query.
	 */
	@SuppressWarnings("rawtypes")
	public synchronized List findByNativeQuery(String sql, Class type, int max) {
		try {
			Query query = null;
			if (type ==  null) {
				query = this.entityManager.createNativeQuery(sql);
			} else {
				query = this.entityManager.createNativeQuery(sql, type);				
			}
			setHints(query);
			query.setMaxResults(max);
			return query.getResultList();
		} catch (Exception badQuery) {
			this.bot.log(this, badQuery);
			return new ArrayList();
		}
	}
	
	/**
	 * Find all relationships related to the vertex or of the vertex relationship type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Relationship> findAllRelationshipsTo(Vertex vertex) {
		Query query = this.entityManager.createQuery("Select r from Relationship r where r.target = :vertex or r.type = :vertex");
		setHints(query);
		query.setParameter("vertex", vertex);
		return query.getResultList();
	}
	
	/**
	 * Find all relationships related to the vertex by the vertex type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Relationship> findAllRelationshipsTo(Vertex vertex, Vertex type) {
		Query query = this.entityManager.createQuery("Select r from Relationship r where r.target = :vertex and r.type = :type");
		setHints(query);
		query.setParameter("vertex", vertex);
		query.setParameter("type", type);
		return query.getResultList();
	}

	/**
	 * Return a query builder.
	 */
	public CriteriaBuilder getCriteriaBuilder() {
		return this.entityManager.getCriteriaBuilder();
	}
	
	/**
	 * Find all relationships related to the vertex or of the vertex type.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Vertex> findAllInstances(Vertex type, Vertex relationship, Calendar start) {
		Vertex instantiation = findByData(Primitive.INSTANTIATION);
		Query query = null;
		if (relationship != null) {
			if (start == null) {
				query = this.entityManager.createQuery(
						"Select distinct v from Vertex v join v.allRelationships r join v.allRelationships r2 where r.target = :type and r.type = :instantiation and r.correctness > 0"
						+ " and r2.type = :relationship and r2.correctness > 0 order by r.creationDate desc");
				query.setParameter("relationship", relationship.detach());
			} else {
				query = this.entityManager.createQuery(
						"Select distinct v from Vertex v join v.allRelationships r join v.allRelationships r2 where r.target = :type and r.type = :instantiation and r.correctness > 0"
						+ " and r2.type = :relationship and r2.correctness > 0 and r2.creationDate >= :start order by r.creationDate desc");
				query.setParameter("relationship", relationship.detach());
				query.setParameter("start", new Date(start.getTimeInMillis()));
			}
		} else {
			if (start == null) {
				query = this.entityManager.createQuery("Select distinct v from Vertex v join v.allRelationships r where r.target = :type and r.type = :instantiation and r.correctness > 0 order by r.creationDate desc");
			} else {
				query = this.entityManager.createQuery("Select distinct v from Vertex v join v.allRelationships r where r.target = :type and r.type = :instantiation and r.correctness > 0 and r.creationDate >= :start order by r.creationDate desc");
				query.setParameter("start", new Date(start.getTimeInMillis()));
			}
		}
		setHints(query);
		query.setParameter("instantiation", instantiation);
		query.setParameter("type", type);
		query.setMaxResults(1000);
		return query.getResultList();
	}
	
	/**
	 * Execute the criteria query.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized List search(CriteriaQuery criteria, int page, int max) {
		Query query = this.entityManager.createQuery(criteria);
		setHints(query);
		query.setFirstResult(page * max);
		query.setMaxResults(max);
		return query.getResultList();
	}
	
	/**
	 * Return the vertex with the given data.
	 */
	public synchronized Vertex findByData(Object data) {
		if (data == null) {
			return null;
		}
		Vertex vertex = this.verticiesByData.get(data);
		if (vertex != null) {
			return vertex;
		}
		Query query = this.entityManager.createNamedQuery("findVertexByData");
		setHints(query);
		query.setParameter("data", BasicVertex.convertDataValue(data));
		query.setParameter("type", BasicVertex.convertDataType(data));
		try {
			vertex = (Vertex)query.getSingleResult();
			if (trackAccessCount()) {
				vertex.incrementAccessCount();
			}
			this.verticiesByData.put(vertex.getData(), vertex);
			return vertex;
		} catch (NoResultException notFound) {
			return null;
		}
	}
	
	/**
	 * Return the vertex with the given data.
	 */
	@SuppressWarnings("unchecked")
	public synchronized Vertex findByName(String name) {
		if (name == null) {
			return null;
		}
		Query query = this.entityManager.createNamedQuery("findVertexByName");
		setHints(query);
		query.setParameter("name", name);
		List<Vertex> result = query.getResultList();
		if (result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}
	
	/**
	 * Return the vertex with the given name.
	 */
	public synchronized Vertex findById(Number id) {
		if (id == null) {
			return null;
		}
		return this.entityManager.find(BasicVertex.class, id);
	}
	
	/**
	 * Return the lob data.
	 */
	public synchronized Data findData(Data data) {
		if (data == null) {
			return null;
		}
		return this.entityManager.find(data.getClass(), data.getId());
	}

	/**
	 * Merge the vertices and relations of the network into this network.
	 */
	public synchronized void merge(Network network) {
		super.merge(network);
		this.size = -1;
	}	
}