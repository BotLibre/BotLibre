<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.bean.ChatBean"%>
<%@page import="org.botlibre.emotion.EmotionalState"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	if (proxy.getBeanId()== null) {
		proxy.setBeanId(proxy.getLastBeanId());
	}
	loginBean = proxy.checkLoginBean(loginBean);
	BotBean botBean = loginBean.getBotBean();
	ChatBean chatBean = loginBean.getBean(ChatBean.class);
	boolean help = loginBean.getHelp();
	loginBean.setHelp(false);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Chat - <%= Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<script>
	(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
	(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
	m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
	})(window,document,'script','//www.google-analytics.com/analytics.js','ga');
	
	ga('create', 'UA-45086519-1', 'botlibre.com');
	ga('send', 'pageview');
	
	</script>
	<% if (loginBean.getFocus()) { %>
		<script type="text/javascript">
		window.onload = function() {
			var divObject = document.getElementById('scroller');
			divObject.scrollTop = divObject.scrollHeight;
		}	
		</script>
	<% } %>
</head>
<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
		<jsp:include page="error.jsp"/>
		<% if (!botBean.isConnected()) { %>
			<%= chatBean.getFarewell() %>
		<% } else { %>
			<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>;">
				<span>chat hosted by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
			</div>
			<% if (chatBean.getShowAvatar()) { %>
				<h2><%= botBean.getInstanceName() %></h2>
				<table style="width:100%;">
				<tr valign="top">
				<% if (botBean.getAvatarFileName() != null) { %>
					<td valign="top">
						<% if (chatBean.getShowChatLog()) { %>
							<img src="<%= botBean.getAvatarFileName() %>" style="max-height:200px;max-width:200px;min-width:100px"/>
						<% } else { %>
							<img height="100%" src="<%= botBean.getAvatarFileName() %>"/>
						<% } %>
					</td>
				<% } %>
				<% if (chatBean.getShowChatLog()) { %>
					<td valign="top" align="left">
						<div id="scroller" style="position:fixed;top:80px;bottom:130px;left:208px;right:4px;overflow:auto;text-align:left">
							<%= chatBean.getChatLog() %>
						</div>
					</td>
				<% } %>
				</tr>
				</table>
			<% } %>
			<div style="position:fixed;bottom:4px;right:4px;left:4px">
				<p class="response"><%= chatBean.getResponseHTML() %></p>
				<% if (chatBean.getSpeak()) { %>
					<div style="display:none;position:absolute;">
					<audio id="audio" onended="document.getElementById('echat').focus();" controls="controls" autoplay="autoplay" 
							src="<%= chatBean.getResponseFileName() %>" type="audio/wav" hidden="true"></audio>
					</div>
				<% } %>
				<form action="embedded" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= proxy.proxyInput() %>
					<%= botBean.instanceInput() %>
					<table style="width:100%;">
						<tr style="width:100%;">
							<td style="width:100%;"><input id="echat" style="max-width:none;margin:0 0 0 0;padding:0" placeholder="<%= chatBean.getPrompt() %>" type="text" name="input"
									autofocus x-webkit-speech/></td>
							<td align="left"><input id="send" type="submit" name="submit" value="<%= chatBean.getSend() %>"/><br/></td>
						</tr>
					</table>
					<span class="menu">
						<% if (chatBean.getAllowSpeech()) { %>					
							<input class="menu" title="Speech requires an html5 audio supporting browser" type=checkbox name=speak <% if (chatBean.getSpeak()) { %>checked<% } %>>Speak</input>
						<% } %>
						<a style="float:right;margin:4px" class="menu" href="<%= "embedded?disconnect" + proxy.proxyString() %>">Disconnect</a><br/>
					</span>
				</form>
			</div>
		<% } %>
		<% if (loginBean.getFocus()) { %>
			<script>
			document.getElementById('echat').focus();
			</script>
		<% } %>
	</div>
<% loginBean.setHelp(help); %>
<% proxy.clear(); %>
</body>
</html>