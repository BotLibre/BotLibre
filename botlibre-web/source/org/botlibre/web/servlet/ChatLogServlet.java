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

import org.botlibre.web.bean.ChatLogBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/chat-log")
@SuppressWarnings("serial")
public class ChatLogServlet extends BeanServlet {
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
		ChatLogBean bean = loginBean.getBean(ChatLogBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
				return;
			}
			
			botBean.checkAdmin();
			String home = (String)request.getParameter("home");
			String newResponse = (String)request.getParameter("new");
			String correct = (String)request.getParameter("correct");
			String browse = (String)request.getParameter("browse");
			String invalidate = (String)request.getParameter("invalidate");
			String validate = (String)request.getParameter("validate");
			String flag = (String)request.getParameter("flag");
			String unflag = (String)request.getParameter("unflag");
			String save = (String)request.getParameter("save");
			String delete = (String)request.getParameter("delete");
			String selectAll = (String)request.getParameter("selectAll");
			String search = (String)request.getParameter("search");
			
			String type = (String)request.getParameter("type");
			String restrict = (String)request.getParameter("restrict");
			String sort = (String)request.getParameter("sort");
			String duration = (String)request.getParameter("duration");
			String export = (String)request.getParameter("export");
			String exportFormat = (String)request.getParameter("export-format");
			String filter = (String)request.getParameter("filter");
			String topic = (String)request.getParameter("topic");
			String label = (String)request.getParameter("label");
			String previous = (String)request.getParameter("previous");
			String next = (String)request.getParameter("next");
			String repeat = (String)request.getParameter("repeat");
			String keywords = (String)request.getParameter("keywords");
			String required = (String)request.getParameter("required");
			String emotes = (String)request.getParameter("emotes");
			String sentiment = (String)request.getParameter("sentiment");
			String confidence = (String)request.getParameter("confidence");
			String actions = (String)request.getParameter("actions");
			String poses = (String)request.getParameter("poses");
			String condition = (String)request.getParameter("condition");
			String think = (String)request.getParameter("think");
			String command = (String)request.getParameter("command");
			String synonyms = (String)request.getParameter("synonyms");
			boolean all = "on".equals((String)request.getParameter("all"));
			String page = (String)request.getParameter("page");
			String importLib = (String)request.getParameter("import-lib");
			String editEditor = (String)request.getParameter("edit-editor");
			String exportNumberPages = (String)request.getParameter("exportNumberPages");
			boolean exportGreetings = "on".equals(request.getParameter("exportGreetings"));
			boolean exportDefaultResponses = "on".equals(request.getParameter("exportDefaultResponses"));

			boolean changed = false;
			if (home != null) {
				bean.reset();
				request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
				return;
			}
			if (newResponse != null) {
				if (search != null) {
					bean.setSearch(search);
				}
				bean.processNewResponse();
				request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
				return;
			} else if (correct != null) {
				loginBean.verifyPostToken(postToken);
				bean.processCorrection(request);
				request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
				return;
			} else if (browse != null) {
				bean.processBrowse(request);
				request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
				return;
			} else if (export != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.export(response, exportFormat, exportNumberPages, exportGreetings, exportDefaultResponses);
				} catch (Exception failed) {
					botBean.error(failed);
					request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
				}
				return;
			} else if (importLib != null) {
				response.sendRedirect("chatlog-import.jsp");
				return;
			} else if (invalidate != null) {
				loginBean.verifyPostToken(postToken);
				changed = true;
				bean.processInvalidate(request);
			} else if (validate != null) {
				loginBean.verifyPostToken(postToken);
				changed = true;
				bean.processValidate(request);
			} else if (flag != null) {
				loginBean.verifyPostToken(postToken);
				changed = true;
				bean.processFlag(request);
			} else if (unflag != null) {
				loginBean.verifyPostToken(postToken);
				changed = true;
				bean.processUnflag(request);
			} else if (save != null) {
				loginBean.verifyPostToken(postToken);
				changed = true;
				bean.processSave(request);
			} else if (delete != null) {
				loginBean.verifyPostToken(postToken);
				changed = true;
				bean.processDelete(request);
			}
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				changed = true;
				bean.processCancel();
			}
			if (duration != null) {
				if (!bean.getDuration().equals(duration)) {
					changed = true;
					bean.setDuration(Utils.sanitize(duration));
				}
			}
			if (filter != null) {
				if (!bean.getFilter().equals(filter.trim())) {
					changed = true;
					bean.setFilter(Utils.sanitize(filter.trim()));
				}
			}
			if (search != null) {
				if (!bean.getSearch().equals(search)) {
					changed = true;
					bean.setSearch(Utils.sanitize(search));
				}
			}
			if (type != null) {
				if (!bean.getType().equals(type)) {
					changed = true;
					bean.setType(Utils.sanitize(type));
				}
			}
			if (restrict != null) {
				if (!bean.getRestriction().equals(restrict)) {
					changed = true;
					bean.setRestriction(Utils.sanitize(restrict));
				}
			}
			if (sort != null) {
				if (!bean.getSort().equals(sort)) {
					changed = true;
					bean.setSort(Utils.sanitize(sort));
				}
			}
			if (editEditor != null) {
				if (!bean.getEditorType().equals(editEditor)) {
					bean.setEditorType(Utils.sanitize(editEditor));
				}
			}
			bean.setSelectAll(!bean.isSelectAll() && (selectAll != null));
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				changed = true;
			} else {
				bean.setPage(0);
				if (duration != null) {
					bean.setShowTopic(all || "on".equals(topic));
					bean.setShowLabel(all || "on".equals(label));
					bean.setShowPrevious(all || "on".equals(previous));
					bean.setShowRepeat(all || "on".equals(repeat));
					bean.setShowKeyWords(all || "on".equals(keywords));
					bean.setShowRequired(all || "on".equals(required));
					bean.setShowEmotes(all || "on".equals(emotes));
					bean.setShowActions(all || "on".equals(actions));
					bean.setShowPoses(all || "on".equals(poses));
					bean.setShowCondition(all || "on".equals(condition));
					bean.setShowThink(all || "on".equals(think));
					bean.setShowCommand(all || "on".equals(command));
					bean.setShowSentiment(all || "on".equals(sentiment));
					bean.setShowConfidence(all || "on".equals(confidence));
					bean.setShowNext(all || "on".equals(next));
					bean.setShowSynonyms(all || "on".equals(synonyms));
				}
			}
			changed = true; // REST calls may have changed.
			if (changed) {
				bean.processQuery();
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		request.getRequestDispatcher("chatlogs.jsp").forward(request, response);
	}
}
