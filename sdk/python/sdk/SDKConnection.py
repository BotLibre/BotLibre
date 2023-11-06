############################################################################
#
#  Copyright 2023 Paphus Solutions Inc.
#
#  Licensed under the Eclipse Public License, Version 1.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.eclipse.org/legal/epl-v10.html
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
############################################################################
import requests
from typing import List
from config.Config import Config
from config.UserConfig import UserConfig
from config.DomainConfig import DomainConfig
from config.ChatConfig import ChatConfig
from sdk.Credentials import Credentials
from util.Utils import Utils, Writer
from config.ChatResponse import ChatResponse
from config.ForumPostConfig import ForumPostConfig
from config.WebMediumConfig import WebMediumConfig
from config.MediaConfig import MediaConfig
from config.UserMessageConfig import UserMessageConfig
from config.AvatarMessage import AvatarMessage
from config.ResponseConfig import ResponseConfig
from config.AvatarMedia import AvatarMedia
from config.AvatarConfig import AvatarConfig
from config.ForumConfig import ForumConfig
from config.BrowseConfig import BrowseConfig
from config.BotModeConfig import BotModeConfig
from config.InstanceConfig import InstanceConfig
from config.VoiceConfig import VoiceConfig
from config.ResponseSearchConfig import ResponseSearchConfig
from config.ConversationConfig import ConversationConfig
from config.LearningConfig import LearningConfig
from config.ChannelConfig import ChannelConfig
from config.ScriptConfig import ScriptConfig
from config.GraphicConfig import GraphicConfig
from config.Speech import Speech
from config.ContentConfig import ContentConfig
from config.TrainingConfig import TrainingConfig
from config.ScriptSourceConfig import ScriptSourceConfig



from requests_toolbelt.multipart.encoder import MultipartEncoder
from typing import TypeVar
T = TypeVar('T', bound=WebMediumConfig)

