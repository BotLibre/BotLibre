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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.rest.UserMessageConfig;

public class UserMessageBean extends ServletBean {
	
	public enum UserMessageSort { Date, Subject, User }

	public enum DisplayOption { Header, Details, Grid }

	public enum UserMessageFolder { Messages, Sent }
		
	String filter = "";
	String userFilter = "";
	UserMessageFolder folder = UserMessageFolder.Messages;
	DisplayOption displayOption = DisplayOption.Header;
	UserMessageSort sort = UserMessageSort.Date;

	public String getFolderCheckedString(UserMessageFolder folder) {
		if (folder == this.folder) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getDisplayCheckedString(DisplayOption display) {
		if (display == this.displayOption) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getSortCheckedString(UserMessageSort sort) {
		if (sort == this.sort) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public void resetSearch() {
		this.sort = UserMessageSort.Date;
		this.displayOption = DisplayOption.Header;
		this.folder = UserMessageFolder.Messages;
		this.filter = "";
		this.userFilter = "";
		this.page = 0;
		this.resultsSize = 0;
	}
	
	public boolean isDefaults() {
		return this.sort == UserMessageSort.Date
			&& this.displayOption == DisplayOption.Header
			&& this.folder == UserMessageFolder.Messages
			&& this.filter.isEmpty()
			&& this.userFilter.isEmpty()
			&& this.page == 0;
	}
	
	public List<UserMessage> getAllInstances() {
		try {
			List<UserMessage> results = AdminDatabase.instance().getAllUserMessages(this.page, this.pageSize, this.filter, this.userFilter, this.sort, getUser(), false);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllUserMessageCount(this.filter, this.userFilter, this.sort, getUser(), false);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<UserMessage>();
		}
	}
	
	public DisplayOption getDisplayOption() {
		return displayOption;
	}
	
	public void setDisplayOption(DisplayOption displayOption){
		this.displayOption = displayOption;
	}
	
	public String getFilter() {
		if (filter == null) {
			return "";
		}
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getUserFilter() {
		if (userFilter == null) {
			return "";
		}
		return userFilter;
	}

	public void setUserFilter(String userFilter) {
		this.userFilter = userFilter;
	}

	public UserMessageFolder getFolder() {
		return folder;
	}

	public void setFolder(UserMessageFolder folder) {
		this.folder = folder;
	}

	public UserMessageSort getSort() {
		return sort;
	}

	public void setSort(UserMessageSort sort) {
		this.sort = sort;
	}
	
	public String searchFormHTML() {
		StringWriter newWriter = new StringWriter();
		newWriter.write("<form action='" + getBrowseAction() + "' method='get' type='submit' class='search'>\n");
		newWriter.write("<span class='menu'>\n");
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>\n");
		newWriter.write(this.loginBean.translate("Filter"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='filter' type='text' value='");
		newWriter.write(getFilter());
		newWriter.write("' title='");
		newWriter.write(this.loginBean.translate("Filter by topic and message text"));
		newWriter.write("' /></td>\n");
		newWriter.write("</div>\n");
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<span class='search-span'>\n");
		newWriter.write(this.loginBean.translate("User"));
		newWriter.write("</span>\n");
		newWriter.write("<input id='searchtext' name='user-filter' type='text' value='");
		newWriter.write(getUserFilter());
		newWriter.write("' title='");
		newWriter.write(this.loginBean.translate("Filter by sender"));
		newWriter.write("' /></td>\n");
		newWriter.write("</div>\n");
		
		newWriter.write("<div class='search-div'>\n");
		newWriter.write("<input style='display:none;position:absolute;' type='submit' name='search' value='' /></td>\n");
		newWriter.write("</div>\n");
		newWriter.write("</span>\n");
		newWriter.write("</form>\n");
		return newWriter.toString();	
	}

	public String searchHTML(SessionProxyBean proxy) {
		StringWriter writer = new StringWriter();
		List<UserMessage> instances = getAllInstances();
		List<UserMessageConfig> userConversations = groupMessagesToConversations(instances);
		writer.write("<span class='menu'>");
		writer.write(getResultsSize() + " ");
		writer.write(this.loginBean.translate("messages,"));
		if (userConversations != null) {
			writer.write(" " + userConversations.size());
		} else { writer.write(" " + String.valueOf(0)); }
		writer.write(" " + this.loginBean.translate("conversations"));
		writer.write("<br/>");
		writePagingString(writer, instances);
		writer.write("</span>");
		writer.write("<br/>");
		writer.write("<br/>");
		writer.write("<table id='browse-user-messages-table' cellspacing='5'>");
		if (userConversations != null) {
			for (UserMessageConfig config : userConversations) {
				writeMessage(writer, config, proxy);
			}
		}
		writer.write("</table>\n");
		writer.write("<br/>");
		writer.write("<span class = menu>");
		writePagingString(writer, instances);
		writer.write("</span>");
		return writer.toString();
	}
	
	public  String getPostAction() {
		return "user-message";
	}
	
	public void writeMessage(StringWriter writer, UserMessageConfig instance, SessionProxyBean proxy) {
		String targetUser = instance.user;
		writer.write("<tr style='border-bottom:1px solid gray;'>");
		writer.write("<td align='left' valign='top' class='user-thumb' style='padding-bottom:5px;'>");
		writer.write("<a style='text-decoration:none;' href='login?view-user=");
		writer.write(instance.user);
		writer.write("'>");
		writer.write("<img class='user-thumb' src='");
		writer.write(instance.avatar);
		writer.write("'/></a></td><td valign='top'>");
		writer.write("<table class='message-box'>");
		writer.write("<tr><td class='message-message'>");
		writer.write("<a style='text-decoration:none;' href='browse-user-to-user-messages?view-message=");
		writer.write(instance.id + proxy.proxyString());
		writer.write("'>");
		writer.write("<span class='message-subject'>");
		if (instance.subject.startsWith("RE:")) {
			instance.subject = instance.subject.substring(3, instance.subject.length()).trim();
		}
		writer.write(instance.subject);
		writer.write("</span><br/>");
		if (!loginBean.getUserId().equals(instance.target)) {
			writer.write("<span class='menu' style='font-weight:bold'> ");
			writer.write(this.loginBean.translate("to"));
			writer.write(" ");
			writer.write(targetUser);
		} else {
			writer.write("<span class='menu' style='font-weight:bold'> ");
			writer.write(this.loginBean.translate("from"));
			writer.write(" ");
			writer.write(targetUser);
		}
		writer.write(" ");
		writer.write(this.loginBean.translate("sent"));
		writer.write(" ");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			Date date = formatter.parse(instance.creationDate);
			writer.write(Utils.displayTimestamp(date));
		} catch (Exception e) {
			error(e);
		}
		writer.write("</span><br/>");
		writer.write(instance.message);
		writer.write("</a>");
		writer.write("</td></tr></table>");
		writer.write("</td>");
		writer.write("<tr><td style='padding-bottom:5px;'></td>");
	}
	
	/**
	 * Group the messages by the from user and filter to just the latest message from the user.
	 */
	public List<UserMessageConfig> groupMessagesToConversations(List<UserMessage> messagesList) {
		List<UserMessageConfig> userMessages = null;
		if (messagesList != null) {
			userMessages = new ArrayList<UserMessageConfig>();
			HashMap<String, UserMessageConfig> userMap = new HashMap<String, UserMessageConfig>();
			for (UserMessage userMessage : messagesList) {
				UserMessageConfig userMessageConfig = new UserMessageConfig();
				userMessageConfig.id = String.valueOf(userMessage.getId());
				userMessageConfig.creator = userMessage.getCreatorId();
				userMessageConfig.owner = userMessage.getOwner().getUserId();
				userMessageConfig.target = userMessage.getTargetId();
				userMessageConfig.creationDate = userMessage.getCreationDateString();
				userMessageConfig.subject = userMessage.getSubject();
				userMessageConfig.message = userMessage.getMessage();
				if (userMessage.getCreatorId().equals(this.loginBean.getUserId()) && userMessage.getOwner().getUserId().equals(this.loginBean.getUserId())) {
					if (!userMap.containsKey(userMessage.getTargetId())) {
						userMessageConfig.user = userMessage.getTargetId();
						userMap.put(userMessage.getTargetId(), userMessageConfig);
						userMessages.add(userMessageConfig);
					}
				} else if (!userMessage.getCreatorId().equals(this.loginBean.getUserId()) && userMessage.getOwner().getUserId().equals(this.loginBean.getUserId())
						&& userMessage.getTarget().getUserId().equals(this.loginBean.getUserId())) {
					if (!userMap.containsKey(userMessage.getCreatorId())) {
						userMessageConfig.user = userMessage.getCreatorId();
						userMap.put(userMessage.getCreatorId(), userMessageConfig);
						userMessages.add(userMessageConfig);
					}
				}
			}
			for (UserMessageConfig userMessageConfig : userMessages) {
				User messageUser = AdminDatabase.instance().getUser(userMessageConfig.user);
				userMessageConfig.resultsSize = String.valueOf(messagesList.size());
				userMessageConfig.avatar = this.loginBean.getAvatarImage(messageUser);
			}
		}
		return userMessages;
	}

	public List<UserMessageConfig> getUserConversations(UserMessageConfig config) {
		try {
			checkLogin();
			int page = Integer.parseInt(config.page);
			int pageSize = Integer.parseInt(config.pageSize);
			if (pageSize > 1000) {
				throw new BotException("Page size - " +  String.valueOf(pageSize) + " is too big.");
			}
			List<UserMessage> messagesList = AdminDatabase.instance().getAllUserMessages(page, pageSize, null, null, null, getUser(), false);
			return groupMessagesToConversations(messagesList);
		} catch(Exception failed) {
			error(failed);
			return null;
		} 
	}
}
