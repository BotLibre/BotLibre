<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Invalid Domain - <%= Site.NAME %></title>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="about">
				
			<h1>Invalid Domain</h1>
			<p style="color:#E00000;">You have used an unregistered domain or subdomain to access this website.  Please register your domain with your content.</p>

		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>