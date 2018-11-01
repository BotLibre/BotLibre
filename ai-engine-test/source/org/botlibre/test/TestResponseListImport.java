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
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.parsing.ResponseListParser;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.Utils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test language processing.
 */

public class TestResponseListImport extends TextTest {

	@BeforeClass
	public static void setup() throws Exception {
		reset();
		Bot bot = Bot.createInstance();
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		URL url = TestAIML.class.getResource("test.res");
		File file = new File(url.toURI());
		ResponseListParser.parser().loadChatFile(file, "Response List", "", false, true, bot);
		List<String> output = registerForOutput(text);
		text.input("this is a very complicated sentence the dog barks all night this is a good reply to that");
		waitForOutput(output);
		Utils.sleep(5000);
		bot.shutdown();
	}

	/**
	 * Test response keywords work.
	 */
	@org.junit.Test
	public void testKeywordMatching() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("this is a very very long sentence that is very long, yes, very long, it has one two three four five size seven eight nine ten or more words");
		String response = waitForOutput(output);
		checkResponse(response, "how long?");

		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is a very very long sentence");
		response = waitForOutput(output);
		checkResponse(response, "how long?");
		
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("very long sentence");
		response = waitForOutput(output);
		checkResponse(response, "how long?");
		
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("sentence");
		response = waitForOutput(output);
		checkResponse(response, "how long?");

		text.input("this is a very very long that is very long, yes, very long, it has one two three four five size seven eight nine ten or more words");
		response = waitForOutput(output);
		if (response.equals("how long?")) {
			fail("should not match: " + response);
		}
		
		text.input("empty");
		response = waitForOutput(output);
		checkResponse(response, "");
		
		text.input("na");
		response = waitForOutput(output);
		checkResponse(response, "");
		
		text.input("is sky blue");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the sky is blue.");
		
		text.input("Sky is BLUE?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the sky is blue.");
		
		text.input("is sky ocean blue");
		response = waitForOutput(output);
		checkResponse(response, "No, the ocean is ocean blue, the sky is sky blue.");
		
		text.input("is the ocean blue");
		response = waitForOutput(output);
		checkResponse(response, "I love hockey!");
		
		text.input("is the grass green?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the grass is green.");
		
		text.input("is the grazz green?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the grass is green.");
		
		text.input("is the sky green?");
		response = waitForOutput(output);
		checkResponse(response, "I love hockey!");
		
		text.input("is the sun yellow?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the sun is yellow.");
		
		text.input("is the sun very bright and yellow?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the sun is yellow.");
		
		text.input("is the sun yllow?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, the sun is yellow.");
		
		text.input("is sun yellow?");
		response = waitForOutput(output);
		checkResponse(response, "I love hockey!");
		
		text.input("Are you a chatbot?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am a bot.");
		
		text.input("Are you a chat bot?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am a bot.");

		text.input("ok");
		response = waitForOutput(output);
		
		text.input("Are you a chatter bot?");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am a bot.");
		
		text.input("are you a chat robot");
		response = waitForOutput(output);
		checkResponse(response, "Yes, I am a bot.");
		
		text.input("Are you a robot chat?");
		response = waitForOutput(output);
		checkResponse(response, "I love hockey!");

