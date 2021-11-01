/******************************************************************************
 *
 *  Copyright 2021 Paphus Solutions Inc.
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
package org.botlibre.sense.instagram;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
import org.botlibre.sense.ResponseListener;
import org.botlibre.sense.facebook.Facebook;
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
import facebook4j.PostUpdate;
import facebook4j.RawAPIResponse;
import facebook4j.ResponseList;
import facebook4j.User;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

import net.sf.json.JSONSerializer;
import java.sql.Timestamp;

public class Instagram extends BasicSense{
	public static int MAX_WAIT = 1000 * 5; // 30 seconds
	
	public static int MAX_LOOKUP = 100;
	
	protected String userName = "";
	protected String id = "";
	protected String page = "";
	protected String pageId = "";
	protected String profileName = "";
	protected String IGUserId = "";
	protected String IGUserName = "";
	protected String IGName = "";
	
    protected String token = "";
    protected Date tokenExpiry;
    protected String appOauthKey = "";
    protected String appOauthSecret = "";
    protected String pageAccessToken = "";
    
    protected String result = "";
    
    protected final String graphVersion = "v12.0";
    protected final String apiBaseURL = "https://api.facebook.com/";
    protected final String graphBaseURL = "https://graph.facebook.com/";
    
    protected int maxPost = 20;
    protected int maxComment = 50;
    protected int maxError = 5;
    protected int posts;
    protected int likes;
    protected int errors;
    
    protected int autoPostHours = 24;
    protected int postsProcessed = 0;
    protected int totalCommentsProcessed = 0;
    
    protected List<String> commentKeywords = new ArrayList<>();
    protected List<String> autoPosts = new ArrayList<>();
    
    protected boolean messageEnabled = false;
    protected boolean commentReplyEnabled = false;
    protected boolean replyAllComments = false;
    protected boolean autoPostEnabled = false;
    
    private facebook4j.Facebook connection;
    
    protected boolean isInitialized;

    public Instagram () {
    	this(false);
    }
    
    public Instagram(boolean enabled) {
    	this.isEnabled = enabled;
    	this.languageState = LanguageState.Discussion;
    }
    
    // If changing properties does not work, clear the cache
    public void disable() {
    	isEnabled = false;
    }
    
    public String getProfileName() {
    	initProperties();
    	return this.profileName;
    }

    public String getUserName() {
    	initProperties();
        return userName;
    }

    public void setUserName(String userName) {
    	initProperties();
        this.userName = userName;
    }
    
    public String getIGUserName() {
    	initProperties();
    	return IGUserName;
    }
    
    public void setIGUserName(String IGUserName) {
    	initProperties();
    	this.IGUserName = IGUserName;
    }
    
    public String getIGName() {
    	initProperties();
    	return IGName;
    }
    
    public void setIGName(String IGName) {
    	initProperties();
    	this.IGName = IGName;
    }

    public String getID() {
    	initProperties();
        return id;
    }
    
    public Date getTokenExpiry() {
    	initProperties();
    	return this.tokenExpiry;
    }
    
    public String getPageAccessToken() {
    	initProperties();
    	return pageAccessToken;
    }
    
    public void setPageAccessToken(String pageAccessToken) {
    	initProperties();
    	this.pageAccessToken = pageAccessToken;
    }

    public void setID(String id) {
    	initProperties();
        this.id = id;
    }
    
    public String getToken() {
    	initProperties();
        return token;
    }

    public void setToken (String token) {
    	initProperties();
        this.token = token;
    }
    
    public String getAppOauthKey() {
    	initProperties();
        return appOauthKey;
    }

    public void setAppOauthKey(String appOauthKey) {
    	initProperties();
        this.appOauthKey = appOauthKey;
    }

    public String getAppOauthSecret() {
    	initProperties();
        return appOauthSecret;
    }

    public void setAppOauthSecret(String appOauthSecret) {
    	initProperties();
        this.appOauthSecret = appOauthSecret;
    }

    public String getApiBaseURL () {
    	initProperties();
    	return apiBaseURL;
    }
    
    public String getGraphBaseURL () {
    	initProperties();
    	return graphBaseURL;
    }
    
    public int getMaxPost() {
    	initProperties();
		return maxPost;
	}

	public void setMaxPost(int maxPost) {
    	initProperties();
		this.maxPost = maxPost;
	}
    
	public int getMaxError() {
    	initProperties();
		return maxError;
	}

	public void setMaxError(int maxError) {
    	initProperties();
		this.maxError = maxError;
	}

	public int getMaxComment() {
    	initProperties();
		return maxComment;
	}

	public void setMaxComment(int maxComment) {
    	initProperties();
		this.maxComment = maxComment;
	}
	
	public boolean getcommentReplyEnabled () {
    	initProperties();
		return commentReplyEnabled;
	}
	
	public void setcommentReplyEnabled (boolean commentReplyEnabled) {
    	initProperties();
		this.commentReplyEnabled = commentReplyEnabled;
	}
	
	public boolean getReplyAllComments() {
		initProperties();
		return replyAllComments;
	}
	
	public void setReplyAllComments(boolean replyAllComments) {
		initProperties();
		this.replyAllComments = replyAllComments;
	}
	
	public boolean getAutoPostEnabled () {
    	initProperties();
		return autoPostEnabled;
	}
	
	public int getAutoPostHours() {
		initProperties();
		return autoPostHours;
	}
	
	public void setAutoPostHours(int autoPostHours) {
		initProperties();
		this.autoPostHours = autoPostHours;
	}
	
	public boolean getMessageEnabled() {
		initProperties();
		return messageEnabled;
	}
	
	public void setAutoPostEnabled (boolean autoPostEnabled) {
    	initProperties();
		this.autoPostEnabled = autoPostEnabled;
	}
	
	public void setMessageEnabled(boolean messageEnabled) {
		initProperties();
		this.messageEnabled = messageEnabled;
	}
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}
	
	public facebook4j.Facebook getConnection() {
		return this.connection;
	}

    public void setConnection (facebook4j.Facebook connection) {
    	initProperties();
        this.connection = connection;
    }
    
    public List<String> getCommentKeywords() {
		initProperties();
		return commentKeywords;
	}

	public void setCommentKeywords(List<String> statusKeywords) {
		initProperties();
		this.commentKeywords = statusKeywords;
	}
	
	public List<String> getAutoPosts() {
		initProperties();
		return autoPosts;
		//return network.createVertex(getPrimitive()).orderedRelations(Primitive.IGAUTOPOSTS);
	}
	
	public void setAutoPosts(List<String> autoPosts) {
		initProperties();
		this.autoPosts = autoPosts;
	}

    /**
     * Connect to facebook
     */
    public void connect() throws FacebookException {
    	initProperties();
    	ConfigurationBuilder config = new ConfigurationBuilder();
		String key = "";
		String secret = "";
		
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
    	//connect();
    	facebook4j.Facebook instagram = getConnection();
		User user = instagram.getMe();
		if (this.userName == null || !this.userName.equals(user.getId())) {
			this.userName = user.getId();
		}
    	try {
	    	RawAPIResponse res = this.connection.callGetAPI("/me/accounts?fields=instagram_business_account{id,name,username}");
	    	JSONObject result = res.asJSONObject();
	    	setResult(result.toString());
	    	String id = result.getJSONArray("data").getJSONObject(0).getJSONObject("instagram_business_account").getString("id");
	    	String userName = result.getJSONArray("data").getJSONObject(0).getJSONObject("instagram_business_account").getString("username");
	    	setID(id);
	    	setUserName(userName);
	    	System.out.println(res);
	    	System.out.println("Connect id"+id);
    	} catch (Exception e) {
    		log(e);
    	}
    	saveProperties();
    }
    
    
    public String authorizeAccount(String callbackURL) throws FacebookException {
		this.connection = new FacebookFactory().getInstance();
		String key = "";
		String secret = "";
		if (this.appOauthKey != null && !this.appOauthKey.isEmpty()) {
			key = this.appOauthKey;
		}
		if (this.appOauthSecret != null && !this.appOauthSecret.isEmpty()) {
			secret = this.appOauthSecret;
		}
		this.connection.setOAuthAppId(key, secret);
		this.connection.setOAuthPermissions("pages_manage_cta,pages_manage_instant_articles,pages_show_list,ads_management,"
				+ "business_management,pages_messaging,instagram_basic,instagram_manage_comments,instagram_manage_insights,"
				+ "instagram_content_publish,pages_read_engagement,pages_manage_metadata,pages_read_user_content,pages_manage_ads,"
				+ "pages_manage_posts,pages_manage_engagement,public_profile");
		
		return this.connection.getOAuthAuthorizationURL(callbackURL);
	}
    
    /**
	 * Authorize a new account to be accessible by the bot.
	 */
	public void authorizeComplete(String pin) {
		try {
			AccessToken accessToken = this.connection.getOAuthAccessToken(pin);
			setToken(accessToken.getToken());
			
			RawAPIResponse res = this.connection.callGetAPI("/me/accounts?fields=instagram_business_account{id,name,username}");
	    	JSONObject result = res.asJSONObject();
	    	setResult(result.toString());
	    	
	    	String tempId = result.getJSONArray("data").getJSONObject(0).getJSONObject("instagram_business_account").getString("id");
	    	String tempUserName = result.getJSONArray("data").getJSONObject(0).getJSONObject("instagram_business_account").getString("username");
	    	setID(tempId);
	    	setUserName(tempUserName);
	    	
	    	System.out.println(res);
	    	System.out.println("Connect id"+id);
	    	
	    	User user = this.connection.getMe();
	    	System.out.println("User name is " + user.getName());
			this.userName = user.getId();
			if (accessToken.getExpires() != null) {
				this.tokenExpiry = new Date(System.currentTimeMillis() + (accessToken.getExpires() * 1000));
			}
			
			this.profileName = user.getName();
			this.isEnabled = true;
			
			saveProperties();
			
			
		} catch (Exception e){
			log(e);
		}

	}
	
	/**
	 * Check profile for comments
	 */
	public void checkProfile() {
		log("Checking Instagram profile.", Level.INFO);
		try {
			if (getcommentReplyEnabled()) { answerNewComments(); }
			if (getAutoPostEnabled()) { checkAutoPost(); }
			//checkRSS();
		} catch (Exception exception) {
			log(exception);
		}
		log("Done checking Instagram profile.", Level.INFO);
	}
	
	
	public void testComment(String message) throws FacebookException {
		if (this.connection == null) {
			connectAccount();
		}
        try {
            RawAPIResponse res = getConnection().callGetAPI("/" + this.id + "/media");
            JSONObject result = res.asJSONObject();
            setResult(result.toString());
            JSONArray media = result.getJSONArray("data");
            if (media.length() > 0) {
            	postComment(media.getJSONObject(0).getString("id"), message);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
	
	public void testReply() throws FacebookException {
		if (this.connection == null) {
			connectAccount();
		}
        try {
            RawAPIResponse res = getConnection().callGetAPI("/" + this.id + "/media?fields=id,comments,timestamp");
            JSONObject result = res.asJSONObject();
            setResult(result.toString());
            JSONArray media = result.getJSONArray("data");
            if (media.length() > 0) {
            	JSONObject comments = media.getJSONObject(0);
            	setResult(comments.toString());
            	if (comments.has("comments")) {
            		JSONObject comment = comments.getJSONObject("comments").getJSONArray("data").getJSONObject(0);
            		setResult(comment.toString());
            		postReply(comment.getString("id"), "Replying to: " + comment.getString("text"));
            	}
            }
        } catch (Exception e) {
            return;
        }
    }
	
	public void postComment (String mediaID, String message) {
		try {
			HashMap<String,String> params = new HashMap<>();
        	params.put("message", message);
        	params.put("comment_enabled", "true");
        	RawAPIResponse post = getConnection().callPostAPI("/" + mediaID + "/comments", params);
        	setResult(post.asJSONObject().toString());
		} catch (Exception e) {
			return;
		}
	}
	
	public void postReply (String text, String commentID) {
        try {
//            POST /{ig-comment-id}/replies?message={message}
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("message", text);
            params.put("comment_enabled", "true");
            RawAPIResponse res = getConnection().callPostAPI("/" + commentID + "/replies", params);
            setResult(res.asJSONObject().toString());
        } catch (Exception e) {
        	log(e);
        }
    }
	
	public List<String> getUserMedia(String userID) throws FacebookException{
		if (this.connection == null) {
    		connectAccount();
    	}
        try {
        	System.out.println("The userid is: " + userID);
            String nextURL = null;
            List<String> mediaIDs = new ArrayList<>();
            HashMap<String, String> params = new HashMap<>();
            RawAPIResponse res;
            JSONObject apiResult;
            do {
                if (nextURL != null) {
                    params.clear();
                    params.put("next", nextURL);
                    res = getConnection().callGetAPI("/" + userID + "/media", params);
                } else {
                    res = getConnection().callGetAPI("/" + userID + "/media");
                }
                apiResult = res.asJSONObject();
                //System.out.println("API RESULT:" + apiResult);
                JSONArray media = apiResult.getJSONArray("data");
                for (int index = 0; index < Math.min(media.length(), getMaxPost()); index++) {
                    JSONObject post = media.getJSONObject(index);
                    mediaIDs.add((String)post.get("id"));
                }
                // Check if there are more post if limit has not been exceeded
                if (media.length() < getMaxPost() && apiResult.getJSONObject("paging").has("next")) {
                    nextURL = (String)apiResult.getJSONObject("paging").get("next");
                } else {
                    nextURL = null;
                }
            } while (nextURL != null);

            setResult(mediaIDs.toString());
            return mediaIDs;
        } catch (Exception e) {
            log(e);
            return new ArrayList<>();
        }
    }
	
	public String postImageMedia (String userID, String imageURL, String caption) {
        String containerID = createImageContainer(userID, imageURL, caption);
        String status = "IN_PROGRESS";
        int counter = 0;
        while (!status.equals("FINISHED") && counter < 10) {
            status = getContainerStatus(containerID);
            if (!status.equals("FINISHED")) {
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
//                    log("Could not sleep: " + e.getMessage());
                }
            }
        }

        if (status.equals("FINISHED")) {
            String newMediaID = publishContainer(userID, containerID);
            return newMediaID;
        }
        return null;
    }

    public String createImageContainer (String userID, String imageURL, String caption) {
        try {
//            https://graph.facebook.com/v5.0/{ig-user-id}/media?image_url={image-url}&caption={caption}&access_token={access-token}
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("image_url", imageURL);
            params.put("caption", caption);
            params.put("access_token", this.token);
            RawAPIResponse res = getConnection().callPostAPI("/" + userID + "/media", params);
            JSONObject apiResult = res.asJSONObject();
            //log(result.toString());

            String containerID = (String)apiResult.get("id");
            return containerID;
        } catch (Exception e) {
            return null;
        }
    }

    public String postVideoMedia (String userID, String videoURL, String caption) {
        try {
            String containerID = createVideoContainer(userID, videoURL, caption);
            String status = "IN_PROGRESS";
            while (!status.equals("FINISHED")) {
                status = getContainerStatus(containerID);
                if (!status.equals("FINISHED")) {
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {
                    	//
                    }
                }
            }

            if (status.equals("FINISHED")) {
                String newMediaID = publishContainer(userID, containerID);
                return newMediaID;
            }
        } catch (Exception e) {
        	return null;
        }
        return null;
    }

    public String createVideoContainer (String userID, String videoURL, String caption) {
        try {
//          https://graph.facebook.com/v5.0/{ig-user-id}/media?video_url={video-url}&media_type={media-type}&caption={caption}&access_token={access-token}
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("video_url", videoURL);
            params.put("media_type", "VIDEO");
            params.put("caption", caption);
            params.put("access_token", this.token);
            RawAPIResponse res = getConnection().callPostAPI("/" + userID + "/media", params);
            JSONObject apiResult = res.asJSONObject();

            String containerID = (String)apiResult.get("id");
            return containerID;
        } catch (Exception e) {
            return null;
        }
    }

    public String getContainerStatus (String containerID) {
        try {
//                https://graph.facebook.com/v5.0/{ig-container-id}?fields=status_code
            HashMap<String,String> params = new HashMap<>();
            params.put("fields", "status_code");
            params.put("access_token", this.token);
            RawAPIResponse res = getConnection().callGetAPI("/" + containerID, params);
            JSONObject apiResult = res.asJSONObject();
            String status = (String) apiResult.get("status_code");
            return status;
        } catch (Exception e) {
            return null;
        }
    }

    public String publishContainer (String userID, String containerID) {
        try {
//          https://graph.facebook.com/v5.0/{ig-user-id}/media_publish?creation_id={creation-id}&access_token={access-token}

            HashMap<String, String> params = new HashMap<>();
            params = new HashMap<String, String>();
            params.put("creation_id", containerID);
            params.put("access_token", this.token);
            RawAPIResponse res = getConnection().callPostAPI("/" + userID + "/media_publish", params);
            JSONObject apiResult = res.asJSONObject();

            String mediaId = (String)apiResult.get("id");
            return mediaId;
        } catch (Exception e) {
            return null;
        }
    }
    

	// Returns 0, 1, or 2 strings depending on the input
	public List<String> parseAutoPost(String content) {
		int urlStart = -1, urlEnd = -1, strlen = content.length();
		int captionStart = -1;
		
		for (int i = 0; i < strlen; i++) {
			if(content.charAt(i) == ' ') {
				if (urlStart != -1 && urlEnd == -1) {
					urlEnd = i;
				}
			} else {
				if (urlStart == -1) {
					urlStart = i;
				} else if (urlStart != -1 && urlEnd != -1 && captionStart == -1) {
					captionStart = i;
					break;
				}
			}
		}
		
		ArrayList<String> output = new ArrayList<>();
		if (urlStart != -1) {
			if (urlEnd != -1 && captionStart != -1) {
				output.add(content.substring(urlStart, urlEnd));
				output.add(content.substring(captionStart, strlen));
			} else if (urlEnd != -1) {
				output.add(content.substring(urlStart, urlEnd));

			} else {
				output.add(content.substring(urlStart, strlen));
			}
		} 
		
		return output;
		
	}
    
    public void checkAutoPost() {
    	if (!getAutoPostEnabled()) {
			return;
		}
    	log("Autoposting", Level.FINE);
		try {
			Network memory = getBot().memory().newMemory();
			Vertex instagram = memory.createVertex(getPrimitive());
			Vertex vertex = instagram.getRelationship(Primitive.IGLASTPOST);
			long last = 0;
			if (vertex != null) {
				last = ((Timestamp)vertex.getData()).getTime();
			}
			long millis = getAutoPostHours() * 60 * 60 * 1000;
			if ((System.currentTimeMillis() - last) < millis) {
				log("Autoposting hours not reached", Level.FINE, getAutoPostHours());
				return;
			}
			List<String> autoposts = getAutoPosts();
			/*
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
					instagram.setRelationship(Primitive.IGLASTPOST, memory.createTimestamp());
					memory.save();
				}
			}*/
		} catch (Exception exception) {
			log(exception);
		}
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
    
    // Process A comment
    public long[] processComment(JSONObject comment, JSONObject parent, Network memory, int count, long max, long last) throws JSONException, ParseException {
    	SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    	System.out.println("The comment is" + comment);
    	
    	String text = comment.getString("text");
		String commentId = comment.getString("id");
		String authorId = comment.getString("username");
		Date date = parser.parse(comment.getString("timestamp"));
		long timestamp = date.getTime();
		
		input(comment, parent, memory);
		
		log("Processing post comment", Level.FINE, commentId, text);
		
		
		if (true || count >= this.maxComment) {
			log("Max Comments reached", Level.FINE, count);
			return null;
		}

		if (timestamp > max) {
			max = timestamp;
		}
		if (!authorId.equals(this.userName)) {
			
			if (true || (System.currentTimeMillis() - timestamp) > DAY) {
				log("Day old post comment", Level.FINE, commentId, timestamp);
				return null;
			}
			if (true || timestamp > last) {
				if (getReplyAllComments() || containsKeywords(text)) {
					count++;
					log("Processing post comment", Level.FINE, text, authorId, userName);
					input(comment, parent, memory);
					Utils.sleep(500);
				} else {
					log("Skipping post comment, missing keywords.", Level.FINE, text);
				}
			} else {
				log("Old post comment", Level.INFO, commentId, timestamp);							
			}
		} else {
			log("Ignoring own comment", Level.INFO, commentId);							
		}
		long[] values = new long[2];
		values[0] = count;
		values[1] = max;
		return values;
	}
    
    public boolean containsKeywords(String commentText) {
    	boolean match = false;
    	List<String> commentWords = new TextStream(commentText.toLowerCase()).allWords();
    	for (String text : getCommentKeywords()) {
			List<String> keywords = new TextStream(text.toLowerCase()).allWords();
			if (!keywords.isEmpty() && commentWords.containsAll(keywords)) {
				match = true;
				break;
			}
		}
    	return match;
    }
    
    public ArrayList<JSONObject> getCommentReplies(JSONObject comment){
    	ArrayList<JSONObject> data = new ArrayList<JSONObject>();
    	
    	try {
    		Queue<JSONObject> q = new LinkedList<>();
        	q.add(comment);
        	while (!q.isEmpty()) {
        		JSONObject top = q.poll();
        		String commentId = comment.getString("id");
        		
        		JSONObject res;
        		res = getConnection().callGetAPI("/" + commentId + "/replies?fields=from,id,text,replies").asJSONObject();
        		JSONArray replies = res.getJSONArray("data");
        		for (int i = 0; i < replies.length(); i++) {
        			JSONObject reply = replies.getJSONObject(i);
        			data.add(reply);
        			q.add(reply);
        		}
        	}
        	return data;
    		
    	} catch(Exception e) {
    		log(e);
    		return null;
    	}
    }

    // Reply to new comments
    public void answerNewComments() {
    	log("Checking for new comments", Level.FINE);
    	
        try {
        	if (this.connection == null) {
        		connectAccount();
        	}
        	
        	Network memory = getBot().memory().newMemory();
			Vertex instagram = memory.createVertex(getPrimitive());
			Vertex vertex = instagram.getRelationship(Primitive.IGLASTCOMMENT);
			
			long last = 0;
			if (vertex != null) {
				last = ((Number)vertex.getData()).longValue();
			}
            
			String next = null;
            long max = 0;
            int commentsProcessed = 0;
            boolean complete = false;
            HashMap<String,String> params = new HashMap<>();
            params.put("fields", "id,timestamp,comments{id,username,text,timestamp,replies{id,username,text,timestamp}}");

            List<String> posts = getUserMedia(getID());
            for(int i = 0; i < posts.size() && commentsProcessed < getMaxComment(); i++) {
            	if (next != null) {
                    params.put("after", next);
                }
            	do {	
            		JSONObject apiResult = getConnection().callGetAPI("/" + posts.get(i), params).asJSONObject();
            		System.out.println(apiResult);
            		JSONArray comments = apiResult.getJSONObject("comments").getJSONArray("data");
            		
            		if (comments != null && comments.length() != 0) {
            			for (int j = 0; j < comments.length(); j++) {
            				JSONObject comment = comments.getJSONObject(j);
            				System.out.println("Comment at " + j + comment);
            				long[] values = processComment(comment, null, memory, commentsProcessed, max, last);
							
            				/*
            				if (values == null) {
								break;
							}
							commentsProcessed = (int)values[0];
							max = values[1];
							if (commentsProcessed == -1) {
								break;
							}*/
							
							ArrayList<JSONObject> replies = getCommentReplies(comment);
							if ((replies != null) && !replies.isEmpty()) {
								for (int index2 = replies.size() - 1; index2 >= 0; index2--) {
									JSONObject reply = replies.get(index2);
									values = processComment(reply, comment, memory, commentsProcessed, max, last);
									if (values == null) {
										break;
									}
									commentsProcessed = (int)values[0];
									max = values[1];
								}
							}
            			}
            		} else {
						log("No comments", Level.FINE, posts.get(i));
					}

            		
            		if (!complete && apiResult.has("paging") && apiResult.getJSONObject("paging").has("next")) {
                        next = apiResult.getJSONObject("paging").getJSONObject("cursors").getString("after");
                    } else {
                        next = null;
                    }
            	} while(next != null && commentsProcessed < getMaxComment());
            	
     	
            }
            if (commentsProcessed >= getMaxComment()) { 
            	log("Max Comments Reached", Level.FINE);
            }
            
            if (max != 0) {
            	instagram.setRelationship(Primitive.IGLASTCOMMENT, memory.createVertex(max));
            	memory.save();
            }
    		
        } catch (Exception e) {
        	log(e);
            setResult(e.getMessage());
        }
    }
    
    public void testGetBotLibreResponse () {
//      {user=id}/media?fields=id,caption,comments{id,user,username,text,timestamp}
    	try {
    		HashMap<String, String> params = new HashMap<>();
    		params.put("fields", "id,caption,comments{id,user,username,text,timestamp}");
    		RawAPIResponse res = connection.callGetAPI(this.id + "/media", params);
    		JSONArray result = res.asJSONObject().getJSONArray("data");
    		setResult(res.asJSONObject().toString());
    		if (result.length() > 0) {
    			JSONObject media = result.getJSONObject(0);
    			if (media.has("comments")) {
    				JSONObject comment = media.getJSONObject("comments").getJSONArray("data").getJSONObject(0);
    				String conversationID = comment.getString("id");
    				String from = comment.getString("username");
    				String text = comment.getString("text");
    				String target = this.userName;
    				setResult("From: " + from +
    						"\nTarget: " + target +
    						"\nText: " + text +
    						"\nConversation ID: " + conversationID);
    				String message = processMessage(from, from, target, text, conversationID);
    				if (message != null) {
    					setResult("Message: " + message);
    				}
    			}
    		}
    	} catch (Exception e) {
//    		setResult(e.getMessage());
    	}
    }
    
    public String processMessage(String fromID, String from, String target, String message, String id) {
        this.responseListener = new ResponseListener();
        Network memory = bot.memory().newMemory();
        inputSentence(message, fromID, from, target, id, memory);
        memory.save();
        String reply = null;
        synchronized (this.responseListener) {
            if (this.responseListener.reply == null) {
                try {
                    this.responseListener.wait(MAX_WAIT);
                } catch (Exception exception) {
                    log(exception);
                    return "Error";
                }
            }
            reply = this.responseListener.reply;
            this.responseListener = null;
        }

        return reply;
    }
    
    /**
	 * Process the post comment.
	 */
	public void input(JSONObject comment, JSONObject parent, Network network) {
		System.out.println("Input called");
		/*
		if (!isEnabled()) {
			return;
		}*/
		try {
			System.out.println("Before here");
			String text = comment.getString("text").trim();
			String commentId = comment.getString("id");
			String authorId = comment.getString("username");
			
			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
			Date date = parser.parse(comment.getString("timestamp"));
			long timestamp = date.getTime();
			
			
			log("Processing post comment", Bot.FINE, text, commentId);
			/*
			if ((System.currentTimeMillis() - timestamp) > DAY) {
				log("Day old post commentt", Bot.FINE, commentId, timestamp);
				return;
			}*/
			
			log("Input post comment", Level.FINE, text, name);
			this.postsProcessed++;
			String id = commentId;
			if (parent != null) {
				id = parent.getString("id");
			}
			
			System.out.println("here");
			inputSentence(text, commentId, authorId, this.userName, commentId, timestamp, network);
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userId, String userName, String targetUserName, String messageId, long time, Network network) {
		System.out.println("InputSentence called "+ messageId);
		
		Vertex input = createInput(text.trim(), network);
		Vertex sentence = input.getRelationship(Primitive.INPUT);
		
		Vertex idVertex = network.createVertex(messageId);
		
		/*
		if (sentence.hasRelationship(Primitive.IGCOMMENT, id)) {
			log("Post already processed", Bot.FINE, id, time);
			return;
		}*/
		sentence.addRelationship(Primitive.IGCOMMENT, idVertex);
		input.addRelationship(Primitive.INSTANTIATION, Primitive.IGCOMMENT);
		input.addRelationship(Primitive.CREATEDAT, network.createVertex(time));
		input.addRelationship(Primitive.ID, idVertex);
		Vertex conversation = network.createInstance(Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.IGCOMMENT);
		Language.addToConversation(input, conversation);
		Vertex user = network.createUniqueSpeaker(new Primitive(userId), Primitive.INSTAGRAM, userName);
		conversation.addRelationship(Primitive.SPEAKER, user);
		input.addRelationship(Primitive.SPEAKER, user);
		if (targetUserName != null) {
			Vertex targetUser = null;
			if (targetUserName.equals(getUserName())) {
				targetUser = network.createVertex(Primitive.SELF);
			} else {
				targetUser = network.createUniqueSpeaker(new Primitive(targetUserName), Primitive.INSTAGRAM);
			}
			input.addRelationship(Primitive.TARGET, targetUser);
			conversation.addRelationship(Primitive.SPEAKER, targetUser);
		}
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
    
    public void inputSentence(String text, String userId, String userName, String targetUserName, String id, Network network) {    
    	Vertex input = createInput(text.trim(), network);
        Vertex user = network.createUniqueSpeaker(new Primitive(userId), Primitive.INSTAGRAM, userName);
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
        conversation.addRelationship(Primitive.TYPE, Primitive.INSTAGRAM);
        conversation.addRelationship(Primitive.ID, network.createVertex(id));
        conversation.addRelationship(Primitive.SPEAKER, user);
        conversation.addRelationship(Primitive.SPEAKER, self);
        Language.addToConversation(input, conversation);

        network.save();
        getBot().memory().addActiveMemory(input);
    }

    protected Vertex createInput(String text, Network network) {
        Vertex sentence = network.createSentence(text);
        Vertex input = network.createInstance(Primitive.INPUT);
        input.setName(text);
        input.addRelationship(Primitive.SENSE, getPrimitive());
        input.addRelationship(Primitive.INPUT, sentence);
        sentence.addRelationship(Primitive.INSTANTIATION, Primitive.INSTAGRAM);
        return input;
    }
    
    @Override
    public void output(Vertex output) {
    	System.out.println("Output called");
    	
    	Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to twitter, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			notifyResponseListener();
			return;
		}
		String text = printInput(output);
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		Vertex id = conversation.getRelationship(Primitive.ID);
		String conversationId = id.printString();
		
		Vertex target = output.mostConscious(Primitive.TARGET);
		String replyTo = conversationId;
		if (target != null && target.hasRelationship(Primitive.WORD)) {
			replyTo = target.mostConscious(Primitive.WORD).printString();
		}
		
		Vertex command = output.mostConscious(Primitive.COMMAND);

		if (this.responseListener != null) {
			this.responseListener.reply = text;
			notifyResponseListener();
		}
		
		// If the response is empty, do not send it.
		if (command == null && text.isEmpty()) {
			return;
		}
		
		System.out.println("Output called," + replyTo + "  " + text);
		if (conversation.hasRelationship(Primitive.TYPE, Primitive.IGCOMMENT)) {
			postReply(text, replyTo);
		} else if (conversation.hasRelationship(Primitive.TYPE, Primitive.INSTAGRAM)) {
			sendIGMessage(text, replyTo);
		} else {
			System.out.println("Invalid Reply Type");
		}

    }
    
    public String createJSONQuickReply(String command, String id, String text) {
		try {
			JSONObject root = (JSONObject)JSONSerializer.toJSON(command);
			if (!root.has("type") || !root.getString("type").equals("instagram")) {
				return "";
			}
			Object json = root.get("quick_replies");
			if (json == null) {
				return "";
			}
			return json.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	} 
    
    public void sendIGMessage(String text, String replyUser){
    	log("Sending Instagram message:", Level.INFO, text, replyUser);

    	String url = "https://graph.facebook.com/v12.0/me/messages?access_token="
    		+getPageAccessToken();
    	
    	System.out.println(url);
    	System.out.println(getPageAccessToken());

    	try {
			if (getMessageEnabled()) {
				try {
					// Check for HTML content and strip button elements.
					String strippedText = format(text);
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
								json = "{recipient:{id:\"" + replyUser + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(text) + "\"}}";
								log("POST", Level.INFO, url, replyUser , json);
								Utils.httpPOST(url, "application/json", json);
								Utils.sleep(500);
							}
						}
					} else {
						postText = strippedText;
					}
					
					Element root = null;
					boolean linkFound = false;
					
					if ((text.indexOf('<') != -1) && (text.indexOf('>') != -1)) {
						try {
							root = getBot().awareness().getSense(Http.class).parseHTML(text);

							
							/* Can be used later for generic templates
							 * 
							NodeList imageNodes = root.getElementsByTagName("img");
							String image = null;
							if (imageNodes.getLength() > 0) {
								String src = ((Element)imageNodes.item(0)).getAttribute("src");
								if (src != null && !src.isEmpty()) {
									image = src;
									String title = ((Element)imageNodes.item(0)).getAttribute("title");
									if (title != null && !title.isEmpty()) {
										imageTitle = title;
									}
								}
							}*/
							
							// Check for <button> link tags.
							NodeList nodes = root.getElementsByTagName("button");
							if (nodes.getLength() > 0) {
								// Check for <button> tags.
								boolean quickReply = nodes.getLength() > 1 && nodes.getLength() <= 11;
								if (quickReply) {
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
											buttonJSON = buttonJSON + "{ \"content_type\": \"text\", \"payload\": \"" + button + "\", title: \"" + button + "\"}";
											linkFound = true;
										}
									}
									if (linkFound) {
										String json = "{\"recipient\":{\"id\":\""
													+ replyUser + "\"}, \"messaging_type\": \"RESPONSE\", \"message\":{ \"text\": \"" + Utils.escapeQuotesJS(postText2) + "\", \"quick_replies\": [ " + buttonJSON + " ]}}";
										log("POST", Level.INFO, url, replyUser, json);
										Utils.httpPOST(url, "application/json", json);
										Utils.sleep(500);
										postText = null;
									}
								} 
							}
							/* Test Temlate Later
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
									log("POST", Level.INFO, url, getFacebookMessengerAccessToken(), json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
									buttonJSON = "";
									count = 0;
								}
							} */
						} catch (Exception exception) {
							log(exception);
						}
					}
					
					if (postText != null) {
						String json = "{recipient:{id:\"" + replyUser + "\"}, message:{ text:\"" + Utils.escapeQuotesJS(postText) + "\"}}";
						log("POST", Level.INFO, url, replyUser, json);
						Utils.httpPOST(url, "application/json", json);
						Utils.sleep(500);
					}
					/* Use Later for Generic Templates
					if (!linkFound && (text.indexOf('<') != -1) && (text.indexOf('>') != -1)) {
						try {
							NodeList nodes = root.getElementsByTagName("img");
							for (int index = 0; index  < nodes.getLength(); index++) {
								Element node = (Element)nodes.item(index);
								String src = node.getAttribute("src");
								if (src != null && !src.isEmpty()) {
									String json = "{recipient:{id:\"" + id + "\"}, message:{ attachment:{ type: \"image\", payload: { url: \"" + src + "\"}}}}";
									log("POST", Level.INFO, url, getFacebookMessengerAccessToken(), json);
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
									log("POST", Level.INFO, url, getFacebookMessengerAccessToken(), json);
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
									log("POST", Level.INFO, url, getFacebookMessengerAccessToken(), json);
									Utils.httpPOST(url, "application/json", json);
									Utils.sleep(500);
								}
							}
						} catch (Exception exception) {
							log(exception);
						}
					}
					*/
				} catch (Exception exception) {
					this.errors++;
					log(exception);
				}
			} else {
				log("Instagram Messaging must be enabled.", Level.WARNING);
			}
		} catch (Exception exception) {
			log(exception);
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
    
    public String inputInstagramMessage(String text, String targetUserName, String senderId, net.sf.json.JSONObject message, Network network) {
		log("Input Instagram Message Called", Level.FINE);
    	Vertex user = network.createUniqueSpeaker(new Primitive(senderId), Primitive.INSTAGRAM);
		if (!user.hasRelationship(Primitive.NAME)) {
			String url = "https://graph.facebook.com/v12.0/me?fields=id,first_name,last_name&access_token="+ getToken();
			String senderName = null;
			try {
				if (getConnection() == null) { connect(); }
				String json = Utils.httpGET(url);
				net.sf.json.JSONObject userJSON = (net.sf.json.JSONObject)JSONSerializer.toJSON(json);
				if (userJSON != null) {
					Object firstName = userJSON.get("first_name");
					Object lastName = userJSON.get("last_name");
					if (firstName instanceof String) {
						senderName = (String)firstName;
					}
					if (lastName instanceof String) {
						if (senderName == null) {
							senderName = "";
						}
						senderName = senderName + " " + (String)lastName;
					}
				}
			} catch (Exception exception) {
				log(url, Level.INFO);
				url = "https://graph.facebook.com/v2.6/" + senderId + "?fields=first_name,last_name&access_token=" + getToken();
				log(url, Level.INFO);
				log(exception);
			}
			if (senderName == null || senderName.isEmpty()) {
				senderName = senderId;
			}
			user = network.createUniqueSpeaker(new Primitive(senderId), Primitive.INSTAGRAM, senderName);
		}
		
		Vertex input = createInput(text.trim(), network);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);		
		input.addRelationship(Primitive.TARGET, self);
		
		/*
		if (getTrackMessageObjects() && message != null) {
			input.addRelationship(Primitive.MESSAGE, getBot().awareness().getSense(Http.class).convertElement(message, network));
		}*/

		Vertex conversationId = network.createVertex(senderId);
		Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(self);
		Vertex conversation = today.getRelationship(conversationId);
		if (conversation == null) {
			conversation = network.createVertex();
			today.setRelationship(conversationId, conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.INSTAGRAM);
		conversation.addRelationship(Primitive.ID, conversationId);
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
		this.responseListener = new ResponseListener();
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
	 * Load settings.
	 */
	public void initProperties() {
		if (this.isInitialized) { return; }
		
		synchronized (this) {
			if (this.isInitialized) { return; }
			
			getBot().memory().loadProperties("Instagram");
			Network memory = getBot().memory().newMemory();
			Vertex instagram = memory.createVertex(getPrimitive());

			String property = this.bot.memory().getProperty("Instagram.userName");
			if (property != null) {
				this.userName = property;
			}
			property = this.bot.memory().getProperty("Instagram.id");
			if (property != null) {
				this.id = property;
			}
			property = this.bot.memory().getProperty("Instagram.result");
			if (property != null) {
				this.result = property;
			}
			
			property = this.bot.memory().getProperty("Instagram.token");
			if (property != null) {
				this.token = property;
			}
			
			property = this.bot.memory().getProperty("Instagram.appOauthKey");
			if (property != null && !property.isEmpty()) {
				this.appOauthKey = Utils.decrypt(Utils.KEY, property);
			}

			property = this.bot.memory().getProperty("Instagram.appOauthSecret");
			if (property != null && !property.isEmpty()) {
				this.appOauthSecret = Utils.decrypt(Utils.KEY, property);
			}
	
			property = this.bot.memory().getProperty("Instagram.pageAccessToken");
			if (property != null && !property.isEmpty()) {
				this.pageAccessToken = Utils.decrypt(Utils.KEY, property);
			}
			
			property = this.bot.memory().getProperty("Instagram.commentReplyEnabled");
			if (property != null) {
				this.commentReplyEnabled = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.autoPostEnabled");
			if (property != null) {
				this.autoPostEnabled = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.IGUserName");
			if (property != null && !property.isEmpty()) {
				this.IGUserName = Utils.decrypt(Utils.KEY, property);
			}
			
			property = this.bot.memory().getProperty("Instagram.messageEnabled");
			if (property != null) {
				this.messageEnabled = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.maxPost");
			if (property != null) {
				this.maxPost = Integer.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.replyAllComments");
			if (property != null) {
				this.replyAllComments = Boolean.valueOf(property);
			}
			
			this.commentKeywords = new ArrayList<String>();
			List<Relationship> keywords = instagram.orderedRelationships(Primitive.IGCOMMENTKEYWORDS);
			if (keywords != null) {
				for (Relationship relationship : keywords) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.commentKeywords.add(text);
					}
				}
			}
			
			this.autoPosts = new ArrayList<String>();
			List<Vertex> autoposts = instagram.orderedRelations(Primitive.IGAUTOPOSTS);
			if (autoposts != null && !autoposts.isEmpty()) {
				Iterator<Vertex> iterator = autoposts.iterator();
				while (iterator.hasNext()) {
					Vertex post = iterator.next();
					this.autoPosts.add(post.printString());
				}
			}
		
			property = this.bot.memory().getProperty("Instagram.maxComment");
			if (property != null) {
				this.maxComment = Integer.valueOf(property);
			}
			
			this.isInitialized = true;
		}
	}
	
	// Save User Instagram preference properties to the database
	@Override
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		
		memory.saveProperty("Instagram.userName", this.userName, true);
		memory.saveProperty("Instagram.token", this.token, true);
		memory.saveProperty("Instagram.id", this.id, true);
		memory.saveProperty("Instagram.result", this.result, true);
		memory.saveProperty("Instagram.IGUserName", this.IGUserName, true);
		memory.saveProperty("Instagram.IGName", this.IGName, true);
		
		// Save the Oauthkey
		if (this.appOauthKey == null || this.appOauthKey.isEmpty()) {
			memory.saveProperty("Instagram.appOauthKey", "", true);
		} else {
			memory.saveProperty("Instagram.appOauthKey", Utils.encrypt(Utils.KEY, this.appOauthKey), true);
		}
		
		// Save the OauthSecret
		if (this.appOauthSecret == null || this.appOauthSecret.isEmpty()) {
			memory.saveProperty("Instagram.appOauthSecret", "", true);
		} else {
			memory.saveProperty("Instagram.appOauthSecret", Utils.encrypt(Utils.KEY, this.appOauthSecret), true);
		}
		
		// Save the Page Access Token
		if (this.pageAccessToken == null || this.pageAccessToken.isEmpty()) {
			memory.saveProperty("Instagram.pageAccessToken", "", true);
		} else {
			memory.saveProperty("Instagram.pageAccessToken", Utils.encrypt(Utils.KEY, this.pageAccessToken), true);
		}

		memory.saveProperty("Instagram.commentReplyEnabled", String.valueOf(this.commentReplyEnabled), true);
		memory.saveProperty("Instagram.autoPostEnabled", String.valueOf(this.autoPostEnabled), true);
		memory.saveProperty("Instagram.messageEnabled", String.valueOf(this.messageEnabled), true);
		
		memory.saveProperty("Instagram.maxPost", String.valueOf(this.maxPost), true);
		memory.saveProperty("Instagram.maxComment", String.valueOf(this.maxComment), true);
		memory.saveProperty("Instagram.replyAllComments", String.valueOf(this.replyAllComments), true);
		
		Vertex instagram = memory.createVertex(getPrimitive());
		instagram.unpinChildren();
		
		instagram.internalRemoveRelationships(Primitive.IGCOMMENTKEYWORDS);
		for (String text : this.commentKeywords) {
			Vertex keywords =  memory.createVertex(text);
			instagram.addRelationship(Primitive.IGCOMMENTKEYWORDS, keywords);
		}
		
		
		/*
		if (this.autoPosts != null) {
			Collection<Relationship> old = instagram.getRelationships(Primitive.IGAUTOPOSTS);
			if (old != null) {
				for (Relationship post : old) {
					if (post.getTarget().instanceOf(Primitive.FORMULA)) {
						SelfCompiler.getCompiler().unpin(post.getTarget());
					}
				}
			}
			instagram.internalRemoveRelationships(Primitive.IGAUTOPOSTS);
			for (String text : autoPosts) {
				Vertex post =  memory.createSentence(text);
				if (post.instanceOf(Primitive.FORMULA)) {
					SelfCompiler.getCompiler().pin(post);
				}
				post.addRelationship(Primitive.INSTANTIATION, Primitive.POST);
				facebook.addRelationship(Primitive.AUTOPOSTS, post);
			}
		}*/
		
		
		
		memory.createVertex(getPrimitive());
		memory.save();
	}
	
}
