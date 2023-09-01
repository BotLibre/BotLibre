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
			<table>
				<tr>
					<td><select id="api" name="api">
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
						</select></td>
					<td><input type="submit" value="Test"></td>
				</tr>
				<tr>
					<td>
						<?php
						if (isset($_GET["api"])) {
							echo "<strong>Selected API: " . $_GET["api"] . "</strong>";
						} else {
							echo "<strong>Nothing is selected.</strong>";
						}
						?>
					</td>
				</tr>
			</table>
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
		public static ?string $imgUrl = "https://botlibre.com/avatars/a48927947.png";

		//Please enter the id of each instance to be able to test them.
		public static ?string $avatarInstanceId = "";
		public static ?string $botInstanceId = "";
		public static ?string $forumInstanceId = "";
		public static ?string $postId = "";
		public static ?string $graphicInstanceId = "";
		public static ?string $scriptInstanceId = "";
		public static ?string $channelInstanceId = "";
		public static ?string $analyticInstanceId = "";

		


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
			return Main::$connection->chat($config);

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
			return Main::$connection->fetchForumPost($forumPostConfig);
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
			return Main::$connection->createForumPost($postConfig);
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
			return Main::$connection->updateForumPost($postConfig);
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
			return Main::$connection->deleteForumPost($postConfig);
		}

		//Tested: passed
		//File was uploaded successfully but didn't show at the livechat. 
		//Link was created i.e livechat?attachment=#123&key=#12345&name=name
		public function testCreateChannelFileAttachment($file)
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$mediaConfig = new MediaConfig();
			$mediaConfig->name = "name"; //File name i.e: file.txt
			$mediaConfig->type = $file->mime; //File Type i.e: text/plain
			$mediaConfig->instance = Main::$channelInstanceId; //Channel ID is required
			return Main::$connection->createChannelFileAttachment($file, $mediaConfig);
		}

		//Tested: passed
		//Image was uploaded successfully but didn't show at the livechat. 
		//Link was created i.e livechat?attachment=#123&key=#12345&name=name
		public function testCreateChannelImageAttachment($imageFile)
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$mediaConfig = new MediaConfig();
			$mediaConfig->name = "name"; //File name i.e: file.txt
			$mediaConfig->type = $imageFile->mime; //File Type i.e: text/plain
			$mediaConfig->instance = Main::$channelInstanceId; //Channel ID is required
			return Main::$connection->createChannelImageAttachment($imageFile, $mediaConfig);
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
			$postConfig->forum = Main::$forumInstanceId; //Forum ID
			$postConfig->parent = ""; //Parent Post
			$postConfig->details = "";
			$postConfig->tags = "";
			return Main::$connection->createReply($postConfig);
		}

		public function testCreateUserMessage()
		{
			//Template
			$writer = "";
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
			$response->instance = Main::$botInstanceId; //Bot ID
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
			Main::$connection->saveResponse($response);
		}

		/**
		 * Tested -> Passed
		 */
		public function testSaveAvatarBackground($imageFile)
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$avatarConfig = new AvatarMedia();
			$avatarConfig->name = "name";
			$avatarConfig->type = $imageFile->mime;
			$avatarConfig->instance = Main::$avatarInstanceId; //Avatar Instance id
			return Main::$connection->saveAvatarBackground($imageFile, $avatarConfig);
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
			return Main::$connection->deleteResponse($config);
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
			return Main::$connection->deleteAvatarMedia($avatarMedia);
		}

		/**
		 * Tested: passed
		 * Avatar media must exist to establish the mediaId
		 */
		public function testSaveAvatarMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$avatarMedia = new AvatarMedia();
			$avatarMedia->instance = Main::$avatarInstanceId; //Instance ID.
			$avatarMedia->mediaId = ""; //media ID required.
			$avatarMedia->type = "image/png"; //media type.
			$avatarMedia->name = "Test"; //Media name.
			$avatarMedia->emotions = "like"; //emotions
			$avatarMedia->actions = "smile";
			$avatarMedia->poses = "default";
			$avatarMedia->talking = true;
			$avatarMedia->hd = false;
			return Main::$connection->saveAvatarMedia($avatarMedia);
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
			$avatarConfig->id = Main::$avatarInstanceId; //Avatar id.
			return Main::$connection->deleteAvatarBackground($avatarConfig);

		}

		public function testFlagInstance()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//This can be applied on either (GraphicConfig, ChannelConfig, DomainConfig, InstanceConfig ..etc);
			$avatarConfig = new AvatarConfig();
			$avatarConfig->flaggedReason = "Test flag an instance.";
			$avatarConfig->id = Main::$avatarInstanceId; //Not allowed to flag your instance.
			return Main::$connection->flag($avatarConfig);
		}


		public function testFlagUser()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$userToFlag = new UserConfig();
			$userToFlag->user = "";
			$userToFlag->instance = "";
			$userToFlag->flaggedReason = "Test flag a user account.";
			return Main::$connection->flagUser($userToFlag);
		}




		//Tested, need to verify account
		public function testSubscribeForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->id = Main::$postId; //ForumPostConfig id.	
			return Main::$connection->subscribeForumPost($forumPostConfig);
		
		}

		//Tested, need to verify account
		public function testUnsubscribeForumPost()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$forumPostConfig = new ForumPostConfig();
			$forumPostConfig->id = Main::$postId; //ForumPostConfig id.
			Main::$connection->unsubscribeForumPost($forumPostConfig);
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
			return Main::$connection->getPosts($browseForumPosts);
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
			$forum->id = Main::$forumInstanceId; //ForumPost id.
			return Main::$connection->subscribeForum($forum);
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
			$forum->id = Main::$forumInstanceId; //ForumPost id.
			return Main::$connection->unsubscribeForum($forum);
		}



		//Tested: passed
		public function testThumbsUpForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function thumbsUp(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumConfig = new ForumConfig();
			$forumConfig->id = Main::$forumInstanceId; //Forum id.{
			return Main::$connection->thumbsUp($forumConfig);
		}


		//Tested: passed
		public function testThumbsDownForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function thumbsDown(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forumPost = new ForumConfig();
			$forumPost->id = Main::$forumInstanceId; //ForumPost id.
			return Main::$connection->thumbsDown($forumPost);
		}


		//Tested: passed
		public function testStarForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//FourmConfig, GraphicConfig, ForumPostConfig ..etc. Has getType() which return the type of the config object.
			//Thus the function testStarForum(takes an object of WebMediumConfig) which inherited from all the config objects.
			$forum = new ForumConfig();
			$forum->id = Main::$forumInstanceId; //ForumPost id.
			$forum->stars=5;
			Main::$connection->star($forum);
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



		//Tested: passed
		public function testTTS()
		{
			$ttsConfig = new Speech();
			$ttsConfig->voice = "English : US : Female : SLT";
			$ttsConfig->text= "Hello World";
			$ttsConfig->mod = "default";
			return Main::$connection->tts($ttsConfig);
		}



		//Tested: passed
		public function testGetAdminsForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//Connecting to a forum and establish validation
			$forumConfig = new ForumConfig();
			$forumConfig->id = Main::$forumInstanceId; //Forum ID
			return Main::$connection->getAdmins($forumConfig);
		}


		//Tested: passed
		public function testGetUsersForum()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			//Connecting to a forum and establish validation
			$forumConfig = new ForumConfig();
			$forumConfig->id = Main::$forumInstanceId; //Forum ID
			return Main::$connection->getUsersOfType($forumConfig);
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
			return Main::$connection->getCategories($contentConfig);
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
			return Main::$connection->getTags($contentConfig);
		}

		//Tested: passed
		public function testGetTemplates()
		{
			return Main::$connection->getTemplates();
		}



		//tested: passed
		public function testGetChannelBotMode()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$channelConfig = new ChannelConfig();
			$channelConfig->id = Main::$channelInstanceId;
			return Main::$connection->getChannelBotMode($channelConfig);
		}



		//Tested
		public function testSaveChannelBotMode()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$mode = array("ListenOnly", "AnswerOnly", "AnswerAndListen");
			$botModeConfig = new BotModeConfig();
			$botModeConfig->instance = Main::$channelInstanceId;
			$botModeConfig->bot = Main::$botInstanceId;
			$botModeConfig->mode = $mode[2];
			return Main::$connection->saveChannelBotMode($botModeConfig);
		}



		//Tested
		public function testSaveForumBotMode()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$mode = array("ListenOnly", "AnswerOnly", "AnswerAndListen");
			$botModeConfig = new BotModeConfig();
			$botModeConfig->instance = Main::$forumInstanceId;
			$botModeConfig->bot = Main::$botInstanceId;
			$botModeConfig->mode = $mode[2];
			return Main::$connection->saveForumBotMode($botModeConfig);
		}



		//Tested: passed
		//Must be admin user.
		//Must set content rating to mature.
		public function testSaveLearning()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$learningConfig = new LearningConfig();
			$learningConfig->instance = Main::$botInstanceId;
			$learningConfig->nlp = "4";
			$learningConfig->language = "en";
			$learningConfig->correctionMode = "Everyone";
			$learningConfig->learningMode = "Everyone";
			$learningConfig->learningRate = "55";
			$learningConfig->scriptTimeout = 10000;
			$learningConfig->responseMatchTimeout = 1000;
			$learningConfig->conversationMatchPercentage = "50";
			$learningConfig->discussionMatchPercentage = "90";
			$learningConfig->enableEmoting = true;
			$learningConfig->enableEmotions = true;
			$learningConfig->enableComprehension = false; //only supported for dedicated servers
			$learningConfig->enableConsciousness = false; //only supported for dedicated servers
			$learningConfig->enableResponseMatch = true;
			$learningConfig->checkExactMatchFirst = true;
			$learningConfig->fixFormulaCase = true;
			$learningConfig->learnGrammar = true;
			$learningConfig->synthesizeResponse = true;
			$learningConfig->disableFlag = false;
			$learningConfig->reduceQuestions = true;
			$learningConfig->trackCase = false;
			return Main::$connection->saveLearning($learningConfig);
		}




		//Tested: passed
		public function testSaveVoice()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$voiceConfig = new VoiceConfig();
			$voiceConfig->instance = Main::$botInstanceId;
			$voiceConfig->voice = "English : US : Female : SLT";
			$voiceConfig->mod = "";
			$voiceConfig->language = "en";
			// $voiceConfig->pitch = "";
			// $voiceConfig->speechRate = "";
			// $voiceConfig->nativeVoice = "";
			return Main::$connection->saveVoice($voiceConfig);
		}


		//Tested: passed
		public function testSaveBotAvatar()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			$instanceConfig->id = Main::$botInstanceId;
			$instanceConfig->instanceAvatar = Main::$avatarInstanceId;
			return Main::$connection->saveBotAvatar($instanceConfig);
		}




		//Tested
		public function testTrainInstance()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}

			$trainingConfig = new TrainingConfig();

			$trainingConfig->instance = Main::$botInstanceId;
			$trainingConfig->operation = "";
			$trainingConfig->question = "";
			$trainingConfig->response = "";
			return Main::$connection->train($trainingConfig);
		}

		public function testUserAdmin()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$userAdminConfig = new UserAdminConfig();
			$userAdminConfig->instance = Main::$botInstanceId;
			$userAdminConfig->type = "Bot";
			$userAdminConfig->operation = "AddUser"; //example: 'AddAdmin', 'AddUser', 'RemoveAdmin', 'RemoveUser'
			$userAdminConfig->operationUser = "";
			return Main::$connection->userAdmin($userAdminConfig);
		}



		//Tested: passed
		public function testCreateAvatar()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$avatarConfig = new AvatarConfig();
			$avatarConfig->name = "";
			$avatarConfig->description = "";
			$avatarConfig->details = "";
			$avatarConfig->disclaimer = "";
			$avatarConfig->categories = "Misc";
			$avatarConfig->license = "";
			$avatarConfig->accessMode = "";
			$avatarConfig->isPrivate = false;
			$avatarConfig->isHidden = false;
			return Main::$connection->create($avatarConfig);
		}


		//Tested: passed (media is uploaded successfully), returned null.
		public function testCreateAvatarMedia($mediaFile)
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$mediaConfig = new AvatarMedia();
			$mediaConfig->instance = Main::$avatarInstanceId;
			$mediaConfig->name = "name"; //Get file name
			$mediaConfig->type = $mediaFile->mime; //Get file type.
			return Main::$connection->createAvatarMedia($mediaFile, $mediaConfig);
		}



		//Tested: passed
		public function testCreateGraphicMedia($mediaFile)
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$graphicConfig = new GraphicConfig();
			$graphicConfig->fileName = "name";
			$graphicConfig->fileType = $mediaFile->mime;
			//Set up Avatar Media
			$graphicConfig->id = Main::$graphicInstanceId;
			return Main::$connection->createGraphicMedia($mediaFile, $graphicConfig);
		}



		//Tested: passed
		public function testUpdateUserIcon($imageFile)
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$userConfig = Main::$connection->getUser();
			return Main::$connection->updateIconUser($imageFile, $userConfig);
		}

		public function testGetForumBotMode(): ?BotModeConfig
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return null;
			}
			$forumConfig = new ForumConfig();
			$forumConfig->id = Main::$forumInstanceId;
			return Main::$connection->getForumBotMode($forumConfig);
		}

		public function testGetVoice()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			$instanceConfig->id = "";
			$instanceConfig->instance = "";
			return Main::$connection->getVoice($instanceConfig);
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
			return Main::$connection->getDefaultResponses($instanceConfig);
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
			$instanceConfig->id = Main::$botInstanceId;
			return Main::$connection->getGreetings($instanceConfig);
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
			return Main::$connection->getResponses($responseSearchConfig);
		}
		/**
		 * Tested : passed
		 * Requirement: Bot ID or instance
		 */
		public function testGetConversations()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$responseSearchConfig = new ResponseSearchConfig();
			$responseSearchConfig->instance = Main::$botInstanceId; //Bot instance
			$responseSearchConfig->responseType = "conversations"; //To get conversations. or responses (to get responses)
			$responseSearchConfig->duration = "week";
			$responseSearchConfig->sort = "date";
			$responseSearchConfig->inputType = "all";
			return Main::$connection->getConversations($responseSearchConfig);
		}


		/**
		 * Tested -> Passed only when Learning Mode is enabled
		 * Requirement: Bot ID
		 */
		public function testGetLearning()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			$instanceConfig->id = Main::$botInstanceId; // Bot Id
			return Main::$connection->getLearning($instanceConfig);
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
			return Main::$connection->browse($browseConfig);
		}


		//Tested: Passed
		public function testGetAvatarMedia()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$avatarConfig = new AvatarConfig();

			$avatarConfig->id = Main::$avatarInstanceId;
			return Main::$connection->getAvatarMedia($avatarConfig);
		}


		//Tested: passed
		public function testGetScriptSource()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptConfig = new ScriptConfig();
			$scriptConfig->id = Main::$scriptInstanceId;
			return Main::$connection->getScriptSource($scriptConfig);
		}



		//Tested: passed
		public function testSaveScriptSource()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();
			$scriptSourceConfig->instance = "";
			$scriptSourceConfig->source = "Hello World";
			Main::$connection->saveScriptSource($scriptSourceConfig);
		}





		//Tested: passed
		public function testGetBotScriptSource()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();
			$scriptSourceConfig->instance = Main::$botInstanceId;
			$scriptSourceConfig->id = Main::$scriptInstanceId;
			return Main::$connection->getBotScriptSource($scriptSourceConfig);
		}



	
		public function testGetBotScripts()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$instanceConfig = new InstanceConfig();
			$instanceConfig->id = Main::$botInstanceId;
			return Main::$connection->getBotScripts($instanceConfig);
		}


		//Tested: passed
		public function testImportBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptConfig = new ScriptConfig();
			$scriptConfig->instance = Main::$botInstanceId;
			$scriptConfig->id = Main::$scriptInstanceId;
			return Main::$connection->importBotScript($scriptConfig);
		}


		//Tested
		public function testImportBotLog()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptConfig = new ScriptConfig();
			$scriptConfig->instance = Main::$botInstanceId;
			$scriptConfig->id = Main::$scriptInstanceId;
			return Main::$connection->importBotLog($scriptConfig);
		}



		public function testSaveBotScript()
		{
			//TODO: Set user
			if (!$this->testConnectUserAccount()) {
				return;
			}
			$scriptSourceConfig = new ScriptSourceConfig();
			$scriptSourceConfig->instance = Main::$botInstanceId; //id
			$scriptSourceConfig->id = Main::$scriptInstanceId; //if not null, source id.
			$scriptSourceConfig->source = "Hi";
			Main::$connection->saveBotScriptSource($scriptSourceConfig);
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
			$scriptSourceConfig->instance = Main::$botInstanceId; //bot id
			$scriptSourceConfig->id = Main::$scriptInstanceId; // script id 
			Main::$connection->deleteBotScript($scriptSourceConfig);
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
			$scriptSourceConfig->instance = Main::$botInstanceId; //bot id
			$scriptSourceConfig->id = Main::$scriptInstanceId; // script id 
			Main::$connection->upBotScript($scriptSourceConfig);
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
			$scriptSourceConfig->instance = Main::$botInstanceId; //bot id
			$scriptSourceConfig->id = Main::$scriptInstanceId; // script id 
			Main::$connection->downBotScript($scriptSourceConfig);
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
					$main->testDeleteForumPost();
					break;
				case "create-channel-file-attachment":
					Utils::includeMessage("<strong>Create a channel file attachment.</strong>");
					Utils::PostImageFromURL(Main::$imgUrl, [$main, 'testCreateChannelFileAttachment']);
					break;
				case "create-channel-image-attachment":
					Utils::includeMessage("<strong>Create a channel image attachment.</strong>");
					Utils::PostImageFromURL(Main::$imgUrl, [$main, 'testCreateChannelImageAttachment']);
					break;
				case "create-post-reply":
					Utils::includeMessage("<strong>Create a post reply.</strong>");
					$channelAttachment = $main->testCreateReply();
					break;
				case "save-avatar-background":
					Utils::includeMessage("<strong>Save - Upload avatar's background.</strong>");
					Utils::PostImageFromURL(Main::$imgUrl, [$main, 'testSaveAvatarBackground']);
					break;
				case "delete-response":
					Utils::includeMessage("<strong>Permanently delete the response, greetings, or default response.</strong>");
					$main->testDeleteResponse();
					break;
				case "delete-avatar-media":
					Utils::includeMessage("<strong>Delete the avatar media.</strong>");
					$main->testDeleteAvatarMedia();
					break;
				case "delete-avatar-background":
					Utils::includeMessage("<strong>Delete the avatar background.</strong>");
					$main->testDeleteAvatarBackground();
					break;
				case "save-avatar-media":
					Utils::includeMessage("<strong>Save the avatar media.</strong>");
					$main->testSaveAvatarMedia();
					break;
				case "flag":
					Utils::includeMessage("<strong>Flag an instance.</strong>");
					$main->testFlagInstance();
					break;
				case "subscribe-forum-post":
					Utils::includeMessage("<strong>Subscribe a forum post.</strong>");
					$main->testSubscribeForumPost();
					break;
				case "unsubscribe-forum-post":
					Utils::includeMessage("<strong>Unsubscribe a forum post.</strong>");
					$main->testUnsubscribeForumPost();
					break;
				case "subscribe-forum":
					Utils::includeMessage("<strong>Subscribe to a forum.</strong>");
					$main->testSubscribeForum();
					break;
				case "get-forum-posts":
					Utils::includeMessage("<strong>Return the list of forum posts for the forum browse criteria.</strong>");
					$list = $main->testGetForumPosts();
					break;
				case "unsubscribe-forum":
					Utils::includeMessage("<strong>Unsubscribe to a forum.</strong>");
					$main->testUnsubscribeForum();
					break;
				case "thumbs-up-forum":
					Utils::includeMessage("<strong>Thumbs up to a forum.</strong>");
					$main->testThumbsUpForum();
					break;
				case "thumbs-down-forum":
					Utils::includeMessage("<strong>Thumbs down to a forum.</strong>");
					$main->testThumbsDownForum();
					break;
				case "star-forum":
					Utils::includeMessage("<strong>Evaluate a forum by placing a number of stars.</strong>");
					$main->testStarForum();
					break;
				case "flag-user":
					Utils::includeMessage("<strong>Flag user account.</strong>");
					$main->testFlagUser();
					break;
				case "avatar-message":
					Utils::includeMessage("<strong>Process the avatar message and return the avatars response.</strong>");
					$avatarMessage = $main->testAvatarMessage();
					break;
				case "tts":
					Utils::includeMessage("<strong>Process the speech message and return the server generate text-to-speech audio file.</strong>");
					$tts = $main->testTTS();
					Utils::includeMessage("<strong>Play Sound</strong>", null, "<a href='" . Main::$WEBSITE . "/".$tts . "'>Play</a>");
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
					$main->testSaveChannelBotMode();
					break;
				case "save-forum-bot-mode":
					Utils::includeMessage("<strong>Save the channel's bot configuration.</strong>");
					$main->testSaveForumBotMode();
					break;
				case "save-learning":
					Utils::includeMessage("<strong>Save the bot's learning configuration.</strong>");
					$main->testSaveLearning();
					break;
				case "save-voice":
					Utils::includeMessage("<strong>Save the bot's voice configuration.</strong>");
					$main->testSaveVoice();
					break;
				case "save-bot-avatar":
					Utils::includeMessage("<strong>Save the bot's avatar configuration.</strong>");
					$main->testSaveBotAvatar();
					break;
				case "train-instance":
					Utils::includeMessage("<strong>Train the bot with a new question/response pair.</strong>");
					$main->testTrainInstance();
					break;
				case "user-admin":
					Utils::includeMessage("<strong>Perform the user administration task (add or remove users, or administrators).</strong>");
					$main->testUserAdmin();
					break;
				case "create-avatar":
					Utils::includeMessage("<strong>Create the new content. Avatar.</strong>");
					$main->testCreateAvatar();
					break;
				case "create-avatar-media":
					Utils::includeMessage("<strong>Add the avatar media file to the avatar.</strong>");
					Utils::PostImageFromURL(Main::$imgUrl,[$main, 'testCreateAvatarMedia']);
					break;
				case "create-graphic-media":
					Utils::includeMessage("<strong>Add the graphic media file to the graphic.</strong>");
					Utils::PostImageFromURL(Main::$imgUrl, [$main, 'testCreateGraphicMedia']);
					break;
				case "update-user-icon":
					Utils::includeMessage("<strong>Update the user's icon. The file will be uploaded to the server.</strong>");
					Utils::PostImageFromURL(Main::$imgUrl, [$main, 'testUpdateUserIcon']);
					break;
				case "get-forum-bot-mode":
					Utils::includeMessage("<strong>Return the forum's bot configuration.</strong>");
					$botModeConfig = $main->testGetForumBotMode();
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
					$main->testSaveScriptSource();
					break;
				case "get-bot-script-source":
					Utils::includeMessage("<strong>Return the source code for a single bot script.</strong>");
					$main->testGetBotScriptSource();
					break;
				case "get-bot-scripts":
					Utils::includeMessage("<strong>Return a list of the bots scripts.</strong>");
					$list = $main->testGetBotScripts();
					break;
				case "import-bot-script":
					Utils::includeMessage("<strong>import a script to the bot.</strong>");
					$main->testImportBotScript();
					break;
				case "import-bot-log":
					Utils::includeMessage("<strong>import a chatlog/response list to the bot.</strong>");
					$main->testImportBotLog();
					break;
				case "save-bot-script-source":
					Utils::includeMessage("<strong>Save the bot script source.</strong>");
					$main->testSaveBotScript();
					break;
				case "delete-bot-script":
					Utils::includeMessage("<strong>Delete selected bot script.</strong>");
					$main->testDeleteBotScript();
					break;
				case "up-bot-script":
					Utils::includeMessage("<strong>Move up one bot script.</strong>");
					$main->testUpBotScript();
					break;
				case "down-bot-script":
					Utils::includeMessage("<strong>Move down one bot script.</strong>");
					$main->testDownBotScript();
					break;
				case "save-response":
					Utils::includeMessage("<strong>Create, update, flag, unflag or invalidate a response.</strong>");
					$main->testSaveResponse();
					break;
			}
		}
		?>
	</fieldset>

	<!-- 
		A compact box that displays the returned readable 
		information resulting from the executed test requests.
	-->
	<div class="boxRight">
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