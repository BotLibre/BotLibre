<%@page import="org.botlibre.web.admin.Category"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean bean = loginBean.getBean(DomainBean.class);
	String title = "Workspace";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Categories - <%= Site.NAME %></title>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="domain-banner.jsp"/>
	<jsp:include page="admin-domain-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-domain-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The categories tab allows you to remove categories from your workspace") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-domains.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt-banner">
			<img src="images/category1.png" class="admin-banner-pic" style="vertical-align:middle">
			<div>
				<p class="help">
					<%= loginBean.translate("The categories tab allows you to remove categories from your workspace") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Categories") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<form action="domain" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<table>
				<tr>
					<td><select class="users" name="selected-category" size=12>
						<% for (Category category : bean.getAllCategories()) { %>
							<option value="<%= category.getId()%>"><%= category.getType() + " : " + category.getName() %></option>
						<% } %>
					</select></td>
					<td valign="top">
						<input id="remove-icon" class="icon" type="submit" name="remove-category" value="" title="Remove the category"/><br/>
					</td>
				</tr>
			</table>
		</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>
