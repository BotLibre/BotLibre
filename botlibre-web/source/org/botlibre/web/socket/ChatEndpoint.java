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
package org.botlibre.web.socket;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.botlibre.util.TextStream;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;
import org.botlibre.web.bean.LiveChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.chat.ChatChannel;
import org.botlibre.web.chat.ChatRoom;
import org.botlibre.web.chat.ChatRoomManager;
import org.botlibre.web.service.AppIDStats;
import org.botlibre.web.service.Stats;

@ServerEndpoint(value = "/live/chat")
//@ServerEndpoint(value = "/live/chat", configurator = ChatEndpointConfig.class)
public class ChatEndpoint {
	public static int MAX_HISTORY = 50;
	public static int MAX_WAITING = 500;

    protected static ConcurrentMap<Long, Long> openRooms = new ConcurrentHashMap<Long, Long>();
    protected static ConcurrentMap<ChatEndpoint, ChatEndpoint> waiting = new ConcurrentHashMap<ChatEndpoint, ChatEndpoint>();

    protected String nick;
    protected String info = "";
    protected Session session;
    protected Long roomId;
    protected User user;
	protected LinkedList<String> history = new LinkedList<String>();

	public static ConcurrentMap<Long, Long> getOpenRooms() {
		return openRooms;
	}
	
	public static void shutdown(ChatChannel channel) {
        Long id = openRooms.get(channel.getId());
        if (id != null) {
        	ChatRoom room = ChatRoomManager.manager().removeInstance(id);
        	if (room != null) {
        		room.shutdown();
        	}
        }
	}

    public ChatEndpoint() {
    }
    
    public void clear() {
    	this.history = new LinkedList<String>();
    }

    @OnOpen
    public void join(Session session) {
    	log(Level.INFO, "join");
    	clear();
        this.session = session;
        //HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        //if (httpSession == null) {
        	if (waiting.size() > MAX_WAITING) {
        		waiting.clear();
        	}
        	waiting.put(this, this);
        	return;
        /*}
		SessionProxyBean proxy = (SessionProxyBean)httpSession.getAttribute("proxy");
		LoginBean loginBean = (LoginBean)httpSession.getAttribute("loginBean");
		if ((proxy != null) && (proxy.getBeanId() != null)) {
			loginBean = proxy.getLoginBean();
			proxy.clear();
		}
        LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
        joinRoom(loginBean, bean);*/
    }

    public void joinRoom(LoginBean loginBean, LiveChatBean bean) {
        ChatChannel channel = bean.getInstance();
        if (channel == null) {
        	if (waiting.size() > MAX_WAITING) {
        		waiting.clear();
        	}
        	waiting.put(this, this);
            return;
        }
        Long id = openRooms.get(bean.getInstance().getId());
        ChatRoom room = ChatRoomManager.manager().getInstance(id);
        if (room == null) {
			Stats.stats.chatRooms++;
        	room = new ChatRoom(channel);
        	id = ChatRoomManager.manager().addInstance(room);
        	room.setId(id);
        	LoginBean roomBean = new LoginBean();
        	roomBean.setUser(loginBean.getUser());
        	roomBean.setLoggedIn(true);
        	room.initialize(roomBean);
        	Long absent = openRooms.putIfAbsent(bean.getInstance().getId(), id);
        	if (absent != null) {
        		ChatRoom managed = ChatRoomManager.manager().getInstance(absent);
        		if (managed == null) {
        			openRooms.put(bean.getInstance().getId(), id);
        		} else {
        			room = managed;
        		}
        	}
        }
        if ((loginBean != null) && (loginBean.getUser() != null) && !loginBean.getUser().getUserId().isEmpty()) {
        	this.nick = loginBean.getUser().getUserId();
        	this.user = loginBean.getUser().detach();
        } else {
        	if (this.info.isEmpty()) {
	        	long nickId = room.nextId();
	        	if (nickId > 0) {
	        		this.nick = "anonymous" + nickId;
	        	} else {
	        		this.nick = "anonymous";
	        	}
        	} else {
        		String nick = new TextStream(this.info).nextWord();
        		if (nick == null) {
        			nick = "anonymous";
        		}
        		String checkNick = nick;
        		while (room.getConnection(checkNick) != null) {
        			checkNick = nick + room.nextId();
        		}
        		this.nick = checkNick;
        	}
        	sendTextIgnoreError("Nick: " + this.nick);
        }
        this.roomId = id;
		Stats.stats.chatConnects++;
        room.join(this);
    	clear();
    }
    
