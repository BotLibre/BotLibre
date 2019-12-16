<%@page import="org.botlibre.web.bean.GoogleAssistantBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% GoogleAssistantBean bean = loginBean.getBean(GoogleAssistantBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Google Assistant - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Google Assistant"/>	
	<meta name="keywords" content="google, google assistant, texting, bot, google home"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Google Assistant tab allows you to connect your bot to Google Assistant.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-google-assistant.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=23109465"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/google-assistant1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to Google Assistant messages.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Google Assistant") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					The Google Assistant tab allows you to connect your bot to Google Assistant.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					The Google Assistant tab allows you to connect your bot to Google Assistant.
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					Connect your bot to a <a href="https://developers.google.com/actions/" target="_blank">Google Assistant</a> account.
				</p>
				<h3><%= loginBean.translate("Google Assistant Properties") %></h3>
				<p>
					<%= loginBean.translate("Register your 'Webhook URL' on the Fulfillment page of the DialogFlow website.") %>
				</p>
				
				<form action="google-assistant" method="post" class="message">
				
				<span class="dropt-banner">
					<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("Set this URL on the Fulfillment page on the DialogFlow website to enable replying to messages.") %>	
					</div>
				</span>
					Google Assistant Webhook URL<br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
				</form>
				<form action="google-assistant" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							Set phrases/words that will end the chat session.<br/>
							Each phrase must be separated by a new line.<br/>
						</div>
					</span>
						<%= loginBean.translate("End Conversation Phrases") %><br/>
						<textarea name="stopPhrases"><%= bean.getStopPhrases() %></textarea><br/>
					
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
