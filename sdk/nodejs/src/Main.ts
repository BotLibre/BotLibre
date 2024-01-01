/*
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
*/

import SDKConnection from "./sdk/SDKConnection"
import BotlibreCredentials from "./sdk/BotlibreCredentials"
import DomainConfig from "./config/DomainConfig"
import UserConfig from "./config/UserConfig"
import { Utils } from './util/Utils'
import SDKException, { SDKError } from "./sdk/SDKException"
import ChatResponse from "./config/ChatResponse"
import ChatConfig from "./config/ChatConfig"
import { ForumPostConfig } from "./config/ForumPostConfig"
import AvatarMessage from "./config/AvatarMessage"
import ForumConfig from "./config/ForumConfig"
import BrowseConfig from "./config/BrowseConfig"
import ContentConfig from "./config/ContentConfig"
import BotModeConfig from "./config/BotModeConfig"
import ChannelConfig from "./config/ChannelConfig"
import AvatarMedia from "./config/AvatarMedia"
import MediaConfig from "./config/MediaConfig"
import UserMessageConfig from "./config/UserMessageConfig"
import ResponseConfig from "./config/ResponseConfig"
import AvatarConfig from "./config/AvatarConfig"
import WebMediumConfig from "./config/WebMediumConfig"
import Speech from "./config/Speech"
import LearningConfig from "./config/LearningConfig"
import VoiceConfig from "./config/VoiceConfig"
import InstanceConfig from "./config/InstanceConfig"
import TrainingConfig from "./config/TrainingConfig"
import UserAdminConfig from "./config/UserAdminConfig"
import GraphicConfig from "./config/GraphicConfig"
import ResponseSearchConfig from "./config/ResponseSearchConfig"
import ConversationConfig from "./config/ConversationConfig"
import ScriptConfig from "./config/ScriptConfig"
import ScriptSourceConfig from "./config/ScriptSourceConfig"

export class Main {
    public connection: SDKConnection
    private domain?: DomainConfig
    public defaultType: string = "Bots"
    public showAds: boolean = true
    private debug?: boolean
    private applicationId: string
    public adult?: boolean
    private domainId?: string
    private username: string
    private password: string
    public static website: string = "http://www.botlibre.com"
    public static websiteHttps: string = "https://www.botlibre.com"
    public static server: string = "botlibre.com"
    constructor(settings: {
        debug: boolean,
        adult: boolean,
        applicationId: string,
        username: string,
        password: string
    }) {
        this.debug = settings.debug
        this.adult = settings.adult
        this.applicationId = settings.applicationId
        this.username = settings.username
        this.password = settings.password
        this.connection = new SDKConnection(new BotlibreCredentials(this.applicationId), this.debug = true)
        if (this.domainId != undefined) {
            this.domain = new DomainConfig()
            this.domain.id = this.domainId
            this.connection.setDomain(this.domain)
            console.log(this.connection.getDomain())
        }
        if (this.debug) {
            Utils.log("[Main] init SDKConnection")
            this.showAds = false
            this.connection.setDebug(true)
        }
    }

    /** Login in with user credentials */
    async connectUserAccount(): Promise<UserConfig | SDKError> {
        if (this.connection.user != undefined) {
            Utils.log("User logged in\nToken has been established already.")
            Utils.log("User: " + this.connection.getUser())
            return this.connection.getUser()
        }
        let config = new UserConfig()
        config.application = this.applicationId
        config.user = this.username
        config.password = this.password
        let user = await this.connection.connect(config)
        return user
    }

