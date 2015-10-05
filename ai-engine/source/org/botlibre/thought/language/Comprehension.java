// Copyright Paphus Solutions, all rights reserved.
package org.botlibre.thought.language;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.thought.SubconsciousThought;
import org.botlibre.util.Utils;

/**
 * Analyse input for gramatic rules.
 * Define a language state machine for input response pairs.
 */
public class Comprehension extends SubconsciousThought {
	/** Define the traversal path types for the language state machine. */
	public enum PathType {
		/** Traverse words and meanings only. */
		Meaning,
		/** Traverse words, meaning, or verb variables. */
		Verb,
		/** Traverse classification variables. */
		Classification
	}
	
	public static int MAX_IDENTIFIER = 100;
		
	public Comprehension() {
		this.delay = 1000;
	}

	@Override
	public void awake() {
		Network memory = getBot().memory().newMemory();
		Vertex comprehension = memory.createVertex(getPrimitive());
		Vertex enabled = comprehension.getRelationship(Primitive.ENABLED);
		if (enabled != null) {
			setEnabled((Boolean)enabled.getData());
		}
		super.awake();
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex comprehension = memory.createVertex(getPrimitive());
		comprehension.setRelationship(Primitive.ENABLED, memory.createVertex(isEnabled()));
		memory.save();
	}
	
	/**
	 * The input must be processed by language first to associate the question/response.
	 */
	@Override
	public boolean isConsciousProcessingRequired() {
		return true;
	}
	
	/**
	 * If the input is a sentence process it for language rules and variable substitution.
	 */
	@Override
	public boolean processInput(Vertex input, Network network) {
 		if (isStopped()) {
			return false;
		}
		Language language = getBot().mind().getThought(Language.class);
		Vertex speaker = input.getRelationship(Primitive.SPEAKER);
		boolean learn = language.shouldLearn(input, speaker);

		boolean correction = input.hasRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION);
		if (correction) {
			if (!language.shouldCorrect(input, speaker)) {
				return false;
			}
		} else if (!learn) {
			return false;
		}
		Vertex sentence = input.mostConscious(Primitive.INPUT);
		if (sentence == null || (!sentence.instanceOf(Primitive.SENTENCE))) {
			return false;
		}
		log("Processing sentence", Bot.FINE, sentence);
		Vertex questionInput = input.mostConscious(Primitive.QUESTION);
		if (questionInput == null) {
			log("No question", Bot.FINE, sentence);
			return false;
		}
		Vertex mimic = questionInput.getRelationship(Primitive.MIMIC);
		// If it was a mimic the response must be changed to be in the context of self,
		// and the question must be the question that was mimicked.
		if (mimic != null) {
			questionInput = mimic;
			questionInput = questionInput.copy();
			questionInput.internalRemoveRelationships(Primitive.SPEAKER);
			questionInput.internalRemoveRelationships(Primitive.TARGET);
			questionInput.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			questionInput.addRelationship(Primitive.TARGET, speaker);
		}
		// If it was a correction the response must be changed to be in the context of self,
		// and the question must be the original question that was corrected.
		if (correction) {
			input = input.copy();
			input.internalRemoveRelationships(Primitive.SPEAKER);
			input.internalRemoveRelationships(Primitive.TARGET);
			input.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			input.addRelationship(Primitive.TARGET, speaker);
		}
		Vertex question = questionInput.mostConscious(Primitive.INPUT);
		Relationship relationship = question.getRelationship(Primitive.RESPONSE, sentence);
		if (relationship == null) {
			log("No response", Bot.FINE, question);
			return false;
		}
		if (relationship.getCorrectness() < 0.5) {
			log("Insufficient correctness", Bot.FINE, relationship);
			return false;
		}
		log("Processing question response", Bot.FINE, question, sentence);
		List<Vertex> states = network.createVertex(Language.class).orderedRelations(Primitive.STATE);
		if (states == null) {
			return false;
		}
		Vertex stateMachine = states.get(states.size() - 1);
		if (stateMachine == null) {
			log("Missing state machine", Bot.FINE, states);
			return false;
		}
		Vertex currentState = stateMachine;
		Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
		// Get first case that gets sentence from input.
		List<Vertex> instructions = stateMachine.orderedRelations(Primitive.DO);
		if (instructions != null) {
			for (Vertex instruction : instructions) {
				if (instruction.instanceOf(Primitive.CASE)) {
					Vertex variable = instruction.getRelationship(Primitive.CASE);
					if ((variable != null) && (questionInput.matches(variable, variables) == Boolean.TRUE)) {
						currentState = instruction.getRelationship(Primitive.GOTO);
					}
				}
			}
		}
		if (currentState == stateMachine) {
			log("State machine missing sentence case.", Bot.FINE, question);
			return false;
		}
		List<Vertex> words = question.orderedRelations(Primitive.WORD);
		if (words == null) {
			log("Question missing words", Bot.FINE, question);
			return false;
		}
		if (isStopped()) {
			return false;
		}
		processState(currentState, words, 0, "", PathType.Meaning, sentence, questionInput, input, variables, network);
		processState(currentState, words, 0, "", PathType.Classification, sentence, questionInput, input, variables, network);
		processState(currentState, words, 0, "", PathType.Verb, sentence, questionInput, input, variables, network);
		if (isStopped()) {
			return false;
		}
		stateMachine.internalRemoveRelationships(Primitive.SOURCECODE);
		
