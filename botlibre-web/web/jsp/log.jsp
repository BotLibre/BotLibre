<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.admin.ErrorMessage"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Log - <%= Site.NAME %></title>
	<meta name="description" content="The log tab allows you monitor and debug your bot's processing"/>	
	<meta name="keywords" content="log, debug, console, output, monitor, bot"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
			  		<%= loginBean.translate("The log tab allows you monitor and debug your bot's processing.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-log.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/log.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("View the bot's log for errors and debugging info.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Log") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
					<%= botBean.getNotConnectedMessage() %>
				<% } else if (!botBean.isAdmin()) { %>
					<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<form action="log" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					Level 
		 			<select name="level"  onchange="this.form.submit()">
						<option value="OFF" <%= botBean.isLogLevelSelected("OFF") %>><%= loginBean.translate("OFF") %></option>
						<option value="SEVERE" <%= botBean.isLogLevelSelected("SEVERE") %>><%= loginBean.translate("SEVERE") %></option>
						<option value="WARNING" <%= botBean.isLogLevelSelected("WARNING") %>><%= loginBean.translate("WARNING") %></option>
						<option value="CONFIG" <%= botBean.isLogLevelSelected("CONFIG") %>><%= loginBean.translate("CONFIG") %></option>
						<option value="INFO" <%= botBean.isLogLevelSelected("INFO") %>><%= loginBean.translate("INFO") %></option>
						<option value="FINE" <%= botBean.isLogLevelSelected("FINE") %>><%= loginBean.translate("FINE") %></option>
						<option value="FINER" <%= botBean.isLogLevelSelected("FINER") %>><%= loginBean.translate("FINER") %></option>
						<% if (botBean.isSuper()) { %>
							<option value="FINEST" <%= botBean.isLogLevelSelected("FINEST") %>><%= loginBean.translate("FINEST") %></option>
							<option value="ALL" <%= botBean.isLogLevelSelected("ALL") %>><%= loginBean.translate("ALL") %></option>
						<% } %>
					</select><br/>
					<% if (!botBean.getInstance().getErrors().isEmpty()) { %>
						<h3><%= loginBean.translate("Warnings") %></h3>
					<input type="submit" name="clearWarnings" value="<%= loginBean.translate("Clear") %>"/>
						<pre id="log"><code>
<% for (ErrorMessage message : botBean.getInstance().getErrors()) { %><%= Utils.escapeHTML(message.getMessage()) %><br/><% } %>
						</code></pre>
						<h3><%= loginBean.translate("Log") %></h3>
					<% } %>
					<input type="submit" name="clear" value="<%= loginBean.translate("Clear") %>"/>
					<pre id="log"><code><%= Utils.escapeHTML(botBean.getLog()) %></code></pre>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
