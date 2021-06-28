<%@page import="org.botlibre.web.bean.WeChatBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% WeChatBean bean = loginBean.getBean(WeChatBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>WeChat - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to WeChat"/>	
	<meta name="keywords" content="mobile, texting, bot, wechat"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
				  		<%= loginBean.translate("The WeChat tab allows you to connect your bot to WeChat.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-wechat.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=18979672"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/wechat1.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to WeChat messages.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("WeChat") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					The WeChat tab allows you to connect your bot to WeChat.
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					The WeChat tab allows you to connect your bot to WeChat.
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					Connect your bot to a <a href="https://www.wechat.com/" target="_blank">WeChat</a> account.
				</p>
				<h3><%= loginBean.translate("WeChat Properties") %></h3>
				
				<form action="wechat" method="post" class="message">
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter this URL on the WeChat Developer Center to enable replying to messages on WeChat.") %>	
						</div>
					</span>
					WeChat Messaging Endpoint URL<br/>
					<input type="text" name="webhook" value="<%= bean.getWebhook() %>" /><br/>
				</form>
				
				<form action="wechat" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter the app id from the WeChat Developer Center.") %>
						</div>
					</span>
					<%= loginBean.translate("WeChat App Id") %><br/>
					<input type="text" name="appId" value="<%= bean.getAppId() %>"/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter the app secret from the WeChat Developer Center") %>
						</div>
					</span>
					<%= loginBean.translate("WeChat App Secret") %><br/>
					<input type="text" name="appPassword" value="<%= bean.getAppPassword() %>"/><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter the token from the WeChat Developer Center") %>
						</div>
					</span>
					<%= loginBean.translate("Token") %><br/>
					<input type="text" name="userToken" value="<%= bean.getUserToken() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Check this box if using International Account.") %><br/>
						</div>
					</span>
					<input name="accountType" type="radio" value="international" <% if (bean.getInternational()) { %>checked<% } %> /><%= loginBean.translate("International Account") %><br/>
						
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Check this box if using China Account") %><br/>
						</div>
					</span>
					<input name="accountType" type="radio" value="china" <% if (!bean.getInternational()) { %>checked<% } %> /><%= loginBean.translate("China Account") %><br/>
					
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Enter menu JSON: See http://admin.wechat.com/wiki/index.php?title=Create") %>
						</div>
					</span>
					<%= loginBean.translate("Menu") %><br/>
					<!-- <input type="text" name="menu" value="<%= bean.getMenu() %>"/><br/> -->
					<textarea name="menu"><%= bean.getMenu() %></textarea><br/>
					
					<!-- <input type="submit" name="check" value="Check Status" title="Have the bot check WeChat"/> -->
					<input type="submit" name="save" value="Save"/><br/>
					<br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
