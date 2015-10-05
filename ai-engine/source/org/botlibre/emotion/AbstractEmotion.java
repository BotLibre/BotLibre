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
package org.botlibre.emotion;

import java.util.Map;

import org.botlibre.api.emotion.Emotion;
import org.botlibre.knowledge.Primitive;

/**
 * Defines a emotion.
 * An emotion is a state defined by a magnitude that affects behavior.
 */

public abstract class AbstractEmotion implements Emotion {
	protected float state;

	@Override
	public float getState() {
		return state;
	}

	@Override
	public void setState(float state) {
		this.state = state;
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public Primitive primitive() {
		return new Primitive(getName());
	}
	
	@Override
	public EmotionalState evaluate(float level) {
		return EmotionalState.NONE;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + state + ")";
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		return;
	}
}

