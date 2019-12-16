/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
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
package org.botlibre.web.bean;

import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.facebook.Facebook;
import org.botlibre.sense.facebook.FacebookMessaging;
import org.botlibre.sense.http.Http;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

import facebook4j.FacebookException;

public class FacebookBean extends ServletBean {
	
	protected String authorisationURL;
	
	public FacebookBean() {
	}

	public String getUserName() {
		return getBot().awareness().getSense(Facebook.class).getUserName();
	}

	public String getProfileName() {
		return getBot().awareness().getSense(Facebook.class).getProfileName();
	}

	public String getToken() {
		return getBot().awareness().getSense(Facebook.class).getToken();
	}

	public String getAppOauthKey() {
		return getBot().awareness().getSense(Facebook.class).getAppOauthKey();
	}

	public String getAppOauthSecret() {
		return getBot().awareness().getSense(Facebook.class).getAppOauthSecret();
	}

	public String getFacebookPage() {
		return getBot().awareness().getSense(Facebook.class).getPage();
	}

	public List<String> getPages() {
		return getBot().awareness().getSense(Facebook.class).getPages();
	}
	
	public String getPagesString() {
		StringWriter writer = new StringWriter();
		int count = 1;
		List<String> pages = getPages();
		for (String page : pages) {
			writer.write("\"");
			writer.write(page);
			writer.write("\"");
			if (count < pages.size()) {
				writer.write(", ");
			}
			count++;
		}
		return writer.toString();
	}

	public Date getTokenExpiry() {
		return getBot().awareness().getSense(Facebook.class).getTokenExpiry();
	}

	public boolean getAutoFriend() {
		return getBot().awareness().getSense(Facebook.class).getAutoFriend();
	}

	public int getMaxFriends() {
		return getBot().awareness().getSense(Facebook.class).getMaxFriends();
	}

	public int getMaxPost() {
		return getBot().awareness().getSense(Facebook.class).getMaxPost();
	}

	public int getMaxLike() {
		return getBot().awareness().getSense(Facebook.class).getMaxLike();
	}

	public boolean getProcessAllPosts() {
		return getBot().awareness().getSense(Facebook.class).getProcessAllPosts();
	}

	public boolean getProcessNewsFeed() {
		return getBot().awareness().getSense(Facebook.class).getProcessNewsFeed();
	}

	public boolean getProcessAllNewsFeed() {
		return getBot().awareness().getSense(Facebook.class).getProcessAllNewsFeed();
	}

	public boolean getLikeAllPosts() {
		return getBot().awareness().getSense(Facebook.class).getLikeAllPosts();
	}

	public boolean getProcessPost() {
		return getBot().awareness().getSense(Facebook.class).getProcessPost();
	}

	public boolean getDisableMessages() {
		return !getBot().awareness().getSense(Facebook.class).getReplyToMessages() && !getBot().awareness().getSense(Facebook.class).getFacebookMessenger();
	}

	public boolean getReplyToMessages() {
		return getBot().awareness().getSense(Facebook.class).getReplyToMessages();
	}

	public boolean getFacebookMessenger() {
		return getBot().awareness().getSense(Facebook.class).getFacebookMessenger();
	}

	public boolean getStripButtonText() {
		return getBot().awareness().getSense(Facebook.class).getStripButtonText();
	}

	public boolean getTrackMessageObjects() {
		return getBot().awareness().getSense(Facebook.class).getTrackMessageObjects();
	}

	public String getFacebookMessengerAccessToken() {
		return getBot().awareness().getSense(Facebook.class).getFacebookMessengerAccessToken();
	}

	public String getButtonType() {
		return getBot().awareness().getSense(Facebook.class).getButtonType();
	}

	public String getPersistentMenu() {
		return getBot().awareness().getSense(Facebook.class).getPersistentMenu();
	}

	public String getGetStartedButton() {
		return getBot().awareness().getSense(Facebook.class).getGetStartedButton();
	}

	public String getGreetingText() {
		return getBot().awareness().getSense(Facebook.class).getGreetingText();
	}

	public String getWelcomeMessage() {
		return getBot().awareness().getSense(Facebook.class).getWelcomeMessage();
	}

	public boolean getAutoPost() {
		return getBot().awareness().getSense(Facebook.class).getAutoPost();
	}

	public int getAutoPostHours() {
		return getBot().awareness().getSense(Facebook.class).getAutoPostHours();
	}

