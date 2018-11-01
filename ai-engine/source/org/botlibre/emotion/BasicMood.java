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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.emotion.Emotion;
import org.botlibre.api.emotion.Mood;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.language.Language;

/**
 * Controls and manages the thought processing.
 */

public class BasicMood implements Mood {
	
	protected Bot bot;
	protected boolean isEnabled = true;

	/**
	 * Map of emotions.
	 */
	protected Map<String, Emotion> emotions;
			
	public BasicMood() {
		this.emotions = new HashMap<String, Emotion>();
	}
	
	public void pool() {
		for (Emotion emotion : this.emotions.values()) {
			emotion.setState(0.0f);
		}
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Mood.enabled", String.valueOf(isEnabled()), true);
		memory.save();
	}
	
	/**
	 * Log the message if the debug level is greater or equal to the level.
	 */
	public void log(String message, Level level, Object... arguments) {
		getBot().log(this, message, level, arguments);
	}
	
	/**
	 * Log the exception.
	 */
	public void log(Throwable exception) {
		getBot().log(this, exception);
	}
	
	/**
	 * Return Bot.
	 */
	@Override
	public Bot getBot() {
		return bot;
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
		getBot().log(this, "Shutdown", Bot.FINE);
	}

	@Override
	public void awake() {
		getBot().log(this, "Awake", Bot.FINE);
		String enabled = this.bot.memory().getProperty("Mood.enabled");
		if (enabled != null) {
			setEnabled(Boolean.valueOf(enabled));
		}
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex mood = memory.createVertex(getClass());
		Vertex property = mood.getRelationship(Primitive.ENABLED);
		if (property != null) {
			setEnabled((Boolean)property.getData());
		}
		
		// Remove old properties.
		mood.internalRemoveRelationships(Primitive.ENABLED);
		
		memory.save();
		
		saveProperties();
	}
	
