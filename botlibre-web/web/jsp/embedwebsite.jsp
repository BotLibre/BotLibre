<%@page import="org.botlibre.web.admin.AccessMode"%>
<%@page import="org.botlibre.web.bean.EmbedTabBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% EmbedTabBean bean = loginBean.getBean(EmbedTabBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Embed on Website - <%= Site.NAME %></title>
</head>
<body style="margin:0;overflow:hidden;">
	<%= bean.getCode() %>
	<iframe src="<%= bean.getWebsite() %>" frameborder="0" style="position:absolute;top:0;width:100%;left:0;height:100%"></<iframe src="">>
</body>
</html>
