<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Unsubscribe<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="section">
<% } %>
	<% boolean error = loginBean.getError() != null; %>
	<jsp:include page="error.jsp"/>
	<% if (!error) { %>
		<h1><%= loginBean.translate("Unsubscribe") %></h1>
		<p>
		  <%= loginBean.translate("You have been successfully unsubscribed.") %>
		</p>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
<% } %>
<% proxy.clear(); %>
</body>
</html>