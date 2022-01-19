/******************************************************************************
 *
 *  Copyright 2021 Paphus Solutions Inc.
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
package org.botlibre.sense.discord;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

import org.javacord.api.entity.message.Message;

import io.github.furstenheim.CopyDown;
import com.github.rjeschke.txtmark.Processor;
import org.jsoup.Jsoup;

import java.util.logging.Level;

public class Discord extends BasicSense {
	
	public static int MAX_WAIT = 60 * 1000;
	
	protected String token = "";
	protected String savedToken = "";
	
	protected boolean initProperties;
	protected LanguageState groupMode = LanguageState.Discussion;
		
	public Discord(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Discord() {
		this(false);
	}
	

	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		
		this.token = this.bot.memory().getProperty("Discord.token");
		this.savedToken = this.bot.memory().getProperty("Discord.savedToken");
		
		setIsEnabled(true);
		
		if (this.token == null) {
			this.token = "";
		}
		if (this.savedToken == null) {
			this.savedToken = "";
		}
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}

	public String getSavedToken() {
		return savedToken;
	}
	
	public void setSavedToken(String savedToken) {
		this.savedToken = savedToken;
	}

	public LanguageState getGroupMode() {
		initProperties();
		return groupMode;
	}

	public void setGroupMode(LanguageState groupMode) {
		initProperties();
		this.groupMode = groupMode;
	}
	
	public void initProperties() {
		if (this.initProperties) {
			return;
		}
		getBot().memory().loadProperties("Discord");
		
		String property = this.bot.memory().getProperty("Discord.token");
		if (property != null) {
			this.token = property;
		}

		property = this.bot.memory().getProperty("Discord.savedToken");
		if (property != null) {
			this.savedToken = property;
		}

		property = this.bot.memory().getProperty("Discord.groupMode");
		if (property != null) {
			this.groupMode = LanguageState.valueOf(property);
		}
		this.initProperties = true;
	}
	
	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Discord.token", this.token, true);
		memory.saveProperty("Discord.savedToken", this.savedToken, true);
		memory.saveProperty("Discord.groupMode", String.valueOf(this.groupMode), false);
		
		memory.save();
		if (this.token != null && !this.token.isEmpty()) {
			setIsEnabled(true);
		}
	}
	
	public String processMessage(Message discord) {
		try {
			String fromId = discord.getAuthor().getIdAsString();
			String fromName = discord.getAuthor().getName();
			if (fromId.equals(discord.getApi().getYourself().getIdAsString())) {
				return null;
			}
			if (discord.getAuthor().isBotUser()) {
				return null;
			}
			boolean group = !discord.isPrivateMessage();
			String recipientId = null;
			String recipientName = null;
			if (group) {
				if (getGroupMode() == LanguageState.Ignore) {
					// Ignore messages sent to a group.
					return null;
				}
				if (!discord.getMentionedUsers().isEmpty()) {
					recipientId = discord.getMentionedUsers().get(0).getIdAsString();
					recipientName = discord.getMentionedUsers().get(0).getName();
				}
			} else {
				recipientId = discord.getApi().getYourself().getIdAsString();
				recipientName = discord.getApi().getYourself().getName();
			}
			
			// trim, remove @id
			String text = Jsoup.parse(Processor.process(discord.getContent())).text();
			text = text.replaceAll("@\\![0-9]+\\s","");

			String conversationId = discord.getChannel().getIdAsString();
			String message = processMessage(text, fromId, fromName, recipientId, recipientName, discord.getApi().getYourself().getIdAsString(), conversationId, group);
			if (message != null && !message.isEmpty()) {
				return sendResponse(discord, message);
			}
		} catch (Exception exception) {
			log(exception);
			return null;
		}
		return null;
	}
	
	public String processMessage(String message, String userId, String userName, String targetUserId, String targetUserName, String botUserId, String conversationId, boolean group) {
		log("Processing message", Level.INFO, message);
		
		this.responseListener = new ResponseListener();
		Network memory = bot.memory().newMemory();
//        this.messagesProcessed++;
		inputSentence(message, userId, userName, targetUserId, targetUserName, botUserId, conversationId, group, memory);
		memory.save();
		String reply = null;
		synchronized (this.responseListener) {
			if (this.responseListener.reply == null) {
				try {
					this.responseListener.wait(MAX_WAIT);
				} catch (Exception exception) {
					log(exception);
					return "";
				}
			}
			reply = this.responseListener.reply;
			this.responseListener = null;
		}
		
		return reply;
	}
	
	public void inputSentence(String text, String userId, String userName, String targetUserId, String targetUserName, String botUserId, String id, boolean group, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createUniqueSpeaker(new Primitive(userId), Primitive.DISCORD, userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);
		
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
		conversation.addRelationship(Primitive.TYPE, Primitive.DISCORD);
		conversation.addRelationship(Primitive.ID, network.createVertex(id));
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		if (group) {
			this.languageState = getGroupMode();
			if (botUserId.equals(targetUserId)) {
				input.addRelationship(Primitive.TARGET, Primitive.SELF);
			} else if (targetUserId != null) {
				input.addRelationship(Primitive.TARGET, network.createUniqueSpeaker(new Primitive(targetUserId), Primitive.DISCORD, targetUserName));
			}
		} else {
			this.languageState = LanguageState.Answering;
			input.addRelationship(Primitive.TARGET, Primitive.SELF);
		}
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
	
	protected Vertex createInput(String text, Network network) {
		Vertex sentence = network.createSentence(text);
		Vertex input = network.createInstance(Primitive.INPUT);
		input.setName(text);
		input.addRelationship(Primitive.SENSE, getPrimitive());
		input.addRelationship(Primitive.INPUT, sentence);
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.DISCORD);
		return input;
	}
	
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			notifyResponseListener();
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			notifyResponseListener();
			return;
		}
		String text = printInput(output);
		text = Utils.stripTags(text);
		
		if (this.responseListener == null) {
			return;
		}
		this.responseListener.reply = text;
		
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		if (conversation != null) {
			this.responseListener.conversation = conversation.getDataValue();
		}
		notifyResponseListener();
	}
	
	protected String sendResponse(Message discord, String response) throws Exception {
		CopyDown converter = new CopyDown();
		discord.getChannel().sendMessage(converter.convert(response));
		return response;
	}
}
