/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.BasicSense;
import org.botlibre.sense.ResponseListener;
import org.botlibre.thought.language.Language;
import org.botlibre.tool.Date;
import org.botlibre.util.Utils;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

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
		Vertex user = network.createUniqueSpeaker(new Primitive(userName), Primitive.SMS);
		Vertex self = network.createVertex(Primitive.SELF);
		Vertex phone = network.createVertex(id);
		input.addRelationship(Primitive.SPEAKER, user);
		input.addRelationship(Primitive.TARGET, self);
		
		Vertex today = network.getBot().awareness().getTool(Date.class).date(self);
		Vertex conversation = today.getRelationship(phone);
		// Start a new conversation if text is empty (new voice call).
		//if (conversation == null || text.isEmpty()) {
		//or maybe not, as can recover from disconnect.
		if (conversation == null) {
			conversation = network.createVertex();
			today.setRelationship(phone, conversation);
			this.conversations++;
		} else {
			checkEngaged(conversation);
		}
		conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
		conversation.addRelationship(Primitive.TYPE, Primitive.SMS);
		conversation.addRelationship(Primitive.ID, phone);
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
		
		this.responseListener = new ResponseListener();
		Network memory = bot.memory().newMemory();
		inputSentence(message, from, from, memory);
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
	 * Process to the message and reply synchronously.
	 */
	public String processVoice(String from, String speech) {
		log("Processing voice message", Level.INFO, from, speech);
		
		this.responseListener = new ResponseListener();
		Network memory = bot.memory().newMemory();
		inputSentence(speech, from, from, memory);
		memory.save();
		String reply = null;
		String command = null;
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
			command = this.responseListener.command;
			this.responseListener = null;
		}
		return generateVoiceTwiML(command, reply);
	}

	public synchronized void notifyExceptionListeners(Exception exception) {
		if (this.responseListener != null) {
			this.responseListener.notifyAll();
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
		text = format(text);

		Vertex command = output.mostConscious(Primitive.COMMAND);
		
		if (this.responseListener == null) {
			return;
		}
		this.responseListener.reply = text;
		if (command != null) {
			this.responseListener.command = command.printString();
		}
		Vertex conversation = output.getRelationship(Primitive.CONVERSATION);
		if (conversation != null) {
			this.responseListener.conversation = conversation.getDataValue();
		}
		synchronized (this.responseListener) {
			this.responseListener.notifyAll();
		}
	}

	/**
	 * Generate the voice Twilio TwiML XML string from the reply and command JSON.
	 */
	public String generateVoiceTwiML(String command, String reply) {
		JSONObject json = null;
		if (command != null && !command.isEmpty()) {
			JSONObject root = (JSONObject)JSONSerializer.toJSON(command);
			json = root.optJSONObject("twiml");
		}
		
		StringWriter writer = new StringWriter();
		writer.write("<Response>");
		if (json == null) {
			writer.write("<Gather timeout='3' input='speech dtmf'>");
			writer.write("<Say>" + reply + "</Say>");
			writer.write("</Gather>");
			writer.write("</Response>");
			return writer.toString();
		}
		// If there is a command, then only write the command elements.
		// Append the reply to the gather.
		boolean hasSay = false;
		for (Object key : json.keySet()) {
			boolean isGather = "Gather".equals(String.valueOf(key));
			boolean isSay = "Say".equals(String.valueOf(key));
			writer.write("<");
			writer.write(String.valueOf(key));
			Object element = json.opt((String)key);
			if (element instanceof String) {
				writer.write(">");
				writer.write(String.valueOf(element));
			} else if (element instanceof JSONObject) {
				boolean hasInput = false;
				boolean hasTimeout = false;
				for (Object attribute : ((JSONObject)element).keySet()) {
					if ("input".equals(String.valueOf(attribute))) {
						hasInput = true;
					}
					if ("timeout".equals(String.valueOf(attribute))) {
						hasTimeout = true;
					}
					writer.write(" ");
					writer.write(String.valueOf(attribute));
					writer.write("='");
					Object value = ((JSONObject)element).opt((String)attribute);
					writer.write(String.valueOf(value));
					writer.write("'");
				}
				if (!hasInput && isGather) {
					writer.write(" input='speech dtmf'");
				}
				if (!hasTimeout && isGather) {
					writer.write(" timeout='3'");
				}
				writer.write(">");
				if (isSay) {
					writer.write(reply);
					hasSay = true;
				}
			}
			if (isGather && !hasSay) {
				writer.write("<Say>" + reply + "</Say>");
			}
			writer.write("</");
			writer.write(String.valueOf(key));
			writer.write(">");
		}
		writer.write("</Response>");
		
		return writer.toString();
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

	public void sendSMS(String phone, String message) {
		log("Sending SMS", Level.INFO, phone, message);
		String url = "https://api.twilio.com/2010-04-01/Accounts/" + getSid() + "/Messages";
		
		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("From", getPhone());
		formParams.put("To", phone);
		formParams.put("Body", format(message));
		try {
			Utils.httpAuthPOST(url, getSid(), getSecret(), formParams);
		} catch (Exception error) {
			log(error);
		}
	}

	public void call(String phone) {
		log("Voice call", Level.INFO, phone);
		String url = "https://api.twilio.com/2010-04-01/Accounts/" + getSid() + "/Calls";

		Map<String, String> formParams = new HashMap<String, String>();
		formParams.put("From", getPhone());
		formParams.put("To", phone);
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
		String text = getBot().mind().getThought(Language.class).getWord(message, message.getNetwork()).printString();
		getBot().stat("sms");
		sendSMS(phone.printString(), text);
	}

	// Self API
	public void call(Vertex source, Vertex phone) {
		getBot().stat("call");
		call(phone.printString());
	}
	
	public String format(String text) {
		text = text.replace("<br/>", "\n");
		text = text.replace("<br>", "\n");
		text = text.replace("</br>", "");
		text = text.replace("<p/>", "\n");
		text = text.replace("<p>", "\n");
		text = text.replace("</p>", "");
		text = text.replace("<li>", "\n");
		text = text.replace("</li>", "");
		text = text.replace("<ul>", "");
		text = text.replace("</ul>", "\n");
		text = text.replace("<ol>", "");
		text = text.replace("</ol>", "\n");
		text = Utils.stripTags(text);
		return text;
	}
}