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
package org.botlibre.sense.skype;

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Enables receiving a sending messages through Skype.
 */
public class Skype extends BasicSense {

	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	protected String appId = "";
	protected String appPassword = "";
	
	protected String token = "";
	protected String token2 = "";
	protected Date tokenExpiry;
	 
	protected boolean initProperties;
	
	protected int messagesProcessed;
	
	public Skype(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Skype() {
		this(false);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		this.token = this.bot.memory().getProperty("Skype.token");
		if (this.token == null) {
			this.token = "";
		}
		this.token2 = this.bot.memory().getProperty("Skype.token2");
		if (this.token2 == null) {
			this.token2 = "";
		}

		if(this.bot.memory().getProperty("Skype.tokenExpiry") != null) {
			this.tokenExpiry = new Date(Long.valueOf(this.bot.memory().getProperty("Skype.tokenExpiry")));
		}
		
		this.appId = this.bot.memory().getProperty("Skype.appId");
		if (this.appId == null) {
			this.appId = "";
		}
		this.appPassword = this.bot.memory().getProperty("Skype.appPassword");
		if (this.appPassword == null) {
			this.appPassword = "";
		}
		
		if (!this.appId.isEmpty() && !this.appPassword.isEmpty()) {
			setIsEnabled(true);
		}
	}
	
	public String getToken() {
		return token.concat(token2);
	}

	public void setToken(String token) {
		int middleIndex = token.length() / 2;
		this.token = token.substring(0, middleIndex);
		this.token2 = token.substring(middleIndex, token.length());
	}
	
	public Date getTokenExpiry() {
		return tokenExpiry;
	}
	
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		if(this.appId != null && this.appId != appId) {
			this.token = "";
			this.token2 = "";
		} 
		this.appId = appId;
	}
	
	public String getAppPassword() {
		return appPassword;
	}

	public void setAppPassword(String appPassword) {
		if(this.appPassword != null && this.appPassword != appPassword) {
			this.token = "";
			this.token2 = "";
		}
		this.appPassword = appPassword;
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
			getBot().memory().loadProperties("Skype");
			//Network memory = getBot().memory().newMemory();
			//Vertex skype = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("Skype.token");
			if (property != null) {
				this.token = property;
			}
			property = this.bot.memory().getProperty("Skype.token2");
			if (property != null) {
				this.token2 = property;
			}
			property = this.bot.memory().getProperty("Skype.tokenExpiry");
			if (property != null) {
				this.tokenExpiry = new Date(Long.valueOf(property));
			}
			property = this.bot.memory().getProperty("Skype.appId");
			if (property != null) {
				this.appId = property;
			}
			property = this.bot.memory().getProperty("Skype.appPassword");
			if (property != null) {
				this.appPassword = property;
			}
			
			this.initProperties = true;
		}
	}
	
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Skype.token", this.token, true);
		memory.saveProperty("Skype.token2", this.token2, true);
		memory.saveProperty("Skype.appId", this.appId, true);
		memory.saveProperty("Skype.appPassword", this.appPassword, true);
		
		if (this.tokenExpiry == null) {
			memory.removeProperty("Skype.tokenExpiry");
		} else {
			memory.saveProperty("Skype.tokenExpiry", String.valueOf(this.tokenExpiry.getTime()), true);
		}
		
		memory.save();
		
		if(this.appId != null && !this.appId.isEmpty() && this.appPassword != null && !this.appPassword.isEmpty()) {
			setIsEnabled(true);
		}
	}
	
	/**
	 * Auto post to channel.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.INFO);
		try {
			initProperties();
			getAccessToken();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.INFO);
	}
	
	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}
	
	public String processMessage(String json) {
		SkypeActivity input = new SkypeActivity(json);
		
		if(input.type.equals("message")) {
			try {
				String message = processMessage(input.fromName, input.recipientName, input.text, input.conversationId);
				return sendResponse(input, message);
			} catch (Exception e) {
				log("Skype send response exception", Level.INFO, e.toString());
				e.printStackTrace();
				return null;
			}
		}
		return null;
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
		
		return reply;
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, String targetUsername, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createSpeaker(userName);
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
		}  else {
			checkEngaged(conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.SKYPE);
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SKYPE);
		return input;
	}
	
	/**
	 * Output the Skype message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to skype, ignore.
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
		
		Vertex command = output.mostConscious(Primitive.COMMAND);
		
		// If the response is empty, do not send it.
		if (command == null && text.isEmpty()) {
			return;
		}
		
		synchronized (this.responseListener) {
			this.responseListener.notifyAll();
		}
	}
	
	/**
	 * Retrieves access token for bot
	 */
	protected void getAccessToken() throws Exception {
		String url = "https://login.microsoftonline.com/botframework.com/oauth2/v2.0/token";

		String type = "application/x-www-form-urlencoded";
		String data = "grant_type=client_credentials&client_id=" + appId + "&client_secret=" + appPassword + "&scope=https%3A%2F%2Fapi.botframework.com%2F.default";
		String json = Utils.httpPOST(url, type, data);
		
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		
		if(root.optString("token_type").equals("Bearer")) {
			int tokenExpiresIn = root.optInt("expires_in");
			String accessToken = root.optString("access_token");
			
			setToken(accessToken);
			this.tokenExpiry = new Date(System.currentTimeMillis() + (tokenExpiresIn * 1000));
			
			log("Skype access token retrieved.", Level.INFO);
		} else {
			log("Skype get access token failed:", Level.INFO);
		}
		
		saveProperties();
	}
	
	protected String sendResponse(SkypeActivity input, String response) throws Exception {		
		Date now = new Date();
		if((StringUtils.isBlank(token) || StringUtils.isBlank(token2) || tokenExpiry == null) || (tokenExpiry != null && now.after(tokenExpiry))) {
			getAccessToken();
		}
		
		SkypeActivity output = new SkypeActivity();
		output.type = "message";
		output.id = input.id;
		output.conversationId = input.conversationId;
		output.conversationName = input.conversationName;
		output.fromId = input.recipientId;
		output.fromName = input.recipientName;
		output.recipientId = input.fromId;
		output.recipientName = input.fromName;
		output.serviceURL = input.serviceURL;
		output.text = response;
		
		if(!output.serviceURL.endsWith("/")) {
			output.serviceURL = output.serviceURL.concat("/");
		}
		
		String url = output.serviceURL + "v3/conversations/" + URLEncoder.encode(output.conversationId, "UTF-8") + "/activities/" + URLEncoder.encode(output.id, "UTF-8");
		
		String json = "{ \"type\": \"message\", \"from\": { \"id\": \"" + output.fromId + "\", \"name\": \"" + output.fromName + "\" }, \"conversation\": {\"id\": \"" + output.conversationId + "\",\"name\": \"" + output.conversationName + "\"},\"recipient\": {\"id\": \"" + output.recipientId  + "\",\"name\": \"" + output.recipientName + "\"},\"text\": \"" + output.text + "\", \"replyToId\": \"" + output.id + "\"}";
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + this.getToken());
		
		Utils.httpPOST(url, "application/json", json, headers);
		
		return json;
	}
}
