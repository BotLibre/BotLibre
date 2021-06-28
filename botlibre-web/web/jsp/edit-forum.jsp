<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean bean = loginBean.getBean(ForumBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Forum<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="forum-banner.jsp"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="section">
<% } %>
	<h1><%= bean.getInstanceName() %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a forum") %>.
		</p>
	<% } else if (bean.getEditInstance() == null) { %>
		<p><%= loginBean.translate("No forum selected.") %></p>
	<% } else if (!(bean.isAdmin() || bean.isSuper())) { %>
		<p><%= loginBean.translate("Must be forum admin.") %></p>
	<% } else { %>
		<form action="forum" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			
			<% bean.writeEditNameHTML(null, false, out); %>
			<% bean.writeEditCommonHTML(null, false, out); %>
			<% if (!bean.getEditInstance().isExternal()) { %>
				<tr>
					<td><%= loginBean.translate("Post Mode") %></td>
					<td><select name="postAccessMode" title="<%= loginBean.translate("Define who can post to this forum") %>">
						<option value="Everyone" <%= bean.isPostAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= bean.isPostAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= bean.isPostAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= bean.isPostAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Post Reply Mode") %></td>
					<td><select name="replyAccessMode" title="<%= loginBean.translate("Define who can reply to posts in this forum") %>">
						<option value="Everyone" <%= bean.isReplyAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= bean.isReplyAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= bean.isReplyAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= bean.isReplyAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
				</tr>
				</table>
			<% } %>

			<% bean.writEditAdCodeHTML(null, false, out); %>
		  	
			<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
		</form>
		<% bean.writePublishDialogHTML(proxy, out); %>
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