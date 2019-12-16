<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.IRCBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% IRCBean ircBean = loginBean.getBean(IRCBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>IRC - <%= Site.NAME %></title>
	<meta name="description" content="The IRC tab allows you to connect your bot to an IRC chat room, so it can chat or listen and learn from others"/>	
	<meta name="keywords" content="irc, chat, chatroom, freenode, icq, bot, irc bot"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The IRC tab allows you to connect your bot to an IRC chat room, so it can chat or listen and learn from others.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-irc.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %> 
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/irc.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to chat with others on an IRC chat channel.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("IRC") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
			
				<% if (ircBean.isConnected()) { %>
					<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently chatting in the") %> <i><%= ircBean.getChannel() %></i> <%= loginBean.translate("channel on the server") %> <i><%= ircBean.getServer() %></i>
					<p/>
					<%= loginBean.translate("To join the chat room join") %>: <a style="color:grey" href="irc://<%= ircBean.getServer() %>/<%= ircBean.getChannel() %>">irc://<%= ircBean.getServer() %>/<%= ircBean.getChannel() %></a>
				<% } else { %>
					<%= botBean.getInstanceName() %> is not currently on IRC.<br/>
					<%= loginBean.translate("Only admin users can configure IRC accounts.") %>
				<% } %>
			<% } else { %>
			
				<% if (ircBean.isConnected()) { %>
					<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently chatting in the") %> <i><%= ircBean.getChannel() %></i> <%= loginBean.translate("channel on the server") %> <i><%= ircBean.getServer() %></i>
					<p/>
					<%= loginBean.translate("To join this chat room join") %>:
					<a style="color:grey" href="irc://<%= ircBean.getServer() %>/<%= ircBean.getChannel() %>">irc://<%= ircBean.getServer() %>/<%= ircBean.getChannel() %></a>
					<form action="irc" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<input id="cancel" type="submit" name="disconnect" value="Disconnect"/><br/>
						<p/>
					</form>
			
				<% } else { %>
			
					<form action="irc" method="post" class="message">
				<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<%= loginBean.translate("Server") %><br/>
						<input id="server" type="text" name="server" value="" /><br/>
						<script>
						$( "#server" ).autocomplete({
						source: [ "irc.freenode.org", "irc.icq.com", "irc.quakenet.org", "irc.efnet.org", "irc.undernet.org" ],
					minLength: 0
						}).on('focus', function(event) {
						var self = this;
						$(self).autocomplete("search", "");
						});
						</script>
						<%= loginBean.translate("Channel") %><br/>
						<input id="channel" type="text" name="channel" value="" /><br/>
						<script>
						$( "#channel" ).autocomplete({
						source: [ "#botlibre", "#ai", "##linux", "##politics", "#teens", "#20_something", "#30_something", "#40_something","#christianity", "#buddhism" ],
					minLength: 0
						}).on('focus', function(event) {
						var self = this;
						$(self).autocomplete("search", "");
						});
						</script>
						<%= loginBean.translate("Nick") %><br/>
						<input type="text" name="nick" value="<%= Utils.compress(botBean.getInstanceName(), 20) %>" /><br/>
						<input type="checkbox" name="listen"><%= loginBean.translate("Listen only") %></input><br/>
						<input id="ok" type="submit" name="connect" value="Connect"/><br/>
						<span class="menu"><%= loginBean.translate("Check") %> <a href="log.jsp">log</a> <%= loginBean.translate("for errors") %></span>
					</form>
				<% } %>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
