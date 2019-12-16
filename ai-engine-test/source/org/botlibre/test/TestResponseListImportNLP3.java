/******************************************************************************
 *
 *  Copyright 2019 Paphus Solutions Inc.
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
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class TestResponseListImportNLP3 extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("testnlp3.res");
		File file = new File(url.toURI());
		ResponseListParser.parser().loadChatFile(file, "Response List", "", false, true, bot);
		List<String> output = registerForOutput(text);
		text.input("this is a very complicated sentence the dog barks all night this is a good reply to that");
		waitForOutput(output);
		Utils.sleep(5000);
		bot.shutdown();
	}
	
	@org.junit.Test
	public void testResponses() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("what");
		String response = waitForOutput(output);
		checkResponse(response, "default");
		
		text.input("what is your age");
		response = waitForOutput(output);
		checkResponse(response, "age");
		
		text.input("what is your name");
		response = waitForOutput(output);
		checkResponse(response, "name");
		
		text.input("what is your gender");
		response = waitForOutput(output);
		checkResponse(response, "gender");
		
		text.input("what is your favorite color");
		response = waitForOutput(output);
		checkResponse(response, "color");
		
		text.input("what is your favorite food");
		response = waitForOutput(output);
		checkResponse(response, "food");
		
		text.input("blah blah");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		text.input("what color");
		response = waitForOutput(output);
		checkResponse(response, "color");
		
		text.input("what is your size");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		text.input("what age");
		response = waitForOutput(output);
		checkResponse(response, "age");
		
		text.input("your name");
		response = waitForOutput(output);
		checkResponse(response, "name");
		
		text.input("what gender");
		response = waitForOutput(output);
		checkResponse(response, "gender");
		
		text.input("favorite color");
		response = waitForOutput(output);
		checkResponse(response, "color");
		
		text.input("food");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		text.input("favorite food");
		response = waitForOutput(output);
		checkResponse(response, "food");
		
		text.input("what");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		text.input("your");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		text.input("where do you live");
		response = waitForOutput(output);
		checkResponse(response, "canada");
		
		text.input("where do you go to school");
		response = waitForOutput(output);
		checkResponse(response, "school");
		
		text.input("where did you grow up");
		response = waitForOutput(output);
		checkResponse(response, "here");
		
		text.input("go to school");
		response = waitForOutput(output);
		checkResponse(response, "school");
		
		text.input("grow up");
		response = waitForOutput(output);
		checkResponse(response, "here");
		
		text.input("where");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		bot.shutdown();
	}
	
	@org.junit.Test
	public void testIrreleventWordsResponses() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("what age is your uncles best friends dog 1 2 3 4");
		String response = waitForOutput(output);
		checkResponse(response, "default");

		text.input("this is a long irrelevant question about what my age is");
		response = waitForOutput(output);
		checkResponse(response, "default");
		
		bot.shutdown();
	}
	
	@org.junit.Test
	public void testTwoWordFragmentResponses() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("is your car black");
		String response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("your car is black");
		response = waitForOutput(output);
		checkResponse(response, "correct");
		
		text.input("hello, is your car black");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("hello, your car is black");
		response = waitForOutput(output);
		checkResponse(response, "correct");
		
		text.input("do you want a cheese burger");
		response = waitForOutput(output);
		checkResponse(response, "yes, i like cheese burgers");
		
		text.input("do you want a burger with cheese");
		response = waitForOutput(output);
		checkResponse(response, "yes, please add cheese to my burger");
		
		text.input("hi, do you want a cheese burger");
		response = waitForOutput(output);
		checkResponse(response, "yes, i like cheese burgers");
		
		text.input("hi, do you want a burger with cheese");
		response = waitForOutput(output);
		checkResponse(response, "yes, please add cheese to my burger");
		
		text.input("a cheese burger is what you want");
		response = waitForOutput(output);
		checkResponse(response, "yes, i like cheese burgers");
		
		text.input("a burger with cheese is what you want");
		response = waitForOutput(output);
		checkResponse(response, "yes, please add cheese to my burger");
		
		text.input("one two three four five");
		response = waitForOutput(output);
		checkResponse(response, "1 2 3 4 5");
		
		text.input("five four three two one");
		response = waitForOutput(output);
		checkResponse(response, "5 4 3 2 1");
		
		text.input("one two three");
		response = waitForOutput(output);
		checkResponse(response, "1 2 3 4 5");
		
		text.input("three two one");
		response = waitForOutput(output);
		checkResponse(response, "5 4 3 2 1");
		
		text.input("three four five");
		response = waitForOutput(output);
		checkResponse(response, "1 2 3 4 5");
		
		text.input("five four three");
		response = waitForOutput(output);
		checkResponse(response, "5 4 3 2 1");
		
		text.input("one two three four");
		response = waitForOutput(output);
		checkResponse(response, "1 2 3 4 5");
		
		text.input("five four three two");
		response = waitForOutput(output);
		checkResponse(response, "5 4 3 2 1");
		
		text.input("要好题问题");
		response = waitForOutput(output);
		checkResponse(response, "问题要");
		
		text.input("题问题要好");
		response = waitForOutput(output);
		checkResponse(response, "要问题");
		
		text.input("要好题问");
		response = waitForOutput(output);
		checkResponse(response, "问题要");
		
		text.input("题问题");
		response = waitForOutput(output);
		checkResponse(response, "要问题");
		
		bot.shutdown();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}
