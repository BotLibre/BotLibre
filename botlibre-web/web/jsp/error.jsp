<%@ page import = "org.botlibre.web.bean.BotBean" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
%>

<% if (loginBean.getError() != null) { %>
	<div style="overflow:auto;max-width:100%">
		<p>
			<%
				String message = loginBean.getErrorMessage();
				String pre = message;
				String post = "";
				int index = message.indexOf("-");
				int index2 = message.indexOf(":");
				if (index != -1) {
					pre = message.substring(0, index);
					post = message.substring(index, message.length());
				} else if (index2 != -1) {
					pre = message.substring(0, index2);
					post = message.substring(index2, message.length());
				}
				message = loginBean.translate(pre) + post;
			%>
			<b><pre><code style="color:#E00000;"><%= message %></code></pre></b>
		</p>
	</div>
	<br/>
	<% loginBean.setError(null); %>
<% } %>
