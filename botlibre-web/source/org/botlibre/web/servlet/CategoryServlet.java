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
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;

@javax.servlet.annotation.WebServlet("/category")
@MultipartConfig
@SuppressWarnings("serial")
public class CategoryServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean bean = getLoginBean(request, response);
		if (bean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		checkProxy(request, response);

		try {
			String postToken = (String)request.getParameter("postToken");
			if  (!bean.checkDomain(request, response)) {
				return;
			}
			String cancelCategory = (String)request.getParameter("cancel-category");
			if (cancelCategory != null) {
				if (bean.getCategoryType().equals("Bot")) {
					response.sendRedirect("browse.jsp");
				} else if (bean.getCategoryType().equals("Analytic")) {
					response.sendRedirect("browse-analytic.jsp");
				}else if (bean.getCategoryType().equals("Forum")) {
					response.sendRedirect("browse-forum.jsp");
				} else if (bean.getCategoryType().equals("IssueTracker")) {
					response.sendRedirect("browse-issuetracker.jsp");
				} else if (bean.getCategoryType().equals("Script")) {
					response.sendRedirect("browse-script.jsp");
				} else if (bean.getCategoryType().equals("Avatar")) {
					response.sendRedirect("browse-avatar.jsp");
				} else if (bean.getCategoryType().equals("Graphic")) {
					response.sendRedirect("browse-graphic.jsp");
				} else if (bean.getCategoryType().equals("Domain")) {
					response.sendRedirect("domains.jsp");
				} else {
					response.sendRedirect("browse-channel.jsp");
				}
				return;
			}
			String name = (String)request.getParameter("name");
			String description = (String)request.getParameter("description");
			String parents = (String)request.getParameter("parents");
			String secured = (String)request.getParameter("secured");
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				bean.verifyPostToken(postToken);
				if (!bean.createCategory(name, description, parents, "on".equals(secured))) {
					request.getRequestDispatcher("create-category.jsp").forward(request, response);
				} else {
					Part filePart = request.getPart("file");
					if ((filePart != null) && (filePart.getSize() > 0)) {
						InputStream stream = filePart.getInputStream();
						byte[] image = BotBean.loadImageFile(stream);
						if ((image != null) && (image.length > 0)) {
							bean.updateCategory(image);
						}
					}
					if (bean.getCategoryType().equals("Bot")) {
						response.sendRedirect("browse.jsp");
					} else if (bean.getCategoryType().equals("Analytic")) {
						response.sendRedirect("browse-analytic.jsp");
					}else if (bean.getCategoryType().equals("Forum")) {
						response.sendRedirect("browse-forum.jsp");
					} else if (bean.getCategoryType().equals("IssueTracker")) {
						response.sendRedirect("browse-issuetracker.jsp");
					} else if (bean.getCategoryType().equals("Script")) {
						response.sendRedirect("browse-script.jsp");
					} else if (bean.getCategoryType().equals("Avatar")) {
						response.sendRedirect("browse-avatar.jsp");
					} else if (bean.getCategoryType().equals("Graphic")) {
						response.sendRedirect("browse-graphic.jsp");
					} else if (bean.getCategoryType().equals("Domain")) {
						response.sendRedirect("domains.jsp");
					} else {
						response.sendRedirect("browse-channel.jsp");
					}
				}
				return;
			}
			String saveCategory = (String)request.getParameter("save-category");
			if (saveCategory != null) {
				bean.verifyPostToken(postToken);
				if (!bean.updateCategory(name, description, parents, "on".equals(secured))) {
					request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				} else {
					Part filePart = request.getPart("file");
					if ((filePart != null) && (filePart.getSize() > 0)) {
						InputStream stream = filePart.getInputStream();
						byte[] image = BotBean.loadImageFile(stream);
						if ((image != null) && (image.length > 0)) {
							bean.updateCategory(image);
						}
					}
					if (bean.getCategoryType().equals("Bot")) {
						response.sendRedirect("browse.jsp");
					}else if (bean.getCategoryType().equals("Analytic")) {
						response.sendRedirect("browse-analytic.jsp");
					} else if (bean.getCategoryType().equals("Forum")) {
						response.sendRedirect("browse-forum.jsp");
					} else if (bean.getCategoryType().equals("IssueTracker")) {
						response.sendRedirect("browse-issuetracker.jsp");
					} else if (bean.getCategoryType().equals("Script")) {
						response.sendRedirect("browse-script.jsp");
					} else if (bean.getCategoryType().equals("Avatar")) {
						response.sendRedirect("browse-avatar.jsp");
					} else if (bean.getCategoryType().equals("Graphic")) {
						response.sendRedirect("browse-graphic.jsp");
					} else if (bean.getCategoryType().equals("Domain")) {
						response.sendRedirect("domains.jsp");
					} else {
						response.sendRedirect("browse-channel.jsp");
					}
				}
				return;
			}
		} catch (Exception failed) {
			AdminDatabase.instance().log(failed);
			bean.setError(failed);
		}
		request.getRequestDispatcher("browse.jsp").forward(request, response);
	}
}
