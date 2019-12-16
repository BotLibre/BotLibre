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

package org.botlibre.sense.slack;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.sense.http.Http;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Enables receiving a sending messages through Slack.
 */
public class Slack extends BasicSense {

	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	protected int maxErrors = 5;
	protected int errors;
	protected String token = "";
	protected String botUsername = "";
	protected String incomingWebhook = "";
	protected String appToken = "";
	
	protected boolean autoPost = false;
	protected int autoPostHours = 24;
	protected String autoPostChannel;
	protected String autoPostUsername;
	
	protected int maxFeed = 20;
	protected List<String> postRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();
	protected String rssChannel;
	protected String rssUsername;
	
	protected int messagesProcessed;
	protected int posts;
	
	protected boolean initProperties;
	
	private boolean enableEmotions = true;
	
	public Slack(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Slack() {
		this(false);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		this.token = this.bot.memory().getProperty("Slack.token");
		if (this.token == null) {
			this.token = "";
		}
		if (!this.token.isEmpty()) {
			setIsEnabled(true);
		}
		this.botUsername = this.bot.memory().getProperty("Slack.botUsername");
		if (this.botUsername == null) {
			this.botUsername = "";
		}
		this.incomingWebhook = this.bot.memory().getProperty("Slack.incomingWebhook");
		if (this.incomingWebhook == null) {
			this.incomingWebhook = "";
		}
		this.rssUsername = this.bot.memory().getProperty("Slack.rssUsername");
		if (this.rssUsername == null) {
			this.rssUsername = "";
		}
		this.rssChannel = this.bot.memory().getProperty("Slack.rssChannel");
		if (this.rssChannel == null) {
			this.rssChannel = "";
		}
		this.autoPostUsername = this.bot.memory().getProperty("Slack.autoPostUsername");
		if (this.autoPostUsername == null) {
			this.autoPostUsername = "";
		}
		this.autoPostChannel = this.bot.memory().getProperty("Slack.autoPostChannel");
		if (this.autoPostChannel == null) {
			this.autoPostChannel = "";
		}
		
		this.appToken = this.bot.memory().getProperty("Slack.appToken");
		if (this.appToken == null) {
			this.appToken = "";
		}
	}
	
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
	public String getBotUsername() {
		return botUsername;
	}
	
	public void setBotUsername(String botUsername) {
		this.botUsername = botUsername;
	}
	
	public String getRssUsername() {
		return rssUsername;
	}
	
	public void setRssUsername(String rssUsername) {
		this.rssUsername = rssUsername;
	}
	
	public String getRssChannel() {
		return rssChannel;
	}
	
	public void setRssChannel(String rssChannel) {
		this.rssChannel = rssChannel;
	}
	
	public String getAutoPostUsername() {
		return autoPostUsername;
	}
	
	public void setAutoPostUsername(String autoPostUsername) {
		this.autoPostUsername = autoPostUsername;
	}
	
	public String getAutoPostChannel() {
		return autoPostChannel;
	}
	
	public void setAutoPostChannel(String rssChannel) {
		this.autoPostChannel = rssChannel;
	}
	
	public String getIncomingWebhook() {
		return incomingWebhook;
	}

	public void setIncomingWebhook(String incomingWebhook) {
		this.incomingWebhook = incomingWebhook;
	}
	
	public int getMaxFeed() {
		return maxFeed;
	}

	public void setMaxFeed(int maxFeed) {
		this.maxFeed = maxFeed;
	}
	
	public List<String> getRssKeywords() {
		initProperties();
		return rssKeywords;
	}

	public void setRssKeywords(List<String> rssKeywords) {
		initProperties();
		this.rssKeywords = rssKeywords;
	}
	
	public List<String> getPostRSS() {
		initProperties();
		return postRSS;
	}

	public void setPostRSS(List<String> postRSS) {
		initProperties();
		this.postRSS = postRSS;
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
	
	public String getAppToken() {
		return appToken;
	}

	public void setAppToken(String appToken) {
		this.appToken = appToken;
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
			getBot().memory().loadProperties("Slack");
			Network memory = getBot().memory().newMemory();
			Vertex slack = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("Slack.token");
			if (property != null) {
				this.token = property;
			}
			property = this.bot.memory().getProperty("Slack.botUsername");
			if (property != null) {
				this.botUsername = property;
			}
			property = this.bot.memory().getProperty("Slack.incomingWebhook");
			if (property != null) {
				this.incomingWebhook = property;
			}
			property = this.bot.memory().getProperty("Slack.autoPost");
			if (property != null) {
				this.autoPost = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Slack.autoPostHours");
			if (property != null) {
				this.autoPostHours = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Slack.rssUsername");
			if (property != null) {
				this.rssUsername = property;
			}
			property = this.bot.memory().getProperty("Slack.rssChannel");
			if (property != null) {
				this.rssChannel = property;
			}
			property = this.bot.memory().getProperty("Slack.autoPostUsername");
			if (property != null) {
				this.autoPostUsername = property;
			}
			property = this.bot.memory().getProperty("Slack.autoPostChannel");
			if (property != null) {
				this.autoPostChannel = property;
			}
			
			property = this.bot.memory().getProperty("Slack.appToken");
			if (property != null) {
				this.appToken = property;
			}
			
			this.postRSS = new ArrayList<String>();
			List<Relationship> rss = slack.orderedRelationships(Primitive.RSS);
			if (rss != null) {
				for (Relationship relationship : rss) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.postRSS.add(text);
					}
				}
			}
			this.rssKeywords = new ArrayList<String>();
			List<Relationship> keywords = slack.orderedRelationships(Primitive.RSSKEYWORDS);
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
		memory.saveProperty("Slack.token", this.token, true);
		memory.saveProperty("Slack.botUsername", this.botUsername, true);
		memory.saveProperty("Slack.incomingWebhook", this.incomingWebhook, true);
		memory.saveProperty("Slack.autoPost", String.valueOf(this.autoPost), false);
		memory.saveProperty("Slack.autoPostHours", String.valueOf(this.autoPostHours), false);
		memory.saveProperty("Slack.rssUsername", this.rssUsername, true);
		memory.saveProperty("Slack.rssChannel", this.rssChannel, true);
		memory.saveProperty("Slack.autoPostUsername", this.autoPostUsername, true);
		memory.saveProperty("Slack.autoPostChannel", this.autoPostChannel, true);
		
		memory.saveProperty("Slack.appToken", this.appToken, true);
		
		Vertex slack = memory.createVertex(getPrimitive());
		slack.unpinChildren();
		slack.internalRemoveRelationships(Primitive.RSS);
		for (String text : this.postRSS) {
			Vertex rss =  memory.createVertex(text);
			slack.addRelationship(Primitive.RSS, rss);
		}
		slack.internalRemoveRelationships(Primitive.RSSKEYWORDS);
		for (String text : this.rssKeywords) {
			Vertex keywords =  memory.createVertex(text);
			slack.addRelationship(Primitive.RSSKEYWORDS, keywords);
		}
		if (autoPosts != null) {
			Collection<Relationship> old = slack.getRelationships(Primitive.AUTOPOSTS);
			if (old != null) {
				for (Relationship post : old) {
					if (post.getTarget().instanceOf(Primitive.FORMULA)) {
						SelfCompiler.getCompiler().unpin(post.getTarget());
					}
				}
			}
			slack.internalRemoveRelationships(Primitive.AUTOPOSTS);
			for (String text : autoPosts) {
				Vertex post =  memory.createSentence(text);
				if (post.instanceOf(Primitive.FORMULA)) {
					SelfCompiler.getCompiler().pin(post);
				}
				post.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
				slack.addRelationship(Primitive.AUTOPOSTS, post);
			}
		}
		
		slack.pinChildren();
		memory.save();
	}

	/**
	 * Process to the message and reply synchronously.
	 */
	public String processMessage(String fromId, String from, String id, String message, String token) {
		log("Processing message", Level.INFO, from, message);
		
		if (!token.equals(this.token) && !token.equals(this.appToken)) {
			return "";
		}
		
		String targetUsername = "";
		// Check if message is directed to the bot.
		if (botUsername.contains(" ") && message.toLowerCase().contains(botUsername.toLowerCase())) {
			targetUsername = botUsername;
		} else {
			TextStream stream = new TextStream(message);
			String word = stream.nextWord();
			while (word != null) {
				word.replace("@", "");
				word.replace(":", "");
				word.replace(",", "");
				word.replace(".", "");
				word.replace("?", "");
				if(word.toLowerCase().equals(botUsername.toLowerCase())) {
					targetUsername = botUsername;
					break;
				}
				word = stream.nextWord();
			}
		}
			
		// Check if message is directed to someone other than the bot.
		if (targetUsername.isEmpty()) {
			if (message.startsWith("@") && message.contains(" ")) {
				targetUsername = message.substring(1, message.indexOf(" "));
				if (targetUsername.endsWith(":")) {
					targetUsername = targetUsername.substring(0, targetUsername.length()-2);
				}
			}
		}
		
		//Check if target is special slack target
		if (targetUsername.equals("everyone") || targetUsername.equals("here") || targetUsername.equals("channel")) {
			targetUsername = botUsername;
		}
		
		this.responseListener = new ResponseListener();
		Network memory = bot.memory().newMemory();
		this.messagesProcessed++;
		inputSentence(message, fromId, from, targetUsername, id, memory);
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
	public void inputSentence(String text, String userId, String userName, String targetUsername, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createUniqueSpeaker(new Primitive(userName), Primitive.SLACK, userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);
		
		if (targetUsername.equals(this.botUsername)) {
			input.addRelationship(Primitive.TARGET, self);
		} else if (!targetUsername.isEmpty()) {
			input.addRelationship(Primitive.TARGET, network.createUniqueSpeaker(new Primitive(targetUsername), Primitive.SLACK, targetUsername));
		}

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
		conversation.addRelationship(Primitive.TYPE, Primitive.SLACK);
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SLACK);
		return input;
	}
	
	/**
	 * Output the Slack message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to slack, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);	
		
		if (this.enableEmotions) {
			text += addEmotion(output);
		}
		
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
	
	/**
	 * Auto post to channel.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.INFO);
		try {
			initProperties();
			checkRSS();
			checkAutoPost();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.INFO);
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
			Vertex slack = memory.createVertex(getPrimitive());
			Vertex vertex = slack.getRelationship(Primitive.LASTRSS);
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
				List<Map<String, Object>> feed = getBot().awareness().getSense(Http.class).parseRSSFeed(Utils.safeURL(url), last);
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
							post(text + " " + entry.get("link"), rssUsername, rssChannel);
					    	Utils.sleep(500);
							count++;
					    	if (time > max) {
					    		max = time;
					    	}
				    	}
				    }
				    if (max != 0) {
						slack.setRelationship(Primitive.LASTRSS, memory.createVertex(max));
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
			Vertex slack = memory.createVertex(getPrimitive());
			Vertex vertex = slack.getRelationship(Primitive.LASTPOST);
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
					post(text, autoPostUsername, autoPostChannel);
			    	Utils.sleep(100);
					slack.setRelationship(Primitive.LASTPOST, memory.createTimestamp());
			    	memory.save();
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public void post(String text, String username, String channel) {
		this.posts++;
		initProperties();
		log("Posting ", Level.INFO, text);
		try {
			JSONObject jsonPayload = new JSONObject();
			
			if(text!=null && !text.isEmpty()) {
				jsonPayload.put("text", text);
			} else {
				return;
			}
			if(username!=null && !username.isEmpty()) {
				jsonPayload.put("username", username);
			}
			if(channel!=null && !channel.isEmpty()) {
				jsonPayload.put("channel", channel);
			}
			
			Map<String, String> params = new HashMap<String, String>();

			params.put("payload", jsonPayload.toString());
			
			Utils.httpPOST(this.incomingWebhook, params);
			
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
	}
	
	public void post(String text)
	{
		post(text, null, null);
	}
	
	public int getPosts() {
		return posts;
	}
	
	public void setPosts(int posts) {
		this.posts = posts;
	}
	
	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}
	
	private String addEmotion(Vertex output){
		
		//EmotionalState emotion = this.getEmotionalState();
		EmotionalState emotion = this.bot.mood().evaluateEmotionalState(output);
		
		switch(emotion)
		{
		case NONE:
			return "";
		case AFRAID:
			return " :scream:";
		case ANGER:
			return " :angry:";
		case BORED:
			return " :sleepy:";
		case CALM:
			return " :pensive:";
		case CONFIDENT:
			return " :sunglasses:";
		case COURAGEOUS:
			return " :triumph:";
		case CRYING:
			return " :cry:";
		case DISLIKE:
			return " :unamused:";
		case ECSTATIC:
			return " :joy:";
		case HAPPY:
			return " :smile:";
		case HATE:
			return " :disappointed:";
		case LAUGHTER:
			return " :laughing:";
		case LIKE:
			return " :smiley:";
		case LOVE:
			return " :heart_eyes:";
		case PANIC:
			return " :cold_sweat:";
		case RAGE:
			return " :rage:";
		case SAD:
			return " :slightly_frowning_face:";
		case SERENE:
			return " :relieved:";
		case SERIOUS:
			return " :neutral_face:";
		case SURPRISE:
			return " :open_mouth:";
		default:
			return "";	
		}
	}
	
	public void processSlackEvent(String json) {
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		JSONObject event = root.getJSONObject("event");
		String token = root.getString("token");
		
		if(event.getString("type").equals("message")) {
			String user = event.getString("user");
			String channel = event.getString("channel");
			String text = event.getString("text");
			
			String reply = this.processMessage(null, user, channel, text, token);
			
			if (reply == null || reply.isEmpty()) {
				return;
			}
			
			String data = "token=" + this.appToken;
			data += "&channel=" + channel;
			data += "&text=" + "@" + user + " " + reply;
			
			this.callSlackWebAPI("chat.postMessage", data);
		}
	}
	
	private void callSlackWebAPI(String function, String data) {
		try {
			Utils.httpPOST("https://slack.com/api/" + function, "application/x-www-form-urlencoded", data);
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
	}
}
