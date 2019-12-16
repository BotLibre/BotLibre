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
package org.botlibre.web.bean;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;

import org.botlibre.Bot;
import org.botlibre.BotException;
import org.botlibre.api.knowledge.Network;
import org.botlibre.api.knowledge.Vertex;
import org.botlibre.api.sense.ExceptionEventListener;
import org.botlibre.api.sense.Sense;
import org.botlibre.emotion.EmotionalState;
import org.botlibre.knowledge.Primitive;
import org.botlibre.self.SelfCompiler;
import org.botlibre.sense.text.CommandInput;
import org.botlibre.sense.text.TextEntry;
import org.botlibre.sense.text.TextInput;
import org.botlibre.sense.twitter.Twitter;
import org.botlibre.thought.language.Language;
import org.botlibre.thought.language.Language.LearningMode;
import org.botlibre.util.TextStream;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.Flaggable;
import org.botlibre.web.service.BingSpeech;
import org.botlibre.web.service.BotStats;
import org.botlibre.web.service.BotTranslationService;
import org.botlibre.web.service.QQSpeech;
import org.botlibre.web.service.Stats;
import org.botlibre.web.service.Voice;

public class ChatBean extends ServletBean implements ExceptionEventListener {
	public static int MAX_LOG = 100000; // 100k
	public static int MAX_UPLOAD_SIZE = 5000000; // 5meg
	public static int MAX_WAIT = 60 * 1000; // 1 minute
	
	boolean hasResponse;
	boolean speak = true;
	boolean allowSpeech = true;
	boolean allowFiles = true;
	boolean debug = false;
	boolean showChooseLanguage = true;
	boolean showAvatar = true;
	boolean showChatLog = true;
	boolean showChatBubble = false;
	boolean checkGreeting;
	boolean firstResponse;
	String send = "Send";
	String prompt = "You say";
	String farewell = "Goodbye";
	String greeting = "";
	boolean allowEmotes = false;
	boolean allowCorrection = false;
	boolean loginBanner = false;
	boolean showTitle = true;
	boolean avatarExpandable = false;
	boolean staticHTML = false;
	boolean menuBar = true;
	boolean sendImage = true;
	
	String info = "";
	String language = "";
	String botLanguage = "";
	
	String response;
	String responseFileName;
	StringWriter chatLog;
	
	String voice = "";
	String mod = "";
	
	public ChatBean() {
	}

	public void setMenubar(boolean menubar) {
		this.menuBar = menubar;
	}
	public boolean getMenubar() {
		return menuBar;
	}
	public void setSendImage(boolean sendImage) {
		this.sendImage = sendImage;
	}	
	public boolean getSendImage() {
		return sendImage;
	}
	public boolean getShowChooseLanguage() {
		return showChooseLanguage;
	}

	public void setShowChooseLanguage(boolean showChooseLanguage) {
		this.showChooseLanguage = showChooseLanguage;
	}
	
	public void setShowChatBubble(boolean bubble) {
		this.showChatBubble = bubble;
	}
	
	public boolean getShowChatBubble() {
		return showChatBubble;
	}

	public String getLanguage() {
		return language;
	}
	
	public String getVoice() {
		return voice;
	}
	
	public String getMod() {
		return mod;
	}

	/**
	 * Return the language to use for the SDK (translation of menus, speech default).
	 * If the language has not been set use the bot's language.
	 */
	public String getSDKLanguage() {
		if (this.language != null && !this.language.isEmpty()) {
			return this.language;
		}
		String lang = this.loginBean.getLanguage();
		if (lang != null && !lang.equals("en")) {
			return lang;
		}
		VoiceBean bean = this.loginBean.getBean(VoiceBean.class);
		return bean.getLanguage();
	}

	/**
	 * Return the language to use for chat speech and translation.
	 */
	public String getChatLanguage() {
		if (this.language != null && !this.language.isEmpty()) {
			return this.language;
		}
		VoiceBean bean = this.loginBean.getBean(VoiceBean.class);
		return bean.getLanguage();
	}
	
	public boolean isTranslating() {
		return this.language != null && !this.language.isEmpty();
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}
	