	public String getLikeKeywords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Facebook.class).getLikeKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}

	public String getAutoPosts() {
		List<Vertex> autotweets = getBot().awareness().getSense(Facebook.class).getAutoPosts(getBot().memory().newMemory());
		if (autotweets == null || autotweets.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		Iterator<Vertex> iterator = autotweets.iterator();
		while (iterator.hasNext()) {
			Vertex tweet = iterator.next();
			if (tweet.instanceOf(Primitive.FORMULA)) {
				writer.write(tweet.getName());
			} else {
				writer.write(tweet.printString());
			}
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}

	public String getPostKeywords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Facebook.class).getPostKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}

	public String getNewsFeedKeywords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Facebook.class).getNewsFeedKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}

	public String getAutoFriendKeywords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Facebook.class).getAutoFriendKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}

	public String getPostRSS() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Facebook.class).getPostRSS().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}

	public String getRSSKeyWords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Facebook.class).getRssKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}
	
	public String getFriends() {
		StringWriter writer = new StringWriter();
		boolean first = true;
		List<String> friends = getBot().awareness().getSense(Facebook.class).getFriends();
		for (String friend : friends) {
			if (!first) {
				writer.write(", ");
			} else {
				first = false;
			}
			writer.write(friend);
		}
		if (friends.size() >= Facebook.MAX_LOOKUP) {
			writer.write("...");
		}
		return writer.toString();
	}
	
	public String getTimeline() {
		StringWriter writer = new StringWriter();
		boolean first = true;
		try {
			for (String tweet : getBot().awareness().getSense(Facebook.class).getTimeline()) {
				if (!first) {
					writer.write("</br>\n");
				} else {
					first = false;
				}
				writer.write(tweet);
			}
		} catch (Exception exception) {
			error(exception);
		}
		return writer.toString();
	}

	public boolean isConnected() {
		return getBotBean().getInstance().getEnableFacebook();
	}

	public boolean isAuthorising() {
		return this.authorisationURL != null;
	}

	public String getAuthorisationURL() {
		return this.authorisationURL;
	}

	public void authoriseAccount(String key, String secret, HttpServletRequest request) throws FacebookException {
		Facebook sense = getBot().awareness().getSense(Facebook.class);
		sense.setAppOauthKey(key.trim());
		sense.setAppOauthSecret(secret.trim());
		String server = request.getServerName().toLowerCase();
		this.authorisationURL = sense.authorizeAccount(http() + "://" + server + "/facebook");
	}

	public void authoriseComplete(String pin) throws FacebookException {
		Facebook sense = getBot().awareness().getSense(Facebook.class);
		sense.authorizeComplete(pin);
		this.authorisationURL = null;
	}

	public void cancelAuthorisation() throws FacebookException {
		this.authorisationURL = null;
	}

	public void connect(String userName, String token, String page, String key, String secret) throws FacebookException {
		Facebook sense = getBot().awareness().getSense(Facebook.class);
		sense.setUserName(userName.trim());
		sense.setToken(token.trim());
		sense.setAppOauthKey(key.trim());
		sense.setAppOauthSecret(secret.trim());
		sense.setPage(page.trim());
		sense.saveProperties(null);
		sense.connectAccount();
		sense.setIsEnabled(true);
		Utils.sleep(100);
		
		sense.subscribeToMessenger();
		
		sense = getBot().awareness().getSense(FacebookMessaging.class);
		sense.setUserName(userName.trim());
		sense.setToken(token.trim());
		sense.setAppOauthKey(key.trim());
		sense.setAppOauthSecret(secret.trim());
		sense.setPage(page.trim());
		sense.connectAccount();
		sense.setIsEnabled(true);
		Utils.sleep(100);

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceFacebook(getBotBean().getInstance().getId(), true));
	}

	public void save(boolean autoFriend, String autoFriendKeywords, String welcomeMessage, String maxFriends,
			String maxPosts, String postKeywords, boolean processPosts, boolean processAllPosts,
			String newsFeedKeywords, boolean processNewsFeed, boolean processAllNewsFeed,
			String messenger, String facebookMessengerAccessToken, String buttonType, boolean stripButtonText, boolean trackMessageObjects,
			String persistentMenu, String getStartedButton, String greetingText,
			String likeKeywords, boolean likeAllPosts,
			String postRSS, String rssKeywords,
			boolean autoPost, String autoPostHours, String autoPosts) throws FacebookException {
		if (!getBotBean().getInstance().isAdult()
				&& (Utils.checkProfanity(welcomeMessage) || Utils.checkProfanity(autoPosts))) {
			throw BotException.offensive();
		}
		Facebook sense = getBot().awareness().getSense(Facebook.class);
		/*
		sense.setWelcomeMessage(welcomeMessage.trim());
		sense.setAutoFriend(autoFriend);
		int max = 0;
		try {
			max = Integer.valueOf(maxFriends);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid max friends number - " + maxFriends + " - " + exception.getMessage());
		}
		if ((max < 0) || (max > 1000)) {
			throw new BotException("Max friends must be a number less than 1000.");
		}
		sense.setMaxFriends(max);*/
		int max = 0;

		if (Site.COMMERCIAL) {
			try {
				max = Integer.valueOf(maxPosts);
			} catch (NumberFormatException exception) {
				throw new BotException("Max posts must be a number less than 50 - " + maxPosts + " - " + exception.getMessage());
			}
			if ((max < 0) || (max > 50)) {
				throw new BotException("Max posts must be a number less than 50.");
			}
		} else {
			try {
				max = Integer.valueOf(maxPosts);
			} catch (NumberFormatException exception) {
				throw new BotException("Max posts be a number less than 20 - " + maxPosts + " - " + exception.getMessage());
			}
			if ((max < 0) || (max > 20)) {
				throw new BotException("Max posts must be a number less than 20 on basic accounts.");
			}
		}

		sense.setMaxPost(max);
		sense.setProcessPost(processPosts);
		sense.setProcessAllPosts(processAllPosts);
		postKeywords = postKeywords.trim();		
		postKeywords.replace(",", "\n");
		TextStream stream = new TextStream(postKeywords);
		sense.setPostKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getPostKeywords().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}

		sense.setProcessNewsFeed(processNewsFeed);
		sense.setProcessAllNewsFeed(processAllNewsFeed);
		if (newsFeedKeywords == null) {
			newsFeedKeywords = "";
		}
		newsFeedKeywords.replace(",", "\n");
		stream = new TextStream(newsFeedKeywords);
		sense.setNewsFeedKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getNewsFeedKeywords().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}

		sense.setReplyToMessages("poll".equals(messenger));
		sense.setFacebookMessenger("app".equals(messenger));
		sense.setFacebookMessengerAccessToken(facebookMessengerAccessToken);
		sense.setButtonType(buttonType);
		sense.setStripButtonText(stripButtonText);
		sense.setTrackMessageObjects(trackMessageObjects);
		boolean newPersistentMenu = !sense.getPersistentMenu().trim().equals(persistentMenu.trim());
		sense.setPersistentMenu(persistentMenu);
		boolean newGetStartedButton = !sense.getGetStartedButton().trim().equals(getStartedButton.trim());
		sense.setGetStartedButton(getStartedButton);
		boolean newGreetingText = !sense.getPersistentMenu().trim().equals(greetingText.trim());
		sense.setGreetingText(greetingText);

		/*autoFriendKeywords.replace(",", "\n");
		stream = new TextStream(autoFriendKeywords.trim());
		sense.setAutoFriendKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getAutoFriendKeywords().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}*/

		sense.setLikeAllPosts(likeAllPosts);
		likeKeywords.replace(",", "\n");
		stream = new TextStream(likeKeywords.trim());
		sense.setLikeKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getLikeKeywords().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		
		stream = new TextStream(postRSS.trim());
		sense.setPostRSS(new ArrayList<String>());
		String error = null;
		while (!stream.atEnd()) {
			String rss = stream.upToAny("\n").trim();
			if (!rss.isEmpty()) {
				if (!rss.contains("http")) {
					throw new BotException("Invalid RSS URL, must contain http - " + rss);
				}
				List<Map<String, Object>> feed = null;
				try {
					TextStream rssStream = new TextStream(rss);
					rssStream.upToAll("http");
					String url = rssStream.nextWord();
					feed = getBot().awareness().getSense(Http.class).parseRSSFeed(Utils.safeURL(url), System.currentTimeMillis());
				} catch (Exception failed) {
					AdminDatabase.instance().log(failed);
				}
				if (feed == null) {
					if (error == null) {
						error = "Invalid RSS detected, ensure the URL is valid and returns an RSS feed in XML form (check log for errors),";
					}
					error = error + "\n" + rss;
				}
				sense.getPostRSS().add(rss);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		if (error != null) {
			throw new BotException(error);
		}
		
		rssKeywords.replace(",", "\n");
		stream = new TextStream(rssKeywords.trim());
		sense.setRssKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String rss = stream.upToAny("\n").trim();
			sense.getRssKeywords().add(rss);
			stream.skip();
			stream.skipWhitespace();
		}
		sense.setAutoPost(autoPost);
		int hours = 0;
		try {
			hours = Integer.valueOf(autoPostHours);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid auto post hours number - " + autoPostHours + " - " + exception.getMessage());
		}
		sense.setAutoPostHours(hours);
		
		stream = new TextStream(autoPosts.trim());
		List<String> posts = new ArrayList<String>();
		while (!stream.atEnd()) {
			String tweet = stream.upToAny("\n").trim();
			if (!tweet.isEmpty()) {
				posts.add(tweet);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		sense.saveProperties(posts);
		if (newPersistentMenu || newGetStartedButton || newGreetingText) {
			sense.updateThreadSettings(persistentMenu, getStartedButton, greetingText);
		}
	}
	
	public String getWebhook() {
		if (!Site.HTTPS) {
			return "";
		}
		String hook = Site.SECUREURLLINK + "/rest/api/facebook/";
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		this.authorisationURL = null;
	}

	public void checkStatus() {
		getBot().setDebugLevel(Level.FINE);
		Facebook facebook = getBot().awareness().getSense(Facebook.class);
		facebook.checkProfile();

		if (facebook.getReplyToMessages()) {
			FacebookMessaging facebookMessaging = getBot().awareness().getSense(FacebookMessaging.class);
			facebookMessaging.checkProfile();
		}
	}

	public void disable() {
		getBot().awareness().getSense(Facebook.class).setIsEnabled(false);
		getBot().awareness().getSense(FacebookMessaging.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceFacebook(getBotBean().getInstance().getId(), false));
	}
}
