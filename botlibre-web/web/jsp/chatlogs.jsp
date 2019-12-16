<%@page import="java.util.Collection"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.bean.ChatLogBean"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.knowledge.Primitive"%>
<%@page import="org.botlibre.web.bean.ChatBean"%>
<%@page import="org.botlibre.api.knowledge.Vertex"%>
<%@page import="org.botlibre.api.knowledge.Relationship"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.util.Utils"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% ChatLogBean bean = loginBean.getBean(ChatLogBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Chat logs - <%= Site.NAME %></title>
	<link rel="stylesheet" type="text/css" href="css/stylechatlogs.css">
	<meta name="description" content="Review your bot's chat logs. Train your bot, correct and add new responses."/>	
	<meta name="keywords" content="chat logs, conversations, logs, add response, greetings, default responses, training, bot, chatbot"/>
	<%= loginBean.getJQueryHeader() %>
	<script>
	$(function() {
		$( "#dialog-import" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#dialog-import-info" ).dialog({
			autoOpen: false,
			modal: true,
			width: 'auto',
			buttons: {
				Ok: function() {
					$( this ).dialog( "close" );
				}
			}
		});
		
		$( "#import-icon" ).click(function() {
			$( "#dialog-import" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-import" ).click(function() {
			$( "#dialog-import" ).dialog( "close" );
			return false;
		});
		
		$( "#dialog-export" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#export-icon" ).click(function() {
			$( "#dialog-export" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-export" ).click(function() {
			$( "#dialog-export" ).dialog( "close" );
			return false;
		});
		
		$( "#export" ).click(function() {
			$( "#dialog-export" ).dialog( "close" );
			return true;
		});
		
		$( "#dialog-button" ).dialog({
			autoOpen: false,
			modal: true,
			buttons: [
						{
							id: "insert-button",
							text: "Insert",
							click: function() {
							},
							class: "okbutton"
						},
						{
							text: "Cancel",
							click: function() {
								$( "#dialog-button" ).dialog( "close" );
								return false;
							}
						}
					]
		});
		
		$( "#cancel-insert-button" ).click(function() {
			$( "#dialog-button" ).dialog( "close" );
			return false;
		});
		
		$( "#add-icon3" ).click(function() {
			setupEditDialog();
			$( "#dialog-add-response" ).dialog( "open" );
			return false;
		});
		
		$( "#edit-icon3" ).click(function() {
			//$( "#dialog-add-response" ).dialog( "open" );
			return false;
		});
		
		let dialogAddResponseWidth = $(window).width()-20;
		if(dialogAddResponseWidth > 1000) { dialogAddResponseWidth = 1000; }
		let dialogAddResponseMaxHeight = $(window).height()-50;
		$( "#dialog-add-response" ).dialog({
			autoOpen: false,
			modal: true,
			width: dialogAddResponseWidth,
			maxHeight: dialogAddResponseMaxHeight,
			buttons: [
						{
							text: "Save",
							click: function() {
								dialogAddResponse();
							},
							class: "okbutton"
						},
						{
							text: "Cancel",
							click: function() {
								clearEditResponseDialog();
								$( this ).dialog( "close" );
							}
						}
					],
			close: function(event, ui) {
				refreshTags();
			}
		});
		
		$( "#dial-add-response-cancel" ).click(function() {
			$( "#dialog-add-response" ).dialog( "close" );
			return false;
		});
		
		// Show Checkboxes
		$("#check-all").change(function() {
			checkAllCheckboxes(this.checked);
		});
		
		// Edit Dialog All Checkbox
		$("#dial-check-all").change(function() {
			checkAllCheckboxes(this.checked);
		});
		
		$("#check-topic").change(function() {
			$(".chat-topic").toggle(this.checked);
			$(".edit-topic-tr").toggle(this.checked);
			$("#dial-check-topic").prop('checked', this.checked);
		});
		
		$("#check-label").change(function() {
			$(".chat-label").toggle(this.checked);
			$(".edit-label-tr").toggle(this.checked);
			$("#dial-check-label").prop('checked', this.checked);
		});
		
		$("#check-keywords").change(function() {
			$(".chat-keyword").toggle(this.checked);
			$(".edit-keywords-tr").toggle(this.checked);
			$("#dial-check-keywords").prop('checked', this.checked);
		});
		
		$("#check-required").change(function() {
			$(".chat-required").toggle(this.checked);
			$(".edit-required-tr").toggle(this.checked);
			$("#dial-check-required").prop('checked', this.checked);
		});
		
		$("#check-emotes").change(function() {
			$(".chat-emote").toggle(this.checked);
			$(".edit-emotes-tr").toggle(this.checked);
			$("#dial-check-emotes").prop('checked', this.checked);
		});
		
		$("#check-sentiment").change(function() {
			$(".chat-sentiment").toggle(this.checked);
			$(".edit-sentiment-tr").toggle(this.checked);
			$("#dial-check-sentiment").prop('checked', this.checked);
		});
		
		$("#check-confidence").change(function() {
			$(".chat-confidence").toggle(this.checked);
			$(".edit-confidence-tr").toggle(this.checked);
		});

		$("#check-actions").change(function() {
			$(".chat-action").toggle(this.checked);
			$(".edit-actions-tr").toggle(this.checked);
			$("#dial-check-actions").prop('checked', this.checked);
		});

		$("#check-poses").change(function() {
			$(".chat-pose").toggle(this.checked);
			$(".edit-poses-tr").toggle(this.checked);
			$("#dial-check-poses").prop('checked', this.checked);
		});
		
		$("#check-next").change(function() {
			$(".chat-next").toggle(this.checked);
			$(".edit-next-tr").toggle(this.checked);
		});

		$("#check-previous").change(function() {
			$(".chat-previous").toggle(this.checked);
			$(".chat-require-previous").toggle(this.checked);
			$(".edit-previous-tr").toggle(this.checked);
			$("#dial-check-previous").prop('checked', this.checked);
		});
		
		$("#check-repeat").change(function() {
			$(".edit-repeat-tr").toggle(this.checked);
			$("#dial-check-repeat").prop('checked', this.checked);
		});
		
		$("#check-condition").change(function() {
			$(".chat-condition").toggle(this.checked);
			$(".edit-condition-tr").toggle(this.checked);
			$("#dial-check-condition").prop('checked', this.checked);
		});

		$("#check-think").change(function() {
			$(".chat-think").toggle(this.checked);
			$(".edit-think-tr").toggle(this.checked);
			$("#dial-check-think").prop('checked', this.checked);
		});
		
		$("#check-command").change(function() {
			$(".chat-command").toggle(this.checked);
			$(".edit-command-tr").toggle(this.checked);
			$("#dial-check-command").prop('checked', this.checked);
		});

		$("#check-synonyms").change(function() {
			$(".chat-synonym").toggle(this.checked);
			$(".edit-synonyms-tr").toggle(this.checked);
			$("#dial-check-synonyms").prop('checked', this.checked);
		});
		
		// Add/Edit Response Dialog Checkboxes
		$("#dial-check-topic").change(function() {
			$("#dial-topic-tr").toggle(this.checked);
			$("#dial-topic-check-tr").toggle(this.checked);
			$("#check-topic").prop('checked', this.checked);
		});
		
		$("#dial-check-label").change(function() {
			$("#dial-intent-label-tr").toggle(this.checked);
			$("#check-label").prop('checked', this.checked);
		});
		
		$("#dial-check-keywords").change(function() {
			$("#dial-keywords-tr").toggle(this.checked);
			$("#check-keywords").prop('checked', this.checked);
		});
		
		$("#dial-check-required").change(function() {
			$("#dial-required-tr").toggle(this.checked);
			$("#check-required").prop('checked', this.checked);
		});
		
		$("#dial-check-emotes").change(function() {
			$("#dial-emotions-tr").toggle(this.checked);
			$("#check-emotes").prop('checked', this.checked);
		});
		
		$("#dial-check-sentiment").change(function() {
			$("#dial-sentiment-tr").toggle(this.checked);
			$("#check-sentiment").prop('checked', this.checked);
		});
		
		$("#dial-check-actions").change(function() {
			$("#dial-actions-tr").toggle(this.checked);
			$("#check-actions").prop('checked', this.checked);
		});
		
		$("#dial-check-poses").change(function() {
			$("#dial-poses-tr").toggle(this.checked);
			$("#check-poses").prop('checked', this.checked);
		});
		
		$("#dial-check-previous").change(function() {
			$("#dial-previous-tr").toggle(this.checked);
			$("#dial-require-previous-tr").toggle(this.checked);
			$("#check-previous").prop('checked', this.checked);
		});

		$("#dial-check-repeat").change(function() {
			$("#dial-on-repeat-tr").toggle(this.checked);
			$("#dial-no-repeat-tr").toggle(this.checked);
			$("#check-repeat").prop('checked', this.checked);
		});
		
		$("#dial-check-condition").change(function() {
			$("#dial-condition-tr").toggle(this.checked);
			$("#check-condition").prop('checked', this.checked);
		});
		
		$("#dial-check-think").change(function() {
			$("#dial-think-tr").toggle(this.checked);
			$("#check-think").prop('checked', this.checked);
		});
		
		$("#dial-check-command").change(function() {
			$("#dial-command-tr").toggle(this.checked);
			$("#check-command").prop('checked', this.checked);
		});
		
		$(document).on('focusin', function(event) {
			if ($(event.target).closest(".mce-window").length) {
				event.stopImmediatePropagation();
			}
		});
		refreshTags();
	});
	
	SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
	var psdk = new SDKConnection();
	<% if (loginBean.getUser() != null) { %>
		var puser = new UserConfig();
		puser.user = '<%= loginBean.getUser().getUserId() %>';
		puser.token = '<%= loginBean.getUser().getToken() %>';
		psdk.user = puser;
	<% } %>
	var botinstance = "<%= botBean.getInstanceId() %>";
	psdk.error = function(error) {
		console.log(error);
		document.getElementById("error-message").innerHTML = error;
		$( "#dialog-error" ).dialog( "open" );
	}
	
	</script>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<jsp:include page="tinymce-chatlogs.jsp"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="chatlog-topper-banner" align="left" style="position:relative;z-index:10;">
		<% boolean isConversation2 = bean.getSearch().equals(ChatLogBean.CONVERSATIONS); %>
		<% boolean isDefault2 = bean.getSearch().equals(ChatLogBean.DEFAULT); %>
		<% boolean isGreeting2 = bean.getSearch().equals(ChatLogBean.GREETINGS); %>
		<% boolean isWord2 = bean.getSearch().equals(ChatLogBean.WORDS); %>
		<% boolean isFlagged2 = bean.getSearch().equals(ChatLogBean.FLAGGED); %>
		<% if (!bean.isCorrection()) { %>
			<div class="clearfix">
			<div class="toolbar">
				<span class="dropt">
					<div class="menu" style="top:35px;">
						<table>
							<% if ((bean.getResults() == null) || bean.getResults().isEmpty()) { %>
								<tr class="menuitem">
									<td><a href="#" onclick="$('#home-icon').click(); return false;" title="<%= loginBean.translate("Home") %>" class="menuitem">
										<img src="images/home_black.svg" class="menu"/> <%= loginBean.translate("Home") %>
										<input name="home" type="hidden"></a>
									</td>
								</tr>
								<tr class="menuitem">
									<td><a href="#" onclick="$('#add-icon').click(); return false;" title="<%= loginBean.translate("Enter a new question and response") %>" class="menuitem">
										<img src="images/plus.svg" class="menu"/> <%= loginBean.translate("New question") %>/<%= loginBean.translate("response") %>
										<input name="new" type="hidden"></a>
									</td>
								</tr>
								<tr class="menuitem">
									<td><a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>" class="menuitem">
										<img src="images/upload.svg" class="menu"/> <%= loginBean.translate("Upload from file") %>
										<input name="import" type="hidden"></a>
									</td>
								</tr>
								<tr class="menuitem">
									<td><a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import a chat log, response list, or AIML script from the script library") %>" class="menuitem">
										<img src="images/import.svg" class="menu"/> <%= loginBean.translate("Import from library") %></a>
									</td>
								</tr>
							<% } else { %>
								<% if (isFlagged2) { %>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#unflag-icon').click(); return false;" title="<%= loginBean.translate("Unflag the selected phrases as not offensive") %>" class="menuitem">
											<img src="images/unflag.svg" class="menu"/> <%= loginBean.translate("Unflag") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all conversations, responses, or phrases") %>" class="menuitem">
											<img src="images/select.svg" class="menu"/> <%= loginBean.translate("Select all") %></a>
										</td>
									</tr>
								<% } else { %>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#home-icon').click(); return false;" title="<%= loginBean.translate("Return to the training start page") %>" class="menuitem">
											<img src="images/home_black.svg" class="menu"/> <%= loginBean.translate("Home") %>
											<input name="home" type="hidden"></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#add-icon').click(); return false;" title="<%= loginBean.translate("Enter a new") %> <%= loginBean.translate(isGreeting2 ? "greeting" : (isDefault2 ? "default response" : "question and response")) %>" class="menuitem">
											<img src="images/plus.svg" class="menu"/> <%= loginBean.translate("New") %> <%= isGreeting2 ? "greeting" : (isDefault2 ? "default response" : "question/response") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#edit-icon').click(); return false;" title="<%= loginBean.translate("Enter a correct response for the selected phrases") %>" class="menuitem">
											<img src="images/edit2.svg" class="menu"/> <%= isConversation2 ? loginBean.translate("Enter correction") : loginBean.translate("Edit response") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#inspect-icon').click(); return false;" title="<%= loginBean.translate("Browse the selected responses or phrases") %>" class="menuitem">
											<img src="images/inspect.svg" class="menu"/> <%= loginBean.translate("Browse selection") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all conversations, responses, or phrases") %>" class="menuitem">
											<img src="images/select.svg" class="menu"/> <%= loginBean.translate("Select all") %></a>
										</td>
									</tr>
									<% if (!isWord2) { %>
										<tr class="menuitem">
											<td><a href="#" onclick="$('#wrong-icon').click(); return false;" title="<%= loginBean.translate("Mark the selected responses as invalid responses, or decrease their correctness %") %>" class="menuitem">
												<img src="images/wrong.svg" class="menu"/> <%= loginBean.translate("Invalidate responses") %></a>
											</td>
										</tr>
										<tr class="menuitem">
											<td><a href="#" onclick="$('#check-icon').click(); return false;" title="<%= loginBean.translate("Mark the selected responses as valid responses, or increase their correctness %") %>" class="menuitem">
												<img src="images/check.svg" class="menu"/> <%= loginBean.translate("Validate responses") %></a>
											</td>
										</tr>
									<% } %>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#flag-icon').click(); return false;" title="<%= loginBean.translate("Flag the selected phrases as offensive") %>" class="menuitem">
											<img src="images/flag.svg" class="menu"/> <%= loginBean.translate("Flag as offensive") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#unflag-icon').click(); return false;" title="<%= loginBean.translate("Unflag the selected phrases as not offensive") %>" class="menuitem">
											<img src="images/unflag.svg" class="menu"/> <%= loginBean.translate("Unflag") %></a>
										</td>
									</tr>
									<% if (!isWord2) { %>
										<tr class="menuitem">
											<td><a href="#" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Delete the selected responses, greeting, default response, or conversations") %>" class="menuitem">
												<img src="images/remove3.svg" class="menu"/> <%= loginBean.translate("Delete") %></a>
											</td>
										</tr>
									<% } %>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export and download the currently displayed logs as a chat log file, response list, or AIML script") %>" class="menuitem">
											<img src="images/download.svg" class="menu"/> <%= loginBean.translate("Export and download") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>" class="menuitem">
											<img src="images/upload.svg" class="menu"/> <%= loginBean.translate("Upload from file") %></a>
										</td>
									</tr>
									<tr class="menuitem">
										<td><a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import a chat log, response list, or AIML script from the script library") %>" class="menuitem">
											<img src="images/import.svg" class="menu"/> <%= loginBean.translate("Import from library") %></a>
										</td>
									</tr>
								<% } %>
							<% } %>
						</table>
					</div>
					<img class="admin-banner-pic" src="images/menu1.png">
				</span>
			<% } %>
			<% if ((bean.getResults() == null) || bean.getResults().isEmpty()) { %>
				<a href="#" id="home" onclick="$('#home-icon').click(); return false;" title="<%= loginBean.translate("Return to the training start page") %>"><img src="images/home_black.svg") class="admin-banner-pic"/></a>
				<a href="#" id="add-icon3" title="<%= loginBean.translate("Enter a new question and response") %>"><img src="images/plus.svg" class="admin-banner-pic"/></a>
				<a href="#" id="import-icon3" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>"><img src="images/upload.svg" class="admin-banner-pic"/></a>
				<a href="#" id="importlib-icon3" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import a chat log, response list, or AIML script from the script library") %>"><img src="images/import.svg" class="admin-banner-pic"/></a>
				</div>
			</div>
			</div>
			<% } else { %>
				<% if (!bean.isCorrection()) { %>
					<% if (isFlagged2) { %>
						<a href="#" id="unflag-icon3" onclick="$('#unflag-icon').click(); return false;" title="<%= loginBean.translate("Unflag the selected phrases as not offensive") %>"><img src="images/unflag.svg" class="admin-banner-pic"/></a>
						<a href="#" id="select-icon3" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all conversations, responses, or phrases") %>"><img src="images/select.svg" class="admin-banner-pic"/></a>
					<% } else { %>
						<a href="#" id="home" onclick="$('#home-icon').click(); return false;" title="<%= loginBean.translate("Return to the training start page") %>"><img src="images/home_black.svg" class="admin-banner-pic"/></a>
						<a href="#" id="add-icon3" title="<%= loginBean.translate("Enter a new") %> <%= loginBean.translate(isGreeting2 ? "greeting" : (isDefault2 ? "default response" : "question and response")) %>"><img src="images/plus.svg" class="admin-banner-pic"/></a>
						<a href="#" id="edit-icon3" onclick="$('#edit-icon').click(); return false;" title="<%= loginBean.translate("Enter a correct response for the selected phrases") %>"><img src="images/edit2.svg" class="admin-banner-pic"/></a>		
						<a href="#" id="inspect-icon3" onclick="$('#inspect-icon').click(); return false;" title="<%= loginBean.translate("Browse the selected responses or phrases") %>"><img src="images/inspect.svg" class="admin-banner-pic"/></a>
						<% if (!loginBean.isMobile()) { %>
							<a href="#" id="select-icon3" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all conversations, responses, or phrases") %>"><img src="images/select.svg" class="admin-banner-pic"/></a>
						<% } %>
						<% if (!loginBean.isMobile() && !isWord2) { %>
							<a href="#" id="wrong-icon3" class="shrinkhide500"  onclick="$('#wrong-icon').click(); return false;" title="<%= loginBean.translate("Mark the selected responses as invalid responses, or decrease their correctness %") %>"><img src="images/wrong.svg" class="admin-banner-pic"/></a>
							<a href="#" id="check-icon3" class="shrinkhide500"  onclick="$('#check-icon').click(); return false;" title="<%= loginBean.translate("Mark the selected responses as valid responses, or increase their correctness %") %>"><img src="images/check.svg" class="admin-banner-pic"/></a>
						<% } %>
						<% if (!loginBean.isMobile()) { %>
							<a href="#" id="flag-icon3" class="shrinkhide500"  onclick="$('#flag-icon').click(); return false;" title="<%= loginBean.translate("Flag the selected phrases as offensive") %>"><img src="images/flag.svg" class="admin-banner-pic"/></a>
							<a href="#" id="unflag-icon3" class="shrinkhide500"  onclick="$('#unflag-icon').click(); return false;" title="<%= loginBean.translate("Unflag the selected phrases as not offensive") %>"><img src="images/unflag.svg" class="admin-banner-pic"/></a>
						<% } %>
						<% if (!loginBean.isMobile() && !isWord2) { %>
							<a href="#" id="remove-icon3" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Delete the selected responses, greeting, default response, or conversations") %>"><img src="images/remove3.svg" class="admin-banner-pic"/></a>
						<% } %>
						<% if (!loginBean.isMobile()) { %>
							<a href="#" id="export-icon3" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export and download the currently displayed logs as a chat log file, response list, or AIML script") %>"><img src="images/download.svg" class="admin-banner-pic"/></a>
							<a href="#" id="import-icon3" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>"><img src="images/upload.svg" class="admin-banner-pic"/></a>
							<a href="#" id="importlib-icon3" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import a chat log, response list, or AIML script from the script library") %>"><img src="images/import.svg" class="admin-banner-pic"/></a>
						<% } %>
					<% } %>
				</div>
			</div>
		<% } %>
	<% } %>
	</div>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The Training & Chat Logs page allows you to add new responses, greetings, and default responses.") %>
					<%= loginBean.translate("You can view the conversations your bot has had, and correct the responses.") %>
					<%= loginBean.translate("You can view and edit responses, greetings, default responses, phrases, words and flagged responses.") %><br/>
					<%= loginBean.translate("You can correct a bot's response in a conversation, or add or edit existing responses.") %>
					<%= loginBean.translate("You can associate keywords, required words, previous responses, topics, and other meta data to your bot's response to improve") %>
					<%= loginBean.translate("its response matching and conversation context.") %>
					<%= loginBean.translate("You can label responses to reuse them in other questions by referencing the #label.") %><br/>
					<%= loginBean.translate("You can also import chat logs, response lists, or AIML files from the shared script library, or upload chat logs from your computer.") %>
					<%= loginBean.translate("You can export and download your bot's conversations, or responses, as a chat log, response list, or AIML file.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-chatlogs.jsp"><%= loginBean.translate("Docs") %></a> : <a target="_blank" href="https://botlibre.com/forum-post?id=483549"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/chatlog1.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Train your bot's responses, view its conversations, import and export chat logs.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Training & Chat Logs") %>
			</h1>
					
			<jsp:include page="error.jsp"/>
			<% try { %>
				<% if (!botBean.isConnected() || !botBean.isAdmin()) { %>
					<p class="help">
						<%= loginBean.translate("The Training & Chat Logs page allows you to add new responses, greetings, and default responses.") %>
						<%= loginBean.translate("You can view the conversations your bot has had, and correct the responses.") %>
						<%= loginBean.translate("You can view and edit responses, greetings, default responses, phrases, words and flagged responses.") %>
					</p>
					<p class="help">
						<%= loginBean.translate("You can correct a bot's response in a conversation, or add or edit existing responses.") %>
						<%= loginBean.translate("You can associate keywords, required words, previous responses, topics, and other meta data to your bot's response to improve") %>
						<%= loginBean.translate("its response matching and conversation context.") %>
						<%= loginBean.translate("You can label responses to reuse them in other questions by referencing the #label.") %>
					</p>
					<p class="help">
					<%= loginBean.translate("You can also import chat logs, response lists, or AIML files from the shared script library, or upload chat logs from your computer.") %>
						<%= loginBean.translate("You can export and download your bot's conversations, or responses, as a chat log, response list, or AIML file.") %>
					</p>
					<p class="help">
						<%= loginBean.translate("See") %> <a href="http://botlibre.com/forum-post?id=483549&embedded" target="_blank" class="blue"><%= loginBean.translate("Chat Logs : how to train your customer service bot by monitoring its chat logs, using keywords and topics</a> for more information.") %>
					</p>
					<br/>
					<% if (!botBean.isConnected()) { %>
						<%= botBean.getNotConnectedMessage() %>
					<% } else if (!botBean.isAdmin()) { %>
						<%= botBean.getMustBeAdminMessage() %>
					<% } %>
				<% } else { %>
					<% boolean isConversation = bean.getSearch().equals(ChatLogBean.CONVERSATIONS); %>
					<% boolean isResponses = bean.getSearch().equals(ChatLogBean.RESPONSES); %>
					<% boolean isDefault = bean.getSearch().equals(ChatLogBean.DEFAULT); %>
					<% boolean isGreeting = bean.getSearch().equals(ChatLogBean.GREETINGS); %>
					<% boolean isPhrase = bean.getSearch().equals(ChatLogBean.PHRASES) || bean.isPhrase(); %>
					<% boolean isPhraseSelected = bean.getSearch().equals(ChatLogBean.PHRASES); %>
					<% boolean isWord = bean.getSearch().equals(ChatLogBean.WORDS); %>
					<% boolean isFlagged = bean.getSearch().equals(ChatLogBean.FLAGGED); %>

					<form id="form" action="chat-log" method="get" class="message">
					<%= botBean.instanceInput() %>
					<span class="menu">
						<div class='search-div'>
							<span class='search-span'><%= loginBean.translate("Search") %></span>
							<select id="search-select" name="search" onchange="this.form.submit()" class="search">
								<option value="conversations" <%= bean.getSearchCheckedString("conversations") %>><%= loginBean.translate("conversations") %></option>
								<option value="responses" <%= bean.getSearchCheckedString("responses") %>><%= loginBean.translate("responses") %></option>
								<option value="greetings" <%= bean.getSearchCheckedString("greetings") %>><%= loginBean.translate("greetings") %></option>
								<option value="default" <%= bean.getSearchCheckedString("default") %>><%= loginBean.translate("default responses") %></option>
								<option value="phrases" <%= bean.getSearchCheckedString("phrases") %>><%= loginBean.translate("phrases") %></option>
								<option value="words" <%= bean.getSearchCheckedString("words") %>><%= loginBean.translate("words") %></option>
								<option value="flagged" <%= bean.getSearchCheckedString("flagged") %>><%= loginBean.translate("flagged responses") %></option>
							</select>
						</div>
						<div class='search-div'>
							<span class='search-span'><%= loginBean.translate("Duration") %></span>
							<select name="duration" onchange="this.form.submit()" class="search">
								<option value="none" <%= bean.getDurationCheckedString("none") %>></option>
								<option value="day" <%= bean.getDurationCheckedString("day") %>><%= loginBean.translate("current day") %></option>
								<option value="week" <%= bean.getDurationCheckedString("week") %>><%= loginBean.translate("current week") %></option>
								<option value="month" <%= bean.getDurationCheckedString("month") %>><%= loginBean.translate("current month") %></option>
								<option value="all" <%= bean.getDurationCheckedString("all") %>><%= loginBean.translate("all time") %></option>
							</select>
						</div>
						<div class='search-div'>
							<span class='search-span'><%= loginBean.translate("Filter") %></span>
							<input class="search" style="width:150px" id="searchtext" name="filter" type="text" value="<%= bean.getFilter() %>" title="<%= loginBean.translate("Filter the results to only include phrases containing the filter text") %>" />
						</div>
						<% if (isConversation) { %>
							<div class='search-div'>
								<span class='search-span'><%= loginBean.translate("Type") %></span>
								<select name="type" onchange="this.form.submit()" class="search">
									<option value="all" <%= bean.getTypeCheckedString("") %>></option>
									<option value="chat" <%= bean.getTypeCheckedString("chat") %>><%= loginBean.translate("chat") %></option>
									<option value="tweet" <%= bean.getTypeCheckedString("tweet") %>><%= loginBean.translate("tweet") %></option>
									<option value="directmessage" <%= bean.getTypeCheckedString("directmessage") %>><%= loginBean.translate("direct message") %></option>
									<option value="post" <%= bean.getTypeCheckedString("post") %>><%= loginBean.translate("post") %></option>
									<option value="facebookmessenger" <%= bean.getTypeCheckedString("facebookmessenger") %>><%= loginBean.translate("Facebook Messenger") %></option>
									<option value="skype" <%= bean.getTypeCheckedString("skype") %>><%= loginBean.translate("Skype") %></option>
									<option value="telegram" <%= bean.getTypeCheckedString("telegram") %>><%= loginBean.translate("Telegram") %></option>
									<option value="kik" <%= bean.getTypeCheckedString("kik") %>><%= loginBean.translate("Kik") %></option>
									<option value="wechat" <%= bean.getTypeCheckedString("wechat") %>><%= loginBean.translate("WeChat") %></option>
									<option value="slack" <%= bean.getTypeCheckedString("slack") %>><%= loginBean.translate("Slack") %></option>
									<option value="email" <%= bean.getTypeCheckedString("email") %>><%= loginBean.translate("email") %></option>
									<option value="sms" <%= bean.getTypeCheckedString("sms") %>><%= loginBean.translate("sms") %></option>
									<option value="alexa" <%= bean.getTypeCheckedString("alexa") %>><%= loginBean.translate("Alexa") %></option>
									<option value="googleAssistant" <%= bean.getTypeCheckedString("googleAssistant") %>><%= loginBean.translate("Google Assistant") %></option>
									<option value="command" <%= bean.getTypeCheckedString("command") %>><%= loginBean.translate("command") %></option>
									<option value="timer" <%= bean.getTypeCheckedString("timer") %>><%= loginBean.translate("timer") %></option>
								</select>
							</div>
						<% } %>
						<div class='search-div'>
							<span class='search-span'><%= loginBean.translate("Restrict") %></span>
							<select name="restrict" onchange="this.form.submit()" class="search">
								<option value="none" <%= bean.getRestrictCheckedString("") %>></option>
								<option value="exact" <%= bean.getRestrictCheckedString("exact") %>><%= loginBean.translate("exact match") %></option>
								<option value="question" <%= bean.getRestrictCheckedString("question") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match question") %></option>
								<option value="keyword" <%= bean.getRestrictCheckedString("keyword") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match keyword") %></option>
								<option value="required" <%= bean.getRestrictCheckedString("required") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match required") %></option>
								<option value="topic" <%= bean.getRestrictCheckedString("topic") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match topic") %></option>
								<option value="label" <%= bean.getRestrictCheckedString("label") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match label") %></option>
								<option value="previous" <%= bean.getRestrictCheckedString("previous") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match previous") %></option>
								<option value="repeat" <%= bean.getRestrictCheckedString("repeat") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("match on repeat") %></option>
								<option value="missing-keyword" <%= bean.getRestrictCheckedString("missing-keyword") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("missing keyword") %></option>
								<option value="missing-required" <%= bean.getRestrictCheckedString("missing-required") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("missing required") %></option>
								<option value="missing-topic" <%= bean.getRestrictCheckedString("missing-topic") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("missing topic") %></option>
								<option value="patterns" <%= bean.getRestrictCheckedString("patterns") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("patterns") %></option>
								<option value="templates" <%= bean.getRestrictCheckedString("templates") %> style="display:<%= isWord || isConversation ? "none;" : "inherit;" %>"><%= loginBean.translate("templates") %></option>
								<option value="wordiskeyword" <%= bean.getRestrictCheckedString("wordiskeyword") %> style="display:<%= !isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("keywords") %></option>
								<option value="wordistopic" <%= bean.getRestrictCheckedString("wordistopic") %> style="display:<%= !isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("topics") %></option>
								<option value="emotes" <%= bean.getRestrictCheckedString("emotes") %>><%= loginBean.translate("emotions") %></option>
								<option value="sentiment" <%= bean.getRestrictCheckedString("sentiment") %> style="display:<%= !isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("sentiment") %></option>
								<option value="synonyms" <%= bean.getRestrictCheckedString("synonyms") %> style="display:<%= isWord ? "inherit;" : "none;" %>"><%= loginBean.translate("synonyms") %></option>
								<option value="flagged" <%= bean.getRestrictCheckedString("flagged") %>><%= loginBean.translate("flagged") %></option>
								<option value="corrections" <%= bean.getRestrictCheckedString("corrections") %> style="display:<%= isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("corrections") %></option>
								<option value="actions" <%= bean.getRestrictCheckedString("actions") %> style="display:<%= isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("actions") %></option>
								<option value="poses" <%= bean.getRestrictCheckedString("poses") %> style="display:<%= isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("poses") %></option>
								<option value="command" <%= bean.getRestrictCheckedString("command") %> style="display:<%= isWord ? "none;" : "inherit;" %>"><%= loginBean.translate("command") %></option>
								<option value="engaged" <%= bean.getRestrictCheckedString("engaged") %> style="display:<%= isConversation ? "inherit;" : "none;" %>"><%= loginBean.translate("engaged") %></option>
							</select>
						</div>
						<div class='search-div'>
							<span class='search-span'><%= loginBean.translate("Sort") %></span>
							<select name="sort" onchange="this.form.submit()" class="search">
								<option value="date" <%= bean.getSortCheckedString("date") %>><%= loginBean.translate("date") %></option>
								<option value="date-desc" <%= bean.getSortCheckedString("date-desc") %>><%= loginBean.translate("date desc") %></option>
								<%
									String questionSort = "phrase";
									if (isWord) {
										questionSort = "word";
									} else if (isResponses) {
										questionSort = "question";
									}
									String responseSort = "response";
									if (!bean.getRestrictCheckedString("topic").isEmpty()) {
										responseSort = "topic";
									} else if (!bean.getRestrictCheckedString("keyword").isEmpty()) {
										responseSort = "keyword";
									} else if (!bean.getRestrictCheckedString("label").isEmpty()) {
										responseSort = "label";
									} else if (!bean.getRestrictCheckedString("required").isEmpty()) {
										responseSort = "required";
									} else if (!bean.getRestrictCheckedString("previous").isEmpty()) {
										responseSort = "previous";
									} else if (!bean.getRestrictCheckedString("repeat").isEmpty()) {
										responseSort = "repeat";
									}
								%>
								<% if (!isConversation) { %>
									<option value="question" <%= bean.getSortCheckedString("question") %>><%= loginBean.translate(questionSort) %></option>
									<% if (isResponses && bean.getRestrictCheckedString("question").isEmpty()) { %>
										<option value="response" <%= bean.getSortCheckedString("response") %>><%= loginBean.translate(responseSort) %></option>
									<% } %>
								<% } %>
							</select>
						</div>
						<br/>
						<div class='search-div'>
							<span>Show</span>
						</div>
						<div id="chatLogCheckBoxDiv">
							<div style="display:inline-block;"><input id="check-all" class="search" type="checkbox" name="all" title="<%= loginBean.translate("Show the responses details") %>"><span><%= loginBean.translate("All") %></span></div>
							<div style="display:inline-block;"><input id="check-topic" class="search" type="checkbox" name="topic" <% if (bean.getShowTopic()) { %>checked<% } %> title="<%= loginBean.translate("Show the topic of the responses") %>"><span><%= loginBean.translate("Topic") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-label" class="search" type="checkbox" name="label" <% if (bean.getShowLabel()) { %>checked<% } %> title="<%= loginBean.translate("Show the intent label of the responses") %>"><span><%= loginBean.translate("Intent Label") %></span></div>
							<div style="display:inline-block;"><input id="check-keywords" class="search" type="checkbox" name="keywords" <% if (bean.getShowKeyWords()) { %>checked<% } %> title="<%= loginBean.translate("Show the question keywords to match the responses") %>"><span><%= loginBean.translate("Keywords") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-required" class="search" type="checkbox" name="required" <% if (bean.getShowRequired()) { %>checked<% } %> title="<%= loginBean.translate("Show the question required words to match the responses") %>"><span><%= loginBean.translate("Required") %></span></div>
							<div style="display:inline-block;"><input id="check-emotes" class="search" type="checkbox" name="emotes" <% if (bean.getShowEmotes()) { %>checked<% } %> title="<%= loginBean.translate("Show the emotions of the responses") %>"><span><%= loginBean.translate("Emotions") %></span></div>
							<div style="display:inline-block;"><input id="check-sentiment" class="search" type="checkbox" name="sentiment" <% if (bean.getShowSentiment()) { %>checked<% } %> title="<%= loginBean.translate("Show user sentiment of the responses") %>"><span><%= loginBean.translate("Sentiment") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-confidence" class="search" type="checkbox" name="confidence" <% if (bean.getShowConfidence()) { %>checked<% } %> title="<%= loginBean.translate("Show bot confidence of the responses") %>"><span><%= loginBean.translate("Confidence") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-actions" class="search" type="checkbox" name="actions" <% if (bean.getShowActions()) { %>checked<% } %> title="<%= loginBean.translate("Show the actions of the responses") %>"><span><%= loginBean.translate("Actions") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-poses" class="search" type="checkbox" name="poses" <% if (bean.getShowPoses()) { %>checked<% } %> title="<%= loginBean.translate("Show the poses of the responses") %>"><span><%= loginBean.translate("Poses") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-next" class="search" type="checkbox" name="next" <% if (bean.getShowNext()) { %>checked<% } %> title="<%= loginBean.translate("Show next questions to the responses") %>"><span><%= loginBean.translate("Next") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-previous" class="search" type="checkbox" name="previous" <% if (bean.getShowPrevious()) { %>checked<% } %> title="<%= loginBean.translate("Show the previous questions to the responses") %>"><span><%= loginBean.translate("Previous") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-repeat" class="search" type="checkbox" name="repeat" <% if (bean.getShowRepeat()) { %>checked<% } %> title="<%= loginBean.translate("Show the response repeat options") %>"><span><%= loginBean.translate("Repeat") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-condition" class="search" type="checkbox" name="condition" <% if (bean.getShowCondition()) { %>checked<% } %> title="<%= loginBean.translate("Show the response condition code") %>"><span><%= loginBean.translate("Condition") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-think" class="search" type="checkbox" name="think" <% if (bean.getShowThink()) { %>checked<% } %> title="<%= loginBean.translate("Show the response think code") %>"><span><%= loginBean.translate("Think") %></span></div>
							<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="check-command" class="search" type="checkbox" name="command" <% if (bean.getShowCommand()) { %>checked<% } %> title="<%= loginBean.translate("Show the response JSON client command") %>"><span><%= loginBean.translate("Command") %></span></div>
							<div style="display:<%= isWord ? "inline-block;" : "none;" %>"><input id="check-synonyms" class="search" type="checkbox" name="synonyms" <% if (bean.getShowSynonyms()) { %>checked<% } %> title="<%= loginBean.translate("Show word's synonyms") %>"><span><%= loginBean.translate("Synonyms") %></span></div>
							<div style="display:inline-block;"><input class="search" style="display:none;position:absolute;" type="submit" name="search" value="<%= loginBean.translate("Search") %>"></div>
						</div>
					</span>
				</form>
				<br/>
				<form id="form" action="chat-log" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<% if (!bean.isCorrection()) { %>
					<% } %>
					<% if ((bean.getResults() == null) || bean.getResults().isEmpty()) { %>
						<div style='position:relative;'>
							<input id="add-icon" class="icon" style="display:none;" type="submit" name="new" value="" title="<%= loginBean.translate("Enter a new question and response") %>">
							<input id="import-icon" class="icon" style="display:none;" type="submit" name="import" value="" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>" onclick="return false;"/>
							<input id="importlib-icon" class="icon" style="display:none;" type="submit" name="import-lib" value="" title="<%= loginBean.translate("Import a chat log, response list, or AIML script from the script library") %>"/>
						</div>
						<h3>Tasks</h3>
						<p>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Add new question and response. Your bot will reply to that question and similar questions with the new response.") %>
									</div>
								</span>
								<a href="chat-log?new=new&search=response"><%= loginBean.translate("Add a new response.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Add new greeting. A greeting is the first response a bot gives in a new conversation.") %>
									</div>
								</span>
								<a href="chat-log?new=new&search=greetings"><%= loginBean.translate("Add a new greeting.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Add new default response. The bot will use a default response when it does not match a user's question with a trained response's question.") %>
									</div>
								</span>
								<a href="chat-log?new=new&search=default"><%= loginBean.translate("Add a new default response.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Add new word. Words let you set the sentiment, emotions, and synonyms for a word.") %>
									</div>
								</span>
								<a href="chat-log?new=new&search=words"><%= loginBean.translate("Add a new word.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review conversations today.") %>
									</div>
								</span>
								<a href="chat-log?&search=conversations&duration=day&filter=&type=all&restrict=none"><%= loginBean.translate("Review conversations today.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review conversations this week.") %>
									</div>
								</span>
								<a href="chat-log?&search=conversations&duration=week&filter=&type=all&restrict=none"><%= loginBean.translate("Review conversations this week.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review all of your bot's greetings. A greeting is the first response a bot gives in a new conversation.") %>
									</div>
								</span>
								<a href="chat-log?&search=greetings&duration=none&filter=&type=all&restrict=none"><%= loginBean.translate("Review greetings.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review all default responses. The bot will use a default response when it does not match a user's question with a trained response's question.") %>
									</div>
								</span>
								<a href="chat-log?&search=default&duration=none&filter=&type=all&restrict=none"><%= loginBean.translate("Review default responses.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review intents & labeled responses. Labels & intents let you reuse the same response for many different questions.") %>
									</div>
								</span>
								<a href="chat-log?&search=responses&duration=all&filter=&type=all&restrict=label&label=on"><%= loginBean.translate("Review intents & labeled responses.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review all responses.") %>
									</div>
								</span>
								<a href="chat-log?&search=responses&duration=all&filter=&type=all&restrict=none"><%= loginBean.translate("Review all responses.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review all responses missing keywords. Normally it is a good idea to add keywords or required words to a response to ensure it used for questions that contain those keywords.") %>
									</div>
								</span>
								<a href="chat-log?&search=responses&duration=all&filter=&type=all&restrict=missing-keyword&label=on"><%= loginBean.translate("Review all responses missing keyword.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review flagged responses. Responses can be flagged as offensive, and the bot will reject, and never say these responses.") %>
									</div>
								</span>
								<a href="chat-log?&search=flagged&duration=all&filter=&type=all&restrict=none"><%= loginBean.translate("Review flagged responses.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review flagged words. Words can be flagged as offensive, and the bot will reject users questions that contain these words.") %>
									</div>
								</span>
								<a href="chat-log?&search=words&duration=all&filter=&type=all&restrict=flagged"><%= loginBean.translate("Review flagged words.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review keywords. Keywords are important words that will take priorty when matching responses (do not make common words like 'the' or 'is' keywords).") %>
									</div>
								</span>
								<a href="chat-log?&search=words&duration=all&filter=&type=all&restrict=wordiskeyword"><%= loginBean.translate("Review keywords.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review words that have synonyms. Synonyms allow similar words in a question to trigger the same response.") %>
									</div>
								</span>
								<a href="chat-log?&search=words&duration=all&filter=&type=all&restrict=synonyms"><%= loginBean.translate("Review synonyms.") %></a>
							</div>
							<div>
								<span class="dropt-banner">
									<img id="help-mini" src="images/help.svg"/>
									<div>
										<%= loginBean.translate("Review words with sentiment. Sentiment can be used to track the user's experience, and is available in the bot's analytics.") %>
									</div>
								</span>
								<a href="chat-log?&search=words&duration=all&filter=&type=all&restrict=sentiment"><%= loginBean.translate("Review sentiment.") %></a>
							</div>
						</p>
					<% } else { %>
						<% if (!bean.isCorrection()) { %>
							<div style='position:relative;'>
							<% if (isFlagged) { %>
								<input id="unflag-icon" class="icon" style="display:none;" type="submit" name="unflag" value="" title="<%= loginBean.translate("Unflag the selected phrases as not offensive") %>">
								<input id="select-icon" class="icon" style="display:none;" type="submit" name="selectAll" value="" title="<%= loginBean.translate("Select all conversations, responses, or phrases") %>">
							<% } else { %>
								<input id="home-icon" class="icon" style="display:none;" type="submit" name="home" value="" title="<%= loginBean.translate("Return to the training start page") %>">
								<input id="add-icon" class="icon" style="display:none;" type="submit" name="new" value="" title="<%= loginBean.translate("Enter a new") %> <%= loginBean.translate(isGreeting ? "greeting" : (isDefault ? "default response" : "question and response")) %>">
								<input id="edit-icon" class="icon" style="display:none;" type="submit" name="correct" value="" title="<%= loginBean.translate("Enter a correct response for the selected phrases") %>">
								<input id="inspect-icon" class="icon" style="display:none;" type="submit" name="browse" value="" title="<%= loginBean.translate("Browse the selected responses or phrases") %>">
								<input id="select-icon" class="icon<%= loginBean.isMobile() ? " hidden" : "" %>" style="display:none;" type="submit" name="selectAll" value="" title="<%= loginBean.translate("Select all conversations, responses, or phrases") %>">
								<input id="wrong-icon" class="icon<%= (loginBean.isMobile() || isWord) ? " hidden" : "" %>" style="display:none;" type="submit" name="invalidate" value="" title="<%= loginBean.translate("Mark the selected responses as invalid responses, or decrease their correctness %") %>">
								<input id="check-icon" class="icon<%= (loginBean.isMobile() || isWord) ? " hidden" : "" %>" style="display:none;" type="submit" name="validate" value="" title="<%= loginBean.translate("Mark the selected responses as valid responses, or increase their correctness %") %>">
								<input id="flag-icon" class="icon<%= loginBean.isMobile() ? " hidden" : "" %>" style="display:none;" type="submit" name="flag" value="" title="<%= loginBean.translate("Flag the selected phrases as offensive") %>">
								<input id="unflag-icon" class="icon<%= loginBean.isMobile() ? " hidden" : "" %>" style="display:none;" type="submit" name="unflag" value="" title="<%= loginBean.translate("Unflag the selected phrases as not offensive") %>">
								<input id="remove-icon" class="icon<%= (loginBean.isMobile() || isWord) ? " hidden" : "" %>" style="display:none;" type="submit" name="delete" value="" title="<%= loginBean.translate("Delete the selected responses, greeting, default response, or conversations") %>">
								<input id="export-icon" class="icon<%= loginBean.isMobile() ? " hidden" : "" %>" style="display:none;" type="submit" name="export" value="" title="<%= loginBean.translate("Export and download the currently displayed logs as a chat log file, response list, or AIML script") %>" onclick="return false;"/>
								<input id="import-icon" class="icon<%= loginBean.isMobile() ? " hidden" : "" %>" style="display:none;" type="submit" name="import" value="" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>" onclick="return false;"/>
								<input id="importlib-icon" class="icon<%= loginBean.isMobile() ? " hidden" : "" %>" style="display:none;" type="submit" name="import-lib" value="" title="<%= loginBean.translate("Import a chat log, response list, or AIML script from the script library") %>"/>
							<% } %>
							</div>
						<% } %>
						<% if (!bean.isCorrection() && (bean.getResults() != null) && (bean.getResultsSize() > 1)) { %>
							<br/>
							<span class = "menu"><%= bean.getResultsSize() %> results.</span><br/>
							<% if ((bean.getPage() > 0) || bean.getResultsSize() > bean.getPageSize()) { %>
								<% if (bean.getPage() > 0) { %>
									<a class="menu" href="chat-log?page=<%= bean.getPage() - 1 %>">Previous</a>
									<% if (bean.getResultsSize() > ((bean.getPage() + 1) * bean.getPageSize())) { %>
										<span class = "menu"> | </span>
									<% } %>
								<% } %>
								<% if (bean.getResultsSize() > ((bean.getPage() + 1) * bean.getPageSize())) { %>
									<a class="menu" href="chat-log?page=<%= bean.getPage() + 1 %>">Next</a>
								<% } %>
								<% for (int index = 0; (index * bean.getPageSize()) < bean.getResultsSize(); index++) { %>
									<span class = "menu"> | </span>
									<% if (index == bean.getPage()) { %>
										<a class="menu" href="chat-log?page=<%= index %>"><b><%= index + 1 %></b></a>
									<% } else { %>
										<a class="menu" href="chat-log?page=<%= index %>"><%= index + 1 %></a>
									<% } %>
								<% } %>
								<br/>
							<% } %>
						<% } %>
					
						<% if (!bean.isCorrection() && (isPhrase || isResponses || isDefault || isGreeting)) { %>
							<% if (isResponses) { %>
								<h3><%= loginBean.translate("Responses") %></h3>
							<% } else if (isDefault) { %>
								<h3><%= loginBean.translate("Default Responses") %></h3>
							<% } else if (isGreeting) { %>
								<h3><%= loginBean.translate("Greetings") %></h3>
							<% } else { %>
								<h3><%= loginBean.translate("Phrases") %></h3>
							<% } %>
							<table id="rootTable" style="width=100%;" cellspacing="2">
								<% if (isResponses || isDefault || isGreeting) { %>
									<tr id="quick-add-hr"><td colspan="6"><hr></td></tr>
									<script>
										SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
										sdkConnection = new SDKConnection();
										
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
										
										function quickEditSaveResponse(event) {
											addShow = false;
											editShow = false;
											let id = getRowId(event);
											let questionResponseRow = $("#question-row-" + id);
											let addNextQRRow = $("#add-question-response-row-" + id);
											let editCurrQRRow = $("#edit-question-response-row-" + id);
											let newEditQuestion = $("#quick-edit-question-" + id);
											let newEditResponse = $("#quick-edit-response-" + id);
											if (newEditQuestion.val().length > 0) {
												$("#question-id-" + id).text(newEditQuestion.val());
											}
											if (newEditResponse.val().length > 0) {
												$("#response-id-" + id).text(newEditResponse.val());
											}
											editCurrQRRow.css( {display: "none"} );
											addNextQRRow.css( {display: "none"} );
											questionResponseRow.css( {display: "table-row"} );
										}
										
										function quickEditCancelResponse(event) {
											addShow = false;
											editShow = false;
											let id = getRowId(event);
											let questionResponseRow = $("#question-row-" + id);
											let addNextQRRow = $("#add-question-response-row-" + id);
											let editCurrQRRow = $("#edit-question-response-row-" + id);
											editCurrQRRow.css( {display: "none"} );
											addNextQRRow.css( {display: "none"} );
											questionResponseRow.css( {display: "table-row"} );
										}
									</script>
								<% } %>
								<% for (Vertex question : bean.getResults()) { %>
								
									<%= bean.displayQuestionVertexHTML(question) %>
									
									<tr id='row-line-<%=question.getId() %>'><td colspan='1'><hr/></td></tr>
									
								<% } %>
							</table>
						
						<% } else if (isFlagged) { %>
		
							<h3><%= loginBean.translate("Flagged Responses") %></h3>
							<table style="width:100%;" cellspacing="2">
								<% for (Vertex flagged : bean.getResults()) { %>
									<tr>
									<td valign="top"><input type=checkbox <%= bean.isSelectAll() ? "checked" : "" %> name=<%= "phrase:" + flagged.getId() %>  title="<%= loginBean.translate("Select phrase for unflagging") %>"></td>
									<td style="width:100%;">
										<span class="chat-flagged" style="color:red">
											<%= Utils.escapeHTML(flagged.printString()) %>
										</span>
									</td>
									</tr>
								 	<tr><td colspan="2"><hr/></td></tr>
								<% } %>
							</table>
							
						<% } else if (isWord && !bean.isCorrection()) { %>
							<h3><%= loginBean.translate("Words") %></h3>
							<table style="width:100%;" cellspacing="2">
								<% for (Vertex word : bean.getResults()) { %>
									<% String id =  "" + word.getId(); %>
									<% String id2 =  id; %>
									<% boolean offensive = word.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE); %>
									<tr>
										<td valign="top"><input type=checkbox <%= (bean.isSelectAll() && isWord) ? "checked" : "" %> name=<%= "word:" + word.getId() %>  title="<%= loginBean.translate("Select word for validation, invalidation, or flagging") %>"></td>
										<td style="width:100%;">
											<span class="<%= offensive ? "chat-flagged" : "chat" %>"><%= Utils.escapeHTML(word.printString()) %></span>
										</td>
									</tr>
									<% if (word.getRelationships(Primitive.EMOTION) != null) { %>
										<% String emotes = bean.getEmotes(word); %>
										<% if (!emotes.isEmpty()) { %>
											<tr>
												<td></td>
												<td style="width:100%;"><span class="chat-emote" title="<%= loginBean.translate("Word emotions. Words can be associated with emotions to allow the bot to determine the emotional state of the user or conversation.") %>"><%= emotes %></span></td>
											</tr>
										<% } %>
									<% } %>
									<% if (word.getRelationships(Primitive.EMOTION) != null) { %>
										<% String sentiment = bean.getSentiment(word); %>
										<% if (!sentiment.isEmpty()) { %>
											<tr>
												<td></td>
												<td style="width:100%;"><span class="chat-sentiment" title="<%= loginBean.translate("Word sentiment. Words can be associated with sentiment to track the user's experience, and is available in the bot's analytics.") %>"><%= sentiment %></span></td>
											</tr>
										<% } %>
									<% } %>
									<% if (word.getRelationships(Primitive.SYNONYM) != null) { %>
										<% String synonyms = bean.getWordSynonyms(word); %>
										<% if (!synonyms.isEmpty()) { %>
											<tr>
												<td></td>
												<td style="width:100%;"><span class="chat-synonym" title="<%= loginBean.translate("Word synonyms. Synonyms allow similar words in a question to trigger the same response.") %>"><%= synonyms %></span></td>
											</tr>
										<% } %>
									<% } %>
								 	<tr><td colspan="2"><hr/></td></tr>
								<% } %>
							</table>
							
						<% } else if (bean.isCorrection()) { %>
							
							<% if (isWord) { %>
								<h3><%= loginBean.translate("Enter the word") %></h3>
							<% } else if (isGreeting) { %>
								<h3><%= loginBean.translate("Enter the greeting") %></h3>
							<% } else if (isDefault) { %>
								<h3><%= loginBean.translate("Enter the default response") %></h3>
							<% } else if (bean.getResults().size() == 1 && !isWord) { %>
								<% Vertex question = bean.getResults().get(0); %>
								<% if ("".equals(question.getData())) { %>
									<h3><%= loginBean.translate("Enter the new question and response") %></h3>
								<% } else { %>
									<h3><%= loginBean.translate("Enter the correct response to the phrase") %></h3>
								<% } %>
							<% } else { %>
								<h3><%= loginBean.translate("Enter the correct responses to the phrases") %></h3>
							<% } %>

							<% if (isPhrase || isResponses || isDefault || isGreeting || isWord) { %>

								<table style="width:100%;" cellspacing="2">
									<% int index = 0; %>
									<% int counter = 0; %>
									<% boolean first = true; %>
									<% for (Vertex question : bean.getResults()) { %>
										<% Vertex answer = bean.getResponses().get(index); %>
										<% Vertex meta = null; %>
										<% if (bean.getMetaList() != null && !bean.getMetaList().isEmpty()) { %>
											<% meta = bean.getMetaList().get(index); %>
										<% } %>
										<% index++; %>
										<% int confidence = 50; %>
										<% String id = "" + question.getId(); %>
										<% String id2 = id; %>
										<% if (answer != null) { %>
											<% id =  id + ":" + answer.getId(); %>
											<% id2 =  id2 + answer.getId(); %>
											<% counter++; %>
										<% } %>
										<tr>
											<td nowrap>
												<span class="dropt-banner">
													<img id="help-mini" src="images/help.svg"/>
													<div>
														<%= 
															(isGreeting || isDefault || isWord) ?
																loginBean.translate("Enter the " + (isGreeting ? "greeting" : (isDefault ? "default response" : "word")) + ".")
																: "Enter the question, if desired, you can use the * wildcard or 'Pattern()' to make a pattern."
														%>
													</div>
												</span>
												<span class="chat"><%= loginBean.translate(isGreeting ? "Greeting" : (isDefault ? "Default Response" : (isWord ? "Word" : "Question"))) %>:</span>
											</td>
											<% if ((isGreeting || isDefault) && !isWord) { %>
												<% if (first) { %>
													<%
														if (question != null && question.printString().startsWith("Template(")) {
															bean.setEditorType("markup");
														}
													%>
													<td width="100%">
														<select id="edit-editor-one" name="edit-editor" title="<%= loginBean.translate("Choose editor to edit response with. Responses can be written in WYSIWYG (rich text) or Text (HTML). Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video.") %>">
															<option value="wysiwyg" <%= bean.isEditorTypeSelected("wysiwyg") %>><%= loginBean.translate("WYSIWYG") %></option>
															<option value="markup" <%= bean.isEditorTypeSelected("markup") %>><%= loginBean.translate("Text") %></option>
														</select><br/>
													</td>
												<% } %>
												</tr>
												<tr>
											<% } %>
											<td colspan="2" style="width:100%;"> 
												<% if ((isGreeting || isDefault) && !isWord) { %>
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
															var botinstance = "<%= botBean.getInstanceId() %>";
															psdk.error = function(error) {
																console.log(error);
																document.getElementById("error-message").innerHTML = error;
																$( "#dialog-error" ).dialog( "open" );
															}
														</script>
														<% if (first) { %>
															<div id="details-text-div" style="display:<%= bean.getEditorType().equals("wysiwyg") ? "initial;" : "none;" %>">
																<textarea id="details"><%= question != null ? Utils.escapeHTML(question.printString()) : "" %></textarea><br/>
															</div>
														<% } %>
														<div id="<%= "text-div" + id %>" style="display:<%= bean.getEditorType().equals("markup") || !first ? "initial;" : "none;" %>">
															<textarea id="<%= "answer" + id %>" name="<%= "answer:" + id %>" type="text" title="<%= loginBean.translate("Edit the") %> <%= loginBean.translate(isGreeting ? "greeting" : "default Response") %>"><%= question != null ? Utils.escapeHTML(question.printString()) : "" %></textarea></br>
														</div>
													</div>
													<% if (first) { %>
														<script>
															$('#edit-editor-one').change(function() {
																var editorType = $('#edit-editor-one').val();
																if (editorType === "markup") {
																	var editorContent = tinyMCE.activeEditor.getContent();
																	if (editorContent.startsWith("<p>")) {
																		editorContent = editorContent.substring(3, editorContent.length);
																	}
																	if (editorContent.endsWith("</p>")) {
																		editorContent = editorContent.substring(0, editorContent.length - 4);
																	}
																	$("#<%= "answer" + id %>").val(editorContent);
																	$('#<%= "text-div" + id %>').css("display", "initial");
																	$("#details-text-div").css("display", "none");
																}
																else if (editorType === "wysiwyg"){
																	tinyMCE.activeEditor.setContent($("#<%= "answer" + id %>").val());
																	$('#<%= "text-div" + id %>').css("display", "none");
																	$("#details-text-div").css("display", "initial");
																}
															});
															$(document).on("submit", "form", function(){
																var editorType = $('#edit-editor-one').val();
																if (editorType === "wysiwyg") {
																	var editorContent = tinyMCE.activeEditor.getContent();
																	if (editorContent.startsWith("<p>")) {
																		editorContent = editorContent.substring(3, editorContent.length);
																	}
																	if (editorContent.endsWith("</p>")) {
																		editorContent = editorContent.substring(0, editorContent.length - 4);
																	}
																	$("#<%= "answer" + id %>").val(editorContent);
																}
															});
														</script>
													<% } %>
												<% } else if (isWord) { %>
													 <input name="<%= "word:" + id %>" type="text" value="<%= Utils.removeCRs(Utils.escapeQuotes(question.printString())) %>" title="<%= loginBean.translate("Add or edit the word") %>">
												<% } else { %>
													<% if (meta != null) { %>
														<input name="<%= "metaid:" + id %>" type="hidden" value="<%= String.valueOf(meta.getId()) %>">
													<% } %>
													<input name="<%= "question:" + id %>" type="text" value="<%= Utils.removeCRs(Utils.escapeQuotes(question.printString())) %>"
														title="<%= loginBean.translate("Enter the question, if desired, you can use the * wildcard or 'Pattern()' to make a pattern.") %>">
												<% } %>
											</td>
										</tr>
										<% if (isResponses || isConversation && (!isDefault && !isGreeting)) { %>
											<tr>
												<td>
												</td>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>
														</div>
													</span>
													<input name="<%= "auto-reduce:" + id %>" type="checkbox" <% if (bean.getAutoReduce()) { %>checked<% } %>
															title="<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses") %>">
													<span class="chat"><%= loginBean.translate("Auto Reduce") %></span>
												</td>
											</tr>
										<% } %>
										<% String sentiment = bean.getSentiment(question); %>
										<% if ((isResponses || isPhrase || isWord) && !isGreeting && !isDefault /*&& (bean.getShowSentiment() || !sentiment.isEmpty())*/) { %>
											<tr class='edit-sentiment-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Associate sentiment with the phrase or word. Sentiment can be used to track the user's experience, and is available in the bot's analytics.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Sentiment") %>:</span>
												</td>
												<td style="width:100%">
													<input id="<%= "sentiment" + id2 %>"
														name="<%= "sentiment:" + id %>" type="text"
														value="<%= sentiment %>"
														title="<%= loginBean.translate("Associate sentiment with the phrase or word. Sentiment can be used to track the user's experience, and is available in the bot's analytics.") %>">
													<script>
														$( "#<%= "sentiment" + id2 %>" ).autocomplete({
															source: [<%= bean.getAllSentimentString() %>],
															minLength: 0
														}).on('focus', function(event) {
															var self = this;
															$(self).autocomplete("search", "");
														});
													</script>
												</td>
											</tr>
										<% } %>
										<% String emotes = bean.getEmotes(question); %>
										<% if ((isPhraseSelected || isWord ) /*&& (bean.getShowEmotes() || !emotes.isEmpty())*/) { %>
											<tr class='edit-emotes-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Associate emotions with the phrase or a word. Emotions to allow the bot to determine the emotional state of the user or conversation.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Emotions") %>:</span>
												</td>
												<td>
													<input id="<%= "emotes" + id2 %>"
															name="<%= "emotes:" + id %>" type="text"
															value="<%= emotes %>"
															title="<%= loginBean.translate("Associate emotions with the phrase or a word. Emotions to allow the bot to determine the emotional state of the user or conversation.") %>">
													<script>
													$(function() {
														var emotes = [<%= bean.getAllEmotionString() %>];
														multiDropDown("#<%= "emotes" + id2 %>", emotes, false);
													});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (isWord /*&& bean.getShowSynonyms()*/) { %>
											<tr class='edit-synonyms-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Associate synonyms with the word. Synonyms allow similar words in a question to trigger the same response.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Synonyms") %>:</span>
												</td>
												<td style="width:100%">
													<input id="<%= "synonym" + id2 %>"
															name="<%= "synonym:" + id %>" type="text"
															value="<%= bean.getWordSynonyms(question) %>"
															title="<%= loginBean.translate("Associate synonyms with the word. Synonyms allow similar words in a question to trigger the same response.") %>">
												</td>
											</tr>
										<% } %>
										<% String actions = bean.getActions(question); %>
										<% if (isPhraseSelected/* && (bean.getShowActions() || !actions.isEmpty())*/) { %>
											<tr class='edit-actions-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Associate an action with the phrase. Actions can be displayed by the bot's avatar.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Actions") %>:</span>
												</td>
												<td>
													<input id="<%= "actions" + id2 %>"
															name="<%= "actions:" + id %>" type="text" 
															value="<%= actions %>"
															title="<%= loginBean.translate("Associate an action with the phrase. Actions can be displayed by the bot's avatar.") %>">
													<script>
														$(function() {
															var actions = [<%= bean.getAllActionsString() %>];
															multiDropDown("#<%= "actions" + id2 %>", actions, false);
														});
													</script>
												</td>
											</tr>
										<% } %>
										<% String poses = bean.getPoses(question); %>
										<% if (isPhraseSelected /*&& (bean.getShowPoses() || !poses.isEmpty())*/) { %>
											<tr class='edit-poses-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Associate a pose with the phrase. Poses can be displayed by the bot's avatar.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Poses") %>:</span>
												</td>
												<td>
													<input id="<%= "poses" + id2 %>"
															name="<%= "poses:" + id %>" type="text" 
															value="<%= poses %>"
															title="<%= loginBean.translate("Associate a pose with the phrase. Poses can be displayed by the bot's avatar.") %>">
													<script>
														$(function() {
															var poses = [<%= bean.getAllPosesString() %>];
															multiDropDown("#<%= "poses" + id2 %>", poses, false);
														});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (!isGreeting && !isDefault && !isWord) { %>
											<tr>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div><%= loginBean.translate("Enter the response. Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video. You can also define a template with Self code inside {} or by using 'Template()'.") %></div>
													</span>
													<span class="chat"><%= loginBean.translate("New Response") %>:</span>
												</td>
												<% if (first) { %>
													<%
														if (answer != null && answer.printString().startsWith("Template(")) {
															bean.setEditorType("markup");
														}
													%>
													<td>
														<select id="edit-editor-two" name="edit-editor" title="<%= loginBean.translate("Choose editor to edit response with. Responses can be written in WYSIWYG (rich text) or Text (HTML). Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video.") %>">
															<option value="wysiwyg" <%= bean.isEditorTypeSelected("wysiwyg") %>><%= loginBean.translate("WYSIWYG") %></option>
															<option value="markup" <%= bean.isEditorTypeSelected("markup") %>><%= loginBean.translate("Text") %></option>
														</select><br/>
													</td>
												<% } %>
											</tr>
											<tr>
												<td colspan="2">
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
															var botinstance = "<%= botBean.getInstanceId() %>";
															psdk.error = function(error) {
																console.log(error);
																document.getElementById("error-message").innerHTML = error;
																$( "#dialog-error" ).dialog( "open" );
															}
														</script>
														<% if (first) { %>
															<%
																if (answer != null && answer.printString().startsWith("Template(")) {
																	bean.setEditorType("markup");
																}
															%>
															<div id="details-text-div" style="display:<%= bean.getEditorType().equals("wysiwyg") ? "initial;" : "none;" %>">
																<textarea id="details"><%= answer != null ? Utils.escapeHTML(answer.printString()) : "" %></textarea><br/>
															</div>
														<% } %>
														<div id="<%= "text-div" + id2 %>" style="display:<%= bean.getEditorType().equals("markup") || !first ? "initial;" : "none;" %>">
															<textarea id="<%= "answer" + id2 %>" name="<%= "answer:" + id %>"><%= answer != null ? Utils.escapeHTML(answer.printString()) : "" %></textarea><br/>
															<script>
																$( "#<%= "answer" + id2 %>" ).autocomplete({
																	source: [<%= bean.getLabelValues() %>],
																	minLength: 0
																}).on('focus', function(event) {
																	var self = this;
																	$(self).autocomplete("search", "");
																});
															</script>
														</div>
													</div>
													<% if (first) { %>
														<script>
															$('#edit-editor-two').change(function() {
																var editorType = $('#edit-editor-two').val();
																if (editorType === "markup") {
																	var editorContent = tinyMCE.activeEditor.getContent();
																	if (editorContent.startsWith("<p>")) {
																		editorContent = editorContent.substring(3, editorContent.length);
																	}
																	if (editorContent.endsWith("</p>")) {
																		editorContent = editorContent.substring(0, editorContent.length - 4);
																	}
																	$("#<%= "answer" + id2 %>").val(editorContent);
																	$("#<%= "text-div" + id2 %>").css("display", "initial");
																	$("#details-text-div").css("display", "none");
																}
																else if (editorType === "wysiwyg") {
																	tinyMCE.activeEditor.setContent($("#<%= "answer" + id2 %>").val());
																	$("#<%= "text-div" + id2 %>").css("display", "none");
																	$("#details-text-div").css("display", "initial");
																}
															});
															$(document).on("submit", "form", function(){
																var editorType = $('#edit-editor-two').val();
																if (editorType === "wysiwyg") {
																	var editorContent = tinyMCE.activeEditor.getContent();
																	if (editorContent.startsWith("<p>")) {
																		editorContent = editorContent.substring(3, editorContent.length);
																	}
																	if (editorContent.endsWith("</p>")) {
																		editorContent = editorContent.substring(0, editorContent.length - 4);
																	}
																	$("#<%= "answer" + id2 %>").val(editorContent);
																}
															});
														</script>
													<% } %>
												</td>
											</tr>
										<% } %>
										<% if (isWord /*&& (bean.getShowKeyWords() || bean.isWordKeyword(question).equals("checked"))*/) { %>
											<tr class='edit-keywords-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Keywords take priority when matching responses. Never make common words like 'the', 'is' keywords.") %>
														</div>
													</span>
													<input name="<%= "word-keyword:" + id %>" type="checkbox" <%= bean.isWordKeyword(question) %> title="<%= loginBean.translate("Keywords take priority when matching responses. Never make common words like 'the', 'is' keywords.") %>">
													<span class="chat"><%= loginBean.translate("Keyword") %></span>
												</td>
											</tr>
										<% } %>
										<% if (isWord/* && (bean.getShowTopic() || bean.isWordTopic(question).equals("checked"))*/) { %>
											<tr class='edit-topic-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Topics can be used in responses to track the context of the conversation.") %>
														</div>
													</span>
													<input name="<%= "word-topic:" + id %>" type="checkbox" <%= bean.isWordTopic(question) %> title="<%= loginBean.translate("Topics can be used in responses to track the context of the conversation.") %>">
													<span class="chat"><%= loginBean.translate("Topic") %></span>
												</td>
												<td>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>
														</div>
													</span>
													<input name="<%= "exclusive-topic:" + id %>" type="checkbox" <%= bean.getWordExclusiveTopicChecked(question) %> title="<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>">
													<span class="chat"><%= loginBean.translate("Exclusive Topic") %></span>
												</td>
											</tr>
										<% } %>
										<% if (isResponses /*&& bean.getShowConfidence()*/) { %>
											<% confidence = bean.getConfidence(question, answer); %>
											<tr class='edit-confidence-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Set the responses confidence. The bot will use its most confident response, and will not use responses with less than 50% confidence or 90% in a group discussion (default is 100%).") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Confidence") %>:</span>
												</td>
												<td style="width:100%">
													<input id="<%= "confidence" + id2 %>"
															name="<%= "confidence:" + id %>" type="text"
															value="<%= answer != null ? confidence : "" %>"
															title="<%= loginBean.translate("Set the responses confidence. The bot will use its most confident response, and will not use responses with less than 50% confidence or 90% in a group discussion (default is 100%).") %>">
												</td>
											</tr>
										<% } %>
										<% if (/*(bean.getShowCondition() || bean.hasCondition(question, answer, meta)) &&*/ !isWord) { %>
											<tr class='edit-condition-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give a condition in Self code that must evaluate to true for the response to be used.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Condition") %>:</span>
												</td>
												<td><input name="<%= "condition:" + id %>" type="text" value="<%= bean.getCondition(question, answer, meta) %>" title="<%= loginBean.translate("Optionally you can give a condition in Self code that must evaluate to true for the response to be used.") %>"></td>
											</tr>
										<% } %>
										<% if (/*(bean.getShowThink() || bean.hasThink(question, answer, meta)) &&*/ !isWord) { %>
											<tr class='edit-think-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give 'think' code in Self that is evaluated when the response is used.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Think") %>:</span>
												</td>
												<td><input name="<%= "think:" + id %>" type="text" value="<%= bean.getThink(question, answer, meta) %>" title="<%= loginBean.translate("Optionally you can give 'think' code in Self that is evaluated when the response is used.") %>"></td>
											</tr>
										<% } %>
										<% if (/*(bean.getShowCommand() || bean.hasCommand(question, answer, meta)) && */!isWord) { %>
											<tr class='edit-command-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give a JSON command (Self code) that is evaluated and returned to the client to support games, and virtual assistance.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Command") %>:</span>
												</td>
												<td><input name="<%= "command:" + id %>" type="text" value="<%= bean.getCommand(question, answer, meta) %>" title="<%= loginBean.translate("Optionally you can give a JSON command (Self code) that is evaluated and returned to the client to support games, and virtual assistance.") %>"></td>
											</tr>
										<% } %>
										<% if (/*(bean.getShowTopic() || bean.hasTopic(question, answer, meta)) &&*/ !isWord) { %>
											<tr class='edit-topic-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give a topic to categorize the response under. Topics can be used in responses to track the context of the conversation. Responses with the active topic are given precedence over other responses.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Topic") %>:</span>
												</td>
												<td>
													<input id="<%= "topic" + id2 %>" name="<%= "topic:" + id %>" type="text" value="<%= bean.getTopic(question, answer, meta) %>" title="<%= loginBean.translate("Optionally you can give a topic to categorize the response under. Topics can be used in responses to track the context of the conversation. Responses with the active topic are given precedence over other responses.") %>">
													<script>
														$( "#<%= "topic" + id2 %>" ).autocomplete({
														source: [<%= bean.getTopicValues(question, answer) %>],
														minLength: 0
														}).on('focus', function(event) {
															var self = this;
															$(self).autocomplete("search", "");
														});
													</script>
												</td>
											</tr>
											<tr class='edit-topic-tr'>
												<td></td>
												<td nowrap>
													<% if (!isGreeting) { %>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("If the topic is required, the response will only be used if that topic is active in the current conversation.") %>
															</div>
														</span>
														<input name="<%= "require-topic:" + id %>" type="checkbox" <%= bean.getRequireTopicChecked(question, answer, meta) %> title="<%= loginBean.translate("If the topic is required, the response will only be used if that topic is active in the current conversation.") %>">
														<span class="chat"><%= loginBean.translate(loginBean.isMobile() ? "Required" : "Require Topic") %></span>
													<% } %>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>
														</div>
													</span>
													<input name="<%= "exclusive-topic:" + id %>" type="checkbox" <%= bean.getExclusiveTopicChecked(question, answer, meta) %> title="<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>">
													<span class="chat"><%= loginBean.translate(loginBean.isMobile() ? "Exclusive" : "Exclusive Topic") %></span>
												</td>
											</tr>
										<% } %>
										<% Vertex label = (isGreeting || isDefault) ? question : answer; %>
										<% if (/*(bean.getShowLabel() || (label != null && label.hasRelationship(Primitive.LABEL))) &&*/ !isWord) { %>
											<tr class='edit-label-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give an intent or label to reuse the response as.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Intent Label") %>:</span>
												</td>
												<td>
													<input id="<%= "label" + id2 %>" name="<%= "label:" + id %>" type="text" value="<%= bean.getLabel(label) %>" title="<%= loginBean.translate("Optionally you can give an intent or label to reuse the response as.") %>">
												</td>
											</tr>
										<% } %>
										<% if (!isGreeting && !isDefault && /*(bean.getShowKeyWords() || bean.hasKeyWords(question, answer, meta)) &&*/ !isWord) { %>
											<tr class='edit-keywords-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give keywords from the question. Keywords take priority when matching responses, the response may be used for any question that has any of the keywords. Never make common words like 'the', 'is' keywords.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Keywords") %>:</span>
												</td>
												<td>
													<input id="<%= "keywords" + id2 %>" name="<%= "keywords:" + id %>" type="text" value="<%= bean.getKeyWords(question, answer, meta) %>" title="<%= loginBean.translate("Optionally you can give keywords from the question. Keywords take priority when matching responses, the response may be used for any question that has any of the keywords. Never make common words like 'the', 'is' keywords.") %>">
													<script>
														$(function() {
															var keywords = [<%= bean.getRequiredValues(question) %>];
															multiDropDown("#<%= "keywords" + id2 %>", keywords, false);
														});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (!isGreeting && !isDefault && !isWord /*&& (bean.getShowRequired() || bean.hasRequired(question, answer, meta))*/) { %>
											<tr class='edit-required-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give required words from the question that will be required for a response match. All of the required words must be in the question for the response to be used.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Required") %>:</span>
												</td>
												<td>
													<input id="<%= "required" + id2 %>"  name="<%= "required:" + id %>" type="text" value="<%= bean.getRequired(question, answer, meta) %>" title="<%= loginBean.translate("Optionally you can give required words from the question that will be required for a response match. All of the required words must be in the question for the response to be used.") %>">
													<script>
														$(function() {
															var required = [<%= bean.getRequiredValues(question) %>];
															multiDropDown("#<%= "required" + id2 %>", required, false);
														});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (!isWord/* && bean.getShowEmotes() || bean.hasEmotes(question, answer, meta)*/) { %>
											<tr class='edit-emotes-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("You can associate emotions with the response. Emotions can be displayed by the bot's avatar.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Emotions") %>:</span>
												</td>
												<td>
													<input id="<%= "resp-emotes" + id2 %>"
															name="<%= "resp-emotes:" + id %>" type="text"
															value="<%= bean.getEmotes(question, answer, meta) %>"
															title="<%= loginBean.translate("You can associate emotions with the response. Emotions can be displayed by the bot's avatar.") %>">
													<script>
													$(function() {
														var emotes = [<%= bean.getAllEmotionString() %>];
														multiDropDown("#<%= "resp-emotes" + id2 %>", emotes, false);
													});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (!isWord /*&& bean.getShowActions() || bean.hasActions(question, answer, meta)*/) { %>
											<tr class='edit-actions-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("You can associate an action with the response. Actions can be displayed by the bot's avatar.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Actions") %>:</span>
												</td>
												<td>
													<input id="<%= "resp-actions" + id2 %>"
															name="<%= "resp-actions:" + id %>" type="text"
															value="<%= bean.getActions(question, answer, meta) %>"
															title="<%= loginBean.translate("You can associate an action with the response. Actions can be displayed by the bot's avatar.") %>">
													<script>
													$(function() {
														var actions = [<%= bean.getAllActionsString() %>];
														multiDropDown("#<%= "resp-actions" + id2 %>", actions, false);
													});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (!isWord /*&& bean.getShowPoses() || bean.hasPoses(question, answer, meta)*/) { %>
											<tr class='edit-poses-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("You can associate a pose with the response. Poses can be displayed by the bot's avatar.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Poses") %>:</span>
												</td>
												<td>
													<input id="<%= "resp-poses" + id2 %>"
															name="<%= "resp-poses:" + id %>" type="text"
															value="<%= bean.getPoses(question, answer, meta) %>"
															title="<%= loginBean.translate("You can associate a pose with the response. Poses can be displayed by the bot's avatar.") %>">
													<script>
														$(function() {
															var poses = [<%= bean.getAllPosesString() %>];
															multiDropDown("#<%= "resp-poses" + id2 %>", poses, false);
														});
													</script>
												</td>
											</tr>
										<% } %>
										<% if (!isGreeting /*&& bean.getShowRepeat()*/ && !isWord) { %>
											<tr class='edit-repeat-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give a different response to used if the current response has already been used in this conversation.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("On Repeat") %>:</span>
												</td>
												<td><input name="<%= "onrepeat:" + id %>" type="text" value="" title="<%= loginBean.translate("Optionally you can give a different response to used if the current response has already been used in this conversation.") %>"></td>
											</tr>
											<tr class='edit-repeat-tr'>
												<td></td>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Require that the response or phrase only be used once in the conversation.") %>
														</div>
													</span>
													<input name="<%= "norepeat:" + id %>" type="checkbox" <%= bean.getNoRepeatChecked(question, answer) %> title="<%= loginBean.translate("Require that the response or phrase only be used once in the conversation.") %>">
													<span class="chat"><%= loginBean.translate("No Repeat") %></span>
												</td>
											</tr>
										<% } %>
										<% if (!isGreeting && /*bean.getShowPrevious() &&*/ !isWord) { %>
											<tr class='edit-previous-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give a previous response to give the response a context. The previous response is the bot's previous response before the user's question.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Previous") %>:</span>
												</td>
												<td><input name="<%= "previous:" + id %>" type="text" value="" title="<%= loginBean.translate("Optionally you can give a previous response to give the response a context. The previous response is the bot's previous response before the user's question.") %>"></td>
											</tr>
											<tr class='edit-previous-tr'>
												<td></td>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Require that the response only be used if the previous response matches one of the previous responses.") %>
														</div>
													</span>
													<input name="<%= "require-previous:" + id %>" type="checkbox" <%= bean.getRequirePreviousChecked(question, answer, meta) %> title="<%= loginBean.translate("Require that the response only be used if the previous response matches one of the previous responses.") %>">
													<span class="chat"><%= loginBean.translate("Require Previous") %></span>
												</td>
											</tr>
										<% } %>
										<% if (/*bean.getShowNext() &&*/ (isGreeting || isDefault || isResponses || isPhrase) && !isWord) { %>
											<tr class='edit-next-tr'>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give the user's next question. Next can be used to handle follow-up questions and conversation flows.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Next") %>:</span>
												</td>
												<td width="100%"><input name="<%= "next:" + id %>" type="text" value="" title="<%= loginBean.translate("Optionally you can give the user's next question. Next can be used to handle follow-up questions and conversation flows.") %>"></td>
											</tr>
										<% } %>
										<tr><td colspan="2"><hr/></td></tr>
										<% first = false; %>
									<% } %>
								</table>
							
							<% } else { %>
							
								<table style="width:100%;" cellspacing="2">
									<% boolean first = true; %>
									<% for (Vertex input : bean.getResults()) { %>
										<% Vertex answer = input.getRelationship(Primitive.INPUT); %>
										<% Vertex questionInput = input.getRelationship(Primitive.QUESTION); %>
										<% if (questionInput == null) { %>
											<tr>
											<td></td>
											<td></td>
											<td><span class="chat"><%= loginBean.translate("Missing question to") %> "
											<% if (answer != null) { %>
												<%= Utils.escapeHTML(answer.printString()) %>
											<% } %>"</span>
											</td>
											</tr>
										<% } else { %>
											<% Vertex previousInput = questionInput.getRelationship(Primitive.QUESTION); %>
											<% if (previousInput != null) { %>
												<tr>
													<td valign="top" nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Add the bot's previous response to the response to better match the conversational context. Click both to require the previous response.") %>
															</div>
														</span>
														<input type=checkbox name="<%= "useprevious:" + input.getId() %>" title="<%= loginBean.translate("Add the bot's previous response to the response to better match the conversational context.") %>">
														<input name="<%= "require-previous:" + input.getId() %>" type="checkbox" title="<%= loginBean.translate("Require that the response only be used if the previous response matches one of the previous responses") %>">
													</td>
													<td  valign="top" nowrap>
													<% Vertex previous = previousInput.getRelationship(Primitive.INPUT); %>
													<% if (previous != null) { %>
														<span class="chat"><%= loginBean.translate("Previous") %>:</span>
													<% } %>
													</td>
													<td style="width:100%;">
													<% if (previous != null) { %>
														<span class="chat"><%= Utils.escapeHTML(previous.printString()) %></span>
													<% } %>
													</td>
												</tr>
											<% } %>
											<tr>
												<td></td>
												<td nowrap>
												<% Vertex question = questionInput.getRelationship(Primitive.INPUT); %>
												<% if (question != null) { %>
													<span class="chat"><%= loginBean.translate("Question") %>:</span>
												<% } %>
												</td>
												<td style="width:100%;">
												<% if (question != null) { %>
													<span class="chat"><%= Utils.escapeHTML(question.printString()) %></span>
												<% } %>
												</td>
											</tr>
											<tr>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>
														</div>
													</span>
													<input name="<%= "auto-reduce:" + input.getId() %>" type="checkbox" <% if (bean.getAutoReduce()) { %>checked<% } %>
																title="<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>">
												</td>
												<td nowrap>
													<span class="chat"><%= loginBean.translate("Auto Reduce") %></span>
												</td>
											</tr>
											<tr>
												<td valign="top" nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Mark the response as an invalid response for this question.") %>
														</div>
													</span>
													<input type=checkbox name="<%= "invalidate:" + input.getId() %>" title="<%= loginBean.translate("Mark the response as an invalid response for this question.") %>">
												</td>
												<td nowrap>
													<span class="chat"><%= loginBean.translate("Response") %>:</span>
												</td>
												<td style="width:100%;">
												<% if (answer != null) { %>
													<span class="chat"><%= Utils.escapeHTML(answer.printString()) %></span>
												<% } %>
												</td>
											</tr>
											<tr>
												<td></td>
												<td nowrap>
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Enter the correction to the response.") %>
														</div>
													</span>
													<span class="chat"><%= loginBean.translate("Correction") %>:</span>
												</td>
												<% if (first) { %>
													<td>
														<select id="edit-editor-three" name="edit-editor" title="<%= loginBean.translate("Choose editor to edit response with. Responses can be written in WYSIWYG (rich text) or Text (HTML). Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video.") %>">
															<option value="wysiwyg" <%= bean.isEditorTypeSelected("wysiwyg") %>><%= loginBean.translate("WYSIWYG") %></option>
															<option value="markup" <%= bean.isEditorTypeSelected("markup") %>><%= loginBean.translate("Text") %></option>
														</select><br/>
													</td>
												<% } %>
											</tr>
											<tr>
												<td></td>
												<td colspan="2">
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
															var botinstance = "<%= botBean.getInstanceId() %>";
															psdk.error = function(error) {
																console.log(error);
																document.getElementById("error-message").innerHTML = error;
																$( "#dialog-error" ).dialog( "open" );
															}
														</script>
														<% if (first) { %>
															<div id="details-text-div" style="display:<%= bean.getEditorType().equals("wysiwyg") ? "initial;" : "none;" %>">
																<textarea id="details" title="<%= loginBean.translate("Enter the correction to the response") %>"><%= answer == null ? "" : Utils.removeCRs(Utils.escapeQuotes(answer.printString())) %></textarea><br/>
															</div>
														<% } %>
														<div id="<%= "text-div" + input.getId() %>" style="display:<%= bean.getEditorType().equals("markup") || !first ? "initial;" : "none;" %>">
															<textarea id="<%= "answer" + input.getId() %>" name="<%= "answer:" + input.getId() %>" title="<%= loginBean.translate("Enter the correction to the response") %>"><%= answer == null ? "" : Utils.removeCRs(Utils.escapeQuotes(answer.printString())) %></textarea><br/>
															<script>
																$( "#<%= "answer" + input.getId() %>" ).autocomplete({
																source: [<%= bean.getLabelValues() %>],
																minLength: 0
																}).on('focus', function(event) {
																	var self = this;
																	$(self).autocomplete("search", "");
																});
															</script>
														</div>
													</div>
													<% if (first) { %>
														<script>
															$('#edit-editor-three').change(function() {
																var editorType = $('#edit-editor-three').val();
																if (editorType === "markup") {
																	var editorContent = tinyMCE.activeEditor.getContent();
																	if (editorContent.startsWith("<p>")) {
																		editorContent = editorContent.substring(3, editorContent.length);
																	}
																	if (editorContent.endsWith("</p>")) {
																		editorContent = editorContent.substring(0, editorContent.length - 4);
																	}
																	$("#<%= "answer" + input.getId() %>").val(editorContent);
																	$("#<%= "text-div" + input.getId() %>").css("display", "initial");
																	$("#details-text-div").css("display", "none");
																}
																else if (editorType === "wysiwyg") {
																	tinyMCE.activeEditor.setContent($("#<%= "answer" + input.getId() %>").val());
																	$("#<%= "text-div" + input.getId() %>").css("display", "none");
																	$("#details-text-div").css("display", "inline");
																}
															});
															$(document).on("submit", "form", function(){
																var editorType = $('#edit-editor-three').val();
																if (editorType === "wysiwyg") {
																	var editorContent = tinyMCE.activeEditor.getContent();
																	if (editorContent.startsWith("<p>")) {
																		editorContent = editorContent.substring(3, editorContent.length);
																	}
																	if (editorContent.endsWith("</p>")) {
																		editorContent = editorContent.substring(0, editorContent.length - 4);
																	}
																	$("#<%= "answer" + input.getId() %>").val(editorContent);
																}
															});
														</script>
													<% } %>
												</td>
											</tr>
											<% if (/*(bean.getShowCondition() || bean.hasCondition(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-condition-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give a condition in Self code that must evaluate to true for the response to be used.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Condition") %>:</span>
													</td>
													<td><input name="<%= "condition:" + + input.getId() %>" type="text" value="<%= bean.getCondition(question, answer, null) %>" title="<%= loginBean.translate("Optionally you can give a condition in Self code that must evaluate to true for the response to be used.") %>"></td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowThink() || bean.hasThink(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-think-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give 'think' code in Self that is evaluated when the response is used.") %>
														</div>
													</span>
														<span class="chat"><%= loginBean.translate("Think") %>:</span>
													</td>
													<td><input name="<%= "think:" + + input.getId() %>" type="text" value="<%= bean.getThink(question, answer, null) %>" title="<%= loginBean.translate("Optionally you can give 'think' code in Self that is evaluated when the response is used.") %>"></td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowCommand() || bean.hasCommand(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-command-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Optionally you can give a JSON command (Self code) that is evaluated and returned to the client to support games, and virtual assistance.") %>
														</div>
													</span>
														<span class="chat"><%= loginBean.translate("Command") %>:</span>
													</td>
													<td><input name="<%= "command:" + + input.getId() %>" type="text" value="<%= bean.getCommand(question, answer, null) %>" title="<%= loginBean.translate("Optionally you can give a JSON command (Self code) that is evaluated and returned to the client to support games, and virtual assistance.") %>"></td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowTopic() || bean.hasTopic(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-topic-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give a topic to categorize the response under. Topics can be used in responses to track the context of the conversation. Responses with the active topic are given precedence over other responses.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Topic") %>:</span>
													</td>
													<td>
														<input id="<%= "topic" + input.getId() %>" name="<%= "topic:" + input.getId() %>" type="text" value="<%= bean.getTopic(question, answer, null) %>"
																	 title="<%= loginBean.translate("Optionally you can give a topic to categorize the response under. Topics can be used in responses to track the context of the conversation. Responses with the active topic are given precedence over other responses.") %>">
														<script>
															$(function() {
																var topic = [<%= bean.getTopicValues(question, null) %>];
																multiDropDown("#<%= "topic" + input.getId() %>", topic, false);
															});
														</script>
													</td>
												</tr>
												<tr class="edit-topic-tr">
													<td></td>
													<td></td>
													<td nowrap>
														<% if (!isGreeting) { %>
															<span class="dropt-banner">
																<img id="help-mini" src="images/help.svg"/>
																<div>
																	<%= loginBean.translate("If the topic is required, the response will only be used if that topic is active in the current conversation.") %>
																</div>
															</span>
															<input name="<%= "require-topic:" + input.getId() %>" type="checkbox" <%= bean.getRequireTopicChecked(question, answer, null) %> title="<%= loginBean.translate("If the topic is required, the response will only be used if that topic is active in the current conversation.") %>">
															<span class="chat"><%= loginBean.translate(loginBean.isMobile() ? "Required" : "Require Topic") %></span>
														<% } %>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>
															</div>
														</span>
														<input name="<%= "exclusive-topic:" + input.getId() %>" type="checkbox" <%= bean.getExclusiveTopicChecked(question, answer, null) %> title="<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>">
														<span class="chat"><%= loginBean.translate(loginBean.isMobile() ? "Exclusive" : "Exclusive Topic") %></span>
													</td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowLabel() || (answer != null && answer.hasRelationship(Primitive.LABEL))) &&*/ !isWord) { %>
												<tr class="edit-label-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give an intent or label to reuse the response as.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Intent Label") %>:</span>
													</td>
													<td>
														<input id="<%= "label" + input.getId() %>" name="<%= "label:" + input.getId() %>" type="text" value="<%= bean.getLabel(answer) %>"
																	 title="<%= loginBean.translate("Optionally you can give an intent or label to reuse the response as.") %>">
													</td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowKeyWords() || bean.hasKeyWords(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-keywords-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give keywords from the question. Keywords take priority when matching responses, the response may be used for any question that has any of the keywords. Never make common words like 'the', 'is' keywords.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Keywords") %>:</span>
													</td>
													<td>
														<input id="<%= "keywords" + input.getId() %>" name="<%= "keywords:" + input.getId() %>" type="text" value="<%= bean.getKeyWords(question, answer, null) %>"
																	 title="<%= loginBean.translate("Optionally you can give keywords from the question. Keywords take priority when matching responses, the response may be used for any question that has any of the keywords. Never make common words like 'the', 'is' keywords.") %>">
														<script>
															$(function() {
																var keywords = [<%= bean.getRequiredValues(question) %>];
																multiDropDown("#<%= "keywords" + input.getId() %>", keywords, false);
															});
														</script>
													</td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowRequired() || bean.hasRequired(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-required-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give required words from the question that will be required for a response match.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Required") %>:</span>
													</td>
													<td>
														<input id="<%= "required" + input.getId() %>" name="<%= "required:" + input.getId() %>" type="text" value="<%= bean.getRequired(question, answer, null) %>"
																	 title="<%= loginBean.translate("Optionally you can give required words from the question that will be required for a response match.") %>">
														<script>
														$(function() {
															var required = [<%= bean.getRequiredValues(question) %>];
															multiDropDown("#<%= "required" + input.getId() %>", required, false);
														});
														</script>
													</td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowEmotes() || bean.hasEmotes(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-emotes-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("You can associate emotions with the response. Emotions can be displayed by the bot's avatar.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Emotions") %>:</span>
													</td>
													<td>
														<input id="<%= "emotes" + input.getId() %>"
																name="<%= "emotes:" + input.getId() %>" type="text"
																value="<%= bean.getEmotes(question, answer, null) %>"
																title="<%= loginBean.translate("You can associate emotions with the response. Emotions can be displayed by the bot's avatar.") %>">
														<script>
														$(function() {
															var emotes = [<%= bean.getAllEmotionString() %>];
															multiDropDown("#<%= "emotes" + input.getId() %>", emotes, false);
														});
														</script>
													</td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowActions() || bean.hasActions(question, answer, null)) &&*/ !isWord) { %>
												<tr class="edit-actions-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("You can associate an action with the response. Actions can be displayed by the bot's avatar.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Actions") %>:</span>
													</td>
													<td>
														<input id="<%= "actions" + input.getId() %>"
																name="<%= "actions:" + input.getId() %>" type="text"
																value="<%= bean.getActions(question, answer, null) %>"
																title="<%= loginBean.translate("You can associate an action with the response. Actions can be displayed by the bot's avatar.") %>">
														<script>
														$(function() {
															var actions = [<%= bean.getAllActionsString() %>];
															multiDropDown("#<%= "actions" + input.getId() %>", actions, false);
														});
														</script>
													</td>
												</tr>
											<% } %>
											<% if (/*(bean.getShowPoses() || bean.hasPoses(question, answer, null)) && */!isWord) { %>
												<tr class="edit-poses-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("You can associate a pose with the response. Poses can be displayed by the bot's avatar.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Poses") %>:</span>
													</td>
													<td>
														<input id="<%= "poses" + input.getId() %>"
																name="<%= "poses:" + input.getId() %>" type="text"
																value="<%= bean.getPoses(question, answer, null) %>"
																title="<%= loginBean.translate("You can associate a pose with the response. Poses can be displayed by the bot's avatar.") %>">
														<script>
															$(function() {
																var poses = [<%= bean.getAllPosesString() %>];
																multiDropDown("#<%= "poses" + input.getId() %>", poses, false);
															});
														</script>
													</td>
												</tr>
											<% } %>
											<% if (!isGreeting /*&& bean.getShowRepeat()*/ && !isWord) { %>
												<tr class="edit-repeat-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give a different response to used if the current response has already been used in this conversation.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("On Repeat") %>:</span>
													</td>
													<td><input name="<%= "onrepeat:" + input.getId() %>" type="text" value="" title="<%= loginBean.translate("Optionally you can give a different response to used if the current response has already been used in this conversation.") %>"></td>
												</tr>
												<tr class="edit-repeat-tr">
													<td></td>
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Require that the response or phrase only be used once.") %>
															</div>
														</span>
														<input name="<%= "norepeat:" + input.getId() %>" type="checkbox" <%= bean.getNoRepeatChecked(question, answer) %> title="<%= loginBean.translate("Require that the response or phrase only be used once") %>">
														<span class="chat"><%= loginBean.translate("No Repeat") %></span>
													</td>
												</tr>
											<% } %>
											<% if (true/*bean.getShowNext() || bean.hasNext(question, answer, null)*/) { %>
												<tr class="edit-next-tr">
													<td></td>
													<td nowrap>
														<span class="dropt-banner">
															<img id="help-mini" src="images/help.svg"/>
															<div>
																<%= loginBean.translate("Optionally you can give the user's next question. Next can be used to handle follow-up questions and conversation flows.") %>
															</div>
														</span>
														<span class="chat"><%= loginBean.translate("Next") %>:</span>
													</td>
													<td>
														<input 
															id="<%= "next" + input.getId() %>"
															name="<%= "next:" + input.getId() %>" type="text"
															value="<%= bean.getNext(question, answer) %>"
															title="<%= loginBean.translate("Optionally you can give the user's next question. Next can be used to handle follow-up questions and conversation flows.") %>">
													</td>
												</tr>
											<% } %>
											<tr>
												<td></td>
												<td></td>
												<td valign="top">
													<span class="dropt-banner">
														<img id="help-mini" src="images/help.svg"/>
														<div>
															<%= loginBean.translate("Process the new response through the bot's learning and comprehension.") %>
														</div>
													</span>
													<input type=checkbox name="<%= "comprehension:" + input.getId() %>" title="<%= loginBean.translate("Process the new response through the bot's learning and comprehension") %>">
													<span class="chat"><%= loginBean.translate("Process learning and comprehension") %></span>
												</td>
											</tr>
										 <% } %>
										 <tr><td colspan="3"><hr/></td></tr>
										 <% first = false; %>
									<% } %>
								</table>
							<% } %>
							
							<input id="ok" type="submit" name="save" value="<%= loginBean.translate("Save") %>" title="<%= loginBean.translate("Save the corrections to the phrases") %>">
							<input id="cancel" type="submit" name="cancel" value="<%= loginBean.translate("Cancel") %>"><br/>
							
						<% } else { %>
								
							<h3><%= loginBean.translate("Conversations") %></h3>
							<% for (Vertex conversation : bean.getResults()) { %>
								<span class="chat">
								<input type=checkbox <%= bean.isSelectAll() ? "checked" : "" %> name=<%= "conversation:" + conversation.getId() %> title="<%= loginBean.translate("Select conversation for deletion") %>">
								<%= bean.getType(conversation) %> - 
								<%= Utils.printDate(conversation.getCreationDate()) %>
								</span>
								<% List<Relationship> inputs = conversation.orderedRelationships(Primitive.INPUT); %>
								
								<% boolean color = false; %>
								<% if (inputs != null) { %>
									<table style="width:100%;" cellspacing="2">
										<% for (Relationship inputRelationship : inputs) { %>
											<% Vertex input = inputRelationship.getTarget(); %>
											<% boolean correction = input.hasRelationship(Primitive.ASSOCIATED, Primitive.CORRECTION); %>
											<% boolean offended = input.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE); %>
											<% Vertex question = input.getRelationship(Primitive.PREVIOUS); %>
											<% Vertex answer = input.getRelationship(Primitive.INPUT); %>
											<% boolean offensive = (answer != null) && answer.hasRelationship(Primitive.ASSOCIATED, Primitive.OFFENSIVE); %>
											<% boolean isMimic = input.hasRelationship(Primitive.MIMIC); %>
											<% Relationship relationship = null; %>
											<% if ((question != null) && (answer != null) && (question.getRelationship(Primitive.INPUT) != null)) { %>
												<% relationship = question.getRelationship(Primitive.INPUT).getRelationship(Primitive.RESPONSE, answer); %>
											<% } %>
											<% boolean isResponse = (relationship != null) && (relationship.getCorrectness() > 0); %>
											<% boolean isInvalid = (relationship != null) && (relationship.getCorrectness() < 0); %>
											<tr>
											<td valign="top"><input type=checkbox name=<%= "input:" + input.getId() %> title="<%= loginBean.translate("Select response for correction, validation, invalidation, flagging, or deletion") %>"></td>
											<td valign="top" nowrap  class="chat-user"><span class="menu"><%= Utils.displayTime(input.getCreationDate()) %></span>
											<% if (loginBean.isMobile()) { %>
												<br/>
											<% } else { %>
												</td>
												<td valign="top" nowrap>
											<% } %>
											<% if (color) { %>
												<span class="chat-user" style="color:#333">
											<% } else { %>
												<span class="chat-user">
											<% } %>
											<%= bean.getName(input.getRelationship(Primitive.SPEAKER)) %>: </span>
											</td>
											<% if (color) { %>
												<td valign="top" align="left" width="100%" style="width:100%;background-color: #d5d5d5">
											<% } else { %>
												<td valign="top" style="width:100%;">
											<% } %>
											<% if (offended) { %>
												<span class="chat-flagged">
											<% } else if (offensive) { %>
												<span class="chat" style="color:#F58723">
											<% } else if (correction) { %>
												<span class="chat" style="color:green">
											<% } else if (isResponse) { %>
												<span class="chat-response">
											<% } else if (isInvalid) { %>
												<span class="chat-inverse">
											<% } else if (isMimic) { %>
												<span class="chat" style="color:#968EF3">
											<% } else if (color) { %>
												<span class="chat" style="color:#333">
											<% } else { %>
												<span class="chat">
											<% } %>
											<% if (answer != null) { %>
												<%= Utils.escapeHTML(answer.printString()) %>
											<% } %>
											</span>
											<% if (input.getRelationships(Primitive.EMOTION) != null) { %>
												<table>
													<tr>
													<%= loginBean.isMobile() ? "" : "<td></td>" %>
													<td></td>
													<td style="width:100%;"><span class="chat-emote"><%= bean.getEmotes(input) %></span></td>
													</tr>
												</table>
											<% } %>
											<% if (input.hasRelationship(Primitive.ACTION)) { %>
												<table>
													<tr>
													<%= loginBean.isMobile() ? "" : "<td></td>" %>
													<td></td>
													<td style="width:100%;"><span class="chat-action"><%= bean.getActions(input) %></span></td>
													</tr>
												</table>
											<% } %>
											<% if (input.hasRelationship(Primitive.POSE)) { %>
												<table>
													<tr>
													<%= loginBean.isMobile() ? "" : "<td></td>" %>
													<td></td>
													<td style="width:100%;"><span class="chat-pose"><%= bean.getPoses(input) %></span></td>
													</tr>
												</table>
											<% } %>
											<% if (input.hasRelationship(Primitive.COMMAND)) { %>
												<table>
													<tr>
													<%= loginBean.isMobile() ? "" : "<td></td>" %>
													<td></td>
													<td style="width:100%;"><span class="chat-template"><%= bean.getCommand(input) %></span></td>
													</tr>
												</table>
											<% } %>
											<% if ((answer != null) && (answer.getRelationships(Primitive.ONREPEAT) != null)) { %>
												<% List<Relationship> repeats = answer.orderedRelationships(Primitive.ONREPEAT); %>
												<% if (repeats != null) { %>
													<table>
													<% for (Relationship repeat : repeats) { %>
														<% String style = "chat-repeat"; %>
														<% if (repeat.getCorrectness() < 0) { style = "chat-inverse"; }; %>
														<tr class='edit-repeat-tr'>
														<td valign="top"><input type=checkbox name=<%= "repeat:" + answer.getId() + ":" + repeat.getTarget().getId() %> title="<%= loginBean.translate("Select on repeat response for validation, invalidation, or deletion") %>">
														<%= loginBean.isMobile() ? "" : "</td><td valign='top'>" %>
														<span class="chat"><%= (int)(repeat.getCorrectness() * 100) %></span></td>
														<td style="width:100%;"><span class="<%= style %>"><%= bean.getResponse(repeat.getTarget()) %></span></td>
														</tr>
													<% } %>
													</table>
												<% } %>
											<% } %>
											<% if ((relationship != null) && relationship.hasMeta()) { %>
												<% if (relationship.getMeta().hasRelationship(Primitive.TOPIC)) { %>
													<table>
														<tr>
														<%= loginBean.isMobile() ? "" : "<td></td>" %>
														<td></td>
														<td style="width:100%;"><span class="chat-topic"><%= bean.getTopic(relationship.getSource(), relationship.getTarget(), null) %></span></td>
														</tr>
													</table>
												<% } %>
											<% } %>
											<% if ((answer != null)) { %>
												<% if (answer.hasRelationship(Primitive.LABEL)) { %>
													<table>
														<tr>
														<%= loginBean.isMobile() ? "" : "<td></td>" %>
														<td></td>
														<td style="width:100%;"><span class="chat-label"><%= bean.getLabel(answer) %></span></td>
														</tr>
													</table>
												<% } %>
											<% } %>
											<% if ((relationship != null) && relationship.hasMeta()) { %>
												<% if (relationship.getMeta().hasRelationship(Primitive.KEYWORD)) { %>
													<table>
														<tr>
														<%= loginBean.isMobile() ? "" : "<td></td>" %>
														<td></td>
														<td style="width:100%;"><span class="chat-keyword"><%= bean.getKeyWords(relationship.getSource(), relationship.getTarget(), null) %></span></td>
														</tr>
													</table>
												<% } %>
											<% } %>
											<% if ((relationship != null) && relationship.hasMeta()) { %>
												<% if (relationship.getMeta().hasRelationship(Primitive.REQUIRED)) { %>
													<table>
														<tr>
														<%= loginBean.isMobile() ? "" : "<td></td>" %>
														<td></td>
														<td style="width:100%;"><span class="chat-required"><%= bean.getRequired(relationship.getSource(), relationship.getTarget(), null) %></span></td>
														</tr>
													</table>
												<% } %>
											<% } %>
											<% if ((relationship != null) && relationship.hasMeta()) { %>
												<% List<Relationship> previous = relationship.getMeta().orderedRelationships(Primitive.PREVIOUS); %>
												<% if (previous != null) { %>
													<table>
													<% for (Relationship prev : previous) { %>
														<% String style = "chat-response"; %>
														<% if (prev.getCorrectness() < 0) { style = "chat-inverse"; }; %>
														<tr class='edit-previous-tr'>
														<td valign="top"><input type=checkbox name=<%= "previous:" + relationship.getMeta().getId() + ":" + prev.getTarget().getId() %> title="<%= loginBean.translate("Select previous response for validation, invalidation, or deletion") %>">
														<%= loginBean.isMobile() ? "" : "</td><td valign='top'>" %>
														<span class="chat"><%= (int)(prev.getCorrectness() * 100) %></span></td>
														<td style="width:100%;"><span class="<%= style %>"><%= bean.getResponse(prev.getTarget()) %></span></td>
														</tr>
													<% } %>
													</table>
												<% } %>
											<% } %>
											<% if ((relationship != null) && relationship.hasMeta()) { %>
												<% List<Relationship> nextRelationships = relationship.getMeta().orderedRelationships(Primitive.NEXT); %>
												<% if (nextRelationships != null) { %>
													<table>
													<% for (Relationship next : nextRelationships) { %>
														<% String style = "chat-next"; %>
														<% if (next.getCorrectness() < 0) { style = "chat-inverse"; }; %>
														<tr class='edit-next-tr'>
														<td valign="top"><input type=checkbox name=<%= "next:" + relationship.getMeta().getId() + ":" + next.getTarget().getId() %>  title="<%= loginBean.translate("Select next response for validation, invalidation, or deletion") %>"></td>
														<%= loginBean.isMobile() ? "" : "</td><td valign='top'>" %>
														<span class="chat"><%= (int)(next.getCorrectness() * 100) %></span></td>
														<td style="width:100%;"><span class="<%= style %>"><%= bean.getResponse(next.getTarget()) %></span></td>
														</tr>
													<% } %>
													</table>
												<% } %>
											<% } %>
											</td>
											</tr>
											<% color = !color; %>
										<% } %>
									</table>
								<% } %>
								<hr/>
							<% } %>
							
						<% } %>
						
						<% if (!bean.isCorrection() && (bean.getResults() != null) && (bean.getResults().size() > 10)) { %>
							<% if ((bean.getPage() > 0) || bean.getResultsSize() > bean.getPageSize()) { %>
									<% if (bean.getPage() > 0) { %>
									<a class="menu" href="chat-log?page=<%= bean.getPage() - 1 %>"><%= loginBean.translate("Previous") %></a>
									<% if (bean.getResultsSize() > ((bean.getPage() + 1) * bean.getPageSize())) { %>
										<span class = "menu"> | </span>
									<% } %>
								<% } %>
								<% if (bean.getResultsSize() > ((bean.getPage() + 1) * bean.getPageSize())) { %>
									<a class="menu" href="chat-log?page=<%= bean.getPage() + 1 %>"><%= loginBean.translate("Next") %></a>
								<% } %>
								<% for (int index = 0; (index * bean.getPageSize()) < bean.getResultsSize(); index++) { %>
									<span class = "menu"> | </span>
									<% if (index == bean.getPage()) { %>
										<a class="menu" href="chat-log?page=<%= index %>"><b><%= index + 1 %></b></a>
									<% } else { %>
										<a class="menu" href="chat-log?page=<%= index %>"><%= index + 1 %></a>
									<% } %>
								<% } %>
								<br/>
							<% } %>
							<span class = "menu"><%= bean.getResultsSize() %> <%= loginBean.translate("results") %>.</span><br/>

							<br/>
						<% } %>
					<% } %>
					<p/>
					</form>

					<div id="dialog-button" title="<%= loginBean.translate("Insert Button") %>" class="dialog">
						<table>
							<tr>
								<td>
									<span style="line-height:16px;left:0px;top:7px;width:120px;height:16px;"><%= loginBean.translate("Name") %></span>
									<input id="button-name" type="text" name="button-name" style="width:250px;"/>
									</br>
									<!-- <input id="insert-button" class="ok" name="add-button" style="text-align:center;" value="<%= loginBean.translate("Insert") %>"/>
									<input id="cancel-insert-button" class="cancel" name="cancel" value="<%= loginBean.translate("Cancel") %>"/> -->
								</td>
							</tr>
						</table>
					</div>
					<div id="dialog-import" title="<%= loginBean.translate("Import") %>" class="dialog">
						<form action="chat-log-upload" method="post" enctype="multipart/form-data" class="message">
							<%= loginBean.postTokenInput() %>
							<%= botBean.instanceInput() %>
							<table>
							<tr><td>
							<%= loginBean.translate("File") %></td><td>
							<input id="import-file" type="file" name="file" multiple="multiple"/>
							</td></tr>
							<tr><td>
							<%= loginBean.translate("Format") %></td><td>
							<select name="import-format" title="<%= loginBean.translate("Choose the format of the script file to import") %>" style="width:150px">
								<option value="Response List"><%= loginBean.translate("Response List") %></option>
								<option value="CSV List"><%= loginBean.translate("CSV List") %></option>
								<option value="Chat Log"><%= loginBean.translate("Chat Log") %></option>
								<option value="AIML"><%= loginBean.translate("AIML") %></option>
							</select>
							<a href="#" onclick="$('#dialog-import-info').dialog('open'); return false;"><img id="help-icon" src="images/help.svg"/></a>
							</td></tr>
							<tr><td>
							<%= loginBean.translate("Encoding") %></td><td>
							<input style="width:150px" name="import-encoding" type="text" value="UTF-8" title="<%= loginBean.translate("Some files may require you to set the character enconding to import correctly") %>"/>
							</td></tr>
							</table>
							<input type="checkbox" checked="checked" name="autoReduce" title="<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>"><%= loginBean.translate("Auto Reduce") %></input><br/>
							<input type="checkbox" checked="checked" name="pin" title="<%= loginBean.translate("Pin the responses in the bot's memory so it will never forget them") %>"><%= loginBean.translate("Pin") %></input><br/>
							<input type="checkbox" name="comprehension" title="<%= loginBean.translate("Process the conversation through the bot's understanding, learning and comprehension (this will take much longer to import)") %>"><%= loginBean.translate("Process learning and comprehension") %></input><br/>
							<input class="ok" type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="<%= loginBean.translate("Upload and import a chat log, response list, or AIML script") %>">
							<input id="cancel-import" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
						</form>
					</div>

					<div id="dialog-export" title="<%= loginBean.translate("Export") %>" class="dialog">
						<form action="chat-log" method="post" class="message">
							<%= loginBean.postTokenInput() %>
							<%= botBean.instanceInput() %>
							<%= loginBean.translate("Format") %>
							<select name="export-format" title="<%= loginBean.translate("Choose the format to export the results to") %>">
								<option value="responses"><%= loginBean.translate("Response List") %></option>
								<option value="chatlog" <%= isConversation ? "selected=\"selected\"" : "" %>><%= loginBean.translate("Chat Log") %></option>
								<option value="aiml"><%= loginBean.translate("AIML") %></option>
							</select><br/>
							<span style="line-height:16px;left:0px;top:7px;width:120px;height:16px;"><%= loginBean.translate("Pages") %>&nbsp&nbsp</span>
							<input id="exportNumberPages" type="text" name="exportNumberPages" style="width:143px;height:27px;" value="1" title="<%= loginBean.translate("Number of pages to be exported") %>"></input><br/>
							<input type="checkbox" name="exportGreetings" title="<%= loginBean.translate("Export greetings") %>"><%= loginBean.translate("Greetings") %></input>
							<input type="checkbox" name="exportDefaultResponses" title="<%= loginBean.translate("Export default responses") %>"><%= loginBean.translate("Default Responses") %></input>
							<input id="export" class="ok" type="submit" name="export" value="<%= loginBean.translate("Export") %>" title="<%= loginBean.translate("Export and download the results as a chat log file, response list, or AIML script") %>"/>
							<input id="cancel-export" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
						</form>
					</div>
					
					<div id="dialog-add-response" title="<%= loginBean.translate("Add Response") %>" class="dialog">			
						<div id="dialog-add-response-basic">
							<table id="dialog-add-response-basic-table">
								<tr id="dial-question-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Enter the question, if desired, you can use the * wildcard or 'Pattern()' to make a pattern.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Question") %>:</span>
									</td>
									<td><input class="dial-input" id="dial-add-question" type="text" onblur="updateKeywordAndRequiredInputs()" title="<%= loginBean.translate("Enter the question, if desired, you can use the * wildcard or 'Pattern()' to make a pattern.") %>"></td>
								</tr>
								
								<tr id="dial-auto-reduce-tr">
									<td></td>
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>
											</div>
										</span>
										<input id="dial-add-auto-reduce" type="checkbox" checked title="<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>">
										<span class="chat"><%= loginBean.translate("Auto Reduce") %></span>
									</td>
								</tr>
								
								<tr>
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div id="dial-response-help-div"><%= loginBean.translate("Enter the response. Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video. You can also define a template with Self code inside {} or by using 'Template()'.") %></div>
										</span>
										<span id="dial-response-span" class="chat"><%= loginBean.translate("New Response") %>:</span>
									</td>
									<td width="100%">
										<select id="edit-editor-dial" name="edit-editor" title="<%= loginBean.translate("Choose editor to edit response with. Responses can be written in WYSIWYG (rich text) or Text (HTML). Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video.") %>">
											<option value="wysiwyg" <%= bean.isEditorTypeSelected("wysiwyg") %>><%= loginBean.translate("WYSIWYG") %></option>
											<option value="markup" <%= bean.isEditorTypeSelected("markup") %>><%= loginBean.translate("Text") %></option>
										</select><br/>
									</td>
								</tr>
							</table>
							<div id="details-div-dial" class="ui-front" style="display:inline;background-color:#fff">
								<div id="details-text-div-dial" style="display:<%= bean.getEditorType().equals("wysiwyg") ? "initial;" : "none;" %>">
									<textarea id="details"></textarea><br/>
								</div>
								<div id="text-div-dial" style="display:<%= bean.getEditorType().equals("markup") || !true ? "initial;" : "none;" %>">
									<textarea rows='6' class="dial-input" id="answer-dial" name="answer-dial" type="text" title="<%= loginBean.translate("Edit the") %> <%= loginBean.translate(isGreeting ? "greeting" : "default Response") %>"></textarea></br>
								</div>
							</div>
							<script>
								$('#edit-editor-dial').change(function() {
									var editorType = $('#edit-editor-dial').val();
									if (editorType === "markup") {
										var editorContent = tinyMCE.activeEditor.getContent();
										if (editorContent.startsWith("<p>")) {
											editorContent = editorContent.substring(3, editorContent.length);
										}
										if (editorContent.endsWith("</p>")) {
											editorContent = editorContent.substring(0, editorContent.length - 4);
										}
										$('#answer-dial').val(editorContent);
										$('#text-div-dial').css("display", "initial");
										$("#details-text-div-dial").css("display", "none");
									}
									else if (editorType === "wysiwyg"){
										tinyMCE.activeEditor.setContent($("#answer-dial").val());
										$('#text-div-dial').css("display", "none");
										$("#details-text-div-dial").css("display", "initial");
									}
								});
							</script>
							<script>
								$( "#answer-dial" ).autocomplete({
									source: [<%= bean.getLabelValues() %>],
									minLength: 0
								}).on('focus', function(event) {
									var self = this;
									$(self).autocomplete("search", "");
									return false;
								});
							</script>
						</div>
						
						<div>
							<input id="dial-add-question-id" type="hidden" value="">
							<input id="dial-add-response-id" type="hidden" value="">
							<input id="dial-add-question-parent-id" type="hidden" value="">
							<input id="dial-add-response-parent-id" type="hidden" value="">
							<input id="dial-add-meta-id" type="hidden" value="">
							<input id="dial-add-meta-parent-id" type="hidden" value="">
						</div>
						
						<div id="dialog-add-response-advanced">
							<div>
								<h4><%= loginBean.translate("Advanced") %></h4>
							</div>
						
							<div id="dialog-add-response-checkbox-div">
								<div style="display:inline-block;"><input id="dial-check-all" class="search" type="checkbox" name="all" title="<%= loginBean.translate("Show the responses details") %>"><span><%= loginBean.translate("All") %></span></div>
								<div style="display:inline-block;"><input id="dial-check-topic" class="search" type="checkbox" name="topic" <% if (bean.getShowTopic()) { %>checked<% } %> title="<%= loginBean.translate("Show the topic of the responses") %>"><span><%= loginBean.translate("Topic") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-label" class="search" type="checkbox" name="label" <% if (bean.getShowLabel()) { %>checked<% } %> title="<%= loginBean.translate("Show the intent label of the responses") %>"><span><%= loginBean.translate("Intent Label") %></span></div>
								<div style="display:inline-block;"><input id="dial-check-keywords" class="search" type="checkbox" name="keywords" <% if (bean.getShowKeyWords()) { %>checked<% } %> title="<%= loginBean.translate("Show the question keywords to match the responses") %>"><span><%= loginBean.translate("Keywords") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-required" class="search" type="checkbox" name="required" <% if (bean.getShowRequired()) { %>checked<% } %> title="<%= loginBean.translate("Show the question required words to match the responses") %>"><span><%= loginBean.translate("Required") %></span></div>
								<div style="display:inline-block;"><input id="dial-check-emotes" class="search" type="checkbox" name="emotes" <% if (bean.getShowEmotes()) { %>checked<% } %> title="<%= loginBean.translate("Show the emotions of the responses") %>"><span><%= loginBean.translate("Emotions") %></span></div>
								<div style="display:inline-block;"><input id="dial-check-sentiment" class="search" type="checkbox" name="sentiment" <% if (bean.getShowSentiment()) { %>checked<% } %> title="<%= loginBean.translate("Show user sentiment of the responses") %>"><span><%= loginBean.translate("Sentiment") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-actions" class="search" type="checkbox" name="actions" <% if (bean.getShowActions()) { %>checked<% } %> title="<%= loginBean.translate("Show the actions of the responses") %>"><span><%= loginBean.translate("Actions") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-poses" class="search" type="checkbox" name="poses" <% if (bean.getShowPoses()) { %>checked<% } %> title="<%= loginBean.translate("Show the poses of the responses") %>"><span><%= loginBean.translate("Poses") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-previous" class="search" type="checkbox" name="previous" <% if (bean.getShowPrevious()) { %>checked<% } %> title="<%= loginBean.translate("Show the previous questions to the responses") %>"><span><%= loginBean.translate("Previous") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-repeat" class="search" type="checkbox" name="repeat" <% if (bean.getShowRepeat()) { %>checked<% } %> title="<%= loginBean.translate("Show the response repeat options") %>"><span><%= loginBean.translate("Repeat") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-condition" class="search" type="checkbox" name="condition" <% if (bean.getShowCondition()) { %>checked<% } %> title="<%= loginBean.translate("Show the response condition code") %>"><span><%= loginBean.translate("Condition") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-think" class="search" type="checkbox" name="think" <% if (bean.getShowThink()) { %>checked<% } %> title="<%= loginBean.translate("Show the response think code") %>"><span><%= loginBean.translate("Think") %></span></div>
								<div style="display:<%= isWord ? "none;" : "inline-block;"%>"><input id="dial-check-command" class="search" type="checkbox" name="command" <% if (bean.getShowCommand()) { %>checked<% } %> title="<%= loginBean.translate("Show the response JSON client command") %>"><span><%= loginBean.translate("Command") %></span></div>
								<div style="display:<%= isWord ? "inline-block;" : "none;" %>"><input id="dial-check-synonyms" class="search" type="checkbox" name="synonyms" <% if (bean.getShowSynonyms()) { %>checked<% } %> title="<%= loginBean.translate("Show word's synonyms") %>"><span><%= loginBean.translate("Synonyms") %></span></div>
							</div>
							</br>
							
							<table id="dialog-add-response-advanced-table">
								<tr id="dial-sentiment-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Associate sentiment with the phrase or word. Sentiment can be used to track the user's experience, and is available in the bot's analytics.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Sentiment") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input dial-autocomplete" id="dial-add-sentiment" name="dial-add-sentiment" value="" type="text" title="<%= loginBean.translate("Associate sentiment with the phrase or word. Sentiment can be used to track the user's experience, and is available in the bot's analytics.") %>">
										<script>
										$( "#<%= "dial-add-sentiment" %>" ).autocomplete({
												source: [<%= bean.getAllSentimentString() %>],
												minLength: 0,
												appendTo: "#dialog-add-response"
											}).on('focus', function(event) {
												var self = this;
												$(self).autocomplete("search", "");
											});
										</script>
									</td>
								</tr>
											
								<tr id="dial-condition-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give a condition in Self code that must evaluate to true for the response to be used.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Condition") %>:</span>
									</td>
									<td style="width:100%"><input class="dial-input" id="dial-add-condition" type="text" title="<%= loginBean.translate("Optionally you can give a condition in Self code that must evaluate to true for the response to be used.") %>"></td>
								</tr>
								
								<tr id="dial-think-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Optionally you can give 'think' code in Self that is evaluated when the response is used.") %>
										</div>
									</span>
										<span class="chat"><%= loginBean.translate("Think") %>:</span>
									</td>
									<td style="width:100%"><input class="dial-input" id="dial-add-think" type="text" title="<%= loginBean.translate("Optionally you can give 'think' code in Self that is evaluated when the response is used.") %>"></td>
								</tr>
								
								<tr id="dial-command-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
										<img id="help-mini" src="images/help.svg"/>
										<div>
											<%= loginBean.translate("Optionally you can give a JSON command (Self code) that is evaluated and returned to the client to support games, and virtual assistance.") %>
										</div>
									</span>
										<span class="chat"><%= loginBean.translate("Command") %>:</span>
									</td>
									<td style="width:100%"><input class="dial-input" id="dial-add-command" type="text" title="<%= loginBean.translate("Optionally you can give a JSON command (Self code) that is evaluated and returned to the client to support games, and virtual assistance.") %>"></td>
								</tr>
								
								<tr id="dial-topic-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give a topic to categorize the response under. Topics can be used in responses to track the context of the conversation. Responses with the active topic are given precedence over other responses.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Topic") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input" id="dial-add-topic" type="text" title="<%= loginBean.translate("Optionally you can give a topic to categorize the response under. Topics can be used in responses to track the context of the conversation. Responses with the active topic are given precedence over other responses.") %>">
									</td>
								</tr>
								
								<tr id="dial-topic-check-tr">
									<td></td>
									<td></td>
									<td nowrap>
										<div id="dial-require-topic-tr">
											<span class="dropt-banner">
												<img id="help-mini" src="images/help.svg"/>
												<div>
													<%= loginBean.translate("If the topic is required, the response will only be used if that topic is active in the current conversation.") %>
												</div>
											</span>
											<input id="dial-add-require-topic" type="checkbox" title="<%= loginBean.translate("If the topic is required, the response will only be used if that topic is active in the current conversation.") %>">
											<span class="chat"><%= loginBean.translate(loginBean.isMobile() ? "Required" : "Require Topic") %></span>
										</div>

										<div id="dial-exclusive-topic-tr">
											<span class="dropt-banner">
												<img id="help-mini" src="images/help.svg"/>
												<div>
													<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>
												</div>
											</span>
											<input id="dial-add-exclusive-topic" type="checkbox" title="<%= loginBean.translate("When an exclusive topic is set on a conversation only responses that have that topic will be used.") %>">
											<span class="chat"><%= loginBean.translate(loginBean.isMobile() ? "Exclusive" : "Exclusive Topic") %></span>
										</div>
									</td>
								</tr>
										
								<tr id="dial-intent-label-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give an intent or label to reuse the response as.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Intent Label") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input" id="dial-add-intent-label" type="text" title="<%= loginBean.translate("Optionally you can give an intent or label to reuse the response as.") %>">
									</td>
								</tr>
										
								<tr id="dial-keywords-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give keywords from the question. Keywords take priority when matching responses, the response may be used for any question that has any of the keywords. Never make common words like 'the', 'is' keywords.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Keywords") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input dial-autocomplete" id="dial-add-keywords" type="text" title="<%= loginBean.translate("Optionally you can give keywords from the question. Keywords take priority when matching responses, the response may be used for any question that has any of the keywords. Never make common words like 'the', 'is' keywords.") %>">
									</td>
								</tr>
								
								<tr id="dial-required-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give required words from the question that will be required for a response match.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Required") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input dial-autocomplete" id="dial-add-required" type="text" title="<%= loginBean.translate("Optionally you can give required words from the question that will be required for a response match.") %>">
									</td>
								</tr>
								
								<tr id="dial-emotions-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("You can associate emotions with the response. Emotions can be displayed by the bot's avatar.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Emotions") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input dial-autocomplete" id="dial-add-emotions" type="text" title="<%= loginBean.translate("You can associate emotions with the response. Emotions can be displayed by the bot's avatar.") %>">
										<script>
											$(function() {
												var emotes = [<%= bean.getAllEmotionString() %>];
												multiDropDown("#<%= "dial-add-emotions" %>", emotes, false);
											});
										</script>
									</td>
								</tr>
								
								<tr id="dial-actions-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("You can associate an action with the response. Actions can be displayed by the bot's avatar.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Actions") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input dial-autocomplete" id="dial-add-actions" type="text" title="<%= loginBean.translate("You can associate an action with the response. Actions can be displayed by the bot's avatar.") %>">
										<script>
											$(function() {
												var actions = [<%= bean.getAllActionsString() %>];
												multiDropDown("#<%= "dial-add-actions"%>", actions, false);
											});
										</script>
									</td>
								</tr>		
								
								<tr id="dial-poses-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("You can associate a pose with the response. Poses can be displayed by the bot's avatar.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Poses") %>:</span>
									</td>
									<td style="width:100%">
										<input class="dial-input dial-autocomplete" id="dial-add-poses" type="text" title="<%= loginBean.translate("You can associate a pose with the response. Poses can be displayed by the bot's avatar.") %>">
										<script>
											$(function() {
												var poses = [<%= bean.getAllPosesString() %>];
												multiDropDown("#<%= "dial-add-poses" %>", poses, false);
											});
										</script>
									</td>
								</tr>
								
								<tr id="dial-on-repeat-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give a different response to used if the current response has already been used in this conversation.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("On Repeat") %>:</span>
									</td>
									<td style="width:100%"><input class="dial-input" id="dial-add-on-repeat" type="text" value="" title="<%= loginBean.translate("Optionally you can give a different response to used if the current response has already been used in this conversation.") %>"></td>
								</tr>
								
								<tr id="dial-no-repeat-tr">
									<td></td>
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Require that the response or phrase only be used once.") %>
											</div>
										</span>
										<input id="dial-add-no-repeat" type="checkbox" title="<%= loginBean.translate("Require that the response or phrase only be used once") %>">
										<span class="chat"><%= loginBean.translate("No Repeat") %></span>
									</td>
								</tr>
								
								<tr id="dial-previous-tr">
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Optionally you can give a previous response to give the response a context. The previous response is the bot's previous response before the user's question.") %>
											</div>
										</span>
										<span class="chat"><%= loginBean.translate("Previous") %>:</span>
									</td>
									<td style="width:100%"><input class="dial-input" id="dial-add-previous" type="text" value="" title="<%= loginBean.translate("Optionally you can give a previous response to give the response a context. The previous response is the bot's previous response before the user's question.") %>"></td>
								</tr>
								
								<tr id="dial-require-previous-tr">
									<td></td>
									<td></td>
									<td nowrap>
										<span class="dropt-banner">
											<img id="help-mini" src="images/help.svg"/>
											<div>
												<%= loginBean.translate("Require that the response only be used if the previous response matches one of the previous responses.") %>
											</div>
										</span>
										<input id="dial-add-require-previous" type="checkbox" title="<%= loginBean.translate("Require that the response only be used if the previous response matches one of the previous responses.") %>">
										<span class="chat"><%= loginBean.translate("Require Previous") %></span>
									</td>
								</tr>
							</table>
						</div>
					</div>
					
					
					
