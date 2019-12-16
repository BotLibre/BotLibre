<%@page import="org.botlibre.web.admin.BotMode"%>
<%@page import="org.botlibre.web.admin.BotInstance"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean bean = loginBean.getBean(ForumBean.class);
	String title = "Forum";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Bot - <%= Site.NAME %></title>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="forum-banner.jsp"/>
	<jsp:include page="admin-forum-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-forum-banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1> 
		<span class="dropt-banner">
			<img id="help-icon" src="images/help.svg"/>
			<div>
				<p class="help"><%= loginBean.translate("The bot tab allows you to configure an automated forum bot agent to service your forum.") %></p>
			</div>
		</span> <%= loginBean.translate("Bot") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<form action="forum" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<%= loginBean.translate("Select automated chat bot agent") %>
			<select name="bot" title="<%= loginBean.translate("Choose the bot to monitor the forum from the list of your bots") %>">
			  	<option value="" <%= bean.getBotCheckedString(null) %>><%= loginBean.translate("None") %></option>
		  		<% for (BotInstance bot : bean.getBots()) {%>
				  	<option value="<%= bot.getId() %>" <%= bean.getBotCheckedString(bot) %>><%= bot.getName() %></option>
				<% } %>
			</select><br/>
			<span class="dropt-banner">
				<%= loginBean.translate("Select bot mode") %>
				<select name="bot-mode">
				  	<option value="AnswerAndListen" <%= bean.getBotModeCheckedString(BotMode.AnswerAndListen) %>><%= loginBean.translate("Answer and Listen") %></option>
				  	<option value="ListenOnly" <%= bean.getBotModeCheckedString(BotMode.ListenOnly) %>><%= loginBean.translate("Listen Only") %></option>
				  	<option value="AnswerOnly" <%= bean.getBotModeCheckedString(BotMode.AnswerOnly) %>><%= loginBean.translate("Answer Only") %></option>
				</select><br/>
				<div>
					<%= loginBean.translate("The bot can be configured in three different modes, which define how the bot will participate in the forum.") %>
					<ul>
					<li><%= loginBean.translate("Listen Only: the bot will not respond to questions, it will only monitor the forum, and learn from the users replies to each other.") %>
					<li><%= loginBean.translate("Answer Only: the bot will only process questions directed to it.") %>
					<li><%= loginBean.translate("Answer and Listen: the bot will answer questions, monitor the forum, and learn from the users replies to each other.") %>
					</ul>
				 </div>
			 </span>
			<input name="save-bot" type="submit" value="<%= loginBean.translate("Save") %>"/>
			<input name="create-bot" type="submit" value="<%= loginBean.translate("Create New Bot") %>" title="<%= loginBean.translate("Create a new bot") %>"><br/>
	    </form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>
