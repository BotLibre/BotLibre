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

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.TrainingBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/training")
@SuppressWarnings("serial")
public class TrainingServlet extends BeanServlet {
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
		TrainingBean trainingBean = loginBean.getBean(TrainingBean.class);

		String newResponse = (String)request.getParameter("newResponse");
		String addDefaultResponse = (String)request.getParameter("addDefaultResponse");
		String removeDefaultResponse = (String)request.getParameter("removeDefaultResponse");
		String selectedResponse = (String)request.getParameter("defaultResponses");
		String newGreeting = (String)request.getParameter("newGreeting");
		String addGreeting = (String)request.getParameter("addGreeting");
		String removeGreeting = (String)request.getParameter("removeGreeting");
		String selectedGreeting = (String)request.getParameter("greetings");
		String question = (String)request.getParameter("question");
		String questionResponse = (String)request.getParameter("response");
		String addResponse = (String)request.getParameter("addResponse");
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
				response.sendRedirect("training.jsp");
				return;
			}
			botBean.checkAdmin();
			if (addDefaultResponse != null) {
				trainingBean.addDefaultResponses(newResponse);
			} else if (removeDefaultResponse != null) {
				trainingBean.removeDefaultResponses(selectedResponse);
			} else if (addResponse != null) {
				trainingBean.addQuestionResponses(question, questionResponse);
			} else if (addGreeting != null) {
				trainingBean.addGreeting(newGreeting);
			} else if (removeGreeting != null) {
				trainingBean.removeGreeting(selectedGreeting);
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("training.jsp");
	}
}
