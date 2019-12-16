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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.twitter.Twitter;
import org.botlibre.sense.twitter.TwitterDirectMessaging;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User.UserType;

import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

public class TwitterBean extends ServletBean {
	
	protected RequestToken authorisationRequest;
	
	public TwitterBean() {
	}

	public String getUserName() {
		return getBot().awareness().getSense(Twitter.class).getUserName();
	}

	public String getToken() {
		return getBot().awareness().getSense(Twitter.class).getToken();
	}

	public String getTokenSecret() {
		return getBot().awareness().getSense(Twitter.class).getTokenSecret();
	}

	public boolean getAutoFollow() {
		return getBot().awareness().getSense(Twitter.class).getAutoFollow();
	}

	public boolean getAutoFollowFriendsFriends() {
		return getBot().awareness().getSense(Twitter.class).getAutoFollowFriendsFriends();
	}

	public boolean getAutoFollowFriendsFollowers() {
		return getBot().awareness().getSense(Twitter.class).getAutoFollowFriendsFollowers();
	}

	public int getMaxFriends() {
		return getBot().awareness().getSense(Twitter.class).getMaxFriends();
	}

	public int getMaxStatus() {
		return getBot().awareness().getSense(Twitter.class).getMaxStatus();
	}

	public int getMaxSearch() {
		return getBot().awareness().getSense(Twitter.class).getMaxSearch();
	}

	public boolean getProcessStatus() {
		return getBot().awareness().getSense(Twitter.class).getProcessStatus();
	}

	public boolean getListenStatus() {
		return getBot().awareness().getSense(Twitter.class).getListenStatus();
	}

	public boolean getLearn() {
		return getBot().awareness().getSense(Twitter.class).getLearn();
	}

	public boolean getLearnFromSelf() {
		return getBot().awareness().getSense(Twitter.class).getLearnFromSelf();
	}

	public boolean getTweetChats() {
		return getBot().awareness().getSense(Twitter.class).getTweetChats();
	}

	public boolean getReplyToMentions() {
		return getBot().awareness().getSense(Twitter.class).getReplyToMentions();
	}

	public boolean getIgnoreReplies() {
		return getBot().awareness().getSense(Twitter.class).getIgnoreReplies();
	}

	public boolean getReplyToMessages() {
		return getBot().awareness().getSense(Twitter.class).getReplyToMessages();
	}

	public String getTweetSearch() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getTweetSearch().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public String getWelcomeMessage() {
		return getBot().awareness().getSense(Twitter.class).getWelcomeMessage();
	}

	public boolean getAutoTweet() {
		return getBot().awareness().getSense(Twitter.class).getAutoTweet();
	}

	public int getAutoTweetHours() {
		return getBot().awareness().getSense(Twitter.class).getAutoTweetHours();
	}

