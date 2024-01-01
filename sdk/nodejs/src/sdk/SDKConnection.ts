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
import Config from '../config/Config'
import Credentials from './Credentials'
import UserConfig from '../config/UserConfig'
import DomainConfig from '../config/DomainConfig'
import SDKException from './SDKException'
import { Utils, XMLReader, XMLWriter } from '../util/Utils'
import fetch from 'node-fetch';
import ChatResponse from '../config/ChatResponse'
import ChatConfig from '../config/ChatConfig'
import { SDKError } from './SDKException'

import { ForumPostConfig } from '../config/ForumPostConfig'
import WebMediumConfig from '../config/WebMediumConfig'
import AvatarMessage from '../config/AvatarMessage'
import MediaConfig from '../config/MediaConfig'
import UserMessageConfig from '../config/UserMessageConfig'
import ResponseConfig from '../config/ResponseConfig'
import AvatarMedia from '../config/AvatarMedia'
import AvatarConfig from '../config/AvatarConfig'
import ForumConfig from '../config/ForumConfig'
import Speech from '../config/Speech'
import BrowseConfig from '../config/BrowseConfig'
import ContentConfig from '../config/ContentConfig'
import InstanceConfig from '../config/InstanceConfig'
import ChannelConfig from '../config/ChannelConfig'
import BotModeConfig from '../config/BotModeConfig'
import LearningConfig from '../config/LearningConfig'
import VoiceConfig from '../config/VoiceConfig'
import TrainingConfig from '../config/TrainingConfig'
import UserAdminConfig from '../config/UserAdminConfig'
import ResponseSearchConfig from '../config/ResponseSearchConfig'
import ConversationConfig from '../config/ConversationConfig'
import { types } from 'util'
import GraphicConfig from '../config/GraphicConfig'
import ScriptSourceConfig from '../config/ScriptSourceConfig'
import ScriptConfig from '../config/ScriptConfig'


class SDKConnection {
    static types: string[] = ["Bots", "Forums", "Graphics", "Live Chat", "Domains", "Scripts", "IssueTracker"]
    static channelTypes: string[] = ["ChatRoom", "OneOnOne"]
    static accessModes: string[] = ["Everyone", "Users", "Members", "Administrators"]
    static mediaAccessModes: string[] = ["Everyone", "Users", "Members", "Administrators", "Disabled"]
    static learningModes: string[] = ["Disabled", "Administrators", "Users", "Everyone"]
    static correctionModes: string[] = ["Disabled", "Administrators", "Users", "Everyone"]
    static botModes: string[] = ["ListenOnly", "AnswerOnly", "AnswerAndListen"]
    url?: string
    static credentials?: Credentials
    private debug: boolean = false
    domain?: DomainConfig
    user?: UserConfig
    /* Create an SDK connection with the credentials.
    Use the Credentials subclass specific to your server. */
    constructor(credentials: Credentials, debug: boolean = false) {
        SDKConnection.credentials = credentials
        this.debug = debug
        this.url = credentials.getUrl()
    }
    /**
     * Return the name of the default user image.
     */
    defaultUserImage(): string {
        return "images/user-thumb.jpg"
    }





    /**Helper function to make a api call request
     * @returns: root of xml tree.
     */
    async sdkConnect<T>(config: Config, path: string, parseData: (root: any) => Promise<T>, file: FileUpload = { type: 'none' }): Promise<T | SDKError> {
        try {
            config.addCredentials(this);
            let xml: undefined
            switch (file.type) {
                case "file":
                    this.POSTFILE(this.url + path, file.data, file.name,config.toXML())
                    break
                case "image":
                    this.POSTIMAGE(this.url + path, file.data, file.name,config.toXML())
                    break
                case "none":
                default:
                    xml = await this.POST(this.url + path, config.toXML());
                    break
            }


            let root = await Utils.loadXML(xml as any);

            if (!root) {
                return {
                    message: 'Failed to load XML.',
                    statusCode: 401,
                    data: xml
                }
            }

            const parsedData = await parseData(root);
            return parsedData;
        } catch (error) {
            console.error(error);
            return {
                message: 'An error occurred.',
                statusCode: 500,
                data: error
            }
        }
    }


