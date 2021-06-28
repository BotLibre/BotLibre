<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	DomainBean domainBean = loginBean.getBean(DomainBean.class);
	String urllink = Site.URLLINK;
	if (request.isSecure()) {
		urllink = Site.SECUREURLLINK;
	}
	if (loginBean.isTranslationRequired()) {
		urllink = "";
	}
%>

<!-- Bottom widget -->
<section id="bottom-widget" class="home-section bg-white">
	<hr style="border-top: 2px solid #005c90;margin-bottom: 40px;"/>
	<div class="container">
		<div class="row">
			<div class="col-md-3">
				<div class="contact-widget wow bounceInLeft">
					
					<a href="login?sign-in=true"><%= loginBean.translate("Sign In") %></a><br>
					<% if (Site.ALLOW_SIGNUP) { %>
						<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && !domainBean.hasValidInstance()) { %>
							<a href="create-domain.jsp"><%= loginBean.translate("Sign Up") %></a><br>
						<% } else { %>
							<a href="login?sign-up=true"><%= loginBean.translate("Sign Up") %></a><br>
						<% } %>
					<% } %>
					<% if (!Site.DEDICATED || loginBean.isLoggedIn()) { %>
						<a href="browse?browse-type=Bot<%= domainBean.domainURL() %>"><%= loginBean.translate("Browse") %></a><br>
						<a href="browse?browse-type=Bot&search=true<%= domainBean.domainURL() %>"><%= loginBean.translate("Search") %></a><br>
						<a href="browse?browse-type=Bot&create=true<%= domainBean.domainURL() %>"><%= loginBean.translate("Create") %></a><br>
						<a href="browse?browse-type=Bot<%= domainBean.domainURL() %>"><%= loginBean.translate("Bots") %></a><br>
						<a href="browse?browse-type=Avatar<%= domainBean.domainURL() %>"><%= loginBean.translate("Avatars") %></a><br>
						<% if (Site.DEDICATED) { %>
							</div>
							</div>	
							<div class="col-md-3">
							<div class="contact-widget wow bounceInUp">
						<% } %>
						<a href="browse?browse-type=Script<%= domainBean.domainURL() %>"><%= loginBean.translate("Scripts") %></a><br>
						<a href="browse?browse-type=Forum<%= domainBean.domainURL() %>"><%= loginBean.translate("Forums") %></a><br>
						<a href="browse?browse-type=Channel<%= domainBean.domainURL() %>"><%= loginBean.translate("Live Chat") %></a><br>
						<a href="browse?browse-type=Graphic<%= domainBean.domainURL() %>"><%= loginBean.translate("Graphics") %></a><br>
						<a href="browse?browse-type=Domain<%= domainBean.domainURL() %>"><%= loginBean.translate("Workspaces") %></a><br>
						<% if (!Site.COMMERCIAL && !Site.DEDICATED) { %>
							<a href="browse?browse-type=ChatWar<%= domainBean.domainURL() %>"><%= loginBean.translate("Chat Bot Wars") %></a><br>
						<% } %>
						<% if (!Site.COMMERCIAL || loginBean.isSuper()) { %>
							<a href="browse?browse-type=User<%= domainBean.domainURL() %>"><%= loginBean.translate("Users") %></a><br>
						<% } %>
					<% } %>
				</div>
			</div>
		</div>
	 
	</div>
</section>

<!-- Footer -->
<footer>
	<div class="container">

		<div class="row">
			<div class="col-md-12">
				<p>
					<a class="lang" href="login?language=true"><img class="menubar" src="images/language2.png"> Language</a> 
					<a class="lang" href="http://<%= Site.URL %>">en</a>
					<% for (String language : loginBean.getLanguages()) { %>
						 <a class="lang" href="http://<%= language + "." + Site.SERVER_NAME %>"><%= language %></a>
					<% } %>
					<br/><br/>
					Version: <%= Site.VERSION %>
					<%= loginBean.isMobile() ? "<br/>" : " - " %>&copy; 2013-2021
					<% if (Site.HTTPS && !loginBean.isHttps()) { %>
						:	<a style="color:#00BB00;font-weight:bold" href="<%= Site.SECUREURLLINK %>">https</a><br/>
					<% } %>
				</p>
			</div>
		</div>
	</div>
</footer>

	<script type="text/javascript">
	// Add app id to page for speech, etc.
	if (document.getElementById("chat") == null && document.getElementById("embed") == null) {
		SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
	}
	</script>
	
