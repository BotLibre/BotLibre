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
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.MemoryImportBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/memory-import")
@SuppressWarnings("serial")
public class MemoryImportServlet extends BeanServlet {
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
		MemoryImportBean bean = loginBean.getBean(MemoryImportBean.class);
		
		try {
			String postToken = (String)request.getParameter("postToken");
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				response.sendRedirect("memory.jsp");
				return;
			}
			botBean.checkAdmin();
			
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				response.sendRedirect("memory.jsp");
				return;
			}
			String importScripts = (String)request.getParameter("import");
			String pin = (String)request.getParameter("pin");
			String languageFilter = Utils.sanitize((String)request.getParameter("language-filter"));
			if (importScripts != null) {
				loginBean.verifyPostToken(postToken);
				setSearchFields(request, bean);
				if (languageFilter != null) {
					bean.setLanguageFiler(languageFilter);
				}
				bean.importData(request, "on".equals(pin));
				response.sendRedirect("memory.jsp");
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");	
			//String languageFilter = (String)request.getParameter("language-filter");
			
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("memory-import.jsp").forward(request, response);
				return;
			}
			if (userFilter != null) {
				bean.resetSearch();
				bean.setUserFilter(Utils.sanitize(userFilter));
				bean.setInstanceFilter(InstanceFilter.Personal);
				request.getRequestDispatcher("memory-import.jsp").forward(request, response);
				return;
			}

			setSearchFields(request, bean);
			if (languageFilter != null) {
				bean.setLanguageFiler(Utils.sanitize(languageFilter));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("memory-import.jsp").forward(request, response);
	}
}
