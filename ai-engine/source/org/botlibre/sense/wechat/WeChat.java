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

package org.botlibre.sense.wechat;

import java.util.Date;
import java.util.logging.Level;

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

public class WeChat extends BasicSense {
	
	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	protected String appId = "";
	protected String appPassword = "";
	protected String token = "";	//Wechat Access Token
	protected Date tokenExpiry;
	protected boolean international; //International Account or China Account
	protected String userToken = ""; //User supplied token
	
	protected boolean initProperties;
	
	protected int messagesProcessed;
	
	protected static String INTERNATIONAL_API = "api.wechat.com";
	protected static String CHINA_API = "api.weixin.qq.com";
	
	public WeChat(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public WeChat() {
		this(false);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		this.token = this.bot.memory().getProperty("WeChat.token");
		if (this.token == null) {
			this.token = "";
		}
		if(this.bot.memory().getProperty("WeChat.tokenExpiry") != null) {
			this.tokenExpiry = new Date(Long.valueOf(this.bot.memory().getProperty("WeChat.tokenExpiry")));
		}
		
		this.appId = this.bot.memory().getProperty("WeChat.appId");
		if (this.appId == null) {
			this.appId = "";
		}
		this.appPassword = this.bot.memory().getProperty("WeChat.appPassword");
		if (this.appPassword == null) {
			this.appPassword = "";
		}
		this.userToken = this.bot.memory().getProperty("WeChat.userToken");
		if (this.userToken == null) {
			this.userToken = "";
		}
		this.international = Boolean.valueOf(this.bot.memory().getProperty("WeChat.international"));
		
		if (!this.userToken.isEmpty()) {
			setIsEnabled(true);
		}
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public Date getTokenExpiry() {
		return tokenExpiry;
	}
	
	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public String getAppPassword() {
		return appPassword;
	}

	public void setAppPassword(String appPassword) {
		this.appPassword = appPassword;
	}
	
	public Boolean getInternational() {
		return international;
	}

	public void setInternational(Boolean international) {
		this.international = international;
	}
	
	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
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
			getBot().memory().loadProperties("WeChat");
			
			String property = this.bot.memory().getProperty("WeChat.token");
			if (property != null) {
				this.token = property;
			}
			property = this.bot.memory().getProperty("WeChat.tokenExpiry");
			if (property != null) {
				this.tokenExpiry = new Date(Long.valueOf(property));
			}
			property = this.bot.memory().getProperty("WeChat.appId");
			if (property != null) {
				this.appId = property;
			}
			property = this.bot.memory().getProperty("WeChat.appPassword");
			if (property != null) {
				this.appPassword = property;
			}
			property = this.bot.memory().getProperty("WeChat.userToken");
			if (property != null) {
				this.userToken = property;
			}
			this.international = Boolean.valueOf(this.bot.memory().getProperty("WeChat.international"));
			
			this.initProperties = true;
		}
	}
	
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("WeChat.token", this.token, true);
		memory.saveProperty("WeChat.appId", this.appId, true);
		memory.saveProperty("WeChat.appPassword", this.appPassword, true);
		memory.saveProperty("WeChat.international", String.valueOf(this.international), true);
		memory.saveProperty("WeChat.userToken", this.userToken, true);
		
		if (this.tokenExpiry == null) {
			memory.removeProperty("WeChat.tokenExpiry");
		} else {
			memory.saveProperty("WeChat.tokenExpiry", String.valueOf(this.tokenExpiry.getTime()), true);
		}
		
		memory.save();
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
		} else {
			checkEngaged(conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.WECHAT);
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.WECHAT);
		return input;
	}
	
	/**
	 * Output the WeChat message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to WeChat, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);	
		
		//Strip html tags from response (but keep link tags)
		text = text.replace("<a", "[[[a");
		text = text.replace("</a", "[/[[a");
		
		text = Utils.stripTags(text);
			
		text = text.replace("[[[a", "<a");
		text = text.replace("[/[[a", "</a");
				
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
	 * Retrieves access token for bot.
	 * Access token must be obtained before calling APIs
	 */
	protected void getAccessToken() throws Exception {
		String url = "https://";
		url = url.concat( this.international ? INTERNATIONAL_API : CHINA_API );
		url = url.concat("/cgi-bin/token");
		
		String data = "?grant_type=client_credential&appid=" + appId + "&secret=" + appPassword;
		url = url.concat(data);
		
		String json = Utils.httpGET(url);
		
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		
		if(root.optString("access_token") != null) {
			int tokenExpiresIn = root.optInt("expires_in");
			String accessToken = root.optString("access_token");
			
			setToken(accessToken);
			this.tokenExpiry = new Date(System.currentTimeMillis() + (tokenExpiresIn * 1000));
			
			log("WeChat access token retrieved - token: " + accessToken + ", expires in " + tokenExpiresIn, Level.INFO);
		} else if (root.optString("errmsg") != null) {
			String errMsg = root.optString("errmsg");
			int errCode = root.optInt("errcode");
			log("WeChat get access token failed - Error code:" + errCode + ", Error Msg: " + errMsg, Level.INFO);
		} else {
			log("WeChat get access token failed.", Level.INFO);
		}
		
		saveProperties();
	}
}
