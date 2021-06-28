<%@page import="org.botlibre.web.bean.KikBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% KikBean bean = loginBean.getBean(KikBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Kik - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Kik"/>	
	<meta name="keywords" content="kik, mobile, texting, bot, kik bot, kik automation"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Kik tab allows you to connect your bot to Kik.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-kik.jsp"><%= loginBean.translate("Docs") %></a> 
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=19397983"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/kik.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to Kik messages.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Kik") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					The Kik tab allows you to connect your bot to Kik.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					The Kik tab allows you to connect your bot to Kik.
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					Connect your bot to a <a href="https://www.kik.com/" target="_blank">Kik</a> account.
				</p>
				<h3><%= loginBean.translate("Kik Properties") %></h3>
				
				<!--
				<form action="kik" method="post" class="message">
				
				<span class="dropt-banner">
					<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("Set this URL on the bot settings page on the Kik website to enable replying to messages on Kik.") %>	
					</div>
				</span>
					Kik Messaging Endpoint URL<br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
				</form>
				-->
				<form action="kik" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the Kik Display Name from the bot configuration page on the Kik Dev website.") %>
						</div>
					</span>
						<%= loginBean.translate("Kik Display Name") %><br/>
						<input type="text" name="username" value="<%= bean.getUsername() %>"/><br/>
						
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the Kik API Key from the bot configuration page on the Kik Dev website.") %>
						</div>
					</span>
						<%= loginBean.translate("Kik API Key") %><br/>
						<input type="text" name="apiKey" value="<%= bean.getApiKey() %>"/><br/>
						
					
					<!-- <input type="submit" name="connect" value="Connect" title="Connect the bot to Kik"/> -->
					<!-- <input type="submit" name="check" value="Check Status" title="Have the bot check Kik"/> -->
					<input type="submit" name="save" value="Save"/><br/>
					<br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
