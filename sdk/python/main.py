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
from typing import List
from sdk.SDKConnection import SDKConnection
from sdk.BotlibreCredentials import BotlibreCredentials
from util.Utils import Utils, Writer
from config.DomainConfig import DomainConfig
from config.UserConfig import UserConfig
from config.ChatResponse import ChatResponse
from config.ChatConfig import ChatConfig
from config.ForumPostConfig import ForumPostConfig
from config.AvatarMessage import AvatarMessage
from config.UserMessageConfig import UserMessageConfig
from config.ResponseConfig import ResponseConfig
from config.AvatarMedia import AvatarMedia
from config.AvatarConfig import AvatarConfig
from config.MediaConfig import MediaConfig
from config.WebMediumConfig import WebMediumConfig
from config.InstanceConfig import InstanceConfig
from config.GraphicConfig import GraphicConfig
from config.ForumConfig import ForumConfig
from config.BrowseConfig import BrowseConfig
from config.Speech import Speech
from config.ContentConfig import ContentConfig
from config.ChannelConfig import ChannelConfig
from config.BotModeConfig import BotModeConfig
from config.LearningConfig import LearningConfig
from config.VoiceConfig import VoiceConfig
from config.TrainingConfig import TrainingConfig
from config.UserAdminConfig import UserAdminConfig
from config.ResponseSearchConfig import ResponseSearchConfig
from config.ScriptConfig import ScriptConfig
from config.ScriptSourceConfig import ScriptSourceConfig





class ID:
    # Enter your application ID here.
    # You can get an application ID from any of the services websites (Bot Libre, Bot Libre for Business)
    APPLICATION = ""
    # Please enter your account details. Both your username and password are required.
    # If you don't have an account yet, you can create one to use.
    USER = ""
    PASSWORD = ""
    # Please enter the id of each instance to be able to test them.
    BOT = ""
    FORUM = ""
    CHANNEL = ""
    AVATAR = ""
    AVATAR_MEDIA = ""
    FORUM_POST = ""
    GRAPHIC = ""
    SCRIPT = ""
    ANALYTIC = ""
    QUESTION = ""
    RESPONSE = ""
    TEMP_IMAGEURL = "https://www.botlibre.com/avatars/a48927947.png" # Using this image to test POSTIMAGE 
    FLAG_USER = ""

    @classmethod
    def check(cls, var: str):
        if(var == ""):
            return input("Missing ID, Enter an ID: ")
        else:
            return var


