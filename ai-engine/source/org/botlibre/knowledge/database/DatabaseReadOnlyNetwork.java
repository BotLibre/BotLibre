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

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;

import org.botlibre.api.knowledge.Data;
import org.botlibre.api.knowledge.MemoryStorageException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Primitive;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.sessions.server.ServerSession;

/**
 * Network using JPA to access a PostgresQL database.
 * This is used for read-only access, and is shared by all objects in the cache.
 */

public class DatabaseReadOnlyNetwork extends DatabaseNetwork {
	
	static Map<String, Object> properties;
	static {
		properties = new HashMap<String, Object>();
		properties.put(QueryHints.READ_ONLY, true);
	}
	
	public DatabaseReadOnlyNetwork(EntityManager entityManager, boolean isShortTerm) {
		super(new EntityManagerImpl(entityManager.unwrap(ServerSession.class), SynchronizationType.UNSYNCHRONIZED), isShortTerm);
		ServerSession server = entityManager.unwrap(ServerSession.class);
		if (!server.getProperties().containsKey("network")) {
			server.setProperty("network", this);
		}
	}
	
	public boolean isReadOnly() {
		return true;
	}
	
	protected void addRelationship(Relationship relationship) {
		throwReadOnly();
	}
	
	protected void throwReadOnly() {
		MemoryStorageException exception = new MemoryStorageException("Network is read-only.");
		exception.printStackTrace();
		throw exception;
	}

	/**
	 * Resume the network after a save.
	 * Keep the MAX_SIZE number of most conscious vertices in memory.
	 */
	public void resume() {
		throwReadOnly();
	}

	/**
	 * Execute and commit the native query.
	 */
	public int executeNativeQuery(String sql) {
		throwReadOnly();
		return 0;
	}

	/**
	 * Execute and commit the update query.
	 */
	public int executeQuery(String jpql) {
		throwReadOnly();
		return 0;
	}

	/**
	 * Commit memory to the database.
	 */
	public void save() {
		throwReadOnly();
	}

	/**
	 * Add the existing vertex to the network.
	 * Used to load an existing vertex, createVertex must be used to create a new one.
	 */
	public synchronized void addVertex(Vertex vertex) {
		throwReadOnly();
	}
	
	/**
	 * Find the exiting vertex, or create a temporary one for primitives.
	 */
	@SuppressWarnings("rawtypes")
	public synchronized Vertex createVertex(Object data) {
		if ((data instanceof String) && ((String)data).length() > MAX_TEXT) {
			data = ((String)data).substring(0, MAX_TEXT);
		}
		if (data instanceof Class) {
			data = new Primitive(((Class)data).getName());
		}
		Vertex vertex = findByData(data);
		if (vertex != null) {
			return vertex;
		}
		if (data instanceof Primitive) {
			vertex = new BasicVertex();
			vertex.setData(data);
			vertex.setNetwork(this);
			return vertex;
		}
		throwReadOnly();
		return null;
	}

	/**
	 * Save the property setting to the current transaction.
	 */
	public void saveProperty(String propertyName, String value, boolean startup) {
		throwReadOnly();
	}

	/**
	 * Remove the property setting to the current transaction.
	 */
	public void removeProperty(String propertyName) {
		throwReadOnly();
	}
	
	/**
	 * Remove the vertex from the network.
	 * Note that the vertex must be no longer referenced by any other vertex in the network.
	 */
	public void removeVertex(Vertex vertex) {
		throwReadOnly();
	}
	
	/**
	 * Remove the vertex and all references to it from the network.
	 */
	public void removeVertexAndReferences(Vertex vertex) {
		throwReadOnly();
	}
	
	/**
	 * Remove the relationship from the network.
	 * Note that the relationship must be no longer referenced by any other vertex in the network.
	 */
	public void removeRelationship(Relationship relationship) {
		throwReadOnly();
	}
	
	public void setHints(Query query) {
		query.setHint(QueryHints.READ_ONLY, true);
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
		Query query = getEntityManager().createNamedQuery("findVertexByData");
		query.setHint(QueryHints.READ_ONLY, true);
		query.setParameter("data", BasicVertex.convertDataValue(data));
		query.setParameter("type", BasicVertex.convertDataType(data));
		try {
			vertex = (Vertex)query.getSingleResult();
			if (this.verticiesByData.size() < MAX_SIZE) {
				this.verticiesByData.put(vertex.getData(), vertex);
			}
			return vertex;
		} catch (NoResultException notFound) {
			return null;
		}
	}
	
	/**
	 * Return the vertex with the given name.
	 */
	public synchronized Vertex findById(Number id) {
		if (id == null) {
			return null;
		}
		return getEntityManager().find(BasicVertex.class, id, properties);
	}
	
	/**
	 * Return the lob data.
	 */
	public synchronized Data findData(Data data) {
		if (data == null) {
			return null;
		}
		return getEntityManager().find(data.getClass(), data.getId(), properties);
	}

	/**
	 * Merge the vertices and relations of the network into this network.
	 */
	public synchronized void merge(Network network) {
		throwReadOnly();
	}	
}