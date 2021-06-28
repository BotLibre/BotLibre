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
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.util.Utils;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.bean.ChatWarBean;
import org.botlibre.web.bean.LoginBean;

@javax.servlet.annotation.WebServlet({"/chatbotwar"})
@SuppressWarnings("serial")
public class ChatWarServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getLoginBean(request, response);
		try {
			if (loginBean == null) {
				loginBean = new LoginBean();
				request.getSession().setAttribute("loginBean", loginBean);
			}
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
			String postToken = (String)request.getParameter("postToken");
			ChatWarBean bean = loginBean.getBean(ChatWarBean.class);
			String vote = request.getParameter("vote");
			String winner = Utils.sanitize(request.getParameter("winner"));
			String token = (String)request.getParameter("token");
			if (vote != null) {
				loginBean.verifyPostToken(postToken);
				if (token == null || !token.equals(String.valueOf(bean.hashCode()))) {
					AdminDatabase.instance().log(Level.WARNING, "hack", BeanServlet.extractIP(request));
					request.getRequestDispatcher("chatwar-start.jsp").forward(request, response);
					return;
				}
				bean.endWar(winner);
				request.getRequestDispatcher("chatwar-start.jsp").forward(request, response);
				return;
			}
			String restart = request.getParameter("restart");
			if (restart != null) {
				request.getRequestDispatcher("chatwar-start.jsp").forward(request, response);
				return;
			}
			String chat1 = Utils.sanitize(request.getParameter("bot1"));
			String chat2 = Utils.sanitize(request.getParameter("bot2"));
			String topic = Utils.sanitize(request.getParameter("topic"));
			if (bean.startWar(chat1, chat2, topic)) {
				request.getRequestDispatcher("chatwar.jsp").forward(request, response);
				return;
			}
		} catch (Exception failed) {
			AdminDatabase.instance().log(failed);
			if (loginBean != null) {
				loginBean.setError(failed);
			}
		}
		request.getRequestDispatcher("chatwar-start.jsp").forward(request, response);
	}
}
