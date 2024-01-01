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

import { XMLReader, XMLWriter } from "../util/Utils";
import WebMediumConfig from "./WebMediumConfig";

export default class ChannelConfig extends WebMediumConfig {
    type?: string
    videoAccessMode?: string
    audioAccessMode?: string
    messages?: string
    usersOnline?: string
    adminsOnline?: string
    getType(): string {
        return "channel"
    }
    stats():string {
        return this.usersOnline + " users online, " + this.adminsOnline + " admins"
    }
    credentials(): WebMediumConfig {
        let config = new ChannelConfig()
        config.id = this.id
        return config
    }
    toXML(): string {
        let writer = new XMLWriter("<channel")
        writer.appendAttribute('type', this.type)
        writer.appendAttribute('videoAccessMode', this.videoAccessMode)
        writer.appendAttribute('audioAccessMode', this.audioAccessMode)
        this.writeXML(writer)
        writer.append('</channel>')
        return writer.toString()
    }
    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.type = reader.readAttribute('type')
        this.videoAccessMode = reader.readAttribute('videoAccessMode')
        this.audioAccessMode = reader.readAttribute('audioAccessMode')
        this.usersOnline = reader.readAttribute('usersOnline')
        this.adminsOnline = reader.readAttribute('adminsOnline')
    }
}