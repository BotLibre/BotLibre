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
	<title>Sys Admin Console - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
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
				<h1>Sys Admin Console</h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
				  Must be sys admin
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
								<p>
									This lets you configure your server settings.
									Use caution when changing server settings as they can stop your server from functioning.
									Changing some settings may require a web server restart.
								</p>
								<h3>URL Settings</h3>
								<p>
									URL Prefix<br/>
									<input type="text" name="URL_PREFIX" value="<%= Site.URL_PREFIX %>"/>
									<br/>
									URL Suffix<br/>
									<input type="text" name="URL_SUFFIX" value="<%= Site.URL_SUFFIX %>"/>
									<br/>
									Server Name<br/>
									<input type="text" name="SERVER_NAME" value="<%= Site.SERVER_NAME %>"/>
									<br/>
									Server Name (2)<br/>
									<input type="text" name="SERVER_NAME2" value="<%= Site.SERVER_NAME2 %>"/>
									<br/>
									URL<br/>
									<input type="text" name="URL" value="<%= Site.URL %>"/>
									<br/>
									URL Link<br/>
									<input type="text" name="URLLINK" value="<%= Site.URLLINK %>"/>
									<br/>
									Secure URL Link<br/>
									<input type="text" name="SECUREURLLINK" value="<%= Site.SECUREURLLINK %>"/>
									<br/>
									Sandbox URL Link<br/>
									<input type="text" name="SANDBOXURLLINK" value="<%= Site.SANDBOXURLLINK %>"/>
									<br/>
									Redirect<br/>
									<input type="text" name="REDIRECT" value="<%= Site.REDIRECT %>"/>
									<br/>
									<input type="checkbox" name="HTTPS" <%= Site.HTTPS ? "checked" : "" %>/> HTTPS
									<br/>
									<input type="checkbox" name="LOCK" <%= Site.LOCK ? "checked" : "" %>/> Lock Domain Name
									<br/>
									Python Server URL<br/>
									<input type="text" name="PYTHONSERVER" value="<%= Site.PYTHONSERVER %>"/>
									<br/>
								</p>
								
								<h3>Platform Settings</h3>
								<p>
									<input type="checkbox" name="BOOTSTRAP" <%= Site.BOOTSTRAP ? "checked" : "" %>/> Allow Bootstrap
									<br/>
									<input type="checkbox" name="READONLY" <%= Site.READONLY ? "checked" : "" %>/> Read-only
									<br/>
									<input type="checkbox" name="ADULT" <%= Site.ADULT ? "checked" : "" %>/> Disable Profanity Filter
									<br/>
									Default Content Rating<br/>
									<input type="text" name="CONTENT_RATING" value="<%= Site.CONTENT_RATING.toString() %>"/>
									<br/>
									Website Name<br/>
									<input type="text" name="NAME" value="<%= Site.NAME %>"/>
									<br/>
									Default Workspace Name<br/>
									<input type="text" name="DOMAIN" value="<%= Site.DOMAIN %>"/>
									<br/>
									ID<br/>
									<input type="text" name="ID" value="<%= Site.ID %>"/>
									<br/>
									JavaScript/css prefix<br/>
									<input type="text" name="PREFIX" value="<%= Site.PREFIX %>"/>
									<br/>
									Persistence Unit<br/>
									<input type="text" name="PERSISTENCE_UNIT" value="<%= Site.PERSISTENCE_UNIT %>"/>
									<br/>
									Persistence Protocol<br/>
									<input type="text" name="PERSISTENCE_PROTOCOL" value="<%= Site.PERSISTENCE_PROTOCOL %>"/>
									<br/>
									Twitter Hashtag<br/>
									<input type="text" name="HASHTAG" value="<%= Site.HASHTAG %>"/>
									<br/>
									Primary Content Type<br/>
									<input type="text" name="TYPE" value="<%= Site.TYPE %>"/>
									<br/>
									<input type="checkbox" name="DEDICATED" <%= Site.DEDICATED ? "checked" : "" %>/> Dedicated Platform
									<br/>
									<input type="checkbox" name="CLOUD" <%= Site.CLOUD ? "checked" : "" %>/> Cloud Platform
									<br/>
									<input type="checkbox" name="COMMERCIAL" <%= Site.COMMERCIAL ? "checked" : "" %>/> Commercial Platform
									<br/>
									<input type="checkbox" name="ALLOW_SIGNUP" <%= Site.ALLOW_SIGNUP ? "checked" : "" %>/> Allow Sign Up
									<br/>
									<input type="checkbox" name="VERIFYUSERS" <%= Site.VERIFYUSERS ? "checked" : "" %>/> Verify Users (require name/email on sign up)
									<br/>
									<input type="checkbox" name="VERIFY_EMAIL" <%= Site.VERIFY_EMAIL ? "checked" : "" %>/> Verify User Email (before allowing content creation)
									<br/>
									<input type="checkbox" name="ANONYMOUS_CHAT" <%= Site.ANONYMOUS_CHAT ? "checked" : "" %>/> Allow Anonymous Chat
									<br/>
									<input type="checkbox" name="REQUIRE_TERMS" <%= Site.REQUIRE_TERMS ? "checked" : "" %>/> Require Accept Terms (to chat)
									<br/>
									<input type="checkbox" name="AGE_RESTRICT" <%= Site.AGE_RESTRICT ? "checked" : "" %>/> Age Restrict (> 13)
									<br/>
									<input type="checkbox" name="BACKLINK" <%= Site.BACKLINK ? "checked" : "" %>/> Require Embedding Backlink
									<br/>
								</p>
								<h3>Services Settings</h3>
								<p>
									<input type="checkbox" name="TWITTER" <%= Site.TWITTER ? "checked" : "" %>/> Twitter
									<br/>
									<input type="checkbox" name="FACEBOOK" <%= Site.FACEBOOK ? "checked" : "" %>/> Facebook
									<br/>
									<input type="checkbox" name="TELEGRAM" <%= Site.TELEGRAM ? "checked" : "" %>/> Telegram
									<br/>
									<input type="checkbox" name="SLACK" <%= Site.SLACK ? "checked" : "" %>/> Slack
									<br/>
									<input type="checkbox" name="SKYPE" <%= Site.SKYPE ? "checked" : "" %>/> Skype
									<br/>
									<input type="checkbox" name="WECHAT" <%= Site.WECHAT ? "checked" : "" %>/> WeChat
									<br/>
									<input type="checkbox" name="KIK" <%= Site.KIK ? "checked" : "" %>/> Kik
									<br/>
									<input type="checkbox" name="EMAIL" <%= Site.EMAIL ? "checked" : "" %>/> Email
									<br/>
									<input type="checkbox" name="TIMERS" <%= Site.TIMERS ? "checked" : "" %>/> Timers
									<br/>
									<input type="checkbox" name="FORGET" <%= Site.FORGET ? "checked" : "" %>/> Forgetfulness
									<br/>
									<input type="checkbox" name="ADMIN" <%= Site.ADMIN ? "checked" : "" %>/> Admin
									<br/>
								</p>
								
								<h3>Email Settings</h3>
								<p>
									<input type="checkbox" name="WEEKLYEMAIL" <%= Site.WEEKLYEMAIL ? "checked" : "" %>/> Weekly Email (summary of active content)
									<br/>
									<input type="checkbox" name="WEEKLYEMAILBOTS" <%= Site.WEEKLYEMAILBOTS ? "checked" : "" %>/> Weekly Email Bots (include active bots in weekly email)
									<br/>
									<input type="checkbox" name="WEEKLYEMAILCHANNELS" <%= Site.WEEKLYEMAILCHANNELS ? "checked" : "" %>/> Weekly Email Channels (include active channels in weekly email)
									<br/>
									<input type="checkbox" name="WEEKLYEMAILFORUMS" <%= Site.WEEKLYEMAILFORUMS ? "checked" : "" %>/> Weekly Email Forums (include active forums in weekly email)
									<br/>
									Email Domain<br/>
									<input type="text" name="EMAILHOST" value="<%= Site.EMAILHOST %>"/>
									<br/>
									Sales Email<br/>
									<input type="text" name="EMAILSALES" value="<%= Site.EMAILSALES %>"/>
									<br/>
									Pay Pal Email<br/>
									<input type="text" name="EMAILPAYPAL" value="<%= Site.EMAILPAYPAL %>"/>
									<br/>
									Email Signature (user bot emails)<br/>
									<input type="text" name="SIGNATURE" value="<%= Site.SIGNATURE %>"/>
									<br/>
									Website Bot Email<br/>
									<input type="text" name="EMAILBOT" value="<%= Site.EMAILBOT %>"/>
									<br/>
									SMTP Host (website bot email)<br/>
									<input type="text" name="EMAILSMTPHost" value="<%= Site.EMAILSMTPHost %>"/>
									<br/>
									SMTP Port (website bot email)<br/>
									<input type="text" name="EMAILSMTPPORT" value="<%= Site.EMAILSMTPPORT %>"/>
									<br/>
									User (website bot email)<br/>
									<input type="text" name="EMAILUSER" value="<%= Site.EMAILUSER %>"/>
									<br/>
									Password (website bot email)<br/>
									<input type="password" name="EMAILPASSWORD" value="<%= Site.EMAILPASSWORD %>"/>
									<br/>
									<input type="checkbox" name="EMAILSSL" <%= Site.EMAILSSL ? "checked" : "" %>/> SSL (website bot email)
									<br/>
								</p>
								
								<h3>Limits</h3>
								<p>
									Bot Knowledge Limit<br/>
									<input type="text" name="MEMORYLIMIT" value="<%= Site.MEMORYLIMIT %>"/>
									<br/>
									Bot Max Script Process Time<br/>
									<input type="text" name="MAX_PROCCESS_TIME" value="<%= Site.MAX_PROCCESS_TIME %>"/>
									<br/>
									User Content Limit<br/>
									<input type="text" name="CONTENT_LIMIT" value="<%= Site.CONTENT_LIMIT %>"/>
									<br/>
									Max Bot Creates(per IP per day)<br/>
									<input type="text" name="MAX_CREATES_PER_IP" value="<%= Site.MAX_CREATES_PER_IP %>"/>
									<br/>
									Max User Messages (per IP per day)<br/>
									<input type="text" name="MAX_USER_MESSAGES" value="<%= Site.MAX_USER_MESSAGES %>"/>
									<br/>
									Max Upload Size<br/>
									<input type="text" name="MAX_UPLOAD_SIZE" value="<%= Site.MAX_UPLOAD_SIZE %>"/>
									<br/>
									Max Live Chat Message (stored in database, per channel)<br/>
									<input type="text" name="MAX_LIVECHAT_MESSAGES" value="<%= Site.MAX_LIVECHAT_MESSAGES %>"/>
									<br/>
									Max Attachments (stored in database, per channel/bot/content)<br/>
									<input type="text" name="MAX_ATTACHMENTS" value="<%= Site.MAX_ATTACHMENTS %>"/>
									<br/>
									Max Translations (stored in database)<br/>
									<input type="text" name="MAX_TRANSLATIONS" value="<%= Site.MAX_TRANSLATIONS %>"/>
									<br/>
									URL Timeout<br/>
									<input type="text" name="URL_TIMEOUT" value="<%= Site.URL_TIMEOUT %>"/>
									<br/>
									Max API (per user, per day)<br/>
									<input type="text" name="MAX_API" value="<%= Site.MAX_API %>"/>
									<br/>
									Bronze Max API (per user, per day)<br/>
									<input type="text" name="MAX_BRONZE" value="<%= Site.MAX_BRONZE %>"/>
									<br/>
									Gold Max API (per user, per day)<br/>
									<input type="text" name="MAX_GOLD" value="<%= Site.MAX_GOLD %>"/>
									<br/>
									Platinum Max API (per user, per day)<br/>
									<input type="text" name="MAX_PLATINUM" value="<%= Site.MAX_PLATINUM %>"/>
									<br/>
									Max Bot Cache<br/>
									<input type="text" name="MAX_BOT_CACHE_SIZE" value="<%= Site.MAX_BOT_CACHE_SIZE %>"/>
									<br/>
									Max Bot Pool<br/>
									<input type="text" name="MAX_BOT_POOL_SIZE" value="<%= Site.MAX_BOT_POOL_SIZE %>"/>
									<br/>
									Max Tweet Import<br/>
									<input type="text" name="MAXTWEETIMPORT" value="<%= Site.MAXTWEETIMPORT %>"/>
									<br/>
								</p>
								
								<h3>API & Service Keys</h3>
								<p>
									Platform Encryption Key<br/>
									<input type="password" name="KEY" value="<%= Site.KEY %>"/>
									<br/>
									Platform Upgrade Secret<br/>
									<input type="password" name="UPGRADE_SECRET" value="<%= Site.UPGRADE_SECRET %>"/>
									<br/>
									Twitter API Auth Key<br/>
									<input type="password" name="TWITTER_OAUTHKEY" value="<%= Site.TWITTER_OAUTHKEY %>"/>
									<br/>
									Twitter API Auth Secret<br/>
									<input type="password" name="TWITTER_OAUTHSECRET" value="<%= Site.TWITTER_OAUTHSECRET %>"/>
									<br/>
									Facebook API App ID<br/>
									<input type="password" name="FACEBOOK_APPID" value="<%= Site.FACEBOOK_APPID %>"/>
									<br/>
									Facebook API App Secret<br/>
									<input type="password" name="FACEBOOK_APPSECRET" value="<%= Site.FACEBOOK_APPSECRET %>"/>
									<br/>
									Google API Key<br/>
									<input type="password" name="GOOGLEKEY" value="<%= Site.GOOGLEKEY %>"/>
									<br/>
									Google API Client ID<br/>
									<input type="password" name="GOOGLECLIENTID" value="<%= Site.GOOGLECLIENTID %>"/>
									<br/>
									Google API Client Secret<br/>
									<input type="password" name="GOOGLECLIENTSECRET" value="<%= Site.GOOGLECLIENTSECRET %>"/>
									<br/>
									Microsoft Speech API Key<br/>
									<input type="password" name="MICROSOFT_SPEECH_KEY" value="<%= Site.MICROSOFT_SPEECH_KEY %>"/>
									<br/>
									Yandex API Key<br/>
									<input type="password" name="YANDEX_KEY" value="<%= Site.YANDEX_KEY %>"/>
									<br/>
								</p>
				
								<input type="submit" name="settings" value="Save"/>
							</form>
						</div>
						<div id="tabs-3" class="ui-tabs-hide">
							<p>
								<a href="create-user.jsp">Create user</a><br/><br/>
								<a href="browse-user.jsp">Browse users</a><br/>
							</p>
						</div>
						<div id="tabs-4" class="ui-tabs-hide">
							<form action="super" method="post" class="message">
								<%= loginBean.postTokenInput() %>
								<input type="submit" name="freeMemory" value="Free Memory"/>
								<input type="submit" name="dropDead" value="Drop Dead"/>
								<input type="submit" name="archiveInactive" value="Archive Inactive"/>
								<input type="submit" name="migrate" value="Migrate"/>
								<input type="submit" name="runForgetfullness" value="Run Forgetfullness"/>
								<br/>
								<input type="submit" name="initDatabase" value="Init Database"/>
								<input type="submit" name="translations" value="Translation"/>
								<input type="submit" name="clearBanned" value="Clear Banned"/>
								<input type="submit" name="cleanupJunk" value="Cleanup Junk"/>
								<br/>
							</form>
							
							<h3>Banned IPs</h3>
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
							Free memory: <%= NumberFormat.getInstance().format(Runtime.getRuntime().freeMemory() / 1000000) %> meg <br/>
							Max memory: <%= NumberFormat.getInstance().format(Runtime.getRuntime().maxMemory() / 1000000) %> meg <br/>
							Total memory: <%= NumberFormat.getInstance().format(Runtime.getRuntime().totalMemory() / 1000000) %> meg <br/>
							Total threads: <%= Thread.getAllStackTraces().keySet().size() %> <br/>
							Active threads: 
							<%
								int active = 0;
								for (Thread thread : Thread.getAllStackTraces().keySet()) {
									if (thread.getState() == Thread.State.RUNNABLE) {
										active++;
									}
								}
							%> <%= active %><br/>
							<br/>
							Admin service: <%= AdminService.instance() == null ? "null" : AdminService.instance().getChecker().isAlive() %><br/>
							Forgetfulness service: <%= ForgetfulnessService.instance() == null ? "null" : ForgetfulnessService.instance().getChecker().isAlive() %> : last run <%= String.valueOf(ForgetfulnessService.lastRun) %><br/>
							<% boolean email = EmailService.instance() != null && EmailService.instance().getChecker() != null; %>
							Email service: <%= !email ? "null" : EmailService.instance().getChecker().isAlive() %>
								 bronze: <%= !email ? "null" : (EmailService.instance().getBronzeChecker() != null && EmailService.instance().getBronzeChecker().isAlive()) %>
								 gold: <%= !email ? "null" : (EmailService.instance().getGoldChecker() != null && EmailService.instance().getGoldChecker().isAlive()) %>
								 platinum: <%= !email ? "null" : (EmailService.instance().getPlatinumChecker() != null && EmailService.instance().getPlatinumChecker().isAlive()) %> <br/>
							<% boolean facebook = FacebookService.instance() != null && FacebookService.instance().getChecker() != null; %>
							Facebook service: <%= !facebook ? "null" : FacebookService.instance().getChecker().isAlive() %>
								 bronze: <%= !facebook ? "null" : (FacebookService.instance().getBronzeChecker() != null && FacebookService.instance().getBronzeChecker().isAlive()) %>
								 gold: <%= !facebook ? "null" : (FacebookService.instance().getGoldChecker() != null && FacebookService.instance().getGoldChecker().isAlive()) %>
								 platinum: <%= !facebook ? "null" : (FacebookService.instance().getPlatinumChecker() != null && FacebookService.instance().getPlatinumChecker().isAlive()) %> <br/>
							<% boolean twitter = TwitterService.instance() != null && TwitterService.instance().getChecker() != null; %>
							Twitter service: <%= !twitter ? "null" : TwitterService.instance().getChecker().isAlive() %>
								 bronze: <%= !twitter ? "null" : (TwitterService.instance().getBronzeChecker() != null && TwitterService.instance().getBronzeChecker().isAlive()) %>
								 gold: <%= !twitter ? "null" : (TwitterService.instance().getGoldChecker() != null && TwitterService.instance().getGoldChecker().isAlive()) %>
								 platinum: <%= !twitter ? "null" : (TwitterService.instance().getPlatinumChecker() != null && TwitterService.instance().getPlatinumChecker().isAlive()) %> <br/>
							</p>
							<% boolean telegram = TelegramService.instance() != null && TelegramService.instance().getChecker() != null; %>
							Telegram service: <%= !telegram ? "null" : TelegramService.instance().getChecker().isAlive() %>
								 bronze: <%= !telegram ? "null" : (TelegramService.instance().getBronzeChecker() != null && TelegramService.instance().getBronzeChecker().isAlive()) %>
								 gold: <%= !telegram ? "null" : (TelegramService.instance().getGoldChecker() != null && TelegramService.instance().getGoldChecker().isAlive()) %>
								 platinum: <%= !telegram ? "null" : (TelegramService.instance().getPlatinumChecker() != null && TelegramService.instance().getPlatinumChecker().isAlive()) %> <br/>
							</p>
							<h3>Bot instance cache</h3>
							<p>
							Count: <%= Bot.getInstances().size() %>
							</p>
							<table id="botinstancecache" class="tablesorter">
								<thead>
								<tr>
									<th>Name</th>
									<th>Database</th>
								</tr>
								</thead>
								<% for (Bot instance : Bot.getInstances().values()) { %>
									<tr><td><%= instance.getName() %></td><td><%= instance.memory().getMemoryName() %></td></tr>
								<% } %>
							</table>
							<h3>Bot cache</h3>
							<p>
							Count: <%= BotManager.manager().getInstances().size() %> : <%= BotManager.manager().getQueue().size() %><br/>
							Scavenges: <%= BotManager.manager().getScavenges() %> : alive: <%= BotManager.manager().getScavenger().isAlive() %> : scavenging: <%= BotManager.manager().isScavenging() %><br/>
							<%
								long maxAge = 0;
								for (InstanceManager.InstanceInfo info : BotManager.manager().getInstances().values()) {
									if (maxAge == 0 || info.age < maxAge) {
										maxAge = info.age;
									}
								}
							%>
							Max Age: <%= maxAge == 0 ? "none" : new java.sql.Timestamp(maxAge) %>
							</p>
							<table id="botcache" class="tablesorter">
								<thead>
								<tr>
									<th>Name</th>
									<th>Database</th>
									<th>Age</th>
									<th>Pooled</th>
									<th>Objects</th>
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
							<h3>Bean cache</h3>
							<p>
							Count: <%= BeanManager.manager().getInstances().size() %> : <%= BeanManager.manager().getQueue().size() %><br/>
							Scavenges: <%= BeanManager.manager().getScavenges() %> : alive: <%= BeanManager.manager().getScavenger().isAlive() %> : scavenging: <%= BotManager.manager().isScavenging() %><br/>
							<% if (BeanManager.manager().isDeadlocked()) { %>
								<span style="color:red;font-weight:bold;font-size:30px">DEADLOCKED!</span><br/>
							<% } %>
							</p>
							<h3>Sessions</h3>
							<p>
							Count: <%= DatabaseMemory.sessions.size() %>
							</p>
							<table id="sessions" class="tablesorter">
								<thead>
								<tr>
									<th>URL</th>
									<th>Count</th>
									<th>Objects</th>
									<th>Relationships</th>
									<th>Read Connections</th>
									<th>Write Connections</th>
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
							<h3>JPA</h3>
							<p>
							Count: <%= SessionManager.getManager().getSessions().size() %>
							</p>
							<table id="jpa" class="tablesorter">
								<thead>
								<tr>
									<th>URL</th>
									<th>Objects</th>
									<th>Relationships</th>
									<th>Read Connections</th>
									<th>Write Connections</th>
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