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
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.language.Language.LanguageState;

/**
 * Defines an external interface.
 * i.e.
 *	- text
 *	- voice
 *	- hearing
 *	- vision
 */

public interface Sense {

	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);
	
	/**
	 * Start sensing.
	 */
	void awake();

	/**
	 * Stop sensing.
	 */
	void shutdown();
	
	/**
	 * Reset state when instance is pooled.
	 */
	void pool();
	
	/**
	 * Receive any input from the sense.  The type of input varies by sense, could be text, sound, image, html etc.
	 */
	void input(Object inputData);
	
	/**
	 * Receive any input from the sense.  The type of input varies by sense, could be text, sound, image, html etc.
	 */
	void input(Object inputData, Network network) throws Exception;
	
	/**
	 * Output the active network through the sense, could be text, sound, image, commands etc..
	 */
	void output(Vertex output);
	
	/**
	 * Return the name that identifies the sense.
	 */
	String getName();

	/**
	 * Set the name that identifies the sense.
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
	 * Return the current conversational state.
	 */
	LanguageState getLanguageState();

	/**
	 * Set the current conversational state.
	 */
	void setLanguageState(LanguageState languageState);

	/**
	 * Set the current conversational mood.
	 */
	void setEmotionalState(EmotionalState emotion);

	/**
	 * Set the current action.
	 */
	void setAction(String action);

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

	void notifyExceptionListeners(Throwable exception);

	/**
	 * Migrate to new properties system.
	 */
	void migrateProperties();

	void saveProperties();
}