<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@ page import = "org.botlibre.web.bean.UserBean" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	UserBean userBean = loginBean.getBean(UserBean.class);
%>

<% if (!loginBean.isMobile()) { %>
	<div id="admin-topper-banner" align="left">
		<div class="clearfix">
		<div class="toolbar">
			<span class="dropt">
				<div class="menu">
					<table>
						<% if (userBean.getUser() != null || userBean.isLoggedIn()) { %>
							<tr class="menuitem">
								<td>
									<a href="browse-user-message.jsp" class="menuitem" title="<%= loginBean.translate("User messages") %>">
										<img src="images/round_message.svg" class="menu"/> <%= loginBean.translate("Messages") %>
									</a>
								</td>
							</tr>
							<tr class="menuitem">
								<td>
									<a href="friendships.jsp" class="menuitem" title="<%= loginBean.translate("User friends") %>">
										<img src="images/friends.svg" class="menu"/> <%= loginBean.translate("Friends") %>
									</a>
								</td>
							</tr>
							<tr class="menuitem">
								<td>
									<a href="user-avatar.jsp" title="<%= loginBean.translate("Upload and configure images for user avatar emotions and responses") %>" class="menuitem">
										<img src="images/round_avatar.svg" class="menu"/> <%= loginBean.translate("Avatar") %>
									</a>
								</td>
							</tr>
							<tr class="menuitem">
								<td>
									<a href="user-voice.jsp" title="<%= loginBean.translate("Configure user avatar language and voice") %>" class="menuitem">
										<img src="images/round_voice.svg" class="menu"/> <%= loginBean.translate("Voice") %>
									</a>
								</td>
							</tr>
							<tr class="menuitem">
								<td>
									<a href="user-stats.jsp" class="menuitem" title="<%= loginBean.translate("User analytics") %>">
										<img src="images/stats-pic.svg" class="menu"/> <%= loginBean.translate("Stats") %>
									</a>
								</td>
							</tr>
						<% } %>
					</table>
				</div>
				<a href="admin-user.jsp" title="<%= loginBean.translate("Admin Console") %>"><img class="admin-banner-pic" src="images/admin.svg"></a>
			</span>
			<% if (userBean.getUser() != null && userBean.isLoggedIn()) { %>
				<a href="browse-user-message.jsp" class="shrinkhide" title="<%= loginBean.translate("User messages") %>"><img src="images/round_message.svg" class="admin-banner-pic"></a>
				<a href="friendships.jsp" class="shrinkhide" title="<%= loginBean.translate("User friends") %>"><img src="images/friends.svg" class="admin-banner-pic"></a>
				<a href="user-avatar.jsp" class="shrinkhide" title="<%= loginBean.translate("Configure user avatar appearance. Choose an animated avatar, or create your own") %>"><img src="images/round_avatar.svg" class="admin-banner-pic"></a> 
				<a href="user-voice.jsp" class="shrinkhide" title="<%= loginBean.translate("Configure your bot's language and voice") %>"><img src="images/round_voice.svg" class="admin-banner-pic"></a>
				<a href="user-stats.jsp" class="shrinkhide" title="<%= loginBean.translate("User analytics") %>"><img src="images/stats-pic.svg" class="admin-banner-pic"></a>
			<% } %>
			</div>
		</div>
	</div>
<% } else { %>
	<div id="admin-topper-banner" align="left">
		<div class="clearfix">
			<div class="toolbar">
				<a href="browse-user-message.jsp" title="<%= loginBean.translate("User messages") %>"><img src="images/round_message.svg" class="admin-banner-pic"></a>
				<a href="friendships.jsp" title="<%= loginBean.translate("User friends") %>"><img src="images/friends.svg" class="admin-banner-pic"></a>
				<a href="user-avatar.jsp" title="<%= loginBean.translate("Upload and configure images for user avatar emotions and responses") %>"><img src="images/round_avatar.svg" class="admin-banner-pic"></a> 
				<a href="user-voice.jsp" title="<%= loginBean.translate("Configure user avatar language and voice") %>"><img src="images/round_voice.png" class="admin-banner-pic"></a>
				<a href="user-stats.jsp" title="<%= loginBean.translate("User analytics") %>"><img src="images/stats-pic.svg" class="admin-banner-pic"></a>
			</div>
		</div>
	</div>
<% } %>

<script>
	window.addEventListener("scroll", function() {scrollAdminTopper()});
	var isAdminScrolled = false;
	
	function scrollAdminTopper() {
		if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
			if (!isAdminScrolled) {
				isAdminScrolled = true;
				var adminTopper = document.getElementById("admin-topper-banner");
				adminTopper.style.position = "fixed";
				adminTopper.style.width = "100%";
				adminTopper.style.top = "40px";
				adminTopper.style.zIndex = 10;

				var chatlogTopper = document.getElementById("chatlog-topper-banner");
				if (chatlogTopper != null) {
					chatlogTopper.style.position = "fixed";
					chatlogTopper.style.width = "100%";
					chatlogTopper.style.top = "77px";
					chatlogTopper.style.zIndex = 10;
				}
			}
		} else {
			if (isAdminScrolled) {
				isAdminScrolled = false;
				var adminTopper = document.getElementById("admin-topper-banner");
				adminTopper.style.position = "relative";
				adminTopper.style.width = null;
				adminTopper.style.top = null;

				var chatlogTopper = document.getElementById("chatlog-topper-banner");
				if (chatlogTopper != null) {
					chatlogTopper.style.position = "relative";
					chatlogTopper.style.width = null;
					chatlogTopper.style.top = null;
				}
			}
		}
	}
</script>
