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

/**
 * Happiness represents the emotions from happy to sad.
 */

public class Happiness extends AbstractEmotion {
	
	@Override
	public EmotionalState evaluate(float level) {
		if (level < -0.6) {
			return EmotionalState.CRYING;
		} else if (level < -0.1) {
			return EmotionalState.SAD;
		} else if (level > 0.6) {
			return EmotionalState.ECSTATIC;
		} else if (level > 0.1) {
			return EmotionalState.HAPPY;
		}
		return EmotionalState.NONE;
	}

	
}

