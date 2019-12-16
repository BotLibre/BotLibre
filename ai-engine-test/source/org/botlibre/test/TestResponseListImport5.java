/******************************************************************************
 *
 *  Copyright 2019 Paphus Solutions Inc.
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

import org.botlibre.Bot;
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestResponseListImport5 extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("test5.res");
		File file = new File(url.toURI());
		ResponseListParser.parser().loadChatFile(file, "Response List", "", false, true, bot);
		List<String> output = registerForOutput(text);
		text.input("this is a very complicated sentence the dog barks all night this is a good reply to that");
		waitForOutput(output);
		Utils.sleep(5000);
		bot.shutdown();
	}
	
	/**
	 * Test response required work.
	 */
	@org.junit.Test
	public void testRequired() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("do you like hot dogs?");
		String response = waitForOutput(output);
		checkResponse(response, "Yes, I like hot dogs.");
		
		text.input("do you like dogs?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I like dogs.");
		
		text.input("do you like red dogs?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I like dogs.");
		
		text.input("do you like hamburgers?");
		response = waitForOutput(output);
		checkResponse(response, "No, I do not like hamburgers.");
		
		text.input("do you like hot hamburgers?");
		response = waitForOutput(output);
		checkResponse(response, "No, I do not like hamburgers.");
		
		text.input("do you have a cat or a dog?");
		response = waitForOutput(output);
		checkResponse(response, "I have a dog.");
		
		text.input("do you prefer hot dogs or hamburgers?");
		response = waitForOutput(output);
		checkResponse(response, "I prefer hot dogs.");
		
		text.input("where can i buy a hot dog?");
		response = waitForOutput(output);
		checkResponse(response, "You can buy one at the stand around the corner.");
		
		text.input("where can i buy a hamburger?");
		response = waitForOutput(output);
		checkResponse(response, "You can buy one at the stand around the corner.");
		
		text.input("did you watch the football game?");
		response = waitForOutput(output);
		checkResponse(response, "No, I don't watch sports.");
		
		text.input("did you watch the soccer match?");
		response = waitForOutput(output);
		checkResponse(response, "No, I don't watch sports.");
		
		text.input("did you watch the soccer game?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("did you watch the football match?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("did you play the football game?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("did you watch the solar eclipse?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("you feeling okay?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("are you okay?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am okay.");
		
		text.input("are you ok?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am okay.");
		
		text.input("are you a hamburger?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("r u ok?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am okay.");
		
		text.input("r u okay?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am okay.");

		text.input("how do you change a car tire?");
		response = waitForOutput(output);
		checkResponse(response, "Hm, I'm not sure!");
		
		text.input("how do you change a car wheel?");
		response = waitForOutput(output);
		checkResponse(response, "Hm, I'm not sure!");
		
		text.input("how do you change a vehicle wheel?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("how do you change a  hamburger?");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("know how you change a car tire?");
		response = waitForOutput(output);
		checkResponse(response, "Hm, I'm not sure!");
		
		text.input("i hope you are feeling ok");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am feeling okay.");
		
		text.input("i hope u r feeling ok");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am feeling okay.");
		
		text.input("i hope u r feeling okay");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am feeling okay.");
		
		text.input("i hope you are feeling potato");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		bot.shutdown();
	}
	
	/**
	 * Test response required patterns work.
	 */
	@org.junit.Test
	public void testPatternRequired() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("where do you live");
		String response = waitForOutput(output);
		checkResponse(response, "Okay.");
		
		text.input("where do you liv");
		response = waitForOutput(output);
		checkResponse(response, "Okay.");
		
		text.input("tell me about yourself");
		response = waitForOutput(output);
		checkResponse(response, "I am a test.");
		
		text.input("tell me what you are");
		response = waitForOutput(output);
		checkResponse(response, "I am a test.");
		
		text.input("tell me the time");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("i will fly the space shuttle to outer space");
		response = waitForOutput(output);
		checkResponse(response, "That sounds cool.");
		
		text.input("i will blast the rocket ship to mars");
		response = waitForOutput(output);
		checkResponse(response, "That sounds cool.");
		
		text.input("i will fly the space shuttle to the asteroid belt");
		response = waitForOutput(output);
		checkResponse(response, "That sounds cool.");
		
		text.input("i will fly the potato to the asteroid belt");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("i will fly the rocket ship into the potato");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("i will fly the potato to the potato sack");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("i like you");
		response = waitForOutput(output);
		checkResponse(response, "I like you too!");
		
		text.input("i really like you");
		response = waitForOutput(output);
		checkResponse(response, "I like you too!");
		
		text.input("i think i like you");
		response = waitForOutput(output);
		checkResponse(response, "I like you too!");
		
		text.input("i really think like you");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		text.input("i don't like you");
		response = waitForOutput(output);
		checkResponse(response, "I don't understand.");
		
		bot.shutdown();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}
