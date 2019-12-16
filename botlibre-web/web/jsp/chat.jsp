<%@page import="org.botlibre.web.service.BotStats"%>
<%@page import="org.botlibre.web.bean.VoiceBean"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page import="org.botlibre.web.bean.ChatBean"%>
<%@ page import="org.botlibre.web.bean.LearningBean"%>
<%@ page import="org.botlibre.web.bean.BotBean"%>
<%@ page import="org.botlibre.web.admin.ClientType"%>
<%@ page import="org.botlibre.emotion.EmotionalState"%>
<%@ page import="org.botlibre.web.bean.LoginBean.Page" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded() || loginBean.isFullScreen() || loginBean.isMobile();
	BotBean botBean = loginBean.getBotBean();
	loginBean.setActiveBean(botBean);
	botBean.connect(ClientType.WEB, request);
	LearningBean learningBean = loginBean.getBean(LearningBean.class);
	VoiceBean voiceBean = loginBean.getBean(VoiceBean.class);
	ChatBean chatBean = loginBean.getBean(ChatBean.class);
	boolean help = loginBean.getHelp();
	loginBean.setHelp(false);
%>

<!DOCTYPE HTML>
<html class="full">
<head>
	<% botBean.writeHeadMetaHTML(out); %>
	<jsp:include page="head.jsp"/>
	<% botBean.writeHeadMetaHTML(out); %>
	<title><%= botBean.getInstanceName() %> Chat <%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<% if (loginBean.getFocus()) { %>
		<script type="text/javascript">
		window.onload = function()
		{
			var divObject = document.getElementById('scroller');
			if (divObject != null) {
				divObject.scrollTop = divObject.scrollHeight;
			}
		}
		</script>
	<% } %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body class="full" style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<table style="background-color: <%= loginBean.getBackgroundColor() %>;" class="chat-div">
	<tr><td>
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	</td></tr>
	<tr><td>
	<% if (!loginBean.isEmbedded() || chatBean.getLoginBanner()) { %>
		<jsp:include page="instance-banner.jsp"/>
	<% } %>
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents-full">
	<div class="browse">
