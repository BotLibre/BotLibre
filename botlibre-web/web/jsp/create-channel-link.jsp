<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% LiveChatBean bean = loginBean.getBean(LiveChatBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Live Chat Channel Link - <%= Site.NAME %></title>
	<meta name="description" content="Create a link to an external chatroom or chat website"/>	
	<meta name="keywords" content="create, link, website, chat, chatroom"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new live chat channel link") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new channel link") %>.
				</p>
			<% } else { %>
				<form action="livechat" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Channel Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" /><br/>
					
					<%= loginBean.translate("Channel Type") %>
					<select id="type" name="type" title="The channel can either be a chat room that allows multiple users to chat with each other, or a one on one channel where users are queued to chat with an administrator.">
						<option value="ChatRoom" <%= bean.isTypeSelected("ChatRoom") %>><%= loginBean.translate("Chat Room") %></option>
						<option value="OneOnOne" <%= bean.isTypeSelected("OneOnOne") %>><%= loginBean.translate("One On One") %></option>
					</select><br/>

					<% bean.writeCreateCommonHTML(error, true, null, false, out); %>
					
					<input id="ok" name="create-link" type="submit" value="<%= loginBean.translate("Create") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>