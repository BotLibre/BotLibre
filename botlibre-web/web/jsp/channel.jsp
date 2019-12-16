<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	LiveChatBean bean = loginBean.getBean(LiveChatBean.class);
	String title = "Live Chat";
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
	<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
		<jsp:include page="channel-banner.jsp"/>
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
	<%= loginBean.translate("No channel selected") %>
<% } else if (!bean.isValidUser()) { %>
	<%= loginBean.translate("This user does not have access to this channel.") %>
<% } else { %>
	<% if (embed && !Site.COMMERCIAL && loginBean.getShowLink()) { %>
		<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>">
		 	<span>chat hosted by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
		</div>
	<% } %>
	<h1><%= bean.getDisplayInstance().getNameHTML() %></h1>
	
	<% bean.writeTabsHTML(proxy, embed, out); %>
	<br/>
		
	<% bean.writeExternalHTML(out); %>
	<% bean.writeFlaggedHTML(out); %>
		
	<% if (bean.getInstance() != null) { %>
		<form action="livechat" method="get" <%= (bean.getInstance().isExternal()) ? "target=\"_blank\"" : "" %> class="message">
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<input id="ok" name="chat" type="submit" value="Chat"/>
			<% if (!bean.getDisplayInstance().isExternal()) { %>
				<% if (!embed) { %>
					<input name="embed-instance" type="submit" value="Embed" title="Embed the channel on your own website">
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