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
package org.botlibre.web.chat;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.BotException;
import org.botlibre.sense.chat.Chat;
import org.botlibre.sense.chat.ChatEvent;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.BotMode;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.LiveChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.chat.ChatChannel.ChannelType;
import org.botlibre.web.socket.ChatBotEndpoint;
import org.botlibre.web.socket.ChatEndpoint;
import org.botlibre.web.service.LiveChatStats;
import org.botlibre.web.service.Stats;

public class ChatRoom {
	public static int MAX_HISTORY = 50;

	protected long id;
	protected long nextId;
	protected ChatChannel channel;
	protected LoginBean loginBean;
	protected List<ChatEndpoint> connections = new ArrayList<ChatEndpoint>();
	protected List<ChatEndpoint> userConnections = new ArrayList<ChatEndpoint>();
	protected List<ChatBotEndpoint> botConnections = new ArrayList<ChatBotEndpoint>();
	protected List<ChatEndpoint> adminConnections = new ArrayList<ChatEndpoint>();
	protected Map<ChatEndpoint, ChatEndpoint> spyConnections = new HashMap<ChatEndpoint, ChatEndpoint>();
	protected Map<ChatEndpoint, ChatEndpoint> privateConnections = new HashMap<ChatEndpoint, ChatEndpoint>();
	protected Map<ChatEndpoint, Long> privateTokens = new HashMap<ChatEndpoint, Long>();
	protected Map<ChatEndpoint, ChatEndpoint> privateRequests = new HashMap<ChatEndpoint, ChatEndpoint>();
	protected Map<String, ChatEndpoint> nicks = new HashMap<String, ChatEndpoint>();
	protected LinkedList<String> history = new LinkedList<String>();
	protected long token = Math.abs(Utils.random().nextLong());
	
	public ChatRoom(ChatChannel channel) {
		this.channel = channel;
	}
	