		bot.shutdown();
	}

	/**
	 * Test greetings.
	 */
	@org.junit.Test
	public void testGreetings() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input(null);
		String response = waitForOutput(output);
		checkResponse(response, "the topic is sports");
		Network network = bot.memory().newMemory();
		Vertex conversation = text.getConversation(network);
		Vertex topic = conversation.getRelationship(Primitive.TOPIC);
		if (topic == null || topic.getData() == null || !topic.getData().equals("sports")) {
			fail("topic did not match: " + topic);
		}
		String command = bot.avatar().getCommand();
		checkResponse(command, "{\"type\":\"intent\", \"value\":\"buzz\"}");
		String action = bot.avatar().getAction();
		checkResponse(action, "smile");
		String pose = bot.avatar().getPose();
		checkResponse(pose, "waving");
		String emote = bot.avatar().getEmote();
		checkResponse(emote, "HAPPY");
		
		text.input("exit");
		response = waitForOutput(output);
		checkResponse(response, "Exiting");
		
		text.input("greeting srai test");
		response = waitForOutput(output);
		checkResponse(response, "Setup done");
		
		text.input(null);
		response = waitForOutput(output);
		checkResponse(response, "The topic is fun");
		network = bot.memory().newMemory();
		conversation = text.getConversation(network);
		topic = conversation.getRelationship(Primitive.TOPIC);
		if (topic == null || topic.getData() == null || !topic.getData().equals("fun")) {
			fail("topic did not match: " + topic);
		}
				
		bot.shutdown();
	}

	/**
	 * Test on repeat and no repeat.
	 */
	@org.junit.Test
	public void testRepeat() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("repeat");
		String response = waitForOutput(output);
		checkResponse(response, "listen the first time");
		
		text.input("repeat");
		response = waitForOutput(output);
		checkResponse(response, "okay, fine");
		
		text.input("repeat");
		response = waitForOutput(output);
		checkResponse(response, "okay, fine");
		
		text.input("x");
		response = waitForOutput(output);
		
		text.input("repeat");
		response = waitForOutput(output);
		checkResponse(response, "okay, fine");
		
		text.input("repeat 2");
		response = waitForOutput(output);
		checkResponse(response, "on repeat 1");
		
		text.input("repeat 2");
		response = waitForOutput(output);
		checkResponse(response, "on repeat 2", "on repeat 3");
		
		text.input("repeat 2");
		response = waitForOutput(output);
		checkResponse(response, "on repeat 2", "on repeat 3");
		
		text.input("test 3");
		response = waitForOutput(output);
		checkResponse(response, "Hello world", "goodbye world");
		
		text.input("test 3");
		response = waitForOutput(output);
		checkResponse(response, "Hello world", "goodbye world");
		
		text.input("test 3");
		response = waitForOutput(output);
		checkResponse(response, "goodbye world");
		
		text.input("test 3");
		response = waitForOutput(output);
		checkResponse(response, "goodbye world");
		
		text.input("bad template");
		response = waitForOutput(output);
		checkResponse(response, "okay");
				
		bot.shutdown();
	}

	/**
	 * Test response matching works.
	 */
	@org.junit.Test
	public void testTopics() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("hockey");
		String response = waitForOutput(output);
		checkResponse(response, "I love hockey!");
		
		text.input("test");
		response = waitForOutput(output);
		checkResponse(response, "hello world");
		
		text.input("test 2");
		response = waitForOutput(output);
		checkResponse(response, "success");
		
		text.input("sports");
		response = waitForOutput(output);
		checkResponse(response, "lets talk about sports");
		
		Network network = bot.memory().newMemory();
		Vertex conversation = text.getConversation(network);
		Vertex topic = conversation.getRelationship(Primitive.TOPIC);
		if (topic == null || topic.getData() == null || !topic.getData().equals("sports")) {
			fail("topic did not match: " + topic);
		}
		
		text.input("hockey");
		response = waitForOutput(output);
		checkResponse(response, "Who is your favorite team?");
		
		text.input("test 2");
		response = waitForOutput(output);
		checkResponse(response, "sports");
		
		text.input("test");
		response = waitForOutput(output);
		checkResponse(response, "Are we still talking about sports?");
		
		text.input("xxx");
		response = waitForOutput(output);
		checkResponse(response, "Are we still talking about sports?");

		text.input("exit");
		response = waitForOutput(output);
		checkResponse(response, "Exiting");
		
		text.input("topic");
		response = waitForOutput(output);
		checkResponse(response, "");
		
		bot.shutdown();
	}

	/**
	 * Test default responses.
	 */
	@org.junit.Test
	public void testDefaults() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINE);
		
		text.input("xxx");
		String response = waitForOutput(output);
		checkResponse(response, "I love hockey!");
		Network network = bot.memory().newMemory();
		Vertex conversation = text.getConversation(network);
		Vertex topic = conversation.getRelationship(Primitive.TOPIC);
		if (topic == null || topic.getData() == null || !topic.getData().equals("hockey")) {
			fail("topic did not match: " + topic);
		}
		String command = bot.avatar().getCommand();
		checkResponse(command, "{\"type\":\"intent\", \"value\":\"beep\"}");
		String action = bot.avatar().getAction();
		checkResponse(action, "yell");
		String pose = bot.avatar().getPose();
		checkResponse(pose, "cheer");
		String emote = bot.avatar().getEmote();
		checkResponse(emote, "HAPPY");

		text.input("clear topic");
		response = waitForOutput(output);
		checkResponse(response, "Topic cleared");

		text.input("xxx");
		response = waitForOutput(output);
		checkResponse(response, "I said I really love hockey");

		text.input("talk about ai");
		response = waitForOutput(output);
		checkResponse(response, "ok");

		text.input("xxx");
		response = waitForOutput(output);
		checkResponse(response, "the topic is ai", "ask question");

		text.input("yyy");
		response = waitForOutput(output);
		checkResponse(response, "the topic is ai", "ask question", "ask another question");
		boolean askQuestion = response.equals("ask question");

		text.input("zzz");
		response = waitForOutput(output);
		if (askQuestion) {
			askQuestion = false;
			checkResponse(response, "ask another question");
		} else {
			askQuestion = true;
			checkResponse(response, "ask question");
		}

		text.input("qqq");
		response = waitForOutput(output);
		if (askQuestion) {
			askQuestion = false;
			checkResponse(response, "ask another question");
		} else {
			askQuestion = true;
			checkResponse(response, "ask question");
		}
		
		bot.shutdown();
	}

	/**
	 * Test sentiment
	 */
	@org.junit.Test
	public void testSentiment() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);

		text.input("you suck");
		String response = waitForOutput(output);
		checkResponse(response, "I'm doing my best");
		Network network = bot.memory().newMemory();
		Vertex conversation = text.getConversation(network);
		List<Vertex> inputs = conversation.orderedRelations(Primitive.INPUT);
		Vertex input = inputs.get(inputs.size() - 2);
		Relationship sentiment = input.getRelationship(Primitive.EMOTION, Primitive.SENTIMENT);
		if (sentiment == null || sentiment.getCorrectness() > -0.1) {
			fail("Incorrect sentiment: " + sentiment);
		}

		text.input("you are evil");
		response = waitForOutput(output);
		checkResponse(response, "No, I'm nice.");
		network = bot.memory().newMemory();
		conversation = text.getConversation(network);
		inputs = conversation.orderedRelations(Primitive.INPUT);
		input = inputs.get(inputs.size() - 2);
		sentiment = input.getRelationship(Primitive.EMOTION, Primitive.SENTIMENT);
		if (sentiment == null || sentiment.getCorrectness() > -0.5) {
			fail("Incorrect sentiment: " + sentiment);
		}

		text.input("ok");
		response = waitForOutput(output);

		text.input("evil");
		response = waitForOutput(output);
		checkResponse(response, "No, I'm nice.");
		network = bot.memory().newMemory();
		conversation = text.getConversation(network);
		inputs = conversation.orderedRelations(Primitive.INPUT);
		input = inputs.get(inputs.size() - 2);
		sentiment = input.getRelationship(Primitive.EMOTION, Primitive.SENTIMENT);
		if (sentiment == null || sentiment.getCorrectness() > -0.5) {
			fail("Incorrect sentiment: " + sentiment);
		}

		text.input("ok");
		response = waitForOutput(output);

		text.input("you're horrible");
		response = waitForOutput(output);
		checkResponse(response, "No, I'm nice.");
		network = bot.memory().newMemory();
		conversation = text.getConversation(network);
		inputs = conversation.orderedRelations(Primitive.INPUT);
		input = inputs.get(inputs.size() - 2);
		sentiment = input.getRelationship(Primitive.EMOTION, Primitive.SENTIMENT);
		if (sentiment == null || sentiment.getCorrectness() > -0.5) {
			fail("Incorrect sentiment: " + sentiment);
		}

		text.input("ok");
		response = waitForOutput(output);

		text.input("you're very mean");
		response = waitForOutput(output);
		checkResponse(response, "No, I'm nice.");
		network = bot.memory().newMemory();
		conversation = text.getConversation(network);
		inputs = conversation.orderedRelations(Primitive.INPUT);
		input = inputs.get(inputs.size() - 2);
		sentiment = input.getRelationship(Primitive.EMOTION, Primitive.SENTIMENT);
		if (sentiment == null || sentiment.getCorrectness() > -0.5) {
			fail("Incorrect sentiment: " + sentiment);
		}

		text.input("ok");
		response = waitForOutput(output);

		text.input("not very nice");
		response = waitForOutput(output);
		checkResponse(response, "No, I'm nice.");
		network = bot.memory().newMemory();
		conversation = text.getConversation(network);
		inputs = conversation.orderedRelations(Primitive.INPUT);
		input = inputs.get(inputs.size() - 2);
		sentiment = input.getRelationship(Primitive.EMOTION, Primitive.SENTIMENT);
		if (sentiment == null || sentiment.getCorrectness() > -0.5) {
			fail("Incorrect sentiment: " + sentiment);
		}

		bot.shutdown();
	}

	/**
	 * Test response matching works.
	 */
	@org.junit.Test
	public void testResponseMatching() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINER);
		
		text.input("this is a very complicated sentence");
		String response = waitForOutput(output);
		checkResponse(response, "this is a good reply to that");
		
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is very complicated sentence");
		response = waitForOutput(output);
		checkResponse(response, "this is a good reply to that");

		text.input("ok");
		response = waitForOutput(output);
		
		text.input("this is a very complicated");
		response = waitForOutput(output);
		checkResponse(response, "this is a good reply to that");
		
		text.input("ok");
		response = waitForOutput(output);

		text.input("test 4");
		response = waitForOutput(output);
		checkResponse(response, "s1", "s2");
		String previous = response;
		
		text.input("test 4");
		response = waitForOutput(output);
		checkResponse(response, "s1", "s2");
		if (previous.equals(response)) {
			fail("Expceting 2nd response.");
		}
		
		text.input("you evil robot");
		response = waitForOutput(output);
		checkResponse(response, "I'm not evil!");
		
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("you horrible robot");
		response = waitForOutput(output);
		checkResponse(response, "I'm not evil!");
		
		text.input("para");
		response = waitForOutput(output);
		checkResponse(response, "Lorem ipsum dolor sit amet,\n" +
"consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
"Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
"Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\n" +
"Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum");
		
		bot.shutdown();
	}

	/**
	 * Test conversations and next.
	 */
	@org.junit.Test
	public void testConversations() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		bot.setDebugLevel(Level.FINER);

		// Exact match and defaults.
		text.input("help");
		String response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");

		text.input("ok");
		response = waitForOutput(output);
		checkResponse(response, "I can only help with bots and chat");

		text.input("test");
		response = waitForOutput(output);
		checkResponse(response, "I can only help with bots and chat");

		text.input("bots");
		response = waitForOutput(output);
		checkResponse(response, "What kind of bot?");

		text.input("ok");
		response = waitForOutput(output);
		checkResponse(response, "I can only help with bots and chat");

		text.input("test");
		response = waitForOutput(output);
		checkResponse(response, "I can only help with bots and chat");

		text.input("chatbot");
		response = waitForOutput(output);
		checkResponse(response, "Trying chatting more");

		text.input("ok");
		response = waitForOutput(output);
		checkResponse(response, "I love hockey!");

		text.input("test");
		response = waitForOutput(output);
		checkResponse(response, "hello world");

		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");

		text.input("bots");
		response = waitForOutput(output);
		checkResponse(response, "What kind of bot?");

		text.input("twitterbot");
		response = waitForOutput(output);
		checkResponse(response, "Try tweeting more");

		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");

		text.input("chat");
		response = waitForOutput(output);
		checkResponse(response, "Live chat or a chat room?");

		text.input("chat room");
		response = waitForOutput(output);
		checkResponse(response, "What is the room's name?");

		// Patterns.
		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");

		text.input("nope");
		response = waitForOutput(output);
		checkResponse(response, "What is the issue with?");

		text.input("clear topic");
		response = waitForOutput(output);
		
		// Required words
		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");

		text.input("chat");
		response = waitForOutput(output);
		checkResponse(response, "Live chat or a chat room?");

		text.input("a live chat room");
		response = waitForOutput(output);
		checkResponse(response, "What is the operator's name?");
		
		// Topics.
		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");
		
		text.input("bots");
		response = waitForOutput(output);
		checkResponse(response, "What kind of bot?");
		
		text.input("tweet");
		response = waitForOutput(output);
		checkResponse(response, "Try tweeting more");
		
		text.input("xxx");
		response = waitForOutput(output);
		checkResponse(response, "For creating a Twitterbot see, https://...");
		
		// Parent.
		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");
		
		text.input("bots");
		response = waitForOutput(output);
		checkResponse(response, "What kind of bot?");

		text.input("chat");
		response = waitForOutput(output);
		checkResponse(response, "Live chat or a chat room?");
		
		text.input("exit");
		response = waitForOutput(output);
		checkResponse(response, "Goodbye");
		
		// Pop.
		text.input("help");
		response = waitForOutput(output);
		checkResponse(response, "Do you want help with bots or chat?");

		text.input("live");
		response = waitForOutput(output);
		checkResponse(response, "I can only help with bots and chat");

		text.input("chat");
		response = waitForOutput(output);
		checkResponse(response, "Live chat or a chat room?");

		text.input("pop");
		response = waitForOutput(output);
		checkResponse(response, "Lets try that again");

		text.input("live");
		response = waitForOutput(output);
		checkResponse(response, "I can only help with bots and chat");

		text.input("chat");
		response = waitForOutput(output);
		checkResponse(response, "Live chat or a chat room?");

		text.input("bounce");
		response = waitForOutput(output);
		checkResponse(response, "Lets try that again");

		text.input("bounce");
		response = waitForOutput(output);
		checkResponse(response, "Lets try that again");
		
		text.input("exit");
		response = waitForOutput(output);
		checkResponse(response, "Goodbye");
		
		bot.shutdown();
	}

	/**
	 * Test response matching works.
	 */
	@org.junit.Test
	public void testResponseMatching2() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("the dog barks all night");
		String response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("barks all night");
		response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);
		}
		text.input("ok");
		response = waitForOutput(output);
		
		text.input("barks all");
		response = waitForOutput(output);
		if (!response.equals("let him in then")) {
			fail("did not match: " + response);
		}
		text.input("ok");
		response = waitForOutput(output);

		text.input("tell  me  your  name");
		response = waitForOutput(output);
		if (!response.equals("My name is self")) {
			fail("did not match: " + response);
		}
		
		bot.shutdown();
	}

	/**
	 * Test previous.
	 */
	@org.junit.Test
	public void testPrevious() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("yes");
		String response = waitForOutput(output);
		checkResponse(response, "no");

		text.input("yes");
		response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("hi");
		response = waitForOutput(output);
		checkResponse(response, "do you like me?");

		text.input("yes");
		response = waitForOutput(output);
		checkResponse(response, "what do you like about me?");
		
		text.input("hey");
		response = waitForOutput(output);
		checkResponse(response, "are you ok?");

		text.input("yes");
		response = waitForOutput(output);
		checkResponse(response, "are you sure?");
		
		text.input("no");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("hi");
		response = waitForOutput(output);
		checkResponse(response, "whats up?");
		
		text.input("no");
		response = waitForOutput(output);
		checkResponse(response, "why not?");
		
		bot.shutdown();
	}

	/**
	 * Test previous.
	 */
	@org.junit.Test
	public void testUTF8() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("要");
		String response = waitForOutput(output);
		checkResponse(response, "问题：要");

		text.input("要要");
		response = waitForOutput(output);
		checkResponse(response, "问题：要");

		text.input("要 要");
		response = waitForOutput(output);
		checkResponse(response, "问题：要");

		text.input("好");
		response = waitForOutput(output);
		checkResponse(response, "问题：要");
		
		bot.shutdown();
	}

	/**
	 * Test previous.
	 */
	@org.junit.Test
	public void testRedirect() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("hi");
		String response = waitForOutput(output);
		checkResponse(response, "do you like me?");
		
		text.input("hi");
		response = waitForOutput(output);
		checkResponse(response, "whats up?");
		
		text.input("hi");
		response = waitForOutput(output);
		checkResponse(response, "whats up?");

		text.input("hi there");
		response = waitForOutput(output);
		checkResponse(response, "whats up?");

		text.input("redirect hi");
		response = waitForOutput(output);
		checkResponse(response, "whats up?");

		text.input("redirect hi there");
		response = waitForOutput(output);
		checkResponse(response, "whats up?");
		
		bot.shutdown();
	}

	/**
	 * Test Self operators.
	 */
	@org.junit.Test
	public void testSelf() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("test self");
		String response = waitForOutput(output);
		checkResponse(response, "2 1 0 0.5 1");
		
		bot.shutdown();
	}

	/**
	 * Test commands.
	 */
	@org.junit.Test
	public void testCommands() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("test command");
		String response = waitForOutput(output);
		checkResponse(response, "Dont garbage collect me");
		String command = bot.avatar().getCommand();
		checkResponse(command, "{\"type\":\"intent\", \"value\":\"open angry birds\"}");
		
		text.input("test command2");
		response = waitForOutput(output);
		checkResponse(response, "Dont garbage collect me");
		command = bot.avatar().getCommand();
		checkResponse(command, "{\"quick_replies\":[{\"nested\":{\"value\":\"open angry birds\"}}]}");
		
		text.input("test command3");
		response = waitForOutput(output);
		checkResponse(response, "datatypes");
		command = bot.avatar().getCommand();
		checkResponse(command, "{\"reply_markup\":[{\"boolean\":true}, {\"number\":3.14}, {\"null\":null}, [false, -123, \"zxv\", null]]}");
		
		bot.shutdown();
	}

	/**
	 * Test the scripts were executed.
	 */
	@org.junit.Test
	public void testScript() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("do you like black");
		String response = waitForOutput(output);
		if (!response.equals("Yes, it is a nice color.")) {
			fail("did not match: " + response);			
		}
		
		text.input("What are you?");
		response = waitForOutput(output);
		if (!response.equals("I am a bot.")) {
			fail("did not match: " + response);			
		}
		
		bot.shutdown();
	}

	/**
	 * Test commands.
	 */
	@org.junit.Test
	public void testCommand() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		
		text.input("email mom");
		String response = waitForOutput(output);
		checkResponse(response, "sending email");
		
		bot.shutdown();
	}

	/**
	 * Test conditions and think elements.
	 */
	@org.junit.Test
	public void testConditions() {
		Bot bot = Bot.createInstance();
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		TextEntry text = bot.awareness().getSense(TextEntry.class);
		List<String> output = registerForOutput(text);
		//bot.setDebugLevel(Level.FINE);
		
		text.input("am I your friend");
		String response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "no");
		
		text.input("you are my friend");
		response = waitForOutput(output);
		checkResponse(response, "ok, were friends");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		text.input("am I your friend");
		response = waitForOutput(output);
		checkResponse(response, "yes");
		
		bot.shutdown();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		shutdown();
	}
}

