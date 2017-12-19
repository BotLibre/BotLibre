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

package org.botlibre.sdk.micro;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.database.DatabaseMemory;
import org.botlibre.sdk.Credentials;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.SDKException;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.http.Wiktionary;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.sense.text.TextInput;
import org.botlibre.sense.text.TextListener;
import org.botlibre.sense.text.TextOutput;
import org.botlibre.thought.consciousness.Consciousness;
import org.botlibre.thought.language.Comprehension;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;

import android.util.Log;

/**
 * The MicroConnection adapts the online bot SDKConnection interface into accessing a local bot stored on the local file system.
 */
public class MicroConnection extends SDKConnection implements TextListener {
	private WebMediumConfig config;
	protected Map<String, Bot> activeBots = new HashMap<String, Bot>();
	public static AvatarConfig avatarConfig;
	public TextOutput message;
	public static Bot bot=null;
	int id = 0;
	
	/**
	 * Create a micro/offline SDK connection.
	 */
	public MicroConnection() {
	}
	
	/**
	 * Create a micro/offline SDK connection.
	 * Use the credentials to enable online support.
	 */
	public MicroConnection(Credentials credentials) {
		
		this.credentials = credentials;
		this.url = credentials.url;
	}
	
	public void addBot(String id, Bot bot){
		activeBots.put(id, bot);
	}
	
	public Bot getBot(String id){
		return activeBots.get(id);
	}
	
	public boolean botContains(String id){
		return activeBots.containsKey(id);
	}
	
	public static Bot getBot(){return bot;}

	/**
	 * Process the bot chat message and return the bot's response.
	 * The ChatConfig should contain the conversation id if part of a conversation.
	 * If a new conversation the conversation id is returned in the response. 
	 */
	public synchronized ChatResponse chat(ChatConfig config) {
		
		if (config.instance == null) {
			config.instance = "default";
		}
		try {
			int i = 0;
				while (i <= 4) {
					if (bot == null) {
						Log.e("MicroConnection - Bot", "Checking for a bot!");
						wait(1000);
						bot = this.activeBots.get(config.instance);
					} else if (bot != null) {
						Log.e("MicroConnection - Bot", "Connected to a bot!");
						break;
					}
					i++;
				}
			} catch (Exception e) {}
		if (bot == null) {
			this.exception = new SDKException("Bot doesn't exists!");
			Log.e("MicroCoonection","Chat Connection: " + exception.getMessage());
			throw this.exception;
		}
		
		TextEntry sense = bot.awareness().getSense(TextEntry.class);
		//setting the user admin for the BOT.
		sense.setUser(reInitialize(bot));
		
		sense.setTextListener(this);
		this.message = null;
		TextInput textInput = new TextInput(config.message);
		textInput.setCorrection(config.correction);
		textInput.setOffended(config.offensive);
		
		sense.input(textInput);
		ChatResponse response = new ChatResponse();
		try {
			if (this.message == null) {
				wait(5000);
			}
			if (this.message != null) {
				response.message = this.message.getMessage();
				response.command = bot.avatar().getCommand();
				try{
				avatarConfig.chatReplay(response);
				}catch(Exception e){}
			}
		} catch (InterruptedException exception) {
			this.exception = new SDKException("Timeout waiting for response");
			throw this.exception;
		}
		return response;
	}
	
	/**
	 * Fetch the local object.
	 */
	public <T extends WebMediumConfig> T fetch(T config) {
		this.config = config;
		System.out.println("Bot name: " + config.name);
		id = Integer.parseInt(config.id);
		bot = loadBot(config.name);
		addBot(config.id, bot);
		if (!(config instanceof InstanceConfig)) {
			return super.fetch(config);
		}
		return config;
	}
	
	/**
	 * Return the bot's voice configuration.
	 */
	public VoiceConfig getVoice(InstanceConfig config) {
		VoiceConfig voice = new VoiceConfig();
		voice.nativeVoice = true;
		voice.language = "en";
		return voice;
	}
	
	public synchronized void sendMessage(TextOutput message) {
		this.message = message;
		notifyAll();
	}
	public boolean checkForBot(String instance) {
		return !MainActivity.resetBot;
	}
	
