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
import SDKException from "./sdk/SDKException"
import ChatResponse from "./config/ChatResponse"
import ChatConfig from "./config/ChatConfig"

export class Main {
    public connection: SDKConnection
    public domain?: DomainConfig
    public defaultType: string = "Bots"
    public showAds: boolean = true
    public debug?: boolean
    public applicationId: string
    public adult?: boolean
    public domainId?: string
    public username: string
    public password: string
    public website: string = "http://www.botlibre.com"
    public websiteHttps: string = "https://www.botlibre.com"
    public server: string = "botlibre.com"
    constructor(pack: {
        debug: boolean,
        adult: boolean,
        applicationId:string,
        username: string,
        password: string
    }) {
        this.debug = pack.debug
        this.adult = pack.adult
        this.applicationId = pack.applicationId
        this.username = pack.username
        this.password = pack.password
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

    async connectUserAccount(): Promise<UserConfig> {
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
        if(user == undefined) {
            throw new SDKException(" Main User undefined.")
        }
        return user
    }

    async sendChatMessage(message:string, botId:string): Promise<ChatResponse | undefined> {
        if(this.connection.getUser()==undefined){
            throw new SDKException("Must be logged in first")
        }
        let config = new ChatConfig
        config.instance = botId
        config.message = message
        return this.connection.chat(config)
    }
}
