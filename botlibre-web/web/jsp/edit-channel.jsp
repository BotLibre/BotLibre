<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Live Chat Channel<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<jsp:include page="channel-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="section">
<% } %>
	<h1><%= bean.getInstanceName() %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a channel") %>.
		</p>
	<% } else if (bean.getEditInstance() == null) { %>
		<p><%= loginBean.translate("No channel selected.") %></p>
	<% } else if (!(bean.isAdmin() || bean.isSuper())) { %>
		<p><%= loginBean.translate("Must be channel admin.") %></p>
	<% } else { %>
		<form action="livechat" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			
			<% bean.writeEditNameHTML(null, false, out); %>
			
			<%= loginBean.translate("Channel Type") %> 
			<select autofocus name="type" title="<%= loginBean.translate("The channel can either be a chat room that allows multiple users to chat with each other, or a one on one channel where users are queued to chat with an administrator.") %>">
				<option value="ChatRoom" <%= bean.isTypeSelected("ChatRoom") %>><%= loginBean.translate("Chat Room") %></option>
				<option value="OneOnOne" <%= bean.isTypeSelected("OneOnOne") %>><%= loginBean.translate("One On One") %></option>
			</select><br/>
			
			<% bean.writeEditCommonHTML(null, false, out); %>
			<% if (!bean.getEditInstance().isExternal()) { %>
				<tr>
					<td><%= loginBean.translate("Video Access") %></td>
					<td><select name="videoAccessMode" title="<%= loginBean.translate("Define who can broadcast video in this channel") %>">
						<option value="Everyone" <%= bean.isVideoAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= bean.isVideoAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= bean.isVideoAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= bean.isVideoAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
						<option value="Disabled" <%= bean.isVideoAccessModeSelected("Disabled") %>><%= loginBean.translate("Disabled") %></option>
					</select></td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Audio Access") %></td>
					<td><select name="audioAccessMode" title="<%= loginBean.translate("Define who can broadcast audio in this channel") %>">
						<option value="Everyone" <%= bean.isAudioAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= bean.isAudioAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= bean.isAudioAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= bean.isAudioAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
						<option value="Disabled" <%= bean.isAudioAccessModeSelected("Disabled") %>><%= loginBean.translate("Disabled") %></option>
					</select></td>
				</tr>
				</table>
			<% } %>

			<% bean.writEditAdCodeHTML(null, false, out); %>
					  	
			<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
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