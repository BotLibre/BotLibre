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

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.UserBean;
import org.botlibre.web.bean.UserBean.DisplayOption;
import org.botlibre.web.bean.UserBean.UserFilter;
import org.botlibre.web.bean.UserBean.UserSort;
import org.botlibre.web.bean.UserBean.UserRestrict;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/user")
@SuppressWarnings("serial")
public class UserServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			// Do not allow web crawlers into the user directory.
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (!loginBean.checkDomain(request, response)) {
			return;
		}
		UserBean bean = loginBean.getBean(UserBean.class);
		try {
			String restrict = (String)request.getParameter("restrict");
			String filter = (String)request.getParameter("filter");
			String nameFilter = (String)request.getParameter("name-filter");
			String tagFilter = (String)request.getParameter("tag-filter");
			String emailFilter = (String)request.getParameter("email-filter");
			String sort = (String)request.getParameter("sort");
			String displayOption = (String)request.getParameter("display");
			String page = (String) request.getParameter("page");
			String id = (String)request.getParameter("id");
			String postToken = (String)request.getParameter("postToken");
			
			if (id != null) {
				loginBean.viewUser(id);
				request.getRequestDispatcher("user.jsp").forward(request, response);
				return;
			}
			String exportAll = (String)request.getParameter("export-all");
			if (exportAll != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.exportAll(request, response)) {
					response.sendRedirect("browse-user.jsp");
				}
				return;
			}
			if (nameFilter != null) {
				bean.setNameFilter(Utils.sanitize(nameFilter));
			}
			if (emailFilter != null) {
				bean.setEmailFilter(Utils.sanitize(emailFilter));
			}
			if (tagFilter != null) {
				bean.setTagFilter(Utils.sanitize(tagFilter));
			}
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("browse-user.jsp").forward(request, response);
				return;
			}
			
			String myAvatars = (String)request.getParameter("my-friends");
			if (myAvatars != null) {
				filter = "friends";
			}
			bean.setPage(0);
			bean.setResultsSize(0);
			bean.setUserFilter(null);
			if ("private".equals(filter) && loginBean.isSuperUser()) {
				bean.setUserFilter(UserFilter.Private);
			} else if ("public".equals(filter)) {
				bean.setUserFilter(UserFilter.Public);
			} else if ("friends".equals(filter)) {
				bean.setUserFilter(UserFilter.Friends);
			}
			if (sort != null && !sort.isEmpty()) {
				bean.setUserSort(UserSort.valueOf(Utils.capitalize(sort)));
			}
			
			if ("grid".equals(displayOption)) {
				bean.setDisplayOption(DisplayOption.Grid);
			} else if ("details".equals(displayOption)) {
				bean.setDisplayOption(DisplayOption.Details);
			} else if ("header".equals(displayOption)) {
				bean.setDisplayOption(DisplayOption.Header);
			}
			if (restrict != null && !restrict.isEmpty()) {
				bean.setUserRestrict(UserRestrict.valueOf(Utils.capitalize(restrict)));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("browse-user.jsp");
	}
}
