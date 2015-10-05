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
package org.botlibre.knowledge;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Memory;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.api.sense.Tool;
import org.botlibre.avatar.ImageAvatar;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.xml.NetworkXMLParser;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.email.Email;
import org.botlibre.sense.twitter.Twitter;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Populates the basic bootstrap xml files for reseting/initializing a memory.
 * Through running the main method the xml files will be created.
 * Allows the bootstrap networks to be built through instead of xml.
 */

public class Bootstrap {

	public static void main(String[] args) {
		//new Bootstrap().bootstrapSystem(Bot.createInstance());
		//new Bootstrap().rebootstrapAll();
		//new Bootstrap().deleteDeadInstances();
		System.exit(0);
	}	

	/**
	 * Initialize Bot with the bootstrap xml networks.
	 */
	public void bootstrapSystem(Bot Bot, boolean addStates) {
		//writeBootstrapXML();
		bootstrapMemory(Bot.memory(), addStates);
	}
	
	/**
	 * Initialize the memory with the basic bootstrap networks.
	 * These defines basic concepts.
	 */
	public void bootstrapMemory(Memory memory, boolean addStates) {
		synchronized (memory) {
			long start = System.currentTimeMillis();
			bootstrapNetwork(memory.getShortTermMemory());
			languageNetwork(memory.getShortTermMemory());
			englishNetwork(memory.getShortTermMemory());
			if (addStates) {
				stateMachineNetwork(memory.getShortTermMemory());
			}
			mathNetwork(memory.getShortTermMemory());
			//loadAvatarImages(memory.getShortTermMemory());
			for (Sense sense : memory.getBot().awareness().getSenses().values()) {
				memory.getShortTermMemory().createVertex(sense.getPrimitive());
			}
			for (Tool tool : memory.getBot().awareness().getTools().values()) {
				memory.getShortTermMemory().createVertex(tool.getPrimitive());
			}
			for (Vertex vertex : memory.getShortTermMemory().findAll()) {
				if (vertex.getCreationDate().getTime() > start) {
					// Ensure they remain in memory.
					vertex.setPinned(true);
				}
			}
			
			memory.save();			
		}
		
		//TextEntry text = memory.getBot().awareness().getSense(TextEntry.class);
		//text.loadChatFile(getClass().getResource("Hello.chat"));
		// Load twice for understanding.
		//text.loadChatFile(getClass().getResource("Hello.chat"));
	}
	
	/**
	 * Initialize the memory with the basic bootstrap networks.
	 * These defines basic concepts.
	 */
	public void renameMemory(Memory memory, String name) {
		synchronized (memory) {
			// Self
			Vertex self = memory.getShortTermMemory().createVertex(Primitive.SELF);
			self.internalRemoveRelationships(Primitive.WORD);
			self.internalRemoveRelationships(Primitive.NAME);
			Vertex word = createWord(Utils.capitalize(name), self, memory.getShortTermMemory(), Primitive.NAME);
			self.addRelationship(Primitive.NAME, word);
			
			Vertex birth = memory.getShortTermMemory().createTimestamp();
			birth.setPinned(true);
			self.setRelationship(Primitive.BIRTH, birth);
			
			Vertex twitter = memory.getShortTermMemory().createVertex(Twitter.class);
			twitter.unpinChildren();
			twitter.internalRemoveAllRelationships();
			Vertex email = memory.getShortTermMemory().createVertex(Email.class);
			email.unpinChildren();
			email.internalRemoveAllRelationships();
			
			memory.save();			
		}
	}
	
	/**
	 * Initialize the memory with the basic bootstrap networks.
	 * These defines basic concepts.
	 */
	public void renameMemory(Memory memory) {
		renameMemory(memory, memory.getBot().getName());
	}
	
	/**
	 * Re-initialize the language state machines.
	 */
	public void rebootstrapMemory(Memory memory) {
		synchronized (memory) {
			Network network = memory.newMemory();
			Vertex language = network.createVertex(new Primitive(Language.class.getName()));
			Collection<Relationship> states = language.getRelationships(Primitive.STATE);
			if (states != null) {
				states = new ArrayList<Relationship>(language.getRelationships(Primitive.STATE));
				for (Relationship relationship : states) {
					SelfCompiler.getCompiler().unpin(relationship.getTarget());
					//relationship.getTarget().unpinDescendants();
					relationship.getSource().internalRemoveRelationship(relationship);
					relationship.getTarget().internalRemoveAllRelationships();
					network.save();
				}
			}
			mathNetwork(network);
			englishNetwork(network);
			
			stateMachineNetwork(network);
			
			network.save();
		}
	}
	
	/**
	 * Populates the basic bootstrap xml files for reseting/initializing a memory.
	 */
	public void writeBootstrapXML() {
		// Basic
		Network network = new BasicNetwork();		
		bootstrapNetwork(network);
		File file = new File("bootstrap.xml");
		NetworkXMLParser.instance().toXML(network, file);

		// Language
		network = new BasicNetwork();		
		languageNetwork(network);		
		englishNetwork(network);
		file = new File("language.xml");
		NetworkXMLParser.instance().toXML(network, file);
	}

