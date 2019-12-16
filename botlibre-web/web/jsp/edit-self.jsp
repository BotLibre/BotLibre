<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.SelfBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.api.knowledge.Vertex"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% SelfBean selfBean = loginBean.getBean(SelfBean.class); %>
<% boolean embed = loginBean.isEmbedded() || loginBean.isFullScreen() || loginBean.isMobile(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Script - <%= Site.NAME %></title>
	<% if (!loginBean.isMobile()) { %>
		<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% } %>
	<%= loginBean.getJQueryHeader() %>
	<script>
	$(function() {
		$( "#dialog-error" ).dialog({
			autoOpen: true,
			modal: true
		});
		
		$( "#ok-error" ).click(function() {
			$( "#dialog-error" ).dialog( "close" );
			return false;
		});
	});
	</script>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<jsp:include page="instance-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>	
	<h1><%= loginBean.translate("Editing Script") %></h1>
	<% if (embed) { %>
		<% if (loginBean.getError() != null) { %>			
			<div id="dialog-error" title="Error" class="dialog">
				<form action="self" method="post" enctype="multipart/form-data" class="message">
					<p><b><pre><code style="color:#E00000;"><%= loginBean.getErrorMessage() %></code></pre></b></p>
					<input id="ok-error" name="ok" type="submit" value="<%= loginBean.translate("OK") %>"/>
				</form>
			</div>
			<% loginBean.setError(null); %>
		<% } %>
	<% } else { %>
		<jsp:include page="error.jsp"/>
	<% } %>
	<% if (!botBean.isConnected()) { %>
		<%= botBean.getNotConnectedMessage() %>
	<% } else if (!botBean.isAdmin()) { %>
		<%= botBean.getMustBeAdminMessage() %>
	<% } else { %>
		<% if (!loginBean.isMobile()) { %>
			<% if (embed) { %>
				<style type="text/css">
					.ace_editor {
						position: fixed !important;
					}
				</style>
				<div id="editor" class="embed-editor"><%= selfBean.getEditSource() %></div>
			<% } else { %>
				<div id="editor" class="editor"><%= selfBean.getEditSource() %></div>
			<% } %>
			<script>
				var editor = ace.edit("editor");
				editor.getSession().setMode("ace/mode/<%= selfBean.getEditLanguage() %>");
				var saveSource = function() {
					document.getElementById("source").value = editor.getSession().getValue();
				}
			</script>
			<jsp:include page="shortcuts.jsp"/>
	 	<% } else { %>
			<div id="editor" class="embed-editor" style="bottom:65px;"><textarea wrap="off" style="font-size:16px;width:100%;height:100%;overflow:scroll;overflow-x:scroll" id="source2"><%= selfBean.getEditSource() %></textarea></div>
			<script>
				var saveSource = function() {
					document.getElementById("source").value = document.getElementById("source2").value;
				}
			</script>
		<% } %>
		
		<% if (embed) { %>
			<div style="position:fixed;bottom:5px;right:4px;left:4px;">
		<% } else { %>
			<br/>
	 	<% } %>
		<form action="self" method="post" enctype="multipart/form-data" class="message" style="display:inline">
			<%= loginBean.postTokenInput() %>
			<textarea id="source" name="input" style="width:0px;height:0px" class="hidden"><%= selfBean.getEditSource() %></textarea>
			<br/>
			<span class="dropt">
				<div style="text-align:left;bottom:35px">
					<table>
						<tbody>
							<tr class="menuitem">
								<td style="font-size:12px;">
									<input type="checkbox" name="optimize" checked title="<%= loginBean.translate("Compile the script to optimized byte-code. This is more efficient, but less dynamic.") %>"> <%= loginBean.translate("Optimize") %></input>
								</td>
							</tr>
							<tr class="menuitem">
								<td style="font-size:12px;">
									<input type="checkbox" name="debug" title="<%= loginBean.translate("Select if the script debug info should be stored (code, and line numbers).	This will make the script consume more memory") %>"> <%= loginBean.translate("Include Debug") %></input>
								</td>
							</tr>
							<tr class="menuitem">
								<td><a href="#" class="menuitem" onclick="$('#script-compile').click(); return false;" title="<%= loginBean.translate("Compile and add the script") %>"><img src="images/script-save.svg" class="menu"> <%= loginBean.translate("Compile") %></a></td>
							</tr>
							<tr class="menuitem">
								<td><a href="#" class="menuitem" onclick="$('#script-decompile').click(); return false;" title="<%= loginBean.translate("Decompile the state machine from its current data structures") %>"><img src="images/ping.svg" class="menu"> <%= loginBean.translate("Decompile") %></a></td>
							</tr>
							<tr class="menuitem">
								<td><a href="#" class="menuitem" onclick="$('#cancel').click(); return false;" title="<%= loginBean.translate("Back") %>"><img src="images/quit.svg" class="menu"> <%= loginBean.translate("Back") %></a></td>
							</tr>
						</tbody>
					</table>
				</div>
				<a href="#" onclick="return false"><img src="images/menu.png" class="toolbar"></a>
			</span>
			<a href="#" onclick="$('#script-compile').click(); return false;" title="<%= loginBean.translate("Compile and add the script") %>"><img src="images/script-save.svg" class="toolbar"></a>
			<a href="#" onclick="$('#cancel').click(); return false;" title="<%= loginBean.translate("Back") %>"><img src="images/quit.svg" class="toolbar"></a>
			<input type="checkbox" name="optimize" checked style="display:none;" title="<%= loginBean.translate("Compile the script to optimized byte-code. This is more efficient, but less dynamic.") %>"/>
			<input type="checkbox" name="debug" style="display:none;" title="<%= loginBean.translate("Select if the script debug info should be stored (code, and line numbers).	This will make the script consume more memory") %>"/>
			<input id="script-compile" type="submit" name="compile" style="display:none;" onclick="saveSource()"/>
			<input id="script-decompile" type="submit" name="decompile" style="display:none;"/>
			<input id="cancel" type="submit" name="cancel" style="display:none;"/>
		</form>
		<br/>
	<% } %>
	<% if (!loginBean.isMobile() || !embed) { %>
		</div>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
</body>
</html>
