<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.service.Stats"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% Stats.page(request); %>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	loginBean.checkSessions(request);
	loginBean.checkMobile(request);
	boolean isProxy = proxy.isProxy();
%>
	<meta charset="UTF-8">
	<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
	<meta name="google-translate-customization" content="3bedbc8a7b114cf5-6f0956cba2f8fe0f-g919b425718ae0e0e-10"></meta>
	<% if (isProxy) { %>
		<meta NAME="ROBOTS" CONTENT="NOINDEX, NOFOLLOW">
	<% } %>
	<link rel="image_src" href="<%= loginBean.http() %>://<%= Site.URL %>/images/logo.png">
	<meta property="og:image" content="<%= loginBean.http() %>://<%= Site.URL %>/images/logo.png" />
	<link rel="shortcut icon" href="favicon.ico">
	
	<link href="https://fonts.googleapis.com/css?family=Montserrat:400,700|Open+Sans:400,300,700,800" rel="stylesheet" media="screen">
	<link rel="stylesheet" href="scripts/fontawesome/css/all.css">
	<link href="css/bootstrap.min.css" rel="stylesheet" media="screen">
	<link href="css/mainstyle.css" rel="stylesheet" media="screen">
	
	<link rel="stylesheet" href="css/commonstyle.css" type="text/css">
	<link rel="stylesheet" href="css/extensions.css" type="text/css">
	
	<% if (loginBean.isMobile()) { %>
		<!--meta name="viewport" content="width=320, initial-scale=1"-->
		<link rel="stylesheet" href="css/mstyle.css" type="text/css">
	<% } else if (loginBean.isFullScreen() || loginBean.isEmbedded()) { %>
		<link rel="stylesheet" href="css/fstyle.css" type="text/css">
	<% } else { %>
		<link rel="stylesheet" href="css/style.css" type="text/css">
	<% } %>
	<script type="text/javascript" src="scripts/sdk.js"></script>
	

<%= loginBean.getJQueryHeader() %>
<!--script src="js/jquery.js"></script-->
