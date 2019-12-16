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
import org.botlibre.web.issuetracker.Issue;
import org.botlibre.web.issuetracker.Issue.IssueType;
import org.botlibre.web.issuetracker.Issue.Priority;
import org.botlibre.web.issuetracker.Issue.Status;
import org.botlibre.web.issuetracker.IssueTracker;
import org.botlibre.web.service.Stats;

public class IssueBean extends BrowseBean<Issue> {
	IssueTrackerBean issueTrackerBean;
	List<Long> results;
	boolean preview;
	boolean editorChange;
	EditorType editorType;
	
	public enum EditorType {WYSIWYG, Markup, HTML}
	
	public IssueBean() {
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
		return this.issueTrackerBean.getAdCode();
	}

	@Override
	public String getPostAction() {
		return "issue";
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
			out.write("' title=\"Edit the issue\"><img src='images/edit.svg' class='toolbar'/></a>\n");
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
			out.write("issue?id=");
			out.write("" + next(getInstance()) + proxy.proxyString());
			out.write("' title=\"Next issue\"><img src='images/up.svg' class='toolbar'/></a>\n");
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
			out.write("issue?id=");
			out.write("" + previous(getInstance()) + proxy.proxyString());
			out.write("' title=\"Previous issue\"><img src='images/down.svg' class='toolbar'/></a>\n");
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
			out.write("' title=\"Edit the issue\"><img src='images/edit.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Edit Issue"));
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
			out.write("issue?id=");
			out.write("" + next(getInstance()) + proxy.proxyString());
			out.write("' title=\"Next issue\"><img src='images/up.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Next Issue"));
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
			out.write("issue?id=");
			out.write("" + previous(getInstance()) + proxy.proxyString());
			out.write("' title=\"Previous issue\"><img src='images/down.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Previous Issue"));
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

	public boolean preview(String title, String priority, String issueType, String status, String details, String tags, boolean createNew) {
		try {
			title = Utils.sanitize(title);
			priority = Utils.sanitize(priority);
			issueType = Utils.sanitize(issueType);
			status = Utils.sanitize(status);
			details = Utils.sanitize(details);
			tags = Utils.sanitize(tags);
			checkLogin();
			this.preview = true;
			if (createNew) {
				setInstance(new Issue(title));
			} else {
				setInstance((Issue)this.instance.clone());
			}
			this.instance.setTracker(this.issueTrackerBean.getInstance());
			this.instance.setTitle(title);
			if (issueType != null) {
				this.instance.setType(IssueType.valueOf(issueType));
			}
			if (status != null) {
				this.instance.setStatus(Status.valueOf(status));
			}
			this.instance.setPriority(Priority.valueOf(priority));
			this.instance.setTextDetails(details);
			this.instance.setTagsString(tags);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean changeEditor(String title, String priority, String issueType, String status, String details, String tags, boolean createNew, String editor) {
		try {
			title = Utils.sanitize(title);
			priority = Utils.sanitize(priority);
			issueType = Utils.sanitize(issueType);
			status = Utils.sanitize(status);
			details = Utils.sanitize(details);
			tags = Utils.sanitize(tags);
			this.editorChange = true;
			this.editorType = EditorType.valueOf(editor);
			checkLogin();
			if (createNew) {
				setInstance(new Issue(title));
			} else {
				setInstance((Issue)this.instance.clone());
			}
			this.instance.setTracker(this.issueTrackerBean.getInstance());
			this.instance.setTitle(title);
			if (issueType != null) {
				this.instance.setType(IssueType.valueOf(issueType));
			}
			if (status != null) {
				this.instance.setStatus(Status.valueOf(status));
			}
			this.instance.setPriority(Priority.valueOf(priority));
			this.instance.setTextDetails(details);
			this.instance.setTagsString(tags);
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

	@Override
	public void disconnect() {
		super.disconnect();
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

	public String getAvatarThumb(Issue instance) {
		return "images/issue-thumb.jpg";
	}

	public String getAvatarImage(Issue instance) {
		return "images/issue.jpg";
	}

	public boolean isAdmin() {
		if (!isLoggedIn()) {
			return false;
		}
		if (getUser() == null || getInstance() == null) {
			return false;
		}
		if (this.issueTrackerBean.isAdmin() || this.issueTrackerBean.isSuper()) {
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
	public List<Issue> getAllInstances() {
		try {
			List<Issue> posts = AdminDatabase.instance().getAllIssues(
					this.issueTrackerBean.getInstance(), this.page, this.pageSize, this.nameFilter, this.userFilter,
					this.instanceFilter, this.instanceSort, this.categoryFilter, this.tagFilter, getUser(), getDomain());
			this.results = new ArrayList<Long>(posts.size());
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (posts.size() < this.pageSize) {
					this.resultsSize = posts.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllIssuesCount(
							this.issueTrackerBean.getInstance(), this.page, this.pageSize, this.nameFilter, this.userFilter,
							this.instanceFilter, this.instanceSort, this.categoryFilter, this.tagFilter, getUser(), getDomain());
				}
			}
			for (Issue post : posts) {
				this.results.add(post.getId());
			}
			return posts;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Issue>();
		}
	}
	
	public boolean hasPrevious(Issue post) {
		if (this.results == null) {
			return false;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == (this.results.size() - 1))) {
			return false;
		}
		return true;
	}
	
	public Long previous(Issue post) {
		if (this.results == null) {
			return null;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == (this.results.size() - 1))) {
			return null;
		}
		return this.results.get(index + 1);
	}
	
	public boolean hasNext(Issue post) {
		if (this.results == null) {
			return false;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == 0)) {
			return false;
		}
		return true;
	}
	
	public Long next(Issue post) {
		if (this.results == null) {
			return null;
		}
		int index = this.results.indexOf(post.getId());
		if ((index == -1) || (index == 0)) {
			return null;
		}
		return this.results.get(index - 1);
	}

	public List<Issue> getAllPriorityInstances() {
		try {
			return AdminDatabase.instance().getAllPriorityIssues(this.issueTrackerBean.getInstance(), getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Issue>();
		}
	}

	public IssueTracker getIssueTracker() {
		return this.issueTrackerBean.getInstance();
	}

	public IssueTrackerBean getIssueTrackerBean() {
		return issueTrackerBean;
	}

	public void setIssueTrackerBean(IssueTrackerBean issueTrackerBean) {
		this.issueTrackerBean = issueTrackerBean;
	}

	public boolean createInstance(String topic, String priority, String issueType, String status, String details, boolean subscribe, String tags) {
		try {
			topic = Utils.sanitize(topic);
			priority = Utils.sanitize(priority);
			issueType = Utils.sanitize(issueType);
			status = Utils.sanitize(status);
			details = Utils.sanitize(details);
			tags = Utils.sanitize(tags);
			if (Site.READONLY) {
				throw new BotException("This website is currently undergoing maintence, please try later.");
			}
			checkLogin();
			if (this.issueTrackerBean.getInstance() == null) {
				throw new BotException("No issue tracker selected");
			}
			this.issueTrackerBean.getInstance().checkIssueAccess(getUser());
			Issue newInstance = new Issue(topic);
			newInstance.setTracker(this.issueTrackerBean.getInstance());
			newInstance.setDomain(this.issueTrackerBean.getInstance().getDomain());
			if (status != null) {
				newInstance.setStatus(Status.valueOf(status));
			}
			if (issueType != null) {
				newInstance.setType(IssueType.valueOf(issueType));
			}
			newInstance.setPriority(Priority.valueOf(priority));
			newInstance.setTextDetails(details);
			newInstance.setTagsString(tags);
			setInstance(newInstance);
			if (!isSuper()) {
				Utils.checkScript(details);
				newInstance.setTextDetails(Utils.sanitize(details));
			}
			if (topic.equals("")) {
				throw new BotException("Invalid title");
			}
			AdminDatabase.instance().validateNewForumPost(topic, details, tags, getIssueTracker().isAdult());
			Stats.stats.issues++;
			setInstance(AdminDatabase.instance().createIssue(newInstance, getUser().getUserId(), tags, newInstance.getDomain()));
			if (subscribe) {
				subscribe();
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
			Issue post = AdminDatabase.instance().validateIssue(this.instance.getId(), getUser().getUserId(), ClientType.WEB);
			if (!isAdmin()) {
				throw new BotException("Must be admin user to delete issue");
			}
			AdminDatabase.instance().delete(post);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
		
	}	

	public boolean updateInstance(String title, String priority, String issueType, String status, String details, String tags, boolean isPriority, boolean isHidden, ClientType type) {
		try {
			title = Utils.sanitize(title);
			priority = Utils.sanitize(priority);
			issueType = Utils.sanitize(issueType);
			status = Utils.sanitize(status);
			details = Utils.sanitize(details);
			tags = Utils.sanitize(tags);
			checkLogin();
			setInstance(AdminDatabase.instance().validateIssue(this.instance.getId(), getUser().getUserId(), type));
			if (!isAdmin()) {
				throw new BotException("Must be admin user to edit issue");
			}
			Issue newInstance = (Issue)this.instance.clone();
			this.editInstance = newInstance;
			newInstance.setTitle(title);
			if (issueType != null) {
				newInstance.setType(IssueType.valueOf(issueType));
			}
			if (status != null) {
				newInstance.setStatus(Status.valueOf(status));
			}
			newInstance.setPriority(Priority.valueOf(priority));
			newInstance.setTextDetails(details);
			newInstance.setTagsString(tags);
			if (this.issueTrackerBean.isAdmin()) {
				newInstance.setIsPriority(isPriority);
				newInstance.setIsHidden(isHidden);
			}
			if (!isSuper()) {
				Utils.checkScript(details);
				newInstance.setTextDetails(Utils.sanitize(details));
			}
			setInstance(AdminDatabase.instance().updateIssue(newInstance, getUser().getUserId(), tags, getDomain()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean validateInstance(String instance, ClientType type) {
		try {
			setInstance(AdminDatabase.instance().validateIssue(Long.valueOf(instance), getUserId(), type));
			if (this.issueTrackerBean.getInstance() == null) {
				this.issueTrackerBean.setInstance(getInstance().getTracker());
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
			setInstance(AdminDatabase.instance().validateIssue(id, getUser().getUserId(), ClientType.WEB));
			if (!isAdmin()) {
				throw new BotException("Must be admin user to edit issue");
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
				throw new BotException("You must register and verify an email address with your account to subscribe to an issue");
			}
			if (!getUser().isVerified()) {
				throw new BotException("You must verify your email address for your account to subscribe to an issue");
			}
			setInstance(AdminDatabase.instance().addIssueSubscriber(this.instance, getUser().getUserId()));
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
			setInstance(AdminDatabase.instance().removeIssueSubscriber(this.instance, getUser().getUserId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public String issueTrackerString() {
		if (this.instance == null || getIssueTracker() == null) {
			return "";
		}
		return "&issuetracker=" + getIssueTracker().getId();
	}

	public String isPrioritySelected(String type) {
		if (getEditInstance() == null || getEditInstance().getPriority() == null) {
			return "";
		}
		if (getEditInstance().getPriority().name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isStatusSelected(String type) {
		if (getEditInstance() == null || getEditInstance().getStatus() == null) {
			return "";
		}
		if (getEditInstance().getStatus().name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isTypeSelected(String type) {
		if (getEditInstance() == null) {
			return "";
		}
		if (getEditInstance().getType().name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	@Override
	public Domain getDomain() {
		return this.issueTrackerBean.getDomain();
	}
	
	@Override
	public Class<Issue> getType() {
		return Issue.class;
	}
	
	@Override
	public String getTypeName() {
		return "Issue";
	}

	@Override
	public String getCreateURL() {
		return "create-issue.jsp";
	}

	@Override
	public String getSearchURL() {
		return "browse-issue.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-issue.jsp";
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
	
}
