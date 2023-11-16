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
import Credentials from './Credentials'
import UserConfig from '../config/UserConfig'
import DomainConfig from '../config/DomainConfig'
import SDKException from './SDKException'
import { Utils, XMLWriter } from '../util/Utils'
import fetch from 'node-fetch';
import { response } from 'express'
import ChatResponse from '../config/ChatResponse'
import ChatConfig from '../config/ChatConfig'


class SDKConnection {
    types: string[] = ["Bots", "Forums", "Graphics", "Live Chat", "Domains", "Scripts", "IssueTracker"]
    channelTypes: string[] = ["ChatRoom", "OneOnOne"]
    accessModes: string[] = ["Everyone", "Users", "Members", "Administrators"]
    mediaAccessModes: string[] = ["Everyone", "Users", "Members", "Administrators", "Disabled"]
    learningModes: string[] = ["Disabled", "Administrators", "Users", "Everyone"]
    correctionModes: string[] = ["Disabled", "Administrators", "Users", "Everyone"]
    botModes: string[] = ["ListenOnly", "AnswerOnly", "AnswerAndListen"]
    url?: string
    credentials?: Credentials
    debug: boolean = false
    domain?: DomainConfig
    user?: UserConfig
    /* Create an SDK connection with the credentials.
    Use the Credentials subclass specific to your server. */
    constructor(credentials: Credentials, debug: boolean = false) {
        this.credentials = credentials
        this.debug = debug
        this.url = credentials.getUrl()
    }
    /**
     * Return the name of the default user image.
     */
    defaultUserImage(): string {
        return "images/user-thumb.jpg"
    }
    /*
        Validate the user credentials (password, or token).
        The user details are returned (with a connection token, password removed).
        The user credentials are soted in the connection, and used on subsequent calls.
        An SDKException is thrown if the connect failed.
    */
    async connect(config: UserConfig): Promise<UserConfig | undefined> {
        try {
            config.addCredentials(this)
            let xml = await this.POST(this.url + "/check-user", config.toXML())
            let root = await Utils.loadXML(xml)
            //console.log(root)
            if(!root) {
                this.user = undefined
                throw new SDKException("User undefined.")
            }
            const user = new UserConfig()
            user.parseXML(root.user)
            this.user = user
        }catch(error) {
            console.error(error)
        }
        return this.user
    }

    async chat(config: ChatConfig): Promise<ChatResponse | undefined> {
        try {
            config.addCredentials(this)
            let xml = await this.POST(this.url + "/post-chat", config.toXML())
            let root = await Utils.loadXML(xml)
            if(!root) {
                throw new SDKException("Chat undefined.")
            }
            let response = new ChatResponse()
            response.parseXML(root.response)
            return response
        } catch(error) {
            console.log(error)
        }
    }

    /* Disconnect from the connection.
    An SDKConnection does not keep a live connection, but this resets its connected user and admin.*/
    disconnect() {
        this.user = undefined
        this.domain = undefined
    }

    /* Return the current application credentials. */
    getCredentials(): Credentials {
        if (this.credentials == undefined) {
            throw new SDKException("Credentials undefined")
        }
        return this.credentials
    }

    /* Set the application credentials. */
    setCredentials(credentials: Credentials) {
        this.credentials = credentials
        this.url = credentials.getUrl()
    }

    /* Return is debugging has been enabled */
    isDebug(): boolean {
        return this.debug
    }

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
            Utils.log("POST: " + url);
            Utils.log("XML: " + xml);
          }
      
          const headers = {
            'Content-Type': 'application/xml'
          };
      
          const requestOptions = {
            method: 'POST',
            headers: headers,
            body: xml
          };
      
          fetch(url, requestOptions)
            .then(function (response) {
              if (!response.ok) {
                throw new SDKException("HTTP Error Status: " + response.status);
              }
              return response.text();
            })
            .then(function (responseText) {
              Utils.log('Response: ' + responseText);
              resolve(responseText); // Resolve the promise with the response data
            })
            .catch(function (error) {
              console.error('Error: ' + error.message);
              reject(error); // Reject the promise with the error
            });
        });
      }

    toString(): string {
        let writer = new XMLWriter("SDKConnection\n")
        writer.append("URL: " + this.url + "\n")
        writer.append("HOST: " +this.getCredentials().getHost() + "\n")
        return writer.toString()
    }
}

export default SDKConnection