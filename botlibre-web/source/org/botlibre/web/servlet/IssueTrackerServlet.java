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

import org.botlibre.util.Utils;

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.IssueBean;
import org.botlibre.web.bean.IssueTrackerBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.IssueTrackerConfig;

@javax.servlet.annotation.WebServlet("/issuetracker")
@SuppressWarnings("serial")
public class IssueTrackerServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class);
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
		String issues = (String)request.getParameter("issues");
		if (browse != null) {
			if (proxy != null) {
				proxy.setInstanceId(browse);
			}
			if (bean.validateInstance(browse)) {
				if (issues != null && bean.getInstance() != null && !bean.getInstance().isExternal()) {
					bean.incrementConnects(ClientType.WEB);
					IssueBean issueBean = loginBean.getBean(IssueBean.class);
					issueBean.resetSearch();
					request.getRequestDispatcher("browse-issue.jsp").forward(request, response);
					return;
				} else if ((issues != null) && bean.getInstance() != null && bean.getInstance().isExternal()) {
					bean.incrementConnects(ClientType.WEB);
					response.sendRedirect(bean.getInstance().getWebsiteURL());
					return;
				}
			}
			request.getRequestDispatcher("issuetracker.jsp").forward(request, response);
			return;
		}
		if (details != null) {
			request.getRequestDispatcher("issuetracker.jsp").forward(request, response);
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
		IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class);
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
				response.sendRedirect("browse-issuetracker.jsp");
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
				response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String issues = (String)request.getParameter("issues");
			if (issues != null) {
				bean.incrementConnects(ClientType.WEB);
				if (bean.getInstance() != null && bean.getInstance().isExternal()) {
					response.sendRedirect(bean.getInstance().getWebsiteURL());
					return;
				}
				request.getRequestDispatcher("browse-issue.jsp").forward(request, response);
				return;
			}
			String createIssueTracker = (String)request.getParameter("create-issuetracker");
			String createIssueTrackerLink = (String)request.getParameter("create-issuetracker-link");
			String allIssues = (String)request.getParameter("all-issues");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			String editInstance = (String)request.getParameter("edit-instance");
			String embedInstance = (String)request.getParameter("embed-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			String subscribe = (String)request.getParameter("subscribe");
			String unsubscribe = (String)request.getParameter("unsubscribe");
			if (createIssueTracker != null) {
				response.sendRedirect("create-issuetracker.jsp");
				return;
			}
			if (createIssueTrackerLink != null) {
				response.sendRedirect("create-issuetracker-link.jsp");
				return;
			}
			if (allIssues != null) {
				bean.setInstance(null);
				request.getRequestDispatcher("browse-issue.jsp").forward(request, response);
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-issuetracker.jsp").forward(request, response);
				return;
			}
			if (embedInstance != null) {
				request.getRequestDispatcher("issuetracker-embedtab.jsp").forward(request, response);
				return;
			}
			String createBot = (String)request.getParameter("create-bot");
			if (createBot != null) {
				response.sendRedirect("create-instance.jsp");
				return;
			}
			
			if (checkCommon(bean, "issuetracker?details=true&id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "issuetracker-users.jsp", request, response)) {
				return;
			}
			
			if (subscribe != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.subscribe();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("issuetracker?details=details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (unsubscribe != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.unsubscribe();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("issuetracker?details=details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");	

			String search = (String)request.getParameter("search-issuetracker");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("issuetracker-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-issuetracker.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("IssueTracker");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("IssueTracker");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("IssueTracker");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-issuetracker.jsp");
				return;
			}
			
			IssueTrackerConfig config = new IssueTrackerConfig();
			updateParameters(config, request);
			String newdomain = (String)request.getParameter("newdomain");
			config.subdomain = (String)request.getParameter("subdomain");
			String isFeatured = (String)request.getParameter("isFeatured");
			String delete = (String)request.getParameter("delete");
			config.createAccessMode = (String)request.getParameter("createAccessMode");
			String adVerified = (String)request.getParameter("adVerified");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-issuetracker.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createLink(config)) {
					response.sendRedirect("create-issuetracker-link.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateIssueTracker(config, newdomain, "on".equals(isFeatured), "on".equals(adVerified))) {
						request.getRequestDispatcher("edit-issuetracker.jsp").forward(request, response);
					} else {
						response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					response.sendRedirect("browse-issuetracker.jsp");
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("issuetracker?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-issuetracker.jsp").forward(request, response);
				return;
			}

			/*String type = (String)request.getParameter("type");
			String caption = (String)request.getParameter("caption");
			String userName = (String)request.getParameter("user");
			String token = (String)request.getParameter("token");
			String password = (String)request.getParameter("password");
			String css = (String)request.getParameter("css");
			String banner = (String)request.getParameter("banner");
			String footer = (String)request.getParameter("footer");
			String color = (String)request.getParameter("color");
			String background = (String)request.getParameter("background");
			String code = (String)request.getParameter("code");
			String embed = (String)request.getParameter("embed");
			String subdomain = (String)request.getParameter("subdomain");
			String showAds = (String)request.getParameter("showAds");
			boolean loginBanner = "on".equals((String)request.getParameter("loginBanner"));
			boolean showLink = "on".equals((String)request.getParameter("showLink"));
			boolean facebookLogin = "on".equals((String)request.getParameter("facebookLogin"));
			if (embed != null) {
				ForumEmbedTabBean embedBean = loginBean.getBean(ForumEmbedTabBean.class);
				embedBean.generateCode(subdomain, type, caption, userName, password, token, css, banner, footer, color, background,
						"on".equals((String)showAds), facebookLogin, loginBanner, showLink);
				request.getRequestDispatcher("forum-embedtab.jsp").forward(request, response);
				return;
			}
			String runCode = (String)request.getParameter("run-code");
			if (runCode != null) {
				ForumEmbedTabBean embedBean = loginBean.getBean(ForumEmbedTabBean.class);
				embedBean.setCode(code);
				response.setHeader("X-XSS-Protection", "0");
				request.getRequestDispatcher("issuetracker-embedtab.jsp").forward(request, response);
				return;
			}*/

			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("issuetracker-search.jsp").forward(request, response);
				return;
			}
			if (userFilter != null) {
				bean.resetSearch();
				bean.setUserFilter(Utils.sanitize(userFilter));
				bean.setInstanceFilter(InstanceFilter.Personal);
				request.getRequestDispatcher("issuetracker-search.jsp").forward(request, response);
				return;
			}

			setSearchFields(request, bean);
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("issuetracker-search.jsp").forward(request, response);
	}
}
