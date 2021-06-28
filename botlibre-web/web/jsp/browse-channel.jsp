<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% LiveChatBean bean = loginBean.getBean(LiveChatBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Live Chat - <%= Site.NAME %></title>
	<meta name="description" content="Browse the live chat channel and chatroom directory"/>	
	<meta name="keywords" content="browse, directory, channels, live chat, chatrooms, chat"/>
</head>
<body>
	<% loginBean.setCategoryType("Channel"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/chat.png" class="admin-banner-pic"><%= loginBean.translate(" Live Chat") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p><%= loginBean.translate("Live chat lets you create a live chat channel or chatroom for a website or mobile app.") %></p>
					<% } else { %>
						<p><%= loginBean.translate("Add live chat or a chatroom to your own website or mobile app.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="livechat" method="get" class="message" style="display:inline">
						<input name="search-channel" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Channels") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-channel" type="submit" value="<%= loginBean.translate("New Channel") %>" title="Create your own live chat channel or chat room"/>
							<input name="create-channel-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="Add a link to an external live chat channel or chat room or website to the chat directory"/>
						<% } %>
						<br/>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
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