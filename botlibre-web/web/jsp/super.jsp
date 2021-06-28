<%@page import="org.botlibre.web.service.TelegramService"%>
<%@page import="org.eclipse.persistence.sessions.server.ServerSession"%>
<%@page import="org.botlibre.knowledge.BasicRelationship"%>
<%@page import="org.botlibre.web.service.FacebookService"%>
<%@page import="org.botlibre.web.service.TwitterService"%>
<%@page import="org.botlibre.web.service.EmailService"%>
<%@page import="org.botlibre.web.service.ForgetfulnessService"%>
<%@page import="org.botlibre.web.service.AdminService"%>
<%@page import="org.botlibre.web.service.AppIDStats"%>
<%@page import="org.botlibre.web.service.Stats"%>
<%@page import="org.botlibre.knowledge.database.DatabaseMemory.SessionInfo"%>
<%@page import="org.botlibre.knowledge.database.DatabaseMemory"%>
<%@page import="org.botlibre.knowledge.BasicVertex"%>
<%@page import="org.eclipse.persistence.internal.sessions.AbstractSession"%>
<%@page import="org.eclipse.persistence.sessions.Session"%>
<%@page import="java.text.NumberFormat"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.sessions.factories.SessionManager"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.service.BeanManager"%>
<%@page import="org.botlibre.web.service.InstanceManager"%>
<%@page import="org.botlibre.web.service.BotManager"%>
<%@page import="org.botlibre.Bot"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.botlibre.web.admin.ClientType"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page import="org.botlibre.web.bean.LoginBean.Page" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<% if (loginBean.checkEmbed(request, response)) { return; } %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Sys Admin Console") %> - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<style type="text/css">
		.setting-labels {
			color: #585858;
			font-size: 16px;
			line-height: 24px;
		}
	</style>
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
</head>
<body>
	<script>
	$(document).ready(function() 
		{
			// Catch empty table errors.
			try {
				$("#botinstancecache").tablesorter({widgets: ['zebra']});
			} catch (err) {}
			try {
				$("#botcache").tablesorter({widgets: ['zebra']});
			} catch (err) {}
			try {
				$("#sessions").tablesorter({widgets: ['zebra']});
			} catch (err) {}
			try {
				$("#jpa").tablesorter({widgets: ['zebra']});
			} catch (err) {}
		} 
	);
	</script>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="about">
				<h1><%= loginBean.translate("Sys Admin Console") %></h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
				  <%= loginBean.translate("Must be sys admin") %>
				<% } else { %>
					<div id="tabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
						<ul class='ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all'>
							<li class='ui-state-default ui-corner-top ui-tabs-active ui-state-active'><a href='#tabs-1' class='ui-tabs-anchor'>Memory</a></li>
							<li class='ui-state-default ui-corner-top'><a href='#tabs-2' class='ui-tabs-anchor'>Admin Settings</a></li>
							<li class='ui-state-default ui-corner-top'><a href='#tabs-3' class='ui-tabs-anchor'>Manage Users</a></li>
							<li class='ui-state-default ui-corner-top'><a href='#tabs-4' class='ui-tabs-anchor'>Admin Tasks</a></li>
						</ul>
						<div id="tabs-2" class="ui-tabs-hide">
							<form action="super" method="post" class="message">
								<%= loginBean.postTokenInput() %>
								<p><%= loginBean.translate(
										"This lets you configure your server settings." +
										"Use caution when changing server settings as they can stop your server from functioning." +
										"Changing some settings may require a web server restart."
									) %>
								</p>
								<h3><%= loginBean.translate("URL Settings") %></h3>
								<div class="setting-labels">
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL's path") %> (e.g. /botlibreplatform)<br/>
										</div>
									</span>
									<%= loginBean.translate("URL Prefix") %><br/>
									<input type="text" name="URL_PREFIX" value="<%= Site.URL_PREFIX %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL's port and path") %> (e.g. :9080/botlibreplatform)<br/>
										</div>
									</span>
									<%= loginBean.translate("URL Suffix") %><br/>
									<input type="text" name="URL_SUFFIX" value="<%= Site.URL_SUFFIX %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL's domain name") %> (e.g. bots.domain.com)<br/>
										</div>
									</span>
									<%= loginBean.translate("Server Name") %><br/>
									<input type="text" name="SERVER_NAME" value="<%= Site.SERVER_NAME %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL's domain name") %> (e.g. bots.domain.com)<br/>
										</div>
									</span>
									<%= loginBean.translate("Server Name (2)") %><br/>
									<input type="text" name="SERVER_NAME2" value="<%= Site.SERVER_NAME2 %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL (without protocol)") %> (e.g. bots.domain.com:9080/botlibreplatform)<br/>
										</div>
									</span>
									<%= loginBean.translate("URL") %><br/>
									<input type="text" name="URL" value="<%= Site.URL %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL with protocol") %> (e.g. http://bots.domain.com:9080/botlibreplatform)<br/>
										</div>
									</span>
									<%= loginBean.translate("URL Link") %><br/>
									<input type="text" name="URLLINK" value="<%= Site.URLLINK %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Server URL with secure protocol") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Secure URL Link") %><br/>
									<input type="text" name="SECUREURLLINK" value="<%= Site.SECUREURLLINK %>"/>
									<br/>
									<%= loginBean.translate("Sandbox URL Link") %><br/>
									<input type="text" name="SANDBOXURLLINK" value="<%= Site.SANDBOXURLLINK %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Old domain name to be redirected. This is blank by default.") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Redirect") %><br/>
									<input type="text" name="REDIRECT" value="<%= Site.REDIRECT %>"/>
									<br/>
									<input type="checkbox" name="HTTPS" <%= Site.HTTPS ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Use HTTPS for links if connected with HTTPS") %><br/>
										</div>
									</span>
									<%= loginBean.translate("HTTPS") %>
									<br/>
									<input type="checkbox" name="LOCK" <%= Site.LOCK ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Do not allow access to the website from any other domains.") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Lock Domain Name") %>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("URL of Python Flask server (only for Bot Libre Enterprise Bot Platform)") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Python Server URL") %><br/>
									<input type="text" name="PYTHONSERVER" value="<%= Site.PYTHONSERVER %>"/>
									<br/>
								</div>
								
								<h3><%= loginBean.translate("Platform Settings") %></h3>
								<div class="setting-labels">
									<input type="checkbox" name="BOOTSTRAP" <%= Site.BOOTSTRAP ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Allow the server to be bootstrapped if it fails to connect to the database") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Allow Bootstrap") %>
									<br/>
									<input type="checkbox" name="READONLY" <%= Site.READONLY ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Do not allow any changes (set when migrating)") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Read-only") %>
									<br/>
									<input type="checkbox" name="ADULT" <%= Site.ADULT ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Disable Profanity Filter") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Disable Profanity Filter") %>
									<br/>
									<%= loginBean.translate("Default Content Rating") %><br/>
									<input type="text" name="CONTENT_RATING" value="<%= Site.CONTENT_RATING.toString() %>"/>
									<br/>
									<%= loginBean.translate("Website Name") %><br/>
									<input type="text" name="NAME" value="<%= Site.NAME %>"/>
									<br/>
									<%= loginBean.translate("Default Workspace Name") %><br/>
									<input type="text" name="DOMAIN" value="<%= Site.DOMAIN %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Internal site id") %><br/>
										</div>
									</span>
									<%= loginBean.translate("ID") %><br/>
									<input type="text" name="ID" value="<%= Site.ID %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("JavaScript embed prefix") %><br/>
										</div>
									</span>
									<%= loginBean.translate("JavaScript/css prefix") %><br/>
									<input type="text" name="PREFIX" value="<%= Site.PREFIX %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("JPA connection protocol to postgres") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Persistence Protocol") %><br/>
									<input type="text" name="PERSISTENCE_PROTOCOL" value="<%= Site.PERSISTENCE_PROTOCOL %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("JPA connection host") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Persistence Host") %><br/>
									<input type="text" name="PERSISTENCE_HOST" value="<%= Site.PERSISTENCE_HOST %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("JPA connection port") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Persistence Port") %><br/>
									<input type="text" name="PERSISTENCE_PORT" value="<%= Site.PERSISTENCE_PORT %>"/>
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("JPA persistence unit") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Persistence Unit") %><br/>
									<input type="text" name="PERSISTENCE_UNIT" value="<%= Site.PERSISTENCE_UNIT %>"/>
									<br/>
									<%= loginBean.translate("Twitter Hashtag") %><br/>
									<input type="text" name="HASHTAG" value="<%= Site.HASHTAG %>"/>
									<br/>
									<%= loginBean.translate("Primary Content Type") %><br/>
									<input type="text" name="TYPE" value="<%= Site.TYPE %>"/>
									<br/>
									<input type="checkbox" name="DEDICATED" <%= Site.DEDICATED ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Dedicated Server") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Dedicated Platform") %>
									<br/>
									<input type="checkbox" name="CLOUD" <%= Site.CLOUD ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Cloud Bot Platform") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Cloud Platform") %>
									<br/>
									<input type="checkbox" name="COMMERCIAL" <%= Site.COMMERCIAL ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("commercial vs free open") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Commercial Platform") %>
									<br/>
									<input type="checkbox" name="ALLOW_SIGNUP" <%= Site.ALLOW_SIGNUP ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable/Disable User Sign Up") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Allow Sign Up") %>
									<br/>
									<input type="checkbox" name="VERIFYUSERS" <%= Site.VERIFYUSERS ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Require user's to give name/email on sign up") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Verify Users (require name/email on sign up)") %>
									<br/>
									<input type="checkbox" name="VERIFY_EMAIL" <%= Site.VERIFY_EMAIL ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Require verified email to create public content") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Verify User Email (before allowing content creation)") %>
									<br/>
									<input type="checkbox" name="ANONYMOUS_CHAT" <%= Site.ANONYMOUS_CHAT ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("If disabled, then chat requires sign in") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Allow Anonymous Chat") %>
									
									<br/>
									<input type="checkbox" name="REQUIRE_TERMS" <%= Site.REQUIRE_TERMS ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Require terms to be accepted before chat") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Require Accept Terms (to chat)") %>
									
									<br/>
									<input type="checkbox" name="AGE_RESTRICT" <%= Site.AGE_RESTRICT ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Require age check before chat") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Age Restrict (> 13)") %>
									
									<br/>
									<input type="checkbox" name="REVIEW_CONTENT" <%= Site.REVIEW_CONTENT ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Require admin approval before public content is visible in browse directory") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Require public content review") %>
									
									<br/>
									<input type="checkbox" name="BACKLINK" <%= Site.BACKLINK ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Require backlink in embed code") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Require Embedding Backlink") %>
									
									<br/>
									<input type="checkbox" name="DISABLE_SUPERGROUP" <%= Site.DISABLE_SUPERGROUP ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Do not allow messages from Telegram super groups") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Disable Telegram super groups") %>
									
									<br/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Block the web agent string (dos attack/web crawler/bot)") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Block web agent string") %><br/>
									<input type="text" name="BLOCK_AGENT" value="<%= Site.BLOCK_AGENT %>"/>
									<br/>
								</div>
								<h3><%= loginBean.translate("Services Settings") %></h3>
								<div class="setting-labels">
									<input type="checkbox" name="TWITTER" <%= Site.TWITTER ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Twitter support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Twitter") %>
									<br/>
									<input type="checkbox" name="FACEBOOK" <%= Site.FACEBOOK ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Facebook support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Facebook") %>
									<br/>
									<input type="checkbox" name="TELEGRAM" <%= Site.TELEGRAM ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Telegram support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Telegram") %>
									<br/>
									<input type="checkbox" name="SLACK" <%= Site.SLACK ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Slack support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Slack") %>
									<br/>
									<input type="checkbox" name="SKYPE" <%= Site.SKYPE ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Skype support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Skype") %>
									<br/>
									<input type="checkbox" name="WECHAT" <%= Site.WECHAT ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot WeChat support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("WeChat") %>
									<br/>
									<input type="checkbox" name="KIK" <%= Site.KIK ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Kik support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Kik") %>
									<br/>
									<input type="checkbox" name="EMAIL" <%= Site.EMAIL ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot Email support") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Email") %>
									<br/>
									<input type="checkbox" name="TIMERS" <%= Site.TIMERS ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot timers") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Timers") %>
									<br/>
									<input type="checkbox" name="FORGET" <%= Site.FORGET ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable bot forgetfulness") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Forgetfulness") %>
									<br/>
									<input type="checkbox" name="ADMIN" <%= Site.ADMIN ? "checked" : "" %>/>
									<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Enable server admin services") %><br/>
										</div>
									</span>
									<%= loginBean.translate("Admin") %>
									<br/>
								</div>
								
								<h3><%= loginBean.translate("Email Settings") %></h3>
								<div class="setting-labels">
									<input type="checkbox" name="WEEKLYEMAIL" <%= Site.WEEKLYEMAIL ? "checked" : "" %>/><%= loginBean.translate("Weekly Email (summary of active content)") %>
									<br/>
									<input type="checkbox" name="WEEKLYEMAILBOTS" <%= Site.WEEKLYEMAILBOTS ? "checked" : "" %>/><%= loginBean.translate("Weekly Email Bots (include active bots in weekly email)") %>
									<br/>
									<input type="checkbox" name="WEEKLYEMAILCHANNELS" <%= Site.WEEKLYEMAILCHANNELS ? "checked" : "" %>/><%= loginBean.translate("Weekly Email Channels (include active channels in weekly email)") %>
									<br/>
									<input type="checkbox" name="WEEKLYEMAILFORUMS" <%= Site.WEEKLYEMAILFORUMS ? "checked" : "" %>/><%= loginBean.translate("Weekly Email Forums (include active forums in weekly email)") %>
									<br/>
									<%= loginBean.translate("Email Domain") %><br/>
									<input type="text" name="EMAILHOST" value="<%= Site.EMAILHOST %>"/>
									<br/>
									<%= loginBean.translate("Sales Email") %><br/>
									<input type="text" name="EMAILSALES" value="<%= Site.EMAILSALES %>"/>
									<br/>
									<%= loginBean.translate("Pay Pal Email") %><br/>
									<input type="text" name="EMAILPAYPAL" value="<%= Site.EMAILPAYPAL %>"/>
									<br/>
									<%= loginBean.translate("Email Signature (user bot emails)") %><br/>
									<input type="text" name="SIGNATURE" value="<%= Site.SIGNATURE %>"/>
									<br/>
									<%= loginBean.translate("Website Bot Email") %><br/>
									<input type="text" name="EMAILBOT" value="<%= Site.EMAILBOT %>"/>
									<br/>
									<%= loginBean.translate("SMTP Host (website bot email)") %><br/>
									<input type="text" name="EMAILSMTPHost" value="<%= Site.EMAILSMTPHost %>"/>
									<br/>
									<%= loginBean.translate("SMTP Port (website bot email)") %><br/>
									<input type="text" name="EMAILSMTPPORT" value="<%= Site.EMAILSMTPPORT %>"/>
									<br/>
									<%= loginBean.translate("User (website bot email)") %><br/>
									<input type="text" name="EMAILUSER" value="<%= Site.EMAILUSER %>"/>
									<br/>
									<%= loginBean.translate("Password (website bot email)") %><br/>
									<input type="password" name="EMAILPASSWORD" value="<%= Site.EMAILPASSWORD %>"/>
									<br/>
									<input type="checkbox" name="EMAILSSL" <%= Site.EMAILSSL ? "checked" : "" %>/><%= loginBean.translate("SSL (website bot email)") %>
									<br/>
								</div>
								
								<h3><%= loginBean.translate("Limits") %></h3>
								<div class="setting-labels">
									<%= loginBean.translate("Bot Knowledge Limit") %><br/>
									<input type="text" name="MEMORYLIMIT" value="<%= Site.MEMORYLIMIT %>"/>
									<br/>
									<%= loginBean.translate("Bot Max Script Process Time") %><br/>
									<input type="text" name="MAX_PROCCESS_TIME" value="<%= Site.MAX_PROCCESS_TIME %>"/>
									<br/>
									<%= loginBean.translate("User Content Limit") %><br/>
									<input type="text" name="CONTENT_LIMIT" value="<%= Site.CONTENT_LIMIT %>"/>
									<br/>
									<%= loginBean.translate("Max Bot Creates(per IP per day)") %><br/>
									<input type="text" name="MAX_CREATES_PER_IP" value="<%= Site.MAX_CREATES_PER_IP %>"/>
									<br/>
									<%= loginBean.translate("Max User Messages (per IP per day)") %><br/>
									<input type="text" name="MAX_USER_MESSAGES" value="<%= Site.MAX_USER_MESSAGES %>"/>
									<br/>
									<%= loginBean.translate("Max Upload Size") %><br/>
									<input type="text" name="MAX_UPLOAD_SIZE" value="<%= Site.MAX_UPLOAD_SIZE %>"/>
									<br/>
									<%= loginBean.translate("Max Live Chat Message (stored in database, per channel)") %><br/>
									<input type="text" name="MAX_LIVECHAT_MESSAGES" value="<%= Site.MAX_LIVECHAT_MESSAGES %>"/>
									<br/>
									<%= loginBean.translate("Max Attachments (stored in database, per channel/bot/content)") %><br/>
									<input type="text" name="MAX_ATTACHMENTS" value="<%= Site.MAX_ATTACHMENTS %>"/>
									<br/>
									<%= loginBean.translate("Max Translations (stored in database)") %><br/>
									<input type="text" name="MAX_TRANSLATIONS" value="<%= Site.MAX_TRANSLATIONS %>"/>
									<br/>
									<%= loginBean.translate("URL Timeout") %><br/>
									<input type="text" name="URL_TIMEOUT" value="<%= Site.URL_TIMEOUT %>"/>
									<br/>
									<%= loginBean.translate("Max API (per user, per day)") %><br/>
									<input type="text" name="MAX_API" value="<%= Site.MAX_API %>"/>
									<br/>
									<%= loginBean.translate("Bronze Max API (per user, per day)") %><br/>
									<input type="text" name="MAX_BRONZE" value="<%= Site.MAX_BRONZE %>"/>
									<br/>
									<%= loginBean.translate("Gold Max API (per user, per day)") %><br/>
									<input type="text" name="MAX_GOLD" value="<%= Site.MAX_GOLD %>"/>
									<br/>
									<%= loginBean.translate("Platinum Max API (per user, per day)") %><br/>
									<input type="text" name="MAX_PLATINUM" value="<%= Site.MAX_PLATINUM %>"/>
									<br/>
									<%= loginBean.translate("Max Bot Cache") %><br/>
									<input type="text" name="MAX_BOT_CACHE_SIZE" value="<%= Site.MAX_BOT_CACHE_SIZE %>"/>
									<br/>
									<%= loginBean.translate("Max Bot Pool") %><br/>
									<input type="text" name="MAX_BOT_POOL_SIZE" value="<%= Site.MAX_BOT_POOL_SIZE %>"/>
									<br/>
									<%= loginBean.translate("Max Tweet Import") %><br/>
									<input type="text" name="MAXTWEETIMPORT" value="<%= Site.MAXTWEETIMPORT %>"/>
									<br/>
								</div>
								
								<h3><%= loginBean.translate("API & Service Keys") %></h3>
								<div class="setting-labels">
									<%= loginBean.translate("Platform Encryption Key") %><br/>
									<input type="password" name="KEY" value="<%= Site.KEY %>"/>
									<br/>
									<%= loginBean.translate("Platform Upgrade Secret") %><br/>
									<input type="password" name="UPGRADE_SECRET" value="<%= Site.UPGRADE_SECRET %>"/>
									<br/>
									<%= loginBean.translate("Twitter API Auth Key") %><br/>
									<input type="password" name="TWITTER_OAUTHKEY" value="<%= Site.TWITTER_OAUTHKEY %>"/>
									<br/>
									<%= loginBean.translate("Twitter API Auth Secret") %><br/>
									<input type="password" name="TWITTER_OAUTHSECRET" value="<%= Site.TWITTER_OAUTHSECRET %>"/>
									<br/>
									<%= loginBean.translate("Facebook API App ID") %><br/>
									<input type="password" name="FACEBOOK_APPID" value="<%= Site.FACEBOOK_APPID %>"/>
									<br/>
									<%= loginBean.translate("Facebook API App Secret") %><br/>
									<input type="password" name="FACEBOOK_APPSECRET" value="<%= Site.FACEBOOK_APPSECRET %>"/>
									<br/>
									<%= loginBean.translate("Google API Key") %><br/>
									<input type="password" name="GOOGLEKEY" value="<%= Site.GOOGLEKEY %>"/>
									<br/>
									<%= loginBean.translate("Google API Client ID") %><br/>
									<input type="password" name="GOOGLECLIENTID" value="<%= Site.GOOGLECLIENTID %>"/>
									<br/>
									<%= loginBean.translate("Google API Client Secret") %><br/>
									<input type="password" name="GOOGLECLIENTSECRET" value="<%= Site.GOOGLECLIENTSECRET %>"/>
									<br/>
									<%= loginBean.translate("Microsoft Speech API Key") %><br/>
									<input type="password" name="MICROSOFT_SPEECH_KEY" value="<%= Site.MICROSOFT_SPEECH_KEY %>"/>
									<br/>
									<%= loginBean.translate("Microsoft Speech API Endpoint") %><br/>
									<input type="password" name="MICROSOFT_SPEECH_ENDPOINT" value="<%= Site.MICROSOFT_SPEECH_ENDPOINT %>"/>
									<br/>
									<%= loginBean.translate("Responsive Voice Key") %><br/>
									<input type="password" name="RESPONSIVEVOICE_KEY" value="<%= Site.RESPONSIVEVOICE_KEY %>"/>
									<br/>
									<%= loginBean.translate("Yandex API Key") %><br/>
									<input type="password" name="YANDEX_KEY" value="<%= Site.YANDEX_KEY %>"/>
									<br/>
									<%= loginBean.translate("Microsoft Translation API Key") %><br/>
									<input type="password" name="MICROSOFT_TRANSLATION_KEY" value="<%= Site.MICROSOFT_TRANSLATION_KEY %>"/>
									<br/>
									<%= loginBean.translate("Microsoft Translation API Endpoint") %><br/>
									<input type="password" name="MICROSOFT_TRANSLATION_ENDPOINT" value="<%= Site.MICROSOFT_TRANSLATION_ENDPOINT %>"/>
									<br/>
								</div>
				
								<input type="submit" name="settings" value="Save"/>
							</form>
						</div>
						<div id="tabs-3" class="ui-tabs-hide">
							<p>
								<a href="create-user.jsp"><%= loginBean.translate("Create user") %></a><br/><br/>
								<a href="browse-user.jsp"><%= loginBean.translate("Browse users") %></a><br/>
							</p>
							<form action="super" method="post" class="message">
								<%= loginBean.postTokenInput() %>
								<h3><%= loginBean.translate("Transfer User") %></h3>
								<table>
									<tr><td><span><%= loginBean.translate("From user") %>:</span></td><td><input type="text" name="transferUserFrom"/></td></tr>
									<tr><td><span><%= loginBean.translate("To user") %>:</span></td><td><input type="text" name="transferUserTo"/></td></tr>
								</table>
								<input type="submit" name="transferUser" value="<%= loginBean.translate("Transfer") %>"/><br/>
							</form>
						</div>
						<div id="tabs-4" class="ui-tabs-hide">
							<form action="super" method="post" class="message">
								<%= loginBean.postTokenInput() %>
								<input type="submit" name="freeMemory" value="<%= loginBean.translate("Free Memory") %>"/>
								<input type="submit" name="dropDead" value="<%= loginBean.translate("Drop Dead") %>"/>
								<input type="submit" name="archiveInactive" value="<%= loginBean.translate("Archive Inactive") %>"/>
								<input type="submit" name="migrate" value="<%= loginBean.translate("Migrate") %>"/>
								<input type="submit" name="runForgetfullness" value="<%= loginBean.translate("Run Forgetfullness") %>"/>
								<br/>
								<input type="submit" name="initDatabase" value="<%= loginBean.translate("Init Database") %>"/>
								<input type="submit" name="translations" value="<%= loginBean.translate("Translation") %>"/>
								<input type="submit" name="clearBanned" value="<%= loginBean.translate("Clear Banned") %>"/>
								<input type="submit" name="cleanupJunk" value="<%= loginBean.translate("Cleanup Junk") %>"/>
								<br/>
							</form>
							
							<h3><%= loginBean.translate("Banned IPs") %></h3>
							<p>
							<ul>
							<% for (String ip : AdminDatabase.bannedIPs.values()) { %>
								<li><%= ip %></li>
							<% } %>
							</ul>
							</p>
						</div>
					
						<div id='tabs-1' class='ui-tabs-panel ui-widget-content ui-corner-bottom'>
							<p>
							<%= loginBean.translate("Free memory") %>: <%= NumberFormat.getInstance().format(Runtime.getRuntime().freeMemory() / 1000000) %> meg <br/>
							<%= loginBean.translate("Max memory") %>: <%= NumberFormat.getInstance().format(Runtime.getRuntime().maxMemory() / 1000000) %> meg <br/>
							<%= loginBean.translate("Total memory") %>: <%= NumberFormat.getInstance().format(Runtime.getRuntime().totalMemory() / 1000000) %> meg <br/>
							<%= loginBean.translate("Total threads") %>: <%= Thread.getAllStackTraces().keySet().size() %> <br/>
							<%= loginBean.translate("Active threads") %>: 
							<%
								int active = 0;
								for (Thread thread : Thread.getAllStackTraces().keySet()) {
									if (thread.getState() == Thread.State.RUNNABLE) {
										active++;
									}
								}
							%> <%= active %><br/>
							<br/>
							<%= loginBean.translate("Admin service") %>: <%= AdminService.instance() == null ? "null" : AdminService.instance().getChecker().isAlive() %><br/>
							<%= loginBean.translate("Forgetfulness service") %>: <%= ForgetfulnessService.instance() == null ? "null" : ForgetfulnessService.instance().getChecker().isAlive() %> : last run <%= String.valueOf(ForgetfulnessService.lastRun) %><br/>
							<% boolean email = EmailService.instance() != null && EmailService.instance().getChecker() != null; %>
							<%= loginBean.translate("Email service") %>: <%= !email ? "null" : EmailService.instance().getChecker().isAlive() %>
								 <%= loginBean.translate("bronze") %>: <%= !email ? "null" : (EmailService.instance().getBronzeChecker() != null && EmailService.instance().getBronzeChecker().isAlive()) %>
								 <%= loginBean.translate("gold") %>: <%= !email ? "null" : (EmailService.instance().getGoldChecker() != null && EmailService.instance().getGoldChecker().isAlive()) %>
								 <%= loginBean.translate("platinum") %>: <%= !email ? "null" : (EmailService.instance().getPlatinumChecker() != null && EmailService.instance().getPlatinumChecker().isAlive()) %> <br/>
							<% boolean facebook = FacebookService.instance() != null && FacebookService.instance().getChecker() != null; %>
							<%= loginBean.translate("Facebook service") %>: <%= !facebook ? "null" : FacebookService.instance().getChecker().isAlive() %>
								 <%= loginBean.translate("bronze") %>: <%= !facebook ? "null" : (FacebookService.instance().getBronzeChecker() != null && FacebookService.instance().getBronzeChecker().isAlive()) %>
								 <%= loginBean.translate("gold") %>: <%= !facebook ? "null" : (FacebookService.instance().getGoldChecker() != null && FacebookService.instance().getGoldChecker().isAlive()) %>
								 <%= loginBean.translate("platinum") %>: <%= !facebook ? "null" : (FacebookService.instance().getPlatinumChecker() != null && FacebookService.instance().getPlatinumChecker().isAlive()) %> <br/>
							<% boolean twitter = TwitterService.instance() != null && TwitterService.instance().getChecker() != null; %>
							<%= loginBean.translate("Twitter service") %>: <%= !twitter ? "null" : TwitterService.instance().getChecker().isAlive() %>
								 <%= loginBean.translate("bronze") %>: <%= !twitter ? "null" : (TwitterService.instance().getBronzeChecker() != null && TwitterService.instance().getBronzeChecker().isAlive()) %>
								 <%= loginBean.translate("gold") %>: <%= !twitter ? "null" : (TwitterService.instance().getGoldChecker() != null && TwitterService.instance().getGoldChecker().isAlive()) %>
								 <%= loginBean.translate("platinum") %>: <%= !twitter ? "null" : (TwitterService.instance().getPlatinumChecker() != null && TwitterService.instance().getPlatinumChecker().isAlive()) %> <br/>
							</p>
							<% boolean telegram = TelegramService.instance() != null && TelegramService.instance().getChecker() != null; %>
							<%= loginBean.translate("Telegram service") %>: <%= !telegram ? "null" : TelegramService.instance().getChecker().isAlive() %>
								 <%= loginBean.translate("bronze") %>: <%= !telegram ? "null" : (TelegramService.instance().getBronzeChecker() != null && TelegramService.instance().getBronzeChecker().isAlive()) %>
								 <%= loginBean.translate("gold") %>: <%= !telegram ? "null" : (TelegramService.instance().getGoldChecker() != null && TelegramService.instance().getGoldChecker().isAlive()) %>
								 <%= loginBean.translate("platinum") %>: <%= !telegram ? "null" : (TelegramService.instance().getPlatinumChecker() != null && TelegramService.instance().getPlatinumChecker().isAlive()) %> <br/>
							</p>
							<h3><%= loginBean.translate("Bot instance cache") %></h3>
							<p>
							<%= loginBean.translate("Count") %>: <%= Bot.getInstances().size() %>
							</p>
							<table id="botinstancecache" class="tablesorter">
								<thead>
								<tr>
									<th><%= loginBean.translate("Name") %></th>
									<th><%= loginBean.translate("Database") %></th>
								</tr>
								</thead>
								<% for (Bot instance : Bot.getInstances().values()) { %>
									<tr><td><%= instance.getName() %></td><td><%= instance.memory().getMemoryName() %></td></tr>
								<% } %>
							</table>
							<h3><%= loginBean.translate("Bot cache") %></h3>
							<p>
							<%= loginBean.translate("Count") %>: <%= BotManager.manager().getInstances().size() %> : <%= BotManager.manager().getQueue().size() %><br/>
							<%= loginBean.translate("Scavenges") %>: <%= BotManager.manager().getScavenges() %> : alive: <%= BotManager.manager().getScavenger().isAlive() %> : scavenging: <%= BotManager.manager().isScavenging() %><br/>
							<%
								long maxAge = 0;
								for (InstanceManager.InstanceInfo info : BotManager.manager().getInstances().values()) {
									if (maxAge == 0 || info.age < maxAge) {
										maxAge = info.age;
									}
								}
							%>
							<%= loginBean.translate("Max Age") %>: <%= maxAge == 0 ? "none" : new java.sql.Timestamp(maxAge) %>
							</p>
							<table id="botcache" class="tablesorter">
								<thead>
								<tr>
									<th><%= loginBean.translate("Name") %></th>
									<th><%= loginBean.translate("Database") %></th>
									<th><%= loginBean.translate("Age") %></th>
									<th><%= loginBean.translate("Pooled") %></th>
									<th><%= loginBean.translate("Objects") %></th>
								</tr>
								</thead>
								<% for (InstanceManager.InstanceInfo info : BotManager.manager().getInstances().values()) { %>
									<tr>
										<td><%= ((Bot)info.instance).getName() %></td>
										<td><%= ((Bot)info.instance).memory().getMemoryName() %></td>
										<td><%= new java.sql.Timestamp(info.age) %></td>
										<td><%= BotManager.manager().getQueue().contains(info) %></td>
										<td><%= ((Bot)info.instance).memory().getShortTermMemory().size() %></td>
									</tr>
								<% } %>
							</table>
							<h3><%= loginBean.translate("Bean cache") %></h3>
							<p>
							<%= loginBean.translate("Count") %>: <%= BeanManager.manager().getInstances().size() %> : <%= BeanManager.manager().getQueue().size() %><br/>
							<%= loginBean.translate("Scavenges") %>: <%= BeanManager.manager().getScavenges() %> : alive: <%= BeanManager.manager().getScavenger().isAlive() %> : scavenging: <%= BotManager.manager().isScavenging() %><br/>
							<% if (BeanManager.manager().isDeadlocked()) { %>
								<span style="color:red;font-weight:bold;font-size:30px">DEADLOCKED!</span><br/>
							<% } %>
							</p>
							<h3><%= loginBean.translate("Sessions") %></h3>
							<p>
							<%= loginBean.translate("Count") %>: <%= DatabaseMemory.sessions.size() %>
							</p>
							<table id="sessions" class="tablesorter">
								<thead>
								<tr>
									<th><%= loginBean.translate("URL") %></th>
									<th><%= loginBean.translate("Count") %></th>
									<th><%= loginBean.translate("Objects") %></th>
									<th><%= loginBean.translate("Relationships") %></th>
									<th><%= loginBean.translate("Read Connections") %></th>
									<th><%= loginBean.translate("Write Connections") %></th>
								</tr>
								</thead>
								<% for (DatabaseMemory.SessionInfo info : DatabaseMemory.sessions.values()) { %>
									<% if (info.session != null) { %>
										<tr>
											<td><%= info.session.getLogin().getURL() %></td>
											<td><%= info.count %></td>
											<td><%= info.session.getIdentityMapAccessorInstance().getIdentityMap(BasicVertex.class).getSize() %></td>
											<td><%= info.session.getIdentityMapAccessorInstance().getIdentityMap(BasicRelationship.class).getSize() %></td>
											<td><%= ((ServerSession)info.session).getConnectionPool("default").getTotalNumberOfConnections() %></td>
											<td><%= ((ServerSession)info.session).getConnectionPool("default").getTotalNumberOfConnections() %></td>
										</tr>
									<% } %>
								<% } %>
							</table>
							<h3><%= loginBean.translate("JPA") %></h3>
							<p>
							<%= loginBean.translate("Count") %>: <%= SessionManager.getManager().getSessions().size() %>
							</p>
							<table id="jpa" class="tablesorter">
								<thead>
								<tr>
									<th><%= loginBean.translate("URL") %></th>
									<th><%= loginBean.translate("Objects") %></th>
									<th><%= loginBean.translate("Relationships") %></th>
									<th><%= loginBean.translate("Read Connections") %></th>
									<th><%= loginBean.translate("Write Connections") %></th>
								</tr>
								</thead>
							<% for (Object value : SessionManager.getManager().getSessions().values()) { %>
								<tr>
									<td><%= ((AbstractSession)value).getLogin().getURL() %></td>
									<% if (((AbstractSession)value).hasDescriptor(BasicVertex.class)) { %>
										 <td><%= ((AbstractSession)value).getIdentityMapAccessorInstance().getIdentityMap(BasicVertex.class).getSize() %></td>
										 <td><%= ((AbstractSession)value).getIdentityMapAccessorInstance().getIdentityMap(BasicRelationship.class).getSize() %></td>
									<% } else { %>
										<td></td>
										<td></td>
									<% } %>
									<td><%= ((ServerSession)value).getConnectionPool("default").getTotalNumberOfConnections() %></td>
									<td><%= ((ServerSession)value).getConnectionPool("default").getTotalNumberOfConnections() %></td>
								</tr>
							<% } %>
							</table>
						</div>
					</div>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>