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
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.avatar.Avatar;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.ImageData;
import org.botlibre.knowledge.Primitive;

/**
 * Controls and manages the thought processing.
 */

public class BasicAvatar implements Avatar {
	
	protected Bot bot;
	
	protected String action;
	protected String pose;
	protected String emote;
			
	public BasicAvatar() {
	}

	@Override
	public void pool() {
		this.action = null;
		this.pose = null;
		this.emote = null;
	}

	@Override
	public String getAction() {
		return action;
	}

	@Override
	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String getPose() {
		return pose;
	}

	@Override
	public void setPose(String pose) {
		this.pose = pose;
	}

	@Override
	public String getEmote() {
		return emote;
	}

	@Override
	public void setEmote(String emote) {
		this.emote = emote;
	}

	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level, Object... arguments) {
		this.bot.log(this, message, level, arguments);
	}
	
	/**
	 * Log the exception.
	 */
	public void log(Throwable exception) {
		this.bot.log(this, exception);
	}
	
	/**
	 * Return Bot.
	 */
	@Override
	public Bot getBot() {
		return bot;
	}

	@Override
	public Primitive getPrimitive() {
		return Primitive.AVATAR;
	}
	
	/**
	 * Set Bot.
	 */
	@Override
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

	@Override
	public void shutdown() {
		this.bot.log(this, "Shutdown", Bot.FINE);
		pool();
	}
	
	@Override
	public void awake() {
		this.bot.log(this, "Awake", Bot.FINE);
	}
	
	/**
	 * Evaluate the output for emotional expression.
	 */
	@Override
	public void evaluateOutput(Vertex output) {
		Vertex action = output.mostConscious(Primitive.ACTION);
		if (action != null && action.isPrimitive()) {
			setAction(action.getDataValue());
		} else {
			setAction(null);
		}
		Vertex pose = output.mostConscious(Primitive.POSE);
		if (pose != null && pose.isPrimitive()) {
			if (pose.getDataValue().equals("default")) {
				setPose(null);
			} else {
				setPose(pose.getDataValue());
			}
		}
		EmotionalState emote = this.bot.mood().evaluateEmotionalState(output);
		setEmote(emote.name());
		emote(emote, output.getNetwork());
	}
	
	/**
	 * Evaluate the input response for actions and poses.
	 */
	@Override
	public void evaluateResponse(Vertex output, Vertex response, Vertex meta) {
		Collection<Relationship> actions = null;
		if (meta != null) {
			actions = meta.getRelationships(Primitive.ACTION);
		}
		if (actions == null) {
			actions = response.getRelationships(Primitive.ACTION);
		}
		if (actions != null) {
			for (Relationship relationship : actions) {
				output.addWeakRelationship(Primitive.ACTION, relationship.getTarget(), relationship.getCorrectness());
			}
		}
		Collection<Relationship> poses = null;
		if (meta != null) {
			poses = meta.getRelationships(Primitive.POSE);
		}
		if (poses == null) {
			poses = response.getRelationships(Primitive.POSE);
		}
		if (poses != null) {
			for (Relationship relationship : poses) {
				output.addWeakRelationship(Primitive.POSE, relationship.getTarget(), relationship.getCorrectness());
			}
		}
	}

	public Long clearAvatarProperty() {
		Long id = null;
		Network memory = getBot().memory().newMemory();
		Vertex avatar = memory.createVertex(getPrimitive());
		
		Vertex property = avatar.getRelationship(Primitive.ID);
		if (property != null) {
			id = (Long)property.getData();
			property.setPinned(false);
		}
		
		avatar.internalRemoveRelationships(Primitive.ID);
		
		memory.save();
		return id;
	}
	
	/**
	 * Output the emotional state to the Avatar.
	 */
	public void emote(EmotionalState state, Network memory) {
		
	}

	@Override
	public ImageData getCurrentImage() {
		return null;
	}
	
	/**
	 * Print a useful string representation of the avatar.  
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "()";
	}

}