<% } %>
<jsp:include page="error.jsp"/>
<% if (embed && !Site.COMMERCIAL && loginBean.getShowLink()) { %>
	<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>;">
		<span>chat hosted by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
	</div>
<% } %>
<% if (proxy.getBeanId() == null) { %>
	<p style="color:#E00000;"><%= loginBean.translate("No bot has been selected, or session has timed out, you need to select a bot from browse") %></p>
<% } else { %>
	<% if (!embed) { %>
		<table class="chat-div">
		<tr><td>
	<% } %>
	</td></tr>
	<tr><td>
	<% if (chatBean.getShowAvatar()) { %>
		<% if (!loginBean.isMobile() && chatBean.getShowTitle()) { %>
			<span class="dropt">
				<h1><%= botBean.getInstanceName() %></h1>
				<div>
					<p class="help">
						<%= botBean.getInstance() == null ? "" : botBean.getInstance().getDescription() %>
					</p>
				</div>
			</span>
		<% } %>
	</td></tr>
	<tr height="100%"><td valign="top">
	<table id="table-scroll" cellspacing="8">
	<tr height="100%">
	<% if (botBean.getAvatarFileName() != null) { %>
		<td align="left" valign="top">
				<% if (!loginBean.isEmbedded() || chatBean.getAvatarExpandable()) { %>
					<a href="<%= "chat?chatLog=" + !chatBean.getShowChatLog() + proxy.proxyString() %>" style="text-decoration:none">
				<% } %>
				<% if (botBean.isVideoAvatar()) { %>
					<% if (chatBean.getShowChatLog()) { %>
						<div id="avatar-video-div" class="avatar-video-div" style="background-image:url('<%= botBean.getAvatarBackground() %>');">
							<video id="avatar-video" class="avatar-video" preload="auto" src="<%= botBean.getAvatarFileName() %>">
								Video format not supported by your browser (try Chrome)
							</video>
						</div>
						<div id="avatar-canvas-div" class="avatar-canvas-div">
							<canvas id="avatar-canvas" class="avatar-canvas">
							</canvas>
						</div>
					<% } else { %>
						<div id="avatar-video-div" class="avatar-video-div-big" style="background-image:url('<%= botBean.getAvatarBackground() %>');">
							<video id="avatar-video" class="avatar-video-big" preload="auto" src="<%= botBean.getAvatarFileName() %>">
								Video format not supported by your browser (try Chrome)
							</video>
						</div>
						<div id="avatar-canvas-div" class="avatar-canvas-div-big">
							<canvas id="avatar-canvas" class="avatar-canvas-big">
							</canvas>
						</div>
					<% } %>
				<% } else { %>
					<div id="avatar-image-div">
					<% if (chatBean.getShowChatLog()) { %>
						<% if (!loginBean.isEmbedded()) { %>
							<span class="dropt">
								<div style="right:4px">
									<img id="avatar2" class="avatar2" src="<%= botBean.getAvatarFileName() %>"/>
								</div>
								<img id="avatar" class="avatar" src="<%= botBean.getAvatarFileName() %>"/>
							</span>
						<% } else { %>
							<img id="avatar" class="avatar" src="<%= botBean.getAvatarFileName() %>"/>
						<% } %>
					<% } else { %>
						<img id="avatar" class="avatar-big" src="<%= botBean.getAvatarFileName() %>"/>
					<% } %>
					</div>
				<% } %>
				<% if (!loginBean.isEmbedded() || chatBean.getAvatarExpandable()) { %>
					</a>
				<% } %>
				<br/>
				<span class="menu"><%= botBean.getStatus() %></span>
			</td>
		<% } %>
		<% if (chatBean.getShowChatLog()) { %>
			<td valign="top" align="left" class="bot-scroller" width="100%">
				<div id="scroller" class="scroller" style="max-height:100px">
				<%= chatBean.getChatLog() %>
				</div>
			</td>
		<% } %>
		</tr>
		</table>
	<% } %>
	</td></tr>
	<tr><td>
	<% if (chatBean.getResponse() != null) { %>
		<p class="response" style="margin:3px"><%= chatBean.getResponseHTML() %></p>
	<% } %>
	<script>
		var response = new ChatResponse();
		response.message = "<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(chatBean.getResponse()))) %>";
		response.avatar = "<%= botBean.getAvatarFileName() %>";
		response.avatarType = "<%= botBean.getAvatarFileType() %>";
		<% if (botBean.getAvatarTalkFileName() != null) { %>
			response.avatarTalk = "<%= botBean.getAvatarTalkFileName() %>";
		<% } %>
		<% if (botBean.getAvatarActionFileName() != null) { %>
			response.avatarAction = "<%= botBean.getAvatarActionFileName() %>";
			response.avatarActionType = "<%= botBean.getAvatarActionFileType() %>";
		<% } %>
		<% if (botBean.getAvatarActionAudioFileName() != null) { %>
			response.avatarActionAudio = "<%= botBean.getAvatarActionAudioFileName() %>";
			response.avatarActionAudioType = "<%= botBean.getAvatarActionAudioFileType() %>";
		<% } %>
		<% if (botBean.getAvatarAudioFileName() != null) { %>
			response.avatarAudio = "<%= botBean.getAvatarAudioFileName() %>";
			response.avatarAudioType = "<%= botBean.getAvatarAudioFileType() %>";
		<% } %>
		<% if (chatBean.getResponseFileName() != null) { %>
			response.speech = "<%= chatBean.getResponseFileName() %>";
		<% } %>
		SDK.updateAvatar(response, <%= chatBean.getSpeak() %>, "");
	</script>
	<form action="chat" method="post" class="message">
		<%= loginBean.postTokenInput() %>
		<%= proxy.proxyInput() %>
		<%= botBean.instanceInput() %>
		<table style="width:100%;">
		<tr style="width:100%;">
			<td style="width:100%;"><input id="chat" class="chat-input" placeholder="<%= chatBean.getPrompt() %>" type="text" name="input" <%= loginBean.isMobile() ? "" : "autofocus" %> x-webkit-speech /></td>
			<td align="left"><input id="send" type="submit" name="submit" value="<%= chatBean.getSend() %>"/><br/></td>
		</tr>
		</table>
		
		<span class="menu">
		<div style="inline-block;position:relative">
			<span class="dropt">
				<div style="text-align:left;bottom:36px">
					<table>
						<% if (loginBean.isMobile() && !loginBean.isEmbedded() && botBean.isAdmin()) { %>
							<tr class="menuitem">
								<td><input class="search" title="Enable logging and debugging info" type="checkbox" name="debug" <% if (chatBean.isDebug()) { %>checked<% } %>>
									 <span class="menuitem"> Debug</span>
						 			<select class="search" name="debugLevel" style="min-width:0;width:auto;">
										<option value="OFF" <%= botBean.isLogLevelSelected("OFF") %>>OFF</option>
										<option value="SEVERE" <%= botBean.isLogLevelSelected("SEVERE") %>>SEVERE</option>
										<option value="WARNING" <%= botBean.isLogLevelSelected("WARNING") %>>WARNING</option>
										<option value="CONFIG" <%= botBean.isLogLevelSelected("CONFIG") %>>CONFIG</option>
										<option value="INFO" <%= botBean.isLogLevelSelected("INFO") %>>INFO</option>
										<option value="FINE" <%= botBean.isLogLevelSelected("FINE") %>>FINE</option>
										<option value="FINER" <%= botBean.isLogLevelSelected("FINER") %>>FINER</option>
										<% if (botBean.isSuper()) { %>
											<option value="FINEST" <%= botBean.isLogLevelSelected("FINEST") %>>FINEST</option>
											<option value="ALL" <%= botBean.isLogLevelSelected("ALL") %>>ALL</option>
										<% } %>
									</select>
								</td>
							</tr>
						<% } %>
						<% if (!loginBean.isEmbedded() && chatBean.showLearning()) { %>
							<tr class="menuitem">
								<td> <input class="search" title="Enable learning" type="checkbox" name="learning" <% if (chatBean.getAllowLearning()) { %>checked<% } %>>
								 <span class="menuitem"> <%= loginBean.translate("Learn") %></span></td>
							</tr>
						<% } %>
						<% if (chatBean.getAllowSpeech()) { %>
							<tr class="menuitem">
								<td><a class="menuitem" onClick="return toggleSpeak();" href="#" title="Enable speech (speech requires an HTML5 audio supporting browser, such as Chrome or Firefox)"><img id="speak2" class="menu" src="images/sound.svg"> <%= loginBean.translate("Speech") %></a></td>
							</tr>
							<tr class="menuitem">
								<td><a class="menuitem" onClick="return toggleListen();" href="#" title="Enable speech recognition (browser must support HTML5 speech API, such as Chrome)"><img id="listen2" class="menu" src="images/micoff.svg"> <%= loginBean.translate("Speech Recognition") %></a></td>
							</tr>
						<% } %>
						<% if (loginBean.isMobile() && (!loginBean.isEmbedded() || chatBean.getAllowEmotes()) && (learningBean.getEnableEmoting() || botBean.isAdmin())) { %>
							<tr class="menuitem">
								<td nowrap>
									<span class="menuitem"><%= loginBean.translate("Emote") %></span>
									<select class="search" id="emote" name="emote" title="Emote associates an emotion with your response" style="min-width:0;width:auto;">
										<% for (EmotionalState state : EmotionalState.values()) { %>
											<option value="<%= state.name() %>"><%= state.name().toLowerCase() %></option>
										<% } %>
									</select>
								</td>
							</tr>
							<tr class="menuitem">
								<td nowrap>
									<span class="menuitem"><%= loginBean.translate("Action") %></span>
									<input id="action" class="search" type="text" name="action" title="Associate an action with your response" style="width:80px;">
									<script>
									$( "#action" ).autocomplete({
										source: [<%= botBean.getAllActionsString() %>],
									    minLength: 0
										}).on('focus', function(event) {
										    var self = this;
										    $(self).autocomplete("search", "");
										});
									</script>
								</td>
							</tr>
						<% } %>
						<% if (!loginBean.isEmbedded() || chatBean.getAllowCorrection()) { %>
							<% if (botBean.isCorrectionAllow()) { %>
								<tr class="menuitem">
									<td><a class="menuitem" onClick="return toggleCorrection();" href="#" title="Submit correction. This will tell the bot that its response was not correct, and it should have said what you respond with"><img id="check2" class="menu" src="images/remove.svg"> Submit correction</a></td>
								</tr>
							<% } %>
							<tr class="menuitem">
								<td><a class="menuitem" onClick="return toggleFlag();" href="#" title="Flag as offensive. An offensive response contain inappropriate language.  Do not misuse the flag option, flaging valid responses as offensive can cause your account to be disabled"><img id="flag2" class="menu" src="images/flag2.svg"> Flag as offensive</a></td>
							</tr>
						<% } %>
						<tr class="menuitem">
							<td><a class="menuitem" onClick="web.exit();" href="<%= loginBean.isEmbedded() ? "chat" : "bot" %>?disconnect<%= proxy.proxyString() %>"><img class="menu" src="images/logout.svg" title="Exit chat"> Disconnect</a></td>
						</tr>
					</table>
				</div>
				<img class="toolbar" src="images/menu.png">
			</span>
			<% if (chatBean.getAllowSpeech()) { %>
				<input id="speakinput" name="speak" type="checkbox" style="display:none" <% if (chatBean.getSpeak()) { %>checked<% } %>>
				<a onClick="return toggleSpeak();" href="#" title="Enable speech (speech requires an HTML5 audio supporting browser, such as Chrome or Firefox)"><img id="speak" class="toolbar" src="images/sound.svg"></a>
				<a onClick="return toggleListen();" href="#" title="Enable speech recognition (browser must support HTML5 speech API, such as Chrome)"><img id="listen" class="toolbar" src="images/micoff.svg"></a>
			<% } %>
			<% if (!loginBean.isEmbedded() || chatBean.getAllowCorrection()) { %>
				<% if (botBean.isCorrectionAllow()) { %>
					<input id="correction" type="checkbox" name="correction" style="display:none">
					<a onClick="return toggleCorrection();" href="#" title="Submit correction. This will tell the bot that its response was not correct, and it should have said what you respond with"><img id="check" class="toolbar" src="images/remove.svg"></a>
				<% } %>
				<input id="offensive" type="checkbox" name="offensive" style="display:none">
				<a onClick="return toggleFlag();" href="#" title="Flag as offensive. An offensive response contain inappropriate language.  Do not misuse the flag option, flaging valid responses as offensive can cause your account to be disabled"><img id="flag" class="toolbar" src="images/flag2.svg"></a>				
			<% } %>
			<% if (!loginBean.isMobile() && ((!loginBean.isEmbedded() || chatBean.getAllowEmotes()) && (learningBean.getEnableEmoting() || botBean.isAdmin()))) { %>
				 <%= loginBean.translate("Emote") %> 
				<select class="search" id="emote" name="emote" title="Emote associates an emotion with your response" style="min-width:0;width:auto;">
					<% for (EmotionalState state : EmotionalState.values()) { %>
						<option value="<%= state.name() %>"><%= state.name().toLowerCase() %></option>
					<% } %>
				</select>
				 <%= loginBean.translate("Action") %> 
				<input id="action" class="search" type="text" name="action" title="Associate an action with your response" style="width:80px;">
				<script>
				$( "#action" ).autocomplete({
					source: [<%= botBean.getAllActionsString() %>],
				    minLength: 0
					}).on('focus', function(event) {
					    var self = this;
					    $(self).autocomplete("search", "");
					});
				</script>
			<% } %>
			<% if (!loginBean.isMobile() && !loginBean.isEmbedded() && botBean.isAdmin()) { %>
				<input class="search" title="Enable logging and debugging info" type="checkbox" name="debug" <% if (chatBean.isDebug()) { %>checked<% } %>>
				<%= loginBean.translate("Debug") %> 
	 			<select class="search" name="debugLevel" style="min-width:0;width:auto;">
					<option value="OFF" <%= botBean.isLogLevelSelected("OFF") %>>OFF</option>
					<option value="SEVERE" <%= botBean.isLogLevelSelected("SEVERE") %>>SEVERE</option>
					<option value="WARNING" <%= botBean.isLogLevelSelected("WARNING") %>>WARNING</option>
					<option value="CONFIG" <%= botBean.isLogLevelSelected("CONFIG") %>>CONFIG</option>
					<option value="INFO" <%= botBean.isLogLevelSelected("INFO") %>>INFO</option>
					<option value="FINE" <%= botBean.isLogLevelSelected("FINE") %>>FINE</option>
					<option value="FINER" <%= botBean.isLogLevelSelected("FINER") %>>FINER</option>
					<% if (botBean.isSuper()) { %>
						<option value="FINEST" <%= botBean.isLogLevelSelected("FINEST") %>>FINEST</option>
						<option value="ALL" <%= botBean.isLogLevelSelected("ALL") %>>ALL</option>
					<% } %>
				</select>
			<% } %>
			<a href="<%= loginBean.isEmbedded() ? "chat" : "bot" %>?disconnect<%= proxy.proxyString() %>"><img class="toolbar" src="images/logout.svg" title="Exit chat"></a>
		</div>
		</span>
	</form>
	<% if (botBean.isAdmin() && chatBean.isDebug()) { %>
		<h3>Log</h3>
		<pre id="log"><code><%= botBean.getLog() %></code></pre>
	<% } %>
	<% if (!loginBean.isMobile() && !loginBean.isEmbedded()) { %>
		<span class="menu"><%= loginBean.translate("All conversations are recorded by the bot's brain, and may be reviewed by the bot administrator, see") %> <a href="privacy.jsp"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("for details") %>.</span>
	<% } %>
	<% if (!embed) { %>
		</td></tr>
		</table>
	<% } %>
	
	<% botBean.writeAd(out); %>
	
	<script>
	SDK.lang = '<%= voiceBean.getLanguage() %>';
	SDK.registerSpeechRecognition(document.getElementById('chat'), document.getElementById('send'));
	var listen = false;
	function toggleListen() {
		listen = !listen;
		if (listen) {
			SDK.startSpeechRecognition();
			document.getElementById('listen').src = "images/mic.svg";
			document.getElementById('listen2').src = "images/mic.svg";
		} else {
			SDK.stopSpeechRecognition();
			document.getElementById('listen').src = "images/micoff.svg";
			document.getElementById('listen2').src = "images/micoff.svg";
		}
	}
	
	var speakinput = document.getElementById('speakinput');
	if (!speakinput.checked) {
		document.getElementById('speak').src = "images/mute.svg";
		document.getElementById('speak2').src = "images/mute.svg";
	}
	function toggleSpeak() {
		speakinput.checked = !speakinput.checked;
		if (speakinput.checked) {
			document.getElementById('speak').src = "images/sound.svg";
			document.getElementById('speak2').src = "images/sound.svg";
		} else {
			document.getElementById('speak').src = "images/mute.svg";
			document.getElementById('speak2').src = "images/mute.svg";
		}
	}
	
	function toggleCorrection() {
		var correction = document.getElementById('correction');
		correction.checked = !correction.checked;
		if (correction.checked) {
			document.getElementById('check').src = "images/remove.svg";
			document.getElementById('check2').src = "images/remove.svg";
		} else {
			document.getElementById('check').src = "images/remove.svg";
			document.getElementById('check2').src = "images/remove.svg";
		}
	}
	
	function toggleFlag() {
		var offensive = document.getElementById('offensive');
		offensive.checked = !offensive.checked;
		if (offensive.checked) {
			document.getElementById('flag').src = "images/unflag.svg";
			document.getElementById('flag2').src = "images/unflag.svg";
		} else {
			document.getElementById('flag').src = "images/flag2.svg";
			document.getElementById('flag2').src = "images/flag2.svg";
		}
	}
	</script>
	<script>
		var scroller = document.getElementById('scroller');
		if (scroller != null) {
			var tableScroll = document.getElementById('table-scroll');
			scroller.style.maxHeight = (tableScroll.parentNode.offsetHeight - 24) + "px";
			var reset = true;
			var resize = function() {
			    scroller.style.maxHeight = "100px";
			    if (reset) {
			        reset = false;
			        setTimeout(function() {
			            reset = true;
			            scroller.style.maxHeight = (tableScroll.parentNode.offsetHeight - 24) + "px";
			        }, 100);
			    }
			}
			window.onresize = resize;
		    setTimeout(resize, 100);
		}
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

<%
	if (chatBean.getFirstResponse()) {
		// Check if dead connects are common and autopool.
		BotStats stats = BotStats.getStats(chatBean.getBotBean().getInstanceId(), chatBean.getBotBean().getInstanceName());
		if ((stats.connects > 10) && (stats.connects >= stats.chats)) {
			chatBean.getBotBean().poolInstance();
		}
	}
%>
<% loginBean.setHelp(help); %>
<% proxy.clear(); %>
</body>
</html>