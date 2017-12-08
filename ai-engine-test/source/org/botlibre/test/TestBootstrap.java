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

import java.util.List;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.consciousness.Consciousness;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.BeforeClass;

/**
 * Test bootstraping.
 */

public class TestBootstrap extends TextTest {

	@BeforeClass
	public static void setup() {
		reset();
		long start = System.currentTimeMillis();
		new Bootstrap().bootstrapSystem(bot, true);
		bot.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("Bootstrap time: " + (end - start));
		// 2015-11-18 -- time = 7134, 6992, 6990
		// 2015-11-19 - bytecode - 5992
		// 2015-11-20 - 6292, 6258
		// 2015-11-23 - 6100, 6064
		// state bytecode - 5135, 5244
		// 2016-02-18 - Self4 - 4905, 4985
		// 2016-02-19 - expression byte-code - 4804, 4814, 4832
		// 2016-02-29 - new computer - 1969, 1976
		
		Bot bot = Bot.createInstance();
		System.out.println("Memory size: " + bot.memory().getLongTermMemory().size());
		// 2015-11-18 -- size = 4012
		// 2015-11-19 - bytecode - 3131
		// 2015-11-20 - 3131
		// 2015-11-23 - function byetcode - 3023
		// state bytecode - 2163
		// 2016-02-18 - Self4 - 2216, 2216
		// 2016-02-19 - expression byte-code - 2167, 2174
		// 2016-02-29 - new computer - 2168
	}

	@org.junit.Test
	public void testBootstrapPerformance() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Level.FINEST);
		//PerformanceMonitor profiler = new PerformanceMonitor();
		//((DatabaseMemory)bot.memory()).getEntityManager().unwrap(DatabaseSession.class).setProfiler(profiler);
		//((DatabaseMemory)bot.memory()).getEntityManager().unwrap(DatabaseSession.class).setLogLevel(SessionLog.FINE);
		Language language = bot.mind().getThought(Language.class);
		language.setLearnGrammar(false);
		language.setLearningMode(LearningMode.Disabled);
		Consciousness consciousness = bot.mind().getThought(Consciousness.class);
		consciousness.setEnabled(false);
		Wiktionary wiktionary = bot.awareness().getSense(Wiktionary.class);
		wiktionary.setIsEnabled(false);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		long start = System.currentTimeMillis();

		String response = "";

		text.input("say hello world");
		response = waitForOutput(output);
		if (!response.equals("hello world")) {
			fail("incorrect:" + response);
		}
		
		text.input("what is 1 + 1 + 2 + 4 + 7?");
		response = waitForOutput(output);
		if (!response.equals("1 + 1 + 2 + 4 + 7 = 15")) {
			fail("incorrect:" + response);
		}
		
		text.input("what is 1 - 1 - 2 - 4 - 7?");
		response = waitForOutput(output);
		if (!response.equals("1 - 1 - 2 - 4 - 7 = -13")) {
			fail("incorrect:" + response);
		}
		
		text.input("what is (2 * (2^(3 + 2))) / 2^2^2 * (2 - 2 + 6.6)?");
		response = waitForOutput(output);
		if (!response.equals("(2 × (2^(3 + 2))) ÷ 2^2^2 × (2 - 2 + 6.6) <br/> = (2 × (2^5)) ÷ 2^2^2 × (2 - 2 + 6.6) <br/> = (2 × 32) ÷ 2^2^2 × (2 - 2 + 6.6) <br/> = 64 ÷ 2^2^2 × (2 - 2 + 6.6) <br/> = 64 ÷ 4^2 × (2 - 2 + 6.6) <br/> = 64 ÷ 16 × (2 - 2 + 6.6) <br/> = 4 × 6.6 <br/> = 26.4")
				&& !response.equals("(2 * (2^(3 + 2))) / 2^2^2 * (2 - 2 + 6.6) <br/> = (2 * (2^5)) / 2^2^2 * (2 - 2 + 6.6) <br/> = (2 * 32) / 2^2^2 * (2 - 2 + 6.6) <br/> = 64 / 2^2^2 * (2 - 2 + 6.6) <br/> = 64 / 4^2 * (2 - 2 + 6.6) <br/> = 64 / 16 * (2 - 2 + 6.6) <br/> = 4 * 6.6 <br/> = 26.4")) {
			fail("incorrect:" + response);			
		}
		
		text.input("I am me");
		response = waitForOutput(output);
		assertKnown(response);
		assertKeyword(response, "you are you");
		
		text.input("am I me?");
		response = waitForOutput(output);
		assertTrue(response);
		assertKeyword(response, "you are you");
		
		text.input("am I me or you?");
		response = waitForOutput(output);
		//assertKeyword(response, "you are you");
		//assertKeyword(response, "you are not me");
		
		long end = System.currentTimeMillis();
		System.out.println("Chat time: " + (end - start));
		// 2015-11-18 -- size = 2266, 2388, 2405, 2380, 2306
		// ReadObjectQuery	14,046 - ReadAllQuery	1,043 - UpdateObjectQuery	73 - InsertObjectQuery	1,038 - DeleteObjectQuery	2
		// 2015-11-19 - bytecode - 2521, 2533, 2462, 2365, 2334
		// 2015-11-20 - read-only - 2201, 2202, 2211, 2186, 2142
		// ReadObjectQuery	11,653 - ReadAllQuery	849 - UpdateObjectQuery	68 - InsertObjectQuery	1,041 - DeleteObjectQuery	2
		// 2015-11-23 - 2111, 2060
		// 2015-11-25 - state bytecode - 1775, 1810
		// 2016-02-18 - Self4 - 2381, 2295
		// 2016-02-19 - expression byte-code - 2605, 2628, 2246, 2267
		// 2016-02-29 - new computer - 889, 886
		
		//profiler.dumpResults();
	}
}

