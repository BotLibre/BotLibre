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

import java.util.HashMap;
import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Awareness;
import org.botlibre.api.sense.Sense;
import org.botlibre.api.sense.Tool;

/**
 * Controls and manages the senses.
 */

public class BasicAwareness implements Awareness {
	
	protected Bot bot;
	protected Map<String, Sense> senses;
	protected Map<String, Sense> sensesByShortName;
	
	protected Map<String, Tool> tools;
	protected Map<String, Tool> toolsByShortName;

	public BasicAwareness() {
		this.senses = new HashMap<String, Sense>();
		this.sensesByShortName = new HashMap<String, Sense>();
		this.tools = new HashMap<String, Tool>();
		this.toolsByShortName = new HashMap<String, Tool>();
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
	@Override
	public void initialize(Map<String, Object> properties) {
		return;
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
		getBot().log(this, "Pool", Bot.FINE);
		for (Sense sense : getSenses().values()) {
			try {
				sense.pool();
			} catch (Exception exception) {
				getBot().log(this, exception);
			}
		}
		for (Tool tool : getTools().values()) {
			try {
				tool.pool();
			} catch (Exception exception) {
				getBot().log(this, exception);
			}
		}
	}

	@Override
	public void shutdown() {
		getBot().log(this, "Shutdown", Bot.FINE);
		for (Sense sense : getSenses().values()) {
			try {
				sense.shutdown();
			} catch (Exception exception) {
				getBot().log(this, exception);
			}
		}
		for (Tool tool : getTools().values()) {
			try {
				tool.shutdown();
			} catch (Exception exception) {
				getBot().log(this, exception);
			}
		}
	}

	@Override
	public void awake() {
		getBot().log(this, "Awake", Bot.FINE);
		for (Sense sense : getSenses().values()) {
			try {
				sense.awake();
			} catch (Exception exception) {
				getBot().log(sense, exception);
			}
		}
		for (Tool tool : getTools().values()) {
			try {
				tool.awake();
			} catch (Exception exception) {
				getBot().log(tool, exception);
			}
		}
	}
	
	public Map<String, Sense> getSenses() {
		return senses;
	}

	@SuppressWarnings("unchecked")
	public <T> T getSense(Class<T> type) {
		return (T)getSense(type.getName());
	}
	
	public Sense getSense(String senseName) {
		Sense sense = getSenses().get(senseName);
		if (sense == null) {
			sense = this.sensesByShortName.get(senseName);
		}
		return sense;
	}
	
	public void addSense(Sense sense) {
		sense.setBot(getBot());
		getSenses().put(sense.getName(), sense);
		// Also index simple name.
		this.sensesByShortName.put(sense.getClass().getSimpleName(), sense);
	}
	
	public void removeSense(Sense sense) {
		getSenses().remove(sense.getName());
		// Also index simple name.
		this.sensesByShortName.remove(sense.getClass().getSimpleName());
	}
	
	public Map<String, Tool> getTools() {
		return tools;
	}

	@SuppressWarnings("unchecked")
	public <T> T getTool(Class<T> type) {
		return (T)getTool(type.getName());
	}
	
	public Tool getTool(String name) {
		Tool tool = getTools().get(name);
		if (tool == null) {
			tool = this.toolsByShortName.get(name);
		}
		return tool;
	}
	
	public void addTool(Tool tool) {
		tool.setBot(getBot());
		getTools().put(tool.getName(), tool);
		// Also index simple name.
		this.toolsByShortName.put(tool.getClass().getSimpleName(), tool);
	}
	
	public void removeTool(Tool tool) {
		getTools().remove(tool.getName());
		// Also index simple name.
		this.toolsByShortName.remove(tool.getClass().getSimpleName());
	}
	
	public String toString() {
		return getClass().getSimpleName();
	}

	/**
	 * Allow the sense to output the response.
	 */
	public void output(Vertex output) {
		getBot().mood().evaluateOutput(output);
		getBot().avatar().evaluateOutput(output);
		for (Sense sense : getSenses().values()) {
			try {
				sense.output(output);
			} catch (Throwable ignore) {
				getBot().log(this, ignore);
			}
		}
	}
}

