<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Sign Up <%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Create a new user account") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("create, user, signup, account, profile, login") %>"/>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
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
	<h1><img class="title" src="images/signup.svg"> <%= loginBean.translate("Sign Up") %></h1>
	<% boolean error = loginBean.getError() != null; %>
	<jsp:include page="error.jsp"/>
	<% if (!Site.ALLOW_SIGNUP && !loginBean.isSuper()) { %>
		Must be sys admin
	<% } else { %>
		<p>
			<%= Site.COMMERCIAL ? "" : loginBean.translate("Signing up is free and easy.") %><br/>
			<%= loginBean.translate("Already registered? Sign in") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("here") %></a>.
		</p>
	
		<% if (loginBean.getFacebookLogin()) { %>
			<script>
				function statusChangeCallback(response) {
					if (response.status === 'connected') {
						connected();
					} else if (response.status === 'not_authorized') {
						document.getElementById('status').innerHTML = 'Please log '
								+ 'into this app.';
					} else {
						document.getElementById('status').innerHTML = 'Please log '
								+ 'into Facebook.';
					}
					if (response.authResponse != null) {
						document.getElementById("credentials-type").value = "Facebook";
						document.getElementById("credentials-userid").value = response.authResponse.userID;
						document.getElementById("credentials-token").value = response.authResponse.accessToken;
						document.getElementById('status').innerHTML = 'Success, '
							+ 'enter profile info and click Create.';
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
	
				function connected() {
					FB.api(
						'/me?fields=id,name,email,website,first_name',
						function(response) {
							console.log(response);
							document.getElementById("create-user").style.display = "none";
							document.getElementById("create-user-fb").style.display = "";
							document.getElementById("name").value = response.name;
							if (response.first_name != null) {
								document.getElementById("user2").value = response.first_name.toLowerCase();
							} else {
								document.getElementById("user2").value = response.name.toLowerCase().replace(" ", "");
							}
							if (response.email != null) {
								document.getElementById("email").value = response.email;
							}
							if (response.website != null) {
								document.getElementById("website").value = response.website;
							}
						});
				}
			
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
		
			<a href="#" class="facebookbutton" onClick="facebookLogin(); return false;"><img style="vertical-align:middle" src="images/facebook2.svg"> <%= loginBean.translate("Sign up using Facebook") %></a>
			<!--fb:login-button scope="public_profile,email,user_about_me,user_website" onlogin="checkLoginState();"></fb:login-button-->
			<br/>
			<div id="status">
			</div>
		<% } %>
		<br/>
	
		<script>
			function validateForm() {
				var user = document.getElementById("user");
				var user2 = document.getElementById("user2");
				if (user.value == "" && (user2 == null || user2.value == "")) {
					user.className = null;
					user.style.border = "2px solid red";
					if (user2 != null) {
						user2.className = null;
						user2.style.border = "2px solid red";
					}
					SDK.showError('<%= loginBean.translate("Enter a user id") %>');
					return false;
				}
				var dateOfBirth = document.getElementById("dateOfBirth");
				var dateOfBirth2 = document.getElementById("dateOfBirth2");
				if (dateOfBirth.value == "" && (dateOfBirth2 == null || dateOfBirth2.value == "")) {
					dateOfBirth.className = null;
					dateOfBirth.style.border = "2px solid red";
					if (dateOfBirth2 != null) {
						dateOfBirth2.className = null;
						dateOfBirth2.style.border = "2px solid red";
					}
					SDK.showError('<%= loginBean.translate("Enter a date of birth") %>');
					return false;
				}
				var password = document.getElementById("password");
				var password2 = document.getElementById("password2");
				if (password.value != null && password.value != "" && password.value.length < 8) {
					password.className = null;
					password.style.border = "2px solid red";
					SDK.showError('<%= loginBean.translate("Passwords must be at least 8 characters") %>');
					return false;
				}
				if (password.value != password2.value) {
					password2.className = null;
					password2.style.border = "2px solid red";
					SDK.showError('<%= loginBean.translate("Passwords do not match") %>');
					return false;
				}
				var terms = document.getElementById("terms");
				var terms2 = document.getElementById("terms2");
				if (!terms.checked && (terms2 == null || !terms2.checked)) {
					terms = document.getElementById("terms-span");
					terms.style.color = "red";
					terms.style.fontWeight = "bold";
					terms = document.getElementById("terms-span2");
					if (terms != null) {
						terms.style.color = "red";
						terms.style.fontWeight = "bold";
					}
					SDK.showError('<%= loginBean.translate("You must accept our terms of service") %>');
					return false;
				}
				return true;
			}
		</script>
		<% if (error && (loginBean.getUser() != null)) { %>
			<form name="create" action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input name="token" type="hidden" value="<%= loginBean.hashCode() %>"/>
				<% if (loginBean.getUser().getCredentialsType() == null) { %>
					<span class="required"><%= loginBean.translate("User Id") %></span><br/>
					<input class="required" autofocus id="user" name="user" type="text" value="<%= loginBean.getUser().getUserId() %>"  title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
					<span class="required"><%= loginBean.translate("Password") %></span><br/>
					<input id="password" class="required" name="password" type="password" value="<%= loginBean.getUser().getPassword() %>"  title="<%= loginBean.translate("Enter a secure password (required)") %>"/><br/>
					<span class="required"><%= loginBean.translate("Retype Password") %></span><br/>
					<input id="password2" class="required" name="password2" type="password" value="<%= loginBean.getUser().getPassword() %>"  title="<%= loginBean.translate("Confirm password (required)") %>"/><br/>
					<% if (Site.AGE_RESTRICT) { %>
						<span class="required"><%= loginBean.translate("Date of Birth") %></span><br/>
						<input class="required" id="dateOfBirth" name="dateOfBirth" type="date" placeholder="yyyy/mm/dd" value="<%= Utils.printSQLDate(loginBean.getUser().getDateOfBirth()) %>"  title="<%= loginBean.translate("Enter your date of birth (yyyy/mm/dd, not visible to other users, required)") %>"/><br/>
					<% } %>
					<%= loginBean.translate("Password hint") %><br/>
					<input name="hint" type="text" value="<%= loginBean.getUser().getHint() %>"  title="<%= loginBean.translate("Enter a personal hint in case you forget your password (optional)") %>"/><br/>
				<% } else { %>
					<span class="required"><%= loginBean.translate("User Id") %></span><br/>
					<input class="required" autofocus name="user" type="text" value="<%= loginBean.getUser().getUserId() %>"  title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
					<input id="credentials-type" name="credentials-type" value="<%= loginBean.getUser().getCredentialsType() %>" type="hidden" />
					<input id="credentials-userid" name="credentials-userid" value="<%= loginBean.getUser().getCredentialsUserID() %>" type="hidden" />
					<input id="credentials-token" name="credentials-token" value="<%= loginBean.getUser().getCredentialsToken() %>" type="hidden" />
				<% } %>
				<% if (Site.COMMERCIAL) { %>
					<span class="required"><%= loginBean.translate("Name") %></span><br/>
					<input class="required" name="name" type="text" value="<%= loginBean.getUser().getName() %>"  title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
					<input name="display-name" type="checkbox" <% if (loginBean.getUser().getShouldDisplayName()) { %>checked="checked"<% } %> title="<%= loginBean.translate("Display your name to other users") %>">Show Name</input><br/>
					<span class="required"><%= loginBean.translate("Email") %></span><br/>
					<input class="required" name="email" type="text" value="<%= loginBean.getUser().getEmail() %>"  title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
				<% } else { %>
					<%= loginBean.translate("Name") %><br/>
					<input name="name" type="text" value="<%= loginBean.getUser().getName() %>"  title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
					<input name="display-name" type="checkbox" <% if (loginBean.getUser().getShouldDisplayName()) { %>checked="checked"<% } %> title="<%= loginBean.translate("Display your name to other users") %>">Show Name</input><br/>
					<%= loginBean.translate("Email") %><br/>
					<input name="email" type="text" value="<%= loginBean.getUser().getEmail() %>"  title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
				<% } %>
				<!-- 
					<%= loginBean.translate("Website") %><br/>
					<input name="website" type="text" value="<%= loginBean.getUser().getWebsite() %>" title="<%= loginBean.translate("Enter your business or personal website (visible to other users, optional)") %>"/><br/>
					<%= loginBean.translate("Bio") %><br/>
					<textarea name="bio"  title="<%= loginBean.translate("Enter anything about you (HTML, visible to other users, optional)") %>"><%= loginBean.getUser().getBio() %></textarea><br/>
				-->
				<input id="terms" name="terms" type="checkbox" title="<%= loginBean.translate("Accept terms of service") %>"><span id="terms-span"><%= loginBean.translate("Accept") %> 
				<% if (Site.DEDICATED) { %>
					<%= loginBean.translate("Terms") %></span><br/>
				<% } else { %>
					<%= loginBean.translate("our") %> <a target="_blank" href="terms.jsp"><%= loginBean.translate("terms") %></a> <%= loginBean.translate("and review our") %> <a target="_blank" href="privacy.jsp"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("policy") %></span><br/>
				<% } %>
				<input id="ok" name="create-user" type="submit" onclick="return validateForm()" value="<%= loginBean.translate("Sign Up") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		<% } else { %>
			<form name="create" id="create-user" action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input name="token" type="hidden" value="<%= loginBean.hashCode() %>"/>
				<span class="required"><%= loginBean.translate("User Id") %></span><br/>
				<input class="required" autofocus id="user" name="user" type="text" value=""  title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
				<span class="required"><%= loginBean.translate("Password") %></span><br/>
				<input id="password" class="required" name="password" type="password" value=""  title="<%= loginBean.translate("Enter a secure password (required)") %>"/><br/>
				<span class="required"><%= loginBean.translate("Retype Password") %></span><br/>
				<input id="password2" class="required" name="password2" type="password" value=""  title="<%= loginBean.translate("Confirm password (required)") %>"/><br/>
				<% if (Site.AGE_RESTRICT) { %>
					<span class="required"><%= loginBean.translate("Date of Birth") %></span><br/>
					<input class="required" id="dateOfBirth" name="dateOfBirth" type="date" placeholder="yyyy/mm/dd" value=""  title="<%= loginBean.translate("Enter your date of birth (yyyy/mm/dd, not visible to other users, required)") %>"/><br/>
				<% } %>
				<%= loginBean.translate("Password hint") %><br/>
				<input name="hint" type="text" value=""  title="<%= loginBean.translate("Enter a personal hint in case you forget your password (optional)") %>"/><br/>
				<% if (Site.COMMERCIAL) { %>
					<span class="required"><%= loginBean.translate("Name") %></span><br/>
					<input class="required" name="name" type="text" value=""  title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
					<input name="display-name" type="checkbox" title="<%= loginBean.translate("Display your name to other users") %>"><%= loginBean.translate("Show Name") %></input><br/>
					<span class="required"><%= loginBean.translate("Email") %></span><br/>
					<input class="required" name="email" type="text" value=""  title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
				<% } else { %>
					<%= loginBean.translate("Name") %><br/>
					<input name="name" type="text" value=""  title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
					<input name="display-name" type="checkbox" title="<%= loginBean.translate("Display your name to other users") %>"><%= loginBean.translate("Show Name") %></input><br/>
				
					<%= loginBean.translate("User Access") %>
					<select name="userAccess" title="<%= loginBean.translate("Define who can see your user profile") %>">
						<option value="Private" <%= loginBean.isUserAccessModeSelected("Private") %>><%= loginBean.translate("Private") %></option>
						<option value="Friends" <%= loginBean.isUserAccessModeSelected("Friends") %>><%= loginBean.translate("Friends") %></option>
						<option value="Everyone" <%= loginBean.isUserAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
					</select>
					<br/>
					<%= loginBean.translate("Email") %><br/>
					<input name="email" type="text" value=""  title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
				<% } %>
				<!-- 
					<%= loginBean.translate("Website") %><br/>
					<input name="website" type="text" value=""  title="<%= loginBean.translate("Enter your business or personal website (visible to other users, optional)") %>"/><br/>
					<%= loginBean.translate("Bio") %><br/>
					<textarea name="bio"  title="<%= loginBean.translate("Enter anything about you (HTML, visible to other users, optional)") %>"></textarea><br/>
				-->
				<input id="terms" name="terms" type="checkbox" title="<%= loginBean.translate("Accept terms of service") %>"><span id="terms-span"><%= loginBean.translate("Accept") %> 
				<% if (Site.DEDICATED) { %>
					<%= loginBean.translate("Terms") %></span><br/>
				<% } else { %>
					<%= loginBean.translate("our") %> <a target="_blank" href="terms.jsp"><%= loginBean.translate("terms") %></a> <%= loginBean.translate("and review our") %> <a target="_blank" href="privacy.jsp"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("policy") %></span><br/>
				<% } %>
				<input id="ok" name="create-user" type="submit" onclick="return validateForm()" value="<%= loginBean.translate("Sign Up") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
			<form name="create" style="display:none" id="create-user-fb" action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input name="token" type="hidden" value="<%= loginBean.hashCode() %>"/>
				<input id="credentials-type" name="credentials-type" type="hidden" />
				<input id="credentials-userid" name="credentials-userid" type="hidden" />
				<input id="credentials-token" name="credentials-token" type="hidden" />
				<span class="required"><%= loginBean.translate("User Id") %></span><br/>
				<input class="required" autofocus id="user2" name="user" type="text" value=""  title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
				<% if (Site.AGE_RESTRICT) { %>
					<span class="required"><%= loginBean.translate("Date of Birth") %></span><br/>
					<input class="required" id="dateOfBirth2" name="dateOfBirth" type="date" placeholder="yyyy/mm/dd" value=""  title="<%= loginBean.translate("Enter your date of birth (yyyy/mm/dd, not visible to other users, required)") %>"/><br/>
				<% } %>
				<% if (Site.COMMERCIAL) { %>
					<span class="required"><%= loginBean.translate("Name") %></span><br/>
					<input class="required" id="name" name="name" type="text" value=""  title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
					<input name="display-name" type="checkbox" title="<%= loginBean.translate("Display your name to other users") %>"><%= loginBean.translate("Show Name") %></input><br/>
					<span class="required"><%= loginBean.translate("Email") %></span><br/>
					<input class="required" id="email" name="email" type="text" value=""  title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
				<% } else { %>
					<%= loginBean.translate("Name") %><br/>
					<input id="name" name="name" type="text" value=""  title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
					<input name="display-name" type="checkbox" title="<%= loginBean.translate("Display your name to other users") %>">Show Name</input><br/>
					<%= loginBean.translate("Email") %><br/>
					<input id="email" name="email" type="text" value=""  title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
				<% } %>
				<!-- 
					<%= loginBean.translate("Website") %><br/>
					<input id="website" name="website" type="text" value=""  title="<%= loginBean.translate("Enter your business or personal website (visible to other users, optional)") %>"/><br/>
					<%= loginBean.translate("Bio") %><br/>
					<textarea id="bio" name="bio" title="<%= loginBean.translate("Enter anything about you (HTML, visible to other users, optional)") %>"></textarea><br/>
				-->
				<input id="terms2" name="terms" type="checkbox" title="Accept terms of service"><span id="terms-span2"><%= loginBean.translate("Accept") %> 
				<% if (Site.DEDICATED) { %>
					<%= loginBean.translate("Terms") %></span><br/>
				<% } else { %>
					<%= loginBean.translate("our") %> <a target="_blank" href="terms.jsp"><%= loginBean.translate("terms") %></a> <%= loginBean.translate("and review our") %> <a target="_blank" href="privacy.jsp"><%= loginBean.translate("privacy") %></a> <%= loginBean.translate("policy") %></span><br/>
				<% } %>
				<input id="ok" name="create-user" type="submit" onclick="return validateForm()" value="<%= loginBean.translate("Sign Up") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		<% } %>
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