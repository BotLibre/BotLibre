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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.eclipse.persistence.annotations.Index;
import org.eclipse.persistence.annotations.PrivateOwned;

import org.botlibre.web.Site;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.rest.WebMediumConfig;

@MappedSuperclass
public abstract class WebMedium extends Flaggable {
	static int MAX_ERRORS = 100;
	static int MAX_CATEGORIES = 10;
	static int MAX_TAGS = 16;
	
	@Index
	protected String name;
	@Index
	protected String alias;
	protected boolean isPrivate;
	protected boolean isAdult = Site.ADULT;
	protected boolean isTemplate;
	protected boolean allowForking;
	protected boolean enableTwitter;
	protected boolean enableFacebook;
	protected boolean enableTelegram;
	protected boolean enableSlack;
	protected boolean enableSkype;
	protected boolean enableWeChat;
	protected boolean enableKik;
	protected boolean enableWolframAlpha;
	protected boolean enableEmail;
	protected boolean enableAlexa;
	protected boolean enableGoogleAssistant;
	protected boolean enableTimers;
	protected boolean isFeatured;
	protected boolean isHidden;
	protected boolean isExternal;
	protected boolean isPaphus;
	protected boolean isReviewed;
	protected String reviewRejectionComments;
	protected boolean showAds = true;
	protected boolean adCodeVerified = false;
	protected boolean contentVerified = false;
	protected AccessMode accessMode = AccessMode.Everyone;
	protected AccessMode forkAccessMode = AccessMode.Administrators;
	protected Long parentId;
	protected ContentRating contentRating = ContentRating.Teen;
	@Column(length=1024)
	protected String description;
	@Column(length=1024)
	protected String details;
	@Column(length=1024)
	protected String disclaimer;
	protected String website;
	@Column(length=1024)
	protected String license;
	@Column(length=1024)
	protected String adCode;
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.LAZY)
	protected AvatarImage avatar;
	protected String lastConnectedUser;
	@Transient
	protected String oldLastConnectedUser;
	@Temporal(TemporalType.TIMESTAMP)
	protected Date lastConnected;
	@Transient
	protected Date oldLastConnected;
	protected int connects;
	protected int restConnects;
	protected int dailyConnects;
	protected int weeklyConnects;
	protected int monthlyConnects;
	protected int restDailyConnects;
	protected int restWeeklyConnects;
	protected int restMonthlyConnects;
	@OneToOne(fetch=FetchType.LAZY)
	protected User creator;
	@OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.PERSIST)
	@PrivateOwned
	protected DomainForwarder domainForwarder;	
	@ManyToMany
	@JoinTable(name = "MEDIUM_ADMINS")
	protected List<User> admins = new ArrayList<User>();
	@ManyToMany
	@JoinTable(name = "MEDIUM_USERS")
	protected List<User> users = new ArrayList<User>();
	@ManyToMany
	@JoinTable(name = "MEDIUM_TAGS")
	protected List<Tag> tags = new ArrayList<Tag>();
	@Transient
	protected String tagsString;
	@ManyToMany
	@JoinTable(name = "MEDIUM_CATEGORIES")
	protected List<Category> categories = new ArrayList<Category>();
	@Transient
	protected String categoriesString;
	@ManyToMany
	@JoinTable(name = "MEDIUM_ERRORS")
	@OrderBy("creationDate")
	protected List<ErrorMessage> errors = new ArrayList<ErrorMessage>();
	
	public WebMedium() {
	}

	public WebMedium(String name) {
		this.name = name;
		this.alias = "";
		this.description = "";
		this.details = "";
		this.disclaimer = "";
		this.license = "";
		this.lastConnected = new Date();
		this.lastConnectedUser = "";
		this.website = "";
		this.adCode = "";
	}
	
	public abstract WebMediumConfig buildConfig();
	
	public abstract WebMediumConfig buildBrowseConfig();

	public void toConfig(WebMediumConfig config) {
		config.id = String.valueOf(this.id);
		config.name = this.name;
		config.alias = this.alias;
		config.isAdult = this.isAdult;
		config.isPrivate = this.isPrivate;
		config.isHidden = this.isHidden;
		if (this.accessMode != null) {
			config.accessMode = this.accessMode.name();
		}
		if (this.forkAccessMode != null) {
			config.forkAccessMode = this.forkAccessMode.name();
		}
		if (this.contentRating != null) {
			config.contentRating = this.contentRating.name();
		}
		config.description = Utils.stripTags(this.description);
		config.details = Utils.stripTags(this.details);
		config.disclaimer = Utils.stripTags(this.disclaimer);
		config.categories = Utils.stripTags(getCategoriesString());
		config.tags = Utils.stripTags(getTagsString());
		config.isFlagged = this.isFlagged;
		config.isReviewed = this.isReviewed;
		config.isExternal = this.isExternal;
		config.isPaphus = this.isPaphus;
		config.flaggedReason = this.flaggedReason;
		config.reviewRejectionComments = this.reviewRejectionComments;
		if (this.creator != null) {
			config.creator = this.creator.getUserId();
		}
		if (this.creationDate != null) {
			config.creationDate = this.creationDate.toString();
		}
		config.website = this.website;
		config.subdomain = getSubdomain();
		config.license = Utils.stripTags(this.license);
		config.showAds = this.showAds;
		config.connects = String.valueOf(this.connects);
		config.dailyConnects = String.valueOf(this.dailyConnects);
		config.weeklyConnects = String.valueOf(this.weeklyConnects);
		config.monthlyConnects = String.valueOf(this.monthlyConnects);
		config.thumbsUp = String.valueOf(this.thumbsUp);
		config.thumbsDown = String.valueOf(this.thumbsDown);
		config.stars = String.valueOf(this.stars);
	}
	
	public void toBrowseConfig(WebMediumConfig config) {
		config.id = String.valueOf(this.id);
		config.name = this.name;
		config.alias = this.alias;
		config.isAdult = this.isAdult;
		config.isPrivate = this.isPrivate;
		config.isHidden = this.isHidden;
		if (this.accessMode != null) {
			config.accessMode = this.accessMode.name();
		}
		if (this.forkAccessMode != null) {
			config.forkAccessMode = this.forkAccessMode.name();
		}
		if (this.contentRating != null) {
			config.contentRating = this.contentRating.name();
		}
		config.description = Utils.stripTags(this.description);
		config.details = Utils.stripTags(this.details);
		config.disclaimer = Utils.stripTags(this.disclaimer);
		config.categories = Utils.stripTags(getCategoriesString());
		config.tags = Utils.stripTags(getTagsString());
		
		config.isFlagged = this.isFlagged;
		config.isReviewed = this.isReviewed;
		config.isExternal = this.isExternal;
		config.isPaphus = this.isPaphus;
		if (this.creator != null) {
			config.creator = this.creator.getUserId();
		}
		if (this.creationDate != null) {
			config.creationDate = this.creationDate.toString();
		}
		config.website = this.website;
		config.subdomain = getSubdomain();
		config.license = Utils.stripTags(this.license);
		
		config.connects = String.valueOf(this.connects);
		config.dailyConnects = String.valueOf(this.dailyConnects);
		config.weeklyConnects = String.valueOf(this.weeklyConnects);
		config.monthlyConnects = String.valueOf(this.monthlyConnects);
		config.thumbsUp = String.valueOf(this.thumbsUp);
		config.thumbsDown = String.valueOf(this.thumbsDown);
		config.stars = String.valueOf(this.stars);
	}

	public boolean isReviewed() {
		return isReviewed;
	}

	public void setReviewed(boolean isReviewed) {
		this.isReviewed = isReviewed;
	}

	public String getReviewRejectionComments() {
		if (reviewRejectionComments == null) {
			return "";
		}
		return reviewRejectionComments;
	}

	public void setReviewRejectionComments(String reviewRejectionComments) {
		this.reviewRejectionComments = reviewRejectionComments;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public boolean isContentVerified() {
		return contentVerified;
	}

	public void setContentVerified(boolean contentVerified) {
		this.contentVerified = contentVerified;
	}

	public AccessMode getForkAccessMode() {
		if (this.forkAccessMode == null) {
			if (this.allowForking) {
				return AccessMode.Users;
			}
			return AccessMode.Administrators;
		}
		return forkAccessMode;
	}

	public void setForkAccessMode(AccessMode forkAccessMode) {
		this.forkAccessMode = forkAccessMode;
	}

	public ContentRating getContentRating() {
		if (this.contentRating == null) {
			if (Site.ADULT) {
				return ContentRating.Adult;
			}
			return ContentRating.Teen;
		}
		return contentRating;
	}

	public int getContentRatingLevel() {
		if (this.contentRating == null) {
			if (Site.ADULT) {
				return Utils.ADULT;
			}
			return Utils.TEEN;
		} else if (this.contentRating == ContentRating.Everyone) {
			return Utils.EVERYONE;
		} else if (this.contentRating == ContentRating.Mature) {
			return Utils.MATURE;
		} else if (this.contentRating == ContentRating.Adult) {
			return Utils.ADULT;
		} else {
			return Utils.TEEN;
		}
	}

	public void setContentRating(ContentRating contentRating) {
		this.contentRating = contentRating;
	}

	public boolean isAdCodeVerified() {
		return adCodeVerified;
	}

	public void setAdCodeVerified(boolean adCodeVerified) {
		this.adCodeVerified = adCodeVerified;
	}

	public String getForwarderAddress() {
		return "";
	}
	
	public boolean getEnableFacebook() {
		return enableFacebook;
	}

	public void setEnableFacebook(boolean enableFacebook) {
		this.enableFacebook = enableFacebook;
	}
	
	public boolean getEnableTelegram() {
		return enableTelegram;
	}

	public void setEnableTelegram(boolean enableTelegram) {
		this.enableTelegram = enableTelegram;
	}
	
	public void setEnableSlack(boolean enableSlack) {
		this.enableSlack = enableSlack;
	}
	
	public void setEnableSkype(boolean enableSkype) {
		this.enableSkype = enableSkype;
	}
	
	public void setEnableWeChat(boolean enableWeChat) {
		this.enableWeChat = enableWeChat;
	}
	
	public void setEnableKik(boolean enableKik) {
		this.enableKik = enableKik;
	}
	
	public void setEnableWolframAlpha(boolean enableWolframAlpha) {
		this.enableWolframAlpha = enableWolframAlpha;
	}

	public boolean getShowAds() {
		return showAds;
	}

	public void setShowAds(boolean showAds) {
		this.showAds = showAds;
	}

	public String getAdCode() {
		if (adCode == null) {
			return "";
		}
		return adCode;
	}

	public void setAdCode(String adCode) {
		this.adCode = adCode;
	}
	
	public boolean hasAdCode() {
		return this.adCode != null && !this.adCode.isEmpty();
	}

	public void addError(ErrorMessage error) {
		if (this.errors.size() > MAX_ERRORS) {
			this.errors.clear();
		}
		this.errors.add(error);
	}

	public List<ErrorMessage> getErrors() {
		return errors;
	}

	public void setErrors(List<ErrorMessage> errors) {
		this.errors = errors;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public boolean isPaphus() {
		return isPaphus;
	}

	public void setPaphus(boolean isPaphus) {
		this.isPaphus = isPaphus;
	}

	public String getWebsite() {
		if (this.website == null) {
			return "";
		}
		return website;
	}
	
	public String getWebsiteURL() {
		return Utils.checkURL(this.website);
	}

	public void setWebsite(String website) {
		this.website = Utils.checkURL(website);;
	}

	public String getOldLastConnectedUser() {
		if (this.oldLastConnectedUser == null) {
			return this.lastConnectedUser;
		}
		return oldLastConnectedUser;
	}

	public void setOldLastConnectedUser(String oldLastConnectedUser) {
		this.oldLastConnectedUser = oldLastConnectedUser;
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

	public String getLastConnectedUser() {
		return lastConnectedUser;
	}

	public void setLastConnectedUser(String lastConnectedUser) {
		this.oldLastConnectedUser = this.lastConnectedUser;
		this.lastConnectedUser = lastConnectedUser;
	}

	public Date getLastConnected() {
		return lastConnected;
	}

	public void setLastConnected(Date lastConnected) {
		this.oldLastConnected = this.lastConnected;
		this.lastConnected = lastConnected;
	}
	
	public boolean getEnableAlexa() {
		return enableAlexa;
	}

	public void setEnableAlexa(boolean enableAlexa) {
		this.enableAlexa = enableAlexa;
	}
	
	public boolean getEnableGoogleAssistant() {
		return enableGoogleAssistant;
	}

	public void setEnableGoogleAssistant(boolean enableGoogleAssistant) {
		this.enableGoogleAssistant = enableGoogleAssistant;
	}

	public boolean getEnableEmail() {
		return enableEmail;
	}

	public void setEnableEmail(boolean enableEmail) {
		this.enableEmail = enableEmail;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public AccessMode getAccessMode() {
		if (accessMode == null) {
			return AccessMode.Everyone;
		}
		return accessMode;
	}

	public void setAccessMode(AccessMode accessMode) {
		this.accessMode = accessMode;
	}

	@Override
	public boolean checkProfanity() {
		super.checkProfanity();
		return Utils.checkProfanity(this.alias) || Utils.checkProfanity(this.name) || Utils.checkProfanity(this.description)
				|| Utils.checkProfanity(this.details) || Utils.checkProfanity(this.disclaimer) || Utils.checkProfanity(this.license);
	}

	@Override
	public void checkConstraints() {
		super.checkConstraints();
		if (this.alias == null || this.alias.isEmpty()) {
			// Default alias to name, lowercase.
			this.alias = this.name.replace(" ", "").toLowerCase();
		}
		if ((this.description.length() >= 1024) || (this.details.length() >= 1024)
				|| (this.disclaimer.length() >= 1024)
				|| (this.license.length() >= 1024)
				|| (this.name.length() >= 100)
				|| (this.alias.length() >= 64)) {
			throw new BotException("Text size limit exceeded");
		}
		Utils.checkHTML(this.name);
		Utils.checkHTML(this.license);
		Utils.checkHTML(this.website);
		Utils.checkScript(this.disclaimer);
		Utils.checkScript(this.details);
		Utils.checkScript(this.description);
		this.name = Utils.sanitize(this.name);
		this.alias = Utils.sanitize(this.alias);
		this.license = Utils.sanitize(this.license);
		this.website = Utils.sanitize(this.website);
		this.disclaimer = Utils.sanitize(this.disclaimer);
		this.details = Utils.sanitize(this.details);
		this.description = Utils.sanitize(this.description);

		if (Site.REVIEW_CONTENT && getDomain() != null && !getDomain().isPrivate() && !getDomain().isHidden() && !isHidden() && !isPrivate()) {
			if (getId() != null && (getDescription() == null || getDescription().isEmpty())) {
				throw new BotException("You must set a description");
			}
		}
	}

	public boolean isAdmin(User user) {
		return (user != null) && (user.isSuperUser() || getAdmins().contains(user));
	}

	public boolean isUser(User user) {
		return (user != null) && getUsers().contains(user);
	}

	public void checkAccess(User user) {
		if ((this.domain != null) && ((user == null) || !(user.isSuperUser() || user.isAdminUser()))) {
			this.domain.checkExpired();
		}
		if (!this.isPrivate && (this.accessMode == null || this.accessMode == AccessMode.Everyone)) {
			return;
		} else if (user == null) {
			throw new BotException("This " + getDisplayName() + " does not allow anonymous access.");
		} else if (this.isPrivate || (this.accessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				throw new BotException("You must be a member to access this " + getDisplayName() + ".");
			}
		} else if (this.accessMode == AccessMode.Users) {
			return;
		} else {
			if (!isAdmin(user)) {
				throw new BotException("You must be an administrator to access this " + getDisplayName() + ".");
			}
		}
	}

	public void checkForkAccess(User user) {
		if (getForkAccessMode() == AccessMode.Users) {
			return;
		} else if (getForkAccessMode() == AccessMode.Members) {
			if (!isUser(user) && !isAdmin(user)) {
				throw new BotException("You must be a member to fork this " + getDisplayName() + ".");
			}
		} else if (getForkAccessMode() == AccessMode.Disabled) {
			throw new BotException("Forking is disabled for this " + getDisplayName() + ".");
		} else {
			if (!isAdmin(user)) {
				throw new BotException("You must be an administrator to fork this " + getDisplayName() + ".");
			}
		}
	}

	public boolean isAllowed(User user) {
		if (!this.isPrivate && (this.accessMode == null) || (this.accessMode == AccessMode.Everyone)) {
			return true;
		} else if (user == null) {
			return false;
		} else if (this.isPrivate || (this.accessMode == AccessMode.Members)) {
			if (!isUser(user) && !isAdmin(user)) {
				return false;
			}
		} else if (this.accessMode == AccessMode.Administrators) {
			if (!isAdmin(user)) {
				return false;
			}
		}
		return true;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public String getLicense() {
		if (license == null) {
			return "";
		}
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getCreatorUserId() {
		if (getCreator() == null) {
			return "";
		}
		return getCreator().getUserId();
	}

	public String getAdminUserId() {
		if (getAdmins().isEmpty()) {
			return "";
		}
		return getAdmins().get(0).getUserId();
	}

	public List<User> getAdmins() {
		return admins;
	}

	public void setAdmins(List<User> admins) {
		this.admins = admins;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public int getConnects() {
		return connects;
	}

	public void setConnects(int connects) {
		this.connects = connects;
	}

	public int getDailyConnects() {
		return dailyConnects;
	}

	public void setDailyConnects(int dailyConnects) {
		this.dailyConnects = dailyConnects;
	}

	public int getWeeklyConnects() {
		return weeklyConnects;
	}

	public void setWeeklyConnects(int weeklyConnects) {
		this.weeklyConnects = weeklyConnects;
	}

	public int getMonthlyConnects() {
		return monthlyConnects;
	}

	public void setMonthlyConnects(int monthlyConnects) {
		this.monthlyConnects = monthlyConnects;
	}

	public int getRestDailyConnects() {
		return restDailyConnects;
	}

	public void setRestDailyConnects(int restDailyConnects) {
		this.restDailyConnects = restDailyConnects;
	}

	public int getRestWeeklyConnects() {
		return restWeeklyConnects;
	}

	public void setRestWeeklyConnects(int restWeeklyConnects) {
		this.restWeeklyConnects = restWeeklyConnects;
	}

	public int getRestMonthlyConnects() {
		return restMonthlyConnects;
	}

	public void setRestMonthlyConnects(int restMonthlyConnects) {
		this.restMonthlyConnects = restMonthlyConnects;
	}

	public int getRestConnects() {
		return restConnects;
	}

	public void setRestConnects(int restConnects) {
		this.restConnects = restConnects;
	}

	public void incrementConnects(ClientType clientType, User user) {
		if (user != null) {
			setLastConnectedUser(user.getUserId());
		} else {
			setLastConnectedUser("Anonymous");
		}
		setLastConnected(new Date());
		this.connects = this.connects + 1;
		this.dailyConnects = this.dailyConnects + 1;
		this.weeklyConnects = this.weeklyConnects + 1;
		this.monthlyConnects = this.monthlyConnects + 1;
		if (clientType == ClientType.REST) {
			this.restConnects = this.restConnects + 1;
			this.restDailyConnects = this.restDailyConnects + 1;
			this.restWeeklyConnects = this.restWeeklyConnects + 1;
			this.restMonthlyConnects = this.restMonthlyConnects + 1;
		}
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public boolean equals(Object instance) {
		if (!(instance instanceof WebMedium)) {
			return false;
		}
		WebMedium content = (WebMedium)instance;
		if (content.getId() == null || getId() == null) {
			return super.equals(instance);
		}
		if (content.getId().equals(getId())) {
			return true;
		}
		return false;
	}
	
	public String getDescriptionText() {
		return Utils.formatHTMLOutput(getDescription());
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAlias() {
		if (this.alias == null) {
			return this.name;
		}
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getName() {
		return name;
	}

	public String getNameHTML() {
		if (this.creator == null) {
			return this.name;
		}
		return this.creator.getUserHTML(this.name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public AvatarImage getAvatar() {
		return avatar;
	}

	public void setAvatar(AvatarImage avatar) {
		this.avatar = avatar;
	}

	/**
	 * Return the cache tags string for editing or creating a new object.
	 */
	public String getEditTagsString() {
		if (this.tagsString != null) {
			return this.tagsString;
		}
		return getTagsString();
	}

	public String getTagsString() {
		if (this.tags.isEmpty()) {
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
		this.tagsString = writer.toString();
		return this.tagsString;
	}

	public void setTagsString(String tagsString) {
		this.tagsString = tagsString;
	}

	public void setCategoriesString(String categoriesString) {
		this.categoriesString = categoriesString;
	}

	public String getCategoriesString() {
		if (this.categoriesString != null) {
			return categoriesString;
		}
		if (this.categories.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Category category : this.categories) {
			writer.write(category.getName());
			count++;
			if (count < this.categories.size()) {
				writer.write(", ");
			}
		}
		return writer.toString();
	}

	public String getTagLinks(String uri) {
		if (this.tags.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Tag tag : this.tags) {
			writer.write("<a href=\"");
			writer.write(uri);
			writer.write(LoginBean.encode(String.valueOf(tag.getName())));
			writer.write("\">");
			writer.write(tag.getName());
			writer.write("</a>");
			count++;
			if (count < this.tags.size()) {
				writer.write(", ");
			}
		}
		return writer.toString();
	}

	public String getCategoryLinks(String uri) {
		if (this.categories.isEmpty()) {
			return "";
		}
		StringWriter writer = new StringWriter();
		int count = 0;
		for (Category category : this.categories) {
			writer.write("<a href=\"");
			writer.write(uri);
			writer.write(LoginBean.encode(String.valueOf(category.getName())));
			writer.write("\">");
			writer.write(category.getName());
			writer.write("</a>");
			count++;
			if (count < this.categories.size()) {
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
			this.tagsString = null;
			return;
		}
		List<Tag> newTags = new ArrayList<Tag>();
		List<String> values = Utils.csv(csv.toLowerCase());
		if (values.size() > MAX_TAGS) {
			throw new BotException("Only " + MAX_TAGS + " tags are allowed");
		}
		for (String word : values) {
			List<Tag> results = em
					.createQuery("Select t from Tag t where t.name = :name and t.type = :type and t.domain = :domain")
					.setParameter("type", getTypeName())
					.setParameter("name", word)
					.setParameter("domain", getDomain())
					.getResultList();
			Tag tag = null;
			if (results.isEmpty()) {
				tag = new Tag();
				tag.setName(word);
				tag.setType(getTypeName());
				tag.setDomain(getDomain());
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
		this.tagsString = null;
	}

	@SuppressWarnings("unchecked")
	public void setCategoriesFromString(String csv, EntityManager em) {
		if (!Site.COMMERCIAL && csv == null) {
			throw new BotException("You must choose at least one category");
		}
		List<Category> newCategories = new ArrayList<Category>();
		List<String> values = Utils.csv(csv);
		if (values.size() > MAX_CATEGORIES) {
			throw new BotException("Only " + MAX_CATEGORIES + " categories are allowed");
		}
		for (String word : values) {
			List<Category> results = em
					.createQuery("Select t from Category t where t.name = :name and t.type = :type and t.domain = :domain")
					.setParameter("type", getTypeName())
					.setParameter("name", word)
					.setParameter("domain", getDomain())
					.getResultList();
			Category category = null;
			if (results.isEmpty()) {
				if (getCreator().isAdminUser()) {
					category = new Category();
					category.setName(word);
					category.setDescription("");
					category.setType(getTypeName());
					category.setDomain(getDomain());
					category.setCreationDate(new Date());
					category.setCreator(getCreator());
					em.persist(category);
				} else {
					throw new BotException("Category " + word + " does not exist");
				}
			} else {
				category = results.get(0);
			}
			if (category.isSecured() && getCreator() != null && !(getCreator().isAdminUser() || getCreator().equals(category.getCreator()))) {
				throw new BotException("Only admins can use this category - " + word);
			}
			if (!newCategories.contains(category)) {
				newCategories.add(category);
			}
		}
		if (Site.REVIEW_CONTENT && getDomain() != null && !getDomain().isPrivate() && !getDomain().isHidden() && !isHidden() && !isPrivate() && newCategories.isEmpty() && !(this instanceof Domain)) {
			throw new BotException("You must choose at least one category");
		}
		List<Category> ancestors = new ArrayList<Category>();
		for (Category category : newCategories) {
			category.addAncestors(ancestors);
		}
		this.categories = ancestors;
		this.categoriesString = null;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	public String getDetails() {
		if (details == null) {
			return "";
		}
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getDisclaimer() {
		if (disclaimer == null) {
			return "";
		}
		return disclaimer;
	}

	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}

	public boolean isFeatured() {
		return isFeatured;
	}

	public void setFeatured(boolean isFeatured) {
		this.isFeatured = isFeatured;
	}

	public boolean isAdult() {
		return isAdult;
	}

	public void setAdult(boolean isAdult) {
		this.isAdult = isAdult;
	}

	public boolean isTemplate() {
		return isTemplate;
	}

	public void setTemplate(boolean isTemplate) {
		this.isTemplate = isTemplate;
	}

	public boolean getAllowForking() {
		return allowForking;
	}

	public void setAllowForking(boolean allowForking) {
		this.allowForking = allowForking;
	}

	public boolean getEnableTwitter() {
		return enableTwitter;
	}

	public void setEnableTwitter(boolean enableTwitter) {
		this.enableTwitter = enableTwitter;
	}

	public boolean getEnableTimers() {
		return enableTimers;
	}

	public void setEnableTimers(boolean enableTimers) {
		this.enableTimers = enableTimers;
	}

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}

	public User getCreator() {
		return creator;
	}

	public void setCreator(User creator) {
		this.creator = creator;
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.name + ")";
	}
	
	public String getSubdomain() {
		if (this.domainForwarder == null) {
			return "";
		}
		if (this.domainForwarder.getSubdomain() != null && !this.domainForwarder.getSubdomain().isEmpty()) {
			return this.domainForwarder.getSubdomain();
		}
		return this.domainForwarder.getDomain();
	}

	public DomainForwarder getDomainForwarder() {
		return domainForwarder;
	}

	public void setDomainForwarder(DomainForwarder domainForwarder) {
		this.domainForwarder = domainForwarder;
	}
	
	public WebMedium clone() {
		WebMedium clone = (WebMedium)super.clone();
		if (clone.domainForwarder != null) {
			clone.domainForwarder = clone.domainForwarder.clone();
		}
		return clone;
	}
	
	@Override
	public void preDelete(EntityManager em) {
	}
}
