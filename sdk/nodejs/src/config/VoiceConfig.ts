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
 * DTO for XML voice config.
 */

export default class VoiceConfig extends Config {
    voice?:string
    nativeVoice?:boolean
    language?:string
    pitch?:string
    speechRate?:string
    mod?:string


    parseXML(element: any): void {
        super.parseXML(element)
        let reader = new XMLReader(element)
        this.voice = reader.readAttribute('voice')
        this.nativeVoice = reader.readAttribute('nativeVoice')
        this.language = reader.readAttribute('language')
        this.pitch = reader.readAttribute('pitch')
        this.mod = reader.readAttribute('mod')
    }

    toXML(): string {
        let writer = new XMLWriter('<voice')
        this.writeCredentials(writer)
        writer.appendAttribute('voice', this.voice)
        writer.appendAttribute('nativeVoice', this.nativeVoice)
        writer.appendAttribute('language', this.language)
        writer.appendAttribute('pitch', this.pitch)
        writer.appendAttribute('speechRate', this.speechRate)
        writer.appendAttribute('mod', this.mod)
        writer.append('/>')
        return writer.toString()
    }
}