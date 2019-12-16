<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.botlibre.web.bean.BotBean"%>
<%@ page import="org.botlibre.web.admin.ClientType"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page import="org.botlibre.web.bean.LoginBean.Page" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the bot properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, bot, chatbot"/>
</head>
<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="browse">
				<% if (botBean.getInstance() != null && !botBean.getInstance().isExternal()) { %>
					<% botBean.connect(ClientType.WEB, request); %>
				<% } %>
				<h1><img class="title" src="images/admin.svg"> <%= loginBean.translate("Admin Console") %></h1>
				<jsp:include page="error.jsp"/>
				<% if (botBean.getInstance() == null || (!botBean.isConnected() && !botBean.getInstance().isExternal())) { %>
					<%= botBean.getNotConnectedMessage() %>
				<% } else if (!botBean.isAdmin()) { %>
					<%= botBean.getMustBeAdminMessage() %>
				<% } %>
				<p>
				<% if (botBean.getInstance() == null || !botBean.getInstance().isExternal()) { %>
					<a href="admin-users.jsp"><img src="images/user1.png" class="admin-pic"></a> <a href="admin-users.jsp"><%= loginBean.translate("Users") %></a> - <span><%= loginBean.translate("Configure who can access, and administer your bot.") %></span><br/>
					<a href="instance-avatar.jsp"><img src="images/avatar1.png" class="admin-pic"></a> <a href="instance-avatar.jsp"><%= loginBean.translate("Avatar") %></a> - <span><%= loginBean.translate("Configure your bot's appearance. Choose an animated avatar, or create your own.") %></span><br/>
					<a href="voice.jsp"><img src="images/voice1.png" class="admin-pic"></a> <a href="voice.jsp"><%= loginBean.translate("Voice") %></a> - <span><%= loginBean.translate("Configure your bot's language and voice.") %></span><br/>
					<a href="learning.jsp"><img src="images/learning.png" class="admin-pic"></a> <a href="learning.jsp"><%= loginBean.translate("Learning & Settings") %></a> - <span><%= loginBean.translate("Configure your bot's learning ability and other settings.") %></span><br/>
					<a href="chatlogs.jsp"><img src="images/chatlog1.png" class="admin-pic"></a> <a href="chatlogs.jsp"><%= loginBean.translate("Training & Chat Logs") %></a> - <span><%= loginBean.translate("Train your bot's responses, greetings, and default responses. View your bot's conversations. Import and export chat logs, response lists, CSV, and AIML files.") %></span><br/>
					<a href="self.jsp"><img src="images/script1.png" class="admin-pic"></a> <a href="self.jsp"><%= loginBean.translate("Scripts") %></a> - <span><%= loginBean.translate("Add, create, edit, import, and export Self or AIML scripting programs.") %></span><br/>
					<a href="twitter.jsp"><img src="images/twitter1.png" class="admin-pic"></a> <a href="twitter.jsp"><%= loginBean.translate("Twitter") %></a> - <span><%= loginBean.translate("Allow your bot to manage a Twitter account and post and chat on Twitter.") %></span><br/>
					<a href="facebook.jsp"><img src="images/facebook4.png" class="admin-pic"></a> <a href="facebook.jsp"><%= loginBean.translate("Facebook") %></a> - <span><%= loginBean.translate("Allow your bot to manage a Facebook account or page and chat on Facebook Messenger.") %></span><br/>
					<a href="telegram.jsp"><img src="images/telegram1.png" class="admin-pic"></a> <a href="telegram.jsp"><%= loginBean.translate("Telegram") %></a> - <span><%= loginBean.translate("Allow bot to manage a Telegram channel, group, or chat on Telegram.") %></span><br/>
					<a href="slack.jsp"><img src="images/slack1.png" class="admin-pic"></a> <a href="slack.jsp"><%= loginBean.translate("Slack") %></a> - <span><%= loginBean.translate("Allow your bot to send, receive, and reply to Slack messages.") %></span><br/>
					<a href="skype.jsp"><img src="images/skype1.png" class="admin-pic"></a> <a href="skype.jsp"><%= loginBean.translate("Skype") %></a> - <span><%= loginBean.translate("Allow your bot to send, receive, and reply to Skype messages.") %></span><br/>
					<a href="wechat.jsp"><img src="images/wechat1.png" class="admin-pic"></a> <a href="wechat.jsp"><%= loginBean.translate("WeChat") %></a> - <span><%= loginBean.translate("Allow your bot to send, receive, and reply to WeChat messages.") %></span><br/>
					<a href="kik.jsp"><img src="images/kik.png" class="admin-pic"></a> <a href="kik.jsp"><%= loginBean.translate("Kik") %></a> - <span><%= loginBean.translate("Allow your bot to send, receive, and reply to Kik messages.") %></span><br/>
					<a href="email.jsp"><img src="images/email.png" class="admin-pic"></a> <a href="email.jsp"><%= loginBean.translate("Email") %></a> - <span><%= loginBean.translate("Allow your bot to manage an email account and send, receive, and reply to emails.") %></span><br/>
					<a href="sms.jsp"><img src="images/twilio.svg" class="admin-pic"></a> <a href="sms.jsp"><%= loginBean.translate("Twilio SMS & IVR") %></a> - <span><%= loginBean.translate("Allow your bot to send, receive, and reply to SMS messages and response to a voice phone using Interactive Voice Response (IVR).") %></span><br/>
					<a href="google.jsp" ><img src="images/google.png" class="admin-pic"></a> <a href="google.jsp" ><%= loginBean.translate("Google") %></a> - <span><%= loginBean.translate("Allow your bot to connect to Google services such as Google Calendar.") %></span><br/>
					<a href="wolframalpha.jsp" ><img src="images/wolframalpha1.png" class="admin-pic"></a> <a href="wolframalpha.jsp" ><%= loginBean.translate("Wolfram Alpha") %></a> - <span><%= loginBean.translate("Allow your bot to connect to Wolfram Alpha services.") %></span><br/>
					<a href="alexa.jsp" ><img src="images/alexa1.png" class="admin-pic"></a> <a href="alexa.jsp" ><%= loginBean.translate("Alexa") %></a> - <span><%= loginBean.translate("Allow your bot to connect to Amazon Alexa.") %></span><br/>
					<a href="google-assistant.jsp" ><img src="images/google-assistant1.png" class="admin-pic"></a> <a href="google-assistant.jsp" ><%= loginBean.translate("Google Assistant") %></a> - <span><%= loginBean.translate("Allow your bot to connect to Google Assistant.") %></span><br/>
					<a href="irc.jsp"><img src="images/irc.png" class="admin-pic"></a> <a href="irc.jsp"><%= loginBean.translate("IRC") %></a> - <span><%= loginBean.translate("Allow your bot to chat with others on an IRC chat channel.") %></span><br/>
					<a href="timers.jsp"><img src="images/timers.png" class="admin-pic"></a> <a href="timers.jsp"><%= loginBean.translate("Timers") %></a> - <span><%= loginBean.translate("Setup your bot to run scripts at various time intervals to automate web tasks.") %></span><br/>
					<a href="import.jsp"><img src="images/web.png" class="admin-pic"></a> <a href="import.jsp"><%= loginBean.translate("Web") %></a> - <span><%= loginBean.translate("Import data from the WikiData, Wiktionary, and other websites.") %></span><br/>
					<a href="memory.jsp"><img src="images/knowledge.png" class="admin-pic"></a> <a href="memory.jsp"><%= loginBean.translate("Knowledge") %></a> - <span><%= loginBean.translate("Browse your bot's knowledge database.") %></span><br/>
					<a href="log.jsp"><img src="images/log.png" class="admin-pic"></a> <a href="log.jsp"><%= loginBean.translate("Log") %></a> - <span><%= loginBean.translate("View the bot's log for errors and debugging info.") %></span><br/>
					<a href="instance-stats.jsp"><img src="images/stats.svg" class="admin-pic"></a> <a href="instance-stats.jsp"><%= loginBean.translate("Analytics") %></a> - <span><%= loginBean.translate("View the bot's statistic and graphs.") %></span>
					<% if (botBean.isConnected()) { %>
						<form action="bot" method="post" class="message">
							<%= loginBean.postTokenInput() %>
							<input id="cancel" name="disconnect" type="submit" value="<%= loginBean.translate("Disconnect") %>"/><br/>
						</form>
					<% } %>
					<h2><%= loginBean.translate("Tasks") %></h2>
					<ul>
						<li><a href="edit-instance.jsp"><%= loginBean.translate("Edit your bot's details including, name, description, and access.") %></a></li>
						<li><a href="edit-instance.jsp"><%= loginBean.translate("Change your bot's knowledge limit from its details page.") %></a></li>
						<li><a href="instance.jsp"><%= loginBean.translate("Delete your bot from its main page.") %></a></li>
						<li><a href="memory?status=true"><%= loginBean.translate("Delete the bot's entire memory, all responses, scripts, and data from its Knowledge page.") %></a></li>
						<li><a href="chatlogs.jsp"><%= loginBean.translate("Add a new response to your bot from its Training page.") %></a></li>
					</ul>
					
				<% } else { %>
					<a href="instance-avatar.jsp"><img src="images/avatar1.png" class="admin-pic"></a> <a href="instance-avatar.jsp"><%= loginBean.translate("Avatar") %></a> - <span><%= loginBean.translate("Configure your bot's appearance.Choose an animated avatar, or create your own.") %></span><br/>
					<a href="voice.jsp"><img src="images/voice1.png" class="admin-pic"></a> <a href="voice.jsp"><%= loginBean.translate("Voice") %></a> - <span><%= loginBean.translate("Configure your bot's language and voice.") %></span><br/>
				<% } %>
				</p>
				
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>