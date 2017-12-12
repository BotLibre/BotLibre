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
import org.botlibre.sense.http.Http;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Enables receiving a sending messages through Facebook.
 */
public class Telegram extends BasicSense {
	
	protected String userId = "";
	protected String userName = "";
	protected String token = "";
	
	protected boolean initProperties;
	
	protected int maxErrors = 5;
	protected int maxMessages = 200;
	protected int maxFeed = 20;
	protected int errors;
	protected boolean checkMessages = false;
	protected boolean realtimeMessages = false;
	protected boolean autoPost = false;
	protected int autoPostHours = 24;
	protected String channel = "";
	protected String profileName = "";
	protected List<String> postRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();

	protected int posts;
	protected int messagesProcessed;
	
	public Telegram(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Answering;
	}
	
	public Telegram() {
		this(false);
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
		memory.saveProperty("Telegram.autoPost", String.valueOf(this.autoPost), false);
		memory.saveProperty("Telegram.autoPostHours", String.valueOf(this.autoPostHours), false);

		Vertex facebook = memory.createVertex(getPrimitive());
		facebook.unpinChildren();
		facebook.internalRemoveRelationships(Primitive.RSS);
		for (String text : this.postRSS) {
			Vertex rss =  memory.createVertex(text);
			facebook.addRelationship(Primitive.RSS, rss);
		}
		facebook.internalRemoveRelationships(Primitive.RSSKEYWORDS);
		for (String text : this.rssKeywords) {
			Vertex keywords =  memory.createVertex(text);
			facebook.addRelationship(Primitive.RSSKEYWORDS, keywords);
		}
		if (autoPosts != null) {
			Collection<Relationship> old = facebook.getRelationships(Primitive.AUTOPOSTS);
			if (old != null) {
				for (Relationship post : old) {
					if (post.getTarget().instanceOf(Primitive.FORMULA)) {
						SelfCompiler.getCompiler().unpin(post.getTarget());
					}
				}
			}
			facebook.internalRemoveRelationships(Primitive.AUTOPOSTS);
			for (String text : autoPosts) {
				Vertex post =  memory.createSentence(text);
				if (post.instanceOf(Primitive.FORMULA)) {
					SelfCompiler.getCompiler().pin(post);
				}
				post.addRelationship(Primitive.INSTANTIATION, Primitive.TWEET);
				facebook.addRelationship(Primitive.AUTOPOSTS, post);
			}
		}

		facebook.pinChildren();
		memory.save();
	}
	
	public void connect(String webhook) throws Exception {
		initProperties();
		log("Connecting to Telegram", Level.INFO);
		String json = Utils.httpGET("https://api.telegram.org/bot" + this.token + "/getMe");
		log("Telegram response", Level.FINE, new TextStream(json).nextLine());

		try {
			JSONObject root = new JSONObject(json);
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
			}
			log("Connected to Telegram", Level.INFO);
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
			JSONObject root = new JSONObject(json);
			JSONArray results = root.getJSONArray("result");
		    if (results != null && results.length() > 0) {
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
				while (results != null && results.length() > 0 && count < this.maxMessages) {
				    for (int index = 0; index < results.length(); index++) {
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
					root = new JSONObject(json);
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

	/**
	 * Reply to the message.
	 */
	public long checkMessage(JSONObject message, long last, long max, Network memory) {
		try {
	    	String id = message.getString("message_id");
	    	String conversationId = message.getJSONObject("chat").getString("id");
	    	String date = message.getString("date");
		    Date createdTime = new Date(((long)Integer.parseInt(date)) * 1000L);
	    	if ((System.currentTimeMillis() - createdTime.getTime()) > DAY) {
				log("Day old message", Level.FINE, createdTime, id, date);
	    		return max;
	    	}
	    	if (createdTime.getTime() > last) {
	    		JSONObject from = message.getJSONObject("from");
			    String fromUser = from.getString("first_name") + " " + from.getString("last_name");
			    String fromUserId = from.getString("id");
			    if (!fromUserId.equals(this.userId)) {
			    	if (message.get("text") == null) {
						log("Ignoring empty message", Level.INFO, fromUser, createdTime, conversationId);
			    	} else {
						String text = message.getString("text").trim();
						log("Processing message", Level.INFO, fromUser, createdTime, conversationId, text);
						this.messagesProcessed++;
						inputSentence(text, fromUser, this.userName, conversationId, memory);
				    	if (createdTime.getTime() > max) {
				    		max = createdTime.getTime();
				    	}
			    	}
			    } else {
					log("Ignoring own message", Level.FINE, createdTime, conversationId);
			    }
	    	}
		} catch (Exception exception) {
			log(exception);
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
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to twitter, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);
		Vertex target = output.mostConscious(Primitive.TARGET);
		String replyTo = target.mostConscious(Primitive.WORD).getData().toString();
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		Vertex id = conversation.getRelationship(Primitive.ID);
		String conversationId = id.printString();
		
		sendMessage(text, replyTo, conversationId);
	}
	
	public String sanitize(String text) {
		return Utils.stripTags(text);
	}

	/**
	 * Send a message to the user.
	 */
	public void sendMessage(String text, String replyUser, String conversationOrUserId) {
		log("Sending message:", Level.INFO, text, replyUser);
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("chat_id", conversationOrUserId);
			if (text.indexOf('<') != -1 && text.indexOf('>') != -1) {
				params.put("text", sanitize(text));
				params.put("parse_mode", "Html");
			} else {
				params.put("text", text);
			}
			//params.put("reply_to_message_id", messageId);

			Utils.httpPOST("https://api.telegram.org/bot" + this.token + "/sendMessage", params);
			
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
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
	public void inputSentence(String text, String userName, String targetUserName, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createSpeaker(userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);		
		input.addRelationship(Primitive.TARGET, self);
		user.addRelationship(Primitive.INPUT, input);
		
		Vertex conversation = network.createVertex(id);
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.DIRECTMESSAGE);
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