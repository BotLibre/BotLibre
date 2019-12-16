<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.MemoryImportBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% MemoryImportBean bean = loginBean.getBean(MemoryImportBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Import Data - <%= Site.NAME %></title>
	<meta name="description" content="Import JSON, CSV, set, map, properties, data scripts into your bot's knowledgebase"/>	
	<meta name="keywords" content="import, data, scripts, json, csv, set, map, properties, bot, chatbot"/>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">	
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<h3><%= loginBean.translate("Import Data") %></h3>
				<%= bean.searchFormHTML() %>
				<form action="memory-import" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= loginBean.getBotBean().instanceInput() %>
					<%= bean.searchHTML() %>
					<br/>
					<input type="checkbox" checked="checked" name="pin" title="Pin the data in the bot's memory so it will never forget them"><%= loginBean.translate("Pin") %></input><br/>
					<input type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="Import the selected data scripts">
					<input id="cancel" type="submit" name="cancel" value="<%= loginBean.translate("Cancel") %>" title="Cancel import and go back to knowledge page">
				</form>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>