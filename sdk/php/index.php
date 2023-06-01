<!--
/******************************************************************************
 *
 *  Copyright 2023 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
-->
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
				<?php
				if (isset($_GET["api"])) {
					echo "<option value='" . $_GET["api"] . "'>/" . $_GET["api"] . "</option>";
				} else {
					echo "<option value=''>select</option>";
				}
				?>
				<option value="connect">/check-user</option>
				<option value="chat">/post-chat</option>
				<option value="view-user">/view-user</option>
				<option value="check-forum-post">/check-forum-post</option>
				<option value="create-user">/create-user</option>
				<option value="update-user">/update-user</option>
				<option value="fetch-image">/fetch-image</option>
				<option value="create-forum-post">/create-forum-post</option>
				<option value="update-forum-post">/update-forum-post</option>
				<option value="create-channel-file-attachment">/create-channel-file-attachment</option>
				<option value="create-channel-image-attachment">/create-channel-image-attachment</option>
				<option value="create-post-reply">/create-post-reply</option>

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

		/* Enter your application ID here.
		 * You can get an application ID from any of the services websites (Bot Libre, Bot Libre for Business)
		 */
		public static string $applicationId = "";


		/**
		 * Please enter your account details. Both your username and password are required.
		 * If you don't have an account yet, you can create one to use.
		 */
		public static string $username = "";
		public static string $password = "";



		/**
		 * Configure your connection credentials here.
		 * Choose which service provider you wish to connect to.
		 */
		public static ?SDKConnection $connection = null;

		public static ?string $domainId = null;
		public static ?DomainConfig $domain;
		public static string $defaultType = "Bots";
		public static bool $showAds = true;

		public static ?string $WEBSITE = "http://www.botlibre.com";
		public static ?string $WEBSITEHTTPS = "https://www.botlibre.com";
		public static ?string $SERVER = "botlibre.com";
		/**
		 * If you are building a single instance app, then you can set the instance id or name here,
		 * and use this activity to launch it.
		 */
		public static ?string $launchInstanceId = ""; // i.e. "171"
		public static ?string $launchInstanceName = ""; // i.e. "Help Bot"
	

		public function __construct()
		{
			Main::$connection = new SDKConnection(new BotlibreCredentails(Main::$applicationId));
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


		public function testConnectUserAccount(): ?UserConfig
		{
			//TODO: Setup user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === ""){
				$debugComment = "<strong>Please fill the required data @ testConnectUserAccount() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			return $userConfig;
		}

		public function testSendChatMessage(): ?ChatResponse
		{
			//TODO: Setup message
			$config = new ChatConfig();
			// Add a message here: $config->message = "How are you?"
			$config->message = "Who are you?";
			$config->application = Main::$applicationId;
			// An ID of the bot example: ID: 165
			$config->instance = "165";
			if($config->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSendChatMessaage() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$response = Main::$connection->chat($config);
			return $response;
		}

		public function testFetchUserDetails(): ?UserConfig
		{
			//TODO: Setup user using 
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId;
			$userConfig->user = "user";
			$userConfig->password = Main::$password;
			if($userConfig->application === "" || $userConfig->password === "") {
				$debugComment = "<strong>Please fill the required data @ testFetchUserDetails() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->fetchUser($userConfig);
			return $userConfig;
		}

		public function testFetchForumPost(): ?ForumPostConfig
		{
			//TODO: Setup user using 
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->application = Main::$applicationId;
			$forumPostConfig->user = Main::$username;
			$forumPostConfig->password = Main::$password;
			//An exmaple: a ForumPost id 5012
			$forumPostConfig->id = "5012";
			if($forumPostConfig->application === "" || $forumPostConfig->user === "" || $forumPostConfig->password === "") {
				$debugComment = "<strong>Please fill the required data @ testFetchUserDetails() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$forumPostConfig = Main::$connection->fetchForumPost($forumPostConfig);
			return $forumPostConfig;
		}

		public function testCreateUser(): ?UserConfig //Tested
		{
			//TODO: Setup user 
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId;
			//Required data for testing.
			// $userConfig->user = "";
			// $userConfig->password = "";
			// $userConfig->hint = "";
			// $userConfig->name = "";
			// $userConfig->email = "";
			// $userConfig->website = "";
			// $userConfig->bio = "";
			// $userConfig->showName = true;
			if (isset($userConfig->user, $userConfig->password, $userConfig->hint, $userConfig->email, $userConfig->name)) {
				$userConfig = Main::$connection->createUser($userConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreateUser() in index.php</strong>";
				include "views/debug.php";
				return null;
			}

			return $userConfig;
		}


		public function testUpdateUser(): ?UserConfig
		{
			//TODO: Setup user 
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId;
			//Required data for testing.
			// $userConfig->user = "";
			// $userConfig->password = "";
			// $userConfig->hint = "";
			// $userConfig->name = "";
			// $userConfig->email = "";
			// $userConfig->website = "";
			// $userConfig->bio = "";
			// $userConfig->showName = true;
			if (isset($userConfig->user, $userConfig->password, $userConfig->hint, $userConfig->email, $userConfig->name)) {
				$userConfig = Main::$connection->updateUser($userConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testUpdateUser() in index.php</strong>";
				include "views/debug.php";
				return null;
			}

			return $userConfig;
		}

		public function testFetchImage()
		{
			//example: ...fetchImage("avatars/a667989.JPEG")
			$url = Main::$connection->fetchImage("avatars/a667989.JPEG");
			return "<img src='" . $url . "' alt='Image' >";
		}

		public function testCreateForumPost()
		{
			//TODO: Setup ForumPost
			$configForumPost = new ForumPostConfig();
			// $configForumPost->forum = ""; //Forum ID
			// $configForumPost->topic = "";
			// $configForumPost->details = "";
			// $configForumPost->tags = "";
			if (isset($configForumPost->forum, $configForumPost->topic, $configForumPost->details, $configForumPost->tags)) {
				$configForumPost = Main::$connection->createForumPost($configForumPost);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreatePostConfig() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			return $configForumPost;
		}

		public function testUpdateForumPost()
		{
			$configForumPost = new ForumPostConfig();
			// $configForumPost->forum = ""; //Forum ID
			// $configForumPost->topic = "";
			// $configForumPost->details = "";
			// $configForumPost->tags = "";
			if (isset($configForumPost->forum, $configForumPost->topic, $configForumPost->details, $configForumPost->tags)) {
				$configForumPost = Main::$connection->createForumPost($configForumPost);
			} else {
				$debugComment = "<strong>Please fill the required data @ testUpdatePostConfig() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			return $configForumPost;
		}

		public function testCreateChannelFileAttachment()
		{
			$mediaConfig = new MediaConfig();
			// $mediaConfig->name = ""; //File name i.e: file.txt
			// $mediaConfig->type = ""; //File Type i.e: text/plain
			// $mediaConfig->instance = ""; //Channel ID is required
			$file = ""; //Get file path
			if (isset($mediaConfig->name, $mediaConfig->type, $file, $mediaConfig->instance)) {
				$mediaConfig = Main::$connection->createChannelFileAttachment($file, $mediaConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreateChannelFileAttachment() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
		}

		public function testCreateChannelImageAttachment()
		{
			$mediaConfig = new MediaConfig();
			// $mediaConfig->name = ""; //File name i.e: file.txt
			// $mediaConfig->type = ""; //File Type i.e: image/jpeg
			// $mediaConfig->instance = ""; //Channel ID is required
			$file = ""; //Get file path
			if (isset($mediaConfig->name, $mediaConfig->type, $file, $mediaConfig->instance)) {
				$mediaConfig = Main::$connection->createChannelImageAttachment($file, $mediaConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreateChannelImageAttachment() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
		}

		public function testCreateReply()
		{
			$postConfig = new ForumPostConfig();
			// $postConfig->details = "";
			// $postConfig->forum = ""; Forum ID
			// $postConfig->parent = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->parent)) {
				$postConfig = Main::$connection->createForumPost($postConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreateReply() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			return $postConfig;
		}

		public function testCreateUserMessage()
		{
			//Template
			$writer .= "user: ";
			$writer .= "model: ";
			$writer .= "face: ";
			$writer .= "eyeColor: ";
			$writer .= "hairColor: ";
			$writer .= "hairStyle: ";
			$writer .= "body: ";
			$writer .= "cloths: ";
			$writer .= "pose: ";
			$writer .= "email: ";
			$writer .= "comments: ";

			$config = new UserMessageConfig();
			// $config->target = "admin";
			// $config->subject = "Avatar Request - Bot Libre";
			// $config->message = $writer;
	
			if (isset($config->target, $config->subject, $config->message)) {
				Main::$connection->createUserMessage($config);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreateUserMessage() in index.php</strong>";
				include "views/debug.php";
			}
		}

		public function testSaveResponse()
		{
			$response = new ResponseConfig();
			// $response->response = "";
			// $response->question = "";
			// $response->instance = "";
			// $response->type = "";
			if (isset($response->response, $response->question, $response->instance, $response->type)) {
				Main::$connection->saveResponse($response);
			} else {
				$debugComment = "<strong>Please fill the required data @ testSaveResponse() in index.php</strong>";
				include "views/debug.php";
			}

		}
	}
	?>

	<!--
		API Request and Response Section.
	-->
	<fieldset>
		<legend>API logs</legend>
		<?php
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
				case "update-user":
					$debugComment = "<strong>update an existing user.</strong>";
					include "views/debug.php";
					$userConfig = $main->testUpdateUser();
					break;
				case "fetch-image":
					$debugComment = "<strong>View an image.</strong>";
					$viewable_image = $main->testFetchImage();
					include "views/debug.php";
					break;
				case "create-forum-post":
					$debugComment = "<strong>Create a forum post.</strong>";
					$newPost = $main->testCreateForumPost();
					include "views/debug.php";
					break;
				case "update-forum-post":
					$debugComment = "<strong>Update a forum post.</strong>";
					$newPost = $main->testUpdateForumPost();
					include "views/debug.php";
					break;
				case "create-channel-file-attachment":
					$debugComment = "<strong>Create a channel file attachment.</strong>";
					$channelAttachment = $main->testCreateChannelFileAttachment();
					include "views/debug.php";
					break;
				case "create-channel-image-attachment":
					$debugComment = "<strong>Create a channel image attachment.</strong>";
					$channelAttachment = $main->testCreateChannelImageAttachment();
					include "views/debug.php";
					break;
				case "create-post-reply":
					$debugComment = "<strong>Create a post reply.</strong>";
					$channelAttachment = $main->testCreateReply();
					include "views/debug.php";
					break;
			}
		}
		?>
	</fieldset>

	<!-- 
		At the bottom of the page, there is a compact box that displays the returned readable 
		information resulting from the executed test requests.
	-->
	<div id="box" style="padding: 10px;">
		<strong>Details</strong>
		<br>
		<?php
		if (isset($userConfig)) {
			echo "<strong>User: </strong>" . $userConfig->user . "<br>";
			echo "<strong>Joined: </strong>" . $userConfig->joined . "<br>";
			echo "<strong>Connects: </strong>" . $userConfig->connects . "<br>";
			echo "<strong>Name: </strong>" . $userConfig->name . "<br>";
			if (isset($response)) {
				echo "<strong>Message: </strong>" . $response->message . "<br>";
			}
		} else if (isset($forumPost)) {
			echo "<strong>Topic: </strong>" . $forumPost->topic . "<br>";
			echo "<strong>Views: </strong>" . $forumPost->views . "<br>";
			echo "<strong>ThumbsUp: </strong>" . $forumPost->thumbsUp . "<br>";
			echo "<strong>Stars: </strong>" . $forumPost->stars . "<br>";
			echo "<strong>Creation Date: </strong>" . $forumPost->creationDate . "<br>";
		} else if (isset($newPost)) {
			echo "<strong>Topic: </strong>" . $newPost->topic . "<br>";
			echo "<strong>Summary: </strong>" . $newPost->summary . "<br>";
			echo "<strong>Details: </strong>" . $newPost->details . "<br>";
			echo "<strong>Forum: </strong>" . $newPost->forum . "<br>";
			echo "<strong>Creation Date: </strong>" . $newPost->creationDate . "<br>";
			echo "<strong>Tags: </strong>" . $newPost->tags . "<br>";
		} else {
			echo "There is no data to show yet.";
		}
		?>
	</div>
</body>

</html>