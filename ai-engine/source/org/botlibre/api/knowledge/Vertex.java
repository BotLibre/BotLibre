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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.botlibre.knowledge.Primitive;

/**
 * Represents a piece of knowledge.
 * Vertex is a very simple structure mainly comprised of its relationships,
 * this gives the system a very simple and extendable structure.
 * Basic implementation to allow subclasses to avoid defining some of the basic stuff.
 * The data attribute allows for vertices that represents something tangible such as
 * text, sound, image, number, etc.  The data is also indexed within a network, so is the
 * only guaranteed way to lookup an existing vertex.
 */

public interface Vertex {
		
	/**
	 * Unique id for the vertex.
	 * All vertices should define a unique identifier.
	 */
	Long getId();
	
	/**
	 * Set the vertex id.
	 * The id can only be set when loading or creating a vertex.
	 */
	void setId(Long id);


	/**
	 * Return the name of the vertex.
	 * The name is used as a hint to refer to what the vertex represents.
	 */
	String getName();

	/**
	 * Set the name of the vertex.
	 * The name is used as a hint to refer to what the vertex represents.
	 */
	void setName(String name);

	/**
	 * Return group id of script.
	 * Used to optimize script loading.
	 */
	long getGroupId();

	/**
	 * Set group id of script.
	 * Used to optimize script loading.
	 */
	void setGroupId(long groupId);
	
	/**
	 * Return word count for a sentence.
	 * Used to detect corrupt/forgotten sentences.
	 */
	int getWordCount();

	/**
	 * Set word count for a sentence.
	 * Used to detect corrupt/forgotten sentences.
	 */
	void setWordCount(int wordCount);
	
	/**
	 * Return the String value of the data.
	 */
	String getDataValue();

	/**
	 * Return the String name of the data's type.
	 */
	String getDataType();
	
	/**
	 * Set the internal data-type of the vertex.
	 */
	void setType(String type);
		
	/**
	 * Print the object's data such as a sentence or paragraph.
	 */
	String printString();
	
	/**
	 * Allows some piece of data to be associated with the vertex.
	 * May be text, image, sound, numeric, date, primitive, etc.
	 */
	Object getData();
	
	/**
	 * Allows some piece of data to be associated with the vertex.
	 * May be text, image, sound, numeric, date, primitive, etc.
	 */
	void setData(Object data);

	/**
	 * Most pure knowledge vertices have no data,
	 * this provides an easy test method.
	 */
	boolean hasData();
	
	/**
	 * Some vertices have no name,
	 * this provides an easy test method.
	 */
	boolean hasName();

	/**
	 * Compare the vertices ignoring case.
	 */
	boolean equalsIgnoreCase(Vertex vertex);
	
	/**
	 * Return the network the vertex is derived from.
	 */
	Network getNetwork();
	
	/**
	 * Set the network the vertex is derived from.
	 */
	void setNetwork(Network network);

	/**
	 * Return the date the vertex was created.
	 */
	Date getCreationDate();
	
	/**
	 * Set the date the vertex was created.
	 */
	void setCreationDate(Date creationDate);

	/**
	 * Return the date the vertex was last accessed.
	 */
	Date getAccessDate();

	/**
	 * Set the date the vertex was last accessed.
	 * Access is considered moving from long term to short term memory.
	 */
	void setAccessDate(Date accessDate);

	/**
	 * Return the number of times the vertex has been accessed.
	 * Access is considered moving from long term to short term memory.
	 */
	int getAccessCount();

	/**
	 * Set the number of times the vertex has been accessed.
	 */
	void setAccessCount(int accessCount);

	/**
	 * Record that the vertex was accessed, update the access time and increment the access count.
	 */
	void incrementAccessCount();
	
	/**
	 * Increase the vertices's level of consciousness.
	 */
	void incrementConsciousnessLevel();
	
	/**
	 * Increase the vertices's level of consciousness by the amount.
	 */
	void incrementConsciousnessLevel(int amount);

	/**
	 * Decrease the vertices's level of consciousness.
	 */
	void decrementConsciousnessLevel();

	/**
	 * Decrease the vertices's level of consciousness by the amount.
	 */
	void decrementConsciousnessLevel(int amount);

