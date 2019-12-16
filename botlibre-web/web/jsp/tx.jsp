<%@page import="org.botlibre.web.bean.TransactionBean"%>
<%@page import="org.botlibre.web.admin.Payment"%>
<%@page import="org.botlibre.web.admin.UserPayment"%>
<%@page import="org.eclipse.persistence.sessions.factories.SessionManager"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.service.BeanManager"%>
<%@page import="org.botlibre.web.service.InstanceManager"%>
<%@page import="org.botlibre.web.service.BotManager"%>
<%@page import="org.botlibre.Bot"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.botlibre.web.admin.ClientType"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page import="org.botlibre.web.bean.LoginBean.Page" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% TransactionBean bean = loginBean.getBean(TransactionBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Transactions - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
</head>
<body>
	<script>
	$(document).ready(function() { 
		$("#user").tablesorter({widgets: ['zebra']});
		$("#domaintx").tablesorter({widgets: ['zebra']});
	});
	</script>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="about">
				<h1><%= loginBean.translate("Transactions")%></h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuperUser()) { %>
				  <%= loginBean.translate("Must be sys admin") %>
				<% } else if (!Site.COMMERCIAL) { %>
					<h3><%= loginBean.translate("User Upgrades") %></h3>
					<%= bean.searchFormUserPaymentHTML() %>
					<%= bean.searchUserPaymentHTML() %>
				<% } else { %>
					<h3><%= loginBean.translate("Account Payment") %></h3>
					<%= bean.searchFormPaymentHTML() %>
					<%= bean.searchPaymentHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>