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
package org.botlibre.thought;

import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.thought.Thought;
import org.botlibre.knowledge.Primitive;
/**
 * Something that given a network of objects can perform some function.
 */

public class BasicThought implements Thought {

	private String name;
	
	/** Back reference to Bot instance. **/
	protected Bot bot;
	
	protected boolean isStopped = false;
	protected boolean isEnabled = true;
	
	/**
	 * Create a new thought.
	 */
	public BasicThought() {
		this.name = getClass().getName();
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
	}

	/**
	 * Return the name that identifies the thought.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name that identifies the thought.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Stop analysing network.
	 */
	@Override
	public void stop() {
		this.isStopped = true;
	}
	
	/**
	 * Analyse and extend the network.
	 */
	@Override
	public void think() {}
	
	/**
	 * Perform any initialization required on startup.
	 */
	@Override
	public void awake() {
		this.bot.log(this, "Awake", Bot.FINE);
	}
	
	/**
	 * Return the short term memory.
	 */
	public Network getShortTermMemory() {
		return this.bot.memory().getShortTermMemory();
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level, Object... arguments) {
		this.bot.log(this, message, level, arguments);
	}
	
	/**
	 * Log the exception.
	 */
	public void log(Throwable exception) {
		this.bot.log(this, exception);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Return the associated Bot instance.
	 */
	@Override
	public Bot getBot() {
		return bot;
	}

	@Override
	public Primitive getPrimitive() {
		return new Primitive(getName());
	}
	
	/**
	 * Set the associated Bot instance.
	 */
	@Override
	public void setBot(Bot bot) {
		this.bot = bot;
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		return;
	}
	
	/**
	 * Thoughts can be conscious or sub-conscious.
	 * A conscious thought is run by the mind single threaded with exclusive access to the short term memory.
	 * A sub-conscious thought is run concurrently, and must run in its own memory space.
	 */
	@Override
	public boolean isConscious() {
		return true;
	}

	/**
	 * Return if this thought must run even under stress.
	 */
	@Override
	public boolean isCritical() {
		return false;
	}

	public boolean isStopped() {
		return isStopped;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}

	public void saveProperties() {
		
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		
	}
}