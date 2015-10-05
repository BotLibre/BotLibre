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

/**
 * Controls and manages the thought processing.
 */

public interface Mind {
	public enum MentalState {
		/**
		 * UNCONSCIOUS is the dream state, the conscious thought is free to wander.
		 */
		UNCONSCIOUS,
		/**
		 * ASLEEP is the dream state, the conscious thought is free to wander.
		 */
		ASLEEP,
		/**
		 * BORED, some activity but conscious thought is free to wander.
		 */
		BORED,
		/**
		 * ACTIVE, active interacting, or performing a task.
		 */
		ACTIVE,
		/**
		 * ALERT, highly active, large amount of sensory input.
		 */
		ALERT,
		/**
		 * PANIC sensory overload, fight, flight or cry.
		 */
		PANIC
	}
		
	void shutdown();
	
	void awake();
	
	/**
	 * Reset state when instance is pooled.
	 */
	void pool();
	
	/**
	 * Return the state of mind.
	 */
	MentalState getState();
	
	/**
	 * Return if in an active state.
	 */
	boolean isActive();
	
	/**
	 * Return if in an conscious state.
	 */
	boolean isConscious();
	
	/**
	 * Return if in an sleep state.
	 */
	boolean isAsleep();
	
	/**
	 * Return if in an bored state.
	 */
	boolean isBored();
	
	Map<String, Thought> getThoughts();

	<T> T getThought(Class<T> type);
	
	Thought getThought(String name);
	
	void addThought(Thought thought);
	
	void removeThought(Thought thought);

	Bot getBot();
	
	void setBot(Bot Bot);
	
	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);

}

