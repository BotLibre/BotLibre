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
import { XMLReader } from '../util/Utils'

class ChatResponse extends Config {
    message?: string
    question?: string
    log?: string
    conversation?: string
    emote?: string
    action?: string
    pose?: string
    avatar?: string
    avatar2?: string
    avatar3?: string
    avatar4?: string
    avatar5?: string
    avatarType?: string
    avatarTalk?: string
    avatarTalkType?: string
    avatarAction?: string
    avatarActionType?: string
    avatarActionAudio?: string
    avatarActionAudioType?: string
    avatarAudio?: string
    avatarAudioType?: string
    avatarBackground?: string
    speech?: string
    command?: string
    constructor() { super() }

    isVideo(): boolean {
        return this.avatarType != undefined && this.avatarType.includes("video")
    }
    isVideoTalk(): boolean {
        return this.avatarTalkType != undefined && this.avatarTalkType.includes("video")
    }

    parseXML(element: any): void {
        //Attributes
        let reader = new XMLReader(element)
        this.conversation = reader.readAttribute("conversation")
        this.emote = reader.readAttribute("emote")
        this.action = reader.readAttribute("action")
        this.pose = reader.readAttribute("pose")
        this.avatar = reader.readAttribute("avatar")
        this.avatar2 = reader.readAttribute("avatar2")
        this.avatar3 = reader.readAttribute("avatar3")
        this.avatar4 = reader.readAttribute("avatar4")
        this.avatar5 = reader.readAttribute("avatar5")
        this.avatarType = reader.readAttribute("avatarType")
        this.avatarTalk = reader.readAttribute("avatarTalk")
        this.avatarTalkType = reader.readAttribute("avatarTalkType")
        this.avatarAction = reader.readAttribute("avatarAction")
        this.avatarActionType = reader.readAttribute("avatarActionType")
        this.avatarActionAudio = reader.readAttribute("avatarActionAudio")
        this.avatarActionAudioType = reader.readAttribute("avatarActionAudioType")
        this.avatarAudio = reader.readAttribute("avatarAudio")
        this.avatarAudioType = reader.readAttribute("avatarAudioType")
        this.avatarBackground = reader.readAttribute("avatarBackground")
        this.speech = reader.readAttribute("speech")
        this.command = reader.readAttribute("command")
        

        //Elements
        this.message = reader.readElement("message")
        this.question = reader.readElement("question")
    }
}

export default ChatResponse