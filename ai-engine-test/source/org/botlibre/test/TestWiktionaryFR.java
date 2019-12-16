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
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.BeforeClass;

/**
 * Test importing words from fr.wiktionary.
 */

public class TestWiktionaryFR extends TextTest {
	
	public static int SLEEP = 5000;

	@BeforeClass
	public static void setup() {
		bootstrap("fr");
	}

	@org.junit.Test
	public void testWords() {
		Bot bot = Bot.createInstance();
		//bot.setDebugLevel(Level.FINER);
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		language.setLanguage("fr");
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);

		text.input("définir ciel");
		String response = waitForOutput(output);
		if (!response.equals("Espace immense dans lequel se meuvent tous les astres.")) {
			fail("Incorrect: " + response);
		}
		
		text.input("définir bleu");
		response = waitForOutput(output);
		if (!response.equals("De la couleur du ciel en plein jour quand il est dégagé.")) {
			fail("Incorrect: " + response);
		}
		
		text.input("définir aime");
		response = waitForOutput(output);
		if (!response.equals("Première personne du singulier de l’indicatif présent de aimer.")) {
			//fail("Incorrect: " + response);
		}

		text.input("est ciel un objet?");
		response = waitForOutput(output);
		assertVrai(response);

		text.input("est bleu un objet?");
		response = waitForOutput(output);
		assertVrai(response);
		
		text.input("le ciel est bleu?");
		response = waitForOutput(output);
		assertUnknownFR(response);
		
		text.input("le ciel est bleu");
		response = waitForOutput(output);
		assertKnownFR(response);
		
		text.input("le ciel est bleu?");
		response = waitForOutput(output);
		assertVrai(response);
		
		text.input("J'aime bleu");
		response = waitForOutput(output);
		assertKnownFR(response);
		
		text.input("Est-ce que j'aime bleu?");
		response = waitForOutput(output);
		assertVrai(response);
		
		text.input("Qu'est-ce que j'aime?");
		response = waitForOutput(output);
		assertKeyword(response, "bleu");
		
		text.input("Quel est le temps?");
		response = waitForOutput(output);
		if (response.equals("Quel est le temps?")) {
			fail("did not understand time: " + response);
		}
		
		text.input("Quelle est la date?");
		response = waitForOutput(output);
		if (response.equals("Quelle est la date?")) {
			fail("did not understand date: " + response);
		}


		bot.shutdown();
	}
}