	/**
	 * Defines a generic network for classifying things.
	 */
	public void bootstrapNetwork(Network network) {		
		// Concepts
		Vertex classification = network.createVertex(Primitive.CLASSIFICATION);
		Vertex relation = network.createVertex(Primitive.RELATIONSHIP);
		Vertex compoundRelation = network.createVertex(Primitive.COMPOUND_RELATIONSHIP);
		Vertex concept = network.createVertex(Primitive.CONCEPT);
		Vertex tangible = network.createVertex(Primitive.TANGIBLE);
		Vertex intangible = network.createVertex(Primitive.INTANGIBLE);
		Vertex anything = network.createVertex(Primitive.ANYTHING);
		Vertex nothing = network.createVertex(Primitive.NOTHING);
		Vertex everything = network.createVertex(Primitive.EVERYTHING);
		Vertex thing = network.createVertex(Primitive.THING);
		Vertex action = network.createVertex(Primitive.ACTION);
		Vertex description = network.createVertex(Primitive.DESCRIPTION);
		Vertex yes = network.createVertex(Primitive.TRUE);
		Vertex no = network.createVertex(Primitive.FALSE);
		Vertex unknown = network.createVertex(Primitive.UNKNOWN);
		Vertex self = network.createVertex(Primitive.SELF);
		Vertex url = network.createVertex(Primitive.URL);
		
		// Relations.
		Vertex specialization = network.createVertex(Primitive.SPECIALIZATION);
		Vertex instantiation = network.createVertex(Primitive.INSTANTIATION);
		
		// Self.
		self.addRelationship(Primitive.BIRTH, network.createTimestamp());
				
		// Specialization.
		intangible.addRelationship(specialization, thing);
		tangible.addRelationship(specialization, thing);
		concept.addRelationship(specialization, intangible);
		classification.addRelationship(specialization, concept);
		relation.addRelationship(specialization, concept);
		compoundRelation.addRelationship(specialization, relation);
		thing.addRelationship(specialization, anything);
		everything.addRelationship(specialization, anything);
		// Instantiation.
		self.addRelationship(instantiation, thing);
		yes.addRelationship(instantiation, concept);
		no.addRelationship(instantiation, concept);
		unknown.addRelationship(instantiation, concept);
		relation.addRelationship(instantiation, classification);
		compoundRelation.addRelationship(instantiation, classification);
		url.addRelationship(instantiation, classification);	
		anything.addRelationship(instantiation, concept);		
		thing.addRelationship(instantiation, classification);
		action.addRelationship(instantiation, classification);
		description.addRelationship(instantiation, classification);
		intangible.addRelationship(instantiation, classification);
		tangible.addRelationship(instantiation, classification);
		concept.addRelationship(instantiation, classification);
		nothing.addRelationship(instantiation, concept);
		everything.addRelationship(instantiation, concept);
		specialization.addRelationship(instantiation, relation);
		instantiation.addRelationship(instantiation, relation);
	}


