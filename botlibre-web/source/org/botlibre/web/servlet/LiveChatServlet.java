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
package org.botlibre.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.ChannelEmbedTabBean;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LiveChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.ChannelConfig;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/livechat")
@SuppressWarnings("serial")
public class LiveChatServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		if (proxy == null) {
			proxy = new SessionProxyBean();
		}
		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		if (loginBean == null) {
			request.getRequestDispatcher("index.jsp").forward(request, response);
			return;
		}
		LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
		loginBean.setActiveBean(bean);
		
		try {
			String postToken = (String)request.getParameter("postToken");
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
			String attachment = (String)request.getParameter("attachment");
			String key = (String)request.getParameter("key");
			if (attachment != null) {
				bean.downloadAttachment(response, attachment, key);
				return;
			}
			String domain = (String)request.getParameter("domain");
			if (domain != null) {
				DomainBean domainBean = loginBean.getBean(DomainBean.class);
				if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
					domainBean.validateInstance(domain);
				}
			}
			String id = (String)request.getParameter("id");
			String chat = (String)request.getParameter("chat");
			if (id != null) {
				if (proxy != null) {
					proxy.setInstanceId(id);
				}
				if (!bean.validateInstance(id)) {
					request.getRequestDispatcher("channel.jsp").forward(request, response);
					return;
				}
				String send = (String)request.getParameter("send");
				String prompt = (String)request.getParameter("prompt");
				String showTitle = (String)request.getParameter("showTitle");
				String info = (String)request.getParameter("info");
				String chatLog = (String)request.getParameter("chatLog");
				String showMenubar = (String)request.getParameter("menubar");
				String showSendImage = (String)request.getParameter("sendImage");
				String showChatBubble = (String)request.getParameter("bubble");
				String language = (String)request.getParameter("language");
				String translate = (String)request.getParameter("translate");
				if (send != null) {
					bean.setSend(Utils.sanitize(send));
				}
				if (prompt != null) {
					bean.setPrompt(Utils.sanitize(prompt));
				}
				if (showTitle != null) {
					bean.setShowTitle(!showTitle.equals("false"));
				}
				if (info != null) {
					bean.setInfo(Utils.sanitize(info));
				}
				if (chatLog != null) {
					bean.setChatLog(!chatLog.equals("false"));
				}
				if (showMenubar != null) {
					bean.setMenubar(!showMenubar.equals("false"));
				}
				if (showSendImage != null) {
					bean.setSendImage(!showSendImage.equals("false"));
				}
				if (showChatBubble != null) {
					bean.setShowChatBubble(!showChatBubble.equals("false"));
				}
				if (language != null) {
					loginBean.setLanguage(Utils.sanitize(language));
				}
				if (translate != null) {
					bean.setLanguage(Utils.sanitize(translate));
				}
				if (chat != null) {
					if (bean.getInstance() != null && bean.getInstance().isExternal()) {
						bean.incrementConnects(ClientType.WEB);
						response.sendRedirect(bean.getInstance().getWebsiteURL());
						return;
					}
					request.getRequestDispatcher("livechat.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("channel.jsp").forward(request, response);
				}
				return;
			}
			String logs = (String)request.getParameter("logs");
			if (logs == null) {
				bean.clearLogResults();
			}
			String cancel = (String)request.getParameter("cancel");
			if (cancel!= null) {
				response.sendRedirect("browse-channel.jsp");
				return;
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String disconnect = (String)request.getParameter("disconnect");
			if (disconnect != null) {
				response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String createChannel = (String)request.getParameter("create-channel");
			if (createChannel != null) {
				request.getRequestDispatcher("create-channel.jsp").forward(request, response);
				return;
			}
			String createChannelLink = (String)request.getParameter("create-channel-link");
			if (createChannelLink != null) {
				request.getRequestDispatcher("create-channel-link.jsp").forward(request, response);
				return;
			}
			String createBot = (String)request.getParameter("create-bot");
			if (createBot != null) {
				response.sendRedirect("create-instance.jsp");
				return;
			}
			if (chat != null) {
				if (bean.getInstance() != null && bean.getInstance().isExternal()) {
					bean.incrementConnects(ClientType.WEB);
					response.sendRedirect(bean.getInstance().getWebsiteURL());
					return;
				}
				request.getRequestDispatcher("livechat.jsp").forward(request, response);
				return;
			}
			String editInstance = (String)request.getParameter("edit-instance");
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-channel.jsp").forward(request, response);
				return;
			}
			String embedInstance = (String)request.getParameter("embed-instance");
			if (embedInstance != null) {
				request.getRequestDispatcher("channel-embedtab.jsp").forward(request, response);
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-channel.jsp").forward(request, response);
				return;
			}
			String addOperator = (String)request.getParameter("addOperator");
			String newOperator = (String)request.getParameter("newOperator");
			if (addOperator != null) {
				loginBean.verifyPostToken(postToken);
				bean.addOperator(newOperator);
				request.getRequestDispatcher("channel-users.jsp").forward(request, response);
				return;
			}
			String removeOperator = (String)request.getParameter("removeOperator");
			String operator = (String)request.getParameter("selected-operator");
			if (removeOperator != null) {
				loginBean.verifyPostToken(postToken);
				bean.removeOperator(operator);
				request.getRequestDispatcher("channel-users.jsp").forward(request, response);
				return;
			}
			String deleteLogs = (String)request.getParameter("deleteLogs");
			if (deleteLogs != null) {
				loginBean.verifyPostToken(postToken);
				bean.deleteChatLogs(request);
				request.getRequestDispatcher("channel-logs.jsp").forward(request, response);
				return;
			}
			String selectAllLogs = (String)request.getParameter("selectAllLogs");
			if (selectAllLogs != null) {
				loginBean.verifyPostToken(postToken);
				bean.setSelectAllLogs(!bean.getSelectAllLogs());
				request.getRequestDispatcher("channel-logs.jsp").forward(request, response);
				return;
			}
			if (logs != null) {
				bean.queryChatLogs(request.getParameter("search"), request.getParameter("duration"), request.getParameter("filter"));
				request.getRequestDispatcher("channel-logs.jsp").forward(request, response);
				return;
			}
			
			if (checkCommon(bean, "livechat?id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "channel-users.jsp", request, response)) {
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");

			String search = (String)request.getParameter("search-channel");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("channel-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-channel.jsp").forward(request, response);
				return;
			}
			String create = (String)request.getParameter("create-channel");
			if (create != null) {
				request.getRequestDispatcher("create-channel.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Channel");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Channel");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Channel");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-channel.jsp");
				return;
			}
			
			ChannelConfig config = new ChannelConfig();
			updateParameters(config, request);
			String newInstance = (String)request.getParameter("newInstance");
			String newdomain = (String)request.getParameter("newdomain");
			config.subdomain = (String)request.getParameter("subdomain");
			config.type = (String)request.getParameter("type");
			config.videoAccessMode = (String)request.getParameter("videoAccessMode");
			config.audioAccessMode = (String)request.getParameter("audioAccessMode");
			config.inviteAccessMode = (String)request.getParameter("inviteAccessMode");
			String isFeatured = (String)request.getParameter("isFeatured");
			String adVerified = (String)request.getParameter("adVerified");
			String delete = (String)request.getParameter("delete");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = newInstance;
				if (!bean.createInstance(config)) {
					request.getRequestDispatcher("create-channel.jsp").forward(request, response);
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = newInstance;
				if (!bean.createLink(config)) {
					request.getRequestDispatcher("create-channel-link.jsp").forward(request, response);
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String saveInstance = (String)request.getParameter("save-instance");
			if (saveInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.updateInstance(config, newdomain, "on".equals(isFeatured), "on".equals(adVerified))) {
					request.getRequestDispatcher("edit-channel.jsp").forward(request, response);
				} else {
					response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String saveBot = (String)request.getParameter("save-bot");
			String botMode = (String)request.getParameter("bot-mode");
			String bot = (String)request.getParameter("bot");
			String welcomeMessage = (String)request.getParameter("welcome-message");
			String statusMessage = (String)request.getParameter("status-message");
			String emailAddress = (String)request.getParameter("emailAddress");
			String emailUserName = (String)request.getParameter("emailUserName");
			String emailPassword = (String)request.getParameter("emailPassword");
			String emailProtocol = (String)request.getParameter("emailProtocol");
			String emailSSL = (String)request.getParameter("emailSSL");
			String emailIncomingHost = (String)request.getParameter("emailIncomingHost");
			String emailIncomingPort = (String)request.getParameter("emailIncomingPort");
			String emailOutgoingHost = (String)request.getParameter("emailOutgoingHost");
			String emailOutgoingPort = (String)request.getParameter("emailOutgoingPort");
			String emailTopic = (String)request.getParameter("emailTopic");
			String emailBody = (String)request.getParameter("emailBody");
			if (saveBot != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.saveSettings(welcomeMessage, statusMessage, bot, botMode,
							emailAddress, emailUserName, emailPassword, emailProtocol, "on".equals(emailSSL),
							emailIncomingHost, emailIncomingPort, emailOutgoingHost, emailOutgoingPort,
							emailTopic, emailBody);
					request.getRequestDispatcher("channel-settings.jsp").forward(request, response);
				} catch (Exception failed) {
					bean.error(failed);
				}
				return;
			}
			String testEmail = (String)request.getParameter("test-email");
			String testEmailAddress = (String)request.getParameter("testEmailAddress");
			if (testEmail != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.saveSettings(welcomeMessage, statusMessage, bot, botMode,
							emailAddress, emailUserName, emailPassword, emailProtocol, "on".equals(emailSSL),
							emailIncomingHost, emailIncomingPort, emailOutgoingHost, emailOutgoingPort,
							emailTopic, emailBody);
					bean.testEmail(testEmailAddress);
					request.getRequestDispatcher("channel-settings.jsp").forward(request, response);
				} catch (Exception failed) {
					bean.error(failed);
				}
				return;
			}
			String deleteInstance = (String)request.getParameter("delete-instance");
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					if (loginBean.getPageType() == Page.Search) {
						request.getRequestDispatcher("channel-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-channel.jsp").forward(request, response);
					}
				}
				return;
			}
			String shutdown = (String)request.getParameter("shutdown");
			if (shutdown != null) {
				bean.shutdownRoom();
				response.sendRedirect("livechat?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String embedType = (String)request.getParameter("type");
			String caption = (String)request.getParameter("caption");
			String landing = (String)request.getParameter("landing");
			String userName = (String)request.getParameter("user");
			String token = (String)request.getParameter("token");
			String password = (String)request.getParameter("password");
			String prompt = (String)request.getParameter("prompt");
			String send = (String)request.getParameter("send");
			String css = (String)request.getParameter("css");
			String customCss = (String)request.getParameter("customcss");
			String banner = (String)request.getParameter("banner");
			String footer = (String)request.getParameter("footer");
			String width = (String)request.getParameter("width");
			String height = (String)request.getParameter("height");
			String offset = (String)request.getParameter("offset");
			String boxlocation = (String)request.getParameter("boxlocation");
			String color = (String)request.getParameter("color");
			String background = (String)request.getParameter("background");
			String codeToken = (String)request.getParameter("codeToken");
			String code = (String)request.getParameter("code");
			String embed = (String)request.getParameter("embed");
			String buttonCss = (String)request.getParameter("buttoncss");
			String subdomain = (String)request.getParameter("subdomain");
			String chooseLanguage = (String)request.getParameter("chooselanguage");
			
			boolean loginBanner = "on".equals((String)request.getParameter("loginBanner"));
			boolean chatlog = "on".equals((String)request.getParameter("chatlog"));
			boolean online = "on".equals((String)request.getParameter("online"));
			boolean bubble = "on".equals((String)request.getParameter("bubble"));
			boolean showTitle = "on".equals((String)request.getParameter("showTitle"));
			boolean showLink = "on".equals((String)request.getParameter("showLink"));
			boolean facebookLogin = "on".equals((String)request.getParameter("facebookLogin"));
			boolean promptContactInfo = "on".equals((String)request.getParameter("promptContactInfo"));
			boolean showAds = "on".equals((String)request.getParameter("showAds"));
			boolean showAdvancedInfo = "on".equals((String)request.getParameter("showAdvancedInfo"));
			boolean showMenubar = "on".equals((String)request.getParameter("showMenubar"));
			boolean showBoxmax = "on".equals((String)request.getParameter("showBoxmax"));
			boolean showSendImage = "on".equals((String)request.getParameter("showSendImage"));
			boolean showEmailChatLog = "on".equals((String)request.getParameter("showEmailChatLog"));
			boolean avatar = "on".equals((String)request.getParameter("avatar"));
			
			if (embed != null) {
				loginBean.verifyPostToken(postToken);
				ChannelEmbedTabBean embedBean = loginBean.getBean(ChannelEmbedTabBean.class);
				embedBean.generateCode(subdomain, embedType, caption, landing, userName, password, token, css, customCss, buttonCss, banner, footer, color,
						background, width, height, offset, boxlocation, chooseLanguage, chatlog, online, bubble, showAds, loginBanner, prompt, send, facebookLogin, showTitle,
						showLink, promptContactInfo, showAdvancedInfo, showMenubar, showBoxmax, showSendImage, showEmailChatLog, avatar);
				request.getRequestDispatcher("channel-embedtab.jsp").forward(request, response);
				return;
			}
			String runCode = (String)request.getParameter("run-code");
			if (runCode != null) {
				ChannelEmbedTabBean embedBean = loginBean.getBean(ChannelEmbedTabBean.class);
				if (!embedBean.getCodeToken().equals(codeToken)) {
					throw new BotException("Invalid execution code");
				}
				embedBean.setCode(code);
				response.setHeader("X-XSS-Protection", "0");
				request.getRequestDispatcher("channel-embedtab.jsp").forward(request, response);
				return;
			}

			if (checkSearchCommon(bean, "channel-search.jsp", request, response)) {
				return;
			}

			setSearchFields(request, bean);
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("channel-search.jsp").forward(request, response);
	}
}