	public Bot loadBot(String instance){
		boolean exists = checkForBot(instance);
		if (exists) {
			System.out.println("Bot exists.");
			Bot bot = Bot.createInstance(Bot.CONFIG_FILE, instance, true);
			bot.setFilterProfanity(false);
			Language language = bot.mind().getThought(Language.class);
			language.setLearningMode(LearningMode.Disabled);
			language.setLearnGrammar(false);
			Comprehension comprehension = bot.mind().getThought(Comprehension.class);
			comprehension.setEnabled(false);
			Consciousness consciousness = bot.mind().getThought(Consciousness.class);
			consciousness.setEnabled(false);
			//Sense sense = bot.awareness().getSense(Wiktionary.class);
			//sense.setIsEnabled(false);
			if (this.debug) {
				bot.setDebugLevel(Level.FINE);
			}
			return bot;
		}
		// Creating a new bot.
		System.out.println("Creating a new bot.");
		DatabaseMemory.RECREATE_DATABASE = true;
		Bot bot = Bot.createInstance(Bot.CONFIG_FILE,instance , true);
		bot.setFilterProfanity(false);
		if (this.debug) {
			bot.setDebugLevel(Level.FINE);
		}
		DatabaseMemory.RECREATE_DATABASE = false;
		
		// Setting the new empty bot.
		Language language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		language.setLearnGrammar(false);
		Comprehension comprehension = bot.mind().getThought(Comprehension.class);
		comprehension.setEnabled(false);
		Consciousness consciousness = bot.mind().getThought(Consciousness.class);
		consciousness.setEnabled(false);
		Sense sense = bot.awareness().getSense(Wiktionary.class);
		sense.setIsEnabled(false);
	
		// Load the default script.
		switch(id) {
		case 000:
			new Bootstrap().bootstrapSystem(bot, true);
			break;
		case 001:
			new Bootstrap().bootstrapSystem(bot, true);
			loadResScript(bot, "greetings.res");
			loadResScript(bot, "basic.res");
			break;
		case 002:
			language.setLearningMode(LearningMode.Everyone);
			new Bootstrap().bootstrapSystem(bot, true);
			loadResScript(bot, "greetings.res");
			loadResScript(bot, "BrainBot.res");
			break;
		case 003:
			new Bootstrap().bootstrapSystem(bot, true);
			loadResScript(bot, "julie.res");
			loadSelfScript(bot, "ispy.self");
			break;
		case 004:
			new Bootstrap().bootstrapSystem(bot, true);
			loadResScript(bot, "eddie.res");
			loadSelfScript(bot, "ispy.self");
			break;
		case 005:
			new Bootstrap().bootstrapSystem(bot, false);
			loadResScript(bot, "greetings.res");
			InputStream in;
			in = getClass().getResourceAsStream("alice.aiml");
			loadAIMLScript(language,in);
			break;
		}
		//Name the Bot.
		renameMemory(bot,instance);
		// Shutdown will save the file.
		
		bot.shutdown();
		// Reload the bot from the file.
		bot = Bot.createInstance(Bot.CONFIG_FILE,instance , true);
		bot.setFilterProfanity(false);
		language = bot.mind().getThought(Language.class);
		language.setLearningMode(LearningMode.Disabled);
		language.setLearnGrammar(false);
		comprehension = bot.mind().getThought(Comprehension.class);
		comprehension.setEnabled(false);
		consciousness = bot.mind().getThought(Consciousness.class);
		consciousness.setEnabled(false);
		//sense = bot.awareness().getSense(Wiktionary.class);
		//sense.setIsEnabled(false);
		
		return bot;
	}
	
	public void loadAIMLScript(Language lang,InputStream source){
		lang.loadAIMLFile(source, config.id, false, false, true,"",10000000);
	}
	
	public void loadResScript(Bot bot, String fileName){
		URL url = getClass().getResource(fileName);
		bot.awareness().getSense(TextEntry.class).loadChatFile(url, "Response List", "", false, true);
	}
	
	public void loadSelfScript(Bot bot, String fileName){
		Network network = bot.memory().getShortTermMemory();
		Vertex language = network.createVertex(new Primitive(Language.class.getName()));
		SelfCompiler compiler = SelfCompiler.getCompiler();
		Vertex stateMachine = compiler.parseStateMachine(getClass().getResource(fileName), "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
	}
	
	public void renameMemory(Bot bot, String name){
		bot.memory().getShortTermMemory().createVertex(Primitive.SELF).addRelationship(Primitive.NAME, bot.memory().getShortTermMemory().createVertex(name));
		
	}
	
	public Vertex reInitialize(Bot bot){
		Network memory = bot.memory().newMemory();
		Vertex user = memory.createSpeaker("Admin");
		user.addRelationship(Primitive.ASSOCIATE, Primitive.ADMINISTRATOR);
		memory.save();
		return user;
	}
	
}