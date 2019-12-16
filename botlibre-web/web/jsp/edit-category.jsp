<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

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
	<title>Edit Category<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
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
	<h1><%= loginBean.getCategory() == null ? "" : loginBean.getCategory().getName() %></h1>
	<jsp:include page="error.jsp"/>			
	<% if (!loginBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a category") %>.
		</p>
	<% } else if (loginBean.getCategory() == null) { %>
		<p><%= loginBean.translate("No category selected.") %></p>
	<% } else if (!loginBean.isAdmin() && !loginBean.getCategory().getCreator().equals(loginBean.getUser())) { %>
		<p><%= loginBean.translate("Must be category admin.") %></p>
	<% } else { %>
	
		<form action="category" method="post" enctype="multipart/form-data" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= loginBean.translate("Category Name") %><br/>
			<input autofocus name="name" type="text" value="<%= loginBean.getCategory().getName() %>" /><br/>
			<%= loginBean.translate("Description") %><br/>
			<input autofocus name="description" type="text" value="<%= loginBean.getCategory().getDescription() %>" /><br/>
			<%= loginBean.translate("Parent Categories") %><br/>
			<input id="categories" name="parents" type="text" value="<%= loginBean.getCategory().getParentsString() %>"
					title="<%= loginBean.translate("Comma seperated list of parent categories") %>" /><br/>
			<input name="secured" type="checkbox" <% if (loginBean.getCategory().isSecured()) { %>checked<% } %> title="<%= loginBean.translate("You can secure the category to disallow other users from using it") %>" ><%= loginBean.translate("Secured (only you can use it)") %></input><br/>
			<script>
		    	$(function() {
			        var availableTags = [<%= loginBean.getAllCategoriesString() %>];
			        multiDropDown("#categories", availableTags);
			    });
			</script>
			<%= loginBean.translate("Image") %><br/>
			<input id="file" type="file" name="file"/><br/>
			<input id="ok" name="save-category" type="submit" value="<%= loginBean.translate("Save") %>"/><input id="cancel" name="cancel-category" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
		</form>
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