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

import { XMLReader } from '../util/Utils'
import Config from './Config'
import InputConfig from './InputConfig'

/**
 * DTO for XML conversation config.
 */


export default class ConversationConfig extends Config {
    id?:string
    creationDate?:string
    type?:string
    input:Array<InputConfig> = new Array<InputConfig>()


    parseXML(element: any): void {
        let reader = new XMLReader(element)
        this.id = reader.readAttribute('id')
        this.creationDate = reader.readAttribute('creationDate')
        this.type = reader.readAttribute('type')
        //All Elements in replies
        const elements = reader.readElements('input')
        elements.forEach((element) => {
            let config = new InputConfig()
            config.parseXML(element)
            this.input.push(config)
        })
    }

    displayCreationDate(): string {
        //TODO: DateFormate
        if(!this.creationDate) {
            return ""
        }
        return this.creationDate
    }

}