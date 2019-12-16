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
package org.botlibre.web.socket;

import java.io.IOException;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.chat.Chat;
import org.botlibre.sense.chat.ChatEvent;
import org.botlibre.sense.chat.ChatListener;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.BotMode;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.chat.ChatMessage;
import org.botlibre.web.chat.ChatRoom;
import org.botlibre.web.service.BotStats;
import org.botlibre.web.service.Stats;

public class ChatBotEndpoint extends ChatEndpoint implements ChatListener {
	
	protected BotInstance bot;
	protected LoginBean loginBean;
	protected boolean isPrivate;
	protected ChatEndpoint privateConnection;
	
	/** Tracks time when a message is begun to be processed for stats. */
	public long messageStartTime;

	public ChatBotEndpoint(BotInstance bot, ChatRoom room) {
		this.bot = bot;
		this.nick = bot.getName();
		this.roomId = room.getId();
	}
	
	public void startStats(Chat sense) {
		this.messageStartTime = System.currentTimeMillis();
		Language language = sense.getBot().mind().getThought(Language.class);
		sense.conversations = 0;
		sense.engaged = 0;
		language.defaultResponses = 0;
		language.confidence = 0;
		language.sentiment = 0;
		BotStats botstats = BotStats.getStats(getBot().getId(), getBot().getName());
		botstats.livechats++;
	}
	
	public void endStats(Chat sense) {
		Language language = sense.getBot().mind().getThought(Language.class);
		BotStats stats = BotStats.getStats(this.loginBean.getBotBean().getInstanceId(), this.loginBean.getBotBean().getInstanceName());
		stats.conversations = stats.conversations + sense.conversations;
		stats.engaged = stats.engaged + sense.engaged;
		stats.defaultResponses = stats.defaultResponses + language.defaultResponses;
		stats.confidence = stats.confidence + language.confidence;
		stats.sentiment = stats.sentiment + language.sentiment;
		stats.messages++;
		Stats.stats.botMessages++;
		Stats.stats.botConversations = Stats.stats.botConversations + sense.conversations;
		sense.conversations = 0;
		sense.engaged = 0;
		language.defaultResponses = 0;
		language.confidence = 0;
		language.sentiment = 0;
		if (this.messageStartTime > 0) {
			long time = System.currentTimeMillis() - this.messageStartTime;
			Stats.stats.botChatTotalResponseTime = Stats.stats.botChatTotalResponseTime + time;
			stats.chatTotalResponseTime = stats.chatTotalResponseTime + time;
		}
	}

	@Override
	public void sendMessage(ChatEvent message) {
		ChatRoom room = getChatRoom();
		if (room == null) {
			return;
		}
		String bot = this.loginBean.getBotBean().getInstanceName();
		ChatEndpoint connection = room.getConnectionNoLock(message.getNick());
		if (connection == null) {
			return;
		}
		ChatEndpoint currentPrivate = room.getPrivate(connection);
		if (currentPrivate != null && currentPrivate != this && !message.isWhisper()) {
			return;
		}
		Chat sense = getChat(room);
		if (sense == null) {
			return;
		}
		endStats(sense);
			
		ChatMessage chat = new ChatMessage();
		chat.setChannel(room.getChannel());
		chat.setDomain(room.getChannel().getDomain());
		chat.setNick(getNick());
		chat.setMessage(message.getMessage());
		chat.setPrivate(isPrivate());
		chat.setWhisper(message.isWhisper());
		if (isPrivate()) {
			chat.setTarget(connection.getUser());
			chat.setTargetNick(connection.getNick());
		}
		AdminDatabase.instance().createChatMessage(chat);
		String response = bot + ": ";
		if (room.isChatRoom() && !isPrivate() && !message.isWhisper()) {
			if (room.getConnections().size() <= 2) {
				response = response + message.getMessage();
			} else {
				response = response + message.getNick() + ": " + message.getMessage();
			}
			room.broadcastMessage(response, false);
			room.broadcastToSpys("spy: " + response, this);
		} else {
			if (message.isWhisper()) {
				response = response + "whisper: ";
			}
			response = response + message.getMessage();
			response = Utils.sanitize(response);
			connection.addHistory(response);
			connection.sendTextIgnoreError(response);
			room.broadcastToSpys("spy: " + response, this);
		}
	}