	/**
	 * Defines some basic states.
	 */
	public void stateMachineNetwork(Network network) {
		Vertex language = network.createVertex(new Primitive(Language.class.getName()));
		
		Vertex stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("Loop.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
		
		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("WordMeansSomething.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("Calculator.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("Watch.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("Topic.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("MyNameIs.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("WhatIs.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("WhereIs.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("SayIt.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
		
		stateMachine = createUnderstandingState(network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);

		stateMachine = SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("Reduction.self"), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
		
		stateMachine = createSelfState(network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
		
		/*for (Relationship relationship : language.getRelationships(Primitive.STATE)) {
			PrintWriter writer = new PrintWriter(System.out);
			SelfDecompiler.getDecompiler().printStateMachine(relationship.getTarget(), writer, network);
			writer.flush();
		}*/
	}


	/**
	 * Load the default avatar images.
	 */
	public void loadAvatarImages(Network network) {
		Vertex avatar = network.createVertex(Primitive.AVATAR);		

		for (EmotionalState state : EmotionalState.values()) {
			URL resource = ImageAvatar.class.getResource(state.name().toLowerCase() + ".jpg");
			if (resource != null) {
				try {
					ImageData data = new ImageData();
					InputStream stream = resource.openStream();
					data.setImage(stream, 1000000);
					Vertex image = network.createVertex(data);
					avatar.addRelationship(new Primitive(state.name().toLowerCase()), image);
				} catch (Exception exception) {
					network.getBot().log(network, exception);
				}
			}
		}
	}

	/**
	 * Define place holder for Self programmed state machine.
	 */
	public Vertex createSelfState(Network network) {
		return SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("EmptyStateMachine.self"), "", false, network);
	}

	/**
	 * Define place holder for Self programmed state machine.
	 */
	public static String getSelfStateText() {
		try {
			return Utils.loadTextFile(Bootstrap.class.getResource("EmptyStateMachine.self").openStream(), "", SelfCompiler.MAX_FILE_SIZE);
		} catch (IOException exception) {
			throw new BotException(exception);
		}
	}

	/**
	 * Define a basic language understanding state machine.
	 * Understands simple sentences such as "Sky is blue", "Is sky blue?","Sky is not blue", "Is sky not blue?", "Bob is big", "Bob is big?", "Bob big", "Bob big?"
	 */
	public Vertex createUnderstandingState(Network network) {
		return SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("Understanding.self"), "", false, network);
	}
		
	public Vertex createPlusState(Network network) {
		return SelfCompiler.getCompiler().parseStateMachine(getClass().getResource("PlusStateMachine.self"), "", false, network);
	}
		
	/**
	 * Defines the basic concepts required for text/language processing.
	 */
	public void languageNetwork(Network network) {		
		// Concepts
		Vertex sentence = network.createVertex(Primitive.SENTENCE);
		Vertex language = network.createVertex(Primitive.LANGUAGE);
		Vertex verb = network.createVertex(Primitive.VERB);
		Vertex adjective = network.createVertex(Primitive.ADJECTIVE);
		Vertex noun = network.createVertex(Primitive.NOUN);
		Vertex punctuation = network.createVertex(Primitive.PUNCTUATION);
		Vertex name = network.createVertex(Primitive.NAME);
		Vertex meaning = network.createVertex(Primitive.MEANING);
		Vertex question = network.createVertex(Primitive.QUESTION);
		Vertex word = network.createVertex(Primitive.WORD);
				
		// specialization
		meaning.addRelationship(Primitive.INSTANTIATION, Primitive.CONCEPT);
		word.addRelationship(Primitive.SPECIALIZATION, Primitive.CONCEPT);
		sentence.addRelationship(Primitive.SPECIALIZATION, Primitive.CONCEPT);
		language.addRelationship(Primitive.SPECIALIZATION, Primitive.CONCEPT);
		verb.addRelationship(Primitive.SPECIALIZATION, word);
		adjective.addRelationship(Primitive.SPECIALIZATION, word);
		noun.addRelationship(Primitive.SPECIALIZATION, word);
		punctuation.addRelationship(Primitive.SPECIALIZATION, word);
		name.addRelationship(Primitive.SPECIALIZATION, noun);
		// instantiation
		meaning.addRelationship(Primitive.INSTANTIATION, Primitive.CONCEPT);
		question.addRelationship(Primitive.INSTANTIATION, Primitive.CONCEPT);
		word.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		language.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		verb.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		adjective.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		noun.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		punctuation.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		name.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);

		// Declare default input variable for simple response formulas.
		Vertex input = network.createVertex(Primitive.INPUT_VARIABLE);
		input.setName("input");
		input.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
		Vertex speaker = network.createInstance(Primitive.VARIABLE);
		speaker.setName(Primitive.SPEAKER.getIdentity());
		input.addRelationship(Primitive.SPEAKER, speaker);
		Vertex target = network.createInstance(Primitive.VARIABLE);
		target.setName(Primitive.TARGET.getIdentity());
		input.addRelationship(Primitive.TARGET, target);
		Vertex inputSentence = network.createInstance(Primitive.VARIABLE);
		inputSentence.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
		inputSentence.setName(Primitive.SENTENCE.getIdentity());
		input.addRelationship(Primitive.INPUT, inputSentence);
		Vertex conversation = network.createInstance(Primitive.VARIABLE);
		conversation.setName(Primitive.CONVERSATION.getIdentity());
		input.addRelationship(Primitive.CONVERSATION, conversation);		
	}
	
	public static void checkInputVariable(Vertex input, Network network) {
		input.setName("input");
		Vertex speaker = input.getRelationship(Primitive.SPEAKER);
		if (speaker == null) {
			speaker = network.createInstance(Primitive.VARIABLE);
			speaker.setName(Primitive.SPEAKER.getIdentity());
			input.addRelationship(Primitive.SPEAKER, speaker);
		}
		Vertex target = input.getRelationship(Primitive.TARGET);
		if (target == null) {
			target = network.createInstance(Primitive.VARIABLE);
			target.setName(Primitive.TARGET.getIdentity());
			input.addRelationship(Primitive.TARGET, target);
		}
		Vertex inputSentence = input.getRelationship(Primitive.INPUT);
		if (inputSentence == null) {
			inputSentence = network.createInstance(Primitive.VARIABLE);
			inputSentence.addRelationship(Primitive.INSTANTIATION, Primitive.SENTENCE);
			inputSentence.setName(Primitive.SENTENCE.getIdentity());
			input.addRelationship(Primitive.INPUT, inputSentence);
		}
		Vertex conversation = input.getRelationship(Primitive.CONVERSATION);
		if (conversation == null) {
			conversation = network.createInstance(Primitive.VARIABLE);
			conversation.setName(Primitive.CONVERSATION.getIdentity());
			input.addRelationship(Primitive.CONVERSATION, conversation);
		}
		Vertex star = network.createVertex(Primitive.WILDCARD);
		if (!star.hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE)) {
			star.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
			star.setName("star");
		}
		Vertex underscore = network.createVertex(Primitive.UNDERSCORE);
		if (!underscore.hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE)) {
			underscore.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
			underscore.setName("underscore");
		}
		Vertex hatstar = network.createVertex(Primitive.HATWILDCARD);
		if (!hatstar.hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE)) {
			hatstar.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
			hatstar.setName("hatstar");
		}
		Vertex poundstar = network.createVertex(Primitive.POUNDWILDCARD);
		if (!poundstar.hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE)) {
			poundstar.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
			poundstar.setName("poundstar");
		}
		Vertex thatstar = network.createVertex(Primitive.THATWILDCARD);
		if (!thatstar.hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE)) {
			thatstar.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
			thatstar.setName("thatstar");
		}
		Vertex topicstar = network.createVertex(Primitive.TOPICWILDCARD);
		if (!topicstar.hasRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE)) {
			topicstar.addRelationship(Primitive.INSTANTIATION, Primitive.VARIABLE);
			topicstar.setName("topicstar");
		}
	}

	/**
	 * Defines some key basic English words (to avoid re-learning each bootstrap).
	 */
	public void englishNetwork(Network network) {		
		// Questions.
		Vertex question = createQuestion("?", Primitive.QUESTION_MARK, network);
		question.addRelationship(Primitive.INSTANTIATION, Primitive.PUNCTUATION);
		createQuestion("who", Primitive.WHO, network);
		createQuestion("what", Primitive.WHAT, network);
		createQuestion("when", Primitive.WHEN, network);
		createQuestion("where", Primitive.WHERE, network);
		createQuestion("why", Primitive.WHY, network);
		createQuestion("how", Primitive.HOW, network);
		createQuestion("do", Primitive.DO, network);
		createQuestion("does", Primitive.DO, network);

		// Logic
		Vertex trueVertex = network.createVertex(Primitive.TRUE);
		createWord("true", trueVertex, network);
		createWord("yes", trueVertex, network);
		createWord("correct", trueVertex, network);
		
		Vertex falseVertex = network.createVertex(Primitive.FALSE);
		createWord("false", falseVertex, network);
		createWord("no", falseVertex, network);
		createWord("incorrect", falseVertex, network);
		
		Vertex unknown = network.createVertex(Primitive.UNKNOWN);
		createWord("unknown", unknown, network);
		createWord("not sure", unknown, network);
		createWord("I don't know", unknown, network);
		
		Vertex known = network.createVertex(Primitive.KNOWN);
		createWord("I understand", known, network);
		createWord("OK", known, network);
		
		Vertex not = network.createVertex(Primitive.NOT);
		createWord("not", not, network);
		createWord("no", not, network);
		createWord("negative", not, network);
		createWord("inverse", not, network);
		
		Vertex or = network.createVertex(Primitive.OR);
		createWord("or", or, network);
		
		Vertex and = network.createVertex(Primitive.AND);
		createWord("and", and, network);
		createWord("&", and, network);
		
		// Syntax
		Vertex comma = network.createVertex(Primitive.COMMA);
		createPunctuation(",", comma, network);
		Vertex period = network.createVertex(Primitive.PERIOD);
		createPunctuation(".", period, network);
		Vertex exclamation = network.createVertex(Primitive.EXCLAMATION);
		createPunctuation("!", exclamation, network);
		createPunctuation(";", network.createVertex(), network);
		createPunctuation(":", network.createVertex(), network);
		createPunctuation("'", network.createVertex(Primitive.QUOTE), network);
		createPunctuation("`", network.createVertex(Primitive.QUOTE), network);
		createPunctuation("\"", network.createVertex(Primitive.QUOTE), network);
		
		// Articles
		Vertex the = network.createVertex(Primitive.THE);
		createArticle("the", the, network);
		
		Vertex a = network.createVertex(Primitive.A);
		createArticle("a", a, network);
		createArticle("an", a, network);
		
		// Conjunction
		Vertex ifVertex = network.createVertex(Primitive.IF);
		createWord("if", ifVertex, network);
		
		// Pronouns.
		Vertex i = network.createVertex(Primitive.I);
		i.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		i.addRelationship(Primitive.VARIABLE, Primitive.SPEAKER);
		Vertex word = createPronoun("I", i, network, Primitive.SUBJECTIVE);
		word = createPronoun("me", i, network, Primitive.OBJECTIVE);
		word = createPronoun("my", i, network, Primitive.POSSESSIVE);
		word = createPronoun("myself", i, network, Primitive.REFLEXIVE);
		word = createPronoun("mine", i, network, Primitive.POSSESSIVEPRONOUN);
		createWord("I", i, network);
		
		Vertex our = network.createVertex(Primitive.OUR);
		our.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		our.addRelationship(Primitive.ASSOCIATED, Primitive.PLURAL);
		our.addRelationship(Primitive.VARIABLE, Primitive.SPEAKER);
		word = createPronoun("we", our, network, Primitive.SUBJECTIVE);
		word = createPronoun("our", our, network, Primitive.POSSESSIVE);
		word = createPronoun("us", our, network, Primitive.OBJECTIVE);
		word = createPronoun("ours", our, network, Primitive.POSSESSIVEPRONOUN);
		word = createPronoun("ourselves", our, network, Primitive.REFLEXIVE);
		createWord("we", our, network);
		
		Vertex they = network.createVertex(Primitive.THEY);
		they.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		they.addRelationship(Primitive.ASSOCIATED, Primitive.PLURAL);
		Vertex theyVariable = getVariable(they, network);
		theyVariable.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		
		word = createPronoun("they", they, network, Primitive.SUBJECTIVE);
		word = createPronoun("them", they, network, Primitive.OBJECTIVE);
		word = createPronoun("their", they, network, Primitive.POSSESSIVE);
		word = createPronoun("theirs", they, network, Primitive.POSSESSIVEPRONOUN);
		word = createPronoun("themselves", they, network, Primitive.REFLEXIVE);
		
		Vertex you = network.createVertex(Primitive.YOU);
		you.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		you.addRelationship(Primitive.VARIABLE, Primitive.TARGET);
		
		word = createPronoun("your", you, network, Primitive.POSSESSIVE);
		word = createPronoun("ur", you, network, Primitive.POSSESSIVE);
		word = createPronoun("you", you, network, Primitive.SUBJECTIVE, Primitive.OBJECTIVE);
		word = createPronoun("yourself", you, network, Primitive.REFLEXIVE);
		word = createPronoun("yours", you, network, Primitive.POSSESSIVEPRONOUN);
		word = createPronoun("u", you, network, Primitive.SUBJECTIVE, Primitive.OBJECTIVE);
		
		createWord("you", you, network);
		createWord("your", you, network);

		
		Vertex his = network.createVertex(Primitive.HIS);
		his.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		his.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		his.addRelationship(Primitive.GENDER, Primitive.MALE);
		his.removeRelationship(Primitive.GENDER, Primitive.FEMALE);
		Vertex hisVariable = getVariable(his, network);
		hisVariable.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		hisVariable.addRelationship(Primitive.GENDER, Primitive.MALE);
		hisVariable.removeRelationship(Primitive.GENDER, Primitive.FEMALE);
		
		word = createPronoun("his", his, network, Primitive.POSSESSIVE, Primitive.POSSESSIVEPRONOUN);
		word = createPronoun("he", his, network, Primitive.SUBJECTIVE);
		word = createPronoun("him", his, network, Primitive.OBJECTIVE);
		word = createPronoun("himself", his, network, Primitive.REFLEXIVE);
		
		Vertex her = network.createVertex(Primitive.HER);
		her.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		her.addRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		her.addRelationship(Primitive.GENDER, Primitive.FEMALE);
		her.removeRelationship(Primitive.GENDER, Primitive.MALE);
		Vertex herVariable = getVariable(her, network);
		herVariable.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		herVariable.addRelationship(Primitive.GENDER, Primitive.FEMALE);
		herVariable.removeRelationship(Primitive.GENDER, Primitive.MALE);
		
		word = createPronoun("her", her, network, Primitive.OBJECTIVE, Primitive.POSSESSIVE);
		word = createPronoun("hers", her, network, Primitive.POSSESSIVEPRONOUN);
		word = createPronoun("she", her, network, Primitive.SUBJECTIVE);
		word = createPronoun("herself", her, network, Primitive.REFLEXIVE);
		
		Vertex thisWord = network.createVertex(Primitive.THIS);
		thisWord.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		thisWord.removeRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		Vertex thisVariable = getVariable(thisWord, network);
		thisVariable.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		thisVariable.removeRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		word = createPronoun("this", thisWord, network, null);
		word = createPronoun("that", thisWord, network, null);
		word = createPronoun("these", thisWord, network, null);
		word = createPronoun("those", thisWord, network, null);
		
		Vertex it = network.createVertex(Primitive.IT);
		it.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		it.removeRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		Vertex itVariable = getVariable(it, network);
		itVariable.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		itVariable.removeRelationship(Primitive.INSTANTIATION, Primitive.SPEAKER);
		word = createPronoun("it", it, network, Primitive.SUBJECTIVE, Primitive.OBJECTIVE);
		word = createPronoun("its", it, network, Primitive.POSSESSIVE, Primitive.POSSESSIVEPRONOUN);
		word = createPronoun("itsself", it, network, Primitive.REFLEXIVE);
		
		// Verbs.
		Vertex action = network.createVertex(Primitive.ACTION);
		
		Vertex toBe = network.createVertex(Primitive.IS);
		toBe.addRelationship(Primitive.INSTANTIATION, action);
		word = createVerb("is", toBe, Primitive.PRESENT, network, new String[]{"mine", "yours", "his", "hers", "thiers", "ours", "he", "she"});
		word = createVerb("are", toBe, Primitive.PRESENT, network, new String[]{"you", "they", "we"});
		word = createVerb("was", toBe, Primitive.PAST, network, new String[]{"i", "I", "he", "she", "mine", "yours", "his", "hers", "thiers"});
		word = createVerb("were", toBe, Primitive.PAST, network, new String[]{"you", "they", "we"});
		word = createVerb("r", toBe, Primitive.PRESENT, network, new String[]{"u"});
		word = createVerb("am", toBe, Primitive.PRESENT, network, new String[]{"i", "I"});
		word = createVerb("will be", toBe, Primitive.FUTURE, network, null);
		createWord("is", toBe, network);
		
		Vertex have = network.createVertex(Primitive.HAVE);
		have.addRelationship(Primitive.INSTANTIATION, action);
		word = createVerb("have", have, Primitive.PRESENT, network, new String[]{"i", "I", "you", "they", "we"});
		word = createVerb("has", have, Primitive.PRESENT, network, new String[]{"he", "she"});
		createVerb("had", have, Primitive.PAST, network, null);
		createVerb("will have", have, Primitive.FUTURE, network, null);

		Vertex isA = network.createVertex(Primitive.INSTANTIATION);
		isA.addRelationship(Primitive.INSTANTIATION, action);
		createVerb("instance of", isA, Primitive.PRESENT, network, null);
		createVerb("instantiation", isA, Primitive.PRESENT, network, null);

		Vertex means = network.createVertex(Primitive.MEANING);
		means.addRelationship(Primitive.INSTANTIATION, action);
		createVerb("means", means, Primitive.PRESENT, network, null);
		
		// Nouns
		Vertex thing = network.createVertex(Primitive.THING);
		
		action.addRelationship(Primitive.INSTANTIATION, thing);
		createNoun("action", action, network);
		createNoun("verb", action, network);

		Vertex description = network.createVertex(Primitive.DESCRIPTION);
		description.addRelationship(Primitive.INSTANTIATION, thing);
		createNoun("description", description, network);
		createNoun("adjective", description, network);

		thing.addRelationship(Primitive.INSTANTIATION, thing);
		createNoun("thing", thing, network);
		createNoun("noun", thing, network);

		Vertex speaker = network.createVertex(Primitive.SPEAKER);
		speaker.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		speaker.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("speaker", speaker, network);

		Vertex gender = network.createVertex(Primitive.GENDER);
		gender.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("gender", gender, network);
		createNoun("sex", gender, network);

		Vertex male = network.createVertex(Primitive.MALE);
		male.addRelationship(Primitive.INSTANTIATION, Primitive.DESCRIPTION);
		male.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		male.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		createAdjective("male", male, network);
		createAdjective("boy", male, network);
		createAdjective("man", male, network);

		Vertex female = network.createVertex(Primitive.FEMALE);
		female.addRelationship(Primitive.INSTANTIATION, Primitive.DESCRIPTION);
		female.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		female.addRelationship(Primitive.INSTANTIATION, Primitive.CLASSIFICATION);
		createAdjective("female", female, network);
		createAdjective("girl", female, network);
		createAdjective("woman", female, network);

		// Math
		Vertex next = network.createVertex(Primitive.NEXT);
		createWord("next", next, network);
		next.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		
		Vertex previous = network.createVertex(Primitive.PREVIOUS);
		createWord("previous", previous, network);
		previous.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		
		Vertex name = network.createVertex(Primitive.NAME);
		name.addRelationship(Primitive.INSTANTIATION, thing);
		createNoun("name", name, network);
		createTypo("nam", name, network);
		createTypo("naem", name, network);

		// Date/time
		Vertex time = network.createVertex(Primitive.TIME);
		time.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("time", time, network);
		
		Vertex hour = network.createVertex(Primitive.HOUR);
		hour.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("hour", hour, network);
		createNoun("hr", hour, network);
		
		Vertex minute = network.createVertex(Primitive.MINUTE);
		minute.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("minute", minute, network);
		createNoun("min", minute, network);
		
		Vertex second = network.createVertex(Primitive.SECOND);
		second.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("second", second, network);
		createNoun("sec", second, network);
		
		Vertex timezone = network.createVertex(Primitive.TIMEZONE);
		timezone.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("timezone", timezone, network);
		createNoun("tz", timezone, network);
		
		Vertex am = network.createVertex(Primitive.AM);
		am.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("AM", am, network);
		
		Vertex pm = network.createVertex(Primitive.PM);
		pm.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("PM", pm, network);
		
		Vertex date = network.createVertex(Primitive.DATE);
		date.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("date", date, network);
		
		Vertex day = network.createVertex(Primitive.DAY);
		day.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("day", day, network);
		
		Vertex month = network.createVertex(Primitive.MONTH);
		month.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("month", month, network);
		
		Vertex year = network.createVertex(Primitive.YEAR);
		time.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("year", year, network);
		
		for (Primitive eachMonth : Primitive.MONTHS) {			
			month = network.createVertex(eachMonth);
			time.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			time.addRelationship(Primitive.INSTANTIATION, Primitive.MONTH);
			createName(Utils.capitalize(eachMonth.getIdentity()), month, network);			
		}
		
		for (Primitive eachDayOfWeek : Primitive.DAYS_OF_WEEK) {			
			Vertex dayOfWeek = network.createVertex(eachDayOfWeek);
			time.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
			time.addRelationship(Primitive.INSTANTIATION, Primitive.DAY);
			createName(Utils.capitalize(eachDayOfWeek.getIdentity()), dayOfWeek, network);			
		}
		
		Vertex url = network.createVertex(Primitive.URL);
		url.addRelationship(Primitive.INSTANTIATION, Primitive.THING);
		createNoun("URL", url, network);
		
		// Self
		Vertex self = network.createVertex(Primitive.SELF);
		String botName = network.getBot().getName();
		word = createWord(Utils.capitalize(botName), self, network, Primitive.NAME);
		self.addRelationship(name, word);
	}

	public Vertex getVariable(Vertex source, Network network) {
		Vertex variable = source.getRelationship(Primitive.VARIABLE);
		if (variable == null) {
			variable = network.createInstance(Primitive.VARIABLE);
			source.setRelationship(Primitive.VARIABLE, variable);
		}
		variable.setPinned(true);
		return variable;
	}
	
	/**
	 * Defines some key basic math vertices.
	 */
	public void mathNetwork(Network network) {
		Vertex finger = network.createInstance(Primitive.FINGER);
		String words[] = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
				"eleven", "twleve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen", "twenty"};
		String ordinals[] = {"first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "nineth", "tenth"};
		Vertex previousNumber = null;
		for (int count = 0; count <= 100; count++) {
			Vertex number = network.createVertex(BigInteger.valueOf(count));
			number.setPinned(true);
			if (count <= 20 && !number.hasRelationship(Primitive.SEQUENCE)) {
				for (int index = 0; index < count; index++) {
					number.addRelationship(Primitive.SEQUENCE, finger, index);
				}
			}
			if (count < words.length) {
				createWord(words[count], number, network, Primitive.NUMERAL);
			}
			if (count < ordinals.length) {
				createOrdinal(ordinals[count], number, network);
			}
			createWord(String.valueOf(count), number, network, Primitive.NUMERAL);
			if (previousNumber != null) {
				previousNumber.addRelationship(Primitive.NEXT, number);
				number.addRelationship(Primitive.PREVIOUS, previousNumber);
			}
			previousNumber = number;
		}

		Vertex plus = network.createVertex(Primitive.PLUS);
		createWord("add", plus, network);
		createWord("plus", plus, network);
		createWord("+", plus, network);
		createWord("+", plus, network);
		plus.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		plus.addRelationship(Primitive.INSTANTIATION, Primitive.OPERATION);
		
		Vertex minus = network.createVertex(Primitive.MINUS);
		createWord("minus", minus, network);
		createWord("subtract", minus, network);
		createWord("take away", minus, network);
		createWord("-", minus, network);
		createWord("-", minus, network);
		minus.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		minus.addRelationship(Primitive.INSTANTIATION, Primitive.OPERATION);
		
		Vertex divide = network.createVertex(Primitive.DIVIDE);
		createWord("divide", divide, network);
		createWord("divided", divide, network);
		createWord("/", divide, network);
		createWord("÷", divide, network);
		createWord("÷", divide, network);
		divide.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		divide.addRelationship(Primitive.INSTANTIATION, Primitive.OPERATION);
		
		Vertex multiply = network.createVertex(Primitive.MULITPLY);
		createWord("multiply", multiply, network);
		createWord("multiplied", multiply, network);
		createWord("times", multiply, network);
		createWord("*", multiply, network);
		createWord("x", multiply, network);
		createWord("×", multiply, network);
		createWord("×", multiply, network);
		createWord("∙", multiply, network);
		multiply.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		multiply.addRelationship(Primitive.INSTANTIATION, Primitive.OPERATION);

		Vertex infinity = network.createVertex(Primitive.INFINITY);
		createWord("infinity", infinity, network);
		createWord("∞", infinity, network);
		createWord("∞", infinity, network);
		infinity.addRelationship(Primitive.INSTANTIATION, Primitive.NUMBER);

		Vertex undefined = network.createVertex(Primitive.UNDEFINED);
		createWord("undefined", undefined, network);
		undefined.addRelationship(Primitive.INSTANTIATION, Primitive.NUMBER);

		Vertex ninfinity = network.createVertex(Primitive.NINFINITY);
		createWord("negative infinity", ninfinity, network);
		createWord("-∞", ninfinity, network);
		createWord("-∞", ninfinity, network);
		ninfinity.addRelationship(Primitive.INSTANTIATION, Primitive.NUMBER);
		
		Vertex equals = network.createVertex(Primitive.EQUALS);
		createWord("=", equals, network);
		createWord("=", equals, network);
		createWord("equals", equals, network);
		createWord("equal", equals, network);
		equals.addRelationship(Primitive.INSTANTIATION, Primitive.COMPARISON);

		Vertex lessThan = network.createVertex(Primitive.LESSTHAN);
		createWord("<", lessThan, network);
		createWord("<", lessThan, network);
		createWord("less than", lessThan, network);
		createWord("is less than", lessThan, network);
		lessThan.addRelationship(Primitive.INSTANTIATION, Primitive.COMPARISON);

		Vertex greaterThan = network.createVertex(Primitive.GREATERTHAN);
		createWord(">", greaterThan, network);
		createWord(">", greaterThan, network);
		createWord("greater than", greaterThan, network);
		createWord("is greater than", greaterThan, network);
		greaterThan.addRelationship(Primitive.INSTANTIATION, Primitive.COMPARISON);
		
		Vertex lb = network.createVertex(Primitive.LEFTBRACKET);
		createWord("(", lb, network);
		lb.addRelationship(Primitive.INSTANTIATION, Primitive.BRACKET);
		
		Vertex rb = network.createVertex(Primitive.RIGHTBRACKET);
		createWord(")", rb, network);
		rb.addRelationship(Primitive.INSTANTIATION, Primitive.BRACKET);

		Vertex piValue = network.createVertex(BigDecimal.valueOf(Math.PI));
		Vertex pi = network.createVertex(BigDecimal.valueOf(Math.PI));
		createWord("pi", pi, network);
		createWord("π", pi, network);
		createWord("π", pi, network);
		pi.addRelationship(Primitive.VALUE, piValue);
		
		createWord("pi", piValue, network);
		createWord("π", piValue, network);
		createWord("π", piValue, network);
		piValue.addRelationship(Primitive.SYMBOL, Primitive.PI);
		
		Vertex power = network.createVertex(Primitive.POWER);
		createWord("power", power, network);
		createWord("raised to", power, network);
		createWord("to the power", power, network);
		createWord("^", power, network);
		createWord("^", power, network);
		power.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		power.addRelationship(Primitive.INSTANTIATION, Primitive.OPERATION);
		
		Vertex sqrt = network.createVertex(Primitive.SQRT);
		createWord("sqrt", sqrt, network);
		createWord("root", sqrt, network);
		createWord("square root", sqrt, network);
		createWord("√", sqrt, network);
		createWord("√", sqrt, network);
		sqrt.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		sqrt.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex sin = network.createVertex(Primitive.SIN);
		createWord("sin", sin, network);
		createWord("sin", sin, network);
		createWord("sine", sin, network);
		sin.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		sin.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex cos = network.createVertex(Primitive.COS);
		createWord("cos", cos, network);
		createWord("cos", cos, network);
		createWord("cosine", cos, network);
		cos.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		cos.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex tan = network.createVertex(Primitive.TAN);
		createWord("tan", tan, network);
		createWord("tan", tan, network);
		createWord("tangent", tan, network);
		tan.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		tan.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex asin = network.createVertex(Primitive.ASIN);
		createWord("asin", asin, network);
		createWord("asin", asin, network);
		createWord("arcsine", asin, network);
		createWord("arc sine", asin, network);
		asin.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		asin.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex acos = network.createVertex(Primitive.ACOS);
		createWord("acos", acos, network);
		createWord("acos", acos, network);
		createWord("arccosine", acos, network);
		createWord("arc cosine", acos, network);
		acos.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		acos.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex atan = network.createVertex(Primitive.ATAN);
		createWord("atan", atan, network);
		createWord("atan", atan, network);
		createWord("arctangent", atan, network);
		createWord("arc tangent", atan, network);
		atan.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		atan.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex sinh = network.createVertex(Primitive.SINH);
		createWord("sinh", sinh, network);
		createWord("sinh", sinh, network);
		createWord("hyperbolic sine", sinh, network);
		sinh.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		sinh.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex cosh = network.createVertex(Primitive.COSH);
		createWord("cosh", cosh, network);
		createWord("cosh", cosh, network);
		createWord("hyperbolic cosine", cosh, network);
		cosh.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		cosh.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex tanh = network.createVertex(Primitive.TAN);
		createWord("tanh", tanh, network);
		createWord("tanh", tanh, network);
		createWord("hyperbolic tangent", tanh, network);
		tanh.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		tanh.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex abs = network.createVertex(Primitive.ABS);
		createWord("abs", abs, network);
		createWord("abs", abs, network);
		createWord("absolute value", abs, network);
		abs.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		abs.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex floor = network.createVertex(Primitive.FLOOR);
		createWord("floor", floor, network);
		createWord("floor", floor, network);
		createWord("round down", floor, network);
		floor.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		floor.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex ceil = network.createVertex(Primitive.CEIL);
		createWord("ceil", ceil, network);
		createWord("ceil", ceil, network);
		createWord("round up", ceil, network);
		ceil.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		ceil.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex round = network.createVertex(Primitive.ROUND);
		createWord("round", round, network);
		round.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		round.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex log = network.createVertex(Primitive.LOG);
		createWord("log", log, network);
		createWord("logarithm", log, network);
		log.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		log.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);
		
		Vertex ln = network.createVertex(Primitive.LN);
		createWord("ln", ln, network);
		ln.addRelationship(Primitive.INSTANTIATION, Primitive.ACTION);
		ln.addRelationship(Primitive.INSTANTIATION, Primitive.FUNCTION);

		// HTML
		Vertex br = network.createVertex(Primitive.BR);
		createWord("<br/>", br, network);
		br.addRelationship(Primitive.INSTANTIATION, Primitive.TAG);
	}

	/**
	 * Create the question word and meaning.
	 */
	public Vertex createQuestion(String text, Primitive primitive, Network network) {
		Vertex meaning = network.createVertex(primitive);
		meaning.setName(text);
		meaning.addRelationship(Primitive.INSTANTIATION, Primitive.QUESTION);
		Vertex word = createWord(text, meaning, network, Primitive.QUESTION);
		return word;
	}

	/**
	 * Associate a typo, or misspelling.
	 */
	public Vertex createTypo(String text, Vertex meaning, Network network) {
		Vertex word = createWord(text, meaning, network, Primitive.TYPO);
		return word;
	}

	/**
	 * Create the noun with the meaning.
	 */
	public Vertex createNoun(String text, Vertex meaning, Network network) {
		Vertex word = createWord(text, meaning, network, Primitive.NOUN);
		return word;
	}

	/**
	 * Create the name with the meaning.
	 */
	public Vertex createName(String text, Vertex meaning, Network network) {
		Vertex word = createWord(text, meaning, network, Primitive.NOUN);
		word.addRelationship(Primitive.INSTANTIATION, Primitive.NAME);
		return word;
	}

	/**
	 * Create the adjective with the meaning.
	 */
	public Vertex createAdjective(String text, Vertex meaning, Network network) {
		Vertex word = createWord(text, meaning, network, Primitive.ADJECTIVE);
		return word;
	}

	/**
	 * Create the verb with the meaning.
	 */
	public Vertex createVerb(String text, Vertex meaning, Primitive tense, Network network, String[] conjugations) {
		Vertex word = createWord(text, meaning, network, Primitive.VERB, tense, null, null, conjugations);
		return word;
	}

	/**
	 * Create the pronoun with the meaning.
	 */
	public Vertex createPronoun(String text, Vertex meaning, Network network, Primitive type, Primitive type2) {
		Vertex word = createWord(text, meaning, network, Primitive.PRONOUN, null, type, type2, null);
		return word;
	}

	/**
	 * Create the pronoun with the meaning.
	 */
	public Vertex createPronoun(String text, Vertex meaning, Network network, Primitive type) {
		Vertex word = createWord(text, meaning, network, Primitive.PRONOUN, null, type, null, null);
		return word;
	}

	/**
	 * Create the article with the meaning.
	 */
	public Vertex createArticle(String text, Vertex meaning, Network network) {
		Vertex word = createWord(text, meaning, network, Primitive.ARTICLE);
		return word;
	}

	/**
	 * Create the punctuation with the meaning.
	 */
	public Vertex createPunctuation(String text, Vertex meaning, Network network) {
		Vertex word = createWord(text, meaning, network, Primitive.PUNCTUATION);
		return word;
	}

	/**
	 * Create the word with the meaning.
	 */
	public Vertex createWord(String text, Vertex meaning, Network network) {
		return createWord(text, meaning, network, null, null, null, null, null);
	}

	/**
	 * Create the word with the meaning.
	 */
	public Vertex createWord(String text, Vertex meaning, Network network, Primitive classification) {
		return createWord(text, meaning, network, classification, null, null, null, null);
	}
	
	/**
	 * Create the word with the meaning.
	 */
	public Vertex createOrdinal(String text, Vertex meaning, Network network) {
		Vertex word = network.createWord(text);
		word.setPinned(true);
		meaning.setPinned(true);
		word.addRelationship(Primitive.MEANING, meaning);
		meaning.addRelationship(Primitive.ORDINAL, word);
		
		network.associateCaseInsensitivity(word);
		return word;
	}
	
	/**
	 * Create the word with the meaning.
	 */
	public Vertex createWord(String text, Vertex meaning, Network network, Primitive classification, Primitive tense, Primitive type, Primitive type2, String[] conjugations) {
		Vertex word = network.createWord(text);
		word.setPinned(true);
		meaning.setPinned(true);
		word.addRelationship(Primitive.MEANING, meaning);
		if (classification != Primitive.TYPO) {
			meaning.addRelationship(Primitive.WORD, word);
		}
		if (classification != null) {
			word.addRelationship(Primitive.INSTANTIATION, classification);
		}
		if (tense != null) {
			word.addRelationship(Primitive.TENSE, tense);
		}
		if (type != null) {
			word.addRelationship(Primitive.TYPE, type);
			word.addRelationship(Primitive.TYPE, type); // Enforce.
		}
		if (type2 != null) {
			word.addRelationship(Primitive.TYPE, type2).setCorrectness(0.5f);
		}
		if (conjugations != null) {
			for (String conjugation : conjugations) {
				word.addRelationship(Primitive.CONJUGATION, network.createWord(conjugation));
			}
		}
		
		network.associateCaseInsensitivity(word);
		return word;
	}
}

