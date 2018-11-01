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
package org.botlibre.sense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.ExceptionEventListener;
import org.botlibre.api.sense.Sense;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Defines an external interface.
 * i.e.
 *	- text
 *	- voice
 *	- hearing
 *	- vision
 */

public class BasicSense implements Sense {
	public static long MINUTE = 1000L * 60L;
	public static long HOUR = 1000L * 60L * 60L;
	public static long DAY = 1000L * 60L * 60L * 24L;
	/** Number of attempt to retry sensory input on failure. */
	public static int RETRY = 3;
	
	/** Default user if none specified. */
	public static String DEFAULT_SPEAKER = "Anonymous";

	/** This default name is the class name. */
	protected String name;
	
	/** Allow the voice to be disabled. */
	protected boolean isEnabled;
	
	/**
	 * Defines the current language conversational state for the sense.
	 * This allows each sense to have its own conversational state.
	 */
	protected LanguageState languageState;
	
	/**
	 * Defines the current emotional state for the sense.
	 * This allows each sense to have its own emotional state.
	 */
	protected EmotionalState emotionalState;

	protected String action;	
	
	/** Back reference to Bot instance. **/
	protected Bot bot;

	protected List<ExceptionEventListener> listeners;
	
	public int conversations;
	public int engaged;
	protected ResponseListener responseListener;
	
	public BasicSense() {
		this.name = getClass().getName();
		this.isEnabled = true;
		this.languageState = LanguageState.Answering;
		this.emotionalState = EmotionalState.NONE;
		this.listeners = new ArrayList<ExceptionEventListener>();
	}
	
	public void notifyResponseListener() {
		if (this.responseListener != null) {
			if (this.responseListener.reply == null) {
				this.responseListener.reply = "";
			}
			synchronized (this.responseListener) {
				this.responseListener.notifyAll();
			}
		}
	}

	public ResponseListener getResponseListener() {
		return responseListener;
	}

	public void setResponseListener(ResponseListener responseListener) {
		this.responseListener = responseListener;
	}

	/**
	 * Record the engaged statistic if the conversation is engaged (over 3 messages).
	 */
	public void checkEngaged(Vertex conversation) {
		Collection<Relationship> relationships = conversation.getRelationships(Primitive.INPUT);
		if (relationships == null) {
			return;
		}
		int size = relationships.size();
		if (size == 3 || size == 4) {
			this.engaged++;
		}
	}
	
	/**
	 * Return if the sense is enabled.
	 */
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Allow the sense to disabled/enabled.
	 */
	@Override
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Return the current conversational state.
	 */
	public LanguageState getLanguageState() {
		return this.languageState;
	}

	/**
	 * Set the current conversational state.
	 */
	public void setLanguageState(LanguageState languageState) {
		this.languageState = languageState;
	}

	/**
	 * Return the current conversational mood.
	 */
	public EmotionalState getEmotionalState() {
		return this.emotionalState;
	}

	/**
	 * Set the current conversational mood.
	 */
	public void setEmotionalState(EmotionalState emotionalState) {
		if (emotionalState == null) {
			this.emotionalState = EmotionalState.NONE;
		} else {
			this.emotionalState = emotionalState;
		}
	}
	
	public String getAction() {
		return this.action;
	}

	/**
	 * Set the current action.
	 */
	public void setAction(String action) {
		if (action == null || action.isEmpty()) {
			this.action = null;
		} else {			
			this.action = action;
		}
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		getBot().log(this, "Awake", Bot.FINE);
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		
	}
	
	/**
	 * Stop sensing.
	 */
	@Override
	public void shutdown() {
		getBot().log(this, "Shutdown", Bot.FINE);
		setIsEnabled(false);
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
	}
	
	/**
	 * Receive any input from the sense.
	 */
	@Override
	public void input(Object input) {
		int attempt = 0;
		Exception failure = null;
		while (attempt < RETRY) {
			if (!isEnabled()) {
				return;
			}
			attempt++;
			try {
				Network network = getBot().memory().newMemory();
				input(input, network);
				network.save();
				return;
			} catch (BotException error) {
				log(error.toString(), Bot.WARNING);
				throw error;
			} catch (Exception failed) {
				failure = failed;
				log(failed.toString(), Bot.WARNING);
				log("Retrying", Bot.WARNING);
			}
		}
		log("Retry failed", Bot.WARNING);
		log(failure);
		notifyExceptionListeners(failure);
	}
	
	/**
	 * Receive any input from the sense.
	 */
	@Override
	public void input(Object input, Network network) throws Exception {
	}
	
	/**
	 * Output the active network to the sense.
	 */
	@Override
	public void output(Vertex output) {
	}

