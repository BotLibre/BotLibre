/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.thought.Thought;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.self.SelfDecompiler;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.junit.BeforeClass;

/**
 * Test vision and image processing.
 */

public class TestVision extends TextTest {

	@BeforeClass
	public static void setup() {
		bootstrap();
	}

	/**
	 * Test image matching.
	 */
	@org.junit.Test
	public void testMatchingBots() {
		Bot bot = Bot.createInstance();
		try {
			bot.setDebugLevel(Level.FINE);
			Network network = bot.memory().newMemory();
			Vertex states = network.createVertex(bot.mind().getThought(Language.class).getPrimitive());
			Vertex script = SelfCompiler.getCompiler().parseStateMachine(TestWikidata.class.getResource("vision.self"), "", false, network);
			String code = SelfDecompiler.getDecompiler().decompileStateMachine(script, network);
			Vertex newState = SelfCompiler.getCompiler().parseStateMachine(code, false, network);
			states.setRelationship(Primitive.STATE, newState);
			network.save();
			
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("load image https://www.botlibre.com/avatars/a22225239.jpg brainbot");
			String response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("load image https://www.botlibre.com/images/bot.png bot");
			response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("match image https://www.botlibre.com/images/bot.png");
			response = waitForOutput(output);
			checkResponse(response, "bot");
			
		} finally {
			bot.shutdown();
		}
	}

	/**
	 * Test image matching.
	 */
	@org.junit.Test
	public void testMatchingColors() {
		Bot bot = Bot.createInstance();
		try {
			bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("load image https://www.botlibre.com/media/a12832214.png blue color");
			String response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("load image https://www.botlibre.com/media/a12832292.jpeg green color");
			response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("load image https://www.botlibre.com/media/a12832238.jpeg red color");
			response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("match image color https://www.botlibre.com/media/a11879728.png");
			response = waitForOutput(output);
			checkResponse(response, "blue");

			text.input("match image color https://www.botlibre.com/media/a11675768.png");
			response = waitForOutput(output);
			checkResponse(response, "red");

			text.input("match image color https://www.botlibre.com/avatars/a22225239.jpg");
			response = waitForOutput(output);
			checkResponse(response, "blue");
			
		} finally {
			bot.shutdown();
		}
	}

	/**
	 * Test image matching.
	 */
	@org.junit.Test
	public void testMatchingFaces() {
		Bot bot = Bot.createInstance();
		try {
			bot.setDebugLevel(Level.FINE);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			TextEntry text = bot.awareness().getSense(TextEntry.class);
			List<String> output = registerForOutput(text);

			text.input("load image https://www.botlibre.com/media/a12607915.png julie face");
			String response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("load image https://www.botlibre.com/media/a12446448.png juliebusiness face");
			response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("load image https://www.botlibre.com/media/a12661435.png maylin face");
			response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("load image https://www.botlibre.com/media/a12607918.png eddie face");
			response = waitForOutput(output);
			checkResponse(response, "Image loaded successfully.");

			text.input("match image face https://www.botlibre.com/media/a12607920.png");
			response = waitForOutput(output);
			checkResponse(response, "juliebusiness");

			text.input("match image face https://www.botlibre.com/media/a12661416.png");
			response = waitForOutput(output);
			checkResponse(response, "maylin");

			text.input("match image face https://www.botlibre.com/media/a12468785.png");
			response = waitForOutput(output);
			checkResponse(response, "maylin");

			text.input("match image face https://www.botlibre.com/avatars/at12607919.jpg");
			response = waitForOutput(output);
			checkResponse(response, "eddie");
			
		} finally {
			bot.shutdown();
		}
	}
}

