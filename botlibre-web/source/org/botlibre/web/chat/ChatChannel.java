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
package org.botlibre.web.chat;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;

import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.BotMode;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.ChannelConfig;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="CHAT_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="CHAT_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="CHAT_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="CHAT_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="CHAT_ERRORS"))
})
public class ChatChannel extends WebMedium {
	protected int messages;
	protected int connectedUsersCount;
	protected int connectedAdminsCount;
	protected ChannelType type = ChannelType.ChatRoom;
	@ManyToMany
	@JoinTable(name = "CHAT_OPERATORS")
	protected List<User> operators = new ArrayList<User>();
	@OneToOne(fetch=FetchType.LAZY)
	protected BotInstance bot;
	protected BotMode botMode = BotMode.AnswerAndListen;
	protected AccessMode videoAccessMode = AccessMode.Users;
	protected AccessMode audioAccessMode = AccessMode.Users;
	protected AccessMode inviteAccessMode = AccessMode.Administrators;
	@Column(length=1024)
	protected String welcomeMessage = "";
	@Column(length=1024)
	protected String statusMessage = "";

	protected String emailAddress = "";
	protected String emailUserName = "";
	protected String emailPassword = "";
	protected String emailProtocol = "";
	protected boolean emailSSL = false;
	protected String emailIncomingHost = "";
	protected int emailIncomingPort = 0;
	protected String emailOutgoingHost = "";
	protected int emailOutgoingPort = 0;

	@Column(length=1024)
	protected String emailTopic = "Chat Log";
	@Column(length=1024)
	protected String emailBody = "Here is the log of your conversation<br/>:log<br/>";
	
	public String getEmailTopic() {
		if (emailTopic == null || emailTopic.isEmpty()) {
			return "Chat Log";
		}
		return emailTopic;
	}

	public void setEmailTopic(String emailTopic) {
		this.emailTopic = emailTopic;
	}

