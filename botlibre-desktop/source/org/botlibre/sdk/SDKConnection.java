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

package org.botlibre.sdk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.config.AvatarMessage;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.Config;
import org.botlibre.sdk.config.ContentConfig;
import org.botlibre.sdk.config.ConversationConfig;
import org.botlibre.sdk.config.DomainConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.ForumPostConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.LearningConfig;
import org.botlibre.sdk.config.MediaConfig;
import org.botlibre.sdk.config.ResponseConfig;
import org.botlibre.sdk.config.ResponseSearchConfig;
import org.botlibre.sdk.config.Speech;
import org.botlibre.sdk.config.TrainingConfig;
import org.botlibre.sdk.config.UserAdminConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserMessageConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

/**
 * Connection class for a REST service connection.
 * The SDK connection gives you access to the paphus or libre server services using a REST API.
 * <p>
 * The services include:
 * <ul>
 * <li> User management (account creation, validation)
 * <li> Bot access, chat, and administration
 * <li> Forum access, posting, and administration
 * <li> Live chat access, chat, and administration
 * <li> Domain access, and administration
 * </ul>
 */
public class SDKConnection {
	protected static String[] types = new String[]{"Bots", "Forums", "Graphics", "Live Chat", "Domains", "Scripts"};
	protected static String[] channelTypes = new String[]{"ChatRoom", "OneOnOne"};
	protected static String[] accessModes = new String[]{"Everyone", "Users", "Members", "Administrators"};
	protected static String[] mediaAccessModes = new String[]{"Everyone", "Users", "Members", "Administrators", "Disabled"};
	protected static String[] learningModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	protected static String[] correctionModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	protected static String[] botModes = new String[]{"ListenOnly", "AnswerOnly", "AnswerAndListen"};
	
	protected String url;
	protected UserConfig user;
	protected DomainConfig domain;
	protected Credentials credentials;
	protected boolean debug = false;
	
	protected SDKException exception;

	/**
	 * Return the name of the default user image.
	 */
	public static String defaultUserImage() {
		return "images/user-thumb.jpg";
	}
	
	/**
	 * Create an SDK connection.
	 */
	public SDKConnection() {
	}
	
	/**
	 * Create an SDK connection with the credentials.
	 * Use the Credentials subclass specific to your server.
	 */
	public SDKConnection(Credentials credentials) {
		this.credentials = credentials;
		this.url = credentials.url;
	}
	
	/**
	 * Validate the user credentials (password, or token).
	 * The user details are returned (with a connection token, password removed).
	 * The user credentials are soted in the connection, and used on subsequent calls.
	 * An SDKException is thrown if the connect failed.
	 */
	public UserConfig connect(UserConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/check-user", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			this.user = null;
			return null;
		}
		try {
			UserConfig user = new UserConfig();
			user.parseXML(root);
			this.user = user;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
		return this.user;
	}
	
