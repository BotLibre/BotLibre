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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AccessMode;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.BotInstance;
import org.botlibre.web.admin.BotMode;
import org.botlibre.web.admin.Category;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.User;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.chat.ChannelAttachment;
import org.botlibre.web.chat.ChatChannel;
import org.botlibre.web.chat.ChatChannel.ChannelType;
import org.botlibre.web.chat.ChatMessage;
import org.botlibre.web.rest.ChannelConfig;
import org.botlibre.web.socket.ChatEndpoint;
import org.botlibre.web.service.Stats;

public class LiveChatBean extends WebMediumBean<ChatChannel> {

	String duration = "";
	String search = "messages";
	String filter = "";
	boolean selectAllLogs;
	boolean showTitle = true;
	boolean menuBar = true;
	boolean sendImage = true;
	boolean chatLog = true;
	boolean showChatBubble = false;
    
	String send = "Send";
	String prompt = "You say";
	String info = "";
	String language = "";
	List<ChatMessage> logResults = new ArrayList<ChatMessage>();
	List<ChannelAttachment> attachmentsResults = new ArrayList<ChannelAttachment>();
	
	public LiveChatBean() {
	}
	
	@Override
	public boolean allowSubdomain() {
		return true;
	}

	public void setChatLog(boolean chatlog) {
		this.chatLog = chatlog;
	}
	
	public boolean getChatLog() {
		return chatLog;
	}
	
	public void setShowChatBubble(boolean bubble) {
		this.showChatBubble = bubble;
	}
	
	public boolean getShowChatBubble() {
		return showChatBubble;
	}
	
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean getShowTitle() {
		return showTitle;
	}

	public void setShowTitle(boolean showTitle) {
		this.showTitle = showTitle;
	}

	public void setMenubar(boolean menubar) {
		this.menuBar = menubar;
	}
	
	public boolean getMenubar() {
		return menuBar;
	}
	
	public void setSendImage(boolean sendImage) {
		this.sendImage = sendImage;
	}
	
	public boolean getSendImage() {
		return sendImage;
	}
	
	public String getSend() {
		return send;
	}

