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
		
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Vertex script = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("test.self"), "", false, network);
		language.setRelationship(Primitive.STATE, script);
		network.save();
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

	@org.junit.Test
	public void testDate() {
		Bot bot = Bot.createInstance();
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			String date = Utils.printDate(new Date(), "EEEE MMMM d y");
			text.input("date");
			String response = waitForOutput(output);
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
		//bot.setDebugLevel(Level.FINER);
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("rss http://botlibre.blogspot.com/feeds/posts/default");
			String response = waitForOutput(output);
			assertKeyword(response, "http://");

			text.input("xml http://botlibre.com/rest/api/form-chat?instance=165&message=ping&application=" + applicationId + "");
			response = waitForOutput(output);
			assertKeyword(response, "pong");
			assertKeyword(response, "message");
			assertKeyword(response, "conversation");

			text.input("xml http://botlibre.com/rest/api/form-chat?instance=165&message=ping&application=" + applicationId + " message");
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("xpath http://botlibre.com/rest/api/form-chat?instance=165&message=ping&application=" + applicationId + " @emote");
			response = waitForOutput(output);
			checkResponse(response, "NONE");

			text.input("xpath http://botlibre.com/rest/api/form-chat?instance=165&message=ping&application=" + applicationId + " message");
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

			text.input("html http://botlibre.com head/meta[2]/@content");
			response = waitForOutput(output);
			checkResponse(response, "Paphus Solutions Inc.");

			text.input("postXML ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("postHTML ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "ping");

			text.input("json http://botlibre.com/rest/json/form-chat?application=" + applicationId + "&instance=165&message=ping");
			response = waitForOutput(output);
			assertKeyword(response, "pong");
			assertKeyword(response, "message");
			assertKeyword(response, "conversation");

			text.input("json http://botlibre.com/rest/json/form-chat?application=" + applicationId + "&instance=165&message=ping message");
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("postJSON ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("postJSON2 ping " + applicationId);
			response = waitForOutput(output);
			checkResponse(response, "pong");

			text.input("csv http://www.botlibre.com/script?file&id=14026345");
			response = waitForOutput(output);
			assertKeyword(response, "Jon Dow");
			assertKeyword(response, "Jane Smith");
			assertKeyword(response, "George Jones the 3rd");

			text.input("json http://www.botlibre.com/script?file&id=14026381");
			response = waitForOutput(output);
			assertKeyword(response, "Jon Dow");
			assertKeyword(response, "Jane Smith");
			assertKeyword(response, "George Jones the 3rd");			
			
		} finally {
			bot.shutdown();
		}
	}
}

