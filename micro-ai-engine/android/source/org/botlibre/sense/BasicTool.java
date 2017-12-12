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
package org.botlibre.sense;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.sense.ExceptionEventListener;
import org.botlibre.api.sense.Tool;
import org.botlibre.knowledge.Primitive;

/**
 * Defines an external interface.
 * i.e.
 *	- calculator
 *	- watch
 */

public class BasicTool implements Tool {

	/** This default name is the class name. */
	protected String name;
	
	/** Allow the voice to be disabled. */
	protected boolean isEnabled;
	
	/** Back reference to Bot instance. **/
	protected Bot bot;

	protected List<ExceptionEventListener> listeners;
	
	public BasicTool() {
		this.name = getClass().getName();
		this.isEnabled = true;
		this.listeners = new ArrayList<ExceptionEventListener>();
	}

	/**
	 * Return if the sense is enabled.
	 */
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Allow the sense to disabled/enabled.
	 */
	@Override
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		getBot().log(this, "Awake", Bot.FINE);
	}
	
	/**
	 * Stop sensing.
	 */
	@Override
	public void shutdown() {
		getBot().log(this, "Shutdown", Bot.FINE);
		setIsEnabled(false);
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
	}

	/**
	 * Return the name that identifies the sense.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name that identifies the sense.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the short term memory.
	 */
	public Network getShortTermMemory() {
		return getBot().memory().getShortTermMemory();
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level, Object... arguments) {
		getBot().log(this, message, level, arguments);
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level) {
		getBot().log(this, message, level);
	}
	
	/**
	 * Log the exception.
	 */
	public void log(Throwable error) {
		getBot().log(this, error);
	}
	
	public Primitive getPrimitive() {
		return new Primitive(getName());
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

	/**
	 * Set the associated Bot instance.
	 */
	@Override
	public void setBot(Bot Bot) {
		this.bot = Bot;
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		return;
	}

	public void notifyExceptionListeners(Exception exception) {
		for (ExceptionEventListener listener : getListeners()) {
			listener.notify(exception);
		}
	}

	public List<ExceptionEventListener> getListeners() {
		return listeners;
	}

	protected void setListeners(List<ExceptionEventListener> listeners) {
		this.listeners = listeners;
	}
	
	/**
	 * Add the exception listener.
	 * It will be notified of any exceptions.
	 */
	public void addListener(ExceptionEventListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}
	
	public void removeListener(ExceptionEventListener listener) {
		this.listeners.remove(listener);
	}
}