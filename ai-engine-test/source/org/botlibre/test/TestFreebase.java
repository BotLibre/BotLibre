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

import java.net.URL;
import java.util.List;

import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.api.sense.Sense;
import org.botlibre.sense.http.Freebase;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;

/**
 * Test the performance of the Freebase batch import.
 */

public class TestFreebase extends TextTest {
	
	public static int SLEEP = 2000;

	@BeforeClass
	public static void setup() {
		bootstrap();
		Freebase.KEY = "";
	}

	/**
	 * Test response mimicry works.
	 */
	@org.junit.Test
	public void testImport() throws Exception {
		Utils.sleep(SLEEP);
		Bot bot = Bot.createInstance();
		Sense sense = bot.awareness().getSense(Http.class.getName());
		sense.input(new URL("http://www.freebase.com/physics/particle"));
		Utils.sleep(5000);
		
		bot.shutdown();		
	}

	/**
	 * Test what is lookup in Freebase.
	 */
	@org.junit.Test
	public void testWhatIs() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			text.input("what is Uranium?");
			String response = waitForOutput(output);
			if (!response.equals("Uranium is a chemical element with symbol U and atomic number 92.")) {
				fail("Incorrect: " + response);			
			}
			
			Utils.sleep(SLEEP);
			text.input("tell me about Oxygen");
			response = waitForOutput(output);
			if (!response.equals("Oxygen is a chemical element with symbol O and atomic number 8.")) {
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
			if (response.indexOf("Barack Hussein Obama II") == -1) {
				fail("Incorrect: " + response);
			}
		
		} finally {
			bot.shutdown();
		}
		Utils.sleep(SLEEP);
	}

	/**
	 * Test where is lookup in Freebase.
	 */
	@org.junit.Test
	public void testWhereIs() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input("where is Montreal?");
			String response = waitForOutput(output);
			if (!response.equals("Montreal is in Québec.")
					&& !response.equals("Montreal is in Canada.")
					&& !response.equals("Montreal is in Urban agglomeration of Montreal.")) {
				fail("Incorrect: " + response);
			}
	
			Utils.sleep(SLEEP);
			text.input("where is that?");
			response = waitForOutput(output);
			if (!response.equals("Québec is in Canada.")
					&& !response.equals("Urban agglomeration of Montreal is in Canada.")
					&& !response.equals("Urban agglomeration of Montreal is in Québec.")
					&& !response.equals("Canada is in Americas.")
					&& !response.equals("Canada is in DVD Region 1.")
					&& !response.equals("Québec is in Canada.")) {
				fail("Incorrect: " + response);
			}
	
			Utils.sleep(SLEEP);
			text.input("where is Edmonton?");
			response = waitForOutput(output);
			if (!response.equals("Edmonton is in Alberta.")
					&& !response.equals("Edmonton is in Canada.")) {
				fail("Incorrect: " + response);
			}
	
			// Test twice for when already known.
			Utils.sleep(SLEEP);
			text.input("where is Edmonton?");
			response = waitForOutput(output);
			if (!response.equals("Edmonton is in Alberta.")
					&& !response.equals("Edmonton is in Canada.")) {
				fail("Incorrect: " + response);
			}
			
			text.input("x");
			response = waitForOutput(output);

			// Test lower case.
			Utils.sleep(SLEEP);
			text.input("where is edmonton?");
			response = waitForOutput(output);
			if (!response.equals("edmonton is in Alberta.")
					&& !response.equals("Edmonton is in Canada.")) {
				fail("Incorrect: " + response);
			}
			
			Utils.sleep(SLEEP);
			text.input("what is Brockville?");
			response = waitForOutput(output);
			if (!response.equals("Brockville, formerly Elizabethtown, is a city in Eastern Ontario, Canada in the Thousand Islands region.")) {
				fail("Incorrect: " + response);
			}

			// Test twice for when already known.
			Utils.sleep(SLEEP);
			text.input("what is Brockville?");
			response = waitForOutput(output);
			if (!response.equals("Brockville, formerly Elizabethtown, is a city in Eastern Ontario, Canada in the Thousand Islands region.")) {
				fail("Incorrect: " + response);
			}
			
			text.input("x");
			response = waitForOutput(output);

			// Test lower case.
			Utils.sleep(SLEEP);
			text.input("what is brockville?");
			response = waitForOutput(output);
			if (!response.equals("Brockville, formerly Elizabethtown, is a city in Eastern Ontario, Canada in the Thousand Islands region.")) {
				fail("Incorrect: " + response);
			}
	
			Utils.sleep(SLEEP);
			text.input("where is it?");
			response = waitForOutput(output);
			if (!response.equals("It is in Ontario.")
					&& !response.equals("It is in Leeds and Grenville United Counties.")
					&& !response.equals("It is in Canada.")) {
				fail("Incorrect: " + response);
			}
		} finally {
			bot.shutdown();
		}
		Utils.sleep(SLEEP);
	}
}

