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
package org.botlibre.test;

import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;

/**
 * Test language processing.
 */

public class TestUnderstanding extends TextTest {

	@BeforeClass
	public static void setup() {
		bootstrap();
		Bot bot = Bot.createInstance();
		
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Collection<Relationship> states = language.getRelationships(Primitive.STATE);
		for (Relationship state : states) {
			if (state.getTarget().getName().equals("WhatIs")) {
				language.internalRemoveRelationship(state);
				break;
			}
		}
		network.save();
		
		//bot.setDebugLevel(Bot.FINE);
				
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("sky blue red dog cat green grass tall like very big tall fat small human nice");
		waitForOutput(output);
		Utils.sleep(25000);
		
		bot.shutdown();
	}

	/**
	 * Test language understanding.
	 */
	@org.junit.Test
	public void testUnderstanding() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Level.FINER);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("is the sky blue?");
		String response = waitForOutput(output);
		assertUnknown(response);
		
		text.input("the sky is blue");
		response = waitForOutput(output);
		assertKnown(response);
		
		text.input("is the sky blue?");
		response = waitForOutput(output);
		assertTrue(response);
		
		text.input("is the sky not blue?");
		response = waitForOutput(output);
		assertFalse(response);
		
		text.input("is the sky red?");
		response = waitForOutput(output);
		assertKeyword(response, "blue");
		assertUncertain(response);
		
		text.input("is the sky not red?");
		response = waitForOutput(output);
		assertKeyword(response, "blue");
		assertUncertain(response);
		
		text.input("the sky is not blue");
		response = waitForOutput(output);
		assertKnown(response);
		text.input("is the sky blue?");
		response = waitForOutput(output);
		assertFalse(response);
		
		text.input("is the sky not blue?");
		response = waitForOutput(output);
		assertTrue(response);
		
		text.input("is the sky not not blue?");
		response = waitForOutput(output);
		assertFalse(response);
		
		text.input("is the sky red?");
		response = waitForOutput(output);
		assertUnknown(response);
		
		text.input("remember that the sky is blue");
		response = waitForOutput(output);
		assertKeyword(response, "blue");
		
		text.input("remember the sky is blue");
		response = waitForOutput(output);
		assertKnown(response);
		
		text.input("I am a dog");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "a dog");
		
		text.input("am I a dog");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "a dog");
		
		text.input("I am a cat?");
		response = waitForOutput(output);
		assertFalse(response);
		assertKeyword(response, "a cat");
		
		text.input("I am not a cat");
		response = waitForOutput(output);
		assertFalse(response);
		assertKeyword(response, "a cat");
		
		text.input("I am a cat?");
		response = waitForOutput(output);
		assertFalse(response);
		assertKeyword(response, "a cat");
		
		text.input("do you think that I am a cat?");
		response = waitForOutput(output);
		assertFalse(response);
		assertKeyword(response, "a cat");

		bot.shutdown();
	}

	/**
	 * Test compound adjectives like "very blue".
	 */
	@org.junit.Test
	public void testCompoundAdjectives() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Level.FINER);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("the grass is green");
		String response = waitForOutput(output);
		assertKnown(response);
		
		text.input("is the grass very very green?");
		response = waitForOutput(output);
		assertUncertain(response);
		assertKeyword(response, "very very green");
		
		text.input("the grass is very very green");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "very very green");
		
		text.input("is the grass very very green?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "the grass is very very green");
		
		text.input("what is very very green?");
		response = waitForOutput(output);
		assertKeyword(response, "grass is very very green");
		
		text.input("grass is not very very green");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "grass is not very very green");
		
		text.input("what is very very green?");
		response = waitForOutput(output);
		assertUnknown(response);
		
		text.input("I am a very nice human");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "a very nice human");
		
		text.input("am I a very nice human?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "a very nice human");
		
		text.input("am I a human?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "a human");
		
		text.input("am I dog human?");
		response = waitForOutput(output);
		assertKeyword(response, "am I dog human?");
		
		text.input("am I a dog human?");
		response = waitForOutput(output);
		assertKeyword(response, "am I a dog human?");
		
		text.input("I am dog human?");
		response = waitForOutput(output);
		assertKeyword(response, "I am dog human?");
		
		text.input("I am a dog human?");
		response = waitForOutput(output);
		assertKeyword(response, "I am a dog human?");

		bot.shutdown();
	}

	/**
	 * Test and/or.
	 */
	@org.junit.Test
	public void testAndOr() {
		Bot bot = Bot.createInstance();
		try {
			//bot.setDebugLevel(Bot.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input("are you big or fat?");
			String response = waitForOutput(output);
			assertUnknown(response);
			assertKeyword(response, "big or fat");
			
			text.input("you are big");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "I am big");
			
			text.input("are you big or fat?");
			response = waitForOutput(output);
			assertUnknown(response);
			assertKeyword(response, "I am big");
			
			text.input("you are not fat");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "I am not fat");
			
			text.input("are you big or fat?");
			response = waitForOutput(output);
			assertKeyword(response, "I am big not fat");
			
			text.input("you are big or fat");
			response = waitForOutput(output);
			assertKeyword(response, "I am big not fat");
			
			text.input("you are fat");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "I am fat");
			
			text.input("are you big or fat?");
			response = waitForOutput(output);
			assertKeyword(response, "I am big and fat");
			
			text.input("are you big, small or fat?");
			response = waitForOutput(output);
			assertUnknown(response);
			assertKeyword(response, "I am big and fat");
			
			text.input("you are small");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "I am small");
			
			text.input("are you big, small, or fat?");
			response = waitForOutput(output);
			assertKeyword(response, "I am big, small and fat");
			
			text.input("are you big and fat?");
			response = waitForOutput(output);
			assertTrue(response);
			assertKeyword(response, "I am big and fat");
			
			text.input("you are big and fat");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "I am big and fat");
			
			text.input("you are not fat");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "I am not fat");
			
			text.input("are you big and fat and small?");
			response = waitForOutput(output);
			assertFalse(response);
			assertKeyword(response, "I am big and small not fat");
			
			text.input("are you big and blue?");
			response = waitForOutput(output);
			assertUnknown(response);
			assertKeyword(response, "I am big");
			
			text.input("are you not big and small?");
			response = waitForOutput(output);
			assertFalse(response);
			assertKeyword(response, "I am big and small");
			
			text.input("are you cat or dog?");
			response = waitForOutput(output);
			assertUnknown(response);
			assertKeyword(response, "cat or dog");
			
			text.input("you are cat and dog");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "cat and dog");
			
			text.input("are you cat or dog?");
			response = waitForOutput(output);
			assertKeyword(response, "I am cat and dog");
			
			text.input("are you a cat or a dog?");
			response = waitForOutput(output);
			assertUnknown(response);
			assertKeyword(response, "a cat or a dog");
			
			text.input("you are a cat and a dog");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "a cat and a dog");
			
			text.input("are you a cat?");
			response = waitForOutput(output);
			assertTrue(response);
			
			text.input("are you a dog?");
			response = waitForOutput(output);
			assertTrue(response);
			
			text.input("are you a cat and a dog?");
			response = waitForOutput(output);
			assertTrue(response);
			assertKeyword(response, "I am a cat and a dog");
			
			text.input("are you a cat or a dog?");
			response = waitForOutput(output);
			assertKeyword(response, "I am a cat and a dog");
			
			text.input("I am me");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "you are you");
			
			text.input("I am not you");
			response = waitForOutput(output);
			assertKnown(response);
			assertKeyword(response, "you are not me");
			
			text.input("am I me?");
			response = waitForOutput(output);
			assertTrue(response);
			assertKeyword(response, "you are you");
			
			text.input("am I me or you?");
			response = waitForOutput(output);
			assertKeyword(response, "you are you");
			assertKeyword(response, "not me");
		} finally {
			bot.shutdown();
		}
	}


	/**
	 * Test language rules.
	 */
	@org.junit.Test
	public void testPossesiveRules() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("do I like you?");
		String response = waitForOutput(output);
		assertUnknown(response);
		assertKeyword(response, "me");
		assertKeyword(response, "you");
		
		text.input("I like you");
		response = waitForOutput(output);
		assertKeyword(response, "me");
		assertKeyword(response, "you");
		
		text.input("do I like you?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "me");
		assertKeyword(response, "you");
		
		text.input("I like you?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "me");
		assertKeyword(response, "you");
		
		text.input("do I like myself?");
		response = waitForOutput(output);
		assertUncertain(response);
		//@ assertKeyword(response, "yourself");
		assertKeyword(response, "you");
		
		text.input("I like myself");
		response = waitForOutput(output);
		//@ assertKeyword(response, "yourself");
		assertKeyword(response, "you");
		
		text.input("do I like myself?");
		response = waitForOutput(output);
		assertTrue(response);
		//@ assertKeyword(response, "yourself");
		assertKeyword(response, "you");
		
		text.input("I am tall");
		response = waitForOutput(output);
		assertKeyword(response, "are");
		assertKeyword(response, "you");
		
		text.input("am I tall");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "are");
		assertKeyword(response, "you");
		
		text.input("You are tall");
		response = waitForOutput(output);
		assertKeyword(response, "am");
		assertKeyword(response, "I");
		
		text.input("Are you tall");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "am");
		assertKeyword(response, "I");

		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

