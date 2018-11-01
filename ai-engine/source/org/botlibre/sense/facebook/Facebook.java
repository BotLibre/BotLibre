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
package org.botlibre.sense.facebook;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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

import facebook4j.Account;
import facebook4j.Comment;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Friend;
import facebook4j.PagableList;
import facebook4j.Post;
import facebook4j.PostUpdate;
import facebook4j.Reading;
import facebook4j.ResponseList;
import facebook4j.User;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Enables receiving a sending messages through Facebook.
 */
public class Facebook extends BasicSense {
	public static int MAX_LOOKUP = 100;
	public static String oauthKey = "key";
	public static String oauthSecret = "secret";
	
	protected String userName = "";
	protected String token = "";
	protected Date tokenExpiry;
	public String appOauthKey = "";
	public String appOauthSecret = "";
	
	protected boolean initProperties;
	
	protected boolean autoFriend = false;
	protected String welcomeMessage = "";
	protected int maxFriends = 100;
	protected int maxFriendsPerCycle = 5;
	protected int maxPage = 5;
	protected int maxLike = 20;
	protected int maxPost = 20;
	protected int maxFeed = 20;
	protected int maxErrors = 5;
	protected int errors;
	protected boolean processPost = false;
	protected boolean processAllPosts = false;
	protected boolean processNewsFeed = false;
	protected boolean processAllNewsFeed = false;
	protected boolean likeAllPosts = false;
	protected boolean replyToMessages = false;
	protected boolean facebookMessenger = false;
	protected String facebookMessengerAccessToken = "";
	protected String buttonType = "auto";
	protected boolean stripButtonText = false;
	protected boolean trackMessageObjects = false;
	protected String persistentMenu = "";
	protected String getStartedButton = "";
	protected String greetingText = "";
	protected boolean autoPost = false;
	protected int autoPostHours = 24;
	protected String page = "";
	protected String pageId = "";
	protected List<String> pages = new ArrayList<String>();
	protected String profileName = "";
	protected List<String> likeKeywords = new ArrayList<String>();
	protected List<String> postRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();
	protected List<String> statusKeywords = new ArrayList<String>();
	protected List<String> newsFeedKeywords = new ArrayList<String>();
	protected List<String> autoFriendKeywords = new ArrayList<String>();

	protected Set<String> processedPosts = new HashSet<String>();
	protected Set<String> wallPosts = new HashSet<String>();
	
	protected int posts;
	protected int postsProcessed;
	protected int messagesProcessed;
	protected int likes;

	protected facebook4j.Facebook connection;	
	
