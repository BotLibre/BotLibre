<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean bean = loginBean.getBean(DomainBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Search Workspaces - <%= Site.NAME %></title>
	<meta name="description" content="Search the workspace"/>	
	<meta name="keywords" content="search, directory, workspaces, domains"/>
	<% if (bean.getResultsSize() == 0) { %>
		<meta NAME="ROBOTS" CONTENT="NOINDEX, NOFOLLOW">
	<% } %>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<body>
	<% loginBean.setPageType(Page.Search); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">	
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<h3><%= loginBean.translate("Search Workspaces") %></h3>
				<%= bean.searchFormHTML() %>
				<%= bean.searchHTML() %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
