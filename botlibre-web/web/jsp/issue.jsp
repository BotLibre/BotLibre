<%@page import="org.botlibre.web.admin.User.UserType"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.IssueBean"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>
<%@page import="org.botlibre.web.issuetracker.Issue"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	IssueTrackerBean issueTrackerBean = loginBean.getBean(IssueTrackerBean.class);
	IssueBean bean = loginBean.getBean(IssueBean.class);
	bean.setIssueTrackerBean(issueTrackerBean);
	String title = "Issue";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getTitle();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<% if (issueTrackerBean.getDisplayInstance() != null && bean.getInstance() != null) { %>
		<meta name="description" content="<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(bean.getInstance().getSummary()))) %>"/>	
		<link rel="image_src" href="http://<%= Site.URL %>/<%= issueTrackerBean.getAvatarImage(issueTrackerBean.getDisplayInstance()) %>">
		<meta property="og:image" content="http://<%= Site.URL %>/<%= issueTrackerBean.getAvatarImage(issueTrackerBean.getDisplayInstance()) %>" />
	<% } %>
	<jsp:include page="head.jsp"/>
	<% if (issueTrackerBean.getDisplayInstance() != null && bean.getInstance() != null) { %>
		<meta name="description" content="<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(bean.getInstance().getSummary()))) %>"/>	
		<link rel="image_src" href="http://<%= Site.URL %>/<%= issueTrackerBean.getAvatarImage(issueTrackerBean.getDisplayInstance()) %>">
		<meta property="og:image" content="http://<%= Site.URL %>/<%= issueTrackerBean.getAvatarImage(issueTrackerBean.getDisplayInstance()) %>" />
	<% } %>
	<title><%= title %><%= embed ? "" : " - " + Site.NAME %></title>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	
	<!-- Twitter Card -->
	<meta name="twitter:card" content="summary" />
	<meta name="twitter:title" content="<%= title %>" />
	<meta name="twitter:description" content="<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(bean.getInstance().getSummary()))) %>" />
	<meta name="twitter:image" content="http://<%= Site.URL %>/<%= issueTrackerBean.getAvatarImage(issueTrackerBean.getDisplayInstance()) %>" />
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
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<% if (bean.getInstance() != null) { %>
				<a href=<%= "issuetracker?issues=true&id=" + bean.getInstance().getTracker().getId() + proxy.proxyString() %>><%= bean.getInstance().getTracker().getName() %></a>			
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (bean.getInstance() == null) { %>
		<%= loginBean.translate("No issue selected") %>
	<% } else if (!issueTrackerBean.isValidUser() || (bean.getInstance().isHidden() && !issueTrackerBean.isAdmin())) { %>
		<%= loginBean.translate("This user does not have access to this issue.") %>
	<% } else { %>
		
		<!-- Ad -->
		<% if (issueTrackerBean.showAds()) { %>
			<%= bean.getAdCode() %>
		<% } %>

		<h1><img src="<%= bean.getAvatarThumb(bean.getInstance()) %>" class="title"/> <%= bean.getInstance().getTitle() %></h1>
		<table>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<p>
					<%= bean.getInstance().getTextDetails() %>
					</p>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<form action="issue" method="get" class="message">
						<%= proxy.proxyInput() %>
						<%= bean.instanceInput() %>
					</form>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<form action="issue" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= proxy.proxyInput() %>
						<%= bean.instanceInput() %>
						<% if (bean.getInstance().getSubscribers().contains(bean.getUser())) { %>
							<input name="unsubscribe" type="submit" value="<%= loginBean.translate("Unsubscribe") %>" title="<%= loginBean.translate("Unsubscribe from email notification of updates") %>">
						<% } else { %>
							<input name="subscribe" type="submit" value="<%= loginBean.translate("Subscribe") %>" title="<%= loginBean.translate("Subscribe to email notification of updates") %>">
						<% } %>
					</form>
					<br/>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<span class="menu">
					<%= loginBean.translate("Id") %>: <%= bean.getInstance().getId() %><br/>
					<%= loginBean.translate("Type") %>: <%= loginBean.translate(bean.getInstance().getType().name()) %> 
					<% if (bean.getInstance().isHidden()) { %>
						<%= loginBean.translate("(hidden)") %>
					<% } %>
					<br/>
					<%= loginBean.translate("Priority") %>: <%= loginBean.translate(bean.getInstance().getPriority().name()) %> 
					<% if (bean.getInstance().isPriority()) { %>
						<%= loginBean.translate("(high priority)") %>
					<% } %>
					<br/>
					<%= loginBean.translate("Status") %>: <%= loginBean.translate(bean.getInstance().getStatus().name()) %><br/>
					<% if (!bean.getInstance().getTags().isEmpty()) { %>
						<%= loginBean.translate("Tags") %>: <%= bean.getInstance().getTagLinks("issue?tags"  + proxy.proxyString() + bean.issueTrackerString() + "&tag-filter=") %><br/>
					<% } %>
					<%= loginBean.translate("Created") %>: <%= Utils.displayTimestamp(bean.getInstance().getCreationDate()) %> 
					<%= loginBean.translate("by") %> <a class="user" href="<%= "login?view-user=" + bean.encodeURI(bean.getInstance().getCreator().getUserId()) + proxy.proxyString() %>">
						<%= bean.getInstance().getCreator().getUserHTML() %></a><br/>
					<% if (bean.getInstance().getUpdatedDate() != null) { %>
						<%= loginBean.translate("Updated") %>: <%= Utils.displayTimestamp(bean.getInstance().getUpdatedDate()) %><br/>
					<% } %>
					<%= loginBean.translate("Views") %>: <%= bean.getInstance().getViews() %>,
					<%= loginBean.translate("today") %>: <%= bean.getInstance().getDailyViews() %>, <%= loginBean.translate("week") %>: <%= bean.getInstance().getWeeklyViews() %>, <%= loginBean.translate("month") %>: <%= bean.getInstance().getMonthlyViews() %><br/>
					</span>
					<% if (bean.getInstance().isFlagged()) { %>
						  <p style="color:red;font-weight:bold;"><%= loginBean.translate("This issue has been flagged for") %> "<%= bean.getInstance().getFlaggedReason() %>" <%= loginBean.translate("by") %> <%= bean.getInstance().getFlaggedUser() %>.</p>
					<% } %>
					
					<% bean.writeToolbarHTML(proxy, embed, out); %>
		
					<% bean.writeAddThisHTML(out); %>
				</td>
			</tr>
		</table>

		<% bean.writeStarDialogHTML(proxy, embed, out); %>
		<% bean.writeDeleteDialogHTML(proxy, out); %>
		<% bean.writeFlagDialogHTML(proxy, embed, out); %>
		
	<% } %>
	</div>
	</div>
	</div>
<% if (!embed) { %>
	<jsp:include page="footer.jsp"/>
<% } else { %>
	<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
<% } %>
<% proxy.clear(); %>
</body>
</html>