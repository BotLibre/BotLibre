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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.sense.chat.IRC;
import org.botlibre.sense.twitter.Twitter;
import org.botlibre.thought.language.Language.LanguageState;

public class IRCBean extends ServletBean {
	public static Map<String, String> rooms = new ConcurrentHashMap<String, String>();
	
	public IRCBean() {
	}

	public String getServer() {
		return getBot().awareness().getSense(IRC.class).getServerName();
	}

	public String getChannel() {
		return getBot().awareness().getSense(IRC.class).getChannelName();
	}

	public String getNick() {
		return getBot().awareness().getSense(IRC.class).getNick();
	}

	public boolean isConnected() {
		return getBot().awareness().getSense(IRC.class).isConnected();
	}

	public void connect(String server, String channel, String nick, boolean listen) {
		String key = server + channel;
		if (rooms.containsKey(key)) {
			throw new BotException("A bot is already listening on that channel.  Only one bot is allowed per channel.");
		}
		Bot bot = getBot();
		IRC irc = bot.awareness().getSense(IRC.class);
		String port = null;
		int index = server.indexOf(':');
		if (index != -1 && (index < server.length())) {
			port = server.substring(index + 1, server.length());
			server = server.substring(0, index);
			try {
				int value = Integer.valueOf(port);
				irc.setPort(value);
			} catch (Exception ignore) {}
		}
		irc.setServerName(server.trim());
		irc.setChannelName(channel.trim());
		irc.setNick(nick.trim());
		irc.setNickAlt(nick + "_");
		irc.setUserName(nick.trim());
		irc.setRealName(nick.trim());
		if (listen) {
			irc.setLanguageState(LanguageState.Listening);
		}
		irc.connect();
		if (getBotBean().getInstance().getEnableTwitter()) {
			Twitter twitter = bot.awareness().getSense(Twitter.class);
			if (twitter != null) {
				twitter.outputTweet("Chatting on IRC server: \"" + server + "\" in channel: " + channel);
			}
		}
		rooms.put(server + channel, getBotBean().getInstance().getName());
	}

	public void disconnectChat() {
		Bot bot = getBot();
		IRC irc = bot.awareness().getSense(IRC.class);
		String key = irc.getServerName() + irc.getChannelName();
		rooms.remove(key);
		getBot().awareness().getSense(IRC.class).disconnect();
	}
}
