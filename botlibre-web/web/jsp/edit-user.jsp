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
	<title>Edit User<%= embed ? "" : " - " + Site.NAME %></title>
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
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn() || loginBean.getEditUser() == null) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit your user profile") %>.
		</p>
	<% } else { %>
	<h1><%= loginBean.getEditUser().getUserId() %></h1>
	<form action="login" method="post" class="message">
		<%= loginBean.postTokenInput() %>
		<%= proxy.proxyInput() %>
		<span class="required"><%= loginBean.translate("Password") %></span><br/>
		<input class="required" autofocus name="old-password" type="password" value="" /><br/>
		<%= loginBean.translate("New Password") %><br/>
		<input name="new-password" type="password" value="" /><br/>
		<%= loginBean.translate("Retype New Password") %><br/>
		<input name="new-password2" type="password" value="" /><br/>
		<%= loginBean.translate("Password hint") %><br/>
		<input name="hint" type="text" value="<%= loginBean.getEditUser().getHint() %>" /><br/>
		<%= loginBean.translate("Name") %><br/>
		<input name="name" type="text" value="<%= loginBean.getEditUser().getName() %>" /><br/>
		<input name="display-name" type="checkbox" <% if (loginBean.getEditUser().getShouldDisplayName()) { %>checked="checked"<% } %> title="<%= loginBean.translate("Display your name to other users") %>" onMouseOut="javascript:return false;"><%= loginBean.translate("Show Name") %></input><br/>
		<%= loginBean.translate("User Access") %>
		<select name="userAccess" title="Define who can see your user profile">
			<option value="Private" <%= loginBean.isUserAccessModeSelected("Private") %>><%= loginBean.translate("Private") %></option>
			<option value="Friends" <%= loginBean.isUserAccessModeSelected("Friends") %>><%= loginBean.translate("Friends") %></option>
			<option value="Everyone" <%= loginBean.isUserAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
		</select>
		<br/>
		<%= loginBean.translate("Tags") %><br/>
		<input id="tags" name="tags" type="text" value="" placeholder='<%= loginBean.translate("optional comma seperated list of tags to tag the user under") %>'/><br/>
		<script>
			$(function() {
				var availableTags = [ <%= loginBean.getAllUserTagsString() %> ];
				multiDropDown('#tags', availableTags);
				$("#tags").val('<%= loginBean.getUser().getTagsString() %>');
			});
		</script>
		<%= loginBean.translate("Email") %><br/>
		<input name="email" type="text" value="<%= loginBean.getEditUser().getEmail() %>" /><br/>
		<%= loginBean.translate("Email Preferences") %> <input name="email-notices" type="checkbox" <% if (loginBean.getEditUser().getEmailNotices()) { %>checked="checked"<% } %> title="Send email notification for specific or site notices" onMouseOut="javascript:return false;"><%= loginBean.translate("Notifications") %></input>
		<input name="email-messages" type="checkbox" <% if (loginBean.getEditUser().getEmailMessages()) { %>checked="checked"<% } %> title="<%= loginBean.translate("Send email notification if the user is sent a message") %>" onMouseOut="javascript:return false;"><%= loginBean.translate("Messages") %></input>
		<input name="email-summary" type="checkbox" <% if (loginBean.getEditUser().getEmailSummary()) { %>checked="checked"<% } %> title="<%= loginBean.translate("Send a weekly summary of the user's bot's activity") %>" onMouseOut="javascript:return false;"><%= loginBean.translate("Weekly Summary") %></input><br/>
		<%= loginBean.translate("Website") %><br/>
		<input name="website" type="text" value="<%= loginBean.getEditUser().getWebsite() %>" /><br/>
		<%= loginBean.translate("Bio") %><br/>
		<textarea name="bio" type="textarea" ><%= loginBean.getEditUser().getBio() %></textarea><br/>						
		<% if (!Site.COMMERCIAL) { %>
			<%= loginBean.translate("Ad Code") %><br/>
			<input name="adVerified" type="checkbox" <%= loginBean.getEditUser().isAdCodeVerified() ? "checked" : "" %> <%= loginBean.isSuper() ? "" : "disabled" %> title="If you ad contains JavaScript it must be verified before it is used"><%= loginBean.translate("Ad Verified") %></input><br/>
			<textarea name="adCode"
					title="<%= loginBean.translate("You can add your own ad code from Google Adsense or any other advertising provider to your content, and make money.  Ensure the ad complise with our terms of service, and does not contain any offensive or adult content.") %>"
					type="textarea" ><%= loginBean.getEditUser().getAdCode() %></textarea><br/>
		<% } %>
		<% if (loginBean.isSuper()) { %>
			<input name="verifiedPayment" type="checkbox" <% if (loginBean.getEditUser().getVerifiedPayment()) { %>checked="checked"<% } %> onMouseOut="javascript:return false;">Payment Verified</input><br/>
			<%= loginBean.translate("Account Type") %>
			<select id="type" name="type">
				<option value="Basic" <%= loginBean.isUserTypeSelected("Basic") %>><%= loginBean.translate("Basic") %></option>
				<option value="Bronze" <%= loginBean.isUserTypeSelected("Bronze") %>><%= loginBean.translate("Bronze") %></option>
				<option value="Gold" <%= loginBean.isUserTypeSelected("Gold") %>><%= loginBean.translate("Gold") %></option>
				<option value="Platinum" <%= loginBean.isUserTypeSelected("Platinum") %>><%= loginBean.translate("Platinum") %></option>
				<option value="Diamond" <%= loginBean.isUserTypeSelected("Diamond") %>><%= loginBean.translate("Diamond") %></option>
				<option value="Partner" <%= loginBean.isUserTypeSelected("Partner") %>><%= loginBean.translate("Partner") %></option>
				<option value="Admin" <%= loginBean.isUserTypeSelected("Admin") %>><%= loginBean.translate("Admin") %></option>
			</select><br/>
		<% } %>
		<input id="ok" name="save-user" type="submit" value="<%= loginBean.translate("Sav") %>e"/><input id="cancel" name="cancel-user" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
	</form>
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