<div id="dialog-import-info" title="<%= loginBean.translate("Import Formats") %>" class="dialog">
	<div style="max-height:600px;max-width:800px">
		<p>
		Four import formats are supported:
		</p>
		<ul>
		<li> Response List
		<li> CSV List
		<li> Chat Log
		<li> AIML
		</ul>
		<p>
		The Training & Chat Logs page also supports exporting conversations, or responses to any of these formats.  The easiest way to learn a format is to export a conversation log or responses in the format to use as an example.
		</p>
		
		<h3> Response List </h3>
		<p>
		A response list is a list of question/response pairs.  Each phrase is separated by a new line, and each question/response list is separated by an empty line.  You can also tag responses with meta data such as keywords to influence when the response is used.
		Response lists are the recommended way to train a bot. The bot will automatically find the best matching response for any question, the questions do not need to be exact matches, only sufficiently similar, or include a keyword.
		</p>
		<p>
		Response lists support the following tags:
		</p>
		<ul>
		<li>keywords: a set of space separated keywords, any question with any of the keywords may trigger the response to be used
		<li>required: a set of space separated words that are all required to be in the question to use the response
		<li>topic: the topic to categories the response, this both sets the conversation topic, and is more likely to be used if the topic is active
		<li>label: a name to label this response so it can be reused for other questions
		<li>previous: the response will more likely be used if the bot's previous response matches (can use patterns, can have multiple previous tags)
		<li>require previous: the response will only be used if the bot's previous response matches (can use patterns, can have multiple require previous tags)
		<li>no repeat: the bot will not use this response twice in the same conversation
		<li>on repeat: the bot will use this phrase the second time this response is triggered in the same conversation (can have multiple on repeat tags)
		<li>condition: a conditional statement of Self code, this response will only be used if the condition evaluates to true
		<li>think: a block of Self code to be executed when the response is used
		<li>command: a JSON command to be sent with the response to the chat client (such as a mobile command to open an app)
		<li>emotions: a set of space separated emotions this response will trigger
		<li>actions: a set of space separated actions this response will trigger
		<li>poses: a set of space separated poses this response will trigger
		</ul>
		<p>
		You can also define "default:" responses, or "greeting:" phrases, or execute Self scripts through "script:".
		</p>
		Example Response List:
		<pre>
