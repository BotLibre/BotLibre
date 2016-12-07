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
package org.botlibre.sense.chat;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;
import org.relayirc.chatengine.Channel;
import org.relayirc.chatengine.ChannelEvent;
import org.relayirc.chatengine.ChannelListener;
import org.relayirc.chatengine.Server;
import org.relayirc.chatengine.ServerEvent;
import org.relayirc.chatengine.ServerListener;

/**
 * Connect to and interact on IRC chat networks.
 */

public class IRC extends BasicSense {
	public static int SLEEP = 1000 * 60 * 10; // 10 minutes.
	
	public static int MAX_SPAM = 3;
	
	public static int LAST_USERS = 5;
	
	private String serverName = "irc.freenode.org";
	private String channelName = "#ai";
	private int port = 6667;
	private String nick = "Bot01";
	private String nickAlt = "Bot01_";
	private String userName = "Bot01";
	private String realName = "Bot01";

	private boolean isConnected = false;
	private Server server;
	private Channel channel;

	/**
	 * Keeps track of the current conversation.
	 */
	private Long conversation;
	
	/** Keeps track of the users in the chat room. */
	private Set<String> users;
	
	/** Maps users to possible nick names. */
	private Map<String, String> userNicks;
	
	/** Keeps track of the last users to chat, to link response. */
	private List<String> lastUsers;
	
	/** Keeps track of spam messages. */
	private Map<String, String> spamText;
	
	/** Keeps track of spam message count. */
	private Map<String, Integer> spamCount;
	
	/** Defines number of message repeat to consider message spam. */
	private int maxSpam = MAX_SPAM;
	
	private List<ChannelListener> channelListeners = new ArrayList<ChannelListener>();
	
	class IRCServerListener implements ServerListener {

		public IRCServerListener() {
			log("Connecting:", Bot.FINE, getServerName());
			try {
				setServer(new Server(getServerName(), getPort(), "n/a", "n/a"));
				getServer().addServerListener(this);
				getServer().connect(getNick(), getNickAlt(), getUserName(), getRealName());
				// Check for a dead connection and reconnect.
				Runnable connectionChecker = new Runnable() {
					public void run() {
						Server server = getServer();
						while (isConnected()) {
							try {
								Thread.sleep(SLEEP);
							} catch (Exception exception) {
								// Ignore.
							}
							server = getServer();
							if (server == null) {
								break;
							}
							log("Ping:", Level.FINER, server.isConnected());
							if (isConnected() && !server.isConnected() && (getServer() != null)) {
								log("Connection lost, reconnecting", Bot.WARNING);								
								connect();
								break;
							}
						}
					}					
				};
				Thread thread = new Thread(connectionChecker);
				thread.start();
			} catch (Exception exception) {
				log(exception);
			}
		}
		
		public String eventToString(ServerEvent event) {
			String toString = "";
			if (event.getSource() != null) {
				toString = toString + " s:" + event.getSource();
			}
			if (event.getChannelName() != null) {
				toString = toString + " c:" + event.getChannelName();
			}
			if (event.getMessage() != null) {
				toString = toString + " m:" + event.getMessage();
			}
			if (event.getOriginNick() != null) {
				toString = toString + " n:" + event.getOriginNick();
			}
			if (event.getTargetNick() != null) {
				toString = toString + " tn:" + event.getTargetNick();
			}
			if (event.getUser() != null) {
				toString = toString + " u:" + event.getUser();
			}
			if (event.getUsers() != null) {
				toString = toString + " us:" + event.getUsers();
			}
			return toString;			
		}

