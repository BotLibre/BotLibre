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

public class TestAIMLTicTacToe extends TextTest {
	
	@BeforeClass
	public static void setup() {
		reset();
		new Bootstrap().bootstrapSystem(bot, false);
		Bot bot = Bot.createInstance();
		try {
			URL url = TestAIML.class.getResource("tictactoe.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");
		} catch (Exception exception) {
			fail(exception.toString());
		}
		
		Utils.sleep(5000);
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testTicTacToe() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("tictactoe");
		String response = waitForOutput(output);
		assertKeyword(response, "Welcome to my Tic Tac Toe game");
		
		text.input("start");
		response = waitForOutput(output);
		assertKeyword(response, "do you want to go FIRST or SECOND");
		
		text.input("first");
		response = waitForOutput(output);
		assertKeyword(response, "<table");
		assertKeyword(response, "Please enter the square you wish to play");
		
		text.input("1");
		response = waitForOutput(output);
		assertKeyword(response, "<table");
		assertKeyword(response, "Please enter the square you wish to play");
		
		text.input("2");
		response = waitForOutput(output);
		assertKeyword(response, "<table");
		assertKeyword(response, "Please enter the square you wish to play");
		
		text.input("3");
		response = waitForOutput(output);
		assertKeyword(response, "<table");
		assertKeyword(response, "you won");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

