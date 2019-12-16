<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

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
	<meta NAME="ROBOTS" CONTENT="NOINDEX, NOFOLLOW">
	<title>Create Message <%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Create a direct message"/>	
	<meta name="keywords" content="create, message, user"/>
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
	<div class="about">
<% } %>
	<h1><%= loginBean.translate("Create new message") %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new message") %>.
		</p>
	<% } else { %>
		<form action="login" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<% boolean hasMessage = loginBean.getUserMessage() != null; %>
			<span class="required">To</span><br/>
			<input name="token" type="hidden" value="<%= loginBean.hashCode() %>"/>
			<input class="required" autofocus name="target" type="text" value="<%= (!hasMessage) ? "" : Utils.sanitize(loginBean.getUserMessage().getTargetId()) %>" /><br/>
			<span class="required">Subject</span><br/>
			<input class="required" autofocus name="subject" type="text" value="<%= (!hasMessage) ? "" : Utils.sanitize(loginBean.getUserMessage().getSubject()) %>" /><br/>
			<%= loginBean.translate("Message") %><br/>
			<textarea style="height:350px" name="message" ><%= (!hasMessage) ? "" : loginBean.getUserMessage().getMessage() %></textarea><br/>
			<input id="ok" name="create-new-user-message" type="submit" value="<%= loginBean.translate("Send") %>"/>
			<input id="cancel" name="cancel-user-message" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
		</form>
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