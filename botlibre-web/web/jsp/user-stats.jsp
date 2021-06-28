<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.ErrorMessage"%>
<%@page import="org.botlibre.web.bean.UserBean"%>
<%@page import="org.botlibre.web.service.AppIDStats"%>
<%@page import="org.botlibre.web.Site"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<jsp:include page="head.jsp"/>
	<title>User Stats - <%= Site.NAME %></title>
	<meta name="description" content="The user stats allows you monitor user activity"/>	
	<link rel="stylesheet" href="css/stats-charts.css" type="text/css">
	<link rel="stylesheet" href="//cdn.jsdelivr.net/chartist.js/latest/chartist.min.css">
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script src="scripts/jquery/jquery.js"></script>
	<script src="scripts/jquery/jquery-ui.min.js"></script>
	<script src="//cdn.jsdelivr.net/chartist.js/latest/chartist.min.js"></script>
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
	<script src="https://momentjs.com/downloads/moment.min.js"></script>
	
	<meta NAME="ROBOTS" CONTENT="NOINDEX, NOFOLLOW">
	<title><%= loginBean.translate("Messages") %><%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("View user analytics") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("analytics, user") %>"/>
	<% loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<style>
		.select-div {
			width: 215px;
			padding-bottom: 2px;
			display: inline-block;
		}
		#datesRangeSelect, #groupBySelect {
			width: 130px;
			margin: 0px;
		}
		.selectiveCharts {
			list-style-type: none;
		    border: 2px solid #CDCDCD;
		}
		.selectiveCharts li {
			display: inline;
		}
		.selectiveCharts li div {
			display: inline-block;
			padding: 7px;
		}
		.selectiveCharts li div span {
			margin-left: 7px;
		}
		.selectTitle {
			width: 62px;
			color: #818181;
		font-size: 12px;
		display: inline-block;
		}
		.ct-series-a .ct-line {
			stroke: #5271ff; /* navy blue */
		}
		.ct-series-a .ct-point {
			stroke: #5271ff; /* navy blue */
		}
		.ct-series-b .ct-line {
			stroke: #ff832c; /* orange pink */
		}
		.ct-series-b .ct-point {
			stroke: #ff832c; /* orange pink */
		}
		.ct-series-c .ct-line {
			stroke: #ff5c5c; /* pink */
		}
		.ct-series-c .ct-point {
			stroke: #ff5c5c; /* pink */
		}
		.ct-series-d .ct-line {
			stroke: #339966; /* dull green */
		}
		.ct-series-d .ct-point {
			stroke: #339966; /* dull green */
		}
		.ct-series-e .ct-line {
			stroke: #99ccff; /* light blue */
		}
		.ct-series-e .ct-point {
			stroke: #99ccff; /* light blue */
		}
	</style>
</head>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
		<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
		<% if (!loginBean.isEmbedded() || loginBean.getLoginBanner()) { %>
			<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>
		<% } %>
		<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
		<script>
			$(document).ready(function() {
				$(".tablesorter").tablesorter({widgets: ['zebra']});
			});
		</script>
		<jsp:include page="banner.jsp"/>
		<div id="admin-topper" align="left">
			<div class="clearfix">
				<a href="<%= "login?view-user=" + loginBean.getUserId() %>"><%= loginBean.getUserId() %></a>
			</div>
		</div>
		<jsp:include page="admin-user-banner.jsp"/>
		<div id="mainbody">
			<div id="contents">
				<div class="about">
