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
import {XMLWriter, Utils} from '../util/Utils'

class ChatConfig extends Config {
    conversation?:string
    correction?:string
    offensive?:string
    disconnect?:string
    emote?:string
    action?:string
    message?:string
    speak?:string
    includeQuestion?:string
    avatarHD?:string
    avatarFormat?:string
    avatar?:string
    language?:string
    voice?:string
    constructor() {super()}
    toXML(): string {
        let writer = new XMLWriter("<chat")
        this.writeCredentials(writer)
        writer.appendAttribute("conversation", this.conversation)
        writer.appendAttribute("emote", this.emote)
        writer.appendAttribute("action", this.action)
        writer.appendAttribute("correction", this.correction)
        writer.appendAttribute("offensive", this.offensive)
        writer.appendAttribute("speak", this.speak)
        writer.appendAttribute("avatar", this.avatar)
        writer.appendAttribute("avatarHD", this.avatarHD)
        writer.appendAttribute("avatarFormat", this.avatarFormat)
        writer.appendAttribute("language", this.language)
        writer.appendAttribute("voice", this.voice)
        writer.appendAttribute("includeQuestion", this.includeQuestion)
        writer.appendAttribute("disconnect", this.disconnect)
        writer.append(">")
        writer.appendElement("message", this.message, true)
        writer.append("</chat>")
        return writer.toString()
    }
}

export default ChatConfig