<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.admin.BotInstance"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% BotBean bean = loginBean.getBotBean(); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Bot Link - <%= Site.NAME %></title>
	<meta name="description" content="Create a link to an external bot"/>	
	<meta name="keywords" content="create, link, website, bot, chatbot"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<% bean.disconnect(); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new bot link") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<jsp:include page="error.jsp"/>
			<% if (!bean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new bot link") %>.
				</p>
			<% } else { %>
				<form action="bot" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Bot Name") %></span><br/>
					<input class="required" name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" autofocus /><br/>
					
					<% bean.writeCreateCommonHTML(error, true, null, false, out); %>
					
					<%= loginBean.translate("API") %> <input type='checkbox' <%= error ? "checked" : "" %>
							onclick="document.getElementById('api').style.display=(this.checked ? 'inline' : 'none');"
							title="Connect to the external bot's web API"><br/>
					<div id='api' <%= error ? "" : "style='display:none'" %>>
					<%= loginBean.translate("API Service") %>
					<script>
						var chooseTemplate = function() {
							var value = document.getElementById('apiService').value;
							if (value == 'BotLibre') {
								document.getElementById('apiURL').value = 'https://www.botlibre.com/rest/api/form-chat?instance=123&message=:message&conversation=:conversation&speak=:speak&application=';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '';
								document.getElementById('apiServerSide').checked = true;
								document.getElementById('apiJSON').checked = false;
							} else if (value == 'Biz') {
								document.getElementById('apiURL').value = 'https://www.botlibre.biz/rest/api/form-chat?instance=123&message=:message&conversation=:conversation&speak=:speak&application=';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '';
								document.getElementById('apiServerSide').checked = true;
								document.getElementById('apiJSON').checked = false;
							} else if (value == 'AIML2') {
								document.getElementById('apiURL').value = 'http://www.pandorabots.com/pandora/talk-xml?botid=123&input=:message&custid=:conversation';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '<result custid=":conversation"><that>:response</that></result>';
								document.getElementById('apiServerSide').checked = true;
								document.getElementById('apiJSON').checked = false;
							} else if (value == 'Forge') {
								document.getElementById('apiURL').value = 'http://www.personalityforge.com/api/chat/?apiKey=123&chatBotID=123&message=:message&externalID=123:conversation';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '{"message":{"message":":response"}}';
								document.getElementById('apiServerSide').checked = true;
								document.getElementById('apiJSON').checked = true;
							}
						}
					</script>
					<select id="apiService" title="Select API template from common bot API services" onchange="chooseTemplate();">
						<option value=""></option>
						<option value="BotLibre">Bot Libre</option>
						<option value="Biz">Bot Libre for Business</option>
						<option value="AIML2">AIML2</option>
						<option value="Forge">Forge</option>
					</select><br/>
					<%= loginBean.translate("API URL") %><br/>
					<input type='text' id='apiURL' name='apiURL' placeholder='API URL template' value="<%= error ? bean.getInstance().getApiURL() : "" %>"><br/>
					<%= loginBean.translate("API Post") %><br/>
					<textarea id='apiPost' name='apiPost' placeholder='API post argument template (XML, JSON, text, leave empty for GET)'><%= error ? bean.getInstance().getApiPost() : "" %></textarea><br/>
					<%= loginBean.translate("API Response") %><br/>
					<textarea id='apiResponse' name='apiResponse' placeholder='API response template (XML, JSON, text)'><%= error ? bean.getInstance().getApiResponse() : "" %></textarea><br/>
					<input type='checkbox' id='apiServerSide' name='apiServerSide' <%= error && bean.getInstance().getApiServerSide() ? "checked" : "" %> title="Execute the API on the server to avoid CORS"> Run API on Server<br/>
					<input type='checkbox' id='apiJSON' name='apiJSON' <%= error && bean.getInstance().getApiJSON() ? "checked" : "" %> title="The API returns JSON data"> JSON<br/>
					</div>
					
					<input id="ok" name="create-link" type="submit" value="<%= loginBean.translate("Create") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>