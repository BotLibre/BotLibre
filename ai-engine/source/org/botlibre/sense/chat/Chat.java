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

import org.botlibre.Bot;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;

/**
 * Connect to and interact on IRC chat networks.
 */

public class Chat extends BasicSense {
	public static int SLEEP = 1000 * 60 * 10; // 10 minutes.
	
	public static int MAX_SPAM = 3;
	
	public static int LAST_USERS = 5;
	
	private boolean isConnected = false;
	
	private String nick = "Bot01";
	private String nickAlt = "Bot01_";

	/**
	 * Keeps track of the current conversation.
	 */
	private Long conversation;
	
	/** Keeps track of the users in the chat room. */
	private Set<String> users;
	
	/** Maps users to possible nick names. */
	private Map<String, String> userNicks;
	
	/** Maps users to speakers. */
	private Map<String, Long> userSpeakers;
	
	/** Keeps track of the last users to chat, to link response. */
	private List<String> lastUsers;
	
	/** Keeps track of spam messages. */
	private Map<String, String> spamText;
	
	/** Keeps track of spam message count. */
	private Map<String, Integer> spamCount;
	
	/** Defines number of message repeat to consider message spam. */
	private int maxSpam = MAX_SPAM;
	
	private ChatListener chatListener;
	
	public Chat() {
		initialize();
		this.languageState = LanguageState.Discussion;
	}

	public ChatListener getChatListener() {
		return chatListener;
	}

	public void setChatListener(ChatListener chatListener) {
		this.chatListener = chatListener;
	}
	
	public void initialize() {
		this.users = new HashSet<String>();
		this.userNicks = new HashMap<String, String>();
		this.userSpeakers = new HashMap<String, Long>();
		this.lastUsers = new LinkedList<String>();
		this.spamText = new HashMap<String, String>();
		this.spamCount = new HashMap<String, Integer>();
		this.conversation = null;
		this.chatListener = null;
	}
	
	public void connect() {
		disconnect();
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
		initialize();
	}
	