greeting: Hello, how can I help?

default: Sorry, I did not understand that, perhaps rephrase your question, or email support@company.com

Hello
Hello there

Hi
Hi, who are you?

How are you?
Fine.
Good.
Swell.
I am sad today.

How old are you?
I am only 1 year old.
keywords: old
required: old
topic: age
		</pre>
		
		<h3> CSV List </h3>
		<p>
		A Comma Separated Values (CSV) list is a list of question/response pairs separated by a comma.  Each question/response is separated by a new line.  CSV is a convenient format to export from Excel or other spreadsheets.  Each response can have multiple questions separated by a "?", and a third, fourth, and fifth column can be used for keywords, required words, and the topic.
		</p>
		For example:
		<pre>
"Hello","Hello there"
"Hi","Hi, how are you? How are you?"
"How are you?","Fine."
"How old are you?","I am only 1 year old.","old","old","age"
		</pre>
		
		<h3> Chat Log </h3>
		<p>
		The chat logs format is a log of a conversation between two or more people.  Each phrase starts with the name of the speaker followed by a ':' (i.e. Jim:).  Each phrase is separated by a new line.  If the log contains multiple conversations, then each conversation must be separated by a empty new line.
		</p>
		<p>
		Chat logs are an easy way to train a bot from exiting conversation logs that you might have from live chat, chat rooms, or find online. However, chat logs are not always the best way to train a bot as it will learn all of the responses. You have more control over how the bot will use a response using a response list file, or AIML.
		</p>
		Example Chat Log:
		<pre>
