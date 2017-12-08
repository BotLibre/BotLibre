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
package org.botlibre.knowledge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Data;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;

/**
 * An interconnected set of vertices,
 * representing and knowledge-space.
 * Basic implementation to allow subclasses to avoid defining some of the basic stuff.
 */

public class BasicNetwork extends AbstractNetwork {

	private static final long serialVersionUID = 1L;
	
	protected Network parent;
	protected Set<Vertex> vertices = null;
	protected Map<Number, Vertex> verticesById = null;
	protected Map<Number, Data> dataById = null;
	
	public BasicNetwork() {
		this(false);
	}
	
	public BasicNetwork(boolean isShortTerm) {
		super(isShortTerm);
		this.vertices = new HashSet<Vertex>();
		this.verticesById = new HashMap<Number, Vertex>();
		this.verticesByData = new HashMap<Object, Vertex>();
		this.dataById = new HashMap<Number, Data>();
	}
	
	public BasicNetwork(Network parent) {
		this(true);
		this.parent = parent;
	}
	
	public void addRelationship(Relationship relationship) {
		if (relationship.getId() != null) {
			// Ensure the nextId sequence is consistent when restoring the network from storage.
			if (nextRelationshipId <= relationship.getId().longValue()) {
				nextRelationshipId = relationship.getId().longValue() + 1;
			}
		} else {
			relationship.setId(nextRelationshipId());
		}
	}
	
	public void resume() {
		getBot().log(this, "Resuming", Bot.FINE, this);
		Set<Vertex> newVertices = new HashSet<Vertex>(Math.max(this.vertices.size(), MAX_SIZE));
		// Shrink to fixed size.
		int level = 1;
		while ((this.vertices.size() > MAX_SIZE) && (level < 256)) {
			Iterator<Vertex> iterator = this.vertices.iterator();
			while ((this.vertices.size() > MAX_SIZE) && iterator.hasNext()) {
				Vertex vertex = iterator.next();
				if ((!vertex.isPrimitive()) && vertex.getConsciousnessLevel() <= level) {
					iterator.remove();
				}
			}
			level = level * 2;
		}
		newVertices.addAll(this.vertices);
		// Reset originals and clear relationships.
		if (getParent() != null) {
			for (Vertex vertex : newVertices) {
				Vertex original = getParent().findById(vertex.getId());
				vertex.setOriginal(original);
			}
		}
		// Reset id hashes.
		this.vertices = newVertices;
		this.verticesById = new HashMap<Number, Vertex>();
		for (Vertex vertex : newVertices) {
			this.verticesById.put(vertex.getId(), vertex);
		}
		this.verticesByData = new HashMap<Object, Vertex>();
		for (Vertex vertex : newVertices) {
			if (vertex.getData() != null) {
				this.verticesByData.put(vertex.getData(), vertex);
			}
		}
	}
	
	/**
	 * Merge the memory into the long term.
	 * This is similar to a transactional commit.
	 * The changes should also be persisted, as the long term should always just be a cache of the storage.
	 * This implementation does not support persistence.
	 */
	public void save() {
		getBot().log(this, "Saving", Bot.FINE, this);
		getParent().merge(this);
	}
	
	/**
	 * Return a thread safe copy of the network.
	 */
	public synchronized BasicNetwork clone() {
		BasicNetwork clone = (BasicNetwork)super.clone();
		clone.setVertices(new HashSet<Vertex>(getVertices()));
		clone.setVerticesById(new HashMap<Number, Vertex>(getVerticesById()));
		clone.setVerticesByData(new HashMap<Object, Vertex>(getVerticesByData()));
		return clone;
	}

	/**
	 * Clear all vertices from the network.
	 */
	public synchronized void clear() {
		this.vertices = new HashSet<Vertex>();
		this.verticesById = new HashMap<Number, Vertex>();
		this.verticesByData = new HashMap<Object, Vertex>();
		this.dataById = new HashMap<Number, Data>();
	}

	/**
	 * Add the existing vertex to the network.
	 * Used to load an existing vertex, createVertex must be used to create a new one.
	 */
	public synchronized void addVertex(Vertex vertex) {
		if (vertex.getId() != null) {
			// Ensure the nextId sequence is consistent when restoring the network from storage.
			if (nextId <= vertex.getId().longValue()) {
				nextId = vertex.getId().longValue() + 1;
			}
		} else {
			vertex.setId(nextId());
		}
		this.verticesById.put(vertex.getId(), vertex);
		if (vertex.getData() != null) {
			if (vertex.getData() instanceof Data) {
				Data data = (Data)vertex.getData();
				if (data.getId() == 0) {
					data.setId(nextDataId());
				} else {
					// Ensure the nextId sequence is consistent when restoring the network from storage.
					if (nextDataId <= data.getId()) {
						nextDataId = data.getId() + 1;
					}
				}
				this.dataById.put(Long.valueOf(data.getId()), data);
			}
			this.verticesByData.put(vertex.getData(), vertex);
		}
		this.vertices.add(vertex);
		((BasicVertex) vertex).setNetwork(this);
	}
	
