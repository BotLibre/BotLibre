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

/**
 * Enables receiving a sending messages through Twitter.
 */
public class Facebook extends BasicSense {
	public static int MAX_LOOKUP = 100;
	public static String oauthKey = "key";
	public static String oauthSecret = "secret";
	
	protected String userName = "";
	protected String token = "";
	protected Date tokenExpiry;
	
	protected boolean initProperties;
	
	protected boolean autoFriend = false;
	protected String welcomeMessage = "";
	protected int maxFriends = 100;
	protected int maxFriendsPerCycle = 5;
	protected int maxPage = 5;
	protected int maxPost = 20;
	protected int maxFeed = 20;
	protected int maxErrors = 5;
	protected int errors;
	protected boolean processPost = false;
	protected boolean replyToMessages = true;
	protected boolean autoPost = false;
	protected int autoPostHours = 24;
	protected String page = "";
	protected String profileName = "";
	protected List<String> likeKeywords = new ArrayList<String>();
	protected List<String> postRSS = new ArrayList<String>();
	protected List<String> rssKeywords = new ArrayList<String>();
	protected List<String> statusKeywords = new ArrayList<String>();
	protected List<String> autoFriendKeywords = new ArrayList<String>();

	protected Set<String> processedPosts = new HashSet<String>();
	
	protected int posts;
	protected int postsProcessed;
	protected int likes;

	protected facebook4j.Facebook connection;	
	