class SDKConnection(object):
    types: list
    channelTypes: list
    accessModes: list
    mediaAccessModes: list
    learningModes: list
    correctionModes: list
    botModes: list
    url: str
    credentials: Credentials
    debug: bool
    domain: str
    user: UserConfig
    # Create an SDK connection with the credentials.
    # Use the Credentials subclass specific to your server.
    def __init__(self, credentials, debug=False):
        self.types = ["Bots", "Forums", "Graphics",
                      "Live Chat", "Domains", "Scripts", "IssueTracker"]
        self.channelTypes = ["ChatRoom", "OneOnOne"]
        self.accessModes = ["Everyone", "Users", "Members", "Administrators"]
        self.mediaAccessModes = ["Everyone", "Users",
                                 "Members", "Administrators", "Disabled"]
        self.learningModes = ["Disabled",
                              "Administrators", "Users", "Everyone"]
        self.correctionModes = ["Disabled",
                                "Administrators", "Users", "Everyone"]
        self.botModes = ["ListenOnly", "AnswerOnly", "AnswerAndListen"]
        self.url = credentials.url
        self.credentials = credentials
        self.debug = debug
        self.domain = None
        self.user = None

    # Return the name of the default user image.
    def defaultUserImage():
        return "images/user-thumb.jpg"

    # Validate the user credentials (password, or token).
    # The user details are returned (with a connection token, password removed).
    # The user credentials are soted in the connection, and used on subsequent calls.
    # An SDKException is thrown if the connect failed.
    def connect(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/check-user", config.toXML())
        if(xml == None):
            self.user == None
            return None
        
        try:
            user = UserConfig()
            user.parseXML(xml)
            self.user = user
        except Exception as e:
            Utils.log_err("check-user", e)
        
        return self.user
    
    
    # Execute the custom API.
    def custom(self, api, config: Config, result: Config):
        config.addCredentials(self)
        xml = self.POST(self.url + "/" + api, config.toXML())
        if(xml == None):
            return None
        try:
            result.parseXML(xml)
        except Exception as e:
            Utils.log_err(api, e)
        return result
    
    
    # Connect to the domain.
	# A domain is an isolated content space.
	# Any browse or query request will be specific to the domain's content.
    def connectDomain(self, config: DomainConfig) -> DomainConfig:
        self.domain = self.fetch(config)
        return self.domain

    # Process the bot chat message and return the bot's response.
	# The ChatConfig should contain the conversation id if part of a conversation.
	# If a new conversation the conversation id i returned in the response.
    def chat(self, config: ChatConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/post-chat",config.toXML())
        if(xml == None):
            return None
        try:
            response = ChatResponse()
            response.parseXML(xml)
            return response
        except Exception as e:
            Utils.log_err("post-chat", e)
            
            
    # Fetch the content details from the server.
	# The id or name and domain of the object must be set.
    def fetch(self, config: T) -> T:
        config.addCredentials(self)
        xml = self.POST(self.url + "/check-" + config.getType(), config.toXML())
        if(xml == None):
            return None
        try:
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("check-" + config.getType(), e)    
    
    
    
    
    # Create the new content.
	# The content will be returned with its new id.
    def create(self, config: T) -> T:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-" + config.getType(), config.toXML())
        if(xml == None):
            return None
        try:
            config: T()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("check-" + config.getType(), e) 
        
    # Update the content.
    def update(self, config: T) -> T:
        config.addCredentials(self)
        xml = self.POST(self.url + "/update-" + config.getType(), config.toXML())
        if(xml == None):
            return None
        try:
            config: T()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("check-" + config.getType(), e)
    
    # Process the avatar message and return the avatars response.
	# This allows the speech and video animation for an avatar to be generated for the message.
    def avatarMessage(self, config: AvatarMessage) -> ChatResponse:
        config.addCredentials(self)
        xml = self.POST(self.url + "/avatar-message", config.toXML())
        if(xml == None):
            return None
        try:
            response = ChatResponse()
            response.parseXML(xml)
            return response
        except Exception as e:
            Utils.log_err("avatar-message", e)
    
    
    # Create a new user
    def createUser(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-user", config.toXML())
        if(xml == None):
            return None
        try:
            user = UserConfig()
            user.parseXML(xml)
            self.user = user
            return user
        except Exception as e:
            Utils.log_err("/create-user", e)
            
    # Update the user details.
	# The password must be passed to allow the update.
    def updateUser(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/update-user", config.toXML())
        if(xml == None):
            return None
        try:
            user = UserConfig()
            user.parseXML(xml)
            self.user = user
            return user
        except Exception as e:
            Utils.log_err("/update-user", e)
    
    
    # Permanently delete the content with the id.
    def delete(self, config: WebMediumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-" + config.getType(), config.toXML())
        if(self.domain!=None and self.domain.id == config.id and config.getType() == "domain"):
            self.domain = None
            
    
    
    # Create a new forum post.
    # You must set the forum id for the post.
    def createForumPost(self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-forum-post", config.toXML())
        if(xml == None):
            return None
        try:
            post = ForumPostConfig()
            post.parseXML(xml)
            return post
        except Exception as e:
            Utils.log_err("/create-forum-post", e)
    
    
    # Update the forum post.
    def updateForumPost(self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/update-forum-post", config.toXML())
        if(xml == None):
            return None
        try:
            post = ForumPostConfig()
            post.parseXML(xml)
            return post
        except Exception as e:
            Utils.log_err("update-forum-post" ,e)
            
    
    # Create or update the response.
	# This can also be used to flag, unflag, validate, or invalidate a response.
    def saveResponse(self, config: ResponseConfig) -> ResponseConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/save-response", config.toXML())
        if(xml == None):
            return None
        try:
            config = ResponseConfig()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("save-response", e)
    
    
    # Save the image as the avatar's background.
    def saveAvatarBackground(self, file, config: AvatarMedia):
        config.addCredentials(self)
        self.POSTIMAGE(self.url + "/save-avatar-background", file, config.name, config.toXML())
    
    
    # Add the avatar media file to the avatar.
    def createAvatarMedia(self, file, config: AvatarMedia):
        config.addCredentials(self)
        if(config.name.endswith(".jpg") or config.name.endswith(".jpeg") or config.name.endswith(".png")):
            if(config.hd): # TODO: Implement POSTHDIMAGE(..)
                self.POSTHDIMAGE(self.url + "/create-avatar-media", file, config.name, config.toXML())
            else:
                self.POSTIMAGE(self.url + "/create-avatar-media", file, config.name, config.toXML())
        else:
            self.POSTFILE(self.url + "/create-avatar-media", file, config.name, config.toXML())


    # Add the avatar media file to the avatar.
    def createGraphicMedia(self, file, config: GraphicConfig):
        config.addCredentials(self)
        if(config.fileName.endswith(".jpg") or config.fileName.endswith(".jpeg") or config.fileName.endswith(".png")):
            self.POSTIMAGE(self.url + "/update-graphic-media", file, config.fileName, config.toXML())
        else:
            self.POSTFILE(self.url + "/update-graphic-media", file, config.fileName, config.toXML())
    
    # Update the user's icon.
    # The file will be uploaded to the server.
    def updateIconUser(self, file, fileName, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POSTIMAGE(self.url + "/update-user-icon",file, fileName, config.toXML())
        if(xml == None):
            return None
        try:
            config = UserConfig()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("update-user-icon", e)
    
    # Permanently delete the forum post with the id.
    def deleteForumPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-forum-post", config.toXML())
        
    # Permanently delete the response, greetings, or default response with the response id (and question id).
    def deleteResponse(self, config: ResponseConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-response", config.toXML())
        
    # Permanently delete the avatar media.
    def deleteAvatarMedia(self, config: AvatarMedia):
        config.addCredentials(self)
        self.POST(self.url + "/delete-avatar-media", config.toXML())
    
    # Permanently delete the avatar background.
    def deleteAvatarBackground(self, config: AvatarConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-avatar-background", config.toXML())
    
    # Save the avatar media tags.
    def saveAvatarMedia(self, config: AvatarMedia):
        config.addCredentials(self)
        self.POST(self.url + "/save-avatar-media", config.toXML())
    
    # Flag the content as offensive, a reason is required.
    def flag(self, config: T):
        config.addCredentials(self)
        self.POST(self.url + "/flag-" + config.getType(), config.toXML())
    
    # Flag the user as offensive, a reason is requried
    def flagUser(self, config: UserConfig):
        config.addCredentials(self)
        self.POST(self.url + "/flag-user", config.toXML())
        
    # Subscribe for email updates for the post.
    def subscribeForumPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/subscribe-post", config.toXML())
        
    # Unsubscribe from email updates for the post.
    def unsubscribeForumPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/unsubscribe-post", config.toXML())
    
    # Subscribe for email updates for the forum.
    def subscribeForum(self, config: ForumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/subscribe-forum",config.toXML())
        
    # Unsubscribe from email updates for the forum.
    def unsubscribeForum(self, config: ForumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/unsubscribe-forum",config.toXML())
        
        
    # Thumbs up the content.
    def thumbsUp(self, config: WebMediumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/thumbs-up-"+config.getType(), config.toXML())
    
    # Thumbs down the content.
    def thumbsDown(self, config: WebMediumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/thumbs-down-" + config.getType(), config.toXML())    
    
    # Rate the content
    def star(self, config: WebMediumConfig):
        config.addCredentials(self)
        self.POST(self.url + "/star-" + config.getType(), config.toXML())
    
    # Thumbs up forum post.
    def thumbsUpPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/thumbs-up-post-"+config.getType(), config.toXML())
    
    # Thumbs down forum post.
    def thumbsDownPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/thumbs-down-post-" + config.getType(), config.toXML())    
    
    # Rate the content.
    def starPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/star-post", config.toXML())
    
    
    # Flag the forum post as offensive, a reason is required.
    def flagForumPost(self, config: ForumPostConfig):
        config.addCredentials(self)
        self.POST(self.url + "/flag-forum-post", config.toXML())
    
    
    # Flag the user as offensive, a reason is required.
    def flagUser(self, config: UserConfig):
        config.addCredentials(self)
        self.POST(self.url + "/flag-user", config.toXML())
        
    # Return the forum's bot configuration.
    def getForumBotMode(self, config: ForumConfig) -> BotModeConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-forum-bot-mode", config.toXML())
        if(xml == None):
            return None
        try:
            botMode = BotModeConfig()
            botMode.parseXML(xml)
            return botMode
        except Exception as e:
            Utils.log_err("/get-forum-bot-mode", e)
            
    # Return the bot's voice configuration.
    def getVoice(self, config: InstanceConfig) -> VoiceConfig:
        config.addCredentials(self)
        xml = self.POST(self.url+"/get-voice", config.toXML())
        if(xml==None):
            return None
        try:
            voice = VoiceConfig()
            voice.parseXML(xml)
            return voice
        except Exception as e:
            Utils.log_err("/get-voice", e)
    
    # Return the bot's default responses.
    def getDefaultResponses(self, config: InstanceConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-default-responses", config.toXML())
        responses = []
        if(xml == None):
            return None
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for item in data:
                responses.append(item.text)
            return responses
        except Exception as e:
            Utils.log_err("/get-default-responses", e)
    
    # Return the bot's greetings
    def getGreetings(self, config: InstanceConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-greetings", config.toXML())
        greetings = []
        if(xml == None):
            return None
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return None
            for item in data.iter():
                greetings.append(item.text)
            return greetings
        except Exception as e:
            Utils.log_err("/get-greetings", e)


    # Search the bot's responses.
    def getResponses(self, config: ResponseSearchConfig) -> List[ResponseConfig]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-responses", config.toXML())
        responses = []
        if(xml == None):
            return responses
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return None
            for item in data:
                responseConfig = ResponseConfig()
                responseConfig.parseXML(item)
                responses.append(responseConfig)
            return responses
        except Exception as e:
            Utils.log_err("/get-responses", e)

    # Search the bot's conversations.
    def getConversation(self, config: ResponseSearchConfig) -> List[ConversationConfig]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-conversations", config.toXML())
        conversations = []
        if(xml == None):
            return None
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return None
            for item in data:
                conversationConfig = ConversationConfig()
                conversationConfig.parseXML(item)
                conversations.append(conversationConfig)
            return conversations
        except Exception as e:
            Utils.log_err("get-conversations", e)
            
    # Return the bot's learning configuration.
    def getLearning(self, config: InstanceConfig) -> LearningConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-learning", config.toXML())
        if(xml == None):
            return None
        try:
            learningConfig = LearningConfig()
            learningConfig.parseXML(xml)
            return learningConfig
        except Exception as e:
            Utils.log_err("get-learning", e)
            
    # Return the list of content for the browse criteria.
	# The type defines the content type (one of Bot, Forum, Channel, Domain).
    def browse(self, config: BrowseConfig):
        config.addCredentials(self)
        type: str
        if config.type == "Bot":
            type = "/get-instances"
        else:
            type = "/get-" + config.type.lower() + "s"
        Utils.log("Browse", "Type: " + type)
        xml = self.POST(self.url + type, config.toXML())
        instances = []
        if(xml == None):
            return None
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return instances
            for item in data:
                instance = None
                if(config.type == "Bot"):
                    instance = InstanceConfig()
                elif (config.type == "Forum"):
                    instance = ForumConfig()
                elif (config.type == "Channel"):
                    instance = ChannelConfig()
                elif (config.type == "Domain"):
                    instance = DomainConfig()
                elif (config.type == "Avatar"):
                    instance = AvatarConfig()
                elif (config.type == "Script"):
                    instance = ScriptConfig()
                elif (config.type == "Graphic"):
                    instance = GraphicConfig()
                instance.parseXML(item)
                instances.append(instance)
            return instances
        except Exception as e:
            Utils.log_err(type, e)
    
    
    # Return the list of media for the avatar.
    def getAvatarMedia(self, config: AvatarConfig) -> List[AvatarMedia]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-avatar-media", config.toXML())
        instances = []
        if(xml == None):
            return None
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return instances
            for item in data:
                instance = AvatarMedia()
                instance.parseXML(item)
                instances.append(instance)
            return instances
        except Exception as e:
            Utils.log_err("get-avatar-media", e)
            
    
    # Return the script source
    def getScriptSource(self, config: ScriptConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-script-source", config.toXML())
        if(xml == None):
            return None
        try:
            script = ScriptSourceConfig()
            script.parseXML(xml)
            return script
        except Exception as e:
            Utils.log_err("/get-script-source", e)
            
    # Create or update script - Save the script source
    def saveScriptSource(self, config: ScriptSourceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-script-source", config.toXML())
        
    
    # Return the source code for a single bot script
    def getBotScriptSource(self, config: ScriptSourceConfig) -> ScriptSourceConfig:
        config.addCredentials(self)
        xml = self.POST(self.url+ "/get-bot-script-source", config.toXML())
        if(xml == None):
            return None
        try:
            botScript = ScriptSourceConfig()
            botScript.parseXML(xml)
            return botScript
        except Exception as e:
            Utils.log_err("/get-bot-script-source", e)
    
    # Return a list of the bots scripts
    def getBotScripts(self, config: InstanceConfig) -> List[ScriptConfig]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-bot-scripts", config.toXML())
        botScripts = []
        if(xml == None):
            return None
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for item in data:
                script = ScriptConfig()
                script.parseXML(item)
                botScripts.append(script)
            return botScripts
        except Exception as e:
            Utils.log_err("/get-bot-scripts", e)
            
    
    # Import a script to the bot
    def importBotScript(self, config: ScriptConfig):
        config.addCredentials(self)
        self.POST(self.url + "/import-bot-script", config.toXML())
        
        
    # Import a chatlog/response list to the bot
    def importBotLog(self, config: ScriptConfig):
        config.addCredentials(self)
        self.POST(self.url + "/import-bot-log", config.toXML())
        
        
    # Save the bot script source
    def saveBotScriptSource(self, config: ScriptSourceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-bot-script-source", config.toXML())
        
    
    # Delete selected bot script
    def deleteBotScript(self, config: ScriptSourceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/delete-bot-script", config.toXML())
        
    # Move up one bot script
    def upBotScript(self, config: ScriptSourceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/up-bot-script", config.toXML())
        
    # Move down one bot script
    def downBotScript(self, config: ScriptSourceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/down-bot-script", config.toXML())
    
    # Create a new file/image/media attachment for a chat channel.
    def createChannelFileAttachment(self, file, config: MediaConfig) -> MediaConfig:
        config.addCredentials(self)
        xml = self.POSTFILE(self.url + "/create-channel-attachment", file, config.name, config.toXML())
        if(xml == None):
            return None
        try:
            media = MediaConfig()
            media.parseXML(xml)
            return media
        except Exception as e:
            Utils.log_err("/create-channel-attachment",e)
    
    # Create a new file/image/media attachment for a chat channel.       
    def createChannelImageAttachment(self, file, config: MediaConfig) -> MediaConfig:
        config.addCredentials(self)
        xml = self.POSTIMAGE(self.url + "/create-channel-attachment", file, config.name, config.toXML())
        if(xml == None):
            return None
        try:
            media = MediaConfig()
            media.parseXML(xml)
            return media
        except Exception as e:
            Utils.log_err("/create-channel-attachment",e)
            
    # Create a reply to a forum post.
	# You must set the parent id for the post replying to.
    def createReply (self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/create-reply", config.toXML())
        if(xml == None):
            return None
        try:
            reply = ForumPostConfig()
            reply.parseXML(xml)
            return reply
        except Exception as e:
            Utils.log_err(e)


    # Process the speech message and return the server generate text-to-speech audio file.
	# This allows for server-side speech generation.
    def tts(self, config: Speech):
        config.addCredentials(self)
        return self.POST(self.url + "/speak", config.toXML())
    
    
    # Return the list of the content types.
    def getTypes(self) -> list:
        return self.types
    
    # Return the channel types.
    def getChannelTypes(self) -> list:
        return self.channelTypes
    
    # Return the access mode types.
    def getAccessModes(self) -> list:
        return self.accessModes
    
    # Return the media access mode types.
    def getMediaAccessModes(self) -> list:
        return self.mediaAccessModes
    
    # Return learning mode types.
    def getLearningModes(self) -> list:
        return self.learningModes
    
    # Return the correction mode types.
    def getCorrectionModes(self) -> list:
        return self.correctionModes
    
    # Return the bot mode types.
    def getBotModes(self) -> list:
        return self.botModes
    

    # Create a user message.
	# This can be used to send a user a direct message.
	# SPAM will cause your account to be deleted.
    def createUserMessage(self, config: UserMessageConfig):
        config.addCredentials(self)
        self.POST(self.url + "/create-user-message", config.toXML())
    
    # Fetch the user details.
	# Function names can't be the same.
    def fetchUser(self, config: UserConfig) -> UserConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/view-user", config.toXML())
        if(xml == None):
            return None
        try:
            user = UserConfig()
            user.parseXML(xml)
            return user
        except Exception as e:
            Utils.log_err("view-user", e)
    
    # Fetch the forum post details for the forum post id.
    def fetchForumPost(self, config: ForumPostConfig) -> ForumPostConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/check-forum-post", config.toXML())
        if(xml == None):
            return None
        try:
            post = ForumPostConfig()
            post.parseXML(xml)
            return post
        except Exception as e:
            Utils.log_err("check-forum-post", e)
            
    # Fetch the URL for the image from the server.
    def fetchImage(self, image: str):
        return "http://" + self.credentials.host + self.credentials.app + "/" + image
    
    
    
    # Return the list of forum posts for the forum browse criteria.
    def getPosts(self, config: BrowseConfig) -> [ForumPostConfig]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-forum-posts", config.toXML())
        instances:[ForumPostConfig] = []
        if(xml == None):
            return instances
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for item in data:
                config: ForumPostConfig = ForumPostConfig()
                config.parseXML(item)
                instances.append(config)
                
            return instances
        except Exception as e:
            Utils.log_err("get-forum-posts", e)
            
    # Return the list of categories for the type, and domain.
    def getCategories(self, config: ContentConfig) -> [ContentConfig]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-categories", config.toXML())
        categories:[ContentConfig] = []
        if(xml == None):
            return categories
        try: 
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for item in data:
                config = ContentConfig()
                config.parseXML(item)
                categories.append(config)
            return categories
        except Exception as e:
            Utils.log_err("get-categories", e)
            
            
    # Return the list of tags for the type, and domain.
    def getTags(self, config: ContentConfig) -> [str]:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-tags", config.toXML())
        tags = []
        if(xml == None):
            return 
        try:
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for tag in data:
                tags.append(tag.attrib.get("name"))
            return tags
        except Exception as e:
           Utils.log_err("get-tags", e)



    # Return the list of bot templates
    def getTemplates(self) -> [str]:
        xml = self.GET(self.url + "/get-all-templates")
        instances = []
        if(xml == None):
            return instances
        try:
            root = Utils.loadXML(xml)
            if(root == None):
                return
            for item in root:
                config = InstanceConfig()
                config.parseXML(item)
                instances.append(config)
            return instances
        except Exception as e:
            Utils.log_err("get-all-templates", e)
    
    # Enable debugging, debug messages will be logged to System.out.
    def setDebug(self, debug: bool):
        self.debug = debug


    # Return the current domain.
	# A domain is an isolated content space.
    def getDomain(self) -> DomainConfig:
        if(self.domain != None):
            return self.domain
        return None


    # Set the current domain.
	# A domain is an isolated content space.
	# connect() should be used to validate and connect a domain.
    def setDomain(self, domain: DomainConfig):
        self.domain = domain


    # Disconnect from the connection.
    # An SDKConnection does not keep a live connection, but this resets its connected user and admin.
    def disconnect(self):
        self.user = None
        self.domain = None


    # Return the current application credentials.
    def getCredentials(self) -> Credentials:
        if(self.credentials == None):
            vars(self.credentials)
            Utils.log("SDKConnection Credentials","Credentials is null")
            return None
        return self.credentials

    #Set the application credentials.
    def setCredentials(self, credentails: Credentials):
        self.credentials = credentails
        self.url = credentails.url
        
    # Return is debugging has been enabled
    def isDebug(self) -> bool:
        return self.debug
    
    
    # Return the administrators of the content.
    def getAdmins(self, config: WebMediumConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-" + config.getType() + "-admins", config.toXML())
        users = []
        if(xml == None):
            return users
        try: 
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for item in data:
                config = UserConfig()
                config.parseXML(item)
                users.append(config)
            return users
        except Exception as e:
            Utils.log_err("get-" + config.getType()+"-admins", e)
    
    
    # Return the users for the content
    def getUsers(self, config: WebMediumConfig):
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-" + config.getType() + "-users", config.toXML())
        users = []
        if(xml == None):
            return users
        try: 
            data = Utils.loadXML(xml)
            if(data == None):
                return
            for item in data:
                config = UserConfig()
                config.parseXML(item)
                users.append(config)
            return users
        except Exception as e:
            Utils.log_err("get-" + config.getType()+"-users", e)
    
    # Return the channel's bot configuration.
    def getChannelBotMode(self, config: ChannelConfig) -> BotModeConfig:
        config.addCredentials(self)
        xml = self.POST(self.url + "/get-channel-bot-mode", config.toXML())
        if(xml == None):
            return None
        
        try:
            config = BotModeConfig()
            config.parseXML(xml)
            return config
        except Exception as e:
            Utils.log_err("get-channel-bot-mode", e)
    
    
    
    # Save the channel's bot configuration.
    def saveChannelBotMode(self, config: BotModeConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-channel-bot-mode", config.toXML())
    
    
    # Save the forum's bot configuration
    def saveForumBotMode(self, config: BotModeConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-forum-bot-mode", config.toXML())
        
    # Save the bot's learning configuration.
    def saveLearning(self, config: LearningConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-learning", config.toXML())
    
    # Save the bot's voice configureation.
    def saveVoice(self, config: VoiceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-voice", config.toXML())
    
    
    # Save the bot's avatar configuration.
    def saveBotAvatar(self, config: InstanceConfig):
        config.addCredentials(self)
        self.POST(self.url + "/save-bot-avatar", config.toXML())
    
    
    # Train the bot with a new question/response pair.
    def train(self, config: TrainingConfig):
        config.addCredentials(self)
        self.POST(self.url + "/train-instance", config.toXML())
    
    
    # Perform the user administrators task (add or remove users, or administrators)
    def userAdmin(self, config: TrainingConfig):
        config.addCredentials(self)
        self.POST(self.url + "/user-admin", config.toXML())
    
    # Return the current connected user.
    def getUser(self) -> UserConfig:
        if(self.user!= None):
            return self.user
        return None
    
    # Set the current connected user.
	# connect() should be used to validate and connect a user.
    def setUser(self, user: UserConfig):
        self.user = user
    
    
    def POST(self, url: str, xml: str):
        if(self.debug):
            Utils.log("POST_URL", url)
            Utils.log("POST_XML",str(xml))
        headers = {
            "Content-Type": "application/xml"
        }
        # Send the POST request with XML data
        response = requests.post(url, data=str(xml), headers=headers)
        
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                Utils.log("POST_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                return None
                
        return response.text
    
    def GET(self, url:str):
        if(self.debug):
            Utils.log("POST_URL", url)
        response = requests.get(url)
        if(200 <= response.status_code <= 299):
            if(self.debug):
                Utils.log("GET_SUCCESSFUL: "+ url, str(response.content)[:500]+"...")
        else:
            if(self.debug):
                Utils.log("GET_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                return None
        return response.text
    
    def POSTFILE(self, url, file, name: str, xml: str):
        if(self.debug):
            Utils.logs(
                "POST IMAGE", "POST_URL: " + url,
                "POST_XML: " + str(xml),
                "Name: " + str(name)
            )
        
        multipart_encoder = MultipartEncoder(fields= {
            'file': file,
            'xml':str(xml)
        })

        headers = {'Content-Type': multipart_encoder.content_type}
        
        request = requests.Request('POST', url, headers=headers, data=multipart_encoder)

        prepared_request = request.prepare()
        #Print the request data
        Utils.log("Prepared Requests: ",
                  prepared_request.method+"\n"+
                  prepared_request.url+"\n"+
                  str(prepared_request.headers))
        # print(prepared_request.body)
        response = requests.Session().send(prepared_request)
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_FILE_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                Utils.log("POST_FILE_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                print(response.text)
                return None
                
        return response.text
    
    
    #TODO: resize image before uploading... 600x600
    def POSTHDIMAGE(self, url, image, name: str, xml: str):
        if(self.debug):
            Utils.logs(
                "POST IMAGE", "POST_URL: " + url,
                "POST_XML: " + str(xml),
                "Name: " + str(name)
            )
        
        # TODO: Resize image - TESTING
        # Utils.resizeImage(600, 600, image)
        
        multipart_encoder = MultipartEncoder(fields= {
            'file': image,
            'xml':str(xml)
        })

        headers = {'Content-Type': multipart_encoder.content_type}
        
        request = requests.Request('POST', url, headers=headers, data=multipart_encoder)

        prepared_request = request.prepare()
        #Print the request data
        Utils.log("Prepared Requests: ",
                  prepared_request.method+"\n"+
                  prepared_request.url+"\n"+
                  str(prepared_request.headers))
        # print(prepared_request.body)
        response = requests.Session().send(prepared_request)
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_IMAGE_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                Utils.log("POST_IMAGE_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                print(response.text)
                return None
                
        return response.text
    
    #TODO: resize image before uploading... 300x300
    def POSTIMAGE(self, url, image, name: str, xml: str):
        if(self.debug):
            Utils.logs(
                "POST IMAGE", "POST_URL: " + url,
                "POST_XML: " + str(xml),
                "Name: " + str(name)
            )
        
        # TODO: Resize image - TESTING
        # Utils.resizeImage(image=image)
        
        multipart_encoder = MultipartEncoder(fields= {
            'file': image,
            'xml':str(xml)
        })

        headers = {'Content-Type': multipart_encoder.content_type}
        
        request = requests.Request('POST', url, headers=headers, data=multipart_encoder)

        prepared_request = request.prepare()
        #Print the request data
        Utils.log("Prepared Requests: ",
                  prepared_request.method+"\n"+
                  prepared_request.url+"\n"+
                  str(prepared_request.headers))
        # print(prepared_request.body)
        response = requests.Session().send(prepared_request)
        if 200 <= response.status_code <= 299:
            if(self.debug):
                Utils.log("POST_IMAGE_SUCCESSFUL: " + url, str(response.content)[:500]+"...") #Limiting output to 500ch
        else:
            if(self.debug):
                if(response.text==None or response.text=="null"):
                    print("Returned: Null.")
                    return
                Utils.log("POST_IMAGE_FAILED: " + str(response.status_code), str(response.content)[:500]+"...")
                return None
                
        return response.text

    def __str__(self):
        writer = Writer()
        writer.append("URL: " + self.url + "\n")
        writer.append("HOST: " + self.credentials.getHost() + "\n")
        writer.append("Application ID: " + self.credentials.applicationId + "\n")
        writer.append("Debug: " + str(self.debug))
        writer.append("\n******* User Details *******\n") if self.getUser() != None else None
        writer.append("Name: " + str(self.getUser().name)+ "\n") if self.getUser() != None else None
        writer.append("Email: " + str(self.getUser().email)+ "\n") if self.getUser() != None and self.getUser().email != "" else None
        writer.append("Connects: " + str(self.getUser().connects)+ "\n") if self.getUser() != None else None
        writer.append("Bots: " + str(self.getUser().bots)+ "\n") if self.getUser() != None else None
        writer.append("Joined: " + str(self.getUser().displayJoined())) if self.getUser() != None else None
        return writer.__str__()
        
