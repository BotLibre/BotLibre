<%@page import="org.botlibre.web.bean.AvatarBean"%>
<%@page import="org.botlibre.web.admin.Avatar"%>
<%@page import="org.botlibre.web.bean.InstanceAvatarBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.admin.AvatarImage"%>
<%@page import="java.util.Map"%>
<%@page import="org.botlibre.emotion.EmotionalState"%>
<%@page import="org.botlibre.web.bean.AvatarInfo"%>
<%@page import = "org.botlibre.web.bean.ChatBean" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% InstanceAvatarBean bean = loginBean.getBean(InstanceAvatarBean.class); %>
<% AvatarBean avatarBean = loginBean.getBean(AvatarBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Avatar - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The avatar tab lets you choose your bot's avatar.") %><br/>
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
			<% if (!botBean.isConnected() && (botBean.getInstance() == null || !botBean.getInstance().isExternal())) { %>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<form action="instance-avatar" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<h3><%= loginBean.translate("Current Avatar") %></h3>
					<% if (botBean.getInstance().getInstanceAvatar() == null) { %>
						<img class="small-icon" src="<%= botBean.getAvatarThumb(botBean.getInstance()) %>"/>
					<% } else { %>
						<img class="small-icon" src="<%= avatarBean.getAvatarThumb(botBean.getInstance().getInstanceAvatar()) %>"/><br/>
						<p><%= botBean.getInstance().getInstanceAvatar().getName() %></p>
						<input name="editAvatar" type="submit" value="Edit"/>
						<input name="testAvatar" type="submit" value="Test"/>
					<% } %>
					<h3><%= loginBean.translate("Change Avatar") %></h3>
					<%= botBean.instanceInput() %>
					<input id="chooseAvatar" type="text" name="chooseAvatar" value="<%= bean.getAvatarText() %>"/><br/>
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
</body>
</html>