    /*
        Validate the user credentials (password, or token).
        The user details are returned (with a connection token, password removed).
        The user credentials are soted in the connection, and used on subsequent calls.
        An SDKException is thrown if the connect failed.
    */
    async connect(config: UserConfig): Promise<UserConfig | SDKError> {
        return await this.sdkConnect<UserConfig>(config, '/check-user', (root): Promise<UserConfig> => {
            let user = new UserConfig()
            user.parseXML(root.user)
            this.user = user
            return Promise.resolve(user)
        })
    }

    /*
     Process the bot chat message and return the bot's response.
     The ChatConfig should contain the conversation id if part of a conversation.
     If a new conversation the conversation id i returned in the response.
    */
    async chat(config: ChatConfig): Promise<ChatResponse | SDKError> {
        return await this.sdkConnect<ChatResponse>(config, '/post-chat', (root): Promise<ChatResponse> => {
            let response = new ChatResponse()
            response.parseXML(root.response)
            return Promise.resolve(response)
        })
    }

    /**
     * Connect to the domain.
     * A domain is an isolated content space.
     * Any browse or query request will be specific to the domain's content.
     */
    async connectDomain(config: DomainConfig): Promise<DomainConfig | SDKError> {
        return await this.sdkConnect<DomainConfig>(config, '/check-', (root): Promise<DomainConfig> => {
            let domain = new DomainConfig()
            domain.parseXML(root.domain)
            return Promise.resolve(domain)
        })
    }

    /**
     * Fetch the user details.
     */
    async fetchUser(config: UserConfig): Promise<UserConfig | SDKError> {
        return await this.sdkConnect<UserConfig>(config, '/view-user', (root): Promise<UserConfig> => {
            let user = new UserConfig()
            user.parseXML(root.user)
            return Promise.resolve(user)
        })
    }

    /**
     * Process the avatar message and return the avatars response.
     * This allows the speech and video animation for an avatar to be generated for the message.
     */
    async avatarMessage(config: AvatarMessage): Promise<ChatResponse | SDKError> {
        return await this.sdkConnect<ChatResponse>(config, '/avatar-message', (root): Promise<ChatResponse> => {
            let config = new ChatResponse()
            config.parseXML(root.response)
            return Promise.resolve(config)
        })
    }
    /**
     * Process the speech message and return the server generate text-to-speech audio file.
     * This allows for server-side speech generation.
     */
    async tts(config: Speech) {
        config.addCredentials(this)
        await this.POST(this.url + '/speak', config.toXML())
    }

    /**
     * Return the administrators of the content.
     */
    async getAdmins(config: WebMediumConfig): Promise<Array<string> | SDKError> {
        return await this.sdkConnect<Array<string>>(config, '/get-' + config.getType() + '-admins', (root): Promise<Array<string>> => {
            let users = new Array<string>()
            let reader = new XMLReader(root.userConfigs)
            let elements = reader.readElements('user')
            elements.forEach((element) => {
                let config = new UserConfig()
                config.parseXML(element)
                if (config.user) {
                    users.push(config.user)
                }
            })
            return Promise.resolve(users)
        })
    }

    /**
     *  Return the users of the content.
     */
    async getUsers(config: WebMediumConfig): Promise<Array<string> | SDKError> {
        return await this.sdkConnect<Array<string>>(config, '/get-' + config.getType() + '-users', (root): Promise<Array<string>> => {
            let users = new Array<string>()
            let reader = new XMLReader(root.userConfigs)
            let elements = reader.readElements('user')
            elements.forEach((element) => {
                let config = new UserConfig()
                config.parseXML(element)
                if (config.user) {
                    users.push(config.user)
                }
            })
            return Promise.resolve(users)
        })
    }

    /**
     * Return the list of forum posts for the forum browse criteria.
     */
    async getPosts(config: BrowseConfig): Promise<Array<ForumPostConfig> | SDKError> {
        return await this.sdkConnect<Array<ForumPostConfig>>(config, '/get-forum-posts', (root): Promise<Array<ForumPostConfig>> => {
            let instances = new Array<ForumPostConfig>()
            let reader = new XMLReader(root.forumPostConfigs)
            let elements = reader.readElements('forum-post')
            elements.forEach((elements) => {
                let config = new ForumPostConfig()
                config.parseXML(elements)
                instances.push(config)
            })
            return Promise.resolve(instances)
        })
    }

