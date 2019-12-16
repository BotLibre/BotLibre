<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.AvatarBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% AvatarBean bean = loginBean.getBean(AvatarBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Avatar - <%= Site.NAME %></title>
	<meta name="description" content="Create a new avatar"/>	
	<meta name="keywords" content="create, avatar"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new avatar") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<% 
				if (bean.getForking()) {
					error = true;
					bean.setForking(false);
				}
			%>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new avatar") %>.
				</p>
			<% } else { %>
				<form action="avatar" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Avatar Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" /><br/>
					
					<% bean.writeCreateCommonHTML(error, false, null, false, out); %>
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