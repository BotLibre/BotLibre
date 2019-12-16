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
	<title>Create Issue<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Create a new issue"/>	
	<meta name="keywords" content="create, issue"/>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% if (bean.getEditorType() == EditorType.WYSIWYG) { %>
		<jsp:include page="tinymce-nofocus.jsp"/>
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
	<h1><%= loginBean.translate("Create new issue") %></h1>
	<% boolean error = loginBean.getError() != null || bean.isPreview() || bean.isEditorChange();  %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new issue") %>.
		</p>
	<% } else { %>
		<form action="issue" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= issueTrackerBean.issueTrackerInput() %>
			<span class="required"><%= loginBean.translate("Title") %></span><br/>
			<input autofocus class="required" autofocus name="title" type="text" value="<%= (!error) ? "" : bean.getInstance().getTitle() %>" /><br/>
			
			<table style="border-spacing: 0px;">
				<tr>
					<td><%= loginBean.translate("Type") %></td>
					<td> </td>
					<td>
						<select id="type" name="type" title="<%= loginBean.translate("The type of issue.") %>">
							<option value="Issue" <%= (!error) ? "" : bean.isTypeSelected("Issue") %>><%= loginBean.translate("Issue") %></option>
							<option value="Bug" <%= (!error) ? "" : bean.isTypeSelected("Bug") %>><%= loginBean.translate("Bug") %></option>
							<option value="Feature" <%= (!error) ? "" : bean.isTypeSelected("Feature") %>><%= loginBean.translate("Feature") %></option>
							<option value="Task" <%= (!error) ? "" : bean.isTypeSelected("Task") %>><%= loginBean.translate("Task") %></option>
							<option value="ServiceRequest" <%= (!error) ? "" : bean.isTypeSelected("ServiceRequest") %>><%= loginBean.translate("Service Request") %></option>
						</select>
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Priority") %></td>
					<td> </td>
					<td>
						<select id="priority" name="priority" title="<%= loginBean.translate("The priority of the issue.") %>">
							<option value="Low" <%= (!error) ? "" : bean.isPrioritySelected("Low") %>><%= loginBean.translate("Low") %></option>
							<option value="Medium" <%= (!error) ? "" : bean.isPrioritySelected("Medium") %>><%= loginBean.translate("Medium") %></option>
							<option value="High" <%= (!error) ? "" : bean.isPrioritySelected("High") %>><%= loginBean.translate("High") %></option>
							<option value="Sever" <%= (!error) ? "" : bean.isPrioritySelected("Sever") %>><%= loginBean.translate("Sever") %></option>
						</select>
					</td>
				</tr>
				<tr>
					<td><%= loginBean.translate("Status") %></td>
					<td> </td>
					<td>
						<select id="status" name="status" title="<%= loginBean.translate("The status of the issue.") %>">
							<option value="Open" <%= (!error) ? "" : bean.isStatusSelected("Open") %>><%= loginBean.translate("Open") %></option>
							<option value="Rejected" <%= (!error) ? "" : bean.isStatusSelected("Rejected") %>><%= loginBean.translate("Rejected") %></option>
							<option value="Deferred" <%= (!error) ? "" : bean.isStatusSelected("Deferred") %>><%= loginBean.translate("Deferred") %></option>
							<option value="Duplicate" <%= (!error) ? "" : bean.isStatusSelected("Duplicate") %>><%= loginBean.translate("Duplicate") %></option>
							<option value="Assigned" <%= (!error) ? "" : bean.isStatusSelected("Assigned") %>><%= loginBean.translate("Assigned") %></option>
							<option value="Implemented" <%= (!error) ? "" : bean.isStatusSelected("Implemented") %>><%= loginBean.translate("Implemented") %></option>
							<option value="Closed" <%= (!error) ? "" : bean.isStatusSelected("Closed") %>><%= loginBean.translate("Closed") %></option>
						</select>
					</td>
				</tr>
			</table>
			
			<%= loginBean.translate("Details") %><br/>
			<script>
				var saveSource = function() { }
			</script>
			<select name="create-editor" title="<%= loginBean.translate("Choose editor to edit post with.  Posts can be written in HTML or markup text.") %>" onchange="saveSource(); this.form.submit()">
				<option value="WYSIWYG" <%= bean.isEditorTypeSelected(EditorType.WYSIWYG) %>><%= loginBean.translate("WYSIWYG") %></option>
				<option value="Markup" <%= bean.isEditorTypeSelected(EditorType.Markup) %>><%= loginBean.translate("Markup") %></option>
				<option value="HTML" <%= bean.isEditorTypeSelected(EditorType.HTML) %>><%= loginBean.translate("HTML") %></option>
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
					<div id="editor" style="height:350px"><%= bean.getInstance().getDetails().getEditDetails() %></div>
					<script>
					    var editor = ace.edit("editor");
					    editor.getSession().setMode("ace/mode/html");
					    saveSource = function() {
					    	document.getElementById("details").value = editor.getSession().getValue();
					    }
					</script>
					<jsp:include page="shortcuts.jsp"/>
				<% } %>
				<div id="details-text-div" style="display:inline">
					<textarea id="details" name="details"
							<%= bean.getEditorType() == EditorType.HTML ? "" : "style='height:350px'" %>
							<%= bean.getEditorType() == EditorType.Markup ? "" : "class='hidden'" %>><%= (!error) ? "" : bean.getInstance().getDetails().getEditDetails() %></textarea><br/>
				</div>
			</div>
			<%= loginBean.translate("Tags") %><br/>
			<input id="tags" name="tags" type="text" value="<%= (!error) ? "" : bean.getInstance().getTagsString() %>" title="<%= loginBean.translate("Comma seperated list of tags to categories the issue under") %>" /><br/>
			<script>
				$(function() {
					var availableTags = [<%= bean.getAllTagsString() %>];
					multiDropDown("#tags", availableTags);
				});
			</script>
			<input name="preview-create" type="submit" value="<%= loginBean.translate("Preview") %>" onclick="saveSource()"/><br/>
			<br/>
			<% if (bean.isPreview()) { %>
				<h1><%= bean.getInstance().getTitle() %></h1>
				<p>
				<%= bean.getInstance().getTextDetails() %>
				</p>
			<% } %>
			<input name="autosubscribe" type="checkbox" title="<%= loginBean.translate("Subscribe to be notified through email of all updates to the issue" ) %>">Subscribe to updates</input><br/>
			<input id="ok" name="create-instance" type="submit" value="<%= loginBean.translate("Create") %>" onclick="saveSource()"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
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