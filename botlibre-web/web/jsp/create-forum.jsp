<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% ForumBean bean = loginBean.getBean(ForumBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Forum - <%= Site.NAME %></title>
	<meta name="description" content="Create a new forum"/>	
	<meta name="keywords" content="create, forum"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new forum") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new forum") %>.
				</p>
			<% } else { %>
				<form action="forum" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Forum Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" /><br/>
					
					<% bean.writeCreateCommonHTML(error, false, null, false, out); %>
					<tr>
					<td><%= loginBean.translate("Post Mode") %></td>
					<td><select name="postAccessMode" title="Define who can post to this forum">
						<option value="Everyone" <%= (!error) ? "selected" : bean.isPostAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= (!error) ? "" : bean.isPostAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= (!error) ? "" : bean.isPostAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= (!error) ? "" : bean.isPostAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
					</tr>
					<tr>
					<td><%= loginBean.translate("Post Reply Mode") %></td>
					<td><select name="replyAccessMode" title="Define who can reply to posts in this forum">
						<option value="Everyone" <%= (!error) ? "selected" : bean.isReplyAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= (!error) ? "" : bean.isReplyAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= (!error) ? "" : bean.isReplyAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= (!error) ? "" : bean.isReplyAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
					</tr>
					</table>
					
					<% bean.writeCreateAdCodeHTML(error, null, false, out); %>
				  	
					<input id="ok" name="create-instance" type="submit" value="<%= loginBean.translate("Create") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>