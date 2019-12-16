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

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.http.Http;
import org.botlibre.sense.telegram.Telegram;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;

public class TelegramBean extends ServletBean {
	
	public TelegramBean() {
	}

	public String getUserName() {
		return getBot().awareness().getSense(Telegram.class).getUserName();
	}

	public String getProfileName() {
		return getBot().awareness().getSense(Telegram.class).getProfileName();
	}
	
	public String getWebhook() {
		if (!Site.HTTPS) {
			return "";
		}
		if (!getRealtimeMessages()) {
			return "";
		}
		String hook = Site.SECUREURLLINK + "/rest/api/telegram/";
		if (getUser().getApplicationId() == null) {
			getLoginBean().setUser(AdminDatabase.instance().resetAppId(getUser().getUserId()));
		}
		hook = hook + getUser().getApplicationId();
		hook = hook + "/" + getBotBean().getInstanceId();
		return hook;
	}

	public String getToken() {
		return getBot().awareness().getSense(Telegram.class).getToken();
	}

	public String getChannel() {
		return getBot().awareness().getSense(Telegram.class).getChannel();
	}

	public String getGroupModeCheckedString(LanguageState value) {
		Bot bot = getBot();
		if (bot == null) {
			return "";
		}
		LanguageState mode = bot.awareness().getSense(Telegram.class).getGroupMode();
		if (mode == null) {
			return "";
		}
		if (mode.equals(value)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public boolean getCheckMessages() {
		return getBot().awareness().getSense(Telegram.class).getCheckMessages();
	}

	public boolean getRealtimeMessages() {
		return getBot().awareness().getSense(Telegram.class).getRealtimeMessages();
	}

	public boolean getAutoPost() {
		return getBot().awareness().getSense(Telegram.class).getAutoPost();
	}

	public int getAutoPostHours() {
		return getBot().awareness().getSense(Telegram.class).getAutoPostHours();
	}

	public boolean getStripButtonText() {
		return getBot().awareness().getSense(Telegram.class).getStripButtonText();
	}

	public boolean getTrackMessageObjects() {
		return getBot().awareness().getSense(Telegram.class).getTrackMessageObjects();
	}

	public String getAutoPosts() {
		List<Vertex> autotweets = getBot().awareness().getSense(Telegram.class).getAutoPosts(getBot().memory().newMemory());
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
		Iterator<String> iterator = getBot().awareness().getSense(Telegram.class).getPostRSS().iterator();
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
		Iterator<String> iterator = getBot().awareness().getSense(Telegram.class).getRssKeywords().iterator();
		while (iterator.hasNext()) {
			writer.write(iterator.next());
			if (iterator.hasNext()) {
				writer.write("\n");				
			}
		}
		return writer.toString();
	}

	public boolean isConnected() {
		return getBotBean().getInstance().getEnableTelegram();
	}

	public void connect(String userName, String token) throws Exception {
		userName = Utils.sanitize(userName);
		token = Utils.sanitize(token);
		Telegram sense = getBot().awareness().getSense(Telegram.class);
		sense.setUserName(userName.trim());
		sense.setToken(token.trim());
		sense.saveProperties(null);
		sense.connect(getWebhook());
		sense.setIsEnabled(true);
		Utils.sleep(100);

		getBotBean().setInstance(AdminDatabase.instance().updateInstanceTelegram(getBotBean().getInstance().getId(), true));
	}

	public void save(String channel, String postRSS, String rssKeywords,
			boolean autoPost, String autoPostHours, String autoPosts,
			boolean checkMessages, boolean realtimeMessages, boolean stripButtonText, boolean trackMessageObjects, String groupMode) throws Exception {

		channel = Utils.sanitize(channel);
		postRSS = Utils.sanitize(postRSS);
		rssKeywords = Utils.sanitize(rssKeywords);
		autoPostHours = Utils.sanitize(autoPostHours);
		//autoPosts = Utils.sanitize(autoPosts); -- templates
		groupMode = Utils.sanitize(groupMode);
		if (!getBotBean().getInstance().isAdult() && Utils.checkProfanity(autoPosts)) {
			throw BotException.offensive();
		}
		Telegram sense = getBot().awareness().getSense(Telegram.class);

		sense.setChannel(channel.trim());
		sense.setCheckMessages(checkMessages);
		if (realtimeMessages != sense.getRealtimeMessages()) {
			// Reset webhook.
			sense.setRealtimeMessages(realtimeMessages);
			sense.connect(getWebhook());
		}
		sense.setStripButtonText(stripButtonText);
		sense.setTrackMessageObjects(trackMessageObjects);
		if (groupMode != null && !groupMode.isEmpty()) {
			sense.setGroupMode(LanguageState.valueOf(groupMode));
		}
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

	@Override
	public void disconnectInstance() {
		disconnect();
	}
	
	@Override
	public void disconnect() {
	}

	public void checkStatus() {
		getBot().setDebugLevel(Level.FINE);
		Telegram telegram = getBot().awareness().getSense(Telegram.class);
		telegram.checkProfile();
	}

	public void disable() throws Exception {
		getBotBean().setInstance(AdminDatabase.instance().updateInstanceTelegram(getBotBean().getInstance().getId(), false));
		
		Telegram sense = getBot().awareness().getSense(Telegram.class);
		sense.setIsEnabled(false);
		sense.connect("");
	}
}
