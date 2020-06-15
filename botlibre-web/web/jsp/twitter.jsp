<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.TwitterBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% TwitterBean bean = loginBean.getBean(TwitterBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Twitter - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Connect your bot to Twitter and configure Twitter properties") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("twitter, bot, twitterbot, twitter automation, social media") %>"/>
	<%= loginBean.getJQueryHeader() %>
	<script>
	$(function() {
		$( "#dialog-import" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#import" ).click(function() {
			$( "#dialog-import" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-import" ).click(function() {
			$( "#dialog-import" ).dialog( "close" );
			return false;
		});
	});
	</script>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The twitter tab allows you to connect your bot to Twitter, and monitor a Twitter feed.") %>
						<%= loginBean.translate("To authorise a new account, just click the 'Authorize' button.") %>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-twitter.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=5015"><%= loginBean.translate("How To Guide") %></a>
			 : <a target="_blank" href="https://youtu.be/xdO6kUoBs8c"><%= loginBean.translate("Video") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/twitter1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to manage a Twitter account and interact with other Twitter users.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Twitter") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The twitter tab allows you to connect your bot to Twitter, and monitor a Twitter feed.") %>
					<%= loginBean.translate("To authorize a new account, just click the 'Authorize' button.") %>
					<br/>
					<%= loginBean.translate("See") %> <a href="https://www.botlibre.com/forum-post?id=5015&embedded" target="_blank" class="blue"><%= loginBean.translate("Automate your Twitter presence with your own Twitterbot") %></a> <%= loginBean.translate("for more information") %>.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<% if (bean.isConnected()) { %>
					<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the Twitter account") %> <i><%= bean.getUserName() %></i><br/>
					<%= loginBean.translate("To see") %> <%= botBean.getInstanceName() %>'s <%= loginBean.translate("profile goto") %> <a style="color:grey" target="_blank" href="http://twitter.com/<%= bean.getUserName() %>">twitter.com/<%= bean.getUserName() %></a>
				<% } else { %>
					<p class="help">
						<%= loginBean.translate("The twitter tab allows you to connect your bot to Twitter, and monitor a Twitter feed.") %>
						<%= loginBean.translate("To authorize a new account, just click the 'Authorize' button.") %>
						<br/>
						<%= loginBean.translate("See") %> <a href="https://www.botlibre.com/forum-post?id=5015&embedded" target="_blank" class="blue"><%= loginBean.translate("Automate your Twitter presence with your own Twitterbot") %></a> <%= loginBean.translate("for more information") %>.
					</p>
					<br/>
					<%= botBean.getMustBeAdminMessage() %>
				<% } %>
			<% } else if (bean.isAuthorising()) { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or to violate the Twitter terms of service.") %><br/>
					<%= loginBean.translate("Please review the") %> <a target="_blank" href="http://support.twitter.com/articles/76915-automation-rules-and-best-practices"><%= loginBean.translate("Automation rules and best practices") %></a>
					<%= loginBean.translate("before connecting your bot to a Twitter account.") %>
				</p>
				<form action="twitter" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<%= loginBean.translate("Open the following URL to authorise the Twitter account:") %><br/>
					<a style="color:grey" target="_blank" href="<%= bean.getAuthorisationURL() %>"><%= bean.getAuthorisationURL() %></a><br/>
					<br/>
					<%= loginBean.translate("Enter pin") %><br/>
					<input autofocus type="text" name="pin" value="" /><br/>
					<input id="ok" type="submit" name="authorise-complete" value="<%= loginBean.translate("Done") %>"/>
					<input id="cancel" type="submit" name="cancel" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or to violate the Twitter terms of service.") %><br/>
					<%= loginBean.translate("Please review the") %> <a target="_blank" href="http://support.twitter.com/articles/76915-automation-rules-and-best-practices"><%= loginBean.translate("Automation rules and best practices") %></a>
					<%= loginBean.translate("before connecting your bot to a Twitter account.") %>
				</p>
				<form action="twitter" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<% if (bean.isConnected()) { %>
						<%= botBean.getInstanceName() %> <%= loginBean.translate("is currently connected to the Twitter account") %> <i><%= bean.getUserName() %></i><br/>
						<%= loginBean.translate("To see") %> <%= botBean.getInstanceName() %>'s <%= loginBean.translate("profile goto") %> <a style="color:grey" target="_blank" href="http://twitter.com/<%= bean.getUserName() %>">twitter.com/<%= bean.getUserName() %></a>
						<br/>
						<input id="cancel" type="submit" name="disconnect" value="<%= loginBean.translate("Disconnect") %>"/>
						<input type="submit" name="check" value="Check Status" title="<%= loginBean.translate("Have the bot check its Twitter timeline, status updates, mentions, etc.") %>"/>
						<input type="submit" id="import" name="import" value="Import Tweets" onclick="return false;" title="<%= loginBean.translate("Import and have your bot learn responses from a tweet search") %>">
					<% } else { %>
						<%= loginBean.translate("To connect to a new account, first click Authorize, then Connect.") %><br/>
						<input type="submit" name="authorise" value="<%= loginBean.translate("Authorize") %>" title="<%= loginBean.translate("Authorize your bot to access a twitter account") %>"/>
						<input type="submit" name="connect" value="<%= loginBean.translate("Connect") %>" title="<%= loginBean.translate("Connect your bot to a twitter account") %>"/>
					<% } %>
					<br/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("You do not need to enter this, just click authorize") %><br/>
						</div>
					</span>
						<%= loginBean.translate("Twitter User") %><br/>
						<input type="text" name="user" value="<%= bean.getUserName() %>" /><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("You do not need to enter this, just click authorize") %><br/>
						</div>
					</span>
						<%= loginBean.translate("Twitter Application Token") %><br/>
						<input type="text" name="token" value="<%= bean.getToken() %>" /><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("You do not need to enter this, just click authorize") %><br/>
						</div>
					</span>
						<%= loginBean.translate("Twitter Application Token Secret") %><br/>
						<input type="text" name="secret" value="<%= bean.getTokenSecret() %>" /><br/>
				</form>
				<form action="twitter" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<h3><%= loginBean.translate("Twitterbot Properties") %></h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should tweet that someone is chatting with it.") %><br/>
							<%= loginBean.translate("The bot will tweet") %> "Talking with [user] on #botlibre".
						</div>
					</span>
					<input name="tweetChats" type="checkbox" <% if (bean.getTweetChats()) { %>checked<% } %> /><%= loginBean.translate("Tweet when someone chats with the bot") %><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should reply when someone mentions it in a tweet.") %>
						</div>
					</span>
					<input name="replyToMentions" type="checkbox" <% if (bean.getReplyToMentions()) { %>checked<% } %> /><%= loginBean.translate("Reply to mentions") %><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should reply when someone sends it a direct message.") %>
						</div>
					</span>
					<input name="replyToMessages" type="checkbox" <% if (bean.getReplyToMessages()) { %>checked<% } %> /><%= loginBean.translate("Reply to direct messages") %><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should read (and possibly respond to) its friend's status updates.") %><br/>
							<%= loginBean.translate("The bot will only read the status updates that match one of the Status Keywords sets below.") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum-post?id=25560&embedded=true"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.
						</div>
					</span>
					<input name="processStatus" type="checkbox" <% if (bean.getProcessStatus()) { %>checked<% } %> /><%= loginBean.translate("Read friends status updates") %><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should read (and not respond to) its friend's status updates.") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum-post?id=25560&embedded=true"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.
						</div>
					</span>
					<input name="listenStatus" type="checkbox" <% if (bean.getListenStatus()) { %>checked<% } %> /><%= loginBean.translate("Read-only") %><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure the bot to learn EVERY tweet as a valid response to itself or its hashtags.") %><br/>
							<%= loginBean.translate("This is not recommended, and ensure your bot complies with Twitter's terms of use.") %><br/>
						</div>
					</span>
					<input name="learn" type="checkbox" <% if (bean.getLearn()) { %>checked<% } %> /><%= loginBean.translate("Learn from friends/search tweets") %><br/>		
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure the bot to learn EVERY tweet from your account as a valid response to itself or its hashtags.") %><br/>
							<%= loginBean.translate("This is not recommended, but can be used to train your bot.") %><br/>
						</div>
					</span>
					<input name="learnFromSelf" type="checkbox" <% if (bean.getLearnFromSelf()) { %>checked<% } %> /><%= loginBean.translate("Learn from your tweets") %><br/>
					
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Maximum status updates to read per cycle (max is 20).") %><br/>
							<%= loginBean.translate("Retweets are not included.") %><br/>
							<% if (!Site.COMMERCIAL && !Site.DEDICATED) { %>
								<%= loginBean.translate("Larger limits are available for commercial accounts at") %>, <a target="_blank" href="https://www.botlibre.biz">www.botlibre.biz</a><br/>
							<% } %>
							<%= loginBean.translate("The bot will only read the status updates that match one of the Status Keywords sets below.") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum-post?id=25560&embedded=true"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.
						</div>
					</span>
						<%= loginBean.translate("Max Status Updates") %><br/>
						<input type="number" name="maxStatus" value="<%= bean.getMaxStatus() %>" /><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Only friend status updates or search results that contains 'all' of one of the keyword/tag sets will be read, and possibly responded to.") %><br/>
							<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %><br/>
							i.e.
<pre>chat bot
chatterbot
</pre>
							<%= loginBean.translate("The bot will only respond if it knows a 'good' response.") %><br/>
							** <%= loginBean.translate("Leave this field blank if you have not trained your bot with any responses.") %><br/>
							** <%= loginBean.translate("DO NOT enter any keywords here UNLESS you have TRAINED your bot with responses, otherwise your account may be DELETED.") %><br/>
							** <%= loginBean.translate("This does not affect retweets") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum-post?id=25560"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.
						</div>
					</span>
						<%= loginBean.translate("Reply Keywords/Hashtags") %><br/>
						<textarea name="statusKeywords" ><%= bean.getStatusKeywords() %></textarea><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Search for tweets matching one of the keyword/tag sets (queries), process and possibly retweet or respond to them.") %><br/>
							<%= loginBean.translate("Use this with caution, avoid spam, and ensure your bot complies with the Twitter terms of use.") %><br/>
							<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %><br/>
							<%= loginBean.translate("Queries use the Twitter search API, see") %> <a target="_blank" href="https://developer.twitter.com/en/docs/tweets/search/guides/standard-operators"><%= loginBean.translate("Twitter Search") %></a> <%= loginBean.translate("for details") %>.<br/>
							i.e.
<pre>chat bot
#chatterbot
bot or chatbot -adult -sex
</pre>
							<%= loginBean.translate("The bot will only respond if it knows a 'good' response.") %><br/>
							<%= loginBean.translate("See") %>, <a target="_blank" href="https://www.botlibre.com/forum-post?id=25560"><%= loginBean.translate("FAQ") %></a> <%= loginBean.translate("for details") %>.
						</div>
					</span>
						<%= loginBean.translate("Tweet Search") %><br/>
						<textarea name="tweetSearch" ><%= bean.getTweetSearch() %></textarea><br/>
						
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Ignore tweet search results that are replies or mentions to other users.") %><br/>
							<%= loginBean.translate("Normally the bot should not retweet or reply to conversations between other users, so replies should be ignored.") %><br/>
						</div>
					</span>
					<input name="ignoreReplies" type="checkbox" <% if (bean.getIgnoreReplies()) { %>checked<% } %> /><%= loginBean.translate("Ignore Replies") %><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
				  			<%= loginBean.translate("Retweet friend's, or search result tweets that contain one of the keyword/tag sets.") %><br/>
				  			<%= loginBean.translate("Use this with caution, avoid spam, and ensure your bot complies with the Twitter terms of use.") %><br/>
							<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %>
							i.e.
<pre>chat bot
chatterbot
</pre>
						</div>
					</span>
						<%= loginBean.translate("Retweet Keywords/Hashtags") %><br/>
						<textarea name="retweet" ><%= bean.getRetweet() %></textarea><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Maximum tweet search result to process or retweets per cycle (max is 20).") %><br/>
							<% if (!Site.COMMERCIAL && !Site.DEDICATED) { %>
								<%= loginBean.translate("Larger limits are available for commercial accounts at,") %> <a target="_blank" href="https://www.botlibre.biz">www.botlibre.biz</a>
							<% } %>
						</div>
					</span>
						<%= loginBean.translate("Max Search Retweets") %><br/>
						<input type="number" name="maxSearch" value="<%= bean.getMaxSearch() %>" /><br/>
				
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot may follow users who follow it.") %><br/>
							<%= loginBean.translate("Use this with caution, and ensure your bot complies with the Twitter terms of use.") %>
						</div>
					</span>
					<input name="autoFollow" type="checkbox" <% if (bean.getAutoFollow()) { %>checked<% } %> /><%= loginBean.translate("Auto Follow") %><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Configure if the bot should follow, or unfollow a user when sent a 'follow me' or 'unfollow me' message.") %><br/>
						</div>
					</span>
					<input name="followMessages" type="checkbox" <% if (bean.getFollowMessages()) { %>checked<% } %> ><%= loginBean.translate("Follow Messages") %></input><br/>	  	
				
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Private message to send users who follow the bot.") %><br/>
						</div>
					</span>
						<%= loginBean.translate("Welcome Message") %><br/>
						<input type="text" name="welcomeMessage" value="<%= bean.getWelcomeMessage() %>" /><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Maximum users to auto follow.") %><br/>
							<%= loginBean.translate("Your bot can have more friends if you add them manually.") %><br/>
						</div>
					</span>
						<%= loginBean.translate("Max Friends") %><br/>
						<input type="number" name="maxFriends" value="<%= bean.getMaxFriends() %>" /><br/>
					<!-- input name="autoFollowFriendsFriends" type="checkbox" title="Configure if the bot should follow the friends of users who follow it. Use this with caution, ensure your bot complies with the Twitter terms of use."
						<% if (bean.getAutoFollowFriendsFriends()) { %>checked<% } %> >Auto Follow Friends Friends</input><br/>
					<input name="autoFollowFriendsFollowers" type="checkbox" title="Configure if the bot should follow the followers of users who follow it. Use this with caution, ensure your bot complies with the Twitter terms of use."
						<% if (bean.getAutoFollowFriendsFollowers()) { %>checked<% } %> >Auto Follow Friends Followers</input><br/-->
					<!--Auto Follow Search<br/>
					<textarea name="autoFollowSearch" title="Follow users who have tweets that match the tweet search. Use this with caution, ensure your bot complies with the Twitter terms of use."
						/><%= bean.getAutoFollowSearch() %></textarea><br/-->
			 
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Only user's who's description contains one of the keyword sets will be followed.") %><br/>
							<%= loginBean.translate("Use this with caution, and ensure your bot complies with the Twitter terms of use.") %><br/>
							<%= loginBean.translate("Keywords must be separated by a space (not a comma), each keyword set must be separated by a new line.") %><br/>
							i.e.
<pre>chat bot
chatterbot
</pre>
						</div>
					</span>
						<%= loginBean.translate("Auto Follow Keywords") %><br/>
						<textarea name="autoFollowKeywords" ><%= bean.getAutoFollowKeywords() %></textarea><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Automatically tweet content from the RSS feeds.") %><br/>
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
						<textarea name="tweetRSS" ><%= bean.getTweetRSS() %></textarea><br/>
			
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
				  			<%= loginBean.translate("Only tweet RSS feeds that contain one of the keywords set in their title.") %><br/>
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
							<%= loginBean.translate("Configure if the bot should tweet automatically every set number of hours.") %>
						</div>
					</span>
					<input name="autoTweet" type="checkbox" <% if (bean.getAutoTweet()) { %>checked<% } %> /><%= loginBean.translate("Auto Tweet") %><br/>
				
					<br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("The number of hours to wait between auto tweets.") %>
						</div>
					</span>
						<%= loginBean.translate("Auto Tweet Hours") %><br/>
						<input type="number" name="autoTweetHours" value="<%= bean.getAutoTweetHours() %>" /><br/>
				
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set of tweets to auto tweet.") %><br/>
							<%= loginBean.translate("List each tweet separated by a new line.") %><br/>
							Self and AIML <%= loginBean.translate("templates can be used") %><br/>
							i.e.
<pre>How is everyone today?
Template("The current time is {Date.time()}")
&lt;template&gt;It is &lt;date/&gt;, and &lt;srai&gt;How are you&lt;/srai&gt;&lt;/template&gt;
</pre>
							<%= loginBean.translate("Note, Twitter will reject duplicate posts, so ensure your auto tweets contains variety to ensure they can be posted.") %>
						</div>
					</span>
					<%= loginBean.translate("Auto Tweets") %><br/>
						<textarea name="autoTweets"
							><%= bean.getAutoTweets() %></textarea><br/>
				
					<input type="submit" name="save" value="<%= loginBean.translate("Save") %>"/><br/>
					<br/>
				</form>
				<% if (bean.isConnected()) { %>
					<h3><%= loginBean.translate("Friends") %></h3>
					<%= bean.getFriends() %> <%= loginBean.translate("friends") %><br/>
					<p>
					<form action="twitter" method="post" class="message">
					<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						Friend<br/>
						<input type="text" name="friend"/><br/>
						<input type="submit" name="add-friend" value="<%= loginBean.translate("Add Friend") %>"/>
						<input id="delete" type="submit" name="remove-friend" value="<%= loginBean.translate("Remove Friend") %>"/>
					</form>
					<h3><%= loginBean.translate("Followers") %></h3>
					<%= bean.getFollowers() %> <%= loginBean.translate("followers") %><br/>
				<% } %>
			<% } %>
		</div>

		<% if (botBean.isConnected()) { %>
			<div id="dialog-import" title="Import Tweets" class="dialog">
				<form action="twitter" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<%= loginBean.translate("Tweet Search") %>
					<input style="width:150px" name="tweetSearch" type="text" value="from:<%= bean.getUserName() %>" title="<%= loginBean.translate("Enter Twitter search @user, keywords or other options") %>" />
					<a target="_blank" href="https://developer.twitter.com/en/docs/tweets/search/guides/standard-operators"><img id="help-icon" src="images/help.svg"/></a>
					<br/>
					<%= loginBean.translate("Max Tweets") %>
					<input style="width:150px" name="maxTweets" type="number" value="100" title="<%= loginBean.translate("Max number of tweets to import") %>" /><br/>
					<input type="checkbox" checked name="tweets" title="<%= loginBean.translate("Import status updates as the question and response") %>"><%= loginBean.translate("Import tweets") %></input><br/>
					<input type="checkbox" checked name="replies" title="<%= loginBean.translate("Import replies as a question/response") %>"><%= loginBean.translate("Import replies") %></input><br/>
					<input class="ok" type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="<%= loginBean.translate("Import and have your bot learn responses from a tweet search") %>">
					<input id="cancel-import" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			</div>
		<% } %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
