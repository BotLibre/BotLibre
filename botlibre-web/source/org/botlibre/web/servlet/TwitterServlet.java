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

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.TwitterBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/twitter")
@SuppressWarnings("serial")
public class TwitterServlet extends BeanServlet {
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
		TwitterBean bean = loginBean.getBean(TwitterBean.class);

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
				response.sendRedirect("twitter.jsp");
				return;
			}
			botBean.checkAdmin();
			String userName = (String)request.getParameter("user");
			String token = (String)request.getParameter("token");
			String secret = (String)request.getParameter("secret");
			String submit = (String)request.getParameter("connect");
			if (submit != null) {
				bean.connect(userName, token, secret);
			}
			
			String importTweets = (String)request.getParameter("import");
			String tweetSearch = (String)request.getParameter("tweetSearch");
			String maxTweets = (String)request.getParameter("maxTweets");
			boolean tweets = "on".equals((String)request.getParameter("tweets"));
			boolean replies = "on".equals((String)request.getParameter("replies"));
			if (importTweets != null) {
				bean.importTweets(tweetSearch, maxTweets, tweets, replies);
			}

			String welcomeMessage = (String)request.getParameter("welcomeMessage");
			boolean autoFollow = "on".equals((String)request.getParameter("autoFollow"));
			boolean autoFollowFriendsFriends = "on".equals((String)request.getParameter("autoFollowFriendsFriends"));
			boolean autoFollowFriendsFollowers = "on".equals((String)request.getParameter("autoFollowFriendsFollowers"));
			String autoFollowSearch = "";//(String)request.getParameter("autoFollowSearch");
			String autoFollowKeywords = (String)request.getParameter("autoFollowKeywords");
			boolean followMessages = "on".equals((String)request.getParameter("followMessages"));
			String maxFriends = (String)request.getParameter("maxFriends");
			String maxStatus = (String)request.getParameter("maxStatus");
			String maxSearch = (String)request.getParameter("maxSearch");
			boolean processStatus = "on".equals((String)request.getParameter("processStatus"));
			boolean listenStatus = "on".equals((String)request.getParameter("listenStatus"));
			boolean learn = "on".equals((String)request.getParameter("learn"));
			boolean learnFromSelf = "on".equals((String)request.getParameter("learnFromSelf"));
			String statusKeywords = (String)request.getParameter("statusKeywords");
			boolean tweetChats = "on".equals((String)request.getParameter("tweetChats"));
			String retweet = (String)request.getParameter("retweet");
			String tweetRSS = (String)request.getParameter("tweetRSS");
			String rssKeywords = (String)request.getParameter("rssKeywords");
			boolean autoTweet = "on".equals((String)request.getParameter("autoTweet"));
			String autoTweetHours = (String)request.getParameter("autoTweetHours");
			String autoTweets = (String)request.getParameter("autoTweets");
			boolean replyToMentions = "on".equals((String)request.getParameter("replyToMentions"));
			boolean replyToMessages = "on".equals((String)request.getParameter("replyToMessages"));
			boolean ignoreReplies = "on".equals((String)request.getParameter("ignoreReplies"));
			submit = (String)request.getParameter("save");
			if (submit != null) {
				bean.save(autoFollow, autoFollowFriendsFriends, autoFollowFriendsFollowers, autoFollowSearch, autoFollowKeywords, followMessages, welcomeMessage, maxFriends,
						maxStatus, statusKeywords, processStatus, listenStatus, learn, learnFromSelf,
						tweetChats, replyToMentions, replyToMessages, retweet, tweetRSS, rssKeywords,
						ignoreReplies, tweetSearch, maxSearch,
						autoTweet, autoTweetHours, autoTweets);
			}
			submit = (String)request.getParameter("disconnect");
			if (submit != null) {
				bean.disable();
			}
			submit = (String)request.getParameter("add-friend");
			if (submit != null) {
				String friend = (String)request.getParameter("friend");
				bean.addFriend(friend);
			}
			submit = (String)request.getParameter("remove-friend");
			if (submit != null) {
				String friend = (String)request.getParameter("friend");
				bean.removeFriend(friend);
			}
			submit = (String)request.getParameter("authorise");
			if (submit != null) {
				bean.authoriseAccount();
			}
			submit = (String)request.getParameter("authorise-complete");
			String pin = (String)request.getParameter("pin");
			if (submit != null) {
				bean.authoriseComplete(pin);
			}
			submit = (String)request.getParameter("cancel");
			if (submit != null) {
				bean.cancelAuthorisation();
			}
			submit = (String)request.getParameter("check");
			if (submit != null) {
				bean.checkStatus();
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("twitter.jsp");
	}
}
