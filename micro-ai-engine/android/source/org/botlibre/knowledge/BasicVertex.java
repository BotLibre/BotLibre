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
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.MemoryStorageException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Path;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.knowledge.VertexIterator;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.self.SelfExecutionException;
import org.botlibre.sense.service.RemoteService;
import org.botlibre.thought.forgetfulness.Forgetfulness;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Represents a piece of knowledge.
 * Vertex is a very simple structure mainly comprised of its relationships,
 * this gives the system a very simple and extendable structure.
 * Basic implementation to allow subclasses to avoid defining some of the basic stuff.
 * The data attribute allows for vertices that represents something tangible such as
 * text, sound, image, number, etc.  The data is also indexed within a network, so is the
 * only guaranteed way to lookup an existing vertex.
 */

public class BasicVertex implements Vertex, Serializable {
	public static String SELF = "SELF";
	public static int SMALL = 10;
	public static int MEDIUM = 50;
	public static int LARGE = 100;

	private static final long serialVersionUID = 1L;
	
	protected Long id;
	protected String name;
	/** Allow for database lazy initialization. */
	protected Collection<Relationship> allRelationships;
	protected transient Map<Vertex, Map<Relationship, Relationship>> relationships;
	protected String dataType;
	protected Object data;
	protected Date creationDate;
	protected Date accessDate;
	protected int accessCount;
	protected boolean pinned;
	protected transient int consciousnessLevel;
	protected Network network;
	protected Vertex original;
	protected Boolean hasResponse;
	protected int wordCount;
	protected boolean isDirty;
	protected long groupId;
	protected boolean isTemporary;
	
	public BasicVertex() {
		this.accessCount = 0;
	}
		
	public BasicVertex(Object data) {
		this();
		this.data = data;
	}
	
	public void init() {
		this.hasResponse = false;
		this.creationDate = new Date();
		incrementAccessCount();
	}

	public boolean isTemporary() {
		return isTemporary;
	}

	public void setIsTemporary(boolean isTemporary) {
		this.isTemporary = isTemporary;
	}

	public int getWordCount() {
		return wordCount;
	}

