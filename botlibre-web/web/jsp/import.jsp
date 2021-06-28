<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.MemoryBean"%>
<%@page import="org.botlibre.web.bean.MemoryBean.BrowseMode"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.api.knowledge.Vertex"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% MemoryBean memoryBean = loginBean.getBean(MemoryBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Web - <%= Site.NAME %></title>
	<meta name="description" content="The web tab allows you to import data from websites and webservices such as Wiktionary, and WikiData."/>	
	<meta name="keywords" content="web, import, freebase, wikidata, wiktionary, data, knowledge, crawl, scrape, html, xml"/>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
	<script>
	$(document).ready(function() 
		{ 
			$("#firsttable").tablesorter({widgets: ['zebra']});
			$("#results").tablesorter({widgets: ['zebra']});
		}
	);
	</script>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="chatlog-topper-banner" align="left" style="position:relative;z-index:10;">
		<div class="clearfix">
		<div class="toolbar">
			<span class="dropt">
				<div class="menu" style="top:35px;">
					<table>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#inspect-icon').click(); return false;" title="<%= loginBean.translate("Inspect the selected objects") %>" class="menuitem">
								<img src="images/inspect.svg" class="menu"/> <%= loginBean.translate("Inspect") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#relationships-icon').click(); return false;" title="<%= loginBean.translate("Browse all objects that reference the selected objects") %>" class="menuitem">
								<img src="images/relationships.svg" class="menu"/> <%= loginBean.translate("Browse Relationships") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all (or the first 100) objects") %>" class="menuitem">
								<img src="images/select.svg" class="menu"/> <%= loginBean.translate("Select All") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#pin-icon').click(); return false;" title="<%= loginBean.translate("Pin the selected objects, so they cannot be forgotten") %>" class="menuitem">
								<img src="images/pin.svg" class="menu"/> <%= loginBean.translate("Pin") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#unpin-icon').click(); return false;" title="<%= loginBean.translate("Unpin the selected objects, so they can be forgotten") %>" class="menuitem">
								<img src="images/unpin.svg" class="menu"/> <%= loginBean.translate("Unpin") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Caution, this will permanently delete the selected objects or relationships") %>" class="menuitem">
								<img src="images/remove3.svg" class="menu"/> <%= loginBean.translate("Delete") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export the objects to JSON or CSV (spreadsheet)") %>" class="menuitem">
								<img src="images/download.svg" class="menu"/> <%= loginBean.translate("Export and Download") %></a>
							</td>
						</tr>
					</table>
				</div>
				<img class="admin-banner-pic" src="images/menu1.png">
			</span>
			<a href="#" onclick="$('#inspect-icon').click(); return false;" title="<%= loginBean.translate("Inspect the selected objects") %>"><img src="images/inspect.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#relationships-icon').click(); return false;" title="<%= loginBean.translate("Browse all objects that reference the selected objects") %>"><img src="images/relationships.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all (or the first 100) objects") %>"><img src="images/select.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#pin-icon').click(); return false;" title="<%= loginBean.translate("Pin the selected objects, so they cannot be forgotten") %>"><img src="images/pin.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#unpin-icon').click(); return false;" title="<%= loginBean.translate("Unpin the selected objects, so they can be forgotten") %>"><img src="images/unpin.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Caution, this will permanently delete the selected objects or relationships") %>"><img src="images/remove3.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export the objects to JSON or CSV (spreadsheet)") %>"><img src="images/download.svg" class="admin-banner-pic"/></a>
		</div>
		</div>
	</div>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The web tab allows you to import data from websites and webservices such as Wiktionary, and WikiData.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-web.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %> 
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/web.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("The web tab allows you to import data from websites and webservices such as Wiktionary, and WikiData.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Web Import") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<form action="web" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<table>
						<tr>
							<td>Import Source</td>
							<td>
								<select name="web">
								 	<option value="Web" <%= memoryBean.getWeb().equals("Web") ? "selected" : "" %>><%= loginBean.translate("Website") %></option>
									<option value="Wiktionary" <%= memoryBean.getWeb().equals("Wiktionary") ? "selected" : "" %>><%= loginBean.translate("Wiktionary") %></option>
									<option value="WikiData" <%= memoryBean.getWeb().equals("WikiData") ? "selected" : "" %>><%= loginBean.translate("WikiData") %></option>
								</select>
							</td>
						</tr>
						<tr>
							<td>Import Mode</td>
							<td>
								<select name="mode">
								 	<option value="Data" <%= memoryBean.getWebMode().equals("Data") ? "selected" : "" %>><%= loginBean.translate("Data") %></option>
								 	<option value="Text" <%= memoryBean.getWebMode().equals("Text") ? "selected" : "" %>><%= loginBean.translate("Text") %></option>
								 	<option value="HTML" <%= memoryBean.getWebMode().equals("HTML") ? "selected" : "" %>><%= loginBean.translate("HTML") %></option>
									<option value="Headers" <%= memoryBean.getWebMode().equals("Headers") ? "selected" : "" %>><%= loginBean.translate("Learn Headers") %></option>
									<option value="Reflexive" <%= memoryBean.getWebMode().equals("Reflexive") ? "selected" : "" %>><%= loginBean.translate("Reflexive Learning") %></option>
								</select>
							</td>
						</tr>
					</table>
					<%= loginBean.translate("Web URL or Keyword") %>
					<br/>
					<input type="text" name="input" value="<%= memoryBean.getWebInput() %>"/>
					<br/>
					<%= loginBean.translate("XPath (optional)") %>
					<br/>
					<input type="text" name="xpath" value="<%= memoryBean.getWebXPath() %>"/>
					<br/>
					<input type="submit" name="submit" value="<%= loginBean.translate("Import") %>" title="<%= loginBean.translate("Import data from the web URL, or search the web services for the keywords.") %>"/>
					<br/>
				</form>
				<% try { %>
					<% if (memoryBean.getResults() != null) { %>
						<br/>
						<form action="memory" method="get" class="message">
							<%= loginBean.postTokenInput() %>
							<%= botBean.instanceInput() %>
							<div class="toolbar" style="display:none;">

								<input id="inspect-icon" class="icon" type="submit" name="browse" value="">
								<input id="relationships-icon" class="icon" type="submit" name="references" value="">
								<input id="select-icon" class="icon" type="submit" name="select-all" value="">
								<input id="pin-icon" class="icon" type="submit" name="pin" value="">
								<input id="unpin-icon" class="icon" type="submit" name="unpin" value="">
								<input id="remove-icon" class="icon" type="submit" name="delete" value="">
								<input id="export-icon" class="icon" type="submit" name="export" value="">
							</div>
							<span class = menu>
								<%= memoryBean.getResultsSize() %> results.<br/>
								<%= memoryBean.pagingString() %>
							</span>
							<% List results = memoryBean.getResults(); %>
							<% if (!results.isEmpty()) { %>
								<table id="results" class="tablesorter">
									<% if (results.get(0) instanceof Vertex) { %>
										<thead>
											<tr>
												<th></th>
												<th><%= loginBean.translate("Id") %></th>
												<th><%= loginBean.translate("Name") %></th>
												<th><%= loginBean.translate("Type") %></th>
												<th><%= loginBean.translate("Data") %></th>
												<th><%= loginBean.translate("Pinned") %></th>
												<th><%= loginBean.translate("Dirty") %></th>
												<th><%= loginBean.translate("Response") %></th>
												<th><%= loginBean.translate("Access count") %></th>
												<th><%= loginBean.translate("Access date") %></th>
												<th><%= loginBean.translate("Creation date") %></th>
											</tr>
										</thead>
					 				<% } %>
									<tbody>
									<% int count = 0; %>
									<% for (Object result : results) { %>
										<tr>
										<% if (result instanceof Vertex) { %>
											<% Vertex vertex = (Vertex)result; %>
											<td><input type="checkbox" name="<%= "v-" + vertex.getId() %>" <%= ((count < 100) && memoryBean.isSelectAll()) ? "checked" : "" %>></td>
											<td><%= vertex.getId() %></td>
											<td><%= (vertex.getName() == null) ? "" : Utils.escapeHTML(vertex.getName()) %></td>
											<td><%= (vertex.getDataType() == null) ? "" : vertex.getDataType() %></td>
											<td><%= (vertex.getDataValue() == null) ? "" : Utils.escapeHTML(vertex.getDataValue()) %></td>
											<td><%= vertex.isPinned() %></td>
											<td><%= vertex.isDirty() %></td>
											<td><%= vertex.hasAnyResponseRelationship() %></td>
											<td><%= vertex.getAccessCount() %></td>
											<td><%= Utils.printDate(vertex.getAccessDate()) %></td>
											<td><%= Utils.printDate(vertex.getCreationDate()) %></td>
						 				<% } else { %>
											<% for (Object element : (Object[])result) { %>
												<% if (element instanceof Vertex) { %>
						 							<td>
													<% Vertex vertex = (Vertex) element; %>
													<input type="checkbox" name="<%= "v-" + vertex.getId() %>" <%= ((count < 100) && memoryBean.isSelectAll()) ? "checked" : "" %>>
													<%= Utils.escapeHTML(vertex.displayString()) %>
							 						</input>
					 							<% } else if (element instanceof Date) { %>
						 							<td nowrap>
					 								<%= Utils.printDate((Date)element) %>
				 								<% } else { %>
						 							<td nowrap>
					 								<%= element %>
					 							<% } %>
						 						</td>
					 						<% } %>
						 				<% } %>
						 				</tr>
										<% count++; %>
									<% } %>
									</tbody>
								</table>
							<% } %>
						</form>
					<% } %>
				<% } catch (Exception exception) { AdminDatabase.instance().log(exception); } %>
				
				<pre id="log"><code><%= botBean.getLog() %></code></pre>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
