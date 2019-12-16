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

import org.botlibre.BotException;

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.SelfBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/self")
@MultipartConfig
@SuppressWarnings("serial")
public class SelfServlet extends BeanServlet {
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
		SelfBean bean = loginBean.getBean(SelfBean.class);

		String input = (String)request.getParameter("input");
		String remove = (String)request.getParameter("remove");
		String compile = (String)request.getParameter("compile");
		String decompile = (String)request.getParameter("decompile");
		String newState = (String)request.getParameter("new");
		String up = (String)request.getParameter("up");
		String down = (String)request.getParameter("down");
		String export = (String)request.getParameter("export");
		String importLib = (String)request.getParameter("import-lib");
		String edit = (String)request.getParameter("edit");
		String cancel = (String)request.getParameter("cancel");
		String state = (String)request.getParameter("state-select");
		String importFormat = (String)request.getParameter("import-format");
		String importEncoding = (String)request.getParameter("import-encoding");
		String createStates = (String)request.getParameter("create-states");
		String mergeState = (String)request.getParameter("merge-state");
		String indexStatic = (String)request.getParameter("index-static");
		String debug = (String)request.getParameter("debug");
		String optimize = (String)request.getParameter("optimize");
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
				response.sendRedirect("self.jsp");
				return;
			}
			botBean.checkAdmin();
			String rebootstrap = (String)request.getParameter("Rebootstrap");
			Part filePart = request.getPart("file");
			if ((filePart != null) && (filePart.getSize() > 0)) {
				if (filePart != null) {
					String fileName = getFileName(filePart);
					if (fileName.indexOf('.') != -1) {
						fileName = fileName.substring(0, fileName.indexOf('.'));
					}
					if (fileName == null) {
						fileName = "new";
					}
					InputStream stream = filePart.getInputStream();
					if ("aiml".equals(importFormat)) {
						bean.loadAIMLFile(stream, fileName, "on".equals(createStates), "on".equals(mergeState), "on".equals(indexStatic), importEncoding);
					} else {
						bean.loadSelfFile(stream, importEncoding, "on".equals(debug), "on".equals(optimize));
					}
				}
			} else if (remove != null) {
				bean.removeSelectedState(state);
			} else if (rebootstrap != null) {
				String confirm = (String)request.getParameter("confirmRebootstrap");
				if (!"on".equals(confirm)) {
					throw new BotException("You must click 'I understand'");
				}
				bean.processRebootstrap();
			} else if (export != null) {
				bean.export(response, state);
				return;
			} else if (importLib != null) {
				response.sendRedirect("script-import.jsp");
				return;
			} else if (newState != null) {
				bean.newState();
				response.sendRedirect("edit-self.jsp");
				return;
			} else if (edit != null) {
				bean.editState(state);
				response.sendRedirect("edit-self.jsp");
				return;
			} else if (compile != null) {
				if (!bean.compile(input, null, "on".equals(debug), "on".equals(optimize))) {
					response.sendRedirect("edit-self.jsp");
					return;
				}
			} else if (decompile != null) {
				bean.decompileState();
				response.sendRedirect("edit-self.jsp");
				return;
			} else if (cancel != null) {
				bean.setStateCode("");
			} else if (up != null) {
				bean.moveSelectStateUp(state);
			} else if (down != null) {
				bean.moveSelectStateDown(state);
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("self.jsp");
	}
}
