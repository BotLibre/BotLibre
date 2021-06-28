<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.UserBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
%>
<% UserBean userBean = loginBean.getBean(UserBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Voice") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Choose and test your user's voice") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("voice, tts, speech, html5 speech, language, test, text to speech") %>"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<jsp:include page="banner.jsp" />
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<div class="clearfix">
				<a href="<%= "login?view-user=" + loginBean.getUserId() %>"><%= loginBean.getUserId() %></a>
			</div>
		</div>
	</div>
	<jsp:include page="admin-user-banner.jsp" />
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The voice tab allows you to select your user's language and voice.") %><br/>
					<%= loginBean.translate("You can from several speech providers.") %><br/>
					<%= loginBean.translate("You can use the HTML5 voice on web browsers that support the HTML Speech API such as Chrome.") %><br/>
					<%= loginBean.translate("The HTML5 voice will use Android or iOS Speech on mobile apps.") %><br/>
					<%= loginBean.translate("This list of HTML5 voices depend on the platform and OS, and the devices configuration.") %><br/>
					<%= loginBean.translate("A Bot Libre voice is consistent across all platforms.") %><br/>
					<%= loginBean.translate("If you use an HTML5 voice, and the browser or platform does not support TTS, then the Bot Libre voice will be used as a fall back.") %>
					<%= loginBean.translate("If an HTML5 voice is set, and it is available it will be used, otherwise any voice for the language will be used, otherwise the default HTML5 voice.") %>
				</div>
				<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-voice.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
		<div id="contents">
			<div class="browse">
				<h1>
					<span class="dropt-banner">
						<img src="images/voice1.png" class="admin-banner-pic">
						<div>
							<p class="help">
								<%= loginBean.translate("Configure your user's language and voice.") %><br/>
							</p>
						</div>
					</span> <%= loginBean.translate("Voice") %>
				</h1>
				<% if (!loginBean.isLoggedIn()) { %>
					<p style="color:#E00000;">
						<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your voice settings") %>.
					</p>
					</div>
					</div>
					</div>
				<% } else { %>
					<jsp:include page="error.jsp" />
					<form action="user-voice" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<table>
						<tr>
							<td><%= loginBean.translate("Speech Provider") %></td>
							<td>
								<select id="provider" name="provider">
									<option value="botlibre" <% if (!userBean.getUser().isNativeVoice()) { %>selected<% } %>><%= loginBean.translate("Bot Libre") %></option>
									<option value="html5" <% if (userBean.getUser().isNativeVoice()) { %>selected<% } %>><%= loginBean.translate("HTML5") %></option>
									<option value="responsive" <% if (userBean.getResponsiveVoice()) { %>selected<% } %> ><%= loginBean.translate("ResponsiveVoice") %></option>
									<option value="bing" <% if (userBean.getBingSpeech()) { %>selected<% } %>><%= loginBean.translate("Microsoft Speech") %></option>
									<option value="qq" <% if (userBean.getQQSpeech()) { %>selected<% } %>><%= loginBean.translate("QQ") %></option>
								</select>
							</td>
						</tr>
				
						<tr id="bingDescTr" style="display:none">
							<td></td>
							<td>
								<div id="bing-speech-div" style="width:300px;vertical-align:top;font-family: Arial;font-size:9pt;line-height: normal">
								<%= loginBean.translate("Microsoft Speech is a third party speech API. You must create an account at ") %> <a target="_blank" href="https://azure.microsoft.com/try/cognitive-services/">https://azure.microsoft.com</a>.
								</div>
							</td>
						</tr>
						
						<tr id="qqDescTr" style="display:none">
							<td></td>
							<td>
								<div id="qq-speech-div" style="display:none;width:300px;vertical-align:top;font-family: Arial;font-size:9pt;line-height: normal">
								<%= loginBean.translate("QQ is a third party speech API. You must create an account at ") %> <a target="_blank" href="https://ai.qq.com">ai.qq.com</a>.
								</div>
							</td>
						</tr>
				
						<tr id="botlibreVoiceTr" style="display:none">
							<td><%= loginBean.translate("Voice") %></td>
								<td><select id="voice" name="voice">
									<option value="cmu-slt" <%= userBean.getVoiceCheckedString("cmu-slt") %>><%= loginBean.translate("English : US : Female : SLT") %></option>
									<option value="cmu-slt-hsmm" <%= userBean.getVoiceCheckedString("cmu-slt-hsmm") %>><%= loginBean.translate("English : US : Female : SLT (hsmm)") %></option>
									<option value="cmu-bdl" <%= userBean.getVoiceCheckedString("cmu-bdl") %>><%= loginBean.translate("English : US : Male : BDL") %></option>
									<option value="cmu-bdl-hsmm" <%= userBean.getVoiceCheckedString("cmu-bdl-hsmm") %>><%= loginBean.translate("English : US : Male : BDL (hsmm)") %></option>
									<option value="cmu-rms" <%= userBean.getVoiceCheckedString("cmu-rms") %>><%= loginBean.translate("English : US : Male : RMS") %></option>
									<option value="cmu-rms-hsmm" <%= userBean.getVoiceCheckedString("cmu-rms-hsmm") %>><%= loginBean.translate("English : US : Male : RMS (hsmm)") %></option>
									<option value="dfki-prudence" <%= userBean.getVoiceCheckedString("dfki-prudence") %>><%= loginBean.translate("English : GB : Female : Prudence") %></option>
									<option value="dfki-prudence-hsmm" <%= userBean.getVoiceCheckedString("dfki-prudence-hsmm") %>><%= loginBean.translate("English : GB : Female : Prudence (hsmm)") %></option>
									<option value="dfki-spike" <%= userBean.getVoiceCheckedString("dfki-spike") %>><%= loginBean.translate("English : GB : Male : Spike") %></option>
									<option value="dfki-spike-hsmm" <%= userBean.getVoiceCheckedString("dfki-spike-hsmm") %>><%= loginBean.translate("English : GB : Male : Spike (hsmm)") %></option>
									<option value="dfki-obadiah" <%= userBean.getVoiceCheckedString("dfki-obadiah") %>><%= loginBean.translate("English : GB : Male : Obadiah") %></option>
									<option value="dfki-obadiah-hsmm" <%= userBean.getVoiceCheckedString("dfki-obadiah-hsmm") %>><%= loginBean.translate("English : GB : Male : Obadiah (hsmm)") %></option>
									<option value="dfki-poppy" <%= userBean.getVoiceCheckedString("dfki-poppy") %>><%= loginBean.translate("English : GB : Female : Poppy") %></option>
									<option value="dfki-poppy-hsmm" <%= userBean.getVoiceCheckedString("dfki-poppy-hsmm") %>><%= loginBean.translate("English : GB : Female : Poppy (hsmm)") %></option>
									<option value="bits1" <%= userBean.getVoiceCheckedString("bits1") %>><%= loginBean.translate("German : DE : Female : Bits1") %></option>
									<option value="bits1-hsmm" <%= userBean.getVoiceCheckedString("bits1-hsmm") %>><%= loginBean.translate("German : DE : Female : Bits1 (hsmm)") %></option>
									<option value="bits3" <%= userBean.getVoiceCheckedString("bits3") %>><%= loginBean.translate("German : DE : Male : Bits3") %></option>
									<option value="bits3-hsmm" <%= userBean.getVoiceCheckedString("bits3-hsmm") %>><%= loginBean.translate("German : DE : Male : Bits3 (hsmm)") %></option>
									<option value="dfki-pavoque-neutral-hsmm" <%= userBean.getVoiceCheckedString("dfki-pavoque-neutral-hsmm") %>><%= loginBean.translate("German : DE : Male : Pavoque (hsmm)") %></option>
									<option value="camille" <%= userBean.getVoiceCheckedString("camille") %>><%= loginBean.translate("French : FR : Female : Camille") %></option>
									<option value="camille-hsmm-hsmm" <%= userBean.getVoiceCheckedString("camille-hsmm-hsmm") %>><%= loginBean.translate("French : FR : Female : Camille (hsmm)") %></option>
									<option value="jessica_voice" <%= userBean.getVoiceCheckedString("jessica_voice") %>><%= loginBean.translate("French : FR : Female : Jessica") %></option>
									<option value="jessica_voice-hsmm" <%= userBean.getVoiceCheckedString("jessica_voice-hsmm") %>><%= loginBean.translate("French : FR : Female : Jessica (hsmm)") %></option>
									<option value="pierre-voice" <%= userBean.getVoiceCheckedString("pierre-voice") %>><%= loginBean.translate("French : FR : Male : Pierre") %></option>
									<option value="pierre-voice-hsmm" <%= userBean.getVoiceCheckedString("pierre-voice-hsmm") %>><%= loginBean.translate("French : FR : Male : Pierre (hsmm)") %></option>
									<option value="enst-dennys-hsmm" <%= userBean.getVoiceCheckedString("enst-dennys-hsmm") %>><%= loginBean.translate("French : FR : Male : Dennys (hsmm)") %></option>
									<option value="marylux" <%= userBean.getVoiceCheckedString("marylux") %>><%= loginBean.translate("Luxembourgish : LU : Female : Lux") %></option>
									<option value="istc-lucia-hsmm" <%= userBean.getVoiceCheckedString("istc-lucia-hsmm") %>><%= loginBean.translate("Italian : IT : Male : Lucia (hsmm)") %></option>
									<option value="voxforge-ru-nsh" <%= userBean.getVoiceCheckedString("voxforge-ru-nsh") %>><%= loginBean.translate("Russian : RU : Male : NSH (hsmm)") %></option>
									<option value="dfki-ot" <%= userBean.getVoiceCheckedString("dfki-ot") %>><%= loginBean.translate("Turkish : TR : Male : OT") %></option>
									<option value="dfki-ot-hsmm" <%= userBean.getVoiceCheckedString("dfki-ot-hsmm") %>><%= loginBean.translate("Turkish : TR : Male : OT (hsmm)") %></option>
									<!-- option value="cmu-nk" <%= userBean.getVoiceCheckedString("cmu-nk") %>><%= loginBean.translate("Telugu : TE : Female : NK") %></option-->
									<option value="cmu-nk-hsmm" <%= userBean.getVoiceCheckedString("cmu-nk-hsmm") %>><%= loginBean.translate("Telugu : TE : Female : NK (hsmm)") %></option>
								</select>
							</td>
						</tr>
						<tr id="botlibreVoiceModTr" style="display:none">
							<td><%= loginBean.translate("Voice Modifier") %></td>
							<td>
								<select id="voice-mod" name="voice-mod">
									<option value="default" <%= userBean.getVoiceModCheckedString("default") %>><%= loginBean.translate("Default") %></option>
									<option value="child" <%= userBean.getVoiceModCheckedString("child") %>><%= loginBean.translate("Child") %></option>
									<option value="whisper" <%= userBean.getVoiceModCheckedString("whisper") %>><%= loginBean.translate("Whisper") %></option>
									<option value="echo" <%= userBean.getVoiceModCheckedString("echo") %>><%= loginBean.translate("Echo") %></option>
									<option value="robot" <%= userBean.getVoiceModCheckedString("robot") %>><%= loginBean.translate("Robot") %></option>
								</select>
							</td>
						</tr> 
						
						<tr id="nativeVoiceTr" style="display:none">
							<td>
								<%= loginBean.translate("Voice") %>
							</td>
							<td>
								<input id="native-voice-name" type="text" name="native-voice-name" value="<%= userBean.getUser().getNativeVoiceName() %>"
										title="<%= loginBean.translate("The name of the native voice. A native mobile device or browser voice can be used on Android, iOS, Chrome and browsers that support the HTML Speech API") %>"
										/>
								<% if (!userBean.getResponsiveVoice()) { %>
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
						
						<tr id="responsiveVoiceTr" style="display:none">
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
							<% if (userBean.getResponsiveVoice()) { %>
								<script>
									initResponsiveVoice();
								</script>
							<% } %>
							
						</tr>
						
						<!-- End Responsive Voice -->
						
						
						<!-- Begin Bing Speech -->
						<script>
							var initBingSpeech = function() {
									SDK.initBingSpeech("<%=userBean.getUser().getUserId() %>");
								SDK.bingSpeech = true;
							}
						</script>
						<tr id="bingVoiceTr" style="display:none">
							<td>
								<%= loginBean.translate("Voice") %>
							</td>
							<td>
								<select id="bingSpeechVoice" name="bingSpeechVoice">
									<option value="en-US, ZiraRUS" <%= userBean.getNativeVoiceCheckedString("en-US, ZiraRUS") %>><%= loginBean.translate("en-US, ZiraRUS") %></option>
									<option value="ar-EG, Hoda" <%= userBean.getNativeVoiceCheckedString("ar-EG, Hoda") %>><%= loginBean.translate("ar-EG, Hoda") %></option>
									<option value="ar-SA, Naayf" <%= userBean.getNativeVoiceCheckedString("ar-SA, Naayf") %>><%= loginBean.translate("ar-SA, Naayf") %></option>
									<option value="bg-BG, Ivan" <%= userBean.getNativeVoiceCheckedString("bg-BG, Ivan") %>><%= loginBean.translate("bg-BG, Ivan") %></option>
									<option value="ca-ES, HerenaRUS" <%= userBean.getNativeVoiceCheckedString("ca-ES, HerenaRUS") %>><%= loginBean.translate("ca-ES, HerenaRUS") %></option>
									<option value="cs-CZ, Jakub" <%= userBean.getNativeVoiceCheckedString("cs-CZ, Jakub") %>><%= loginBean.translate("cs-CZ, Jakub") %></option>
									<option value="da-DK, HelleRUS" <%= userBean.getNativeVoiceCheckedString("da-DK, HelleRUS") %>><%= loginBean.translate("da-DK, HelleRUS") %></option>
									<option value="de-AT, Michael" <%= userBean.getNativeVoiceCheckedString("de-AT, Michael") %>><%= loginBean.translate("de-AT, Michael") %></option>
									<option value="de-CH, Karsten" <%= userBean.getNativeVoiceCheckedString("de-CH, Karsten") %>><%= loginBean.translate("de-CH, Karsten") %></option>
									<option value="de-DE, Hedda" <%= userBean.getNativeVoiceCheckedString("de-DE, Hedda") %>><%= loginBean.translate("de-DE, Hedda") %></option>
									<option value="de-DE, HeddaRUS" <%= userBean.getNativeVoiceCheckedString("de-DE, HeddaRUS") %>><%= loginBean.translate("de-DE, HeddaRUS") %></option>
									<option value="de-DE, Stefan, Apollo" <%= userBean.getNativeVoiceCheckedString("de-DE, Stefan, Apollo") %>><%= loginBean.translate("de-DE, Stefan, Apollo") %></option>
									<option value="el-GR, Stefanos" <%= userBean.getNativeVoiceCheckedString("el-GR, Stefanos") %>><%= loginBean.translate("el-GR, Stefanos") %></option>
									<option value="en-AU, Catherine" <%= userBean.getNativeVoiceCheckedString("en-AU, Catherine") %>><%= loginBean.translate("en-AU, Catherine") %></option>
									<option value="en-AU, HayleyRUS" <%= userBean.getNativeVoiceCheckedString("en-AU, HayleyRUS") %>><%= loginBean.translate("en-AU, HayleyRUS") %></option>
									<option value="en-CA, Linda" <%= userBean.getNativeVoiceCheckedString("en-CA, Linda") %>><%= loginBean.translate("en-CA, Linda") %></option>
									<option value="en-CA, HeatherRUS" <%= userBean.getNativeVoiceCheckedString("en-CA, HeatherRUS") %>><%= loginBean.translate("en-CA, HeatherRUS") %></option>
									<option value="en-GB, Susan, Apollo" <%= userBean.getNativeVoiceCheckedString("en-GB, Susan, Apollo") %>><%= loginBean.translate("en-GB, Susan, Apollo") %></option>
									<option value="en-GB, HazelRUS" <%= userBean.getNativeVoiceCheckedString("en-GB, HazelRUS") %>><%= loginBean.translate("en-GB, HazelRUS") %></option>
									<option value="en-GB, George, Apollo" <%= userBean.getNativeVoiceCheckedString("en-GB, George, Apollo") %>><%= loginBean.translate("en-GB, George, Apollo") %></option>
									<option value="en-IE, Sean" <%= userBean.getNativeVoiceCheckedString("en-IE, Sean") %>><%= loginBean.translate("en-IE, Sean") %></option>
									<option value="en-IN, Heera, Apollo" <%= userBean.getNativeVoiceCheckedString("en-IN, Heera, Apollo") %>><%= loginBean.translate("en-IN, Heera, Apollo") %></option>
									<option value="en-IN, PriyaRUS" <%= userBean.getNativeVoiceCheckedString("en-IN, PriyaRUS") %>><%= loginBean.translate("en-IN, PriyaRUS") %></option>
									<option value="en-IN, Ravi, Apollo" <%= userBean.getNativeVoiceCheckedString("en-IN, Ravi, Apollo") %>><%= loginBean.translate("en-IN, Ravi, Apollo") %></option>
									<option value="en-US, JessaRUS" <%= userBean.getNativeVoiceCheckedString("en-US, JessaRUS") %>><%= loginBean.translate("en-US, JessaRUS") %></option>
									<option value="en-US, BenjaminRUS" <%= userBean.getNativeVoiceCheckedString("en-US, BenjaminRUS") %>><%= loginBean.translate("en-US, BenjaminRUS") %></option>
									<option value="es-ES, Laura, Apollo" <%= userBean.getNativeVoiceCheckedString("es-ES, Laura, Apollo") %>><%= loginBean.translate("es-ES, Laura, Apollo") %></option>
									<option value="es-ES, HelenaRUS" <%= userBean.getNativeVoiceCheckedString("es-ES, HelenaRUS") %>><%= loginBean.translate("es-ES, HelenaRUS") %></option>
									<option value="es-ES, Pablo, Apollo" <%= userBean.getNativeVoiceCheckedString("es-ES, Pablo, Apollo") %>><%= loginBean.translate("es-ES, Pablo, Apollo") %></option>
									<option value="es-MX, HildaRUS" <%= userBean.getNativeVoiceCheckedString("es-MX, HildaRUS") %>><%= loginBean.translate("es-MX, HildaRUS") %></option>
									<option value="es-MX, Raul, Apollo" <%= userBean.getNativeVoiceCheckedString("es-MX, Raul, Apollo") %>><%= loginBean.translate("es-MX, Raul, Apollo") %></option>
									<option value="fi-FI, HeidiRUS" <%= userBean.getNativeVoiceCheckedString("fi-FI, HeidiRUS") %>><%= loginBean.translate("fi-FI, HeidiRUS") %></option>
									<option value="fr-CA, Caroline" <%= userBean.getNativeVoiceCheckedString("fr-CA, Caroline") %>><%= loginBean.translate("fr-CA, Caroline") %></option>
									<option value="fr-CA, HarmonieRUS" <%= userBean.getNativeVoiceCheckedString("fr-CA, HarmonieRUS") %>><%= loginBean.translate("fr-CA, HarmonieRUS") %></option>
									<option value="fr-CH, Guillaume" <%= userBean.getNativeVoiceCheckedString("fr-CH, Guillaume") %>><%= loginBean.translate("fr-CH, Guillaume") %></option>
									<option value="fr-FR, Julie, Apollo" <%= userBean.getNativeVoiceCheckedString("fr-FR, Julie, Apollo") %>><%= loginBean.translate("fr-FR, Julie, Apollo") %></option>
									<option value="fr-FR, HortenseRUS" <%= userBean.getNativeVoiceCheckedString("fr-FR, HortenseRUS") %>><%= loginBean.translate("fr-FR, HortenseRUS") %></option>
									<option value="fr-FR, Paul, Apollo" <%= userBean.getNativeVoiceCheckedString("fr-FR, Paul, Apollo") %>><%= loginBean.translate("fr-FR, Paul, Apollo") %></option>
									<option value="he-IL, Asaf" <%= userBean.getNativeVoiceCheckedString("he-IL, Asaf") %>><%= loginBean.translate("he-IL, Asaf") %></option>
									<option value="hi-IN, Kalpana, Apollo" <%= userBean.getNativeVoiceCheckedString("hi-IN, Kalpana, Apollo") %>><%= loginBean.translate("hi-IN, Kalpana, Apollo") %></option>
									<option value="hi-IN, Kalpana" <%= userBean.getNativeVoiceCheckedString("hi-IN, Kalpana") %>><%= loginBean.translate("hi-IN, Kalpana") %></option>
									<option value="hi-IN, Hemant" <%= userBean.getNativeVoiceCheckedString("hi-IN, Hemant") %>><%= loginBean.translate("hi-IN, Hemant") %></option>
									<option value="hr-HR, Matej" <%= userBean.getNativeVoiceCheckedString("hr-HR, Matej") %>><%= loginBean.translate("hr-HR, Matej") %></option>
									<option value="hu-HU, Szabolcs" <%= userBean.getNativeVoiceCheckedString("hu-HU, Szabolcs") %>><%= loginBean.translate("hu-HU, Szabolcs") %></option>
									<option value="id-ID, Andika" <%= userBean.getNativeVoiceCheckedString("id-ID, Andika") %>><%= loginBean.translate("id-ID, Andika") %></option>
									<option value="it-IT, Cosimo, Apollo" <%= userBean.getNativeVoiceCheckedString("it-IT, Cosimo, Apollo") %>><%= loginBean.translate("it-IT, Cosimo, Apollo") %></option>
									<option value="ja-JP, Ayumi, Apollo" <%= userBean.getNativeVoiceCheckedString("ja-JP, Ayumi, Apollo") %>><%= loginBean.translate("ja-JP, Ayumi, Apollo") %></option>
									<option value="ja-JP, Ichiro, Apollo" <%= userBean.getNativeVoiceCheckedString("ja-JP, Ichiro, Apollo") %>><%= loginBean.translate("ja-JP, Ichiro, Apollo") %></option>
									<option value="ja-JP, HarukaRUS" <%= userBean.getNativeVoiceCheckedString("ja-JP, HarukaRUS") %>><%= loginBean.translate("ja-JP, HarukaRUS") %></option>
									<option value="ja-JP, LuciaRUS" <%= userBean.getNativeVoiceCheckedString("ja-JP, LuciaRUS") %>><%= loginBean.translate("ja-JP, LuciaRUS") %></option>
									<option value="ja-JP, EkaterinaRUS" <%= userBean.getNativeVoiceCheckedString("ja-JP, EkaterinaRUS") %>><%= loginBean.translate("ja-JP, EkaterinaRUS") %></option>
									<option value="ko-KR, HeamiRUS" <%= userBean.getNativeVoiceCheckedString("ko-KR, HeamiRUS") %>><%= loginBean.translate("ko-KR, HeamiRUS") %></option>
									<option value="ms-MY, Rizwan" <%= userBean.getNativeVoiceCheckedString("ms-MY, Rizwan") %>><%= loginBean.translate("ms-MY, Rizwan") %></option>
									<option value="nb-NO, HuldaRUS" <%= userBean.getNativeVoiceCheckedString("nb-NO, HuldaRUS") %>><%= loginBean.translate("nb-NO, HuldaRUS") %></option>
									<option value="nl-NL, HannaRUS" <%= userBean.getNativeVoiceCheckedString("nl-NL, HannaRUS") %>><%= loginBean.translate("nl-NL, HannaRUS") %></option>
									<option value="pl-PL, PaulinaRUS" <%= userBean.getNativeVoiceCheckedString("pl-PL, PaulinaRUS") %>><%= loginBean.translate("pl-PL, PaulinaRUS") %></option>
									<option value="pt-BR, HeloisaRUS" <%= userBean.getNativeVoiceCheckedString("pt-BR, HeloisaRUS") %>><%= loginBean.translate("pt-BR, HeloisaRUS") %></option>
									<option value="pt-BR, Daniel, Apollo" <%= userBean.getNativeVoiceCheckedString("pt-BR, Daniel, Apollo") %>><%= loginBean.translate("pt-BR, Daniel, Apollo") %></option>
									<option value="pt-PT, HeliaRUS" <%= userBean.getNativeVoiceCheckedString("pt-PT, HeliaRUS") %>><%= loginBean.translate("pt-PT, HeliaRUS") %></option>
									<option value="ro-RO, Andrei" <%= userBean.getNativeVoiceCheckedString("ro-RO, Andrei") %>><%= loginBean.translate("ro-RO, Andrei") %></option>
									<option value="ru-RU, Irina, Apollo" <%= userBean.getNativeVoiceCheckedString("ru-RU, Irina, Apollo") %>><%= loginBean.translate("ru-RU, Irina, Apollo") %></option>
									<option value="ru-RU, Pavel, Apollo" <%= userBean.getNativeVoiceCheckedString("ru-RU, Pavel, Apollo") %>><%= loginBean.translate("ru-RU, Pavel, Apollo") %></option>
									<option value="sk-SK, Filip" <%= userBean.getNativeVoiceCheckedString("sk-SK, Filip") %>><%= loginBean.translate("sk-SK, Filip") %></option>
									<option value="sl-SI, Lado" <%= userBean.getNativeVoiceCheckedString("sl-SI, Lado") %>><%= loginBean.translate("sl-SI, Lado") %></option>
									<option value="sv-SE, HedvigRUS" <%= userBean.getNativeVoiceCheckedString("sv-SE, HedvigRUS") %>><%= loginBean.translate("sv-SE, HedvigRUS") %></option>
									<option value="ta-IN, Valluvar" <%= userBean.getNativeVoiceCheckedString("ta-IN, Valluvar") %>><%= loginBean.translate("ta-IN, Valluvar") %></option>
									<option value="th-TH, Pattara" <%= userBean.getNativeVoiceCheckedString("th-TH, Pattara") %>><%= loginBean.translate("th-TH, Pattara") %></option>
									<option value="tr-TR, SedaRUS" <%= userBean.getNativeVoiceCheckedString("tr-TR, SedaRUS") %>><%= loginBean.translate("tr-TR, SedaRUS") %></option>
									<option value="vi-VN, An" <%= userBean.getNativeVoiceCheckedString("vi-VN, An") %>><%= loginBean.translate("vi-VN, An") %></option>
									<option value="zh-CN, HuihuiRUS" <%= userBean.getNativeVoiceCheckedString("zh-CN, HuihuiRUS") %>><%= loginBean.translate("zh-CN, HuihuiRUS") %></option>
									<option value="zh-CN, Yaoyao, Apollo" <%= userBean.getNativeVoiceCheckedString("zh-CN, Yaoyao, Apollo") %>><%= loginBean.translate("zh-CN, Yaoyao, Apollo") %></option>
									<option value="zh-CN, Kangkang, Apollo" <%= userBean.getNativeVoiceCheckedString("zh-CN, Kangkang, Apollo") %>><%= loginBean.translate("zh-CN, Kangkang, Apollo") %></option>
									<option value="zh-HK, Tracy, Apollo" <%= userBean.getNativeVoiceCheckedString("zh-HK, Tracy, Apollo") %>><%= loginBean.translate("zh-HK, Tracy, Apollo") %></option>
									<option value="zh-HK, TracyRUS" <%= userBean.getNativeVoiceCheckedString("zh-HK, TracyRUS") %>><%= loginBean.translate("zh-HK, TracyRUS") %></option>
									<option value="zh-HK, Danny, Apollo" <%= userBean.getNativeVoiceCheckedString("zh-HK, Danny, Apollo") %>><%= loginBean.translate("zh-HK, Danny, Apollo") %></option>
									<option value="zh-TW, Yating, Apollo" <%= userBean.getNativeVoiceCheckedString("zh-TW, Yating, Apollo") %>><%= loginBean.translate("zh-TW, Yating, Apollo") %></option>
									<option value="zh-TW, HanHanRUS" <%= userBean.getNativeVoiceCheckedString("zh-TW, HanHanRUS") %>><%= loginBean.translate("zh-TW, HanHanRUS") %></option>
									<option value="zh-TW, Zhiwei, Apollo" <%= userBean.getNativeVoiceCheckedString("zh-TW, Zhiwei, Apollo") %>><%= loginBean.translate("zh-TW, Zhiwei, Apollo") %></option>
								</select>
							</td>
							
							<% if (userBean.getBingSpeech()) { %>
								<script>
									initBingSpeech();
								</script>
							<% } %>
						</tr>
						
						<tr id="bingApiKeyTr" style="display:none">
							<td>
								<%= loginBean.translate("Microsoft Speech API Key") %>
							</td>
							<td>
								<input type="text" id="bingSpeechApiKey" name="bingSpeechApiKey" value="<%= userBean.getUser().getNativeVoiceApiKey() %>">
							</td>
						</tr>
						<tr id="bingApiEndpointTr" style="display:none">
							<td>
								<%= loginBean.translate("Microsoft Speech API Endpoint") %>
							</td>
							<td>
								<input type="text" id="bingApiEndpoint" name="bingApiEndpoint" value="<%= userBean.getUser().getVoiceApiEndpoint() %>">
							</td>
						</tr>
									
						<!-- End Bing Speech -->
						
						<!-- Begin QQ Speech -->
						<tr id="qqVoiceTr" style="display:none">
							<script>
								var initQQSpeech = function() {
									document.getElementById("qq-speech-div").style.display = "inline";
	
									SDK.initQQSpeech("<%=userBean.getUser().getUserId()%>");
									SDK.qqSpeech = true;
								}
							</script>
							
							<td>
								<%= loginBean.translate("Voice") %>
							</td>
							<td>
								<select id="qqSpeechVoice" name="qqSpeechVoice">
									<option value="1" <%= userBean.getNativeVoiceCheckedString("1") %>><%= loginBean.translate("Mandarin Male") %></option>
									<option value="5" <%= userBean.getNativeVoiceCheckedString("5") %>><%= loginBean.translate("Jing Qi Female") %></option>
									<option value="6" <%= userBean.getNativeVoiceCheckedString("6") %>><%= loginBean.translate("Huanxin Female") %></option>
									<option value="7" <%= userBean.getNativeVoiceCheckedString("7") %>><%= loginBean.translate("Bi Sheng Female") %></option>
								</select>
							</td>
						</tr>
							
						<tr id="qqAppIdTr" style="display:none">
							<td>
								<%= loginBean.translate("QQ Speech App Id") %>
							</td>
							<td>
								<input type="text" name="qqSpeechAppId" value="<%= userBean.getUser().getNativeVoiceAppId() %>">
							</td>
						</tr>
						<tr id="qqAppKeyTr" style="display:none">
							<td>
								<%= loginBean.translate("QQ Speech App Key") %>
							</td>
							<td>
								<input type="text" name="qqSpeechApiKey" value="<%= userBean.getUser().getNativeVoiceApiKey() %>">
							</td>
							<% if (userBean.getQQSpeech()) { %>
								<script>
									initQQSpeech();
								</script>
							<% } %>
							
						</tr>
						
						<!-- End QQ Speech -->
						
						<tr>
							<td><%= loginBean.translate("Language") %></td>
							<td>
								<input id="language" type="text" name="language" value="<%= userBean.getUser().getLanguage() %>"
										title="The language code for the native voice (i.e. en-US, fr, zh)"/>
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
						</table>
						<% if (userBean.getUser() != null && userBean.isLoggedIn()) { %>
							<input type="submit" name="save" value="<%= loginBean.translate("Save") %>"/>
						<% } %>
						<br/>
						
						<hr>
						
						<h2><%= loginBean.translate("Test Speech") %></h2>
											
						<input type="text" style="padding-bottom:4px;max-width:700px" id="speak" name="speak" value="<%= userBean.getSpeakText() %>" />
						<script>
							var testVoice = function() {
								var text = document.getElementById('speak').value;
								var voice = document.getElementById('voice').value;
								var mod = document.getElementById('voice-mod').value;
								var nativeVoice = document.getElementById('provider').value !== 'botlibre';
								var nativeVoiceName = document.getElementById('native-voice-name').value;
								var apiKey = null;
								var apiEndpoint = null;
								var language = document.getElementById('language').value;
								if (document.getElementById('provider').value === 'bing') {
									nativeVoiceName = document.getElementById('bingSpeechVoice').value;
									apiKey = document.getElementById('bingSpeechApiKey').value;
									apiEndpoint = document.getElementById('bingApiEndpoint').value;
								} else if(document.getElementById('provider').value === 'qq') {
									nativeVoiceName = document.getElementById('qqSpeechVoice').value;
								}
								console.log(text);
								console.log(voice);
								console.log(nativeVoice);
								console.log(nativeVoiceName);
								console.log(language);
								SDK.tts(text, voice, nativeVoice, language, nativeVoiceName, mod, apiKey, apiEndpoint);
							}
						</script>
						<br/>
						
						<input type="submit" name="test" value="<%= loginBean.translate("Test") %>" onclick="testVoice(); return false;"/>
						<br/>
						<br/>
				</form>
			</div>
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
	<% } %>
	<jsp:include page="footer.jsp" />
</body>
</html>
