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

import org.botlibre.web.bean.AttachmentsBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/attachments")
@SuppressWarnings("serial")
public class AttachmentServlet extends BeanServlet {
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
		AttachmentsBean bean = loginBean.getBean(AttachmentsBean.class);

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
				response.sendRedirect("instance-attachments.jsp");
				return;
			}
			botBean.checkAdmin();
			
			String duration = (String)request.getParameter("duration");
			String search = (String)request.getParameter("search");
			String selectAllAttachments = (String)request.getParameter("selectAllAttachments");
			String deleteAttachments = (String)request.getParameter("deleteAttachments");
			if (search != null) {
				bean.queryAttachments(duration);
			}
			if (selectAllAttachments != null) {
				bean.setSelectAll(!bean.getSelectAll());
				request.getRequestDispatcher("instance-attachments.jsp").forward(request, response);
				return;
			}
			if (deleteAttachments != null) {
				bean.deleteAttachments(request);
			}
			if (duration != null) {
				bean.queryAttachments(duration);
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("instance-attachments.jsp");
	}
}
