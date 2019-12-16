<%@page import="org.botlibre.web.bean.GoogleBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% GoogleBean bean = loginBean.getBean(GoogleBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Google") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Connect your bot to Google services such as Google Calendar") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("google, calendar, bot, services, api") %>"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The Google tab allows you to connect your bot to Google services such as Google Calendar.") %>
					<%= loginBean.translate("You can access Google services from Self scripts using the Google and GoogleCalendar classes.") %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-google.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %> 
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
			<img src="images/google.png" class="admin-banner-pic" style="vertical-align:middle">
			<div>
				<p class="help">
					<%= loginBean.translate("Allow your bot to connect to Google services such as Google Calendar.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Google") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The Google tab allows you to connect your bot to Google services such as Google Calendar.") %>
					<%= loginBean.translate("You can access Google services from Self scripts using the Google and GoogleCalendar classes.") %>
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					<%= loginBean.translate("The Google tab allows you to connect your bot to Google services such as Google Calendar.") %>
					<%= loginBean.translate("You can access Google services from Self scripts using the Google and GoogleCalendar classes.") %>
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else if (bean.isAuthorising()) { %>
				<h2><%= loginBean.translate("Authorizing") %></h2>
				<form action="google" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<%= loginBean.translate("Open the following URL to authorise the bot to access your Google Calendar:") %><br/>
					<a style="color:grey" target="_blank" href="<%= bean.getAuthURL() %>"><%= bean.getAuthURL() %></a><br/>
					<br/>
					<%= loginBean.translate("Enter auth code") %><br/>
					<input autofocus type="text" name="authCode" value="" /><br/>								
					<input id="ok" type="submit" name="authorise-complete" value="<%= loginBean.translate("Done") %>"/>
					<input id="cancel" type="submit" name="cancel" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please ensure your bot's Google API usage complies with Google's ") %><a target="_blank" href="https://developers.google.com/terms/"><%= loginBean.translate("terms of service") %></a>.
				</p>
				<p>
					<%= loginBean.translate("Connect your bot to") %> <a href="https://calendar.google.com/" target="_blank"><%= loginBean.translate("Google Calendar") %></a>.
				</p>
				<form action="google" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<% if (bean.isAuthorized()) { %>
						<%= loginBean.translate("This bot is connected to the Google account ") + bean.getGoogleAccountId() %><br/>
						<input id="cancel" type="submit" name="clear" value="<%= loginBean.translate("Disconnect") %>"/><br/>
					<% } else { %>
						<%= loginBean.translate("This bot is not connected to Google services") %><br/>
						<input type="submit" name="authorize" value="<%= loginBean.translate("Authorize") %>"/><br/>
					<% } %>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