    /**
     * Return the list of categories for the type, and domain.
     */
    async getCategories(config: ContentConfig): Promise<Array<ContentConfig> | SDKError> {
        return await this.sdkConnect<Array<ContentConfig>>(config, '/get-categories', (root): Promise<Array<ContentConfig>> => {
            let categories = new Array<ContentConfig>()
            console.log(root)
            let reader = new XMLReader(root.categoryConfigs)
            let elements = reader.readElements('category')
            elements.forEach((element) => {
                let config = new ContentConfig()
                config.parseXML(element)
                categories.push(config)
            })
            return Promise.resolve(categories)
        })
    }

    /**
     * Return the list of tags for the type, and domain.
     */
    async getTags(config: ContentConfig): Promise<Array<string> | SDKError> {
        return await this.sdkConnect<Array<string>>(config, '/get-tags', (root): Promise<Array<string>> => {
            let tags = new Array<string>()
            let reader = new XMLReader(root.tagConfigs)
            let elements = reader.readElements('tag')
            elements.forEach((element) => {
                let config = new ContentConfig()
                config.parseXML(element)
                if (config.name) {
                    tags.push(config.name)
                }
            })
            return Promise.resolve(tags)
        })
    }

    /**
     * Return the list of bot templates.
     */
    async getTemplates(): Promise<Array<string> | SDKError> {
        let xml = await this.GET(this.url + '/get-all-templates')
        let root = await Utils.loadXML(xml)
        let instances = new Array<string>()
        if (root) {
            let reader = new XMLReader(root.instanceConfigs)
            let elements = reader.readElements('instance')
            elements.forEach((element) => {
                let config = new InstanceConfig()
                config.parseXML(element)
                if (config.name) {
                    instances.push(config.name)
                }
            })
        }
        return Promise.resolve(instances)
    }

    /**
     * Return the channel's bot configuration.
     */
    async getChannelBotMode(config: ChannelConfig): Promise<BotModeConfig | SDKError> {
        return await this.sdkConnect<BotModeConfig>(config, '/get-channel-bot-mode', (root): Promise<BotModeConfig> => {
            let config = new BotModeConfig()
            config.parseXML(root['bot-mode'])
            return Promise.resolve(config)
        })
    }
    /**
     * Save the channel's bot configuration.
     */
    async saveChannelBotMode(config: BotModeConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/save-channel-bot-mode', config.toXML())
    }

    /**
     * Save the forum's bot configuration.
     */
    async saveForumBotMode(config: BotModeConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/save-forum-bot-mode', config.toXML())
    }

    /**
     * Save the bot's learning configuration.
     */
    async saveLearning(config: LearningConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/save-learning', config.toXML())
    }

    /**
     * Save the bot's voice configuration.
     */
    async saveVoice(config: VoiceConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/save-voice', config.toXML())
    }
    /**
     * Train the bot with a new question/response pair.
     */
    async train(config: TrainingConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/train-instance', config.toXML())
    }

    /**
     * Perform the user administration task (add or remove users, or administrators).
     */
    async userAdmin(config: UserAdminConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/user-admin', config.toXML())
    }
    /**
     * Save the image as the avatar's background.
     */
    //TODO: Test
    async saveAvatarBackground(file: any, config: AvatarMedia) {
        config.addCredentials(this)
        await this.POSTIMAGE(this.url + "/save-avatar-background", file, config.name, config.toXML())
    }

    /** Create graphic media */
    async createGraphicMedia(file:any, config:GraphicConfig) {
        config.addCredentials(this)
        if(config.fileType?.includes("image")) {
            this.POSTIMAGE(this.url + "/update-graphic-media", file, config.name, config.toXML())
        }else {
            this.POSTFILE(this.url + "/update-graphic-media", file, config.name, config.toXML())
        }
    }

    /** Update user icon */
    async updateUserIcon(file:any, name:string, config:UserConfig) : Promise<UserConfig | SDKError>{
        return this.sdkConnect<UserConfig>(config, '/update-user-icon', (root):Promise<UserConfig>=>{
            let config = new UserConfig()
            config.parseXML(root.user)
            return Promise.resolve(config)
        },{type:'image', data:file, name:name})
    }

