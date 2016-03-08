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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.BeforeClass;

/**
 * Test the performance of the Freebase batch import.
 */

public class TestWikidata extends TextTest {
	
	public static int SLEEP = 5000;

	@BeforeClass
	public static void setup() {
		bootstrap();
		
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Collection<Relationship> states = new ArrayList<Relationship>(language.getRelationships(Primitive.STATE));
		for (Relationship state : states) {
			if (state.getTarget().getName().equals("WhatIs")) {
				Vertex wikidata = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("WhatIsWikidata.self"), "", false, network);
				language.replaceRelationship(state, wikidata);
				network.removeRelationship(state);
			} else if (state.getTarget().getName().equals("WhereIs")) {
				Vertex wikidata = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("WhereIsWikidata.self"), "", false, network);
				language.replaceRelationship(state, wikidata);
				network.removeRelationship(state);
			}
		}
		network.save();
	}

	/**
	 * Test response mimicry works.
	 */
	@org.junit.Test
	public void testImport() throws Exception {
		Utils.sleep(SLEEP);
		Bot bot = Bot.createInstance();
		//Sense sense = bot.awareness().getSense(Http.class.getName());
		//sense.input(new URL("http://www.freebase.com/physics/particle"));
		Utils.sleep(5000);
		
		bot.shutdown();		
	}

	/**
	 * Test response mimicry works.
	 */
	@org.junit.Test
	public void testWhatIs() {
		Utils.sleep(SLEEP);
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("what is Uranium?");
		String response = waitForOutput(output);
		if (!response.equals("radioactive, metallic element with the atomic number 92")) {
			fail("Incorrect: " + response);			
		}
		
		Utils.sleep(SLEEP);
		text.input("tell me about Oxygen");
		response = waitForOutput(output);
		if (!response.equals("element with the atomic number of 8")) {
			fail("Incorrect: " + response);			
		}

		Utils.sleep(SLEEP);
		text.input("define love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect: " + response);			
		}

		Utils.sleep(SLEEP);
		text.input("who is Barack Obama?");
		response = waitForOutput(output);
		if (response.indexOf("44th President") == -1) {
			fail("Incorrect: " + response);
		}

		Utils.sleep(SLEEP);
		text.input("who is his father?");
		response = waitForOutput(output);
		if (response.indexOf("Barack Obama") == -1) {
			fail("Incorrect: " + response);
		}

		Utils.sleep(SLEEP);
		text.input("who is George Bush?");
		response = waitForOutput(output);
		if (response.indexOf("43rd President") == -1) {
			fail("Incorrect: " + response);
		}

		Utils.sleep(SLEEP);
		text.input("who is Barack Obama?");
		response = waitForOutput(output);
		if (response.indexOf("44th President") == -1) {
			fail("Incorrect: " + response);
		}

		Utils.sleep(SLEEP);
		text.input("who is his mother?");
		response = waitForOutput(output);
		if (response.indexOf("Ann Dunham") == -1) {
			fail("Incorrect: " + response);
		}

		Utils.sleep(SLEEP);
		text.input("where is Montreal?");
		response = waitForOutput(output);
		checkResponse(response, "Montreal is in Canada.");

		Utils.sleep(SLEEP);
		text.input("where is Edmonton?");
		response = waitForOutput(output);
		checkResponse(response, "Edmonton is in Canada.");
		
		// Test twice for when already known.
		Utils.sleep(SLEEP);
		text.input("where is Edmonton?");
		response = waitForOutput(output);
		checkResponse(response, "Edmonton is in Canada.");
		
		text.input("x");
		response = waitForOutput(output);
		
		// Test lower case.
		Utils.sleep(SLEEP);
		text.input("where is edmonton?");
		response = waitForOutput(output);
		checkResponse(response, "Edmonton is in Canada.");

		Utils.sleep(SLEEP);
		text.input("what is Brockville?");
		response = waitForOutput(output);
		checkResponse(response, "city in Ontario, Canada");

		// Test twice for when already known.
		Utils.sleep(SLEEP);
		text.input("what is Brockville?");
		response = waitForOutput(output);
		checkResponse(response, "city in Ontario, Canada");

		text.input("x");
		response = waitForOutput(output);
		
		// Test lower case.
		Utils.sleep(SLEEP);
		text.input("what is brockville?");
		response = waitForOutput(output);
		checkResponse(response, "city in Ontario, Canada");

		Utils.sleep(SLEEP);
		text.input("where is it?");
		response = waitForOutput(output);
		checkResponse(response, "It is in Canada.");

		bot.shutdown();
	}
}

