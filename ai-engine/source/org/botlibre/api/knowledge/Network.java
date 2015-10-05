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
package org.botlibre.api.knowledge;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.botlibre.Bot;
import org.botlibre.knowledge.Primitive;

/**
 * An interconnected set of vertices,
 * representing and knowledge-space.
 * Define methods for creating, removing and finding vertices.
 */

public interface Network {
	/**
	 * Resume after a merge/save.
	 */
	void resume();

	/**
	 * Merge the memory into the long term.
	 * This is similar to a transactional commit.
	 * The changes should also be persisted, as the long term should always just be a cache of the storage.
	 */
	void save();
	
	/**
	 * Create a new vertex in this network,
	 * assign the id.
	 */
	Vertex createVertex();
	
	/**
	 * Return the matching registered vertex, or register if missing.
	 */
	Vertex createVertex(Vertex vertex);
	
	/**
	 * Create a new vertex in this network with the data.
	 * If a vertex with the data already exists, then it is return as the data must be unique.
	 * If new a new vertex is created and a unique id assigned.
	 */
	Vertex createVertex(Object data);

	/**
	 * Return the relationship meta vertex.
	 */
	Vertex createMeta(Relationship relationship);
	
	/**
	 * Create a timestamp based on the current nanos.
	 */
	Vertex createTimestamp();

	/**
	 * Remove the vertex and all references to it from the network.
	 */
	void removeVertexAndReferences(Vertex vertex);
	
	/**
	 * Remove the vertex from the network.
	 * Note that the vertex must be no longer referenced by any other vertex in the network.
	 */
	void removeVertex(Vertex vertex);

	/**
	 * Remove the relationship from the network.
	 * Note that the relationship must be no longer referenced by any other vertex in the network.
	 */
	void removeRelationship(Relationship relationship);
	
	/**
	 * Return all vertices.
	 */
	List<Vertex> findAll();
	
	/**
	 * Return all vertices.
	 */
	List<Vertex> findAll(int pageSize, int page);

	/**
	 * Return all vertices matching the filter.
	 */
	List<Vertex> findAllLike(String filter);

	/**
	 * Return all vertices matching the filter.
	 */
	List<Vertex> findAllLike(String filter, int pageSize, int page);

	/**
	 * Return all vertices matching the query.
	 */
	@SuppressWarnings("rawtypes")
	List findAllQuery(String query);

	/**
	 * Return all vertices matching the query.
	 */
	@SuppressWarnings("rawtypes")
	List findAllQuery(String query, Map parameters, int pageSize, int page);
	
	/**
	 * Return count of all vertices.
	 */
	int countAll();

	/**
	 * Return count of vertices matching the filter.
	 */
	int countAllLike(String filter);
	
	/**
	 * Return all vertices matching the query.
	 */
	@SuppressWarnings("rawtypes")
	List findAllQuery(String query, int max);
	
	/**
	 * Execute the native query.
	 */
	@SuppressWarnings("rawtypes")
	List findByNativeQuery(String sql, Class type, int max);

	/**
	 * Execute and commit the native query.
	 */
	int executeNativeQuery(String sql);

	/**
	 * Execute and commit the update query.
	 */
	int executeQuery(String jpql);

	/**
	 * Find all relationships related to the vertex or of the vertex type.
	 */
	List<Vertex> findAllInstances(Vertex type, Vertex relationship, Calendar start);
	
	/**
	 * Return a query builder.
	 */
	CriteriaBuilder getCriteriaBuilder();
	
	/**
	 * Execute the criteria query.
	 */
	@SuppressWarnings({ "rawtypes" })
	List search(CriteriaQuery criteria, int page, int max);

	/**
	 * Find all relationships related to the vertex or of the vertex relationship type.
	 */
	List<Relationship> findAllRelationshipsTo(Vertex vertex);

