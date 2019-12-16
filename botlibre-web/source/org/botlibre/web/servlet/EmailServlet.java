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

import org.botlibre.web.bean.EmailBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/email")
@SuppressWarnings("serial")
public class EmailServlet extends BeanServlet {
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
		EmailBean bean = loginBean.getBean(EmailBean.class);

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
				response.sendRedirect("email.jsp");
				return;
			}
			botBean.checkAdmin();	
			
			String userName = (String)request.getParameter("userName");
			String password = (String)request.getParameter("password");
			String emailAddress = (String)request.getParameter("emailAddress");
			String incomingHost = (String)request.getParameter("incomingHost");
			String incomingPort = (String)request.getParameter("incomingPort");
			String outgoingHost = (String)request.getParameter("outgoingHost");
			String outgoingPort = (String)request.getParameter("outgoingPort");
			boolean ssl = "on".equals((String)request.getParameter("ssl"));
			boolean reply = "on".equals((String)request.getParameter("replyEmail"));
			String testEmailAddress = (String)request.getParameter("testEmailAddress");
			String protocol = (String)request.getParameter("protocol");			
			String testEmail = (String)request.getParameter("testEmail");
			String signature = (String)request.getParameter("signature");
			String submit = (String)request.getParameter("save");
			if (submit != null) {
				bean.save(userName, password, emailAddress, incomingHost, incomingPort, outgoingHost, outgoingPort, protocol, ssl, signature, reply);
			}
			submit = (String)request.getParameter("disconnect");
			if (submit != null) {
				bean.disable();
			}
			submit = (String)request.getParameter("check");
			if (submit != null) {
				bean.checkEmail();
			}
			if (testEmail != null) {
				bean.testEmail("test", "test", testEmailAddress);
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("email.jsp");
	}
}
