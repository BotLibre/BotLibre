<%@page import="org.botlibre.thought.language.Language.LanguageState"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.TelegramBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% TelegramBean bean = loginBean.getBean(TelegramBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Telegram") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Connect your bot to Telegram and configure Telegram properties") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("telegram, bot, telegram bot, telegram automation, social media, mobile") %>"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Telegram tab allows you to connect your bot to") %> <a href="https://telegram.org" target="_blank">Telegram</a>.<br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-telegram.jsp"><%= loginBean.translate("Docs") %></a> : <a target="_blank" href="https://www.botlibre.com/forum-post?id=12635785"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/telegram1.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow bot to manage a Telegram channel, group, or chat on Telegram.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Telegram") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The Telegram tab allows you to connect your bot to") %> <a href="https://telegram.org" target="_blank">Telegram</a>.
					<br/>
					<%= loginBean.translate("See") %> <a target="_blank" href="https://www.botlibre.com/forum-post?id=12635785" class="blue"><%= loginBean.translate("Automate your Telegram presence with your own Telegram bot") %></a> <%= loginBean.translate("for more information") %>.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<% if (bean.isConnected()) { %>
					<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the Telegram account") %> <i><%= bean.getUserName() %></i><br/>
					To see <%= botBean.getInstanceName() %>'s <%= loginBean.translate("profile goto") %> <a style="color:grey" target="_blank" href="https://telegram.me/<%= bean.getUserName() %>">telegram.me/<%= bean.getUserName() %></a>
				<% } else { %>
					<p class="help">
						<%= loginBean.translate("The Telegram tab allows you to connect your bot to") %> <a href="https://telegram.org" target="_blank">Telegram</a>.
						<br/>
						<%= loginBean.translate("See") %> <a href="https://www.botlibre.com/forum-post?id=12635785" target="_blank" class="blue"><%= loginBean.translate("Automate your Telegram presence with your own Telegram bot") %></a> <%= loginBean.translate("for more information") %>.
					</p>
					<br/>
					<%= botBean.getMustBeAdminMessage() %>
				<% } %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or to violate the Telegram's terms of service.") %>
				</p>
				<form action="telegram" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<% if (bean.isConnected()) { %>
						<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the Telegram account") %> <i><%= bean.getUserName() %></i><br/>
						<%= loginBean.translate("To see") %> <%= botBean.getInstanceName() %>'s <%= loginBean.translate("profile goto") %> <a style="color:grey" target="_blank" href="https://telegram.me/<%= bean.getUserName() %>">telegram.me/<%= bean.getUserName() %></a>
						<br/>
						<input id="cancel" type="submit" name="disconnect" value="<%= loginBean.translate("Disconnect") %>"/>
						<input type="submit" name="check" value="<%= loginBean.translate("Check Messages") %>" title="<%= loginBean.translate("Have the bot check its Telegram messages") %>"/>
					<% } else { %>
						<%= botBean.instanceInput() %>
						<%= loginBean.translate("To create a bot on Telegram you need send the message '/newbot' to the") %> <a style="color:grey" target="_blank" href="https://telegram.me/BotFather">@BotFather</a><br/>
						<br/>
						<%= loginBean.translate("Enter bot's token (from Telegram)") %><br/>
						<input autofocus type="text" name="token" value="<%= bean.getToken() %>" /><br/>								
						<input type="submit" name="connect" value="<%= loginBean.translate("Connect") %>" title="<%= loginBean.translate("Connect your bot to Telegram") %>"/>
					<% } %>
					<br/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("You do not need to enter this, just click Connect") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Telegram Bot") %><br/>
					<input type="text" name="user" value="<%= bean.getUserName() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("You do not need to enter this, just click Connect") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Bot Token (from Telegram)") %><br/>
					<input type="text" name="token" value="<%= bean.getToken() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enable realtime messages and then click Connect to automatically send your webhook to Telegram") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Webhook URL") %><br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>"/><br/>

				</form>
				<form action="telegram" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<h3><%= loginBean.translate("Telegram Bot Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should poll for new messages and reply.") %><br/>
						</div>
					</span>
					<input name="messages" type="radio" value="poll" <% if (bean.getCheckMessages()) { %>checked<% } %> /><%= loginBean.translate("Check messages (poll)") %><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should use a webhook to reply to messages in realtime.") %><br/>
						</div>
					</span>
					<input name="messages" type="radio" value="webhook" <% if (bean.getRealtimeMessages()) { %>checked<% } %> /><%= loginBean.translate("Realtime messages (webhook)") %><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should remove the button text from the message.") %><br/>
						</div>
					</span>
					<input type="checkbox" name="stripButtonText" <% if (bean.getStripButtonText()) { %>checked<% } %> ><%= loginBean.translate("Remove Button Text") %></input><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the entire JSON message from Telegram should be stored in the bot's input and conversation log (and be accessible in scripts).") %><br/>
						</div>
					</span>
					<input type="checkbox" name="trackMessageObjects" <% if (bean.getTrackMessageObjects()) { %>checked<% } %> ><%= loginBean.translate("Track Message Objects") %></input><br/>

					<h3><%= loginBean.translate("Group Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("If the bot is added to a group, by default it will only reply to messages it has a good response to, or sent to its user id.") %><br/>
							<ul>
								<li><%= loginBean.translate("Ignore") %>: <%= loginBean.translate("the bot will ignore and not process all group messages") %>.
								<li><%= loginBean.translate("ListeningOnly") %>: <%= loginBean.translate("the bot will listen and process messages, but never reply") %>.
								<li><%= loginBean.translate("Listening") %>: <%= loginBean.translate("the bot will listen and process messages, but only reply to messages sent to its user id") %>.
								<li><%= loginBean.translate("Discussion") %>: <%= loginBean.translate("(default) the bot will reply to messages it has a good response to, or sent to its user id") %>.
								<li><%= loginBean.translate("Conversational") %>: <%= loginBean.translate("the bot will reply to all messages") %>.
							</ul>
						</div>
					</span>
					<%= loginBean.translate("Group Reply Mode") %>
					<select name="groupMode">
						<option value="Ignore" <%= bean.getGroupModeCheckedString(LanguageState.Ignore) %>><%= loginBean.translate("Ignore") %></option>
						<option value="ListeningOnly" <%= bean.getGroupModeCheckedString(LanguageState.ListeningOnly) %>><%= loginBean.translate("Listening Only") %></option>
						<option value="Listening" <%= bean.getGroupModeCheckedString(LanguageState.Listening) %>><%= loginBean.translate("Listening") %></option>
						<option value="Discussion" <%= bean.getGroupModeCheckedString(LanguageState.Discussion) %>><%= loginBean.translate("Discussion") %></option>
						<option value="Conversational" <%= bean.getGroupModeCheckedString(LanguageState.Conversational) %>><%= loginBean.translate("Conversational") %></option>
					</select>

					<h3><%= loginBean.translate("Channel Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("You can have your bot manage a channel, and post from an RSS feed, or auto post.") %><br/>
							<%= loginBean.translate("Enter your channel's name, and ensure your bot has been added as a channel administrator.") %>
						</div>
					</span>
					<%= loginBean.translate("Channel") %><br/>
					<input type="text" name="channel" value="<%= bean.getChannel() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Automatically post content from the RSS feeds to a channel.") %><br/>
							<%= loginBean.translate("List each feed separated by a new line.") %><br/>
							<%= loginBean.translate("You can include a prefix and/or a suffix to append to the RSS title.") %><br/>
							i.e.
<pre>Blog http://acme.blogspot.com/feeds/posts/default #blog
Tech News http://feeds.reuters.com/reuters/technologyNews #tech #news
</pre>
							<%= loginBean.translate("Ensure your URL is a valid RSS feed (XML, not HTML).") %>
						</div>
					</span>
					<%= loginBean.translate("RSS Feeds") %><br/>
					<textarea name="postRSS" ><%= bean.getPostRSS() %></textarea><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Only post RSS feeds that contain one of the keywords set in their title.") %><br/>
							<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %><br/>
							i.e.
<pre>chat bot
chatterbot
</pre>
						</div>
					</span>
					<%= loginBean.translate("RSS Keywords") %><br/>
					<textarea name="rssKeywords"><%= bean.getRSSKeyWords() %></textarea><br/>

					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should post automatically every set number of hours.") %><br/>
						</div>
					</span>
					<input name="autoPost" type="checkbox" <% if (bean.getAutoPost()) { %>checked<% } %> /><%= loginBean.translate("Auto Post") %><br/>

					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("The number of hours to wait between auto posts.") %>
						</div>
					</span>
					<%= loginBean.translate("Auto Post Hours") %><br/>
					<input type="number" name="autoPostHours" value="<%= bean.getAutoPostHours() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set of posts to auto post.") %><br/>
							<%= loginBean.translate("List each post separated by a new line.") %><br/>
							<%= loginBean.translate("Self and AIML templates can be used") %><br/>
							i.e.
<pre>How is everyone today?
Template("The current time is {Date.time()}")
&lt;template&gt;It is &lt;date/&gt;, and &lt;srai&gt;How are you&lt;/srai&gt;&lt;/template&gt;
</pre>
						</div>
					</span>
					<%= loginBean.translate("Auto Posts") %><br/>
					<textarea name="autoPosts"><%= bean.getAutoPosts() %></textarea><br/>

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