<% } %>
					<jsp:include page="error.jsp"/>
					<% if (!loginBean.isLoggedIn()) { %>
						<p style="color:#E00000;">
							<%= loginBean.translate("You must first") %> <a href="<%= "login?sign-in=sign-in" + proxy.proxyString() %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to view your analytics") %>.
						</p>
					<% } else { %>
						<h1>
							<span class="dropt-banner">
								<img src="images/stats.svg" class="admin-banner-pic">
								<div>
									<p class="help">
										<%= loginBean.translate("View the user's analytics and charts.") %><br/>
									</p>
								</div>
							</span> User Analytics
						</h1>
						<div class="select-div">
							<span id="dateRangeSpan" class="selectTitle">Duration</span>
							<select id="datesRangeSelect">
								<option value="weekly"><%= loginBean.translate("current week") %></option>
								<option value="monthly"><%= loginBean.translate("current month") %></option>
								<option value="allDatesRange"><%= loginBean.translate("all time") %></option>
							</select>
						</div>
						<div class="select-div"> 
							<span id="groupByStatsSpan" class="selectTitle">Group By</span>
							<select id="groupBySelect">
								<option value="none"><%= loginBean.translate("day") %></option>
								<option value="weekly"><%= loginBean.translate("week") %></option>
								<option value="monthly"><%= loginBean.translate("month") %></option>
							</select>
						</div>
						<div id="userStatsDiv" style="display:block;">
							<ul id="apiList" class="selectiveCharts">
								<li><div><input class="apiCalls" type="checkbox" name="API Calls" checked><span><%= loginBean.translate("API Calls") %></span></div></li>
								<li><div><input class="overLimit" type="checkbox" name="Over Limit"><span><%= loginBean.translate("Over Limit") %></span></div></li>
							</ul>
							<div id="apiStatsChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
							<h2><%= loginBean.translate("API Stats") %></h2>
							<table id="apiStats" class="tablesorter">
								<thead>
									<tr>
										<th>Date</th>
										<th>API Calls</th>
										<th>Over Limit</th>
									</tr>
								</thead>
								<tbody>
									<% for (AppIDStats stat : AdminDatabase.instance().getAllAppIDStats(String.valueOf(loginBean.getUser().getApplicationId()))) { %>
										<tr>
											<td><%= stat.date %></td>
											<td><%= stat.apiCalls %></td>
											<td><%= stat.overLimit %></td>
										</tr>
									<% } %>
								</tbody>
							</table>
						</div>
					<% } %>
				</div>
	<% if (!embed) { %>
		</div>
		</div>
		<jsp:include page="footer.jsp"/>
	<% } else { %>
		</div>
		<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
	<% } %>
	<% if (loginBean.isLoggedIn()) { %>
		<script>
			var apiCallsArray = [];
			var overLimitArray = [];
			function generateData() {
				<% List<AppIDStats> statsList = new ArrayList<AppIDStats>(); %>
				<% statsList = AdminDatabase.instance().getAllAppIDStats(String.valueOf(loginBean.getUser().getApplicationId())); %>
				<% for (int i = statsList.size() - 1; i >= 0; i--) { %>
					<% AppIDStats stat = statsList.get(i); %>
					apiCallsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.apiCalls %> } );
					overLimitArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.overLimit %> } );
				<% } %>
			}
			generateData();
		
			function generateAnalyticsData(dateInterval, groupByValue, listType) {
				var tempApiCallsArray = apiCallsArray;
				var tempOverLimitArray = overLimitArray;
				var timeInterval = apiCallsArray.length;
				if (dateInterval === "weekly" && timeInterval >= 7) {
					timeInterval = 7;
				} else if (dateInterval === "monthly" && timeInterval >= 30) {
					timeInterval = 30;
				}
				if (timeInterval == 7 || timeInterval == 30) {
					tempApiCallsArray = tempApiCallsArray.slice(apiCallsArray.length - timeInterval, apiCallsArray.length);
					tempOverLimitArray = tempOverLimitArray.slice(overLimitArray.length - timeInterval, overLimitArray.length);
				}
				var groupByNum = 0
				if (groupByValue === "weekly") {
					groupByNum = 7;
				} else if (groupByValue === "monthly") {
					groupByNum = 30;
				}
				var apiUserStats = {};
				var listTypeStr = "#" + listType + " li div ";
				var apiCalls = { 'API Calls': groupBy(tempApiCallsArray, groupByNum), 'status': $(listTypeStr + '.apiCalls').prop('checked') };
				var overLimit = { 'Over Limit': groupBy(tempOverLimitArray, groupByNum), 'status': $(listTypeStr + '.overLimit').prop('checked') };
				apiUserStats['apiList'] = { 'API Calls': apiCalls, 'Over Limit': overLimit };
				return apiUserStats;
			}
		
			function groupBy(array, group) {
				if (group == 0) return array;
				var i;
				var sum = 0;
				var counter = 0;
				var groupArray = new Array();
				for (i = 0; i < array.length; i++) {
					sum += array[i].y;
					if (counter == group - 1) {
						var date = { x: array[i].x, y: sum };
						groupArray.push(date);
						sum = 0;
						counter = 0;
					} else {
						counter += 1;
					}
				}
				return groupArray;
			}
		
			function generateChart(botJsonData, listType, checkBoxType, chartType, isChecked) {
				var i = 1;
				var seriesArray = [];
				var chartColorArray = [];
				var socialMediaType = botJsonData[listType];
				for (var key in socialMediaType) {
					if(socialMediaType.hasOwnProperty(key)) {
						var mediaType = socialMediaType[key];
						if (key === checkBoxType) {
							if (isChecked == true) {
								mediaType.status = true;
								seriesArray.push( { name: ('series-' + i), data: mediaType[key] } );
								chartColorArray.push(key);
								i++;
							} else {
								mediaType.status = false;
							}
						} else {
							if (mediaType.status == true) {
								seriesArray.push( { name: ('series-' + i), data: mediaType[key] } );
								chartColorArray.push(key);
								i++;
							}
						}
					}
				}
				setCheckBoxLabelColor(listType, chartColorArray);
				drawCharts(seriesArray, chartType);
			}
			/* Generate initial chat graph for current week duration with no grouping */
			var apiStatsData = generateAnalyticsData("weekly", "none", "apiList");
			generateChart(apiStatsData, "apiList", "", "#apiStatsChart", false);
		
			function setCheckBoxLabelColor(listType, checkBoxArray) {
				var listTypeStr = "#" + listType + " li";
				$(listTypeStr).each(function(li) {
				    var checkBoxName = $(this).children(0).children(0).attr("name");
				    var index = checkBoxArray.indexOf(checkBoxName);
				    if (index == 0) {
				    	$(this).children(0).children(1).css("color", "#5271ff");
				    } else if (index == 1) {
				    	$(this).children(0).children(1).css("color", "#ff832c");
				    } else if (index == 2) {
				    	$(this).children(0).children(1).css("color", "#ff5c5c");
				    } else if (index == 3) {
				    	$(this).children(0).children(1).css("color", "#339966");
				    } else if (index == 4) {
				    	$(this).children(0).children(1).css("color", "#99ccff");
				    } else {
				    	$(this).children(0).children(1).css("color", "inherit");
				    }
				});
			}
		
			function resetCheckBoxes(listType) {
				$(listType).each(function(li) {
				    var checkBoxName = $(this).children(0).children(0).attr("name");
				    if (checkBoxName === "API Calls" || checkBoxName === "Over Limit") {
				    	$(this).children(0).children(0).prop("checked", true);
				    } else {
				    	$(this).children(0).children(0).prop("checked", false);
				    }
				});
			}
		
			function drawCharts(seriesArray, chartType) {
				var chart = new Chartist.Line(chartType, {
					  series: seriesArray 
					},
					{
						axisX: {
						    type: Chartist.FixedScaleAxis,
						    divisor: 12,
						    labelInterpolationFnc: function(value) {
						      return moment(value).format('MMM D');
						    }
						},
						axisY: {
					         scaleMinSpace: 30
					    }
				});
			}
		
			$('.apiCalls').change(function() {
				var checkBoxType = $(this).attr('name');
				var listType = $(this).parent().parent().parent().attr('id');
				var apiAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
				if ($(this).prop("checked")) {
					generateChart(apiAnalticsData, listType, checkBoxType, "#apiStatsChart", true);
				} else {
					generateChart(apiAnalticsData, listType, checkBoxType, "#apiStatsChart", false);
				}
			});
			$('.overLimit').change(function() {
				var checkBoxType = $(this).attr('name');
				var listType = $(this).parent().parent().parent().attr('id');
				var apiAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
				if ($(this).prop("checked")) {
					generateChart(apiAnalticsData, listType, checkBoxType, "#apiStatsChart", true);
				} else {
					generateChart(apiAnalticsData, listType, checkBoxType, "#apiStatsChart", false);
				}
			});
			/* ################# Date Range Drop Down Select Box ################# */
			$("#datesRangeSelect").on("change", function() {
				var dateRangeValue = this.value;
				var apiAnalticsData = generateAnalyticsData(dateRangeValue, $('#groupBySelect').val(), "apiList");
				generateChart(apiAnalticsData, "apiList", "", "#apiStatsChart", false);
			});
			/* ################# Group By Drop Down Select Box ################# */
			$("#groupBySelect").on("change", function() {
				var groupByValue = this.value;
				var apiAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), groupByValue, "apiList");
				generateChart(apiAnalticsData, "apiList", "", "#apiStatsChart", false);
			});
		</script>
	<% } %>
	<% proxy.clear(); %>
	</body>
</html>