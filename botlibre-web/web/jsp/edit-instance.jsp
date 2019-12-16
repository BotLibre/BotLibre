<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean bean = loginBean.getBotBean(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Bot - <%= Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= bean.getInstanceName() %></h1>
			<jsp:include page="error.jsp"/>
			<% if (!bean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a bot") %>.
				</p>
			<% } else if (bean.getInstance() == null) { %>
				<p><%= loginBean.translate("No bot selected.") %></p>
			<% } else if (!(bean.isAdmin() || bean.isSuper())) { %>
				<p><%= loginBean.translate("Must be bot admin.") %></p>
			<% } else { %>
			<% boolean external = bean.getInstance().isExternal(); %>
			<form action="bot" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= bean.instanceInput() %>
			
				<% bean.writeEditNameHTML(null, false, out); %>
				<% bean.writeEditCommonHTML(null, false, out); %>
				</table>
					
				<% if (!external) { %>
					<% if (bean.isSuper()) { %>
						<input name="isTemplate" type="checkbox" <% if (bean.getEditInstance().isTemplate()) { %>checked<% } %> title="<%= loginBean.translate("A template bot is only used as a template to create other bots from, and can only be accessed by its administrator") %>" ><%= loginBean.translate("Is Template") %></input><br/>
					<% } %>
					<%= loginBean.translate("Knowledge Limit") %><br/>
					<input name="memoryLimit" type="number" value="<%= bean.getEditInstance().getMemoryLimit() %>" title="<%= loginBean.translate("Max size of the bot's knowledgebase") %>"  /><br/>
				<% } else { %>
					<%= loginBean.translate("API Service") %>
					<script>
						var chooseTemplate = function() {
							var value = document.getElementById('apiService').value;
							if (value == 'BotLibre') {
								document.getElementById('apiURL').value = 'https://www.botlibre.com/rest/api/form-chat?instance=123&message=:message&conversation=:conversation&speak=:speak&application=';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '';
								document.getElementById('apiServerSide').checked = false;
							} else if (value == 'Paphus') {
								document.getElementById('apiURL').value = 'https://www.botlibre.biz/rest/api/form-chat?instance=123&message=:message&conversation=:conversation&speak=:speak&application=';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '';
								document.getElementById('apiServerSide').checked = false;
							} else if (value == 'AIML2') {
								document.getElementById('apiURL').value = 'http://www.pandorabots.com/pandora/talk-xml?botid=123&input=:message&custid=:conversation';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '<result custid=":conversation"><that>:response</that></result>';
								document.getElementById('apiServerSide').checked = true;
							} else if (value == 'Forge') {
								document.getElementById('apiURL').value = 'http://www.personalityforge.com/api/chat/?apiKey=123&chatBotID=123&message=:message&externalID=123:conversation';
								document.getElementById('apiPost').value = '';
								document.getElementById('apiResponse').value = '{"message":{"message":":response"}}';
								document.getElementById('apiServerSide').checked = true;
								document.getElementById('apiJSON').checked = true;
							}
						}
					</script>
					<select id="apiService" title="<%= loginBean.translate("Select API template from common bot API services") %>" onchange="chooseTemplate();">
						<option value=""></option>
						<option value="BotLibre">Bot Libre</option>
						<option value="Biz">Bot Libre for Business</option>
						<option value="AIML2">AIML2</option>
						<option value="Forge">Forge</option>
					</select><br/>
					<%= loginBean.translate("API URL") %><br/>
					<input type='text' id='apiURL' name='apiURL' title='API URL template' value="<%= bean.getEditInstance().getApiURL() %>"><br/>
					<%= loginBean.translate("API Post") %><br/>
					<textarea id='apiPost' name='apiPost' title='<%= loginBean.translate("API post argument template (XML, JSON, text, leave empty for GET)") %>'><%= bean.getEditInstance().getApiPost() %></textarea><br/>
					<%= loginBean.translate("API Response") %><br/>
					<textarea id='apiResponse' name='apiResponse' title='API response template (XML, JSON, text)'><%= bean.getEditInstance().getApiResponse() %></textarea><br/>
					<input type='checkbox' id='apiServerSide' name='apiServerSide' <%= bean.getInstance().getApiServerSide() ? "checked" : "" %> title="<%= loginBean.translate("Execute the API on the server to avoid CORS") %>"> <%= loginBean.translate("Run API on Server") %><br/>
					<input type='checkbox' id='apiJSON' name='apiJSON' <%= bean.getInstance().getApiJSON() ? "checked" : "" %> title="<%= loginBean.translate("The API returns JSON data") %>"> JSON<br/>
				<% } %>
				
				<% bean.writEditAdCodeHTML(null, false, out); %>
	  	
				<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
			</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>