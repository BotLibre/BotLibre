<%@page import="org.botlibre.web.admin.MediaFile"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.web.admin.Category"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.GraphicBean"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	GraphicBean bean = loginBean.getBean(GraphicBean.class);
	String title = "Graphic";
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
<% try { %>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<jsp:include page="graphic-banner.jsp"/>
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
		<%= loginBean.translate("No graphic selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this graphic") %>
	<% } else { %>
		<h1><%= bean.getDisplayInstance().getNameHTML() %></h1>
		
		<% bean.writeTabsHTML(proxy, embed, out); %>
		<br/>
		
		<% bean.writeExternalHTML(out); %>
		<% bean.writeFlaggedHTML(out); %>
		<% bean.writeReviewHTML(out); %>
		
		<% if (bean.isValidUser() && (bean.getInstance() != null) && (!bean.getInstance().isExternal()) && bean.getInstance().getMedia() != null) { %>
			<% MediaFile media = bean.getInstance().getMedia(); %>
			<% if (media.isVideo()) { %>
				<video src="<%= media.getFileName() %>"style="max-height:300px;max-width:300px;" controls></video>
				<br/><span class="menu"><%= media.getName() %> : <%= media.getType() %></span>
			<% } else if (media.isAudio()) { %>
				<audio src="<%= media.getFileName() %>" style="max-height:300px;max-width:300px;" controls></audio>
				<br/><span class="menu"><%= media.getName() %> : <%= media.getType() %></span>
			<% } else { %>
				<span class="dropt">
					<div style="position:absolute;right:4px">
						<img src="<%= media.getFileName() %>" style="max-height:600px;max-width:600px;min-width:200px"/>
					</div>
					<a href="<%= media.getFileName() %>" target="_blank"><img src="<%= media.getFileName() %>" alt="<%= media.getMediaId() %>" style="max-height:300px;max-width:300px;"/></a>
					<br/><span class="menu"><%= media.getName() %> : <%= media.getType() %></span>
				</span>
			<% } %>
			<br/>
		<% } %>
			
		<% if (bean.getInstance() != null && !bean.getInstance().isExternal()) { %>
			<form id="media-upload-form" action="graphic-media-upload" method="post" enctype="multipart/form-data" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<% if (bean.isAdmin()) { %>
					<input onclick="GraphicsUploader.openUploadDialog(this.form, 'Upload Media', true, true, false); return false;" type="submit" value="<%= loginBean.translate("Upload") %>" title="<%= loginBean.translate("Upload and share an image, video, or sound media file. Do not upload copyright material unless you own the copyright") %>"/>
				<% } %>
				<input type="submit" name="download-graphic" value="<%= loginBean.translate("Download") %>" title="<%= loginBean.translate("Download the media file") %>">
			</form>
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
<% } catch (Exception error) { loginBean.error(error); }%>
</body>
</html>