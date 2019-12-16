<%@page import="org.botlibre.web.bean.SMSBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% SMSBean bean = loginBean.getBean(SMSBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Twilio") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Connect your bot to SMS text messaging and voice IVR") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("sms, voice, phone, ivr, mobile, texting, bot, sms bot, sms automation") %>"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The Twilio tab allows you to connect your bot to Twilio SMS messaging and voice IVR.") %><br/>
				</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-sms.jsp"><%= loginBean.translate("Docs") %></a> 
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=12896887"><%= loginBean.translate("How To Guide") %></a> 
			 : <a target="_blank" href="https://youtu.be/WpQNC_FuDcg"><%= loginBean.translate("Video") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/twilio.svg" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to SMS messages and voice calls.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Twilio SMS & IVR") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The Twilio tab allows you to connect your bot to Twilio SMS messaging and voice IVR.") %>
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					<%= loginBean.translate("The Twilio tab allows you to connect your bot to Twilio SMS messaging and voice IVR.") %>
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					<%= loginBean.translate("Connect your bot to a") %> <a href="https://www.twilio.com/" target="_blank">Twilio</a> <%= loginBean.translate(" account.") %>
				</p>
				<p>
					<%= loginBean.translate("Only registering the webhook with your Twilio account is required to reply to SMS messages and voice calls.") %>
					<%= loginBean.translate("To send messages, or make voice calls, you need to provide your Twilio account SID.") %>
				</p>
				<form action="sms" method="post" class="message">
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set this URL in your Twilio account to enable replying to SMS messages.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio SMS Webhook URL") %><br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set this URL in your Twilio account to enable replying to voice IVR calls.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio Voice Webhook URL") %><br/>
					<input type="text" name="voiceWebhook" value="<%= bean.getVoiceWebhook() %>" /><br/>
				</form>

				<form action="sms" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<h3>SMS Properties</h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("To send SMS messages, or make voice calls, enter your Twilio account SID.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio SID") %><br/>
					<input type="text" name="sid" value="<%= bean.getSid() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("To send SMS messages, or make voice calls, enter your Twilio account secret.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio Auth Token") %><br/>
					<input type="text" name="secret" value="<%= bean.getSecret() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("To send SMS messages, or make voice calls, enter your Twilio account phone number.") %>
							<%= loginBean.translate("Use the full number, i.e. +16131234567") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio Phone Number") %><br/>
					<input type="text" name="phone" value="<%= bean.getPhone() %>"/><br/>

					<input type="submit" name="save" value="<%= loginBean.translate("Save") %>"/><br/>
					<br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
