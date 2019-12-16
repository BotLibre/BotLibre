<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	BotBean bean = loginBean.getBotBean();
%>

<div id="admin-topper" align="left" style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% if (bean.getInstance() != null) { %>
		<a href="<%= "browse?id=" + bean.getInstance().getId() + proxy.proxyString() %>"><%= bean.getInstance().getName() %></a>
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