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
import org.botlibre.web.bean.UserToUserMessageBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/browse-user-to-user-messages")
@SuppressWarnings("serial")
public class UserToUserMessageServlet extends BeanServlet {
	
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
		UserToUserMessageBean bean = loginBean.getBean(UserToUserMessageBean.class);
		try {
			String filter = (String)request.getParameter("filter");
			String page = (String) request.getParameter("page");
			String targetUserId = (String)request.getParameter("targetUserId");
			String viewMessage = (String)request.getParameter("view-message");
			String delete = (String)request.getParameter("delete-user-to-user-messages-confirm");
			
			if (viewMessage != null) {
				bean.setViewMessage(viewMessage);
				if (!loginBean.viewMessage(viewMessage)) {
					response.sendRedirect("browse-user-message.jsp");
					return;
				}
			}
			if (page != null) {
				bean.setMessagePage(Integer.valueOf(page));
				request.getRequestDispatcher("browse-user-to-user-messages.jsp").forward(request, response);
				return;
			}
			if (filter != null) {
				bean.setFilter(Utils.sanitize(filter));
				bean.setMessagePage(0);
			}
			if (targetUserId != null) {
				bean.deleteUserToUserMessages("on".equals(delete), targetUserId);
				response.sendRedirect("browse-user-message.jsp");
				return;
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("browse-user-to-user-messages.jsp").forward(request, response);
	}
}
