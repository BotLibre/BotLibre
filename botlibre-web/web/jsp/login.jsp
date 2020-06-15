<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.admin.Domain"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean domainBean = loginBean.getBean(DomainBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Sign In<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Sign in to your user account") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("login, connect, signin, user, account") %>"/>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>	
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="section">
<% } %>
	<% boolean error = loginBean.getError() != null; %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<h1><img class="title" src="images/login.svg"> <%= loginBean.translate("Sign In") %></h1>
		<% if (Site.ALLOW_SIGNUP) { %>
			<p>
				<%= loginBean.translate("New users can create a new account") %> <a href="<%= "login?sign-up=true" + proxy.proxyString() %>"><%= loginBean.translate("here") %></a><%= Site.COMMERCIAL ? "" : loginBean.translate(", it is free") %>.
			</p>
		<% } %>
		
		<% if (loginBean.getFacebookLogin()) { %>
			<script>
				function statusChangeCallback(response) {
					if (response.status === 'connected') {
						if (response.authResponse != null) {
							document.getElementById("credentials-type").value = "Facebook";
							document.getElementById("credentials-userid").value = response.authResponse.userID;
							document.getElementById("credentials-token").value = response.authResponse.accessToken;
							document.getElementById('ok').click();
						}
					} else if (response.status === 'not_authorized') {
						document.getElementById('status').innerHTML = 'Please log '
								+ 'into this app.';
					} else {
						document.getElementById('status').innerHTML = 'Please log '
								+ 'into Facebook.';
					}
				}
			
				window.fbAsyncInit = function() {
					FB.init({
						appId : '<%= Site.FACEBOOK_APPID %>',
						cookie : true,
						xfbml : true,
						version : 'v2.2'
					});
				};
			
				// Load the SDK asynchronously
				(function(d, s, id) {
					var js, fjs = d.getElementsByTagName(s)[0];
					if (d.getElementById(id))
						return;
					js = d.createElement(s);
					js.id = id;
					js.src = "//connect.facebook.net/en_US/sdk.js";
					fjs.parentNode.insertBefore(js, fjs);
				}(document, 'script', 'facebook-jssdk'));
	
				
				function checkLoginState() {
					FB.getLoginStatus(function(response) {
						statusChangeCallback(response);
					});
				}
	
				function facebookLogin() {
					FB.login(function(response) {
						statusChangeCallback(response);
					}, {
						scope : 'public_profile,email'
					});
				}
			</script>
			
			<!--fb:login-button scope="public_profile" onlogin="checkLoginState();"></fb:login-button-->
			<a href="#" class="facebookbutton" onClick="facebookLogin(); return false;"><img style="vertical-align:middle" src="images/facebook2.svg"> <%= loginBean.translate("Sign in using Facebook") %></a>
			<br/>
			<div id="status">
			</div>
		<% } %>
		<br/>
		
		<% if (!error || loginBean.getEditUser() == null) { %>
			<form id="login" action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input id="credentials-type" name="credentials-type" type="hidden" />
				<input id="credentials-userid" name="credentials-userid" type="hidden" />
				<input id="credentials-token" name="credentials-token" type="hidden" />
				<%= loginBean.translate("User ID") %> (<%= loginBean.translate("or email") %>)<br/>
				<input autofocus name="user" type="text" /><br/>
				<%= loginBean.translate("Password") %><br/>
				<input name="password" type="password" /><br/>
				<input name="remember" type="checkbox"><%= loginBean.translate("remember me") %></input><br/>
				<input id="ok" name="connect" type="submit" value="<%= loginBean.translate("Sign In") %>"/>
			</form>
		<% } else { %>
			<form id="login" action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input id="credentials-type" name="credentials-type" type="hidden" />
				<input id="credentials-userid" name="credentials-userid" type="hidden" />
				<input id="credentials-token" name="credentials-token" type="hidden" />
				<%= loginBean.translate("User ID (or email)") %><br/>
				<input autofocus name="user" type="text" value="<%= loginBean.getEditUser().getUserId() %>" /><br/>
				<%= loginBean.translate("Password") %><br/>
				<input name="password" type="password" value="<%= loginBean.getEditUser().getPassword() %>" /><br/>
				<input name="remember" type="checkbox"><%= loginBean.translate("remember me") %></input><br/>
				<input id="ok" name="connect" type="submit" value="<%= loginBean.translate("Sign In") %>"/>
			</form>
		<% } %>
		<br/>
		<span><a href="<%= "login?request-reset-password"	+ proxy.proxyString() %>"><%= loginBean.translate("Request password reset") %></a></span>
		<br/>
		<% if (!embed && !loginBean.isHttps() && Site.HTTPS) { %>
			<p><%= loginBean.translate("Switch to") %> <a style="color:#00BB00;font-weight:bold" href="<%= Site.SECUREURLLINK %>">https</a></p>
		<% } %>
	
	<% } else { %>
		<h1><%= loginBean.translate("Welcome") %></h1>
		<p>
			<%= loginBean.translate("Welcome") %>
			<a href="<%= "login?view-user=" + loginBean.encodeURI(loginBean.getUser().getUserId()) + proxy.proxyString() %>">
			<%= loginBean.getUser().getUserId() %></a>
			<%= loginBean.translate("you have") %> <%= loginBean.getUser().getConnects() %> <%= loginBean.translate("connects") %>,
			<%= loginBean.translate("and last connected on") %> <%= loginBean.getUser().getOldLastConnected() %><br/>					
			<% int newMessages = loginBean.getNewMessageCount(); %>
			<% if (newMessages == 1) { %>
				<br/><%= loginBean.translate("You have") %> <%= newMessages %> <%= loginBean.translate("new") %> <a href="<%= "login?browse-user-messages"	+ proxy.proxyString() %>"><%= loginBean.translate("message") %></a>.
			<% } else if (newMessages > 1) { %>
				<br/><%= loginBean.translate("You have") %> <%= newMessages %> <%= loginBean.translate("new") %> <a href="<%= "login?browse-user-messages"	+ proxy.proxyString() %>"><%= loginBean.translate("messages") %></a>.
			<% } %>
			<% if (!Site.COMMERCIAL && !Site.DEDICATED && loginBean.getUser().isExpired()) { %>
				<b style="color:red"><%= loginBean.translate("Your account has expired please renew your upgrade") %> <a href="upgrade.jsp"><%= loginBean.translate("here") %></a></b><br/>
			<% } %>
			<% if (!loginBean.getUser().isVerified()) { %>
				<b style="color:red"><%= loginBean.translate("Please verify your email address") %> 
				<a href="<%= "login?send-verify" + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Send a verification email to your email address") %>"><%= loginBean.translate("resend verify") %></a></b><br/>
			<% } %>
			<% if (!Site.COMMERCIAL && !Site.DEDICATED) { %>
				<br/>
				<b><%= loginBean.translate("Are you using Bot Libre for Business? We strongly recommend you upgrade to Bot Libre for Business") %> <a href="https://www.botlibre.biz"><%= loginBean.translate("www.botlibre.biz") %></a></b><br/>
			<% } %>
			<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && !embed) { %>
				<form action="domain" method="get">
					<% List<Domain> domains = domainBean.getUserInstances(); %>
					<% if (!domains.isEmpty()) { %>
						<%= loginBean.translate("Workspaces") %>
						<select id="domain" name="domain" onchange="this.form.submit()">
							<% for (Domain domain : domains) { %>
								<option value="<%= domain.getId() %>" <%= domainBean.getDomainSelected(domain.getId().toString()) %>><%= domain.getName() %></option>
							<% } %>
						</select> <a href="domain?details"><%= loginBean.translate("Details") %></a> : <a href="domain?details"><%= loginBean.translate("Make Payment") %></a> : <a href="create-domain.jsp"><%= loginBean.translate("Create New Workspace") %></a>
					<% } else { %>
						<a href="create-domain.jsp"><%= loginBean.translate("Create Workspace") %></a>
					<% } %>
				</form>
			<% } %>
			<br/>
			<a href="browse?my-instances=My+Bots"><%= loginBean.translate("My Bots") %></a><br/>
			<a href="browse?create-instance=New+Bot"><%= loginBean.translate("Create New Bot") %></a><br/>
			<% if (loginBean.getRedirect() != null) { %>
				<br/>
				<a href="<%= loginBean.getRedirect() %>"><%= loginBean.translate("Return") %></a>
			<% } %>
			<form action="login" method="get" class="message">
				<%= proxy.proxyInput() %>
				<input name="user-details" type="submit" value="<%= loginBean.translate("User Details") %>"/>
				<input id="cancel" name="logout" type="submit" value="<%= loginBean.translate("Sign Out") %>"/>
			</form>
			<% if (loginBean.isSuper()) { %>
				<h2>Sys Admin Pages</h2>
				<a href="super.jsp">Admin Console</a> : 
				<a href="stats.jsp">Stats</a> : 
				<a href="browse-user.jsp">All Users</a> :
				<a href="tx.jsp">All Payments</a><br/><br/>
			<% } %>
		</p>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
<% } %>
<% proxy.clear(); %>
</body>
</html>