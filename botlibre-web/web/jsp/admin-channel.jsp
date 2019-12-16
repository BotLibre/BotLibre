<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.admin.ClientType"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@ page contentType="text/html; charset=UTF-8" %>

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
	<title><%= title %> Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the channel properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, channel, chat"/>
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
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1><img class="title" src="images/admin.svg"> <%= loginBean.translate("Admin Console") %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!livechatBean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } %>
		<p>
			<a href="channel-users.jsp"><img src="images/user1.png" class="admin-pic"></a> <a href="channel-users.jsp"><%= loginBean.translate("Users") %></a> - <span><%= loginBean.translate("Configure users, operators, and administrators of the channel.") %></span><br/>
			<a href="channel-settings.jsp"><img src="images/learning.png" class="admin-pic"></a> <a href="channel-settings.jsp"><%= loginBean.translate("Settings") %></a> - <span><%= loginBean.translate("Configure settings including welcome messages, email, and an automated chat bot to service the channel.") %></span><br/>
			<a href="channel-logs.jsp"><img src="images/chatlog1.png" class="admin-pic"></a> <a href="channel-logs.jsp"><%= loginBean.translate("Chat Logs") %></a> - <span><%= loginBean.translate("View the channel chat logs.") %></span><br/>
		</p>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>