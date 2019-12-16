<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.LoginBean"%>

<%@page import="org.botlibre.web.bean.WebMediumBean"%>
<%@page import="org.botlibre.web.admin.WebMedium"%>
<%@page import="org.botlibre.web.bean.BrowseBean"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<script>
	<% loginBean = proxy.checkLoginBean(loginBean); %>
	<% WebMediumBean bean = (WebMediumBean) loginBean.getActiveBean(); %>
	var selectAdmins = false;
	var selectUsers = false;
	var sdkConnection = null;
	<% bean.validateInstance(Long.toString(bean.getInstanceId())); %>
	SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
	sdkConnection = new SDKConnection();
	
	var sdkUser = new UserConfig();
	<% if (loginBean.isLoggedIn()) { %>
		sdkUser.user = "<%= loginBean.getUser().getUserId() %>";
		sdkUser.token = "<%= loginBean.getUser().getToken() %>";
		sdkConnection.user = sdkUser;
	<% } %>
	sdkConnection.error = function(error) {
		console.log(error);
		SDK.showError(error, "<%= loginBean.translate("Server Error") %>");
		return;;
	}
	
	function selectAllUsers(tableName) {
		$("#" + tableName + " tbody").find('tr').each(function (index, row) {
			var tableRow = $(row);
			var checkBox = tableRow.find('input[type="checkbox"]');
			if (!checkBox.is(':checked')) {
				checkBox.prop('checked', true);
			}
		});
	}
	
	function unselectAllUsers(tableName) {
		$("#" + tableName + " tbody").find('tr').each(function (index, row) {
			var tableRow = $(row);
			var checkBox = tableRow.find('input[type="checkbox"]');
			if (checkBox.is(':checked')) {
				checkBox.prop('checked', false);
			}
		});
	}
</script>

<h3><%= loginBean.translate("Administrators") %></h3>
<div class="toolbar">
	<img id="select-all-admins" src="images/select.svg" />
	<img id="remove-selected-admins" src="images/remove3.svg" />
</div>
<table id="admins-table" class="tablesorter">
	<thead>
		<tr>
			<th><%= loginBean.translate("Select") %></th>
			<th><%= loginBean.translate("User Name") %></th>
		</tr>
	</thead>
	<tbody>
		<% for (User user : ((WebMedium)bean.getInstance()).getAdmins()) { %>
		<tr id="<%= user.getUserId()%>">
			<td><input name="selected-admin" value="<%= user.getUserId()%>"
				type="checkbox" /></td>
			<td>
				<span value="<%= user.getUserId()%>"><%= user.getUserId() %></span>
			</td>
		</tr>
		<% } %>
	</tbody>
</table>
<div style="display: flex;">
	<input id="admin-input" class="users" type="text" name="newAdmin" style="width: 100%;" title="<%= loginBean.translate("Enter the user id of the user to add as an administrator.") %>" />
	<img id="add-new-admin" name="addAdmin" src="images/plus.svg" style="margin-top: 3px;" title="<%= loginBean.translate("Add the new administrator") %>" />
</div>
<script>
	$('#select-all-admins').click(function(event) {
		event.preventDefault();
		if (!selectAdmins) {
			selectAdmins = true;
			selectAllUsers("admins-table");
		} else {
			selectAdmins = false;
			unselectAllUsers("admins-table");
		}
	});
			
	function addAdmin() {
		var adminName = $("input[name=newAdmin]").val();
		if (adminName == null || adminName === "") {
			SDK.showError("Please enter a user to add.", "User Error");
			return false;
		}
		$("input[name=newAdmin]").val('');
		var userAdminConfig = new UserAdminConfig();
		userAdminConfig.operation = "AddAdmin";
		userAdminConfig.operationUser = adminName;
		userAdminConfig.instance = "<%=bean.getInstanceId()%>";
		userAdminConfig.type = "<%=bean.getInstance().getTypeName()%>";
		sdkConnection.userAdmin(userAdminConfig, function() {
			var newRow = "<tr id='" + adminName + "'><td><input name='selected-admin' value='" + adminName + "' type='checkbox'></td><td><span value='" + adminName + "'>" + adminName + "</span></td></tr>";
			var row = $('#admins-table tbody:last-child');
			row.append(newRow);
		});
	}
			
	$('#add-new-admin').on('click', function(event) {
		event.preventDefault();
		addAdmin();
	});
			
	$('#admin-input').keypress(function (event) {
		var key = event.which;
		if (key == 13) {
			addAdmin();
			return false;  
		}
	});
			
	$('#remove-selected-admins').click(function(event) {
		event.preventDefault();
		var array = [];
		$('#admins-table tbody').find('tr').each(function (index, row) {
			var currRow = $(row);
			var checkBox = currRow.find('input[type="checkbox"]');
			if (checkBox.is(':checked')) {
				array.push(currRow.prop('id'));
			}
		});
		if (array.length == 0) {
			SDK.showError("Please select users to remove.", "Delete User Error");
			return false;
		}
		var sourceId = "";
		for (let i = 0; i < array.length; i++) {
			if (i == array.length - 1) {
				sourceId = sourceId.concat(array[i]);
			} else {
					sourceId = sourceId.concat(array[i] + ",");
			}
		}
		var userAdminConfig = new UserAdminConfig();
		userAdminConfig.operation = "RemoveAdmin";
		userAdminConfig.operationUser = sourceId;
		userAdminConfig.instance = "<%=bean.getInstanceId()%>";
		userAdminConfig.type = "<%=bean.getInstance().getTypeName()%>";
		sdkConnection.userAdmin(userAdminConfig, function() {
			for (let item in array) {
				var row = $('#admins-table tbody').find('tr#' + array[item]).remove();
			}
		});
	});