	/**
	 * Create a new vertex from the source.
	 * The source is from another network.
	 */
	public synchronized Vertex createVertex(Vertex source) {
		Vertex vertex = findById(source.getId());
		if (vertex == null) {
			vertex = findByData(source.getData());
		}
		if (vertex == null) {
			vertex = new BasicVertex();
			vertex.setName(source.getName());
			vertex.setData(source.getData());
			vertex.setAccessCount(source.getAccessCount());
			vertex.setAccessDate(source.getAccessDate());
			vertex.setPinned(source.isPinned());
			vertex.setCreationDate(source.getCreationDate());
			vertex.setConsciousnessLevel(source.getConsciousnessLevel());
			vertex.setId(source.getId());
			addVertex(vertex);
		}
		return vertex;
	}

	public Set<Vertex> getVertices() {
		return vertices;
	}

	protected void setVertices(Set<Vertex> vertices) {
		this.vertices = vertices;
	}
	
	public Network getParent() {
		return parent;
	}
	
	public void setParent(Network parent) {
		this.parent = parent;
	}

	public int size() {
		return getVertices().size();
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
			relationship.getSource().internalRemoveRelationship(relationship);
		}
		removeVertex(vertex);
	}
	
	/**
	 * Remove the vertex from the network.
	 * Note that the vertex must be no longer referenced by any other vertex in the network.
	 */
	public void removeVertex(Vertex vertex) {
		this.vertices.remove(vertex.getId());
		if (vertex.hasData()) {
			this.verticesByData.remove(vertex.getData());
		}
	}
	
	/**
	 * Return count of all vertices.
	 */
	public int countAll() {
		return findAll().size();
	}
	
	/**
	 * Return count of all vertices matching the query.
	 * Currently unable to process in memory.
	 */
	public int countAllLike(String filter) {
		return findAllLike(filter).size();
	}
	
	/**
	 * Return all vertices.
	 */
	public List<Vertex> findAll() {
		return new ArrayList<Vertex>(getVertices());
	}
	
	/**
	 * Return all vertices.
	 */
	public List<Vertex> findAll(int pageSize, int page) {
		return findAll();
	}
	
	/**
	 * Return all vertices matching the query.
	 * Currently unable to process in memory.
	 */
	public List<Vertex> findAllQuery(String query) {
		return new ArrayList<Vertex>();
	}
	
	/**
	 * Return all vertices matching the query.
	 * Currently unable to process in memory.
	 */
	@SuppressWarnings("rawtypes")
	public List<Vertex> findAllQuery(String query, Map parameters, int pageSize, int page) {
		return new ArrayList<Vertex>();
	}
	
	/**
	 * Return all vertices matching the query.
	 * Currently unable to process in memory.
	 */
	public List<Vertex> findAllQuery(String query, int max) {
		return new ArrayList<Vertex>();
	}
	
	/**
	 * Execute the native query.
	 */
	@SuppressWarnings("rawtypes")
	public List findByNativeQuery(String sql, Class type, int max) {
		return new ArrayList<Vertex>();
	}

	/**
	 * Execute and commit the native query.
	 */
	public int executeNativeQuery(String sql) {
		return 0;
	}

	/**
	 * Execute and commit the update query.
	 */
	public int executeQuery(String jpql) {
		return 0;
	}
	
	/**
	 * Return all vertices matching the filter.
	 */
	public List<Vertex> findAllLike(String filter) {
		Pattern pattern = Pattern.compile(filter.replace("*", ".*"));
		List<Vertex> results = new ArrayList<Vertex>();
		for (Vertex vertex : findAll()) {
			if (vertex.hasData()) {
				if (pattern.matcher(vertex.getDataValue()).matches()) {
					results.add(vertex);
				}
			}
		}
		return results;
	}
	
	/**
	 * Return all vertices matching the filter.
	 */
	public List<Vertex> findAllLike(String filter, int pageSize, int page) {
		return findAllLike(filter);
	}
	
	/**
	 * Return the vertex with the given data.
	 */
	public synchronized Vertex findByData(Object data) {
		if (data == null) {
			return null;
		}
		Vertex vertex = (Vertex) this.verticesByData.get(data);
		// If not local, lookup in parent and cloned into local.
		if ((vertex == null) && (this.parent != null)) {
			Vertex originalVertex = this.parent.findByData(data);
			if (originalVertex != null) {
				vertex = new BasicVertex(originalVertex);
				addVertex(vertex);
				vertex.incrementAccessCount();
			}
		}
		return vertex;
	}
	
	/**
	 * Return the lob data.
	 */
	public synchronized Data findData(Data data) {
		return this.dataById.get(data.getId());
	}
	
	/**
	 * Return the vertex with the given name.
	 */
	public synchronized Vertex findByName(String name) {
		if (name == null) {
			return null;
		}
		for (Vertex vertex : findAll()) {
			if (name.equals(vertex.getName())) {
				return vertex;
			}
		}
		return null;
	}
	
	/**
	 * Return the vertex with the given name.
	 */
	public synchronized Vertex findById(Number id) {
		if (id == null) {
			return null;
		}
		Vertex vertex = (Vertex) this.verticesById.get(id);
		// If not local, lookup in parent and cloned into local.
		if ((vertex == null) && (this.parent != null)) {
			Vertex originalVertex = this.parent.findById(id);
			if (originalVertex != null) {
				vertex = new BasicVertex(originalVertex);
				addVertex(vertex);
			}
		}
		return vertex;
	}

	/**
	 * Return a query builder.
	 */
	public CriteriaBuilder getCriteriaBuilder() {
		return null;
	}
	
	/**
	 * Find all relationships related to the vertex or of the vertex type.
	 */
	public synchronized List<Vertex> findAllInstances(Vertex type, Vertex relationship, Calendar start) {
		return new ArrayList<Vertex>();		
	}
	
	/**
	 * Execute the criteria query.
	 */
	@SuppressWarnings({ "rawtypes" })
	public synchronized List search(CriteriaQuery criteria, int page, int max) {
		return new ArrayList<Vertex>();		
	}
	
	/**
	 * Find all relationships related to the vertex or of the vertex relationship type.
	 */
	public synchronized List<Relationship> findAllRelationshipsTo(Vertex vertex) {
		List<Relationship> relationships = new ArrayList<Relationship>();
		Iterator<Vertex> iterator = findAll().iterator();
		// Remove all references.
		while (iterator.hasNext()) {
			Vertex next = iterator.next();
			Iterator<Relationship> allRelationships = next.allRelationships();
			while (allRelationships.hasNext()) {
				Relationship relationship = allRelationships.next();
				if (relationship.getTarget() == vertex) {
					relationships.add(relationship);
				} else if (relationship.getType() == vertex) {
					relationships.add(relationship);
				}
			}
		}
		return relationships;
	}
	
	/**
	 * Find all relationships related to the vertex by the vertex type.
	 */
	public synchronized List<Relationship> findAllRelationshipsTo(Vertex vertex, Vertex type) {
		List<Relationship> relationships = new ArrayList<Relationship>();
		Iterator<Vertex> iterator = findAll().iterator();
		// Remove all references.
		while (iterator.hasNext()) {
			Vertex next = iterator.next();
			Collection<Relationship> allRelationships = next.getRelationships(type);
			if (allRelationships != null) {
				for (Relationship relationship : allRelationships) {
					if (relationship.getTarget() == vertex) {
						relationships.add(relationship);
					}
				}
			}
		}
		return relationships;
	}
	
	/**
	 * Find all relationships related to the vertex by the vertex type.
	 */
	public synchronized List<Relationship> findAllRelationshipsTo(Vertex vertex, Vertex type, Date date) {
		List<Relationship> relationships = new ArrayList<Relationship>();
		Iterator<Vertex> iterator = findAll().iterator();
		// Remove all references.
		while (iterator.hasNext()) {
			Vertex next = iterator.next();
			Collection<Relationship> allRelationships = next.getRelationships(type);
			if (allRelationships != null) {
				for (Relationship relationship : allRelationships) {
					if (relationship.getTarget() == vertex && relationship.getCreationDate().after(date)) {
						relationships.add(relationship);
					}
				}
			}
		}
		return relationships;
	}

	public Map<Number, Vertex> getVerticesById() {
		return verticesById;
	}

	public void setVerticesById(Map<Number, Vertex> verticesById) {
		this.verticesById = verticesById;
	}

	public Map<Number, Data> getDataById() {
		return dataById;
	}

	public void setDataById(Map<Number, Data> dataById) {
		this.dataById = dataById;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + size()+ ")";			
	}

}