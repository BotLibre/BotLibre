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

import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.FacebookBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/facebook")
@SuppressWarnings("serial")
public class FacebookServlet extends BeanServlet {
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
		FacebookBean bean = loginBean.getBean(FacebookBean.class);

		try {
			String postToken = (String)request.getParameter("postToken");
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				response.sendRedirect("facebook.jsp");
				return;
			}
			botBean.checkAdmin();
			String userName = Utils.sanitize((String)request.getParameter("user"));
			String token = (String)request.getParameter("token");
			String page = Utils.sanitize((String)request.getParameter("page"));
			String key = Utils.sanitize((String)request.getParameter("appOauthKey"));
			String secret = Utils.sanitize((String)request.getParameter("appOauthSecret"));
			String submit = (String)request.getParameter("connect");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.connect(userName, token, page, key, secret);
			}

			String welcomeMessage = Utils.sanitize((String)request.getParameter("welcomeMessage"));
			boolean autoFriend = "on".equals((String)request.getParameter("autoFriend"));
			String autoFriendKeywords = Utils.sanitize((String)request.getParameter("autoFriendKeywords"));
			String maxFriends = Utils.sanitize((String)request.getParameter("maxFriends"));
			String maxPosts = Utils.sanitize((String)request.getParameter("maxPosts"));
			boolean processPosts = "on".equals((String)request.getParameter("processPosts"));
			String postKeywords = Utils.sanitize((String)request.getParameter("postKeywords"));
			boolean processAllPosts = "on".equals((String)request.getParameter("processAllPosts"));
			boolean processNewsFeed = "on".equals((String)request.getParameter("processNewsFeed"));
			String newsFeedKeywords = Utils.sanitize((String)request.getParameter("newsFeedKeywords"));
			boolean processAllNewsFeed = "on".equals((String)request.getParameter("processAllNewsFeed"));
			String likeKeywords = (String)request.getParameter("likeKeywords");
			boolean likeAllPosts = "on".equals((String)request.getParameter("likeAllPosts"));
			String postRSS = Utils.sanitize((String)request.getParameter("postRSS"));
			String rssKeywords = Utils.sanitize((String)request.getParameter("rssKeywords"));
			boolean autoPost = "on".equals((String)request.getParameter("autoPost"));
			boolean stripButtonText = "on".equals((String)request.getParameter("stripButtonText"));
			boolean trackMessageObjects = "on".equals((String)request.getParameter("trackMessageObjects"));
			String autoPostHours = Utils.sanitize((String)request.getParameter("autoPostHours"));
			String autoPosts = (String)request.getParameter("autoPosts"); // templates
			String messenger = Utils.sanitize((String)request.getParameter("messenger"));
			String buttonType = Utils.sanitize((String)request.getParameter("buttonType"));
			String persistentMenu = Utils.sanitize((String)request.getParameter("persistentMenu"));
			String getStartedButton = Utils.sanitize((String)request.getParameter("getStartedButton"));
			String greetingText = Utils.sanitize((String)request.getParameter("greetingText"));
			String facebookMessengerAccessToken = Utils.sanitize((String)request.getParameter("facebookMessengerAccessToken"));
			submit = (String)request.getParameter("save");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.save(autoFriend, autoFriendKeywords, welcomeMessage, maxFriends,
						maxPosts, postKeywords, processPosts, processAllPosts,
						newsFeedKeywords, processNewsFeed, processAllNewsFeed,
						messenger, facebookMessengerAccessToken, buttonType, stripButtonText, trackMessageObjects,
						persistentMenu, getStartedButton, greetingText,
						likeKeywords, likeAllPosts,
						postRSS, rssKeywords,
						autoPost, autoPostHours, autoPosts);
			}
			submit = (String)request.getParameter("disconnect");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.disable();
			}
			submit = (String)request.getParameter("authorise");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.authoriseAccount(key, secret, request);
				response.sendRedirect(bean.getAuthorisationURL());
				return;
			}
			String code = (String)request.getParameter("code");
			if (code != null) {
				bean.authoriseComplete(code);
			}
			submit = (String)request.getParameter("cancel");
			if (submit != null) {
				bean.cancelAuthorisation();
			}
			submit = (String)request.getParameter("check");
			if (submit != null) {
				loginBean.verifyPostToken(postToken);
				bean.checkStatus();
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("facebook.jsp");
	}
}
