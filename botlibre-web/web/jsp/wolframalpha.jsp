<%@page import="org.botlibre.web.bean.WolframAlphaBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% WolframAlphaBean bean = loginBean.getBean(WolframAlphaBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Wolfram Alpha - <%= Site.NAME %></title>
	<meta name="description" content="Connect your bot to Wolfram Alpha"/>	
	<meta name="keywords" content="wolfram alpha, mobile, texting, bot, wolfram alpha bot, wolfram alpha automation"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Wolfram Alpha tab allows you to connect your bot to Wolfram Alpha.") %><br/>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-wolframalpha.jsp"><%= loginBean.translate("Docs") %></a> : <a target="_blank" href="https://www.botlibre.com/forum-post?id=20099005"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt">
					<img src="images/wolframalpha1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot use Wolfram Alpha to respond to messages.") %><br/>
							<%= loginBean.translate("Wolfram Alpha provides a computational knowledge engine that can compute answers for many industries.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Wolfram Alpha") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The Wolfram Alpha tab allows you to connect your bot to Wolfram Alpha.") %><br/>
					<%= loginBean.translate("Wolfram Alpha provides a computational knowledge engine that can compute answers for many industries.") %><br/>
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					<%= loginBean.translate("The Wolfram Alpha tab allows you to connect your bot to Wolfram Alpha.") %><br/>
					<%= loginBean.translate("Wolfram Alpha provides a computational knowledge engine that can compute answers for many industries.") %><br/>
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please ensure your bot's Wolfram Alpha API usage complies with Wolfram Alpha's ") %><a target="_blank" href="https://products.wolframalpha.com/api/termsofuse.html"><%= loginBean.translate("terms of service") %></a>.
				</p>
				<p>
					Connect your bot to a <a href="https://www.wolframalpha.com/" target="_blank">Wolfram Alpha</a> account.
				</p>
				<h3><%= loginBean.translate("Wolfram Alpha Properties") %></h3>

		  		<form action="wolframalpha" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set the App Id from the Wolfram Alpha website.") %>
						</div>
					</span>
					<%= loginBean.translate("Wolfram Alpha App Id") %><br/>
					<input type="text" name="appId" value="<%= bean.getAppId() %>"/><br/>

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
