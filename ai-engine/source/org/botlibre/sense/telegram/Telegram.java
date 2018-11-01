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
package org.botlibre.sense.telegram;

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
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Enables receiving a sending messages through Facebook.
 */
public class Telegram extends BasicSense {
	public static PolicyFactory sanitizer;
	
	protected String userId = "";
	protected String userName = "";
	protected String token = "";
	
	protected boolean initProperties;
	public static int MAX_WAIT = 1000; // 1 second, otherwise Telegram will timeout.
	protected int maxErrors = 5;
	protected int maxMessages = 200;
	protected int maxFeed = 20;
	protected int errors;
	protected boolean checkMessages = false;
	protected boolean realtimeMessages = true;
	protected boolean autoPost = false;
	protected int autoPostHours = 24;
	protected String channel = "";
	protected String profileName = "";
	protected List<String> postRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();
	protected boolean stripButtonText = false;
	protected boolean trackMessageObjects = false;
	protected LanguageState groupMode = LanguageState.Discussion;

	protected int posts;
	protected int messagesProcessed;
	
	public Telegram(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Answering;
	}
	
	public Telegram() {
		this(false);
	}
	
	public boolean getTrackMessageObjects() {
		initProperties();
		return trackMessageObjects;
	}

	public void setTrackMessageObjects(boolean trackMessageObjects) {
		initProperties();
		this.trackMessageObjects = trackMessageObjects;
	}

	public LanguageState getGroupMode() {
		initProperties();
		return groupMode;
	}

