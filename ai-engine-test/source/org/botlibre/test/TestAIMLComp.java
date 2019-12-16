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
 * Test AIML compatibility support.
 */

public class TestAIMLComp extends TextTest {
	
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
			URL url = TestAIML.class.getResource("test-aiml-comp.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");
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
		
		Utils.sleep(5000);
		
		bot.shutdown();
	}

	@org.junit.Test
	public void testAIML() {
		try {
			Bot bot = Bot.createInstance();
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			language.setAimlCompatibility(true);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);
			//bot.setDebugLevel(Level.FINER);
			
			text.input("test spaces TEST");
			String response = waitForOutput(output);
			//checkResponse(response, "Hello TEST world");
			
			text.input("test get default");
			response = waitForOutput(output);
			checkResponse(response, "Unknown");
			
			text.input("test map default");
			response = waitForOutput(output);
			checkResponse(response, "Unknown");
			
			text.input("set printstring");
			response = waitForOutput(output);
			checkResponse(response, "foo"); // bar?
			
			text.input("reverse 1 2 3 4 5");
			response = waitForOutput(output);
			checkResponse(response, "5 4 3 2 1");
			
			text.input("date 2019/09/25");
			response = waitForOutput(output);
			checkResponse(response, "year: 2019 month: 09 day: 25");
		} finally {
			bot.shutdown();
		}
	}


	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

