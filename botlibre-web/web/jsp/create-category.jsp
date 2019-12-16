<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Category - <%= Site.NAME %></title>
	<meta name="description" content="Create a new category"/>	
	<meta name="keywords" content="create, category"/>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="section">
<% } %>
	<h1><%= loginBean.translate("New Category") %></h1>
	<% boolean error = loginBean.getError() != null; %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new category") %>.
		</p>
	<% } else { %>
		<form action="category" method="post" enctype="multipart/form-data" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= loginBean.translate("Name") %><br/>
			<input autofocus name="name" type="text" value="<%= error ? loginBean.getCategory().getName() : "" %>" /><br/>
			<%= loginBean.translate("Description") %><br/>
			<input name="description" type="text" value="<%= error ? loginBean.getCategory().getDescription() : "" %>" /><br/>
			<%= loginBean.translate("Parent Categories") %><br/>
			<input id="categories" name="parents" type="text" value="<%= error ? loginBean.getCategory().getParentsString() : loginBean.getActiveCategory() %>"
					title="Comma seperated list of parent categories" /><br/>
			<input name="secured" type="checkbox" <% if (error && loginBean.getCategory().isSecured()) { %>checked<% } %> title="You can secure the category to disallow other users from using it" >Secured (only you can use it)</input><br/>
			
			<script>
	    	$(function() {
		        var availableTags = [<%= loginBean.getAllCategoriesString() %>];
		        multiDropDown("#categories", availableTags);
		    });
			</script>
			<%= loginBean.translate("Image") %><br/>
			<input id="file" type="file" name="file"/><br/>
			<input id="ok" name="create-category" type="submit" value="<%= loginBean.translate("Create") %>"/><input id="cancel" name="cancel-category" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
		</form>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>