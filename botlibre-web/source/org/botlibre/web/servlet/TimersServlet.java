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

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.TimersBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/timers")
@SuppressWarnings("serial")
public class TimersServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		BotBean botBean = loginBean.getBotBean();
		TimersBean bean = loginBean.getBean(TimersBean.class);

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
				botBean.connect(ClientType.WEB);
				if (!botBean.isConnected()) {
					response.sendRedirect("timers.jsp");
					return;
				}
			}
			botBean.checkAdmin();
			boolean enableTimers = "on".equals((String)request.getParameter("enableTimers"));
			String timerHours = (String)request.getParameter("timerHours");
			String timers = (String)request.getParameter("timers");
			String submit = (String)request.getParameter("save");
			if (submit != null) {
				bean.save(enableTimers, timerHours, timers);
			}
			submit = (String)request.getParameter("check");
			if (submit != null) {
				bean.runsTimers();
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("timers.jsp");
	}
}