	public void setMod(String mod) {
		this.mod = mod;
	}
	
	public String getBotLanguage() {
		return botLanguage;
	}

	public void setBotLanguage(String botLanguage) {
		this.botLanguage = botLanguage;
	}

	public boolean getFirstResponse() {
		return firstResponse;
	}

	public void setFirstResponse(boolean firstResponse) {
		this.firstResponse = firstResponse;
	}

	public boolean getAllowEmotes() {
		return allowEmotes;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void setAllowEmotes(boolean allowEmotes) {
		this.allowEmotes = allowEmotes;
	}

	public boolean getAllowCorrection() {
		return allowCorrection;
	}

	public void setAllowCorrection(boolean allowCorrection) {
		this.allowCorrection = allowCorrection;
	}

	public boolean getLoginBanner() {
		return loginBanner;
	}

	public void setLoginBanner(boolean loginBanner) {
		this.loginBanner = loginBanner;
	}

	public boolean getShowTitle() {
		return showTitle;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

	public boolean getAvatarExpandable() {
		return avatarExpandable;
	}

	public void setAvatarExpandable(boolean avatarExpandable) {
		this.avatarExpandable = avatarExpandable;
	}

	public boolean getStaticHTML() {
		return staticHTML;
	}

	public void setStaticHTML(boolean staticHTML) {
		this.staticHTML = staticHTML;
	}

	public boolean getShowChatLog() {
		return showChatLog;
	}

	public void setShowChatLog(boolean showChatLog) {
		this.showChatLog = showChatLog;
	}

	public String getFarewell() {
		return farewell;
	}

	public void setFarewell(String farewell) {
		this.farewell = farewell;
	}

	public String getGreeting() {
		return greeting;
	}

	public void setGreeting(String greeting) {
		this.greeting = greeting;
	}

	public String getSend() {
		return send;
	}

	public void setSend(String send) {
		this.send = send;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public boolean getAllowSpeech() {
		return allowSpeech;
	}

	public void setAllowSpeech(boolean allowSpeech) {
		this.allowSpeech = allowSpeech;
	}

	public boolean getAllowFiles() {
		return allowFiles;
	}

	public void setAllowFiles(boolean allowFiles) {
		this.allowFiles = allowFiles;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setChatLog(StringWriter chatLog) {
		this.chatLog = chatLog;
	}

	@Override
	public void disconnectInstance() {
		disconnect();
	}

	@Override
	public void disconnect() {		
		this.hasResponse = false;
		this.chatLog = null;
		this.firstResponse = false;
		this.response = null;
		this.responseFileName = null;
		this.debug = false;
		this.checkGreeting = false;
		if (!getLoginBean().isEmbedded()) {
			this.send = "Send";
			this.prompt = "You say";
			this.farewell = "Goodbye";
			this.allowEmotes = false;
			this.allowCorrection = false;
			this.loginBanner = false;
			this.showTitle = true;
			this.avatarExpandable = false;
			this.showAvatar = true;
			this.showChatLog = true;
			this.allowSpeech = true;
			this.staticHTML = false;
			this.showChatBubble = false;
		}
	}

	public boolean getShowAvatar() {
		return showAvatar;
	}

	public void setShowAvatar(boolean showAvatar) {
		this.showAvatar = showAvatar;
	}

	public String getResponseHTML() {
		return Flaggable.formatBasicHTMLOutput(Utils.sanitize(this.response));
	}

	public String getResponse() {
		return  response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	public static String getSpeechFileName(String voice, String mod, String response) {
		if (response == null) {
			return null;
		}
		StringWriter writer = new StringWriter();
		String fileName = encode(response.toLowerCase());
		int size = 0;
		for (int index = 0; index < fileName.length(); index++) {
			char next = fileName.charAt(index);
			if (Character.isLetterOrDigit(next)) {
				writer.write(next);
			}
			size++;
			if (size > Voice.MAX_FILE_NAME_SIZE) {
				break;
			}
		}
		String file = writer.toString();
		if (file.isEmpty()) {
			return null;
		}
		if (voice == null) {
			voice = Voice.instance().getDefault();
		}
		if (mod != null) {
			voice = voice + mod;
		}
		return "speech/" + voice + "/" + file;
	}
	
	public String getResponseFileName() {
		return responseFileName;
	}

	public void setResponseFileName(String responseFileName) {
		this.responseFileName = responseFileName;
	}

	public boolean getSpeak() {
		return speak;
	}

	public void setSpeak(boolean speak) {
		this.speak = speak;
	}

	/**
	 * Return the response as a valid argument to Goggle TTS.
	 */
	public String getResponseTTS() {
		StringWriter writer = new StringWriter();
		String tts = getResponse().toLowerCase();
		for (int index = 0; index < tts.length(); index++) {
			char next = tts.charAt(index);
			if (next == ' ') {
				writer.write("%20");
			}
			if (next == '+') {
				writer.write("%20plus%20");
			}
			if (next == '=') {
				writer.write("%20equals%20");
			}
			if (Character.isLetterOrDigit(next)) {
				writer.write(next);
			}
		}
		return writer.toString();
	}
	
	public String translateInput(String input) {
		if (this.language == null || this.language.length() < 2) {
			return input;
		}
		if (this.botLanguage == null || this.botLanguage.length() < 2) {
			return input;
		}
		if (this.botLanguage.equals(this.language)) {
			return input;
		}
		return BotTranslationService.instance().translate(input, this.language.substring(0, 2), this.botLanguage.substring(0, 2));
	}
	
	public String translateOutput(String output) {
		if (this.language == null || this.language.length() < 2) {
			return output;
		}
		if (this.botLanguage == null || this.botLanguage.length() < 2) {
			return output;
		}
		if (this.botLanguage.equals(this.language)) {
			return output;
		}
		return BotTranslationService.instance().translate(output, this.botLanguage.substring(0, 2), this.language.substring(0, 2));
	}
	
	/**
	 * Check if the conversation has been lost, and attempt to restart.
	 */
	public void checkConversationId(long conversationId) {
		if (conversationId == 0) {
			return;
		}
		Bot bot = getBot();
		if (bot == null) {
			throw new BotException("Connection lost or timed out");
		}
		TextEntry sense = bot.awareness().getSense(TextEntry.class);
		sense.setConversationId(conversationId);
	}
	
	public synchronized void processCommand(String json, boolean correction, boolean offensive, Boolean learn) {
		Stats.stats.botChats++;
		Stats.lastChat = System.currentTimeMillis();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.chats++;
		this.hasResponse = false;
		Bot bot = getBot();
		if (bot == null) {
			throw new BotException("Connection lost or timed out");
		}
		
		if (correction  && (!getBotBean().isCorrectionAllow())) {
			throw new BotException("You do not have permission to correct");
		}
		if (offensive  && (!getLoginBean().isLoggedIn())) {
			throw new BotException("You must sign in to flag a response as offensive");
		}
		TextEntry sense = bot.awareness().getSense(TextEntry.class);
		if (sense != null) {
			if (this.chatLog == null) {
				initialize(bot);
			}
			if ((json == null) || (json.equals(""))) {
				return;
			}
			if (learn != null && learn != getAllowLearning()) {
				if (learn == Boolean.TRUE && !showLearning()) {
					throw new BotException("Learning can only be enabled by an administrator");
				}
				Language language = bot.mind().getThought(Language.class);
				language.setAllowLearning(learn);
			}
			sense.addListener(this);
			long start = System.currentTimeMillis();
			long time = 0;
			try {
				sense.input(new CommandInput(json));
				while (! this.hasResponse) {
					time = System.currentTimeMillis() - start;
					if (time > MAX_WAIT) {
						AdminDatabase.instance().getLog().log(Level.WARNING, "Timeout waiting for response: " + getBotBean().getInstanceName());
						this.response = "Timeout waiting for response";
						this.hasResponse = true;
						this.responseFileName = null;
						return;
					}
					try {
						Thread.sleep(100);
					} catch (Exception ignore) {}
				}
				//AdminDatabase.instance().getLog().log(Level.FINE, "Chat response time: " + time);
			} finally {
				sense.removeListener(this);
				Stats.stats.botMessages++;
				stats.messages++;
				time = System.currentTimeMillis() - start;
				Stats.stats.botChatTotalResponseTime = Stats.stats.botChatTotalResponseTime + time;
				stats.chatTotalResponseTime = stats.chatTotalResponseTime + time;
			}
		}
	}
	
	public synchronized void processInput(String text, boolean correction, boolean offensive, Boolean learn) {
		text = translateInput(text);
		Stats.stats.botChats++;
		Stats.lastChat = System.currentTimeMillis();
		BotStats stats = BotStats.getStats(getBotBean().getInstanceId(), getBotBean().getInstanceName());
		stats.chats++;
		this.hasResponse = false;
		Bot bot = getBot();
		if (bot == null) {
			throw new BotException("Connection lost or timed out");
		}
		if (correction  && (!getBotBean().isCorrectionAllow())) {
			throw new BotException("You do not have permission to correct");
		}
		if (offensive  && (!getLoginBean().isLoggedIn())) {
			throw new BotException("You must sign in to flag a response as offensive");
		}
		if (offensive  && (getBotBean().getInstance().getDisableFlag())) {
			throw new BotException("You do not have permission to flag a response as offensive");
		}
		TextEntry sense = bot.awareness().getSense(TextEntry.class);
		Language language = bot.mind().getThought(Language.class);
		long start = System.currentTimeMillis();
		sense.conversations = 0;
		sense.engaged = 0;
		language.defaultResponses = 0;
		language.confidence = 0;
		language.sentiment = 0;
		try {
			if (this.chatLog == null) {
				initialize(bot);
			}
			if ((text == null) || (text.equals(""))) {
				greet();
				return;
			}
			if (learn != null && learn != getAllowLearning()) {
				if (learn == Boolean.TRUE && !showLearning()) {
					throw new BotException("Learning can only be enabled by an administrator");
				}
				language.setAllowLearning(learn);
			}
			// Check for Self script execution.
			if ((text.length() > 3) && text.substring(0, 2).equals("&&")) {
				if (!getBotBean().isAdmin()) {
					throw new BotException("Only admin is allowed to execute code");
				}
				Network memory = bot.memory().newMemory();
				bot.log(SelfCompiler.getCompiler(), "eval", Level.INFO, text);
				Vertex result = SelfCompiler.getCompiler().evaluateExpression(
						text.substring(2, text.length()), sense.getUser(memory), memory.createVertex(Primitive.SELF), false, false, memory);
				memory.save();
				if (this.staticHTML) {
					this.chatLog.append("<tr><td><span class=\"chat-2\">Script: </span></td><td><span class=\"chat-2\">");
					this.chatLog.append(text.substring(2, text.length()));
					this.chatLog.append("</span></td></tr>");
					this.chatLog.append("<tr><td><span class=\"chat-2\">Result: </span></td><td><span class=\"chat-2\">");
					this.chatLog.append("" + result.getId() + ": ");
					this.chatLog.append(Utils.sanitize(result.printString()));
					this.chatLog.append("</span></td></tr>\n");
				}
				this.response = "" + result.getId() + ": " + result.printString();
				bot.log(SelfCompiler.getCompiler(), "eval result", Level.INFO, this.response);
				this.hasResponse = true;
				this.responseFileName = null;
				return;
			}
			TextInput textInput = new TextInput(text, correction, offensive);
			sense.addListener(this);
			try {
				sense.input(textInput);

				if (this.staticHTML) {
					this.chatLog.append("<tr><td><span class=\"chat-2\">You: </span></td><td><span class=\"chat-2\">");
					this.chatLog.append(text);
					this.chatLog.append("</span></td></tr>");
				}
				
				long time = 0;
				while (! this.hasResponse) {
					time = System.currentTimeMillis() - start;
					if (time > MAX_WAIT) {
						Stats.stats.botChatTimeouts++;
						AdminDatabase.instance().getLog().log(Level.WARNING, "Timeout waiting for response: " + getBotBean().getInstanceName());
						this.response = "Timeout waiting for response";
						this.hasResponse = true;
						this.responseFileName = null;
						return;
					}
					try {
						Thread.sleep(100);
					} catch (Exception ignore) {}
				}
			} finally {
				sense.removeListener(this);
			}
		} finally {
			stats.conversations = stats.conversations + sense.conversations;
			stats.engaged = stats.engaged + sense.engaged;
			stats.defaultResponses = stats.defaultResponses + language.defaultResponses;
			stats.confidence = stats.confidence + language.confidence;
			stats.sentiment = stats.sentiment + language.sentiment;
			stats.messages++;
			Stats.stats.botMessages++;
			Stats.stats.botConversations = Stats.stats.botConversations + sense.conversations;
			sense.conversations = 0;
			sense.engaged = 0;
			language.defaultResponses = 0;
			language.confidence = 0;
			language.sentiment = 0;
			long time = System.currentTimeMillis() - start;
			Stats.stats.botChatTotalResponseTime = Stats.stats.botChatTotalResponseTime + time;
			stats.chatTotalResponseTime = stats.chatTotalResponseTime + time;
		}
	}
	
	public void initialBot() {
		Bot bot = getBot();
		if (this.chatLog == null) {
			initialize(bot);
			this.chatLog = new StringWriter();
		}		
	}

	public void greet() {
		this.hasResponse = false;
		this.checkGreeting = true;
		Bot bot = getBot();
		TextEntry sense = bot.awareness().getSense(TextEntry.class);
		if (sense != null) {
			sense.addListener(this);
			try {
				if (this.chatLog == null) {
					initialize(bot);
					this.chatLog = new StringWriter();
				}
				TextInput textInput = new TextInput(null, false, false);
				sense.input(textInput);
				long start = System.currentTimeMillis();
				while (! this.hasResponse) {
					if ((System.currentTimeMillis() - start) > MAX_WAIT) {
						this.response = "Timeout waiting for response";
						this.hasResponse = true;
						this.responseFileName = null;
						return;
					}
					try {
						Thread.sleep(100);
					} catch (Exception ignore) {}
				}
				speak();
			} finally {
				sense.removeListener(this);
			}
		}
	}
	
	public void speak() {
		if (this.speak && this.loginBean.getBotBean().isConnected()) {
			VoiceBean voiceBean = getLoginBean().getBean(VoiceBean.class);
			if (getVoice() != null && getMod() != null && !getVoice().equals("") && !getMod().equals("")) {
				this.responseFileName = speak(prepareSpeechText(getResponse()), getVoice(), getMod());
			} else {
				this.responseFileName = speak(prepareSpeechText(getResponse()), voiceBean.getVoice(), voiceBean.getVoiceMod());
			}
		}
	}
	
	public static String prepareSpeechText(String text) {
		if (text == null) {
			return "";
		}
		if ((text.indexOf('<') != -1) && (text.indexOf('>') != -1)) {
			String strippedText = Utils.stripTag(text, "button");
			strippedText = Utils.stripTag(text, "select");
			strippedText = Utils.stripTag(text, "script");
			strippedText = Utils.stripTagClass(text, "nospeech");
			strippedText = Utils.stripTags(strippedText);
			if (strippedText.contains("&")) {
				strippedText = strippedText.replace("&nbsp;", " ");
				strippedText = strippedText.replace("&amp;", "&");
				strippedText = strippedText.replace("&lt;", "<");
				strippedText = strippedText.replace("&gt;", ">");
				strippedText = strippedText.replace("&quot;", "\"");
				strippedText = strippedText.replace("&apos;", "'");
			}
			return strippedText;
		}
		if (text.contains("&")) {
			text = text.replace("&nbsp;", " ");
			text = text.replace("&amp;", "&");
			text = text.replace("&lt;", "<");
			text = text.replace("&gt;", ">");
			text = text.replace("&quot;", "\"");
			text = text.replace("&apos;", "'");
		}
		// Replace http://... with "link"
		if ((text.indexOf("http://") == -1) && (text.indexOf("https://") == -1)) {
			return text;
		}
		StringWriter writer = new StringWriter();
		TextStream stream = new TextStream(text);
		while (!stream.atEnd()) {
			String chunk = stream.upToAll("http");
			writer.write(chunk);
			if (!stream.atEnd()) {
				String word = stream.nextWord();
				if ((word.indexOf("http://") == -1) && (word.indexOf("https://") == -1)) {
					writer.write(word);
				} else {
					TextStream wordStream = new TextStream(word);
					wordStream.skipToAll("//", true);
					String host = wordStream.upTo('/');
					writer.write(host);
				}
			}
		}
		return writer.toString();
	}
	
	public static String speak(String text, String voice, String mod) {
		if (text == null) {
			return null;
		}
		text = text.trim();
		if (voice != null) {
			voice = voice.trim();
		}
		String path = LoginBean.outputFilePath;
		String filePath = getSpeechFileName(voice, mod, text);
		if (filePath == null) {
			return null;
		}
		File file = new File(path + "/" + filePath);
		if (!file.exists()) {
			if (!Voice.instance().speak(voice, mod, text, path + "/" + filePath)) {
				return null;
			}
		}
		return filePath + ".wav";
	}
	
	public static String speakQQ(String text, String voice, String apiKey, String appId) {
		if (text == null) {
			return null;
		}
		text = text.trim();
		if (voice != null) {
			voice = voice.trim();
		}
	    String path = LoginBean.outputFilePath;
	    String filePath = getSpeechFileName(voice, "QQ", text);
	    if (filePath == null) {
	    	return null;
	    }
	    File file = new File(path + "/" + filePath + ".mp3");
        if (!file.exists()) {       
        	if (!QQSpeech.speak(voice, text, path + "/" + filePath, apiKey, appId)) {
        		return null;
        	}
        }
        return filePath + ".mp3";
	}
	
	public static String speakBing(String text, String voice, String apiKey, String token, String apiEndpoint) {
		if (text == null) {
			return null;
		}
		text = text.trim();
		if (voice != null) {
			voice = voice.trim();
		}
	    String path = LoginBean.outputFilePath;
	    String filePath = getSpeechFileName(voice, "Bing", text);
	    if (filePath == null) {
	    	return null;
	    }
	    File file = new File(path + "/" + filePath + ".mp3");
        if (!file.exists()) {       
        	if (!BingSpeech.speak(voice, text, path + "/" + filePath, apiKey, token, apiEndpoint, false)) {
        		return null;
        	}
        }
        return filePath + ".mp3";
	}

	public synchronized void processInfo(String info) {
		TextEntry sense = getBot().awareness().getSense(TextEntry.class);
		if (sense != null) {
			sense.setInfo(info);
		}
	}

	public synchronized void processEmote(String emote) {
		Sense sense = getBot().awareness().getSense(TextEntry.class);
		if (sense != null) {
			EmotionalState state = EmotionalState.NONE;
			if ((emote != null) && !emote.equals("")) {
				state = EmotionalState.valueOf(emote.toUpperCase());
			}
			sense.setEmotionalState(state);
		}
	}

	public synchronized void processAction(String action) {
		Sense sense = getBot().awareness().getSense(TextEntry.class);
		sense.setAction(action);
	}

	/**
	 * Return the current chat log.
	 * Clear the log if it is too big.
	 */
	public String getChatLog() {
		if (this.chatLog == null) {
			return "";
		}
		String text = this.chatLog.toString();
		if (text.length() > MAX_LOG) {
			this.chatLog = new StringWriter();
		}
		return "<table style=\"width=100%;\" cellspacing=2>" + text + "</table>";
	}

	/**
	 * Return if learning should be used for the input.
	 */
	public boolean showLearning() {
		boolean isAdmin = getBotBean().isAdmin();
		boolean isAnonymous = !isLoggedIn();
		LearningMode mode = getBot().mind().getThought(Language.class).getLearningMode();
		if (isAdmin) {
			return true;
		} else if (mode == LearningMode.Disabled) {
			return false;
		} else if (!isAdmin && (mode == LearningMode.Administrators)) {
			return false;					
		} else if (isAnonymous && (mode == LearningMode.Users)) {
			return false;
		}
		return true;
	}

	/**
	 * Return if learning should be used for the input.
	 */
	public boolean getAllowLearning() {
		Boolean learning = getBot().mind().getThought(Language.class).getAllowLearning();
		if (learning != null) {
			return learning;
		}
		boolean isAdmin = getBotBean().isAdmin();
		boolean isAnonymous = !isLoggedIn();
		LearningMode mode = getBot().mind().getThought(Language.class).getLearningMode();
		if (mode == LearningMode.Disabled) {
			return false;
		} else if (!isAdmin && (mode == LearningMode.Administrators)) {
			return false;
		} else if (isAnonymous && (mode == LearningMode.Users)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Initialize the Bot instance for the chat.
	 */
	@Override
	public void reInitialize(Bot bot) {
		final String name = this.loginBean.getBotBean().getInstanceName();
		this.response = null;
		this.hasResponse = false;
		this.botLanguage = getBotBean().getInstance().getLanguage();
		TextEntry sense = (TextEntry)bot.awareness().getSense(TextEntry.class);
		sense.clearConversation();
		Network memory = bot.memory().newMemory();
		String userName = this.loginBean.getUserId();
		Vertex user = null;
		if (!this.loginBean.isLoggedIn()) {
			// Always create anonymous as new.
			userName = "anonymous";
			user = memory.createAnonymousSpeaker();
			if (this.info != null && !this.info.isEmpty()) {
				userName = new TextStream(this.info).nextWord();
				if (userName == null || userName.isEmpty()) {
					userName = "anonymous";
				}
				user.addRelationship(Primitive.NAME, memory.createName(userName));
			}
		} else {
			if (userName.indexOf('@') != -1) {
				userName = userName.substring(0, userName.indexOf('@'));
			}
			user = memory.createUniqueSpeaker(new Primitive(userName), Primitive.CHAT, userName);
			if (this.loginBean.getBotBean().isAdmin()) {
				user.addRelationship(Primitive.ASSOCIATED, Primitive.ADMINISTRATOR);
			}
		}
		memory.save();
		sense.setUser(user);
		if (sense != null) {
			sense.setWriter(new Writer() {
				public void write(char[] text, int start, int end) {
					ChatBean.this.response = translateOutput(new String(text, start, end));
					ChatBean.this.hasResponse = true;
					if (ChatBean.this.chatLog == null) {
						ChatBean.this.chatLog = new StringWriter();
					}
					ChatBean.this.responseFileName = null;
					if (ChatBean.this.staticHTML) {
						ChatBean.this.chatLog.append("<tr><td nowrap><span class=\"chat-1\">");
						ChatBean.this.chatLog.append(name);
						ChatBean.this.chatLog.append(": </span></td><td align=left width=\"100%\" class=\"chat-1\"><span class=\"chat-1\">");
						ChatBean.this.chatLog.append(Utils.sanitize(ChatBean.this.response));
						ChatBean.this.chatLog.append("</span></td></tr>\n");
					}
				}
				public void flush() { }
				public void close() { }
			});
		}
		if (this.loginBean.getBotBean().getInstance().getEnableTwitter()) {
			Twitter twitter = bot.awareness().getSense(Twitter.class);
			if (twitter != null) {
				twitter.outputTweet("Talking with " + userName + " on #" + Site.HASHTAG);
			}
		}
		this.loginBean.getBotBean().outputAvatar();
	}

	/**
	 * Initialize the Bot instance for the chat.
	 */
	@Override
	public void initialize(Bot bot) {
		reInitialize(bot);
		this.chatLog = new StringWriter();
	}

	/**
	 * Return the current conversation id.
	 */
	public long getConversation() {
		TextEntry sense = (TextEntry)getBot().awareness().getSense(TextEntry.class);
		if (sense.getConversationId() == null) {
			return 0;
		}
		return sense.getConversationId();
	}
	
	public void notify(Throwable exception) {
		if (this.checkGreeting) {
			this.checkGreeting = false;
			this.hasResponse = true;
			return;
		}
		this.loginBean.setError(exception);
		this.hasResponse = true;
	}
}
