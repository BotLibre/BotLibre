<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean bean = loginBean.getBean(DomainBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Workspace<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<jsp:include page="domain-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="section">
<% } %>
	<h1><%= bean.getInstanceName() %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isLoggedIn()) { %>
		<p>
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a workspace") %>.
		</p>
	<% } else if (bean.getEditInstance() == null) { %>
		<p><%= loginBean.translate("No workspace selected.") %></p>
	<% } else if (!(bean.isAdmin() || bean.isSuper())) { %>
		<p><%= loginBean.translate("Must be workspace admin.") %></p>
	<% } else { %>
	
	<form action="domain" method="post" class="message">
		<%= loginBean.postTokenInput() %>
		<%= proxy.proxyInput() %>
		<%= bean.instanceInput() %>
		
		<% bean.writeEditNameHTML(null, false, out); %>
		<% bean.writeEditCommonHTML(null, false, out); %>
		<tr>
			<td><%= loginBean.translate("Creation Mode") %></td>
			<td><select name="creationMode" title="<%= loginBean.translate("Define who can create content in this workspace") %>">
				<option value="Everyone" <%= bean.isCreationModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
				<option value="Users" <%= bean.isCreationModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
				<option value="Members" <%= bean.isCreationModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
				<option value="Administrators" <%= bean.isCreationModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
			</select></td>
		</tr>
		</table>
		
		<% bean.writEditAdCodeHTML(null, false, out); %>
		
		<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
	</form>
	<% bean.writePublishDialogHTML(proxy, out); %>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
<% } %>
<% proxy.clear(); %>
</body>
</html>