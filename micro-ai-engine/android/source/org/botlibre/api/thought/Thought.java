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
package org.botlibre.api.thought;

import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.knowledge.Primitive;

/**
 * Something that given a network of objects can perform some function.
 */

public interface Thought {

	/**
	 * Return the name that identifies the thought.
	 */
	String getName();

	/**
	 * Set the name that identifies the thought.
	 */
	void setName(String name);
	
	/**
	 * Stop analyzing network.
	 */
	void stop();
	
	boolean isStopped();
	
	/**
	 * Reset state when instance is pooled.
	 */
	void pool();
	
	/**
	 * Analyze and extend the network.
	 */
	void think();

	/**
	 * Perform any initialization required on startup.
	 */
	void awake();

	/**
	 * Return the primitive representation of the thought.
	 */
	Primitive getPrimitive();
	
	/**
	 * Return the associated Bot instance.
	 */
	Bot getBot();

	/**
	 * Set the associated Bot instance.
	 */
	void setBot(Bot Bot);
	
	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);
	
	/**
	 * Thoughts can be conscious or sub-conscious.
	 * A conscious thought is run by the mind single threaded with exclusive access to the short term memory.
	 * A sub-conscious thought is run concurrently, and must run in its own memory space.
	 */
	boolean isConscious();
	
	/**
	 * Return if this thought must run even under stress.
	 */
	boolean isCritical();

	void saveProperties();
	
	/**
	 * Migrate to new properties system.
	 */
	void migrateProperties();
}