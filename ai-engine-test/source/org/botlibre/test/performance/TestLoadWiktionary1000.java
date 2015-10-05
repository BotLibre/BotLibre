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
package org.botlibre.test.performance;

import java.net.URL;

import org.botlibre.Bot;
import org.botlibre.api.sense.Sense;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.http.Http;

/**
 * Loading all words from English Wiktionary into main database.
 */

public class TestLoadWiktionary1000 {

	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			testLoadWords();
			long time = System.currentTimeMillis() - start;
			System.out.println("Total Time:" + time);
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}
	
	/**
	 * Test loading 1000 words from Wiktionary.
	 */
	public static void testLoadWords() throws Exception {
		Bot bot = Bot.createInstance();
		try {
			bot.memory().createMemory("wiktionary1000");
		} catch (Exception alreadyExists) {}
		bot.shutdown();
		DatabaseMemory.DATABASE_URL = "jdbc:postgresql:wiktionary1000";
		DatabaseMemory.DATABASE_TEST_URL = "jdbc:postgresql:wiktionary1000";		
		DatabaseMemory.RECREATE_DATABASE = true;
		bot = Bot.createInstance();
		Sense sense = bot.awareness().getSense(Http.class.getName());
		sense.input(new URL("http://en.wiktionary.org/wiki/Category:1000_English_basic_words"));
		bot.shutdown();
	}	

}

