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
import org.botlibre.web.bean.GraphicBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.rest.GraphicConfig;

@javax.servlet.annotation.WebServlet("/graphic")
@SuppressWarnings("serial")
public class GraphicServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		GraphicBean bean = loginBean.getBean(GraphicBean.class);
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
		String file = (String)request.getParameter("file");
		if (browse != null) {
			if (proxy != null) {
				proxy.setInstanceId(browse);
			}
			boolean valid = bean.validateInstance(browse);
			bean.incrementConnects(ClientType.WEB);
			if ((file != null)) {
				if (!valid || bean.getInstance().getMedia() == null) {
					request.getRequestDispatcher("images/graphic.png").forward(request, response);
					return;
				}
				//response.setContentType("text/plain");
				//response.setHeader("Content-disposition","attachment; filename=" + encodeURI(state.getName()) + ".log");
				//PrintWriter writer = response.getWriter();
				//writer.write(bean.getInstance().getMedia().);
				//writer.flush();
				try {
					request.getRequestDispatcher(bean.getInstance().getMedia().getFileName()).forward(request, response);
					return;
				} catch (Throwable failed) {
					loginBean.error(failed);
				}
				request.getRequestDispatcher("graphic.jsp").forward(request, response);
				return;
			}
			request.getRequestDispatcher("graphic.jsp").forward(request, response);
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
		GraphicBean bean = loginBean.getBean(GraphicBean.class);
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
				response.sendRedirect("browse-graphic.jsp");
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
				response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String export = (String)request.getParameter("export");
			if (export != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.export(response)) {
					response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			String exportAll = (String)request.getParameter("export-all");
			if (exportAll != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.exportAll(request, response)) {
					response.sendRedirect("graphic-search.jsp");
				}
				return;
			}
			String copy = (String)request.getParameter("copy");
			if (copy != null) {
				bean.copyInstance();
				response.sendRedirect("create-graphic.jsp");
				return;
			}
			String createGraphic = (String)request.getParameter("create-graphic");
			String createGraphicLink = (String)request.getParameter("create-graphic-link");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			String editInstance = (String)request.getParameter("edit-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			if (createGraphic != null) {
				response.sendRedirect("create-graphic.jsp");
				return;
			}
			if (createGraphicLink != null) {
				response.sendRedirect("create-graphic-link.jsp");
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-graphic.jsp").forward(request, response);
				return;
			}
			
			if (checkCommon(bean, "graphic?id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "graphic-users.jsp", request, response)) {
				return;
			}
						
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");

			String search = (String)request.getParameter("search-graphic");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("graphic-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-graphic.jsp").forward(request, response);
				return;
			}
			String create = (String)request.getParameter("create-graphic");
			if (create != null) {
				request.getRequestDispatcher("create-graphic.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Graphic");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Graphic");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Graphic");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-graphic.jsp");
				return;
			}
			
			GraphicConfig config = new GraphicConfig();
			updateParameters(config, request);
			String newdomain = (String)request.getParameter("newdomain");
			String isFeatured = (String)request.getParameter("isFeatured");
			String delete = (String)request.getParameter("delete");
			String adVerified = (String)request.getParameter("adVerified");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-graphic.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createLink(config)) {
					response.sendRedirect("create-graphic-link.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateGraphic(config, newdomain, "on".equals(isFeatured), "on".equals(adVerified))) {
						request.getRequestDispatcher("edit-graphic.jsp").forward(request, response);
					} else {
						response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					if (loginBean.getPageType() == Page.Search) {
						request.getRequestDispatcher("graphic-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-graphic.jsp").forward(request, response);
					}
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("graphic?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-graphic.jsp").forward(request, response);
				return;
			}

			if (checkSearchCommon(bean, "graphic-search.jsp", request, response)) {
				return;
			}
			
			setSearchFields(request, bean);
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("graphic-search.jsp").forward(request, response);
	}
}