    /**
     * Add the avatar media file to the avatar.
     */
    async createAvatarMedia(file: any, config: AvatarMedia) {
        config.addCredentials(this)
        await this.POSTIMAGE(this.url + "/update-user-icon", file, config.name, config.toXML())
    }
    /**
     * Update the user's icon.
     * The file will be uploaded to the server.
     */
    async updateIcon(file: any, config: UserConfig): Promise<UserConfig> {
        let xml = await this.POSTIMAGE(this.url + "/update-user-icon", file, config.name, config.toXML())
        let root = await Utils.loadXML(xml)
        if (root) {
            let config = new UserConfig()
            config.parseXML(root.user)
        }
        return Promise.resolve(config)
    }
    /**
     * Return the forum's bot configuration.
     */
    async getForumBotMode(config: ForumConfig): Promise<BotModeConfig | SDKError> {
        return await this.sdkConnect<BotModeConfig>(config, '/get-forum-bot-mode', (root): Promise<BotModeConfig> => {
            let config = new BotModeConfig()
            config.parseXML(root['bot-mode'])
            return Promise.resolve(config)
        })
    }
    /**
     * Return the bot's voice configuration.
     */
    async getVoice(config: InstanceConfig): Promise<VoiceConfig | SDKError> {
        return await this.sdkConnect<VoiceConfig>(config, '/get-voice', (root): Promise<VoiceConfig> => {
            let config = new VoiceConfig()
            config.parseXML(root.voice)
            return Promise.resolve(config)
        })
    }
    /**
     * Return the bot's default responses.
     */
    async getDefaultResponses(config: InstanceConfig): Promise<Array<string> | SDKError> {
        config.addCredentials(this)
        let xml = await this.POST(this.url + '/get-default-responses', config.toXML())
        let defaultResponses = new Array<string>()
        let root = await Utils.loadXML(xml)
        if (root) {
            let reader = new XMLReader(root.instanceConfigs)
            let elements = reader.readElements('instance')
            elements.forEach((element) => {
                let config = new InstanceConfig()
                config.parseXML(element)
                if (config.name) {
                    defaultResponses.push(config.name)
                }
            })
        }
        return Promise.resolve(defaultResponses)
    }
    /**
     * Return the bot's greetings.
     */
    async getGreetings(config: InstanceConfig): Promise<Array<string> | SDKError> {
        config.addCredentials(this)
        let xml = await this.POST(this.url + '/get-greetings', config.toXML())
        let root = await Utils.loadXML(xml)
        let instances = new Array<string>()
        console.log(root)
        if (root) {
            let reader = new XMLReader(root.instanceConfigs)
            let elements = reader.readElements('instance')
            elements.forEach((element) => {
                let config = new InstanceConfig()
                config.parseXML(element)
                if (config.name) {
                    instances.push(config.name)
                }
            })
        }
        return Promise.resolve(instances)
    }
    /**
     * Search the bot's responses.
     */
    async getResponse(config: ResponseSearchConfig): Promise<Array<ResponseConfig> | SDKError> {
        return await this.sdkConnect<Array<ResponseConfig>>(config, '/get-responses', (root): Promise<Array<ResponseConfig>> => {
            let responses = new Array<ResponseConfig>()
            let reader = new XMLReader(root.responseConfigs)
            let elements = reader.readElements('response')
            console.log(root)
            elements.forEach((element) => {
                let config = new ResponseConfig()
                config.parseXML(element)
                if (config) {
                    responses.push(config)
                }
            })
            return Promise.resolve(responses)
        })
    }

    /**
     * Search the bot's conversations.
     */
    async getConversations(config: ResponseSearchConfig): Promise<Array<ConversationConfig> | SDKError> {
        return await this.sdkConnect<Array<ConversationConfig>>(config, '/get-conversations', (root): Promise<Array<ConversationConfig>> => {
            console.log(root)
            let conversations = new Array<ConversationConfig>()
            let reader = new XMLReader(root.conversationConfigs)
            let elements = reader.readElements('conversation')
            elements.forEach(element => {
                let config = new ConversationConfig()
                config.parseXML(element)
                if(config) {
                    conversations.push(config)
                }
            })
            return Promise.resolve(conversations)
        })
    }
    /**
	 * Return the bot's learning configuration.
	 */
    async getLearning(config: InstanceConfig): Promise<LearningConfig | SDKError> {
        return await this.sdkConnect<LearningConfig>(config, '/get-learning', (root): Promise<LearningConfig>=> {
            let config = new LearningConfig()
            config.parseXML(root.learning)
            return Promise.resolve(config)
        })
    }

