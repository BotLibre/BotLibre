<%@page import="org.botlibre.web.admin.AvatarMedia"%>
<%@page import="org.botlibre.web.bean.AvatarBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.botlibre.emotion.EmotionalState"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AvatarBean bean = loginBean.getBean(AvatarBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Avatar Editor<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Add and edit the avatar's media files, and metadata tags (emotions, actions, and poses)"/>	
	<meta name="keywords" content="avatar, editor"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<% try { %>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<jsp:include page="avatar-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-avatar-banner.jsp"/>
	<div id="avatar-topper-banner" align="left" style="position:relative;z-index:10;">
		<div class="clearfix">
		<div class="toolbar">
			<span class="dropt">
				<div class="menu">
					<table>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#save-icon').click(); return false;" title="<%= loginBean.translate("Save current changes") %>" class="menuitem">
								<img src="images/save.svg" class="menu"/> <%= loginBean.translate("Save Avatar") %>
								<input name="save-media" type="hidden"></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all media") %>" class="menuitem">
								<img src="images/select.svg" class="menu"/> <%= loginBean.translate("Select All") %>
								<input name="select-all-media" type="hidden"></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Delete selected media") %>" class="menuitem">
								<img src="images/remove3.svg" class="menu"/> <%= loginBean.translate("Delete Avatar") %>
								<input name="delete-media" type="hidden"></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="GraphicsUploader.openUploadDialog(document.getElementById('avatar-media-upload'), 'Upload Media', null, null, null, true); return false;" title="<%= loginBean.translate("Upload images, video, or sound files for the avatar") %>" class="menuitem">
								<img src="images/upload.svg" class="menu"/> <%= loginBean.translate("Upload media") %></a>
							</td>
						</tr>
					</table>
				</div>
				<img class="admin-banner-pic" src="images/menu1.png">
			</span>
			<a href="#" id="save-icon-link" onclick="$('#save-icon').click(); return false;" title="<%= loginBean.translate("Save current avatar changes") %>"><img src="images/save.svg" class="admin-banner-pic"/></a>
			<a href="#" id="select-icon-link" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all media") %>"><img src="images/select.svg" class="admin-banner-pic"/></a>
			<a href="#" id="remove-icon-link" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Delete selected media") %>"><img src="images/remove3.svg" class="admin-banner-pic"/></a>
			<a href="#" id="import-icon-link" onclick="GraphicsUploader.openUploadDialog(document.getElementById('avatar-media-upload'), 'Upload Media', null, null, null, true); return false;" title="<%= loginBean.translate("Upload images, video, or sound files for the avatar") %>"><img src="images/upload.svg" class="admin-banner-pic"/></a>
		</div>
		</div>
	</div>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The avatar editor allows you to upload or import images, video, and audio files for your avatar, and tag them with emotions, actions, and poses.") %>
					<%= loginBean.translate("You can add as many images, videos, or audio as you want, and they will be randomly selected if not tagged.") %>
					<%= loginBean.translate("If tagged, they will only be used when the bot expresses the tagged emotion, action, or pose.") %><br/>
					<%= loginBean.translate("You should have at least a 'talking' pose tagged, this will be displayed when the bot is talking.") %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-avatars.jsp"><%= loginBean.translate("Docs") %></a> : <a target="_blank" href="https://www.botlibre.com/forum-post?id=682689"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (bean.getInstance() == null) { %>
		No avatar selected
	<% } else if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<h1>
			<span class="dropt-banner">
				<img src="images/analytic-media.svg" class="admin-banner-pic">
				<div>
					<p class="help">
						<%= loginBean.translate("Configure, import, and upload media for the avatar.") %>
					</p>
				</div>
			</span> Avatar Editor
		</h1>
		<form action="avatar" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= bean.instanceInput() %>
			<% if (bean.isAdmin()) { %>
				<input id="save-icon" class="icon" type="submit" name="save-media"  style="display:none;" value="" title="<%= loginBean.translate("Save the current changes") %>">
				<input id="select-icon" class="icon" type="submit" name="select-all-media" style="display:none;" value="" title="<%= loginBean.translate("Select all media") %>">
				<input id="remove-icon" class="icon" type="submit" name="delete-media" style="display:none;" value="" title="<%= loginBean.translate("Delete the selected media") %>">
				<input id="import-icon"onclick="GraphicsUploader.openUploadDialog(document.getElementById('avatar-media-upload'), '<%= loginBean.translate("Upload Media") %>', null, null, null, true); return false;" class="icon" type="submit" name="upload" style="display:none;" value="" title="<%= loginBean.translate("Upload images, video, or sound files for the avatar") %>"/>
				<br/>
			<% } %>
			<table>
				<tr>
					<td></td>
					<td>
						<span class="dropt-banner">
							<img id="help-mini" src="images/help.svg"/>
							<div>
								<%= loginBean.translate("Upload a background image to overlay video on (this can reduce the video flicker).") %>
							</div>
						</span>
						<%= loginBean.translate("Background Image") %><br/>
						<% if (bean.getInstance().getBackground() == null) { %>
							<span class="menu"><%= loginBean.translate("none") %></span>
						<% } else { %>
							<img src="<%= bean.getInstance().getBackground().getFileName() %>" alt="<%= bean.getInstance().getBackground().getFileName() %>" style="max-height:150px;max-width:150px;"/>
						<% } %>
					</td>
					<td>
						<% if (bean.isAdmin()) { %>
							<input id="upload-background" onclick="GraphicsUploader.openUploadDialog(document.getElementById('avatar-background-upload'), 'Upload Background'); return false;" class="icon import-icon" type="submit" name="upload" value="" title="<%= loginBean.translate("Upload a background image to overlay video on (this can reduce the video flicker)") %>"/>
							<input id="avatar-remove-icon" class="icon remove-icon" type="submit" name="delete-background" value="" title="Clear the background image">
						<% } %>
					</td>
				</tr>
				<% List<AvatarMedia> avatars = bean.getInstance().getMedia(); %>
				<% if (!avatars.isEmpty()) { %>
						<% for (AvatarMedia avatar : avatars) { %>
							<tr><td colspan="3"><hr></td></tr>
							<tr>
								<td><input type=checkbox name=<%= avatar.getMediaId() %> <%= bean.isSelectAll() ? "checked" : "" %>></td>
								<td>
									<% if (avatar.isVideo()) { %>
										<video src="<%= bean.getMediaFileName(avatar) %>"style="max-height:150px;max-width:150px;" controls></video>
										<div style="max-width:180px;overflow:hidden;">
											<span class="menu"><%= avatar.getName() %> : <%= avatar.getType() %></span>
										</div>
									<% } else if (avatar.isAudio()) { %>
										<audio src="<%= bean.getMediaFileName(avatar) %>" style="max-height:150px;max-width:250px;" controls></audio>
										<div style="max-width:180px;overflow:hidden;">
												<span class="menu"><%= avatar.getName() %> : <%= avatar.getType() %></span>
											</div>
									<% } else { %>
										<span class="dropt-banner">
											<div style="position:absolute;right:40px">
												<img src="<%= bean.getMediaFileName(avatar) %>" style="max-height:400px;max-width:400px;min-width:200px"/>
											</div>
											<img src="<%= bean.getMediaFileName(avatar) %>" alt="<%= avatar.getMediaId() %>" style="max-height:150px;max-width:150px;"/>
											<div style="max-width:180px;overflow:hidden;">
												<span class="menu"><%= avatar.getName() %> : <%= avatar.getType() %></span>
											</div>
										</span>
									<% } %>
								</td>
								<td valign="top">
									<table>
										<tr>
											<td>
												<span class="dropt-banner">
													<img id="help-mini" src="images/help.svg"/>
													<div>
														<%= loginBean.translate("Comma seperated list of emotions to trigger the media.") %>
													</div>
												</span>
												<%= loginBean.translate("Emotions") %>
											</td>
											<td>
												<input id="<%= "emotions-" + avatar.getMediaId() %>" name="<%= "emotions:" + avatar.getMediaId() %>" type="text" value="<%= avatar.getEmotions().toLowerCase() %>" title="<%= loginBean.translate("Comma seperated list of emotions to trigger the media.") %>" /><br/>
												<script>
												$(function() {
													var emotions = [<%= bean.getAllEmotionString() %>];
													multiDropDown("#<%= "emotions-" + avatar.getMediaId() %>", emotions);
												});
												</script>
											</td>
										</tr><tr>
											<td>
												<span class="dropt-banner">
													<img id="help-mini" src="images/help.svg"/>
													<div>
														<%= loginBean.translate("Comma seperated list of actions to trigger the media.") %>
													</div>
												</span>
												<%= loginBean.translate("Actions") %>
											</td>
											<td>
												<input id="<%= "actions-" + avatar.getMediaId() %>" name="<%= "actions:" + avatar.getMediaId() %>" type="text" value="<%= avatar.getActions() %>" title="<%= loginBean.translate("Comma seperated list of actions to trigger the media.") %>" /><br/>
												<script>
												$(function() {
													var actions = [<%= bean.getAllActionsString() %>];
													multiDropDown("#<%= "actions-" + avatar.getMediaId() %>", actions);
												});
												</script>
											</td>
										</tr><tr>
											<td>
												<span class="dropt-banner">
													<img id="help-mini" src="images/help.svg"/>
													<div>
														<%= loginBean.translate("Comma seperated list of poses to trigger the media.") %>
													</div>
												</span>
												<%= loginBean.translate("Poses") %>
											</td>
											<td>
												<input id="<%= "poses-" + avatar.getMediaId() %>" name="<%= "poses:" + avatar.getMediaId() %>" type="text" value="<%= avatar.getPoses() %>" title="<%= loginBean.translate("Comma seperated list of poses to trigger the media.") %>" /><br/>
												<script>
												$(function() {
													var poses = [<%= bean.getAllPosesString() %>];
													multiDropDown("#<%= "poses-" + avatar.getMediaId() %>", poses);
												});
												</script>
											</td>
										</tr><tr>
											<td>
												<span class="dropt-banner">
													<img id="help-mini" src="images/help.svg"/>
													<div>
														<%= loginBean.translate("A talking video is displayed when the avatar is talking.") %>
														<%= loginBean.translate("At least one talking video is required for an animated avatar.") %>
													</div>
												</span>
												<input type=checkbox name=<%= "talking:" + avatar.getMediaId() %> <%= avatar.getTalking() ? "checked" : "" %>> <%= loginBean.translate("Talking") %>
											</td>
										</tr><tr>
											<td>
												<span class="dropt-banner">
													<img id="help-mini" src="images/help.svg"/>
													<div>
														<%= loginBean.translate("High definition media will be automatically used when the avatar width is greater than 360 pixels.") %>
													</div> 
												</span>
												<input type=checkbox name=<%= "hd:" + avatar.getMediaId() %> <%= avatar.getHD() ? "checked" : "" %>> <%= loginBean.translate("HD") %>
											</td>
										</tr>
									</table>
								</td>
							</tr>
						<% } %>
				<% } %>
			</table>
		</form>
		</div>
		
		<form id="avatar-media-upload" action="avatar-media-upload" method="post" enctype="multipart/form-data" class="message" style="display:none">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
		</form>
	
		<form id="avatar-background-upload" action="avatar-background-upload" method="post" enctype="multipart/form-data" class="message" style="display:none">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
		</form>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<%
	} catch (Exception exception) {
		loginBean.setError(exception);
	}
%>
<% proxy.clear(); %>
</body>
</html>
