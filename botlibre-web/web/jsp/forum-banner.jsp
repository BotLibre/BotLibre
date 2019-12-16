<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.ForumPostBean"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.forum.ForumPost"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean forumBean = loginBean.getBean(ForumBean.class);
	ForumPostBean forumPostBean = loginBean.getBean(ForumPostBean.class);
	forumPostBean.setForumBean(forumBean);
%>

<div id="admin-topper" align="left" style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<div class="clearfix">
		<% if (forumPostBean.getInstance() != null) { %>
			<a href=<%= "forum?id=" + forumPostBean.getInstance().getForum().getId() + proxy.proxyString() %>><%= forumPostBean.getInstance().getForum().getName() %></a>			
			<% if ((forumPostBean.getInstance() != null) && (forumPostBean.getInstance().getParent() != null)) { %>
				: <a href="<%= "forum-post?id=" + forumPostBean.getInstance().getParent().getId() + proxy.proxyString() %>"><%= forumPostBean.getInstance().getParent().getTopic() %></a>
			<% } %>
		<% } else if (forumBean.getInstance() != null) { %>
			<a href=<%= "forum?id=" + forumBean.getInstance().getId() + proxy.proxyString() %>><%= forumBean.getInstance().getName() %></a>
		<% } else if (proxy.getInstanceId() != null) { %>
			<a href=<%= "forum?id=" + proxy.getInstanceId() + proxy.proxyString() %>>Forum</a>
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