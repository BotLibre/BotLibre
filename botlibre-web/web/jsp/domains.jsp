<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.web.admin.Category"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BrowseBean.DisplayOption"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<%
	if (loginBean.checkEmbed(request, response)) {
		return;
	}
	DomainBean bean = loginBean.getBean(DomainBean.class);
	loginBean.setActiveBean(bean);
	if (loginBean.getDomainEmbedded()) {
		response.sendRedirect(Site.URLLINK);
		return;
	}
	bean.setInstance(null);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Workspaces - <%= Site.NAME %></title>
	<meta name="description" content="Browse the workspace directory"/>	
	<meta name="keywords" content="browse, directory, workspaces, domains"/>
</head>
<body>
	<% loginBean.setCategoryType("Domain"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<h1><img src="images/domain.png" class="admin-banner-pic" style="vertical-align:middle"><%= loginBean.translate(" Workspaces") %></h1>
				<p><%= loginBean.translate("Browse public workspaces, or create your own private or shared workspace") %></p>
				<%= bean.browseCategoriesHTML() %>
				<% if (bean.getCategory() == null) { %>
					<%= bean.browseFeaturedHTML() %>
				<% } %>
				<form action="domain" method="get" class="message" style="display:inline">
					<input name="search-domain" type="submit" value="<%= loginBean.translate("Search") %>"/>
					<% if (loginBean.isLoggedIn()) { %>
						<input name="my-instances" type="submit" value="<%= loginBean.translate("My Workspaces") %>"/>
					<% } %>
					<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
						<input name="create-domain" type="submit" value="<%= loginBean.translate("New Workspace") %>" title="Create your own workspace"/>
					<% } %>
					<br/>
				</form>
				<br/>
				<%= bean.browseHTML() %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>