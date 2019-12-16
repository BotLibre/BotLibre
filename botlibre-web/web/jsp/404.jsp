<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<%
	String urllink = Site.URLLINK;
	if (request.isSecure()) {
		urllink = Site.SECUREURLLINK;
	}
	if (loginBean.isTranslationRequired()) {
		urllink = "";
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Missing Page - <%= loginBean.isEmbedded() ? "" : Site.NAME %></title>
	<link rel="stylesheet" href="/botlibre/css/style.css" type="text/css">
</head>
<body>
	<% if (!loginBean.isEmbedded()) { %>
		<div id="header">
			<div class="clearfix">
				<a href="<%= urllink %>/index.jsp"><img style="cursor: pointer;float:left" src="images/banner.png" height="50"/></a>
			</div>
		</div>
		<div id="mainbody">
			<div id="contents">
				<div class="about">
					<h1><%= loginBean.translate("Missing Page") %></h1>
					<%= loginBean.translate("Sorry the URL you have entered is not correct, or has moved.") %><br/>
					<%= loginBean.translate("Please search our site for your page and update your link.") %>
					<p/>
				</div>
			</div>
		</div>
	<% } else { %>
			<h1><%= loginBean.translate("Missing Page") %></h1>
			<%= loginBean.translate("Sorry the URL you have entered is not correct, or has moved.") %><br/>	
	<% } %>
</body>
</html>