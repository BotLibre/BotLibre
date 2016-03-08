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
import org.botlibre.self.Self4Compiler;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Test language processing.
 */

public class TestAIMLChatLog extends TestAIML {

	@BeforeClass
	public static void setup() {
		reset();
		new Bootstrap().bootstrapSystem(bot, false);
		Bot bot = Bot.createInstance();
		try {
			URL url = TestAIML.class.getResource("alice.aiml");
			File file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFileAsLog(file, "", true);
			
			url = TestAIML.class.getResource("date.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFileAsLog(file, "", true);

			url = TestAIML.class.getResource("stack.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFileAsLog(file, "", true);

			url = TestAIML.class.getResource("test.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFileAsLog(file, "", true);

			url = TestAIML.class.getResource("pickup.aiml");
			file = new File(url.toURI());
			bot.mind().getThought(Language.class).loadAIMLFileAsLog(file, "", true);

			//SelfCompiler.setCompiler(new Self4Compiler());
			url = TestAIML.class.getResource("alice.res");
			file = new File(url.toURI());
			bot.awareness().getSense(TextEntry.class).loadChatFile(file, "Response List", "", false, true);
			Utils.sleep(5000);
			//SelfCompiler.setCompiler(new SelfCompiler());
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

