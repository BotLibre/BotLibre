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

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;
import org.javacord.api.entity.message.Message;

import io.github.furstenheim.CopyDown;
import com.github.rjeschke.txtmark.Processor;
import org.jsoup.Jsoup;

import java.util.logging.Level;

public class Discord extends BasicSense {

    public static int MAX_WAIT = 60 * 1000;

    protected String token = "";

    protected boolean initProperties;

    protected int messagesProcessed;

    protected DiscordApi api;
        
    protected ListenerManager<MessageCreateListener> listenerManager ;

    public Discord(boolean enabled) {
        this.isEnabled = enabled;
        this.languageState = LanguageState.Discussion;
    }

    public Discord() { this(false); }

    public String getUserName() {
        return this.api.getYourself().getName();
    }
    /**
     * Start sensing.
     */
    @Override
    public void awake() {

		this.token = this.bot.memory().getProperty("Discord.token");
		
		if (this.token == null || this.token.equals("")) {
		    this.token = "";
		} else {
			this.configApi();
			this.startMessageListener();
		}
    }

    /**
     * @deprecated should be removed in the future
     * use {@link #configApi()} and {@link #startMessageListener()} instead
     */
    public void startClient() {
        this.api = new DiscordApiBuilder().setToken(this.token).login().join();
        this.api.addMessageCreateListener(event -> {
            this.processMessage(event.getMessage());
        });
    }

    public String getToken() { return token; }
    
    public void configApi() {
    	this.api = new DiscordApiBuilder().setToken(this.token).login().join();
    }
    
    public void startMessageListener() {
    	this.listenerManager = this.api.addMessageCreateListener(event -> {
            this.processMessage(event.getMessage());
        });
    }

    public void stopMessageListener() {
    	if (this.listenerManager != null) {
    		this.listenerManager.remove();
    		this.api.disconnect();
    	}
    }

    public void setToken( String token ) {
        this.token = token;
    }

    public String getJoinLink() { return api.createBotInvite(); }

    public void initProperties() {
        if (this.initProperties) {
            return;
        }
        getBot().memory().loadProperties("Discord");

        String property = this.bot.memory().getProperty("Discord.token");
        if (property != null) {
            this.token = property;
        }
        this.initProperties = true;
    }

    public void saveProperties() {
        Network memory = getBot().memory().newMemory();
        memory.saveProperty("Discord.token", this.token, true);

        memory.save();
        if (this.token != null && !this.token.isEmpty()) {
            setIsEnabled(true);
        }
    }

    /**
     * Auto post to channel
     */
    public void checkProfile() {
        log("Checking profile.", Level.INFO);
        try {
            initProperties();
        } catch (Exception exception) {
            log(exception);
        }
        log("Done checking profile.", Level.INFO);
    }

    public int getMessagesProcessed() { return messagesProcessed; }

    public void setMessagesProcessed( int messagesProcessed ) { this.messagesProcessed = messagesProcessed; }

    public String processMessage(Message discord) {
        try {
            String fromId = discord.getAuthor().getIdAsString();
            String fromName = discord.getAuthor().getName();
            String recipientId = this.api.getYourself().getIdAsString();
            if (fromId.equals(recipientId)) {
                return null;
            }
            String recipientName = this.api.getYourself().getName();
            String text = Jsoup.parse(Processor.process(discord.getContent())).text();
            String conversationId = discord.getChannel().getIdAsString();
            String message = processMessage(fromId, fromName, recipientName, text, conversationId);
            if (message != null && !message.isEmpty()) {
                return sendResponse(discord, message);
            }
        } catch (Exception exception) {
            log("Discord send response exception", Level.INFO, exception.toString());
            exception.printStackTrace();
            return null;
        }
        return null;
    }

    public String processMessage(String fromID, String from, String target, String message, String id) {
        log("Processing message", Level.INFO, message);

        this.responseListener = new ResponseListener();
        Network memory = bot.memory().newMemory();
        this.messagesProcessed++;
        inputSentence(message, fromID, from, target, id, memory);
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

    public void inputSentence(String text, String userId, String userName, String targetUserName, String id, Network network) {
        Vertex input = createInput(text.trim(), network);
        Vertex user = network.createUniqueSpeaker(new Primitive(userId), Primitive.DISCORD, userName);
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
        }  else {
            checkEngaged(conversation);
        }
        conversation.addRelationship(Primitive.INSTANTIATION, Primitive.CONVERSATION);
        conversation.addRelationship(Primitive.TYPE, Primitive.DISCORD);
        conversation.addRelationship(Primitive.ID, network.createVertex(id));
        conversation.addRelationship(Primitive.SPEAKER, user);
        conversation.addRelationship(Primitive.SPEAKER, self);
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

