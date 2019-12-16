<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.SelfBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.api.knowledge.Vertex"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% SelfBean selfBean = loginBean.getBean(SelfBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Scripts") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("The scripts page allows you to create, edit, remove, import, and export Self or AIML scripts") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("scripts, code, aiml, self, bot, import, export, editor, scripting, program") %>"/>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
	<script>
		$(function() {
			$( "#dialog-rebootstrap" ).dialog({
				autoOpen: false,
				modal: true
			});
			
			$( "#rebootstrap" ).click(function() {
				$( "#dialog-rebootstrap" ).dialog( "open" );
				return false;
			});
			
			$( "#cancel-rebootstrap" ).click(function() {
				$( "#dialog-rebootstrap" ).dialog( "close" );
				return false;
			});
			
			$( "#cancel-import" ).click(function() {
				$( "#dialog-import" ).dialog( "close" );
				return false;
			});
			
			$( "#dialog-import" ).dialog({
				autoOpen: false,
				modal: true
			});
			
			$( "#import-icon" ).click(function() {
				$( "#dialog-import" ).dialog( "open" );
				return false;
			});
			
			$( "#select-icon" ).click(function() {
				return false;
			});
		});
	</script>
	<script>
		var selectScripts = false;
		var sdkConnection = null;
		
		SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
		sdkConnection = new SDKConnection();
		sdkConnection.debug = true;
				
		var sdkUser = new UserConfig();
		<% if (loginBean.isLoggedIn()) { %>
			sdkUser.user = "<%= loginBean.getUser().getUserId() %>";
			sdkUser.token = "<%= loginBean.getUser().getToken() %>";
			sdkConnection.user = sdkUser;
		<% } %>
		sdkConnection.error = function(error) {
			console.log(error);
			SDK.showError(error, "<%= loginBean.translate("Server Error") %>");
			return;;
		}
				
		function selectAllScripts(tableName) {
			$("#" + tableName + " tbody").find('tr').each(function (index, row) {
				var tableRow = $(row);
				var checkBox = tableRow.find('input[type="checkbox"]');
				if (!checkBox.is(':checked')) {
					checkBox.prop('checked', true);
				}
			});
		}
				
		function unselectAllScripts(tableName) {
			$("#" + tableName + " tbody").find('tr').each(function (index, row) {
				var tableRow = $(row);
				var checkBox = tableRow.find('input[type="checkbox"]');
				if (checkBox.is(':checked')) {
					checkBox.prop('checked', false);
				}
			});
		}
	</script>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="chatlog-topper-banner" align="left" style="position:relative;z-index:10;">
		<div class="clearfix">
		<div class="toolbar">
			<span class="dropt">
				<div class="menu" style="top:35px;">
					<table>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#add-icon').click(); return false;" title="<%= loginBean.translate("Add a new script") %>" class="menuitem">
								<img src="images/plus.svg" class="menu"/> <%= loginBean.translate("Add Script") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#edit-icon').click(); return false;" title="<%= loginBean.translate("Edit the script") %>" class="menuitem">
								<img src="images/edit2.svg" class="menu"/> <%= loginBean.translate("Edit Script") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" id="select-all-menu-icon" title="<%= loginBean.translate("Select all (or the first 100) objects") %>" class="menuitem">
								<img src="images/select.png" class="menu"/> <%= loginBean.translate("Select all") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#delete-script').click(); return false;" title="<%= loginBean.translate("Remove the script") %>" class="menuitem">
								<img src="images/remove3.svg" class="menu"/> <%= loginBean.translate("Delete Script") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export the script") %>" class="menuitem">
								<img src="images/download.svg" class="menu"/> <%= loginBean.translate("Export Script") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Upload and import a Self script, or AIML script") %>" class="menuitem">
								<img src="images/upload.svg" class="menu"/> <%= loginBean.translate("Upload Script") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import a Self script, or AIML script from the script library") %>" class="menuitem">
								<img src="images/import.svg" class="menu"/> <%= loginBean.translate("Import Script") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#move-scripts-up').click(); return false;" title="<%= loginBean.translate("Move the script up in the order of precedence") %>" class="menuitem">
								<img src="images/script-up.svg" class="menu"/> <%= loginBean.translate("Move Up") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#move-scripts-down').click(); return false;" title="<%= loginBean.translate("Move the script down in the order of precedence") %>" class="menuitem">
								<img src="images/script-down.svg" class="menu"/> <%= loginBean.translate("Move Down") %></a>
							</td>
						</tr>
					</table>
				</div>
				<img class="admin-banner-pic" src="images/menu1.png">
			</span>
			<a href="#" onclick="$('#add-icon').click(); return false;" title="<%= loginBean.translate("Add a new script") %>"><img src="images/plus.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#edit-icon').click(); return false;" title="<%= loginBean.translate("Edit the script") %>"><img src="images/edit2.svg" class="admin-banner-pic"/></a>
			<a href="#" id="select-all-icon" title="<%= loginBean.translate("Select all (or the first 100) objects") %>"><img src="images/select.svg" class="admin-banner-pic"/></a>
			<a href="#" id="delete-script" title="<%= loginBean.translate("Remove the script") %>"><img src="images/remove3.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export the script") %>"><img src="images/download.svg" class="admin-banner-pic"/></a>	
			<a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Upload and import a Self script, or AIML script") %>"><img src="images/upload.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import a Self script, or AIML script from the script library") %>"><img src="images/import.svg" class="admin-banner-pic"/></a>
			<a href="#" id="move-scripts-up" title="<%= loginBean.translate("Move the script up in the order of precedence") %>"><img src="images/script-up.svg" class="admin-banner-pic"/></a>
			<a href="#" id="move-scripts-down" title="<%= loginBean.translate("Move the script down in the order of precedence") %>"><img src="images/script-down.svg" class="admin-banner-pic"/></a>
		</div>
		</div>
	</div>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
					<div>
						<%= loginBean.translate("The scripts tab allows you to create, edit, remove, import, and export Self or AIML scripts.") %>
						<%= loginBean.translate("You can upload scripts, or import scripts from the shared script library.") %>
						<%= loginBean.translate("You can also view the scripts your bot has written for itself.") %><br/>
						<%= loginBean.translate("Note, AIML scripts can also be imported from the Chat Logs page as a chat log, and this is more efficient for large scripts, or scripts that only contain questions and answers.") %><br/>
						<% if (!Site.DEDICATED) { %>
							<%= loginBean.translate("For help on writing scripts see") %>:
							<ul>
								<li> <a href="https://www.botlibre.com/forum-post?id=699077&embedded=true" target="_blank" class="blue"><%= loginBean.translate("Introducing the Self scripting language") %></a><br/>
								<li> <a href=https://www.botlibre.com/forum-post?id=28654&embedded=true" target="_blank" class="blue"><%= loginBean.translate("Scripting your bot with AIML") %></a><br/>
								<li> <a href="https://www.botlibre.com/forum-post?id=1156738&embedded=true" target="_blank" class="blue"><%= loginBean.translate("What's new in AIML 2.0") %></a>
							</ul>
						<% } %>
					</div>
					<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-self.jsp"><%= loginBean.translate("Docs") %></a> : <a target="_blank" href="https://www.botlibre.com/forum-post?id=699077"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/script1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Add, create, edit, import, and export Self or AIML scripting programs.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Scripts") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
			
				<div id="dialog-import" title="<%= loginBean.translate("Import") %>" class="dialog">
					<form action="self" method="post" enctype="multipart/form-data" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<table>
						<tr><td>
						<%= loginBean.translate("File") %></td><td>
						<input id="import-file" type="file" name="file"/>
						</td></tr>
						<tr><td>
						<%= loginBean.translate("Format") %></td><td>
						<select name="import-format" title="<%= loginBean.translate("Choose the format of the script file to import") %>" style="width:150px">
							<option value="self">Self</option>
							<option value="aiml">AIML</option>
						</select>
						</td></tr>
						<tr><td>
						<%= loginBean.translate("Encoding") %></td><td>
						<input style="width:150px" name="import-encoding" type="text" value="UTF-8" title="<%= loginBean.translate("Some files may require you to set the character enconding to import correctly") %>" />
						</td></tr>
						</table>
						<input type="checkbox" name="optimize" checked title="<%= loginBean.translate("Compile the script to optimized byte-code. This is more efficient, but less dynamic.") %>"><%= loginBean.translate("Optimize") %></input><br/>
						<input type="checkbox" name="index-static" title="<%= loginBean.translate("Select if static patterns should be indexed as responses instead of being compiled as case states (this can improve performance and reduce memory)") %>"><%= loginBean.translate("Index Patterns") %></input><br/>
						<input type="checkbox" name="create-states" checked title="<%= loginBean.translate("Select if AIML imports should create state machines, this can improve performance and ensure correct ordering") %>"><%= loginBean.translate("Create States") %></input><br/>
						<input type="checkbox" name="merge-state" title="<%= loginBean.translate("Select if the imported script should be merge with the last script") %>"><%= loginBean.translate("Merge") %></input><br/>
						<input type="checkbox" name="debug" title="<%= loginBean.translate("Select if the script debug info should be stored (code, and line numbers).  This will make the script consume more memory") %>"><%= loginBean.translate("Include Debug") %></input><br/>
						<input class="ok" type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="<%= loginBean.translate("Upload and import a Self script, or AIML script") %>">
						<input id="cancel-import" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
					</form>
				</div>
				<form id="form" action="self" method="post" enctype="multipart/form-data" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<h3><%= loginBean.translate("Active Scripts") %></h3>
					<table id="user-scripts-table" class="tablesorter">
						<thead>
							<tr>
								<th><%= loginBean.translate("Select") %></th>
								<th><%= loginBean.translate("Script Name") %></th>
								<th><%= loginBean.translate("Script Language") %></th>
							</tr>
						</thead>
						<tbody>
							<% for (Vertex state : selfBean.getLanguageStateMachines()) { %>
								<tr id="<%= state.getId() %>">
									<td>
										<input name="state-select" value="<%= state.getId() %>" type="checkbox"/>
									</td>
									<td>
										<span value="<%= state.getId() %>" <%= selfBean.getStateCheckedString(state) %>><%= state.getName() %></span>
									</td>
									<td>
										<span><%= selfBean.getStateLanguage(state) %></span>
									</td>
								</tr>
							<% } %>
						</tbody>
					</table>
					<script>
						$('#select-all-menu-icon').click(function(event) {
							event.preventDefault();
							if (!selectScripts) {
								selectScripts = true;
								selectAllScripts("user-scripts-table");
							} else {
								selectScripts = false;
								unselectAllScripts("user-scripts-table");
							}
						});
						
						$('#select-all-icon').click(function(event) {
							event.preventDefault();
							if (!selectScripts) {
								selectScripts = true;
								selectAllScripts("user-scripts-table");
							} else {
								selectScripts = false;
								unselectAllScripts("user-scripts-table");
							}
						});
						
						$('#move-scripts-up').click(function(event) {
				        	event.preventDefault();
				          	var array = [];
				          	$('#user-scripts-table tbody').find('tr').each(function (index, row) {
					          	var currRow = $(row);
					            var checkBox = currRow.find('input[type="checkbox"]');
					            if (checkBox.is(':checked')) {
						            array.push(currRow.prop('id'));
					            }
				        	});
				          	if (array.length == 0) {
								SDK.showError("Please select scripts to move up.", "Select Script Error");
								return false;
							}
				          	var scriptIds = "";
					        for (let i = 0; i < array.length; i++) {
					        	if (i == array.length - 1) {
					        		scriptIds = scriptIds.concat(array[i]);
					        	} else {
					        		scriptIds = scriptIds.concat(array[i] + ",");
					        	}
					        }
				          	var scriptSourceConfig = new ScriptSourceConfig();
					        scriptSourceConfig.id = scriptIds;
					        scriptSourceConfig.instance = "<%= botBean.getInstanceId() %>";
					        sdkConnection.upBotScript(scriptSourceConfig, function() {
					        	for(item in array) {
					        		var row = $('#user-scripts-table tbody').find('tr#' + array[item]);
						        	var prevRow = row.prev('tr');
						        	var prevRowId = prevRow.prop('id');
					        		$('#user-scripts-table tbody #' + prevRowId).insertAfter( $('#user-scripts-table tbody #' + array[item]));
					        	}
					        });
				        });

				        $('#move-scripts-down').click(function(event) {
				        	event.preventDefault();
					       	var array = [];
					        $('#user-scripts-table tbody').find('tr').each(function (index, row) {
					          	var currRow = $(row);
					            var checkBox = currRow.find('input[type="checkbox"]');
					            if (checkBox.is(':checked')) {
						            array.push(currRow.prop('id'));
					            }
					        });
					        if (array.length == 0) {
								SDK.showError("Please select scripts to move down.", "Select Script Error");
								return false;
							}
					        array.reverse();
				          	var scriptIds = "";
					        for (let i = 0; i < array.length; i++) {
					        	if (i == array.length - 1) {
					        		scriptIds = scriptIds.concat(array[i]);
					        	} else {
					        		scriptIds = scriptIds.concat(array[i] + ",");
					        	}
					        }
					        var scriptSourceConfig = new ScriptSourceConfig();
					        scriptSourceConfig.id = scriptIds;
					        scriptSourceConfig.instance = "<%= botBean.getInstanceId() %>";
					        sdkConnection.downBotScript(scriptSourceConfig, function() {
					        	for(item in array) {
					        		var row = $('#user-scripts-table tbody').find('tr#' + array[item]);
					          		var nextRow = row.next('tr');
					          		var nextRowId = nextRow.prop('id');
					        		$('#user-scripts-table tbody #' + nextRowId).insertBefore($('#user-scripts-table tbody #' + array[item]));	
						        }
					        });
				        });
				        
				        $('#delete-script').click(function(event) {
				        	event.preventDefault();
				        	var array = [];
					        $('#user-scripts-table tbody').find('tr').each(function (index, row) {
					          	var currRow = $(row);
					            var checkBox = currRow.find('input[type="checkbox"]');
					            if (checkBox.is(':checked')) {
						            array.push(currRow.prop('id'));
					            }
					        });
					        if (array.length == 0) {
								SDK.showError("Please select scripts to delete.", "Delete Script Error");
								return false;
							}
					        var scriptIds = "";
					        for (let i = 0; i < array.length; i++) {
					        	if (i == array.length - 1) {
					        		scriptIds = scriptIds.concat(array[i]);
					        	} else {
					        		scriptIds = scriptIds.concat(array[i] + ",");
					        	}
					        }
					        var scriptSourceConfig = new ScriptSourceConfig();
					        scriptSourceConfig.id = scriptIds;
					        scriptSourceConfig.instance = "<%= botBean.getInstanceId() %>";
					        sdkConnection.deleteBotScript(scriptSourceConfig, function() {
					        	for (let item in array) {
					        		var row = $('#user-scripts-table tbody').find('tr#' + array[item]).remove();
					        	}
					        });
				        });
					</script>				
					<div style="display:none">
						<input id="up-icon" type="submit" name="up" value=""/>
						<input id="down-icon" type="submit" name="down" value=""/>
						<input id="add-icon" type="submit" name="new" value=""/>
						<input id="edit-icon" type="submit" name="edit" value=""/>
						<input id="remove-icon" type="submit" name="remove" value="">
						<input id="export-icon" type="submit" name="export" value="">
						<input id="import-icon" type="submit" name="import" value=""/>
						<input id="importlib-icon" type="submit" name="import-lib" value=""/>
					</div>
				</form>

				<h3><%= loginBean.translate("Bootstrap") %></h3>
			
				<div id="dialog-rebootstrap" title="<%= loginBean.translate("Rebootstrap") %>" class="dialog">
					<form action="self" method="post" enctype="multipart/form-data" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<input type="checkbox" name="confirmRebootstrap"><%= loginBean.translate("I understand this will permanently delete all scripts and rebootstrap with the system defaults") %></input><br/>
						<input class="delete" type="submit" name="Rebootstrap" value="<%= loginBean.translate("Rebootstrap") %>" title="<%= loginBean.translate("Caution, this will delete all state machines and rebootstrap with the system defaults") %>">
						<input id="cancel-rebootstrap" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
					</form>
				</div>
						
				<form action="self" method="post" enctype="multipart/form-data" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<input id="rebootstrap" class="delete" type="submit" name="Rebootstrap" value="<%= loginBean.translate("Rebootstrap") %>" title="<%= loginBean.translate("Caution, this will delete all state machines and rebootstrap with the system defaults") %>"><br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