    /*Send a chat message to a bot*/
    async sendChatMessage(message: string, botId: string): Promise<ChatResponse | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ChatConfig
        config.instance = botId
        config.message = message
        return this.connection.chat(config)
    }

    /**Fetch user details */
    async fetchUser(username: string): Promise<UserConfig | SDKError> {
        let config = new UserConfig()
        config.user = username
        return this.connection.fetchUser(config)
    }

    /** Create forum post */
    async createForumPost(forumId: string, topic: string, detials: string, tags: string): Promise<ForumPostConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.forum = forumId
        config.topic = topic
        config.details = detials
        config.tags = tags
        return this.connection.createForumPost(config)
    }
    /** Update forum post */
    async updateForumPost(forumId: string, postId:string, topic: string, detials: string, tags: string): Promise<ForumPostConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.id = postId
        config.forum = forumId
        config.topic = topic
        config.details = detials
        config.tags = tags
        return this.connection.updateForumPost(config)
    }

    /** Delete forum post */
    async deleteForumPost(forumId:string, postId: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.forum = forumId
        config.id = postId
        this.connection.deleteForumPost(config)
    }
    /** Create channel file attachment */
    async createChannelFileAttachment(instanceId:string, file:any, name:string, fileType: string): Promise<MediaConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new MediaConfig()
        config.instance = instanceId
        config.name = name
        config.type = fileType
        return this.connection.createChannelFileAttachment(file, config)
    }

    /** Create channel image attachment */
    async createChannelImageAttachment(instanceId:string, image:any, name:string, fileType: string): Promise<MediaConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new MediaConfig()
        config.instance = instanceId
        config.name = name
        config.type = fileType
        return this.connection.createChannelImageAttachment(image, config)
    }

    /** create reply */
    async createReply(forumId: string, postId:string, details: string, tags:string): Promise<ForumPostConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.forum = forumId
        config.id = postId
        config.details = details
        config.tags = tags
        return this.connection.createReplay(config)
    }

    /** Create user message */
    async createUserMessage(toUser: string, message: string): Promise<void> {
        let config = new UserMessageConfig()
        config.user = toUser
        config.message = message
        return this.connection.createUserMessage(config)
    }

    /** Save response */
    async saveResponse(data:{
        botId:string,
        responseId?: string,
        questionId?:string,
        type?:string,
        correctness?:string,
        question?:string,
        response?:string,
        topic?:string,
        label?:string,
        keywords?:string,
        required?:string,
        emotions?:string,
        actions?:string,
        poses?:string,
        previous?:string,
        onRepeat?:string,
        command?:string,
        noRepeat?:boolean,
        requirePrevious?:boolean,
        requireTopic?:boolean,
    }): Promise<ResponseConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ResponseConfig()
        config.instance = data.botId
        config.responseId = data.responseId
        config.questionId = data.questionId
        config.type = data.type
        config.correctness = data.correctness
        config.question = data.question
        config.response = data.response
        config.topic = data.topic
        config.label = data.label
        config.keywords = data.keywords
        config.required = data.required
        config.emotions = data.emotions
        config.actions = data.actions
        config.poses = data.poses
        config.previous = data.previous
        config.onRepeat = data.onRepeat
        config.command = data.command
        config.noRepeat = data.noRepeat
        config.requirePrevious = data.requirePrevious
        config.requireTopic = data.requireTopic
        return this.connection.saveResponse(config)
    }

    /** Delete response */
    async deleteResponse(responseId:string, questionId?:string, type?:string): Promise<void> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ResponseConfig()
        config.instance = responseId
        config.type = type
        config.questionId = questionId
        config.responseId = responseId
        return this.connection.deleteResponse(config)
    }

    /** Fetch forum post */
    async fetchForumPost(forumId: string): Promise<ForumPostConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.id = forumId
        return this.connection.fetchForumPost(config)
    }

    /**Avatar Message */
    async avatarMessage(data: {applicationId:string,
        instance: string, avatar: string, speak: boolean, message: string, emote: string, action: string,
        pose: string, voice: string, format: string, hd: boolean
    }): Promise<ChatResponse | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarMessage()
        config.application = data.applicationId
        config.instance = data.instance
        config.avatar = data.avatar
        config.speak = data.speak
        config.message = data.message
        config.emote = data.emote
        config.action = data.action
        config.pose = data.pose
        config.voice = data.voice
        config.format = data.format
        config.hd = data.hd
        return this.connection.avatarMessage(config)
    }

    /** Create a new user */
    async createUser(data: {
        user: string, password: string, hint: string, name: string, email: string,
        website: string, bio: string, showName: boolean
    }): Promise<UserConfig | SDKError> {
        let config = new UserConfig()
        config.user = data.user
        config.password = data.password
        config.hint = data.hint
        config.name = data.name
        config.email = data.email
        config.website = data.website
        config.bio = data.bio
        config.showName = data.showName
        return this.connection.createUser(config)
    }

    /** Update User details */
    async updateUser(data: {
        user: string, password: string, hint: string, name: string, email: string,
        website: string, bio: string, showName: boolean
    }): Promise<UserConfig | SDKError> {
        let config = new UserConfig()
        config.user = data.user
        config.password = data.password
        config.hint = data.hint
        config.name = data.name
        config.email = data.email
        config.website = data.website
        config.bio = data.bio
        config.showName = data.showName
        return this.connection.updateUser(config)
    }

    /** Get Admins of a forum */
    async getAdminsForum(data: {
        forumId: string
    }): Promise<Array<string> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumConfig()
        config.id = data.forumId
        return this.connection.getAdmins(config)
    }

    /** Get Users of a forum */
    async getUsersForum(data: { forumId: string }): Promise<Array<string> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumConfig()
        config.id = data.forumId
        return this.connection.getUsers(config)
    }
    /** Get posts list of a forum */
    async getForumPosts(data: {type: string, typeFilter: string, sort: string}): Promise<Array<ForumPostConfig> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new BrowseConfig()
        config.type = data.type
        config.typeFilter = data.typeFilter
        config.sort = data.sort
        return this.connection.getPosts(config)
    }

    /** Get categories */
    async getCategories(data: {type:string}): Promise<Array<ContentConfig> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ContentConfig()
        config.type = data.type
        return this.connection.getCategories(config)
    }

    /** Get tags */
    async getTags(data: {type:string}) : Promise<Array<string> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ContentConfig()
        config.type = data.type
        return this.connection.getTags(config)
    }

    /** Get all templates */
    async getAllTemplates() {
        return this.connection.getTemplates()
    }

    async saveForumBotMode(forumId:string, botId:string, mode:string): Promise<void> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        //"ListenOnly", "AnswerOnly", "AnswerAndListen"
        let config = new BotModeConfig()
        config.instance = forumId
        config.bot = botId
        config.mode = mode
        return this.connection.saveForumBotMode(config)
    }

    /** Get Channel bot mode */
    async getChannelBotMode(data: {channelId:string}) : Promise<BotModeConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ChannelConfig()
        config.id = data.channelId
        return this.connection.getChannelBotMode(config)
    }

    /** Save channel bot mode */
    async saveChannelBotMode(channelId:string, botId:string, mode:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        //"ListenOnly", "AnswerOnly", "AnswerAndListen"
        let config = new BotModeConfig()
        config.instance = channelId
        config.bot = botId
        config.mode = mode
        return this.connection.saveChannelBotMode(config)
    }

    async saveLearning(data: {botId:string, language:"en", correctionMode: "Everyone", LearningMode: "Everyone", learningRate:"55"}) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new LearningConfig()
        config.instance = data.botId
        config.correctionMode = data.correctionMode
        config.learningMode = data.LearningMode
        config.learningRate = data.learningRate
        config.scriptTimeout = 10000
        config.responseMatchTimeout = 1000
        config.conversationMatchPercentage = "50"
        config.discussionMatchPercentage = "90"
        config.enableEmoting = true
        config.enableEmotions = true
        config.enableComprehension = false //only supported for dedicated servers
        config.enableConsciousness = false //only supported for dedicated servers
        config.enableResponseMatch = true
        config.checkExactMatchFirst = true
        config.fixFormulaCase = true
        config.learnGrammar = true
        config.synthesizeResponse = true
        return this.connection.saveLearning(config)
    }

    //Save voice
    async saveVoice(botId:string, voice: string = "English : US : Female : SLT", mod:string, language: string, pitch:string, nativeVoice:boolean) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new VoiceConfig()
        config.instance = botId
        config.voice = voice
        config.mod = mod
        config.language = language
        config.pitch = pitch
        config.nativeVoice = nativeVoice
        return this.connection.saveVoice(config)
    }

    /** Save avatar bot */
    async saveBotAvatar(botId:string, avatarId:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new InstanceConfig()
        config.id = botId
        config.instanceAvatar = avatarId
        return this.connection.saveBotAvatar(config)
    }


    /** Train */
    async train(botId:string, operation:string, question:string, response:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new TrainingConfig()
        config.instance = botId
        config.operation = operation
        config.question = question
        config.response = response
        return this.connection.train(config)
    }

    /** User Admin */
    async userAdmin(botId:string, type:string = "Bot", operation:string, operationUser:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new UserAdminConfig()
        config.instance = botId
        config.type = type
        //["AddUser", "AddAdmin", "RemoveUser", "RemoveAdmin"]
        config.operation = operation
        config.operationUser = operationUser
        return this.connection.userAdmin(config)
    }

    /** Create avatar */
    async createAvatar(data: {
        name:string,
        description?:string,
        details?:string,
        disclamier?:string,
        categorise?:string
        license?:string,
        accessMode?:string,
        isPrivate?:boolean,
        isHidden?:boolean
    }): Promise<AvatarConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarConfig()
        config.name = data.name
        config.description = data.description
        config.details = data.details
        config.disclaimer = data.disclamier
        config.categories = data.categorise
        config.license = data.license
        config.accessMode = data.accessMode
        config.isPrivate = data.isPrivate
        config.isHidden = data.isHidden
        return this.connection.createAvatar(config)
    }

    /** Create a graphic media */
    async createGraphicMedia(graphicId:string, image:any, name:string, fileType:string = "image/jpeg") {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new GraphicConfig()
        config.id = graphicId
        config.fileName = name
        config.fileType = fileType
        return this.connection.createGraphicMedia(image, config)
    }

    /** Update current user icon */
    async updateUserIcon(image:any, name:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config =  this.connection.getUser()
        return this.connection.updateUserIcon(image,name, config)
    }

    /** Get forum bot mode */
    async getForumBotMode(forumId:string) : Promise<BotModeConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumConfig()
        config.id = forumId
        return this.connection.getForumBotMode(config)
    }

    /** Get voice */
    async getVoice(instanceId:string, id:string) : Promise<VoiceConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new InstanceConfig()
        config.id = id
        config.instance = instanceId
        return this.connection.getVoice(config)
    }

    /** Get default response */
    async getDefaultResponse(instanceId:string, id:string): Promise<Array<string> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new InstanceConfig()
        config.id = id
        config.instance = instanceId
        return this.connection.getDefaultResponses(config)
    }

    /** Get greetings */
    async getGreetings(instanceId:string, id:string): Promise<Array<string> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new InstanceConfig()
        config.id = id
        config.instance = instanceId
        return this.connection.getGreetings(config)
    }

    /** Get responses */
    async getResponses(data: {
        instance: string, 
        responseType: string,
        duration: string,
        inputType:string,
        restrict:string,
        filter:string
    }): Promise<Array<ResponseConfig> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ResponseSearchConfig()
        config.instance = data.instance
        config.responseType = data.responseType
        config.duration = data.duration
        config.inputType = data.inputType
        config.restrict = data.restrict
        config.filter = data.filter
        return this.connection.getResponse(config)
    }

    /** Get Conversations */
    async getConversations(instanceId:string, responseType:string = "conversation", duration:string = "week", sort:string = "date", inputType:string = "all"): Promise<Array<ConversationConfig> | SDKError> {
        let config = new ResponseSearchConfig()
        config.instance = instanceId
        config.responseType = responseType
        config.duration = duration
        config.inputType = inputType
        return this.connection.getConversations(config)
    }

    /** Get Learning */
    async getLearning(botId:string): Promise<LearningConfig | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new InstanceConfig()
        config.id = botId
        return this.connection.getLearning(config)
    }

    async browse(type:string, typeFilter: string, contentRating:string): Promise<Array<WebMediumConfig> | SDKError> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new BrowseConfig()
        config.type = type
        config.typeFilter = typeFilter
        config.contentRating = contentRating
        return this.connection.browse(config)
    }

    async getAvatarMedia(avatarId:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarConfig()
        config.id = avatarId
        return this.connection.getAvatarMedia(config)
    }

    async getScriptSource(scriptId:string){
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ScriptConfig()
        config.id = scriptId
        return this.connection.getScriptSource(config)
    }


    async saveScriptSource(scriptId:string, source:string = "Hello World") {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ScriptSourceConfig()
        config.instance = scriptId
        config.source = source
        return this.connection.saveScriptSource(config)
    }




    /* Save avatar background */
    async saveAvatarBackground(data: {avatarId:string, image:any, name:string, fileType: string}):Promise<void> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarMedia()
        config.name = data.name
        config.type = data.fileType
        config.instance = data.avatarId
        return this.connection.saveAvatarBackground(data.image, config)
    }
    /** Delete avatar background */
    async deleteAvatarBackground(avatarId:string): Promise<void> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarConfig()
        config.id = avatarId
        return this.connection.deleteAvatarBackground(config)
    }

    /** Create avatar media */
    async createAvatarMedia(avatarId: string, image:any, name:string, fileType:string = "image/jpeg") : Promise<void> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarMedia()
        config.instance = avatarId
        config.name = name
        config.type = fileType
        return this.connection.createAvatarMedia(image, config)
    } 
    /** Save avatar media */
    async saveAvatarMedia(data: {
        avatarId:string, mediaId:string,
        emotions?:string, actions?:string, 
        poses?:string, talking?:boolean, hd?:boolean
    }): Promise<void> {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarMedia()
        config.instance = data.avatarId
        config.mediaId = data.mediaId
        config.emotions = data.emotions
        config.actions = data.actions
        config.poses = data.poses
        config.talking = data.talking
        config.hd = data.hd
        return this.connection.saveAvatarMedia(config)
    }

    /** Delete avatar media */
    async deleteAvatarMedia(avatarId:string, mediaId:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new AvatarMedia()
        config.mediaId = mediaId
        config.instance = avatarId
        return this.connection.deleteAvatarMedia(config)        
    }

    /** Flag an instance */
    async flagInstance(config:WebMediumConfig, instanceId:string, flaggedReason:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        config.flaggedReason = flaggedReason
        config.id = instanceId
        return this.connection.flag(config)
    }
    /** Flag user */
    async flagUser(userId:string, flaggedReason:string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new UserConfig()
        config.user = userId
        config.flaggedReason = flaggedReason
        return this.connection.flagUser(config)
    }

    /** Subscribe to a forum post */
    async subscribeForumPost(postId: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.id = postId
        return this.connection.subscribeForumPost(config)
    }

    /** Subscribe to a forum */
    async subscribeForum(forumId: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumConfig()
        config.id = forumId
        return this.connection.subscribeForum(config)
    }
     /** unsubscribe to a forum post */
     async unsubscribeForumPost(postId: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumPostConfig()
        config.id = postId
        return this.connection.unsubscribeForumPost(config)
    }
     /** Unsubscribe to a forum */
     async unsubscribeForum(forumId: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumConfig()
        config.id = forumId
        return this.connection.unsubscribeForum(config)
    }

    /** Thumsb up */
    async thumbsUp(config:WebMediumConfig, id: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        config.id = id
        return this.connection.thumbsUp(config)
    }

    /** Thumsb up post*/
    async thumbsUpPost(config:ForumPostConfig, id: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        config.id = id
        return this.connection.thumbsUpPost(config)
    }

    /** Thumsb down */
    async thumbsDown(config:WebMediumConfig, id: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        config.id = id
        return this.connection.thumbsDown(config)
    }

    /** Thumsb up post*/
    async thumbsDownPost(config:ForumPostConfig, id: string) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        config.id = id
        return this.connection.thumbsDownPost(config)
    }

    /** Star a forum */
    async starForum(forumId:string, star:number) {
        if (!this.connection.getUser()) {
            throw new SDKException("Must be logged in first.")
        }
        let config = new ForumConfig()
        config.id = forumId
        config.stars = star.toString()
        return this.connection.star(config)
    }

    /** TTS */
    async TTS(voice: string = "English : US : Female : SLT" ,text:string = "Hello Word", mod: string = "default") {
        let config = new Speech()
        config.voice = voice
        config.text = text
        config.mod = mod
        return this.connection.tts(config)   
    }
 

}
