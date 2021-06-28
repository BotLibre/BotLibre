<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.TimersBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% TimersBean bean = loginBean.getBean(TimersBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Timers") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Setup automatic timer scripts") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("timer, scripts, automation") %>"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The Timers tab allows you to setup your bot to run scripts at various time intervals.") %>
						<%= loginBean.translate("This can be used to automate tasks such as polling websites or services for new content, and processing the content.") %>
					</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-timers.jsp"><%= loginBean.translate("Docs") %></a> : <a target="_blank" href="https://www.botlibre.com/forum-post?id=17076300"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/timers.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Setup your bot to run scripts at various time intervals to automate web tasks.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Timers") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The Timers tab allows you to setup your bot to run scripts at various time intervals.") %>
					<%= loginBean.translate("This can be used to automate tasks such as polling websites or services for new content, and processing the content.") %>
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					<%= loginBean.translate("The Timers tab allows you to setup your bot to run scripts at various time intervals.") %>
					<%= loginBean.translate("This can be used to automate tasks such as polling websites or services for new content, and processing the content.") %>
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, hacking, illegal activities, or to violate the terms of service of our website, or any other website.") %>
				</p>
				<form action="timers" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<input name="enableTimers" type="checkbox" <% if (bean.getEnableTimers()) { %>checked<% } %> /><%= loginBean.translate("Enable Timers") %><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("The number of hours to wait between timers.") %>
						</div>
					</span>
					<%= loginBean.translate("Timer Interval (hours)") %><br/>
					<input type="number" name="timerHours" value="<%= bean.getTimerHours() %>"/><br/>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set of timer messages.") %><br/>
							<%= loginBean.translate("List each message separated by a new line.") %><br/>
							<%= loginBean.translate("Ensure your bot defines a scripted response for the message.") %><br/>
							i.e.
<pre>check stock prices
post rss feeds to forums
send mailing list
</pre>
						</div>
					</span>
					<%= loginBean.translate("Timer Scripts") %><br/>
					<textarea name="timers"><%= bean.getTimers() %></textarea><br/>
					<input type="submit" name="save" value="Save"/>
					<input type="submit" name="check" value="Run Timers"/><br/>
					<br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
