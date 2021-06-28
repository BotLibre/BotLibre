<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ScriptBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% ScriptBean bean = loginBean.getBean(ScriptBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Scripts - <%= Site.NAME %></title>
	<meta name="description" content="Browse the script directory"/>	
	<meta name="keywords" content="browse, directory, scripts, files, code"/>
</head>
<body>
	<% loginBean.setCategoryType("Script"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/script.png" class="admin-banner-pic"><%= loginBean.translate(" Scripts") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p>
							<%= loginBean.translate("Share and version script files.") %><br/>
							<a target="_blank" href="browse?browse-type=Script&domain=1"><%= loginBean.translate("Click here to browse the script library.") %></a>
						</p>
					<% } else { %>
						<p><%= loginBean.translate("Browse our open script library, or share your own scripts.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="script" method="get" class="message" style="display:inline">
						<input name="search-script" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Scripts") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-script" type="submit" value="<%= loginBean.translate("New Script") %>" title="Create a new script"/>
							<input name="create-script-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="Add a link to an external script or website to the script directory"/>
							<% if (loginBean.isLoggedIn()) { %>
								<input name="import-script" type="submit" value="<%= loginBean.translate("Import") %>" onclick="document.getElementById('upload-file').click(); return false;" title="Import a script export file"/>
							<% } %>
						<% } %>
						<br/>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
					<% } %>
					<% if (loginBean.isLoggedIn()) { %>
						<form action="script-meta-import" method="post" enctype="multipart/form-data" style="display:inline">
							<%= loginBean.postTokenInput() %>
							<input id="upload-file" class="hidden" onchange="this.form.submit()" type="file" name="file" multiple/>
						</form>
					<% } %>
					<br/>
					<%= bean.browseHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>