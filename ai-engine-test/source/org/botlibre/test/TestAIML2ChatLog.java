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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Test language processing.
 */

public class TestAIML2ChatLog extends TestAIML2 {
	
	public boolean isChatLog() {
		return true;
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
			bot.mind().getThought(Language.class).loadAIMLFileAsLog(file, "", true);
		} catch (Exception exception) {
			fail(exception.toString());
		}
		
		Utils.sleep(5000);
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

