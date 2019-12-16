<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.GraphicBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% GraphicBean bean = loginBean.getBean(GraphicBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Graphics - <%= Site.NAME %></title>
	<meta name="description" content="Browse the graphics directory"/>	
	<meta name="keywords" content="browse, directory, graphics, images, videos, audio, media"/>
</head>
<body>
	<% loginBean.setCategoryType("Graphic"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/graphic.png" class="admin-banner-pic" style="vertical-align:middle"><%= loginBean.translate(" Graphics") %></h1>
					<% if (domainBean.hasValidInstance() || Site.COMMERCIAL) { %>
						<p><%= loginBean.translate("Share graphic files for the web or mobile.") %></p>
					<% } else { %>
						<p><%= loginBean.translate("Browse our graphic directory or share your own graphics.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="graphic" method="get" class="message" style="display:inline">
						<input name="search-graphic" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Graphics") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-graphic" type="submit" value="<%= loginBean.translate("New Graphic") %>" title="Create a new graphic"/>
							<input name="create-graphic-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="Add a link to an external graphic or website to the graphics directory"/>
							<% if (loginBean.isLoggedIn()) { %>
								<input name="import-graphic" type="submit" value="<%= loginBean.translate("Import") %>" onclick="document.getElementById('upload-file').click(); return false;" title="Import a graphic export file"/>
							<% } %>
						<% } %>
						<br/>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
					<% } %>
					<% if (loginBean.isLoggedIn()) { %>
						<form action="graphic-import" method="post" enctype="multipart/form-data" style="display:inline">
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