	public String getRetweet() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getRetweet().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public String getAutoTweets() {
		List<Vertex> autotweets = getBot().awareness().getSense(Twitter.class).getAutoTweets(getBot().memory().newMemory());
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

	public String getStatusKeywords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getStatusKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public String getAutoFollowKeywords() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getAutoFollowKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public String getAutoFollowSearch() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getAutoFollowSearch().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public String getTweetRSS() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getTweetRSS().iterator();
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
		Iterator<String> iterator = getBot().awareness().getSense(Twitter.class).getRssKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public boolean getFollowMessages() {
		return getBot().awareness().getSense(Twitter.class).getFollowMessages();
	}
	
	public int getFriends() {
		return getBot().awareness().getSense(Twitter.class).getFriendsCount();
	}
	
	public int getFollowers() {
		return getBot().awareness().getSense(Twitter.class).getFollowersCount();
	}
	
	public String getTimeline() {
		StringWriter writer = new StringWriter();
		boolean first = true;
		try {
			for (String tweet : getBot().awareness().getSense(Twitter.class).getTimeline()) {
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
		return getBotBean().getInstance().getEnableTwitter();
	}

	public boolean isAuthorising() {
		return this.authorisationRequest != null;
	}

	public String getAuthorisationURL() {
		return this.authorisationRequest.getAuthorizationURL();
	}

	public void authoriseAccount() throws TwitterException {
		Twitter sense = getBot().awareness().getSense(Twitter.class);
		this.authorisationRequest = sense.authorizeAccount();
	}

	public void authoriseComplete(String pin) throws TwitterException {
		Twitter sense = getBot().awareness().getSense(Twitter.class);
		sense.authorizeComplete(pin.trim());
		this.authorisationRequest = null;
	}

	public void cancelAuthorisation() throws TwitterException {
		this.authorisationRequest = null;
	}

	public void importTweets(String tweetSearch, String maxTweets, boolean tweets, boolean replies) throws TwitterException {
		int max = Site.MAXTWEETIMPORT;
		if (getUser().getType() == UserType.Bronze) {
			max = max * 2;
		} else if (getUser().getType() == UserType.Gold) {
			max = max * 5;
		} else if (getUser().getType() == UserType.Platinum) {
			max = max * 10;
		} else if (getUser().getType() == UserType.Diamond) {
			max = max * 20;
		} else if (getUser().getType() == UserType.Partner) {
			max = max * 20;
		} else if (getUser().getType() == UserType.Admin) {
			max = max * 100;
		}
		int maxValue = Integer.valueOf(maxTweets);
		if (maxValue > max) {
			throw new BotException("You can only import at most " + max + " tweets unless you upgrade your account");
		}
		Twitter sense = getBot().awareness().getSense(Twitter.class);
		sense.learnSearch(tweetSearch, maxValue, tweets, replies);
	}

	public void connect(String userName, String token, String secret) throws TwitterException {
		Twitter sense = getBot().awareness().getSense(Twitter.class);
		sense.setUserName(userName.trim());
		sense.setToken(token.trim());
		sense.setTokenSecret(secret.trim());
		sense.saveProperties(null);
		sense.connect();
		sense.setIsEnabled(true);
		Utils.sleep(100);
		
		sense = getBot().awareness().getSense(TwitterDirectMessaging.class);
		sense.setUserName(userName.trim());
		sense.setToken(token.trim());
		sense.setTokenSecret(secret.trim());
		sense.connect();
		sense.setIsEnabled(true);

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceTwitter(getBotBean().getInstance().getId(), true));
	}

	public void save(boolean autoFollow, boolean autoFollowFriendsFriends, boolean autoFollowFriendsFollowers,
			String autoFollowSearch, String autoFollowKeywords, boolean followMessages, String welcomeMessage, String maxFriends,
			String maxStatus, String statusKeywords, boolean processStatus, boolean listenStatus, boolean learn, boolean learnFromSelf,
			boolean tweetChats, boolean replyToMentions, boolean replyToMessages, String retweet, String tweetRSS, String rssKeywords,
			boolean ignoreReplies, String tweetSearch, String maxSearch, 
			boolean autoTweet, String autoTweetHours, String autoTweets) throws TwitterException {
		
		if (!getBotBean().getInstance().isAdult()
				&& (Utils.checkProfanity(welcomeMessage) || Utils.checkProfanity(autoTweets))) {
			throw BotException.offensive();
		}
		autoFollowSearch = Utils.sanitize(autoFollowSearch);
		autoFollowKeywords = Utils.sanitize(autoFollowKeywords);
		welcomeMessage = Utils.sanitize(welcomeMessage);
		maxFriends = Utils.sanitize(maxFriends);
		retweet = Utils.sanitize(retweet);
		tweetRSS = Utils.sanitize(tweetRSS);
		rssKeywords = Utils.sanitize(rssKeywords);
		tweetSearch = Utils.sanitize(tweetSearch);
		maxSearch = Utils.sanitize(maxSearch);
		autoTweetHours = Utils.sanitize(autoTweetHours);
		//autoTweets = Utils.sanitize(autoTweets); -- templates
		
		Twitter sense = getBot().awareness().getSense(Twitter.class);
		sense.setWelcomeMessage(welcomeMessage.trim());
		sense.setAutoFollow(autoFollow);
		sense.setAutoFollowFriendsFriends(autoFollowFriendsFriends);
		sense.setAutoFollowFriendsFollowers(autoFollowFriendsFollowers);
		TextStream stream = new TextStream(autoFollowSearch.trim());
		sense.setAutoFollowSearch(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getAutoFollowSearch().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		sense.setFollowMessages(followMessages);
		int max = 5;
		try {
			max = Integer.valueOf(maxFriends);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid max friends number - " + maxFriends + " - " + exception.getMessage());
		}
		if ((max < 0) || (max > 1000)) {
			throw new BotException("Max friends must be a number less than 1000.");
		}
		sense.setMaxFriends(max);
		
		max = 5;
		if (Site.COMMERCIAL) {
			try {
				max = Integer.valueOf(maxStatus);
			} catch (NumberFormatException exception) {
				throw new BotException("Max status must be a number less than 50 - " + maxStatus + " - " + exception.getMessage());
			}
			if ((max < 0) || (max > 50)) {
				throw new BotException("Max processed status updates must be a number less than 50.");
			}
		} else {
			try {
				max = Integer.valueOf(maxStatus);
			} catch (NumberFormatException exception) {
				throw new BotException("Max status must be a number less than 20 - " + maxStatus + " - " + exception.getMessage());
			}
			if ((max < 0) || (max > 20)) {
				throw new BotException("Max processed status updates must be a number less than 20 on basic accounts.");
			}
		}

		sense.setMaxStatus(max);
		
		max = 5;
		if (Site.COMMERCIAL) {
			try {
				max = Integer.valueOf(maxSearch);
			} catch (NumberFormatException exception) {
				throw new BotException("Max search must be a number less than 50 - " + maxStatus + " - " + exception.getMessage());
			}
			if ((max < 0) || (max > 50)) {
				throw new BotException("Max processed search tweets must be a number less than 50.");
			}
		} else {
			try {
				max = Integer.valueOf(maxSearch);
			} catch (NumberFormatException exception) {
				throw new BotException("Max search must be a number less than 20 - " + maxStatus + " - " + exception.getMessage());
			}
			if ((max < 0) || (max > 20)) {
				throw new BotException("Max processed search tweets must be a number less than 20 on basic accounts.");
			}
		}
		sense.setMaxSearch(max);

		sense.setProcessStatus(processStatus);
		sense.setListenStatus(listenStatus);
		sense.setLearn(learn);
		sense.setLearnFromSelf(learnFromSelf);
		sense.setIgnoreReplies(ignoreReplies);
		statusKeywords = statusKeywords.trim();
		if (processStatus && statusKeywords.isEmpty()) {
			//throw new BotException("Status keywords are required to process friends status updates.");
		}
		
		statusKeywords.replace(",", "\n");
		stream = new TextStream(statusKeywords);
		sense.setStatusKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getStatusKeywords().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		
		autoFollowKeywords.replace(",", "\n");
		stream = new TextStream(autoFollowKeywords.trim());
		sense.setAutoFollowKeywords(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getAutoFollowKeywords().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		sense.setTweetChats(tweetChats);
		sense.setReplyToMentions(replyToMentions);
		sense.setReplyToMessages(replyToMessages);
		
		retweet.replace(",", "\n");
		stream = new TextStream(retweet.trim());
		sense.setRetweet(new ArrayList<String>());
		while (!stream.atEnd()) {
			String keywords = stream.upToAny("\n").trim();
			if (!keywords.isEmpty()) {
				sense.getRetweet().add(keywords);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		
		stream = new TextStream(tweetRSS.trim());
		sense.setTweetRSS(new ArrayList<String>());
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
				sense.getTweetRSS().add(rss);
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
		
		tweetSearch.replace(",", "\n");
		stream = new TextStream(tweetSearch.trim());
		sense.setTweetSearch(new ArrayList<String>());
		while (!stream.atEnd()) {
			String search = stream.upToAny("\n").trim();
			if (!search.isEmpty()) {
				sense.getTweetSearch().add(search);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		sense.setAutoTweet(autoTweet);
		int hours = 0;
		try {
			hours = Integer.valueOf(autoTweetHours);
		} catch (NumberFormatException exception) {
			throw new BotException("Invalid auto tweet hours number - " + autoTweetHours + " - " + exception.getMessage());
		}
		sense.setAutoTweetHours(hours);
		
		stream = new TextStream(autoTweets.trim());
		List<String> tweets = new ArrayList<String>();
		while (!stream.atEnd()) {
			String tweet = stream.upToAny("\n").trim();
			if (!tweet.isEmpty()) {
				tweets.add(tweet);
			}
			stream.skip();
			stream.skipWhitespace();
		}
		sense.saveProperties(tweets);
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
		this.authorisationRequest = null;
	}

	public void checkStatus() {
		Twitter twitter = getBot().awareness().getSense(Twitter.class);
		twitter.checkProfile();
		if (twitter.getReplyToMessages()) {
			twitter = getBot().awareness().getSense(TwitterDirectMessaging.class);
			twitter.checkProfile();
		}
	}

	public void disable() {
		getBot().awareness().getSense(Twitter.class).setIsEnabled(false);
		getBot().awareness().getSense(TwitterDirectMessaging.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceTwitter(getBotBean().getInstance().getId(), false));
	}

	public void addFriend(String friend) {
		getBot().awareness().getSense(Twitter.class).addFriend(friend);
	}

	public void removeFriend(String friend) {
		getBot().awareness().getSense(Twitter.class).removeFriend(friend);
	}
}
