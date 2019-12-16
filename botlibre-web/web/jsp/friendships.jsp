<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.bean.UserBean"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>

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
	<title>Friends - <%= Site.NAME %></title>
	<meta name="description" content="Add and remove users and administrators for the bot"/>	
	<meta name="keywords" content="users, administrators, access, security, bot"/>
	<link rel="stylesheet" href="scripts/jquery/jquery-ui.min.css">
	<script src="scripts/jquery/jquery.js"></script>
	<script src="scripts/jquery/jquery-ui.min.js"></script>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="instance-banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
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
	<h1>
		<span class="dropt">
			<img src="images/user1.png" class="admin-banner-pic" style="vertical-align:middle">
			<div>
				<p class="help">
					<%= loginBean.translate("Configure the users and administrators of your bot.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Friends") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p style="color:#E00000;">
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your friends.") %>
		</p>
	<% } else { %>
		<jsp:include page="user-friends.jsp"/>
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
