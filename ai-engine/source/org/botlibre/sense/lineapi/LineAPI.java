/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.sense.lineapi;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.sense.http.Http;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.eclipse.persistence.oxm.json.JsonObjectBuilderResult;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Enables receiving a sending messages through Line.
 */
public class LineAPI extends BasicSense {
	public static PolicyFactory sanitizer;
	protected LanguageState groupMode = LanguageState.Discussion;
	protected String authToken;
	protected boolean initProperties = false;
	protected boolean isEnabled;
	protected int messagesProcessed;
	public static int MAX_WAIT = 1000;

	
	public LineAPI(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Answering;
	}
	
	public LineAPI() {
		this(false);
	}
	
	public void setToken(String authToken) {
		this.authToken = authToken;
	}
	
	public String getToken() {
		this.initProperties();
		return this.authToken;
	}
	
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	
	public int getMessagesProcessed() {
		return this.messagesProcessed;
	}
	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}
	
	public LanguageState getGroupMode() {
		initProperties();
		return groupMode;
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		this.isEnabled = true;
		super.awake();
		return;
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
			getBot().memory().loadProperties("Line");
			Network memory = getBot().memory().newMemory();
			Vertex line = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("Line.authToken");
			if (property != null) {
				this.authToken = property;
			}
			this.initProperties = true;
		}
	}
	
	public void saveProperties(String authToken) {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Line.authToken", authToken, true);
		memory.save();
		this.authToken = authToken;
	}
	
	
	public String sanitize(String text) {
		// Telegram does not support <br> but does support new lines.
		text = text.replace("<br/>", "\n");
		text = text.replace("<br>", "\n");
		text = text.replace("</br>", "");
		text = text.replace("<p/>", "\n");
		text = text.replace("<p>", "\n");
		text = text.replace("</p>", "");
		text = text.replace("<li>", "\n");
		text = text.replace("</li>", "");
		text = text.replace("<ul>", "");
		text = text.replace("</ul>", "\n");
		text = text.replace("<ol>", "");
		text = text.replace("</ol>", "\n");
		if (sanitizer == null) {
			sanitizer = new HtmlPolicyBuilder().allowElements(
					"b", "i", "strong", "code", "em", "pre").toFactory().and(Sanitizers.LINKS);
		}
		String result = sanitizer.sanitize(text);
		if (result.contains("&")) {
			// The sanitizer is too aggressive and escaping some chars.
			//result = result.replace("&#34;", "\"");
			result = result.replace("&#96;", "`");
			//result = result.replace("&#39;", "'");
			result = result.replace("&#64;", "@");
			result = result.replace("&#61;", "=");
			result = result.replace("&#43;", "+");
			result = result.replace("&amp;", "&");
		}
		return result;
	}


	
	/**
	 * Send a message to the user.
	 */
	public void sendMessage(String text, String replyToken) {
		try {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", "Bearer "+ this.authToken);
			String strippedText = sanitize(text);
	        	String formattedMsg = String.format("[{'type': 'text', 'text': %s}]", strippedText);
	        	String json = "{\"replyToken\": \"" + replyToken + "\", \"messages\": [{\"type\": \"text\", \"text\": \"" + strippedText +"\"}]}";
			Utils.httpPOST("https://api.line.me/v2/bot/message/reply", "application/json", json, headers);
		}
		catch (Exception exception) {
			log(exception);
			exception.printStackTrace();
		}
	}

	public void checkMessage(JSONObject event, long last, long max, Network network) {
		try {
			String replyToken = event.getString("replyToken");
			String userId = event.getJSONObject("source").getString("userId");
		
			Long timestamp = event.getLong("timestamp");
		
			JSONObject chat = event.getJSONObject("message");
			String type = null;
			if (event.getJSONObject("message").has("type")) {
				type = chat.getString("type");
				}
			String messageId = null;
			if (chat.has("id")) {
				messageId = chat.getString("id");
				}
			String text = "";
			if (chat.get("text") != null) {
				text = chat.getString("text").trim();
				}
			this.messagesProcessed++;
			
			Vertex user = network.createUniqueSpeaker(new Primitive(userId), Primitive.Line, userId);
			Vertex conversationId = network.createVertex(messageId);
			Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(conversationId);
			Vertex conversation = today.getRelationship(conversationId);
		
			if (conversation == null) {
				conversation = network.createVertex();
				today.setRelationship(conversationId, conversation);
				conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
				conversation.addRelationship(Primitive.TYPE, Primitive.Line);
				conversation.addRelationship(Primitive.ID, conversationId);
				Vertex reply = network.createWord(replyToken);
				conversation.addRelationship(Primitive.REPLY, reply);
				conversation.addRelationship(Primitive.SPEAKER, Primitive.SELF);
				if (type != null) {
					conversation.addRelationship(new Primitive("chatType"), network.createVertex(type));
					}
				conversation.addRelationship(Primitive.SPEAKER, user);
				}
			
			inputSentence(text, user, conversation, messageId, chat, network);
			notifyResponseListener();
		}
		catch (Exception e) {
			log("Error Checking Message", Level.INFO, e.getMessage());	
			}
		}
	
	
	protected Vertex createInput(String text, Network network) {
		Vertex sentence = network.createSentence(text);
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		return input;
	}
	
	public void inputSentence(String text, Vertex user, Vertex conversation, String messageId, JSONObject message, Network network) {
		Vertex input = createInput(text.trim(), network);
		if (messageId != null) {
			input.addRelationship(Primitive.ID, network.createVertex(messageId));
		}
		
		input.addRelationship(Primitive.SPEAKER, user);	
		this.languageState = LanguageState.Answering;
		input.addRelationship(Primitive.TARGET, Primitive.SELF);
		
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
	
	
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			notifyResponseListener();
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			notifyResponseListener();
			return;
		}
		String text = printInput(output);
		Vertex target = output.mostConscious(Primitive.TARGET);
		String replyTo = null;
		if (target != null) {
			Vertex user = target.mostConscious(new Primitive("username"));
			if (user == null) {
				user = target.mostConscious(Primitive.WORD);
			}
			if (user == null || user.getData() == null) {
				replyTo = user.getId().toString();
			} else {
				replyTo = user.getData().toString();
			}
		}
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		Vertex id = conversation.getRelationship(Primitive.ID);
		String conversationId = id.printString();

		if (this.responseListener != null) {
			this.responseListener.reply = text;
		}
		notifyResponseListener();
		
		// Don't send empty messages.
		if (text.isEmpty()) {
			return;
		}
		Vertex command = output.mostConscious(Primitive.COMMAND);
		String json = null;
		if (command != null) {
			json = command.printString();
		}
		
		String replyToken = conversation.getRelationship(Primitive.REPLY).printString();
		sendMessage(text, replyToken);
	}
	
}