Jim: Hello
Cindy: Hello Jim
Jim: How are you Cindy?
Cindy: I am fine thanks for asking.

Jim: Hi
Cindy: Hey
Jim: What is your name?
Cindy: My name is Cindy.
		</pre>
		<p>
		The name is important, as the bot can learn to replace the name in a phrase with the speaker or targets of the phrase.  For example "Hello Jim" can automatically become the template response Template("Hello {:target}"). Templates require that comprehension is enabled in the bot.
		</p>
		<p>
		It is important to separate new conversations by an empty line.  The bot will learn from the conversation in context.  Starting a new conversation starts a new context, so the bot will not get confused with the phrases in the previous conversation.  i.e. if there was no new line above, the bot with think that "Hi" was a response to "I am fine thanks for asking." in the context of "How are you Cindy?".
		</p>
		<p>
		The chat log format also supports some advanced syntax.  You can add a default response to your bot using the "default:" prefix.  A default response is what the bot will say when it does not know a good response.
		</p>
		<p>
		You can associate a phrase with an emotional state using "<happy>", any of the following emotions can be used love, like, dislike, hate, rage, anger, calm, serene, ecstatic, happy, sad, crying, panic, afraid, confident, courageous, surprise, bored, laughter, serious.
		</p>
		<p>
		You can execute a Self script from a chat log.  This allows you to load knowledge into your bot.  Scripts are executed using the "script:" prefix.
		</p>
		For example:
		<pre>
default: I do not understand.
default: Sorry, please rephrase your question.

anonymous:<laughter> lol

anonymous:<love> love

anonymous:<anger> Stop that!

script: #self.name = "Cindy";
		</pre>
		
		<h3> AIML </h3>
		<p>
		The Artificial Intelligence Markup Language is a standard XML format for chat bot responses.  It includes XML elements for patterns (questions) and templates (responses).
		</p>
		<p>
		The are many free AIML files available on the web in many different languages.<br/>
		See <a href="https://www.botlibre.com/forum-post?id=28654">Scripting your bot with AIML</a> for more information.
		</p>
	</div>
</div>
				<% } %>
			<% } catch (Exception error) { botBean.error(error); }%>
			</div>
		</div>
	</div>
	<jsp:include page="chatlogs-edit.jsp"/>
	<jsp:include page="footer.jsp"/>
</body>
</html>
