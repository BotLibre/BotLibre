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
package org.botlibre.sense.text;

import java.io.Writer;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Process text to and from networks and
 * stimulates natural language thoughts.
 */

public class TextEntry extends BasicSense {
	public static int LOG_SLEEP = 1000;
	
	/**
	 * Keeps track of the user involved in the conversation.
	 */
	protected Long userId;
	
	/**
	 * Keeps track of the current conversation.
	 */
	protected Long conversationId;

	/**
	 * The writer is the stream, text-box or chat client to output text to.
	 */
	protected Writer writer;
	
	protected TextListener textListener;
	
	/** Allows contact info to be passed to sense. */
	protected String info;

	public TextEntry() {
	}

	public TextListener getTextListener() {
		return textListener;
	}

	public void setTextListener(TextListener textListener) {
		this.textListener = textListener;
	}

	/**
	 * Return the user involved in the conversation.
	 */
	public Vertex getUser(Network network) {
		if (this.userId == null) {
			return null;
		}
		return network.findById(this.userId);
	}

	/**
	 * Set the user involved in the conversation.
	 */
	public void setUser(Vertex user) {
		if (user == null) {
			this.userId = null;
		} else {
			this.userId = user.getId();
		}
	}
	
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * Return the writer used to output text to.
	 */
	public Writer getWriter() {
		return this.writer;
	}

	/**
	 * Set the writer used to output text to.
	 */
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	/**
	 * Process the text input.
	 */
	@Override
	public void input(Object inputText, Network network) {
		if (!isEnabled()) {
			return;
		}
		TextInput text = null;
		if (inputText instanceof TextInput) {
			text = (TextInput)inputText;
		} else if (inputText instanceof CommandInput) {
			CommandInput command = (CommandInput)inputText;
			log("Command", Level.INFO, command.command, getUser(network), getConversation(network));
			inputCommand(command, network);
			return;
		} else {
			text = new TextInput((String)inputText);
		}
		log("Input", Level.INFO, text.text, getUser(network), getConversation(network));
		inputSentence(text, network);
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(TextInput text, Network network) {
		Vertex input = null;
		boolean newConversation = text.getText() == null;
		if (newConversation) {
			// Null input is used to get greeting.
			input = network.createInstance(Primitive.INPUT);
			input.addRelationship(Primitive.SENSE, getPrimitive());
			input.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
			input.addRelationship(Primitive.INPUT, Primitive.NULL);
		} else {
			input = createInputSentence(text.getText().trim(), network);
			input.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
		}
		if (text.isCorrection()) {
			input.addRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION);
		}
		if (text.isOffended()) {
			input.addRelationship(Primitive.ASSOCIATED, Primitive.OFFENDED);
		}
		input.addRelationship(Primitive.TARGET, Primitive.SELF);
		// Process speaker.
		Vertex speaker = getUser(network);
		if (speaker == null) {
			speaker = network.createSpeaker(DEFAULT_SPEAKER);
			speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ANONYMOUS);
			if (this.info != null && !this.info.isEmpty()) {
				String name = new TextStream(this.info).nextWord();
				speaker.addRelationship(Primitive.NAME, network.createName(name));
			}
			setUser(speaker);
		}

		Language language = this.bot.mind().getThought(Language.class);
		boolean applyEmote = language.shouldLearn(input, speaker) || (text.isCorrection() && language.shouldCorrect(input, speaker));
		
		input.addRelationship(Primitive.SPEAKER, speaker);
		if (this.emotionalState != null && this.emotionalState != EmotionalState.NONE) {
			this.emotionalState.apply(input);
			if (applyEmote) {
				this.emotionalState.apply(input.getRelationship(Primitive.INPUT));
			}
		}
		if (this.action != null) {
			input.addRelationship(Primitive.ACTION, new Primitive(this.action));
			if (applyEmote) {
				input.getRelationship(Primitive.INPUT).addRelationship(Primitive.ACTION, new Primitive(this.action));
			}
		}
		// Process conversation.
		Vertex conversation = getConversation(network);
		if (newConversation || (conversation == null)) {
			conversation = network.createInstance(Primitive.CONVERSATION);
			conversation.addRelationship(Primitive.TYPE, Primitive.CHAT);
			setConversation(conversation);
			conversation.addRelationship(Primitive.SPEAKER, speaker);
			conversation.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			if (this.info != null && !this.info.isEmpty()) {
				Vertex infoInput = createInputSentence("Info: " + this.info.trim(), network);
				infoInput.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
				Language.addToConversation(infoInput, conversation);
			}
			this.conversations++;
		} else {
			checkEngaged(conversation);
		}
		if (!newConversation) {
			Language.addToConversation(input, conversation);
		} else {
			input.addRelationship(Primitive.CONVERSATION, conversation);
		}
		