class Main:
    connection: SDKConnection
    domain: str
    defaultType: str
    showAds: bool
    debug: bool
    applicationId: str

    def __init__(self,
                 debug=True,
                 adult=False,
                 showAds=True,
                 applicationID=None,
                 username=None,
                 password=None):
        self.debug = debug
        self.adult = adult
        self.applicationId = applicationID
        self.domainId = None
        self.username = username
        self.password = password
        self.domain = None
        self.defaultType = "Bots"
        self.website = "http://www.botlibre.com"
        self.websiteHttps = "https://botlibre.com"
        self.server = "botlibre.com"
        self.showAds = showAds  # default true
        # Configure your connection credentials here.
        # Choose which service provider you wish to connect to.
        self.connection = SDKConnection(
            BotlibreCredentials(self.applicationId), debug=self.debug)

        if(self.domainId != None):
            self.domain = DomainConfig()
            self.domain.id = self.domainId
            self.connection.setDomain(self.domain)
        if(self.debug):
            Utils.log("[Main] init SDKConnection", self.connection)
            self.showAds = False
            self.connection.setDebug(True)

    # Login in with user credentials
    def connectUserAccount(self) -> bool:
        if(self.connection.user != None):
            Utils.log("User logged in", "Token has been established already.")
            return True
        userConfig = UserConfig()
        userConfig.application = self.applicationId
        userConfig.user = self.username
        userConfig.password = self.password
        if(userConfig.user == "" or userConfig.password == "" or userConfig.application == ""):
            Utils.log(
                "[Main]", "Please fill the required data @ connectUserAccount in main.py")
            return False
        self.connection.connect(userConfig)
        return True

    # Send a chat message to a bot
    def sendChatMessage(self, botId: str = ID.BOT) -> ChatResponse:
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ChatConfig()
        config.instance = botId
        config.message = input(
            "BotID: " + config.instance + " - " + "Enter Message: ")
        return self.connection.chat(config)

    # Fetch user details
    def fetchUser(self) -> UserConfig:
        userConfig = UserConfig()
        userConfig.application = self.applicationId
        userConfig.user = input("User to fetch (i.e user) : ")
        return self.connection.fetchUser(userConfig)

    # Fetch forum post
    def fetchFormPost(self, FORUM_POST_ID: str = ID.FORUM_POST) -> ForumPostConfig:
        check_user = self.connectUserAccount()
        if(check_user == False):
            return None
        config = ForumPostConfig()
        config.id = FORUM_POST_ID
        return self.connection.fetchForumPost(config)

    # Create a new user account
    def createUser(self) -> UserConfig:
        config = UserConfig()
        config.user = input("User: ")
        config.password = input("Password: ")
        config.hint = input("Hint: ")
        config.name = input("Name: ")
        config.email = input("Email: ")
        config.website = input("Website: ")
        config.bio = input("Bio: ")
        showName = input("Show Name (y or n): ")
        if(showName == "y" or showName == "Y"):
            config.showName = True
        else:
            config.showName = False
        return self.connection.createUser(config)

    def updateUser(self) -> UserConfig:
        # user must be logged in
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        userConfig = self.connection.getUser()
        userConfig.password = input("Old Password: ")
        userConfig.newPassword = input("New Password: ")
        userConfig.hint = input("Hint: ")
        userConfig.name = input("Name: ")
        userConfig.email = input("Email: ")
        userConfig.website = input("Website: ")
        userConfig.bio = input("Bio: ")
        showName = input("Show Name (y or n): ")
        if(showName == "y" or showName == "Y"):
            userConfig.showName = True
        else:
            userConfig.showName = False
        return self.connection.updateUser(userConfig)

    def avatarMessage(self, avatarId: str = ID.AVATAR):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        avatarMessage = AvatarMessage()
        avatarMessage.application = self.applicationId
        avatarMessage.instance = avatarId
        avatarMessage.avatar = avatarId
        avatarMessage.speak = False
        avatarMessage.message = input("Type message: ")
        avatarMessage.emote = ""
        avatarMessage.action = ""
        avatarMessage.pose = ""
        avatarMessage.voice = "cmu-slt"
        avatarMessage.format = "mp4"  # or webm
        avatarMessage.hd = False
        return self.connection.avatarMessage(avatarMessage)

    def createForumPost(self, forumId: str = ID.FORUM):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumPostConfig()
        config.forum = forumId
        config.topic = input("Topic: ")
        config.details = input("Detials: ")
        config.tags = input("Tags: ")
        return self.connection.createForumPost(config)

    def updateForumPost(self, FORUM_ID: str = ID.FORUM, forumPostId: str = ID.FORUM_POST):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumPostConfig()
        config.forum = FORUM_ID
        config.id = forumPostId  # post id
        config.topic = input("Topic: ")
        config.details = input("Details: ")
        config.tags = input("Tags: ")
        return self.connection.updateForumPost(config)

    def deleteForumPost(self, forumId: str = ID.FORUM, FORUM_POST_ID: str = ID.FORUM_POST):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumPostConfig()
        config.forum = forumId
        config.id = FORUM_POST_ID
        return self.connection.deleteForumPost(config)

    def createChannelFileAttachment(self, instanceId: str = ID.CHANNEL, fileName: str = None, fileType: str= None, file = None):
        # TODO: Test
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = MediaConfig()
        config.instance = instanceId
        config.name = fileName
        config.type = fileType
        return self.connection.createChannelFileAttachment(file, config)
        

    def createChannelImageAttachment(self, instanceId: str = ID.CHANNEL, fileName: str = None, fileType: str= None, file = None):
        # TODO: Test
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = MediaConfig()
        config.instance = instanceId
        config.name = fileName
        config.type = fileType
        return self.connection.createChannelImageAttachment(file, config)
        

    def createReply(self, forumId: str = ID.FORUM, forumPostId: str = ID.FORUM_POST):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumPostConfig()
        config.forum = forumId
        config.parent = forumPostId
        config.details = input("Detials: ")
        config.tags = input("Tags: ")
        return self.connection.createReply(config)

    def createUserMessage(self):
        config = UserMessageConfig()
        config.user = input("Target: ")
        config.model = input("Subject (i.e Avatar Request - Bot Libre): ")
        writer = Writer("user: " + input("User: "))
        writer.append("model: " + input("Model: "))
        writer.append("facae: " + input("Face: "))
        writer.append("eyeColor: " + input("Eye Color: "))
        writer.append("Hair Color: " + input("Hair Color: "))
        writer.append("body: " + input("Body: "))
        writer.append("cloths: " + input("Cloths: "))
        writer.append("pose: " + input("Pose: "))
        writer.append("email: " + input("Email: "))
        writer.append("comment: " + input("Comment: "))
        config.message = writer
        return self.connection.createUserMessage(config)

    def saveResponse(self, botId: str = ID.BOT, responseId: str = ID.QUESTION, questionId: str=""):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ResponseConfig()
        config.instance = botId
        config.responseId = None if(responseId == "") else responseId
        config.questionId = None if(questionId == "") else questionId
        config.type = input(
            "Type (response, conversation, greeting, flagged):")
        config.correctness = input("Correctness: ")
        # config.flagged = input("Flagged: ")
        config.question = input("Question: ")
        config.response = input("Response: ")
        config.topic = input("Topic: ")
        config.label = input("Label: ")
        config.keywords = input("Keywords: ")
        config.required = False if(
            input("Required (y, n): ").lower == "y") else False
        config.emotions = input("Emotions (i.e like): ")
        config.actions = input("Actions: ")
        config.poses = input("Poses: ")
        config.previous = input("Previous: ")
        config.onRepeat = input("onRepeat: ")
        config.command = input("Command: ")
        config.noRepeat = False if(
            input("No Repeat (y, n): ").lower == "y") else False
        config.requirePrevious = False if(
            input("Require Previous (y, n): ").lower == "y") else False
        config.requireTopic = False if(
            input("Require Topic (y, n): ").lower == "y") else False
        return self.connection.saveResponse(config)

    def deleteResponse(self, responseId: str = ID.RESPONSE):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ResponseConfig()
        config.instance = responseId
        # config.type = input("Type (response, conversation, greeting, flagged):")
        # config.questionId = questionId
        # config.responseId = responseId
        return self.connection.deleteResponse(config)
    
    
    def saveAvatarBackground(self, avatarId: str = ID.AVATAR, imageData = None,fileName: str=None, fileType: str = "image/jpeg"):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarMedia()
        config.name = fileName
        config.type = fileType
        config.instance = avatarId
        return self.connection.saveAvatarBackground(imageData, config)
    
    def deleteAvatarBackground(self, avatarId: str = ID.AVATAR):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarConfig()
        config.id = avatarId
        return self.connection.deleteAvatarBackground(config)
    
    def createAvatarMedia(self, avatarId: str = ID.AVATAR, imageData=None, fileName:str=None, fileType:str = "image/jpeg"):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarMedia()
        config.instance = avatarId
        config.name = fileName
        config.type = fileType
        return self.connection.createAvatarMedia(imageData, config)
    
    def saveAvatarMedia(self, avatarId: str = ID.AVATAR, mediaId: str = ID.AVATAR_MEDIA):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarMedia()
        config.instance = avatarId
        config.mediaId = mediaId
        config.emotions = input("Can be added, with multiple entries separated by commas : \nEmotions (i.e., none, like, happy): ")
        config.actions = input("Actoins (smile, laugh, ...): ")
        config.poses = input("Poses (talking, dancing, sleeping, ...) : ")
        config.talking = True if ("t" in input("Talking (true, false): ").lower()) else False
        print(f'Talking set to {config.talking}')
        config.hd = True if ("t" in input("HD (true, false): ").lower()) else False
        print(f'HD set to {config.hd}')
        return self.connection.saveAvatarMedia(config)
    
    def deleteAvatarMedia(self, avatarId: str = ID.AVATAR, mediaId: str = ID.AVATAR_MEDIA):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarMedia()
        config.mediaId = mediaId
        config.instance = avatarId
        return self.connection.deleteAvatarMedia(config)
    
    def flagInstance(self, config: WebMediumConfig , instanceId: str = ID.BOT, flaggedReason: str =""):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config.flaggedReason = flaggedReason
        config.id = instanceId
        return self.connection.flag(config)
    
    def flagUser(self, userId:str = ID.USER, flaggedReason: str = ""):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = UserConfig()
        config.user = userId
        config.flaggedReason = flaggedReason
        return self.connection.flagUser(config)
    
    def subscribeForumPost(self, forumPostId: str = ID.FORUM_POST):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumPostConfig()
        config.id = forumPostId
        return self.connection.subscribeForumPost(config)

    def subscribeForum(self, forumId: str = ID.FORUM):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumConfig()
        config.id = forumId
        return self.connection.subscribeForum(config)

    def unsubscribeForumPost(self, forumPostId: str = ID.FORUM_POST):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumPostConfig()
        config.id = forumPostId
        return self.connection.unsubscribeForum(config)
    
    def unsubscribeForum(self, forumId: str = ID.FORUM):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumConfig()
        config.id = forumId
        return self.connection.unsubscribeForumPost(config)

    def getForumPosts(self, type: str = "Post", typeFilter: str = "Public", sort: str = "date"):
        config = BrowseConfig()
        config.type = type
        config.typeFilter = typeFilter
        config.sort = sort
        return self.connection.getPosts(config)
    
    def thumbsUp(self, config: WebMediumConfig, id: str):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config.id = id
        return self.connection.thumbsUp(config)
    
    def thumbsDown(self, config: WebMediumConfig, id: str):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config.id = id
        return self.connection.thumbsDown(config)
    
    def starForum(self, forumId: str = ID.FORUM, star: int = 5):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumConfig()
        config.id = forumId
        config.stars = star
        main.connection.star(config)
    
    def TTS(self, text: str = "Hello Word", mod: str = "default"):
        config = Speech()
        config.voice = "English : US : Female : SLT"
        config.text = text
        config.mod = mod
        return main.connection.tts(config)
    
    def getAdminsForum(self, forumId: str=ID.FORUM):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumConfig()
        config.id = forumId
        return self.connection.getAdmins(config)
    
    def getUsersForum(self, forumId: str= ID.FORUM):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ForumConfig()
        config.id = forumId
        return self.connection.getUsers(config)
    
    def getCategories(self, instance="Bot") -> [ContentConfig]:
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ContentConfig()
        config.type = instance
        return self.connection.getCategories(config)
    
    def getTags(self, instance="Bot") -> [ContentConfig]:      
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ContentConfig()
        config.type = instance
        return self.connection.getTags(config)

    def getAllTemplates(self):
        return self.connection.getTemplates()
    
    def getChannelBotMode(self, channelId: str = ID.CHANNEL):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ChannelConfig()
        config.id = channelId
        return self.connection.getChannelBotMode(config)
    
    def saveChannelBotMode(self, channelId: str = ID.CHANNEL, botId:str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        option = int(input("Select [1 - 3]\n(1)Listen Only - (2)Answer Only - (3)Answer And Listen : "))
        mode = ["ListenOnly", "AnswerOnly", "AnswerAndListen"]
        config = BotModeConfig()
        config.instance = channelId
        config.bot = botId
        config.mode = mode[option-1]
        return self.connection.saveChannelBotMode(config)
    
    def saveForumBotMode(self, forumId: str = ID.FORUM, botId: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        option = int(input("Select [1 - 3]\n(1)Listen Only - (2)Answer Only - (3)Answer And Listen : "))
        mode = ["ListenOnly", "AnswerOnly", "AnswerAndListen"]
        config = BotModeConfig()
        config.instance = forumId
        config.bot = botId
        config.mode = mode[option-1]
        return self.connection.saveForumBotMode(config)

    def saveLearning(
        self, botId: str = ID.BOT,
        nlp: str = "4",
        language: str = "en",
        correctionMode: str = "Everyone",
        learningMode:str = "Everyone",
        learningRate: str = "55",
        ):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = LearningConfig()
        config.instance = botId
        config.nlp = nlp
        config.language = language
        config.correctionMode = correctionMode
        config.learningMode = learningMode
        config.learningRate = learningRate
        config.scriptTimeout = 10000
        config.responseMatchTimeout = 1000
        config.conversationMatchPercentage = "50"
        config.discussionMatchPercentage = "90"
        config.enableEmoting = True
        config.enableEmotions = True
        config.enableComprehension = False #only supported for dedicated servers
        config.enableConsciousness = False #only supported for dedicated servers
        config.enableResponseMatch = True
        config.checkExactMatchFirst = True
        config.fixFormulaCase = True
        config.learnGrammar = True
        config.synthesizeResponse = True
        config.disableFlag = False
        config.reduceQuestions = True
        config.trackCase = False
        return self.connection.saveLearning(config)
    
    def saveVoice(self, botId: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = VoiceConfig()
        config.instance = botId
        config.voice = "English : US : Female : SLT"
        config.mod = ""
        config.language = "en"
        config.pitch = ""
        config.speechRate = ""
        config.nativeVoice = ""
        return self.connection.saveVoice(config)
        
    def saveBotAvatar(self, botId: str = ID.BOT, avatarId: str = ID.AVATAR):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = InstanceConfig()
        config.id = botId
        config.instanceAvatar = avatarId
        return self.connection.saveBotAvatar(config)
    
    def train(self, botId: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = TrainingConfig()
        config.instance = botId
        Utils.logs("Train")
        config.operation = input("Operation: ")
        config.question = input("Question: ")
        config.response = input("Response: ")
        return self.connection.train(config)
    
    def userAdmin(self, botId:str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = UserAdminConfig()
        config.instance = botId
        Utils.logs("User Admin (Add or remove users)")        
        config.type = input("Type (i.e Bot) : ")
        option = int(input("Operation: (1)Add User - (2)Add Admin - (3)Remove User - (4)Remove Admin: "))
        operations = ["AddUser", "AddAdmin", "RemoveUser", "RemoveAdmin"]
        config.operation = operations[option-1]
        print("Selected: " + config.operation)
        config.operationUser = input("Operation User: ")
        return self.connection.userAdmin(config)
    
    def createAvatar(self):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarConfig()
        config.name = input("Name: ")
        config.description = input("Description: ")
        config.detials = input("Detials: ")
        config.disclamier = input("Disclaimer: ")
        config.categories = input("Categories (i.e Misc) : ")
        config.license = input("License: ")
        config.accessMode = input("AccessMode: ")
        config.isPrivate = True if(input("isPrivate (y or n) : ").lower()=="y") else False
        config.isHidden = True if(input("isHidden (y or n) : ").lower()=="y") else False
        return self.connection.create(config)
    
    def createGraphicMedia(self, graphicId: str = ID.GRAPHIC, imageData=None, fileName:str=None, fileType:str = "image/jpeg"):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = GraphicConfig()
        config.id = graphicId
        config.fileName = fileName
        config.fileType = fileType
        return self.connection.createGraphicMedia(imageData, config)
    
    def updateUserIcon(self, imageData=None, fileName:str=None, fileType:str = "image/jpeg"):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = self.connection.getUser()
        return main.connection.updateIconUser(imageData, fileName, config)
        
    def getForumBotMode(self, forumId: str = ID.FORUM):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        
        config = ForumConfig()
        config.id = forumId
        return self.connection.getForumBotMode(config)
    
    def getVoice(self, instanceId: str = ID.BOT, id: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = InstanceConfig()
        config.id = id
        config.instance = instanceId
        return self.connection.getVoice(config)
    
    def getDefaultResponses(self, instanceId: str = ID.BOT, id: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = InstanceConfig()
        config.id = id
        config.instace = instanceId
        return self.connection.getDefaultResponses(config)
    
    def getGreetings(self, instanceId: str = ID.BOT) -> List[str]:
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = InstanceConfig()
        config.id = instanceId
        return self.connection.getGreetings(config)
    
    def getResponses(self, instance: str = ID.BOT, 
                    responseType: str = "responses", 
                    duration: str = "all", 
                    inputType: str = "all",
                    restrict: str = "exact", 
                    filter: str = ""    
                ) -> List[ResponseConfig]:
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ResponseSearchConfig()
        config.instance = instance
        config.responseType = responseType
        config.duration = duration
        config.inputType = inputType
        config.restrict = restrict
        config.filter = filter
        return self.connection.getResponses(config)
    
    def getConversations(self, instanceId: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ResponseSearchConfig()
        config.instance = instanceId
        config.responseType = "conversations"
        config.duration = "week"
        config.sort = "date"
        config.inputType = "all"
        return self.connection.getConversation(config)
        
    # Learning Mode must be enabled from the Bot Settings.
    def getLearning(self, botId: str = ID.BOT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = InstanceConfig()
        config.id = botId
        return self.connection.getLearning(config)
        
    def browse(self, type: str = "Bot", typeFilter: str = "Featured", contentRating: str = "Everyone"):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = BrowseConfig()
        config.type = type
        config.typeFilter = typeFilter
        config.contentRating = contentRating
        return self.connection.browse(config)
    
    def getAvatarMedia(self, avatarId:str = ID.AVATAR):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = AvatarConfig()
        config.id = avatarId
        return self.connection.getAvatarMedia(config)
    
    def getScriptSource(self, scriptId:str = ID.SCRIPT):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ScriptConfig()
        config.id = scriptId
        return self.connection.getScriptSource(config)
    
    def saveScriptSource(self,
                scriptId: str = ID.SCRIPT,
                source:str = "Hello World"
            ):
        isUser = self.connectUserAccount()
        if(isUser == False):
            return None
        config = ScriptSourceConfig()
        config.instance = scriptId
        config.source = source
        return self.connection.saveScriptSource(config)
        
    

###### MAIN ######
main = Main (
    applicationID=ID.APPLICATION,
    username=ID.USER,
    password=ID.PASSWORD
)

help = (
    """
    ********************
    *   BotLibre SDK   *
    ********************
    0  - Current User Account
    1  - Post Chat
    2  - Fetch User (user)
    3  - Fetch Forum Post
    4  - Create New User
    5  - Update Current User
    6  - Avatar Message : Not tested
    7  - Create (Forum Post)
    8  - Update (Forum Post)
    9  - Delete (Forum Post)
    10 - Create Reply (Post)
    11 - Save Response Question : Not tested
    12 - Save Response Response : Not tested
    13 - Delete Response : Not tested
    14 - Create (Avatar) Media
    15 - Save (Avatar) Media
    16 - Delete (Avatar) Media
    17 - Save (Avatar) Background
    18 - Delete (Avatar) Background
    19 - Create Channel File Attachement : Not tested
    20 - Create Channel Image Attachement : Not tested
    21 - Flag Instance (Bot) : Not tested
    22 - Flag Instance (Avatar) : Not tested
    23 - Flag Instance (Graphic) : Not tested
    24 - Flag User : Not tested
    25 - Subscribe to a (Forum)
    26 - Subscribe to a (Forum Post)
    27 - Unsubscribe to a (Forum)
    28 - Unsubscribe to a (Forum Post)
    29 - Get (Forum Posts)
    30 - Thumbs up (Need one (Forum, Avatar, Bot ..etc))
    31 - Thumbs Down (Need one (Forum, Avatar, Bot ..etc))
    32 - Set Star for a (Forum) (Set star/review out of 5)
    33 - TTS
    34 - Get Admins (Forum)
    35 - Get Users (Forum)
    36 - Get Categories (Bot)
    37 - Get Tags (Bot)
    38 - Get All Templates
    39 - Get Channel Bot Mode
    40 - Save Channel Bot Mode
    41 - Save Fourm Bot Mode
    42 - Save Learning Mode
    43 - Save Voice
    44 - Save Bot Avatar
    45 - Train Instance
    46 - User Admin (Perform admin task (add or remove users))
    47 - Create a new (Avatar)
    48 - Create (Graphic) Media
    49 - Update User Icon
    50 - Get Forum Bot Mode : Not tested
    51 - Get Voice : Not tested
    52 - Get Default Responses of (Bot)
    53 - Get Greetings of (Bot)
    54 - Get Responses of (Bot)
    55 - Get Conversations of (Bot)
    56 - Get Learning (Bot) Learning Mode Must Be Enabled
    57 - Browse
    58 - Get (Avatar) Media
    59 - Get (Script) Source
    60 - Save (Script) Source
    """
)
print(help)


# Select options
def switch(option):
    try:
        if(int(option) == 0):
            # Connect User Account
            main.connectUserAccount()
            # Check user data
            user = main.connection.getUser()
            Utils.log("User Details", "User: " + user.user + "\n" +
                      "Email: " + user.email + "\n" +
                      "Name: " + user.name)
        elif(int(option) == 1):
            # Chat
            response = main.sendChatMessage()
            Utils.log("Message Detials", "Response: " + response.message)
        elif(int(option) == 2):
            # Fetch user details by taking username as input
            user = main.fetchUser()
            Utils.log("User Details", "User: " +
                      user.user if(user.user != None) else "User does not exist.")
        elif(int(option) == 3):
            forumPost = main.fetchFormPost()
            Utils.log("Fetch Forum Post",
                      "Topic: " + forumPost.topic + "\n" +
                      "Fourm: " + forumPost.forum + "\n" +
                      "Summary: " + forumPost.summary + "\n"
            )
            for reply in forumPost.replies:
                Utils.log(reply.topic + " - by: " +
                          reply.creator, reply.summary)
        elif(int(option) == 4):
            main.createUser()
        elif(int(option) == 5):
            main.updateUser()
        elif(int(option) == 6):
            main.avatarMessage()
        elif(int(option) == 7):
            post = main.createForumPost()
            Utils.log("POST ID: "+str(post.id))
        elif(int(option) == 8):
            post = main.updateForumPost()
            Utils.log("POST ID: "+str(post.id))
        elif(int(option) == 9):
            main.deleteForumPost()
        elif(int(option) == 10):
            replyPost = main.createReply()
            Utils.log("Rplied POST ID: " + str(replyPost.id))
        elif(int(option) == 11):
            main.saveResponse()
        elif(int(option) == 12):
            main.saveResponse()
        elif(int(option) == 13):
            main.deleteResponse()
        elif(int(option)==14):
            file, name, type = Utils.PostImageFromURL(ID.TEMP_IMAGEURL)
            main.createAvatarMedia(
                imageData=file,
                fileName=name,
                fileType=type
            )
        elif(int(option)==15):
            main.saveAvatarMedia()
        elif(int(option)==16):
            main.deleteAvatarMedia()
        elif(int(option)==17):
            file, name, type = Utils.PostImageFromURL(ID.TEMP_IMAGEURL)
            main.saveAvatarBackground(
                imageData=file,
                fileName=name,
                fileType=type
            )
        elif(int(option)==18):
            main.deleteAvatarBackground()
        elif(int(option)==19):
            pass
            #file, name, type = Utils.PostImageFromURL(ID.TEMP_IMAGEURL)
            #main.createChannelFileAttachment(fileName=name, fileType=type, file=file)
        elif(int(option)==20):
            file, name, type = Utils.PostImageFromURL(ID.TEMP_IMAGEURL)
            main.createChannelFileAttachment(
                fileName=name,
                fileType=type,
                file=file
            )
        elif(int(option)==21):
            main.flagInstance(
                config=InstanceConfig(),
                flaggedReason="Reason..."
            ) #by default its using ID.BOT
        elif(int(option)==22):
            main.flagInstance(
                config=AvatarConfig(), 
                instanceId=ID.AVATAR,
                flaggedReason="Reason..."
            )
        elif(int(option)==23):
            main.flagInstance(
                config=GraphicConfig(),
                instanceId=ID.GRAPHIC,
                flaggedReason="Reason..."
            )
        elif(int(option)==24):
            main.flagUser(ID.FLAG_USER,"Reason...")
        elif(int(option)==25):
            main.subscribeForum()
        elif(int(option)==26):
            main.subscribeForumPost()
        elif(int(option)==27):
            main.unsubscribeForum()
        elif(int(option)==28):
            main.unsubscribeForumPost()
        elif(int(option)==29):
            posts:[ForumPostConfig] = main.getForumPosts()
            i: int = 0
            for post in posts:
                if(main.debug): #Limit output to 10 posts
                    if(i>10):
                        break
                if(post.id !=None):
                    Utils.logs("Post creator: " + post.creator,
                        "Topic: " + post.topic if post.topic != None else None,
                        "Summary: " + post.summary if post.summary != None else None
                    )
                i+=1
        elif(int(option)==30):
            # we can use ForumConfig, FourmPostConfig, GraphicConfig..etc
            main.thumbsUp(ForumConfig(), ID.FORUM)
        elif(int(option)==31):
            # we can use ForumConfig, FourmPostConfig, GraphicConfig..etc
            main.thumbsDown(ForumConfig(), ID.FORUM) # i.e: GraphicConfig(), ID.GRAPHIC
        elif(int(option)==32):
            main.starForum()
        elif(int(option)==33):
            result = main.TTS()
            Utils.log("TTS", result)
        elif(int(option)==34):
            admins = main.getAdminsForum()
            for admin in admins:
                print("Admin: " + admin.user)
        elif(int(option)==35):
            users = main.getUsersForum()
            for user in users:
                print("User: " + user.user)
        elif(int(option)==36):
            categories = main.getCategories()
            for category in categories:
                print(category.name)
                print(category.type)
                print(category.description)
                print("-=-=-=-=-=-=-=-=-")
        elif(int(option)==37):
            tags = main.getTags()
            for tag in tags:
                print(tag)
        elif(int(option)==38):
            templates = main.getAllTemplates()
            for template in templates:
                print(template.name)
        elif(int(option)==39):
            botMode = main.getChannelBotMode()
            print(botMode.bot)
            print(botMode.mode)
        elif(int(option)==40):
            main.saveChannelBotMode()
        elif(int(option)==41):
            main.saveForumBotMode()
        elif(int(option)==42):
            main.saveLearning()
        elif(int(option)==43):
            main.saveVoice()
        elif(int(option)==44):
            main.saveBotAvatar()
        elif(int(option)==45):
            main.train()
        elif(int(option)==46):
            main.userAdmin()
        elif(int(option)==47):
            content = main.createAvatar()
            Utils.logs("New Avatar Created", 
                       "Avatar ID: " + content.id,
                       "Avatar Creator: " + content.creator,
                    )
        elif(int(option)==48):
            file, name, type = Utils.PostImageFromURL(ID.TEMP_IMAGEURL)
            main.createGraphicMedia(
                imageData=file,
                fileName=name,
                fileType=type
            )
        elif(int(option)==49):
            file, name, type = Utils.PostImageFromURL(ID.TEMP_IMAGEURL)
            main.updateUserIcon(file, name)
        elif(int(option)==50):
            main.getForumBotMode()
        elif(int(option)==51):
            main.getVoice()
        elif(int(option)==52):
            responses = main.getDefaultResponses(id=str(50010323), instanceId=str(50010323))
            print(responses)
        elif(int(option)==53):
            greetings = main.getGreetings()
            print(greetings)
        elif(int(option)==54):
            allResponses = main.getResponses()
            for item in allResponses:
                print("Question: "+item.question.text + " | ID: " + str(item.questionId))
                print("Response: "+item.response.text + " | ID: " + str(item.responseId))
                print("-"*12)
        elif(int(option)==55):
            allConversations = main.getConversations()
            for item in allConversations:
                print(item.id)
                print(item.creationDate)
                for valInput in item.inputs:
                    Utils.logs("Inputs: ",
                        "Speaker: " + valInput.speaker,
                        "Target: " + valInput.target,
                        "Value: "+ str(valInput.value)
                    )
                print(12*"-")
        elif(int(option)==56):
            learnignMode = main.getLearning()
            Utils.logs("Learing Mode", 
                "Learning Mode: " + str(learnignMode.learningMode),
                "Script Timeout: " + str(learnignMode.scriptTimeout)
            )
        elif(int(option)==57):
            selection = input("Type (1) Bot \n(2) Forum\n(3) Channel\n(4) Domain\n(5) Avatar\n(6) Script\n(7) Graphic\n->")
            types = ["Bot", "Forum", "Channel", "Domain", "Avatar", "Script", "Graphic"]
            result = main.browse(type=types[int(selection)-1])
            for item in result:
                print("Name: " + item.name)
        elif(int(option)==58):
            medias = main.getAvatarMedia()
            for media in medias:
                print(media.name)
        elif(int(option)==59):
            scriptSource = main.getScriptSource()
            print("Creator: "+str(scriptSource.creator))
            print("Next Version: "+str(scriptSource.getNextVersion()))
            print("Version Name: "+str(scriptSource.versionName))
            print("Creation Date: "+ str(scriptSource.creationDate))
            print("Source: " + str(scriptSource.source))
        elif(int(option)==60):
            main.saveScriptSource()
            
        else:
            pass
    except Exception as e:
        Utils.log_err("main-input", e)


# Taking input from user
while True:
    user_input = input(
        "Enter an option (0 : 99) ('q' to quit or 'h' for help): ")
    if user_input.lower() == 'q':
        break
    elif user_input.lower() == 'h':
        Utils.log("Connection Details: ", main.connection)
        Utils.log("Help", help)
        continue
    switch(user_input)
