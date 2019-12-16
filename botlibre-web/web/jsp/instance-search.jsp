<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.admin.BotInstance" %>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.admin.Tag"%>
<%@page import="org.botlibre.web.bean.BrowseBean.DisplayOption"%>
<%@page import="org.botlibre.web.bean.BrowseBean.InstanceFilter" %>
<%@page import="org.botlibre.web.bean.BrowseBean.InstanceSort"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% BotBean bean = loginBean.getBotBean(); %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Search Bots - <%= Site.NAME %></title>
	<meta name="description" content="Search the open bot directory"/>	
	<meta name="keywords" content="search, directory, bots, chatbots, open"/>
	<% if (bean.getResultsSize() == 0) { %>
		<meta NAME="ROBOTS" CONTENT="NOINDEX, NOFOLLOW">
	<% } %>
	<%= loginBean.getJQueryHeader() %>
	<% boolean allowAccess = loginBean.checkDomainAccess(); %>
</head>

<body>	
	<% loginBean.setPageType(Page.Search); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (allowAccess) { %>
					<h3><%= loginBean.translate("Search Bots") %></h3>
					<%= bean.searchFormHTML() %>
					<%= bean.searchHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
