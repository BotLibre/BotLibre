<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.service.Translation"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<% if (loginBean.checkEmbed(request, response)) { return; } %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Translation Console - <%= Site.NAME %></title>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents">
			<div class="about">
				<h1>Translation Console</h1>
				<jsp:include page="error.jsp"/>
				<% if (!loginBean.isSuper()) { %>
				  Must be sys admin
				<% } else { %>
					<form action="super" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						Text or ID to Translation<br/>
						<input type="text" name="text"/><br/>
						Source Language<br/>
						<input type="text" name="sourceLanguage" value="en" /><br/>
						Target Language<br/>
						<input type="text" name="targetLanguage" /><br/>
						Translation<br/>
						<input type="text" name="translation"/><br/>

						<input type="submit" name="addTranslation" value="Add Translation"/>
					</form>
					<h2>Bot Translations</h2>
					<p>Total: <%= AdminDatabase.instance().countBotTranslations() %></p>
					<table>
						<thead>
						<tr>
							<th>Source</th>
							<th>Target</th>
							<th>Count</th>
						</tr>
						</thead>
						<tbody>
						<% for (Object[] group : AdminDatabase.instance().groupBotTranslations()) { %>
							<tr>
								<td><%= group[0] %></td>
								<td><%= group[1] %></td>
								<td><%= group[2] %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<h2>Translations</h2>
					<p>Total: <%= AdminDatabase.instance().countTranslations() %></p>
					<table>
						<thead>
						<tr>
							<th>Source</th>
							<th>Target</th>
							<th>Count</th>
						</tr>
						</thead>
						<tbody>
						<% for (Object[] group : AdminDatabase.instance().groupTranslations()) { %>
							<tr>
								<td><%= group[0] %></td>
								<td><%= group[1] %></td>
								<td><%= group[2] %></td>
							</tr>
						<% } %>
						</tbody>
					</table>
					<form action="super" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<!-- table>
							<thead>
							<tr>
								<th></th>
								<th>Source</th>
								<th>Target</th>
								<th>Text</th>
								<th>Translation</th>
							</tr>
							</thead>
							<tbody>
							<% for (Translation translation : AdminDatabase.instance().getAllTranslations()) { %>
								<tr>
									<td><input type="checkbox" name="<%= "id-" + System.identityHashCode(translation) %>"></td>
									<td><%= translation.sourceLanguage %></td>
									<td><%= translation.targetLanguage %></td>
									<td><%= Utils.escapeHTML(translation.text) %></td>
									<td><%= Utils.escapeHTML(translation.translation) %></td>
								</tr>
							<% } %>
							</tbody>
						</table>
						<input type="submit" name="removeTranslations" value="Remove Translations"/>
						<br/-->
						<input type="submit" name="importTranslations" value="Import Translations" onclick="document.getElementById('upload-file').click(); return false;"/>
						<input type="submit" name="exportTranslations" value="Export Translations"/>
					</form>
					
					<form id="upload-form" action="upload-translations" method="post" enctype="multipart/form-data" class="message">
						<%= loginBean.postTokenInput() %>
						<input id="upload-file" class="hidden" onchange="this.form.submit()" type="file" name="file"/>
					</form>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>