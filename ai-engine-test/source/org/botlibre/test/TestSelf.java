/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.BeforeClass;

/**
 * Test Self scripting and API.
 */

public class TestSelf extends TextTest {
	
	public static String applicationId = "";

	@BeforeClass
	public static void setup() {
		bootstrap();
		Bot bot = Bot.createInstance();
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Vertex script = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("test.self"), "", false, network);
		language.setRelationship(Primitive.STATE, script);
		network.save();
		bot.shutdown();
	}

	@org.junit.Test
	public void testOperations() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("test math");
			String response = waitForOutput(output);
			checkResponse(response, "pass");

			text.input("test operations");
			response = waitForOutput(output);
			checkResponse(response, "pass");
		} finally {
			bot.shutdown();
		}
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

		text.input("another foo@email.com");
		response = waitForOutput(output);
		checkResponse(response, "email");

		text.input("another 3.14");
		response = waitForOutput(output);
		checkResponse(response, "number");

		text.input("another http://www.email.com");
		response = waitForOutput(output);
		checkResponse(response, "url");

		text.input("2000-01-01");
		response = waitForOutput(output);
		checkResponse(response, "date");

		text.input("x 2000-01-01");
		response = waitForOutput(output);
		checkResponse(response, "x 2000-01-01");

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

		text.input("what is a horse");
		response = waitForOutput(output);
		checkResponse(response, "I have no idea what a horse is.");

		text.input("What is a horse?");
		response = waitForOutput(output);
		checkResponse(response, "I have no idea what a horse is.");
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testDate() {
		Bot bot = Bot.createInstance();
		try {
			//bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			
			text.input("test dates");
			String response = waitForOutput(output);
			checkResponse(response, "pass");

			String date = Utils.printDate(new Date(), "EEEE MMMM d y");
			text.input("date");
			response = waitForOutput(output);
			checkResponse(response, date);
			
			text.input("time");
			response = waitForOutput(output);
			assertKeyword(response, ":");
			
			text.input("timestamp");
			response = waitForOutput(output);
			assertKeyword(response, ":");
			
			text.input("timeZone");
			response = waitForOutput(output);
			assertKeyword(response, "America/New_York");
			
			text.input("any date 2016-10-11 12:01:04");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 12:01:04.0");
			
			text.input("any date 11-10-2016 12:01:04");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 12:01:04.0");
			
			text.input("any date 2016-10-11 12:01:04 EDT");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 12:01:04.0");
			
			text.input("any date Jan 1, 2016 12:01:04 EST");
			response = waitForOutput(output);
			checkResponse(response, "2016-01-01 12:01:04.0");
			
			text.input("any date January 1, 2016 12:01:04 EST");
			response = waitForOutput(output);
			checkResponse(response, "2016-01-01 12:01:04.0");
			
			text.input("any date 2016/10/11");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 00:00:00.0");
			
			text.input("any date 11/10/16");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 00:00:00.0");
			
			text.input("any date Jan 12, 2016");
			response = waitForOutput(output);
			checkResponse(response, "2016-01-12 00:00:00.0");
			
			text.input("any date January 12 2016");
			response = waitForOutput(output);
			checkResponse(response, "2016-01-12 00:00:00.0");
			
			text.input("any date 2016/10/11 1:30 pm");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 13:30:00.0");
			
			text.input("any date 2016/100/11 1:30 pm");
			response = waitForOutput(output);
			checkResponse(response, "any date 2016/100/11 1:30 pm");
			
			text.input("any date 2017-1-19T13:30:0-08:00");
			response = waitForOutput(output);
			checkResponse(response, "2017-01-19 16:30:00.0");
			
			text.input("any date 2 pm");
			response = waitForOutput(output);
			checkResponse(response, "1970-01-01 14:00:00.0");
			
			text.input("any date 12:30 pm");
			response = waitForOutput(output);
			checkResponse(response, "1970-01-01 12:30:00.0");
			
			text.input("any date 23:30:00");
			response = waitForOutput(output);
			checkResponse(response, "1970-01-01 23:30:00.0");
			
			text.input("hours 3 2016/10/11");
			response = waitForOutput(output);
			checkResponse(response, "2016-10-11 03:00:00.0");
			
		} finally {
			bot.shutdown();
		}
	}

	@org.junit.Test
	public void testTopic() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("topic music");
			String response = waitForOutput(output);
			checkResponse(response, "topic set");
			
			text.input("topic");
			response = waitForOutput(output);
			checkResponse(response, "music");
			
			text.input("clear topic");
			response = waitForOutput(output);
			checkResponse(response, "topic cleared");
			
			text.input("topic");
			response = waitForOutput(output);
			checkResponse(response, "none");
			
			text.input("test conversation");
			response = waitForOutput(output);
			checkResponse(response, "pass_________");
			
			text.input("topic empty");
			response = waitForOutput(output);
			checkResponse(response, "topic set");
			
			text.input("");
			response = waitForOutput(output);
			checkResponse(response, "success");
			
			text.input("clear topic");
			response = waitForOutput(output);
			checkResponse(response, "topic cleared");
			
		} finally {
			bot.shutdown();
		}
	}

	@org.junit.Test
	public void testLanguage() {
		Bot bot = Bot.createInstance();
		try {
			bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("test language");
			String response = waitForOutput(output);
			checkResponse(response, "ok");
			
			text.input("test empty");
			response = waitForOutput(output);
			checkResponse(response, "");
			
			text.input("test hello world equals hello world");
			response = waitForOutput(output);
			checkResponse(response, "hello world");
			
			text.input("test hello equals hello world");
			response = waitForOutput(output);
			checkResponse(response, "fail");
			
			text.input("test hello equals hello");
			response = waitForOutput(output);
			checkResponse(response, "Hello hello");
			
			text.input("test hello equals bye");
			response = waitForOutput(output);
			checkResponse(response, "fail");
			
			text.input("test how are you equals how are you");
			response = waitForOutput(output);
			checkResponse(response, "How are you how are you");
			
		} finally {
			bot.shutdown();
		}
	}

	@org.junit.Test
	public void testArrays() {
		Bot bot = Bot.createInstance();
		try {
			//bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("test arrays");
			String response = waitForOutput(output);
			checkResponse(response, "ok");
			
		} finally {
			bot.shutdown();
		}
	}

	@org.junit.Test
	public void testJSON() {
		Bot bot = Bot.createInstance();
		try {
			//bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("test nested json");
			String response = waitForOutput(output);
			checkResponse(response, "123 Main Street");
			
		} finally {
			bot.shutdown();
		}
	}

	@org.junit.Test
	public void testSenses() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("email test@botlibre.com test this is a test");
			String response = waitForOutput(output);
			checkResponse(response, "ok");
			
			text.input("tweet hello world");
			response = waitForOutput(output);
			checkResponse(response, "ok");
			
			text.input("facebook hello world");
			response = waitForOutput(output);
			checkResponse(response, "ok");
			
			text.input("telegram hello world");
			response = waitForOutput(output);
			checkResponse(response, "ok");
			
			text.input("define water");
			response = waitForOutput(output);
			checkResponse(response, "A substance found at room temperature and pressure as a clear liquid; it is present naturally as rain, and found in rivers, lakes and seas; its solid form is ice and its gaseous form is steam.");
			
			text.input("lookup Barack Obama");
			response = waitForOutput(output);
			checkResponse(response, "44th President of the United States of America");
			
			text.input("lookup mother on Barack Obama");
			response = waitForOutput(output);
			checkResponse(response, "Ann Dunham");
			
		} finally {
			bot.shutdown();
		}
	}

	@org.junit.Test
	public void testHttp() {
		Bot bot = Bot.createInstance();
		bot.setDebugLevel(Level.FINE);
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("rss http://botlibre.blogspot.com/feeds/posts/default");
			String response = waitForOutput(output);
			assertKeyword(response, "http://");

			text.input("xml http://botlibre.com/rest/api/form-chat?instance=14187473&message=ping&application=" + applicationId + "");
			response = waitForOutput(output);
			assertKeyword(response, "pong");
			assertKeyword(response, "message");
			assertKeyword(response, "conversation");

			text.input("xml http://botlibre.com/rest/api/form-chat?instance=14187473&message=ping&application=" + applicationId + " message");
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("xpath http://botlibre.com/rest/api/form-chat?instance=14187473&message=ping&application=" + applicationId + " @emote");
			response = waitForOutput(output);
			checkResponse(response, "NONE");

			text.input("xpath http://botlibre.com/rest/api/form-chat?instance=14187473&message=ping&application=" + applicationId + " message");
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("xpath http://botlibre.com/rest/api/form-get-all-instances?application=" + applicationId + " instance/@isPrivate");
			response = waitForOutput(output);
			checkResponse(response, "false");

			text.input("xpath http://botlibre.com/rest/api/form-get-all-instances?application=" + applicationId + " instance/avatar");
			response = waitForOutput(output);
			assertKeyword(response, "avatar");

			text.input("xpath http://botlibre.com/rest/api/form-get-all-instances?application=" + applicationId + " instance[1]/avatar");
			response = waitForOutput(output);
			assertKeyword(response, "avatar");

			text.input("html https://botlibre.com head/meta[2]/@content");
			response = waitForOutput(output);
			checkResponse(response, "Paphus Solutions Inc.");

			text.input("html https://botlibre.com count(head/meta)");
			response = waitForOutput(output);
			checkResponse(response, "6");

			text.input("html text https://botlibre.com //h1");
			response = waitForOutput(output);
			checkResponse(response, "Bot Libre!");

			text.input("html list 1 https://botlibre.com //h3/text()");
			response = waitForOutput(output);
			checkResponse(response, "Browse");
			
			text.input("html xml https://botlibre.com head/meta[2]");
			response = waitForOutput(output);
			assertKeyword(response, "Paphus Solutions Inc.");
			assertKeyword(response, "<meta");

			text.input("postXML ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("postHTML ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "Brain Bot");

			text.input("json https://botlibre.com/rest/json/form-chat?application=" + applicationId + "&instance=165&message=ping");
			response = waitForOutput(output);
			assertKeyword(response, "pong");
			assertKeyword(response, "message");
			assertKeyword(response, "conversation");

			text.input("json https://botlibre.com/rest/json/form-chat?application=" + applicationId + "&instance=165&message=ping message");
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("postJSON ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("postJSON2 ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("csv https://www.botlibre.com/script?file&id=14026345");
			response = waitForOutput(output);
			assertKeyword(response, "Jon Dow");
			assertKeyword(response, "Jane Smith");
			assertKeyword(response, "George Jones the 3rd");

			text.input("json https://www.botlibre.com/script?file&id=14026381");
			response = waitForOutput(output);
			assertKeyword(response, "Jon Dow");
			assertKeyword(response, "Jane Smith");
			assertKeyword(response, "George Jones the 3rd");			
			
		} finally {
			bot.shutdown();
		}
	}
}

