<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.bean.BrowseBean"%>
<%@page import="org.botlibre.web.admin.AccessMode"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.ChannelEmbedTabBean"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	LiveChatBean livechatBean = loginBean.getBean(LiveChatBean.class);
	String title = "Live Chat";
	if (livechatBean.getInstance() != null) {
		title = livechatBean.getInstance().getName();
	}
	ChannelEmbedTabBean bean = loginBean.getBean(ChannelEmbedTabBean.class);
	boolean help = loginBean.getHelp();
	loginBean.setHelp(false);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<%= loginBean.getJQueryHeader() %>
	<title><%= title %> Embed - <%= Site.NAME %></title>
	<meta name="description" content="Generate the JavaScript code to embed your live chat channel or chatroom on your own website"/>	
	<meta name="keywords" content="embed, javascript, channel, live chat, chatroom, chat"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="channel-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<% livechatBean.browseBannerHTML(out, proxy); %>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("You can embed your live chat channel or chat room on your own website or blog just by adding some simple html to your site.") %>
					<%= loginBean.translate("You are free to embed your own channels for personal, or commercial purposes.") %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-livechat.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1><%= loginBean.translate("Embed live chat on your own website or blog") %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!livechatBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to embed a channel") %>.
		</p>
	<% } else if (livechatBean.getInstance() == null) { %>
		<p><%= loginBean.translate("No channel selected.") %></p>
	<% } else if (!livechatBean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this channel.") %>
	<% } else { %>
			<% if (!bean.getCode().isEmpty()) { %>
				<%= bean.getCode() %>
				<h2><%= loginBean.translate("Embedding Code") %></h2>
				<!--  Browsers do not let you execute code that was submitted, so need this in separate form -->
				<form action="livechat" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= proxy.proxyInput() %>
					<%= livechatBean.instanceInput() %>
					<input name="codeToken" type="hidden" value="<%= bean.generateCodeToken() %>"/>
					<textarea name="code" ><%= bean.getDisplayCode() %></textarea><br/>
					<input id="ok" onclick="document.getElementById('get-code-form').submit(); return false;" type="submit" name="get-code" value="Generate Code"/>
					<input type="submit" name="run-code" value="Execute Code"/><br/>
				</form>
			<% } %>
			<h2><%= loginBean.translate("Embedding Options") %></h2>
			<form id="get-code-form" action="livechat" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<input id="embed" name="embed" type="hidden" value="embed"/>
				<table>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Type of embedding. Can be either box, link, button, or frame. The 'box' option is recommended.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Embedding Type") %>
						</td>
						<td>
							<select name="type" onchange="this.form.submit()">
								<option value="box" <%= bean.getTypeSelectedString("box") %>>Box</option>
								<option value="link" <%= bean.getTypeSelectedString("link") %>>Link</option>
								<option value="button" <%= bean.getTypeSelectedString("button") %>>Button</option>
								<option value="frame" <%= bean.getTypeSelectedString("frame") %>>Frame</option>
							</select><br/>
						</td>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Choose the look and feel for the embedded chat. Each option is defined by its CSS stylesheet, choose from 5 styles, or create your own by copying one of the other stylesheets and choosing custom.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Style Sheet") %>
						</td>
						<td>
							<select id="css" name="css" onchange="this.form.submit()">
								<option value="chatlog" <%= bean.getCssSelectedString("chatlog") %>>Chat Log</option>
								<option value="social_chat" <%= bean.getCssSelectedString("social_chat") %>>Social Chat</option>
								<option value="chatroom" <%= bean.getCssSelectedString("chatroom") %>>Chatroom</option>
								<option value="blue_chat" <%= bean.getCssSelectedString("blue_chat") %>>Blue Chat</option>
								<option value="pink_chat" <%= bean.getCssSelectedString("pink_chat") %>>Pink Chat</option>
								<option value="custom_chat" <%= bean.getCssSelectedString("custom_chat") %>>Custom Chat</option>
							</select><br/>
						</td>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Enter the URL to your own custom stylesheet. First choose 'Custom Chat' above.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Custom Style Sheet") %>
						</td>
						<td style="width: 480px;">
							<input id="customcss" type="text" name="customcss" disabled value="<%= bean.getCustomCss() %>" title="<%= loginBean.translate("Enter the URL to your own custom stylesheet. First choose 'Custom Chat' above.") %>" />
						</td>
						<script>
							var selectedCss = $('#css').val();
							if (selectedCss === "custom_chat") {
								$("#customcss").prop('disabled', false);
							}
							$('#css').on('change', function(e) {
								var selectedValue = e.currentTarget.value;
								if (selectedValue === "custom_chat") {
									$('#customcss').val("");
									$("#customcss").prop('disabled', false);
								} else {
									$("#customcss").prop('disabled', true);
								}
								$('#get-code-form').submit();
							});
						</script>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Choose the look and feel for the chat button. You can also customize the look using your own stylesheet.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Button Style Sheet") %>
						</td>
						<td>
							<select name="buttoncss" onchange="this.form.submit()">
								<option value="blue_round_button" <%= bean.getButtonCssSelectedString("blue_round_button") %>>Blue Round Button</option>
								<option value="red_round_button" <%= bean.getButtonCssSelectedString("red_round_button") %>>Red Round Button</option>
								<option value="green_round_button" <%= bean.getButtonCssSelectedString("green_round_button") %>>Green Round Button</option>
								<option value="blue_bot_button" <%= bean.getButtonCssSelectedString("blue_bot_button") %>>Blue Bot Button</option>
								<option value="red_bot_button" <%= bean.getButtonCssSelectedString("red_bot_button") %>>Red Bot Button</option>
								<option value="green_bot_button" <%= bean.getButtonCssSelectedString("green_bot_button") %>>Green Bot Button</option>
								<option value="purple_chat_button" <%= bean.getButtonCssSelectedString("purple_chat_button") %>>Purple Chat Button</option>
								<option value="red_chat_button" <%= bean.getButtonCssSelectedString("red_chat_button") %>>Red Chat Button</option>
								<option value="green_chat_button" <%= bean.getButtonCssSelectedString("green_chat_button") %>>Green Chat Button</option>
								<option value="square_chat_button" <%= bean.getButtonCssSelectedString("square_chat_button") %>>Square Chat Button</option>
								<option value="round_chat_button" <%= bean.getButtonCssSelectedString("round_chat_button") %>>Round Chat Button</option>
							</select><br/>
						</td>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Set the location of the button in your embedded chat.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Location") %>
						</td>
						<td>
							<select name="boxlocation" onchange="this.form.submit()">
								<option value="bottom-right" <%= bean.getLocationSelectedString("bottom-right") %>><%= loginBean.translate("Bottom Right") %></option>
								<option value="bottom-left" <%= bean.getLocationSelectedString("bottom-left") %>><%= loginBean.translate("Bottom Left") %></option>
								<option value="top-right" <%= bean.getLocationSelectedString("top-right") %>><%= loginBean.translate("Top Right") %></option>
								<option value="top-left" <%= bean.getLocationSelectedString("top-left") %>><%= loginBean.translate("Top Left") %></option>
							</select><br/>
						</td>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Set the language of embedded chat.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Language") %>
						</td>
						<td>
							<select id='chooselanguage' name="chooselanguage" onchange="this.form.submit()">
								<option value='en' <%= bean.getLanguageSelectedString("en") %>>English</option>
								<option value='fr' <%= bean.getLanguageSelectedString("fr") %>>French</option>
								<option value='es' <%= bean.getLanguageSelectedString("es") %>>Spanish</option>
								<option value='pt' <%= bean.getLanguageSelectedString("pt") %>>Portuguese</option>
								<option value='de' <%= bean.getLanguageSelectedString("de") %>>German</option>
								<option value='zh' <%= bean.getLanguageSelectedString("zh") %>>Chinese</option>
								<option value='ja' <%= bean.getLanguageSelectedString("ja") %>>Japanese</option>
								<option value='ar' <%= bean.getLanguageSelectedString("ar") %>>Arabic</option>
								<option value='ru' <%= bean.getLanguageSelectedString("ru") %>>Russian</option>
							</select>
						</td>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Set if users should be prompted for contact information before being allowed to chat.") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Ask for Contact Info") %>
						</td>
						<td>
							<input name="promptContactInfo" type="checkbox" title="<%= loginBean.translate("Set if users should be prompted for contact information before being allowed to chat.") %>"
								<% if (bean.getPromptContactInfo()) { %>checked<% } %>><br/>
						</td>
					</tr>
					<tr>
						<td>
							<span class="dropt-banner">
								<img id="help-mini" src="images/help.svg">
								<div>
									<p class="help">
										<%= loginBean.translate("Show advanced embedding options") %>
									</p>
								</div>
							</span>
							<%= loginBean.translate("Show Advanced Info") %>
						</td>
						<td>
							<input id="showAdvancedInfo" name="showAdvancedInfo" type="checkbox" title="<%= loginBean.translate("Show advanced embedding options") %>"
								<% if (bean.getAdvancedInfo()) { %>checked<% } %>><br/>
						</td>
					</tr>
				</table>
				<script>
					$('#showAdvancedInfo').change(function() {
						if ($(this).prop("checked")) {
							$('#advancedInfo').show();
						} else {
							$('#advancedInfo').hide();
						}
					});
				</script>
				<div id="advancedInfo" style="display:none;">
					<% if (livechatBean.isAdmin()) { %>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg">
							<div>
								<p class="help">
									<%= loginBean.translate("Choose a subdomain to host your channel's own website, or give a domain that you have registered and forward to this server's ip address") %>
								</p>
							</div>
						</span>
						<%= loginBean.translate("Subdomain (or domain)") %><br/>
						<input id="subdomain" name="subdomain" type="text" value="<%= livechatBean.getInstance().getSubdomain() %>" title="<%= loginBean.translate("You can choose a subdomain to host your channel's own website, or give a domain that you have registered and forward to this server's ip address") %>"  /><br/>
					<% } %>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Choose if the chat frame should start on the channel page or chat page") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Landing Page") %>
					<select name="landing" onchange="this.form.submit()">
						<option value="chat" <%= bean.getLandingSelectedString("chat") %>>Chat</option>
						<option value="channel" <%= bean.getLandingSelectedString("channel") %>>Channel</option>
					</select><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Set the caption text of the chat button or link") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Caption") %><br/>
					<input type="text" name="caption" value="<%= Utils.escapeHTML(bean.getCaption()) %>" title="<%= loginBean.translate("Set the caption text of the chat button or link") %>" /><br/>
					<% if (livechatBean.getInstance().isPrivate() || livechatBean.getInstance().getAccessMode() != AccessMode.Everyone) { %>
						<%= loginBean.translate("Guest User") %><br/>
						<input type="text" name="user" value="<%= bean.getUserName() %>" title="<%= loginBean.translate("Guest user to connect as") %>" /><br/>
						<%= loginBean.translate("Password") %><br/>
						<input type="password" name="password" value="<%= bean.getPassword() %>" title="<%= loginBean.translate("Password for guest user (not secure)") %>" /><br/>
						<%= loginBean.translate("Token") %><br/>
						<input type="text" name="token" value="<%= bean.getToken() %>" title="T<%= loginBean.translate("oken for guest user") %>" /><br/>
					<% } %>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Embed banner HTML into the channel's frame chat by setting HTML fragment or URL") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Banner HTML") %><br/>
					<input type="text" name="banner" value="<%= Utils.escapeHTML(bean.getBanner()) %>" title="<%= loginBean.translate("Embed banner HTML into the channel's frame chat by setting HTML fragment or URL") %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Embed footer HTML into the channel's embed chat by setting HTML fragment or URL") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Footer HTML") %><br/>
					<input type="text" name="footer" value="<%= Utils.escapeHTML(bean.getFooter()) %>" title="<%= loginBean.translate("Embed footer HTML into the channel's embed chat by setting HTML fragment or URL") %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Set width (in pixels) of frame, window, or box") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Width") %><br/>
					<input type="number" name="width" value="<%= bean.getWidth() %>" title="<%= loginBean.translate("The width (in pixles) of the frame, window, or box") %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Set height (in pixels) of frame, window, or box") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Height") %><br/>
					<input type="number" name="height" value="<%= bean.getHeight() %>" title="<%= loginBean.translate("The height (in pixles) of the frame, window, or box") %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Horizontal offset (in pixels) the box location") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Offset") %><br/>
					<input type="number" name="offset" value="<%= bean.getOffset() %>" title="<%= loginBean.translate("Horizontal offset (in pixels) the box location") %>" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Set the color of the square chat button (other buttons are images), (HTML color name or RGB HEX)") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Color") %><br/>
					<input type="text" id="embedcolor" name="color" value="<%= bean.getColor() %>" title="<%= loginBean.translate("Set the color of the square chat button (HTML color name or RGB HEX)") %>" /><br/>
					<script>
						$( "#embedcolor" ).autocomplete({
						source: [<%= BrowseBean.getColorsString() %>],
						minLength: 0
						}).on('focus', function(event) {
							var self = this;
							$(self).autocomplete("search", "");
						});
					</script>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Set the background color of the chat (HTML color name or RGB HEX)") %>
							</p>
						</div>
					</span>
					<%= loginBean.translate("Background Color") %><br/>
					<input type="text" id="embedbackground" name="background" value="<%= bean.getBackground() %>" title="<%= loginBean.translate("Set the background color of the chat (HTML color name or RGB HEX)") %>" /><br/>
					<script>
						$( "#embedbackground" ).autocomplete({
						source: [<%= BrowseBean.getColorsString() %>],
						minLength: 0
						}).on('focus', function(event) {
							var self = this;
							$(self).autocomplete("search", "");
						});
					</script>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
							<%= loginBean.translate("Set chat input text prompt") %> (default is 'You say')
							</p>
						</div>
					</span>			
					<%= loginBean.translate("Prompt") %><br/>
					<input type="text" name="prompt" value="<%= Utils.escapeHTML(bean.getPrompt()) %>" title="<%= loginBean.translate("Set chat input text prompt") %> (default is 'You say')" /><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Display the chat log") %>
							</p>
						</div>
					</span>
					<input name="chatlog" type="checkbox" title="<%= loginBean.translate("Display the chat log") %>"
						<% if (bean.getChatLog()) { %>checked<% } %>><%= loginBean.translate("Chat Log") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Display the online users") %>
							</p>
						</div>
					</span>
					<input name="online" type="checkbox" title="<%= loginBean.translate("Display the online users") %>"
						<% if (bean.getOnline()) { %>checked<% } %>><%= loginBean.translate("Online Users") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Display the last chat message in a chat bubble") %>
							</p>
						</div>
					</span>
					<input name="bubble" type="checkbox" title="<%= loginBean.translate("Display the last chat message in a chat bubble") %>"
						<% if (bean.getBubble()) { %>checked<% } %>><%= loginBean.translate("Chat Bubble") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Choose if login banner should be displayed") %>
							</p>
						</div>
					</span>
					<input name="loginBanner" type="checkbox" title="<%= loginBean.translate("Choose if login banner should be displayed") %>"
							<% if (bean.getLoginBanner()) { %>checked<% } %>><%= loginBean.translate("Login Banner") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Choose if Facebook login option should be provided") %>
							</p>
						</div>
					</span>
					<input name="facebookLogin" type="checkbox" title="<%= loginBean.translate("Choose if Facebook login option should be provided") %>"
							<% if (bean.getFacebookLogin()) { %>checked<% } %>><%= loginBean.translate("Facebook Login") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Choose if channel's name should be displayed as the title header") %>
							</p>
						</div>
					</span>
					<input name="showTitle" type="checkbox" title="<%= loginBean.translate("Choose if channel's name should be displayed as the title header") %>"
							<% if (bean.getShowTitle()) { %>checked<% } %>><%= loginBean.translate("Show Title") %></input><br/>
					<% if (!Site.COMMERCIAL) { %>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg">
							<div>
								<p class="help">
									<%= loginBean.translate("Choose if a backlink to") %>  <%= Site.NAME %> <%= loginBean.translate("should be displayed (requires Platinum account to remove)") %>
								</p>
							</div>
						</span>
						<input name="showLink" type="checkbox" title="<%= loginBean.translate("Choose if a backlink to") %>  <%= Site.NAME %> <%= loginBean.translate("should be displayed (requires Platinum account to remove)") %>"
								<% if (bean.getShowLink()) { %>checked<% } %>><%= loginBean.translate("Backlink") %></input><br/>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg">
							<div>
								<p class="help">
									<%= loginBean.translate("Choose if the channel's ad should be displayed") %>
								</p>
							</div>
						</span>
						<input name="showAds" type="checkbox" title="<%= loginBean.translate("Choose if the channel's ad should be displayed") %>"
								<% if (bean.getShowAds()) { %>checked<% } %>><%= loginBean.translate("Show Ads") %></input><br/>
					<% } %>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Show the menu bar") %>
							</p>
						</div>
					</span>
					<input name="showMenubar" type="checkbox" title="<%= loginBean.translate("Show Menu Bar") %>"
						<% if (bean.getShowMenubar()) { %>checked<% } %>><%= loginBean.translate("Show Menu Bar") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Show maximize button (popup) in box chat") %>
							</p>
						</div>
					</span>
					<input name="showBoxmax" type="checkbox" title="<%= loginBean.translate("Show maximize button (popup) in box chat") %>"
						<% if (bean.getBoxmax()) { %>checked<% } %>><%= loginBean.translate("Show Max Button") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Show upload image and file menu buttons") %>
							</p>
						</div>
					</span>
					<input name="showSendImage" type="checkbox" title="<%= loginBean.translate("Show upload image and file menu buttons") %>"
						<% if (bean.getSendImage()) { %>checked<% } %>><%= loginBean.translate("Show Send Image") %></input><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg">
						<div>
							<p class="help">
								<%= loginBean.translate("Show 'Send Email Chat Log' menu button") %>
							</p>
						</div>
					</span>
					<input name="showEmailChatLog" type="checkbox" title="<%= loginBean.translate("Show 'Send Email Chat Log' menu button") %>"
						<% if (bean.getEmailChatLog()) { %>checked<% } %>><%= loginBean.translate("Show Email Chat Log") %></input><br/>
				</div>
				<script>
					if (<%= bean.getAdvancedInfo() %>) {
						$('#advancedInfo').show();
					} else {
						$('#advancedInfo').hide();
					}
				</script>
			</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% loginBean.setHelp(help); %>
<% proxy.clear(); %>
</body>
</html>
