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

import org.botlibre.BotException;
import org.botlibre.util.Utils;

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.MemoryBean;
import org.botlibre.web.bean.MemoryBean.BrowseMode;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/memory")
@SuppressWarnings("serial")
public class MemoryServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}
	
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
		MemoryBean bean = loginBean.getBean(MemoryBean.class);
		
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
			String submit = (String)request.getParameter("query");
			if (submit != null) {
				String input = Utils.sanitize((String)request.getParameter("input"));
				String type = Utils.sanitize((String)request.getParameter("type"));
				String classification = Utils.sanitize((String)request.getParameter("classification"));
				String pinned = Utils.sanitize((String)request.getParameter("pinned"));
				String sort = Utils.sanitize((String)request.getParameter("sort"));
				String order = Utils.sanitize((String)request.getParameter("order"));
				bean.processBrowse(input, type, classification, "on".equals(pinned), sort, order);
			}
			submit = (String)request.getParameter("graph");
			if (submit != null) {
				bean.processGraph(request);
			}
			submit = (String)request.getParameter("migrate");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.migrate();
			}
			submit = (String)request.getParameter("page");
			if (submit != null) {
				bean.processBrowsePage(submit);
			}
			submit = (String)request.getParameter("delete-all");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				String confirm = (String)request.getParameter("confirm");
				if (!"on".equals(confirm)) {
					throw new BotException("You must click 'I understand'");
				}
				bean.processDeleteAll();
			}
			submit = (String)request.getParameter("clear-cache");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.processClearCache();
			}
			submit = (String)request.getParameter("execute");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				String code = (String)request.getParameter("code");
				bean.executeCode(code);
			}
			submit = (String)request.getParameter("browse");
			if (submit != null) {
				bean.processSelection(request);
			}
			submit = (String)request.getParameter("search");
			if (submit != null) {
				bean.reset();
				bean.setMode(BrowseMode.Search);
			}
			submit = (String)request.getParameter("reports");
			if (submit != null) {
				bean.reset();
				bean.setMode(BrowseMode.Reports);
			}
			submit = (String)request.getParameter("worksheet");
			if (submit != null) {
				bean.reset();
				bean.setMode(BrowseMode.Worksheet);
			}
			submit = (String)request.getParameter("status");
			if (submit != null) {
				bean.reset();
				bean.setMode(null);
			}
			submit = (String)request.getParameter("pin");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.processPin(request);
			}
			submit = (String)request.getParameter("unpin");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.processUnpin(request);
			}
			submit = (String)request.getParameter("delete");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.processDelete(request);
			}
			submit = (String)request.getParameter("references");
			if (submit != null) {
				bean.processReferences(request);
			}
			submit = (String)request.getParameter("select-all");
			if (submit != null) {
				if (bean.isSelectAll()) {
					bean.setSelectAll(false);
				} else {
					bean.setSelectAll(true);
				}
			}
			submit = (String)request.getParameter("export");
			if (submit != null) {
				String exportFormat = (String)request.getParameter("export-format");
				try {
					loginBean.verifyPostToken(postToken);
					bean.export(response, exportFormat);
				} catch (Exception failed) {
					botBean.error(failed);
				}
				return;
			}
			String importLib = (String)request.getParameter("importlib");
			if (importLib != null) {
				response.sendRedirect("memory-import.jsp");
				return;
			}
			String report = (String)request.getParameter("report");
			if (report != null) {
				loginBean.verifyPostToken(postToken);
				if (report.equals("unreferenced")) {
					bean.processUnreferenced();
				} else if (report.equals("unreferenced-data")) {
					bean.processUnreferencedWithData();
				} else if (report.equals("unreferenced-data")) {
					bean.processUnreferencedWithData();
				} else if (report.equals("unreferenced-pinned")) {
					bean.processUnreferencedPinned();
				} else if (report.equals("old-data")) {
					bean.processOldData();
				} else if (report.equals("least-referenced")) {
					bean.processLeastReferenced();
				} else if (report.equals("most-relationships")) {
					bean.processMostRelationships();
				}
			}
			String task = (String)request.getParameter("task");
			if (task != null) {
				loginBean.verifyPostToken(postToken);
				if (task.equals("forget")) {
					bean.runForgetfullness();
				} else if (task.equals("delete-unreferenced")) {
					bean.deleteUnreferenced();
				} else if (task.equals("delete-unreferenced-data")) {
					bean.deleteUnreferencedData();
				} else if (task.equals("delete-unreferenced-pinned")) {
					bean.deleteUnreferencedPinned();
				} else if (task.equals("delete-old-data")) {
					bean.deleteOldData();
				} else if (task.equals("delete-grammar")) {
					bean.deleteGrammar();
				} else if (task.equals("fix-responses")) {
					bean.fixResponses();
				} else if (task.equals("fix-relationships")) {
					bean.fixRelationships();
				}
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("memory.jsp").forward(request, response);
	}
}
