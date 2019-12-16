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
	<title>Create Bot - <%= Site.NAME %></title>
	<meta name="description" content="Create a new bot"/>	
	<meta name="keywords" content="create, bot, chatbot, virtual agent, twitterbot, facebook bot"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<% bean.disconnect(); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
			<h1><%= loginBean.translate("Create new bot") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<% 
				if (bean.getForking()) {
					error = true;
					bean.setForking(false);
				}
			%>
			<jsp:include page="error.jsp"/>
			<% if (!bean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new bot") %>.
				</p>
			<% } else { %>
				<form action="bot" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Bot Name") %></span><br/>
					<input class="required" name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" autofocus /><br/>
					
					<span class="required"><%= loginBean.translate("Template") %></span><br/>
					<input class="required" id="template" type="text" name="template" placeholder='<%= loginBean.translate("type of bot to create") %>' value="<%= bean.getTemplate() %>"/><br/>
					<script>
					var templateauto = $( "#template" );
					templateauto.autocomplete({
						source: [
							<% for (BotInstance instance : bean.getAllTemplates()) {%>
								{
									value: "<%= instance.getAlias() %>",
									label: "<%= instance.getAlias() %>",
									description: "<%= instance.getDescription().replace("\n", " ").replace("\r", " ") %>",
									image: "<%= bean.getAvatarImage(instance) %>"
								},
							<% } %>
							],
							minLength: 0,
							maxLength: 10
						});
					templateauto.data( "ui-autocomplete" )._renderItem = function( ul, item ) {
						var inner_html = '<a><table style="max-width:800px"><tr><td><img style="border: 1px solid #d5d5d5;height:80px;max-width:initial" src="'
								+ item.image + '"></td><td valign="top">'
								+ item.label + '<br/><span style="font-size:12px">'
								+ item.description + '</span></td></tr></table></a>';
						return $( "<li></li>" )
								.data( "item.autocomplete", item )
								.append(inner_html)
								.appendTo( ul );
					};
					templateauto.on('focus', function(event) {
						var self = this;
						$(self).autocomplete("search", "");
					});
					</script>
					
					<% bean.writeCreateCommonHTML(error, false, null, false, out); %>
					</table>

					<% if (bean.isSuper()) { %>
						<input name="isTemplate" type="checkbox" <% if (error && bean.getInstance().isTemplate()) { %>checked<% } %> title="A template bot is only used as a template to create other bots from, and can only be accessed by its administrator" onMouseOut="javascript:return false;"><%= loginBean.translate("Is Template") %></input><br/>
						<!-- input name="isSchema" type="checkbox" <% if (error && bean.getInstance().isSchema()) { %>checked<% } %> title="The bots database can be created as a schema, or as its own database" onMouseOut="javascript:return false;"><%= loginBean.translate("Is Schema") %></input><br/ -->
					<% } %>
					
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