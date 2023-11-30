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
import {Utils} from './util/Utils'
import SDKException, { SDKError } from "./sdk/SDKException"
import ChatResponse from "./config/ChatResponse"
import ChatConfig from "./config/ChatConfig"

//TODO: REFACTOR: implement cookie to save token. use cookie-parser. Use map<string, Main> for every user.

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
    public static  websiteHttps: string = "https://www.botlibre.com"
    public static server: string = "botlibre.com"
    constructor(settings: {
        debug: boolean,
        adult: boolean,
        applicationId:string,
        username: string,
        password: string
    }) {
        this.debug = settings.debug
        this.adult = settings.adult
        this.applicationId = settings.applicationId
        this.username = settings.username
        this.password = settings.password
        this.connection = new SDKConnection(new BotlibreCredentials(this.applicationId), this.debug = true)
        if(this.domainId != undefined){
            this.domain = new DomainConfig()
            this.domain.id = this.domainId
            this.connection.setDomain(this.domain)
            console.log(this.connection.getDomain())
        }
        if(this.debug) {
            Utils.log("[Main] init SDKConnection")
            this.showAds = false
            this.connection.setDebug(true)
        }
    }   

    /** Login in with user credentials */
    async connectUserAccount(): Promise<UserConfig | SDKError> {
        if(this.connection.user != undefined) {
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
    async sendChatMessage(message:string, botId:string): Promise<ChatResponse | SDKError> {
        if(!this.connection.getUser()){
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
}
