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

import java.util.List;

import org.botlibre.Bot;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test the language cache.
 */

public class TestLanguageCache extends TestLanguage {

	@BeforeClass
	public static void setup() {
		bootstrap();
		
		DatabaseMemory.RECREATE_DATABASE = true;
		Bot.systemCache = Bot.createInstance(Bot.CONFIG_FILE, "cache", false);
		DatabaseMemory.RECREATE_DATABASE = false;
		
		TextEntry text = Bot.systemCache.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		text.input("sky blue red dog barks all night the cat green grass tall like very loves good Dirt dirt");
		waitForOutput(output);
		Utils.sleep(10000);
		
		Bot bot = Bot.createInstance();
		
		text = bot.awareness().getSense(TextEntry.class);
		output = registerForOutput(text);
		text.input("sky blue red dog barks all night the cat green grass tall like very loves good Dirt dirt");
		waitForOutput(output);
		Utils.sleep(10000);
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

