<%@page import="org.botlibre.web.admin.BotAttachment"%>
<%@page import="org.botlibre.web.bean.AttachmentsBean"%>
<%@page import="java.util.Calendar"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	try {

	boolean embed = loginBean.isEmbedded();
	BotBean botBean = loginBean.getBotBean();
	AttachmentsBean bean = loginBean.getBean(AttachmentsBean.class);
	String title = "Attachments";
	if (botBean.getInstance() != null) {
		title = botBean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Attachments - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("View image and file attachments sent to the bot") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("attachments") %>"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="instance-banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The attachments tab allows you to view image and file attachments in your bot's database.") %><br/>
				</div>
				<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-attachments.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt-banner">
			<img src="images/graphic1.png" class="admin-banner-pic">
			<div>
				<p class="help">
					<%= loginBean.translate("The attachments tab allows you to view image and file attachments in your bot's database.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Attachments") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (!botBean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<p>
			<%= loginBean.translate("Caution, some attachments may be offensive, or be unsafe.") %><br/>
		</p>
		<form action="attachments" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= botBean.instanceInput() %>
			<input name="logs" type="hidden" value="show"/>
			<span class="menu">
				<div class='search-div'>
					<span class='search-span'><%= loginBean.translate("Duration") %></span>
					<select class="search" name="duration" onchange="this.form.submit()">
						<option value="none" <%= bean.getDurationCheckedString("none") %>></option>
						<option value="day" <%= bean.getDurationCheckedString("day") %>><%= loginBean.translate("current day") %></option>
						<option value="week" <%= bean.getDurationCheckedString("week") %>><%= loginBean.translate("current week") %></option>
						<option value="month" <%= bean.getDurationCheckedString("month") %>><%= loginBean.translate("current month") %></option>
						<option value="all" <%= bean.getDurationCheckedString("all") %>><%= loginBean.translate("all time") %></option>
					</select>
				</div>
				<input class="search" style="display:none;position:absolute;" type="submit" name="search" value="Search">
			</span>
			<br/><br/>
			<% if (!bean.getAttachmentsResults().isEmpty()) { %>
				<input id="select-icon" class="icon" type="submit" name="selectAllAttachments" value="" title="<%= loginBean.translate("Select all of the attachments") %>">
				<input id="remove-icon" class="icon" type="submit" name="deleteAttachments" value="" title="<%= loginBean.translate("Delete the selected attachments") %>">
				<br/>
				<% Calendar current, last = null; %>
				<% for (BotAttachment attachment : bean.getAttachmentsResults()) { %>
					<% if (last == null) { %>
						<% last = Calendar.getInstance(); last.setTime(attachment.getCreationDate()); %>
						<h5><%= new java.sql.Date(attachment.getCreationDate().getTime()) %></h5>
						<table style="width=100%;" cellspacing="4">
					<% } else { %>
						<% current = Calendar.getInstance(); current.setTime(attachment.getCreationDate()); %>
						<% if (last.get(Calendar.DAY_OF_YEAR) != current.get(Calendar.DAY_OF_YEAR)) { %>
							<% last = current; %>
							</table>
							<h5><%= new java.sql.Date(attachment.getCreationDate().getTime()) %></h5>
							<table style="width=100%;" cellspacing="4">
						<% } %>
					<% } %>
					<tr>
						<td><input type=checkbox name=<%= attachment.getMediaId() %> <%= bean.getAllCheckedString() %>></td>
						<td nowrap><span class="chat"><%= new java.sql.Time(attachment.getCreationDate().getTime()) %></span></td>
						<td nowrap><span class="chat"><%= attachment.getCreatorId() %> </span></td>
						<td nowrap><span class="chat"><%= attachment.getName() %> </span></td>
						<td nowrap><span class="chat"><%= attachment.getType() %> </span></td>
						<td nowrap><span class="chat"><%= Utils.linkHTML(Site.SECUREURLLINK + "/" + attachment.getFileName()) %> </span></td>
					</tr>
				<% } %>
				</table>
			<% } %>
			<br/><br/>
		</form>
	<% } %>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
<% } catch (Exception error) { loginBean.error(error); }%>
</body>
</html>
