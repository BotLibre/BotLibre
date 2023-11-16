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
import Config from "./Config";
import SDKConnection from "../sdk/SDKConnection";
import {XMLReader, XMLWriter} from "../util/Utils"
import SDKException from "../sdk/SDKException";
//TODO: Implement or import a Date for functions displayJoined, displayLastJoined

class UserConfig extends Config {
    password?: string
    newPassword?: string
    hint?: string
    name?: string
    showName?: boolean
    email?: string
    website?: string
    bio?: string
    over18?: boolean
    avatar?: string
    connects?: string
    bots?: string
    posts?: string
    messages?: string
    forums?: string
    scripts?: string
    graphics?: string
    avatars?: string
    domains?: string
    channels?: string
    joined?: string
    lastConnect?: string
    type?: string
    isFlagged?: boolean
    flaggedReason?: string
    constructor() {
        super()
    }

    displayJoined() { 
        //TODO:
    }
    displayLastJoined() {
        //TODO:
    }

    addCredentials(connection: SDKConnection): void {
        if(connection.getCredentials() == undefined) throw new SDKException(
            "SDKConnection: You must establish a connection."
        )
        this.application = connection.getCredentials().getApplicationId()
        let domain = connection.getDomain()
        if(domain != undefined) {
            this.domain = domain.id
        }
    }

    parseXML(element: any) {
        //Attributes
        let reader = new XMLReader(element)
        this.user = reader.readAttribute('user')
        this.name = reader.readAttribute('name')
        this.showName = reader.readAttribute('showName')
        this.token = reader.readAttribute('token')
        this.email = reader.readAttribute('email')
        this.hint = reader.readAttribute('hint')
        this.website = reader.readAttribute('website')
        this.connects = reader.readAttribute('connects')
        this.bots = reader.readAttribute('bots')
        this.posts = reader.readAttribute('posts')
        this.messages = reader.readAttribute('messages')
        this.forums = reader.readAttribute('forums')
        this.channels = reader.readAttribute('channels')
        this.avatars = reader.readAttribute('avatars')
        this.scripts = reader.readAttribute('scripts')
        this.graphics = reader.readAttribute('graphics')
        this.domains = reader.readAttribute('domains')
        this.joined = reader.readAttribute('joined')
        this.lastConnect = reader.readAttribute('lastConnect')
        this.type = reader.readAttribute('type')
        this.isFlagged = reader.readAttribute('isFlagged')
        
        //Elements
        this.bio = reader.readElement('bio')
        this.avatar = reader.readElement('avatar')
        this.flaggedReason = reader.readElement('flaggedReason')
    }

    toXML(): string {
        let writer = new XMLWriter("<user")
        this.writeCredentials(writer)
        writer.appendAttribute("password", this.password)
        writer.appendAttribute("newPassword", this.newPassword)
        writer.appendAttribute("hint", this.hint)
        writer.appendAttribute("name", this.name)
        writer.appendAttribute("showName", this.showName)
        writer.appendAttribute("email", this.email)
        writer.appendAttribute("website", this.website)
        writer.appendAttribute("over18", this.over18)
        writer.append(">")
        writer.appendElement("bio",this.bio, true)
        writer.append("</user>")
        return writer.toString()
    }

}

export default UserConfig