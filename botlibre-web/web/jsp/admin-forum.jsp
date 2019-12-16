<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.admin.ClientType"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean bean = loginBean.getBean(ForumBean.class);
	String title = "Forum";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the forum properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, forum"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="forum-banner.jsp"/>
	<jsp:include page="admin-forum-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-forum-banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1><img class="title" src="images/admin.svg"> <%= loginBean.translate("Admin Console") %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } %>
		<p>
			<a href="forum-users.jsp"><img src="images/user1.png" class="admin-pic"></a> <a href="forum-users.jsp"><%= loginBean.translate("Users") %></a> - <span><%= loginBean.translate("Configure users, and administrators of the forum.") %></span><br/>
			<a href="forum-bot.jsp"><img src="images/avatar1.png" class="admin-pic"></a> <a href="forum-bot.jsp"><%= loginBean.translate("Bot") %></a> - <span><%= loginBean.translate("Configure an automated forum bot to service the forum.") %></span><br/>
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