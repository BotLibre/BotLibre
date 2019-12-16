<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.bean.DomainBean.WizardState"%>
<%@page import="org.botlibre.web.admin.Payment"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	DomainBean bean = loginBean.getBean(DomainBean.class);
	String title = "Workspace";
	if (bean.getDisplayInstance() != null) {
		title = bean.getDisplayInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<% bean.writeHeadMetaHTML(out); %>
	<jsp:include page="head.jsp"/>
	<% bean.writeHeadMetaHTML(out); %>
	<title><%= title %><%= embed ? "" : " - " + Site.NAME %></title>
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
	<div class="browse">
<% } %>	
	<jsp:include page="error.jsp"/>
	<% if (bean.getDisplayInstance() == null) { %>
		<%= loginBean.translate("No workspace selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this workspace.") %>
	<% } else { %>
		<h1><%= bean.getDisplayInstance().getNameHTML() %></h1>
		
		<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD) && bean.getWizardState() == WizardState.Complete) { %>
			<p><b><%= loginBean.translate("Payment received, thank you.") %></b></p>
			<% bean.setWizardState(null); %>
		<% } %>
		
		<div id='tabs' class='ui-tabs ui-widget ui-widget-content ui-corner-all'>
			<% bean.writeTabLabelsHTML(proxy, embed, out); %>
			<% bean.writeInfoTabHTML(proxy, embed, out); %>
			<% bean.writeDetailsTabHTML(proxy, embed, out); %>
			<% bean.writeStatsTabHTML(proxy, embed, out); %>
		</div>
		
		<% bean.writeExternalHTML(out); %>
		<% bean.writeFlaggedHTML(out); %>
		
		<% if (bean.getDisplayInstance().isExpired()) { %>
			<p style="color:red;font-weight:bold;"><%= loginBean.translate("This workspace has expired") %></p>
		<% } %>
		
		<% bean.writeToolbarHTML(proxy, embed, out); %>		
		<br/>
		<% bean.writeAddThisHTML(out); %>
		<br/>
		<% bean.writeAd(out); %>
	
		<% bean.writeStarDialogHTML(proxy, embed, out); %>
		<% bean.writeDeleteDialogHTML(proxy, out); %>
		<% bean.writeFlagDialogHTML(proxy, embed, out); %>
		<% bean.writeChangeIconFormHTML(proxy, out); %>
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