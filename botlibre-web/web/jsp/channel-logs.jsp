<%@page import="org.botlibre.web.chat.ChannelAttachment"%>
<%@page import="java.util.Calendar"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.chat.ChatMessage"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	try {

	boolean embed = loginBean.isEmbedded();
	LiveChatBean livechatBean = loginBean.getBean(LiveChatBean.class);
	String title = "Live Chat";
	if (livechatBean.getInstance() != null) {
		title = livechatBean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Logs - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("View chat logs for the channel") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("chat logs, logs, conversations, chat, chatroom, live chat, channel") %>"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="channel-banner.jsp"/>
	<jsp:include page="admin-channel-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-channel-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The chat logs tab allows you to monitor your channel.") %><br/>
				</div>
				<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-livechat.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt-banner">
			<img src="images/chatlog1.png" class="admin-banner-pic">
			<div>
				<p class="help">
					<%= loginBean.translate("The chat logs tab allows you to monitor your channel.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Chat Logs") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!livechatBean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<form action="livechat" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= livechatBean.instanceInput() %>
			<input name="logs" type="hidden" value="show"/>
			<span class="menu">
				<div class='search-div'>
					<span class='search-span'><%= loginBean.translate("Search") %></span>
					<select class="search" name="search" onchange="this.form.submit()">
						<option value="messages" <%= livechatBean.getSearchCheckedString("messages") %>><%= loginBean.translate("messages") %></option>
						<option value="attachments" <%= livechatBean.getSearchCheckedString("attachments") %>><%= loginBean.translate("attachments") %></option>
					</select>
				</div>
				<div class='search-div'>
					<span class='search-span'><%= loginBean.translate("Duration") %></span>
					<select class="search" name="duration" onchange="this.form.submit()">
						<option value="none" <%= livechatBean.getDurationCheckedString("none") %>></option>
						<option value="day" <%= livechatBean.getDurationCheckedString("day") %>><%= loginBean.translate("current day") %></option>
						<option value="week" <%= livechatBean.getDurationCheckedString("week") %>><%= loginBean.translate("current week") %></option>
						<option value="month" <%= livechatBean.getDurationCheckedString("month") %>><%= loginBean.translate("current month") %></option>
						<option value="all" <%= livechatBean.getDurationCheckedString("all") %>><%= loginBean.translate("all time") %></option>
					</select>
				</div>
				<div class='search-div'>
					<span class='search-span'><%= loginBean.translate("Filter") %></span>
					<input class="search" style="width:150px" id="searchtext" name="filter" type="text" value="<%= livechatBean.getFilter() %>" title="<%= loginBean.translate("Filter the results to only include messages containing the filter text") %>" /></td>
				</div>
				<input class="search" style="display:none;position:absolute;" type="submit" name="searchsubmit" value="Search">
			</span>
			<br/><br/>
			<% if (!livechatBean.getLogResults().isEmpty()) { %>
				<input id="select-icon" class="icon" type="submit" name="selectAllLogs" value="" title="<%= loginBean.translate("Select all of the messages") %>">
				<input id="remove-icon" class="icon" type="submit" name="deleteLogs" value="" title="<%= loginBean.translate("Delete the selected messages") %>">
				<br/>
				<% Calendar current, last = null; %>
				<% for (ChatMessage message : livechatBean.getLogResults()) { %>
					<% if (last == null) { %>
						<% last = Calendar.getInstance(); last.setTime(message.getCreationDate()); %>
						<h5><%= new java.sql.Date(message.getCreationDate().getTime()) %></h5>
						<table style="width=100%;" cellspacing="4">
					<% } else { %>
						<% current = Calendar.getInstance(); current.setTime(message.getCreationDate()); %>
						<% if (last.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR)) { %>
							<% last = current; %>
							</table>
							<h5><%= new java.sql.Date(message.getCreationDate().getTime()) %></h5>
							<table style="width=100%;" cellspacing="4">
						<% } %>
					<% } %>
					<tr>
						<td><input type=checkbox name=<%= message.getId() %> <%= livechatBean.getLogCheckedString() %>></td>
						<td nowrap><span class="chat"><%= new java.sql.Time(message.getCreationDate().getTime()) %></span></td>
						<td nowrap><span class="chat"><%= message.getNick() %> </span></td>
						<td nowrap><span class="chat"><%= (message.getTargetNick() == null) ? "" : message.getTargetNick() %> </span></td>
						<td><span class=<%= message.isPrivate() ? "chat-response" : "chat"%>><%= Utils.escapeHTML(message.getMessage()) %></span></td>
					</tr>
				<% } %>
				</table>
			<% } else if (!livechatBean.getAttachmentsResults().isEmpty()) { %>
				<input id="select-icon" class="icon" type="submit" name="selectAllLogs" value="" title="<%= loginBean.translate("Select all of the attachments") %>">
				<input id="remove-icon" class="icon" type="submit" name="deleteLogs" value="" title="<%= loginBean.translate("Delete the selected attachments") %>">
				<br/>
				<% Calendar current, last = null; %>
				<% for (ChannelAttachment attachment : livechatBean.getAttachmentsResults()) { %>
					<% if (last == null) { %>
						<% last = Calendar.getInstance(); last.setTime(attachment.getCreationDate()); %>
						<h5><%= new java.sql.Date(attachment.getCreationDate().getTime()) %></h5>
						<table style="width=100%;" cellspacing="4">
					<% } else { %>
						<% current = Calendar.getInstance(); current.setTime(attachment.getCreationDate()); %>
						<% if (last.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR)) { %>
							<% last = current; %>
							</table>
							<h5><%= new java.sql.Date(attachment.getCreationDate().getTime()) %></h5>
							<table style="width=100%;" cellspacing="4">
						<% } %>
					<% } %>
					<tr>
						<td><input type=checkbox name=<%= attachment.getMediaId() %> <%= livechatBean.getLogCheckedString() %>></td>
						<td nowrap><span class="chat"><%= new java.sql.Time(attachment.getCreationDate().getTime()) %></span></td>
						<td nowrap><span class="chat"><%= attachment.getCreatorId() %> </span></td>
						<td nowrap><span class="chat"><%= attachment.getName() %> </span></td>
						<td nowrap><span class="chat"><%= attachment.getType() %> </span></td>
						<td nowrap><span class="chat"><%= Utils.linkHTML(Site.SECUREURLLINK + "/" + attachment.getFileName()) %> </span></td>
					</tr>
				<% } %>
				</table>
			<% } %>
			<br/><br/>
		</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
<% } catch (Exception error) { loginBean.error(error); }%>
</body>
</html>
