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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
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
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.RawAPIResponse;
import facebook4j.ResponseList;
import facebook4j.User;
import facebook4j.auth.AccessToken;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

import net.sf.json.JSONSerializer;

public class Instagram extends BasicSense{
	public static int MAX_WAIT = 1000 * 5; // 30 seconds
	
	public static int MAX_LOOKUP = 100;
	
	//protected static String oauthKey = "key";
	//protected static String oauthSecret = "secret";
	protected String userName = "";
	protected String id = "";
	
    protected String token = "";
    protected Date tokenExpiry;
    protected String appOauthKey = "";
    protected String appOauthSecret = "";
    protected String pageAccessToken = "";
    
    protected String result = "";
    
    protected final String graphVersion = "v12.0";
    protected final String apiBaseURL = "https://api.facebook.com/";
    protected final String graphBaseURL = "https://graph.facebook.com/";
    
    protected int maxPost = 5;
    protected int maxComment = 30;
    protected int maxLike = 50;
    protected int maxError = 5;
    protected int posts;
    protected int comments;
    protected int likes;
    protected int errors;
    
    protected String page = "";
	protected String pageId = "";
	protected String profileName = "";

    protected List<String> answeredComments = new ArrayList<>();
    
    protected boolean messageEnabled = false;
    protected boolean likeAllComments = false;
    protected boolean replyToComments = false;
    protected boolean autoPost = false;