	public String getEmailBody() {
		if (emailBody == null || emailBody.isEmpty()) {
			return "Here is the log of your conversation<br/>:log<br/>";
		}
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public String getEmailAddress() {
		if (emailAddress == null) {
			return "";
		}
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getEmailUserName() {
		if (emailUserName == null) {
			return "";
		}
		return emailUserName;
	}

	public void setEmailUserName(String emailUserName) {
		this.emailUserName = emailUserName;
	}

	public String getEmailPassword() {
		if (emailPassword == null) {
			return "";
		}
		return emailPassword;
	}

	public void setEmailPassword(String emailPassword) {
		this.emailPassword = emailPassword;
	}

	public String getEmailProtocol() {
		if (emailPassword == null) {
			return "";
		}
		return emailProtocol;
	}

	public void setEmailProtocol(String emailProtocol) {
		this.emailProtocol = emailProtocol;
	}

	public boolean getEmailSSL() {
		return emailSSL;
	}

	public void setEmailSSL(boolean emailSSL) {
		this.emailSSL = emailSSL;
	}

	public String getEmailIncomingHost() {
		if (emailIncomingHost == null) {
			return "";
		}
		return emailIncomingHost;
	}

	public void setEmailIncomingHost(String emailIncomingHost) {
		this.emailIncomingHost = emailIncomingHost;
	}

	public int getEmailIncomingPort() {
		return emailIncomingPort;
	}

	public void setEmailIncomingPort(int emailIncomingPort) {
		this.emailIncomingPort = emailIncomingPort;
	}

	public String getEmailOutgoingHost() {
		if (emailOutgoingHost == null) {
			return "";
		}
		return emailOutgoingHost;
	}

	public void setEmailOutgoingHost(String emailOutgoingHost) {
		this.emailOutgoingHost = emailOutgoingHost;
	}

	public int getEmailOutgoingPort() {
		return emailOutgoingPort;
	}

	public void setEmailOutgoingPort(int emailOutgoingPort) {
		this.emailOutgoingPort = emailOutgoingPort;
	}

	public enum ChannelType { ChatRoom, OneOnOne, Random }
	
	public ChatChannel() {
	}

	public ChatChannel(String name) {
		super(name);
	}
	
	public String getDefaultWelcomeMessage() {
		if (isOneOnOne()) {
			if (hasBot()) {
				return "Welcome to :title, you are in position :position in the queue, there are :operators operators online, and :available available.<br/> Type or click 'accept' at any point to speak with :bot, the automated chat bot agent.";
			} else {
				return "Welcome to :title, you are in position :position in the queue, there are :operators operators online, and :available available.";
			}
		} else {
			return "Welcome to :title, there are :users users in this channel, including :administrators administrators, :bots bots, and :private users in private channels.";
		}
	}
	
	public String getDefaultStatusMessage() {
		if (isOneOnOne()) {
			return "You are in position :position in the queue, there are :operators operators online, and :available available.";
		} else {
			return "";
		}
	}
	
	public String getWelcomeMessage() {
		if (this.welcomeMessage == null || this.welcomeMessage.isEmpty()) {
			return getDefaultWelcomeMessage();
		}
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		if (welcomeMessage != null && welcomeMessage.equals(getDefaultWelcomeMessage())) {
			this.welcomeMessage = "";
			return;
		}
		this.welcomeMessage = welcomeMessage;
	}

	public String getStatusMessage() {
		if (this.statusMessage == null || this.statusMessage.isEmpty()) {
			return getDefaultStatusMessage();
		}
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		if (statusMessage != null && statusMessage.equals(getDefaultStatusMessage())) {
			this.statusMessage = "";
			return;
		}
		this.statusMessage = statusMessage;
	}

	public AccessMode getVideoAccessMode() {
		if (videoAccessMode == null) {
			return AccessMode.Users;
		}
		return videoAccessMode;
	}
	
	public AccessMode getInviteAccessMode() {
		if (inviteAccessMode == null) {
			return AccessMode.Administrators;
		}
		return inviteAccessMode;
	}

	public String getForwarderAddress() {
		return "/livechat?id=" + getId() + "&chat=true";
	}

	public void setVideoAccessMode(AccessMode videoAccessMode) {
		this.videoAccessMode = videoAccessMode;
	}
	
	public void setInviteAccessMode(AccessMode inviteAccessMode) {
		this.inviteAccessMode = inviteAccessMode;
	}

	public AccessMode getAudioAccessMode() {
		if (audioAccessMode == null) {
			return AccessMode.Users;
		}
		return audioAccessMode;
	}

	public void setAudioAccessMode(AccessMode audioAccessMode) {
		this.audioAccessMode = audioAccessMode;
	}

	@Override
	public String getTypeName() {
		return "Channel";
	}
	
	public WebMediumConfig buildBrowseConfig() {
		ChannelConfig config = new ChannelConfig();
		toBrowseConfig(config);
		config.messages = String.valueOf(this.messages);
		config.usersOnline = String.valueOf(this.connectedUsersCount);
		config.adminsOnline = String.valueOf(this.connectedAdminsCount);
		return config;
	}
	
	public ChannelConfig buildConfig() {
		ChannelConfig config = new ChannelConfig();
		toConfig(config);
		config.type = this.type.name();
		config.videoAccessMode = getVideoAccessMode().name();
		config.audioAccessMode = getAudioAccessMode().name();
		config.inviteAccessMode = getInviteAccessMode().name();
		config.messages = String.valueOf(this.messages);
		config.usersOnline = String.valueOf(this.connectedUsersCount);
		config.adminsOnline = String.valueOf(this.connectedAdminsCount);
		return config;
	}
	
	public boolean hasBot() {
		return this.bot != null;
	}
	
	public BotInstance getBot() {
		return bot;
	}

	public void setBot(BotInstance bot) {
		this.bot = bot;
	}

	public BotMode getBotMode() {
		return botMode;
	}

	public void setBotMode(BotMode botMode) {
		this.botMode = botMode;
	}

	public boolean isOneOnOne() {
		return this.type == ChannelType.OneOnOne;
	}
	
	public boolean isChatRoom() {
		return this.type == ChannelType.ChatRoom;
	}

	public ChannelType getType() {
		if (type == null) {
			return ChannelType.ChatRoom;
		}
		return type;
	}

	public void setType(ChannelType type) {
		this.type = type;
	}

	public List<User> getOperators() {
		return operators;
	}

	public boolean isOperator(User user) {
		return (user != null) && getOperators().contains(user);
	}

	public void setOperators(List<User> operators) {
		this.operators = operators;
	}

	public int getConnectedUsersCount() {
		return connectedUsersCount;
	}

	public void setConnectedUsersCount(int connectedUsersCount) {
		this.connectedUsersCount = connectedUsersCount;
	}

	public int getConnectedAdminsCount() {
		return connectedAdminsCount;
	}

	public void setConnectedAdminsCount(int connectedAdminsCount) {
		this.connectedAdminsCount = connectedAdminsCount;
	}

	public int getMessages() {
		return messages;
	}

	public void setMessages(int messages) {
		this.messages = messages;
	}
	
	@Override
	public void preDelete(EntityManager em) {
		super.preDelete(em);
		Query query = em.createQuery("Delete from ChatMessage p where p.channel = :channel");
		query.setParameter("channel", detach());
		query.executeUpdate();
		query = em.createQuery("Delete from ChannelAttachment p where p.channel = :channel");
		query.setParameter("channel", detach());
		query.executeUpdate();
	}
	
}
