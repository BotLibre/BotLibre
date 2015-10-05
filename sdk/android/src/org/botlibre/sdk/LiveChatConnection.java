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

package org.botlibre.sdk;

import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.UserConfig;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;

/**
 * Connection class for a Live Chat, or chatroom connection.
 * A live chat connection is different than an SDKConnection as it is asynchronous,
 * and uses web sockets for communication.
 */
public class LiveChatConnection {
	protected boolean debug = false;
	protected ChannelConfig channel;
	protected UserConfig user;
	protected Credentials credentials;
    protected WebSocketConnection socket;
    protected LiveChatListener listener;
    protected boolean keepAlive = false;
    protected Thread keepAliveThread;
	
    /**
     * Create a new connection with the application credentials and the listener.
     * The listener will be notified asynchronously of messages and events.
     */
	public LiveChatConnection(Credentials credentials, LiveChatListener listener) {
		this.credentials = credentials;
		this.listener = listener;
	}
	
	/**
	 * Connection to the live chat server channel.
	 * Validate the user credentials.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void connect(ChannelConfig channel, UserConfig user) {
		this.channel = channel;
		this.user = user;
		try {
			this.socket = new WebSocketConnection();
			this.socket.connect("ws://" + this.credentials.host + this.credentials.app
						+ "/live/chat", new WebSocketConnectionHandler() {
				@Override
				public void onOpen() {
					if (LiveChatConnection.this.user == null) {
						LiveChatConnection.this.socket.sendTextMessage("connect " + LiveChatConnection.this.channel.id + " " + LiveChatConnection.this.credentials.applicationId);
					} else {
						LiveChatConnection.this.socket.sendTextMessage(
								"connect " + LiveChatConnection.this.channel.id + " " + LiveChatConnection.this.user.user
								+ " " + LiveChatConnection.this.user.token + " " + LiveChatConnection.this.credentials.applicationId);						
					}
				}

				@Override
				public void onTextMessage(String text) {
			    	String user = "";
			    	String message = text;
			    	int index = text.indexOf(':');
			    	if (index != -1) {
			    		user = text.substring(0, index);
			    		message = text.substring(index + 2, text.length());
			    	}
					if (user.equals("Online-xml")) {
						return;
					}
					if (user.equals("Online")) {
						LiveChatConnection.this.listener.updateUsers(message);
						return;
					}
					
					if (LiveChatConnection.this.keepAlive && user.equals("Info") && text.contains("pong")) {
						return;
					}
					if (user.equals("Info")) {
						LiveChatConnection.this.listener.info(text);
						return;
					}
					if (user.equals("Error")) {
						LiveChatConnection.this.listener.error(text);
						return;
					}
					LiveChatConnection.this.listener.message(text);
				}

				@Override
				public void onClose(int code, String reason) {
					LiveChatConnection.this.listener.message("Info: Closed");
					LiveChatConnection.this.listener.closed();
				}
			});
		} catch (Exception exception) {
			throw new SDKException(exception);
		}
	}

	/**
	 * Sent a text message to the channel.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 * Note, the listener will receive its own messages.
	 */
	public void sendMessage(String message) {
		checkSocket();
		this.socket.sendTextMessage(message);
	}

	/**
	 * Accept a private request.
	 * This is also used by an operator to accept the top of the waiting queue.
	 * This can also be used by a user to chat with the channel bot.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void accept() {
		checkSocket();
		this.socket.sendTextMessage("accept");
	}

	/**
	 * Test the connection.
	 * A pong message will be returned, this message will not be broadcast to the channel.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void ping() {
		checkSocket();
		this.socket.sendTextMessage("ping");
	}

	/**
	 * Exit from the current private channel.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void exit() {
		checkSocket();
		this.socket.sendTextMessage("exit");
	}

	/**
	 * Request a private chat session with a user.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void pvt(String user) {
		checkSocket();
		this.socket.sendTextMessage("pvt: " + user);
	}

	/**
	 * Boot a user from the channel.
	 * You must be a channel administrator to boot a user.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void boot(String user) {
		checkSocket();
		this.socket.sendTextMessage("boot: " + user);
	}

	/**
	 * Send a private message to a user.
	 * This call is asynchronous, any error or success with be sent as a separate message to the listener.
	 */
	public void whisper(String user, String message) {
		checkSocket();
		this.socket.sendTextMessage("whisper:" + user + ": " + message);
	}

	/**
	 * Disconnect from the channel.
	 */
	public void disconnect() {
    	this.keepAlive = false;
    	if (this.keepAliveThread != null) {
    		this.keepAliveThread.interrupt();
    	}
    	if (this.socket != null) {
    		this.socket.disconnect();
    	}
	}
 
    protected void runKeepAlive() {
    	this.keepAliveThread = new Thread() {
    		public void run() {
    			while (keepAlive) {
	    			sendMessage("ping");
	    			try {
	    				Thread.sleep(600000);
	    			} catch (InterruptedException exception) {
	    				return;
	    			}
    			}
    		}
    	};
    	this.keepAliveThread.start();
    }
    
    public boolean isDebug() {
		return debug;
	}

	/**
	 * Enable debugging messages (logged to System.out).
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Return the current channel.
	 */
	public ChannelConfig getChannel() {
		return channel;
	}

	protected void setChannel(ChannelConfig channel) {
		this.channel = channel;
	}

	/**
	 * Return the current user.
	 */
	public UserConfig getUser() {
		return user;
	}

	protected void setUser(UserConfig user) {
		this.user = user;
	}

	/**
	 * Return the current application credentials.
	 */
	public Credentials getCredentials() {
		return credentials;
	}

	protected void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

	public LiveChatListener getListener() {
		return listener;
	}

	public void setListener(LiveChatListener listener) {
		this.listener = listener;
	}

	/**
	 * Return if the connection will be kept alive, and not allowed to timeout due to inactivity.
	 */
	public boolean isKeepAlive() {
		return keepAlive;
	}

	/**
	 * Set if the connection should be kept alive, and not allowed to timeout due to inactivity.
	 */
	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
    	if (this.keepAlive) {
    		runKeepAlive();
    	} else if (this.keepAliveThread != null) {
    		this.keepAliveThread.interrupt();
    		this.keepAliveThread = null;
    	}
    }
	
	protected void checkSocket() {
		if (this.socket == null) {
			throw new SDKException("Not connected");
		}
	}
}