	public void setGroupMode(LanguageState groupMode) {
		initProperties();
		this.groupMode = groupMode;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getMaxFeed() {
		return maxFeed;
	}

	public void setMaxFeed(int maxFeed) {
		this.maxFeed = maxFeed;
	}

	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public boolean getStripButtonText() {
		initProperties();
		return stripButtonText;
	}

	public void setStripButtonText(boolean stripButtonText) {
		initProperties();
		this.stripButtonText = stripButtonText;
	}

	public List<String> getRssKeywords() {
		initProperties();
		return rssKeywords;
	}

	public void setRssKeywords(List<String> rssKeywords) {
		initProperties();
		this.rssKeywords = rssKeywords;
	}

	public boolean getAutoPost() {
		initProperties();
		return autoPost;
	}

	public void setAutoPost(boolean autoPost) {
		initProperties();
		this.autoPost = autoPost;
	}

	public int getAutoPostHours() {
		initProperties();
		return autoPostHours;
	}

	public void setAutoPostHours(int autoPostHours) {
		initProperties();
		this.autoPostHours = autoPostHours;
	}

	public List<Vertex> getAutoPosts(Network network) {
		return network.createVertex(getPrimitive()).orderedRelations(Primitive.AUTOPOSTS);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		this.userName = this.bot.memory().getProperty("Telegram.userName");
		if (this.userName == null) {
			this.userName = "";
		}
		this.userId = this.bot.memory().getProperty("Telegram.userId");
		if (this.userId == null) {
			this.userId = "";
		}
		this.token = this.bot.memory().getProperty("Telegram.token");
		if (this.token == null) {
			this.token = "";
		}
		if (!this.token.isEmpty()) {
			setIsEnabled(true);
		}
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
			getBot().memory().loadProperties("Telegram");
			Network memory = getBot().memory().newMemory();
			Vertex telegram = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("Telegram.profileName");
			if (property != null) {
				this.profileName = property;
			}
			property = this.bot.memory().getProperty("Telegram.channel");
			if (property != null) {
				this.channel = property;
			}
			property = this.bot.memory().getProperty("Telegram.checkMessages");
			if (property != null) {
				this.checkMessages = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Telegram.realtimeMessages");
			if (property != null) {
				this.realtimeMessages = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Telegram.stripButtonText");
			if (property != null) {
				this.stripButtonText = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Telegram.trackMessageObjects");
			if (property != null) {
				this.trackMessageObjects = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Telegram.groupMode");
			if (property != null) {
				this.groupMode = LanguageState.valueOf(property);
			}
			property = this.bot.memory().getProperty("Telegram.autoPost");
			if (property != null) {
				this.autoPost = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Telegram.autoPostHours");
			if (property != null) {
				this.autoPostHours = Integer.valueOf(property);
			}
			this.postRSS = new ArrayList<String>();
			List<Relationship> rss = telegram.orderedRelationships(Primitive.RSS);
			if (rss != null) {
				for (Relationship relationship : rss) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.postRSS.add(text);
					}
				}
			}
			this.rssKeywords = new ArrayList<String>();
			List<Relationship> keywords = telegram.orderedRelationships(Primitive.RSSKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					this.rssKeywords.add(text);
				}
			}
			this.initProperties = true;
		}
	}

	public void saveProperties(List<String> autoPosts) {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Telegram.userId", this.userId, true);
		memory.saveProperty("Telegram.userName", this.userName, true);
		memory.saveProperty("Telegram.token", this.token, true);

		memory.saveProperty("Telegram.channel", this.channel, false);
		memory.saveProperty("Telegram.profileName", this.profileName, false);
		memory.saveProperty("Telegram.checkMessages", String.valueOf(this.checkMessages), false);
		memory.saveProperty("Telegram.realtimeMessages", String.valueOf(this.realtimeMessages), false);
		memory.saveProperty("Telegram.stripButtonText", String.valueOf(this.stripButtonText), false);
		memory.saveProperty("Telegram.trackMessageObjects", String.valueOf(this.trackMessageObjects), false);
		memory.saveProperty("Telegram.groupMode", String.valueOf(this.groupMode), false);
		memory.saveProperty("Telegram.autoPost", String.valueOf(this.autoPost), false);
		memory.saveProperty("Telegram.autoPostHours", String.valueOf(this.autoPostHours), false);

		Vertex sense = memory.createVertex(getPrimitive());
		sense.unpinChildren();
		sense.internalRemoveRelationships(Primitive.RSS);
		for (String text : this.postRSS) {
			Vertex rss =  memory.createVertex(text);
			sense.addRelationship(Primitive.RSS, rss);
		}
		sense.internalRemoveRelationships(Primitive.RSSKEYWORDS);
		for (String text : this.rssKeywords) {
			Vertex keywords =  memory.createVertex(text);
			sense.addRelationship(Primitive.RSSKEYWORDS, keywords);
		}
		if (autoPosts != null) {
			Collection<Relationship> old = sense.getRelationships(Primitive.AUTOPOSTS);
			if (old != null) {
				for (Relationship post : old) {
					if (post.getTarget().instanceOf(Primitive.FORMULA)) {
						SelfCompiler.getCompiler().unpin(post.getTarget());
					}
				}
			}
			sense.internalRemoveRelationships(Primitive.AUTOPOSTS);
			for (String text : autoPosts) {
				Vertex post =  memory.createSentence(text);
				if (post.instanceOf(Primitive.FORMULA)) {
					SelfCompiler.getCompiler().pin(post);
				}
				post.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
				sense.addRelationship(Primitive.AUTOPOSTS, post);
			}
		}

		sense.pinChildren();
		memory.save();
	}
	
	/**
	 * Register the webhook for the bot for real-time messages.
	 */
	public void connect(String webhook) throws Exception {
		initProperties();
		log("Connecting to Telegram", Level.INFO);
		String json = Utils.httpGET("https://api.telegram.org/bot" + this.token + "/getMe");
		log("Telegram response", Level.FINE, new TextStream(json).nextLine());

		try {
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			JSONObject result = root.getJSONObject("result");
			
			if (this.userName == null || !this.userName.equals(result.getString("username"))) {
				this.userId = result.getString("id");
				this.userName = result.getString("username");
				this.profileName = result.getString("first_name");
				saveProperties(null);
			}
	
			if (webhook != null && !webhook.isEmpty() && this.realtimeMessages) {
				log("Registering webhook", Level.INFO, webhook);
				Map<String, String> params = new HashMap<String, String>();
				params.put("url", webhook);
				String response = Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/setWebhook", params);
				log("Webhook registration response", Level.FINE, response);
				log("Webhook registered", Level.INFO, webhook);
			} else {
				Map<String, String> params = new HashMap<String, String>();
				params.put("url", "");
				String response = Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/setWebhook", params);
				log("Webhook registration response", Level.FINE, response);
			}
			log("Connected to Telegram", Level.INFO);
		} catch (Exception exception) {
			throw new BotException("Invalid JSON response: " + new TextStream(json).nextLine());
		}
	}

	/**
	 * Remove the webhook to disable realtime messages.
	 */
	public void disconnect() throws Exception {
		initProperties();
		log("Disconnecting from Telegram", Level.INFO);
		String json = Utils.httpGET("https://api.telegram.org/bot" + this.token + "/getMe");
		log("Telegram response", Level.FINE, new TextStream(json).nextLine());

		try {
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			JSONObject result = root.getJSONObject("result");
			
			if (this.userName == null || !this.userName.equals(result.getString("username"))) {
				this.userId = result.getString("id");
				this.userName = result.getString("username");
				this.profileName = result.getString("first_name");
				saveProperties(null);
			}
	
			Map<String, String> params = new HashMap<String, String>();
			params.put("url", "");
			String response = Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/setWebhook", params);
			log("Webhook registration response", Level.FINE, response);
				
			log("Disconnected from Telegram", Level.INFO);
		} catch (Exception exception) {
			throw new BotException("Invalid JSON response: " + new TextStream(json).nextLine());
		}
	}

	/**
	 * Auto post to channel.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.INFO);
		try {
			initProperties();
			checkMessages();
			checkRSS();
			checkAutoPost();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.INFO);
	}

	/**
	 * Check messages and reply.
	 */
	public void checkMessages() {
		if (!getCheckMessages()) {
			return;
		}
		log("Checking messages.", Level.INFO);
		try {
			String json = Utils.httpGET("https://api.telegram.org/bot" + this.token + "/getUpdates");
			log("Messages result", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
			JSONArray results = root.getJSONArray("result");
		    if (results != null && results.size() > 0) {
				Network memory = getBot().memory().newMemory();
				Vertex telegram = memory.createVertex(getPrimitive());
				Vertex vertex = telegram.getRelationship(Primitive.LASTDIRECTMESSAGE);
				long last = 0;
				if (vertex != null) {
					last = ((Number)vertex.getData()).longValue();
				}
				long max = 0;
				int offest = 0;
				int count = 0;
				while (results != null && results.size() > 0 && count < this.maxMessages) {
				    for (int index = 0; index < results.size(); index++) {
				    	count++;
				    	JSONObject result = results.getJSONObject(index);
						int updateId = Integer.parseInt(result.getString("update_id"));
						if (updateId > offest) {
							offest = updateId;
						}
						if (result.get("message") == null) {
							continue;
						}
				    	JSONObject message = result.getJSONObject("message");
				    	max = checkMessage(message, last, max, memory);
				    }
					json = Utils.httpGET("https://api.telegram.org/bot" + this.token + "/getUpdates?offset=" + (offest + 1));
					log("Messages result", Level.FINE, json);
					root = (JSONObject)JSONSerializer.toJSON(json);
					results = root.getJSONArray("result");
				}
			    if (max != 0) {
					telegram.setRelationship(Primitive.LASTDIRECTMESSAGE, memory.createVertex(max));
			    	memory.save();
			    }
		    } else {
				log("No conversations", Level.FINE);
		    }
		} catch (Exception exception) {
			log(exception);
			if (exception.getMessage() != null && exception.getMessage().indexOf("Conflict: another webhook is active") != -1) {
				this.checkMessages = false;
				saveProperties(null);
			}
		}
	}

	public String processMessage(JSONObject message, Network memory) {
		this.responseListener = new ResponseListener();
		checkMessage(message, 0, 0, memory);	
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
	 * Reply to the message.
	 * 
	 * message: {"update_id":1234, "message":{"message_id":123,"from":{"id":1234,"first_name":"Michael","last_name":"Jones","username":"mjones"},"chat":{"id":-1234,"title":"test","type":"group","all_members_are_administrators":false},"date":1496344361,"text":"Hello"}}
	 * leave: {"update_id":1234, "message":{"message_id":123,"from":{"id":1234,"first_name":"Michael","last_name":"Jones","username":"mjones"},"chat":{"id":-1234,"title":"test","type":"group","all_members_are_administrators":false},"date":1496344379,"left_chat_participant":{"id":1234,"first_name":"Michael","last_name":"Jones","username":"mjones"},"left_chat_member":{"id":233598213,"first_name":"Michael","last_name":"Jones","username":"mjones"}}}
	 * join: {"update_id":1234, "message":{"message_id":123,"from":{"id":1234,"first_name":"James","last_name":"Jones","username":"jjones","language_code":"en"},"chat":{"id":-1234,"title":"test","type":"group","all_members_are_administrators":false},"date":1496344627,"new_chat_participant":{"id":1234,"first_name":"Michael","last_name":"Jones","username":"mjones"},"new_chat_member":{"id":1234,"first_name":"Michael","last_name":"Jones","username":"mjones"},"new_chat_members":[{"id":233598213,"first_name":"Michael","last_name":"Jones","username":"mjones"}]}}
	 */
	public long checkMessage(JSONObject message, long last, long max, Network network) {
    	String id = message.getString("message_id");
    	if (message.get("chat") == null) {
    		notifyResponseListener();
    		return max;
		}
    	JSONObject chat = message.getJSONObject("chat");
    	String chatId = chat.getString("id");
    	String chatType = chat.getString("type");
    	boolean group = "group".equals(chatType) || "supergroup".equals(chatType);
    	if (group && getGroupMode() == LanguageState.Ignore) {
    		// Ignore messages sent to a group.
    		notifyResponseListener();
    		return max;
    	}
    	String date = message.getString("date");
	    Date createdTime = new Date(((long)Integer.parseInt(date)) * 1000L);
    	if ((System.currentTimeMillis() - createdTime.getTime()) > DAY) {
			log("Day old message", Level.FINE, createdTime, id, date);
			notifyResponseListener();
    		return max;
    	}
    	if (createdTime.getTime() > last) {
    		JSONObject from = message.getJSONObject("from");
		    String fromUserId = from.getString("id");
		    if (!fromUserId.equals(this.userId)) {
			    String fromUser = fromUserId;
			    if (from.has("first_name")) {
			    	fromUser = from.getString("first_name");
			    }
			    if (from.has("last_name")) {
			    	fromUser = fromUser + " " + from.getString("last_name");
			    }
			    String username = null;
			    if (from.has("username")) {
			    	username = from.getString("username");
			    }
			    String chatusername = null;
			    if (chat.has("username")) {
			    	chatusername = chat.getString("username");
			    }
			    String type = null;
			    if (chat.has("type")) {
			    	type = chat.getString("type");
			    }
			    String title = null;
			    if (chat.has("title")) {
			    	title = chat.getString("title");
			    }
			    String messageId = null;
			    if (message.has("message_id")) {
			    	messageId = message.getString("message_id");
			    }
			    String text = "";
			    boolean join = false;
		    	if (message.get("text") != null) {
					text = message.getString("text").trim();
		    	} else if (message.get("new_chat_participant") != null) {
		    		text = "join";
		    		join = true;
	    		} else if (message.get("left_chat_participant") != null) {
		    		text = "leave";
		    		join = true;
	    		}
		    	if (!getTrackMessageObjects() && (join || (text == null || text.isEmpty()))) {
	    			log("Ignoring empty message", Level.INFO, fromUser, createdTime, chatId);
		    		notifyResponseListener();
		    	} else {
					log("Processing message", Level.INFO, fromUser, createdTime, chatId, text);
					this.messagesProcessed++;
					Vertex user = network.createUniqueSpeaker(new Primitive(fromUserId), Primitive.TELEGRAM, fromUser);
					if (username != null) {
						user.addRelationship(new Primitive("username"), network.createVertex(username));
					}
					Vertex conversationId = network.createVertex(chatId);
					Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(conversationId);
					Vertex conversation = today.getRelationship(conversationId);
					if (conversation == null) {
						conversation = network.createVertex();
						today.setRelationship(conversationId, conversation);
						if (group) {
							conversation.addRelationship(Primitive.TYPE, Primitive.GROUP);
						}
						this.conversations++;
					} else {
						checkEngaged(conversation);
					}
					conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
					conversation.addRelationship(Primitive.TYPE, Primitive.TELEGRAM);
					conversation.addRelationship(Primitive.ID, conversationId);
					if (chatusername != null) {
						conversation.addRelationship(new Primitive("username"), network.createVertex(chatusername));
					}
					conversation.addRelationship(Primitive.SPEAKER, Primitive.SELF);
					if (type != null) {
						conversation.addRelationship(new Primitive("chatType"), network.createVertex(type));
					}
					if (title != null) {
						conversation.addRelationship(new Primitive("title"), network.createVertex(title));
					}
					conversation.addRelationship(Primitive.SPEAKER, user);
					inputSentence(text, user, this.userName, conversation, messageId, group, message, network);
			    	if (createdTime.getTime() > max) {
			    		max = createdTime.getTime();
			    	}
		    	}
		    } else {
				log("Ignoring own message", Level.FINE, createdTime, chatId);
	    		notifyResponseListener();
		    }
    	} else {
    		notifyResponseListener();
    	}
    	return max;
	}
	
	/**
	 * Check RSS feed.
	 */
	public void checkRSS() {
		if (getPostRSS().isEmpty()) {
			return;
		}
		log("Processing RSS", Level.FINE, getPostRSS());
		try {
			Network memory = getBot().memory().newMemory();
			Vertex facebook = memory.createVertex(getPrimitive());
			Vertex vertex = facebook.getRelationship(Primitive.LASTRSS);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			for (String rss : getPostRSS()) {
				TextStream stream = new TextStream(rss);
				String prefix = stream.upToAll("http").trim();
				if (prefix.isEmpty()) {
					prefix = "";
				}
				prefix = prefix + " ";
				String url = stream.nextWord();
				String postfix = " " + stream.upToEnd().trim();
				List<Map<String, Object>> feed = getBot().awareness().getSense(Http.class).parseRSSFeed(new URL(url), last);
			    if (feed != null) {
					long max = 0;
					int count = 0;
					this.errors = 0;
				    for (int index = feed.size() - 1; index >= 0; index--) {
				    	Map<String, Object> entry = feed.get(index);
				    	long time = (Long)entry.get("published");
				    	if ((System.currentTimeMillis() - time) > DAY) {
				    		continue;
				    	}
				    	if (time > last) {
					    	if (count > this.maxFeed) {
					    		break;
					    	}
					    	if (this.errors > this.maxErrors) {
					    		break;
					    	}
							String text = (String)entry.get("title");
							if (!getRssKeywords().isEmpty()) {
								boolean match = false;
								List<String> words = new TextStream(text.toLowerCase()).allWords();
					    		for (String keywords : getRssKeywords()) {
									List<String> keyWords = new TextStream(keywords.toLowerCase()).allWords();
							    	if (!keyWords.isEmpty()) {
							    		if (words.containsAll(keyWords)) {
							    			match = true;
										    break;
								    	}
							    	}
					    		}
					    		if (!match) {
									log("Skipping RSS, missing keywords", Level.FINE, text);
						    		continue;
					    		}
				    		}
							log("Posting RSS", Level.FINE, entry.get("title"));
				    		text = prefix + text + postfix;
							if (text.length() > 120) {
								text = text.substring(0, 120);
							}
							post(text + " " + entry.get("link"), null);
					    	Utils.sleep(500);
							count++;
					    	if (time > max) {
					    		max = time;
					    	}
				    	}
				    }
				    if (max != 0) {
						facebook.setRelationship(Primitive.LASTRSS, memory.createVertex(max));
				    	memory.save();
				    }
			    }
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Auto post.
	 */
	public void checkAutoPost() {
		if (!getAutoPost()) {
			return;
		}
		log("Autoposting", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex facebook = memory.createVertex(getPrimitive());
			Vertex vertex = facebook.getRelationship(Primitive.LASTPOST);
			long last = 0;
			if (vertex != null) {
				last = ((Timestamp)vertex.getData()).getTime();
			}
			long millis = getAutoPostHours() * 60 * 60 * 1000;
			if ((System.currentTimeMillis() - last) < millis) {
				log("Autoposting hours not reached", Level.FINE, getAutoPostHours());
				return;
			}
			List<Vertex> autoposts = getAutoPosts(memory);
			if (autoposts != null && !autoposts.isEmpty()) {
				int index = Utils.random().nextInt(autoposts.size());
				Vertex post = autoposts.get(index);
				String text = null;
				// Check for labels and formulas
				if (post.instanceOf(Primitive.LABEL)) {
					post = post.mostConscious(Primitive.RESPONSE);
				}
				if (post.instanceOf(Primitive.FORMULA)) {
					Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
					SelfCompiler.addGlobalVariables(memory.createInstance(Primitive.INPUT), null, memory, variables);
					Vertex result = getBot().mind().getThought(Language.class).evaluateFormula(post, variables, memory);
					if (result != null) {
						text = getBot().mind().getThought(Language.class).getWord(result, memory).getDataValue();
					} else {
						log("Invalid autopost template formula", Level.WARNING, post);
						text = null;
					}
				} else {
					text = post.printString();
				}
				if (text != null) {
					log("Autoposting", Level.INFO, post);
					post(text, null);
			    	Utils.sleep(100);
					facebook.setRelationship(Primitive.LASTPOST, memory.createTimestamp());
			    	memory.save();
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Output the status or direct message reply.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			notifyResponseListener();
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to twitter, ignore.
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
		if (conversation.hasRelationship(Primitive.TYPE, Primitive.GROUP)) {
			text = "@" + replyTo + " " + text;
		}
		sendMessage(text, replyTo, conversationId, json);
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
	 * Call a generic Telegram web API.
	 */
	public Vertex postJSON(String url, Vertex paramsObject, Network network) {
		log("POST JSON:", Level.INFO, url);
		try {
			Map<String, String> params = getBot().awareness().getSense(Http.class).convertToMap(paramsObject);
			String json = Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/" + url, params);
			log("JSON", Level.FINE, json);
			JSONObject root = (JSONObject)JSONSerializer.toJSON(json.trim());
			if (root == null) {
				return null;
			}
			Vertex object = getBot().awareness().getSense(Http.class).convertElement(root, network);
			return object;
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
		return null;
	}

	/**
	 * Self API.
	 * POST the HTTP params and return the JSON data from the Telegram web API.
	 * Append the bot token.
	 */
	public Vertex postJSON(Vertex source, Vertex url, Vertex params) {
		Network network = source.getNetwork();
		return postJSON(url.printString(), params, network);
	}

	/**
	 * Send a message to the user.
	 */
	public void sendMessage(String text, String replyUser, String conversationOrUserId, String command) {
		log("Sending message:", Level.INFO, text, replyUser);
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("chat_id", conversationOrUserId);
			if (text.indexOf('<') != -1 && text.indexOf('>') != -1) {
				String strippedText = text;
				if (getStripButtonText()) {
					strippedText = Utils.stripTag(strippedText, "button");
				}
				strippedText = sanitize(strippedText);
				log("Sanitized message:", Level.INFO, strippedText, replyUser);
				params.put("text", strippedText);
				params.put("parse_mode", "Html");
			} else {
				params.put("text", text);
			}
			// Check for a command message.
			if (command != null && !command.isEmpty()) {
				JSONObject json = (JSONObject)JSONSerializer.toJSON(command);
				if (json.containsKey("reply_markup")) {
					JSONObject markup = json.getJSONObject("reply_markup");
					params.put("reply_markup", markup.toString());
				}
			} else {
				// Check for HTML content to translate to Telegram objects.
				if ((text.indexOf('<') != -1) && (text.indexOf('>') != -1)) {
					try {
						Element root = getBot().awareness().getSense(Http.class).parseHTML(text);
						NodeList nodes = root.getElementsByTagName("button");
						if (nodes.getLength() > 0) {
							String markup = "{\"resize_keyboard\":true,\"one_time_keyboard\":true, \"keyboard\":[";
							int count = 0;
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String button = node.getTextContent().trim();
								if (button != null && !button.isEmpty()) {
									if (count > 0) {
										markup = markup + ",";
									}
									markup = markup + "[{\"text\":\"" + button + "\"}]";
									count++;
								}
							}
							markup = markup + "]}";
							if (count > 0) {
								params.put("reply_markup", markup);
							}
						}
						nodes = root.getElementsByTagName("img");
						if (nodes.getLength() > 0) {
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								String imgClass = node.getAttribute("class");
								String title = node.getAttribute("title");
								if (src != null && !src.isEmpty()) {
									Map<String, String> photoParams = new HashMap<String, String>();
									photoParams.put("chat_id", conversationOrUserId);
									// Support stickers through class="sticker"
									if (imgClass != null && imgClass.contains("sticker")) {
										photoParams.put("sticker", src);
										log("sendSticker:", Level.INFO, params);
										Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendSticker", photoParams);
									} else {
										photoParams.put("photo", src);
										if (title != null && !title.isEmpty()) {
											photoParams.put("caption", title);
										}
										log("sendPhoto:", Level.INFO, params);
										Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendPhoto", photoParams);
									}
								}
							}
						}
						nodes = root.getElementsByTagName("video");
						if (nodes.getLength() > 0) {
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								String title = node.getAttribute("title");
								if (src != null && !src.isEmpty()) {
									Map<String, String> photoParams = new HashMap<String, String>();
									photoParams.put("chat_id", conversationOrUserId);
									photoParams.put("video", src);
									if (title != null && !title.isEmpty()) {
										photoParams.put("caption", title);
									}
									log("sendVideo:", Level.INFO, params);
									Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendVideo", photoParams);
								}
							}
						}
						nodes = root.getElementsByTagName("audio");
						if (nodes.getLength() > 0) {
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								String title = node.getAttribute("title");
								if (src != null && !src.isEmpty()) {
									Map<String, String> photoParams = new HashMap<String, String>();
									photoParams.put("chat_id", conversationOrUserId);
									photoParams.put("audio", src);
									if (title != null && !title.isEmpty()) {
										photoParams.put("caption", title);
									}
									log("sendAudio:", Level.INFO, params);
									Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendAudio", photoParams);
								}
							}
						}
					} catch (Exception exception) {
						log(exception);
						exception.printStackTrace();
					}
				}
			}
			//params.put("reply_to_message_id", messageId);

			log("sendMessage:", Level.INFO, params);
			Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendMessage", params);
		} catch (Exception exception) {
			this.errors++;
			log(exception);
			exception.printStackTrace();
		}
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void message(Vertex source, Vertex conversationOrUserId, Vertex text) {
		sendMessage(source, conversationOrUserId, text);
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void sendMessage(Vertex source, Vertex text, Vertex conversationOrUserId) {
		sendMessage(text.printString(), conversationOrUserId.printString(), conversationOrUserId.printString(), null);
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void sendMessage(Vertex source, Vertex text, Vertex conversationOrUserId, Vertex command) {
		sendMessage(text.printString(), conversationOrUserId.printString(), conversationOrUserId.printString(), command.printString());
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, Vertex user, String botUserName, Vertex conversation, String messageId, boolean group, JSONObject message, Network network) {
		String target = null;
		if (text.startsWith("@")) {
			TextStream stream = new TextStream(text);
			stream.skip();
			target = stream.nextWord();
			stream.skip();
			text = stream.upToEnd();
		}
		Vertex input = createInput(text.trim(), network);
		if (messageId != null) {
			input.addRelationship(Primitive.ID, network.createVertex(messageId));
		}
		if (getTrackMessageObjects() && message != null) {
			input.addRelationship(Primitive.MESSAGE, getBot().awareness().getSense(Http.class).convertElement(message, network));
		}
		input.addRelationship(Primitive.SPEAKER, user);
		if (group) {
			this.languageState = getGroupMode();
			if (botUserName.equals(target)) {
				input.addRelationship(Primitive.TARGET, Primitive.SELF);
			} else if (target != null) {
				input.addRelationship(Primitive.TARGET, network.createSpeaker(target));
			}
		} else {
			this.languageState = LanguageState.Answering;
			input.addRelationship(Primitive.TARGET, Primitive.SELF);
		}
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		return input;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	

	public boolean getCheckMessages() {
		initProperties();
		return checkMessages;
	}

	public void setCheckMessages(boolean checkMessages) {
		this.checkMessages = checkMessages;
	}

	public boolean getRealtimeMessages() {
		initProperties();
		return realtimeMessages;
	}

	public void setRealtimeMessages(boolean realtimeMessages) {
		this.realtimeMessages = realtimeMessages;
	}

	public List<String> getPostRSS() {
		initProperties();
		return postRSS;
	}

	public void setPostRSS(List<String> postRSS) {
		initProperties();
		this.postRSS = postRSS;
	}
	
	public void post(String text, String id) {
		initProperties();
		this.posts++;
		log("Posting to channel:", Level.INFO, this.channel, text);
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("chat_id", "@" + this.channel);
			if (text.indexOf('<') != -1 && text.indexOf('>') != -1) {
				params.put("text", sanitize(text));
				params.put("parse_mode", "Html");
			} else {
				params.put("text", text);
			}

			Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendMessage", params);
			
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
	}

	// Self API
	public void post(Vertex source, Vertex sentence) {
		if (sentence.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(sentence.getNetwork().createInstance(Primitive.INPUT), null, sentence.getNetwork(), variables);
			sentence = getBot().mind().getThought(Language.class).evaluateFormula(sentence, variables, sentence.getNetwork());
			if (sentence == null) {
				log("Invalid template formula", Level.WARNING, sentence);
				return;
			}
		}
		String post = getBot().mind().getThought(Language.class).getWord(sentence, sentence.getNetwork()).getDataValue();
		getBot().stat("telegram.post");
		post(post, null);
	}
	
}