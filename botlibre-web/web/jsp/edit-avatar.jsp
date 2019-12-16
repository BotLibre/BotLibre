<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.AvatarBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AvatarBean bean = loginBean.getBean(AvatarBean.class);
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Edit Avatar<%= embed ? "" : " - " + Site.NAME %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<jsp:include page="avatar-banner.jsp"/>
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
			<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to edit a avatar") %>.
		</p>
	<% } else if (bean.getEditInstance() == null) { %>
		<p><%= loginBean.translate("No avatar selected.") %></p>
	<% } else if (!(bean.isAdmin() || bean.isSuper())) { %>
		<p><%= loginBean.translate("Must be avatar admin.") %></p>
	<% } else { %>
		<form action="avatar" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			
			<% bean.writeEditNameHTML(null, false, out); %>
			<% bean.writeEditCommonHTML(null, false, out); %>
			</table>
	
			<% bean.writEditAdCodeHTML(null, false, out); %>
	  	
			<input id="ok" name="save-instance" type="submit" value="<%= loginBean.translate("Save") %>"/><input id="cancel" name="cancel-instance" type="submit" value="<%= loginBean.translate("Cancel") %>"/><br/>
		</form>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>