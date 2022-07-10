<%@page import="org.botlibre.web.Site"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Register - <%= Site.NAME %></title>
	<meta name="description" content="Register info"/>	
	<meta name="keywords" content="Register, info, email, phone, twitter, facebook, support"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody" style="padding: 20px; max-width: 900px; margin: auto;">
	<jsp:include page="error.jsp"/>
	<%@ include file="newsletter-form.jsp" %>
	</div>		
	<jsp:include page="footer.jsp"/>
</body>
</html>