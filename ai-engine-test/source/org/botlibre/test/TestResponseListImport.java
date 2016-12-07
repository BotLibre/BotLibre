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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;

/**
 * Test language processing.
 */

public class TestResponseListImport extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("test.res");
		File file = new File(url.toURI());
		bot.awareness().getSense(TextEntry.class).loadChatFile(file, "Response List", "", false, true);
		List<String> output = registerForOutput(text);
		text.input("this is a very complicated sentence the dog barks all night this is a good reply to that");
		waitForOutput(output);
		Utils.sleep(5000);
		bot.shutdown();
	}

	/**
	 * Test response keywords work.
	 */
	@org.junit.Test
	public void testKeywordMatching() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("this is a very very long sentence that is very long, yes, very long, it has one two three four five size seven eight nine ten or more words");
		String response = waitForOutput(output);
		if (!response.equals("how long?")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is a very very long sentence");
		response = waitForOutput(output);
		if (!response.equals("how long?")) {
			fail("did not match: " + response);
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("very long sentence");
		response = waitForOutput(output);
		if (!response.equals("how long?")) {
			fail("did not match: " + response);
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("sentence");
		response = waitForOutput(output);
		if (!response.equals("how long?")) {
			fail("did not match: " + response);
		}

		bot.shutdown();
	}

	/**
	 * Test response matching works.
	 */
	@org.junit.Test
	public void testResponseMatching() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);
		
		text.input("this is a very complicated sentence");
		String response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is very complicated sentence");
		response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is a very complicated");
		response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
				
		bot.shutdown();
	}

	/**
	 * Test response matching works.
	 */
	@org.junit.Test
	public void testResponseMatching2() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("the dog barks all night");
		String response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("barks all night");
		response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("barks all");
		response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		bot.shutdown();
	}

	/**
	 * Test previous.
	 */
	@org.junit.Test
	public void testPrevious() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("yes");
		String response = waitForOutput(output);
		checkResponse(response, "no");

		text.input("yes");
		response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("hi");
		response = waitForOutput(output);
		checkResponse(response, "do you like me?");

		text.input("yes");
		response = waitForOutput(output);
		checkResponse(response, "what do you like about me?");
		
		text.input("hey");
		response = waitForOutput(output);
		checkResponse(response, "are you ok?");

		text.input("yes");
		response = waitForOutput(output);
		checkResponse(response, "are you sure?");
		
		text.input("no");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("hi");
		response = waitForOutput(output);
		checkResponse(response, "do you like me?");
		
		text.input("no");
		response = waitForOutput(output);
		checkResponse(response, "why not?");
		
		bot.shutdown();
	}

	/**
	 * Test Self operators.
	 */
	@org.junit.Test
	public void testSelf() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("test self");
		String response = waitForOutput(output);
		checkResponse(response, "2 1 0 0.5 1");
		
		bot.shutdown();
	}

	/**
	 * Test the scripts were executed.
	 */
	@org.junit.Test
	public void testScript() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("do you like black");
		String response = waitForOutput(output);
		if (!response.equals("Yes, it is a nice color.")) {
			fail("did not match: " + response);			
		}
		
		text.input("What are you?");
		response = waitForOutput(output);
		if (!response.equals("I am a bot.")) {
			fail("did not match: " + response);			
		}
		
		bot.shutdown();
	}

	/**
	 * Test commands.
	 */
	@org.junit.Test
	public void testCommand() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("email mom");
		String response = waitForOutput(output);
		checkResponse(response, "sending email");
		
		bot.shutdown();
	}

	/**
	 * Test conditions and think elements.
	 */
	@org.junit.Test
	public void testConditions() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINE);
		
		text.input("am I your friend");
		String response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("you are my friend");
		response = waitForOutput(output);
		checkResponse(response, "ok, were friends");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

