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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.UserMessage;
import org.botlibre.web.rest.UserMessageConfig;


public class UserToUserMessageBean  extends ServletBean {
		
	String filter = "";
	String viewMessage = "";
	private boolean firstMessageSent = false;
	private String mostRecentDate = "";
	private int messagePage = 0;
	private int messagePageSize = 56;
	private int messageResultsSize = 0;
	
	public void setFirstMessageSent(boolean firstMessageSent) {
		this.firstMessageSent = firstMessageSent;
	}
	
	public boolean getFirstMessageSent() {
		return firstMessageSent;
	}
	
	public void setMostRecentDate(String date) {
		mostRecentDate = date;
	}
	
	public String getMostRecentDate() {
		return mostRecentDate;
	}

	public void setMessagePage(int messagePage) {
		this.messagePage = messagePage;
	}
	
	public int getMessagePage() {
		return messagePage;
	}
	
	public void setMessagePageSize(int messagePageSize) {
		this.messagePageSize = messagePageSize;
	}
	
	public int getMessagePageSize() {
		return messagePageSize;
	}
	
	public void setMessageResultsSize(int messageResultsSize) {
		this.messageResultsSize = messageResultsSize;
	}
	
	public int getMessageResultsSize() {
		return messageResultsSize;
	}
	
	public void resetSearch() {
		this.filter = "";
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
	
	public void setViewMessage(String viewMessage) {
		this.viewMessage = viewMessage;
	}
	
	public String getViewMessage() {
		return viewMessage;
	}
	
	public String searchFormHTML() {
		/*StringWriter newWriter = new StringWriter();
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
		newWriter.write("</span>\n");
		newWriter.write("</form>\n");
		return newWriter.toString();	*/
		return "";
	}
	
	public String searchUserToUserMessagesHTML(SessionProxyBean proxy) {
		StringWriter writer = new StringWriter();
		UserMessage userMessage = this.loginBean.getUserMessage();
		UserMessageConfig config = new UserMessageConfig();
		config.user = loginBean.getUserId();
		config.creator = loginBean.getUserId();
		config.owner = loginBean.getUserId();
		if (!loginBean.getUserId().equals(userMessage.getCreatorId())) {
			config.target = userMessage.getCreatorId();
		} else if (!loginBean.getUserId().equals(userMessage.getOwner().getUserId())) {
			config.target = userMessage.getOwner().getUserId();
		} else {
			config.target = userMessage.getTarget().getUserId();
		}
		config.subject = userMessage.getSubject();
		List<UserMessage> messagesList = AdminDatabase.instance().getUserToUserMessages(this.messagePage, this.messagePageSize, this.filter, config);
		writer.write("<table id='botplatformchatconsole' class='botplatformchatconsole' width='100%' cellspacing='2' style='margin-top:10px;'>");
		if (messagesList != null && messagesList.size() > 0) {
			if (messagesList.size() > this.messagePageSize) {
				messagesList.remove(messagesList.size() - 1);
				setMessageResultsSize(messagesList.size());
			} else if (messagesList.size() == this.messagePageSize) { // means that last page would contain exact messages as page size and there is no need to look for older messages
				setMessageResultsSize(messagesList.size() - 1);
			} else {
				setMessageResultsSize(messagesList.size());
			}
			for (int i = messagesList.size() - 1; i >= 0; i--) {
				UserMessage message = messagesList.get(i);
				writeUserToUserMessage(writer, message, proxy);
			}
			String date = "";
			if (this.messagePage == 0) {
				date = messagesList.get(0).getCreationDateString();
				setMostRecentDate(date);
			}
		}
		writer.write("</table>");
		String targetUserId = "";
		if (!config.creator.equals(this.loginBean.getUserId())) {
			targetUserId = config.creator;
		} else {
			targetUserId = config.target;
		}
		writer.write("<input name='target' type='hidden' value='");
		writer.write(targetUserId);
		writer.write("'/>");
		writer.write("<input name='subject' type='hidden' value='");
		writer.write(config.subject);
		writer.write("'/>");
		return writer.toString();
	}
	
	public  String getPostAction() {
		return "browse-user-to-user-messages";
	}
	
	public void writeUserToUserMessage(StringWriter writer, UserMessage message, SessionProxyBean proxy) {
		writer.write("<tr style='vertical-align:top;'>");
		if (message.getCreatorId().equals(loginBean.getUserId()) && message.getOwner().getUserId().equals(loginBean.getUserId()) && !message.getTargetId().equals(loginBean.getUserId())) {
			writer.write("<td class='botplatformmessage-2' align='left' colspan='2' width='100%'>");
			writer.write("<div class='botplatformmessage-2-div'>");
			writer.write("<span class='botplatformmessage-user-2'>");
			writer.write(this.loginBean.getUserId() + ":  ");
			writer.write("<small>");
			writer.write(Utils.displayTimestamp(message.getCreationDate()));
			writer.write("</small>");
			writer.write("</span>");
			writer.write("<br>");
			writer.write("<div class='botplatformmessage-2-div-2'>");
			writer.write("<span class='botplatformmessage-2'>");
			String messageText = message.getMessageText().replace("\n", "").replace("\r", "");
			writer.write(messageText);
			writer.write("</span>");
			writer.write("</div>");
			writer.write("</div>");
			writer.write("</td>");
		} else if (message.getOwner().getUserId().equals(loginBean.getUserId()) && message.getTargetId().equals(loginBean.getUserId())) {
			writer.write("<td align='left' valign='top' class='botplatformmessage-user-1' nowrap='nowrap' style='padding-bottom:5px;padding-right:15px;'>");
			String targetUser = "";
			if (!loginBean.getUserId().equals(message.getCreatorId())) {
				targetUser = message.getCreator().getUserId();
			} else if (!loginBean.getUserId().equals(message.getOwner().getUserId())) {
				targetUser = message.getOwner().getUserId();
			} else if (!loginBean.getUserId().equals(message.getTargetId())) {
				targetUser = message.getTarget().getUserId();
			}
			writer.write("<a style='text-decoration:none;' href='login?view-user=");
			writer.write(this.loginBean.encodeURI(targetUser) + proxy.proxyString());
			writer.write("'>");
			writer.write("<img class='user-thumb' src='");
			writer.write(this.loginBean.getAvatarThumb(message.getCreator()));
			writer.write("'>");
			writer.write("</a>");
			writer.write("</td>");
			writer.write("<td class='botplatformmessage-1' align='left' width='100%'>");
			writer.write("<div class='botplatformmessage-1-div'>");
			writer.write("<span class='botplatformmessage-user-1'>");
			writer.write(message.getCreatorId() + ":  ");
			writer.write("<small>");
			writer.write(Utils.displayTimestamp(message.getCreationDate()));
			writer.write("</small>");
			writer.write("</span>");
			writer.write("<br>");
			writer.write("<div class='botplatformmessage-1-div-2'>");
			writer.write("<span class='botplatformmessage-1'>");
			String messageText = message.getMessageText().replace("\n", "").replace("\r", "");
			writer.write(messageText);
			writer.write("</span>");
			writer.write("</div>");
			writer.write("</div>");
			writer.write("</td>");
		}
		writer.write("</tr>");
	}

	public List<UserMessageConfig> getUserToUserMessages(UserMessageConfig config) {
		try {
			checkLogin();
			User targetUser = AdminDatabase.instance().getUser(config.target);
			if (targetUser == null) {
				throw new BotException("User does not exists - " + config.target);
			}
			List<UserMessageConfig> messages = null;
			int page = Integer.parseInt(config.page);
			int pageSize = Integer.parseInt(config.pageSize);
			int resultsSize = Integer.parseInt(config.resultsSize);
			if (pageSize > 1000) throw new BotException("Page size - " +  String.valueOf(pageSize) + " is too big.");
			List<UserMessage> messagesList = AdminDatabase.instance().getUserToUserMessages(Integer.parseInt(config.page), Integer.parseInt(config.pageSize), "", config);
			if (messagesList != null) {
				messages = new ArrayList<UserMessageConfig>();
				resultsSize = messagesList.size();
				if (resultsSize > pageSize) {
					messagesList.remove(resultsSize - 1);
				}
				for (int i = messagesList.size() - 1; i >= 0; i--) {
					UserMessage userMessage = messagesList.get(i);
					UserMessageConfig messageConfig = new UserMessageConfig();
					messageConfig.id = Long.toString(userMessage.getId());
					messageConfig.creationDate = userMessage.getCreationDateString();
					messageConfig.owner = userMessage.getOwner().getUserId();
					messageConfig.creator = userMessage.getCreatorId();
					messageConfig.target = userMessage.getTargetId();
					if (userMessage.getParent() == null) {
						messageConfig.parent = "";
					} else {
						messageConfig.parent = String.valueOf(userMessage.getParent().getId());
					}
					messageConfig.subject = userMessage.getSubject();
					messageConfig.message = userMessage.getMessage();
					messageConfig.avatar = loginBean.getAvatarImage(targetUser);
					messageConfig.page = String.valueOf(page);
					messageConfig.pageSize = String.valueOf(pageSize);
					messageConfig.resultsSize = String.valueOf(resultsSize);
					messages.add(messageConfig);
				}
			}
			return messages;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public List<UserMessageConfig> checkUserNewMessages(UserMessageConfig config) {
		try {
			checkLogin();
			List<UserMessageConfig> messages = null;
			List<UserMessage> messagesList = AdminDatabase.instance().checkUserNewMessages(config);
			if (messagesList != null) {
				messages = new ArrayList<UserMessageConfig>();
				Iterator<UserMessage> it = messagesList.iterator();
				while(it.hasNext()) {
					UserMessage userMessage = it.next();
					UserMessageConfig messageConfig = new UserMessageConfig();
					messageConfig.id = Long.toString(userMessage.getId());
					messageConfig.creationDate = userMessage.getCreationDateString();
					messageConfig.creator = userMessage.getCreatorId();
					messageConfig.owner = userMessage.getOwner().getUserId();
					messageConfig.target = userMessage.getTargetId();
					if (userMessage.getParent() == null) {
						messageConfig.parent = "";
					} else {
						messageConfig.parent = String.valueOf(userMessage.getParent().getId());
					}
					messageConfig.subject = userMessage.getSubject();
					messageConfig.message = userMessage.getMessage();
					messageConfig.avatar = loginBean.getAvatarThumb(userMessage.getCreator());
					SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					Date date1 = formater.parse(messageConfig.creationDate);
					Date date2 = formater.parse(config.creationDate);
					if (date1.compareTo(date2) > 0) {
						messages.add(messageConfig);
					}
				}
			}
			return messages;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public UserMessageConfig createUserMessage(UserMessageConfig config, @Context HttpServletRequest requestContext) {
		try {
			checkLogin();
			User target = AdminDatabase.instance().getUser(config.target);
			if ((target != null && target.isPrivate()) && (!loginBean.isAdmin() && !target.getUserId().equals(loginBean.getUser().getUserId()))) {
				throw new BotException("Cannot message private profiles - " + config.target);
			}
			UserMessage message = null;
			UserMessageConfig messageConfig = null;
			if (config.parent != null) {
				UserMessage parentMessage = AdminDatabase.instance().getUserMessage(Long.parseLong(config.parent));
				loginBean.setUserMessage(parentMessage);
				message = loginBean.createUserMessageReply(config.message);
			} else {
				message = loginBean.createUserMessage(config.target, config.subject, config.message, requestContext.getRemoteAddr());
			}
			if (message != null) {
				messageConfig = new UserMessageConfig();
				messageConfig.id = String.valueOf(message.getId());
				messageConfig.creationDate = message.getCreationDateString();
				messageConfig.subject = message.getSubject();
				messageConfig.message = message.getMessage();
				messageConfig.owner = message.getOwner().getUserId();
				messageConfig.creator = message.getCreatorId();
				messageConfig.target = message.getTargetId();
				if (message.getParent() != null) {
					messageConfig.parent = String.valueOf(message.getParent().getId());
				}
				messageConfig.avatar = loginBean.getAvatarImage(message.getTarget());
			}
			return messageConfig;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public boolean deleteUserToUserMessages(boolean confirm, String targetUserId) {
		try {
			if (!confirm) {
				throw new BotException("Must check 'I'm sure'");
			}
			checkLogin();
			UserMessageConfig config = new UserMessageConfig();
			config.user = this.loginBean.getUserId();
			config.target = targetUserId;
			AdminDatabase.instance().deleteUserToUserMessages(config);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
}
