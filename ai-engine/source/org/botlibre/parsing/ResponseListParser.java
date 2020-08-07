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
package org.botlibre.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfParseException;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;


/**
 * This parses several response list and chat log formats:
 * <ul>
 * <li> A "response list" is a set of questions on one line followed by their response on the next, with a blank line in-between responses.
 * <li> A "CSV list" is a modified version of the response list format that is spreadsheet friendly, using comma separated values.
 * <li> A "chat log" is a conversation log, with the user name, colon, chat message, followed by the next message, with a blank line in-between conversations.
 * </ul>
 */
public class ResponseListParser {
	public static int MAX_FILE_SIZE = 10000000;  // 10 meg
	public static int PAGE = 100;
	
	protected static ResponseListParser parser = new ResponseListParser();

	public static ResponseListParser parser() {
		return parser;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Process the log file from a URL.
	 */
	public void loadChatFile(URL file, String format, String encoding, boolean processUnderstanding, boolean pin, boolean autoReduce, Bot bot) {
		try {
			loadChatFile(Utils.openStream(file), format, encoding, MAX_FILE_SIZE, processUnderstanding, pin, autoReduce, bot);
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file.
	 */
	public void loadChatFile(File file, String format, String encoding, boolean processUnderstanding, boolean pin, boolean autoReduce, Bot bot) {
		try {
			loadChatFile(new FileInputStream(file), format, encoding, MAX_FILE_SIZE, processUnderstanding, pin, autoReduce, bot);
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file.
	 */
	public void loadChatFile(File file, String format, String encoding, boolean processUnderstanding, boolean pin, Bot bot) {
		try {
			loadChatFile(new FileInputStream(file), format, encoding, MAX_FILE_SIZE, processUnderstanding, pin, false, bot);
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	/**
	 * Process the log file for a chat conversation.
	 * Input each message, in a listening only mode.
	 */
	public void loadChatFile(InputStream stream, String format, String encoding, int maxSize, boolean processUnderstanding, boolean pin, boolean autoReduce, Bot bot) {
		try {
			String text = Utils.loadTextFile(stream, encoding, maxSize);
			loadChat(text, format, processUnderstanding, pin, autoReduce, bot);
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
	public void loadChat(String text, String format, boolean processUnderstanding, boolean pin, boolean autoReduce, Bot bot) {
		try {
			if ("Response List".equalsIgnoreCase(format)) {
				processResponseLog(text, pin, autoReduce, bot);
			} else if ("Chat Log".equalsIgnoreCase(format)) {
				processChatLog(text, processUnderstanding, pin, autoReduce, bot);
			} else if ("CSV List".equalsIgnoreCase(format)) {
				processCSVLog(text, pin, autoReduce, bot);
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
	public void processChatLog(String log, boolean comprehension, boolean pin, boolean autoReduce,  Bot bot) {
		bot.log(this, "Loading chat log", Level.INFO, log.length());
		TextStream stream = new TextStream(log);
		Vertex lastSpeaker = null;
		TextEntry sense = bot.awareness().getSense(TextEntry.class);
		LanguageState oldState = sense.getLanguageState();
		Language languageThought = bot.mind().getThought(Language.class);
		LearningMode oldMode = languageThought.getLearningMode();
		if (comprehension) {
			sense.setLanguageState(LanguageState.ListeningOnly);
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
				bot.log(this, "Processing chat log", Level.INFO, count, line);
				for (int index = 0; index < BasicSense.RETRY; index++) {
					Network network = bot.memory().newMemory();
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
						sense.setEmotionalState(state);
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
							input = SelfCompiler.getCompiler().evaluateExpression(
									message, network.createVertex(Primitive.SELF), network.createVertex(Primitive.SELF), pin, false, network);
						} else {
							if (comprehension) {
								input = sense.createInputSentence(message, network);
								input.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
								if (sense.getEmotionalState() != EmotionalState.NONE) {
									sense.getEmotionalState().apply(input);
									sense.getEmotionalState().apply(input.getRelationship(Primitive.INPUT));
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
								if (autoReduce && !question.getDataValue().startsWith("Pattern(")) {
									question = network.createSentence(Utils.reduce(question.getDataValue()));
								}
								if (pin) {
									question.setPinned(true);
								}
								Language.addResponse(question, sentence, null, null, null, languageThought.getLearningRate(), network);
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
						bot.log(this, failed);
						network.clear();
						throw failed;
					} catch (Exception failed) {
						failed.printStackTrace();
						bot.log(this, failed);
						network.clear();
					}
				}
				if (comprehension) {
					if (input == null) {
						return;
					}
					bot.memory().addActiveMemory(input);
					int abort = 0;
					while ((abort < 20) && bot.memory().getActiveMemory().size() > 10) {
						Utils.sleep(100);
						abort++;
					}
					if (count == 100) {
						bot.log(this, "Chat log import exceeds 100 lines, offloading to background task", Level.WARNING);
					}
					if (abort >= 20 || count > 100) {
						Utils.sleep(sense.LOG_SLEEP);
					}
				}
			}
			if (languageThought.getLearningMode() == LearningMode.Administrators && !wasAdmin) {
				throw new SelfParseException("Expected 'admin:' speaker in chat text when learning mode is administrators only", stream);
			}
		} finally {
			sense.setLanguageState(oldState);
			languageThought.setLearningMode(oldMode);
		}
	}
		
	/**
	 * Process the log file for a list of question/answers.
	 * Input each message, in a listening only mode.
	 */
	public void processResponseLog(String log, boolean pin, boolean autoReduce, Bot bot) {
		bot.log(this, "Loading response log", Level.INFO, log.length());
		TextStream stream = new TextStream(log);
		Network network = bot.memory().newMemory();
		Vertex question = null;
		Vertex answer = null;
		boolean first = true;
		boolean isDefault = false;
		boolean isGreeting = false;
		boolean isWord = false;
		boolean hasSentiment = false;
		int lastIndent = 0;
		int indent = 0;
		List<Relationship> conversationStack = new ArrayList<>();
		Relationship lastResponseRelationship = null;
		int pageCount = 0;
		while (!stream.atEnd()) {
			String fullLine = stream.nextLine();
			String line = fullLine.trim();
			if (first && line.indexOf("<?xml") != -1) {
				throw new SelfParseException("Response list format must be text, not XML", stream);
			}
			first = false;
			String originalLine = line;
			// Skip blank lines.
			while (line.isEmpty()) {
				if (stream.atEnd()) {
					break;
				}
				question = null;
				answer = null;
				fullLine = stream.nextLine();
				line = fullLine.trim();
				originalLine = line;
				if (!line.isEmpty()) {
					pageCount++;
					if (pageCount >= PAGE) {
						network.save();
						// Clear memory.
						network = bot.memory().newMemory();
						pageCount = 0;
					}
				}
			}
			bot.log(this, "Processing response log", Level.INFO, line);
			indent = 0;
			int index = 0;
			// Check the indent, support tabs and 4 spaces.
			while (fullLine.startsWith("\t", index) || fullLine.startsWith("    ", index)) {
				if (fullLine.startsWith("\t", index)) {
					index++;
				} else {
					index = index + 4;
				}
				indent++;
			}
			// If a new question, check if the indent has changed, and pop/push the conversation stack.
			if (indent > lastIndent) {
				if (indent > (lastIndent + 1) || lastResponseRelationship == null) {
					throw new SelfParseException("Incorrect conversation indent", stream);
				}
				conversationStack.add(lastResponseRelationship);
				question = null;
				answer = null;
			} else if (indent < lastIndent) {
				for (int count = 0; count < (lastIndent - indent); count++) {
					if (conversationStack.isEmpty()) {
						throw new SelfParseException("Incorrect conversation indent", stream);
					}
					conversationStack.remove(conversationStack.size() - 1);
				}
				question = null;
				answer = null;
			}
			lastIndent = indent;
			// Check for quoted multi-line responses.
			if (line.startsWith("\"")) {
				String nextLine = line;
				while (!stream.atEnd() && !nextLine.endsWith("\"")) {
					nextLine = stream.nextLine().trim();
					line = line + "\n" + nextLine;
				}
				line = line.substring(1, line.length() - 1);
				originalLine = line;
			}
			TextStream lineStream = new TextStream(line);
			String command = lineStream.upTo(':');
			if (!lineStream.atEnd()) {
				lineStream.skip();
				line = lineStream.upToEnd().trim();
				// Check for quoted multi-line greetings/defaults.
				if (line.startsWith("\"") &&
						(command.equals("greeting")
							|| command.equals("default")
							|| command.equals("question")
							|| command.equals("response"))) {
					String nextLine = line;
					while (!stream.atEnd() && !nextLine.endsWith("\"")) {
						nextLine = stream.nextLine().trim();
						line = line + "\n" + nextLine;
					}
					line = line.substring(1, line.length() - 1);
					originalLine = line;
				}
			} else {
				command = "";
			}
			Vertex conversationMeta = null;
			if (!conversationStack.isEmpty()) {
				Relationship conversation = conversationStack.get(conversationStack.size() - 1);
				conversation = network.createVertex(conversation.getSource()).getRelationship(conversation.getType(), conversation.getTarget());
				conversationMeta = network.createMeta(conversation);
				// Set parent relationship for inheritance.
				if (conversationStack.size() > 1) {
					conversation = conversationStack.get(conversationStack.size() - 2);
					conversation = network.createVertex(conversation.getSource()).getRelationship(conversation.getType(), conversation.getTarget());
					Vertex parent = network.createMeta(conversation);
					conversationMeta.addRelationship(Primitive.PARENT, parent);
				}
			}
			if (command.equalsIgnoreCase("default")) {
				if (conversationMeta == null) {
					isDefault = true;
					Vertex language = network.createVertex(Language.class);
					question = network.createSentence(line);
					if (pin) {
						SelfCompiler.getCompiler().pin(question);
					}
					lastResponseRelationship = language.addRelationship(Primitive.RESPONSE, question);
				} else {
					// Use a * pattern for conversations.
					// Conversations are defined by the NEXT relationship on the meta.
					question = network.createVertex(Primitive.DEFAULT);
					answer = network.createSentence(line);
					if (pin) {
						SelfCompiler.getCompiler().pin(answer);
					}
					Relationship relationship = conversationMeta.addRelationship(Primitive.NEXT, question);
					Vertex meta = network.createMeta(relationship);
					lastResponseRelationship = meta.addWeakRelationship(Primitive.RESPONSE, answer, 1.0f);
				}
			} else if (command.equalsIgnoreCase("greeting")) {
				isGreeting = true;
				Vertex language = network.createVertex(Language.class);
				question = network.createSentence(line);
				if (pin) {
					SelfCompiler.getCompiler().pin(question);
				}
				lastResponseRelationship = language.addRelationship(Primitive.GREETING, question);
			} else if (command.equalsIgnoreCase("phrase")) {
				question = network.createSentence(line);
				if (pin) {
					question.setPinned(true);
				}
			} else if (command.equalsIgnoreCase("word")) {
				isWord = true;
				question = network.createWord(line);
				if (pin) {
					question.setPinned(true);
				}
			} else if (command.equalsIgnoreCase("script")) {
				Vertex result = SelfCompiler.getCompiler().evaluateExpression(
						line, network.createVertex(Primitive.SELF), network.createVertex(Primitive.SELF), pin, false, network);
				if (pin) {
					SelfCompiler.getCompiler().pin(result);
				}
			} else if (command.equalsIgnoreCase("keyword")) {
				if (!isWord) {
					throw new SelfParseException("Missing word for keyword", stream);
				}
				if (line.contains("false")) {
					Relationship relationship = question.getRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
					if (relationship != null) {
						question.internalRemoveRelationship(relationship);
					}
				} else {
					question.addRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
				}
			} else if (command.equalsIgnoreCase("keywords")) {
				if (question == null || answer == null) {
					throw new SelfParseException("Missing question and response for keywords", stream);
				}
				Vertex context = checkConversationMeta(conversationMeta, question, network);
				Language.addSentenceKeyWordsMeta(context, answer, line, network);
			} else if (command.equalsIgnoreCase("required")) {
				if (question == null || answer == null) {
					throw new SelfParseException("Missing question and response for required words", stream);
				}
				Vertex context = checkConversationMeta(conversationMeta, question, network);
				Language.addSentenceRequiredMeta(context, answer, line, network);
			} else if (command.equalsIgnoreCase("sentiment")) {
				if (question == null) {
					throw new SelfParseException("Missing word or phrase for sentiment", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceEmotesMeta(language, question, line, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceEmotesMeta(language, question, Primitive.GREETING, line, network);
					} else {
						// Clear the existing emotions, unless sentiment was already applied.
						if (!hasSentiment) {
							question.internalRemoveRelationships(Primitive.EMOTION);
						}
						for (String emote : Utils.getWords(line)) {
							if (!emote.equals("none")) {
								try {
									EmotionalState.valueOf(emote.toUpperCase()).apply(question);
								} catch (Exception exception) {
									throw new SelfParseException("Invalid sentiment: " + emote, stream);
								}
							}
						}
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceEmotesMeta(context, answer, line, network);
				}
				hasSentiment = true;
			} else if (command.equalsIgnoreCase("emotions")) {
				if (question == null) {
					throw new SelfParseException("Missing word or phrase for emotions", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceEmotesMeta(language, question, line, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceEmotesMeta(language, question, Primitive.GREETING, line, network);
					} else {
						// Clear the existing emotions, unless sentiment was already applied.
						if (!hasSentiment) {
							question.internalRemoveRelationships(Primitive.EMOTION);
						}
						for (String emote : Utils.getWords(line)) {
							if (!emote.equals("none")) {
								try {
									EmotionalState.valueOf(emote.toUpperCase()).apply(question);
								} catch (Exception exception) {
									throw new SelfParseException("Invalid emotion: " + emote, stream);
								}
							}
						}
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceEmotesMeta(context, answer, line, network);
				}
				hasSentiment = true;
			} else if (command.equalsIgnoreCase("actions")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for actions", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceActionMeta(language, question, line, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceActionMeta(language, question, Primitive.GREETING, line, network);
					} else {
						question.internalRemoveRelationships(Primitive.ACTION);
						for (String action : Utils.getWords(line)) {
							if (!action.equals("none")) {
								question.addRelationship(Primitive.ACTION, new Primitive(action));
							}
						}
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceActionMeta(context, answer, line, network);
				}
			} else if (command.equalsIgnoreCase("poses")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for poses", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentencePoseMeta(language, question, line, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentencePoseMeta(language, question, Primitive.GREETING, line, network);
					} else {
						question.internalRemoveRelationships(Primitive.POSE);
						for (String pose : Utils.getWords(line)) {
							if (!pose.equals("none")) {
								question.addRelationship(Primitive.POSE, new Primitive(pose));
							}
						}
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentencePoseMeta(context, answer, line, network);
				}
			} else if (command.equalsIgnoreCase("synonyms")) {
				if (question == null || !isWord) {
					throw new SelfParseException("Missing word for synonyms", stream);
				}
				question.internalRemoveRelationships(Primitive.SYNONYM);
				TextStream synonymStream = new TextStream(line);
				while (!synonymStream.atEnd()) {
					String word = synonymStream.nextWord();
					if (word == null) {
						break;
					}
					// Support compound words.
					if (word.equals("\"")) {
						word = synonymStream.upTo('"');
					}
					Vertex synonym = network.createWord(word);
					question.addRelationship(Primitive.SYNONYM, synonym);
					synonym.addRelationship(Primitive.SYNONYM, question);
					if (pin) {
						SelfCompiler.getCompiler().pin(synonym);
					}
				}
			} else if (command.equalsIgnoreCase("previous")) {
				if (question == null || (answer == null && !isDefault)) {
					throw new SelfParseException("Missing question and response for previous", stream);
				}
				Vertex previous = null;
				if (line.startsWith("#")) {
					line = line.substring(1, line.length());
					if (!Utils.isAlphaNumeric(line)) {
						throw new SelfParseException("A intent label must be a single alpha numeric string with no spaces (use - for a space) - " + line, stream);
					}
					previous = network.createVertex(new Primitive(line));
				} else {
					previous = network.createSentence(line);
				}
				if (pin) {
					SelfCompiler.getCompiler().pin(previous);
				}
				if (isDefault) {
					Vertex language = network.createVertex(Language.class);
					Language.addSentencePreviousMeta(language, question, previous, false, network);
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentencePreviousMeta(context, answer, previous, false, network);
				}
			} else if (command.equalsIgnoreCase("require previous")) {
				if (question == null || (answer == null && !isDefault)) {
					throw new SelfParseException("Missing question and response for previous", stream);
				}
				Vertex previous = null;
				if (line.startsWith("#")) {
					line = line.substring(1, line.length());
					if (!Utils.isAlphaNumeric(line)) {
						throw new SelfParseException("A intent label must be a single alpha numeric string with no spaces (use - for a space) - " + line, stream);
					}
					previous = network.createVertex(new Primitive(line));
				} else {
					previous = network.createSentence(line);
				}
				if (pin) {
					SelfCompiler.getCompiler().pin(previous);
				}
				if (isDefault) {
					Vertex language = network.createVertex(Language.class);
					Language.addSentencePreviousMeta(language, question, previous, true, network);
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentencePreviousMeta(context, answer, previous, true, network);
				}
			} else if (command.equalsIgnoreCase("label") || command.equalsIgnoreCase("intent")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for intent label", stream);
				}
				if (line.startsWith("#")) {
					line = line.substring(1, line.length());
				}
				if (!Utils.isAlphaNumeric(line)) {
					throw new SelfParseException("An intent label must be a single alpha numeric string with no spaces (use - for a space) - " + line, stream);
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
					throw new SelfParseException("Missing question for on repeat", stream);
				}
				Vertex repeat = network.createSentence(line);
				if (pin) {
					SelfCompiler.getCompiler().pin(repeat);
				}
				if (answer == null) {
					question.addRelationship(Primitive.ONREPEAT, repeat);
				} else {
					answer.addRelationship(Primitive.ONREPEAT, repeat);
				}
			} else if (command.equalsIgnoreCase("no repeat")) {
				if (question == null) {
					throw new SelfParseException("Missing question for no repeat", stream);
				}
				if (answer == null) {
					question.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				} else {
					answer.addRelationship(Primitive.REQUIRE, Primitive.NOREPEAT);
				}
			} else if (command.equalsIgnoreCase("topic")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for topic", stream);
				}
				if (isWord) {
					question.addRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
					Relationship relationship = question.getRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
					if (relationship != null) {
						question.internalRemoveRelationship(relationship);
					}
				} else {
					if (answer == null) {
						if (isDefault) {
							Vertex language = network.createVertex(Language.class);
							Language.addSentenceTopicMeta(language, question, line, false, null, network);
						} else if (isGreeting) {
							Vertex language = network.createVertex(Language.class);
							Language.addSentenceTopicMeta(language, question, Primitive.GREETING, line, false, null, network);
						}
					} else {
						Vertex context = checkConversationMeta(conversationMeta, question, network);
						Language.addSentenceTopicMeta(context, answer, line, false, null, network);
					}
				}
			} else if (command.equalsIgnoreCase("exclusive topic")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for topic", stream);
				}
				if (isWord) {
					question.addRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
					question.addRelationship(Primitive.ASSOCIATED, Primitive.EXCLUSIVE);
				} else {
					if (answer == null) {
						if (isDefault) {
							Vertex language = network.createVertex(Language.class);
							Language.addSentenceTopicMeta(language, question, line, false, true, network);
						} else if (isGreeting) {
							Vertex language = network.createVertex(Language.class);
							Language.addSentenceTopicMeta(language, question, Primitive.GREETING, line, false, true, network);
						}
					} else {
						Vertex context = checkConversationMeta(conversationMeta, question, network);
						Language.addSentenceTopicMeta(context, answer, line, false, true, network);
					}
				}
			} else if (command.equalsIgnoreCase("require topic")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for topic", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceTopicMeta(language, question, line, true, null, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceTopicMeta(language, question, Primitive.GREETING, line, true, null, network);
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceTopicMeta(context, answer, line, true, null, network);
				}
			} else if (command.equalsIgnoreCase("command")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for command", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceCommandMeta(language, question, line, pin, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceCommandMeta(language, question, Primitive.GREETING, line, pin, network);
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceCommandMeta(context, answer, line, pin, network);
				}
			} else if (command.equalsIgnoreCase("think")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for think", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceThinkMeta(language, question, line, pin, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceThinkMeta(language, question, Primitive.GREETING, line, pin, network);
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceThinkMeta(context, answer, line, pin, network);
				}
			} else if (command.equalsIgnoreCase("condition")) {
				if (question == null) {
					throw new SelfParseException("Missing phrase for condition", stream);
				}
				if (answer == null) {
					if (isDefault) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceConditionMeta(language, question, line, pin, network);
					} else if (isGreeting) {
						Vertex language = network.createVertex(Language.class);
						Language.addSentenceConditionMeta(language, question, Primitive.GREETING, line, pin, network);
					}
				} else {
					Vertex context = checkConversationMeta(conversationMeta, question, network);
					Language.addSentenceConditionMeta(context, answer, line, pin, network);
				}
			} else if (command.equalsIgnoreCase("confidence")) {
				if (question == null || answer == null) {
					throw new SelfParseException("Missing question and response for confidence", stream);
				}
				Vertex context = checkConversationMeta(conversationMeta, question, network);
				Language.setConfidence(context, answer, Primitive.RESPONSE, line, network);
			} else {
				isDefault = false;
				isGreeting = false;
				isWord = false;
				hasSentiment = false;
				Vertex sentence = null;
				if (originalLine.startsWith("#")) {
					originalLine = originalLine.substring(1, originalLine.length());
					if (!Utils.isAlphaNumeric(originalLine)) {
						throw new SelfParseException("An intent label must be a single alpha numeric string with no spaces (use - for a space) - " + originalLine, stream);
					}
					sentence = network.createVertex(new Primitive(originalLine));
					if (!sentence.hasRelationship(Primitive.INSTANTIATION, Primitive.LABEL)) {
						bot.log(this, "Missing intent label", Level.INFO, originalLine);
					}
				} else if (command.equalsIgnoreCase("question")) {
					sentence = network.createSentence(line);
				} else if (command.equalsIgnoreCase("response")) {
					sentence = network.createSentence(line);
				} else if (command.equalsIgnoreCase("pattern")) {
					sentence = network.createSentence("Pattern(\"" + line + "\")");
				} else if (command.equalsIgnoreCase("template")) {
					sentence = network.createSentence("Template(\"" + line + "\")");
				} else {
					sentence = network.createSentence(originalLine);
				}
				if (pin) {
					SelfCompiler.getCompiler().pin(sentence);
				}
				if (question == null) {
					question = sentence;
				} else {
					answer = sentence;
					if (autoReduce && !question.getDataValue().startsWith("Pattern(")) {
						question = network.createSentence(Utils.reduce(question.getDataValue()));
						if (pin) {
							question.setPinned(true);
						}
					}
					if (conversationMeta == null) {
						lastResponseRelationship = Language.addResponse(question, answer, null, null, null, 1.0f, network);
					} else {
						// Conversations are defined by the NEXT relationship on the meta.
						Relationship next = conversationMeta.addRelationship(Primitive.NEXT, question);
						Vertex meta = network.createMeta(next);
						lastResponseRelationship = meta.addWeakRelationship(Primitive.RESPONSE, answer, 1.0f);
					}
				}
			}
		}
		network.save();
	}
	
	/**
	 * Check if a nest conversation and create a meta to add the response to in place of the question.
	 */
	public Vertex checkConversationMeta(Vertex conversationMeta, Vertex question, Network network) {
		if (conversationMeta == null) {
			return question;
		} else {
			Relationship relationship = conversationMeta.addRelationship(Primitive.NEXT, question);
			Vertex meta = network.createMeta(relationship);
			return meta;
		}
	}

	/**
	 * Process the log file for list a of question/answers.
	 * Input each message, in a listening only mode.
	 */
	public void processCSVLog(String log, boolean pin, boolean autoReduce, Bot bot) {
		bot.log(this, "Loading csv log", Level.INFO, log.length());
		TextStream stream = new TextStream(log);
		Network network = bot.memory().newMemory();
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
					network = bot.memory().newMemory();
				}
			}
			// Allow either ',' or '","' separators.
			boolean quotes = line.contains("\"");
			bot.log(this, "Processing csv line", Level.INFO, line);
			// "questions","answer","topic"
			// "What is this? What's this?","This is Open Bot.","Bot"
			TextStream lineStream = new TextStream(line);
			if (quotes) {
				lineStream.skipTo('"');
				lineStream.skip();
			}
			if (lineStream.atEnd()) {
				bot.log(this, "Expecting \" character", Level.WARNING, line);
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
				bot.log(this, "Expecting \",\" characters", Level.WARNING, line);
				continue;
			}
			bot.log(this, "Processing csv question", Level.INFO, questionText);
			String answerText = null;
			if (quotes) {
				answerText = lineStream.upToAll("\"").trim();
			} else {
				answerText = lineStream.upTo(',').trim();
			}
			lineStream.skip();
			bot.log(this, "Processing csv answer", Level.INFO, answerText);
			answer = network.createSentence(answerText);
			if (pin) {
				SelfCompiler.getCompiler().pin(answer);
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
					if (autoReduce && !questionText.startsWith("Pattern(")) {
						questionText = Utils.reduce(questionText);
					}
					question = network.createSentence(questionText);
					if (pin) {
						SelfCompiler.getCompiler().pin(question);
					}
					Language.addResponse(question, answer, topic, keywords, required, 0.9f, network);
				}
			}
			network.save();
		}
		network.save();
	}

}
