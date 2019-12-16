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
	<title>Message - <%= Site.NAME %></title>
	<meta name="description" content="Create a direct message reply"/>	
	<meta name="keywords" content="create, message, user, reply"/>
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
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<a href="<%= "login?browse-user-messages=true" + proxy.proxyString() %>"><%= loginBean.translate("Messages") %></a>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="about">
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new message") %>.
		</p>
	<% } else if (loginBean.getUserMessage() == null) { %>
		<p>
			<%= loginBean.translate("No message selected.") %>
		</p>
	<% } else { %>
		<table>
			<tr>
				<td align="left" valign="top">
					<a href="<%= "login?view-user=" + loginBean.encodeURI(loginBean.getUserMessage().getCreatorId()) + proxy.proxyString() %>">
					<img src="<%= loginBean.getAvatarThumb(loginBean.getUserMessage().getCreator()) %>" height="50"/></a>
				</td>
				<td style="width:100%" align="left" valign="top">
					<h1><%= loginBean.getUserMessage().getSubject() %></h1>
				</td>
			</tr>
		</table>
		<table>
			<tr>
				<td colspan="2">
					<span class="menu" style="font-weight:bold">
						from <a class="user" href="<%= "login?view-user=" + loginBean.encodeURI(loginBean.getUserMessage().getCreatorId()) + proxy.proxyString() %>">
						<%= loginBean.getUserMessage().getCreatorId() %></a> 
						sent <%= Utils.displayTimestamp(loginBean.getUserMessage().getCreationDate())%>
					</span>
					<p>
						<%= loginBean.getUserMessage().getMessageText() %>
					</p>
				</td>
			</tr>
		</table>
		<form action="login" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<textarea autofocus style="height:300px" id="message" name="message" ></textarea><br/>
			<input name="create-user-message-reply" type="submit" value="<%= loginBean.translate("Reply") %>"/>
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