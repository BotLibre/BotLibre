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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfParseException;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

/**
 * Process text to and from networks and
 * stimulates natural language thoughts.
 */

public class TextEntry extends BasicSense {
	public static int LOG_SLEEP = 5000;
	public static int MAX_FILE_SIZE = 10000000;  // 10 meg
	
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
	
	/** Allows contact info to be passed to sense. */
	protected String info;

	public TextEntry() {
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
		speaker.addRelationship(Primitive.INPUT, input);
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
				System.out.println("addToConversation: " + infoInput);
			}
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
		if (getWriter() == null) {
			log("Missing writer", Level.WARNING);
			return;
		}
		try {
			getWriter().write(printInput(output));
		} catch (Exception failed) {
			log(failed);
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
		return network.findById(this.conversationId);
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
		this.conversationId = conversation.getId();
	}
	
	/**
	 * Process the log file from a URL.
	 */
	public void loadChatFile(URL file, String format, String encoding, boolean processUnderstanding, boolean pin) {
		try {
			loadChatFile(Utils.openStream(file), format, encoding, MAX_FILE_SIZE, processUnderstanding, pin);
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file.
	 */
	public void loadChatFile(File file, String format, String encoding, boolean processUnderstanding, boolean pin) {
		try {
			loadChatFile(new FileInputStream(file), format, encoding, MAX_FILE_SIZE, processUnderstanding, pin);
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file for a chat conversation.
	 * Input each message, in a listening only mode.
	 */
	public void loadChatFile(InputStream stream, String format, String encoding, int maxSize, boolean processUnderstanding, boolean pin) {
		try {
			String text = Utils.loadTextFile(stream, encoding, maxSize);
			loadChat(text, format, processUnderstanding, pin);
		} catch (BotException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file for a chat conversation.
	 * Input each message, in a listening only mode.
	 */
	public void loadChat(String text, String format, boolean processUnderstanding, boolean pin) {
		try {
			if ("Response List".equalsIgnoreCase(format)) {
				processResponseLog(text, pin);
			} else if ("Chat Log".equalsIgnoreCase(format)) {
				processChatLog(text, processUnderstanding, pin);
			} else if ("CSV List".equalsIgnoreCase(format)) {
				processCSVLog(text, pin);
			} else {
				throw new BotException("Invalid chat log format '" + format + "'");				
			}
		} catch (BotException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file for a chat conversation.
	 * Input each message, in a listening only mode.
	 */
	public void processChatLog(String log, boolean comprehension, boolean pin) {
		log("Loading chat log", Level.INFO, log.length());
		TextStream stream = new TextStream(log);
		Vertex lastSpeaker = null;
		LanguageState oldState = getLanguageState();
		Language languageThought = this.bot.mind().getThought(Language.class);
		LearningMode oldMode = languageThought.getLearningMode();
		if (comprehension) {
			setLanguageState(LanguageState.ListeningOnly);
			if (languageThought.getLearningMode() == LearningMode.Disabled) {
				languageThought.setLearningMode(LearningMode.Everyone);
			}
		}
		Vertex conversation = null;
		Vertex question = null;
		Vertex previous = null;
		boolean wasAdmin = false;
		boolean first = true;
		boolean cycle = false;
		int count = 0;
		try {
			while (!stream.atEnd()) {
				count++;
				int marker = stream.getPosition();
				String line = stream.nextLine().trim();
				if (first && line.indexOf("<?xml") != -1) {
					throw new SelfParseException("Chat log format must be text, not XML", stream);					
				}
				first = false;
				boolean cr = false;
				// Skip blank lines.
				while (line.length() == 0) {
					if (stream.atEnd()) {
						return;
					}
					cr = true;
					marker = stream.getPosition();
					line = stream.nextLine().trim();
				}
				Vertex input = null;
				log("Processing chat log", Level.INFO, count, line);
				for (int index = 0; index < RETRY; index++) {
					Network network = this.bot.memory().newMemory();
					try {
						if (comprehension) {
							// CR means new conversation.
							if (cr || (conversation == null)) {
								conversation = network.createInstance(Primitive.CONVERSATION);
								conversation.addRelationship(Primitive.TYPE, Primitive.CHAT);
								lastSpeaker = null;
							} else {
								conversation = network.createVertex(conversation);
							}
						}
						if (cr) {
							question = null;
							previous = null;
						}
						TextStream lineStream = new TextStream(line);
						String speakerName = lineStream.upTo(':');
						if (lineStream.atEnd()) {
							lineStream.reset();
							if (cycle) {
								speakerName = "self";
							} else {
								speakerName = "anonymous";
							}
							cycle = !cycle;
						} else {
							lineStream.skip();
						}
						char peek = lineStream.peek();
						EmotionalState state = EmotionalState.NONE;
						if (peek == '<') {
							lineStream.skip();
							String emotion = lineStream.upTo('>');
							if (lineStream.atEnd()) {
								stream.setPosition(marker);
								throw new SelfParseException("Expected '<emotion>' in chat text", stream);
							}
							lineStream.skip();
							try {
								state = EmotionalState.valueOf(emotion.toUpperCase());
							} catch (Exception exception) {
								stream.setPosition(marker);
								throw new SelfParseException("Invalid '<emotion>' in chat text", stream);								
							}
						}
						setEmotionalState(state);
						String message = lineStream.upToEnd().trim();
						if (speakerName.equalsIgnoreCase("default")) {
							Vertex language = network.createVertex(Language.class);
							input = network.createSentence(message);
							input.setPinned(true);
							language.addRelationship(Primitive.RESPONSE, input);
						} else if (speakerName.equalsIgnoreCase("greeting")) {
							Vertex language = network.createVertex(Language.class);
							input = network.createSentence(message);
							input.setPinned(true);
							language.addRelationship(Primitive.GREETING, input);
						} else if (speakerName.equalsIgnoreCase("script")) {
							input = SelfCompiler.getCompiler().evaluateEquation(
									message, network.createVertex(Primitive.SELF), network.createVertex(Primitive.SELF), false, network);
						} else {
							if (comprehension) {
								input = createInputSentence(message, network);
								input.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
								if (this.emotionalState != EmotionalState.NONE) {
									this.emotionalState.apply(input);
									this.emotionalState.apply(input.getRelationship(Primitive.INPUT));
								}
								if (lastSpeaker != null) {
									lastSpeaker = network.createVertex(lastSpeaker);
									input.addRelationship(Primitive.TARGET, lastSpeaker);
								}
								Vertex speaker = null;
								if (speakerName.toLowerCase().equals("self")) {
									speaker = network.createVertex(Primitive.SELF);
									wasAdmin = true;
								} else {
									speaker = network.createSpeaker(speakerName);
									if (speakerName.equals("admin")) {
										speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
										wasAdmin = true;
									}
								}
								input.addRelationship(Primitive.SPEAKER, speaker);
								speaker.addRelationship(Primitive.INPUT, input);
								conversation.addRelationship(Primitive.SPEAKER, speaker);
								Language.addToConversation(input, conversation);
								lastSpeaker = speaker;
							}
							Vertex sentence = network.createSentence(message);
							if (pin) {
								sentence.setPinned(true);
							}
							if (question != null) {
								question = network.createVertex(question);
								if (pin) {
									question.setPinned(true);
								}
								question.addWeakRelationship(Primitive.RESPONSE, sentence, languageThought.getLearningRate());
								question.associateAll(Primitive.WORD, question, Primitive.QUESTION);
								network.checkReduction(question);
								question.weakAssociateAll(Primitive.SYNONYM, sentence, Primitive.RESPONSE, languageThought.getLearningRate());
								if (previous != null) {
									previous = network.createVertex(previous);
									Language.addSentencePreviousMeta(question, sentence, previous, false, network);
								}
							}
							previous = question;
							question = sentence;
						}
						network.save();
						break;
					} catch (SelfParseException failed) {
						failed.printStackTrace();
						log(failed);
						network.clear();
						throw failed;
					} catch (Exception failed) {
						failed.printStackTrace();
						log(failed);
						network.clear();
					}
				}
				if (comprehension) {
					if (input == null) {
						return;
					}
					this.bot.memory().addActiveMemory(input);
					int abort = 0;
					while ((abort < 20) && !input.hasRelationship(Primitive.CONTEXT)) {
						Utils.sleep(100);
						Network memory = this.bot.memory().newMemory();
						input = memory.createVertex(input);
						abort++;
					}
					if (count == 100) {
						log("Chat log import exceeds 100 lines, offloading to background task", Level.WARNING);
					}
					if (abort >= 20 || count > 100) {
						Utils.sleep(LOG_SLEEP);
					}
				}
			}
			if (languageThought.getLearningMode() == LearningMode.Administrators && !wasAdmin) {
				throw new SelfParseException("Expected 'admin:' speaker in chat text when learning mode is administrators only", stream);
			}
		} finally {
			setLanguageState(oldState);
			languageThought.setLearningMode(oldMode);
		}
	}
		
	/**
	 * Process the log file for a list of question/answers.
	 * Input each message, in a listening only mode.
	 */
	public void processResponseLog(String log, boolean pin) {
		log("Loading response log", Level.INFO, log.length());
		TextStream stream = new TextStream(log);
		Network network = this.bot.memory().newMemory();
		Vertex question = null;
		Vertex answer = null;
		boolean first = true;
		boolean isDefault = false;
		while (!stream.atEnd()) {
			String line = stream.nextLine().trim();
			if (first && line.indexOf("<?xml") != -1) {
				throw new SelfParseException("Chat log format must be text, not XML", stream);					
			}
			first = false;
			String originalLine = line;
			// Skip blank lines.
			while (line.isEmpty()) {
				if (stream.atEnd()) {
					return;
				}
				question = null;
				answer = null;
				line = stream.nextLine().trim();
				originalLine = line;
				if (!line.isEmpty()) {
					network = this.bot.memory().newMemory();
				}
			}
			log("Processing response log", Level.INFO, line);
			TextStream lineStream = new TextStream(line);
			String command = lineStream.upTo(':');
			if (!lineStream.atEnd()) {
				lineStream.skip();
				line = lineStream.upToEnd().trim();
			} else {
				command = "";
			}
			if (command.equalsIgnoreCase("default")) {
				isDefault = true;
				Vertex language = network.createVertex(Language.class);
				question = network.createSentence(line);
				question.setPinned(true);
				language.addRelationship(Primitive.RESPONSE, question);
			} else if (command.equalsIgnoreCase("greeting")) {
				Vertex language = network.createVertex(Language.class);
				question = network.createSentence(line);
				question.setPinned(true);
				language.addRelationship(Primitive.GREETING, question);
			} else if (command.equalsIgnoreCase("script")) {
				SelfCompiler.getCompiler().evaluateEquation(
						line, network.createVertex(Primitive.SELF), network.createVertex(Primitive.SELF), false, network);
			} else if (command.equalsIgnoreCase("keywords")) {
				if (question == null || answer == null) {
					throw new BotException("Missing question and response for keywords");
				}
				Language.addSentenceKeyWordsMeta(question, answer, line, network);
			} else if (command.equalsIgnoreCase("required")) {
				if (question == null || answer == null) {
					throw new BotException("Missing question and response for required words");
				}
				Language.addSentenceRequiredMeta(question, answer, line, network);
			} else if (command.equalsIgnoreCase("emotions")) {
				if (question == null) {
					throw new BotException("Missing phrase for emotions");
				}
				if (answer == null) {
					question.internalRemoveRelationships(Primitive.EMOTION);
					for (String emote : Utils.getWords(line)) {
						if (!emote.equals("none")) {
							try {
								EmotionalState.valueOf(emote.toUpperCase()).apply(question);
							} catch (Exception exception) {
								throw new BotException("Invalid emotion: " + emote);
							}
						}
					}					
				} else {
					Language.addSentenceEmotesMeta(question, answer, line, network);
				}
			} else if (command.equalsIgnoreCase("actions")) {
				if (question == null) {
					throw new BotException("Missing phrase for actions");
				}
				if (answer == null) {
					question.internalRemoveRelationships(Primitive.ACTION);
					for (String action : Utils.getWords(line)) {
						if (!action.equals("none")) {
							question.addRelationship(Primitive.ACTION, new Primitive(action));
						}
					}					
				} else {
					Language.addSentenceActionMeta(question, answer, line, network);
				}
			} else if (command.equalsIgnoreCase("poses")) {
				if (question == null) {
					throw new BotException("Missing phrase for poses");
				}
				if (answer == null) {
					question.internalRemoveRelationships(Primitive.POSE);
					for (String pose : Utils.getWords(line)) {
						if (!pose.equals("none")) {
							question.addRelationship(Primitive.POSE, new Primitive(pose));
						}
					}					
				} else {
					Language.addSentencePoseMeta(question, answer, line, network);
				}
			} else if (command.equalsIgnoreCase("previous")) {
				if (question == null || (answer == null && !isDefault)) {
					throw new BotException("Missing question and response for previous");
				}
				Vertex previous = network.createSentence(line);
				if (pin) {
					previous.setPinned(true);
				}
				if (isDefault) {
					Vertex language = network.createVertex(Language.class);
					Language.addSentencePreviousMeta(language, question, previous, false, network);
				} else {
					Language.addSentencePreviousMeta(question, answer, previous, false, network);
				}
			} else if (command.equalsIgnoreCase("require previous")) {
				if (question == null || (answer == null && !isDefault)) {
					throw new BotException("Missing question and response for previous");
				}
				Vertex previous = network.createSentence(line);
				if (pin) {
					previous.setPinned(true);
				}
				if (isDefault) {
					Vertex language = network.createVertex(Language.class);
					Language.addSentencePreviousMeta(language, question, previous, true, network);
				} else {
					Language.addSentencePreviousMeta(question, answer, previous, true, network);
				}
			} else if (command.equalsIgnoreCase("label")) {
				if (question == null) {
					throw new BotException("Missing phrase for label");
				}
				if (line.startsWith("#")) {
					line = line.substring(1, line.length());
				}
				if (!Utils.isAlphaNumeric(line)) {
					throw new BotException("A label must be a single alpha numeric string with no spaces (use - for a space) - " + line);
				}
				Vertex label = network.createVertex(new Primitive(line));
				if (pin) {
					label.setPinned(true);
				}
				label.addRelationship(Primitive.INSTANTIATION, Primitive.LABEL);
				if (answer == null) {
					question.setRelationship(Primitive.LABEL, label);
					label.setRelationship(Primitive.RESPONSE, question);
				} else {
					answer.setRelationship(Primitive.LABEL, label);
					label.setRelationship(Primitive.RESPONSE, answer);
				}
			} else if (command.equalsIgnoreCase("on repeat")) {
				if (question == null) {
					throw new BotException("Missing question for on repeat");
				}
				Vertex repeat = network.createSentence(line);
				if (pin) {
					repeat.setPinned(true);
				}
				if (answer == null) {
					question.addRelationship(Primitive.ONREPEAT, repeat);
				} else {
					answer.addRelationship(Primitive.ONREPEAT, repeat);
				}
			} else if (command.equalsIgnoreCase("no repeat")) {
				if (question == null) {
					throw new BotException("Missing question for no repeat");
				}
				if (answer == null) {
					question.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				} else {
					answer.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				}
			} else if (command.equalsIgnoreCase("topic")) {
				if (question == null) {
					throw new BotException("Missing phrase for topic");
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceTopicMeta(language, question, line, network);
					} else {
						Vertex topicFragment = network.createFragment(line);
						topicFragment.addRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
						network.createVertex(Primitive.TOPIC).addRelationship(Primitive.INSTANCE, topicFragment);
						topicFragment.addRelationship(Primitive.QUESTION, question);
						question.setRelationship(Primitive.TOPIC, topicFragment);
					}
				} else {
					Language.addSentenceTopicMeta(question, answer, line, network);
				}
			} else {
				isDefault = false;
				Vertex sentence = null;
				if (originalLine.startsWith("#")) {
					originalLine = originalLine.substring(1, originalLine.length());
					if (!Utils.isAlphaNumeric(originalLine)) {
						throw new BotException("A label must be a single alpha numeric string with no spaces (use - for a space) - " + originalLine);
					}
					sentence = network.createVertex(new Primitive(originalLine));
					if (!sentence.hasRelationship(Primitive.INSTANTIATION, Primitive.LABEL)) {
						throw new BotException("Missing label - #" + originalLine);								
					}
				} else {
					sentence = network.createSentence(originalLine);
				}
				if (pin) {
					sentence.setPinned(true);
				}
				if (question == null) {
					question = sentence;
				} else {
					answer = sentence;
					question.addWeakRelationship(Primitive.RESPONSE, sentence, 0.9f);
					question.associateAll(Primitive.WORD, question, Primitive.QUESTION);
					network.checkReduction(question);
					question.associateAll(Primitive.SYNONYM, sentence, Primitive.RESPONSE);
				}
			}
			network.save();
		}
		network.save();
	}

	/**
	 * Process the log file for list a of question/answers.
	 * Input each message, in a listening only mode.
	 */
	public void processCSVLog(String log, boolean pin) {
		log("Loading csv log", Level.INFO, log.length());
		TextStream stream = new TextStream(log);
		Network network = this.bot.memory().newMemory();
		Vertex question = null;
		Vertex answer = null;
		boolean first = true;
		while (!stream.atEnd()) {
			String line = stream.nextLine().trim();
			if (first && line.indexOf("<?xml") != -1) {
				throw new SelfParseException("Chat log format must be text, not XML", stream);					
			}
			first = false;
			// Skip blank lines.
			while (line.isEmpty()) {
				if (stream.atEnd()) {
					return;
				}
				question = null;
				answer = null;
				line = stream.nextLine().trim();
				if (!line.isEmpty()) {
					network = this.bot.memory().newMemory();
				}
			}
			// Allow either ',' or '","' separators.
			boolean quotes = line.contains("\"");
			log("Processing csv line", Level.INFO, line);
			// "questions","answer","topic"
			// "What is this? What's this?","This is Open Bot.","Bot"
			TextStream lineStream = new TextStream(line);
			if (quotes) {
				lineStream.skipTo('"');
				lineStream.skip();
			}
			if (lineStream.atEnd()) {
				log("Expecting \" character", Level.WARNING, line);
				continue;
			}
			String questionText = null;
			if (quotes) {
				questionText = lineStream.upToAll("\",\"").trim();
				lineStream.skip("\",\"".length());
			} else {
				questionText = lineStream.upTo(',').trim();
				lineStream.skip();
			}
			if (lineStream.atEnd()) {
				log("Expecting \",\" characters", Level.WARNING, line);
				continue;
			}
			log("Processing csv question", Level.INFO, questionText);
			String answerText = null;
			if (quotes) {
				answerText = lineStream.upToAll("\"").trim();
			} else {
				answerText = lineStream.upTo(',').trim();				
			}
			lineStream.skip();
			log("Processing csv answer", Level.INFO, answerText);
			answer = network.createSentence(answerText);
			if (pin) {
				answer.setPinned(true);
			}
			// Topic
			String topic = "";
			if (quotes) {
				if (lineStream.peek() != ',') {
					lineStream.skipTo('"', true);
					topic = lineStream.upTo('"').trim();
				}
			} else {
				topic = lineStream.upTo(',').trim();				
			}
			lineStream.skip();
			// Keywords
			String keywords = "";
			if (quotes) {
				if (lineStream.peek() != ',') {
					lineStream.skipTo('"', true);
					keywords = lineStream.upTo('"').trim();
				}
			} else {
				keywords = lineStream.upTo(',').trim();				
			}
			lineStream.skip();
			// Required
			String required = "";
			if (quotes) {
				if (lineStream.peek() != ',') {
					lineStream.skipTo('"', true);
					required = lineStream.upTo('"').trim();
				}
			} else {
				required = lineStream.upTo(',').trim();				
			}
			
			TextStream questionStream = new TextStream(questionText);
			while (!questionStream.atEnd()) {
				questionText = questionStream.upTo('?', true).trim();
				if (!questionText.isEmpty() && !questionText.equals("?")) {
					question = network.createSentence(questionText);
					if (pin) {
						question.setPinned(true);
					}
					question.addWeakRelationship(Primitive.RESPONSE, answer, 0.9f);
					question.associateAll(Primitive.WORD, question, Primitive.QUESTION);
					network.checkReduction(question);
					question.associateAll(Primitive.SYNONYM, answer, Primitive.RESPONSE);
					if (!topic.isEmpty()) {
						log("Processing csv topic", Level.INFO, topic);
						Language.addSentenceTopicMeta(question, answer, topic, network);
					}
					if (!keywords.isEmpty()) {
						log("Processing csv keywords", Level.INFO, topic);
						Language.addSentenceKeyWordsMeta(question, answer, keywords, network);
					}
					if (!required.isEmpty()) {
						log("Processing csv required", Level.INFO, topic);
						Language.addSentenceRequiredMeta(question, answer, required, network);
					}
				}
			}
			network.save();
		}
		network.save();
	}
}