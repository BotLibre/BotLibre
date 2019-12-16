<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.service.IPStats"%>
<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.service.IPStats"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>IP Stats - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
</head>
<body>
	<script>
	$(document).ready(function() 
	    { 
	        $("#stats").tablesorter({widgets: ['zebra']});
	    } 
	);
	</script>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="about">
				<h1>IP Stats - <%= Utils.sanitize(loginBean.getSelectedStats()) %></h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
				  Must be sys admin
				<% } else { %>
					<table id="stats" class="tablesorter">
						<thead>
						<tr>
							<th>Date</th>
							<th>Sessions</th>
							<th>Pages</th>
							<th>API Calls</th>
							<th>Bad API Calls</th>
							<th>Bot Creates</th>
							<th>Bot Connects</th>
							<th>Bot Chats</th>
							<th>User Messages</th>
							<th>Agent</th>
						</tr>
						</thead>
						<tbody>
						<% for (IPStats stat : AdminDatabase.instance().getAllIPStats(loginBean.getSelectedStats())) { %>
							<tr>
								<td><%= stat.date %></td>
								<td><%= stat.sessions %></td>
								<td><%= stat.pages %></td>
								<td><%= stat.api %></td>
								<td><%= stat.badAPI %></td>
								<td><%= stat.botCreates %></td>
								<td><%= stat.botConnects %></td>
								<td><%= stat.botChats %></td>
								<td><%= stat.userMessages %></td>
								<td><%= Utils.sanitize(stat.agent) %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>