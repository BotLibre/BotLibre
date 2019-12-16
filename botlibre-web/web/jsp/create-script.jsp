<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ScriptBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% ScriptBean bean = loginBean.getBean(ScriptBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Script - <%= Site.NAME %></title>
	<meta name="description" content="Create a new script"/>	
	<meta name="keywords" content="create, website, script, file, code"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new script") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<% 
				if (bean.getForking()) {
					error = true;
					bean.setForking(false);
				}
			%>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new script") %>.
				</p>
			<% } else { %>
				<form action="script" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Script Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" /><br/>
					<%= loginBean.translate("Language") %><br/>
					<input id="language" name="language" type="text" value="<%= (!error) ? "" : bean.getInstance().getLanguage() %>" placeholder="programming language or type of script" /><br/>
			  		<script>
					$( "#language" ).autocomplete({
					source: [<%= bean.getAllLanguagesString() %>],
				    minLength: 0
					}).on('focus', function(event) {
					    var self = this;
					    $(self).autocomplete("search", "");
					});
					</script>
					
					<% bean.writeCreateCommonHTML(error, false, null, false, out); %>
					</table>
					
					<% bean.writeCreateAdCodeHTML(error, null, false, out); %>
				  	
					<input id="ok" name="create-instance" type="submit" value="<%= loginBean.translate("Create") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>