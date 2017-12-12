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
package org.botlibre.api.sense;

import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.knowledge.Primitive;

/**
 * Defines an external interface.
 * i.e.
 *	- calculator
 *  - watch
 */

public interface Tool {

	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);
	
	/**
	 * Start.
	 */
	void awake();

	/**
	 * Stop.
	 */
	void shutdown();
	
	/**
	 * Reset state when instance is pooled.
	 */
	void pool();
	
	/**
	 * Return the name that identifies the tool.
	 */
	String getName();

	/**
	 * Set the name that identifies the tool.
	 */
	void setName(String name);

	/**
	 * Return the associated Bot instance.
	 */
	Bot getBot();

	/**
	 * Set the associated Bot instance.
	 */
	void setBot(Bot Bot);

	/**
	 * Return if the sense is enabled.
	 */
	boolean isEnabled();

	/**
	 * Allow the sense to disabled/enabled.
	 */
	void setIsEnabled(boolean isEnabled);

	/**
	 * Allows senses to be referenced by memory.
	 */
	Primitive getPrimitive();

	void notifyExceptionListeners(Exception exception);
}