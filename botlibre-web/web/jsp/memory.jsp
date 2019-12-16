<%@page import="java.util.List"%>
<%@page import="java.util.Date"%>
<%@page import="org.botlibre.web.bean.MemoryBean.BrowseMode"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.MemoryBean"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.api.knowledge.Vertex" %>
<%@page import="org.botlibre.api.knowledge.Relationship"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.Collection"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% MemoryBean bean = loginBean.getBean(MemoryBean.class); %>

<!DOCTYPE HTML>
<html style="background-color: #fff;">
<head>
	<jsp:include page="head.jsp"/>
	<title>Knowledge - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("The knowledge tab allows you to query, view, and edit the knowledge in your bot's knowledgebase") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("knowledge, data, database, objects, object oriented, network, bot, brain, query, search") %>"/>
	<% if (!loginBean.isMobile() && bean.getMode() == BrowseMode.Worksheet) { %>
		<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
	<% } %>
	<%= loginBean.getJQueryHeader() %>
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
	<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
	<script>
	$(function() {
		$( "#dialog-delete-all" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#delete-all" ).click(function() {
			$( "#dialog-delete-all" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-delete-all" ).click(function() {
			$( "#dialog-delete-all" ).dialog( "close" );
			return false;
		});
		$( "#dialog-import" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#import-icon" ).click(function() {
			$( "#dialog-import" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-import" ).click(function() {
			$( "#dialog-import" ).dialog( "close" );
			return false;
		});
		
		$( "#dialog-export" ).dialog({
			autoOpen: false,
			modal: true
		});
		
		$( "#export-icon" ).click(function() {
			$( "#dialog-export" ).dialog( "open" );
			return false;
		});
		
		$( "#cancel-export" ).click(function() {
			$( "#dialog-export" ).dialog( "close" );
			return false;
		});
		
		$( "#export" ).click(function() {
			$( "#dialog-export" ).dialog( "close" );
			return true;
		});
	});
	</script>
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
		<% if ((bean.getResults() != null) && ((bean.getMode() == BrowseMode.Selection) || (bean.getMode() == BrowseMode.Search) || (bean.getMode() == BrowseMode.Reports) || (bean.getMode() == BrowseMode.Worksheet))) { %>
			<span class="dropt">
				<div class="menu" style="top:35px;">
					<table>
						<tr class="menuitem">
							<td><a href="memory?search=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Browse") %>" class="menuitem">
								<img src="images/home_black.svg" class="menu"/> <%= loginBean.translate("Browse") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="memory?reports=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Process reports and tasks on the bot's knowledgebase") %>" class="menuitem">
								<img src="images/stats.svg" class="menu"/> <%= loginBean.translate("Reports & Tasks") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="memory?worksheet=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Execute Self code to modify the knowledgebase") %>" class="menuitem">
								<img src="images/script1.png" class="menu"/> <%= loginBean.translate("Worksheet") %></a>
							</td>
						</tr>
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
							<td><a href="#" onclick="$('#graph-icon').click(); return false;" title="<%= loginBean.translate("View a graph of the selected objects") %>" class="menuitem">
								<img src="images/relationship_graph.svg" class="menu"/> <%= loginBean.translate("Graph Relationships") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all (or the first 100) objects") %>" class="menuitem">
								<img src="images/select.svg" class="menu"/> <%= loginBean.translate("Select all") %></a>
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
								<img src="images/download.svg" class="menu"/> <%= loginBean.translate("Export and download") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Import objects from JSON, CSV, Set, Map, or Properties") %>" class="menuitem">
								<img src="images/upload.svg" class="menu"/> <%= loginBean.translate("Upload and import") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import objects from a JSON, CSV, Set, Map, or Properties script in the script library") %>" class="menuitem">
								<img src="images/import.svg" class="menu"/> <%= loginBean.translate("Import from library") %></a>
							</td>
						</tr>
					</table>
				</div>
				<img class="admin-banner-pic" src="images/menu1.png">
			</span>
			<a href="memory?search=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Browse") %>"><img src="images/home_black.svg" class="admin-banner-pic"/></a>
			<a href="memory?reports=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Process reports and tasks on the bot's knowledgebase") %>"><img src="images/stats.svg" class="admin-banner-pic"/></a>
			<a href="memory?worksheet=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Execute Self code to modify the knowledgebase") %>"><img src="images/script1.png" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#inspect-icon').click(); return false;" title="<%= loginBean.translate("Inspect the selected objects") %>"><img src="images/inspect.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#relationships-icon').click(); return false;" title="<%= loginBean.translate("Browse all objects that reference the selected objects") %>"><img src="images/relationships.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#graph-icon').click(); return false;" title="<%= loginBean.translate("View graph of the selected objects") %>"><img src="images/relationship_graph.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#select-icon').click(); return false;" title="<%= loginBean.translate("Select all (or the first 100) objects") %>"><img src="images/select.svg" class="admin-banner-pic"/></a>
			<a href="#" class="shrinkhide500" onclick="$('#pin-icon').click(); return false;" title="<%= loginBean.translate("Pin the selected objects, so they cannot be forgotten") %>"><img src="images/pin.svg" class="admin-banner-pic"/></a>
			<a href="#" class="shrinkhide500" onclick="$('#unpin-icon').click(); return false;" title="<%= loginBean.translate("Unpin the selected objects, so they can be forgotten") %>"><img src="images/unpin.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#remove-icon').click(); return false;" title="<%= loginBean.translate("Caution, this will permanently delete the selected objects or relationships") %>"><img src="images/remove3.svg" class="admin-banner-pic"/></a>
			<a href="#" class="shrinkhide500" onclick="$('#export-icon').click(); return false;" title="<%= loginBean.translate("Export the objects to JSON or CSV (spreadsheet)") %>"><img src="images/download.svg" class="admin-banner-pic"/></a>
			<a href="#" class="shrinkhide500" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Import objects from JSON, CSV, Set, Map, or Properties") %>"><img src="images/upload.svg" class="admin-banner-pic"/></a>
			<a href="#" class="shrinkhide500" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import objects from a JSON, CSV, , Set, Map, or Properties script in the script library") %>"><img src="images/import.svg" class="admin-banner-pic"/></a>
		<% } else { %>
			<span class="dropt">
				<div class="menu" style="top:35px;">
					<table>
						<tr class="menuitem">
							<td><a href="memory?search=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Browse") %>" class="menuitem">
								<img src="images/home_black.svg" class="menu"/> <%= loginBean.translate("Browse") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="memory?reports=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Process reports and tasks on the bot's knowledgebase") %>" class="menuitem">
								<img src="images/stats.svg" class="menu"/> <%= loginBean.translate("Reports & Tasks") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="memory?worksheet=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Execute Self code to modify the knowledgebase") %>" class="menuitem">
								<img src="images/script1.png" class="menu"/> <%= loginBean.translate("Worksheet") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Import objects from JSON, Set, Map, or Properties file") %>" class="menuitem">
								<img src="images/upload.png" class="menu"/> <%= loginBean.translate("Upload and import") %></a>
							</td>
						</tr>
						<tr class="menuitem">
							<td><a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import objects from a JSON, CSV, Set, Map, or Properties script in the script library") %>" class="menuitem">
								<img src="images/import.png" class="menu"/> <%= loginBean.translate("Import from library") %></a>
							</td>
						</tr>
					</table>
				</div>
				<img class="admin-banner-pic" src="images/menu1.png">
			</span>
			<a href="memory?search=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Browse") %>"><img src="images/home_black.svg" class="admin-banner-pic"/></a>
			<a href="memory?reports=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Process reports and tasks on the bot's knowledgebase") %>"><img src="images/stats.svg" class="admin-banner-pic"/></a>
			<a href="memory?worksheet=true&instance=<%= botBean.getInstanceId() %>" title="<%= loginBean.translate("Execute Self code to modify the knowledgebase") %>"><img src="images/script1.png" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#import-icon').click(); return false;" title="<%= loginBean.translate("Import objects from JSON, CSV, Set, Map, or Properties file") %>"><img src="images/upload.svg" class="admin-banner-pic"/></a>
			<a href="#" onclick="$('#importlib-icon').click(); return false;" title="<%= loginBean.translate("Import objects from a JSON, CSV, Set, Map, or Properties script in the script library") %>"><img src="images/import.svg" class="admin-banner-pic"/></a>
			<form action="memory" method="get" class="message" style="display:none;">
				<input id="import-icon" class="icon" type="submit" name="import"/>
				<input id="importlib-icon" class="icon" type="submit" name="importlib"/>
			</form>
		<% } %>
		</div>
		</div>
	</div>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The knowledge tab allows you to query, view, and edit the knowledge in your bot's knowledgebase.") %><br/>
					<% if (!Site.DEDICATED) { %>
						<%= loginBean.translate("See ") %><a target="_blank" href="https://www.botlibre.com/forum-post?id=705860" class="blue"><%= loginBean.translate("Self scripting syntax") %></a>
					<% } %>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-knowledge.jsp"><%= loginBean.translate("Docs") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/knowledge.png" class="admin-banner-pic" style="vertical-align:middle">
					<div>
						<p class="help">
							<%= loginBean.translate("Browse your bot's knowledge database.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Knowledge") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<% if (bean.getMode() == BrowseMode.Search) { %>
					<h3><%= loginBean.translate("Search") %></h3>
					<form action="memory" method="get" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<span class="menu">
							<div class='search-div'>
								<span class='search-span'><%= loginBean.translate("Filter") %></span>
								<input autofocus class="search" type="text" name="input" value="<%= bean.getInput() %>"
										title="<%= loginBean.translate("Enter your knowledge data query string, use * as a wildcard") %>" />
							</div>
							<div class='search-div'>
								<span class='search-span'><%= loginBean.translate("Type") %></span>
								<select class="search" name="type" title="<%= loginBean.translate("Filter the results by the type of the data") %>">
									<option value=""></option>
									<option value="String" <%= bean.getType().equals("String") ? "selected" : "" %>>String</option>
									<option value="Primitive" <%= bean.getType().equals("Primitive") ? "selected" : "" %>>Primitive</option>
									<option value="Meta" <%= bean.getType().equals("Meta") ? "selected" : "" %>>Meta</option>
									<option value="Date" <%= bean.getType().equals("Date") ? "selected" : "" %>>Date</option>
									<option value="Time" <%= bean.getType().equals("Time") ? "selected" : "" %>>Time</option>
									<option value="Timestamp" <%= bean.getType().equals("Timestamp") ? "selected" : "" %>>Timestamp</option>
									<option value="Text" <%= bean.getType().equals("Text") ? "selected" : "" %>>Text</option>
									<option value="Binary" <%= bean.getType().equals("Binary") ? "selected" : "" %>>Binary</option>
									<option value="java.net.URI" <%= bean.getType().equals("java.net.URI") ? "selected" : "" %>>URL</option>
									<option value="java.lang.Boolean" <%= bean.getType().equals("java.lang.Boolean") ? "selected" : "" %>>Boolean</option>
									<option value="java.lang.Integer" <%= bean.getType().equals("java.lang.Integer") ? "selected" : "" %>>Integer</option>
									<option value="java.lang.Long" <%= bean.getType().equals("java.lang.Long") ? "selected" : "" %>>Long</option>
									<option value="java.lang.Double" <%= bean.getType().equals("java.lang.Double") ? "selected" : "" %>>Double</option>
									<option value="java.math.BigInteger" <%= bean.getType().equals("java.math.BigInteger") ? "selected" : "" %>>BigInteger</option>
									<option value="java.math.BigDecimal" <%= bean.getType().equals("java.math.BigDecimal") ? "selected" : "" %>>BigDecimal</option>
								</select>
							</div>
							<div class='search-div'>
								<span class='search-span'><%= loginBean.translate("Class") %></span>
								<input id="classification" class="search" name="classification" type="text" value="<%= bean.getClassification() %>" title="<%= loginBean.translate("Filter the results by the classification of the data") %>">
								<script>
									$( '#classification' ).autocomplete({
										source: ['sentence', 'paragraph', 'fragment', 'tweet', 'email', 'input', 'conversation', 'speaker', 'word', 'compound-word', 'thing',
												 'action', 'description', 'noun', 'pronoun', 'verb', 'adjective', 'article', 'adjective', 'determiner', 'interjection',
												 'punctuation', 'question', 'name', 'numeral', 'number', 'context', 'list', 'variable', 'relationship', 'classification',
												 'keyword', 'topic', 'pattern', 'formula', 'expression', 'equation', 'state', 'case'
										], minLength: 0 }).on('focus', function(event) { var self = this; $(self).autocomplete('search', ''); });
								</script>
							</div>
							<div class='search-div'>
								<input class="search" type="checkbox" name="pinned" <%= bean.isPinned() ? "checked" : "" %> title="<%= loginBean.translate("Filter only pinned data") %>"><%= loginBean.translate("Pinned") %></input>
							</div>
							<br/>
							<div class='search-div'>
								<span class='search-span'><%= loginBean.translate("Sort") %></span>
								<select class="search" name="sort">
									<option value="dataValue" <%= bean.getSort().equals("dataValue") ? "selected" : "" %>><%= loginBean.translate("Value") %></option>
									<option value="name" <%= bean.getSort().equals("name") ? "selected" : "" %>><%= loginBean.translate("Name") %></option>
									<option value="dataType" <%= bean.getSort().equals("dataType") ? "selected" : "" %>><%= loginBean.translate("Type") %></option>
									<option value="creationDate" <%= bean.getSort().equals("creationDate") ? "selected" : "" %>><%= loginBean.translate("Creation Date") %></option>
									<option value="accessDate" <%= bean.getSort().equals("accessDate") ? "selected" : "" %>><%= loginBean.translate("Access Date") %></option>
									<option value="accessCount" <%= bean.getSort().equals("accessCount") ? "selected" : "" %>><%= loginBean.translate("Access Count") %></option>
								</select>
							</div>
							<div class='search-div'>
								<span class='search-span'><%= loginBean.translate("Order") %></span>
								<select class="search" name="order">
									<option value="asc" <%= bean.getOrder().equals("asc") ? "selected" : "" %>><%= loginBean.translate("Ascending") %></option>
									<option value="desc" <%= bean.getOrder().equals("desc") ? "selected" : "" %>><%= loginBean.translate("Descending") %></option>
								</select>
							</div>
						</span>
						<br/>
						<input id="ok" type="submit" name="query" value="<%= loginBean.translate("Query") %>"/>
						<% if (bean.getResultsSize() == 0) { %>
							<br/><br/>
							<h3><%= loginBean.translate("Status") %></h3>
							<%= loginBean.translate("Total objects") %>: <code><%= String.format("%,d", botBean.getInstanceSize()) %></code>
							<br/>
							<%= loginBean.translate("Max objects") %>: <code><%= String.format("%,d", botBean.getInstance().getMemoryLimit()) %></code> 
							<a href="bot?edit-instance=true&instance=<%= botBean.getInstanceId() %>"><%= loginBean.translate("increase knowledge limit") %></a>
							<br/>
							<%= loginBean.translate("Remaining objects") %>: <code><%= String.format("%,d", (botBean.getInstance().getMemoryLimit() - botBean.getInstanceSize())) %></code>
							<br/>
							(<%= loginBean.translate("memory is automatically cleaned nightly") %>)
							<% if (bean.getMode() == null && botBean.isSuper()) { %>
								<!-- br/ -->
								<!-- input type="submit" name="migrate" value="Migrate"/ -->
							<% } %>
							</p>
							<input id="delete-all" class="delete" type="submit" name="delete-all" value="<%= loginBean.translate("Delete All") %>" title="<%= loginBean.translate("Caution, this will permanently delete everything from the bot's memory and bootstrap it with minimal knowledge") %>"/>
							<input type="submit" name="clear-cache" value="<%= loginBean.translate("Clear Cache") %>" title="<%= loginBean.translate("This will clear your bots shared server-side cache. This can sometimes resolve issues your bot is having. This may affect any connected users.") %>"/>
						<% } %>
					</form>
					<br/>
				<% } else if (bean.getMode() == BrowseMode.Reports) { %>
					<h3><%= loginBean.translate("Reports and Tasks") %></h3>
					<form action="memory" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<table>
						<tr><td nowrap><%= loginBean.translate("Report") %></td>
						<td>
						<select style="width:220px" name="report" onchange="this.form.submit()" title="<%= loginBean.translate("Execute a predefined report") %>">
							<option value=""></option>
							<option value="unreferenced"><%= loginBean.translate("Find unreferenced objects") %></option>
							<option value="unreferenced-data"><%= loginBean.translate("Find unreferenced data objects") %></option>
							<option value="unreferenced-pinned"><%= loginBean.translate("Find unreferenced pinned objects") %></option>
							<option value="old-data"><%= loginBean.translate("Find old conversations") %></option>
							<option value="least-referenced"><%= loginBean.translate("Find least referenced objects") %></option>
							<option value="most-relationships"><%= loginBean.translate("Find objects with the most relationships") %></option>
						</select>
						</td></tr>
						<tr><td nowrap><%= loginBean.translate("Run Task") %></td>
						<td>
						<select style="width:220px" name="task" onchange="this.form.submit()" title="<%= loginBean.translate("Execute a predefined administrative task") %>">
							<option value=""></option>
							<option value="delete-unreferenced"><%= loginBean.translate("Delete unreferenced objects") %></option>
							<option value="delete-unreferenced-data"><%= loginBean.translate("Delete unreferenced data objects") %></option>
							<!--option value="delete-unreferenced-pinned">Delete unreferenced pinned objects</option-->
							<option value="delete-old-data"><%= loginBean.translate("Delete old conversations") %></option>
							<option value="delete-grammar"><%= loginBean.translate("Delete grammar data") %></option>
							<option value="fix-responses"><%= loginBean.translate("Fix corrupt responses") %></option>
							<option value="fix-relationships"><%= loginBean.translate("Fix corrupt relationships") %></option>
							<option value="forget"><%= loginBean.translate("Run forgetfullness") %></option>
						</select>
						</td></tr>
						</table>
					</form>
				<% } else if (bean.getMode() == BrowseMode.Selection) { %>
					<h3><%= loginBean.translate("Selection") %></h3>
					<form action="memory" method="get" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<div class="toolbar" style="display:none;">
							<input id="inspect-icon" class="icon" type="submit" name="browse" value="">
							<input id="relationships-icon" class="icon" type="submit" name="references" value="">
							<input id="graph-icon" class="icon" type="submit" name="graph" value="">
							<input id="select-icon" class="icon" type="submit" name="select-all" value="">
							<input id="pin-icon" class="icon" type="submit" name="pin" value="">
							<input id="unpin-icon" class="icon" type="submit" name="unpin" value="">
							<input id="remove-icon" class="icon" type="submit" name="delete" value="") %>">
							<input id="export-icon" class="icon" type="submit" name="export" value="">
							<input id="import-icon" class="icon" type="submit" name="import" value="">
							<input id="importlib-icon" class="icon" type="submit" name="importlib" value="">
						</div>
						<% for (Vertex vertex : bean.getSelection()) { %>
							<h4><input type="checkbox" name="<%= "v-" + vertex.getId() %>" <%= bean.isSelectAll() ? "checked" : "" %>><%= Utils.escapeHTML(vertex.displayString()) %></h4>
							<table class="tablesorter">
								<thead>
									<tr>
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
										<th><%= loginBean.translate("Consciousness level") %></th>
										<th><%= loginBean.translate("Total Relationships") %></th>
									</tr>
								</thead>
								<tbody>
									<tr>
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
										<td><%= vertex.getConsciousnessLevel() %></td>
										<td><%= vertex.totalRelationships() %><%= vertex.getAllRelationships().size() == vertex.totalRelationships() ? "" : " (corruption detected, should be " + vertex.getAllRelationships().size() + ")" %></td>
									</tr>
								</tbody>
							</table>
							<% if (!vertex.getRelationships().isEmpty()) { %> 
								<span class="menu"><%= loginBean.translate("Relationships") %>:</span>
								<table id="firsttable" class="tablesorter">
									<thead>
										<tr>
											<th></th>
											<th><%= loginBean.translate("Type") %></th>
											<th><%= loginBean.translate("Target") %></th>
											<th><%= loginBean.translate("Meta") %></th>
											<th><%= loginBean.translate("Correctness") %></th>
											<th><%= loginBean.translate("Access count") %></th>
											<th><%= loginBean.translate("Access date") %></th>
											<th><%= loginBean.translate("Creation date") %></th>
										</tr>
									</thead>
									<tbody>
										<% Iterator<Relationship> relationships = vertex.orderedAllRelationships(); %>
										<% if (vertex.getAllRelationships().size() != vertex.totalRelationships()) { relationships = vertex.getAllRelationships().iterator(); } %>
										<% while (relationships.hasNext()) { %>
											<% Relationship relationship = relationships.next(); %>
											<tr>
												<td>
													<input type="checkbox" name="<%= "r-" + relationship.getSource().getId() + "-" + relationship.getType().getId() + "-" + relationship.getTarget().getId() %>">
							 					</td>
												<td>
													<input type="checkbox" name="<%= "v-" + relationship.getType().getId() %>">
													<%= Utils.escapeHTML(relationship.getType().displayString()) %>
							 					</td>
												<td>
													<input type="checkbox" name="<%= "v-" + relationship.getTarget().getId() %>">
													<%= Utils.escapeHTML(relationship.getTarget().displayString()) %>
													</input>
												</td>
												<td>
												<% if (relationship.getMeta() != null) { %>
													<input type="checkbox" name="<%= "v-" + relationship.getMeta().getId() %>">
													<%= Utils.escapeHTML(relationship.getMeta().displayString()) %>
													</input>
												<% } %>
												</td>
												<td><%= relationship.getCorrectness() %></td>
												<td><%= relationship.getAccessCount() %></td>
												<td><%= Utils.printDate(relationship.getAccessDate()) %></td>
												<td><%= Utils.printDate(relationship.getCreationDate()) %></td>
											</tr>
										<% } %>
									</tbody>
								</table>
							<% } %>
						<% } %>
					</form>
				<% } else if (bean.getMode() == BrowseMode.Worksheet) { %>
					<h3><%= loginBean.translate("Worksheet") %></h3>
					<form action="memory" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<% if (!loginBean.isMobile()) { %>
							<div id="editor" class="editor" style="height:200px"><%= bean.getCode() %></div>
							<script>
								var editor = ace.edit("editor");
								editor.getSession().setMode("ace/mode/self");
								var saveSource = function() {
									document.getElementById("code").value = editor.getSession().getValue();
								}
							</script>
							<textarea id="code" name="code" style="width:0px;height:0px" class="hidden"><%= bean.getCode() %></textarea>
						<% } else { %>
							<textarea id="code" name="code"><%= bean.getCode() %></textarea>
						<% } %>
						<br/>
						<input type="submit" name="execute" value="<%= loginBean.translate("Execute") %>" onclick="saveSource()" title="Execute the Self code">
					</form>
					<br/>
				<% } else if (bean.getMode() == BrowseMode.Graph) { %>
					<h3><%= loginBean.translate("Graph") %></h3>
						<% List<Vertex> results = bean.getResults(); %>
						<% if (!results.isEmpty()) { %>
							<div id="chart_div"></div>
							<script type="text/javascript">
								google.charts.load('current', {packages:["orgchart"]});
								google.charts.setOnLoadCallback(drawChart);
					
								function drawChart() {
									var data = new google.visualization.DataTable();
									data.addColumn('string', 'Id');
									data.addColumn('string', 'SourceId');
									data.addColumn('string', 'ToolTip');
					
									<% for(Vertex v : results) { %>
										<%=bean.displayGraph(v, 2) %>
									<% } %>
					
									var chart = new google.visualization.OrgChart(document.getElementById('chart_div'));
									chart.draw(data, {allowHtml:true, allowCollapse:true});
								}
							</script>
						<% } %>
					<br/>
				<% } %>
				<% if ((bean.getResults() != null) && ((bean.getMode() == BrowseMode.Search) || (bean.getMode() == BrowseMode.Reports) || (bean.getMode() == BrowseMode.Worksheet))) { %>
					<form action="memory" method="get" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<div class="toolbar" style="display:none;">
							<input id="inspect-icon" class="icon" type="submit" name="browse" value="">
							<input id="relationships-icon" class="icon" type="submit" name="references" value="">
							<input id="graph-icon" class="icon" type="submit" name="graph" value="">
							<input id="select-icon" class="icon" type="submit" name="select-all" value="">
							<input id="pin-icon" class="icon" type="submit" name="pin" value="">
							<input id="unpin-icon" class="icon" type="submit" name="unpin" value="">
							<input id="remove-icon" class="icon" type="submit" name="delete" value="">
							<input id="export-icon" class="icon" type="submit" name="export" value="">
							<input id="import-icon" class="icon" type="submit" name="import" value="">
							<input id="importlib-icon" class="icon" type="submit" name="importlib" value="">
						</div>
						<span class = menu>
							<%= bean.getResultsSize() %> results.<br/>
							<%= bean.pagingString() %>
						</span>
						<% List results = bean.getResults(); %>
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
										<td><input type="checkbox" name="<%= "v-" + vertex.getId() %>" <%= ((count < 100) && bean.isSelectAll()) ? "checked" : "" %>></td>
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
												<input type="checkbox" name="<%= "v-" + vertex.getId() %>" <%= ((count < 100) && bean.isSelectAll()) ? "checked" : "" %>>
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
				<p/>
			
				<div id="dialog-delete-all" title="<%= loginBean.translate("Delete All") %>" class="dialog">
					<form action="memory" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<input type="checkbox" name="confirm"><%= loginBean.translate("I understand this will permanently delete everything from the bot's memory and bootstrap it with minimal knowledge") %></input><br/>
						<input id="delete" type="submit" name="delete-all" value="<%= loginBean.translate("Delete All") %>" title="<%= loginBean.translate("Caution, this will permanently delete everything from the bot's memory and bootstrap it with minimal knowledge") %>">
						<input id="cancel-rebootstrap" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
					</form>
				</div>
			
				<div id="dialog-import" title="<%= loginBean.translate("Import") %>" class="dialog">
					<form action="memory-upload" method="post" enctype="multipart/form-data" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<table>
						<tr><td>
						<%= loginBean.translate("File") %></td><td>
						<input id="import-file" type="file" name="file"/>
						</td></tr>
						<tr><td>
						<%= loginBean.translate("Format") %></td><td>
						<select name="import-format" title="<%= loginBean.translate("Choose the format of the knowledge file to import") %>" style="width:150px">
							<option value="json"><%= loginBean.translate("JSON") %></option>
							<option value="csv"><%= loginBean.translate("CSV") %></option>
							<option value="set"><%= loginBean.translate("Set") %></option>
							<option value="map"><%= loginBean.translate("Map") %></option>
							<option value="properties"><%= loginBean.translate("Properties") %></option>
						</select>
						</td></tr>
						<tr><td>
						<%= loginBean.translate("Encoding") %></td><td>
						<input style="width:150px" name="import-encoding" type="text" value="UTF-8" title="<%= loginBean.translate("Some files may require you to set the character enconding to import correctly") %>" />
						</td></tr>
						</table>
						<input type="checkbox" name="pin" title="Pin the objects in the bot's memory so it will never forget them"><%= loginBean.translate("Pin") %></input><br/>
						<input class="ok" type="submit" name="import" value="<%= loginBean.translate("Import") %>" title="<%= loginBean.translate("Upload and import the knowledge file") %>">
						<input id="cancel-import" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
					</form>
				</div>
		
				<div id="dialog-export" title="<%= loginBean.translate("Export") %>" class="dialog">
					<form action="memory" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<%= botBean.instanceInput() %>
						<%= loginBean.translate("Format") %>
						<select name="export-format" title="<%= loginBean.translate("Choose the format to export the objects to") %>">
							<option value="json"><%= loginBean.translate("JSON") %></option>
							<option value="csv"><%= loginBean.translate("CSV") %></option>
						</select><br/>
						<input id="export" class="ok" type="submit" name="export" value="<%= loginBean.translate("Export") %>" title="<%= loginBean.translate("Export and download the objects to a file") %>"/>
						<input id="cancel-export" class="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
					</form>
				</div>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
