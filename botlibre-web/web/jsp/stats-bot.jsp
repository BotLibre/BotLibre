<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.service.BotStats"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Bot Stats - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
</head>
<body>
	<script>
	$(document).ready(function() {
		$("#stats").tablesorter({widgets: ['zebra']});
	});
	</script>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="about">
				<h1>Bot Stats - <%= Utils.sanitize(loginBean.getSelectedStats()) %></h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
					Must be sys admin
				<% } else { %>
					<table id="stats" class="tablesorter">
						<thead>
						<tr>
							<th>Date</th>
							<th>Conversations</th>
							<th>Messages</th>
							<th>Conversation Length</th>
							<th>Engaged Conversations</th>
							<th>Default Responses</th>
							<th>Confidence</th>
							<th>Sentiment</th>
							<th>Connects</th>
							<th>Chats</th>
							<th>Live Chats</th>
							<th>API</th>
							<th>Errors</th>
							<th>Response Time</th>
							<th>Imports</th>
							<th>Tweets</th>
							<th>Retweets</th>
							<th>Tweets Processed</th>
							<th>Direct Messages</th>
							<th>Facebook Posts</th>
							<th>Facebook Likes</th>
							<th>Facebook Processed</th>
							<th>Facebook Messages</th>
							<th>Skype Messages</th>
							<th>WeChat Messages</th>
							<th>Kik Messages</th>
							<th>Slack Posts</th>
							<th>Slack Messages</th>
							<th>Telegram Posts</th>
							<th>Telegram Messages</th>
							<th>Emails</th>
							<th>Emails Processed</th>
							<th>SMS Sent</th>
							<th>SMS Processed</th>
							<th>Alexa Messages</th>
							<th>Google Assistant Messages</th>
						</tr>
						</thead>
						<tbody>
						<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(loginBean.getSelectedStats())) { %>
							<tr>
								<td><%= stat.date %></a></td>
								<td><%= stat.conversations %></td>
								<td><%= stat.messages %></td>
								<td><%= Math.round((stat.messages / Math.max(stat.conversations, 1)) * 100) / 100 %></td>
								<td><%= stat.engaged %></td>
								<td><%= stat.defaultResponses %></td>
								<td><%= Math.round((stat.confidence / Math.max(stat.messages, 1)) * 100) / 100 %></td>
								<td><%= Math.round((stat.sentiment / Math.max(stat.messages, 1)) * 100) %></td>
								<td><%= stat.connects %></td>
								<td><%= stat.chats %></td>
								<td><%= stat.livechats %></td>
								<td><%= stat.api %></td>
								<td><%= stat.errors %></td>
					 			<td><%= stat.chatTotalResponseTime / Math.max(stat.messages, 1) %></td>
								<td><%= stat.imports %></td>
								<td><%= stat.tweets %></td>
								<td><%= stat.retweets %></td>
								<td><%= stat.tweetsProcessed %></td>
								<td><%= stat.directMessagesProcessed %></td>
								<td><%= stat.facebookPosts %></td>
								<td><%= stat.facebookLikes %></td>
								<td><%= stat.facebookProcessed %></td>
								<td><%= stat.facebookMessagesProcessed %></td>
								<td><%= stat.skypeMessagesProcessed %></td>
								<td><%= stat.wechatMessagesProcessed %></td>
								<td><%= stat.kikMessagesProcessed %></td>
								<td><%= stat.slackPosts %></td>
								<td><%= stat.slackMessagesProcessed %></td>
								<td><%= stat.telegramPosts %></td>
								<td><%= stat.telegramMessagesProcessed %></td>
								<td><%= stat.emails %></td>
								<td><%= stat.emailsProcessed %></td>
								<td><%= stat.smsSent %></td>
								<td><%= stat.smsProcessed %></td>
								<td><%= stat.alexaMessagesProcessed %></td>
								<td><%= stat.googleAssistantMessagesProcessed %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>