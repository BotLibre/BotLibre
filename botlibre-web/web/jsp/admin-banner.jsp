<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@ page import = "org.botlibre.web.bean.BotBean" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	BotBean bean = loginBean.getBotBean();
%>

<script>
	var testBot = function() {
		SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
		var tsdk = new SDKConnection();
		var tuser = new UserConfig();
		<% if (loginBean.isLoggedIn()) { %>
			tuser.user = "<%= loginBean.getUser().getUserId() %>";
			tuser.token = "<%= loginBean.getUser().getToken() %>";
			tsdk.user = tuser;
		<% } %>
		var tweb = new WebChatbotListener();
		tweb.connection = tsdk;
		tweb.instance = "<%= bean.getInstanceId() %>";
		tweb.debug = true;
		tweb.popup();
	}
</script>
<% if (!embed) { %>
	<% bean.browseBannerHTML(out, proxy); %>
<% } %>
<% if (!loginBean.isMobile()) { %>
	<div id="admin-topper-banner" align="left">
		<div class="clearfix">
		
		<div class="toolbar">
			<span class="dropt">
				<div class="menu">
					<table>
						<% if (bean.getInstance() == null || !bean.getInstance().isExternal()) { %>
							<tr class="menuitem">
								<td><a href="admin-users.jsp" title="<%= loginBean.translate("Configure the users and administrators of your bot") %>" class="menuitem">
									<img src="images/user1.png" class="menu"/> <%= loginBean.translate("Users") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="instance-avatar.jsp" title="<%= loginBean.translate("Upload and configure images for bots emotions and responses") %>" class="menuitem">
									<img src="images/avatar1.png" class="menu"/> <%= loginBean.translate("Avatar") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="voice.jsp" title="<%= loginBean.translate("Configure your bot's language and voice") %>" class="menuitem">
									<img src="images/voice1.png" class="menu"/> <%= loginBean.translate("Voice") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="learning.jsp" title="<%= loginBean.translate("Configure your bot's learning ability and other settings") %>" class="menuitem">
									<img src="images/learning.png" class="menu"/> <%= loginBean.translate("Learning & Settings") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="chatlogs.jsp" title="<%= loginBean.translate("Train your bot's responses, view its converstions, import and export chat logs") %>" class="menuitem">
									<img src="images/chatlog1.png" class="menu"/> <%= loginBean.translate("Training & Chat Logs") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="self.jsp" title="<%= loginBean.translate("Add, create, edit, import, and export scripting programs") %>" class="menuitem">
									<img src="images/script1.png" class="menu"/> <%= loginBean.translate("Scripts") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="twitter.jsp" title="<%= loginBean.translate("Allow bot to manage a Twitter account and interact with other Twitter users") %>" class="menuitem">
									<img src="images/twitter1.png" class="menu"/> <%= loginBean.translate("Twitter") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="facebook.jsp" title="<%= loginBean.translate("Allow bot to manage a Facebook account or page and interact with other Facebook users") %>" class="menuitem">
									<img src="images/facebook4.png" class="menu"/> <%= loginBean.translate("Facebook") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="telegram.jsp" title="<%= loginBean.translate("Allow bot to manage a Telegram channel, group, or chat on Telegram") %>" class="menuitem">
									<img src="images/telegram1.png" class="menu"/> <%= loginBean.translate("Telegram") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="slack.jsp" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to Slack messages") %>" class="menuitem">
									<img src="images/slack1.png" class="menu"/> <%= loginBean.translate("Slack") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="skype.jsp" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to Skype messages") %>" class="menuitem">
									<img src="images/skype1.png" class="menu"/> <%= loginBean.translate("Skype") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="wechat.jsp" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to WeChat messages") %>" class="menuitem">
									<img src="images/wechat1.png" class="menu"/> <%= loginBean.translate("WeChat") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="kik.jsp" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to Kik messages") %>" class="menuitem">
									<img src="images/kik.png" class="menu"/> <%= loginBean.translate("Kik") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="email.jsp" title="<%= loginBean.translate("Allow bot to manage an email account and answer emails") %>" class="menuitem">
									<img src="images/email.png" class="menu"/> <%= loginBean.translate("Email") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="sms.jsp" title="<%= loginBean.translate("Allow bot to send, receive, and reply to SMS messages") %>" class="menuitem">
									<img src="images/twilio.svg" class="menu"/> <%= loginBean.translate("Twilio SMS & IVR") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="google.jsp" title="<%= loginBean.translate("Allow your bot to connect to Google services such as Google Calendar.") %>" class="menuitem">
									<img src="images/google.png" class="menu"/> <%= loginBean.translate("Google") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="wolframalpha.jsp" title="<%= loginBean.translate("Allow your bot to connect to Wolfram Alpha services.") %>" class="menuitem">
									<img src="images/wolframalpha1.png" class="menu"/> <%= loginBean.translate("Wolfram Alpha") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="alexa.jsp" title="<%= loginBean.translate("Allow your bot to connect to Amazon Alexa.") %>" class="menuitem">
									<img src="images/alexa1.png" class="menu"/> <%= loginBean.translate("Alexa") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="google-assistant.jsp" title="<%= loginBean.translate("Allow your bot to connect to Google Assistant.") %>" class="menuitem">
									<img src="images/google-assistant1.png" class="menu"/> <%= loginBean.translate("Google Assistant") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="irc.jsp" title="<%= loginBean.translate("Allow bot to chat with others on IRC channels") %>" class="menuitem">
									<img src="images/irc.png" class="menu"/> <%= loginBean.translate("IRC") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="timers.jsp" title="<%= loginBean.translate("Setup your bot to run scripts at various time intervals to automate web tasks.") %>" class="menuitem">
									<img src="images/timers.png" class="menu"/> <%= loginBean.translate("Timers") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="import.jsp" title="<%= loginBean.translate("Import data from the web") %>" class="menuitem">
									<img src="images/web.png" class="menu"/> <%= loginBean.translate("Web") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="memory.jsp" title="<%= loginBean.translate("Browse the bot's knowledge database") %>" class="menuitem">
									<img src="images/knowledge.png" class="menu"/> <%= loginBean.translate("Knowledge") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="log.jsp" title="<%= loginBean.translate("View the bot's log for errors and debugging info") %>" class="menuitem">
									<img src="images/log.png" class="menu"/> <%= loginBean.translate("Log") %></a>
								</td>
							</tr>
						<% } else { %>
							<tr class="menuitem">
								<td><a href="instance-avatar.jsp" title="<%= loginBean.translate("Upload and configure images for bots emotions and responses") %>" class="menuitem">
									<img src="images/avatar1.png" class="menu"/> <%= loginBean.translate("Avatar") %></a>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="voice.jsp" title="<%= loginBean.translate("Configure your bot's language and voice") %>" class="menuitem">
									<img src="images/voice1.png" class="menu"/> <%= loginBean.translate("Voice") %></a>
								</td>
							</tr>
						<% } %>
					</table>
				</div>
				<a href="admin.jsp" title="<%= loginBean.translate("Admin Console") %>"><img class="admin-banner-pic" src="images/admin.svg"></a>
			</span>
			<% if (bean.getInstance() == null || !bean.getInstance().isExternal()) { %>
				<a href="admin-users.jsp" class="shrinkhide1000" title="<%= loginBean.translate("Configure the users and administrators of your bot") %>"><img src="images/user1.png" class="admin-banner-pic"></a> 
				<a href="instance-avatar.jsp" class="shrinkhide" title="<%= loginBean.translate("Configure your bot's appearance. Choose an animated avatar, or create your own") %>"><img src="images/avatar1.png" class="admin-banner-pic"></a> 
				<a href="voice.jsp" class="shrinkhide1000" title="<%= loginBean.translate("Configure your bot's language and voice") %>"><img src="images/voice1.png" class="admin-banner-pic"></a> 
				<a href="learning.jsp" class="shrinkhide1000" title="<%= loginBean.translate("Configure your bot's learning ability and other settings") %>"><img src="images/learning.png" class="admin-banner-pic"></a> 
				<a href="chatlogs.jsp" title="<%= loginBean.translate("Train your bot's responses, view its converstions, import and export chat logs") %>"><img src="images/chatlog1.png" class="admin-banner-pic"></a> 
				<a href="self.jsp" title="<%= loginBean.translate("Add, create, edit, import, and export scripting programs") %>"><img src="images/script1.png" class="admin-banner-pic"></a> 
				<a href="twitter.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow bot to manage a Twitter account and interact with other Twitter users") %>"><img src="images/twitter1.png" class="admin-banner-pic"></a> 
				<a href="facebook.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow bot to manage a Facebook account or page and interact with other Facebook users") %>"><img src="images/facebook4.png" class="admin-banner-pic"></a> 
				<a href="telegram.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow bot to manage a Telegram channel or chat on Telegram") %>"><img src="images/telegram1.png" class="admin-banner-pic"></a> 
				<a href="slack.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to Slack messages") %>"><img src="images/slack1.png" class="admin-banner-pic"></a> 
				<a href="skype.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to Skype messages") %>"><img src="images/skype1.png" class="admin-banner-pic"></a>
				<a href="wechat.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to WeChat messages") %>"><img src="images/wechat1.png" class="admin-banner-pic"></a>
				<a href="kik.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to send, receive, and reply to Kik messages") %>"><img src="images/kik.png" class="admin-banner-pic"></a>
				<a href="email.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow bot to manage an email account and answer emails") %>"><img src="images/email.png" class="admin-banner-pic"></a> 
				<a href="sms.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow bot to send, receive, and reply to SMS messages and voice calls") %>"><img src="images/twilio.svg" class="admin-banner-pic"></a> 
				<a href="google.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to connect to Google services such as Google Calendar.") %>"><img src="images/google.png" class="admin-banner-pic"></a> 
				<a href="wolframalpha.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to connect to Wolfram Alpha services.") %>"><img src="images/wolframalpha1.png" class="admin-banner-pic"></a> 
				<a href="alexa.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to connect to Alexa.") %>"><img src="images/alexa1.png" class="admin-banner-pic"></a> 
				<a href="google-assistant.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow your bot to connect to Google Assistant.") %>"><img src="images/google-assistant1.png" class="admin-banner-pic"></a> 
				<a href="irc.jsp" class="shrinkhide1200" title="<%= loginBean.translate("Allow bot to chat with others on IRC channels") %>"><img src="images/irc.png" class="admin-banner-pic"></a> 
				<a href="timers.jsp" class="shrinkhide1000" title="<%= loginBean.translate("Setup your bot to run scripts at various time intervals to automate web tasks.") %>"><img src="images/timers.png" class="admin-banner-pic"></a>
				<a href="import.jsp" class="shrinkhide1000" title="<%= loginBean.translate("Import data from the web") %>"><img src="images/web.png" class="admin-banner-pic"></a> 
				<a href="memory.jsp" title="<%= loginBean.translate("Browse the bot's knowledge database") %>"><img src="images/knowledge.png" class="admin-banner-pic"></a> 
				<a href="log.jsp" title="<%= loginBean.translate("View the bot's log for errors and debugging info") %>"><img src="images/log.png" class="admin-banner-pic"></a>
				<a href="#" onclick="testBot(); return false;" title="<%= loginBean.translate("Test chatting with the bot") %>"><img src="images/green_bot_button128.png" class="admin-banner-pic"></a>
			<% } else { %>
				<a href="instance-avatar.jsp" title="<%= loginBean.translate("Upload and configure images for bots emotions and responses") %>"><img src="images/avatar1.png" class="admin-banner-pic"></a> 
				<a href="voice.jsp" title="<%= loginBean.translate("Configure your bot's language and voice") %>"><img src="images/voice1.png" class="admin-banner-pic"></a>
			<% } %>
			</div>
		</div>
	</div>
