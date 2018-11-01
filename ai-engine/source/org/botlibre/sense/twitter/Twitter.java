/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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
package org.botlibre.sense.twitter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.Bot;
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trends;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.SearchResource;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Enables receiving a sending messages through Twitter.
 */
public class Twitter extends BasicSense {
	public static int TREND_CHECK = 1000 * 60 * 60 * 1; // 1 hour.
	public static int MAX_LOOKUP = 100;
	public static String oauthKey = "key";
	public static String oauthSecret = "secret";
	
	protected String userName = "";
	protected String token = "";
	protected String tokenSecret = "";
	
	protected boolean initProperties;
	
	protected boolean autoFollow = false;
	protected boolean autoFollowFriendsFriends = false;
	protected boolean autoFollowFriendsFollowers = false;
	protected boolean followMessages = false;
	protected String welcomeMessage = "";
	protected int maxFriends = 100;
	protected int maxFriendsPerCycle = 5;
	protected int maxWelcomesPerCycle = 20;
	protected int maxPage = 5;
	protected int maxStatus = 20;
	protected int maxFeed = 20;
	protected int maxSearch = 20;
	protected int maxErrors = 5;
	protected int errors;
	protected boolean processStatus = false;
	protected boolean listenStatus = false;
	protected boolean tweetChats = true;
	protected boolean replyToMentions = true;
	protected boolean replyToMessages = true;
	protected boolean ignoreReplies = true;
	protected boolean autoTweet = false;
	protected boolean learn = false;
	protected boolean learnFromSelf = false;
	protected int autoTweetHours = 24;
	protected List<String> retweet = new ArrayList<String>();
	protected List<String> tweetRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();
	protected List<String> tweetSearch = new ArrayList<String>();
	protected List<String> statusKeywords = new ArrayList<String>();
	protected List<String> autoFollowKeywords = new ArrayList<String>();
	protected List<String> autoFollowSearch = new ArrayList<String>();

	protected boolean checkTrends = false;
	protected long lastTrendsCheck;
	protected Set<Long> processedTweets = new HashSet<Long>();
	
	protected int tweets;
	protected int tweetsProcessed;
	protected int retweets;

	protected twitter4j.Twitter connection;	
	
