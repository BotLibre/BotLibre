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
package org.botlibre.web.admin;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;

@Entity
@Table(name="USERS")
public class User implements Cloneable {
	static Random random = new Random();
	static int MAX_TAGS = 16;
	public static final String RESPONSIVEVOICE = "ResponsiveVoice";
	public static final String BINGSPEECH = "BingSpeech";
	public static final String QQSPEECH = "QQSpeech";
	@Id
	protected String userId = "";
	@Transient
	protected String password = "";
	protected byte[] encryptedPassword;
	protected long token;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date tokenReset;
	@Column(unique=true)
	protected Long applicationId;
	protected String hint = "";
	protected String name = "";
	protected String gender = "";
	@Column(length=1024)
	protected String properties = "";
	protected String ip = "";
	protected String source = "";
	protected String affiliate = "";
	protected UserType type = UserType.Basic;
	protected UserAccess access = UserAccess.Friends;
	protected CredentialsType credentialsType;
	protected String credentialsUserID;
	@Column(length=1024)
	protected String credentialsToken;
	protected String email = "";
	protected boolean emailNotices = true;
	protected boolean emailMessages = true;
	protected boolean emailSummary = true;
	protected boolean isSubscribed;
	protected String website = "";
	@Column(length=1024)
	protected String bio = "";
	@Column(length=1024)
	protected String adCode = "";
	protected boolean adCodeVerified = false;
	protected boolean isVerified;
	protected long verifyToken;
	protected boolean verifiedPayment;
	protected boolean shouldDisplayName = true;
	protected boolean over18;
	protected boolean superUser;
	protected int connects;
	protected int posts;
	protected int messages;
	protected boolean isBlocked;
	protected boolean isFlagged;
	protected boolean isDeleted;
	protected String flaggedReason;
	protected String flaggedUser;
	@Temporal(TemporalType.DATE)
	protected Date dateOfBirth;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date creationDate;
	@Transient
	protected Date oldLastConnected;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date lastConnected;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
	protected AvatarImage avatar;
	protected int instances;
	protected int forums;
	protected int channels;
	protected int scripts;
	protected int analytics;
	protected int domains;
	protected int avatars;
	protected int graphics;
	protected int issueTrackers;
	protected int issues;
	protected int affiliates;
	protected int friends;
	protected int followers;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date upgradeDate;
	protected int upgradeDuration = 12;
	@ManyToMany
	@JoinTable(name = "USER_PAYMENTS")
	protected List<UserPayment> payments = new ArrayList<UserPayment>();
	protected boolean isBot = false;
	@Transient
	protected boolean newMessage = false;
	protected String voice;
	protected String voiceMod;
	protected boolean nativeVoice;
	protected String nativeVoiceName;
	protected String nativeVoiceProvider;
	protected String nativeVoiceApiKey;
	protected String nativeVoiceAppId;
	protected String voiceApiEndpoint;
	protected String language;
	protected String speechRate;
	protected String pitch;
	protected Avatar instanceAvatar;
	protected boolean active = true;
	
	@ManyToMany
	@JoinTable(name = "USER_TAGS")
	protected List<Tag> tags = new ArrayList<Tag>();

	public enum UserType {Basic, Bronze, Gold, Platinum, Diamond, Partner, Admin, Avatar }

	public enum UserAccess {Private, Friends, Everyone}
	
	public enum CredentialsType {Facebook, Google, Apple}
	
	public User() { }

	public User(String userId, String password) {
		this.userId = userId;
		this.password = password;
		this.lastConnected = new Date();
		this.connects = 0;
	}
	
