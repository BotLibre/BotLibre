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
import java.util.List;
import java.util.logging.Level;

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

public class TestResponseListImport3 extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		//TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("test3.res");
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
		try {
			Bot bot = Bot.createInstance();
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input(null);
			String response = waitForOutput(output);
			checkResponse(response, "welcome");
			
			text.input("hi");
			response = waitForOutput(output);
			checkResponse(response, "hello");
			
			text.input("hey");
			response = waitForOutput(output);
			checkResponse(response, "huh?");

			bot.shutdown();
			
			bot = Bot.createInstance();
			language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			text = bot.awareness().getSense(TextEntry.class);
			output = registerForOutput(text);
			
			text.input(null);
			response = waitForOutput(output);
			checkResponse(response, "welcome");
			
			text.input("hey");
			response = waitForOutput(output);
			checkResponse(response, "hi");
			
		} finally {
			bot.shutdown();
		}
	}

	/**
	 * Test default responses.
	 */
	@org.junit.Test
	public void testDefaults() {
		try {
			Bot bot = Bot.createInstance();
			//bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input("xxx");
			String response = waitForOutput(output);
			checkResponse(response, "huh?");
			
			text.input("what");
			response = waitForOutput(output);
			checkResponse(response, "I asked you what?");
			
			text.input("huh");
			response = waitForOutput(output);
			checkResponse(response, "huh?");
			
			text.input("huh");
			response = waitForOutput(output);
			checkResponse(response, "I said huh first.");
			
		} finally {
			bot.shutdown();
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

