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
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.slack.Slack;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class SlackBean extends ServletBean {

	public SlackBean() {
	}
	
	public String getToken() {
		return getBot().awareness().getSense(Slack.class).getToken();
	}
	
	public String getBotUsername() {
		return getBot().awareness().getSense(Slack.class).getBotUsername();
	}
	
	public String getRssUsername() {
		return getBot().awareness().getSense(Slack.class).getRssUsername();
	}
	
	public String getAutoPostUsername() {
		return getBot().awareness().getSense(Slack.class).getAutoPostUsername();
	}
	
	public String getRssChannel() {
		return getBot().awareness().getSense(Slack.class).getRssChannel();
	}
	
	public String getAutoPostChannel() {
		return getBot().awareness().getSense(Slack.class).getAutoPostChannel();
	}
	
	public String getIncomingWebhook() {
		return getBot().awareness().getSense(Slack.class).getIncomingWebhook();
	}
	
	public boolean getAutoPost() {
		return getBot().awareness().getSense(Slack.class).getAutoPost();
	}

	public int getAutoPostHours() {
		return getBot().awareness().getSense(Slack.class).getAutoPostHours();
	}
	
	public String getAutoPosts() {
		List<Vertex> autotweets = getBot().awareness().getSense(Slack.class).getAutoPosts(getBot().memory().newMemory());
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
	
	public String getPostRSS() {
		StringWriter writer = new StringWriter();
		Iterator<String> iterator = getBot().awareness().getSense(Slack.class).getPostRSS().iterator();
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
		Iterator<String> iterator = getBot().awareness().getSense(Slack.class).getRssKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");
			}
		}
		return writer.toString();
	}
	
	public void connect() throws Exception {

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceSlack(getBotBean().getInstance().getId(), true));
	}
	
	public void save(String token, String botUsername, String incomingWebhook, String postRSS, String rssKeywords,
			boolean autoPost, String autoPostHours, String autoPosts, String rssUsername, String rssChannel, String autoPostUsername, String autoPostChannel) {
		
		token = Utils.sanitize(token);
		botUsername = Utils.sanitize(botUsername);
		incomingWebhook = Utils.sanitize(incomingWebhook);
		postRSS = Utils.sanitize(postRSS);
		rssKeywords = Utils.sanitize(rssKeywords);
		//autoPosts = Utils.sanitize(autoPosts); -- templates
		rssUsername = Utils.sanitize(rssUsername);
		rssChannel = Utils.sanitize(rssChannel);
		autoPostUsername = Utils.sanitize(autoPostUsername);
		autoPostChannel = Utils.sanitize(autoPostChannel);
		if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(autoPosts)) {
			throw BotException.offensive();
		}
		Slack sense = getBot().awareness().getSense(Slack.class);
		sense.setToken(token.trim());
		sense.setBotUsername(botUsername.trim());
		sense.setIncomingWebhook(incomingWebhook.trim());
		sense.setRssUsername(rssUsername.trim());
		sense.setRssChannel(rssChannel.trim());
		sense.setAutoPostUsername(autoPostUsername.trim());
		sense.setAutoPostChannel(autoPostChannel.trim());
		
		TextStream stream = new TextStream(postRSS.trim());
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
	}
	
	public String getWebhook() {
		String hook = null;
		if (Site.HTTPS) {
			hook = Site.SECUREURLLINK + "/rest/api/slack/";
		} else {
			hook = Site.URLLINK + "/rest/api/slack/";
		}
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}
	
	public void clear() {
		Slack sense = getBot().awareness().getSense(Slack.class);
		sense.setToken("");
		sense.setBotUsername("");
		sense.setIncomingWebhook("");
		sense.setAutoPost(false);
		sense.setAutoPostChannel("");
		sense.setAutoPostHours(0);
		sense.setAutoPostUsername("");
		sense.setPostRSS(null);
		sense.setRssChannel("");
		sense.setRssKeywords(null);
		sense.setRssUsername("");
		sense.saveProperties();
	}
	
	@Override
	public void disconnectInstance() {
		disconnect();
	}

	@Override
	public void disconnect() {
	}
	
	public void checkStatus() {
		getBot().setDebugLevel(Level.FINE);
		Slack slack = getBot().awareness().getSense(Slack.class);
		slack.checkProfile();
	}

	public void disable() {
		getBot().awareness().getSense(Slack.class).setIsEnabled(false);
		
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceSlack(getBotBean().getInstance().getId(), false));
	}
}
