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

import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;

/**
 * A convenience enum of different emotional states.
 */

public enum EmotionalState {
	NONE,
	LOVE, LIKE, DISLIKE, HATE,
	RAGE, ANGER, CALM, SERENE,
	ECSTATIC, HAPPY, SAD, CRYING,
	PANIC, AFRAID, CONFIDENT, COURAGEOUS,
	SURPRISE, BORED,
	LAUGHTER, SERIOUS;
	
	public Primitive primitive() {
		return new Primitive(name().toLowerCase());
	}
	
	/**
	 * Apply the emotion to the vertex.
	 * Associate to the relevant emotion.
	 */
	public void apply(Vertex vertex) {
		
		if (this == EmotionalState.LOVE) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.LOVE);
			vertex.addRelationship(Primitive.EMOTION, Primitive.LOVE);			
		} else if (this == EmotionalState.LIKE) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.LOVE);			
		} else if (this == EmotionalState.DISLIKE) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.LOVE);
		} else if (this == EmotionalState.HATE) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.LOVE);
			vertex.removeRelationship(Primitive.EMOTION, Primitive.LOVE);			
		}
		
		else if (this == EmotionalState.RAGE) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.ANGER);
			vertex.addRelationship(Primitive.EMOTION, Primitive.ANGER);
		} else if (this == EmotionalState.ANGER) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.ANGER);
		} else if (this == EmotionalState.CALM) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.ANGER);
		} else if (this == EmotionalState.SERENE) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.ANGER);
			vertex.removeRelationship(Primitive.EMOTION, Primitive.ANGER);
		}
		
		else if (this == EmotionalState.ECSTATIC) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.HAPPINESS);
			vertex.addRelationship(Primitive.EMOTION, Primitive.HAPPINESS);
		} else if (this == EmotionalState.HAPPY) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.HAPPINESS);
		} else if (this == EmotionalState.SAD) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.HAPPINESS);
		} else if (this == EmotionalState.CRYING) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.HAPPINESS);
			vertex.removeRelationship(Primitive.EMOTION, Primitive.HAPPINESS);
		}
		
		else if (this == EmotionalState.PANIC) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.FEAR);
			vertex.addRelationship(Primitive.EMOTION, Primitive.FEAR);
		} else if (this == EmotionalState.AFRAID) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.FEAR);
		} else if (this == EmotionalState.CONFIDENT) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.FEAR);
		} else if (this == EmotionalState.COURAGEOUS) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.FEAR);
			vertex.removeRelationship(Primitive.EMOTION, Primitive.FEAR);
		}
		
		else if (this == EmotionalState.SURPRISE) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.SURPRISE);
		} else if (this == EmotionalState.BORED) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.SURPRISE);
		}
		
		else if (this == EmotionalState.LAUGHTER) {
			vertex.addRelationship(Primitive.EMOTION, Primitive.HUMOR);
		} else if (this == EmotionalState.SERIOUS) {
			vertex.removeRelationship(Primitive.EMOTION, Primitive.HUMOR);
		}
	}
}

