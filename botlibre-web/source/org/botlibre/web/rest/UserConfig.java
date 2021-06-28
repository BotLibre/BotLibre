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
package org.botlibre.web.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.botlibre.web.admin.User;

/**
 * DTO for XML user creation config.
 */
@XmlRootElement(name="user")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserConfig extends Config {
	@XmlAttribute(name="new-password")
	public String newPassword;
	@XmlAttribute
	public String dateOfBirth;
	@XmlAttribute
	public String hint;
	@XmlAttribute
	public String name;
	@XmlAttribute
	public boolean showName;
	@XmlAttribute
	public String gender;
	@XmlAttribute
	public String properties;
	@XmlAttribute
	public String source;
	@XmlAttribute
	public String affiliate;
	@XmlAttribute
	public String userAccess;
	@XmlAttribute
	public String email;
	@XmlAttribute
	public Boolean emailMessages;
	@XmlAttribute
	public Boolean emailNotices;
	@XmlAttribute
	public Boolean emailSummary;
	@XmlAttribute
	public boolean isSubscribed;
	@XmlAttribute
	public String website;
	@XmlAttribute
	public boolean over18;
	@XmlAttribute
	public String connects;
	@XmlAttribute
	public String bots;
	@XmlAttribute
	public String forums;
	@XmlAttribute
	public String channels;
	@XmlAttribute
	public String posts;
	@XmlAttribute
	public String avatars;
	@XmlAttribute
	public String scripts;
	@XmlAttribute
	public String analytics;
	@XmlAttribute
	public String graphics;
	@XmlAttribute
	public String issues;
	@XmlAttribute
	public String issueTrackers;
	@XmlAttribute
	public String domains;
	@XmlAttribute
	public String friends;
	@XmlAttribute
	public String followers;
	@XmlAttribute
	public String affiliates;
	@XmlAttribute
	public String messages;
	@XmlAttribute
	public String joined;
	@XmlAttribute
	public String lastConnect;
	@XmlAttribute
	public String upgradeDate;
	@XmlAttribute
	public String expiryDate;
	@XmlAttribute
	public String type;
	@XmlAttribute
	public boolean isFlagged;
	@XmlAttribute
	public String credentialsType;
	@XmlAttribute
	public String credentialsUserID;
	@XmlAttribute
	public String credentialsToken;
	@XmlAttribute
	public String applicationId;
	@XmlAttribute
	public boolean newMessage;
	@XmlAttribute
	public String tags;
	
	public String bio;
	public String avatar;
	public String avatarThumb;
	public String adCode;
	public String flaggedReason;
	
	@XmlAttribute
	public boolean isBot;
	@XmlAttribute
	public String voice;
	@XmlAttribute
	public String voiceMod;
	@XmlAttribute
	public boolean nativeVoice;
	@XmlAttribute
	public String nativeVoiceName;
	@XmlAttribute
	public String nativeVoiceProvider;
	@XmlAttribute
	public String nativeVoiceApiKey;
	@XmlAttribute
	public String nativeVoiceAppId;
	@XmlAttribute
	public String voiceApiEndpoint;
	@XmlAttribute
	public String language;
	@XmlAttribute
	public String speechRate;
	@XmlAttribute
	public String pitch;
	@XmlAttribute
	public long instanceAvatarId;
	@XmlAttribute
	public String channelType;
	@XmlAttribute
	public String flaggedUser;
	@XmlAttribute
	public String users;
	@XmlAttribute
	public String viewUser;
	
	public UserConfig() {
		
	}
	
	public UserConfig(User user, boolean allFields, boolean friend) {
		this.user = user.getUserId();
		if (allFields) {
			this.token = String.valueOf(user.getToken());
			this.applicationId = String.valueOf(user.getApplicationId());
			this.hint = user.getHint();
			this.affiliate = user.getAffiliate();
			if (user.getCredentialsType() != null) {
				this.credentialsType = user.getCredentialsType().name();
			}
			this.source = user.getSource();
			this.email = user.getEmail();
			this.newMessage = user.getNewMessage();
			
			this.nativeVoiceApiKey = user.getNativeVoiceApiKey();
			this.nativeVoiceAppId = user.getNativeVoiceAppId();
			this.voiceApiEndpoint = user.getVoiceApiEndpoint();
			
			if (user.getUpgradeDate() != null) {
				this.upgradeDate = user.getUpgradeDate().toString();
			}
			if (user.getExpiryDate() != null) {
				this.expiryDate = user.getExpiryDate().toString();
			}
			this.affiliates = String.valueOf(user.getAffiliates());
			this.gender = user.getGender();
			this.properties = user.getProperties();
			this.isSubscribed = user.isSubscribed();
			this.emailMessages = user.getEmailMessages();
			this.emailNotices = user.getEmailNotices();
			this.emailSummary = user.getEmailSummary();
			this.voice = user.getVoice();
			this.voiceMod = user.getVoiceMod();
			this.nativeVoice = user.isNativeVoice();
			this.nativeVoiceName = user.getNativeVoiceName();
			this.nativeVoiceProvider = user.getNativeVoiceProvider();
			this.speechRate = user.getSpeechRate();
			this.pitch = user.getPitch();
			if (user.getInstanceAvatar() != null) {
				this.instanceAvatarId = user.getInstanceAvatar().getId();
			}
		}
		if (allFields || user.isPublic() || friend) {
			if (allFields || user.getShouldDisplayName()) {
				this.name = user.getName();
			}
			this.website = user.getWebsite();
			this.bio = user.getBio();
			this.over18 = user.isOver18();
			this.connects = String.valueOf(user.getConnects());
			this.posts = String.valueOf(user.getPosts());
			this.bots = String.valueOf(user.getInstances());
			this.channels = String.valueOf(user.getChannels());
			this.forums = String.valueOf(user.getForums());
			this.messages = String.valueOf(user.getMessages());
			this.avatars = String.valueOf(user.getAvatars());
			this.scripts = String.valueOf(user.getScripts());
			this.graphics = String.valueOf(user.getGraphics());
			this.issues = String.valueOf(user.getIssues());
			this.issueTrackers = String.valueOf(user.getIssueTrackers());
			this.analytics = String.valueOf(user.getAnalytics());
			this.domains = String.valueOf(user.getDomains());
			this.flaggedReason = user.getFlaggedReason();
			this.flaggedUser = user.getFlaggedUser();
			if (user.getCreationDate() != null) {
				this.joined = user.getCreationDate().toString();
			}
			if (user.getLastConnected() != null) {
				this.lastConnect = user.getLastConnected().toString();
			}
			this.type = user.getType().name();
			
			this.friends = String.valueOf(user.getFriends());
			this.followers = String.valueOf(user.getFollowers());
		}
		this.isFlagged = user.isFlagged();
		this.tags = user.getTagsString();
		this.isBot = user.isBot();
		this.language = user.getLanguage();
		this.showName = user.getShouldDisplayName();
		this.userAccess = user.getAccess().name();
	}
}
