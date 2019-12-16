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
import org.botlibre.web.bean.AvatarBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.UserBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/user-avatar")
@SuppressWarnings("serial")
public class UserAvatarServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		UserBean userBean = loginBean.getBean(UserBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			if (userBean.getUser() == null) {
				response.sendRedirect("index.jsp");
				return;
			}
			String saveAvatar = request.getParameter("saveAvatar");
			if (saveAvatar != null) {
				userBean.chooseAvatar(request.getParameter("chooseAvatar"));
				response.sendRedirect("user-avatar.jsp");
				return;
			}
			String editAvatar = request.getParameter("editAvatar");
			if (editAvatar != null) {
				try {
					AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
					if (userBean.getUser().getInstanceAvatar() == null) {
						throw new BotException("Missing avatar");
					}
					avatarBean.validateInstance(String.valueOf(userBean.getUser().getInstanceAvatar().getId()));
					if (loginBean.getError() != null) {
						response.sendRedirect("user-avatar.jsp");
						return;
					}
					avatarBean.checkAdmin();
					response.sendRedirect("avatar-editor.jsp");
					return;
				} catch (Exception exception) {
					loginBean.error(exception);
				}
				response.sendRedirect("user-avatar.jsp");
				return;
			}
			String testAvatar = request.getParameter("testAvatar");
			if (testAvatar != null) {
				try {
					AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
					if (userBean.getUser().getInstanceAvatar() == null) {
						throw new BotException("Missing avatar");
					}
					avatarBean.validateInstance(String.valueOf(userBean.getUser().getInstanceAvatar().getId()));
					if (loginBean.getError() != null) {
						response.sendRedirect("user-avatar.jsp");
						return;
					}
					response.sendRedirect("avatar-embed.jsp");
					return;
				} catch (Exception exception) {
					loginBean.error(exception);
				}
				response.sendRedirect("user-avatar.jsp");
			}
			String createAvatar = request.getParameter("createAvatar");
			if (createAvatar != null) {
				try {
					AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
					if (userBean.getUser() != null) {
						avatarBean.createUserAvatarInstance(userBean.getUser());
					}
					if (loginBean.getError() != null) {
						response.sendRedirect("user-avatar.jsp");
						return;
					}
					userBean.chooseAvatar(String.valueOf(avatarBean.getInstanceId()));
					if (loginBean.getError() != null) {
						response.sendRedirect("user-avatar.jsp");
						return;
					}
					response.sendRedirect("avatar-editor.jsp");
					return;
				} catch (Exception exception) {
					loginBean.error(exception);
				}
				response.sendRedirect("user-avatar.jsp");
				return;
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
	}
}
