/******************************************************************************
 *
 *  Copyright 2018 Paphus Solutions Inc.
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
package org.botlibre.sense.alexa;

import java.net.URL;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
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
import net.sf.json.JSONSerializer;

public class Alexa extends BasicSense {

	public static int MAX_WAIT = 60 * 1000; // 1 minute
	private static int TIMESTAMP_TOLERANCE = 150;
	private static String SIGNATURE_ALGORITHM = "SHA1withRSA";
	
	private static X509Certificate cachedCertificate = null;
	private static String cachedCertificateURL = "";
	
	protected String launchResponse = "";
	protected String helpResponse = "";
	protected String cancelResponse = "";
	protected String stopResponse = "";
	protected String fallbackResponse = "";	
	protected List<String> stopPhrases = new ArrayList<String>();
	protected String followupPrompt = "";
	
	protected boolean autoExit = false;
	
	protected boolean initProperties;
	
	protected int messagesProcessed;
	
	public Alexa(boolean enabled) {
		this.isEnabled = enabled;
		this.languageState = LanguageState.Discussion;
	}
	
	public Alexa() {
		this(false);
	}
	
	/**
	 * Start sensing.
	 */
	@Override
	public void awake() {
		super.awake();
		this.launchResponse = this.bot.memory().getProperty("Alexa.launchResponse");
		if (this.launchResponse == null) {
			this.launchResponse = "";
		}
		this.helpResponse = this.bot.memory().getProperty("Alexa.helpResponse");
		if (this.helpResponse == null) {
			this.helpResponse = "";
		}
		this.cancelResponse = this.bot.memory().getProperty("Alexa.cancelResponse");
		if (this.cancelResponse == null) {
			this.cancelResponse = "";
		}
		this.stopResponse = this.bot.memory().getProperty("Alexa.stopResponse");
		if (this.stopResponse == null) {
			this.stopResponse = "";
		}
		this.fallbackResponse = this.bot.memory().getProperty("Alexa.fallbackResponse");
		if (this.fallbackResponse == null) {
			this.fallbackResponse = "";
		}
		this.followupPrompt = this.bot.memory().getProperty("Alexa.followupPrompt");
		if (this.followupPrompt == null) {
			this.followupPrompt = "";
		}
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
			getBot().memory().loadProperties("Alexa");
			Network memory = getBot().memory().newMemory();
			Vertex alexa = memory.createVertex(getPrimitive());
			
			String property = this.bot.memory().getProperty("Alexa.launchResponse");
			if (property != null) {
				this.launchResponse = property;
			}
			property = this.bot.memory().getProperty("Alexa.cancelResponse");
			if (property != null) {
				this.cancelResponse = property;
			}
			property = this.bot.memory().getProperty("Alexa.stopResponse");
			if (property != null) {
				this.stopResponse = property;
			}
			property = this.bot.memory().getProperty("Alexa.helpResponse");
			if (property != null) {
				this.helpResponse = property;
			}
			property = this.bot.memory().getProperty("Alexa.fallbackResponse");
			if (property != null) {
				this.fallbackResponse = property;
			}
			property = this.bot.memory().getProperty("Alexa.followupPrompt");
			if (property != null) {
				this.followupPrompt = property;
			}
			property = this.bot.memory().getProperty("Alexa.autoExit");
			if (property != null) {
				this.autoExit = Boolean.valueOf(property);
			}
			
			this.stopPhrases = new ArrayList<String>();
			List<Relationship> rss = alexa.orderedRelationships(Primitive.RSS);
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
		
		memory.saveProperty("Alexa.launchResponse", this.launchResponse, true);
		memory.saveProperty("Alexa.cancelResponse", this.cancelResponse, true);
		memory.saveProperty("Alexa.stopResponse", this.stopResponse, true);
		memory.saveProperty("Alexa.helpResponse", this.helpResponse, true);
		memory.saveProperty("Alexa.fallbackResponse", this.fallbackResponse, true);
		memory.saveProperty("Alexa.followupPrompt", this.followupPrompt, true);
		memory.saveProperty("Alexa.autoExit", String.valueOf(this.autoExit), false);
		
		Vertex alexa = memory.createVertex(getPrimitive());
		alexa.unpinChildren();
		alexa.internalRemoveRelationships(Primitive.RSS);
		for (String text : this.stopPhrases) {
			Vertex rss =  memory.createVertex(text);
			alexa.addRelationship(Primitive.RSS, rss);
		}
		
		alexa.pinChildren();
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
		if(text == null || (text!= null && text.isEmpty())) {
			input.setRelationship(Primitive.INPUT, Primitive.NULL);
			input.setRelationship(Primitive.TARGET, Primitive.SELF);
		}
				
		Vertex user = network.createUniqueSpeaker(new Primitive(userName), Primitive.ALEXA);
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
		conversation.addRelationship(Primitive.TYPE, Primitive.ALEXA);
		conversation.addRelationship(Primitive.ID, network.createVertex(id));
		conversation.addRelationship(Primitive.SPEAKER, user);
		conversation.addRelationship(Primitive.SPEAKER, self);
		
		if(text != null && !text.isEmpty()) {
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
		sentence.addRelationship(Primitive.INSTANTIATION, Primitive.ALEXA);
		return input;
	}
	
	/**
	 * Output the Alexa message.
	 */
	@Override
	public void output(Vertex output) {
		if (!isEnabled()) {
			return;
		}
		Vertex sense = output.mostConscious(Primitive.SENSE);
		// If not output to Alexa, ignore.
		if ((sense == null) || (!getPrimitive().equals(sense.getData()))) {
			return;
		}
		String text = printInput(output);	
		
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
		
	public JSONObject getJSONResponse(String message, boolean elicitSlot, String slotName, boolean shouldEndSession) {
		JSONObject body = new JSONObject();
		body.put("version", "1.0");
		
		JSONObject response = new JSONObject();
		
		JSONObject outputSpeech = new JSONObject();
		outputSpeech.put("type", "PlainText");
		outputSpeech.put("text", Utils.stripTags(message));
		response.put("outputSpeech", outputSpeech);
		
		JSONObject card = new JSONObject();
		card.put("type", "Simple");
		card.put("title", getBot().getName());
		card.put("content", message);
		response.put("card", card);
		
		if(elicitSlot) {
			JSONArray directives = new JSONArray();
			JSONObject directive = new JSONObject();
			directive.put("type", "Dialog.ElicitSlot");
			directive.put("slotToElicit", slotName);
			directives.add(directive);
			response.put("directives", directives);
		}
		
		JSONObject reprompt = new JSONObject();
		reprompt.put("outputSpeech", outputSpeech);
		response.put("reprompt", reprompt);
		
		response.put("shouldEndSession", shouldEndSession);
		
		body.put("response", response);
		
		return body;
	}
	
	public void validateRequest(String json, String signature, String signatureCertChainUrl) throws Exception {
		JSONObject root = (JSONObject)JSONSerializer.toJSON(json);
		
		//1. Check request signature
		URL signatureURL = new URL(signatureCertChainUrl);
		if(!signatureURL.getProtocol().toLowerCase().equals("https")) { throw new Exception("Alexa Request - Invalid Protocol: " + signatureURL.getProtocol()); }
		if(!signatureURL.getHost().toLowerCase().equals("s3.amazonaws.com")) { throw new Exception("Alexa Request - Invalid Host " + signatureURL.getHost()); }
		if(!signatureURL.getPath().startsWith("/echo.api/")) { throw new Exception("Alexa Request - Invalid Path: " + signatureURL.getPath()); }
		if(signatureURL.getPort() > 0 && signatureURL.getPort()!=443) { throw new Exception("Alexa Request - Invalid Port: " + signatureURL.getPort()); }
		
		Date now = new Date();
		
		//Check if there is a certificate cached already
		boolean validCachedCertificate = false;
		X509Certificate cert = null;
		if(cachedCertificateURL.equals(signatureCertChainUrl) && cachedCertificate != null) {
			if(!cachedCertificate.getNotBefore().after(now) && !cachedCertificate.getNotAfter().before(now)) {
				if(checkCertSubjectAlternativeName(cachedCertificate)) {
					validCachedCertificate = true;
				}
			}
		}
		
		if(!validCachedCertificate) {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
	    	cert = (X509Certificate)cf.generateCertificate(signatureURL.openConnection().getInputStream());
	    	cachedCertificate = cert;
	    	cachedCertificateURL = signatureCertChainUrl;
		}
		else {
			cert = cachedCertificate;
		}
	    if(cert.getNotBefore().after(now) || cert.getNotAfter().before(now)) { throw new Exception("Alexa Request - Invalid Certificate(NotBefore/After)"); }
	    boolean subjectAlternativeName = checkCertSubjectAlternativeName(cert);
	    if(!subjectAlternativeName) { throw new Exception("Alexa Request - Invalid Certificate (SAN)"); }
	    PublicKey publicKey = cert.getPublicKey();
	    byte[] encryptedSignature = Base64.getDecoder().decode(signature);
	      
        Signature s = Signature.getInstance(SIGNATURE_ALGORITHM);
        s.initVerify(publicKey);
        s.update(json.getBytes("UTF-8"));
        if (!s.verify(encryptedSignature)) {
            throw new Exception("Alexa Request - Invalid Signature");
        }			    
	    
		//2. Check request timestamp
        Date timeStamp = Date.from( Instant.parse(root.getJSONObject("request").optString("timestamp")));
        long seconds = Math.abs(now.getTime()-timeStamp.getTime())/1000;
		
        if(seconds > TIMESTAMP_TOLERANCE) {
        	throw new Exception("Alexa Request - Invalid Timestamp: " + seconds);
        }
	}
	
	private boolean checkCertSubjectAlternativeName(X509Certificate cert) {
		Collection<List<?>> san;
		try {
			san = cert.getSubjectAlternativeNames();
			for (List<?> s : san) {
		    	for(Object q : s) {
		    		if(q.equals("echo-api.amazon.com")) { return true; }
		    	}
		    }
		} catch (CertificateParsingException e) {
			e.printStackTrace();
		}
	    return false;
	}
	
	public String getLaunchResponse() {
		return launchResponse;
	}

	public void setLaunchResponse(String launchResponse) {
		this.launchResponse = launchResponse;
	}
	
	public String getHelpResponse() {
		return helpResponse;
	}

	public void setHelpResponse(String helpResponse) {
		this.helpResponse = helpResponse;
	}
	
	public String getCancelResponse() {
		return cancelResponse;
	}

	public void setCancelResponse(String cancelResponse) {
		this.cancelResponse = cancelResponse;
	}
	
	public String getStopResponse() {
		return stopResponse;
	}

	public void setStopResponse(String stopResponse) {
		this.stopResponse = stopResponse;
	}
	
	public String getFallbackResponse() {
		return fallbackResponse;
	}

	public void setFallbackResponse(String fallbackResponse) {
		this.fallbackResponse = fallbackResponse;
	}
	
	public String getFollowupPrompt() {
		return followupPrompt;
	}

	public void setFollowupPrompt(String followupPrompt) {
		this.followupPrompt = followupPrompt;
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
	
	public boolean getAutoExit() {
		initProperties();
		return autoExit;
	}

	public void setAutoExit(boolean autoExit) {
		initProperties();
		this.autoExit = autoExit;
	}
}
