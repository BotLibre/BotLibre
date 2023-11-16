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

import SDKConnection from '../sdk/SDKConnection'
import SDKException from '../sdk/SDKException'
import {XMLWriter, XMLReader} from '../util/Utils'
class Config {
    application?: string
    domain?: string
    user?: string
    token?: string
    instance?: string
    type?: string
    
    addCredentials(connection: SDKConnection) {
        if(connection.getCredentials() == undefined) throw new SDKException(
            "SDKConnection: You must establish a connection."
        )
        this.application = connection.getCredentials().getApplicationId()
        let user = connection.getUser()
        if(this.user == undefined && user!=undefined) {
            this.user = user.user
            this.token = user.token
        }
        let domain = connection.getDomain()
        if(domain != undefined && this.domain == undefined) {
            this.domain = domain.id
        }
    }

    getType(): string {
        return "domain"
    }

    toXML(): string {
        return ("<config/>")
    }

    parseXML(element: any) {
        let reader = new XMLReader(element)
        this.application = reader.readAttribute('application')
        this.domain = reader.readAttribute('domain')
        this.user = reader.readAttribute('user')
        this.token = reader.readAttribute('token')
        this.instance = reader.readAttribute('instance')
    }
    
    writeCredentials(writer: XMLWriter): XMLWriter {
        writer.appendAttribute("user", this.user)
        writer.appendAttribute("token", this.token)
        writer.appendAttribute("type", this.type)
        writer.appendAttribute("instance", this.instance)
        writer.appendAttribute("application", this.application)
        writer.appendAttribute("domain", this.domain)
        return writer
    }
}

export default Config