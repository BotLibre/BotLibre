<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<script type="text/javascript">	
	var Toolbar = {};
	
	Toolbar.code = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n[code lang=xml lines=20]\ncode\n[code]');
		return false;
	});
	
	Toolbar.h1 = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n= h1 =');
		return false;
	});
	
	Toolbar.h2 = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n== h2 ==');
		return false;
	});
	
	Toolbar.h3 = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n=== h3 ===');
		return false;
	});
	
	Toolbar.bullet = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n* listitem');
		return false;
	});
	
	Toolbar.numbered = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n# listitem');
		return false;
	});
	
	Toolbar.indent = (function() {
		SDK.insertAtCaret(document.getElementById('details'), '\n:');
		return false;
	});
	
	Toolbar.link = (function() {
		SDK.insertAtCaret(document.getElementById('details'), 'http://');
		return false;
	});
	
	Toolbar.image = (function() {
		psdk.uploadIssueTrackerAttachment(issueTracker, true, function(link) {
			SDK.insertAtCaret(document.getElementById('details'), link);
		});
		return false;
	});
	
	Toolbar.file = (function() {
		psdk.uploadIssueTrackerAttachment(issueTracker, false, function(link) {
			SDK.insertAtCaret(document.getElementById('details'), link);
		});
		return false;
	});
	
	var fullscreen = false;
	
	Toolbar.fullscreen = (function() {
		var div = document.getElementById('details-div');
		var textdiv = document.getElementById('details-text-div');
		var details = document.getElementById('details');
		var footer = document.getElementById('footer');
		var postDetails = document.getElementById('post-details');
		fullscreen = !fullscreen;
		if (fullscreen) {
			div.style.position = "fixed";
			div.style.top = "0";
			div.style.bottom = "0";
			div.style.left = "0";
			div.style.right = "0";
			div.style.zIndex = "99";
			
			textdiv.style.position = "fixed";
			textdiv.style.top = "44px";
			textdiv.style.bottom = "2px";
			textdiv.style.left = "2px";
			textdiv.style.right = "2px";
			textdiv.style.width = "100%";
			
			details.style.width = "100%";
			details.style.height = "100%";
			details.style.maxWidth = "none";
			details.style.margin = "0";
			details.style.padding = "0";
			
			footer.style.display = "none";
			postDetails.style.display = "none";
		} else {
			div.style.position = "relative";
			div.style.top = "";
			div.style.bottom = "";
			div.style.left = "";
			div.style.right = "";
			div.style.zIndex = "1";
			
			textdiv.style.position = "";
			textdiv.style.top = "";
			textdiv.style.bottom = "";
			textdiv.style.left = "";
			textdiv.style.right = "";
			textdiv.style.width = "";
			
			details.style.width = "";
			details.style.height = "350px";
			details.style.maxWidth = "";
			details.style.margin = "";
			details.style.padding = "";
			
			footer.style.display = "";
			postDetails.style.display = "";
		}
		return false;
	});
</script>
<div style="inline-block;position:relative">
	<span class="dropt">
		<div style="text-align:left;top:36px">
			<table>
				<tr>
					<td><a onClick="Toolbar.h1()" href="#"><img class="menu" src="images/h1.png" title="<%= loginBean.translate("Append heading markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.h1()" href="#"><%= loginBean.translate("Heading 1") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.h2()" href="#"><img class="menu" src="images/h2.png" title="<%= loginBean.translate("Append heading markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.h2()" href="#"><%= loginBean.translate("Heading 2") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.h3()" href="#"><img class="menu" src="images/h3.png" title="<%= loginBean.translate("Append heading markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.h3()" href="#"><%= loginBean.translate("Heading 3") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.bullet()" href="#"><img class="menu" src="images/bullet.png" title="<%= loginBean.translate("Append bullet list markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.bullet()" href="#"><%= loginBean.translate("Bullet list") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.numbered()" href="#"><img class="menu" src="images/nbullet.png" title="<%= loginBean.translate("Append numbered list markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.numbered()" href="#"><%= loginBean.translate("Numbered list") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.indent()" href="#"><img class="menu" src="images/indent.png" title="<%= loginBean.translate("Append indent markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.indent()" href="#"><%= loginBean.translate("Indent") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.code()" href="#"><img class="menu" src="images/code.png" title="<%= loginBean.translate("Append code formatting markup") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.code()" href="#"><%= loginBean.translate("Formatted code block") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.image()" href="#"><img class="menu" src="images/image.svg" title="<%= loginBean.translate("Upload resize and insert an image file") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.image()" href="#"><%= loginBean.translate("Insert image") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.file()" href="#"><img class="menu" src="images/attach.svg" title="<%= loginBean.translate("Upload and insert and file or media attachment") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.file()" href="#"><%= loginBean.translate("Insert file") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.link()" href="#"><img class="menu" src="images/link.png" title="<%= loginBean.translate("Insert http link") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.link()" href="#"><%= loginBean.translate("Insert link") %></a></td>
				</tr>
				<tr>
					<td><a onClick="Toolbar.fullscreen()" href="#"><img class="menu" src="images/fullscreen.png" title="<%= loginBean.translate("Maximize or reset editor") %>"></a></td>
					<td><a class="menu" onClick="Toolbar.fullscreen()" href="#"><%= loginBean.translate("Fullscreen") %></a></td>
				</tr>
			</table>
		</div>
		<img class="toolbar" src="images/menu.png">
	</span>
	<a onClick="Toolbar.h1()" href="#"><img class="toolbar" src="images/h1.png" title="<%= loginBean.translate("Append heading markup") %>"></a>
	<a onClick="Toolbar.h2()" href="#"><img class="toolbar" src="images/h2.png" title="<%= loginBean.translate("Append heading markup") %>"></a>
	<a onClick="Toolbar.h3()" href="#"><img class="toolbar" src="images/h3.png" title="<%= loginBean.translate("Append heading markup") %>"></a>
	<a onClick="Toolbar.bullet()" href="#"><img class="toolbar" src="images/bullet.png" title="<%= loginBean.translate("Append bullet list markup") %>"></a>
	<a onClick="Toolbar.numbered()" href="#"><img class="toolbar" src="images/nbullet.png" title="<%= loginBean.translate("Append numbered list markup") %>"></a>
	<a onClick="Toolbar.indent()" href="#"><img class="toolbar" src="images/indent.png" title="<%= loginBean.translate("Append indent markup") %>"></a>
	<a onClick="Toolbar.code()" href="#"><img class="toolbar" src="images/code.png" title="<%= loginBean.translate("Append code formatting markup") %>"></a>
	<a onClick="Toolbar.image()" href="#"><img class="toolbar" src="images/image.svg" title="<%= loginBean.translate("Upload resize and insert an image file") %>"></a>
	<a onClick="Toolbar.file()" href="#"><img class="toolbar" src="images/attach.svg" title="<%= loginBean.translate("Upload and insert and file or media attachment") %>"></a>
	<a onClick="Toolbar.link()" href="#"><img class="toolbar" src="images/link.png" title="<%= loginBean.translate("Insert http link") %>"></a>
	<a onClick="Toolbar.fullscreen()" href="#"><img class="toolbar" src="images/fullscreen.png" title="<%= loginBean.translate("Maximize or reset editor") %>"></a>
</div>
<div id="dialog-error" title="Error" class="dialog">
	<p id="error-message"></p>
</div>
<script>
	$(function() {
		$( "#dialog-error" ).dialog({
			autoOpen: false,
			modal: true,
			buttons: {
				Ok: function() {
					$( this ).dialog( "close" );
				}
			}
		});
	});
</script>