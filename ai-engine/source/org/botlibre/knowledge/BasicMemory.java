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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Memory;
import org.botlibre.api.knowledge.MemoryEventListener;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;

/**
 * Defines a set of networks that make up a knowledge base.
 * Defines long term, short term and flash networks.
 * Basic implementation to allow subclasses to avoid defining some of the basic stuff,
 * Note this basic implementation is not persistent.
 */

public class BasicMemory implements Memory {

	/** Back reference to Bot instance. **/
	protected Bot bot;
	protected List<Vertex> activeMemory;
	protected Network shortTermMemory;
	protected Network longTermMemory;
	protected List<MemoryEventListener> listeners;
	
	public BasicMemory() {
		this.longTermMemory = new BasicNetwork();
		this.longTermMemory.setBot(getBot());
		this.shortTermMemory = new BasicNetwork(this.longTermMemory);
		this.shortTermMemory.setBot(getBot());
		this.activeMemory = new ArrayList<Vertex>();
		this.listeners = new ArrayList<MemoryEventListener>();
	}

	/**
	 * Return the current connected database name.
	 */
	public String getMemoryName() {
		return "Basic";
	}

	/**
	 * Return Bot.
	 */
	public Bot getBot() {
		return bot;
	}
	
	/**
	 * Set Bot.
	 */
	public void setBot(Bot bot) {
		this.bot = bot;
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	public void initialize(Map<String, Object> properties) {
		return;
	}
	
	/**
	 * Active memory represents the last sensory state.
	 */
	public List<Vertex> getActiveMemory() {
		return activeMemory;
	}
	
	/**
	 * Add the sensory data to the active memory.
	 * Register the vertex in the short-term memory and return the registered version.
	 */
	public synchronized Vertex addActiveMemory(Vertex vertex) {
		Vertex activeVertex = getShortTermMemory().createVertex(vertex);
		getActiveMemory().add(activeVertex);
		for (MemoryEventListener listener : getListeners()) {
			listener.addActiveMemory(vertex);
		}
		notifyAll();
		return activeVertex;
	}

	/**
	 * Represents a non-committed transactional memory.
	 * Helps to define a learn scope and local context.
	 */
	public Network getShortTermMemory() {
		return shortTermMemory;
	}

	/**
	 * Return an isolated transactional memory.
	 * Can be used by senses or sub-conscious thought for concurrent processing.
	 */
	public Network newMemory() {
		return new BasicNetwork(getLongTermMemory());
	}

	/**
	 * Represents the persisted memory (or cache there of).
	 */
	public Network getLongTermMemory() {
		return longTermMemory;
	}
	
	public void setActiveMemory(List<Vertex> activeMemory) {
		this.activeMemory = activeMemory;
	}
	
	public void setShortTermMemory(Network shortTermMemory) {
		this.shortTermMemory = shortTermMemory;
	}
	
	public void setLongTermMemory(Network longTermMemory) {		
		this.longTermMemory = longTermMemory;
		if (getShortTermMemory().getParent() != longTermMemory) {
			getShortTermMemory().setParent(longTermMemory);
		}
	}
	
	/**
	 * Merge the short term memory into the long term and clears the short term.
	 * This is similar to a transactional commit.
	 * The changes should also be persisted, as the long term should always just be a cache of the storage.
	 * This implementation does not support persistence.
	 */
	public void save() {
		getBot().log(this, "Saving", Bot.FINE, getShortTermMemory());
		getLongTermMemory().merge(getShortTermMemory());
		getShortTermMemory().resume();
	}
		
	/**
	 * This implementation does not support persistence.
	 */
	public void restore() {
		getBot().log(this, "Restoring", Bot.FINE, this);
	}
	
	/**
	 * This implementation does not support persistence.
	 */
	public void fastRestore(String database) {
		getBot().log(this, "Restoring", Bot.FINE, this, database);
	}
		
	/**
	 * This implementation does not support persistence.
	 */
	public void restore(String database) {
		getBot().log(this, "Restoring", Bot.FINE, this, database);
	}

	/**
	 * Create a memory database.
	 */
	public void createMemory(String database) { }

	/**
	 * Create a memory database.
	 */
	public void createMemoryFromTemplate(String database, String template) { }

	/**
	 * Destroy the database.
	 */
	public void destroyMemory(String database) { }
	
	/**
	 * Delete all content from the database.
	 */
	public void deleteMemory() {
		this.longTermMemory = new BasicNetwork();
		this.longTermMemory.setBot(getBot());
		this.shortTermMemory = new BasicNetwork(this.longTermMemory);
		this.shortTermMemory.setBot(getBot());		
	}
	
	/**
	 * Allow import of another memory location.
	 */
	public void importMemory(String location) { }
	
	/**
	 * Allow switching to another memory location.
	 */
	public void switchMemory(String location) { }
	
	/**
	 * Shutdown the memory.
	 */
	public synchronized void shutdown() {
		try {
			save();
		} catch (Exception exception) {
			getBot().log(this, exception);
		}
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	public void pool() {
		
	}
	
	/**
	 * Reset the short term and active memories.
	 * Revert to the long term state.
	 */
	public void abort() {
		getBot().log(this, "Aborting", Bot.FINE, getShortTermMemory());
		this.activeMemory = new LinkedList<Vertex>();
		this.shortTermMemory = new BasicNetwork();
		this.shortTermMemory.setBot(getBot());
	}

	public String toString() {
		return getClass().getSimpleName() + "(active(" + this.activeMemory.size()
			//+ "), short(" + this.shortTermMemory.size()
			+ "), long(" + this.longTermMemory.size() + "))";			
	}

	public List<MemoryEventListener> getListeners() {
		return listeners;
	}

	protected void setListeners(List<MemoryEventListener> listeners) {
		this.listeners = listeners;
	}
	
	/**
	 * Add the memory listener.
	 * It will be notified of any new active memory.
	 */
	public void addListener(MemoryEventListener listener) {
		getListeners().add(listener);
	}
	
	public void removeListener(MemoryEventListener listener) {
		getListeners().remove(listener);
	}

	public int cacheSize() {
		return 0;
	}
	
	public void freeMemory() {	}
}