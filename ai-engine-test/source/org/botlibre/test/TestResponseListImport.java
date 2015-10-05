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

import java.util.List;

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
	static String log = "this is a very complicated sentence\n"
			+ "this is a good reply to that\n\n"
			+ "the dog barks all night\n"
			+ "let him in then\n\n"
			+ "this is a very very long sentence that is very long, yes, very long, it has one two three four five size seven eight nine ten or more words\n"
			+ "how long?\n"
			+ "keywords: sentence\n\n";

	@BeforeClass
	public static void setup() {
		reset();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		text.processResponseLog(log, false);
		Utils.sleep(2000);
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
		
		text.input("this is a very complicated sentence");
		String response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this very complicated");
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

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