		network.save();
		this.bot.memory().addActiveMemory(input);
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputCommand(CommandInput command, Network network) {
		Vertex input = createInputCommand(command.command, network);
		input.addRelationship(Primitive.INSTANTIATION, Primitive.COMMAND);
		input.addRelationship(Primitive.TARGET, Primitive.SELF);
		// Process speaker.
		Vertex speaker = getUser(network);
		if (speaker == null) {
			speaker = network.createSpeaker(DEFAULT_SPEAKER);
			speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ANONYMOUS);
			if (this.info != null && !this.info.isEmpty()) {
				String name = new TextStream(this.info).nextWord();
				speaker.addRelationship(Primitive.NAME, network.createName(name));
			}
			setUser(speaker);
		}
		
		input.addRelationship(Primitive.SPEAKER, speaker);
		// Process conversation.
		Vertex conversation = getConversation(network);
		if (conversation == null) {
			conversation = network.createInstance(Primitive.CONVERSATION);
			conversation.addRelationship(Primitive.TYPE, Primitive.COMMAND);
			setConversation(conversation);
			conversation.addRelationship(Primitive.SPEAKER, speaker);
			conversation.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			if (this.info != null && !this.info.isEmpty()) {
				Vertex infoInput = createInputSentence("Info: " + this.info.trim(), network);
				infoInput.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
				Language.addToConversation(infoInput, conversation);
			}
		}
		Language.addToConversation(input, conversation);
		
		network.save();
		this.bot.memory().addActiveMemory(input);
	}

	/**
	 * Output the vertex to text.
	 */
	@Override
	public synchronized void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to Text, ignore.
		if (sense == null || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		if (this.textListener != null) {
			TextOutput text = new TextOutput();
			text.setMessage(printInput(output));
			this.textListener.sendMessage(text);
		} else {
			if (this.writer == null) {
				log("Missing writer", Level.WARNING);
				return;
			}
			try {
				this.writer.write(printInput(output));
			} catch (Exception failed) {
				log(failed);
			}
		}
	}

	/**
	 * Return the current conversation.
	 */
	public Long getConversationId() {
		return this.conversationId;
	}
	
	/**
	 * Return the current conversation.
	 */
	public Vertex getConversation(Network network) {
		if (this.conversationId == null) {
			return null;
		}
		Vertex id = network.findByData(this.conversationId);
		if (id == null) {
			this.conversationId = null;
			return null;
		}
		Vertex conversation = id.getRelationship(Primitive.CONVERSATION);
		if (conversation == null || !conversation.instanceOf(Primitive.CONVERSATION)) {
			this.conversationId = null;
			return null;
		}
		return conversation;
	}

	public void clearConversation() {
		this.conversationId = null;
		this.userId = null;
		this.action = null;
		this.emotionalState = EmotionalState.NONE;
		this.info = null;
	}
	
	/**
	 * Stop sensing.
	 */
	@Override
	public void shutdown() {
		super.shutdown();
		clearConversation();
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
		clearConversation();
	}

	/**
	 * Set the current conversation.
	 */
	public void setConversation(Vertex conversation) {
		if (conversation == null) {
			this.conversationId = null;
			return;
		}
		// Generate a random unique id for the conversation to allow client to access the conversation by id,
		// and still have the conversation secure.
		Vertex id = conversation.getRelationship(Primitive.GID);
		if (id == null) {
			id = conversation.getNetwork().createVertex(Math.abs(Utils.random().nextLong()));
			conversation.setRelationship(Primitive.GID, id);
			id.setRelationship(Primitive.CONVERSATION, conversation);
		}
		this.conversationId = (Long)id.getData();
	}

	/**
	 * Set the current conversation.
	 */
	public void setConversationId(long conversationId) {
		this.conversationId = conversationId;
	}
}