    /**
	 * Return the list of content for the browse criteria.
	 * The type defines the content type (one of Bot, Forum, Channel, Domain).
	 */

    async browse(config: BrowseConfig): Promise<Array<WebMediumConfig> | SDKError> {
        config.addCredentials(this)
        let type: string = ""
        if(type === "Bot") {
            type="/get-instances"
        } else {
            type = "/get-" + config.type?.toLowerCase() + "s"
        }
        let xml = await this.POST(this.url + type, config.toXML())
        let root = await Utils.loadXML(xml)
        let instances = new Array<WebMediumConfig>()
        console.log(root)
        if(root) {
            let reader = new XMLReader(root)
            let elements = reader.readElements('browse')
            elements.forEach(element => {
                let instance
                if(config.type === "Bot") {
                    instance = new InstanceConfig()
                } else if(config.type === "Forum") {
                    instance = new ForumConfig()
                }else if(config.type === "Channel") {
                    instance = new ChannelConfig()
                } else if(config.type === "Domain") {
                    instance = new DomainConfig()
                } else if(config.type === "Avatar") {
                    instance = new AvatarConfig()
                }
                instance?.parseXML(element)
                if(instance){
                    instances.push(instance)
                }
            })
        }
        return Promise.resolve(instances)
    }


    /**
	 * Return the list of media for the avatar.
	 */
    async getAvatarMedia(config: AvatarConfig): Promise<Array<AvatarMedia> | SDKError> {
        return await this.sdkConnect<Array<AvatarMedia>>(config,'/get-avatar-media', (root): Promise<Array<AvatarMedia>>=> {
            console.log(root)
            let reader = new XMLReader(root)
            let instances = new Array<AvatarMedia>()
            let elements = reader.readElements('avatar-media')
            elements.forEach(element => {
                let config = new AvatarMedia()
                config.parseXML(element)
                instances.push(config)
            })
            return Promise.resolve(instances)
        })
    }

    /**
     * Save the bot's avatar configuration.
     */
    async saveBotAvatar(config: InstanceConfig) {
        config.addCredentials(this)
        this.POST(this.url + '/save-bot-avatar', config.toXML())
    }

    /**
     * Create a new user
     */
    async createUser(config: UserConfig): Promise<UserConfig | SDKError> {
        return await this.sdkConnect<UserConfig>(config, '/create-uest', (root): Promise<UserConfig> => {
            let config = new UserConfig()
            config.parseXML(root.user)
            return Promise.resolve(config)
        })
    }

    /** 
     * Craete new content
     */
    async createAvatar(config:AvatarConfig): Promise<AvatarConfig | SDKError> {
        return await this.sdkConnect<AvatarConfig>(config, '/create-avatar', (root): Promise<AvatarConfig>=>{
            let config = new AvatarConfig()
            config.parseXML(root.avatar)
            return Promise.resolve(config)
        })
    }

    /**
     * Create a new forum post.
     * You must set the forum id for the post.
     */
    async createForumPost(config: ForumPostConfig): Promise<ForumPostConfig | SDKError> {
        return await this.sdkConnect<ForumPostConfig>(config, '/create-forum-post', (root): Promise<ForumPostConfig> => {
            let config = new ForumPostConfig()
            config.parseXML(root["forum-post"])
            return Promise.resolve(config)
        })
    }

    /** Return the script source */
    async getScriptSource(config:ScriptConfig) : Promise<ScriptSourceConfig | SDKError>{
        return await this.sdkConnect<ScriptSourceConfig>(config, '/get-script-source', (root): Promise<ScriptSourceConfig>=> {
            let config = new ScriptSourceConfig()
            config.parseXML(root['script-source'])
            return Promise.resolve(config)
        })
    }
    /** Create or update script - Save the script source */
    async saveScriptSource(config: ScriptSourceConfig) {
        config.addCredentials(this)
        this.POST(this.url + '/save-script-source', config.toXML())
    }

    /**
     * Create a new file/image/media attachment for a chat channel.
     */

