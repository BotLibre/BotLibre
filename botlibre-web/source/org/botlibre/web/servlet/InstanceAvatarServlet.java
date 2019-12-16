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
import org.botlibre.web.bean.InstanceAvatarBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/instance-avatar")
@SuppressWarnings("serial")
public class InstanceAvatarServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		BotBean botBean = loginBean.getBotBean();
		InstanceAvatarBean bean = loginBean.getBean(InstanceAvatarBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected() && (botBean.getInstance() == null || !botBean.getInstance().isExternal())) {
				response.sendRedirect("instance-avatar.jsp");
				return;
			}
			botBean.checkAdmin();
			
			String saveAvatar = request.getParameter("saveAvatar");
			if (saveAvatar != null) {
				loginBean.verifyPostToken(postToken);
				bean.chooseAvatar(request.getParameter("chooseAvatar"));
				response.sendRedirect("instance-avatar.jsp");
				return;
			}
			String editAvatar = request.getParameter("editAvatar");
			if (editAvatar != null) {
				try {
					AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
					if (botBean.getInstance().getInstanceAvatar() == null) {
						throw new BotException("Missing avatar");
					}
					avatarBean.validateInstance(String.valueOf(botBean.getInstance().getInstanceAvatar().getId()));
					if (loginBean.getError() != null) {
						response.sendRedirect("instance-avatar.jsp");
						return;
					}
					avatarBean.checkAdmin();
					response.sendRedirect("avatar-editor.jsp");
					return;
				} catch (Exception exception) {
					loginBean.error(exception);
				}
				response.sendRedirect("instance-avatar.jsp");
				return;
			}
			String testAvatar = request.getParameter("testAvatar");
			if (testAvatar != null) {
				try {
					AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
					if (botBean.getInstance().getInstanceAvatar() == null) {
						throw new BotException("Missing avatar");
					}
					avatarBean.validateInstance(String.valueOf(botBean.getInstance().getInstanceAvatar().getId()));
					if (loginBean.getError() != null) {
						response.sendRedirect("instance-avatar.jsp");
						return;
					}
					response.sendRedirect("avatar-embed.jsp");
					return;
				} catch (Exception exception) {
					loginBean.error(exception);
				}
				response.sendRedirect("instance-avatar.jsp");
			}
			String createAvatar = request.getParameter("createAvatar");
			if (createAvatar != null) {
				try {
					loginBean.verifyPostToken(postToken);
					AvatarBean avatarBean = loginBean.getBean(AvatarBean.class);
					avatarBean.createInstance(botBean.getInstance());
					if (loginBean.getError() != null) {
						response.sendRedirect("instance-avatar.jsp");
						return;
					}
					bean.chooseAvatar(String.valueOf(avatarBean.getInstanceId()));
					if (loginBean.getError() != null) {
						response.sendRedirect("instance-avatar.jsp");
						return;
					}
					response.sendRedirect("avatar-editor.jsp");
					return;
				} catch (Exception exception) {
					loginBean.error(exception);
				}
				response.sendRedirect("instance-avatar.jsp");
				return;
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("instance-avatar.jsp");
	}
}
