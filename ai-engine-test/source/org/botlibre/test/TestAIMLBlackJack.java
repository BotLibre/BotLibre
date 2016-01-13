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

import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test AIML2 support.
 */

public class TestAIMLBlackJack extends TextTest {
	
	@BeforeClass
	public static void setup() {
		reset();
		new Bootstrap().bootstrapSystem(bot, false);
		Bot bot = Bot.createInstance();
		try {
			URL url = TestAIML.class.getResource("blackjack.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");
		} catch (Exception exception) {
			fail(exception.toString());
		}
		
		Utils.sleep(5000);
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testBlackJack() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("blackjack");
		String response = waitForOutput(output);
		assertKeyword(response, "Welcome to my Blackjack game");
		
		text.input("d");
		response = waitForOutput(output);
		assertKeyword(response, "how many coins do you wish to bet");
		
		text.input("10");
		response = waitForOutput(output);
		assertKeyword(response, "Type H to hit, S to stand or D to double down");
		
		text.input("s");
		response = waitForOutput(output);
		assertKeyword(response, "Type D to deal");
		
		text.input("d");
		response = waitForOutput(output);
		assertKeyword(response, "how many coins do you wish to bet");
		
		text.input("9");
		response = waitForOutput(output);
		assertKeyword(response, "Type H to hit, S to stand or D to double down");
		
		text.input("s");
		response = waitForOutput(output);
		assertKeyword(response, "Type D to deal");
		
		text.input("d");
		response = waitForOutput(output);
		assertKeyword(response, "how many coins do you wish to bet");
		
		text.input("xxx");
		response = waitForOutput(output);
		assertKeyword(response, "You cannot bet \"xxx coins\"");
		
		text.input("quit");
		response = waitForOutput(output);
		assertKeyword(response, "Ok. What would you like to do now?");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

