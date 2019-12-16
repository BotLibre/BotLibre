<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.SelfBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% SelfBean bean = loginBean.getBean(SelfBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Import Scripts") %> - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">	
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<h3><%= loginBean.translate("Import Scripts") %></h3>
				<%= bean.searchFormHTML() %>
				<form action="script-import" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= bean.instanceInput() %>
					<%= bean.searchHTML() %>
					<br/>
					<input type="checkbox" name="optimize" checked title="<%= loginBean.translate("Compile the script to optimized byte-code. This is more efficient, but less dynamic.") %>"><%= loginBean.translate("Optimize") %></input>
					<input type="checkbox" name="index-static" title="<%= loginBean.translate("Select if static patterns should be indexed as responses instead of being compiled as case states (this can improve performance and reduce memory)") %>"><%= loginBean.translate("Index Patterns") %></input>
					<input type="checkbox" name="create-states" checked title="<%= loginBean.translate("Select if AIML imports should create state machines, this can improve performance and ensure correct ordering") %>"><%= loginBean.translate("Create States") %></input>
					<input type="checkbox" name="merge-state" title="<%= loginBean.translate("Select if the imported script should be merge with the last script") %>"><%= loginBean.translate("Merge") %></input>
					<input type="checkbox" name="debug" title="<%= loginBean.translate("Select if the script debug info should be stored (code, and line numbers).  This will make the script consume more memory") %>"><%= loginBean.translate("Include Debug") %></input>
					<br/>
					<input type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="<%= loginBean.translate("Import the selected scripts") %>">
					<input id="cancel" type="submit" name="cancel" value="<%= loginBean.translate("Cancel") %>" title="<%= loginBean.translate("Cancel import and go back to scripts page") %>">
				</form>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>