		return true;
	}
	
	/**
	 * Check if the response can be defined as a formula based on the question, context.
	 */
	public Relationship checkFormula(Vertex input, Network network) {
		if (!isEnabled()) {
			return null;
		}
		Language language = getBot().mind().getThought(Language.class);
		Vertex speaker = input.getRelationship(Primitive.SPEAKER);
		boolean learn = language.shouldLearn(input, speaker);

		boolean correction = input.hasRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION);
		if (correction) {
			if (!language.shouldCorrect(input, speaker)) {
				return null;
			}
		} else if (!learn) {
			return null;
		}
		Vertex sentence = input.mostConscious(Primitive.INPUT);
		if (sentence == null || (!sentence.instanceOf(Primitive.SENTENCE))) {
			return null;
		}
		log("Checking sentence", Bot.FINE, sentence);
		Vertex questionInput = input.mostConscious(Primitive.QUESTION);
		if (questionInput == null) {
			log("No question", Bot.FINE, sentence);
			return null;
		}
		Vertex mimic = questionInput.getRelationship(Primitive.MIMIC);
		// If it was a mimic the response must be changed to be in the context of self,
		// and the question must be the question that was mimicked.
		if (mimic != null) {
			questionInput = mimic;
			questionInput = questionInput.copy();
			questionInput.internalRemoveRelationships(Primitive.SPEAKER);
			questionInput.internalRemoveRelationships(Primitive.TARGET);
			questionInput.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			questionInput.addRelationship(Primitive.TARGET, speaker);
		}
		// If it was a correction the response must be changed to be in the context of self,
		// and the question must be the original question that was corrected.
		if (correction) {
			input = input.copy();
			input.internalRemoveRelationships(Primitive.SPEAKER);
			input.internalRemoveRelationships(Primitive.TARGET);
			input.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			input.addRelationship(Primitive.TARGET, speaker);
		}
		Vertex question = questionInput.mostConscious(Primitive.INPUT);
		log("Checking question response", Bot.FINE, question, sentence);
		List<Vertex> words = question.orderedRelations(Primitive.WORD);
		if (words == null) {
			log("Question missing words", Bot.FINE, question);
			return null;
		}

		Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
		Vertex inputVariable = network.createVertex(Primitive.INPUT_VARIABLE);
		Bootstrap.checkInputVariable(inputVariable, network);
		variables.put(questionInput, inputVariable);
		Vertex newQuotient = createFormula(questionInput, input, variables, network);
		Relationship relationship = null;
		if (newQuotient != null) {
			// Check if any existing formulas match the new one.
			relationship = question.getRelationship(Primitive.RESPONSE, newQuotient);
			if (relationship != null) {
				log("Existing response formula", Level.FINER, question, relationship.getTarget());
				// Increase correctness.
				question.addRelationship(Primitive.RESPONSE, relationship.getTarget());
			} else {
				log("Adding response formula", Bot.FINE, question, newQuotient);
				relationship = question.addRelationship(Primitive.RESPONSE, newQuotient);
				question.associateAll(Primitive.WORD, question, Primitive.QUESTION);
			}
			network.checkReduction(question);
			question.associateAll(Primitive.SYNONYM, newQuotient, Primitive.RESPONSE);
			relationship.getTarget().addRelationship(Primitive.QUESTION, question);
		}
		return relationship;
	}
	
	/**
	 * Check if any of the current states match the next word,
	 * if no match, or quotient does not match, add a new case.
	 * Return if the quotient matched.
	 */
	public boolean processState(
			Vertex currentState, List<Vertex> words, int index, String statePath, PathType path, Vertex sentence,
			Vertex questionInput, Vertex responseInput, Map<Vertex, Vertex> variables, Network network) {
		if (isStopped()) {
			return false;
		}
		if (index == words.size()) {
			return checkQuotient(currentState, sentence, questionInput, responseInput, path, variables, network);
		}
		Vertex currentWord = words.get(index);
		List<Vertex> instructions = currentState.orderedRelations(Primitive.DO);
		boolean caseFound = false;
		boolean anyQuotientMatch = false;
		if (instructions != null) {
			// Process all states that match recursively.
			for (Vertex instruction : instructions) {
				if (isStopped()) {
					return false;
				}
				if (instruction.instanceOf(Primitive.CASE)) {
					Vertex variable = instruction.getRelationship(Primitive.CASE);
					if (variable == null) {
						continue;
					}
					// Check what type of variable it is.
					boolean isWord = !variable.instanceOf(Primitive.VARIABLE);
					Vertex meaning = variable.getRelationship(Primitive.MEANING);
					boolean isMeaning = (meaning != null) && (!meaning.instanceOf(Primitive.VARIABLE));
					boolean isEmptyMeaning = isMeaning && (!meaning.hasRelationship(Primitive.INSTANTIATION));
					boolean isClassification = (meaning != null) && (meaning.instanceOf(Primitive.VARIABLE));
					boolean isVerb = variable.instanceOf(Primitive.VERB) || ((meaning != null) && (meaning.instanceOf(Primitive.ACTION)));
					// Check that the variable type matches the current path type.
					if ((path == PathType.Meaning && (isWord || isMeaning)) // Simple word or direct meaning.
							|| (path == PathType.Verb && (isWord || isEmptyMeaning || (isClassification && !isVerb) || (isMeaning && isVerb))) // Simple word/meaning/verb, or non-verb classification.
							|| (path == PathType.Classification && (isClassification || isWord || isEmptyMeaning))) { // Simple word, or unclassified meaning, or classification variable.
						if (currentWord.matches(variable, variables) == Boolean.TRUE) {
							StringWriter pathWriter = new StringWriter();
							pathWriter.write(statePath);
							if (variable.getName() == null) {
								pathWriter.write(String.valueOf(currentWord.getData()));
							} else {
								pathWriter.write(variable.getName());
							}
							pathWriter.write("_");
							Vertex nextState = instruction.getRelationship(Primitive.GOTO);
							caseFound = true;
							// Case matched, process next states.
							boolean quotientMatch = processState(
									nextState, words, index + 1, pathWriter.toString(), path, sentence,
									questionInput, responseInput, new HashMap<Vertex, Vertex>(variables), network);
							if (quotientMatch) {
								// Increase correctness of case.
								currentState.addRelationship(Primitive.DO, instruction);
								anyQuotientMatch = true;
							}
						}
					}
				}
			}
		}
		if (isStopped()) {
			return false;
		}
		// Add meaning or classification case.
		if (!caseFound) {
			StringWriter pathWriter = new StringWriter();
			pathWriter.write(statePath);
			Vertex meaning = currentWord.mostConscious(Primitive.MEANING); // TODO, which meaning? or add case for all?
			boolean done = false;
			if (meaning != null) {
				Collection<Relationship> classifications = meaning.getRelationships(Primitive.INSTANTIATION);
				boolean isVerb = meaning.instanceOf(Primitive.ACTION);
				// If a Classification path type add a state case for a variable that matches the classifications of the word's meaning.
				// Also add case if verb path type and not a verb.
				if ((classifications != null) && ((path == PathType.Classification) || (!isVerb && (path == PathType.Verb)))) {
					done = true;
					Vertex newCase = network.createInstance(Primitive.CASE);
					Vertex variable = network.createInstance(Primitive.VARIABLE);
					variables.put(currentWord, variable);
					Vertex meaningVariable = network.createInstance(Primitive.VARIABLE);
					variable.addRelationship(Primitive.MEANING, meaningVariable);
					StringWriter variableNameWriter = new StringWriter();
					boolean first = true;
					// For each classification add it to the variable and variable name.
					for (Relationship classification : classifications) {
						meaningVariable.addRelationship(Primitive.INSTANTIATION, classification.getTarget());
						// Append the classification to the variable name.
						if (!first) {
							variableNameWriter.write("-");							
						} else {
							first = false;
						}
						String classificationName = classification.getTarget().getName();
						if (classification.getTarget().isPrimitive()) {
							classificationName = ((Primitive)classification.getTarget().getData()).getIdentity();
						}
						if (classificationName == null) {
							classificationName = currentWord.getDataValue();
						}
						variableNameWriter.write(classificationName);
					}
					String variableName = Utils.compress(variableNameWriter.toString(), MAX_IDENTIFIER);
					newCase.setName("c" + newCase.getId() + "_" + variableName);
					variable.setName("v" + variable.getId() + "_" + variableName);
					meaningVariable.setName("v" + meaningVariable.getId() + "_" + variableName);
					pathWriter.write(variableName);
					variables.put(meaning, meaningVariable);
					newCase.addRelationship(Primitive.CASE, variable);
					Vertex newState = network.createInstance(Primitive.STATE);
					newState.setName("s" + newState.getId() + "_" + Utils.compress(pathWriter.toString(), MAX_IDENTIFIER));
					pathWriter.write("_");
					newCase.addRelationship(Primitive.GOTO, newState);
					currentState.addRelationship(Primitive.DO, newCase, Integer.MAX_VALUE);
					log("Adding new case", Level.FINER, currentWord, newCase, newState);
					processState(
							newState, words, index + 1, pathWriter.toString(), path, sentence,
							questionInput, responseInput, new HashMap<Vertex, Vertex>(variables), network);
				}
			}
			if (isStopped()) {
				return false;
			}
			if (!done) {
				// Add a specific case for the word or meaning.
				pathWriter.write(currentWord.getDataValue());
				pathWriter.write("_");
				Vertex newCase = network.createInstance(Primitive.CASE);
				newCase.setName("c" + newCase.getId() + "_" + Utils.compress(currentWord.getDataValue(), MAX_IDENTIFIER));
				// The index decides if new case should be added to the beginning, or end.
				// TODO: variables should be ordered by number of relationships
				if (meaning != null) {
					Vertex variable = network.createInstance(Primitive.VARIABLE);
					variable.setName("v" + variable.getId() + "_" + Utils.compress(currentWord.getDataValue(), MAX_IDENTIFIER));
					variables.put(currentWord, variable);
					variable.addRelationship(Primitive.MEANING, meaning);
					newCase.addRelationship(Primitive.CASE, variable);
				} else {
					newCase.addRelationship(Primitive.CASE, currentWord);					
				}
				Vertex newState = network.createInstance(Primitive.STATE);
				newState.setName("s" + newState.getId() + "_" + Utils.compress(pathWriter.toString(), MAX_IDENTIFIER));
				newCase.addRelationship(Primitive.GOTO, newState);
				currentState.addRelationship(Primitive.DO, newCase, 0);
				log("Adding new case", Level.FINER, currentWord, newCase, newState);
				processState(
						newState, words, index + 1, pathWriter.toString(), path, sentence,
						questionInput, responseInput, new HashMap<Vertex, Vertex>(variables), network);
			}
			
		}
		return anyQuotientMatch;
	}
	
	/**
	 * Add the sentence from the previous input to the relationships response meta info.
	 */
	public void addSentencePreviousMeta(Relationship relationship, Vertex questionInput, Vertex previousQuestionInput,
				Map<Vertex, Vertex> variables, Network network) {
		// Associate previous question as meta info.
		if (previousQuestionInput != null) {
			Vertex previousQuestion = previousQuestionInput.getRelationship(Primitive.INPUT);
			if (previousQuestion != null) {
				Vertex meta = network.createMeta(relationship);
				Vertex newQuotient = createFormula(questionInput, previousQuestionInput, variables, network);
				if (newQuotient == null) {
					newQuotient = previousQuestion;
				}
				meta.addRelationship(Primitive.PREVIOUS, newQuotient);
			}
		}
	}
	
	/**
	 * Check if any of the existing quotients match, if none do, then add a new quotient.
	 */
	public boolean checkQuotient(
			Vertex currentState, Vertex sentence, Vertex questionInput, Vertex responseInput,
			PathType path, Map<Vertex, Vertex> variables, Network network) {
		// Check if any existing quotients match.
		Collection<Relationship> quotients = currentState.getRelationships(Primitive.QUOTIENT);
		Vertex previousQuestionInput = questionInput.getRelationship(Primitive.QUESTION);
		if (quotients != null) {
			for (Relationship quotient : quotients) {
				if (isStopped()) {
					return false;
				}
				Vertex quotientSentence = quotient.getTarget().getRelationship(Primitive.SENTENCE);
				if ((quotientSentence != null) && (quotientSentence == sentence)) {
					log("Existing quotient", Level.FINER, currentState, quotientSentence);
					// Increase correctness.
					Relationship relationship = currentState.addRelationship(Primitive.QUOTIENT, quotient.getTarget());
					// Associate previous question as meta info.
					addSentencePreviousMeta(relationship, questionInput, previousQuestionInput, variables, network);
					return true;
				}
			}
		}
		if (isStopped()) {
			return false;
		}
		Vertex newQuotient = createFormula(questionInput, responseInput, variables, network);
		if (newQuotient == null) {
			newQuotient = sentence;
		} else {
			// Check if any existing formulas match the new one (by name).
			Relationship quotient = currentState.getRelationship(Primitive.QUOTIENT, newQuotient);
			if (quotient != null) {
				log("Existing quotient formula", Level.FINER, currentState, quotient.getTarget());
				// Increase correctness.
				Relationship relationship = currentState.addRelationship(Primitive.QUOTIENT, quotient.getTarget());
				addSentencePreviousMeta(relationship, questionInput, previousQuestionInput, variables, network);
				return true;
			}
		}
		if (isStopped()) {
			return false;
		}
		// Only add variable quotients as possible until they are confirmed without multiple distinct matches.
		if ((path == PathType.Verb) || (path == PathType.Classification)) {
			Vertex question = questionInput.getRelationship(Primitive.INPUT);
			Relationship quotient = currentState.getRelationship(Primitive.POSSIBLE_QUOTIENT, newQuotient);
			if (quotient != null) {
				if (!quotient.getMeta().hasRelationship(Primitive.SENTENCE, question)) {
					// Must be a new sentence with same quotient, generalize it.
					currentState.addRelationship(Primitive.QUOTIENT, newQuotient);
					currentState.internalRemoveRelationship(quotient);
					log("Adding new generic quotient", Bot.FINE, currentState, newQuotient, question);
					return false;
				} else {
					log("Generic quotient already defined for sentence", Level.FINER, currentState, newQuotient, question);
				}
			} else {
				Relationship relationship = currentState.addRelationship(Primitive.POSSIBLE_QUOTIENT, newQuotient);
				Vertex meta = network.createMeta(relationship);
				meta.addRelationship(Primitive.SENTENCE, question);
				log("Adding new possible quotient", Level.FINE, currentState, newQuotient);
			}
			return false;
		}
		Relationship relationship = currentState.addRelationship(Primitive.QUOTIENT, newQuotient);
		addSentencePreviousMeta(relationship, questionInput, previousQuestionInput, variables, network);
		log("Adding new quotient", Level.FINE, currentState, newQuotient);
		return false;
	}

	/**
	 * Attempt to create a formula response from the question and response.
	 */
	public Vertex createFormula(Vertex questionInput, Vertex responseInput, Map<Vertex, Vertex> variables, Network network) {
		Vertex response = responseInput.getRelationship(Primitive.INPUT);
		if (response == null || (!response.instanceOf(Primitive.SENTENCE)) || !response.hasRelationship(Primitive.WORD)) {
			return null;
		}
		log("Checking formula", Level.FINER, questionInput, responseInput);
		boolean isFormula = false;
		List<Vertex> formulaWords = new ArrayList<Vertex>();
		Vertex inputVariable = variables.get(questionInput);
		Vertex speaker = questionInput.getRelationship(Primitive.SPEAKER);
		if (speaker != null) {
			Vertex speakerVariable = inputVariable.getRelationship(Primitive.SPEAKER);
			Collection<Relationship> words = speaker.getRelationships(Primitive.WORD);
			if ((words != null) && (speakerVariable != null)) {
				for (Relationship word : words) {
					variables.put(word.getTarget(), speakerVariable);
				}
			}
		}
		Vertex target = questionInput.getRelationship(Primitive.TARGET);
		if (target != null) {
			Vertex targetVariable = inputVariable.getRelationship(Primitive.TARGET);
			Collection<Relationship> words = target.getRelationships(Primitive.WORD);
			if ((words != null) && (targetVariable != null)) {
				for (Relationship word : words) {
					variables.put(word.getTarget(), targetVariable);					
				}
			}
		}
		// Index any word variables meanings so they are processed.		
		for (Map.Entry<Vertex, Vertex> entry : new HashMap<Vertex, Vertex>(variables).entrySet()) {
			Vertex word = entry.getKey();
			Vertex variable = entry.getValue();
			if (word.instanceOf(Primitive.WORD) && variable.instanceOf(Primitive.VARIABLE)) {
				Vertex meaning = variable.mostConscious(Primitive.MEANING);
				if ((meaning != null) && !meaning.instanceOf(Primitive.VARIABLE)) {
					variables.put(meaning, meaning);
				}
			}
		}
		// Check each word in the sentence.
		for (Vertex word : response.orderedRelations(Primitive.WORD)) {
			// Check if any variables match the word.
			Vertex variable = variables.get(word);
			if (variable != null) {
				log("Formula defined", Level.FINER, questionInput, word, variable);
				isFormula = true;
				formulaWords.add(variable);
			} else {
				// Next check if any variable value has a relationship based on any other variable value.
				// TODO variables should be ordered, and only use each once.
				boolean match = false;
				for (Map.Entry<Vertex, Vertex> entry : variables.entrySet()) {
					Vertex value = entry.getKey();
					variable = entry.getValue();
					if (!value.instanceOf(Primitive.VARIABLE) && !value.instanceOf(Primitive.WORD) && !value.instanceOf(Primitive.SENTENCE)) {
						// Check any of the variable's value's words match the word.
						Collection<Relationship> words = value.getRelationships(Primitive.WORD);
						if (words != null) {
							for (Relationship valueWord : words) {
								if (word == valueWord) {
									log("Formula defined", Level.FINER, questionInput, word, variable);
									match = true;
									isFormula = true;
									formulaWords.add(variable);
									break;
								}
							}
						}
						for (Map.Entry<Vertex, Vertex> typeEntry : variables.entrySet()) {
							Vertex associateType = typeEntry.getKey();
							if (associateType != value
									&& !associateType.instanceOf(Primitive.WORD)
									&& !associateType.instanceOf(Primitive.VARIABLE)
									&& !associateType.instanceOf(Primitive.SENTENCE)) {
								Collection<Relationship> associations = value.getRelationships(associateType);
								if (associations != null) {
									for (Relationship association : associations) {
										if (!association.getTarget().instanceOf(Primitive.SENTENCE)) {
											words = association.getTarget().getRelationships(Primitive.WORD);
											if (words != null) {
												for (Relationship valueWord : words) {
													if (word == valueWord) {
														Vertex equation = network.createInstance(Primitive.EQUATION);
														equation.addRelationship(Primitive.OPERATOR, Primitive.GET);
														equation.addRelationship(Primitive.ARGUMENT, typeEntry.getValue());
														equation.addRelationship(Primitive.ARGUMENT, variable);
														match = true;
														log("Formula defined", Level.FINER, questionInput, word, equation);
														isFormula = true;
														formulaWords.add(equation);
														break;
													}
												}
											}
										}
										if (match) {
											break;
										}
									}
								}
							}
							if (match) {
								break;
							}
						}
					}
				}
				if (!match) {
					// Next check if any variables has any relationships to the word.
					// TODO variables should be ordered, and only use each once.
					for (Map.Entry<Vertex, Vertex> entry : variables.entrySet()) {
						Vertex value = entry.getKey();
						variable = entry.getValue();
						// Don't check words, only meanings.
						if (!value.instanceOf(Primitive.VARIABLE) && !value.instanceOf(Primitive.WORD) && !value.instanceOf(Primitive.SENTENCE)) {
							Iterator<Relationship> relationships = value.allRelationships();
							while (relationships.hasNext()) {
								Relationship relation = relationships.next();
								if (!relation.isInverse() && !relation.getTarget().instanceOf(Primitive.SENTENCE)) {
									Collection<Relationship> words = relation.getTarget().getRelationships(Primitive.WORD);
									if (words != null) {
										for (Relationship valueWord : words) {
											if (word == valueWord.getTarget()) {
												Vertex equation = network.createInstance(Primitive.EQUATION);
												equation.addRelationship(Primitive.OPERATOR, Primitive.GET);
												equation.addRelationship(Primitive.ARGUMENT, relation.getType());
												equation.addRelationship(Primitive.ARGUMENT, variable);
												match = true;
												log("Formula defined", Level.FINER, questionInput, word, equation);
												isFormula = true;
												formulaWords.add(equation);
												break;
											}
										}
									}
								}
								if (match) {
									break;
								}
							}
						}
					}
				}
				if (!match) {
					formulaWords.add(word);
				}
			}
		}
		if (isFormula) {
			Vertex formula = network.createInstance(Primitive.FORMULA);
			formula.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
			formula.addRelationship(Primitive.SENTENCE, response);
			int index = 0;
			for (Vertex word : formulaWords) {
				formula.addRelationship(Primitive.WORD, word, index);
				index++;
			}
			formula = SelfDecompiler.getDecompiler().createUniqueFormula(formula, network);
			return formula;
		}
		log("Not a formula", Level.FINER, questionInput, responseInput);
		return null;
	}
	
}
