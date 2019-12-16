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

import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.ChatLogImportBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/chatlog-import")
@SuppressWarnings("serial")
public class ChatLogImportServlet extends BeanServlet {
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
		ChatLogImportBean bean = loginBean.getBean(ChatLogImportBean.class);
		
		try {
			String postToken = (String)request.getParameter("postToken");
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				response.sendRedirect("chatlogs.jsp");
				return;
			}
			botBean.checkAdmin();
			
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				response.sendRedirect("chatlogs.jsp");
				return;
			}
			String importScripts = (String)request.getParameter("import");
			String createStates = (String)request.getParameter("create-states");
			String mergeState = (String)request.getParameter("merge-state");
			String debug = (String)request.getParameter("debug");
			String comprehension = (String)request.getParameter("comprehension");
			String pin = (String)request.getParameter("pin");
			String autoReduce = (String)request.getParameter("autoReduce");
			if (importScripts != null) {
				loginBean.verifyPostToken(postToken);
				bean.importChatLogs(request, "on".equals(createStates), "on".equals(mergeState), "on".equals(debug), "on".equals(comprehension), "on".equals(pin), "on".equals(autoReduce));
				response.sendRedirect("chatlogs.jsp");
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");	
			String languageFilter = (String)request.getParameter("language-filter");
			
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("chatlog-import.jsp").forward(request, response);
				return;
			}
			if (userFilter != null) {
				bean.resetSearch();
				bean.setUserFilter(Utils.sanitize(userFilter));
				bean.setInstanceFilter(InstanceFilter.Personal);
				request.getRequestDispatcher("chatlog-import.jsp").forward(request, response);
				return;
			}

			setSearchFields(request, bean);
			if (languageFilter != null) {
				bean.setLanguageFiler(Utils.sanitize(languageFilter));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("chatlog-import.jsp").forward(request, response);
	}
}
