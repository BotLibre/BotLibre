<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>

<%@page contentType="text/html; charset=UTF-8" %>

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
	<title><%= title %> Users - <%= Site.NAME %></title>
	<meta name="description" content="Add and remove users and administrators for the forum"/>	
	<meta name="keywords" content="users, administrators, access, security, forum"/>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
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
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The users tab allows you to add users, and administrators to your forum.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-users.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt-banner">
			<img src="images/user1.png" class="admin-banner-pic" style="vertical-align:middle">
			<div>
				<p class="help">
					<%= loginBean.translate("Configure the users and administrators of your forum.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Users") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<jsp:include page="users-admin.jsp"/>
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
