<%@page import="org.botlibre.web.bean.AvatarBean"%>
<%@page import="org.botlibre.web.admin.Avatar"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.admin.AvatarImage"%>
<%@page import="java.util.Map"%>
<%@page import="org.botlibre.emotion.EmotionalState"%>
<%@page import="org.botlibre.web.bean.AvatarInfo"%>
<%@page import = "org.botlibre.web.bean.ChatBean" %>
<%@page import="org.botlibre.web.bean.UserBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<% 
	loginBean = proxy.checkLoginBean(loginBean); 
%>
<% AvatarBean avatarBean = loginBean.getBean(AvatarBean.class); %>
<% UserBean userBean = loginBean.getBean(UserBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Avatar - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<div class="clearfix">
				<a href="<%= "login?view-user=" + loginBean.getUserId() %>"><%= loginBean.getUserId() %></a>
			</div>
		</div>
	</div>
	<jsp:include page="admin-user-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The avatar tab lets you choose your user's avatar.") %><br/>
					<%= loginBean.translate("An avatar is the physical representation of your bot, and can include images, video, audio, and animation.") %>
					<%= loginBean.translate("You can choose an avatar from our open avatar directory, or create your own.") %><br/>
					<%= loginBean.translate("To use an avatar from our avatar directory enter its name or id and click 'Save'.") %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-avatars.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=682689"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/avatar1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Select an avatar or enter its name or id, or create your own avatar.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Avatar") %>
			</h1>
			<jsp:include page="error.jsp"/>
			
			<% if (!loginBean.isLoggedIn()) { %>
				<p style="color:#E00000;">
					<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your avatar") %>.
				</p>
			<% } else if (userBean.getUser() != null) { %>
				<form action="user-avatar" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<h3><%= loginBean.translate("Current Avatar") %></h3>
					<% if (userBean.getUser().getInstanceAvatar() == null) { %>
						<img class="small-icon" src="<%= loginBean.getAvatarImage(userBean.getUser()) %>"/>
					<% } else { %>
						<img id="<%= userBean.getUser().getInstanceAvatar() %>" class="small-icon" src="<%= avatarBean.getAvatarThumb(userBean.getUser().getInstanceAvatar()) %>"/><br/>
						<p><%= userBean.getUser().getInstanceAvatar().getName() %></p>
						<input name="editAvatar" type="submit" value="Edit"/>
						<input name="testAvatar" type="submit" value="Test"/>
					<% } %>
					<h3><%= loginBean.translate("Change Avatar") %></h3>
					<input id="chooseAvatar" type="text" name="chooseAvatar" value="<%= userBean.getAvatarText() %>"/><br/>
					<script>
					var templateauto = $( "#chooseAvatar" );
					templateauto.autocomplete({
						source: [
							<% for (Avatar avatar : avatarBean.getAllLinkableInstances()) {%>
								{
									value: "<%= avatar.getId() %> : <%= avatar.getName() %>",
									label: "<%= avatar.getName() %>",
									description: "<%= avatar.getLicense() %>",
									image: "<%= avatarBean.getAvatarThumb(avatar) %>"
								},
							<% } %>
							],
							minLength: 0,
							maxLength: 10
						});
					templateauto.data( "ui-autocomplete" )._renderItem = function( ul, item ) {
						var inner_html = '<a><table><tr><td><img style="border: 1px solid #d5d5d5;" height="80" src="'
								+ item.image + '"></td><td valign="top">'
								+ item.label + '<br/><span style="font-size:12px">'
								+ item.description + '</span></td></tr></table></a>';
						return $( "<li></li>" )
							.data( "item.autocomplete", item )
							.append(inner_html)
							.appendTo( ul );
					};
					templateauto.on('focus', function(event) {
						var self = this;
						$(self).autocomplete("search", "");
					});
					</script>
					<input name="saveAvatar" type="submit" value="<%= loginBean.translate("Save") %>"/>
					<input name="createAvatar" type="submit" value="<%= loginBean.translate("Create") %>"/>
				</form>
				<br/>
				<a target="_blank" href="browse?browse-type=Avatar&domain=1"><%= loginBean.translate("Click here to browse the avatar directory.") %></a><br/><br/>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
	<% proxy.clear(); %>
</body>
</html>
