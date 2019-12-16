<%@page import="org.botlibre.web.bean.ChatBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	BotBean botBean = loginBean.getBotBean();
	ChatBean chatBean = loginBean.getBean(ChatBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= botBean.getInstanceName() %> Chat <%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
		<jsp:include page="error.jsp"/>
		<% if (embed && !Site.COMMERCIAL && loginBean.getShowLink()) { %>
			<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>;">
				<span>chat hosted by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
			</div>
		<% } %>
		<p><%= chatBean.getFarewell() %></p>
	</div>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
</body>
<% proxy.clear(); %>
</html>