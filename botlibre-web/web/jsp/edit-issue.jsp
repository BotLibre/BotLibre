<%@page import="org.botlibre.web.bean.IssueBean.EditorType"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>
<%@page import="org.botlibre.web.bean.IssueBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	IssueTrackerBean issueTrackerBean = loginBean.getBean(IssueTrackerBean.class);
	IssueBean bean = loginBean.getBean(IssueBean.class);
	bean.setIssueTrackerBean(issueTrackerBean);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Issue<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% if (bean.getEditorType() == EditorType.WYSIWYG) { %>
		<jsp:include page="tinymce.jsp"/>
	<% } %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="issuetracker-banner.jsp"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="about">
<% } %>
	<h1><%= bean.getInstance() == null ? loginBean.translate("Edit Issue") : bean.getInstance().getTitle() %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit an issue") %>.
		</p>
	<% } else if (bean.getEditInstance() == null) { %>
		<p><%= loginBean.translate("No issue selected.") %></p>
	<% } else if (!(bean.isAdmin() || bean.isSuper())) { %>
		<p><%= loginBean.translate("Must be issue admin.") %></p>
	<% } else { %>
		
		<form action="issue" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<%= loginBean.translate("Title") %><br/>
			<input name="title" type="text" value="<%= bean.getEditInstance().getTitle() %>"/><br/>
			
			<table style="border-spacing: 0px;">
				<tr>
					<td><%= loginBean.translate("Type") %></td>
					<td> </td>
					<td>
						<select id="type" name="type" title="<%= loginBean.translate("The type of issue.") %>">
							<option value="Issue" <%= bean.isTypeSelected("Issue") %>><%= loginBean.translate("Issue") %></option>
							<option value="Bug" <%= bean.isTypeSelected("Bug") %>><%= loginBean.translate("Bug") %></option>
							<option value="Feature" <%= bean.isTypeSelected("Feature") %>><%= loginBean.translate("Feature") %></option>
							<option value="Task" <%= bean.isTypeSelected("Task") %>><%= loginBean.translate("Task") %></option>
							<option value="ServiceRequest" <%= bean.isTypeSelected("ServiceRequest") %>><%= loginBean.translate("Service Request") %></option>
						</select>
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Priority") %></td>
					<td> </td>
					<td>
						<select id="priority" name="priority" title="<%= loginBean.translate("The priority of the issue.") %>">
							<option value="Low" <%= bean.isPrioritySelected("Low") %>><%= loginBean.translate("Low") %></option>
							<option value="Medium" <%= bean.isPrioritySelected("Medium") %>><%= loginBean.translate("Medium") %></option>
							<option value="High" <%= bean.isPrioritySelected("High") %>><%= loginBean.translate("High") %></option>
							<option value="Sever" <%= bean.isPrioritySelected("Sever") %>><%= loginBean.translate("Sever") %></option>
						</select>
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Status") %></td>
					<td> </td>
					<td>
						<select id="status" name="status" title="<%= loginBean.translate("The status of the issue.") %>">
							<option value="Open" <%= bean.isStatusSelected("Open") %>><%= loginBean.translate("Open") %></option>
							<option value="Rejected" <%= bean.isStatusSelected("Rejected") %>><%= loginBean.translate("Rejected") %></option>
							<option value="Deferred" <%= bean.isStatusSelected("Deferred") %>><%= loginBean.translate("Deferred") %></option>
							<option value="Duplicate" <%= bean.isStatusSelected("Duplicate") %>><%= loginBean.translate("Duplicate") %></option>
							<option value="Assigned" <%= bean.isStatusSelected("Assigned") %>><%= loginBean.translate("Assigned") %></option>
							<option value="Implemented" <%= bean.isStatusSelected("Implemented") %>><%= loginBean.translate("Implemented") %></option>
							<option value="Closed" <%= bean.isStatusSelected("Closed") %>><%= loginBean.translate("Closed") %></option>
						</select>
					</td>
				</tr>
			</table>
			
			<%= loginBean.translate("Details") %><br/>
			<script>
				var saveSource = function() { }
			</script>
			<select name="edit-editor" title="<%= loginBean.translate("Choose editor to edit issue with.  Issues can be written in HTML or markup text.") %>" onchange="saveSource(); this.form.submit()">
				<option value="WYSIWYG" <%= bean.isEditorTypeSelected(EditorType.WYSIWYG) %>><%= loginBean.translate("WYSIWYG") %></option>
				<option value="Markup" <%= bean.isEditorTypeSelected(EditorType.Markup) %>><%= loginBean.translate("Markup") %></option>
				<option value="HTML" <%= bean.isEditorTypeSelected(EditorType.HTML) %>><%= loginBean.translate("HTML") %></option>
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
					var issueTracker = "<%= issueTrackerBean.getInstanceId() %>";
					psdk.error = function(error) {
						console.log(error);
						document.getElementById("error-message").innerHTML = error;
						$( "#dialog-error" ).dialog( "open" );
					}
				</script>
				<% if (bean.getEditorType() == EditorType.Markup) { %>
					<jsp:include page="issue-markup-toolbar.jsp"/>
				<% } else if (bean.getEditorType() == EditorType.HTML) { %>
					<div id="editor" style="height:350px"><%= bean.getEditInstance().getDetails().getEditDetails() %></div>
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
							<%= bean.getEditorType() == EditorType.HTML ? "" : "style='height:350px'" %>
							<%= bean.getEditorType() == EditorType.Markup ? "" : "class='hidden'" %>><%= bean.getInstance().getDetails().getEditDetails() %></textarea><br/>
				</div>
			</div>
			<%= loginBean.translate("Tags") %><br/>
			<input id="tags" name="tags" type="text" value="<%= bean.getEditInstance().getTagsString() %>" title="<%= loginBean.translate("Comma seperated list of tags to categories the issue under") %>"/><br/>
			<script>
			$(function() {
				var availableTags = [<%= bean.getAllTagsString() %>];
				multiDropDown("#tags", availableTags);
			});
			</script>
			<% if (issueTrackerBean.isAdmin()) { %>
				<input name="isPriority" type="checkbox" <% if (bean.getEditInstance().isPriority()) { %>checked<% } %> ><%= loginBean.translate("High Priority") %></input><br/>
				<input name="isHidden" type="checkbox" <% if (bean.getEditInstance().isHidden()) { %>checked<% } %> ><%= loginBean.translate("Hidden") %></input><br/>
			<% } %>
			<input name="preview-edit" type="submit" value="<%= loginBean.translate("Preview") %>" onclick="saveSource()"/><br/>
			<br/>
			<% if (bean.isPreview()) { %>
				<h1><%= bean.getEditInstance().getTitle() %></h1>
				<p>
				<%= bean.getEditInstance().getTextDetails() %>
				</p>
			<% } %>
			<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>" onclick="saveSource()"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
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