<%@page import="org.botlibre.web.bean.VoiceBean"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.botlibre.web.admin.AdminDatabase"%>
<%@ page import="org.botlibre.util.Utils"%>
<%@ page import="org.botlibre.web.Site"%>
<%@ page import="org.botlibre.web.bean.ChatBean"%>
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
	<link rel="stylesheet" href="css/game-sdk.css" type="text/css">
	<script type="text/javascript" src="scripts/game-sdk.js"></script>
	<% botBean.writeHeadMetaHTML(out); %>
	<title><%= botBean.getInstanceName() %> Chat <%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<% if (loginBean.getCssURL() == "") { %>
		<link rel="stylesheet" href="css/chatroom.css" type="text/css">
	<% } %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body class="full" style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<table style="background-color: <%= loginBean.getBackgroundColor() %>;" class="<%= Site.PREFIX %>framechat-div">
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
<% if (embed && !Site.COMMERCIAL && loginBean.getShowLink()) { %>
	<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>;">
		<span>powered by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
	</div>
<% } %>
<% if (!(Site.REQUIRE_TERMS && !loginBean.isEmbedded() && !loginBean.isLoggedIn() && !loginBean.isAgeConfirmed())) { %>
	<jsp:include page="error.jsp"/>
<% } %>
<% if (botBean.getInstance() == null) { %>
	<% if (embed) { %></td></tr><tr height="100%"><td style='text-align:center'><% } %>
	<p style="color:#E00000;">
		<%= loginBean.translate("No bot has been selected, or session has timed out, you need to select a bot from browse") %>
	</p>
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
					<input id="ok" name="cancel" type="submit" onclick="window.location.href = '<%= loginBean.isEmbedded() ? "chat" : "bot" %>?disconnect<%= proxy.proxyString() %>'; return false;" value="<%= loginBean.translate("OK") %>"/>
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
					<input id="cancel" name="cancel" type="submit" onclick="window.location.href = '<%= loginBean.isEmbedded() ? "chat" : "bot" %>?disconnect<%= proxy.proxyString() %>'; return false;" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		</div>
<% } else { %>
	<% if (!embed) { %>
		<table class="<%= Site.PREFIX %>framechat-div">
		<tr><td>
	<% } %>
	</td></tr>
	<tr><td>
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
	<table id="<%= Site.PREFIX %>frametable-scroll" cellspacing="8" width="100%">
	<tr height="100%">
	<td align="left" valign="top">
		<% if (!loginBean.isEmbedded() || chatBean.getAvatarExpandable()) { %>
			<a href="#" onClick="web.resizeAvatar();return false;" style="text-decoration:none">
		<% } %>
		<div id="<%= Site.PREFIX %>frameavatar-div" class="<%= Site.PREFIX %>frameavatar-div">
			<div id="<%= Site.PREFIX %>frameavatar-image-div">
				<img id="<%= Site.PREFIX %>frameavatar" class="<%= Site.PREFIX %>frameavatar"/>
			</div>
			<div id="<%= Site.PREFIX %>frameavatar-video-div" class="<%= Site.PREFIX %>frameavatar-video-div"
						<%= botBean.getAvatarBackground() == null ? "" :  ("style=\"background-image:url('" + botBean.getAvatarBackground() + "');\"") %>>
				<video muted='true' id="<%= Site.PREFIX %>frameavatar-video" class="<%= Site.PREFIX %>frameavatar-video" preload="auto">
					Video format not supported by your browser (try Chrome)
				</video>
			</div>
			<div id="<%= Site.PREFIX %>frameavatar-canvas-div" class="<%= Site.PREFIX %>frameavatar-canvas-div" style="display:none;">
				<canvas id="<%= Site.PREFIX %>frameavatar-canvas" class="<%= Site.PREFIX %>frameavatar-canvas">
					Canvas not supported by your browser (try Chrome)
				</canvas>
			</div>
			<div id="<%= Site.PREFIX %>frameavatar-game-div" class="avatar-game-div" style="display:none">
		</div>
		<% if (!loginBean.isEmbedded() || chatBean.getAvatarExpandable()) { %>
			</a><br/>
		<% } else { %>
			<br/>
		<% } %>
		<span id="<%= Site.PREFIX %>frameavatar-status" class="<%= Site.PREFIX %>framemenu"></span>
	</td>
	<td valign="top" align="left" class="<%= Site.PREFIX %>framebot-scroller" width="100%">
		<div id="<%= Site.PREFIX %>framescroller" class="<%= Site.PREFIX %>framescroller">
		<table id="<%= Site.PREFIX %>frameconsole" width="100%" cellspacing="2">
		</table>
		</div>
	</td>
	</tr>
	</table>
	<!-- % } % -->
	</td></tr>
	<tr><td>
	<div id="<%= Site.PREFIX %>framebubble-div" class="<%= Site.PREFIX %>framebubble-div">
		<% if (!loginBean.isEmbedded() || chatBean.getShowChatBubble()) { %>
			<div id="<%= Site.PREFIX %>framebubble" class="<%= Site.PREFIX %>framebubble">
				<div id="<%= Site.PREFIX %>framebubble-text" class="<%= Site.PREFIX %>framebubble-text">
					<span id="<%= Site.PREFIX %>frameresponse" class="<%= Site.PREFIX %>frameresponse"><%= chatBean.getGreeting() %></span><br>
				</div>
			</div>
		<% } else { %>
			<div>
				<div id="<%= Site.PREFIX %>framebubble-text" class="<%= Site.PREFIX %>framebubble-text">
					<span id="<%= Site.PREFIX %>frameresponse" class="<%= Site.PREFIX %>frameresponse"><%= chatBean.getGreeting() %></span><br>
				</div>
			</div>
		<% } %>
	</div>
	<form class="message" onsubmit="SDK.initAudio(); web.sendMessage(); return false;">
		<table style="width:100%;">
		<tr style="width:100%;">
			<td style="width:100%;"><input id="<%= Site.PREFIX %>framechat" class="<%= Site.PREFIX %>framechat-input" placeholder="<%= chatBean.getPrompt() %>" type="text" name="input" <%= loginBean.isMobile() ? "" : "autofocus" %> x-webkit-speech /></td>
			<td align="left"><input id="sendicon" class="sendicon" type="submit" name="submit" value=""/><br/></td>
		</tr>
		</table>
		<% if (chatBean.getMenubar()) { %>
			<span class="<%= Site.PREFIX %>menu">
			<div style="inline-block;position:relative">
				<span class="dropt">
					<div style="text-align:left;bottom:32px">
						<table>
							<% if (chatBean.getShowChooseLanguage()) { %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><img class='<%= Site.PREFIX %>menu' src='images/language.svg' title='Translate to and from your selected language'>
									 <select id='chooselanguage'>
										<option value='none'>Choose Language</option>
										<option value='en'>English</option>
										<option value='zh'>Chinese</option>
										<option value='es'>Spanish</option>
										<option value='pt'>Portuguese</option>
										<option value='de'>German</option>
										<option value='fr'>French</option>
										<option value='ja'>Japanese</option>
										<option value='ar'>Arabic</option>
										<option value='none'>None</option>
										<option value='none'></option>
										<option value='af'>Afrikaans</option>
										<option value='sq'>Albanian</option>
										<option value='hy'>Armenian</option>
										<option value='az'>Azerbaijani</option>
										<option value='ba'>Bashkir</option>
										<option value='eu'>Basque</option>
										<option value='be'>Belarusian</option>
										<option value='bn'>Bengali</option>
										<option value='bs'>Bosnian</option>
										<option value='bg'>Bulgarian</option>
										<option value='ca'>Catalan</option>
										<option value='za'>Chinese</option>
										<option value='hr'>Croatian</option>
										<option value='cs'>Czech</option>
										<option value='da'>Danish</option>
										<option value='nl'>Dutch</option>
										<option value='en'>English</option>
										<option value='et'>Estonian</option>
										<option value='fi'>Finnish</option>
										<option value='fr'>French</option>
										<option value='gl'>Galician</option>
										<option value='ka'>Georgian</option>
										<option value='de'>German</option>
										<option value='gu'>Gujarati</option>
										<option value='ht'>Haitian</option>
										<option value='he'>Hebrew</option>
										<option value='hi'>Hindi</option>
										<option value='hu'>Hungarian</option>
										<option value='id'>Indonesian</option>
										<option value='ga'>Irish</option>
										<option value='it'>Italian</option>
										<option value='ja'>Japanese</option>
										<option value='kn'>Kannada</option>
										<option value='kk'>Kazakh</option>
										<option value='ky'>Kirghiz</option>
										<option value='ko'>Korean</option>
										<option value='la'>Latin</option>
										<option value='lv'>Latvian</option>
										<option value='lt'>Lithuanian</option>
										<option value='mk'>Macedonian</option>
										<option value='mg'>Malagasy</option>
										<option value='ms'>Malay</option>
										<option value='mt'>Maltese</option>
										<option value='mn'>Mongolian</option>
										<option value='no'>Norwegian</option>
										<option value='fa'>Persian</option>
										<option value='pl'>Polish</option>
										<option value='pt'>Portuguese</option>
										<option value='pa'>Punjabi</option>
										<option value='ro'>Romanian</option>
										<option value='ru'>Russian</option>
										<option value='sr'>Serbian</option>
										<option value='si'>Sinhalese</option>
										<option value='sk'>Slovak</option>
										<option value='es'>Spanish</option>
										<option value='sw'>Swahili</option>
										<option value='sv'>Swedish</option>
										<option value='tl'>Tagalog</option>
										<option value='tg'>Tajik</option>
										<option value='ta'>Tamil</option>
										<option value='tt'>Tatar</option>
										<option value='th'>Thai</option>
										<option value='tr'>Turkish</option>
										<option value='uk'>Ukrainian</option>
										<option value='ur'>Urdu</option>
										<option value='uz'>Uzbek</option>
										<option value='cy'>Welsh</option>
								  	</select>
								</td>
								</tr>
							<% } %>
							<% if (loginBean.isMobile() && (!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) && botBean.isAdmin()) { %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td>
										 <input class="search" title="<%= loginBean.translate("Enable logging and debugging info") %>" type="checkbox" id="debug" name="debug">
										 <span class="<%= Site.PREFIX %>menuitem"> <%= loginBean.translate("Debug") %></span>
							 			<select class="search" id="debugLevel" name="debugLevel" style="min-width:0;width:auto;">
											<option value="OFF">OFF</option>
											<option value="SEVERE">SEVERE</option>
											<option value="WARNING">WARNING</option>
											<option value="CONFIG">CONFIG</option>
											<option value="INFO">INFO</option>
											<option value="FINE">FINE</option>
											<option value="FINER">FINER</option>
											<% if (botBean.isSuper()) { %>
												<option value="FINEST">FINEST</option>
												<option value="ALL">ALL</option>
											<% } %>
										</select>
									</td>
								</tr>
							<% } %>
							<% if ((!loginBean.isEmbedded() || loginBean.isEmbeddedDebug())) { %>
								<tr class="<%= Site.PREFIX %>menuitem" id="learning-div" style="display:none">
									<td> <input id="learning" style="display:none" class="search" title="<%= loginBean.translate("Enable learning") %>" type="checkbox" id="learning" name="learning"> <a> <%= loginBean.translate("Learn") %></a></td>
								</tr>
							<% } %>
							<% if (chatBean.getAllowSpeech()) { %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return toggleSpeak();" href="#" title="<%= loginBean.translate("Enable speech (speech requires an HTML5 audio supporting browser, such as Chrome or Firefox)") %>">
										<img id="speak2" class="<%= Site.PREFIX %>menu" src="images/sound.svg"> <%= loginBean.translate("Speech") %></a>
									</td>
								</tr>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return toggleListen();" href="#" title="<%= loginBean.translate("Enable speech recognition (browser must support HTML5 speech API, such as Chrome)") %>">
										<img id="listen2" class="<%= Site.PREFIX %>menu" src="images/micoff.svg"> <%= loginBean.translate("Speech Recognition") %></a>
									</td>
								</tr>
							<% } %>
							<% if (chatBean.getAllowFiles()) { %>
								<% if (chatBean.getSendImage()) { %>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.sendImage();" href="#" title="<%= loginBean.translate("Upload and resize an image to send to the bot") %>">
											<img id="upload-image" class="<%= Site.PREFIX %>menu" src="images/image.svg"> <%= loginBean.translate("Upload image") %></a>
										</td>
									</tr>
									<tr class="<%= Site.PREFIX %>menuitem">
										<td><a class="<%= Site.PREFIX %>menuitem" onClick="return web.sendAttachment();" href="#" title="<%= loginBean.translate("Upload a file or full size image to send to the bot") %>">
											<img id="upload-file" class="<%= Site.PREFIX %>menu" src="images/attach.svg"> <%= loginBean.translate("Upload file") %></a>
										</td>
									</tr>
								<% } %>
							<% } %>
							<% if (loginBean.isMobile() && (((!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) || chatBean.getAllowEmotes()) || botBean.isAdmin())) { %>
								<tr id="emote-div" style="display:none" class="<%= Site.PREFIX %>menuitem">
									<td nowrap>
										<span class="<%= Site.PREFIX %>menuitem"><%= loginBean.translate("Emote") %></span>
										<select class="search" id="emote" name="emote" title="<%= loginBean.translate("Emote associates an emotion with your response") %>" style="min-width:0;width:auto;">
											<% for (EmotionalState state : EmotionalState.values()) { %>
												<option value="<%= state.name() %>"><%= state.name().toLowerCase() %></option>
											<% } %>
										</select>
									</td>
								</tr>
								<tr id="action-div" style="display:none" class="<%= Site.PREFIX %>menuitem">
									<td nowrap>
										<span class="<%= Site.PREFIX %>menuitem"><%= loginBean.translate("Action") %></span>
										<input id="action" class="search" type="text" name="action" title="<%= loginBean.translate("Associate an action with your response") %>" style="width:80px;">
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
							<% if ((!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) || chatBean.getAllowCorrection()) { %>
								<tr id="correction-div" style="display:none" class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return correction();" href="#" title="<%= loginBean.translate("Submit correction. This will tell the bot that its response was not correct, and it should have said what you respond with") %>">
										<img id="check2" class="<%= Site.PREFIX %>menu" src="images/remove.svg"> <%= loginBean.translate("Submit correction") %></a>
									</td>
								</tr>
							<% } %>
							<% if (((!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) || chatBean.getAllowCorrection()) && !botBean.getInstance().getDisableFlag()) { %>
								<tr class="<%= Site.PREFIX %>menuitem">
									<td><a class="<%= Site.PREFIX %>menuitem" onClick="return flag();" href="#" title="<%= loginBean.translate("Flag as offensive. An offensive response contain inappropriate language.  Do not misuse the flag option, flaging valid responses as offensive can cause your account to be disabled") %>">
										<img id="flag2" class="<%= Site.PREFIX %>menu" src="images/flag2.svg"> <%= loginBean.translate("Flag as offensive") %></a>
									</td>
								</tr>
							<% } %>
							<tr id="<%= Site.PREFIX %>frameshowChatLogRow" class="<%= Site.PREFIX %>menuitem">
								<td><a id="<%= Site.PREFIX %>frameshowChatLog" class="<%= Site.PREFIX %>menuitem" onClick="return web.showChatLog();" href="#">
									<img class="<%= Site.PREFIX %>menu" src="images/chat_log.svg" title="<%= loginBean.translate("Show Chat Log") %>"> <%= loginBean.translate("Chat Log") %></a>
								</td>
							</tr>
							<tr id="<%= Site.PREFIX %>frameshowAvatarRow" class="<%= Site.PREFIX %>menuitem" style="display:none;">
								<td><a id="<%= Site.PREFIX %>frameshowAvatar" class="<%= Site.PREFIX %>menuitem" onClick="return web.showAvatar();" href="#">
									<img class="<%= Site.PREFIX %>menu" src="images/avatar-icon.png" title="<%= loginBean.translate("Show Avatar") %>"> <%= loginBean.translate("Avatar") %></a>
								</td>
							</tr>
							<tr class="<%= Site.PREFIX %>menuitem">
								<td><a class="<%= Site.PREFIX %>menuitem" onClick="web.exit();" href="<%= (loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) ? "chat" : "bot" %>?disconnect<%= proxy.proxyString() %>">
									<img class="<%= Site.PREFIX %>menu" src="images/logout.svg" title="<%= loginBean.translate("Exit chat") %>"> <%= loginBean.translate("Disconnect") %></a>
								</td>
							</tr>		
						</table>
					</div>
					<img id="menu" class="toolbar" src="images/menu.png">
					<% if (chatBean.getShowChooseLanguage()) { %>
						<img id="language" class="toolbar" src="images/language.svg">
					<% } %>
				</span>
				<% if (chatBean.getAllowSpeech()) { %>
					<a onClick="return toggleSpeak();" href="#" title="<%= loginBean.translate("Enable speech (speech requires an HTML5 audio supporting browser, such as Chrome or Firefox)") %>"><img id="speak" class="toolbar" src="images/sound.svg"></a>
					<a onClick="return toggleListen();" href="#" title="<%= loginBean.translate("Enable speech recognition (browser must support HTML5 speech API, such as Chrome)") %>"><img id="listen" class="toolbar" src="images/micoff.svg"></a>
				<% } %>
				<% if (chatBean.getAllowFiles() && !loginBean.isMobile()) { %>
					<% if (chatBean.getSendImage()) { %>
						<a onClick="return web.sendImage();" href="#" title="<%= loginBean.translate("Upload and resize an image to send to the bot") %>"><img id="upload-image" class="toolbar" src="images/image.svg"></a>
						<a onClick="return web.sendAttachment();" href="#" title="<%= loginBean.translate("Upload a file to send to the bot") %>"><img id="upload-file" class="toolbar" src="images/attach.svg"></a>
					<% } %>
				<% } %>
				<% if ((!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) || chatBean.getAllowCorrection()) { %>
					<span id="correction-div2" style="display:none">
					<input id="correction" type="checkbox" name="correction" style="display:none">
					<a onClick="return correction();" href="#" title="<%= loginBean.translate("Submit correction. This will tell the bot that its response was not correct, and it should have said what you respond with") %>"><img id="check" class="toolbar" src="images/remove.svg"></a>
					</span>
				<% } %>
				<% if (((!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) || chatBean.getAllowCorrection()) && !botBean.getInstance().getDisableFlag()) { %>
					<input id="offensive" type="checkbox" name="offensive" style="display:none">
					<a onClick="return flag();" href="#" title="<%= loginBean.translate("Flag as offensive. An offensive response contain inappropriate language.  Do not misuse the flag option, flaging valid responses as offensive can cause your account to be disabled") %>"><img id="flag" class="toolbar" src="images/flag2.svg"></a>				
				<% } %>
				<% if (!loginBean.isMobile() && ((!loginBean.isEmbedded() || chatBean.getAllowEmotes()) || botBean.isAdmin())) { %>
					<span id="emote-div2" style="display:none">
					<%= loginBean.translate("Emote") %> 
					<select class="search" id="emote" name="emote" title="<%= loginBean.translate("Emote associates an emotion with your response") %>" style="min-width:0;width:auto;">
						<% for (EmotionalState state : EmotionalState.values()) { %>
							<option value="<%= state.name() %>"><%= state.name().toLowerCase() %></option>
						<% } %>
					</select>
					 <%= loginBean.translate("Action") %> 
					<input id="action" class="search" type="text" name="action" title="<%= loginBean.translate("Associate an action with your response") %>" style="width:80px;">
					<script>
					$( "#action" ).autocomplete({
						source: [<%= botBean.getAllActionsString() %>],
						minLength: 0
						}).on('focus', function(event) {
							var self = this;
							$(self).autocomplete("search", "");
						});
					</script>
					</span>
				<% } %>
				<% if (!loginBean.isMobile() && (!loginBean.isEmbedded() || loginBean.isEmbeddedDebug()) && botBean.isAdmin()) { %>
					<input class="search" title="<%= loginBean.translate("Enable logging and debugging info") %>" type="checkbox" id="debug" name="debug">
					<%= loginBean.translate("Debug") %> 
					<select class="search" id="debugLevel" name="debugLevel" style="min-width:0;width:auto;">
						<option value="OFF">OFF</option>
						<option value="SEVERE">SEVERE</option>
						<option value="WARNING">WARNING</option>
						<option value="CONFIG">CONFIG</option>
						<option value="INFO">INFO</option>
						<option value="FINE">FINE</option>
						<option value="FINER">FINER</option>
						<% if (botBean.isSuper()) { %>
							<option value="FINEST">FINEST</option>
							<option value="ALL">ALL</option>
						<% } %>
					</select>
				<% } %>
				<a id="<%= Site.PREFIX %>frameshowChatLogButton" onClick="return web.showChatLog();" href="#"><img class="toolbar" src="images/chat_log.svg" title="<%= loginBean.translate("Show Chat Log") %>"></a>
				<a id="<%= Site.PREFIX %>frameshowAvatarButton" onClick="return web.showAvatar();" href="#" style="display:none;"><img class="toolbar" src="images/avatar-icon.png" title="<%= loginBean.translate("Show Avatar") %>"></a>
				<a onClick="web.exit();" href="<%= loginBean.isEmbedded() ? "chat" : "bot" %>?disconnect<%= proxy.proxyString() %>"><img class="toolbar" src="images/logout.svg" title="<%= loginBean.translate("Exit chat") %>"></a>
			</div>
			</span>
		<% } %>
	</form>
	<div><pre id="log" style="display:none"></pre></div>
	<% if (!loginBean.isMobile() && !loginBean.isEmbedded()) { %>
		<span class="<%= Site.PREFIX %>menu"><%= loginBean.translate("All conversations are recorded by the bot's brain, and may be reviewed by the bot administrator, see") %> <a href="privacy.jsp"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("for details") %>.</span>
		<div id="learning-warning" style="display:none" ><span class="<%= Site.PREFIX %>menu"><%= loginBean.translate("WARNING: This bot has learning enabled, it will learn and use all of your responses.") %></span></div>
	<% } %>
	<% if (!embed) { %>
		</td></tr>
		</table>
	<% } %>
	
	<div id='<%= Site.PREFIX %>yandex' style="display:none"><br/><span>Powered by <a target='_blank' href='http://translate.yandex.com/'>Yandex.Translate</a></span></div>

	<% botBean.writeAd(out); %>
	
	<div id='dialog-flag' title='<%= loginBean.translate("Flag") %>' class='dialog'>
		<form action='#' method='post' class='message'>
			<input id='flag-confirm' type='checkbox' name='flagged' title='Do not misuse the flag option'>
				<%= loginBean.translate("Flag message as offensive") %>
			</input><br/>
			<input id='flag-reason' name='flag-reason' type='text' placeholder='<%= loginBean.translate("reason") %>'/>
		</form>
	</div>

	<script>
		var flagMessage = function() {
			if (document.getElementById('flag-confirm').checked && document.getElementById('flag-reason').value != '') {
				$('#dialog-flag').dialog('close');
				var offensive = document.getElementById('offensive');
				offensive.checked = true;
				web.sendMessage(document.getElementById('flag-reason').value);
			} else {
				SDK.showError("<%= loginBean.translate("You must click 'Flag' and enter reason") %>");
			}
			return false;
		}
		$('#flag-reason').keypress(function(event) {
			if (event.keyCode == $.ui.keyCode.ENTER) {
				flagMessage();
				return false;
			}
		});
		$(function() { $('#dialog-flag').dialog({
				autoOpen: false,
				modal: true,
				buttons: [
					{
						text: "<%= loginBean.translate("Flag") %>",
						click: function() {
							flagMessage();
						},
						class: "okbutton"
					},
					{
						text: "<%= loginBean.translate("Cancel") %>",
						click: function() {
							$( this ).dialog( "close" );
						}
					}
				]}); });
	</script>
	
	<div id='dialog-correct' title='<%= loginBean.translate("Correction") %>' class='dialog'>
		<form action='#' method='post' class='message'>
			<%= loginBean.translate("Enter what the bot should have said:") %><br/>
			<input id='correct-message' name='correction-message' type='text' placeholder='<%= loginBean.translate("correction") %>'/>
		</form>
	</div>

	<script>
		var correctMessage = function() {
			if (document.getElementById('correct-message').value != '') {
				$('#dialog-correct').dialog('close');
				var correction = document.getElementById('correction');
				correction.checked = true;
				web.sendMessage(document.getElementById('correct-message').value);
			} else {
				SDK.showError("<%= loginBean.translate("You must enter correction") %>");
			}
			return false;
		}
		$('#correct-message').keypress(function(event) {
			if (event.keyCode == $.ui.keyCode.ENTER) {
				correctMessage();
				return false;
			}
		});
		$(function() { $('#dialog-correct').dialog({
				autoOpen: false,
				modal: true,
				buttons: [
							{
								text: "<%= loginBean.translate("Correct") %>",
								click: function() {
									correctMessage();
								},
								class: "okbutton"
							},
							{
								text: "<%= loginBean.translate("Cancel") %>",
								click: function() {
									$( this ).dialog( "close" );
								}
							}
						]}); });
	</script>

	<% if (voiceBean.getResponsiveVoice()) { %>
		<script src='https://code.responsivevoice.org/responsivevoice.js?key=<%= Site.RESPONSIVEVOICE_KEY %>'></script>
	<% } %>
	<script type="text/javascript">
		<% if (loginBean.isEmbedded()) { %>
			<% if (loginBean.getApplicationId() != null) { %>
				SDK.applicationId = "<%= loginBean.getApplicationId() %>";
			<% } %>
		<% } else { %>
			SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
		<% } %>
		<% if (botBean.getInstance().getAllowJavaScript()) { %>;
			SDK.secure = false;
		<% } %>
		SDK.lang = '<%= chatBean.getSDKLanguage() %>';
		var sdk = new SDKConnection();
		var user = new UserConfig();
		<% if (loginBean.isLoggedIn()) { %>
			user.user = "<%= loginBean.getUser().getUserId() %>";
			user.token = "<%= loginBean.getUser().getToken() %>";
			sdk.user = user;
		<% } %>
		var web = new WebChatbotListener();
		web.connection = sdk;
		web.instance = "<%= botBean.getInstanceId() %>";
		web.instanceName = "<%= Utils.escapeQuotes(botBean.getInstanceName()) %>";
		web.userName = "You";
		web.contactInfo = "<%= Utils.escapeQuotes(chatBean.getInfo()) %>";
		var isEmbedded = <%= loginBean.isEmbedded() %>;
		web.bubble = <%= chatBean.getShowChatBubble() %>;
		web.prefix = "<%= Site.PREFIX %>frame";
		<% if (loginBean.getUser() != null) { %>
			web.userThumb['name'] = "<%= loginBean.getUser().getUserId() %>";
		<% } %>
		web.userThumb['avatar'] = '<%= loginBean.getAvatarImage(loginBean.getUser()) %>';
		web.botThumb['name'] = "<%= botBean.getInstanceName() %>";
		web.botThumb['avatar'] = '<%= botBean.getAvatarImage(botBean.getInstance()) %>';
		web.avatar = <%= chatBean.getShowAvatar() %>;
		web.chatLog = <%= chatBean.getShowChatLog() %>;
		web.avatarExpandable = <%= chatBean.getAvatarExpandable() %>;
		web.greeting = "<%= chatBean.getGreeting() %>";
		web.speak = <%= chatBean.getSpeak() %>;
		web.nativeVoice = <%= voiceBean.getNativeVoice() %>;
		web.nativeVoiceName = "<%= voiceBean.getNativeVoiceName() %>";
		<% if (voiceBean.getResponsiveVoice()) { %>;
			SDK.initResponsiveVoice();
		<% } %>
		<% if (voiceBean.getBingSpeech()) { %>;
			SDK.initBingSpeech("<%= botBean.getInstanceId() %>");
		<% } %>
		<% if (voiceBean.getQQSpeech()) { %>;
			SDK.initQQSpeech("<%= botBean.getInstanceId() %>");
		<% } %>
		web.lang = "<%= chatBean.getChatLanguage() %>";
		<% if (chatBean.isTranslating()) { %>
			web.translate = true;
			web.nativeVoice = true;
		<% } %>
		
		
		<% if (loginBean.isMobile()) { %>
			web.focus = false;
		<% } %>
		<% if (botBean.getInstance().isExternal() && !botBean.getInstance().getApiServerSide()) { %>
			web.external = true;
			web.apiURL = '<%= botBean.getInstance().getApiURL() %>';
			web.apiPost = '<%= botBean.getInstance().getApiPost() %>';
			web.apiResponse = '<%= botBean.getInstance().getApiResponse() %>';
		<% } %>
		var first = true;
		web.onresponse = function(message) {
			var element = document.getElementById('correction');
			if (element != null) {
				element.checked = false;
			}
			element = document.getElementById('offensive');
			if (element != null) {
				element.checked = false;
			}
			if (first) {
				first = false;
				var settings = new ChatSettings();
				settings.conversation = web.conversation;
				sdk.chatSettings(settings, function(settings) {
					// style.display = null does not work in IE...
					if (settings.allowEmotes) {
						var element = document.getElementById('emote-div');
						if (element != null) {
							element.style.display = "table-row";
						}
						element = document.getElementById('emote-div2');
						if (element != null) {
							element.style.display = "inline";
						}
						element = document.getElementById('action-div');
						if (element != null) {
							element.style.display = "table-row";
						}
					}
					if (settings.allowCorrection) {
						var element = document.getElementById('correction-div');
						if (element != null) {
							element.style.display = "table-row";
						}
						element = document.getElementById('correction-div2');
						if (element != null) {
							element.style.display = "inline";
						}
					}
					if (settings.allowLearning) {
						var element = document.getElementById('learning-div');
						if (element != null) {
							element.style.display = "table-row";
						}
						element = document.getElementById('learning');
						if (element != null) {
							element.style.display = "inline-block";
							element.checked = settings.learning;
						}
					}
					if (settings.learning) {
						element = document.getElementById('learning-warning');
						if (element != null) {
							element.style.display = "block";
						}
					}
				});
			}
		}
		if (web.avatar && web.chatLog) {
			document.getElementById(web.prefix + 'avatar-div').style.display = "inline-block";
			if (window.innerWidth < 480) {
				document.getElementById(web.prefix + 'scroller').style.display = "none";
				document.getElementById(web.prefix + 'bubble-div').style.display = "block";
			} else {
				document.getElementById(web.prefix + 'scroller').style.display = "inline-block";
				document.getElementById(web.prefix + 'bubble-div').style.display = "none";
			}
		} else if (web.avatar && !web.chatLog) {
			var chatLogRow = document.getElementById(web.prefix + "showChatLogRow");
			var chatLogButtonDiv = document.getElementById(web.prefix + "showChatLogButton");
			if (chatLogRow != null) {
				chatLogRow.style.display = "none";
			}
			if (chatLogButtonDiv != null) {
				chatLogButtonDiv.style.display = "none";
			}
			document.getElementById(web.prefix + 'avatar-div').style.display = "inline-block";
			document.getElementById(web.prefix + 'scroller').style.display = "none";
			document.getElementById(web.prefix + 'response').style.display = "inline";
		} else if (!web.avatar && web.chatLog) {
			var chatLogRow = document.getElementById(web.prefix + "showChatLogRow");
			var chatLogButtonDiv = document.getElementById(web.prefix + "showChatLogButton");
			if (chatLogRow != null) {
				chatLogRow.style.display = "none";
			}
			if (chatLogButtonDiv != null) {
				chatLogButtonDiv.style.display = "none";
			}
			var avatarRow = document.getElementById(web.prefix + "showAvatarRow");
			var avatarButtonDiv = document.getElementById(web.prefix + "showAvatarButton");
			if (avatarRow != null) {
				avatarRow.style.display = "none";
			}
			if (avatarButtonDiv != null) {
				avatarButtonDiv.style.display = "none";
			}
			document.getElementById(web.prefix + 'avatar-div').style.display = "none";
			document.getElementById(web.prefix + 'scroller').style.display = "inline-block";
			document.getElementById(web.prefix + 'bubble-div').style.display = "none";
		} else {
			var chatLogRow = document.getElementById(web.prefix + "showChatLogRow");
			var chatLogButtonDiv = document.getElementById(web.prefix + "showChatLogButton");
			if (chatLogRow != null) {
				chatLogRow.style.display = "none";
			}
			if (chatLogButtonDiv != null) {
				chatLogButtonDiv.style.display = "none";
			}
			document.getElementById(web.prefix + 'avatar-div').style.display = "none";
			document.getElementById(web.prefix + 'scroller').style.display = "none";
			document.getElementById(web.prefix + 'response').style.display = "inline";
		}
		
		web.showChatLog = function() {
			<% if (chatBean.getMenubar()) { %>
				document.getElementById(web.prefix + "showChatLogRow").style.display = "none";
				document.getElementById(web.prefix + "showChatLogButton").style.display = "none";
				document.getElementById(web.prefix + "showAvatarRow").style.display = "block";
				document.getElementById(web.prefix + "showAvatarButton").style.display = "inline-block";
			<% } %>
			document.getElementById(web.prefix + "avatar-div").style.display = "none";
			document.getElementById(web.prefix + 'bubble-div').style.display = "none";
			document.getElementById(web.prefix + 'scroller').style.display = "inline-block";
			document.getElementById(web.prefix + "avatar-status").style.display = "none";
		}
		web.showAvatar  = function() {
			<% if (chatBean.getMenubar()) { %>
				document.getElementById(web.prefix + "showChatLogRow").style.display = "block";
				document.getElementById(web.prefix + "showChatLogButton").style.display = "inline-block";
				document.getElementById(web.prefix + "showAvatarRow").style.display = "none";
				document.getElementById(web.prefix + "showAvatarButton").style.display = "none";
			<% } %>
			document.getElementById(web.prefix + "avatar-div").style.display = "initial";
			if (web.hd) {
				document.getElementById(web.prefix + 'scroller').style.display = "none";
				document.getElementById(web.prefix + 'bubble-div').style.display = "block";
			}
			if (window.innerWidth < 480) {
				document.getElementById(web.prefix + 'scroller').style.display = "none";
				document.getElementById(web.prefix + 'bubble-div').style.display = "block";
			}
		} 

		if (user.user != null) {
			user = sdk.connect(user, function(user) {
				web.greet();
			});
		} else {
			web.greet();
		}
		window.onunload = function() {
			web.exit();
		}
		var listen = false;
		
		SDK.registerSpeechRecognition(document.getElementById(web.prefix + 'chat'), document.getElementById('sendicon'));
		
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
			return false;
		}
	
		<% if (!chatBean.getSpeak()) { %>;
			document.getElementById('speak').src = "images/mute.svg";
			document.getElementById('speak2').src = "images/mute.svg";
		<% } %>
		function toggleSpeak() {
			web.toggleSpeak();
			if (web.speak) {
				SDK.initAudio();
				document.getElementById('speak').src = "images/sound.svg";
				document.getElementById('speak2').src = "images/sound.svg";
			} else {
				document.getElementById('speak').src = "images/mute.svg";
				document.getElementById('speak2').src = "images/mute.svg";
			}
			return false;
		}
		
		function correction() {
			$('#dialog-correct').dialog('open');
			return false;
		}
		
		function flag() {
			$('#dialog-flag').dialog('open');
			return false;
		}

		var langOrig = null;
		var nativeVoiceOrig = null;
		if (document.getElementById("chooselanguage") != null) {
			document.getElementById("chooselanguage").addEventListener("change", function() {
				if (nativeVoiceOrig == null && langOrig == null) {
					langOrig = web.lang;
					nativeVoiceOrig = web.nativeVoice;
				}
				var element = document.getElementById('chooselanguage');
				web.lang = element.value;
				var prefix = "<%= Site.PREFIX %>";
				if (web.lang != "none") {
					document.getElementById(prefix + 'yandex').style.display = "inline";
					web.nativeVoice = true;
					web.translate = true;
				} else {
					document.getElementById(prefix + 'yandex').style.display = "none";
					web.translate = false;
					web.lang = langOrig;
					web.nativeVoice = nativeVoiceOrig;
				}
			});
		}
	</script>
	<script>
		var prefix = "<%= Site.PREFIX %>frame";
		var scroller = document.getElementById(prefix + 'scroller');
		var tableScroll = document.getElementById(prefix + 'table-scroll');
		var avatar = document.getElementById(prefix +'avatar');
		var avatarVideo = document.getElementById(prefix + 'avatar-video');
		var avatarVideoDiv = document.getElementById(prefix + 'avatar-video-div');
		var avatarCanvas = document.getElementById(prefix + 'avatar-canvas');
		var avatarCanvasDiv = document.getElementById(prefix + 'avatar-canvas-div');
		var avatarStatus = document.getElementById(prefix + 'avatar-status');
		scroller.style.maxHeight = (tableScroll.parentNode.offsetHeight - 24) + "px";
		var bubbleTextDiv = document.getElementById(prefix + "bubble-text");
		
		var reset = true;
		var resize = function() {
			scroller.style.maxHeight = "200px";
			if (window.innerWidth >= 480 && web.chatLog) {
				var scrollerDiv = document.getElementById(web.prefix + "scroller");
				var bubbleDiv = document.getElementById(web.prefix + "bubble-div");
				var chatLogButton = document.getElementById(web.prefix + "showChatLogButton");
				if (web.hd && chatLogButton.style.display != "none") {
					scrollerDiv.style.display = "none";
					if (web.bubble) {
						bubbleDiv.style.display = "block";
					}
				} else {
					scrollerDiv.style.display = "inline-block";
					bubbleDiv.style.display = "none";	
				}
				if (chatLogButton != null && chatLogButton.style.display === "none") {
					document.getElementById(web.prefix + 'avatar-div').style.display = "none";
				}
			}
			if (window.innerWidth < 480 && web.avatar && web.chatLog) {
				if (bubbleTextDiv != null) {
					bubbleTextDiv.style.maxHeight = "100px";
				}
				document.getElementById(web.prefix + 'avatar-div').style.display = "inline-block";
				var bubbleDiv = document.getElementById(web.prefix + "bubble-div");
				var bubble = document.getElementById(web.prefix + "bubble");
				var chatLogButton = document.getElementById(web.prefix + "showChatLogButton");
				if (chatLogButton.style.display != "none" && !web.hd) { // chat log button visible (viewing avatar)
					if (bubble != null) {
						bubble.style.display = "block";
					}
					document.getElementById(web.prefix + "scroller").style.display = "none";
					bubbleDiv.style.display = "block";
				}
				else if (chatLogButton != null && chatLogButton.style.display === "none" && !web.hd) { // avatar button visible (viewing chat log)
					document.getElementById(web.prefix + 'avatar-div').style.display = "none";
					bubbleDiv.style.display = "none";
				} else if (chatLogButton != null && chatLogButton.style.display != "none" && web.hd) { // chat log button visible (viewing avatar)
					document.getElementById(web.prefix + 'avatar-div').style.display = "inline-block";
					document.getElementById(web.prefix + "scroller").style.display = "none";
					if (web.bubble) {
						bubbleDiv.style.display = "block";
					}
				} else if (chatLogButton != null && chatLogButton.style.display === "none" && web.hd) { // avatar button visible (viewing chat log)
					bubbleDiv.style.display = "none";
					document.getElementById(web.prefix + 'avatar-div').style.display = "none";
					document.getElementById(web.prefix + "scroller").style.display = "inline-block";

				}
			} else if (window.innerWidth < 480 && !web.avatar && web.chatLog) {
				document.getElementById(web.prefix + "scroller").style.display = "inline-block";
				document.getElementById(web.prefix + "bubble-div").style.display = "none";
			} else if (window.innerWidth < 480 && web.avatar && !web.chatLog) {
				document.getElementById(web.prefix + "bubble-div").style.display = "block";
			}
			if (bubbleTextDiv != null) {
				var bubbleWidth = (tableScroll.parentNode.offsetWidth - 30) + "px";
				bubbleTextDiv.style.overflowY = "auto";
				bubbleTextDiv.style.overflowX = "hidden";
				bubbleTextDiv.style.wordWrap = "break-word";
				bubbleTextDiv.style.maxWidth = bubbleWidth;
			}
			<% if (!loginBean.isMobile()) { %>
				if (avatar.className == "avatar-big") {
					avatar.style.height = "100px";
					avatar.style.maxWidth = (tableScroll.parentNode.offsetWidth - 24) + "px";
					if (avatarCanvasDiv.style.display != "none") {
						avatarVideo.style.height = "100px";
						avatarVideo.style.maxWidth = tableScroll.parentNode.offsetWidth + "px";
						avatarVideoDiv.style.height = "100px";
						avatarVideoDiv.style.maxWidth = tableScroll.parentNode.offsetWidth + "px";
						avatarVideoDiv.style.minHeight = "initial";
						avatarVideoDiv.style.minWidth = "initial";
						avatarVideoDiv.style.backgroundSize = "auto 100px";
						avatarCanvas.style.height = "100px";
						avatarCanvas.style.maxWidth = (tableScroll.parentNode.offsetWidth - 24) + "px";
						avatarCanvasDiv.style.height = "100px";
						avatarCanvasDiv.style.maxWidth = (tableScroll.parentNode.offsetWidth - 24) + "px";
						avatarCanvasDiv.style.minHeight = "initial";
						avatarCanvasDiv.style.minWidth = "initial";
						avatarCanvasDiv.style.backgroundSize = "initial";
					}
				} else {
					avatar.style.height = null;
					avatar.style.maxWidth = null;
					avatarVideo.style.height = null;
					avatarVideo.style.maxWidth = null;
					avatarVideoDiv.style.height = null;
					avatarVideoDiv.style.maxWidth = null;
					avatarVideoDiv.style.minHeight = null;
					avatarVideoDiv.style.minWidth = null;
					avatarVideoDiv.style.backgroundSize = null;
					avatarCanvas.style.height = null;
					avatarCanvas.style.maxWidth = null;
					avatarCanvasDiv.style.height = null;
					avatarCanvasDiv.style.maxWidth = null;
					avatarCanvasDiv.style.minHeight = null;
					avatarCanvasDiv.style.minWidth = null;
					avatarCanvasDiv.style.backgroundSize = null;
				}
			<% } %>
			
			if (reset) {
				reset = false;
				setTimeout(function() {
					reset = true;
					if (scroller != null) {
						scroller.style.maxHeight = (tableScroll.parentNode.offsetHeight - 48) + "px";
					}
					<% if (!loginBean.isMobile()) { %>
						if (avatar.className == "avatar-big") {
							var offest = (tableScroll.parentNode.offsetHeight - avatarStatus.offsetHeight - 52) + "px";
							avatar.style.height = offest;
							if (avatarCanvasDiv.style.display != "none") {
								avatarVideo.style.height = offest;
								avatarVideoDiv.style.height = offest;
								avatarVideoDiv.style.backgroundSize = "auto " + "offest";
								avatarCanvas.style.height = offest;
								avatarCanvasDiv.style.height = offest;
							}
						}
					<% } %>
				}, 100);
			}
		}
		<% if (embed) { %>
			window.onresize = resize;
		<% } %>
		setTimeout(resize, 1000);
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