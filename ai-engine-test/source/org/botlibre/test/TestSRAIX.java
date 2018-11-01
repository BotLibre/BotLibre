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
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test AIML2 SRAIX support.
 */

public class TestSRAIX extends TextTest {
	public static String applicationId = "";
	
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
		
		Utils.sleep(5000);
		
		bot.shutdown();
		//RemoteService.SERVER = "http://localhost:9080/botlibre";
	}

	@org.junit.Test
	public void testSRAIX() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("sraixkey 2+2");
		String response = waitForOutput(output);
		if (!response.equals("4") && !response.equals("Uh, Four.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixchomsky hello");
		response = waitForOutput(output);
		if (response.indexOf("Hello") == -1 && response.indexOf("Hi") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixchomsky2 hello");
		response = waitForOutput(output);
		if (response.indexOf("Hello") == -1 && response.indexOf("Hi") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("pannous 2+2");
		response = waitForOutput(output);
		if (!response.equals("4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixbrainbot " + applicationId + " 2+2");
		response = waitForOutput(output);
		if (!response.equals("2 + 2 = 4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixbrainbot2 " + applicationId + " 2+2");
		response = waitForOutput(output);
		if (!response.equals("2 + 2 = 4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixbrainbot3 " + applicationId + " 2+2");
		response = waitForOutput(output);
		if (!response.equals("2 + 2 = 4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixbrainbot4 2+2");
		response = waitForOutput(output);
		if (!response.equals("Error")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixbrainbot5 " + applicationId + " 2+2");
		response = waitForOutput(output);
		if (!response.equals("2 + 2 = 4")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraix what is love");
		response = waitForOutput(output);
		if (response.indexOf("Sometimes I think love is just a biological urge. Other times LOVE seems like a spiritual quality. Love, unlike energy or matter, seems limitless.") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixlimit what is love");
		response = waitForOutput(output);
		if (!response.equals("Sometimes I think love is just a biological urge.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("sraixforge hello");
		response = waitForOutput(output);
		if (response.indexOf("Hello") == -1 && response.indexOf("Hi") == -1) {
			fail("Incorrect response: " + response);
		}
	}

	@org.junit.Test
	public void testServices() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("wikidata Ottawa");
		String response = waitForOutput(output);
		if (response.indexOf("capital city of Canada") == -1) {
			fail("Incorrect response: " + response);
		}
		
		text.input("wikidatacountry Ottawa");
		response = waitForOutput(output);
		if (!response.equals("Canada")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("freebase Toronto");
		response = waitForOutput(output);
		if (response.indexOf("Toronto is the most populous city in Canada and the provincial capital of Ontario.") == -1) {
			//fail("Incorrect response: " + response);
		}
		
		text.input("wikidatacountry Toronto");
		response = waitForOutput(output);
		if (!response.equals("Canada")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("wiktionary water");
		response = waitForOutput(output);
		if (!response.equals("A substance found at room temperature and pressure as a clear liquid; it is present naturally as rain, and found in rivers, lakes and seas; its solid form is ice and its gaseous form is steam.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("wikidata Toronto hint country");
		response = waitForOutput(output);
		if (!response.equals("Canada")) {
			fail("Incorrect response: " + response);
		}
	}


	@org.junit.Test
	public void testHttp() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);

		text.input("xml http://botlibre.com/rest/api/form-chat?instance=165&message=ping&application=" + applicationId + " message");
		String response = waitForOutput(output);
		checkResponse(response, "Pong");

		text.input("fetch html https://botlibre.com head/meta[2]/@content");
		response = waitForOutput(output);
		checkResponse(response, "Paphus Solutions Inc.");

		text.input("json http://botlibre.com/rest/json/form-chat?instance=165&message=ping&application=" + applicationId + " message");
		response = waitForOutput(output);
		checkResponse(response, "Pong");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