	/**
	 * Return the vertices's level of consciousness.
	 */
	int getConsciousnessLevel();

	/**
	 * Set the vertices's level of consciousness.
	 */
	void setConsciousnessLevel(int consciousnessLevel);

	/**
	 * Return if the vertex is pinned to memory, and will not be forgotten.
	 */
	boolean isPinned();

	/**
	 * Set if the vertex should be pinned to memory.
	 * Pinned vertices will not be forgotten.
	 */
	void setPinned(boolean pinned);

	/**
	 * Return if the vertex is temporary, and not persistent.
	 */
	boolean isTemporary();

	/**
	 * Set if the vertex is temporary, and not persistent.
	 */
	void setIsTemporary(boolean pinned);
	
	/**
	 * Pin the vertex and all of its descendants into memory.
	 * This can be used for important data such as language rules.
	 */
	void pinDescendants();
	
	/**
	 * Unpin the vertex and all of its descendants from memory.
	 * This can be used to release removed language rules.
	 */
	void unpinDescendants();
	
	/**
	 * Return if the relationships have been instantiated.
	 */
	boolean hasRelationships();

	/**
	 * Return all of the relationships of the primitive type, sorted by index.
	 */
	List<Relationship> orderedRelationships(Primitive primitive);
	
	/**
	 * Return all of the relationships of the type, sorted by index.
	 */
	List<Relationship> orderedRelationships(Vertex relationshipType);

	
	/**
	 * Return all of the relationships of the type, sorted by consciousness level.
	 */
	List<Relationship> orderedRelationshipsByConsciousness(Primitive primitive);
	
	/**
	 * Return all of the relationships of the type, sorted by consciousness level.
	 */
	List<Relationship> orderedRelationshipsByConsciousness(Vertex relationshipType);
	
	/**
	 * Return all of the relationships targets of the primitive type, sorted by index.
	 */
	List<Vertex> orderedRelations(Primitive primitive);
	
	/**
	 * Return all of the relationship targets of the type, sorted by index.
	 */
	List<Vertex> orderedRelations(Vertex relationshipType);
	
	/**
	 * Return any relationship target of the primitive type.
	 */
	Vertex getRelationship(Primitive type);

	/**
	 * Return any relationship target of the type.
	 */
	Vertex getRelationship(Vertex type);

	/**
	 * Return any relationship target of the primitive type.
	 */
	Collection<Relationship> getRelationships(Primitive type);

	/**
	 * Return the relationship to the target of the primitive type.
	 */
	Relationship getRelationship(Primitive type, Primitive target);

	/**
	 * Return the relationship to the target of the primitive type.
	 */
	Relationship getRelationship(Primitive type, Vertex target);

	/**
	 * Return the relationship to the target of the primitive type.
	 */
	Relationship getRelationship(Vertex type, Vertex target);

	/**
	 * Return all relationships of the type.
	 * A vertex may have a relation to any other vertex including itself,
	 * relations may be of different types such as "classification", "belongs", "contained", etc.
	 * The real knowledge of the vertex is define by its relationships.
	 */
	Collection<Relationship> getRelationships(Vertex relationshipType);

	/**
	 * Return the last of the ordered relationship, or null.
	 */
	Vertex lastRelationship(Vertex type);

	/**
	 * Fix the corrupted relationship index order.
	 * The order can get corrupted when relationships are forgotten.
	 */
	void fixRelationships(Vertex type);


	/**
	 * Return the the vertex has exceeded it relationships maximum.
	 */
	boolean isDirty();

	/**
	 * Mark the vertex for forgetfullness.
	 */
	void setIsDirty(boolean isDirty);
	
	/**
	 * Return the fromLast last of the ordered relationship, or null.
	 * i.e. 2nd last, or 3rd last, etc.
	 */
	Vertex lastRelationship(Vertex type, int fromLast);

	/**
	 * Return the fromLast last of the ordered relationship, or null.
	 * i.e. 2nd last, or 3rd last, etc.
	 */
	Vertex lastRelationship(Primitive type, int fromLast);

