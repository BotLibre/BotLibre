<%@page import="org.botlibre.web.admin.User.UserType"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.ForumPostBean"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.forum.ForumPost"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	ForumBean forumBean = loginBean.getBean(ForumBean.class);
	ForumPostBean forumPostBean = loginBean.getBean(ForumPostBean.class);
	forumPostBean.setForumBean(forumBean);
	String title = "Forum Post";
	if (forumPostBean.getInstance() != null) {
		title = forumPostBean.getInstance().getTopic();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<% if (forumBean.getDisplayInstance() != null && forumPostBean.getInstance() != null) { %>
		<meta name="description" content="<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(forumPostBean.getInstance().getSummary()))) %>"/>	
		<link rel="image_src" href="http://<%= Site.URL %>/<%= forumBean.getAvatarImage(forumBean.getDisplayInstance()) %>">
		<meta property="og:image" content="http://<%= Site.URL %>/<%= forumBean.getAvatarImage(forumBean.getDisplayInstance()) %>" />
	<% } %>
	<jsp:include page="head.jsp"/>
	<% if (forumBean.getDisplayInstance() != null && forumPostBean.getInstance() != null) { %>
		<meta name="description" content="<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(forumPostBean.getInstance().getSummary()))) %>"/>	
		<link rel="image_src" href="http://<%= Site.URL %>/<%= forumBean.getAvatarImage(forumBean.getDisplayInstance()) %>">
		<meta property="og:image" content="http://<%= Site.URL %>/<%= forumBean.getAvatarImage(forumBean.getDisplayInstance()) %>" />
	<% } %>
	<title><%= title %><%= embed ? "" : " - " + Site.NAME %></title>
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= loginBean.getJQueryHeader() %>
	
	<!-- Twitter Card -->
	<meta name="twitter:card" content="summary" />
	<meta name="twitter:title" content="<%= title %>" />
	<meta name="twitter:description" content="<%= Utils.removeCRs(Utils.escapeQuotes(Utils.stripTags(forumPostBean.getInstance().getSummary()))) %>" />
	<meta name="twitter:image" content="http://<%= Site.URL %>/<%= forumBean.getAvatarImage(forumBean.getDisplayInstance()) %>" />
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="forum-banner.jsp"/>
	<% } %>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<jsp:include page="banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<% if (forumPostBean.getInstance() != null) { %>
				<a href=<%= "forum?id=" + forumPostBean.getInstance().getForum().getId() + proxy.proxyString() %>><%= forumPostBean.getInstance().getForum().getName() %></a>			
				<% if ((forumPostBean.getInstance() != null) && (forumPostBean.getInstance().getParent() != null)) { %>
					: <a href="<%= "forum-post?id=" + forumPostBean.getInstance().getParent().getId() + proxy.proxyString() %>"><%= forumPostBean.getInstance().getParent().getTopic() %></a>
				<% } %>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (forumPostBean.getInstance() == null) { %>
		<%= loginBean.translate("No forum post selected") %>
	<% } else if (!forumBean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this post.") %>
	<% } else { %>
		
		<!-- Ad -->
		<% if (forumBean.showAds()) { %>
			<%= forumPostBean.getAdCode() %>
		<% } %>
		<table style="width:100%">
			<tr style="width:100%">
				<td align="left">
					<a href="<%= "login?view-user=" + forumPostBean.encodeURI(forumPostBean.getInstance().getCreator().getUserId()) + proxy.proxyString() %>">
						<img src="<%= forumPostBean.getAvatarThumb(forumPostBean.getInstance()) %>" class="user-thumb"/>
					</a>
				</td>
				<td style="width:100%" align="left" valign="top">
					<h1 style="margin-left:10px"><%= forumPostBean.getInstance().getTopic() %></h1>
				</td>
			</tr>
		</table>
		<table>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<span class="menu" style="font-weight:bold">
						<%= loginBean.translate("by") %> <a class="user" href="<%= "login?view-user=" + forumPostBean.encodeURI(forumPostBean.getInstance().getCreator().getUserId()) + proxy.proxyString() %>">
						<%= forumPostBean.getInstance().getCreator().getUserHTML() %></a> 
						<%= loginBean.translate("posted") %> <%= Utils.displayTimestamp(forumPostBean.getInstance().getCreationDate())%>
					</span>
					<p>
					<%= forumPostBean.getInstance().getTextDetails() %>
					</p>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<form action="forum-post" method="get" class="message">
						<%= proxy.proxyInput() %>
						<%= forumPostBean.instanceInput() %>
						<%= forumPostBean.getInstance().printReplies(forumPostBean, proxy) %>
					</form>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<form action="forum-post" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= proxy.proxyInput() %>
						<%= forumPostBean.instanceInput() %>
						<% if (forumPostBean.getInstance().getForum().isReplyAllowed(loginBean.getUser())) { %>
							<input name="reply" type="submit" value="<%= loginBean.translate("Reply") %>"/>
						<% } else { %>
						    <input id="disabled" disabled="disabled" title="<%= loginBean.translate("You do not have access to reply") %>" name="reply" type="submit" value="<%= loginBean.translate("Reply") %>"/>
						<% } %>
						<% if (forumPostBean.getInstance().getSubscribers().contains(forumPostBean.getUser())) { %>
							<input name="unsubscribe" type="submit" value="<%= loginBean.translate("Unsubscribe") %>" title="<%= loginBean.translate("Unsubscribe from email notification of replies") %>">
						<% } else { %>
							<input name="subscribe" type="submit" value="<%= loginBean.translate("Subscribe") %>" title="<%= loginBean.translate("Subscribe to email notification of replies") %>">
						<% } %>
					</form>
					<br/>
				</td>
			</tr>
			<tr style="width:100%">
				<td colspan="2" style="width:100%" align="left" valign="top">
					<span class="menu">
					<%= loginBean.translate("Id") %>: <%= forumPostBean.getInstance().getId() %><br/>
					<% if (!forumPostBean.getInstance().getTags().isEmpty()) { %>
						<%= loginBean.translate("Tags") %>: <%= forumPostBean.getInstance().getTagLinks("forum-post?tags"  + proxy.proxyString() + forumBean.forumString() + "&tag-filter=") %><br/>
					<% } %>
					<%= loginBean.translate("Posted") %>: <%= Utils.displayTimestamp(forumPostBean.getInstance().getCreationDate()) %><br/>
					<% if (forumPostBean.getInstance().getUpdatedDate() != null) { %>
						<%= loginBean.translate("Updated") %>: <%= Utils.displayTimestamp(forumPostBean.getInstance().getUpdatedDate()) %><br/>
					<% } %>
					<%= loginBean.translate("Replies") %>: <%= forumPostBean.getInstance().getReplyCount() %><br/>
					<%= loginBean.translate("Views") %>: <%= forumPostBean.getInstance().getViews() %>,
					<%= loginBean.translate("today") %>: <%= forumPostBean.getInstance().getDailyViews() %>, <%= loginBean.translate("week") %>: <%= forumPostBean.getInstance().getWeeklyViews() %>, <%= loginBean.translate("month") %>: <%= forumPostBean.getInstance().getMonthlyViews() %><br/>
					</span>
					<% if (forumPostBean.getInstance().isFlagged()) { %>
						  <p style="color:red;font-weight:bold;"><%= loginBean.translate("This post has been flagged for") %> "<%= forumPostBean.getInstance().getFlaggedReason() %>" <%= loginBean.translate("by") %> <%= forumPostBean.getInstance().getFlaggedUser() %>.</p>
					<% } %>

					<% forumPostBean.writeToolbarHTML(proxy, embed, out); %>

					<% forumPostBean.writeAddThisHTML(out); %>
				</td>
			</tr>
		</table>

		<% forumPostBean.writeStarDialogHTML(proxy, embed, out); %>
		<% forumPostBean.writeDeleteDialogHTML(proxy, out); %>
		<% forumPostBean.writeFlagDialogHTML(proxy, embed, out); %>

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