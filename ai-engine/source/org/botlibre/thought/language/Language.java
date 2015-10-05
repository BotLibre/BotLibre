// Copyright Paphus Solutions, all rights reserved.
package org.botlibre.thought.language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.aiml.AIMLParser;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.TextData;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfExecutionException;
import org.botlibre.self.SelfParseException;
import org.botlibre.sense.context.Context;
import org.botlibre.sense.http.Http;
import org.botlibre.thought.BasicThought;
import org.botlibre.util.Utils;

/**
 * Processes words to determine meaning and response.
 */

public class Language extends BasicThought {
	public static float CONVERSATION_MATCH_PERCENTAGE = 0.5f;
	public static float DISCUSSION_MATCH_PERCENTAGE = 0.9f;
	public static int COVERSATIONAL_RESPONSE_DELAY = 1000;
	public static int MAX_STATE_PROCESS = 10000;
	public static int MAX_RESPONSE_PROCESS = 1000;
	public static int MAX_FILE_SIZE = 10000000;  // 10 meg
	public static int MAX_DEPTH = 50;
	public static int MAX_STACK = 500;
	public static boolean PROCESS_HTTP = true;

	protected boolean enableEmote = true;
	protected boolean enableResponseMatch = true;
	protected boolean checkExactMatchFirst = true;
	protected boolean fixFormulaCase = true;
	protected boolean learnGrammar = true;
	protected float conversationMatchPercentage = CONVERSATION_MATCH_PERCENTAGE;
	protected float discussionMatchPercentage = DISCUSSION_MATCH_PERCENTAGE;
	protected float learningRate = 0.5f;
	protected Boolean allowLearning;

	protected int maxStateProcess = MAX_STATE_PROCESS;
	protected int maxResponseMatchProcess = MAX_RESPONSE_PROCESS;
	protected String language = null;
	protected int recursiveInputDepth;
	protected int recursiveFormulaDepth;
	protected long startTime;
	protected boolean abort;

	/** Store the state machine used for the last response (if any). */
	protected Long lastStateMachineId;
	/** Store the state used for the last response (if any). */
	protected Long lastStateId;
	/** Store the quotient used for the last response (if any). */
	protected Long lastQuotientId;
	/** Store the meta relationship for last response (if any). */
	protected Long lastResponseMetaId;
	/** Store if a mimic was used. */
	protected boolean wasMimic;
	/** Store the learning mode. */
	protected LearningMode learningMode = LearningMode.Everyone;
	/** Store the correction mode. */
	protected CorrectionMode correctionMode = CorrectionMode.Everyone;
	
	/** Optimize state loading. */
	protected Set<Long> loadedStates = new HashSet<Long>();
	
	/**
	 * Defines the various language conversational states.
	 */
	public enum LanguageState {
		Associate, Conversational, Listening, ListeningOnly, Discussion, Answering
	}
	/**
	 * Defines who the bot will learn from.
	 */
	public enum LearningMode {
		Disabled, Administrators, Users, Everyone
	}
	/**
	 * Defines who the bot will allow corrections from.
	 */
	public enum CorrectionMode {
		Disabled, Administrators, Users, Everyone
	}
	
	/**
	 * Add the input to the conversation.
	 */
	public static void addToConversation(Vertex input, Vertex conversation) {
		input.addRelationship(Primitive.CONVERSATION, conversation);
		Vertex previous = conversation.lastRelationship(Primitive.INPUT);
		if (previous != null) {
			previous.addRelationship(Primitive.NEXT, input);
			input.addRelationship(Primitive.PREVIOUS, previous);
		}
		conversation.addRelationship(Primitive.INPUT, input, Integer.MAX_VALUE);
	}
	
	/**
	 * Add the sentence from the previous input to the relationships response meta info.
	 */
	public static void addSentencePreviousMeta(Relationship relationship, Vertex previousQuestionInput, Network network) {
		// Associate previous question as meta info.
		if (previousQuestionInput != null) {
			Vertex previousQuestion = previousQuestionInput.getRelationship(Primitive.INPUT);
			if (previousQuestion != null) {
				Vertex meta = network.createMeta(relationship);
				meta.addRelationship(Primitive.PREVIOUS, previousQuestion);
			}
		}
	}
	
	/**
	 * Add the keywords as required keywords for a response match to the question meta.
	 */
	public static void addSentenceKeyWordsMeta(Vertex question, Vertex answer, String keywords, Network network) {
		if (keywords == null || keywords.trim().isEmpty()) {
			clearSentenceMeta(question, answer, Primitive.KEYWORD, network);
		} else {
			Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
			if (relationship != null) {
				Vertex meta = network.createMeta(relationship);
				meta.internalRemoveRelationships(Primitive.KEYWORD);
				List<String> words = Utils.getWords(keywords);
				for (String keyword : words) {
					Vertex word = network.createWord(keyword);
					meta.addRelationship(Primitive.KEYWORD, word);
					word.addRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
					word.addRelationship(Primitive.KEYQUESTION, question);

					Vertex lowercase = network.createWord(keyword.toLowerCase());
					if (lowercase != word) {
						meta.addRelationship(Primitive.KEYWORD, lowercase);
						lowercase.addRelationship(Primitive.INSTANTIATION, Primitive.KEYWORD);
						lowercase.addRelationship(Primitive.KEYQUESTION, question);
					}
				}
			}
			network.checkReduction(question);
			Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
			if (synonyms != null) {
				for (Relationship synonym : synonyms) {
					relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
					if (relationship != null) {
						Vertex meta = network.createMeta(relationship);
						meta.internalRemoveRelationships(Primitive.KEYWORD);
						List<String> words = Utils.getWords(keywords);
						for (String keyword : words) {
							Vertex word = network.createWord(keyword);
							meta.addRelationship(Primitive.KEYWORD, word);
							word.addRelationship(Primitive.KEYQUESTION, question);

							Vertex lowercase = network.createWord(keyword.toLowerCase());
							if (lowercase != word) {
								meta.addRelationship(Primitive.KEYWORD, lowercase);
								lowercase.addRelationship(Primitive.KEYQUESTION, question);
							}
						}

					}					
				}
			}
		}
	}
	
	/**
	 * Add the emotes to the response.
	 */
	public static void addSentenceEmotesMeta(Vertex question, Vertex answer, String emotes, Network network) {
		if (emotes == null || emotes.trim().isEmpty()) {
			clearSentenceMeta(question, answer, Primitive.EMOTION, network);
		} else {
			Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
			if (relationship != null) {
				Vertex meta = network.createMeta(relationship);
				meta.internalRemoveRelationships(Primitive.EMOTION);
				List<String> words = Utils.getWords(emotes);
				for (String emote : words) {
					EmotionalState.valueOf(emote.toUpperCase()).apply(meta);
				}
			}
			network.checkReduction(question);
			Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
			if (synonyms != null) {
				for (Relationship synonym : synonyms) {
					relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
					if (relationship != null) {
						Vertex meta = network.createMeta(relationship);
						meta.internalRemoveRelationships(Primitive.EMOTION);
						List<String> words = Utils.getWords(emotes);
						for (String emote : words) {
							EmotionalState.valueOf(emote.toUpperCase()).apply(meta);
						}
					}					
				}
			}
		}
	}
	
	/**
	 * Add the actions to the response.
	 */
	public static void addSentenceActionMeta(Vertex question, Vertex answer, String actions, Network network) {
		if (actions == null || actions.trim().isEmpty()) {
			clearSentenceMeta(question, answer, Primitive.ACTION, network);
		} else {
			Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
			if (relationship != null) {
				Vertex meta = network.createMeta(relationship);
				meta.internalRemoveRelationships(Primitive.ACTION);
				List<String> words = Utils.getWords(actions);
				for (String action : words) {
					if (!action.equals("none")) {
						meta.addRelationship(Primitive.ACTION, new Primitive(action));
					}
				}
			}
			network.checkReduction(question);
			Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
			if (synonyms != null) {
				for (Relationship synonym : synonyms) {
					relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
					if (relationship != null) {
						Vertex meta = network.createMeta(relationship);
						meta.internalRemoveRelationships(Primitive.ACTION);
						List<String> words = Utils.getWords(actions);
						for (String action : words) {
							if (!action.equals("none")) {
								meta.addRelationship(Primitive.ACTION, new Primitive(action));
							}
						}
					}					
				}
			}
		}
	}
	
	/**
	 * Add the actions to the response.
	 */
	public static void addSentencePoseMeta(Vertex question, Vertex answer, String poses, Network network) {
		if (poses == null || poses.trim().isEmpty()) {
			clearSentenceMeta(question, answer, Primitive.POSE, network);
		} else {
			Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
			if (relationship != null) {
				Vertex meta = network.createMeta(relationship);
				meta.internalRemoveRelationships(Primitive.POSE);
				List<String> words = Utils.getWords(poses);
				for (String pose : words) {
					if (!pose.equals("none")) {
						meta.addRelationship(Primitive.POSE, new Primitive(pose));
					}
				}
			}
			network.checkReduction(question);
			Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
			if (synonyms != null) {
				for (Relationship synonym : synonyms) {
					relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
					if (relationship != null) {
						Vertex meta = network.createMeta(relationship);
						meta.internalRemoveRelationships(Primitive.POSE);
						List<String> words = Utils.getWords(poses);
						for (String pose : words) {
							if (!pose.equals("none")) {
								meta.addRelationship(Primitive.POSE, new Primitive(pose));
							}
						}
					}					
				}
			}
		}
	}
	
