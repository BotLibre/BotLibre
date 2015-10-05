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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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

public class BasicNetwork extends AbstractNetwork implements Serializable {

	private static final long serialVersionUID = 1L;
		
	protected Network parent;
	protected Set<Vertex> verticies = null;
	protected Map<Number, Vertex> verticiesById = null;

	protected static long nextId() {
		return nextId++;
	}
	
	public BasicNetwork() {
		this(false);
	}
	
	public BasicNetwork(boolean isShortTerm) {
		super(isShortTerm);
		this.verticies = new HashSet<Vertex>();
		this.verticiesById = new HashMap<Number, Vertex>();
		this.verticiesByData = new HashMap<Object, Vertex>();
	}
	
	public BasicNetwork(Network parent) {
		this(true);
		this.parent = parent;
	}
	
	protected void addRelationship(Relationship relationship) {
		// Nothing required by default.
	}
	
	public void resume() {
		getBot().log(this, "Resuming", Bot.FINE, this);
		Set<Vertex> newVerticies = new HashSet<Vertex>(Math.max(this.verticies.size(), MAX_SIZE));
		// Shrink to fixed size.
		int level = 1;
		while ((this.verticies.size() > MAX_SIZE) && (level < 256)) {
			Iterator<Vertex> iterator = this.verticies.iterator();
			while ((this.verticies.size() > MAX_SIZE) && iterator.hasNext()) {
				Vertex vertex = iterator.next();
				if ((!vertex.isPrimitive()) && vertex.getConsciousnessLevel() <= level) {
					iterator.remove();
				}
			}
			level = level * 2;
		}
		newVerticies.addAll(this.verticies);
		// Reset originals and clear relationships.
		if (getParent() != null) {
			for (Vertex vertex : newVerticies) {
				Vertex original = getParent().findById(vertex.getId());
				vertex.setOriginal(original);
			}
		}
		// Reset id hashes.
		this.verticies = newVerticies;
		this.verticiesById = new HashMap<Number, Vertex>();
		for (Vertex vertex : newVerticies) {
			this.verticiesById.put(vertex.getId(), vertex);
		}
		this.verticiesByData = new HashMap<Object, Vertex>();
		for (Vertex vertex : newVerticies) {
			if (vertex.getData() != null) {
				this.verticiesByData.put(vertex.getData(), vertex);
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
		clone.setVerticies(new HashSet<Vertex>(getVerticies()));
		clone.setVerticiesById(new HashMap<Number, Vertex>(getVerticiesById()));
		clone.setVerticiesByData(new HashMap<Object, Vertex>(getVerticiesByData()));
		return clone;
	}

	/**
	 * Clear all vertices from the network.
	 */
	public synchronized void clear() {
		this.verticies = new HashSet<Vertex>();
		this.verticiesById = new HashMap<Number, Vertex>();
		this.verticiesByData = new HashMap<Object, Vertex>();
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
			getVerticiesById().put(vertex.getId(), vertex);
		}
		if (vertex.getData() != null) {
			getVerticiesByData().put(vertex.getData(), vertex);
		}
		getVerticies().add(vertex);
		((BasicVertex) vertex).setNetwork(this);
	}

	protected Set<Vertex> getVerticies() {
		return verticies;
	}

	protected void setVerticies(Set<Vertex> verticies) {
		this.verticies = verticies;
	}
	
	public Network getParent() {
		return parent;
	}
	
	public void setParent(Network parent) {
		this.parent = parent;
	}

	public int size() {
		return getVerticies().size();
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
		getVerticies().remove(vertex.getId());
		if (vertex.hasData()) {
			getVerticiesByData().remove(vertex.getData());
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
		return new ArrayList<Vertex>(getVerticies());
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
		Vertex vertex = (Vertex) getVerticiesByData().get(data);
		// If not local, lookup in parent and cloned into local.
		if ((vertex == null) && (getParent() != null)) {
			Vertex originalVertex = getParent().findByData(data);
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
		return data;
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
		Vertex vertex = (Vertex) getVerticiesById().get(id);
		// If not local, lookup in parent and cloned into local.
		if ((vertex == null) && (getParent() != null)) {
			Vertex originalVertex = getParent().findById(id);
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

	public Map<Number, Vertex> getVerticiesById() {
		return verticiesById;
	}

	public void setVerticiesById(Map<Number, Vertex> verticiesById) {
		this.verticiesById = verticiesById;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + size()+ ")";			
	}

}