	/**
	 * Return the last of the ordered relationship, or null.
	 */
	Vertex lastRelationship(Primitive type);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level.
	 */
	Vertex mostConscious(Vertex type);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level.
	 */
	Vertex mostConscious(Primitive type);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	Vertex mostConscious(Vertex type, float min);
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	Relationship mostConsciousRelationship(Primitive type);
	
	/**
	 * Return the relationship related by the type, with the high consciousness level and correctness greater than the value.
	 */
	Relationship mostConsciousRelationship(Primitive type, float min);
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	Relationship mostConsciousRelationship(Vertex type);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level and correctness greater than the value.
	 */
	Vertex mostConscious(Primitive type, float min);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level and correctness greater than the value.
	 */
	Vertex nextMostConscious(Primitive type, Set<Vertex> ignoring);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level and correctness greater than the value.
	 */
	Vertex nextMostConscious(Vertex type, Set<Vertex> ignoring, float min, boolean inverse);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level and correctness greater than the value.
	 */
	Relationship nextMostConsciousRelationship(Vertex type, Set<Vertex> ignoring, float min, boolean inverse);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level and correctness greater than the value.
	 */
	Vertex nextMostConscious(Vertex type, Vertex ignoring, float min);
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	Relationship nextMostConsciousRelationship(Vertex type, Vertex ignoring);
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	Relationship nextMostConsciousRelationship(Primitive type, Vertex ignoring);
	
	/**
	 * Return the relationship related by the type, with the high consciousness level greater than the value.
	 */
	Relationship nextMostConsciousRelationship(Primitive type, Vertex ignoring, float min);
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	Vertex nextMostConscious(Primitive type, Vertex ignoring, float min);

	/**
	 * Return the target vertex related by the type, of the classification, with the high consciousness level.
	 */
	Vertex mostConscious(Vertex type, Vertex classification);

