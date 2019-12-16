<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumPostBean"%>
<%@page import="org.botlibre.web.bean.ForumPostBean.EditorType"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean forumBean = loginBean.getBean(ForumBean.class);
	ForumPostBean forumPostBean = loginBean.getBean(ForumPostBean.class);
	forumPostBean.setForumBean(forumBean);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Forum Post<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% if (forumPostBean.getEditorType() == EditorType.WYSIWYG) { %>
		<jsp:include page="tinymce.jsp"/>
	<% } %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="forum-banner.jsp"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="about">
<% } %>
	<h1><%= forumPostBean.getInstance() == null ? loginBean.translate("Edit Post") : forumPostBean.getInstance().getTopic() %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!forumPostBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a post") %>.
		</p>
	<% } else if (forumPostBean.getEditInstance() == null) { %>
		<p><%= loginBean.translate("No post selected.") %></p>
	<% } else if (!forumPostBean.isAdmin()) { %>
		<p><%= loginBean.translate("Must be post admin.") %></p>
	<% } else { %>
		
		<form action="forum-post" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= forumPostBean.instanceInput() %>
			<%= loginBean.translate("Topic") %><br/>
			<input name="topic" type="text" value="<%= forumPostBean.getEditInstance().getTopic() %>"/><br/>
			<%= loginBean.translate("Details") %> 
			<script>
			    var saveSource = function() { }
			</script>
			<select name="edit-editor" title="<%= loginBean.translate("Choose editor to edit post with.  Posts can be written in HTML or markup text.") %>" onchange="saveSource(); this.form.submit()">
				<option value="WYSIWYG" <%= forumPostBean.isEditorTypeSelected(EditorType.WYSIWYG) %>><%= loginBean.translate("WYSIWYG") %></option>
				<option value="Markup" <%= forumPostBean.isEditorTypeSelected(EditorType.Markup) %>><%= loginBean.translate("Markup") %></option>
				<option value="HTML" <%= forumPostBean.isEditorTypeSelected(EditorType.HTML) %>><%= loginBean.translate("HTML") %></option>
			</select><br/>
			<div id="details-div" style="display:inline;background-color:#fff">
				<script type="text/javascript">
					SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
					var psdk = new SDKConnection();			
					<% if (loginBean.getUser() != null) { %>
						var puser = new UserConfig();
						puser.user = '<%= loginBean.getUser().getUserId() %>';
						puser.token = '<%= loginBean.getUser().getToken() %>';
						psdk.user = puser;
					<% } %>
					var forum = "<%= forumBean.getInstanceId() %>";
					psdk.error = function(error) {
						console.log(error);
						document.getElementById("error-message").innerHTML = error;
						$( "#dialog-error" ).dialog( "open" );
					}
				</script>
				<% if (forumPostBean.getEditorType() == EditorType.Markup) { %>
					<jsp:include page="markup-toolbar.jsp"/>
				<% } else if (forumPostBean.getEditorType() == EditorType.HTML) { %>
					<div id="editor" style="height:350px"><%= forumPostBean.getEditInstance().getDetails().getEditDetails() %></div>
					<script>
					    var editor = ace.edit("editor");
					    editor.getSession().setMode("ace/mode/html");
					    setTimeout(function () {
						    editor.focus();
					    }, 500);
					    saveSource = function() {
					    	document.getElementById("details").value = editor.getSession().getValue();
					    }
					</script>
					<jsp:include page="shortcuts.jsp"/>
				<% } %>
				<div id="details-text-div" style="display:inline">
					<textarea autofocus id="details" name="details" onMouseOut="javascript:return false;"
							<%= forumPostBean.getEditorType() == EditorType.HTML ? "" : "style='height:350px'" %>
							<%= forumPostBean.getEditorType() == EditorType.Markup ? "" : "class='hidden'" %>><%= forumPostBean.getInstance().getDetails().getEditDetails() %></textarea><br/>
				</div>
			</div>
			<div id="post-details" style="display:inline">
				<%= loginBean.translate("Tags") %><br/>
				<input id="tags" name="tags" type="text" value="<%= forumPostBean.getEditInstance().getTagsString() %>" title="<%= loginBean.translate("Comma seperated list of tags to categories the post under") %>"/><br/>
				<script>
		    	$(function() {
			        var availableTags = [<%= forumPostBean.getAllTagsString() %>];
			        multiDropDown("#tags", availableTags);
			    });
				</script>
				<% if (forumBean.isAdmin()) { %>
					<input name="isFeatured" type="checkbox" <% if (forumPostBean.getEditInstance().isFeatured()) { %>checked<% } %> ><%= loginBean.translate("Is Featured") %></input><br/>
				<% } %>
				<input name="preview-edit" type="submit" value="<%= loginBean.translate("Preview") %>" onclick="saveSource()"/><br/>
				<br/>
				<% if (forumPostBean.isPreview()) { %>
					<h1><%= forumPostBean.getEditInstance().getTopic() %></h1>
					<p>
					<%= forumPostBean.getEditInstance().getTextDetails() %>
					</p>
				<% } %>
				<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>" onclick="saveSource()"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
			</div>
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