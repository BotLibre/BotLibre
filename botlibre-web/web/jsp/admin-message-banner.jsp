<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@ page import = "org.botlibre.web.bean.UserBean" %>
<%@page import="org.botlibre.web.bean.UserToUserMessageBean"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	UserBean userBean = loginBean.getBean(UserBean.class);
	UserToUserMessageBean bean = loginBean.getBean(UserToUserMessageBean.class);
%>

	<div id="admin-message-banner" align="left" style="background-color:#fff;border-bottom:1px solid #d8d8d8;">
		<div class="clearfix" style="background-color:#fff;">
		<div class="toolbar">
			<span class="dropt">
				<div class="menu" style="top:36px;">
					<table>
						<% if (userBean.getUser() != null || userBean.isLoggedIn()) { %>
							<tr class="menuitem">
								<td>
									<a href="#" onclick="$('#dialog-delete-user-to-user-messages').dialog('open'); return false;" class="menuitem" title="<%= loginBean.translate("Delete all messages with this user") %>">
										<img src="images/remove.svg" class="menu" style="margin-right:4px;"/><%= loginBean.translate("Delete Conversation") %>
									</a>
								</td>
							</tr>
						<% } %>
					</table>
				</div>
				<img id="menu" class="toolbar" src="images/menu.png" style="margin:unset;padding:unset;">
			</span>
			<% if (userBean.getUser() != null && userBean.isLoggedIn()) { %>
				<span style="margin:0px;display:inline-block;">
					<form id="user-message-action-form" action="browse-user-to-user-messages" method="get" type="submit" class="search">
						<%= loginBean.postTokenInput() %>
						<span class="menu">
							<div id="search-filter-div" class="search-div">
								<span id="search-filter-span" class="search-span"><%= loginBean.translate("Filter") %></span>
								<input id="searchtext" name="filter" type="text" value="<%= bean.getFilter() %>" title="<%= loginBean.translate("Filter by topic and message text") %>"/>
							</div>
							<div style="display:inline-block;margin:2px;">
								<a href="browse-user-to-user-messages?view-message=<%= bean.getViewMessage() %>&page=<%= (bean.getMessagePage() + 1) %>" id="user-messages-previous-page-up" style="display:none;" title="Display previous user messages on page up.">
									<img id="menu" class="toolbar" src="images/up2.svg" style="margin:unset;padding:unset;">
								</a>
							</div>
							<div style="display:inline-block;margin:2px;">
								<a href="browse-user-to-user-messages?view-message=<%= bean.getViewMessage() %>&page=<%= (bean.getMessagePage() - 1) %>" id="user-messages-next-page-down" style="display:none;" title="Display next user messages on page down.">
									<img id="menu" class="toolbar" src="images/down2.svg" style="margin:unset;padding:unset;">
								</a>
							</div>
							<div>
								<input id="view-message" name="view-message" type="hidden" value="<%= bean.getViewMessage() %>" />
							</div>
						</span>
					</form>
				</span>
			<% } %>
			</div>
		</div>
	</div>

<script>
	window.addEventListener("scroll", function() { scrollAdminMessageTopper() });
	var isAdminMessageScrolled = false;
	function scrollAdminMessageTopper() {
		if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
			if (!isAdminMessageScrolled) {
				isAdminMessageScrolled = true;
				var adminMessageTopper = document.getElementById("admin-message-banner");
				adminMessageTopper.style.position = "fixed";
				adminMessageTopper.style.width = "100%";
				adminMessageTopper.style.top = "77px";
				adminMessageTopper.style.zIndex = 10;
			}
		} else {
			if (isAdminMessageScrolled) {
				isAdminMessageScrolled = false;
				var adminMessageTopper = document.getElementById("admin-message-banner");
				adminMessageTopper.style.position = "relative";
				adminMessageTopper.style.width = null;
				adminMessageTopper.style.top = null;
			}
		}
	}
</script>
