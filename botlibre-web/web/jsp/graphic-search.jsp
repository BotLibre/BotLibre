<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.GraphicBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% GraphicBean bean = loginBean.getBean(GraphicBean.class); %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Search Graphics - <%= Site.NAME %></title>
	<meta name="description" content="Search the graphics directory"/>	
	<meta name="keywords" content="search, directory, graphics, images, videos, audio, media"/>
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
					<h3 onclick="document.getElementById('search').click()" class="clickable"><img src="images/search.svg" class="admin-banner-pic" style="width:28px;padding:4px;"/>
						<%= loginBean.translate("Search Graphics") %>
					</h3>
					<%= bean.searchFormHTML() %>
					<%= bean.searchHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