	public void setSend(String send) {
		this.send = send;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	@Override
	public String getPostAction() {
		return "livechat";
	}
	
	@Override
	public void writeBrowseStats(StringWriter writer, ChatChannel instance) {
		writer.write("User online: " + instance.getConnectedUsersCount() + "<br/>\n");
		writer.write("Admins online: " + instance.getConnectedAdminsCount() + "<br/>\n");
		writer.write("Messages: " + instance.getMessages() + "<br/>\n");
	}
	
	@Override
	public void writeSearchOptions(StringWriter writer) {
		writer.write("<option value='Messages' " + getInstanceSortCheckedString(InstanceSort.Messages) + ">messages</option>\n");
		writer.write("<option value='Users' " + getInstanceSortCheckedString(InstanceSort.Users) + ">users online</option>\n");
	}
	
	@Override
	public void writeInfoTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write("<span>Channel Type: ");
			out.write(String.valueOf(getDisplayInstance().getType()));
			out.write("</span><br/>\n");
			if (!getDisplayInstance().isExternal()) {
				out.write("<span>Users currently online: ");
				out.write(String.valueOf(getDisplayInstance().getConnectedUsersCount()));
				out.write("</span><br/>\n");
				out.write("<span>Admins currently online: ");
				out.write(String.valueOf(getDisplayInstance().getConnectedAdminsCount()));
				out.write("</span><br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	@Override
	public void writeStatsTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			if (!getDisplayInstance().isExternal()) {
				out.write("Messages: ");
				out.write(String.valueOf(getDisplayInstance().getMessages()));
				out.write("<br/>\n");
			}
		} catch (Exception exception) {
			error(exception);
		}
	}

	@Override
	public void writeMenuExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (getInstance() == null || getInstance().isExternal() || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='livechat?shutdown");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write(this.loginBean.postTokenString());
			out.write("' class='button' title='Shutdown the chat room'><img src='images/logout.svg' class='menu'/> Shutdown</a></td>\n");
			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public ChannelAttachment addAttachment(byte[] image, String name, String type, String user, String token) {
		try {
			checkInstance();
			ChannelAttachment attachment = new ChannelAttachment();
			attachment.setName(name);
			attachment.setType(type);
			attachment.checkAttachmentType();
			attachment.generateKey();
			Media media = new Media();
			media.setMedia(image);
			return AdminDatabase.instance().addChannelAttachment(attachment, media, this.instance, user, token);
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public boolean addOperator(String userid) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if ((userid == null) || userid.isEmpty()) {
				throw new BotException("Please select a user");
			}
			setInstance(AdminDatabase.instance().addChannelOperator(this.instance, userid));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean removeOperator(String userid) {
		try {
			checkLogin();
			checkInstance();
			setInstance(AdminDatabase.instance().validate(ChatChannel.class, this.instance.getId(), getUser().getUserId()));
			checkAdmin();
			if ((userid == null) || userid.isEmpty()) {
				throw new BotException("Please select a user");
			}
			setInstance(AdminDatabase.instance().removeChannelOperator(this.instance, userid));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public List<ChannelAttachment> getAttachmentsResults() {
		return attachmentsResults;
	}

	public void setAttachmentsResults(List<ChannelAttachment> attachmentsResults) {
		this.attachmentsResults = attachmentsResults;
	}

	public boolean getSelectAllLogs() {
		return selectAllLogs;
	}

	public void setSelectAllLogs(boolean selectAllLogs) {
		this.selectAllLogs = selectAllLogs;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
		this.duration = "";
		this.search = "messages";
		this.filter = "";
		this.selectAllLogs = false;
		this.logResults = new ArrayList<ChatMessage>();
		this.attachmentsResults = new ArrayList<ChannelAttachment>();
		if (!getLoginBean().isEmbedded()) {
			this.send = "Send";
			this.prompt = "You say";
			this.showTitle = true;
			showChatBubble = false;
		}
	}

	@Override
	public String getEmbeddedBanner() {
		return "channel-banner.jsp";
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

	public String isVideoAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Users;
		if (getEditInstance() != null) {
			mode = getEditInstance().getVideoAccessMode();
		}
		if (mode != null && mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isAudioAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Users;
		if (getEditInstance() != null) {
			mode = getEditInstance().getAudioAccessMode();
		}
		if (mode != null && mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}

	public String isInviteAccessModeSelected(String type) {
		AccessMode mode = AccessMode.Users;
		if (getEditInstance() != null) {
			mode = getEditInstance().getInviteAccessMode();
		}
		if (mode != null && mode.name().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String getDurationCheckedString(String duration) {
		if (duration.equals(this.duration)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getSearchCheckedString(String search) {
		if (search.equals(this.search)) {
			return "selected=\"selected\"";
		}
		return "";
	}
	
	public String getLogCheckedString() {
		if (this.selectAllLogs) {
			return "checked=\"checked\"";
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	public void deleteChatLogs(HttpServletRequest request) {
		this.selectAllLogs = false;
		Set<Long> ids = new HashSet<Long>();
		for (Object parameter : request.getParameterMap().entrySet()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>)parameter;
			String key = entry.getKey();
			try {
				ids.add(Long.valueOf(key));
			} catch (NumberFormatException ignore) {}
		}
		for (Iterator<ChatMessage> iterator = this.logResults.iterator(); iterator.hasNext(); ) {
			ChatMessage message = iterator.next();
			if (ids.contains(message.getId())) {
				AdminDatabase.instance().delete(message);
				iterator.remove();
			}
		}
		for (Iterator<ChannelAttachment> iterator = this.attachmentsResults.iterator(); iterator.hasNext(); ) {
			ChannelAttachment attachment = iterator.next();
			if (ids.contains(attachment.getMediaId())) {
				AdminDatabase.instance().deleteChannelAttachment(attachment);
				iterator.remove();
			}
		}
	}
	
	public void clearLogResults() {
		this.logResults = new ArrayList<ChatMessage>();
		this.duration = "";
		this.selectAllLogs = false;
	}
	
	public void queryChatLogs(String search, String duration, String filter) {
		this.selectAllLogs = false;
		this.duration = duration;
		this.search = search;
		this.filter = filter;
		if (duration.equals("") || duration.equals("none")) {
			this.logResults = new ArrayList<ChatMessage>();
			this.attachmentsResults = new ArrayList<ChannelAttachment>();
			return;
		}
		try {
			Calendar start = Calendar.getInstance();
			start.add(Calendar.DAY_OF_YEAR, -1);
			if (duration.equals("week")) {
				start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) - 7);
			} else if (duration.equals("month")) {
				start.set(Calendar.DAY_OF_YEAR, start.get(Calendar.DAY_OF_YEAR) - 30);
			} else if (duration.equals("all")) {
				start = null;
			}
			if (search.equals("attachments")) {
				this.logResults = new ArrayList<ChatMessage>();
				this.attachmentsResults = AdminDatabase.instance().getAllChannelAttachments(this.instance, 0, 1000, filter, start, null, InstanceFilter.Public, InstanceSort.Date, getUser(), getDomain());
			} else {
				this.attachmentsResults = new ArrayList<ChannelAttachment>();
				this.logResults = AdminDatabase.instance().getAllChats(this.instance, 0, 1000, filter, start, null, InstanceFilter.Public, InstanceSort.Date, getUser(), getDomain());
			}
		} catch (Exception failed) {
			error(failed);
			this.logResults = new ArrayList<ChatMessage>();
			this.attachmentsResults = new ArrayList<ChannelAttachment>();
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
	
	public List<ChatMessage> getLogResults() {
		return logResults;
	}

	public void setLogResults(List<ChatMessage> logResults) {
		this.logResults = logResults;
	}

	public List<ChatChannel> getAllInstances(Domain domain) {
		try {
			List<ChatChannel> results = AdminDatabase.instance().getAllChannels(this.page, this.pageSize, this.categoryFilter, this.nameFilter, this.userFilter, 
					this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating,this.tagFilter, this.startFilter, this.endFilter, getUser(), domain);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllChannelsCount(this.categoryFilter, this.nameFilter, this.userFilter, 
							this.instanceFilter, this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<ChatChannel>();
		}
	}

	public List<ChatChannel> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllChannels(
					0, 100, "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", "", "", null, getDomain());
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<ChatChannel>();
		}
	}
	
	public boolean allowsMedia() {
		return this.instance != null &&
				(this.instance.getVideoAccessMode() != AccessMode.Disabled || this.instance.getAudioAccessMode() != AccessMode.Disabled);
	}
	
	public boolean allowVideo() {
		if (this.instance == null || this.instance.getVideoAccessMode() == AccessMode.Disabled) {
			return false;
		}
		if (getUser() != null && getUser().isFlagged()) {
			return false;
		}
		if (this.instance.getVideoAccessMode() == AccessMode.Everyone) {
			return true;
		}
		if (!isLoggedIn()) {
			return false;
		}
		if (this.instance.getVideoAccessMode() == AccessMode.Users) {
			return true;
		}
		if (this.instance.isAdmin(getUser())) {
			return true;
		}
		if (this.instance.getVideoAccessMode() == AccessMode.Members && this.instance.isAdmin(getUser())) {
			return true;
		}
		return false;
	}
	
	public boolean allowAudio() {
		if (this.instance == null || this.instance.getAudioAccessMode() == AccessMode.Disabled) {
			return false;
		}
		if (getUser() != null && getUser().isFlagged()) {
			return false;
		}
		if (this.instance.getAudioAccessMode() == AccessMode.Everyone) {
			return true;
		}
		if (!isLoggedIn()) {
			return false;
		}
		if (this.instance.getAudioAccessMode() == AccessMode.Users) {
			return true;
		}
		if (this.instance.isAdmin(getUser())) {
			return true;
		}
		if (this.instance.getAudioAccessMode() == AccessMode.Members && this.instance.isAdmin(getUser())) {
			return true;
		}
		return false;
	}
	
	public boolean allowInvite() {
		if (this.instance == null || this.instance.getInviteAccessMode() == AccessMode.Disabled) {
			return false;
		}
		if (getUser() != null && getUser().isFlagged()) {
			return false;
		}
		if (this.instance.getInviteAccessMode() == AccessMode.Everyone) {
			// Require users.
			//return true;
		}
		if (!isLoggedIn()) {
			return false;
		}
		if (this.instance.getInviteAccessMode() == AccessMode.Users) {
			return true;
		}
		if (this.instance.isAdmin(getUser())) {
			return true;
		}
		if (this.instance.getInviteAccessMode() == AccessMode.Members && this.instance.isAdmin(getUser())) {
			return true;
		}
		return false;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/chat.png";
		}
		return file;
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/chat-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarThumb(WebMedium instance) {
		if (instance instanceof ChatChannel) {
			if ((instance.getAvatar() == null) && ((ChatChannel)instance).hasBot()) {
				return "images/bot-thumb.jpg";
			}
		}
		return super.getAvatarThumb(instance);
	}

	@Override
	public String getAvatarImage(WebMedium instance) {
		if (instance instanceof ChatChannel) {
			if ((instance.getAvatar() == null) && ((ChatChannel)instance).hasBot()) {
				return "images/bot.png";
			}
		}
		return super.getAvatarImage(instance);
	}

	public boolean createInstance(ChannelConfig config) {
		try {
			checkLogin();
			config.sanitize();
			ChatChannel newInstance = new ChatChannel(config.name);
			setInstance(newInstance);
			newInstance.setType(ChannelType.valueOf(config.type));
			updateFromConfig(newInstance, config);
			if (config.videoAccessMode != null) {
				newInstance.setVideoAccessMode(AccessMode.valueOf(config.videoAccessMode));
			}
			if (config.audioAccessMode != null) {
				newInstance.setAudioAccessMode(AccessMode.valueOf(config.audioAccessMode));
			}
			if (config.inviteAccessMode != null) {
				newInstance.setInviteAccessMode(AccessMode.valueOf(config.inviteAccessMode));
			}
			newInstance.setDomain(getDomain());
			checkVerfied(config);
			//AdminDatabase.instance().validateNewChannel(newInstance.getAlias(), config.description, config.tags, config.isAdult, getDomain());
			setSubdomain(config.subdomain, newInstance);
			Stats.stats.chatCreates++;
			setInstance(AdminDatabase.instance().createChannel(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create a directory link.
	 */
	public boolean createLink(ChannelConfig config) {
		try {
			if (Site.READONLY) {
				throw new BotException("This website is currently undergoing maintence, please try later.");
			}
			checkLogin();
			config.sanitize();
			ChatChannel newInstance = new ChatChannel(config.name);
			newInstance.setType(ChannelType.valueOf(config.type));
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
				throw new BotException("You must enter a valid URL for an external channel");
			}
			//AdminDatabase.instance().validateNewChannel(newInstance.getAlias(), config.description, config.tags, config.isAdult, getDomain());			
			setInstance(AdminDatabase.instance().createChannel(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	/**
	 * Create a live chat channel for the bot.
	 */
	public boolean createBotChannel(ChannelType type, BotBean botBean) {
		try {
			BotInstance bot = botBean.getInstance();
			String name = null;
			String alias = null;
			if (type == ChannelType.OneOnOne) {
				name = botBean.getInstance().getName() + " Live Chat";
				alias = botBean.getInstance().getAlias() + "LiveChat";
			} else {
				name = botBean.getInstance().getName() + " Chat Room";
				alias = botBean.getInstance().getAlias() + "ChatRoom";
			}
			ChatChannel newInstance = new ChatChannel(name);
			newInstance.setAlias(alias);
			newInstance.setType(type);
			newInstance.setDomain(bot.getDomain());
			newInstance.setDescription(bot.getDescription());
			newInstance.setDetails(bot.getDetails());
			newInstance.setDisclaimer(bot.getDisclaimer());
			newInstance.setLicense(bot.getLicense());
			newInstance.setAdult(bot.isAdult());
			newInstance.setPrivate(bot.isPrivate());
			newInstance.setHidden(bot.isHidden());
			newInstance.setAccessMode(bot.getAccessMode());
			newInstance.setContentRating(bot.getContentRating());
			newInstance.setCreator(bot.getCreator());
			newInstance.setAdCode(bot.getAdCode());
			newInstance.setShowAds(bot.getShowAds());
			newInstance.setCreator(bot.getCreator());
			setInstance(newInstance);
			//AdminDatabase.instance().validateNewChannel(newInstance.getAlias(), bot.getDescription(), bot.getTagsString(), bot.isAdult(), bot.getDomain());
			for (Category botCategory : bot.getCategories()) {
				checkCategory(botCategory);
			}
			Stats.stats.chatCreates++;
			setInstance(AdminDatabase.instance().createChannel(newInstance, bot.getCreator(), bot.getCategoriesString(), bot.getTagsString(), this.loginBean));
			setInstance((ChatChannel)AdminDatabase.instance().updateChannelSettings(newInstance, bot.getId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	/**
	 * Check if the bot has a live chat channel, otherwise create it.
	 */
	public boolean checkBotChannel(ChannelType type, BotBean botBean) {
		String alias = null;
		String oldAlias = null;
		if (type == ChannelType.OneOnOne) {
			alias = botBean.getInstance().getAlias() + "LiveChat";
			oldAlias = botBean.getInstance().getName() + " Live Chat";
		} else {
			alias = botBean.getInstance().getAlias() + "ChatRoom";
			oldAlias = botBean.getInstance().getName() + " Chat Room";
		}
		boolean found = validateInstance(alias, botBean.getInstance().getDomain());
		if (!found) {
			// Also check old alias.
			found = validateInstance(oldAlias, botBean.getInstance().getDomain());
		}
		if (found) {
			if (!botBean.getInstance().getCreator().equals(getInstance().getCreator())) {
				setInstance(null);
				found = false;
				// If the creator does not match and was using the old alias, then create under the new alias.
				if (getInstance().getAlias().equals(alias)) {
					this.loginBean.setError(new BotException("Invalid live chat channel, contact support"));
					return false;
				}
			}
		}
		this.loginBean.setError(null);
		if (!found) {
			createBotChannel(type, botBean);
			if (botBean.getInstance().isReviewed()) {
				AdminDatabase.instance().updateReviewed(this.instance);
			}
		}
		try {
			BotInstance bot = botBean.getInstance();
			if (bot.getAvatar() != null
						&& (this.instance.getAvatar() == null || (bot.getAvatar().getImage().length != this.instance.getAvatar().getImage().length))) {
				setInstance(AdminDatabase.instance().update(this.instance, bot.getAvatar().getImage()));
				if (botBean.getInstance().isReviewed()) {
					AdminDatabase.instance().updateReviewed(this.instance);
				}
			} else if (bot.getAvatar() == null && bot.getInstanceAvatar() != null && bot.getInstanceAvatar().getAvatar() != null
						&& (this.instance.getAvatar() == null || (bot.getInstanceAvatar().getAvatar().getImage().length != this.instance.getAvatar().getImage().length))) {
				setInstance(AdminDatabase.instance().update(this.instance, bot.getInstanceAvatar().getAvatar().getImage()));
				if (botBean.getInstance().isReviewed()) {
					AdminDatabase.instance().updateReviewed(this.instance);
				}
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Create the personal live chat channel for the user.
	 */
	public boolean createUserChannel(ChannelType type, BotBean botBean) {
		try {
			String name = null;
			String alias = null;
			if (type == ChannelType.OneOnOne) {
				name = getUser().getUserId() + " Live Chat";
				alias = getUser().getUserId() + "LiveChat";
			} else {
				name = getUser().getUserId() + " Chat Room";
				alias = getUser().getUserId() + "ChatRoom";
			}
			ChatChannel newInstance = new ChatChannel(name);
			newInstance.setAlias(alias);
			newInstance.setType(type);
			newInstance.setDescription(getUser().getBioText());
			newInstance.setPrivate(getUser().isPrivate());
			newInstance.setHidden(!getUser().isPublic());
			newInstance.setCreator(getUser());
			newInstance.setDomain(getDomain());
			setInstance(newInstance);
			Category category = new Category();
			category.setName("Personal");
			category.setDomain(getDomain());
			checkCategory(category);
			Stats.stats.chatCreates++;
			setInstance(AdminDatabase.instance().createChannel(newInstance, getUser(), "Personal", getUser().getTagsString(), this.loginBean));
			setInstance((ChatChannel)AdminDatabase.instance().updateChannelSettings(newInstance, botBean.getInstanceId()));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	/**
	 * Check if the bot has a live chat channel, otherwise create it.
	 */
	public boolean checkUserChannel(User user, ChannelType type, BotBean botBean) {
		try {
			String alias = null;
			if (type == ChannelType.OneOnOne) {
				alias = user.getUserId() + "LiveChat";
			} else {
				alias = user.getUserId() + "ChatRoom";
			}
			boolean found = validateInstance(alias, botBean.getInstance().getDomain());
			if (!found) {
				if (!this.loginBean.isLoggedIn() || !user.equals(getUser())) {
					throw new BotException("This user does not have a personal channel");
				}
				this.loginBean.setError(null);
				createUserChannel(type, botBean);
			} else {
				if (!botBean.getInstance().getCreator().equals(getInstance().getCreator())) {
					throw new BotException("Invalid live chat channel, contact support");
				}
			}
			if (user.getAvatar() != null
					&& (this.instance.getAvatar() == null || (user.getAvatar().getImage().length != this.instance.getAvatar().getImage().length))) {
				setInstance(AdminDatabase.instance().update(this.instance, getUser().getAvatar().getImage()));
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean shutdownRoom() {
		try {
			checkLogin();
			checkInstance();
			setInstance((ChatChannel)AdminDatabase.instance().validate(ChatChannel.class, this.instance.getId(), getUser().getUserId()));
			checkAdmin();
			ChatEndpoint.shutdown(this.instance);
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateInstance(ChannelConfig config, String newdomain, Boolean featured, Boolean adVerified) {
		try {
			checkLogin();
			checkInstance();
			config.sanitize();
			setInstance((ChatChannel)AdminDatabase.instance().validate(ChatChannel.class, this.instance.getId(), getUser().getUserId()));
			checkAdminOrSuper();
			ChatChannel newInstance = (ChatChannel)this.instance.clone();
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
			newInstance.setType(ChannelType.valueOf(config.type));
			if (config.videoAccessMode != null) {
				newInstance.setVideoAccessMode(AccessMode.valueOf(config.videoAccessMode));
			}
			if (config.audioAccessMode != null) {
				newInstance.setAudioAccessMode(AccessMode.valueOf(config.audioAccessMode));
			}
			if (config.inviteAccessMode != null) {
				newInstance.setInviteAccessMode(AccessMode.valueOf(config.inviteAccessMode));
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
			setInstance((ChatChannel)AdminDatabase.instance().updateChannel(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean saveSettings(String welcomeMessage, String statusMessage, String bot, String mode,
				String emailAddress, String emailUserName, String emailPassword, String emailProtocol, Boolean emailSSL,
				String emailIncomingHost, String emailIncomingPort, String emailOutgoingHost, String emailOutgoingPort,
				String emailTopic, String emailBody) {
		try {
			welcomeMessage = Utils.sanitize(welcomeMessage);
			statusMessage = Utils.sanitize(statusMessage);
			mode = Utils.sanitize(mode);
			emailAddress = Utils.sanitize(emailAddress);
			emailUserName = Utils.sanitize(emailUserName);
			emailProtocol = Utils.sanitize(emailProtocol);
			emailIncomingHost = Utils.sanitize(emailIncomingHost);
			emailIncomingPort = Utils.sanitize(emailIncomingPort);
			emailOutgoingHost = Utils.sanitize(emailOutgoingHost);
			emailOutgoingPort = Utils.sanitize(emailOutgoingPort);
			emailTopic = Utils.sanitize(emailTopic);
			emailBody = Utils.sanitize(emailBody);
			checkLogin();
			checkInstance();
			setInstance((ChatChannel)AdminDatabase.instance().validate(ChatChannel.class, this.instance.getId(), getUser().getUserId()));
			checkAdmin();
			ChatChannel newInstance = (ChatChannel)this.instance.clone();
			if (welcomeMessage != null) {
				if (!isSuper()) {
					Utils.checkScript(welcomeMessage);
				}
				newInstance.setWelcomeMessage(Utils.sanitize(welcomeMessage));
			}
			if (statusMessage != null) {
				if (!isSuper()) {
					Utils.checkScript(statusMessage);
				}
				newInstance.setStatusMessage(Utils.sanitize(statusMessage));
			}
			newInstance.setBotMode(BotMode.valueOf(mode));
			Long id = null;
			if (!bot.trim().isEmpty()) {
				if (!getBotBean().validateInstance(bot)) {
					return false;
				}
				id = getBotBean().getInstanceId();
			}
			if (emailAddress != null) {
				newInstance.setEmailAddress(emailAddress);
			}
			if (emailUserName != null) {
				newInstance.setEmailUserName(emailUserName);
			}
			if (emailPassword != null) {
				newInstance.setEmailPassword(emailPassword);
			}
			if (emailProtocol != null) {
				newInstance.setEmailProtocol(emailProtocol);
			}
			if (emailSSL != null) {
				newInstance.setEmailSSL(emailSSL);
			}
			if (emailIncomingHost != null) {
				newInstance.setEmailIncomingHost(emailIncomingHost);
			}
			if (emailIncomingPort != null) {
				newInstance.setEmailIncomingPort(Integer.valueOf(emailIncomingPort));
			}
			if (emailOutgoingHost != null) {
				newInstance.setEmailOutgoingHost(emailOutgoingHost);
			}
			if (emailOutgoingPort != null) {
				newInstance.setEmailOutgoingPort(Integer.valueOf(emailOutgoingPort));
			}
			if (emailTopic != null) {
				newInstance.setEmailTopic(emailTopic);
			}
			if (emailBody != null) {
				newInstance.setEmailBody(emailBody);
			}
			setInstance((ChatChannel)AdminDatabase.instance().updateChannelSettings(newInstance, id));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Download the attachment media.
	 */
	public boolean downloadAttachment(HttpServletResponse response, String id, String key) {
		ChannelAttachment attachment = null;
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
			attachment = AdminDatabase.instance().findChannelAttachment(mediaId);
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
	
	@Override
	public Class<ChatChannel> getType() {
		return ChatChannel.class;
	}
	
	@Override
	public String getTypeName() {
		return "Channel";
	}

	@Override
	public String getCreateURL() {
		return "create-channel.jsp";
	}

	@Override
	public String getSearchURL() {
		return "channel-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-channel.jsp";
	}
	
	public void testEmail(String address) {
		sendEmail(address, "test", null, "this is a test");
	}
	
	public void sendEmailInBackground(final String address, final String subject, final String text,  final String html) {
		Runnable task = new Runnable() {			
			@Override
			public void run() {
				sendEmail(address, subject, text, html);
			}
		};
		Thread thread = new Thread(task);
		thread.start();
	}
	
	public void sendEmail(final String address, final String subject, final String text,  final String html) {
		Store store = null;
		try {
			Stats.stats.emails++;
			AdminDatabase.instance().log(Level.INFO, "Sending email", address, subject);
	        //store = connectStore();
			Session session = connectSession();

			MimeMessage message = new MimeMessage(session);
		    message.setFrom(new InternetAddress(this.instance.getEmailAddress()));
		    message.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
		    message.setSubject(subject);
		    if (html != null) {
			    message.setContent(html, "text/html; charset=UTF-8");
		    } else {
		    	message.setText(text);
		    }

		    // Send message
		    Transport.send(message);
		} catch (Throwable messagingException) {
			AdminDatabase.instance().log(messagingException);
			error(messagingException);
		} finally {
			try {
				if (store != null) {
					store.close();
				}
			} catch (Exception ignore) {}
		}
	}
	
	public Store connectStore() throws MessagingException {
		Properties properties = new Properties();
		properties.put("mail." + this.instance.getEmailProtocol() + ".timeout", 5000);
		properties.put("mail." + this.instance.getEmailProtocol() + ".connectiontimeout", 5000);
		//properties.setProperty("mail.store.protocol", getProtocol());
	    Session session = Session.getInstance(properties, null);
	    Store store = session.getStore(this.instance.getEmailProtocol());
	    if (this.instance.getEmailIncomingPort() == 0) {
	    	store.connect(this.instance.getEmailIncomingHost(), this.instance.getEmailUserName(), this.instance.getEmailPassword());
	    } else {
	    	store.connect(this.instance.getEmailIncomingHost(), this.instance.getEmailIncomingPort(), this.instance.getEmailUserName(), this.instance.getEmailPassword());
	    }
	    return store;
	}
	
	public Session connectSession() {			 
		Properties props = new Properties();
		Session session = null;
		if (this.instance.getEmailSSL()) {
			props.put("mail.smtp.host", this.instance.getEmailOutgoingHost());
			props.put("mail.smtp.port", this.instance.getEmailOutgoingPort());
			props.put("mail.smtp.socketFactory.port", this.instance.getEmailOutgoingPort());
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			 
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(instance.getEmailUserName(), instance.getEmailPassword());
				}
			});
		} else {
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", this.instance.getEmailOutgoingHost());
			props.put("mail.smtp.port", this.instance.getEmailOutgoingPort());
			 
			session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(instance.getEmailUserName(), instance.getEmailPassword());
				}
			});
		}
		return session;
	}
	
}
