<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.admin.Domain"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.admin.Friendship"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.service.AppIDStats"%>
<%@page import="org.botlibre.web.admin.UserPayment"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.admin.WebMedium"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean domainBean = loginBean.getBean(DomainBean.class);
	String title = loginBean.translate("User");
	if (loginBean.getViewUser() != null) {
		title = loginBean.getViewUser().getUserId();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %><%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="<%= loginBean.getViewUser() == null ? "" : Utils.escapeQuotes(Utils.stripTags(loginBean.getViewUser().getBio())) %>"/>
	<link rel='image_src' href='<%= loginBean.http() + "://" + Site.URL + "/" + loginBean.getAvatarImage(loginBean.getViewUser()) %>' >
	<meta property='og:image' content='<%= loginBean.http() + "://" + Site.URL + "/" + loginBean.getAvatarImage(loginBean.getViewUser()) %>' />
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<script>
	$(function() {		
		$( "#dialog-delete" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#dialog-flag" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#delete-user" ).click(function() {
			$( "#dialog-delete" ).dialog( "open" );
			return false;
		});
		
		$( "#delete-user2" ).click(function() {
			$( "#dialog-delete" ).dialog( "open" );
			return false;
		});
		
		$( "#flag" ).click(function() {
			$( "#dialog-flag" ).dialog( "open" );
			return false;
		});
		
		$( "#flag2" ).click(function() {
			$( "#dialog-flag" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-delete" ).click(function() {
			$( "#dialog-delete" ).dialog( "close" );
			return false;
		});
		
		$( "#cancel-flag" ).click(function() {
			$( "#dialog-flag" ).dialog( "close" );
			return false;
		});
	});
	</script>
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
	<jsp:include page="error.jsp"/>
	<% if (loginBean.getViewUser() == null) { %>
		No user selected
	<% } else { %>
	
		<h1><%= loginBean.getViewUser().getUserHTML() %></h1>
		<% boolean admin = loginBean.isLoggedIn() && (loginBean.isSuper() || loginBean.getViewUser().equals(loginBean.getUser())); %>
		
		<% if (!admin && !loginBean.getViewUser().isPublic()) { %>
			<h5><%= loginBean.translate("This user's profile is private.") %></h5>
		<% } else if (!admin && !loginBean.getViewUser().isVerified() && !loginBean.getViewUser().isBot()) { %>
			<h5><%= loginBean.translate("This user's profile is hidden until they have verified their email address.") %></h5>
		<% } else { %>
			<% if (admin && !loginBean.getViewUser().isPrivate() && !loginBean.getViewUser().isVerified() && !loginBean.getViewUser().isBot()) { %>
				<h5><%= loginBean.translate("Your profile will be hidden until you have verified your email address.") %>
				<a class="menu" href="<%= "login?send-verify" + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Send a verification email to this email address") %>"><%= loginBean.translate("Resend Verify") %></a></h5>
			<% } else if (admin && !loginBean.getViewUser().isVerified() && !loginBean.getViewUser().isBot()) { %>
				<h5><%= loginBean.translate("Please verify your email address.") %>
				<a class="menu" href="<%= "login?send-verify" + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Send a verification email to this email address") %>"><%= loginBean.translate("Resend Verify") %></a></h5>
			<% } %>
			<div id="tabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
				<ul class='ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all'>
					<li class='ui-state-default ui-corner-top ui-tabs-active ui-state-active'>
						<a href='#tabs-1' class='ui-tabs-anchor' <%= loginBean.isMobile() ? "style='font-size:14px;'" : "" %>><%= loginBean.translate("Info") %></a>
					</li>
					<li class='ui-state-default ui-corner-top'>
						<a href='#tabs-2' class='ui-tabs-anchor' <%= loginBean.isMobile() ? "style='font-size:14px;'" : "" %>><%= loginBean.translate("Content") %></a>
					</li>
					<% if (admin) { %>
						<li class='ui-state-default ui-corner-top'>
							<a href='#tabs-3' class='ui-tabs-anchor' <%= loginBean.isMobile() ? "style='font-size:14px;'" : "" %>><%=loginBean.translate("Details") %></a>
						</li>
					<% } %>
					<% if (!Site.COMMERCIAL) { %>
						<li class='ui-state-default ui-corner-top'>
							<a href='#tabs-4' class='ui-tabs-anchor' <%= loginBean.isMobile() ? "style='font-size:14px;'" : "" %>><%=loginBean.translate("Friends") %></a>
						</li>
					<% } %>
				</ul>
				<div id='tabs-1' class='ui-tabs-panel ui-widget-content ui-corner-bottom'>
					<div style="float:right">
						<span class="dropt">
							<div style="right:4px">
								<% if (admin) { %>
									<a onclick='return changeIcon()' href='#' style='text-decoration:none' title='<%= loginBean.translate("Change your icon to a new image") %>'>
										<img src="<%= loginBean.getAvatarImage(loginBean.getViewUser()) %>" class="big-icon"/>
									</a>
								<% } else { %>
									<img src="<%= loginBean.getAvatarImage(loginBean.getViewUser()) %>" class="big-icon"/>
								<% } %>
							</div>
							<% if (admin) { %>
								<a onclick='return changeIcon()' href='#' style='text-decoration:none' title='<%= loginBean.translate("Change your icon to a new image") %>'>
									<img src="<%= loginBean.getAvatarImage(loginBean.getViewUser()) %>" class="small-icon"/>
								</a>
							<% } else { %>
								<img src="<%= loginBean.getAvatarImage(loginBean.getViewUser()) %>" class="small-icon"/>
							<% } %>
						</span>
					</div>	
					<p>
						<span class="description"><%= loginBean.getViewUser().getBioText() %></span>
					</p>
					<p>
						<% if (loginBean.getViewUser().getShouldDisplayName() || admin) { %>
							<span><%= loginBean.translate("Name") %>:</span> <%= loginBean.getViewUser().getName() %><br/>
						<% } %>
						<% if (loginBean.isSuper()) { %>
							<span><%= loginBean.translate("IP") %>:</span> <%= loginBean.getViewUser().getIP() %><br/>
							<span><%= loginBean.translate("Source") %>:</span> <%= loginBean.getViewUser().getSource() %><br/>
						<% } %>
						<% if (admin) { %>
							<span><%= loginBean.translate("Email") %>:</span> <%= Utils.sanitizer().sanitize(loginBean.getViewUser().getEmail()) %>
							<% if (loginBean.getViewUser().isVerified()) { %>
								: <span><%= loginBean.translate("verified") %></span>
							<% }  else { %>
								: <span><%= loginBean.translate("not verified") %> : <a class="menu" href="<%= "login?send-verify" + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Send a verification email to this email address") %>"><%= loginBean.translate("Resend Verify") %></a></span>
							<% } %>
							<br/>
							<span><%= loginBean.translate("Date of Birth") %>: <%= Utils.displayDate(loginBean.getViewUser().getDateOfBirth()) %></span><br/>
						<% } %>
						<% if (!loginBean.getViewUser().getWebsite().isEmpty()) { %>
							<span><%= loginBean.translate("Website") %>:</span> <%= loginBean.getViewUser().getWebsiteURL() %><br/>
						<% } %>
						<span>
						<% if (admin) { %>
							<% if (loginBean.getViewUser().getApplicationId() == null) { %>
								<%= loginBean.translate("Application ID") %>: <%= loginBean.translate("not assigned") %> :
								<a class="menu" href="<%= "login?reset-app-id" + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("An application id is required for use with the REST API, or the SDK") %>"><%= loginBean.translate("Obtain") %></a><br/>
							<% } else { %>
								<%= loginBean.translate("Application ID") %>: <%= loginBean.getViewUser().getApplicationId() %> :
								<a class="menu" href="<%= "login?reset-app-id"	+ proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("An application id is required for use with the REST API, or the SDK") %>"><%= loginBean.translate("Reset") %></a><br/>
							<% } %>
							<% if (!Site.DEDICATED) { %>
								<a href="partners.jsp"><%= loginBean.translate("Affiliate Link") %></a><br/>
							<% } %>
						<% } %>
						<% if (!Site.COMMERCIAL) { %>
							<%= loginBean.translate("Account Type") %>: <%= loginBean.getViewUser().getType() %><br/>
							<% if (admin && loginBean.getViewUser().getUpgradeDate() != null) { %>
								<%= loginBean.translate("Upgrade Date") %>: <%= Utils.displayDate(loginBean.getViewUser().getUpgradeDate()) %><br/>
								<%= loginBean.translate("Expiry Date") %>: <%= Utils.displayDate(loginBean.getViewUser().getExpiryDate()) %><br/>
								<% if (loginBean.getViewUser().isExpired()) { %>
									<b style="color:red">Your account has expired please renew your upgrade <a href="upgrade.jsp">here</a></b><br/>
								<% } %>
							<% } %>
						<% } %>
						<%= loginBean.translate("User Access") %>: <%= loginBean.getViewUser().getAccess() %><br/>
						<%= loginBean.translate("Joined") %>: <%= Utils.displayDate(loginBean.getViewUser().getCreationDate()) %><br/>
						<%= loginBean.translate("Connects") %>: <%= loginBean.getViewUser().getConnects() %><br/>
						<%= loginBean.translate("Last connected") %>: <%= Utils.displayTimestamp(loginBean.getViewUser().getLastConnected()) %><br/>
						</span>
					</p>
				</div>
				<div id="tabs-2" class="ui-tabs-hide">
					<p>
						<span>
						<% if (!embed) { %>
								<%= loginBean.translate("Bots") %>: <a href="<%= "browse?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getInstances() %></a><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Channels") %>: <a href="<%= "livechat?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getChannels() %></a><br/>
						<% } %>
						<% if (!embed || (loginBean.getActiveBean() instanceof LiveChatBean)) { %>
								<%= loginBean.translate("Chats") %>: <%= loginBean.getViewUser().getMessages() %><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Forums") %>: <a href="<%= "forum?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getForums() %></a><br/>
						<% } %>
						<% if (!embed || (loginBean.getActiveBean() instanceof ForumBean)) { %>
								<%= loginBean.translate("Posts") %>: <a href="<%= "forum-post?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getPosts() %></a><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Scripts") %>: <a href="<%= "script?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getScripts() %></a><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Avatars") %>: <a href="<%= "avatar?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getAvatars() %></a><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Graphics") %>: <a href="<%= "graphic?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getGraphics() %></a><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Analytics") %>: <a href="<%= "analytic?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getAnalytics() %></a><br/>
						<% } %>
						<% if (!embed) { %>
								<%= loginBean.translate("Workspaces") %>: <a href="<%= "domain?user-filter=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>"><%= loginBean.getViewUser().getDomains() %></a><br/>
						<% } %>
						</span>
					</p>
				</div>
				<% if (admin) { %>
					<div id="tabs-3" class="ui-tabs-hide">
						<p>
							<span>
							<% if (loginBean.getViewUser().getApplicationId() != null) { %>
								<% AppIDStats stats = AppIDStats.getStats(String.valueOf(loginBean.getViewUser().getApplicationId()), loginBean.getViewUser().getUserId()); %>
								<%= loginBean.translate("API calls today") %>: <%= stats == null ? 0 : stats.apiCalls %><br/>
								<%= loginBean.translate("API calls over limit") %>: <%= stats == null ? 0 : stats.overLimit %><br/>
							<% } %>
							<% if (loginBean.isSuper()) { %>
								Affiliate: <%= loginBean.getViewUser().getAffiliate() %><br/>
								<% if (!Site.COMMERCIAL) { %>
									Payment Verified: <%= loginBean.getViewUser().getVerifiedPayment() %><br/>
								<% } %>
							<% } %>
							<% if (admin) { %>
								Affiliates: <%= loginBean.getViewUser().getAffiliates() %><br/>
								<% if (!Site.COMMERCIAL) { %>
									<% if (!loginBean.getViewUser().getPayments().isEmpty()) { %>
										Payments:
										<% for (UserPayment payment : loginBean.getViewUser().getPayments()) { %>
											<br/>
											<%= payment.getPaymentDate() %> : <%= payment.getType() %> : <%= payment.getStatus() %> : <%= payment.getPaypalAmt() %> - <%= payment.getPaypalTx() %> - <%= payment.getPaypalCc() %> - <%= payment.getPaypalSt() %> - <%= payment.getUserId() %>
										<% } %>
									<% } %>
								<% } %>
							<% } %>
							</span>
						</p>
					</div>
				<% } %>
				<% if (!Site.COMMERCIAL) { %>
					<div id="tabs-4" class="ui-tabs-hide">
						<% if (loginBean.isLoggedIn()) { %>
							<% List<Friendship> friends = loginBean.getUserFriendships(loginBean.getViewUser().getUserId()); %>
							<% if (friends.size() != 0) { %>
								<h4><%= loginBean.translate("Friends") %></h4>
							<% } %>
							<% for (Friendship friend : friends) { %>
								<div style="padding-left:20px;padding-bottom:5px;">
									<a id="user-friend-link" href="login?view-user=<%= friend.getFriend() %>"><%= friend.getFriend() %></a>
								</div>
							<% } %>
							<% List<Friendship> followers = loginBean.getUserFollowers(loginBean.getViewUser().getUserId()); %>
							<% if (followers.size() != 0) { %>
								<h4><%= loginBean.translate("Followers") %></h4>
							<% } %>
							<% for (Friendship follower : followers) { %>
								<div style="padding-left:20px;padding-bottom:5px;">
									<span value="<%= follower.getUserId()%>"><a id="user-follower-link" href="login?view-user=<%= follower.getUserId() %>"><%= follower.getUserId() %></a></span>
								</div>
							<% } %>
						<% } %>
					</div>
				<% } %>
			</div>
			<% if (admin) { %>
				<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && !embed) { %>
					<form action="domain" method="get">
						<% List<Domain> domains = domainBean.getUserInstances(); %>
						<% if (!domains.isEmpty()) { %>
							<%= loginBean.translate("Workspaces") %>
							<select id="domain" name="domain" onchange="this.form.submit()">
								<% for (Domain domain : domains) { %>
									<option value="<%= domain.getId() %>" <%= domainBean.getDomainSelected(domain.getId().toString()) %>><%= domain.getName() %></option>
								<% } %>
							</select> <a href="domain?details=true&id=<%= domainBean.getSelectedDomainId() %>"><%= loginBean.translate("Details") %></a> : <a href="domain?details=true&id=<%= domainBean.getSelectedDomainId() %>"><%= loginBean.translate("Make Payment") %></a> : <a href="create-domain.jsp"><%= loginBean.translate("Create New Workspace") %></a>
						<% } else { %>
							<a href="create-domain.jsp"><%= loginBean.translate("Create Workspace") %></a>
						<% } %>
					</form>
				<% } %>
			<% } %>
			<% if (loginBean.getViewUser().isFlagged()) { %>
				<p style="color:red;font-weight:bold;"><%= loginBean.translate("This user has been flagged for") %> "<%= loginBean.getViewUser().getFlaggedReason() %>" <%= loginBean.translate("by") %> <%= loginBean.getViewUser().getFlaggedUser() %>.</p>
			<% } %>
			<% if (loginBean.getViewUser().isBlocked()) { %>
				<p style="color:red;font-weight:bold;"><%= loginBean.translate("This user has been banned") %></p>
			<% } %>
		<% } %>	
		<div style='position:relative;margin-top:12px;margin-bottom:12px'>
			<span class='dropt'>
				<div style='text-align:left;bottom:36px'>
					<table>
						<% if (loginBean.isLoggedIn() && loginBean.getViewUser().equals(loginBean.getUser())) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="admin-user.jsp" title='<%= loginBean.translate("Administer user avatar and voice configurations") %>'>
										<img src='images/admin.svg' class='menu'/> <%= loginBean.translate("User Administration") %>
									</a>
								</td>
							</tr>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%= "login?browse-user-messages" + proxy.proxyString() %>" title='<%= loginBean.translate("Browse user messages") %>'>
										<img src='images/round_message.svg' class='menu'/> <%= loginBean.translate("User Messages") %>
									</a>
								</td>
							</tr>
						<% } %>
						<% if (loginBean.isLoggedIn() && loginBean.getViewUser().equals(loginBean.getUser())) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%= "login?browse-user-stats" + proxy.proxyString() %>" title='<%= loginBean.translate("View your analytics") %>'>
										<img src='images/stats-pic.svg' class='menu'/> <%= loginBean.translate("User Analytics") %>
									</a>
								</td>
							</tr>
						<% } %>	
						<% if (admin) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%= "login?edit-user" + proxy.proxyString() %>" title='<%= loginBean.translate("Edit user details") %>'>
										<img src='images/edit.svg' class='menu'/> <%= loginBean.translate("Edit Details") %>
									</a>
								</td>
							</tr>
						<% } %>
						<% if (!loginBean.getViewUser().isFlagged()) { %>
							<% if (loginBean.isLoggedIn()) { %>
								<% if (!loginBean.getViewUser().equals(loginBean.getUser()) || loginBean.isSuper()) { %>
									<tr class='menuitem'>
										<td>
											<a class='menuitem' id="flag2" href="#" title='<%= loginBean.translate("Flag user as offensive, or in violation of site rules") %>'>
												<img src='images/flag2.svg' class='menu'/> <%= loginBean.translate("Flag User") %>
											</a>
										</td>
									</tr>
								<% } else { %>
									<tr class='menuitem'>
										<td>
											<a class='menuitem' href="#" onclick="SDK.showError('You cannot flag yourself'); return false;" title='<%= loginBean.translate("You cannot flag yourself") %>'>
												<img src='images/flag2.svg' class='menu'/> <%= loginBean.translate("Flag User") %>
											</a>
										</td>
									</tr>
								<% } %>
							<% } else { %>
								<tr class='menuitem'>
									<td>
										<a class='menuitem' href="#" onclick="SDK.showError('You must sign in first'); return false;" title='<%= loginBean.translate("You must sign in first") %>'>
											<img src='images/flag2.svg' class='menu'/> <%= loginBean.translate("Flag User") %>
										</a>
									</td>
								</tr>
							<% } %>
						<% } else if (loginBean.isSuper()) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%="login?unflag-user" + proxy.proxyString() + loginBean.postTokenString() %>">
										<img src='images/unflag2.svg' class='menu'/> <%= loginBean.translate("Unflag User") %>
									</a>
								</td>
							</tr>
						<% } %>
						<% if (loginBean.isSuper()) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%="login?become-user" + proxy.proxyString() + loginBean.postTokenString() %>">
										<img src='images/empty.png' class='menu'/> <%= loginBean.translate("Become User") %>
									</a>
								</td>
							</tr>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%="login?block-user" + proxy.proxyString() + loginBean.postTokenString() %>">
										<img src='images/empty.png' class='menu'/> <%= loginBean.translate("Block User") %>
									</a>
								</td>
							</tr>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%="login?unblock-user" + proxy.proxyString() + loginBean.postTokenString() %>">
										<img src='images/empty.png' class='menu'/> <%= loginBean.translate("Unblock User") %>
									</a>
								</td>
							</tr>
						<% } %>
						<% if (admin) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="#" onclick="changeIcon(); return false;" title='Change your user icon'>
										<img src='images/icon.jpg' class='menu'/> <%= loginBean.translate("Change Icon") %>
									</a>
								</td>
							</tr>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' id="delete-user2" href="#" onclick="return false;" title='Delete your account'>
										<img src='images/remove.svg' class='menu'/> <%= loginBean.translate("Delete Account") %>
									</a>
								</td>
							</tr>
						<% } %>
						<% if (!admin && !loginBean.getViewUser().isPrivate()) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%="login?send-message&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>" title="<%= loginBean.translate("Send a message to the user") %>">
										<img src='images/round_message.svg' class='menu'/> <%= loginBean.translate("Send Message") %>
									</a>
								</td>
							</tr>
							<% if (!Site.COMMERCIAL && loginBean.isLoggedIn()) { %>
								<tr class='menuitem'>
									<td>
										<a class='menuitem' href="<%="login?add-new-friend&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Add friend") %>">
											<img src='images/round_avatar.svg' class='menu'/> <%= loginBean.translate("Add Friend") %>
										</a>
									</td>
								</tr>
							<% } %>
						<% } else if (loginBean.isLoggedIn() && (loginBean.isSuper() && !loginBean.getViewUser().equals(loginBean.getUser()))) { %>
							<tr class='menuitem'>
								<td>
									<a class='menuitem' href="<%="login?send-message&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>" title="<%= loginBean.translate("Send a message to the user") %>">
										<img src='images/round_message.svg' class='menu'/> <%= loginBean.translate("Send Message") %>
									</a>
								</td>
							</tr>
							<% if (!Site.COMMERCIAL && loginBean.isLoggedIn()) { %>
								<tr class='menuitem'>
									<td>
										<a class='menuitem' href="<%="login?add-new-friend&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Add friend") %>">
											<img src='images/round_avatar.svg' class='menu'/> <%= loginBean.translate("Add Friend") %>
										</a>
									</td>
								</tr>
							<% } %>
						<% } %>
					</table>
				</div>
				<a href='#' onclick='return false'><img src='images/menu.png' class='toolbar'/></a>
			</span>
			<% if (loginBean.isLoggedIn() && loginBean.getViewUser().equals(loginBean.getUser())) { %>
				<a href="admin-user.jsp" title="User administration"><img src="images/admin.svg" class="toolbar"></a>
				<a href="<%= "login?browse-user-messages" + proxy.proxyString() %>" title='<%= loginBean.translate("User messages") %>'><img src='images/round_message.svg' class='toolbar'/></a>
				<% if (!Site.COMMERCIAL) { %>
					<a href="<%= "login?browse-user-friends" + proxy.proxyString() %>" title='<%= loginBean.translate("User friends") %>'><img src='images/friends.svg' class='toolbar'/></a>
				<% } %>
			<% } %>
			<% if (loginBean.isLoggedIn() && loginBean.getViewUser().equals(loginBean.getUser())) { %>		
				<a href="<%= "login?browse-user-stats" + proxy.proxyString() %>" title='<%= loginBean.translate("User analytics") %>'><img src='images/stats-pic.svg' class='toolbar'/></a>
			<% } %>	
			<% if (admin) { %>
				<a href="<%= "login?edit-user"	+ proxy.proxyString() %>" title='<%= loginBean.translate("Edit user details") %>'><img src='images/edit.svg' class='toolbar'/></a>
			<% } %>
			<% if (!loginBean.getViewUser().isFlagged()) { %>
				<% if (loginBean.isLoggedIn()) { %>
					<% if (!loginBean.getViewUser().equals(loginBean.getUser()) || loginBean.isSuper()) { %>
						<a id="flag" href="#" title="<%= loginBean.translate("Flag user as offensive, or in violation of site rules") %>"><img src='images/flag2.svg' class='toolbar'/></a>
					<% } else { %>
						<a onclick="SDK.showError('<%= loginBean.translate("You cannot flag yourself") %>'); return false;" title='<%= loginBean.translate("You cannot flag yourself") %>'><img src='images/flag2.svg' class='toolbar'/></a>
					<% } %>
				<% } else { %>
					<a onclick="SDK.showError('<%= loginBean.translate("You must sign in first") %>'); return false;" title='<%= loginBean.translate("You must sign in first") %>'><img src='images/flag2.svg' class='toolbar'/></a>
				<% } %>
			<% } else if (loginBean.isSuper()) { %>
				<a href="<%="login?unflag-user" + proxy.proxyString() + loginBean.postTokenString() %>"><img src='images/unflag2.svg' class='toolbar'/></a>
			<% } %>
			<% if (admin) { %>
				<a id="delete-user" href="#" title='<%= loginBean.translate("Delete your account") %>' onclick="return false;"><img src='images/remove.svg' class='toolbar'/></a>
			<% } %>
			<% if (!admin && !loginBean.getViewUser().isPrivate()) { %>
				<a href="<%="login?send-message&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>" title="<%= loginBean.translate("Send a message to the user") %>"><img src='images/round_message.svg' class='toolbar'/></a>
				<% if (!Site.COMMERCIAL && loginBean.isLoggedIn()) { %>
					<a href="<%="login?add-new-friend&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Add friend") %>">
						<img src='images/round_avatar.svg' class='toolbar'/>
					</a>
				<% } %>
			<% } else if (loginBean.isLoggedIn() && (loginBean.isSuper() && !loginBean.getViewUser().equals(loginBean.getUser()))) { %>
				<a href="<%="login?send-message&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() %>" title="<%= loginBean.translate("Send a message to the user") %>"><img src='images/round_message.svg' class='toolbar'/></a>
				<% if (!Site.COMMERCIAL && loginBean.isLoggedIn()) { %>
					<a href="<%="login?add-new-friend&user=" + loginBean.encodeURI(loginBean.getViewUser().getUserId()) + proxy.proxyString() + loginBean.postTokenString() %>" title="<%= loginBean.translate("Add friend") %>">
						<img src='images/round_avatar.svg' class='toolbar'/>
					</a>
				<% } %>
			<% } %>
		</div>
		
		<div id="dialog-flag" title="<%= loginBean.translate("Flag") %>" class="dialog">
			<form action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input type="checkbox" name="flagged" title="<%= loginBean.translate("Do not misuse the flag option, flagging valid users can cause your account to be disabled") %>"><%= loginBean.translate("Flag user as offensive, or in violation of site rules") %></input><br/>
				<input name="flag-reason" type="text" placeholder="<%= loginBean.translate("reason") %>"/ /><br/>
				<input class="delete" name="flag-user" type="submit" value="<%= loginBean.translate("Flag") %>" title="<%= loginBean.translate("Flag user as offensive, or in violation of site rules") %>"/>
				<input id="cancel-flag" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		</div>
		
		<div id="dialog-delete" title="<%= loginBean.translate("Delete") %>" class="dialog">
			<form action="login" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<input type="checkbox" name="confirmDelete" title="<%= loginBean.translate("Permanently delete your user account") %>"><%= loginBean.translate("Permanently delete your user account") %></input><br/>
				<input class="delete" name="delete-user" type="submit" value="<%= loginBean.translate("Delete") %>" title="<%= loginBean.translate("Permanently delete your user account (you must delete your content first)") %>"/>
				<input id="cancel-delete" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		</div>
		
		<form id='icon-upload-form' action='user-upload' method='post' enctype='multipart/form-data' style='display:none'>
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
		</form>
		<script>
			var changeIcon = function () {
				SDK.application = '<%= AdminDatabase.getTemporaryApplicationId() %>';
				return GraphicsUploader.openUploadDialog(document.getElementById('icon-upload-form'), '<%= loginBean.translate("Change Icon") %>');
			}
		</script>
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