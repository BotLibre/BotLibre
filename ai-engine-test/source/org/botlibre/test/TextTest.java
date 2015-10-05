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

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.botlibre.Bot;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sense.text.TextEntry;

/**
 * Test text processing.
 */

public abstract class TextTest {
	protected static Bot bot;
	
	public static void fail(String message) {
		Assert.fail(message);
	}
	
	/**
	 * Allow text output to be recorded.
	 */
	public static List<String> registerForOutput(TextEntry text) {
		final List<String> output = new ArrayList<String>();
		text.setWriter(new Writer() {
			public void write(char[] text, int start, int end) {
				output.add(new String(text, start, end));
				synchronized (output) {
					output.notify();
				}
			}
			public void flush() {}
			public void close() {}
		});
		return output;
	}
	
	/**
	 * Wait for the next text output.
	 */
	public static String waitForOutput(List<String> output) {
		if (!output.isEmpty()) {
			String response = output.get(output.size() - 1);
			output.clear();
			return response;
		}
		synchronized (output) {
			try {
				output.wait(10000);
			} catch (InterruptedException exception) {}
		}
		if (output.isEmpty()) {
			fail("No response.");
		}
		String response = output.get(output.size() - 1);
		output.clear();
		return response;
	}
	
	/**
	 * Bootstrap.
	 */
	public static void bootstrap() {
		reset();
		new Bootstrap().bootstrapSystem(bot, true);
	}
	
	/**
	 * Reset.
	 */
	public static void reset() {
		DatabaseMemory.TEST = true;
		DatabaseMemory.RECREATE_DATABASE = true;
		//Bot.DEFAULT_DEBUG_LEVEL = Bot.FINE;
		bot = Bot.createInstance();
		DatabaseMemory.RECREATE_DATABASE = false;
	}
	
	/**
	 * Load Wiktionary database.
	 */
	public void import1000Words() {
		Bot bot = getBot();
		bot.memory().importMemory("wiktionary1000");
		bot.shutdown();
		TextTest.bot = Bot.createInstance();
	}
	
	public void assertKeyword(String response, String keyword) {
		if (!response.toLowerCase().contains(keyword.toLowerCase())) {
			fail("Should contain: " + keyword + " : " + response);
		}
	}
	
	public void assertKnown(String response) {
		response = response.toLowerCase();
		if (!(response.contains("i understand") || response.contains("okay")
				|| response.contains("i will remember") || response.contains("i believe"))) {
			fail("Should understand: " + response);
		}
	}
	
	public void assertUnknown(String response) {
		response = response.toLowerCase();
		if (!(response.contains("unknown") || response.contains("not sure")
				|| response.contains("no idea") || response.contains("i don't know"))) {
			fail("Should not know the answer: " + response);
		}
	}
	
	public void assertUncertain(String response) {
		response = response.toLowerCase();
		if (!(response.contains("perhaps") || response.contains("not certain") || response.contains("pretty sure"))) {
			fail("Should not know the answer: " + response);
		}
	}
	
	public void assertTrue(String response) {
		response = response.toLowerCase();
		if (!(response.contains("yes") || response.contains("correct") || response.contains("true")
					|| response.contains("that's right"))) {
			fail("Should know answer is true: " + response);
		}
	}
	
	public void assertFalse(String response) {
		response = response.toLowerCase();
		if (!response.contains("no") && (!response.contains("incorrect")) && (!response.contains("false"))) {
			fail("Should know answer is false: " + response);
		}
	}
	
	/**
	 * Shutdown the instance.
	 */
	public static void shutdown() {
		getBot().shutdown();
	}
	
	public static Bot getBot() {
		return bot;
	}

	public static void checkResponse(String response, String... expected) {
		boolean found = false;
		for (String match : expected) {
			if (response.equals(match)) {
				found = true;
				break;
			}
		}
		if (!found) {
			fail("Incorrect response: '" + response + "' was expecting one of " + Arrays.asList(expected));			
		}
	}

}

