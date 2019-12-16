<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.AvatarBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% AvatarBean bean = loginBean.getBean(AvatarBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Browse Avatars") %> - <%= Site.NAME %></title>
</head>
<body>
	<% loginBean.setCategoryType("Avatar"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/avatar.png" class="admin-banner-pic"> <%= loginBean.translate("Avatars") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p>
							<%= loginBean.translate("Avatars can be used by a bot, or embeded with speech directly on a website.") %><br/>
							<a target="_blank" href="browse?browse-type=Avatar&domain=1"><%= loginBean.translate("Click here to browse the avatar directory.") %></a>
						</p>
					<% } else { %>
						<p><%= loginBean.translate("Add an animated avatar and speech to your own website, or create your own avatar for your bot.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="avatar" method="get" class="message" style="display:inline">
						<input name="search-avatar" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Avatars") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-avatar" type="submit" value="<%= loginBean.translate("New Avatar") %>" title="Create a new avatar"/>
							<input name="create-avatar-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="Add a link to an external avatar or website to the avatar directory"/>
							<% if (loginBean.isLoggedIn()) { %>
								<input name="import-avatar" type="submit" value="<%= loginBean.translate("Import") %>" onclick="document.getElementById('upload-file').click(); return false;" title="Import an avatar export file"/>
							<% } %>
						<% } %>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
					<% } %>
					<% if (loginBean.isLoggedIn()) { %>
						<form action="avatar-import" method="post" enctype="multipart/form-data" style="display:inline">
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
