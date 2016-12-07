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

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.junit.BeforeClass;

/**
 * Test Self scripting and API.
 */

public class TestISpy extends TextTest {
	
	public static String applicationId = "8580253908277089542";

	@BeforeClass
	public static void setup() {
		bootstrap();
		
		Network network = bot.memory().newMemory();
		Vertex language = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
		Vertex script = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("ispy.self"), "", false, network);
		language.setRelationship(Primitive.STATE, script);
		network.save();
	}
	
	/**
	 * Test the I Spy game.
	 */
	@org.junit.Test
	public void testISpy() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Level.FINER);
		try {
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("i spy");
			String response = waitForOutput(output);
			checkResponse(response, "Would you like to play I Spy with me?");

			text.input("y");
			response = waitForOutput(output);
			checkResponse(response, "Would you like to spy or guess?");

			text.input("guess");
			response = waitForOutput(output);
			assertKeyword(response, "I spy with my little eye something that is");

			text.input("a car");
			response = waitForOutput(output);
			assertKeyword(response, "No");

			text.input("a plane");
			response = waitForOutput(output);
			assertKeyword(response, "No");

			text.input("give up");
			response = waitForOutput(output);
			assertKeyword(response, "I was thinking of");
			assertKeyword(response, "Would you like to play again?");

			text.input("yes");
			response = waitForOutput(output);
			checkResponse(response, "Would you like to spy or guess?");

			text.input("spy");
			response = waitForOutput(output);
			checkResponse(response, "Okay, start by saying 'I spy something that is...'");

			text.input("I spy something that is blue");
			response = waitForOutput(output);
			
			text.input("no");
			response = waitForOutput(output);
			checkResponse(response, "I give up, what is it?");
			
			text.input("water");
			response = waitForOutput(output);
			checkResponse(response, "Would you like to play again?");
			
			text.input("no");
			response = waitForOutput(output);
			checkResponse(response, "Okay, thank you for playing with me.");
			
		} finally {
			bot.shutdown();
		}
	}
}

