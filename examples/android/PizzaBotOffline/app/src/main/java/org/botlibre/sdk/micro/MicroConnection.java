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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.Sense;
import org.botlibre.knowledge.BasicVertex;
import org.botlibre.knowledge.Bootstrap;
import org.botlibre.knowledge.Primitive;
import org.botlibre.knowledge.micro.MicroMemory;
import org.botlibre.sdk.Credentials;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.SDKException;
import org.botlibre.sdk.activity.ChatActivity;
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

import android.app.Activity;
import android.net.Uri;
import android.util.Log;

/**
 * The MicroConnection replaces the normal SDK remote connection with one that creates
 * and accessed bots locally.
 */
public class MicroConnection extends SDKConnection implements TextListener {
	private WebMediumConfig config;
	protected Map<String, Bot> activeBots = new HashMap<String, Bot>();
	public static AvatarConfig avatarConfig;
	public TextOutput message;
	public static Bot bot;
	
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

	public void addBot(String id, Bot bot) {
		activeBots.put(id, bot);
	}

	public Bot getBot(String id) {
		return activeBots.get(id);
	}

	public static Bot getBot() {
		return bot;
	}

	/**
	 * Process the bot chat message and return the bot's response.
	 * The ChatConfig should contain the conversation id if part of a conversation.
	 * If a new conversation the conversation id is returned in the response. 
	 */
	public synchronized ChatResponse chat(ChatConfig config) {
		if (config.instance == null) {
			config.instance = "default";
		}
		// this method will return a Bot with the default instance
		Bot bot = getBot(this.config.id);
		ChatActivity.debug("Serialized: " + this.config.name);

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
		// setting the user admin for the BOT.
		sense.setUser(reInitialize(bot));
		bot.setFilterProfanity(false);

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
				try {
					avatarConfig.chatReply(response);
				} catch (Exception e) {
				}
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
		MicroMemory.storageFileName = config.name;
		int id = Integer.parseInt(config.id);
		bot = loadBot(config.name, id);
		addBot(config.id, bot);
		if(!(config instanceof InstanceConfig)){
			Log.e("MicroConnection","Config is not from InstanceConfig");
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
	
	public synchronized Bot loadBot(String name, int id) {
		boolean exists = MicroMemory.checkExists();
		Log.e("MicroConnection","Bot exists: " + exists);
		/*try {
			if (!exists) {
				switch(id) {
					case 0:
						MicroConnection.copyStream(BasicVertex.class.getResourceAsStream("Empty"));
						break;
					case 1:
						MicroConnection.copyStream(BasicVertex.class.getResourceAsStream("Basic"));
						break;
					case 2:
						MicroConnection.copyStream(BasicVertex.class.getResourceAsStream("MobileAssistant"));
						break;
				}
			}
		} catch (Exception exception) {
			Log.e("MicroConnection", "Error: " + exception);
			exception.printStackTrace();
		}*/

		Log.e("MicroConnection","Load bot file: " + Bot.CONFIG_FILE);
		Log.e("MicroConnection","Load name: " + name);
		Bot bot = Bot.createInstance(Bot.CONFIG_FILE, name, true);
		bot.setName(name);
		Log.e("MicroConnection","Bot: " + bot);
		Log.e("MicroConnection","Bot name: " + bot.getName());

		Language language = bot.mind().getThought(Language.class);
		language.setLearnGrammar(false);
		//if (id != 2) {
			language.setLearningMode(LearningMode.Disabled);
			Comprehension comprehension = bot.mind().getThought(Comprehension.class);
			comprehension.setEnabled(false);
			Consciousness consciousness = bot.mind().getThought(Consciousness.class);
			consciousness.setEnabled(false);
			Sense sense = bot.awareness().getSense(Wiktionary.class);
			sense.setIsEnabled(false);
		//}

//		renameMemory(bot,instance);//rename the bot with the new instance name.
//		bot.memory().shutdown();//save the new settings.

		if (!exists) {
			try {
				new Bootstrap().bootstrapSystem(bot, false);
				loadResScript(bot, "/assets/servicebot.res");
				loadResScript(bot, "/assets/farewells.res");
				loadResScript(bot, "/assets/pizza.res");
				loadSelfScript(bot, "/assets/pizza.self");
				bot.memory().shutdown();
			} catch (Exception exception) {
				Log.wtf("loadSelfScript", exception);
			}
		}
		return bot;
	}
	
	public void loadAIMLScript(Language lang,InputStream source){
		lang.loadAIMLFile(source, config.id, false, false, true,"",10000000);
	}
	
	public void loadResScript(Bot bot, String fileName) {
		URL url = getClass().getResource(fileName);
		bot.awareness().getSense(TextEntry.class).loadChatFile(url, "Response List", "", false, true);
	}

	public void loadResScript(Bot bot, File file) {
		bot.awareness().getSense(TextEntry.class).loadChatFile(file, "Response List", "", false, true);
	}
	
	public void loadSelfScript(Bot bot, String fileName) {
		loadSelfScript(bot, getClass().getResource(fileName));
	}

	public void loadSelfScript(Bot bot, URL url) {
		Network network = bot.memory().getShortTermMemory();
		Vertex language = network.createVertex(new Primitive(Language.class.getName()));
		SelfCompiler compiler = SelfCompiler.getCompiler();
		Vertex stateMachine = compiler.parseStateMachine(url, "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
	}

	public void loadSelfScript(Bot bot, File file) {
		Network network = bot.memory().getShortTermMemory();
		Vertex language = network.createVertex(new Primitive(Language.class.getName()));
		SelfCompiler compiler = SelfCompiler.getCompiler();
		Vertex stateMachine = compiler.parseStateMachine(file, "", false, network);
		language.addRelationship(Primitive.STATE, stateMachine);
		SelfCompiler.getCompiler().pin(stateMachine);
	}
	
	public void renameMemory(Bot bot, String name) {
		bot.memory().getShortTermMemory().createVertex(Primitive.SELF).addRelationship(Primitive.NAME, bot.memory().getShortTermMemory().createVertex(name));
		
	}
	
	public Vertex reInitialize(Bot bot) {
		Network memory = bot.memory().newMemory();
		Vertex user = memory.createSpeaker("Admin");
		user.addRelationship(Primitive.ASSOCIATE, Primitive.ADMINISTRATOR);
		memory.save();
		return user;
	}
	
	//copy files from jar file (using input stream to Android internal storage).
	public static void copyStream(InputStream input) throws IOException {
		FileOutputStream output = MainActivity.current.openFileOutput(MicroMemory.storageFileName,
				Activity.MODE_PRIVATE);
		byte[] buffer = new byte[1024]; // Adjust if you want
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}
}