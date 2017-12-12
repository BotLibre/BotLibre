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
package org.botlibre.sense.sms;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.BasicSense;
import org.botlibre.thought.language.Language;
import org.botlibre.util.Utils;

/**
 * Receive and respond to incoming email.
 * Can use any pop mail server, such as gmail.
 */

public class Twilio extends BasicSense {
	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	protected String sid = "";
	protected String secret = "";
	protected String phone = "";
	
	protected boolean initProperties;
	
	protected SMSListener listener;
	
	public Twilio() {
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
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
			getBot().memory().loadProperties("Twilio");
			String property = this.bot.memory().getProperty("Twilio.sid");
			if (property != null) {
				this.sid = property;
			}
			property = this.bot.memory().getProperty("Twilio.secret");
			if ((property != null) && (this.sid != null)) {
				this.secret = Utils.decrypt(Utils.KEY, property);
			}
			property = this.bot.memory().getProperty("Twilio.phone");
			if (property != null) {
				this.phone = property;
			}
			this.initProperties = true;
		}
	}

	public void saveProperties() {
		Network memory = getBot().memory().newMemory();
		memory.saveProperty("Twilio.sid", this.sid, false);
		memory.saveProperty("Twilio.secret", Utils.encrypt(Utils.KEY, this.secret), false);
		memory.saveProperty("Twilio.phone", this.phone, false);
		memory.save();
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.SMS);
		return input;
	}

	/**
	 * Process the text sentence.
	 */
	public void inputSentence(String text, String userName, String id, Network network) {
		Vertex input = createInput(text.trim(), network);
		Vertex user = network.createSpeaker(userName);
		Vertex self = network.createVertex(Primitive.SELF);
		input.addRelationship(Primitive.SPEAKER, user);		
		input.addRelationship(Primitive.TARGET, self);
		user.addRelationship(Primitive.INPUT, input);
		
		Vertex conversation = network.createVertex(id);
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.SMS);
		conversation.addRelationship(Primitive.ID, network.createVertex(id));
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		Language.addToConversation(input, conversation);
		
		network.save();
		getBot().memory().addActiveMemory(input);
	}

	/**
	 * Process to the message and reply synchronously.
	 */
	public String processMessage(String from, String message) {
		log("Processing message", Level.INFO, from, message);
		
		this.listener = new SMSListener();
		Network memory = bot.memory().newMemory();
		inputSentence(message, from, from, memory);
		memory.save();
		String reply = null;
		synchronized (this.listener) {
			if (this.listener.reply == null) {
				try {
					this.listener.wait(MAX_WAIT);
				} catch (Exception exception) {
					log(exception);
					return "";
				}
			}
			reply = this.listener.reply;
			this.listener = null;
		}
		
		return reply;
	}

	public synchronized void notifyExceptionListeners(Exception exception) {
		if (this.listener != null) {
			this.listener.notifyAll();
		}
		super.notifyExceptionListeners(exception);
	}

	/**
	 * Output the SMS message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to sms, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);		
		if (this.listener == null) {
			return;
		}
		this.listener.reply = text;
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		if (conversation != null) {
			this.listener.conversation = conversation.getDataValue();
		}
		synchronized (this.listener) {
			this.listener.notifyAll();
		}
	}
	
	public String getSid() {
		initProperties();
		return sid;
	}

	public void setSid(String sid) {
		initProperties();
		this.sid = sid;
	}
	
	public String getPhone() {
		initProperties();
		return phone;
	}

	public void setPhone(String phone) {
		initProperties();
		this.phone = phone;
	}

	public String getSecret() {
		initProperties();
		return secret;
	}

	public void setSecret(String secret) {
		initProperties();
		this.secret = secret;
	}

	public SMSListener getListener() {
		return listener;
	}

	public void setListener(SMSListener listener) {
		this.listener = listener;
	}

	public void sendSMS(String phone, String message) {
		log("Sending SMS", Level.INFO, phone, message);
		String url = "https://api.twilio.com/2010-04-01/Accounts/" + getSid() + "/Messages";
		
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("From", getPhone());
		formParams.put("To", phone);
		formParams.put("Body", message);
		try {
			Utils.httpAuthPOST(url, getSid(), getSecret(), formParams);
		} catch (Exception error) {
			log(error);
		}
	}

	// Self API
	public void sms(Vertex source, Vertex phone, Vertex message) {
		if (message.instanceOf(Primitive.FORMULA)) {
			Map<Vertex, Vertex> variables = new HashMap<Vertex, Vertex>();
			SelfCompiler.addGlobalVariables(message.getNetwork().createInstance(Primitive.INPUT), null, message.getNetwork(), variables);
			message = getBot().mind().getThought(Language.class).evaluateFormula(message, variables, message.getNetwork());
			if (message == null) {
				log("Invalid template formula", Level.WARNING, message);
				return;
			}
		}
		String post = getBot().mind().getThought(Language.class).getWord(message, message.getNetwork()).printString();
		getBot().stat("sms");
		sendSMS(phone.printString(), post);
	}
}