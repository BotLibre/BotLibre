<style>
	#contents {
		justify-content: center;
		min-height: 570px;
		flex-direction: column;
		margin: auto;
		width: 100%;
	}

	.newsletter-form {
		margin: 0;
		width: 100%;
		max-width: 100%;
	}

	img.newsletter-img {
		display: block;
		height: 80px;
		object-fit: cover;
	}

	div.ui-dialog {
		z-index: 1051 !important;
	}

	.message input[type='submit'].subscribeLater {
		text-align: center;
		background: transparent;
		display: inline;
		text-decoration: underline;
		color: #888;
		text-transform: capitalize;
		height: auto;
		margin: 0;
		padding: 0;
	}

	@media only screen and (min-width: 767px) {
		img.newsletter-img {
			width: 400px;
			height: auto;
		}

		.newsletter-form {
			width: 500px;
		}

		#contents {
			max-width: 100%;
			flex-direction: row;
		}
	}
</style>

<div style="display: flex; padding: 0;" id="contents">
	<img class="newsletter-img" src="marketing/botlibre-community.png">
	<div class="newsletter-form" class="about">
		<h1 style="text-align:center;margin-top:0;font-size:24px;font-weight:bold;"><%= loginBean.translate("REGISTER") %></h1>
		<div style="text-align:center;" class="newsletter-header">
			<p><%= loginBean.translate("As we edge closer to the metaverse being a reality, chatbots will become a high commodity!") %></p><br>
			<p><b><%= loginBean.translate("Do you want free content  and support on building your chatbot for your business?") %></b></p>
			<p><b><%= loginBean.translate("PLUS facts on up-to-date industry trends?") %></b></p><hr>
			<h5><b><%= loginBean.translate("SIGN UP NOW") %></b></h5>
		</div>
		<form action="login" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<input type="hidden" id="spamCheck" name="spamCheck">
			<input placeholder="<%= loginBean.translate("Email") %>" type="email" id="email" name="email">
			<input placeholder="<%= loginBean.translate("Name") %>" type="text" id="name" name="name" spellcheck="false">
			<input placeholder="<%= loginBean.translate("Company") %>" type="text" id="business-name" spellcheck="false" name="business">
			<label><input required type="checkbox" name="newsletterCheck"><div class="checkmark"></div> <%= loginBean.translate("Subscribe to Bot Libre Newsletter") %></label>
			<br/>
			<input id="ok" type="submit" onclick="return validateForm()" name="contact" value="<%= loginBean.translate("grab this free investment") %>">
			<input formnovalidate type="submit" name="subscribeLater" class="subscribeLater" value="<%= loginBean.translate("i\'ll do it later") %>">
		</form>
	</div>
</div>

<script>
	function validateForm() {
		var email = document.getElementById("email");
		if (!email.value.includes("@")) {
			email.className = null;
			email.style.border = "2px solid red";
			SDK.showError('<%= loginBean.translate("Enter a valid email address") %>');
			return false;
		}
		var name = document.getElementById("name");
		if (name.value == "") {
			name.className = null;
			name.style.border = "2px solid red";
			SDK.showError('<%= loginBean.translate("Enter your name") %>');
			return false;
		}
		var business = document.getElementById("business-name");
		if (business.value == "") {
			business.className = null;
			business.style.border = "2px solid red";
			SDK.showError('<%= loginBean.translate("Enter your business name") %>');
			return false;
		}
		var newsletterCheck = $('input[name=newsletterCheck]').get(0);
		if (!newsletterCheck.checked) {
			SDK.showError('<%= loginBean.translate("Please check \\'Subscribe to Bot Libre Newsletter\\'") %>');
			return false;
		}
		var spamCheck = document.getElementById("spamCheck");
		spamCheck.value = "ok";
		return true;
	}
</script>
