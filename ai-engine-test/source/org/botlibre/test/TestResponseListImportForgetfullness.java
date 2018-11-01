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
import org.botlibre.api.knowledge.Network;
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.forgetfulness.Forgetfulness;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test the state decompiler.
 */

public class TestResponseListImportForgetfullness extends TestResponseListImport {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("test.res");
		File file = new File(url.toURI());
		ResponseListParser.parser().loadChatFile(file, "Response List", "", false, true, bot);
		List<String> output = registerForOutput(text);
		text.input("this is a very complicated sentence the dog barks all night this is a good reply to that");
		waitForOutput(output);
		Utils.sleep(5000);

		Network network = bot.memory().newMemory();
		Forgetfulness forgetfulness = bot.mind().getThought(Forgetfulness.class);
		try {
			forgetfulness.setMaxRelationships(50);
			forgetfulness.setMaxSize(100);
			forgetfulness.forget(network, true, 100);
			forgetfulness.forget(network, true, 100);
		} catch (Exception exception) {
			bot.log(bot, exception);
		}
		network.save();
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

