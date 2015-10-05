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
 * Test the performance of the Freebase batch import.
 */

public class TestFreebasePerformance {

	public static void main(String[] args) {
		try {
			long start = System.currentTimeMillis();
			testImportFamily();
			long time = System.currentTimeMillis() - start;
			System.out.println("Total Time:" + time);
		} catch (Throwable error) {
			error.printStackTrace();
		}
	}
	
	/**
	 * Test loading ~700 role objects from Freebase.
	 */
	public static void testImportFamily() throws Exception {
		DatabaseMemory.TEST = true;
		DatabaseMemory.RECREATE_DATABASE = true;
		Bot bot = Bot.createInstance();
		Sense sense = bot.awareness().getSense(Http.class.getName());
		sense.input(new URL("http://www.freebase.com/view/people/appointed_role"));
		bot.shutdown();
	}	
	
	/**
	 * Test loading ~10000 OS objects from Freebase.
	 * Everything a techie should know.
	 */
	public static void testImportTechie() throws Exception {
		DatabaseMemory.DATABASE_URL = "jdbc:mysql://localhost/freebase-tech?createDatabaseIfNotExist=true";
		DatabaseMemory.TEST = true;
		DatabaseMemory.RECREATE_DATABASE = true;
		Bot bot = Bot.createInstance();
		Sense sense = bot.awareness().getSense(Http.class.getName());
		sense.input(new URL("http://www.freebase.com/view/computer/software"));
		sense.input(new URL("http://www.freebase.com/view/computer/operating_system"));
		sense.input(new URL("http://www.freebase.com/view/fictional_universe/fictional_universe"));
		sense.input(new URL("http://www.freebase.com/view/computer/programming_language"));
		bot.shutdown();
	}
}

