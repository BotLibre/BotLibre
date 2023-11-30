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
import { Utils, XMLWriter } from '../util/Utils'
import fetch from 'node-fetch';
import ChatResponse from '../config/ChatResponse'
import ChatConfig from '../config/ChatConfig'
import { SDKError } from './SDKException'


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
    async sdkConnect<T>(config: Config, path: string, parseData: (root: any) => Promise<T>): Promise<T | SDKError> {
        try {
            config.addCredentials(this);
            let xml = await this.POST(this.url + path, config.toXML());
            let root = await Utils.loadXML(xml);

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
     * Fetch the URL for the image from the server.
     */
    fetchImage(image: string): string {
        let url: string = "http://" + this.getCredentials().getHost() + this.getCredentials().getApp() + "/" + image
        return url
    }

    /**
     * Fetch the forum post details for the forum post id.
     */
    //TODO: fetchForumPost(...)


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

    toString(): string {
        let writer = new XMLWriter("SDKConnection\n")
        writer.append("URL: " + this.url + "\n")
        writer.append("HOST: " + this.getCredentials().getHost() + "\n")
        return writer.toString()
    }
}

export default SDKConnection