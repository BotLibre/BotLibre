<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded() || loginBean.isFullScreen() || loginBean.isMobile();
	LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
	
	String title = "Live Chat";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
	boolean help = loginBean.getHelp();
	loginBean.setHelp(false);
%>
<!DOCTYPE HTML>
<html class="full">
<head>
	<% bean.writeHeadMetaHTML(out); %>
	<jsp:include page="head.jsp"/>
	<% bean.writeHeadMetaHTML(out); %>
	<title><%= title %><%= loginBean.isEmbedded() ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<% if (loginBean.getCssURL() == "") { %>
		<link rel="stylesheet" href="css/chatroom.css" type="text/css">
	<% } %>
	<script src="scripts/RTCMultiConnection.js"></script>
	<script src="https://rtcmulticonnection.herokuapp.com/socket.io/socket.io.js"></script>
</head>
<% if (embed) { %>
	<body class="full" style="background-color: <%= loginBean.getBackgroundColor() %>;overflow-y:hidden;">
	<table style="background-color: <%= loginBean.getBackgroundColor() %>;" class="<%= Site.PREFIX %>framechat-div">
	<tr><td>
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	</td></tr>
	<tr><td>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="channel-banner.jsp"/>
	<% } %>
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents-full">
	<div class="browse">
<% } %>
<% if (!(Site.REQUIRE_TERMS && !loginBean.isEmbedded() && !loginBean.isLoggedIn() && !loginBean.isAgeConfirmed())) { %>
	<jsp:include page="error.jsp"/>
<% } %>
<% if (bean.getInstance() == null) { %>
	<% if (embed) { %></td></tr><tr height="100%"><td style='text-align:center'><% } %>
	<p style="color:#E00000;"><%= loginBean.translate("Not connected") %></p>
<% } else if (!Site.ANONYMOUS_CHAT && !loginBean.isEmbedded() && !loginBean.isLoggedIn()) { %>
	<% if (embed) { %></td></tr><tr height="100%"><td style='text-align:center'><% } %>
	<script>
		$(function() {
			$( "#dialog-terms" ).dialog({
				autoOpen: true,
				modal: true
			});
		});
	</script>
	<div id="dialog-terms" title="<%= loginBean.translate("Sign In Required") %>" class="dialog">
		<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to chat") %>.
	</div>
<% } else if (Site.REQUIRE_TERMS && !loginBean.isEmbedded() && !loginBean.isLoggedIn() && !loginBean.isAgeConfirmed()) { %>
	<% if (embed) { %></td></tr><tr height="100%"><td><% } %>
		<script>
			function validateForm() {
				var dateOfBirth = document.getElementById("dateOfBirth");
				if (dateOfBirth.value == "") {
					dateOfBirth.className = null;
					dateOfBirth.style.border = "2px solid red";
					SDK.showError('<%= loginBean.translate("Enter a date of birth") %>');
					return false;
				}
				var terms = document.getElementById("terms");
				if (!terms.checked) {
					terms = document.getElementById("terms-span");
					terms.style.color = "red";
					terms.style.fontWeight = "bold";
					SDK.showError('<%= loginBean.translate("You must accept our terms of service") %>');
					return false;
				}
				return true;
			}
			$(function() {
				$( "#dialog-terms" ).dialog({
					autoOpen: true,
					modal: true
				});
			});
		</script>
		<div id="dialog-terms" title="<%= loginBean.translate("Accept Terms") %>" class="dialog">
			<% if (loginBean.isMinor()) { %>
				<p><%= loginBean.translate("You must be 13 years or older to access this service") %></p>
				<form action="login" method="post" class="message">
					<input id="ok" name="cancel" type="submit" onclick="window.location.href = 'livechat?disconnect'; return false;" value="<%= loginBean.translate("OK") %>"/>
				</form>
			<% } else { %>
				<jsp:include page="error.jsp"/>
				<p>
				<%= loginBean.translate("Please") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a>, 
					<a href="<%= "login?sign-up=sign-up" + proxy.proxyString() %>"><%= loginBean.translate("sign up") %></a> <%= loginBean.translate("or,") %><br/>
				<%= loginBean.translate("accept our terms and review our privacy policy.") %><br/>
				</p>
				<form action="login" method="post" class="message">
					<%= proxy.proxyInput() %>
					<% if (Site.AGE_RESTRICT) { %>
						<span><%= loginBean.translate("Date of Birth") %></span><br/>
						<input id="dateOfBirth" name="dateOfBirth" type="date" placeholder="yyyy/mm/dd" value=""  title="<%= loginBean.translate("Enter your date of birth (yyyy/mm/dd, not visible to other users, required)") %>"/><br/>
					<% } %>
					<input id="terms" name="terms" type="checkbox" title="Accept terms of service"><span id="terms-span"><%= loginBean.translate("Accept") %> 
					<% if (Site.DEDICATED) { %>
						<%= loginBean.translate("Terms") %></span><br/>
					<% } else { %>
						<%= loginBean.translate("our") %> <a target="_blank" href="terms.jsp"><%= loginBean.translate("terms") %></a> <%= loginBean.translate("and review our") %> <a target="_blank" href="privacy.jsp"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("policy") %></span><br/>
					<% } %>
					<br/>
					<input id="ok" name="verify-anonymous" type="submit" onclick="return validateForm()" value="<%= loginBean.translate("OK") %>"/>
					<input id="cancel" name="cancel" type="submit" onclick="window.location.href = 'livechat?disconnect'; return false;" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		</div>
<% } else { %>
	<% if (!loginBean.isMobile() && bean.getShowTitle()) { %>
		<div style="float:right;z-index:21;position:relative">
			<% if (!loginBean.isEmbedded()) { %>
				<span class="dropt">
					<div style="right:4px">
						<img src="<%= bean.getAvatarImage(bean.getInstance()) %>" height="250"/>
					</div>
					<img src="<%= bean.getAvatarImage(bean.getInstance()) %>" height="64"/>
				</span>
			<% } else { %>
				<img src="<%= bean.getAvatarImage(bean.getInstance()) %>" height="64"/>
			<% } %>
		</div>
		<span class="dropt">
			<h1><%= bean.getInstanceName() %></h1>
			<div>
				<p class="help">
					<%= bean.getInstance().getDescription() %>
				</p>
			</div>
		</span>
	<% } %>
	<script>
		SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
		var sdk = new SDKConnection();
		var chat = new LiveChatConnection();
		chat.sdk = sdk;
		chat.contactInfo = "<%= Utils.escapeQuotes(bean.getInfo()) %>";
		var web = new WebLiveChatListener();
		web.sdk = sdk;
		<% if (loginBean.isEmbedded()) { %>
			web.linkUsers = false;
			web.onlineBar = true;
			web.isFrame = true;
		<% } %>
		web.chatLog = <%= bean.getChatLog() %>;
		web.bubble = <%= bean.getShowChatBubble() %>;
		web.prefix = "<%= Site.PREFIX %>frame";
		web.lang = "<%= bean.getLanguage() %>";
		
		web.nick = <%= loginBean.getUser() == null ? "null" : "'" + loginBean.getUserId() + "'" %>;
		<% if (loginBean.isLoggedIn()) { %>
			var user = new UserConfig();
			user.user = '<%= loginBean.getUser().getUserId() %>';
			user.token = '<%= loginBean.getUser().getToken() %>';
			sdk.user = user;
		<% } else { %>
			var user = null;
		<% } %>
		<% if (loginBean.isMobile()) { %>
			web.focus = false;
		<% } %>
		var channel = new ChannelConfig();
		channel.id = "<%= bean.getInstanceId() %>";
		chat.listener = web;
		chat.connect(channel, user);
	</script>
	
	<% if (!embed) { %>
		<table class="<%= Site.PREFIX %>framechat-div">
		<tr><td>
	<% } %>
		<div id="<%= Site.PREFIX %>frameonline-div" class="<%= Site.PREFIX %>frameonline-div">
			<div id="<%= Site.PREFIX %>frameonline" class="<%= Site.PREFIX %>frameonline">
				<table></table>
			</div>
		</div>
	</td></tr>
	<tr height="100%"><td valign="top">
		<table id="<%= Site.PREFIX %>frametable-scroll" cellspacing="8" width="100%">
			<tr height="100%">
				<td align="left" valign="top">
					<div id="<%= Site.PREFIX %>frameavatar-div" class="<%= Site.PREFIX %>frameavatar-div" style="display:none;">
						<div id="<%= Site.PREFIX %>frameavatar-image-div">
							<img id="<%= Site.PREFIX %>frameavatar" class="<%= Site.PREFIX %>frameavatar"/>
						</div>
						<div id="<%= Site.PREFIX %>frameavatar-video-div" class="<%= Site.PREFIX %>frameavatar-video-div">
							<video muted='true' id="<%= Site.PREFIX %>frameavatar-video" class="<%= Site.PREFIX %>frameavatar-video" preload="auto">
								Video format not supported by your browser (try Chrome)
							</video>
						</div>
						<div id="<%= Site.PREFIX %>frameavatar-canvas-div" class="<%= Site.PREFIX %>frameavatar-canvas-div" style="display:none;">
							<canvas id="<%= Site.PREFIX %>frameavatar-canvas" class="<%= Site.PREFIX %>frameavatar-canvas">
								Canvas not supported by your browser (try Chrome)
							</canvas>
						</div>
						<div id="<%= Site.PREFIX %>frameavatar-game-div" class="avatar-game-div" style="display:none"></div>
					</div>
		<td valign="top" align="left" class="<%= Site.PREFIX %>framebot-scroller" width="100%">
			<div id="<%= Site.PREFIX %>framescroller" class="<%= Site.PREFIX %>framescroller" style="max-height:340px;">
				<table id="<%= Site.PREFIX %>frameconsole" cellspacing=2 style="width:100%"></table>
			</div>
	</td>
	</td></tr></table>
	</td></tr>
	<tr><td>
		<div id="<%= Site.PREFIX %>framebubble-div" class="<%= Site.PREFIX %>framebubble-div">
			<% if (bean.getShowChatBubble()) { %>
				<div class="<%= Site.PREFIX %>framebubble">
					<div class="<%= Site.PREFIX %>framebubble-text">
						<span id="<%= Site.PREFIX %>frameresponse" class="<%= Site.PREFIX %>frameresponse"></span><br>
					</div>
				</div>
			<% } else { %>
				<div>
					<div class="<%= Site.PREFIX %>framebubble-text">
						<span id="<%= Site.PREFIX %>frameresponse" class="<%= Site.PREFIX %>frameresponse"></span><br>
					</div>
				</div>
			<% } %>
		</div>
		<form onsubmit="return web.sendMessage();" class="message">
			<table style="width:100%;">
			<tr style="width:100%;">
				<td style="width:100%;"><input id="<%= Site.PREFIX %>framechat" class="<%= Site.PREFIX %>framechat-input" placeholder="<%= bean.getPrompt() %>" type="text" name="input"
						<%= loginBean.isMobile() ? "" : "autofocus" %> x-webkit-speech /></td>
				<td align="left"><input id="sendicon" class="sendicon" type="submit" name="submit" value=""/><br/></td>
			</tr>
			</table>
		</form>
		
		<% if (bean.getMenubar()) { %>
			<span class="<%= Site.PREFIX %>menu">
				<div style="inline-block;position:relative">
					<span class="dropt">
						<div style="text-align:left;bottom:32px">
							<table>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.ping();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/ping.svg" title="<%= loginBean.translate("Verify your connection to the server") %>"> <%= loginBean.translate("Ping server") %></a></td>
								</tr>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return toggleKeepAlive();" href="#"><img id="keepalive" class="<%= Site.PREFIX %>menu" src="images/empty.png" title="<%= loginBean.translate("Ping the server every minute to keep the connection alive") %>"> <%= loginBean.translate("Keep Alive") %></a></td>
								</tr>
						 		<% if (!bean.getInstance().isOneOnOne() || bean.isAdmin()) { %>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.flag();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/flag2.svg" title="<%= loginBean.translate("Flag a user for offensive content") %>"> <%= loginBean.translate("Flag user") %></a></td>
									</tr>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.whisper();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/whisper.png" title="<%= loginBean.translate("Send a private message to another user") %>"> <%= loginBean.translate("Whisper user") %></a></td>
									</tr>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.pvt();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/accept.svg" title="<%= loginBean.translate("Invite another user to a private channel") %>"> <%= loginBean.translate("Request private") %></a></td>
									</tr>
								 	<% if (bean.isAdmin()) { %>
										<tr class="<%= Site.PREFIX %>menuitem">
											<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.boot();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/boot.png" title="<%= loginBean.translate("Evict a user from the channel") %>"> <%= loginBean.translate("Boot user") %></a></td>
										</tr>
										<tr class="<%= Site.PREFIX %>menuitem">
											<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.spyMode();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/spy.png" title="<%= loginBean.translate("Spy on all messages in the channel") %>"> <%= loginBean.translate("Spy mode") %></a></td>
										</tr>
								 	<% } %>
								<% } %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.clear();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/empty.png" title="<%= loginBean.translate("Clear the local chat log") %>"> <%= loginBean.translate("Clear log") %></a></td>
								</tr>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.accept();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/accept.svg" title="<%= loginBean.translate("Accept a private request from an operator, bot, or another user") %>"> <%= loginBean.translate("Accept private") %></a></td>
								</tr>
								<% if (bean.getSendImage()) { %>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.sendImage();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/image.svg" title="<%= loginBean.translate("Resize and send an image attachment") %>"> <%= loginBean.translate("Send image") %></a></td>
									</tr>
								<% } %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.sendAttachment();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/attach.svg" title="<%= loginBean.translate("Send an image or file attachment") %>"> <%= loginBean.translate("Send file") %></a></td>
								</tr>
								<tr id="<%= Site.PREFIX %>frameChatLogRow" class="<%= Site.PREFIX %>menuitem" style="display:none;">
									<td><a id="<%= Site.PREFIX %>frameChatLogLink" class="<%= Site.PREFIX %>menuitem" onClick="hideAvatar();" href="#">
										<img class="<%= Site.PREFIX %>menu" src="images/chat_log.svg" title="<%= loginBean.translate("Show Chat Log") %>"> <%= loginBean.translate("Chat Log") %></a>
									</td>
								</tr>
								<tr id="<%= Site.PREFIX %>frameAvatarRow" class="<%= Site.PREFIX %>menuitem">
									<td><a id="<%= Site.PREFIX %>frameAvatarLink" class="<%= Site.PREFIX %>menuitem" onClick="showAvatar();" href="#">
										<img class="<%= Site.PREFIX %>menu" src="images/avatar-icon.png" title="<%= loginBean.translate("Show Avatar") %>"> <%= loginBean.translate("Avatar") %></a>
									</td>
								</tr>
								<% if (bean.allowsMedia() && (bean.getInstance().isChatRoom())) { %>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return configureMedia();" href="#"><img id="media" class="<%= Site.PREFIX %>menu" src="images/video.svg" title="<%= loginBean.translate("Configure audio and video sharing and options") %>"> <%= loginBean.translate("Media settings") %></a></td>
									</tr>
								<% } %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return toggleSound();" href="#"><img id="sound" class="<%= Site.PREFIX %>menu" src="images/sound.svg" title="<%= loginBean.translate("Toggle sound") %>"> <%= loginBean.translate("Sound") %></a></td>
								</tr>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return toggleListen();" href="#">
											<img id="mic" class="<%= Site.PREFIX %>menu" src="images/micoff.svg" title="<%= loginBean.translate("Enable speech recognition (browser must support HTML5 speech recognition, such as Chrome)") %>"> <%= loginBean.translate("Speech recognition") %></a>
									</td>
								</tr>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.exit();" href="#"><img class="<%= Site.PREFIX %>menu" src="images/quit.svg" title="<%= loginBean.translate("Exit the channel or active private channel") %>"> <%= loginBean.translate("Quit private or channel") %></a></td>
								</tr>
								<% if (!loginBean.isEmbedded()) { %>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td>
											<a class="<%= Site.PREFIX %>menuitem" href="#" onclick="document.getElementById('disconnect').click()"><img class="<%= Site.PREFIX %>menu" src="images/logout.svg" title="<%= loginBean.translate("Exit the channel, and go back to the channel page") %>"> <%= loginBean.translate("Exit chat") %></a>
											<form action="livechat" style="display:none">
												<%= proxy.proxyInput() %>
												<input type="submit" id="disconnect" name="disconnect"/>
											</form>
										</td>
									</tr>
								<% } %>
							</table>
						</div>
						<img class="<%= Site.PREFIX %>toolbar" src="images/menu.png">
					</span>
					<a onClick="return web.accept();" href="#"><img class="<%= Site.PREFIX %>toolbar" src="images/accept.svg" title="<%= loginBean.translate("Accept a private request from an operator, bot, or another user") %>"></a>
					<% if (bean.getSendImage()) { %>
						<a onClick="return web.sendImage();" href="#"><img class="<%= Site.PREFIX %>toolbar" src="images/image.svg" title="<%= loginBean.translate("Resize and send an image attachment") %>"></a>
						<a onClick="return web.sendAttachment();" href="#"><img class="<%= Site.PREFIX %>toolbar" src="images/attach.svg" title="<%= loginBean.translate("Send a file or media attachment") %>"></a>
					<% } %>
					<% if (bean.allowsMedia() && (bean.getInstance().isChatRoom())) { %>
						<a onClick="return configureMedia();" href="#"><img id="media" class="<%= Site.PREFIX %>toolbar" src="images/video.svg" title="<%= loginBean.translate("Configure audio and video sharing and options") %>"></a>
					<% } %>
					<a id="<%= Site.PREFIX %>soundButton" onClick="toggleSound();" href="#"><img id="soundButton" class="<%= Site.PREFIX %>toolbar" src="images/sound.svg" title="<%= loginBean.translate("Sound") %>"></a>
					<a onClick="return web.exit();" href="#"><img class="<%= Site.PREFIX %>toolbar" src="images/quit.svg" title="<%= loginBean.translate("Exit the channel or active private channel") %>"></a>
					<a id="<%= Site.PREFIX %>frameChatLogButton" onClick="hideAvatar();" href="#" style="display:none;"><img class="toolbar" src="images/chat_log.svg" title="<%= loginBean.translate("Show Chat Log") %>"></a>
					<a id="<%= Site.PREFIX %>frameAvatarButton" onClick="showAvatar();" href="#"><img class="toolbar" src="images/avatar-icon.png" title="<%= loginBean.translate("Show Avatar") %>"></a>
					<% if (!loginBean.isEmbedded()) { %>
						<a href="#" onclick="document.getElementById('disconnect').click()"><img class="<%= Site.PREFIX %>toolbar" src="images/logout.svg" title="<%= loginBean.translate("Exit the channel, and go back to the channel page") %>"></a>
					<% } %>
				</div>
			</span>
		<% } %>

		<% if (!loginBean.isEmbedded() && !loginBean.isMobile()) { %>
			<span class="<%= Site.PREFIX %>menu"><%= loginBean.translate("All conversations are recorded, and may be reviewed by the channel administrator, see") %> <a href="privacy.jsp" target="_blank"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("for details") %>.</span>
		<% } %>
	<% if (!embed) { %>
		</td></tr>
		</table>
	<% } %>
	
	<% bean.writeAd(out); %>
	
	<script>
		function showAvatar() {
			web.toggleAvatar = true;
			document.getElementById(web.prefix + "avatar-div").style.display = "block";
			document.getElementById(web.prefix + "ChatLogRow").style.display = "block";
			document.getElementById(web.prefix + "ChatLogButton").style.display = "inline-block";
			document.getElementById(web.prefix + "AvatarButton").style.display = "none";
			document.getElementById(web.prefix + "AvatarRow").style.display = "none";
		}
		
		function hideAvatar() {
			web.toggleAvatar = false;
			document.getElementById(web.prefix + "avatar-div").style.display = "none";
			document.getElementById(web.prefix + "ChatLogRow").style.display = "none";
			document.getElementById(web.prefix + "ChatLogButton").style.display = "none";
			document.getElementById(web.prefix + "AvatarButton").style.display = "inline-block";
			document.getElementById(web.prefix + "AvatarRow").style.display = "block";

		}
		
		<% if (embed) { %>
			var onlineDiv = document.getElementById(web.prefix + 'online-div');
			onlineDiv.style.maxWidth = (window.innerWidth - 10) + "px";
			onlineDiv.style.display = "inline-block";
			onlineDiv.style.overflowX = "auto";
			onlineDiv.style.overflowY = "hidden";
			onlineDiv.style.whiteSpace = "nowrap";
			onlineDiv.style.width = "100%";
			window.onresize = function() {
				var max = Math.min(screen.width, window.innerWidth);
				onlineDiv.style.maxWidth = (max - 10) + "px";
			};
		<% } %>
		<% if (bean.getChatLog()) { %>
			if (web.bubble) {
				document.getElementById(web.prefix + 'response').style.display = "inline-block";
			} else {
				document.getElementById(web.prefix + 'bubble-div').style.display = "none";
				document.getElementById(web.prefix + 'response').style.display = "none";
			}
		<% } else { %>
			document.getElementById(web.prefix + 'scroller').style.display = "none";
			document.getElementById(web.prefix + 'response').style.display = "inline-block";
		<% } %>
		var scroller = document.getElementById(web.prefix + 'scroller');
		scroller.style.maxHeight = scroller.parentNode.offsetHeight + "px";
		var tableScroll = document.getElementById(web.prefix + 'table-scroll');
		var reset = true;
		var resize = function() {
			scroller.style.maxHeight = "200px";
			if (reset) {
				reset = false;
				setTimeout(function() {
					reset = true;
					//scroller.style.maxHeight = (scroller.parentNode.offsetHeight - 4) + "px";
					scroller.style.maxHeight = (tableScroll.parentNode.offsetHeight - 48) + "px";
				}, 100);
			}
		}
		window.onresize = resize;
		setTimeout(resize, 1000);
	</script>
	<script>
		SDK.registerSpeechRecognition(document.getElementById(web.prefix + 'chat'), document.getElementById('sendicon'));
		var listen = false;
		
		function toggleKeepAlive() {
			web.toggleKeepAlive();
			if (web.connection.keepAlive) {
				document.getElementById('keepalive').src = "images/ping.svg";
			} else {
				document.getElementById('keepalive').src = "images/empty.png";
			}
			return false;
		}
		
		function toggleListen() {
			listen = !listen;
			if (listen) {
				SDK.startSpeechRecognition();
				document.getElementById('mic').src = "images/mic.svg";
			} else {
				SDK.stopSpeechRecognition();
				document.getElementById('mic').src = "images/micoff.svg";
			}
			return false;
		}
		
		function toggleSound() {
			web.toggleSound();
			if (web.sound) {
				document.getElementById('sound').src = "images/sound.svg";
				document.getElementById('soundButton').src = "images/sound.svg";
			} else {
				document.getElementById('sound').src = "images/mute.svg";
				document.getElementById('soundButton').src = "images/mute.svg";
			}
			return false;
		}
		
	</script>
	<script>
		var shareVideo = false;
		var shareAudio = false;
		var allowMedia = false;
		var showControls = false;
		var channelToken = null;
		var videosContainer = document.getElementById(web.prefix + 'online-div');
		var setupVideo = function() {
			if (chat.mediaConnection != null) {
				if (!allowMedia) {
					chat.disconnectMedia();
					return false;
				}
				chat.resetMedia(shareAudio, shareVideo);
				return false;
			}
			if (!allowMedia) {
				return false;
			}
			chat.onMediaStream = function(e) {
				if (!showControls) {
					e.mediaElement.removeAttribute("controls");
				}
			    e.mediaElement.height = <%= loginBean.isMobile() ? "100" : "200" %>;
			    var id = encodeURIComponent(e.userid);
			    var userid = e.userid.substring(5);
			    var userdiv = document.getElementById(id);
			    var nameLabel;
			    if (userdiv != null) {
			    	if (userdiv.getAttribute('media') == null) {
				        if (userdiv.parentNode) {
				        	userdiv.parentNode.removeChild(userdiv);
				        }
				        nameLabel = userdiv.lastElementChild;
			    	} else {
			    		userdiv.removeChild(userdiv.firstElementChild);
			    		userdiv.insertBefore(e.mediaElement, userdiv.firstChild);
					    setTimeout(resize, 1000);
			    		return;
			    	}
			    }
			    var videobox = document.createElement('div');
			    videobox.id = id;
			    videobox.setAttribute('userid', e.userid);
			    videobox.setAttribute('media', 'true');
			    videobox.className = web.prefix + "online-user";
			    videobox.style.cssFloat = "left";
			    videobox.style.paddingRight = "7px";
			    videobox.appendChild(e.mediaElement);
			    if (nameLabel == null) {
				    nameLabel = document.createElement('div');
				    nameLabel.className = web.prefix + "online-user-label";
				    nameLabel.innerHTML = userid;
				}
			    videobox.appendChild(nameLabel);
			    var menu = document.createElement('div');
			    var muteUserImgSrc = "user-"+web.nick == e.userid ? "images/mute.svg" : "images/sound.svg" ;
			    menu.innerHTML = "<div style='inline-block;position:relative'><div><span class='dropt'>"
					+ "<div style='text-align:left;bottom:22px'><table>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return muteAudio('" + e.userid + "');\" href='#'><img id='" + e.userid + "muteAudio' class='<%= Site.PREFIX %>menu' src='" + muteUserImgSrc + "' title='<%= loginBean.translate("Mute user") %>'> <%= loginBean.translate("Mute user") %></a></td></tr>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return muteVideo('" + e.userid + "');\" href='#'><img id='" + e.userid + "muteVideo' class='<%= Site.PREFIX %>menu' src='images/video.svg' title='<%= loginBean.translate("Stop video") %>'> <%= loginBean.translate("Stop video") %></a></td></tr>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return web.expandVideo('" + e.userid + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/zoomin.svg' title='<%= loginBean.translate("Increase the video size") %>'> <%= loginBean.translate("Expand video") %></a></td></tr>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return web.shrinkVideo('" + e.userid + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/zoomout.svg' title='<%= loginBean.translate("Shrink the video size") %>'> <%= loginBean.translate("Shrink video") %></a></td></tr>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return web.pvt('" + userid  + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/accept.svg' title='<%= loginBean.translate("Invite user to a private channel") %>'> <%= loginBean.translate("Private user") %></a></td></tr>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return web.whisper('" + userid  + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/whisper.png' title='<%= loginBean.translate("Send private message to user") %>'> <%= loginBean.translate("Whipser user") %></a></td></tr>"
					+ "<tr class='<%= Site.PREFIX %>menuitem'><td><a class='<%= Site.PREFIX %>menuitem' onClick=\"return web.flag('" + userid  + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/flag2.svg' title='<%= loginBean.translate("Flag the user for offensive content") %>'> <%= loginBean.translate("Flag user") %></a></td></tr>"
					+ "</table></div><img class='<%= Site.PREFIX %>menu' src='images/menu.png'></span>"
			    	+ "<a onClick=\"return muteAudio('" + e.userid + "');\" href='#'><img id='" + e.userid + "muteAudioButton' class='<%= Site.PREFIX %>menu' src='" + muteUserImgSrc + "' title='<%= loginBean.translate("Mute user") %>'></a>"
			    	+ "<a onClick=\"return muteVideo('" + e.userid + "');\" href='#'><img id='" + e.userid + "muteVideoButton' class='<%= Site.PREFIX %>menu' src='images/video.svg' title='<%= loginBean.translate("Stop video") %>'></a>"
			    	+ "<a onClick=\"return web.expandVideo('" + e.userid + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/zoomin.svg' title='<%= loginBean.translate("Increase the video size") %>'></a>"
			    	+ "<a onClick=\"return web.shrinkVideo('" + e.userid + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/zoomout.svg' title='<%= loginBean.translate("Shrink the video size") %>'></a>"
			    	//+ "<a onClick=\"return web.pvt('" + userid + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/accept.svg' title='<%= loginBean.translate("Invite user to a private channel") %>'></a>"
			    	//+ "<a onClick=\"return web.whisper('" + userid + "');\" href='#'><img class='<%= Site.PREFIX %>menu' src='images/whisper.png' title='<%= loginBean.translate("Send private message to user") %>'></a>"
			    	+ "</div></div>";
			    videobox.appendChild(menu);
			    videosContainer.insertBefore(videobox, videosContainer.firstChild);
			    setTimeout(resize, 1000);
			};
			chat.onMediaStreamEnded = function(e) {
			    e.mediaElement.style.opacity = 0;
			};
			chat.connectMedia(channelToken, shareAudio, shareVideo);
		}
		var configureMedia = function() {
			$( "#dialog-media" ).dialog("open");
			return false;
		}
		<% if (bean.allowsMedia()) { %>
			chat.onNewChannel = function(token) {
				videosContainer.innerHTML = "<div id='" + "<%= Site.PREFIX %>frame" + "online' class='" + "<%= Site.PREFIX %>frame" + "online'><table></table></div>";
				chat.disconnectMedia();
				channelToken = token;
				<% if (bean.getInstance().isChatRoom()) { %>
					configureMedia();
				<% } else { %>
					if (token.indexOf("private") != -1) {
						configureMedia();
					}
				<% } %>
			}
		<% } %>
	</script>
	<script>
		function muteAudio(userid) {
			var muted = web.muteAudio(userid);
			if (muted) {
				document.getElementById(userid + 'muteAudio').src = "images/mute.svg";
				document.getElementById(userid + 'muteAudioButton').src = "images/mute.svg";
			} else {
				document.getElementById(userid + 'muteAudio').src = "images/sound.svg";
				document.getElementById(userid + 'muteAudioButton').src = "images/sound.svg";
			}
			return false;
		}
		function muteVideo(userid) {
			var muted = web.muteVideo(userid);
			if (muted) {
				document.getElementById(userid + 'muteVideo').src = "images/mutevideo.png";
				document.getElementById(userid + 'muteVideoButton').src = "images/mutevideo.png";
			} else {
				document.getElementById(userid + 'muteVideo').src = "images/video.svg";
				document.getElementById(userid + 'muteVideoButton').src = "images/video.svg";
			}
			return false;
		}
	</script>
	<div id="dialog-media" title="Media Permission" class="dialog">
		<p><%= loginBean.translate("This channel allows audio and video chat.  Please select your media preference.") %></p>
		<input type="checkbox" checked id="allow-media" title="<%= loginBean.translate("Allow viewing, listening to, and sharing channel audio and video media") %>"><%= loginBean.translate("Allow media") %></input><br/>
		<% if (bean.allowAudio()) { %>
			<input type="checkbox" id="share-audio" title="<%= loginBean.translate("Share audio using your microphone. Ensure your broadcast complies with our terams of service and is not adult, or offensive.") %>"><%= loginBean.translate("Share your audio") %></input><br/>
		<% } %>
		<% if (bean.allowVideo()) { %>
			<input type="checkbox" id="share-video" title="<%= loginBean.translate("Share video using your webcam. Ensure your broadcast complies with our terams of service and is not adult, or offensive.") %>"><%= loginBean.translate("Share your video") %></input><br/>
		<% } %>
		<input type="checkbox" checked id="media-controls" title="<%= loginBean.translate("Show audio/video controls") %>"><%= loginBean.translate("Show media controls") %></input><br/>
		<% if (!loginBean.isEmbedded()) { %>
			<span class="<%= Site.PREFIX %>menu">
				<%= loginBean.translate("Ensure you media content complies with our") %> <a href="terms.jsp" target="_blank"><%= loginBean.translate("terms") %></a>.<br/>
				<%= loginBean.translate("Use") %> <a href="<%= Site.SECUREURLLINK %>">https</a> <%= loginBean.translate("for audio/video on Chrome.") %>
			</span>
		<% } %>
	</div>
	<script>
		$(function() {
			$( "#dialog-media" ).dialog({
				autoOpen: false,
				modal: false,
				buttons: {
					Ok: function() {
						$(this).dialog("close");
						allowMedia = document.getElementById('allow-media').checked;
						<% if (bean.allowVideo()) { %>
							shareVideo = document.getElementById('share-video').checked;
						<% } %>
						<% if (bean.allowAudio()) { %>
							shareAudio = document.getElementById('share-audio').checked;
						<% } %>
						showControls = document.getElementById('media-controls').checked;
						setupVideo();
					}
				}
			});
		});
	</script>
<% } %>
<% if (!embed) { %>
	</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
	</td></tr>
	</table>
<% } %>

<% loginBean.setHelp(help); %>
<% proxy.clear(); %>
</body>
</html>
