<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ChatLogImportBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% ChatLogImportBean bean = loginBean.getBean(ChatLogImportBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Import Chat Logs - <%= Site.NAME %></title>
	<meta name="description" content="Import response lists, chat logs, or AIML scripts into your bot's knowledgebase"/>	
	<meta name="keywords" content="import, chat logs, response lists, aiml, scripts, training, bot, chatbot"/>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">	
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<h3><%= loginBean.translate("Import Chat Logs") %></h3>
				<%= bean.searchFormHTML() %>
				<form action="chatlog-import" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= loginBean.getBotBean().instanceInput() %>
					<%= bean.searchHTML() %>
					<br/>
					<input type="checkbox" checked="checked" name="autoReduce" title="<%= loginBean.translate("Configure if questions should be reduced to lower case when indexing responses.") %>"><%= loginBean.translate("Auto Reduce") %></input><br/>
					<input type="checkbox" checked="checked" name="pin" title="Pin the responses in the bot's memory so it will never forget them"><%= loginBean.translate("Pin") %></input><br/>
					<input type="checkbox" name="comprehension" title="Process chat logs through the bot's understanding, learning and comprehension (this will take much longer to import)"><%= loginBean.translate("Process learning and comprehension") %></input><br/>
					<input type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="Import the selected chat logs">
					<input id="cancel" type="submit" name="cancel" value="<%= loginBean.translate("Cancel") %>" title="Cancel import and go back to chat logs page">
				</form>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>