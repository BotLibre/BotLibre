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
 * DTO for XML media config.
 */

export default class MediaConfig extends Config {
    id?: number
    name?: string
    type?: string
    file?: string
    key?: string
    parseXML(element: any): void {
        let reader = new XMLReader(element)
        this.id = reader.readAttribute('id')
        this.type = reader.readAttribute('type')
        this.file = reader.readAttribute('file')
        this.key = reader.readAttribute('key')
    }

    toXML(): string {
        let writer = new XMLWriter('<media')
        this.writeCredentials(writer)
        writer.appendAttribute('id', this.id)
        writer.appendAttribute('name', this.name)
        writer.appendAttribute('type', this.type)
        writer.appendAttribute('file', this.file)
        writer.appendAttribute('key', this.key)
        writer.append('/>')
        return writer.toString()
    }
}