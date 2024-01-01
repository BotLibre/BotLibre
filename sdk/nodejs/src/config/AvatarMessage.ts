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

import { XMLWriter } from "../util/Utils";
import Config from "./Config";

export default class AvatarMessage extends Config {
    message?: string
    avatar?:string
    emote?:string
    action?:string
    pose?:string
    speak?:boolean
    voice?:string
    format?: string
    hd?:boolean
    constructor(){
        super()
    }
    toXML(): string{
        let writer = new XMLWriter("<avatar-message")
        this.writeCredentials(writer)
        writer.appendAttribute('avatar', this.avatar)
        writer.appendAttribute('emote', this.emote)
        writer.appendAttribute('action', this.action)
        writer.appendAttribute('pose', this.pose)
        writer.appendAttribute('format', this.format)
        writer.appendAttribute('voice', this.voice)
        writer.appendAttribute('speak', this.speak)
        writer.appendAttribute('hd', this.hd)
        writer.append(">")
        writer.appendElement('message', this.message, true)
        writer.append('</avatar-message>')
        return writer.toString()
    }
}