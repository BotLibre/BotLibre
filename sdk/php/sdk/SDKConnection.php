<?php
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


require_once "Credentials.php";
require_once('./config/UserConfig.php');
require_once('./config/Config.php');
require_once('./config/ChatConfig.php');
require_once('./config/ChatResponse.php');
require_once('./config/DomainConfig.php');
require_once('./config/ForumPostConfig.php');
require_once('./config/UserMessageConfig.php');
require_once('./config/ResponseConfig.php');
require_once('./config/ForumPostConfig.php');
require_once('./config/MediaConfig.php');
require_once('./config/AvatarMedia.php');
require_once('./config/BrowseConfig.php');
require_once('./config/ContentConfig.php');
require_once('./config/AvatarMessage.php');
require_once('./config/Speech.php');
require_once('./config/InstanceConfig.php');
require_once('./config/AvatarConfig.php');
require_once('./config/ChannelConfig.php');
require_once('./config/BotModeConfig.php');
require_once('./config/LearningConfig.php');
require_once('./config/VoiceConfig.php');
require_once('./config/ScriptConfig.php');
require_once('./config/ScriptSourceConfig.php');
require_once('./config/ResponseSearchConfig.php');
require_once('./config/TrainingConfig.php');
require_once('./config/GraphicConfig.php');


class SDKConnection
{
	protected static $types = array("Bots", "Forums", "Graphics", "Live Chat", "Domains", "Scripts", "IssueTracker");
	protected static $channelTypes = array("ChatRoom", "OneOnOne");
	protected static $accessModes = array("Everyone", "Users", "Members", "Administrators");
	protected static $mediaAccessModes = array("Everyone", "Users", "Members", "Administrators", "Disabled");
	protected static $learningModes = array("Disabled", "Administrators", "Users", "Everyone");
	protected static $correctionModes = array("Disabled", "Administrators", "Users", "Everyone");
	protected static $botModes = array("ListenOnly", "AnswerOnly", "AnswerAndListen");
	protected string $url;
	protected ?UserConfig $user;
	protected ?DomainConfig $domain;
	protected Credentials $credentials;
	protected bool $debug = false;

	protected SDKException $exception;


	/**
	 * Return the name of the default user image.
	 */
	function defaultUserImage()
	{
		return "images/user-thumb.jpg";
	}
	/**
	 * Create an SDK connection with the credentials.
	 * Use the Credentials subclass specific to your server.
	 */
	public function __construct(Credentials $credentials)
	{
		$this->credentials = $credentials;
		$this->url = $credentials->url;
	}
	/**
	 * Validate the user credentials (password, or token).
	 * The user details are returned (with a connection token, password removed).
	 * The user credentials are soted in the connection, and used on subsequent calls.
	 * An SDKException is thrown if the connect failed.
	 */
	public function connect(UserConfig $config): ?UserConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/check-user", $config->toXML());
		if ($xml == null) {
			$this->user == null;
			return null;
		}
		try {
			$user = new UserConfig();
			$user->parseXML($xml);
			$this->user = $user;
		} catch (Exception $exception) {
			echo "Exception: " . $exception->getMessage() . "\n";
		}

