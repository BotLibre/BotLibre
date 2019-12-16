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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;

import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.BotMode;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.User;
import org.botlibre.web.forum.Forum;
import org.botlibre.web.forum.ForumAttachment;
import org.botlibre.web.rest.ForumConfig;
import org.botlibre.web.service.Stats;

public class ForumBean extends WebMediumBean<Forum> {
	
	public ForumBean() {
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
		return "forum-banner.jsp";
	}

	@Override
	public String getPostAction() {
		return "forum";
	}
	
	public String forumString() {
		if (this.instance == null) {
			return "";
		}
		return "&forum=" + getInstanceId();
	}
	
	public String forumInput() {
		if (this.instance == null) {
			return "";
		}
		return "<input name=\"forum\" type=\"hidden\" value=\"" + getInstanceId() + "\"/>";
	}
	
	@Override
	public void writeBrowseStats(StringWriter writer, Forum instance) {
		writer.write("Posts: " + instance.getPosts() + "<br/>\n");
	}
	
	@Override
	public void writeSearchOptions(StringWriter writer) {
		writer.write("<option value='Posts' " + getInstanceSortCheckedString(InstanceSort.Posts) + ">posts</option>\n");
	}
	
	@Override
	public void writeStatsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (!getDisplayInstance().isExternal()) {
				out.write("Posts: ");
				out.write(String.valueOf(getDisplayInstance().getPosts()));
				out.write(", week: ");
				out.write(String.valueOf(getDisplayInstance().getWeeklyPosts()));
				out.write("<br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public String isPostAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Everyone;
		if (getEditInstance() != null) {
			mode = getEditInstance().getPostAccessMode();
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isReplyAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Everyone;
		if (getEditInstance() != null) {
			mode = getEditInstance().getReplyAccessMode();
		}
		if (mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String getBotCheckedString(BotInstance bot) {
		if ((getInstance() == null) || (bot == null)) {
			return "";
		}
		if (bot.equals(getInstance().getBot())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getBotModeCheckedString(BotMode botMode) {
		if ((getInstance() == null) || (botMode == null)) {
			return "";
		}
		if (botMode.equals(getInstance().getBotMode())) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public ForumAttachment addAttachment(byte[] image, String name, String type, String user, String token) {
		try {
			checkInstance();
			ForumAttachment attachment = new ForumAttachment();
			attachment.setName(name);
			attachment.setType(type);
			attachment.checkAttachmentType();
			attachment.generateKey();
			Media media = new Media();
			media.setMedia(image);
			return AdminDatabase.instance().addForumAttachment(attachment, media, this.instance, user, token);
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	/**
	 * Download the attachment media.
	 */
	public boolean downloadAttachment(HttpServletResponse response, String id, String key) {
		ForumAttachment attachment = null;
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
			attachment = AdminDatabase.instance().findForumAttachment(mediaId);
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
				throw new BotException("You must register and verify an email address with your account to subscribe to a forum");
			}
			if (!getUser().isVerified()) {
				throw new BotException("You must verify your email address for your account to subscribe to a forum");
			}
			if (!getUser().getEmailSummary()) {
				throw new BotException("You must enable summary emails in your account to subscribe to a forum");
			}
			setInstance(AdminDatabase.instance().addForumSubscriber(this.instance, getUser().getUserId()));
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
			setInstance(AdminDatabase.instance().removeForumSubscriber(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean saveBot(String bot, String mode) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			Forum newInstance = (Forum)this.instance.clone();
			newInstance.setBotMode(BotMode.valueOf(mode));
			Long id = null;
			if (!bot.trim().isEmpty()) {
				if (!getBotBean().validateInstance(bot)) {
					return false;
				}
				id = getBotBean().getInstanceId();
			}
			setInstance((Forum)AdminDatabase.instance().updateForumBot(newInstance, id));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public List<Forum> getAllInstances(Domain domain) {
		try {
			List<Forum> results = AdminDatabase.instance().getAllForums(this.page, this.pageSize, this.categoryFilter, this.nameFilter, this.userFilter, 
					this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllForumsCount(this.categoryFilter, this.nameFilter, this.userFilter, this.instanceFilter, 
							this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, getUser(), domain);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Forum>();
		}
	}

	public List<Forum> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllForums(
					0, 100, "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", null, getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Forum>();
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/forum-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/forum.png";
		}
		return file;
	}

	public boolean createInstance(ForumConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Forum newInstance = new Forum(config.name);
			setInstance(newInstance);
			checkVerfied(config);
			if (config.postAccessMode != null) {
				newInstance.setPostAccessMode(AccessMode.valueOf(config.postAccessMode));
			}
			if (config.replyAccessMode != null) {
				newInstance.setReplyAccessMode(AccessMode.valueOf(config.replyAccessMode));
			}
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			//AdminDatabase.instance().validateNewForum(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setSubdomain(config.subdomain, newInstance);
			Stats.stats.forumCreates++;
			setInstance(AdminDatabase.instance().createForum(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(ForumConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Forum newInstance = new Forum(config.name);
			newInstance.setDomain(getDomain());
			newInstance.setDescription(config.description);
			newInstance.setDetails(config.details);
			newInstance.setDisclaimer(config.disclaimer);
			newInstance.setWebsite(config.website);
			newInstance.setAdult(config.isAdult);
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
				throw new BotException("You must enter a valid URL for an external forum");
			}
			//AdminDatabase.instance().validateNewForum(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());			
			setInstance(AdminDatabase.instance().createForum(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateForum(ForumConfig config, String newdomain, Boolean featured, Boolean adVerified) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			Forum newInstance = (Forum)this.instance.clone();
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
			if (config.postAccessMode != null) {
				newInstance.setPostAccessMode(AccessMode.valueOf(config.postAccessMode));
			}
			if (config.replyAccessMode != null) {
				newInstance.setReplyAccessMode(AccessMode.valueOf(config.replyAccessMode));
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
			newInstance.setTagsString(config.tags);
			newInstance.setCategoriesString(config.categories);
			setSubdomain(config.subdomain, newInstance);
			setInstance(AdminDatabase.instance().updateForum(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	@Override
	public Class<Forum> getType() {
		return Forum.class;
	}
	
	@Override
	public String getTypeName() {
		return "Forum";
	}

	@Override
	public String getCreateURL() {
		return "create-forum.jsp";
	}

	@Override
	public String getSearchURL() {
		return "forum-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-forum.jsp";
	}
	
}
