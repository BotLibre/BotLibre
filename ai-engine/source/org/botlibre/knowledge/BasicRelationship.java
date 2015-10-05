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

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Date;

import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.util.Utils;

/**
 * Meta reference.
 * Basic implementation to allow subclasses to avoid defining some of the basic stuff.
 */

public class BasicRelationship implements Relationship, Comparable<Relationship>, Serializable {

	private static final long serialVersionUID = 1L;

	protected Long id;
	protected Vertex type;
	protected Vertex source;
	protected Vertex target;
	protected Vertex meta;
	
	/** Index of the relationships in the source's relationships of that type. */
	protected int index = -1;

	/** Fuzzy value of the certainty of the relationship's correctness, between 0-1. */
	protected float correctness = 0.5f;
	
	protected Date creationDate;
	protected Date accessDate;
	protected int accessCount;
	protected boolean pinned;
	protected int consciousnessLevel;
	protected int hashCode;
	
	public BasicRelationship() {
		super();
	}
	
	public BasicRelationship(Vertex source, Vertex type, Vertex target) {
		super();
		this.source = source;
		this.target = target;
		this.type = type;
	}
	
	public Long getId() {
		return id;
	}

	/**
	 * Set the relationship id.
	 * The id can only be set when loading or creating a relationship.
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	public boolean equals(Object another) {
		if (this == another) {
			return true;
		}
		if (hashCode() != another.hashCode()) {
			return false;
		}
		if (!(another instanceof Relationship)) {
			return false;
		}

		Relationship relationship = (Relationship) another;
		if ((this.id != null) && this.id.equals(relationship.getId())) {
		  return true;
		}
		
		// Allow preset ordering, if both index and not the same, then not equal.
		if (hasIndex() && relationship.hasIndex()) {
			if (this.index != relationship.getIndex()) {
				return false;
			}
		}

		return this.source.equals(relationship.getSource())
			&& this.target.equals(relationship.getTarget())
			&& this.type.equals(relationship.getType());
	}

	/**
	 * Compare the relationships by index, to allow sorting.
	 */
	public int compareTo(Relationship another) {
		if (this == another) {
		  return 0;
		}
		int index = ((Relationship)another).getIndex();
		if (getIndex() > index ) {
			return 1;
		} else if (index == getIndex()) {
			return 0;
		} else {
			return -1;
		}
	}

	/**
	 * Return the fuzzy value of the certainty of the relationship's correctness.
	 * Values are between 0 and 1.
	 */
	public float getCorrectness() {
		return correctness;
	}

	/**
	 * Set the fuzzy value of the certainty of the relationship's correctness.
	 * Values are between 0 and 1.
	 */
	public void setCorrectness(float correctness) {
		this.correctness = correctness;
	}
	
	/**
	 * Return if the relationship is inverse, i.e. know to not exist.
	 */
	public boolean isInverse() {
		return getCorrectness() < 0;
	}
	
	/**
	 * Return the index of the relationships in the source's relationships of that type.
	 */
	public int getIndex() {
		return this.index;
	}

	/**
	 * Set the index of the relationships in the source's relationships of that type.
	 */	
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Return if the relationship has an index (preset order).
	 */	
	public boolean hasIndex() {
		return this.index != -1;
	}
	
	public Vertex getType() {
		return type;
	}
	
	public Vertex getMeta() {
		return meta;
	}
	
	public boolean hasMeta() {
		return meta != null;
	}
	
	public Vertex getSource() {
		return source;
	}
	
	public Vertex getTarget() {
		return target;
	}
	
	public void resetHashCode() {
		this.hashCode = 0;
	}
	
	public int hashCode() {
		if (this.hashCode != 0) {
			return this.hashCode;
		}
		if ((this.type == null) || (this.target == null)) {
			return super.hashCode();
		}
		this.hashCode = this.type.hashCode() + this.target.hashCode();
		return this.hashCode;
	}
	
	public void setSource(Vertex source) {
		this.source = source;
	}
	
	public void setTarget(Vertex target) {
		this.target = target;
	}
	
	public void setType(Vertex type) {
		this.type = type;
	}
	
	public void setMeta(Vertex meta) {
		this.meta = meta;
	}

	/**
	 * Return if the vertex is pinned to memory, and will not be forgotten.
	 */
	public boolean isPinned() {
		return pinned;
	}

	/**
	 * Set if the vertex should be pinned to memory.
	 * Pinned vertices will not be forgotten.
	 */
	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	/**
	 * Return the date the vertex was created.
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Set the date the vertex was created.
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * Return the date the vertex was last accessed.
	 */
	public Date getAccessDate() {
		return accessDate;
	}

	/**
	 * Set the date the vertex was last accessed.
	 * Access is considered moving from long term to short term memory.
	 */
	public void setAccessDate(Date accessDate) {
		this.accessDate = accessDate;
	}

	/**
	 * Return the number of times the vertex has been accessed.
	 * Access is considered moving from long term to short term memory.
	 */
	public int getAccessCount() {
		return accessCount;
	}

	/**
	 * Set the number of times the vertex has been accessed.
	 */
	public void setAccessCount(int accessCount) {
		this.accessCount = accessCount;
	}

	/**
	 * Record that the vertex was accessed, update the access time and increment the access count.
	 */
	public void incrementAccessCount() {
		if ((this.accessDate != null) && ((System.currentTimeMillis() - this.accessDate.getTime()) < (24 * Utils.HOUR)))  {
			// Avoid incrementing if already incremented this day.
			return;
		}
		setAccessDate(new Date());
		setAccessCount(this.accessCount + 1);
	}

	/**
	 * Increase the vertices's level of consciousness.
	 */
	public void incrementConsciousnessLevel() {
		incrementConsciousnessLevel(1);
	}

	/**
	 * Decrease the vertices's level of consciousness.
	 */
	public void decrementConsciousnessLevel() {
		decrementConsciousnessLevel(1);
	}

	/**
	 * Decrease the vertices's level of consciousness by the amount.
	 */
	public void decrementConsciousnessLevel(int amount) {
		this.consciousnessLevel = this.consciousnessLevel - amount;		
	}
	
	/**
	 * Increase the vertices's level of consciousness by the amount.
	 */
	public void incrementConsciousnessLevel(int amount) {
		this.consciousnessLevel = this.consciousnessLevel + amount;
		if (this.consciousnessLevel > 5) {
			incrementAccessCount();
		}
	}

	/**
	 * Return the vertices's level of consciousness.
	 */
	public int getConsciousnessLevel() {
		return this.consciousnessLevel;
	}

	/**
	 * Set the vertices's level of consciousness.
	 */
	public void setConsciousnessLevel(int consciousnessLevel) {
		this.consciousnessLevel = consciousnessLevel;
	}
	
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		BasicVertex.writeHeader(getType(), writer);
		writer.print(" [");
		writer.print(getIndex());
		writer.print("] ");
		writer.print(" (");
		writer.print(((int)(getCorrectness() * 100)) / 100f);
		writer.print(")-> ");
		BasicVertex.writeHeader(getTarget(), writer);
		writer.flush();
		stringWriter.flush();

		return stringWriter.toString();
	}
}