    //TODO: FILE IS NOT ADDED. NEED: POSTFILE(...)
    async createChannelFileAttachment(file: any, config: MediaConfig): Promise<MediaConfig | SDKError> {
        return await this.sdkConnect<MediaConfig>(config, 'create-channel-attachment', (root): Promise<MediaConfig> => {
            let config = new MediaConfig()
            config.parseXML(root.media)
            return Promise.resolve(config)
        })
    }

    /**
     * Create a new file/image/media attachment for a chat channel.
     */
    //TODO: FILE IS NOT ADDED. NEED: POSTIMAGE(...)
    async createChannelImageAttachment(file: any, config: MediaConfig): Promise<MediaConfig | SDKError> {
        return await this.sdkConnect<MediaConfig>(config, 'create-channel-attachment', (root): Promise<MediaConfig> => {
            let config = new MediaConfig()
            config.parseXML(root.media)
            return Promise.resolve(config)
        })
    }

    /**
     * Create a reply to a forum post.
     * You must set the parent id for the post replying to.
     */
    async createReplay(config: ForumPostConfig): Promise<ForumPostConfig | SDKError> {
        return await this.sdkConnect<ForumPostConfig>(config, 'create-reply', (root): Promise<ForumPostConfig> => {
            let config = new ForumPostConfig()
            config.parseXML(root['forum-post'])
            return Promise.resolve(config)
        })
    }

    /**
     * Create a user message.
     * This can be used to send a user a direct message.
     * SPAM will cause your account to be deleted.
     */
    async createUserMessage(config: UserMessageConfig) {
        config.addCredentials(this);
        await this.POST(this.url + '/create-user-message', config.toXML());
    }

    /**
     * Update the forum post.
     */
    async updateForumPost(config: ForumPostConfig): Promise<ForumPostConfig | SDKError> {
        return await this.sdkConnect<ForumPostConfig>(config, '/update-forum-post', (root): Promise<ForumPostConfig> => {
            let config = new ForumPostConfig()
            config.parseXML(root['forum-post'])
            return Promise.resolve(config)
        })
    }

    /**
     * Create or update the response.
     * This can also be used to flag, unflag, validate, or invalidate a response.
     */
    async saveResponse(config: ResponseConfig): Promise<ResponseConfig | SDKError> {
        return await this.sdkConnect<ResponseConfig>(config, '/save-response', (root): Promise<ResponseConfig> => {
            let response = new ResponseConfig()
            response.parseXML(root.response)
            return Promise.resolve(response)
        })
    }

    /**
     * Update the user details.
     * The password must be passed to allow the update.
     */
    async updateUser(config: UserConfig): Promise<UserConfig | SDKError> {
        return await this.sdkConnect<UserConfig>(config, 'update-user', (root): Promise<UserConfig> => {
            let config = new UserConfig()
            config.parseXML(root.user)
            return Promise.resolve(config)
        })
    }

    /**
     * Permanently delete the content with the id.
     */
    async delete(config: WebMediumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/delete-' + config.getType(), config.toXML())
        if (this.domain != null && this.domain.id == config.id && config.getType() == "domain") {
            this.domain = undefined
        }
    }
    /**
     * Permanently delete the forum post with the id.
    */
    async deleteForumPost(config: ForumPostConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/delete-forum-post', config.toXML())
    }
    /**
     * Permanently delete the response, greetings, or default response with the response id (and question id).
     */
    async deleteResponse(config: ResponseConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/delete-response', config.toXML())
    }

    /**
     * Permanently delete the avatar media.
     */
    async deleteAvatarMedia(config: AvatarMedia) {
        config.addCredentials(this)
        await this.POST(this.url + '/delete-avatar-media', config.toXML())
    }
    /**
     * Permanently delete the avatar background.
     */
    async deleteAvatarBackground(config: AvatarConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/delete-avatar-background', config.toXML())
    }
    /**
     * Save the avatar media tags.
     */
    async saveAvatarMedia(config: AvatarMedia) {
        config.addCredentials(this)
        await this.POST(this.url + '/save-avatar-media', config.toXML())
    }
    /**
     * Flag the content as offensive, a reason is required.
     */
    async flag(config: WebMediumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/flag' + config.getType(), config.toXML())
    }

