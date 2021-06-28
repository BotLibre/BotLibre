<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	BotBean bean = loginBean.getBotBean();
	String title = "Bot";
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
	<jsp:include page="instance-banner.jsp"/>
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
		<%= loginBean.translate("No bot selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this bot") %>
	<% } else { %>
		<h1><%= bean.getDisplayInstance().getNameHTML() %></h1>
		
		<div id="tabs" class="ui-tabs ui-widget ui-widget-content ui-corner-all">
			<% bean.writeTabLabelsHTML(proxy, embed, out); %>
			<% bean.writeInfoTabHTML(proxy, embed, out); %>
			<div id="tabs-2" class="ui-tabs-hide">
				<p>
					<% if (!bean.getDisplayInstance().getDetails().isEmpty()) { %>
						<span class="details"><%= bean.getDisplayInstance().getDetails() %></span><br/>
					<% } %>
					<span>
					<%= loginBean.translate("License") %>: <%= bean.getDisplayInstance().getLicense() %><br/>
					<%= loginBean.translate("Created") %>: <%= Utils.displayDate(bean.getDisplayInstance().getCreationDate()) %><br/>
					<%= loginBean.translate("Creator") %>: <a href="<%= "login?view-user=" + bean.encodeURI(bean.getDisplayInstance().getCreatorUserId()) + proxy.proxyString() %>">
					<%= bean.getDisplayInstance().getCreatorUserId() %></a>
					: <a class="menu" href="<%="login?send-message=" + bean.encodeURI(bean.getDisplayInstance().getName()) + "&user=" + bean.encodeURI(bean.getDisplayInstance().getCreatorUserId()) + proxy.proxyString() %>" title="Send a message to the bot's creator">Send Message</a><br/>
					<% if (!embed && !bean.getDisplayInstance().isExternal()) { %>
						<%= loginBean.translate("Access") %>: <%= bean.getDisplayInstance().getAccessMode() %><%= bean.getDisplayInstance().isHidden() ? " (hidden)" : "" %><%= bean.getDisplayInstance().isPrivate() ? " (private)" : "" %><br/>
						<%= loginBean.translate("Id") %>: <%= bean.getDisplayInstance().getId() %>
						<% if (bean.isSuper()) { %>
							<%= bean.getDisplayInstance().isSchema() ? "Schema" : "Database" %>: <%= bean.getDisplayInstance().getDatabaseId() %>
							<% if (bean.getDisplayInstance().getDomainForwarder() != null) { %>
								<br/><%= loginBean.translate("Forwarder") %>: <input type='text' style="width:90%" value='<%= bean.getDisplayInstance().getDomainForwarder().getForwarderAddress() %>'/>
							<% } %>
						<% } %>
						<br/>
						<%= loginBean.translate("Link") %>: <a target="_blank" href="<%= "http://" + Site.URL + "/browse?id=" + bean.getDisplayInstance().getId() %>"><%= "http://" + Site.URL + "/browse?id=" + bean.getDisplayInstance().getId() %></a><br/>
						<%= loginBean.translate("Knowledge") %>: <%= bean.getDisplayInstance().getMemorySize() %> objects (max <%= bean.getDisplayInstance().getMemoryLimit() %>)<br/>
					<% } %>
					</span>
				</p>
			</div>
			<% bean.writeStatsTabHTML(proxy, embed, out); %>
		</div>
		
		<% bean.writeExternalHTML(out); %>
		<% bean.writeFlaggedHTML(out); %>
		<% bean.writeReviewHTML(out); %>
		
		<% if (bean.getDisplayInstance().isArchived()) { %>
			<p><span><%= loginBean.translate("This bot has been archived due to over 3 months of inactivity, to recover this bot please email") %> support@botlibre.com.</span></p>
		<% } %>
		<% if (bean.isAdmin() && !bean.getDisplayInstance().getErrors().isEmpty()) { %>
			<p><span><b><%= loginBean.translate("This bot has warnings or errors, check the") %> <a href="bot?log=true"><%= loginBean.translate("log") %></a> <%= loginBean.translate("to view or clear them") %>.</b></span></p>
		<% } %>
		<% if (!Site.COMMERCIAL && bean.isAdmin() && bean.getUser().isBasic()) { %>
			<p><span><b><%= loginBean.translate("Bots on Basic accounts may be archived or deleted if inactive for 3 months.") %> <a href="upgrade.jsp"><%= loginBean.translate("Upgrade") %></a> <%= loginBean.translate("your account to ensure your bot is not archived or deleted.") %></b></span></p>
		<% } %>
		
		<% if (bean.getInstance() != null) { %>
			<form action="bot" method="get" class="message">
				<%= bean.instanceInput() %>
				<% if (!bean.getDisplayInstance().isArchived()  && !bean.getDisplayInstance().isTemplate()) { %>
					<input id="ok" name="dynamicChat" <%= (bean.getInstance().isExternal() && !bean.getInstance().hasAPI()) ? "formtarget=\"_blank\"" : "" %> type="submit" value="<%= loginBean.translate("Chat") %>" title="<%= loginBean.translate("Chat with the bot on the web") %>"/>
				<% } %>
				<% if (!embed) { %>
					<% if (!bean.getDisplayInstance().isExternal() && !bean.getDisplayInstance().isArchived() && !bean.getDisplayInstance().isTemplate()) { %>
						<!--input id="ok" formmethod="get" name="chat" type="submit" value="Web Chat" title="Chat with the bot on the web using static HTML"/-->
						<input id="ok" name="livechat" type="submit" value="<%= loginBean.translate("Live Chat") %>" title="<%= loginBean.translate("Connect to the bot's live chat channel to chat with it, or its administrator") %>"/>
						<input id="ok" name="chatroom" type="submit" value="<%= loginBean.translate("Chat Room") %>" title="<%= loginBean.translate("Connect to the bot's chat room channel to chat with it and other users") %>"/>
					<% } %>
				<% } %>
			</form>
			<form action="bot" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<% if (!embed) { %>
					<% if (!embed && bean.isMember() && !bean.getDisplayInstance().isExternal() && !bean.getDisplayInstance().isArchived() && (bean.getDisplayInstance().getAllowForking() || bean.isAdmin())) { %>
						<% if (bean.isLoggedIn()) { %>
							<input name="fork" type="submit" value="Fork" title="<%= loginBean.translate("Create a clone of this bot that you will own and administer") %>"/>
						<% } else { %>
							<input id="disabled" name="fork" type="submit" value="<%= loginBean.translate("Fork") %>" disabled="disabled" title="<%= loginBean.translate("You must sign in first") %>"/>
						<% } %>
					<% } %>
					<% if (!embed && bean.isMember() && !bean.getDisplayInstance().isExternal() && !bean.getDisplayInstance().isArchived() && !bean.getDisplayInstance().isTemplate()) { %>
						<% if (bean.isLoggedIn()) { %>
							<input name="embed-instance" type="submit" value="<%= loginBean.translate("Embed") %>" title="<%= loginBean.translate("Generate embedding code to add the bot to your own website") %>"/>
						<% } else { %>
							<input id="disabled" name="embed-instance" type="submit" value="<%= loginBean.translate("Embed") %>" disabled="disabled" title="<%= loginBean.translate("You must sign in first") %>"/>
						<% } %>
					<% } %>
					<% if (bean.isAdmin() && !bean.getDisplayInstance().isArchived()) { %>
						<input name="admin" type="submit" value="<%= loginBean.translate("Admin") %>" title="<%= loginBean.translate("Train, configure, and monitor your bot") %>"/>
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