<%@page import="org.botlibre.web.service.BeanManager"%>
<%@page import="org.botlibre.web.service.TranslationService"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.Domain"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.BotBean" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<%
	proxy.setRedirect(false);
	loginBean = proxy.checkLoginBean(loginBean);
	BotBean botBean = loginBean.getBotBean();
	DomainBean domainBean = loginBean.getBean(DomainBean.class);
	loginBean.initialize(getServletContext(), request, response);
	String urllink = Site.URLLINK;
	if (request.isSecure()) {
		urllink = Site.SECUREURLLINK;
	}
	if (loginBean.isTranslationRequired()) {
		urllink = "";
	}
%>

<div class="navbar1" <%= AdminDatabase.outOfMemory || BeanManager.manager().isDeadlocked() ? "style='background-color:red'" : "" %>>

	<div class="dropdown">
		<a href="" onclick="return false;" class="dropbtn"><%= loginBean.translate("More") %> <i class="fa fa-caret-down"></i></a>
	
		<div class="dropdown-content">
			<% if (Site.COMMERCIAL) { %>
				<% if (!Site.DEDICATED) { %>
					<a href="pricing.jsp"><%= loginBean.translate("Pricing") %></a>
					<a href="freetrial.jsp"><%= loginBean.translate("Free Trial") %></a>
				<% } %>
			<% } else { %>
				<a href="upgrade.jsp"><%= loginBean.translate("Upgrade") %></a>
			<% } %>
			<% if (!loginBean.isLoggedIn()) { %>
				<a href="login?sign-in=true"><%= loginBean.translate("Sign In") %></a>
				<% if (Site.ALLOW_SIGNUP) { %>
					<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && !domainBean.hasValidInstance()) { %>
						<a href="create-domain.jsp"><%= loginBean.translate("Sign Up") %></a>
					<% } else { %>
						<a href="login?sign-up=true"><%= loginBean.translate("Sign Up") %></a>
					<% } %>
				<% } %>
			<% } else { %>
				<a href="login?user-details=true"><%= loginBean.translate("User Details") %></a>
				<a href="browse?my-instances=My+Bots"><%= loginBean.translate("My Bots") %></a>
				<a href="#" onclick="document.getElementById('logout').click()"><%= loginBean.translate("Sign Out") %></a>
				<form action="login" style="display:none">
					<%= proxy.proxyInput() %>
					<input type="submit" id="logout" name="logout"/>
				</form>
			<% } %>
			<% if (!Site.DEDICATED) { %>
				<a href="http://botlibre.blogspot.com/" target="_blank"><%= loginBean.translate("Visit Blog") %></a>
				<a href="search.jsp"><%= loginBean.translate("Search Website") %></a>
			<% } %>
			<a href="login?language=language"><%= loginBean.translate("Choose Language") %></a>
			<% if (!Site.DEDICATED) { %>
				<a href="api.jsp"><%= loginBean.translate("API") %></a>
				<a href="sdk.jsp"><%= loginBean.translate("SDK") %></a>
				<a href="enterprise-bot-platform.jsp"><%= loginBean.translate("Enterprise Bot Platform") %></a>
				<a href="download.jsp"><%= loginBean.translate("Download") %></a>
				<a href="browse?browse-type=Desktop"><%= loginBean.translate("Desktop Download") %></a>
				<% if (!Site.COMMERCIAL && !Site.DEDICATED) { %>
					<a href="browse?browse-type=ChatWar"><%= loginBean.translate("Chat Bot Wars") %></a>
				<% } %>
				<a href="doc.jsp"><%= loginBean.translate("Docs") %></a>
				<a href="help.jsp"><%= loginBean.translate("Help") %></a>
			<% } %>
		</div>
	</div>

	<% if (!Site.DEDICATED) { %>
		<div class="dropdown1">
			 <a href="" onclick="return false;" class="dropbtn1"><%= domainBean.getInstance() == null || domainBean.getInstance().getName().equals(Site.DOMAIN) ? Site.NAME : domainBean.getInstance().getName() %> <i class="fa fa-caret-down"></i></a>
			
			<div class="dropdown-content1">
				
				<a href="domain?domain=<%= Site.DOMAIN %>"><%= Site.NAME %></a>
				<a href="https://www.botlibre.com">Bot Libre</a>
				<a value="-">-----------</a>
				<a href="create-domain.jsp">Create new workspace</a>
				<% List<Domain> domains = domainBean.getUserInstances(); %>
				<% if (!domains.isEmpty()) { %>
					<a value="-">-----------</a>
				<% } %>
				<% for (Domain domain : domains) { %>
					<a href="domain?domain=<%= domain.getId() %>"><%= domain.getName() %></a>
				<% } %>
			</div>
		</div>
	<% } %>

	<% if (!Site.DEDICATED) { %>
		<a href="login?language=true" class="main-link"><img src="images/language2.png" alt="" title="<%= loginBean.translate("Language") %>"><span><%= loginBean.translate("Language") %></span></a>
		<a href="help.jsp" class="main-link2"><img src="images/help2.png" alt="" title="<%= loginBean.translate("Help") %>"><span><%= loginBean.translate("Help") %></span></a>
		<a href="search.jsp" class="main-link"><img src="images/search2.png" alt="" title="<%= loginBean.translate("Search") %>"><span><%= loginBean.translate("Search") %></span></a>
		<a href="http://botlibre.blogspot.com/" target="_blank" class="main-link"><img src="images/blog2.png" alt="" title="<%= loginBean.translate("Blog") %>"><span><%= loginBean.translate("Blog") %></span></a>
	<% } %>
	<% if (!loginBean.isLoggedIn()) { %>
		<% if (Site.ALLOW_SIGNUP) { %>
			<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && !domainBean.hasValidInstance()) { %>
				<a href="create-domain.jsp" class="main-link2"><img src="images/signup2.png" alt="" title="<%= loginBean.translate("Sign Up") %>"><span><%= loginBean.translate("Sign Up") %></span></a>
			<% } else { %>
				<a href="login?sign-up=true" class="main-link2"><img src="images/signup2.png" alt="" title="<%= loginBean.translate("Sign Up") %>"><span><%= loginBean.translate("Sign Up") %></span></a>
			<% } %>
		<% } %>
		<a href="login?sign-in=true" class="main-link"> <img src="images/login2.png" alt="" title="<%= loginBean.translate("Sign In") %>"><span><%= loginBean.translate("Sign In") %></span></a>
	<% } else { %>
		<div class="dropdown">
			<a href="" onclick="return false;" class="main-link" style="padding-bottom: 12px;"> <img src="<%= loginBean.getAvatarImage(loginBean.getUser()) %>"><span><%= loginBean.getUserId() %></span> <i class="fa fa-caret-down"></i></a>
			<div class="dropdown-content">
				<a href="login?user-details=true"><%= loginBean.translate("User Details") %></a>
				<a href="browse?my-instances=My+Bots"><%= loginBean.translate("My Bots") %></a>
				<a href="login?browse-user-messages=true"><%= loginBean.translate("Messages") %></a>
				<a href="#" onclick="document.getElementById('logout').click()"><%= loginBean.translate("Sign Out") %></a>
				<form action="login" style="display:none">
					<%= proxy.proxyInput() %>
					<input type="submit" id="logout" name="logout"/>
				</form>
			</div>
		</div>
	<% } %>
	<% if (Site.COMMERCIAL) { %>
		<% if (!Site.DEDICATED) { %>
			<a href="freetrial.jsp" class="main-link2" style="float: left"> <img src="images/freetrial2.png" alt="" title="<%= loginBean.translate("Free Trial") %>"><span><%= loginBean.translate("Free Trial") %></span></a>
		<% } %>
	<% } else if (!Site.DEDICATED) {%>
		<a href="upgrade.jsp" class="main-link2" style="float: left"> <img src="images/dollar2.png" alt="" title="<%= loginBean.translate("Upgrade") %>"><span><%= loginBean.translate("Upgrade") %></span></a>
	<% } %>
