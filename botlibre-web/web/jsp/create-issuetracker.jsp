<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Issue Tracker - <%= Site.NAME %></title>
	<meta name="description" content="Create a new issue tracker"/>	
	<meta name="keywords" content="create, issue tracking"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new issue tracker") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new issue tracker") %>.
				</p>
			<% } else { %>
				<form action="issuetracker" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Issue Tracker Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" /><br/>
					
					<% bean.writeCreateCommonHTML(error, false, null, false, out); %>					
					<tr>
					<td><%= loginBean.translate("Create Mode") %></td>
					<td><select name="createAccessMode" title="Define who can create issues in this issue tracker">
						<option value="Everyone" <%= (!error) ? "selected" : bean.isCreateAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= (!error) ? "" : bean.isCreateAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= (!error) ? "" : bean.isCreateAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= (!error) ? "" : bean.isCreateAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
					</tr>
					<tr>
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