	/**
	 * Trim special IRC command chars from the text.
	 */
	public String trimSpecialChars(String text) {
		return text;
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
		ChatEvent message = (ChatEvent) inputText;
		String text = message.getMessage();
		String user = message.getNick();
		if (checkSpam(user, text)) {
			return;
		}
		text = trimSpecialChars(text);
		List<String> targetUsers = new ArrayList<String>();
		if (!message.isGreet()) {
			TextStream stream = new TextStream(text);
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
				stream.skipWhitespace();
				if (!stream.atEnd()) {
					text = stream.upToEnd();
				}
			}
			// Allow for compound names.
			if (targetUsers.isEmpty() && (text.indexOf(':') != -1)) {
				stream.reset();
				firstWord = stream.upTo(':');
				firstWordLower = firstWord.toLowerCase();
				stream.skip();
				if (getUsers().contains(firstWord) || getUserNicks().containsKey(firstWordLower)) {
					if (getUsers().contains(firstWord)) {
						targetUsers.add(firstWord);
					} else {
						targetUsers.add(getUserNicks().get(firstWordLower));
					}
					if (!stream.atEnd()) {
						stream.next();
					}
					stream.skipWhitespace();
					if (!stream.atEnd()) {
						text = stream.upToEnd();
					}
				}			
			}
			if (targetUsers.isEmpty()) {
				for (String possibleUser : this.lastUsers) {
					if ((possibleUser.length() > 2) && text.indexOf(possibleUser) != -1) {
						targetUsers.add(possibleUser);
						if (text.indexOf(possibleUser) == 0 && (text.length() > (possibleUser.length() + 1))) {
							text = text.substring(possibleUser.length() + 1, text.length());
						}
					}
					String trimmedPossibleUser = trimUserName(possibleUser);
					if ((trimmedPossibleUser.length() > 2) && text.indexOf(trimmedPossibleUser) != -1) {
						targetUsers.add(possibleUser);
						if (text.indexOf(trimmedPossibleUser) == 0 && (text.length() > (trimmedPossibleUser.length() + 1))) {
							text = text.substring(trimmedPossibleUser.length() + 1, text.length());
						}
					}
				}
				// Check self.
				if (text.indexOf(getNick()) != -1) {
					targetUsers.add(getNick());
					if (text.indexOf(getNick()) == 0 && (text.length() > (getNick().length() + 1))) {
						text = text.substring(getNick().length() + 1, text.length());
					}
				}
				String trimmedNick = trimUserName(getNick());
				if (text.indexOf(trimmedNick) != -1) {
					targetUsers.add(getNick());
					if (text.indexOf(trimmedNick) == 0 && (text.length() > (trimmedNick.length() + 1))) {
						text = text.substring(trimmedNick.length() + 1, text.length());
					}
				}
				if (targetUsers.isEmpty()) {
					targetUsers.addAll(this.lastUsers);
				}
			}
			// If in a private, or only two users, then assume the message is for the bot.
			if (getUsers().size() == 2) {
				targetUsers.clear();
				targetUsers.add(getNick());
			}
		}
		inputSentence(text.trim(), user, targetUsers, message.isGreet(), message.isWhisper(), network);
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
	public void inputSentence(String text, String userName, List<String> targetUserNames, boolean isGreet, boolean isWhisper, Network network) {		
		Vertex input = createInputSentence(text.trim(), network);
		if (isGreet) {
			// Null input is used to get greeting.
			input.setRelationship(Primitive.INPUT, Primitive.NULL);
			input.setRelationship(Primitive.TARGET, Primitive.SELF);
		}
		input.addRelationship(Primitive.INSTANTIATION, Primitive.CHAT);
		if (isWhisper) {
			input.addRelationship(Primitive.ASSOCIATED, Primitive.WHISPER);
		}
		// Process speaker.
		Long userId = this.userSpeakers.get(userName);
		Vertex user = null;
		if (userId != null) {
			user = network.findById(userId);
		}
		if (user == null) {
			if (userName.startsWith("anonymous")) {
				user = network.createAnonymousSpeaker();
			} else {
				user = network.createSpeaker(userName);
			}
			this.userSpeakers.put(userName, user.getId());
		}
		user.addRelationship(Primitive.NICK, network.createName(userName));
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
				targetUser.addRelationship(Primitive.NICK, network.createVertex(targetUserName));
				input.addRelationship(Primitive.TARGET, targetUser);
			}
		}
		//user.addRelationship(Primitive.INPUT, input);
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
			this.conversations++;
		} else {
			checkEngaged(conversation);
		}
		if (!isGreet) {
			Language.addToConversation(input, conversation);
		} else {
			input.addRelationship(Primitive.CONVERSATION, conversation);
		}
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
			if (getChatListener() != null) {
				log("Output:", Bot.FINE, output);
				ChatEvent message = new ChatEvent();
				if (output.hasRelationship(Primitive.ASSOCIATED, Primitive.WHISPER)) {
					message.setWhisper(true);
				}
				message.setNick(getNick(output));
				message.setMessage(printInput(output));
				getChatListener().sendMessage(message);
				addLastUser(getNick());
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	public String getNick(Vertex output) {
		Vertex target = output.getRelationship(Primitive.TARGET);
		if (target == null) {
			return null;
		}
		Vertex nick = target.getRelationship(Primitive.NICK);
		if (nick == null) {
			nick = target.getRelationship(Primitive.NAME);
		}
		if (nick == null) {
			nick = target.getRelationship(Primitive.WORD);
		}
		if (nick == null) {
			return null;
		}
		return (String)nick.getData();		
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

	public void addUser(String user) {
		this.users.add(user);
		String trimmedUser = trimUserName(user);
		if (!trimmedUser.equals(user)) {
			this.userNicks.put(trimmedUser, user);
			this.userNicks.put(user, trimmedUser);
			trimmedUser = user.toLowerCase();
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
			this.userNicks.remove(user.toLowerCase());
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