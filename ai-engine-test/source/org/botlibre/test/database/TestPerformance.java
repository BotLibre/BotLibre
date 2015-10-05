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
package org.botlibre.test.database;

import java.net.URL;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.sense.Sense;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.http.Http;

/**
 * Test the performance of the database memory.
 */

public class TestPerformance {

	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			test1000Words();
			long time = System.currentTimeMillis() - start;
			System.out.flush();
			System.out.println("Total Time:" + time);
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}
	
	/**
	 * Test loading 1000 words from Wiktionary.
	 */
	public static void test1000Words() throws Exception {
		DatabaseMemory.DATABASE_URL = "jdbc:derby:top1000words;create=true";
		DatabaseMemory.TEST = true;
		DatabaseMemory.RECREATE_DATABASE = true;
		Bot bot = Bot.createInstance();
		bot.setDebugLevel(Level.OFF);
		Sense sense = bot.awareness().getSense(Http.class.getName());
		sense.input(new URL("http://en.wiktionary.org/wiki/Category:1000_English_basic_words"));
		bot.shutdown();
	}	

}

