<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.web.admin.Category"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>
<%@page import="org.botlibre.web.issuetracker.IssueTracker"%>
<%@page import="org.botlibre.web.bean.BrowseBean.DisplayOption"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Issue Trackers - <%= Site.NAME %></title>
	<meta name="description" content="Browse the issue tracker directory"/>	
	<meta name="keywords" content="browse, directory, issue tracking"/>
</head>
<body>
	<% loginBean.setCategoryType("IssueTracker"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/issuetracker.png" class="admin-banner-pic"><%= loginBean.translate(" Issue Tracking") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p><%= loginBean.translate("Create an issue tracker for the web or mobile.") %></p>
					<% } else { %>
						<p><%= loginBean.translate("Log an issue with our platform, or create your own issue tracker for you own website or mobile app.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="issuetracker" method="get" class="message" style="display:inline">
						<input name="search-issuetracker" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Issue Trackers") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-issuetracker" type="submit" value="<%= loginBean.translate("New Issue Tracker") %>" title="<%= loginBean.translate("Create your own issue tracker") %>"/>
							<input name="create-issuetracker-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="<%= loginBean.translate("Add a link to an external forum or website to the issue tracker directory") %>"/>
						<% } %>
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