	public ChatEndpoint getPrivateConnection() {
		return privateConnection;
	}

	public void setPrivateConnection(ChatEndpoint privateConnection) {
		this.privateConnection = privateConnection;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public ChatBotEndpoint createPrivate(ChatEndpoint connection, ChatRoom room) {
		ChatBotEndpoint clone = new ChatBotEndpoint(this.bot, room);
		clone.setPrivate(true);
		clone.setPrivateConnection(connection);
		LoginBean bean = new LoginBean();
		clone.setLoginBean(bean);
		bean.setUser(room.getLoginBean().getUser());
		bean.setLoggedIn(true);
		bean.getBotBean().setInstance(this.bot);
		bean.getBotBean().connect(ClientType.WEB);
			if (bean.getError() != null) {
				return null;
			}
			Bot instance = bean.getBotBean().getBot();
			Chat sense = instance.awareness().getSense(Chat.class);
			if (sense == null) {
			throw new BotException("Connection to bot failed");
		}
		sense.setChatListener(this);
		sense.setNick(bean.getBotBean().getInstanceName());
		sense.setNickAlt(bean.getBotBean().getInstanceName() + "1");
		sense.addUser(connection.getNick());
		sense.addUser(sense.getNick());
		
		return clone;
	}

	public Chat getChat(ChatRoom room) {
		this.loginBean.getBotBean().connect(ClientType.WEB);
		if (loginBean.getError() != null) {
			return null;
		}
		Bot instance = this.loginBean.getBotBean().getBot();
		Chat sense = instance.awareness().getSense(Chat.class);
		if (sense.getChatListener() != this) {
			sense.initialize();
			if (room.getChannel().getBotMode() == BotMode.ListenOnly) {
				sense.setLanguageState(LanguageState.ListeningOnly);
			} else {
				sense.setLanguageState(LanguageState.Discussion);
			}
			sense.setChatListener(this);
			sense.setNick(this.loginBean.getBotBean().getInstanceName());
			sense.setNickAlt(this.loginBean.getBotBean().getInstanceName() + "1");
			resetUsers(room);
		}
		return sense;
	}

	public void resetUsers(ChatRoom room) {
		Bot instance = this.loginBean.getBotBean().getBot();
		if (instance == null) {
			return;
		}
		Chat sense = instance.awareness().getSense(Chat.class);
		sense.getUsers().clear();
		Network memory = instance.memory().newMemory();
		if (this.isPrivate) {
			sense.addUser(this.privateConnection.getNick());
			Vertex speaker = memory.createSpeaker(this.privateConnection.getNick());
			if (room.getChannel().isAdmin(this.privateConnection.getUser())) {
				speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
			}
			if (room.getChannel().isOperator(this.privateConnection.getUser())) {
				speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
			}
		} else {
			for (ChatEndpoint client : room.getConnections()) {
				sense.addUser(client.getNick());
				Vertex speaker = memory.createSpeaker(client.getNick());
				if (room.getChannel().isAdmin(client.getUser())) {
					speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
				}
				if (room.getChannel().isOperator(client.getUser())) {
					speaker.addRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
				}
			}
		}
		memory.save();
		sense.addUser(sense.getNick());
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public String getNickId() {
		return "#" + bot.getId();
	}

	public BotInstance getBot() {
		return bot;
	}

	public void setBot(BotInstance bot) {
		this.bot = bot;
	}

	@Override
	public void sendText(String message) throws IOException {
	
	}
	
	@Override
	public void sendTextIgnoreError(String message) {
		
	}
	
	@Override
	public void close() {
		if (this.loginBean.getBotBean().isConnected()) {
				Bot instance = this.loginBean.getBotBean().getBot();
				Chat sense = instance.awareness().getSense(Chat.class);
				sense.disconnect();
			this.loginBean.getBotBean().disconnect();
		}
	}
	
	@Override
	public boolean isBot() {
		return true;
	}
}
