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
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
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
		bot.shutdown();
		Bot bot = Bot.createInstance();
		try {
			URL url = TestAIML.class.getResource("test-aiml2.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");
		} catch (Exception exception) {
			fail(exception.toString());
		}
		try {
			URL url = TestAIML.class.getResource("animal.set");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLSETFile(file, "animal", "");
		} catch (Exception exception) {
			fail(exception.toString());
		}
		try {
			URL url = TestAIML.class.getResource("state2capital.map");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLMAPFile(file, "state2capital", "");
		} catch (Exception exception) {
			fail(exception.toString());
		}
		try {
			URL url = TestAIML.class.getResource("bot.properties");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLPropertiesFile(file, "");
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
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testConditions() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("condition hello");
		String response = waitForOutput(output);
		checkResponse(response, "Hi");
		
		text.input("condition something");
		response = waitForOutput(output);
		checkResponse(response, "Condition default");
		
		text.input("condition");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		text.input("condition what is life");
		response = waitForOutput(output);
		checkResponse(response, "Life");
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testSets() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("Is a tiger an animal?");
		String response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("Is an ant an animal?");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		checkResponse(response, "yes");
		
		text.input("Is an Dog an Animal?");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("Is an dog an Animal?");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("Is a tree an animal");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		text.input("Is a mountain goat an animal?");
		response = waitForOutput(output);
		checkResponse(response, "yes"); // Compound sets not yet supported.
		
		text.input("Is a tiger shark an animal?");
		response = waitForOutput(output);
		checkResponse(response, "yes"); // Compound sets not yet supported.
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testMaps() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("What is the capital of alabama?");
		String response = waitForOutput(output);
		checkResponse(response, "Montgomery");
		
		text.input("capital of New York?");
		response = waitForOutput(output);
		checkResponse(response, "Albany");
		
		text.input("capital of Canada?");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testProperties() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("What is your favorite song?");
		String response = waitForOutput(output);
		checkResponse(response, "Imagine");
		
		text.input("What is your favorite ice cream?");
		response = waitForOutput(output);
		checkResponse(response, "Chocolate");
		
		text.input("What do you do for fun?");
		response = waitForOutput(output);
		checkResponse(response, "I like to impersonate human beings");
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testCompoundWords() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);
		
		text.input("stars topic");
		String response = waitForOutput(output);
		checkResponse(response, "Set stars");
		
		text.input("zzz");
		response = waitForOutput(output);
		checkResponse(response, "no stars");
		
		text.input("zzz star");
		response = waitForOutput(output);
		checkResponse(response, "one star");
		
		text.input("zzz yyy star");
		response = waitForOutput(output);
		checkResponse(response, "one star");
		
		text.input("zzz star star");
		response = waitForOutput(output);
		checkResponse(response, "two stars");

		if (!isChatLog()) {
			text.input("zzz yyy star star");
			response = waitForOutput(output);
			checkResponse(response, "two stars");
		}
		
		text.input("zzz give up");
		response = waitForOutput(output);
		checkResponse(response, "found give up");
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testIntervals() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);
		
		text.input("what is the date");
		String response = waitForOutput(output);
		String today = Utils.printDate(new Date(), "EEEE MMMM d y");
		checkResponse(response, "The date is " + today);
		
		text.input("what is the time");
		SimpleDateFormat format = new SimpleDateFormat("hh 'o''clock' a, zzzz");
		String time = format.format(new Date());
		response = waitForOutput(output);
		checkResponse(response, "The time is " + time);
		
		text.input("what is the time in GMT");
		Calendar calendar = Calendar.getInstance();
		response = waitForOutput(output);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		Time timeValue = Utils.parseTime(
				calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
		today = Utils.printTime(timeValue, "HH:mm:ss");
		checkResponse(response, "The time is " + today);
		
		text.input("what is the time in GMT+4");
		calendar = Calendar.getInstance();
		response = waitForOutput(output);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+4"));
		timeValue = Utils.parseTime(
				calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
		today = Utils.printTime(timeValue, "HH:mm:ss");
		checkResponse(response, "The time is " + today);
		
		text.input("what is the timestamp in GMT+4");
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT+4"));
		java.sql.Timestamp timestampValue = Utils.parseTimestamp(
				calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND));
		response = waitForOutput(output);
		checkResponse(response, "The timestamp is " + timestampValue.toString());
		
		text.input("what is the date in french");
		calendar = Calendar.getInstance();
		response = waitForOutput(output);
		Locale locale = Locale.FRENCH;
		String result = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale) + ", "
					+ calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " "
					+ String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		checkResponse(response, "The date is " + result);
		
		text.input("what is the date in France");
		calendar = Calendar.getInstance();
		response = waitForOutput(output);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT-1"));
		locale = Locale.FRENCH;
		result = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale) + ", "
					+ calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " "
					+ String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " - "
					+ String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
		checkResponse(response, "The date is " + result);
		
		text.input("what is the jdate in France");
		response = waitForOutput(output);
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("GMT-1"));
		locale = Locale.FRENCH;
		result = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, locale) + " "
					+ ((calendar.get(Calendar.DAY_OF_MONTH) < 10) ? "0" : "") + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + ", "
					+ String.valueOf(calendar.get(Calendar.YEAR)) + " - "
					+ String.valueOf((calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR))) + ":"
					+ ((calendar.get(Calendar.MINUTE) < 10) ? "0" : "") + String.valueOf(calendar.get(Calendar.MINUTE)) + ":"
					+ ((calendar.get(Calendar.SECOND) < 10) ? "0" : "") + String.valueOf(calendar.get(Calendar.SECOND));
		checkResponse(response, "The date is " + result);
		
		text.input("how many days until Christmas?");
		response = waitForOutput(output);
		Calendar christmas = Calendar.getInstance();
		christmas.set(Calendar.MONTH, 11);
		christmas.set(Calendar.DAY_OF_MONTH, 25);
		Calendar date = Calendar.getInstance();
		int days = christmas.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR);
		checkResponse(response, "" + days + " days until Christmas.");
		
		text.input("how many months until Christmas?");
		response = waitForOutput(output);
		date = Calendar.getInstance();
		int months = christmas.get(Calendar.MONTH) - date.get(Calendar.MONTH);
		checkResponse(response, "" + months + " months until Christmas.");
		
		text.input("how many weeks until Christmas?");
		response = waitForOutput(output);
		date = Calendar.getInstance();
		int weeks = christmas.get(Calendar.WEEK_OF_YEAR) - date.get(Calendar.WEEK_OF_YEAR) - 2;
		if (weeks < 0) {
			weeks = 52 + weeks;
		}
		checkResponse(response, "" + weeks + " weeks until Christmas.", "" + (weeks + 1) + " weeks until Christmas.");
		
		text.input("how many hours until Christmas?");
		response = waitForOutput(output);
		date = Calendar.getInstance();
		int hours = (christmas.get(Calendar.DAY_OF_YEAR) * 24)
				- ((date.get(Calendar.DAY_OF_YEAR) * 24) + date.get(Calendar.HOUR_OF_DAY));
		checkResponse(response, "" + hours + " hours until Christmas.");
		
		text.input("how many minutes until Christmas?");
		date = Calendar.getInstance();
		response = waitForOutput(output);
		
		long minutes = (christmas.get(Calendar.DAY_OF_YEAR) * 24L * 60L)
				- (((date.get(Calendar.DAY_OF_YEAR) * 24L) + date.get(Calendar.HOUR_OF_DAY)) * 60L + date.get(Calendar.MINUTE));
		checkResponse(response, "" + minutes + " minutes until Christmas.");
		
		text.input("how many seconds until Christmas?");
		date = Calendar.getInstance();
		response = waitForOutput(output);
		long seconds = (christmas.get(Calendar.DAY_OF_YEAR) * 24L * 60L * 60L)
				- ((((date.get(Calendar.DAY_OF_YEAR) * 24L) + date.get(Calendar.HOUR_OF_DAY)) * 60L + date.get(Calendar.MINUTE)) * 60L);
		checkResponse(response, "" + seconds + " seconds until Christmas.");
		
		bot.shutdown();
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
		if (!response.toLowerCase().equals("the answer is 1")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("what is 10 + 22");
		response = waitForOutput(output);
		if (!response.toLowerCase().equals("the answer is 32")) {
			fail("Incorrect response: " + response);
		}
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testLearn() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			//bot.setDebugLevel(Level.FINER);
			
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
		} finally {
			bot.shutdown();
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
		
		bot.shutdown();
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
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testSelf() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("2 + 2");
		String response = waitForOutput(output);
		if (response.indexOf("4") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sqrt 4");
		response = waitForOutput(output);
		if (response.indexOf("2") == -1) {
			fail("Incorrect response: " + response);
		}
		
		bot.shutdown();
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
		if (response.indexOf("id is anonymous") == -1) {
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
		
		bot.shutdown();
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
		//bot.setDebugLevel(Level.FINER);
		
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
		
		text.input("do you really me?");
		response = waitForOutput(output);
		if (response.equals("Yes, I love you.")) {
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
		
		text.input("x q");
		response = waitForOutput(output);
		checkResponse(response, "success1");
		
		text.input("q");
		response = waitForOutput(output);
		checkResponse(response, "success1");
		
		text.input("r");
		response = waitForOutput(output);
		checkResponse(response, "success1");
		
		text.input("x");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		text.input("a d");
		response = waitForOutput(output);
		checkResponse(response, "success2");
		
		text.input("d");
		response = waitForOutput(output);
		checkResponse(response, "success2");
		
		text.input("x d");
		response = waitForOutput(output);
		checkResponse(response, "success2");
		
		text.input("x d x");
		response = waitForOutput(output);
		checkResponse(response, "success2");
		
		text.input("x a x");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		text.input("pattern3");
		response = waitForOutput(output);
		checkResponse(response, "success3");
		
		text.input("x pattern3");
		response = waitForOutput(output);
		checkResponse(response, "success3");
		
		text.input("x y pattern3");
		response = waitForOutput(output);
		checkResponse(response, "success3");
		
		text.input("x y pattern3 x");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		text.input("pattern4");
		response = waitForOutput(output);
		checkResponse(response, "success4");
		
		text.input("pattern4 x y");
		response = waitForOutput(output);
		checkResponse(response, "success4");
		
		text.input("x pattern4");
		response = waitForOutput(output);
		checkResponse(response, "no idea");
		
		text.input("pattern5");
		response = waitForOutput(output);
		checkResponse(response, "success5");
		
		text.input("x y pattern5 x");
		response = waitForOutput(output);
		checkResponse(response, "success5");
		
		text.input("x y pattern5 x xx");
		response = waitForOutput(output);
		checkResponse(response, "success5");
		
		text.input("pre pattern6");
		response = waitForOutput(output);
		checkResponse(response, "success6");
		
		text.input("clear");
		response = waitForOutput(output);
		
		text.input("x pre pattern6");
		response = waitForOutput(output);
		checkResponse(response, "success6");
		
		text.input("pattern6");
		response = waitForOutput(output);
		checkResponse(response, "fail6");
		
		text.input("clear");
		response = waitForOutput(output);
		
		text.input("x pattern6");
		response = waitForOutput(output);
		checkResponse(response, "fail6");
		
		bot.shutdown();
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
		
		text.input("set nested");
		response = waitForOutput(output);
		if (!response.equals("Ok n4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("test nested");
		response = waitForOutput(output);
		if (!response.equals("Nested <br/>   <a href=\"http://www.botlibre.com/images/bot.png\" target=\"_blank\"><img alt=\"miniature\" heigh=\"100\" src=\"http://www.botlibre.com/images/bot.png\" width=\"100\"></img></a>")) {
			fail("Incorrect response: " + response);
		}
		
		bot.shutdown();		
	}

	/**
	 * Test regular expression patterns.
	 */
	@org.junit.Test
	public void testRegex() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);
		
		// Chatlogs do not support full patterns.
		if (!isChatLog()) {
			text.input("1234");
			String response = waitForOutput(output);
			checkResponse(response, "number");
	
			text.input("9");
			response = waitForOutput(output);
			checkResponse(response, "number");
	
			text.input("3.14");
			response = waitForOutput(output);
			checkResponse(response, "float");
	
			text.input("-100");
			response = waitForOutput(output);
			checkResponse(response, "float");
	
			text.input("1,000,000");
			response = waitForOutput(output);
			checkResponse(response, "numeric");
	
			text.input("1,235.55");
			response = waitForOutput(output);
			checkResponse(response, "numeric");
	
			text.input("foo@email.com");
			response = waitForOutput(output);
			checkResponse(response, "email");
		}

		text.input("another foo@email.com");
		String response = waitForOutput(output);
		checkResponse(response, "email");

		text.input("another 3.14");
		response = waitForOutput(output);
		checkResponse(response, "number");

		text.input("another http://www.email.com");
		response = waitForOutput(output);
		checkResponse(response, "url");

		text.input("12 / 6");
		response = waitForOutput(output);
		checkResponse(response, "2");

		text.input("10 / 2");
		response = waitForOutput(output);
		checkResponse(response, "5");

		text.input("10 * 2");
		response = waitForOutput(output);
		checkResponse(response, "20");

		text.input("what is 4 * 2");
		response = waitForOutput(output);
		checkResponse(response, "8");

		text.input("4 * 2 is how much?");
		response = waitForOutput(output);
		checkResponse(response, "8");

		text.input("tell me 4 * 2 is how much?");
		response = waitForOutput(output);
		checkResponse(response, "8");

		if (!isChatLog()) {
			text.input("what is a horse");
			response = waitForOutput(output);
			checkResponse(response, "I have no idea what a horse is.");
	
			text.input("What is a horse?");
			response = waitForOutput(output);
			checkResponse(response, "I have no idea what a horse is.");
		}
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

