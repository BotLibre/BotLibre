<%@page import="java.util.Collection"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.bean.ChatLogBean"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.knowledge.Primitive"%>
<%@page import = "org.botlibre.web.bean.ChatBean" %>
<%@page import="org.botlibre.api.knowledge.Vertex" %>
<%@page import="org.botlibre.api.knowledge.Relationship"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.util.Utils"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% ChatLogBean bean = loginBean.getBean(ChatLogBean.class); %>

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
		return;
	}
	
	var editType = document.getElementById("search-select");
	
	var isConversation = false;
	var isResponses = false;
	var isDefault = false;
	var isGreeting = false;
	var isPhrase = false;
	var isPhraseSelected = false;
	var isWord = false;
	var isFlagged = false;
	
	var question = document.getElementById("dial-add-question");
	var response = document.getElementById("dial-add-response");
	var sentiment = document.getElementById("dial-add-sentiment");
	var condition = document.getElementById("dial-add-condition");
	var think = document.getElementById("dial-add-think");
	var command = document.getElementById("dial-add-command");
	var topic = document.getElementById("dial-add-topic");
	var label = document.getElementById("dial-add-intent-label"); 
	var keywords = document.getElementById("dial-add-keywords");
	var required = document.getElementById("dial-add-required"); 
	var emotions = document.getElementById("dial-add-emotions");
	var actions = document.getElementById("dial-add-actions"); 
	var poses = document.getElementById("dial-add-poses");
	var onRepeat = document.getElementById("dial-add-on-repeat"); 
	var previous = document.getElementById("dial-add-previous");

	var requireTopic = document.getElementById("dial-add-require-topic");
	var exclusiveTopic = document.getElementById("dial-add-exclusive-topic"); 
	var noRepeat = document.getElementById("dial-add-no-repeat");
	var requirePrevious = document.getElementById("dial-add-require-previous");
	var autoReduce = document.getElementById("dial-add-auto-reduce");
	
	var questionIdDial = document.getElementById("dial-add-question-id");
	var responseIdDial = document.getElementById("dial-add-response-id");
	var questionParentIdDial = document.getElementById("dial-add-question-parent-id");
	var responseParentIdDial = document.getElementById("dial-add-response-parent-id");
	var metaIdDial = document.getElementById("dial-add-meta-id");
	var metaParentIdDial = document.getElementById("dial-add-meta-parent-id");
	
	var dialogAddResponse = function() {
		isConversation = editType.value === "conversations";
		isResponses = editType.value === "responses";
		isDefault = editType.value === "default";
		isGreeting = editType.value === "greetings";
		isPhrase = editType.value === "phrases";
		isPhraseSelected = editType.value === "phrases";
		isWord = editType.value === "words";
		isFlagged = editType.value === "flagged";
		
		if(!isResponses && !isDefault && !isGreeting && !isWord) {
			isResponses = true;
		}
		if((isDefault || isGreeting) && questionParentIdDial.value != "") {
			isResponses = true;
		}

		var editorType = $('#edit-editor-dial').val();
		if (editorType === "wysiwyg") {
			var editorContent = tinyMCE.activeEditor.getContent();
			if (editorContent.startsWith("<p>")) {
				editorContent = editorContent.substring(3, editorContent.length);
			}
			if (editorContent.endsWith("</p>")) {
				editorContent = editorContent.substring(0, editorContent.length - 4);
			}
			$("answer-dial").val(editorContent);
			response = editorContent;
		} else {
			response = document.getElementById("answer-dial").value;
		}

		if (isResponses && (question.value == "" || response == "")) {
			SDK.showError("<%= loginBean.translate("Please enter the question and response") %>", "<%= loginBean.translate("Error") %>");
			return false;
		} else if(isDefault && response == "") {
			SDK.showError("<%= loginBean.translate("Please enter the default response") %>", "<%= loginBean.translate("Error") %>");
			return false;
		} else if(isGreeting && response == "") {
			SDK.showError("<%= loginBean.translate("Please enter the greeting") %>", "<%= loginBean.translate("Error") %>");
			return false;
		} else if(isWord && response == "") {
			SDK.showError("<%= loginBean.translate("Please enter the word") %>", "<%= loginBean.translate("Error") %>");
			return false;
		}
		
		var config = new ResponseConfig();
			
		config.questionId = questionIdDial.value;
		config.responseId = responseIdDial.value;
		config.metaId = metaIdDial.value;
		
		config.parentQuestionId = questionParentIdDial.value;
		config.parentResponseId = responseParentIdDial.value;
		
		if(metaParentIdDial.value != null && metaParentIdDial.value != "") {
			config.metaId = metaParentIdDial.value;
		}
		
		config.instance = "<%= botBean.getInstanceId() %>";
		config.question = question.value;
		config.response = response;
		config.sentiment = sentiment.value;
		config.condition = condition.value;
		config.think = think.value;
		config.command = command.value;
		config.topic = topic.value;
		config.label = label.value;
		config.keywords = keywords.value;
		config.required = required.value;
		config.emotions = emotions.value;
		config.actions = actions.value;
		config.poses = poses.value;
		config.onRepeat = onRepeat.value;
		config.previous = previous.value;
		
		config.noRepeat = noRepeat.checked;
		config.requirePrevious = requirePrevious.checked;
		config.requireTopic = requireTopic.checked;
		config.exclusiveTopic = exclusiveTopic.checked;
		config.autoReduce = autoReduce.checked;
		
		if(isResponses) {
			config.type = "response";
		} else if(isDefault) {
			config.type = "default";
		} else if(isGreeting) {
			config.type = "greeting";
		} else if(isWord) {
			config.type = "word";
		}
		
		sdkConnection.addQuestionResponse(config, function(config) {
			
			var responseId = config.questionId + "-" + config.responseId;
			var hr = document.getElementById("quick-add-hr");
			var parentMetaId = "";
			if(metaIdDial.value != null && metaIdDial.value != "") {
				parentMetaId = metaIdDial.value + "-";
			}
			
			if(hr == null) {
				location.reload(true);
			} 
			
			var newRow = config.displayHTML;
			var newScript = "";
			var insertPosition = "afterend";
			var toggleMetaTable = false;
			
			if(responseIdDial.value === "") { //New response		
				//Root Level Response
				if(config.parentQuestionId == null || config.parentQuestionId == "") {	
					var existingQuestionRow = document.getElementById("question-row-" + config.questionId);
					if(existingQuestionRow != null) {
						hr = existingQuestionRow.previousElementSibling;
						existingQuestionRow.remove();
					} else {
						newRow += "<tr id='row-line-" + config.questionId + "'><td colspan='1'><hr/></td></tr>";
					}
				} else { //Nested Response
					//Check for existing meta table
					var existingMetaTable = document.getElementById("table-meta-" + config.metaId);
					if(existingMetaTable != null) {
						hr = existingMetaTable.previousElementSibling;
						existingMetaTable.remove();
						toggleMetaTable = true;
					} else {
						hr = document.getElementById("response-td-id-" + parentMetaId + config.parentQuestionId + "-" + config.parentResponseId).parentNode;
						insertPosition = "afterend";

						newRow = "<tr><td></td><td></td><td></td>"; 
						newRow += "<td id='td-toggle-parent-" + parentMetaId + config.parentQuestionId + "-" + config.parentResponseId + "'>";
						newRow += "<span><img src='images/circle-minus.png' class='menu-small' id='expand-table-button-" + config.metaId + "'></span>";
						
						newScript = "<script>\n";
						newScript += "$(function() {\n";
						newScript += "$('#expand-table-button-" + config.metaId + "').on('click', function() {\n";
						newScript += "$('#table-meta-" + config.metaId + "').toggle();\n";
						newScript += "var src = ($(this).attr('src') === 'images/circle-plus.png') ? 'images/circle-minus.png' : 'images/circle-plus.png';\n";
						newScript += "$('#expand-table-button-" + config.metaId + "').attr('src', src);\n";
						newScript += "});\n});\n<\/script>\n";
						
						newRow += config.displayHTML;
						
						toggleMetaTable = true;
					}
				}
				
				if(hr != null) {
					hr.insertAdjacentHTML(insertPosition, newRow);
				}

				newScript += getScript(config.displayHTML);
				
				if(newScript != "") {
					$('head').append(newScript);
				}
				
				if(toggleMetaTable) {
					$("#table-meta-" + config.metaId).toggle();
				}
				
			} else { //Edit response
				
				let oldResponseId = questionIdDial.value + '-' + responseIdDial.value;
				let oldResponseIdMeta = "";
				let responseIdMeta = "";
				if(config.parentQuestionId == null || config.parentQuestionId == "") {
					oldResponseIdMeta = questionIdDial.value + '-' + responseIdDial.value;
					responseIdMeta = config.questionId + "-" + config.responseId;
				}
				else {
					oldResponseIdMeta = metaIdDial.value + '-' + questionIdDial.value + '-' + responseIdDial.value;
					responseIdMeta = config.metaId + "-" + config.questionId + "-" + config.responseId;
				}
				
				//Root Level Response
				if(config.parentQuestionId == null || config.parentQuestionId == "") {	
					var existingQuestionRow = document.getElementById("question-row-" + questionIdDial.value);
					if(existingQuestionRow != null) {
						hr = existingQuestionRow.previousElementSibling;
						existingQuestionRow.remove();
					}
				} else { //Nested Response
					//Check for existing meta table
					var existingMetaTable = document.getElementById("table-meta-" + metaIdDial.value);
					if(existingMetaTable != null) {
						hr = existingMetaTable.previousElementSibling;
						existingMetaTable.remove();
						toggleMetaTable = true;
					} else {
						hr = document.getElementById("response-td-id-" + parentMetaId + config.parentQuestionId + "-" + config.parentResponseId).parentNode;
						insertPosition = "afterend";

						newRow = "<tr><td></td><td></td><td></td>"; 
						newRow += "<td id='td-toggle-parent-" + parentMetaId + config.parentQuestionId + "-" + config.parentResponseId + "'>";
						newRow += "<span><img src='images/circle-minus.png' class='menu-small' id='expand-table-button-" + config.metaId + "'></span>";
						
						newScript = "<script>\n";
						newScript += "$(function() {\n";
						newScript += "$('#expand-table-button-" + config.metaId + "').on('click', function() {\n";
						newScript += "$('#table-meta-" + config.metaId + "').toggle();\n";
						newScript += "var src = ($(this).attr('src') === 'images/circle-plus.png') ? 'images/circle-minus.png' : 'images/circle-plus.png';\n";
						newScript += "$('#expand-table-button-" + config.metaId + "').attr('src', src);\n";
						newScript += "});\n});\n<\/script>\n";
						
						newRow += config.displayHTML;
						toggleMetaTable = true;
					}
				}
				
				if(hr != null) {
					hr.insertAdjacentHTML(insertPosition, newRow);
				}
				
				newScript += getScript(config.displayHTML);
				
				if(newScript != "") {
					$('head').append(newScript);
				}
				
				if(toggleMetaTable) {
					$("#table-meta-" + metaIdDial.value).toggle();
				}
			}
			
			clearEditResponseDialog();
			
			question.focus();
			
			refreshTags();
			$('#dialog-add-response').dialog('close');
		});
	};
	
	var editQuestionResponse = function(button) {
		var buttonId = button.id;
		var ids = parseElementId(buttonId);
		var index = 0;
		var meta_id = null;
		if (ids.length == 3) {
			meta_id = ids[0];
			index = 1;
		}
		var question_id = ids[index];
		var response_id = ids[index + 1];
		
		var config = new ResponseConfig();
		config.instance = "<%= botBean.getInstanceId() %>";
		config.questionId = question_id;
		config.responseId = response_id;
		config.metaId = meta_id;
		config.type = editType.value;
		
		sdkConnection.getQuestionResponse(config, function(config) {

			question.value = config.question;
			// Switch to markup before setting response value.
			if(config.response.startsWith("Template(\"")) {
				$('#edit-editor-dial').val("markup");
				$('#edit-editor-dial').change();
			}
			var response = config.response;
			response = response.replace(/&lt;/g, "<");
			response = response.replace(/&gt;/g, ">");
			response = response.replace(/&amp;/g, "&");
			document.getElementById("answer-dial").value = response;
			tinyMCE.activeEditor.setContent($("#answer-dial").val());
			
			sentiment.value = config.sentiment;
			if(config.condition != null) {
				condition.value = SDK.unescapeHTML(config.condition);
			}
			if(config.think != null) {
				think.value = SDK.unescapeHTML(config.think);
			}
			if(config.command != null) {
				command.value = SDK.unescapeHTML(config.command);
			}
			if(config.topic != null) {
				topic.value = SDK.unescapeHTML(config.topic);
			}
			label.value = config.label;
			if(config.keywords != null) {
				keywords.value = SDK.unescapeHTML(config.keywords);
			}
			if(config.required != null) {
				required.value = SDK.unescapeHTML(config.required);
			}
			emotions.value = config.emotions;
			if(config.actions != null) {
				actions.value = SDK.unescapeHTML(config.actions);
			}
			if(config.poses != null) {
				poses.value = SDK.unescapeHTML(config.poses);
			}
			onRepeat.value = config.onRepeat;
			previous.value = config.previous;

			//Checkboxes
			requireTopic.checked = config.requireTopic;
			exclusiveTopic.checked = config.exclusiveTopic;
			noRepeat.checked = config.noRepeat;
			requirePrevious.checked = config.requirePrevious;
			autoReduce.checked = true;
						
			var parentRow = document.getElementById("table-meta-" + config.metaId);
			var parentNode;
			if(parentRow != 'undefined' && parentRow != null) {
				parentNode = parentRow.parentNode;
				var _ids = parseElementId(parentNode.id);
				var _index = 0;
				var _metaId = null;
				if (_ids.length == 3) {
					_metaId = _ids[0];
					_index = 1;
				}
				var _questionId = _ids[_index];
				var _responseId = _ids[_index + 1];
				questionParentIdDial.value = _questionId;
				responseParentIdDial.value = _responseId;
				if(_metaId != null) {
					metaParentIdDial.value = _metaId;
				}
			}
			
			questionIdDial.value = config.questionId;
			responseIdDial.value = config.responseId;
			
			metaIdDial.value = config.metaId;
			
			setupEditDialog();
			

			$( "#dialog-add-response" ).dialog( "open" );
			
		});
	}
	
	function getRowId(event) {
		let row = $(event.target).closest('tr');
		let rowId = row[0].id;
		let arr = rowId.split("-");
		let id;
		if(arr.length > 1) {
			id = arr[arr.length - 1];
			id = parseInt(id);
			return id;
		}
	}

	var setupEditDialog = function() {	
		
		isConversation = editType.value === "conversations";
		isResponses = editType.value === "responses";
		isDefault = editType.value === "default";
		isGreeting = editType.value === "greetings";
		isPhrase = editType.value === "phrases";
		isPhraseSelected = editType.value === "phrases";
		isWord = editType.value === "words";
		isFlagged = editType.value === "flagged";
		
		var isEdit = (responseIdDial.value != "");
		
		if(!isResponses && !isDefault && !isGreeting && !isWord) {
			isResponses = true;
		}
		
		if((isDefault || isGreeting) && questionParentIdDial.value != "") {
			isResponses = true;
		}
		
		if(isResponses || isWord) {
			$("#dial-sentiment-tr").toggle( $("#dial-check-sentiment").prop("checked") );
		} else {
			$("#dial-sentiment-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-condition-tr").toggle( $("#dial-check-condition").prop("checked") );
		} else {
			$("#dial-condition-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-think-tr").toggle( $("#dial-check-think").prop("checked") );	
		} else {
			$("#dial-think-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-command-tr").toggle( $("#dial-check-command").prop("checked") );
		} else {
			$("#dial-command-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-topic-tr").toggle( $("#dial-check-topic").prop("checked") );
		} else {
			$("#dial-topic-tr").hide();
		}
		
		if(isResponses || isDefault) {
			$("#dial-topic-check-tr").toggle( $("#dial-check-topic").prop("checked") );
			$("#dial-require-topic-tr").show();
		} else {
			$("#dial-require-topic-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault || isWord) {
			$("#dial-topic-check-tr").toggle( $("#dial-check-topic").prop("checked") );
			$("#dial-exclusive-topic-tr").show();
		} else {
			$("#dial-exclusive-topic-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-intent-label-tr").toggle( $("#dial-check-label").prop("checked") );
		} else {
			$("#dial-intent-label-tr").hide();
		}
		
		if(isResponses) {
			$("#dial-keywords-tr").toggle( $("#dial-check-keywords").prop("checked") );
		} else {
			$("#dial-keywords-tr").hide();
		}
		
		if(isResponses) {
			$("#dial-required-tr").toggle( $("#dial-check-required").prop("checked") );
		} else {
			$("#dial-required-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault || isWord) {
			$("#dial-emotions-tr").toggle( $("#dial-check-emotes").prop("checked") );
		} else {
			$("#dial-emotions-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-actions-tr").toggle( $("#dial-check-actions").prop("checked") );
		} else {
			$("#dial-actions-tr").hide();
		}
		
		if(isResponses || isGreeting || isDefault) {
			$("#dial-poses-tr").toggle( $("#dial-check-poses").prop("checked") );
		} else {
			$("#dial-poses-tr").hide();
		}
		
		if(isResponses || isDefault) {
			$("#dial-on-repeat-tr").toggle( $("#dial-check-repeat").prop("checked") );
		} else {
			$("#dial-on-repeat-tr").hide();
		}
		
		if(isResponses || isDefault) {
			$("#dial-no-repeat-tr").toggle( $("#dial-check-repeat").prop("checked") );
		} else {
			$("#dial-no-repeat-tr").hide();
		}
		
		if(isResponses || isDefault) {
			$("#dial-previous-tr").toggle( $("#dial-check-previous").prop("checked") );
		} else {
			$("#dial-previous-tr").hide();
		}
		
		if(isResponses || isDefault) {
			$("#dial-require-previous-tr").toggle( $("#dial-check-previous").prop("checked") );
		} else {
			$("#dial-require-previous-tr").hide();
		}
		
		if (isResponses) {
			$("#dial-question-tr").show();
			$("#dial-auto-reduce-tr").show();
			$('#dial-response-help-div').text("<%= loginBean.translate("Enter the response. Responses support most HTML tags including a (links), b (bold), ol/li (lists), button, img, and video. You can also define a template with Self code inside {} or by using 'Template()'.") %>");
			$('#dial-response-span').text("Response: ");
			if(isEdit) {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Edit Response") %>" );
			} else {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Add Response") %>" );
			}
		}
		else if (isGreeting) {
			$("#dial-question-tr").hide();
			$("#dial-auto-reduce-tr").hide();
			$('#dial-response-help-div').text("<%= loginBean.translate("Enter the greeting.") %>");
			$('#dial-response-span').text("Greeting: ");
			if(isEdit) {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Edit Greeting") %>" );
			} else {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Add Greeting") %>" );
			}
		}
		else if (isDefault) {
			$("#dial-question-tr").hide();
			$("#dial-auto-reduce-tr").hide();
			$('#dial-response-help-div').text("<%= loginBean.translate("Enter the default response.") %>");
			$('#dial-response-span').text("Default Response: ");
			if(isEdit) {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Edit Default Response") %>" );
			} else {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Add Default Response") %>" );
			}
		}
		else if (isWord) {
			$("#dial-question-tr").hide();
			$("#dial-auto-reduce-tr").hide();
			$('#dial-response-help-div').text("<%= loginBean.translate("Enter the word.") %>");
			$('#dial-response-span').text("Word: ");
			if(isEdit) {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Edit Word") %>" );
			} else {
				$( "#dialog-add-response" ).dialog( "option", "title", "<%= loginBean.translate("Add Word") %>" );
			}
		}			
	}
	
	// Delete the response and row when response delete hover button is clicked.
	function deleteQuestionResponse(button) {
		var buttonId = button.id;
		var ids = parseElementId(buttonId);
		var index = 0;
		var metaId = null;
		if (ids.length == 3) {
			metaId = ids[0];
			index = 1;
		}
		var questionId = ids[index];
		var responseId = ids[index + 1];

		isDefault = editType.value === "default";
		isGreeting = editType.value === "greetings";
		
		if((isDefault || isGreeting) && metaId != null) {
			isResponses = true;
		}
		
		var config = new ResponseConfig();
		config.instance = "<%= botBean.getInstanceId() %>";
		config.questionId = questionId;
		config.responseId = responseId;
		config.metaId = metaId;
		
		if(isResponses) {
			config.type = "response";
		}else if(isDefault) {
			config.type = "default";
		} else if(isGreeting) {
			config.type = "greeting";
		}	
		
		sdkConnection.deleteQuestionResponse(config, function() {
			deleteQuestionResponseTable(questionId, responseId, metaId);
		});
	}
	
	function deleteQuestionResponseTable(questionId, responseId, metaId) {
		var responseRows = 0;
		var questionTable;
		if(metaId != null && metaId != "") {
			questionTable = $('#question-row-' + metaId + '-' + questionId).find('table').first();
		} else {
			questionTable = $('#question-row-' + questionId).find('table').first();
		}
		for(var i = 0; i < questionTable[0].rows.length; i++) {
			if(questionTable[0].rows[i].id.startsWith("question-response-row-")) {
				responseRows++;
			}
		}
		
		if(responseRows <= 1) {
			if(metaId != null && metaId != "") {
				$('#question-row-' + metaId + '-' + questionId).remove();
			} else {
				$('#question-row-' + questionId).remove();
			}	
			$('#row-line-' + questionId).remove();
		}
		else {
			if(metaId == null) {
				responseId = questionId + "-" + responseId;
			} else {
				responseId = metaId + "-" + questionId + "-" + responseId;
			}
			$('#question-response-row-' + responseId).remove();
			$('#td-toggle-parent-' + responseId).remove();
		}
		
		if(metaId != null) {
			var metaTable = $('#table-meta-' + metaId);
			if(metaTable[0].rows.length == 0) {
				$('#table-meta-' + metaId).remove();
				
				var expandButton = document.getElementById('expand-table-button-' + metaId);
				expandButton.parentNode.remove();
			}
		}
	}
	
	// Parse numbers from element id and return as array of values.
	function parseElementId(elementId) {
		var tokens = elementId.split("-");
		var ids = [];
		for (var index = 0; index < tokens.length; index++) {
			var id = parseInt(tokens[index]);
			if (!isNaN(id)) {
				ids.push(id);
			}
		}
		return ids;
	}
	
	var clearEditResponseDialog = function() {
		
		question.value = "";
		tinyMCE.activeEditor.setContent("");
		document.getElementById("answer-dial").value = "";
		
		condition.value = "";
		think.value = "";
		command.value = "";
		think.value = "";
		label.value = "";
		keywords.value = "";
		required.value = "";
		emotions.value = "";
		actions.value = "";
		poses.value = "";
		onRepeat.value = "";
		previous.value = "";
		sentiment.value = "";
		topic.value = "";
		
		requireTopic.checked = false;
		exclusiveTopic.checked = false;
		noRepeat.checked = false;
		requirePrevious.checked = false;
		autoReduce.checked = true;
		
		questionIdDial.value = "";
		responseIdDial.value = "";
		questionParentIdDial.value = "";
		responseParentIdDial.value = "";
		metaIdDial.value = "";
		metaParentIdDial.value = "";
	}
	
	// Show the quick add follow-up question for a response.
	function showQuickAddNextResponse(button) {
		
		var buttonId = button.id;
		var ids = parseElementId(buttonId);
		var index = 0;
		var metaId = null;
		if (ids.length == 3) {
			metaId = ids[0];
			index = 1;
		}
		var questionId = ids[index];
		var responseId = ids[index + 1];
		var parentId = questionId + "-" + responseId;
		if (metaId != null) {
			parentId = metaId + "-" + parentId;
		}
		
		questionParentIdDial.value = questionId;
		responseParentIdDial.value = responseId;
		metaIdDial.value = metaId;
		
		setupEditDialog();
		
		$( "#dialog-add-response" ).dialog( "open" );
	}
	
	// Grabs and returns the <script> tags and the contents within from a string
	function getScript(html) {
		if(html == null) {
			return "";
		}
		return html.match(/<script[\s\S]*?>[\s\S]*?<\/script>/gi);
	}
	
	function addExpandButton(parentQuestionId, parentResponseId) {
		//TODO:
	}
	
	function addExpandButtonScript(metaId) {
		//TODO:
	}
	
	function refreshTags() {
		$(".chat-topic").toggle($("#check-topic").prop('checked'));
		//$(".chat-label").toggle($("#check-label").prop('checked')); - use for response, not meta
		$(".chat-keyword").toggle($("#check-keywords").prop('checked'));
		$(".chat-required").toggle($("#check-required").prop('checked'));
		$(".chat-emote").toggle($("#check-emotes").prop('checked'));
		$(".chat-sentiment").toggle($("#check-sentiment").prop('checked'));
		$(".chat-confidence").toggle($("#check-confidence").prop('checked'));
		$(".chat-action").toggle($("#check-actions").prop('checked'));
		$(".chat-pose").toggle($("#check-poses").prop('checked'));
		$(".chat-next").toggle($("#check-next").prop('checked'));
		$(".chat-previous").toggle($("#check-previous").prop('checked'));
		$(".chat-require-previous").toggle($("#check-previous").prop('checked'));
		$(".chat-condition").toggle($("#check-condition").prop('checked'));
		$(".chat-think").toggle($("#check-think").prop('checked'));
		$(".chat-command").toggle($("#check-command").prop('checked'));
		$(".chat-synonym").toggle($("#check-synonyms").prop('checked'));
		
		$(".edit-sentiment-tr").toggle($("#check-sentiment").prop('checked'));
		$(".edit-emotes-tr").toggle($("#check-emotes").prop('checked'));
		$(".edit-topic-tr").toggle($("#check-topic").prop('checked'));
		$(".edit-keywords-tr").toggle($("#check-keywords").prop('checked'));
		$(".edit-synonyms-tr").toggle($("#check-synonyms").prop('checked'));
		$(".edit-actions-tr").toggle($("#check-actions").prop('checked'));
		$(".edit-poses-tr").toggle($("#check-poses").prop('checked'));
		$(".edit-condition-tr").toggle($("#check-condition").prop('checked'));
		$(".edit-confidence-tr").toggle($("#check-confidence").prop('checked'));
		$(".edit-think-tr").toggle($("#check-think").prop('checked'));
		$(".edit-command-tr").toggle($("#check-command").prop('checked'));
		$(".edit-label-tr").toggle($("#check-label").prop('checked'));
		$(".edit-required-tr").toggle($("#check-required").prop('checked'));
		$(".edit-repeat-tr").toggle($("#check-repeat").prop('checked'));
		$(".edit-previous-tr").toggle($("#check-previous").prop('checked'));
		$(".edit-next-tr").toggle($("#check-next").prop('checked'));
	}
	
	function checkAllCheckboxes(checked) {
		$("#check-all").prop('checked', checked);
		$("#check-topic").prop('checked', checked);
		$("#check-label").prop('checked', checked);
		$("#check-keywords").prop('checked', checked);
		$("#check-required").prop('checked', checked);
		$("#check-emotes").prop('checked', checked);
		$("#check-sentiment").prop('checked', checked);
		$("#check-confidence").prop('checked', checked);
		$("#check-actions").prop('checked', checked);
		$("#check-poses").prop('checked', checked);
		$("#check-next").prop('checked', checked);
		$("#check-previous").prop('checked', checked);
		$("#check-repeat").prop('checked', checked);
		$("#check-condition").prop('checked', checked);
		$("#check-think").prop('checked', checked);
		$("#check-command").prop('checked', checked);
		$("#check-synonyms").prop('checked', checked);
		
		$("#dial-check-all").prop('checked', checked);
		$("#dial-check-topic").prop('checked', checked);
		$("#dial-check-label").prop('checked', checked);
		$("#dial-check-keywords").prop('checked', checked);
		$("#dial-check-required").prop('checked', checked);
		$("#dial-check-emotes").prop('checked', checked);
		$("#dial-check-sentiment").prop('checked', checked);
		$("#dial-check-actions").prop('checked', checked);
		$("#dial-check-poses").prop('checked', checked);
		$("#dial-check-previous").prop('checked', checked);
		$("#dial-check-repeat").prop('checked', checked);
		$("#dial-check-condition").prop('checked', checked);
		$("#dial-check-think").prop('checked', checked);
		$("#dial-check-command").prop('checked', checked);
		$("#dial-check-synonyms").prop('checked', checked);
		
		refreshTags();
		setupEditDialog();
	}
	
	function updateKeywordAndRequiredInputs() {
		var wordlist = $("#dial-add-question").val().replace(/[.,\/#!$%\^&\*;:{}=\-_`~?/"()]/g,"");
		var words = wordlist.split(" ");
		multiDropDown("#dial-add-keywords", words, false);
		multiDropDown("#dial-add-required", words, false);
	}
	
</script>