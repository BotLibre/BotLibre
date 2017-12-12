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

import java.util.Map;

import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;

/**
 * Defines a emotion.
 * An emotion is a state defined by a magnitude that affects behavior.
 */

public interface Emotion {
	float getState();
	
	void setState(float state);

	Primitive primitive();
	
	String getName();

	EmotionalState evaluate(float level);
	
	/**
	 * Initialize any configuration properties.
	 */
	void initialize(Map<String, Object> properties);
}

