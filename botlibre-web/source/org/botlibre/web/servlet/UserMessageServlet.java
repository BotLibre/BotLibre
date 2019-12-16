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
import org.botlibre.web.bean.UserMessageBean.DisplayOption;
import org.botlibre.web.bean.UserMessageBean.UserMessageFolder;
import org.botlibre.web.bean.UserMessageBean.UserMessageSort;
import org.botlibre.web.bean.UserMessageBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/user-message")
@SuppressWarnings("serial")
public class UserMessageServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			// Require HTTP session.
			response.sendRedirect("index.jsp");
			return;
		}
		UserMessageBean bean = loginBean.getBean(UserMessageBean.class);
		try {
			String filter = (String)request.getParameter("filter");
			String userFilter = (String)request.getParameter("user-filter");
			String sort = (String)request.getParameter("sort");
			String displayOption = (String)request.getParameter("display");
			String folder = (String)request.getParameter("folder");
			String page = (String) request.getParameter("page");
			String id = (String)request.getParameter("id");
			
			String viewMessage = (String)request.getParameter("view-message");
			if (viewMessage != null) {
				if (loginBean.viewMessage(viewMessage)) {
					request.getRequestDispatcher("browse-user-to-user-messages.jsp").forward(request, response);
					return;
				}
			}
			
			if (id != null) {
				loginBean.viewUser(id);
				request.getRequestDispatcher("user.jsp").forward(request, response);
				return;
			}
			if (filter != null) {
				bean.setFilter(Utils.sanitize(filter));
			}
			if (userFilter != null) {
				bean.setUserFilter(Utils.sanitize(userFilter));
			}
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("browse-user-message.jsp").forward(request, response);
				return;
			}
			bean.setPage(0);
			bean.setResultsSize(0);
			if (sort != null && !sort.isEmpty()) {
				bean.setSort(UserMessageSort.valueOf(Utils.capitalize(sort)));
			}
			if (displayOption != null && !displayOption.isEmpty()) {
				bean.setDisplayOption(DisplayOption.valueOf(Utils.capitalize(displayOption)));
			}
			if (folder != null && !folder.isEmpty()) {
				bean.setFolder(UserMessageFolder.valueOf(Utils.capitalize(folder)));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("browse-user-message.jsp");
	}
}
