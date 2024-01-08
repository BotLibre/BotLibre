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

import { XMLReader, XMLWriter } from '../util/Utils'
import Config from './Config'

/**
 * DTO for XML content config.
 */

export default class ContentConfig extends Config {
    type?:string
    name?:string
    icon?:string
    description?:string
    constructor(){super()}
    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element);
        this.type = reader.readAttribute('type')
        this.name = reader.readAttribute('name')
        this.icon = reader.readAttribute('icon')
        this.description = reader.readElement('description')
    }
    toXML(): string {
        let writer = new XMLWriter('<content')
        this.writeCredentials(writer)
        writer.append('/>')
        return writer.toString()
    }
}