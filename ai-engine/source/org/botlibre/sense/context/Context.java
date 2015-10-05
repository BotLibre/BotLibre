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
package org.botlibre.sense.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;

/**
 * Allows a vertices to be explicitly selected.
 * Allows thought to associate the current context with the selection.
 * Similar to vision or touch focusing on an object.
 */

public class Context extends BasicSense {
	public static int CONTEXT_SIZE = 16;
	
	/** Allows notification of the stack top. */
	protected Selector selector;
	
	/** Keeps track of objects in the current context, for language and association. */
	protected List<Long> contextStack = new ArrayList<Long>();

	public Context() {
	}

	public List<Long> getContextStack() {
		return this.contextStack;
	}

	/**
	 * Return the top of the context stack.
	 */
	public Vertex top(Network network) {
		if (this.contextStack.isEmpty()) {
			return null;
		}
		return network.findById(this.contextStack.get(this.contextStack.size() - 1));
	}

	/**
	 * Return the top of the context stack.
	 */
	public Vertex top(Vertex vertex) {
		if (!vertex.hasData() || !(vertex.getData() instanceof Number)) {
			return null;
		}
		int index = ((Number)vertex.getData()).intValue();
		if (this.contextStack.size() < index) {
			return null;
		}
		return vertex.getNetwork().findById(this.contextStack.get(this.contextStack.size() - index));
	}

	/**
	 * Clear the current context.
	 */
	public void clear() {
		this.contextStack.clear();
	}

	/**
	 * Return the first element on the stack matching the variable.
	 */
	public Vertex search(Vertex variable) {
		for (int index = this.contextStack.size() - 1; index >= 0; index--) {
			Vertex context = variable.getNetwork().findById(this.contextStack.get(index));
			if ((context != null) && (!context.hasRelationship(Primitive.VARIABLE)) && (variable.matches(context, new HashMap<Vertex, Vertex>()) == Boolean.TRUE)) {
				log("search", Level.FINE, context);
				return context;
			}
		}
		return null;
	}

	/**
	 * Add the vertex to the context.
	 */
	public void push(Vertex vertex) {
		log("push", Level.FINE, vertex);
		this.contextStack.add(vertex.getId());
		while (this.contextStack.size() > CONTEXT_SIZE) {
			this.contextStack.remove(0);
		}
	}
	
	/**
	 * Return the selector used to access the current selection.
	 */
	public Selector getSelector() {
		return this.selector;
	}

	/**
	 * Set the selector used to access the current selection.
	 */
	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	/**
	 * Process the input.
	 * Add the vertex to active memory
	 */
	public void input(Object input) {
		if (!isEnabled()) {
			return;
		}
		log("Input", Bot.FINE, input);
		push(getBot().memory().addActiveMemory((Vertex)input));
	}
	
	/**
	 * Set the vertex as the current selection.
	 */
	public void output(Vertex output) {
		if (!isEnabled() || (getSelector() == null)) {
			return;
		}
		getSelector().setSelection(output);
	}
	
	public String toString() {
		return getClass().getSimpleName() + "(" + getContextStack() + ")";
	}
	
}