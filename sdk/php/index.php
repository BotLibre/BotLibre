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
				<option value="">/</option>
				<option value="connect">/check-user</option>
				<option value="chat">/post-chat</option>
				<option value="view-user">/view-user</option>
				<option value="check-forum-post">/check-forum-post</option>
				<option value="create-user">/create-user</option>
				<option value="update-user">/update-user</option>
				<option value="fetch-image">/fetch-image</option>
				<option value="create-forum-post">/create-forum-post</option>
				<option value="update-forum-post">/update-forum-post</option>
				<option value="delete-forum-post">/delete-forum-post</option>
				<option value="get-forum-posts">/get-forum-posts</option>
				<option value="create-channel-file-attachment">/create-channel-file-attachment</option>
				<option value="create-channel-image-attachment">/create-channel-image-attachment</option>
				<option value="create-post-reply">/create-post-reply</option>
				<option value="save-avatar-background">/save-avatar-background</option>
				<option value="delete-avatar-background">/delete-avatar-background</option>
				<option value="save-avatar-media">/save-avatar-media</option>
				<option value="delete-avatar-media">/delete-avatar-media</option>
				<option value="delete-response">/delete-response</option>
				<option value="flag">/flag</option>
				<option value="subscribe-forum-post">/subscribe-forum-post</option>
				<option value="unsubscribe-forum-post">/unsubscribe-forum-post</option>
				<option value="subscribe-forum">/subscribe-forum</option>
				<option value="unsubscribe-forum">/unsubscribe-forum</option>
				<option value="thumbs-up-forum">/thumbs-up-forum</option>
				<option value="thumbs-down-forum">/thumbs-down-forum</option>
				<option value="star-forum">/star-forum</option>
				<option value="avatar-message">/avatar-message</option>
				<option value="tts">/tts</option>
				<option value="get-admins-forum">/get-admins-forum</option>
				<option value="get-users-forum">/get-users-forum</option>
				<option value="get-categories">/get-categories</option>
				<option value="get-tags">/get-tags</option>
				<option value="get-templates">/get-templates</option>
				<option value="get-channel-bot-mode">/get-channel-bot-mode</option>
				<option value="save-channel-bot-mode">/save-channel-bot-mode</option>
				<option value="save-forum-bot-mode">/save-forum-bot-mode</option>
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
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testConnectUserAccount() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			return $userConfig;
		}

		/**
		 * Tested -> Worked
		 * Requirement: Bot ID, and a Message.
		 */

		public function testSendChatMessage(): ?ChatResponse
		{
			//TODO: Set message
			$config = new ChatConfig();
			// Add a message here: $config->message = "How are you?"
			$config->message = "Who are you?";
			$config->application = Main::$applicationId;
			// An ID of the bot example: ID: 165
			$config->instance = "165";
			if ($config->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSendChatMessaage() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$response = Main::$connection->chat($config);
			return $response;
		}

		/**
		 * Tested -> Worked
		 * Requirements: 
		 * 1. Application ID
		 * 2. Username
		 * 3. Password
		 */
		public function testFetchUserDetails(): ?UserConfig
		{
			//TODO: Set user using 
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId;
			$userConfig->user = "user";
			$userConfig->password = Main::$password;
			if ($userConfig->application === "" || $userConfig->password === "") {
				$debugComment = "<strong>Please fill the required data @ testFetchUserDetails() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->fetchUser($userConfig);
			return $userConfig;
		}

		/**
		 * Tested -> Worked
		 * Requirements
		 * 1. Application ID
		 * 2. User
		 * 3. Password
		 * 4. Forum Post ID
		 */

		public function testFetchForumPost(): ?ForumPostConfig
		{
			//TODO: Set user using 
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->application = Main::$applicationId;
			$forumPostConfig->user = Main::$username;
			$forumPostConfig->password = Main::$password;
			// $forumPostConfig->id = "5012";
			if ($forumPostConfig->application === "" || $forumPostConfig->user === "" || $forumPostConfig->password === "") {
				$debugComment = "<strong>Please fill the required data @ testFetchUserDetails() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$forumPostConfig = Main::$connection->fetchForumPost($forumPostConfig);
			return $forumPostConfig;
		}

		/**
		 * Tested-> Worked
		 * Requirments:
		 * 1. User
		 * 2. Password
		 * 3. Name
		 * 4. Email
		 */

		public function testCreateUser(): ?UserConfig
		{
			//TODO: Set user 
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
			//TODO: Set user 
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

		/**
		 * Tested -> Worked
		 * Requirements: 
		 * 1. User must be logged in
		 * 2. Forum ID
		 * 3. Topic and Details.
		 */

		public function testCreateForumPost()
		{
			//TODO: Set user
			$this->testConnectUserAccount();
			//TODO: Set ForumPostConfig
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			// $postConfig->topic = "";
			// $postConfig->details = "";
			// $postConfig->tags = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->topic)) {
				$postConfig = Main::$connection->createForumPost($postConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testCreatePostConfig() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			return $postConfig;
		}

		/**
		 * Tested -> Worked
		 * Requirements: 
		 * 1. User must be logged in
		 * 2. Forum ID
		 * 3. Topic and Details.
		 */

		public function testUpdateForumPost()
		{
			//TODO: Set user
			$this->testConnectUserAccount();
			//TODO: Set ForumPostConfig
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			// $postConfig->topic = "";
			// $postConfig->details = "";
			// $postConfig->tags = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->topic)) {
				$configForumPost = Main::$connection->createForumPost($postConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testUpdateForumPost() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			return $postConfig;
		}


		public function testDeleteForumPost(): bool
		{
			//TODO: Set user
			$this->testConnectUserAccount();
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			if (isset($postConfig->forum)) {
				Main::$connection->deleteForumPost($postConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testDeleteForumPost() in index.php</strong>";
				include "views/debug.php";
				return false;
			}
			return true;
		}

		//Tested -> Failed, returned file name is null.
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


		/**
		 * Tested -> Worked
		 * Requirements: 
		 * 1. User must be logged in
		 * 2. Forum ID
		 * 3. Topic and Details.
		 */
		public function testCreateReply()
		{
			//TODO: Set user
			$this->testConnectUserAccount();
			//TODO: Set ForumPostConfig
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			// $postConfig->parent = ""; //Parent Post
			// $postConfig->details = "";
			// $postConfig->tags = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->parent)) {
				$postConfig = Main::$connection->createReply($postConfig);
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

		/**
		 * Tested -> Failed, Result: File Name is null.
		 * 
		 * Function: Updting a background image of an Avatar.
		 */
		public function testSaveAvatarBackground()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSaveAvatarBackground() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			$avatarConfig = new AvatarMedia();
			//image path directory
			$imagePath = $_SERVER['DOCUMENT_ROOT'] . DIRECTORY_SEPARATOR . 'imgtest.jpg';

			// echo "token: " . $userConfig->token . "<br>";
			// echo "image path: " . $imagePath;
	
			$avatarConfig->user = $userConfig->user;
			$avatarConfig->token = $userConfig->token;


			// $avatarConfig->name = "imgtest.jpg";
			// $avatarConfig->type = "image/jpeg";
			// $avatarConfig->application = Main::$applicationId;
			// $avatarConfig->instance = ""; //instance id
	
			if (isset($avatarConfig->application, $avatarConfig->name, $avatarConfig->instance, $avatarConfig->type)) {
				Main::$connection->saveAvatarBackground($imagePath, $avatarConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testSaveAvatarBackground() in index.php</strong>";
				include "views/debug.php";
			}
		}

		public function testDeleteResponse()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testDeleteResponse() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			$config = new ResponseConfig();

			$config->user = $userConfig->user;
			$config->token = $userConfig->token;


			// $config->instance = ""; //response id
			// $config->type = ""; //i.e conversation, greeting, response, flagged.
			// $config->responseId = ""; // response ID.
			// $config->questionId = ""; // question ID.
	
			if (isset($config->instance, $config->type, $config->responseId, $condfig->questionId)) {
				Main::$connection->deleteResponse($config);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testDeleteResponse() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testDeleteAvatarMedia()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testDeleteAvatarMedia() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	

			$avatarMedia = new AvatarMedia();

			$avatarMedia->user = $userConfig->user;
			$avatarMedia->token = $userConfig->token;

			// $avatarMedia->mediaId = ""; //media ID.
			// $avatarMedia->instance = "";//Instance ID.
			if (isset($avatarMedia->instance, $avatarMedia->mediaId)) {
				Main::$connection->deleteAvatarMedia($avatarMedia);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testDeleteAvatarMedia() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}


		public function testSaveAvatarMedia()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSaveAvatarMedia() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	

			$avatarMedia = new AvatarMedia();

			$avatarMedia->user = $userConfig->user;
			$avatarMedia->token = $userConfig->token;

			// $avatarMedia->mediaId = ""; //media ID.
			// $avatarMedia->type=""; //media type.
			// $avatarMedia->instance = "";//Instance ID.
			// $avatarMedia->name = ""; //Media name.
			// $avatarMedia->emotions = ""; //emotions
			// $avatarMedia->actions = "";
			// $avatarMedia->poses = "";
			// $avatarMedia->talking ="";
			// $avatarMedia->hd = false; 
	
			if (isset($avatarMedia->instance, $avatarMedia->type, $avatarMedia->name, $avatarMedia->emotions, $avatarMedia->talking, $avatarMedia->poses, $avatarMedia->actions, $avatarMedia->mediaId)) {
				Main::$connection->saveAvatarMedia($avatarMedia);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testSaveAvatarMedia() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}
		public function testDeleteAvatarBackground()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testDeleteAvatarBackground() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	

			$avatarConfig = new AvatarConfig();

			$avatarConfig->user = $userConfig->user;
			$avatarConfig->token = $userConfig->token;

			//$avatarConfig->id = ""; //Avatar id.
	
			if (isset($avatarConfig->id)) {
				Main::$connection->deleteAvatarBackground($avatarConfig);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testDeleteAvatarBackground() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testFlagInstance()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testFlagInstance() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			//This can be applied on either (GraphicConfig, ChannelConfig, DomainConfig ..etc);
			$avatarConfig = new AvatarConfig();
			$avatarConfig->user = $userConfig->user;
			$avatarConfig->token = $userConfig->token;
			$avatarConfig->flaggedReason = "Reason for flagging. " . $avatarConfig->getType();
			//$avatarConfig->id = ""; //Avatar id.
	
			if (isset($avatarConfig->id, $avatarConfig->flaggedReason)) {
				Main::$connection->flag($avatarConfig);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testFlagInstance() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}


		public function testFlagUser()
		{
			//TODO: Set user
			// $userConfig = new UserConfig();
			// $userConfig->application = Main::$applicationId; //application id, username and password are required.
			// $userConfig->user = Main::$username;
			// $userConfig->password = Main::$password;
			// if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
			// 	$debugComment = "<strong>Please fill the required data @ testFlagUser() in index.php</strong>";
			// 	include "views/debug.php";
			// 	return null;
			// }
			// $userConfig = Main::$connection->connect($userConfig);
	

			$viewUser = new UserConfig();
			// $viewUser->user = "";
			// $viewUser->instance = "";
			// $viewUser->flaggedReason = "Reason for flagging. ";

			if (isset($viewUser->user, $viewUser->flaggedReason, $viewUser->instance)) {
				Main::$connection->flagUser($viewUser);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testFlagUser() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}


		public function testSubscribeForumPost()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSubscribeForumPost() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->user = $userConfig->user;
			$forumPostConfig->token = $userConfig->token;
			//$forumPostConfig->id = ""; //ForumPostConfig id.
			if (isset($forumPostConfig->id)) {
				Main::$connection->subscribeForumPost($forumPostConfig);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testSubscribeForumPost() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testUnsubscribeForumPost()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testUnsubscribeForumPost() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->user = $userConfig->user;
			$forumPostConfig->token = $userConfig->token;
			//$forumPostConfig->id = ""; //ForumPostConfig id.
			if (isset($forumPostConfig->id)) {
				Main::$connection->unsubscribeForumPost($forumPostConfig);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testUnsubscribeForumPost() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		/**
		 * Tested -> Worked
		 * Requirement: User login.
		 */

		public function testGetForumPosts()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testGetForumPosts() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
			$browseForumPosts = new BrowseConfig();
			$browseForumPosts->user = $userConfig->user;
			$browseForumPosts->token = $userConfig->token;
			$browseForumPosts->type = "Post";
			$browseForumPosts->typeFilter = "Public";
			$browseForumPosts->sort = "date";
			if (isset($browseForumPosts->type, $browseForumPosts->typeFilter, $browseForumPosts->sort)) {
				return Main::$connection->getPosts($browseForumPosts);
			} else {
				$debugComment = "<strong>Please fill the required data @ testGetForumPosts() in index.php</strong>";
				include "views/debug.php";
			}
		}

		public function testSubscribeForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSubscribeForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			$forumPost = new ForumConfig();
			$forumPost->user = $userConfig->user;
			$forumPost->token = $userConfig->token;
			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->subscribeForum($forumPost);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testSubscribeForum() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testUnsubscribeForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testUnsubscribeForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			$forumPost = new ForumConfig();
			$forumPost->user = $userConfig->user;
			$forumPost->token = $userConfig->token;
			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->unsubscribeForum($forumPost);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testUnsubscribeForum() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testThumbsUpForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testThumbsUpForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function thumbsUp(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();
			$forumPost->user = $userConfig->user;
			$forumPost->token = $userConfig->token;
			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->thumbsUp($forumPost);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testThumbsUpForum() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testThumbsDownForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testThumbsDownForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function thumbsDown(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();
			$forumPost->user = $userConfig->user;
			$forumPost->token = $userConfig->token;
			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->thumbsDown($forumPost);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testThumbsDownForum() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testStarForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testStarForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);
			//Now we can use the token from the user after User Login.
	
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function testStarForum(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();
			$forumPost->user = $userConfig->user;
			$forumPost->token = $userConfig->token;
			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->star($forumPost);
				return true;
			} else {
				$debugComment = "<strong>Please fill the required data @ testStarForum() in index.php</strong>";
				include "views/debug.php";
			}
			return false;
		}

		public function testAvatarMessage()
		{
			$avatarMessage = new AvatarMessage();
			// $avatarMessage->application = "";
			// $avatarMessage->instance = "";
			// $avatarMessage->avatar = "";
			// $avatarMessage->speak = true;
			// $avatarMessage->message = "";
			// $avatarMessage->emote = "";
			// $avatarMessage->action = "";
			// $avatarMessage->pose = "";
			// $avatarMessage->format = "";
			// $avatarMessage->hd = false;
			if (isset($avatarMessage->instance, $avatarMessage->avatar, $avatarMessage->message, $avatarMessage->speak)) {
				if ($avatarMessage->speak) {
					$avatarMessage->voice = "";
				}
				return Main::$connection->avatarMessage($avatarMessage);
			} else {
				$debugComment = "<strong>Please fill the required data @ testAvatarMessage() in index.php</strong>";
				include "views/debug.php";
			}

		}

		public function testTTS()
		{
			$ttsConfig = new Speech();
			// $ttsConfig->voice = "";
			// $ttsConfig->text= "";
			// $ttsConfig->mod = "";
			if (isset($ttsConfig->voice, $ttsConfig->text, $ttsConfig->mod)) {
				return Main::$connection->tts($ttsConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testTTS() in index.php</strong>";
				include "views/debug.php";
			}
		}

		public function testGetAdminsForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testGetAdminsForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			//Connecting to a forum and establish validation
	
			$forumConfig = new ForumConfig();
			$forumConfig->application = $this->applicationId;
			$forumConfig->user = $userConfig->user;
			$forumConfig->token = $userConfig->token;
			// $forumConfig->id = ""; //Forum ID
	

			if (isset($forumConfig->id, $forumConfig->user, $forumConfig->token)) {
				return Main::$connection->getAdmins($forumConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testGetAdminsForum() in index.php</strong>";
				include "views/debug.php";
			}

		}



		public function testGetUsersForum()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testGetUsersForum() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			//Connecting to a forum and establish validation
	
			$forumConfig = new ForumConfig();
			$forumConfig->application = $this->applicationId;
			$forumConfig->user = $userConfig->user;
			$forumConfig->token = $userConfig->token;
			// $forumConfig->id = ""; //Forum ID
	

			if (isset($forumConfig->id, $forumConfig->user, $forumConfig->token)) {
				return Main::$connection->getUsersOfType($forumConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testGetUsersForum() in index.php</strong>";
				include "views/debug.php";
			}

		}

		/**
		 * Tested -> Worked
		 * Requirement: User login.
		 */
		public function testGetCategories()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testGetCategories() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			$contentConfig = new ContentConfig();
			$contentConfig->user = $userConfig->user;
			$contentConfig->token = $userConfig->token;
			$contentConfig->type = "Bot"; //Forum, Channel, Aavatar, Script, Domain.
			if (isset($contentConfig->type)) {
				return Main::$connection->getCategories($contentConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testGetCategories() in index.php</strong>";
				include "views/debug.php";
			}
			return null;
		}
		/**
		 * Tested -> Worked
		 * Requirement: User login.
		 */
		public function testGetTags()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testGetTags() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			$contentConfig = new ContentConfig();
			$contentConfig->user = $userConfig->user;
			$contentConfig->token = $userConfig->token;
			$contentConfig->type = "Bot"; //Forum, Channel, Aavatar, Script, Domain.
			if (isset($contentConfig->type)) {
				return Main::$connection->getCategories($contentConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testGetTags() in index.php</strong>";
				include "views/debug.php";
			}
			return null;
		}

		public function testGetTemplates()
		{
			return Main::$connection->getTemplates();
		}

		public function testGetChannelBotMode()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testGetChannelBotMode() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			$channelConfig = new ChannelConfig();
			$channelConfig->user = $userConfig->user;
			$channelConfig->token = $userConfig->token;
			// $channelConfig->id = "";
	
			if (isset($channelConfig->user, $channelConfig->token, $channelConfig->id)) {
				return Main::$connection->getChannelBotMode($channelConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testGetChannelBotMode() in index.php</strong>";
				include "views/debug.php";
			}
			return null;
		}

		public function testSaveChannelBotMode()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSaveChannelBotMode() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			$botModeConfig = new BotModeConfig();
			$botModeConfig->user = $userConfig->user;
			$botModeConfig->token = $userConfig->token;
			// $botModeConfig->instance = "";
			// $botModeConfig->bot = "";
			// $botModeConfig->mode = "";
	
			if (isset($botModeConfig->instance, $botModeConfig->user, $botModeConfig->token, $botModeConfig->bot, $botModeConfig->mode)) {
				return Main::$connection->saveChannelBotMode($botModeConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testSaveChannelBotMode() in index.php</strong>";
				include "views/debug.php";
			}
			return null;
		}


		public function testSaveForumBotMode()
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				$debugComment = "<strong>Please fill the required data @ testSaveForumBotMode() in index.php</strong>";
				include "views/debug.php";
				return null;
			}
			$userConfig = Main::$connection->connect($userConfig);

			$botModeConfig = new BotModeConfig();
			$botModeConfig->user = $userConfig->user;
			$botModeConfig->token = $userConfig->token;
			// $botModeConfig->instance = "";
			// $botModeConfig->bot = "";
			// $botModeConfig->mode = "";
	
			if (isset($botModeConfig->instance, $botModeConfig->user, $botModeConfig->token, $botModeConfig->bot, $botModeConfig->mode)) {
				return Main::$connection->saveForumBotMode($botModeConfig);
			} else {
				$debugComment = "<strong>Please fill the required data @ testSaveForumBotMode() in index.php</strong>";
				include "views/debug.php";
			}
			return null;
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
				case "delete-forum-post":
					$debugComment = "<strong>Delete forum post.</strong>";
					$pass = $main->testDeleteForumPost();
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
				case "save-avatar-background":
					$debugComment = "<strong>Save - Upload avatar's background.</strong>";
					$saveAvatar = $main->testSaveAvatarBackground();
					include "views/debug.php";
					break;
				case "delete-response":
					$debugComment = "<strong>Permanently delete the response, greetings, or default response.</strong>";
					$pass = $main->testDeleteResponse();
					include "views/debug.php";
					break;
				case "delete-avatar-media":
					$debugComment = "<strong>Delete the avatar media.</strong>";
					$pass = $main->testDeleteAvatarMedia();
					include "views/debug.php";
					break;
				case "delete-avatar-background":
					$debugComment = "<strong>Delete the avatar background.</strong>";
					$pass = $main->testDeleteAvatarBackground();
					include "views/debug.php";
					break;
				case "save-avatar-media":
					$debugComment = "<strong>Save the avatar media.</strong>";
					$pass = $main->testSaveAvatarMedia();
					include "views/debug.php";
					break;
				case "flag":
					$debugComment = "<strong>Flag an instance.</strong>";
					$pass = $main->testFlagInstance();
					include "views/debug.php";
					break;
				case "subscribe-forum-post":
					$debugComment = "<strong>Subscribe a forum post.</strong>";
					$pass = $main->testSubscribeForumPost();
					include "views/debug.php";
					break;
				case "unsubscribe-forum-post":
					$debugComment = "<strong>Unsubscribe a forum post.</strong>";
					$pass = $main->testUnsubscribeForumPost();
					include "views/debug.php";
					break;
				case "subscribe-forum":
					$debugComment = "<strong>Subscribe to a forum.</strong>";
					$pass = $main->testSubscribeForum();
					include "views/debug.php";
					break;
				case "get-forum-posts":
					$debugComment = "<strong>Return the list of forum posts for the forum browse criteria.</strong>";
					$list = $main->testGetForumPosts();
					include "views/debug.php";
					break;
				case "unsubscribe-forum":
					$debugComment = "<strong>Unsubscribe to a forum.</strong>";
					$pass = $main->testUnsubscribeForum();
					include "views/debug.php";
					break;
				case "thumbs-up-forum":
					$debugComment = "<strong>Thumbs up to a forum.</strong>";
					$pass = $main->testThumbsUpForum();
					include "views/debug.php";
					break;
				case "thumbs-down-forum":
					$debugComment = "<strong>Thumbs down to a forum.</strong>";
					$pass = $main->testThumbsDownForum();
					include "views/debug.php";
					break;
				case "star-forum":
					$debugComment = "<strong>Evaluate a forum by placing a number of stars.</strong>";
					$pass = $main->testStarForum();
					include "views/debug.php";
					break;
				case "flag-user":
					$debugComment = "<strong>Flag user account.</strong>";
					$pass = $main->testFlagUser();
					include "views/debug.php";
					break;
				case "avatar-message":
					$debugComment = "<strong>Process the avatar message and return the avatars response.</strong>";
					$avatarMessage = $main->testAvatarMessage();
					include "views/debug.php";
					break;
				case "tts":
					$debugComment = "<strong>Process the speech message and return the server generate text-to-speech audio file.</strong>";
					$tts = $main->testTTS();
					include "views/debug.php";
					break;
				case "get-admins-forum":
					$debugComment = "<strong>Return the administrators of the content.</strong>";
					$list = $main->testGetAdminsForum();
					include "views/debug.php";
					break;
				case "get-users-forum":
					$debugComment = "<strong>Return the users of the content.</strong>";
					$list = $main->testGetUsersForum();
					include "views/debug.php";
					break;
				case "get-categories":
					$debugComment = "<strong>Return the administrators of the content.</strong>";
					$list = $main->testGetCategories();
					include "views/debug.php";
					break;
				case "get-tags":
					$debugComment = "<strong>Return the list of tags for the type, and domain.</strong>";
					$list = $main->testGetTags();
					include "views/debug.php";
					break;
				case "get-templates":
					$debugComment = "<strong>Return the list of bot templates.</strong>";
					$list = $main->testGetTemplates();
					include "views/debug.php";
					break;
				case "get-channel-bot-mode":
					$debugComment = "<strong>Return the channel's bot configuration.</strong>";
					$botModeConfig = $main->testGetChannelBotMode();
					include "views/debug.php";
					break;
				case "save-channel-bot-mode":
					$debugComment = "<strong>Save the channel's bot configuration.</strong>";
					$botModeConfig = $main->testSaveChannelBotMode();
					include "views/debug.php";
					break;
				case "save-forum-bot-mode":
					$debugComment = "<strong>Save the channel's bot configuration.</strong>";
					$botModeConfig = $main->testSaveForumBotMode();
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
			if (isset($newPost->tags)) {
				echo "<strong>Tags: </strong>" . $newPost->tags . "<br>";
			}
		} else if (isset($pass)) {
			echo "<strong>Passed: </strong>" . $pass . "<br>";
		} else if (isset($avatarMessage)) {
			echo "<strong>Instance: </strong>" . $avatarMessage->instance . "<br>";
			echo "<strong>Avatar: </strong>" . $avatarMessage->avatar . "<br>";
			echo "<strong>Message: </strong>" . $avatarMessage->message . "<br>";
		} else if (isset($tts)) {
			echo "<strong>Speech: </strong>" . $tts . "<br>";
		} else if (isset($list)) {
			print_r($list);
		} else if (isset($botModeConfig)) {
			echo "<strong>Type: </strong>" . $botModeConfig->type . "<br>";
			echo "<strong>Mode: </strong>" . $botModeConfig->mode . "<br>";
			echo "<strong>Bot: </strong>" . $botModeConfig->bot . "<br>";
		} else {
			echo "There is no data to show yet.";
		}
		?>
	</div>
</body>

</html>