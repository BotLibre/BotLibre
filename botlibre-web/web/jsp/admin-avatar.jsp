<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.AvatarBean"%>
<%@page import="org.botlibre.web.admin.ClientType"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AvatarBean bean = loginBean.getBean(AvatarBean.class);
	String title = "Avatar";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the avatar properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, avatar"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="avatar-banner.jsp"/>
	<jsp:include page="admin-avatar-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-avatar-banner.jsp"/>
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
			<a href="avatar-users.jsp"><img src="images/user1.png" class="admin-pic"></a> <a href="avatar-users.jsp"><%= loginBean.translate("Users") %></a> - <span><%= loginBean.translate("Configure users, and administrators of the avatar.") %></span><br/>
		</p>
		<p>
			<a href="avatar-editor.jsp"><img src="images/analytic-media.svg" class="admin-pic"></a> <a href="avatar-editor.jsp"><%= loginBean.translate("Avatar Editor") %></a> - <span><%= loginBean.translate("Configure, import, and upload media for the avatar.") %></span><br/>
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