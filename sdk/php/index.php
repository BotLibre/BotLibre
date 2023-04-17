<!DOCTYPE html>
<html>

<head>
	<meta charset="utf-8">
	<title>BotLibre API</title>
	<link rel="stylesheet" type="text/css" href="styles/styles.css">
</head>

<body>
	<div class="container">
		<form action="index.php" method="get">
			<label for="api">API Endpoint:</label>
			<select id="api" name="api">
				<option value="">select</option>
				<option value="connect">/check-user</option>
				<option value="chat">/post-chat</option>
				<option value="view-user">/view-user</option>
				<option value="check-forum-post">/check-forum-post</option>
				<!--option value="create-user">/create-user</option-->
			</select>
			<br><br>
			<input type="submit" value="TEST">

			<?php
			if (isset($_GET["api"])) {
				echo "<strong style='padding: 10px; font-size: 18px;'>Selected API : </strong>" . "<u style='font-size: 19px;'>" . $_GET["api"] . "</u>";
			} else {
				echo "<strong style='font-size: 18px;'>Nothing is selected.</strong>";
			}
			?>

		</form>
	</div>
	<?php
	require_once('sdk/SDKConnection.php');
	require_once('sdk/BotlibreCredentails.php');

	class Main
	{

		public static ?bool $DEBUG = true;
		public static ?bool $ADULT = false;

		/**
		 * Enter your application ID here.
		 * You can get an application ID from any of the services websites (BOT libre, FORUMS libre, LIVE CHAT libre, Paphus Live Chat)
		 */
		public string $applicationId = "986530966192349057";
		public string $twitterAppId = "6022406562833792899";
		public string $paphusAppId = "1444710791353231612";
		public string $forumsAppId = "1401036148793048377";
		public string $livechatAppId = "1401036148793048377";

		/**
		 * Configure your connection credentials here.
		 * Choose which service provider you wish to connect to.
		 */
		public static ?SDKConnection $connection = null;

		public static ?string $domainId = null;
		public static ?DomainConfig $domain;
		public static string $defaultType = "Bots";
		public static bool $showAds = true;

		public function __construct()
		{
			Main::$connection = new SDKConnection(new BotlibreCredentails($this->applicationId));
			if (Main::$domainId != null) {
				Main::$domain = new DomainConfig();
				Main::$domain->id = $this->domainId;
				Main::$connection->setDomain(Main::$domain);
			}
			if (Main::$DEBUG) {
				$debugInfo = Main::$connection;
				$debugComment = "[Main] initializing SDKConnection.";
				Main::$showAds = false;
				Main::$connection->setDebug(true);
				include "views/debug.php";
			}

		}
		public static ?string $WEBSITE = "http://www.botlibre.com";
		public static ?string $WEBSITEHTTPS = "https://www.botlibre.com";
		public static ?string $SERVER = "botlibre.com";
		/**
		 * If you are building a single instance app, then you can set the instance id or name here,
		 * and use this activity to launch it.
		 */
		public static ?string $launchInstanceId = ""; // i.e. "171"
		public static ?string $launchInstanceName = ""; // i.e. "Help Bot"
	


		public function testConnectUserAccount(): UserConfig
		{
			//TODO: Setup user
			$userConfig = new UserConfig();
			$userConfig->application = "7369571872937606859";
			$userConfig->user = "Account_test";
			$userConfig->password = "password123";
			$userConfig = Main::$connection->connect($userConfig);
			return $userConfig;
		}

		public function testSendChatMessage(): ?ChatResponse
		{
			//TODO: Setup message
			$config = new ChatConfig();
			$config->message = "Who are you?";
			$config->application = "7369571872937606859";
			$config->instance = "165";
			$response = Main::$connection->chat($config);
			return $response;
		}

		public function testFetchUserDetails(): ?UserConfig
		{
			//TODO: Setup user using 
			$userConfig = new UserConfig();
			$userConfig->application = "7369571872937606859";
			$userConfig->user = "user";
			//$userConfig->password = "password123";
			$userConfig = Main::$connection->fetchUser($userConfig);
			return $userConfig;
		}

		public function testFetchForumPost(): ?ForumPostConfig
		{
			//TODO: Setup user using 
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->application = "7369571872937606859";
			$forumPostConfig->user = "Account_test";
			$forumPostConfig->password = "password123";
			$forumPostConfig->id = "5012";
			$forumPostConfig = Main::$connection->fetchForumPost($forumPostConfig);
			return $forumPostConfig;
		}

		public function testCreateUser(): ?UserConfig //Hasn't been tested yet.
		{
			//TODO: Setup user using 
			$userConfig = new UserConfig();
			$userConfig->application = "7369571872937606859";
			$userConfig->user = "Account_test";
			$userConfig->password = "password123";
			//Missing additional information
			$userConfig = Main::$connection->createUser($userConfig);
			return $userConfig;
		}
	}
	?>
	<fieldset>
		<legend>API logs</legend>
		<?php
		//TODO:`
		$main = new Main();
		if (isset($_GET["api"])) {
			switch ($_GET["api"]) {
				case "connect";
					$debugComment = "<strong>Connecting a user account. @account_test</strong>";
					include "views/debug.php";
					$userConfig = $main->testConnectUserAccount();
					break;
				case "chat":
					$debugComment = "<strong>Connecting a user account. @account_test and sending a 'hello world' message.</strong>";
					include "views/debug.php";
					$userConfig = $main->testConnectUserAccount();
					$debugComment = "<strong>Sending a chat message.</strong>";
					include "views/debug.php";
					$response = $main->testSendChatMessage();
					break;
				case "view-user":
					$debugComment = "<strong>Fetch user details. @test</strong>";
					include "views/debug.php";
					$userConfig = $main->testFetchUserDetails();
					break;
				case "check-forum-post":
					$debugComment = "<strong>Fetch Forum post detials.</strong>";
					include "views/debug.php";
					$forumPost = $main->testFetchForumPost();
					break;
				case "create-user":
					$debugComment = "<strong>Create a new user.</strong>";
					include "views/debug.php";
					$userConfig = $main->testCreateUser();
					break;
			}
		}
		?>

	</fieldset>
	<div id="box" style="padding: 10px;">
		<strong>Details</strong>
		<br>
		<?php
		if (isset($userConfig)) {
			echo "User: " . $userConfig->user . "<br>";
			echo "Joined: " . $userConfig->joined . "<br>";
			echo "Connects: " . $userConfig->connects . "<br>";
			echo "Name: " . $userConfig->name . "<br>";
			if (isset($response)) {
				echo "Message: " . $response->message . "<br>";
			}
		} else if (isset($forumPost)) {
			echo "Topic: " . $forumPost->topic . "<br>";
			echo "Views: " . $forumPost->views . "<br>";
			echo "ThumbsUp: " . $forumPost->thumbsUp . "<br>";
			echo "Stars: " . $forumPost->stars . "<br>";
			echo "Creation Date: " . $forumPost->creationDate . "<br>";
		} else {
			echo "There is no data to show yet.";
		}
		?>
	</div>
</body>

</html>