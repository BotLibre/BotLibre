<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.IssueBean"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>
<%@page import="org.botlibre.web.issuetracker.Issue"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class);
	IssueBean issueBean = loginBean.getBean(IssueBean.class);
	issueBean.setIssueTrackerBean(bean);
%>

<div id="admin-topper" align="left" style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<div class="clearfix">
		<% if (issueBean.getInstance() != null) { %>
			<a href=<%= "issuetracker?id=" + issueBean.getInstance().getTracker().getId() + proxy.proxyString() %>><%= issueBean.getInstance().getTracker().getName() %></a>
		<% } else if (bean.getInstance() != null) { %>
			<a href=<%= "issuetracker?id=" + bean.getInstance().getId() + proxy.proxyString() %>><%= bean.getInstance().getName() %></a>
		<% } else if (proxy.getInstanceId() != null) { %>
			<a href=<%= "issuetracker?id=" + proxy.getInstanceId() + proxy.proxyString() %>>Forum</a>
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
</div>