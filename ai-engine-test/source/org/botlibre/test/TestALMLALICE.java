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
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.consciousness.Consciousness;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.junit.BeforeClass;

/**
 * Test loading ALICE AIML.
 */

public class TestALMLALICE extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		long start = System.currentTimeMillis();
		new Bootstrap().bootstrapSystem(bot, false);

		URL url = TestAIML.class.getResource("alice_merged.aiml");
		File file = new File(url.toURI());
		bot.mind().getThought(Language.class).loadAIMLFile(file, true, false, "");

		url = TestAIML.class.getResource("alice.res");
		file = new File(url.toURI());
		bot.awareness().getSense(TextEntry.class).loadChatFile(file, "Response List", "", false, true);
		
		bot.shutdown();
		long end = System.currentTimeMillis();
		System.out.println("Bootstrap time: " + (end - start));
		// 2015-11-23 - 70658, 69952, 68645
		// function bytecode - 69002
		
		Bot bot = Bot.createInstance();
		System.out.println("Memory size: " + bot.memory().getLongTermMemory().size());
		// 2015-11-23 - 38409
		// function bytecode - 36905
	}

	@org.junit.Test
	public void testPerformance() {
		Bot bot = Bot.createInstance();
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

		text.input("where is alice based");
		response = waitForOutput(output);
		if (!response.equals("It is in Oakland, California.")) {
			fail("Incorrect response: " + response);
		}
		
		text.input("Who, is ALICE Toklas...?");
		response = waitForOutput(output);
		if (!response.equals("She was the partner of Gertrude Stein, and inventor of the pot brownie.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("Is HAL smarter than you?");
		response = waitForOutput(output);
		if (!response.equals("The ALICE series is the most intelligent chat robot software.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("call me alice");
		response = waitForOutput(output);
		if (!response.equals("My name is ALICE too!")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("WHERE IS THE DOCUMENTATION");
		response = waitForOutput(output);
		if (!response.equals("Try visiting <a href=\"http://www.botbots.com\" target=\"_new\">Botbots.com</a> or <a href=\"http://www.alicebot.org\" target=\"_new\">Alicebot.org</a>.")) {
			fail("Incorrect response: " + response);			
		}
		
		text.input("WHAT DOES A L I C E stand for?");
		response = waitForOutput(output);
		if (!response.equals("ALICE = Artificial Linguistic Internet Computer Entity")) {
			fail("Incorrect response: " + response);			
		}
				
		long end = System.currentTimeMillis();
		System.out.println("Chat time: " + (end - start));
		// 2015-11-18 -- 4161, 2715, 2673
		// function bytecode - 2483
		
		//profiler.dumpResults();
	}
}

