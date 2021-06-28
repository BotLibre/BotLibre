<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% BotBean bean = loginBean.getBotBean(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Features - <%= Site.NAME %></title>
	<meta name="description" content="Features of the Bot Libre platform"/>	
	<meta name="keywords" content="features, info"/>
</head>
<body>
	<% loginBean.setPageType(Page.Features); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="about">
			<h1><%= loginBean.translate("Features") %></h1>
			<p>
				<%= loginBean.translate(Site.NAME + " offers a rich set of features for creating, monitoring, and access your bots and content.") %>
			</p>
			<% if (!Site.DEDICATED) { %>
				<div class="feature">
					<h4><%= loginBean.translate("Products") %></h4>
					<p>
						<ul>
							<li><a href="enterprise-bot-platform.jsp"><%= loginBean.translate("Enterprise Bot Platform") %></a></li>
							<li><a target="_blank" href="https://play.google.com/store/apps/details?id=com.paphus.botlibre.client.android"><%= loginBean.translate("Bot Libre Android") %></a></li>
							<li><a target="_blank" href="https://play.google.com/store/apps/details?id=com.paphus.botlibre.offline"><%= loginBean.translate("Bot Libre Android Offline") %></a></li>
							<li><a target="_blank" href="https://itunes.apple.com/ca/app/bot-libre/id998358768"><%= loginBean.translate("Bot Libre iOS") %></a></li>
							<li><a href="browse?browse-type=Desktop"><%= loginBean.translate("Bot Libre Desktop") %></a></li>
							<li><a target="_blank" href="http://botlibre.org"><%= loginBean.translate("BotLibre.org") %></a></li>
						</ul>
					</p>
				</div>
				<div class="feature">
					<h4><%= loginBean.translate("Whitepapers") %></h4>
					<p>
						<ul>
							<li><a href="whitepaper.pdf" target="_blank"><%= loginBean.translate("Whitepaper") %></a></li>
							<li><a href="whitepaper-enterprise.pdf" target="_blank"><%= loginBean.translate("Enterprise Bot Platform Whitepaper") %></a></li>
							<li><a href="whitepaper-analytics.pdf" target="_blank"><%= loginBean.translate("Analytics Whitepaper") %></a></li>
							<li><a href="slides.pdf" target="_blank"><%= loginBean.translate("Slide Show") %></a></li>
						</ul>
					</p>
				</div>
				<br/>
			<% } %>
			<form action="login" method="post" class="message">
				<input id="ok" type="submit" name="sales" value="<%= loginBean.translate("Contact Sales") %>">
				<input id="ok" type="submit" name="demo" value="<%= loginBean.translate("Request Demo") %>">
			</form>
			<br/>
			<div class="feature">
				<h4><img src="images/create.png" class="title"> <%= loginBean.translate("Create") %></h4>
				<%= loginBean.translate(Site.NAME + " makes it easy to your own bot") %>
				<ul>
					<li><%= loginBean.translate("One click bot creation") %>
					<li><%= loginBean.translate("Choose from library of bot templates") %>
					<li><%= loginBean.translate("Fork existing bots") %>
					<li><%= loginBean.translate("Private, restricted, and hidden access control") %>
					<li><%= loginBean.translate("Tag and categorize") %>
				</ul>
			</div>

			<div class="feature">
				<h4><img src="images/chat-icon.png" class="title"> <%= loginBean.translate("Chat") %></h4>
				<%= loginBean.translate("Chat with your bot anywhere") %>
				<ul>
					<li><%= loginBean.translate("Chat with realistic voice") %>
					<li><%= loginBean.translate("Chat with real emotions") %>
					<li><%= loginBean.translate("Chat with rich HTML and JavaScript") %>
					<li><%= loginBean.translate("Chat with buttons, links, and commands") %>
					<li><%= loginBean.translate("Chat on Facebook, Twitter, Telegram, WhatsApp, Skype, Kik, WeChat, Slack, email, SMS, IVR") %>
					<li><%= loginBean.translate("Chat in live chat channel or chatroom") %>
					<li><%= loginBean.translate("Chat from mobile app") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/avatar-icon.png" class="title"> <%= loginBean.translate("Avatar") %></h4>
						<%= loginBean.translate("Choose or design your bot's own 3D animated video avatar") %>
				<ul>
					<li><%= loginBean.translate("Upload images, video, and audio from phone or web") %>
					<li><%= loginBean.translate("Choose from shared") %> <a class="blue" href="browse-avatar.jsp"><%= loginBean.translate("avatar library") %></a>
					<li><%= loginBean.translate("Tag media with emotions, actions, and poses to let your bot express itself") %>
					<li><%= loginBean.translate("Embed avatar on your own website with JavaScript and mobile SDK") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/voice.png" class="title"> <%= loginBean.translate("Voice") %></h4>
						<%= loginBean.translate("Chat with realistic") %> <a class="blue" href="voice.jsp"><%= loginBean.translate("voice") %></a>, <%= loginBean.translate("and speech recognition") %>
				<ul>
					<li><%= loginBean.translate("Choose voice and language") %>
					<li><%= loginBean.translate("Choose mobile voice and language") %>
					<li><%= loginBean.translate("Talk on mobile, or through Google Chrome") %>
					<li><%= loginBean.translate("TTS web API") %></a>
				</ul>					
			</div>
			<div class="feature">
				<h4><img src="images/twitter1.png" class="title"> Twitter</h4>
						<%= loginBean.translate("Make your own") %> Twitterbot
				<ul>
					<li><%= loginBean.translate("Automate Twitter account") %>
					<li><%= loginBean.translate("Respond to mentions and friends status updates") %>
					<li><%= loginBean.translate("Search and process tweets") %>
					<li><%= loginBean.translate("Retweet keywords") %>
					<li><%= loginBean.translate("Tweet RSS feeds") %>
					<li><%= loginBean.translate("Schedule auto-tweets") %>
					<li><%= loginBean.translate("Import tweets") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/facebook4.png" class="title"> Facebook</h4>
						<%= loginBean.translate("Automate your") %> Facebook <%= loginBean.translate("account or page") %>
				<ul>
					<li><%= loginBean.translate("Chat on Facebook Messenger in realtime") %>
					<li><%= loginBean.translate("Chat with buttons, images and links") %>
					<li><%= loginBean.translate("Automate Facebook account or page") %>
					<li><%= loginBean.translate("Respond to feed posts and comments") %>
					<li><%= loginBean.translate("Post an RSS feed") %>
					<li><%= loginBean.translate("Schedule auto-posts") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/skype1.png" class="title"> Skype</h4>
				<%= loginBean.translate("Create a bot for") %> Skype
				<ul>
					<li><%= loginBean.translate("Respond to chat messages in realtime") %>
					<li><%= loginBean.translate("Connect you bot to the Microsoft Bot Framework") %>
					<li><%= loginBean.translate("Connect with Bing, Cortona, Kik, and more") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/telegram1.png" class="title"> Telegram</h4>
				<%= loginBean.translate("Create a bot for") %> Telegram
				<ul>
					<li><%= loginBean.translate("Respond to chat messages in realtime") %>
					<li><%= loginBean.translate("Manage a channel") %>
					<li><%= loginBean.translate("Post an RSS feed") %>
					<li><%= loginBean.translate("Schedule auto-posts") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/kik.png" class="title"> Kik</h4>
				<%= loginBean.translate("Create a bot for") %> Kik
				<ul>
					<li><%= loginBean.translate("Respond to chat messages in realtime") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/wechat1.png" class="title"> WeChat</h4>
				<%= loginBean.translate("Create a bot for") %> WeChat
				<ul>
					<li><%= loginBean.translate("Respond to chat messages in realtime") %>
					<li><%= loginBean.translate("Manage a WeChat official account") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/slack1.png" class="title"> Slack</h4>
				<%= loginBean.translate("Create a bot for") %> Slack
				<ul>
					<li><%= loginBean.translate("Monitor a multi user channel") %>
					<li><%= loginBean.translate("Respond to targeted, or key questions") %>
					<li><%= loginBean.translate("Post an RSS feed") %>
					<li><%= loginBean.translate("Schedule auto-posts") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/twilio.svg" class="title"> <%= loginBean.translate("SMS & IVR") %></h4>
				<%= loginBean.translate("Create a bot for SMS and IVR") %>
				<ul>
					<li><%= loginBean.translate("Connect your bot to Twilio SMS & IVR") %>
					<li><%= loginBean.translate("Reply to SMS messages, send SMS messages from chat or social media") %>
					<li><%= loginBean.translate("Answer voice phone calls, initiate voice calls from chat or social media") %>
					<li><%= loginBean.translate("Automate call centers or answering machines") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/alexa1.png" class="title"> Alexa</h4>
				<%= loginBean.translate("Create a bot for Amazon") %> Alexa
				<ul>
					<li><%= loginBean.translate("Deploy your bot to Alexa's skills directory") %>
					<li><%= loginBean.translate("Enable access to your business on Alexa devices") %>
					<li><%= loginBean.translate("Create your own business or personal assistant") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/google-assistant1.png" class="title"> Google Home</h4>
				<%= loginBean.translate("Create a bot for ") %> Google Home and Google Assistant
				<ul>
					<li><%= loginBean.translate("Deploy your bot to Google's actions directory") %>
					<li><%= loginBean.translate("Enable access to your business on Google Home devices") %>
					<li><%= loginBean.translate("Create your own business or personal assistant") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/embed-icon.png" class="title"> <%= loginBean.translate("Web") %></h4>
				<%= loginBean.translate("Embed your bot on your own webite, or mobile app") %>
				<ul>
					<li><%= loginBean.translate("Embed tab makes embedding easy") %>
					<li><%= loginBean.translate("5 different embedding options") %>
					<li><%= loginBean.translate("Copy HTML or JavaScript code to your website or blog") %>
					<li><%= loginBean.translate("Use the web API to access from web or mobile") %>
					<li><%= loginBean.translate("Use the JavaScript SDK to build your own web app") %>
					<li><%= loginBean.translate("Use the mobile SDK to build your own mobile app") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/train.png" class="title"> <%= loginBean.translate("NLP") %></h4>
				<%= loginBean.translate("Train your bot through conversation") %>, <%= loginBean.translate("chat logs") %>, <%= loginBean.translate("or scripting") %>
				<ul>
					<li><%= loginBean.translate("No programming required") %>
					<li><%= loginBean.translate("Bot's can learn through conversation") %>
					<li><%= loginBean.translate("Correct your bot's responses while chatting") %>
					<li><%= loginBean.translate("Train your bot with keywords, topics, synonyms, sentiment, and context") %>
					<li><%= loginBean.translate("Graphical response editor with buttons, images, and rich media") %>
					<li><%= loginBean.translate("Import response lists, chat logs, AIML, and program scripts") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/script.png" class="title"> <%= loginBean.translate("Script") %></h4>
				<a class="blue" href="browse-script.jsp"><%= loginBean.translate("Script") %></a> <%= loginBean.translate("your bot with") %> Self <%= loginBean.translate("scripts") %>, <%= loginBean.translate("or") %> AIML
				<ul>
					<li><%= loginBean.translate("Script conversations with our powerful Self scripting language (JavaScript dialect)") %>
					<li><%= loginBean.translate("Import and export standard AIML scripts") %>
					<li><%= loginBean.translate("Import or share scripts in our script library") %>
					<li><%= loginBean.translate("Let your bot program itself through machine learning") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/knowledge.png" class="title"> <%= loginBean.translate("Knowledge") %></h4>
				<%= loginBean.translate("Each bot has its own integrated object database") %>
				<ul>
					<li><%= loginBean.translate("Browse and edit knowledge from Knowledge Browser") %>
					<li><%= loginBean.translate("Import and export data as JSON, XML, and CSV") %>
					<li><%= loginBean.translate("Import data from the web and web services") %>
					<li><%= loginBean.translate("Use and persist knowledge and data from scripts") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/timers.png" class="title"> <%= loginBean.translate("Timers & Bot Services") %></h4>
				<%= loginBean.translate("Automate tasks using") %> <a class="blue" target="_blank" href="https://www.botlibre.com/forum-post?id=17076300"><%= loginBean.translate("timers") %></a>
				<ul>
					<li><%= loginBean.translate("Schedule bot to perform tasks on set intervals") %>
					<li><%= loginBean.translate("Follow up on social media interactions") %>
					<li><%= loginBean.translate("Administer mailing lists") %>
					<li><%= loginBean.translate("Access your bot's knowledge and script processing through a JSON web service") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/script.png" class="title"> <%= loginBean.translate("Web Services") %></h4>
				<%= loginBean.translate("Use web services to connect your bot with anything") %>
				<ul>
					<li><%= loginBean.translate("Call JSON and XML web services from scripts") %>
					<li><%= loginBean.translate("Scrape HTML data from webpages") %>
					<li><%= loginBean.translate("Built-in classes for Facebook, Twitter, Telegram, Slack, GoogleCalendar, Email, Twilio SMS, WolframAlpha, WikiData, Wiktionary, Http, and more") %>
					<li><%= loginBean.translate("Built-in services for XML, JSON, HTML, WikiData, Wiktionary, AIML SRAIX, Pannous, PersonalityForge") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/chatlog.png" class="title"> <%= loginBean.translate("Monitor") %></h4>
						<%= loginBean.translate("Monitor, review, and correct your bot's conversations") %>
				<ul>
					<li><%= loginBean.translate("Chat Logs") %> <%= loginBean.translate("tab makes monitoring your bot easy") %>
					<li><%= loginBean.translate("Filter conversations, responses, and flagged phrases") %>
					<li><%= loginBean.translate("Show response context, keywords, and topic") %>
					<li><%= loginBean.translate("Correct, invalidate, or flag responses") %>
					<li><%= loginBean.translate("Import or export chat logs, response lists, or AIML") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/stats.svg" class="title"> <%= loginBean.translate("Analytics") %></h4>
						<%= loginBean.translate("Graph and analyse your bot's performance and engagement") %>
				<ul>
					<li><%= loginBean.translate("Analytics tab provides key statistics and charts") %>
					<li><%= loginBean.translate("Track conversations, messages, engaged conversations, confidence, and sentiment") %>
					<li><%= loginBean.translate("Graph and compare multiple difference statisitcs") %>
					<li><%= loginBean.translate("View live data, week, month, or all data") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/analytic.png" class="title"> <%= loginBean.translate("Deep Learning") %></h4>
				<%= loginBean.translate("Create deep learning analytics for image classification, or to analyze data") %></a>
				<ul>
					<li><%= loginBean.translate("Free open library of deep learning analytics") %>
					<li><%= loginBean.translate("Analytics web API") %>
					<li><%= loginBean.translate("Upload Tensorflow deep learning neural networks") %>
					<li><%= loginBean.translate("Classify images, and analyze data") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/livechat-icon.png" class="title"> <%= loginBean.translate("Live Chat") %></h4>
				<%= loginBean.translate("Integrate your bot with live chat") %>
				<ul>
					<li><%= loginBean.translate("Create live chat channels and chatrooms") %>
					<li><%= loginBean.translate("Embed live chat on your own website or mobile app") %>
					<li><%= loginBean.translate("Have your bot monitor your channel when your not available") %>
					<li><%= loginBean.translate("Let your bot learn from your live chat conversations") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/forum-icon.png" class="title"> <%= loginBean.translate("Forums") %></h4>
				<%= loginBean.translate("Create your own") %> <a class="blue" href="browse-forum.jsp"><%= loginBean.translate("forums") %></a>
				<ul>
					<li><%= loginBean.translate("Create a forum for your bot") %>
					<li><%= loginBean.translate("Embed forums on your own website or mobile app") %>
					<li><%= loginBean.translate("Browse Help and Info forums") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/issue.png" class="title"> <%= loginBean.translate("Issue Tracking") %></h4>
				<%= loginBean.translate("Create your own") %> <a class="blue" href="browse-issuetracker.jsp"><%= loginBean.translate("issue tracker") %></a>
				<ul>
					<li><%= loginBean.translate("Track issues, bugs, features, task, and service requests") %>
					<li><%= loginBean.translate("Embed your issue tracker on your own website or mobile app") %>
					<li><%= loginBean.translate("Access and post issues and service requests from your bot") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/home.png" class="title"> <%= loginBean.translate("Web Hosting") %></h4>
						<%= loginBean.translate("Host your own chat or bot website") %>
				<ul>
					<li><%= loginBean.translate("Link a subdomain to your bot's personal website") %>
					<li><%= loginBean.translate("Edit your bot's page header and footer in HTML") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/api.png" class="title"> <%= loginBean.translate("API and SDK") %></h4>
				<%= loginBean.translate("Makes building your own website or mobile app easy") %>
				<ul>
					<li><%= loginBean.translate("Access chat bots, live chat, and forums from web") %> <a class="blue" href="api.jsp">API</a>
					<li><%= loginBean.translate("Use the JavaScript") %> <a class="blue" href="sdk.jsp">SDK</a> <%= loginBean.translate("to integrate with your website") %>
					<li><%= loginBean.translate("Reuse") %> <a class="blue" href="sdk.jsp">SDK</a> <%= loginBean.translate("mobile Android and iOS components in your own app") %>
				</ul>
			</div>
			<div class="feature">
				<h4><img src="images/multilingual.png" class="title"> <%= loginBean.translate("Multilingual") %></h4>
				<%= loginBean.translate("Train your bot in any language") %>
				<ul>
					<li><%= loginBean.translate("Bots can learn in any language") %>
					<li><%= loginBean.translate("Support for UTF-8 character sets") %>
					<li><%= loginBean.translate("Choose voice's language") %>
					<li><%= loginBean.translate("Automatic chat translation") %>
				</ul>
			</div>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>