</script>
<p />
<h3><%= loginBean.translate("Users") %></h3>
<div class="toolbar">
	<img id="select-all-users" src="images/select.svg" />
	<img id="remove-selected-users" src="images/remove3.svg" />
</div>
<table id="users-table" class="tablesorter">
	<thead>
		<tr>
			<th><%= loginBean.translate("Select") %></th>
			<th><%= loginBean.translate("User Name") %></th>
		</tr>
	</thead>
	<tbody>
		<% for (User user : ((WebMedium)bean.getInstance()).getUsers()) { %>
		<tr id="<%= user.getUserId()%>">
			<td><input name="selected-user" value="<%= user.getUserId()%>"
				type="checkbox" /></td>
			<td>
				<span value="<%= user.getUserId()%>"><%= user.getUserId() %></span>
			</td>
		</tr>
		<% } %>
	</tbody>
</table>
<div style="display: flex;">
	<input id="user-input" class="users" type="text" name="newUser" style="width: 100%;" title="<%= loginBean.translate("Enter the user id of the user to add.") %>" />
	<img id="add-new-user" name="addUser" src="images/plus.svg" style="margin-top: 3px;" title="<%= loginBean.translate("Add the new user") %>" />
</div>
<script>
	$('#select-all-users').click(function(event) {
		event.preventDefault();
		if (!selectUsers) {
			selectUsers = true;
			selectAllUsers("users-table");
		} else {
			selectUsers = false;
			unselectAllUsers("users-table");
		}
	});
			
	function addUser() {
		var userName = $("input[name=newUser]").val();
		if (userName == null || userName === "") {
			SDK.showError("Please enter a user to add.", "Add User Error");
			return false;
		}
		$("input[name=newUser]").val('');
		var userAdminConfig = new UserAdminConfig();
		userAdminConfig.operation = "AddUser";
		userAdminConfig.operationUser = userName;
		userAdminConfig.instance = "<%= bean.getInstanceId() %>";
		userAdminConfig.type = "<%=bean.getInstance().getTypeName()%>";
		sdkConnection.userAdmin(userAdminConfig, function() {
			var newRow = "<tr id='" + userName + "'><td><input name='selected-user' value='" + userName + "' type='checkbox'></td><td><span value='" + userName + "'>" + userName + "</span></td></tr>";
			var row = $('#users-table tbody:last-child');
			row.append(newRow);
		});
	}
			
	$('#add-new-user').on('click', function(event) {
		event.preventDefault();
		addUser();
	});
			
	$('#user-input').keypress(function (event) {
		var key = event.which;
		if (key == 13) {
			addUser();
			return false;  
		}
	});
			
	$('#remove-selected-users').click(function(event) {
		event.preventDefault();
		var array = [];
		$('#users-table tbody').find('tr').each(function (index, row) {
			var currRow = $(row);
			var checkBox = currRow.find('input[type="checkbox"]');
			if (checkBox.is(':checked')) {
				array.push(currRow.prop('id'));
			}
		});
		if (array.length == 0) {
			SDK.showError("Please select users to remove.", "Delete User Error");
			return false;
		}
		var sourceId = "";
		for (let i = 0; i < array.length; i++) {
			if (i == array.length - 1) {
				sourceId = sourceId.concat(array[i]);
			} else {
				sourceId = sourceId.concat(array[i] + ",");
			}
		}
		var userAdminConfig = new UserAdminConfig();
		userAdminConfig.operation = "RemoveUser";
		userAdminConfig.operationUser = sourceId;
		userAdminConfig.instance = "<%=bean.getInstanceId()%>";
		userAdminConfig.type = "<%=bean.getInstance().getTypeName()%>";
		sdkConnection.userAdmin(userAdminConfig, function() {
			for (let item in array) {
				var row = $('#users-table tbody').find('tr#' + array[item]).remove();
			}
		});
	});
</script>