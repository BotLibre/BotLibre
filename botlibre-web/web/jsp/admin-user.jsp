<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.UserBean"%>
<%@page import="org.botlibre.web.admin.ClientType"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	UserBean userBean = loginBean.getBean(UserBean.class);
	String title = "Admin User";
	if (userBean.getUser() != null) {
		title = userBean.getUser().getUserId();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the avatar properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, avatar"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="avatar-banner.jsp"/>
	<jsp:include page="admin-user-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<div class="clearfix">
				<a href="<%= "login?view-user=" + loginBean.getUserId() %>"><%= loginBean.getUserId() %></a>
			</div>
		</div>
	</div>
	<jsp:include page="admin-user-banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1><img class="title" src="images/admin.svg"> <%= loginBean.translate("User Administration") %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p style="color:#E00000;">
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your avatar") %>.
		</p>
	<% } else { %>
		<p>
			<a href="browse-user-message.jsp"><img src="images/round_message.svg" class="admin-pic"></a>
			<a href="browse-user-message.jsp"><%= loginBean.translate("Messages") %></a> - <span><%= loginBean.translate("View user messages.") %></span><br/>
			<a href="friendships.jsp"><img src="images/friends.svg" class="admin-pic"></a>
			<a href="friendships.jsp"><%= loginBean.translate("Friends") %></a> - <span><%= loginBean.translate("View user friends and followers.") %></span><br/>
			<a href="user-avatar.jsp"><img src="images/round_avatar.svg" class="admin-pic"></a>
			<a href="user-avatar.jsp"><%= loginBean.translate("Avatar") %></a> - <span><%= loginBean.translate("Configure user avatar appearance. Choose an animated avatar, or create your own.") %></span><br/>
			<a href="user-voice.jsp"><img src="images/round_voice.svg" class="admin-pic"></a>
			<a href="user-voice.jsp"><%= loginBean.translate("Voice") %></a> - <span><%= loginBean.translate("Configure user avatar language and voice.") %></span><br/>
			<a href="user-stats.jsp"><img src="images/stats-pic.svg" class="admin-pic"></a>
			<a href="user-stats.jsp"><%= loginBean.translate("Analytics") %></a> - <span><%= loginBean.translate("View user analytics.") %></span><br/>
		</p>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>