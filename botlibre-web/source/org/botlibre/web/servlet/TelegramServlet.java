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

import org.botlibre.web.bean.TelegramBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/telegram")
@SuppressWarnings("serial")
public class TelegramServlet extends BeanServlet {
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
		TelegramBean bean = loginBean.getBean(TelegramBean.class);

		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				response.sendRedirect("facebook.jsp");
				return;
			}
			botBean.checkAdmin();
			String userName = (String)request.getParameter("user");
			String token = (String)request.getParameter("token");
			String submit = (String)request.getParameter("connect");
			if (submit != null) {
				bean.connect(userName, token);
			}

			String channel = (String)request.getParameter("channel");
			String postRSS = (String)request.getParameter("postRSS");
			String rssKeywords = (String)request.getParameter("rssKeywords");
			boolean autoPost = "on".equals((String)request.getParameter("autoPost"));
			String autoPostHours = (String)request.getParameter("autoPostHours");
			String autoPosts = (String)request.getParameter("autoPosts");
			String groupMode = (String)request.getParameter("groupMode");
			boolean checkMessages = "poll".equals((String)request.getParameter("messages"));
			boolean realtimeMessages = "webhook".equals((String)request.getParameter("messages"));
			boolean stripButtonText = "on".equals((String)request.getParameter("stripButtonText"));
			boolean trackMessageObjects = "on".equals((String)request.getParameter("trackMessageObjects"));
			submit = (String)request.getParameter("save");
			if (submit != null) {
				bean.save(channel, postRSS, rssKeywords,
						autoPost, autoPostHours, autoPosts,
						checkMessages, realtimeMessages, stripButtonText, trackMessageObjects, groupMode);
			}
			submit = (String)request.getParameter("disconnect");
			if (submit != null) {
				bean.disable();
			}
			submit = (String)request.getParameter("check");
			if (submit != null) {
				bean.checkStatus();
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("telegram.jsp");
	}
}
