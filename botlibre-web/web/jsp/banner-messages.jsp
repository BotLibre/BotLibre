<%@page import="org.botlibre.web.Site"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
%>

<div id="admin-topper" align="left" style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% if (loginBean.getUser() != null) { %>
		<a href=<%= "login?view-user=" + loginBean.getUserId() + proxy.proxyString() %>><%= loginBean.getUserId() %></a>
	<% } %>
	<span style="float:right">
		<% if (!loginBean.isLoggedIn()) { %>
			<a href="<%= "login?sign-in" + proxy.proxyString() %>"><%= loginBean.translate("Sign In") %></a>
			<% if (Site.ALLOW_SIGNUP) { %>
				 : <a href="<%= "login?sign-up" + proxy.proxyString() %>"><%= loginBean.translate("Sign Up") %></a>
			<% } %>
		<% } else { %>
			<a href="<%= "login?sign-in" + proxy.proxyString() %>"><%= loginBean.getUser().getUserId() %></a> :
			<a href="#" onclick="document.getElementById('logout').click()"><%= loginBean.translate("Sign Out") %></a>
			<form action="login" style="display:none">
				<%= proxy.proxyInput() %>
				<input type="submit" id="logout" name="logout"/>
			</form>
		<% } %>
	</span>
</div>