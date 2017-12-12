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
import org.botlibre.api.knowledge.Vertex;

/**
 * Controls and manages the senses.
 */

public interface Awareness {

	void shutdown();
	
	void awake();
	
	/**
	 * Reset state when instance is pooled.
	 */
	void pool();
	
	Map<String, Sense> getSenses();

	<T> T getSense(Class<T> type);
	
	/**
	 * Return the sense with the name.
	 */
	Sense getSense(String name);
	
	void addSense(Sense sense);
	
	void removeSense(Sense sense);
	
	Map<String, Tool> getTools();

	<T> T getTool(Class<T> type);
	
	/**
	 * Return the tool with the name.
	 */
	Tool getTool(String name);
	
	void addTool(Tool sense);
	
	void removeTool(Tool sense);

	Bot getBot();
	
	void setBot(Bot Bot);

	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);

	/**
	 * Allow the sense to output the response.
	 */
	void output(Vertex output);
}

