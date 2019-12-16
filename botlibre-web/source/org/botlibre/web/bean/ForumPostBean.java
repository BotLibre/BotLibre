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

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.User;
import org.botlibre.web.forum.Forum;
import org.botlibre.web.forum.ForumPost;
import org.botlibre.web.service.EmailService;
import org.botlibre.web.service.Stats;

public class ForumPostBean extends BrowseBean<ForumPost> {
	ForumBean forumBean;
	ForumPost reply;
	boolean preview;
	boolean editorChange;
	List<Long> results;
	EditorType editorType;
	
	public enum EditorType {WYSIWYG, Markup, HTML}
	
	public ForumPostBean() {
		this.displayOption = DisplayOption.Header;
		this.instanceSort = InstanceSort.Date;
	}

	public String getAdCode() {
		if (this.instance != null) {
			if (this.instance.getCreator().hasAdCode()) {
				if (this.instance.getCreator().isAdCodeVerified()) {
					return this.instance.getCreator().getAdCode();
				}
				return Utils.sanitize(this.instance.getCreator().getAdCode());
			}
		}
		return this.forumBean.getAdCode();
	}

	@Override
	public String getPostAction() {
		return "forum-post";
	}

	@Override
	public void writeEditButtonHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<a href='");
			out.write(getPostAction());
			out.write("?edit-instance");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"Edit the post\"><img src='images/edit.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void writeNextButtonHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<a href='");
			out.write("forum-post?id=");
			out.write("" + next(getInstance()) + proxy.proxyString());
			out.write("' title=\"Next post\"><img src='images/up.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public void writePreviousButtonHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<a href='");
			out.write("forum-post?id=");
			out.write("" + previous(getInstance()) + proxy.proxyString());
			out.write("' title=\"Previous post\"><img src='images/down.svg' class='toolbar'/></a>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeEditMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write(getPostAction());
			out.write("?edit-instance");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' title=\"Edit the post\"><img src='images/edit.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Edit Post"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeNextMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write("forum-post?id=");
			out.write("" + next(getInstance()) + proxy.proxyString());
			out.write("' title=\"Next post\"><img src='images/up.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Next Post"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writePreviousMenuItemHTML(SessionProxyBean proxy, Writer out) {
		if (this.instance == null || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='");
			out.write("forum-post?id=");
			out.write("" + previous(getInstance()) + proxy.proxyString());
			out.write("' title=\"Previous post\"><img src='images/down.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Previous Post"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	@Override
	public void writeMenuButtonHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (this.instance == null) {
			return;
		}
		try {
			out.write("<span class='dropt'>\n");
			out.write("<div style='text-align:left;bottom:36px'>\n");
			out.write("<table>\n");
			writeEditMenuItemHTML(proxy, out);
			writeDeleteMenuItemHTML(proxy, out);
			writeFlagMenuItemHTML(proxy, embed, out);
			writeThumbsMenuItemsHTML(proxy, embed, out);
			writeStarMenuItemHTML(proxy, embed, out);
			if (hasNext(getInstance())) {
				writeNextMenuItemHTML(proxy, out);
			}
			if (hasPrevious(getInstance())) {
				writePreviousMenuItemHTML(proxy, out);
			}
			out.write("</table>\n");
			out.write("</div>\n");
			super.writeMenuButtonHTML(proxy, embed, out);
			out.write("</span>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void writeToolbarPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (hasNext(getInstance())) {
			writeNextButtonHTML(proxy, out);
		}
		if (hasPrevious(getInstance())) {
			writePreviousButtonHTML(proxy, out);
		}
	}

	@Override
	public void disconnect() {
		super.disconnect();
		this.reply = null;
	}

	@Override
	public void resetSearch() {
		super.resetSearch();
		this.results = null;
		this.instanceSort = InstanceSort.Date;
	}

	public boolean isDefaults() {
		return this.instanceSort == InstanceSort.Date
			&& this.instanceFilter == InstanceFilter.Public
			&& this.instanceRestrict == InstanceRestrict.None
			&& (this.categoryFilter == null || this.categoryFilter.isEmpty())
			&& (this.nameFilter == null || this.nameFilter.isEmpty())
			&& (this.tagFilter == null || this.tagFilter.isEmpty())
			&& this.page == 0;
	}

	public String getAvatarThumb(ForumPost instance) {
		return getAvatarThumb(instance.getCreator().getAvatar());
	}

	public String getAvatarImage(ForumPost instance) {
		return getAvatarImage(instance.getCreator().getAvatar());
	}

	public boolean isAdmin() {
		if (!isLoggedIn()) {
			return false;
		}
		if (getUser() == null || getInstance() == null) {
			return false;
		}
		if (this.forumBean.isAdmin() || this.forumBean.isSuper()) {
			return true;
		}
		return getInstance().getCreator().equals(getUser());
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = this.loginBean.getAvatarThumb(avatar, 192);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/user-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/user.png";
		}
		return file;
	}

	@Override
	public List<ForumPost> getAllInstances() {
		try {
			List<ForumPost> posts = AdminDatabase.instance().getAllForumPosts(
					this.forumBean.getInstance(), this.page, this.pageSize, this.nameFilter, this.userFilter,
					this.instanceFilter, this.instanceSort, this.categoryFilter, this.tagFilter, getUser(), getDomain());
			this.results = new ArrayList<Long>(posts.size());
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (posts.size() < this.pageSize) {
					this.resultsSize = posts.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllForumPostsCount(
							this.forumBean.getInstance(), this.page, this.pageSize, this.nameFilter, this.userFilter,
							this.instanceFilter, this.instanceSort, this.categoryFilter, this.tagFilter, getUser(), getDomain());
				}
			}
			for (ForumPost post : posts) {
				this.results.add(post.getId());
			}
			return posts;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<ForumPost>();
		}
	}
	
	public boolean hasPrevious(ForumPost post) {
		if (this.results == null) {
			return false;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == (this.results.size() - 1))) {
			return false;
		}
		return true;
	}
	
	public Long previous(ForumPost post) {
		if (this.results == null) {
			return null;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == (this.results.size() - 1))) {
			return null;
		}
		return this.results.get(index + 1);
	}
	
	public boolean hasNext(ForumPost post) {
		if (this.results == null) {
			return false;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == 0)) {
			return false;
		}
		return true;
	}
	
	public Long next(ForumPost post) {
		if (this.results == null) {
			return null;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == 0)) {
			return null;
		}
		return this.results.get(index - 1);
	}

	public List<ForumPost> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllFeaturedForumPosts(this.forumBean.getInstance(), getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<ForumPost>();
		}
	}

	public Forum getForum() {
		return this.forumBean.getInstance();
	}

	public ForumBean getForumBean() {
		return forumBean;
	}

	public void setForumBean(ForumBean forumBean) {
		this.forumBean = forumBean;
	}

	public boolean createInstance(String topic, String details, boolean subscribe, String tags) {
		try {
			topic = Utils.sanitize(topic);
			tags = Utils.sanitize(tags);
			if (Site.READONLY) {
				throw new BotException("This website is currently undergoing maintence, please try later.");
			}
			checkLogin();
			if (this.forumBean.getInstance() == null) {
				throw new BotException("No forum selected");
			}
			this.forumBean.getInstance().checkPostAccess(getUser());
			ForumPost newInstance = new ForumPost(topic);
			newInstance.setForum(this.forumBean.getInstance());
			newInstance.setDomain(this.forumBean.getInstance().getDomain());
			newInstance.setTagsString(tags);
			setInstance(newInstance);
			if (!isSuper()) {
				Utils.checkScript(details);
				newInstance.setTextDetails(Utils.sanitize(details));
			} else {
				newInstance.setTextDetails(details);
			}
			if (topic.equals("")) {
				throw new BotException("Invalid topic");
			}
			AdminDatabase.instance().validateNewForumPost(topic, details, tags, getForum().isAdult());
			Stats.stats.forumPosts++;
			setInstance(AdminDatabase.instance().createForumPost(newInstance, getUser().getUserId(), tags, newInstance.getDomain()));
			if (subscribe) {
				subscribe();
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean changeEditor(String topic, String details, String tags, boolean createNew, String editor) {
		try {
			topic = Utils.sanitize(topic);
			tags = Utils.sanitize(tags);
			editor = Utils.sanitize(editor);
			if (!isSuper()) {
				details = Utils.sanitize(details);
			}
			this.editorChange = true;
			this.editorType = EditorType.valueOf(editor);
			checkLogin();
			if (createNew) {
				setInstance(new ForumPost(topic));
			} else {
				setInstance((ForumPost)this.instance.clone());
			}
			this.instance.setForum(this.forumBean.getInstance());
			this.instance.setTopic(topic);
			this.instance.setTextDetails(details);
			this.instance.setTagsString(tags);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean changeReplyEditor(String topic, String details, String tags, boolean createNew, String editor) {
		try {
			topic = Utils.sanitize(topic);
			tags = Utils.sanitize(tags);
			editor = Utils.sanitize(editor);
			if (!isSuper()) {
				details = Utils.sanitize(details);
			}
			this.editorChange = true;
			this.editorType = EditorType.valueOf(editor);
			checkLogin();
			if (createNew) {
				setReply(new ForumPost(topic));
			}
			this.reply.setForum(this.forumBean.getInstance());
			this.reply.setTopic(topic);
			this.reply.setTextDetails(details);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean preview(String topic, String details, String tags, boolean createNew) {
		try {
			topic = Utils.sanitize(topic);
			tags = Utils.sanitize(tags);
			checkLogin();
			if (!isSuper()) {
				details = Utils.sanitize(details);
			}
			this.preview = true;
			if (createNew) {
				setInstance(new ForumPost(topic));
			} else {
				setInstance((ForumPost)this.instance.clone());
			}
			this.instance.setForum(this.forumBean.getInstance());
			this.instance.setTopic(topic);
			this.instance.setTextDetails(details);
			this.instance.setTagsString(tags);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean previewReply(String topic, String details, String tags, boolean createNew) {
		try {
			topic = Utils.sanitize(topic);
			tags = Utils.sanitize(tags);
			if (!isSuper()) {
				details = Utils.sanitize(details);
			}
			checkLogin();
			this.preview = true;
			if (createNew) {
				setReply(new ForumPost(topic));
			}
			this.reply.setForum(this.forumBean.getInstance());
			this.reply.setTopic(topic);
			this.reply.setTextDetails(details);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean createReply(String details, boolean replyToParent) {
		try {
			if (Site.READONLY) {
				throw new BotException("This website is currently undergoing maintence, please try later.");
			}
			checkLogin();
			if (this.instance == null) {
				throw new BotException("Missing post, or session timeout");
			}
			this.instance.getForum().checkReplyAccess(getUser());
			String topic = this.instance.getTopic();
			if (this.instance.getParent() == null) {
				topic = "RE: " + topic;
			}
			ForumPost reply = new ForumPost(topic);
			reply.setForum(this.instance.getForum());
			reply.setDomain(this.instance.getForum().getDomain());
			if (replyToParent && this.instance.getParent() != null) {
				reply.setParent(this.instance.getParent());
			} else {
				reply.setParent(this.instance);
			}
			if (!isSuper()) {
				Utils.checkScript(details);
				reply.setTextDetails(Utils.sanitize(details));
			} else {
				reply.setTextDetails(details);
			}
			if (details.equals("")) {
				throw new BotException("Invalid reply");
			}
			AdminDatabase.instance().validateNewForumPost("", details, "", this.instance.getForum().isAdult());
			Stats.stats.forumPosts++;
			setInstance(AdminDatabase.instance().createForumPost(reply, getUser().getUserId(), "", reply.getDomain()));
			
			// Email subscribers.
			for (User user : reply.getParent().getSubscribers()) {
				if (!user.equals(getUser()) && user.hasEmail() && user.isVerified() && user.getEmailNotices()) {
					StringWriter writer = new StringWriter();
					writer.write("New reply to forum post \"");
					writer.write(reply.getParent().getTopic());
					writer.write("\" from ");
					writer.write(getUser().getUserId());
					writer.write(".\n<p>");
					writer.write("To view this post on ");
					writer.write(Site.NAME);
					writer.write(" click <a href=\"" + Site.SECUREURLLINK + "/forum-post?id=" + reply.getId() + "\">here</a>.\n<p>");
					writer.write("\n<br/>Reply follows:\n\n<br/><p>");
					writer.write("<b>" + reply.getTopic() + "</b>\n<p>");
					writer.write(reply.getSummary());
					writer.write("\n<p>");
					writer.write("\n<br/><hr>");
					writer.write("\n<p>This is an automated email from " + Site.NAME + " - <a href=\"" + Site.SECUREURLLINK + "\">" + Site.SECUREURLLINK + "</a>.");
					writer.write("\n<p>To unsubscribe from this post goto <a href=\"" + Site.SECUREURLLINK + "/forum-post?id=" + reply.getParent().getId() + "\">" + reply.getParent().getTopic() + "</a> and click 'unsubscribe' (you must login).");
					writer.write("\n<p><a href=\"" + Site.SECUREURLLINK + "/login?unsubscribe=all&user="
						+ user.getUserId() + "&token=" + user.getVerifyToken() + "\">Unsubscribe from all notices</a>.");
					EmailService.instance().sendEmail(user.getEmail(), "New reply to forum post \"" + reply.getParent().getTopic() + "\" on " + Site.NAME, null, writer.toString());
				}
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean deleteInstance(boolean confirm) {
		try {
			if (!confirm) {
				throw new BotException("Must check 'I'm sure'");
			}
			checkLogin();
			ForumPost post = AdminDatabase.instance().validateForumPost(this.instance.getId(), getUser().getUserId(), ClientType.WEB);
			if (!isAdmin()) {
				throw new BotException("Must be admin user to delete post");
			}
			AdminDatabase.instance().delete(post);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
		
	}	

	public boolean updateInstance(String topic, String details, String tags, boolean featured, ClientType type) {
		try {
			topic = Utils.sanitize(topic);
			tags = Utils.sanitize(tags);
			checkLogin();
			setInstance(AdminDatabase.instance().validateForumPost(this.instance.getId(), getUser().getUserId(), type));
			if (!isAdmin()) {
				throw new BotException("Must be admin user to edit post");
			}
			ForumPost newInstance = (ForumPost)this.instance.clone();
			this.editInstance = newInstance;
			newInstance.setTopic(topic);
			newInstance.setTagsString(tags);
			if (isSuper()) {
				newInstance.setFeatured(featured);
			}
			if (!isSuper()) {
				Utils.checkScript(details);
				newInstance.setTextDetails(Utils.sanitize(details));
			} else {
				newInstance.setTextDetails(details);
			}
			setInstance(AdminDatabase.instance().updateForumPost(newInstance, getUser().getUserId(), tags, getDomain()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean validateInstance(String instance, ClientType type) {
		try {
			Stats.stats.forumPostViews++;
			setInstance(AdminDatabase.instance().validateForumPost(Long.valueOf(instance), getUserId(), type));
			if (this.forumBean.getInstance() == null) {
				this.forumBean.setInstance(getInstance().getForum());
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean editInstance(long id) {
		try {
			checkLogin();
			setInstance(AdminDatabase.instance().validateForumPost(id, getUser().getUserId(), ClientType.WEB));
			if (!isAdmin()) {
				throw new BotException("Must be admin user to edit post");
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public boolean subscribe() {
		try {
			checkLogin();
			checkInstance();
			if (!getUser().hasEmail()) {
				throw new BotException("You must register and verify an email address with your account to subscribe to a forum post");
			}
			if (!getUser().isVerified()) {
				throw new BotException("You must verify your email address for your account to subscribe to a forum post");
			}
			setInstance(AdminDatabase.instance().addForumPostSubscriber(this.instance, getUser().getUserId()));
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
			setInstance(AdminDatabase.instance().removeForumPostSubscriber(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public String isEditorTypeSelected(EditorType type) {
		if (type.equals(this.editorType)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public EditorType getEditorType() {
		if (this.editorType == null) {
			if (this.loginBean.isMobile()) {
				this.editorType = EditorType.Markup;
			} else {
				this.editorType = EditorType.WYSIWYG;				
			}
		}
		return editorType;
	}

	public void setEditorType(EditorType editorType) {
		this.editorType = editorType;
	}

	public ForumPost getReply() {
		return reply;
	}

	public void setReply(ForumPost reply) {
		this.reply = reply;
	}

	public boolean isPreview() {
		return preview;
	}

	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	public boolean isEditorChange() {
		return editorChange;
	}

	public void setEditorChange(boolean editorChange) {
		this.editorChange = editorChange;
	}

	@Override
	public Domain getDomain() {
		return this.forumBean.getDomain();
	}
	
	@Override
	public Class<ForumPost> getType() {
		return ForumPost.class;
	}
	
	@Override
	public String getTypeName() {
		return "Post";
	}

	@Override
	public String getCreateURL() {
		return "create-post.jsp";
	}

	@Override
	public String getSearchURL() {
		return "browse-post.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-post.jsp";
	}
	
}
