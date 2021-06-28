<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.BotMode"%>
<%@page import="org.botlibre.web.admin.BotInstance"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	LiveChatBean livechatBean = loginBean.getBean(LiveChatBean.class);
	String title = "Live Chat";
	if (livechatBean.getInstance() != null) {
		title = livechatBean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Settings - <%= Site.NAME %></title>
	<meta name="description" content="Configure channel settings and properties"/>	
	<meta name="keywords" content="settings, config, properties, chat, chatroom, live chat, channel"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="channel-banner.jsp"/>
	<jsp:include page="admin-channel-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-channel-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The settings tab allows you to configure your channel.") %><br/>
				</div>
				<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-livechat.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt-banner">
			<img src="images/learning.png" class="admin-banner-pic">
			<div>
				<p class="help">
					<%= loginBean.translate("The settings page lets your configure your channel including linking an automated chat bot agent to service your channel, and configuring your welcome and status messages.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Settings") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!livechatBean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<form action="livechat" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= livechatBean.instanceInput() %>
			<h3><%= loginBean.translate("Messages") %></h3>
			<%= loginBean.translate("Welcome Message") %><br/>
			<input name="welcome-message" type="text" value="<%= Utils.escapeHTML(livechatBean.getInstance().getWelcomeMessage()) %>"/><br/>
			<%= loginBean.translate("Status Message") %><br/>
			<input name="status-message" type="text" value="<%= Utils.escapeHTML(livechatBean.getInstance().getStatusMessage()) %>"/><br/>
			<h3><%= loginBean.translate("Automated Chat Bot Agent") %></h3>
			<table>
				<tr>
					<td><%= loginBean.translate("Bot") %></td>
					<td>
						<select style="width:200px" name="bot" title="Choose the bot to monitor the channel from the list of your bots">
							<option value="" <%= livechatBean.getBotCheckedString(null) %>>None</option>
							<% for (BotInstance bot : livechatBean.getBots()) {%>
								<option value="<%= bot.getId() %>" <%= livechatBean.getBotCheckedString(bot) %>><%= bot.getName() %></option>
							<% } %>
						</select>
					</td>
					<td>
						<a style="margin-left:8px" href="#" onclick="document.getElementById('create-bot').click(); return false;" title="Create a new bot"><%= loginBean.translate("Create New Bot") %></a>
						<input style="display:none" id="create-bot" name="create-bot" type="submit">
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Bot Mode") %></td>
					<td>
						<span class="dropt-banner">
							<select style="width:200px" name="bot-mode">
								<option value="AnswerAndListen" <%= livechatBean.getBotModeCheckedString(BotMode.AnswerAndListen) %>><%= loginBean.translate("Answer and Listen") %></option>
								<option value="ListenOnly" <%= livechatBean.getBotModeCheckedString(BotMode.ListenOnly) %>><%= loginBean.translate("Listen Only") %></option>
								<option value="AnswerOnly" <%= livechatBean.getBotModeCheckedString(BotMode.AnswerOnly) %>><%= loginBean.translate("Answer Only") %></option>
							</select>
							<div>
								<%= loginBean.translate("The bot can be configured in three different modes, which define how the bot will participate in the conversation.") %>
								<ul>
								<li><%= loginBean.translate("Listen Only") %>: <%= loginBean.translate("the bot will not respond to questions, it will only monitor the conversation and may learn from the users, or operators if configured") %>.
								<li><%= loginBean.translate("Answer Only") %>: <%= loginBean.translate("the bot will only answer questions directed to it") %>.
								<li><%= loginBean.translate("Answer and Listen") %>: <%= loginBean.translate("the bot will answer questions, and monitor the conversation") %>.
								</ul>
							</div>
						</span>
					</td>
					<td></td>
				</tr>
			</table>

			<h3><%= loginBean.translate("Email") %></h3>
			<%= loginBean.translate("Email Address") %><br/>
			<input type="email" name="emailAddress" value="<%= livechatBean.getInstance().getEmailAddress() %>" /><br/>
			<%= loginBean.translate("User") %><br/>
			<input type="text" name="emailUserName" value="<%= livechatBean.getInstance().getEmailUserName() %>" /><br/>
			<%= loginBean.translate("Password") %><br/>
			<input type="password" name="emailPassword" value="<%= livechatBean.getInstance().getEmailPassword() %>" /><br/>
			<%= loginBean.translate("Protocol") %><br/>
			<input type="text" name="emailProtocol" value="<%= livechatBean.getInstance().getEmailProtocol() %>" /><br/>
			<input type="checkbox" name="emailSSL" <% if (livechatBean.getInstance().getEmailSSL()) { %>checked<% } %>/>SSL<br/>
			<%= loginBean.translate("Incoming Host") %><br/>
			<input type="text" name="emailIncomingHost" value="<%= livechatBean.getInstance().getEmailIncomingHost() %>" /><br/>
			<%= loginBean.translate("Incoming Port") %><br/>
			<input type="number" name="emailIncomingPort" value="<%= livechatBean.getInstance().getEmailIncomingPort() %>" /><br/>
			<%= loginBean.translate("Outgoing Host") %><br/>
			<input type="text" name="emailOutgoingHost" value="<%= livechatBean.getInstance().getEmailOutgoingHost() %>" /><br/>
			<%= loginBean.translate("Outgoing Port") %><br/>
			<input type="number" name="emailOutgoingPort" value="<%= livechatBean.getInstance().getEmailOutgoingPort() %>" /><br/>
			<%= loginBean.translate("Email Chat Log Topic") %><br/>
			<input type="text" name="emailTopic" value="<%= livechatBean.getInstance().getEmailTopic() %>" /><br/>
			<%= loginBean.translate("Email Chat Log Body") %><br/>
			<input type="text" name="emailBody" value="<%= livechatBean.getInstance().getEmailBody() %>" /><br/>
	
			<a href="#" onclick="document.getElementById('test-email').click(); return false;" title="Send a test email"><%= loginBean.translate("Send test email") %></a><br/>
			<input style="display:none" id="test-email" name="test-email" type="submit">			
			<input type="text" name="testEmailAddress" placeholder="email address" value="<%= livechatBean.getInstance().getEmailAddress() %>" /><br/>

			<input name="save-bot" type="submit" value="<%= loginBean.translate("Save") %>"/><br/>
		</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>
