<%@page import="org.botlibre.web.service.ErrorStats"%>
<%@page import="org.botlibre.web.service.LiveChatStats"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.service.BotStats"%>
<%@page import="org.botlibre.web.service.Stats"%>
<%@page import="org.botlibre.web.service.IPStats"%>
<%@page import="org.botlibre.web.service.PageStats"%>
<%@page import="org.botlibre.web.service.AppIDStats"%>
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
			$("#appstats").tablesorter({widgets: ['zebra']});
			$("#botstats").tablesorter({widgets: ['zebra']});
			$("#livechatstats").tablesorter({widgets: ['zebra']});
			$("#ipstats").tablesorter({widgets: ['zebra']});
			$("#pagestats").tablesorter({widgets: ['zebra']});
			$("#referstats").tablesorter({widgets: ['zebra']});
			$("#errortats").tablesorter({widgets: ['zebra']});
		}
	);
	</script>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="about">
				<h1>Stats</h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
					Must be sys admin
				<% } else { %>
					<a href="super?allstats=true">history</a><br/><br/>
					Page Views: <%= Stats.stats.pages %><br/>
					Sessions: <%= Stats.stats.sessions %><br/>
					API Calls: <%= Stats.stats.apiCalls %><br/>
					API Over Limit: <%= Stats.stats.apiOverLimit %><br/>
					Bad API Calls: <%= Stats.stats.badAPICalls %><br/>
					Anonymous API Calls: <%= Stats.stats.anonymousAPICalls %><br/>
					Anonymous API Over Limit: <%= Stats.stats.anonymousAPIOverLimit %><br/>
					Errors: <%= Stats.stats.errors %><br/>
					Memory Frees: <%= Stats.stats.memoryFrees %><br/>
					Total Memory: <%= Runtime.getRuntime().totalMemory() %><br/>
					Free Memory: <%= Runtime.getRuntime().freeMemory() %><br/>
					Web Downloads: <%= Stats.stats.webDownloads %><br/>
					Desktop Downloads: <%= Stats.stats.desktopDownloads %><br/>
					
					<h3>Twitter</h3>
					Twitterbots: <%= AdminDatabase.instance().getAllTwitterInstances().size() %><br/>
					Last run: <%= Stats.stats.lastTwitterRun %><br/>
					Runs: <%= Stats.stats.twitterRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumTwitterRuns %><br/>
					Gold Runs: <%= Stats.stats.goldTwitterRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeTwitterRuns %><br/>
					Tweets: <%= Stats.stats.botTweets %><br/>
					Retweets: <%= Stats.stats.botRetweets %><br/>
					Processed: <%= Stats.stats.botTweetsProcessed %><br/>
					Direct Messages: <%= Stats.stats.botDirectMessagesProcessed %><br/>
					 
					<h3>Facebook</h3>
					Facebots: <%= AdminDatabase.instance().getAllFacebookInstances().size() %><br/>
					Last run: <%= Stats.stats.lastFacebookRun %><br/>
					Runs: <%= Stats.stats.facebookRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumFacebookRuns %><br/>
					Gold Runs: <%= Stats.stats.goldFacebookRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeFacebookRuns %><br/>
					Posts: <%= Stats.stats.botFacebookPosts %><br/>
					Likes: <%= Stats.stats.botFacebookLikes %>
					Processed: <%= Stats.stats.botFacebookProcessed %><br/>
					Messages: <%= Stats.stats.botFacebookMessagesProcessed %><br/>
					API: <%= Stats.stats.botFacebookAPI %><br/>
										 
					<h3>Telegram</h3>
					Telegrambots: <%= AdminDatabase.instance().getAllTelegramInstances().size() %><br/>
					Last run: <%= Stats.stats.lastTelegramRun %><br/>
					Runs: <%= Stats.stats.telegramRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumTelegramRuns %><br/>
					Gold Runs: <%= Stats.stats.goldTelegramRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeTelegramRuns %><br/>
					Posts: <%= Stats.stats.botTelegramPosts %><br/>
					Messages: <%= Stats.stats.botTelegramMessagesProcessed %><br/>
					API: <%= Stats.stats.botTelegramAPI %><br/>
					
					<h3>Slack</h3>
					Slackbots: <%= AdminDatabase.instance().getAllSlackInstances().size() %><br/>
					Last run: <%= Stats.stats.lastSlackRun %><br/>
					Runs: <%= Stats.stats.slackRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumSlackRuns %><br/>
					Gold Runs: <%= Stats.stats.goldSlackRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeSlackRuns %><br/>
					Posts: <%= Stats.stats.botSlackPosts %><br/>
					Messages: <%= Stats.stats.botSlackMessagesProcessed %><br/>
					API: <%= Stats.stats.botSlackAPI %><br/>
					
					<h3>Skype</h3>
					Skypebots: <%= AdminDatabase.instance().getAllSkypeInstances().size() %><br/>
					Last run: <%= Stats.stats.lastSkypeRun %><br/>
					Runs: <%= Stats.stats.skypeRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumSkypeRuns %><br/>
					Gold Runs: <%= Stats.stats.goldSkypeRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeSkypeRuns %><br/>
					Messages: <%= Stats.stats.botSkypeMessagesProcessed %><br/>
					API: <%= Stats.stats.botSkypeAPI %><br/>
					
					<h3>Kik</h3>
					Kikbots: <%= AdminDatabase.instance().getAllKikInstances().size() %><br/>
					Last run: <%= Stats.stats.lastKikRun %><br/>
					Runs: <%= Stats.stats.kikRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumKikRuns %><br/>
					Gold Runs: <%= Stats.stats.goldKikRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeKikRuns %><br/>
					Messages: <%= Stats.stats.botKikMessagesProcessed %><br/>
					API: <%= Stats.stats.botKikAPI %><br/>
					
					<h3>WeChat</h3>
					WeChatbots: <%= AdminDatabase.instance().getAllWeChatInstances().size() %><br/>
					Last run: <%= Stats.stats.lastWeChatRun %><br/>
					Runs: <%= Stats.stats.wechatRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumWeChatRuns %><br/>
					Gold Runs: <%= Stats.stats.goldWeChatRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeWeChatRuns %><br/>
					Messages: <%= Stats.stats.botWeChatMessagesProcessed %><br/>
					API: <%= Stats.stats.botWeChatAPI %><br/>
					 
					<h3>Email</h3>
					Emailbots: <%= AdminDatabase.instance().getAllEmailInstances().size() %><br/>
					Last run: <%= Stats.stats.lastEmailRun %><br/>
					Runs: <%= Stats.stats.emailRuns %><br/>
					Platinum Runs: <%= Stats.stats.platinumEmailRuns %><br/>
					Gold Runs: <%= Stats.stats.goldEmailRuns %><br/>
					Bronze Runs: <%= Stats.stats.bronzeEmailRuns %><br/>
					Admin Emails: <%= Stats.stats.emails %><br/>
					Bot Emails: <%= Stats.stats.botEmails %> Processed: <%= Stats.stats.botEmailsProcessed %><br/>
					
					<h3>Twilio</h3>
					SMS Sent: <%= Stats.stats.botSMSSent %><br/>
					SMS Processed: <%= Stats.stats.botSMSProcessed %><br/>
					SMS API: <%= Stats.stats.botSMSAPI %><br/>
					Voice Calls: <%= Stats.stats.botTwilioVoiceCalls %><br/>
					Voice Processed: <%= Stats.stats.botTwilioVoiceProcessed %><br/>
					Voice API: <%= Stats.stats.botTwilioVoiceAPI %><br/>
					
					<h3>Alexa</h3>
					Alexabots: <%= AdminDatabase.instance().getAllAlexaInstances().size() %><br/>
					Messages: <%= Stats.stats.botAlexaMessagesProcessed %><br/>
					API: <%= Stats.stats.botAlexaAPI %><br/>
					
					<h3>Google Assistant</h3>
					GoogleAssistantbots: <%= AdminDatabase.instance().getAllGoogleAssistantInstances().size() %><br/>
					Messages: <%= Stats.stats.botGoogleAssistantMessagesProcessed %><br/>
					API: <%= Stats.stats.botGoogleAssistantAPI %><br/>
					
					<h3>Bots</h3>
					Creates: <%= Stats.stats.botCreates %>
					Connects: <%= Stats.stats.botConnects %>
					Chats: <%= Stats.stats.botChats %>
					Avg Response Time: <%= Stats.stats.botChatTotalResponseTime / (Math.max(Stats.stats.botChats, 1)) %>
					Timeouts: <%= Stats.stats.botChatTimeouts %><br/>
					
					<h3>Users</h3>
					Creates: <%= Stats.stats.userCreates %>
					Connects: <%= Stats.stats.userConnects %>
					Messages: <%= Stats.stats.userMessages %><br/>
					
					<h3>Forums</h3>
					Creates: <%= Stats.stats.forumCreates %>
					Posts: <%= Stats.stats.forumPosts %>
					Views: <%= Stats.stats.forumPostViews %><br/>
					
					<h3>Live Chat</h3>
					Creates: <%= Stats.stats.chatCreates %>
					Rooms: <%= Stats.stats.chatRooms %>
					Connects: <%= Stats.stats.chatConnects %>
					Messages: <%= Stats.stats.chatMessages %><br/>
					
					<h3>Analytic</h3>
					Analytic Image Upload: <%= Stats.stats.analyticImageUpload %><br/>
					Analytic Binary Upload: <%= Stats.stats.analyticBinaryUpload %><br/>
					Analytic Test: <%= Stats.stats.analyticTest %><br/>
					Analytic Training: <%= Stats.stats.analyticTraining %><br/>
					Analytic Training Busy: <%= Stats.stats.analyticTrainingBusy %><br/>
					
					<h3>App ID Stats</h3>
					<table id="appstats" class="tablesorter">
						<thead>
						<tr>
							<th>App ID</th>
							<th>App User Id</th>
							<th>API Calls</th>
							<th>Over Limit</th>
							<th>User Creates</th>
							<th>Bot Creates</th>
						</tr>
						</thead>
						<tbody>
						<% for (AppIDStats stat : AppIDStats.sortedStats()) { %>
							<tr>
								<td><a href="super?appstats=<%= stat.appID %>"><%= Utils.sanitize(stat.appID) %></a></td>
								<td><a href="login?view-user=<%= stat.userId %>"><%= Utils.sanitize(stat.userId) %></a></td>
								<td><%= stat.apiCalls %></td>
								<td><%= stat.overLimit %></td>
								<td><%= stat.userCreates %></td>
								<td><%= stat.botCreates %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<h3>Bot Stats</h3>
					<table id="botstats" class="tablesorter">
						<thead>
						<tr>
							<th>ID</th>
							<th>Name</th>
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
						<% for (BotStats stat : BotStats.sortedStats()) { %>
							<tr>
								<td><a href="super?botstats=<%= stat.botId %>"><%= stat.botId %></a></td>
								<td><a href="browse?id=<%= stat.botId %>"><%= Utils.sanitize(stat.botName) %></a></td>
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
					<h3>Live Chat Stats</h3>
					<table id="livechatstats" class="tablesorter">
						<thead>
						<tr>
							<th>ID</th>
							<th>Name</th>
							<th>Connects</th>
							<th>Messages</th>
						</tr>
						</thead>
						<tbody>
						<% for (LiveChatStats stat : LiveChatStats.sortedStats()) { %>
							<tr>
								<td><a href="super?livechatstats=<%= stat.channelId %>"><%= stat.channelId %></a></td>
								<td><a href="livechat?id=<%= stat.channelId %>"><%= Utils.sanitize(stat.channelName) %></a></td>
								<td><%= stat.connects %></td>
								<td><%= stat.messages %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<h3>IP Stats</h3>
					<table id="ipstats" class="tablesorter">
						<thead>
						<tr>
							<th>IP</th>
							<th>Sessions</th>
							<th>Pages</th>
							<th>API Calls</th>
							<th>Bad API Calls</th>
							<th>Bot Creates</th>
							<th>Bot Connects</th>
							<th>Bot Chats</th>
							<th>User Messages</th>
							<th>Agent</th>
						</tr>
						</thead>
						<tbody>
						<% for (IPStats stat : IPStats.sortedStats()) { %>
							<tr>
								<td><a href="super?ipstats=<%= stat.ip %>"><%= Utils.sanitize(stat.ip) %></a></td>
								<td><%= stat.sessions %></td>
								<td><%= stat.pages %></td>
								<td><%= stat.api %></td>
								<td><%= stat.badAPI %></td>
								<td><%= stat.botCreates %></td>
								<td><%= stat.botConnects %></td>
								<td><%= stat.botChats %></td>
								<td><%= stat.userMessages %></td>
								<td><a href="super?agentstats=<%= Utils.encodeURL(Utils.sanitize(stat.agent)) %>"><%= Utils.sanitize(stat.agent) %></a></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<h3>Page Stats</h3>
					<table id="pagestats" class="tablesorter">
						<thead>
						<tr>
							<th>Page</th>
							<th>Hits</th>
						</tr>
						</thead>
						<tbody>
						<% for (PageStats stat : PageStats.sortedStats()) { %>
							<tr>
								<td><a href="super?pagestats=<%= Utils.sanitize(stat.page) %>"><%= Utils.sanitize(stat.page) %></a></td>
								<td><%= stat.hits %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<h3>Referrer Stats</h3>
					<table id="referstats" class="tablesorter">
						<thead>
						<tr>
							<th>Page</th>
							<th>Refers</th>
						</tr>
						</thead>
						<tbody>
						<% for (ReferrerStats stat : ReferrerStats.sortedStats()) { %>
							<tr>
								<td><a href="super?referstats=<%= Utils.sanitize(stat.page) %>"><%= Utils.sanitize(stat.page) %></a></td>
								<td><%= stat.refers %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<h3>Error Stats</h3>
					<table id="errorstats" class="tablesorter">
						<thead>
						<tr>
							<th>Error Message</th>
							<th>Errors</th>
						</tr>
						</thead>
						<tbody>
						<% for (ErrorStats stat : ErrorStats.sortedStats()) { %>
							<tr>
								<td><a href="super?errorstats=<%= Utils.sanitize(stat.message) %>"><%= Utils.sanitize(stat.message) %></a></td>
								<td><%= stat.errors %></td>
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