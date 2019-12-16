<%@page import="org.botlibre.web.admin.Domain"%>
<%@page import="org.botlibre.web.admin.Payment"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import = "org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
try {
	boolean databaseFailure = false;
	DomainBean domainBean = null;
	try {
		AdminDatabase.instance().getFactory();
		loginBean = proxy.checkLoginBean(loginBean);
		if (loginBean.checkForward(request, response)) {
			return;
		}
		domainBean = loginBean.getBean(DomainBean.class);
		loginBean.initialize(getServletContext(), request, response); // Need to init in root page for cookie.
	} catch (Exception exception) {
		// Detect the first start failure and prompt for the database password.
		if (Site.BOOTSTRAP && AdminDatabase.DATABASEFAILURE) {
			loginBean.setError(exception);
			databaseFailure = true;
			loginBean.setBootstrap(true);
		}
	}
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= Site.NAME %> - Commercial virtual agent, live chat, chat bot, forum, and chatroom hosting platform</title>
	<meta name="description" content="Professional bot hosting and development platform for the web, mobile, and social media"/>	
	<meta name="keywords" content="chat bot, open source, create chat bot, create your own chat bot, chat, bot, commercial, hosting, embed, artificial intelligence, ai, create, twitterbot, twitter, facebook, ircbot, irc, chatterbot, chatbot"/>

	<link href="css/simpletextrotator.css" rel="stylesheet" media="screen">
	<link href="css/overwrite.css" rel="stylesheet" media="screen">
	<link href="css/animate.css" rel="stylesheet" media="screen">
	


</head>

<% if (databaseFailure) { %>

	<body style="background-color: #fff;">
		<div class="center">
			<h1><%= loginBean.translate("Welcome to") %> <%= Site.NAME %></h1>
			<jsp:include page="error.jsp"/>
			<p>
				<%= loginBean.translate("There was a password failure trying to access your database.") %><br/>
				<%= loginBean.translate("Please enter your database postgres user password below.") %>
			</p>
			<p>
				<%= loginBean.translate("If this is your first time starting this platform you will next need to sign in as:") %><br/>
				<%= loginBean.translate("user") %>: admin, <%= loginBean.translate("password") %>: password<br/>
				<%= loginBean.translate("After signing in click on 'Admin Console' to configure your platform settings.") %><br/>
			</p>
			<form action="super" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<p>
				<%= loginBean.translate("Database Password") %><br/>
				<input type="password" name="database-password"/>
				<br/>
				<input type="submit" id="ok" name="bootstrap" value="<%= loginBean.translate("Connect") %>"/>
				</p>
			</form>
		</div>

<% } else if (embed) { %>

	<body style="background-color: #fff;">
	<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>

<% } else { %>

<body class="mainpage">
	<% loginBean.setPageType(Page.Home); %>
	<jsp:include page="banner.jsp"/>

	<jsp:include page="error.jsp"/>

	<!-- HOME -->
	<div id="intro">
		<div class="overlay">
			<div class="intro-text">
			 
				<div class="container" style="padding-bottom:150px">
					<% if (domainBean.hasValidInstance()) { %>
						<div class="col-md-12">
							<div id="rotator">
								<a href="domain?details=true&id=<%= loginBean.getDomain().getId() %>"><h1><span style="color: #fff"><%= loginBean.getDomain().getName() %></span></h1></a>
								<div class="line-spacer"></div>
								<p><span><%= loginBean.getDomain().getDescriptionText() %></span></p>
								<% if (domainBean.isAdmin() && Site.COMMERCIAL && !Site.DEDICATED) { %>
									<p>
										<a href="domain?details=true" style="text-decoration: none;">
											<span class="menu"><%= loginBean.translate("View details or make a payment") %></span>
										</a>
									</p>
								<% } %>
							</div>
							<br>
							<% if (!loginBean.isLoggedIn() && Site.ALLOW_SIGNUP) { %>
								<span> <a href="login?sign-up=true" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign Up") %>&rarr;</a></span>
							<% } %>
							<span> <a href="browse?browse-type=Bot&browse=true<%= domainBean.domainURL() %>" class="btn btn-3 wow fadeInUp"><%= loginBean.translate("Browse") %>&rarr;</a></span>
							<% if (loginBean.isLoggedIn() && loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
								<span> <a href="browse?browse-type=Bot&create=true" class="btn btn-4 wow fadeInUp"><%= loginBean.translate("Create") %>&rarr;</a></span>
							<% } %>
						</div>
					<% } else if (Site.DEDICATED) { %>
						<div class="col-md-12">
							<div id="rotator">
								<% if (domainBean.isAdmin()) { %>
									<a href="domain?details=true&id=<%= loginBean.getDomain().getId() %>"><h1><span class="1strotate" style="color: #fff"><%= Site.NAME %></span></h1></a>
								<% } else { %>
									<h1><span style="color: #fff"><%= Site.NAME %></span></h1>
								<% } %>
								<div class="line-spacer"></div>
								<p><span><%= loginBean.getDomain().getDescriptionText() %></span></p>
							</div>
							<br>
							<% if (!loginBean.isLoggedIn()) { %>
								<span> <a href="login?sign-in=true" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign In") %>&rarr;</a></span>
							<% } %>
							<% if (!loginBean.isLoggedIn() && Site.ALLOW_SIGNUP) { %>
								<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD)) { %>
									<span> <a href="create-domain.jsp" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign Up") %>&rarr;</a></span>
								<% } else { %>
									<span> <a href="login?sign-up=true" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign Up") %>&rarr;</a></span>
								<% } %>
							<% } %>
							<% if (loginBean.isLoggedIn()) { %>
								<span> <a href="browse?browse-type=Bot&browse=true<%= domainBean.domainURL() %>" class="btn btn-3 wow fadeInUp"><%= loginBean.translate("Browse") %>&rarr;</a></span>
							<% } %>
							<% if (loginBean.isLoggedIn() && loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
								<span> <a href="browse?browse-type=Bot&create=true" class="btn btn-4 wow fadeInUp"><%= loginBean.translate("Create") %>&rarr;</a></span>
							<% } %>
						</div>
					<% } else { %>
						
					<% } %>
				</div>
			</div>
		</div>
	</div>
	
	<% if (!domainBean.hasValidInstance() && !Site.DEDICATED) { %>



	<% } %>
	
	<jsp:include page="footer.jsp"/>
	
	<% if (!domainBean.hasValidInstance() && loginBean.getGreet() && !loginBean.isLoggedIn() && !loginBean.isMobile()) { %>
		<% loginBean.setGreet(false); %>
	<% } %>
	
	<!-- js -->
	<script src="js/bootstrap.min.js"></script>
	<script src="js/wow.min.js"></script>
	<script src="js/mb.bgndGallery.js"></script>
	<script src="js/mb.bgndGallery.effects.js"></script>
	<script src="js/jquery.simple-text-rotator.min.js"></script>
	<script src="js/jquery.scrollTo.min.js"></script>
	<script src="js/jquery.nav.js"></script>
	<script src="js/modernizr.custom.js"></script>
	<script src="js/grid.js"></script>
	<script src="js/stellar.js"></script>

	<!- Custom Javascript -->
	<script src="js/custom.js"></script>
	<script src="js/videoplay.js"></script>

<% } %>
<% proxy.clear(); %>
<% } catch (Exception exception) { AdminDatabase.instance().log(exception); }%>

</body>
</html>