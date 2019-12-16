<%@page import="org.botlibre.web.bean.BrowseBean"%>
<%@page import="org.botlibre.web.admin.AccessMode"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.ForumEmbedTabBean"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean forumBean = loginBean.getBean(ForumBean.class);
	String title = "Forum";
	if (forumBean.getInstance() != null) {
		title = forumBean.getInstance().getName();
	}
	ForumEmbedTabBean bean = loginBean.getBean(ForumEmbedTabBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<%= loginBean.getJQueryHeader() %>
	<title><%= title %> Embed - <%= Site.NAME %></title>
	<meta name="description" content="Generate the HTML or JavaScript code to embed your forum on your own website"/>	
	<meta name="keywords" content="embed, javascript, html, code, forum"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="forum-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<% forumBean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt-banner">
			<img id="help-icon" src="images/help.svg"/>
			<div>
				<p class="help">
					<%= loginBean.translate("You can embed your forum on your own website or blog just by adding some simple html to your site.") %>
					<%= loginBean.translate("You are free to embed your own forums for personal, or commercial purposes.") %>
				</p>
			</div>
		</span> <%= loginBean.translate("Embed the forum on your own website, or blog") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to embed a forum") %>.
		</p>
	<% } else if (forumBean.getInstance() == null) { %>
		<p><%= loginBean.translate("No forum selected.") %></p>
	<% } else if (!forumBean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this forum.") %>
	<% } else { %>
		<% if (!bean.getCode().isEmpty()) { %>
			<%= bean.getCode() %>
			<h2>Embedding Code</h2>
			<form action="forum" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<input name="codeToken" type="hidden" value="<%= bean.generateCodeToken() %>"/>
				<textarea name="code" ><%= bean.getDisplayCode() %></textarea><br/>
				<input id="ok" onclick="document.getElementById('get-code-form').submit(); return false;" type="submit" name="get-code" value="Generate Code"/>
				<input type="submit" name="run-code" value="Execute Code"/><br/>
			</form>
		<% } %>
		<h2><%= loginBean.translate("Embedding Options") %></h2>
		<form id="get-code-form" action="forum" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= forumBean.instanceInput() %>
			<input id="embed" name="embed" type="hidden" value="embed"/>
			<% if (forumBean.isAdmin()) { %>
				<%= loginBean.translate("Subdomain (or domain)") %><br/>
				<input id="subdomain" name="subdomain" type="text" value="<%= forumBean.getInstance().getSubdomain() %>" title="<%= loginBean.translate("You can choose a subdomain to host your own forum website, or give a domain that you have registered and forward to this server's ip address") %>"  /><br/>
			<% } %>
			<%= loginBean.translate("Embedding Type") %>
			<select name="type" onchange="this.form.submit()">
				<option value="link" <%= bean.getTypeSelectedString("link") %>><%= loginBean.translate("Link") %></option>
				<option value="button" <%= bean.getTypeSelectedString("button") %>><%= loginBean.translate("Button") %></option>
				<option value="bubble" <%= bean.getTypeSelectedString("bubble") %>><%= loginBean.translate("Bubble") %></option>
				<option value="bar" <%= bean.getTypeSelectedString("bar") %>><%= loginBean.translate("Bar") %></option>
				<option value="frame" <%= bean.getTypeSelectedString("frame") %>><%= loginBean.translate("Frame") %></option>
			</select><br/>
			Caption<br/>
			<input type="text" name="caption" value="<%= bean.getCaption() %>" title="<%= loginBean.translate("The text on the button or link") %>" /><br/>
			<% if (forumBean.getInstance().isPrivate() || forumBean.getInstance().getAccessMode() != AccessMode.Everyone) { %>
				<%= loginBean.translate("Guest User") %><br/>
				<input type="text" name="user" value="<%= bean.getUserName() %>" title="<%= loginBean.translate("Guest user to connect as") %>" /><br/>
				<%= loginBean.translate("Password") %><br/>
				<input type="password" name="password" value="<%= bean.getPassword() %>" title="<%= loginBean.translate("Password for guest user (not secure)") %>" /><br/>
				<%= loginBean.translate("Token") %><br/>
				<input type="text" name="token" value="<%= bean.getToken() %>" title="<%= loginBean.translate("Token for guest user") %>" /><br/>
			<% } %>
			<%= loginBean.translate("Style Sheet") %><br/>
			<input type="text" name="css" value="<%= bean.getCss() %>" title="<%= loginBean.translate("The CSS style sheet or CSS to customize the page") %>" /><br/>
			<%= loginBean.translate("Banner HTML") %><br/>
			<input type="text" name="banner" value="<%= bean.getBanner() %>" title="<%= loginBean.translate("An HTML script or page to embed as the page banner") %>" /><br/>
			<%= loginBean.translate("Footer HTML") %><br/>
			<input type="text" name="footer" value="<%= bean.getFooter() %>" title="<%= loginBean.translate("An HTML script or page to embed as the page footer") %>" /><br/>
			<%= loginBean.translate("Color") %><br/>
			<input type="text" id="embedcolor" name="color" value="<%= bean.getColor() %>" title="<%= loginBean.translate("The color to use for the button or link") %>" /><br/>
			<script>
				$( "#embedcolor" ).autocomplete({
				source: [<%= BrowseBean.getColorsString() %>],
			    minLength: 0
				}).on('focus', function(event) {
				    var self = this;
				    $(self).autocomplete("search", "");
				});
			</script>
			<%= loginBean.translate("Background Color") %><br/>
			<input type="text" id="embedbackground" name="background" value="<%= bean.getBackground() %>" title="<%= loginBean.translate("The background color to use") %>" /><br/>
			<script>
				$( "#embedbackground" ).autocomplete({
				source: [<%= BrowseBean.getColorsString() %>],
			    minLength: 0
				}).on('focus', function(event) {
				    var self = this;
				    $(self).autocomplete("search", "");
				});
			</script>
			
			<input name="loginBanner" type="checkbox" title="<%= loginBean.translate("Choose if login banner should be displayed") %>"
					<% if (bean.getLoginBanner()) { %>checked<% } %>><%= loginBean.translate("Login Banner") %></input><br/>
			<input name="facebookLogin" type="checkbox" title="<%= loginBean.translate("Choose if Facebook login option should be provided") %>"
					<% if (bean.getFacebookLogin()) { %>checked<% } %>><%= loginBean.translate("Facebook Login") %></input><br/>
			<% if (!Site.COMMERCIAL) { %>
				<input name="showLink" type="checkbox" title="<%= loginBean.translate("Choose if a backlink to") %>  <%= Site.NAME %> <%= loginBean.translate("should be displayed (requires Bronze account)") %>"
						<% if (bean.getShowLink()) { %>checked<% } %>><%= loginBean.translate("Backlink") %></input><br/>
				<input name="showAds" type="checkbox" title="<%= loginBean.translate("Choose if the forum's ad show be displayed") %>"
						<% if (bean.getShowAds()) { %>checked<% } %>><%= loginBean.translate("Show Ads") %></input><br/>
			<% } %>
		</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>