	public void initialize(LoginBean loginBean) {
		this.loginBean = loginBean;
		if (this.channel.hasBot()) {
			loginBean.getBotBean().setInstance(this.channel.getBot());
			ChatBotEndpoint botEndpoint = new ChatBotEndpoint(this.channel.getBot(), this);
			botEndpoint.setLoginBean(this.loginBean);
			addConnection(botEndpoint);
		}
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public List<ChatEndpoint> getUserConnections() {
		return userConnections;
	}

	public void setUserConnections(List<ChatEndpoint> userConnections) {
		this.userConnections = userConnections;
	}

	public List<ChatBotEndpoint> getBotConnections() {
		return botConnections;
	}

	public void setBotConnections(List<ChatBotEndpoint> botConnections) {
		this.botConnections = botConnections;
	}

	public List<ChatEndpoint> getAdminConnections() {
		return adminConnections;
	}

	public void setAdminConnections(List<ChatEndpoint> adminConnections) {
		this.adminConnections = adminConnections;
	}

	public Map<ChatEndpoint, ChatEndpoint> getPrivateConnections() {
		return privateConnections;
	}

	public void setPrivateConnections(
			Map<ChatEndpoint, ChatEndpoint> privateConnections) {
		this.privateConnections = privateConnections;
	}

	public Map<ChatEndpoint, ChatEndpoint> getPrivateRequests() {
		return privateRequests;
	}

	public void setPrivateRequests(Map<ChatEndpoint, ChatEndpoint> privateRequests) {
		this.privateRequests = privateRequests;
	}

	public Map<String, ChatEndpoint> getNicks() {
		return nicks;
	}

	public void setNicks(Map<String, ChatEndpoint> nicks) {
		this.nicks = nicks;
	}

	public LinkedList<String> getHistory() {
		return history;
	}

	public void setHistory(LinkedList<String> history) {
		this.history = history;
	}

	public synchronized long nextId() {
		return nextId++;
	}

	public ChatChannel getChannel() {
		return channel;
	}

	public void setChannel(ChatChannel channel) {
		this.channel = channel;
	}
	public void clearOnlineList(ChatEndpoint connection) {
		connection.sendTextIgnoreError("Online: ");
		connection.sendTextIgnoreError("Online-xml: <table></table>");
	}

	public void message(ChatEndpoint connection, String message) {
		try {
			if (message.startsWith("Media:")) {
				if (isInPrivate(connection)) {
					ChatEndpoint client = getPrivate(connection);
					if (client != null) {
						client.sendTextIgnoreError(message);
					}
				} else {
					broadcast(message, false);
				}
				return;
			}
			log(Level.INFO, "message", message, connection.getNick());
			message = message.trim();
			if (message.equalsIgnoreCase("ping")) {
				connection.sendTextIgnoreError("Info: pong");
				return;
			}
			if (message.equalsIgnoreCase("exit")) {
				if (isInPrivate(connection)) {
					removePrivate(connection, false);
				} else {
					end(connection);
					connection.sendTextIgnoreError("Info: You have exited.");
					clearOnlineList(connection);
					connection.close();
				}
				return;
			}
			if (message.equalsIgnoreCase("accept")) {
				if (isInPrivate(connection)) {
					connection.sendTextIgnoreError("Error: You are already in a private channel, exit first.");
					return;
				}
				if (isOneOnOne() && !isAdmin(connection)) {
					if (this.channel.hasBot()) {
						for (ChatBotEndpoint bot : this.botConnections) {
							addPrivate(connection, bot);
							return;
						}
					}
					connection.sendTextIgnoreError("Error: No operators currently available, you will be connected to the next available operator.");
					return;
				}
				ChatEndpoint client = this.privateRequests.remove(connection);
				if (client == null) {
					if (isOneOnOne()) {
						client = nextWaitingConnection();
						if (client != null) {
							addPrivate(connection, client);
						} else {
							connection.sendTextIgnoreError("Error: No users waiting.");
						}
					} else {
						connection.sendTextIgnoreError("Error: Missing private request.");
					}
				} else {
					addPrivate(connection, client);
				}
				return;
			}
			int index = message.indexOf(':');
			String command = "";
			if (index != -1) {
				command = message.substring(0, index);
			}
			if (command.equalsIgnoreCase("mode")) {
				if (!isAdmin(connection)) {
					connection.sendTextIgnoreError("Error: Must be admin to change mode.");					
				} else {
					if (message.length() < (index + 2)) {
						connection.sendTextIgnoreError("Error: Invalid mode.");
						return;
					}
					String mode = message.substring(index + 2, message.length());
					if (mode.equals("spy")) {
						addSpy(connection);
						connection.sendTextIgnoreError("Info: Changed mode to spy.");
					} else if (mode.equals("normal")) {
						removeSpy(connection);
						connection.sendTextIgnoreError("Info: Changed mode to normal.");
					} else {
						connection.sendTextIgnoreError("Error: Invalid mode.");					
					}
				}
				return;
			}
			ChatMessage chat = new ChatMessage();
			chat.setChannel(this.channel);
			chat.setDomain(this.channel.getDomain());
			chat.setCreator(connection.getUser());
			chat.setNick(connection.getNick());
			chat.setMessage(message);
			boolean isPrivate = isInPrivate(connection);
			chat.setPrivate(isPrivate);
			if (isPrivate) {
				chat.setTarget(getPrivate(connection).getUser());
				chat.setTargetNick(getPrivate(connection).getNick());
			}
			broadcastToSpys("spy: " + connection.getNick() + ": " + message, connection);
			Stats.stats.chatMessages++;
			LiveChatStats stats = LiveChatStats.getStats(this.channel.getId(), this.channel.getName());
			stats.messages++;
			AdminDatabase.instance().createChatMessage(chat);
			if (index != -1) {
				if (command.equalsIgnoreCase("boot")) {
					if (!isAdmin(connection)) {
						connection.sendTextIgnoreError("Error: Must be admin to boot.");
					} else {
						if (message.length() < (index + 2)) {
							connection.sendTextIgnoreError("Error: Missing user.");
							return;
						}
						String user = message.substring(index + 2, message.length());
						if (!bootUser(user)) {
							connection.sendTextIgnoreError("Error: Invalid user " + user + ".");
						}
					}
					return;
				}
				if (command.equalsIgnoreCase("flag")) {
					if (message.length() < (index + 2)) {
						connection.sendTextIgnoreError("Error: Missing user.");
						return;
					}
					String remaining = message.substring(index + 2, message.length());
					index = remaining.indexOf(':');
					if (index == -1) {
						connection.sendTextIgnoreError("Error: Invalid flag, syntax is 'flag: user: reason'.");
					} else {
						String user = remaining.substring(0, index);
						if (remaining.length() < (index + 2)) {
							connection.sendTextIgnoreError("Error: Missing reason.");
							return;
						}
						String reason = remaining.substring(index + 2, remaining.length());
						if (connection.getNick().contains("anonymous")) {
							connection.sendTextIgnoreError("Error: You must be logged in to flag a user.");
						} else {
							try {
								if (!flagUser(connection, user, reason)) {
									connection.sendTextIgnoreError("Error: Invalid user " + user + ".");
								}
							} catch (Exception error) {
								connection.sendTextIgnoreError("Error: " + error.getMessage());
							}
						}
					}
					return;
				}
				if (command.equalsIgnoreCase("private") || command.equalsIgnoreCase("pvt")) {
					if (isOneOnOne() && !isAdmin(connection)) {
						connection.sendTextIgnoreError("Error: Must be admin.");
						return;
					}
					if (message.length() < (command.length() + 2)) {
						connection.sendTextIgnoreError("Error: Missing user.");
						return;
					}
					String user = message.substring(index + 2, message.length());
					privateUser(connection, user);
					return;
				}
				if (command.equalsIgnoreCase("whisper") || command.equalsIgnoreCase("w") || command.equalsIgnoreCase("whipser")) {
					if (message.length() < (command.length() + 2)) {
						connection.sendTextIgnoreError("Error: Missing user.");
						return;
					}
					String remaining = message.substring(index + 2, message.length());
					index = remaining.indexOf(':');
					if (index == -1) {
						connection.sendTextIgnoreError("Error: Invalid whisper, syntax is 'whisper: user: message'.");
					} else {
						String user = remaining.substring(0, index);
						if (remaining.length() < (index + 2)) {
							connection.sendTextIgnoreError("Error: Missing message.");
							return;
						}
						String whisper = remaining.substring(index + 2, remaining.length());
						if (!whisperUser(connection, user, whisper)) {
							connection.sendTextIgnoreError("Error: Invalid user " + user + ".");
						}
					}
					return;
				}
				if (command.equalsIgnoreCase("email")) {
					if (!isInPrivate(connection)) {
						connection.sendTextIgnoreError("Error: Can only email private conversation");
						return;
					}
					String email = message.substring(index + 2, message.length());
					if (!email.contains("@") || !email.contains(".")) {
						connection.sendTextIgnoreError("Error: Invalid email address");
						return;
					}
					if (emailChatLog(connection, email)) {
						connection.sendTextIgnoreError("Info: Email sent");
					}
					return;
				}
			}
			String filteredMessage = String.format("%s: %s", connection.getNick(), message);
			if (isInPrivate(connection)) {
				filteredMessage = Utils.sanitize(filteredMessage);
				connection.addHistory(filteredMessage);
				getPrivate(connection).addHistory(filteredMessage);
				connection.sendTextIgnoreError(filteredMessage);
				ChatEndpoint client = getPrivate(connection);
				if (client.isBot()) {
					ChatBotEndpoint bot = (ChatBotEndpoint)client;
					Chat sense = bot.getChat(this);
					if (sense == null) {
						throw new BotException("Connection to bot failed");
					}
					bot.startStats(sense);
					ChatEvent chatMessage = new ChatEvent();
					chatMessage.setNick(connection.getNick());
					chatMessage.setMessage(message);
					sense.input(chatMessage);
				} else {
					if (this.channel.getBotMode() != BotMode.AnswerOnly) {
						for (ChatBotEndpoint bot : this.botConnections) {
							Chat sense = bot.getChat(this);
							if (sense != null) {
								bot.startStats(sense);
								ChatEvent chatMessage = new ChatEvent();
								chatMessage.setNick(connection.getNick());
								chatMessage.setMessage(client.getNick() + ": " + message);
								sense.input(chatMessage);
							}
						}
					}
					try {
						client.sendText(filteredMessage);
					} catch (Exception exception) {
						log(Level.WARNING, exception.toString());
						end(client);
						client.close();
						return;
					}
				}
			} else if (isChatRoom()) {
				broadcastMessage(filteredMessage, false);
				if (this.channel.hasBot()) {
					for (ChatBotEndpoint bot : this.botConnections) {
						Chat sense = bot.getChat(this);
						if (sense == null) {
							throw new BotException("Connection to bot failed");
						}
						if (this.channel.getBotMode() == BotMode.AnswerOnly) {
							String nick = bot.getNick();
							if ((message.length() < nick.length()) || message.substring(0, nick.length()).equals(nick)) {
								continue;
							}
						}
						bot.startStats(sense);
						ChatEvent chatMessage = new ChatEvent();
						chatMessage.setNick(connection.getNick());
						chatMessage.setMessage(message);
						sense.input(chatMessage);
					}
				}
			} else {
				broadcastMessage(filteredMessage, true);
			}
		} catch (Exception exception) {
			log(exception);
			connection.sendTextIgnoreError(String.format("Error: %s", exception.getMessage()));
			return;
		}
	}
	
	public synchronized ChatEndpoint getPrivate(ChatEndpoint connection) {
		return this.privateConnections.get(connection);
	}

	public void broadcastOnlineList(ChatEndpoint connection, ChatEndpoint client) {
		StringWriter writer = new StringWriter();
		writer.write("Online: ");  
		writer.write(connection.getNickId());
		writer.write(", ");
		writer.write(client.getNickId());
		String message = writer.toString();
		connection.sendTextIgnoreError(message);
		client.sendTextIgnoreError(message);
	}

	public void writeUserHTML(StringWriter writer, ChatEndpoint client) {
		if (client.isBot()) {
			writeBotHTML(writer, (ChatBotEndpoint)client);
			return;
		}
		writer.write("<div class='online-user' id='user-");
		writer.write(LiveChatBean.encode(client.getNick()));
		writer.write("'>");
		if (client.getUser() != null) {
			//writer.write("<a class='user' target='_blank' href='login?view-user=");
			//writer.write(this.loginBean.encodeURI(client.getUser().getUserId()));
			//writer.write("'>");
			writer.write("<img src='");
			if (Site.HTTPS) {
				writer.write(Site.SECUREURLLINK);
			} else {
				writer.write(Site.URLLINK);
			}
			writer.write("/");
			writer.write(this.loginBean.getAvatarThumb(client.getUser()));
			writer.write("' class='chat-user-thumb'/>");
			//writer.write("</a>");
			writer.write("<div class='online-user-label'>");
			writer.write("<a class='user' target='_blank' href='login?view-user=");
			writer.write(this.loginBean.encodeURI(client.getUser().getUserId()));
			writer.write("'>");
			writer.write(client.getUser().getUserHTML());
			writer.write("</a>");
			writer.write("</div>");
		} else {
			writer.write("<img src='");
			if (Site.HTTPS) {
				writer.write(Site.SECUREURLLINK);
			} else {
				writer.write(Site.URLLINK);
			}
			writer.write("/");
			writer.write(this.loginBean.getAvatarThumb(client.getUser()));
			writer.write("' width='50'/>");
			writer.write("<div class='online-user-label'>");
			writer.write(client.getNick());
			writer.write("</div>");
		}
		writer.write("</div>");
	}

	public void writeBotHTML(StringWriter writer, ChatBotEndpoint bot) {
		writer.write("<div class='online-user'>");
		//writer.write("<a class='user' target='_blank' href='browse?id=");
		//writer.write(String.valueOf(bot.getBot().getId()));
		//writer.write("'>");
		writer.write("<img src='");
		if (Site.HTTPS) {
			writer.write(Site.SECUREURLLINK);
		} else {
			writer.write(Site.URLLINK);
		}
		writer.write("/");
		writer.write(bot.getLoginBean().getBotBean().getAvatarThumb(bot.getLoginBean().getBotBean().getInstance()));
		writer.write("' class='chat-user-thumb'/>");
		//writer.write("</a>");
		writer.write("<div class='online-user-label'>");
		writer.write("<a class='user' target='_blank' href='browse?id=");
		writer.write(String.valueOf(bot.getBot().getId()));
		writer.write("'>");
		writer.write(bot.getBot().getNameHTML());
		writer.write("</a>");
		writer.write("</div>");
		writer.write("</div>");
	}

	public void broadcastOnlineXML(ChatEndpoint connection, ChatEndpoint client) {
		StringWriter writer = new StringWriter();
		writer.write("Online-xml: ");
		writer.write("<div class='online'>");
		writeUserHTML(writer, connection);
		writeUserHTML(writer, client);
		writer.write("</div>");
		String message = writer.toString();
		connection.sendTextIgnoreError(message);
		client.sendTextIgnoreError(message);
	}

	public void broadcastOnlineList(boolean adminsOnly) {
		StringWriter writer = new StringWriter();
		writer.write("Online: ");
		synchronized (this) {
			for (Iterator<ChatEndpoint> iterator = this.connections.iterator(); iterator.hasNext(); ) {
				ChatEndpoint client = iterator.next();
				if (!isInPrivate(client) || (adminsOnly && getPrivate(client).isBot())) {
					writer.write(client.getNickId());
					if (iterator.hasNext()) {
						writer.write(", ");
					}
				}
			}
		}
		if (!adminsOnly) {
			for (ChatBotEndpoint bot : this.botConnections) {
				bot.resetUsers(this);
			}
		}
		broadcast(writer.toString(), adminsOnly);
	}

	public void broadcastOnlineXML(boolean adminsOnly) {
		StringWriter writer = new StringWriter();
		writer.write("Online-xml: ");
		writer.write("<div class='online'>");
		synchronized (this) {
			for (Iterator<ChatEndpoint> iterator = this.connections.iterator(); iterator.hasNext(); ) {
				ChatEndpoint client = iterator.next();
				if (!isInPrivate(client) || (adminsOnly && getPrivate(client).isBot())) {
					writeUserHTML(writer, client);
				}
			}
		}
		writer.write("</div>");
		broadcast(writer.toString(), adminsOnly);
	}

	public synchronized void updateConnected(ChatEndpoint connection) {
		try {
			this.channel = AdminDatabase.instance().updateConnected(this.channel, this.connections.size());
			this.channel = AdminDatabase.instance().updateConnectedAdmins(this.channel, this.adminConnections.size());
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public boolean isChatRoom() {
		return this.channel.getType() == ChannelType.ChatRoom;
	}
	
	public boolean isOneOnOne() {
		return this.channel.getType() == ChannelType.OneOnOne;
	}
	
	public void join(ChatEndpoint connection) {
		log(Level.INFO, "join", connection.getNick());
		addConnection(connection);
		try {
			AdminDatabase.instance().incrementConnects(this.channel, ClientType.WEB, connection.getUser());
			updateConnected(connection);

			ChatMessage chat = new ChatMessage();
			chat.setChannel(this.channel);
			chat.setDomain(this.channel.getDomain());
			chat.setCreator(connection.getUser());
			chat.setNick(connection.getNick());
			chat.setMessage("connect: " + connection.getInfo());
			AdminDatabase.instance().createChatMessage(chat);
		} catch (Exception exception) {
			log(exception);
			connection.sendTextIgnoreError(String.format("Error: %s", exception.getMessage()));
		}
		LiveChatStats stats = LiveChatStats.getStats(this.channel.getId(), this.channel.getName());
		stats.connects++;
		boolean isAdmin = isAdmin(connection);
		if (isAdmin) {
			for (String oldMessage : this.history) {
				connection.sendTextIgnoreError(Utils.sanitize(oldMessage));
			}
		}
		connection.sendTextIgnoreError("Channel: " + Site.ID + this.token);
		String message = null;
		if (isOneOnOne() && !isAdmin) {
			message = String.format("Info: %s %s", connection.getNick(), "has joined. Type or click 'accept' to accept chat request.");
		} else {
			message = String.format("Info: %s %s", connection.getNick(), "has joined.");			
		}
		boolean adminsOnly = isOneOnOne();
		broadcastMessage(message, adminsOnly);
		broadcastOnlineList(adminsOnly);
		broadcastOnlineXML(adminsOnly);
		if (isOneOnOne() && isAdmin) {
			broadcastWaiting();			
		}
		String welcome = welcomeString(connection, isAdmin);
		connection.sendTextIgnoreError(welcome);
		if (isAdmin && isOneOnOne()) {
			connection.sendTextIgnoreError("Info: You are in operator mode, type or click 'accept' to chat with the next user waiting in the queue.");			
		}
	}
	
	public String welcomeString(ChatEndpoint connection, boolean isAdmin) {
		String message = "Info: " + getChannel().getWelcomeMessage();
		if (isChatRoom()) {
			message = message.replace(":title", this.channel.getName());
			message = message.replace(":users", String.valueOf(this.connections.size()));
			message = message.replace(":administrators", String.valueOf(this.adminConnections.size()));
			message = message.replace(":bots", String.valueOf(this.botConnections.size()));
			message = message.replace(":private", String.valueOf(this.privateConnections.size() / 2));
		} else if (isAdmin) {
			StringWriter writer = new StringWriter();
			writer.write("Info: Welcome to ");
			writer.write(this.channel.getName());
			if (this.connections.size() - this.adminConnections.size() == 1) {
				writer.write(", there is ");
				writer.write(String.valueOf(Math.max(0, (this.connections.size() - this.adminConnections.size()))));
				writer.write(" user, ");
			} else {
				writer.write(", there are ");
				writer.write(String.valueOf(Math.max(0, (this.connections.size() - this.adminConnections.size() - this.botConnections.size()))));
				writer.write(" users, ");
			}
			writer.write(String.valueOf(this.botConnections.size()));
			if (this.botConnections.size() == 1) {
				writer.write(" bot, and ");
			} else {
				writer.write(" bots, and ");
			}
			writer.write(String.valueOf(this.adminConnections.size()));
			if (this.adminConnections.size() == 1) {
				writer.write(" operator online.");
			} else {
				writer.write(" operators online. ");
			}
			message = writer.toString();
		} else {
			message = message.replace(":title", this.channel.getName());
			message = message.replace(":position", String.valueOf(Math.max(0, (this.connections.size() - this.adminConnections.size() - this.botConnections.size() - (this.privateConnections.size() / 2)))));
			message = message.replace(":operators", String.valueOf(this.adminConnections.size()));
			message = message.replace(":available", String.valueOf(Math.max(0, (this.adminConnections.size() - (this.privateConnections.size() / 2)))));
			if (this.channel.hasBot()) {
				message = message.replace(":bot", String.valueOf(this.channel.getBot().getName()));
			}
		}
		return message;
	}
	
	public void end(ChatEndpoint connection) {
		log(Level.INFO, "end", connection.getNick());
		String message = "";
		boolean adminsOnly = true;
		if (!this.connections.contains(connection)) {
			return;
		}
		removeConnection(connection);
		if (isInPrivate(connection)) {
			removePrivate(connection, true);			
		}
		updateConnected(connection);
		message = String.format("Info: %s %s", connection.getNick(), "has disconnected.");
		adminsOnly = isOneOnOne();
		if (adminsOnly) {
			broadcastWaiting();
		}
		broadcastMessage(message, adminsOnly);
		broadcastOnlineList(adminsOnly);
		broadcastOnlineXML(adminsOnly);
	}

	public synchronized void removeConnection(ChatEndpoint connection) {
		this.connections.remove(connection);
		this.adminConnections.remove(connection);
		this.spyConnections.remove(connection);
		this.botConnections.remove(connection);
		this.nicks.remove(connection.getNick());
		ChatEndpoint value = this.privateConnections.remove(connection);
		if (value != null) {
			this.privateConnections.remove(value);
		}
	}

	public synchronized void addConnection(ChatEndpoint connection) {
		this.connections.add(connection);
		if (isAdmin(connection)) {
			this.adminConnections.add(connection);
		}
		if (connection.isBot()) {
			this.botConnections.add((ChatBotEndpoint)connection);
		}
		this.nicks.put(connection.getNick(), connection);
	}

	public boolean addPrivate(ChatEndpoint connection, ChatEndpoint client) {
		connection.clear();
		if (client.isBot()) {
			client = ((ChatBotEndpoint)client).createPrivate(connection, this);
			if (client == null) {
				connection.sendTextIgnoreError("Error: Connection failed.");				
				return false;
			}
		} else {
			client.clear();
		}
		synchronized (this) {
			ChatEndpoint old = this.privateConnections.get(connection);
			if (old != null) {
				this.privateConnections.remove(old);
			}
			old = this.privateConnections.get(client);
			if (old != null) {
				this.privateConnections.remove(old);
			}
			this.privateConnections.put(connection, client);
			this.privateConnections.put(client, connection);
			Long token = Math.abs(Utils.random().nextLong());
			this.privateTokens.put(connection, token);
			this.privateTokens.put(client, token);
		}
		client.sendTextIgnoreError("Info: Connected to private channel with " + connection.getNick() + ".");
		client.sendTextIgnoreError("Channel: " + Site.ID + token + "private");
		if (isAdmin(connection)) {
			connection.sendTextIgnoreError("Info: Connected to private channel with " + client.getNick() + ": " + client.getInfo());
		} else {
			connection.sendTextIgnoreError("Info: Connected to private channel with " + client.getNick());			
		}
		connection.sendTextIgnoreError("Channel: " + Site.ID + token + "private");
		boolean adminsOnly = isOneOnOne();
		if (adminsOnly) {
			broadcastWaiting();
		}
		broadcastOnlineList(adminsOnly);
		broadcastOnlineXML(adminsOnly);
		broadcastOnlineList(connection, client);
		broadcastOnlineXML(connection, client);
		
		if (client.isBot()) {
			ChatEvent chatMessage = new ChatEvent();
			chatMessage.setGreet(true);
			chatMessage.setNick(connection.getNick());
			chatMessage.setMessage("");
			ChatBotEndpoint bot = (ChatBotEndpoint)client;
			Chat sense = bot.getChat(this);
			if (sense == null) {
				throw new BotException("Connection to bot failed");
			}
			bot.startStats(sense);
			sense.input(chatMessage);
		}
		return true;
	}

	public ChatEndpoint removePrivate(ChatEndpoint connection, boolean end) {
		connection.clear();
		if (!end) {
			connection.sendTextIgnoreError("Info: You have left the private channel, exiting to main channel.");
			connection.sendTextIgnoreError("Channel: " + Site.ID + this.token);
		}
		ChatEndpoint value = null;
		synchronized (this) {
			value = this.privateConnections.remove(connection);
			this.privateTokens.remove(connection);
		}
		if (value != null) {
			value.clear();
			synchronized (this) {
				this.privateConnections.remove(value);
				this.privateTokens.remove(value);
			}
			value.sendTextIgnoreError("Info: " + connection.getNick() + " has left the private channel, exiting to main channel.");
			value.sendTextIgnoreError("Channel: " + this.token);
			if (value.isBot()) {
				value.close();
			} else if (isOneOnOne() && !isAdmin(value)) {
				clearOnlineList(value);
				synchronized (this) {
					this.connections.remove(value);
					this.connections.add(value);
				}
			}
		}
		boolean adminsOnly = isOneOnOne();
		if (adminsOnly) {
			if (!end && !isAdmin(connection)) {
				clearOnlineList(connection);
				synchronized (this) {
					this.connections.remove(connection);
					this.connections.add(connection);
				}
			}
			broadcastWaiting();
		}
		broadcastOnlineList(adminsOnly);
		broadcastOnlineXML(adminsOnly);
		return value;
	}

	public List<ChatEndpoint> getConnections() {
		return connections;
	}

	public void setConnections(List<ChatEndpoint> connections) {
		this.connections = connections;
	}

	public boolean bootUser(String nick) {
		ChatEndpoint client = getConnection(nick);
		if (client == null) {
			return false;
		}
		client.sendTextIgnoreError("Info: You have been booted");
		clearOnlineList(client);
		end(client);
		client.close();
		return true;
	}

	public boolean flagUser(ChatEndpoint connection, String nick, String reason) {
		AdminDatabase.instance().flagUser(nick, connection.getNick(), reason);
		ChatEndpoint client = getConnection(nick);
		if (client == null) {
			return false;
		}
		client.sendTextIgnoreError("Info: You have been flagged for '" + reason + "'");
		clearOnlineList(client);
		end(client);
		client.close();
		return true;
	}
	
	public synchronized boolean isInPrivate(ChatEndpoint connection) {
		return this.privateConnections.containsKey(connection);
	}
	
	public synchronized boolean isSpy(ChatEndpoint connection) {
		return this.spyConnections.containsKey(connection);
	}
	
	public synchronized void addSpy(ChatEndpoint connection) {
		this.spyConnections.put(connection, connection);
	}
	
	public synchronized void removeSpy(ChatEndpoint connection) {
		this.spyConnections.remove(connection);
	}

	/**
	 * Send a copy of the conversation log to the user's email.
	 */
	public boolean emailChatLog(ChatEndpoint connection, String email) {
		if (this.channel.getEmailAddress().isEmpty()) {
			connection.sendTextIgnoreError("Error: Email has not been configured for this channel");
			return false;
		}
		StringWriter writer = new StringWriter();
		writer.write("<br/>\n");
		for (String chat : connection.getHistory()) {
			writer.write(chat);
			writer.write("<br/>\n");
		}
		try {
			AdminDatabase.instance().log(Level.INFO, "Sending email", this.channel, email);
			String body = this.channel.getEmailBody().replace(":log", writer.toString());
			LiveChatBean bean = this.loginBean.getBean(LiveChatBean.class);
			bean.setInstance(this.channel);
			bean.sendEmail(email, this.channel.getEmailTopic(), null, body);
		} catch (Exception exception) {
			connection.sendTextIgnoreError("Error: Email not available");
			return false;
		}
		return true;
	}

	public boolean privateUser(ChatEndpoint connection, String nick) {
		ChatEndpoint client = getConnection(nick);
		if (client == null) {
			connection.sendTextIgnoreError("Error: Invalid user " + nick + ".");
			return false;
		}
		if (isInPrivate(connection)) {
			connection.sendTextIgnoreError("Error: You are already in a private channel, exit first.");
			return false;
		}
		if (isInPrivate(client)) {
			if (isAdmin(connection)) {
				removePrivate(client, false);
				client.sendTextIgnoreError("Info: Switching to private chat with " + connection.getNick() + ".");
				if (!addPrivate(connection, client)) {
					return false;
				}
				return true;
			} else {
				connection.sendTextIgnoreError("Info: " + client.getNick() + " is already is a private channel.");
				return false;
			}
		}
		if (isOneOnOne()) {
			if (!isAdmin(connection)) {
				connection.sendTextIgnoreError("Error: Must be admin.");
				return false;
			}
			if (!addPrivate(connection, client)) {
				return false;
			}
		} else {
			if (client.isBot()) {
				if (!addPrivate(connection, client)) {
					return false;
				}
			} else {
				client.sendTextIgnoreError("Info: Private chat requested by " + connection.getNick() + ".");
				connection.sendTextIgnoreError("Info: Waiting for " + client.getNick() + " to accept.");
				synchronized (this) {
					this.privateRequests.put(client, connection);
				}
			}
		}
		return true;
	}
	
	public synchronized ChatEndpoint getConnection(String nick) {
		ChatEndpoint connection = this.nicks.get(nick);
		if (connection == null) {
			for (ChatEndpoint client : this.connections) {
				if (client.getNick().equalsIgnoreCase(nick)) {
					connection = client;
					break;
				}
			}
		}
		return connection;
	}
	
	public ChatEndpoint getConnectionNoLock(String nick) {
		ChatEndpoint connection = this.nicks.get(nick);
		if (connection == null) {
			for (ChatEndpoint client : this.connections) {
				if (client.getNick().equalsIgnoreCase(nick)) {
					connection = client;
					break;
				}
			}
		}
		return connection;
	}
	
	public synchronized ChatEndpoint nextWaitingConnection() {
		for (ChatEndpoint client : this.connections) {
			if (!client.isBot() && !isAdmin(client) && (!isInPrivate(client) || getPrivate(client).isBot())) {
				return client;
			}
		}
		return null;
	}

	public boolean whisperUser(ChatEndpoint connection, String nick, String message) {
		ChatEndpoint client = getConnection(nick);
		if (client == null) {
			return false;
		}
		if (client.isBot()) {
			ChatBotEndpoint bot = (ChatBotEndpoint)client;
			Chat sense = bot.getChat(this);
			if (sense == null) {
				throw new BotException("Connection to bot failed");
			}
			bot.startStats(sense);
			ChatEvent chatMessage = new ChatEvent();
			chatMessage.setNick(connection.getNick());
			chatMessage.setMessage(message);
			chatMessage.setWhisper(true);
			sense.input(chatMessage);
			return true;
		}
		try {
			String whisper = connection.getNick() + ": whipser: " + message;
			client.sendText(Utils.sanitize(whisper));
		} catch (Exception exception) {
			log(Level.WARNING, exception.toString());
			end(client);
			client.close();
			return false;
		}
		String whisper = connection.getNick() + ": whipser: " + client.getNick() + ": " + message;
		connection.sendTextIgnoreError(Utils.sanitize(whisper));
		return true;
	}

	public boolean isAdmin(ChatEndpoint client) {
		return this.channel.isAdmin(client.getUser()) || this.channel.isOperator(client.getUser());
	}
	
	public void broadcastMessage(String message, boolean adminsOnly) {
		synchronized (this) {
			this.history.add(message);
			if (this.history.size() > MAX_HISTORY) {
				this.history.removeFirst();
			}
		}
		broadcast(Utils.sanitize(message), adminsOnly);
	}
	
	public void broadcastWaiting() {
		List<ChatEndpoint> dead = new ArrayList<ChatEndpoint>();
		int position = 1;
		for (ChatEndpoint client : this.connections) {
			if (!client.isBot() && !isAdmin(client) && !isInPrivate(client)) {
				try {
					String message = "Info: " + getChannel().getStatusMessage();
					message = message.replace(":position", String.valueOf(position));
					message = message.replace(":operators", String.valueOf(this.adminConnections.size()));
					message = message.replace(":available", String.valueOf(Math.max(0, (this.adminConnections.size() - (this.privateConnections.size() / 2)))));
					client.sendText(message);
					position++;
				} catch (Exception exception) {
					log(Level.WARNING, exception.toString());
					dead.add(client);
				}
			}
		}
		removeDead(dead, true);
	}
	
	public void broadcast(String message, boolean adminsOnly) {
		List<ChatEndpoint> dead = new ArrayList<ChatEndpoint>();
		for (ChatEndpoint client : this.connections) {
			if (!isInPrivate(client) && (!adminsOnly || isAdmin(client))) {
				try {
					client.sendText(message);
				} catch (Exception exception) {
					log(Level.WARNING, exception.toString());
					dead.add(client);
				}
			}
		}
		removeDead(dead, adminsOnly);
	}
	
	public void removeDead(List<ChatEndpoint> dead, boolean adminsOnly) {
		if (dead.isEmpty()) {
			return;
		}
		for (ChatEndpoint client : dead) {
			removeConnection(client);
			client.close();
		}
		updateConnected(null);
		String deadMessage = null;
		if (dead.size() == 1) {
			deadMessage = String.format("Info: %s %s", dead.get(0).getNick(), "has been disconnected.");
		} else {
			StringWriter deadNicks = new StringWriter();
			deadNicks.write("Info: ");
			for (ChatEndpoint client : dead) {
				deadNicks.write(client.getNick());
				deadNicks.write(", ");
			}
			deadNicks.write("have been disconnected.");
			deadMessage = deadNicks.toString();
		}
		broadcast(deadMessage, adminsOnly);
		broadcastOnlineList(isOneOnOne());
		broadcastOnlineXML(isOneOnOne());
	}
	
	public void broadcastToSpys(String message, ChatEndpoint connection) {
		List<ChatEndpoint> dead = new ArrayList<ChatEndpoint>();
		if (this.spyConnections.isEmpty()) {
			return;
		}
		for (ChatEndpoint client : new ArrayList<ChatEndpoint>(this.spyConnections.values())) {
			if (client == connection
						|| (isInPrivate(client) && getPrivate(client) == connection)
						|| (!isInPrivate(client) && !isInPrivate(connection))) {
				continue;
			}
			try {
				client.sendText(message);
			} catch (Exception exception) {
				log(Level.WARNING, exception.toString());
				dead.add(client);
			}
		}
		removeDead(dead, true);
	}

	public void shutdown() {
		log(Level.INFO, "shutdown");
		try {
			broadcastMessage("Info: Shutting down", false);
			for (ChatEndpoint client : this.connections) {
				client.close();
			}
			this.channel = AdminDatabase.instance().updateConnected(this.channel, 0);
			this.channel = AdminDatabase.instance().updateConnectedAdmins(this.channel, 0);
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public void log(Level level, String message, Object... args) {
		AdminDatabase.instance().log(level, message, this, (Object[])args);
	}
	
	public void log(Exception exception) {
		AdminDatabase.instance().log(exception);
	}
	
	public String toString() {
		if (this.channel == null) {
			return getClass().getSimpleName();
		}
		return getClass().getSimpleName() + "(" + this.channel.getName() + ")";
	}
	
}