    /**
     * Subscribe for email updates for the forum.
     */

    async subscribeForum(config: ForumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/subscribe-forum', config.toXML())
    }

    /**
     * Unsubscribe from email updates for the forum.
     */
    async unsubscribeForum(config: ForumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/unsubscribe-forum', config.toXML())
    }

    /**
     * Thumbs up the content.
     */
    async thumbsUp(config: WebMediumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/thumbs-up-' + config.getType(), config.toXML())
    }
    /**
     * Thumbs down the content.
     */
    async thumbsDown(config: WebMediumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/thumbs-down-' + config.getType(), config.toXML())
    }
    /**
     * Rate the content.
     */
    async star(config: WebMediumConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/star-' + config.getType(), config.toXML())
    }
    /**
     * Thumbs up the content.
     */
    async thumbsUpPost(config: ForumPostConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/thumbs-up-post', config.toXML())
    }
    /**
     * Thumbs down the content.
     */
    async thumbsDownPost(config: ForumPostConfig) {
        config.addCredentials(this);
        await this.POST(this.url + '/thumbs-down-post', config.toXML());
    }
    /**
     * Rate the content.
     */
    async starPost(config: ForumPostConfig) {
        config.addCredentials(this)
        await this.POST(this.url + '/star-post', config.toXML());
    }
    /**
     * Flag the forum post as offensive, a reason is required.
     */
    async flagPost(config: WebMediumConfig) {
        config.addCredentials(this);
        await this.POST(this.url + '/flag-forum-post', config.toXML());
    }
    /**
     * Flag the user post as offensive, a reason is required.
     */

    async flagUser(config: UserConfig) {
        config.addCredentials(this);
        await this.POST(this.url + '/flag-user', config.toXML());
    }
    /** Subscribe for email updates for the post. */
    async subscribeForumPost(config: ForumPostConfig) {
        config.addCredentials(this);
        await this.POST(this.url + '/subscribe-post', config.toXML());
    }

    /** unsubscribe for email updates for the post. */
    async unsubscribeForumPost(config: ForumPostConfig) {
        config.addCredentials(this);
        await this.POST(this.url + '/unsubscribe-post', config.toXML());
    }



    /**
     * Fetch the URL for the image from the server.
     */
    fetchImage(image: string): string {
        let url: string = "http://" + this.getCredentials().getHost() + this.getCredentials().getApp() + "/" + image
        return url
    }

    /**
     * Fetch the forum post details for the forum post id.
     */
    async fetchForumPost(config: ForumPostConfig): Promise<ForumPostConfig | SDKError> {
        return await this.sdkConnect<ForumPostConfig>(config, '/check-forum-post', (root): Promise<ForumPostConfig> => {
            let config = new ForumPostConfig()
            config.parseXML(root['forum-post'])
            return Promise.resolve(config)
        })
    }


    /* Disconnect from the connection.
    An SDKConnection does not keep a live connection, but this resets its connected user and admin.*/
    disconnect() {
        this.user = undefined
        this.domain = undefined
    }

    /* Return the current application credentials. */
    getCredentials(): Credentials {
        if (SDKConnection.credentials == undefined) {
            throw new SDKException("Credentials undefined")
        }
        return SDKConnection.credentials
    }

    /* Set the application credentials. */
    setCredentials(credentials: Credentials) {
        SDKConnection.credentials = credentials
        this.url = credentials.getUrl()
    }

    /* Return is debugging has been enabled */
    isDebug(): boolean {
        return this.debug
    }

    /* Enable debugging  */
    setDebug(debug: boolean): void {
        this.debug = debug
    }
    /*
        Return the current domain.
        A domain is an isolated content space.
    */
    getDomain(): DomainConfig | undefined {
        if (this.domain != undefined) {
            return this.domain
        }
        return undefined
    }

    /* 
        Set the current domain.
        A domain is an isolated content space.
        connect() should be used to validate and connect a domain.
    */
    setDomain(domain: DomainConfig) {
        this.domain = domain
    }
    /* Return the current connected user. */
    getUser(): UserConfig {
        if (this.user == undefined) {
            throw new SDKException("User is undefined.")
        }
        return this.user
    }

