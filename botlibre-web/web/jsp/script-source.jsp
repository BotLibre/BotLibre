<%@page import="org.botlibre.web.bean.LoginBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ScriptBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded() || loginBean.isFullScreen() || loginBean.isMobile();
	ScriptBean bean = loginBean.getBean(ScriptBean.class);
	String title = "Script";
	if (bean.getDisplayInstance() != null) {
		title = bean.getDisplayInstance().getName();
	}
	boolean ads = bean.showAds();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> <%= loginBean.translate("Source") %> - <%= Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= LoginBean.getJQueryHeader() %>
	<% if (!loginBean.isMobile()) { %>
		<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% } %>
	<script>
	$(function() {
		
		$( "#dialog-save" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#dialog-upload" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#save" ).click(function() {
			$( "#dialog-save" ).dialog( "open" );
			return false;
		});
		
		$( "#upload" ).click(function() {
			$( "#dialog-upload" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-save" ).click(function() {
			$( "#dialog-save" ).dialog( "close" );
			return false;
		});
		
		$( "#cancel-upload" ).click(function() {
			$( "#dialog-upload" ).dialog( "close" );
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
	<div id="contents-full">
	<div class="browse">
<% } %>	
	<jsp:include page="error.jsp"/>
	<% if (bean.getDisplayInstance() == null) { %>
		<%= loginBean.translate("No script selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this script.") %>
	<% } else { %>
	
		<% if (!loginBean.isMobile()) { %>
			<% if (embed) { %>
				<style type="text/css">			  
					.ace_editor {
						position: fixed !important;
					}
				</style>
				<div id="editor" class="embed-editor"><%= bean.getDisplayInstance().getSource().getEditSource() %></div>
			<% } else { %>
					<div id="editor" class="editor"><%= bean.getDisplayInstance().getSource().getEditSource() %></div>
			<% } %>
		<% } else { %>
			<div id="editor" class="embed-editor" style="bottom:65px;"><textarea wrap="off" style="font-size:16px;width:100%;height:100%;overflow:scroll;overflow-x:scroll" id="source2"><%= bean.getDisplayInstance().getSource().getEditSource() %></textarea></div>
			<script>
				var saveSource = function() {
					document.getElementById("source").value = document.getElementById("source2").value
				}
			</script>
		<% } %>
		
		<% if (embed) { %>
			<div style="position:fixed;bottom:5px;right:4px;left:4px;">
		<% } %>
		<% if (!loginBean.isMobile()) { %>
			<script>
				var editor = ace.edit("editor");
				//editor.setTheme("ace/theme/monokai");
				editor.getSession().setMode("ace/mode/<%= bean.getSourceLanguage() %>");
				<% if (!bean.isAdmin()) { %>
					editor.setReadOnly(true);
				<% } %>
				var saveSource = function() {
					document.getElementById("source").value = editor.getSession().getValue();
				}
			</script>
			<jsp:include page="shortcuts.jsp"/>
		<% } %>
			
		<div id="dialog-save" title="Save" class="dialog">
			<form id="save-form" action="script" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<textarea id="source" class="hidden" name="source"><%= bean.getDisplayInstance().getSource().getEditSource() %></textarea>
				<input type="checkbox" name="version" title="<%= loginBean.translate("Save the source as a new version") %>"><%= loginBean.translate("Version") %></input>
				<input style="width:200px" name="versionName" type="text" value="<%= bean.getDisplayInstance().getNextVersion() %>" /><br/>
				<input class="ok" name="save" type="submit" value="<%= loginBean.translate("Save") %>" onclick="saveSource()" title="<%= loginBean.translate("Save the script") %>"/>
				<input id="cancel-save" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		</div>
			
		<div id="dialog-upload" title="<%= loginBean.translate("Upload Script") %>" class="dialog">
			<form id="upload-form" action="script-source-upload" method="post" enctype="multipart/form-data" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<input id="upload-file" class="hidden" onchange="this.form.submit()" type="file" name="file"/>
				<table>
					<tr>
						<td><input type="checkbox" name="version" title="<%= loginBean.translate("Upload the source as a new version") %>"><%= loginBean.translate("Version") %></input></td>
						<td><input style="width:200px" name="versionName" type="text" value="<%= bean.getDisplayInstance().getNextVersion() %>"
									/></td>
					</tr>
					<tr>
						<td><%= loginBean.translate("Encoding") %></td>
						<td><input style="width:200px" name="import-encoding" type="text" value="UTF-8"
									title="<%= loginBean.translate("Some files may require you to set the character enconding to import correctly") %>" /></td>
					</tr>
				</table>
				<input name="upload-script-source" type="submit" value="<%= loginBean.translate("Upload") %>" 
						onclick="document.getElementById('upload-file').click(); return false;" title="<%= loginBean.translate("Upload a new version of the source code") %>"/>
				<input class="cancel" id="cancel-upload" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
			</form>
		</div>
		<form action="script" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<span class="dropt">
				<div style="text-align:left;bottom:36px">
					<table>
						<tbody>
							<tr class="menuitem">
								<td><a href="#" class="menuitem" onclick="$('#details').click(); return false;" title="<%= loginBean.translate("Script details") %>"><img src="images/home_black.svg" class="menu"> <%= loginBean.translate("Script Details") %></a></td>
							</tr>
							<% if (bean.isAdmin()) { %>
								<tr class="menuitem">
									<td><a href="#" class="menuitem" onclick="$('#save').click(); return false;" title="<%= loginBean.translate("Save the script") %>"><img src="images/script-save2.svg" class="menu"> <%= loginBean.translate("Save") %></a></td>
								</tr>
								<tr class="menuitem">
									<td><a href="#" class="menuitem" onclick="$('#upload').click(); return false;" title="<%= loginBean.translate("Upload a new version of the source code") %>"><img src="images/script-upload.svg" class="menu"> <%= loginBean.translate("Upload") %></a></td>
								</tr>
							<% } %>
							<tr class="menuitem">
								<td><a href="#" class="menuitem" onclick="$('#download').click(); return false;" title="<%= loginBean.translate("Download the script source code") %>"><img src="images/script-download.svg" class="menu"> <%= loginBean.translate("Download") %></a></td>
							</tr>
						</tbody>
					</table>
				</div>
				<a href="#" onclick="return false"><img src="images/menu.png" class="toolbar"></a>
			</span>
			<a href="#" onclick="$('#details').click(); return false;" title="<%= loginBean.translate("Script details") %>"><img src="images/home_black.svg" class="toolbar"></a>
			<% if (bean.isAdmin()) { %>
				<input id="save" class="ok" type="submit" name="save" value="<%= loginBean.translate("Save") %>" style="display:none;" title="<%= loginBean.translate("Save the script") %>"/>
				<input id="upload" type="submit" name="upload-script-source" value="<%= loginBean.translate("Upload") %>" style="display:none;" title="<%= loginBean.translate("Upload a new version of the source code") %>"/>
			<% } %>
			<input id="download" type="submit" name="download-script-source" value="<%= loginBean.translate("Download") %>" style="display:none;" title="<%= loginBean.translate("Download the script source code") %>">
			<input id="details" name="show-details" type="submit" value="<%= loginBean.translate("Details") %>" style="display:none;"/>
			<% if (bean.isAdmin()) { %>
				<a href="#" onclick="$('#save').click(); return false;" title="<%= loginBean.translate("Save the script") %>"><img src="images/script-save2.svg" class="toolbar"></a>
				<a href="#" onclick="$('#upload').click(); return false;" title="<%= loginBean.translate("Upload a new version of the source code") %>"><img src="images/script-upload.svg" class="toolbar"></a>
			<% } %>
			<a href="#" onclick="$('#download').click(); return false;" title="<%= loginBean.translate("Download the script source code") %>"><img src="images/script-download.svg" class="toolbar"></a>
		</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
<% } %>
<% proxy.clear(); %>
</body>
</html>