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
				<option value="connect">check-user</option>
				<option value="chat">post-chat</option>
				<option value="view-user">view-user</option>
				<option value="check-forum-post">check-forum-post</option>
				<option value="create-user">create-user</option>
				<option value="update-user">update-user</option>
				<option value="fetch-image">fetch-image</option>
				<option value="create-forum-post">create-forum-post</option>
				<option value="update-forum-post">update-forum-post</option>
				<option value="delete-forum-post">delete-forum-post</option>
				<option value="get-forum-posts">get-forum-posts</option>
				<option value="create-channel-file-attachment">create-channel-file-attachment</option>
				<option value="create-channel-image-attachment">create-channel-image-attachment</option>
				<option value="create-post-reply">create-post-reply</option>
				<option value="save-avatar-background">save-avatar-background</option>
				<option value="delete-avatar-background">delete-avatar-background</option>
				<option value="save-avatar-media">save-avatar-media</option>
				<option value="delete-avatar-media">delete-avatar-media</option>
				<option value="delete-response">delete-response</option>
				<option value="save-response">save-response</option>
				<option value="flag">flag</option>
				<option value="subscribe-forum-post">subscribe-forum-post</option>
				<option value="unsubscribe-forum-post">unsubscribe-forum-post</option>
				<option value="subscribe-forum">subscribe-forum</option>
				<option value="unsubscribe-forum">unsubscribe-forum</option>
				<option value="thumbs-up-forum">thumbs-up-forum</option>
				<option value="thumbs-down-forum">thumbs-down-forum</option>
				<option value="star-forum">star-forum</option>
				<option value="avatar-message">avatar-message</option>
				<option value="tts">tts</option>
				<option value="get-admins-forum">get-admins-forum</option>
				<option value="get-users-forum">get-users-forum</option>
				<option value="get-categories">get-categories</option>
				<option value="get-tags">get-tags</option>
				<option value="get-templates">get-templates</option>
				<option value="get-channel-bot-mode">get-channel-bot-mode</option>
				<option value="save-channel-bot-mode">save-channel-bot-mode</option>
				<option value="save-forum-bot-mode">save-forum-bot-mode</option>
				<option value="save-learning">save-learning</option>
				<option value="save-voice">save-voice</option>
				<option value="save-bot-avatar">save-bot-avatar</option>
				<option value="train-instance">train-instance</option>
				<option value="user-admin">user-admin</option>
				<option value="create-avatar-media">create-avatar-media</option>
				<option value="create-avatar">create-avatar</option>
				<option value="create-graphic-media">create-graphic-media</option>
				<option value="update-user-icon">update-user-icon</option>
				<option value="get-forum-bot-mode">get-forum-bot-mode</option>
				<option value="get-voice">get-voice</option>
				<option value="get-default-responses">get-default-responses</option>
				<option value="get-greetings">get-greetings</option>
				<option value="get-responses">get-responses</option>
				<option value="get-conversations">get-conversations</option>
				<option value="get-learning">get-learning</option>
				<option value="browse">browse</option>
				<option value="get-avatar-media">get-avatar-media</option>
				<option value="get-script-source">get-script-source</option>
				<option value="save-script-source">save-script-source</option>
				<option value="get-bot-script-source">get-bot-script-source</option>
				<option value="get-bot-scripts">get-bot-scripts</option>
				<option value="import-bot-script">import-bot-script</option>
				<option value="import-bot-log">import-bot-log</option>
				<option value="save-bot-script-source">save-bot-script-source</option>
				<option value="delete-bot-script">delete-bot-script</option>
				<option value="up-bot-script">up-bot-script</option>
				<option value="down-bot-script">down-bot-script</option>
			</select>
			<br><br>
			<input type="submit" value="TEST">

			<?php
			if (isset($_GET["api"])) {
				echo "<strong>Selected API : </strong>" . "<u>" . $_GET["api"] . "</u>";
			} else {
				echo "<strong>Nothing is selected.</strong>";
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
				Utils::includeMessage("[Main] initializing SDKConnection.", null, Main::$connection);
				Main::$showAds = false;
				Main::$connection->setDebug(true);
			}

		}

		/**
		 * Tested: passed
		 */
		public function testConnectUserAccount(): bool
		{
			//TODO: Set user
			$userConfig = new UserConfig();
			$userConfig->application = Main::$applicationId; //application id, username and password are required.
			$userConfig->user = Main::$username;
			$userConfig->password = Main::$password;
			if ($userConfig->user === "" || $userConfig->password === "" || $userConfig->application === "") {
				Utils::includeMessage("<strong>Please fill the required data @ testConnectUserAccount() in index.php</strong>");
				return false;
			}
			Main::$connection->connect($userConfig);
			return true;
		}

		/**
		 * Tested -> passed
		 * Requirement: Bot ID, and a Message.
		 */

		public function testSendChatMessage(): ?ChatResponse
		{
			if (!$this->testConnectUserAccount()) {
				return null;
			}
			//TODO: Set message
			$config = new ChatConfig();
			// Add a message here: $config->message = "How are you?"
			$config->message = "How are you?";

			// An ID of the bot example: ID: 165
			$config->instance = "165";
			if (isset($config->instance)) {
				return Main::$connection->chat($config);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSendChatMessaage() in index.php</strong>");
			return null;
		}

		/**
		 * Tested -> passed
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
				Utils::includeMessage("<strong>Please fill the required data @ testFetchUserDetails() in index.php</strong>");
				return null;
			}
			return Main::$connection->fetchUser($userConfig);
		}

		/**
		 * Tested -> passed
		 * Requirements
		 * 1. Application ID
		 * 2. User
		 * 3. Password
		 * 4. Forum Post ID
		 */

		public function testFetchForumPost(): ?ForumPostConfig
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return null;
			}
			$forumPostConfig = new ForumPostConfig();
			// $forumPostConfig->id = "";
			if (isset($forumPostConfig->id)) {
				return Main::$connection->fetchForumPost($forumPostConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testFetchUserDetails() in index.php</strong>");
			return null;
		}

		/**
		 * Tested-> passed
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
			// $userConfig->application = Main::$applicationId;
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
				Utils::includeMessage("<strong>Please fill the required data @ testCreateUser() in index.php</strong>");
				return null;
			}

			return $userConfig;
		}

		/**
		 * Tested: passed
		 * Requirements: Must be logged in to update current user data, i.e name, email and password.
		 */
		public function testUpdateUser(): ?UserConfig
		{
			//TODO: Set user 
	
			if (!$this->testConnectUserAccount()) {
				return null;
			}
			$userConfig = Main::$connection->getUser(); //Updating current user account.
			//Required data for testing.
			// $userConfig->password = "";
			// $userConfig->hint = "testAccount";
			// $userConfig->name = "";
			// $userConfig->email = "";
			// $userConfig->website = "";
			// $userConfig->bio = "Test update user";
			// $userConfig->showName = true;
			if (isset($userConfig->hint, $userConfig->bio)) {
				$userConfig = Main::$connection->updateUser($userConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testUpdateUser() in index.php</strong>");
				return null;
			}

			return $userConfig;
		}

		//Tested
		public function testFetchImage()
		{
			//example: ...fetchImage("avatars/a667989.JPEG")
			$url = Main::$connection->fetchImage("avatars/a667989.JPEG");
			return "<img src='" . $url . "' alt='Image' >";
		}

		/**
		 * Tested -> passed
		 * Requirements: 
		 * 1. User must be logged in
		 * 2. Forum ID
		 * 3. Topic and Details.
		 */

		public function testCreateForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//TODO: Set ForumPostConfig
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			// $postConfig->topic = "";
			// $postConfig->details = "";
			// $postConfig->tags = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->topic)) {
				$postConfig = Main::$connection->createForumPost($postConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testCreatePostConfig() in index.php</strong>");
				return null;
			}
			return $postConfig;
		}

		/**
		 * Tested -> passed
		 * Requirements: 
		 * 1. User must be logged in
		 * 2. Forum ID
		 * 3. Topic and Details.
		 */

		public function testUpdateForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//TODO: Set ForumPostConfig
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			// $postConfig->topic = "";
			// $postConfig->details = "";
			// $postConfig->tags = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->topic)) {
				return Main::$connection->updateForumPost($postConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testUpdateForumPost() in index.php</strong>");
				return null;
			}
		}

		/**
		 * Tested: passed
		 * Requirements: The ID of the post and the ID of the forum containing the post.
		 */
		public function testDeleteForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //required forum id.
			// $postConfig->id = ""; // required post id to be deleted.
			if (isset($postConfig->forum, $postConfig->id)) {
				Main::$connection->deleteForumPost($postConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testDeleteForumPost() in index.php</strong>");
			return false;
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
				return Main::$connection->createChannelFileAttachment($file, $mediaConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateChannelFileAttachment() in index.php</strong>");
			return null;
		}

		public function testCreateChannelImageAttachment()
		{
			$mediaConfig = new MediaConfig();
			// $mediaConfig->name = ""; //File name i.e: file.txt
			// $mediaConfig->type = ""; //File Type i.e: image/jpeg
			// $mediaConfig->instance = ""; //Channel ID is required
			$file = ""; //Get file path
			if (isset($mediaConfig->name, $mediaConfig->type, $file, $mediaConfig->instance)) {
				return Main::$connection->createChannelImageAttachment($file, $mediaConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateChannelImageAttachment() in index.php</strong>");
			return null;
		}


		/**
		 * Tested -> passed
		 * Requirements: 
		 * 1. User must be logged in
		 * 2. Forum ID
		 * 3. Topic and Details.
		 */
		public function testCreateReply()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//TODO: Set ForumPostConfig
			$postConfig = new ForumPostConfig();
			// $postConfig->forum = ""; //Forum ID
			// $postConfig->parent = ""; //Parent Post
			// $postConfig->details = "";
			// $postConfig->tags = "";
			if (isset($postConfig->details, $postConfig->forum, $postConfig->parent)) {
				return Main::$connection->createReply($postConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateReply() in index.php</strong>");
			return null;
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
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateUserMessage() in index.php</strong>");
			return false;
		}


		/**
		 * Tested -> passed for creating a new question and a response.
		 */
		public function testSaveResponse()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$response = new ResponseConfig();
			$response->instance = ""; //Bot ID
			// $response->responseId ="";
			// $response->questionId ="";
			// $response->type = "";
			// $response->correctness = "";
			// $response->flagged = "";
			$response->question = "Test Question";
			$response->response = "Test Response";
			// $response->topic = "";
			// $response->label = "";
			// $response->keywords = "";
			// $response->required = "";
			// $response->emotions = "like";
			// $response->actions = "";
			// $response->poses = "";
			// $response->previous = "";
			// $response->onRepeat = "";
			// $response->command = "";
			// $response->noRepeat = "";
			// $response->requirePrevious = "";
	
			if (isset($response->instance)) {
				Main::$connection->saveResponse($response);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveResponse() in index.php</strong>");
			return false;
		}

		/**
		 * Tested -> Failed, Result: File Name is null.
		 * 
		 * Function: Updting a background image of an Avatar.
		 */
		public function testSaveAvatarBackground()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			$avatarConfig = new AvatarMedia();
			//image path directory
			$imagePath = $_SERVER['DOCUMENT_ROOT'] . DIRECTORY_SEPARATOR . 'imgtest.jpg';

			// echo "image path: " . $imagePath;
	

			// $avatarConfig->name = "imgtest.jpg";
			// $avatarConfig->type = "image/jpeg";
			// $avatarConfig->application = Main::$applicationId;
			// $avatarConfig->instance = ""; //instance id
	
			if (isset($avatarConfig->application, $avatarConfig->name, $avatarConfig->instance, $avatarConfig->type)) {
				Main::$connection->saveAvatarBackground($imagePath, $avatarConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveAvatarBackground() in index.php</strong>");
			return false;
		}


		//Tested -> passed
		//Requirement: Bot instance id, type: response, response id and question id.
	
		public function testDeleteResponse()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			$config = new ResponseConfig();


			// $config->instance = ""; //response id
			// $config->type = "response"; //i.e conversation, greeting, response, flagged.
			// $config->responseId = ""; // response ID.
			// $config->questionId = ""; // question ID.
	
			// $config->question = "";
			// $config->response = "";
	
			if (isset($config->instance, $config->responseId)) {
				Main::$connection->deleteResponse($config);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testDeleteResponse() in index.php</strong>");
			return false;
		}


		/**
		 * Tested: Passed
		 */
		public function testDeleteAvatarMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
			$avatarMedia = new AvatarMedia();

			// $avatarMedia->mediaId = ""; //media ID.
			// $avatarMedia->instance = "";//Instance ID.
			if (isset($avatarMedia->instance, $avatarMedia->mediaId)) {
				Main::$connection->deleteAvatarMedia($avatarMedia);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testDeleteAvatarMedia() in index.php</strong>");
			return false;
		}


		public function testSaveAvatarMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	

			$avatarMedia = new AvatarMedia();

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
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveAvatarMedia() in index.php</strong>");
			return false;
		}



		/**
		 * Tested: passed
		 */
		public function testDeleteAvatarBackground()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
		

			$avatarConfig = new AvatarConfig();
			// $avatarConfig->id = ""; //Avatar id.
	
			if (isset($avatarConfig->id)) {
				Main::$connection->deleteAvatarBackground($avatarConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testDeleteAvatarBackground() in index.php</strong>");
			return false;

		}

		public function testFlagInstance()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			//This can be applied on either (GraphicConfig, ChannelConfig, DomainConfig ..etc);
			$avatarConfig = new AvatarConfig();
			$avatarConfig->flaggedReason = "Reason for flagging. " . $avatarConfig->getType();
			//$avatarConfig->id = ""; //Avatar id.
	
			if (isset($avatarConfig->id, $avatarConfig->flaggedReason)) {
				Main::$connection->flag($avatarConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testFlagInstance() in index.php</strong>");
			return false;
		}


		public function testFlagUser()
		{
			//TODO: Set user
			
			$viewUser = new UserConfig();
			// $viewUser->user = "";
			// $viewUser->instance = "";
			// $viewUser->flaggedReason = "Reason for flagging. ";
	
			if (isset($viewUser->user, $viewUser->flaggedReason, $viewUser->instance)) {
				Main::$connection->flagUser($viewUser);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testFlagUser() in index.php</strong>");
			return false;
		}


		public function testSubscribeForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			$forumPostConfig = new ForumPostConfig();
			//$forumPostConfig->id = ""; //ForumPostConfig id.
			if (isset($forumPostConfig->id)) {
				Main::$connection->subscribeForumPost($forumPostConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSubscribeForumPost() in index.php</strong>");
			return false;
		}

		public function testUnsubscribeForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			$forumPostConfig = new ForumPostConfig();
			//$forumPostConfig->id = ""; //ForumPostConfig id.
			if (isset($forumPostConfig->id)) {
				Main::$connection->unsubscribeForumPost($forumPostConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testUnsubscribeForumPost() in index.php</strong>");
			return false;
		}

		/**
		 * Tested -> passed
		 * Requirement: User login.
		 */

		public function testGetForumPosts()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
			$browseForumPosts = new BrowseConfig();
			$browseForumPosts->type = "Post";
			$browseForumPosts->typeFilter = "Public";
			$browseForumPosts->sort = "date";
			if (isset($browseForumPosts->type, $browseForumPosts->typeFilter, $browseForumPosts->sort)) {
				return Main::$connection->getPosts($browseForumPosts);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetForumPosts() in index.php</strong>");
			return null;
		}
		/**
		 * Tested -> passed
		 * Requirement: forum id.
		 */
		public function testSubscribeForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			$forum = new ForumConfig();

			// $forum->id = ""; //ForumPost id.
			if (isset($forum->id)) {
				Main::$connection->subscribeForum($forum);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSubscribeForum() in index.php</strong>");

			return false;
		}

		/**
		 * Tested -> passed
		 * Requirement: forum id.
		 */
		public function testUnsubscribeForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			$forum = new ForumConfig();
			// $forum->id = ""; //ForumPost id.
			if (isset($forum->id)) {
				Main::$connection->unsubscribeForum($forum);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testUnsubscribeForum() in index.php</strong>");

			return false;
		}



		public function testThumbsUpForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function thumbsUp(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();

			// $forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->thumbsUp($forumPost);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testThumbsUpForum() in index.php</strong>");

			return false;
		}

		public function testThumbsDownForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function thumbsDown(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();
			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->thumbsDown($forumPost);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testThumbsDownForum() in index.php</strong>");

			return false;
		}

		public function testStarForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			
	
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function testStarForum(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();

			//$forumPost->id = ""; //ForumPost id.
			if (isset($forumPost->id)) {
				Main::$connection->star($forumPost);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testStarForum() in index.php</strong>");

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
			}
			Utils::includeMessage("<strong>Please fill the required data @ testAvatarMessage() in index.php</strong>");
			return null;
		}

		public function testTTS()
		{
			$ttsConfig = new Speech();
			// $ttsConfig->voice = "";
			// $ttsConfig->text= "";
			// $ttsConfig->mod = "";
			if (isset($ttsConfig->voice, $ttsConfig->text, $ttsConfig->mod)) {
				return Main::$connection->tts($ttsConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testTTS() in index.php</strong>");
			return null;

		}

		public function testGetAdminsForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			//Connecting to a forum and establish validation
	
			$forumConfig = new ForumConfig();

			// $forumConfig->id = ""; //Forum ID
	

			if (isset($forumConfig->id, $forumConfig->user)) {
				return Main::$connection->getAdmins($forumConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetAdminsForum() in index.php</strong>");
			return null;

		}



		public function testGetUsersForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			//Connecting to a forum and establish validation
	
			$forumConfig = new ForumConfig();

			// $forumConfig->id = ""; //Forum ID
	

			if (isset($forumConfig->id)) {
				return Main::$connection->getUsersOfType($forumConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetUsersForum() in index.php</strong>");
			return null;
		}

		/**
		 * Tested -> passed
		 * Requirement: User login.
		 */
		public function testGetCategories()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$contentConfig = new ContentConfig();

			$contentConfig->type = "Bot"; //Forum, Channel, Aavatar, Script, Domain.
			if (isset($contentConfig->type)) {
				return Main::$connection->getCategories($contentConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetCategories() in index.php</strong>");
			return null;
		}
		/**
		 * Tested -> passed
		 * Requirement: User login.
		 */
		public function testGetTags()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$contentConfig = new ContentConfig();

			$contentConfig->type = "Bot"; //Forum, Channel, Aavatar, Script, Domain.
			if (isset($contentConfig->type)) {
				return Main::$connection->getTags($contentConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetTags() in index.php</strong>");

			return null;
		}

		public function testGetTemplates()
		{
			return Main::$connection->getTemplates();
		}

		public function testGetChannelBotMode()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$channelConfig = new ChannelConfig();

			// $channelConfig->id = "";
	
			if (isset($channelConfig->user, $channelConfig->id)) {
				return Main::$connection->getChannelBotMode($channelConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetChannelBotMode() in index.php</strong>");

			return null;
		}

		public function testSaveChannelBotMode()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$botModeConfig = new BotModeConfig();

			// $botModeConfig->instance = "";
			// $botModeConfig->bot = "";
			// $botModeConfig->mode = "";
	
			if (isset($botModeConfig->instance, $botModeConfig->user, $botModeConfig->bot, $botModeConfig->mode)) {
				return Main::$connection->saveChannelBotMode($botModeConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveChannelBotMode() in index.php</strong>");
			return null;
		}


		public function testSaveForumBotMode()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$botModeConfig = new BotModeConfig();

			// $botModeConfig->instance = "";
			// $botModeConfig->bot = "";
			// $botModeConfig->mode = "";
	
			if (isset($botModeConfig->instance, $botModeConfig->user, $botModeConfig->bot, $botModeConfig->mode)) {
				return Main::$connection->saveForumBotMode($botModeConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveForumBotMode() in index.php</strong>");
			return null;
		}

		public function testSaveLearning()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$learningConfig = new LearningConfig();

			// $learningConfig->instance = "";
			// $learningConfig->correctionMode = "";
			// $learningConfig->learningMode = "";
			// $learningConfig->learningRate = "";
			// $learningConfig->scriptTimeout = 0;
			// $learningConfig->responseMatchTimeout = 0;
			// $learningConfig->conversationMatchPercentage = "";
			// $learningConfig->discussionMatchPercentage = "";
			// $learningConfig->enableEmoting = true;
			// $learningConfig->enableEmotions = true;
			// $learningConfig->enableComprehension = true;
			// $learningConfig->enableConsciousness = true;
			// $learningConfig->enableResponseMatch = true;
			// $learningConfig->checkExactMatchFirst = true;
			// $learningConfig->fixFormulaCase = true;
			// $learningConfig->learnGrammar = true;
			// $learningConfig->synthesizeResponse = true;
	
			if (isset($learningConfig->instance, $learningConfig->user, $learningConfig->learningMode, $learningConfig->learningRate)) {
				Main::$connection->saveLearning($learningConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveLearning() in index.php</strong>");
			return false;

		}

		public function testSaveVoice()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$voiceConfig = new VoiceConfig();

			// $voiceConfig->instance = "";
			// $voiceConfig->voice = "";
			// $voiceConfig->mod = "";
			// $voiceConfig->language = "";
			// $voiceConfig->pitch = "";
			// $voiceConfig->speechRate = "";
			// $voiceConfig->nativeVoice = "";
			if (isset($voiceConfig->instance, $voiceConfig->user, $voiceConfig->voice, $voiceConfig->mod)) {
				Main::$connection->saveVoice($voiceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveVoice() in index.php</strong>");
			return false;
		}

		public function testSaveBotAvatar()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$instanceConfig = new InstanceConfig();

			// $instanceConfig->instance = "";
			// $instanceConfig->instanceAvatar = "";
			if (isset($instanceConfig->instance, $instanceConfig->user, $instanceConfig->instanceAvatar)) {
				Main::$connection->saveBotAvatar($instanceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveBotAvatar() in index.php</strong>");
			return false;
		}

		public function testTrainInstance()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$trainingConfig = new TrainingConfig();

			// $trainingConfig->instance = "";
			// $trainingConfig->operation = "";
			// $trainingConfig->question = "";
			// $trainingConfig->response = "";
	
			if (isset($trainingConfig->instance, $trainingConfig->operation, $trainingConfig->question, $trainingConfig->response)) {
				Main::$connection->train($trainingConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testTrainInstance() in index.php</strong>");
			return false;
		}

		public function testUserAdmin()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$userAdminConfig = new UserAdminConfig();

			$userAdminConfig->instance = "";
			// $userAdminConfig->type = "";
			// $userAdminConfig->operation = ""; //example: 'Addadmin'
			// $userAdminConfig->operationUser = "";
	
			if (isset($userAdminConfig->instance, $userAdminConfig->type, $userAdminConfig->operation, $trainingConfig->operationUser)) {
				Main::$connection->userAdmin($userAdminConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testUserAdmin() in index.php</strong>");

			return false;
		}


		public function testCreateAvatar()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$avatarConfig = new AvatarConfig();

			$avatarConfig->name = "";
			$avatarConfig->description = "";
			// $avatarConfig->details = "";
			// $avatarConfig->disclaimer = "";
			// $avatarConfig->categories = "Misc";
			// $avatarConfig->license = "";
			// $avatarConfig->accessMode = "";
			// $avatarConfig->isPrivate = false;
			// $avatarConfig->isHidden = false;
			if (isset($avatarConfig->name, $avatarConfig->description, $avatarConfig->details, $avatarConfig->accessMode, $avatarConfig->isPrivate)) {
				Main::$connection->create($avatarConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateAvatar() in index.php</strong>");

			return false;
		}

		public function testCreateAvatarMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$mediaConfig = new AvatarMedia();



			//Load file
			$target_dir = "uploads/";
			$file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
			$type = strtolower(pathinfo($file, PATHINFO_EXTENSION));
			//Set up Avatar Media
			$mediaConfig->instance = "";
			$mediaConfig->name = $file; //Get file name
			// $mediaConfig->type = $type; //Get file type.
			// $mediaConfig->hd = "";
	

			if (isset($mediaConfig->instance, $mediaConfig->name, $mediaConfig->type, $file)) {
				Main::$connection->createAvatarMedia($file, $mediaConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateAvatarMedia() in index.php</strong>");

			return false;
		}


		public function testCreateGraphicMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$graphicConfig = new GraphicConfig();



			//Load file
			$target_dir = "uploads/";
			$file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
			// $type = strtolower(pathinfo($file, PATHINFO_EXTENSION));
			//Set up Avatar Media
			$graphicConfig->id = "";
			// $graphicConfig->fileName = $file; //Get file name
			// $graphicConfig->fileType = $type; //Get file type.
	

			if (isset($graphicConfig->id, $graphicConfig->fileName, $graphicConfig->fileType, $file)) {
				//Main::$connection->createGraphicMedia($file, $graphicConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testCreateGraphicMedia() in index.php</strong>");

			return false;
		}

		public function testUpdateUserIcon()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//Load file
			$target_dir = "uploads/";
			$file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
			$type = strtolower(pathinfo($file, PATHINFO_EXTENSION));
			echo "-----<br>";
			echo "type: " . $type;
			print_r($type);
			echo "-----<br>";

			if (isset($file)) {
				// return Main::$connection->updateIconUser($file, $userConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testUpdateUserIcon() in index.php</strong>");
			}
			return null;
		}

		public function testGetFroumBotMode(): ?BotModeConfig
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return null;
			}

			$forumConfig = new ForumConfig();

			// $forumConfig->id = "";
	
			if (isset($forumConfig->id)) {
				return Main::$connection->getForumBotMode($forumConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetFroumBotMode() in index.php</strong>");
			}
			return null;
		}

		public function testGetVoice()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			// $instanceConfig->id = "";
			// $instanceConfig->instance = "";
	
			if (isset($instanceConfig->id, $instanceConfig->instance)) {
				return Main::$connection->getVoice($instanceConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetVoice() in index.php</strong>");
			}
			return null;

		}
		/**
		 * Tested -> passed
		 * Requirement: Bot ID or instance
		 */
		public function testGetDefaultResponses()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			// $instanceConfig->id = "";
			// $instanceConfig->instance = "";
	
			if (isset($instanceConfig->id, $instanceConfig->instance)) {
				return Main::$connection->getDefaultResponses($instanceConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetDefaultResponses() in index.php</strong>");
			}
			return null;

		}


		/**
		 * Tested -> passed
		 * Requirement: Bot ID or instance
		 */
		public function testGetGreetings()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			// $instanceConfig->id = "";
			// $instanceConfig->instance = "";

			if (isset($instanceConfig->id, $instanceConfig->instance)) {
				return Main::$connection->getGreetings($instanceConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetGreetings() in index.php</strong>");
			}
			return null;
		}

		/**
		 * Tested -> passed
		 * Requirement: Bot ID or instance
		 */

		public function testGetResponses()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$responseSearchConfig = new ResponseSearchConfig();

			// $responseSearchConfig->instance = ""; //Bot id
			$responseSearchConfig->responseType = "responses"; //To get responses
			$responseSearchConfig->duration = "all";
			$responseSearchConfig->inputType = "all";
			$responseSearchConfig->restrict = "exact";
			$responseSearchConfig->filter = ""; //response (From ResponseConfig)
	

			if (isset($responseSearchConfig->instance, $responseSearchConfig->responseType, $responseSearchConfig->filter)) {
				return Main::$connection->getResponses($responseSearchConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetResponses() in index.php</strong>");
			}
			return null;
		}
		/**
		 * Tested -> Does not return conversations
		 * Requirement: Bot ID or instance
		 */
		public function testGetConversations()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$responseSearchConfig = new ResponseSearchConfig();
			// $responseSearchConfig->instance = ""; //Bot instance
			$responseSearchConfig->responseType = "conversation"; //To get conversations. or response (to get responses)
			$responseSearchConfig->duration = "day";
			$responseSearchConfig->inputType = "all";
			$responseSearchConfig->restrict = "none";
			$responseSearchConfig->filter = "";


			if (isset($responseSearchConfig->instance, $responseSearchConfig->responseType, $responseSearchConfig->filter)) {
				return Main::$connection->getConversations($responseSearchConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetConversations() in index.php</strong>");
			}
			return null;
		}



		/**
		 * Tested -> Passed only when Learning Mode is enabled
		 * Requirement: Bot ID and instance
		 */
		public function testGetLearning()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();

			// $instanceConfig->instance = ""; //Need a bot ID
			// $instanceConfig->id = "";
	

			if (isset($instanceConfig->instance)) {
				return Main::$connection->getLearning($instanceConfig);
			} else {
				Utils::includeMessage("<strong>Please fill the required data @ testGetLearning() in index.php</strong>");
			}
			return null;
		}


		/**
		 * Tested -> Passed
		 * Requirement: Type: Bot, Script, Avatar, Forum, Domain or Graphic.
		 */
		public function testBrowse()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$browseConfig = new BrowseConfig();

			$browseConfig->type = "Bot"; //Can be either Avatar, Forum, Graphic, Bot, Script ...etc
			$browseConfig->typeFilter = "Featured";
			$browseConfig->contentRating = "Everyone";


			if (isset($browseConfig->type, $browseConfig->typeFilter, $browseConfig->contentRating)) {
				return Main::$connection->browse($browseConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testBrowse() in index.php</strong>");

			return null;
		}

		public function testGetAvatarMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$avatarConfig = new AvatarConfig();

			// $avatarConfig->id = "";
			// $avatarConfig->instance = "";
	

			if (isset($browseConfig->instance)) {
				return Main::$connection->getAvatarMedia($avatarConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetAvatarMedia() in index.php</strong>");

			return null;
		}

		public function testGetScriptSource()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptConfig = new ScriptConfig();

			// $scriptConfig->instance = "";
			// $scriptConfig->id = "";
	
			if (isset($scriptConfig->instance, $scriptConfig->id)) {
				return Main::$connection->getScriptSource($scriptConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetScriptSource() in index.php</strong>");

			return null;
		}



		public function testSaveScriptSource()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();

			// $scriptSourceConfig->instance = "";
			// $scriptSourceConfig->id = "";
			// $scriptSourceConfig->source = "";
	
			if (isset($scriptSourceConfig->instance, $scriptSourceConfig->id, $scriptSourceConfig->source)) {
				Main::$connection->saveScriptSource($scriptSourceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveScriptSource() in index.php</strong>");

			return false;
		}



		public function testGetBotScriptSource()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();

			// $scriptSourceConfig->instance = "";
			// $scriptSourceConfig->id = "";
	
			if (isset($scriptSourceConfig->instance, $scriptSourceConfig->id)) {
				return Main::$connection->getBotScriptSource($scriptSourceConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetBotScriptSource() in index.php</strong>");
			return null;
		}

		public function testGetBotScripts()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();

			// $instanceConfig->instance = "";
			// $instanceConfig->id = "";
	
			if (isset($instanceConfig->instance, $instanceConfig->id)) {
				return Main::$connection->getBotScripts($instanceConfig);
			}
			Utils::includeMessage("<strong>Please fill the required data @ testGetBotScripts() in index.php</strong>");
			return null;
		}

		public function testImportBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptConfig = new ScriptConfig();

			// $scriptConfig->id = "";
			if (isset($scriptConfig->id)) {
				Main::$connection->importBotScript($scriptConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testImportBotScript() in index.php</strong>");
			return false;
		}

		public function testImportBotLog()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptConfig = new ScriptConfig();

			// $scriptConfig->instance = "";
			// $scriptConfig->id = "";
	
			if (isset($scriptConfig->instance, $scriptConfig->id)) {
				Main::$connection->importBotLog($scriptConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testImportBotLog() in index.php</strong>");
			return false;
		}

		public function testSaveBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();

			// $scriptSourceConfig->instance = ""; //id
			// $scriptSourceConfig->id = ""; //if not null, source id.
			// $scriptSourceConfig->source = "";
			if (isset($scriptSourceConfig->instance, $scriptSourceConfig->id, $scriptSourceConfig->source)) {
				Main::$connection->saveBotScriptSource($scriptSourceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testSaveBotScript() in index.php</strong>");
			return false;
		}

		/**
		 * Tested: passed
		 */
		public function testDeleteBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();
			// $scriptSourceConfig->instance = ""; //bot id
			// $scriptSourceConfig->id = ""; // script id 
			if (isset($scriptSourceConfig->instance, $scriptSourceConfig->id)) {
				Main::$connection->deleteBotScript($scriptSourceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testDeleteBotScript() in index.php</strong>");
			return false;
		}


		/**
		 * Tested: passed
		 */
		public function testUpBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();
			// $scriptSourceConfig->instance = ""; //bot id
			// $scriptSourceConfig->id = ""; // script id 
			if (isset($scriptSourceConfig->instance, $scriptSourceConfig->id)) {
				Main::$connection->upBotScript($scriptSourceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testUpBotScript() in index.php</strong>");
			return false;
		}
		/**
		 * Tested: passed
		 */
		public function testDownBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();
			// $scriptSourceConfig->instance = ""; //bot id
			// $scriptSourceConfig->id = ""; // script id 
			if (isset($scriptSourceConfig->instance, $scriptSourceConfig->id)) {
				Main::$connection->downBotScript($scriptSourceConfig);
				return true;
			}
			Utils::includeMessage("<strong>Please fill the required data @ testDownBotScript() in index.php</strong>");
			return false;
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
					Utils::includeMessage("<strong>Connecting a user account. @account_test</strong>");
					$userConfig = $main->testConnectUserAccount();
					break;
				case "chat":
					Utils::includeMessage("<strong>Sending a chat message.</strong>");
					$response = $main->testSendChatMessage();
					break;
				case "view-user":
					Utils::includeMessage("<strong>Fetch user details. @test</strong>");
					$userConfig = $main->testFetchUserDetails();
					break;
				case "check-forum-post":
					Utils::includeMessage("<strong>Fetch Forum post detials.</strong>");
					$forumPost = $main->testFetchForumPost();
					break;
				case "create-user":
					Utils::includeMessage("<strong>Create a new user.</strong>");
					$userConfig = $main->testCreateUser();
					break;
				case "update-user":
					Utils::includeMessage("<strong>update an existing user.</strong>");
					$userConfig = $main->testUpdateUser();
					break;
				case "fetch-image":
					Utils::includeMessage("<strong>View an image.</strong>", $main->testFetchImage());
					break;
				case "create-forum-post":
					Utils::includeMessage("<strong>Create a forum post.</strong>");
					$newPost = $main->testCreateForumPost();
					break;
				case "update-forum-post":
					Utils::includeMessage("<strong>Update a forum post.</strong>");
					$newPost = $main->testUpdateForumPost();
					break;
				case "delete-forum-post":
					Utils::includeMessage("<strong>Delete forum post.</strong>");
					$pass = $main->testDeleteForumPost();
					break;
				case "create-channel-file-attachment":
					Utils::includeMessage("<strong>Create a channel file attachment.</strong>");
					$channelAttachment = $main->testCreateChannelFileAttachment();
					break;
				case "create-channel-image-attachment":
					Utils::includeMessage("<strong>Create a channel image attachment.</strong>");
					$channelAttachment = $main->testCreateChannelImageAttachment();
					break;
				case "create-post-reply":
					Utils::includeMessage("<strong>Create a post reply.</strong>");
					$channelAttachment = $main->testCreateReply();
					break;
				case "save-avatar-background":
					Utils::includeMessage("<strong>Save - Upload avatar's background.</strong>");
					$pass = $main->testSaveAvatarBackground();
					break;
				case "delete-response":
					Utils::includeMessage("<strong>Permanently delete the response, greetings, or default response.</strong>");
					$pass = $main->testDeleteResponse();
					break;
				case "delete-avatar-media":
					Utils::includeMessage("<strong>Delete the avatar media.</strong>");
					$pass = $main->testDeleteAvatarMedia();
					break;
				case "delete-avatar-background":
					Utils::includeMessage("<strong>Delete the avatar background.</strong>");
					$pass = $main->testDeleteAvatarBackground();
					break;
				case "save-avatar-media":
					Utils::includeMessage("<strong>Save the avatar media.</strong>");
					$pass = $main->testSaveAvatarMedia();
					break;
				case "flag":
					Utils::includeMessage("<strong>Flag an instance.</strong>");
					$pass = $main->testFlagInstance();
					break;
				case "subscribe-forum-post":
					Utils::includeMessage("<strong>Subscribe a forum post.</strong>");
					$pass = $main->testSubscribeForumPost();
					break;
				case "unsubscribe-forum-post":
					Utils::includeMessage("<strong>Unsubscribe a forum post.</strong>");
					$pass = $main->testUnsubscribeForumPost();
					break;
				case "subscribe-forum":
					Utils::includeMessage("<strong>Subscribe to a forum.</strong>");
					$pass = $main->testSubscribeForum();
					break;
				case "get-forum-posts":
					Utils::includeMessage("<strong>Return the list of forum posts for the forum browse criteria.</strong>");
					$list = $main->testGetForumPosts();
					break;
				case "unsubscribe-forum":
					Utils::includeMessage("<strong>Unsubscribe to a forum.</strong>");
					$pass = $main->testUnsubscribeForum();
					break;
				case "thumbs-up-forum":
					Utils::includeMessage("<strong>Thumbs up to a forum.</strong>");
					$pass = $main->testThumbsUpForum();
					break;
				case "thumbs-down-forum":
					Utils::includeMessage("<strong>Thumbs down to a forum.</strong>");
					$pass = $main->testThumbsDownForum();
					break;
				case "star-forum":
					Utils::includeMessage("<strong>Evaluate a forum by placing a number of stars.</strong>");
					$pass = $main->testStarForum();
					break;
				case "flag-user":
					Utils::includeMessage("<strong>Flag user account.</strong>");
					$pass = $main->testFlagUser();
					break;
				case "avatar-message":
					Utils::includeMessage("<strong>Process the avatar message and return the avatars response.</strong>");
					$avatarMessage = $main->testAvatarMessage();
					break;
				case "tts":
					Utils::includeMessage("<strong>Process the speech message and return the server generate text-to-speech audio file.</strong>");
					$tts = $main->testTTS();
					break;
				case "get-admins-forum":
					Utils::includeMessage("<strong>Return the administrators of the content.</strong>");
					$list = $main->testGetAdminsForum();
					break;
				case "get-users-forum":
					Utils::includeMessage("<strong>Return the users of the content.</strong>");
					$list = $main->testGetUsersForum();
					break;
				case "get-categories":
					Utils::includeMessage("<strong>Return the administrators of the content.</strong>");
					$list = $main->testGetCategories();
					break;
				case "get-tags":
					Utils::includeMessage("<strong>Return the list of tags for the type, and domain.</strong>");
					$list = $main->testGetTags();
					break;
				case "get-templates":
					Utils::includeMessage("<strong>Return the list of bot templates.</strong>");
					$list = $main->testGetTemplates();
					break;
				case "get-channel-bot-mode":
					Utils::includeMessage("<strong>Return the channel's bot configuration.</strong>");
					$botModeConfig = $main->testGetChannelBotMode();
					break;
				case "save-channel-bot-mode":
					Utils::includeMessage("<strong>Save the channel's bot configuration.</strong>");
					$botModeConfig = $main->testSaveChannelBotMode();
					break;
				case "save-forum-bot-mode":
					Utils::includeMessage("<strong>Save the channel's bot configuration.</strong>");
					$botModeConfig = $main->testSaveForumBotMode();
					break;
				case "save-learning":
					Utils::includeMessage("<strong>Save the bot's learning configuration.</strong>");
					$pass = $main->testSaveLearning();
					break;
				case "save-voice":
					Utils::includeMessage("<strong>Save the bot's voice configuration.</strong>");
					$pass = $main->testSaveVoice();
					break;
				case "save-bot-avatar":
					Utils::includeMessage("<strong>Save the bot's avatar configuration.</strong>");
					$pass = $main->testSaveBotAvatar();
					break;
				case "train-instance":
					Utils::includeMessage("<strong>Train the bot with a new question/response pair.</strong>");
					$pass = $main->testTrainInstance();
					break;
				case "user-admin":
					Utils::includeMessage("<strong>Perform the user administration task (add or remove users, or administrators).</strong>");
					$pass = $main->testUserAdmin();
					break;
				case "create-avatar":
					Utils::includeMessage("<strong>Create the new content. Avatar.</strong>");
					$pass = $main->testCreateAvatar();
					break;
				case "create-avatar-media":
					Utils::includeMessage("<strong>Add the avatar media file to the avatar.</strong>");
					$pass = $main->testCreateAvatarMedia();
					break;
				case "create-graphic-media":
					Utils::includeMessage("<strong>Add the graphic media file to the graphic.</strong>");
					$pass = $main->testCreateGraphicMedia();
					break;
				case "update-user-icon":
					Utils::includeMessage("<strong>Update the user's icon. The file will be uploaded to the server.</strong>");
					$userConfig = $main->testUpdateUserIcon();
					break;
				case "get-forum-bot-mode":
					Utils::includeMessage("<strong>Return the forum's bot configuration.</strong>");
					$botModeConfig = $main->testGetFroumBotMode();
					break;
				case "get-voice":
					Utils::includeMessage("<strong>Return the bot's voice configuration.</strong>");
					$voiceConfig = $main->testGetVoice();
					break;
				case "get-default-responses":
					Utils::includeMessage("<strong>Return the bot's default responses.</strong>");
					$list = $main->testGetDefaultResponses();
					break;
				case "get-greetings":
					Utils::includeMessage("<strong>Return the bot's greetings.</strong>");
					$list = $main->testGetGreetings();
					break;
				case "get-responses":
					Utils::includeMessage("<strong>Search the bot's responses.</strong>");
					$list = $main->testGetResponses();
					break;
				case "get-conversations":
					Utils::includeMessage("<strong>Search the bot's conversations.</strong>");
					$list = $main->testGetConversations();
					break;
				case "get-learning":
					Utils::includeMessage("<strong>Return the bot's learning configuration.</strong>");
					$learningConfig = $main->testGetLearning();
					break;
				case "browse":
					Utils::includeMessage("<strong>Return the list of content for the browse criteria. The type defines the content type (one of Bot, Forum, Channel, Domain)..</strong>");
					$list = $main->testBrowse();
					break;
				case "get-avatar-media":
					Utils::includeMessage("<strong>Return the list of media for the avatar.</strong>");
					$list = $main->testGetAvatarMedia();
					break;
				case "get-script-source":
					Utils::includeMessage("<strong>Return the script source.</strong>");
					$scriptSourceConfig = $main->testGetScriptSource();
					break;
				case "save-script-source":
					Utils::includeMessage("<strong>Create or update script - Save the script source.</strong>");
					$pass = $main->testSaveScriptSource();
					break;
				case "get-bot-script-source":
					Utils::includeMessage("<strong>Return the source code for a single bot script.</strong>");
					$pass = $main->testGetBotScriptSource();
					break;
				case "get-bot-scripts":
					Utils::includeMessage("<strong>Return a list of the bots scripts.</strong>");
					$list = $main->testGetBotScripts();
					break;
				case "import-bot-script":
					Utils::includeMessage("<strong>import a script to the bot.</strong>");
					$pass = $main->testImportBotScript();
					break;
				case "import-bot-log":
					Utils::includeMessage("<strong>import a chatlog/response list to the bot.</strong>");
					$pass = $main->testImportBotLog();
					break;
				case "save-bot-script-source":
					Utils::includeMessage("<strong>Save the bot script source.</strong>");
					$pass = $main->testSaveBotScript();
					break;
				case "delete-bot-script":
					Utils::includeMessage("<strong>Delete selected bot script.</strong>");
					$pass = $main->testDeleteBotScript();
					break;
				case "up-bot-script":
					Utils::includeMessage("<strong>Move up one bot script.</strong>");
					$pass = $main->testUpBotScript();
					break;
				case "down-bot-script":
					Utils::includeMessage("<strong>Move down one bot script.</strong>");
					$pass = $main->testDownBotScript();
					break;
				case "save-response":
					Utils::includeMessage("<strong>Create, update, flag, unflag or invalidate a response.</strong>");
					$pass = $main->testSaveResponse();
					break;
			}
		}
		?>
	</fieldset>

	<!-- 
		At the bottom of the page, there is a compact box that displays the returned readable 
		information resulting from the executed test requests.
	-->
	<div id="box">
		<strong>Details</strong>
		<br>
		<?php
		if (Main::$connection->getUser() != null) {
			echo "<strong>User: </strong>" . Main::$connection->getUser()->user . "<br>";
			echo "<strong>Joined: </strong>" . Main::$connection->getUser()->joined . "<br>";
			echo "<strong>Connects: </strong>" . Main::$connection->getUser()->connects . "<br>";
			echo "<strong>Name: </strong>" . Main::$connection->getUser()->name . "<br>";
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
			echo "<strong>Request: </strong>" . ($pass ? 'Posted' : 'Missing data') . "<br>";
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
		} else if (isset($voiceConfig)) {
			echo "<strong>Voice: </strong>" . $voiceConfig->voice . "<br>";
			echo "<strong>NativeVoice: </strong>" . $voiceConfig->nativeVoice . "<br>";
			echo "<strong>Language: </strong>" . $voiceConfig->language . "<br>";
		} else if (isset($learningConfig, $learningConfig->learningMode)) {
			echo "<strong>Learning Mode: </strong>" . $learningConfig->learningMode . "<br>";
			echo "<strong>Learning Rate: </strong>" . $learningConfig->learningRate . "<br>";
			// echo "<strong>Correction Mode: </strong>" . $learningConfig->correctionMode . "<br>";
		} else if (isset($scriptSourceConfig)) {
			echo "<strong>Version: </strong>" . $scriptSourceConfig->version . "<br>";
			echo "<strong>Version Name: </strong>" . $scriptSourceConfig->versionName . "<br>";
		} else {
			echo "There is no data to show yet.";
		}
		?>
	</div>
</body>

</html>