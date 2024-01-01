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

import Config from "./Config"
import { XMLReader, XMLWriter } from "../util/Utils"
/**
 * DTO for XML content config.
 */
export default class UserMessageConfig extends Config {
    id?:string
    creationDate?:string
    owner?:string
    creator?:string
    target?:string
    parent?:string
    subject?:string
    message?:string

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.id = reader.readAttribute('id')
        this.creationDate = reader.readAttribute('creationDate')
        this.owner = reader.readAttribute('owner')
        this.creator = reader.readAttribute('creator')
        this.target = reader.readAttribute('target')
        this.parent = reader.readAttribute('parent')
        //Elements
        this.subject = reader.readElement('subject')
        this.message = reader.readElement('message')
    }

    toXML(): string {
        let writer = new XMLWriter('<user-message')
        this.writeCredentials(writer)
        writer.appendAttribute('id', this.id)
        writer.appendAttribute('creationDate', this.creationDate)
        writer.appendAttribute('owner', this.owner)
        writer.appendAttribute('creator', this.creator)
        writer.appendAttribute('target', this.target)
        writer.appendAttribute('parent', this.parent)
        writer.append('>')
        //Elements
        writer.appendElement('subject', this.subject, true)
        writer.appendElement('message', this.message, true)
        writer.append('</user-message>')
        return writer.toString()

    }
}