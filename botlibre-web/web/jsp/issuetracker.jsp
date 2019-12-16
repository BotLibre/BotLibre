<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	IssueTrackerBean bean = loginBean.getBean(IssueTrackerBean.class);
	String title = "Issue Tracker";
	if (bean.getDisplayInstance() != null) {
		title = bean.getDisplayInstance().getName();
	}
	if (!embed) {
		title = title + " - " + Site.NAME;
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<% bean.writeHeadMetaHTML(out); %>
	<jsp:include page="head.jsp"/>
	<% bean.writeHeadMetaHTML(out); %>
	<title><%= title %></title>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="issuetracker-banner.jsp"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<% bean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>	
	<jsp:include page="error.jsp"/>
	<% if (bean.getDisplayInstance() == null) { %>
		<%= loginBean.translate("No issue tracker selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this issue tracker.") %>
	<% } else { %>
		<h1><%= bean.getDisplayInstance().getNameHTML() %></h1>
		
		<% bean.writeTabsHTML(proxy, embed, out); %>
		
		<% bean.writeExternalHTML(out); %>
		<% bean.writeFlaggedHTML(out); %>
		
		<% if (bean.getInstance() != null) { %>
			<form action="issuetracker" method="post" class="message" <%= (bean.getInstance().isExternal()) ? "target=\"_blank\"" : "" %>>
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<input name="issues" type="submit" value="<%= loginBean.translate("Issues") %>" title="<%= loginBean.translate("Browse issues") %>"/>
				<% if (!bean.getDisplayInstance().isExternal()) { %>
					<% if (bean.getDisplayInstance().getSubscribers().contains(bean.getUser())) { %>
						<input name="unsubscribe" type="submit" value="<%= loginBean.translate("Unsubscribe") %>" title="<%= loginBean.translate("Unsubscribe from a weekly email summary of the issue tracker activity") %>">
					<% } else { %>
						<input name="subscribe" type="submit" value="<%= loginBean.translate("Subscribe") %>" title="<%= loginBean.translate("Subscribe to a weekly email summary of the issue tracker activity") %>">
					<% } %>
				<% } %>
			</form>
			<br/>
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