<%@page import="org.botlibre.web.script.ScriptSource"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ScriptBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ScriptBean bean = loginBean.getBean(ScriptBean.class);
	String title = "Script";
	if (bean.getDisplayInstance() != null) {
		title = bean.getDisplayInstance().getName();
	}
	ScriptSource viewSource = bean.getViewSource();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> <%= loginBean.translate("Versions") %><%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>	
	<script>
	$(function() {		
		$( "#dialog-delete" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#delete" ).click(function() {
			$( "#dialog-delete" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-delete" ).click(function() {
			$( "#dialog-delete" ).dialog( "close" );
			return false;
		});
	});
	</script>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<jsp:include page="script-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<% bean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>	
	<jsp:include page="error.jsp"/>
	<% if (bean.getDisplayInstance() == null) { %>
		<%= loginBean.translate("No script selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this script.") %>
	<% } else { %>
			
		<div id="dialog-delete" title="Delete" class="dialog">
			<form id="delete-form" action="script" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<input type="checkbox" name="confirm" onclick="document.getElementById('form-confirm').click();" title="<%= loginBean.translate("Caution, this will permently delete the script versions") %>"><%= loginBean.translate("I'm sure") %></input><br/>
				<input class="delete" name="delete-version" type="submit" value="<%= loginBean.translate("Delete") %>" onclick="document.getElementById('form-delete').click(); return false;" title="<%= loginBean.translate("Permently delete the script versions") %>"/>
				<input id="cancel-delete" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		</div>
			
		<form action="script" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<input class="hidden" id="form-confirm" type="checkbox" name="confirm">
			<input class="hidden" id="form-delete" name="delete-version" type="submit" value="Delete"/>
			
			<table style="width=100%;" cellspacing="4">
				<tr><td><%= loginBean.translate("Version") %></td><td><%= loginBean.translate("Created") %></td><td><%= loginBean.translate("Updated") %></td><td>User</td></tr>
				<% for (ScriptSource source : bean.getAllScriptVersions()) { %>
					<tr>
						<td nowrap><input type=checkbox name=<%= source.getId() %>><span class="chat"><%= source.getVersion() %></span></td>
						<td nowrap><span class="chat"><%= source.getCreationDate() %></span></td>
						<td nowrap><span class="chat"><%= source.getUpdateDate() %></span></td>
						<td nowrap><span class="chat"><%= source.getCreator().getUserId() %> </span></td>
					</tr>
				<% } %>
			</table>
			<div style="position:relative;margin-top:12px;margin-bottom:12px">
				<span class="dropt">
					<div style="text-align:left;bottom:35px">
						<table>
							<tbody>
								<tr class="menuitem">
									<td><a href="#" class="menuitem" onclick="$('#details-version').click(); return false;" title="<%= loginBean.translate("Script details") %>"><img src="images/home_black.svg" class="menu"> <%= loginBean.translate("Script Details") %></a></td>
								</tr>
								<% if (bean.isAdmin()) { %>
									<tr class="menuitem">
										<td><a href="#" class="menuitem" onclick="$('#delete').click(); return false;" title="<%= loginBean.translate("Permently delete the script versions") %>"><img src="images/script-delete.svg" class="menu"> <%= loginBean.translate("Delete Version") %></a></td>
									</tr>
								<% } %>
								<tr class="menuitem">
									<td><a href="#" class="menuitem" onclick="$('#view-version').click(); return false;" title="<%= loginBean.translate("View the version source") %>"><img src="images/script-view.svg" class="menu"> <%= loginBean.translate("View Version") %></a></td>
								</tr>
							</tbody>
						</table>
					</div>
					<a href="#" onclick="return false"><img src="images/menu.png" class="toolbar"></a>
				</span>
				<a href="#" onclick="$('#details-version').click(); return false;" title="<%= loginBean.translate("Script details") %>"><img src="images/home_black.svg" class="toolbar"></a>
				<% if (bean.isAdmin()) { %>
						<a href="#" onclick="$('#delete').click(); return false;" title="<%= loginBean.translate("Permenatly delete script versions") %>"><img src="images/script-delete.svg" class="toolbar"></a>
				<% } %>
				<a href="#" onclick="$('#view-version').click(); return false;" title="<%= loginBean.translate("View the version source") %>"><img src="images/script-view.svg" class="toolbar"></a>
			</div>
		
			<input id="details-version" name="show-details" type="submit" style="display:none;" title="<%= loginBean.translate("Script version details") %>"/>
			<% if (bean.isAdmin()) { %>
				<input id="delete" name="delete-version" type="submit" style="display:none;" title="<%= loginBean.translate("Permently delete the script versions") %>">
			<% } %>
			<input id="view-version" type="submit" name="view-version" style="display:none;" title="<%= loginBean.translate("View the version source") %>"/>
		</form>
		
		<% if (viewSource != null) { %>
			<br/>
			<div id="editor" style="width:100%;height:500px;max-width:none"><%= viewSource.getEditSource() %></div>
			<script>
			    var editor = ace.edit("editor");
			    editor.getSession().setMode("ace/mode/<%= bean.getSourceLanguage() %>");
				editor.setReadOnly(true);
			</script>
		<% } %>
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