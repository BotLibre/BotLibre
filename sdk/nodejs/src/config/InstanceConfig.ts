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

import { XMLReader, XMLWriter } from "../util/Utils"
import WebMediumConfig from "./WebMediumConfig"

/**
 * DTO for XML bot instance config.
 */

export default class InstanceConfig extends WebMediumConfig {
    size?:string
    instanceAvatar?: string
    allowForking?:boolean
    hasAPI?:boolean
    template?:string
    rank?:string
    wins?:string
    losses?:string

    getType(): string {
        return "instance"
    }

    stats(): string {
        return this.connects + " connects, " + this.dailyConnects + " today, " + this.weeklyConnects + " week, " + this.monthlyConnects + " month"
    }

    credentials(): WebMediumConfig {
        let config = new InstanceConfig()
        config.id = this.id
        return config
    }

    toXML(): string {
        let writer = new XMLWriter('<instance')
        writer.appendAttribute('allowForking', this.allowForking)
        writer.appendAttribute('instanceAvatar', this.instanceAvatar)
        this.writeXML(writer)
        writer.appendElement('template', this.template)
        writer.append('</instance>')
        return writer.toString()
    }

    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.allowForking = reader.readAttribute('allowForking')
        this.hasAPI = reader.readAttribute('hasAPI')
        this.size = reader.readAttribute('size')
        this.instanceAvatar = reader.readAttribute('instanceAvatar')
        this.rank = reader.readAttribute('rank')
        this.wins = reader.readAttribute('wins')
        this.losses = reader.readAttribute('losses')
        //Element
        this.template = reader.readElement('template')
    }
}