	/**
	 * Add the previous for a response match to the question meta.
	 */
	public static void addSentencePreviousMeta(Vertex question, Vertex answer, Vertex previous, boolean require, Network network) {
		Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
		if (relationship != null) {
			Vertex meta = relationship.getMeta();
			if (previous != null) {
				meta = network.createMeta(relationship);
				meta.addRelationship(Primitive.PREVIOUS, previous);
			}
			if (meta != null) {
				if (require) {
					meta.addRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);					
				} else {
					Relationship required = meta.getRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);
					if (required != null) {
						relationship.getMeta().internalRemoveRelationship(required);
					}				
				}
			}
		}
		network.checkReduction(question);
		Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
		if (synonyms != null) {
			for (Relationship synonym : synonyms) {
				relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
				if (relationship != null) {
					Vertex meta = relationship.getMeta();
					if (previous != null) {
						meta = network.createMeta(relationship);
						meta.addRelationship(Primitive.PREVIOUS, previous);
					}
					if (meta != null) {
						if (require) {
							meta.addRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);					
						} else {
							Relationship required = meta.getRelationship(Primitive.REQUIRE, Primitive.PREVIOUS);
							if (required != null) {
								relationship.getMeta().internalRemoveRelationship(required);
							}				
						}
					}
				}					
			}
		}
	}
	
	/**
	 * Add the keywords as required keywords for a response match to the question meta.
	 */
	public static void addSentenceRequiredMeta(Vertex question, Vertex answer, String required, Network network) {
		if (required == null || required.trim().isEmpty()) {
			clearSentenceMeta(question, answer, Primitive.REQUIRED, network);
		} else {
			Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
			if (relationship != null) {
				Vertex meta = network.createMeta(relationship);
				meta.internalRemoveRelationships(Primitive.REQUIRED);
				List<String> words = Utils.getWords(required);
				for (String keyword : words) {
					Vertex word = network.createWord(keyword);
					meta.addRelationship(Primitive.REQUIRED, word);
					word.addRelationship(Primitive.QUESTION, question);
				}
			}
			network.checkReduction(question);
			Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
			if (synonyms != null) {
				for (Relationship synonym : synonyms) {
					relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
					if (relationship != null) {
						Vertex meta = network.createMeta(relationship);
						meta.internalRemoveRelationships(Primitive.REQUIRED);
						List<String> words = Utils.getWords(required);
						for (String keyword : words) {
							meta.addRelationship(Primitive.REQUIRED, network.createWord(keyword));
						}
					}					
				}
			}
		}
	}
	
	/**
	 * Add the topic as a desired topic for a response match to the question meta.
	 */
	public static void addSentenceTopicMeta(Vertex question, Vertex answer, String topic, Network network) {
		if (topic == null || topic.trim().isEmpty()) {
			clearSentenceMeta(question, answer, Primitive.TOPIC, network);
		} else {
			Vertex topicFragment = network.createFragment(topic);
			topicFragment.addRelationship(Primitive.INSTANTIATION, Primitive.TOPIC);
			network.createVertex(Primitive.TOPIC).addRelationship(Primitive.INSTANCE, topicFragment);
			topicFragment.addRelationship(Primitive.QUESTION, question);
			Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
			if (relationship != null) {
				Vertex meta = network.createMeta(relationship);
				meta.setRelationship(Primitive.TOPIC, topicFragment);
			}
			network.checkReduction(question);
			Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
			if (synonyms != null) {
				for (Relationship synonym : synonyms) {
					relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
					if (relationship != null) {
						Vertex meta = network.createMeta(relationship);
						meta.setRelationship(Primitive.TOPIC, topicFragment);
					}
				}
			}
		}
	}
	
	/**
	 * Clear any relations in the meta of the type.
	 */
	public static void clearSentenceMeta(Vertex question, Vertex answer, Primitive type, Network network) {
		Relationship relationship = question.getRelationship(Primitive.RESPONSE, answer);
		if (relationship != null && relationship.hasMeta()) {
			relationship.getMeta().internalRemoveRelationships(type);				
		}
		network.checkReduction(question);
		Collection<Relationship> synonyms = question.getRelationships(Primitive.SYNONYM);
		if (synonyms != null) {
			for (Relationship synonym : synonyms) {
				relationship = synonym.getTarget().getRelationship(Primitive.RESPONSE, answer);
				if (relationship != null && relationship.hasMeta()) {
					relationship.getMeta().internalRemoveRelationships(type);						
				}
			}
		}
	}

	/**
	 * Add the sentence from the previous input to the relationships response meta info.
	 */
	public static boolean addCorrection(Vertex originalQuestionInput, Vertex originalQuestion, Vertex wrongResponseInput, Vertex correction, Vertex previousQuestionInput, Network network) {
		boolean wasCorrect= false;
		Relationship relationship = null;
		if (wrongResponseInput != null) {
			Vertex wrongResponse = wrongResponseInput.mostConscious(Primitive.INPUT);
			// If correcting with same answer, then don't uncorrect.
			if (wrongResponse != correction) {
				relationship = originalQuestion.getRelationship(Primitive.RESPONSE, wrongResponse);
				// Could be a wrong response, or just a wrong response in the context.
				// First mark wrong in context, then mark wrong in general.
				if ((relationship == null) || (relationship.getCorrectness() < 0.5)) {
					originalQuestion.removeRelationship(Primitive.RESPONSE, wrongResponse);
					network.checkReduction(originalQuestion);
					originalQuestion.inverseAssociateAll(Primitive.SYNONYM, wrongResponse, Primitive.RESPONSE);
				} else {
					relationship.setCorrectness(relationship.getCorrectness() / 2);
					originalQuestionInput.removeRelationship(Primitive.RESPONSE, wrongResponseInput);
					// Dissociate previous question as meta info.
					removeSentencePreviousMeta(relationship, previousQuestionInput, network);
				}
			} else {
				wasCorrect = true;
			}
			Vertex input = wrongResponseInput.copy();
			input.addRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION);
			input.setRelationship(Primitive.INPUT, correction);
			relationship = network.getBot().mind().getThought(Comprehension.class).checkFormula(input, network);
			if (relationship != null) {
				correction = relationship.getTarget();
			}
		}
		relationship = originalQuestion.addRelationship(Primitive.RESPONSE, correction);
		if (!originalQuestion.instanceOf(Primitive.PATTERN)) {
			originalQuestion.associateAll(Primitive.WORD, originalQuestion, Primitive.QUESTION);
		}
		network.checkReduction(originalQuestion);
		originalQuestion.associateAll(Primitive.SYNONYM, correction, Primitive.RESPONSE);
		correction.addRelationship(Primitive.QUESTION, originalQuestion);
		originalQuestion.setPinned(true);
		correction.setPinned(true);
		// Associate previous question as meta info.
		addSentencePreviousMeta(relationship, previousQuestionInput, network);
		return wasCorrect;
	}
	
	/**
	 * Remove the sentence from the previous input to the relationships response meta info.
	 */
	public static void removeSentencePreviousMeta(Relationship relationship, Vertex previousQuestionInput, Network network) {
		// Associate previous question as meta info.
		if (previousQuestionInput != null) {
			Vertex previousQuestion = previousQuestionInput.getRelationship(Primitive.INPUT);
			if (previousQuestion != null) {
				Vertex meta = network.createMeta(relationship);
				meta.removeRelationship(Primitive.PREVIOUS, previousQuestion);
			}
		}
	}
	
	/**
	 * Create a new thought.
	 */
	public Language() {
	}

	@Override
	public void awake() {
		Network memory = getBot().memory().newMemory();
		Vertex language = memory.createVertex(getPrimitive());
		Vertex property = language.getRelationship(Primitive.EMOTE);
		if (property != null) {
			setEnableEmote((Boolean)property.getData());
		}
		property = language.getRelationship(Primitive.LANGUAGE);
		if (property != null) {
			setLanguage((String)property.getData());
		}
		property = language.getRelationship(Primitive.LEARNING);
		if (property != null) {
			setLearningMode(LearningMode.valueOf((String)property.getData()));
		}
		property = language.getRelationship(Primitive.MAXRESPONSEMATCHPROCESS);
		if (property != null) {
			setMaxResponseMatchProcess((Integer)property.getData());
		}
		property = language.getRelationship(Primitive.MAXSTATEPROCESS);
		if (property != null) {
			setMaxStateProcess((Integer)property.getData());
		}
		property = language.getRelationship(Primitive.CONVERSATIONMATCHPERCENTAGE);
		if (property != null) {
			setConversationMatchPercentage((Float)property.getData());
		}
		property = language.getRelationship(Primitive.DISCUSSIONMATCHPERCENTAGE);
		if (property != null) {
			setDiscussionMatchPercentage((Float)property.getData());
		}
		property = language.getRelationship(Primitive.LEARNINGRATE);
		if (property != null) {
			setLearningRate((Float)property.getData());
		}
		property = language.getRelationship(Primitive.CHECKEXACTMATCHFIRST);
		if (property != null) {
			setCheckExactMatchFirst((Boolean)property.getData());
		}
		property = language.getRelationship(Primitive.ENABLERESPONSEMATCH);
		if (property != null) {
			setEnableResponseMatch((Boolean)property.getData());
		}
		property = language.getRelationship(Primitive.LEARNGRAMMAR);
		if (property != null) {
			setLearnGrammar((Boolean)property.getData());
		}
		property = language.getRelationship(Primitive.FIXFORMULACASE);
		if (property != null) {
			setFixFormulaCase((Boolean)property.getData());
		}
		property = language.getRelationship(Primitive.CORRECTION);
		if (property != null) {
			if (property.getData() instanceof Boolean) {
				if ((Boolean)property.getData()) {
					setCorrectionMode(CorrectionMode.Everyone);
				} else {
					setCorrectionMode(CorrectionMode.Administrators);					
				}
			} else {
				setCorrectionMode(CorrectionMode.valueOf((String)property.getData()));
			}
		}
	}

	public CorrectionMode getCorrectionMode() {
		return correctionMode;
	}

	public void setCorrectionMode(CorrectionMode correctionMode) {
		this.correctionMode = correctionMode;
	}

	public LearningMode getLearningMode() {
		return learningMode;
	}

	public void setLearningMode(LearningMode learningMode) {
		this.learningMode = learningMode;
	}

	public boolean getEnableResponseMatch() {
		return enableResponseMatch;
	}

	public void setEnableResponseMatch(boolean enableResponseMatch) {
		this.enableResponseMatch = enableResponseMatch;
	}

	public boolean getCheckExactMatchFirst() {
		return checkExactMatchFirst;
	}

	public void setCheckExactMatchFirst(boolean checkExactMatchFirst) {
		this.checkExactMatchFirst = checkExactMatchFirst;
	}

	public float getConversationMatchPercentage() {
		return conversationMatchPercentage;
	}

	public void setConversationMatchPercentage(float conversationMatchPercentage) {
		this.conversationMatchPercentage = conversationMatchPercentage;
	}
	
	public float getLearningRate() {
		return learningRate;
	}

	public void setLearningRate(float learningRate) {
		this.learningRate = learningRate;
	}

	public float getDiscussionMatchPercentage() {
		return discussionMatchPercentage;
	}

	public void setDiscussionMatchPercentage(float discussionMatchPercentage) {
		this.discussionMatchPercentage = discussionMatchPercentage;
	}

	public int getMaxStateProcess() {
		return maxStateProcess;
	}

	public void setMaxStateProcess(int maxStateProcess) {
		this.maxStateProcess = maxStateProcess;
	}

	public int getMaxResponseMatchProcess() {
		return maxResponseMatchProcess;
	}

	public void setMaxResponseMatchProcess(int maxResponseMatchProcess) {
		this.maxResponseMatchProcess = maxResponseMatchProcess;
	}

	public boolean getFixFormulaCase() {
		return fixFormulaCase;
	}

	public void setFixFormulaCase(boolean fixFormulaCase) {
		this.fixFormulaCase = fixFormulaCase;
	}

	public boolean getLearnGrammar() {
		return learnGrammar;
	}

	public void setLearnGrammar(boolean learnGrammar) {
		this.learnGrammar = learnGrammar;
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex language = memory.createVertex(getPrimitive());
		language.unpinChildren();
		
		language.setRelationship(Primitive.CORRECTION, memory.createVertex(getCorrectionMode().name()));
		language.setRelationship(Primitive.EMOTE, memory.createVertex(getEnableEmote()));
		language.setRelationship(Primitive.LANGUAGE, memory.createVertex(getLanguage()));
		language.setRelationship(Primitive.LEARNING, memory.createVertex(getLearningMode().name()));
		language.setRelationship(Primitive.MAXRESPONSEMATCHPROCESS, memory.createVertex(getMaxResponseMatchProcess()));
		language.setRelationship(Primitive.MAXSTATEPROCESS, memory.createVertex(getMaxStateProcess()));
		language.setRelationship(Primitive.DISCUSSIONMATCHPERCENTAGE, memory.createVertex(getDiscussionMatchPercentage()));
		language.setRelationship(Primitive.LEARNINGRATE, memory.createVertex(getLearningRate()));
		language.setRelationship(Primitive.CONVERSATIONMATCHPERCENTAGE, memory.createVertex(getConversationMatchPercentage()));
		language.setRelationship(Primitive.ENABLERESPONSEMATCH, memory.createVertex(getEnableResponseMatch()));
		language.setRelationship(Primitive.CHECKEXACTMATCHFIRST, memory.createVertex(getCheckExactMatchFirst()));
		language.setRelationship(Primitive.LEARNGRAMMAR, memory.createVertex(getLearnGrammar()));
		language.setRelationship(Primitive.FIXFORMULACASE, memory.createVertex(getFixFormulaCase()));
		
		language.pinChildren();
		memory.save();
	}

	public Map<Primitive, Object> clearVoiceProperties() {
		Map<Primitive, Object> properties = new HashMap<Primitive, Object>();
		Network memory = getBot().memory().newMemory();
		Vertex language = memory.createVertex(getPrimitive());
		Vertex property = language.getRelationship(Primitive.VOICE);
		if (property != null) {
			properties.put(Primitive.VOICE, property.getData());
			property.setPinned(false);
		}
		property = language.getRelationship(Primitive.NATIVEVOICE);
		if (property != null) {
			properties.put(Primitive.NATIVEVOICE, property.getData());
			property.setPinned(false);
		}
		property = language.getRelationship(Primitive.NATIVEVOICENAME);
		if (property != null) {
			properties.put(Primitive.NATIVEVOICENAME, property.getData());
			property.setPinned(false);
		}
		property = language.getRelationship(Primitive.PITCH);
		if (property != null) {
			properties.put(Primitive.PITCH, property.getData());
			property.setPinned(false);
		}
		property = language.getRelationship(Primitive.SPEECHRATE);
		if (property != null) {
			properties.put(Primitive.SPEECHRATE, property.getData());
			property.setPinned(false);
		}
		property = language.getRelationship(Primitive.LANGUAGE);
		if (property != null) {
			properties.put(Primitive.LANGUAGE, property.getData());
		}
		
		language.internalRemoveRelationships(Primitive.VOICE);
		language.internalRemoveRelationships(Primitive.NATIVEVOICE);
		language.internalRemoveRelationships(Primitive.NATIVEVOICENAME);
		language.internalRemoveRelationships(Primitive.PITCH);
		language.internalRemoveRelationships(Primitive.SPEECHRATE);
		
		memory.save();
		return properties;
	}
	
	/**
	 * Stop analysing network.
	 */
	public void stop() {
		pool();
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
		this.allowLearning = null;
	}
		
	public Boolean getAllowLearning() {
		return allowLearning;
	}

	public void setAllowLearning(Boolean allowLearning) {
		this.allowLearning = allowLearning;
	}

	/**
	 * Analyse the active memory for language.
	 */
	public void think() {
		if (isStopped() || !isEnabled()) {
			return;
		}
		Network network = getShortTermMemory();
		List<Vertex> activeMemory = getBot().memory().getActiveMemory();
		for (int i = 0; i < activeMemory.size(); i++) {
			Vertex vertex = network.createVertex(activeMemory.get(i));
			log("Processing", Level.FINER, vertex);
			if (vertex.instanceOf(Primitive.INPUT)) {
				Vertex input = vertex;
				List<Vertex> sentences = new ArrayList<Vertex>();
				List<Vertex> responses = new ArrayList<Vertex>();
				Vertex inputValue = input.getRelationship(Primitive.INPUT);
				if (inputValue != null) {
					for (Vertex sentence : input.orderedRelations(Primitive.INPUT)) {
						if (sentence.instanceOf(Primitive.SENTENCE)) {
							// Check if the input is a paragraph.
							Vertex paragraph = network.createParagraph(sentence);
							if (paragraph.instanceOf(Primitive.PARAGRAPH)) {
								sentences.addAll(paragraph.orderedRelations(Primitive.SENTENCE));
							} else {
								sentences.add(paragraph);
							}
						} else if (sentence.instanceOf(Primitive.PARAGRAPH)) {
							sentences.addAll(sentence.orderedRelations(Primitive.SENTENCE));							
						}
					}
				}
				List<Relationship> targets = vertex.orderedRelationships(Primitive.TARGET);
				Vertex target = null;
				if (targets != null && (targets.size() == 1)) {
					target = targets.get(0).getTarget();
				}
				Vertex speaker = input.mostConscious(Primitive.SPEAKER);
				Vertex self = network.createVertex(Primitive.SELF);
				Vertex inputSense = input.mostConscious(Primitive.SENSE);
				Vertex conversation = input.mostConscious(Primitive.CONVERSATION);
				boolean correction = input.hasRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION);
				boolean offended = input.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENDED);
				boolean newConversation = (inputValue != null) && (inputValue.is(Primitive.NULL));
				LanguageState state = LanguageState.Answering;
				Sense sense = null;
				if (inputSense != null) {
					sense = getBot().awareness().getSense(((Primitive)inputSense.getData()).getIdentity());
					if (sense != null) {
						state = sense.getLanguageState();
					}
				}
				// Create output.
				Vertex output = network.createInstance(Primitive.INPUT);
				try {
					Vertex response = null;
					int index = 0;
					Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
					SelfCompiler.addGlobalVariables(input, null, network, variables);
					if (newConversation) {
						// If Correcting then this is a response to that previous question.
						response = processGreeting(input, conversation, network, state, variables);
						if (response != null) {
							responses.add(response);
						}
					} else {
						for (Vertex sentence : sentences) {
							checkQuestion(sentence, network);
							if (PROCESS_HTTP) {
								// TODO make this occur in discovery
								//processHttp(sentence, network);
							}
							log("Processing sentence", Level.FINE, sentence, speaker, targets);
							if (offended) {
								// If offensive then remove the last sentence.
								processOffensive(input, speaker, target, conversation, network);
							}
							if (correction) {
								// If Correcting then this is a response to that previous question.
								response = processCorrection(input, sentence, speaker, target, conversation, network);
							} else if (state == LanguageState.Associate) {
								// Associate the context selection with the sentence.
								response = processAssociation(sentence, network);
							} else if ((state == LanguageState.Listening) || (state == LanguageState.ListeningOnly)) {
								processListening(input, sentence, speaker, conversation, targets, network, state);
								// If target is self, then give an answer.
								if ((state == LanguageState.Listening) && (target == self)) {
									response = processConversational(input, sentence, conversation, variables, network, state);
								} else {
									// Associate response, process understanding, but don't respond.
									processUnderstanding(input, sentence, this.conversationMatchPercentage, variables, network);
									return;
								}
							} else if (state == LanguageState.Discussion) {
								processListening(input, sentence, speaker, conversation, targets, network, state);
								// If target is self, then give an answer.
								if (target == self) {
									response = processConversational(input, sentence, conversation, variables, network, state);							
								} else if ((targets == null) || targets.isEmpty() || (targets.size() > 1)) {
									// Process anything to a group, or to no one as a discussion.
									response = processDiscussion(input, sentence, conversation, variables, network);
								} else {
									processUnderstanding(input, sentence, this.conversationMatchPercentage, variables, network);
									log("Discussion response to other", Level.FINE, targets);
								}
							} else if (state == LanguageState.Conversational) {
								processListening(input, sentence, speaker, conversation, targets, network, state);
								response = processConversational(input, sentence, conversation, variables, network, state);
							} else if (state == LanguageState.Answering) {
								processListening(input, sentence, speaker, conversation, targets, network, state);
								response = processConversational(input, sentence, conversation, variables, network, state);
							}
							// Check for formula and transpose
							if ((response != null) && response.instanceOf(Primitive.FORMULA)) {
								log("Response is formula", Level.FINE, response);
								Vertex result = evaluateFormula(response, variables, network);
								if (result == null) {
									log("Formula cannot be evaluated", Level.FINE, response);
									response = null;
								} else {
									response = getWord(result, network);
								}
							}
							if (response == null || (!(response.getData() instanceof String) && !response.instanceOf(Primitive.PARAGRAPH))) {
								// Answering must respond.
								if (state == LanguageState.Answering) {
									response = sentence;
								} else {
									continue;
								}
							}
							log("Response", Level.INFO, response, speaker, conversation);
							index++;
							if ((sentences.size() == 1) || sentence.instanceOf(Primitive.QUESTION) || (index == sentences.size())) {
		
								if (!response.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
									Vertex meta = null;
									if (this.lastResponseMetaId != null) {
										meta = network.findById(this.lastResponseMetaId);
										this.lastResponseMetaId = null;
									}
									this.bot.mood().evaluateResponse(response, meta);
									this.bot.avatar().evaluateResponse(output, response, meta);
									responses.add(response);
								} else {
									response = checkDuplicateOrOffensiveResponse(response, sentence, conversation, input, variables, network, true, false);
									if (!response.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
										Vertex meta = null;										
										if (this.lastResponseMetaId != null) {
											meta = network.findById(this.lastResponseMetaId);
											this.lastResponseMetaId = null;
										}
										this.bot.mood().evaluateResponse(response, meta);
										this.bot.avatar().evaluateResponse(output, response, meta);
										responses.add(response);
									}
								}
							}
						}
					}
					if (responses.isEmpty()) {
						if (sense != null) {
							sense.notifyExceptionListeners(new BotException("No response"));
						}
						return;
					}
					if (inputSense != null) {
						// Associate input sense with output.
						output.addRelationship(Primitive.SENSE, inputSense);
					}
					if (conversation != null) {
						Language.addToConversation(output, conversation);
					}
					if (speaker != null) {
						output.addRelationship(Primitive.TARGET, speaker);
					}
					Vertex topic = input.mostConscious(Primitive.TOPIC);
					if (topic != null) {
						output.addRelationship(Primitive.TOPIC, topic);
					}
					if (input.hasRelationship(Primitive.ASSOCIATED, Primitive.WHISPER)) {
						output.addRelationship(Primitive.ASSOCIATED, Primitive.WHISPER);
					}
					if (!newConversation) {
						output.addRelationship(Primitive.QUESTION, input);
						input.addRelationship(Primitive.RESPONSE, output);
					}
					output.addRelationship(Primitive.SPEAKER, self);
					self.addRelationship(Primitive.INPUT, output);
					if (responses.get(0).getData() != null) {
						output.setName(responses.get(0).getData().toString());
					}
					if (responses.size() == 1) {
						if (this.wasMimic) {
							output.addRelationship(Primitive.MIMIC, input);
						}
						response = responses.get(0);
						output.addRelationship(Primitive.INPUT, response);
						Collection<Relationship> actions = response.getRelationships(Primitive.ACTION);
						if (actions != null) {
							for (Relationship relationship : actions) {
								output.addWeakRelationship(Primitive.ACTION, relationship.getTarget(), relationship.getCorrectness());
							}
						}
						Collection<Relationship> poses = response.getRelationships(Primitive.POSE);
						if (poses != null) {
							for (Relationship relationship : poses) {
								output.addWeakRelationship(Primitive.POSE, relationship.getTarget(), relationship.getCorrectness());
							}
						}
					} else {
						Vertex paragraph = network.createInstance(Primitive.PARAGRAPH);
						index = 0;
						for (Vertex each : responses) {
							paragraph.addRelationship(Primitive.SENTENCE, each, index);
							index++;
							Collection<Relationship> actions = each.getRelationships(Primitive.ACTION);
							if (actions != null) {
								for (Relationship relationship : actions) {
									output.addWeakRelationship(Primitive.ACTION, relationship.getTarget(), relationship.getCorrectness());
								}
							}
							Collection<Relationship> poses = response.getRelationships(Primitive.POSE);
							if (poses != null) {
								for (Relationship relationship : poses) {
									output.addWeakRelationship(Primitive.POSE, relationship.getTarget(), relationship.getCorrectness());
								}
							}
						}
						output.addRelationship(Primitive.INPUT, paragraph);	
					}
					this.wasMimic = false;
					// Record response time.
					output.setCreationDate(new Date());
					getBot().memory().save();
					// Allow the sense to output the response.
					getBot().awareness().output(output);
				} catch (RuntimeException exception) {
					if (sense != null) {
						sense.notifyExceptionListeners(exception);						
					}
					throw exception;
				}
			}
			this.recursiveInputDepth = 0;
			this.recursiveFormulaDepth = 0;
			this.startTime = 0;
			this.abort = false;
		}
	}
	
	/**
	 * Process the input and return the response.
	 */
	public Vertex input(Vertex input, Vertex sentence, Map<Vertex, Vertex> variables, Network network) {
		log("REDIRECT", Level.FINE, sentence, this.recursiveInputDepth);
		if (this.recursiveInputDepth > MAX_DEPTH) {
			log("Input", Level.WARNING, "Max recursive depth exceeded", this.recursiveInputDepth, sentence);
			return null;
		}
		this.recursiveInputDepth++;
		// Check for formula and transpose
		if (sentence.instanceOf(Primitive.FORMULA)) {
			Vertex result = evaluateFormula(sentence, variables, network);
			if (result == null) {
				log("Formula cannot be evaluated", Level.FINE, sentence);
				return null;
			} else {
				sentence = getWord(result, network);
			}
		}
		if (sentence.hasRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE) && (sentence.getData() == null)) {
			sentence = createSentenceText(sentence, network);
		} else if (!sentence.hasRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE) && (sentence.getData() instanceof String)) {
			sentence = network.createSentence((String)sentence.getData(), true, false, false);
		}
		Vertex conversation = input.getRelationship(Primitive.CONVERSATION);
		Vertex response = processConversational(input, sentence, conversation, variables, network, LanguageState.Answering);
		if ((response != null) && response.instanceOf(Primitive.FORMULA)) {
			log("Response is formula", Level.FINE, response);
			SelfCompiler.addGlobalVariables(input, sentence, network, variables);
			Vertex result = evaluateFormula(response, variables, network);
			if (result == null) {
				log("Formula cannot be evaluated", Level.FINE, response);
				response = null;
			} else {
				response = getWord(result, network);
			}
		}
		this.wasMimic = false;
		return response;
	}
	
	/**
	 * Evaluate the sentence formula in the context of the input.
	 */
	public Vertex evaluateFormula(Vertex formula, Map<Vertex, Vertex> variables, Network network) {
		try {
			//sentence.addRelationship(Primitive.FORMULA, formula);
			List<Vertex> relationships = formula.orderedRelations(Primitive.WORD);
			if (relationships == null) {
				return null;
			}
			List<Vertex> words = new ArrayList<Vertex>(relationships.size());
			boolean caseSensitive = false;
			for (Vertex word : relationships) {
				Vertex result = null;
				if (word.is(Primitive.WILDCARD)) {
					Vertex value = variables.get(word);
					if (value == null) {
						return null;
					}
					result = value;
				} else if (word.instanceOf(Primitive.VARIABLE)) {
					Vertex value = variables.get(word);
					if (value == null) {
						return null;
					}
					result = value;
				} else if (word.instanceOf(Primitive.EQUATION)) {
					Vertex quotient = word.applyQuotient(variables);
					variables.remove(network.createVertex(Primitive.RETURN));
					if (quotient == null) {
						return null;
					}
					while (quotient.instanceOf(Primitive.FORMULA)) {
						this.recursiveFormulaDepth++;
						if (this.recursiveFormulaDepth > MAX_DEPTH) {
							throw new SelfExecutionException(word, "Max recursive formula execution");
						}
						quotient = evaluateFormula(quotient, variables, network);
						this.recursiveFormulaDepth--;
						if (quotient == null) {
							return null;
						}
					}
					if (quotient.is(Primitive.NULL)) {
						return null;
					} else if (quotient.is(Primitive.RETURN)) {
						// think: just execute the equation.
					} else {
						result = quotient;
					}
					if (result != null && result.hasRelationship(Primitive.TYPE, Primitive.CASESENSITVE)) {
						caseSensitive = true;
					}
				} else {
					result = word;
				}
				if (result != null) {
					if (result.instanceOf(Primitive.WORD)) {
						words.add(result);
					} else if (result.instanceOf(Primitive.SENTENCE) || result.instanceOf(Primitive.FRAGMENT)) {
						if (relationships.size() == 1 && (result.instanceOf(Primitive.SENTENCE))) {
							return result;
						}
						Vertex space = null;
						Vertex sentenceWithSpaces = result;
						// Check if sentence has text or not.
						if (result.getData() instanceof String) {
							// It is has text, and doesn't contain any spaces, then it must be reparsed with spaces.
							if (!result.hasRelationship(Primitive.TYPE, Primitive.SPACE) && !result.hasRelationship(Primitive.WORD, Primitive.SPACE)
									&& ((result.getRelationships(Primitive.WORD) == null) || (result.getRelationships(Primitive.WORD).size() > 1))) {
								sentenceWithSpaces = network.createSentence((String)result.getData(), true, false, true);
							}
						} else {
							// It is a dynamic sentence, may need to add spaces.
							if (!result.hasRelationship(Primitive.TYPE, Primitive.SPACE) && !result.hasRelationship(Primitive.WORD, Primitive.SPACE)) {
								space = network.createVertex(Primitive.SPACE);
							}
						}
						List<Vertex> nestedWords = sentenceWithSpaces.orderedRelations(Primitive.WORD);
						if (nestedWords != null) {
							for (int index = 0; index < nestedWords.size(); index++) {
								Vertex nestedWord = nestedWords.get(index);
								words.add(nestedWord);
								if ((space != null) && (index < (nestedWords.size() - 1))) {
									words.add(space);
								}
							}
						}
					} else {
						words.add(result);
					}
				}
			}
			Vertex sentence = network.createInstance(Primitive.SENTENCE);
			if (formula.hasRelationship(Primitive.TYPE, Primitive.SPACE) || formula.hasRelationship(Primitive.WORD, Primitive.SPACE)) {
				sentence.addRelationship(Primitive.TYPE, Primitive.SPACE);
			}
			if (caseSensitive) {
				sentence.addRelationship(Primitive.TYPE, Primitive.CASESENSITVE);
			}
			Vertex previous = network.createVertex(Primitive.NULL);
			for (int index = 0; index < words.size(); index++) {
				Vertex word = words.get(index);
				if (word.is(Primitive.SPACE)) {
					sentence.addRelationship(Primitive.WORD, word, index);
					continue;
				}
				Vertex next = null;
				if (words.size() > (index + 1)) {
					next = words.get(index + 1);
					int nextIndex = index + 2;
					while (next.is(Primitive.SPACE) && (nextIndex < words.size())) {
						next = words.get(nextIndex);
						nextIndex++;
					}
				} else {
					next = network.createVertex(Primitive.NULL);
				}
				word = getWordFollowing(word, previous, next, network);
				sentence.addRelationship(Primitive.WORD, word, index);
				previous = word;
			}
			return createSentenceText(sentence, network);
		} catch (Exception exception) {
			log(exception);
			return null;
		}
	}
	
	/**
	 * Determine if the pattern matches the sentence.
	 */
	public static boolean evaluatePattern(Vertex pattern, Vertex sentence, Primitive variable, Map<Vertex, Vertex> variables, Network network) {
		List<Vertex> elements = pattern.orderedRelations(Primitive.WORD);
		List<Vertex> words = sentence.orderedRelations(Primitive.WORD);
		if (words == null && (sentence.getData() instanceof String) && (!((String)sentence.getData()).isEmpty())) {
			// Sentence may not have been parsed.
			sentence = network.createSentence(sentence.getDataValue());
			words = sentence.orderedRelations(Primitive.WORD);
		}
		if (elements == null && (pattern.getData() instanceof String) && (!((String)pattern.getData()).isEmpty()) && (!((String)pattern.getData()).startsWith("Pattern"))) {
			// Sentence may not have been parsed.
			sentence = network.createSentence(pattern.getDataValue());
			elements = sentence.orderedRelations(Primitive.WORD);
		}
		if (elements == null || words == null) {
			return false;
		}
		boolean result = evaluatePattern(pattern, sentence, variable, variables, network, elements, words, 0);
		return result;
	}
	
	/**
	 * Determine if the pattern matches the sentence.
	 */
	public static boolean evaluatePattern(Vertex pattern, Vertex sentence, Primitive variable, Map<Vertex, Vertex> variables, Network network,
			List<Vertex> elements, List<Vertex> words, int wildcardSkip) {
		boolean wasWildcard = false;
		int elementIndex = 0;
		int wildcardSkips = 0;
		boolean end = false;
		boolean hadMatchAfterWildcard = false;
		List<List<Vertex>> star = new ArrayList<List<Vertex>>();
		List<Vertex> currentStar = null;
		for (int index = 0; index < words.size(); index++) {
			if (elementIndex >= elements.size()) {
				if (!wasWildcard) {
					// Ignore trailing punctuation.
					while (index < words.size()) {
						Vertex word = words.get(index);
						if (!word.instanceOf(Primitive.PUNCTUATION)) {
							if (hadMatchAfterWildcard) {
								return evaluatePattern(pattern, sentence, variable, variables, network, elements, words, wildcardSkip + 1);
							}
							return false;
						}
						index++;
					}
					recordStar(star, variable, variables, network);
					return true;
				}
				end = true;
			}
			Vertex element = null;
			if (!end) {
				element = elements.get(elementIndex);
			}
			Vertex word = words.get(index);
			boolean required = true;
			if (element != null) {
				boolean found = false;
				if (element == word) {
					found = true;
				}
				if (!found && (element.is(Primitive.WILDCARD) || element.is(Primitive.UNDERSCORE) || element.is(Primitive.HATWILDCARD) || element.is(Primitive.POUNDWILDCARD))) {
					wasWildcard = true;
					if (currentStar != null) {
						star.add(currentStar);
					}
					currentStar = new ArrayList<Vertex>();
					elementIndex++;
					// * must match at least one word.
					if (element.is(Primitive.WILDCARD) || element.is(Primitive.UNDERSCORE) || elementIndex >= elements.size()) {
						currentStar.add(word);
						continue;
					}
					element = elements.get(elementIndex);
				}
				if (!found) {
					if (element.instanceOf(Primitive.EQUATION)) {
						element = element.applyQuotient(variables);
					}
					if (element.instanceOf(Primitive.VARIABLE)) {
						found = element.matches(word, variables) == Boolean.TRUE;
						currentStar = new ArrayList<Vertex>();
						currentStar.add(word);
						star.add(currentStar);
						currentStar = null;
					}
				}
				if (!found && (element == word)) {
					found = true;
				}
				if (!found && element.getData() instanceof String && word.getData() instanceof String) {
					if (((String)element.getData()).equalsIgnoreCase((String)word.getData())) {
						found = true;
					}
				} else if (!found) {
					if (element.instanceOf(Primitive.LIST)) {
						required = (element.hasRelationship(Primitive.TYPE, Primitive.REQUIRED));
						Collection<Relationship> values = element.getRelationships(Primitive.SEQUENCE);
						if (values != null) {
							for (Relationship value : values) {
								if (value.getTarget().getData() instanceof String && word.getData() instanceof String) {
									if (((String)value.getTarget().getData()).equalsIgnoreCase((String)word.getData())) {
										found = true;
									}
								} else if (value.getTarget().hasRelationship(Primitive.WORD, word)) {
									found = true;
								}
								if (found) {
									break;
								}
							}
						}
					} else {
						// Element is an object, check all words for match.
						if (element.hasRelationship(Primitive.WORD, word)) {
							found = true;
						}
					}
				}
				if (found) {
					// Must skip matching words to check for other matches if the same word is in the phrase twice.
					if (wasWildcard && (wildcardSkips < wildcardSkip)) {
						wildcardSkips++;
						currentStar.add(word);
						continue;						
					} else {
						hadMatchAfterWildcard = hadMatchAfterWildcard || (wasWildcard && elementIndex < elements.size());
						elementIndex++;
						wasWildcard = false;
						if (currentStar != null) {
							star.add(currentStar);
						}
						currentStar = null;
						continue;
					}
				}
			}
			if (word.instanceOf(Primitive.PUNCTUATION)) {
				continue;
			}
			if (wasWildcard) {
				currentStar.add(word);
				continue;
			}
			if (!required) {
				elementIndex++;
				index--;
				continue;
			}
			if (hadMatchAfterWildcard) {
				return evaluatePattern(pattern, sentence, variable, variables, network, elements, words, wildcardSkip + 1);
			}
			return false;
		}
		if (elementIndex >= elements.size()) {
			if (currentStar != null) {
				star.add(currentStar);
			}
			recordStar(star, variable, variables, network);
			return true;
		}
		// Support many of none wildcards.
		if (elementIndex == (elements.size() - 1)) {
			Vertex element = elements.get(elementIndex);
			if (element.is(Primitive.HATWILDCARD) || element.is(Primitive.POUNDWILDCARD)) {
				if (currentStar != null) {
					star.add(currentStar);
				} else {
					star.add(new ArrayList<Vertex>());					
				}
				recordStar(star, variable, variables, network);
				return true;
			}
		}
		if (hadMatchAfterWildcard) {
			return evaluatePattern(pattern, sentence, variable, variables, network, elements, words, wildcardSkip + 1);
		}
		return false;
	}
	
	public static void recordStar(List<List<Vertex>> star, Primitive variable, Map<Vertex, Vertex> variables, Network network) {
		if (star.isEmpty()) {
			return;
		} else if (star.size() == 1) {
			variables.put(network.createVertex(variable), buildStar(star.get(0), variables, network));
			return;
		}
		Vertex starValue = network.createInstance(Primitive.FRAGMENT);
		int index = 0;
		for (List<Vertex> words : star) {
			starValue.addRelationship(Primitive.WORD, buildStar(words, variables, network), index);
			index++;
		}
		variables.put(network.createVertex(variable), starValue);
	}
	
	public static Vertex buildStar(List<Vertex> star, Map<Vertex, Vertex> variables, Network network) {
		if (star.isEmpty()) {
			return network.createWord("");
		} else if (star.size() == 1) {
			return star.get(0);
		}
		Vertex starValue = network.createInstance(Primitive.FRAGMENT);
		int index = 0;
		for (Vertex word : star) {
			starValue.addRelationship(Primitive.WORD, word, index);
			index++;
		}
		return starValue;
	}
	
	/**
	 * Associate the word to the current context selection.
	 */
	public Vertex processAssociation(Vertex text, Network network) {
		Vertex meaning = ((Context)getBot().awareness().getSense(Context.class)).top(network);
		if (meaning == null) {
			return null;
		}
		text.addRelationship(Primitive.MEANING, meaning);
		List<Relationship> words = text.orderedRelationships(Primitive.WORD);
		// Associate as single word or define as compound word.
		if (words.size() == 1) {
			Vertex word = words.get(0).getTarget();
			word.addRelationship(Primitive.MEANING, meaning);
			meaning.addRelationship(Primitive.WORD, word);
		} else if (words.size() > 1) {
			text.addRelationship(Primitive.INSTANTIATION, Primitive.COMPOUND_WORD);
			text.addRelationship(Primitive.INSTANTIATION, Primitive.WORD);
			meaning.addRelationship(Primitive.WORD, text);
			Vertex word = words.get(0).getTarget();
			word.addRelationship(Primitive.COMPOUND_WORD, text);
		}
		Vertex response = meaning.mostConscious(Primitive.WORD);
		return response;
	}
	
	/**
	 * Mark the sentence as a question.
	 */
	public void checkQuestion(Vertex sentence, Network network) {
		if (sentence.instanceOf(Primitive.QUESTION)) {
			log("Sentence is a question", Bot.FINE, sentence);
			return;
		}
		Collection<Relationship> words = sentence.getRelationships(Primitive.WORD);
		if (words != null) {
			for (Relationship word : words) {
				Vertex meaning = word.getTarget().mostConscious(Primitive.MEANING);
				if (meaning != null) {
					if (meaning.instanceOf(Primitive.QUESTION)) {
						sentence.addRelationship(Primitive.INSTANTIATION, Primitive.QUESTION);
						log("Sentence is a question", Bot.FINE, sentence, meaning);
						break;
					}
				}
			}
		}
	}
	
	/**
	 * Lookup any urls in the text.
	 */
	public void processHttp(Vertex sentence, Network network) {
		Collection<Relationship> words = sentence.getRelationships(Primitive.WORD);
		if (words != null) {
			Sense http = getBot().awareness().getSense(Http.class);
			for (Relationship word : words) {
				Vertex meaning = word.getTarget().mostConscious(Primitive.MEANING);
				if (meaning != null) {
					if (meaning.instanceOf(Primitive.URL)) {
						log("Prcoessing URL", Bot.FINE, meaning);
						http.input(meaning.getData());
					}
				}
			}
		}
	}
	
	/**
	 * Process the correction to the last question.
	 */
	public Vertex processCorrection(Vertex input, Vertex correction, Vertex speaker, Vertex target, Vertex conversation, Network network) {
		if (!shouldCorrect(input, speaker)) {
			throw new BotException("You do not have permission to correct");
		}
		Vertex originalQuestion = null;
		Vertex originalQuestionInput = null;
		if (target != null) {
			// Get last input said by speaker.
			originalQuestionInput = getLastInputInConversation(conversation, speaker, 2);
			if (originalQuestionInput != null) {
				originalQuestion = originalQuestionInput.mostConscious(Primitive.INPUT);
			}
		}
		if (originalQuestion == null) {
			log("Correction missing question", Bot.FINE, correction);
			return correction;
		}
		// Get last input said by target.
		Vertex wrongResponseInput = originalQuestionInput.mostConscious(Primitive.RESPONSE);
		Vertex previousQuestionInput = originalQuestionInput.getRelationship(Primitive.QUESTION);
		boolean wasCorrect = addCorrection(originalQuestionInput, originalQuestion, wrongResponseInput, correction, previousQuestionInput, network);
		originalQuestionInput.addRelationship(Primitive.RESPONSE, input);
		input.addRelationship(Primitive.QUESTION, originalQuestionInput);
		log("Correction question", Bot.FINE, originalQuestion);
		log("Correction response", Bot.FINE, correction);
		// Check if last response was from understanding and correct state machine.
		// Do not remove pinned quotients.
		Vertex quotient = getLastQuotient(network);
		if (!wasCorrect && (quotient != null) && !quotient.isPinned()) {
			Vertex state = getLastState(network);
			log("Correcting quotient", Bot.FINE, quotient, state);
			Relationship relationship = state.getRelationship(Primitive.QUOTIENT, quotient);
			// Could be a wrong response, or just a wrong response in the context.
			// First mark wrong in context, then mark wrong in general.
			if ((relationship == null) || (relationship.getCorrectness() < 0.5)) {
				originalQuestion.removeRelationship(Primitive.RESPONSE, quotient);
				network.checkReduction(originalQuestion);
				originalQuestion.inverseAssociateAll(Primitive.SYNONYM, quotient, Primitive.RESPONSE);
			} else {
				relationship.setCorrectness(relationship.getCorrectness() / 2);
				// Dissociate previous question as meta info.
				removeSentencePreviousMeta(relationship, previousQuestionInput, network);
			}			
		}
		return correction;
	}
	
	/**
	 * Process a offensive response.
	 * Remove the sentence.
	 */
	public void processOffensive(Vertex input, Vertex speaker, Vertex target, Vertex conversation, Network network) {
		Vertex originalQuestion = null;
		Vertex originalQuestionInput = null;
		if (target != null) {
			// Get last input said by speaker.
			originalQuestionInput = getLastInputInConversation(conversation, speaker, 2);
			if (originalQuestionInput != null) {
				originalQuestion = originalQuestionInput.mostConscious(Primitive.INPUT);
			}
		}
		if (originalQuestion == null) {
			log("Offensive missing question", Bot.FINE);
			return;
		}
		// Get last input said by target.
		Vertex wrongResponseInput = originalQuestionInput.mostConscious(Primitive.RESPONSE);
		if (wrongResponseInput == null) {
			log("Offensive missing response", Bot.FINE);
			return;
		}
		Vertex wrongResponse = wrongResponseInput.mostConscious(Primitive.INPUT);
		if (wrongResponse == null) {
			log("Offensive missing response input", Bot.FINE);
			return;
		}
		if (wrongResponse.hasInverseRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
			throw new BotException("The response has been marked as not offensive by the bot's admin, so cannot be flagged as offensive.");
		}
		log("Offensive response flagged", Bot.WARNING, wrongResponse);
		// Remove this as a response from all questions.
		Collection<Relationship> relationships = network.findAllRelationshipsTo(wrongResponse);
		for (Relationship relationship : relationships) {
			if (relationship.getType().is(Primitive.RESPONSE)) {
				relationship.getSource().removeRelationship(relationship.getType(), relationship.getTarget());
			}
		}
		wrongResponseInput.addRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
		wrongResponse.addRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
		network.checkReduction(wrongResponse);
		wrongResponse.associateAll(Primitive.SYNONYM, network.createVertex(Primitive.OFFENSIVE), Primitive.ASSOCIATED);
		// Check if last response was from understanding and correct state machine.
		// Do not remove pinned quotients.
		Vertex quotient = getLastQuotient(network);
		if ((quotient != null) && !quotient.isPinned()) {
			Vertex state = getLastState(network);
			log("Correcting quotient", Bot.FINE, quotient, state);
			state.removeRelationship(Primitive.QUOTIENT, quotient);
		}
	}
	
	/**
	 * Process the discussion sentence.
	 * Only response if a question and understood,
	 * or has an exact known response.
	 */
	public Vertex processDiscussion(Vertex input, Vertex sentence, Vertex conversation, Map<Vertex, Vertex> variables, Network network) {
		boolean checkUnderstanding = true;
		Vertex response = null;
		if (!this.checkExactMatchFirst) {
			// Try to understand first.
			response = processUnderstanding(input, sentence, this.discussionMatchPercentage, variables, network);
			checkUnderstanding = false;
			if (response != null) {
				if (sentence.instanceOf(Primitive.QUESTION)) {
					log("Discussion sentence understood", Bot.FINE, sentence, response);
				} else {
					response = null;
					log("Discussion sentence understood, but not a question", Bot.FINE, sentence, response);				
				}
			}
		}
		boolean hadResponse = response != null;
		Relationship relationship = null;
		if (response == null) {
			// Check if the sentence has a known response.
			relationship = bestResponse(this.discussionMatchPercentage, input, sentence, null, null, network);
			if (relationship != null) {
				response = relationship.getTarget();
			}
			if (response == null && !this.checkExactMatchFirst) {
				// Try to understand first.
				response = processUnderstanding(input, sentence, this.discussionMatchPercentage, variables, network);
				checkUnderstanding = false;
				if (response != null) {
					if (sentence.instanceOf(Primitive.QUESTION)) {
						log("Discussion sentence understood", Bot.FINE, sentence, response);
					} else {
						response = null;
						log("Discussion sentence understood, but not a question", Bot.FINE, sentence, response);				
					}
				}					
			}
			hadResponse = response != null;
			if (response != null) {
				log("Question known response", Bot.FINE, response);
			} else {
				Relationship mostConsciousRelationship = sentence.mostConsciousRelationship(Primitive.RESPONSE);
				hadResponse = mostConsciousRelationship != null;
				if (mostConsciousRelationship != null) {
					log("Question known response was not certain", Bot.FINE, mostConsciousRelationship, this.discussionMatchPercentage);
					response = null;
				} else {
					log("No known response, checking question patterns", Bot.FINE, sentence);
					// Try to find a pattern match.
					relationship = matchPattern(sentence, null, input, variables, network, this.discussionMatchPercentage);
					if (relationship != null) {
						response = relationship.getTarget();
						log("Question pattern match", Bot.FINE, response);
					} else {
						log("No known response, checking similar questions", Bot.FINE, sentence);
						// Try to find a good match.
						relationship = findResponseMatch(sentence, null, input, network, this.discussionMatchPercentage);
						if (relationship != null) {
							response = relationship.getTarget();
							log("Discussion similar question match", Bot.FINE, response);
						} else {
							// Find the best match for logging.
							relationship = findResponseMatch(sentence, null, input, network, 0);
							if (relationship != null) {
								log("Discussion question match response was not valid", Bot.FINE, relationship, this.discussionMatchPercentage);
								response = null;
							}
						}
					}
				}
			}
		}
		if (response == null) {
			return null;
		}
		// Pause to avoid rapid responses, if someone else responds, then don't
		Vertex lastInput = conversation.lastRelationship(Primitive.INPUT);
		if (lastInput != null) {
			Vertex lastSentence = lastInput.mostConscious(Primitive.INPUT);
			if (lastSentence != sentence) {
				log("Sentence was already responded to", Bot.FINE, sentence, lastSentence);
				return null;
			}
			try {
				getBot().memory().wait(COVERSATIONAL_RESPONSE_DELAY);
			} catch (InterruptedException ignore) {}
			if (getBot().memory().getActiveMemory().size() > 1) {
				log("New active memory", Bot.FINE, getBot().memory().getActiveMemory().size());
				// Use a new memory to access new input.
				Network tempMemory = getBot().memory().newMemory();
				Vertex tempConversation = tempMemory.createVertex(conversation);
				Vertex tempSentence = tempMemory.createVertex(sentence);
				lastInput = tempConversation.lastRelationship(Primitive.INPUT);
				lastSentence = lastInput.mostConscious(Primitive.INPUT);
				if (lastSentence != tempSentence) {
					log("Sentence was already responded to", Bot.FINE, sentence, lastSentence);
					return null;
				}
			}
		}
		// Avoid responding the same way twice in a row.
		if (response != null && conversation != null) {
			Vertex newResponse = checkDuplicateOrOffensiveResponse(response, sentence, conversation, input, variables, network, !hadResponse, checkUnderstanding);
			if (response == newResponse && relationship != null && relationship.hasMeta()) {
				Vertex topic = relationship.getMeta().getRelationship(Primitive.TOPIC);
				if (topic != null && !topic.instanceOf(Primitive.PATTERN)) {
					log("Conversation topic", Bot.FINE, topic);
					conversation.setRelationship(Primitive.TOPIC, topic);
				}
			}
			response = newResponse;
		}
		return response;
	}
	
	/**
	 * Check if the previous response was the same and try to find a new one.
	 */
	public Vertex checkDuplicateOrOffensiveResponse(Vertex response, Vertex sentence, Vertex conversation, Vertex input, Map<Vertex, Vertex> variables, Network network, boolean allowMatch, boolean checkUnderstanding) {
		Vertex self = network.createVertex(Primitive.SELF);
		Vertex previousOutput = getLastInputInConversation(conversation, self, 1);
		Vertex previousResponse = null;
		if (previousOutput != null) {
			previousResponse = previousOutput.mostConscious(Primitive.INPUT);
		}
		Vertex newResponse = response;
		if (previousResponse != null) {
			boolean offensive = response.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE);
			if ((previousResponse == response) || offensive) {
				if (offensive) {
					log("Response was offensive", Bot.FINE, response);					
				} else {
					log("Response was same as previous", Bot.FINE, previousResponse);
				}
				newResponse = null;
				if (checkUnderstanding) {
					// Try to understand first.
					newResponse = processUnderstanding(input, sentence, this.conversationMatchPercentage, variables, network);
				}
				if (newResponse == null) {
					// Try to find another response.
					Relationship nextBest = bestResponse(this.conversationMatchPercentage, input, sentence, null, response, network);
					if (nextBest != null) {
						newResponse = nextBest.getTarget();
					}
					if ((newResponse == null) && allowMatch) {
						// Try to find a good match.
						Relationship relationship = matchPattern(sentence, response, input, variables, network, this.conversationMatchPercentage);
						if (relationship != null) {
							newResponse = relationship.getTarget();
						} else {
							// Try to find a good match.
							relationship = findResponseMatch(sentence, response, input, network, this.conversationMatchPercentage);
							if (relationship != null) {
								newResponse = relationship.getTarget();
							}
						}
					}
				}
				if (newResponse == null) {
					if (offensive) {
						log("Response was offensive, no other response available", Bot.FINE, response);
						return null;
					}
					log("Response is duplicate, but no other response available", Bot.FINE, previousResponse);
					// No other response, go with previous one.
					newResponse = response;
				} else {
					if (newResponse.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
						log("New response was also offensive", Bot.FINE, response);
						return null;
					}
					log("Response was duplicate, found another response", Bot.FINE, newResponse);
				}
				
			}
		}
		return newResponse;
	}
	
	/**
	 * Return the best response to the question, taking into account the input history.
	 */
	public Relationship bestResponse(float percentage, Vertex input, Vertex sentence, Vertex question, Vertex previousResponse, Network network) {
		return bestResponse(percentage, input, sentence, question, previousResponse, true, network);
	}
	
	/**
	 * Return the best response to the question, taking into account the input history.
	 */
	public Relationship bestResponse(float percentage, Vertex input, Vertex sentence, Vertex question, Vertex previousResponse, boolean cascade, Network network) {
		// Check if any response has the same previous question.
		Vertex previousQuestionInput = input.getRelationship(Primitive.QUESTION);
		Vertex previousQuestion = null;
		if (previousQuestionInput != null) {
			previousQuestion = previousQuestionInput.getRelationship(Primitive.INPUT);
		}
		Collection<Relationship> responses = sentence.getRelationships(Primitive.RESPONSE);
		Vertex conversation = input.getRelationship(Primitive.CONVERSATION);
		Vertex currentTopic = null;
		if (conversation != null) {
			currentTopic = conversation.mostConscious(Primitive.TOPIC);
		}
		Relationship best = null;		
		if (responses != null) {
			Relationship bestWithTopic = null;
			Relationship bestWithPrevious = null;
			Relationship bestWithTopicPrevious = null;
			for (Relationship response : responses) {
				if ((response.getCorrectness() >= percentage) && (response.getTarget() != previousResponse)) {
					if (response.getTarget().hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE)) {
						continue;
					}
					Vertex meta = response.getMeta();
					if (meta != null) {
						if ((previousQuestion == null) && meta.hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)) {
							continue;
						}
						Vertex topic = meta.getRelationship(Primitive.TOPIC);
						if (bestWithTopic != null && topic == null) {
							continue;
						}
						Collection<Relationship> required = meta.getRelationships(Primitive.REQUIRED);
						if ((required != null) && (!required.isEmpty()) && (question != null)) {
							// Ensure all required words are in the matched question.
							//TODO also check synonyms
							boolean found = true;
							for (Relationship word : required) {
								if (!question.hasRelationship(Primitive.WORD, word.getTarget())) {
									Vertex lowercase = invertWordCase(word.getTarget(), network);
									if (lowercase == null || lowercase == word || !question.hasRelationship(Primitive.WORD, lowercase)) {
										Vertex uppercase = network.findByData(((String)word.getTarget().getData()).toUpperCase());
										if (uppercase == null || uppercase == word || !question.hasRelationship(Primitive.WORD, uppercase)) {
											found = false;
											break;
										}
									}
								}
							}
							if (!found) {
								continue;
							}
						} else {
							Collection<Relationship> keywords = meta.getRelationships(Primitive.KEYWORD);
							if ((keywords != null) && (!keywords.isEmpty()) && (question != null)) {
								// Ensure any keywords are in the matched question.
								//TODO also check synonyms
								boolean found = false;
								for (Relationship keyword : keywords) {
									if (question.hasRelationship(Primitive.WORD, keyword.getTarget())) {
										found = true;
										break;
									} else {
										Vertex lowercase = invertWordCase(keyword.getTarget(), network);
										if (lowercase != null && lowercase != keyword && question.hasRelationship(Primitive.WORD, lowercase)) {
											found = true;
											break;
										} else {
											Vertex uppercase = network.findByData(((String)keyword.getTarget().getData()).toUpperCase());
											if (uppercase != null && uppercase != keyword && question.hasRelationship(Primitive.WORD, uppercase)) {
												found = true;
												break;
											}
										}
									}
								}
								if (!found) {
									continue;
								}
							}
						}
						if (topic != null) {
							boolean requireTopic = meta.hasRelationship(Primitive.REQUIRE, Primitive.TOPIC);
							if (currentTopic == null && requireTopic) {
								continue;
							}
							if (currentTopic != null) {
								boolean match = topic == currentTopic;
								if (!match && topic.instanceOf(Primitive.PATTERN)) {
									match = evaluatePattern(topic, currentTopic, Primitive.TOPICWILDCARD, new HashMap<Vertex, Vertex>(), network);
								}
								if (!match && requireTopic) {
									continue;
								}
								if (match && (bestWithTopic == null || (response.getCorrectness() > bestWithTopic.getCorrectness()))) {
									if (previousQuestion != null) {
										if (meta.hasRelationship(Primitive.PREVIOUS, previousQuestion)) {
											bestWithTopicPrevious = response;
										} else {
											if (meta.hasInverseRelationship(Primitive.PREVIOUS, previousQuestion)) {
												continue;
											}
											Collection<Relationship> previous = meta.getRelationships(Primitive.PREVIOUS);
											if (previous != null) {
												for (Relationship relationship : previous) {
													match = evaluatePattern(relationship.getTarget(), previousQuestion, Primitive.THATWILDCARD, new HashMap<Vertex, Vertex>(), network);
													if (match) {
														bestWithTopicPrevious = response;
													}
												}
											}
										}
									}
									if (meta.hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)) {
										continue;
									}
									bestWithTopic = response;
								}
							}
						}
						if ((bestWithTopic == null) && (previousQuestion != null) && ((bestWithPrevious == null) || (response.getCorrectness() > bestWithPrevious.getCorrectness()))) {
							boolean match = false;
							if (meta.hasRelationship(Primitive.PREVIOUS, previousQuestion)) {
								bestWithPrevious = response;
							} else {
								if (meta.hasInverseRelationship(Primitive.PREVIOUS, previousQuestion)) {
									continue;
								}
								Collection<Relationship> previous = meta.getRelationships(Primitive.PREVIOUS);
								if (previous != null) {
									for (Relationship relationship : previous) {
										match = evaluatePattern(relationship.getTarget(), previousQuestion, Primitive.THATWILDCARD, new HashMap<Vertex, Vertex>(), network);
										if (match) {
											bestWithPrevious = response;
											break;
										}
									}
								}
								if (!match && meta.hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)) {
									continue;
								}
							}
						}
					}
					if ((best == null) || (response.getCorrectness() > best.getCorrectness())) {
						best = response;
					}
				}
			}
			if (bestWithTopicPrevious != null) {
				if (bestWithTopicPrevious.hasMeta()) {
					this.lastResponseMetaId = bestWithTopicPrevious.getMeta().getId();
				}
				return bestWithTopicPrevious;
			}
			if (bestWithTopic != null) {
				if (bestWithTopic.hasMeta()) {
					this.lastResponseMetaId = bestWithTopic.getMeta().getId();
				}
				return bestWithTopic;
			}
			if (bestWithPrevious != null) {
				if (bestWithPrevious.hasMeta()) {
					this.lastResponseMetaId = bestWithPrevious.getMeta().getId();
				}
				return bestWithPrevious;
			}
			if (best != null) {
				if (best.hasMeta()) {
					this.lastResponseMetaId = best.getMeta().getId();
				}
				return best;
			}
		}
		network.checkReduction(sentence);
		Collection<Relationship> meanings = sentence.getRelationships(Primitive.SYNONYM);
		if (cascade && (meanings != null)) {
			for (Relationship meaning : meanings) {
				best = bestResponse(percentage, input, meaning.getTarget(), question, previousResponse, false, network);
				if (best != null) {
					return best;
				}
			}
		}
		return best;
	}
	
	/**
	 * Process the start of a new conversation and output the greeting.
	 */
	public Vertex processGreeting(Vertex input, Vertex conversation, Network network, LanguageState state, Map<Vertex, Vertex> variables) {
		Vertex language = network.createVertex(getPrimitive());
		Collection<Relationship> greetings = language.getRelationships(Primitive.GREETING);
		if (greetings == null) {
			log("No greeting", Bot.FINE);
			return null;
		}
		Vertex greeting = Utils.random(greetings).getTarget();
		log("Greeting", Bot.FINE, greeting);
		// Check for formula and transpose
		if ((greeting != null) && greeting.instanceOf(Primitive.FORMULA)) {
			log("Greeting is formula", Level.FINE, greeting);
			Vertex result = evaluateFormula(greeting, variables, network);
			if (result == null) {
				log("Formula cannot be evaluated", Level.FINE, greeting);
				greeting = null;
				// Find non formula.
				for (Relationship relationship : greetings) {
					if (!relationship.getTarget().instanceOf(Primitive.FORMULA)) {
						greeting = relationship.getTarget();
					}
				}
			} else {
				greeting = getWord(result, network);
			}
		}
		return greeting;
	}
	
	/**
	 * Process the conversational sentence.
	 * Try to understand, otherwise,
	 * determine the best know response, if there is no known
	 * response, then resort to mimicry.
	 */
	public Vertex processConversational(Vertex input, Vertex sentence, Vertex conversation, Map<Vertex, Vertex> variables, Network network, LanguageState state) {
		Vertex response = null;
		boolean checkUnderstanding = true;
		if (!this.checkExactMatchFirst) {
			// Try to understand first.
			response = processUnderstanding(input, sentence, this.conversationMatchPercentage, variables, network);
			checkUnderstanding = false;
		}
		Relationship relationship = null;
		if (response == null) {			
			// Check if the sentence has a known response.
			relationship = bestResponse(0.1f, input, sentence, null, null, network);
			if (relationship != null) {
				response = relationship.getTarget();
			}
		}
		if ((response == null) && this.checkExactMatchFirst) {
			// Try to understand first.
			response = processUnderstanding(input, sentence, this.conversationMatchPercentage, variables, network);
			checkUnderstanding = false;
		}
		boolean hadResponse = response != null;
		if (response != null) {
			log("Question known response", Bot.FINE, response);
		} else {
			log("No known response, checking question patterns", Bot.FINE, sentence);
			// Try to find a pattern that matches.
			relationship = matchPattern(sentence, null, input, variables, network, this.conversationMatchPercentage);
			if (relationship != null) {
				response = relationship.getTarget();
				log("Question pattern match", Bot.FINE, response);
			} else {
				log("No known response, checking similar questions", Bot.FINE, sentence);
				// Try to find a good match.
				relationship = findResponseMatch(sentence, null, input, network, this.conversationMatchPercentage);
				if (relationship != null) {
					response = relationship.getTarget();
					log("Conversation similar question match response", Bot.FINE, response);
				} else  if ((state == LanguageState.Answering) || (state == LanguageState.Discussion)) {
					this.wasMimic = true;
					Vertex language = network.createVertex(getPrimitive());
					boolean sequential = language.hasRelationship(Primitive.DEFAULT_SEQUENTIAL);
					List<Relationship> defaultResponses = language.orderedRelationships(Primitive.RESPONSE);
					if (defaultResponses == null) {
						// Mimic.
						log("Conversation mimic", Bot.FINE, sentence);
						response = sentence;
					} else {
						variables.put(network.createVertex(Primitive.WILDCARD), sentence);
						Vertex previousQuestionInput = input.getRelationship(Primitive.QUESTION);
						Vertex previousQuestion = null;
						if (previousQuestionInput != null) {
							previousQuestion = previousQuestionInput.getRelationship(Primitive.INPUT);
						}
						// Check topic
						if (conversation != null) {
							Vertex topic = conversation.getRelationship(Primitive.TOPIC);
							if (topic != null) {
								Vertex topicMatch = null;
								Vertex previousMatch = null;
								for (Relationship defaultResponse : defaultResponses) {
									if (defaultResponse.hasMeta()) {
										Vertex defaultTopic = defaultResponse.getMeta().getRelationship(Primitive.TOPIC);
										if (defaultTopic != null) {
											boolean match = topic == defaultTopic;
											if (!match && defaultTopic.instanceOf(Primitive.PATTERN)) {
												match = evaluatePattern(defaultTopic, topic, Primitive.TOPIC, new HashMap<Vertex, Vertex>(), network);
											}
											if (match) {
												if (defaultResponse.getMeta().hasRelationship(Primitive.PREVIOUS)) {
													boolean previousMatches = defaultResponse.getMeta().hasRelationship(Primitive.PREVIOUS, previousQuestion);
													if (!previousMatches) {
														// Check for patterns.
														Collection<Relationship> previousResponses = defaultResponse.getMeta().getRelationships(Primitive.PREVIOUS);
														if (previousResponses != null) {
															for (Relationship previousResponse : previousResponses) {
																if (!previousResponse.isInverse() && previousResponse.getTarget().instanceOf(Primitive.PATTERN)) {
																	previousMatches = evaluatePattern(previousResponse.getTarget(), previousQuestion, Primitive.PREVIOUS, new HashMap<Vertex, Vertex>(), network);
																	if (previousMatches) {
																		previousMatch = defaultResponse.getTarget();
																		log("Conversation topic and previous default response", Level.FINE, defaultTopic, previousMatch);
																		previousMatch = checkDefaultResponseFormula(previousMatch, input, network, variables);
																		if (previousMatch != null) {
																			break;
																		}
																	}
																} 
															}
														}
													}
												} else if (topicMatch == null) {
													topicMatch = defaultResponse.getTarget();
													log("Conversation topic default response", Level.FINE, defaultTopic, topicMatch);
													topicMatch = checkDefaultResponseFormula(topicMatch, input, network, variables);
												}
											}
										}
									}
								}
								if (previousMatch != null) {
									response = previousMatch;
								} else if (topicMatch != null) {
									response = topicMatch;
								}
							}
						}
						if (response == null) {
							// Check previous.
							if (previousQuestion != null) {
								for (Relationship defaultResponse : defaultResponses) {
									if (defaultResponse.hasMeta()) {
										boolean match = defaultResponse.getMeta().hasRelationship(Primitive.PREVIOUS, previousQuestion);
										if (!match) {
											// Check for patterns.
											Collection<Relationship> previousResponses = defaultResponse.getMeta().getRelationships(Primitive.PREVIOUS);
											if (previousResponses != null) {
												for (Relationship previousResponse : previousResponses) {
													if (!previousResponse.isInverse() && previousResponse.getTarget().instanceOf(Primitive.PATTERN)) {
														match = evaluatePattern(previousResponse.getTarget(), previousQuestion, Primitive.PREVIOUS, new HashMap<Vertex, Vertex>(), network);
														if (match) {
															break;
														}
													} 
												}
											}
										}
										if (match) {
											response = defaultResponse.getTarget();
											log("Conversation previous default response", Bot.FINE, previousQuestion, response);
											response = checkDefaultResponseFormula(response, input, network, variables);
											if (response != null) {
												break;
											}
										}
									}
								}
							}
						}
						if (response == null) {
							List<Relationship> candidates = new ArrayList<Relationship>();
							for (Relationship defaultResponse : defaultResponses) {
								if (!defaultResponse.hasMeta()) {
									candidates.add(defaultResponse);
								}
							}
							response = Utils.random(candidates).getTarget();
							log("Conversation default response", Level.FINE, sentence, response);
							response = checkDefaultResponseFormula(response, input, network, variables);
							if (response == null) {
								// Find non formula.
								for (Relationship defaultResponse : candidates) {
									if (!defaultResponse.getTarget().instanceOf(Primitive.FORMULA)) {
										response = defaultResponse.getTarget();
									}
								}
								if (response == null) {
									// Try once more.
									response = Utils.random(candidates).getTarget();
									response = checkDefaultResponseFormula(response, input, network, variables);
								}
								if (response == null) {
									// Mimic.
									log("Conversation mimic, failed to find default response", Level.FINE, sentence);
									response = sentence;									
								}
							} else {
								response = getWord(response, network);
							}
						}
					}
				}
			}
		}
		// Avoid responding the same way twice in a row.
		if ((response != null) && (conversation != null)) {
			Vertex newResponse = checkDuplicateOrOffensiveResponse(response, sentence, conversation, input, variables, network, !hadResponse, checkUnderstanding);
			if (response == newResponse && relationship != null && relationship.hasMeta()) {
				Vertex topic = relationship.getMeta().getRelationship(Primitive.TOPIC);
				if (topic != null && !topic.instanceOf(Primitive.PATTERN)) {
					log("Conversation topic", Bot.FINE, topic);
					conversation.setRelationship(Primitive.TOPIC, topic);
				}
			}
			response = newResponse;
		}
		return response;
	}
	
	public Vertex checkDefaultResponseFormula(Vertex response, Vertex input, Network network, Map<Vertex, Vertex> variables) {
		if ((response != null) && response.instanceOf(Primitive.FORMULA)) {
			log("Default response is formula", Level.FINE, response);
			Vertex result = evaluateFormula(response, variables, network);
			if (result == null) {
				log("Formula cannot be evaluated", Level.FINE, response);
			}
			return result;
		}
		return response;
	}
	
	/**
	 * Return the last thing the speaker said in the conversation.
	 */
	public Vertex getLastInputInConversation(Vertex conversation, Vertex speaker, int last) {
		if (conversation == null) {
			return null;
		}
		List<Vertex> allInput = conversation.orderedRelations(Primitive.INPUT);
		if (allInput != null) {
			int count = 0;
			for (int index  = allInput.size() - 1; index >= 0; index--) {
				Vertex previousInput = allInput.get(index);
				if (previousInput.mostConscious(Primitive.SPEAKER) == speaker) {
					count++;
					if (count >= last) {
						return previousInput;
					}
				}
			}
		}
		return null;		
	}

	/**
	 * Return if learning should be used for the input.
	 */
	public boolean shouldLearn(Vertex input, Vertex speaker) {
		if (this.allowLearning == Boolean.TRUE) {
			return true;
		} else if (this.allowLearning == Boolean.FALSE) {
			return false;
		}
		boolean isAdmin = false;
		boolean isAnonymous = true;
		if (speaker != null) {
			isAdmin = speaker.hasRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
			isAnonymous = speaker.hasRelationship(Primitive.ASSOCIATED, Primitive.ANONYMOUS);
		}
		if (this.learningMode == LearningMode.Disabled) {
			return false;
		} else if (!isAdmin && (this.learningMode == LearningMode.Administrators)) {
			if (speaker.is(Primitive.SELF)) {
				return true;
			}
			return false;					
		} else if (isAnonymous && (this.learningMode == LearningMode.Users)) {
			return false;
		}
		return true;
	}

	/**
	 * Return if learning should be used for the input.
	 */
	public boolean shouldCorrect(Vertex input, Vertex speaker) {
		boolean isAdmin = false;
		boolean isAnonymous = true;
		if (speaker != null) {
			isAdmin = speaker.hasRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
			isAnonymous = speaker.hasRelationship(Primitive.ASSOCIATED, Primitive.ANONYMOUS);
		}
		if (this.correctionMode == CorrectionMode.Disabled) {
			return false;
		} else if (!isAdmin && (this.correctionMode == CorrectionMode.Administrators)) {
			return false;					
		} else if (isAnonymous && (this.correctionMode == CorrectionMode.Users)) {
			return false;
		}
		return true;
	}

	/**
	 * Self API for checking is a user is trusted and should allow correction.
	 */
	public Vertex allowCorrection(Vertex vertex) {
		log("allowCorrection", Level.FINE, vertex);
		if (shouldCorrect(null, vertex)) {
			return vertex.getNetwork().createVertex(Primitive.TRUE);
		}
		return vertex.getNetwork().createVertex(Primitive.FALSE);
	}
	
	/**
	 * Associate the response, attempt to understand.
	 */
	public Vertex processListening(Vertex input, Vertex sentence, Vertex speaker, Vertex conversation, List<Relationship> targets, Network network, LanguageState state) {
		if (targets != null) {
			for (int index = 0; index < targets.size(); index++) {
				Vertex target = targets.get(index).getTarget();
				// Get last input said by target.
				Vertex lastInput = getLastInputInConversation(conversation, target, 1);
				// Abort if is first question and talking to self.
				if (input == lastInput) {
					return sentence;
				}
				if (lastInput == null) {
					continue;
				}
				Vertex lastSentence = lastInput.mostConscious(Primitive.INPUT);
				Vertex mimic = lastInput.getRelationship(Primitive.MIMIC);
				if (mimic != null) {
					lastSentence = mimic.mostConscious(Primitive.INPUT);
				}
				if (lastSentence == null) {
					continue;
				}
				float value = 1.0f/(index + (1/this.learningRate));
				if (!shouldLearn(input, speaker)) {
					// Still maintain input state.
					lastInput.addWeakRelationship(Primitive.RESPONSE, input, value);
					input.addWeakRelationship(Primitive.QUESTION, lastInput, value);					
				} else {
					// Associate response.
					// Associate previous question as meta info.
					Vertex previousQuestionInput = lastInput.getRelationship(Primitive.QUESTION);
					sentence.addWeakRelationship(Primitive.QUESTION, lastSentence, value);
					lastInput.addWeakRelationship(Primitive.RESPONSE, input, value);
					lastSentence.associateAll(Primitive.WORD, lastSentence, Primitive.QUESTION);
					input.addWeakRelationship(Primitive.QUESTION, lastInput, value);
					Relationship relationship = null;
					if (index == 0) {
						relationship = getBot().mind().getThought(Comprehension.class).checkFormula(input, network);
						if (relationship == null) {
							relationship = lastSentence.addWeakRelationship(Primitive.RESPONSE, sentence, value);
							network.checkReduction(lastSentence);
							lastSentence.weakAssociateAll(Primitive.SYNONYM, sentence, Primitive.RESPONSE, value);
						}
					} else {
						relationship = lastSentence.addWeakRelationship(Primitive.RESPONSE, sentence, value);
					}
					addSentencePreviousMeta(relationship, previousQuestionInput, network);
				}
				log("Listening sentence", Bot.FINE, lastSentence);
			}
		}
		if (speaker != null) {
			Vertex lastInput = getLastInputInConversation(speaker, conversation, 1);
			if (lastInput != null) {
				Vertex lastSentence = lastInput.mostConscious(Primitive.INPUT);
				if (lastSentence != null) {
					lastSentence.addRelationship(Primitive.NEXT, sentence);
					sentence.addRelationship(Primitive.PREVIOUS, lastSentence);
				}
			}			
		}
		return sentence;
	}
	
	/**
	 * Compute the real value of the sentence.
	 */
	public int computeMaxSentenceValue(Vertex match, Vertex original, Network network) {
		Collection<Relationship> words = match.getRelationships(Primitive.WORD);
		int max = 0;
		for (Relationship word : words) {
			boolean found  = false;
			Vertex lowercase = invertWordCase(word.getTarget(), network);
			if (original.hasRelationship(Primitive.WORD, word.getTarget())) {
				found = true;
			} else {
				if ((lowercase != null) && (lowercase != word)) {
					if (original.hasRelationship(Primitive.WORD, lowercase)) {
						found = true;
					}
				}
				// TODO synonyms, uppercase
			}
			if (found) {
				int value = computeWordValue(word.getTarget());
				if ((lowercase != null) && (lowercase != word)) {
					value = Math.max(value, computeWordValue(lowercase));
				}
				max = max + value;
			}
		}
		return max;
	}
	
	/**
	 * Return the matching value for the word, some word types are worth more than others.
	 */
	public int computeWordValue(Vertex word) {
		int value = 2;
		int count = 0;
		if (word.instanceOf(Primitive.NOUN)) {
			value = value + 12;
			count++;
		}
		if (word.instanceOf(Primitive.ADJECTIVE)) {
			value = value + 6;
			count++;
		}
		if (word.instanceOf(Primitive.INTERJECTION)) {
			value = value + 6;
			count++;
		}
		if (word.instanceOf(Primitive.VERB)) {
			value = value + 4;
			count++;
		}
		if (word.instanceOf(Primitive.QUESTION)) {
			value = value + 3;
			count++;
		}
		if (word.instanceOf(Primitive.ADVERB)) {
			value = value + 2;
			count++;
		}
		if (count == 0) {
			// Check for meaning.
			Collection<Relationship> meanings = word.getRelationships(Primitive.MEANING);
			if (meanings != null) {
				for (Relationship relation : meanings) {
					Vertex meaning = relation.getTarget();
					// Value nouns and adjective over other words.
					if (meaning.instanceOf(Primitive.THING)) {
						value = value + 12;
					} else if (meaning.instanceOf(Primitive.DESCRIPTION)) {
						value = value + 6;
					} else if (meaning.instanceOf(Primitive.INTERJECTION)) {
						value = value + 6;
					} else if (meaning.instanceOf(Primitive.ACTION)) {
						value = value + 4;
					} else if (meaning.instanceOf(Primitive.QUESTION)) {
						value = value + 3;
					} else {
						value = value + 2;
					}
					// TODO consider consciousness level, number of relationships
				}
				value = value / meanings.size();
			}
		} else {
			value = value / count;
		}
		if (word.instanceOf(Primitive.PUNCTUATION) || (word.instanceOf(Primitive.ARTICLE))) {
			value = 1;
		}
		if (word.instanceOf(Primitive.KEYWORD)) {
			value = value * 2 + 25;
		}
		return value;
	}
	
	/**
	 * Add all of the sentences for the word with its value.
	 */
	public void recordSetenceValues(Vertex word, Collection<Relationship> relationships, int value, Vertex sentence, Map<Vertex, Integer> matches, Network network, List<Vertex> defer) {
		if (relationships != null) {
			if ((defer != null) && relationships.size() > 100) {
				log("Deferring word", Level.FINER, word, relationships.size());
				defer.add(word);
				return;
			}
			for (Relationship sentenceRelation : relationships) {
				Vertex otherSentence = sentenceRelation.getTarget();
				if (sentence != otherSentence) {
					// Only index sentences with responses.
					if (otherSentence.hasAnyResponseRelationship()) {
						Integer count = matches.get(otherSentence);
						if (count == null) {
							count = 0;
						}
						matches.put(otherSentence, count + value);
						log("Increasing question match value", Level.FINER, otherSentence, count + value, value);
					} else {
						log("Sentence has no responses", Level.FINEST, otherSentence);
					}
				}
			}
		}
	}
	
	/**
	 * Add all of the patterns for the word with its value.
	 */
	public void recordPatternValues(Vertex word, Vertex sentence, Map<Vertex, Integer> matches, Network network, List<Vertex> defer) {
		Collection<Relationship> sentenceRelations = word.getRelationships(Primitive.PATTERN);
		if (sentenceRelations != null) {
			if ((defer != null) && sentenceRelations.size() > 100) {
				defer.add(word);
				return;
			}
			int value = computeWordValue(word);
			for (Relationship sentenceRelation : sentenceRelations) {
				Vertex otherSentence = sentenceRelation.getTarget();
				if (sentence != otherSentence) {
					// Only index sentences with responses.
					if (otherSentence.hasAnyResponseRelationship()) {
						Integer count = matches.get(otherSentence);
						if (count == null) {
							count = 0;
						}
						matches.put(otherSentence, count + value);
					}
				}
			}
			// Record max value.
			Integer count = matches.get(sentence);
			if (count == null) {
				count = 0;
			}
			matches.put(sentence, count + value);
		}
	}
	
	public static Vertex invertWordCase(Vertex word, Network network) {
		if (!(word.getData() instanceof String)) {
			return null;
		}
		String text = (String)word.getData();
		if (Utils.isCaps(text) || Utils.isCapitalized(text)) {
			return network.findByData(((String)word.getData()).toLowerCase());
		} else {
			return network.findByData(Utils.capitalize((String)word.getData()));					
		}
	}

	/**
	 * Add all of the questions for all of the words to the matching map.
	 */
	public void addQuestionMatches(Vertex sentence, Network network, long startTime, long processTime, List<Relationship> wordRelations, Map<Vertex, Integer> matches, Primitive key, boolean keywords) {
		List<Vertex> deferred = new ArrayList<Vertex>();
		for (Relationship wordRelation : wordRelations) {
			Vertex word = wordRelation.getTarget();
			if (keywords && !word.instanceOf(Primitive.KEYWORD)) {
				continue;
			}
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > processTime) {
				log("Search time limit reached (time, matches)", Level.INFO, processTime, matches.size());
				break;
			}
			int value = computeWordValue(word);
			Vertex lowercase = invertWordCase(word, network);
			if ((lowercase != null) && (lowercase != word)) {
				value = Math.max(value, computeWordValue(lowercase));
			}
			Vertex uppercase = network.findByData(((String)word.getData()).toUpperCase());
			if ((uppercase != null) && (uppercase != word)) {
				value = Math.max(value, computeWordValue(uppercase));
			}
			Collection<Relationship> questions = word.getRelationships(key);
			if (questions != null) {
				log("Finding similar questions for word (word, value, questions, keyword)", Level.FINER, word.getData(), value, questions.size(), keywords);
				recordSetenceValues(word, questions, value, sentence, matches, network, deferred);
			}
			if ((lowercase != null) && (lowercase != word)) {
				questions = lowercase.getRelationships(key);
				if (questions != null) {
					log("Finding similar questions for word lowercase (word, value, questions, keyword)", Level.FINER, lowercase.getData(), value, questions.size(), keywords);
					recordSetenceValues(lowercase, questions, value, sentence, matches, network, deferred);
				}
			}
			if ((uppercase != null) && (uppercase != word)) {
				questions = uppercase.getRelationships(key);
				if (questions != null) {
					log("Finding similar questions for word uppercase (word, value, questions, keyword)", Level.FINER, uppercase.getData(), value, questions.size(), keywords);
					recordSetenceValues(uppercase, questions, value, sentence, matches, network, deferred);
				}
			}
			// TODO synonyms, plurals, conjegations
		}
		// Process keywords with lots of sentences last.
		for (Vertex word : deferred) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > processTime) {
				log("Search time limit reached (time, matches)", Level.INFO, processTime, matches.size());					
				break;
			}
			int value = computeWordValue(word);
			Vertex lowercase = invertWordCase(word, network);
			if ((lowercase != null) && (lowercase != word)) {
				value = Math.max(value, computeWordValue(lowercase));
			}
			Vertex uppercase = network.findByData(((String)word.getData()).toUpperCase());
			if ((uppercase != null) && (uppercase != word)) {
				value = Math.max(value, computeWordValue(uppercase));
			}
			Collection<Relationship> questions = word.getRelationships(key);
			if (questions != null) {
				log("Checking deferred word (word, value, questions)", Level.FINER, word.getData(), value, questions.size());
				recordSetenceValues(word, questions, value, sentence, matches, network, null);
			}
		}
	}
	
	/**
	 * Find the best match for the sentence.
	 * Traverse its words to find other sentences they are used in,
	 * and pick other sentence with the most words in common.
	 */
	public Relationship findResponseMatch(Vertex sentence, Vertex previousResponse, Vertex input, Network network, float percentage) {
		if (!this.enableResponseMatch) {
			return null;
		}
		List<Relationship> wordRelations = sentence.orderedRelationships(Primitive.WORD);
		if (wordRelations == null) {
			return null;
		}
		long startTime = System.currentTimeMillis();
		log("Searching for similar questions", Bot.FINE);
		Map<Vertex, Integer> matches = new HashMap<Vertex, Integer>();
		long processTime = this.maxResponseMatchProcess;
		if (getBot().isDebugFine()) {
			log("Increasing processing time to allow debugging", Level.INFO, getBot().getDebugLevel());
			processTime = processTime * 20;
		}
		// Record all keyword matches.
		addQuestionMatches(sentence, network, startTime, processTime, wordRelations, matches, Primitive.KEYQUESTION, true);
		addQuestionMatches(sentence, network, startTime, processTime, wordRelations, matches, Primitive.QUESTION, true);
		Map<Vertex, Integer> keyWordsMatches = new HashMap<Vertex, Integer>(matches);
		addQuestionMatches(sentence, network, startTime, processTime, wordRelations, matches, Primitive.QUESTION, false);
		addQuestionMatches(sentence, network, startTime, processTime, wordRelations, matches, Primitive.SENTENCE, false);
		
		// Find the best match.
		Map.Entry<Vertex, Integer> bestMatch = null;
		int wordCount = wordRelations.size();
		double multiplier = (1.0 - percentage) * 15;
		int tooBig = (int) (wordCount * multiplier) + 2;
		int tooSmall = 0;
		int bestWordCount = 0;
		int bestAbs = 0;
		Relationship bestResponse = null;
		startTime = System.currentTimeMillis();
		log("Searching for best question match (min words, max words, match size)", Bot.FINE, tooSmall, tooBig, matches.size());
		for (Map.Entry<Vertex, Integer> entry : matches.entrySet()) {
			boolean hasKeyword = keyWordsMatches.containsKey(entry.getKey());
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > processTime) {
				log("Process time limit reached (time, matches)", Level.INFO, processTime, matches.size());
				break;
			}
			if (sentence == entry.getKey() || entry.getKey().instanceOf(Primitive.PATTERN)) {
				continue;
			}
			log("Processing (value, question)", Level.FINER, entry.getValue(), entry.getKey());
			if (bestMatch == null) {
				Collection<Relationship> relationships = entry.getKey().getRelationships(Primitive.WORD);
				if (relationships == null) {
					continue;
				}
				int entryWordCount = relationships.size();
				// Ignore if too big or too small.
				if (hasKeyword || (entryWordCount <= tooBig) && (entryWordCount >= tooSmall)) {
					bestResponse = bestResponse(percentage, input, entry.getKey(), sentence, previousResponse, network);
					if (bestResponse != null) {
						log("Possible question match (value, question)", Level.FINE, entry.getValue(), entry.getKey());
						bestMatch = entry;
						bestWordCount = entryWordCount;
						bestAbs = Math.abs(wordCount - bestWordCount);
					}
				}
			} else {
				if (entry.getValue() >= bestMatch.getValue()) {
					Collection<Relationship> relationships = entry.getKey().getRelationships(Primitive.WORD);
					if (relationships == null) {
						continue;
					}
					int entryWordCount = relationships.size();
					// Ignore if too big or too small.
					if (hasKeyword || (entryWordCount <= tooBig) && (entryWordCount >= tooSmall)) {
						int entryAbs = Math.abs(wordCount - entryWordCount);
						if ((entry.getValue() > bestMatch.getValue()) || (entryAbs < bestAbs)) {
							Relationship response = null;
							response = bestResponse(percentage, input, entry.getKey(), sentence, previousResponse, network);
							if (response != null) {
								log("Better question match (value, question)", Level.FINE, entry.getValue(), entry.getKey());
								bestMatch = entry;
								bestWordCount = entryWordCount;
								bestAbs = entryAbs;
								bestResponse = response;
							}
						}
					}
				}
			}
		}
		if (bestResponse == null) {
			log("No valid question match", Bot.FINE);
			return null;
		}
		if (keyWordsMatches.containsKey(bestMatch.getKey())) {
			log("Question keyword match", Bot.FINE);
		} else {
			int max = computeMaxSentenceValue(sentence, sentence, network);
			// If % then ok.
			double required = max * percentage * 0.8;
			// Recompute value using all words, as some words may not store relation to sentence.
			int matchValue = computeMaxSentenceValue(bestMatch.getKey(), sentence, network);
			log("Question best match (score, max score, required score, question)", Bot.FINE, matchValue, max, required, bestMatch.getKey());
			if (matchValue < required) {
				log("Question bad match, insufficient score (score, required score, question)", Bot.FINE, matchValue, required, bestMatch.getKey());
				return null;
			}
			int matchMax = computeMaxSentenceValue(bestMatch.getKey(), bestMatch.getKey(), network);
			if (matchValue * multiplier < matchMax) {
				log("Question bad match, too generic (score, multiplier, value, match max, question)", Bot.FINE, matchValue, multiplier, matchValue * multiplier, matchMax, bestMatch.getKey());
				return null;			
			}
		}
		
		log("Question match response", Bot.FINE, bestResponse);
		return bestResponse;
	}
	
	/**
	 * Find the best pattern that matches the sentence.
	 */
	public Relationship matchPattern(Vertex sentence, Vertex previousResponse, Vertex input, Map<Vertex, Vertex> variables, Network network, float percentage) {
		List<Relationship> wordRelations = sentence.orderedRelationships(Primitive.WORD);
		if (wordRelations == null) {
			return null;
		}
		long startTime = System.currentTimeMillis();
		Map<Vertex, Integer> matches = new HashMap<Vertex, Integer>();
		long processTime = this.maxResponseMatchProcess;
		// Record all of the matches.
		List<Vertex> deferred = new ArrayList<Vertex>();
		for (Relationship wordRelation : wordRelations) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > processTime) {
				log("Pattern search time limit reached", Level.INFO, processTime, matches.size());					
				break;
			}
			Vertex word = wordRelation.getTarget();
			recordPatternValues(word, sentence, matches, network, deferred);
			Vertex lowercase = null;
			if (!(word.getData() instanceof String)) {
				return null;
			}
			String text = (String)word.getData();
			if (Utils.isCaps(text) || Utils.isCapitalized(text)) {
				lowercase = network.findByData(((String)word.getData()).toLowerCase());
			} else {
				lowercase = network.findByData(Utils.capitalize((String)word.getData()));					
			}
			if ((lowercase != null) && (lowercase != word)) {
				recordPatternValues(lowercase, sentence, matches, network, deferred);
			}
		}
		// Process words with lots of sentences last.
		for (Vertex word : deferred) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > processTime) {
				log("Pattern search time limit reached", Level.INFO, processTime, matches.size());					
				break;
			}
			recordPatternValues(word, sentence, matches, network, null);
		}
		// Find the best match.
		Map.Entry<Vertex, Integer> bestMatch = null;
		Relationship bestResponse = null;
		boolean bestHasUnderscore = false;
		startTime = System.currentTimeMillis();
		log("Found possible patterns", Level.FINE, matches.size());
		for (Map.Entry<Vertex, Integer> entry : matches.entrySet()) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > processTime) {
				log("Pattern process time limit reached", Level.INFO, processTime, matches.size());
				break;
			}
			if (sentence == entry.getKey()) {
				continue;
			}
			if (bestMatch == null) {
				if (!evaluatePattern(entry.getKey(), sentence, Primitive.WILDCARD, variables, network)) {
					continue;
				}
				bestResponse = bestResponse(percentage, input, entry.getKey(), sentence, previousResponse, network);
				if (bestResponse != null) {
					bestMatch = entry;
					bestHasUnderscore = entry.getKey().hasRelationship(Primitive.WORD, Primitive.UNDERSCORE)
								|| entry.getKey().hasRelationship(Primitive.WORD, Primitive.POUNDWILDCARD)
								|| entry.getKey().hasRelationship(Primitive.TYPE, Primitive.PRECEDENCE);
				}
			} else {
				boolean hasUnderscore = entry.getKey().hasRelationship(Primitive.WORD, Primitive.UNDERSCORE)
							|| entry.getKey().hasRelationship(Primitive.WORD, Primitive.POUNDWILDCARD)
							|| entry.getKey().hasRelationship(Primitive.TYPE, Primitive.PRECEDENCE);
				if (entry.getValue() > bestMatch.getValue() || (hasUnderscore && ! bestHasUnderscore)) {
					if (!evaluatePattern(entry.getKey(), sentence, Primitive.WILDCARD, variables, network)) {
						continue;
					}
					Relationship response = bestResponse(percentage, input, entry.getKey(), sentence, previousResponse, network);
					if (response != null) {
						bestResponse = response;
						if (bestResponse != null) {
							bestMatch = entry;
							bestHasUnderscore = hasUnderscore;
						}
					}
				}
			}
		}
		if (bestResponse == null) {
			log("No valid pattern", Bot.FINE);
			return null;
		}
		log("Pattern match", Bot.FINE, bestMatch.getKey(), bestResponse);
		return bestResponse;
	}
	
	/**
	 * Attempt to understand the sentence using state machines.
	 */
	public Vertex processUnderstanding(Vertex input, Vertex sentence, float correctnessRequired, Map<Vertex, Vertex> variables, Network network) {
		if (!sentence.hasRelationship(Primitive.WORD)) {
			return null;
		}
		// Lookup and apply language rules.
		List<Vertex> states = network.createVertex(getPrimitive()).orderedRelations(Primitive.STATE);
		if (states == null || states.isEmpty()) {
			return null;
		}
		List<Vertex> compoundWords = processCompoundWords(sentence.orderedRelationships(Primitive.WORD));
		Vertex compoundSentence = sentence;
		// Check if the input has compound words.
		if (compoundWords != null) {
			if (compoundWords != null) {
				// Sentence had compound words, so need to create new sentence and switch input.
				compoundSentence = network.createInstance(Primitive.SENTENCE);
				for (int index = 0; index < compoundWords.size(); index++) {
					Vertex word = compoundWords.get(index);
					compoundSentence.addRelationship(Primitive.WORD, word, index);
				}
			}
		}
		List<Vertex> inputs = new ArrayList<Vertex>(1);
		inputs.add(input);
		Vertex response = checkState(null, input, compoundSentence, states, 0, 0, inputs, variables, new ArrayList<Vertex>(), correctnessRequired, network);
		if ((response == null) && (compoundWords != null)) {
			// If had compound words, also check without them.
			variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(input, sentence, network, variables);
			response = checkState(null, input, sentence, states, 0, 0, inputs, variables, new ArrayList<Vertex>(), correctnessRequired, network);
		}
		if (response != null) {
			response = getWord(response, network);
			if (response != null) {
				log("Sentence understood", Bot.FINE, sentence, response);
			} else {
				log("Sentence understood but no words", Bot.FINE, sentence);
				response = null;
			}
		} else {			
			log("Sentence not understood", Bot.FINE, sentence);
		}
		return response;
	}
	
	/**
	 * Transform the list of words, into a list of compound words.
	 */
	public List<Vertex> processCompoundWords(List<Relationship> words) {
		List<Vertex> compoundedWords = new ArrayList<Vertex>(words.size());
		for (int index = 0; index < words.size(); index++) {
			Vertex word = words.get(index).getTarget();
			// Get the compound words for each word.
			Collection<Relationship> compoundWords = word.getRelationships(Primitive.COMPOUND_WORD);
			if (compoundWords == null) {
				// No compound words, so just add the single word.
				compoundedWords.add(word);
			} else {
				boolean found = false;
				// Check each compound word, to see if it follows the word.
				for (Relationship compundWord : compoundWords) {
					int compoundIndex = 1;
					List<Relationship> wordParts = compundWord.getTarget().orderedRelationships(Primitive.WORD);
					if ((wordParts == null) || (wordParts.size() <= 1)) {
						// Avoid corrupt data (from forgetfullness).
						break;
					}
					// Check that all the compound words are contained.
					while (((index + compoundIndex) < words.size()) && (compoundIndex < wordParts.size())) {
						Vertex wordPart = wordParts.get(compoundIndex).getTarget();
						Vertex nextWord = words.get(index + compoundIndex).getTarget();
						// If the words don't match, then stop checking and check the next compound word.
						if (!wordPart.equals(nextWord)) {
							break;
						}
						compoundIndex++;
					}
					// If the compound word is a match, the add it.
					// TODO: What if multiple compound words match?  Take longest, or most conscious?
					if (compoundIndex == wordParts.size()) {
						found = true;
						log("Compound word found", Bot.FINE, compundWord.getTarget());
						compoundedWords.add(compundWord.getTarget());
						index = index + compoundIndex - 1;
						break;
					}
				}
				if (!found) {
					for (Relationship compundWord : compoundWords) {
						int compoundIndex = 1;
						List<Relationship> wordParts = compundWord.getTarget().orderedRelationships(Primitive.WORD);
						if ((wordParts == null) || (wordParts.size() <= 1)) {
							// Avoid corrupt data (from forgetfullness).
							break;
						}
						// Check that all the compound words are contained.
						while (((index + compoundIndex) < words.size()) && (compoundIndex < wordParts.size())) {
							Vertex wordPart = wordParts.get(compoundIndex).getTarget();
							Vertex nextWord = words.get(index + compoundIndex).getTarget();
							// If the words don't match, then stop checking and check the next compound word.
							if (!wordPart.equalsIgnoreCase(nextWord)) {
								break;
							}
							compoundIndex++;
						}
						// If the compound word is a match, the add it.
						// TODO: What if multiple compound words match?  Take longest, or most conscious?
						if (compoundIndex == wordParts.size()) {
							found = true;
							log("Compound word found", Bot.FINE, compundWord.getTarget());
							compoundedWords.add(compundWord.getTarget());
							index = index + compoundIndex - 1;
							break;
						}
					}
				}
				if (!found) {
					compoundedWords.add(word);
				}
			}
		}
		if (words.size() == compoundedWords.size()) {
			return null;
		}
		return compoundedWords;
	}

	/**
	 * Apply each state machine vertex to the sentence of words.
	 * If the state machine finds a match, it will record the real vertices mapped to the state machine variables.
	 */
	public Vertex checkState(Vertex root, Vertex input, Vertex sentence, List<Vertex> states, int index, int recurse, List<Vertex> inputs, Map<Vertex, Vertex> variables, List<Vertex> stateStack, float correctnessRequired, Network network) {
		if (states == null || this.abort) {
			return null;
		}
		if (this.startTime == 0) {
			this.startTime = System.currentTimeMillis();
		}
		long processTime = this.maxStateProcess;
		Vertex state = null;
		try {
			while (index <= inputs.size()) {
				Vertex currentInput = null;
				if (index < inputs.size()) {
					currentInput = inputs.get(index);
				}
				// Check each state machine for a match.
				for (ListIterator<Vertex> iterator = states.listIterator(); iterator.hasNext(); ) {
					if (this.abort) {
						return null;
					}
					if ((System.currentTimeMillis() - startTime) > processTime) {
						log("State processing time limit reached", Level.WARNING, processTime, state);
						this.abort = true;
						return null;
					}
					if (stateStack.size() > MAX_STACK) {
						log("State stack overflow", Level.WARNING, MAX_STACK, state);
						this.abort = true;
						return null;
					}
					// Record local variables so they can be discarded if there is no match.
					Map<Vertex, Vertex> localVariables = new HashMap<Vertex, Vertex>(variables);
					state = iterator.next();
					Vertex lastState = null;
					if (!stateStack.isEmpty()) {
						lastState = stateStack.get(stateStack.size() - 1);
					}
					stateStack.add(state);
					Vertex newRoot = root;
					if (root == null) {
						newRoot = state;
						log("STATE MACHINE", Level.FINE, state, currentInput);
						if (!this.loadedStates.contains(state.getId()) && (stateStack.size() == 1) && this.loadedStates.size() < 20) {
							this.loadedStates.add(state.getId());
							SelfCompiler.getCompiler().fastLoadChildren(state);
						}
						// Record root state variable.
						localVariables.put(network.createVertex(Primitive.STATE), state);
					} else {
						log("STATE", Level.FINER, state, currentInput);
					}
					Collection<Relationship> equations = state.orderedRelationships(Primitive.DO);
					Vertex response = null;
					if (equations != null) {
						for (Relationship equationRelationship : equations) {
							if (this.abort) {
								return null;
							}
							if ((System.currentTimeMillis() - startTime) > processTime) {
								log("State processing time limit reached", Level.WARNING, processTime, state);
								this.abort = true;
								return null;
							}
							Vertex equation = equationRelationship.getTarget();
							if (equation.instanceOf(Primitive.CASE) || equation.hasRelationship(Primitive.PATTERN)) {
								Vertex pattern = equation.getRelationship(Primitive.PATTERN);
								Vertex caseVariable = null;
								Boolean match = false;
								boolean anyOrNone = false;
								boolean emptyMatch = false;
								if (pattern != null) {
									log("PATTERN", Level.FINER, pattern, currentInput);
									caseVariable = pattern;
									// Avoid checking pattern again if in recursive state.
									if (lastState != state) {
										match = evaluatePattern(pattern, sentence, Primitive.WILDCARD, localVariables, network);
									}
								} else {
									caseVariable = equation.getRelationship(Primitive.CASE);
									if (caseVariable != null && caseVariable.instanceOf(Primitive.EQUATION)) {
										caseVariable = caseVariable.applyQuotient(localVariables);
									}
									if (caseVariable != null) {
										anyOrNone = (caseVariable.getName() != null && (caseVariable.getName().equals("poundstar") || caseVariable.getName().equals("hatstar")))
												|| (caseVariable.instanceOf(Primitive.LIST) && !caseVariable.hasRelationship(Primitive.TYPE, Primitive.REQUIRED));
									}
									if (index >= inputs.size()) {
										if (anyOrNone) {
											emptyMatch = true;
											match = Boolean.TRUE;
											log("CASE", Level.FINER, caseVariable, currentInput);
										} else {
											continue;
										}
									} else {
										currentInput = inputs.get(index);
										log("CASE", Level.FINER, caseVariable, currentInput);
										if (caseVariable != null) {
											match = caseVariable.matches(currentInput, localVariables);
											if (currentInput == input) {
												Vertex sentenceVariable = caseVariable.getRelationship(Primitive.INPUT);
												if (sentenceVariable != null) {
													localVariables.put(sentenceVariable, sentence);
												}
											}
										}
										if (match != Boolean.TRUE && anyOrNone) {
											emptyMatch = true;
											match = Boolean.TRUE;											
										}
									}
								}
								if (match == Boolean.TRUE) {
									Vertex topic = equation.getRelationship(Primitive.TOPIC);
									if (topic != null) {
										match = false;
										Vertex conversation = input.getRelationship(Primitive.CONVERSATION);
										Vertex currentTopic = null;
										if (conversation != null) {
											currentTopic = conversation.getRelationship(Primitive.TOPIC);
										}
										log("Checking topic", Level.FINER, topic, currentTopic);
										if (currentTopic != null) {
											match = evaluatePattern(topic, currentTopic, Primitive.TOPICWILDCARD, localVariables, network);
										}
									}
									Vertex that = equation.getRelationship(Primitive.THAT);
									if (match && (that != null)) {
										match = false;
										Vertex questionInput = input.getRelationship(Primitive.QUESTION);
										log("Checking that", Level.FINER, that, questionInput);
										if (questionInput != null) {
											Vertex question = questionInput.getRelationship(Primitive.INPUT);
											if (question != null) {
												match = evaluatePattern(that, question, Primitive.THATWILDCARD, localVariables, network);
											}
										}											
									}
								}
								// Check if the word matches the state machine variable.
								if (match == Boolean.TRUE) {
									if (pattern != null) {
										log("PATTERN MATCH", Level.FINE, pattern, currentInput);
									} else {
										log("CASE MATCH", Level.FINER, caseVariable, currentInput);
									}
									// Check next state/word recursively.
									Vertex template = equation.getRelationship(Primitive.TEMPLATE);
									if (template != null) {
										if (template.instanceOf(Primitive.FORMULA)) {
											log("Template is formula", Level.FINE, template);
											response = evaluateFormula(template, localVariables, network);
											if (response == null) {
												log("Formula cannot be evaluated", Level.FINE, template);
											} else {
												return response;
											}
										} else if (template.instanceOf(Primitive.EQUATION)) {
											response = evaluateQuotientResponse(template, state, localVariables, network);
											if (response != null) {
												return response;
											}
										} else {
											return template;
										}
									}
									// Check for AS, maps case variable into target variable.
									Vertex as = equation.getRelationship(Primitive.AS);
									if (as != null) {
										localVariables.put(as, localVariables.get(caseVariable));
									}
									List<Vertex> gotoStates = equation.orderedRelations(Primitive.GOTO);
									if (gotoStates != null) {
										if ((gotoStates.size() == 1) && gotoStates.get(0).is(Primitive.RETURN)) {
											log("CASE RETURN", Level.FINER);
											return null;
										}
										List<Relationship> arguments = equation.orderedRelationships(Primitive.FOR);
										List<Vertex> newInputs = inputs;
										int newIndex = index + 1;
										if (arguments != null) {
											Vertex variable = arguments.get(1).getTarget();
											Vertex value = arguments.get(0).getTarget();
											newInputs = new ArrayList<Vertex>();
											List<Relationship> relationships = variable.applyQuotient(localVariables).orderedRelationships(value);
											if (relationships != null) {
												for (Relationship result : relationships) {
													newInputs.add(result.getTarget());
												}
											}
											newIndex = 0;
										}
										log("CASE GOTO STATE", Level.FINER, gotoStates);
										if (!emptyMatch) {
											response = checkState(newRoot, input, sentence, gotoStates, newIndex, recurse, newInputs, localVariables, stateStack, correctnessRequired, network);
										}
										if (response != null) {
											return response;
										}
										if (anyOrNone && (newIndex == index + 1) &&  !gotoStates.contains(state)) {
											// Also check matching nothing.
											response = checkState(newRoot, input, sentence, gotoStates, index, recurse, newInputs, localVariables, stateStack, correctnessRequired, network);
										}
										if (response != null) {
											return response;
										}
									}
								} else {
									log("Case not matched", Level.FINER, caseVariable, currentInput);
								}
							} else if (equation.instanceOf(Primitive.DO)) {
								log("DO", Level.FINER, state, currentInput);
								equation.getRelationship(Primitive.DO).applyQuotient(localVariables);
								localVariables.remove(network.createVertex(Primitive.RETURN));
							} else if (equation.instanceOf(Primitive.GOTO)) {
								// May require terminal state.
								if (!equation.hasRelationship(Primitive.FINALLY) || (index >= inputs.size())) {
									List<Vertex> gotoStates = equation.orderedRelations(Primitive.GOTO);
									log("GOTO", Level.FINER, state, gotoStates);
									List<Vertex> arguments = equation.orderedRelations(Primitive.ARGUMENT);
									if (arguments == null || arguments.isEmpty()) {
										response = checkState(newRoot, input, sentence, gotoStates, index, recurse, inputs, localVariables, stateStack, correctnessRequired, network);
									} else {
										List<Vertex> newInputs = new ArrayList<Vertex>();
										for (Vertex argument : arguments) {
											newInputs.add(argument.applyQuotient(localVariables));
										}
										response = checkState(newRoot, input, sentence, gotoStates, 0, recurse, newInputs, localVariables, stateStack, correctnessRequired, network);
									}
									if (response != null) {
										return response;
									}
								}
							} else if (equation.instanceOf(Primitive.PUSH)) {
								Vertex argument = equation.getRelationship(Primitive.ARGUMENT).applyQuotient(localVariables);
								log("PUSH", Level.FINER, state, argument);
								inputs.add(index, argument);								
							} else if (equation.instanceOf(Primitive.RETURN)) {
								log("RETURN", Level.FINER, state, currentInput);
								List<Vertex> lastStates = new ArrayList<Vertex>(1);
								Vertex current = stateStack.remove(stateStack.size() - 1);
								Vertex last = stateStack.remove(stateStack.size() - 1);
								lastStates.add(last);
								Map<Vertex, Vertex> newVariables = localVariables;
								// Allow the return to clear the variable stack, or keep specific variables.
								Collection<Relationship> arguments = equation.getRelationships(Primitive.ARGUMENT);
								if (arguments != null) {
									newVariables = new HashMap<Vertex, Vertex>(variables);
									for (Relationship variable : arguments) {
										newVariables.put(variable.getTarget(), localVariables.get(variable.getTarget()));
									}
								}
								// A return value is used as the next state value, (reruns the previous state with the new value).
								Vertex value = equation.getRelationship(Primitive.RETURN);
								if (value != null && value.isVariable()) {
									value = localVariables.get(value);
									if (value == null) {
										value = network.createVertex(Primitive.NULL);
									}
								}
								if (value != null) {
									List<Vertex> newInputs = new ArrayList<Vertex>(inputs);
									if (currentInput == null) {
										newInputs.add(value);										
									} else {
										index++;
										newInputs.add(index, value);
									}
									recurse++;
									if (recurse > MAX_DEPTH) {
										stateStack.add(last);
										stateStack.add(current);
										throw new SelfExecutionException(current, "Max recursive state execution");
									}
									response = checkState(newRoot, input, sentence, lastStates, index++, recurse, newInputs, newVariables, stateStack, correctnessRequired, network);									
								} else {
									response = checkState(newRoot, input, sentence, lastStates, index++, recurse, inputs, newVariables, stateStack, correctnessRequired, network);
								}
								if (response != null) {
									return response;
								}
								stateStack.add(last);
								stateStack.add(current);
							}
						}
					}
					// Ignore punctuation.
					if (currentInput != null && currentInput.instanceOf(Primitive.PUNCTUATION)) {
						response = checkState(newRoot, input, sentence, states, index+1, recurse, inputs, localVariables, stateStack, correctnessRequired, network);
						if (response != null) {
							return response;
						}
					}
					
					// Check if the entire sentence was matched.
					if ((index >= inputs.size())) {
						Vertex[] pair = bestQuotient(correctnessRequired, state, localVariables, input, sentence, network);
						if (pair != null) {
							response = pair[1];
							log("ANSWER", Level.FINER, state, response);
							Vertex quotient = pair[0];
							setLastStateMachine(root);
							setLastState(state);
							setLastQuotient(quotient);
							return response;
						}
					}
					stateStack.remove(stateStack.size() - 1);
					log("STATE DONE", Level.FINER, state, currentInput);
				}
				index++;
				return null;
			}
		} catch (Exception failure) {
			log("Error occured in processing state", Level.WARNING, root == null ? state : root);
			log(failure);
			return null;
		} finally {
			if (root == null) {
				log("State processing time", Level.FINE, System.currentTimeMillis() - startTime);
			}
		}
		return null;
	}
	
	/**
	 * Return the best response to the question, taking into account the input history.
	 */
	public Vertex[] bestQuotient(float percentage, Vertex state, Map<Vertex, Vertex> localVariables, Vertex input, Vertex sentence, Network network) {
		// Check if any response has the same previous question.
		Vertex previousQuestionInput = input.getRelationship(Primitive.QUESTION);
		Vertex previousQuestion = null;
		if (previousQuestionInput != null) {
			previousQuestion = previousQuestionInput.getRelationship(Primitive.INPUT);
			if (previousQuestion != null) {
				Collection<Relationship> quotients = state.getRelationships(Primitive.QUOTIENT);
				if (quotients != null) {
					Vertex bestResponse = null;
					Relationship best = null;
					Relationship bestWithPrevious = null;
					for (Relationship quotient : quotients) {
						if (quotient.getCorrectness() >= percentage) {
							Vertex meta = quotient.getMeta();
							if (meta != null) {
								if (hasPrevious(meta, previousQuestion, state, localVariables, network)) {
									if ((bestWithPrevious == null) || (quotient.getCorrectness() > bestWithPrevious.getCorrectness())) {
										Vertex response = evaluateQuotientResponse(quotient.getTarget(), state, localVariables, network);
										if ((response == null) || sentence.hasInverseRelationship(Primitive.RESPONSE, response)
													|| sentence.hasAnyAssociatedInverseRelationship(Primitive.SYNONYM, response, Primitive.RESPONSE)) {
											continue;
										} else {
											bestWithPrevious = quotient;
											bestResponse = response;
										}
									}
								}
								if (meta.hasInverseRelationship(Primitive.PREVIOUS, previousQuestion) || meta.hasRelationship(Primitive.REQUIRE, Primitive.PREVIOUS)) {
									continue;
								}
							}
							if ((bestWithPrevious == null) && ((best == null) || (quotient.getCorrectness() > best.getCorrectness()))) {
								Vertex response = evaluateQuotientResponse(quotient.getTarget(), state, localVariables, network);
								if ((response == null) || sentence.hasInverseRelationship(Primitive.RESPONSE, response)
											|| sentence.hasAnyAssociatedInverseRelationship(Primitive.SYNONYM, response, Primitive.RESPONSE)) {
									continue;
								} else {
									best = quotient;
									bestResponse = response;
								}
							}
						}
					}
					if (bestWithPrevious != null) {
						Vertex[] pair = new Vertex[2];
						pair[0] = bestWithPrevious.getTarget();
						pair[1] = bestResponse;
						return pair;
					}
					if (bestResponse == null) {
						return null;
					}
					Vertex[] pair = new Vertex[2];
					pair[0] = best.getTarget();
					pair[1] = bestResponse;
					return pair;
				}
			}
		}
		Vertex quotient = state.mostConscious(Primitive.QUOTIENT, percentage);
		if (quotient == null) {
			return null;
		}
		Vertex response = evaluateQuotientResponse(quotient, state, localVariables, network);
		if ((response == null) || sentence.hasInverseRelationship(Primitive.RESPONSE, response)
				|| sentence.hasAnyAssociatedInverseRelationship(Primitive.SYNONYM, response, Primitive.RESPONSE)) {
			quotient = state.nextMostConscious(Primitive.QUOTIENT, quotient, percentage);
			response = evaluateQuotientResponse(quotient, state, localVariables, network);
			if ((response == null) || sentence.hasInverseRelationship(Primitive.RESPONSE, response)
						|| sentence.hasAnyAssociatedInverseRelationship(Primitive.SYNONYM, response, Primitive.RESPONSE)) {
				return null;
			}
		}
		Vertex[] pair = new Vertex[2];
		pair[0] = quotient;
		pair[1] = response;
		return pair;
	}
	
	/**
	 * Return the best response to the question, taking into account the input history.
	 */
	public boolean hasPrevious(Vertex meta, Vertex sentence, Vertex state, Map<Vertex, Vertex> localVariables, Network network) {
		Collection<Relationship> relationship = meta.getRelationships(Primitive.PREVIOUS);
		if (relationship == null) {
			return false;
		}
		if (meta.hasRelationship(Primitive.PREVIOUS, sentence)) {
			return true;
		}
		for (Relationship previous : relationship) {
			if (previous.getTarget().instanceOf(Primitive.FORMULA)) {
				log("Previous is formula", Level.FINE, previous.getTarget());
				Vertex result = evaluateFormula(previous.getTarget(), localVariables, network);
				if (result == null) {
					log("Formula cannot be evaluated", Level.FINE, previous.getTarget());					
				} else if (result.equals(sentence)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Evaluate the quotient and possible forumula response.
	 */
	public Vertex evaluateQuotientResponse(Vertex quotient, Vertex state, Map<Vertex, Vertex> localVariables, Network network) {		
		Vertex response = null;
		if (quotient != null) {
			log("Applying quotient", Level.FINE, quotient, state);			
			response = quotient.applyQuotient(localVariables);
			localVariables.remove(network.createVertex(Primitive.RETURN));
			log("Quotient result", Level.FINE, response);
			// Check for formula and transpose
			if ((response != null) && response.instanceOf(Primitive.FORMULA)) {
				log("Quotient is formula", Level.FINE, response);
				response = evaluateFormula(response, localVariables, network);
				if (response == null) {
					log("Formula cannot be evaluated", Level.FINE, response);
				}
			} else if ((response != null) && !response.hasData() && !response.instanceOf(Primitive.PARAGRAPH)) {
				response = createSentenceText(response, network);
			}
			if ((response != null) && response.is(Primitive.NULL)) {
				response = null;
			}
		}
		return response;
	}
	
	/**
	 * Create the text for the sentence.
	 */
	public Vertex createSentenceText(Vertex vertex, Network network) {
		StringWriter writer = new StringWriter();
		List<Relationship> relationships = vertex.orderedRelationships(Primitive.WORD);
		Vertex previous = network.createVertex(Primitive.NULL);
		boolean inferWhitespace = !vertex.hasRelationship(Primitive.TYPE, Primitive.SPACE) && !vertex.hasRelationship(Primitive.WORD, Primitive.SPACE);
		boolean caseSensitive = !this.fixFormulaCase || vertex.hasRelationship(Primitive.TYPE, Primitive.CASESENSITVE);
		if (relationships != null) {
			boolean first = true;
			String last = null;
			for (int index = 0; index < relationships.size(); index++) {
				Relationship relationship = relationships.get(index);
				Vertex next = network.createVertex(Primitive.NULL);
				if (relationships.size() > (index + 1)) {
					next = relationships.get(index + 1).getTarget();
				}
				Vertex word = relationship.getTarget();
				if (word.is(Primitive.SPACE)) {
					writer.write(" ");
					continue;
				}
				if (!(word.getData() instanceof String)) {
					word = getWordFollowing(word, relationship, previous, next, network);
				}
				String text = word.printString();
				if (!caseSensitive) {
					if (first) {
						text = Utils.capitalize(text);
					} else if (text.equals("i") || text.equals("I")) {
						text = text.toUpperCase();
					} else {
						boolean isName = word.instanceOf(Primitive.NAME);
						boolean isNoun = word.instanceOf(Primitive.NOUN);
						boolean isVerb = word.instanceOf(Primitive.VERB) || word.instanceOf(Primitive.ARTICLE)
								|| word.instanceOf(Primitive.PRONOUN) || word.instanceOf(Primitive.QUESTION);
						if (isName && !isVerb) {
							text = Utils.capitalize(text);
						} else if (isVerb && !isName && !isNoun) {
							text = text.toLowerCase();						
						}
					}
				}
				if (inferWhitespace) {
					if (!first && !text.equals("'") && !text.equals(")") && !"(".equals(last) && !"'".equals(last) && !text.equals("^") && !"^".equals(last) && (!(word.instanceOf(Primitive.PUNCTUATION)))) {
						writer.write(" ");
					}
				}
				writer.write(text);
				first = text.equals(".");
				previous = word;
				last = text;
			}
		}
		String text = writer.toString();
		Vertex sentence = network.createSentence(text.trim(), true);
		if (vertex.hasRelationship(Primitive.TYPE, Primitive.CASESENSITVE)) {
			sentence.addRelationship(Primitive.TYPE, Primitive.CASESENSITVE);
		}
		return sentence;
	}
	
	/**
	 * Return the sentence or word for the vertex.
	 */
	public Vertex getWord(Vertex vertex, Network network) {
		if (vertex.instanceOf(Primitive.SENTENCE) || vertex.instanceOf(Primitive.WORD)) {
			if (!vertex.hasData()) {
				// If it was a self created sentence it may not have text.
				vertex = createSentenceText(vertex, network);
			}
			if (!vertex.instanceOf(Primitive.SENTENCE)) {
				vertex.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
			}
			return vertex;
		}
		// May be a string that needs to be converted to a sentence.
		if (vertex.hasData() && (vertex.getData() instanceof String)) {
			return network.createSentence((String)vertex.getData());
		}
		if (vertex.instanceOf(Primitive.PARAGRAPH)) {
			return vertex;
		}
		Vertex word = vertex.mostConscious(Primitive.SENTENCE);
		if (word == null) {
			Vertex previous = network.createVertex(Primitive.NULL);
			word = getWordFollowing(vertex, previous, previous, network);
		}
		return word;
	}
	
	/**
	 * Return the sentence or word for the vertex.
	 */
	public static Vertex getWordFollowing(Vertex vertex, Vertex previousWord, Vertex nextWord, Network network) {
		return getWordFollowing(vertex, null, previousWord, nextWord, network);
	}
	
	/**
	 * Return the sentence or word for the vertex.
	 */
	public static Vertex getWordFollowing(Vertex vertex, Relationship relationship, Vertex previousWord, Vertex nextWord, Network network) {
		if (vertex.instanceOf(Primitive.WORD)) {
			if (!vertex.hasData()) {
				// Check if it is a newly create compound word that needs the text.
				if (vertex.instanceOf(Primitive.COMPOUND_WORD)) {
					Collection<Relationship> wordParts = vertex.orderedRelationships(Primitive.WORD);
					if (wordParts != null) {
						StringWriter writer = new StringWriter();
						boolean first = true;
						for (Relationship wordPart : wordParts) {
							if (!first) {
								writer.write(" ");
							} else {
								first = false;
							}
							writer.write(wordPart.getTarget().getDataValue());
						}
						return network.createWord(writer.toString());
					}
				}
			}
			return vertex;
		}
		if (vertex.instanceOf(Primitive.FRAGMENT)) {
			if (!vertex.hasData()) {
				return network.createFragment(printFragment(vertex, previousWord, nextWord, network));
			}
			return vertex;
		}
		if (vertex.instanceOf(Primitive.SENTENCE)) {
			if (!vertex.hasData()) {
				return network.createSentence(printFragment(vertex, previousWord, nextWord, network));
			}
			return vertex;
		}
		if (vertex.instanceOf(Primitive.PARAGRAPH)) {
			return vertex;
		}
		if (vertex.instanceOf(Primitive.LIST)) {
			if (!vertex.hasData()) {
				// Print the list.
				Collection<Relationship> values = vertex.orderedRelationships(Primitive.SEQUENCE);
				if (values != null) {
					StringWriter writer = new StringWriter();
					int index = 0;
					for (Relationship value : values) {
						if (!value.isInverse()) {
							if (index != 0) {
								writer.write(", ");
								if (index == (values.size() - 1)) {
									writer.write("and ");
								}
							}
							writer.write(getWordFollowing(value.getTarget(), previousWord, nextWord, network).getDataValue());
							index++;
						}
					}
					return network.createFragment(writer.toString());
				} else {
					return network.createWord("empty");					
				}
			}
		}
		// Verb conjugation
		Vertex tense = null;
		if (relationship != null && relationship.hasMeta()) {
			tense = relationship.getMeta().getRelationship(Primitive.TENSE);
		}
		Vertex word = null;
		if (vertex.instanceOf(Primitive.ACTION)) {
			word = vertex.getAssoiate(network.createVertex(Primitive.WORD),
				previousWord, network.createVertex(Primitive.CONJUGATION),
				tense, network.createVertex(Primitive.TENSE),
				previousWord, network.createVertex(Primitive.PREVIOUS));
		}
		if (word == null) {
			// Pronoun subjuction
			Collection<Relationship> types = null;
			if (relationship != null && relationship.hasMeta()) {
				types = relationship.getMeta().getRelationships(Primitive.TYPE);
			}
			if (types == null) {
				word = vertex.mostConscious(Primitive.NAME);
			}
			if (word == null) {
				// What sounds right
				if (nextWord != null && !nextWord.instanceOf(Primitive.WORD)) {
					word = vertex.getAssoiate(network.createVertex(Primitive.WORD),
							previousWord, network.createVertex(Primitive.PREVIOUS),
							nextWord.getRelationships(Primitive.WORD), network.createVertex(Primitive.NEXT),
							types, network.createVertex(Primitive.TYPE), null);
					nextWord = nextWord.getRelationship(Primitive.WORD);
				} else {
					word = vertex.getAssoiate(network.createVertex(Primitive.WORD),
							previousWord, network.createVertex(Primitive.PREVIOUS),
							nextWord, network.createVertex(Primitive.NEXT),
							types, network.createVertex(Primitive.TYPE), null);
				}
			}
		}
		if (word == null) {
			word = vertex.mostConscious(Primitive.WORD);
		}
		if ((word == null) && (vertex.instanceOf(Primitive.SEQUENCE))) {
			// TODO assume digits for now, should probably change to sequence
			List<Relationship> digits = vertex.orderedRelationships(Primitive.DIGIT);
			if (digits != null) {
				StringWriter writer = new StringWriter();
				for (int index = digits.size() - 1; index >= 0; index--) {
					Relationship digit = digits.get(index);
					writer.write(String.valueOf(digit.getTarget().getData()));
				}
				word = network.createWord(writer.toString());
			}
		}
		if (word == null) {
			if (!vertex.hasData()) {
				// If no word, call it what it is.
				Collection<Relationship> classifications = vertex.getRelationships(Primitive.INSTANTIATION);
				if (classifications != null) {
					Vertex mostSpecialized = null;
					for (Relationship classification : classifications) {
						if (mostSpecialized == null) {
							mostSpecialized = classification.getTarget();
						} else if (classification.getTarget().hasRelationship(Primitive.SPECIALIZATION, mostSpecialized)) {
							mostSpecialized = classification.getTarget();
						}
					}
					word = mostSpecialized.mostConscious(Primitive.WORD);
				}
				if (word == null) {
					word = network.createVertex("");
				}
			} else if (vertex.isPrimitive()) {
				word = network.createWord(vertex.getDataValue());
			} else if (vertex.getData() instanceof Time) {				
				word = network.createWord(Utils.printTime((Time)vertex.getData(), "h:mm:ss a z"));
			} else {
				word = network.createFragment(vertex.getDataValue());
			}
		}
		return word;
	}
	
	public static String printFragment(Vertex fragment, Vertex previousWord, Vertex nextWord, Network network) {
		List<Relationship> values = fragment.orderedRelationships(Primitive.WORD);
		StringWriter writer = new StringWriter();
		int index = 0;
		if (values != null) {
			for (Relationship value : values) {
				if (index != 0) {
					writer.write(" ");
				}
				Vertex next = nextWord;
				if (index < values.size()) {
					next = values.get(index).getTarget();
				}
				Vertex word = getWordFollowing(value.getTarget(), previousWord, next, network);
				writer.write(word.printString());
				index++;
				previousWord = word;
			}
		}
		return writer.toString();
	}

	public Vertex getLastStateMachine(Network network) {
		if (this.lastStateMachineId == null) {
			return null;
		}
		return network.findById(this.lastStateMachineId);
	}

	public void setLastStateMachine(Vertex lastStateMachine) {
		if (lastStateMachine == null) {
			this.lastStateMachineId = null;
		} else {
			this.lastStateMachineId = lastStateMachine.getId();
		}
	}

	public Vertex getLastState(Network network) {
		if (this.lastStateId == null) {
			return null;
		}
		return network.findById(this.lastStateId);
	}

	public void setLastState(Vertex lastState) {
		if (lastState == null) {
			this.lastStateId = null;
		} else {
			this.lastStateId = lastState.getId();
		}
	}

	public Vertex getLastQuotient(Network network) {
		if (this.lastQuotientId == null) {
			return null;
		}
		return network.findById(this.lastQuotientId);
	}

	public void setLastQuotient(Vertex lastQuotient) {
		if (lastQuotient == null) {
			this.lastQuotientId = null;
		} else {
			this.lastQuotientId = lastQuotient.getId();
		}
	}

	public boolean getEnableEmote() {
		return enableEmote;
	}

	public void setEnableEmote(boolean enableEmote) {
		this.enableEmote = enableEmote;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * Load, compile, and add the state machine from the .self file.
	 */
	public void loadSelfFile(File file, String encoding, boolean debug) {
		try {
			loadSelfFile(new FileInputStream(file), encoding, MAX_FILE_SIZE, debug);
		} catch (IOException exception) {
			throw new SelfParseException("Parsing error occurred", exception);
		}
	}
	
	/**
	 * Load, compile, and add the state machine from the .self file.
	 */
	public void loadSelfFile(URL url, String encoding, boolean debug) {
		try {
			loadSelfFile(Utils.openStream(url), encoding, MAX_FILE_SIZE, debug);
		} catch (IOException exception) {
			throw new SelfParseException("Parsing error occurred", exception);
		}
	}
	
	/**
	 * Load, compile, and add the state machine from the .self file stream.
	 */
	public void loadSelfFile(InputStream stream, String encoding, int maxSize, boolean debug) {
		Network network = getBot().memory().newMemory();
		Vertex language = network.createVertex(getPrimitive());
		Vertex stateMachine = SelfCompiler.getCompiler().parseStateMachine(stream, debug, network, encoding, maxSize);
		SelfCompiler.getCompiler().pin(stateMachine);
		language.addRelationship(Primitive.STATE, stateMachine);
		network.save();
	}

	/**
	 * Load, parse, the aiml file as a chat log.
	 */
	public void loadAIMLFileAsLog(File file, String encoding, boolean pin) {
		try {
			loadAIMLFileAsLog(new FileInputStream(file), encoding, MAX_FILE_SIZE, pin);
		} catch (IOException exception) {
			throw new SelfParseException("Parsing error occurred", exception);
		}
	}

	/**
	 * Load, parse, the aiml file as a script.
	 */
	public void loadAIMLFile(File file, boolean createStates, boolean indexStatic, String encoding) {
		try {
			loadAIMLFile(new FileInputStream(file), file.getName(), createStates, false, indexStatic, encoding, MAX_FILE_SIZE);
		} catch (IOException exception) {
			throw new SelfParseException("Parsing error occurred", exception);
		}
	}
	
	/**
	 * Load, parse, the aiml file as a chat log.
	 */
	public void loadAIMLFileAsLog(InputStream stream, String encoding, int maxSize, boolean pin) {
		String text = Utils.loadTextFile(stream, encoding, MAX_FILE_SIZE);
		loadAIMLAsLog(text, pin);
	}
	
	/**
	 * Load, parse, the aiml as a chat log.
	 */
	public void loadAIMLAsLog(String text, boolean pin) {
		long start = System.currentTimeMillis();
		Network network = getBot().memory().newMemory();
		AIMLParser.parser().parseAIML(text, false, false, pin, false, null, network);
		network.save();
		log("AIML parsing time", Level.INFO, System.currentTimeMillis() - start);
	}
	
	/**
	 * Load, parse, the aiml file as a state machine.
	 */
	public void loadAIMLFile(InputStream stream, String name, boolean createStates, boolean mergeState, boolean indexStatic, String encoding, int maxSize) {
		String text = Utils.loadTextFile(stream, encoding, MAX_FILE_SIZE);
		loadAIML(text, name, createStates, mergeState, indexStatic);
	}
	
	/**
	 * Load, parse, the aiml file as a state machine.
	 */
	public void loadAIML(String text, String name, boolean createStates, boolean mergeState, boolean indexStatic) {
		long start = System.currentTimeMillis();
		Network network = getBot().memory().newMemory();
		Vertex stateMachine = null;
		Vertex language = network.createVertex(getPrimitive());
		if (mergeState) {
			stateMachine = language.lastRelationship(Primitive.STATE);
		} 
		if (stateMachine == null) {
			stateMachine = network.createInstance(Primitive.STATE);
			stateMachine.setName(name);
			language.addRelationship(Primitive.STATE, stateMachine);
			TextData data = new TextData();
			data.setText(text);
			stateMachine.addRelationship(Primitive.SOURCECODE, network.createVertex(data));
		}
		stateMachine = AIMLParser.parser().parseAIML(text, true, createStates, false, indexStatic, stateMachine, network);
		SelfCompiler.getCompiler().pin(stateMachine);
		network.save();
		log("AIML parsing time", Level.INFO, System.currentTimeMillis() - start);
	}
}