<%@page import="java.io.StringWriter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.Tag"%>
<%@page import="org.botlibre.web.bean.ForumBean"%>
<%@page import="org.botlibre.web.forum.Forum"%>
<%@page import="org.botlibre.web.bean.ForumPostBean"%>
<%@page import="org.botlibre.web.forum.ForumPost"%>
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
	ForumBean forumBean = loginBean.getBean(ForumBean.class);
	ForumPostBean forumPostBean = loginBean.getBean(ForumPostBean.class);
	forumPostBean.setForumBean(forumBean);
	String title = "Forum";
	String forum = "";
	if (forumBean.getInstance() != null) {
		title = forumBean.getInstance().getName();
		forum = "&forum=" + forumBean.getInstanceId();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<% forumBean.writeHeadMetaHTML(out); %>
	<jsp:include page="head.jsp"/>
	<% forumBean.writeHeadMetaHTML(out); %>
	<title><%= title %> Posts<%= embed ? "" : " - " + Site.NAME %></title>
	<%= loginBean.getJQueryHeader() %>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
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
	<% forumBean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="about">
<% } %>
<jsp:include page="error.jsp"/>
<% if (!forumBean.isValidUser() || (embed && (forumBean.getInstance() == null))) { %>
	<%= loginBean.translate("This user does not have access to this forum.") %>
<% } else { %>
	<% if (embed && !Site.COMMERCIAL && loginBean.getShowLink()) { %>
		<div id="microtopper" align=right style="background-color: <%= loginBean.getBackgroundColor() %>">
		 	<span>forum hosted by <a href="http://<%= Site.URL %>" target="_blank"><%= Site.NAME %></a></span>
		</div>
	<% } %>
	
	<% forumBean.writeAd(out); %>
	
	<% List<ForumPost> instances = new ArrayList<ForumPost>(); %>
	<% if (forumPostBean.isDefaults()) { %>
		<% instances = forumPostBean.getAllFeaturedInstances(); %>
	<% } %>
	<% int count = 0; %>
	<% int row = 0; %>
	<% String width = loginBean.isMobile() ? "300" : "800"; %>
	<% if (!instances.isEmpty()) { %>
		<h3><%= loginBean.translate("Featured") %></h3>
			<form action="forum-post" method="get" class="message">
			<%= proxy.proxyInput() %>
			<%= forumBean.forumInput() %>
			<table cellspacing="5">
				<tr>
					<% for (ForumPost instance : instances) {%>
						<% count++; %>
						<% row++; %>
						<% if (row >= 2) { %>
							</tr>
							<tr>
							<% row = 1; %>
						<% } %>
						<td align="left" valign="top" class="user-thumb">
							<a style="text-decoration:none;" href="<%= "login?view-user=" + forumBean.encodeURI(instance.getCreator().getUserId()) + proxy.proxyString() %>">
								<img src="<%= forumPostBean.getAvatarThumb(instance) %>" class="user-thumb"/>
							</a>
						</td>
						<td valign="top" >
						 	<% if (!loginBean.isMobile()) { %>
								<span class="dropt">
								<div style="border-width:0px;padding:0;max-width:none">
									<table class="forum-box2">
										<tr>
											<td class="forum-article">
												<a style="text-decoration:none;" href="<%= "forum-post?id=" + instance.getId() + proxy.proxyString() %>">
													<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
														<span class="forum-featured-new"><%= instance.getTopic() %></span><br/>
													<% } else { %>
														<span class="forum-featured"><%= instance.getTopic() %></span><br/>
													<% } %>
													<span class="menu" style="font-weight:bold">
														<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %> 
											  	  		<%= loginBean.translate("posted") %> <%= Utils.displayTimestamp(instance.getCreationDate())%>
											  	  	</span><br/>
													<% if (instance.isFlagged()) { %>
														<span style="color:red;font-weight:bold;"><%= loginBean.translate("This post is flagged.")%></span><br/>
													<% } else {%>
														<div id="forum-summary" class="forum-summary">
															<%= instance.getSummary() %>
														</div>
													<% } %>
													<span class="details">
													<% if (!instance.getTags().isEmpty()) { %>
														<%= loginBean.translate("Tags") %>: <%= instance.getTagsString() %><br/>
													<% } %>
													<% if (forumBean.getInstance() == null) { %>
														<%= loginBean.translate("Forum") %>: <%= instance.getForum().getNameHTML() %><br/>
													<% } %>
													<% if (instance.getUpdatedDate() != null) { %>
														<%= loginBean.translate("Updated") %>: <%= Utils.displayTimestamp(instance.getUpdatedDate()) %><br/>
													<% } %>
													<%= loginBean.translate("Replies") %>: <%= instance.getReplyCount() %>, 
													<%= loginBean.translate("Views") %>: <%= instance.getViews() %>, <%= loginBean.translate("today") %>: <%= instance.getDailyViews() %>, <%= loginBean.translate("week") %>: <%= instance.getWeeklyViews() %>, <%= loginBean.translate("month") %>: <%= instance.getMonthlyViews() %><br/>
													<%= loginBean.translate("Thumbs up") %>: <%= instance.getThumbsUp() %>, <%= loginBean.translate("thumbs down") %>: <%= instance.getThumbsDown() %>, <%= loginBean.translate("stars") %>: <%= Utils.truncate(instance.getStars()) %><br/>
													</span>
												</a>
											</td>
										</tr>
									</table>
								</div>
							<% } %>				
							<table class="forum-box">
								<tr><td class="forum-article">
										<a style="text-decoration:none;" href="<%= "forum-post?id=" + instance.getId() + proxy.proxyString() %>">
											<% if (instance.isFlagged()) { %>
												<span class="forum-flagged"><%= instance.getTopic() %></span><br/>
											<% } else { %>
												<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
													<span class="forum-featured-new"><%= instance.getTopic() %></span><br/>
												<% } else { %>
													<span class="forum-featured"><%= instance.getTopic() %></span><br/>
												<% } %>
											<% } %>
											<span class="menu" style="font-weight:bold">
												<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %> 
									  	  		<%= loginBean.translate("posted") %> <%= Utils.displayTimestamp(instance.getCreationDate())%>
									  	  	</span><br/>
											<span class="details">
											<% if (forumBean.getInstance() == null) { %>
												<%= loginBean.translate("Forum") %>: <%= instance.getForum().getNameHTML() %> |
											<% } %>
											<%= loginBean.translate("Replies") %>: <%= instance.getReplyCount() %> | 
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
	<% if (forumBean.getInstance() != null) { %>
		<form action="forum-post" method="post" class="message">
			<%= proxy.proxyInput() %>
			<%= forumBean.forumInput() %>
			<% if (forumBean.getInstance().isPostAllowed(loginBean.getUser())) { %>
				<input name="create-post" type="submit" value="<%= loginBean.translate("New Post") %>"/>
			<% } else { %>
				<input id="disabled" disabled="disabled" title="You do not have access to post" name="create-post" type="submit" value="<%= loginBean.translate("New Post") %>"/>
			<% } %>
			<input name="show-details" type="submit" value="<%= loginBean.translate("Details") %>"/>
		</form>
	<% } %>
	<h3><%= loginBean.translate("Browse") %></h3>
	<form action="forum-post" method="get" class="search">
		<%= proxy.proxyInput() %>
		<%= forumBean.forumInput() %>
		<span class="menu">
			<input type="radio" name="instance-filter" <%= forumPostBean.getInstanceFilterCheckedString(InstanceFilter.Public) %> value="public" onClick="this.form.submit()"><%= loginBean.translate("all posts") %></input>
			<input type="radio" name="instance-filter" <%= forumPostBean.getInstanceFilterCheckedString(InstanceFilter.Personal) %> value="personal" onClick="this.form.submit()"><%= loginBean.translate("my posts") %></input>
			<br/>
			<div class="search-div">
				<span class="search-span"><%= loginBean.translate("Topic") %></span>
				<input id="searchtext" name="name-filter" type="text" value="<%= forumPostBean.getNameFilter() %>" />
			</div>
			<% if (forumBean.getInstance() == null) { %>
				<div class="search-div">
					<span class="search-span"><%= loginBean.translate("Categories") %></span>
					<input id="categories" name="category-filter" type="text" value="<%= forumPostBean.getCategoryFilter() %>"
									title="Filter by a comma seperated list of category names" />
					<script>
						$( "#categories" ).autocomplete({
						source: [<%= forumBean.getAllCategoriesString() %>],
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
				<input id="tags" name="tag-filter" type="text" value="<%= forumPostBean.getTagFilter() %>"
						title="Filter by a comma seperated list of tag names" />
				<script>
					$( "#tags" ).autocomplete({
					source: [<%= forumPostBean.getAllTagsString() %>],
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
					<option value="header" <%= forumPostBean.getDisplayCheckedString(DisplayOption.Header) %>><%= loginBean.translate("Topic") %></option>
					<option value="details" <%= forumPostBean.getDisplayCheckedString(DisplayOption.Details) %>><%= loginBean.translate("Details") %></option>
				</select>
			</div>
			<div class="search-div">
				<span class="search-span"><%= loginBean.translate("Sort") %></span>
				<select name="instance-sort" onchange="this.form.submit()">
					<option value="Name" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.Name) %>><%= loginBean.translate("name") %></option>
					<option value="Date" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.Date) %>><%= loginBean.translate("date") %></option>
					<option value="ThumbsUp" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.ThumbsUp) %>><%= loginBean.translate("thumbs up") %></option>
					<option value="ThumbsDown" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.ThumbsDown) %>><%= loginBean.translate("thumbs down") %></option>
					<option value="Stars" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.Stars) %>><%= loginBean.translate("stars") %></option>
					<option value="Connects" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.Connects) %>><%= loginBean.translate("views") %></option>
					<option value="DailyConnects" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.DailyConnects) %>><%= loginBean.translate("views today") %></option>
					<option value="WeeklyConnects" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.WeeklyConnects) %>><%= loginBean.translate("views this week") %></option>
					<option value="MonthlyConnects" <%= forumPostBean.getInstanceSortCheckedString(InstanceSort.MonthlyConnects) %>><%= loginBean.translate("views this month") %></option>
				</select>
			</div>
			<input style="display:none;position:absolute;" type="submit" name="search" value="Search">
		</span>
		<br/>
	</form>
	
	<% instances = forumPostBean.getAllInstances(); %>
	<span class='menu'>
		<%= forumPostBean.getResultsSize() %> results.<br/>
		<% forumPostBean.writePagingString(out, instances); %>
	</span>
	
	<table cellspacing="5">
		<tr>
			<% count = 0; %>
			<% row = 0; %>
			<% for (ForumPost instance : instances) {%>
				<% count++; %>
				<% row++; %>
				<% if (row >= 2) { %>
					</tr>
					<tr>
					<% row = 1; %>
				<% } %>
				<td align="left" valign="top" class="user-thumb">
					<a style="text-decoration:none;" href="<%= "login?view-user=" + forumBean.encodeURI(instance.getCreator().getUserId()) + proxy.proxyString() %>">
						<img src="<%= forumPostBean.getAvatarThumb(instance) %>" class="user-thumb"/>
					</a>
				</td>
				<td valign="top" >
					<% boolean showDetails = forumPostBean.getDisplayOption() == DisplayOption.Details; %>
					<% if (showDetails || !loginBean.isMobile()) { %>
						<% if (!showDetails) { %>
							<span class="dropt">
							<div style="border-width:0px;padding:0;max-width:none">
							<table class="forum-box2">
						<% } else { %>
							<table class="forum-box">
						<% } %>
						<tr>
							<td class="forum-article">
								<a style="text-decoration:none;" href="<%= "forum-post?id=" + instance.getId() + proxy.proxyString() %>">
									<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
										<span class="forum-topic-new"><%= instance.getTopic() %></span><br/>
									<% } else { %>
										<span class="forum-topic"><%= instance.getTopic() %></span><br/>
									<% } %>
									<span class="menu" style="font-weight:bold">
										<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %> 
							  	  		<%= loginBean.translate("posted") %> <%= Utils.displayTimestamp(instance.getCreationDate())%>
							  	  	</span><br/>
									<% if (instance.isFlagged()) { %>
										<span style="color:red;font-weight:bold;"><%= loginBean.translate("This post is flagged.") %></span><br/>
									<% } else { %>
										<div id="forum-summary" class="forum-summary">
											<%= instance.getSummary() %>
										</div>
									<% } %>
									<span class="details">
									<% if (!instance.getTags().isEmpty()) { %>
										<%= loginBean.translate("Tags") %>: <%= instance.getTagsString() %><br/>
									<% } %>
									<% if (forumBean.getInstance() == null) { %>
										<%= loginBean.translate("Forum") %>: <%= instance.getForum().getNameHTML() %><br/>
									<% } %>
									<% if (instance.getUpdatedDate() != null) { %>
										<%= loginBean.translate("Updated") %>: <%= Utils.displayTimestamp(instance.getUpdatedDate()) %><br/>
									<% } %>
									<%= loginBean.translate("Replies") %>: <%= instance.getReplyCount() %>, 
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
						<table class="forum-box">
							<tr>
								<td class="forum-article">
									<a style="text-decoration:none;" href="<%= "forum-post?id=" + instance.getId() + proxy.proxyString() %>">
										<% if (instance.isFlagged()) { %>
											<span class="forum-flagged"><%= instance.getTopic() %></span><br/>
										<% } else { %>								
											<% if ((loginBean.getUser() == null) || instance.getCreationDate().after(loginBean.getUser().getOldLastConnected())) { %>
												<span class="forum-topic-new"><%= instance.getTopic() %></span><br/>
											<% } else { %>
												<span class="forum-topic"><%= instance.getTopic() %></span><br/>
											<% } %>
										<% } %>
										<span class="menu" style="font-weight:bold">
											<%= loginBean.translate("by") %> <%= instance.getCreator().getUserHTML() %> 
								  	  		<%= loginBean.translate("posted") %> <%= Utils.displayTimestamp(instance.getCreationDate())%>
								  	  	</span><br/>
										<span class="details">
										<% if (forumBean.getInstance() == null) { %>
											<%= loginBean.translate("Forum") %>: <%= instance.getForum().getNameHTML() %> |
										<% } %>
										<%= loginBean.translate("Replies") %>: <%= instance.getReplyCount() %> | 
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
		<% forumPostBean.writePagingString(out, instances); %>
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