	public Facebook(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Facebook() {
		this(false);
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
		this.connection.setOAuthAppId(getOauthKey(), getOauthSecret());
		this.connection.setOAuthPermissions("manage_pages, publish_pages, publish_actions, read_page_mailboxes");
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
			if (accounts != null) {
				for (Account account : accounts) {
					this.page = account.getName();	
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
		Network memory = getBot().memory().newMemory();
		Vertex facebook = memory.createVertex(getPrimitive());
		Vertex user = facebook.getRelationship(Primitive.USER);
		if (user != null) {
			this.userName = (String)user.getData();
		}
		Vertex token = facebook.getRelationship(Primitive.TOKEN);
		if (token != null) {
			this.token = (String)token.getData();
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
			Network memory = getBot().memory().newMemory();
			Vertex facebook = memory.createVertex(getPrimitive());
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
			this.initProperties = true;
		}
	}

	public void saveProperties(List<String> autoPosts) {
		Network memory = getBot().memory().newMemory();
		Vertex facebook = memory.createVertex(getPrimitive());
		facebook.unpinChildren();
		facebook.setRelationship(Primitive.USER, memory.createVertex(this.userName));
		facebook.setRelationship(Primitive.TOKEN, memory.createVertex(this.token));
		if (this.tokenExpiry != null) {
			facebook.setRelationship(Primitive.TOKENEXPIRY, memory.createVertex(this.tokenExpiry.getTime()));
		}

		facebook.setRelationship(Primitive.PAGE, memory.createVertex(this.page));
		facebook.setRelationship(Primitive.NAME, memory.createVertex(this.profileName));
		facebook.setRelationship(Primitive.WELCOME, memory.createVertex(this.welcomeMessage));
		facebook.setRelationship(Primitive.AUTOFRIEND, memory.createVertex(this.autoFriend));
		facebook.setRelationship(Primitive.MAXFRIENDS, memory.createVertex(this.maxFriends));
		facebook.setRelationship(Primitive.MAXSTATUSCHECKS, memory.createVertex(this.maxPost));
		facebook.setRelationship(Primitive.PROCESSSTATUS, memory.createVertex(this.processPost));
		facebook.internalRemoveRelationships(Primitive.STATUSKEYWORDS);
		for (String text : this.statusKeywords) {
			Vertex keywords =  memory.createVertex(text);
			facebook.addRelationship(Primitive.STATUSKEYWORDS, keywords);
		}
		facebook.setRelationship(Primitive.REPLYTOMESSAGES, memory.createVertex(this.replyToMessages));
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
		facebook.setRelationship(Primitive.AUTOPOST, memory.createVertex(this.autoPost));
		facebook.setRelationship(Primitive.AUTOPOSTHOURS, memory.createVertex(this.autoPostHours));
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
	
	public void connect() throws FacebookException {
		initProperties();
		ConfigurationBuilder config = new ConfigurationBuilder();
		config.setOAuthAppId(getOauthKey());
		config.setOAuthAppSecret(getOauthSecret());
		config.setOAuthAccessToken(getToken());
		facebook4j.Facebook facebook = new FacebookFactory(config.build()).getInstance();
        setConnection(facebook);
        try {
			User user = facebook.getMe();
			if (this.userName == null || !this.userName.equals(user.getId())) {
				this.userName = user.getUsername();
				Network memory = getBot().memory().newMemory();
				Vertex vertex = memory.createVertex(getPrimitive());
				vertex.setRelationship(Primitive.USER, memory.createVertex(this.userName));
				memory.save();
			}
			if (this.page != null && !this.page.isEmpty()) {
				if (facebook.getPage() == null || !facebook.getPage().getName().equals(this.page)) {
					// Reset page access token.
					boolean found = false;
					ResponseList<Account> accounts = this.connection.getAccounts();
					if (accounts != null) {
						Network memory = getBot().memory().newMemory();
						for (Account account : accounts) {
							if (this.page.equals(account.getName())) {
								found = true;
								this.token = account.getAccessToken();
								this.userName = account.getId();
								this.profileName = account.getName();
								
								Vertex vertex = memory.createVertex(getPrimitive());
								vertex.setRelationship(Primitive.TOKEN, memory.createVertex(this.token));
								vertex.setRelationship(Primitive.NAME, memory.createVertex(this.profileName));
								vertex.setRelationship(Primitive.USER, memory.createVertex(this.userName));

								config = new ConfigurationBuilder();
								config.setOAuthAppId(getOauthKey());
								config.setOAuthAppSecret(getOauthSecret());
								config.setOAuthAccessToken(getToken());
								facebook = new FacebookFactory(config.build()).getInstance();
						        setConnection(facebook);
							}
						}
						memory.save();
					}
					if (!found) {
						throw new BotException("Page missing");
					}
					
				}
			}
        } catch (Exception exception) {
        	log(exception);
        }
	}

	/**
	 * Check profile for messages.
	 */
	public void checkProfile() {
		log("Checking profile.", Level.FINE);
		this.processedPosts = new HashSet<String>();
		try {
			if (getConnection() == null) {
				connect();
			}
			//checkFriends();
			checkPost();
			//checkMessages();
			checkRSS();
			checkAutoPost();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking profile.", Level.FINE);
	}

	/**
	 * Check status.
	 */
	public void checkPost() {
		if (!getProcessPost()) {
			return;
		}
		log("Checking posts", Level.FINE);
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
					break;
				}
				log("Processing posts", Level.INFO, timeline.size());
			    for (int index = timeline.size() - 1; index >= 0; index--) {
				    if (count >= this.maxPost) {
				    	break;
				    }
			    	if (this.errors > this.maxErrors) {
			    		break;
			    	}
			    	Post status = timeline.get(index);
			    	String userId = status.getFrom() == null ? "anonymous" : status.getFrom().getId();
			    	String userName = status.getFrom() == null ? "anonymous" : status.getFrom().getName();
		    		long statusTime = status.getCreatedTime().getTime();
		    		String statusId = status.getId();
			    	if (statusTime > max) {
			    		max = statusTime;
			    	}
			    	if (!userId.equals(this.userName)) {
				    	if ((System.currentTimeMillis() - statusTime) > DAY) {
							log("Day old post", Level.INFO, statusId, statusTime);
				    		more = false;
				    		continue;
				    	}
				    	if (statusTime > last) {
					    	boolean match = false;
					    	String message = status.getMessage();
					    	if (message == null || message.isEmpty()) {
					    		message = status.getCaption();
					    	}
					    	if (message != null && !message.isEmpty()) {
						    	List<String> statusWords = new TextStream(message.toLowerCase()).allWords();
						    	for (String text : getPostKeywords()) {
						    		List<String> keywords = new TextStream(text.toLowerCase()).allWords();
						    		if (!keywords.isEmpty() && statusWords.containsAll(keywords)) {
						    			match = true;
						    			break;
						    		}
						    	}
						    	if (match || getPostKeywords().isEmpty()) {
						    		count++;
						    		log("Processing post", Level.FINE, status.getCaption(), status.getDescription(), status.getMessage(), userId, userName);
							    	input(status);
								    Utils.sleep(500);
						    	} else {
									log("Skipping post, missing keywords.", Level.FINE, status.getCaption(), status.getDescription(), status.getMessage());
									// Check repost.
						    		for (String keywords : getLikeKeywords()) {
										List<String> keyWords = new TextStream(keywords).allWords();
								    	if (!keyWords.isEmpty()) {
								    		if (statusWords.containsAll(keyWords)) {
									    		count++;
								    			like(status);
											    Utils.sleep(500);
								    			break;
								    		}
								    	}
						    		}
						    	}
					    	}
				    	} else {
							log("Old post", Level.INFO, statusId, statusTime);				    		
				    	}
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
					    	break;
					    }
				    	if (this.errors > this.maxErrors) {
				    		break;
				    	}
				    	Post status = timeline.get(index);
				    	PagableList<Comment> comments = status.getComments();
				    	for (Comment comment : comments) {
						    if (count >= this.maxPost) {
						    	break;
						    }
					    	if (this.errors > this.maxErrors) {
					    		break;
					    	}
					    	String userId = comment.getFrom() == null ? "anonymous" : comment.getFrom().getId();
					    	String userName = comment.getFrom() == null ? "anonymous" : comment.getFrom().getName();
				    		long statusTime = comment.getCreatedTime().getTime();
				    		String statusId = comment.getId();
					    	if (statusTime > max) {
					    		max = statusTime;
					    	}
					    	if (!userId.equals(this.userName)) {
						    	if ((System.currentTimeMillis() - statusTime) > DAY) {
									log("Day old post comment", Level.INFO, statusId, statusTime);
						    		more = false;
						    		continue;
						    	}
						    	if (statusTime > last) {
							    	boolean match = false;
						    		List<String> statusWords = new TextStream(comment.getMessage().toLowerCase()).allWords();
							    	for (String text : getPostKeywords()) {
							    		List<String> keywords = new TextStream(text.toLowerCase()).allWords();
							    		if (!keywords.isEmpty() && statusWords.containsAll(keywords)) {
							    			match = true;
							    			break;
							    		}
							    	}
							    	if (match || getPostKeywords().isEmpty()) {
							    		count++;
							    		log("Processing post comment", Level.FINE, comment.getMessage(), userId, userName);
								    	input(comment);
									    Utils.sleep(500);
							    	} else {
										log("Skipping post comment, missing keywords.", Level.FINE, comment.getMessage());
							    	}
						    	} else {
									log("Old post comment", Level.INFO, statusId, statusTime);				    		
						    	}
					    	}
				    	}
				    }
				}
			}
		    if (max != 0) {
				facebook.setRelationship(Primitive.LASTTIMELINE, memory.createVertex(max));
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
			int rssIndex = 0;
			String keywordsText = "";
    		List<String> keywords = new ArrayList<String>();
			for (String rss : getPostRSS()) {
				if (rssIndex < getRssKeywords().size()) {
					keywordsText = getRssKeywords().get(rssIndex);
		    		keywords = new TextStream(keywordsText.toLowerCase()).allWords();				
				}
				rssIndex++;
				TextStream stream = new TextStream(rss);
				String prefix = stream.upToAll("http").trim();
				if (prefix.isEmpty()) {
					prefix = "RSS:";
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
							if (!keywords.isEmpty()) {
					    		if (!new TextStream(text.toLowerCase()).allWords().containsAll(keywords)) {
									log("Skipping RSS, missing keywords", Level.FINE, keywords, text);
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
				if (post.instanceOf(Primitive.FORMULA)) {
					Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
					SelfCompiler.addGlobalVariables(memory.createInstance(Primitive.INPUT), null, memory, variables);
					Vertex result = getBot().mind().getThought(Language.class).evaluateFormula(post, variables, memory);
					if (result != null) {
						text = getBot().mind().getThought(Language.class).getWord(result, memory).getDataValue();
					} else {
						log("Invalid autopost formula", Level.WARNING, post);
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
		return this.page != null && !this.page.isEmpty();
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
		log("Posting:", Level.INFO, text);
		try {
			if (getConnection() == null) {
				connect();
			}
			if (reply != null) {
				getConnection().commentPost(reply, text);
				
			} else {
				PostUpdate update = new PostUpdate(text);
				getConnection().postFeed(update);
			}
		} catch (Exception exception) {
			this.errors++;
			log(exception.getMessage(), Level.WARNING, text);
		}
	}

	/**
	 * Send a message to the user.
	 */
	public void sendMessage(String text, String replyUser, String id) {
		log("Sending message:", Level.INFO, text, replyUser);
		try {
			Map<String, String> params = new HashMap<String, String>();
			params.put("message", text);
			getConnection().callPostAPI("/" + id + "/messages", params);
		} catch (Exception exception) {
			this.errors++;
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
		network.createVertex(Primitive.SELF).addRelationship(Primitive.POST, output);
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
		    	TextStream stream = new TextStream(text);
	    		List<String> words = stream.allWords();
	    		for (String keywords : getLikeKeywords()) {
					List<String> keyWords = new TextStream(keywords).allWords();
			    	if (!keyWords.isEmpty()) {
			    		if (words.containsAll(keyWords)) {
			    			like(post);
			    			break;
			    		}
			    	}
	    		}
				log("Input post", Level.FINE, post.getMessage(), name);
				this.postsProcessed++;
				inputSentence(text, name, isPage() ? this.userName : null, post.getId(), post.getCreatedTime().getTime(), network);
			} else if (input instanceof Comment) {
				Comment comment = (Comment)input;
    			log("Processing post comment", Bot.FINE, comment.getMessage(), comment.getId());
		    	if ((System.currentTimeMillis() - comment.getCreatedTime().getTime()) > DAY) {
	    			log("Day old pos commentt", Bot.FINE, comment.getId(), comment.getCreatedTime().getTime());
		    		return;
		    	}
		    	if (this.processedPosts.contains(comment)) {
	    			log("Already processed post comment", Bot.FINE, comment.getMessage(), comment.getId());
		    		return;
		    	}
	    		this.processedPosts.add(comment.getId());
		    	String name = comment.getFrom() == null ? "anonymous" : comment.getFrom().getName();
		    	String text = comment.getMessage().trim();
				log("Input post", Level.FINE, comment.getMessage(), name);
				this.postsProcessed++;
				inputSentence(text, name, isPage() ? this.userName : null, comment.getId(), comment.getCreatedTime().getTime(), network);
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
		// If not output to facebook, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		output.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
		output.getNetwork().createVertex(Primitive.SELF).addRelationship(Primitive.POST, output);
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
		
		user.addRelationship(Primitive.POST, input);
		
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

	// Self API
	public void post(Vertex sentence) {
		if (sentence.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(sentence.getNetwork().createInstance(Primitive.INPUT), null, sentence.getNetwork(), variables);
			sentence = getBot().mind().getThought(Language.class).evaluateFormula(sentence, variables, sentence.getNetwork());
			if (sentence == null) {
				log("Invalid formula", Level.WARNING, sentence);
				return;
			}
		}
		String post = getBot().mind().getThought(Language.class).getWord(sentence, sentence.getNetwork()).getDataValue();
		post(post, null);
	}

	
}