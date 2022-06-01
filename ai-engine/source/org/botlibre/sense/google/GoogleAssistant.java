/** Copyright 2013-2018 Paphus Solutions Inc. - All rights reserved. */
package org.botlibre.sense.google;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Relationship;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GoogleAssistant extends BasicSense {
	
	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	protected List<String> stopPhrases = new ArrayList<String>();
	
	protected boolean autoExit = false;

	protected boolean initProperties;
	
	protected int messagesProcessed;
	
	public GoogleAssistant(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public GoogleAssistant() {
		this(false);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		setIsEnabled(true);
	}
	
	/**
	 * Load settings.
	 */
	public void initProperties() {
		if (this.initProperties) {
			return;
		}
		synchronized (this) {
			if (this.initProperties) {
				return;
			}
			getBot().memory().loadProperties("GoogleAssistant");
			Network memory = getBot().memory().newMemory();
			Vertex googleAssistant = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("GoogleAssistant.autoExit");
			if (property != null) {
				this.autoExit = Boolean.valueOf(property);
			}
			
			this.stopPhrases = new ArrayList<String>();
			List<Relationship> rss = googleAssistant.orderedRelationships(Primitive.RSS);
			if (rss != null) {
				for (Relationship relationship : rss) {
					String text = ((String)relationship.getTarget().getData()).trim();
					if (!text.isEmpty()) {
						this.stopPhrases.add(text);
					}
				}
			}
			
			this.initProperties = true;
		}
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		
		memory.saveProperty("GoogleAssistant.autoExit", String.valueOf(this.autoExit), false);
		
		Vertex googleAssistant = memory.createVertex(getPrimitive());
		googleAssistant.unpinChildren();
		googleAssistant.internalRemoveRelationships(Primitive.RSS);
		for (String text : this.stopPhrases) {
			Vertex rss =  memory.createVertex(text);
			googleAssistant.addRelationship(Primitive.RSS, rss);
		}
		
		googleAssistant.pinChildren();
		memory.save();
		setIsEnabled(true);
	}
	
	public int getMessagesProcessed() {
		return messagesProcessed;
	}

	public void setMessagesProcessed(int messagesProcessed) {
		this.messagesProcessed = messagesProcessed;
	}
	
	/**
	 * Process to the message and reply synchronously.
	 */
	public String processMessage(String from, String message, String id) {
		log("Processing message", Level.INFO, message);
		
		this.responseListener = new ResponseListener();
		Network memory = bot.memory().newMemory();
		this.messagesProcessed++;
		inputSentence(message, from, id, memory);
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
	
	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, String id, Network network) {
		Vertex input = createInput(text != null ? text.trim() : "", network);
		
		//Greeting
		if (text == null || text.isEmpty()) {
			input.setRelationship(Primitive.INPUT, Primitive.NULL);
			input.setRelationship(Primitive.TARGET, Primitive.SELF);
		}
		
		Vertex user = network.createUniqueSpeaker(new Primitive(userName), Primitive.GOOGLEASSISTANT);
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
		conversation.addRelationship(Primitive.TYPE, Primitive.GOOGLEASSISTANT);
		conversation.addRelationship(Primitive.ID, network.createVertex(id));
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		
		if (text != null && !text.isEmpty()) {
			Language.addToConversation(input, conversation);
		} else {
			input.addRelationship(Primitive.CONVERSATION, conversation);
		}
		
		network.save();
		getBot().memory().addActiveMemory(input);
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.GOOGLEASSISTANT);
		return input;
	}
	
	/**
	 * Output the Google Assistant message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to Google Assistant, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);
		
		//Strip html tags from response
		text = Utils.stripTags(text);
		
		if (this.responseListener == null) {
			return;
		}
		this.responseListener.reply = text;
		
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		if (conversation != null) {
			this.responseListener.conversation = conversation.getDataValue();
		}
		
		synchronized (this.responseListener) {
			this.responseListener.notifyAll();
		}
	}
	
	public JSONObject getJSONResponse(String message, boolean expectResponse) {
		JSONObject body = new JSONObject();
		body.put("fulfillmentText", message);
		JSONObject payload = new JSONObject();
		JSONObject google = new JSONObject();
		google.put("expectUserResponse", expectResponse);
		JSONObject richResponse = new JSONObject();
		JSONArray items = new JSONArray();
		JSONObject simpleResponse = new JSONObject();
		JSONObject textToSpeech = new JSONObject();
		textToSpeech.put("textToSpeech", message);
		simpleResponse.put("simpleResponse", textToSpeech);
		items.add(simpleResponse);
		richResponse.put("items", items);
		google.put("richResponse", richResponse);
		payload.put("google", google);
		body.put("payload", payload);
		return body;
	}
	
	public boolean getAutoExit() {
		initProperties();
		return autoExit;
	}

	public void setAutoExit(boolean autoExit) {
		initProperties();
		this.autoExit = autoExit;
	}
	
	public List<String> getStopPhrases() {
		initProperties();
		return stopPhrases;
	}
	
	public void setStopPhrases(List<String> stopPhrases) {
		initProperties();
		this.stopPhrases = stopPhrases;
	}
	
	public boolean isStopPhrase(String phrase) {
		if(phrase!=null) {
			initProperties();
			for(String s : stopPhrases) {
				if(phrase.toLowerCase().equals(s.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
}
