<%@page import="org.botlibre.web.bean.UserMessageBean"%>
<%@page import="org.botlibre.web.bean.UserToUserMessageBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.admin.UserMessage"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	UserMessageBean bean = loginBean.getBean(UserMessageBean.class);
	UserToUserMessageBean messageBean = loginBean.getBean(UserToUserMessageBean.class);
	messageBean.setMessagePage(0);
	messageBean.setFilter("");
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>User Messages <%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Check your direct messages and reply") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("messages, user, inbox") %>"/>
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
	<% if (loginBean.getUser() != null) { %>
		<div id="admin-topper" align="left">
			<div class="clearfix">
				<a href="<%= "login?view-user=" + loginBean.getUserId() %>"><%= loginBean.getUserId() %></a>
			</div>
		</div>
	<% } %>
	<jsp:include page="admin-user-banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="about">
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p style="color:#E00000;">
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your messages") %>.
		</p>
	<% } else { %>
		<form action="login" method="post" class="message">
			<%= proxy.proxyInput() %>
			<input name="create-user-message" type="submit" value="New Message"/>
		</form>
		<h3><%= loginBean.translate("Messages") %></h3>
		<%= bean.searchFormHTML() %>
		<%= bean.searchHTML(proxy) %>
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
