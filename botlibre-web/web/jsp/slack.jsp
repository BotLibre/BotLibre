<%@page import="org.botlibre.web.bean.SlackBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% SlackBean bean = loginBean.getBean(SlackBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Slack - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Slack"/>	
	<meta name="keywords" content="slack, mobile, texting, bot, slack bot, slack automation"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Slack tab allows you to connect your bot to Slack.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-slack.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=13979789"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/slack1.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to Slack messages.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Slack") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					The Slack tab allows you to connect your bot to Slack.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					The Slack tab allows you to connect your bot to Slack.
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					Connect your bot to a <a href="https://www.slack.com/" target="_blank">Slack</a> account.
				</p>
				<h3><%= loginBean.translate("Slack Outgoing Webhook Properties") %></h3>
				<p>
					<%= loginBean.translate("Register your webhook on the Outgoing WebHooks configuration page on the Slack website.") %>
				</p>
				
				<form action="slack" method="post" class="message">
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set this URL on the Outgoing WebHooks configuration page on the Slack website to enable replying to messages on Slack.") %>	
						</div>
					</span>
					Slack Outgoing Webhook URL<br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
				</form>
				<form action="slack" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter the Token from the Outgoing WebHook configuration page on the Slack website.") %>
						</div>
					</span>
					<%= loginBean.translate("Slack Outgoing WebHook Token") %><br/>
					<input type="text" name="token" value="<%= bean.getToken() %>"/><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter the username from the Outgoing WebHook configuration page on the Slack website.") %>
						</div>
					</span>
					<%= loginBean.translate("Slack Bot Username") %><br/>
					<input type="text" name="botUsername" value="<%= bean.getBotUsername() %>"/><br/>

					<h3><%= loginBean.translate("Slack Incoming Webhook Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter the WebHook URL from the Incoming WebHook configuration page on the Slack website.") %>
						</div>
					</span>
					<%= loginBean.translate("Slack Incoming WebHook URL") %><br/>
					<input type="text" name="incomingWebhook" value="<%= bean.getIncomingWebhook() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							Automatically post content from the RSS feeds to a channel.<br/>
							List each feed separated by a new line.<br/>
							You can include a prefix and/or a suffix to append to the RSS title.<br/>
							i.e.
<pre>Blog http://botlibre.blogspot.com/feeds/posts/default #blog
Tech News http://feeds.reuters.com/reuters/technologyNews 
#tech #news
</pre>
							Ensure your URL is a valid RSS feed (XML, not HTML).
						</div>
					</span>
					<%= loginBean.translate("RSS Feeds") %><br/>
					<textarea name="postRSS" ><%= bean.getPostRSS() %></textarea><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							Only post RSS feeds that contain one of the keywords set in their title.<br/>
							Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.<br/>
							i.e.
<pre>chat bot
chatterbot
</pre>
						</div>
					</span>
					<%= loginBean.translate("RSS Keywords") %><br/>
					<textarea name="rssKeywords"><%= bean.getRSSKeyWords() %></textarea><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Override the bot username to use when posting an rss feed.") %>
						</div>
					</span>
					<%= loginBean.translate("RSS Feed Bot Username (Optional)") %><br/>
					<input type="text" name="rssUsername" value="<%= bean.getRssUsername() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Override the channel to post the rss feed to.") %>
						</div>
					</span>
					<%= loginBean.translate("RSS Feed Channel (Optional)") %><br/>
					<input type="text" name="rssChannel" value="<%= bean.getRssChannel() %>"/><br/>

					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should post automatically every set number of hours.") %>
						</div>
					</span>
					<input name="autoPost" type="checkbox" <% if (bean.getAutoPost()) { %>checked<% } %> />Auto Post<br/>

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
							Set of posts to auto post.<br/>
							List each post separated by a new line.<br/>
							Self and AIML templates can be used<br/>
							i.e.
<pre>How is everyone today?
Template("The current time is {Date.time()}")
&lt;template&gt;
It is &lt;date/&gt;, and &lt;srai&gt;How are you&lt;/srai&gt;
&lt;/template&gt;
</pre>
						</div>
					</span>
					<%= loginBean.translate("Auto Posts") %><br/>
					<textarea name="autoPosts"><%= bean.getAutoPosts() %></textarea><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Override the bot username to use when posting an auto post.") %>
						</div>
					</span>
					<%= loginBean.translate("") %>
					Auto Post Bot Username (Optional)<br/>
					<input type="text" name="autoPostUsername" value="<%= bean.getAutoPostUsername() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Override the channel to post the auto post to.") %>
						</div>
					</span>
					<%= loginBean.translate("Auto Post Channel (Optional)") %><br/>
					<input type="text" name="autoPostChannel" value="<%= bean.getAutoPostChannel() %>"/><br/>

					<input type="submit" name="check" value="Check Status" title="Have the bot check Slack"/>
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