		return $this->user;
	}



	/**
	 * Execute the custom API.
	 */
	public function custom(string $api, Config $config, Config $result): ?Config
	{ //Need Testing
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/" . $api, $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$result->parseXML($xml);
		} catch (Exception $exception) {

			echo "Error: " . $exception->getMessage();
		}
		return $result;
	}

	/**
	 * Connect to the live chat channel and return a LiveChatConnection.
	 * A LiveChatConnection is separate from an SDKConnection and uses web sockets for
	 * asynchronous communication.
	 * The listener will be notified of all messages.
	 */
	// public function openLiveChat(ChannelConfig $channel, LiveChatListener $listener) : LiveChatConnection {
	// 	LiveChatConnection $connection = new LiveChatConnection($this->credentials, $listener);
	// 	$connection->connect($channel, $this->user);
	// 	return $connection;
	// }

	/**
	 * Connect to the domain.
	 * A domain is an isolated content space.
	 * Any browse or query request will be specific to the domain's content.
	 */
	public function connectDomain(DomainConfig $config) : DomainConfig {
		$this->domain = $this->fetch($config);
		return $this->domain;
	}

	/**
	 * Disconnect from the connection.
	 * An SDKConnection does not keep a live connection, but this resets its connected user and domain.
	 */
	public function disconnect()
	{
		$this->user = null;
		$this->domain = null;
	}

	/**
	 * Process the bot chat message and return the bot's response.
	 * The ChatConfig should contain the conversation id if part of a conversation.
	 * If a new conversation the conversation id i returned in the response.
	 */
	public function chat(ChatConfig $config): ?ChatResponse
	{ //Tested
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/post-chat", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$response = new ChatResponse();
			$response->parseXML($xml);
			return $response;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Fetch the content details from the server.
	 * The id or name and domain of the object must be set.
	 */
	public function fetch($config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/check-" . $config->getType(), $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$config->parseXML($xml);
			return $config;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create the new content.
	 * The content will be returned with its new id.
	 */
	public function create($config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/create-" . $config->getType(), $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$config->parseXML($xml);
			return $config;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}




	/**
	 * Process the avatar message and return the avatars response.
	 * This allows the speech and video animation for an avatar to be generated for the message.
	 */
	public function avatarMessage(AvatarMessage $config): ?ChatResponse
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/avatar-message", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$response = new ChatResponse();
			$response->parseXML($xml);
			return $response;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage(); //Misssing implementation of SDKException.parseFailure(exception);
		}
	}


	/**
	 * Fetch the user details.
	 * Function names can't be the same.
	 */
	public function fetchUser(UserConfig $config): ?UserConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/view-user", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$user = new UserConfig();
			$user->parseXML($xml);
			return $user;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Fetch the URL for the image from the server.
	 */
	public function fetchImage(string $image): string
	{
		return "http://" . $this->credentials->host . $this->credentials->app . "/" . $image;
	}

	/**
	 * Fetch the forum post details for the forum post id.
	 */
	public function fetchForumPost(ForumPostConfig $config): ?ForumPostConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/check-forum-post", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$post = new ForumPostConfig();
			$post->parseXML($xml);
			return $post;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create a new user.
	 */
	public function createUser(UserConfig $config): ?UserConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/create-user", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$user = new UserConfig();
			$user->parseXML($xml);
			$this->user = $user;
			return $user;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}
	/**
	 * Update the user details.
	 * The password must be passed to allow the update.
	 */
	public function updateUser(UserConfig $config): ?UserConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/update-user", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$user = new UserConfig();
			$user->parseXML($xml);
			$this->user = $user;
			return $user;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create a new forum post.
	 * You must set the forum id for the post.
	 */
	public function createForumPost(ForumPostConfig $config): ?ForumPostConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/create-forum-post", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$post = new ForumPostConfig();
			$post->parseXML($xml);
			return $post;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create a new file/image/media attachment for a chat channel.
	 */
	public function createChannelFileAttachment(string $file, MediaConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POSTFILE($this->url . "/create-channel-attachment", $file, $config->name, $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$media = new MediaConfig();
			$media->parseXML($xml);
			return $media;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Create a new file/image/media attachment for a chat channel.
	 */
	public function createChannelImageAttachment(string $file, MediaConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POSTIMAGE($this->url . "/create-channel-attachment", $file, $config->name, $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$media = new MediaConfig();
			$media->parseXML($xml);
			return $media;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create a reply to a forum post.
	 * You must set the parent id for the post replying to.
	 */
	public function createReply(ForumPostConfig $config): ?ForumPostConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/create-reply", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$reply = new ForumPostConfig();
			$reply->parseXML($xml);
			return $reply;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create a user message.
	 * This can be used to send a user a direct message.
	 * SPAM will cause your account to be deleted.
	 */
	public function createUserMessage(UserMessageConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/create-user-message", $config->toXML());
	}

	/**
	 * Update the forum post.
	 */
	public function updateForumPost(ForumPostConfig $config): ?ForumPostConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/update-forum-post", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$config = new ForumPostConfig();
			$config->parseXML($xml);
			return $config;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Create or update the response.
	 * This can also be used to flag, unflag, validate, or invalidate a response.
	 */
	public function saveResponse(?ResponseConfig $config): ?ResponseConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/save-response", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$response = new ResponseConfig();
			$response->parseXML($xml);
			return $response;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}



	/**
	 * Permanently delete the content with the id.
	 */
	public function delete(WebMediumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/delete-" . $config->getType(), $config->toXML());
		if (!isset($this->domain) && $this->domain->id === $config->id && $config->getType() === "domain") {
			$this->domain = null;
		}
	}



	/**
	 * Permanently delete the forum post with the id.
	 */
	public function deleteForumPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/delete-forum-post", $config->toXML());
	}

	/**
	 * Permanently delete the response, greetings, or default response with the response id (and question id).
	 */
	public function deleteResponse(ResponseConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/delete-response", $config->toXML());
	}

	/**
	 * Permanently delete the avatar media.
	 */
	public function deleteAvatarMedia(AvatarMedia $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/delete-avatar-media", $config->toXML());
	}

	/**
	 * Permanently delete the avatar background.
	 */
	public function deleteAvatarBackground(AvatarConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/delete-avatar-background", $config->toXML());
	}

	/**
	 * Save the avatar media tags.
	 */
	public function saveAvatarMedia(AvatarMedia $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/save-avatar-media", $config->toXML());
	}


	/**
	 * Flag the content as offensive, a reason is required.
	 */
	public function flag(WebMediumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/flag-" . $config->getType(), $config->toXML());
	}


	/**
	 * Subscribe for email updates for the post.
	 */
	public function subscribeForumPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/subscribe-post", $config->toXML());
	}

	/**
	 * Subscribe for email updates for the forum.
	 */
	public function subscribeForum(ForumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/subscribe-forum", $config->toXML());
	}


	/**
	 * Unsubscribe from email updates for the post.
	 */
	public function unsubscribeForumPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/unsubscribe-post", $config->toXML());
	}

	/**
	 * Unsubscribe for email updates for the forum.
	 */
	public function unsubscribeForum(ForumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/unsubscribe-forum", $config->toXML());
	}

	/**
	 * Thumbs up the content.
	 */
	public function thumbsUp(WebMediumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/thumbs-up-" . $config->getType(), $config->toXML());
	}

	/**
	 * Thumbs down the content.
	 */
	public function thumbsDown(WebMediumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/thumbs-down-" . $config->getType(), $config->toXML());
	}

	/**
	 * Rate the content.
	 */
	public function star(WebMediumConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/star-" . $config->getType(), $config->toXML());
	}

	/**
	 * Thumbs up the content.
	 */
	public function thumbsUpPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/thumbs-up-post", $config->toXML());
	}

	/**
	 * Thumbs down the content.
	 */
	public function thumbsDownPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/thumbs-down-post", $config->toXML());
	}

	/**
	 * Rate the content.
	 */
	public function starPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/start-post", $config->toXML());
	}

	/**
	 * Flag the forum post as offensive, a reason is required.
	 */
	public function flagForumPost(ForumPostConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/flag-forum-post", $config->toXML());
	}

	/**
	 * Flag the user as offensive, a reason is required.
	 */
	public function flagUser(UserConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/flag-user", $config->toXML());
	}

	/**
	 * Return the forum's bot configuration.
	 */
	public function getForumBotMode(ForumConfig $config): ?BotModeConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-forum-bot-mode", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$botMode = new BotModeConfig();
			$botMode->parseXML($xml);
			return $botMode;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Return the bot's voice configuration.
	 */
	public function getVoice(InstanceConfig $config) : ?VoiceConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-voice", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$voice = new VoiceConfig();
			$voice->parseXML($xml);
			return $voice;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the bot's default responses.
	 */

	public function getDefaultResponses(InstanceConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-default-responses", $config->toXML());
		$defaultResponses = array();
		if ($xml == null) {
			return $defaultResponses;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				array_push($defaultResponses, $element);
			}
			return $defaultResponses;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the bot's greetings.
	 */
	public function getGreetings(InstanceConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-greetings", $config->toXML());
		$greetings = array();
		if ($xml == null) {
			return $greetings;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				array_push($greetings, $element);
			}
			return $greetings;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}
	/**
	 * Search the bot's responses.
	 */
	public function getResponses(ResponseSearchConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-responses", $config->toXML());
		$responses = array();
		if ($xml == null) {
			return $responses;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				$response = new ResponseConfig();
				$response->parseXML($element);
				array_push($responses, $response);
			}
			return $responses;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}
	/**
	 * Search the bot's conversations.
	 */
	public function getConversations(ResponseSearchConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-converstaions", $config->toXML());
		$conversations = array();
		if ($xml == null) {
			return $conversations;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				$response = new ConversationConfig();
				$response->parseXML($element);
				array_push($conversations, $response);
			}
			return $conversations;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Return the bot's learning configuration.
	 */
	public function getLearning(InstanceConfig $config): ?LearningConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-learning", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$learning = new LearningConfig();
			$learning->parseXML($xml);
			return $learning;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the list of content for the browse criteria.
	 * The type defines the content type (one of Bot, Forum, Channel, Domain).
	 */
	public function browse(BrowseConfig $config)
	{
		$config->addCredentials($this);
		$type = "";
		if($config->type == "Bot") {
			$type = "/get-instances";
		} else {
			$type = "/get-" . strtolower($config->type) . "s";
		}
		echo "BROWSE - TYPE: " . $type;
		$xml = $this->POST($this->url . $type, $config->toXML());
		$instances = array();
		if ($xml == null) {
			return $instances;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				$instance = null;
				if($config->type === "Bot") {
					$instance = new InstanceConfig();
				} else if($config->type === "Forum") {
					$instance = new ForumConfig();
				} else if($config->type === "Channel") {
					$instance = new ChannelConfig();
				} else if($config->type === "Domain") {
					$instance = new DomainConfig();
				} else if($config->type === "Avatar") {
					$instance = new AvatarConfig();
				} else if($config->type === "Script") {
					$instance = new ScriptConfig();
				} else if($config->type === "Graphic") {
					$instance = new GraphicConfig();
				}
				$instance->parseXML($element);
				array_push($instances, $instance);
			}
			return $instances;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}



	/**
	 * Return the list of media for the avatar.
	 */
	public function getAvatarMedia(AvatarConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-avatar-media", $config->toXML());
		$instances = array();
		if ($xml == null) {
			return $instances;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				$instance = new AvatarMedia();
				$instance->parseXML($element);
				array_push($instances, $instance);
			}
			return $instances;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the script source.
	 */
	public function getScriptSource(ScriptConfig $config): ?ScriptSourceConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-script-source", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$script = new ScriptSourceConfig();
			$script->parseXML($xml);
			return $script;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Create or update script - Save the script source
	 */
	public function saveScriptSource(ScriptSourceConfig $config) : void {
		$config->addCredentials($this);
		$this->POST($this->url . "/save-script-source", $config->toXML());
	}



	/**
	 * Return the source code for a single bot script
	 */
	public function getBotScriptSource(ScriptSourceConfig $config): ?ScriptSourceConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-bot-script-source", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$botScript = new ScriptSourceConfig();
			$botScript->parseXML($xml);
			return $botScript;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Return a list of the bots scripts.
	 */
	public function getBotScripts(InstanceConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-bot-scripts", $config->toXML());
		$botScripts = array();
		if ($xml == null) {
			return $botScripts;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			}
			// else {
			//     print_r($xmlData);
			// }
			foreach ($xmlData as $element) {
				$script = new ScriptConfig();
				$script->parseXML($element);
				array_push($botScripts, $script);
			}
			return $botScripts;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * import a script to the bot
	 */
	public function importBotScript(ScriptConfig $config) {
		$config->addCredentials($this);
		$this->POST($this->url . "/import-bot-script" , $config->toXML());
	}



	/**
	 * import a chatlog/response list to the bot
	 */
	public function importBotLog(ScriptConfig $config) {
		$config->addCredentials($this);
		$this->POST($this->url . "/import-bot-log" , $config->toXML());
	}

	/**
	 * Save the bot script source
	 */
	public function saveBotScriptSource (ScriptSourceConfig $config) {
		$config->addCredentials($this);
		$this->POST($this->url . "/save-bot-script-source" , $config->toXML());
	}

	/**
	 * Delete selected bot script
	 */
	public function deleteBotScript(ScriptSourceConfig $config) {
		$config->addCredentials($this);
		$this->POST($this->url . "/delete-bot-script", $config->toXML());
	}

	/**
	 * Move up one bot script
	 */
	public function upBotScript(ScriptSourceConfig $config) {
		$config->addCredentials($this);
		$this->POST($this->url . "/up-bot-script", $config->toXML());
	}
	
	/**
	 * Move down one bot script
	 */
	public function downBotScript(ScriptSourceConfig $config) {
		$config->addCredentials($this);
		$this->POST($this->url . "/down-bot-script",  $config->toXML());
	}

	/**
	 * Process the speech message and return the server generate text-to-speech audio file.
	 * This allows for server-side speech generation.
	 */
	public function tts(Speech $config): ?string
	{
		$config->addCredentials($this);
		return $this->POST($this->url . "/speak", $config->toXML());
	}



	/**
	 * Return the list of content types.
	 */
	public function getTypes()
	{
		return $this->types;
	}

	/**
	 * Return the channel types.
	 */
	public function getChannelTypes()
	{
		return $this->channelTypes;
	}

	/**
	 * Return the access mode types.
	 */
	public function getAccessModes()
	{
		return $this->accessModes;
	}

	/**
	 * Return the media access mode types.
	 */
	public function getMediaAccessModes()
	{
		return $this->mediaAccessModes;
	}

	/**
	 * Return the learning mode types.
	 */
	public function getLearningModes()
	{
		return $this->learningModes;
	}

	/**
	 * Return the correction mode types.
	 */
	public function getCorrectionModes()
	{
		return $this->correctionModes;
	}

	/**
	 * Return the bot mode types.
	 */
	public function getBotModes()
	{
		return $this->botModes;
	}

	/**
	 * Return the current connected user.
	 */
	public function getUser(): UserConfig
	{
		return $this->user;
	}

	/**
	 * Set the current connected user.
	 * connect() should be used to validate and connect a user.
	 */
	public function setUser(UserConfig $user): void
	{
		$this->user = $user;
	}

	/**
	 * Return the current domain.
	 * A domain is an isolated content space.
	 */
	public function getDomain(): ?DomainConfig
	{
		if (isset($this->domain)) {
			return $this->domain;
		}
		return null;
	}

	/**
	 * Set the current domain.
	 * A domain is an isolated content space.
	 * connect() should be used to validate and connect a domain.
	 */
	public function setDomain(DomainConfig $domain): void
	{
		$this->domain = $domain;
	}

	/**
	 * Return the current application credentials.
	 */
	public function getCredentials(): ?Credentials
	{
		if ($this->credentials == null) {
			var_dump($this->credentials);
			echo "(SDKConnection) This credentials is null.";
			return null;
		}
		return $this->credentials;
	}

	/**
	 * Set the application credentials.
	 */
	public function setCredentials(Credentials $credentials): void
	{
		$this->credentials = $credentials;
		$this->url = $credentials->url;
	}

	/**
	 * Return is debugging has been enabled.
	 */
	public function isDebug(): bool
	{
		return $this->debug;
	}

	/**
	 * Enable debugging, debug messages will be logged to System.out.
	 */
	public function setDebug(bool $debug): void
	{
		$this->debug = $debug;
	}

	/**
	 * Return the administrators of the content.
	 */
	public function getAdmins(WebMediumConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-" . $config->getType() . "-admins", $config->toXML());
		$users = array();
		if ($users == null) {
			return $users;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					$user = new UserConfig();
					$user->parseXML($element);
					array_push($users, $user->user);
				}
			}
			return $users;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the list of user details for the comma separated values list of user ids.
	 */
	public function getUsers(string $usersCSV)
	{
		$config = new UserConfig();
		$config->user = $usersCSV;
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-users", $config->toXML());
		$users = array();
		if ($xml == null) {
			return $users;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					$userConfig = new UserConfig();
					$userConfig->parseXML($element);
					array_push($users, $userConfig);
				}
			}
			return $users;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Return the list of forum posts for the forum browse criteria.
	 */
	public function getPosts(BrowseConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-forum-posts", $config->toXML());
		$instances = array();
		if ($xml == null) {
			return $instances;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					$post = new ForumPostConfig();
					$post->parseXML($element);
					array_push($instances, $post);
				}
			}
			return $instances;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the list of categories for the type, and domain.
	 */
	public function getCategories(ContentConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-categories", $config->toXML());
		$categories = array();
		if ($xml == null) {
			return $categories;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					$category = new ContentConfig();
					$category->parseXML($element);
					array_push($categories, $category);
				}
			}
			return $categories;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/*
	 *Return the list of tags for the type, and domain.
	 */
	public function getTags(ContentConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-tags", $config->toXML());
		$tags = array();
		array_push($tags, "");
		if ($xml == null) {
			return $tags;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					//tags.add(((Element)root.getChildNodes().item(index)).getAttribute("name"));
					array_push($tags, $element);
				}
			}
			return $tags;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Return the list of bot templates.
	 */

	//return the list of templates with the pictures.
	public function getTemplates()
	{
		$xml = $this->GET($this->url . "/get-all-templates");
		$instances = array();
		if ($xml == null) {
			return $instances;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					$instance = new InstanceConfig();
					$instance->parseXML($element);
					array_push($instances, $instance);
				}
			}
			return $instances;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}


	/**
	 * Return the users for the content.
	 */
	public function getUsersOfType(WebMediumConfig $config)
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-" . $config->getType() . "-users", $config->toXML());
		$users = array();
		if ($xml == null) {
			return $users;
		}
		try {
			$xmlData = simplexml_load_string($xml);
			if ($xmlData === false) {
				echo "Failed loading XML: ";
				foreach (libxml_get_errors() as $error) {
					echo "<br>", $error->message;
				}
			} else {
				foreach ($xmlData as $element) {
					$user = new UserConfig();
					$user->parseXML($element);
					array_push($users, $user->user);
				}
			}
			return $users;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Return the channel's bot configuration.
	 */
	public function getChannelBotMode(ChannelConfig $config): ?BotModeConfig
	{
		$config->addCredentials($this);
		$xml = $this->POST($this->url . "/get-channel-bot-mode", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$botMode = new BotModeConfig();
			$botMode->parseXML($xml);
			return $botMode;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	/**
	 * Save the channel's bot configuration.
	 */
	public function saveChannelBotMode(BotModeConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url + "/save-channel-bot-mode", $config->toXML());
	}


	/**
	 * Save the forum's bot configuration.
	 */
	public function saveForumBotMode(BotModeConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url + "/save-forum-bot-mode", $config->toXML());
	}

	/**
	 * Save the bot's learning configuration.
	 */
	public function saveLearning(LearningConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url + "/save-learning", $config->toXML());
	}


	/**
	 * Save the bot's voice configuration.
	 */
	public function saveVoice(VoiceConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url + "/save-voice", $config->toXML());
	}

	/**
	 * Save the bot's avatar configuration.
	 */
	public function saveBotAvatar(InstanceConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url + "/save-bot-avatar", $config->toXML());
	}

	/**
	 * Train the bot with a new question/response pair.
	 */
	public function train(TrainingConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/train-instance", $config->toXML());
	}


	/**
	 * Perform the user administration task (add or remove users, or administrators).
	 */
	public function userAdmin(UserAdminConfig $config): void
	{
		$config->addCredentials($this);
		$this->POST($this->url . "/user-admin", $config->toXML());
	}


	/**
	 * Save the image as the avatar's background.
	 */
	public function saveAvatarBackground(string $file, AvatarMedia $config): void
	{
		$config->addCredentials($this);
		$this->POSTIMAGE($this->url . "/save-avatar-background", $file, $config->name, $config->toXML());
	}

	/**
	 * Add the avatar media file to the avatar.
	 */
	public function createAvatarMedia(string $file, AvatarMedia $config): void
	{
		$config->addCredentials($this);
		if ((pathinfo($file, PATHINFO_EXTENSION) === "jpg") || (pathinfo($file, PATHINFO_EXTENSION) === "jpeg")) {
			if ($config->hd) {
				$this->POSTHDIMAGE($this->url . "/create-avatar-media", $file, $config->name, $config->toXML());
			} else {
				$this->POSTIMAGE($this->url . "/create-avatar-media", $file, $config->name, $config->toXML());
			}
		} else {
			$this->POSTFILE($this->url . "/create-avatar-media", $file, $config->name, $config->toXML());
		}
	}

	/**
	 * Add the graphic media file to the graphic.
	 */
	public function createGraphicMedia(string $file, GraphicConfig $config): void
	{
		$config->addCredentials($this);
		if ((pathinfo($file, PATHINFO_EXTENSION) === "jpg") || (pathinfo($file, PATHINFO_EXTENSION) === "jpeg")) {
			$this->POSTIMAGE($this->url . "/update-graphic-media", $file, $config->fileName, $config->toXML());
		} else {
			$this->POSTFILE($this->url . "/update-graphic-media", $file, $config->fileName, $config->toXML());
		}
	}


	/**
	 * Update the user's icon.
	 * The file will be uploaded to the server.
	 */
	public function updateIconUser(string $file, UserConfig $config): ?UserConfig
	{
		$config->addCredentials($this);
		$xml = $this->POSTIMAGE($this->url . "/update-user-icon", $file, "image.jpg", $config->toXML());
		if ($xml == null) {
			return null;
		}
		try {
			$config = new UserConfig();
			$config->parseXML($xml);
			return $config;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}



	public function GET(string $url): string
	{
		if ($this->debug) {
			$debugComment = "GET_URL: " . $url;
			$debugInfo = $url;
			include "./views/debug.php";
		}
		$curl = curl_init();
		curl_setopt($curl, CURLOPT_URL, $url);
		curl_setopt($curl, CURLOPT_HTTPGET, true);
		curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
		//curl_setopt($ch, CURLOPT_HEADER, 1);

		// $headers = [
		// 	'Content-Type: application/xml',
		// 	'Accept: application/xml'
		// ];

		// curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
		$response = curl_exec($curl);
		if ($e = curl_error($curl)) {
			echo $e;
		} else {
			if ($this->debug) {
				if (isset($response) || $response !== null) {
					$result = simplexml_load_string($response) or die("Error: Cannot create object");
					$debugComment = "Result after the request.";
					$debugInfo = $result;
					include "./views/debug.php";
				}
			}
		}
		curl_close($curl);
		return $response;
	}
	public function POST(string $url, string $xml): string
	{
		if ($this->debug) {
			$debugComment = "POST_URL: " . $url;
			$debugInfo = htmlentities($xml);
			include "./views/debug.php";
		}
		$curl = curl_init();
		$xmlData = simplexml_load_string($xml) or die("Error: Prior of xml request. Cannot create object");
		if ($this->debug) {
			$debugComment = "POST: Sending xml request.";
			$debugInfo = $xmlData;
			include "./views/debug.php";
		}

		curl_setopt($curl, CURLOPT_URL, $url);
		curl_setopt($curl, CURLOPT_POST, true);
		curl_setopt($curl, CURLOPT_POSTFIELDS, $xml); //It needs the actual xml text not the object
		// The result of simplexml_load_string($xml) passing a string xml will return a data object xml.
		// curl_setopt just need a string text of the xml.
		curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
		//curl_setopt($curl, CURLOPT_HEADER, 1);

		$headers = [
			'Content-Type: application/xml',
			'Accept: application/xml'
		];

		curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
		$response = curl_exec($curl);
		if ($e = curl_error($curl)) {
			echo $e;
		} else {
			if ($this->debug) {
				$this->checkResponse($response);
			}
		}
		curl_close($curl);
		return $response;
	}


	public function POSTIMAGE(string $url, string $file, string $name, string $xml): ?string
	{
		if ($this->debug) {
			$debugComment = "POST_FILE: " . $name . "<br>" . $url . "<br>" . $file;
			$debugInfo = htmlentities($xml);
			include "./views/debug.php";
		}
		try {
			$curl = curl_init();
			$xmlData = simplexml_load_string($xml) or die("Error: Prior of xml request. Cannot create object");
			if ($this->debug) {
				$debugComment = "POST: Sending xml request.";
				$debugInfo = $xmlData;
				include "./views/debug.php";
			}
			curl_setopt($curl, CURLOPT_URL, $url);
			$originalImage = imagecreatefromjpeg($file);
			$resizedImage = imagecreatetruecolor(300, 300);

			//Resizing the image
			imagecopyresampled($resizedImage, $originalImage, 0, 0, 0, 0, 300, 300, imagesx($originalImage), imagesy($originalImage));

			// Create a temporary file to store the resized image
			$tmpFilename = tempnam(sys_get_temp_dir(), 'upload');
			imagejpeg($resizedImage, $tmpFilename, 90);
			//curl file object
			$fileBody = new CURLFile($tmpFilename, 'image/jpeg', $name);

			$postData = array(
				'file' => $fileBody,
				'xml' => $xml
			);

			curl_setopt($curl, CURLOPT_POST, true);
			curl_setopt($curl, CURLOPT_POSTFIELDS, $postData);
			curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
			$headers = [
				'Content-Type: multipart/form-data',
				'Accept: text/plain'
			];

			curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);


			ob_start(); //Start output buffering
			print_r($fileBody);
			echo ob_get_clean() . "<br>"; //End output buffering. 
			//Get the output buffer contents and clear the buffer

			//Execute request
			$response = curl_exec($curl);
			
			//Check for errors
			if (curl_errno($curl)) {
				echo "Error: " . curl_error($curl);
			} else {
				if ($this->debug) {
					$this->checkResponse($response);
				}
			}
			//Clean up
			curl_close($curl);
			imagedestroy($originalImage);
			imagedestroy($resizedImage);
			unlink($tmpFilename);
			return $response;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	public function checkResponse($response): void
	{
		libxml_use_internal_errors(true); //Enable internal error handling
		if (isset($response) || $response !== null) {
			$debugComment = "<br>Result after the request.";
			$result = simplexml_load_string($response);
			if ($result === false) {
				$debugInfo = "";
				$debugInfo .= $response;
				// XML parsing failed, handle the error
				$errors = libxml_get_errors();
				foreach ($errors as $error) {
					$debugInfo .= "<br>XML Error: " . $error->message;
				}
				libxml_clear_errors(); //Clear the error buffer
			} else {
				$debugInfo = $result;
			}
			include "./views/debug.php";
		}
		libxml_use_internal_errors(false); //Disable 
	}


	public function POSTHDIMAGE(string $url, string $file, string $name, string $xml): ?string
	{
		if ($this->debug) {
			$debugComment = "POST_FILE: " . $name . "<br>" . $url . "<br>" . $file;
			$debugInfo = htmlentities($xml);
			include "./views/debug.php";
		}
		try {
			$curl = curl_init();
			$xmlData = simplexml_load_string($xml) or die("Error: Prior of xml request. Cannot create object");
			if ($this->debug) {
				$debugComment = "POST: Sending xml request.";
				$debugInfo = $xmlData;
				include "./views/debug.php";
			}
			curl_setopt($curl, CURLOPT_URL, $url);
			$originalImage = imagecreatefromjpeg($file);
			$resizedImage = imagecreatetruecolor(600, 600);

			//Resizing the image
			imagecopyresampled($resizedImage, $originalImage, 0, 0, 0, 0, $desiredWidth, $desiredHeight, imagesx($originalImage), imagesy($originalImage));

			//Compress the resized image to a JPEG byte array
			//there is a code
			ob_start();
			imagejpeg($resizedImage, null, 90);
			$byte_arr = ob_get_clean();
			//curl file object
			$fileBody = new CURLFile(null, 'image/jpeg', $name);
			//Data
			$postData = array(
				'file' => $fileBody,
				'xml' => $xml
			);

			curl_setopt($curl, CURLOPT_POST, true);
			curl_setopt($curl, CURLOPT_POSTFIELDS, $postData);
			curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
			$headers = [
				'Content-Type: application/xml',
				'Accept: application/xml'
			];

			curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
			//Execute request
			$response = curl_exec($curl);

			//Check for errors
			if (curl_errno($curl)) {
				echo "Error: " . curl_error($curl);
			} else {
				if ($this->debug) {
					if (isset($response) || $response !== null) {
						$result = simplexml_load_string($response) or die("Error: Cannot create object");
						$debugComment = "Result after the request.";
						$debugInfo = $result;
						include "./views/debug.php";
					}
				}
			}
			curl_close($curl);
			echo $response;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}

	public function POSTFILE(string $url, string $path, string $name, string $xml): ?string
	{
		if ($this->debug) {
			$debugComment = "POST_FILE: " . $name . "<br>" . $url . "<br>" . $path;
			$debugInfo = htmlentities($xml);
			include "./views/debug.php";
		}
		try {
			$ch = curl_init();
			$xmlData = simplexml_load_string($xml) or die("Error: Prior of xml request. Cannot create object");
			if ($this->debug) {
				$debugComment = "POST: Sending xml request.";
				$debugInfo = $xmlData;
				include "./views/debug.php";
			}
			curl_setopt($curl, CURLOPT_URL, $url);
			//curl file object
			$file = new CURLFile($path, mime_content_type($path), basename($path));
			//Data
			$postData = array(
				'file' => $file,
				'xml' => $xml
			);

			curl_setopt($curl, CURLOPT_POST, true);
			curl_setopt($curl, CURLOPT_POSTFIELDS, $postData);
			curl_setopt($curl, CURLOPT_RETURNTRANSFER, true);
			$headers = [
				'Content-Type: multipart/form-data',
				'Accept: application/xml' //text/plain
			];

			curl_setopt($curl, CURLOPT_HTTPHEADER, $headers);
			//Execute request
			$response = curl_exec($curl);

			//Check for errors
			if (curl_errno($curl)) {
				echo "Error: " . curl_error($curl);
			} else {
				if ($this->debug) {
					if (isset($response) || $response !== null) {
						$result = simplexml_load_string($response) or die("Error: Cannot create object");
						$debugComment = "Result after the request.";
						$debugInfo = $result;
						include "./views/debug.php";
					}
				}
			}
			curl_close($curl);
			echo $response;
		} catch (Exception $exception) {
			echo "Error: " . $exception->getMessage();
		}
	}
}
?>