	/**
	 * Execute the custom API.
	 */
	public Config custom(String api, Config config, Config result) {
		config.addCredentials(this);
		String xml = POST(this.url + "/" + api, config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			result.parseXML(root);
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
		return result;
	}
	
	
	/**
	 * Connect to the live chat channel and return a LiveChatConnection.
	 * A LiveChatConnection is separate from an SDKConnection and uses web sockets for
	 * asynchronous communication.
	 * The listener will be notified of all messages.
	 */
	public LiveChatConnection openLiveChat(ChannelConfig channel, LiveChatListener listener) {
		LiveChatConnection connection = new LiveChatConnection(this.credentials, listener);
		connection.connect(channel, this.user);
		return connection;
	}
	
	/**
	 * Connect to the domain.
	 * A domain is an isolated content space.
	 * Any browse or query request will be specific to the domain's content.
	 */	
	public DomainConfig connect(DomainConfig config) {
		this.domain = fetch(config);
		return this.domain;
	}
	
	/**
	 * Disconnect from the connection.
	 * An SDKConnection does not keep a live connection, but this resets its connected user and domain.
	 */	
	public void disconnect() {
		this.user = null;
		this.domain = null;
	}
	
	/**
	 * Fetch the user details.
	 */	
	public UserConfig fetch(UserConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/view-user", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			UserConfig user = new UserConfig();
			user.parseXML(root);
			return user;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Fetch the URL for the image from the server.
	 */	
	public URL fetchImage(String image) {
		try {
			return new URL("http://" + this.credentials.host + this.credentials.app + "/" + image);
		} catch (Exception exception) {
			this.exception = new SDKException(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Fetch the forum post details for the forum post id.
	 */	
	public ForumPostConfig fetch(ForumPostConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/check-forum-post", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			ForumPostConfig post = new ForumPostConfig();
			post.parseXML(root);
			return post;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Create a new user.
	 */
	public UserConfig create(UserConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/create-user", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			UserConfig user = new UserConfig();
			user.parseXML(root);
			this.user = user;
			return user;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Create a new forum post.
	 * You must set the forum id for the post.
	 */
	public ForumPostConfig create(ForumPostConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/create-forum-post", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			ForumPostConfig post = new ForumPostConfig();
			post.parseXML(root);
			return post;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}

	/**
	 * Create a new file/image/media attachment for a chat channel.
	 */
	public MediaConfig createChannelFileAttachment(String file, MediaConfig config) {
		config.addCredentials(this);
		String xml = POSTFILE(this.url + "/create-channel-attachment", file, config.name, config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			MediaConfig media = new MediaConfig();
			media.parseXML(root);
			return media;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}

	/**
	 * Create a new file/image/media attachment for a chat channel.
	 */
	public MediaConfig createChannelImageAttachment(String file, MediaConfig config) {
		config.addCredentials(this);
		String xml = POSTIMAGE(this.url + "/create-channel-attachment", file, config.name, config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			MediaConfig media = new MediaConfig();
			media.parseXML(root);
			return media;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Create a reply to a forum post.
	 * You must set the parent id for the post replying to.
	 */
	public ForumPostConfig createReply(ForumPostConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/create-reply", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			ForumPostConfig reply = new ForumPostConfig();
			reply.parseXML(root);
			return reply;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Create a user message.
	 * This can be used to send a user a direct message.
	 * SPAM will cause your account to be deleted.
	 */
	public void createUserMessage(UserMessageConfig config) {
		config.addCredentials(this);
		POST(this.url + "/create-user-message", config.toXML());
	}
	
	/**
	 * Fetch the content details from the server.
	 * The id or name and domain of the object must be set.
	 */
	@SuppressWarnings("unchecked")
	public <T extends WebMediumConfig> T fetch(T config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/check-" + config.getType(), config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			config = (T)config.getClass().newInstance();
			config.parseXML(root);
			return config;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Create the new content.
	 * The content will be returned with its new id.
	 */
	@SuppressWarnings("unchecked")
	public <T extends WebMediumConfig> T create(T config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/create-" + config.getType(), config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			config = (T)config.getClass().newInstance();
			config.parseXML(root);
			return config;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Update the content.
	 */
	@SuppressWarnings("unchecked")
	public <T extends WebMediumConfig> T update(T config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/update-" + config.getType(), config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			config = (T)config.getClass().newInstance();
			config.parseXML(root);
			return config;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Update the forum post.
	 */
	public ForumPostConfig update(ForumPostConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/update-forum-post", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			config = new ForumPostConfig();
			config.parseXML(root);
			return config;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Create or update the response.
	 * This can also be used to flag, unflag, validate, or invalidate a response.
	 */
	public ResponseConfig saveResponse(ResponseConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/save-response", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			ResponseConfig response = new ResponseConfig();
			response.parseXML(root);
			return response;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Update the user details.
	 * The password must be passed to allow the update.
	 */
	public UserConfig update(UserConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/update-user", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			UserConfig user = new UserConfig();
			user.parseXML(root);
			this.user = user;
			return user;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Permanently delete the content with the id.
	 */
	public void delete(WebMediumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/delete-" + config.getType(), config.toXML());
		if(this.domain!=null && this.domain.id.equals(config.id) && config.getType().equals("domain") ){
			domain=null;
		}
	}
	
	/**
	 * Permanently delete the forum post with the id.
	 */
	public void delete(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/delete-forum-post", config.toXML());
	}
	
	/**
	 * Permanently delete the response, greetings, or default response with the response id (and question id).
	 */
	public void delete(ResponseConfig config) {
		config.addCredentials(this);
		POST(this.url + "/delete-response", config.toXML());
	}
	
	/**
	 * Permanently delete the avatar media.
	 */
	public void delete(AvatarMedia config) {
		config.addCredentials(this);
		POST(this.url + "/delete-avatar-media", config.toXML());
	}
	
	/**
	 * Permanently delete the avatar background.
	 */
	public void deleteAvatarBackground(AvatarConfig config) {
		config.addCredentials(this);
		POST(this.url + "/delete-avatar-background", config.toXML());
	}
	
	/**
	 * Save the avatar media tags.
	 */
	public void saveAvatarMedia(AvatarMedia config) {
		config.addCredentials(this);
		POST(this.url + "/save-avatar-media", config.toXML());
	}
	
	/**
	 * Flag the content as offensive, a reason is required.
	 */
	public void flag(WebMediumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/flag-" + config.getType(), config.toXML());
	}
	
	/**
	 * Subscribe for email updates for the post.
	 */
	public void subscribe(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/subscribe-post", config.toXML());
	}
	
	/**
	 * Subscribe for email updates for the forum.
	 */
	public void subscribe(ForumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/subscribe-forum", config.toXML());
	}
	
	/**
	 * Unsubscribe from email updates for the post.
	 */
	public void unsubscribe(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/unsubscribe-post", config.toXML());
	}
	
	/**
	 * Unsubscribe from email updates for the forum.
	 */
	public void unsubscribe(ForumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/unsubscribe-forum", config.toXML());
	}
	
	/**
	 * Thumbs up the content.
	 */
	public void thumbsUp(WebMediumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/thumbs-up-" + config.getType(), config.toXML());
	}
	
	/**
	 * Thumbs down the content.
	 */
	public void thumbsDown(WebMediumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/thumbs-down-" + config.getType(), config.toXML());
	}
	
	/**
	 * Rate the content.
	 */
	public void star(WebMediumConfig config) {
		config.addCredentials(this);
		POST(this.url + "/star-" + config.getType(), config.toXML());
	}
	
	/**
	 * Thumbs up the content.
	 */
	public void thumbsUp(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/thumbs-up-post", config.toXML());
	}
	
	/**
	 * Thumbs down the content.
	 */
	public void thumbsDown(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/thumbs-down-post", config.toXML());
	}
	
	/**
	 * Rate the content.
	 */
	public void star(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/star-post", config.toXML());
	}
	
	/**
	 * Flag the forum post as offensive, a reason is required.
	 */
	public void flag(ForumPostConfig config) {
		config.addCredentials(this);
		POST(this.url + "/flag-forum-post", config.toXML());
	}
	
	/**
	 * Flag the user post as offensive, a reason is required.
	 */
	public void flag(UserConfig config) {
		config.addCredentials(this);
		POST(this.url + "/flag-user", config.toXML());
	}
	
	/**
	 * Process the bot chat message and return the bot's response.
	 * The ChatConfig should contain the conversation id if part of a conversation.
	 * If a new conversation the conversation id is returned in the response. 
	 */
	public ChatResponse chat(ChatConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/post-chat", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			ChatResponse response = new ChatResponse();
			response.parseXML(root);
			return response;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Process the avatar message and return the avatars response.
	 * This allows the speech and video animation for an avatar to be generated for the message.
	 */
	public ChatResponse avatarMessage(AvatarMessage config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/avatar-message", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			ChatResponse response = new ChatResponse();
			response.parseXML(root);
			return response;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Process the speech message and return the server generate text-to-speech audio file.
	 * This allows for server-side speech generation.
	 */
	public String tts(Speech config) {
		config.addCredentials(this);
		return POST(this.url + "/speak", config.toXML());
	}
	
	/**
	 * Return the administrators of the content.
	 */
	public List<String> getAdmins(WebMediumConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-" + config.getType() + "-admins", config.toXML());
		List<String> users = new ArrayList<String>();
		Element root = parse(xml);
		if (root == null) {
			return users;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			UserConfig user = new UserConfig();
			user.parseXML((Element)root.getChildNodes().item(index));
			users.add(user.user);
		}
		return users;
	}
	
	/**
	 * Return the list of user details for the comma separated values list of user ids.
	 */
	public List<UserConfig> getUsers(String usersCSV) {
		UserConfig config = new UserConfig();
		config.user = usersCSV;
		config.addCredentials(this);
		String xml = POST(this.url + "/get-users", config.toXML());
		List<UserConfig> users = new ArrayList<UserConfig>();
		Element root = parse(xml);
		if (root == null) {
			return users;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			Element child = (Element)root.getChildNodes().item(index);
			UserConfig userConfig = new UserConfig();
			userConfig.parseXML(child);
			users.add(userConfig);
		}
		return users;
	}
	
	/**
	 * Return the list of forum posts for the forum browse criteria.
	 */
	public List<ForumPostConfig> getPosts(BrowseConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-forum-posts", config.toXML());
		List<ForumPostConfig> instances = new ArrayList<ForumPostConfig>();
		Element root = parse(xml);
		if (root == null) {
			return instances;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			ForumPostConfig post = new ForumPostConfig();
			post.parseXML((Element)root.getChildNodes().item(index));
			instances.add(post);
		}
		return instances;
	}
	
	/**
	 * Return the list of categories for the type, and domain.
	 */
	
	public List<ContentConfig> getCategories(ContentConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-categories", config.toXML());
		List<ContentConfig> categories = new ArrayList<ContentConfig>();
		Element root = parse(xml);
		if (root == null) {
			return categories;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			ContentConfig category = new ContentConfig();
			category.parseXML((Element)root.getChildNodes().item(index));
			categories.add(category);
		}
		return categories;
	}
	
	/**
	 * Return the list of tags for the type, and domain.
	 */
	public List<String> getTags(ContentConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-tags", config.toXML());
		List<String> tags = new ArrayList<String>();
		tags.add("");
		Element root = parse(xml);
		if (root == null) {
			return tags;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			tags.add(((Element)root.getChildNodes().item(index)).getAttribute("name"));
		}
		return tags;
	}
	
	/**
	 * Return the list of bot templates.
	 */
	public List<String> getTemplates() {
		String xml = GET(this.url + "/get-all-templates");
		List<String> instances = new ArrayList<String>();
		Element root = parse(xml);
		if (root == null) {
			return instances;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			instances.add(((Element)root.getChildNodes().item(index)).getAttribute("name"));
		}
		return instances;
	}
	
	/**
	 * Return the users for the content.
	 */
	public List<String> getUsers(WebMediumConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-" + config.getType() + "-users", config.toXML());
		List<String> users = new ArrayList<String>();
		Element root = parse(xml);
		if (root == null) {
			return users;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			UserConfig user = new UserConfig();
			user.parseXML((Element)root.getChildNodes().item(index));
			users.add(user.user);
		}
		return users;
	}
	
	/**
	 * Return the channel's bot configuration.
	 */
	public BotModeConfig getChannelBotMode(ChannelConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-channel-bot-mode", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			BotModeConfig botMode = new BotModeConfig();
			botMode.parseXML(root);
			return botMode;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Save the channel's bot configuration.
	 */
	public void saveChannelBotMode(BotModeConfig config) {
		config.addCredentials(this);
		POST(this.url + "/save-channel-bot-mode", config.toXML());
	}
	
	/**
	 * Save the forum's bot configuration.
	 */
	public void saveForumBotMode(BotModeConfig config) {
		config.addCredentials(this);
		POST(this.url + "/save-forum-bot-mode", config.toXML());
	}
	
	/**
	 * Save the bot's learning configuration.
	 */
	public void saveLearning(LearningConfig config) {
		config.addCredentials(this);
		POST(this.url + "/save-learning", config.toXML());
	}
	
	/**
	 * Save the bot's voice configuration.
	 */
	public void saveVoice(VoiceConfig config) {
		config.addCredentials(this);
		POST(this.url + "/save-voice", config.toXML());
	}
	
	/**
	 * Save the bot's avatar configuration.
	 */
	public void saveBotAvatar(InstanceConfig config) {
		config.addCredentials(this);
		POST(this.url + "/save-bot-avatar", config.toXML());
	}
	
	/**
	 * Train the bot with a new question/response pair.
	 */
	public void train(TrainingConfig config) {
		config.addCredentials(this);
		POST(this.url + "/train-instance", config.toXML());
	}
	
	/**
	 * Perform the user administration task (add or remove users, or administrators).
	 */
	public void userAdmin(UserAdminConfig config) {
		config.addCredentials(this);
		POST(this.url + "/user-admin", config.toXML());
	}
	
	/**
	 * Save the image as the avatar's background.
	 */
	public void saveAvatarBackground(String file, AvatarMedia config) {
		config.addCredentials(this);
		POSTIMAGE(this.url + "/save-avatar-background", file, config.name, config.toXML());
	}
	
	/**
	 * Add the avatar media file to the avatar.
	 */
	public void createAvatarMedia(String file, AvatarMedia config) {
		config.addCredentials(this);
		if ((file.indexOf(".jpg") != -1) || (file.indexOf(".jpeg") != -1)) {
			if (config.hd) {
				POSTHDIMAGE(this.url + "/create-avatar-media", file, config.name, config.toXML());
			} else {
				POSTIMAGE(this.url + "/create-avatar-media", file, config.name, config.toXML());
			}
		} else {
			POSTFILE(this.url + "/create-avatar-media", file, config.name, config.toXML());
		}
	}
	
	/**
	 * Update the contents icon.
	 * The file will be uploaded to the server.
	 */
	@SuppressWarnings("unchecked")
	public <T extends WebMediumConfig> T updateIcon(String file, T config) {
		config.addCredentials(this);
		String xml = POSTIMAGE(this.url + "/update-" + config.getType() + "-icon", file, "image.jpg", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			config = (T)config.getClass().newInstance();
			config.parseXML(root);
			return config;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Update the user's icon.
	 * The file will be uploaded to the server.
	 */
	public UserConfig updateIcon(String file, UserConfig config) {
		config.addCredentials(this);
		String xml = POSTIMAGE(this.url + "/update-user-icon", file, "image.jpg", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			config = new UserConfig();
			config.parseXML(root);
			return config;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	public String POSTIMAGE(String url, String file, String name, String xml) {
		if (this.debug) {
			System.out.println("POST: " + url);
			System.out.println("file: " + file);
			System.out.println("XML: " + xml);
		}
		String result = "";
		try {
			Bitmap bitmap = loadImage(file, 300, 300);
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
	        byte[] byte_arr = stream.toByteArray();
	        ByteArrayBody fileBody = new ByteArrayBody(byte_arr, name);
	
	        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	
	        multipartEntity.addPart("file", fileBody);
	        multipartEntity.addPart("xml", new StringBody(xml));
	
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response = null;

            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(multipartEntity);
            response = httpclient.execute(httppost);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, HTTP.UTF_8);
			}

			if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
				this.exception = new SDKException(""
				   + response.getStatusLine().getStatusCode()
				   + " : " + result);
	 			throw this.exception;
			}

        } catch (Exception exception) {
 			this.exception = new SDKException(exception);
 			throw this.exception;
 		}
 		return result;
	}
	
	public String POSTHDIMAGE(String url, String file, String name, String xml) {
		if (this.debug) {
			System.out.println("POST: " + url);
			System.out.println("file: " + file);
			System.out.println("XML: " + xml);
		}
		String result = "";
		try {
			Bitmap bitmap = loadImage(file, 600, 600);
	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
	        //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
	        byte[] byte_arr = stream.toByteArray();
	        ByteArrayBody fileBody = new ByteArrayBody(byte_arr, name);
	
	        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	
	        multipartEntity.addPart("file", fileBody);
	        multipartEntity.addPart("xml", new StringBody(xml));
	
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response = null;

            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(multipartEntity);
            response = httpclient.execute(httppost);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, HTTP.UTF_8);
			}

			if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
				this.exception = new SDKException(""
				   + response.getStatusLine().getStatusCode()
				   + " : " + result);
	 			throw this.exception;
			}

        } catch (Exception exception) {
 			this.exception = new SDKException(exception);
 			throw this.exception;
 		}
 		return result;
	}
	
	public String POSTFILE(String url, String path, String name, String xml) {
		if (this.debug) {
			System.out.println("POST: " + url);
			System.out.println("file: " + path);
			System.out.println("XML: " + xml);
		}
		String result = "";
		try {
			File file = new File(path);
			FileInputStream stream = new FileInputStream(file);
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			int read = 0;
	        byte[] buffer = new byte[4096];
	        while ((read = stream.read(buffer)) != -1 ) {
	        	output.write(buffer, 0, read);
	        }
	        byte[] byte_arr = output.toByteArray();
	        stream.close();
	        ByteArrayBody fileBody = new ByteArrayBody(byte_arr, name);
	
	        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
	
	        multipartEntity.addPart("file", fileBody);
	        multipartEntity.addPart("xml", new StringBody(xml));
	
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpResponse response = null;

            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(multipartEntity);
            response = httpclient.execute(httppost);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, HTTP.UTF_8);
			}

			if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
				this.exception = new SDKException(""
				   + response.getStatusLine().getStatusCode()
				   + " : " + result);
	 			throw this.exception;
			}

        } catch (Exception exception) {
 			this.exception = new SDKException(exception);
 			throw this.exception;
 		}
 		return result;
	}

	public Bitmap loadImage(String path, int reqWidth, int reqHeight) {
	    /*BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

        int height = options.outHeight;
        int width = options.outWidth;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;

        if (height > reqHeight) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }

        int expectedWidth = width / inSampleSize;

        if (expectedWidth > reqWidth) {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }

	    options.inSampleSize = inSampleSize;
	    options.inJustDecodeBounds = false;

	    return BitmapFactory.decodeFile(path, options);*/
		return null;
	 }
	
	/**
	 * Return the forum's bot configuration.
	 */
	public BotModeConfig getForumBotMode(ForumConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-forum-bot-mode", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			BotModeConfig botMode = new BotModeConfig();
			botMode.parseXML(root);
			return botMode;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Return the bot's voice configuration.
	 */
	public VoiceConfig getVoice(InstanceConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-voice", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			VoiceConfig voice = new VoiceConfig();
			voice.parseXML(root);
			return voice;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Return the bot's default responses.
	 */
	public List<String> getDefaultResponses(InstanceConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-default-responses", config.toXML());
		List<String> defaultResponses = new ArrayList<String>();
		Element root = parse(xml);
		if (root == null) {
			return defaultResponses;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			defaultResponses.add(((Element)root.getChildNodes().item(index)).getChildNodes().item(0).getTextContent());
		}
		return defaultResponses;
	}
	
	/**
	 * Return the bot's greetings.
	 */
	public List<String> getGreetings(InstanceConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-greetings", config.toXML());
		List<String> greetings = new ArrayList<String>();
		Element root = parse(xml);
		if (root == null) {
			return greetings;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			greetings.add(((Element)root.getChildNodes().item(index)).getChildNodes().item(0).getTextContent());
		}
		return greetings;
	}
	
	/**
	 * Search the bot's responses.
	 */
	public List<ResponseConfig> getResponses(ResponseSearchConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-responses", config.toXML());
		List<ResponseConfig> responses = new ArrayList<ResponseConfig>();
		Element root = parse(xml);
		if (root == null) {
			return responses;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			ResponseConfig response = new ResponseConfig();
			response.parseXML((Element)root.getChildNodes().item(index));
			responses.add(response);
		}
		return responses;
	}
	
	/**
	 * Search the bot's conversations.
	 */
	public List<ConversationConfig> getConversations(ResponseSearchConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-conversations", config.toXML());
		List<ConversationConfig> conversations = new ArrayList<ConversationConfig>();
		Element root = parse(xml);
		if (root == null) {
			return conversations;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			ConversationConfig response = new ConversationConfig();
			response.parseXML((Element)root.getChildNodes().item(index));
			conversations.add(response);
		}
		return conversations;
	}
	
	/**
	 * Return the bot's learning configuration.
	 */
	public LearningConfig getLearning(InstanceConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-learning", config.toXML());
		Element root = parse(xml);
		if (root == null) {
			return null;
		}
		try {
			LearningConfig learning = new LearningConfig();
			learning.parseXML(root);
			return learning;
		} catch (Exception exception) {
			this.exception = SDKException.parseFailure(exception);
			throw this.exception;
		}
	}
	
	/**
	 * Return the list of content for the browse criteria.
	 * The type defines the content type (one of Bot, Forum, Channel, Domain).
	 */
	@SuppressLint("DefaultLocale")
	public List<WebMediumConfig> browse(BrowseConfig config) {
		config.addCredentials(this);
		String type = "";
		if (config.type.equals("Bot")) {
			type = "/get-instances";
		} else {
			type = "/get-" + config.type.toLowerCase() + "s";
		}
		String xml = POST(this.url + type, config.toXML());
		List<WebMediumConfig> instances = new ArrayList<WebMediumConfig>();
		Element root = parse(xml);
		if (root == null) {
			return instances;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			WebMediumConfig instance = null;
			if (config.type.equals("Bot")) {
				instance = new InstanceConfig();
			} else if (config.type.equals("Forum")) {
				instance = new ForumConfig();
			} else if (config.type.equals("Channel")) {
				instance = new ChannelConfig();
			} else if (config.type.equals("Domain")) {
				instance = new DomainConfig();
			} else if (config.type.equals("Avatar")) {
				instance = new AvatarConfig();
			}
			instance.parseXML((Element)root.getChildNodes().item(index));
			instances.add(instance);
		}
		return instances;
	}
	
	/**
	 * Return the list of media for the avatar.
	 */
	public List<AvatarMedia> getAvatarMedia(AvatarConfig config) {
		config.addCredentials(this);
		String xml = POST(this.url + "/get-avatar-media", config.toXML());
		List<AvatarMedia> instances = new ArrayList<AvatarMedia>();
		Element root = parse(xml);
		if (root == null) {
			return instances;
		}
		for (int index = 0; index < root.getChildNodes().getLength(); index++) {
			AvatarMedia instance = new AvatarMedia();
			instance.parseXML((Element)root.getChildNodes().item(index));
			instances.add(instance);
		}
		return instances;
	}
	
	/**
	 * Return the list of content types.
	 */
	public String[] getTypes() {
		return types;
	}
	
	/**
	 * Return the channel types.
	 */
	public String[] getChannelTypes() {
		return channelTypes;
	}
	
	/**
	 * Return the access mode types.
	 */
	public String[] getAccessModes() {
		return accessModes;
	}
	
	/**
	 * Return the media access mode types.
	 */
	public String[] getMediaAccessModes() {
		return mediaAccessModes;
	}
	
	/**
	 * Return the learning mode types.
	 */
	public String[] getLearningModes() {
		return learningModes;
	}
	
	/**
	 * Return the correction mode types.
	 */
	public String[] getCorrectionModes() {
		return correctionModes;
	}
	
	/**
	 * Return the bot mode types.
	 */
	public String[] getBotModes() {
		return botModes;
	}
	
	/**
	 * Return the current connected user.
	 */
	public UserConfig getUser() {
		return user;
	}
	
	/**
	 * Set the current connected user.
	 * connect() should be used to validate and connect a user.
	 */
	public void setUser(UserConfig user) {
		this.user = user;
	}
	
	/**
	 * Return the current domain.
	 * A domain is an isolated content space.
	 */
	public DomainConfig getDomain() {
		return domain;
	}
	
	/**
	 * Set the current domain.
	 * A domain is an isolated content space.
	 * connect() should be used to validate and connect a domain.
	 */
	public void setDomain(DomainConfig domain) {
		this.domain = domain;
	}
	
	/**
	 * Return the current application credentials.
	 */
	public Credentials getCredentials() {
		return credentials;
	}
	
	/**
	 * Set the application credentials.
	 */
	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
		this.url = credentials.url;
	}
	
	/**
	 * Return is debugging has been enabled.
	 */
	public boolean isDebug() {
		return debug;
	}
	
	/**
	 * Enable debugging, debug messages will be logged to System.out.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * Return the last thrown exception.
	 */
	public SDKException getException() {
		return exception;
	}

	protected void setException(SDKException exception) {
		this.exception = exception;
	}

	public String GET(String url) {
		if (this.debug) {
			System.out.println("GET: " + url);
		}
		String xml = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpGet, localContext);
			HttpEntity entity = response.getEntity();
			xml = EntityUtils.toString(entity, HTTP.UTF_8);

			if (response.getStatusLine().getStatusCode() != 200) {
				this.exception = new SDKException(""
				   + response.getStatusLine().getStatusCode()
				   + " : " + xml);
				return "";
			}
		} catch (Exception exception) {
			if (this.debug) {
				exception.printStackTrace();
			}
			this.exception = new SDKException(exception);
			throw this.exception;
		}
		return xml;
	}

	public String POST(String url, String xml) {
		if (this.debug) {
			System.out.println("POST: " + url);
			System.out.println("XML: " + xml);
		}
		String result = "";
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(url);
			
			StringEntity content = new StringEntity(xml, "utf-8");
			content.setContentType("application/xml");
			httpPost.setEntity(content);
			
			HttpResponse response = httpClient.execute(httpPost, localContext);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, HTTP.UTF_8);
			}

			if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
				this.exception = new SDKException(""
				   + response.getStatusLine().getStatusCode()
				   + " : " + result);
				throw this.exception;
			}
		} catch (Exception exception) {
			if (this.debug) {
				exception.printStackTrace();
			}
			this.exception = new SDKException(exception);
			throw this.exception;
		}
		return result;
	}
	
	public Element parse(String xml) {
		if (this.debug) {
			System.out.println(xml);
		}
		Document dom = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource source = new InputSource();
			source.setCharacterStream(new StringReader(xml));
			dom = builder.parse(source);
			return dom.getDocumentElement();
		} catch (Exception exception) {
			if (this.debug) {
				exception.printStackTrace();
			}
			this.exception = new SDKException(exception.getMessage(), exception);
			throw this.exception;
		}
	}
}