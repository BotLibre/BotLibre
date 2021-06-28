<%@page import="org.botlibre.web.admin.User"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.LoginBean"%>
<%@page import="org.botlibre.web.admin.Friendship"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.WebMediumBean"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>

<script>
	<% loginBean = proxy.checkLoginBean(loginBean); %>
	<% WebMediumBean bean = (WebMediumBean) loginBean.getActiveBean(); %>
	var selectUsers = false;
	var sdkConnection = null;
	SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
	sdkConnection = new SDKConnection();
	sdkConnection.debug = true;
	
	var sdkUser = new UserConfig();
	<% if (loginBean.isLoggedIn()) { %>
		sdkUser.user = "<%= loginBean.getUser().getUserId() %>";
		sdkUser.token = "<%= loginBean.getUser().getToken() %>";
		sdkConnection.user = sdkUser;
	<% } %>
	sdkConnection.error = function(error) {
		console.log(error);
		SDK.showError(error, "<%= loginBean.translate("Error") %>");
		return;
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

<div class="toolbar">
	<img id="select-all-friends" src="images/select.svg" />
	<img id="remove-selected-friends" src="images/remove3.svg" />
</div>
<table id="user-friends-table" class="tablesorter">
	<thead>
		<tr>
			<th><%= loginBean.translate("Select") %></th>
			<th><%= loginBean.translate("Friend Name") %></th>
		</tr>
	</thead>
	<tbody>
		<% for (Friendship friend : loginBean.getUserFriendships(loginBean.getUser().getUserId())) { %>
			<tr id="<%= friend.getId()%>">
				<td>
					<input name="selected-friend" value="<%= friend.getFriend() %>" type="checkbox"/>
				</td>
				<td>
					<a class="friend-link" href="login?view-user=<%= friend.getFriend() %>"><%= friend.getFriend() %></a>
				</td>
			</tr>
		<% } %>
	</tbody>
</table>
<div style="display: flex;">
	<input id="friend-input" class="users" type="text" name="newFriend" style="width:100%;" title="<%= loginBean.translate("Enter the user id of the friend to add") %>" />
	<img id="add-new-friend" name="addFriend" src="images/plus.svg" style="margin-top: 3px;" title="<%= loginBean.translate("Add the new friend") %>" />
</div>
<h3><%= loginBean.translate("Followers") %></h3>
<table id="user-friends-followers-table" class="tablesorter">
	<thead>
		<tr>
			<th><%= loginBean.translate("Friend Name") %></th>
		</tr>
	</thead>
	<tbody>
		<% for (Friendship followers : loginBean.getUserFollowers(loginBean.getUser().getUserId())) { %>
			<tr id="<%= followers.getId()%>">
				<td>
					<span value="<%= followers.getUserId()%>"><a class="follower-link" href="login?view-user=<%= followers.getUserId() %>"><%= followers.getUserId() %></a></span>
				</td>
			</tr>
		<% } %>
	</tbody>
</table>
<script>
	$('#select-all-friends').click(function(event) {
		event.preventDefault();
		if (!selectUsers) {
			selectUsers = true;
			selectAllUsers("user-friends-table");
		} else {
			selectUsers = false;
			unselectAllUsers("user-friends-table");
		}
	});
			
	function addFriend() {
		var friendName = $("input[name=newFriend]").val();
		if (friendName == null || friendName === "") {
			SDK.showError("<%= loginBean.translate("Please enter a user to add.") %>", "<%= loginBean.translate("Error") %>");
			return false;
		}
		$("input[name=newFriend]").val('');
		var userFriendsConfig = new UserFriendsConfig();
		userFriendsConfig.action = "AddFriendship";
		userFriendsConfig.userFriend = friendName;
		userFriendsConfig.instance = "<%= bean.getInstanceId() %>";
		sdkConnection.userFriendship(userFriendsConfig, function(friendConfig) {
			var newRow = "<tr id='" + friendConfig.user + "'><td><input name='selected-friend' value='" + friendConfig.user + "' type='checkbox'></td><td><a class='friend-link' href='login?view-user=" + friendConfig.user + "'>" + friendConfig.user + "</a></td></tr>";
			var row = $('#user-friends-table tbody:last-child');
			row.append(newRow);
		});
	}
			
	$('#add-new-friend').on('click', function(event) {
		event.preventDefault();
		addFriend();
	});
			
	$('#friend-input').keypress(function (event) {
		var key = event.which;
		if (key == 13) {
			addFriend();
			return false;  
		}
	});
			
	$('#remove-selected-friends').click(function(event) {
		event.preventDefault();
		let friends = [];
		let friendIds = [];
		$('#user-friends-table tbody').find('tr').each(function (index, row) {
			let currRow = $(row);
			let checkBox = currRow.find('input[type="checkbox"]');
			if (checkBox.is(':checked')) {
				friends.push(checkBox.val());
				friendIds.push(currRow.prop('id'));
			}
		});
		if (friends.length == 0) {
			SDK.showError("<%= loginBean.translate("Please select friend to remove.") %>", "<%= loginBean.translate("Error") %>");
			return false;
		}
		let sourceId = "";
		for (let i = 0; i < friends.length; i++) {
			if (i == friends.length - 1) {
				sourceId = sourceId.concat(friends[i]);
			} else {
				sourceId = sourceId.concat(friends[i] + ", ");
			}
		}
		let userFriendsConfig = new UserFriendsConfig();
		userFriendsConfig.action = "RemoveFriendship";
		userFriendsConfig.userFriend = sourceId;
		userFriendsConfig.instance = "<%=bean.getInstanceId()%>";
		sdkConnection.userFriendship(userFriendsConfig, function() {
			for (let item in friendIds) {
				let friendId = friendIds[item];
				if (friendId.startsWith("@")) {
					$('#user-friends-table tbody').find('tr#\\' + friendIds[item]).remove();
				} else {
					$('#user-friends-table tbody').find('tr#' + friendIds[item]).remove();
				}
			}
		});
	});
</script>