/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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

package org.botlibre.sense.kik;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

/**
 * Enables receiving a sending messages through Kik.
 */
public class Kik extends BasicSense {
	
	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	protected int maxErrors = 5;
	protected int errors;
	
	protected String username = "";
	protected String apiKey = "";
	
	protected boolean autoPost = false;
	protected int autoPostHours = 24;
	
	protected int maxFeed = 20;
	protected List<String> postRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();
	
	protected int messagesProcessed;
	protected int posts;
	
	protected boolean initProperties;

	public Kik(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Kik() {
		this(false);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		this.username = this.bot.memory().getProperty("Kik.username");
		if (this.username == null) {
			this.username = "";
		}
		this.apiKey = this.bot.memory().getProperty("Kik.apiKey");
		if (this.apiKey == null) {
			this.apiKey = "";
		}
		
		if (!this.username.isEmpty() && !this.apiKey.isEmpty()) {
			setIsEnabled(true);
		}
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public void configure(String webhookURL) {
		String url = "https://api.kik.com/v1/config";
		String authHeader = username.concat(":").concat(apiKey);
		
		byte[] encodedBytes = Base64.getEncoder().encode(authHeader.getBytes());
		authHeader = new String(encodedBytes);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Basic " + authHeader);
		
		String json = "{\"webhook\": \"" + webhookURL + "\", \"features\": {\"receiveReadReceipts\": false, \"receiveIsTyping\": false, \"manuallySendReadReceipts\": false, \"receiveDeliveryReceipts\": false}}";
		
		try {
			log("Attempting to configure bot: ", Level.INFO, json);
			setIsEnabled(true);
			Utils.httpPOST(url, "application/json", json, headers);
		} catch (Exception e) {
			e.printStackTrace();
			log("Kik could not configure bot: ", Level.INFO, e.toString());
			setIsEnabled(false);
		}
	}
	
	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}
	
	/**
	 * Auto post to channel.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.INFO);
		try {
			initProperties();
			//checkRSS();
			//checkAutoPost();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.INFO);
	}
	
	/**
	 * Load settings.
	 */
	public void initProperties() {
		if (this.initProperties) {
			return;
		}
		synchronized (this) {
			if (this.initProperties) {
				return;
			}
			getBot().memory().loadProperties("Kik");
			//Network memory = getBot().memory().newMemory();
			//Vertex skype = memory.createVertex(getPrimitive());

			String property = this.bot.memory().getProperty("Kik.username");
			if (property != null) {
				this.username = property;
			}
			property = this.bot.memory().getProperty("Kik.apiKey");
			if (property != null) {
				this.apiKey = property;
			}
			
			this.initProperties = true;
		}
	}
	
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Kik.username", this.username, true);
		memory.saveProperty("Kik.apiKey", this.apiKey, true);
		
		memory.save();
		
		if (!this.username.isEmpty() && !this.apiKey.isEmpty()) {
			setIsEnabled(true);
		}
	}
	
	/**
	 * Process to the message and reply synchronously.
	 */
	public String processMessage(String from, String target, String message, String id) {
		log("Processing message", Level.INFO, message);
		
		this.responseListener = new ResponseListener();
		Network memory = bot.memory().newMemory();
		this.messagesProcessed++;
		inputSentence(message, from, target, id, memory);
		memory.save();
		String reply = null;
		synchronized (this.responseListener) {
			if (this.responseListener.reply == null) {
				try {
					this.responseListener.wait(MAX_WAIT);
				} catch (Exception exception) {
					log(exception);
					return "";
				}
			}
			reply = this.responseListener.reply;
			this.responseListener = null;
		}
		
		sendResponse(from, reply, "text", id);
		
		return reply;
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, String targetUsername, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createUniqueSpeaker(new Primitive(userName), Primitive.KIK, userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);
		input.addRelationship(Primitive.TARGET, self);

		Vertex conversationId = network.createVertex(id);
		Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(self);
		Vertex conversation = today.getRelationship(conversationId);
		if (conversation == null) {
			conversation = network.createVertex();
			today.setRelationship(conversationId, conversation);
			this.conversations++;
		} else {
			checkEngaged(conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.KIK);
		conversation.addRelationship(Primitive.ID, network.createVertex(id));
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
	
	/**
	 * Create an input based on the sentence.
	 */
	protected Vertex createInput(String text, Network network) {
		Vertex sentence = network.createSentence(text);
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.KIK);
		return input;
	}
	
	/**
	 * Output the Kik message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to kik, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);
		
		//Strip html tags from response
		text = Utils.stripTags(text);
		
		if (this.responseListener == null) {
			return;
		}
		this.responseListener.reply = text;
		
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		if (conversation != null) {
			this.responseListener.conversation = conversation.getDataValue();
		}
		
		synchronized (this.responseListener) {
			this.responseListener.notifyAll();
		}
	}
	
	protected void sendResponse(String to, String body, String type, String chatId) {
		String url = "https://api.kik.com/v1/message";
		String authHeader = username.concat(":").concat(apiKey);
		
		byte[] encodedBytes = Base64.getEncoder().encode(authHeader.getBytes());
		authHeader = new String(encodedBytes);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Basic " + authHeader);
		
		String json = "{\"messages\": [{\"body\": \"" + body + "\", \"to\": \"" + to + "\", \"type\": \"" + type + "\", \"chatId\": \"" + chatId + "\"}]}";
		
		try {
			Utils.httpPOST(url, "application/json", json, headers);
		} catch (Exception e) {
			log("Kik could not send message: ", Level.INFO, e.toString());
		}
	}
}
