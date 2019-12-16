<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.ForumPostBean.EditorType"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumPostBean"%>
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
	<title>Create Forum Reply<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Create a new forum reply"/>	
	<meta name="keywords" content="create, post, forum, reply"/>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% if (forumPostBean.getEditorType() == EditorType.WYSIWYG) { %>
		<jsp:include page="tinymce.jsp"/>		
	<% } %>
	<script>
		location.hash = "#anchor";
	</script>
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
	<h1><%= loginBean.translate("Reply") %></h1>
	<% boolean error = loginBean.getError() != null  || forumPostBean.isPreview() || forumPostBean.isEditorChange(); %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to reply") %>.
		</p>
	<% } else if (forumPostBean.getInstance() == null) { %>
		<%= loginBean.translate("No forum post selected") %>
	<% } else { %>
		<table style="width:100%">
			<tr style="width:100%">
				<td align="left">
					<a href="<%= "login?view-user=" + forumPostBean.encodeURI(forumPostBean.getInstance().getCreator().getUserId()) + proxy.proxyString() %>">
						<img src="<%= forumPostBean.getAvatarThumb(forumPostBean.getInstance()) %>" class="user-thumb"/>
					</a>
				</td>
				<td style="width:100%" align="left" valign="top">
					<h1 style="margin-left:10px"><%= forumPostBean.getInstance().getTopic() %></h1>
				</td>
			</tr>
		</table>
		<table>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<span class="menu" style="font-weight:bold">
						<%= loginBean.translate("by") %> <a class="user" href="<%= "login?view-user=" + forumPostBean.encodeURI(forumPostBean.getInstance().getCreator().getUserId()) + proxy.proxyString() %>">
						<%= forumPostBean.getInstance().getCreator().getUserHTML() %></a> 
						<%= loginBean.translate("posted") %> <%= Utils.displayTimestamp(forumPostBean.getInstance().getCreationDate())%>
					</span>
					<p>
					<%= forumPostBean.getInstance().getTextDetails() %>
					</p>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<form action="forum-post" method="get" class="message">
						<%= proxy.proxyInput() %>
						<%= forumPostBean.instanceInput() %>
						<%= forumPostBean.getInstance().printReplies(forumPostBean, proxy) %>
					</form>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<a id="anchor"></a>
					<span class="menu">
					<% if (!forumPostBean.getInstance().getTags().isEmpty()) { %>
						<%= loginBean.translate("Tags") %>: <%= forumPostBean.getInstance().getTagsString() %><br/>
					<% } %>
					<%= loginBean.translate("Posted") %>: <%= forumPostBean.getInstance().getCreationDateString() %><br/>
					<% if (forumPostBean.getInstance().getUpdatedDate() != null) { %>
						<%= loginBean.translate("Updated") %>: <%= forumPostBean.getInstance().getUpdatedDateString() %><br/>
					<% } %>
					</span>
					<% if (forumPostBean.getInstance().isFlagged()) { %>
						<p style="color:red;font-weight:bold;"><%= loginBean.translate("This post has been flagged for") %> "<%= forumPostBean.getInstance().getFlaggedReason() %>" <%= loginBean.translate("by") %> <%= forumPostBean.getInstance().getFlaggedUser() %>.</p>
					<% } %>
				</td>
			</tr>
		</table>
		<form action="forum-post" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= forumPostBean.instanceInput() %>
			<%= loginBean.translate("Details") %>
			<script>
			    var saveSource = function() { }
			</script>
			<select name="reply-editor" title="Choose editor to edit post with.  Posts can be written in HTML or markup text." onchange="saveSource(); this.form.submit()">
				<option value="WYSIWYG" <%= forumPostBean.isEditorTypeSelected(EditorType.WYSIWYG) %>>WYSIWYG</option>
				<option value="Markup" <%= forumPostBean.isEditorTypeSelected(EditorType.Markup) %>>Markup</option>
				<option value="HTML" <%= forumPostBean.isEditorTypeSelected(EditorType.HTML) %>>HTML</option>
			</select><br/>
			<div id="details-div" style="display:inline;background-color:#fff">
				<script type="text/javascript">
					SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
					var psdk = new SDKConnection();			
					<% if (loginBean.isLoggedIn()) { %>
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
					<div id="editor" style="height:350px"><%= (!error) ? "" : forumPostBean.getReply().getDetails().getEditDetails() %></div>
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
					<textarea autofocus id="details" name="details"
							<%= forumPostBean.getEditorType() == EditorType.HTML ? "" : "style='height:350px'" %>
							<%= forumPostBean.getEditorType() == EditorType.Markup ? "" : "class='hidden'" %>><%= (!error) ? "" : forumPostBean.getReply().getDetails().getEditDetails() %></textarea><br/>
				</div>
			</div>
			<div id="post-details" style="display:inline">
				<input name="preview-reply" type="submit" value="Preview" onclick="saveSource()"/><br/>
				<p>
				<% if (forumPostBean.isPreview()) { %>
					<br/>
					<%= forumPostBean.getReply().getTextDetails() %><br/>
				<% } %>
				</p>
				<input name="replyToParent" type="checkbox" <%= (forumPostBean.getInstance().getParent() == null) ? "" : "checked" %> title="Reply to the parent post, instead of creating a nested reply"><%= loginBean.translate("Reply to Parent") %></input><br/>
				<input id="ok" name="create-reply" type="submit" value="<%= loginBean.translate("Reply") %>" onclick="saveSource()"/>
				<input id="cancel" name="cancel-reply" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</div>
		</form>
		<script>
			document.getElementById('details').focus();
		</script>
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