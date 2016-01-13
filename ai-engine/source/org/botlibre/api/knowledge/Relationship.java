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

import java.util.Date;


/**
 * Relationship between two vertices.
 * The relation itself is a vertex as it has an identity associated to it,
 * and the relation may be related to other relations/vertices.
 */

public interface Relationship extends Comparable<Relationship> {

	Long getId();
	
	Vertex getSource();

	Vertex getTarget();

	Vertex getType();
	
	Vertex getMeta();
	
	boolean hasMeta();

	/**
	 * Return the fuzzy value of the certainty of the relationship's correctness.
	 * Values are between 0 and 1.
	 */
	float getCorrectness();

	/**
	 * Set the fuzzy value of the certainty of the relationship's correctness.
	 * Values are between 0 and 1.
	 */
	void setCorrectness(float correctness);
	
	/**
	 * Return if the relationship is inverse, i.e. know to not exist.
	 */
	boolean isInverse();

	void setSource(Vertex source);

	void setTarget(Vertex target);

	void setType(Vertex type);
	
	void setMeta(Vertex meta);
	
	/**
	 * Return the index of the relationships in the source's relationships of that type.
	 */
	int getIndex();
	
	/**
	 * Set the index of the relationships in the source's relationships of that type.
	 */	
	void setIndex(int index);
	
	/**
	 * Return if the relationship has an index (preset order).
	 */	
	boolean hasIndex();

	/**
	 * Return the date the relationship was created.
	 */
	Date getCreationDate();
	
	/**
	 * Set the date the relationship was created.
	 */
	void setCreationDate(Date creationDate);

	/**
	 * Return the date the relationship was last accessed.
	 */
	Date getAccessDate();

	/**
	 * Set the date the relationship was last accessed.
	 */
	void setAccessDate(Date accessDate);

	/**
	 * Return the number of times the relationship has been accessed.
	 */
	int getAccessCount();

	/**
	 * Set the number of times the relationship has been accessed.
	 */
	void setAccessCount(int accessCount);

	/**
	 * Record that the relationship was accessed, update the access time and increment the access count.
	 */
	void incrementAccessCount();
	
	/**
	 * Increase the relationship's level of consciousness.
	 */
	void incrementConsciousnessLevel();
	
	/**
	 * Increase the relationship's level of consciousness by the amount.
	 */
	void incrementConsciousnessLevel(int amount);

	/**
	 * Decrease the relationship's level of consciousness.
	 */
	void decrementConsciousnessLevel();

	/**
	 * Decrease the relationship's level of consciousness by the amount.
	 */
	void decrementConsciousnessLevel(int amount);

	/**
	 * Return the relationship's level of consciousness.
	 */
	int getConsciousnessLevel();

	/**
	 * Set the relationship's level of consciousness.
	 */
	void setConsciousnessLevel(int consciousnessLevel);

	/**
	 * Return if the relationship is pinned to memory, and will not be forgotten.
	 */
	boolean isPinned();

	/**
	 * Set if the relationship should be pinned to memory.
	 * Pinned relationship will not be forgotten.
	 */
	void setPinned(boolean pinned);

	boolean checkHashCode();

}