    protected List<String> imageURLs = new ArrayList<String>();
    protected List<String> captions = new ArrayList<String>();

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
    	messageEnabled = false;
    	likeAllComments = false;
    	replyToComments = false;
    	autoPost = false;
    	saveProperties();
    }
    
    public String getProfileName() {
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

    public String getID() {
    	initProperties();
        return id;
    }
    
    public Date getTokenExpiry() {
    	return this.tokenExpiry;
    }
    
    public String getPageAccessToken() {
    	initProperties();
    	return pageAccessToken;
    }
    
    public void setPageAccessToken(String pageAccessToken) {
    	//initProperties();
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
    	//initProperties();
        this.appOauthKey = appOauthKey;
    }

    public String getAppOauthSecret() {
    	initProperties();
        return appOauthSecret;
    }

    public void setAppOauthSecret(String appOauthSecret) {
    	//initProperties();
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

	public int getMaxLike() {
    	initProperties();
		return maxLike;
	}

	public void setMaxLike(int maxLike) {
    	initProperties();
		this.maxLike = maxLike;
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
    	
	public boolean getLikeAllComments () {
    	initProperties();
		return likeAllComments;
	}
	
	public void setLikeAllComments (boolean likeAllComments) {
    	initProperties();
		this.likeAllComments = likeAllComments;
	}
	
	public boolean getReplyToComments () {
    	initProperties();
		return replyToComments;
	}
	
	public void setReplyToComments (boolean replyToComments) {
    	initProperties();
		this.replyToComments = replyToComments;
	}
	
	public boolean getAutoPost () {
    	initProperties();
		return autoPost;
	}
	
	public boolean getMessageEnabled() {
		initProperties();
		return messageEnabled;
	}
	
	public void setAutoPost (boolean autoPost) {
    	initProperties();
		this.autoPost = autoPost;
	}
	
	public void setMessageEnabled(boolean messageEnabled) {
		this.messageEnabled = messageEnabled;
	}
	
	public List<String> getImageURLs() {
    	initProperties();
		return imageURLs;
	}

	public void setImageURLs(List<String> imageURLs) {
    	initProperties();
		this.imageURLs = imageURLs;
	}
	
	public List<String> getCaptions() {
    	initProperties();
		return captions;
	}

	public void setCaptions(List<String> captions) {
    	initProperties();
		this.captions = captions;
	}
	
	public List<String> getAnswered() {
		return this.answeredComments;
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
    	facebook4j.Facebook facebook = getConnection();
		User user = facebook.getMe();
		if (this.userName == null || !this.userName.equals(user.getId())) {
			this.userName = user.getId();
		}
    	try {
	    	RawAPIResponse res = this.connection.callGetAPI("/me/accounts?fields=instagram_business_account{id,name,username}");
	    	JSONObject result = res.asJSONObject();
	    	setResult(result.toString());
	    	this.id = result.getJSONArray("data").getJSONObject(0).getJSONObject("instagram_business_account").getString("id");
	    	this.userName = result.getJSONArray("data").getJSONObject(0).getJSONObject("instagram_business_account").getString("username");
    	} catch (Exception e) {
    		log(e);
    	}
    	saveAuth();
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
	 * Authorise a new account to be accessible by Bot.
	 */
    

	public void authorizeComplete(String pin) throws FacebookException {
		
		AccessToken token = this.connection.getOAuthAccessToken(pin);
		setToken(token.getToken());

		User user = this.connection.getMe();
		System.out.println("User name is " + user.getName());
		System.out.println("PAT is " + getPageAccessToken());
		this.userName = user.getId();
		if (token.getExpires() != null) {
			this.tokenExpiry = new Date(System.currentTimeMillis() + (token.getExpires() * 1000));
		}
		
		this.profileName = user.getName();
		this.isEnabled = true;
		
		saveAuth();
		
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
	
	public void testDeleteComment() throws FacebookException {
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
            		deleteComment(comment.getString("id"));
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
	
	public void postReply (String commentID, String text) {
        try {
//            POST /{ig-comment-id}/replies?message={message}
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("message", text);
            params.put("comment_enabled", "true");
            RawAPIResponse res = getConnection().callPostAPI("/" + commentID + "/replies", params);
            setResult(res.asJSONObject().toString());
        } catch (Exception e) {
        	//
        }
    }
	
	public boolean deleteComment (String commentID) {
        try {
            RawAPIResponse res = getConnection().callDeleteAPI(commentID);
            JSONObject apiResult = res.asJSONObject();
            setResult(apiResult.toString());
            boolean success = apiResult.getBoolean("success");
            return success;
        } catch (Exception e) {
            return false;
        }
    }
	
	public List<String> getUserMedia(String userID) {
        try {
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
                JSONArray media = apiResult.getJSONArray("data");
                for (int index = 0; index < media.length(); index++) {
                    JSONObject comment = media.getJSONObject(index);
                    mediaIDs.add((String)comment.get("id"));
                }
                if (apiResult.getJSONObject("paging").has("next")) {
                    nextURL = (String)apiResult.getJSONObject("paging").get("next");
                } else {
                    nextURL = null;
                }
            } while (nextURL != null);

            setResult(mediaIDs.toString());
            return mediaIDs;
        } catch (Exception e) {
            //
            return null;
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
//            https://graph.facebook.com/v5.0/{ig-user-id}/media?video_url={video-url}&media_type={media-type}&caption={caption}&access_token={access-token}
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
//            https://graph.facebook.com/v5.0/{ig-user-id}/media_publish?creation_id={creation-id}&access_token={access-token}

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
	
    public List<JSONObject> getCommentDates (String mediaID) {
        try {
//            GET {ig-media-id}/comments?fields=like_count,replies,username,text
            HashMap<String, String> params = new HashMap<>();
            params.put("fields", "text,timestamp,replies{text,timestamp,user},media,user");
            RawAPIResponse res = getConnection().callGetAPI("/" + mediaID + "/comments", params);
            JSONObject apiResult = res.asJSONObject();
            List<JSONObject> commentInfo = new ArrayList<>();
            JSONArray comments = apiResult.getJSONArray("data");
            for (int index = 0; index < comments.length(); index++) {
                JSONObject comment = comments.getJSONObject(index);
                commentInfo.add(comment);
            }
            return commentInfo;
        } catch (Exception e) {
            return null;
        }
    }
    
    public void clearAnswers () {
    	this.answeredComments.clear();
    	Network memory = getBot().memory().newMemory();
        Vertex instagram = memory.createVertex(getPrimitive());
        instagram.internalRemoveRelationships(Primitive.ANSWEREDCOMMENTS);
		for (String text : this.answeredComments) {
			Vertex keywords =  memory.createVertex(text);
			instagram.addRelationship(Primitive.ANSWEREDCOMMENTS, keywords);
		}
		memory.save();
    }
    
    public void answerNewComments() {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
//            Date commentDate = parser.parse(comment.getString("timestamp"));
//            17841446937956570/media?fields=id,timestamp,comments{id,text,timestamp,replies}
            Date date;
            String next = null;
            HashMap<String,String> params = new HashMap<>();
            params.put("fields", "id,timestamp,comments{id,username,text,timestamp,replies{id,username,text,timestamp}}");
            do {
                if (next != null) {
                    params.put("after", next);
                }
                RawAPIResponse res = getConnection().callGetAPI("/" + this.id + "/media", params);
                JSONObject apiResult = res.asJSONObject();

                JSONArray allMedia = apiResult.getJSONArray("data");
                for (int i = 0; i < allMedia.length(); i++) {
                    JSONObject media = allMedia.getJSONObject(i);
                    if (!media.has("comments")) {
                        continue;
                    }
                    JSONArray comments = media.getJSONObject("comments").getJSONArray("data");
                    for (int j = 0; j < comments.length(); j++) {
                        JSONObject comment = comments.getJSONObject(j);
                        date = parser.parse(comment.getString("timestamp"));
                        if (System.currentTimeMillis() - date.getTime() > DAY) {
                            answeredComments.remove(comment.getString("id"));
                        } else {
                        	if (!comment.getString("username").equals(this.userName) && 
                        			!this.answeredComments.contains(comment.getString("id"))) {
                                postReply(comment.getString("id"), "Hi there " + comment.getString("username") +
                                        "!");
                                this.answeredComments.add(comment.getString("id"));
                            }
                        }
                        if (comment.has("replies")) {
                            JSONArray replies = comment.getJSONObject("replies").getJSONArray("data");
                            for (int k = 0; k < replies.length(); k++) {
                                JSONObject reply = replies.getJSONObject(k);
                                date = parser.parse(reply.getString("timestamp"));
                                if (System.currentTimeMillis() - date.getTime() > DAY) {
                                    answeredComments.remove(reply.getString("id"));
                                    continue;
                                }
                                if (!reply.getString("username").equals(this.userName) && 
                                		!this.answeredComments.contains(reply.getString("id"))) {
                                    postReply(comment.getString("id"), "Hi there " + reply.getString("username") +
                                            "!");
                                    this.answeredComments.add(reply.getString("id"));
                                }
                            }
                        }
                    }
                }

                if (apiResult.getJSONObject("paging").has("next")) {
                    next = apiResult.getJSONObject("paging").getJSONObject("cursors").getString("after");
                } else {
                    next = null;
                }
            } while (next != null);
            
            setResult(this.answeredComments.toString());
            
            Network memory = getBot().memory().newMemory();
            Vertex instagram = memory.createVertex(getPrimitive());
            instagram.internalRemoveRelationships(Primitive.ANSWEREDCOMMENTS);
    		for (String text : this.answeredComments) {
    			Vertex keywords =  memory.createVertex(text);
    			instagram.addRelationship(Primitive.ANSWEREDCOMMENTS, keywords);
    		}
    		memory.save();
    		
        } catch (Exception e) {
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
		
		sendIGMessage(text, replyTo);
        
		/*
        try {
        	sendIGMessage(text, conversationId);
        } catch (Exception e) {
        	log(e);
        }*/
        
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
		System.out.println("InputInstagramMessage Called.");
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
			
			property = this.bot.memory().getProperty("Instagram.likeAllComments");
			if (property != null) {
				this.likeAllComments = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.replyToComments");
			if (property != null) {
				this.replyToComments = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.autoPost");
			if (property != null) {
				this.autoPost = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.messageEnabled");
			if (property != null) {
				this.messageEnabled = Boolean.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.maxPost");
			if (property != null) {
				this.maxPost = Integer.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.maxLike");
			if (property != null) {
				this.maxLike = Integer.valueOf(property);
			}
			
			property = this.bot.memory().getProperty("Instagram.maxComment");
			if (property != null) {
				this.maxComment = Integer.valueOf(property);
			}
			
			this.imageURLs = new ArrayList<String>();
			List<Relationship> images = instagram.orderedRelationships(Primitive.IMAGEURLS);
			if (images != null) {
				for (Relationship relationship : images) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.imageURLs.add(text);
					}
				}
			}
			
			this.captions = new ArrayList<String>();
			List<Relationship> captions = instagram.orderedRelationships(Primitive.CAPTIONS);
			if (captions != null) {
				for (Relationship relationship : captions) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.captions.add(text);
					}
				}
			}
			
			this.answeredComments = new ArrayList<String>();
			List<Relationship> answeredComments = instagram.orderedRelationships(Primitive.ANSWEREDCOMMENTS);
			if (answeredComments != null) {
				for (Relationship relationship : answeredComments) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.answeredComments.add(text);
					}
				}
			}
			
			this.isInitialized = true;
		}
	}
	
	// Save Authentication properties in the database
	public void saveAuth() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Instagram.userName", this.userName, true);
		memory.saveProperty("Instagram.token", this.token, true);
		memory.saveProperty("Instagram.id", this.id, true);
		memory.saveProperty("Instagram.result", this.result, true);
		
		// Save the Oauthkey
		if (this.appOauthKey == null || this.appOauthKey.isEmpty()) {
			memory.saveProperty("Instagram.appOauthKey", "", true);
		} else {
			memory.saveProperty("Instagram.appOauthKey", Utils.encrypt(Utils.KEY, this.appOauthKey), true);
		}
		
		// Save the OauthSecret
		if (this.appOauthKey == null || this.appOauthKey.isEmpty()) {
			memory.saveProperty("Instagram.appOauthSecret", "", true);
		} else {
			memory.saveProperty("Instagram.appOauthSecret", Utils.encrypt(Utils.KEY, this.appOauthSecret), true);
		}
		
		// Save the Page Access Token
		System.out.println("Save PAT: " + this.pageAccessToken);
		if (this.pageAccessToken == null || getPageAccessToken().isEmpty()) {
			memory.saveProperty("Instagram.pageAccessToken", "", true);
		} else {
			memory.saveProperty("Instagram.pageAccessToken", Utils.encrypt(Utils.KEY, this.pageAccessToken), true);
		}
		
	}
	
	// Save User Instagram preference properties to the database
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		
		System.out.println("Save messageEnabled: " + messageEnabled);
		
		memory.saveProperty("Instagram.likeAllComments", String.valueOf(this.likeAllComments), true);
		memory.saveProperty("Instagram.replyToComments", String.valueOf(this.replyToComments), true);
		memory.saveProperty("Instagram.autoPost", String.valueOf(this.autoPost), true);
		memory.saveProperty("Instagram.messageEnabled", String.valueOf(this.messageEnabled), true);
		
		memory.saveProperty("Instagram.maxPost", String.valueOf(this.maxPost), true);
		memory.saveProperty("Instagram.maxLike", String.valueOf(this.maxLike), true);
		memory.saveProperty("Instagram.maxComment", String.valueOf(this.maxComment), true);
		
		
		Vertex instagram = memory.createVertex(getPrimitive());
		
		instagram.internalRemoveRelationships(Primitive.IMAGEURLS);
		for (String text : this.imageURLs) {
			Vertex keywords =  memory.createVertex(text);
			instagram.addRelationship(Primitive.IMAGEURLS, keywords);
		}
		
		instagram.internalRemoveRelationships(Primitive.CAPTIONS);
		for (String text : this.captions) {
			Vertex keywords =  memory.createVertex(text);
			instagram.addRelationship(Primitive.CAPTIONS, keywords);
		}
		
		instagram.internalRemoveRelationships(Primitive.ANSWEREDCOMMENTS);
		for (String text : this.answeredComments) {
			Vertex keywords =  memory.createVertex(text);
			instagram.addRelationship(Primitive.ANSWEREDCOMMENTS, keywords);
		}
		
		memory.save();
	}
	
}