	public Twitter(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Twitter() {
		this(false);
	}
	
	public boolean getListenStatus() {
		initProperties();
		return listenStatus;
	}

	public void setListenStatus(boolean listenStatus) {
		initProperties();
		this.listenStatus = listenStatus;
	}

	public int getTweets() {
		return tweets;
	}

	public void setTweets(int tweets) {
		this.tweets = tweets;
	}

	public int getTweetsProcessed() {
		return tweetsProcessed;
	}

	public void setTweetsProcessed(int tweetsProcessed) {
		this.tweetsProcessed = tweetsProcessed;
	}

	public int getRetweets() {
		return retweets;
	}

	public void setRetweets(int retweets) {
		this.retweets = retweets;
	}

	public String getWelcomeMessage() {
		initProperties();
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		initProperties();
		this.welcomeMessage = welcomeMessage;
	}

	public List<String> getRssKeywords() {
		initProperties();
		return rssKeywords;
	}

	public void setRssKeywords(List<String> rssKeywords) {
		initProperties();
		this.rssKeywords = rssKeywords;
	}

	public int getMaxSearch() {
		initProperties();
		return maxSearch;
	}

	public void setMaxSearch(int maxSearch) {
		initProperties();
		this.maxSearch = maxSearch;
	}

	public List<String> getAutoFollowSearch() {
		initProperties();
		return autoFollowSearch;
	}

	public void setAutoFollowSearch(List<String> autoFollowSearch) {
		initProperties();
		this.autoFollowSearch = autoFollowSearch;
	}

	public boolean getAutoFollowFriendsFriends() {
		initProperties();
		return autoFollowFriendsFriends;
	}

	public void setAutoFollowFriendsFriends(boolean autoFollowFriendsFriends) {
		initProperties();
		this.autoFollowFriendsFriends = autoFollowFriendsFriends;
	}

	public boolean getAutoFollowFriendsFollowers() {
		initProperties();
		return autoFollowFriendsFollowers;
	}

	public void setAutoFollowFriendsFollowers(boolean autoFollowFriendsFollowers) {
		initProperties();
		this.autoFollowFriendsFollowers = autoFollowFriendsFollowers;
	}

	public int getMaxFeed() {
		initProperties();
		return maxFeed;
	}

	public void setMaxFeed(int maxFeed) {
		initProperties();
		this.maxFeed = maxFeed;
	}

	public boolean getAutoTweet() {
		initProperties();
		return autoTweet;
	}

	public void setAutoTweet(boolean autoTweet) {
		initProperties();
		this.autoTweet = autoTweet;
	}

	public int getAutoTweetHours() {
		initProperties();
		return autoTweetHours;
	}

	public void setAutoTweetHours(int autoTweetHours) {
		initProperties();
		this.autoTweetHours = autoTweetHours;
	}

	public List<Vertex> getAutoTweets(Network network) {
		return network.createVertex(getPrimitive()).orderedRelations(Primitive.AUTOTWEETS);
	}

	public boolean getIgnoreReplies() {
		initProperties();
		return ignoreReplies;
	}

	public void setIgnoreReplies(boolean ignoreReplies) {
		initProperties();
		this.ignoreReplies = ignoreReplies;
	}

	public List<String> getStatusKeywords() {
		initProperties();
		return statusKeywords;
	}

	public void setStatusKeywords(List<String> statusKeywords) {
		initProperties();
		this.statusKeywords = statusKeywords;
	}

	public List<String> getAutoFollowKeywords() {
		initProperties();
		return autoFollowKeywords;
	}

	public void setAutoFollowKeywords(List<String> autoFollowKeywords) {
		initProperties();
		this.autoFollowKeywords = autoFollowKeywords;
	}

	/**
	 * Authorise a new account to be accessible by Bot.
	 * Return the request token that contains the URL that the user must use to authorise twitter.
	 */
	public RequestToken authorizeAccount() throws TwitterException {
		twitter4j.Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(getOauthKey(), getOauthSecret());
		RequestToken requestToken = twitter.getOAuthRequestToken();
		setConnection(twitter);
		return requestToken;
	}
	
	/**
	 * Authorise a new account to be accessible by Bot.
	 */
	public void authorizeComplete() throws TwitterException {
		AccessToken token = getConnection().getOAuthAccessToken();
		setToken(token.getToken());
		setTokenSecret(token.getTokenSecret());
	}
	
	/**
	 * Authorise a new account to be accessible by Bot.
	 */
	public void authorizeComplete(String pin) throws TwitterException {
		AccessToken token = getConnection().getOAuthAccessToken(pin);
		setToken(token.getToken());
		setTokenSecret(token.getTokenSecret());
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		String user = this.bot.memory().getProperty("Twitter.user");
		if (user != null) {
			this.userName = user;
		}
		String token = this.bot.memory().getProperty("Twitter.token");
		if (token != null) {
			this.token = token;
		}
		String secret = this.bot.memory().getProperty("Twitter.secret");
		if (secret != null) {
			String data = secret;
			// Check if encrypted from && prefix.
			if (data.startsWith("&&")) {
				try {
					this.tokenSecret = Utils.decrypt(Utils.KEY, data.substring(2, data.length()));
				} catch (Exception exception) {
					this.tokenSecret = data;
				}
			} else {
				this.tokenSecret = data;
			}
			setIsEnabled(true);
		}
		String property = this.bot.memory().getProperty("Twitter.tweetChats");
		if (property != null) {
			this.tweetChats = Boolean.valueOf(property);
		}
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex twitter = memory.createVertex(getPrimitive());
		Vertex user = twitter.getRelationship(Primitive.USER);
		if (user != null) {
			this.userName = (String)user.getData();
		}
		Vertex token = twitter.getRelationship(Primitive.TOKEN);
		if (token != null) {
			this.token = (String)token.getData();
		}
		Vertex secret = twitter.getRelationship(Primitive.SECRET);
		if (secret != null) {
			String data = (String)secret.getData();
			// Check if encrypted from && prefix.
			if (data.startsWith("&&")) {
				try {
					this.tokenSecret = Utils.decrypt(Utils.KEY, data.substring(2, data.length()));
				} catch (Exception exception) {
					this.tokenSecret = data;
				}				
			} else {
				this.tokenSecret = data;
			}
			setIsEnabled(true);
		}
		Vertex property = twitter.getRelationship(Primitive.TWEETCHATS);
		if (property != null) {
			this.tweetChats = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.WELCOME);
		if (property != null) {
			this.welcomeMessage = (String)property.getData();
		}
		property = twitter.getRelationship(Primitive.AUTOFOLLOW);
		if (property != null) {
			this.autoFollow = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.AUTOFOLLOWFRIENDSFRIENDS);
		if (property != null) {
			this.autoFollowFriendsFriends = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.AUTOFOLLOWFRIENDSFOLLOWERS);
		if (property != null) {
			this.autoFollowFriendsFollowers = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.FOLLOWMESSAGES);
		if (property != null) {
			this.followMessages = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.MAXFRIENDS);
		if (property != null) {
			this.maxFriends = ((Number)property.getData()).intValue();
		}
		property = twitter.getRelationship(Primitive.MAXSTATUSCHECKS);
		if (property != null) {
			this.maxStatus = ((Number)property.getData()).intValue();
		}
		property = twitter.getRelationship(Primitive.PROCESSSTATUS);
		if (property != null) {
			this.processStatus = (Boolean)property.getData();
		}
		this.statusKeywords = new ArrayList<String>();
		List<Relationship> keywords = twitter.orderedRelationships(Primitive.STATUSKEYWORDS);
		if (keywords != null) {
			for (Relationship relationship : keywords) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.statusKeywords.add(text);
				}
			}
		}
		this.retweet = new ArrayList<String>();
		keywords = twitter.orderedRelationships(Primitive.RETWEET);
		if (keywords != null) {
			for (Relationship relationship : keywords) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.retweet.add(text);
				}
			}
		}
		this.autoFollowKeywords = new ArrayList<String>();
		List<Relationship> search = twitter.orderedRelationships(Primitive.AUTOFOLLOWKEYWORDS);
		if (search != null) {
			for (Relationship relationship : search) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.autoFollowKeywords.add(text);
				}
			}
		}
		this.autoFollowSearch = new ArrayList<String>();
		search = twitter.orderedRelationships(Primitive.AUTOFOLLOWSEARCH);
		if (search != null) {
			for (Relationship relationship : search) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.autoFollowSearch.add(text);
				}
			}
		}
		this.tweetSearch = new ArrayList<String>();
		search = twitter.orderedRelationships(Primitive.TWEETSEARCH);
		if (search != null) {
			for (Relationship relationship : search) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.tweetSearch.add(text);
				}
			}
		}
		this.tweetRSS = new ArrayList<String>();
		List<Relationship> rss = twitter.orderedRelationships(Primitive.TWEETRSS);
		if (rss != null) {
			for (Relationship relationship : rss) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.tweetRSS.add(text);
				}
			}
		}
		this.rssKeywords = new ArrayList<String>();
		keywords = twitter.orderedRelationships(Primitive.RSSKEYWORDS);
		if (keywords != null) {
			for (Relationship relationship : keywords) {
				String text = ((String)relationship.getTarget().getData()).trim();
				this.rssKeywords.add(text);
			}
		}
		property = twitter.getRelationship(Primitive.REPLYTOMENTIONS);
		if (property != null) {
			this.replyToMentions = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.REPLYTOMESSAGES);
		if (property != null) {
			this.replyToMessages = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.AUTOTWEET);
		if (property != null) {
			this.autoTweet = (Boolean)property.getData();
		}
		property = twitter.getRelationship(Primitive.AUTOTWEETHOURS);
		if (property != null) {
			this.autoTweetHours = ((Number)property.getData()).intValue();
		}
		
		// Remove old properties.
		twitter.internalRemoveRelationships(Primitive.USER);
		twitter.internalRemoveRelationships(Primitive.TOKEN);
		twitter.internalRemoveRelationships(Primitive.SECRET);
		twitter.internalRemoveRelationships(Primitive.TWEETCHATS);
		twitter.internalRemoveRelationships(Primitive.WELCOME);
		twitter.internalRemoveRelationships(Primitive.AUTOFOLLOW);
		twitter.internalRemoveRelationships(Primitive.AUTOFOLLOWFRIENDSFOLLOWERS);
		twitter.internalRemoveRelationships(Primitive.FOLLOWMESSAGES);
		twitter.internalRemoveRelationships(Primitive.MAXFRIENDS);
		twitter.internalRemoveRelationships(Primitive.MAXSTATUSCHECKS);
		twitter.internalRemoveRelationships(Primitive.PROCESSSTATUS);
		twitter.internalRemoveRelationships(Primitive.REPLYTOMENTIONS);
		twitter.internalRemoveRelationships(Primitive.REPLYTOMESSAGES);
		twitter.internalRemoveRelationships(Primitive.AUTOTWEET);
		twitter.internalRemoveRelationships(Primitive.AUTOTWEETHOURS);
		
		memory.save();
		
		saveProperties(null);
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
			getBot().memory().loadProperties("Twitter");
			String property = this.bot.memory().getProperty("Twitter.welcomeMessage");
			if (property != null) {
				this.welcomeMessage = property;
			}
			property = this.bot.memory().getProperty("Twitter.autoFollow");
			if (property != null) {
				this.autoFollow = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.autoFollowFriendsFriends");
			if (property != null) {
				this.autoFollowFriendsFriends = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.autoFollowFriendsFollowers");
			if (property != null) {
				this.autoFollowFriendsFollowers = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.followMessages");
			if (property != null) {
				this.followMessages = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.maxFriends");
			if (property != null) {
				this.maxFriends = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.maxStatus");
			if (property != null) {
				this.maxStatus = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.maxSearch");
			if (property != null) {
				this.maxSearch = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.processStatus");
			if (property != null) {
				this.processStatus = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.listenStatus");
			if (property != null) {
				this.listenStatus = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.learn");
			if (property != null) {
				this.learn = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.learnFromSelf");
			if (property != null) {
				this.learnFromSelf = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.replyToMentions");
			if (property != null) {
				this.replyToMentions = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.replyToMessages");
			if (property != null) {
				this.replyToMessages = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.ignoreReplies");
			if (property != null) {
				this.ignoreReplies = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.autoTweet");
			if (property != null) {
				this.autoTweet = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Twitter.autoTweetHours");
			if (property != null) {
				this.autoTweetHours = Integer.valueOf(property);
			}

			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			this.statusKeywords = new ArrayList<String>();
			List<Relationship> keywords = twitter.orderedRelationships(Primitive.STATUSKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.statusKeywords.add(text);
					}
				}
			}
			this.retweet = new ArrayList<String>();
			keywords = twitter.orderedRelationships(Primitive.RETWEET);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.retweet.add(text);
					}
				}
			}
			this.autoFollowKeywords = new ArrayList<String>();
			List<Relationship> search = twitter.orderedRelationships(Primitive.AUTOFOLLOWKEYWORDS);
			if (search != null) {
				for (Relationship relationship : search) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.autoFollowKeywords.add(text);
					}
				}
			}
			this.autoFollowSearch = new ArrayList<String>();
			search = twitter.orderedRelationships(Primitive.AUTOFOLLOWSEARCH);
			if (search != null) {
				for (Relationship relationship : search) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.autoFollowSearch.add(text);
					}
				}
			}
			this.tweetSearch = new ArrayList<String>();
			search = twitter.orderedRelationships(Primitive.TWEETSEARCH);
			if (search != null) {
				for (Relationship relationship : search) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.tweetSearch.add(text);
					}
				}
			}
			this.tweetRSS = new ArrayList<String>();
			List<Relationship> rss = twitter.orderedRelationships(Primitive.TWEETRSS);
			if (rss != null) {
				for (Relationship relationship : rss) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.tweetRSS.add(text);
					}
				}
			}
			this.rssKeywords = new ArrayList<String>();
			keywords = twitter.orderedRelationships(Primitive.RSSKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					this.rssKeywords.add(text);
				}
			}
			
			this.initProperties = true;
		}
	}

	public void saveProperties(List<String> autoTweets) {
		Network memory = getBot().memory().newMemory();
		Vertex twitter = memory.createVertex(getPrimitive());
		twitter.unpinChildren();

		memory.saveProperty("Twitter.user", this.userName, true);
		memory.saveProperty("Twitter.token", this.token, true);
		memory.saveProperty("Twitter.secret", "&&" + Utils.encrypt(Utils.KEY, this.tokenSecret), true);
		memory.saveProperty("Twitter.tweetChats", String.valueOf(this.tweetChats), true);

		memory.saveProperty("Twitter.welcomeMessage", this.welcomeMessage, false);
		memory.saveProperty("Twitter.autoFollow", String.valueOf(this.autoFollow), false);
		memory.saveProperty("Twitter.autoFollowFriendsFriends", String.valueOf(this.autoFollowFriendsFriends), false);
		memory.saveProperty("Twitter.autoFollowFriendsFollowers", String.valueOf(this.autoFollowFriendsFollowers), false);
		memory.saveProperty("Twitter.followMessages", String.valueOf(this.followMessages), false);
		memory.saveProperty("Twitter.maxFriends", String.valueOf(this.maxFriends), false);
		memory.saveProperty("Twitter.maxStatus", String.valueOf(this.maxStatus), false);
		memory.saveProperty("Twitter.maxSearch", String.valueOf(this.maxSearch), false);
		memory.saveProperty("Twitter.processStatus", String.valueOf(this.processStatus), false);
		memory.saveProperty("Twitter.listenStatus", String.valueOf(this.listenStatus), false);
		memory.saveProperty("Twitter.learn", String.valueOf(this.learn), false);
		memory.saveProperty("Twitter.learnFromSelf", String.valueOf(this.learnFromSelf), false);
		memory.saveProperty("Twitter.replyToMentions", String.valueOf(this.replyToMentions), false);
		memory.saveProperty("Twitter.replyToMessages", String.valueOf(this.replyToMessages), false);
		memory.saveProperty("Twitter.ignoreReplies", String.valueOf(this.ignoreReplies), false);
		memory.saveProperty("Twitter.autoTweet", String.valueOf(this.autoTweet), false);
		memory.saveProperty("Twitter.autoTweetHours", String.valueOf(this.autoTweetHours), false);
		
		twitter.internalRemoveRelationships(Primitive.STATUSKEYWORDS);
		for (String text : this.statusKeywords) {
			Vertex keywords =  memory.createVertex(text);
			twitter.addRelationship(Primitive.STATUSKEYWORDS, keywords);
		}
		twitter.internalRemoveRelationships(Primitive.RETWEET);
		for (String text : this.retweet) {
			Vertex keywords =  memory.createVertex(text);
			twitter.addRelationship(Primitive.RETWEET, keywords);
		}
		twitter.internalRemoveRelationships(Primitive.AUTOFOLLOWKEYWORDS);
		for (String text : this.autoFollowKeywords) {
			Vertex search =  memory.createVertex(text);
			twitter.addRelationship(Primitive.AUTOFOLLOWKEYWORDS, search);
		}
		twitter.internalRemoveRelationships(Primitive.AUTOFOLLOWSEARCH);
		for (String text : this.autoFollowSearch) {
			Vertex search =  memory.createVertex(text);
			twitter.addRelationship(Primitive.AUTOFOLLOWSEARCH, search);
		}
		twitter.internalRemoveRelationships(Primitive.TWEETSEARCH);
		for (String text : this.tweetSearch) {
			Vertex search =  memory.createVertex(text);
			twitter.addRelationship(Primitive.TWEETSEARCH, search);
		}
		twitter.internalRemoveRelationships(Primitive.TWEETRSS);
		for (String text : this.tweetRSS) {
			Vertex rss =  memory.createVertex(text);
			twitter.addRelationship(Primitive.TWEETRSS, rss);
		}
		twitter.internalRemoveRelationships(Primitive.RSSKEYWORDS);
		for (String text : this.rssKeywords) {
			Vertex keywords =  memory.createVertex(text);
			twitter.addRelationship(Primitive.RSSKEYWORDS, keywords);
		}
		if (autoTweets != null) {
			Collection<Relationship> old = twitter.getRelationships(Primitive.AUTOTWEETS);
			if (old != null) {
				for (Relationship tweet : old) {
					if (tweet.getTarget().instanceOf(Primitive.FORMULA)) {
						SelfCompiler.getCompiler().unpin(tweet.getTarget());
					}
				}
			}
			twitter.internalRemoveRelationships(Primitive.AUTOTWEETS);
			for (String text : autoTweets) {
				Vertex tweet =  memory.createSentence(text);
				if (tweet.instanceOf(Primitive.FORMULA)) {
					SelfCompiler.getCompiler().pin(tweet);
				}
				tweet.addRelationship(Primitive.INSTANTIATION, Primitive.TWEET);
				twitter.addRelationship(Primitive.AUTOTWEETS, tweet);
			}
		}

		twitter.pinChildren();
		memory.save();
	}
	
	public void connect() throws TwitterException {
		initProperties();
		ConfigurationBuilder config = new ConfigurationBuilder();
		config.setOAuthConsumerKey(getOauthKey());
		config.setOAuthConsumerSecret(getOauthSecret());
		config.setOAuthAccessToken(getToken());
		config.setOAuthAccessTokenSecret(getTokenSecret());
		twitter4j.Twitter twitter = new TwitterFactory(config.build()).getInstance();
		User user = twitter.verifyCredentials();
		if (!this.userName.equals(user.getScreenName())) {
			this.userName = user.getScreenName();
			saveProperties(null);
		}
		//AccessToken accessToken = new AccessToken(getToken(), getTokenSecret());
		//twitter4j.Twitter twitter = new TwitterFactory().getInstance(accessToken);
		//twitter4j.Twitter twitter = new TwitterFactory().getInstance(getOauthKey(), getOauthSecret(), accessToken);
		//twitter4j.Twitter twitter = new TwitterFactory().getInstance(getUsername(), getPassword());
		setConnection(twitter);
	}

	/**
	 * Check profile for messages.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.INFO);
		this.processedTweets = new HashSet<Long>();
		try {
			if (getConnection() == null) {
				connect();
			}
			if (this.checkTrends) {
				checkTrends();
			}
			checkFollowers();
			checkStatus();
			checkMentions();
			checkSearch();
			checkRSS();
			checkAutoTweet();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.INFO);
	}

	/**
	 * Add the follower.
	 */
	public void addFriend(String friend) {
		try {
			getConnection().createFriendship(friend);
		} catch (TwitterException exception) {
			log(exception);
		}
	}

	/**
	 * Add the follower.
	 */
	public void removeFriend(String friend) {
		try {
			getConnection().destroyFriendship(friend);
		} catch (TwitterException exception) {
			log(exception);
		}
	}

	/**
	 * Check status.
	 */
	public void checkStatus() {
		if (!getProcessStatus()) {
			return;
		}
		log("Checking status", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTTIMELINE);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			long max = 0;
			ResponseList<Status> timeline = null;
			boolean more = true;
			int page = 1;
			int count = 0;
			this.errors = 0;
			while (more && (count <= this.maxStatus) && page <= this.maxPage) {
				if (last == 0) {
					timeline = getConnection().getHomeTimeline();
					more = false;
				} else {
					Paging paging = new Paging(page, last);
					timeline = getConnection().getHomeTimeline(paging);
					if ((timeline == null) || (timeline.size() < 20)) {
						more = false;
					}
					page++;
				}
				if ((timeline == null) || timeline.isEmpty()) {
					break;
				}
				log("Processing status", Level.INFO, timeline.size());
				for (int index = timeline.size() - 1; index >= 0; index--) {
					if (count >= this.maxStatus) {
						break;
					}
					if (this.errors > this.maxErrors) {
						break;
					}
					Status status = timeline.get(index);
					String name = status.getUser().getScreenName();
					if (!name.equals(this.userName)) {
						long statusTime = status.getCreatedAt().getTime();
						long statusId = status.getId();
						if (statusId > max) {
							max = statusId;
						}
						if ((System.currentTimeMillis() - statusTime) > DAY) {
							log("Day old status", Level.INFO, statusId, statusTime);
							more = false;
							continue;
						}
						if (statusId > last) {
							if (Utils.checkProfanity(status.getText())) {
								continue;
							}
							boolean match = false;
							List<String> statusWords = new TextStream(status.getText().toLowerCase()).allWords();
							if (getListenStatus()) {
								this.languageState = LanguageState.Listening;
								match = true;
							} else {
								for (String text : getStatusKeywords()) {
									List<String> keywords = new TextStream(text.toLowerCase()).allWords();
									if (!keywords.isEmpty() && statusWords.containsAll(keywords)) {
										match = true;
										break;
									}
								}
							}
							if (getLearn()) {
								learnTweet(status, true, true, memory);
							}
							if (match) {
								count++;
								input(status);
								Utils.sleep(500);
							} else {
								log("Skipping status, missing keywords", Level.FINE, status.getText());
								if (!status.isRetweet() && !status.getUser().isProtected() && !status.isRetweetedByMe()) {
									boolean retweeted = false;
									// Check retweet.
									for (String keywords : getRetweet()) {
										List<String> keyWords = new TextStream(keywords.toLowerCase()).allWords();
										if (!keyWords.isEmpty()) {
											if (statusWords.containsAll(keyWords)) {
												retweeted = true;
												count++;
												retweet(status);
												Utils.sleep(500);
												break;
											}
										}
									}
									if (!retweeted) {
										log("Skipping rewteet, missing keywords", Level.FINE, status.getText());										
									}
								} else if (!getRetweet().isEmpty()) {
									if (status.isRetweet()) {
										log("Skipping rewteet", Level.FINE, status.getText());
									} else if (status.getUser().isProtected()) {
										log("Skipping protected user", Level.FINE, status.getText());
									} else if (status.isRetweetedByMe()) {
										log("Skipping already retweeted", Level.FINE, status.getText());
									}
								}
							}
						} else {
							log("Old status", Level.INFO, statusId, statusTime);							
						}
					}
				}
			}
			if (max != 0) {
				twitter.setRelationship(Primitive.LASTTIMELINE, memory.createVertex(max));
				memory.save();
			}
		} catch (Exception exception) {
			log(exception);
		}
		// Wait for language processing.
		int count = 0;
		while (count < 60 && !getBot().memory().getActiveMemory().isEmpty()) {
			Utils.sleep(1000);
		}
	}

	/**
	 * Learn responses from the tweet search.
	 */
	public void learnSearch(String tweetSearch, int maxSearch, boolean processTweets, boolean processReplies) {
		log("Learning from tweet search", Level.INFO, tweetSearch);
		try {
			Network memory = getBot().memory().newMemory();
			int count = 0;
			this.errors = 0;
			Set<Long> processed = new HashSet<Long>();
			Query query = new Query(tweetSearch);
			query.count(100);
			SearchResource search = getConnection().search();
			QueryResult result = search.search(query);
			List<Status> tweets = result.getTweets();
			if (tweets != null) {
				log("Processing search results", Level.INFO, tweets.size(), tweetSearch);
				for (Status tweet : tweets) {
					if (count > maxSearch) {
						log("Max search results processed", Level.INFO, maxSearch);
						break;
					}
					if (!processed.contains(tweet.getId())) {
						log("Processing search result", Level.INFO, tweet.getUser().getScreenName(), tweetSearch, tweet.getText());
						processed.add(tweet.getId());
						learnTweet(tweet, processTweets, processReplies, memory);
						count++;
					}
				}
				memory.save();
			}
			// Search only returns 7 days, search for users as well.
			TextStream stream = new TextStream(tweetSearch);
			while (!stream.atEnd()) {
				stream.skipToAll("from:", true);
				if (stream.atEnd()) {
					break;
				}
				String user = stream.nextWord();
				String arg[] = new String[1];
				arg[0] = user;
				ResponseList<User> users = getConnection().lookupUsers(arg);
				if (!users.isEmpty()) {
					long id = users.get(0).getId();
					boolean more = true;
					int page = 1;
					while (more) {
						Paging pageing = new Paging(page);
						ResponseList<Status> timeline = getConnection().getUserTimeline(id, pageing);
						if ((timeline == null) || (timeline.size() < 20)) {
							more = false;
						}
						page++;
						if ((timeline == null) || timeline.isEmpty()) {
							more = false;
							break;
						}
						log("Processing user timeline", Level.INFO, user, timeline.size());
						for (int index = timeline.size() - 1; index >= 0; index--) {
							if (count >= maxSearch) {
								more = false;
								break;
							}
							Status tweet = timeline.get(index);
							if (!processed.contains(tweet.getId())) {
								log("Processing user timeline result", Level.INFO, tweet.getUser().getScreenName(), tweet.getText());
								processed.add(tweet.getId());
								learnTweet(tweet, processTweets, processReplies, memory);
								count++;
							}
						}
						memory.save();
					}
					if (count >= maxSearch) {
						log("Max search results processed", Level.INFO, maxSearch);
						break;
					}
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Learn from the profiles posts.
	 */
	public void checkLearning() {
		if (!getLearnFromSelf()) {
			return;
		}
		log("Checking learning", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTLEARN);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			long max = 0;
			ResponseList<Status> timeline = getConnection().getUserTimeline();
			if ((timeline == null) || timeline.isEmpty()) {
				return;
			}
			log("Processing status", Level.INFO, timeline.size());
			for (int index = timeline.size() - 1; index >= 0; index--) {
				Status tweet = timeline.get(index);
				long statusTime = tweet.getCreatedAt().getTime();
				long statusId = tweet.getId();
				if (statusId > max) {
					max = statusId;
				}
				if ((System.currentTimeMillis() - statusTime) > DAY) {
					log("Day old status", Level.INFO, statusId, statusTime);
					continue;
				}
				if (statusId > last) {
					learnTweet(tweet, true, true, memory);
				} else {
					log("Old status", Level.INFO, statusId, statusTime);							
				}
			}
			if (max != 0) {
				twitter.setRelationship(Primitive.LASTTIMELINE, memory.createVertex(max));
				memory.save();
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public void learnTweet(Status tweet, boolean processTweets, boolean processReplies, Network memory) throws Exception {
		String text = tweet.getText();
		// Exclude retweets
		if (tweet.isRetweet()) {
			log("Tweet is retweet", Level.FINER, tweet.getText());
			return;
		}
		if (Utils.checkProfanity(text)) {
			log("Ignoring profanity", Level.INFO, text);
			return;
		}
		// Exclude protected
		if (tweet.getUser().isProtected() && !tweet.getUser().getScreenName().equals(getUserName())) {
			log("Tweet is protected", Level.FINER, tweet.getText());
			return;
		}
		log("Learning status", Level.INFO, text);
		// Exclude replies/mentions
		if (tweet.getText().indexOf('@') != -1) {
			log("Tweet is reply", Level.FINER, tweet.getText());
			if (!processReplies) {
				return;
			}
			long id = tweet.getInReplyToStatusId();
			if (id > 0) {
				try {
					Status reply = getConnection().showStatus(id);
					String replyText = reply.getText();
					if (replyText != null && !replyText.isEmpty()) {
						// Filter out @users
						for (String word : new TextStream(text).allWords()) {
							if (word.startsWith("@")) {
								text = text.replace(word, "");
							}
						}
						for (String word : new TextStream(replyText).allWords()) {
							if (word.startsWith("@")) {
								replyText = replyText.replace(word, "");
							}
						}
						Vertex question = memory.createSentence(replyText.trim());
						Vertex sentence = memory.createSentence(text.trim());
						Language.addResponse(question, sentence, memory);
					}
				} catch (Exception ignore) {
					log(ignore.toString(), Level.WARNING);
				}
				
			}
			return;
		}
		if (!processTweets) {
			return;
		}
		Vertex sentence = memory.createSentence(text);
		String keywords = "";
		for (String word : new TextStream(text).allWords()) {
			if (word.startsWith("#")) {
				keywords = keywords + " " + word + " " + word.substring(1, word.length());
			}
		}
		Language.addResponse(sentence, sentence, null, keywords, null, memory);
	}

	/**
	 * Check messages to this user.
	 */
	public void checkMentions() {
		if (!getReplyToMentions()) {
			return;
		}
		try {
			log("Checking mentions", Level.FINE);
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTMENTION);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			long max = 0;
			ResponseList<Status> mentions = null;
			boolean more = true;
			int page = 1;
			while (more) {
				if (last == 0) {
					mentions = getConnection().getMentionsTimeline();
					more = false;
				} else {
					Paging paging = new Paging(page, last);
					mentions = getConnection().getMentionsTimeline(paging);
					if ((mentions == null) || (mentions.size() < 20)) {
						more = false;
					}
					page++;
				}
				if ((mentions == null) || mentions.isEmpty()) {
					break;
				}
				log("Processing mentions", Level.FINE, mentions.size());
				for (int index = mentions.size() - 1; index >= 0; index--) {
					Status tweet = mentions.get(index);
					long statusTime = tweet.getCreatedAt().getTime();
					long statusId = tweet.getId();
					if (statusId > max) {
						max = statusId;
					}
					if ((System.currentTimeMillis() - statusTime) > DAY) {
						log("Day old mention", Level.INFO, statusId, statusTime);
						more = false;
						continue;
					}
					// Exclude self
					if (tweet.getUser().getScreenName().equals(getUserName())) {
						continue;
					}
					if (statusId > last) {
						log("Processing mention", Level.INFO, tweet.getText(), tweet.getUser().getScreenName());
						input(tweet);
						Utils.sleep(100);
					} else {
						log("Old mention", Level.INFO, statusId, statusTime);							
					}
				}
			}
			if (max != 0) {
				twitter.setRelationship(Primitive.LASTMENTION, memory.createVertex(max));
				memory.save();
			}
		} catch (Exception exception) {
			log(exception);
		}
		// Wait for language processing.
		int count = 0;
		while (count < 60 && !getBot().memory().getActiveMemory().isEmpty()) {
			Utils.sleep(1000);
		}
		this.languageState = LanguageState.Discussion;
	}

	/**
	 * Check search keywords.
	 */
	public void checkSearch() {
		if (getTweetSearch().isEmpty()) {
			return;
		}
		log("Processing search", Level.FINE, getTweetSearch());
		try {
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTSEARCH);
			long last = 0;
			long max = 0;
			int count = 0;
			this.errors = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			Set<Long> processed = new HashSet<Long>();
			for (String tweetSearch : getTweetSearch()) {
				Query query = new Query(tweetSearch);
				if (vertex != null) {
					query.setSinceId(last);
				}
				SearchResource search = getConnection().search();
				QueryResult result = search.search(query);
				List<Status> tweets = result.getTweets();
				if (tweets != null) {
					log("Processing search results", Level.FINE, tweets.size(), tweetSearch);
					for (Status tweet : tweets) {
						if (count > this.maxSearch) {
							log("Max search results processed", Level.FINE, this.maxSearch);
							break;
						}
						if (tweet.getId() > last  && !processed.contains(tweet.getId())) {
							if (tweet.getId() > max) {
								max = tweet.getId();
							}
							boolean match = false;
							// Exclude replies/mentions
							if (getIgnoreReplies() && tweet.getText().indexOf('@') != -1) {
								log("Ignoring: Tweet is reply", Level.FINER, tweet.getText());
								continue;
							}
							// Exclude retweets
							if (tweet.isRetweet()) {
								log("Ignoring: Tweet is retweet", Level.FINER, tweet.getText());
								continue;
							}
							// Exclude protected
							if (tweet.getUser().isProtected()) {
								log("Ignoring: Tweet is protected", Level.FINER, tweet.getText());
								continue;
							}
							// Exclude self
							if (tweet.getUser().getScreenName().equals(getUserName())) {
								log("Ignoring: Tweet is from myself", Level.FINER, tweet.getText());
								continue;
							}
							// Ignore profanity
							if (Utils.checkProfanity(tweet.getText())) {
								log("Ignoring: Tweet contains profanity", Level.FINER, tweet.getText());
								continue;
							}
							List<String> statusWords = new TextStream(tweet.getText().toLowerCase()).allWords();
							for (String text : getStatusKeywords()) {
								List<String> keywords = new TextStream(text.toLowerCase()).allWords();
								if (statusWords.containsAll(keywords)) {
									match = true;
									break;
								}
							}
							if (getLearn()) {
								learnTweet(tweet, true, true, memory);
							}
							if (match) {
								processed.add(tweet.getId());
								log("Processing search", Level.INFO, tweet.getUser().getScreenName(), tweetSearch, tweet.getText());
								input(tweet);
								Utils.sleep(500);
								count++;
							} else {
								if (!tweet.isRetweetedByMe()) {
									boolean found = false;
									// Check retweet.
									for (String keywords : getRetweet()) {
										List<String> keyWords = new TextStream(keywords).allWords();
										if (!keyWords.isEmpty()) {
											if (statusWords.containsAll(keyWords)) {
												found = true;
												processed.add(tweet.getId());
												count++;
												retweet(tweet);
												Utils.sleep(500);
												break;
											}
										}
									}
									if (!found) {
										log("Missing keywords", Level.FINER, tweet.getText());
									}
								} else {
									log("Already retweeted", Level.FINER, tweet.getText());
								}
							}
						}
					}
				}
				if (count > this.maxSearch) {
					break;
				}
				if (this.errors > this.maxErrors) {
					break;
				}
			}
			if (max != 0) {
				twitter.setRelationship(Primitive.LASTSEARCH, memory.createVertex(max));
				memory.save();
			}
		} catch (Exception exception) {
			log(exception);
		}
		// Wait for language processing.
		int count = 0;
		while (count < 60 && !getBot().memory().getActiveMemory().isEmpty()) {
			Utils.sleep(1000);
		}
	}

	/**
	 * Check search keywords.
	 */
	public void checkAutoFollowSearch(int friendCount) {
		if (getAutoFollowSearch().isEmpty()) {
			return;
		}
		log("Processing autofollow search", Level.FINE, getAutoFollowSearch());
		try {
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTAUTOFOLLOWSEARCH);
			long last = 0;
			long max = 0;
			int count = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			for (String followSearch : getAutoFollowSearch()) {
				Query query = new Query(followSearch);
				if (vertex != null) {
					query.setSinceId(last);
				}
				SearchResource search = getConnection().search();
				QueryResult result = search.search(query);
				List<Status> tweets = result.getTweets();
				if (tweets != null) {
					for (Status tweet : tweets) {
						if (count > this.maxSearch) {
							break;
						}
						if (tweet.getId() > last) {
							log("Autofollow search", Level.FINE, tweet.getText(), tweet.getUser().getScreenName(), followSearch);
							if (checkFriendship(tweet.getUser().getId(), false)) {
								friendCount++;
								if (friendCount >= getMaxFriends()) {
									log("Max friend limit", Level.FINE, getMaxFriends());
									return;
								}
							}
							count++;
							if (tweet.getId() > max) {
								max = tweet.getId();
							}
						}
					}
				}
				if (count > this.maxSearch) {
					break;
				}
			}
			if (max != 0) {
				twitter.setRelationship(Primitive.LASTAUTOFOLLOWSEARCH, memory.createVertex(max));
				memory.save();
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Check RSS feed.
	 */
	public void checkRSS() {
		if (getTweetRSS().isEmpty()) {
			return;
		}
		log("Processing RSS", Level.FINE, getTweetRSS());
		try {
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTRSS);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			int rssIndex = 0;
			String keywordsText = "";
			List<String> keywords = new ArrayList<String>();
			for (String rss : getTweetRSS()) {
				if (rssIndex < getRssKeywords().size()) {
					keywordsText = getRssKeywords().get(rssIndex);
					keywords = new TextStream(keywordsText.toLowerCase()).allWords();				
				}
				rssIndex++;
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
					log("Processing RSS feed", Level.FINE, feed.size(), rss);
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
							if (!keywords.isEmpty()) {
								if (!new TextStream(text.toLowerCase()).allWords().containsAll(keywords)) {
									log("Skipping RSS, missing keywords", Level.FINE, keywords, text);
									continue;
								}
							}
							log("Tweeting RSS", Level.FINE, entry.get("title"));
							text = prefix + text + postfix;
							if (text.length() > 120) {
								text = text.substring(0, 120);
							}
							tweet(text + " " + entry.get("link"), 0L);
							Utils.sleep(500);
							count++;
							if (time > max) {
								max = time;
							}
						}
					}
					if (max != 0) {
						twitter.setRelationship(Primitive.LASTRSS, memory.createVertex(max));
						memory.save();
					}
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Auto tweet.
	 */
	public void checkAutoTweet() {
		if (!getAutoTweet()) {
			return;
		}
		log("Autotweeting", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex twitter = memory.createVertex(getPrimitive());
			Vertex vertex = twitter.getRelationship(Primitive.LASTTWEET);
			long last = 0;
			if (vertex != null) {
				last = ((Timestamp)vertex.getData()).getTime();
			}
			long millis = getAutoTweetHours() * 60 * 60 * 1000;
			if ((System.currentTimeMillis() - last) < millis) {
				log("Autotweeting hours not reached", Level.FINE, getAutoTweetHours());
				return;
			}
			List<Vertex> autotweets = getAutoTweets(memory);
			if (autotweets != null && !autotweets.isEmpty()) {
				int index = Utils.random().nextInt(autotweets.size());
				Vertex tweet = autotweets.get(index);
				String text = null;
				// Check for labels and formulas
				if (tweet.instanceOf(Primitive.LABEL)) {
					tweet = tweet.mostConscious(Primitive.RESPONSE);
				}
				if (tweet.instanceOf(Primitive.FORMULA)) {
					Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
					SelfCompiler.addGlobalVariables(memory.createInstance(Primitive.INPUT), null, memory, variables);
					Vertex result = getBot().mind().getThought(Language.class).evaluateFormula(tweet, variables, memory);
					if (result != null) {
						text = getBot().mind().getThought(Language.class).getWord(result, memory).getDataValue();
					} else {
						log("Invalid autotweet template formula", Level.WARNING, tweet);
						text = null;
					}
				} else {
					text = tweet.printString();
				}
				if (text != null) {
					log("Autotweeting", Level.INFO, tweet);
					tweet(text, 0L);
					Utils.sleep(100);
					twitter.setRelationship(Primitive.LASTTWEET, memory.createTimestamp());
					memory.save();
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Return the list of followers names.
	 */
	public List<String> getFollowers() {
		List<String> followers = new ArrayList<String>();
		try {
			long[] ids = getConnection().getFollowersIDs(-1).getIDs();
			if (ids.length == 0) {
				return followers;
			}
			int index = 0;
			while (ids.length > index) {
				long[] lookup = ids;
				if (index > 0) {
					lookup = Arrays.copyOfRange(ids, index, Math.min(ids.length, index + MAX_LOOKUP));
				} else if (ids.length > MAX_LOOKUP) {
					lookup = Arrays.copyOf(ids, MAX_LOOKUP);
				}
				index = index + MAX_LOOKUP;
				ResponseList<User> users = getConnection().lookupUsers(lookup);
				for (User user : users) {
					followers.add(user.getScreenName());
				}
				// Only return first 100.
				break;
			}
		} catch (Exception exception) {
			log(exception);
		}
		return followers;
	}

	/**
	 * Return the time-line.
	 */
	public List<String> getTimeline() {
		List<String> timeline = new ArrayList<String>();
		try {
			ResponseList<Status> statuses = getConnection().getHomeTimeline();
			for (Status status : statuses) {
				timeline.add(status.getCreatedAt() + " - <b>" + status.getUser().getScreenName() + "</b>:  " + status.getText());
			}
		} catch (Exception exception) {
			log(exception);
			throw new BotException(exception);
		}
		return timeline;
	}

	/**
	 * Return the total number of friends.
	 */
	public int getFriendsCount() {
		try {
			return getConnection().getFriendsIDs(-1).getIDs().length;
		} catch (Exception exception) {
			log(exception);
			return 0;
		}
	}

	/**
	 * Return the total number of followers.
	 */
	public int getFollowersCount() {
		try {
			return getConnection().getFollowersIDs(-1).getIDs().length;
		} catch (Exception exception) {
			log(exception);
			return 0;
		}
	}

	/**
	 * Return the list of friends names.
	 */
	public List<String> getFriends() {
		List<String> friends = new ArrayList<String>();
		try {
			long[] friendIds = getConnection().getFriendsIDs(-1).getIDs();
			int index = 0;
			while (friendIds.length > index) {
				long[] lookup = friendIds;
				if (index > 0) {
					lookup = Arrays.copyOfRange(friendIds, index, Math.min(friendIds.length, index + MAX_LOOKUP));
				} else if (friendIds.length > MAX_LOOKUP) {
					lookup = Arrays.copyOf(friendIds, MAX_LOOKUP);
				}
				index = index + MAX_LOOKUP;
				ResponseList<User> users = getConnection().lookupUsers(lookup);
				for (User user : users) {
					friends.add(user.getScreenName());
				}
				// Only return first 100.
				break;
			}
		} catch (Exception exception) {
			log(exception);
		}
		return friends;
	}

	/**
	 * Check followers.
	 */
	public void checkFollowers() {
		if (!getAutoFollow() && getWelcomeMessage().isEmpty()) {
			return;
		}
		try {
			log("Checking followers", Level.FINE);
			long[] followerIds = getConnection().getFollowersIDs(-1).getIDs(); //max 5000
			long[] friends = getConnection().getFriendsIDs(-1).getIDs();
			int friendCount = friends.length;
			int count = 0;
			boolean welcomeOnly = false;
			if (friendCount >= getMaxFriends()) {
				if (!getWelcomeMessage().isEmpty()) {
					welcomeOnly = true;
				} else {
					log("Max friend limit", Level.FINE, getMaxFriends());
					return;
				}
			}
			for (int index = 0; index < followerIds.length; index++) {
				boolean found = false;
				long followerId = followerIds[index];
				for (long friend : friends) {
					if (followerId == friend) {
						found = true;
						break;
					}
				}
				if (!found) {
					log("Checking new follower", Level.FINE, followerId);
					boolean isNewFriend = checkFriendship(followerId, welcomeOnly);
					if (!isNewFriend) {
						// Followers are ordered, so if already followed ignore the rest.
						break;
					}
					friendCount++;
					if (friendCount >= getMaxFriends()) {
						if (!getWelcomeMessage().isEmpty()) {
							welcomeOnly = true;
						} else {
							return;
						}
					}
					count++;
					if (count >= this.maxFriendsPerCycle) {
						if (!getWelcomeMessage().isEmpty() && count < this.maxWelcomesPerCycle) {
							welcomeOnly = true;
						} else {
							log("Max friend per cycle limit", Level.FINE, this.maxFriendsPerCycle);
							return;
						}
					}
					if (!welcomeOnly && getAutoFollowFriendsFriends()) {
						log("Checking friends friends", Level.FINE, followerId);
						long[] friendsFriends = getConnection().getFriendsIDs(followerId, -1).getIDs();
						for (long friendsFriend : friendsFriends) {
							if (checkFriendship(friendsFriend, welcomeOnly)) {
								friendCount++;
								if (friendCount >= getMaxFriends()) {
									log("Max friend limit", Level.FINE, getMaxFriends());
									return;
								}
								count++;
								if (count >= this.maxFriendsPerCycle) {
									log("Max friend per cycle limit", Level.FINE, this.maxFriendsPerCycle);
									return;
								}
							}
						}
					}
					if (!welcomeOnly && getAutoFollowFriendsFollowers()) {
						log("Checking friends followers", Level.FINE, followerId);
						long[] friendsFollowers = getConnection().getFollowersIDs(followerId, -1).getIDs();
						for (long friendsFollower : friendsFollowers) {
							if (checkFriendship(friendsFollower, welcomeOnly)) {
								friendCount++;
								if (friendCount >= getMaxFriends()) {
									log("Max friend limit", Level.FINE, getMaxFriends());
									return;
								}
								count++;
								if (count >= this.maxFriendsPerCycle) {
									log("Max friend per cycle limit", Level.FINE, this.maxFriendsPerCycle);
									return;
								}
							}
						}
					}
				}
			}
			checkAutoFollowSearch(friendCount);
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public boolean checkFriendship(long friendId, boolean welcomeOnly) throws TwitterException {
		long[] lookup = new long[1];
		lookup[0] = friendId;
		ResponseList<User> users = getConnection().lookupUsers(lookup);
		User friend = users.get(0);
		if (friend.getScreenName().equals(getUserName())) {
			return false;
		}
		if (!getAutoFollowKeywords().isEmpty()) {
			StringWriter writer = new StringWriter();
			writer.write(friend.getScreenName().toLowerCase());
			writer.write(" ");
			writer.write(friend.getDescription().toLowerCase());
			writer.write(" ");
			writer.write(friend.getLocation().toLowerCase());
			writer.write(" ");
			writer.write(friend.getLang().toLowerCase());
			writer.write(" ");
			writer.write(friend.getName().toLowerCase());
			boolean match = false;
			for (String text : getAutoFollowKeywords()) {
				List<String> keywords = new TextStream(text.toLowerCase()).allWords();
				if (new TextStream(writer.toString()).allWords().containsAll(keywords)) {
					match = true;
					break;
				}
			}
			if (!match) {
				log("Autofollow skipping friend, does not match keywords", Level.FINE, friend.getScreenName());
				return false;
			}
		}
		Network memory = getBot().memory().newMemory();
		Vertex speaker = memory.createSpeaker(friend.getScreenName());
		speaker.setPinned(true);
		// Only try to follow a user once.
		if (!speaker.hasRelationship(Primitive.FOLLOWED)) {
			speaker.addRelationship(Primitive.FOLLOWED, memory.createTimestamp());
			memory.save();
			if (!welcomeOnly && getAutoFollow()) {
				log("Adding autofollow friend.", Level.INFO, friend.getScreenName());
				getConnection().createFriendship(friendId);
				Utils.sleep(1000);
			}
			if (!getWelcomeMessage().isEmpty()) {
				log("Sending welcome message.", Level.INFO, friend.getScreenName());
				sendMessage(getWelcomeMessage(), friend.getScreenName());
				Utils.sleep(1000);
			}
			if (welcomeOnly) {
				return false;
			}
			return true;
		}
		log("Autofollow skipping friend, already followed once", Level.FINE, friend.getScreenName());
		return false;
	}
	
	public void log(TwitterException exception) {
		log(new TextStream(exception.toString()).nextLine(), Bot.WARNING);		
	}

	/**
	 * Check trends.
	 */
	public void checkTrends() {
		try {
			Network network = getBot().memory().newMemory();
			if ((this.lastTrendsCheck == 0)
					|| ((System.currentTimeMillis() - this.lastTrendsCheck) > TREND_CHECK)) {
				Vertex twitter = network.createVertex(getPrimitive());
				Vertex lastCheck = twitter.getRelationship(Primitive.TREND);
				if (lastCheck == null) {
					twitter.addRelationship(Primitive.TREND, network.createTimestamp());
				} else {
					long lastCheckTime = ((Timestamp)lastCheck.getData()).getTime();
					if ((System.currentTimeMillis() - lastCheckTime) < TREND_CHECK) { // 1 hours.
						return;
					}
				}
				log("Checking trends", Bot.FINE);
				twitter.setRelationship(Primitive.TREND, network.createTimestamp());
				/**List<Trends> dailyTrends = getConnection().getDailyTrends();
				List<String> trendNames = new ArrayList<String>();
				if (dailyTrends.size() > 0) {
					for (Trend trend : dailyTrends.get(0).getTrends()) {
						trendNames.add(trend.getName());
					}
				}
				for (String text : trendNames) {
					if (text.charAt(0) == '#') {
						text = text.substring(1, text.length());
					}
					log("Trend:", Bot.FINE, text);
					Vertex word = network.createWord(text);
					
					network.save();
					getBot().memory().addActiveMemory(word);
				}*/
				this.lastTrendsCheck = System.currentTimeMillis();
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Tweet.
	 */
	public void tweet(String html, Long reply) {
		String text = format(html);
		if (text.length() > 140) {
			int index =  text.indexOf("http://");
			if (index == -1) {
				text = text.substring(0, 140);
			} else if (index > 120) {
				text = text.substring(0, 120) + " " + text.substring(index, text.length());
			}
		}
		this.tweets++;
		log("Tweeting:", Level.INFO, text);
		try {
			if (getConnection() == null) {
				connect();
			}
			StatusUpdate update = new StatusUpdate(text);
			if (reply != null) {
				update.setInReplyToStatusId(reply);
			}
			
			// Check for linked media.
			if ((html.indexOf('<') != -1) && (html.indexOf('>') != -1)) {
				String media = null;
				Element root = getBot().awareness().getSense(Http.class).parseHTML(html);
				NodeList nodes = root.getElementsByTagName("img");
				if (nodes.getLength() > 0) {
					String src = ((Element)nodes.item(0)).getAttribute("src");
					if (src != null && !src.isEmpty()) {
						media = src;
					}
				}
				if (media == null) {
					nodes = root.getElementsByTagName("video");
					if (nodes.getLength() > 0) {
						String src = ((Element)nodes.item(0)).getAttribute("src");
						if (src != null && !src.isEmpty()) {
							media = src;
						}
					}
				}
				if (media == null) {
					nodes = root.getElementsByTagName("audio");
					if (nodes.getLength() > 0) {
						String src = ((Element)nodes.item(0)).getAttribute("src");
						if (src != null && !src.isEmpty()) {
							media = src;
						}
					}
				}
				if (media != null) {
					try {
						URL url = new URL(media);
						URLConnection urlConnection = url.openConnection();
						InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
						update.setMedia("image.png", stream);
					} catch (Exception exception) {
						log(exception);
					}
				}
			}
			
			
			getConnection().updateStatus(update);
		} catch (Exception exception) {
			this.errors++;
			log(exception.getMessage(), Level.WARNING, text);
		}
	}
	
	public String format(String text) {
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
		text = Utils.stripTags(text);
		return text;
	}

	/**
	 * Send a message to the user.
	 */
	public void sendMessage(String text, String replyUser) {
		log("Sending message:", Level.INFO, text, replyUser);
		try {
			text = format(text);
			getConnection().sendDirectMessage(replyUser, text);
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
	}

	/**
	 * Retweet the tweet.
	 */
	public void retweet(Status tweet) {
		if (tweet.isRetweet()) {
			tweet = tweet.getRetweetedStatus();
		}
		if (tweet.getUser().isProtected()) {
			log("Cannot retweet protected user", Level.INFO, tweet.getUser().getScreenName(), tweet.getText());
			return;
		}
		this.retweets++;
		log("Retweeting:", Level.INFO, tweet.getText(), tweet.getUser().getScreenName());
		try {
			if (getConnection() == null) {
				connect();
			}
			getConnection().retweetStatus(tweet.getId());
		} catch (Exception exception) {
			if (exception.getMessage() != null && exception.getMessage().contains("authorized") && exception.getMessage().contains("endpoint")) {
				this.errors = this.errors + 5;				
			}
			this.errors++;
			log(exception.toString(), Level.WARNING, tweet.getText());
		}
	}

	/**
	 * Output the tweet if twitter is connected.
	 */
	public void outputTweet(String tweet) {
		if (!isEnabled() || !getTweetChats()) {
			return;
		}
		Network network = getBot().memory().newMemory();
		Vertex setence = network.createSentence(tweet);
		Vertex output = network.createInstance(Primitive.INPUT);
		output.setName(tweet);
		output.addRelationship(Primitive.INPUT, setence);
		output.addRelationship(Primitive.SENSE, getPrimitive());
		output.addRelationship(Primitive.SPEAKER, Primitive.SELF);
		output.addRelationship(Primitive.INSTANTIATION, Primitive.TWEET);
		Vertex target = output.mostConscious(Primitive.TARGET);
		if (target != null) {
			String replyTo = target.mostConscious(Primitive.WORD).getData().toString();
			tweet = "@" + replyTo + " " + tweet;
		}
		network.save();
		tweet(tweet, null);
	}
	
	/**
	 * Process the email message.
	 */
	@Override
	public void input(Object input, Network network) {
		if (!isEnabled()) {
			return;
		}
		try {
			if (input instanceof Status) {				
				Status tweet = (Status)input;
				log("Processing status", Bot.FINE, tweet.getText(), tweet.getId());
				if ((System.currentTimeMillis() - tweet.getCreatedAt().getTime()) > DAY) {
					log("Day old status", Bot.FINE, tweet.getId(), tweet.getCreatedAt().getTime());
					return;
				}
				if (this.processedTweets.contains(tweet.getId())) {
					log("Already processed status", Bot.FINE, tweet.getText(), tweet.getId());
					return;
				}
				this.processedTweets.add(tweet.getId());
				String name = tweet.getUser().getScreenName();
				String replyTo = tweet.getInReplyToScreenName();
				String text = tweet.getText().trim();
				TextStream stream = new TextStream(text);
				String firstWord = null;
				if (getIgnoreReplies()) {
					if (stream.peek() == '@') {
						stream.next();
						String replyTo2 = stream.nextWord();
						firstWord = stream.peekWord();
						text = stream.upToEnd().trim();
						if (!replyTo2.equals(replyTo)) {
							log("Reply to does not match:", Bot.FINE, replyTo2, replyTo);
						}
						replyTo = replyTo2;
						if (replyTo.equals(this.userName) && getFollowMessages()) {
							if ("follow".equals(firstWord)) {
								log("Adding friend", Level.INFO, tweet.getUser().getScreenName());
								getConnection().createFriendship(tweet.getUser().getId());
							} else if ("unfollow".equals(firstWord)) {
								log("Removing friend", Level.INFO, tweet.getUser().getScreenName());
								getConnection().destroyFriendship(tweet.getUser().getId());
							}
						}
					}
				} else {
					// Ignore the reply user, force the bot to reply.
					replyTo = null;
				}
				if (!tweet.isRetweet() && !tweet.getUser().isProtected()) {
					stream.reset();
					List<String> words = stream.allWords();
					for (String keywords : getRetweet()) {
						List<String> keyWords = new TextStream(keywords).allWords();
						if (!keyWords.isEmpty()) {
							if (words.containsAll(keyWords)) {
								retweet(tweet);
								break;
							}
						}
					}
				}
				log("Input status", Level.FINE, tweet.getText(), name, replyTo);
				this.tweetsProcessed++;
				inputSentence(text, name, replyTo, tweet, network);
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Output the status or direct message reply.
	 */
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to twitter, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		output.addRelationship(Primitive.INSTANTIATION, Primitive.TWEET);
		String text = printInput(output);
		// Don't send empty tweets.
		if (text.isEmpty()) {
			return;
		}
		Vertex target = output.mostConscious(Primitive.TARGET);
		if (target != null) {
			String replyTo = target.mostConscious(Primitive.WORD).getData().toString();
			text = "@" + replyTo + " " + text;
		}
		Vertex question = output.getRelationship(Primitive.QUESTION);
		Long reply = null;
		if (question != null) {
			Vertex id = question.getRelationship(Primitive.ID);
			if (id != null) {
				reply = ((Number)id.getData()).longValue();
			}
		}
		tweet(text, reply);
	}	

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, String targetUserName, Status status, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex sentence = input.getRelationship(Primitive.INPUT);
		Vertex id = network.createVertex(status.getId());
		if (sentence.hasRelationship(Primitive.TWEET, id)) {
			log("Status already processed", Bot.FINE, status.getId(), status.getCreatedAt().getTime());			
			return;
		}
		sentence.addRelationship(Primitive.TWEET, id);
		input.addRelationship(Primitive.INSTANTIATION, Primitive.TWEET);
		input.addRelationship(Primitive.CREATEDAT, network.createVertex(status.getCreatedAt().getTime()));
		input.addRelationship(Primitive.ID, id);
		Vertex conversation = network.createInstance(Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.TWEET);
		Language.addToConversation(input, conversation);
		Vertex user = network.createSpeaker(userName);
		conversation.addRelationship(Primitive.SPEAKER, user);
		input.addRelationship(Primitive.SPEAKER, user);
		if (targetUserName != null) {
			Vertex targetUser = null;
			if (targetUserName.equals(getUserName())) {
				targetUser = network.createVertex(Primitive.SELF);
			} else {
				targetUser = network.createSpeaker(targetUserName);
			}
			input.addRelationship(Primitive.TARGET, targetUser);
			conversation.addRelationship(Primitive.SPEAKER, targetUser);
		}
		
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.TWEET);
		return input;
	}

	public String getOauthKey() {
		return oauthKey;
	}

	public void setOauthKey(String oauthKey) {
		Twitter.oauthKey = oauthKey;
	}

	public String getOauthSecret() {
		return oauthSecret;
	}

	public void setOauthSecret(String oauthSecret) {
		Twitter.oauthSecret = oauthSecret;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public twitter4j.Twitter getConnection() throws TwitterException {
		if (connection == null) {
			connect();
		}
		return connection;
	}

	public void setConnection(twitter4j.Twitter connection) {
		this.connection = connection;
	}

	public boolean getAutoFollow() {
		initProperties();
		return autoFollow;
	}

	public void setAutoFollow(boolean autoFollow) {
		initProperties();
		this.autoFollow = autoFollow;
	}

	public int getMaxFriends() {
		initProperties();
		return maxFriends;
	}

	public void setMaxFriends(int maxFriends) {
		initProperties();
		this.maxFriends = maxFriends;
	}

	public boolean getLearn() {
		initProperties();
		return learn;
	}

	public void setLearn(boolean learn) {
		initProperties();
		this.learn = learn;
	}

	public boolean getLearnFromSelf() {
		initProperties();
		return learnFromSelf;
	}

	public void setLearnFromSelf(boolean learnFromSelf) {
		initProperties();
		this.learnFromSelf = learnFromSelf;
	}

	public boolean getProcessStatus() {
		initProperties();
		return processStatus;
	}

	public void setProcessStatus(boolean processStatus) {
		initProperties();
		this.processStatus = processStatus;
	}

	public boolean getTweetChats() {
		return tweetChats;
	}

	public boolean getReplyToMentions() {
		initProperties();
		return replyToMentions;
	}

	public boolean getReplyToMessages() {
		initProperties();
		return replyToMessages;
	}

	public void setTweetChats(boolean tweetChats) {
		this.tweetChats = tweetChats;
	}
	
	public void setReplyToMentions(boolean replyToMentions) {
		this.replyToMentions = replyToMentions;
	}

	public void setReplyToMessages(boolean replyToMessages) {
		this.replyToMessages = replyToMessages;
	}

	public List<String> getRetweet() {
		initProperties();
		return retweet;
	}

	public void setRetweet(List<String> retweet) {
		initProperties();
		this.retweet = retweet;
	}

	public List<String> getTweetRSS() {
		initProperties();
		return tweetRSS;
	}

	public void setTweetRSS(List<String> tweetRSS) {
		initProperties();
		this.tweetRSS = tweetRSS;
	}

	public boolean getCheckTrends() {
		return checkTrends;
	}

	public void setCheckTrends(boolean checkTrends) {
		this.checkTrends = checkTrends;
	}

	public List<String> getTweetSearch() {
		initProperties();
		return tweetSearch;
	}

	public void setTweetSearch(List<String> tweetSearch) {
		initProperties();
		this.tweetSearch = tweetSearch;
	}

	public boolean getFollowMessages() {
		initProperties();
		return followMessages;
	}

	public void setFollowMessages(boolean followMessages) {
		initProperties();
		this.followMessages = followMessages;
	}

	public int getMaxStatus() {
		initProperties();
		return maxStatus;
	}

	public void setMaxStatus(int maxStatus) {
		initProperties();
		this.maxStatus = maxStatus;
	}

	// Self API
	public void tweet(Vertex source, Vertex sentence) {
		if (sentence.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(sentence.getNetwork().createInstance(Primitive.INPUT), null, sentence.getNetwork(), variables);
			sentence = getBot().mind().getThought(Language.class).evaluateFormula(sentence, variables, sentence.getNetwork());
			if (sentence == null) {
				log("Invalid template formula", Level.WARNING, sentence);
				return;
			}
		}
		String tweet = getBot().mind().getThought(Language.class).getWord(sentence, sentence.getNetwork()).getDataValue();
		getBot().stat("twitter.tweet");
		tweet(tweet, 0L);
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void message(Vertex source, Vertex replyUser, Vertex text) {
		sendMessage(text.printString(), replyUser.printString());
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void sendMessage(Vertex source, Vertex text, Vertex replyUser) {
		sendMessage(text.printString(), replyUser.printString());
	}

	// Self API
	public Vertex trend(Network network) throws TwitterException {
		Trends trends = getConnection().getPlaceTrends(1);
		if (trends.getTrends().length > 0) {
			return network.createObject(trends.getTrends()[0].getName());
		}
		return null;
	}
	
}