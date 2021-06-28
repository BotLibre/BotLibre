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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.BotException;
import org.botlibre.util.Utils;
import org.botlibre.web.Site;
import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.ScriptBean;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.ScriptConfig;

@javax.servlet.annotation.WebServlet("/script")
@SuppressWarnings("serial")
public class ScriptServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		ScriptBean bean = loginBean.getBean(ScriptBean.class);
		loginBean.setActiveBean(bean);
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
		
		String browse = (String)request.getParameter("id");
		String source = (String)request.getParameter("source");
		String versions = (String)request.getParameter("versions");
		String file = (String)request.getParameter("file");
		if (browse != null) {
			if (proxy != null) {
				proxy.setInstanceId(browse);
			}
			if (bean.validateInstance(browse)) {
				if ((versions != null)) {
					request.getRequestDispatcher("script-versions.jsp").forward(request, response);
					return;
				}
				if (file != null) {
					bean.incrementConnects(ClientType.WEB);
					if (Site.LOCK && !loginBean.isSandbox()) {
						throw new BotException("Must use sandbox domain");
					}
					if (bean.getInstance().getLanguage().equals("HTML")) {
						response.setContentType("text/html; charset=utf-8");
					}
					//response.setHeader("Content-disposition","attachment; filename=" + encodeURI(state.getName()) + ".log");
					PrintWriter writer = response.getWriter();
					writer.write(bean.getInstance().getSourceCode());
					writer.flush();
					return;
				}
				if (source != null) {
					bean.incrementConnects(ClientType.WEB);
					if (bean.getInstance().isExternal()) {
						response.sendRedirect(bean.getInstance().getWebsiteURL());
					} else {
						request.getRequestDispatcher("script-source.jsp").forward(request, response);
					}
					return;
				}
			} else if (file != null) {
				request.getRequestDispatcher("error.jsp").forward(request, response);
				return;
			}
			request.getRequestDispatcher("script.jsp").forward(request, response);
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
		ScriptBean bean = loginBean.getBean(ScriptBean.class);
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
			if (cancel != null) {
				response.sendRedirect("browse-script.jsp");
				return;
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String showDetails = (String)request.getParameter("show-details");
			if (showDetails != null) {
				request.getRequestDispatcher("script.jsp").forward(request, response);
				return;
			}
			String viewVersion = (String)request.getParameter("view-version");
			if (viewVersion != null) {
				bean.viewVersion(request);
				request.getRequestDispatcher("script-versions.jsp").forward(request, response);
				return;
			}
			bean.setViewSource(null);
			String deleteVersion = (String)request.getParameter("delete-version");
			if (deleteVersion != null) {
				loginBean.verifyPostToken(postToken);
				String confirm = (String)request.getParameter("confirm");
				bean.deleteVersions(request, "on".equals(confirm));
				request.getRequestDispatcher("script-versions.jsp").forward(request, response);
				return;
			}
			String showSource = (String)request.getParameter("show-source");
			if (showSource != null) {
				if (bean.getInstance() != null && bean.getInstance().isExternal()) {
					response.sendRedirect(bean.getInstance().getWebsiteURL());
					return;
				}
				request.getRequestDispatcher("script-source.jsp").forward(request, response);
				return;
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("script?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String export = (String)request.getParameter("export");
			if (export != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.export(response)) {
					response.sendRedirect("script?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String copy = (String)request.getParameter("copy");
			if (copy != null) {
				bean.copyInstance();
				response.sendRedirect("create-script.jsp");
				return;
			}
			String download = (String)request.getParameter("download-script-source");
			if (download != null) {
				if (!bean.downloadScriptSource(response)) {
					response.sendRedirect("script?source=true&id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String save = (String)request.getParameter("save");
			if (save != null) {
				loginBean.verifyPostToken(postToken);
				String source = (String)request.getParameter("source");
				String version = (String)request.getParameter("version");
				String versionName = (String)request.getParameter("versionName");
				bean.updateScriptSource(source, "on".equals(version), versionName);
				response.sendRedirect("script?source=true&id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String createScript = (String)request.getParameter("create-script");
			String createScriptLink = (String)request.getParameter("create-script-link");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			String editInstance = (String)request.getParameter("edit-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			if (createScript != null) {
				response.sendRedirect("create-script.jsp");
				return;
			}
			if (createScriptLink != null) {
				response.sendRedirect("create-script-link.jsp");
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("script?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-script.jsp").forward(request, response);
				return;
			}
			
			if (checkCommon(bean, "script?details=true&id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "script-users.jsp", request, response)) {
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");	

			String search = (String)request.getParameter("search-script");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("script-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-script.jsp").forward(request, response);
				return;
			}
			String create = (String)request.getParameter("create-script");
			if (create != null) {
				request.getRequestDispatcher("create-script.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Script");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Script");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Script");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-script.jsp");
				return;
			}
			
			ScriptConfig config = new ScriptConfig();
			updateParameters(config, request);
			String newdomain = (String)request.getParameter("newdomain");
			config.subdomain = (String)request.getParameter("subdomain");
			config.language = (String)request.getParameter("language");
			boolean isFeatured = "on".equals((String)request.getParameter("isFeatured"));
			String delete = (String)request.getParameter("delete");
			String adVerified = (String)request.getParameter("adVerified");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-script.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					if (bean.getInstance().isExternal()) {
						response.sendRedirect("script?id=" + bean.getInstanceId() + proxy.proxyString());
					} else {
						response.sendRedirect("script?source=true&id=" + bean.getInstanceId() + proxy.proxyString());						
					}
				}
				return;
			}
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createLink(config)) {
					response.sendRedirect("create-script-link.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					if (bean.getInstance().isExternal()) {
						response.sendRedirect("script?id=" + bean.getInstanceId() + proxy.proxyString());
					} else {
						response.sendRedirect("script?source=true&id=" + bean.getInstanceId() + proxy.proxyString());						
					}
				}
				return;
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateScript(config, newdomain, isFeatured, "on".equals(adVerified))) {
						request.getRequestDispatcher("edit-script.jsp").forward(request, response);
					} else {
						response.sendRedirect("script?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("script?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					if (loginBean.getPageType() == Page.Search) {
						request.getRequestDispatcher("script-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-script.jsp").forward(request, response);
					}
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("script?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-script.jsp").forward(request, response);
				return;
			}

			if (checkSearchCommon(bean, "script-search.jsp", request, response)) {
				return;
			}

			setSearchFields(request, bean);
			String languageFilter = (String)request.getParameter("language-filter");
			if (languageFilter != null) {
				bean.setLanguageFiler(Utils.sanitize(languageFilter));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("script-search.jsp").forward(request, response);
	}
}