	/**
	 * Find all relationships related to the vertex by the type.
	 */
	List<Relationship> findAllRelationshipsTo(Vertex vertex, Vertex type);
	
	/**
	 * Return the vertex with the given data.
	 */
	Vertex findByData(Object data);
	
	/**
	 * Return the lob data.
	 */
	Data findData(Data data);

	/**
	 * Return the vertex with the given name.
	 */
	Vertex findByName(String name);
	
	/**
	 * Return the vertex with the given id.
	 */
	Vertex findById(Number id);

	/**
	 * Return the parent network.
	 * A network is essentially a nested-transactional sub-graph of the parent.
	 */
	Network getParent();

	/**
	 * Set the parent network.
	 * A network is essentially a nested-transactional sub-graph of the parent.
	 */
	void setParent(Network parent);
	
	/**
	 * Merge the vertices and relations of the network into the source network.
	 */
	void merge(Network network);
	
	/**
	 * Merge the vertex into this network.
	 */
	void merge(Vertex source);

	/**
	 * Merge the vertex into this network from an import.
	 * This is used from importing another memory.
	 */
	void importMerge(Vertex source, Map<Vertex, Vertex> identitySet);

	/**
	 * Return the total number of vertices stored.
	 */
	int size();

	/**
	 * Associate alternative cases of the word with the meaning, type, tense, etc..
	 */
	void associateCaseInsensitivity(Vertex word);

	/**
	 * Associate alternative cases of the word with the meaning.
	 */
	void associateCaseInsensitivity(String word, Vertex meaning);
	
	/**
	 * Clear all vertices from the network.
	 */
	void clear();

	/**
	 * Create a new instance of the type.
	 */
	Vertex createInstance(Primitive type);

	/**
	 * Create a new instance of the type.
	 */
	Vertex createInstance(Vertex type);
	
	/**
	 * Tokenize the text into its words and create a vertex representation of the word or compound word.
	 */
	Vertex createWord(String text);
	
	/**
	 * Tokenize the text into its words and create a vertex representation of the sentence fragment.
	 */
	Vertex createFragment(String text);
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 */
	Vertex createSentence(String text);
	
	/**
	 * Tokenize the sentence pattern into its words and wildcrads, and create a vertex representation.
	 */
	Vertex createPattern(String text);
	
	/**
	 * Compile the forumla response.
	 */
	Vertex createFormula(String text);

	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 */
	Vertex createSentence(String text, boolean generated);
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 */
	Vertex createSentence(String text, boolean generated, boolean reduced);
	
	/**
	 * Tokenize the sentence into its words and create a vertex representation.
	 */
	Vertex createSentence(String text, boolean generated, boolean reduced, boolean whitespace);
	
	/**
	 * Convert the sentence to a paragraph if it has multiple phrases.
	 */
	Vertex createParagraph(Vertex sentence);
	
	/**
	 * Tokenize the paragraph into its sentences and create a vertex representation.
	 */
	Vertex createParagraph(String text);

	/**
	 * Create the word, and its meaning.
	 * If the word or meaning exist, use the existing one.
	 */
	Vertex createObject(String name);
	
	/**
	 * Create the primitive and associate the word to it.
	 */
	Vertex createPrimitive(String name);

	/**
	 * Create the word, and a new meaning.
	 */
	Vertex createNewObject(String name);

	/**
	 * Create a new anonymous speaker.
	 */
	Vertex createAnonymousSpeaker();
	
	/**
	 * Find or create the speaker with the name.
	 */
	Vertex createSpeaker(String name);
	
	/**
	 * Find or create the speaker with the name.
	 */
	Vertex createName(String name);
	
	/**
	 * Check if the sentence has been reduced, if not, then reduce.
	 */
	void checkReduction(Vertex sentence);

	/**
	 * Return the associated Bot instance.
	 */
	Bot getBot();

	/**
	 * Set the associated Bot instance.
	 */
	void setBot(Bot Bot);
}