    public ChatRoom getChatRoom() {
    	return ChatRoomManager.manager().getInstance(this.roomId);
    }

    @OnClose
    public void end() {
    	log(Level.INFO, "end");
    	clear();
        ChatRoom room = getChatRoom();
        if (room != null) {
        	room.end(this);
        } else {
        	waiting.remove(this);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        ChatRoom room = getChatRoom();
        if (room != null) {
    		room.message(this, message);
        } else {
        	if (message.startsWith("connect")) {
        		try {
        			String connectString = message;
        			if (message.indexOf("@info") != -1) {
        				int index = message.indexOf("@info");
        				connectString = message.substring(0, index);
        				this.info = message.substring(index + 5, message.length()).trim();
        			}
	        		TextStream stream = new TextStream(connectString.trim());
	        		stream.nextWord();
	        		String channel = stream.nextWord();
	        		String user = stream.nextWord();
	        		String password = stream.nextWord();
	        		String applicationId = stream.nextWord();
	        		if (password == null) {
	        			applicationId = user;
	        			user = null;
	        		}
        			String appUser = AdminDatabase.instance().validateApplicationId(applicationId, null);
        			if (appUser != null) {
        				AppIDStats stat = AppIDStats.getStats(applicationId, appUser);
        				AppIDStats.checkMaxAPI(stat, appUser, null, null);
        				stat.apiCalls++;
        			}
	        		long token = 0;
	        		if (password != null) {
		        		try {
		        			token = Long.valueOf(password);
		        			password = null;
		        		} catch (Exception invalid) {}
	        		}
	        		LoginBean loginBean = new LoginBean();
	        		if (user != null) {
	        			loginBean.connect(user, password, token);
		        		if (loginBean.getError() != null) {
		        			throw loginBean.getError();
		        		}
	        		}
	        		LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
	        		bean.validateInstance(channel);
	        		if (loginBean.getError() != null) {
	        			throw loginBean.getError();
	        		}
	        		joinRoom(loginBean, bean);
	        		waiting.remove(this);
        		} catch (Throwable exception) {
        			sendTextIgnoreError("Connect failed: " + exception.getMessage());
        		}
        		return;
        	}
        	sendTextIgnoreError("Error: The channel has been disconnected.");
        }
    }

    @OnError
    public void onError(Throwable exception) throws Throwable {
    	log(Level.WARNING, exception.toString());
    }

    public void sendText(String message) throws IOException {
    	this.session.getBasicRemote().sendText(message);
    }

    public void sendTextIgnoreError(String message) {
    	try {
    		this.session.getBasicRemote().sendText(message);
    	} catch (Exception exception) {
        	AdminDatabase.instance().log(exception);
    	}
    }

    public void close() {
    	try {
        	clear();
    		this.session.close();
    	} catch (IOException ignore) {}
    }

	public String getNickId() {
		return nick;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Long getRoomId() {
		return roomId;
	}

	public void setRoomId(Long roomId) {
		this.roomId = roomId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

    public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public LinkedList<String> getHistory() {
		return history;
	}

	public void setHistory(LinkedList<String> history) {
		this.history = history;
	}

	public void addHistory(String message) {
		try {
			this.history.add(message);
			if (this.history.size() > MAX_HISTORY) {
				this.history.removeFirst();
			}
		} catch (Exception exception) {}
	}

	public boolean isBot() {
    	return false;
    }
	
	public void log(Level level, String message, Object... args) {
    	AdminDatabase.instance().log(level, message, this, (Object[])args);
	}
	
	public void log(Throwable exception) {
    	AdminDatabase.instance().log(exception);
	}
	
	public String toString() {
		return getClass().getSimpleName() + ":" + this.roomId;
	}
}
