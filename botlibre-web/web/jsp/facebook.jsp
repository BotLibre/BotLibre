<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.FacebookBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% FacebookBean bean = loginBean.getBean(FacebookBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Facebook - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Facebook, and configure Facebook properties"/>	
	<meta name="keywords" content="facebook, bot, facebook bot, facebot, facebook automation, social media, facebook messenger"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The Facebook tab allows you to connect your bot to Facebook, and monitor a Facebook account or page.") %><br/>
					<%= loginBean.translate("Bot Libre provides its own Facebook app to provide many automation features, and lets you develop your own Facebook app to gain access to more features.") %><br/>
					<%= loginBean.translate("To authorize a new account, just click the 'Authorize' button, then click 'Connect'.") %><br/>
					<%= loginBean.translate("Some features are only supported for Facebook Page automation, or with your own Facebook app key.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-facebook.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=12742773"><%= loginBean.translate("How To Guide") %></a>
			 : <a target="_blank" href="https://youtu.be/Bzj6Fv6A7c4"><%= loginBean.translate("Video") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/facebook4.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to manage a Facebook account or page and interact with other Facebook users.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Facebook") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The Facebook tab allows you to connect your bot to Facebook, and monitor a Facebook account or page.") %>
				</p>
				<p class="help">
					<%= loginBean.translate("Bot Libre provides its own Facebook app to provide many automation features, and lets you develop your own Facebook app to gain access to more features.") %>
				</p>
				<p class="help">
					<%= loginBean.translate("To authorize a new account, just click the 'Authorize' button, then click 'Connect'.") %>
				</p>
				<p class="help">
					<%= loginBean.translate("Some features are only supported for Facebook Page automation, or with your own Facebook app key.") %>
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<% if (bean.isConnected()) { %>
					<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the Facebook account") %> <i><%= bean.getUserName() %> : <%= bean.getProfileName() %></i><br/>
					<%= loginBean.translate("To see") %> <%= botBean.getInstanceName() %>'s <%= loginBean.translate("profile goto") %> <a style="color:grey" target="_blank" href="http://facebook.com/<%= bean.getUserName() %>">facebook.com/<%= bean.getUserName() %></a>
				<% } else { %>
					<p class="help">
						<%= loginBean.translate("The Facebook tab allows you to connect your bot to Facebook, and monitor a Facebook account or page.") %>
					</p>
					<p class="help">
						<%= loginBean.translate("Bot Libre provides its own Facebook app to provide many automation features, and lets you develop your own Facebook app to gain access to more features.") %>
					</p>
					<p class="help">
						<%= loginBean.translate("To authorize a new account, just click the 'Authorize' button, then click 'Connect'.") %>
					</p>
					<p class="help">
						<%= loginBean.translate("Some features are only supported for Facebook Page automation, or with your own Facebook app key.") %>
					</p>
					<br/>
					<%= botBean.getMustBeAdminMessage() %>
				<% } %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or to violate the Facebook terms of service.") %><br/>
					<%= loginBean.translate("Please review Facebook's") %> <a target="_blank" href="https://www.facebook.com/legal/terms"><%= loginBean.translate("Terms of Service") %></a>
					<%= loginBean.translate("before connecting your bot to a Facebook account or page.") %><br/>
					<%= loginBean.translate("This service is dependent on access to the Facebook API, we are not responsible if Facebook revokes access to your account, or our service.") %>
				</p>
				<form action="facebook" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<% if (bean.isConnected()) { %>
						<p>
						<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the Facebook account") %> <i><%= bean.getUserName() %> : <%= bean.getProfileName() %></i><br/>
						<% if (bean.getTokenExpiry() != null) { %>
							<%= loginBean.translate("This Facebook access token will expire on") %> <i><%= bean.getTokenExpiry() %></i> <%= loginBean.translate("and will need to be renewed") %><br/>
						<% } %>
						<%= loginBean.translate("To see") %> <%= botBean.getInstanceName() %>'s <%= loginBean.translate("profile goto") %> <a style="color:grey" target="_blank" href="http://facebook.com/<%= bean.getUserName() %>">facebook.com/<%= bean.getUserName() %></a>
						</p>
						<input id="cancel" type="submit" name="disconnect" value="<%= loginBean.translate("Disconnect") %>" title="<%= loginBean.translate("Disconnect the bot from Facebook, it will no longer check is Facebook status") %>"/>
						<input type="submit" name="check" value="<%= loginBean.translate("Check Posts") %>" title="<%= loginBean.translate("Have the bot check its Facebook timeline, posts, etc. See Log page for what was processed") %>"/>
					<% } else { %>
						<p>
							<%= loginBean.translate("This bot is not currently connected to a Facebook acccount.") %></br>
							<%= loginBean.translate("To connect to a new account, first click 'Authorize', then 'Connect'.") %><br/>
							<%= loginBean.translate("To connect a bot to Facebook Messenger or to your own Facebook app, enter the app id and secret.") %>
						</p>
						<input type="submit" name="authorise" value="<%= loginBean.translate("Authorize") %>" title="<%= loginBean.translate("Authorize your bot to access a facebook account") %>"/>
						<input type="submit" name="connect" value="<%= loginBean.translate("Connect") %>" title="<%= loginBean.translate("Connect your bot to a facebook account") %>"/>
					<% } %>
						<span id="advancedOptions">
						
						<br/><br/>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("You do not need to enter this, just click authorize.") %><br/>
							</div>
						</span>
						<%= loginBean.translate("Facebook User") %><br/>
						<input type="text" name="user" value="<%= bean.getUserName() %>" /><br/>
						
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("You do not need to enter this, just click authorise.") %><br/>
							</div>
						</span>
						<%= loginBean.translate("Facebook Access Token") %><br/>
						<input type="text" name="token" value="<%= bean.getToken() %>"/><br/>
						
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Select the page you wish the bot to monitor.") %><br/>
							</div>
						</span>
						<%= loginBean.translate("Page") %><br/>
						<input id="pages" type="text" name="page" value="<%= bean.getFacebookPage() %>"/><br/>
						<% if (!bean.getPages().isEmpty()) { %>
							<script>
							$( '#pages' ).autocomplete({ source: [<%= bean.getPagesString() %>], minLength: 0 }).on('focus',
									function(event) { var self = this; $(self).autocomplete('search', ''); });
							</script>
						<% } %>
						
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Advanced: Only for user developing their own Facebook app") %><br/>
							</div>
						</span>
						<%= loginBean.translate("Facebook App ID") %><br/>
						<input type="text" name="appOauthKey" value="<%= bean.getAppOauthKey() %>"/><br/>
						
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Advanced: Only for user developing their own Facebook app") %><br/>
							</div>
						</span>
						<%= loginBean.translate("Facebook App Secret") %><br/>
						<input type="text" name="appOauthSecret" value="<%= bean.getAppOauthSecret() %>"/><br/>
						
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("A webhook is only required for realtime Facebook Messenger page messages. You must submit your webhook to your Facebook app") %><br/>
							</div>
						</span>
						<%= loginBean.translate("Webhook Callback URL") %><br/>
						<input type="text" name="webhook" value="<%= bean.getWebhook() %>"/><br/>
					</span>
				</form>
				<form id="facebookProperties" action="facebook" method="post" class="message" <%= !bean.isConnected() && bean.getToken().isEmpty() ? "style='display:none'" : "" %>>
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<h3><%= loginBean.translate("Facebook Messenger Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should not reply when someone sends it a message. ") %><br/>
						</div>
					</span>
					<input type="radio" name="messenger" value="none"
							<% if (bean.getDisableMessages()) { %>checked<% } %> ><%= loginBean.translate("Do not reply to messages") %></input><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should poll its messages and reply when someone sends it a message.") %><br/>
							<%= loginBean.translate("Currently only supported for authorized page accounts.") %><br/>
							<%= loginBean.translate("You must use your own Facebook app key to use this feature.") %>
						</div>
					</span>
					<input type="radio" name="messenger" value="poll"
							<% if (bean.getReplyToMessages()) { %>checked<% } %> ><%= loginBean.translate("Poll and reply to messages") %></input><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure Facebook Messenger support for a Facebook page.") %><br/>
							<%= loginBean.translate("You must create your own Facebook page and app for this and enter your Facebook page access token.") %><br/>
						</div>
					</span>
					<input type="radio" name="messenger"" value="app"
							<% if (bean.getFacebookMessenger()) { %>checked<% } %> ><%= loginBean.translate("Facebook Messenger app (realtime messages)") %></input><br/>
					
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure Facebook Messenger support for a Facebook page.") %><br/>
						<%= loginBean.translate("You must create your own Facebook page and app for this and enter your Facebook page access token.") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Facebook Messenger Page Access Token") %><br/>
					<input type="text" name="facebookMessengerAccessToken" value="<%= bean.getFacebookMessengerAccessToken() %>"/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Facebook Messenger supports two button types, 'buttons', and 'quick replies'.") %><br/>
							<%= loginBean.translate("For HTML responses, by default 'buttons' are used for <= 3 buttons, and 'quick replies' are used for > 3 buttons.") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Button Type") %> 
					<select name="buttonType">
						<option value="auto" <% if (bean.getButtonType().equals("auto")) { %>selected<% } %>><%= loginBean.translate("Auto") %></option>
						<option value="button" <% if (bean.getButtonType().equals("button")) { %>selected<% } %>><%= loginBean.translate("Button") %></option>
						<option value="quickReply" <% if (bean.getButtonType().equals("quickReply")) { %>selected<% } %>><%= loginBean.translate("Quick Reply") %></option>
					</select><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Remove the button text from the message as it will be displayed in the Facebook Messenger button.") %><br/>
						</div>
					</span>
					<input type="checkbox" name="stripButtonText" <% if (bean.getStripButtonText()) { %>checked<% } %> ><%= loginBean.translate("Remove Button Text") %></input><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the entire JSON message from Facebook should be stored in the bot's input and conversation log (and be accessible in scripts).") %><br/>
						</div>
					</span>
					<input type="checkbox" name="trackMessageObjects" <% if (bean.getTrackMessageObjects()) { %>checked<% } %> ><%= loginBean.translate("Track Message Objects") %></input><br/>
					
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure Facebook Messenger greeting text.") %><br/>
							<%= loginBean.translate("This greeting is display to first time users messaging your page.") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Greeting Text") %><br/>
					<input type="text" name="greetingText" value="<%= bean.getGreetingText() %>"/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure Facebook Messenger get started button post back.") %><br/>
							<%= loginBean.translate("This 'Get Started' button is display to first time users messaging your page.") %><br/>
							<%= loginBean.translate("When clicked your post back message will be sent to your bot.") %><br/>
						</div>
					</span>
					<%= loginBean.translate("Get Started Button Post Back") %><br/>
					<input type="text" name="getStartedButton" value="<%= bean.getGetStartedButton() %>"/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set of menu items for the Facebook Messenger 'Persistent Menu'.") %><br/>
							<%= loginBean.translate("A menu can be a postback message, and URL link, or a nested menu.") %><br/>
							<%= loginBean.translate("For a nested menu use * and **. Only 2 levels of nested menus are supported.") %><br/>
							<%= loginBean.translate("See") %> <a target="_blank" href="https://developers.facebook.com/docs/messenger-platform/thread-settings/persistent-menu">Persistent Menu</a><br/>
							<%= loginBean.translate("i.e.") %>
<pre>Home
Back, go back
Website
* FAQ, http://acme.com/faq
* Contact, http://acme.com/contact
</pre>
						</div>
					</span>
					<%= loginBean.translate("Persistent Menu") %><br/>
					<textarea name="persistentMenu"><%= bean.getPersistentMenu() %></textarea><br/>
						
					<h3><%= loginBean.translate("Facebook Page/Profile Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Maximum posts to process per cycle (max is 20).") %><br/>
							<!-- Likes are not included.<br/-->
							<% if (!Site.COMMERCIAL && !Site.DEDICATED) { %>
								<%= loginBean.translate("Larger limits are available for commercial accounts at") %>, <a target="_blank" href="https://www.botlibre.biz">www.botlibre.biz</a><br/>
							<% } %>
							<%= loginBean.translate("The bot will only read posts that match one of the post keywords sets below.") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum?id=9384"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.
						</div>
					</span>
					<%= loginBean.translate("Max Posts") %><br/>
					<input type="number" name="maxPosts" value="<%= bean.getMaxPost() %>"/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should read (and possibly respond to) its wall/page's posts.") %><br/>
							<%= loginBean.translate("The bot will only read the posts that match one of the Wall Reply Keywords sets below.") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum?id=9384"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.<br/>
							<%= loginBean.translate("Facebook only allows access to wall posts to authorized Page accounts, or if you use your own Facebook app key.") %>
						</div>
					</span>
					<input name="processPosts" type="checkbox" onclick="document.getElementById('processPosts').style.display=(this.checked ? 'inline' : 'none');"
							<% if (bean.getProcessPost()) { %>checked<% } %> ><%= loginBean.translate("Process Wall Posts") %></input><br/>
					
					<span id="processPosts" <%= bean.getProcessPost() ? "" : "style='display:none'" %>>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Configure if the bot should reply to all posts to its wall/page.") %><br/>
								<%= loginBean.translate("Use this with caution, avoid spam, and ensure your bot complies with the Facebook terms of use.") %><br/>
							</div>
						</span>
						<input name="processAllPosts" type="checkbox" onclick="document.getElementById('postKeywords').style.display=(this.checked ? 'none' : 'inline');" 
								<% if (bean.getProcessAllPosts()) { %>checked<% } %> ><%= loginBean.translate("Reply to All Wall Posts") %></input><br/>
					
						<span id="postKeywords" <%= bean.getProcessAllPosts() ? "style='display:none'" : "" %>>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg"/>
								<div>
									<%= loginBean.translate("Only posts that contains 'all' of one of the keyword/tag sets will be replied to.") %>
									<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %><br/>
									<%= loginBean.translate("i.e.") %>
<pre>chat bot
chatterbot
</pre>
								</div>
							</span>
							<%= loginBean.translate("Wall Reply Keywords/Hashtags") %><br/>
							<textarea name="postKeywords" ><%= bean.getPostKeywords() %></textarea><br/>
						</span>
					</span>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Automatically like posts shared on its wall.") %><br/>
						</div>
					</span>
					<input name="autoLike" type="checkbox" onclick="document.getElementById('autoLike').style.display=(this.checked ? 'inline' : 'none');"
							<%= !bean.getLikeAllPosts() && bean.getLikeKeywords().isEmpty() ? "" : "checked" %>><%= loginBean.translate("Auto Like") %></input><br/>
					
					<span id="autoLike" <%= !bean.getLikeAllPosts() && bean.getLikeKeywords().isEmpty()? "style='display:none'" : "" %>>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Configure if the bot should like all posts shared on its wall.") %><br/>
								<%= loginBean.translate("Use this with caution, avoid spam, and ensure your bot complies with the Facebook terms of use.") %><br/>
							</div>
						</span>
						<input name="likeAllPosts" type="checkbox" onclick="document.getElementById('likeKeywords').style.display=(this.checked ? 'none' : 'inline');" 
								<% if (bean.getLikeAllPosts()) { %>checked<% } %> ><%= loginBean.translate("Like All Posts") %></input><br/>
						
						<span id="likeKeywords" <%= bean.getLikeAllPosts() ? "style='display:none'" : "" %>>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg"/>
								<div>
									<%= loginBean.translate("Like posts that contain one of the keyword/tag sets.") %><br/>
								<%= loginBean.translate("Use this with caution, avoid spam, and ensure your bot complies with the Facebook terms of use.") %><br/>
								<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %>
								<%= loginBean.translate("i.e.") %>
<pre>chat bot
chatterbot
</pre>
								</div>
							</span>
							<%= loginBean.translate("Like Keywords/Hashtags") %><br/>
							<textarea name="likeKeywords"><%= bean.getLikeKeywords() %></textarea><br/>
						</span>
					</span>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Automatically post content from an RSS feed.") %><br/>
						</div>
					</span>
					<input name="rssFeed" type="checkbox" onclick="document.getElementById('rssFeed').style.display=(this.checked ? 'inline' : 'none');"
							<%= bean.getPostRSS().isEmpty() ? "" : "checked" %>><%= loginBean.translate("Post RSS Feed") %></input><br/>
					
					
					<span id="rssFeed" <%= bean.getPostRSS().isEmpty() ? "style='display:none'" : "" %>>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Automatically post content from the RSS feeds.") %><br/>
									<%= loginBean.translate("List each feed separated by a new line.") %><br/>
									<%= loginBean.translate("You can include a prefix and/or a suffix to append to the RSS title.") %><br/>
									<%= loginBean.translate("i.e.") %>
<pre>Blog http://acme.blogspot.com/feeds/posts/default #blog
Tech News http://feeds.reuters.com/reuters/technologyNews #tech #news
</pre>
									<%= loginBean.translate("Ensure your URL is a valid RSS feed (XML, not HTML).") %>
							</div>
						</span>
						<%= loginBean.translate("RSS Feeds") %><br/>
						<textarea name="postRSS"><%= bean.getPostRSS() %></textarea><br/>
					
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Only post RSS feeds that contain one of the keywords set in their title.") %><br/>
									<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %><br/>
									<%= loginBean.translate("i.e.") %>
<pre>chat bot
chatterbot
</pre>
							</div>
						</span>
						<%= loginBean.translate("RSS Keywords") %><br/>
						<textarea name="rssKeywords"><%= bean.getRSSKeyWords() %></textarea><br/>
					</span>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should post automatically every set number of hours.") %>
						</div>
					</span>
					<input name="autoPost" type="checkbox" onclick="document.getElementById('autopost').style.display=(this.checked ? 'inline' : 'none');"
							<% if (bean.getAutoPost()) { %>checked<% } %> ><%= loginBean.translate("Auto Post") %></input><br/>
					
					<span id="autopost" <%= bean.getAutoPost() ? "" : "style='display:none'" %>>
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
									<%= loginBean.translate("i.e.") %>
<pre>How is everyone today?
Template("The current time is {Date.time()}")
&lt;template&gt;It is &lt;date/&gt;, and &lt;srai&gt;How are you&lt;/srai&gt;&lt;/template&gt;
</pre>
							</div>
						</span>
						<%= loginBean.translate("Auto Posts") %><br/>
						<textarea name="autoPosts" ><%= bean.getAutoPosts() %></textarea><br/>
					</span>
					
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
