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

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.ChatBean;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LiveChatBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.chat.ChatChannel.ChannelType;
import org.botlibre.web.rest.InstanceConfig;
import org.botlibre.web.service.BeanManager;

@javax.servlet.annotation.WebServlet("/bot")
@SuppressWarnings("serial")
public class BotServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean bean = getLoginBean(request, response);
		String postToken = (String)request.getParameter("postToken");
		String attachment = (String)request.getParameter("attachment");
		String key = (String)request.getParameter("key");
		if (attachment != null) {
			if (bean == null) {
				bean = new LoginBean();
			}
			bean.getBotBean().downloadAttachment(response, attachment, key);
			return;
		}
		if (bean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		SessionProxyBean proxy = checkProxy(request, response);
		BotBean botBean = bean.getBotBean();
		
		try {
			if (!bean.checkDomain(request, response)) {
				return;
			}
			bean.initialize(getServletContext(), request, response);
			proxy.setRedirect(false);
			String domain = (String)request.getParameter("domain");
			if (domain != null) {
				DomainBean domainBean = bean.getBean(DomainBean.class);
				if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
					domainBean.validateInstance(domain);
				}
			}
			
			String cancel = (String)request.getParameter("cancel");
			if (cancel!= null) {
				response.sendRedirect("browse.jsp");
				return;
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("instance.jsp");
				return;
			}
	
			String chat = (String)request.getParameter("chat");
			if (chat != null) {
				if (bean.getBotBean().getInstance() != null && bean.getBotBean().getInstance().isExternal()) {
					bean.getBotBean().incrementConnects(ClientType.WEB);
					response.sendRedirect(bean.getBotBean().getInstance().getWebsiteURL());
					return;
				}
				proxy.setBeanId(null);
				bean = proxy.cloneLoginBean(bean);
				Long proxyId = proxy.getBeanId();
				bean.getBotBean().connect(ClientType.WEB, request);
				bean.getBean(ChatBean.class).greet();
				bean.getBotBean().outputAvatar();
				proxy.setBeanId(proxyId);
				request.getRequestDispatcher("chat.jsp").forward(request, response);
				return;
			}
			String dynamicChat = (String)request.getParameter("dynamicChat");
			if (dynamicChat != null) {
				if (bean.getBotBean().getInstance() != null
							&& bean.getBotBean().getInstance().isExternal()
							&& !bean.getBotBean().getInstance().hasAPI()) {
					bean.getBotBean().incrementConnects(ClientType.WEB);
					response.sendRedirect(bean.getBotBean().getInstance().getWebsiteURL());
					return;
				}
				request.getRequestDispatcher("dchat.jsp").forward(request, response);
				return;
			}
			String livechat = (String)request.getParameter("livechat");
			if (livechat != null) {
				if (botBean.getInstance() == null) {
					request.getRequestDispatcher("instance.jsp").forward(request, response);
					return;
				}
				LiveChatBean livechatBean = bean.getBean(LiveChatBean.class);
				if (!livechatBean.checkBotChannel(ChannelType.OneOnOne, bean.getBotBean())) {
					request.getRequestDispatcher("instance.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("livechat.jsp").forward(request, response);
				}
				return;
			}
			String chatroom = (String)request.getParameter("chatroom");
			if (chatroom != null) {
				if (botBean.getInstance() == null) {
					request.getRequestDispatcher("instance.jsp").forward(request, response);
					return;
				}
				LiveChatBean livechatBean = bean.getBean(LiveChatBean.class);
				if (!livechatBean.checkBotChannel(ChannelType.ChatRoom, bean.getBotBean())) {
					request.getRequestDispatcher("instance.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("livechat.jsp").forward(request, response);
				}
				return;
			}
			String admin = (String)request.getParameter("admin");
			if (admin != null) {
				if (botBean.getInstance() == null || !botBean.getInstance().isExternal()) {
					bean.disconnectInstance();
					botBean.connect(ClientType.WEB, request);
				}
				response.sendRedirect("admin.jsp");
				return;
			}
			String log = (String)request.getParameter("log");
			if (log != null) {
				bean.disconnectInstance();
				botBean.connect(ClientType.WEB, request);
				response.sendRedirect("log.jsp");
				return;
			}
			
			String editInstance = (String)request.getParameter("edit-instance");
			String embedInstance = (String)request.getParameter("embed-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			if (editInstance != null) {
				if (!botBean.editInstance(botBean.getInstanceId())) {
					response.sendRedirect("browse?id=" + botBean.getInstanceId());
					return;
				}
				response.sendRedirect("edit-instance.jsp");
				return;
			}
			if (embedInstance != null) {
				response.sendRedirect("embedtab.jsp");
				return;
			}
			if (checkCommon(botBean, "browse?id=" + botBean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(botBean, "admin-users.jsp", request, response)) {
				return;
			}
			
			String browse = (String)request.getParameter("browse");
			
			InstanceConfig config = new InstanceConfig();
			updateParameters(config, request);
			String newInstance = (String)request.getParameter("newInstance");
			String newdomain = (String)request.getParameter("newdomain");
			String memoryLimit = (String)request.getParameter("memoryLimit");
			config.subdomain = (String)request.getParameter("subdomain");
			config.template = (String)request.getParameter("template");
			config.allowForking = "on".equals((String)request.getParameter("allowForking"));
			String isTemplate = (String)request.getParameter("isTemplate");
			boolean isFeatured = "on".equals((String)request.getParameter("isFeatured"));
			String adVerified = (String)request.getParameter("adVerified");
			String delete = (String)request.getParameter("delete");
			String isSchema = (String)request.getParameter("isSchema");
			String apiURL = (String)request.getParameter("apiURL");
			String apiPost = (String)request.getParameter("apiPost");
			String apiResponse = (String)request.getParameter("apiResponse");
			boolean apiServerSide = "on".equals((String)request.getParameter("apiServerSide"));
			boolean apiJSON = "on".equals((String)request.getParameter("apiJSON"));

			String createInstance = (String)request.getParameter("create-instance");
			if (createInstance != null) {
				bean.verifyPostToken(postToken);
				config.name = newInstance;
				if (!botBean.createInstance(config, "on".equals(isTemplate), true, BeanServlet.extractIP(request))) {
					response.sendRedirect("create-instance.jsp");
				} else {
					bean.setPageType(Page.Browse);
					response.sendRedirect("browse?id=" + botBean.getInstance().getId());
				}
				return;
			}
			String createLink = (String)request.getParameter("create-link");
			if (createLink != null) {
				bean.verifyPostToken(postToken);
				config.name = newInstance;
				if (!botBean.createLink(config, BeanServlet.extractIP(request), apiURL, apiPost, apiResponse, apiServerSide, apiJSON)) {
					response.sendRedirect("create-instance-link.jsp");
				} else {
					bean.setPageType(Page.Browse);
					response.sendRedirect("browse?id=" + botBean.getInstance().getId());
				}
				return;
			}
			String fork = (String)request.getParameter("fork");
			if (fork != null) {
				botBean.forkInstance();
				response.sendRedirect("create-instance.jsp");
				return;
			}
			if (saveInstance != null) {
				try {
					bean.verifyPostToken(postToken);
					if (!botBean.updateInstance(config, newdomain, memoryLimit,
								"on".equals(isTemplate), isFeatured, "on".equals(adVerified),
								apiURL, apiPost, apiResponse, apiServerSide, apiJSON)) {
						response.sendRedirect("edit-instance.jsp");
					} else {
						response.sendRedirect("browse?id=" + botBean.getInstance().getId());
					}
				} catch (Exception failed) {
					bean.setError(failed);
					response.sendRedirect("edit-instance.jsp");
				}
				return;
			}
			if (deleteInstance != null) {
				bean.verifyPostToken(postToken);
				if (!botBean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("browse?id=" + botBean.getInstanceId());
				} else {
					if (bean.getPageType() == Page.Search) {
						request.getRequestDispatcher("instance-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse.jsp").forward(request, response);
					}
				}
				return;
			}

			if (checkSearchCommon(botBean, "instance-search.jsp", request, response)) {
				return;
			}
			String disconnect = (String)request.getParameter("disconnect");
			if (disconnect != null) {
				if (request.getParameter("proxy") != null) {
					Long proxyId = Long.valueOf((String)request.getParameter("proxy"));
					proxy.setBeanId(proxyId);
					LoginBean proxyBean = proxy.getLoginBean();
					bean.disconnectInstance();
					botBean.setInstance(proxyBean.getBotBean().getInstance());
					proxyBean.disconnect();
					BeanManager.manager().removeInstance(proxyId);
				} else {
					bean.disconnectInstance();
				}
				if (botBean.getInstance() == null) {
					request.getRequestDispatcher("instance.jsp").forward(request, response);
					return;
				}
				response.sendRedirect("browse?id=" + botBean.getInstanceId());
				return;
			} else if (browse != null) {
				botBean.validateInstance(browse);
				request.getRequestDispatcher("instance.jsp").forward(request, response);
				return;
			}
		} catch (Exception failed) {
			AdminDatabase.instance().log(failed);
			bean.setError(failed);
		}
		response.sendRedirect("index.jsp");
	}
}
