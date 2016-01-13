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
package org.botlibre.thought.consciousness;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.thought.Mind.MentalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.BasicThought;

/**
 * Consciousness monitors active memory and decides what to do.
 */
public class Consciousness extends BasicThought {
	public static int IGNORE_RELATIONS_MAX = 50;
	public static int TRAVERSAL_LIMIT = 100;
	public static int MAX_PROCESS_TIME = 100;
			
	/** Defines the flow of time. */
	protected Long lastContext;
	
	/**
	 * Create a new consciousness.
	 */
	public Consciousness() {
	}

	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		if (properties.containsKey("traversal-limit")) {
			TRAVERSAL_LIMIT = Integer.parseInt((String)properties.get("traversal-limit"));
			log("Init property:", Level.FINE, "traversal-limit", TRAVERSAL_LIMIT);
		}
	}
	
	/**
	 * Return the last conscious point in time.
	 */
	public Vertex getLastContext(Network network) {
		if (this.lastContext == null) {
			return null;
		}
		return network.findById(this.lastContext);
	}
	
	/**
	 * Set the last conscious point in time.
	 */
	public void setLastContext(Vertex lastContext) {
		if (lastContext == null) {
			this.lastContext = null;
		} else {
			this.lastContext = lastContext.getId();			
		}
	}
	
	/**
	 * Return the number of levels to traverse a vertex for the current state.
	 */
	public int getTraversalLevel() {
		MentalState state = this.bot.mind().getState();
		if (state == MentalState.PANIC) {
			return 1;
		} else if (state == MentalState.ALERT) {
			return 3;
		} else if (state == MentalState.ACTIVE) {
			return 5;
		} else if (state == MentalState.BORED) {
			return 10;
		} else if (state == MentalState.ASLEEP) {
			return 20;
		}
		return 1;
	}
	
	/**
	 * Age the network, decrease consciousness level by 10%.
	 */
	public void age(Network network) {
		for (Vertex vertex : network.findAll()) {
			int level = vertex.getConsciousnessLevel();
			if (level > 0) {
				vertex.decrementConsciousnessLevel(level / 2);
			}
		}
	}

	/**
	 * Return the current allowed processing time.
	 * This decreases when stressed.
	 */
	public long getProcessingTime() {
		long processTime = MAX_PROCESS_TIME;
		int state = this.bot.mind().getState().ordinal();
		if (state > MentalState.ALERT.ordinal()) {
			processTime = MAX_PROCESS_TIME / 5;
		} else if (state > MentalState.ACTIVE.ordinal()) {
			processTime = MAX_PROCESS_TIME / 2;
		} else if (state < MentalState.ACTIVE.ordinal()) {
			processTime = MAX_PROCESS_TIME * 2;
		}
		return processTime;
	}
	
	/**
	 * Analyse the active memory.
	 * Output the active article to the senses.
	 */
	@Override
	public void think() {
		Network network = getShortTermMemory();
		if (!isEnabled()) {
			// Still need to record context processing.
			List<Vertex> activeMemory = this.bot.memory().getActiveMemory();
			Iterator<Vertex> vertices = activeMemory.iterator();
			
			// Create a context for this point in time, associate everything with it.
			Vertex context = network.createVertex(Primitive.NULL);
			while (vertices.hasNext()) {
				// Must register into the current memory context.
				Vertex vertex = network.createVertex(vertices.next());
				if (!vertex.isPrimitive() && !vertex.hasRelationship(Primitive.INSTANTIATION, Primitive.CONTEXT)) {
					vertex.addRelationship(Primitive.CONTEXT, context);
				}
			}
			return;
		}
		long startTime = System.currentTimeMillis();
		long processTime = getProcessingTime();
		// First age short term memory.
		age(network);
		
		List<Vertex> activeMemory = this.bot.memory().getActiveMemory();
		Iterator<Vertex> vertices = activeMemory.iterator();
		
		// Create a context for this point in time, associate everything with it.
		Vertex context = network.createTimestamp();
		context.addRelationship(Primitive.INSTANTIATION, Primitive.CONTEXT);
		while (vertices.hasNext()) {
			// Must register into the current memory context.
			Vertex vertex = network.createVertex(vertices.next());
			if (!vertex.isPrimitive() && !vertex.hasRelationship(Primitive.INSTANTIATION, Primitive.CONTEXT)) {
				vertex.addRelationship(Primitive.CONTEXT, context);
				context.addRelationship(Primitive.CONTEXT, vertex);
			}
		}
		// Associate this point in time with the previous to create a flow.
		Vertex lastContext = getLastContext(network);
		if (lastContext != null) {
			lastContext.addRelationship(network.createVertex(Primitive.NEXT), context);
			context.addRelationship(network.createVertex(Primitive.PREVIOUS), lastContext);
		}
		setLastContext(context);

		vertices = activeMemory.iterator();
		// Process active vertices n levels deep.
		Set<Vertex> recursiveSet = new HashSet<Vertex>();
		// Use a breath first search, and abort at 1000 vertices.
		Set<Vertex> breadthSet = new HashSet<Vertex>();
		while (vertices.hasNext()) {
			// Must register into the current memory context.
			breadthSet.add(network.createVertex(vertices.next()));
		}
		int levels = getTraversalLevel();
		while ((levels > 0) && (recursiveSet.size() < TRAVERSAL_LIMIT)) {
			Set<Vertex> nextLevelBreadthSet = new HashSet<Vertex>();
			Iterator<Vertex> iterator = breadthSet.iterator();
			boolean maxTime = false;
			while (iterator.hasNext() && (recursiveSet.size() < TRAVERSAL_LIMIT)) {
				think(iterator.next(), levels, recursiveSet, nextLevelBreadthSet);
				long currentTime = System.currentTimeMillis();
				if ((currentTime - startTime) > processTime) {
					maxTime = true;
					log("Process time limit reached", Level.INFO, processTime, recursiveSet.size());					
					break;
				}
			}
			if (maxTime) {
				break;
			}
			breadthSet = nextLevelBreadthSet;
			levels--;
		}
		if (recursiveSet.size() >= TRAVERSAL_LIMIT) {
			log("Traversal limit reached", Level.FINE, recursiveSet.size());
		}
	}

	@Override
	public void awake() {
		String enabled = this.bot.memory().getProperty("Consciousness.enabled");
		if (enabled != null) {
			setEnabled(Boolean.valueOf(enabled));
		}
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex mood = memory.createVertex(getClass());
		Vertex property = mood.getRelationship(Primitive.ENABLED);
		if (property != null) {
			setEnabled((Boolean)property.getData());
		}
		
		// Remove old properties.
		mood.internalRemoveRelationships(Primitive.ENABLED);
		
		memory.save();
		
		saveProperties();
	}

	public void saveProperties() {
		Network memory = this.bot.memory().newMemory();
		memory.saveProperty("Consciousness.enabled", String.valueOf(isEnabled()), true);
		memory.save();
	}
	
	/**
	 * Analyse vertex and traverse its relationships.
	 * Swap the active vertex if interesting.
	 */
	public void think(Vertex vertex, int levels, Set<Vertex> recursiveSet, Set<Vertex> breadtheSet) {
		if (vertex == null || recursiveSet.contains(vertex)) {
			return;
		}
		recursiveSet.add(vertex);
		int size = recursiveSet.size();
		vertex.incrementConsciousnessLevel(levels);
		log("Increment:" + levels, Level.FINEST, vertex);
		for (Entry<Vertex, Map<Relationship, Relationship>> entry : vertex.getRelationships().entrySet()) {
			if ((size + breadtheSet.size()) >= TRAVERSAL_LIMIT) {
				break;
			}
			// Only traverse rare relationships.
			if (entry.getValue().size() > IGNORE_RELATIONS_MAX) {
				continue;
			}
			for (Relationship relationship : entry.getValue().values()) {
				breadtheSet.add(relationship.getType());
				breadtheSet.add(relationship.getTarget());
			}
		}
		for (Iterator<Relationship> relationships = vertex.allRelationships(); relationships.hasNext();) {
			if ((size + breadtheSet.size()) >= TRAVERSAL_LIMIT) {
				break;
			}
			Relationship relationship = relationships.next();
			breadtheSet.add(relationship.getType());
			breadtheSet.add(relationship.getTarget());
		}
	}
}
