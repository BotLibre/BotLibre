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

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.botlibre.Bot;
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test language processing.
 */

public class TestResponseListImport2 extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		//TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("test2.res");
		File file = new File(url.toURI());
		ResponseListParser.parser().loadChatFile(file, "Response List", "", false, true, bot);
		//List<String> output = registerForOutput(text);
		//text.input("this is a very complicated sentence the dog barks all night this is a good reply to that");
		//waitForOutput(output);
		//Utils.sleep(5000);
		bot.shutdown();
	}

	/**
	 * Test greetings.
	 */
	@org.junit.Test
	public void testGreetings() {
		// Test that first default is random.
		Set<String> responses = new HashSet<String>();
		for (int index = 0; index < 10; index++) {
			Bot bot = Bot.createInstance();
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input(null);
			String response = waitForOutput(output);
			responses.add(response);
			
			bot.shutdown();
		}
		if (responses.size() < 5) {
			for (int index = 0; index < 10; index++) {
				Bot bot = Bot.createInstance();
				Language language = bot.mind().getThought(Language.class);
				language.setLearningMode(LearningMode.Disabled);
				TextEntry text = bot.awareness().getSense(TextEntry.class);
				List<String> output = registerForOutput(text);
				
				text.input(null);
				String response = waitForOutput(output);
				responses.add(response);
				
				bot.shutdown();
			}
		}
		if (responses.size() < 5) {
			for (int index = 0; index < 10; index++) {
				Bot bot = Bot.createInstance();
				Language language = bot.mind().getThought(Language.class);
				language.setLearningMode(LearningMode.Disabled);
				TextEntry text = bot.awareness().getSense(TextEntry.class);
				List<String> output = registerForOutput(text);
				
				text.input(null);
				String response = waitForOutput(output);
				responses.add(response);
				
				bot.shutdown();
			}
		}
		if (responses.size() < 5) {
			fail("Greetings are not random: " + responses);
		}
	}

	/**
	 * Test default responses.
	 */
	@org.junit.Test
	public void testDefaults() {
		// Test that first default is random.
		Set<String> responses = new HashSet<String>();
		for (int index = 0; index < 10; index++) {
			Bot bot = Bot.createInstance();
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input("xxx");
			String response = waitForOutput(output);
			responses.add(response);
			
			bot.shutdown();
		}
		if (responses.size() != 5) {
			for (int index = 0; index < 10; index++) {
				Bot bot = Bot.createInstance();
				Language language = bot.mind().getThought(Language.class);
				language.setLearningMode(LearningMode.Disabled);
				TextEntry text = bot.awareness().getSense(TextEntry.class);
				List<String> output = registerForOutput(text);
				
				text.input("xxx");
				String response = waitForOutput(output);
				responses.add(response);
				
				bot.shutdown();
			}
		}
		if (responses.size() != 5) {
			for (int index = 0; index < 10; index++) {
				Bot bot = Bot.createInstance();
				Language language = bot.mind().getThought(Language.class);
				language.setLearningMode(LearningMode.Disabled);
				TextEntry text = bot.awareness().getSense(TextEntry.class);
				List<String> output = registerForOutput(text);
				
				text.input("xxx");
				String response = waitForOutput(output);
				responses.add(response);
				
				bot.shutdown();
			}
		}
		if (responses.size() != 5) {
			fail("First default is not random: " + responses);
		}

		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		// Test that defaults are random.
		responses = new HashSet<String>();
		for (int index = 0; index < 20; index++) {
			
			text.input("xxx" + index);
			String response = waitForOutput(output);
			responses.add(response);
		}
		if (responses.size() != 5) {
			fail("Defaults are not random: " + responses);
		}
		
		text.input("set topic 1");
		String response = waitForOutput(output);
		checkResponse(response, "topic set");
		
		// Test topic defaults are random.
		responses = new HashSet<String>();
		for (int index = 0; index < 20; index++) {
			text.input("xxx" + index);
			response = waitForOutput(output);
			responses.add(response);
			assertKeyword(response, "topic");
		}
		if (responses.size() != 5) {
			fail("Topic defaults are not random: " + responses);
		}
		
		text.input("clear topic");
		response = waitForOutput(output);
		checkResponse(response, "topic cleared");

		// Test previous defaults are random.
		responses = new HashSet<String>();
		for (int index = 0; index < 10; index++) {
			text.input("set previous");
			response = waitForOutput(output);
			checkResponse(response, "previous 1");
			
			text.input("xxx" + index);
			response = waitForOutput(output);
			responses.add(response);
			assertKeyword(response, "previous");
		}
		if (responses.size() != 5) {
			for (int index = 0; index < 10; index++) {
				text.input("set previous");
				response = waitForOutput(output);
				checkResponse(response, "previous 1");
				
				text.input("xxx" + index);
				response = waitForOutput(output);
				responses.add(response);
				assertKeyword(response, "previous");
			}
		}
		if (responses.size() != 5) {
			for (int index = 0; index < 10; index++) {
				text.input("set previous");
				response = waitForOutput(output);
				checkResponse(response, "previous 1");
				
				text.input("xxx" + index);
				response = waitForOutput(output);
				responses.add(response);
				assertKeyword(response, "previous");
			}
		}
		if (responses.size() != 5) {
			fail("Previous defaults are not random: " + responses);
		}
		
		text.input("set condition");
		response = waitForOutput(output);
		checkResponse(response, "condition set");
		
		// Test topic defaults are random.
		responses = new HashSet<String>();
		for (int index = 0; index < 20; index++) {
			
			text.input("xxx" + index);
			response = waitForOutput(output);
			responses.add(response);
			assertKeyword(response, "condition");
		}
		if (responses.size() != 5) {
			fail("Condition defaults are not random: " + responses);
		}
		
		bot.shutdown();
	}

	/**
	 * Test responses.
	 */
	@org.junit.Test
	public void testResponses() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		// Test that responses are random.
		Set<String> responses = new HashSet<String>();
		for (int index = 0; index < 10; index++) {
			text.input("question 1 " + index);
			String response = waitForOutput(output);
			responses.add(response);
		}
		if (responses.size() < 5) {
			for (int index = 0; index < 10; index++) {
				text.input("question 1 " + index);
				String response = waitForOutput(output);
				responses.add(response);
			}
		}
		if (responses.size() < 5) {
			for (int index = 0; index < 10; index++) {
				text.input("question 1 " + index);
				String response = waitForOutput(output);
				responses.add(response);
			}
		}
		if (responses.size() < 5) {
			fail("Responses are not random: " + responses);
		}
		
		// Test that responses are still random.
		responses = new HashSet<String>();
		for (int index = 0; index < 10; index++) {
			text.input("question 1 " + index);
			String response = waitForOutput(output);
			responses.add(response);
		}
		if (responses.size() < 5) {
			for (int index = 0; index < 10; index++) {
				text.input("question 1 " + index);
				String response = waitForOutput(output);
				responses.add(response);
			}
		}
		if (responses.size() < 5) {
			for (int index = 0; index < 10; index++) {
				text.input("question 1 " + index);
				String response = waitForOutput(output);
				responses.add(response);
			}
		}
		if (responses.size() < 5) {
			fail("Responses are not random: " + responses);
		}
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