	/**
	 * Return the target vertex inversely/negatively related by the type, with the high consciousness level.
	 */
	Vertex mostConscious(Vertex type, float min, boolean inverse);

	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType);

	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex defaultAssociate);

	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2);

	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2, Vertex associate3, Vertex associateType3);

	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2, Collection<Relationship> associate3, Vertex associateType3, Vertex defaultAssociate);

	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Collection<Relationship> associate2, Vertex associateType2, Collection<Relationship> associate3, Vertex associateType3, Vertex defaultAssociate);
	
	/**
	 * Return the target vertex related by the type, that is also most correctly related to the associate vertex by the relationship.
	 * If no related vertices are related to the associate, then return the most conscious.
	 */
	Vertex mostConsciousWithAssoiate(Vertex type, Vertex associate, Vertex associateType);
	
	/**
	 * Return the target vertex related by the type, that is also most correctly related to the associate vertex by the relationship.
	 * If no related vertices are related to the associate, then return the most conscious.
	 */
	Vertex mostConsciousWithAssoiates(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2);
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	void associateAll(Vertex relationshipType, Vertex target, Vertex type);
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	void associateAll(Primitive relationshipType, Vertex target, Primitive type);

	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	void weakAssociateAll(Vertex relationshipType, Vertex target, Vertex type, float correctnessMultiplier);
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	void weakAssociateAll(Primitive relationshipType, Vertex target, Primitive type, float correctnessMultiplier);

	/**
	 * Dissociate each of the relationship target vertices with the target vertex by the type.
	 */
	void inverseAssociateAll(Primitive relationshipType, Vertex source, Primitive type);

	/**
	 * Dissociate each of the relationship target vertices with the target vertex by the type.
	 */
	void inverseAssociateAll(Vertex relationshipType, Vertex source, Vertex type);

	/**
	 * Return all of the relationships.
	 */
	Collection<Relationship> getAllRelationships();
	
	/**
	 * Return all relationships.
	 * Relationships are stored as a map keyed on the relationship type,
	 * with ordered list of relationship targets.
	 */
	Map<Vertex, Map<Relationship, Relationship>> getRelationships();
	
	/**
	 * Provides an easier method of traversing all the relations of a vertex.
	 */
	Iterator<Relationship> allRelationships();
	
	/**
	 * Create a copy of the vertex with all of the same relationships.
	 */
	Vertex copy();

	/**
	 * Create a copy of the vertex with only the id.
	 */
	Vertex detach();
	
	/**
	 * Iterator over all related vertices to the vertex.
	 */
	void iterate(VertexIterator iterator);
	
	/**
	 * Return the total number of all relationships.
	 */
	int totalRelationships();
	
	/**
	 * Provides an easier method of traversing all the relations of a vertex.
	 */
	Iterator<Relationship> orderedAllRelationships();

	/**
	 * Add the relation of the relationship primitive type to the target primitive.
	 */
	Relationship addRelationship(Primitive type, Primitive target);
	
	/**
	 * Add the relation of the relationship primitive type to the target vertex.
	 */
	Relationship addRelationship(Primitive type, Vertex target);
	
	/**
	 * Add the relation of the type to the other vertex.
	 */
	Relationship addRelationship(Vertex type, Vertex target);
	
	/**
	 * Add the relation of the type to the other vertex.
	 * Ignore bookkeeping, don't increment correctness.
	 */
	Relationship addRelationship(Vertex type, Vertex target, int index, boolean internal);

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * The correctness decreases the correctness of the relation.
	 */
	Relationship addWeakRelationship(Primitive type, Primitive target, float correctnessMultiplier);

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * The correctness decreases the correctness of the relation.
	 */
	Relationship addWeakRelationship(Primitive type, Vertex target, float correctnessMultiplier);
	
	/**
	 * Add the relation of the relationship type to the target vertex.
	 * The correctness decreases the correctness of the relation.
	 */
	Relationship addWeakRelationship(Vertex type, Vertex target, float correctnessMultiplier);
	
	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	Relationship addRelationship(Primitive type, Vertex target, int index);
	
	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	Relationship addRelationship(Primitive type, Primitive target, int index);

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	Relationship addRelationship(Vertex type, Vertex target, int index);

	/**
	 * Add the relation ensuring uniqueness.
	 */
	Relationship addRelationship(Relationship relationship, boolean internal);
	
	/**
	 * Remove the relation of the type from the other vertex.
	 * This creates an inverse relationship.
	 */
	Relationship removeRelationship(Vertex type, Vertex target);
	
	/**
	 * Remove the relation of the primitive type from the other vertex.
	 * This creates an inverse relationship.
	 */
	Relationship removeRelationship(Primitive type, Vertex target);
	
	/**
	 * Remove the relation of the primitive type from the other vertex.
	 * This creates an inverse relationship.
	 */
	Relationship removeRelationship(Primitive type, Primitive target);

	/**
	 * Replace the relationship with the new target at the same index.
	 */
	void replaceRelationship(Relationship oldRelationship, Vertex newTarget);

	/**
	 * Set the relationship, removing the old value.
	 */
	void setRelationship(Primitive type, Vertex newValue);
	
	/**
	 * Set the relationship, removing the old value.
	 */
	void setRelationship(Vertex type, Vertex newValue);
	
	/**
	 * Remove the relation.
	 */
	void internalRemoveRelationship(Relationship relationship);
	
	/**
	 * Remove the relationships of the type.
	 */
	void internalRemoveRelationships(Primitive type);
		
	/**
	 * Remove the relationships of the type.
	 */
	void internalRemoveRelationships(Vertex type);
	
	/**
	 * Pin the targets of all relationships to memory.
	 */
	void pinChildren();
	
	/**
	 * Unpin the targets of all relationships from memory.
	 */
	void unpinChildren();
	
	/**
	 * Remove all relationships.
	 */
	void internalRemoveAllRelationships();

	/**
	 * Return if the vertex has a relationship of the type to the target.
	 * Include inverses.
	 */
	boolean internalHasRelationship(Vertex type, Vertex target);
	
	/**
	 * Apply the quotient.
	 * Apply the equation to the variable matches.
	 */
	Vertex applyQuotient(Map<Vertex, Vertex> variables, Network network);

	/**
	 * Evaluates any eval functions in the equation or formula..
	 * This is used by learn.
	 */
	public Vertex applyEval(Map<Vertex, Vertex> variables, Network network);
	
	/**
	 * Compare if the two vertices match.
	 * Consider variables.
	 * Used for rule processing.
	 */
	Boolean matches(Vertex vertex, Map<Vertex, Vertex> matches);

	/**
	 * Compare if the two vertices match.
	 * Consider variables.
	 * Used for rule processing.
	 */
	boolean collectMatches(Vertex vertex, Map<Vertex, Set<Vertex>> matches);
	
	/**
	 * Return if the vertex is a system primitive.
	 */
	boolean isPrimitive();

	/**
	 * Return if the vertex is the vertex for the data.
	 */
	boolean is(Object data);
	
	/**
	 * Return if the vertex is an instantiation of the primitive type.
	 */
	boolean instanceOf(Primitive type);

	/**
	 * Return if the vertex is an instantiation of the type.
	 */
	boolean instanceOf(Vertex type);

	/**
	 * Return if the vertex is a variable.
	 */
	boolean isVariable();

	/**
	 * Return if the vertex is a list.
	 */
	boolean isList();
	
	/**
	 * Return if the vertex is an equation.
	 */
	boolean isEquation();

	/**
	 * Return if the vertex has a relationship of the type primitive.
	 */
	boolean hasRelationship(Primitive type);

	/**
	 * Return if the vertex has a relationship of the type primitive to the target primitive.
	 */
	boolean hasRelationship(Primitive type, Primitive target);
	
	/**
	 * Return if the vertex has a relationship of the type primitive to the target.
	 */
	boolean hasRelationship(Primitive type, Vertex target);
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	boolean hasRelationship(Vertex type, Vertex target);
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	boolean hasOrInheritsRelationship(Vertex type, Vertex target);
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	boolean hasOrInheritsRelationship(Vertex type, Vertex target, Map<Vertex, Vertex> recursion);
	
	/**
	 * Return if the vertex has any relationship of the type.
	 */
	boolean hasRelationship(Vertex type);
	
	/**
	 * Return if the vertex has any relationship to any target.
	 */
	boolean hasAnyRelationshipToTarget(Vertex target);
	
	/**
	 * Return if the vertex has any relationship to any target that is an instantiation of the classification.
	 */
	boolean hasAnyRelationshipToTargetOfType(Vertex classification);
	
	/**
	 * Return the most conscious target the vertex has any relationship to that is an instantiation of the classification.
	 */
	Vertex mostConsciousTargetOfType(Vertex classification);
	
	/**
	 * Return if the vertex has an inverse/negative relationship of the type to the target.
	 */
	boolean hasInverseRelationship(Primitive type, Primitive target);
	
	/**
	 * Return if the vertex has an inverse/negative relationship of the type to the target.
	 */
	boolean hasInverseRelationship(Primitive type, Vertex target);
	
	/**
	 * Return if any of the associates of the vertex have an inverse/negative relationship of the type to the target.
	 */
	boolean hasAnyAssociatedInverseRelationship(Primitive associate, Vertex target, Primitive type);
	
	/**
	 * Return if any of the associates of the vertex have an inverse/negative relationship of the type to the target.
	 */
	boolean hasAnyAssociatedInverseRelationship(Vertex associate, Vertex target, Vertex type);
	
	/**
	 * Return if the vertex has an inverse/negative relationship of the type to the target.
	 */
	boolean hasInverseRelationship(Vertex type, Vertex target);
	
	/**
	 * Return if the vertex has an inverse/negative relationship of the type to the target.
	 */
	boolean hasOrInheritsInverseRelationship(Vertex type, Vertex target);
	
	/**
	 * Return if the vertex has an inverse/negative relationship of the type to the target.
	 */
	boolean hasOrInheritsInverseRelationship(Vertex type, Vertex target, Map<Vertex, Vertex> recursion);
	
	/**
	 * PERF: Used to check response without accessing relationships.
	 */
	boolean hasAnyResponseRelationship();
	
	/**
	 * Return a short toString, or header of the vertex.
	 */
	String description();
	
	/**
	 * Set the original long term vertex the short term vertex was derived from.
	 */
	void setOriginal(Vertex original);

	/**
	 * User friendly toString.
	 */
	String displayString();
	
	void init();
}