	/**
	 * Return the name that identifies the sense.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Set the name that identifies the sense.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Return the short term memory.
	 */
	public Network getShortTermMemory() {
		return getBot().memory().getShortTermMemory();
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level, Object... arguments) {
		getBot().log(this, message, level, arguments);
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level) {
		getBot().log(this, message, level);
	}
	
	/**
	 * Log the exception.
	 */
	public void log(Throwable error) {
		getBot().log(this, error);
	}
	
	public Primitive getPrimitive() {
		return new Primitive(getName());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Create an input based on the sentence.
	 */
	protected Vertex createInputParagraph(String text, Network network) {
		if (getBot().getFilterProfanity()) {
			if (Utils.checkProfanity(text, getBot().getContentRating())) {
				throw BotException.offensive();
			}
		}
		Utils.checkScript(text);
		Vertex sentence = network.createParagraph(text);
		if (sentence.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
			throw BotException.offensive();
		}
		if (sentence.instanceOf(Primitive.PARAGRAPH)) {
			Collection<Relationship> relationships = sentence.getRelationships(Primitive.SENTENCE);
			for (Relationship relationship : relationships) {
				if (relationship.getTarget().hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
					throw BotException.offensive();
				}
			}
		}
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		return input;
	}
	
	/**
	 * Create an input based on the sentence.
	 */
	public Vertex createInputSentence(String text, Network network) {
		if (getBot().getFilterProfanity()) {
			if (Utils.checkProfanity(text, getBot().getContentRating())) {
				throw BotException.offensive();
			}
		}
		Utils.checkScript(text);
		Vertex sentence = network.createSentence(text);
		if (sentence.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
			throw BotException.offensive();
		}
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		return input;
	}
	
	/**
	 * Create an input based on the sentence.
	 */
	protected Vertex createInputCommand(String json, Network network) {
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		if (root == null) {
			return null;
		}
		Vertex object = getBot().awareness().getSense(Http.class).convertElement(root, network);
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(json);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, object);
		return input;
	}

	/**
	 * Return the associated Bot instance.
	 */
	@Override
	public Bot getBot() {
		return bot;
	}

	/**
	 * Set the associated Bot instance.
	 */
	@Override
	public void setBot(Bot Bot) {
		this.bot = Bot;
	}
	
	/**
	 * Initialize any configurable settings from the properties.
	 */
	@Override
	public void initialize(Map<String, Object> properties) {
		return;
	}
		
	/**
	 * Attempt to discover information on the vertex.
	 */
	public boolean discover(Vertex input, Network network, Vertex currentTime) {
		Vertex sentence = input.getRelationship(Primitive.INPUT);
		if (sentence != null) {
			if (sentence.instanceOf(Primitive.PARAGRAPH)) {
				Collection<Relationship> sentences = sentence.getRelationships(Primitive.SENTENCE);
				if (sentences != null) {
					for (Relationship relationship : sentences) {
						return checkSentence(relationship.getTarget(), network, currentTime);						
					}
				}				
			} else if (sentence.instanceOf(Primitive.SENTENCE)) {
				return checkSentence(sentence, network, currentTime);
			}
		}
		return false;
	}
		
	/**
	 * Check if the sentence has been discovered.
	 */
	public boolean checkSentence(Vertex sentence, Network network, Vertex currentTime) {
		Vertex lastChecked = sentence.getRelationship(getPrimitive());
		if (lastChecked == null) {
			log("Discovering sentence:", Bot.FINE, sentence);
			discoverSentence(sentence, network, currentTime);
			sentence.addRelationship(getPrimitive(), currentTime);
			return true;
		}
		return false;
	}
	
	/**
	 * Attempt to discover information on the sentence words.
	 */
	public void discoverSentence(Vertex sentence, Network network, Vertex currentTime) {
		
	}
	
	/**
	 * Convert the input into text.
	 */
	public String printInput(Vertex input) {
		Vertex sentence = input.getRelationship(Primitive.INPUT);
		if (sentence != null) {
			return sentence.printString();
		}
		return "";
	}

	public synchronized void notifyExceptionListeners(Throwable exception) {
		for (ExceptionEventListener listener : getListeners()) {
			listener.notify(exception);
		}
	}

	public List<ExceptionEventListener> getListeners() {
		return listeners;
	}

	protected void setListeners(List<ExceptionEventListener> listeners) {
		this.listeners = listeners;
	}
	
	/**
	 * Add the exception listener.
	 * It will be notified of any exceptions.
	 */
	public synchronized void addListener(ExceptionEventListener listener) {
		if (!this.listeners.contains(listener)) {
			this.listeners.add(listener);
		}
	}
	
	public synchronized void removeListener(ExceptionEventListener listener) {
		this.listeners.remove(listener);
	}
	
	public void saveProperties() {
		
	}
}