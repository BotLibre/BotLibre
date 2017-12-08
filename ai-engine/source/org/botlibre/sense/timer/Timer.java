/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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
package org.botlibre.sense.timer;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

/**
 * Enables executing message event at time intervals.
 */
public class Timer extends BasicSense {
	
	protected boolean initProperties;
	
	protected int maxErrors = 5;
	protected int errors;
	protected boolean enableTimers = false;
	protected int timerHours = 24;
	
	public Timer(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Answering;
	}
	
	public Timer() {
		this(false);
	}

	public boolean getEnableTimers() {
		initProperties();
		return enableTimers;
	}

	public void setEnableTimers(boolean enableTimers) {
		initProperties();
		this.enableTimers = enableTimers;
	}

	public int getTimerHours() {
		initProperties();
		return timerHours;
	}

	public void setTimerHours(int timerHours) {
		initProperties();
		this.timerHours = timerHours;
	}

	public List<Vertex> getTimers(Network network) {
		return network.createVertex(getPrimitive()).orderedRelations(Primitive.MESSAGES);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		setIsEnabled(true);
	}

	
	/**
	 * Load settings.
	 */
	public void initProperties() {
		if (this.initProperties) {
			return;
		}
		synchronized (this) {
			if (this.initProperties) {
				return;
			}
			getBot().memory().loadProperties("Timer");
			
			String property = this.bot.memory().getProperty("Timer.enableTimers");
			if (property != null) {
				this.enableTimers = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Timer.timerHours");
			if (property != null) {
				this.timerHours = Integer.valueOf(property);
			}
			this.initProperties = true;
		}
	}

	public void saveProperties(List<String> timers) {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Timer.enableTimers", String.valueOf(this.enableTimers), false);
		memory.saveProperty("Timer.timerHours", String.valueOf(this.timerHours), false);

		Vertex sense = memory.createVertex(getPrimitive());
		sense.unpinChildren();
		if (timers != null) {
			sense.internalRemoveRelationships(Primitive.MESSAGES);
			for (String text : timers) {
				Vertex message =  memory.createSentence(text);
				message.addRelationship(Primitive.INSTANTIATION, Primitive.TIMER);
				sense.addRelationship(Primitive.MESSAGES, message);
			}
		}
		sense.pinChildren();
		memory.save();
	}

	/**
	 * Auto post.
	 */
	public void checkTimers() {
		if (!getEnableTimers()) {
			return;
		}
		log("Timers", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex sense = memory.createVertex(getPrimitive());
			Vertex vertex = sense.getRelationship(Primitive.LASTPOST);
			long last = 0;
			if (vertex != null) {
				last = ((Timestamp)vertex.getData()).getTime();
			}
			long millis = getTimerHours() * 60 * 60 * 1000;
			if ((System.currentTimeMillis() - last) < millis) {
				log("Timer hours not reached", Level.FINE, getTimerHours());
				return;
			}
			// Increase script timeout.
			Language language = getBot().mind().getThought(Language.class);
			int timeout = language.getMaxStateProcess();
			language.setMaxStateProcess(timeout * 2);
			try {
				List<Vertex> timers = getTimers(memory);
				if (timers != null && !timers.isEmpty()) {
					for (Vertex timer : timers) {
						if (timer != null) {
							log("Timer", Level.INFO, timer);
							Network network = getBot().memory().newMemory();
							inputSentence(timer.printString(), network);
					    	Utils.sleep(100);
							sense.setRelationship(Primitive.LASTPOST, memory.createTimestamp());
					    	memory.save();
						}
					}
				}
			} finally {
				language.setMaxStateProcess(timeout);
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, self);		
		input.addRelationship(Primitive.TARGET, self);
		
		Vertex conversation = network.createVertex();
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.TIMER);
		conversation.addRelationship(Primitive.SPEAKER, self);
		conversation.addRelationship(Primitive.SPEAKER, self);
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
	
	/**
	 * Create an input based on the sentence.
	 */
	protected Vertex createInput(String text, Network network) {
		Vertex sentence = network.createSentence(text);
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.TIMER);
		return input;
	}
	
}