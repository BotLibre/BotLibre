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
package org.botlibre.api.emotion;

import java.util.List;
import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;

/**
 * Defines and manages the emotion state.
 * Contains a set of Emotions.
 */

public interface Mood {
		
	void shutdown();
	
	void awake();
	
	void pool();
	
	Map<String, Emotion> getEmotions();

	<T> T getEmotion(Class<T> type);
	
	Emotion getEmotion(String name);
	
	void addEmotion(Emotion emotion);
	
	void removeEmotion(Emotion emotion);

	Bot getBot();
	
	void setBot(Bot Bot);

	/**
	 * Evaluate the active memory input for emotional influence.
	 */
	void evaluate();

	/**
	 * Evaluate the output and express emotion.
	 */
	void evaluateOutput(Vertex output);
	
	/**
	 * Evaluate the input response for emotional influence.
	 */
	void evaluateResponse(Vertex response, Vertex meta);

	/**
	 * Determine the current main emotional state.
	 */
	EmotionalState currentEmotionalState();
	
	/**
	 * Determine the main emotional state of the output.
	 */
	EmotionalState evaluateEmotionalState(Vertex output);
	
	/**
	 * Determine the emotional states of the output.
	 */
	List<EmotionalState> evaluateEmotionalStates(Vertex output);
	
	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);
	
	boolean isEnabled();
	
	void setEnabled(boolean isEnabled);

	void saveProperties();

}