<% } else { %>
	<div id="admin-topper-banner" align="left">
		<div class="clearfix">
			<div class="toolbar">
				<a href="admin.jsp" title="<%= loginBean.translate("") %>"><img src="images/admin.svg" class="admin-banner-pic"/></a>
				<a href="chatlogs.jsp" title="<%= loginBean.translate("Train your bot's responses, view its converstions, import and export chat logs") %>"><img src="images/chatlog1.png" class="admin-banner-pic"></a> 
				<a href="self.jsp" title="<%= loginBean.translate("Add, create, edit, import, and export scripting programs") %>"><img src="images/script1.png" class="admin-banner-pic"></a> 
				<a href="memory.jsp" title="<%= loginBean.translate("Browse the bot's knowledge database") %>"><img src="images/knowledge.png" class="admin-banner-pic"></a> 
				<a href="#" onclick="testBot(); return false;" title="<%= loginBean.translate("Test chatting with the bot") %>"><img src="images/green_bot_button128.png" class="admin-banner-pic"></a>
			</div>
		</div>
	</div>
<% } %>

<script>
	window.addEventListener("scroll", function() {scrollAdminTopper()});
	var isAdminScrolled = false;
	
	function scrollAdminTopper() {
		if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
			if (!isAdminScrolled) {
				isAdminScrolled = true;
				var adminTopper = document.getElementById("admin-topper-banner");
				adminTopper.style.position = "fixed";
				adminTopper.style.width = "100%";
				adminTopper.style.top = "40px";
				adminTopper.style.zIndex = 10;

				var chatlogTopper = document.getElementById("chatlog-topper-banner");
				if (chatlogTopper != null) {
					chatlogTopper.style.position = "fixed";
					chatlogTopper.style.width = "100%";
					chatlogTopper.style.top = "77px";
					chatlogTopper.style.zIndex = 10;
				}
			}
		} else {
			if (isAdminScrolled) {
				isAdminScrolled = false;
				var adminTopper = document.getElementById("admin-topper-banner");
				adminTopper.style.position = "relative";
				adminTopper.style.width = null;
				adminTopper.style.top = null;

				var chatlogTopper = document.getElementById("chatlog-topper-banner");
				if (chatlogTopper != null) {
					chatlogTopper.style.position = "relative";
					chatlogTopper.style.width = null;
					chatlogTopper.style.top = null;
				}
			}
		}
	}
</script>
