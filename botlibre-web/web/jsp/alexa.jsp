<%@page import="org.botlibre.web.bean.AlexaBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% AlexaBean bean = loginBean.getBean(AlexaBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Alexa - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Alexa"/>	
	<meta name="keywords" content="alexa, mobile, texting, bot, alexa bot, alexa skill, amazon"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Alexa tab allows you to connect your bot to Alexa.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-alexa.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=23305700"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/alexa1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to connect with Alexa.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Alexa") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					The Alexa tab allows you to connect your bot to Alexa.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					The Alexa tab allows you to connect your bot to an Alexa Skill.
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					Connect your bot to an <a href="https://developer.amazon.com/alexa" target="_blank">Alexa</a> account.
				</p>
				<h3><%= loginBean.translate("Alexa Properties") %></h3>
				<p>
					<%= loginBean.translate("Register your 'Alexa Skill Endpoint URL' on the Endpoint settings page on the Alexa Skills dashboard.") %>
				</p>
				
				<form action="alexa" method="post" class="message">
				
				<span class="dropt-banner">
					<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("Enter this URL into the Endpoint settings page of the Alexa Skills dashboard.") %>	
					</div>
				</span>
					Alexa Skill Endpoint URL<br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
				</form>
				<form action="alexa" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					
					<h3><%= loginBean.translate("Built-In Intent Responses") %></h3>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the text Alexa will say when launching your skill.") %>
						</div>
					</span>
					<%= loginBean.translate("Alexa Launch Response") %><br/>
					<input type="text" name="launchResponse" value="<%= bean.getLaunchResponse() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the text Alexa will say when the user asks for help.") %>
						</div>
					</span>
					<%= loginBean.translate("Alexa Help Response") %><br/>
					<input type="text" name="helpResponse" value="<%= bean.getHelpResponse() %>"/><br/>
						
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the text Alexa will say when the user cancels interacting with your skill.") %>
						</div>
					</span>
					<%= loginBean.translate("Alexa Cancel Response") %><br/>
					<input type="text" name="cancelResponse" value="<%= bean.getCancelResponse() %>"/><br/>
						
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the text Alexa will say when the user stops interacting with your skill.") %>
						</div>
					</span>
					<%= loginBean.translate("Alexa Stop Response") %><br/>
					<input type="text" name="stopResponse" value="<%= bean.getStopResponse() %>"/><br/>
						
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the text Alexa will say when it does not understand the user's command.") %>
						</div>
					</span>
					<%= loginBean.translate("Alexa Fallback Response") %><br/>
					<input type="text" name="fallbackResponse" value="<%= bean.getFallbackResponse() %>"/><br/>
						
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("If set, will append prompt to any response that does not contain '?' or HTML.") %>
						</div>
					</span>
					<%= loginBean.translate("Alexa Followup Prompt") %><br/>
					<input type="text" name="followupPrompt" value="<%= bean.getFollowupPrompt() %>"/><br/>
						
					<h3><%= loginBean.translate("End Conversation") %></h3>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enable to end session if bot's response does not contain '?' or HTML.") %>
						</div>
					</span>
					<input name="autoExit" type="checkbox" <% if (bean.getAutoExit()) { %>checked<% } %> />Auto Exit<br/>
					
					<br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							Set phrases/words that will end the chat session.<br/>
							Each phrase must be separated by a new line.<br/>
						</div>
					</span>
					<%= loginBean.translate("End Conversation Phrases") %><br/>
					<textarea name="stopPhrases"><%= bean.getStopPhrases() %></textarea><br/>
					
					<input type="submit" name="save" value="Save"/> 
					<input type="submit" name="default" value="Default" title="Set responses to default."/>
					<br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