	@Override
	public Map<String, Emotion> getEmotions() {
		return emotions;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getEmotion(Class<T> type) {
		return (T)getEmotions().get(type.getName());
	}

	@Override
	public Emotion getEmotion(String name) {
		return getEmotions().get(name);
	}

	@Override
	public void addEmotion(Emotion emotion) {
		getEmotions().put(emotion.getName(), emotion);
	}

	@Override
	public void removeEmotion(Emotion emotion) {
		getEmotions().remove(emotion.getName());
	}
	
	/**
	 * Evaluate the active memory input for emotional influence.
	 */
	@Override
	public void evaluate() {
		if (!isEnabled()) {
			return;
		}
		Network network = getBot().memory().getShortTermMemory();
		List<Vertex> activeMemory = getBot().memory().getActiveMemory();
		Iterator<Vertex> inputs = activeMemory.iterator();
		while (inputs.hasNext()) {
			// Must register into the current memory context.
			Vertex input = network.createVertex(inputs.next());
			Vertex source = input;
			if (input.instanceOf(Primitive.INPUT)) {
				// Let emotions simmer.
				for (Emotion emotion : getEmotions().values()) {
					emotion.setState(emotion.getState() / 2);
				}
				Collection<Relationship> emotions = input.getRelationships(Primitive.EMOTION);
				Vertex sentence = input.getRelationship(Primitive.INPUT);
				if ((emotions == null) && (sentence != null)) {
					// Attempt to determine emotion from sentence.
					emotions = sentence.getRelationships(Primitive.EMOTION);
					if (emotions != null) {
						source = sentence;
						for (Relationship emotionRelation : emotions) {
							Relationship relationship = input.addRelationship(Primitive.EMOTION, emotionRelation.getTarget());
							relationship.setCorrectness(emotionRelation.getCorrectness());
						}
					} else {
						// Attempt to determine from words.
						List<Relationship> words = sentence.orderedRelationships(Primitive.WORD);
						if (words != null) {
							for (Relationship word : words) {
								emotions = word.getTarget().getRelationships(Primitive.EMOTION);
								if (emotions != null) {
									for (Relationship emotionRelation : emotions) {
										Relationship relationship = input.getRelationship(Primitive.EMOTION, emotionRelation.getTarget());
										if (relationship == null) {
											relationship = input.addRelationship(Primitive.EMOTION, emotionRelation.getTarget());
											relationship.setCorrectness(emotionRelation.getCorrectness());
										} else {
											relationship.setCorrectness((relationship.getCorrectness() + emotionRelation.getCorrectness()) / 2);												
										}
									}
								}
							}
							// Check for compound word emotions as well.
							List<Vertex> compoundWords = Language.processCompoundWords(words);
							if (compoundWords != null) {
								for (Vertex word : compoundWords) {
									if (word.instanceOf(Primitive.COMPOUND_WORD)) {
										emotions = word.getRelationships(Primitive.EMOTION);
										if (emotions != null) {
											for (Relationship emotionRelation : emotions) {
												Relationship relationship = input.getRelationship(Primitive.EMOTION, emotionRelation.getTarget());
												if (relationship == null) {
													relationship = input.addRelationship(Primitive.EMOTION, emotionRelation.getTarget());
													relationship.setCorrectness(emotionRelation.getCorrectness());
												} else {
													relationship.setCorrectness((relationship.getCorrectness() + emotionRelation.getCorrectness()) / 2);												
												}
											}
										}
									}
								}
							}
							emotions = input.getRelationships(Primitive.EMOTION);
						}
					}
				}
				if (emotions != null) {
					// Increment or decrement each emotional state based on the input.
					for (Emotion emotion : getEmotions().values()) {
						Vertex emotionVertex = network.createVertex(emotion.primitive());
						Relationship relationship = source.getRelationship(Primitive.EMOTION, emotionVertex);
						if (relationship != null) {
							try {
								float value = relationship.getCorrectness();
								emotion.setState(emotion.getState() + value);
								log("Applying emotion", Level.FINE, emotion, value);
							} catch (Exception error) {
								getBot().log(this, error);
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Evaluate the input response for emotional influence.
	 */
	@Override
	public void evaluateResponse(Vertex response, Vertex meta) {
		if (!isEnabled()) {
			return;
		}
		// Let emotions simmer.
		for (Emotion emotion : getEmotions().values()) {
			emotion.setState(emotion.getState() / 2);
		}
		Collection<Relationship> emotions = null;
		if (meta != null) {
			emotions = meta.getRelationships(Primitive.EMOTION);
		}
		if (emotions == null) {
			emotions = response.getRelationships(Primitive.EMOTION);
		}
		if (emotions == null) {
			// Attempt to determine from words.
			List<Relationship> words = response.orderedRelationships(Primitive.WORD);
			if (words != null) {
				emotions = new ArrayList<Relationship>();
				for (Relationship word : words) {
					Collection<Relationship> wordEmotions = word.getTarget().getRelationships(Primitive.EMOTION);
					if (wordEmotions != null) {
						emotions.addAll(wordEmotions);
					}
				}
				// Check for compound word emotions as well.
				List<Vertex> compoundWords = Language.processCompoundWords(words);
				if (compoundWords != null) {
					for (Relationship word : words) {
						if (word.getTarget().instanceOf(Primitive.COMPOUND_WORD)) {
							Collection<Relationship> wordEmotions = word.getTarget().getRelationships(Primitive.EMOTION);
							if (wordEmotions != null) {
								emotions.addAll(wordEmotions);
							}
						}
					}
				}
			}
		}
		if (emotions != null) {
			// Increment or decrement each emotional state based on the input.
			for (Relationship relationship : emotions) {
				Vertex emotionVertex = relationship.getTarget();
				Emotion emotion = getEmotion(emotionVertex.getDataValue());
				try {
					float value = relationship.getCorrectness();
					emotion.setState(emotion.getState() + value);
					log("Applying emotion", Level.FINE, emotion, value);
				} catch (Exception error) {
					getBot().log(this, error);
				}
			}
		}
	}

	/**
	 * Self API to set mood.
	 */
	public Vertex setEmotion(Vertex source, Vertex emotionVertex, Vertex valueVertex) {
		Emotion emotion = getEmotion(emotionVertex.getDataValue());
		if (emotion == null) {
			log("Invalid emotion", Level.FINE, emotion);
		}
		if (valueVertex.getData() instanceof Number) {
			float value = ((Number)valueVertex.getData()).floatValue();
			if (value > 1 || value < -1) {
				log("Invalid emotion value (must be between -1 and 1)", Level.FINE, value);
			} else {
				emotion.setState(emotion.getState() + value);
			}
		}
		return source;
	}

	/**
	 * Self API to get mood.
	 */
	public Vertex getEmotion(Vertex source, Vertex emotionVertex) {
		Emotion emotion = getEmotion(emotionVertex.getDataValue());
		if (emotion == null) {
			log("Invalid emotion", Level.FINE, emotion);
			return null;
		}
		return source.getNetwork().createVertex(emotion.getState());
	}
	
	/**
	 * Evaluate the output with emotional expression.
	 */
	@Override
	public void evaluateOutput(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		for (Emotion emotion : getBot().mood().getEmotions().values()) {
			if (Math.abs(emotion.getState()) > 0.1) {
				Relationship relationship = output.addRelationship(Primitive.EMOTION, emotion.primitive());
				relationship.setCorrectness(emotion.getState());
			}
		}
	}
	
	/**
	 * Determine the main emotional state of the output.
	 */
	@Override
	public EmotionalState currentEmotionalState() {
		EmotionalState state = EmotionalState.NONE;
		float max = 0;
		float value = 0;
		Emotion maxEmotion = null;
		if (emotions != null) {
			// Find the most relevant emotional state.
			for (Emotion emotion : getEmotions().values()) {
				if (Math.abs(emotion.getState()) > max) {
					value = emotion.getState();
					max = Math.abs(value);
					maxEmotion = emotion;
				}
			}
		}
		if (max >= 0.3) {
			state = maxEmotion.evaluate(value);
		}
		return state;
	}
	
	/**
	 * Determine the main emotional state of the output.
	 */
	@Override
	public EmotionalState evaluateEmotionalState(Vertex output) {
		Collection<Relationship> emotions = output.getRelationships(Primitive.EMOTION);
		EmotionalState state = EmotionalState.NONE;
		float max = 0;
		float value = 0;
		Emotion maxEmotion = null;
		if (emotions != null) {
			// Find the most relevant emotional state.
			for (Emotion emotion : getEmotions().values()) {
				Relationship relationship = output.getRelationship(Primitive.EMOTION, emotion.primitive());
				if ((relationship != null) && (Math.abs(relationship.getCorrectness()) > max)) {
					value = relationship.getCorrectness();
					max = Math.abs(value);
					maxEmotion = emotion;
				}
			}
		}
		if (max >= 0.3) {
			state = maxEmotion.evaluate(value);
		}
		return state;
	}
	
	/**
	 * Determine the emotional states of the output.
	 */
	@Override
	public List<EmotionalState> evaluateEmotionalStates(Vertex output) {
		Collection<Relationship> emotions = output.getRelationships(Primitive.EMOTION);
		List<EmotionalState> states = new ArrayList<EmotionalState>();
		if (emotions != null) {
			// Find the most relevant emotional state.
			for (Emotion emotion : getEmotions().values()) {
				Relationship relationship = output.getRelationship(Primitive.EMOTION, emotion.primitive());
				if (relationship != null && Math.abs(relationship.getCorrectness()) >= 0.3) {
					states.add(emotion.evaluate(relationship.getCorrectness()));
				}
			}
		}
		return states;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	/**
	 * Print a useful string representation of the mood.  
	 */
	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		writer.write(getClass().getSimpleName());
		writer.write("(");
		boolean first = true;
		for (Emotion emotion : getEmotions().values()) {
			if (! first) {
				writer.write(", ");
			} else {
				first = false;
			}
			writer.write(emotion.toString());
		}
		writer.write(")");
		return writer.toString();
	}

}

