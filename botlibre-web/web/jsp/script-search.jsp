<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ScriptBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% ScriptBean bean = loginBean.getBean(ScriptBean.class); %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Search Scripts") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Search the script directory") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("search, directory, scripts, files") %>"/>
	<% if (bean.getResultsSize() == 0) { %>
		<meta NAME="ROBOTS" CONTENT="NOINDEX, NOFOLLOW">
	<% } %>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
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
					<h3><%= loginBean.translate("Search Scripts") %></h3>
					<%= bean.searchFormHTML() %>
					<%= bean.searchHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>