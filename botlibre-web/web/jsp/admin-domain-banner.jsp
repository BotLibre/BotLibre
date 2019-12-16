<%@page import="org.botlibre.web.bean.DomainBean"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean bean = loginBean.getBean(DomainBean.class);
%>

<% if (!embed) { %>
	<% bean.browseBannerHTML(out, proxy); %>
<% } %>
<div id="admin-topper-banner" align="left">
	<% if (!embed) { %>
		<div class="clearfix">
	<% } %>
	<div class="toolbar">
	<a href="<%= "domain?admin"  + proxy.proxyString() + bean.instanceString() %>"><img class="admin-banner-pic" src="images/admin.svg"></a>
	<a href="domain-users.jsp" title="<%= loginBean.translate("Configure users, and administrators of the workspace") %>"><img src="images/user1.png" class="admin-banner-pic"></a>
	<a href="domain-categories.jsp" title="<%= loginBean.translate("Configure categories for the workspace") %>"><img src="images/category1.png" class="admin-banner-pic"></a> 
	<a href="domain-tags.jsp" title="<%= loginBean.translate("Configure tags for the workspace") %>"><img src="images/tag.png" class="admin-banner-pic"></a>
	</div>
	<% if (!embed) { %>
		</div>
	<% } %>
</div>

<script>
	window.addEventListener("scroll", function() {scrollAdminTopper()});
	var isAdminScrolled = false;
	
	function scrollAdminTopper() {
		if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
			if (!isAdminScrolled) {
				isAdminScrolled = true;
				document.getElementById("admin-topper-banner").style.position = "fixed";
				document.getElementById("admin-topper-banner").style.width = "100%";
				document.getElementById("admin-topper-banner").style.top = "40px";
				document.getElementById("admin-topper-banner").style.zIndex = 10;
			}
		} else {
			if (isAdminScrolled) {
				isAdminScrolled = false;
				document.getElementById("admin-topper-banner").style.position = "relative";
				document.getElementById("admin-topper-banner").style.width = null;
				document.getElementById("admin-topper-banner").style.top = null;
			}
		}
	}
</script>
