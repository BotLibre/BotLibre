<%@page import="org.botlibre.web.admin.AccessMode"%>
<%@page import="org.botlibre.web.admin.AvatarMedia"%>
<%@page import="org.botlibre.web.bean.AvatarBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.botlibre.emotion.EmotionalState"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AvatarBean bean = loginBean.getBean(AvatarBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Avatar Embed<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Test an avatar and generate the JavaScript code to embed it on your own website"/>	
	<meta name="keywords" content="avatar, embed, javascript"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<jsp:include page="avatar-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<% bean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>	
	<jsp:include page="error.jsp"/>
	<h1>
		<span class="dropt-banner">
			<img id="help-icon" src="images/help.svg"/>
			<div>
				<p class="help"><%= loginBean.translate("The avatar embed page allows you test the avatar and generate embedding code to add the avatar to your own website.") %></p>
			</div>
		</span> <%= loginBean.translate("Embed Avatar") %>
	</h1>
	<% if (!bean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("Please") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to get the code to embed an avatar") %>.
		</p>
	<% } %>
	<% if (bean.getInstance() == null) { %>
		<p><%= loginBean.translate("No avatar selected.") %></p>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this avatar.") %>
	<% } else { %>
			<!--  Browsers do not let you execute code that was submitted, so need this in separate form -->
			<% if (!bean.getEmbedCode().isEmpty()) { %>
				<%= bean.getEmbedCode() %>
				<% if (bean.isLoggedIn() && bean.hasValidApplicationId() && (!bean.getEmbedResponsiveVoice() || Site.COMMERCIAL)) { %>
					<h2>Embedding Code</h2>
					<form action="avatar" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= proxy.proxyInput() %>
						<%= bean.instanceInput() %>
						<textarea name="embedcode" ><%= bean.getEmbedDisplayCode() %></textarea><br/>
						<input id="ok" onclick="document.getElementById('get-code-form').submit(); return false;" type="submit" name="get-embed-code" value="Generate Code"/>
						<input type="submit" name="run-embed-code" value="Execute Code"/>
						<br/>
					</form>
				<% } else { %>
					<form action="avatar" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<input id="ok" onclick="document.getElementById('get-code-form').submit(); return false;" type="submit" name="get-embed-code" value="Test"/>
						<br/>
					</form>
				<% } %>
			<% } %>
		<h2><%= loginBean.translate("Embedding Options") %></h2>
		<form id="get-code-form" action="avatar" method="post" class="message">
			<input id="embed" name="embed" type="hidden" value="embed"/>
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<%= loginBean.translate("Speech") %><br/>
			<textarea type="text" name="speech" title="The text for the avatar to speak" ><%= bean.getEmbedSpeech() %></textarea><br/>
			<table>
				<% if (bean.getInstance().isPrivate() || bean.getInstance().getAccessMode() != AccessMode.Everyone) { %>
					<tr>
						<td><%= loginBean.translate("Guest User") %></td>
						<td><input type="text" name="user" value="<%= bean.getUserName() %>" title="Guest user to connect as, you need to add this user to your avatar's users (do not use your own user id)" /></td>
					</tr>
					<tr>
						<td><%= loginBean.translate("Password") %></td>
						<td><input type="password" name="password" value="<%= bean.getPassword() %>" title="Password for guest user (not secure)" /></td>
					</tr>
					<tr>
						<td><%= loginBean.translate("Token") %></td>
						<td><input type="text" name="token" value="<%= bean.getToken() %>" title="Token for guest user (this will expire, use a guest user/password instead)" /></td>
					</tr>
				<% } %>
				
				<!-- Begin Voice -->
				
				<tr>
					<td><%= loginBean.translate("Speech Provider") %></td>
					<td>
				   		<select id="provider" name="provider">
							<option value="botlibre" <% if (!bean.getEmbedNativeVoice()) { %>selected<% } %>><%= loginBean.translate("Bot Libre") %></option>
							<option value="html5" <% if (bean.getEmbedNativeVoice()) { %>selected<% } %>><%= loginBean.translate("HTML5") %></option>
							<option value="responsive" <% if (bean.getEmbedResponsiveVoice()) { %>selected<% } %> ><%= loginBean.translate("ResponsiveVoice") %></option>
							<option value="bing" <% if (bean.getEmbedBingSpeech()) { %>selected<% } %>><%= loginBean.translate("Microsoft Speech") %></option>
							<option value="qq" <% if (bean.getEmbedQQSpeech()) { %>selected<% } %>><%= loginBean.translate("QQ") %></option>
						</select>
					</td>
					</tr>
					
					<tr id="bingDescTr">
		   				<td></td>
		   				<td>
		   					<div id="bing-speech-div" style="width:300px;vertical-align:top;font-family: Arial;font-size:9pt;line-height: normal">
							<%= loginBean.translate("Bing Speech is a third party speech API. You must create an account at ") %> <a target="_blank" href="https://azure.microsoft.com/try/cognitive-services/">https://azure.microsoft.com</a>.
							</div>
		   				</td>
					</tr>
					
					<tr id="qqDescTr">
		   				<td></td>
		   				<td>
		   					<div id="qq-speech-div" style="display:none;width:300px;vertical-align:top;font-family: Arial;font-size:9pt;line-height: normal">
							<%= loginBean.translate("QQ is a third party speech API. You must create an account at ") %> <a target="_blank" href="https://ai.qq.com">ai.qq.com</a>.
							</div>
						</td>
					</tr>
			
					<tr id="botlibreVoiceTr">			   
					<td><%= loginBean.translate("Voice") %></td>
					<td><select id="voice" name="voice">
						<option value="cmu-slt" <%= bean.getVoiceCheckedString("cmu-slt") %>><%= loginBean.translate("English : US : Female : SLT") %></option>
						<option value="cmu-slt-hsmm" <%= bean.getVoiceCheckedString("cmu-slt-hsmm") %>><%= loginBean.translate("English : US : Female : SLT (hsmm)") %></option>
						<option value="cmu-bdl" <%= bean.getVoiceCheckedString("cmu-bdl") %>><%= loginBean.translate("English : US : Male : BDL") %></option>
						<option value="cmu-bdl-hsmm" <%= bean.getVoiceCheckedString("cmu-bdl-hsmm") %>><%= loginBean.translate("English : US : Male : BDL (hsmm)") %></option>
						<option value="cmu-rms" <%= bean.getVoiceCheckedString("cmu-rms") %>><%= loginBean.translate("English : US : Male : RMS") %></option>
						<option value="cmu-rms-hsmm" <%= bean.getVoiceCheckedString("cmu-rms-hsmm") %>><%= loginBean.translate("English : US : Male : RMS (hsmm)") %></option>
						<option value="dfki-prudence" <%= bean.getVoiceCheckedString("dfki-prudence") %>><%= loginBean.translate("English : GB : Female : Prudence") %></option>
						<option value="dfki-prudence-hsmm" <%= bean.getVoiceCheckedString("dfki-prudence-hsmm") %>><%= loginBean.translate("English : GB : Female : Prudence (hsmm)") %></option>
						<option value="dfki-spike" <%= bean.getVoiceCheckedString("dfki-spike") %>><%= loginBean.translate("English : GB : Male : Spike") %></option>
						<option value="dfki-spike-hsmm" <%= bean.getVoiceCheckedString("dfki-spike-hsmm") %>><%= loginBean.translate("English : GB : Male : Spike (hsmm)") %></option>
						<option value="dfki-obadiah" <%= bean.getVoiceCheckedString("dfki-obadiah") %>><%= loginBean.translate("English : GB : Male : Obadiah") %></option>
						<option value="dfki-obadiah-hsmm" <%= bean.getVoiceCheckedString("dfki-obadiah-hsmm") %>><%= loginBean.translate("English : GB : Male : Obadiah (hsmm)") %></option>
						<option value="dfki-poppy" <%= bean.getVoiceCheckedString("dfki-poppy") %>><%= loginBean.translate("English : GB : Female : Poppy") %></option>
						<option value="dfki-poppy-hsmm" <%= bean.getVoiceCheckedString("dfki-poppy-hsmm") %>><%= loginBean.translate("English : GB : Female : Poppy (hsmm)") %></option>
						<option value="bits1" <%= bean.getVoiceCheckedString("bits1") %>><%= loginBean.translate("German : DE : Female : Bits1") %></option>
						<option value="bits1-hsmm" <%= bean.getVoiceCheckedString("bits1-hsmm") %>><%= loginBean.translate("German : DE : Female : Bits1 (hsmm)") %></option>
						<option value="bits3" <%= bean.getVoiceCheckedString("bits3") %>><%= loginBean.translate("German : DE : Male : Bits3") %></option>
						<option value="bits3-hsmm" <%= bean.getVoiceCheckedString("bits3-hsmm") %>><%= loginBean.translate("German : DE : Male : Bits3 (hsmm)") %></option>
						<option value="dfki-pavoque-neutral-hsmm" <%= bean.getVoiceCheckedString("dfki-pavoque-neutral-hsmm") %>><%= loginBean.translate("German : DE : Male : Pavoque (hsmm)") %></option>
						<option value="camille" <%= bean.getVoiceCheckedString("camille") %>><%= loginBean.translate("French : FR : Female : Camille") %></option>
						<option value="camille-hsmm-hsmm" <%= bean.getVoiceCheckedString("camille-hsmm-hsmm") %>><%= loginBean.translate("French : FR : Female : Camille (hsmm)") %></option>
						<option value="jessica_voice" <%= bean.getVoiceCheckedString("jessica_voice") %>><%= loginBean.translate("French : FR : Female : Jessica") %></option>
						<option value="jessica_voice-hsmm" <%= bean.getVoiceCheckedString("jessica_voice-hsmm") %>><%= loginBean.translate("French : FR : Female : Jessica (hsmm)") %></option>
						<option value="pierre-voice" <%= bean.getVoiceCheckedString("pierre-voice") %>><%= loginBean.translate("French : FR : Male : Pierre") %></option>
						<option value="pierre-voice-hsmm" <%= bean.getVoiceCheckedString("pierre-voice-hsmm") %>><%= loginBean.translate("French : FR : Male : Pierre (hsmm)") %></option>
						<option value="enst-dennys-hsmm" <%= bean.getVoiceCheckedString("enst-dennys-hsmm") %>><%= loginBean.translate("French : FR : Male : Dennys (hsmm)") %></option>
						<option value="marylux" <%= bean.getVoiceCheckedString("marylux") %>><%= loginBean.translate("Luxembourgish : LU : Female : Lux") %></option>
						<option value="istc-lucia-hsmm" <%= bean.getVoiceCheckedString("istc-lucia-hsmm") %>><%= loginBean.translate("Italian : IT : Male : Lucia (hsmm)") %></option>
						<option value="voxforge-ru-nsh" <%= bean.getVoiceCheckedString("voxforge-ru-nsh") %>><%= loginBean.translate("Russian : RU : Male : NSH (hsmm)") %></option>
						<option value="dfki-ot" <%= bean.getVoiceCheckedString("dfki-ot") %>><%= loginBean.translate("Turkish : TR : Male : OT") %></option>
						<option value="dfki-ot-hsmm" <%= bean.getVoiceCheckedString("dfki-ot-hsmm") %>><%= loginBean.translate("Turkish : TR : Male : OT (hsmm)") %></option>
						<!-- option value="cmu-nk" <%= bean.getVoiceCheckedString("cmu-nk") %>><%= loginBean.translate("Telugu : TE : Female : NK") %></option-->
						<option value="cmu-nk-hsmm" <%= bean.getVoiceCheckedString("cmu-nk-hsmm") %>><%= loginBean.translate("Telugu : TE : Female : NK (hsmm)") %></option>
					</select></td>
					</tr>
					<tr id="botlibreVoiceModTr">
						<td><%= loginBean.translate("Voice Modifier") %></td>
						<td>
							<select id="voice-mod" name="voice-mod">
								<option value="default" <%= bean.getVoiceModCheckedString("default") %>><%= loginBean.translate("Default") %></option>
								<option value="child" <%= bean.getVoiceModCheckedString("child") %>><%= loginBean.translate("Child") %></option>
								<option value="whisper" <%= bean.getVoiceModCheckedString("whisper") %>><%= loginBean.translate("Whisper") %></option>
								<option value="echo" <%= bean.getVoiceModCheckedString("echo") %>><%= loginBean.translate("Echo") %></option>
								<option value="robot" <%= bean.getVoiceModCheckedString("robot") %>><%= loginBean.translate("Robot") %></option>
							</select>
						</td>
					</tr> 
					
					<tr id="nativeVoiceTr">
						<td>
							<%= loginBean.translate("Voice") %>
						</td>
						<td>
							<input id="native-voice-name" type="text" name="native-voice-name" value="<%= bean.getEmbedNativeVoiceName() %>"
									title="<%= loginBean.translate("The name of the native voice. A native mobile device or browser voice can be used on Android, iOS, Chrome and browsers that support the HTML Speech API") %>"
									/>
							<% if (!bean.getEmbedResponsiveVoice()) { %>
								<script>
								if ('speechSynthesis' in window) {
									var voices = speechSynthesis.getVoices();
									var init = false;
									if (voices.length == 0) {
										speechSynthesis.onvoiceschanged = function() {
											voices = speechSynthesis.getVoices();
											if (init) {
												return;
											}
											init = true;
											var names = [];
											for (i = 0; i < voices.length; i++) {
												names[i] = voices[i].name;
											}
											$( "#native-voice-name" ).autocomplete({
											source: names,
											minLength: 0
											}).on('focus', function(event) {
												var self = this;
												$(self).autocomplete("search", "");
											});
										}
									} else {
										var names = [];
										for (i = 0; i < voices.length; i++) {
											names[i] = voices[i].name;
										}
										$( "#native-voice-name" ).autocomplete({
										source: names,
										minLength: 0
										}).on('focus', function(event) {
											var self = this;
											$(self).autocomplete("search", "");
										});
									}
								}
								
								var initNativeVoice = function() {
									voices = speechSynthesis.getVoices();
									var names = [];
									for (i = 0; i < voices.length; i++) {
										names[i] = voices[i].name;
									}
									$( "#native-voice-name" ).autocomplete({
									source: names,
									minLength: 0
									}).on('focus', function(event) {
										var self = this;
										$(self).autocomplete("search", "");
									});
								}
								</script>
							<% } %>
						</td>
					</tr>
					
					<!-- Begin Responsive Voice -->
					
					<tr id="responsiveVoiceTr">
						<script src='https://code.responsivevoice.org/responsivevoice.js?key=<%= Site.RESPONSIVEVOICE_KEY %>'></script>
						<script>
							var initResponsiveVoice = function() {
		   						document.getElementById("responsive-voice-div").style.display = "inline";

								SDK.initResponsiveVoice();
								SDK.responsiveVoice = true;
								var voices = responsiveVoice.getVoices();
								var names = [];
								for (i = 0; i < voices.length; i++) {
									names[i] = voices[i].name;
								}
								$( "#native-voice-name" ).autocomplete({
								source: names,
								minLength: 0
								}).on('focus', function(event) {
									var self = this;
									$(self).autocomplete("search", "");
								});
							}
						</script>
						<td>
							<%= loginBean.translate("Responsive Voice") %>
						</td>
						<td>
							<div id="responsive-voice-div" style="display:none;width:300px;vertical-align:top;font-family: Arial;font-size:9pt;line-height: normal">
							<%= loginBean.translate("ResponsiveVoice is a third party speech API. You must create an account at ") %> <a target="_blank" href="https://responsivevoice.org">responsivevoice.org</a>.
							<br/>
							<% if (!Site.COMMERCIAL) { %>
								<%= loginBean.translate("ResponsiveVoice is only offered through our commercial website ") %> <a target="_blank" href="https://www.botlibre.biz">botlibre.biz</a>.
								<br/>
								<%= loginBean.translate("You can only test the voice here.") %>.
								<br/>
							<% } %>
							<a rel="license" href="//responsivevoice.org/"><img title="ResponsiveVoice Text To Speech" src="https://responsivevoice.org/wp-content/uploads/2014/08/120x31.png" style="float:left;padding-right:2px" /></a><span xmlns:dct="http://purl.org/dc/terms/" property="dct:title"><a href="//responsivevoice.org/" target="_blank" title="ResponsiveVoice Text To Speech">ResponsiveVoice</a></span> used under <a rel="license" href="http://creativecommons.org/licenses/by-nc-nd/4.0/" title="Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License">Non-Commercial License</a></div>
							<div style="clear:both;">&nbsp;</div>
						</td>
						<% if (bean.getEmbedResponsiveVoice()) { %>
							<script>
								initResponsiveVoice();
							</script>
						<% } %>
						
					</tr>
					
					<!-- End Responsive Voice -->
					
					<!-- Begin Bing Speech -->
					<script>
							var initBingSpeech = function() {
		   						SDK.initBingSpeech("<%=bean.getInstanceId()%>", "avatar");
								SDK.bingSpeech = true;
							}

					</script>
					<tr id="bingVoiceTr">
						<td>
							<%= loginBean.translate("Voice") %>
						</td>
						<td>
							<select id="bingSpeechVoice" name="bingSpeechVoice">
								<option value="en-US, ZiraRUS" <%= bean.getNativeVoiceCheckedString("en-US, ZiraRUS") %>><%= loginBean.translate("en-US, ZiraRUS") %></option>
								<option value="ar-EG, Hoda" <%= bean.getNativeVoiceCheckedString("ar-EG, Hoda") %>><%= loginBean.translate("ar-EG, Hoda") %></option>
								<option value="ar-SA, Naayf" <%= bean.getNativeVoiceCheckedString("ar-SA, Naayf") %>><%= loginBean.translate("ar-SA, Naayf") %></option>
								<option value="bg-BG, Ivan" <%= bean.getNativeVoiceCheckedString("bg-BG, Ivan") %>><%= loginBean.translate("bg-BG, Ivan") %></option>
								<option value="ca-ES, HerenaRUS" <%= bean.getNativeVoiceCheckedString("ca-ES, HerenaRUS") %>><%= loginBean.translate("ca-ES, HerenaRUS") %></option>
								<option value="cs-CZ, Jakub" <%= bean.getNativeVoiceCheckedString("cs-CZ, Jakub") %>><%= loginBean.translate("cs-CZ, Jakub") %></option>
								<option value="da-DK, HelleRUS" <%= bean.getNativeVoiceCheckedString("da-DK, HelleRUS") %>><%= loginBean.translate("da-DK, HelleRUS") %></option>
								<option value="de-AT, Michael" <%= bean.getNativeVoiceCheckedString("de-AT, Michael") %>><%= loginBean.translate("de-AT, Michael") %></option>
								<option value="de-CH, Karsten" <%= bean.getNativeVoiceCheckedString("de-CH, Karsten") %>><%= loginBean.translate("de-CH, Karsten") %></option>
								<option value="de-DE, Hedda" <%= bean.getNativeVoiceCheckedString("de-DE, Hedda") %>><%= loginBean.translate("de-DE, Hedda") %></option>
								<option value="de-DE, HeddaRUS" <%= bean.getNativeVoiceCheckedString("de-DE, HeddaRUS") %>><%= loginBean.translate("de-DE, HeddaRUS") %></option>
								<option value="de-DE, Stefan, Apollo" <%= bean.getNativeVoiceCheckedString("de-DE, Stefan, Apollo") %>><%= loginBean.translate("de-DE, Stefan, Apollo") %></option>
								<option value="el-GR, Stefanos" <%= bean.getNativeVoiceCheckedString("el-GR, Stefanos") %>><%= loginBean.translate("el-GR, Stefanos") %></option>
								<option value="en-AU, Catherine" <%= bean.getNativeVoiceCheckedString("en-AU, Catherine") %>><%= loginBean.translate("en-AU, Catherine") %></option>
								<option value="en-AU, HayleyRUS" <%= bean.getNativeVoiceCheckedString("en-AU, HayleyRUS") %>><%= loginBean.translate("en-AU, HayleyRUS") %></option>
								<option value="en-CA, Linda" <%= bean.getNativeVoiceCheckedString("en-CA, Linda") %>><%= loginBean.translate("en-CA, Linda") %></option>
								<option value="en-CA, HeatherRUS" <%= bean.getNativeVoiceCheckedString("en-CA, HeatherRUS") %>><%= loginBean.translate("en-CA, HeatherRUS") %></option>
								<option value="en-GB, Susan, Apollo" <%= bean.getNativeVoiceCheckedString("en-GB, Susan, Apollo") %>><%= loginBean.translate("en-GB, Susan, Apollo") %></option>
								<option value="en-GB, HazelRUS" <%= bean.getNativeVoiceCheckedString("en-GB, HazelRUS") %>><%= loginBean.translate("en-GB, HazelRUS") %></option>
								<option value="en-GB, George, Apollo" <%= bean.getNativeVoiceCheckedString("en-GB, George, Apollo") %>><%= loginBean.translate("en-GB, George, Apollo") %></option>
								<option value="en-IE, Sean" <%= bean.getNativeVoiceCheckedString("en-IE, Sean") %>><%= loginBean.translate("en-IE, Sean") %></option>
								<option value="en-IN, Heera, Apollo" <%= bean.getNativeVoiceCheckedString("en-IN, Heera, Apollo") %>><%= loginBean.translate("en-IN, Heera, Apollo") %></option>
								<option value="en-IN, PriyaRUS" <%= bean.getNativeVoiceCheckedString("en-IN, PriyaRUS") %>><%= loginBean.translate("en-IN, PriyaRUS") %></option>
								<option value="en-IN, Ravi, Apollo" <%= bean.getNativeVoiceCheckedString("en-IN, Ravi, Apollo") %>><%= loginBean.translate("en-IN, Ravi, Apollo") %></option>
								<option value="en-US, JessaRUS" <%= bean.getNativeVoiceCheckedString("en-US, JessaRUS") %>><%= loginBean.translate("en-US, JessaRUS") %></option>
								<option value="en-US, BenjaminRUS" <%= bean.getNativeVoiceCheckedString("en-US, BenjaminRUS") %>><%= loginBean.translate("en-US, BenjaminRUS") %></option>
								<option value="es-ES, Laura, Apollo" <%= bean.getNativeVoiceCheckedString("es-ES, Laura, Apollo") %>><%= loginBean.translate("es-ES, Laura, Apollo") %></option>
								<option value="es-ES, HelenaRUS" <%= bean.getNativeVoiceCheckedString("es-ES, HelenaRUS") %>><%= loginBean.translate("es-ES, HelenaRUS") %></option>
								<option value="es-ES, Pablo, Apollo" <%= bean.getNativeVoiceCheckedString("es-ES, Pablo, Apollo") %>><%= loginBean.translate("es-ES, Pablo, Apollo") %></option>
								<option value="es-MX, HildaRUS" <%= bean.getNativeVoiceCheckedString("es-MX, HildaRUS") %>><%= loginBean.translate("es-MX, HildaRUS") %></option>
								<option value="es-MX, Raul, Apollo" <%= bean.getNativeVoiceCheckedString("es-MX, Raul, Apollo") %>><%= loginBean.translate("es-MX, Raul, Apollo") %></option>
								<option value="fi-FI, HeidiRUS" <%= bean.getNativeVoiceCheckedString("fi-FI, HeidiRUS") %>><%= loginBean.translate("fi-FI, HeidiRUS") %></option>
								<option value="fr-CA, Caroline" <%= bean.getNativeVoiceCheckedString("fr-CA, Caroline") %>><%= loginBean.translate("fr-CA, Caroline") %></option>
								<option value="fr-CA, HarmonieRUS" <%= bean.getNativeVoiceCheckedString("fr-CA, HarmonieRUS") %>><%= loginBean.translate("fr-CA, HarmonieRUS") %></option>
								<option value="fr-CH, Guillaume" <%= bean.getNativeVoiceCheckedString("fr-CH, Guillaume") %>><%= loginBean.translate("fr-CH, Guillaume") %></option>
								<option value="fr-FR, Julie, Apollo" <%= bean.getNativeVoiceCheckedString("fr-FR, Julie, Apollo") %>><%= loginBean.translate("fr-FR, Julie, Apollo") %></option>
								<option value="fr-FR, HortenseRUS" <%= bean.getNativeVoiceCheckedString("fr-FR, HortenseRUS") %>><%= loginBean.translate("fr-FR, HortenseRUS") %></option>
								<option value="fr-FR, Paul, Apollo" <%= bean.getNativeVoiceCheckedString("fr-FR, Paul, Apollo") %>><%= loginBean.translate("fr-FR, Paul, Apollo") %></option>
								<option value="he-IL, Asaf" <%= bean.getNativeVoiceCheckedString("he-IL, Asaf") %>><%= loginBean.translate("he-IL, Asaf") %></option>
								<option value="hi-IN, Kalpana, Apollo" <%= bean.getNativeVoiceCheckedString("hi-IN, Kalpana, Apollo") %>><%= loginBean.translate("hi-IN, Kalpana, Apollo") %></option>
								<option value="hi-IN, Kalpana" <%= bean.getNativeVoiceCheckedString("hi-IN, Kalpana") %>><%= loginBean.translate("hi-IN, Kalpana") %></option>
								<option value="hi-IN, Hemant" <%= bean.getNativeVoiceCheckedString("hi-IN, Hemant") %>><%= loginBean.translate("hi-IN, Hemant") %></option>
								<option value="hr-HR, Matej" <%= bean.getNativeVoiceCheckedString("hr-HR, Matej") %>><%= loginBean.translate("hr-HR, Matej") %></option>
								<option value="hu-HU, Szabolcs" <%= bean.getNativeVoiceCheckedString("hu-HU, Szabolcs") %>><%= loginBean.translate("hu-HU, Szabolcs") %></option>
								<option value="id-ID, Andika" <%= bean.getNativeVoiceCheckedString("id-ID, Andika") %>><%= loginBean.translate("id-ID, Andika") %></option>
								<option value="it-IT, Cosimo, Apollo" <%= bean.getNativeVoiceCheckedString("it-IT, Cosimo, Apollo") %>><%= loginBean.translate("it-IT, Cosimo, Apollo") %></option>
								<option value="ja-JP, Ayumi, Apollo" <%= bean.getNativeVoiceCheckedString("ja-JP, Ayumi, Apollo") %>><%= loginBean.translate("ja-JP, Ayumi, Apollo") %></option>
								<option value="ja-JP, Ichiro, Apollo" <%= bean.getNativeVoiceCheckedString("ja-JP, Ichiro, Apollo") %>><%= loginBean.translate("ja-JP, Ichiro, Apollo") %></option>
								<option value="ja-JP, HarukaRUS" <%= bean.getNativeVoiceCheckedString("ja-JP, HarukaRUS") %>><%= loginBean.translate("ja-JP, HarukaRUS") %></option>
								<option value="ja-JP, LuciaRUS" <%= bean.getNativeVoiceCheckedString("ja-JP, LuciaRUS") %>><%= loginBean.translate("ja-JP, LuciaRUS") %></option>
								<option value="ja-JP, EkaterinaRUS" <%= bean.getNativeVoiceCheckedString("ja-JP, EkaterinaRUS") %>><%= loginBean.translate("ja-JP, EkaterinaRUS") %></option>
								<option value="ko-KR, HeamiRUS" <%= bean.getNativeVoiceCheckedString("ko-KR, HeamiRUS") %>><%= loginBean.translate("ko-KR, HeamiRUS") %></option>
								<option value="ms-MY, Rizwan" <%= bean.getNativeVoiceCheckedString("ms-MY, Rizwan") %>><%= loginBean.translate("ms-MY, Rizwan") %></option>
								<option value="nb-NO, HuldaRUS" <%= bean.getNativeVoiceCheckedString("nb-NO, HuldaRUS") %>><%= loginBean.translate("nb-NO, HuldaRUS") %></option>
								<option value="nl-NL, HannaRUS" <%= bean.getNativeVoiceCheckedString("nl-NL, HannaRUS") %>><%= loginBean.translate("nl-NL, HannaRUS") %></option>
								<option value="pl-PL, PaulinaRUS" <%= bean.getNativeVoiceCheckedString("pl-PL, PaulinaRUS") %>><%= loginBean.translate("pl-PL, PaulinaRUS") %></option>
								<option value="pt-BR, HeloisaRUS" <%= bean.getNativeVoiceCheckedString("pt-BR, HeloisaRUS") %>><%= loginBean.translate("pt-BR, HeloisaRUS") %></option>
								<option value="pt-BR, Daniel, Apollo" <%= bean.getNativeVoiceCheckedString("pt-BR, Daniel, Apollo") %>><%= loginBean.translate("pt-BR, Daniel, Apollo") %></option>
								<option value="pt-PT, HeliaRUS" <%= bean.getNativeVoiceCheckedString("pt-PT, HeliaRUS") %>><%= loginBean.translate("pt-PT, HeliaRUS") %></option>
								<option value="ro-RO, Andrei" <%= bean.getNativeVoiceCheckedString("ro-RO, Andrei") %>><%= loginBean.translate("ro-RO, Andrei") %></option>
								<option value="ru-RU, Irina, Apollo" <%= bean.getNativeVoiceCheckedString("ru-RU, Irina, Apollo") %>><%= loginBean.translate("ru-RU, Irina, Apollo") %></option>
								<option value="ru-RU, Pavel, Apollo" <%= bean.getNativeVoiceCheckedString("ru-RU, Pavel, Apollo") %>><%= loginBean.translate("ru-RU, Pavel, Apollo") %></option>
								<option value="sk-SK, Filip" <%= bean.getNativeVoiceCheckedString("sk-SK, Filip") %>><%= loginBean.translate("sk-SK, Filip") %></option>
								<option value="sl-SI, Lado" <%= bean.getNativeVoiceCheckedString("sl-SI, Lado") %>><%= loginBean.translate("sl-SI, Lado") %></option>
								<option value="sv-SE, HedvigRUS" <%= bean.getNativeVoiceCheckedString("sv-SE, HedvigRUS") %>><%= loginBean.translate("sv-SE, HedvigRUS") %></option>
								<option value="ta-IN, Valluvar" <%= bean.getNativeVoiceCheckedString("ta-IN, Valluvar") %>><%= loginBean.translate("ta-IN, Valluvar") %></option>
								<option value="th-TH, Pattara" <%= bean.getNativeVoiceCheckedString("th-TH, Pattara") %>><%= loginBean.translate("th-TH, Pattara") %></option>
								<option value="tr-TR, SedaRUS" <%= bean.getNativeVoiceCheckedString("tr-TR, SedaRUS") %>><%= loginBean.translate("tr-TR, SedaRUS") %></option>
								<option value="vi-VN, An" <%= bean.getNativeVoiceCheckedString("vi-VN, An") %>><%= loginBean.translate("vi-VN, An") %></option>
								<option value="zh-CN, HuihuiRUS" <%= bean.getNativeVoiceCheckedString("zh-CN, HuihuiRUS") %>><%= loginBean.translate("zh-CN, HuihuiRUS") %></option>
								<option value="zh-CN, Yaoyao, Apollo" <%= bean.getNativeVoiceCheckedString("zh-CN, Yaoyao, Apollo") %>><%= loginBean.translate("zh-CN, Yaoyao, Apollo") %></option>
								<option value="zh-CN, Kangkang, Apollo" <%= bean.getNativeVoiceCheckedString("zh-CN, Kangkang, Apollo") %>><%= loginBean.translate("zh-CN, Kangkang, Apollo") %></option>
								<option value="zh-HK, Tracy, Apollo" <%= bean.getNativeVoiceCheckedString("zh-HK, Tracy, Apollo") %>><%= loginBean.translate("zh-HK, Tracy, Apollo") %></option>
								<option value="zh-HK, TracyRUS" <%= bean.getNativeVoiceCheckedString("zh-HK, TracyRUS") %>><%= loginBean.translate("zh-HK, TracyRUS") %></option>
								<option value="zh-HK, Danny, Apollo" <%= bean.getNativeVoiceCheckedString("zh-HK, Danny, Apollo") %>><%= loginBean.translate("zh-HK, Danny, Apollo") %></option>
								<option value="zh-TW, Yating, Apollo" <%= bean.getNativeVoiceCheckedString("zh-TW, Yating, Apollo") %>><%= loginBean.translate("zh-TW, Yating, Apollo") %></option>
								<option value="zh-TW, HanHanRUS" <%= bean.getNativeVoiceCheckedString("zh-TW, HanHanRUS") %>><%= loginBean.translate("zh-TW, HanHanRUS") %></option>
								<option value="zh-TW, Zhiwei, Apollo" <%= bean.getNativeVoiceCheckedString("zh-TW, Zhiwei, Apollo") %>><%= loginBean.translate("zh-TW, Zhiwei, Apollo") %></option>
							</select>
						</td>
						
						<% if (bean.getEmbedBingSpeech()) { %>
							<script>
								initBingSpeech();
							</script>
						<% } %>
					</tr>
					
					<tr id="bingApiKeyTr">
				 	<% if(bean.isAdmin()) { %>
						<td>
							<%= loginBean.translate("Microsoft Speech API Key") %>
						</td>
						<td>
							<input type="text" name="bingSpeechApiKey" value="<%= bean.getEmbedNativeVoiceApiKey() %>">
						</td>
					<% } %>
					</tr>
					
					<tr id="bingApiEndpointTr">
				 	<% if(bean.isAdmin()) { %>
						<td>
							<%= loginBean.translate("Microsoft Speech API Endpoint") %>
						</td>
						<td>
							<input type="text" name="bingSpeechApiEndpoint" value="<%= bean.getEmbedVoiceApiEndpoint() %>">
						</td>
					<% } %>
					</tr>
					
					<!-- End Bing Speech -->
					
					<!-- Begin QQ Speech -->
					<tr id="qqVoiceTr">
						<script>
							var initQQSpeech = function() {
		   						document.getElementById("qq-speech-div").style.display = "inline";

								SDK.initQQSpeech("<%=bean.getInstanceId()%>", "avatar");
								SDK.qqSpeech = true;
							}
						</script>
						
						<td>
							<%= loginBean.translate("Voice") %>
						</td>
						<td>
							<select id="qqSpeechVoice" name="qqSpeechVoice">
								<option value="1" <%= bean.getNativeVoiceCheckedString("1") %>><%= loginBean.translate("Mandarin Male") %></option>
								<option value="5" <%= bean.getNativeVoiceCheckedString("5") %>><%= loginBean.translate("Jing Qi Female") %></option>
								<option value="6" <%= bean.getNativeVoiceCheckedString("6") %>><%= loginBean.translate("Huanxin Female") %></option>
								<option value="7" <%= bean.getNativeVoiceCheckedString("7") %>><%= loginBean.translate("Bi Sheng Female") %></option>
							</select>
						</td>
					</tr>
						
					<tr id="qqAppIdTr">
						<% if(bean.isAdmin()) { %>
						<td>							
							<%= loginBean.translate("QQ Speech App Id") %>
						</td>
						<td>
							<input type="text" name="qqSpeechAppId" value="<%= bean.getEmbedNativeVoiceAppId() %>">
						</td>
						<% } %>
					</tr>
					<tr id="qqAppKeyTr">
						<% if(bean.isAdmin()) { %>
						<td>
							<%= loginBean.translate("QQ Speech App Key") %>
						</td>
						<td>
							<input type="text" name="qqSpeechApiKey" value="<%= bean.getEmbedNativeVoiceApiKey() %>">
						</td>
						<% } %>
						<% if (bean.getEmbedQQSpeech()) { %>
							<script>
								initQQSpeech();
							</script>
						<% } %>
						
					</tr>
					
					<!-- End QQ Speech -->
					
					<tr>
						<td><%= loginBean.translate("Language") %></td>
						<td>
							<input id="language" type="text" name="language" value="<%= bean.getEmbedLang() %>"
									title="The language code for the native voice (i.e. en-US, fr, zh)"
									/>
							<script>
							$( "#language" ).autocomplete({
							source: ["en-US", "en-GB", "fr", "es", "it", "de", "pt", "ru", "zh", "ja", "ko", "te" ],
							minLength: 0
							}).on('focus', function(event) {
								var self = this;
								$(self).autocomplete("search", "");
							});
							</script>
						</td>
					</tr>
				
				<!-- End Voice -->
				<tr>
					<td><%= loginBean.translate("Width") %></td>
					<td>
						<input type="number" name="width" value="<%= bean.getEmbedWidth() %>" title="The width (in pixles) of the avatar box" />
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Height") %></td>
					<td>
						<input type="number" name="height" value="<%= bean.getEmbedHeight() %>" title="The height (in pixles) of the avatar box" />
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Background Color") %></td>
					<td>
						<input id="embedbackground" type="text" name="background" value="<%= bean.getEmbedBackground() %>" title="The background color to use" />
						<script>
						$( "#embedbackground" ).autocomplete({
						source: [<%= bean.getColorsString() %>],
						minLength: 0
						}).on('focus', function(event) {
							var self = this;
							$(self).autocomplete("search", "");
						});
						</script>
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Emotion") %></td>
					<td>
						<input id="emotion" name="emotion" type="text" value="<%= bean.getEmbedEmotion().toLowerCase() %>" title="Emotion for avatar to express" /><br/>
						<script>
						$( "#emotion" ).autocomplete({
							source: [<%= bean.getAvailableEmotionsString() %>],
							minLength: 0
							}).on('focus', function(event) {
								var self = this;
								$(self).autocomplete("search", "");
							});
						</script>
					</td>
				</tr><tr>
					<td><%= loginBean.translate("Action") %></td>
					<td>
						<input id="action" name="action" type="text" value="<%= bean.getEmbedAction() %>" title="Action for avatar to perform" /><br/>
						<script>
						$( "#action" ).autocomplete({
							source: [<%= bean.getAvailableActionsString() %>],
							minLength: 0
							}).on('focus', function(event) {
								var self = this;
								$(self).autocomplete("search", "");
							});
						</script>
					</td>
				</tr><tr>
					<td><%= loginBean.translate("Pose") %></td>
					<td>
						<input id="pose" name="pose" type="text" value="<%= bean.getEmbedPose() %>" title="Pose for avatar to hold" /><br/>
						<script>
						$( "#pose" ).autocomplete({
							source: [<%= bean.getAvailablePosesString() %>],
							minLength: 0
							}).on('focus', function(event) {
								var self = this;
								$(self).autocomplete("search", "");
							});
						</script>
					</td>
				</tr>
			</table>
		</form>
		<% bean.setEmbedCode(""); %>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	
	<script>
		document.getElementById('provider')
				.addEventListener('change', function () {
			var value = this.value;		
			showProviderControls(value);		
		});
		
		var showBotLibreControls = function() {
			console.log("Showing bot libre controls");
			document.getElementById('botlibreVoiceTr').style.display = 'table-row';
			document.getElementById('botlibreVoiceModTr').style.display = 'table-row';
			document.getElementById('nativeVoiceTr').style.display = 'none';
			document.getElementById('responsiveVoiceTr').style.display = 'none';
			document.getElementById('bingVoiceTr').style.display = 'none';
			document.getElementById('bingApiKeyTr').style.display = 'none';
			document.getElementById('bingApiEndpointTr').style.display = 'none';
			document.getElementById('bingDescTr').style.display = 'none';
			document.getElementById('qqVoiceTr').style.display = 'none';
			document.getElementById('qqAppIdTr').style.display = 'none';
			document.getElementById('qqAppKeyTr').style.display = 'none';
			document.getElementById('qqDescTr').style.display = 'none';
			SDK.responsiveVoice = false;
			SDK.qqSpeech = false;
			SDK.bingSpeech = false;
		}
		
		var showHTML5Controls = function() {
			console.log("Showing html5 controls");
			document.getElementById('botlibreVoiceTr').style.display = 'none';
			document.getElementById('botlibreVoiceModTr').style.display = 'none';
			document.getElementById('nativeVoiceTr').style.display = 'table-row';
			document.getElementById('responsiveVoiceTr').style.display = 'none';
			document.getElementById('bingVoiceTr').style.display = 'none';
			document.getElementById('bingApiKeyTr').style.display = 'none';
			document.getElementById('bingApiEndpointTr').style.display = 'none';
			document.getElementById('bingDescTr').style.display = 'none';
			document.getElementById('qqVoiceTr').style.display = 'none';
			document.getElementById('qqAppIdTr').style.display = 'none';
			document.getElementById('qqAppKeyTr').style.display = 'none';
			document.getElementById('qqDescTr').style.display = 'none';
			initNativeVoice();
			SDK.responsiveVoice = false;
			SDK.qqSpeech = false;
			SDK.bingSpeech = false;
		}
		
		var showResponsiveVoiceControls = function() {
			console.log("Showing responsive controls");
			document.getElementById('botlibreVoiceTr').style.display = 'none';
			document.getElementById('botlibreVoiceModTr').style.display = 'none';
			document.getElementById('nativeVoiceTr').style.display = 'table-row';
			document.getElementById('responsiveVoiceTr').style.display = 'table-row';
			document.getElementById('bingVoiceTr').style.display = 'none';
			document.getElementById('bingApiKeyTr').style.display = 'none';
			document.getElementById('bingApiEndpointTr').style.display = 'none';
			document.getElementById('bingDescTr').style.display = 'none';
			document.getElementById('qqVoiceTr').style.display = 'none';
			document.getElementById('qqAppIdTr').style.display = 'none';
			document.getElementById('qqAppKeyTr').style.display = 'none';
			document.getElementById('qqDescTr').style.display = 'none';
			initResponsiveVoice();
			SDK.qqSpeech = false;
			SDK.bingSpeech = false;
		}
		
		var showBingControls = function() {
			console.log("Showing bing controls");
			document.getElementById('botlibreVoiceTr').style.display = 'none';
			document.getElementById('botlibreVoiceModTr').style.display = 'none';
			document.getElementById('nativeVoiceTr').style.display = 'none';
			document.getElementById('responsiveVoiceTr').style.display = 'none';
			document.getElementById('bingVoiceTr').style.display = 'table-row';
			document.getElementById('bingApiKeyTr').style.display = 'table-row';
			document.getElementById('bingApiEndpointTr').style.display = 'table-row';
			document.getElementById('bingDescTr').style.display = 'table-row';
			document.getElementById('qqVoiceTr').style.display = 'none';
			document.getElementById('qqAppIdTr').style.display = 'none';
			document.getElementById('qqAppKeyTr').style.display = 'none';
			document.getElementById('qqDescTr').style.display = 'none';
			initBingSpeech();
			SDK.responsiveVoice = false;
			SDK.qqSpeech = false;
		}
		
		var showQQControls = function() {
			console.log("Showing qq controls");
			document.getElementById('botlibreVoiceTr').style.display = 'none';
			document.getElementById('botlibreVoiceModTr').style.display = 'none';
			document.getElementById('nativeVoiceTr').style.display = 'none';
			document.getElementById('responsiveVoiceTr').style.display = 'none';
			document.getElementById('bingVoiceTr').style.display = 'none';
			document.getElementById('bingApiKeyTr').style.display = 'none';
			document.getElementById('bingApiEndpointTr').style.display = 'none';
			document.getElementById('bingDescTr').style.display = 'none';
			document.getElementById('qqVoiceTr').style.display = 'table-row';
			document.getElementById('qqAppIdTr').style.display = 'table-row';
			document.getElementById('qqAppKeyTr').style.display = 'table-row';
			document.getElementById('qqDescTr').style.display = 'table-row';
			initQQSpeech();
			SDK.responsiveVoice = false;
			SDK.bingSpeech = false;
		}
		
		var showProviderControls = function(provider) {
			if (provider === "botlibre") {
				showBotLibreControls();
			} else if (provider === "html5") {
				showHTML5Controls();
			} else if (provider === "responsive") {
				showResponsiveVoiceControls();
			} else if (provider === "bing") {
				showBingControls();
			} else if (provider === "qq") {
				showQQControls();
			}
		}
		
		//Check provider selected at startup
		showProviderControls(document.getElementById('provider').value);
		
	</script>
	
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>
