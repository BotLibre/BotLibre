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

import java.util.List;
import java.util.Map;

import org.botlibre.Bot;

/**
 * Defines a set of networks that make up a knowledge base.
 * Defines long term, short term and active networks.
 * Handle persistence of networks and merging of network spaces from short term to long term.
 */

public interface Memory {
	
	/**
	 * Active memory represents the last sensory state.
	 */
	List<Vertex> getActiveMemory();

	/**
	 * Add the sensory data to the active memory.
	 * Register the vertex in the short-term memory and return the registered version.
	 */
	Vertex addActiveMemory(Vertex vertex);
	
	/**
	 * Represents a non-committed transactional memory.
	 * Helps to define a learn scope and local context.
	 */
	Network getShortTermMemory();

	/**
	 * Return an isolated transactional memory.
	 * Can be used by senses or sub-conscious thought for concurrent processing.
	 */
	Network newMemory();

	/**
	 * Represents the persisted memory (or cache there of).
	 */
	Network getLongTermMemory();
	
	/**
	 * Merge the short term memory into the long term and clears the short term.
	 * This is similar to a transactional commit.
	 * The changes should also be persisted, as the long term should always just be a cache of the storage.
	 */
	void save() throws MemoryStorageException;
		
	/**
	 * Restores the memory from a persisted state.
	 */
	void restore() throws MemoryStorageException;
		
	/**
	 * Restores the memory from a persisted state.
	 */
	void restore(String database, boolean isSchema) throws MemoryStorageException;
	
	/**
	 * Restores the memory from a persisted state.
	 */
	void fastRestore(String database, boolean isSchema) throws MemoryStorageException;

	/**
	 * Return the current connected database name.
	 */
	String getMemoryName();

	/**
	 * Create a memory database.
	 */
	void createMemory(String database);

	/**
	 * Create a memory database.
	 */
	void createMemory(String database, boolean isSchema);

	/**
	 * Create a memory database.
	 */
	void createMemoryFromTemplate(String database, String template);

	/**
	 * Create a memory database.
	 */
	void createMemoryFromTemplate(String database, boolean isSchema, String template, boolean templateIsSchema);

	/**
	 * Destroy the database.
	 */
	void destroyMemory(String database);

	/**
	 * Destroy the database.
	 */
	void destroyMemory(String database, boolean isSchema);
	
	/**
	 * Delete all content from the database.
	 */
	void deleteMemory();
	
	/**
	 * Allow import of another memory location.
	 */
	void importMemory(String location);
	
	/**
	 * Shutdown the memory.
	 */
	void shutdown();
	
	/**
	 * Reset state when instance is pooled.
	 */
	void pool();
	
	/**
	 * Allow switching to another memory location.
	 */
	void switchMemory(String location);
	
	/**
	 * Allow switching to another memory location.
	 */
	void switchMemory(String location, boolean isSchema);

	/**
	 * Reset the short term and active memories.
	 * Revert to the long term state.
	 */
	void abort();

	Bot getBot();
	
	void setBot(Bot Bot);

	/**
	 * Initialize any configurable settings from the properties.
	 */
	void initialize(Map<String, Object> properties);

	void awake();
	
	void loadProperties(String propertySet);

	void clearProperties(String propertySet);
	
	/**
	 * Return the property setting.
	 */
	String getProperty(String property);

	/**
	 * Set the property setting.
	 */
	String setProperty(String property, String value);

	/**
	 * Remove the property setting.
	 */
	String removeProperty(String property);
	
	/**
	 * Add the memory listener.
	 * It will be notified of any new active memory.
	 */
	void addListener(MemoryEventListener listener);
	
	void removeListener(MemoryEventListener listener);

	int cacheSize();
	
	void freeMemory();
}