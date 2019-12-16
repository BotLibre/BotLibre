<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.service.AppIDStats"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>AppID Stats - <%= Site.NAME %></title>
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
				<h1>AppID Stats - <%= Utils.sanitize(loginBean.getSelectedStats()) %></h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
				  Must be sys admin
				<% } else { %>
					<table id="stats" class="tablesorter">
						<thead>
						<tr>
							<th>Date</th>
							<th>App User Id</th>
							<th>API Calls</th>
							<th>Over Limit</th>
							<th>User Creates</th>
							<th>Bot Creates</th>
						</tr>
						</thead>
						<tbody>
						<% for (AppIDStats stat : AdminDatabase.instance().getAllAppIDStats(loginBean.getSelectedStats())) { %>
							<tr>
								<td><%= stat.date %></td>
								<td><a href="login?view-user=<%= stat.userId %>"><%= Utils.sanitize(stat.userId) %></a></td>
								<td><%= stat.apiCalls %></td>
								<td><%= stat.overLimit %></td>
								<td><%= stat.userCreates %></td>
								<td><%= stat.botCreates %></td>
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