<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.web.admin.Category"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.forum.Forum"%>
<%@page import="org.botlibre.web.bean.BrowseBean.DisplayOption"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% ForumBean bean = loginBean.getBean(ForumBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Forums - <%= Site.NAME %></title>
	<meta name="description" content="Browse the forum directory"/>	
	<meta name="keywords" content="browse, directory, forums"/>
</head>
<body>
	<% loginBean.setCategoryType("Forum"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/forum.png" class="admin-banner-pic"><%= loginBean.translate(" Forums") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p>
							<%= loginBean.translate("Forums lets you create your own forums for a website or mobile app.") %><br/>
							<a target="_blank" href="browse?browse-type=Forum&domain=1"><%= loginBean.translate("Click here to browse our forums.") %></a>
						</p>
					<% } else { %>
						<p><%= loginBean.translate("Browse our forums, or add your own forum to your own website or mobile app.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="forum" method="get" class="message" style="display:inline">
						<input name="search-forum" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Forums") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-forum" type="submit" value="<%= loginBean.translate("New Forum") %>" title="Create your own forum"/>
							<input name="create-forum-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="Add a link to an external forum or website to the forum directory"/>
						<% } %>
						<input name="all-posts" type="submit" value="All Posts"/>
						<br/>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
					<% } %>
					<br/>
					<%= bean.browseHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>