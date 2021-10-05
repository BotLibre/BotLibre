package org.botlibre.sense.discord;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.javacord.api.util.event.ListenerManager;

public class DiscordConnection {

    protected Long connectionInstanceID;
    
    protected String token = "";

    protected DiscordApi api;

    protected ListenerManager<MessageCreateListener> listenerManager;

    public DiscordConnection() {
    }

    public ListenerManager<MessageCreateListener> connect(String token, ProcessMessage processMessageInterface) {
    	this.api = new DiscordApiBuilder().setToken(token).login().join();
        this.listenerManager = this.api.addMessageCreateListener(event -> {
            processMessageInterface.processMessage(event.getMessage());
        });

        return this.listenerManager;
    }

    public void disconnect() {
    	this.listenerManager.remove();
    	this.api.disconnect();
    	
    	this.listenerManager = null;
    	this.api = null;
    }
}

public interface ProcessMessage {
	public String processMessage(Message discord);
}