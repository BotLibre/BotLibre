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

import org.botlibre.web.admin.ClientType;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.service.PageStats;
import org.botlibre.web.bean.ForumBean;
import org.botlibre.web.bean.ForumPostBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.SessionProxyBean;

@javax.servlet.annotation.WebServlet("/forum-post")
@SuppressWarnings("serial")
public class ForumPostServlet extends BeanServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		// TODO: Temp fix to prevent crawler dos attack.
		if (request.getParameter("proxy") != null) {
			PageStats.page(request);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		if (!loginBean.checkDomain(request, response)) {
			return;
		}
		ForumPostBean bean = loginBean.getBean(ForumPostBean.class);
		ForumBean forumBean = loginBean.getBean(ForumBean.class);
		if (bean.getForumBean() == null) {
			bean.setForumBean(forumBean);
		}
		
		String browse = (String)request.getParameter("id");
		if (browse != null) {
			if (!bean.validateInstance(browse, ClientType.WEB)) {
				request.getRequestDispatcher("browse-forum.jsp").forward(request, response);
				return;
			}
			request.getRequestDispatcher("forum-post.jsp").forward(request, response);
			return;
		}
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		ForumPostBean bean = loginBean.getBean(ForumPostBean.class);
		ForumBean forumBean = loginBean.getBean(ForumBean.class);
		if (bean.getForumBean() == null) {
			bean.setForumBean(forumBean);
		}
		
		try {
			String postToken = (String)request.getParameter("postToken");
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
			String forum = (String)request.getParameter("forum");
			if (forum != null) {
				if (forumBean.getInstance() == null || !String.valueOf(forumBean.getInstanceId()).equals(forum)) {
					forumBean.validateInstance(forum);
				}
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance, ClientType.WEB);
				}
			}
			bean.setPreview(false);
			bean.setEditorChange(false);
			String showDetails = (String)request.getParameter("show-details");
			if (showDetails != null) {
				response.sendRedirect("forum?details=true&id=" + forumBean.getInstanceId() + proxy.proxyString());
				return;
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String cancelReply = (String)request.getParameter("cancel-reply");
			if (cancelReply != null) {
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				response.sendRedirect("browse-post.jsp");
				return;
			}
			String createPost = (String)request.getParameter("create-post");
			String reply = (String)request.getParameter("reply");
			String createReply = (String)request.getParameter("create-reply");
			String createInstance = (String)request.getParameter("create-instance");
			String editInstance = (String)request.getParameter("edit-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			String thumbsup = (String)request.getParameter("thumbs-up");
			String thumbsdown = (String)request.getParameter("thumbs-down");
			String star1 = (String)request.getParameter("star1");
			String star2 = (String)request.getParameter("star2");
			String star3 = (String)request.getParameter("star3");
			String star4 = (String)request.getParameter("star4");
			String star5 = (String)request.getParameter("star5");
			String unflag = (String)request.getParameter("unflag");
			String flag = (String)request.getParameter("flag");
			String flagged = (String)request.getParameter("flagged");
			String flagReason = (String)request.getParameter("flag-reason");
			if (createPost != null) {
				request.getRequestDispatcher("create-post.jsp").forward(request, response);
				return;
			}
			if (reply != null) {
				request.getRequestDispatcher("create-reply.jsp").forward(request, response);
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-post.jsp").forward(request, response);
				return;
			}
			if (flag != null) {
				loginBean.verifyPostToken(postToken);
				bean.flagInstance(flagReason, flagged);
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (unflag != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.unflagInstance();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (thumbsup != null) {
				loginBean.verifyPostToken(postToken);
				bean.thumbsUp();
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (thumbsdown != null) {
				loginBean.verifyPostToken(postToken);
				bean.thumbsDown();
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (star1 != null) {
				loginBean.verifyPostToken(postToken);
				bean.star(1);
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (star2 != null) {
				loginBean.verifyPostToken(postToken);
				bean.star(2);
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (star3 != null) {
				loginBean.verifyPostToken(postToken);
				bean.star(3);
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (star4 != null) {
				loginBean.verifyPostToken(postToken);
				bean.star(4);
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			if (star5 != null) {
				loginBean.verifyPostToken(postToken);
				bean.star(5);
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String subscribe = (String)request.getParameter("subscribe");
			if (subscribe != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.subscribe();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String unsubscribe = (String)request.getParameter("unsubscribe");
			if (unsubscribe != null) {
				try {
					loginBean.verifyPostToken(postToken);
					bean.unsubscribe();
				} catch (Exception failed) {
					bean.error(failed);
				}
				response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			
			String page = (String)request.getParameter("page");
			String userFilter = (String)request.getParameter("user-filter");
			
			String topic = (String)request.getParameter("topic");
			String details = (String)request.getParameter("details");
			String tags = (String)request.getParameter("tags");
			String isFeatured = (String)request.getParameter("isFeatured");
			String delete = (String)request.getParameter("delete");
			String replyToParent = (String)request.getParameter("replyToParent");
			String autosubscribe = (String)request.getParameter("autosubscribe");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.createInstance(topic, details, "on".equals(autosubscribe), tags)) {
					request.getRequestDispatcher("create-post.jsp").forward(request, response);
				} else {
					request.getRequestDispatcher("browse-post.jsp").forward(request, response);
				}
				return;
			}
			if (createReply != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.createReply(details, "on".equals(replyToParent))) {
					request.getRequestDispatcher("create-reply.jsp").forward(request, response);
				} else {
					response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateInstance(topic, details, tags, "on".equals(isFeatured), ClientType.WEB)) {
						request.getRequestDispatcher("edit-post.jsp").forward(request, response);
					} else {
						response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
					request.getRequestDispatcher("edit-post.jsp").forward(request, response);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("forum-post?id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					request.getRequestDispatcher("browse-post.jsp").forward(request, response);
				}
				return;
			}
			String previewCreate = (String)request.getParameter("preview-create");
			if (previewCreate != null) {
				loginBean.verifyPostToken(postToken);
				bean.preview(topic, details, tags, true);
				request.getRequestDispatcher("create-post.jsp").forward(request, response);
				return;
			}
			String previewEdit = (String)request.getParameter("preview-edit");
			if (previewEdit != null) {
				loginBean.verifyPostToken(postToken);
				bean.preview(topic, details, tags, false);
				request.getRequestDispatcher("edit-post.jsp").forward(request, response);
				return;
			}
			String previewReply = (String)request.getParameter("preview-reply");
			if (previewReply != null) {
				loginBean.verifyPostToken(postToken);
				bean.previewReply(topic, details, tags, true);
				request.getRequestDispatcher("create-reply.jsp").forward(request, response);
				return;
			}
			String editEditor = (String)request.getParameter("edit-editor");
			if (editEditor != null) {
				loginBean.verifyPostToken(postToken);
				bean.changeEditor(topic, details, tags, false, editEditor);
				request.getRequestDispatcher("edit-post.jsp").forward(request, response);
				return;
			}
			String createEditor = (String)request.getParameter("create-editor");
			if (createEditor != null) {
				loginBean.verifyPostToken(postToken);
				bean.changeEditor(topic, details, tags, true, createEditor);
				request.getRequestDispatcher("create-post.jsp").forward(request, response);
				return;
			}
			String replyEditor = (String)request.getParameter("reply-editor");
			if (replyEditor != null) {
				loginBean.verifyPostToken(postToken);
				bean.changeReplyEditor(topic, details, tags, true, replyEditor);
				request.getRequestDispatcher("create-reply.jsp").forward(request, response);
				return;
			}
			
			if (page != null) {
				bean.setPage(Integer.valueOf(page));
				request.getRequestDispatcher("browse-post.jsp").forward(request, response);
				return;
			}
			if (userFilter != null) {
				bean.resetSearch();
				bean.setUserFilter(Utils.sanitize(userFilter));
				bean.setInstanceFilter(InstanceFilter.Personal);
				if (!loginBean.isEmbedded()) {
					forumBean.setInstance(null);
				}
				request.getRequestDispatcher("browse-post.jsp").forward(request, response);
				return;
			}

			setSearchFields(request, bean);
		} catch (Exception failed) {
			bean.error(failed);
		}
		request.getRequestDispatcher("browse-post.jsp").forward(request, response);
	}
}
