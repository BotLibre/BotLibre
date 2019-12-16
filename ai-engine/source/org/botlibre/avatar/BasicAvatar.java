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
import org.botlibre.knowledge.BinaryData;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.thought.language.Language;

/**
 * Controls and manages the thought processing.
 */

public class BasicAvatar implements Avatar {
	
	protected Bot bot;
	
	protected String action;
	protected String pose;
	protected String emote;
	protected String command;
			
	public BasicAvatar() {
	}

	@Override
	public void pool() {
		this.action = null;
		this.pose = null;
		this.emote = null;
		this.command = null;
	}

	@Override
	public void reset() {
		this.action = null;
		this.emote = null;
		this.command = null;
	}

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public void setCommand(String command) {
		this.command = command;
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
		}
		Vertex command = output.getRelationship(Primitive.COMMAND);
		if (command != null) {
			setCommand(command.printString());
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
	public void evaluateResponse(Vertex output, Vertex response, Vertex meta, Map<Vertex, Vertex> variables, Network network) {
		Collection<Relationship> actions = null;
		if (meta != null) {
			actions = meta.getRelationships(Primitive.ACTION);
		}
		if (actions == null) {
			actions = response.getRelationships(Primitive.ACTION);
		}
		if (actions != null && !actions.isEmpty()) {
			for (Relationship relationship : actions) {
				output.addWeakRelationship(Primitive.ACTION, relationship.getTarget(), relationship.getCorrectness());
			}
		} else if (getAction() != null && !getAction().isEmpty()) {
			output.addRelationship(Primitive.ACTION, network.createVertex(getAction()));
		}
		Collection<Relationship> poses = null;
		if (meta != null) {
			poses = meta.getRelationships(Primitive.POSE);
		}
		if (poses == null) {
			poses = response.getRelationships(Primitive.POSE);
		}
		if (poses != null && !poses.isEmpty()) {
			for (Relationship relationship : poses) {
				output.addWeakRelationship(Primitive.POSE, relationship.getTarget(), relationship.getCorrectness());
			}
		} else if (getPose() != null && !getPose().isEmpty()) {
			output.addRelationship(Primitive.POSE, network.createVertex(getPose()));
		}
		if (meta != null) {
			Vertex command = meta.getRelationship(Primitive.COMMAND);
			if (command != null && command.instanceOf(Primitive.FORMULA)) {
				log("Evaluating command", Level.FINE, command);
				Vertex data = getBot().mind().getThought(Language.class).evaluateFormula(command, variables, network);
				if (data != null) {
					output.addRelationship(Primitive.COMMAND, data);
				}
			} else if (command != null) {
				output.addRelationship(Primitive.COMMAND, command);
			}
		} else if (getCommand() != null && !getCommand().isEmpty()) {
			output.addRelationship(Primitive.COMMAND, network.createVertex(getCommand()));
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
	public BinaryData getCurrentImage() {
		return null;
	}
	
	/**
	 * Print a useful string representation of the avatar.  
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() + "()";
	}

	/**
	 * Self API to set avatar action.
	 */
	public Vertex setAction(Vertex source, Vertex action) {
		setAction(action.printString());
		return source;
	}

	/**
	 * Self API to get avatar pose.
	 */
	public Vertex getPose(Vertex source) {
		if (getPose() == null) {
			return source.getNetwork().createVertex(Primitive.NULL);
		}
		return source.getNetwork().createVertex(getPose());
	}

	/**
	 * Self API to set avatar pose.
	 */
	public Vertex setPose(Vertex source, Vertex pose) {
		setPose(pose.printString());
		return source;
	}

	/**
	 * Self API to set avatar command.
	 */
	public Vertex setCommand(Vertex source, Vertex command) {
		Network network = source.getNetwork();
		Http http = network.getBot().awareness().getSense(Http.class);
		setCommand(http.toJSON(source, command).printString());
		return source;
	}
}

