<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.ClientType"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean bean = loginBean.getBean(DomainBean.class);
	String title = "Workspace";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the workspace properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, workspace"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="domain-banner.jsp"/>
	<jsp:include page="admin-domain-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-domain-banner.jsp"/>
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
			<a href="domain-users.jsp"><img src="images/user1.png" class="admin-pic"></a> <a href="domain-users.jsp"><%= loginBean.translate("Users") %></a> - <span><%= loginBean.translate("Configure users, and administrators of the workspace.") %></span><br/>
			<a href="domain-categories.jsp"><img src="images/category1.png" class="admin-pic"></a> <a href="domain-categories.jsp"><%= loginBean.translate("Categories") %></a><span> - <%= loginBean.translate("Configure categories for the workspace.") %></span><br/>
			<a href="domain-tags.jsp"><img src="images/tag.png" class="admin-pic"></a> <a href="domain-tags.jsp"><%= loginBean.translate("Tags") %></a><span> - <%= loginBean.translate("Configure tags for the workspace.") %></span><br/>
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