</div>

<nav class="nav1">
	<!-- Translation -->
	<% if (loginBean.getShowLanguage()) { %>
		<div style="float:right">
			<ul>
				<li><span><a href="http://<%= Site.URL %>">en</a></span></li>
				<% for (String language : loginBean.getLanguages()) { %>
					<li><span><a href="http://<%= language + "." + Site.SERVER_NAME %>"><%= language %></a></span></li>
				<% } %>
				<li>
					<div id="google_translate_element" style="margin:2px"></div>
					<script type="text/javascript">
						function googleTranslateElementInit() {
							new google.translate.TranslateElement({pageLanguage: 'en', layout: google.translate.TranslateElement.InlineLayout.SIMPLE}, 'google_translate_element');
						}
					</script>
					<script type="text/javascript" src="//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit"></script>
				</li>
			</ul>
		</div>
	<% } %>

	<div class="container">
		<% if (domainBean.hasValidInstance()) { %>
			<div style="margin:auto;width:52px">
				<a href="domain?domain=<%= loginBean.getDomain().getId() %>" alt="Bots">
					<img src="<%= domainBean.getAvatarImage(loginBean.getDomain()) %>" alt="Logo"  style="width: 50px; margin-bottom: 10px; margin-top: 10px">
				</a>
			</div>
		<% } else { %>
			<div style="margin:auto;width:192px">
				<a href="index.jsp" alt="Bots">
					<% String domainAvatarImage = domainBean.getAvatarImage(loginBean.getDomain()); %>
					<img src="<%= Site.DEDICATED && !domainAvatarImage.equals(domainBean.getAvatarImage((Domain) null)) ? domainAvatarImage : "images/banner.png" %>" alt="Logo"  style="width: 190px; margin-bottom: 10px; margin-top: 10px">
				</a>
			</div>
		<% } %>

		<% if (!Site.DEDICATED || Site.CLOUD || loginBean.isLoggedIn()) { %>
			<ul class="items">
				<li><a href="browse?browse-type=Bot&browse=true" alt="Bots"><img src="images/bot1.png" alt="" title="Bots"> <span><%= loginBean.translate("Bots") %></span></a></li>
				<li><a href="browse?browse-type=Avatar&browse=true" alt="Bots"><img src="images/avatar2.png" alt="" title="Avatars"> <span><%= loginBean.translate("Avatars") %></span></a></li>
				<li><a href="browse?browse-type=Script&browse=true" alt="Bots"><img src="images/script1.png" alt="" title="Scripts"> <span><%= loginBean.translate("Scripts") %></span></a></li>
				<li><a href="browse?browse-type=Channel&browse=true" alt="Bots"><img src="images/chat1.png" alt="" title="Live Chat"> <span><%= loginBean.translate("Live Chat") %></span> </a></li>
				<li><a href="browse?browse-type=Forum&browse=true" alt="Bots"><img src="images/forum1.png" alt="" title="Forums"> <span><%= loginBean.translate("Forums") %></span></a></li>
				<li><a href="browse?browse-type=IssueTracker&browse=true" alt="Bots"><img src="images/issuetracker1.png" alt="" title="Issue Tracking"> <span><%= loginBean.translate("Issue Tracking") %></span></a></li>
				<li><a href="browse?browse-type=Graphic&browse=true" alt="Bots"><img src="images/graphic1.png" alt="" title="Graphics"> <span><%= loginBean.translate("Graphics") %></span></a></li>
				<% if (!loginBean.getDomainEmbedded()) { %>
					<li><a href="browse?browse-type=Domain&browse=true" alt="Bots"><img src="images/domain1.svg" alt="" title="Workspaces"> <span><%= loginBean.translate("Workspaces") %></span></a></li>
				<% } %>
			</ul>
		<% } %>
	</div>
</nav>

<% if (Site.ADULT && !loginBean.isLoggedIn() && !loginBean.isAgeConfirmed()) { %>
	<div id="topper">
	<div class="clearfix">
			<br/>
			<h3>Confirm Age</h3>
			<form action="login" method="post">
				<p>This website is restricted to adults 18 years or older.
				<input name="confirm-adult" type="checkbox" onchange="this.form.submit()">I am 18 years old or older</input>
				</p>
			</form>
		</div>
	</div>
<% } %>
	
<% if (Site.READONLY) { %>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<p style="color:red;font-weight:bold">** Server is current under maintenance, any changes will be lost **<p>
		</div>
	</div>
<% } %>
<% if (loginBean.isSuper() && BeanManager.manager().isDeadlocked()) { %>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<p style="color:red;font-weight:bold">** DEADLOCKED! **<p>
		</div>
	</div>
<% } %>
<% if (loginBean.isSuper() && AdminDatabase.outOfMemory) { %>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<p style="color:red;font-weight:bold">** OUT OF MEMORY! **<p>
		</div>
	</div>
<% } %>

<!-- banner scrolling -->
<script src="js/banner.js"></script>