	public Facebook(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Facebook() {
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
	
	public String getAppOauthKey() {
		return appOauthKey;
	}

	public void setAppOauthKey(String appOauthKey) {
		this.appOauthKey = appOauthKey;
	}

	public String getAppOauthSecret() {
		return appOauthSecret;
	}

	public void setAppOauthSecret(String appOauthSecret) {
		this.appOauthSecret = appOauthSecret;
	}

	public List<String> getPages() {
		return pages;
	}

	public void setPages(List<String> pages) {
		this.pages = pages;
	}

	public boolean getStripButtonText() {
		initProperties();
		return stripButtonText;
	}

	public void setStripButtonText(boolean stripButtonText) {
		initProperties();
		this.stripButtonText = stripButtonText;
	}

	public String getButtonType() {
		initProperties();
		if (this.buttonType == null || this.buttonType.isEmpty()) {
			this.buttonType = "auto";
		}
		return this.buttonType;
	}

	public void setButtonType(String buttonType) {
		initProperties();
		this.buttonType = buttonType;
	}

	public String getGetStartedButton() {
		initProperties();
		return getStartedButton;
	}

	public void setGetStartedButton(String getStartedButton) {
		initProperties();
		this.getStartedButton = getStartedButton;
	}

	public String getGreetingText() {
		initProperties();
		return greetingText;
	}

	public void setGreetingText(String greetingText) {
		initProperties();
		this.greetingText = greetingText;
	}

	public String getPersistentMenu() {
		initProperties();
		return persistentMenu;
	}

	public void setPersistentMenu(String persistentMenu) {
		initProperties();
		this.persistentMenu = persistentMenu;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getPostsProcessed() {
		return postsProcessed;
	}

	public void setPostsProcessed(int postsProcessed) {
		this.postsProcessed = postsProcessed;
	}

	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getPage() {
		initProperties();
		return page;
	}

	public String getPageId() {
		initProperties();
		if ((pageId == null || pageId.isEmpty()) && isPage()) {
			try {
				connect();
				this.pageId = getConnection().getPage().getId();
			} catch (Exception exception) {
				log(exception);
			}
		}
		return pageId;
	}

	public void setPageId(String pageId) {
		initProperties();
		this.pageId = pageId;
	}

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public void setPage(String page) {
		initProperties();
		this.page = page;
	}

	public String getFacebookMessengerAccessToken() {
		initProperties();
		return facebookMessengerAccessToken;
	}

	public void setFacebookMessengerAccessToken(String facebookMessengerAccessToken) {
		this.facebookMessengerAccessToken = facebookMessengerAccessToken;
	}

	public String getWelcomeMessage() {
		initProperties();
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		initProperties();
		this.welcomeMessage = welcomeMessage;
	}

	public boolean getProcessAllPosts() {
		initProperties();
		return processAllPosts;
	}

	public void setProcessAllPosts(boolean processAllPosts) {
		initProperties();
		this.processAllPosts = processAllPosts;
	}

	public boolean getProcessAllNewsFeed() {
		initProperties();
		return processAllNewsFeed;
	}

	public void setProcessAllNewsFeed(boolean processAllNewsFeed) {
		initProperties();
		this.processAllNewsFeed = processAllNewsFeed;
	}

	public boolean getLikeAllPosts() {
		initProperties();
		return likeAllPosts;
	}

	public void setLikeAllPosts(boolean likesAllPosts) {
		initProperties();
		this.likeAllPosts = likesAllPosts;
	}

	public List<String> getRssKeywords() {
		initProperties();
		return rssKeywords;
	}

	public void setRssKeywords(List<String> rssKeywords) {
		initProperties();
		this.rssKeywords = rssKeywords;
	}

	public boolean getProcessNewsFeed() {
		initProperties();
		return processNewsFeed;
	}

	public void setProcessNewsFeed(boolean processNewsFeed) {
		initProperties();
		this.processNewsFeed = processNewsFeed;
	}

	public List<String> getNewsFeedKeywords() {
		initProperties();
		return newsFeedKeywords;
	}

	public void setNewsFeedKeywords(List<String> newsFeedKeywords) {
		initProperties();
		this.newsFeedKeywords = newsFeedKeywords;
	}

	public int getMaxFeed() {
		initProperties();
		return maxFeed;
	}

	public void setMaxFeed(int maxFeed) {
		initProperties();
		this.maxFeed = maxFeed;
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

	public List<String> getPostKeywords() {
		initProperties();
		return statusKeywords;
	}

	public void setPostKeywords(List<String> statusKeywords) {
		initProperties();
		this.statusKeywords = statusKeywords;
	}

	public List<String> getAutoFriendKeywords() {
		initProperties();
		return autoFriendKeywords;
	}

	public void setAutoFriendKeywords(List<String> autoFriendKeywords) {
		initProperties();
		this.autoFriendKeywords = autoFriendKeywords;
	}

	/**
	 * Authorise a new account to be accessible by Bot.
	 * Return the request token that contains the URL that the user must use to authorise facebook.
	 */
	public String authorizeAccount(String callbackURL) throws FacebookException {
		this.connection = new FacebookFactory().getInstance();
		String key = getOauthKey();
		String secret = getOauthSecret();
		if (this.appOauthKey != null && !this.appOauthKey.isEmpty()) {
			key = this.appOauthKey;
		}
		if (this.appOauthSecret != null && !this.appOauthSecret.isEmpty()) {
			secret = this.appOauthSecret;
		}
		this.connection.setOAuthAppId(key, secret);
		if (this.appOauthKey != null && !this.appOauthKey.isEmpty()) {
			this.connection.setOAuthPermissions("manage_pages,publish_pages");
		} else {
			this.connection.setOAuthPermissions("manage_pages,publish_pages");
		}
		//this.connection.setOAuthPermissions("read_stream, manage_pages, publish_pages, publish_actions, read_mailbox, read_page_mailboxes");
		return this.connection.getOAuthAuthorizationURL(callbackURL);
	}
	
	/**
	 * Authorise a new account to be accessible by Bot.
	 */
	public void authorizeComplete(String pin) throws FacebookException {
		AccessToken token = this.connection.getOAuthAccessToken(pin);
	    setToken(token.getToken());

		User user = this.connection.getMe();
		this.userName = user.getId();
		if (token.getExpires() != null) {
			this.tokenExpiry = new Date(System.currentTimeMillis() + (token.getExpires() * 1000));
		}
		this.profileName = user.getName();

		try {
			this.page = "";
			ResponseList<Account> accounts = this.connection.getAccounts();
			this.pages = new ArrayList<String>();
			if (accounts != null) {
				for (Account account : accounts) {
					this.page = account.getName();
					this.pages.add(account.getName());
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	    
	   /* Map<String, String> params = new HashMap<String, String>();
	    params.put("client_id", this.oauthKey);
	    params.put("client_secret", this.oauthSecret);
	    params.put("grant_type", "fb_exchange_token");
	    params.put("fb_exchange_token", token.getToken());

	    RawAPIResponse apiResponse = this.connection.callGetAPI("/oauth/access_token", params);

	    String response = apiResponse.asString();
	    AccessToken newAccessToken = new AccessToken(response);

	    this.connection.setOAuthAccessToken(newAccessToken);
	    setToken(newAccessToken.getToken());

		this.tokenExpiry = new Date(System.currentTimeMillis() + (newAccessToken.getExpires() * 1000));
		System.out.println(this.tokenExpiry);*/
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		this.userName = this.bot.memory().getProperty("Facebook.user");
		if (this.userName == null) {
			this.userName = "";
		}
		this.token = this.bot.memory().getProperty("Facebook.token");
		if (this.token == null) {
			this.token = "";
		}
		if (!this.token.isEmpty()) {
			setIsEnabled(true);
		}
		
		this.appOauthKey = this.bot.memory().getProperty("Facebook.appOauthKey");
		if (this.appOauthKey != null && !this.appOauthKey.isEmpty()) {
			this.appOauthKey = Utils.decrypt(Utils.KEY, this.appOauthKey);
		}
		if (this.appOauthKey == null) {
			this.appOauthKey = "";
		}
		
		this.appOauthSecret = this.bot.memory().getProperty("Facebook.appOauthSecret");
		if (this.appOauthSecret != null && !this.appOauthSecret.isEmpty()) {
			this.appOauthSecret = Utils.decrypt(Utils.KEY, this.appOauthSecret);
		}
		if (this.appOauthSecret == null) {
			this.appOauthSecret = "";
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
			getBot().memory().loadProperties("Facebook");
			Network memory = getBot().memory().newMemory();
			Vertex facebook = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("Facebook.tokenExpiry");
			if (property != null) {
				this.tokenExpiry = new Date(Long.valueOf(property));
			}
			property = this.bot.memory().getProperty("Facebook.welcomeMessage");
			if (property != null) {
				this.welcomeMessage = property;
			}
			property = this.bot.memory().getProperty("Facebook.profileName");
			if (property != null) {
				this.profileName = property;
			}
			property = this.bot.memory().getProperty("Facebook.page");
			if (property != null) {
				this.page = property;
			}
			property = this.bot.memory().getProperty("Facebook.pageId");
			if (property != null) {
				this.pageId = property;
			}
			property = this.bot.memory().getProperty("Facebook.autoFriend");
			if (property != null) {
				this.autoFriend = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.maxFriends");
			if (property != null) {
				this.maxFriends = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.maxPost");
			if (property != null) {
				this.maxPost = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.maxLike");
			if (property != null) {
				this.maxLike = Integer.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.processPost");
			if (property != null) {
				this.processPost = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.processAllPosts");
			if (property != null) {
				this.processAllPosts = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.processNewsFeed");
			if (property != null) {
				this.processNewsFeed = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.processAllNewsFeed");
			if (property != null) {
				this.processAllNewsFeed = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.likeAllPosts");
			if (property != null) {
				this.likeAllPosts = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.replyToMessages");
			if (property != null) {
				this.replyToMessages = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.facebookMessenger");
			if (property != null) {
				this.facebookMessenger = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.facebookMessengerAccessToken");
			if (property != null) {
				this.facebookMessengerAccessToken = property;
			}
			property = this.bot.memory().getProperty("Facebook.buttonType");
			if (property != null) {
				this.buttonType = property;
			}
			property = this.bot.memory().getProperty("Facebook.stripButtonText");
			if (property != null) {
				this.stripButtonText = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.trackMessageObjects");
			if (property != null) {
				this.trackMessageObjects = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.persistentMenu");
			if (property != null) {
				this.persistentMenu = property;
			}
			property = this.bot.memory().getProperty("Facebook.getStartedButton");
			if (property != null) {
				this.getStartedButton = property;
			}
			property = this.bot.memory().getProperty("Facebook.greetingText");
			if (property != null) {
				this.greetingText = property;
			}
			property = this.bot.memory().getProperty("Facebook.autoPost");
			if (property != null) {
				this.autoPost = Boolean.valueOf(property);
			}
			property = this.bot.memory().getProperty("Facebook.autoPostHours");
			if (property != null) {
				this.autoPostHours = Integer.valueOf(property);
			}
			this.statusKeywords = new ArrayList<String>();
			List<Relationship> keywords = facebook.orderedRelationships(Primitive.STATUSKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.statusKeywords.add(text);
					}
				}
			}
			this.newsFeedKeywords = new ArrayList<String>();
			keywords = facebook.orderedRelationships(Primitive.NEWSFEEDKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.newsFeedKeywords.add(text);
					}
				}
			}
			this.likeKeywords = new ArrayList<String>();
			keywords = facebook.orderedRelationships(Primitive.LIKEKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.likeKeywords.add(text);
					}
				}
			}
			this.autoFriendKeywords = new ArrayList<String>();
			List<Relationship> search = facebook.orderedRelationships(Primitive.AUTOFRIENDKEYWORDS);
			if (search != null) {
				for (Relationship relationship : search) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.autoFriendKeywords.add(text);
					}
				}
			}
			this.postRSS = new ArrayList<String>();
			List<Relationship> rss = facebook.orderedRelationships(Primitive.RSS);
			if (rss != null) {
				for (Relationship relationship : rss) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.postRSS.add(text);
					}
				}
			}
			this.rssKeywords = new ArrayList<String>();
			keywords = facebook.orderedRelationships(Primitive.RSSKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					this.rssKeywords.add(text);
				}
			}
			this.initProperties = true;
		}
	}

	/**
	 * Migrate to new properties system.
	 */
	public void migrateProperties() {
		Network memory = getBot().memory().newMemory();
		Vertex facebook = memory.createVertex(getPrimitive());
		
		// Load old properties.
		Vertex user = facebook.getRelationship(Primitive.USER);
		if (user != null) {
			this.userName = (String)user.getData();
		}
		Vertex token = facebook.getRelationship(Primitive.TOKEN);
		if (token != null) {
			this.token = (String)token.getData();
			setIsEnabled(true);
		}		
		Vertex tokenExpiry = facebook.getRelationship(Primitive.TOKENEXPIRY);
		if (tokenExpiry != null) {
			this.tokenExpiry = new Date((Long)tokenExpiry.getData());
		}
		Vertex property = facebook.getRelationship(Primitive.WELCOME);
		if (property != null) {
			this.welcomeMessage = (String)property.getData();
		}
		property = facebook.getRelationship(Primitive.NAME);
		if (property != null) {
			this.profileName = (String)property.getData();
		}
		property = facebook.getRelationship(Primitive.PAGE);
		if (property != null) {
			this.page = (String)property.getData();
		}
		property = facebook.getRelationship(Primitive.AUTOFRIEND);
		if (property != null) {
			this.autoFriend = (Boolean)property.getData();
		}
		property = facebook.getRelationship(Primitive.MAXFRIENDS);
		if (property != null) {
			this.maxFriends = ((Number)property.getData()).intValue();
		}
		property = facebook.getRelationship(Primitive.MAXSTATUSCHECKS);
		if (property != null) {
			this.maxPost = ((Number)property.getData()).intValue();
		}
		property = facebook.getRelationship(Primitive.PROCESSSTATUS);
		if (property != null) {
			this.processPost = (Boolean)property.getData();
		}
		this.statusKeywords = new ArrayList<String>();
		List<Relationship> keywords = facebook.orderedRelationships(Primitive.STATUSKEYWORDS);
		if (keywords != null) {
			for (Relationship relationship : keywords) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.statusKeywords.add(text);
				}
			}
		}
		this.likeKeywords = new ArrayList<String>();
		keywords = facebook.orderedRelationships(Primitive.LIKEKEYWORDS);
		if (keywords != null) {
			for (Relationship relationship : keywords) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.likeKeywords.add(text);
				}
			}
		}
		this.autoFriendKeywords = new ArrayList<String>();
		List<Relationship> search = facebook.orderedRelationships(Primitive.AUTOFRIENDKEYWORDS);
		if (search != null) {
			for (Relationship relationship : search) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.autoFriendKeywords.add(text);
				}
			}
		}
		this.postRSS = new ArrayList<String>();
		List<Relationship> rss = facebook.orderedRelationships(Primitive.RSS);
		if (rss != null) {
			for (Relationship relationship : rss) {
				String text = ((String)relationship.getTarget().getData()).trim();
				if (!text.isEmpty()) {
					this.postRSS.add(text);
				}
			}
		}
		this.rssKeywords = new ArrayList<String>();
		keywords = facebook.orderedRelationships(Primitive.RSSKEYWORDS);
		if (keywords != null) {
			for (Relationship relationship : keywords) {
				String text = ((String)relationship.getTarget().getData()).trim();
				this.rssKeywords.add(text);
			}
		}
		property = facebook.getRelationship(Primitive.REPLYTOMESSAGES);
		if (property != null) {
			this.replyToMessages = (Boolean)property.getData();
		}
		property = facebook.getRelationship(Primitive.AUTOPOST);
		if (property != null) {
			this.autoPost = (Boolean)property.getData();
		}
		property = facebook.getRelationship(Primitive.AUTOPOSTHOURS);
		if (property != null) {
			this.autoPostHours = ((Number)property.getData()).intValue();
		}
		
		// Remove old properties.
		facebook.unpinChildren();
		facebook.internalRemoveRelationships(Primitive.USER);
		facebook.internalRemoveRelationships(Primitive.TOKEN);
		facebook.internalRemoveRelationships(Primitive.TOKENEXPIRY);
		facebook.internalRemoveRelationships(Primitive.PAGE);
		facebook.internalRemoveRelationships(Primitive.NAME);
		facebook.internalRemoveRelationships(Primitive.WELCOME);
		facebook.internalRemoveRelationships(Primitive.AUTOFRIEND);
		facebook.internalRemoveRelationships(Primitive.MAXFRIENDS);
		facebook.internalRemoveRelationships(Primitive.MAXSTATUSCHECKS);
		facebook.internalRemoveRelationships(Primitive.PROCESSSTATUS);
		facebook.internalRemoveRelationships(Primitive.REPLYTOMESSAGES);
		facebook.internalRemoveRelationships(Primitive.AUTOPOST);
		facebook.internalRemoveRelationships(Primitive.AUTOPOSTHOURS);
		
		memory.save();
		
		saveProperties(null);
	}

	public void saveProperties(List<String> autoPosts) {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Facebook.user", this.userName, true);
		memory.saveProperty("Facebook.token", this.token, true);
		if (this.appOauthKey == null || this.appOauthKey.isEmpty()) {
			memory.saveProperty("Facebook.appOauthKey", "", true);
		} else {
			memory.saveProperty("Facebook.appOauthKey", Utils.encrypt(Utils.KEY, this.appOauthKey), true);
		}
		if (this.appOauthKey == null || this.appOauthKey.isEmpty()) {
			memory.saveProperty("Facebook.appOauthSecret", "", true);
		} else {
			memory.saveProperty("Facebook.appOauthSecret", Utils.encrypt(Utils.KEY, this.appOauthSecret), true);
		}
		
		if (this.tokenExpiry == null) {
			memory.removeProperty("Facebook.tokenExpiry");
		} else {
			memory.saveProperty("Facebook.tokenExpiry", String.valueOf(this.tokenExpiry.getTime()), false);
		}

		memory.saveProperty("Facebook.page", this.page, false);
		memory.saveProperty("Facebook.pageId", this.pageId, false);
		memory.saveProperty("Facebook.profileName", this.profileName, false);
		memory.saveProperty("Facebook.welcomeMessage", this.welcomeMessage, false);
		memory.saveProperty("Facebook.autoFriend", String.valueOf(this.autoFriend), false);
		memory.saveProperty("Facebook.maxFriends", String.valueOf(this.maxFriends), false);
		memory.saveProperty("Facebook.maxPost", String.valueOf(this.maxPost), false);
		memory.saveProperty("Facebook.maxLike", String.valueOf(this.maxLike), false);
		memory.saveProperty("Facebook.processPost", String.valueOf(this.processPost), false);
		memory.saveProperty("Facebook.processAllPosts", String.valueOf(this.processAllPosts), false);
		memory.saveProperty("Facebook.processNewsFeed", String.valueOf(this.processNewsFeed), false);
		memory.saveProperty("Facebook.processAllNewsFeed", String.valueOf(this.processAllNewsFeed), false);
		memory.saveProperty("Facebook.likeAllPosts", String.valueOf(this.likeAllPosts), false);
		memory.saveProperty("Facebook.autoFriend", String.valueOf(this.autoFriend), false);
		memory.saveProperty("Facebook.replyToMessages", String.valueOf(this.replyToMessages), false);
		memory.saveProperty("Facebook.facebookMessenger", String.valueOf(this.facebookMessenger), false);
		memory.saveProperty("Facebook.facebookMessengerAccessToken", String.valueOf(this.facebookMessengerAccessToken), false);
		memory.saveProperty("Facebook.buttonType", String.valueOf(this.buttonType), false);
		memory.saveProperty("Facebook.stripButtonText", String.valueOf(this.stripButtonText), false);
		memory.saveProperty("Facebook.trackMessageObjects", String.valueOf(this.trackMessageObjects), false);
		memory.saveProperty("Facebook.persistentMenu", String.valueOf(this.persistentMenu), false);
		memory.saveProperty("Facebook.getStartedButton", String.valueOf(this.getStartedButton), false);
		memory.saveProperty("Facebook.greetingText", String.valueOf(this.greetingText), false);
		memory.saveProperty("Facebook.autoPost", String.valueOf(this.autoPost), false);
		memory.saveProperty("Facebook.autoPostHours", String.valueOf(this.autoPostHours), false);

		Vertex facebook = memory.createVertex(getPrimitive());
		facebook.unpinChildren();
		facebook.internalRemoveRelationships(Primitive.STATUSKEYWORDS);
		for (String text : this.statusKeywords) {
			Vertex keywords =  memory.createVertex(text);
			facebook.addRelationship(Primitive.STATUSKEYWORDS, keywords);
		}
		facebook.internalRemoveRelationships(Primitive.NEWSFEEDKEYWORDS);
		for (String text : this.newsFeedKeywords) {
			Vertex keywords =  memory.createVertex(text);
			facebook.addRelationship(Primitive.NEWSFEEDKEYWORDS, keywords);
		}
		facebook.internalRemoveRelationships(Primitive.LIKEKEYWORDS);
		for (String text : this.likeKeywords) {
			Vertex keywords =  memory.createVertex(text);
			facebook.addRelationship(Primitive.LIKEKEYWORDS, keywords);
		}
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
				post.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
				facebook.addRelationship(Primitive.AUTOPOSTS, post);
			}
		}

		facebook.pinChildren();
		memory.save();
	}

	/**
	 * Set the "Persistent Menu" in the page's Facebook Messenger.
	 */
	public void updateThreadSettings(String persistentMenu, String getStartedButton, String greetingText) {
		try {
			if (getFacebookMessengerAccessToken() == null || getFacebookMessengerAccessToken().isEmpty()) {
				return;
			}
			boolean newAPI = persistentMenu.contains("*");
			String url = null;
			if (newAPI) {
				url = "https://graph.facebook.com/v2.6/me/messenger_profile?access_token=";
			} else {
				url = "https://graph.facebook.com/v2.6/me/thread_settings?access_token=";
			}
			url = url + getFacebookMessengerAccessToken();
			
			// Update Persistent Menu
			persistentMenu = persistentMenu.trim();
			if (persistentMenu.isEmpty()) {
				// Delete menu.
				log("DELETE", Level.INFO, url);
				String json = null;
				if (newAPI) {
					json = "{ \"fields\":[ \"persistent_menu\" ] }";
				} else {
					json = "{ \"setting_type\" : \"call_to_actions\",\"thread_state\" : \"existing_thread\" }";
				}
				String response = Utils.httpDELETE(url, "application/json", json);
				log("Response", Level.INFO, response);
			} else {
				String json = null;
				if (persistentMenu.startsWith("{")) {
					// Allow raw JSON
					json = persistentMenu;
				} else {
					TextStream stream = new TextStream(persistentMenu);
					if (stream.atEnd()) {
						// Delete menu.
						log("DELETE", Level.INFO, url);
						json = "{ \"setting_type\" : \"call_to_actions\",\"thread_state\" : \"existing_thread\" }";
						String response = Utils.httpDELETE(url, "application/json", json);
						log("Response", Level.INFO, response);
					} else {
						if (newAPI) {
							json = "{ \"persistent_menu\":[ { \"locale\":\"default\", \"call_to_actions\":[ ";
						} else {
							json = "{ \"setting_type\" : \"call_to_actions\",\"thread_state\" : \"existing_thread\", \"call_to_actions\" : [ ";
						}
						boolean first = true;
						int nested = 0;
						String nextLine = null;
						String line = null;
						while (!stream.atEnd() || (nextLine != null && !nextLine.isEmpty())) {
							if (nextLine != null) {
								line = nextLine;
								nextLine = null;
							} else {
								line = stream.nextLine().trim();
							}
							nextLine = stream.nextLine().trim();
							List<String> tokens = null;
							if (line.startsWith("**")) {
								tokens = Utils.csv(line.substring(2, line.length()));
							} else if (line.startsWith("*")) {
								tokens = Utils.csv(line.substring(1, line.length()));
							} else {
								tokens = Utils.csv(line);
							}
							if (tokens.isEmpty()) {
								continue;
							}
							// Check for 2 level nested menus using * and **
							if (nested == 0) {
								if (nextLine.startsWith("*")) {
									nested = 1;
									String title = tokens.get(0);
									if (!first) {
										json = json + ", ";
									}
									json = json + "{ \"type\":\"nested\", \"title\":\"" + title + "\", \"call_to_actions\": [ ";
									first = true;
									continue;
								}
							} else if (nested == 1) {
								if (nextLine.startsWith("**")) {
									nested = 2;
									String title = tokens.get(0);
									if (!first) {
										json = json + ", ";
									}
									json = json + "{ \"type\":\"nested\", \"title\":\"" + title + "\", \"call_to_actions\": [ ";
									first = true;
									continue;
								}
							}
							if (nested == 1 && !line.startsWith("*")) {
								json = json + " ] }";
								nested = 0;
							}
							if (nested == 2 && !line.startsWith("**")) {
								json = json + " ] }";
								nested = 1;
							}
							String title = tokens.get(0).trim();
							String payload = title;
							if (tokens.size() > 1) {
								payload = tokens.get(1).trim();
							}
							if (!first) {
								json = json + ", ";
							} else {
								first = false;
							}
							if (payload.startsWith("http")) {
								json = json + "{ \"type\":\"web_url\", \"title\":\"" + title + "\", \"url\":\"" + payload + "\"}";
							} else {
								json = json + "{ \"type\":\"postback\", \"title\":\"" + title + "\", \"payload\":\"" + payload + "\"}";
							}
						}
						while (nested > 0) {
							json = json + " ] }";
							nested--;
						}
						json = json + " ] }";
					}
				}
				log("POST", Level.INFO, url, json);
				String response = Utils.httpPOST(url, "application/json", json);
				log("Response", Level.INFO, response);
			}
			
			// Update Get Started Button
			getStartedButton = getStartedButton.trim();
			if (getStartedButton.isEmpty()) {
				// Delete menu.
				log("DELETE", Level.INFO, url);
				String json = null;
				if (newAPI) {
					json = "{ \"fields\":[ \"get_started\" ] }";
				} else {
					json = "{ \"setting_type\" : \"call_to_actions\",\"thread_state\" : \"new_thread\" }";
				}
				String response = Utils.httpDELETE(url, "application/json", json);
				log("Response", Level.INFO, response);
			} else {
				String json = null;
				if (getStartedButton.startsWith("{")) {
					// Allow raw JSON
					json = getStartedButton;
				} else {
					if (newAPI) {
						json = "{ \"get_started\":{ \"payload\":\"" + getStartedButton + "\" }";
					} else {
						json = "{ \"setting_type\" : \"call_to_actions\",\"thread_state\" : \"new_thread\", \"call_to_actions\" : [ { \"payload\" : \"" + getStartedButton + "\" } ] }";
					}
				}
				log("POST", Level.INFO, url, json);
				String response = Utils.httpPOST(url, "application/json", json);
				log("Response", Level.INFO, response);
			}
			
			// Update Greeting Text
			greetingText = greetingText.trim();
			if (greetingText.isEmpty()) {
				// Delete menu.
				log("DELETE", Level.INFO, url);
				String json = null;
				if (newAPI) {
					json = "{ \"fields\":[ \"greeting\" ] }";
				} else {
					json = "{ \"setting_type\" : \"greeting\" }";
				}
				String response = Utils.httpDELETE(url, "application/json", json);
				log("Response", Level.INFO, response);
			} else {
				String json = null;
				if (greetingText.startsWith("{")) {
					// Allow raw JSON
					json = greetingText;
				} else {
					if (newAPI) {
						json = "{ \"greeting\":[ { \"locale\":\"default\", \"text\":\"" + greetingText + "\" } ] }";
					} else {
						json = "{ \"setting_type\" : \"greeting\", \"greeting\" : { \"text\" : \"" + greetingText + "\" } }";
					}
				}
				log("POST", Level.INFO, url, json);
				String response = Utils.httpPOST(url, "application/json", json);
				log("Response", Level.INFO, response);
			}
		} catch (Exception exception) {
			throw new BotException(exception);
		}
	}
	
	public void connect() throws FacebookException {
		initProperties();
		ConfigurationBuilder config = new ConfigurationBuilder();
		String key = getOauthKey();
		String secret = getOauthSecret();
		if (this.appOauthKey != null && !this.appOauthKey.isEmpty()) {
			key = this.appOauthKey;
		}
		if (this.appOauthSecret != null && !this.appOauthSecret.isEmpty()) {
			secret = this.appOauthSecret;
		}
		config.setOAuthAppId(key);
		config.setOAuthAppSecret(secret);
		config.setOAuthAccessToken(getToken());
		facebook4j.Facebook facebook = new FacebookFactory(config.build()).getInstance();
        setConnection(facebook);
	}
	
	public void connectAccount() throws FacebookException {
		connect();
		facebook4j.Facebook facebook = getConnection();
		User user = facebook.getMe();
		if (this.userName == null || !this.userName.equals(user.getId())) {
			this.userName = user.getId();
			this.profileName = user.getName();
		}
		this.pageId = "";
		if (this.page != null && !this.page.isEmpty()) {
			if (facebook.getPage() == null || !facebook.getPage().getName().equals(this.page)) {
				// Reset page access token.
				boolean found = false;
				ResponseList<Account> accounts = this.connection.getAccounts();
				if (accounts != null) {
					for (Account account : accounts) {
						if (this.page.equals(account.getName())) {
							found = true;
							this.token = account.getAccessToken();
							this.userName = account.getId();
							this.profileName = account.getName();

							String key = getOauthKey();
							String secret = getOauthSecret();
							if (this.appOauthKey != null && !this.appOauthKey.isEmpty()) {
								key = this.appOauthKey;
							}
							if (this.appOauthSecret != null && !this.appOauthSecret.isEmpty()) {
								secret = this.appOauthSecret;
							}
							ConfigurationBuilder config = new ConfigurationBuilder();
							config.setOAuthAppId(key);
							config.setOAuthAppSecret(secret);
							config.setOAuthAccessToken(getToken());
							facebook = new FacebookFactory(config.build()).getInstance();
					        setConnection(facebook);
					        this.pageId = facebook.getPage().getId();
							log("Connected to Facebook page", Level.INFO, facebook.getPage().getId(), facebook.getPage().getName());
						}
					}
				}
				if (!found) {
					throw new BotException("Page missing");
				}
			}
		}
		saveProperties(null);
	}
	
	public void subscribeToMessenger() throws FacebookException {
		if (getFacebookMessenger()) {
			if (this.page != null && !this.page.isEmpty() && (getConnection().getPage() != null)) {
				try {
					String url = "https://graph.facebook.com/v2.6/" + getConnection().getPage().getId() + "/subscribed_apps";
					log("Subscribing to Facebook Messenger", Level.INFO);
					Map<String, String> params = new HashMap<String, String>();
					params.put("access_token", getFacebookMessengerAccessToken());
					String result = Utils.httpPOST(url, params);
					log("Facebook Messenger subscription success", Level.INFO, result);
				} catch (Exception exception) {
					log(exception);
				}
			} else {
				log("Facebook Messenger only allowed for page messaging", Level.WARNING);
			}
		}
	}

	/**
	 * Check profile for messages.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.INFO);
		this.processedPosts = new HashSet<String>();
		this.wallPosts = new HashSet<String>();
		try {
			//checkFriends();
			checkWall();
			//checkNewsFeed();
			checkRSS();
			checkAutoPost();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.INFO);
	}

	/**
	 * Check wall posts.
	 */
	public void checkWall() {
		if (!getProcessPost()) {
			return;
		}
		log("Checking wall posts", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex facebook = memory.createVertex(getPrimitive());
			Vertex vertex = facebook.getRelationship(Primitive.LASTTIMELINE);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			long max = 0;
			ResponseList<Post> timeline = null;
			boolean more = true;
			int page = 1;
			int count = 0;
			int like = 0;
			this.errors = 0;
			while (more && (count <= this.maxPost) && page <= this.maxPage) {
				if (last == 0) {
					timeline = getConnection().getFeed(new Reading().fields("id", "message", "caption", "description", "created_time", "from"));
					more = false;
				} else {
					Reading paging = new Reading();
					paging.fields("id", "message", "caption", "description", "created_time", "from");
					max = last;
					paging.since(new Date(last));
					timeline = getConnection().getFeed(paging);
					if ((timeline == null) || (timeline.size() < 20)) {
						more = false;
					}
					page++;
				}
				if ((timeline == null) || timeline.isEmpty()) {
					log("No new wall posts", Level.FINE);
					break;
				}
				log("Processing posts", Level.INFO, timeline.size());
			    for (int index = timeline.size() - 1; index >= 0; index--) {
				    if (count >= this.maxPost) {
						log("Max posts", Level.FINE, count);
				    	break;
				    }
			    	if (this.errors > this.maxErrors) {
						log("Max errors", Level.WARNING, this.errors);
			    		break;
			    	}
			    	Post post = timeline.get(index);
			    	String userId = post.getFrom() == null ? "anonymous" : post.getFrom().getId();
			    	String userName = post.getFrom() == null ? "anonymous" : post.getFrom().getName();
					log("Processing post", Level.FINE, post.getId(), userName, post.getCaption());
		    		long postTime = post.getCreatedTime().getTime();
		    		String postId = post.getId();
			    	if (postTime > max) {
			    		max = postTime;
			    	}
			    	if (!userId.equals(this.userName)) {
				    	if ((System.currentTimeMillis() - postTime) > DAY) {
							log("Day old post", Level.INFO, postId, postTime);
				    		more = false;
				    		continue;
				    	}
				    	if (postTime > last) {
					    	boolean match = false;
					    	String message = post.getMessage();
					    	if (message == null || message.isEmpty()) {
					    		message = post.getCaption();
					    	}
					    	if (getLikeAllPosts()) {
							    if (like >= this.maxLike) {
									log("Max like", Level.FINE, like);
							    } else {
						    		like++;
					    			like(post);
								    Utils.sleep(500);
							    }
					    	}
					    	if (message != null && !message.isEmpty()) {
						    	List<String> postWords = new TextStream(message.toLowerCase()).allWords();
					    		// Like
						    	if (!getLikeAllPosts()) {
						    		for (String keywords : getLikeKeywords()) {
										List<String> keyWords = new TextStream(keywords.toLowerCase()).allWords();
								    	if (!keyWords.isEmpty()) {
								    		if (postWords.containsAll(keyWords)) {
											    if (like >= this.maxLike) {
													log("Max like", Level.FINE, like);
											    } else {
										    		like++;
									    			like(post);
												    Utils.sleep(500);
											    }
											    break;
									    	}
								    	}
						    		}
						    	}
					    		// Reply.
						    	for (String text : getPostKeywords()) {
						    		List<String> keywords = new TextStream(text.toLowerCase()).allWords();
						    		if (!keywords.isEmpty() && postWords.containsAll(keywords)) {
						    			match = true;
						    			break;
						    		}
						    	}
						    	if (match || getProcessAllPosts()) {
						    		count++;
						    		log("Processing post", Level.FINE, post.getCaption(), post.getDescription(), post.getMessage(), userId, userName);
						    		this.wallPosts.add(post.getId());
							    	input(post);
								    Utils.sleep(500);
						    	} else {
									log("Skipping post, missing keywords.", Level.FINE, post.getCaption(), post.getDescription(), post.getMessage());
						    	}
					    	} else {
								log("Empty message", Level.FINE, post);
					    	}
				    	} else {
							log("Old post", Level.INFO, postId, postTime);				    		
				    	}
			    	} else {
						log("Ignoring own post", Level.INFO, postId);				    		
			    	}
			    }
			}
			// Process comments.
			if (count <= this.maxPost) {
				timeline = getConnection().getFeed(new Reading().fields("id", "from", "created_time", "comments"));
				if ((timeline != null) && !timeline.isEmpty()) {
					log("Processing post comments", Level.INFO, timeline.size());
				    for (int index = timeline.size() - 1; index >= 0; index--) {
					    if (count >= this.maxPost) {
							log("Max posts", Level.FINE, count);
					    	break;
					    }
				    	if (this.errors > this.maxErrors) {
							log("Max errors", Level.WARNING, this.errors);
				    		break;
				    	}
				    	Post post = timeline.get(index);
						log("Processing post comments", Level.FINE, post.getId(), post.getCaption(), post.getDescription(), post.getMessage());
				    	PagableList<Comment> comments = post.getComments();
				    	if (comments != null && !comments.isEmpty()) {
					    	for (Comment comment : comments) {
					    		long[] values = processComment(comment, null, memory, count, max, last);
					    		if (values == null) {
					    			break;
					    		}
					    		count = (int)values[0];
					    		max = values[1];
					    		if (count == -1) {
					    			break;
					    		}
								ResponseList<Comment> replies = getConnection().getCommentReplies(comment.getId());
								if ((replies != null) && !replies.isEmpty()) {
							    	for (int index2 = replies.size() - 1; index2 >= 0; index2--) {
							    		Comment reply = replies.get(index2);
							    		values = processComment(reply, comment, memory, count, max, last);
							    		if (values == null) {
							    			break;
							    		}
							    		count = (int)values[0];
							    		max = values[1];
							    	}
								}
					    	}
				    	} else {
							log("No comments", Level.FINE, post.getId());
						}
				    }
				}
			} else {
				log("Max posts", Level.FINE, count);
			}
		    if (max != 0) {
				facebook.setRelationship(Primitive.LASTTIMELINE, memory.createVertex(max));
		    	memory.save();
		    }
		} catch (Exception exception) {
			log(exception);
		}
	}

	public long[] processComment(Comment comment, Comment parent, Network memory, int count, long max, long last) {
    	String userId = comment.getFrom() == null ? "anonymous" : comment.getFrom().getId();
    	String userName = comment.getFrom() == null ? "anonymous" : comment.getFrom().getName();
		log("Processing post comment", Level.FINE, comment.getId(), userName, comment.getMessage());
	    if (count >= this.maxPost) {
			log("Max posts", Level.FINE, count);
    		return null;
	    }
    	if (this.errors > this.maxErrors) {
			log("Max errors", Level.WARNING, this.errors);
    		return null;
    	}
		long postTime = comment.getCreatedTime().getTime();
		String postId = comment.getId();
    	if (postTime > max) {
    		max = postTime;
    	}
    	if (!userId.equals(this.userName)) {
	    	if ((System.currentTimeMillis() - postTime) > DAY) {
				log("Day old post comment", Level.FINE, postId, postTime);
	    		return null;
	    	}
	    	if (postTime > last) {
		    	boolean match = false;
	    		List<String> postWords = new TextStream(comment.getMessage().toLowerCase()).allWords();
		    	for (String text : getPostKeywords()) {
		    		List<String> keywords = new TextStream(text.toLowerCase()).allWords();
		    		if (!keywords.isEmpty() && postWords.containsAll(keywords)) {
		    			match = true;
		    			break;
		    		}
		    	}
		    	if (match || getProcessAllPosts()) {
		    		count++;
		    		log("Processing post comment", Level.FINE, comment.getMessage(), userId, userName);
			    	input(comment, parent, memory);
				    Utils.sleep(500);
		    	} else {
					log("Skipping post comment, missing keywords.", Level.FINE, comment.getMessage());
		    	}
	    	} else {
				log("Old post comment", Level.INFO, postId, postTime);				    		
	    	}
    	} else {
			log("Ignoring own comment", Level.INFO, postId);				    		
    	}
    	long[] values = new long[2];
    	values[0] = count;
    	values[1] = max;
    	return values;
	}

	/**
	 * Check news feed posts.
	 */
	public void checkNewsFeed() {
		if (isPage() || !getProcessNewsFeed()) {
			return;
		}
		log("Checking news feed posts", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex facebook = memory.createVertex(getPrimitive());
			Vertex vertex = facebook.getRelationship(Primitive.LASTNEWSFEED);
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
			long max = 0;
			ResponseList<Post> timeline = null;
			boolean more = true;
			int page = 1;
			int count = 0;
			int like = 0;
			this.errors = 0;
			while (more && (count <= this.maxPost) && page <= this.maxPage) {
				if (last == 0) {
					timeline = getConnection().getHome(new Reading().fields("id", "message", "caption", "description", "created_time", "from"));
					more = false;
				} else {
					Reading paging = new Reading();
					paging.fields("id", "message", "caption", "description", "created_time", "from");
					max = last;
					paging.since(new Date(last));
					timeline = getConnection().getHome(paging);
					if ((timeline == null) || (timeline.size() < 20)) {
						more = false;
					}
					page++;
				}
				if ((timeline == null) || timeline.isEmpty()) {
					log("Empty news feed", Level.FINE);
					break;
				}
				log("Processing posts", Level.INFO, timeline.size());
			    for (int index = timeline.size() - 1; index >= 0; index--) {
				    if (count >= this.maxPost) {
						log("Max posts", Level.FINE, count);
				    	break;
				    }
			    	if (this.errors > this.maxErrors) {
						log("Max errors", Level.WARNING, this.errors);
			    		break;
			    	}
			    	Post post = timeline.get(index);
			    	String userId = post.getFrom() == null ? "anonymous" : post.getFrom().getId();
			    	String userName = post.getFrom() == null ? "anonymous" : post.getFrom().getName();
					log("Processing post", Level.FINE, post.getId(), userName, post.getCaption());
		    		long postTime = post.getCreatedTime().getTime();
		    		String postId = post.getId();
			    	if (postTime > max) {
			    		max = postTime;
			    	}
			    	if (!userId.equals(this.userName)) {
				    	if ((System.currentTimeMillis() - postTime) > DAY) {
							log("Day old post", Level.INFO, postId, postTime);
				    		more = false;
				    		continue;
				    	}
				    	if (postTime > last) {
					    	boolean match = false;
					    	String message = post.getMessage();
					    	if (message == null || message.isEmpty()) {
					    		message = post.getCaption();
					    	}
					    	if (getLikeAllPosts()) {
							    if (like >= this.maxLike) {
									log("Max like", Level.FINE, like);
							    } else {
						    		like++;
					    			like(post);
								    Utils.sleep(500);
							    }
					    	}
					    	if (message != null && !message.isEmpty()) {
						    	List<String> postWords = new TextStream(message.toLowerCase()).allWords();
					    		// Like
						    	if (getLikeAllPosts()) {
						    		for (String keywords : getLikeKeywords()) {
										List<String> keyWords = new TextStream(keywords.toLowerCase()).allWords();
								    	if (!keyWords.isEmpty()) {
								    		if (postWords.containsAll(keyWords)) {
											    if (like >= this.maxLike) {
													log("Max like", Level.FINE, like);
											    } else {
										    		like++;
									    			like(post);
												    Utils.sleep(500);
											    }
											    break;
									    	}
								    	}
						    		}
						    	}
					    		// Reply.
						    	for (String text : getNewsFeedKeywords()) {
						    		List<String> keywords = new TextStream(text.toLowerCase()).allWords();
						    		if (!keywords.isEmpty() && postWords.containsAll(keywords)) {
						    			match = true;
						    			break;
						    		}
						    	}
						    	if (match || getProcessAllNewsFeed()) {
						    		count++;
						    		log("Processing post", Level.FINE, post.getCaption(), post.getDescription(), post.getMessage(), userId, userName);
							    	input(post);
								    Utils.sleep(500);
						    	} else {
									log("Skipping post, missing keywords.", Level.FINE, post.getCaption(), post.getDescription(), post.getMessage());
						    	}
					    	} else {
								log("Empty message", Level.FINE, post);
					    	}
				    	} else {
							log("Old post", Level.INFO, postId, postTime);				    		
				    	}
			    	}
			    }
			}
		    if (max != 0) {
				facebook.setRelationship(Primitive.LASTNEWSFEED, memory.createVertex(max));
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
	
	public boolean isPage() {
		return getPage() != null && !getPage().isEmpty();
	}

	/**
	 * Return the list of friends names.
	 */
	public List<String> getFriends() {
		List<String> friends = new ArrayList<String>();
		try {
			if (isPage()) {
				return friends;
			}
			ResponseList<Friend> list = getConnection().getFriends();
		    for (Friend user : list) {
		    	friends.add(user.getName());
		    	if (friends.size() >= 100) {
				    // Only return first 100.
				    break;
		    	}
			}
		} catch (Exception exception) {
			log(exception);
		}
	    return friends;
	}

	/**
	 * Return the time-line.
	 */
	public List<String> getTimeline() {
		List<String> timeline = new ArrayList<String>();
		try {
		    ResponseList<Post> statuses = getConnection().getFeed();
		    if (statuses != null) {
			    for (Post status : statuses) {
			    	timeline.add(String.valueOf(status.getCreatedTime()) + " - <b>"
			    			+ (status.getFrom() == null ? "" : String.valueOf(status.getFrom().getName())) + "</b>:  "
			    			+ String.valueOf(status.getMessage()));
			    }
		    }
		} catch (Exception exception) {
			log(exception);
			throw new BotException(exception);
		}
	    return timeline;
	}

	/**
	 * Check friends.
	 */
	public void checkFriends() {
		
	}
	
	/*
	public boolean checkFriendship(long friend) throws FacebookException {
		long[] lookup = new long[1];
		lookup[0] = friend;
		ResponseList<User> users = getConnection().lookupUsers(lookup);
		User user = users.get(0);
		if (user.getName().equals(getUserName())) {
			return false;
		}
		if (!getAutoFriendKeywords().isEmpty()) {
			StringWriter writer = new StringWriter();
			writer.write(user.getName().toLowerCase());
			writer.write(" ");
			writer.write(user.getDescription().toLowerCase());
			writer.write(" ");
			writer.write(user.getLocation().toLowerCase());
			writer.write(" ");
			writer.write(user.getLang().toLowerCase());
			writer.write(" ");
			writer.write(user.getName().toLowerCase());
	    	boolean match = false;
	    	for (String text : getAutoFriendKeywords()) {
	    		List<String> keywords = new TextStream(text.toLowerCase()).allWords();
	    		if (new TextStream(writer.toString()).allWords().containsAll(keywords)) {
	    			match = true;
	    			break;
	    		}
	    	}
	    	if (!match) {
				log("Autofollow skipping friend, does not match keywords", Level.FINE, user.getName());
	    		return false;
	    	}
		}
		Network memory = getBot().memory().newMemory();
    	Vertex speaker = memory.createSpeaker(user.getName());
    	// Only try to follow a user once.
    	if (!speaker.hasRelationship(Primitive.FOLLOWED)) {
			log("Adding autofollow friend.", Level.INFO, user.getName());
	    	speaker.addRelationship(Primitive.FOLLOWED, memory.createTimestamp());
	    	memory.save();
			getConnection().createFriendship(friend);
			Utils.sleep(1000);
			if (!getWelcomeMessage().isEmpty()) {
				sendMessage(getWelcomeMessage(), user.getName());
			}
			return true;
    	}
		log("Autofollow skipping friend, already followed once", Level.FINE, user.getName());
    	return false;
	}*/
	
	public void log(FacebookException exception) {
		log(new TextStream(exception.toString()).nextLine(), Bot.WARNING);		
	}

	/**
	 * Post.
	 */
	public void post(String text, String reply) {
		this.posts++;
		log("Posting:", Level.INFO, text, reply);
		try {
			if (getConnection() == null) {
				connect();
			}
			if (reply != null) {
				getConnection().commentPost(reply, format(text));
			} else {
				PostUpdate update = new PostUpdate(format(text));
				getConnection().postFeed(update);
			}
		} catch (Exception exception) {
			this.errors++;
			log(exception.getMessage(), Level.WARNING, text);
		}
	}
	
	/**
	 * Prepare and format the text for Facebook.
	 */
	public String format(String text) {
		text = text.replace("\n", "");
		text = text.replace("\r", "");
		text = text.replace("\\", ""); // Facebook does not like this character for some reason.
		text = Utils.stripTags(text);
		// Strip tags changes <br> to \n
		text = text.replace("\n", " \\n");
		text = text.trim();
		while (text.endsWith("\\n")) {
			text = text.substring(0, text.length() - "\\n".length());
		}
		return text;
	}

	/**
	 * Send a message to the user.
	 */
	public void sendMessage(Vertex source, Vertex message, Vertex conversationId) {
		if (message.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(message.getNetwork().createInstance(Primitive.INPUT), null, message.getNetwork(), variables);
			message = getBot().mind().getThought(Language.class).evaluateFormula(message, variables, message.getNetwork());
			if (message == null) {
				log("Invalid template formula", Level.WARNING, message);
				return;
			}
		}
		String text = getBot().mind().getThought(Language.class).getWord(message, message.getNetwork()).printString();
		sendMessage(text, "", conversationId.printString());
	}

	/**
	 * Send a message to the user.
	 */
	public void sendMessage(String text, String replyUser, String id) {
		log("Sending message:", Level.INFO, text, replyUser);
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("message", format(text));
			getConnection().callPostAPI("/" + id + "/messages", params);
		} catch (Exception exception) {
			this.errors++;
			log(exception);
		}
	}

	/**
	 * Send a message to the user.
	 */
	public void sendFacebookMessengerMessage(String text, String replyUser, String id) {
		log("Sending messenger message:", Level.INFO, text, replyUser);
		try {
			if (isPage() && (!getPageId().isEmpty())) {
				try {
					String url = "https://graph.facebook.com/v2.6/" + getPageId() + "/messages?access_token=" + getFacebookMessengerAccessToken();
					// Check for HTML content and strip button elements.
					String strippedText = text;
					if (getStripButtonText()) {
						strippedText = Utils.stripTag(strippedText, "button");
					}
					strippedText = format(strippedText);
					String postText = null;
					// Max size limit
					if (strippedText.length() >= 320) {
						TextStream stream = new TextStream(strippedText);
						while (!stream.atEnd()) {
							String message = stream.nextParagraph(320);
							String json = null;
							if (stream.atEnd()) {
								postText = message;
							} else {
								json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(message) + "\"}}";
								log("POST", Level.INFO, json);
								Utils.httpPOST(url, "application/json", json);
								Utils.sleep(500);
							}
						}
					} else {
						postText = strippedText;
					}
					Element root = null;
					boolean linkFound = false;
					// Check for HTML content - first check for link to convert, otherwise images (only one of the other).
					// This is complicated as link use button which require the text, and images can't have text.
					if ((text.indexOf('<') != -1) && (text.indexOf('>') != -1)) {
						try {
							root = getBot().awareness().getSense(Http.class).parseHTML(text);
							// Find image for template, fb only allows one image.
							NodeList imageNodes = root.getElementsByTagName("img");
							String image = null;
							String imageTitle = "...";
							if (imageNodes.getLength() > 0) {
								String src = ((Element)imageNodes.item(0)).getAttribute("src");
								if (src != null && !src.isEmpty()) {
									image = src;
									String title = ((Element)imageNodes.item(0)).getAttribute("title");
									if (title != null && !title.isEmpty()) {
										imageTitle = title;
									}
								}
							}
							// Check for <a> link tags.
							NodeList nodes = root.getElementsByTagName("a");
							List<String> extraButtons = new ArrayList<String>();
							if (nodes.getLength() > 0) {
								String buttonJSON = "";
								String postText2 = postText;
								String href = "";
								int count = 0;
								for (int index = 0; index  < nodes.getLength(); index++) {
									Element node = (Element)nodes.item(index);
									String button = null;
									NodeList buttonNodes = node.getElementsByTagName("button");
									if (buttonNodes.getLength() > 0) {
										String buttonText = buttonNodes.item(0).getTextContent().trim();
										if (buttonText != null && !buttonText.isEmpty()) {
											button = buttonText;
										}
									}
									href = node.getAttribute("href");
									String link = node.getTextContent().trim();
									if (link.isEmpty()) {
										link = "Link";
									}
									if (button != null) {
										link = button;
									}
									if (link.length() > 80) {
										link = link.substring(0, 80);
									}
									if (postText2 == null) {
										postText2 = link;
									}
									if (href != null && !href.isEmpty()) {
										if (count < 3) {
											count++;
											if (!buttonJSON.isEmpty()) {
												buttonJSON = buttonJSON + ", ";
											}
											if (href.startsWith("chat:")) {
												String chat = href.substring("chat:".length(), href.length()).trim();
												buttonJSON = buttonJSON + "{ type: \"postback\", payload: \"" + chat + "\", title: \"" + Utils.escapeQuotesJS(link) + "\"}";
											} else {
												buttonJSON = buttonJSON + "{ type: \"web_url\", url: \"" + href + "\", title: \"" + Utils.escapeQuotesJS(link) + "\"}";
											}
											linkFound = true;
										} else {
											extraButtons.add(button);
										}
									}
								}
								if (linkFound) {
									String json = null;
									if (image == null) {
										json = "{recipient:{id:\""
												+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"button\", text: \""
												+ Utils.escapeQuotesJS(postText2) + "\", buttons: [ " + buttonJSON + " ]}}}}";
									} else {
										// Generic template does not have text.
										json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(postText2) + "\"}}";
										log("POST", Level.INFO, json);
										Utils.httpPOST(url, "application/json", json);
										Utils.sleep(500);
										postText = null;
										json = "{recipient:{id:\""
												+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"generic\", elements:[{ image_url: \""
												+ image + "\", title: \"" + imageTitle + "\", item_url: \"" + href + "\", buttons: [ " + buttonJSON + " ]}]}}}}";
									}
									log("POST", Level.INFO, json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
									postText = null;
								}
							} else {
								// Check for <button> tags.
								nodes = root.getElementsByTagName("button");
								boolean quickReply = getButtonType().equals("quickReply") || 
										(getButtonType().equals("auto") && nodes.getLength() > 3 && nodes.getLength() <= 11);
								if (image == null && quickReply) {
									String buttonJSON = "";
									String postText2 = postText;
									for (int index = 0; index  < (Math.min(11, nodes.getLength())); index++) {
										Element node = (Element)nodes.item(index);
										String button = node.getTextContent().trim();
										if (button != null && !button.isEmpty()) {
											if (postText2 == null) {
												postText2 = button;
											}
											if (!buttonJSON.isEmpty()) {
												buttonJSON = buttonJSON + ", ";
											}
											button = Utils.escapeQuotesJS(button);
											buttonJSON = buttonJSON + "{ content_type: \"text\", payload: \"" + button + "\", title: \"" + button + "\"}";
											linkFound = true;
										}
									}
									if (linkFound) {
										String json = "{recipient:{id:\""
													+ id + "\"}, message:{ text: \"" + Utils.escapeQuotesJS(postText2) + "\", quick_replies: [ " + buttonJSON + " ]}}";
										log("POST", Level.INFO, json);
										Utils.httpPOST(url, "application/json", json);
										Utils.sleep(500);
										postText = null;
									}
								} else if (nodes.getLength() > 0) {
									String buttonJSON = "";
									String postText2 = postText;
									int count = 0;
									for (int index = 0; index  < nodes.getLength(); index++) {
										Element node = (Element)nodes.item(index);
										String button = node.getTextContent().trim();
										if (button != null && !button.isEmpty()) {
											if (postText2 == null) {
												postText2 = button;
											}
											if (count < 3) {
												if (!buttonJSON.isEmpty()) {
													buttonJSON = buttonJSON + ", ";
												}
												button = Utils.escapeQuotesJS(button);
												buttonJSON = buttonJSON + "{ type: \"postback\", payload: \"" + button + "\", title: \"" + button + "\"}";
												linkFound = true;
											} else {
												extraButtons.add(button);
											}
											count++;
										}
									}
									if (linkFound) {
										String json = null;
										if (image == null) {
											json = "{recipient:{id:\""
													+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"button\", text: \""
													+ Utils.escapeQuotesJS(postText2) + "\", buttons: [ " + buttonJSON + " ]}}}}";
										} else {
											// Generic template does not have text.
											json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(postText) + "\"}}";
											log("POST", Level.INFO, json);
											Utils.httpPOST(url, "application/json", json);
											Utils.sleep(500);
											postText = null;
											json = "{recipient:{id:\""
													+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"generic\", elements:[{ image_url: \""
													+ image + "\", title: \"" + imageTitle+ "\", buttons: [ " + buttonJSON + " ]}]}}}}";
										}
										log("POST", Level.INFO, json);
										Utils.httpPOST(url, "application/json", json);
										Utils.sleep(500);
										postText = null;
									}
								} else {
									// Check for <select>/<option> tags.
									nodes = root.getElementsByTagName("option");
									quickReply = getButtonType().equals("quickReply") || 
											(getButtonType().equals("auto") && nodes.getLength() > 3 && nodes.getLength() <= 11);
									if (image == null && quickReply) {
										String buttonJSON = "";
										String postText2 = postText;
										for (int index = 0; index  < (Math.min(11, nodes.getLength())); index++) {
											Element node = (Element)nodes.item(index);
											String button = node.getTextContent().trim();
											if (button != null && !button.isEmpty()) {
												if (postText2 == null) {
													postText2 = button;
												}
												if (!buttonJSON.isEmpty()) {
													buttonJSON = buttonJSON + ", ";
												}
												button = Utils.escapeQuotesJS(button);
												buttonJSON = buttonJSON + "{ content_type: \"text\", payload: \"" + button + "\", title: \"" + button + "\"}";
												linkFound = true;
											}
										}
										if (linkFound) {
											String json = "{recipient:{id:\""
														+ id + "\"}, message:{ text: \"" + Utils.escapeQuotesJS(postText2) + "\", quick_replies: [ " + buttonJSON + " ]}}";
											log("POST", Level.INFO, json);
											Utils.httpPOST(url, "application/json", json);
											Utils.sleep(500);
											postText = null;
										}
									} else if (nodes.getLength() > 0) {
										String buttonJSON = "";
										String postText2 = postText;
										int count = 0;
										for (int index = 0; index  < nodes.getLength(); index++) {
											Element node = (Element)nodes.item(index);
											String button = node.getTextContent().trim();
											if (button != null && !button.isEmpty()) {
												if (postText2 == null) {
													postText2 = button;
												}
												if (count < 3) {
													if (!buttonJSON.isEmpty()) {
														buttonJSON = buttonJSON + ", ";
													}
													button = Utils.escapeQuotesJS(button);
													buttonJSON = buttonJSON + "{ type: \"postback\", payload: \"" + button + "\", title: \"" + button + "\"}";
													linkFound = true;
												} else {
													extraButtons.add(button);
												}
												count++;
											}
										}
										if (linkFound) {
											String json = null;
											if (image == null) {
												json = "{recipient:{id:\""
														+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"button\", text: \""
														+ Utils.escapeQuotesJS(postText2) + "\", buttons: [ " + buttonJSON + " ]}}}}";
											} else {
												// Generic template does not have text.
												json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(postText) + "\"}}";
												log("POST", Level.INFO, json);
												Utils.httpPOST(url, "application/json", json);
												Utils.sleep(500);
												postText = null;
												json = "{recipient:{id:\""
														+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"generic\", elements:[{ image_url: \""
														+ image + "\", title: \"" + imageTitle+ "\", buttons: [ " + buttonJSON + " ]}]}}}}";
											}
											log("POST", Level.INFO, json);
											Utils.httpPOST(url, "application/json", json);
											Utils.sleep(500);
											postText = null;
										}
									}
								}
							}
							int count = 0;
							String buttonJSON = "";
							for (String button : extraButtons) {
								button = Utils.escapeQuotesJS(button);
								if (!buttonJSON.isEmpty()) {
									buttonJSON = buttonJSON + ", ";
								}
								buttonJSON = buttonJSON + "{ type: \"postback\", payload: \"" + button + "\", title: \"" + button + "\"}";
								count++;
								if (count == 3 || count == extraButtons.size()) {
									String json = "{recipient:{id:\""
											+ id + "\"}, message:{ attachment: { type: \"template\", payload: { template_type: \"button\", text: \""
											+ "..." + "\", buttons: [ " + buttonJSON + " ]}}}}";
									log("POST", Level.INFO, json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
									buttonJSON = "";
									count = 0;
								}
							}
						} catch (Exception exception) {
							log(exception);
						}
					}
					if (postText != null) {
						String json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(postText) + "\"}}";
						log("POST", Level.INFO, json);
						Utils.httpPOST(url, "application/json", json);
						Utils.sleep(500);
					}
					if (!linkFound && (text.indexOf('<') != -1) && (text.indexOf('>') != -1)) {
						try {
							NodeList nodes = root.getElementsByTagName("img");
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								if (src != null && !src.isEmpty()) {
									String json = "{recipient:{id:\"" + id + "\"}, message:{ attachment:{ type: \"image\", payload: { url: \"" + src + "\"}}}}";
									log("POST", Level.INFO, json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
								}
							}
							nodes = root.getElementsByTagName("audio");
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								if (src != null && !src.isEmpty()) {
									String json = "{recipient:{id:\"" + id + "\"}, message:{ attachment:{ type: \"audio\", payload: { url: \"" + src + "\"}}}}";
									log("POST", Level.INFO, json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
								}
							}
							nodes = root.getElementsByTagName("video");
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								if (src != null && !src.isEmpty()) {
									String json = "{recipient:{id:\"" + id + "\"}, message:{ attachment:{ type: \"video\", payload: { url: \"" + src + "\"}}}}";
									log("POST", Level.INFO, json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
								}
							}
						} catch (Exception exception) {
							log(exception);
						}
					}
				} catch (Exception exception) {
					this.errors++;
					log(exception);
				}
			} else {
				log("Facebook Messenger only allowed for page messaging", Level.WARNING);
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	/**
	 * Send a button template message to the user.
	 */
	public void sendFacebookMessengerButtonMessage(String text, String command, String replyUser, String id) {
		log("Sending messenger message:", Level.INFO, text, replyUser);
		try {
			if (isPage() && (!getPageId().isEmpty())) {
				try {
					String url = "https://graph.facebook.com/v2.6/" + getPageId() + "/messages?access_token=" + getFacebookMessengerAccessToken();
					String strippedText = format(text);
					String quickReply = createJSONQuickReply(command, id, strippedText);
					// Max size limit
					if (strippedText.length() >= 320) {
						TextStream stream = new TextStream(strippedText);
						while (!stream.atEnd()) {
							String json = null;
							String message = stream.nextParagraph(320);
							if (stream.atEnd() && quickReply.isEmpty()) {
								json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(message) + "\"}}";
							} else {
								json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(message)
										+ "\", quick_replies:" + quickReply + "}}";
							}
							log("POST", Level.INFO, json);
							Utils.httpPOST(url, "application/json", json);
							Utils.sleep(500);
						}
					} else if (!strippedText.isEmpty()) {
						String json = null;
						if (quickReply.isEmpty()) {
							json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(strippedText) + "\"}}";
						} else {
							json = "{recipient:{id:\"" + id + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(strippedText)
									+ "\", quick_replies:" + quickReply + "}}";
						}
						log("POST", Level.INFO, json);
						Utils.httpPOST(url, "application/json", json);
						Utils.sleep(500);
					}
					String attachment = createJSONAttachment(command, id, strippedText);
					if (!attachment.isEmpty()) {
						String json = "{recipient:{id:\"" + id + "\"}, message:{attachment:" + attachment + "}}";
						log("POST", Level.INFO, json);
						Utils.httpPOST(url, "application/json", json);
					}
				} catch (Exception exception) {
					this.errors++;
					log(exception);
				}
			} else {
				log("Facebook Messenger only allowed for page messaging", Level.WARNING);
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Like the post.
	 */
	public void like(Post post) {
		this.likes++;
		String userName = post.getFrom() == null ? "anonymous" : post.getFrom().getName();
		log("Liking:", Level.INFO, post.getCaption(), userName);
		try {
			if (getConnection() == null) {
				connect();
			}
			getConnection().likePost(post.getId());
		} catch (Exception exception) {
			if (exception.getMessage() != null && exception.getMessage().contains("authorized") && exception.getMessage().contains("endpoint")) {
				this.errors = this.errors + 5;				
			}
			this.errors++;
			log(exception.toString(), Level.WARNING, post.getCaption());
		}
	}

	/**
	 * Output the post if facebook is connected.
	 */
	public void outputPost(String post) {
		if (!isEnabled()) {
			return;
		}
		Network network = getBot().memory().newMemory();
		Vertex setence = network.createSentence(post);
		Vertex output = network.createInstance(Primitive.INPUT);
		output.setName(post);
		output.addRelationship(Primitive.INPUT, setence);
		output.addRelationship(Primitive.SENSE, getPrimitive());
		output.addRelationship(Primitive.SPEAKER, Primitive.SELF);
		output.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		Vertex target = output.mostConscious(Primitive.TARGET);
		if (target != null) {
			String replyTo = target.mostConscious(Primitive.WORD).getData().toString();
			post = "@" + replyTo + " " + post;
		}
		network.save();
		post(post, null);
	}
	
	/**
	 * Process the post.
	 */
	@Override
	public void input(Object input, Network network) {
		if (!isEnabled()) {
			return;
		}
		try {
			if (input instanceof Post) {
				Post post = (Post)input;
    			log("Processing post", Bot.FINE, post.getCaption(), post.getId());
		    	if ((System.currentTimeMillis() - post.getCreatedTime().getTime()) > DAY) {
	    			log("Day old post", Bot.FINE, post.getId(), post.getCreatedTime().getTime());
		    		return;
		    	}
		    	if (this.processedPosts.contains(post.getId())) {
	    			log("Already processed post", Bot.FINE, post.getCaption(), post.getMessage(), post.getId());
		    		return;
		    	}
	    		this.processedPosts.add(post.getId());
		    	String name = post.getFrom() == null ? "anonymous" : post.getFrom().getName();
		    	String message = post.getMessage();
		    	if (message == null || message.isEmpty()) {
		    		message = post.getCaption();
		    	}
		    	if (message == null || message.isEmpty()) {
	    			log("Empty post", Bot.FINE, post.getCaption(), post.getMessage(), post.getId());
		    		return;
		    	}
		    	String text = message.trim();
				log("Input post", Level.FINE, post.getMessage(), name);
				this.postsProcessed++;
				String target = null;
				if (this.wallPosts.contains(post.getId())) {
					target = this.userName;
				}
				inputSentence(text, name, target, post.getId(), post.getCreatedTime().getTime(), network);
			} else if (input instanceof Comment) {
				Comment comment = (Comment)input;
    			log("Processing post comment", Bot.FINE, comment.getMessage(), comment.getId());
		    	if ((System.currentTimeMillis() - comment.getCreatedTime().getTime()) > DAY) {
	    			log("Day old post commentt", Bot.FINE, comment.getId(), comment.getCreatedTime().getTime());
		    		return;
		    	}
		    	if (this.processedPosts.contains(comment)) {
	    			log("Already processed post comment", Bot.FINE, comment.getMessage(), comment.getId());
		    		return;
		    	}
	    		this.processedPosts.add(comment.getId());
		    	String name = comment.getFrom() == null ? "anonymous" : comment.getFrom().getName();
		    	String text = comment.getMessage().trim();
				log("Input post comment", Level.FINE, comment.getMessage(), name);
				this.postsProcessed++;
				String id = comment.getId();
				if (comment.getParent() != null) {
					id = comment.getParent().getId();
				}
				inputSentence(text, name, this.userName, id, comment.getCreatedTime().getTime(), network);
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	/**
	 * Process the post comment.
	 */
	public void input(Comment comment, Comment parent, Network network) {
		if (!isEnabled()) {
			return;
		}
		try {
			log("Processing post comment", Bot.FINE, comment.getMessage(), comment.getId());
	    	if ((System.currentTimeMillis() - comment.getCreatedTime().getTime()) > DAY) {
    			log("Day old post commentt", Bot.FINE, comment.getId(), comment.getCreatedTime().getTime());
	    		return;
	    	}
	    	if (this.processedPosts.contains(comment)) {
    			log("Already processed post comment", Bot.FINE, comment.getMessage(), comment.getId());
	    		return;
	    	}
    		this.processedPosts.add(comment.getId());
	    	String name = comment.getFrom() == null ? "anonymous" : comment.getFrom().getName();
	    	String text = comment.getMessage().trim();
			log("Input post comment", Level.FINE, comment.getMessage(), name);
			this.postsProcessed++;
			String id = comment.getId();
			if (parent != null) {
				id = parent.getId();
			}
			inputSentence(text, name, this.userName, id, comment.getCreatedTime().getTime(), network);
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	/**
	 * Output the post or direct message reply.
	 */
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to facebook, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		output.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		String text = printInput(output);
		Vertex question = output.getRelationship(Primitive.QUESTION);
		String reply = null;
		if (question != null) {
			Vertex id = question.getRelationship(Primitive.ID);
			if (id != null) {
				reply = (String)id.getData();
			}
		}
		post(text, reply);
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
	public void inputSentence(String text, String userName, String targetUserName, String messageId, long time, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex sentence = input.getRelationship(Primitive.INPUT);
		Vertex id = network.createVertex(messageId);
		if (sentence.hasRelationship(Primitive.POST, id)) {
			log("Post already processed", Bot.FINE, id, time);			
			return;
		}
		sentence.addRelationship(Primitive.POST, id);
		input.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		input.addRelationship(Primitive.CREATEDAT, network.createVertex(time));
		input.addRelationship(Primitive.ID, id);
		Vertex conversation = network.createInstance(Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.POST);
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		return input;
	}

	public String getOauthKey() {
		return oauthKey;
	}

	public void setOauthKey(String oauthKey) {
		Facebook.oauthKey = oauthKey;
	}

	public String getOauthSecret() {
		return oauthSecret;
	}

	public void setOauthSecret(String oauthSecret) {
		Facebook.oauthSecret = oauthSecret;
	}

	public String getToken() {
		return token;
	}

	public Date getTokenExpiry() {
		return tokenExpiry;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public facebook4j.Facebook getConnection() throws FacebookException {
		if (connection == null) {
			connect();
		}
		return connection;
	}

	public void setConnection(facebook4j.Facebook connection) {
		this.connection = connection;
	}

	public boolean getAutoFriend() {
		initProperties();
		return autoFriend;
	}

	public void setAutoFriend(boolean autoFriend) {
		initProperties();
		this.autoFriend = autoFriend;
	}

	public int getMaxFriends() {
		initProperties();
		return maxFriends;
	}

	public void setMaxFriends(int maxFriends) {
		initProperties();
		this.maxFriends = maxFriends;
	}

	public boolean getProcessPost() {
		initProperties();
		return processPost;
	}

	public void setProcessPost(boolean processPost) {
		initProperties();
		this.processPost = processPost;
	}

	public boolean getReplyToMessages() {
		initProperties();
		return replyToMessages;
	}

	public boolean getFacebookMessenger() {
		initProperties();
		return facebookMessenger;
	}

	public void setFacebookMessenger(boolean facebookMessenger) {
		this.facebookMessenger = facebookMessenger;
	}

	public void setReplyToMessages(boolean replyToMessages) {
		this.replyToMessages = replyToMessages;
	}

	public List<String> getLikeKeywords() {
		initProperties();
		return likeKeywords;
	}

	public void setLikeKeywords(List<String> likeKeywords) {
		initProperties();
		this.likeKeywords = likeKeywords;
	}

	public List<String> getPostRSS() {
		initProperties();
		return postRSS;
	}

	public void setPostRSS(List<String> postRSS) {
		initProperties();
		this.postRSS = postRSS;
	}

	public int getMaxPost() {
		initProperties();
		return maxPost;
	}

	public void setMaxPost(int maxPost) {
		initProperties();
		this.maxPost = maxPost;
	}

	public int getMaxLike() {
		initProperties();
		return maxLike;
	}

	public void setMaxLike(int maxLike) {
		initProperties();
		this.maxLike = maxLike;
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
		getBot().stat("facebook.post");
		post(post, null);
	}

	// Self API
	public void postComment(Vertex source, Vertex sentence, Vertex postId) {
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
		getBot().stat("facebook.post");
		post(post, postId.printString());
	}

	/**
	 * Check the command JSON for a Facebook attachment object to append to a Facebook Messeneger message.
	 */
	public String createJSONAttachment(String command, String id, String text) {		
		JSONObject root = (JSONObject)JSONSerializer.toJSON(command);
		if (!root.has("type") || !root.getString("type").equals("facebook")) {
			return "";
		}
		JSONObject json = root.getJSONObject("attachment");
		if (json == null) {
			return "";
		}
		return json.toString();
	}

	/**
	 * Check the command JSON for a Facebook attachment object to append to a Facebook Messeneger message.
	 */
	public String createJSONQuickReply(String command, String id, String text) {		
		JSONObject root = (JSONObject)JSONSerializer.toJSON(command);
		if (!root.has("type") || !root.getString("type").equals("facebook")) {
			return "";
		}
		Object json = root.get("quick_replies");
		if (json == null) {
			return "";
		}
		return json.toString();
	}
}