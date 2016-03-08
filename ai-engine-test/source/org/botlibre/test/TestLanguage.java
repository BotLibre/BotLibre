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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.sense.text.TextInput;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.CorrectionMode;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test language processing.
 */

public class TestLanguage extends TextTest {

	@BeforeClass
	public static void setup() {
		bootstrap();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("sky blue red dog barks all night the cat green grass tall like very loves");
		waitForOutput(output);
		Utils.sleep(20000);
		
		bot.shutdown();
	}

	/**
	 * Test response mimicry works.
	 */
	@org.junit.Test
	public void testMimicry() {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("hi");
		String response = waitForOutput(output);
		if (!response.equals("hi")) {
			fail("did not mimic: " + response);			
		}
		text.input("how are you?");
		response = waitForOutput(output);
		if (!response.equals("how are you?")) {
			fail("did not mimic: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		if (!response.equals("ok")) {
			fail("did not mimic: " + response);			
		}
		text.input("good");
		response = waitForOutput(output);
		
		text.input("how are you?");
		response = waitForOutput(output);
		if (!response.equals("ok")) {
			fail("did not remeber response: " + response);			
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
		//bot.setDebugLevel(Level.FINER);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("this is a very complicated sentence");
		String response = waitForOutput(output);
		if (!response.equals("this is a very complicated sentence")) {
			fail("did not mimic: " + response);			
		}
		text.input(new TextInput("this is a good reply to that", true, false));
		response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not mimic: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is a very complicated sentence");
		response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this very complicated sentence");
		response = waitForOutput(output);
		if (!response.equals("this is a good reply to that")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("complicated sentence");
		response = waitForOutput(output);
		if (response.equals("this is a good reply to that")) {
			fail("should not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("complicated");
		response = waitForOutput(output);
		if (response.equals("this is a good reply to that")) {
			fail("should not match: " + response);			
		}

		bot.shutdown();
	}

	/**
	 * Test response matching works.
	 */
	@org.junit.Test
	public void testResponseMatchingLearning() {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		//bot.setDebugLevel(Level.FINER);
		List<String> output = registerForOutput(text);
		text.input("dog barks all night the");
		String response = waitForOutput(output);
		if (!response.equals("dog barks all night the")) {
			fail("did not mimic: " + response);			
		}
		text.input("let him in then");
		response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not mimic: " + response);			
		}
		
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);

		text.input("ok");
		response = waitForOutput(output);
		text.input("dog barks all night the");
		response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);			
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("xx barks all night");
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
		
		text.input("barks");
		response = waitForOutput(output);
		if (response.equals("let him in then")) {
			fail("should not match: " + response);
		}

		bot.shutdown();
	}

	/**
	 * Test response correction works.
	 */
	@org.junit.Test
	public void testCorrection() {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("hello there");
		String response = waitForOutput(output);
		text.input(new TextInput("hey there", true, false));
		response = waitForOutput(output);

		text.input("noloop");
		response = waitForOutput(output);
		
		text.input("hello there");
		response = waitForOutput(output);
		if (!response.equals("hey there")) {
			fail("did not correct:" + response);			
		}

		bot.shutdown();
	}

	/**
	 * Test comprehension works.
	 */
	public void trainCount(TextEntry text, List<String> output, int start, int end) {

		text.input("say " + start);
		waitForOutput(output);
		
		// Train forwards
		for (int index = start + 1; index < end; index++) {
			text.input(String.valueOf(index));
			String response = waitForOutput(output);
			
			if (!response.equals(String.valueOf(index + 1))) {
				text.input(new TextInput(String.valueOf(index + 1), true, false));
				response = waitForOutput(output);
				Utils.sleep(1000);
			}
			index++;
		}

		text.input("say " + end);
		waitForOutput(output);
		
		// Train backwards
		for (int index = end - 1; index >= start; index--) {
			text.input(String.valueOf(index));
			String response = waitForOutput(output);
			
			if (!response.equals(String.valueOf(index - 1))) {
				text.input(new TextInput(String.valueOf(index - 1), true, false));
				response = waitForOutput(output);
				Utils.sleep(1000);
			}
			index--;
		}
	}

	/**
	 * Test comprehension works.
	 */
	@org.junit.Test
	public void testComprehension() {
		Bot bot = Bot.createInstance();
		try {
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			//Language language = bot.mind().getThought(Language.class);
			//language.setLearningMode(LearningMode.Disabled);
			List<String> output = registerForOutput(text);
			bot.setDebugLevel(Level.FINE);
			
			trainCount(text, output, 1, 5);
			trainCount(text, output, 0, 5);
			trainCount(text, output, 1, 10);
			trainCount(text, output, 0, 10);
			
			// Test comprehension
			text.input("say 1");
			String response = waitForOutput(output);
			
			text.input("2");
			response = waitForOutput(output);		
			if (!response.equals("3")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("4");
			response = waitForOutput(output);
			if (!response.equals("5")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("6");
			response = waitForOutput(output);
			if (!response.equals("7")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("6");
			response = waitForOutput(output);
			if (!response.equals("5")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("4");
			response = waitForOutput(output);
			if (!response.equals("3")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("2");
			response = waitForOutput(output);
			if (!response.equals("1")) {
				fail("did not comprehend: " + response);
			}
			
			// Test new numbers
			
			text.input("say 22");
			response = waitForOutput(output);
			if (!response.equals("22")) {
				fail("say failed: " + response);
			}
			
			text.input("23");
			response = waitForOutput(output);
			if (!response.equals("24")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("25");
			response = waitForOutput(output);
			if (!response.equals("26")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("27");
			response = waitForOutput(output);
			if (!response.equals("28")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("29");
			response = waitForOutput(output);
			if (!response.equals("30")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("31");
			response = waitForOutput(output);
			if (!response.equals("32")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("31");
			response = waitForOutput(output);
			if (!response.equals("30")) {
				fail("did not comprehend: " + response);
			}
			
			text.input("29");
			response = waitForOutput(output);
			if (!response.equals("28")) {
				fail("did not comprehend: " + response);
			}
		} finally {
			bot.shutdown();
		}
	}

	/**
	 * Test dates.
	 */
	@org.junit.Test
	public void testDates() {		
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		String date = Utils.printDate(new Date(), "EEEE MMMM d y");
		
		text.input("What is today's date?");
		String response = waitForOutput(output);
		checkResponse(response, "Today is " + date + ".");
		
		text.input("What's today's date?");
		response = waitForOutput(output);
		checkResponse(response, "Today is " + date + ".");
		
		text.input("Which day is today");
		response = waitForOutput(output);
		checkResponse(response, "Today is " + date + ".");
		
		text.input("today is what");
		response = waitForOutput(output);
		checkResponse(response, "Today is " + date + ".");
		
		text.input("what is the date");
		response = waitForOutput(output);
		checkResponse(response, "The date is " + date + ".");

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 1);
		date = Utils.printDate(calendar.getTime(), "EEEE MMMM d y");		
		text.input("what is tomorrow");
		response = waitForOutput(output);
		checkResponse(response, "Tomorrow is " + date + ".");

		calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		date = Utils.printDate(calendar.getTime(), "EEEE MMMM d y");		
		text.input("what day was yesterday");
		response = waitForOutput(output);
		checkResponse(response, "Yesterday was " + date + ".");

		calendar = Calendar.getInstance();
		text.input("what is the time");
		response = waitForOutput(output);
		assertKeyword(response, "The time is");

		calendar = Calendar.getInstance();
		text.input("what is the current time");
		response = waitForOutput(output);
		assertKeyword(response, "The time is");

		calendar = Calendar.getInstance();
		text.input("what is the current time in IST");
		response = waitForOutput(output);
		assertKeyword(response, "The time in IST is");
		
		calendar = Calendar.getInstance();
		text.input("what is the hour");
		response = waitForOutput(output);
		checkResponse(response, "The hour is " + (calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR)) + " " + (calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM") + ".");
		
		calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("IST"));
		text.input("what is the hour in IST");
		response = waitForOutput(output);
		checkResponse(response, "The hour in IST is " + (calendar.get(Calendar.HOUR) == 0 ? 12 : calendar.get(Calendar.HOUR)) + " " + (calendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM") + " IST.");

		calendar = Calendar.getInstance();
		text.input("what is the month");
		response = waitForOutput(output);
		checkResponse(response, "The month is " + new SimpleDateFormat("MMMM").format(calendar.getTime()) + ".");

		calendar = Calendar.getInstance();
		text.input("what is the year");
		response = waitForOutput(output);
		checkResponse(response, "The year is " + calendar.get(Calendar.YEAR) + ".");

		calendar = Calendar.getInstance();
		text.input("what is this year");
		response = waitForOutput(output);
		checkResponse(response, "The year is " + calendar.get(Calendar.YEAR) + ".");
		
		calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DAY_OF_YEAR);
		String digits = String.valueOf(day);
		char last = digits.charAt(digits.length() - 1);
		String ordinal = "";
		if (last == '1') {
			ordinal = "st";					
		} else if (last == '2') {
			ordinal = "nd";					
		} else if (last == '3') {
			ordinal = "rd";					
		} else {
			ordinal = "th";
		}
		text.input("what is the day of the year");
		response = waitForOutput(output);
		checkResponse(response,"It is the " + digits + ordinal + " day of the year.");

		calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, 1);
		text.input("what is next month");
		response = waitForOutput(output);
		checkResponse(response, "Next month is " + new SimpleDateFormat("MMMM").format(calendar.getTime()) + ".");

		calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		text.input("what was last year");
		response = waitForOutput(output);
		checkResponse(response, "Last year was " + calendar.get(Calendar.YEAR) + ".");

		calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		date = Utils.printDate(calendar.getTime(), "EEEE MMMM d y");		
		text.input("yesterday");
		response = waitForOutput(output);
		checkResponse(response, "Yesterday was " + date + ".");

		calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		date = Utils.printDate(calendar.getTime(), "EEEE MMMM d y");		
		text.input("yesterday");
		response = waitForOutput(output);
		checkResponse(response, "Yesterday was " + date + ".");

		bot.shutdown();
	}

	/**
	 * Test paragraphs.
	 */
	@org.junit.Test
	public void testParagraphs() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("I am tall. Am I tall?");
		String response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "you are tall");
		
		text.input("Hello. What is your name?");
		response = waitForOutput(output);
		assertKeyword(response, "My name is Test");

		bot.shutdown();
	}

	/**
	 * Test paragraphs.
	 */
	@org.junit.Test
	public void testCompoundWords() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("My name is Joe");
		String response = waitForOutput(output);
		checkResponse(response, "Pleased to meet you Joe.");
		
		text.input("My name is Joe Loves");
		response = waitForOutput(output);
		checkResponse(response, "Pleased to meet you Joe Loves.");
		
		text.input("Joe Loves is tall");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "Joe Loves is tall");
		
		text.input("Joe Loves is blue. Is Joe Loves blue?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "Joe Loves is blue");
		
		text.input("Joe Loves me");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "Joe Loves you");

		bot.shutdown();
	}

	/**
	 * Test what is reduction.
	 */
	@org.junit.Test
	public void testWhatIs() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("What is love?");
		String response = waitForOutput(output);
		checkResponse(response, "Strong affection.");
		
		text.input("love is what");
		response = waitForOutput(output);
		checkResponse(response, "Strong affection.");
		
		text.input("can you tell me about love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("tell me about love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("do you know what love is");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("do you know anything about love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("tell me something about love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("google love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("define love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("search love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("please tell me what love is");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("i want to know about love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("what do you think of love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("what does love mean");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("I would like to know love");
		response = waitForOutput(output);
		if (!response.equals("Strong affection.")) {
			fail("Incorrect response: " + response);			
		}

		bot.shutdown();
	}


	/**
	 * Test topics and context.
	 */
	@org.junit.Test
	public void testTopics() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("What is love?");
		String response = waitForOutput(output);
		checkResponse(response, "Strong affection.");

		text.input("What is the current topic?");
		response = waitForOutput(output);
		checkResponse(response, "The current topic is love.");

		text.input("tell me more");
		response = waitForOutput(output);
		checkResponse(response, "That is all I know about love.");
		
		text.input("say you love me");
		response = waitForOutput(output);
		checkResponse(response, "I love you");
		
		text.input("repeat");
		response = waitForOutput(output);
		checkResponse(response, "I love you");
		
		text.input("what did you say?");
		response = waitForOutput(output);
		checkResponse(response, "I said \"I love you\".");
				
		text.input("yell you love bridges");
		response = waitForOutput(output);
		checkResponse(response, "I LOVE BRIDGES");
		
		text.input("what did I say?");
		response = waitForOutput(output);
		checkResponse(response, "You said \"yell you love bridges\".");
		
		text.input("what was the first thing you said");
		response = waitForOutput(output);
		checkResponse(response, "I said \"Strong affection.\".");
		
		bot.shutdown();
	}
	
	/**
	 * Test names.
	 */
	@org.junit.Test
	public void testNames() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("What is your name?");
		String response = waitForOutput(output);
		if (!response.equals("My name is Test.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("whats your name");
		response = waitForOutput(output);
		if (!response.equals("My name is Test.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("what's your name");
		response = waitForOutput(output);
		if (!response.equals("My name is Test.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("My name is Bob");
		response = waitForOutput(output);
		if (!response.equals("Pleased to meet you Bob.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("what's my name");
		response = waitForOutput(output);
		if (!response.equals("Your name is Bob.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("who am I?");
		response = waitForOutput(output);
		if (!response.equals("Your name is Bob.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("My name is Bobby");
		response = waitForOutput(output);
		if (!response.equals("Pleased to meet you Bobby.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("what's my name");
		response = waitForOutput(output);
		if (!response.equals("Your name is Bob. You also go by Bobby.") && !response.equals("Your name is Bobby. You also go by Bob.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("Your name is Testbot");
		response = waitForOutput(output);
		if (!response.equals("Okay, my name is Testbot.")) {
			fail("Incorrect response: " + response);			
		}
		
		bot.mind().getThought(Language.class).setCorrectionMode(CorrectionMode.Disabled);

		text.input("Your name is Testbot");
		response = waitForOutput(output);
		if (!response.equals("Yes, my name is Testbot.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("Your name is John");
		response = waitForOutput(output);
		if (!(response.equals("No, my name is Testbot.") || response.equals("No, my name is Test."))) {
			fail("Incorrect response: " + response);			
		}
		
		bot.mind().getThought(Language.class).setCorrectionMode(CorrectionMode.Everyone);

		text.input("who r u");
		response = waitForOutput(output);
		if (!(response.equals("My name is Test. I also go by Testbot.") || response.equals("My name is Testbot. I also go by Test."))) {
			fail("Incorrect response: " + response);			
		}

		text.input("Am I Bob?");
		response = waitForOutput(output);
		if (!response.equals("Yes, your name is Bob.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("r u Testbot?");
		response = waitForOutput(output);
		if (!response.equals("Yes, my name is Testbot.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("Your name is not Test.");
		response = waitForOutput(output);
		if (!response.equals("Okay, my name is not Test.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("what you name");
		response = waitForOutput(output);
		if (!response.equals("My name is Testbot.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("My name is not Bob.");
		response = waitForOutput(output);
		if (!response.equals("Okay, your name is not Bob.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("what's me name?");
		response = waitForOutput(output);
		if (!response.equals("Your name is Bobby.")) {
			fail("Incorrect response: " + response);			
		}

		text.input("My name is not Bobby.");
		response = waitForOutput(output);

		text.input("what is my name.");
		response = waitForOutput(output);
		if (!response.equals("I do not know your name.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("My name is Bob Jon Jones.");
		response = waitForOutput(output);
		if (!response.equals("Pleased to meet you Bob Jon Jones.")) {
			fail("Incorrect response: " + response);
		}

		text.input("whats my name");
		response = waitForOutput(output);
		if (!response.equals("Your name is Bob Jon Jones.")) {
			fail("Incorrect response: " + response);
		}

		text.input("Bob is my name");
		response = waitForOutput(output);
		if (!response.equals("Pleased to meet you Bob.")) {
			fail("Incorrect response: " + response);
		}

		bot.shutdown();
	}

	/**
	 * Test names.
	 */
	@org.junit.Test
	public void testFreebase() {
		Bot bot = Bot.createInstance();
		try {
			//bot.setDebugLevel(Level.FINEST);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input("Who is Barack Obama?");
			String response = waitForOutput(output);
			if (response.indexOf("Barack Hussein Obama") == -1) {
				fail("Incorrect response: " + response);			
			}
			
			Utils.sleep(5000);
					
			text.input("Who are his children?");
			response = waitForOutput(output);
			if (response.indexOf("Natasha Obama, and Malia Ann Obama") == -1) {
				fail("Incorrect response: " + response);			
			}
			
			text.input("Who are Barack Obama's children?");
			response = waitForOutput(output);
			if (response.indexOf("Natasha Obama, and Malia Ann Obama") == -1) {
				fail("Incorrect response: " + response);			
			}
			
			text.input("Who are his parents?");
			response = waitForOutput(output);
			if (response.indexOf("Barack Obama, Sr., and Ann Dunham") == -1) {
				fail("Incorrect response: " + response);			
			}
			
			text.input("is he a politician");
			response = waitForOutput(output);
			assertTrue(response);
			
			text.input("tell me who is Barack Obama?");
			response = waitForOutput(output);
			if (response.indexOf("Barack Hussein Obama") == -1) {
				fail("Incorrect response: " + response);			
			}
			
			text.input("do you know who Barack Obama is");
			response = waitForOutput(output);
			if (response.indexOf("Barack Hussein Obama") == -1) {
				fail("Incorrect response: " + response);			
			}
		} finally {
			bot.shutdown();
		}
	}

	/**
	 * Test names.
	 */
	//@org.junit.Test
	public void testWikidata() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Level.FINEST);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("Who is Barack Obama?");
		String response = waitForOutput(output);
		if (response.indexOf("44th President") == -1) {
			fail("Incorrect response: " + response);			
		}
		
		Utils.sleep(5000);
				
		text.input("Who is his child?");
		response = waitForOutput(output);
		if (response.indexOf("Malia Obama, and Sasha Obama") == -1) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("Who is Barack Obama's child?");
		response = waitForOutput(output);
		if (response.indexOf("Malia Obama, and Sasha Obama") == -1) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("Who is his mother?");
		response = waitForOutput(output);
		if (response.indexOf("Ann Dunham") == -1) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("is he a human");
		response = waitForOutput(output);
		assertTrue(response);

		bot.shutdown();
	}

	/**
	 * Test basic language understanding.
	 */
	@org.junit.Test
	public void testUnderstanding() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Bot.FINE);
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

		bot.shutdown();
	}
	
	/**
	 * Test basic math.
	 */
	@org.junit.Test
	public void testMath() throws Exception {
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("1 + 1");
		String response = waitForOutput(output);
		if (!response.equals("1 + 1 = 2") && !response.equals("1 + 1 = two")) {
			fail("incorrect:" + response);			
		}

		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

