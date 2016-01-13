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
package org.botlibre.avatar;

import java.util.Collection;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.util.Utils;

/**
 * Controls and manages the thought processing.
 */

public class ImageAvatar extends BasicAvatar {
	public static int MAX_UPLOAD_SIZE = 1000000; // 1meg
	
	protected BinaryData currentImage;
			
	public ImageAvatar() {
	}

	@Override
	public void awake() {
		getBot().log(this, "Loading images", Bot.FINE);
		Network memory = getBot().memory().newMemory();
		Vertex avatar = memory.createVertex(Primitive.AVATAR);
		Vertex image = avatar.mostConscious(new Primitive(EmotionalState.NONE.name().toLowerCase()));
		if ((image != null) && (image.getData() instanceof BinaryData)) {
			this.currentImage = (BinaryData)memory.findData((BinaryData)image.getData());
		}
	}
	
	/**
	 * Output the emotional state to the Avatar.
	 */
	@Override
	public void emote(EmotionalState state, Network memory) {
		getBot().log(this, "Emote", Bot.FINE, state);
		Vertex avatar = memory.createVertex(Primitive.AVATAR);
		Collection<Relationship> images = avatar.getRelationships(state.primitive());
		Vertex image = null;
		if (images != null) {
			image = Utils.random(images).getTarget();
		} else {
			getBot().log(this, "Missing image for emotion", Bot.FINE, state);
			images = avatar.getRelationships(EmotionalState.NONE.primitive());
			if (images != null) {
				image = Utils.random(images).getTarget();
			}
		}
		if ((image != null) && (image.getData() instanceof BinaryData)) {
			this.currentImage = (BinaryData)memory.findData((BinaryData)image.getData());
		}
	}
	
	@Override
	public BinaryData getCurrentImage() {
		return currentImage;
	}

	public void setCurrentImage(BinaryData currentImage) {
		this.currentImage = currentImage;
	}

}