	public User(String userId) {
		this.userId = userId;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getLanguage() {
		if (language == null) {
			language = "en";
		}
		return language;
	}
	
	public String getGender() {
		if (gender == null) {
			return "";
		}
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
	
	public String getProperties() {
		if (gender == null) {
			return "";
		}
		return properties;
	}

	public void setProperties(String properties) {
		this.properties = properties;
	}
	
	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public String getVoiceMod() {
		return voiceMod;
	}

	public void setVoiceMod(String voiceMod) {
		this.voiceMod = voiceMod;
	}

	public boolean isNativeVoice() {
		return nativeVoice;
	}

	public void setNativeVoice(boolean nativeVoice) {
		this.nativeVoice = nativeVoice;
	}

	public String getNativeVoiceName() {
		if (nativeVoiceName == null) {
			nativeVoiceName = "";
		}
		return nativeVoiceName;
	}

	public void setNativeVoiceName(String nativeVoiceName) {
		this.nativeVoiceName = nativeVoiceName;
	}

	public String getNativeVoiceProvider() {
		return nativeVoiceProvider;
	}

	public void setNativeVoiceProvider(String nativeVoiceProvider) {
		this.nativeVoiceProvider = nativeVoiceProvider;
	}

	public String getNativeVoiceApiKey() {
		if (nativeVoiceApiKey == null) {
			nativeVoiceApiKey = "";
		}
		return nativeVoiceApiKey;
	}

	public void setNativeVoiceApiKey(String nativeVoiceApiKey) {
		this.nativeVoiceApiKey = nativeVoiceApiKey;
	}

	public String getNativeVoiceAppId() {
		if (nativeVoiceAppId == null) {
			nativeVoiceAppId = "";
		}
		return nativeVoiceAppId;
	}

	public void setNativeVoiceAppId(String nativeVoiceAppId) {
		this.nativeVoiceAppId = nativeVoiceAppId;
	}
	
	public String getVoiceApiEndpoint() {
		if(voiceApiEndpoint == null || voiceApiEndpoint.isEmpty()) {
			return "https://eastus.api.cognitive.microsoft.com/sts/v1.0/issueToken";
		}
		return voiceApiEndpoint;
	}

	public void setVoiceApiEndpoint(String endpoint) {
		this.voiceApiEndpoint = endpoint;
	}

	public String getSpeechRate() {
		return speechRate;
	}

	public void setSpeechRate(String speechRate) {
		this.speechRate = speechRate;
	}

	public String getPitch() {
		return pitch;
	}

	public void setPitch(String pitch) {
		this.pitch = pitch;
	}

	public Avatar getInstanceAvatar() {
		return instanceAvatar;
	}

	public void setInstanceAvatar(Avatar instanceAvatar) {
		this.instanceAvatar = instanceAvatar;
	}
	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getSource() {
		if (source == null) {
			return "";
		}
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

	public UserAccess getAccess() {
		if (access == null) {
			return UserAccess.Friends;
		}
		return access;
	}

	public void setAccess(UserAccess access) {
		this.access = access;
	}
	
	public boolean isPrivate() {
		return this.access == UserAccess.Private;
	}
	
	public boolean isPublic() {
		return this.access == UserAccess.Everyone;
	}
	
	public String getAffiliate() {
		if (affiliate == null) {
			return "";
		}
		return affiliate;
	}

	public void setAffiliate(String affiliate) {
		this.affiliate = affiliate;
	}

	public Date getUpgradeDate() {
		return upgradeDate;
	}

	public Date getExpiryDate() {
		if (this.upgradeDate == null) {
			return null;
		}
		Calendar date = Calendar.getInstance();
		date.setTime(this.upgradeDate);
		date.add(Calendar.MONTH, getUpgradeDuration());
		return date.getTime();
	}

	public boolean isExpired() {
		if (Site.COMMERCIAL || this.type == UserType.Basic || this.type == UserType.Partner || this.type == UserType.Admin) {
			return false;
		}
		if (this.upgradeDate == null) {
			long time = 0;
			for (UserPayment payment : getPayments()) {
				if (payment.paymentDate != null && payment.paymentDate.getTime() > time && !payment.userType.equals(UserType.Avatar)) {
					time = payment.paymentDate.getTime();
				}
			}
			if (time == 0) {
				return false;
			}
			this.upgradeDate = new Date(time);
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(this.upgradeDate);
		calendar.add(Calendar.MONTH, getUpgradeDuration());
		return new Date().getTime() > calendar.getTime().getTime();
	}

	public void setUpgradeDate(Date upgradeDate) {
		this.upgradeDate = upgradeDate;
	}

	public int getUpgradeDuration() {
		if (upgradeDuration == 0) {
			return 12;
		}
		return upgradeDuration;
	}

	public void setUpgradeDuration(int upgradeDuration) {
		this.upgradeDuration = upgradeDuration;
	}

	public boolean isAdCodeVerified() {
		return adCodeVerified;
	}

	public void setAdCodeVerified(boolean adCodeVerified) {
		this.adCodeVerified = adCodeVerified;
	}

	public CredentialsType getCredentialsType() {
		return credentialsType;
	}

	public void setCredentialsType(CredentialsType credentialsType) {
		this.credentialsType = credentialsType;
	}

	public String getCredentialsUserID() {
		return credentialsUserID;
	}

	public void setCredentialsUserID(String credentialsUserID) {
		this.credentialsUserID = credentialsUserID;
	}

	public String getCredentialsToken() {
		return credentialsToken;
	}

	public void setCredentialsToken(String credentialsToken) {
		this.credentialsToken = credentialsToken;
	}

	public boolean getVerifiedPayment() {
		return verifiedPayment;
	}

	public void setVerifiedPayment(boolean verifiedPayment) {
		this.verifiedPayment = verifiedPayment;
	}

	public Date getTokenReset() {
		return tokenReset;
	}

	public void setTokenReset(Date tokenReset) {
		this.tokenReset = tokenReset;
	}

	public String getUserHTML() {
		String id = this.userId;
		int index = this.userId.indexOf('@');
		if (index > 1) {
			id = this.userId.substring(0, index + 1);
		}
		return getUserHTML(id);
	}

	public String getUserHTML(String text) {
		UserType type = getType();
		if (type == UserType.Bronze) {
			return "<span class='bronze-user'>" + text + "</span>";
		} else if (type == UserType.Gold) {
			return "<span class='gold-user'>" + text + "</span>";
		} else if (type == UserType.Platinum) {
			return "<span class='platinum-user'>" + text + "</span>";
		} else if (type == UserType.Diamond) {
			return "<span class='diamond-user'>" + text + "</span>";
		} else if (this.type == UserType.Partner) {
			return "<span class='partner-user'>" + text + "</span>";
		} else if (this.type == UserType.Admin) {
			return "<span class='admin-user'>" + text + "</span>";
		} else {
			return "<span class='basic-user'>" + text + "</span>";
		}
	}

	public int getContentLimit() {
		if (this.type == UserType.Partner || this.type == UserType.Admin) {
			return 10000000;
		}
		if (Site.COMMERCIAL) {
			return Site.CONTENT_LIMIT;
		}
		UserType type = getType();
		if (type == UserType.Bronze) {
			return 20;
		} else if (type == UserType.Gold) {
			return 50;
		} else if (type == UserType.Platinum) {
			return 100;
		} else if (type == UserType.Diamond) {
			return 200;
		}
		return 10;
	}
	
	public UserType getType() {
		if (this.type == null || isExpired()) {
			return UserType.Basic;
		}
		return type;
	}

	public void setType(UserType type) {
		this.type = type;
	}

	public List<UserPayment> getPayments() {
		return payments;
	}

	public void setPayments(List<UserPayment> payments) {
		this.payments = payments;
	}

	public boolean hasAdCode() {
		return this.adCode != null && !this.adCode.isEmpty();
	}

	public String getAdCode() {
		if (this.adCode == null) {
			return "";
		}
		return adCode;
	}

	public int getGraphics() {
		return graphics;
	}

	public void setGraphics(int graphics) {
		this.graphics = graphics;
	}

	public void setAdCode(String adCode) {
		this.adCode = adCode;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}
	
	public boolean getEmailNotices() {
		return emailNotices;
	}

	public void setEmailNotices(boolean emailNotices) {
		this.emailNotices = emailNotices;
	}

	public boolean getEmailMessages() {
		return emailMessages;
	}

	public void setEmailMessages(boolean emailMessages) {
		this.emailMessages = emailMessages;
	}

	public boolean getEmailSummary() {
		return emailSummary;
	}

	public void setEmailSummary(boolean emailSummary) {
		this.emailSummary = emailSummary;
	}

	public long getVerifyToken() {
		return verifyToken;
	}

	public void setVerifyToken(long verifyToken) {
		this.verifyToken = verifyToken;
	}

	public boolean isBot() {
		return isBot;
	}

	public void setIsBot(boolean isBot) {
		this.isBot = isBot;
	}

	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}

	public boolean isVerified() {
		return isVerified && active;
	}

	public boolean isBasic() {
		return getType() == UserType.Basic;
	}

	public boolean isBronze() {
		return getType() == UserType.Bronze;
	}

	public boolean isGold() {
		return getType() == UserType.Gold;
	}

	public boolean isPlatinum() {
		return getType() == UserType.Platinum;
	}

	public boolean isDiamond() {
		return getType() == UserType.Diamond;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}

	public User clone() {
		try {
			return (User)super.clone();
		} catch (CloneNotSupportedException ignore) {
			throw new InternalError(ignore.getMessage());
		}
	}
	
	public void checkConstraints() {
		if ((this.password != null && (this.password.length() >= 255)) || (this.name.length() >= 100)
				|| (this.website.length() >= 255) || (this.bio.length() >= 1024)) {
			throw new BotException("Text size limit exceeded");
		}
		String name = this.name;
		String hint = this.hint;
		String source = this.source;
		String affiliate = this.affiliate;
		String bio = this.bio;
		String website = this.website;
		String email = this.email;
		this.name = Utils.sanitize(this.name);
		this.hint = Utils.sanitize(this.hint);
		this.source = Utils.sanitize(this.source);
		this.affiliate = Utils.sanitize(this.affiliate);
		this.bio = Utils.sanitize(this.bio);
		this.website = Utils.sanitize(this.website);
		
		Utils.checkHTML(name);
		Utils.checkHTML(hint);
		Utils.checkHTML(source);
		Utils.checkHTML(affiliate);
		Utils.checkScript(bio);
		Utils.checkHTML(website);
		Utils.checkHTML(email);
	}

	public boolean checkProfanity() {
		return Utils.checkProfanity(this.userId) || Utils.checkProfanity(this.name) || Utils.checkProfanity(this.email)
				|| Utils.checkProfanity(this.website) || Utils.checkProfanity(this.bio);
	}

	public AvatarImage getAvatar() {
		return avatar;
	}

	public void setAvatar(AvatarImage avatar) {
		this.avatar = avatar;
	}
	
	public int getConnects() {
		return connects;
	}

	public void setConnects(int connects) {
		this.connects = connects;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public String getPassword() {
		if (password == null) {
			return "";
		}
		return password;
	}
	
	public void setPassword(String password) {
		if (password == null) {
			password = "";
		}
		this.password = password;
	}
	
	public byte[] getEncryptedPassword() {
		return encryptedPassword;
	}

	public void setEncryptedPassword(byte[] encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (name == null) {
			name = "";
		}
		this.name = name;
	}
	
	public Date getLastConnected() {
		return lastConnected;
	}
	
	public void setLastConnected(Date lastConnected) {
		this.oldLastConnected = this.lastConnected;
		this.lastConnected = lastConnected;
	}
	
	public int getInstances() {
		return instances;
	}
	
	public void setInstances(int instances) {
		this.instances = instances;
	}
		
	public int getForums() {
		return forums;
	}

	public void setForums(int forums) {
		this.forums = forums;
	}

	public int getIssueTrackers() {
		return issueTrackers;
	}

	public void setIssueTrackers(int issueTrackers) {
		this.issueTrackers = issueTrackers;
	}

	public int getChannels() {
		return channels;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public int getScripts() {
		return scripts;
	}

	public void setScripts(int scripts) {
		this.scripts = scripts;
	}

	public int getAnalytics() {
		return analytics;
	}

	public void setAnalytics(int analytics) {
		this.analytics = analytics;
	}

	public int getDomains() {
		return domains;
	}

	public void setDomains(int domains) {
		this.domains = domains;
	}

	public int getAvatars() {
		return avatars;
	}

	public void setAvatars(int avatars) {
		this.avatars = avatars;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		if (hint == null) {
			hint = "";
		}
		this.hint = hint;
	}

	public boolean hasEmail() {
		return email != null && email.contains("@");
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email == null) {
			email = "";
		}
		this.email = email;
	}

	public boolean isOver18() {
		return over18;
	}

	public void setOver18(boolean over18) {
		this.over18 = over18;
	}

	public Date getOldLastConnected() {
		if (this.oldLastConnected == null) {
			return this.lastConnected;
		}
		return this.oldLastConnected;
	}

	public void setOldLastConnected(Date oldLastConnected) {
		this.oldLastConnected = oldLastConnected;
	}
	
	public boolean isSuperUser() {
		return superUser;
	}
	
	public boolean isAdminUser() {
		return this.type == UserType.Admin || this.superUser;
	}
	
	public boolean isPartnerUser() {
		return this.type == UserType.Partner;
	}

	public void setSuperUser(boolean superUser) {
		this.superUser = superUser;
	}

	public long getToken() {
		return token;
	}

	public void setToken(long token) {
		this.token = token;
	}

	public void resetToken() {
		this.token = Math.abs(random.nextLong());
		this.tokenReset = new Date();
	}

	public void resetVerifyToken() {
		this.verifyToken = Math.abs(random.nextLong());
	}

	public Long getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Long applicationId) {
		this.applicationId = applicationId;
	}

	public void resetApplicationId() {
		this.applicationId = Math.abs(random.nextLong());
	}

	public void checkApplicationId() {
		if (this.applicationId == null) {
			resetApplicationId();
		}
	}

	public String getWebsiteURL() {
		if (getWebsite().isEmpty()) {
			return null;
		}
		if (this.website.charAt(0) == 'h') {
			return "<a target=\"_blank\" rel=\"nofollow\" href=\"" + this.website + "\">" + this.website + "</a>";
		} else if (this.website.charAt(0) == '<') {
			return this.website;			
		} else {
			return "<a target=\"_blank\" rel=\"nofollow\" href=\"http://" + this.website + "\">" + this.website + "</a>";			
		}
	}

	public String getWebsite() {
		if (this.website == null) {
			return "";
		}
		return website;
	}

	public void setWebsite(String website) {
		if (website == null) {
			website = "";
		}
		this.website = website;
	}

	public boolean getShouldDisplayName() {
		return shouldDisplayName;
	}

	public void setShouldDisplayName(boolean displayName) {
		this.shouldDisplayName = displayName;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getIssues() {
		return issues;
	}

	public void setIssues(int issues) {
		this.issues = issues;
	}

	public String getBio() {
		if (this.bio == null) {
			return "";
		}
		return bio;
	}

	public String getBioText() {
		return Utils.formatHTMLOutput(getBio());
	}

	public void setBio(String bio) {
		if (bio == null) {
			bio = "";
		}
		this.bio = bio;
	}

	@Override
	public int hashCode() {
		return getUserId().hashCode();
	}

	@Override
	public boolean equals(Object user) {
		if (!(user instanceof User)) {
			return false;
		}
		return ((User)user).getUserId().equals(getUserId());
	}

	public void setNewMessage(boolean newMessage) {
		this.newMessage = newMessage;
	}
	
	public boolean getNewMessage() {
		return newMessage;
	}
	
	public boolean isFlagged() {
		return isFlagged;
	}

	public void setFlagged(boolean isFlagged) {
		this.isFlagged = isFlagged;
	}

	public String getFlaggedReason() {
		return flaggedReason;
	}

	public void setFlaggedReason(String flaggedReason) {
		this.flaggedReason = flaggedReason;
	}

	public String getFlaggedUser() {
		return flaggedUser;
	}

	public void setFlaggedUser(String flaggedUser) {
		this.flaggedUser = flaggedUser;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public void setBlocked(boolean isBlocked) {
		this.isBlocked = isBlocked;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public User detach() {
		User user = new User(this.userId);
		user.type = this.type;
		if (this.shouldDisplayName) {
			user.name = this.name;
		}
		return user;
	}
	
	public int getFriends() {
		return friends;
	}

	public void setFriends(int friends) {
		this.friends = friends;
	}

	public int getFollowers() {
		return followers;
	}

	public void setFollowers(int followers) {
		this.followers = followers;
	}

	public int getAffiliates() {
		return affiliates;
	}

	public void setAffiliates(int affiliates) {
		this.affiliates = affiliates;
	}

	public int getMessages() {
		return messages;
	}

	public void setMessages(int messages) {
		this.messages = messages;
	}
	
	public String getTagsString() {
		if (this.tags == null || this.tags.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Tag tag : this.tags) {
			writer.write(tag.getName());
			count++;
			if (count < this.tags.size()) {
				writer.write(", ");
			}
		}
		return writer.toString();
	}
	
	@SuppressWarnings("unchecked")
	public void setTagsFromString(String csv, EntityManager em) {
		for (Tag tag : this.tags) {
			tag.setCount(tag.getCount() - 1);
		}
		if (csv == null || csv.length() == 0) {
			this.tags = new ArrayList<Tag>();
			return;
		}
		List<Tag> newTags = new ArrayList<Tag>();
		List<String> values = Utils.csv(csv.toLowerCase());
		if (values.size() > MAX_TAGS) {
			throw new BotException("Only " + MAX_TAGS + " tags are allowed");
		}
		for (String word : values) {
			List<Tag> results = em
					.createQuery("Select t from Tag t where t.name = :name and t.type = :type")
					.setParameter("type", "User")
					.setParameter("name", word)
					.getResultList();
			Tag tag = null;
			if (results.isEmpty()) {
				tag = new Tag();
				tag.setName(word);
				tag.setType("User");
				tag.setDomain(null);
				em.persist(tag);
			} else {
				tag = results.get(0);
			}
			if (!newTags.contains(tag)) {
				newTags.add(tag);
				tag.setCount(tag.getCount() + 1);
			}
		}
		this.tags = newTags;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + this.userId + ")";
	}
}
