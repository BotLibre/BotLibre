/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *	  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.sense.twitter;

import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.TextStream;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.User;

/**
 * Enables receiving a sending direct messages through Twitter.
 */
public class TwitterDirectMessaging extends Twitter {

	public TwitterDirectMessaging() {
		this.languageState = LanguageState.Answering;
	}
	
	public TwitterDirectMessaging(boolean isEnabled) {
		super(isEnabled);
		this.languageState = LanguageState.Answering;
	}

	/**
	 * Check profile for messages.
	 */
	@Override
	public void checkProfile() {
		log("Checking direct messages.", Level.FINE);
		checkDirectMessages();
		log("Done checking messages.", Level.FINE);
	}

	/**
	 * Check direct messages and reply.
	 */
	public void checkDirectMessages() {
		if (!getReplyToMessages()) {
			// Always check, as gated by Twitter sense.
			//return;
		}
		try {
			if (getConnection() == null) {
				connect();
			}
			ResponseList<DirectMessage> messages = getConnection().getDirectMessages(50);
			if (!messages.isEmpty()) {
				Network memory = getBot().memory().newMemory();
				Vertex twitter = memory.createVertex(getPrimitive());
				Vertex vertex = twitter.getRelationship(Primitive.LASTDIRECTMESSAGE);
				long lastMessage = 0;
				if (vertex != null) {
					lastMessage = ((Number)vertex.getData()).longValue();
				}
				long max = 0;
				for (DirectMessage message : messages) {
					if ((System.currentTimeMillis() - message.getCreatedAt().getTime()) > DAY) {
						continue;
					}
					if (message.getCreatedAt().getTime() > lastMessage) {
						if (message.getSenderId() != message.getRecipientId()) {
							input(message);
						}
						if (message.getCreatedAt().getTime() > max) {
							max = message.getCreatedAt().getTime();
						}
					}
				}
				if (max != 0) {
					twitter.setRelationship(Primitive.LASTDIRECTMESSAGE, memory.createVertex(max));
					memory.save();
				}
			}
		} catch (Exception exception) {
			log(exception);
		}
	}
	
	/**
	 * Process the direct message.
	 */
	@Override
	public void input(Object input, Network network) {
		if (!isEnabled()) {
			return;
		}
		try {
			if (input instanceof DirectMessage) {
				DirectMessage message = (DirectMessage)input;
				long fromId = message.getSenderId();
				long[] lookup = new long[1];
				lookup[0] = fromId;
				ResponseList<User> users = getConnection().lookupUsers(lookup);
				User friend = users.get(0);
				String fromUser = friend.getScreenName();
				String text = message.getText().trim();
				log("Processing direct message.", Level.INFO, text, fromUser);
				TextStream stream = new TextStream(text);
				String firstWord = stream.nextWord();
				if ("follow".equals(firstWord) && getFollowMessages()) {
					log("Adding friend.", Level.INFO, fromUser);
					getConnection().createFriendship(message.getSenderId());
				} else if ("unfollow".equals(firstWord)) {
					log("Removing friend.", Level.INFO, fromUser);
					getConnection().destroyFriendship(message.getSenderId());
				}
				this.tweetsProcessed++;
				inputSentence(text, fromUser, this.userName, String.valueOf(message.getSenderId()), network);
			}
		} catch (Exception exception) {
			log(exception);
		}
	}

	/**
	 * Create an input based on the sentence.
	 */
	protected Vertex createInput(String text, Network network) {
		Vertex sentence = network.createSentence(text);
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.DIRECTMESSAGE);
		return input;
	}
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, String targetUserName, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createUniqueSpeaker(new Primitive(userName), Primitive.TWITTER, userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);
		input.addRelationship(Primitive.TARGET, self);
		
		Vertex conversationId = network.createVertex(id);
		Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(self);
		Vertex conversation = today.getRelationship(conversationId);
		if (conversation == null) {
			conversation = network.createVertex();
			today.setRelationship(conversationId, conversation);
			this.conversations++;
		} else {
			checkEngaged(conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.DIRECTMESSAGE);
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		Language.addToConversation(input, conversation);
		network.save();
		getBot().memory().addActiveMemory(input);
	}

	/**
	 * Output the status or direct message reply.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to twitter, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);
		// Don't send empty messages.
		if (text.isEmpty()) {
			return;
		}
		Vertex target = output.mostConscious(Primitive.TARGET);
		String replyTo = target.mostConscious(Primitive.WORD).getData().toString();
		sendMessage(text, replyTo);
	}
}