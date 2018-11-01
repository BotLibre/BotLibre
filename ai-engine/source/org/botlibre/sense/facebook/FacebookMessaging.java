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
package org.botlibre.sense.facebook;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.ResponseListener;
import org.botlibre.sense.http.Http;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LanguageState;
import org.botlibre.util.Utils;

import facebook4j.Message;
import facebook4j.RawAPIResponse;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Enables receiving a sending messages through Facebook.
 */
public class FacebookMessaging extends Facebook {

	public static int MAX_WAIT = 1000; // 1 second, otherwise Facebook will timeout.
	
	public FacebookMessaging() {
		this.languageState = LanguageState.Answering;
	}
	
	public FacebookMessaging(boolean isEnabled) {
		super(isEnabled);
		this.languageState = LanguageState.Answering;
	}

	/**
	 * Check profile for messages.
	 */
	@Override
	public void checkProfile() {
		log("Checking messages.", Level.FINE);
		initProperties();
		checkDirectMessages();
		log("Done checking messages.", Level.FINE);
	}

	/**
	 * Check direct messages and reply.
	 */
	public void checkDirectMessages() {
		if (!getReplyToMessages()) {
			return;
		}
		try {
			if (getConnection() == null) {
				connect();
			}
			if (!isPage()) {
				log("Only pages can check messages", Level.WARNING);
				return;
			}
			RawAPIResponse res = getConnection().callGetAPI("/" + getConnection().getPage().getId() + "/conversations");
			JSONObject result = res.asJSONObject();
			JSONArray conversations = result.getJSONArray("data");
		    if (conversations != null && conversations.length() > 0) {
				Network memory = getBot().memory().newMemory();
				Vertex facebook = memory.createVertex(getPrimitive());
				Vertex vertex = facebook.getRelationship(Primitive.LASTDIRECTMESSAGE);
				long lastMessage = 0;
				if (vertex != null) {
					lastMessage = ((Number)vertex.getData()).longValue();
				}
				long max = 0;
			    for (int index = 0; index < conversations.length(); index++) {
			    	JSONObject conversation = conversations.getJSONObject(index);
			    	String conversationId = conversation.getString("id");
					log("Processing conversation", Level.FINE, conversationId);
					res = getConnection().callGetAPI("/" + conversationId + "/messages?fields=id,created_time,from,message");
					result = res.asJSONObject();
					JSONArray messages = result.getJSONArray("data");
				    if (messages != null && messages.length() > 0) {
					    for (int i = 0; i < messages.length(); i++) {
					    	JSONObject message = messages.getJSONObject(i);
						    Date createdTime = Utils.parseDate(message.getString("created_time"), "yyyy-MM-dd'T'HH:mm:ssX").getTime();
					    	if ((System.currentTimeMillis() - createdTime.getTime()) > DAY) {
								log("Day old message", Level.FINE, createdTime, conversationId);
					    		continue;
					    	}
					    	if (createdTime.getTime() > lastMessage) {
							    String fromUser = message.getJSONObject("from").getString("name");
							    String fromUserId = message.getJSONObject("from").getString("id");
							    if (!fromUserId.equals(this.userName)) {
									String text = message.getString("message").trim();
									log("Processing message", Level.INFO, fromUser, createdTime, conversationId, text);
									this.messagesProcessed++;
									inputSentence(text, fromUserId, fromUser, this.userName, conversationId, memory);
							    	if (createdTime.getTime() > max) {
							    		max = createdTime.getTime();
							    	}
							    } else {
									log("Ignoring own message", Level.FINE, createdTime, conversationId);
							    }
					    	} else {
								log("Old message", Level.FINE, createdTime, conversationId);
					    	}
					    }
				    } else {
						log("No messages", Level.FINE, conversationId);
				    }
			    }
			    if (max != 0) {
					if (vertex != null) {
						vertex.setPinned(false);
					}
			    	Vertex maxVertex = memory.createVertex(max);
			    	maxVertex.setPinned(true);
			    	facebook.setRelationship(Primitive.LASTDIRECTMESSAGE, maxVertex);
			    	memory.save();
			    }
			/*
		    InboxResponseList<Message> messages = getConnection().getInbox();
		    System.out.println(messages);
		    if (!messages.isEmpty()) {
				Network memory = getBot().memory().newMemory();
				Vertex facebook = memory.createVertex(getPrimitive());
				Vertex vertex = facebook.getRelationship(Primitive.LASTDIRECTMESSAGE);
				long lastMessage = 0;
				if (vertex != null) {
					lastMessage = ((Number)vertex.getData()).longValue();
				}
				long max = 0;
			    for (Message message : messages) {
				    System.out.println(message);
				    System.out.println(message.getId());
				    System.out.println(message.getFrom());
				    System.out.println(message.getMessage());
				    System.out.println(message.getCreatedTime());
			    	if ((System.currentTimeMillis() - message.getCreatedTime().getTime()) > DAY) {
			    		continue;
			    	}
			    	if (message.getCreatedTime().getTime() > lastMessage) {
						input(message);
				    	if (message.getCreatedTime().getTime() > max) {
				    		max = message.getCreatedTime().getTime();
				    	}
			    	}
			    }
			    if (max != 0) {
			    	facebook.setRelationship(Primitive.LASTDIRECTMESSAGE, memory.createVertex(max));
			    	memory.save();
			    }*/
		    } else {
				log("No conversations", Level.FINE);
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
			if (input instanceof Message) {
				Message message = (Message)input;
			    String fromId = message.getFrom().getId();
			    String fromUser = message.getFrom().getName();
				String text = message.getMessage().trim();
				log("Processing message.", Level.INFO, text, fromUser);
				this.messagesProcessed++;
				inputSentence(text, fromId, fromUser, this.userName, message.getId(), network);
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
	public void inputSentence(String text, String userId, String userName, String targetUserName, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createUniqueSpeaker(new Primitive(userId), Primitive.FACEBOOKMESSENGER, userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);		
		input.addRelationship(Primitive.TARGET, self);

		Vertex conversationId = network.createVertex(id);
		Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(self);
		Vertex conversation = today.getRelationship(conversationId);
		if (conversation == null) {
			conversation = network.createVertex();
			today.setRelationship(conversationId, conversation);
			conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
			conversation.addRelationship(Primitive.TYPE, Primitive.FACEBOOKMESSENGER);
			conversation.addRelationship(Primitive.ID, network.createVertex(id));
			conversation.addRelationship(Primitive.SPEAKER, self);
			this.conversations++;
		} else {
			checkEngaged(conversation);
		}
		conversation.addRelationship(Primitive.SPEAKER, user);
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}
	
	/**
	 * Process the text sentence.
	 */
	public String inputFacebookMessengerMessage(String text, String targetUserName, String senderId, net.sf.json.JSONObject message, Network network) {
		Vertex user = network.createUniqueSpeaker(new Primitive(senderId), Primitive.FACEBOOKMESSENGER);
		if (!user.hasRelationship(Primitive.NAME)) {
			String senderName = null;
			try {
				if (getConnection() == null) {
					connect();
				}
				String url = "https://graph.facebook.com/v2.6/" + senderId + "?fields=first_name,last_name&access_token=" + getFacebookMessengerAccessToken();
				String json = Utils.httpGET(url);
				net.sf.json.JSONObject userJSON = (net.sf.json.JSONObject)JSONSerializer.toJSON(json);
				if (userJSON != null) {
					Object firstName = userJSON.get("first_name");
					Object lastName = userJSON.get("last_name");
					if (firstName instanceof String) {
						senderName = (String)firstName;
					}
					if (lastName instanceof String) {
						if (senderName == null) {
							senderName = "";
						}
						senderName = senderName + " " + (String)lastName;
					}
				}
			} catch (Exception exception) {
				String url = "https://graph.facebook.com/v2.6/" + senderId + "?fields=first_name,last_name&access_token=" + getFacebookMessengerAccessToken();
				log(url, Level.INFO);
				url = "https://graph.facebook.com/v2.6/" + senderId + "?fields=first_name,last_name&access_token=" + getToken();
				log(url, Level.INFO);
				log(exception);
			}
			if (senderName == null || senderName.isEmpty()) {
				senderName = senderId;
			}
			user = network.createUniqueSpeaker(new Primitive(senderId), Primitive.FACEBOOKMESSENGER, senderName);
		}
		Vertex input = createInput(text.trim(), network);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);		
		input.addRelationship(Primitive.TARGET, self);
		if (getTrackMessageObjects() && message != null) {
			input.addRelationship(Primitive.MESSAGE, getBot().awareness().getSense(Http.class).convertElement(message, network));
		}

		Vertex conversationId = network.createVertex(senderId);
		Vertex today = network.getBot().awareness().getTool(org.botlibre.tool.Date.class).date(self);
		Vertex conversation = today.getRelationship(conversationId);
		if (conversation == null) {
			conversation = network.createVertex();
			today.setRelationship(conversationId, conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.FACEBOOKMESSENGER);
		conversation.addRelationship(Primitive.ID, conversationId);
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
		this.responseListener = new ResponseListener();
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
	 * Output the status or direct message reply.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			notifyResponseListener();
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to twitter, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			notifyResponseListener();
			return;
		}
		String text = printInput(output);
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		Vertex id = conversation.getRelationship(Primitive.ID);
		String conversationId = id.printString();
		
		Vertex target = output.mostConscious(Primitive.TARGET);
		String replyTo = conversationId;
		if (target != null && target.hasRelationship(Primitive.WORD)) {
			replyTo = target.mostConscious(Primitive.WORD).printString();
		}
		
		Vertex command = output.mostConscious(Primitive.COMMAND);

		if (this.responseListener != null) {
			this.responseListener.reply = text;
			notifyResponseListener();
		}
		
		// If the response is empty, do not send it.
		if (command == null && text.isEmpty()) {
			return;
		}
		if (conversation.hasRelationship(Primitive.TYPE, Primitive.FACEBOOKMESSENGER)) {
			if (command == null) {
				sendFacebookMessengerMessage(text, replyTo, conversationId);
			} else {
				sendFacebookMessengerButtonMessage(text, command.printString(), replyTo, conversationId);
			}
		} else {
			sendMessage(text, replyTo, conversationId);
		}
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void sendMessage(Vertex source, Vertex message, Vertex conversationId) {
		if (message.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(message.getNetwork().createInstance(Primitive.INPUT), null, message.getNetwork(), variables);
			message = getBot().mind().getThought(Language.class).evaluateFormula(message, variables, message.getNetwork());
			if (message == null) {
				log("Invalid template formula", Level.WARNING, message);
				return;
			}
		}
		String text = getBot().mind().getThought(Language.class).getWord(message, message.getNetwork()).printString();
		sendFacebookMessengerMessage(text, "", conversationId.printString());
	}

	/**
	 * Self API
	 * Send a message to the user.
	 */
	public void sendMessage(Vertex source, Vertex message, Vertex conversationId, Vertex command) {
		if (message.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(message.getNetwork().createInstance(Primitive.INPUT), null, message.getNetwork(), variables);
			message = getBot().mind().getThought(Language.class).evaluateFormula(message, variables, message.getNetwork());
			if (message == null) {
				log("Invalid template formula", Level.WARNING, message);
				return;
			}
		}
		String text = getBot().mind().getThought(Language.class).getWord(message, message.getNetwork()).printString();
		sendFacebookMessengerButtonMessage(text, command.printString(), "", conversationId.printString());
	}
	
	/*
	 Facebook /conversations response
	{"data":
		[
		 {"id":"t_mid.1439494778225:8df1f7618937f24b83","snippet":"what time is it?","updated_time":"2015-08-13T19:41:36+0000","message_count":2,
			"tags": {"data":[{"name":"inbox"},{"name":"read"},{"name":"seen"},{"name":"source:chat"}]},
			"participants":{"data": [{"name":"Brain Bot","email":"1603485376574546\u0040facebook.com","id":"1603485376574546"}, {"name":"Pablo Grandinetti","email":"10153500543273168\u0040facebook.com","id":"10153500543273168"} ]},
			"senders":{"data":[{"name":"Pablo Grandinetti","email":"10153500543273168\u0040facebook.com","id":"10153500543273168"}]},
			"can_reply":true,"is_subscribed":true,"link":"\/brainchatbot\/manager\/messages\/?mercurythreadid=user\u00253A580418167&threadid=mid.1439494778225\u00253A8df1f7618937f24b83&folder=inbox",
			"messages":{
				"data":[{
					"id":"m_mid.1439494896455:2990ae164ba7414921",
					"created_time":"2015-08-13T19:41:36+0000",
					"tags":{"data":[{"name":"inbox"},{"name":"read"},{"name":"source:chat"}]},
					"from":{"name":"Pablo Grandinetti","email":"10153500543273168\u0040facebook.com","id":"10153500543273168"},
					"to":{"data":[{"name":"Brain Bot","email":"1603485376574546\u0040facebook.com","id":"1603485376574546"}]},
					"message":"what time is it?"},
					{"id":"m_mid.1439494778225:8df1f7618937f24b83",
						"created_time":"2015-08-13T19:39:38+0000",
						"tags":{"data":[{"name":"inbox"},{"name":"read"},{"name":"source:chat"}]},
						"from":{"name":"Pablo Grandinetti","email":"10153500543273168\u0040facebook.com","id":"10153500543273168"},
						"to":{"data":[{"name":"Brain Bot","email":"1603485376574546\u0040facebook.com","id":"1603485376574546"}]},
						"message":"Hi"}],
				"paging":{"previous":"https:\/\/graph.facebook.com\/v2.3\/t_mid.1439494778225:8df1f7618937f24b83\/messages?access_token=CAANJOepfuhYBAPjv8MjyKwiglMvUyi11sTZAldP6GHiF6iMoFAIXTOGroUH1UXSOhoPFVUbATGPU4pzb0I3dMzrM0tIrSXHbPsw2j9k8Lu0iHtCfZAfdI73Y2Mbe89QYFowBzSX00z9bfSB9OQ4CGQVpb3hKpiXtoWch0MEofPEBrYacti&limit=25&since=1439494896&__paging_token=enc_AdA4B01qhifCqJuw7vqrZB6TbGgb6vdqh3jGnbWNAmr6tDrFBxH1wsXu74ZAsjvZAoFqWiZBdzQ7nnWlwVm8ZAyZBggWlX7r8lm67ETKZCmYr9tr5nmHQZDZD&__previous=1","next":"https:\/\/graph.facebook.com\/v2.3\/t_mid.1439494778225:8df1f7618937f24b83\/messages?access_token=CAANJOepfuhYBAPjv8MjyKwiglMvUyi11sTZAldP6GHiF6iMoFAIXTOGroUH1UXSOhoPFVUbATGPU4pzb0I3dMzrM0tIrSXHbPsw2j9k8Lu0iHtCfZAfdI73Y2Mbe89QYFowBzSX00z9bfSB9OQ4CGQVpb3hKpiXtoWch0MEofPEBrYacti&limit=25&until=1439494778&__paging_token=enc_AdCEazJZArKYZButGDMhKO7F8zIfHeDhFUd3dammZB5r4lZCRtQScIBQQpiNrFBynPcgRljWyWSWPKdRvxyM33TPZAp6i11AdYPR4CX7pZCJI4uZBvnggZDZD"}}},
			
		{"id":"t_mid.1439004373957:4fa6dcadd69692ea95","snippet":"\u53ef\u4ee5\u5e2e\u6211\u4e2a\u5fd9\u5417\uff1f","updated_time":"2015-08-08T03:26:27+0000","message_count":2,
				"tags":{"data":[{"name":"inbox"},{"name":"read"},{"name":"seen"},{"name":"source:chat"}]},
				"participants":{"data":[{"name":"\u9ec4\u91d1\u4f1f","email":"1623400744598654\u0040facebook.com","id":"1623400744598654"},{"name":"Brain Bot","email":"1603485376574546\u0040facebook.com","id":"1603485376574546"}]},
				"senders":{"data":[{"name":"\u9ec4\u91d1\u4f1f","email":"1623400744598654\u0040facebook.com","id":"1623400744598654"}]},
				"can_reply":true,"is_subscribed":true,"link":"\/brainchatbot\/manager\/messages\/?mercurythreadid=user\u00253A100007862318965&threadid=mid.1439004373957\u00253A4fa6dcadd69692ea95&folder=inbox",
				"messages":{"data":[{"id":"m_mid.1439004387183:c47081ffee6bf33c17","created_time":"2015-08-08T03:26:27+0000","tags":{"data":[{"name":"inbox"},{"name":"read"},{"name":"source:chat"}]},"from":{"name":"\u9ec4\u91d1\u4f1f","email":"1623400744598654\u0040facebook.com","id":"1623400744598654"},"to":{"data":[{"name":"Brain Bot","email":"1603485376574546\u0040facebook.com","id":"1603485376574546"}]},"message":"\u53ef\u4ee5\u5e2e\u6211\u4e2a\u5fd9\u5417\uff1f"},{"id":"m_mid.1439004373957:4fa6dcadd69692ea95","created_time":"2015-08-08T03:26:14+0000","tags":{"data":[{"name":"inbox"},{"name":"read"},{"name":"source:chat"}]},"from":{"name":"\u9ec4\u91d1\u4f1f","email":"1623400744598654\u0040facebook.com","id":"1623400744598654"},"to":{"data":[{"name":"Brain Bot","email":"1603485376574546\u0040facebook.com","id":"1603485376574546"}]},"message":"hi"}],"paging":{"previous":"https:\/\/graph.facebook.com\/v2.3\/t_mid.1439004373957:4fa6dcadd69692ea95\/messages?access_token=CAANJOepfuhYBAPjv8MjyKwiglMvUyi11sTZAldP6GHiF6iMoFAIXTOGroUH1UXSOhoPFVUbATGPU4pzb0I3dMzrM0tIrSXHbPsw2j9k8Lu0iHtCfZAfdI73Y2Mbe89QYFowBzSX00z9bfSB9OQ4CGQVpb3hKpiXtoWch0MEofPEBrYacti&limit=25&since=1439004387&__paging_token=enc_AdCAUpDVhuv8zNYZCXq79E0pzeFdeAfRYyJO5SGzZADYDaHJM4pn7frJKgVVQZA2JQbc9iCZC7JKR5Ezd9ZBMcpQZAQPvvdzBkwHmF9rPcjMKCcz2UGgZDZD&__previous=1","next":"https:\/\/graph.facebook.com\/v2.3\/t_mid.1439004373957:4fa6dcadd69692ea95\/messages?access_token=CAANJOepfuhYBAPjv8MjyKwiglMvUyi11sTZAldP6GHiF6iMoFAIXTOGroUH1UXSOhoPFVUbATGPU4pzb0I3dMzrM0tIrSXHbPsw2j9k8Lu0iHtCfZAfdI73Y2Mbe89QYFowBzSX00z9bfSB9OQ4CGQVpb3hKpiXtoWch0MEofPEBrYacti&limit=25&until=1439004374&__paging_token=enc_AdA6mkmptPXZCn4t7EF2clICSM74wWY8ZBAz9rlAQqsduhaIjtjdZA9WoKsjBqgZCuvWzW4JR9HAWJ8EMQ7qYXxuQw75J68oXxyQluloJbFZCK0o97gZDZD"}}}],
	
	"paging":{"previous":"https:\/\/graph.facebook.com\/v2.3\/1603485376574546\/conversations?access_token=CAANJOepfuhYBAPjv8MjyKwiglMvUyi11sTZAldP6GHiF6iMoFAIXTOGroUH1UXSOhoPFVUbATGPU4pzb0I3dMzrM0tIrSXHbPsw2j9k8Lu0iHtCfZAfdI73Y2Mbe89QYFowBzSX00z9bfSB9OQ4CGQVpb3hKpiXtoWch0MEofPEBrYacti&limit=25&since=1439494896&__paging_token=enc_AdCpLMo6RAqVIg5SGXL9h8lr8eYeAHAnNTUOTeWS5TUo9spjYBd2vvILmjjkuzSl5MlI49T25HZA47ZB37RHNB7SRCW19ZBP4KZCHrPE1pCbBxlLhgZDZD&__previous=1","next":"https:\/\/graph.facebook.com\/v2.3\/1603485376574546\/conversations?access_token=CAANJOepfuhYBAPjv8MjyKwiglMvUyi11sTZAldP6GHiF6iMoFAIXTOGroUH1UXSOhoPFVUbATGPU4pzb0I3dMzrM0tIrSXHbPsw2j9k8Lu0iHtCfZAfdI73Y2Mbe89QYFowBzSX00z9bfSB9OQ4CGQVpb3hKpiXtoWch0MEofPEBrYacti&limit=25&until=1439004387&__paging_token=enc_AdBAP3Q6Yky9Q0bmmnsnsPsuQLUtfJXmXSk8NY0TxSSe0ZBSp2K6wRkRAtRgdChlyLF17NRBxDnPr3XKaRjkFdnzjuco0BY51ipMJEMIXAgEBTAZDZD"}}
	}
	*/
}