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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.User;
import org.botlibre.web.issuetracker.IssueTracker;
import org.botlibre.web.issuetracker.IssueTrackerAttachment;
import org.botlibre.web.rest.IssueTrackerConfig;

public class IssueTrackerBean extends WebMediumBean<IssueTracker> {
	
	public IssueTrackerBean() {
	}

	/**
	 * Record the API access.
	 */
	public boolean apiConnect() {
		incrementConnects(ClientType.REST);
		return true;
	}
	
	@Override
	public boolean allowSubdomain() {
		return true;
	}

	@Override
	public String getEmbeddedBanner() {
		return "issuetracker-banner.jsp";
	}

	@Override
	public String getPostAction() {
		return "issuetracker";
	}
	
	/**
	 * Copy instance.
	 */
	public void copyInstance() {
		try {
			checkLogin();
			checkInstance();
			IssueTracker parent = getInstance();
			IssueTracker newInstance = new IssueTracker(parent.getName());
			newInstance.setDescription(parent.getDescription());
			newInstance.setDetails(parent.getDetails());
			newInstance.setDisclaimer(parent.getDisclaimer());
			newInstance.setTagsString(parent.getTagsString());
			newInstance.setCategoriesString(parent.getCategoriesString());
			newInstance.setLicense(parent.getLicense());
			newInstance.setContentRating(parent.getContentRating());
			setInstance(newInstance);
			setForking(true);
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	public List<IssueTracker> getAllInstances(Domain domain) {
		try {
			List<IssueTracker> results = AdminDatabase.instance().getAllIssueTrackers(this.page, this.pageSize, this.categoryFilter, this.nameFilter,
					this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllIssueTrackersCount(this.categoryFilter, this.nameFilter,
							this.userFilter, this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<IssueTracker>();
		}
	}

	public List<IssueTracker> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllIssueTrackers(
					0, 100, "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", "", "", null, getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<IssueTracker>();
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/issuetracker-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/issuetracker.png";
		}
		return file;
	}

	public boolean createInstance(IssueTrackerConfig config) {
		try {
			checkLogin();
			config.sanitize();
			IssueTracker newInstance = new IssueTracker(config.name);
			setInstance(newInstance);
			if (config.createAccessMode != null) {
				newInstance.setCreateAccessMode(AccessMode.valueOf(config.createAccessMode));
			}
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			checkVerfied(config);
			setSubdomain(config.subdomain, newInstance);
			//AdminDatabase.instance().validateNewIssueTracker(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setInstance(AdminDatabase.instance().createIssueTracker(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(IssueTrackerConfig config) {
		try {
			checkLogin();
			config.sanitize();
			IssueTracker newInstance = new IssueTracker(config.name);
			newInstance.setDomain(getDomain());
			newInstance.setDescription(config.description);
			newInstance.setDetails(config.details);
			newInstance.setDisclaimer(config.disclaimer);
			newInstance.setWebsite(config.website);
			newInstance.setAdult(Site.ADULT);
			newInstance.setExternal(true);
			newInstance.setPaphus(config.website.contains("paphuslivechat") || config.website.contains("botlibre.biz"));
			newInstance.setTagsString(config.tags);
			newInstance.setCategoriesString(config.categories);
			setInstance(newInstance);
			checkVerfied(config);
			if (config.name.equals("")) {
				throw new BotException("Invalid name");
			}
			if (!config.website.contains("http")) {
				throw new BotException("You must enter a valid URL for an external script");
			}
			//AdminDatabase.instance().validateNewIssueTracker(newInstance.getAlias(), config.description, config.tags, config.isAdult, getDomain());			
			setInstance(AdminDatabase.instance().createIssueTracker(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateIssueTracker(IssueTrackerConfig config, String newdomain, Boolean featured, Boolean adVerified) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			IssueTracker newInstance = (IssueTracker)this.instance.clone();
			this.editInstance = newInstance;
			updateFromConfig(newInstance, config);
			if (config.creator != null && isSuperUser()) {
				User user = AdminDatabase.instance().validateUser(config.creator);
				newInstance.setCreator(user);
			}
			if (newdomain != null && !newdomain.equals(this.instance.getDomain().getAlias())) {
				Domain domain = AdminDatabase.instance().validateDomain(newdomain);
				newInstance.setDomain(domain);
			}
			if (config.createAccessMode != null) {
				newInstance.setCreateAccessMode(AccessMode.valueOf(config.createAccessMode));
			}
			if (newInstance.getAdCode() == null || (config.adCode != null && !newInstance.getAdCode().equals(config.adCode))) {
				newInstance.setAdCodeVerified(false);
			}
			if (adVerified != null && isSuper()) {
				newInstance.setAdCodeVerified(adVerified);
			}
			if (isSuper() && featured != null) { 
				newInstance.setFeatured(featured);
			}
			setSubdomain(config.subdomain, newInstance);
			setInstance(AdminDatabase.instance().updateIssueTracker(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public String issueTrackerInput() {
		if (this.instance == null) {
			return "";
		}
		return "<input name=\"issuetracker\" type=\"hidden\" value=\"" + getInstanceId() + "\"/>";
	}
	
	public String isCreateAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Everyone;
		if (getEditInstance() != null) {
			mode = getEditInstance().getCreateAccessMode();
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	/**
	 * Download the attachment media.
	 */
	public boolean downloadAttachment(HttpServletResponse response, String id, String key) {
		IssueTrackerAttachment attachment = null;
		Media media = null;
		try {
			if (id == null || id.isEmpty()) {
				throw new BotException("Invalid attachment id");
			}
			if (key == null || key.isEmpty()) {
				throw new BotException("Invalid attachment key");
			}
			long mediaId = Long.valueOf(id);
			long keyId = Long.valueOf(key);
			attachment = AdminDatabase.instance().findIssueTrackerAttachment(mediaId);
			if (attachment.getKey() != keyId) {
				throw new BotException("Invalid attachment key");
			}
			media = AdminDatabase.instance().findMedia(mediaId);
			if (media == null) {
				throw new BotException("Missing media");
			}
		} catch (Exception exception) {
			try {
				response.setContentType("plain/text");
				response.getWriter().write(exception.getMessage());
			} catch (Exception ignore) {}
			error(exception);
			return false;
		}
		try {
			attachment.checkAttachmentType();
			response.setContentType(attachment.getType());
			response.setHeader("Content-disposition","inline; filename=" + encodeURI(attachment.getName()));
			//response.setHeader("Content-disposition","attachment; filename=" + encodeURI(attachment.getName()));
			ServletOutputStream stream = response.getOutputStream();
			stream.write(media.getMedia());
			//stream.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
	
	public boolean subscribe() {
		try {
			checkLogin();
			checkInstance();
			if (!getUser().hasEmail()) {
				throw new BotException("You must register and verify an email address with your account to subscribe to an issue tracker");
			}
			if (!getUser().isVerified()) {
				throw new BotException("You must verify your email address for your account to subscribe to an issue tracker");
			}
			if (!getUser().getEmailSummary()) {
				throw new BotException("You must enable summary emails in your account to subscribe to an issue tracker");
			}
			setInstance(AdminDatabase.instance().addIssueTrackerSubscriber(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean unsubscribe() {
		try {
			checkLogin();
			checkInstance();
			setInstance(AdminDatabase.instance().removeIssueTrackerSubscriber(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public IssueTrackerAttachment addAttachment(byte[] image, String name, String type, String user, String token) {
		try {
			checkInstance();
			IssueTrackerAttachment attachment = new IssueTrackerAttachment();
			attachment.setName(name);
			attachment.setType(type);
			attachment.checkAttachmentType();
			attachment.generateKey();
			Media media = new Media();
			media.setMedia(image);
			return AdminDatabase.instance().addIssueTrackerAttachment(attachment, media, this.instance, user, token);
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}

	@Override
	public Class<IssueTracker> getType() {
		return IssueTracker.class;
	}
	
	@Override
	public String getTypeName() {
		return "IssueTracker";
	}

	@Override
	public String getCreateURL() {
		return "create-issuetracker.jsp";
	}

	@Override
	public String getSearchURL() {
		return "issuetracker-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-issuetracker.jsp";
	}
	
}
