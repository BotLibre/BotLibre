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
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;

/**
 * Test language processing.
 */

public class TestAIML extends TextTest {

	@BeforeClass
	public static void setup() {
		reset();
		new Bootstrap().bootstrapSystem(bot, false);
		Bot bot = Bot.createInstance();
		try {
			URL url = TestAIML.class.getResource("alice.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");
			
			url = TestAIML.class.getResource("date.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");

			url = TestAIML.class.getResource("stack.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");

			url = TestAIML.class.getResource("test.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");

			url = TestAIML.class.getResource("pickup.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");

			url = TestAIML.class.getResource("alice.res");
			file = new File(url.toURI());
			bot.awareness().getSense(TextEntry.class).loadChatFile(file, "Response List", "", false, true);
		} catch (Exception exception) {
			fail(exception.toString());
		}
		
		Utils.sleep(5000);
		
		bot.shutdown();
	}

	/**
	 * Test alice AIML file.
	 */
	@org.junit.Test
	public void testPickup() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("say something");
		String response = waitForOutput(output);
		if (response.equals("say something")) {
			fail("Incorrect response: " + response);
		}

		text.input("say something else");
		response = waitForOutput(output);
		if (response.equals("say something else")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("say something else");
		response = waitForOutput(output);
		if (response.equals("say something else")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("say something else");
		response = waitForOutput(output);
		if (response.equals("say something else")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("say something else");
		response = waitForOutput(output);
		if (response.equals("say something else")) {
			fail("Incorrect response: " + response);
		}

		bot.shutdown();
	}

	/**
	 * Test test AIML file.
	 */
	@org.junit.Test
	public void testTest() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINE);
		
		text.input("botname");
		String response = waitForOutput(output);
		if (!response.equals("My name is ALICE")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("uppercase foo Bar.");
		response = waitForOutput(output);
		if (!response.equals("UPPER CASE FOO BAR")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("lowercase FOO Bar");
		response = waitForOutput(output);
		if (!response.equals("lower case foo bar")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("formal foo bar");
		response = waitForOutput(output);
		if (!response.equals("Foo Bar")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sentence foo barbar");
		response = waitForOutput(output);
		if (!response.equals("Foo barbar")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("gender he likes her, cause she digs him");
		response = waitForOutput(output);
		if (!response.equals("She likes his cause he digs her")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("person I am not you");
		response = waitForOutput(output);
		if (!response.equals("You are not I")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("person2 I am not him");
		response = waitForOutput(output);
		if (!response.equals("He am not me")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("test random 3");
		response = waitForOutput(output);
		if (!(response.equals("Random 1") || response.equals("Random 2") || response.equals("Random 3"))) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("test random 3");
		response = waitForOutput(output);
		if (!(response.equals("Random 1") || response.equals("Random 2") || response.equals("Random 3"))) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("quote");
		response = waitForOutput(output);
		if (!response.equals("\"I\" am \"very\" tired")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("size");
		response = waitForOutput(output);
		if (response.indexOf("Size is") == -1) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("date");
		response = waitForOutput(output);
		if (response.indexOf("Date is") == -1) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("version");
		response = waitForOutput(output);
		if (response.indexOf("Version is") == -1) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("sr quote");
		response = waitForOutput(output);
		if (!response.equals("Sr is \"I\" am \"very\" tired")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("html some stuff");
		response = waitForOutput(output);
		if (!response.equals("This<br/>is <a href=\"http://www.botlibre.com\">very</a><p>long</p><p/>ok<p>some stuff</p>")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("test a b c");
		response = waitForOutput(output);
		if (!response.equals("Test set to a b c")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("test");
		response = waitForOutput(output);
		if (!response.equals("A b c")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("test 2");
		response = waitForOutput(output);
		
		text.input("condition");
		response = waitForOutput(output);
		if (!response.equals("Two")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("cond2");
		response = waitForOutput(output);
		if (!response.equals("Two")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("cond3");
		response = waitForOutput(output);
		if (!response.equals("two")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("test a 9 and 9");
		response = waitForOutput(output);
		
		text.input("condition");
		response = waitForOutput(output);
		if (!response.equals("Nine something")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("star test and a b and c d");
		response = waitForOutput(output);
		if (!response.equals("Test")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("first star test and a and b");
		response = waitForOutput(output);
		if (!response.equals("Test")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("3rd star test and a and b");
		response = waitForOutput(output);
		if (!response.equals("B")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("input abc");
		response = waitForOutput(output);
		if (!response.equals("input abc")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("star waka");
		response = waitForOutput(output);
		
		text.input("last input");
		response = waitForOutput(output);
		if (!response.equals("star waka")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("star waka");
		response = waitForOutput(output);
		
		text.input("before last input");
		response = waitForOutput(output);
		if (!response.equals("last input")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("that");
		response = waitForOutput(output);
		if (!response.equals("last input")) {
			fail("Incorrect response: '" + response + "'");
		}

		text.input("star waka");
		response = waitForOutput(output);

		text.input("before that");
		response = waitForOutput(output);
		if (!response.equals("last input")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("1 2 3 4 5");
		response = waitForOutput(output);
		if (response.equals("2 2 3 4 5")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("count 5 numbers");
		response = waitForOutput(output);
		if (!response.equals("5 numbers numbers")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("1 2 3 4 5");
		response = waitForOutput(output);
		if (!response.equals("2 2 3 4 5")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("xxx yyy zzz");
		response = waitForOutput(output);
		if (!response.equals("number default")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("1 + 1");
		response = waitForOutput(output);
		if (!response.equals("2 + 2")) {
			fail("Incorrect response: '" + response + "'");
		}
		
		text.input("3 + 3");
		response = waitForOutput(output);
		if (!response.equals("4 + 3")) {
			fail("Incorrect response: '" + response + "'");
		}

		bot.shutdown();		
	}

	/**
	 * Test alice AIML file.
	 */
	@org.junit.Test
	public void testAlice() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("where is alice based");
		String response = waitForOutput(output);
		if (!response.equals("It is in Oakland, California.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("Who, is ALICE Toklas...?");
		response = waitForOutput(output);
		if (!response.equals("She was the partner of Gertrude Stein, and inventor of the pot brownie.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("Is HAL smarter than you?");
		response = waitForOutput(output);
		if (!response.equals("The ALICE series is the most intelligent chat robot software.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("call me alice");
		response = waitForOutput(output);
		if (!response.equals("My name is ALICE too!")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("WHERE IS THE DOCUMENTATION");
		response = waitForOutput(output);
		if (!response.equals("Try visiting <a href=\"http://www.Botbots.com\" target=\"_new\">Botbots.com</a> or <a href=\"http://www.alicebot.org\" target=\"_new\">Alicebot.org</a>.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("WHAT DOES A L I C E stand for?");
		response = waitForOutput(output);
		if (!response.equals("ALICE = Artificial Linguistic Internet Computer Entity")) {
			fail("Incorrect response: " + response);			
		}

		bot.shutdown();
	}

	/**
	 * Test date AIML file.
	 */
	@org.junit.Test
	public void testDate() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		Calendar date = Calendar.getInstance();
		
		text.input("WHAT YEAR IS THIS");
		String response = waitForOutput(output);
		if (!response.equals(date.get(Calendar.YEAR) + ".")) {
			fail("Incorrect response: " + response);			
		}

		text.input("What month is it right now?");
		response = waitForOutput(output);
		if (!response.equals(date.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + ".")) {
			fail("Incorrect response: " + response);			
		}

		text.input("IS TODAY SUNDAY or saturday?");
		response = waitForOutput(output);
		if (!response.equals("Today is " + date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US) + ".")) {
			fail("Incorrect response: " + response);			
		}

		bot.shutdown();		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

