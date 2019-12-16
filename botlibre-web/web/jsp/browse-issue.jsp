<%@page import="java.io.StringWriter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.Tag"%>
<%@page import="org.botlibre.web.bean.IssueTrackerBean"%>
<%@page import="org.botlibre.web.issuetracker.IssueTracker"%>
<%@page import="org.botlibre.web.bean.IssueBean"%>
<%@page import="org.botlibre.web.issuetracker.Issue"%>
<%@page import="org.botlibre.web.bean.BrowseBean.InstanceFilter"%>
<%@page import="org.botlibre.web.bean.BrowseBean.InstanceSort"%>
<%@page import="org.botlibre.web.bean.BrowseBean.DisplayOption"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	IssueTrackerBean issueTrackerBean = loginBean.getBean(IssueTrackerBean.class);
	IssueBean bean = loginBean.getBean(IssueBean.class);
	bean.setIssueTrackerBean(issueTrackerBean);
	String title = "Issue Tracker";
	String issueTracker = "";
	if (issueTrackerBean.getInstance() != null) {
		title = issueTrackerBean.getInstance().getName();
		issueTracker = "&issuetracker=" + issueTrackerBean.getInstanceId();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<% issueTrackerBean.writeHeadMetaHTML(out); %>
	<jsp:include page="head.jsp"/>
	<% issueTrackerBean.writeHeadMetaHTML(out); %>
	<title><%= title %> Issues<%= embed ? "" : " - " + Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
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
	<% issueTrackerBean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="about">
<% } %>
<jsp:include page="error.jsp"/>
<% if (!issueTrackerBean.isValidUser() || (embed && (issueTrackerBean.getInstance() == null))) { %>
	<%= loginBean.translate("This user does not have access to this issue tracker.") %>
<% } else { %>
	<% if (embed && !Site.COMMERCIAL && loginBean.getShowLink()) { %>
		<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>">
		 	<span>issue tracker hosted by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
		</div>
	<% } %>
	
	<% issueTrackerBean.writeAd(out); %>
	
	<% List<Issue> instances = new ArrayList<Issue>(); %>
	<% if (bean.isDefaults()) { %>
		<% instances = bean.getAllPriorityInstances(); %>
	<% } %>
	<% int count = 0; %>
	<% int row = 0; %>
	<% String width = loginBean.isMobile() ? "300" : "800"; %>
	<% if (!instances.isEmpty()) { %>
		<h3><%= loginBean.translate("High Priority") %></h3>
			<form action="issue" method="get" class="message">
			<%= proxy.proxyInput() %>
			<%= issueTrackerBean.issueTrackerInput() %>
			<table cellspacing="5">
				<tr>
					<% for (Issue instance : instances) {%>
						<% count++; %>
						<% row++; %>
						<% if (row >= 2) { %>
							</tr>
							<tr>
							<% row = 1; %>
						<% } %>
						<td align="left" valign="top" class="user-thumb">
							<a style="text-decoration:none;" href="<%= "issue?id=" + instance.getId() + proxy.proxyString() %>">
								<img src="<%= bean.getAvatarThumb(instance) %>" class="user-thumb"/>
							</a>
						</td>
						<td valign="top" >
						 	<% if (!loginBean.isMobile()) { %>
								<span class="dropt">
								<div style="border-width:0px;padding:0;max-width:none">
									<table class="issue-box2">
										<tr>
											<td class="issue-article">
												<a style="text-decoration:none;" href="<%= "issue?id=" + instance.getId() + proxy.proxyString() %>">
													<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
														<span class="issue-priority-new"><%= instance.getTitle() %></span><br/>
													<% } else { %>
														<span class="issue-priority"><%= instance.getTitle() %></span><br/>
													<% } %>
													<span class="menu" style="font-weight:bold">
											  	  		<%= loginBean.translate("Created") %> <%= Utils.displayTimestamp(instance.getCreationDate())%> 
														<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %>
											  	  	</span><br/>
													<% if (instance.isFlagged()) { %>
														<span style="color:red;font-weight:bold;"><%= loginBean.translate("This issue is flagged.")%></span><br/>
													<% } else {%>
														<div id="issue-summary" class="issue-summary">
															<%= instance.getSummary() %>
														</div>
													<% } %>
													<span class="details">
													<% if (!instance.getTags().isEmpty()) { %>
														<%= loginBean.translate("Tags") %>: <%= instance.getTagsString() %><br/>
													<% } %>
													<% if (issueTrackerBean.getInstance() == null) { %>
														<%= loginBean.translate("Issue Tracker") %>: <%= instance.getTracker().getNameHTML() %><br/>
													<% } %>
													<% if (instance.getUpdatedDate() != null) { %>
														<%= loginBean.translate("Updated") %>: <%= Utils.displayTimestamp(instance.getUpdatedDate()) %><br/>
													<% } %>
													<%= loginBean.translate("Type") %>: <%= loginBean.translate(instance.getType().name()) %><br/>
													<%= loginBean.translate("Priority") %>: <%= loginBean.translate(instance.getPriority().name()) %><br/>
													<%= loginBean.translate("Status") %>: <%= loginBean.translate(instance.getStatus().name()) %><br/>
													<%= loginBean.translate("Views") %>: <%= instance.getViews() %>, <%= loginBean.translate("today") %>: <%= instance.getDailyViews() %>, <%= loginBean.translate("week") %>: <%= instance.getWeeklyViews() %>, <%= loginBean.translate("month") %>: <%= instance.getMonthlyViews() %><br/>
													<%= loginBean.translate("Thumbs up") %>: <%= instance.getThumbsUp() %>, <%= loginBean.translate("thumbs down") %>: <%= instance.getThumbsDown() %>, <%= loginBean.translate("stars") %>: <%= Utils.truncate(instance.getStars()) %><br/>
													</span>
												</a>
											</td>
										</tr>
									</table>
								</div>
							<% } %>
							<table class="issue-box">
								<tr><td class="issue-article">
										<a style="text-decoration:none;" href="<%= "issue?id=" + instance.getId() + proxy.proxyString() %>">
											<% if (instance.isFlagged()) { %>
												<span class="issue-flagged"><%= instance.getTitle() %></span><br/>
											<% } else { %>
												<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
													<span class="issue-priority-new"><%= instance.getTitle() %></span><br/>
												<% } else { %>
													<span class="issue-priority"><%= instance.getTitle() %></span><br/>
												<% } %>
											<% } %>
											<span class="menu" style="font-weight:bold">
									  	  		<%= loginBean.translate("Created") %> <%= Utils.displayTimestamp(instance.getCreationDate())%> 
												<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %>
									  	  	</span><br/>
											<span class="details">
											<% if (issueTrackerBean.getInstance() == null) { %>
												<%= loginBean.translate("Issue Tracker") %>: <%= instance.getTracker().getNameHTML() %> |
											<% } %>
											<%= loginBean.translate("Type") %>: <%= loginBean.translate(instance.getType().name()) %><br/>
											<%= loginBean.translate("Priority") %>: <%= loginBean.translate(instance.getPriority().name()) %><br/>
											<%= loginBean.translate("Status") %>: <%= loginBean.translate(instance.getStatus().name()) %><br/>
											<%= loginBean.translate("Views") %>: <%= instance.getViews() %></span><br/>
										</a>
								</td></tr>
							</table>
						 	<% if (!loginBean.isMobile()) { %>
								</span>
							<% } %>
						</td>
					<% } %>
				</tr>
			</table>
			</form>
	<% } %>
	<% if (issueTrackerBean.getInstance() != null) { %>
			<form action="issue" method="post" class="message">
			<%= proxy.proxyInput() %>
			<%= issueTrackerBean.issueTrackerInput() %>
			<% if (issueTrackerBean.getInstance().isIssueAllowed(loginBean.getUser())) { %>
				<input name="create-issue" type="submit" value="<%= loginBean.translate("New Issue") %>"/>
			<% } else { %>
				<input id="disabled" disabled="disabled" title="You do not have access to create issues" name="create-issue" type="submit" value="<%= loginBean.translate("New Issue") %>"/>
			<% } %>
			<input name="show-details" type="submit" value="<%= loginBean.translate("Details") %>"/>
		</form>
	<% } %>
	<h3><%= loginBean.translate("Browse") %></h3>
	<form action="issue" method="get" class="search">
		<%= proxy.proxyInput() %>
		<%= issueTrackerBean.issueTrackerInput() %>
		<span class="menu">
			<input type="radio" name="instance-filter" <%= bean.getInstanceFilterCheckedString(InstanceFilter.Public) %> value="public" onClick="this.form.submit()"><%= loginBean.translate("all issues") %></input>
			<input type="radio" name="instance-filter" <%= bean.getInstanceFilterCheckedString(InstanceFilter.Personal) %> value="personal" onClick="this.form.submit()"><%= loginBean.translate("my issues") %></input>
			<br/>
			<div class="search-div">
				<span class="search-span"><%= loginBean.translate("Title") %></span>
				<input id="searchtext" name="name-filter" type="text" value="<%= bean.getNameFilter() %>" />
			</div>
			<% if (issueTrackerBean.getInstance() == null) { %>
				<div class="search-div">
					<span class="search-span"><%= loginBean.translate("Categories") %></span>
					<input id="categories" name="category-filter" type="text" value="<%= bean.getCategoryFilter() %>"
									title="Filter by a comma seperated list of category names" />
					<script>
						$( "#categories" ).autocomplete({
						source: [<%= issueTrackerBean.getAllCategoriesString() %>],
							minLength: 0
						}).on('focus', function(event) {
								var self = this;
								$(self).autocomplete("search", "");
						});
					</script>
				</div>
			<% } %>
			<div class="search-div">
				<span class="search-span">Tags</span>
				<input id="tags" name="tag-filter" type="text" value="<%= bean.getTagFilter() %>"
						title="Filter by a comma seperated list of tag names" />
				<script>
					$( "#tags" ).autocomplete({
					source: [<%= bean.getAllTagsString() %>],
						minLength: 0
					}).on('focus', function(event) {
							var self = this;
							$(self).autocomplete("search", "");
					});
				</script>
			</div>
			<div class="search-div">
				<span class="search-span"><%= loginBean.translate("Display") %></span>
				<select name="display" onchange="this.form.submit()">
					<option value="header" <%= bean.getDisplayCheckedString(DisplayOption.Header) %>><%= loginBean.translate("Title") %></option>
					<option value="details" <%= bean.getDisplayCheckedString(DisplayOption.Details) %>><%= loginBean.translate("Details") %></option>
				</select>
			</div>
			<div class="search-div">
				<span class="search-span"><%= loginBean.translate("Sort") %></span>
				<select name="instance-sort" onchange="this.form.submit()">
					<option value="Name" <%= bean.getInstanceSortCheckedString(InstanceSort.Name) %>><%= loginBean.translate("name") %></option>
					<option value="Date" <%= bean.getInstanceSortCheckedString(InstanceSort.Date) %>><%= loginBean.translate("date") %></option>
					<option value="ThumbsUp" <%= bean.getInstanceSortCheckedString(InstanceSort.ThumbsUp) %>><%= loginBean.translate("thumbs up") %></option>
					<option value="ThumbsDown" <%= bean.getInstanceSortCheckedString(InstanceSort.ThumbsDown) %>><%= loginBean.translate("thumbs down") %></option>
					<option value="Stars" <%= bean.getInstanceSortCheckedString(InstanceSort.Stars) %>><%= loginBean.translate("stars") %></option>
					<option value="Connects" <%= bean.getInstanceSortCheckedString(InstanceSort.Connects) %>><%= loginBean.translate("views") %></option>
					<option value="DailyConnects" <%= bean.getInstanceSortCheckedString(InstanceSort.DailyConnects) %>><%= loginBean.translate("views today") %></option>
					<option value="WeeklyConnects" <%= bean.getInstanceSortCheckedString(InstanceSort.WeeklyConnects) %>><%= loginBean.translate("views this week") %></option>
					<option value="MonthlyConnects" <%= bean.getInstanceSortCheckedString(InstanceSort.MonthlyConnects) %>><%= loginBean.translate("views this month") %></option>
				</select>
			</div>
			<input style="display:none;position:absolute;" type="submit" name="search" value="Search">
		</span>
		<br/>
	</form>
	
	<% instances = bean.getAllInstances(); %>
	<span class='menu'>
		<%= bean.getResultsSize() %> results.<br/>
		<% bean.writePagingString(out, instances); %>
	</span>
	
	<table cellspacing="5">
		<tr>
			<% count = 0; %>
			<% row = 0; %>
			<% for (Issue instance : instances) {%>
				<% count++; %>
				<% row++; %>
				<% if (row >= 2) { %>
					</tr>
					<tr>
					<% row = 1; %>
				<% } %>
				<td align="left" valign="top" class="user-thumb">
					<a style="text-decoration:none;" href="<%= "issue?id=" + instance.getId() + proxy.proxyString() %>">
						<img src="<%= bean.getAvatarThumb(instance) %>" class="user-thumb"/>
					</a>
				</td>
				<td valign="top" >
					<% boolean showDetails = bean.getDisplayOption() == DisplayOption.Details; %>
					<% if (showDetails || !loginBean.isMobile()) { %>
						<% if (!showDetails) { %>
							<span class="dropt">
							<div style="border-width:0px;padding:0;max-width:none">
							<table class="issue-box2">
						<% } else { %>
							<table class="issue-box">
						<% } %>
						<tr>
							<td class="issue-article">
								<a style="text-decoration:none;" href="<%= "issue?id=" + instance.getId() + proxy.proxyString() %>">
									<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
										<span class="issue-title-new"><%= instance.getTitle() %></span><br/>
									<% } else { %>
										<span class="issue-title"><%= instance.getTitle() %></span><br/>
									<% } %>
									<span class="menu" style="font-weight:bold">
							  	  		<%= loginBean.translate("Created") %> <%= Utils.displayTimestamp(instance.getCreationDate())%> 
										<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %>
							  	  	</span><br/>
									<% if (instance.isFlagged()) { %>
										<span style="color:red;font-weight:bold;"><%= loginBean.translate("This issue is flagged.") %></span><br/>
									<% } else { %>
										<div id="issue-summary" class="issue-summary">
											<%= instance.getSummary() %>
										</div>
									<% } %>
									<span class="details">
									<% if (!instance.getTags().isEmpty()) { %>
										<%= loginBean.translate("Tags") %>: <%= instance.getTagsString() %><br/>
									<% } %>
									<% if (issueTrackerBean.getInstance() == null) { %>
										<%= loginBean.translate("Issue Tracker") %>: <%= instance.getTracker().getNameHTML() %><br/>
									<% } %>
									<%= loginBean.translate("Type") %>: <%= loginBean.translate(instance.getType().name()) %><br/>
									<%= loginBean.translate("Priority") %>: <%= loginBean.translate(instance.getPriority().name()) %><br/>
									<%= loginBean.translate("Status") %>: <%= loginBean.translate(instance.getStatus().name()) %><br/>
									<% if (instance.getUpdatedDate() != null) { %>
										<%= loginBean.translate("Updated") %>: <%= Utils.displayTimestamp(instance.getUpdatedDate()) %><br/>
									<% } %>
									<%= loginBean.translate("Views") %>: <%= instance.getViews() %>, <%= loginBean.translate("today") %>: <%= instance.getDailyViews() %>, <%= loginBean.translate("week") %>: <%= instance.getWeeklyViews() %>, <%= loginBean.translate("month") %>: <%= instance.getMonthlyViews() %><br/>
									<%= loginBean.translate("Thumbs up") %>: <%= instance.getThumbsUp() %>, <%= loginBean.translate("thumbs down") %>: <%= instance.getThumbsDown() %>, <%= loginBean.translate("stars") %>: <%= Utils.truncate(instance.getStars()) %><br/>
									</span>
							 	</a>
							</td>
						</tr>
						</table>
					<% } %>
					<% if (!showDetails) { %>
						<% if (!loginBean.isMobile()) { %>
							</div>
						<% } %>
						<table class="issue-box">
							<tr>
								<td class="issue-article">
									<a style="text-decoration:none;" href="<%= "issue?id=" + instance.getId() + proxy.proxyString() %>">
										<% if (instance.isFlagged()) { %>
											<span class="issue-flagged"><%= instance.getTitle() %></span><br/>
										<% } else { %>								
											<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
												<span class="issue-title-new"><%= instance.getTitle() %></span><br/>
											<% } else { %>
												<span class="issue-title"><%= instance.getTitle() %></span><br/>
											<% } %>
										<% } %>
										<span class="menu" style="font-weight:bold">
								  	  		<%= loginBean.translate("Created") %> <%= Utils.displayTimestamp(instance.getCreationDate())%> 
											<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %>
								  	  	</span><br/>
										<span class="details">
										<% if (issueTrackerBean.getInstance() == null) { %>
											<%= loginBean.translate("Issue Tracker") %>: <%= instance.getTracker().getNameHTML() %> |
										<% } %>
										<%= loginBean.translate("Type") %>: <%= loginBean.translate(instance.getType().name()) %><br/>
										<%= loginBean.translate("Priority") %>: <%= loginBean.translate(instance.getPriority().name()) %><br/>
										<%= loginBean.translate("Status") %>: <%= loginBean.translate(instance.getStatus().name()) %><br/>
										<%= loginBean.translate("Views") %>: <%= instance.getViews() %></span><br/>
									</a>
								</td>
							</tr>
						</table>
						<% if (!loginBean.isMobile()) { %>
							</span>
						<% } %>
					<% } %>
				</td>
			<% } %>
		</tr>
	</table>
	
	<span class='menu'>
		<% bean.writePagingString(out, instances); %>
	</span>

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