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

import java.net.URL;

import junit.framework.TestCase;

import org.botlibre.Bot;
import org.botlibre.api.sense.Sense;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.http.Http;

/**
 * Test loading 100 words from Wiktionary sense.
 */

public class TestWiktionary extends TestCase {
	
	/**
	 * Test loading 100 words from Wiktionary.
	 */
	public static void test100Words() throws Exception {
		DatabaseMemory.TEST = true;
		DatabaseMemory.RECREATE_DATABASE = true;
		Bot.DEFAULT_DEBUG_LEVEL = Bot.FINE;
		Bot bot = Bot.createInstance();
		Sense sense = bot.awareness().getSense(Http.class.getName());
		sense.input(new URL("http://en.wiktionary.org/wiki/Category:100_English_basic_words"));
		bot.shutdown();
	}	

}