		public void onConnect(ServerEvent event) {
			try {
				log("Connected:", Bot.FINE, eventToString(event));
				getServer().sendJoin(getChannelName());
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onWhoIs(ServerEvent event) {
			try {
				log("WhoIs:", Bot.FINE, eventToString(event));
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onIsOn(ServerEvent event) {
			try {
				log("IsOn:", Bot.FINE, eventToString(event));
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onStatus(ServerEvent event) {
			try {
				log("Status:", Bot.FINE, eventToString(event));
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onChannelPart(ServerEvent event) {
			try {
				log("ChannelPart:", Bot.FINE, eventToString(event));
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onChannelAdd(ServerEvent event) {
			try {
				log("ChannelAdd:", Bot.FINE, eventToString(event));
				Channel channel = (Channel) event.getChannel();
				setChannel(channel);
				channel.addChannelListener(new IRCChannelListener());
				for (ChannelListener listener : getChannelListeners()) {
					channel.addChannelListener(listener);				
				}
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onInvite(ServerEvent event) {
			try {
				log("Invite:", Bot.FINE, eventToString(event));
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onChannelJoin(ServerEvent event) {
			try {
				Channel channel = (Channel) event.getChannel();
				setChannel(channel);
				log("Joined:", Bot.FINE, channel);
				// Wait for add.
			} catch (Exception exception) {
				log(exception);
			}
		}

		public void onDisconnect(ServerEvent event) {
			log("Disconnected:", Bot.FINE, eventToString(event));
		}
	}
	
	class IRCChannelListener implements ChannelListener {
		
		public String eventToString(ChannelEvent event) {
			String toString = "";
			if (event.getSource() != null) {
				toString = toString + " s:" + event.getSource();
			}
			if (event.getSubjectAddress() != null) {
				toString = toString + " sa:" + event.getSubjectAddress();
			}
			if (event.getSubjectNick() != null) {
				toString = toString + " sn:" + event.getSubjectNick();
			}
			if (event.getOriginNick() != null) {
				toString = toString + " n:" + event.getOriginNick();
			}
			if (event.getOriginAddress() != null) {
				toString = toString + " na:" + event.getOriginAddress();
			}
			if (event.getValue() != null) {
				toString = toString + " v:" + event.getValue();
			}
			return toString;			
		}

		public void onMessage(ChannelEvent event) {
			log("Message:", Level.INFO, eventToString(event));
			try {
				input(event);
			} catch (Exception exception) {
				log(exception);
			}
		}
		
		public void onBan(ChannelEvent event) {
			log("Ban:", Bot.FINE, eventToString(event));
		}
		
		public void onKick(ChannelEvent event) {
			log("Kick:", Bot.FINE, eventToString(event));
		}
		
		public void onPart(ChannelEvent event) {
			log("Part:", Bot.FINE, eventToString(event));
		}
		
		public void onQuit(ChannelEvent event) {
			log("Quit:", Bot.FINE, eventToString(event));
			removeUser(((String)event.getValue()).trim());
		}
		
		public void onConnect(ChannelEvent event) {
			log("Connect:", Bot.FINE, eventToString(event));
		}
		
		public void onOp(ChannelEvent event) {
			log("Op:", Bot.FINE, eventToString(event));
		}
		
		public void onDeOp(ChannelEvent event) {
			log("DeOp:", Bot.FINE, eventToString(event));
		}
		
		public void onJoin(ChannelEvent event) {
			log("Join:", Bot.FINE, eventToString(event));
			addUser(((String)event.getValue()).trim());
		}
		
		public void onJoins(ChannelEvent event) {
			log("Join:", Bot.FINE, eventToString(event));
			TextStream stream = new TextStream((String)event.getValue());
			String user = stream.nextWord();
			while (user != null) {
				addUser(user);
				user = stream.nextWord();
			}
		}
		
		public void onNick(ChannelEvent event) {
			log("Nick:", Bot.FINE, eventToString(event));
		}
		
		public void onAction(ChannelEvent event) {
			log("Action:", Bot.FINE, eventToString(event));
		}
		
		public void onActivation(ChannelEvent event) {
			log("Activation:", Bot.FINE, eventToString(event));
		}
		
		public void onTopicChange(ChannelEvent event) {
			log("TopicChange:", Bot.FINE, eventToString(event));
		}
		
		public void onDisconnect(ChannelEvent event) {
			log("Disconnect:", Bot.FINE, eventToString(event));
		}
	}

	public IRC() {
		initialize();
		this.languageState = LanguageState.Discussion;
	}

	public void initialize() {
		this.users = new HashSet<String>();
		this.userNicks = new HashMap<String, String>();
		this.lastUsers = new LinkedList<String>();
		this.spamText = new HashMap<String, String>();
		this.spamCount = new HashMap<String, Integer>();
		this.conversation = null;
	}
	
	public void connect() {
		disconnect();
		new IRCServerListener();
		setConnected(true);
	}
	
	/**
	 * Stop sensing.
	 */
	@Override
	public void shutdown() {
		super.shutdown();
		disconnect();
	}
	
	/**
	 * Reset state when instance is pooled.
	 */
	@Override
	public void pool() {
		disconnect();
	}
	
	public void disconnect() {
		setConnected(false);
		Server server = getServer();
		setServer(null);
		setChannel(null);
		initialize();
		if (server != null) {
			server.disconnect();
		}
	}
	
	/**
	 * Trim special IRC command chars from the text.
	 */
	public String trimSpecialChars(String text) {
		TextStream stream = new TextStream(text);
		StringWriter writer = new StringWriter();
		while (!stream.atEnd()) {
			char next = stream.next();
			// char 3 means the next to chars are a command like colour, etc.
			if (next == (char)3) {
				stream.skip(2);
			} else {
				writer.append(next);
			}
		}
		return writer.toString();
	}
	
	/**
	 * Trim non-letters and lower case.
	 */
	public String trimUserName(String text) {
		TextStream stream = new TextStream(text);
		StringWriter writer = new StringWriter();
		while (!stream.atEnd()) {
			char next = stream.next();
			// char 3 means the next to chars are a command like colour, etc.
			if (Character.isLetter(next)) {
				writer.append(next);
			}
		}
		return writer.toString().toLowerCase();
	}
	
	/**
	 * Process the input chat event.
	 * Check the source user and check for a targeted user.
	 * Ignore if spam.
	 */
	public void input(Object inputText, Network network) {
		if (!isEnabled()) {
			return;
		}
		ChannelEvent event = (ChannelEvent) inputText;
		String text = (String)event.getValue();
		String user = event.getOriginNick();
		if (checkSpam(user, text)) {
			return;
		}
		text = trimSpecialChars(text);
		TextStream stream = new TextStream(text);
		List<String> targetUsers = new ArrayList<String>();
		String firstWord = stream.nextWord();
		if (firstWord == null) {
			// Ignore empty chat.
			return;
		}
		String firstWordLower = firstWord.toLowerCase();
		// Check if a directed question and trim nick.
		// Try to avoid matching users with common word names like 'hi', 'lol'
		if (getUsers().contains(firstWord) || getUserNicks().containsKey(firstWordLower)) {
			if (getUsers().contains(firstWord)) {
				targetUsers.add(firstWord);
			} else {
				targetUsers.add(getUserNicks().get(firstWordLower));
			}
			if (!stream.atEnd()) {
				stream.next();
			}
			text = stream.upToEnd();
		} else {
			for (String possibleUser : this.lastUsers) {
				if ((possibleUser.length() > 2) && text.indexOf(possibleUser) != -1) {
					targetUsers.add(possibleUser);
				}
				String trimmedPossibleUser = trimUserName(possibleUser);
				if ((trimmedPossibleUser.length() > 2) && text.indexOf(trimmedPossibleUser) != -1) {
					targetUsers.add(possibleUser);
				}
			}
			// Check self.
			if (text.indexOf(getNick()) != -1) {
				targetUsers.add(getNick());
			}
			String trimmedNick = trimUserName(getNick());
			if (text.indexOf(trimmedNick) != -1) {
				targetUsers.add(getNick());
			}
			if (targetUsers.isEmpty()) {
				targetUsers.addAll(this.lastUsers);
			}
		}
		inputSentence(text.trim(), user, targetUsers, network);
		addLastUser(user);
	}

	/**
	 * Ignore users that spam the same message repeatedly.
	 */
	public boolean checkSpam(String user, String text) {
		String lastSpam = this.spamText.get(user);
		if (text.equals(lastSpam)) {
			Integer count = this.spamCount.get(user);
			if (count == null) {
				count = 0;
			}
			this.spamCount.put(user, count + 1);
			if (count.intValue() > this.maxSpam) {
				return true;
			}
		} else {
			this.spamText.put(user, text);
			this.spamCount.put(user, 1);
		}
		if (this.spamText.size() > 50) {
			this.spamText.clear();
			this.spamCount.clear();
		}
		return false;
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, List<String> targetUserNames, Network network) {
		Vertex input = createInputSentence(text.trim(), network);
		input.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
		// Process speaker.
		Vertex user = network.createSpeaker(userName);
		input.addRelationship(Primitive.SPEAKER, user);
		// Process target speakers.
		Set<String> uniqueTargetUserNames = new HashSet<String>();
		for (String targetUserName : targetUserNames) {
			if (!targetUserName.equals(userName) && !uniqueTargetUserNames.contains(targetUserName)) {
				uniqueTargetUserNames.add(targetUserName);
				Vertex targetUser = null;
				if (targetUserName.equals(getNick()) || targetUserName.equals(getNickAlt())) {
					targetUser = network.createVertex(Primitive.SELF);
				} else {
					targetUser = network.createSpeaker(targetUserName);
				}
				input.addRelationship(Primitive.TARGET, targetUser);
			}
		}
		user.addRelationship(Primitive.INPUT, input);
		// Process conversation.
		Vertex conversation = getConversation(network);
		if (conversation == null) {
			conversation = network.createInstance(Primitive.CONVERSATION);
			conversation.addRelationship(Primitive.TYPE, Primitive.CHAT);
			setConversation(conversation);
			conversation.addRelationship(Primitive.SPEAKER, Primitive.SELF);
			for (String eachUser : getUsers()) {
				conversation.addRelationship(Primitive.SPEAKER, network.createSpeaker(eachUser));
			}
		}
		Language.addToConversation(input, conversation);
		network.save();
		getBot().memory().addActiveMemory(input);
	}

	/**
	 * Output the vertex to text.
	 */
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to IRC, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		try {
			if (getChannel() != null) {
				log("Output:", Bot.FINE, output);
				getChannel().sendMessage(printInput(output) + "\n");
				for (ChannelListener listener : getChannelListeners()) {
					ChannelEvent event = new ChannelEvent(getChannel(), getNick(), getUserName(), printInput(output));
					listener.onMessage(event);				
				}
				this.lastUsers.remove(0);
				this.lastUsers.add(getNick());
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public void addLastUser(String user) {
		/*if (this.lastUsers.contains(user)) {
			this.lastUsers.remove(user); 
		} -- only last five messages, not users -- */
		if (this.lastUsers.size() > LAST_USERS) {
			this.lastUsers.remove(this.lastUsers.size() - 1);
		}
		this.lastUsers.add(0, user);
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getNickAlt() {
		return nickAlt;
	}

	public void setNickAlt(String nickAlt) {
		this.nickAlt = nickAlt;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}

	public List<ChannelListener> getChannelListeners() {
		return channelListeners;
	}

	public void setChannelListeners(List<ChannelListener> channelListeners) {
		this.channelListeners = channelListeners;
	}

	public void addUser(String user) {
		this.users.add(user);
		String trimmedUser = trimUserName(user);
		if (!trimmedUser.equals(user)) {
			this.userNicks.put(trimmedUser, user);
			this.userNicks.put(user, trimmedUser);
		}
	}

	public void removeUser(String user) {
		this.users.remove(user);
		String trimmedUser = trimUserName(user);
		if (!trimmedUser.equals(user)) {
			this.userNicks.remove(trimmedUser);
			this.userNicks.remove(user);
		}
	}

	public Set<String> getUsers() {
		return users;
	}

	public void setUsers(Set<String> users) {
		this.users = users;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public Map<String, String> getUserNicks() {
		return userNicks;
	}

	public void setUserNicks(Map<String, String> userNicks) {
		this.userNicks = userNicks;
	}

	/**
	 * Return the current conversation.
	 */
	public Vertex getConversation(Network network) {
		if (this.conversation == null) {
			return null;
		}
		return network.findById(conversation);
	}

	/**
	 * Set the current conversation.
	 */
	public void setConversation(Vertex conversation) {
		if (conversation == null) {
			this.conversation = null;
		} else {
			this.conversation = conversation.getId();
		}
	}

}