<%@page import="org.botlibre.web.bean.UserBean.UserRestrict"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.sessions.factories.SessionManager"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.service.BeanManager"%>
<%@page import="org.botlibre.web.service.InstanceManager"%>
<%@page import="org.botlibre.web.service.BotManager"%>
<%@page import="org.botlibre.Bot"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>

<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.botlibre.web.admin.ClientType"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.bean.LoginBean"%>  	
<%@page import="org.botlibre.web.bean.UserBean"%>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% UserBean userBean = loginBean.getBean(UserBean.class); %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Browse Users") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Browse the user directory") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("browse, search, users, directory") %>"/>
	<% if (userBean.getResultsSize() == 0) { %>
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
					<h3><%= loginBean.translate("Search Public User Profiles") %></h3>
					<%= userBean.searchFormUserHTML() %>
					<%= userBean.searchHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>