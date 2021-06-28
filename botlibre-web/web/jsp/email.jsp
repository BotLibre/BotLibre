<%@page import="org.botlibre.web.bean.EmailBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% EmailBean bean = loginBean.getBean(EmailBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Email - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to email"/>	
	<meta name="keywords" content="email, gmail, email bot, bot, email automation"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The email tab allows you to connect your bot to an email account, and monitor and reply to messages.") %>
					<%= loginBean.translate("Connecting a bot to email also allows access to email from scripts.") %><br/>
					<%= loginBean.translate("The bot can be connected passively (does not reply) or actively (replies to new email).") %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-email.jsp"><%= loginBean.translate("Docs") %></a> : <a href="https://www.botlibre.com/forum-post?id=30006901"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/email.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to manage an email account and reply to email.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Email") %>
			</h1>
			<p>
			<%= loginBean.translate("Note, you are not allowed to use your bot for spam, or violate our terms of service.") %>
			</p>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<% if (bean.isConnected()) { %>
					<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the email address") %>
					<a target="_blank" href="<%= "mailto:" + bean.getEmailAddress() %>"><%= bean.getEmailAddress() %></a><br/>
				<% } else { %>
					<%= botBean.getMustBeAdminMessage() %>
				<% } %>
			<% } else { %>
				<% if (bean.isConnected()) { %>
						<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the email address") %>
						<a target="_blank" href="<%= "mailto:" + bean.getUserName() %>"><%= bean.getUserName() %></a><br/><br/>
				<% } %>
				<form action="email" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Email address that your bot will reply as, i.e. mybot@gmail.com") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Email Address") %><br/>
					<input type="email" name="emailAddress" value="<%= bean.getEmailAddress() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Email account user name, this is normally the email address but depends on your email service, i.e. mybot@gmail.com") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("User") %><br/>
					<input type="text" name="userName" value="<%= bean.getUserName() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Email account password.") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Password") %><br/>
					<input type="password" name="password" value="<%= bean.getPassword() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Enter your email service protocol (pop3, imaps), check your email provider's documentation.") %>
								<%= loginBean.translate("Enter 'imaps' for Gmail.") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Protocol") %><br/>
					<input type="text" name="protocol" value="<%= bean.getProtocol() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Use an SSL connection to access email. Most email providers provide SSL and non-SSL on different ports.") %>
								<%= loginBean.translate("This is not required for Gmail.") %>
							</p>
						</div>
					</span>
					<input type="checkbox" name="ssl" <% if (bean.getSSL()) { %>checked<% } %>/>SSL<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Enter your email service incoming host name (pop3, imaps), check your email provider's documentation.") %>
								<%= loginBean.translate("The imaps host for Gmail is imap.gmail.com") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Incoming Host") %><br/>
					<input type="text" name="incomingHost" value="<%= bean.getIncomingHost() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Enter your email service incoming port (pop3, imaps), check your email provider's documentation.") %>
								<%= loginBean.translate("The imaps port for Gmail is 993.") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Incoming Port") %><br/>
					<input type="number" name="incomingPort" value="<%= bean.getIncomingPort() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Enter your email service outgoing host name (smtp), check your email provider's documentation.") %>
								<%= loginBean.translate("The smtp host for Gmail is smtp.gmail.com") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Outgoing Host") %><br/>
					<input type="text" name="outgoingHost" value="<%= bean.getOutgoingHost() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Enter your email service outgoing port (smtp), check your email provider's documentation.") %>
								<%= loginBean.translate("The smtp port for Gmail is 587.") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Outgoing Port") %><br/>
					<input type="number" name="outgoingPort" value="<%= bean.getOutgoingPort() %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("The bot will pool for new email and reply to all new email.") %>
								<%= loginBean.translate("Only click this if you want the bot to reply to email, otherwise email will only be used to send email from scripts.") %>
							</p>
						</div>
					</span>
					<input type="checkbox" name="replyEmail" <% if (bean.isConnected()) { %>checked<% } %>/><%= loginBean.translate("Reply to Email") %><br/>
					<% if (bean.isConnected()) { %>
						<input id="cancel" type="submit" name="disconnect" value="<%= loginBean.translate("Disconnect") %>"/>
						<input type="submit" name="check" value="<%= loginBean.translate("Check Email") %>" title="<%= loginBean.translate("Have the bot check its email") %>"/><br/><br/>
					<% } %>
					
					<%= loginBean.translate("Signature") %><br/>
					<textarea name="signature" title="<%= loginBean.translate("Email signature to include in all emails") %>"
							><%= bean.getSignature() %></textarea><br/>
					<input type="submit" name="save" value="<%= loginBean.translate("Save") %>"/><br/>
					<br/>
					<hr/>
					<h2><%= loginBean.translate("Test Email") %></h2>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Verifies the email settings. By clicking 'Test', the bot will send a test email to the entered email address.") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Email Address") %><br/>
					<input type="email" name="testEmailAddress" value=""/><br/>
					<input type="submit" name="testEmail" value="<%= loginBean.translate("Test") %>"/><br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>