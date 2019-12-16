<%@page import="org.botlibre.web.bean.SkypeBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% SkypeBean bean = loginBean.getBean(SkypeBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Skype - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Skype"/>	
	<meta name="keywords" content="skype, mobile, texting, bot, skype bot, skype automation"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Skype tab allows you to connect your bot to Skype.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-skype.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=18556425"><%= loginBean.translate("How To Guide") %></a>
			 : <a target="_blank" href="https://youtu.be/8-WrqWsnjoI"><%= loginBean.translate("Video") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/skype1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to Skype messages.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Skype") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					The Skype tab allows you to connect your bot to Skype.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					The Skype tab allows you to connect your bot to Skype.
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					Connect your bot to a <a href="https://www.skype.com/" target="_blank">Skype</a> account.
				</p>
				<h3><%= loginBean.translate("Skype Properties") %></h3>
				<p>
					<%= loginBean.translate("Register your 'Messaging endpoint' on the bot settings page on the Skype website.") %>
				</p>

				<form action="slack" method="post" class="message">
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set this URL on the bot settings page on the Skype website to enable replying to messages on Skype.") %>	
						</div>
					</span>
					Skype Messaging Endpoint URL<br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
				</form>
				<form action="skype" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the Skype App Id from the bot settings page on the Skype website.") %>
						</div>
					</span>
					<%= loginBean.translate("Skype App Id") %><br/>
					<input type="text" name="appId" value="<%= bean.getAppId() %>"/><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the Skype App Password that was provided from the Skype website.") %>
						</div>
					</span>
						<%= loginBean.translate("Skype App Password") %><br/>
						<input type="text" name="appPassword" value="<%= bean.getAppPassword() %>"/><br/>

					<!-- <input type="submit" name="check" value="Check Status" title="Have the bot check Skype"/> -->
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