    async GET(url: string): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.debug) {
                Utils.log("POST: " + url)
            }
            fetch(url, { method: 'GET' })
                .then(function (response) {
                    return response.text();
                })
                .then(function (responseText) {
                    Utils.log('Response: ' + responseText)
                    resolve(responseText) //Resolve the promise with the response data
                    return responseText
                })
                .catch(function (error) {
                    Utils.log('Error: ' + error.message)
                    reject(error) //Reject the promise with the error
                    return error.message
                })
        })
    }

    async POST(url: string, xml: string): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.debug) {
                Utils.log("POST: " + url)
                Utils.log("XML: " + xml)
            }

            const headers = {
                'Content-Type': 'application/xml'
            }

            const requestOptions = {
                method: 'POST',
                headers: headers,
                body: xml
            }
            fetch(url, requestOptions)
                .then(function (response) {
                    return response.text();
                })
                .then(function (responseText) {
                    Utils.log('Response: ' + responseText)
                    resolve(responseText) //Resolve the promise with the response data
                    return responseText
                })
                .catch(function (error) {
                    Utils.log('Error: ' + error.message)
                    reject(error) //Reject the promise with the error
                    return error.message
                })
        })
    }

    async POSTFILE(url: string, file: Blob, name: string = 'name', xml: string): Promise<any> {
        return await new Promise((resolve, reject) => {
            if (this.debug) {
                Utils.log("POST: " + url)
                Utils.log("XML: " + xml)
                Utils.log("name: " + name)
                console.log(file)
            }

            const formData: BodyInit = new FormData();
            formData.append('xml', xml);
            formData.append('file', file, name);
            console.log(formData)

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'multipart/form-data' },
                body: formData as any
            })
                .then(function (response) {
                    return response.text();
                })
                .then(function (responseText) {
                    Utils.log('Response: ' + responseText)
                    resolve(responseText);
                    return responseText;
                })
                .catch(function (error) {
                    Utils.log('Error: ' + error.message)
                    reject(error);
                    return error.message;
                });
        })
    }

    //TODO: Fix issue with the server, receving Internal Error 500, Null Pointer Exception, Multipart...
    async POSTIMAGE(url: string, image: Blob, name: string = 'name', xml: string): Promise<any> {
        return await new Promise((resolve, reject) => {
            if (this.debug) {
                Utils.log("POST: " + url)
                Utils.log("XML: " + xml)
                Utils.log("name: " + name)
                console.log(image)
            }

            const formData: BodyInit = new FormData();
            formData.append('xml', xml);
            formData.append('file', image, name);
            console.log(formData)
            fetch(url, {
                method: 'POST',
                body: formData as any,
                headers: { 'Content-Type': 'multipart/form-data' }
            })
                .then(function (response) {
                    return response.text();
                })
                .then(function (responseText) {
                    Utils.log('Response: ' + responseText)
                    resolve(responseText);
                    return responseText;
                })
                .catch(function (error) {
                    Utils.log('Error: ' + error.message)
                    reject(error);
                    return error.message;
                });
        })
    }

    /**
	 * Return the list of content types.
	 */
    getTypes(): Array<string> {
        return SDKConnection.types
    }

    /**
	 * Return the channel types.
	 */
    getChannelTypes(): Array<string> {
        return SDKConnection.channelTypes
    }

    /**
	 * Return the access mode types.
	 */
    getAccessModes(): Array<string> {
        return SDKConnection.accessModes
    }


    /**
	 * Return the media access mode types.
	 */
	getMediaAccessModes(): Array<string> {
		return SDKConnection.mediaAccessModes
	}
	
	/**
	 * Return the learning mode types.
	 */
    getLearningModes() : Array<string>{
		return SDKConnection.learningModes
	}
	
	/**
	 * Return the correction mode types.
	 */
    getCorrectionModes() : Array<string> {
		return SDKConnection.correctionModes
	}
	
	/**
	 * Return the bot mode types.
	 */
	getBotModes(): Array<string> {
		return SDKConnection.botModes
	}
	

    toString(): string {
        let writer = new XMLWriter("SDKConnection\n")
        writer.append("URL: " + this.url + "\n")
        writer.append("HOST: " + this.getCredentials().getHost() + "\n")
        return writer.toString()
    }
}

interface FileUpload {
    type: "image" | "file" | 'none',
    data?:any,
    name?:string
}

export default SDKConnection