	public void setWordCount(int wordCount) {
		this.wordCount = wordCount;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	/**
	 * PERF: Used to check response without accessing relationships.
	 */
	public boolean hasAnyResponseRelationship() {
		if (this.hasResponse == null) {
			this.hasResponse = hasRelationship(Primitive.RESPONSE);
		}
		return this.hasResponse;
	}

	public Boolean getHasResponse() {
		return hasResponse;
	}

	public void setHasResponse(Boolean hasResponse) {
		this.hasResponse = hasResponse;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setIsDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	/**
	 * Create the vertex as a clone of the original.
	 */
	public BasicVertex(Vertex original) {
		this.id = original.getId();
		this.name = original.getName();
		this.data = original.getData();
		this.creationDate = original.getCreationDate();
		this.accessCount = original.getAccessCount();
		this.accessDate = original.getAccessDate();
		this.consciousnessLevel = original.getConsciousnessLevel();
		this.original = original;
	}

	/**
	 * Return the original long term vertex the short term vertex was derived from.
	 */
	protected Vertex getOriginal() {
		return original;
	}	
	
	/**
	 * Set the original long term vertex the short term vertex was derived from.
	 */
	public void setOriginal(Vertex original) {
		this.original = original;
		this.relationships = null;
	}
	
	/**
	 * Return the network the vertex is derived from.
	 */
	public Network getNetwork() {
		return network;
	}	
	
	/**
	 * Set the network the vertex is derived from.
	 */
	public void setNetwork(Network network) {
		this.network = network;
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
		if (isPrimitive()) {
			return;
		}
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
		int size = getRelationships().size();
		if (size > SMALL) {
			this.consciousnessLevel++;
		}
		if (size > MEDIUM) {
			this.consciousnessLevel++;
		}
		if (size > LARGE) {
			this.consciousnessLevel++;
		}
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
	
	/**
	 * Helper inner class used to making iterating a vertices's relationships easier.
	 */
	protected class RelationshipIterator implements Iterator<Relationship> {
		private Iterator<Map<Relationship, Relationship>> mapIterator;
		private Iterator<Relationship> collectionIterator;
		private boolean order;

		public RelationshipIterator(boolean order) {
			this.mapIterator = getRelationships().values().iterator();
			this.collectionIterator = null;
			this.order = order;
		}

		public Relationship next() {
			if ((this.collectionIterator == null) || (! this.collectionIterator.hasNext())) {
				if (order) {
					this.collectionIterator = new TreeSet<Relationship>(mapIterator.next().values()).iterator();
				} else {
					this.collectionIterator = mapIterator.next().values().iterator();
				}
			}
			return this.collectionIterator.next();
		}

		public boolean hasNext() {
			if ((this.collectionIterator == null) || (! this.collectionIterator.hasNext())) {
				if (! this.mapIterator.hasNext()) {
					return false;
				}
				if (order) {
					this.collectionIterator = new TreeSet<Relationship>(mapIterator.next().values()).iterator();
				} else {
					this.collectionIterator = mapIterator.next().values().iterator();
				}
			}
			return this.collectionIterator.hasNext();
		}
		
		public void remove() {
			if (this.collectionIterator == null) {
				this.collectionIterator = mapIterator.next().values().iterator();
			}
			this.collectionIterator.remove();
		}		
	}

	/**
	 * Add the relation of the relationship primitive type to the target primitive.
	 */
	public synchronized Relationship addRelationship(Primitive type, Primitive target) {
		Vertex typeVertex = this.network.createVertex(type);
		Vertex targetVertex = this.network.createVertex(target);
		return addRelationship(typeVertex, targetVertex, -1, false);
	}

	/**
	 * Add the relation of the relationship primitive type to the target vertex.
	 */
	public synchronized Relationship addRelationship(Primitive type, Vertex target) {
		Vertex primitive = this.network.createVertex(type);
		return addRelationship(primitive, target, -1, false);
	}

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	public synchronized Relationship addRelationship(Vertex type, Vertex target) {
		return addRelationship(type, target, -1, false);
	}

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	public synchronized Relationship addRelationship(Primitive type, Vertex target, int index) {
		return addRelationship(this.network.createVertex(type), target, index, false);
	}

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	public synchronized Relationship addRelationship(Primitive type, Primitive target, int index) {
		return addRelationship(this.network.createVertex(type), this.network.createVertex(target), index, false);
	}

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * These are currently uni-directional.
	 * Only a single relation of a type can be defined to the same target,
	 * i.e. relationships to the same vertex are unique.
	 * unless the relationship is ordered, then they are only equal if the same index.
	 */
	public synchronized Relationship addRelationship(Vertex type, Vertex target, int index) {
		return addRelationship(type, target, index, false);
	}
	
	/**
	 * Add the relation of the relationship type to the target vertex.
	 * Only increment the correctness if not internal.
	 */
	public synchronized Relationship addRelationship(Vertex type, Vertex target, int index, boolean internal) {
		BasicRelationship relationship = new BasicRelationship(this, type, target);
		relationship.setIndex(index);
		return addRelationship(relationship, internal, false, 0.5f);
	}

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * The correctness decreases the correctness of the relation.
	 */
	public synchronized Relationship addWeakRelationship(Primitive type, Primitive target, float correctnessMultiplier) {
		return addWeakRelationship(this.network.createVertex(type), this.network.createVertex(target), correctnessMultiplier);
	}

	/**
	 * Add the relation of the relationship type to the target vertex.
	 * The correctness decreases the correctness of the relation.
	 */
	public synchronized Relationship addWeakRelationship(Primitive type, Vertex target, float correctnessMultiplier) {
		return addWeakRelationship(this.network.createVertex(type), target, correctnessMultiplier);
	}
	
	/**
	 * Add the relation of the relationship type to the target vertex.
	 * The correctness decreases the correctness of the relation.
	 */
	public synchronized Relationship addWeakRelationship(Vertex type, Vertex target, float correctnessMultiplier) {
		BasicRelationship relationship = new BasicRelationship(this, type, target);
		return addRelationship(relationship, false, false, correctnessMultiplier);
	}
	
	/**
	 * Add the relation ensuring uniqueness.
	 */
	public synchronized Relationship addRelationship(Relationship relationship, boolean internal) {
		return addRelationship(relationship, internal, false, 0.5f);		
	}
	
	/**
	 * Add the relation ensuring uniqueness.
	 */
	public synchronized Relationship addRelationship(Relationship relationship, boolean internal, boolean init, float correctnessMultiplier) {
		if (!internal && !init && !this.isTemporary && (this.network.isReadOnly()
					|| relationship.getType().getNetwork().isReadOnly()
					|| relationship.getTarget().getNetwork().isReadOnly())) {
			MemoryStorageException exception = new MemoryStorageException("Read-only vertices cannot be modified.");
			exception.printStackTrace();
			throw exception;
		}
		// Stored as map of maps.
		Map<Relationship, Relationship> relationships = getRelationships().get(relationship.getType());
		if (relationships == null) {
			relationships = new HashMap<Relationship, Relationship>();
			getRelationships().put(relationship.getType(), relationships);
		}
		// Check if already has the relationship.
		Relationship existing = relationships.get(relationship);
		if (existing == null) {
			if (! internal) {
				// MAX_VALUE means add to the end.
				if (!relationship.hasIndex() || (relationship.getIndex() == Integer.MAX_VALUE)) {
					relationship.setIndex(relationships.size());
				}
				relationship.setCorrectness(correctnessMultiplier);
			}
			relationships.put(relationship, relationship);
			// Also add to allRelationships for JPA change tracking.
			if (!init && (this.allRelationships != null)) {
				this.allRelationships.add(relationship);
			}
			if (!internal) {
				relationship.setCreationDate(new Date());
			}
			existing = relationship;
		} else if (! internal) {
			float correctness = existing.getCorrectness();
			if (correctness != 2.0f) { // 2.0 is used to define prefer relationship
				// Either switch to positive as the inverse value,
				// or increment its correctness by 1/2.
				if (correctness < 0) {
					correctness = (-1.0f - correctness) * -1.0f;
					if (correctness <= -0.99) {
						correctness = -1;
					}
				} else {
					correctness = correctness + ((1.0f - correctness) * correctnessMultiplier);
					if (correctness >= 0.99) {
						correctness = 1;
					}
				}
				existing.setCorrectness(correctness);
			}
		}
		if (!internal) {
			existing.incrementAccessCount();
			if (relationship.getType().is(Primitive.RESPONSE)) {
				this.hasResponse = Boolean.TRUE;
			}
			if (relationships.size() > Forgetfulness.MAX_RELATIONSHIPS) {
				setIsDirty(true);  // direct access doesn't detect change...
			}
			if ((this.allRelationships != null) && (this.allRelationships.size() > Forgetfulness.MAX_RELATIONSHIPS)) {
				if (this.allRelationships.size() != totalRelationships()) {
					// Corruption detected.
					System.out.println("Relationship corruption detected: " + this);
					setIsDirty(true);  // direct access doesn't detect change...
				}
			}
			
		}
		return existing;
	}

	/**
	 * Two vertices are equal if they have the same data.
	 */
	public boolean equals(Object object) {
		if (this == object) {
		  return true;
		}
		if (!(object instanceof Vertex)) {
			return false;
		}
		Vertex otherVertex = (Vertex) object;
		if ((this.id != null) && this.id.equals(otherVertex.getId())) {
		  return true;
		}
		if ((this.data == null) || (otherVertex.getData() == null)) {
		  return false;
		}
		return (this.data.equals(otherVertex.getData()));
	}

	/**
	 * Compare the vertices ignoring case.
	 */
	public boolean equalsIgnoreCase(Vertex vertex) {
		if (this.equals(vertex)) {
		  return true;
		}
		if ((this.data instanceof String) && (vertex.getData() instanceof String)) {
		  return ((String)this.data).equalsIgnoreCase((String)vertex.getData());
		}
		return false;
	}
	
	public Long getId() {
		return id;
	}

	/**
	 * Set the vertex id.
	 * The id can only be set when loading or creating a vertex.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Hash first on data then id for equality compatibility.
	 */
	public int hashCode() {
		if (this.data != null) {
			return this.data.hashCode();
		} else if (this.id != null) {
			return this.id.intValue();
		}
		return super.hashCode();
	}
  		
	/**
	 * Return the data value that the vertex represents.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Set the data value that the vertex represents.
	 */
	public void setData(Object data) {
		this.data = data;
		if (data != null) {
			this.dataType = convertDataType(data);
		}
	}
  
	/**
	 * Return if the vertex has a data value that it represents.
	 */
	public boolean hasData() {
		return data != null;
	}
	
	/**
	 * Return if the relationships have been instantiated.
	 */
	public synchronized boolean hasRelationships() {
		return this.relationships != null;
	}
	
	/**
	 * Return all relationships.
	 * Relationships are stored as a map keyed on the relationship type,
	 * with ordered list of relationship targets.
	 */
	public Map<Vertex, Map<Relationship, Relationship>> getRelationships() {
		if (this.relationships == null) {
			// This is only done for SerializedMemory, not DatabaseMemory
			this.relationships = new HashMap<Vertex, Map<Relationship, Relationship>>();
			// Lazy init from parent.
			if (this.original != null) {
				for (Iterator<Relationship> iterator = this.original.allRelationships(); iterator.hasNext(); ) {
					Relationship originalRelationship = iterator.next();
					Vertex type = this.network.findById(originalRelationship.getType().getId());
					Vertex target = this.network.findById(originalRelationship.getTarget().getId());
					Relationship relationship = addRelationship(type, target, originalRelationship.getIndex(), true);
					if (originalRelationship.hasMeta()) {
						Vertex meta = this.network.findById(originalRelationship.getMeta().getId());
						relationship.setMeta(meta);
					}
					relationship.setCorrectness(originalRelationship.getCorrectness());
				}
			} else if (this.allRelationships != null) {
				// Lazy init from database.
				for (Relationship relationship : this.allRelationships) {
					addRelationship(relationship, true, true, 0);
				}
			}
			
		}
		return this.relationships;
	}

	/**
	 * Apply the quotient.
	 * Apply the equation to the variable matches.
	 * This is basically a meta-language (4GL) based on Equation objects (vertices).
	 */
	public synchronized Vertex applyQuotient(Map<Vertex, Vertex> variables, Network network) {
		// TODO error handle invalid operations
		Vertex result = null;
		boolean isDebug = network.getBot().isDebugFiner();
		if (isVariable()) {
			result = variables.get(this);
			if (result == null) {
				result = network.createVertex(Primitive.NULL);
			}
		} else if (instanceOf(Primitive.EQUATION)) {
			try {
				// Check for byte-code.
				if (getData() instanceof BinaryData) {
					Vertex equation = new SelfDecompiler().parseEquationByteCode(this, (BinaryData)getData(), this.network);
					return equation.applyQuotient(variables, network);
				}
				Vertex operator = getRelationship(Primitive.OPERATOR);
				if (operator == null) {
					return network.createVertex(Primitive.NULL);
				}
				List<Relationship> arguments = orderedRelationships(Primitive.ARGUMENT);
				if (isDebug) {
					Vertex source = getRelationship(Primitive.SOURCE);
					String sourceCode = "";
					if (source != null) {
						sourceCode = String.valueOf(source.getData()).trim();
					} else if (operator.isPrimitive()) {
						sourceCode = ((Primitive)operator.getData()).getIdentity().toUpperCase() + "(" + orderedRelations(Primitive.ARGUMENT) + ")";
					}
					Vertex number = getRelationship(Primitive.LINE_NUMBER);
					if (number != null) {
						sourceCode = String.valueOf(number.getData()) + ":" + sourceCode;
					}
					network.getBot().log(SELF, sourceCode, Level.FINER);
				}
				// NOT :0
				// Check if negated.
				if (operator.is(Primitive.NOT)) {
					Vertex equation = arguments.get(0).getTarget();
					result = equation.applyQuotient(variables, network);
					if (result.is(Primitive.TRUE)) {
						result = network.createVertex(Primitive.FALSE);
					} else if (result.is(Primitive.FALSE)) {
						result = network.createVertex(Primitive.TRUE);
					} else if (result.is(Primitive.UNKNOWN)) {
						result = network.createVertex(Primitive.UNKNOWN);
					}
				} else if (operator.is(Primitive.RELATION)) {
					result = applyRELATION(arguments, variables, network);
				} else if (operator.is(Primitive.RELATED)) {
					result = applyRELATED(arguments, variables, network);
				} else if (operator.is(Primitive.ASSOCIATE) || operator.is(Primitive.DISSOCIATE) || operator.is(Primitive.WEAKASSOCIATE)) {
					result = applyASSOCIATE(operator, arguments, variables, network);
				} else if (operator.is(Primitive.FOR)) {
					result = applyFOR(arguments, variables, network);					
				} else if (operator.is(Primitive.WHILE)) {
					result = applyWHILE(arguments, variables, network);					
				} else if (operator.is(Primitive.ASSIGN)) {
					// ASSIGN :0 TO :1
					// Assign a variable a new value.
					Vertex variable = arguments.get(0).getTarget();
					Vertex value = arguments.get(1).getTarget().applyQuotient(variables, network);
					if (value != null) {
						variables.put(variable, value);
					}
					if (isDebug) {
						network.getBot().log(SELF, "ASSIGN " + variable + " TO " + value, Level.FINER);
					}
					result = value;
				} else if (operator.is(Primitive.DEFINE)) {
					// DEFINE :0 AS (:1, :2)
					// Define the word(s) for something.
					Vertex object = arguments.get(0).getTarget().applyQuotient(variables, network);
					Iterator<Relationship> iterator = arguments.iterator();
					iterator.next();
					while (iterator.hasNext()) {
						Vertex word = iterator.next().getTarget().applyQuotient(variables, network);
						word.addRelationship(Primitive.MEANING, object);
						object.addRelationship(Primitive.WORD, word);
						network.associateCaseInsensitivity((String)word.getData(), object);
						if (isDebug) {
							network.getBot().log(SELF, "DEFINE " + object + " AS " + word, Level.FINER);
						}
					}
					result = object;
				} else if (operator.is(Primitive.RANDOM)) {
					result = applyRANDOM(arguments, variables, network);
				} else if (operator.is(Primitive.DEBUG)) {
					result = applyDEBUG(arguments, variables, network);
				} else if (operator.is(Primitive.IF)) {
					result = applyIF(arguments, variables, network);
				} else if (operator.is(Primitive.GREATER)) {
					result = applyGREATER(arguments, variables, network);
				} else if (operator.is(Primitive.LESS)) {
					result = applyLESS(arguments, variables, network);
				} else if (operator.is(Primitive.EQUAL)) {
					result = applyEQUAL(arguments, variables, network);
				} else if (operator.is(Primitive.OR) || operator.is(Primitive.AND)) {
					result = applyCONDITION(arguments, variables, network);
				} else if (operator.is(Primitive.DO)) {
					// DO (:0, :1, :2, ...)
					// Apply each equation in the arguments.
					Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
					if (arguments != null) {
						for (Relationship doEquation : arguments) {
							result = doEquation.getTarget().applyQuotient(variables, network);
							if (variables.containsKey(returnPrimitive)) {
								// Clear return for named functions.
								if (hasName()) {
									variables.remove(returnPrimitive);
								}
								return result;
							}
						}
					}
				} else if (operator.is(Primitive.THINK)) {
					// THINK (:0, :1, :2, ...)
					// Apply each equation in the arguments and return.
					Vertex returnPrimitive = network.createVertex(Primitive.RETURN);
					for (Relationship doEquation : arguments) {
						result = doEquation.getTarget().applyQuotient(variables, network);
						if (variables.containsKey(returnPrimitive)) {
							// Clear return for named functions.
							if (hasName()) {
								variables.remove(returnPrimitive);
							}
							return result;
						}
					}
					result = returnPrimitive;
				} else if (operator.is(Primitive.WORD)) {
					result = applyWORD(arguments, variables, network);
				} else if (operator.is(Primitive.SENTENCE)) {
					result = applySENTENCE(arguments, variables, network);
				} else if (operator.is(Primitive.UPPERCASE)) {
					result = applyFRAGMENT(arguments, variables, network, Primitive.UPPERCASE);
				} else if (operator.is(Primitive.LOWERCASE)) {
					result = applyFRAGMENT(arguments, variables, network, Primitive.LOWERCASE);
				} else if (operator.is(Primitive.FORMAT)) {
					Vertex as = getRelationship(Primitive.AS);
					if (as != null) {
						as = as.applyQuotient(variables, network);
					}
					if (as != null && as.isPrimitive()) {
						result = applyFRAGMENT(arguments, variables, network, (Primitive)as.getData());
					} else {
						result = applyFRAGMENT(arguments, variables, network, null);
					}
				} else if (operator.is(Primitive.PRIMITIVE)) {
					// PRIMITIVE (:0)
					// Create a primitive from the string.
					if (arguments.size() == 0) {
						result = network.createVertex(Primitive.NULL);
					}
					Vertex primitive = arguments.get(0).getTarget().applyQuotient(variables, network);
					result = network.createVertex(new Primitive(((String.valueOf(primitive.getData()).toLowerCase()))));
				} else if (operator.is(Primitive.INPUT)) {
					result = applyINPUT(arguments, variables, network);
				} else if (operator.is(Primitive.GET)) {
					result = applyGET(arguments, variables, network);
				} else if (operator.is(Primitive.SET)) {
					result = applySET(arguments, variables, network);
				} else if (operator.is(Primitive.ALL)) {
					result = applyALL(arguments, variables, network);
				} else if (operator.is(Primitive.COUNT)) {
					result = applyCOUNT(arguments, variables, network);
				} else if (operator.is(Primitive.APPEND)) {
					result = applyAPPEND(arguments, variables, network);
				} else if (operator.is(Primitive.NEW)) {
					result = applyNEW(arguments, variables, network);
				} else if (operator.is(Primitive.CALL)) {
					result = applyCALL(arguments, variables, network);
				} else if (operator.is(Primitive.LEARN)) {
					result = applyLEARN(arguments, variables, network);
				} else if (operator.is(Primitive.SRAI) || operator.is(Primitive.REDIRECT)) {
					result = applySRAI(arguments, variables, network);
				} else if (operator.is(Primitive.SRAIX) || operator.is(Primitive.REQUEST)) {
					result = applySRAIX(arguments, variables, network);
				} else if (operator.is(Primitive.RETURN)) {
					// RETURN :0
					if (arguments == null || arguments.isEmpty()) {
						result = network.createVertex(Primitive.NULL);
					} else {
						result = arguments.get(0).getTarget().applyQuotient(variables, network);
					}
					variables.put(network.createVertex(Primitive.RETURN), result);
				}
				// Clear return for named functions.
				if (hasName()) {
					variables.remove(network.createVertex(Primitive.RETURN));
				}
			} catch (SelfExecutionException exception) {
				throw exception;
			} catch (Exception exception) {
				network.getBot().log(this, exception);
				throw new SelfExecutionException(this, exception);
			}
		} else {
			result = (Vertex)(Object)this;
		}
		if (result == null) {
			result = network.createVertex(Primitive.NULL);
		}
		if (result.getNetwork() != network) {
			result = network.createVertex(result);			
		}
		// Check for formula and transpose
		if (result.instanceOf(Primitive.FORMULA)) {
			Language language = network.getBot().mind().getThought(Language.class);
			Vertex newResult = language.evaluateFormula(result, variables, network);
			if (newResult == null) {
				language.log("Template formula cannot be evaluated", Level.FINE, result);
				result = network.createVertex(Primitive.NULL);
			} else {
				result = language.getWord(newResult, network);
			}
		}
		network.getBot().log(this, "result:", Level.FINER, result);
		return result;
	}

	/**
	 * Evaluates any eval functions in the equation or formula..
	 * This is used by learn.
	 */
	public Vertex applyEval(Map<Vertex, Vertex> variables, Network network) {
		Vertex result = null;
		try {
			if (isVariable()) {
				result = variables.get(this);
				if (result == null) {
					result = network.createVertex(Primitive.NULL);
				}
			} else if (instanceOf(Primitive.EQUATION)) {
				// Check for byte-code.
				if (getData() instanceof BinaryData) {
					Vertex equation = new SelfDecompiler().parseEquationByteCode(this, (BinaryData)getData(), this.network);
					return equation.applyEval(variables, network);
				}
				Vertex operator = getRelationship(Primitive.OPERATOR);
				List<Relationship> arguments = orderedRelationships(Primitive.ARGUMENT);
				if (operator.is(Primitive.EVAL)) {
					// EVAL :0
					return arguments.get(0).getTarget().applyQuotient(variables, network);
				}
			} else {
				result = (Vertex)(Object)this;
			}
			if (result == null) {
				result = network.createVertex(Primitive.NULL);
			}
			if (result.getNetwork() != network) {
				result = network.createVertex(result);
			}
			boolean formula = result.instanceOf(Primitive.FORMULA);
			boolean pattern = result.instanceOf(Primitive.PATTERN);
			// Check for formula and transpose
			if (formula || pattern) {
				List<Vertex> words = result.orderedRelations(Primitive.WORD);
				if (words == null) {
					return result;
				}
				List<Vertex> newWords = new ArrayList<Vertex>(words.size());
				boolean eval = false;
				boolean formulaRequired = false;
				for (Vertex word: words) {
					if (word.instanceOf(Primitive.EQUATION)) {
						// Check for byte-code.
						if (word.getData() instanceof BinaryData) {
							word = new SelfDecompiler().parseEquationByteCode(word, (BinaryData)word.getData(), this.network);
						}
						Vertex operator = word.getRelationship(Primitive.OPERATOR);
						if (operator != null && operator.is(Primitive.EVAL)) {
							eval = true;
							Vertex newWord = word.applyEval(variables, network);
							if (newWord.instanceOf(Primitive.EQUATION) || newWord.instanceOf(Primitive.FORMULA)) {
								formulaRequired = true;
							}
							newWords.add(newWord);
						} else {
							formulaRequired = true;
							newWords.add(word);
						}
					} else if (word.instanceOf(Primitive.VARIABLE)) {
						formulaRequired = true;
						newWords.add(word);
					} else {
						newWords.add(word);
					}
				}
				if (eval) {
					if (pattern) {
						result = network.createTemporyVertex();
						result.addRelationship(Primitive.INSTANTIATION, Primitive.PATTERN);
					} else if (formulaRequired) {
						result = network.createInstance(Primitive.FORMULA);
					} else {
						result = network.createTemporyVertex();
						result.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
					}
					int index = 0;
					for (Vertex word : newWords) {
						result.addRelationship(Primitive.WORD, word, index);
						index++;
					}
					if (!formulaRequired) {
						Language language = network.getBot().mind().getThought(Language.class);
						result = language.createSentenceText(result, network);
						if (pattern) {
							result = network.createSentence(Utils.reduce(result.printString()));
						}
					}
				}
			}
		} catch (SelfExecutionException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new SelfExecutionException(this, exception);
		}
		return result;
	}

	/**
	 * Apply the WORD operation.
	 * WORD (:0, :1, :2, ...)
	 * Create a compound word from the arguments.
	 */
	public Vertex applyWORD(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex nil = network.createVertex(Primitive.NULL);
		if (arguments.size() == 0) {
			return nil;
		}
		StringWriter writer = new StringWriter();
		List<Vertex> words = new ArrayList<Vertex>();
		for (Relationship relationship : arguments) {
			Vertex word = relationship.getTarget().applyQuotient(variables, network);
			if (word.instanceOf(Primitive.LIST)) {
				List<Vertex> elements = word.orderedRelations(Primitive.SEQUENCE);
				if (elements != null) {
					words.addAll(elements);
				}
			} else {
				words.add(word);
			}
		}
		Vertex previousWord = nil;
		for (int index = 0; index < words.size(); index++) {
			Vertex word = words.get(index);
			Vertex nextWord = nil;
			if (words.size() > (index + 1)) {
				nextWord = words.get(index + 1);
			}
			word = Language.getWordFollowing(word, previousWord, nextWord, network);
			writer.write(String.valueOf(word.getData()));
			if ((index + 1) < words.size()) {
				writer.write(" ");
			}
			previousWord = word;
		}
		return network.createWord(writer.toString());
	}

	/**
	 * Apply the SENTENCE operation.
	 * SENTENCE (:0, :1, :2, ...)
	 * Create a sentence from printing the arguments.
	 */
	public Vertex applySENTENCE(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex result = null;
		Vertex nil = network.createVertex(Primitive.NULL);
		if (arguments.size() == 0) {
			result = nil;
		} else if (arguments.size() == 1) {
			result = arguments.get(0).getTarget().applyQuotient(variables, network);
			if (!(result.getData() instanceof String)) {
				StringWriter writer = new StringWriter();
				Vertex text = Language.getWordFollowing(result, nil, nil, network);
				writer.write(text.getDataValue());
				result = network.createSentence(writer.toString());
			} else {
				result = network.createSentence((String)result.getData());				
			}
		} else {
			StringWriter writer = new StringWriter();
			List<Vertex> words = new ArrayList<Vertex>();
			for (Relationship relationship : arguments) {
				words.add(relationship.getTarget().applyQuotient(variables, network));
			}
			Vertex previousWord = nil;
			for (int index = 0; index < words.size(); index++) {
				Vertex word = words.get(index);
				Vertex nextWord = nil;
				if (words.size() > (index + 1)) {
					nextWord = words.get(index + 1);
				}
				word = Language.getWordFollowing(word, previousWord, nextWord, network);
				writer.write(String.valueOf(word.getData()));
				if ((index + 1) < words.size()) {
					writer.write(" ");
				}
				previousWord = word;
			}
			result = network.createSentence(writer.toString());
		}
		return result;
	}

	/**
	 * Apply the FRAGMENT operation.
	 * FRAGMENT (:0, :1, :2, ...)
	 * Create a fragment from printing the arguments.
	 */
	public Vertex applyFRAGMENT(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, Primitive format) {
		String text = null;
		Vertex nil = network.createVertex(Primitive.NULL);
		if (arguments.size() == 0) {
			return nil;
		} else if (arguments.size() == 1) {
			Vertex result = arguments.get(0).getTarget().applyQuotient(variables, network);
			if (!(result.getData() instanceof String)) {
				StringWriter writer = new StringWriter();
				Vertex word = Language.getWordFollowing(result, nil, nil, network);
				writer.write(word.getDataValue());
				text = writer.toString();
			} else {
				text = (String)result.getData();				
			}
		} else {
			StringWriter writer = new StringWriter();
			List<Vertex> words = new ArrayList<Vertex>();
			for (Relationship relationship : arguments) {
				words.add(relationship.getTarget().applyQuotient(variables, network));
			}
			Vertex previousWord = nil;
			for (int index = 0; index < words.size(); index++) {
				Vertex word = words.get(index);
				Vertex nextWord = nil;
				if (words.size() > (index + 1)) {
					nextWord = words.get(index + 1);
				}
				word = Language.getWordFollowing(word, previousWord, nextWord, network);
				writer.write(String.valueOf(word.getData()));
				if ((index + 1) < words.size()) {
					writer.write(" ");
				}
				previousWord = word;
			}
			text = writer.toString();
		}
		boolean caseSensitive = false;
		if (format != null) {
			if (format.equals(Primitive.UPPERCASE)) {
				text = text.toUpperCase();
				caseSensitive = true;
			} else if (format.equals(Primitive.LOWERCASE)) {
				text = text.toLowerCase();
				caseSensitive = true;
			} else if (format.equals(Primitive.FORMAL)) {
				text = org.botlibre.tool.Utils.formal(text);
				caseSensitive = true;
			} else if (format.equals(Primitive.PERSON)) {
				text = org.botlibre.tool.Utils.person(text);
			} else if (format.equals(Primitive.PERSON2)) {
				text = org.botlibre.tool.Utils.person2(text);
			} else if (format.equals(Primitive.GENDER)) {
				text = org.botlibre.tool.Utils.gender(text);
			} else if (format.equals(Primitive.SENTENCE)) {
				text = Utils.capitalize(text);
				caseSensitive = true;
			} else if (format.equals(Primitive.EXPLODE)) {
				text = org.botlibre.tool.Utils.explode(text);
			} else if (format.equals(Primitive.NORMALIZE)) {
				text = org.botlibre.tool.Utils.normalize(text);
			} else if (format.equals(Primitive.DENORMALIZE)) {
				text = org.botlibre.tool.Utils.denormalize(text);
			}
		}
		Vertex fragment = network.createFragment(text);
		if (caseSensitive) {
			fragment.addRelationship(Primitive.TYPE, Primitive.CASESENSITVE);
		}
		return fragment;
	}
	
	/**
	 * Apply the ALL operation.
	 * ALL :0 FROM :1
	 * Get all the relationship value of type ARGUMENT(0) from ARGUMENT(1) as a new list
	 */
	public Vertex applyALL(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex result = null;
		Vertex relationship = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex source = arguments.get(1).getTarget().applyQuotient(variables, network);
		if (arguments.size() > 2) {
			Vertex associate = arguments.get(2).getTarget().applyQuotient(variables, network);
			Vertex associateRelationship = arguments.get(3).getTarget().applyQuotient(variables, network);
			// TODO
			result = source.mostConsciousWithAssoiate(relationship, associate, associateRelationship);
		} else {
			List<Relationship> values = source.orderedRelationships(relationship);
			if (values == null) {
				// Check all meanings of all words.
				Collection<Relationship> words = relationship.getRelationships(Primitive.WORD);
				Set<Vertex> processed = new HashSet<Vertex>();
				processed.add(relationship);
				for (Relationship word : words) {
					Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
					if (otherMeanings != null) {
						for (Relationship meaning : otherMeanings) {
							if (!processed.contains(meaning.getTarget())) {
								processed.add(meaning.getTarget());
								values = source.orderedRelationships(meaning.getTarget());
								if (values != null) {
									break;
								}
							}
						}
					}
				}
			}
			if (values == null) {
				result = network.createVertex(Primitive.NULL);
			} else {
				result = network.createInstance(Primitive.LIST);
				int index = 0;
				for (Relationship value : values) {
					if (value.getCorrectness() > 0) {
						if (index > 12) {
							break;
						}
						result.addRelationship(Primitive.SEQUENCE, value.getTarget(), index);
						index++;
					}
				}
			}
		}
		if (result == null) {
			result = network.createVertex(Primitive.NULL);
		}
		return result;
	}

	/**
	 * Apply the ASSOCIATE or DISSOCIATE operation.
	 * ASSOCIATE :0 TO :1 BY :3
	 * ASSOCIATE :0 TO :1 BY :3 WITH META :4 AS :5
	 * DISSOCIATE :0 TO :1 BY :3
	 * Associate the left with the right by the association, or remove association if negated.
	 * i.e. "Bob loves Jill" (associate Bob to Jill by #loves)
	 */
	public Vertex applyASSOCIATE(Vertex operator, List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex source = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex target = arguments.get(1).getTarget().applyQuotient(variables, network);
		Vertex association = arguments.get(2).getTarget().applyQuotient(variables, network);
		Relationship relationship = null;
		if (operator.is(Primitive.DISSOCIATE)) {
			if (source.hasRelationship(association, target)) {
				relationship = source.removeRelationship(association, target);
			} else if (target.instanceOf(Primitive.WORD) && target.getData() instanceof String) {
				// Check words case.
				if (!Utils.isCapitalized((String)target.getData())) {
					Vertex capitalized = network.createWord(Utils.capitalize((String)target.getData()));
					if (source.hasRelationship(association, capitalized)) {
						relationship = source.removeRelationship(association, capitalized);
					}
				} else {
					Vertex lower = network.createWord(((String)target.getData()).toLowerCase());
					if (source.hasRelationship(association, lower)) {
						relationship = source.removeRelationship(association, lower);
					}
				}
			} else {
				// Check all meanings of all words.
				Collection<Relationship> words = target.getRelationships(Primitive.WORD);
				if (words != null) {
					Set<Vertex> processed = new HashSet<Vertex>();
					processed.add(target);
					for (Relationship word : words) {
						Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
						if (source.hasRelationship(word.getTarget())) {
							relationship = source.removeRelationship(association, word.getTarget());
							break;
						}
						if (otherMeanings != null) {
							for (Relationship meaning : otherMeanings) {
								if (!processed.contains(meaning.getTarget())) {
									processed.add(meaning.getTarget());
									if (source.hasRelationship(meaning.getTarget())) {
										relationship = source.removeRelationship(association, meaning.getTarget());
										break;
									}
								}
							}
						}									
					}
				}
			}
			if (relationship == null) {
				relationship = source.removeRelationship(association, target);
			}
			network.getBot().log(this, "Removing relation", Level.FINE, source, association, target);
		} else if (operator.is(Primitive.WEAKASSOCIATE)) {
			relationship = source.addWeakRelationship(association, target, 0.1f);
			network.getBot().log(this, "Adding weak relation", Level.FINE, source, association, target);
		} else {
			relationship = source.addRelationship(association, target);
			network.getBot().log(this, "Adding relation", Level.FINE, source, association, target);
		}
		if (arguments.size() == 5) {
			Vertex metaType = arguments.get(3).getTarget().applyQuotient(variables, network);
			Vertex metaTarget = arguments.get(4).getTarget().applyQuotient(variables, network);
			if (!metaTarget.is(Primitive.NULL)) {
				Vertex meta = network.createMeta(relationship);
				meta.addRelationship(metaType, metaTarget);
				network.getBot().log(this, "Adding relation meta", Level.FINER, metaType, metaTarget);
			}
		}
		return network.createVertex(Primitive.KNOWN);
	}

	/**
	 * Append the relationship in order.
	 * APPEND :0 TO :1 OF :2
	 * APPEND :0 TO :1 OF :2 WITH META :3 AS :4
	 * Add the relationship value ARGUMENT(2) of type ARGUMENT(1) to ARGUMENT(0)
	 */
	public Vertex applyAPPEND(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex source = arguments.get(2).getTarget().applyQuotient(variables, network);
		Vertex type = arguments.get(1).getTarget().applyQuotient(variables, network);
		Vertex target = arguments.get(0).getTarget().applyQuotient(variables, network);
		Relationship relationship = source.addRelationship(type, target, Integer.MAX_VALUE);
		if (arguments.size() == 5) {
			Vertex metaType = arguments.get(3).getTarget().applyQuotient(variables, network);
			Vertex metaTarget = arguments.get(4).getTarget().applyQuotient(variables, network);
			if (!metaTarget.is(Primitive.NULL)) {
				Vertex meta = network.createMeta(relationship);
				meta.addRelationship(metaType, metaTarget);
			}
		}
		return source;
	}

	/**
	 * Apply the OR/AND condition.
	 * IF (:0, :1) OR (:2, :3) THEN :0 ELSE :1
	 */
	public Vertex applyCONDITION(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex first = arguments.get(0).getTarget().applyQuotient(variables, network);
		boolean result = false;
		if (arguments.size() == 1) {
			result = first.is(Primitive.TRUE);
		} else {
			Vertex second = arguments.get(1).getTarget().applyQuotient(variables, network);
			result = first.matches(second, new HashMap<Vertex, Vertex>()) == Boolean.TRUE;
		}
		if (hasRelationship(Primitive.NOT, Primitive.NOT)) {
			result = !result;
		}
		Collection<Relationship> conditions = getRelationships(Primitive.CONDITION);
		if (conditions != null) {
			for (Relationship condition : conditions) {
				Vertex operator = condition.getTarget().getRelationship(Primitive.OPERATOR);
				if (operator == null) {
					continue;
				}
				if (!result && operator.is(Primitive.OR)) {
					Vertex value = condition.getTarget().applyQuotient(variables, network);
					if (value.is(Primitive.TRUE)) {
						result = true;
					}
				} else if (result && operator.is(Primitive.AND)) {
					Vertex value = condition.getTarget().applyQuotient(variables, network);
					if (value.is(Primitive.FALSE)) {
						result = false;
					}
				}
			}
		}
		if (result) {
			return network.createVertex(Primitive.TRUE);
		} else {
			return network.createVertex(Primitive.FALSE);			
		}
	}

	/**
	 * Apply the IF operation.
	 * IF (:0, :1) OR (:2, :3) THEN :0 ELSE :1
	 * If the first argument matches the second then apply the then, else apply the last then.
	 */
	public Vertex applyIF(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex first = arguments.get(0).getTarget().applyQuotient(variables, network);
		boolean result = false;
		if (arguments.size() == 1) {
			result = first.is(Primitive.TRUE);
		} else {
			Vertex second = arguments.get(1).getTarget().applyQuotient(variables, network);
			result = first.matches(second, new HashMap<Vertex, Vertex>()) == Boolean.TRUE;
		}
		if (hasRelationship(Primitive.NOT, Primitive.NOT)) {
			result = !result;
		}
		Collection<Relationship> conditions = getRelationships(Primitive.CONDITION);
		if (conditions != null) {
			for (Relationship condition : conditions) {
				Vertex operator = condition.getTarget().getRelationship(Primitive.OPERATOR);
				if (operator == null) {
					continue;
				}
				if (!result && operator.is(Primitive.OR)) {
					Vertex value = condition.getTarget().applyQuotient(variables, network);
					if (value.is(Primitive.TRUE)) {
						result = true;
					}
				} else if (result && operator.is(Primitive.AND)) {
					Vertex value = condition.getTarget().applyQuotient(variables, network);
					if (value.is(Primitive.FALSE)) {
						result = false;
					}
				}
			}
		}
		if (result) {
			Vertex then = getRelationship(Primitive.THEN);
			if (then != null) {
				return then.applyQuotient(variables, network);
			}
		} else {
			Vertex elseEquation = getRelationship(Primitive.ELSE);
			if (elseEquation != null) {
				return elseEquation.applyQuotient(variables, network);
			}
		}
		if (result) {
			return network.createVertex(Primitive.TRUE);
		} else {
			return network.createVertex(Primitive.FALSE);			
		}
	}

	/**
	 * Apply the SET operation.
	 * SET :0 TO :1 ON :3
	 * SET the left with the right by the association, replace any existing relationship.
	 * i.e. "Bob only loves Jill" (set #loves to Jill on Bob)
	 */
	public Vertex applySET(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex association = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex source = arguments.get(2).getTarget().applyQuotient(variables, network);
		Vertex target = arguments.get(1).getTarget().applyQuotient(variables, network);
		source.setRelationship(association, target);
		network.getBot().log(this, "Setting relation", Level.FINER, source, association, target);
		// Following some crazy AIML implied rules here...
		if (association.isPrimitive() && (association.is(Primitive.IT) || association.is(Primitive.HE) || association.is(Primitive.SHE))) {
			return association;
		}
		return target;
	}

	/**
	 * Apply the RANDOM operation.
	 * RANDOM (:0, :1, ...)
	 * Return a random argument.
	 */
	public Vertex applyRANDOM(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		if (arguments.isEmpty()) {
			return network.createVertex(Primitive.NULL);
		}
		return Utils.random(arguments).getTarget().applyQuotient(variables, network);
	}

	/**
	 * Apply the DEBUG operation.
	 * DEBUG ("debug", :0, :2)
	 * Log the arguments to the log.
	 */
	public Vertex applyDEBUG(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		if (network.getBot().isDebugFine()) {
			StringWriter writer = new StringWriter();
			boolean first = true;
			for (Relationship argument : arguments) {
				if (!first) {
					writer.write(" : ");
				}
				first = false;
				writer.write(argument.getTarget().applyQuotient(variables, network).printString());
			}
			network.getBot().log("DEBUG", writer.toString(), Level.FINE);
		}
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Apply the COUNT operation.
	 * COUNT :0 OF :1
	 * Return if the number of elements.
	 */
	public Vertex applyCOUNT(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex source = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex relationship;
		if (arguments.size() == 1) {
			relationship = network.createVertex(Primitive.SEQUENCE);
		} else {
			relationship = source;
			source = arguments.get(1).getTarget().applyQuotient(variables, network);			
		}
		Collection<Relationship> values = source.getRelationships(relationship);
		BigInteger count = null;
		if (values == null) {
			count = BigInteger.valueOf(0);
		} else {
			count = BigInteger.valueOf(values.size());			
		}
		return network.createVertex(count);
	}

	/**
	 * Apply the GREATER operation.
	 * GREATER (:0, :2)
	 * Return if the left is bigger than the right.
	 */
	public Vertex applyGREATER(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex left = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex right = arguments.get(1).getTarget().applyQuotient(variables, network);
		if (!(left.getData() instanceof Number) || !(right.getData() instanceof Number))  {
			return network.createVertex(Primitive.FALSE);
		}
		if (((Number) left.getData()).doubleValue() > ((Number) right.getData()).doubleValue()) {
			return network.createVertex(Primitive.TRUE);			
		} else {
			return network.createVertex(Primitive.FALSE);			
		}
	}

	/**
	 * Apply the LESS operation.
	 * LESS (:0, :2)
	 * Return if the left is smaller than the right.
	 */
	public Vertex applyLESS(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex left = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex right = arguments.get(1).getTarget().applyQuotient(variables, network);
		if (!(left.getData() instanceof Number) || !(right.getData() instanceof Number))  {
			return network.createVertex(Primitive.FALSE);
		}
		if (((Number) left.getData()).doubleValue() < ((Number) right.getData()).doubleValue()) {
			return network.createVertex(Primitive.TRUE);			
		} else {
			return network.createVertex(Primitive.FALSE);			
		}
	}

	/**
	 * Apply the LESS operation.
	 * RQUAL (:0, :2)
	 * Return if the left is equal to the right.
	 */
	public Vertex applyEQUAL(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex left = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex right = arguments.get(1).getTarget().applyQuotient(variables, network);
		if (left.equals(right)) {
			return network.createVertex(Primitive.TRUE);			
		} else {
			return network.createVertex(Primitive.FALSE);			
		}
	}

	/**
	 * Apply the FOR operation.
	 * FOR EACH #word OF :sentence AS :word [AND EACH :3 OF :4 AS :5] DO  (:0, :1, ...)
	 * Evaluate the DO for each relationship.
	 */
	public Vertex applyFOR(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		// Process a for loop, repeat the operation for each element in the sequence.
		List<List<Relationship>> sequences = new ArrayList<List<Relationship>>();
		List<Vertex> forVariables = new ArrayList<Vertex>();
		// Process the tri-pairs of source.relationship=variable.
		int maxSequenceSize = 0;
		for (int index = 0; index < arguments.size(); index = index + 3) {
			Vertex source = arguments.get(index + 1).getTarget().applyQuotient(variables, network);
			Vertex relationship = arguments.get(index).getTarget().applyQuotient(variables, network);
			List<Relationship> sequence = source.orderedRelationships(relationship);
			if (sequence == null) {
				sequence = new ArrayList<Relationship>(0);
			}
			// Keep track of the biggest sequence to null pads others.
			if (sequence.size() > maxSequenceSize) {
				maxSequenceSize = sequence.size();
			}
			sequences.add(sequence);
			Vertex variable = null;
			if (arguments.size() > 2) {
				variable = arguments.get(index + 2).getTarget();
			}
			forVariables.add(variable);
		}
		List<Relationship> doEquations = orderedRelationships(Primitive.DO);
		Vertex result;
		for (int index = 0; index < maxSequenceSize; index++)  {
			for (int variableIndex = 0; variableIndex < forVariables.size(); variableIndex++) {
				Vertex variable = forVariables.get(variableIndex);							
				if (variable != null) {
					List<Relationship> sequence = sequences.get(variableIndex);
					Vertex value = null;
					if (index >= sequence.size()) {
						value = network.createVertex(Primitive.NULL);
					} else {
						value = sequence.get(index).getTarget();
					}
					variables.put(variable, value);
				}
			}
			for (Relationship doEquation: doEquations) {
				result = doEquation.getTarget().applyQuotient(variables, network);
				if (variables.containsKey(network.createVertex(Primitive.RETURN))) {
					return result;
				}
			}
		}
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Apply the WHILE operation.
	 * WHILE (:0, :1) DO  (:0, :1, ...)
	 * Evaluate the DO while true or matching.
	 */
	public Vertex applyWHILE(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		// Process a while loop, repeat the operation until true or max depth
		int depth = 0;
		boolean condition = true;
		List<Relationship> doEquations = orderedRelationships(Primitive.DO);
		Vertex result = network.createVertex(Primitive.NULL);
		while (condition && depth < Language.MAX_STACK)  {
			Vertex first = arguments.get(0).getTarget().applyQuotient(variables, network);
			if (arguments.size() == 1) {
				condition = first.is(Primitive.TRUE);
			} else {
				Vertex second = arguments.get(1).getTarget().applyQuotient(variables, network);
				condition = first.matches(second, new HashMap<Vertex, Vertex>()) == Boolean.TRUE;
			}
			if (condition) {
				for (Relationship doEquation: doEquations) {
					result = doEquation.getTarget().applyQuotient(variables, network);
					if (variables.containsKey(network.createVertex(Primitive.RETURN))) {
						return result;
					}
				}
			}
			depth++;
		}
		if (depth >= Language.MAX_STACK) {
			network.getBot().log(SELF, "Max stack exceeded on while loop", Level.WARNING, Language.MAX_STACK);
		}
		return result;
	}
	
	/**
	 * Apply the INPUT operation.
	 * INPUT :0 PART :1 FOR :2
	 * Get the last input from the conversation for the speaker.
	 */
	public Vertex applyINPUT(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex index = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex speaker = arguments.get(1).getTarget().applyQuotient(variables, network);
		Vertex part = null;
		int partValue = 1;
		if (arguments.size() == 3) {
			part = speaker;
			try {
				partValue = Integer.valueOf(String.valueOf(part.getData()));
			} catch (Exception exception) {
				// Ignore, use 1;
			}
			speaker = arguments.get(2).getTarget().applyQuotient(variables, network);
		}
		Vertex input = network.createVertex(Primitive.INPUT_VARIABLE);
		Vertex conversation = variables.get(input.getRelationship(Primitive.CONVERSATION));
		if (conversation == null) {
			return network.createVertex(Primitive.NULL);
		}
		int count = 0;
		int value = 1;
		try {
			value = Integer.valueOf(String.valueOf(index.getData()));
		} catch (Exception exception) {
			// Ignore, use 1;
		}
		List<Vertex> inputs = conversation.orderedRelations(Primitive.INPUT);
		int element = inputs.size() - 1;
		while (count < value && element >= 0) {
			input = inputs.get(element);
			if (input.hasRelationship(Primitive.SPEAKER, speaker)) {
				count++;
				if (count == value) {
					Vertex sentence = input.getRelationship(Primitive.INPUT);
					if (part == null) {
						return sentence;
					}
					if (!sentence.instanceOf(Primitive.PARAGRAPH)) {
						if (partValue == 1) {
							return sentence;
						}
						return network.createVertex(Primitive.NULL);
					}
					List<Vertex> sentences = sentence.orderedRelations(Primitive.SENTENCE);
					if (partValue > sentences.size()) {
						return network.createVertex(Primitive.NULL);
					}
					return sentences.get(partValue - 1);
				}
			}
			element--;
		}
		return network.createVertex(Primitive.NULL);
	}

	/**
	 * Apply the GET operation.
	 * GET :0 FROM :1
	 * Get the relationship value of type ARGUMENT(0) from ARGUMENT(1)
	 */
	public Vertex applyGET(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex result = null;
		Vertex relationship = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex source = arguments.get(1).getTarget().applyQuotient(variables, network);
		Vertex index = getRelationship(Primitive.INDEX);
		Vertex lastindex = getRelationship(Primitive.LASTINDEX);
		if ((index != null) && (index.getData() instanceof Number)) {
			int position = ((Number)index.getData()).intValue();
			List<Vertex> values = source.orderedRelations(relationship);
			if (values != null && position > 0 && position <= values.size()) {
				result = values.get(position - 1);
			}
		} else if ((lastindex != null) && (lastindex.getData() instanceof Number)) {
			int position = ((Number)lastindex.getData()).intValue();
			List<Vertex> values = source.orderedRelations(relationship);
			if (values != null && position > 0 && position <= values.size()) {
				result = values.get(values.size() - position);
			}
		} else {
			if (arguments.size() > 2) {
				Vertex associate = arguments.get(2).getTarget().applyQuotient(variables, network);
				Vertex associateRelationship = arguments.get(3).getTarget().applyQuotient(variables, network);
				result = source.mostConsciousWithAssoiate(relationship, associate, associateRelationship);
			} else {
				result = source.mostConscious(relationship);
			}
		}
		if (result == null) {
			// Check all meanings of all words.
			Collection<Relationship> words = relationship.getRelationships(Primitive.WORD);
			if (words != null) {
				Set<Vertex> processed = new HashSet<Vertex>();
				processed.add(relationship);
				for (Relationship word : words) {
					Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
					if (otherMeanings != null) {
						for (Relationship meaning : otherMeanings) {
							if (!processed.contains(meaning.getTarget())) {
								processed.add(meaning.getTarget());
								result = source.mostConscious(meaning.getTarget());
								if (result != null) {
									break;
								}
							}
						}
					}									
				}
			}
			if (result == null) {
				result = network.createVertex(Primitive.NULL);
			}
		}
		return result;
	}

	/**
	 * Apply the NEW operation.
	 * NEW (:0, :1, ...)
	 * i.e. new Number, Sequence
	 * Create a new vertex as an instance of the argument types.
	 */
	public Vertex applyNEW(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex newVertex = null;
		newVertex = network.createVertex();
		for (Relationship argument : arguments) {
			Vertex type = argument.getTarget().applyQuotient(variables, network);
			newVertex.addRelationship(Primitive.INSTANTIATION, type);
			// Assign the name of the type to the default name of the instance.
			/*Collection<Relationship> names = type.getRelationships(Primitive.WORD);
			if (names != null) {
				for (Relationship name : names) {
					newVertex.addRelationship(Primitive.WORD, name.getTarget());
				}
			}*/						
			// Check if the type is a classification, if not, make its instantiations, specializations.
			if (!type.hasRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION)) {
				Collection<Relationship> specializations = type.getRelationships(Primitive.INSTANTIATION);
				if (specializations != null) {
					for (Relationship specialization : specializations) {
						type.addRelationship(Primitive.SPECIALIZATION, specialization.getTarget());
					}
					type.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
				}
			}
		}
		return newVertex;
	}

	/**
	 * Apply the call operation.
	 * CALL :0 ON :1 [WITH (:2, :3, ...)]
	 * i.e. CALL #push ON #Context WITH :thing1
	 * Call out to the named sense, arguments and return value must be vertices.
	 */
	@SuppressWarnings("rawtypes")
	public Vertex applyCALL(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) throws Exception {
		String methodName = ((Primitive)arguments.get(0).getTarget().applyQuotient(variables, network).getData()).getIdentity();
		String senseName = ((Primitive)arguments.get(1).getTarget().applyQuotient(variables, network).getData()).getIdentity();
		Object source = network.getBot().awareness().getSense(senseName);
		if (source == null) {
			source = network.getBot().mind().getThought(senseName);
		}
		if (source == null) {
			source = network.getBot().awareness().getTool(senseName);
		}
		if (source == null) {
			throw new SelfExecutionException(this, "Missing calling sense, thought, or tool.");
		}
		int size = arguments.size() - 1;
		Object[] methodArguments = new Object[size];
		Class[] argumentTypes = new Class[size];
		argumentTypes[0] = Vertex.class;
		methodArguments[0] = this;
		if (getNetwork() != network) {
			methodArguments[0] = network.createVertex(this);			
		}
		for (int index = 2; index < arguments.size(); index++) {
			Vertex argument = arguments.get(index).getTarget().applyQuotient(variables, network);
			methodArguments[index - 1] = argument;
			argumentTypes[index - 1] = Vertex.class;
		}
		Method method = source.getClass().getMethod(methodName, argumentTypes);
		Vertex result = (Vertex)method.invoke(source, methodArguments);
		if (result == null) {
			result = network.createVertex(Primitive.NULL);
		} else {
			result = network.createVertex(result);						
		}
		return result;
	}

	/**
	 * Apply the LEARN operation.
	 * LEARN :0 THAT :that TOPIC :topic TEMPLATE :1
	 * Evaluate and add the new response.
	 */
	public Vertex applyLEARN(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) throws Exception {
		Vertex pattern = arguments.get(0).getTarget().applyEval(variables, network);
		Vertex template = arguments.get(1).getTarget().applyEval(variables, network);
		Relationship relationship = pattern.addRelationship(Primitive.RESPONSE, template);
		template.addRelationship(Primitive.QUESTION, pattern);
		Vertex that = getRelationship(Primitive.THAT);
		if (that != null) {
			that = that.applyEval(variables, network);
			Vertex meta = network.createMeta(relationship);
			meta.addRelationship(Primitive.PREVIOUS, that);
			meta.addRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);
		}
		Vertex topic = getRelationship(Primitive.TOPIC);
		if (topic != null) {
			topic = topic.applyEval(variables, network);
			Vertex meta = network.createMeta(relationship);
			meta.addRelationship(Primitive.TOPIC, topic);
			meta.addRelationship(Primitive.REQUIRE, Primitive.TOPIC);
		}
		network.getBot().log(this, "New response learned", Level.FINER, pattern, template, that, topic);
		if (!pattern.instanceOf(Primitive.PATTERN)) {
			pattern.associateAll(Primitive.WORD, pattern, Primitive.QUESTION);
		} else {
			// Check for state and extend.
			Vertex state = variables.get(network.createVertex(Primitive.STATE));
			if (state != null) {
				// Get first case that gets sentence from input.
				List<Vertex> instructions = state.orderedRelations(Primitive.DO);
				Vertex sentenceState = null;
				if (instructions != null) {
					for (Vertex instruction : instructions) {
						if (instruction.instanceOf(Primitive.CASE)) {
							Vertex variable = instruction.getRelationship(Primitive.CASE);
							if ((variable != null) && variable.isVariable() && variable.hasRelationship(Primitive.INPUT)) {
								sentenceState = instruction.getRelationship(Primitive.GOTO);
								break;
							}
						}
					}				
				}
				if (sentenceState != null) {
					if (sentenceState.getNetwork() != network) {
						sentenceState = network.createVertex(sentenceState);
					}
					Vertex child = AIMLParser.parser().createState(pattern, sentenceState, network);
					Vertex equation = network.createInstance(Primitive.CASE);
					equation.addRelationship(Primitive.PATTERN, pattern);
					if (that != null) {
						equation.addRelationship(Primitive.THAT, that);
					}
					if (topic != null) {
						equation.addRelationship(Primitive.TOPIC, topic);
					}
					equation.addRelationship(Primitive.TEMPLATE, template);
					child.addRelationship(Primitive.DO, equation);
				}
			}
		}
		return pattern;
	}

	/**
	 * Apply the SRAI operation.
	 * SRAI "Hello"
	 * Return the response to processing the input.
	 */
	public Vertex applySRAI(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) throws Exception {
		Vertex sentence = arguments.get(0).getTarget().applyQuotient(variables, network);
		if (!sentence.instanceOf(Primitive.SENTENCE) && sentence.instanceOf(Primitive.FRAGMENT)) {
			sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
		}
		Vertex input = variables.get(network.createVertex(Primitive.INPUT_VARIABLE));
		input = input.copy();
		input.setRelationship(Primitive.INPUT, sentence);
		Vertex response = network.getBot().mind().getThought(Language.class).input(input, sentence, variables, network);
		if (response == null) {
			return network.createVertex(Primitive.NULL);
		}
		return response;
	}

	/**
	 * Apply the SRAIX operation.
	 * SRAIX "what is love" BOT "Brain Bot" LIMIT 5 SERVICE #botlibre APIKEY 12345 BOTID 12345 HINT "google" DEFAULT "Brain Bot is offline"
	 * Execute the remote service call, and return the response.
	 */
	public Vertex applySRAIX(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) throws Exception {
		Vertex sentence = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex apikey = getRelationship(Primitive.APIKEY);
		String apikeyValue = null;
		if (apikey != null) {
			apikey = apikey.applyQuotient(variables, network);
			apikeyValue = apikey.printString();
		}
		Vertex limit = getRelationship(Primitive.LIMIT);
		int limitValue = -1;
		if (limit != null) {
			limit = limit.applyQuotient(variables, network);
			limitValue = Integer.parseInt(limit.getDataValue());
		}
		Vertex bot = getRelationship(Primitive.BOT);
		String botValue = null;
		if (bot != null) {
			bot = bot.applyQuotient(variables, network);
			botValue = bot.printString();
		}
		Vertex botid = getRelationship(Primitive.BOTID);
		String botidValue = null;
		if (botid != null) {
			botid = botid.applyQuotient(variables, network);
			botidValue = botid.printString();
		}
		Vertex server = getRelationship(Primitive.SERVER);
		String serverValue = null;
		if (server != null) {
			server = server.applyQuotient(variables, network);
			serverValue = server.printString();
		}
		Vertex service = getRelationship(Primitive.SERVICE);
		Primitive serviceValue = null;
		if (service != null) {
			service = service.applyQuotient(variables, network);
			if (service.isPrimitive()) {
				serviceValue = (Primitive)service.getData();
			}
		}
		Vertex hint = getRelationship(Primitive.HINT);
		String hintValue = null;
		if (hint != null) {
			hint = hint.applyQuotient(variables, network);
			hintValue = hint.printString();
		}
		Vertex defaultResponse = getRelationship(Primitive.DEFAULT);
		String defaultValue = null;
		if (defaultResponse != null) {
			defaultResponse = defaultResponse.applyQuotient(variables, network);
			defaultValue = defaultResponse.printString();
		}
		try {
			String message = sentence.printString();
			String response = network.getBot().awareness().getSense(RemoteService.class).request(message, botValue, botidValue, serverValue, serviceValue, apikeyValue, limitValue, hintValue, network);
			if (response == null) {
				if (defaultValue != null && !defaultValue.isEmpty()) {
					return network.createSentence(defaultValue);					
				}
				return network.createVertex(Primitive.NULL);
			}
			return network.createSentence(response);
		} catch (Exception exception) {
			network.getBot().log(this, exception);
			if (defaultValue != null && !defaultValue.isEmpty()) {
				return network.createSentence(defaultValue);					
			}
			return network.createVertex(Primitive.NULL);
		}
	}

	/**
	 * Check if any of the words have the relationship.
	 */
	public Vertex checkRelationTargetForAllWords(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, Vertex left, Vertex right, Vertex relation, Collection<Relationship> words) {
		// Check all meanings of all words.
		if (words != null && !right.instanceOf(Primitive.WORD)) {
			Set<Vertex> processed = new HashSet<Vertex>();
			processed.add(right);
			for (Relationship word : words) {
				Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
				if (otherMeanings != null) {
					for (Relationship meaning : otherMeanings) {
						if (!processed.contains(meaning.getTarget())) {
							processed.add(meaning.getTarget());
							if (left.hasOrInheritsRelationship(relation, meaning.getTarget())) {
								// Left has the relationship, return true.
								return network.createVertex(Primitive.TRUE);
							} else if (left.hasOrInheritsInverseRelationship(relation, meaning.getTarget())) {
								// Left has an inverse relationship to the right, return false.
								return network.createVertex(Primitive.FALSE);
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check if any of the words have the relationship.
	 */
	public Vertex checkRelationRelationshipForAllWords(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network, Vertex left, Vertex right, Vertex relation, Collection<Relationship> words) {
		// Check all meanings of all words.
		if (words != null && !right.instanceOf(Primitive.WORD)) {
			Set<Vertex> processed = new HashSet<Vertex>();
			processed.add(right);
			for (Relationship word : words) {
				Collection<Relationship> otherMeanings = word.getTarget().getRelationships(Primitive.MEANING);
				if (otherMeanings != null) {
					for (Relationship meaning : otherMeanings) {
						if (!processed.contains(meaning.getTarget())) {
							processed.add(meaning.getTarget());
							if (left.hasOrInheritsRelationship(meaning.getTarget(), right)) {
								// Left has the relationship, return true.
								return network.createVertex(Primitive.TRUE);
							} else if (left.hasOrInheritsInverseRelationship(meaning.getTarget(), right)) {
								// Left has an inverse relationship to the right, return false.
								return network.createVertex(Primitive.FALSE);
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Apply the relation operation.
	 * IS :0 RELATED TO :1 [BY :2]
	 * Return if the left has the relation to the right
	 * i.e. "Bob love's Jill" (does Bob have #loves relationship to Jill)
	 * or also "Bob love's?" (what does Bob have a #loves relationship to?)
	 * or "Bob feels how to Jill?" (what is Bob's relationship to Jill?)
	 */
	public Vertex applyRELATION(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex result = null;
		Vertex relation = null;
		Vertex left = arguments.get(0).getTarget().applyQuotient(variables, network);
		Vertex right = arguments.get(1).getTarget().applyQuotient(variables, network);
		// The relation is optional, if no relation then check any relation
		if (arguments.size() > 2) {
			relation = arguments.get(2).getTarget().applyQuotient(variables, network);
		}
		if (relation == null) {
			// If no relation type, then check for any relationship.
			if (left.hasAnyRelationshipToTarget(right)) {
				// Left has a relationship to right, return true.
				result = network.createVertex(Primitive.TRUE);
			} else if (left.hasRelationship(right)) {
				// Left has a relationship of type right, return the value.
				result = left.mostConscious(right);
			} else if (right.hasRelationship(left)) {
				// Right has a relationship of type left, return the value.
				result = right.mostConscious(left);
			} else if (left.hasAnyRelationshipToTargetOfType(right)) {
				// Left has a relationship to a target that is an instance of right, return the target.
				result = left.mostConsciousTargetOfType(right);
			} else if (right.hasAnyRelationshipToTargetOfType(left)) {
				// Left has a relationship to a target that is an instance of right, return the target.
				result = right.mostConsciousTargetOfType(left);
			}
		} else {
			relation = relation.applyQuotient(variables, network);
			if (left.hasOrInheritsRelationship(relation, right)) {
				// Left has the relationship, return true.
				result = network.createVertex(Primitive.TRUE);
			} else if (left.hasOrInheritsInverseRelationship(relation, right)) {
				// Left has an inverse relationship to the right, return false.
				result = network.createVertex(Primitive.FALSE);
			} else {
				if (relation.is(Primitive.IS)) {
					if (left.hasAnyRelationshipToTarget(right)) {
						// Left has a relationship to right, return true.
						result = network.createVertex(Primitive.TRUE);
					}
				}
				if (result == null && (right.getData() instanceof String)) {
					// Check case.
					Vertex lower = network.createVertex(((String)right.getData()).toLowerCase());
					if (left.hasOrInheritsRelationship(relation, lower)) {
						// Left has the relationship, return true.
						result = network.createVertex(Primitive.TRUE);
					} else if (left.hasOrInheritsInverseRelationship(relation, lower)) {
						// Left has an inverse relationship to the right, return false.
						result = network.createVertex(Primitive.FALSE);
					}
					Vertex caps = network.createVertex(Utils.capitalize(((String)right.getData()).toLowerCase()));
					if (left.hasOrInheritsRelationship(relation, caps)) {
						// Left has the relationship, return true.
						result = network.createVertex(Primitive.TRUE);
					} else if (left.hasOrInheritsInverseRelationship(relation, caps)) {
						// Left has an inverse relationship to the right, return false.
						result = network.createVertex(Primitive.FALSE);
					}
				}
				if (result == null) {
					// Check all meanings of all words.
					Collection<Relationship> words = right.getRelationships(Primitive.WORD);
					result = checkRelationTargetForAllWords(arguments, variables, network, left, right, relation, words);
				}

				if (result == null) {					
					// Check synonyms as well.
					Collection<Relationship> words = right.getRelationships(Primitive.SYNONYM);
					result = checkRelationTargetForAllWords(arguments, variables, network, left, right, relation, words);
				}
				// Check all meanings of all words for relation.
				if (result == null) {
					// Check all meanings of all words.
					Collection<Relationship> words = relation.getRelationships(Primitive.WORD);
					result = checkRelationRelationshipForAllWords(arguments, variables, network, left, right, relation, words);
				}
				if (result == null) {
					// Check synonyms as well.
					Collection<Relationship> words = relation.getRelationships(Primitive.SYNONYM);
					result = checkRelationRelationshipForAllWords(arguments, variables, network, left, right, relation, words);
				}
			}
			// TODO: clean this up, and handle all other cases.
		}
		if (result != null) {
			network.getBot().log(SELF, "Found relation", Level.FINER, left, relation, right, result);
		} else {						
			result = network.createVertex(Primitive.UNKNOWN);
			network.getBot().log(SELF, "Relation unknown", Level.FINER, left, relation, right, result);
		}
		return result;
	}

	/**
	 * Apply the related operation.
	 * RELATED TO :0 [BY :1]
	 * Return what has a relationship to :0 by the relationships :1,
	 * or what has any relationship to :0.
	 */
	public Vertex applyRELATED(List<Relationship> arguments, Map<Vertex, Vertex> variables, Network network) {
		Vertex result = null;
		Vertex relation = null;
		Vertex source = arguments.get(0).getTarget().applyQuotient(variables, network);
		// The relation is optional, if no relation then check any relation
		if (arguments.size() > 1) {
			relation = arguments.get(1).getTarget().applyQuotient(variables, network);
		}
		List<Relationship> relationships = null;
		if (relation == null) {
			relationships = source.getNetwork().findAllRelationshipsTo(source);
		} else {
			relationships = source.getNetwork().findAllRelationshipsTo(source, relation);			
		}
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse() && ((result == null) || (relationship.getSource().getConsciousnessLevel() > result.getConsciousnessLevel()))) {
				result = relationship.getSource();
			}
		}
		if (result != null) {
			network.getBot().log(SELF, "Found relation", Level.FINER, source, relation, result);
		} else {
			result = network.createVertex(Primitive.NULL);
			network.getBot().log(SELF, "Relation unknown", Level.FINER, source, relation, result);
		}
		return result;
	}
	
	/**
	 * Compare if the two vertices match.
	 * Used for rule processing.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized Boolean matches(Vertex vertex, Map<Vertex, Vertex> variables) {
		if (this == vertex) {
			return Boolean.TRUE;
		}
		if (hasData() && vertex.hasData()) {
			if ((this.data instanceof String) && (vertex.getData() instanceof String)) {
				if (((String)this.data).equalsIgnoreCase((String)vertex.getData())) {
					return Boolean.TRUE;
				}
			} else if ((this.data instanceof Number) && (vertex.getData() instanceof Number)) {
				if (((Number)this.data).doubleValue() == ((Number)vertex.getData()).doubleValue()) {
					return Boolean.TRUE;
				}
			}
		}
		Vertex variable;
		Vertex match;
		if (isVariable() && vertex.isVariable()) {
			// Variables must be identical to match.
			return Boolean.FALSE;
		}
		if (instanceOf(Primitive.ARRAY) || vertex.instanceOf(Primitive.ARRAY)) {
			Vertex list = this;
			Vertex item = vertex;
			if (vertex.instanceOf(Primitive.ARRAY)) {
				list = vertex;
				item = this;					
			}
			Collection<Relationship> elements = list.orderedRelationships(Primitive.ELEMENT);
			if (elements != null) {
				for (Relationship element : elements) {
					if (element.getTarget().matches(item, variables) == Boolean.TRUE) {
						return Boolean.TRUE;
					}
				}
			}
		}
		if (instanceOf(Primitive.LIST) || vertex.instanceOf(Primitive.LIST)) {
			Vertex list = this;
			Vertex item = vertex;
			if (vertex.instanceOf(Primitive.LIST)) {
				list = vertex;
				item = this;					
			}
			Collection<Relationship> elements = list.orderedRelationships(Primitive.SEQUENCE);
			if (elements != null) {
				for (Relationship element : elements) {
					if (element.getTarget().matches(item, variables) == Boolean.TRUE) {
						return Boolean.TRUE;
					}
				}
			}
		}
		if (isVariable()) {
			variable = (Vertex)(Object)this;
			match = vertex;
		} else if (vertex.isVariable()) {
			variable = vertex;
			match = (Vertex)(Object)this;
		} else {
			if (instanceOf(Primitive.PATTERN)) {
				return Language.evaluatePattern(this, vertex, Primitive.WILDCARD, new HashMap<Vertex, Vertex>(), this.network);
			} else if (vertex.instanceOf(Primitive.PATTERN)) {
				return Language.evaluatePattern(vertex, this, Primitive.WILDCARD, new HashMap<Vertex, Vertex>(), this.network);
			}
			// Match primitives to words.
			if (isPrimitive() && hasRelationship(Primitive.WORD, vertex)) {
				return Boolean.TRUE;
			}
			// Not equal, not variables, don't match.
			return null;
		}
		// Return if variable has already been matched.
		if (variables.containsKey(variable)) {
			if (variables.get(variable) == match) {
				return Boolean.TRUE;
			}
		}
		this.network.getBot().log(variable, " checking match", Level.FINEST, match);
		// Compare the variables relationships to the match.
		boolean wasInclude = false;
		for (Iterator<Relationship> iterator = variable.allRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			boolean inverse = relationship.isInverse();
			if (relationship.getTarget().is(Primitive.VARIABLE) || relationship.getType().is(Primitive.COMMENT)) {
				// Ignore instance of variable relationship.
				continue;
			}
			if (relationship.getType().is(Primitive.EQUALS)) {
				// Check includes and excludes.
				if (relationship.getTarget().equals(match)) {
					if (inverse) {
						return Boolean.FALSE;
					}
					variables.put(variable, match);
					variables.put(match, variable);
					return Boolean.TRUE;
				}
				if (!inverse) {
					wasInclude = true;
				}
				continue;
			}
			Vertex target = relationship.getTarget();
			Vertex type = relationship.getType();
			// If neither is a variable, just check if the relationship exists.	
			if (!type.isVariable() && !target.isVariable()) {
				if (match.hasRelationship(type, target)) {
					if (inverse) {
						return Boolean.FALSE;
					} else {
						continue;
					}
				}				
			} else if (type.isVariable()) {
				// If the type is a variable, then must check if any types match, and check if their target matches.
				// If the type is a match, but no targets are, then the type cannot be a match.
				boolean found = false;
				for (Vertex typeMatch : match.getRelationships().keySet()) {
					if (type.matches(typeMatch, variables) == Boolean.TRUE) {
						if (target.isVariable()) {
							// Check if any targets match variable.
							found = matchesTarget(match, typeMatch, target, variables);
							if (found) {
								break;
							}
						} else {
							// Check if has exact relationship.
							if (match.hasRelationship(typeMatch, target)) {
								found = true;
								break;
							}
						}
						break;
					}
				}
				if (!inverse && found) {
					continue;
				} else if (inverse && found) {
					this.network.getBot().log(variable, " does not match", Level.FINEST, match);
					return Boolean.FALSE;
				} else if (inverse) {
					this.network.getBot().log(variable, " does not match", Level.FINEST, match);
				}
			} else if (target.isVariable()) {
				// Check if any targets match variable.
				boolean found = matchesTarget(match, type, target, variables);
				if (!inverse && found) {
					continue;
				} else if (inverse && found) {
					this.network.getBot().log(variable, " does not match", Level.FINEST, match);
					return Boolean.FALSE;
				} else if (inverse) {
					this.network.getBot().log(variable, " does not match", Level.FINEST, match);
				}
			}
			if (!inverse) {
				this.network.getBot().log(variable, " does not match", Level.FINEST, match);
				return null;
			}
		}
		if (wasInclude) {
			this.network.getBot().log(variable, " does not match", Level.FINEST, match);
			return Boolean.FALSE;
		}
		this.network.getBot().log(variable, " matches", Level.FINER, match);
		variables.put(variable, match);
		if (variable.hasName()) {
			((Map)variables).put(variable.getName(), match);
		}
		variables.put(match, variable);
		return Boolean.TRUE;
	}
	
	/**
	 * Return if any targets of the match's type relationship match the target variable.
	 */
	protected boolean matchesTarget(Vertex match, Vertex type, Vertex target, Map<Vertex, Vertex> variables) {
		// Check if any targets match variable.
		List<Relationship> targets = match.orderedRelationshipsByConsciousness(type);
		if (targets != null) {
			for (Relationship targetMatch : targets) {
				if (!targetMatch.isInverse() && (target.matches(targetMatch.getTarget(), variables)  == Boolean.TRUE)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Compare if the two vertices match.
	 * Depth allows breadth first search for variables.
	 * Used for rule processing.
	 */
	public synchronized boolean collectMatches(Vertex vertex, Map<Vertex, Set<Vertex>> variables) {
		if (this == vertex) {
			return true;
		}
		Vertex variable;
		Vertex match;
		if (isVariable() && vertex.isVariable()) {
			// Variables must be identical to match.
			return false;
		} else if (isVariable()) {
			variable = (Vertex)(Object)this;
			match = vertex;
		} else if (vertex.isVariable()) {
			variable = vertex;
			match = (Vertex)(Object)this;
		} else {
			// Not equal, not variables, don't match.
			return false;
		}
		// Return if variable has already been matched.
		Set<Vertex> matches = variables.get(variable);
		if (match == null) {
			matches = new HashSet<Vertex>();
			variables.put(variable, matches);
		}
		if (matches.contains(match)) {
			return true;
		}
		this.network.getBot().log(variable, " checking match", Level.FINEST, match);
		// Keep track of variables that cannot match other values.
		Map<Vertex, Set<Vertex>> conflicts = new HashMap<Vertex, Set<Vertex>>();
		// Compare the variables relationships to the match.
		for (Iterator<Relationship> iterator = variable.allRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			if (Primitive.VARIABLE.equals(relationship.getTarget().getData())) {
				// Ignore instance of variable relationship.
				continue;
			}
			Vertex target = relationship.getTarget();
			Vertex type = relationship.getType();
			// If neither is a variable, just check if the relationship exists.	
			if (!type.isVariable() && !target.isVariable()) {
				if (match.hasRelationship(type, target)) {
					continue;
				}
			}
			if (type.isVariable()) {
				// If the type is a variable, then must check if any types match, and check if their target matches.
				// If the type is a match, but no targets are, then the type cannot be a match.
				boolean found = false;
				for (Vertex typeMatch : match.getRelationships().keySet()) {
					if (type.collectMatches(typeMatch, variables)) {
						boolean localFound = false;
						if (target.isVariable()) {
							// Check if any targets match variable.
							Collection<Relationship> targets = match.getRelationships(typeMatch);
							if (targets != null) {
								for (Relationship targetMatch : targets) {
									if (target.collectMatches(targetMatch.getTarget(), variables)) {
										found = true;
										localFound = true;
									}
								}
							}
						} else {
							// Check if has exact relationship.
							if (match.hasRelationship(typeMatch, target)) {
								found = true;
								localFound = true;
							}
						}
						if (!localFound) {
							// Type cannot be a match (if this vertex is a match).
							Set<Vertex> typeConflicts = conflicts.get(type);
							if (typeConflicts == null) {
								typeConflicts = new HashSet<Vertex>();
								conflicts.put(type, typeConflicts);
							}
							typeConflicts.add(relationship.getType());
						}
					}
				}
				if (found) {
					continue;
				}
			} else if (target.isVariable()) {
				// Check if any targets match variable.
				Collection<Relationship> targets = match.getRelationships(type);
				boolean found = false;
				if (targets != null) {
					for (Relationship targetMatch : targets) {
						if (target.collectMatches(targetMatch.getTarget(), variables)) {
							found = true;
						}
					}
					if (found) {
						continue;
					}
				}
			}
			this.network.getBot().log(variable, " does not matches", Level.FINEST, match);
			return false;
		}
		this.network.getBot().log(variable, " matches", Level.FINER, match);
		matches.add(match);
		return true;
	}
	
	/**
	 * Compare if the two vertices match.
	 * 0 means perfect match.
	 * -n means n relationships that do not match.
	 * +n means n relationships that do match.
	 */
	public synchronized int compare(Vertex vertex) {
		if (this == vertex) {
			return 0;
		}
		int matches = 0;
		boolean match = true;
		for (Iterator<Relationship> iterator = allRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			if (relationship.isInverse()) {
				if (!vertex.hasRelationship(relationship.getType(), relationship.getTarget())) {
					matches ++;
				} else {
					matches --;
					match = false;
				}
			} else {
				if (vertex.hasRelationship(relationship.getType(), relationship.getTarget())) {
					matches ++;
				} else {
					matches --;
					match = false;
				}
			}
		}
		for (Iterator<Relationship> iterator = vertex.allRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			if (relationship.isInverse()) {
				if (!hasRelationship(relationship.getType(), relationship.getTarget())) {
					matches ++;
				} else {
					matches --;
					match = false;
				}
			} else {
				if (hasRelationship(relationship.getType(), relationship.getTarget())) {
					matches ++;
				} else {
					matches --;
					match = false;
				}
			}
		}
		if (match) {
			return 0;
		}
		return matches;
	}
	
	/**
	 * Fix the corrupted relationship index order.
	 * The order can get corrupted when relationships are forgotten.
	 */
	public void fixRelationships(Vertex type) {
		int index = 0;
		for (Relationship each : orderedRelationships(type)) {
			if (each.getIndex() != index) {
				each.setIndex(index);
			}
			index++;
		}
	}

	/**
	 * Return the total number of all relationships.
	 */
	public synchronized int totalRelationships() {
		int size = 0;
		for (Map<Relationship, Relationship> relationships : getRelationships().values()) {
			size = size + relationships.size();
		}
		return size;
	}

	/**
	 * Return any relationship target of the primitive type.
	 */
	public Vertex getRelationship(Primitive type) {
		return getRelationship(this.network.createVertex(type));
	}

	/**
	 * Return any relationship target of the type.
	 */
	public Vertex getRelationship(Vertex type) {
		Map<Relationship, Relationship> targets = getRelationships().get(type);
		if (targets == null) {
			return null;
		}
		for (Relationship relationship : targets.values()) {
			if (relationship.getCorrectness() > 0.0) {
				return relationship.getTarget();
			}
		}
		return null;
	}

	/**
	 * Return any relationship target of the primitive type.
	 */
	public Collection<Relationship> getRelationships(Primitive type) {
		return getRelationships(this.network.createVertex(type));
	}
	
	/**
	 * Return all of the relationships of the type.
	 */
	public Collection<Relationship> getRelationships(Vertex relationshipType) {
		Map<Relationship, Relationship> targets = getRelationships().get(relationshipType);
		if (targets == null) {
			return null;
		}		
		return new ArrayList<Relationship>(targets.values());
	}

	/**
	 * Return the last of the ordered relationship, or null.
	 */
	public Vertex lastRelationship(Vertex type) {
		if (getRelationships().get(type) == null) {
			return null;
		}
		int max = 0;
		Relationship last = null;
		for (Relationship relationship : getRelationships(type)) {
			if (relationship.getIndex() >= max) {
				max = relationship.getIndex();
				last = relationship;
			}
		}
		if (last == null) {
			return null;
		}
		if ((last.getIndex() + 1) != getRelationships(type).size()) {
			this.network.getBot().log(this, "Corrupted relationship index detected, correcting", Level.FINE, type);
			fixRelationships(type);
		}
		return last.getTarget();		
	}

	/**
	 * Return the fromLast last of the ordered relationship, or null.
	 * i.e. 2nd last, or 3rd last, etc.
	 */
	public Vertex lastRelationship(Vertex type, int fromLast) {
		if (getRelationships().get(type) == null) {
			return null;
		}
		List<Relationship> tail = new LinkedList<Relationship>();
		for (Relationship relationship : getRelationships(type)) {
			Relationship previous = null;
			int previousIndex = 0;
			for (int index = 0; index < tail.size(); index++) {
				Relationship oneOfTheLast = tail.get(index);
				if (relationship.getIndex() > oneOfTheLast.getIndex()) {
					previous = oneOfTheLast;
					previousIndex = index;
				}
			}
			if (previous != null) {
				tail.add(previousIndex + 1, relationship);
			} else if (tail.size() < fromLast) {
				tail.add(0, relationship);
			}
			if (tail.size() > fromLast) {
				tail.remove(0);
			}
		}
		if (tail.size() < fromLast) {
			return null;
		}
		Relationship last = tail.get(tail.size() - 1);
		if ((last.getIndex() + 1) != getRelationships(type).size()) {
			this.network.getBot().log(this, "Corrupted relationship index detected, correcting", Level.FINE, type);
			fixRelationships(type);
		}
		return tail.get(0).getTarget();		
	}
	
	/**
	 * Return the fromLast last of the ordered relationship, or null.
	 */
	public Vertex lastRelationship(Primitive type) {
		return lastRelationship(this.network.createVertex(type));
	}
	
	/**
	 * Return the last of the ordered relationship, or null.
	 */
	public Vertex lastRelationship(Primitive type, int fromLast) {
		return lastRelationship(this.network.createVertex(type), fromLast);
	}
	
	/**
	 * Return all of the relationships of the primitive type, sorted by index.
	 */
	public List<Relationship> orderedRelationships(Primitive primitive) {
		return orderedRelationships(this.network.createVertex(primitive));
	}
	
	/**
	 * Return all of the relationships of the type, sorted by index.
	 */
	public synchronized List<Relationship> orderedRelationships(Vertex relationshipType) {
		if (getRelationships().get(relationshipType) == null) {
			return null;
		}
		List<Relationship> list = new ArrayList<Relationship>(getRelationships(relationshipType));
		Collections.sort(list);
		return list;
	}
	
	/**
	 * Return all of the relationships of the type, sorted by consciousness level.
	 */
	public List<Relationship> orderedRelationshipsByConsciousness(Primitive primitive) {
		return orderedRelationships(this.network.createVertex(primitive));		
	}
	
	/**
	 * Return all of the relationships of the type, sorted by consciousness level.
	 */
	public synchronized List<Relationship> orderedRelationshipsByConsciousness(Vertex relationshipType) {
		if (getRelationships().get(relationshipType) == null) {
			return null;
		}
		List<Relationship> list = new ArrayList<Relationship>(getRelationships(relationshipType));
		Collections.sort(list, new Comparator<Relationship>() {
			public int compare(Relationship left, Relationship right) {
				float leftLevel = computeCorrectness(left);
				float rightLevel = computeCorrectness(right);
				if (leftLevel == rightLevel) {
					return 0;
				}
				if (leftLevel > rightLevel) {
					return -1;
				}
				return 1;
			}
		});
		return list;
	}
	
	/**
	 * Return all of the relationships targets of the primitive type, sorted by index.
	 */
	public List<Vertex> orderedRelations(Primitive primitive) {
		return orderedRelations(this.network.createVertex(primitive));
	}
	
	/**
	 * Return all of the relationship targets of the type, sorted by index.
	 */
	public List<Vertex> orderedRelations(Vertex relationshipType) {
		List<Relationship> relationships = orderedRelationships(relationshipType);
		if (relationships == null) {
			return null;
		}
		List<Vertex> vertices = new ArrayList<Vertex>(relationships.size());
		for (Relationship relationship : relationships) {
			if (relationship.getCorrectness() > 0.0) {
				vertices.add(relationship.getTarget());
			}
		}
		return vertices;
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Vertex mostConscious(Vertex type, float min) {
		return nextMostConscious(type, (Vertex)null, min, false);
	}
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	public Relationship mostConsciousRelationship(Vertex type) {
		return nextMostConsciousRelationship(type, (Vertex)null, 0, false);
	}
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	public Relationship nextMostConsciousRelationship(Vertex type, Vertex ignoring) {
		return nextMostConsciousRelationship(type, ignoring, 0, false);
	}
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	public Relationship mostConsciousRelationship(Primitive type) {
		return mostConsciousRelationship(this.network.createVertex(type));
	}
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	public Relationship mostConsciousRelationship(Primitive type, float correctness) {
		return nextMostConsciousRelationship(this.network.createVertex(type), (Vertex)null, correctness, false);
	}
	
	/**
	 * Return the relationship related by the type, with the high consciousness level.
	 */
	public Relationship nextMostConsciousRelationship(Primitive type, Vertex ignoring) {
		return nextMostConsciousRelationship(this.network.createVertex(type), ignoring);
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Vertex mostConscious(Primitive type, float min) {
		return mostConscious(this.network.createVertex(type), min);
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Vertex mostConscious(Vertex type, float min, boolean inverse) {
		return nextMostConscious(type, (Vertex)null, min, inverse);
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Vertex nextMostConscious(Primitive type, Set<Vertex> ignoring) {
		return nextMostConscious(this.network.createVertex(type), ignoring, 0f, false);
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Vertex nextMostConscious(Primitive type, Vertex ignoring, float min) {
		return nextMostConscious(this.network.createVertex(type), ignoring, min);
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Relationship nextMostConsciousRelationship(Primitive type, Vertex ignoring, float min) {
		return nextMostConsciousRelationship(this.network.createVertex(type), ignoring, min, false);
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level greater than the value.
	 */
	public Vertex nextMostConscious(Vertex type, Vertex ignoring, float min) {
		return nextMostConscious(type, ignoring, min, false);
	}
	
	/**
	 * Return the target vertex related by the type with the high consciousness level.
	 */
	public Vertex mostConscious(Primitive type) {
		return mostConscious(this.network.createVertex(type));
	}
	
	/**
	 * Return the target vertex related by the type with the high consciousness level.
	 */
	public Vertex mostConscious(Vertex type) {
		return nextMostConscious(type, (Vertex)null, 0, false);
	}
	
	/**
	 * Return the target vertex inversely/negatively related by the type, with the high consciousness level.
	 * The level is multiplied by the relationship correctness.
	 */
	public Vertex nextMostConscious(Primitive type, Vertex ignoring, float min, boolean inverse) {
		return nextMostConscious(this.network.createVertex(type), ignoring, 0f, false);
	}
	
	/**
	 * Return the target vertex inversely/negatively related by the type, with the high consciousness level.
	 * The level is multiplied by the relationship correctness.
	 */
	public Vertex nextMostConscious(Vertex type, Vertex ignoring, float min, boolean inverse) {
		Relationship relationship = nextMostConsciousRelationship(type, ignoring, min, inverse);
		if (relationship != null) {
			return relationship.getTarget();
		}
		return null;
	}
	
	/**
	 * Return the target vertex inversely/negatively related by the type, with the high consciousness level.
	 * The level is multiplied by the relationship correctness.
	 */
	public Vertex nextMostConscious(Vertex type, Set<Vertex> ignoring, float min, boolean inverse) {
		Relationship relationship = nextMostConsciousRelationship(type, ignoring, min, inverse);
		if (relationship != null) {
			return relationship.getTarget();
		}
		return null;
	}
	
	public static float computeCorrectness(Relationship relationship) {
		float correctness = Math.abs(relationship.getCorrectness());
		int consciousnessLevel = relationship.getTarget().getConsciousnessLevel();
		float level = consciousnessLevel * correctness;
		// Return most correct if all at 0 level.
		if (consciousnessLevel == 0) {
			level = correctness;
		}
		return level;
	}
	
	/**
	 * Return the target vertex inversely/negatively related by the type, with the high consciousness level.
	 * The level is multiplied by the relationship correctness.
	 */
	public synchronized Relationship nextMostConsciousRelationship(Vertex type, Vertex ignoring, float min, boolean inverse) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship highest = null;
		float highestLevel = 0;
		float highestCorrectness = 0;
		for (Relationship relationship : relationships) {
			if ((relationship.isInverse() && inverse) || (!relationship.isInverse() && !inverse)) {
				if (ignoring != relationship.getTarget()) {
					float correctness = Math.abs(relationship.getCorrectness());
					float level = computeCorrectness(relationship);
					if ((highest == null) || (level > highestLevel)) {
						if ((highest == null) || (correctness >= highestCorrectness)) {
							highest = relationship;
							highestLevel = level;
							highestCorrectness = correctness;
						}
					}
				}
			}
		}
		if (highest == null) {
			return null;
		}
		if (Math.abs(highest.getCorrectness()) < min) {
			this.network.getBot().log(this, "Relationship not sufficiently correct", Level.FINER, highest, highest.getCorrectness(), min);
			return null;
		}
		return highest;
	}
	
	/**
	 * Return the target vertex inversely/negatively related by the type, with the high consciousness level.
	 * The level is multiplied by the relationship correctness.
	 */
	public synchronized Relationship nextMostConsciousRelationship(Vertex type, Set<Vertex> ignoring, float min, boolean inverse) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship highest = null;
		float highestLevel = 0;
		float highestCorrectness = 0;
		for (Relationship relationship : relationships) {
			if ((relationship.isInverse() && inverse) || (!relationship.isInverse() && !inverse)) {
				if (!ignoring.contains(relationship.getTarget())) {
					float correctness = Math.abs(relationship.getCorrectness());
					float level = computeCorrectness(relationship);
					if ((highest == null) || (level > highestLevel)) {
						if ((highest == null) || (correctness >= highestCorrectness)) {
							highest = relationship;
							highestLevel = level;
							highestCorrectness = correctness;
						}
					}
				}
			}
		}
		if (highest == null) {
			return null;
		}
		if (Math.abs(highest.getCorrectness()) < min) {
			this.network.getBot().log(this, "Relationship not sufficiently correct", Level.FINER, highest, highest.getCorrectness(), min);
			return null;
		}
		return highest;
	}


	/**
	 * Return the most conscious target the vertex has any relationship to that is an instantiation of the classification.
	 */
	public synchronized Vertex mostConsciousTargetOfType(Vertex classification) {
		Iterator<Relationship> relationships = allRelationships();
		Vertex highest = null;
		Vertex instantiation = this.network.createVertex(Primitive.INSTANTIATION);
		while (relationships.hasNext()) {
			Relationship relationship = relationships.next();
			if (!relationship.isInverse()
					&& (relationship.getTarget().hasRelationship(instantiation, classification))
					&& ((highest == null)
							|| (relationship.getTarget().getConsciousnessLevel() > highest.getConsciousnessLevel()))) {
				highest = relationship.getTarget();
			}
		}
		return highest;
	}
	
	/**
	 * Return the target vertex related by the type, with the high consciousness level.
	 */
	public synchronized Vertex mostConscious(Vertex type, Vertex classification) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Vertex highest = null;
		Vertex instantiation = this.network.createVertex(Primitive.INSTANTIATION);
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse()
					&& (relationship.getTarget().hasRelationship(instantiation, classification))
					&& ((highest == null)
							|| (relationship.getTarget().getConsciousnessLevel() > highest.getConsciousnessLevel()))) {
				highest = relationship.getTarget();
			}
		}
		return highest;
	}
	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType) {
		return getAssoiate(type, associate, associateType, null, null, null, null);
	}
	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex defaultAssociate) {
		return getAssoiate(type, associate, associateType, (Vertex)null, null, (Vertex)null, null, defaultAssociate);
	}
	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2) {
		return getAssoiate(type, associate, associateType, associate2, associateType2, null, null);
	}
	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2, Vertex associate3, Vertex associateType3) {
		return getAssoiate(type, associate, associateType, associate2, associateType2, associate3, associateType3, null);
	}
	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 * Ideally the target also has associate2.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType,
				Vertex associate2, Vertex associateType2,
				Vertex associate3, Vertex associateType3,
				Vertex defaultAssociate) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship best = null;
		float bestMatch = 0;
		for (Relationship relationship : relationships) {
			if (relationship.isInverse()) {
				continue;
			}
			Vertex target = relationship.getTarget();
			float match = 0;
			if (target.hasRelationship(associateType, associate)) {
				match = target.getRelationship(associateType, associate).getCorrectness();
			}
			if ((associate2 != null) && target.hasRelationship(associateType2, associate2)) {
				match = match + target.getRelationship(associateType2, associate2).getCorrectness();
			}
			if ((associate3 != null) && target.hasRelationship(associateType3, associate3)) {
				match = match + target.getRelationship(associateType3, associate3).getCorrectness();
			}
			if (match > 0 && match >= bestMatch) {
				if (best == null || match > bestMatch || relationship.getCorrectness() > best.getCorrectness()) {
					best = relationship;
					bestMatch = match;
				}
			}
		}
		if (best != null) {
			return best.getTarget();
		}
		return defaultAssociate;
	}

	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 * Ideally the target also has associate2.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType,
				Vertex associate2, Vertex associateType2,
				Collection<Relationship> associates3, Vertex associateType3,
				Vertex defaultAssociate) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship best = null;
		float bestMatch = 0;
		for (Relationship relationship : relationships) {
			if (relationship.isInverse()) {
				continue;
			}
			Vertex target = relationship.getTarget();
			float match = 0;
			if (target.hasRelationship(associateType, associate)) {
				match = target.getRelationship(associateType, associate).getCorrectness();
			}
			if ((associate2 != null) && target.hasRelationship(associateType2, associate2)) {
				match = match + target.getRelationship(associateType2, associate2).getCorrectness();
			}
			if (associates3 != null) {
				float max = 0;
				for (Relationship associate3 : associates3) {
					if (target.hasRelationship(associateType3, associate3.getTarget())) {
						max = Math.max(max, target.getRelationship(associateType3, associate3.getTarget()).getCorrectness());
					}
				}
				match = match + max;
			}
			if (match > 0 && match >= bestMatch) {
				if (best == null || match > bestMatch || relationship.getCorrectness() > best.getCorrectness()) {
					best = relationship;
					bestMatch = match;
				}
			}
		}
		if (best != null) {
			return best.getTarget();
		}
		return defaultAssociate;
	}

	
	/**
	 * Return the target vertex related by the type, that has the relationship to the previous, otherwise null if not found.
	 * Ideally the target also has associate2.
	 */
	public synchronized Vertex getAssoiate(Vertex type, Vertex associate, Vertex associateType,
				Collection<Relationship> associates2, Vertex associateType2,
				Collection<Relationship> associates3, Vertex associateType3,
				Vertex defaultAssociate) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship best = null;
		float bestMatch = 0;
		for (Relationship relationship : relationships) {
			if (relationship.isInverse()) {
				continue;
			}
			Vertex target = relationship.getTarget();
			float match = 0;
			if (target.hasRelationship(associateType, associate)) {
				match = target.getRelationship(associateType, associate).getCorrectness();
			}
			if (associates2 != null) {
				float max = 0;
				for (Relationship associate2 : associates2) {
					if (target.hasRelationship(associateType2, associate2.getTarget())) {
						max = Math.max(max, target.getRelationship(associateType2, associate2.getTarget()).getCorrectness());
					}
				}
				match = match + max;
			}
			if (associates3 != null) {
				float max = 0;
				for (Relationship associate3 : associates3) {
					if (target.hasRelationship(associateType3, associate3.getTarget())) {
						max = Math.max(max, target.getRelationship(associateType3, associate3.getTarget()).getCorrectness());
					}
				}
				match = match + max;
			}
			if (match > 0 && match >= bestMatch) {
				if (best == null || match > bestMatch || relationship.getCorrectness() > best.getCorrectness()
						|| (match == bestMatch && relationship.getCorrectness() == best.getCorrectness()
								&& best.getTarget().getData() instanceof String && Utils.isCaps((String)best.getTarget().getData()))) {
					best = relationship;
					bestMatch = match;
				}
			}
		}
		if (best != null) {
			return best.getTarget();
		}
		return defaultAssociate;
	}
	
	/**
	 * Return the target vertex related by the type, that is also most correctly related to the associate vertex by the relationship.
	 * If no related vertices are related to the associate, then return the most conscious.
	 */
	public synchronized Vertex mostConsciousWithAssoiate(Vertex type, Vertex associate, Vertex associateType) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship highest = null;
		Map<Vertex, Relationship> assoicates = new HashMap<Vertex, Relationship>();
		Collection<Relationship> associateRelationships = associate.getRelationships(associateType);
		if (associateRelationships != null) {
			for (Relationship relationship : associate.getRelationships(associateType)) {
				assoicates.put(relationship.getTarget(), relationship);
			}
			for (Relationship relationship : relationships) {
				Vertex target = relationship.getTarget();
				Relationship associateTarget =  assoicates.get(target);
				if ((associateTarget != null) && (!relationship.isInverse())
						&& ((highest == null) || (associateTarget.getCorrectness() > highest.getCorrectness()))) {
					highest = associateTarget;
				}
			}
		}
		if (highest == null) {
			return mostConscious(type);
		}
		return highest.getTarget();
	}
	
	/**
	 * Return the target vertex related by the type, that is also most correctly related to the associate vertex by the relationship.
	 * If no related vertices are related to the associate, then return the most conscious.
	 */
	public synchronized Vertex mostConsciousWithAssoiates(Vertex type, Vertex associate, Vertex associateType, Vertex associate2, Vertex associateType2) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return null;
		}
		Relationship highest = null;
		Map<Vertex, Relationship> first = new HashMap<Vertex, Relationship>();
		Map<Vertex, Relationship> both = new HashMap<Vertex, Relationship>();
		Map<Vertex, Relationship> all = new HashMap<Vertex, Relationship>();
		Collection<Relationship> associateRelationships = associate.getRelationships(associateType);
		if (associateRelationships != null) {
			for (Relationship relationship : associateRelationships) {
				first.put(relationship.getTarget(), relationship);
				all.put(relationship.getTarget(), relationship);
			}
		}
		Collection<Relationship> associate2Relationships = associate2.getRelationships(associateType2);
		if (associate2Relationships != null) {
			for (Relationship relationship : associate2Relationships) {
				if (first.containsKey(relationship.getTarget())) {
					both.put(relationship.getTarget(), relationship);					
				}
				all.put(relationship.getTarget(), relationship);
			}
		}
		if (!both.isEmpty()) {
			for (Relationship relationship : relationships) {
				Vertex target = relationship.getTarget();
				Relationship associateTarget =  both.get(target);
				if ((associateTarget != null) && (!relationship.isInverse())
						&& ((highest == null) || (associateTarget.getCorrectness() > highest.getCorrectness()))) {
					highest = associateTarget;
				}
			}
			if (highest != null) {
				return highest.getTarget();
			}
		}
		if (!all.isEmpty()) {
			for (Relationship relationship : relationships) {
				Vertex target = relationship.getTarget();
				Relationship associateTarget =  all.get(target);
				if ((associateTarget != null) && (!relationship.isInverse())
						&& ((highest == null) || (associateTarget.getCorrectness() > highest.getCorrectness()))) {
					highest = associateTarget;
				}
			}
		}
		if (highest == null) {
			return mostConscious(type);
		}
		return highest.getTarget();
	}
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	public synchronized void weakAssociateAll(Primitive associate, Vertex target, Primitive type, float correctnessMultiplier) {
		weakAssociateAll(this.network.createVertex(associate), target, this.network.createVertex(type), correctnessMultiplier);
	}
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	public synchronized void weakAssociateAll(Vertex associate, Vertex target, Vertex type, float correctnessMultiplier) {
		Collection<Relationship> relationships = getRelationships(associate);
		if (relationships == null) {
			return;
		}
		for (Relationship relationship : relationships) {
			relationship.getTarget().addWeakRelationship(type, target, correctnessMultiplier);				
		}
	}
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	public synchronized void associateAll(Primitive associate, Vertex target, Primitive type) {
		associateAll(this.network.createVertex(associate), target, this.network.createVertex(type));
	}
	
	/**
	 * Associate each of the relationship target vertices with the target vertex by the type.
	 */
	public synchronized void associateAll(Vertex associate, Vertex target, Vertex type) {
		Collection<Relationship> relationships = getRelationships(associate);
		if (relationships == null) {
			return;
		}
		for (Relationship relationship : relationships) {
			Vertex relation = relationship.getTarget();
			if (!relation.isVariable()) {
				if (relation.isArray()) {
					Collection<Relationship> elements = getRelationships(Primitive.ELEMENT);
					if (elements == null) {
						continue;
					}
					for (Relationship element : elements) {
						element.getTarget().addRelationship(type, target);
					}
				} else {
					relation.addRelationship(type, target);					
				}
			}
		}
	}
	
	/**
	 * Dissociate the source with each of the relationship targets by the type.
	 */
	public synchronized void inverseAssociateAll(Primitive associate, Vertex target, Primitive type) {
		inverseAssociateAll(this.network.createVertex(associate), target, this.network.createVertex(type));
	}
	
	/**
	 * Dissociate the source with each of the relationship targets by the type.
	 */
	public synchronized void inverseAssociateAll(Vertex associate, Vertex target, Vertex type) {
		Collection<Relationship> relationships = getRelationships(associate);
		if (relationships == null) {
			return;
		}
		for (Relationship relationship : relationships) {
			relationship.getTarget().removeRelationship(type, target);				
		}
	}
	
	/**
	 * Return if any of the associates of the vertex have an inverse/negative relationship of the type to the target.
	 */
	public synchronized boolean hasAnyAssociatedInverseRelationship(Primitive associate, Vertex target, Primitive type) {
		return hasAnyAssociatedInverseRelationship(this.network.createVertex(associate), target, this.network.createVertex(type));
	}
	
	/**
	 * Return if any of the associates of the vertex have an inverse/negative relationship of the type to the target.
	 */
	public synchronized boolean hasAnyAssociatedInverseRelationship(Vertex associate, Vertex target, Vertex type) {
		Collection<Relationship> relationships = getRelationships(associate);
		if (relationships == null) {
			return false;
		}
		for (Relationship relationship : relationships) {
			if (relationship.getTarget().hasInverseRelationship(type, target)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove the relationship of the relation type to the target vertex.
	 * This records the relationships with a negative correctness,
	 * or subtracts from the current correctness.
	 */
	public synchronized Relationship removeRelationship(Vertex type, Vertex target) {
		return removeRelationship(new BasicRelationship(this, type, target));
	}

	/**
	 * Remove the relationship of the relation primitive type to the target vertex.
	 * This records the relationships with a negative correctness,
	 * or subtracts from the current correctness.
	 */
	public synchronized Relationship removeRelationship(Primitive type, Vertex target) {
		return removeRelationship(this.network.createVertex(type), target);
	}

	/**
	 * Remove the relationship of the relation primitive type to the target vertex.
	 * This records the relationships with a negative correctness,
	 * or subtracts from the current correctness.
	 */
	public synchronized Relationship removeRelationship(Primitive type, Primitive target) {
		return removeRelationship(this.network.createVertex(type), this.network.createVertex(target));
	}

	/**
	 * Remove the relationship.
	 * This records the relationships with a negative correctness,
	 * or subtracts from the current correctness.
	 */
	public synchronized Relationship removeRelationship(Relationship relationship) {
		relationship = addRelationship(relationship, true);
		float correctness = relationship.getCorrectness();
		// Either switch to negative as the inverse value,
		// or increment its incorrectness by 1/2.
		if (correctness > 0) {
			relationship.setCorrectness((1.0f - correctness) * -1.0f);
		} else {
			correctness = correctness + ((-1.0f - correctness) * 0.5f);
			if (correctness <= -0.99) {
				correctness = -1;
			}
			relationship.setCorrectness(correctness);
		}
		return relationship;
	}
	
	/**
	 * Remove the relationship.
	 */
	public synchronized void internalRemoveRelationship(Relationship relationship) {
		if (relationship == null) {
			return;
		}
		Map<Relationship, Relationship> relationships = getRelationships().get(relationship.getType());
		if (relationships == null) {
			if (this.allRelationships != null && this.allRelationships.contains(relationship)) {
				this.network.removeRelationship(relationship);
				this.allRelationships.remove(relationship);
			}
			return;
		}
		Relationship existing = relationships.remove(relationship);
		if (existing == null) {
			if (this.allRelationships != null && this.allRelationships.contains(relationship)) {
				this.network.removeRelationship(relationship);
				this.allRelationships.remove(relationship);
			}
			return;
		}
		this.network.removeRelationship(existing);
		// Also remove from allRelationships
		if (this.allRelationships != null) {
			this.allRelationships.remove(existing);
		}
		if (relationships.isEmpty()) {
			getRelationships().remove(relationship.getType());
			return;
		}
		// Fix indexes.
		for (Relationship each : relationships.values()) {
			if (each.getIndex() > relationship.getIndex()) {
				each.setIndex(each.getIndex() - 1);
			}
		}
	}
	
	/**
	 * Remove the relationship.
	 */
	public synchronized void internalRemoveRelationship(Primitive type, Primitive target) {
		Relationship relationship = getRelationship(type, target);
		if (relationship != null) {
			internalRemoveRelationship(relationship);
		}
	}
	
	/**
	 * Remove the relationship.
	 */
	public synchronized void internalRemoveRelationship(Vertex type, Vertex target) {
		Relationship relationship = getRelationship(type, target);
		if (relationship != null) {
			internalRemoveRelationship(relationship);
		}
	}
	
	/**
	 * Remove the relationships of the type.
	 */
	public synchronized void internalRemoveRelationships(Primitive type) {
		internalRemoveRelationships(this.network.createVertex(type));
	}
	
	/**
	 * Remove the relationships of the type.
	 */
	public synchronized void internalRemoveRelationships(Vertex type) {
		Map<Relationship, Relationship> relationships = getRelationships().get(type);
		if (relationships == null) {
			return;
		}
		for (Relationship relationship : relationships.values()) {
			this.network.removeRelationship(relationship);
			// Also remove from allRelationships
			if (this.allRelationships != null) {
				this.allRelationships.remove(relationship);
			}
		}
		getRelationships().remove(type);
	}
	
	/**
	 * Pin the targets of all relationships to memory.
	 */
	public synchronized void pinChildren() {
		for (Iterator<Relationship> iterator = allRelationships(); iterator.hasNext(); ) {
			iterator.next().getTarget().setPinned(true);
		}
	}
	
	/**
	 * Unpin the targets of all relationships from memory.
	 */
	public synchronized void unpinChildren() {
		for (Iterator<Relationship> iterator = allRelationships(); iterator.hasNext(); ) {
			iterator.next().getTarget().setPinned(false);
		}
	}
	
	/**
	 * Remove all relationships.
	 */
	public synchronized void internalRemoveAllRelationships() {
		for (Iterator<Relationship> iterator = allRelationships(); iterator.hasNext(); ) {
			this.network.removeRelationship(iterator.next());
		}
		getRelationships().clear();
		if (this.allRelationships != null) {
			this.allRelationships.clear();
		}
	}
	
	/**
	 * Replace the relationship with the new target at the same index.
	 */
	public synchronized void replaceRelationship(Relationship oldRelationship, Vertex newTarget) {
		Map<Relationship, Relationship> relationships = getRelationships().get(oldRelationship.getType());
		if (relationships == null) {
			return;
		}
		Relationship existing = relationships.remove(oldRelationship);
		if (existing == null) {
			return;
		}
		// Also remove from allRelationships
		if (this.allRelationships != null) {
			this.allRelationships.remove(existing);
		}
		addRelationship(oldRelationship.getType(), newTarget, oldRelationship.getIndex());
	}
	
	/**
	 * Set the relationship, removing the old value.
	 */
	public synchronized void setRelationship(Primitive type, Vertex newValue) {
		setRelationship(this.network.createVertex(type), newValue);
	}
	
	/**
	 * Set the relationship, removing the old value.
	 */
	public synchronized void setRelationship(Primitive type, Primitive newValue) {
		setRelationship(this.network.createVertex(type), this.network.createVertex(newValue));
	}
	
	/**
	 * Set the relationship, removing the old value.
	 */
	public synchronized void setRelationship(Vertex type, Vertex newValue) {
		Map<Relationship, Relationship> relationships = getRelationships().get(type);
		if (relationships != null) {
			for (Iterator<Relationship> iterator = relationships.values().iterator(); iterator.hasNext(); ) {
				Relationship existingValue = iterator.next();
				iterator.remove();
				if (this.allRelationships != null) {
					this.allRelationships.remove(existingValue);
				}
				this.network.removeRelationship(existingValue);
			}
		}
		addRelationship(type, newValue);
	}
	
	public void setRelationships(Map<Vertex, Map<Relationship, Relationship>> relationships) {
		this.relationships = relationships;
	}
	
	/**
	 * Provides an easier method of traversing all the relations of all types of a vertex.
	 */
	public synchronized Iterator<Relationship> allRelationships() {
		return new RelationshipIterator(false);
	}

	/**
	 * Iterator over all related vertices to the vertex.
	 */
	public void iterate(VertexIterator iterator) {
		if ((iterator.getDepth() == 0) && (iterator.getPath() == Path.BreadthFirst)) {
			Map<Vertex, Vertex> currentLevel = new IdentityHashMap<Vertex, Vertex>();
			iterator.setBreadthSet(currentLevel);
			iterator.incrementDepth();
			iterate(iterator);
			iterator.decrementDepth();
			Map<Vertex, Vertex> nextLevel = currentLevel;
			while (!nextLevel.isEmpty()) {
				iterator.incrementDepth();
				if (iterator.isMaxDepth()) {
					return;
				}
				currentLevel = nextLevel;
				nextLevel = new IdentityHashMap<Vertex, Vertex>();
				iterator.setBreadthSet(nextLevel);
				for (Vertex vertex : currentLevel.values()) {
					vertex.iterate(iterator);
					if (iterator.isMaxIterations()) {
						return;
					}
				}
			}
		} else {
			if (iterator.getTraversed().containsKey(this)) {
				return;
			}
			if (iterator.isMaxIterations()) {
				return;
			}
			if (iterator.getIgnorePrimitives() && isPrimitive()) {
				return;
			}
			iterator.getTraversed().put(this, this);
			boolean iterateRelationships = iterator.iterate(this);
			if (!iterateRelationships) {
				return;
			}
			if (getRelationships().isEmpty()) {
				return;
			}
			if (iterator.getPath() == Path.BreadthFirst) {
				for (Map<Relationship, Relationship>  relationships : getRelationships().values()) {
					for (Relationship relationship : relationships.values()) {
						iterator.addBreadth(relationship.getTarget());
					}
				}
			} else {
				// Depth first
				if (iterator.isMaxDepth()) {
					return;
				}
				iterator.incrementDepth();
				for (Map<Relationship, Relationship>  relationships : getRelationships().values()) {
					for (Relationship relationship : relationships.values()) {
						relationship.getTarget().iterate(iterator);
						if (iterator.isMaxIterations()) {
							return;
						}
					}
				}
				iterator.decrementDepth();
			}
		}
	}
	
	/**
	 * Pin the vertex and all of its descendants into memory.
	 * This can be used for important data such as language rules.
	 */
	public void pinDescendants() {
		iterate(new AbstractVertexIterator() {
			public boolean iterate(Vertex vertex) {
				if (vertex.isPinned() || (vertex.getCreationDate().getTime() > (System.currentTimeMillis() - 60000))) {
					network.getBot().log(vertex, "pinned", Level.FINEST);
					vertex.setPinned(true);
					return true;
				} else {
					return false;
				}
			}
		});
	}
	
	/**
	 * Unpin the vertex and all of its descendants from memory.
	 * This can be used to release removed language rules.
	 */
	public void unpinDescendants() {
		final long creationTime = getCreationDate().getTime();
		iterate(new AbstractVertexIterator() {
			public boolean iterate(Vertex vertex) {
				if (vertex.isPinned()
						&& (vertex.getCreationDate().getTime() > (creationTime - 60000))
						&& (vertex.getCreationDate().getTime() < (creationTime + 60000))) {
					network.getBot().log(vertex, "unpinned", Level.FINEST);
					vertex.setPinned(false);
					return true;
				} else {
					return false;
				}
			}
		});
	}
	
	/**
	 * Provides an easier method of traversing all the relations of all types of a vertex.
	 */
	public synchronized Iterator<Relationship> orderedAllRelationships() {
		return new RelationshipIterator(true);
	}
	
	public String displayString() {
		StringWriter writer = new StringWriter();
		writer.write(String.valueOf(this.id));
		if (hasData()) {
			writer.write(" : ");
			if (isPrimitive()) {
				writer.write("#");				
			}
			if (this.data instanceof String) {
				writer.write("\"");				
			}
			writer.write(getDataValue());
			if (this.data instanceof String) {
				writer.write("\"");				
			}
		} else {
			if (hasName()) {
				writer.write(" : {");
				writer.write(getName());
				writer.write("}");
			}
		}
		return writer.toString();
	}
	
	public static void writeHeader(Vertex vertex, PrintWriter writer) {
		writer.print("<");
		writer.print(vertex.getId());
		if (vertex.hasName()) {
			writer.print(" {");
			writer.print(vertex.getName());
			writer.print("}");
		}
		if (vertex.hasData()) {
			writer.print(" ");
			if (vertex.isPrimitive()) {
				writer.print("#");				
			}
			writer.print(vertex.getDataValue());
		}
		writer.print(" ");
		if (vertex.isDirty()) {
			writer.print("*,");
		}
		writer.print("a:" + vertex.getAccessCount());
		writer.print(",c:" + vertex.getConsciousnessLevel());
		if (vertex.isPinned()) {
			writer.print(",p");
		}
		writer.print(">");
	}
	
	/**
	 * Return if the vertex is a system primitive.
	 */
	public boolean isPrimitive() {
		return this.data instanceof Primitive;
	}
	
	/**
	 * Return if the vertex is for a meta relationship.
	 */
	public boolean isMeta() {
		return hasRelationship(Primitive.INSTANTIATION, Primitive.META);
	}
	
	/**
	 * Return if the vertex is a variable.
	 */
	public boolean isVariable() {
		return hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
	}
	
	/**
	 * Return if the vertex is a list.
	 */
	public boolean isList() {
		return hasRelationship(Primitive.INSTANTIATION, Primitive.LIST);
	}
	
	/**
	 * Return if the vertex is an array.
	 */
	public boolean isArray() {
		return hasRelationship(Primitive.INSTANTIATION, Primitive.ARRAY);
	}
	
	/**
	 * Return if the vertex is a equation.
	 */
	public boolean isEquation() {
		return hasRelationship(Primitive.INSTANTIATION, Primitive.EQUATION);
	}
	
	/**
	 * Return if the vertex data is equal to the data.
	 */
	public synchronized boolean is(Object data) {
		return (data != null) && data.equals(this.data);
	}
	
	/**
	 * Return if the vertex is an instantiation of the primitive type.
	 */
	public synchronized boolean instanceOf(Primitive type) {
		return instanceOf(this.network.createVertex(type));
	}
	
	/**
	 * Return if the vertex is an instantiation of the type.
	 */
	public synchronized boolean instanceOf(Vertex type) {
		return hasRelationship(this.network.createVertex(Primitive.INSTANTIATION), type);
	}
	
	/**
	 * Return if the vertex has a relationship of the type primitive.
	 */
	public boolean hasRelationship(Primitive type) {
		return hasRelationship(this.network.createVertex(type));
	}
	
	/**
	 * Return if the vertex has a relationship of the type primitive to the target primitive.
	 */
	public boolean hasRelationship(Primitive type, Primitive target) {
		return hasRelationship(this.network.createVertex(type), this.network.createVertex(target));
	}
	
	/**
	 * Return if the vertex has a relationship of the type primitive to the target.
	 */
	public boolean hasRelationship(Primitive type, Vertex target) {
		return hasRelationship(this.network.createVertex(type), target);
	}
	
	/**
	 * Return the relationship of the type primitive to the target.
	 */
	public Relationship getRelationship(Primitive type, Vertex target) {
		return getRelationship(this.network.createVertex(type), target);
	}
	
	/**
	 * Return the relationship of the type primitive to the target.
	 */
	public Relationship getRelationship(Primitive type, Primitive target) {
		return getRelationship(this.network.createVertex(type), this.network.createVertex(target));
	}
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	public boolean hasRelationship(Vertex type, Vertex target) {
		Relationship relationship = getRelationship(type, target);
		return (relationship != null) && (!relationship.isInverse());
	}
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	public boolean hasOrInheritsRelationship(Vertex type, Vertex target) {
		return hasOrInheritsRelationship(type, target, null);
	}
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	public synchronized boolean hasOrInheritsRelationship(Vertex type, Vertex target, Map<Vertex, Vertex> recursion) {
		Relationship relationship = getRelationship(type, target);
		if (relationship == null) {
			// Check for variables.
			if (target.isVariable()) {
				Collection<Relationship> relationships = getRelationships(type);
				if (relationships != null) {
					if (recursion == null) {
						recursion = new HashMap<Vertex, Vertex>();
					}
					boolean inverse = true;
					for (Relationship each : relationships) {
						if (target.matches(each.getTarget(), recursion)  == Boolean.TRUE) {
							if (!each.isInverse()) {
								return true;
							} else {
								inverse = true;
							}
						}
					}
					if (inverse) {
						return false;
					}
				}
			}
			
			// If no relationship, check its classifications.
			Collection<Relationship> classifications = null;
			if (instanceOf(Primitive.CLASSIFICATION)) {
				classifications = getRelationships(Primitive.SPECIALIZATION);
			} else {
				classifications = getRelationships(Primitive.INSTANTIATION);
			}
			if (classifications != null) {
				// Switch instantiation to specialization.
				if (type.isPrimitive() && type.getData().equals(Primitive.INSTANTIATION)) {
					type = this.network.createVertex(Primitive.SPECIALIZATION);
				}
				for (Relationship classification : classifications) {
					if (recursion == null) {
						recursion = new HashMap<Vertex, Vertex>();
					}
					recursion.put(this, this);
					if (!recursion.containsKey(classification.getTarget())  && classification.getTarget().hasOrInheritsRelationship(type, target, recursion)) {
						return true;
					}
				}
			}
		}
		return (relationship != null) && (!relationship.isInverse());
	}
	
	/**
	 * Return the relationship of the type primitive to the target.
	 */
	public synchronized Relationship getRelationship(Vertex type, Vertex target) {
		Map<Relationship, Relationship> relationships = getRelationships().get(type);
		if (relationships == null) {
			return null;
		}
		return relationships.get(new BasicRelationship(this, type, target));
	}
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 * Include inverses.
	 */
	public synchronized boolean internalHasRelationship(Vertex type, Vertex target) {
		Map<Relationship, Relationship> relationships = getRelationships().get(type);
		if (relationships == null) {
			return false;
		}
		boolean b = relationships.containsKey(new BasicRelationship(this, type, target));
		if (b) {
			return b;
		}
		return b;
	}
	
	/**
	 * Return if the vertex has any (non-inverse) relationship of the type.
	 */
	public synchronized boolean hasRelationship(Vertex type) {
		Collection<Relationship> relationships = getRelationships(type);
		if (relationships == null) {
			return false;
		}
		for (Relationship relationship : relationships) {
			if (!relationship.isInverse()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return if the vertex has any relationship to any target.
	 */
	public synchronized boolean hasAnyRelationshipToTarget(Vertex target) {
		Iterator<Relationship> relationships = allRelationships();
		while (relationships.hasNext()) {
			Relationship relationship = relationships.next();
			if (!relationship.isInverse() && relationship.getTarget().equals(target)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return if the vertex has any relationship to any target that is an instantiation of the classification.
	 */
	public synchronized boolean hasAnyRelationshipToTargetOfType(Vertex classification) {
		Iterator<Relationship> relationships = allRelationships();
		while (relationships.hasNext()) {
			Relationship relationship = relationships.next();
			if (!relationship.isInverse() && relationship.getTarget().hasRelationship(Primitive.INSTANTIATION, classification)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return if the vertex has an inverse relationship of the type to the target.
	 */
	public boolean hasInverseRelationship(Primitive type, Primitive target) {
		return hasInverseRelationship(this.network.createVertex(type), this.network.createVertex(target));
	}
	
	/**
	 * Return if the vertex has an inverse relationship of the type to the target.
	 */
	public boolean hasInverseRelationship(Primitive type, Vertex target) {
		return hasInverseRelationship(this.network.createVertex(type), target);
	}
	
	/**
	 * Return if the vertex has an inverse relationship of the type to the target.
	 */
	public synchronized boolean hasInverseRelationship(Vertex type, Vertex target) {
		Relationship relationship = getRelationship(type, target);
		return (relationship != null) && (relationship.isInverse());
	}
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	public boolean hasOrInheritsInverseRelationship(Vertex type, Vertex target) {
		return hasOrInheritsInverseRelationship(type, target, null);
	}
	
	/**
	 * Return if the vertex has a relationship of the type to the target.
	 */
	public synchronized boolean hasOrInheritsInverseRelationship(Vertex type, Vertex target, Map<Vertex, Vertex> recursion) {
		Relationship relationship = getRelationship(type, target);
		if (relationship == null) {
			// Check for variables.
			if (target.isVariable()) {
				Collection<Relationship> relationships = getRelationships(type);
				if (relationships != null) {
					if (recursion == null) {
						recursion = new HashMap<Vertex, Vertex>();
					}
					for (Relationship each : relationships) {
						if (target.matches(each.getTarget(), recursion)  == Boolean.TRUE) {
							return each.isInverse();
						}
					}
				}
			}
			
			// If no relationship, check its classifications.
			Collection<Relationship> classifications = null;
			if (instanceOf(Primitive.CLASSIFICATION)) {
				classifications = getRelationships(Primitive.SPECIALIZATION);
			} else {
				classifications = getRelationships(Primitive.INSTANTIATION);
			}
			if (classifications != null) {
				// Switch instantiation to specialization.
				if (type.isPrimitive() && type.getData().equals(Primitive.INSTANTIATION)) {
					type = this.network.createVertex(Primitive.SPECIALIZATION);
				}
				for (Relationship classification : classifications) {
					if (recursion == null) {
						recursion = new HashMap<Vertex, Vertex>();
					}
					recursion.put(this, this);
					if (!recursion.containsKey(classification.getTarget())  && classification.getTarget().hasOrInheritsInverseRelationship(type, target, recursion)) {
						return true;
					}
				}
			}
		}
		return (relationship != null) && (relationship.isInverse());
	}

	public String description() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		writeHeader(this, writer);
		writer.println();
		if (this.relationships != null) {
			for (Map.Entry<Vertex, Map<Relationship, Relationship>> entry : getRelationships().entrySet()) {
				Vertex type = (Vertex) entry.getKey();
				Map<Relationship, Relationship> targets = entry.getValue();
				writer.print("\t");
				writeHeader(type, writer);
				writer.print(" -> ");
				boolean first = true;
				for (Relationship relationship : targets.values()) {
					if (first) {
						first = false;
					} else {
						writer.write(", ");
					}
					writer.print("(0.");
					writer.print((int)(relationship.getCorrectness() * 100));
					writer.print(")");
					writeHeader(relationship.getTarget(), writer);
				}
				writer.println();
			}
		}
		writer.flush();
		stringWriter.flush();

		return stringWriter.toString();
	}
		
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		writeHeader(this, writer);
		writer.flush();
		stringWriter.flush();
		
		return stringWriter.toString();
	}
	
	public String getDataType() {
		if (this.data == null) {
			return this.dataType;
		}
		return convertDataType(this.data);
	}

	public String getDataValue() {
		return convertDataValue(this.data);
	}
	
	@SuppressWarnings("unchecked")
	public void setDataValue(String value) {
		if (value == null) {
			this.data = null;
			return;
		}
		if (this.dataType instanceof String) {
			try {
				if (this.dataType.equals("Primitive")) {
					this.data = new Primitive(value);
				} else if (this.dataType.equals("String")) {
					this.data = value;
				} else if (this.dataType.equals("Time")) {
					this.data = Utils.parseTime((String)value);
				} else if (this.dataType.equals("Date")) {
					this.data = Utils.parseDate((String)value);
				} else if (this.dataType.equals("Timestamp")) {
					this.data = Utils.parseTimestamp(value);
				} else if (this.dataType.equals("Image")) {
					this.data = new BinaryData((String)value);
				} else if (this.dataType.equals("Binary")) {
					this.data = new BinaryData((String)value);
				} else if (this.dataType.equals("Text")) {
					this.data = new TextData((String)value);
				} else if (this.dataType.equals("URI")) {
					this.data = new URI((String)value);
				} else {
					Class<Object> typeClass = (Class<Object>)Class.forName((String)this.dataType);							
					this.data = typeClass.getConstructor(String.class).newInstance(value);
					if (this.data instanceof URL) {
						try {
							this.data = ((URL) (this.data)).toURI();
						} catch (Exception invalid) {
							URL url = (URL) this.data;
							this.data = new URI(url.getProtocol(), url.getAuthority(), url.getPath(), url.getQuery(), url.getRef());
						}
					}
				}
			} catch (Exception error) {
				if (this.network == null) {
					System.out.println("DataValue error:" + this.id + "-" + value);
					error.printStackTrace();
				} else {
					this.network.getBot().log(this.id, "DataValue error", Bot.WARNING, value);
					this.network.getBot().log(this, error);
				}
			}
		} else {
			this.data = value;
		}
	}
	
	/**
	 * Set the internal data-type of the vertex.
	 */
	public void setType(String type) {
		this.dataType = type;
	}
	
	@SuppressWarnings("unchecked")
	public void setDataType(String type) {
		this.dataType = type;
		if (this.data instanceof String) {
			try {
				if (type.equals("Primitive")) {
					this.data = new Primitive((String)this.data);
				} else if (type.equals("String")) {
					//this.data = this.data;
				} else if (type.equals("Time")) {
					this.data = Utils.parseTime((String)this.data);
				} else if (type.equals("Date")) {
					this.data = Utils.parseDate((String)this.data);
				} else if (type.equals("Timestamp")) {
					this.data = Utils.parseTimestamp((String)this.data);
				} else if (type.equals("Image")) {
					this.data = new BinaryData((String)this.data);
				} else if (type.equals("Binary")) {
					this.data = new BinaryData((String)this.data);
				} else if (type.equals("Text")) {
					this.data = new TextData((String)this.data);
				} else if (type.equals("URI")) {
					this.data = new URI((String)this.data);
				} else {
					Class<Object> typeClass = (Class<Object>)Class.forName(type);							
					this.data = typeClass.getConstructor(String.class).newInstance(this.data);
				}
			} catch (Exception error) {
				if (this.network == null) {
					error.printStackTrace();
				} else {
					this.network.getBot().log(this, error);
				}
			}
		}
	}

	public static String convertDataValue(Object data) {
		if (data == null) {
			return null;
		} else if (data instanceof String) {
			return (String)data;
		} else if (data instanceof Primitive) {
			// TODO: prefix a #
			return ((Primitive) data).getIdentity();
		} else if (data instanceof BinaryData) {
			return String.valueOf(((BinaryData)data).getId());
		} else if (data instanceof TextData) {
			return String.valueOf(((TextData)data).getId());
		} else {
			return data.toString();
		}
	}
	
	public static String convertDataType(Object data) {
		if (data == null) {
			return null;
		} else if (data instanceof String) {
			return "String";
		} else if (data instanceof Primitive) {
			return "Primitive";
		} else if (data instanceof Time) {
			return "Time";
		} else if (data instanceof Timestamp) {
			return "Timestamp";
		} else if (data instanceof java.sql.Date) {
			return "Date";
		} else if (data instanceof BinaryData) {
			return "Binary";
		} else if (data instanceof TextData) {
			return "Text";
		} else if (data instanceof URI) {
			return "URI";
		} else {
			return data.getClass().getName();
		}
	}

	/**
	 * Create a copy of the vertex with all of the same relationships.
	 * Since data is unique, this should only be used on vertices without data.
	 */
	public synchronized Vertex copy() {
		Vertex copy = this.network.createVertex();
		for (Iterator<Relationship> iterator = allRelationships(); iterator.hasNext(); ) {
			Relationship relationship = iterator.next();
			copy.addRelationship(relationship.getType(), relationship.getTarget());
		}
		return copy;
	}

	/**
	 * Create a copy of the vertex with only the id.
	 */
	public Vertex detach() {
		BasicVertex vertex = new BasicVertex();
		vertex.setId(this.id);
		return vertex;
	}

	public synchronized Collection<Relationship> getAllRelationships() {
		// Should not be required, as allRelationships is maintained.
		/*if (this.relationships != null) {
			this.allRelationships = new ArrayList<Relationship>();
			for (Iterator<Relationship> iterator = allRelationships(); iterator.hasNext(); ) {
				this.allRelationships.add(iterator.next());
			}
			this.relationships = null;
		}*/
		return this.allRelationships;
	}

	protected void setAllRelationships(Collection<Relationship> allRelationships) {
		this.allRelationships = allRelationships;
		this.relationships = null;
	}

	/**
	 * Return the name of the vertex.
	 * The name is used as a hint to refer to what the vertex represents.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the vertex.
	 * The name is used as a hint to refer to what the vertex represents.
	 */
	public void setName(String name) {
		if ((name != null) && (name.length() > AbstractNetwork.MAX_TEXT)) {
			name = name.substring(0, AbstractNetwork.MAX_TEXT);
		}
		this.name = name;
	}
	
	/**
	 * Some vertices have no name,
	 * this provides an easy test method.
	 */
	public boolean hasName() {
		return this.name != null;
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
	 * Print the object's data such as a sentence or paragraph.
	 */
	public String printString() {
		return printString(0);
	}
	
	/**
	 * Print the object's data such as a sentence or paragraph.
	 */
	public String printString(int depth) {
		if (depth > 100) {
			return "";
		}
		StringWriter writer = new StringWriter();
		if (instanceOf(Primitive.PARAGRAPH) && this.data == null) {
			Collection<Vertex> sentences = orderedRelations(Primitive.SENTENCE);
			if (sentences != null) {
				boolean first = true;
				String last = "";
				for (Vertex each : sentences) {
					if (last.length() > 0) {
						Character terminator = last.charAt(last.length() - 1);
						if (Character.isLetterOrDigit(terminator)) {
							writer.write(".");
						}
					}
					if (!first) {
						writer.write("  ");
					}
					last = each.printString(depth++);
					writer.write(last);
					first = false;
				}
			}
		} else if (instanceOf(Primitive.ARRAY)) {
			Collection<Vertex> elements = orderedRelations(Primitive.ELEMENT);
			writer.write("[");
			if (elements != null) {
				boolean first = true;
				for (Vertex each : elements) {
					if (!first) {
						writer.write(",  ");
					}
					writer.write(each.printString(depth++));
					first = false;
				}
			}
			writer.write("]");
		} else if (instanceOf(Primitive.LIST)) {
			Collection<Vertex> elements = orderedRelations(Primitive.SEQUENCE);
			writer.write("(");
			if (elements != null) {
				boolean first = true;
				for (Vertex each : elements) {
					if (!first) {
						writer.write(",  ");
					}
					writer.write(each.printString(depth++));
					first = false;
				}
			}
			writer.write(")");
		} else if (this.data != null) {
			writer.write(this.data.toString());
		} else if (instanceOf(Primitive.FRAGMENT) || instanceOf(Primitive.SENTENCE)) {
			return Language.printFragment(this, this.network.createVertex(Primitive.NULL), this.network.createVertex(Primitive.NULL), network);
		} else if (getName() != null) {
			writer.write(getName());
		} else {
			writer.write("{" + String.valueOf(getId()) + "}");			
		}
		return writer.toString();
	}

}