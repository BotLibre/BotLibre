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
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.ForumBean;
import org.botlibre.web.bean.ForumEmbedTabBean;
import org.botlibre.web.bean.ForumPostBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.ForumConfig;

@javax.servlet.annotation.WebServlet("/forum")
@SuppressWarnings("serial")
public class ForumServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		ForumBean bean = loginBean.getBean(ForumBean.class);
		loginBean.setActiveBean(bean);
		if  (!loginBean.checkDomain(request, response)) {
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
		
		String browse = (String)request.getParameter("id");
		String details = (String)request.getParameter("details");
		String posts = (String)request.getParameter("posts");
		if (browse != null) {
			if (proxy != null) {
				proxy.setInstanceId(browse);
			}
			if (bean.validateInstance(browse)) {
				if (details == null && bean.getInstance() != null && !bean.getInstance().isExternal()) {
					bean.incrementConnects(ClientType.WEB);
					ForumPostBean postBean = loginBean.getBean(ForumPostBean.class);
					postBean.resetSearch();
					request.getRequestDispatcher("browse-post.jsp").forward(request, response);
					return;
				} else if ((posts != null) && bean.getInstance() != null && bean.getInstance().isExternal()) {
					bean.incrementConnects(ClientType.WEB);
					response.sendRedirect(bean.getInstance().getWebsiteURL());
					return;
				}
			}
			request.getRequestDispatcher("forum.jsp").forward(request, response);
			return;
		}
		if (details != null) {
			request.getRequestDispatcher("forum.jsp").forward(request, response);
			return;
		}
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		ForumBean bean = loginBean.getBean(ForumBean.class);
		loginBean.setActiveBean(bean);
		
		try {
			String postToken = (String)request.getParameter("postToken");
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
			String domain = (String)request.getParameter("domain");
			if (domain != null) {
				DomainBean domainBean = loginBean.getBean(DomainBean.class);
				if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
					domainBean.validateInstance(domain);
				}
			}
			String cancel = (String)request.getParameter("cancel");
			if (cancel!= null) {
				response.sendRedirect("browse-forum.jsp");
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
				response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String posts = (String)request.getParameter("posts");
			if (posts != null) {
				bean.incrementConnects(ClientType.WEB);
				if (bean.getInstance() != null && bean.getInstance().isExternal()) {
					response.sendRedirect(bean.getInstance().getWebsiteURL());
					return;
				}
				request.getRequestDispatcher("browse-post.jsp").forward(request, response);
				return;
			}
			String createForum = (String)request.getParameter("create-forum");
			String createForumLink = (String)request.getParameter("create-forum-link");
			String allPosts = (String)request.getParameter("all-posts");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			String editInstance = (String)request.getParameter("edit-instance");
			String embedInstance = (String)request.getParameter("embed-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			String subscribe = (String)request.getParameter("subscribe");
			String unsubscribe = (String)request.getParameter("unsubscribe");
			if (createForum != null) {
				response.sendRedirect("create-forum.jsp");
				return;
			}
			if (createForumLink != null) {
				response.sendRedirect("create-forum-link.jsp");
				return;
			}
			if (allPosts != null) {
				bean.setInstance(null);
				request.getRequestDispatcher("browse-post.jsp").forward(request, response);
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-forum.jsp").forward(request, response);
				return;
			}
			if (embedInstance != null) {
				request.getRequestDispatcher("forum-embedtab.jsp").forward(request, response);
				return;
			}
			String createBot = (String)request.getParameter("create-bot");
			if (createBot != null) {
				response.sendRedirect("create-instance.jsp");
				return;
			}
			
			if (checkCommon(bean, "forum?details=true&id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "forum-users.jsp", request, response)) {
				return;
			}
			
			if (subscribe != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.subscribe();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("forum?details=details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (unsubscribe != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.unsubscribe();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("forum?details=details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String saveBot = (String)request.getParameter("save-bot");
			String botMode = (String)request.getParameter("bot-mode");
			String bot = (String)request.getParameter("bot");
			if (saveBot != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.saveBot(bot, botMode);
				} catch (Exception failed) {
					bean.error(failed);
				}
				request.getRequestDispatcher("forum-bot.jsp").forward(request, response);
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");	

			String search = (String)request.getParameter("search-forum");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("forum-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-forum.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Forum");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Forum");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Forum");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-forum.jsp");
				return;
			}
			
			ForumConfig config = new ForumConfig();
			updateParameters(config, request);
			String newdomain = (String)request.getParameter("newdomain");
			config.subdomain = (String)request.getParameter("subdomain");
			String isFeatured = (String)request.getParameter("isFeatured");
			String delete = (String)request.getParameter("delete");
			config.postAccessMode = (String)request.getParameter("postAccessMode");
			config.replyAccessMode = (String)request.getParameter("replyAccessMode");
			String adVerified = (String)request.getParameter("adVerified");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-forum.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createLink(config)) {
					response.sendRedirect("create-forum-link.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateForum(config, newdomain, "on".equals(isFeatured), "on".equals(adVerified))) {
						request.getRequestDispatcher("edit-forum.jsp").forward(request, response);
					} else {
						response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
					request.getRequestDispatcher("edit-forum.jsp").forward(request, response);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					if (loginBean.getPageType() == Page.Search) {
						request.getRequestDispatcher("forum-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-forum.jsp").forward(request, response);
					}
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("forum?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-forum.jsp").forward(request, response);
				return;
			}

			String type = (String)request.getParameter("type");
			String caption = (String)request.getParameter("caption");
			String userName = (String)request.getParameter("user");
			String token = (String)request.getParameter("token");
			String password = (String)request.getParameter("password");
			String css = (String)request.getParameter("css");
			String banner = (String)request.getParameter("banner");
			String footer = (String)request.getParameter("footer");
			String color = (String)request.getParameter("color");
			String background = (String)request.getParameter("background");
			String codeToken = (String)request.getParameter("codeToken");
			String code = (String)request.getParameter("code");
			String embed = (String)request.getParameter("embed");
			String subdomain = (String)request.getParameter("subdomain");
			String showAds = (String)request.getParameter("showAds");
			boolean loginBanner = "on".equals((String)request.getParameter("loginBanner"));
			boolean showLink = "on".equals((String)request.getParameter("showLink"));
			boolean facebookLogin = "on".equals((String)request.getParameter("facebookLogin"));
			if (embed != null) {
				loginBean.verifyPostToken(postToken);
				ForumEmbedTabBean embedBean = loginBean.getBean(ForumEmbedTabBean.class);
				embedBean.generateCode(subdomain, type, caption, userName, password, token, css, banner, footer, color, background,
						"on".equals((String)showAds), facebookLogin, loginBanner, showLink);
				request.getRequestDispatcher("forum-embedtab.jsp").forward(request, response);
				return;
			}
			String runCode = (String)request.getParameter("run-code");
			if (runCode != null) {
				ForumEmbedTabBean embedBean = loginBean.getBean(ForumEmbedTabBean.class);
				if (!embedBean.getCodeToken().equals(codeToken)) {
					throw new BotException("Invalid execution code");
				}
				embedBean.setCode(code);
				response.setHeader("X-XSS-Protection", "0");
				request.getRequestDispatcher("forum-embedtab.jsp").forward(request, response);
				return;
			}

			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("forum-search.jsp").forward(request, response);
				return;
			}
			if (userFilter != null) {
				bean.resetSearch();
				bean.setUserFilter(Utils.sanitize(userFilter));
				bean.setInstanceFilter(InstanceFilter.Personal);
				request.getRequestDispatcher("forum-search.jsp").forward(request, response);
				return;
			}

			setSearchFields(request, bean);
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("forum-search.jsp").forward(request, response);
	}
}
