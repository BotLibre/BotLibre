<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.service.Stats"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Stats - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
</head>
<body>
	<script>
	$(document).ready(function() { 
			$("#stats").tablesorter({widgets: ['zebra']});
		}
	);
	</script>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">
			<div class="about">
				<h1>Stats</h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
					Must be sys admin
				<% } else { %>
					<table id="stats" class="tablesorter">
						<thead>
						<tr>
							<th>Date</th>
							<th>Page views</th>
							<th>Sessions</th>
							<th>API Calls</th>
							<th>API Over Limit</th>
							<th>Bad API calls</th>
							<th>Anonymous API calls</th>
							<th>Anonymous API Over Limit</th>
							<th>Errors</th>
							<th>Memory Frees</th>
							<th>Total Memory</th>
							<th>Free Memory</th>
							<th>Web Downloads</th>
							<th>Desktop Downloads</th>
							
							<th>Cached Translations</th>
							<th>Translations</th>
							<th>Translation Errors</th>
							<th>Cached Bot Translations</th>
							<th>Bot Translations</th>
							<th>Bot Translation Errors</th>
							<th>Speech API</th>
							
							<th>Bots</th>
							<th>Active Bots</th>
							<th>Bot Messages</th>
							<th>Bot Conversations</th>
							<th>Bot Creates</th>
							<th>Bot Connects</th>
							<th>Bot Chats</th>
							<th>Response Time</th>
							<th>Bot Chat Timeouts</th>
							<th>Twitter Bots</th>
							<th>Twitter Runs</th>
							<th>Platinum Runs</th>
							<th>Gold Runs</th>
							<th>Bronze Runs</th>
							<th>Tweets</th>
							<th>Retweets</th>
							<th>Processed</th>
							<th>Direct Messages</th>
							<th>Facebook Bots</th>
							<th>Facebook Runs</th>
							<th>Platinum Runs</th>
							<th>Gold Runs</th>
							<th>Bronze Runs</th>
							<th>Posts</th>
							<th>Processed</th>
							<th>Messages</th>
							<th>API</th>
							<th>Telegram Bots</th>
							<th>Telegram Runs</th>
							<th>Platinum Runs</th>
							<th>Gold Runs</th>
							<th>Bronze Runs</th>
							<th>Posts</th>
							<th>Messages</th>
							<th>API</th>
							<th>Skype Bots</th>
							<th>Messages</th>
							<th>API</th>
							<th>Kik Bots</th>
							<th>Messages</th>
							<th>API</th>
							<th>Slack Bots</th>
							<th>Slack Runs</th>
							<th>Messages</th>
							<th>API</th>
							<th>WeChat Bots</th>
							<th>Messages</th>
							<th>API</th>
							<th>Email Bots</th>
							<th>Email Runs</th>
							<th>Platinum Runs</th>
							<th>Gold Runs</th>
							<th>Bronze Runs</th>
							<th>Bot Emails</th>
							<th>Processed</th>
							<th>Admin Emails</th>
							<th>SMS Sent</th>
							<th>SMS Processed</th>
							<th>SMS API</th>
							<th>Voice Calls</th>
							<th>Voice Processed</th>
							<th>Voice API</th>
							<th>WhatsApp Sent</th>
							<th>WhatsApp Processed</th>
							<th>WhatsApp API</th>
							<th>Alexa Bots</th>
							<th>Alexa Messages</th>
							<th>Alexa API</th>
							<th>Google Assistant Bots</th>
							<th>Google Assistant Messages</th>
							<th>Google Assistant API</th>
							
							<th>User Creates</th>
							<th>User Connects</th>
							<th>User Messages</th>
							
							<th>Forum Creates</th>
							<th>Forum Posts</th>
							<th>Forum Views</th>
							
							<th>Live Chat Creates</th>
							<th>Live Chat Rooms</th>
							<th>Live Chat Connects</th>
							<th>Live Chat Messages</th>
							
							<th>Analytic Image Uploads</th>
							<th>Analytic Binary Upload</th>
							<th>Analytic Test Image Upload</th>
							<th>Analytic Test</th>
							<th>Analytic Train</th>
							<th>Analytic Train Busy</th>
							<th>Analytic Test Media</th>
							<th>Analytic Test Media Busy</th>
						</tr>
						</thead>
						<tbody>
						<% for (Stats stat : AdminDatabase.instance().getAllStats()) { %>
							<tr>
								<td><%= stat.date %></td>
								<td><%= stat.pages %></td>
								<td><%= stat.sessions %></td>
								<td><%= stat.apiCalls %></td>
								<td><%= stat.apiOverLimit %></td>
								<td><%= stat.badAPICalls %></td>
								<td><%= stat.anonymousAPICalls %></td>
								<td><%= stat.anonymousAPIOverLimit %></td>
								<td><%= stat.errors %></td>
								<td><%= stat.memoryFrees %></td>
								<td><%= stat.totalMemory %></td>
								<td><%= stat.freeMemory %></td>
								<td><%= stat.webDownloads %></td>
								<td><%= stat.desktopDownloads %></td>
								
								<td><%= stat.cachedTranslations %></td>
								<td><%= stat.translations %></td>
								<td><%= stat.translationErrors %></td>
								<td><%= stat.cachedBotTranslations %></td>
								<td><%= stat.botTranslations %></td>
								<td><%= stat.botTranslationErrors %></td>
								<td><%= stat.speechAPI %></td>
								
								<td><%= stat.bots %></td>
								<td><%= stat.activeBots %></td>
								<td><%= stat.botMessages %></td>
								<td><%= stat.botConversations %></td>
								<td><%= stat.botCreates %></td>
								<td><%= stat.botConnects %></td>
								<td><%= stat.botChats %></td>
								<td><%= stat.botChatTotalResponseTime / (Math.max(stat.botMessages, 1)) %></td>
								<td><%= stat.botChatTimeouts %></td>
								<td><%= stat.twitterBots %></td>
								<td><%= stat.twitterRuns %></td>
								<td><%= stat.platinumTwitterRuns %></td>
								<td><%= stat.goldTwitterRuns %></td>
								<td><%= stat.bronzeTwitterRuns %></td>
								<td><%= stat.botTweets %></td>
								<td><%= stat.botRetweets %></td>
								<td><%= stat.botTweetsProcessed %></td>
								<td><%= stat.botDirectMessagesProcessed %></td>
								<td><%= stat.facebookBots %></td>
								<td><%= stat.facebookRuns %></td>
								<td><%= stat.platinumFacebookRuns %></td>
								<td><%= stat.goldFacebookRuns %></td>
								<td><%= stat.bronzeFacebookRuns %></td>
								<td><%= stat.botFacebookPosts %></td>
								<td><%= stat.botFacebookProcessed %></td>
								<td><%= stat.botFacebookMessagesProcessed %></td>
								<td><%= stat.botFacebookAPI %></td>
								<td><%= stat.telegramBots %></td>
								<td><%= stat.telegramRuns %></td>
								<td><%= stat.platinumTelegramRuns %></td>
								<td><%= stat.goldTelegramRuns %></td>
								<td><%= stat.bronzeTelegramRuns %></td>
								<td><%= stat.botTelegramPosts %></td>
								<td><%= stat.botTelegramMessagesProcessed %></td>
								<td><%= stat.botTelegramAPI %></td>
								<td><%= stat.skypeBots %></td>
								<td><%= stat.botSkypeMessagesProcessed %></td>
								<td><%= stat.botSkypeAPI %></td>
								<td><%= stat.kikBots %></td>
								<td><%= stat.botKikMessagesProcessed %></td>
								<td><%= stat.botKikAPI %></td>
								<td><%= stat.slackBots %></td>
								<td><%= stat.slackRuns %></td>
								<td><%= stat.botSlackMessagesProcessed %></td>
								<td><%= stat.botSlackAPI %></td>
								<td><%= stat.wechatBots %></td>
								<td><%= stat.botWeChatMessagesProcessed %></td>
								<td><%= stat.botWeChatAPI %></td>
								<td><%= stat.emailBots %></td>
								<td><%= stat.emailRuns %></td>
								<td><%= stat.platinumEmailRuns %></td>
								<td><%= stat.goldEmailRuns %></td>
								<td><%= stat.bronzeEmailRuns %></td>
								<td><%= stat.botEmails %></td>
								<td><%= stat.botEmailsProcessed %></td>
								<td><%= stat.emails %></td>
								<td><%= stat.botSMSSent %></td>
								<td><%= stat.botSMSProcessed %></td>
								<td><%= stat.botSMSAPI %></td>
								<td><%= stat.botTwilioVoiceCalls %></td>
								<td><%= stat.botTwilioVoiceProcessed %></td>
								<td><%= stat.botTwilioVoiceAPI %></td>
								<td><%= stat.botWhatsAppSent %></td>
								<td><%= stat.botWhatsAppProcessed %></td>
								<td><%= stat.botWhatsAppAPI %></td>
								<td><%= stat.alexaBots %></td>
								<td><%= stat.botAlexaMessagesProcessed %></td>
								<td><%= stat.botAlexaAPI %></td>
								<td><%= stat.googleAssistantBots %></td>
								<td><%= stat.botGoogleAssistantMessagesProcessed %></td>
								<td><%= stat.botGoogleAssistantAPI %></td>
								
								<td><%= stat.userCreates %></td>
								<td><%= stat.userConnects %></td>
								<td><%= stat.userMessages %></td>
								
								<td><%= stat.forumCreates %></td>
								<td><%= stat.forumPosts %></td>
								<td><%= stat.forumPostViews %></td>
								
								<td><%= stat.chatCreates %></td>
								<td><%= stat.chatRooms %></td>
								<td><%= stat.chatConnects %></td>
								<td><%= stat.chatMessages %></td>
								
								<td><%= stat.analyticImageUpload %></td>
								<td><%= stat.analyticBinaryUpload %></td>
								<td><%= stat.analyticTestImageUpload %></td>
								<td><%= stat.analyticTest %></td>
								<td><%= stat.analyticTraining %></td>
								<td><%= stat.analyticTrainingBusy %></td>
								<td><%= stat.analyticTestMediaProcessing %></td>
								<td><%= stat.analyticTestMediaBusy %></td>
								
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