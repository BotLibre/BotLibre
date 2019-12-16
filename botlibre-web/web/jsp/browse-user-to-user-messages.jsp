<%@page import="org.botlibre.web.bean.UserToUserMessageBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.bean.LoginBean"%>
<%@page import="org.botlibre.web.admin.UserMessage"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	UserToUserMessageBean bean = loginBean.getBean(UserToUserMessageBean.class);
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html style="height:100%">
<head>
	<jsp:include page="head.jsp"/>
	<title>User Messages <%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Check your direct messages and reply") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("messages, user, inbox") %>"/>
	<link rel='stylesheet' href='css/user_to_user_messages.css' type='text/css'>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body style="height:100%;overflow:hidden;">
	<jsp:include page="banner-messages.jsp"/>
	<jsp:include page="admin-user-banner.jsp"/>
	<jsp:include page="admin-message-banner.jsp"/>
	<div id="mainbody" style="height:100%;">
	<div id="contents" style="padding:unset;max-width:unset;">
	<% if (loginBean.isMobile()) { %>
		<div class="about" style="position:fixed;top:120px;bottom:50px;overflow:auto;padding-right:12px;padding-left:2px;width:100%;">
	<% } else { %>
		<div class="about" style="position:fixed;top:120px;bottom:50px;overflow:auto;padding-right:2px;padding-left:2px;width:100%;">
	<% } %>
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (!loginBean.isLoggedIn()) { %>
		<p style="color:#E00000;">
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your messages") %>.
		</p>
	<% } else { %>
		<div id="dialog-delete-user-to-user-messages" title="<%= loginBean.translate("Delete") %>" style="display:block;width:auto;min-height:84px;max-height:none;height:auto;">
			<form id="user-messages-delete-form" action="browse-user-to-user-messages" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<input name="targetUserId" type="hidden" value="">
				<input id="delete-user-messages-confirm" type="checkbox" name="delete-user-to-user-messages-confirm" title="<%= loginBean.translate("Caution, this will permently delete all messages with this user.") %>"><%= loginBean.translate("I'm sure") %><br>
			</form>
		</div>
		<%= bean.searchUserToUserMessagesHTML(proxy) %>
		<% if (loginBean.isMobile()) { %>
			<div id="messageDiv" style="width:100%;position:fixed;bottom:0px;padding-right:19px;">
		<% } else { %>
			<div id="messageDiv" style="width:100%;position:fixed;bottom:0px;padding-right:7px;padding-left:2px;">
		<% } %>
			<form id="sendUserMessageForm" action="login" method="post" class="message" style="display:flex;">
				<input name="token" type="hidden" value="<%= loginBean.getUser().getToken() %>"/>
				<input id="user-message" type="text" autofocus placeholder="<%= loginBean.translate("You say") %>" style="max-width:none;width:100%;margin-right:3px;">
				<input id="sendicon" class="sendicon" type="submit" name="submit" value="">
			</form>
		</div>
		<script>
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
				SDK.showError(error, "<%= loginBean.translate("Error") %>");
				return;
			}
		
			var firstMessageSent = false;
			var messagesPage = "<%= bean.getMessagePage() %>";
			var messagesPageSize = "<%= bean.getMessagePageSize() %>";
			var messageResultsSize = "<%= bean.getMessageResultsSize() %>";
			var mostRecentCreationDate = "<%= bean.getMostRecentDate() %>";
			var creationDate = "<%= bean.getMostRecentDate() %>";
			
			if (messagesPage == 0) {
				firstMessageSent = true;
			}
			if (messagesPage == 0 && messageResultsSize == messagesPageSize) {
				document.getElementById('user-messages-previous-page-up').style.display = "initial";
				document.getElementById('user-messages-next-page-down').style.display = "none";
			} else if (messagesPage > 0 && messageResultsSize == messagesPageSize) {
				document.getElementById('user-messages-previous-page-up').style.display = "initial";
				document.getElementById('user-messages-next-page-down').style.display = "initial";
			} else if (messagesPage > 0 && messageResultsSize < messagesPageSize) {
				document.getElementById('user-messages-previous-page-up').style.display = "none";
				document.getElementById('user-messages-next-page-down').style.display = "initial";
			}
			
			function scrollTable() {
				let tableWrapper = document.getElementsByClassName('about')[0];
				tableWrapper.scrollTop = tableWrapper.scrollHeight - tableWrapper.clientHeight;
			}
			scrollTable();
		
			var pollMessagesPostRequest = function() {
				let userMessageConfig = new UserMessageConfig();
				userMessageConfig.user = "<%= loginBean.getUserId() %>";
				userMessageConfig.creationDate = creationDate;
				userMessageConfig.token = $('input[name=token]').val();;
				userMessageConfig.application = '<%= loginBean.getUser().getApplicationId() %>';
				userMessageConfig.owner = $('input[name=target]').val();;
				userMessageConfig.creator = $('input[name=target]').val();;
				userMessageConfig.target = '<%= loginBean.getUserId() %>';
				userMessageConfig.subject = '';
				userMessageConfig.message = '';
				sdkConnection.pollUserToUserMessages(userMessageConfig, function(messageConfig) {
					if(messageConfig.creationDate != null) {
						mostRecentCreationDate = messageConfig.creationDate;
						creationDate = mostRecentCreationDate;
					}
					if(creationDate != null && messageConfig.message != null && messageConfig.creator != null) {
						let messageTable = $('#botplatformchatconsole tbody');
						let tr = document.createElement('tr');
						tr.style.verticalAlign = "top";
						let td = document.createElement('td');
						let td2 = document.createElement('td');
						let div = document.createElement('div');
						let div2 = document.createElement('div');
						let span = document.createElement('span');
						let span2 = document.createElement('span');
						let alink = document.createElement('a');
						alink.style.cssText = 'text-decoration:none;';
						alink.setAttribute('href', 'login?view-user=' + messageConfig.creator);
						let img = document.createElement('img');
						img.className = 'botplatformmessage-user';
						img.setAttribute('src', messageConfig.avatar);
						td.className = 'botplatformmessage-user-1';
						td2.className = 'botplatformmessage-1';
						div.className = 'botplatformmessage-1-div';
						div2.className = 'botplatformmessage-1-div-2';
						span.className = 'botplatformmessage-user-1';
						span2.className = 'botplatformmessage-1';
						td.setAttribute('align', 'left');
						td.setAttribute('nowrap', 'nowrap');
						td.style.cssText = 'padding-bottom:5px;padding-right:15px;';
						td2.setAttribute('align', 'left');
						td2.setAttribute('width', '100%');		
						alink.appendChild(img);
						td.appendChild(alink);
						let date = new Date(creationDate);
						let time = date.getHours() + ":" + ((date.getMinutes() < 10)? "0" : "") + date.getMinutes() + ":" + ((date.getSeconds() < 10)? "0" : "") + date.getSeconds();
						span.innerHTML = messageConfig.creator + " <small>" + time + "</small>";
						span2.innerHTML = SDK.linkURLs(messageConfig.message);
						div.appendChild(span);
						div2.appendChild(span2);
						div.appendChild(div2);
						td2.appendChild(div);
						tr.appendChild(td);
						tr.appendChild(td2);
						messageTable.append(tr);
						scrollTable();
					}
				});
			}
			
			var counter = 0;
			var timeout = 0;
			var messagePolling = function() {
				setTimeout(function() {
					counter++;
					timeout+= 1000;
					pollMessagesPostRequest();
					if(counter <= 11) {
						messagePolling();
					} else {
						firstMessageSent = true;
						counter = 0;
						timeout = 0;
					}
				}, timeout);
			}
				
			var deleteUserMessages = function() {
				if (document.getElementById('delete-user-messages-confirm').checked) {
					let targetUserId = $('input[name=target]').val();
					$('input[name=targetUserId]').val(targetUserId);
					let targetIdTwo = $('input[name=targetUserId]').val();
					document.getElementById("user-messages-delete-form").submit();
					return true;
				} else {
					SDK.showError("You must click 'I'm sure' ");
					return false;
				}
			}
			
			var dialog = $("#dialog-delete-user-to-user-messages").dialog({
				autoOpen: false,
				resizable: false,
				height: "auto",
				width: 300,
				modal: true,
				buttons: {
					"Delete": function() {
						deleteUserMessages();
					},
					"Cancel": function() {
						$(this).dialog("close");
					}
				},
				open: function() {
					let deleteButton = $('.ui-dialog-buttonpane').find('button:contains("Delete")');
					deleteButton[0].style.color = "white";
					deleteButton[0].style.background = "#ef4c4c";
				}
			});
			
			$('#sendUserMessageForm').submit(function () {
				let userMessage = $('#user-message').val();
				userMessage = userMessage.trim();
				if (userMessage.length == 0) {
					return false;
				}
				$('#user-message').val("");
				
				let userMessageConfig = new UserMessageConfig();
				userMessageConfig.user = "<%= loginBean.getUserId() %>";
				userMessageConfig.token = $('input[name=token]').val();
				userMessageConfig.application = '<%= loginBean.getUser().getApplicationId() %>';
				userMessageConfig.owner = '<%= loginBean.getUserId() %>';
				userMessageConfig.creator = '<%= loginBean.getUserId() %>';
				userMessageConfig.target = $('input[name=target]').val();
				userMessageConfig.subject = $('input[name=subject]').val();
				userMessageConfig.message = userMessage;
				
				sdkConnection.createUserMessage(userMessageConfig, function(messageConfig) {
					let messageTable = $('#botplatformchatconsole tbody');
					let tr = document.createElement('tr');
					tr.style.verticalAlign = "top";
					let td = document.createElement('td');
					let div = document.createElement('div');
					let div2 = document.createElement('div');
					let span = document.createElement('span');
					let span2 = document.createElement('span');
					td.className = 'botplatformmessage-2';
					div.className = 'botplatformmessage-2-div';
					div2.className = 'botplatformmessage-2-div-2';
					span.className = 'botplatformmessage-user-2';
					span2.className = 'botplatformmessage-2';
					td.setAttribute('colspan', '2');
					td.setAttribute('align', 'left');
					td.setAttribute('width', '100%');
					let date = new Date(); 
					let time = date.getHours() + ":" + ((date.getMinutes() < 10)? "0" : "") + date.getMinutes() + ":" + ((date.getSeconds() < 10)? "0" : "") + date.getSeconds();
					span.innerHTML = userMessageConfig.creator + " <small>" + time + "</small>";
					span2.innerHTML = SDK.linkURLs(messageConfig.message);
					div.appendChild(span);
					div2.appendChild(span2);
					div.appendChild(div2);
					td.appendChild(div);
					tr.appendChild(td);
					messageTable.append(tr);
					scrollTable();
					if(messagesPage == 0 && firstMessageSent) { // poll messaging only on the first page
						firstMessageSent = false;
						messagePolling();
					}
				});
				return false;
			});
		</script>
	<% } %>
</div>
<% if (!embed) { %>
	</div>
	</div>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
<% } %>
<% proxy.clear(); %>
</body>
</html>
