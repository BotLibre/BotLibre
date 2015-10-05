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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;

/**
 * Test AIML2 support.
 */

public class TestAIML2 extends TextTest {
	
	public boolean isChatLog() {
		return false;
	}

	@BeforeClass
	public static void setup() {
		reset();
		new Bootstrap().bootstrapSystem(bot, false);
		Bot bot = Bot.createInstance();
		try {
			URL url = TestAIML.class.getResource("test-aiml2.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");
		} catch (Exception exception) {
			fail(exception.toString());
		}
		
		Utils.sleep(5000);
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testTopics() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("hockey");
		String response = waitForOutput(output);
		if (!response.equals("what?")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sports");
		response = waitForOutput(output);
		if (!response.equals("I like sports")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("hockey");
		response = waitForOutput(output);
		if (!response.equals("i love hockey")) {
			fail("Incorrect response: " + response);
		}
	}

	@org.junit.Test
	public void testIntervals() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("what is the date");
		String response = waitForOutput(output);
		String today = Utils.printDate(new Date(), "EEEE MMMM d y");
		if (!response.equals("The date is " + today)) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is the time");
		SimpleDateFormat format = new SimpleDateFormat("hh 'o''clock' a, zzzz");
		String time = format.format(new Date());
		response = waitForOutput(output);
		if (!response.equals("The time is " + time)) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how many days until Christmas?");
		response = waitForOutput(output);
		Calendar christmas = Calendar.getInstance();
		christmas.set(Calendar.MONTH, 11);
		christmas.set(Calendar.DAY_OF_MONTH, 25);
		Calendar date = Calendar.getInstance();
		int days = christmas.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR);
		if (!response.equals("" + days + " days until Christmas.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how many months until Christmas?");
		response = waitForOutput(output);
		date = Calendar.getInstance();
		int months = christmas.get(Calendar.MONTH) - date.get(Calendar.MONTH);
		if (!response.equals("" + months + " months until Christmas.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how many weeks until Christmas?");
		response = waitForOutput(output);
		date = Calendar.getInstance();
		int weeks = christmas.get(Calendar.WEEK_OF_YEAR) - date.get(Calendar.WEEK_OF_YEAR);
		if (!response.equals("" + weeks + " weeks until Christmas.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how many hours until Christmas?");
		response = waitForOutput(output);
		date = Calendar.getInstance();
		int hours = (christmas.get(Calendar.DAY_OF_YEAR) * 24)
				- ((date.get(Calendar.DAY_OF_YEAR) * 24) + date.get(Calendar.HOUR_OF_DAY));
		if (!response.equals("" + hours + " hours until Christmas.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how many minutes until Christmas?");
		date = Calendar.getInstance();
		response = waitForOutput(output);
		
		long minutes = (christmas.get(Calendar.DAY_OF_YEAR) * 24L * 60L)
				- (((date.get(Calendar.DAY_OF_YEAR) * 24L) + date.get(Calendar.HOUR_OF_DAY)) * 60L + date.get(Calendar.MINUTE));
		if (!response.equals("" + minutes + " minutes until Christmas.")) {
			fail("Incorrect response: " + response + " - should be " + minutes);
		}
		
		text.input("how many seconds until Christmas?");
		date = Calendar.getInstance();
		response = waitForOutput(output);
		long seconds = (christmas.get(Calendar.DAY_OF_YEAR) * 24L * 60L * 60L)
				- ((((date.get(Calendar.DAY_OF_YEAR) * 24L) + date.get(Calendar.HOUR_OF_DAY)) * 60L + date.get(Calendar.MINUTE)) * 60L);
		if (!response.equals("" + seconds + " seconds until Christmas.")) {
			fail("Incorrect response: " + response + " - should be " + seconds);
		}
	}

	@org.junit.Test
	public void testLoop() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("spaces 1 2 3");
		String response = waitForOutput(output);
		if (!response.equals("1 2 3")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("nospaces 1 2 3");
		response = waitForOutput(output);
		if (!response.equals("123")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("loop 3");
		response = waitForOutput(output);
		if (!response.equals("321")) {
			//fail("Incorrect response: " + response);
		}
		
		text.input("loop 5");
		response = waitForOutput(output);
		if (!response.equals("54321")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is 2 + 2");
		response = waitForOutput(output);
		if (!response.toLowerCase().equals("the answer is 4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is 0 + 1");
		response = waitForOutput(output);
		if (!response.equals("The answer is 1")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is 10 + 22");
		response = waitForOutput(output);
		if (!response.equals("The answer is 32")) {
			fail("Incorrect response: " + response);
		}
	}

	@org.junit.Test
	public void testLearn() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);
		
		text.input("something learned");
		String response = waitForOutput(output);
		if (!response.equals("no idea")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn something");
		response = waitForOutput(output);
		if (!response.equals("Ok")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something learned");
		response = waitForOutput(output);
		if (!response.equals("yep")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something2 learned2");
		response = waitForOutput(output);
		if (!response.equals("no idea")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn2 something2");
		response = waitForOutput(output);
		if (!response.equals("Ok2")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something2 learned2 cool stuff");
		response = waitForOutput(output);
		if (!response.equals("Cool stuff")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn3 something3");
		response = waitForOutput(output);
		if (!response.equals("Ok3")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something3 learned3 cool stuff");
		response = waitForOutput(output);
		if (!response.equals("That cool stuff")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn4 something4");
		response = waitForOutput(output);
		if (!response.equals("Ok4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something4 learned4 cool stuff");
		response = waitForOutput(output);
		if (!response.equals("no idea")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("topic something4");
		response = waitForOutput(output);
		if (!response.equals("The topic is now something4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something4 learned4 cool stuff");
		response = waitForOutput(output);
		if (!response.equals("Topic cool stuff")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn5 something5 a new response");
		response = waitForOutput(output);
		if (!response.equals("Ok5")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something5 learned5 ok");
		response = waitForOutput(output);
		if (!response.equals("A new response")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn6 something6 cool stuff");
		response = waitForOutput(output);
		if (!response.equals("Ok6")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("something6 learned6 other stuff");
		response = waitForOutput(output);
		if (!response.equals("I learned cool stuff not other stuff")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn7 something7 whatup notin");
		response = waitForOutput(output);
		if (!response.equals("Ok7")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("whatup");
		response = waitForOutput(output);
		if (!response.equals("Notin")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("learn8 something8 flowers candy");
		response = waitForOutput(output);
		if (!response.equals("Ok8")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("do you like flowers?");
		response = waitForOutput(output);
		if (!response.equals("No I like candy")) {
			fail("Incorrect response: " + response);
		}

		text.input("xxx");
		response = waitForOutput(output);

		text.input("wrong");
		response = waitForOutput(output);
		if (!response.equals("What should I have said?")) {
			fail("Incorrect response: " + response);
		}
		
		if (!isChatLog()) {
			text.input("yyy");
			response = waitForOutput(output);
			if (!response.equals("Okay, I will answer \"yyy\" to \"xxx\" next time")) {
				fail("Incorrect response: " + response);
			}
			
			text.input("xxx");
			response = waitForOutput(output);
			if (!response.equals("Yyy")) {
				fail("Incorrect response: " + response);
			}
		}
	}

	@org.junit.Test
	public void testTempVariables() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("local temp 44");
		String response = waitForOutput(output);
		if (!response.equals("44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("local tmp 44");
		response = waitForOutput(output);
		if (!response.equals("44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("if tmp 44");
		response = waitForOutput(output);
		if (!response.equals("True")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("if else tmp 44");
		response = waitForOutput(output);
		if (!response.equals("True")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("ifelse2 tmp 44");
		response = waitForOutput(output);
		if (!response.equals("True")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("if else2 tmp 44");
		response = waitForOutput(output);
		if (!response.equals("True")) {
			fail("Incorrect response: " + response);
		}
	}

	@org.junit.Test
	public void testNameElements() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("your name is Bob");
		String response = waitForOutput(output);
		if (!response.equals("My name is Bob")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is your name?");
		response = waitForOutput(output);
		if (!response.equals("My name is Bob")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("your age is 44");
		response = waitForOutput(output);
		if (!response.equals("My age is 44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is your age??");
		response = waitForOutput(output);
		if (!response.equals("My age is 44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("your hair is brown");
		response = waitForOutput(output);
		if (!response.equals("My hair is brown")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is your hair??");
		response = waitForOutput(output);
		if (!response.equals("My hair is brown")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("am I old?");
		response = waitForOutput(output);
		if (!response.equals("You are old")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("am I young?");
		response = waitForOutput(output);
		if (!response.equals("You are old")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how old am I?");
		response = waitForOutput(output);
		if (!response.equals("You are old")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how old am I?");
		response = waitForOutput(output);
		if (!response.equals("You are old")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("What age am I");
		response = waitForOutput(output);
		if (!response.equals("You are old")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("your age is 22");
		response = waitForOutput(output);
		if (!response.equals("My age is 22")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("am I old?");
		response = waitForOutput(output);
		if (!response.equals("You are young")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("am I young?");
		response = waitForOutput(output);
		if (!response.equals("You are young")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how old am I?");
		response = waitForOutput(output);
		if (!response.equals("You are young")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("What age am I");
		response = waitForOutput(output);
		if (!response.equals("You are young")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("My age is 22");
		response = waitForOutput(output);
		if (!response.equals("Your age is 22")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("are we the same age?");
		response = waitForOutput(output);
		if (!response.equals("Yes we are both 22")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("My age is 44");
		response = waitForOutput(output);
		if (!response.equals("Your age is 44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("are we the same age?");
		response = waitForOutput(output);
		if (!response.equals("No you are 22 and I am 44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what did you say?");
		response = waitForOutput(output);
		if (!response.equals("I said \"No you are 22 and I am 44\"")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("are we the same age?");
		response = waitForOutput(output);
		if (!response.equals("No you are 22 and I am 44")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what did I say?");
		response = waitForOutput(output);
		if (!response.equals("You said \"are we the same age?\"")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what did you say 3 times ago?");
		response = waitForOutput(output);
		if (!response.equals("I said \"I said \"No you are 22 and I am 44\"\"")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what did I say 4 times ago?");
		response = waitForOutput(output);
		if (!response.equals("You said \"are we the same age?\"")) {
			fail("Incorrect response: " + response);
		}
	}

	@org.junit.Test
	public void testFunctions() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("size");
		String response = waitForOutput(output);
		if (response.indexOf("Size is") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("vocabulary");
		response = waitForOutput(output);
		if (response.indexOf("Vocabulary is") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("id");
		response = waitForOutput(output);
		if (response.indexOf("Id is Anonymous") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("version");
		response = waitForOutput(output);
		if (response.indexOf("Version is") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("program");
		response = waitForOutput(output);
		if (response.indexOf("Program is") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("system");
		response = waitForOutput(output);
		if (response.indexOf("System is") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("explode hello");
		response = waitForOutput(output);
		if (!response.equals("H e l l o")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("normalize http://www.foo.com");
		response = waitForOutput(output);
		if (!response.equals("Http colon slash slash www dot foo dot com")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("denormalize http colon slash slash www dot foo dot com");
		response = waitForOutput(output);
		if (!response.equals("Http://www.foo.com")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("normalize joe@foo-bar.com");
		response = waitForOutput(output);
		if (!response.equals("Joe at foo dash bar dot com")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("denormalize joe at foo dash bar dot com");
		response = waitForOutput(output);
		if (!response.equals("Joe@foo-bar.com")) {
			fail("Incorrect response: " + response);
		}
	}


	/**
	 * Test new patterns wildcards.
	 */
	@org.junit.Test
	public void testPatterns() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINE);
		
		text.input("hi alice");
		String response = waitForOutput(output);
		if (!response.equals("Alice hi")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("are you alice");
		response = waitForOutput(output);
		if (!response.equals("Nope")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("how are you alice");
		response = waitForOutput(output);
		if (!isChatLog()) {
			if (!response.equals("Alice how are you")) {
				fail("Incorrect response: " + response);
			}
		} else {
			if (!response.equals("fine")) {
				fail("Incorrect response: " + response);
			}			
		}
		
		text.input("alice are you nice");
		response = waitForOutput(output);
		if (!response.equals("#alice nice")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("alice are you");
		response = waitForOutput(output);
		if (!response.equals("#alice")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("alice are you ok");
		response = waitForOutput(output);
		if (!isChatLog()) {
			if (!response.equals("#alice ok")) {
				fail("Incorrect response: " + response);
			}
		} else {
			if (!response.equals("yes")) {
				fail("Incorrect response: " + response);
			}			
		}
		
		text.input("you are fred");
		response = waitForOutput(output);
		if (!response.equals("no")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("you fred");
		response = waitForOutput(output);
		if (!response.equals("no")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("you love fred");
		response = waitForOutput(output);
		if (!response.equals("Fred love")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("you really love fred");
		response = waitForOutput(output);
		if (!response.equals("Fred really love")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("hat fred");
		response = waitForOutput(output);
		if (!response.equals("^fred")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("hat loves fred");
		response = waitForOutput(output);
		if (!response.equals("^fred loves")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("hat really loves fred");
		response = waitForOutput(output);
		if (!response.equals("^fred really loves")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("isbotname Test");
		response = waitForOutput(output);
		if (!response.equals("Yes, that is my name")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("topic foo");
		response = waitForOutput(output);
		
		text.input("istopic foo");
		response = waitForOutput(output);
		if (!response.equals("Yes, that is the topic")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("setcolor red");
		response = waitForOutput(output);
		if (!response.equals("Okay, red is a color.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("iscolor red");
		response = waitForOutput(output);
		if (!response.equals("Yes, red is a nice color.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("do you love me?");
		response = waitForOutput(output);
		if (!response.equals("Yes, I love you.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("do you hate me?");
		response = waitForOutput(output);
		if (!response.equals("No, I love you.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("do you eat");
		response = waitForOutput(output);
		if (!response.equals("Yes, I do.")) {
			fail("Incorrect response: " + response);
		}
		
	}

	/**
	 * Test HTML templates.
	 */
	@org.junit.Test
	public void testHTML() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("html bar");
		String response = waitForOutput(output);
		if (!response.equals("<b>bold</b><a href=\"foo.com\" target=\"_blank\">bar</a><p>hello<br>world</br></p><ol><li>foo</li><li>bar</li></ol>")) {
			fail("Incorrect response: " + response);
		}
		
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

